/* ===============================================================================
*
* Part of the InfoGlue Content Management Platform (www.infoglue.org)
*
* ===============================================================================
*
*  Copyright (C)
* 
* This program is free software; you can redistribute it and/or modify it under
* the terms of the GNU General Public License version 2, as published by the
* Free Software Foundation. See the file LICENSE.html for more information.
* 
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License along with
* this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
* Place, Suite 330 / Boston, MA 02111-1307 / USA.
*
* ===============================================================================
*/
package org.infoglue.deliver.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.SmallestContentVersionVO;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallishContentImpl;
import org.infoglue.cms.entities.management.AccessRightVO;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl;
import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.CategoryImpl;
import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.GroupImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.management.impl.simple.RoleImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallAvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallSystemUserImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserRoleImpl;
import org.infoglue.cms.entities.publishing.PublicationDetailVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationDetailImpl;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
import org.infoglue.cms.services.CacheEvictionBeanListenerService;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
import org.infoglue.deliver.applications.filters.URIMapperCache;
import org.infoglue.deliver.cache.PageCacheHelper;
import org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.DigitalAssetDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;

/**
 * @author mattias
 *
 * This is a selective publication thread. What that means is that it only throws
 * away objects and pages in the cache which are affected. Experimental for now.
 */
public class SelectiveLivePublicationThread extends PublicationThread
{
    public final static Logger logger = Logger.getLogger(SelectiveLivePublicationThread.class.getName());
	private static VisualFormatter formatter = new VisualFormatter();

    private List cacheEvictionBeans = new ArrayList();
    private List notifications = null;
    
    public SelectiveLivePublicationThread(List notifiations)
	{
    	this.notifications = notifiations;
	}
	

	public synchronized void run()
	{
		logger.warn("Run in SelectiveLivePublicationThread....");
		
		int publicationDelay = 5000;
	    String publicationThreadDelay = CmsPropertyHandler.getPublicationThreadDelay();
	    if(publicationThreadDelay != null && !publicationThreadDelay.equalsIgnoreCase("") && publicationThreadDelay.indexOf("publicationThreadDelay") == -1)
	        publicationDelay = Integer.parseInt(publicationThreadDelay);
	    
	    Random r = new Random();
	    int randint = (Math.abs(r.nextInt()) % 11) / 8 * 1000;
	    publicationDelay = publicationDelay + randint;
	    
	    logger.info("\n\n\nSleeping " + publicationDelay + "ms.\n\n\n");
		try 
		{
			sleep(publicationDelay);
		} 
		catch (InterruptedException e1) 
		{
			e1.printStackTrace();
		}

    	logger.warn("before cacheEvictionBeans:" + cacheEvictionBeans.size());
	    synchronized(notifications)
        {
	    	cacheEvictionBeans.addAll(notifications);
	    	notifications.clear();
	    	this.notifications = null;
        }
	    
	    boolean processedServerNodeProperties = false;
	    try
	    {
		    Iterator cacheEvictionBeansIterator = cacheEvictionBeans.iterator();
		    while(cacheEvictionBeansIterator.hasNext())
		    {
		    	CacheEvictionBean cacheEvictionBean = (CacheEvictionBean)cacheEvictionBeansIterator.next();
			    String className = cacheEvictionBean.getClassName();
			    if(className == null)
			    	logger.error("No className in CacheEvictionBean");
			    if(cacheEvictionBean.getObjectName() == null)
			    	logger.error("No objectName in CacheEvictionBean");
			    	
			    if(className.equalsIgnoreCase("ServerNodeProperties"))
			    {
			    	if(processedServerNodeProperties || cacheEvictionBean.getObjectName().equals("MySettings"))
			    	{
			    		cacheEvictionBeansIterator.remove();
			    		//logger.info("Removed one ServerNodeProperties update as it will be processed anyway in this eviction cycle");
			    	}
			    	else
			    	{
			    		processedServerNodeProperties = true;
			    	}
			    }
		    }
	    }
	    catch (Exception e) 
	    {
	    	logger.error("Error in selective live publication thread. Could not process eviction beans part 1: " + e.getMessage(), e);
		}
		    
		logger.info("cacheEvictionBeans.size:" + cacheEvictionBeans.size() + ":" + RequestAnalyser.getRequestAnalyser().getBlockRequests());
		if(cacheEvictionBeans.size() > 0)
		{
			try
			{		
				Timer t = new Timer();

				logger.info("setting block");
		        RequestAnalyser.getRequestAnalyser().setBlockRequests(true);
		        
		        //logger.info("cacheEvictionBeans:" + cacheEvictionBeans.size());
		        boolean accessRightsFlushed = false;
		        List<String> processedEntities = new ArrayList<String>();
				Iterator i = cacheEvictionBeans.iterator();
				while(i.hasNext())
				{
				    CacheEvictionBean cacheEvictionBean = (CacheEvictionBean)i.next();
				    
				    boolean processedInterupted = false;
				    boolean skipOriginalEntity = false;
				    try
				    {
					    RequestAnalyser.getRequestAnalyser().addOngoingPublications(cacheEvictionBean);
					    
					    String className = cacheEvictionBean.getClassName();
					    String objectId = cacheEvictionBean.getObjectId();
					    String objectName = cacheEvictionBean.getObjectName();
						String typeId = cacheEvictionBean.getTypeId();
						
					    List<Map<String,String>> allIGCacheCalls = new ArrayList<Map<String,String>>();

						logger.info("className:" + className + " objectId:" + objectId + " objectName: " + objectName + " typeId: " + typeId);
					    if(className.indexOf("AccessRight") > -1)
					    {
					    	logger.info("Special handling of access rights..");
					    	if(!accessRightsFlushed)
					    	{
						        CacheController.clearCache(AccessRightImpl.class);
						        CacheController.clearCache(AccessRightRoleImpl.class);
						        CacheController.clearCache(AccessRightGroupImpl.class);
						        CacheController.clearCache(AccessRightUserImpl.class);

					    		CacheController.clearCache("personalAuthorizationCache");
					    		accessRightsFlushed = true;
					    	}
					    	
					    	skipOriginalEntity = true;
					    	
					    	try
					    	{
						    	AccessRightVO acVO = AccessRightController.getController().getAccessRightVOWithId(new Integer(objectId));
						    	InterceptionPointVO icpVO = InterceptionPointController.getController().getInterceptionPointVOWithId(acVO.getInterceptionPointId());
						    	if(!processedEntities.contains("" + icpVO.getCategory() + "_" + acVO.getParameters()))
						    	{
						    		logger.info("icpVO:" + icpVO.getName());
							    	if(icpVO.getName().indexOf("Content.") > -1)
							    	{
							    		logger.info("Was a content access... let's clear caches for that content.");
							    		String idAsString = acVO.getParameters();
							    		if(idAsString != null && !idAsString.equals(""))
							    			addCacheUpdateDirective("org.infoglue.cms.entities.content.impl.simple.ContentImpl", idAsString, allIGCacheCalls);
							    	}
							    	else if(icpVO.getName().indexOf("ContentVersion.") > -1)
							    	{
							    		logger.info("Was a contentversion access... let's clear caches for that content.");
							    		String idAsString = acVO.getParameters();
							    		if(idAsString != null && !idAsString.equals(""))
							    			addCacheUpdateDirective("org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl", idAsString, allIGCacheCalls);
							    	}
								    else if(icpVO.getName().indexOf("SiteNode.") > -1)
								    {
								    	logger.info("Was a sitenode access... let's clear caches for that content.");
							    		String idAsString = acVO.getParameters();
							    		if(idAsString != null && !idAsString.equals(""))
							    			addCacheUpdateDirective("org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl", idAsString, allIGCacheCalls);
								    }
									else if(icpVO.getName().indexOf("SiteNodeVersion.") > -1)
									{
										logger.info("Was a sitenode version access... let's clear caches for that content.");
							    		String idAsString = acVO.getParameters();
							    		if(idAsString != null && !idAsString.equals(""))
							    			addCacheUpdateDirective("org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl", idAsString, allIGCacheCalls);
									}
									else
									{
										logger.info("****************************");
										logger.info("* WHAT TO DO WITH: " + icpVO.getName() + " *");
										logger.info("****************************");
									}
							    	logger.info("Feeling done with " + "" + icpVO.getCategory() + "_" + acVO.getParameters());
							    	processedEntities.add("" + icpVO.getCategory() + "_" + acVO.getParameters());
						    	}
						    	else
						    		logger.info("Allready processed " + icpVO.getCategory() + "_" + acVO.getParameters());
						    }
					    	catch(Exception e2)
					    	{
					    		logger.warn("Error handling access right update: " + e2.getMessage());
					    	}
					    }
					    
				        boolean isDependsClass = false;
					    if(className != null && className.equalsIgnoreCase(PublicationDetailImpl.class.getName()))
					        isDependsClass = true;
				
					    if(!typeId.equalsIgnoreCase("" + NotificationMessage.SYSTEM))
					    {
					    	//CacheController.clearCaches(className, objectId, null);
							CacheController.setForcedCacheEvictionMode(true);
					    }
								    
					    if(!skipOriginalEntity)
					    	addCacheUpdateDirective(className, objectId, allIGCacheCalls);

					    logger.info("Updating className with id:" + className + ":" + objectId);
					    if(className != null && !typeId.equalsIgnoreCase("" + NotificationMessage.SYSTEM) && !skipOriginalEntity)
						{
						    Class type = Class.forName(className);
			
						    if(!isDependsClass && 
						    		className.equalsIgnoreCase(SystemUserImpl.class.getName()) || 
						    		className.equalsIgnoreCase(RoleImpl.class.getName()) || 
						    		className.equalsIgnoreCase(GroupImpl.class.getName()) ||
						    		className.equalsIgnoreCase(SmallSystemUserImpl.class.getName()) || 
						    		className.equalsIgnoreCase(SmallRoleImpl.class.getName()) || 
						    		className.equalsIgnoreCase(SmallGroupImpl.class.getName()) || 
						    		className.equalsIgnoreCase(SystemUserRoleImpl.class.getName()) || 
						    		className.equalsIgnoreCase(SystemUserGroupImpl.class.getName()))
						    {
						        Object[] ids = {objectId};
						        CacheController.clearCache(type, ids);
							}
						    else if(!isDependsClass)
						    {
						        Object[] ids = {new Integer(objectId)};
							    CacheController.clearCache(type, ids);
						    }
			
							//If it's an contentVersion we should delete all images it might have generated from attributes.
							if(Class.forName(className).getName().equals(ContentImpl.class.getName()))
							{
							    logger.info("We clear all small contents as well " + objectId);
								Class typesExtra = SmallContentImpl.class;
								Object[] idsExtra = {new Integer(objectId)};
								CacheController.clearCache(typesExtra, idsExtra);
	
							    logger.info("We clear all small contents as well " + objectId);
								Class typesExtraSmallish = SmallishContentImpl.class;
								Object[] idsExtraSmallish = {new Integer(objectId)};
								CacheController.clearCache(typesExtraSmallish, idsExtraSmallish);
	
								logger.info("We clear all medium contents as well " + objectId);
								Class typesExtraMedium = MediumContentImpl.class;
								Object[] idsExtraMedium = {new Integer(objectId)};
								CacheController.clearCache(typesExtraMedium, idsExtraMedium);
							}
							if(Class.forName(className).getName().equals(ContentVersionImpl.class.getName()))
							{
							    logger.info("We clear all small contents as well " + objectId);
								Class typesExtra = SmallContentVersionImpl.class;
								Object[] idsExtra = {new Integer(objectId)};
								CacheController.clearCache(typesExtra, idsExtra);
	
							    logger.info("We clear all small contents as well " + objectId);
								Class typesExtraSmallest = SmallestContentVersionImpl.class;
								Object[] idsExtraSmallest = {new Integer(objectId)};
								CacheController.clearCache(typesExtraSmallest, idsExtraSmallest);
							}
							else if(Class.forName(className).getName().equals(AvailableServiceBindingImpl.class.getName()))
							{
							    Class typesExtra = SmallAvailableServiceBindingImpl.class;
								Object[] idsExtra = {new Integer(objectId)};
								CacheController.clearCache(typesExtra, idsExtra);
							}
							else if(Class.forName(className).getName().equals(SiteNodeImpl.class.getName()))
							{
							    Class typesExtra = SmallSiteNodeImpl.class;
								Object[] idsExtra = {new Integer(objectId)};
								CacheController.clearCache(typesExtra, idsExtra);
							}
							else if(Class.forName(className).getName().equals(SiteNodeVersionImpl.class.getName()))
							{
							    Class typesExtra = SmallSiteNodeVersionImpl.class;
								Object[] idsExtra = {new Integer(objectId)};
								CacheController.clearCache(typesExtra, idsExtra);
							}
							else if(Class.forName(className).getName().equals(DigitalAssetImpl.class.getName()))
							{
								CacheController.clearCache("digitalAssetCache");
								Class typesExtra = SmallDigitalAssetImpl.class;
								Object[] idsExtra = {new Integer(objectId)};
								CacheController.clearCache(typesExtra, idsExtra);
								
								Class typesExtraMedium = MediumDigitalAssetImpl.class;
								Object[] idsExtraMedium = {new Integer(objectId)};
								CacheController.clearCache(typesExtraMedium, idsExtraMedium);
	
								String disableAssetDeletionInLiveThread = CmsPropertyHandler.getDisableAssetDeletionInLiveThread();
								if(disableAssetDeletionInLiveThread != null && !disableAssetDeletionInLiveThread.equals("true"))
								{
									logger.info("We should delete all images with digitalAssetId " + objectId);
									DigitalAssetDeliveryController.getDigitalAssetDeliveryController().deleteDigitalAssets(new Integer(objectId));
								}
							}
							else if(Class.forName(className).getName().equals(MediumDigitalAssetImpl.class.getName()))
							{
								CacheController.clearCache("digitalAssetCache");
								Class typesExtra = SmallDigitalAssetImpl.class;
								Object[] idsExtra = {new Integer(objectId)};
								CacheController.clearCache(typesExtra, idsExtra);
								
								Class typesExtraMedium = DigitalAssetImpl.class;
								Object[] idsExtraMedium = {new Integer(objectId)};
								CacheController.clearCache(typesExtraMedium, idsExtraMedium);
	
								String disableAssetDeletionInLiveThread = CmsPropertyHandler.getDisableAssetDeletionInLiveThread();
								if(disableAssetDeletionInLiveThread != null && !disableAssetDeletionInLiveThread.equals("true"))
								{
									logger.info("We should delete all images with digitalAssetId " + objectId);
									DigitalAssetDeliveryController.getDigitalAssetDeliveryController().deleteDigitalAssets(new Integer(objectId));
								}
							}
							else if(Class.forName(className).getName().equals(SystemUserImpl.class.getName()))
							{
							    Class typesExtra = SmallSystemUserImpl.class;
								Object[] idsExtra = {objectId};
								CacheController.clearCache(typesExtra, idsExtra);
							}
							else if(Class.forName(className).getName().equals(RoleImpl.class.getName()))
							{
							    Class typesExtra = SmallRoleImpl.class;
								Object[] idsExtra = {objectId};
								CacheController.clearCache(typesExtra, idsExtra);
							}
							else if(Class.forName(className).getName().equals(GroupImpl.class.getName()))
							{
							    Class typesExtra = SmallGroupImpl.class;
								Object[] idsExtra = {objectId};
								CacheController.clearCache(typesExtra, idsExtra);
							}
							else if(Class.forName(className).getName().equals(PublicationImpl.class.getName()))
							{
								logger.info("**************************************");
								logger.info("*    HERE THE MAGIC SHOULD HAPPEN    *");
								logger.info("**************************************");
								
								PublicationVO publicationVO = PublicationController.getController().getPublicationVO(new Integer(objectId));
								if(publicationVO != null)
								{
									List publicationDetailVOList = PublicationController.getController().getPublicationDetailVOList(new Integer(objectId));
									Iterator publicationDetailVOListIterator = publicationDetailVOList.iterator();
									while(publicationDetailVOListIterator.hasNext())
									{
										PublicationDetailVO publicationDetailVO = (PublicationDetailVO)publicationDetailVOListIterator.next();
										logger.info("publicationDetailVO.getEntityClass():" + publicationDetailVO.getEntityClass());
										logger.info("publicationDetailVO.getEntityId():" + publicationDetailVO.getEntityId());
									
										if(publicationDetailVO.getEntityClass().indexOf("pageCache") > -1)
										{
											logger.info("publicationDetailVO.getEntityClass():" + publicationDetailVO.getEntityClass());
											
											if(publicationDetailVO.getEntityClass().indexOf("pageCache:") == 0)
											{
												String groupQualifyer = publicationDetailVO.getEntityClass().substring("pageCache:".length());
												logger.info("This is a application pageCache-clear request... specific:" + groupQualifyer);
												CacheController.clearCaches(publicationDetailVO.getEntityClass(), "" + publicationDetailVO.getEntityId(), null);
											}
											else
											{
												CacheController.clearCaches("pageCache", "selectiveCacheUpdateNonApplicable", null);
											}
											
								    		//CacheController.clearCacheForGroup("pageCacheExtra", "selectiveCacheUpdateNonApplicable");
											//CacheController.clearCacheForGroup("pageCache", "selectiveCacheUpdateNonApplicable");							    		
										}
										else if(Class.forName(publicationDetailVO.getEntityClass()).getName().equals(ContentVersion.class.getName()))
										{
											logger.info("We clear all caches having references to contentVersion: " + publicationDetailVO.getEntityId());
											try
											{
												Integer contentId = ContentVersionController.getContentVersionController().getContentIdForContentVersion(publicationDetailVO.getEntityId());
											    
											    ContentVO previousContentVO = ContentController.getContentController().getContentVOWithId(contentId);
											    Integer previousParentContentId = previousContentVO.getParentContentId();
											    logger.info("previousParentContentId:" + previousParentContentId);
	
											    addCacheUpdateDirective(publicationDetailVO.getEntityClass(), publicationDetailVO.getEntityId().toString(), allIGCacheCalls);
											    //CacheController.clearCaches(publicationDetailVO.getEntityClass(), publicationDetailVO.getEntityId().toString(), null);
											    
												CacheController.clearCache(SmallContentVersionImpl.class, new Integer[]{new Integer(publicationDetailVO.getEntityId())});
												CacheController.clearCache(SmallestContentVersionImpl.class, new Integer[]{new Integer(publicationDetailVO.getEntityId())});
	
												logger.info("We clear all small contents as well " + contentId);
												CacheController.clearCache(ContentImpl.class, new Integer[]{contentId});
												CacheController.clearCache(SmallContentImpl.class, new Integer[]{contentId});
												CacheController.clearCache(SmallishContentImpl.class, new Integer[]{contentId});
												CacheController.clearCache(MediumContentImpl.class, new Integer[]{contentId});
												CacheController.clearCache(SmallSiteNodeVersionImpl.class, new Integer[]{new Integer(objectId)});		
												
												logger.info("Handling parents....");
												
												ContentVO contentVOAfter = ContentController.getContentController().getContentVOWithId(contentId);
											    Integer currentParentContentId = contentVOAfter.getParentContentId();
											    logger.info("previousParentContentId:" + previousParentContentId);
											    logger.info("currentParentContentId:" + currentParentContentId);
	
											    logger.info("We should also clear the parents...");
												if(currentParentContentId != null)
												{
													logger.info("contentVOAfter - clear the new:" + contentVOAfter.getName() + " / " + currentParentContentId);
													//CacheController.clearCaches(Content.class.getName(), currentParentContentId.toString(), null);
													addCacheUpdateDirective(Content.class.getName(), currentParentContentId.toString(), allIGCacheCalls);
													
												    logger.info("We clear all small siteNodes as well " + currentParentContentId);
													CacheController.clearCache(ContentImpl.class, new Integer[]{currentParentContentId});
													CacheController.clearCache(SmallContentImpl.class, new Integer[]{currentParentContentId});
													CacheController.clearCache(SmallishContentImpl.class, new Integer[]{currentParentContentId});
													CacheController.clearCache(MediumContentImpl.class, new Integer[]{currentParentContentId});
												}
	
												if(currentParentContentId != null && previousParentContentId != null && !previousParentContentId.equals(previousParentContentId))
												{
													logger.info("contentVOAfter - clear the new:" + contentVOAfter.getName() + " / " + currentParentContentId);
													//CacheController.clearCaches(Content.class.getName(), previousParentContentId.toString(), null);
													addCacheUpdateDirective(Content.class.getName(), previousParentContentId.toString(), allIGCacheCalls);
													
												    logger.info("We clear all small siteNodes as well " + previousParentContentId);
													CacheController.clearCache(ContentImpl.class, new Integer[]{previousParentContentId});
													CacheController.clearCache(SmallContentImpl.class, new Integer[]{previousParentContentId});
													CacheController.clearCache(SmallishContentImpl.class, new Integer[]{previousParentContentId});
													CacheController.clearCache(MediumContentImpl.class, new Integer[]{previousParentContentId});
												}
											}
											catch(Exception e)
											{
												logger.warn("An error occurred handling content version from publication " + publicationVO.getId() + ":" + e.getMessage());
											}
										}
										else if(Class.forName(publicationDetailVO.getEntityClass()).getName().equals(SiteNodeVersion.class.getName()))
										{
											try
											{
												SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(publicationDetailVO.getEntityId());
												//logger.info("siteNodeVersionVO:" + siteNodeVersionVO.getId());
												Integer siteNodeId = siteNodeVersionVO.getSiteNodeId();
												
												CacheController.clearCache("pageCacheLatestSiteNodeVersions", "" + siteNodeId);
												String versionKey = "" + siteNodeId + "_" + CmsPropertyHandler.getOperatingMode() + "_siteNodeVersionVO";		
											    CacheController.clearCache("latestSiteNodeVersionCache", versionKey);
												
											    logger.info("We also clear the meta info content..");
	
											    SiteNodeVO previousSiteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
											    Integer previousParentSiteNodeId = previousSiteNodeVO.getParentSiteNodeId();
											    logger.info("previousParentSiteNodeId:" + previousParentSiteNodeId);
											    Object previousParentSiteNodeIdCandidate = CacheController.getCachedObject("parentSiteNodeCache", "" + siteNodeId);
											    logger.info("previousParentSiteNodeIdCandidate:" + previousParentSiteNodeIdCandidate);
											    if(previousParentSiteNodeIdCandidate != null && !(previousParentSiteNodeIdCandidate instanceof NullObject))
											    	previousParentSiteNodeId = ((SiteNodeVO)previousParentSiteNodeIdCandidate).getId();
											    logger.info("previousParentSiteNodeId:" + previousParentSiteNodeId);
											    	
											    //CacheController.clearCaches(publicationDetailVO.getEntityClass(), publicationDetailVO.getEntityId().toString(), null);
												//if(siteNodeId != null)
												//	CacheController.clearCaches(SiteNode.class.getName(), siteNodeId.toString(), null);
											    
											    logger.info("We clear all small siteNodes as well " + siteNodeId);
												CacheController.clearCache(SiteNodeImpl.class, new Integer[]{siteNodeId});
												CacheController.clearCache(SmallSiteNodeImpl.class, new Integer[]{siteNodeId});
												CacheController.clearCache(SmallSiteNodeVersionImpl.class, new Integer[]{new Integer(publicationDetailVO.getEntityId())});		
	
												logger.info("We clear all contents as well " + previousSiteNodeVO.getMetaInfoContentId());
												Class metaInfoContentExtra = ContentImpl.class;
												Object[] idsMetaInfoContentExtra = {previousSiteNodeVO.getMetaInfoContentId()};
												CacheController.clearCache(metaInfoContentExtra, idsMetaInfoContentExtra);
												
												logger.info("We clear all small contents as well " + previousSiteNodeVO.getMetaInfoContentId());
												Class metaInfoContentExtraSmall = SmallContentImpl.class;
												CacheController.clearCache(metaInfoContentExtraSmall, idsMetaInfoContentExtra);
												
												logger.info("We clear all smallish contents as well " + previousSiteNodeVO.getMetaInfoContentId());
												Class metaInfoContentExtraSmallish = SmallishContentImpl.class;
												CacheController.clearCache(metaInfoContentExtraSmallish, idsMetaInfoContentExtra);
			
												logger.info("We clear all medium contents as well " + previousSiteNodeVO.getMetaInfoContentId());
												Class metaInfoContentExtraMedium = MediumContentImpl.class;
												CacheController.clearCache(metaInfoContentExtraMedium, idsMetaInfoContentExtra);
												
												//CacheController.clearCaches(ContentImpl.class.getName(), previousSiteNodeVO.getMetaInfoContentId().toString(), null);
												addCacheUpdateDirective(ContentImpl.class.getName(), previousSiteNodeVO.getMetaInfoContentId().toString(), allIGCacheCalls);
			
												Database db = CastorDatabaseService.getDatabase();
												db.begin();
												
												LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(previousSiteNodeVO.getRepositoryId(), db);
												ContentVersionVO metaInfoContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(previousSiteNodeVO.getMetaInfoContentId(), masterLanguageVO.getId(), db);
												addCacheUpdateDirective(ContentVersionImpl.class.getName(), metaInfoContentVersionVO.getId().toString(), allIGCacheCalls);
												
												List contentVersionIds = new ArrayList();
												if(previousSiteNodeVO.getMetaInfoContentId() != null)
												{
													List<SmallestContentVersionVO> contentVersionVOList = ContentVersionController.getContentVersionController().getSmallestContentVersionVOList(previousSiteNodeVO.getMetaInfoContentId(), db);
													for(SmallestContentVersionVO cvVO : contentVersionVOList)
													{
														contentVersionIds.add(cvVO.getId());
														logger.info("We clear the meta info contentVersion " + cvVO.getId());
													}
												}
												/*
												Content content = ContentController.getContentController().getReadOnlyContentWithId(previousSiteNodeVO.getMetaInfoContentId(), db);
												List contentVersionIds = new ArrayList();
												Iterator contentVersionIterator = content.getContentVersions().iterator();
												logger.info("Versions:" + content.getContentVersions().size());
												while(contentVersionIterator.hasNext())
												{
													ContentVersion contentVersion = (ContentVersion)contentVersionIterator.next();
													contentVersionIds.add(contentVersion.getId());
													logger.info("We clear the meta info contentVersion " + contentVersion.getId());
												}
												*/
			
												db.rollback();
			
												db.close();
												
												Iterator contentVersionIdsIterator = contentVersionIds.iterator();
												logger.info("Versions:" + contentVersionIds.size());
												while(contentVersionIdsIterator.hasNext())
												{
													Integer contentVersionId = (Integer)contentVersionIdsIterator.next();
													logger.info("We clear the meta info contentVersion " + contentVersionId);
													Class metaInfoContentVersionExtra = ContentVersionImpl.class;
													Object[] idsMetaInfoContentVersionExtra = {contentVersionId};
													CacheController.clearCache(metaInfoContentVersionExtra, idsMetaInfoContentVersionExtra);
													//CacheController.clearCaches(ContentVersionImpl.class.getName(), contentVersionId.toString(), null);
													//addCacheUpdateDirective(ContentVersionImpl.class.getName(), contentVersionId.toString(), allIGCacheCalls);
												}
												
												logger.info("After:" + contentVersionIds.size());
			
												SiteNodeVersionVO previousSiteNodeVersionVO = SiteNodeVersionController.getController().getPreviousActiveSiteNodeVersionVO(siteNodeVersionVO.getSiteNodeId(), siteNodeVersionVO.getId(), new Integer(CmsPropertyHandler.getOperatingMode()));
												//logger.info("previousSiteNodeVersionVO:" + previousSiteNodeVersionVO.getId());
	
												addCacheUpdateDirective(publicationDetailVO.getEntityClass(), publicationDetailVO.getEntityId().toString(), allIGCacheCalls);
												if(siteNodeId != null)
												{
													//logger.info("What really happened.... let's find out");
													boolean anyRealDifferences = isThereAnyRealDifferencesBetweenSiteNodeVersions(siteNodeVersionVO, previousSiteNodeVersionVO);
													//logger.info("anyRealDifferences:" + anyRealDifferences);
													if(anyRealDifferences)
														addCacheUpdateDirective(SiteNode.class.getName(), siteNodeId.toString(), allIGCacheCalls);
													else
													{
														//logger.info("We'll skip it and assume that this was just a meta info update...");
													}
												}
	
												//Handling access rights...
												if(siteNodeVersionVO.getIsProtected().intValue() != SiteNodeVersionVO.INHERITED || (previousSiteNodeVersionVO != null && previousSiteNodeVersionVO.getIsProtected().intValue() != SiteNodeVersionVO.INHERITED))
												{
											        CacheController.clearCache(AccessRightImpl.class);
											        CacheController.clearCache(AccessRightRoleImpl.class);
											        CacheController.clearCache(AccessRightGroupImpl.class);
											        CacheController.clearCache(AccessRightUserImpl.class);
	
										    		CacheController.clearCache("personalAuthorizationCache");
												}
												
												logger.info("Handling parents....");
												
												SiteNodeVO siteNodeVOAfter = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
											    Integer currentParentSiteNodeId = siteNodeVOAfter.getParentSiteNodeId();
											    logger.info("previousParentSiteNodeId:" + previousParentSiteNodeId);
											    logger.info("currentParentSiteNodeId:" + currentParentSiteNodeId);
	
											    logger.info("We should also clear the parents...");
												if(currentParentSiteNodeId != null)
												{
													if(previousSiteNodeVersionVO == null)
													{
														//logger.info("Looks to be first version - let's update parent as well");
														logger.info("siteNodeVOAfter - clear the new:" + siteNodeVOAfter.getName() + " / " + currentParentSiteNodeId);
														addCacheUpdateDirective(SiteNode.class.getName(), currentParentSiteNodeId.toString(), allIGCacheCalls);
													}
													
												    logger.info("We clear all small siteNodes as well " + currentParentSiteNodeId);
													CacheController.clearCache(SiteNodeImpl.class, new Integer[]{currentParentSiteNodeId});
													CacheController.clearCache(SmallSiteNodeImpl.class, new Integer[]{currentParentSiteNodeId});
												}
	
												if(currentParentSiteNodeId != null && previousParentSiteNodeId != null && !previousParentSiteNodeId.equals(currentParentSiteNodeId))
												{
													logger.info("siteNodeVOAfter was not the same - lets clear the old:" + siteNodeVOAfter.getName() + " / " + currentParentSiteNodeId);
													//CacheController.clearCaches(SiteNode.class.getName(), previousParentSiteNodeId.toString(), null);
													addCacheUpdateDirective(SiteNode.class.getName(), currentParentSiteNodeId.toString(), allIGCacheCalls);
													addCacheUpdateDirective(SiteNode.class.getName(), previousParentSiteNodeId.toString(), allIGCacheCalls);
													
												    logger.info("We clear all small siteNodes as well " + previousParentSiteNodeId);
													CacheController.clearCache(SiteNodeImpl.class, new Integer[]{previousParentSiteNodeId});
													CacheController.clearCache(SmallSiteNodeImpl.class, new Integer[]{previousParentSiteNodeId});
												}
											}
											catch (Exception e) 
											{
												logger.warn("An error occurred handling sitenode version from publication " + publicationVO.getId() + ":" + e.getMessage());
											}
										}
										
									}
								}
								else
								{
									long diff = System.currentTimeMillis() - cacheEvictionBean.getReceivedTimestamp();
									if(diff < 1000*60)
									{
										processedInterupted = true;
										logger.warn("Could not find publication in database. It may be a replication delay issue - lets try again.");
										synchronized(CacheController.notifications)
								        {
									    	CacheController.notifications.add(cacheEvictionBean);
								        }
									}
									else
									{
										logger.warn("Could not find publication in database. It may be a replication delay issue but now it's been very long so we have to abort.");
									}
								}
							}
							
						    long elapsedTime = t.getElapsedTime();
						    if(elapsedTime > 100)
						    	logger.warn("Cleared all castor caches for " + className + ":" + objectId + " took");

							for(Map<String,String> igCacheCall : allIGCacheCalls)
							{
								logger.info("Calling clear caches with:" + igCacheCall.get("className") + ":" + igCacheCall.get("objectId"));
								CacheController.clearCaches(igCacheCall.get("className"), igCacheCall.get("objectId"), null);
								
							    elapsedTime = t.getElapsedTime();
							    if(elapsedTime > 100)
							    	logger.warn("Clearing all caches for " + igCacheCall.get("className") + ":" + igCacheCall.get("objectId") + " took");
							}

							if(CmsPropertyHandler.getServerNodeProperty("recacheEntities", true, "false").equals("true"))
								recacheEntities(cacheEvictionBean);
						}	
						else if(!skipOriginalEntity)
						{
							/*
							logger.info("Was notification message in selective live publication...");
							logger.info("className:" + className);
							logger.info("objectId:" + objectId);
							logger.info("objectName:" + objectName);
							logger.info("typeId:" + typeId);
							*/
							if(className.equals("ServerNodeProperties"))
							{
								logger.info("clearing InfoGlueAuthenticationFilter");
								CacheController.clearServerNodeProperty(true);
								logger.info("cleared InfoGlueAuthenticationFilter");
								InfoGlueAuthenticationFilter.initializeProperties();
								logger.info("initialized InfoGlueAuthenticationFilter");
								logger.info("Shortening page stats");
								RequestAnalyser.shortenPageStatistics();
	
							    logger.info("Updating all caches from SelectiveLivePublicationThread as this was a publishing-update\n\n\n");
							    //CacheController.clearCastorCaches();

							    String[] excludedCaches = CacheController.getPublicationPersistentCacheNames();
								logger.info("clearing all except " + excludedCaches + " as we are in publish mode..\n\n\n");											
								CacheController.clearCaches(null, null, excludedCaches);
							    
								//logger.info("Recaching all caches as this was a publishing-update\n\n\n");
								//CacheController.cacheCentralCastorCaches();
								CacheController.clearCastorCaches();
								logger.info("Cleared all castor caches...");
								
								//logger.info("Finally clearing page cache and other caches as this was a publishing-update\n\n\n");
								logger.info("Finally clearing page cache and some other caches as this was a publishing-update\n\n\n");
								//CacheController.clearCache("ServerNodeProperties");
								//CacheController.clearCache("serverNodePropertiesCache");
								
							    CacheController.clearCache("boundContentCache");
						        //CacheController.clearFileCaches("pageCache");
								PageCacheHelper.getInstance().clearPageCache();
								
								CacheController.clearCache("pageCache");
								CacheController.clearCache("pageCacheExtra");
								CacheController.clearCache("componentCache");
								CacheController.clearCache("NavigationCache");
								CacheController.clearCache("pagePathCache");
								CacheController.clearCache("pageCacheParentSiteNodeCache");
								CacheController.clearCache("pageCacheLatestSiteNodeVersions");
								CacheController.clearCache("pageCacheSiteNodeTypeDefinition");
								
							}
							else if(className.equalsIgnoreCase("PortletRegistry"))
						    {
								logger.info("clearing portletRegistry");
								CacheController.clearPortlets();
								logger.info("cleared portletRegistry");
						    }
							else if(className.indexOf("RepositoryImpl") > -1)
						    {
								logger.info("clearing repo affecting stuff");
								CacheController.clearServerNodeProperty(true);

								Class repoClass = RepositoryImpl.class;
								CacheController.clearCache(repoClass);
								CacheController.clearCaches(repoClass.getName(), null, null);

								CacheController.clearCache("repositoryCache");
								CacheController.clearCache("masterRepository");
								CacheController.clearCache("parentRepository");
								CacheController.clearCache("componentPropertyCache");
						        //CacheController.clearFileCaches("pageCache");
								PageCacheHelper.getInstance().clearPageCache();

						        CacheController.clearCache("pageCache");
								CacheController.clearCache("pageCacheExtra");
								CacheController.clearCache("componentCache");
								CacheController.clearCache("NavigationCache");
								CacheController.clearCache("pagePathCache");
						    	URIMapperCache.getInstance().clear();
								logger.info("cleared repo affecting stuff");
						    }
							else
							{
								logger.info("This was an deviation: " + className);
								Class type = Class.forName(className);
						        Object[] ids = {objectId};
						        CacheController.clearCache(type, ids);
						        CacheController.clearCache(type);
						    	CacheController.clearCaches(className, objectId, null);
						    	
						    	logger.info("Clearing content types and repos");
						    	Class ctdClass = ContentTypeDefinitionImpl.class;
						    	CacheController.clearCache("contentTypeDefinitionCache");
								CacheController.clearCache(ctdClass);
								CacheController.clearCaches(ctdClass.getName(), null, null);

								Class repoClass = RepositoryImpl.class;
								CacheController.clearCache("repositoryCache");
								CacheController.clearCache("masterRepository");
								CacheController.clearCache(repoClass);
								CacheController.clearCaches(repoClass.getName(), null, null);

								Class categoryClass = CategoryImpl.class;
								CacheController.clearCache("categoryCache");
								CacheController.clearCache(categoryClass);
								CacheController.clearCaches(categoryClass.getName(), null, null);
							}
								
							for(Map<String,String> igCacheCall : allIGCacheCalls)
							{
								if(igCacheCall.get("className") == null || !igCacheCall.get("className").equals("ServerNodeProperties"))
								{
									logger.info("Calling clear caches with:" + igCacheCall.get("className") + ":" + igCacheCall.get("objectId"));
									CacheController.clearCaches(igCacheCall.get("className"), igCacheCall.get("objectId"), null);
								}
							}
						}
				    }
				    catch (Exception e) 
				    {
				    	logger.error("An error occurred handling cache eviction bean in SelectiveLivePublicationThread:" + e.getMessage());
				    	logger.warn("An error occurred handling cache eviction bean in SelectiveLivePublicationThread:" + e.getMessage(), e);
					}
				    finally
				    {
						//TODO
						CacheEvictionBeanListenerService.getService().notifyListeners(cacheEvictionBean);
	
					    RequestAnalyser.getRequestAnalyser().removeOngoingPublications(cacheEvictionBean);
					    if(!processedInterupted)
					    {
						    cacheEvictionBean.setProcessed();
						    if(cacheEvictionBean.getPublicationId() > -1 || cacheEvictionBean.getClassName().equals("ServerNodeProperties"))
						    	RequestAnalyser.getRequestAnalyser().addPublication(cacheEvictionBean);
					    }				    	
				    }
				}
			} 
			catch (Exception e)
			{
			    logger.error("An error occurred in the SelectiveLivePublicationThread:" + e.getMessage(), e);
			}
			finally
			{
		        logger.warn("released block \n\n DONE---");
				RequestAnalyser.getRequestAnalyser().setBlockRequests(false);
			}
		}
	}
	

	private boolean isThereAnyRealDifferencesBetweenSiteNodeVersions(SiteNodeVersionVO siteNodeVersionVO, SiteNodeVersionVO previousSiteNodeVersionVO) 
	{
		if(siteNodeVersionVO == null || previousSiteNodeVersionVO == null)
		{
			logger.info("One seems null:" + siteNodeVersionVO + ":" + previousSiteNodeVersionVO);
			return true;
		}
		
		try
		{
			if(!siteNodeVersionVO.getContentType().equals(previousSiteNodeVersionVO.getContentType()))
			{
				logger.info("Diffed in getContentType");
				return true;
			}
			if(!siteNodeVersionVO.getDisableEditOnSight().equals(previousSiteNodeVersionVO.getDisableEditOnSight()))
			{
				logger.info("Diffed in getDisableEditOnSight");
				return true;
			}
			if(!siteNodeVersionVO.getDisableForceIdentityCheck().equals(previousSiteNodeVersionVO.getDisableForceIdentityCheck()))
			{
				logger.info("Diffed in getDisableForceIdentityCheck");
				return true;
			}
			if(!siteNodeVersionVO.getDisableLanguages().equals(previousSiteNodeVersionVO.getDisableLanguages()))
			{
				logger.info("Diffed in getDisableLanguages");
				return true;
			}
			if(!siteNodeVersionVO.getDisablePageCache().equals(previousSiteNodeVersionVO.getDisablePageCache()))
			{
				logger.info("Diffed in getDisablePageCache");
				return true;
			}
			if(!siteNodeVersionVO.getForceProtocolChange().equals(previousSiteNodeVersionVO.getForceProtocolChange()))
			{
				logger.info("Diffed in getForceProtocolChange");
				return true;
			}
			if(!siteNodeVersionVO.getIsProtected().equals(previousSiteNodeVersionVO.getIsProtected()))
			{
				logger.info("Diffed in getIsProtected");
				return true;
			}
			if(!siteNodeVersionVO.getPageCacheKey().equals(previousSiteNodeVersionVO.getPageCacheKey()))
			{
				logger.info("Diffed in getPageCacheKey");
				return true;
			}
			if((siteNodeVersionVO.getPageCacheTimeout() != null && previousSiteNodeVersionVO.getPageCacheTimeout() != null) && (
					(siteNodeVersionVO.getPageCacheTimeout() == null && previousSiteNodeVersionVO.getPageCacheTimeout() != null) ||
					(siteNodeVersionVO.getPageCacheTimeout() != null && previousSiteNodeVersionVO.getPageCacheTimeout() == null) ||
					!siteNodeVersionVO.getPageCacheTimeout().equals(previousSiteNodeVersionVO.getPageCacheTimeout())))
			{
				logger.info("Diffed in getPageCacheTimeout");
				return true;
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			//return true;
		}

		return false;
	}


	public void addCacheUpdateDirective(String className, String objectId, List<Map<String, String>> allIGCacheCalls) 
	{
		Map<String,String> cacheUpdateDirective = new HashMap<String,String>();
		cacheUpdateDirective.put("className", className);
		cacheUpdateDirective.put("objectId", objectId);
		allIGCacheCalls.add(cacheUpdateDirective);
	}
	
	private void recacheEntities(CacheEvictionBean cacheEvictionBean) throws Exception
	{
		Timer t = new Timer();
		
	    String className = cacheEvictionBean.getClassName();
	    String objectId = cacheEvictionBean.getObjectId();
	    logger.info("*********************************");
	    logger.info("recacheEntities for " + className);
	    logger.info("*********************************");
	    
		Database db = CastorDatabaseService.getDatabase();
		db.begin();
		
		try
		{
			if(Class.forName(className).getName().equals(ContentImpl.class.getName()))
			{
				getObjectWithId(ContentImpl.class, new Integer(objectId), db);
				getObjectWithId(SmallContentImpl.class, new Integer(objectId), db);
				getObjectWithId(SmallishContentImpl.class, new Integer(objectId), db);
				getObjectWithId(MediumContentImpl.class, new Integer(objectId), db);
			}
			if(Class.forName(className).getName().equals(ContentVersionImpl.class.getName()))
			{
				getObjectWithId(ContentVersionImpl.class, new Integer(objectId), db);
				getObjectWithId(SmallContentVersionImpl.class, new Integer(objectId), db);
				getObjectWithId(SmallestContentVersionImpl.class, new Integer(objectId), db);
			}
			else if(Class.forName(className).getName().equals(AvailableServiceBindingImpl.class.getName()))
			{
				getObjectWithId(AvailableServiceBindingImpl.class, new Integer(objectId), db);
				getObjectWithId(SmallAvailableServiceBindingImpl.class, new Integer(objectId), db);
			}
			else if(Class.forName(className).getName().equals(SiteNodeImpl.class.getName()))
			{
				SiteNodeImpl siteNode = (SiteNodeImpl)getObjectWithId(SiteNodeImpl.class, new Integer(objectId), db);
				getObjectWithId(SmallSiteNodeImpl.class, new Integer(objectId), db);
				
				/*
				NodeDeliveryController ndc = NodeDeliveryController.getNodeDeliveryController(new Integer(objectId), new Integer(-1), new Integer(-1));
				Repository repository = siteNode.getRepository();
		    	if(repository != null)
				{
					Collection languages = repository.getRepositoryLanguages();
					Iterator languageIterator = languages.iterator();
					while(languageIterator.hasNext())
					{
						RepositoryLanguage repositoryLanguage = (RepositoryLanguage)languageIterator.next();
						Language currentLanguage = repositoryLanguage.getLanguage();
						LanguageDeliveryController.getLanguageDeliveryController().getLanguageIfSiteNodeSupportsIt(db, currentLanguage.getId(), siteNode.getId());
					}
				}
				*/
			}
			else if(Class.forName(className).getName().equals(SiteNodeVersionImpl.class.getName()))
			{
				getObjectWithId(SiteNodeVersionImpl.class, new Integer(objectId), db);
				getObjectWithId(SmallSiteNodeVersionImpl.class, new Integer(objectId), db);
			}
			else if(Class.forName(className).getName().equals(DigitalAssetImpl.class.getName()))
			{
				getObjectWithId(SmallDigitalAssetImpl.class, new Integer(objectId), db);
				getObjectWithId(MediumDigitalAssetImpl.class, new Integer(objectId), db);
			}
			else if(Class.forName(className).getName().equals(MediumDigitalAssetImpl.class.getName()))
			{
				getObjectWithId(SmallDigitalAssetImpl.class, new Integer(objectId), db);
				getObjectWithId(DigitalAssetImpl.class, new Integer(objectId), db);
			}
			else if(Class.forName(className).getName().equals(PublicationImpl.class.getName()))
			{
				List publicationDetailVOList = PublicationController.getController().getPublicationDetailVOList(new Integer(objectId));
				Iterator publicationDetailVOListIterator = publicationDetailVOList.iterator();
				while(publicationDetailVOListIterator.hasNext())
				{
					PublicationDetailVO publicationDetailVO = (PublicationDetailVO)publicationDetailVOListIterator.next();
					logger.info("publicationDetailVO.getEntityClass():" + publicationDetailVO.getEntityClass());
					logger.info("publicationDetailVO.getEntityId():" + publicationDetailVO.getEntityId());
					if(Class.forName(publicationDetailVO.getEntityClass()).getName().equals(ContentVersion.class.getName()))
					{
						logger.info("We cache all content having references to contentVersion: " + publicationDetailVO.getEntityId());
						Integer contentId = ContentVersionController.getContentVersionController().getContentIdForContentVersion(publicationDetailVO.getEntityId());
						getObjectWithId(ContentVersionImpl.class, new Integer(publicationDetailVO.getEntityId()), db);
						getObjectWithId(SmallContentVersionImpl.class, new Integer(publicationDetailVO.getEntityId()), db);
						getObjectWithId(SmallestContentVersionImpl.class, new Integer(publicationDetailVO.getEntityId()), db);

						getObjectWithId(ContentImpl.class, new Integer(contentId), db);
						getObjectWithId(SmallContentImpl.class, new Integer(contentId), db);
						getObjectWithId(SmallishContentImpl.class, new Integer(contentId), db);
						getObjectWithId(MediumContentImpl.class, new Integer(contentId), db);
					}
					else if(Class.forName(publicationDetailVO.getEntityClass()).getName().equals(SiteNodeImpl.class.getName()))
					{
						SiteNodeImpl siteNode = (SiteNodeImpl)getObjectWithId(SiteNodeImpl.class, new Integer(objectId), db);
						getObjectWithId(SmallSiteNodeImpl.class, new Integer(objectId), db);
						
						/*
						NodeDeliveryController ndc = NodeDeliveryController.getNodeDeliveryController(new Integer(objectId), new Integer(-1), new Integer(-1));
						Repository repository = siteNode.getRepository();
				    	if(repository != null)
						{
							Collection languages = repository.getRepositoryLanguages();
							Iterator languageIterator = languages.iterator();
							while(languageIterator.hasNext())
							{
								RepositoryLanguage repositoryLanguage = (RepositoryLanguage)languageIterator.next();
								Language currentLanguage = repositoryLanguage.getLanguage();
								LanguageDeliveryController.getLanguageDeliveryController().getLanguageIfSiteNodeSupportsIt(db, currentLanguage.getId(), siteNode.getId());
							}
						}
						*/
					}
					else if(Class.forName(publicationDetailVO.getEntityClass()).getName().equals(SiteNodeVersion.class.getName()))
					{
						Integer siteNodeId = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(publicationDetailVO.getEntityId()).getSiteNodeId();
					    
						getObjectWithId(SiteNodeVersionImpl.class, new Integer(publicationDetailVO.getEntityId()), db);
						getObjectWithId(SmallSiteNodeVersionImpl.class, new Integer(publicationDetailVO.getEntityId()), db);

						SiteNodeImpl siteNode = (SiteNodeImpl)getObjectWithId(SiteNodeImpl.class, new Integer(siteNodeId), db);
						getObjectWithId(SmallSiteNodeImpl.class, new Integer(siteNodeId), db);
						
						/*
						NodeDeliveryController ndc = NodeDeliveryController.getNodeDeliveryController(new Integer(objectId), new Integer(-1), new Integer(-1));
						Repository repository = siteNode.getRepository();
				    	if(repository != null)
						{
							Collection languages = repository.getRepositoryLanguages();
							Iterator languageIterator = languages.iterator();
							while(languageIterator.hasNext())
							{
								RepositoryLanguage repositoryLanguage = (RepositoryLanguage)languageIterator.next();
								Language currentLanguage = repositoryLanguage.getLanguage();
								LanguageDeliveryController.getLanguageDeliveryController().getLanguageIfSiteNodeSupportsIt(db, currentLanguage.getId(), siteNode.getId());
							}
						}
						*/
						
						SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId, db);
						if(siteNodeVO.getMetaInfoContentId() != null)
						{
							/*
							getObjectWithId(ContentImpl.class, new Integer(siteNodeVO.getMetaInfoContentId()), db);
							getObjectWithId(SmallContentImpl.class, new Integer(siteNodeVO.getMetaInfoContentId()), db);
							getObjectWithId(SmallishContentImpl.class, new Integer(siteNodeVO.getMetaInfoContentId()), db);
							getObjectWithId(MediumContentImpl.class, new Integer(siteNodeVO.getMetaInfoContentId()), db);
							
							Content content = ContentController.getContentController().getReadOnlyContentWithId(siteNodeVO.getMetaInfoContentId(), db);
							Iterator contentVersionIterator = content.getContentVersions().iterator();
							logger.info("Versions:" + content.getContentVersions().size());
							while(contentVersionIterator.hasNext())
							{
								ContentVersion contentVersion = (ContentVersion)contentVersionIterator.next();
								getObjectWithId(ContentVersionImpl.class, new Integer(contentVersion.getId()), db);
								getObjectWithId(SmallContentVersionImpl.class, new Integer(contentVersion.getId()), db);
								getObjectWithId(SmallestContentVersionImpl.class, new Integer(contentVersion.getId()), db);
							}
							*/
						}
					}
					
				}
			}
			
			db.rollback();
		}
		catch (Exception e) 
		{
			logger.error("Problem recaching:" + e.getMessage(), e);
		}
		finally
		{
			try
			{
				db.close();
			}
			catch (Exception e) 
			{
				logger.error("Problem closing db");
			}
		}
		if(logger.isInfoEnabled())
			t.printElapsedTime("Recaching entities in SelectiveLivePublicationThread took:");
		
	}
	
	protected Object getObjectWithId(Class arg, Integer id, Database db) throws SystemException, Bug
	{
		Object object = null;
		try
		{
			object = db.load(arg, id, Database.ReadOnly);
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
		}
    
		if(object == null)
		{
			throw new Bug("The object with id [" + id + "] was not found. This should never happen.");
		}
		return object;
	}
}
