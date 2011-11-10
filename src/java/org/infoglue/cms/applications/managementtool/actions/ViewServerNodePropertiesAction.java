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

package org.infoglue.cms.applications.managementtool.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infoglue.cms.applications.common.actions.InfoGluePropertiesAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.management.ServerNodeVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.cms.util.RemoteCacheUpdater;
import org.infoglue.deliver.util.CacheController;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
 * This class implements the action class for viewServerNodeProperties.
 * The use-case lets the user see all extra-properties for a serverNode
 * 
 * @author Mattias Bogeblad  
 */

public class ViewServerNodePropertiesAction extends InfoGluePropertiesAbstractAction
{ 
	private static final long serialVersionUID = 1L;

	private ServerNodeVO serverNodeVO 			= new ServerNodeVO();
	private PropertySet propertySet				= null; 
	private List serverNodeVOList				= new ArrayList();
	private String key 							= null;
	private String activeDivId					= "commonSettings";
	
    public ViewServerNodePropertiesAction()
    {
    }
        
    protected void initialize(Integer serverNodeId) throws Exception
    {
        if(serverNodeId != null && serverNodeId.intValue() > -1)
            this.serverNodeVO = ServerNodeController.getController().getServerNodeVOWithId(serverNodeId);
        else
        {
        	this.serverNodeVO.setName("Default");
        }
        
        Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    this.propertySet = PropertySetManager.getInstance("jdbc", args);
    } 

    /**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doExecute() throws Exception
    {
		this.serverNodeVOList = ServerNodeController.getController().getServerNodeVOList();
        this.initialize(getServerNodeId());

        return "success";
    }
    
    private void populate(PropertySet ps, String key)
    {
        String value = this.getRequest().getParameter(key);
	    if(value != null && !value.equals(""))
	        ps.setString("serverNode_" + this.getServerNodeId() + "_" + key, value);
    }

    private void populateData(PropertySet ps, String key)
    {
        try
        {
            String value = this.getRequest().getParameter(key);
    	    if(value != null && !value.equals(""))
    	        ps.setData("serverNode_" + this.getServerNodeId() + "_" + key, value.getBytes("utf-8"));            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doSave() throws Exception
    {
		validateSecurityCode();

        Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    populate(ps, "isPageCacheOn");
	    populate(ps, "useSelectivePageCacheUpdate");
	    populate(ps, "expireCacheAutomatically");
	    populate(ps, "cacheExpireInterval");
	    populate(ps, "deliverRequestTimeout");
	    populate(ps, "liveDeliverRequestTimeout");
	    populate(ps, "killLiveRequestWhichTimedout");
	    populate(ps, "useHighLoadLimiter");
	    populate(ps, "maxActiveRequests");
	    populate(ps, "maxRequestTime");
	    populate(ps, "session.timeout");
	    populate(ps, "compressPageCache");
	    populate(ps, "compressPageResponse");
	    populate(ps, "disableDecoratedFinalRendering");
	    populate(ps, "siteNodesToRecacheOnPublishing");
	    populate(ps, "recachePublishingMethod");
	    populate(ps, "recacheUrl");
	    populate(ps, "useUpdateSecurity");
	    populate(ps, "allowXForwardedIPCheck");

	    populate(ps, "allowedAdminIP");
	    String allowedAdminIP = this.getRequest().getParameter("allowedAdminIP");
	    if(allowedAdminIP != null && !allowedAdminIP.equals(""))
	        ServerNodeController.getController().setAllowedAdminIP(allowedAdminIP);

	    populate(ps, "pageKey");
	    populate(ps, "componentKey");
	    populateData(ps, "cacheSettings");
	    populateData(ps, "extraPublicationPersistentCacheNames");
	    populate(ps, "cmsBaseUrl");
	    populate(ps, "cmsFullBaseUrl");
	    populate(ps, "componentEditorUrl");
	    populate(ps, "componentRendererUrl");
	    populate(ps, "componentRendererAction");
	    populate(ps, "editOnSiteUrl");
	    populate(ps, "useFreeMarker");
	    populate(ps, "webServerAddress");
	    populate(ps, "applicationBaseAction");
	    populate(ps, "digitalAssetBaseUrl");
	    populate(ps, "imagesBaseUrl");
	    populate(ps, "digitalAssetPath");
	    populate(ps, "urlFormatting");
	    populate(ps, "enableNiceURI");
	    populate(ps, "enableNiceURIInWorking");
	    populate(ps, "enableNiceURIForLanguage");
	    populate(ps, "enableDiskAssets");
	    populate(ps, "disableAssetDeletionInWorkThread");
	    populate(ps, "disableAssetDeletionInLiveThread");
	    populate(ps, "niceURIEncoding");
	    populate(ps, "niceURIAttributeName");
	    populateData(ps, "niceURICharacterReplacingMapping");
	    populate(ps, "niceURIUseLowerCase");
	    populate(ps, "niceURIDefaultReplacementCharacter");
	    populate(ps, "niceURIDisableNiceURIForContent");
	    populate(ps, "niceURIDefaultReplacementCharacterForContent");
	    populate(ps, "duplicateAssetsBetweenVersions");
	    populate(ps, "requestArgumentDelimiter");
	    populate(ps, "errorHandling");
	    populate(ps, "errorUrl");
	    populate(ps, "errorBusyUrl");
	    populate(ps, "externalThumbnailGeneration");
	    populate(ps, "URIEncoding");
	    populate(ps, "workflowEncoding");
	    populate(ps, "formsEncoding");
	    populate(ps, "uploadFromEncoding");
	    populate(ps, "uploadToEncoding");
	    populate(ps, "assetKeyFromEncoding");
	    populate(ps, "assetKeyToEncoding");
	    populate(ps, "useShortTableNames");
	    populate(ps, "useImprovedContentCategorySearch");
	    populate(ps, "logDatabaseMessages");
	    populate(ps, "statistics.enabled");
	    populate(ps, "statisticsLogPath");
	    populate(ps, "statisticsLogOneFilePerDay");
	    populate(ps, "statisticsLogger");
	    populate(ps, "enablePortal");
	    populate(ps, "portletBase");
	    populate(ps, "mail.smtp.host");
	    populate(ps, "mail.smtp.port");
	    populate(ps, "mail.smtp.auth");
	    populate(ps, "mail.smtp.user");
	    populate(ps, "mail.smtp.password");
	    populate(ps, "mail.contentType");
	    populate(ps, "systemEmailSender");
	    populate(ps, "warningEmailReceiver");
	    populate(ps, "emailRecipientLimit");
	    populate(ps, "loginUrl");
	    populate(ps, "logoutUrl");
	    populate(ps, "invalidLoginUrl");
	    populate(ps, "successLoginBaseUrl");
	    populate(ps, "authenticatorClass");
	    populate(ps, "authorizerClass");
	    populate(ps, "serverName");
	    populate(ps, "authConstraint");
	    populate(ps, "extraParametersFile");
	    populateData(ps, "extraSecurityParameters");
	    populate(ps, "casValidateUrl");
	    populate(ps, "casProxyValidateUrl");
	    populate(ps, "casServiceUrl");
	    populate(ps, "casLogoutUrl");
	    populate(ps, "ipAddressesToFallbackToBasicAuth");
	    
	    populate(ps, "deliver_loginUrl");
	    populate(ps, "deliver_logoutUrl");
	    populate(ps, "deliver_invalidLoginUrl");
	    populate(ps, "deliver_successLoginBaseUrl");
	    populate(ps, "deliver_authenticatorClass");
	    populate(ps, "deliver_authorizerClass");
	    populate(ps, "deliver_serverName");
	    populate(ps, "deliver_authConstraint");
	    populate(ps, "deliver_extraParametersFile");
	    populateData(ps, "deliver_extraSecurityParameters");
	    populate(ps, "deliver_security.anonymous.username");
	    populate(ps, "deliver_security.anonymous.password");
	    populate(ps, "deliver_casValidateUrl");
	    populate(ps, "deliver_casProxyValidateUrl");
	    populate(ps, "deliver_casServiceUrl");
	    populate(ps, "deliver_casLogoutUrl");

	    populate(ps, "workingStyleInformation");
	    populate(ps, "finalStyleInformation");
	    populate(ps, "publishStyleInformation");
	    populate(ps, "publishedStyleInformation");
	    populateData(ps, "customContentTypeIcons");
	    populateData(ps, "shortcuts");
	    populateData(ps, "WYSIWYGToolbarComboPreviewCSS");
   		populateData(ps, "WYSIWYGEditorAreaCSS");

	    populate(ps, "disableImageEditor");
	    populate(ps, "hideProtectedProperties");
	    
	    populate(ps, "protectContentTypes");
	    populate(ps, "protectWorkflows");
	    populate(ps, "protectCategories");

	    populate(ps, "internalSearchEngine");

	    populate(ps, "onlyAllowFolderType");
	    populate(ps, "skipResultDialogIfPossible");

	    populate(ps, "maxRows");
	    populate(ps, "maxNumberOfAssetInSearches");
	    populate(ps, "componentBindningAssetBrowser");
	    populate(ps, "prefferedWYSIWYG");
	    
	    populate(ps, "defaultNumberOfYearsBeforeExpire");
	    populate(ps, "enableDateTimeDirectEditing");
	    populate(ps, "showContentVersionFirst");
	    populate(ps, "tree");
	    populate(ps, "treemode");
	    populate(ps, "disableCustomIcons");
	    populate(ps, "showComponentsFirst");
	    populate(ps, "showAllWorkflows");
	    populate(ps, "editOnSight");
	    populate(ps, "previewDeliveryUrl");
	    populate(ps, "stagingDeliveryUrl");
	    populateData(ps, "internalDeliveryUrls");
	    populateData(ps, "publicDeliveryUrls");
	    populateData(ps, "toolLanguages");
	    populateData(ps, "deploymentServers");
	    populateData(ps, "vcServers");
	    populate(ps, "decoratedPageInvoker");
	    
	    populate(ps, "edition.pageSize");
	    populate(ps, "content.tree.sort");
	    populate(ps, "structure.tree.sort");
	    populate(ps, "structure.tree.isHidden");
	    populate(ps, "content.tree.hideForbidden");
	    populate(ps, "structure.tree.hideForbidden");
	    populate(ps, "enforceRigidContentAccess");
	    populate(ps, "disableEmptyUrls");
	    populate(ps, "cacheUpdateAction");
	    populate(ps, "logPath");

	    populate(ps, "logTransactions");
	    populate(ps, "enableExtranetCookies");
	    populate(ps, "useAlternativeBrowserLanguageCheck");
	    populate(ps, "caseSensitiveRedirects");
	    populate(ps, "useDNSNameInURI");
	    
	    populate(ps, "extranetCookieTimeout");
	    populate(ps, "webServicesBaseUrl");
	    populate(ps, "livePublicationThreadClass");
	    populate(ps, "publicationThreadDelay");
	    populate(ps, "pathsToRecacheOnPublishing");
	    populate(ps, "disableTemplateDebug");
	    populate(ps, "exportFormat");
	    populate(ps, "dbRelease");
	    populate(ps, "dbUser");
	    populate(ps, "dbPassword");
	    populate(ps, "masterServer");
	    populate(ps, "slaveServer");
	    populate(ps, "buildName");
	    populate(ps, "adminToolsPath");
	    populate(ps, "dbScriptPath");
	    populate(ps, "digitalAssetUploadPath");
	    populate(ps, "inputCharacterEncoding");
	    populate(ps, "deliver_inputCharacterEncoding");
		populate(ps, "protectDeliverWorking");
	    populate(ps, "protectDeliverPreview");
	    populate(ps, "forceIdentityCheck");
	    populate(ps, "allowCrossSiteSubmitToPublish");
	    populate(ps, "usePasswordEncryption");
	    populate(ps, "helpUrl");
	    populateData(ps, "headerHTML");
	    
	    populate(ps, "allowPublicationEventFilter");
	    populate(ps, "defaultPublicationEventFilter");
	    
	    populate(ps, "numberOfVersionsToKeepDuringClean");
	    populate(ps, "keepOnlyOldPublishedVersionsDuringClean");
	    populate(ps, "minimumTimeBetweenVersionsDuringClean");
	    populateData(ps, "assetUploadTransformationsSettings");

	    populate(ps, "setDerivedLastModifiedInLive");
	    populate(ps, "maxNumberOfVersionsForDerivedLastModifiedInLive");
	    populate(ps, "allowInternalCallsBasedOnIP");
	    
	    populate(ps, "assetFileNameForm");

	    populate(ps, "deriveProtocolWhenUsingProtocolRedirects");
	    populate(ps, "useAccessBasedProtocolRedirects");
	    populate(ps, "unprotectedProtocolName");
	    populate(ps, "protectedProtocolName");
	    populate(ps, "unprotectedProtocolPort");
	    populate(ps, "protectedProtocolPort");
	    populate(ps, "accessBasedProtocolRedirectHTTPCode");

	    populate(ps, "allowedDirectLoginNames");

	    try
	    {
	    	UserControllerProxy.getController().updateAnonymousUserPassword();
	    }
	    catch (SystemException e) 
		{
			e.printStackTrace();
		}
	    
		try 
		{
			CacheController.clearServerNodeProperty(true);
			InfoGlueAuthenticationFilter.initializeCMSProperties();
		} 
		catch (SystemException e) 
		{
			e.printStackTrace();
		}

		NotificationMessage notificationMessage = new NotificationMessage("ViewServerNodePropertiesAction.doSave():", "ServerNodeProperties", this.getInfoGluePrincipal().getName(), NotificationMessage.SYSTEM, "0", "ServerNodeProperties");
		//ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
		RemoteCacheUpdater.getSystemNotificationMessages().add(notificationMessage);
			    
    	return "save";
    }

    /**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doDeleteProperty() throws Exception
    {
		validateSecurityCode();

        Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);

        ps.remove("serverNode_" + this.getServerNodeId() + "_" + key);

		try 
		{
			CacheController.clearServerNodeProperty(true);
			InfoGlueAuthenticationFilter.initializeCMSProperties();
		} 
		catch (SystemException e) 
		{
			e.printStackTrace();
		}

		NotificationMessage notificationMessage = new NotificationMessage("ViewServerNodePropertiesAction.doSave():", "ServerNodeProperties", this.getInfoGluePrincipal().getName(), NotificationMessage.SYSTEM, "0", "ServerNodeProperties");
		//ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
		RemoteCacheUpdater.getSystemNotificationMessages().add(notificationMessage);

    	return "save";
    }

    /**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doSaveAndExit() throws Exception
    {
    	doSave();
    	
        return "saveAndExit";
    }

    public java.lang.Integer getServerNodeId()
    {
        return this.serverNodeVO.getServerNodeId();
    }
        
    public void setServerNodeId(java.lang.Integer serverNodeId) throws Exception
    {
        this.serverNodeVO.setServerNodeId(serverNodeId);
    }

	public ServerNodeVO getServerNodeVO() 
	{
		return serverNodeVO;
	}

	public PropertySet getPropertySet() 
	{
		return propertySet;
	}

	public String getPropertyValue(String prefix, String key) 
	{
		String value = propertySet.getString("serverNode_" + this.getServerNodeId() + "_" + prefix + "_" + key);

		return (value != null ? value : "");
	}

	public String getPropertyValue(String key) 
	{
		String value = propertySet.getString("serverNode_" + this.getServerNodeId() + "_" + key);

		return (value != null ? value : "");
	}
	
	public String getDataPropertyValue(String key) throws Exception
	{
		byte[] valueBytes = propertySet.getData("serverNode_" + this.getServerNodeId() + "_" + key);
	    
		return (valueBytes != null ? new String(valueBytes, "utf-8") : "");
	}

	public String getDataPropertyValue(String prefix, String key) throws Exception
	{
		byte[] valueBytes = propertySet.getData("serverNode_" + this.getServerNodeId() + "_" + prefix + "_" + key);
	    
		return (valueBytes != null ? new String(valueBytes, "utf-8") : "");
	}

	public CmsPropertyHandler getCmsPropertyHandler() 
	{
		return new CmsPropertyHandler();
	}

    public List getServerNodeVOList()
    {
        return serverNodeVOList;
    }

	public String getKey() 
	{
		return key;
	}

	public void setKey(String key) 
	{
		this.key = key;
	}

	public String getActiveDivId()
	{
		return activeDivId;
	}

	public void setActiveDivId(String activeDivId)
	{
		this.activeDivId = activeDivId;
	}
}
