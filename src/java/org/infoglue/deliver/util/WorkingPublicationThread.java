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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptorController;
import org.infoglue.cms.controllers.kernel.impl.simple.LuceneController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
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
import org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl;
import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.GroupImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.management.impl.simple.RoleImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallAvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserImpl;
import org.infoglue.cms.entities.publishing.PublicationDetailVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationDetailImpl;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl;
import org.infoglue.cms.services.CacheEvictionBeanListenerService;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
import org.infoglue.deliver.applications.filters.URIMapperCache;
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
					String changedAttributeNames = cacheEvictionBean.getChangedAttributeNames();
				    logger.info("className:" + className);
					logger.info("objectId:" + objectId);
					logger.info("objectName:" + objectName);
					logger.info("typeId:" + typeId);
					logger.info("changedAttributeNames:" + changedAttributeNames);

				    boolean skipOriginalEntity = false;

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
				    		logger.error("Error handling access right update: " + e2.getMessage());
				    	}
				    	
				    	if(!accessRightsFlushed)
				    	{
					     	CacheController.clearCache("personalAuthorizationCache");
				    		accessRightsFlushed = true;
				    	}

				    	//continue;
				    }
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

					    logger.info("Updating className with id:" + className + ":" + objectId);
					    if(className != null && !typeId.equalsIgnoreCase("" + NotificationMessage.SYSTEM) && !skipOriginalEntity)
						{
						    Class type = Class.forName(className);
			
						    if(!isDependsClass && 
						    		className.equalsIgnoreCase(SystemUserImpl.class.getName()) || 
						    		className.equalsIgnoreCase(RoleImpl.class.getName()) || 
						    		className.equalsIgnoreCase(GroupImpl.class.getName()))
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

								//Removing all scriptExtensionBundles to make sure
								CacheController.removeScriptExtensionBundles();							    	
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
								Class repoClass = RepositoryImpl.class;
								CacheController.clearCache(repoClass);
								CacheController.clearCaches(repoClass.getName(), null, null);

								CacheController.clearCache("repositoryCache");
								CacheController.clearCache("masterRepository");
						        CacheController.clearFileCaches("pageCache");
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
								
								List<SmallestContentVersionVO> contentVersionVOList = DigitalAssetController.getContentVersionVOListConnectedToAssetWithId(new Integer(objectId));	
					    		Iterator<SmallestContentVersionVO> contentVersionVOListIterator = contentVersionVOList.iterator();
					    		while(contentVersionVOListIterator.hasNext())
					    		{
					    			SmallestContentVersionVO contentVersionVO = contentVersionVOListIterator.next();
					    			logger.info("Invoking clearCaches for ContentVersionImpl with id:" + contentVersionVO.getId());
						    		CacheController.clearCaches(ContentVersionImpl.class.getName(), contentVersionVO.getId().toString(), null);					    			
					    		}

								//Removing all scriptExtensionBundles to make sure
								CacheController.removeScriptExtensionBundles();							    	
							}
							
						    logger.info("4");
						}	
					    t.printElapsedTime("Clearing all castor caches for " + className + " took");

					    for(Map<String,String> igCacheCall : allIGCacheCalls)
						{
							logger.info("Calling clear caches with:" + igCacheCall.get("className") + ":" + igCacheCall.get("objectId"));
							CacheController.clearCaches(igCacheCall.get("className"), igCacheCall.get("objectId"), null);
						}
					    
					    if(!skipOriginalEntity)
					    {
						    CacheController.clearCaches(className, objectId, changedAttributeNames, null);
							CacheController.setForcedCacheEvictionMode(true);
						    t.printElapsedTime("Clearing all caches took");
	
						    if(!className.equals(SystemUserImpl.class.getName()) &&
						       !className.equals(RoleImpl.class.getName()) &&
						       !className.equals(GroupImpl.class.getName()))
						 	{
					    		logger.info("Going to index:" + className + ":" + objectId + ":" + typeId);
								//Fixa sŒ detta funkar och att delete av version ocksŒ slŒr
								NotificationMessage notificationMessage = new NotificationMessage("LuceneController", className, "SYSTEM", Integer.parseInt(typeId), Integer.parseInt(objectId), "" + objectName);
								new Thread(new SearchIndexHelper(notificationMessage)).start();
								LuceneController.getController().notify(notificationMessage);
								logger.info("------------------------------------------->Done indexing in working thread");					 		
						 	}
					    }
						CacheEvictionBeanListenerService.getService().notifyListeners(cacheEvictionBean);
					}
					catch (Exception e) 
					{
						logger.error("Error handling cache update message:" + className + ":" + objectId, e);
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
	
	public void addCacheUpdateDirective(String className, String objectId, List<Map<String, String>> allIGCacheCalls) 
	{
		Map<String,String> cacheUpdateDirective = new HashMap<String,String>();
		cacheUpdateDirective.put("className", className);
		cacheUpdateDirective.put("objectId", objectId);
		allIGCacheCalls.add(cacheUpdateDirective);
	}


}
