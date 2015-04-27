
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
 
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.CacheManager;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.persist.spi.CallbackInterceptor;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumContentVersionImpl;
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
import org.infoglue.cms.entities.management.impl.simple.CategoryImpl;
import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.GroupImpl;
import org.infoglue.cms.entities.management.impl.simple.GroupPropertiesImpl;
import org.infoglue.cms.entities.management.impl.simple.InterceptionPointImpl;
import org.infoglue.cms.entities.management.impl.simple.InterceptorImpl;
import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
import org.infoglue.cms.entities.management.impl.simple.PageDeliveryMetaDataEntityImpl;
import org.infoglue.cms.entities.management.impl.simple.PageDeliveryMetaDataImpl;
import org.infoglue.cms.entities.management.impl.simple.RegistryImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryLanguageImpl;
import org.infoglue.cms.entities.management.impl.simple.RoleImpl;
import org.infoglue.cms.entities.management.impl.simple.RolePropertiesImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallRepositoryImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallSystemUserImpl;
import org.infoglue.cms.entities.management.impl.simple.SubscriptionFilterImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.TransactionHistoryImpl;
import org.infoglue.cms.entities.management.impl.simple.UserPropertiesImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.impl.simple.MediumSiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl;
import org.infoglue.cms.entities.workflow.impl.simple.WorkflowDefinitionImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ChangeNotificationController;
import org.infoglue.cms.util.CmsSessionContextListener;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.cms.util.RemoteCacheUpdater;
import org.infoglue.deliver.util.CacheController;
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

    public static boolean disableACLNotificationsToLive = false;

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
    	//if(arg0.getClass().getName().contains("RegistryImpl"))
		//	Thread.dumpStack();
		//System.out.println("Loaded 2" + arg0.getClass().getName());
		
		//if(arg1.getId() == AccessMode.Shared.getId() && arg0.getClass().getName().indexOf(".CategoryImpl") > -1)
		//	logger.error("Loaded 2" + arg0.getClass().getName() + " in write mode");
		
		return null;
	}


    public void storing(Object object, boolean modified) throws Exception
    {
/*
    	if(object.getClass().getName().indexOf(".ContentVersionImpl") > -1 || object.getClass().getName().indexOf(".ContentImpl") > -1 || object.getClass().getName().indexOf(".SiteNodeVersionImpl") > -1 || object.getClass().getName().indexOf(".SiteNodeImpl") > -1)
    	{
    		logger.warn("storing " + object.getClass().getName());
    		if(logger.isDebugEnabled())
    			Thread.dumpStack();
    	}
*/
		//logger.error("storing...:" + object + ":" + modified);
		// ( (Persistent) object ).jdoStore( modified );
   		
   		//logger.error("Should we store -------------->" + object.getClass().getName() + ":" + modified + ":" + ((IBaseEntity)object).getId());
    	if (AccessRightGroupImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
			AccessRightRoleImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
			AccessRightUserImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
    		TransactionHistoryImpl.class.getName().indexOf(object.getClass().getName()) == -1 && 
    		RegistryImpl.class.getName().indexOf(object.getClass().getName()) == -1 && 
    		SubscriptionFilterImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
    		PageDeliveryMetaDataImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
    		PageDeliveryMetaDataEntityImpl.class.getName().indexOf(object.getClass().getName()) == -1 && modified)
	    {
    		if(logger.isInfoEnabled())
    			logger.info("Actually stored it:" + object + ":" + modified);
    	    
			String userName = "SYSTEM";
			try
			{
				InfoGluePrincipal principal = InfoGlueAbstractAction.getSessionInfoGluePrincipal();
				if(principal != null && principal.getName() != null)
					userName = principal.getName();				
			} 
			catch (NoClassDefFoundError e){}
			
			Timer t = new Timer();
    		Map extraInfo = CacheController.getExtraInfo(SiteNodeVersionImpl.class.getName(), getObjectIdentity(object).toString());
    		//System.out.println("extraInfo in jdo callback:" + extraInfo);
    		boolean skipRemoteUpdate = false;	
    		if(extraInfo != null && object.getClass().getName().indexOf("SiteNodeVersion") > -1 && extraInfo.containsKey("skipSiteNodeVersionUpdate"))
    			skipRemoteUpdate = true;
    		//System.out.println("skipRemoteUpdate:" + skipRemoteUpdate);
			//This uses a hook and adds extra info to the notification if it exists
    		String storedClassName = getNotificationClassNameAndAddExtraInfo(object, false);
			NotificationMessage notificationMessage = new NotificationMessage("CmsJDOCallback", storedClassName, userName, NotificationMessage.TRANS_UPDATE, getObjectIdentity(object), getObjectName(object), CacheController.getExtraInfo(storedClassName, getObjectIdentity(object).toString()));
			if(!skipRemoteUpdate)
				ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
	
			boolean skipSystemNotificationUpdate = false;
			if(disableACLNotificationsToLive && (object.getClass().getName().indexOf("SystemUserImpl") > -1 || object.getClass().getName().indexOf("SystemUserGroupImpl") > -1 || object.getClass().getName().indexOf("SystemUserRoleImpl") > -1))
				skipSystemNotificationUpdate = true;

			if(object.getClass().getName().indexOf("org.infoglue.cms.entities.management") > -1 && 
					!object.getClass().getName().equals(RegistryImpl.class.getName()) && 
					object.getClass().getName().indexOf("AccessRight") == -1 && 
					object.getClass().getName().indexOf("ContentTypeDefinitionImpl") == -1 && 
					!skipSystemNotificationUpdate)
			{
				//System.out.println("object.getClass():" + object.getClass());
				RemoteCacheUpdater.getSystemNotificationMessages().add(notificationMessage);
			}
			    
			if(object.getClass().getName().equals(RepositoryImpl.class.getName()))
			{
				clearCache(SmallRepositoryImpl.class);
				CacheController.clearCache("repositoryCache");
			}
			if(object.getClass().getName().equals(SmallRepositoryImpl.class.getName()))
			{
				clearCache(RepositoryImpl.class);
				CacheController.clearCache("repositoryCache");
			}
			else if(object.getClass().getName().equals(CategoryImpl.class.getName()))
			{
				CacheController.clearCache("categoriesCache");
			}
			else if(object.getClass().getName().equals(CategoryImpl.class.getName()) || object.getClass().getName().equals(ContentTypeDefinitionImpl.class.getName()))
			{
				CacheController.clearCache("contentTypeDefinitionCache");
				CacheController.clearCache("contentTypeCategoryKeysCache");
			}
			else if(object.getClass().getName().equals(InterceptionPointImpl.class.getName()))
			{
				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
			}
			else if(object.getClass().getName().equals(InterceptorImpl.class.getName()))
			{
				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
			}
			else if(object.getClass().getName().equals(AccessRightImpl.class.getName()) || object.getClass().getName().equals(AccessRightRoleImpl.class.getName()) || object.getClass().getName().equals(AccessRightGroupImpl.class.getName()) || object.getClass().getName().equals(AccessRightUserImpl.class.getName()))
			{
				try
				{
					AccessRightImpl ar = (AccessRightImpl)object;
					String acKey = "" + ar.getValueObject().getInterceptionPointId();
					if(ar.getValueObject().getParameters() != null && !ar.getValueObject().getParameters().equals(""))
						acKey = "" + ar.getValueObject().getInterceptionPointId() + "_" + ar.getValueObject().getParameters();
					CacheController.clearUserAccessCache(acKey);						  
				}
				catch (Exception e) 
				{
					logger.error("Failed to clear user cache:" + e.getMessage());
				}
				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
			}
			else if(object.getClass().getName().equals(ContentImpl.class.getName()))
			{
				clearCache(SmallContentImpl.class);
				clearCache(SmallishContentImpl.class);
				clearCache(MediumContentImpl.class);

				//CacheController.clearCache("childContentCache");
				try
				{
					ContentImpl content = (ContentImpl)object;

					CacheController.clearCacheForGroup("contentCache", "content_" + content.getId());
					CacheController.clearCache("rootContentCache", "root_" + content.getRepositoryId());
					CacheController.clearCacheForGroup("contentVersionCache", "content_" + content.getId());
					CacheController.clearCacheForGroup("childContentCache", "content_" + content.getId());
					if(content.getParentContent() != null)
						CacheController.clearCacheForGroup("childContentCache", "content_" + content.getParentContent().getId());
					else
						CacheController.clearCache("rootContentCache", "root_" + content.getRepositoryId());
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage(), e);
				}
			}
			else if(object.getClass().getName().equals(MediumContentImpl.class.getName()))
			{
				clearCache(SmallContentImpl.class);
				clearCache(SmallishContentImpl.class);
				clearCache(ContentImpl.class);

				//CacheController.clearCache("childContentCache");
				try
				{
					MediumContentImpl content = (MediumContentImpl)object;
					CacheController.clearCacheForGroup("contentCache", "content_" + content.getId());
					CacheController.clearCacheForGroup("contentVersionCache", "content_" + content.getId());
					CacheController.clearCache("childContentCache");
					if(content.getParentContentId() == null)
						CacheController.clearCache("rootContentCache", "root_" + content.getRepositoryId());
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage(), e);
				}
			}
			else if(object.getClass().getName().equals(SmallContentImpl.class.getName()))
			{
				clearCache(MediumContentImpl.class);
				clearCache(SmallishContentImpl.class);
				clearCache(ContentImpl.class);

				//CacheController.clearCache("childContentCache");
				try
				{
					SmallContentImpl content = (SmallContentImpl)object;
					CacheController.clearCacheForGroup("contentCache", "content_" + content.getId());
					CacheController.clearCacheForGroup("contentVersionCache", "content_" + content.getId());
					CacheController.clearCache("childContentCache");
					if(content.getParentContentId() == null)
						CacheController.clearCache("rootContentCache", "root_" + content.getRepositoryId());
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage(), e);
				}
			}
			else if(object.getClass().getName().equals(ContentVersionImpl.class.getName()))
			{
				try
				{
					ContentVersionImpl contentVersion = (ContentVersionImpl)object;
					if(contentVersion.getOwningContent().getContentTypeDefinition() == null || (
					   contentVersion.getOwningContent().getContentTypeDefinition().getName().equalsIgnoreCase("HTMLTemplate") ||
					   contentVersion.getOwningContent().getContentTypeDefinition().getName().equalsIgnoreCase("PagePartTemplate")))
					{
						ComponentController.getController().reIndexComponentContentsDelayed(contentVersion.getOwningContent().getId());
					}

					CacheController.clearCacheForGroup("registryCache", "" + ("org.infoglue.cms.entities.content.ContentVersion_" + getObjectIdentity(object)).hashCode());						

					//CacheController.clearCacheForGroup("childContentCache", "content_" + contentVersion.getOwningContent().getId());
					CacheController.clearCacheForGroup("childContentCache", "content_" + contentVersion.getOwningContent().getId());
					if(contentVersion.getOwningContent().getValueObject().getParentContentId() != null)
						CacheController.clearCacheForGroup("childContentCache", "content_" + contentVersion.getOwningContent().getValueObject().getParentContentId());					

					CacheController.clearCacheForGroup("contentVersionCache", "content_" + contentVersion.getOwningContent().getId());
					CacheController.clearCacheForGroup("contentVersionCache", "contentVersion_" + contentVersion.getId());

					CacheController.clearCacheForGroup("contentAttributeCache", "content_" + contentVersion.getContentId());
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage(), e);
				}

				clearCache(MediumContentVersionImpl.class);
				clearCache(SmallContentVersionImpl.class);
				clearCache(SmallestContentVersionImpl.class);
			}
			else if(object.getClass().getName().equals(MediumContentVersionImpl.class.getName()))
			{
				try
				{
					ContentTypeDefinitionVO htmlTemplateMetaInfoCTDVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("HTMLTemplate");
					ContentTypeDefinitionVO pagePartTemplateMetaInfoCTDVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("PagePartTemplate");
					MediumContentVersionImpl contentVersion = (MediumContentVersionImpl)object;
					ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersion.getContentId(), true);
					if(contentVO.getContentTypeDefinitionId() == null || (
					   contentVO.getContentTypeDefinitionId().equals(htmlTemplateMetaInfoCTDVO.getId()) ||
					   (pagePartTemplateMetaInfoCTDVO != null && contentVO.getContentTypeDefinitionId().equals(pagePartTemplateMetaInfoCTDVO.getId()))))
					{
						ComponentController.getController().reIndexComponentContentsDelayed(contentVersion.getContentId());
					}

					CacheController.clearCacheForGroup("registryCache", "" + ("org.infoglue.cms.entities.content.ContentVersion_" + getObjectIdentity(object)).hashCode());

					CacheController.clearCacheForGroup("childContentCache", "content_" + contentVO.getId());
					if(contentVO.getParentContentId() != null)
						CacheController.clearCacheForGroup("childContentCache", "content_" + contentVO.getParentContentId());

					CacheController.clearCacheForGroup("contentVersionCache", "content_" + contentVersion.getContentId());
					CacheController.clearCacheForGroup("contentVersionCache", "contentVersion_" + contentVersion.getId());

					CacheController.clearCacheForGroup("contentAttributeCache", "content_" + contentVersion.getContentId());
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage());
				}

				clearCache(ContentVersionImpl.class);
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
				clearCache(SmallestSiteNodeImpl.class);
				
				try
				{
					SiteNodeImpl siteNode = (SiteNodeImpl)object;
					CacheController.clearCache("siteNodeCache", "" + siteNode.getId());
					CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNode.getId());
					CacheController.clearCacheForGroup("childPagesCache", "siteNode_" + siteNode.getId());
					if(siteNode.getParentSiteNode() != null)
					{
						CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNode.getParentSiteNode().getId());					
						CacheController.clearCacheForGroup("childPagesCache", "siteNode_" + siteNode.getParentSiteNode().getId());					
					}
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
			else if(object.getClass().getName().equals(SmallSiteNodeImpl.class.getName()))
			{
				clearCache(SiteNodeImpl.class);
				clearCache(SmallestSiteNodeImpl.class);
				
				try
				{
					SmallSiteNodeImpl siteNode = (SmallSiteNodeImpl)object;
					CacheController.clearCache("siteNodeCache", "" + siteNode.getId());
					CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNode.getId());
					CacheController.clearCacheForGroup("childPagesCache", "siteNode_" + siteNode.getId());
					if(siteNode.getParentSiteNodeId() != null)
					{
						CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNode.getParentSiteNodeId());					
						CacheController.clearCacheForGroup("childPagesCache", "siteNode_" + siteNode.getParentSiteNodeId());					
					}
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
			else if(object.getClass().getName().equals(SiteNodeVersionImpl.class.getName()) || object.getClass().getName().equals(MediumSiteNodeVersionImpl.class.getName()))
			{
				CacheController.clearCacheForGroup("registryCache", "" + ("org.infoglue.cms.entities.structure.SiteNodeVersion_" + getObjectIdentity(object)).hashCode());						
				
				clearCache(SmallSiteNodeVersionImpl.class);
				if(object.getClass().getName().equals(MediumSiteNodeVersionImpl.class.getName()))
				{
					clearCache(SiteNodeVersionImpl.class);

					try
					{
						MediumSiteNodeVersionImpl siteNodeVersion = (MediumSiteNodeVersionImpl)object;
						SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVersion.getValueObject().getSiteNodeId(), true);
						CacheController.clearCacheForGroup("siteNodeCacheWithLatestVersion", "siteNode_" + siteNodeVO.getId());
						CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNodeVO.getId());
						CacheController.clearCacheForGroup("childPagesCache", "siteNode_" + siteNodeVO.getId());
						if(siteNodeVO.getParentSiteNodeId() != null)
						{
							CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNodeVO.getParentSiteNodeId());					
							CacheController.clearCacheForGroup("childPagesCache", "siteNode_" + siteNodeVO.getParentSiteNodeId());
						}
						
						CacheController.clearCacheForGroup("latestSiteNodeVersionCache", "siteNode_" + siteNodeVO.getId());
					}
					catch (Exception e) 
					{
						logger.warn("Error in JDOCallback:" + e.getMessage(), e);
					}
				}
				else
				{
					SiteNodeVersionImpl siteNodeVersion = (SiteNodeVersionImpl)object;
					
					clearCache(MediumSiteNodeVersionImpl.class, siteNodeVersion.getId());
					try
					{
						SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVersion.getValueObject().getSiteNodeId(), true);
						if(siteNodeVO != null)
						{	
							CacheController.clearCacheForGroup("siteNodeCacheWithLatestVersion", "siteNode_" + siteNodeVO.getId());
							CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNodeVO.getId());
							CacheController.clearCacheForGroup("childPagesCache", "siteNode_" + siteNodeVO.getId());
							if(siteNodeVO.getParentSiteNodeId() != null)
							{
								CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNodeVO.getParentSiteNodeId());					
								CacheController.clearCacheForGroup("childPagesCache", "siteNode_" + siteNodeVO.getParentSiteNodeId());					
							}
							
							CacheController.clearCacheForGroup("latestSiteNodeVersionCache", "siteNode_" + siteNodeVO.getId());
						}
					}
					catch (Exception e) 
					{
						logger.warn("Error in JDOCallback:" + e.getMessage(), e);
					}
					
				}
				

				//CacheController.clearCache("childSiteNodesCache");
				CacheController.clearCache("parentSiteNodeCache");
				//SiteNodeVersionImpl siteNodeVersion = (SiteNodeVersionImpl)object;
				//CacheController.clearCacheForGroup("latestSiteNodeVersionCache", "siteNode_" + siteNodeVersion.getOwningSiteNode().getId());
				//CacheController.clearCacheForGroup("latestSiteNodeVersionCache", "siteNode_" + siteNodeVersion.getOwningSiteNode().getId());
			}
			else if(object.getClass().getName().equals(WorkflowDefinitionImpl.class.getName()))
			{
				CacheController.clearCache("workflowCache");
			}
			else if(object.getClass().getName().equals(SystemUserImpl.class.getName()))
			{
				clearCache(SmallSystemUserImpl.class);
				CacheController.clearCache("principalCache");
				CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
	    		
	    		LuceneUsersController.getController().reset();
			}
			else if(object.getClass().getName().equals(GroupImpl.class.getName()))
			{
				clearCache(SmallGroupImpl.class);
				CacheController.clearCache("groupListCache");
				CacheController.clearCache("groupVOListCache");
				CacheController.clearCache("principalPropertyValueCache");
	    		
	    		LuceneUsersController.getController().reset();
			}
			else if(object.getClass().getName().equals(RoleImpl.class.getName()))
			{
				clearCache(SmallRoleImpl.class);
				CacheController.clearCache("roleListCache");
				CacheController.clearCache("roleVOListCache");
				CacheController.clearCache("principalPropertyValueCache");
	    		
	    		LuceneUsersController.getController().reset();
			}
			else if(object.getClass().getName().equals(SystemUserGroupImpl.class.getName()))
			{
				//clearCache(SystemUserImpl.class);
				//clearCache(GroupImpl.class);
				CacheController.clearCache("groupListCache");
				CacheController.clearCache("groupVOListCache");
				
				CacheController.clearCache("principalCache");
				CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("rolePropertiesCache");
				CacheController.clearCache("groupPropertiesCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
				
				new Thread(new Runnable() { public void run() {try {CmsSessionContextListener.reCacheSessionPrincipal(); LuceneUsersController.getController().reset();} catch (Exception e) {}}}).start();
			}
			else if(object.getClass().getName().equals(SystemUserRoleImpl.class.getName()))
			{
				//clearCache(SystemUserImpl.class);
				//clearCache(RoleImpl.class);
				CacheController.clearCache("roleListCache");
				CacheController.clearCache("roleVOListCache");

				CacheController.clearCache("principalCache");
				CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("rolePropertiesCache");
				CacheController.clearCache("groupPropertiesCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
			
				new Thread(new Runnable() { public void run() {try {CmsSessionContextListener.reCacheSessionPrincipal(); LuceneUsersController.getController().reset();} catch (Exception e) {}}}).start();
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

	public String getNotificationClassNameAndAddExtraInfo(Object object, boolean addMetaData) 
	{
		String storedClassName = object.getClass().getName();

		if(storedClassName.equals("org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl"))
		{
			try
			{
				SiteNodeImpl impl = ((SiteNodeImpl)object);
			    Map<String,String> extraInfoMap = new HashMap<String,String>();
				extraInfoMap.put("siteNodeId", ""+impl.getId());
			    extraInfoMap.put("parentSiteNodeId", ""+impl.getValueObject().getParentSiteNodeId());
			    extraInfoMap.put("repositoryId", ""+impl.getValueObject().getRepositoryId());
			    CacheController.setExtraInfo(SiteNodeImpl.class.getName(), impl.getId().toString(), extraInfoMap);
			}
			catch (Exception e) 
			{
				logger.error("Error setting extra info:" + e.getMessage(), e);
			}
		}		
		else if(storedClassName.equals("org.infoglue.cms.entities.structure.impl.simple.MediumSiteNodeVersionImpl"))
		{
			storedClassName = "org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl";
		}
		else if(storedClassName.equals("org.infoglue.cms.entities.content.impl.simple.MediumContentImpl") || storedClassName.equals("org.infoglue.cms.entities.content.impl.simple.SmallContentImpl"))
		{
			/*
			try
			{
				MediumContentVersionImpl impl = ((MediumContentVersionImpl)object);
				ContentVO contentVO = ContentController.getContentController().getContentVOWithId(impl.getId());
	    	    Map<String,String> extraInfoMap = new HashMap<String,String>();
	 			extraInfoMap.put("contentId", ""+contentVO.getId());
	    	    extraInfoMap.put("parentContentId", ""+contentVO.getParentContentId());
	    	    extraInfoMap.put("repositoryId", ""+contentVO.getRepositoryId());
	    	    CacheController.setExtraInfo(ContentVersionImpl.class.getName(), impl.getId().toString(), extraInfoMap);
			}
			catch (Exception e) 
			{
				logger.error("Error setting extra info:" + e.getMessage(), e);
			}
			*/
			storedClassName = "org.infoglue.cms.entities.content.impl.simple.ContentImpl";
		}
		else if(storedClassName.equals("org.infoglue.cms.entities.content.impl.simple.MediumContentVersionImpl"))
		{
			storedClassName = "org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl";
		}
		else if(storedClassName.equals("org.infoglue.cms.entities.structure.impl.simple.SmallQualifyerImpl"))
		{
			storedClassName = "org.infoglue.cms.entities.structure.impl.simple.QualifyerImpl";
		}

		return storedClassName;
	}

	private synchronized void clearCache(Class c) throws Exception
	{
		if(c.getName().contains(".SiteNodeVersionImpl") || 
		   c.getName().contains(".MediumSiteNodeVersionImpl") || 
		   c.getName().contains(".SiteNodeImpl") || 
		   c.getName().contains(".SmallSiteNodeImpl") || 
		   c.getName().contains(".LanguageImpl") || 
		   c.getName().contains(".RepositoryImpl") || 
		   c.getName().contains(".RepositoryLanguageImpl") || 
		   c.getName().contains(".DigitalAssetImpl") || 
		   c.getName().contains(".MediumDigitalAssetImpl") || 
		   c.getName().contains(".ContentVersionImpl") || 
		   c.getName().contains(".MediumContentVersionImpl") || 
		   c.getName().contains(".AccessRightRoleImpl") || 
		   c.getName().contains(".AccessRightGroupImpl") || 
		   c.getName().contains(".AccessRightUserImpl") || 
		   c.getName().contains(".RegistryImpl"))
		{
			logger.info("Skipping " + c.getName() + " as they have no castor cache");
			return;
		}

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

	private synchronized void clearCache(Class c, Object id) throws Exception
	{
		if(c.getName().contains(".SiteNodeVersionImpl") || 
		   c.getName().contains(".MediumSiteNodeVersionImpl") || 
		   c.getName().contains(".SiteNodeImpl") || 
		   c.getName().contains(".SmallSiteNodeImpl") || 
		   c.getName().contains(".LanguageImpl") || 
		   c.getName().contains(".RepositoryImpl") || 
		   c.getName().contains(".RepositoryLanguageImpl") || 
		   c.getName().contains(".DigitalAssetImpl") || 
		   c.getName().contains(".MediumDigitalAssetImpl") || 
		   c.getName().contains(".ContentVersionImpl") || 
		   c.getName().contains(".MediumContentVersionImpl") || 
		   c.getName().contains(".AccessRightRoleImpl") || 
		   c.getName().contains(".AccessRightGroupImpl") || 
		   c.getName().contains(".AccessRightUserImpl") || 
		   c.getName().contains(".RegistryImpl"))
		{
			logger.info("Skipping " + c.getName() + " as they have no castor cache");
			return;
		}

		Database db = CastorDatabaseService.getDatabase();

		try
		{
			CacheManager manager = db.getCacheManager();
			manager.expireCache(c, id);
			
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
    	/*
    	if(object.getClass().getName().indexOf(".ContentVersionImpl") > -1 || object.getClass().getName().indexOf(".ContentImpl") > -1 || object.getClass().getName().indexOf(".SiteNodeVersionImpl") > -1 || object.getClass().getName().indexOf(".SiteNodeImpl") > -1)
    	{
    		logger.warn("created " + object.getClass().getName());
    		if(logger.isDebugEnabled())
    			Thread.dumpStack();
    	}
    	*/

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
    		SubscriptionFilterImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
    		PageDeliveryMetaDataImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
    		PageDeliveryMetaDataEntityImpl.class.getName().indexOf(object.getClass().getName()) == -1)
	    {
    	    String userName = "SYSTEM";
			try
			{
				InfoGluePrincipal principal = InfoGlueAbstractAction.getSessionInfoGluePrincipal();
				if(principal != null && principal.getName() != null)
					userName = principal.getName();
		    } 
			catch (NoClassDefFoundError e){}
			
    		String storedClassName = getNotificationClassNameAndAddExtraInfo(object, false);
    	    NotificationMessage notificationMessage = new NotificationMessage("CMSJDOCallback", storedClassName, userName, NotificationMessage.TRANS_CREATE, getObjectIdentity(object), getObjectName(object), CacheController.getExtraInfo(storedClassName, getObjectIdentity(object).toString()));
    	    ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);

			boolean skipSystemNotificationUpdate = false;
			if(disableACLNotificationsToLive && (object.getClass().getName().indexOf("SystemUserImpl") > -1 || object.getClass().getName().indexOf("SystemUserGroupImpl") > -1 || object.getClass().getName().indexOf("SystemUserRoleImpl") > -1))
				skipSystemNotificationUpdate = true;

    	    if(object.getClass().getName().indexOf("org.infoglue.cms.entities.management") > -1 && 
    	    		!object.getClass().getName().equals(RegistryImpl.class.getName()) &&
    	    		object.getClass().getName().indexOf("AccessRight") == -1 && 
					!skipSystemNotificationUpdate)
    	    {
			    RemoteCacheUpdater.getSystemNotificationMessages().add(notificationMessage);
    	    }
    	    
			if(object.getClass().getName().equals(RepositoryImpl.class.getName()))
			{
				CacheController.clearCache("repositoryCache");
			}
			else if(object.getClass().getName().equals(SmallRepositoryImpl.class.getName()))
			{
				CacheController.clearCache("repositoryCache");
			}
			else if(object.getClass().getName().equals(CategoryImpl.class.getName()) || object.getClass().getName().equals(ContentTypeDefinitionImpl.class.getName()))
			{
				CacheController.clearCache("contentTypeDefinitionCache");
				CacheController.clearCache("categoriesCache");
			}
			else if(object.getClass().getName().equals(InterceptionPointImpl.class.getName()))
			{
				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
			}
			else if(object.getClass().getName().equals(InterceptorImpl.class.getName()))
			{
				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
			}
			else if(object.getClass().getName().equals(AccessRightImpl.class.getName()) || object.getClass().getName().equals(AccessRightRoleImpl.class.getName()) || object.getClass().getName().equals(AccessRightGroupImpl.class.getName()) || object.getClass().getName().equals(AccessRightUserImpl.class.getName()))
			{
				try
				{
					AccessRightImpl ar = (AccessRightImpl)object;
					String acKey = "" + ar.getValueObject().getInterceptionPointId();
					if(ar.getValueObject().getParameters() != null && !ar.getValueObject().getParameters().equals(""))
						acKey = "" + ar.getValueObject().getInterceptionPointId() + "_" + ar.getValueObject().getParameters();
					CacheController.clearUserAccessCache(acKey);						  
				}
				catch (Exception e) 
				{
					logger.error("Failed to clear user cache:" + e.getMessage());
				}

				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
			}
			else if(object.getClass().getName().equals(ContentImpl.class.getName()))
			{
				try
				{
					ContentImpl content = (ContentImpl)object;
					CacheController.clearCacheForGroup("childContentCache", "content_" + content.getId());
					if(content.getValueObject().getParentContentId() != null)
					{
						CacheController.clearCacheForGroup("childContentCache", "content_" + content.getValueObject().getParentContentId());
						if(ContentController.getContentController().getDoesContentExist(content.getId()))
						{
							ContentVO parentContentVO = ContentController.getContentController().getContentVOWithId(content.getValueObject().getParentContentId(), false);
							if(parentContentVO.getParentContentId() != null)
							{
								CacheController.clearCacheForGroup("childContentCache", "content_" + parentContentVO.getParentContentId());
							}
						}
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
			else if(object.getClass().getName().equals(MediumContentImpl.class.getName()))
			{
				try
				{
					MediumContentImpl content = (MediumContentImpl)object;
					CacheController.clearCacheForGroup("childContentCache", "content_" + content.getId());
					if(content.getValueObject().getParentContentId() != null)
					{
						CacheController.clearCacheForGroup("childContentCache", "content_" + content.getValueObject().getParentContentId());
						ContentVO parentContentVO = ContentController.getContentController().getContentVOWithId(content.getValueObject().getParentContentId(), false);
						if(parentContentVO.getParentContentId() != null)
						{
							CacheController.clearCacheForGroup("childContentCache", "content_" + parentContentVO.getParentContentId());
						}
					}
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage(), e);
				}
				
				clearCache(SmallContentImpl.class);
				clearCache(SmallishContentImpl.class);
				clearCache(ContentImpl.class);
			}
			else if(object.getClass().getName().equals(SmallContentImpl.class.getName()))
			{
				try
				{
					SmallContentImpl content = (SmallContentImpl)object;
					CacheController.clearCacheForGroup("childContentCache", "content_" + content.getId());
					if(content.getValueObject().getParentContentId() != null)
					{
						CacheController.clearCacheForGroup("childContentCache", "content_" + content.getValueObject().getParentContentId());
						ContentVO parentContentVO = ContentController.getContentController().getContentVOWithId(content.getValueObject().getParentContentId(), false);
						if(parentContentVO.getParentContentId() != null)
						{
							CacheController.clearCacheForGroup("childContentCache", "content_" + parentContentVO.getParentContentId());
						}
					}
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage(), e);
				}
				
				clearCache(MediumContentImpl.class);
				clearCache(SmallishContentImpl.class);
				clearCache(ContentImpl.class);
			}
			else if(object.getClass().getName().equals(ContentVersionImpl.class.getName()))
			{
				ContentVersionImpl contentVersion = (ContentVersionImpl)object;
				
				CacheController.clearCacheForGroup("contentVersionCache", "content_" + contentVersion.getContentId());

				clearCache(MediumContentVersionImpl.class);
				clearCache(SmallContentVersionImpl.class);
				clearCache(SmallestContentVersionImpl.class);
			}
			else if(object.getClass().getName().equals(MediumContentVersionImpl.class.getName()))
			{
				MediumContentVersionImpl contentVersion = (MediumContentVersionImpl)object;

				CacheController.clearCacheForGroup("contentVersionCache", "content_" + contentVersion.getContentId());

				clearCache(ContentVersionImpl.class);
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
			else if(object.getClass().getName().equals(SiteNodeVersionImpl.class.getName()) || object.getClass().getName().equals(MediumSiteNodeVersionImpl.class.getName()))
			{
				//CacheController.clearCache("childSiteNodesCache");
				CacheController.clearCache("parentSiteNodeCache");
				if(object.getClass().getName().equals(MediumSiteNodeVersionImpl.class.getName()))
				{
					MediumSiteNodeVersionImpl siteNodeVersion = (MediumSiteNodeVersionImpl)object;
					CacheController.clearCacheForGroup("latestSiteNodeVersionCache", "siteNode_" + siteNodeVersion.getValueObject().getSiteNodeId());
					CacheController.clearCacheForGroup("siteNodeCacheWithLatestVersion", "siteNode_" + siteNodeVersion.getValueObject().getSiteNodeId());
				}
				else
				{
					SiteNodeVersion siteNodeVersion = (SiteNodeVersion)object;
					Integer siteNodeId = siteNodeVersion.getValueObject().getSiteNodeId();
					if(siteNodeId == null) 
						siteNodeId = siteNodeVersion.getOwningSiteNode().getId();
					
					CacheController.clearCacheForGroup("latestSiteNodeVersionCache", "siteNode_" + siteNodeId);
					CacheController.clearCacheForGroup("siteNodeCacheWithLatestVersion", "siteNode_" + siteNodeId);
				}
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
				clearCache(SmallSystemUserImpl.class);
				CacheController.clearCache("principalCache");
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
	    		
	    		LuceneUsersController.getController().reset();
			}
			else if(object.getClass().getName().equals(GroupImpl.class.getName()))
			{
				clearCache(SmallGroupImpl.class);
				CacheController.clearCache("groupListCache");
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
	    		
	    		LuceneUsersController.getController().reset();
			}
			else if(object.getClass().getName().equals(RoleImpl.class.getName()))
			{
				clearCache(SmallRoleImpl.class);
				CacheController.clearCache("roleListCache");
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
	    		
	    		LuceneUsersController.getController().reset();
			}
			else if(object.getClass().getName().equals(SystemUserGroupImpl.class.getName()))
			{
				//clearCache(SystemUserImpl.class);
				//clearCache(GroupImpl.class);
				CacheController.clearCache("principalCache");
				CacheController.clearCache("groupListCache");
				
				CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("rolePropertiesCache");
				CacheController.clearCache("groupPropertiesCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
				
				new Thread(new Runnable() { public void run() {try {CmsSessionContextListener.reCacheSessionPrincipal();LuceneUsersController.getController().reset();} catch (Exception e) {}}}).start();
			}
			else if(object.getClass().getName().equals(SystemUserRoleImpl.class.getName()))
			{
				//clearCache(SystemUserImpl.class);
				//clearCache(RoleImpl.class);
				CacheController.clearCache("principalCache");
				CacheController.clearCache("groupListCache");

				CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("rolePropertiesCache");
				CacheController.clearCache("groupPropertiesCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
				
				new Thread(new Runnable() { public void run() {try {CmsSessionContextListener.reCacheSessionPrincipal();LuceneUsersController.getController().reset();} catch (Exception e) {}}}).start();
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
			else if(object.getClass().getName().equals(MediumDigitalAssetImpl.class.getName()))
			{
				CacheController.clearCache("digitalAssetCache");
				clearCache(SmallDigitalAssetImpl.class);
				clearCache(DigitalAssetImpl.class);
			}

			//logger.error("created end...:" + object);
    	}
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
    		SubscriptionFilterImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
    		PageDeliveryMetaDataImpl.class.getName().indexOf(object.getClass().getName()) == -1 &&
    		PageDeliveryMetaDataEntityImpl.class.getName().indexOf(object.getClass().getName()) == -1)
	    {
       	    String userName = "SYSTEM";
			try
			{	
				InfoGluePrincipal principal = InfoGlueAbstractAction.getSessionInfoGluePrincipal();
				if(principal != null && principal.getName() != null)
					userName = principal.getName();
			} 
			catch (NoClassDefFoundError e){}

    		String storedClassName = getNotificationClassNameAndAddExtraInfo(object, true);
		    NotificationMessage notificationMessage = new NotificationMessage("CMSJDOCallback", storedClassName, userName, NotificationMessage.TRANS_DELETE, getObjectIdentity(object), getObjectName(object), CacheController.getExtraInfo(storedClassName, getObjectIdentity(object).toString()));
		    ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);

			boolean skipSystemNotificationUpdate = false;
			if(disableACLNotificationsToLive && (object.getClass().getName().indexOf("SystemUserImpl") > -1 || object.getClass().getName().indexOf("SystemUserGroupImpl") > -1 || object.getClass().getName().indexOf("SystemUserRoleImpl") > -1))
				skipSystemNotificationUpdate = true;

			if(object.getClass().getName().indexOf("org.infoglue.cms.entities.management") > -1 && 
					!object.getClass().getName().equals(RegistryImpl.class.getName()) && 
					object.getClass().getName().indexOf("AccessRight") == -1 && 
					!skipSystemNotificationUpdate)
			{
			    RemoteCacheUpdater.getSystemNotificationMessages().add(notificationMessage);
			}
			
			if(object.getClass().getName().equals(RepositoryImpl.class.getName()))
			{
				CacheController.clearCache("repositoryCache");
				CacheController.clearCache("repositoryRootNodesCache");
			}
			else if(object.getClass().getName().equals(SmallRepositoryImpl.class.getName()))
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
	    		CacheController.clearCache("userAccessCache");
			}
			else if(object.getClass().getName().equals(InterceptorImpl.class.getName()))
			{
				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
			}
			else if(object.getClass().getName().equals(AccessRightImpl.class.getName()) || object.getClass().getName().equals(AccessRightRoleImpl.class.getName()) || object.getClass().getName().equals(AccessRightGroupImpl.class.getName()) || object.getClass().getName().equals(AccessRightUserImpl.class.getName()))
			{
				try
				{
					AccessRightImpl ar = (AccessRightImpl)object;
					String acKey = "" + ar.getValueObject().getInterceptionPointId();
					if(ar.getValueObject().getParameters() != null && !ar.getValueObject().getParameters().equals(""))
						acKey = "" + ar.getValueObject().getInterceptionPointId() + "_" + ar.getValueObject().getParameters();
					CacheController.clearUserAccessCache(acKey);						  
				}
				catch (Exception e) 
				{
					logger.error("Failed to clear user cache:" + e.getMessage());
				}

				CacheController.clearCache("interceptionPointCache");
				CacheController.clearCache("interceptorsCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
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
					CacheController.clearCacheForGroup("contentCache", "content_" + content.getId());
					CacheController.clearCacheForGroup("childContentCache", "content_" + content.getId());
					if(content.getParentContent() != null)
						CacheController.clearCacheForGroup("childContentCache", "content_" + content.getParentContent().getId());
					else
						CacheController.clearCache("rootContentCache", "root_" + content.getRepositoryId());
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
				
				ContentVersionImpl contentVersion = (ContentVersionImpl)object;
				if(contentVersion.getOwningContent().getContentTypeDefinition() == null || (
				   contentVersion.getOwningContent().getContentTypeDefinition().getName().equalsIgnoreCase("HTMLTemplate") ||
				   contentVersion.getOwningContent().getContentTypeDefinition().getName().equalsIgnoreCase("PagePartTemplate")))
				{
					ComponentController.getController().reIndexComponentContentsDelayed(contentVersion.getOwningContent().getId());
				}
				CacheController.clearCacheForGroup("contentVersionCache", "content_" + contentVersion.getOwningContent().getId());

				clearCache(MediumContentVersionImpl.class);
				clearCache(SmallContentVersionImpl.class);
				clearCache(SmallestContentVersionImpl.class);
				
				RegistryController.getController().clearRegistryForReferencingEntityNameThreaded(ContentVersion.class.getName(), getObjectIdentity(object).toString());
				//RegistryController.getController().clearRegistryForReferencingEntityName(ContentVersion.class.getName(), getObjectIdentity(object).toString());
			}
			else if(object.getClass().getName().equals(MediumContentVersionImpl.class.getName()))
			{
				MediumContentVersionImpl contentVersion = (MediumContentVersionImpl)object;
				try
				{
					ContentTypeDefinitionVO htmlTemplateMetaInfoCTDVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("HTMLTemplate");
					ContentTypeDefinitionVO pagePartTemplateMetaInfoCTDVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("PagePartTemplate");
					ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersion.getContentId(), true);
					if(contentVO.getContentTypeDefinitionId() == null || (
					   contentVO.getContentTypeDefinitionId().equals(htmlTemplateMetaInfoCTDVO.getId()) ||
					   (pagePartTemplateMetaInfoCTDVO != null && contentVO.getContentTypeDefinitionId().equals(pagePartTemplateMetaInfoCTDVO.getId()))))
					{
						ComponentController.getController().reIndexComponentContentsDelayed(contentVersion.getContentId());
					}
					CacheController.clearCacheForGroup("contentVersionCache", "content_" + contentVersion.getContentId());
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage(), e);
				}

				clearCache(ContentVersionImpl.class);
				clearCache(SmallContentVersionImpl.class);
				clearCache(SmallestContentVersionImpl.class);
				clearCache(ContentImpl.class, contentVersion.getValueObject().getContentId());

				RegistryController.getController().clearRegistryForReferencingEntityNameThreaded(ContentVersion.class.getName(), getObjectIdentity(object).toString());
				//RegistryController.getController().clearRegistryForReferencingEntityName(ContentVersion.class.getName(), getObjectIdentity(object).toString());
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
			else if(object.getClass().getName().equals(MediumDigitalAssetImpl.class.getName()))
			{
				CacheController.clearCache("digitalAssetCache");
				clearCache(SmallDigitalAssetImpl.class);
				clearCache(DigitalAssetImpl.class);
				//logger.info("We should delete all images with digitalAssetId " + getObjectIdentity(object));
				DigitalAssetController.deleteCachedDigitalAssets((Integer)getObjectIdentity(object));
			}
			else if(object.getClass().getName().equals(SiteNodeImpl.class.getName()))
			{
			    RegistryController.getController().clearRegistryForReferencedEntity(SiteNode.class.getName(), getObjectIdentity(object).toString());
				RegistryController.getController().clearRegistryForReferencingEntityCompletingName(SiteNode.class.getName(), getObjectIdentity(object).toString());
				clearCache(SmallSiteNodeImpl.class);
				
				try
				{
					SiteNodeImpl siteNode = (SiteNodeImpl)object;
					CacheController.clearCache("siteNodeCache", "" + siteNode.getId());
					CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNode.getId());
					CacheController.clearCacheForGroup("childPagesCache", "siteNode_" + siteNode.getId());
					if(siteNode.getParentSiteNode() != null)
					{
						CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNode.getParentSiteNode().getId());					
						CacheController.clearCacheForGroup("childPagesCache", "siteNode_" + siteNode.getParentSiteNode().getId());					
					}
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage(), e);
				}

				//CacheController.clearCache("childSiteNodesCache");
				CacheController.clearCache("parentSiteNodeCache");
				CacheController.clearCache("repositoryRootNodesCache");
				CacheController.clearCacheForGroup("latestSiteNodeVersionCache", "siteNode_" + (Integer)getObjectIdentity(object));
				CacheController.clearCacheForGroup("siteNodeCache","" + (Integer)getObjectIdentity(object));

			}
			else if(object.getClass().getName().equals(SmallSiteNodeImpl.class.getName()))
			{
			    RegistryController.getController().clearRegistryForReferencedEntity(SiteNode.class.getName(), getObjectIdentity(object).toString());
				RegistryController.getController().clearRegistryForReferencingEntityCompletingName(SiteNode.class.getName(), getObjectIdentity(object).toString());
				clearCache(SiteNodeImpl.class);
				
				try
				{
					SmallSiteNodeImpl siteNode = (SmallSiteNodeImpl)object;
					CacheController.clearCache("siteNodeCache", "" + siteNode.getId());
					CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNode.getId());
					CacheController.clearCacheForGroup("childPagesCache", "siteNode_" + siteNode.getId());
					if(siteNode.getParentSiteNodeId() != null)
					{
						CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNode.getParentSiteNodeId());					
						CacheController.clearCacheForGroup("childPagesCache", "siteNode_" + siteNode.getParentSiteNodeId());					
					}
				}
				catch (Exception e) 
				{
					logger.warn("Error in JDOCallback:" + e.getMessage(), e);
				}

				//CacheController.clearCache("childSiteNodesCache");
				CacheController.clearCache("parentSiteNodeCache");
				CacheController.clearCache("repositoryRootNodesCache");
				CacheController.clearCacheForGroup("latestSiteNodeVersionCache", "siteNode_" + (Integer)getObjectIdentity(object));
				CacheController.clearCacheForGroup("siteNodeCache","" + (Integer)getObjectIdentity(object));

			}
			else if(object.getClass().getName().equals(SiteNodeVersionImpl.class.getName()) || object.getClass().getName().equals(MediumSiteNodeVersionImpl.class.getName()))
			{
				Timer t = new Timer();
				
				clearCache(SmallSiteNodeVersionImpl.class);
				if(object.getClass().getName().equals(MediumSiteNodeVersionImpl.class.getName()))
				{
					clearCache(SiteNodeVersionImpl.class);
					try
					{
						MediumSiteNodeVersionImpl siteNodeVersion = (MediumSiteNodeVersionImpl)object;
						CacheController.clearCacheForGroup("siteNodeCacheWithLatestVersion", "siteNode_" + (Integer)getObjectIdentity(object));
						CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNodeVersion.getValueObject().getSiteNodeId());
						CacheController.clearCacheForGroup("childPagesCache", "siteNode_" + siteNodeVersion.getValueObject().getSiteNodeId());
						CacheController.clearCacheForGroup("latestSiteNodeVersionCache", "siteNode_" + siteNodeVersion.getValueObject().getSiteNodeId());
						SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVersion.getValueObject().getSiteNodeId(), true);
						if(siteNodeVO.getParentSiteNodeId() != null)
						{
							CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNodeVO.getParentSiteNodeId());					
							CacheController.clearCacheForGroup("childPagesCache", "siteNode_" + siteNodeVO.getParentSiteNodeId());					
						}
					}
					catch (Exception e) 
					{
						logger.warn("Error in JDOCallback:" + e.getMessage(), e);
					}
				}
				else
				{
					clearCache(MediumSiteNodeVersionImpl.class);
					try
					{
						SiteNodeVersionImpl siteNodeVersion = (SiteNodeVersionImpl)object;
						Integer siteNodeId = siteNodeVersion.getSiteNodeId();
						if(siteNodeId == null && siteNodeVersion.getOwningSiteNode() != null) 
							siteNodeId = siteNodeVersion.getOwningSiteNode().getId();

						CacheController.clearCacheForGroup("siteNodeCacheWithLatestVersion", "siteNode_" + siteNodeId);
						CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNodeId);
						CacheController.clearCacheForGroup("childPagesCache", "siteNode_" + siteNodeId);
						CacheController.clearCacheForGroup("latestSiteNodeVersionCache", "siteNode_" + siteNodeId);
						SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVersion.getValueObject().getSiteNodeId(), true);
						if(siteNodeVO.getParentSiteNodeId() != null)
						{
							CacheController.clearCacheForGroup("childSiteNodesCache", "siteNode_" + siteNodeVO.getParentSiteNodeId());
							CacheController.clearCacheForGroup("childPagesCache", "siteNode_" + siteNodeVO.getParentSiteNodeId());
						}
					}
					catch (Exception e) 
					{
						logger.warn("Error in JDOCallback:" + e.getMessage(), e);
					}
				}
				
				
				//CacheController.clearCache("childSiteNodesCache");
				CacheController.clearCache("parentSiteNodeCache");

				RegistryController.getController().clearRegistryForReferencingEntityNameThreaded(SiteNodeVersion.class.getName(), getObjectIdentity(object).toString());
				//RegistryController.getController().clearRegistryForReferencingEntityName(SiteNodeVersion.class.getName(), getObjectIdentity(object).toString());
			}
			else if(object.getClass().getName().equals(WorkflowDefinitionImpl.class.getName()))
			{
				CacheController.clearCache("workflowCache");
			}
			else if(object.getClass().getName().equals(SystemUserImpl.class.getName()))
			{
				clearCache(SmallSystemUserImpl.class);
				CacheController.clearCache("principalCache");
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
	    		LuceneUsersController.getController().reset();
			}
			else if(object.getClass().getName().equals(GroupImpl.class.getName()))
			{
				clearCache(SmallGroupImpl.class);
				CacheController.clearCache("groupListCache");
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
	    		LuceneUsersController.getController().reset();
			}
			else if(object.getClass().getName().equals(RoleImpl.class.getName()))
			{
				clearCache(SmallRoleImpl.class);
				CacheController.clearCache("roleListCache");
			    CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");
	    		LuceneUsersController.getController().reset();
			}
			else if(object.getClass().getName().equals(SystemUserGroupImpl.class.getName()))
			{
				CacheController.clearCache("principalCache");
				CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("rolePropertiesCache");
				CacheController.clearCache("groupPropertiesCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");

	    		new Thread(new Runnable() { public void run() {try {CmsSessionContextListener.reCacheSessionPrincipal();LuceneUsersController.getController().reset();} catch (Exception e) {}}}).start();
			}
			else if(object.getClass().getName().equals(SystemUserRoleImpl.class.getName()))
			{
				CacheController.clearCache("principalCache");
				CacheController.clearCache("principalPropertyValueCache");
				CacheController.clearCache("rolePropertiesCache");
				CacheController.clearCache("groupPropertiesCache");
				CacheController.clearCache("authorizationCache");
				CacheController.clearCache("personalAuthorizationCache");
	    		CacheController.clearCache("userAccessCache");

	    		new Thread(new Runnable() { public void run() {try {CmsSessionContextListener.reCacheSessionPrincipal();LuceneUsersController.getController().reset();} catch (Exception e) {}}}).start();
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

	    	NotificationMessage notificationMessage = new NotificationMessage("CmsJDOCallback", object.getClass().getName(), userName, NotificationMessage.TRANS_UPDATE, getObjectIdentity(object), object.toString());
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

	private String getObjectName(Object entity) throws Bug
	{
		String objectName = null;
		
		try
		{
			if(entity instanceof ContentImpl)
				objectName = ((Content)entity).getName();
			else if(entity instanceof ContentVersionImpl)
				objectName = ((ContentVersion)entity).getOwningContent().getName() + " (" + ((ContentVersion)entity).getLanguage().getName() + " version)";
			else if(entity instanceof SiteNodeImpl)
				objectName = ((SiteNode)entity).getName();
			else if(entity instanceof SiteNodeVersionImpl)
				objectName = ((SiteNodeVersion)entity).getOwningSiteNode().getName();
			else
				objectName = entity.toString();
		}
		catch (Exception e) 
		{
			objectName = ""+entity;
		}
		
		return objectName;
	}

	//@Override
	public void modifying(Object arg0) throws Exception 
	{
	}
}

