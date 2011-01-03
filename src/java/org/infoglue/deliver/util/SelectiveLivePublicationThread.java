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
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
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
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.RepositoryLanguage;
import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.GroupImpl;
import org.infoglue.cms.entities.management.impl.simple.RoleImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallAvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallSystemUserImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserImpl;
import org.infoglue.cms.entities.publishing.PublicationDetailVO;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationDetailImpl;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
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
		logger.info("Run in SelectiveLivePublicationThread....");
		
		int publicationDelay = 5000;
	    String publicationThreadDelay = CmsPropertyHandler.getPublicationThreadDelay();
	    if(publicationThreadDelay != null && !publicationThreadDelay.equalsIgnoreCase("") && publicationThreadDelay.indexOf("publicationThreadDelay") == -1)
	        publicationDelay = Integer.parseInt(publicationThreadDelay);
	    
	    Random r = new Random();
	    int randint = (Math.abs(r.nextInt()) % 11) / 8 * 1000;
	    publicationDelay = publicationDelay + randint;
	    //System.out.println("publicationDelay:" + publicationDelay);
	    
	    logger.info("\n\n\nSleeping " + publicationDelay + "ms.\n\n\n");
		try 
		{
			sleep(publicationDelay);
		} 
		catch (InterruptedException e1) 
		{
			e1.printStackTrace();
		}

    	logger.info("before cacheEvictionBeans:" + cacheEvictionBeans.size());
	    synchronized(notifications)
        {
	    	cacheEvictionBeans.addAll(notifications);
	    	notifications.clear();
	    	this.notifications = null;
        }
	    
	    Iterator cacheEvictionBeansIterator = cacheEvictionBeans.iterator();
	    boolean processedServerNodeProperties = false;
	    while(cacheEvictionBeansIterator.hasNext())
	    {
	    	CacheEvictionBean cacheEvictionBean = (CacheEvictionBean)cacheEvictionBeansIterator.next();
		    String className = cacheEvictionBean.getClassName();
		    if(className.equalsIgnoreCase("ServerNodeProperties"))
		    {
		    	if(processedServerNodeProperties)
		    	{
		    		cacheEvictionBeansIterator.remove();
		    		//System.out.println("Removed one ServerNodeProperties update as it will be processed anyway in this eviction cycle");
		    	}
		    	else
		    	{
		    		processedServerNodeProperties = true;
		    	}
		    }
	    }

		logger.info("cacheEvictionBeans.size:" + cacheEvictionBeans.size() + ":" + RequestAnalyser.getRequestAnalyser().getBlockRequests());
		if(cacheEvictionBeans.size() > 0)
		{
			try
			{		
				logger.info("setting block");
		        RequestAnalyser.getRequestAnalyser().setBlockRequests(true);
				
				Iterator i = cacheEvictionBeans.iterator();
				while(i.hasNext())
				{
				    CacheEvictionBean cacheEvictionBean = (CacheEvictionBean)i.next();
				    String className = cacheEvictionBean.getClassName();
				    String objectId = cacheEvictionBean.getObjectId();
				    String objectName = cacheEvictionBean.getObjectName();
					String typeId = cacheEvictionBean.getTypeId();
					
				    logger.info("className:" + className + " objectId:" + objectId + " objectName: " + objectName + " typeId: " + typeId);
	
			        boolean isDependsClass = false;
				    if(className != null && className.equalsIgnoreCase(PublicationDetailImpl.class.getName()))
				        isDependsClass = true;
			
				    if(!typeId.equalsIgnoreCase("" + NotificationMessage.SYSTEM))
				    	CacheController.clearCaches(className, objectId, null);
		
				    logger.info("Updating className with id:" + className + ":" + objectId);
					if(className != null && !typeId.equalsIgnoreCase("" + NotificationMessage.SYSTEM))
					{
					    Class type = Class.forName(className);
		
					    if(!isDependsClass && 
					    		className.equalsIgnoreCase(SystemUserImpl.class.getName()) || 
					    		className.equalsIgnoreCase(RoleImpl.class.getName()) || 
					    		className.equalsIgnoreCase(GroupImpl.class.getName()) ||
					    		className.equalsIgnoreCase(SmallSystemUserImpl.class.getName()) || 
					    		className.equalsIgnoreCase(SmallRoleImpl.class.getName()) || 
					    		className.equalsIgnoreCase(SmallGroupImpl.class.getName()))
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
							List publicationDetailVOList = PublicationController.getController().getPublicationDetailVOList(new Integer(objectId));
							Iterator publicationDetailVOListIterator = publicationDetailVOList.iterator();
							while(publicationDetailVOListIterator.hasNext())
							{
								PublicationDetailVO publicationDetailVO = (PublicationDetailVO)publicationDetailVOListIterator.next();
								logger.info("publicationDetailVO.getEntityClass():" + publicationDetailVO.getEntityClass());
								logger.info("publicationDetailVO.getEntityId():" + publicationDetailVO.getEntityId());
								if(Class.forName(publicationDetailVO.getEntityClass()).getName().equals(ContentVersion.class.getName()))
								{
									logger.info("We clear all caches having references to contentVersion: " + publicationDetailVO.getEntityId());
									Integer contentId = ContentVersionController.getContentVersionController().getContentIdForContentVersion(publicationDetailVO.getEntityId());
								    CacheController.clearCaches(publicationDetailVO.getEntityClass(), publicationDetailVO.getEntityId().toString(), null);

									logger.info("We clear all small contents as well " + contentId);
									Class typesExtra = SmallContentImpl.class;
									Object[] idsExtra = {contentId};
									CacheController.clearCache(typesExtra, idsExtra);
					
								    logger.info("We clear all small contents as well " + objectId);
									Class typesExtraSmallish = SmallishContentImpl.class;
									Object[] idsExtraSmallish = {new Integer(objectId)};
									CacheController.clearCache(typesExtraSmallish, idsExtraSmallish);

									logger.info("We clear all medium contents as well " + contentId);
									Class typesExtraMedium = MediumContentImpl.class;
									Object[] idsExtraMedium = {contentId};
									CacheController.clearCache(typesExtraMedium, idsExtraMedium);
								}
								else if(Class.forName(publicationDetailVO.getEntityClass()).getName().equals(SiteNodeVersion.class.getName()))
								{
									Integer siteNodeId = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(publicationDetailVO.getEntityId()).getSiteNodeId();
								    CacheController.clearCaches(publicationDetailVO.getEntityClass(), publicationDetailVO.getEntityId().toString(), null);

								    logger.info("We clear all small siteNodes as well " + siteNodeId);
								    Class typesExtra = SmallSiteNodeImpl.class;
									Object[] idsExtra = {siteNodeId};
									CacheController.clearCache(typesExtra, idsExtra);
									
								    Class typesExtra2 = SmallSiteNodeVersionImpl.class;
									Object[] idsExtra2 = {new Integer(objectId)};
									CacheController.clearCache(typesExtra2, idsExtra2);

								    logger.info("We also clear the meta info content..");
									SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);

									logger.info("We clear all contents as well " + siteNodeVO.getMetaInfoContentId());
									Class metaInfoContentExtra = ContentImpl.class;
									Object[] idsMetaInfoContentExtra = {siteNodeVO.getMetaInfoContentId()};
									CacheController.clearCache(metaInfoContentExtra, idsMetaInfoContentExtra);
									
									logger.info("We clear all small contents as well " + siteNodeVO.getMetaInfoContentId());
									Class metaInfoContentExtraSmall = SmallContentImpl.class;
									CacheController.clearCache(metaInfoContentExtraSmall, idsMetaInfoContentExtra);
									
									logger.info("We clear all smallish contents as well " + siteNodeVO.getMetaInfoContentId());
									Class metaInfoContentExtraSmallish = SmallishContentImpl.class;
									CacheController.clearCache(metaInfoContentExtraSmallish, idsMetaInfoContentExtra);

									logger.info("We clear all medium contents as well " + siteNodeVO.getMetaInfoContentId());
									Class metaInfoContentExtraMedium = MediumContentImpl.class;
									CacheController.clearCache(metaInfoContentExtraMedium, idsMetaInfoContentExtra);
									
									CacheController.clearCaches(ContentImpl.class.getName(), siteNodeVO.getMetaInfoContentId().toString(), null);

									Database db = CastorDatabaseService.getDatabase();
									db.begin();
									
									Content content = ContentController.getContentController().getReadOnlyContentWithId(siteNodeVO.getMetaInfoContentId(), db);
									List contentVersionIds = new ArrayList();
									Iterator contentVersionIterator = content.getContentVersions().iterator();
									logger.info("Versions:" + content.getContentVersions().size());
									while(contentVersionIterator.hasNext())
									{
										ContentVersion contentVersion = (ContentVersion)contentVersionIterator.next();
										contentVersionIds.add(contentVersion.getId());
										logger.info("We clear the meta info contentVersion " + contentVersion.getId());
									}

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
										CacheController.clearCaches(ContentVersionImpl.class.getName(), contentVersionId.toString(), null);
									}
									
									logger.info("After:" + content.getContentVersions().size());

								}
								
							}
						}
						
						if(CmsPropertyHandler.getServerNodeProperty("recacheEntities", true, "false").equals("true"))
							recacheEntities(cacheEvictionBean);
					}	
					else
					{
						/*
						System.out.println("Was notification message in selective live publication...");
						System.out.println("className:" + className);
						System.out.println("objectId:" + objectId);
						System.out.println("objectName:" + objectName);
						System.out.println("typeId:" + typeId);
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
							//CacheController.clearCaches(null, null, new String[] {"ServerNodeProperties", "serverNodePropertiesCache", "pageCache", "pageCacheExtra", "componentCache", "NavigationCache", "pagePathCache", "userCache", "pageCacheParentSiteNodeCache", "pageCacheLatestSiteNodeVersions", "pageCacheSiteNodeTypeDefinition", "JNDIAuthorizationCache", "WebServiceAuthorizationCache", "importTagResultCache"});
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
					        CacheController.clearFileCaches("pageCache");
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
						else
						{
							Class type = Class.forName(className);
					        Object[] ids = {objectId};
					        CacheController.clearCache(type, ids);
					        CacheController.clearCache(type);
					    	CacheController.clearCaches(className, objectId, null);
						}
					}

					RequestAnalyser.getRequestAnalyser().addPublication("" + formatter.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " - " + cacheEvictionBean.getClassName() + " - " + cacheEvictionBean.getObjectId());
				}
			} 
			catch (Exception e)
			{
			    logger.error("An error occurred in the SelectiveLivePublicationThread:" + e.getMessage(), e);
			}
			finally
			{
		        logger.info("released block \n\n DONE---");
				RequestAnalyser.getRequestAnalyser().setBlockRequests(false);
			}
		}
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
