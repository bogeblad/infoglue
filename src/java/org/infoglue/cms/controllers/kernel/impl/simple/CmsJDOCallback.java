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

package org.infoglue.cms.controllers.kernel.impl.simple;
 
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.CacheManager;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.persist.spi.CallbackInterceptor;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVersion;
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
import org.infoglue.cms.entities.kernel.IBaseEntity;
import org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl;
import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.CategoryImpl;
import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.GroupImpl;
import org.infoglue.cms.entities.management.impl.simple.GroupPropertiesImpl;
import org.infoglue.cms.entities.management.impl.simple.InterceptionPointImpl;
import org.infoglue.cms.entities.management.impl.simple.InterceptorImpl;
import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
import org.infoglue.cms.entities.management.impl.simple.RegistryImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryLanguageImpl;
import org.infoglue.cms.entities.management.impl.simple.RoleImpl;
import org.infoglue.cms.entities.management.impl.simple.RolePropertiesImpl;
import org.infoglue.cms.entities.management.impl.simple.SubscriptionFilterImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.TransactionHistoryImpl;
import org.infoglue.cms.entities.management.impl.simple.UserPropertiesImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl;
import org.infoglue.cms.entities.workflow.impl.simple.WorkflowDefinitionImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ChangeNotificationController;
import org.infoglue.cms.util.CmsSessionContextListener;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.cms.util.RemoteCacheUpdater;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.SearchIndexHelper;
import org.infoglue.deliver.util.Timer;


/**
 * CMSJDOCallback.java
 * Created on 2002-okt-09 
 * @author Stefan Sik, ss@frovi.com 
 * ss
 * 
 */
public class CmsJDOCallback implements CallbackInterceptor
{
    private final static Logger logger = Logger.getLogger(CmsJDOCallback.class.getName());
    
    public void using(Object object, Database db)
    {
    	//logger.error("Using " + object);
        // ( (Persistent) object ).jdoPersistent( db );
    }


    public Class loaded(Object object, short accessMode) throws Exception
    {
    	//System.out.println("Loaded 1" + object.getClass().getName());
    	//System.out.print(".");
    	//if(accessMode == AccessMode.Shared.getId())
    	//	logger.error("Loaded 2" + object.getClass().getName() + " in write mode");
		// return ( (Persistent) object ).jdoLoad(accessMode);
        return null;
    }

	public Class loaded(Object arg0, AccessMode arg1) throws Exception 
	{
    	//System.out.println("Loaded 2" + arg0.getClass().getName());
		//System.out.println("Loaded 2" + arg0.getClass().getName());
		
		//if(arg1.getId() == AccessMode.Shared.getId() && arg0.getClass().getName().indexOf(".CategoryImpl") > -1)
		//	logger.error("Loaded 2" + arg0.getClass().getName() + " in write mode");
		
		return null;
	}

    public void storing(Object object, boolean modified) throws Exception
    {
        // ( (Persistent) object ).jdoStore( modified );

   		//logger.info("Should we store -------------->" + object + ":" + modified);
    	if (AccessRightGroupImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
			AccessRightRoleImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
			AccessRightUserImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
    		TransactionHistoryImpl.class.getName().indexOf(object.getClass().getName()) == -1 && 
    		RegistryImpl.class.getName().indexOf(object.getClass().getName()) == -1 && 
    		SubscriptionFilterImpl.class.getName().indexOf(object.getClass().getName()) == -1 && modified)
	    {
	   		logger.info("Actually stored it:" + object + ":" + modified);
	    	
			String userName = "SYSTEM";
			try
			{
				InfoGluePrincipal principal = InfoGlueAbstractAction.getSessionInfoGluePrincipal();
				if(principal != null && principal.getName() != null)
					userName = principal.getName();				
			} 
			catch (NoClassDefFoundError e){}

    		Map extraInfo = CacheController.getExtraInfo(SiteNodeVersionImpl.class.getName(), getObjectIdentity(object).toString());
    		//System.out.println("extraInfo in jdo callback:" + extraInfo);
    		boolean skipRemoteUpdate = false;	
    		if(extraInfo != null && object.getClass().getName().indexOf("SiteNodeVersion") > -1 && extraInfo.containsKey("skipSiteNodeVersionUpdate"))
    			skipRemoteUpdate = true;
    		//System.out.println("skipRemoteUpdate:" + skipRemoteUpdate);
			//This uses a hook and adds extra info to the notification if it exists
			NotificationMessage notificationMessage = new NotificationMessage("CmsJDOCallback", object.getClass().getName(), userName, NotificationMessage.TRANS_UPDATE, getObjectIdentity(object), getObjectName(object), CacheController.getExtraInfo(object.getClass().getName(), getObjectIdentity(object).toString()));
			if(!skipRemoteUpdate)
				ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
			
			if(object.getClass().getName().indexOf("org.infoglue.cms.entities.management") == -1)
			{
				new Thread(new SearchIndexHelper(notificationMessage, 2000L)).start();
				//LuceneController.getController().notify(notificationMessage);
	    	}
    	
			if(object.getClass().getName().indexOf("org.infoglue.cms.entities.management") > -1 && 
					!object.getClass().getName().equals(RegistryImpl.class.getName()) && 
					object.getClass().getName().indexOf("AccessRight") == -1)
			{
				//System.out.println("object.getClass():" + object.getClass());
				RemoteCacheUpdater.getSystemNotificationMessages().add(notificationMessage);
			}
			
			if(object.getClass().getName().equals(RepositoryImpl.class.getName()))
			{
				CacheController.clearCache("repositoryCache");
			}
			else if(object.getClass().getName().equals(CategoryImpl.class.getName()))
			{
				CacheController.clearCache("categoriesCache");
			}
			else if(object.getClass().getName().equals(CategoryImpl.class.getName()) || object.getClass().getName().equals(ContentTypeDefinitionImpl.class.getName()))
			{
				CacheController.clearCache("contentTypeCategoryKeysCache");
			}
			else if(object.getClass().getName().equals(InterceptionPointImpl.class.getName()))
			{
				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
			}
			else if(object.getClass().getName().equals(InterceptorImpl.class.getName()))
			{
				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
			}
			else if(object.getClass().getName().equals(AccessRightImpl.class.getName()) || object.getClass().getName().equals(AccessRightRoleImpl.class.getName()) || object.getClass().getName().equals(AccessRightGroupImpl.class.getName()) || object.getClass().getName().equals(AccessRightUserImpl.class.getName()))
			{
				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
				CacheController.clearCache("componentContentsCache");
			}
			else if(object.getClass().getName().equals(ContentTypeDefinitionImpl.class.getName()))
			{
				CacheController.clearCache("contentTypeDefinitionCache");
			}
			else if(object.getClass().getName().equals(ContentImpl.class.getName()))
			{
				//CacheController.clearCache("childContentCache");
				try
				{
					ContentImpl content = (ContentImpl)object;
					if(content.getContentTypeDefinition() == null || !content.getContentTypeDefinition().getName().equalsIgnoreCase("Meta info"))
					{
						CacheController.clearCache("componentContentsCache");		
					}
					CacheController.clearCacheForGroup("contentCache", "" + content.getId());
					CacheController.clearCacheForGroup("contentVersionCache", "content_" + content.getId());
					CacheController.clearCacheForGroup("childContentCache", "content_" + content.getId());
					if(content.getParentContent() != null)
						CacheController.clearCacheForGroup("childContentCache", "content_" + content.getParentContent().getId());					
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage(), e);
				}
				
				clearCache(SmallContentImpl.class);
				clearCache(SmallishContentImpl.class);
				clearCache(MediumContentImpl.class);
			}
			else if(object.getClass().getName().equals(ContentVersionImpl.class.getName()))
			{
				try
				{
					ContentVersionImpl contentVersion = (ContentVersionImpl)object;
					if(contentVersion.getOwningContent().getContentTypeDefinition() == null || !contentVersion.getOwningContent().getContentTypeDefinition().getName().equalsIgnoreCase("Meta info"))
					{
						CacheController.clearCache("componentContentsCache");						
					}
					CacheController.clearCacheForGroup("registryCache", "" + ("org.infoglue.cms.entities.content.ContentVersion_" + getObjectIdentity(object)).hashCode());						

					CacheController.clearCacheForGroup("contentVersionCache", "content_" + contentVersion.getOwningContent().getId());
					CacheController.clearCacheForGroup("contentVersionCache", "contentVersion_" + contentVersion.getId());
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage(), e);
				}

				clearCache(SmallContentVersionImpl.class);
				clearCache(SmallestContentVersionImpl.class);
			}
			else if(object.getClass().getName().equals(RepositoryLanguageImpl.class.getName()))
			{
				CacheController.clearCache("masterLanguageCache");
				CacheController.clearCache("repositoryLanguageListCache");
			}
			else if(object.getClass().getName().equals(MediumDigitalAssetImpl.class.getName()))
			{
				CacheController.clearCache("digitalAssetCache");
				CacheController.clearCache("cachedAssetFileList");
				clearCache(SmallDigitalAssetImpl.class);
				clearCache(DigitalAssetImpl.class);
				//logger.error("We should delete all images with digitalAssetId " + getObjectIdentity(object));
				DigitalAssetController.deleteCachedDigitalAssets((Integer)getObjectIdentity(object));
			}
			else if(object.getClass().getName().equals(DigitalAssetImpl.class.getName()))
			{
				CacheController.clearCache("digitalAssetCache");
				CacheController.clearCache("cachedAssetFileList");
				clearCache(SmallDigitalAssetImpl.class);
				clearCache(MediumDigitalAssetImpl.class);
				//logger.error("We should delete all images with digitalAssetId " + getObjectIdentity(object));
				DigitalAssetController.deleteCachedDigitalAssets((Integer)getObjectIdentity(object));
			}
			else if(object.getClass().getName().equals(SiteNodeImpl.class.getName()))
			{
				clearCache(SmallSiteNodeImpl.class);
				
				try
				{
					SiteNodeImpl siteNode = (SiteNodeImpl)object;
					CacheController.clearCache("siteNodeCache", "" + siteNode.getId());
					CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNode.getId());
					if(siteNode.getParentSiteNode() != null)
						CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNode.getParentSiteNode().getId());					
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage(), e);
				}

				//CacheController.clearCache("childSiteNodesCache");
				CacheController.clearCache("parentSiteNodeCache");
				CacheController.clearCacheForGroup("latestSiteNodeVersionCache", "siteNode_" + (Integer)getObjectIdentity(object));
				CacheController.clearCache("siteNodeCache","" + (Integer)getObjectIdentity(object));
			}
			else if(object.getClass().getName().equals(SiteNodeVersionImpl.class.getName()))
			{
				CacheController.clearCacheForGroup("registryCache", "" + ("org.infoglue.cms.entities.structure.SiteNodeVersion_" + getObjectIdentity(object)).hashCode());						

				clearCache(SmallSiteNodeVersionImpl.class);

				try
				{
					SiteNodeVersionImpl siteNodeVersion = (SiteNodeVersionImpl)object;
					CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNodeVersion.getOwningSiteNode().getId());
					if(siteNodeVersion.getOwningSiteNode().getParentSiteNode() != null)
						CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNodeVersion.getOwningSiteNode().getParentSiteNode().getId());					
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage(), e);
				}

				//CacheController.clearCache("childSiteNodesCache");
				CacheController.clearCache("parentSiteNodeCache");
				SiteNodeVersionImpl siteNodeVersion = (SiteNodeVersionImpl)object;
				CacheController.clearCacheForGroup("latestSiteNodeVersionCache", "siteNode_" + siteNodeVersion.getOwningSiteNode().getId());
			}
			else if(object.getClass().getName().equals(WorkflowDefinitionImpl.class.getName()))
			{
				CacheController.clearCache("workflowCache");
			}
			else if(object.getClass().getName().equals(SystemUserImpl.class.getName()))
			{
				CacheController.clearCache("principalCache");
				CacheController.clearCache("componentContentsCache");
				CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
			}
			else if(object.getClass().getName().equals(GroupImpl.class.getName()))
			{
				CacheController.clearCache("groupListCache");
				CacheController.clearCache("groupVOListCache");
				CacheController.clearCache("componentContentsCache");
			    CacheController.clearCache("principalPropertyValueCache");
			}
			else if(object.getClass().getName().equals(RoleImpl.class.getName()))
			{
				CacheController.clearCache("roleListCache");
				CacheController.clearCache("roleVOListCache");
				CacheController.clearCache("componentContentsCache");
			    CacheController.clearCache("principalPropertyValueCache");
			}
			else if(object.getClass().getName().equals(SystemUserGroupImpl.class.getName()))
			{
				//clearCache(SystemUserImpl.class);
				//clearCache(GroupImpl.class);
				CacheController.clearCache("groupListCache");
				CacheController.clearCache("groupVOListCache");
				CacheController.clearCache("componentContentsCache");

				CacheController.clearCache("principalCache");
				CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("rolePropertiesCache");
				CacheController.clearCache("groupPropertiesCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");

				new Thread(new Runnable() { public void run() {try {CmsSessionContextListener.reCacheSessionPrincipal();} catch (Exception e) {}}}).start();
			}
			else if(object.getClass().getName().equals(SystemUserRoleImpl.class.getName()))
			{
				//clearCache(SystemUserImpl.class);
				//clearCache(RoleImpl.class);
				CacheController.clearCache("roleListCache");
				CacheController.clearCache("roleVOListCache");
				CacheController.clearCache("componentContentsCache");

				CacheController.clearCache("principalCache");
				CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("rolePropertiesCache");
				CacheController.clearCache("groupPropertiesCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
			
				new Thread(new Runnable() { public void run() {try {CmsSessionContextListener.reCacheSessionPrincipal();} catch (Exception e) {}}}).start();
			}
			else if(object.getClass().getName().equals(UserPropertiesImpl.class.getName()))
			{
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("relatedCategoriesCache");
			}
			else if(object.getClass().getName().equals(GroupPropertiesImpl.class.getName()))
			{
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("groupPropertiesCache");
				CacheController.clearCache("relatedCategoriesCache");
			}
			else if(object.getClass().getName().equals(RolePropertiesImpl.class.getName()))
			{
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("rolePropertiesCache");
				CacheController.clearCache("relatedCategoriesCache");
			}
			else if(object.getClass().getName().equals(AvailableServiceBindingImpl.class.getName()))
			{
			    CacheController.clearCache("availableServiceBindingCache");
			}
			else if(object.getClass().getName().equals(LanguageImpl.class.getName()))
			{
			    CacheController.clearCache("languageCache");
			}
			
    	}
    }

	private synchronized void clearCache(Class c) throws Exception
	{
		Database db = CastorDatabaseService.getDatabase();

		try
		{
		    Class[] types = {c};
			Class[] ids = {null};
			CacheManager manager = db.getCacheManager();
			manager.expireCache(types);
			//db.expireCache(types, null);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			db.close();			
		}
	}

    public void creating( Object object, Database db )
        throws Exception
    {
        // ( (Persistent) object ).jdoBeforeCreate( db );
    }


    public void created(Object object) throws Exception
    {
    	//if(object.getClass().getName().indexOf("SystemUserImpl") > -1)
    	//	return;
    	
    	Timer t = new Timer();
    	
        // ( (Persistent) object ).jdoAfterCreate();

		// Write to trans-log
    	
    	//String className = object.getClass().getName();		
		//if (CmsSystem.getTransactionHistoryEntityClassName().indexOf(className) == -1)
		//	CmsSystem.transactionLogEntry("CMSJDOCallback:" + object.getClass().getName(), CmsSystem.TRANS_CREATE, getEntityId(object), object.toString());        
		//logger.error("created...:" + object + ":" + object.getClass().getName());
    	if(logger.isInfoEnabled())
    		logger.info("created..........................." + object + ":" + object.getClass().getName());
    	
    	if (AccessRightGroupImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
			AccessRightRoleImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
			AccessRightUserImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
			TransactionHistoryImpl.class.getName().indexOf(object.getClass().getName()) == -1 && 
    		RegistryImpl.class.getName().indexOf(object.getClass().getName()) == -1 && 
    		SubscriptionFilterImpl.class.getName().indexOf(object.getClass().getName()) == -1)
	    {
    	    String userName = "SYSTEM";
			try
			{
				InfoGluePrincipal principal = InfoGlueAbstractAction.getSessionInfoGluePrincipal();
				if(principal != null && principal.getName() != null)
					userName = principal.getName();
		    } 
			catch (NoClassDefFoundError e){}
			
    	    NotificationMessage notificationMessage = new NotificationMessage("CMSJDOCallback", object.getClass().getName(), userName, NotificationMessage.TRANS_CREATE, getObjectIdentity(object), getObjectName(object), CacheController.getExtraInfo(object.getClass().getName(), getObjectIdentity(object).toString()));
    	    ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
			
			if(object.getClass().getName().indexOf("org.infoglue.cms.entities.management") == -1)
			{
				new Thread(new SearchIndexHelper(notificationMessage, 2000L)).start();
			}
			
    	    if(object.getClass().getName().indexOf("org.infoglue.cms.entities.management") > -1 && 
    	    		!object.getClass().getName().equals(RegistryImpl.class.getName()) &&
    	    		object.getClass().getName().indexOf("AccessRight") == -1)
			    RemoteCacheUpdater.getSystemNotificationMessages().add(notificationMessage);

			if(object.getClass().getName().equals(RepositoryImpl.class.getName()))
			{
				CacheController.clearCache("repositoryCache");
			}
			else if(object.getClass().getName().equals(CategoryImpl.class.getName()))
			{
				CacheController.clearCache("categoriesCache");
			}
			else if(object.getClass().getName().equals(InterceptionPointImpl.class.getName()))
			{
				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
			}
			else if(object.getClass().getName().equals(InterceptorImpl.class.getName()))
			{
				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
			}
			else if(object.getClass().getName().equals(AccessRightImpl.class.getName()) || object.getClass().getName().equals(AccessRightRoleImpl.class.getName()) || object.getClass().getName().equals(AccessRightGroupImpl.class.getName()) || object.getClass().getName().equals(AccessRightUserImpl.class.getName()))
			{
				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
				CacheController.clearCache("componentContentsCache");
			}
			else if(object.getClass().getName().equals(ContentTypeDefinitionImpl.class.getName()))
			{
				CacheController.clearCache("contentTypeDefinitionCache");
			}
			else if(object.getClass().getName().equals(ContentImpl.class.getName()))
			{
				//CacheController.clearCache("childContentCache");

				clearCache(SmallContentImpl.class);
				clearCache(SmallishContentImpl.class);
				clearCache(MediumContentImpl.class);
			}
			else if(object.getClass().getName().equals(ContentVersionImpl.class.getName()))
			{
				CacheController.clearCache("componentContentsCache");

				clearCache(SmallContentVersionImpl.class);
				clearCache(SmallestContentVersionImpl.class);
			}
			else if(object.getClass().getName().equals(SiteNodeImpl.class.getName()))
			{
				//CacheController.clearCache("childSiteNodesCache");
				CacheController.clearCache("parentSiteNodeCache");
				CacheController.clearCacheForGroup("latestSiteNodeVersionCache", "siteNode_" + (Integer)getObjectIdentity(object));
				CacheController.clearCache("siteNodeCache","" + (Integer)getObjectIdentity(object));
			}
			else if(object.getClass().getName().equals(SiteNodeVersionImpl.class.getName()))
			{
				//CacheController.clearCache("childSiteNodesCache");
				CacheController.clearCache("parentSiteNodeCache");
				SiteNodeVersionImpl siteNodeVersion = (SiteNodeVersionImpl)object;
				CacheController.clearCacheForGroup("latestSiteNodeVersionCache", "siteNode_" + siteNodeVersion.getOwningSiteNode().getId());
			}
			else if(object.getClass().getName().equals(RepositoryLanguageImpl.class.getName()))
			{
				CacheController.clearCache("masterLanguageCache");
				CacheController.clearCache("repositoryLanguageListCache");
			}
			else if(object.getClass().getName().equals(WorkflowDefinitionImpl.class.getName()))
			{
				CacheController.clearCache("workflowCache");
			}
			else if(object.getClass().getName().equals(SystemUserImpl.class.getName()))
			{
				CacheController.clearCache("principalCache");
				CacheController.clearCache("componentContentsCache");
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
			}
			else if(object.getClass().getName().equals(GroupImpl.class.getName()))
			{
				CacheController.clearCache("groupListCache");
				CacheController.clearCache("componentContentsCache");
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
			}
			else if(object.getClass().getName().equals(RoleImpl.class.getName()))
			{
				CacheController.clearCache("roleListCache");
				CacheController.clearCache("componentContentsCache");
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
			}
			else if(object.getClass().getName().equals(SystemUserGroupImpl.class.getName()))
			{
				//clearCache(SystemUserImpl.class);
				//clearCache(GroupImpl.class);
				CacheController.clearCache("principalCache");
				CacheController.clearCache("groupListCache");
				CacheController.clearCache("componentContentsCache");
				
				CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("rolePropertiesCache");
				CacheController.clearCache("groupPropertiesCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
				
				new Thread(new Runnable() { public void run() {try {CmsSessionContextListener.reCacheSessionPrincipal();} catch (Exception e) {}}}).start();
			}
			else if(object.getClass().getName().equals(SystemUserRoleImpl.class.getName()))
			{
				//clearCache(SystemUserImpl.class);
				//clearCache(RoleImpl.class);
				CacheController.clearCache("principalCache");
				CacheController.clearCache("groupListCache");
				CacheController.clearCache("componentContentsCache");

				CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("rolePropertiesCache");
				CacheController.clearCache("groupPropertiesCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
				
				new Thread(new Runnable() { public void run() {try {CmsSessionContextListener.reCacheSessionPrincipal();} catch (Exception e) {}}}).start();
			}
			else if(object.getClass().getName().equals(UserPropertiesImpl.class.getName()))
			{
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("relatedCategoriesCache");
			}
			else if(object.getClass().getName().equals(GroupPropertiesImpl.class.getName()))
			{
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("groupPropertiesCache");
				CacheController.clearCache("relatedCategoriesCache");
			}
			else if(object.getClass().getName().equals(RolePropertiesImpl.class.getName()))
			{
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("rolePropertiesCache");
				CacheController.clearCache("relatedCategoriesCache");
			}
			else if(object.getClass().getName().equals(AvailableServiceBindingImpl.class.getName()))
			{
			    CacheController.clearCache("availableServiceBindingCache");
			}
			else if(object.getClass().getName().equals(LanguageImpl.class.getName()))
			{
			    CacheController.clearCache("languageCache");
			}
			else if(object.getClass().getName().equals(DigitalAssetImpl.class.getName()))
			{
				CacheController.clearCache("digitalAssetCache");
				clearCache(SmallDigitalAssetImpl.class);
				clearCache(MediumDigitalAssetImpl.class);
			}

			//logger.error("created end...:" + object);
    	}
    	
    	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("CmsJDOCallback.created", t.getElapsedTime());
    }


    public void removing( Object object ) throws Exception
    {
        // ( (Persistent) object ).jdoBeforeRemove();
    }


    public void removed( Object object ) throws Exception
    {
		//logger.error("removed...:" + object);
        // ( (Persistent) object ).jdoAfterRemove();
        
       	if (AccessRightGroupImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
			AccessRightRoleImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
			AccessRightUserImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
			TransactionHistoryImpl.class.getName().indexOf(object.getClass().getName()) == -1 && 
       		RegistryImpl.class.getName().indexOf(object.getClass().getName()) == -1 && 
    		SubscriptionFilterImpl.class.getName().indexOf(object.getClass().getName()) == -1)
	    {
       	    String userName = "SYSTEM";
			try
			{	
				InfoGluePrincipal principal = InfoGlueAbstractAction.getSessionInfoGluePrincipal();
				if(principal != null && principal.getName() != null)
					userName = principal.getName();
			} 
			catch (NoClassDefFoundError e){}

		    NotificationMessage notificationMessage = new NotificationMessage("CMSJDOCallback", object.getClass().getName(), userName, NotificationMessage.TRANS_DELETE, getObjectIdentity(object), getObjectName(object), CacheController.getExtraInfo(object.getClass().getName(), getObjectIdentity(object).toString()));
		    ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
		    
			if(object.getClass().getName().indexOf("org.infoglue.cms.entities.management") == -1)
			{
				new Thread(new SearchIndexHelper(notificationMessage, 2000L)).start();
			}
			
			if(object.getClass().getName().indexOf("SystemUser") == -1)
			{
				NotificationMessage deleteNotificationMessage = new NotificationMessage("Object deleted:", object.getClass().getName(), userName, NotificationMessage.TRANS_DELETE, getObjectIdentity(object), getObjectName(object), CacheController.getExtraInfo(object.getClass().getName(), getObjectIdentity(object).toString()));
				TransactionHistoryController.getController().create(deleteNotificationMessage);

				if(object.getClass().getName().indexOf("org.infoglue.cms.entities.management") == -1)
				{
					new Thread(new SearchIndexHelper(deleteNotificationMessage, 2000L)).start();
				}
			}
			
			if(object.getClass().getName().indexOf("org.infoglue.cms.entities.management") > -1 && 
					!object.getClass().getName().equals(RegistryImpl.class.getName()) && 
					object.getClass().getName().indexOf("AccessRight") == -1
					)
			    RemoteCacheUpdater.getSystemNotificationMessages().add(notificationMessage);

			if(object.getClass().getName().equals(RepositoryImpl.class.getName()))
			{
				CacheController.clearCache("repositoryCache");
				CacheController.clearCache("repositoryRootNodesCache");
			}
			else if(object.getClass().getName().equals(CategoryImpl.class.getName()))
			{
				CacheController.clearCache("categoriesCache");
			}
			else if(object.getClass().getName().equals(InterceptionPointImpl.class.getName()))
			{
				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
			}
			else if(object.getClass().getName().equals(InterceptorImpl.class.getName()))
			{
				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
			}
			else if(object.getClass().getName().equals(AccessRightImpl.class.getName()) || object.getClass().getName().equals(AccessRightRoleImpl.class.getName()) || object.getClass().getName().equals(AccessRightGroupImpl.class.getName()) || object.getClass().getName().equals(AccessRightUserImpl.class.getName()))
			{
				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
				CacheController.clearCache("componentContentsCache");
			}
			else if(object.getClass().getName().equals(ContentTypeDefinitionImpl.class.getName()))
			{
				CacheController.clearCache("contentTypeDefinitionCache");
			}
			else if(object.getClass().getName().equals(ContentImpl.class.getName()))
			{
				try
				{
					ContentImpl content = (ContentImpl)object;
					CacheController.clearCacheForGroup("contentCache", "" + content.getId());
					CacheController.clearCacheForGroup("childContentCache", "content_" + content.getId());
					if(content.getParentContent() != null)
						CacheController.clearCacheForGroup("childContentCache", "content_" + content.getParentContent().getId());					
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage(), e);
				}
				//CacheController.clearCache("childContentCache");

				clearCache(SmallContentImpl.class);
				clearCache(SmallishContentImpl.class);
				clearCache(MediumContentImpl.class);

				RegistryController.getController().clearRegistryForReferencedEntity(Content.class.getName(), getObjectIdentity(object).toString());
				RegistryController.getController().clearRegistryForReferencingEntityCompletingName(Content.class.getName(), getObjectIdentity(object).toString());
			}
			else if(object.getClass().getName().equals(ContentVersionImpl.class.getName()))
			{
				CacheController.clearCacheForGroup("contentCategoryCache", "contentVersion_" + getObjectIdentity(object).toString());
				CacheController.clearCache("componentContentsCache");
				clearCache(SmallContentVersionImpl.class);
				clearCache(SmallestContentVersionImpl.class);
				RegistryController.getController().clearRegistryForReferencingEntityName(ContentVersion.class.getName(), getObjectIdentity(object).toString());
			}
			else if(object.getClass().getName().equals(RepositoryLanguageImpl.class.getName()))
			{
				CacheController.clearCache("masterLanguageCache");
				CacheController.clearCache("repositoryLanguageListCache");
			}
			else if(object.getClass().getName().equals(DigitalAssetImpl.class.getName()))
			{
				CacheController.clearCache("digitalAssetCache");
				clearCache(SmallDigitalAssetImpl.class);
				clearCache(MediumDigitalAssetImpl.class);
				//logger.info("We should delete all images with digitalAssetId " + getObjectIdentity(object));
				DigitalAssetController.deleteCachedDigitalAssets((Integer)getObjectIdentity(object));
			}
						
			else if(object.getClass().getName().equals(SiteNodeImpl.class.getName()))
			{
			    RegistryController.getController().clearRegistryForReferencedEntity(SiteNode.class.getName(), getObjectIdentity(object).toString());
				RegistryController.getController().clearRegistryForReferencingEntityCompletingName(SiteNode.class.getName(), getObjectIdentity(object).toString());
				RedirectController.getController().deleteRelatedRedirects(Integer.parseInt(getObjectIdentity(object).toString()));
				clearCache(SmallSiteNodeImpl.class);
				
				try
				{
					SiteNodeImpl siteNode = (SiteNodeImpl)object;
					CacheController.clearCache("siteNodeCache", "" + siteNode.getId());
					CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNode.getId());
					if(siteNode.getParentSiteNode() != null)
						CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNode.getParentSiteNode().getId());					
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage(), e);
				}

				//CacheController.clearCache("childSiteNodesCache");
				CacheController.clearCache("parentSiteNodeCache");
				CacheController.clearCache("repositoryRootNodesCache");
				CacheController.clearCacheForGroup("latestSiteNodeVersionCache", "siteNode_" + (Integer)getObjectIdentity(object));
				CacheController.clearCache("siteNodeCache","" + (Integer)getObjectIdentity(object));
			}
			else if(object.getClass().getName().equals(SiteNodeVersionImpl.class.getName()))
			{
				clearCache(SmallSiteNodeVersionImpl.class);
				
				try
				{
					SiteNodeVersionImpl siteNodeVersion = (SiteNodeVersionImpl)object;
					CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNodeVersion.getOwningSiteNode().getId());
					if(siteNodeVersion.getOwningSiteNode().getParentSiteNode() != null)
						CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNodeVersion.getOwningSiteNode().getParentSiteNode().getId());					
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage(), e);
				}
				
				//CacheController.clearCache("childSiteNodesCache");
				CacheController.clearCache("parentSiteNodeCache");
				SiteNodeVersionImpl siteNodeVersion = (SiteNodeVersionImpl)object;
				CacheController.clearCacheForGroup("latestSiteNodeVersionCache", "siteNode_" + siteNodeVersion.getOwningSiteNode().getId());

				RegistryController.getController().clearRegistryForReferencingEntityName(SiteNodeVersion.class.getName(), getObjectIdentity(object).toString());
			}
			else if(object.getClass().getName().equals(WorkflowDefinitionImpl.class.getName()))
			{
				CacheController.clearCache("workflowCache");
			}
			else if(object.getClass().getName().equals(SystemUserImpl.class.getName()))
			{
				CacheController.clearCache("principalCache");
				CacheController.clearCache("componentContentsCache");
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
			}
			else if(object.getClass().getName().equals(GroupImpl.class.getName()))
			{
				CacheController.clearCache("groupListCache");
				CacheController.clearCache("componentContentsCache");
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
			}
			else if(object.getClass().getName().equals(RoleImpl.class.getName()))
			{
				CacheController.clearCache("roleListCache");
				CacheController.clearCache("componentContentsCache");
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
			}
			else if(object.getClass().getName().equals(SystemUserGroupImpl.class.getName()))
			{
				CacheController.clearCache("principalCache");
				CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("rolePropertiesCache");
				CacheController.clearCache("groupPropertiesCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
				new Thread(new Runnable() { public void run() {try {CmsSessionContextListener.reCacheSessionPrincipal();} catch (Exception e) {}}}).start();
			}
			else if(object.getClass().getName().equals(SystemUserRoleImpl.class.getName()))
			{
				CacheController.clearCache("principalCache");
				CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("rolePropertiesCache");
				CacheController.clearCache("groupPropertiesCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
				new Thread(new Runnable() { public void run() {try {CmsSessionContextListener.reCacheSessionPrincipal();} catch (Exception e) {}}}).start();
			}
			else if(object.getClass().getName().equals(UserPropertiesImpl.class.getName()))
			{
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("relatedCategoriesCache");
			}
			else if(object.getClass().getName().equals(GroupPropertiesImpl.class.getName()))
			{
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("groupPropertiesCache");
				CacheController.clearCache("relatedCategoriesCache");
			}
			else if(object.getClass().getName().equals(RolePropertiesImpl.class.getName()))
			{
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("rolePropertiesCache");
				CacheController.clearCache("relatedCategoriesCache");
			}
			else if(object.getClass().getName().equals(AvailableServiceBindingImpl.class.getName()))
			{
			    CacheController.clearCache("availableServiceBindingCache");
			}
			else if(object.getClass().getName().equals(LanguageImpl.class.getName()))
			{
			    CacheController.clearCache("languageCache");
			}


       	}
    }


    public void releasing(Object object, boolean committed)
    {
        //logger.error("releasing...:" + object + ":" + committed);
        // ( (Persistent) object ).jdoTransient();
        
        /*
        if(DigitalAssetImpl.class.getName().equals(object.getClass().getName()) && committed)
	    {
	        logger.info("Actually releasing it:" + object + ":" + committed);
    		String userName = "SYSTEM";
    		InfoGluePrincipal principal = InfoGlueAbstractAction.getSessionInfoGluePrincipal();
			if(principal != null && principal.getName() != null)
				userName = principal.getName();

	    	NotificationMessage notificationMessage = new NotificationMessage("CmsJDOCallback", object.getClass().getName(), userName, NotificationMessage.TRANS_UPDATE, getObjectIdentity(object), getObjectName(object));
	    	ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
	    }
	    */

    }


    public void updated(Object object) throws Exception
    {
        //logger.error("updated...:" + object);
        // ( (Persistent) object ).jdoUpdate();
    	
    	//String className = object.getClass().getName();
		//if (CmsSystem.getTransactionHistoryEntityClassName().indexOf(className) == -1)
		//	CmsSystem.transactionLogEntry("CMSJDOCallback:" + object.getClass().getName(), CmsSystem.TRANS_UPDATE, getEntityId(object), object.toString());   

//		logger.info("updated..........................." + object);
/*
     	if (TransactionHistoryImpl.class.getName().indexOf(object.getClass().getName()) == -1)
	    {
	    	String userName = "Fix later";
			InfoGluePrincipal principal = InfoGlueAbstractAction.getSessionInfoGluePrincipal();
			if(principal != null && principal.getName() != null)
				userName = principal.getName();

	    	NotificationMessage notificationMessage = new NotificationMessage("CMSJDOCallback:" + object.getClass().getName(), object.getClass().getName(), userName, CmsSystem.TRANS_UPDATE, getObjectIdentity(object), getObjectName(object));
	    	ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
	    }
*/
     }


	private Integer getEntityId(Object entity) throws Bug
	{
		Integer entityId = new Integer(-1);
		
		try
		{
			entityId = ((IBaseEntity) entity).getId();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Bug("Unable to retrieve object id");
		}
		
		return entityId;
	}

	private Object getObjectIdentity(Object entity) throws Bug
	{
		Object objectIdentity = new Integer(-1);
		
		try 
		{
			objectIdentity = ((IBaseEntity) entity).getIdAsObject();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Bug("Unable to retrieve object identity");
		}
		
		return objectIdentity;
	}

	private String getObjectName(Object entity) throws Bug
	{
		String objectName = null;
		
		if(entity instanceof ContentImpl)
			objectName = ((Content)entity).getName();
		else if(entity instanceof ContentVersionImpl)
			objectName = ((ContentVersion)entity).getOwningContent().getName() + " (" + ((ContentVersion)entity).getLanguage().getName() + " version)";
		else if(entity instanceof SiteNodeImpl)
			objectName = ((SiteNode)entity).getName();
		else if(entity instanceof SmallSiteNodeVersionImpl)
			objectName = ((SmallSiteNodeVersionImpl)entity).getSiteNodeVersionId() + "";
		else if(entity instanceof SiteNodeVersionImpl)
			objectName = ((SiteNodeVersion)entity).getOwningSiteNode().getName();
		else
			objectName = entity.toString();
		
		return objectName;
	}

}

