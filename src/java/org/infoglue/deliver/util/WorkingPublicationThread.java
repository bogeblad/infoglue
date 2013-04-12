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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.SmallestContentVersionVO;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallishContentImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl;
import org.infoglue.cms.entities.management.AccessRightVO;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl;
import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
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
import org.infoglue.cms.entities.publishing.impl.simple.PublicationDetailImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl;
import org.infoglue.cms.services.CacheEvictionBeanListenerService;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
import org.infoglue.deliver.applications.filters.URIMapperCache;
import org.infoglue.deliver.cache.PageCacheHelper;
import org.infoglue.deliver.controllers.kernel.impl.simple.DigitalAssetDeliveryController;

/**
 * @author mattias
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorkingPublicationThread extends Thread
{
    public final static Logger logger = Logger.getLogger(WorkingPublicationThread.class.getName());
	private static VisualFormatter formatter = new VisualFormatter();

    private List cacheEvictionBeans = new ArrayList();
	
	public WorkingPublicationThread()
	{
	}
	
	public List getCacheEvictionBeans()
	{
		return cacheEvictionBeans;
	}

	public void run() 
	{
		work();
	}
	

	public void work()
	{
		//synchronized (cacheEvictionBeans) 
		//{

		logger.info("cacheEvictionBeans.size:" + cacheEvictionBeans.size() + ":" + RequestAnalyser.getRequestAnalyser().getBlockRequests());
        if(cacheEvictionBeans.size() > 0)
		{
        	//logger.warn("setting block");
            //RequestAnalyser.setBlockRequests(true);
    		
            //Database db = null;
			
			try
			{
			    //db = CastorDatabaseService.getDatabase();
	
				//int publicationDelay = 50;
	
			    //logger.info("\n\n\nSleeping " + publicationDelay + "ms.\n\n\n");
			    //sleep(publicationDelay);
			
				Timer t = new Timer();
				Timer tTotal = new Timer();

		        boolean accessRightsFlushed = false;
		        List<String> processedEntities = new ArrayList<String>();
				List<String> accessRightsToClear = new ArrayList<String>();

				Iterator i = cacheEvictionBeans.iterator();
				while(i.hasNext())
				{
				    CacheEvictionBean cacheEvictionBean = (CacheEvictionBean)i.next();
				    
				    RequestAnalyser.getRequestAnalyser().addOngoingPublications(cacheEvictionBean);

				    String className = cacheEvictionBean.getClassName();
				    String objectId = cacheEvictionBean.getObjectId();
				    String objectName = cacheEvictionBean.getObjectName();
					String typeId = cacheEvictionBean.getTypeId();
					Map<String,String> extraInformation = cacheEvictionBean.getExtraInformation();
					String changedAttributeNames = extraInformation.get("changedAttributeNames");
				    logger.info("className:" + className);
					logger.info("objectId:" + objectId);
					logger.info("objectName:" + objectName);
					logger.info("typeId:" + typeId);
					logger.info("changedAttributeNames:" + changedAttributeNames);

				    boolean skipOriginalEntity = false;

					List<Map<String,String>> allIGCacheCalls = new ArrayList<Map<String,String>>();

				    logger.info("className:" + className + " objectId:" + objectId + " objectName: " + objectName + " typeId: " + typeId + ":" + extraInformation);
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
						    	String acKey = "" + icpVO.getId() + "_" + acVO.getParameters();
								accessRightsToClear.add(acKey);

						    	//logger.info("icpVO:" + icpVO.getName());
						    	if(icpVO.getName().indexOf("Content.") > -1)
						    	{
						    		//logger.info("Was a content access... let's clear caches for that content.");
						    		String idAsString = acVO.getParameters();
						    		if(idAsString != null && !idAsString.equals(""))
						    			addCacheUpdateDirective("org.infoglue.cms.entities.content.impl.simple.ContentImpl", idAsString, allIGCacheCalls);
						    	}
						    	else if(icpVO.getName().indexOf("ContentVersion.") > -1)
						    	{
						    		//logger.info("Was a contentversion access... let's clear caches for that content.");
						    		String idAsString = acVO.getParameters();
						    		if(idAsString != null && !idAsString.equals(""))
						    			addCacheUpdateDirective("org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl", idAsString, allIGCacheCalls);
						    	}
							    else if(icpVO.getName().indexOf("SiteNode.") > -1)
							    {
							    	//logger.info("Was a sitenode access... let's clear caches for that content.");
						    		String idAsString = acVO.getParameters();
						    		if(idAsString != null && !idAsString.equals(""))
						    			addCacheUpdateDirective("org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl", idAsString, allIGCacheCalls);
							    }
								else if(icpVO.getName().indexOf("SiteNodeVersion.") > -1)
								{
									//logger.info("Was a sitenode version access... let's clear caches for that content.");
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
						    	logger.info("Feeling done with " + "" + icpVO.getName() + "_" + acVO.getParameters());
						    	processedEntities.add("" + icpVO.getName() + "_" + acVO.getParameters());
					    	}
					    	else
					    		logger.info("Allready processed " + icpVO.getCategory() + "_" + acVO.getParameters());
					    }
				    	catch(Exception e2)
				    	{
				    		logger.warn("Error handling access right update: " + e2.getMessage());
				    	}
				    	
				    	if(!accessRightsFlushed)
				    	{
					     	CacheController.clearCache("personalAuthorizationCache");
				    		accessRightsFlushed = true;
				    	}
				    	//t.printElapsedTime("Access rights");
				    	//continue;
				    }
				    //t.printElapsedTime("First part in working thread done...");
					//logger.info("changedAttributeNames in working thread:" + changedAttributeNames);
					
					//logger.info("className:" + className);
					//logger.info("objectId:" + objectId);
					//logger.info("objectName:" + objectName);
					//logger.info("typeId:" + typeId);

					try
					{
				        boolean isDependsClass = false;
					    if(className != null && className.equalsIgnoreCase(PublicationDetailImpl.class.getName()))
					        isDependsClass = true;
				
					    if(!skipOriginalEntity)
					    	addCacheUpdateDirective(className, objectId, allIGCacheCalls);
					    
					    //t.printElapsedTime("1.1");

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
						        //t.printElapsedTime("1.2");
							}
						    else if(!isDependsClass)
						    {
						    	try
						    	{
							        Object[] ids = {new Integer(objectId)};
								    CacheController.clearCache(type, ids);
								    //t.printElapsedTime("1.3");
						    	}
						    	catch (Exception e) 
						    	{
						    		logger.warn("Problem clearing cache for type:" + type + " AND ID:" + objectId);
								}
						    }
			
						    //t.printElapsedTime("2");

							//If it's an contentVersion we should delete all images it might have generated from attributes.
							if(Class.forName(className).getName().equals(ContentImpl.class.getName()))
							{
							    logger.info("We clear all small contents as well " + objectId);
								Class typesExtra = SmallContentImpl.class;
								Object[] idsExtra = {new Integer(objectId)};
								CacheController.clearCache(typesExtra, idsExtra);
	
							    logger.info("We clear all smallish contents as well " + objectId);
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
								//t.printElapsedTime("ContentVersionImpl...");
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
							else if(Class.forName(className).getName().equals(RepositoryImpl.class.getName()))
							{
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
	
								String disableAssetDeletionInWorkThread = CmsPropertyHandler.getDisableAssetDeletionInWorkThread();
								if(disableAssetDeletionInWorkThread != null && !disableAssetDeletionInWorkThread.equals("true"))
								{
									logger.info("We should delete all images with digitalAssetId " + objectId);
									DigitalAssetDeliveryController.getDigitalAssetDeliveryController().deleteDigitalAssets(new Integer(objectId));
								}
								
								Set<String> handledVersions = new HashSet<String>();
								List<SmallestContentVersionVO> contentVersionVOList = DigitalAssetController.getContentVersionVOListConnectedToAssetWithId(new Integer(objectId));	
					    		Iterator<SmallestContentVersionVO> contentVersionVOListIterator = contentVersionVOList.iterator();
					    		while(contentVersionVOListIterator.hasNext())
					    		{
					    			SmallestContentVersionVO contentVersionVO = contentVersionVOListIterator.next();
					    			logger.info("Invoking clearCaches for ContentVersionImpl with id:" + contentVersionVO.getId());
					    			String key = contentVersionVO.getContentId() + "_" + contentVersionVO.getLanguageId();
					    			//System.out.println("Invoking clearCaches for ContentVersionImpl with id:" + key + " " + contentVersionVO.getId());
					    			if(!handledVersions.contains(key))
					    			{
							    		CacheController.clearCaches(ContentVersionImpl.class.getName(), contentVersionVO.getId().toString(), null);		    			
							    		CacheController.clearCaches(SmallContentVersionImpl.class.getName(), contentVersionVO.getId().toString(), null);					    			
							    		CacheController.clearCaches(SmallestContentVersionImpl.class.getName(), contentVersionVO.getId().toString(), null);
							    		handledVersions.add(key);
					    			}
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
	
								String disableAssetDeletionInWorkThread = CmsPropertyHandler.getDisableAssetDeletionInWorkThread();
								if(disableAssetDeletionInWorkThread != null && !disableAssetDeletionInWorkThread.equals("true"))
								{
									logger.info("We should delete all images with digitalAssetId " + objectId);
									DigitalAssetDeliveryController.getDigitalAssetDeliveryController().deleteDigitalAssets(new Integer(objectId));
								}
								
								Set<String> handledVersions = new HashSet<String>();
								List<SmallestContentVersionVO> contentVersionVOList = DigitalAssetController.getContentVersionVOListConnectedToAssetWithId(new Integer(objectId));	
					    		Iterator<SmallestContentVersionVO> contentVersionVOListIterator = contentVersionVOList.iterator();
					    		while(contentVersionVOListIterator.hasNext())
					    		{
					    			SmallestContentVersionVO contentVersionVO = contentVersionVOListIterator.next();
					    			String key = contentVersionVO.getContentId() + "_" + contentVersionVO.getLanguageId();
					    			//System.out.println("Invoking clearCaches for ContentVersionImpl with id:" + key + " " + contentVersionVO.getId());
					    			if(!handledVersions.contains(key))
					    			{
							    		CacheController.clearCaches(ContentVersionImpl.class.getName(), contentVersionVO.getId().toString(), null);		    			
							    		CacheController.clearCaches(SmallContentVersionImpl.class.getName(), contentVersionVO.getId().toString(), null);					    			
							    		CacheController.clearCaches(SmallestContentVersionImpl.class.getName(), contentVersionVO.getId().toString(), null);	
							    		handledVersions.add(key);
					    			}
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
						}	
					    
					    // t.printElapsedTime("3");
					    long elapsedTime = t.getElapsedTime();
					    if(elapsedTime > 50)
					    	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Clearing all castor caches in working publication thread took", elapsedTime);
						
					    
					    for(Map<String,String> igCacheCall : allIGCacheCalls)
						{
							logger.info("Calling clear caches with:" + igCacheCall.get("className") + ":" + igCacheCall.get("objectId") + ":" + extraInformation);
							CacheController.clearCaches(igCacheCall.get("className"), igCacheCall.get("objectId"), extraInformation, null);
							handledCacheCalls.put("" + igCacheCall.get("className") + "_" + igCacheCall.get("objectId") + "_" + extraInformation, new Boolean(true));
							
						    elapsedTime = t.getElapsedTime();
						    if(elapsedTime > 10)
						    	logger.warn("Clearing all caches for " + igCacheCall.get("className") + ":" + igCacheCall.get("objectId"));
						}
					    
					    String key = "" + className + "_" + objectId + "_" + extraInformation;
					    if(!skipOriginalEntity && handledCacheCalls.get(key) == null)
					    {
					    	logger.info("" + className + ":" + objectId + ":" + extraInformation);
						    CacheController.clearCaches(className, objectId, extraInformation, null);
							CacheController.setForcedCacheEvictionMode(true);
						   
							if(elapsedTime > 100)
						    	logger.warn("Clearing all caches for " + className + ":" + objectId + ":" + changedAttributeNames);
					    }
					    else
					    	logger.info("Skipping cache clear for the same entity..");
					    
					    //System.out.println("Adding:" + cacheEvictionBean.getObjectName() + ":" + cacheEvictionBean.getObjectId());
						CacheEvictionBeanListenerService.getService().notifyListeners(cacheEvictionBean);
					}
					catch (Exception e) 
					{
						if(e.getMessage().indexOf("was not found") > -1 || (e.getCause() != null && e.getCause().getMessage().indexOf("was not found") > -1))
							logger.warn("A delete operation probably gave us trouble clearing the correct caches");
						else
							logger.warn("Error handling cache update message:" + className + ":" + objectId, e);
					}
					
				    RequestAnalyser.getRequestAnalyser().removeOngoingPublications(cacheEvictionBean);
				    cacheEvictionBean.setProcessed();
				    //if(cacheEvictionBean.getPublicationId() > -1)
				    	RequestAnalyser.getRequestAnalyser().addPublication(cacheEvictionBean);
				}
				
				//TEST
				if(accessRightsToClear != null && accessRightsToClear.size() > 0)
				{
			        CacheController.clearCache(AccessRightImpl.class);
			        CacheController.clearCache(AccessRightRoleImpl.class);
			        CacheController.clearCache(AccessRightGroupImpl.class);
			        CacheController.clearCache(AccessRightUserImpl.class);
			        
			        for(String acKey : accessRightsToClear)
			        {
	    				logger.info("Clearing access rights for:" + acKey);
	    				CacheController.clearUserAccessCache(acKey);	
					}
				
		    		CacheController.clearCache("personalAuthorizationCache");
		    		CacheController.clearCache("pageCache");
		    		CacheController.clearCache("pageCacheExtra");
				}
				
				//tTotal.printElapsedTime("Working publication thread took");
		        RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Working publication thread took", tTotal.getElapsedTime());
			} 
			catch (Exception e)
			{
			    logger.error("An error occurred in the WorkingPublicationThread:" + e.getMessage(), e);
			}
			/*
			finally
			{
				try
				{
					if(db != null)
						db.close();
				}
				catch(Exception e)
				{
				    logger.error("An error occurred in the WorkingPublicationThread:" + e.getMessage(), e);
				}
			}
			*/
		}
        
        RequestAnalyser.getRequestAnalyser().setBlockRequests(false);
		logger.info("released block");
	}
	
	private Map<String,Boolean> handledCacheCalls = new HashMap<String,Boolean>();
	
	public void addCacheUpdateDirective(String className, String objectId, List<Map<String, String>> allIGCacheCalls) 
	{
		Map<String,String> cacheUpdateDirective = new HashMap<String,String>();
		cacheUpdateDirective.put("className", className);
		cacheUpdateDirective.put("objectId", objectId);
		allIGCacheCalls.add(cacheUpdateDirective);
	}


}
