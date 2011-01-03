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
 
import org.apache.log4j.Logger;
import org.exolab.castor.jdo.CacheManager;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.persist.spi.CallbackInterceptor;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
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
import org.infoglue.cms.entities.kernel.IBaseEntity;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl;
import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
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
import org.infoglue.cms.entities.management.impl.simple.SystemUserImpl;
import org.infoglue.cms.entities.management.impl.simple.TransactionHistoryImpl;
import org.infoglue.cms.entities.management.impl.simple.UserPropertiesImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl;
import org.infoglue.cms.entities.workflow.impl.simple.WorkflowDefinitionImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ChangeNotificationController;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.cms.util.RemoteCacheUpdater;
import org.infoglue.deliver.util.CacheController;


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
    	//System.out.println("Using " + object);
        // ( (Persistent) object ).jdoPersistent( db );
    }


    public Class loaded(Object object, short accessMode) throws Exception
    {
		//System.out.println("Loaded " + object.getClass().getName() + " accessMode:" + accessMode);
		//if(accessMode == AccessMode.Shared.getId())
    	//	Thread.dumpStack();
		
		// return ( (Persistent) object ).jdoLoad(accessMode);
        return null;
    }

	public Class loaded(Object arg0, AccessMode arg1) throws Exception 
	{
		//System.out.println("Loaded " + arg0.getClass().getName() + " accessMode:" + arg1);
		//if(arg1.getId() == AccessMode.Shared.getId())
		//	Thread.dumpStack();
		
		return null;
	}

    public void storing(Object object, boolean modified) throws Exception
    {
		//System.out.println("storing...:" + object + ":" + modified);
        // ( (Persistent) object ).jdoStore( modified );
		//if(object.getClass().getName().indexOf("Small") > -1 || object.getClass().getName().indexOf("RepositoryLanguage") > -1 || object.getClass().getName().indexOf("ContentImpl") > -1 || object.getClass().getName().indexOf("SiteNodeImpl") > -1 || object.getClass().getName().indexOf("AccessRight") > -1 || object.getClass().getName().indexOf("StructureTool.Read") > -1)
		//if(object.getClass().getName().indexOf("ContentImpl") > -1)
		//	Thread.dumpStack();

   		//logger.info("Should we store -------------->" + object + ":" + modified);
    	if (TransactionHistoryImpl.class.getName().indexOf(object.getClass().getName()) == -1 && 
    		RegistryImpl.class.getName().indexOf(object.getClass().getName()) == -1 && 
    		SubscriptionFilterImpl.class.getName().indexOf(object.getClass().getName()) == -1 && modified)
	    {
	        //System.out.println("Actually stored it:" + object + ":" + modified);
	    	logger.info("Actually stored it:" + object + ":" + modified);
    	    
			String userName = "SYSTEM";
			try
			{
				InfoGluePrincipal principal = InfoGlueAbstractAction.getSessionInfoGluePrincipal();
				if(principal != null && principal.getName() != null)
					userName = principal.getName();				
			} 
			catch (NoClassDefFoundError e){}
			
			NotificationMessage notificationMessage = new NotificationMessage("CmsJDOCallback", object.getClass().getName(), userName, NotificationMessage.TRANS_UPDATE, getObjectIdentity(object), object.toString());
			ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
			if(object.getClass().getName().indexOf("org.infoglue.cms.entities.management") > -1 && !object.getClass().getName().equals(RegistryImpl.class.getName()))
			    RemoteCacheUpdater.getSystemNotificationMessages().add(notificationMessage);
			    
			if(object.getClass().getName().equals(RepositoryImpl.class.getName()))
			{
				CacheController.clearCache("repositoryCache");
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
				CacheController.clearCache("childContentCache");
				try
				{
					ContentImpl content = (ContentImpl)object;
					if(content.getContentTypeDefinition() == null || !content.getContentTypeDefinition().getName().equalsIgnoreCase("Meta info"))
					{
						CacheController.clearCache("componentContentsCache");						
					}
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
				clearCache(SmallDigitalAssetImpl.class);
				clearCache(DigitalAssetImpl.class);
				//System.out.println("We should delete all images with digitalAssetId " + getObjectIdentity(object));
				DigitalAssetController.deleteCachedDigitalAssets((Integer)getObjectIdentity(object));
			}
			else if(object.getClass().getName().equals(DigitalAssetImpl.class.getName()))
			{
				CacheController.clearCache("digitalAssetCache");
				clearCache(SmallDigitalAssetImpl.class);
				clearCache(MediumDigitalAssetImpl.class);
				//System.out.println("We should delete all images with digitalAssetId " + getObjectIdentity(object));
				DigitalAssetController.deleteCachedDigitalAssets((Integer)getObjectIdentity(object));
			}
			else if(object.getClass().getName().equals(SiteNodeImpl.class.getName()))
			{
				CacheController.clearCache("childSiteNodesCache");
			}
			else if(object.getClass().getName().equals(SiteNodeVersionImpl.class.getName()))
			{
				clearCache(SmallSiteNodeVersionImpl.class);
				CacheController.clearCache("childSiteNodesCache");
			}
			else if(object.getClass().getName().equals(WorkflowDefinitionImpl.class.getName()))
			{
				CacheController.clearCache("workflowCache");
			}
			else if(object.getClass().getName().equals(SystemUserImpl.class.getName()))
			{
				CacheController.clearCache("principalCache");
				CacheController.clearCache("componentContentsCache");
			}
			else if(object.getClass().getName().equals(GroupImpl.class.getName()))
			{
				CacheController.clearCache("groupListCache");
				CacheController.clearCache("groupVOListCache");
				CacheController.clearCache("componentContentsCache");
			}
			else if(object.getClass().getName().equals(RoleImpl.class.getName()))
			{
				CacheController.clearCache("roleListCache");
				CacheController.clearCache("roleVOListCache");
				CacheController.clearCache("componentContentsCache");
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
        // ( (Persistent) object ).jdoAfterCreate();

		// Write to trans-log
    	
    	//String className = object.getClass().getName();		
		//if (CmsSystem.getTransactionHistoryEntityClassName().indexOf(className) == -1)
		//	CmsSystem.transactionLogEntry("CMSJDOCallback:" + object.getClass().getName(), CmsSystem.TRANS_CREATE, getEntityId(object), object.toString());        
		//System.out.println("created...:" + object + ":" + object.getClass().getName());
    	logger.info("created..........................." + object + ":" + object.getClass().getName());
    	if (TransactionHistoryImpl.class.getName().indexOf(object.getClass().getName()) == -1 && 
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
			
    	    NotificationMessage notificationMessage = new NotificationMessage("CMSJDOCallback", object.getClass().getName(), userName, NotificationMessage.TRANS_CREATE, getObjectIdentity(object), object.toString());
    	    ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
			if(object.getClass().getName().indexOf("org.infoglue.cms.entities.management") > -1 && !object.getClass().getName().equals(RegistryImpl.class.getName()))
			    RemoteCacheUpdater.getSystemNotificationMessages().add(notificationMessage);

			if(object.getClass().getName().equals(RepositoryImpl.class.getName()))
			{
				CacheController.clearCache("repositoryCache");
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
				CacheController.clearCache("childContentCache");
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
			else if(object.getClass().getName().equals(SiteNodeVersionImpl.class.getName()))
			{
				CacheController.clearCache("childSiteNodesCache");
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
			}
			else if(object.getClass().getName().equals(GroupImpl.class.getName()))
			{
				CacheController.clearCache("groupListCache");
				CacheController.clearCache("componentContentsCache");
			}
			else if(object.getClass().getName().equals(RoleImpl.class.getName()))
			{
				CacheController.clearCache("roleListCache");
				CacheController.clearCache("componentContentsCache");
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

			//System.out.println("created end...:" + object);
    	}
    }


    public void removing( Object object )
        throws Exception
    {
        // ( (Persistent) object ).jdoBeforeRemove();
    }


    public void removed( Object object ) throws Exception
    {
		//System.out.println("removed...:" + object);
        // ( (Persistent) object ).jdoAfterRemove();
        
       	if (TransactionHistoryImpl.class.getName().indexOf(object.getClass().getName()) == -1 && 
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

		    NotificationMessage notificationMessage = new NotificationMessage("CMSJDOCallback", object.getClass().getName(), userName, NotificationMessage.TRANS_DELETE, getObjectIdentity(object), object.toString());
		    ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
			if(object.getClass().getName().indexOf("org.infoglue.cms.entities.management") > -1 && !object.getClass().getName().equals(RegistryImpl.class.getName()))
			    RemoteCacheUpdater.getSystemNotificationMessages().add(notificationMessage);

			if(object.getClass().getName().equals(RepositoryImpl.class.getName()))
			{
				CacheController.clearCache("repositoryCache");
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
				CacheController.clearCache("childContentCache");
				clearCache(SmallContentImpl.class);
				clearCache(SmallishContentImpl.class);
				clearCache(MediumContentImpl.class);

				RegistryController.getController().clearRegistryForReferencedEntity(Content.class.getName(), getObjectIdentity(object).toString());
				RegistryController.getController().clearRegistryForReferencingEntityCompletingName(Content.class.getName(), getObjectIdentity(object).toString());
			}
			else if(object.getClass().getName().equals(ContentVersionImpl.class.getName()))
			{
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
				CacheController.clearCache("childSiteNodesCache");
			}
			else if(object.getClass().getName().equals(SiteNodeVersionImpl.class.getName()))
			{
				clearCache(SmallSiteNodeVersionImpl.class);
				CacheController.clearCache("childSiteNodesCache");
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
			}
			else if(object.getClass().getName().equals(GroupImpl.class.getName()))
			{
				CacheController.clearCache("groupListCache");
				CacheController.clearCache("componentContentsCache");
			}
			else if(object.getClass().getName().equals(RoleImpl.class.getName()))
			{
				CacheController.clearCache("roleListCache");
				CacheController.clearCache("componentContentsCache");
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
        //System.out.println("releasing...:" + object + ":" + committed);
        // ( (Persistent) object ).jdoTransient();
        
        /*
        if(DigitalAssetImpl.class.getName().equals(object.getClass().getName()) && committed)
	    {
	        logger.info("Actually releasing it:" + object + ":" + committed);
    		String userName = "SYSTEM";
    		InfoGluePrincipal principal = InfoGlueAbstractAction.getSessionInfoGluePrincipal();
			if(principal != null && principal.getName() != null)
				userName = principal.getName();

	    	NotificationMessage notificationMessage = new NotificationMessage("CmsJDOCallback", object.getClass().getName(), userName, NotificationMessage.TRANS_UPDATE, getObjectIdentity(object), object.toString());
	    	ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
	    }
	    */

    }


    public void updated(Object object) throws Exception
    {
        //System.out.println("updated...:" + object);
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

	    	NotificationMessage notificationMessage = new NotificationMessage("CMSJDOCallback:" + object.getClass().getName(), object.getClass().getName(), userName, CmsSystem.TRANS_UPDATE, getEntityId(object), object.toString());
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

}

