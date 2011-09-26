package org.infoglue.cms.controllers.kernel.impl.simple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.ImageButton;
import org.infoglue.cms.applications.common.ToolbarButton;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.AvailableServiceBindingVO;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.InterceptorVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RedirectVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.management.ServiceDefinitionVO;
import org.infoglue.cms.entities.management.SiteNodeTypeDefinitionVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.workflow.WorkflowDefinitionVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.providers.ToolbarProvider;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.RemoteCacheUpdater;
import org.infoglue.deliver.util.Timer;

public class ToolbarController implements ToolbarProvider
{
	private final static Logger logger = Logger.getLogger(ToolbarController.class.getName());

	private static final long serialVersionUID = 1L;
	
	private VisualFormatter formatter = new VisualFormatter();
	private String URIEncoding = CmsPropertyHandler.getURIEncoding();
	
	public List<ToolbarButton> getRightToolbarButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		try
		{
			List<ToolbarButton> toolbarButtons = new ArrayList<ToolbarButton>();
	
			//if(toolbarKey.equalsIgnoreCase("tool.common.adminTool.header"))
			//	toolbarButtons.addAll(getMySettingsButton(toolbarKey, principal, locale, request, disableCloseButton));
			toolbarButtons.addAll(getHelpButton(toolbarKey, principal, locale, request, disableCloseButton));
			
			if(!disableCloseButton)
			{
				toolbarButtons.add(getDialogCloseButton(toolbarKey, principal, locale, request, disableCloseButton));
			}
			
			return toolbarButtons;
		}
		catch(Exception e) {e.printStackTrace();}			
					
		return new ArrayList<ToolbarButton>();	
	}
	
	public List<ToolbarButton> getToolbarButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		Timer t = new Timer();
		
		logger.info("toolbarKey:" + toolbarKey);
		
		try
		{
			if(toolbarKey.equalsIgnoreCase("tool.common.adminTool.header"))
				return getAdminToolStandardButtons();

			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentHeader"))
				return getContentButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionHeader"))
				return getContentVersionButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionStandaloneHeader"))
				return getContentVersionStandaloneButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionWizardHeader"))
				return getContentVersionButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.common.globalSubscriptions.header"))
				return getGlobalSubscriptionsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.structuretool.siteNodeComponentsHeader"))
				return getSiteNodeButtons(toolbarKey, principal, locale, request);
					
			
			/*
			if(toolbarKey.equalsIgnoreCase("tool.structuretool.createSiteNodeHeader"))
				return getCreateSiteNodeButtons();
			*/
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.repositoryList.header"))
				return getRepositoriesButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRepository.header"))
				return getRepositoryDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewLanguageList.header"))
				return getLanguagesButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewLanguage.header"))
				return getLanguageDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewCategoryList.header") || toolbarKey.equalsIgnoreCase("tool.managementtool.editCategory.header"))
				return getCategoryButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewContentTypeDefinitionList.header"))
				return getContentTypeDefinitionsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewContentTypeDefinition.header"))
				return getContentTypeDefinitionDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewWorkflowDefinitionList.header"))
				return getWorkflowDefinitionsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewWorkflowDefinition.header"))
				return getWorkflowDefinitionDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.portletList.header"))
				return getPortletsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			//if(toolbarKey.equalsIgnoreCase("tool.managementtool.portlet.header"))
			//	return getPortletDetailsButtons();
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.redirectList.header"))
				return getRedirectsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRedirect.header"))
				return getRedirectDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroupProperties.header"))
				return getGroupPropertiesButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRoleProperties.header"))
				return getRolePropertiesButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewUserProperties.header"))
				return getUserPropertiesButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSystemUserList.header"))
				return getSystemUsersButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSystemUser.header"))
				return getSystemUserDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRoleList.header"))
				return getRolesButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRole.header"))
				return getRoleDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroupList.header"))
				return getGroupsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroup.header"))
				return getGroupDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptionPointList.header"))
				return getInterceptionPointsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptionPoint.header"))
				return getInterceptionPointButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptorList.header"))
				return getInterceptorsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptor.header"))
				return getInterceptorButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewServiceDefinitionList.header"))
				return getServiceDefinitionsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewServiceDefinition.header"))
				return getServiceDefinitionDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewAvailableServiceBindingList.header"))
				return getAvailableServiceBindingsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewAvailableServiceBinding.header"))
				return getAvailableServiceBindingDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSiteNodeTypeDefinitionList.header"))
				return getSiteNodeTypeDefinitionsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSiteNodeTypeDefinition.header"))
				return getSiteNodeTypeDefinitionDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			//if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewUp2DateList.header"))
			//	return getAvailablePackagesButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.serverNodeList.header"))
				return getServerNodesButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.managementtool.serverNodeProperties.header"))
				return getApplicationSettingsButtons(toolbarKey, principal, locale, request, disableCloseButton);

			//if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewServerNode.header"))
			//	return getServerNodeDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewMessageCenter.header"))
				return getMessageCenterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.themes.header"))
				return getThemesButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.labels.header"))
				return getLabelsButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.publishingtool.repositoryPublications"))
				return getPublicationsButtons(locale, request);
			if(toolbarKey.equalsIgnoreCase("tool.publishingtool.globalSettings.header"))
				return getSystemPublicationsButtons(locale, request);
		}
		catch(Exception e) {e.printStackTrace();}			
					
		return new ArrayList<ToolbarButton>();				
	}
	

	public List<ToolbarButton> getFooterToolbarButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		Timer t = new Timer();
		
		logger.info("toolbarKey:" + toolbarKey);
				
		try
		{
			if(toolbarKey.equalsIgnoreCase("tool.common.install.introduction.title") ||
			   toolbarKey.equalsIgnoreCase("tool.common.install.database.title") ||
			   toolbarKey.equalsIgnoreCase("tool.common.install.server.title") ||
			   toolbarKey.equalsIgnoreCase("tool.common.install.initialData.title"))
			{
				return getCommonNextCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			}
			if(toolbarKey.equalsIgnoreCase("tool.common.install.databaseUpgrade.title"))
			{
				return getInstallUpgradeDatabaseFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			}
				

			if(toolbarKey.equalsIgnoreCase("tool.common.install.installationFinished.title"))
				asButtons(getDialogCloseButton(toolbarKey, principal, locale, request, false));
			
			if(toolbarKey.equalsIgnoreCase("tool.common.constraintException.title"))
				return asButtons(getDialogCloseButton(toolbarKey, principal, locale, request, false));
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.accessRights.header"))
				return getCommonFooterSaveOrSaveAndExitOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "UpdateAccessRights!saveAndExitV3.action");

			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentHeader"))
				return asButtons(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));

			if(toolbarKey.equalsIgnoreCase("tool.contenttool.changeVersionLanguage.header"))
				return getCommonFooterSaveOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionAsXMLHeader"))
				return getCommonFooterSaveOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.changeContentType.header"))
				return getCommonFooterSaveOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.tasktool.availableTasks.header"))
				return asButtons(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton));
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.exportContent.header"))
				return getCommonFooterSaveOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.exportContent.headerFinished"))
				return asButtons(getDialogCloseButton(toolbarKey, principal, locale, request, disableCloseButton));

			if(toolbarKey.equalsIgnoreCase("tool.contenttool.importContent.header"))
				return getCommonFooterSaveOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.importContent.headerFinished"))
				return asButtons(getDialogCloseButton(toolbarKey, principal, locale, request, disableCloseButton));
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.createContentHeader") || toolbarKey.equalsIgnoreCase("tool.contenttool.createFolderHeader"))
				return getCommonFooterSaveOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionHeader"))
				return getContentVersionFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.contenttool.assetDialog.assetDialogHeader"))
				return getContentVersionAssetsFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.assetDialog.assetDialogForComponentHeader"))
				return getContentVersionAssetsForComponentBindingFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.uploadDigitalAsset.header"))
				return getUploadDigitalAssetsFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.contenttool.fileUpload.fileUploadFailedHeader"))
				return getUploadDigitalAssetsFailedFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.contenttool.imageEditorHeader"))
				return getImageEditorFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.linkDialog.linkDialogHeader"))
				return getLinkDialogFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionHistory.label"))
				return getContentHistoryFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.structuretool.choosePagesLabel"))
				return getCommonFooterSaveOrCloseButton(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.structuretool.chooseContentLabel"))
				return getCommonFooterSaveOrCloseButton(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.structuretool.chooseContentsLabel"))
				return getCommonFooterSaveOrCloseButton(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.structuretool.componentPropertiesEditorLabel"))
				return getCommonAddSaveCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.structuretool.chooseRelatedContentsLabel"))
				return getContentRelationFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionStandaloneHeader"))
				return getContentVersionStandaloneFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionWizardHeader"))
				return getContentVersionWizardFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.createContentWizardChooseLocation.title"))
				return getCommonNextCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.createContentWizardInputContent.title"))
				return getCommonNextCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.createContentWizardUploadDigitalAsset.title"))
				return getCommonNextCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentPropertiesHeader"))
				return getCommonFooterSaveOrSaveAndExitOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "ViewContentProperties!saveAndExitV3.action");

			if(toolbarKey.equalsIgnoreCase("tool.structuretool.choosePageLabel"))
				return getCommonFooterUseSelectedOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.structuretool.siteNodeDetailsHeader"))
				return getSiteNodeFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.structuretool.siteNodeHistory.header"))
				return getSiteNodeHistoryFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.structuretool.siteNodeTypeDefinitionMissingHeader"))
				return getCommonFooterSaveOrCloseButton(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.structuretool.createSiteNodeHeader"))
				return getCreateSiteNodeFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.createPageTemplateWizard.title"))
				return getCreatePageTemplateFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.createEmailChooseRecipients.title"))
				return getMessageCenterFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.createEmailComposeEmail.title"))
				return getMessageCenterComposeEmailFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewMessageCenter.header"))
				return getMessageCenterFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.common.subscriptions.header"))
				return getSaveCancelFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.structuretool.publishSiteNode.header"))
				return getPublishPageFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.structuretool.unpublishSiteNode.header"))
				return getUnpublishPageFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.contenttool.publishContent.header"))
				return getPublishContentsFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.common.unpublishing.unpublishContentsHeader"))
				return getUnPublishContentsFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.managementtool.mysettings.header"))
				return getMySettingsFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.common.trashcan.title"))
				return getTrashcanFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			
			/*
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.repositoryList.header"))
				return getRepositoriesButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRepository.header"))
				return getRepositoryDetailsButtons();
			*/
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroupProperties.header"))
				return getEntityPropertiesFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRoleProperties.header"))
				return getEntityPropertiesFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewUserProperties.header"))
				return getEntityPropertiesFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.createSystemUser.header"))
				return getCreateSystemUserFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSystemUser.header"))
				return getSystemUserDetailFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRole.header"))
				return getRoleDetailFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.createRole.header"))
				return getCreateRoleFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroup.header"))
				return getGroupDetailFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.createGroup.header"))
				return getCreateGroupFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.assetDialog.assetDialogForMultipleBindingsHeader"))
				return getAssetDialogForMultipleBindingsFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.serverNodeProperties.header"))
				return getCommonFooterSaveOrCancelByRefreshButton(toolbarKey, principal, locale, request, disableCloseButton);

			if(toolbarKey.equalsIgnoreCase("tool.managementtool.repositoryLanguages.header"))
				return getCommonFooterSaveOrCancelByJavascriptButton(toolbarKey, principal, locale, request, disableCloseButton, "cancel();");
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewContentTypeDefinitionSimple.header"))
				return getCommonFooterSaveOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			
			
			/*
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewLanguageList.header"))
				return getLanguagesButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewLanguage.header"))
				return getLanguageDetailsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptionPointList.header"))
				return getInterceptionPointsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptionPoint.header"))
				return getInterceptionPointButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptorList.header"))
				return getInterceptorsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptor.header"))
				return getInterceptorButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewServiceDefinitionList.header"))
				return getServiceDefinitionsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewServiceDefinition.header"))
				return getServiceDefinitionDetailsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewAvailableServiceBindingList.header"))
				return getAvailableServiceBindingsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewAvailableServiceBinding.header"))
				return getAvailableServiceBindingDetailsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSiteNodeTypeDefinitionList.header"))
				return getSiteNodeTypeDefinitionsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSiteNodeTypeDefinition.header"))
				return getSiteNodeTypeDefinitionDetailsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewContentTypeDefinitionList.header"))
				return getContentTypeDefinitionsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewContentTypeDefinition.header"))
				return getContentTypeDefinitionDetailsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewCategoryList.header") || toolbarKey.equalsIgnoreCase("tool.managementtool.editCategory.header"))
				return getCategoryButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewUp2DateList.header"))
				return getAvailablePackagesButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewWorkflowDefinitionList.header"))
				return getWorkflowDefinitionsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewWorkflowDefinition.header"))
				return getWorkflowDefinitionDetailsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.portletList.header"))
				return getPortletsButtons();
			//if(toolbarKey.equalsIgnoreCase("tool.managementtool.portlet.header"))
			//	return getPortletDetailsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.redirectList.header"))
				return getRedirectsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRedirect.header"))
				return getRedirectDetailsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.serverNodeList.header"))
				return getServerNodesButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewServerNode.header"))
				return getServerNodeDetailsButtons();
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewMessageCenter.header"))
				return getMessageCenterButtons();
			*/
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.deploymentQuick.header"))
				return getQuickDeployFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.deploymentVC.header"))
				return getVCDeployFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.deploymentVC.chooseTagHeader"))
				return getVCDeployFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
		
			if(toolbarKey.equalsIgnoreCase("tool.structuretool.moveSiteNode.header"))
				return getCommonFooterSaveOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton, null, getLocalizedString(locale, "tool.structuretool.toolbarV3.movePageLabel"), getLocalizedString(locale, "tool.structuretool.toolbarV3.movePageTitle"));
			if(toolbarKey.equalsIgnoreCase("tool.structuretool.moveMultipleSiteNodes.header"))
				return getCommonAddNextCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.structuretool.moveMultipleSiteNode.header"))
				return getCommonFooterSaveOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.structuretool.moveMultipleSiteNode.finished"))
				return asButtons(getDialogCloseButton(toolbarKey, principal, locale, request, disableCloseButton));
			
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.moveContent.header"))
				return getCommonFooterSaveOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton, null, getLocalizedString(locale, "tool.contenttool.toolbarV3.moveContentLabel"), getLocalizedString(locale, "tool.contenttool.toolbarV3.moveContentLabel"));
			if(toolbarKey.equalsIgnoreCase("tool.contenttool.moveMultipleContent.header"))
				return getCommonAddNextCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.uploadTheme.header"))
				return getCommonFooterSaveOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
			if(toolbarKey.equalsIgnoreCase("tool.managementtool.uploadTranslation.header"))
				return getCommonFooterSaveOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton);

		}
		catch(Exception e) {e.printStackTrace();}			
					
		return new ArrayList<ToolbarButton>();				
	}

	private List<ToolbarButton> getAssetDialogForMultipleBindingsFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("useSelectedAsset", 
				  getLocalizedString(locale, "tool.contenttool.assetDialog.chooseAttachment"), 
				  getLocalizedString(locale, "tool.contenttool.assetDialog.chooseAttachment"), 
				  "useSelectedAsset();", 
				  "", 
				  "", 
				  "linkInsert", 
				  true, 
				  false, 
				  "", 
				  "", 
				  ""));
		
		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		//buttons.add(getCommonFooterCancelButton("ViewListGroup!listManagableGroups.action"));
				
		return buttons;
	}

	
	private List<ToolbarButton> getGroupDetailFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, "UpdateGroup!saveAndExitV3.action"));
		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "ViewListGroup!listManagableGroups.action", false));
				
		return buttons;
	}

	private List<ToolbarButton> getCreateGroupFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, "CreateGroup!saveAndExitV3.action"));
		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "ViewListGroup!listManagableGroups.action", false));
				
		return buttons;
	}

	private List<ToolbarButton> getRoleDetailFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, "UpdateRole!saveAndExitV3.action"));
		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "ViewListRole!listManagableRoles.action", false));
				
		return buttons;
	}

	private List<ToolbarButton> getCreateRoleFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, "CreateRole!saveAndExitV3.action"));
		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "ViewListRole!listManagableRoles.action", false));
				
		return buttons;
	}

	private List<ToolbarButton> getSystemUserDetailFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		String primaryKey = request.getParameter("userName");
		if(primaryKey == null || primaryKey.equals(""))
			throw new Exception("Missing argument userName for primaryKey.");
		
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		InfoGluePrincipal infoGluePrincipal = UserControllerProxy.getController().getUser(primaryKey);
		if(infoGluePrincipal == null)
			throw new SystemException("No user found called '" + primaryKey + "'. This could be an encoding issue if you gave your user a login name with non ascii chars in it. Look in the administrative manual on how to solve it.");
		boolean supportsUpdate = infoGluePrincipal.getAutorizationModule().getSupportUpdate();
		
		if(supportsUpdate)
		{
			buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
			buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, "UpdateSystemUser!saveAndExitV3.action"));
		}
		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "ViewListSystemUser!v3.action", false));
				
		return buttons;
	}

	private List<ToolbarButton> getCreateSystemUserFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, "CreateSystemUser!saveAndExitV3.action"));
		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "ViewListSystemUser!listManagableSystemUsers.action", false));
				
		return buttons;
	}
	
	private List<ToolbarButton> getEntityPropertiesFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
									  getLocalizedString(locale, "tool.contenttool.save.label"), 
									  getLocalizedString(locale, "tool.contenttool.save.label"),
									  "validateAndSubmitContentForm();",
									  "images/v3/saveInlineIcon.gif",
									  "left",
									  "save",
									  true));

		buttons.add(new ToolbarButton("",
									  getLocalizedString(locale, "tool.contenttool.saveAndExit.label"), 
									  getLocalizedString(locale, "tool.contenttool.saveAndExit.label"),
									  "validateAndSubmitContentFormThenExit();",
									  "images/v3/saveAndExitInlineIcon.gif",
									  "left",
									  "saveAndExit",
									  true));
		
		buttons.add(new ToolbarButton("",
				  					  getLocalizedString(locale, "tool.contenttool.cancel.label"), 
				  					  getLocalizedString(locale, "tool.contenttool.cancel.label"),
				  					  "cancel();",
				  					  "images/v3/cancelIcon.gif",
				  					  "left",
				  					  "cancel",
				  					  true));
		
		return buttons;
	}
	
	
	private List<ToolbarButton> getAdminToolStandardButtons() throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		return buttons;
	}
	
	
	private List<ToolbarButton> getContentButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		Integer contentId = new Integer(request.getParameter("contentId"));
		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
		
		
		ToolbarButton createButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.createContentLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.createContentTitle"),
				  "CreateContent!inputV3.action?isBranch=false&repositoryId=" + contentVO.getRepositoryId() + "&parentContentId=" + contentId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "create",
				  "inlineDiv");

		ToolbarButton createFolderButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.createContentFolderLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.createContentFolderTitle"),
				  "CreateContent!inputV3.action?isBranch=true&repositoryId=" + contentVO.getRepositoryId() + "&parentContentId=" + contentId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "create",
				  "inlineDiv");

		createButton.getSubButtons().add(createFolderButton);
		buttons.add(createButton);

		ToolbarButton moveButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.moveContentLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.moveContentTitle"),
				  "MoveContent!inputV3.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&hideLeafs=true&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "moveContent");

		ToolbarButton moveMultipleButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.moveMultipleContentLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.moveMultipleContentTitle"),
				  "MoveMultipleContent!inputV3.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "moveContent");
		
		moveButton.getSubButtons().add(moveMultipleButton);
		buttons.add(moveButton);

		ToolbarButton deleteButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentTitle"),
				  "DeleteContent!V3.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&changeTypeId=4&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "",
				  "delete",
				  true,
				  true,
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentConfirmationLabel", new String[]{contentVO.getName()}),
				  "inlineDiv");
		
		if(contentVO.getIsBranch())
		{
			ToolbarButton deleteChildrenButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentChildrenLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentChildrenTitle"),
				  "DeleteContentChildren.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&changeTypeId=4&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "",
				  "delete",
				  true,
				  true,
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentChildrenLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentChildrenConfirmationLabel", new String[]{contentVO.getName()}),
				  "inlineDiv");
		
			deleteButton.getSubButtons().add(deleteChildrenButton);
		}
		
		buttons.add(deleteButton);

		if(contentVO.getIsBranch())
		{
			buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.editContentMetaInfoLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.editContentMetaInfoTitle"),
				  "ViewContentProperties!V3.action?contentId=" + contentId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "properties"));
		}
		
		ToolbarButton publishButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.publishContentLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.publishContentTitle"),
				  "ViewListContentVersion!V3.action?contentId=" + contentId + "&recurseContents=false&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "publish");

		ToolbarButton submitToPublishButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.publishContentsLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.publishContentsTitle"),
				  "ViewListContentVersion!V3.action?contentId=" + contentId + "&recurseContents=true&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "submitToPublish");

		publishButton.getSubButtons().add(submitToPublishButton);
		buttons.add(publishButton);

		if(ContentController.getContentController().hasPublishedVersion(contentId) || contentVO.getIsBranch())
		{
			ToolbarButton unpublishButton = new ToolbarButton("",
					  getLocalizedString(locale, "tool.contenttool.toolbarV3.unpublishContentsLabel"), 
					  getLocalizedString(locale, "tool.contenttool.toolbarV3.unpublishContentsTitle"),
					  "UnpublishContentVersion!inputV3.action?contentId=" + contentId + "&recurseContents=false&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
					  "",
					  "unpublish");
	
			ToolbarButton unpublishAllButton = new ToolbarButton("",
					  getLocalizedString(locale, "tool.contenttool.toolbarV3.unpublishContentsAllLabel"), 
					  getLocalizedString(locale, "tool.contenttool.toolbarV3.unpublishContentsAllTitle"),
					  "UnpublishContentVersion!inputChooseContentsV3.action?contentId=" + contentId + "&recurseContents=false&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
					  "",
					  "unpublish");
	
			unpublishButton.getSubButtons().add(unpublishAllButton);
			buttons.add(unpublishButton);
		}
		
		if(contentVO.getIsProtected().intValue() == ContentVO.YES.intValue())
		{
			buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.accessRights.accessRightsContentButtonLabel"), 
				  getLocalizedString(locale, "tool.common.accessRights.accessRightsContentButtonLabel"),
				  "ViewAccessRights!V3.action?interceptionPointCategory=Content&extraParameters=" + contentVO.getId() + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "images/v3/accessRightIcon.gif",
				  "accessRights"));
		}
		
		String contentPath = getContentIDPath(contentVO);

		ToolbarButton syncTreeButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.showContentInTreeLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.showContentInTreeTitle"),
				  "javascript:syncWithTree('" + contentPath + "', " + contentVO.getRepositoryId() + ", 'contentTreeIframe');",
				  "",
				  "syncTree");

		ToolbarButton runTaskButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.runTaskLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.runTaskTitle"),
				  "ViewExecuteTask.action?contentId=" + contentId + "",
				  "",
				  "runTask");

		ToolbarButton changeContentTypeButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.changeContentTypeDefinitionLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.changeContentTypeDefinitionTitle"),
				  "UpdateContent!inputContentType.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId(),
				  "",
				  "changeContentType");

		ToolbarButton exportContentButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.exportContentLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.exportContentTitle"),
				  "ExportContent!input.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId(),
				  "",
				  "exportContent");

		ToolbarButton importContentButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.importContentLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.importContentTitle"),
				  "ImportContent!input.action?parentContentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId(),
				  "",
				  "importContent");

		ToolbarButton createContentsFromUploadButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.createContentsFromUploadLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.createContentsFromUploadLabel"),
				  "CreateContentAndAssetFromUpload!input.action?parentContentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId(),
				  "",
				  "createContentsFromUpload");

		syncTreeButton.getSubButtons().add(runTaskButton);
		syncTreeButton.getSubButtons().add(changeContentTypeButton);
		syncTreeButton.getSubButtons().add(exportContentButton);
		syncTreeButton.getSubButtons().add(importContentButton);
		syncTreeButton.getSubButtons().add(createContentsFromUploadButton);
		buttons.add(syncTreeButton);

		return buttons;
	}

	private String getContentIDPath(ContentVO contentVO)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(contentVO.getId());
		try
		{
			if(contentVO.getParentContentId() != null)
			{
				ContentVO parentContentVO = ContentController.getContentController().getContentVOWithId(contentVO.getParentContentId());
				while(parentContentVO != null)
				{
					sb.insert(0, parentContentVO.getId() + ",");
					if(parentContentVO.getParentContentId() != null)
						parentContentVO = ContentController.getContentController().getContentVOWithId(parentContentVO.getParentContentId());
					else
						parentContentVO = null;
				}
			}
		}
		catch (Exception e) 
		{
			logger.warn("Problem getting ContentIDPath: " + e.getMessage());
		}
		
		return sb.toString();
	}

	private List<ToolbarButton> getContentVersionStandaloneButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		Timer t = new Timer();
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		LanguageVO currentLanguageVO = null;
		ContentVO contentVO = null;
		
		Integer primaryKeyAsInteger = null;
		try
		{
			primaryKeyAsInteger = new Integer(request.getParameter("contentVersionId"));
		}
		catch (Exception e) 
		{
		}
		try
		{
			if(request.getAttribute("contentVersionId") != null)
				primaryKeyAsInteger = new Integer("" + request.getAttribute("contentVersionId"));
		}
		catch (Exception e) 
		{
		}

		
		if(primaryKeyAsInteger != null)
		{
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(primaryKeyAsInteger);
			contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId());
			currentLanguageVO = LanguageController.getController().getLanguageVOWithId(contentVersionVO.getLanguageId());
		}
		else
		{
			Integer contentId = null;
			if(request.getAttribute("contentId") != null)
				contentId = new Integer((String)request.getAttribute("contentId"));
			else
				contentId = new Integer((String)request.getParameter("contentId"));

			Integer languageId = null;
			if(request.getAttribute("languageId") != null)
				languageId = new Integer((String)request.getAttribute("languageId"));
			else
				languageId = new Integer((String)request.getParameter("languageId"));

			contentVO = ContentController.getContentController().getContentVOWithId(contentId);
			currentLanguageVO = LanguageController.getController().getLanguageVOWithId(languageId);
		}
		
		ToolbarButton languageDropButton = new ToolbarButton("",
															 StringUtils.capitalize(currentLanguageVO.getDisplayLanguage()), 
															 StringUtils.capitalize(currentLanguageVO.getDisplayLanguage()),
				  											 "",
					  										 "",
					  										 "right",
					  										 "locale",
					  										 false);
		
		Iterator repositoryLanguagesIterator = LanguageController.getController().getLanguageVOList(contentVO.getRepositoryId()).iterator();
		while(repositoryLanguagesIterator.hasNext())
		{
			LanguageVO languageVO = (LanguageVO)repositoryLanguagesIterator.next();
			if(!currentLanguageVO.getId().equals(languageVO.getId()))
			{
				languageDropButton.getSubButtons().add(new ToolbarButton("" + languageVO.getId(),
						 StringUtils.capitalize(languageVO.getDisplayLanguage()), 
						 StringUtils.capitalize(languageVO.getDisplayLanguage()),
						 "changeLanguage(" + contentVO.getId() + ", " + languageVO.getId() + ");",
						 "",
						 ""));
			}
		}
		
		buttons.add(languageDropButton);
		
		return buttons;
	}

	private List<ToolbarButton> getContentVersionButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		String contentIdString = request.getParameter("contentId");
		if(contentIdString == null || contentIdString.equals(""))
			contentIdString = (String)request.getAttribute("contentId");

		String languageIdString = request.getParameter("languageId");
		if(languageIdString == null || languageIdString.equals(""))
			languageIdString = (String)request.getAttribute("languageId");
		if(languageIdString != null && languageIdString.equals(""))
			languageIdString = null;
		
		if(contentIdString == null || contentIdString.equals(""))
		{
			logger.error("No contentId was sent in to getContentVersionButtons so we cannot continue. Check why. Original url: " + request.getRequestURI() + "?" + request.getQueryString());
			return buttons;
		}
		
		Integer contentId = new Integer(contentIdString);
		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
		
		String contentVersionIdString = request.getParameter("contentVersionId");
		Integer contentVersionId = null;
		if(contentVersionIdString == null)
		{
			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, masterLanguageVO.getId());
			if(contentVersionVO != null)
				contentVersionId = contentVersionVO.getId();
		}
		else
		{
			contentVersionId = new Integer(contentVersionIdString);
		}
		
		ToolbarButton moveButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.moveContentLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.moveContentTitle"),
				  "MoveContent!inputV3.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&hideLeafs=true&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "moveContent");

		ToolbarButton moveMultipleButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.moveMultipleContentLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.moveMultipleContentTitle"),
				  "MoveMultipleContent!inputV3.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "moveContent");
		
		moveButton.getSubButtons().add(moveMultipleButton);
		buttons.add(moveButton);
		
        //if(!isReadOnly(contentVersionId))
		//{
			if(contentVersionId != null)
			{
				buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.contenttool.uploadDigitalAsset.label"), 
					  getLocalizedString(locale, "tool.contenttool.uploadDigitalAsset.label"),
					  "ViewDigitalAsset.action?contentVersionId=" + contentVersionId + "",
					  "",
					  "attachAsset"));
			}
		//}
        		
        ToolbarButton deleteButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentTitle"),
				  "DeleteContent!V3.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&changeTypeId=4&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "",
				  "delete",
				  true,
				  true,
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentConfirmationLabel", new String[]{contentVO.getName()}),
				  "inlineDiv");
		
		if(contentVO.getIsBranch())
		{
			ToolbarButton deleteChildrenButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentChildrenLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentChildrenTitle"),
				  "DeleteContentChildren.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&changeTypeId=4&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "",
				  "delete",
				  true,
				  true,
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentChildrenLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentChildrenConfirmationLabel", new String[]{contentVO.getName()}),
				  "inlineDiv");
		
			deleteButton.getSubButtons().add(deleteChildrenButton);
		}
		
		buttons.add(deleteButton);

		if(contentVO.getIsBranch())
		{
			buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.editContentMetaInfoLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.editContentMetaInfoTitle"),
				  "ViewContentProperties!V3.action?contentId=" + contentId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "properties"));
		}
		
		ToolbarButton publishButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.publishContentLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.publishContentTitle"),
				  "ViewListContentVersion!v3.action?contentId=" + contentId + (languageIdString != null ? "&languageId=" + languageIdString : "") + "&recurseContents=false&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "publish");

		if(contentVO.getIsBranch())
		{
			ToolbarButton submitToPublishButton = new ToolbarButton("",
					  getLocalizedString(locale, "tool.contenttool.toolbarV3.publishContentsLabel"), 
					  getLocalizedString(locale, "tool.contenttool.toolbarV3.publishContentsTitle"),
					  "ViewListContentVersion!v3.action?contentId=" + contentId + (languageIdString != null ? "&languageId=" + languageIdString : "") + "&recurseContents=false&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
					  "",
					  "submitToPublish");
	
			publishButton.getSubButtons().add(submitToPublishButton);
		}
		
		buttons.add(publishButton);
		
		if(ContentController.getContentController().hasPublishedVersion(contentId) || contentVO.getIsBranch())
		{
			ToolbarButton unpublishButton = new ToolbarButton("",
					  getLocalizedString(locale, "tool.contenttool.toolbarV3.unpublishContentsLabel"), 
					  getLocalizedString(locale, "tool.contenttool.toolbarV3.unpublishContentsTitle"),
					  "UnpublishContentVersion!inputV3.action?contentId=" + contentId + (languageIdString != null ? "&languageId=" + languageIdString : "") + "&recurseContents=false&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
					  "",
					  "unpublish");
	
			ToolbarButton unpublishAllButton = new ToolbarButton("",
					  getLocalizedString(locale, "tool.contenttool.toolbarV3.unpublishContentsAllLabel"), 
					  getLocalizedString(locale, "tool.contenttool.toolbarV3.unpublishContentsAllTitle"),
					  "UnpublishContentVersion!inputChooseContentsV3.action?contentId=" + contentId + (languageIdString != null ? "&languageId=" + languageIdString : "") + "&recurseContents=false&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
					  "",
					  "unpublish");
	
			unpublishButton.getSubButtons().add(unpublishAllButton);
			buttons.add(unpublishButton);
		}
		
		if(contentVO.getContentTypeDefinitionId() != null)
		{
			ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVO.getContentTypeDefinitionId());
			if(contentTypeDefinitionVO != null && (contentTypeDefinitionVO.getName().equalsIgnoreCase("HTMLTemplate") || contentTypeDefinitionVO.getName().equalsIgnoreCase("PageTemplate") || contentTypeDefinitionVO.getName().equalsIgnoreCase("PagePartTemplate")))
			{
				buttons.add(new ToolbarButton("",
						  getLocalizedString(locale, "tool.contenttool.toolbarV3.componentAccessRightsLabel"), 
						  getLocalizedString(locale, "tool.contenttool.toolbarV3.componentAccessRightsTitle"),
						  "ViewAccessRights!V3.action?interceptionPointCategory=Component&extraParameters=" + contentId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
						  "",
						  "componentAccessRights"));
				
				buttons.add(new ToolbarButton("",
						  getLocalizedString(locale, "tool.contenttool.toolbarV3.deployComponentLabel"), 
						  getLocalizedString(locale, "tool.contenttool.toolbarV3.deployComponentTitle"),
						  "ViewDeploymentChooseServer!inputQuickV3.action?contentId=" + contentId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
						  "",
						  "deployContent"));
			}
		}
		/*
		//if(contentVO.getIsProtected().intValue() == ContentVO.YES.intValue())
		//{
			ToolbarButton contentAccessRight = new ToolbarButton("",
					  getLocalizedString(locale, "tool.common.accessRights.accessRightsContentButtonLabel"), 
					  getLocalizedString(locale, "tool.common.accessRights.accessRightsContentButtonLabel"),
					  "ViewAccessRights!V3.action?interceptionPointCategory=Content&extraParameters=" + contentVO.getId() + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
					  "images/v3/accessRightIcon.gif",
					  "accessRights");

			ToolbarButton contentVersionAccessRight = new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.accessRights.accessRightsContentVersionButtonLabel"), 
				  getLocalizedString(locale, "tool.common.accessRights.accessRightsContentVersionButtonLabel"),
				  "ViewAccessRights!V3.action?interceptionPointCategory=ContentVersion&extraParameters=" + contentId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "images/v3/accessRightIcon.gif",
				  "accessRights");
	
			contentAccessRight.getSubButtons().add(contentVersionAccessRight);
			buttons.add(contentAccessRight);
		//}
		 */

		String contentPath = getContentIDPath(contentVO);

		ToolbarButton syncTreeButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.showContentInTreeLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.showContentInTreeTitle"),
				  "javascript:syncWithTree('" + contentPath + "', " + contentVO.getRepositoryId() + ", 'contentTreeIframe');",
				  "",
				  "syncTree");

		ToolbarButton runTaskButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.runTaskLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.runTaskTitle"),
				  "ViewExecuteTask.action?contentId=" + contentId + "",
				  "",
				  "runTask");

		syncTreeButton.getSubButtons().add(runTaskButton);

		if(contentVersionId != null)
		{
			ToolbarButton changeLanguageButton = new ToolbarButton("",
					  getLocalizedString(locale, "tool.contenttool.toolbarV3.changeLanguageLabel"), 
					  getLocalizedString(locale, "tool.contenttool.toolbarV3.changeLanguageTitle"),
					  "ChangeVersionLanguage!inputV3.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&contentVersionId=" + contentVersionId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
					  "",
					  "changeLanguage");
			syncTreeButton.getSubButtons().add(changeLanguageButton);
			
			ToolbarButton versionAsXMLButton = new ToolbarButton("",
					  getLocalizedString(locale, "tool.contenttool.toolbarV3.showDataAsXMLLabel"), 
					  getLocalizedString(locale, "tool.contenttool.toolbarV3.showDataAsXMLTitle"),
					  "ViewContentVersion!asXMLV3.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&contentVersionId=" + contentVersionId,
					  "",
					  "showDataAsXML");
			syncTreeButton.getSubButtons().add(versionAsXMLButton);
		}
		
		ToolbarButton changeContentTypeButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.changeContentTypeDefinitionLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.changeContentTypeDefinitionTitle"),
				  "UpdateContent!inputContentType.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId(),
				  "",
				  "changeContentType");
		syncTreeButton.getSubButtons().add(changeContentTypeButton);

		ToolbarButton exportContentButton = new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.exportContentLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.exportContentTitle"),
				  "ExportContent!input.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId(),
				  "",
				  "exportContent");
		syncTreeButton.getSubButtons().add(exportContentButton);

		buttons.add(syncTreeButton);
		/*
		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.showContentInTreeLabel"), 
				  getLocalizedString(locale, "tool.contenttool.toolbarV3.showContentInTreeTitle"),
				  "javascript:syncWithTree('" + contentPath + "', " + contentVO.getRepositoryId() + ", 'contentTreeIframe');",
				  "",
				  "syncTree"));

		if(contentVersionId != null)
		{
			buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.contenttool.toolbarV3.changeLanguageLabel"), 
					  getLocalizedString(locale, "tool.contenttool.toolbarV3.changeLanguageTitle"),
					  "ChangeVersionLanguage!inputV3.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&contentVersionId=" + contentVersionId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
					  "",
					  "changeLanguage"));	
		}
		*/
		return buttons;
	}
	

	private List<ToolbarButton> getContentVersionFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		String saveAndExitURL = (String)request.getAttribute("saveAndExitURL");
		
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		buttons.add(getCompareButton(toolbarKey, principal, locale, request, disableCloseButton));

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		
		/*
		buttons.add(new ToolbarButton("",
									  getLocalizedString(locale, "tool.contenttool.publish.label"), 
									  getLocalizedString(locale, "tool.contenttool.publish.label"),
									  "javascript:validateAndSubmitContentFormThenSubmitToPublish();",
				  					  "images/v3/publishIcon.gif"));
		*/
		
		buttons.add(new ToolbarButton("cancel",
				  					  getLocalizedString(locale, "tool.contenttool.cancel.label"), 
				  					  getLocalizedString(locale, "tool.contenttool.cancel.label"),
				  					  "document.location.reload(true);",
				  					  "images/v3/cancelIcon.gif",
				  					  "left",
									  "cancel",
				  					  true));
		
		return buttons;
	}

	private List<ToolbarButton> getContentVersionStandaloneFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		String contentVersionIdString = request.getParameter("contentVersionId");
		if(contentVersionIdString == null || contentVersionIdString.equals(""))
			contentVersionIdString = (String)request.getAttribute("contentVersionId");
		
		String saveAndExitURL = (String)request.getAttribute("saveAndExitURL");
		
		buttons.add(getCompareButton(toolbarKey, principal, locale, request, disableCloseButton));

		if(contentVersionIdString != null && !contentVersionIdString.equals(""))
		{
			buttons.add(new ToolbarButton("uploadAsset", 
					  getLocalizedString(locale, "tool.contenttool.uploadNewAttachment"), 
					  getLocalizedString(locale, "tool.contenttool.uploadNewAttachment"), 
					  "ViewDigitalAsset.action?contentVersionId=" + contentVersionIdString, 
					  "", 
					  "", 
					  "attachAsset", 
					  false, 
					  false, 
					  "", 
					  "", 
					  "inlineDiv",
					  500,
					  550));
		}

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		if(saveAndExitURL != null && !saveAndExitURL.equals(""))
			buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, saveAndExitURL));
		else
		{	
			buttons.add(new ToolbarButton("saveAndExit",
									  getLocalizedString(locale, "tool.contenttool.saveAndExit.label"), 
									  getLocalizedString(locale, "tool.contenttool.saveAndExit.label"),
									  "validateAndSubmitContentFormThenClose();",
									  "images/v3/saveAndExitInlineIcon.gif",
									  "left",
									  "saveAndExit",
									  true));
		}
				
		/*
		buttons.add(new ToolbarButton("",
									  getLocalizedString(locale, "tool.contenttool.publish.label"), 
									  getLocalizedString(locale, "tool.contenttool.publish.label"),
									  "javascript:validateAndSubmitContentFormThenSubmitToPublish();",
				  					  "images/v3/publishIcon.gif"));
		*/
	
		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton));
		
		return buttons;
	}

	
	private List<ToolbarButton> getContentVersionAssetsFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		String contentIdString = request.getParameter("contentId");
		if(contentIdString == null || contentIdString.equals(""))
			contentIdString = (String)request.getAttribute("contentId");
		
		if(contentIdString == null || contentIdString.equals(""))
		{
			logger.error("No contentId was sent in to getContentVersionButtons so we cannot continue. Check why. Original url: " + request.getRequestURI() + "?" + request.getQueryString());
			return buttons;
		}
		
		Integer contentId = new Integer(contentIdString);
		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
		
		String contentVersionIdString = request.getParameter("contentVersionId");
		Integer contentVersionId = null;
		if(contentVersionIdString == null)
		{
			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, masterLanguageVO.getId());
			if(contentVersionVO != null)
				contentVersionId = contentVersionVO.getId();
		}
		else
		{
			contentVersionId = new Integer(contentVersionIdString);
		}
		
		buttons.add(new ToolbarButton("useSelectedAsset", 
				  getLocalizedString(locale, "tool.contenttool.assetDialog.chooseAttachment"), 
				  getLocalizedString(locale, "tool.contenttool.assetDialog.chooseAttachment"), 
				  "useSelectedAsset();", 
				  "", 
				  "", 
				  "linkInsert", 
				  true, 
				  false, 
				  "", 
				  "", 
				  ""));

		buttons.add(new ToolbarButton("uploadAsset", 
				  getLocalizedString(locale, "tool.contenttool.uploadNewAttachment"), 
				  getLocalizedString(locale, "tool.contenttool.uploadNewAttachment"), 
				  "ViewDigitalAsset.action?contentVersionId=" + contentVersionId, 
				  "", 
				  "", 
				  "attachAsset", 
				  false, 
				  false, 
				  "", 
				  "", 
				  "inlineDiv",
				  500,
				  550));

		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton));
		
		return buttons;
	}

	private List<ToolbarButton> getContentVersionAssetsForComponentBindingFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		String contentIdString = request.getParameter("contentId");
		if(contentIdString == null || contentIdString.equals(""))
			contentIdString = (String)request.getAttribute("contentId");
		
		if(contentIdString != null && !contentIdString.equals("") && !contentIdString.equals("-1"))
		{
			Integer contentId = new Integer(contentIdString);
			ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
			
			String contentVersionIdString = request.getParameter("contentVersionId");
			Integer contentVersionId = null;
			if(contentVersionIdString == null)
			{
				LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());
				ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, masterLanguageVO.getId());
				if(contentVersionVO != null)
					contentVersionId = contentVersionVO.getId();
			}
			else
			{
				contentVersionId = new Integer(contentVersionIdString);
			}
		}
		
		buttons.add(new ToolbarButton("useSelectedAsset", 
				  getLocalizedString(locale, "tool.contenttool.assetDialog.chooseAttachment"), 
				  getLocalizedString(locale, "tool.contenttool.assetDialog.chooseAttachment"), 
				  "useSelectedAsset();", 
				  "", 
				  "", 
				  "linkInsert", 
				  true, 
				  false, 
				  "", 
				  "", 
				  ""));

		buttons.add(new ToolbarButton("uploadAsset", 
				  getLocalizedString(locale, "tool.contenttool.uploadNewAttachment"), 
				  getLocalizedString(locale, "tool.contenttool.uploadNewAttachment"), 
				  "uploadAsset();", 
				  "", 
				  "", 
				  "attachAsset", 
				  true, 
				  false, 
				  "", 
				  "", 
				  ""));
		
		/*
		buttons.add(new ToolbarButton("uploadAsset", 
				  getLocalizedString(locale, "tool.contenttool.uploadNewAttachment"), 
				  getLocalizedString(locale, "tool.contenttool.uploadNewAttachment"), 
				  "ViewDigitalAsset.action?contentVersionId=" + contentVersionId,
				  "", 
				  "", 
				  "attachAsset", 
				  false, 
				  false, 
				  "", 
				  "", 
				  "inlineDiv",
				  500,
				  550));
				  */

		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton));
		
		return buttons;
	}

	private List<ToolbarButton> getUploadDigitalAssetsFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, ""));
		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "onCancel();", true));
		buttons.add(getDialogCloseButton(toolbarKey, principal, locale, request, disableCloseButton));

		return buttons;
	}

	private List<ToolbarButton> getUploadDigitalAssetsFailedFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterReturnToReferrerOrHistoryBackButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton));

		return buttons;
	}

	private List<ToolbarButton> getImageEditorFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("resize", 
				  getLocalizedString(locale, "tool.contenttool.imageEditor.resize"), 
				  getLocalizedString(locale, "tool.contenttool.imageEditor.resize"), 
				  "openResizeImageDialog();", 
				  "", 
				  "", 
				  "resize", 
				  true, 
				  false, 
				  "", 
				  "", 
				  ""));

		buttons.add(new ToolbarButton("crop", 
				  getLocalizedString(locale, "tool.contenttool.imageEditor.crop"), 
				  getLocalizedString(locale, "tool.contenttool.imageEditor.crop"), 
				  "activateCropTool();", 
				  "", 
				  "", 
				  "crop", 
				  true, 
				  false, 
				  "", 
				  "", 
				  ""));

		buttons.add(new ToolbarButton("rotate", 
				  getLocalizedString(locale, "tool.contenttool.imageEditor.rotate"), 
				  getLocalizedString(locale, "tool.contenttool.imageEditor.rotate"), 
				  "openRotateImageDialog();", 
				  "", 
				  "", 
				  "rotate", 
				  true, 
				  false, 
				  "", 
				  "", 
				  ""));

		buttons.add(new ToolbarButton("undo", 
				  getLocalizedString(locale, "tool.contenttool.undo.label"), 
				  getLocalizedString(locale, "tool.contenttool.undo.label"), 
				  "openUndoDialog();", 
				  "", 
				  "", 
				  "undo", 
				  true, 
				  false, 
				  "", 
				  "", 
				  ""));

		buttons.add(new ToolbarButton("save", 
				  getLocalizedString(locale, "tool.contenttool.save.label"), 
				  getLocalizedString(locale, "tool.contenttool.save.label"), 
				  "openSaveImageDialog();", 
				  "", 
				  "", 
				  "save", 
				  true, 
				  false, 
				  "", 
				  "", 
				  ""));

		buttons.add(new ToolbarButton("saveAs", 
				  getLocalizedString(locale, "tool.contenttool.saveAs.label"), 
				  getLocalizedString(locale, "tool.contenttool.saveAs.label"), 
				  "openSaveAsImageDialog();", 
				  "", 
				  "", 
				  "save", 
				  true, 
				  false, 
				  "", 
				  "", 
				  ""));
		
		return buttons;
	}
	
	private List<ToolbarButton> getLinkDialogFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		String contentIdString = request.getParameter("contentId");
		if(contentIdString == null || contentIdString.equals(""))
			contentIdString = (String)request.getAttribute("contentId");
		
		if(contentIdString == null || contentIdString.equals(""))
		{
			logger.error("No contentId was sent in to getContentVersionButtons so we cannot continue. Check why. Original url: " + request.getRequestURI() + "?" + request.getQueryString());
			return buttons;
		}
		
		Integer contentId = new Integer(contentIdString);
		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
		
		String contentVersionIdString = request.getParameter("contentVersionId");
		Integer contentVersionId = null;
		if(contentVersionIdString == null)
		{
			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, masterLanguageVO.getId());
			if(contentVersionVO != null)
				contentVersionId = contentVersionVO.getId();
		}
		else
		{
			contentVersionId = new Integer(contentVersionIdString);
		}
		

		buttons.add(new ToolbarButton("useSelectedPage", 
				  getLocalizedString(locale, "tool.contenttool.linkDialog.choosePage"), 
				  getLocalizedString(locale, "tool.contenttool.linkDialog.choosePage"), 
				  "useSelectedPage();", 
				  "", 
				  "", 
				  "linkInsert", 
				  true, 
				  false, 
				  "", 
				  "", 
				  ""));

		buttons.add(new ToolbarButton("useSelectedAsset", 
				  getLocalizedString(locale, "tool.contenttool.assetDialog.chooseAttachment"), 
				  getLocalizedString(locale, "tool.contenttool.assetDialog.chooseAttachment"), 
				  "useSelectedAsset();", 
				  "", 
				  "", 
				  "linkInsertAsset", 
				  true, 
				  false, 
				  "", 
				  "", 
				  ""));

		/*
		buttons.add(new ToolbarButton("uploadAsset", 
				  getLocalizedString(locale, "tool.contenttool.uploadNewAttachment"), 
				  getLocalizedString(locale, "tool.contenttool.uploadNewAttachment"), 
				  "openPopup('ViewDigitalAsset.action?contentVersionId=" + contentVersionId + "', 'DigitalAsset', 'width=400,height=200,resizable=no');", 
				  "", 
				  "", 
				  "attachAsset", 
				  true, 
				  false, 
				  "", 
				  "", 
				  "",
				  300,
				  200));
		*/
		
		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "onCancel();", true));

		return buttons;
	}
	
	
	private List<ToolbarButton> getContentVersionWizardFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
									  getLocalizedString(locale, "tool.common.nextButton.label"), 
									  getLocalizedString(locale, "tool.common.nextButton.label"),
									  "javascript:validateAndSubmitContentForm();",
									  "images/v3/saveInlineIcon.gif",
									  "save"));
		
		buttons.add(new ToolbarButton("",
				  					  getLocalizedString(locale, "tool.contenttool.cancel.label"), 
				  					  getLocalizedString(locale, "tool.contenttool.cancel.label"),
				  					  "if(parent && parent.closeInlineDiv) parent.parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
				  					  "images/v3/cancelIcon.gif",
				  					  "left",
									  "cancel",
				  					  true));
		
		return buttons;
	}

	private List<ToolbarButton> getSiteNodeButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		String siteNodeId = request.getParameter("siteNodeId");
		//String repositoryId = request.getParameter("repositoryId");
		SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(new Integer(siteNodeId));
		
		SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(new Integer(siteNodeId));

		buttons.add(new ToolbarButton("createSiteNode",
				  getLocalizedString(locale, "tool.structuretool.toolbarV3.createPageLabel"), 
				  getLocalizedString(locale, "tool.structuretool.toolbarV3.createPageTitle"),
				  "CreateSiteNode!inputV3.action?isBranch=true&repositoryId=" + siteNodeVO.getRepositoryId() + "&parentSiteNodeId=" + siteNodeId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "create"));

		ToolbarButton moveSiteNodeButton = new ToolbarButton("moveSiteNode",
				  getLocalizedString(locale, "tool.structuretool.toolbarV3.movePageLabel"), 
				  getLocalizedString(locale, "tool.structuretool.toolbarV3.movePageTitle"),
				  "MoveSiteNode!inputV3.action?repositoryId=" + siteNodeVO.getRepositoryId() + "&siteNodeId=" + siteNodeId + "&hideLeafs=true&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "movePage");


		ToolbarButton moveMultipleSiteNodeButton = new ToolbarButton("moveMultipleSiteNode",
				  getLocalizedString(locale, "tool.structuretool.toolbarV3.moveMultiplePageLabel"), 
				  getLocalizedString(locale, "tool.structuretool.toolbarV3.moveMultiplePageTitle"),
				  "MoveMultipleSiteNodes!input.action?repositoryId=" + siteNodeVO.getRepositoryId() + "&siteNodeId=" + siteNodeId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "",
				  "movePage");
		
		moveSiteNodeButton.getSubButtons().add(moveMultipleSiteNodeButton);

		buttons.add(moveSiteNodeButton);
				
		//if(!hasPublishedVersion())
		//{
			buttons.add(new ToolbarButton("deleteSiteNode",
					  getLocalizedString(locale, "tool.structuretool.toolbarV3.deletePageLabel"), 
					  getLocalizedString(locale, "tool.structuretool.toolbarV3.deletePageTitle"),
					  "DeleteSiteNode!V3.action?siteNodeId=" + siteNodeId + "&repositoryId=" + siteNodeVO.getRepositoryId() + "&changeTypeId=4&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
					  "",
					  "",
					  "delete",
					  true,
					  true,
					  getLocalizedString(locale, "tool.structuretool.toolbarV3.deletePageLabel"), 
					  getLocalizedString(locale, "tool.structuretool.toolbarV3.deletePageConfirmationLabel", new String[]{siteNodeVO.getName()}),
					  "inlineDiv"));
			//}
		/*
		else
		{
			buttons.add(new ToolbarButton("deleteSiteNode",
					  getLocalizedString(locale, "tool.structuretool.toolbarV3.deletePageLabel"), 
					  getLocalizedString(locale, "tool.structuretool.toolbarV3.deletePageTitle"),
					  "alert('" + getLocalizedString(locale, "tool.structuretool.deleteSiteNode.unpublishFirst") + "');",
					  "",
					  "",
					  "delete",
					  true,
					  false,
					  "", 
					  "",
					  ""));
		}
		*/
			
		ToolbarButton pageMetaDataButton = new ToolbarButton("",
			getLocalizedString(locale, "tool.structuretool.toolbarV3.editPageMetaInfoLabel"), 
			getLocalizedString(locale, "tool.structuretool.toolbarV3.editPageMetaInfoTitle"),
			"ViewAndCreateContentForServiceBinding.action?siteNodeId=" + siteNodeId + "&repositoryId=" + siteNodeVO.getRepositoryId() + "&siteNodeVersionId=" + siteNodeVersionVO.getId() + "&hideLeafs=true&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
			"",
			"properties");

		ToolbarButton pageDetailButton = StructureToolbarController.getPageDetailButtons(siteNodeVO.getRepositoryId(), new Integer(siteNodeId), locale, principal);
		pageMetaDataButton.getSubButtons().add(pageDetailButton);
		ToolbarButton pageDetailSimpleButton = StructureToolbarController.getPageDetailSimpleButtons(siteNodeVO.getRepositoryId(), new Integer(siteNodeId), locale, principal);
		pageMetaDataButton.getSubButtons().add(pageDetailSimpleButton);
		buttons.add(pageMetaDataButton);
		
		buttons.add(StructureToolbarController.getPreviewButtons(siteNodeVO.getRepositoryId(), new Integer(siteNodeId), locale));

		ToolbarButton publishButton = StructureToolbarController.getPublishCurrentNodeButton(siteNodeVO.getRepositoryId(), new Integer(siteNodeId), locale);
		ToolbarButton publishStructureButton = StructureToolbarController.getPublishButtons(siteNodeVO.getRepositoryId(), new Integer(siteNodeId), locale);
		publishButton.getSubButtons().add(publishStructureButton);
		buttons.add(publishButton);
		
		ToolbarButton unpublishButton = StructureToolbarController.getUnpublishButton(siteNodeVO.getRepositoryId(), new Integer(siteNodeId), locale, true);
		//ToolbarButton unpublishStructureButton = StructureToolbarController.getUnpublishButton(siteNodeVO.getRepositoryId(), new Integer(siteNodeId), locale, true);

		publishButton.getSubButtons().add(unpublishButton);
		//publishButton.getSubButtons().add(unpublishStructureButton);
		/*
		unpublishButton.getSubButtons().add(unpublishStructureButton);
		buttons.add(unpublishButton);
		*/
		
		//if(siteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES.intValue())
		//{
			buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.accessRights.accessRightsButtonLabel"), 
				  getLocalizedString(locale, "tool.common.accessRights.accessRightsButtonTitle"),
				  "ViewAccessRights!V3.action?interceptionPointCategory=SiteNodeVersion&extraParameters=" + siteNodeVersionVO.getId() + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "images/v3/accessRightIcon.gif",
				  "accessRights"));
		//}

		buttons.add(new ToolbarButton("",
			  getLocalizedString(locale, "tool.contenttool.toolbarV3.runTaskLabel"), 
			  getLocalizedString(locale, "tool.contenttool.toolbarV3.runTaskTitle"),
			  "ViewExecuteTask.action?siteNodeId=" + siteNodeId + "",
			  "",
			  "runTask"));

		return buttons;

		/*
		buttons.add(new ImageButton(this.getCMSBaseUrl() + "/CreateSiteNode!input.action?isBranch=true&parentSiteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.newSiteNode"), "New SiteNode"));	
		
		ImageButton moveButton = getMoveButton();
		moveButton.getSubButtons().add(getMoveMultipleButton());
		buttons.add(moveButton);	

		
		if(this.siteNodeVersionVO != null && this.siteNodeVersionVO.getStateId().equals(SiteNodeVersionVO.WORKING_STATE))
			buttons.add(new ImageButton(true, "javascript:openPopup('ViewAndCreateContentForServiceBinding.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&siteNodeVersionId=" + this.siteNodeVersionVO.getId() + "', 'PageProperties', 'width=750,height=700,resizable=no,status=yes,scrollbars=yes');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.editSiteNodeProperties"), "Edit siteNode properties"));
		else if(this.siteNodeVersionVO != null)
			buttons.add(new ImageButton(true, "javascript:openPopupWithOptionalParameter('ViewAndCreateContentForServiceBinding.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&siteNodeVersionId=" + this.siteNodeVersionVO.getId() + "', 'PageProperties', 'width=750,height=700,resizable=no,status=yes,scrollbars=yes', '" + getLocalizedString(getSession().getLocale(), "tool.structuretool.changeSiteNodeStateToWorkingQuestion") + "', 'changeStateToWorking=true');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.editSiteNodeProperties"), "Edit siteNode properties"));

		buttons.add(getPreviewButtons());
		
		if(hasPublishedVersion())
		{
		    ImageButton unpublishButton = new ImageButton(this.getCMSBaseUrl() + "/UnpublishSiteNodeVersion!input.action?siteNodeId=" + this.siteNodeId + "&siteNodeVersionId=" + this.siteNodeVersionId, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.unpublishVersion"), "tool.contenttool.unpublishVersion.header");
		    ImageButton unpublishAllButton = new ImageButton(this.getCMSBaseUrl() + "/UnpublishSiteNodeVersion!inputChooseSiteNodes.action?siteNodeId=" + this.siteNodeId + "&siteNodeVersionId=" + this.siteNodeVersionId, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.unpublishAllVersion"), "tool.contenttool.unpublishAllVersion.header");
		    unpublishButton.getSubButtons().add(unpublishAllButton);
		
		    buttons.add(unpublishButton);
		}
		
		ImageButton coverButton = new ImageButton(this.getCMSBaseUrl() + "/ViewSiteNode.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&stay=true", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeCover"), "SiteNode Cover");	
		coverButton.getSubButtons().add(getSimplePageComponentsButton());
		buttons.add(coverButton);	

		if(!isReadOnly())
		{
		    ImageButton pageComponentsButton = getViewPageComponentsButton();
		    pageComponentsButton.getSubButtons().add(getSimplePageComponentsButton());
		    buttons.add(pageComponentsButton);	
		}
		
		ImageButton publishButton = getPublishCurrentNodeButton();
	    publishButton.getSubButtons().add(getPublishButton());
	    buttons.add(publishButton);	
	    		
		buttons.add(getExecuteTaskButton());

		if(this.siteNodeVersionVO != null && this.siteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES.intValue())
			buttons.add(getAccessRightsButton());	
		*/
	}
	
	private List<ToolbarButton> getCreateSiteNodeButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.save.label"), 
				  getLocalizedString(locale, "tool.contenttool.save.label"),
				  "javascript:validateAndSubmitContentForm();",
				  "images/v3/saveInlineIcon.gif",
				  "save"));
	
		return buttons;
	}
	
	private List<ToolbarButton> getSiteNodeFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		buttons.addAll(getCommonFooterSaveOrSaveAndExitOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "UpdateSiteNode!saveAndExitV3Inline.action"));
		
		buttons.add(getCompareButton(toolbarKey, principal, locale, request, disableCloseButton));

		return buttons;
	}

	private List<ToolbarButton> getSiteNodeHistoryFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCompareButton(toolbarKey, principal, locale, request, disableCloseButton));
				
		return buttons;
	}

	private List<ToolbarButton> getContentHistoryFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCompareButton(toolbarKey, principal, locale, request, disableCloseButton));
				
		return buttons;
	}

	private List<ToolbarButton> getContentRelationFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.addButton.label"), 
				  getLocalizedString(locale, "tool.common.addButton.label"),
				  "add();",
				  "images/v3/addIcon.png",
				  "left",
				  "add",
				  true));

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton));
				
		return buttons;		
	}

	private ToolbarButton getCompareButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		return new ToolbarButton("compare",
									  getLocalizedString(locale, "tool.common.compare.label"), 
									  getLocalizedString(locale, "tool.common.compare.label"),
									  "compare();",
									  "images/v3/compareIcon.gif",
									  "left",
									  "compare",
									  true);
	}

	private List<ToolbarButton> getCreateSiteNodeFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton));

		return buttons;
	}

	private List<ToolbarButton> getCreatePageTemplateFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		//Fix this dialog later
		//buttons.addAll(getCommonNextCancelButton(toolbarKey, principal, locale, request, disableCloseButton));

		return buttons;
	}

	private List<ToolbarButton> getMySettingsFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton));
						
		return buttons;
	}

	private List<ToolbarButton> getTrashcanFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.emptyTrashButton.label"), 
				  getLocalizedString(locale, "tool.common.emptyTrashButton.label"),
				  "emptyTrash();",
				  "",
				  "left",
				  "clearTrashcan",
				  true));

		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton));
						
		return buttons;
	}

	private List<ToolbarButton> getInstallUpgradeDatabaseFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.upgradeButton.label"), 
				  getLocalizedString(locale, "tool.common.upgradeButton.label"),
				  "upgrade();",
				  "",
				  "left",
				  "runTask",
				  true));

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.nextButton.label"), 
				  getLocalizedString(locale, "tool.common.nextButton.label"),
				  "next();",
				  "images/v3/nextBackground.gif",
				  "left",
				  "next",
				  true));
		
		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton));
						
		return buttons;
	}

	private List<ToolbarButton> getMessageCenterFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
									  getLocalizedString(locale, "tool.common.nextButton.label"), 
									  getLocalizedString(locale, "tool.common.nextButton.label"),
									  "submitForm();",
									  "images/v3/nextBackground.gif",
				  					  "left",
									  "next",
									  true));

		buttons.add(new ToolbarButton("",
				  					  getLocalizedString(locale, "tool.common.cancelButton.label"), 
				  					  getLocalizedString(locale, "tool.common.cancelButton.label"),
				  					  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
				  					  "images/v3/cancelIcon.gif",
				  					  "left",
				  					  "cancel",
				  					  true));

		return buttons;
	}

	private List<ToolbarButton> getMessageCenterComposeEmailFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
									  getLocalizedString(locale, "tool.managementtool.sendMessage.label"), 
									  getLocalizedString(locale, "tool.managementtool.sendMessage.label"),
									  "submitForm();",
									  "images/v3/nextBackground.gif",
				  					  "left",
									  "commit",
									  true));

		buttons.add(new ToolbarButton("",
				  					  getLocalizedString(locale, "tool.common.cancelButton.label"), 
				  					  getLocalizedString(locale, "tool.common.cancelButton.label"),
				  					  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
				  					  "images/v3/cancelIcon.gif",
				  					  "left",
				  					  "cancel",
				  					  true));

		return buttons;
	}

	private List<ToolbarButton> getSaveCancelFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton));


		return buttons;
	}

	private List<ToolbarButton> getSaveCancelFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton, String javascriptCode)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton, javascriptCode, null, null));
		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton));


		return buttons;
	}

	private List<ToolbarButton> getPublishPageFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		SiteNodeVO siteNodeVO = null;
		
		Integer primaryKeyAsInteger = null;
		try
		{
			primaryKeyAsInteger = new Integer(request.getParameter("siteNodeId"));
		}
		catch (Exception e) 
		{
		}

		if(primaryKeyAsInteger != null)
			siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(primaryKeyAsInteger);
			
		if(siteNodeVO != null && hasAccessTo(principal, "PublishingTool.Read", true) && hasAccessTo(principal, "Repository.Read", "" + siteNodeVO.getRepositoryId()))
		{
			buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.common.publishing.publishButtonLabel"), 
					  getLocalizedString(locale, "tool.common.publishing.publishButtonLabel"),
					  "submitToPublish('true');",
					  "images/v3/publishPageIcon.gif",
					  "left",
					  "publish",
					  true));
		}
		
		if(siteNodeVO != null && hasAccessTo(principal, "Common.SubmitToPublishButton", true))
		{
			buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.common.publishing.submitToPublishButtonLabel"), 
					  getLocalizedString(locale, "tool.common.publishing.submitToPublishButtonLabel"),
					  "submitToPublish('false');",
					  "images/v3/publishPageIcon.gif",
					  "left",
					  "submitToPublish",
					  true));
		}
		
		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.cancelButton.label"), 
				  getLocalizedString(locale, "tool.common.cancelButton.label"),
				  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
				  "images/v3/cancelIcon.gif",
				  "left",
				  "cancel",
				  true));
		
		return buttons;
	}
	
	private List<ToolbarButton> getUnpublishPageFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		SiteNodeVO siteNodeVO = null;
		
		Integer primaryKeyAsInteger = null;
		try
		{
			primaryKeyAsInteger = new Integer(request.getParameter("siteNodeId"));
		}
		catch (Exception e) 
		{
		}

		if(primaryKeyAsInteger != null)
			siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(primaryKeyAsInteger);
			
		if(siteNodeVO != null && hasAccessTo(principal, "PublishingTool.Read", true) && hasAccessTo(principal, "Repository.Read", "" + siteNodeVO.getRepositoryId()))
		{
			buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.common.unpublishing.unpublishButtonLabel"), 
					  getLocalizedString(locale, "tool.common.unpublishing.unpublishButtonLabel"),
					  "submitToPublish('true');",
					  "images/v3/unpublishPageIcon.gif",
					  "left",
					  "publish",
					  true));
		}
		
		if(siteNodeVO != null && hasAccessTo(principal, "Common.SubmitToPublishButton", true))
		{
			buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.common.unpublishing.submitToUnpublishButtonLabel"), 
					  getLocalizedString(locale, "tool.common.unpublishing.submitToUnpublishButtonLabel"),
					  "submitToPublish('false');",
					  "images/v3/unpublishPageIcon.gif",
					  "left",
					  "submitToPublish",
					  true));
		}
		
		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.cancelButton.label"), 
				  getLocalizedString(locale, "tool.common.cancelButton.label"),
				  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
				  "images/v3/cancelIcon.gif",
				  "left",
				  "cancel",
				  true));
		
		return buttons;
	}

	private List<ToolbarButton> getPublishContentsFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		ContentVO contentVO = null;
		
		Integer primaryKeyAsInteger = null;
		try
		{
			primaryKeyAsInteger = new Integer(request.getParameter("contentId"));
		}
		catch (Exception e) 
		{
		}

		if(primaryKeyAsInteger != null)
			contentVO = ContentController.getContentController().getContentVOWithId(primaryKeyAsInteger);
			
		if(contentVO != null && hasAccessTo(principal, "PublishingTool.Read", true) && hasAccessTo(principal, "Repository.Read", "" + contentVO.getRepositoryId()))
		{
			buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.common.publishing.publishButtonLabel"), 
					  getLocalizedString(locale, "tool.common.publishing.publishButtonLabel"),
					  "submitToPublish('true');",
					  "images/v3/publishContentIcon.gif",
					  "left",
					  "publish",
					  true));
		}
		
		if(contentVO != null && hasAccessTo(principal, "Common.SubmitToPublishButton", true))
		{
			buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.common.publishing.submitToPublishButtonLabel"), 
					  getLocalizedString(locale, "tool.common.publishing.submitToPublishButtonLabel"),
					  "submitToPublish('false');",
					  "images/v3/publishContentIcon.gif",
					  "left",
					  "submitToPublish",
					  true));
		}
		
		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.cancelButton.label"), 
				  getLocalizedString(locale, "tool.common.cancelButton.label"),
				  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
				  "images/v3/cancelIcon.gif",
				  "left",
				  "cancel",
				  true));
		
		return buttons;
	}

	private List<ToolbarButton> getUnPublishContentsFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		ContentVO contentVO = null;
		
		Integer primaryKeyAsInteger = null;
		try
		{
			primaryKeyAsInteger = new Integer(request.getParameter("contentId"));
		}
		catch (Exception e) 
		{
		}

		if(primaryKeyAsInteger != null)
			contentVO = ContentController.getContentController().getContentVOWithId(primaryKeyAsInteger);
			
		if(contentVO != null && hasAccessTo(principal, "PublishingTool.Read", true) && hasAccessTo(principal, "Repository.Read", "" + contentVO.getRepositoryId()))
		{
			buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.common.unpublishing.unpublishButtonLabel"), 
					  getLocalizedString(locale, "tool.common.unpublishing.unpublishButtonLabel"),
					  "submitToPublish('true');",
					  "images/v3/publishPageIcon.gif",
					  "left",
					  "publish",
					  true));
		}
		
		if(contentVO != null && hasAccessTo(principal, "Common.SubmitToPublishButton", true))
		{
			buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.common.unpublishing.submitToUnpublishButtonLabel"), 
					  getLocalizedString(locale, "tool.common.unpublishing.submitToUnpublishButtonLabel"),
					  "submitToPublish('false');",
					  "images/v3/publishPageIcon.gif",
					  "left",
					  "submitToPublish",
					  true));
		}
		
		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.cancelButton.label"), 
				  getLocalizedString(locale, "tool.common.cancelButton.label"),
				  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
				  "images/v3/cancelIcon.gif",
				  "left",
				  "cancel",
				  true));
		
		return buttons;
	}

	private List<ToolbarButton> getGlobalSubscriptionsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.createSubscription.header"), 
				  getLocalizedString(locale, "tool.common.createSubscription.header"),
				  "showDiv('newSubscriptionForm')",
				  "images/v3/createBackgroundPenPaper.gif",
				  "left",
				  "create",
				  true));
		
		return buttons;
	}

	
	
	private List<ToolbarButton> getSystemUsersButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		if(UserControllerProxy.getController().getSupportCreate())
		{
			boolean hasAccessToCreateRole = hasAccessTo(principal, "SystemUser.Create", true);
			if(hasAccessToCreateRole)
			{
				buttons.add(new ToolbarButton("",
											  getLocalizedString(locale, "tool.managementtool.createSystemUser.header"), 
											  getLocalizedString(locale, "tool.managementtool.createSystemUser.header"),
											  "CreateSystemUser!input.action",
											  "images/v3/createBackgroundPenPaper.gif",
											  "create",
											  "workIframe"));
			}
		}
		
		/*		
		buttons.add(new ToolbarButton(true, "javascript:toggleSearchForm();", getLocalizedString(locale, "images.managementtool.buttons.searchButton"), "Search Form"));
		*/
		
		return buttons;
	}

	private List<ToolbarButton> getSystemUserDetailsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		String userName = request.getParameter("userName");
		
		logger.info("userName:" + userName);
		if(!UserControllerProxy.getController().userExists(userName))
		{
			logger.info("userName did not exist - we try to decode it:" + userName);
			byte[] bytes = Base64.decodeBase64(userName);
			String decodedUserName = new String(bytes, "utf-8");
			logger.info("decodedUserName:" + decodedUserName);
			if(UserControllerProxy.getController().userExists(decodedUserName))
			{
				logger.info("decodedUserName existed:" + decodedUserName);
				userName = decodedUserName;
			}
		}

		if(!userName.equals(CmsPropertyHandler.getAnonymousUser()))
		{
			InfoGluePrincipal user = UserControllerProxy.getController().getUser(userName);
			if(user.getAutorizationModule().getSupportDelete())
			{
				buttons.add(new ToolbarButton("",
						getLocalizedString(locale, "tool.managementtool.deleteSystemUser.header"), 
						getLocalizedString(locale, "tool.managementtool.deleteSystemUser.header"),
						"DeleteSystemUser.action?userName=" + formatter.encodeBase64(userName) + "&igSecurityCode=" + request.getSession().getAttribute("securityCode"),
						"images/v3/createBackgroundPenPaper.gif",
						"left",
						"delete",
						false,
						true,
						getLocalizedString(locale, "tool.managementtool.deleteSystemUser.header"),
						getLocalizedString(locale, "tool.managementtool.deleteSystemUser.text", new String[]{userName}),
				  		"workIframe"));
			}
			
			if(user.getAutorizationModule().getSupportUpdate())
			{
				buttons.add(new ToolbarButton("",
						  getLocalizedString(locale, "tool.managementtool.viewSystemUserPasswordDialog.header"), 
						  getLocalizedString(locale, "tool.managementtool.viewSystemUserPasswordDialog.header"),
						  "UpdateSystemUserPassword!input.action?userName=" + formatter.encodeBase64(userName) + "&igSecurityCode=" + request.getSession().getAttribute("securityCode"),
						  "images/v3/passwordIcon.gif",
						  "accessRights",
						  "workIframe"));

				//buttons.add(new ToolbarButton("UpdateSystemUserPassword!input.action?userName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding), URIEncoding), getLocalizedString(locale, "images.managementtool.buttons.updateSystemUserPassword"), "Update user password"));
			}
		}
		
		List contentTypeDefinitionVOList = UserPropertiesController.getController().getContentTypeDefinitionVOList(userName);
		if(contentTypeDefinitionVOList.size() > 0)
		{
			buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.managementtool.viewUserProperties.header"), 
					  getLocalizedString(locale, "tool.managementtool.viewUserProperties.header"),
					  "ViewUserProperties.action?userName=" + formatter.encodeBase64(userName),
					  "images/v3/advancedSettingsIcon.gif",
					  "properties",
					  "workIframe"));

			//buttons.add(new ToolbarButton("ViewUserProperties.action?userName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding), URIEncoding), getLocalizedString(locale, "images.managementtool.buttons.viewSystemUserProperties"), "View User Properties"));
		}
		
		if(principal.getIsAdministrator())
		{
			buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.managementtool.transferAccessRights.header"), 
					  getLocalizedString(locale, "tool.managementtool.transferAccessRights.header"),
					  "AuthorizationSwitchManagement!inputUser.action?userName=" + formatter.encodeBase64(userName),
					  "images/v3/createBackgroundPenPaper.gif",
					  "create",
					  "workIframe"));
		}

		return buttons;				
	}
	
	private List<ToolbarButton> getRolesButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		if(RoleControllerProxy.getController().getSupportCreate())
		{
			boolean hasAccessToCreateRole = hasAccessTo(principal, "Role.Create", true);
			if(hasAccessToCreateRole)
			{
				buttons.add(new ToolbarButton("",
											  getLocalizedString(locale, "tool.managementtool.createRole.header"), 
											  getLocalizedString(locale, "tool.managementtool.createRole.header"),
											  "CreateRole!input.action",
											  "images/v3/createBackgroundPenPaper.gif",
											  "create",
											  "workIframe"));
			}
		}
		
		return buttons;
	}
	
	private List<ToolbarButton> getRoleDetailsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		String roleName = request.getParameter("roleName");
		
		logger.info("roleName:" + roleName);
		if(!RoleControllerProxy.getController().roleExists(roleName))
		{
			logger.info("roleName did not exist - we try to decode it:" + roleName);
			byte[] bytes = Base64.decodeBase64(roleName);
			String decodedRoleName = new String(bytes, "utf-8");
			logger.info("decodedRoleName:" + decodedRoleName);
			if(RoleControllerProxy.getController().roleExists(decodedRoleName))
			{
				logger.info("decodedRoleName existed:" + decodedRoleName);
				roleName = decodedRoleName;
			}
		}
		
		InfoGlueRole role = RoleControllerProxy.getController().getRole(roleName);
		if(role.getAutorizationModule().getSupportDelete())
		{
			//boolean hasAccessToDeleteRole = hasAccessTo(principal, "Role.Delete", "" + roleName);
			//if(hasAccessToDeleteRole)
			//{
				buttons.add(new ToolbarButton("",
						getLocalizedString(locale, "tool.managementtool.deleteRole.header"), 
						getLocalizedString(locale, "tool.managementtool.deleteRole.header"),
						"DeleteRole.action?roleName=" + formatter.encodeBase64(roleName) + "&igSecurityCode=" + request.getSession().getAttribute("securityCode"),
						"images/v3/createBackgroundPenPaper.gif",
						"left",
						"delete",
						false,
						true,
						getLocalizedString(locale, "tool.managementtool.deleteRole.header"),
						getLocalizedString(locale, "tool.managementtool.deleteRole.text", new String[]{roleName}),
				  		"workIframe"));
			//}
		}
		
		List contentTypeDefinitionVOList = RolePropertiesController.getController().getContentTypeDefinitionVOList(roleName);
		if(contentTypeDefinitionVOList.size() > 0)
		{
			//boolean hasAccessToEditProperties = hasAccessTo(principal, "Role.EditProperties", true);
			//if(hasAccessToEditProperties)
			//{
				buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.managementtool.viewRoleProperties.header"), 
					  getLocalizedString(locale, "tool.managementtool.viewRoleProperties.header"),
					  "ViewRoleProperties.action?roleName=" + formatter.encodeBase64(roleName),
					  "images/v3/advancedSettingsIcon.gif",
					  "properties",
					  "workIframe"));
				//}
		}

		/*
		boolean hasAccessToManageAllAccessRights = hasAccessTo(principal, "Role.ManageAllAccessRights", true);
		boolean hasAccessToManageAccessRights = hasAccessTo(principal, "Role.ManageAccessRights", "" + roleName);
		if(hasAccessToManageAllAccessRights || hasAccessToManageAccessRights)
		{
			buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.accessRights.header"), 
				  getLocalizedString(locale, "tool.contenttool.accessRights.header"),
				  "ViewAccessRights.action?interceptionPointCategory=Role&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding) + "&returnAddress=ViewRole!v3.action?roleName=" + URLEncoder.encode(primaryKey, URIEncoding) + "&colorScheme=ManagementTool",
				  "images/v3/accessRightsIcon.gif",
				  "accessRights"));
		}
		*/
		/*
		if(principal.getIsAdministrator())
			buttons.add(new ToolbarButton("AuthorizationSwitchManagement!inputRole.action?roleName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding)), getLocalizedString(locale, "images.managementtool.buttons.transferRoleAccessRights"), "Transfer Roles Access Rights"));
		*/

		return buttons;				
	}

	
	private List<ToolbarButton> getGroupsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		if(GroupControllerProxy.getController().getSupportCreate())
		{
			//boolean hasAccessToCreateGroup = hasAccessTo(principal, "Group.Create", true);
			//if(hasAccessToCreateGroup)
			//{
				buttons.add(new ToolbarButton("",
											  getLocalizedString(locale, "tool.managementtool.createGroup.header"), 
											  getLocalizedString(locale, "tool.managementtool.createGroup.header"),
											  "CreateGroup!input.action",
											  "images/v3/createBackgroundPenPaper.gif",
											  "create",
											  "workIframe"));
			//}
		}
		
		return buttons;
	}

	private List<ToolbarButton> getGroupDetailsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		String groupName = request.getParameter("groupName");
		
		logger.info("groupName:" + groupName);
		if(!GroupControllerProxy.getController().groupExists(groupName))
		{
			logger.info("groupName did not exist - we try to decode it:" + groupName);
			byte[] bytes = Base64.decodeBase64(groupName);
			String decodedGroupName = new String(bytes, "utf-8");
			logger.info("decodedGroupName:" + decodedGroupName);
			if(GroupControllerProxy.getController().groupExists(decodedGroupName))
			{
				logger.info("decodedGroupName existed:" + decodedGroupName);
				groupName = decodedGroupName;
			}
		}

		InfoGlueGroup group = GroupControllerProxy.getController().getGroup(groupName);
		if(group.getAutorizationModule().getSupportDelete())
		{
			//boolean hasAccessToDeleteGroup = hasAccessTo(principal, "Group.Delete", "" + primaryKey);
			//if(hasAccessToDeleteGroup)
			//{
				buttons.add(new ToolbarButton("",
						getLocalizedString(locale, "tool.managementtool.deleteGroup.header"), 
						getLocalizedString(locale, "tool.managementtool.deleteGroup.header"),
						"DeleteGroup.action?groupName=" + formatter.encodeBase64(groupName) + "&igSecurityCode=" + request.getSession().getAttribute("securityCode"),
						"images/v3/createBackgroundPenPaper.gif",
						"left",
						"delete",
						false,
						true,
						getLocalizedString(locale, "tool.managementtool.deleteGroup.header"),
						getLocalizedString(locale, "tool.managementtool.deleteGroup.text", new String[]{groupName}),
				  		"workIframe"));
				//}
		}
		
		List contentTypeDefinitionVOList = GroupPropertiesController.getController().getContentTypeDefinitionVOList(groupName);
		if(contentTypeDefinitionVOList.size() > 0)
		{
			//boolean hasAccessToEditProperties = hasAccessTo(principal, "Group.EditProperties", true);
			//if(hasAccessToEditProperties)
			//{
				buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.managementtool.viewGroupProperties.header"), 
					  getLocalizedString(locale, "tool.managementtool.viewGroupProperties.header"),
					  "ViewGroupProperties.action?groupName=" + formatter.encodeBase64(groupName),
					  "images/v3/advancedSettingsIcon.gif",
					  "properties",
					  "workIframe"));
				//}
		}

		/*
		boolean hasAccessToManageAllAccessRights = hasAccessTo(principal, "Group.ManageAllAccessRights", true);
		boolean hasAccessToManageAccessRights = hasAccessTo(principal, "Group.ManageAccessRights", "" + groupName);
		if(hasAccessToManageAllAccessRights || hasAccessToManageAccessRights)
		{
			buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.accessRights.header"), 
				  getLocalizedString(locale, "tool.contenttool.accessRights.header"),
				  "ViewAccessRights.action?interceptionPointCategory=Group&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding) + "&returnAddress=ViewGroup!v3.action?groupName=" + URLEncoder.encode(primaryKey, URIEncoding) + "&colorScheme=ManagementTool",
				  "images/v3/accessRightsIcon.gif",
				  "accessRights"));
		}
		*/
		/*
		if(principal.getIsAdministrator())
			buttons.add(new ToolbarButton("AuthorizationSwitchManagement!inputGroup.action?groupName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding)), getLocalizedString(locale, "images.managementtool.buttons.transferGroupAccessRights"), "Transfer Groups Access Rights"));
		*/

		return buttons;				
	}

	private List<ToolbarButton> getGroupPropertiesButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		String entityId = request.getParameter("entityId");
		
		if(entityId != null && !entityId.equals(""))
		{
			buttons.add(new ToolbarButton("uploadAsset", 
					  getLocalizedString(locale, "tool.contenttool.uploadNewAttachment"), 
					  getLocalizedString(locale, "tool.contenttool.uploadNewAttachment"), 
					  "ViewDigitalAsset.action?entity=org.infoglue.cms.entities.management.GroupProperties&entityId=" + entityId + "",
					  "", 
					  "", 
					  "attachAsset", 
					  false, 
					  false, 
					  "", 
					  "", 
					  "inlineDiv",
					  500,
					  550));
		}
		
		return buttons;
	}

	private List<ToolbarButton> getRolePropertiesButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		String entityId = request.getParameter("entityId");
		
		if(entityId != null && !entityId.equals(""))
		{
			buttons.add(new ToolbarButton("uploadAsset", 
					  getLocalizedString(locale, "tool.contenttool.uploadNewAttachment"), 
					  getLocalizedString(locale, "tool.contenttool.uploadNewAttachment"), 
					  "ViewDigitalAsset.action?entity=org.infoglue.cms.entities.management.RoleProperties&entityId=" + entityId + "",
					  "", 
					  "", 
					  "attachAsset", 
					  false, 
					  false, 
					  "", 
					  "", 
					  "inlineDiv",
					  500,
					  550));
		}
		
		return buttons;
	}

	private List<ToolbarButton> getUserPropertiesButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		String entityId = request.getParameter("entityId");
		
		if(entityId != null && !entityId.equals(""))
		{
			buttons.add(new ToolbarButton("uploadAsset", 
					  getLocalizedString(locale, "tool.contenttool.uploadNewAttachment"), 
					  getLocalizedString(locale, "tool.contenttool.uploadNewAttachment"), 
					  "ViewDigitalAsset.action?entity=org.infoglue.cms.entities.management.UserProperties&entityId=" + entityId + "",
					  "", 
					  "", 
					  "attachAsset", 
					  false, 
					  false, 
					  "", 
					  "", 
					  "inlineDiv",
					  500,
					  550));
		}
		
		return buttons;
	}

	/*
	private List<ToolbarButton> getGroupsButtons() throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		if(UserControllerProxy.getController().getSupportCreate())
			buttons.add(new ToolbarButton("CreateGroup!input.action", getLocalizedString(locale, "images.managementtool.buttons.newGroup"), "New Group"));	
		//if(UserControllerProxy.getController().getSupportDelete())
		//	buttons.add(new ToolbarButton(true, "javascript:submitListFormWithPrimaryKey('group', 'groupName');", getLocalizedString(locale, "images.managementtool.buttons.deleteGroup"), "tool.managementtool.deleteGroups.header"));
		
		return buttons;
	}
	
	private List<ToolbarButton> getGroupDetailsButtons() throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		String yesDestination 	= URLEncoder.encode("DeleteGroup.action?groupName=" + URLEncoder.encode(primaryKey, URIEncoding), URIEncoding);
		String noDestination  	= URLEncoder.encode("ViewListGroup.action?title=Groups", URIEncoding);
		String message 		 	= URLEncoder.encode("Do you really want to delete the group " + URLEncoder.encode(primaryKey, URIEncoding), URIEncoding);
		
		InfoGlueGroup group = GroupControllerProxy.getController().getGroup(primaryKey);
		if(group.getAutorizationModule().getSupportDelete())
			buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteGroup.header&yesDestination=" + yesDestination + "&noDestination=" + noDestination + "&message=tool.managementtool.deleteGroup.text&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding), getLocalizedString(locale, "images.managementtool.buttons.deleteGroup"), "tool.managementtool.deleteGroup.header"));
		
		List<ToolbarButton> contentTypeDefinitionVOList<ToolbarButton> = GroupPropertiesController.getController().getContentTypeDefinitionVOList(primaryKey);
		if(contentTypeDefinitionVOList.size() > 0)
			buttons.add(new ToolbarButton("ViewGroupProperties.action?groupName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding)), getLocalizedString(locale, "images.managementtool.buttons.viewGroupProperties"), "View Group Properties"));
		
		if(principal.getIsAdministrator())
			buttons.add(new ToolbarButton("AuthorizationSwitchManagement!inputGroup.action?groupName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding)), getLocalizedString(locale, "images.managementtool.buttons.transferGroupAccessRights"), "Transfer Groups Access Rights"));
				
		boolean hasAccessToManageAllAccessRights = hasAccessTo(principal, "Group.ManageAllAccessRights", true);
		boolean hasAccessToManageAccessRights = hasAccessTo(principal, "Group.ManageAccessRights", "" + primaryKey);
		if(hasAccessToManageAllAccessRights || hasAccessToManageAccessRights)
			buttons.add(new ToolbarButton("ViewAccessRights.action?interceptionPointCategory=Group&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding) + "&returnAddress=ViewGroup.action?groupName=" + URLEncoder.encode(primaryKey, URIEncoding) + "&colorScheme=ManagementTool", getLocalizedString(locale, "images.managementtool.buttons.accessRights"), "Group Access Rights"));

		return buttons;				
	}
	*/
	
	private List<ToolbarButton> getRepositoriesButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.createRepository.header"), 
				  getLocalizedString(locale, "tool.managementtool.createRepository.header"),
				  "CreateRepository!inputV3.action",
				  "images/v3/createBackgroundPenPaper.gif",
				  "create",
				  "workIframe"));
		/*
		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteRepositories.header"),
				getLocalizedString(locale, "tool.managementtool.deleteRepositories.header"),
				"submitListForm('repository');",
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				true, 
				false, 
				"Delete repository?",
				"Really want to delete rep...",
				"workIframe"));
		*/
		
		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.importRepository.header"), 
				  getLocalizedString(locale, "tool.managementtool.importRepository.header"),
				  "javascript:openPopup('ImportRepository!input.action', 'Import', 'width=600,height=500,resizable=no');",
				  "",
				  "left",
				  "importContent",
				  true));


		return buttons;
	}
	
	private List<ToolbarButton> getRepositoryDetailsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		Integer repositoryId = new Integer(request.getParameter("repositoryId"));
		RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);
		
		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.deleteRepository.header"), 
				  getLocalizedString(locale, "tool.managementtool.deleteRepository.header"),
				  "DeleteRepository!markForDelete.action?repositoryId=" + repositoryId + "&igSecurityCode=" + request.getSession().getAttribute("securityCode"),
				  "images/v3/createBackgroundPenPaper.gif",
				  "left",
				  "create",
				  false,
				  true,
				  getLocalizedString(locale, "tool.managementtool.deleteRepository.header"),
				  getLocalizedString(locale, "tool.managementtool.deleteRepository.text", new String[]{repositoryVO.getName()}),
				  "workIframe"));

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.exportRepository.header"), 
				  getLocalizedString(locale, "tool.managementtool.exportRepository.header"),
				  "javascript:openPopup('ExportRepository!input.action?repositoryId=" + request.getParameter("repositoryId") + "', 'Export', 'width=600,height=500,resizable=no');",
				  "",
				  "left",
				  "exportContent",
				  true));

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.importRepositoryCopy.header"), 
				  getLocalizedString(locale, "tool.managementtool.importRepositoryCopy.header"),
				  "javascript:openPopup('ImportRepository!inputCopy.action?repositoryId=" + request.getParameter("repositoryId") + "', 'Copy', 'width=600,height=500,resizable=no');",
				  "",
				  "left",
				  "importContent",
				  true));

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.repositoryProperties.header"), 
				  getLocalizedString(locale, "tool.managementtool.repositoryProperties.header"),
				  "ViewRepositoryProperties.action?repositoryId=" + request.getParameter("repositoryId"),
				  "",
				  "properties",
				  "workIframe"));

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.accessRights.header"), 
				  getLocalizedString(locale, "tool.contenttool.accessRights.header"),
				  "ViewAccessRights!V3.action?interceptionPointCategory=Repository&extraParameters=" + request.getParameter("repositoryId") + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "images/v3/accessRightIcon.gif",
				  "accessRights",
				  "inlineDiv"));
		
		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.repositoryLanguages.header"), 
				  getLocalizedString(locale, "tool.managementtool.repositoryLanguages.header"),
				  "ViewListRepositoryLanguage.action?repositoryId=" + request.getParameter("repositoryId") + "&returnAddress=ViewRepository.action?repositoryId=" + request.getParameter("repositoryId"),
				  "",
				  "changeLanguage",
				  "workIframe"));

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.rebuildRegistry.header"), 
				  getLocalizedString(locale, "tool.managementtool.rebuildRegistry.header"),
				  "javascript:openPopup('RebuildRegistry!input.action?repositoryId=" + request.getParameter("repositoryId") + "', 'Registry', 'width=400,height=200,resizable=no');",
				  "",
				  "left",
				  "runTask",
				  true));
		
		return buttons;
	}

	private List<ToolbarButton> getLanguagesButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.createLanguage.header"), 
				  getLocalizedString(locale, "tool.managementtool.createLanguage.header"),
				  "CreateLanguage!input.action",
				  "images/v3/createBackgroundPenPaper.gif",
				  "create",
				  "workIframe"));
		/*
		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteLanguages.header"),
				getLocalizedString(locale, "tool.managementtool.deleteLanguages.header"),
				"submitListForm('language');",
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				true, 
				false, 
				"Delete repository?",
				"Really want to delete rep...",
				"workIframe"));
		*/
		
		return buttons;
	}

	private List<ToolbarButton> getLanguageDetailsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		Integer languageId = new Integer(request.getParameter("languageId"));
		LanguageVO languageVO = (LanguageVO)LanguageController.getController().getLanguageVOWithId(languageId);

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.deleteLanguage.header"), 
				  getLocalizedString(locale, "tool.managementtool.deleteLanguage.header"),
				  "DeleteLanguage.action?languageId=" + languageId + "&igSecurityCode=" + request.getSession().getAttribute("securityCode"),
				  "images/v3/createBackgroundPenPaper.gif",
				  "left",
				  "create",
				  false,
				  true,
				  getLocalizedString(locale, "tool.managementtool.deleteLanguage.header"),
				  getLocalizedString(locale, "tool.managementtool.deleteLanguage.text", new String[]{languageVO.getName()}),
				  "workIframe"));

		return buttons;
	}

	private List<ToolbarButton> getCategoryButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		String url = "CategoryManagement!new.action";
		String categoryIdString = request.getParameter("categoryId");
		if(categoryIdString != null && !categoryIdString.equals(""))
			url += "?model/parentId=" + categoryIdString;

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.createCategory.header"), 
				  getLocalizedString(locale, "tool.managementtool.createCategory.header"),
				  url,
				  "images/v3/createBackgroundPenPaper.gif",
				  "create",
				  "workIframe"));
		/*
		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteCategories.header"),
				getLocalizedString(locale, "tool.managementtool.deleteCategories.header"),
				"submitListForm('category');",
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				true, 
				false, 
				"",
				"",
				"workIframe"));
		*/
	    final String protectCategories = CmsPropertyHandler.getProtectCategories();
	    if(protectCategories != null && protectCategories.equalsIgnoreCase("true") && request.getParameter("categoryId") != null && !request.getParameter("categoryId").equals(""))
	    {
			Integer categoryId = new Integer(request.getParameter("categoryId"));
			CategoryVO categoryVO = CategoryController.getController().findById(categoryId);

			buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.common.accessRights.accessRightsButtonLabel"), 
					  getLocalizedString(locale, "tool.common.accessRights.accessRightsButtonTitle"),
					  "ViewAccessRights!V3.action?interceptionPointCategory=Category&extraParameters=" + categoryVO.getName() + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
					  "images/v3/accessRightIcon.gif",
					  "accessRights",
					  "inlineDiv"));
	    }

		return buttons;
	}
	
	private List<ToolbarButton> getContentTypeDefinitionsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.createContentTypeDefinition.header"), 
				  getLocalizedString(locale, "tool.managementtool.createContentTypeDefinition.header"),
				  "CreateContentTypeDefinition!input.action",
				  "images/v3/createBackgroundPenPaper.gif",
				  "create",
				  "workIframe"));
		/*
		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteContentTypeDefinitions.header"),
				getLocalizedString(locale, "tool.managementtool.deleteContentTypeDefinitions.header"),
				"submitListForm('contentTypeDefinition');",
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				true, 
				false, 
				"",
				"",
				"workIframe"));
		*/

		return buttons;
	}

	private List<ToolbarButton> getContentTypeDefinitionDetailsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		Integer contentTypeDefinitionId = new Integer(request.getParameter("contentTypeDefinitionId"));
		ContentTypeDefinitionVO contentTypeDefinitionVO = (ContentTypeDefinitionVO)ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentTypeDefinitionId);
		
		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.deleteContentTypeDefinition.header"), 
				  getLocalizedString(locale, "tool.managementtool.deleteContentTypeDefinition.header"),
				  "DeleteContentTypeDefinition.action?deleteContentTypeDefinitionId=" + contentTypeDefinitionId + "&igSecurityCode=" + request.getSession().getAttribute("securityCode"),
				  "images/v3/createBackgroundPenPaper.gif",
				  "left",
				  "delete",
				  false,
				  true,
				  getLocalizedString(locale, "tool.managementtool.deleteContentTypeDefinition.header"),
				  getLocalizedString(locale, "tool.managementtool.deleteContentTypeDefinition.text", new String[]{contentTypeDefinitionVO.getName()}),
				  "workIframe"));

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.asXML.label"), 
				  getLocalizedString(locale, "tool.managementtool.asXML.label"),
				  "ViewContentTypeDefinition!useSimple.action?contentTypeDefinitionId=" + contentTypeDefinitionId,
				  "",
				  "showDataAsXML"));

	    final String protectContentTypes = CmsPropertyHandler.getProtectContentTypes();
	    if(protectContentTypes != null && protectContentTypes.equalsIgnoreCase("true"))
	    {
			buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.common.accessRights.accessRightsButtonLabel"), 
					  getLocalizedString(locale, "tool.common.accessRights.accessRightsButtonTitle"),
					  "ViewAccessRights!V3.action?interceptionPointCategory=ContentTypeDefinition&extraParameters=" + contentTypeDefinitionVO.getName() + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
					  "images/v3/accessRightIcon.gif",
					  "accessRights",
					  "inlineDiv"));
	    }

		return buttons;
	}

	private List<ToolbarButton> getWorkflowDefinitionsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.createWorkflowDefinition.header"), 
				  getLocalizedString(locale, "tool.managementtool.createWorkflowDefinition.header"),
				  "CreateWorkflowDefinition!input.action",
				  "images/v3/createBackgroundPenPaper.gif",
				  "create",
				  "workIframe"));
		/*
		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteWorkflowDefinitions.header"),
				getLocalizedString(locale, "tool.managementtool.deleteWorkflowDefinitions.header"),
				"submitListForm('workflowDefinition');",
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				true, 
				false, 
				"",
				"",
				"workIframe"));
		*/
		return buttons;
	}

	private List<ToolbarButton> getWorkflowDefinitionDetailsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		Integer workflowDefinitionId = new Integer(request.getParameter("workflowDefinitionId"));
		WorkflowDefinitionVO workflowDefinitionVO = WorkflowDefinitionController.getController().getWorkflowDefinitionVOWithId(workflowDefinitionId);

		
		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.deleteWorkflowDefinition.header"), 
				  getLocalizedString(locale, "tool.managementtool.deleteWorkflowDefinition.header"),
				  "DeleteWorkflowDefinition.action?workflowDefinitionId=" + workflowDefinitionId + "&igSecurityCode=" + request.getSession().getAttribute("securityCode"),
				  "images/v3/createBackgroundPenPaper.gif",
				  "left",
				  "delete",
				  false,
				  true,
				  getLocalizedString(locale, "tool.managementtool.deleteWorkflowDefinition.header"),
				  getLocalizedString(locale, "tool.managementtool.deleteWorkflowDefinition.text", new String[]{workflowDefinitionVO.getName()}),
				  "workIframe"));

	    final String protectWorkflows = CmsPropertyHandler.getProtectWorkflows();
	    if(protectWorkflows != null && protectWorkflows.equalsIgnoreCase("true"))
	    {
			buttons.add(new ToolbarButton("",
					  getLocalizedString(locale, "tool.common.accessRights.accessRightsButtonLabel"), 
					  getLocalizedString(locale, "tool.common.accessRights.accessRightsButtonTitle"),
					  "ViewAccessRights!V3.action?interceptionPointCategory=Workflow&extraParameters=" + workflowDefinitionVO.getName() + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
					  "images/v3/accessRightIcon.gif",
					  "accessRights",
					  "inlineDiv"));
	    }

		return buttons;
	}

	private List<ToolbarButton> getPortletsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.createPortlet.header"), 
				  getLocalizedString(locale, "tool.managementtool.createPortlet.header"),
				  "UploadPortlet.action",
				  "images/v3/createBackgroundPenPaper.gif",
				  "create",
				  "workIframe"));

		return buttons;
	}

	private List<ToolbarButton> getRedirectsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.createRedirect.header"), 
				  getLocalizedString(locale, "tool.managementtool.createRedirect.header"),
				  "CreateRedirect!input.action",
				  "images/v3/createBackgroundPenPaper.gif",
				  "create",
				  "workIframe"));
		/*
		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteRedirects.header"),
				getLocalizedString(locale, "tool.managementtool.deleteRedirects.header"),
				"submitListForm('redirect');",
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				true, 
				false, 
				"",
				"",
				"workIframe"));
		*/
		return buttons;
	}

	private List<ToolbarButton> getRedirectDetailsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		Integer redirectId = new Integer(request.getParameter("redirectId"));
		RedirectVO redirectVO = RedirectController.getController().getRedirectVOWithId(redirectId);
		
		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.deleteRedirect.header"), 
				  getLocalizedString(locale, "tool.managementtool.deleteRedirect.header"),
				  "DeleteRedirect.action?redirectId=" + redirectId + "&igSecurityCode=" + request.getSession().getAttribute("securityCode"),
				  "images/v3/createBackgroundPenPaper.gif",
				  "left",
				  "delete",
				  false,
				  true,
				  getLocalizedString(locale, "tool.managementtool.deleteRedirect.header"),
				  getLocalizedString(locale, "tool.managementtool.deleteRedirect.text", new String[]{redirectVO.getUrl()}),
				  "workIframe"));

		return buttons;
	}


	private List<ToolbarButton> getInterceptionPointsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.createInterceptionPoint.header"), 
				  getLocalizedString(locale, "tool.managementtool.createInterceptionPoint.header"),
				  "CreateInterceptionPoint!input.action",
				  "images/v3/createBackgroundPenPaper.gif",
				  "create",
				  "workIframe"));
		
		/*
		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteInterceptionPoints.header"),
				getLocalizedString(locale, "tool.managementtool.deleteInterceptionPoints.header"),
				"submitListForm('interceptionPoint');",
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				true, 
				false, 
				"",
				"",
				"workIframe"));
		*/
		
		return buttons;
	}


	private List<ToolbarButton> getInterceptionPointButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithId(new Integer(request.getParameter("interceptionPointId")));

		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteInterceptionPoints.header"),
				getLocalizedString(locale, "tool.managementtool.deleteInterceptionPoints.header"),
				"DeleteInterceptionPoint.action?interceptionPointId=" + interceptionPointVO.getId() + "&igSecurityCode=" + request.getSession().getAttribute("securityCode"),
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				false, 
				true, 
				getLocalizedString(locale, "tool.managementtool.deleteInterceptionPoint.header"),
				getLocalizedString(locale, "tool.managementtool.deleteInterceptionPoint.text", new String[]{interceptionPointVO.getName()}),
				"workIframe"));

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.accessRights.header"), 
				  getLocalizedString(locale, "tool.contenttool.accessRights.header"),
				  "ViewAccessRights!V3.action?interceptionPointCategory=" + interceptionPointVO.getCategory() + "&interceptionPointId=" + interceptionPointVO.getId() + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "images/v3/accessRightsIcon.gif",
				  "accessRights",
				  "inlineDiv"));

		return buttons;
	}
	
	private List<ToolbarButton> getInterceptorsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.createInterceptor.header"), 
				  getLocalizedString(locale, "tool.managementtool.createInterceptor.header"),
				  "CreateInterceptor!input.action",
				  "images/v3/createBackgroundPenPaper.gif",
				  "create",
				  "workIframe"));
		/*
		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteInterceptors.header"),
				getLocalizedString(locale, "tool.managementtool.deleteInterceptors.header"),
				"submitListForm('interceptor');",
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				true, 
				false, 
				"",
				"",
				"workIframe"));
		*/
		
		return buttons;
	}

	private List<ToolbarButton> getInterceptorButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		InterceptorVO interceptorVO = InterceptorController.getController().getInterceptorVOWithId(new Integer(request.getParameter("interceptorId")));

		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteInterceptor.header"),
				getLocalizedString(locale, "tool.managementtool.deleteInterceptor.header"),
				"DeleteInterceptor.action?interceptorId=" + interceptorVO.getId() + "&igSecurityCode=" + request.getSession().getAttribute("securityCode"),
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				false, 
				true, 
				getLocalizedString(locale, "tool.managementtool.deleteInterceptor.header"),
				getLocalizedString(locale, "tool.managementtool.deleteInterceptor.text", new String[]{interceptorVO.getName()}),
				"workIframe"));

		return buttons;
	}

	
	private List<ToolbarButton> getServiceDefinitionsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.createServiceDefinition.header"), 
				  getLocalizedString(locale, "tool.managementtool.createServiceDefinition.header"),
				  "CreateServiceDefinition!input.action",
				  "images/v3/createBackgroundPenPaper.gif",
				  "create",
				  "workIframe"));
		/*
		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteServiceDefinitions.header"),
				getLocalizedString(locale, "tool.managementtool.deleteServiceDefinitions.header"),
				"submitListForm('serviceDefinition');",
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				true, 
				false, 
				"",
				"",
				"workIframe"));
		*/
		return buttons;
	}

	private List<ToolbarButton> getServiceDefinitionDetailsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		ServiceDefinitionVO serviceDefinitionVO = ServiceDefinitionController.getController().getServiceDefinitionVOWithId(new Integer(request.getParameter("serviceDefinitionId")));

		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteServiceDefinition.header"),
				getLocalizedString(locale, "tool.managementtool.deleteServiceDefinition.header"),
				"DeleteServiceDefinition.action?serviceDefinitionId=" + serviceDefinitionVO.getId() + "&igSecurityCode=" + request.getSession().getAttribute("securityCode"),
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				false, 
				true, 
				getLocalizedString(locale, "tool.managementtool.deleteServiceDefinition.header"),
				getLocalizedString(locale, "tool.managementtool.deleteServiceDefinition.text", new String[]{serviceDefinitionVO.getName()}),
				"workIframe"));

		return buttons;
	}


	private List<ToolbarButton> getAvailableServiceBindingsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.createAvailableServiceBinding.header"), 
				  getLocalizedString(locale, "tool.managementtool.createAvailableServiceBinding.header"),
				  "CreateAvailableServiceBinding!input.action",
				  "images/v3/createBackgroundPenPaper.gif",
				  "create",
				  "workIframe"));
		/*
		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteAvailableServiceBindings.header"),
				getLocalizedString(locale, "tool.managementtool.deleteAvailableServiceBindings.header"),
				"submitListForm('availableServiceBinding');",
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				true, 
				false, 
				"",
				"",
				"workIframe"));
		*/
		return buttons;
	}

	private List<ToolbarButton> getAvailableServiceBindingDetailsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		AvailableServiceBindingVO availableServiceBindingVO = AvailableServiceBindingController.getController().getAvailableServiceBindingVOWithId(new Integer(request.getParameter("availableServiceBindingId")));

		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteAvailableServiceBinding.header"),
				getLocalizedString(locale, "tool.managementtool.deleteAvailableServiceBinding.header"),
				"DeleteAvailableServiceBinding.action?availableServiceBindingId=" + availableServiceBindingVO.getId() + "&igSecurityCode=" + request.getSession().getAttribute("securityCode"),
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				false, 
				true, 
				getLocalizedString(locale, "tool.managementtool.deleteAvailableServiceBinding.header"),
				getLocalizedString(locale, "tool.managementtool.deleteAvailableServiceBinding.text", new String[]{availableServiceBindingVO.getName()}),
				"workIframe"));

		return buttons;
	}
	
	private List<ToolbarButton> getSiteNodeTypeDefinitionsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.createSiteNodeTypeDefinition.header"), 
				  getLocalizedString(locale, "tool.managementtool.createSiteNodeTypeDefinition.header"),
				  "CreateSiteNodeTypeDefinition!input.action",
				  "images/v3/createBackgroundPenPaper.gif",
				  "create",
				  "workIframe"));
		/*
		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteSiteNodeTypeDefinitions.header"),
				getLocalizedString(locale, "tool.managementtool.deleteSiteNodeTypeDefinitions.header"),
				"submitListForm('siteNodeTypeDefinition');",
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				true, 
				false, 
				"",
				"",
				"workIframe"));
		*/
		return buttons;
	}

	private List<ToolbarButton> getSiteNodeTypeDefinitionDetailsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		
		SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionVOWithId(new Integer(request.getParameter("siteNodeTypeDefinitionId")));

		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteSiteNodeTypeDefinition.header"),
				getLocalizedString(locale, "tool.managementtool.deleteSiteNodeTypeDefinition.header"),
				"DeleteSiteNodeTypeDefinition.action?siteNodeTypeDefinitionId=" + siteNodeTypeDefinitionVO.getId() + "&igSecurityCode=" + request.getSession().getAttribute("securityCode"),
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				false, 
				true, 
				getLocalizedString(locale, "tool.managementtool.deleteSiteNodeTypeDefinition.header"),
				getLocalizedString(locale, "tool.managementtool.deleteSiteNodeTypeDefinition.text", new String[]{siteNodeTypeDefinitionVO.getName()}),
				"workIframe"));

		return buttons;
	}

	
	private List<ToolbarButton> getServerNodesButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.createServerNode.header"), 
				  getLocalizedString(locale, "tool.managementtool.createServerNode.header"),
				  "CreateServerNode!input.action",
				  "images/v3/createBackgroundPenPaper.gif",
				  "create",
				  "workIframe"));
		/*
		buttons.add(new ToolbarButton("",
				getLocalizedString(locale, "tool.managementtool.deleteServerNodes.header"),
				getLocalizedString(locale, "tool.managementtool.deleteServerNodes.header"),
				"submitListForm('serverNode');",
				"images/v3/deleteBackgroundWasteBasket.gif",
				"left",
				"delete",
				true, 
				false, 
				"",
				"",
				"workIframe"));
		*/
		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.editServerNodeProperties.header"), 
				  getLocalizedString(locale, "tool.managementtool.editServerNodeProperties.header"),
				  "ViewServerNodeProperties.action?serverNodeId=-1",
				  "images/v3/deleteBackgroundWasteBasket.gif",
				  "delete",
				  "workIframe"));

		return buttons;
	}

	private List<ToolbarButton> getApplicationSettingsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		return buttons;
	}

	/*

	private List<ToolbarButton> getServerNodesButtons() throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		buttons.add(new ToolbarButton("CreateServerNode!input.action", getLocalizedString(locale, "images.managementtool.buttons.newServerNode"), "tool.managementtool.createServerNode.header"));	
		buttons.add(new ToolbarButton(true, "javascript:submitListForm('serverNode');", getLocalizedString(locale, "images.managementtool.buttons.deleteServerNode"), "tool.managementtool.deleteServerNodes.header"));
		buttons.add(new ToolbarButton("ViewServerNodeProperties.action?serverNodeId=-1", getLocalizedString(locale, "images.global.buttons.editProperties"), "Edit Properties", new Integer(22), new Integer(80)));
		
		return buttons;
	}
	
	private List<ToolbarButton> getServerNodeDetailsButtons() throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteServerNode.header&yesDestination=" + URLEncoder.encode("DeleteServerNode.action?serverNodeId=" + primaryKey, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListServerNode.action?title=ServerNodes", "UTF-8") + "&message=tool.managementtool.deleteServerNode.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteServerNode"), "tool.managementtool.deleteServerNode.header"));
		buttons.add(new ToolbarButton("ViewServerNodeProperties.action?serverNodeId=" + primaryKey, getLocalizedString(locale, "images.global.buttons.editProperties"), "Edit Properties", new Integer(22), new Integer(80)));
		
		return buttons;				
	}
	*/

	private List<ToolbarButton> getMessageCenterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.createEmail.header"), 
				  getLocalizedString(locale, "tool.managementtool.createEmail.header"),
				  "CreateEmail!inputChooseRecipientsV3.action",
				  "images/v3/createBackgroundPenPaper.gif",
				  "create"));

		return buttons;
	}

	private List<ToolbarButton> getThemesButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.uploadTheme.header"), 
				  getLocalizedString(locale, "tool.managementtool.uploadTheme.header"),
				  "ViewThemes!input.action",
				  "images/v3/createBackgroundPenPaper.gif",
				  "create",
				  "workIframe"));

		return buttons;
	}

	private List<ToolbarButton> getLabelsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.managementtool.uploadTranslation.header"), 
				  getLocalizedString(locale, "tool.managementtool.uploadTranslation.header"),
				  "ViewLabels!input.action",
				  "images/v3/createBackgroundPenPaper.gif",
				  "create",
				  "workIframe"));

		return buttons;
	}

	
	private List<ToolbarButton> getQuickDeployFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.nextButton.label"), 
				  getLocalizedString(locale, "tool.common.nextButton.label"),
				  "submitForm();",
				  "images/v3/nextBackground.gif",
				  "left",
				  "next",
				  true));

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.cancelButton.label"), 
				  getLocalizedString(locale, "tool.common.cancelButton.label"),
				  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
				  "images/v3/cancelIcon.gif",
				  "left",
				  "cancel",
				  true));
		
		return buttons;
	}

	private List<ToolbarButton> getVCDeployFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.nextButton.label"), 
				  getLocalizedString(locale, "tool.common.nextButton.label"),
				  "submitForm();",
				  "images/v3/nextBackground.gif",
				  "left",
				  "next",
				  true));

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.cancelButton.label"), 
				  getLocalizedString(locale, "tool.common.cancelButton.label"),
				  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
				  "images/v3/cancelIcon.gif",
				  "left",
				  "cancel",
				  true));
		
		return buttons;
	}

	private List<ToolbarButton> getPublicationsButtons(Locale locale, HttpServletRequest request)
	{
		Integer repositoryId = new Integer(request.getParameter("repositoryId"));

	    List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

	    buttons.add(new ToolbarButton("previewButton",
				  getLocalizedString(locale, "tool.publishtool.preview.header"), 
				  getLocalizedString(locale, "tool.publishtool.preview.header"),
				  "submitToPreview();",
				  "images/v3/previewIcon.png",
				  "left",
				  "preview",
				  true));

	    buttons.add(new ToolbarButton("createEdition",
				  getLocalizedString(locale, "tool.publishtool.createEdition.header"), 
				  getLocalizedString(locale, "tool.publishtool.createEdition.header"),
				  "submitToCreate();",
				  "images/v3/previewIcon.png",
				  "left",
				  "create",
				  true));

	    buttons.add(new ToolbarButton("unpublishEdition",
				  getLocalizedString(locale, "tool.publishtool.unpublishEdition.header"), 
				  getLocalizedString(locale, "tool.publishtool.unpublishEdition.header"),
				  "submitToUnpublish();",
				  "images/v3/trashcan.png",
				  "left",
				  "trashcan",
				  true));

	    buttons.add(new ToolbarButton("denyPublishing",
				  getLocalizedString(locale, "tool.publishtool.denyPublication.header"), 
				  getLocalizedString(locale, "tool.publishtool.denyPublication.header"),
				  "submitToDeny();",
				  "images/v3/denyPublicationIcon.png",
				  "left",
				  "denyPublication",
				  true));

		try
		{
		    RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);

		    String repositoryName = repositoryVO.getName();
			String dnsName = repositoryVO.getDnsName();

		    String previewUrl = null;
		    
		    String keyword = "preview=";
		    int startIndex = (dnsName == null) ? -1 : dnsName.indexOf(keyword);
		    if(startIndex != -1)
		    {
		        int endIndex = dnsName.indexOf(",", startIndex);
			    if(endIndex > -1)
		            dnsName = dnsName.substring(startIndex, endIndex);
		        else
		            dnsName = dnsName.substring(startIndex);

			    String[] dnsSplit = dnsName.split("=");
			    if (dnsSplit != null && dnsSplit.length > 1) 
			    {
			    	previewUrl = dnsSplit[1] + CmsPropertyHandler.getComponentRendererUrl().replaceAll("Working", "Preview") + "ViewPage.action";
			    } 
			    else 
			    {
			    	previewUrl = CmsPropertyHandler.getStagingDeliveryUrl();
			    }
		    }
		    else
		    {
		        previewUrl = CmsPropertyHandler.getStagingDeliveryUrl();
		    }

		    buttons.add(new ToolbarButton("previewSite",
					  getLocalizedString(locale, "tool.publishtool.previewEnvironment.header"), 
					  getLocalizedString(locale, "tool.publishtool.previewEnvironment.header"),
					  "javascript:openPopup('" + previewUrl + "?repositoryName=" + repositoryName + "', 'StagingPreview', 'width=800,height=600,resizable=yes,toolbar=yes,scrollbars=yes,status=yes,location=yes,menubar=yes');",
					  "images/v3/previewIcon.png",
					  "left",
					  "preview",
					  true));
		}
		catch(Exception e)
		{
		    logger.error("Problem getting all publication tool buttons: " + e.getMessage(), e);
		}
		
		return buttons;				
	}

	private List<ToolbarButton> getSystemPublicationsButtons(Locale locale, HttpServletRequest request)
	{
	    List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

	    if(RemoteCacheUpdater.getSystemNotificationMessages().size() > 0)
	    {
		    buttons.add(new ToolbarButton("pushButton",
					  getLocalizedString(locale, "tool.publishingtool.pushChanges.header"), 
					  getLocalizedString(locale, "tool.publishingtool.pushChanges.header"),
					  "ViewPublications!pushSystemNotificationMessages.action",
					  "images/v3/publishIcon.png",
					  "publish",
					  "publishingWorkIframe"));
	    }
	    
		return buttons;				
	}

	private List<ToolbarButton> getMySettingsButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		buttons.add(new ToolbarButton("mySettingsButton",
									  getLocalizedString(locale, "tool.managementtool.mysettings.header"), 
									  getLocalizedString(locale, "tool.managementtool.mysettings.header"),
									  "javascript:openMySettings();",
									  "images/v3/mySettingsIcon.gif",
									  "left",
									  "mySettings",
									  true));
		
		return buttons;
	}

	
	private List<ToolbarButton> getHelpButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		String helpPageBaseUrl = "http://www.infoglue.org";
		
		String helpPageUrl = "";

		if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionHeader"))
			helpPageUrl = "/help/tools/contenttool/contentVersion";

		if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRoleList.header"))
			helpPageUrl = "/help/tools/managementtool/roles";
		if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRole.header"))
			helpPageUrl = "/help/tools/managementtool/role";
		if(toolbarKey.equalsIgnoreCase("tool.managementtool.createRole.header"))
			helpPageUrl = "/help/tools/managementtool/create_role";

		if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroupList.header"))
			helpPageUrl = "/help/tools/managementtool/groups";
		if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroup.header"))
			helpPageUrl = "/help/tools/managementtool/group";
		if(toolbarKey.equalsIgnoreCase("tool.managementtool.createGroup.header"))
			helpPageUrl = "/help/tools/managementtool/create_group";

		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		buttons.add(new ToolbarButton("helpButton",
									  getLocalizedString(locale, "tool.common.helpButton.label"), 
									  getLocalizedString(locale, "tool.common.helpButton.title"),
									  helpPageUrl,
									  "images/v3/helpIcon.gif",
									  "help"));

		return buttons;
	}

	private List<ToolbarButton> getWindowCloseButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("exitButton",
									  getLocalizedString(locale, "tool.common.closeWindowButton.label"), 
									  getLocalizedString(locale, "tool.common.closeWindowButton.title"),
									  "if(parent && parent.document.location.href != document.location.href && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.document.location.href != document.location.href && parent.closeDialog) parent.closeDialog(); else { window.close();}",
				  					  "images/v3/closeWindowIcon.gif",
				  					  "right",
									  "close",
				  					  true));
		return buttons;
	}

	private ToolbarButton getDialogCloseButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		return new ToolbarButton("exitButton",
									  getLocalizedString(locale, "tool.common.closeWindowButton.label"), 
									  getLocalizedString(locale, "tool.common.closeWindowButton.title"),
									  "if(parent && parent.document.location.href != document.location.href && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.document.location.href != document.location.href && parent.closeDialog) parent.closeDialog(); else { window.close();}",
				  					  "images/v3/closeWindowIcon.gif",
				  					  "right",
									  "close",
				  					  true);
	}

	private ToolbarButton getDialogCancelButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		return new ToolbarButton("cancelButton",
									  getLocalizedString(locale, "tool.common.cancelButton.label"), 
									  getLocalizedString(locale, "tool.common.cancelButton.label"),
									  "if(parent && parent.document.location.href != document.location.href && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.document.location.href != document.location.href && parent.closeDialog) parent.closeDialog(); else { window.close();}",
				  					  "images/v3/cancel.gif",
				  					  "left",
									  "cancel",
				  					  true);
	}

	private ToolbarButton getDialogCancelButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton, String cancelJavascript)
	{
		return new ToolbarButton("cancelButton",
									  getLocalizedString(locale, "tool.common.cancelButton.label"), 
									  getLocalizedString(locale, "tool.common.cancelButton.label"),
									  cancelJavascript,
				  					  "images/v3/cancel.gif",
				  					  "left",
									  "cancel",
				  					  true);
	}

	private ToolbarButton getCommonFooterReturnToReferrerOrHistoryBackButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		return new ToolbarButton("historyBackButton",
									  getLocalizedString(locale, "tool.contenttool.fileUpload.fileUploadFailedBackLabel"), 
									  getLocalizedString(locale, "tool.contenttool.fileUpload.fileUploadFailedBackLabel"),
									  "if(typeof window.returnToReferrer == 'function') returnToReferrer(); else history.back();",
				  					  "",
				  					  "left",
									  "previous",
				  					  true);
	}

	private List<ToolbarButton> getCommonFooterUseSelectedOrCancelButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterUseSelectedButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getDialogCancelButton(toolbarKey, principal, locale, request, disableCloseButton));
				
		return buttons;		
	}

	private List<ToolbarButton> getCommonFooterSaveOrCancelButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getDialogCancelButton(toolbarKey, principal, locale, request, disableCloseButton));
				
		return buttons;		
	}

	private List<ToolbarButton> getCommonFooterSaveOrCancelByRefreshButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getDialogCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "document.location.href = document.location.href;"));
				
		return buttons;		
	}

	private List<ToolbarButton> getCommonFooterSaveOrCancelByJavascriptButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton, String cancelJavascript)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getDialogCancelButton(toolbarKey, principal, locale, request, disableCloseButton, cancelJavascript));
				
		return buttons;		
	}

	private List<ToolbarButton> getCommonFooterSaveOrCancelButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton, String saveJavascript, String saveLabel, String saveTitle)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton, saveJavascript, saveLabel, saveTitle));
		buttons.add(getDialogCancelButton(toolbarKey, principal, locale, request, disableCloseButton));
				
		return buttons;		
	}

	private List<ToolbarButton> getCommonFooterSaveOrSaveAndExitOrCancelButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton, String exitUrl)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, exitUrl));
		buttons.add(getDialogCancelButton(toolbarKey, principal, locale, request, disableCloseButton));
				
		return buttons;		
	}

	private List<ToolbarButton> getCommonFooterSaveOrCloseButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getDialogCloseButton(toolbarKey, principal, locale, request, disableCloseButton));
				
		return buttons;		
	}

	private List<ToolbarButton> getCommonFooterSaveOrSaveAndExitOrCloseButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton, String exitUrl)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
		buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, exitUrl));
		buttons.add(getDialogCloseButton(toolbarKey, principal, locale, request, disableCloseButton));
				
		return buttons;		
	}

	private List<ToolbarButton> getCommonNextCancelButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.nextButton.label"), 
				  getLocalizedString(locale, "tool.common.nextButton.label"),
				  "next();",
				  "images/v3/nextBackground.gif",
				  "left",
				  "next",
				  true));

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.cancelButton.label"), 
				  getLocalizedString(locale, "tool.common.cancelButton.label"),
				  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
				  "images/v3/cancelIcon.gif",
				  "left",
				  "cancel",
				  true));
				
		return buttons;		
	}

	private List<ToolbarButton> getCommonAddNextCancelButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.addButton.label"), 
				  getLocalizedString(locale, "tool.common.addButton.label"),
				  "add();",
				  "images/v3/addIcon.png",
				  "left",
				  "add",
				  true));

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.nextButton.label"), 
				  getLocalizedString(locale, "tool.common.nextButton.label"),
				  "next();",
				  "images/v3/nextBackground.gif",
				  "left",
				  "next",
				  true));

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.cancelButton.label"), 
				  getLocalizedString(locale, "tool.common.cancelButton.label"),
				  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
				  "images/v3/cancelIcon.gif",
				  "left",
				  "cancel",
				  true));
				
		return buttons;		
	}
	
	private List<ToolbarButton> getCommonAddSaveCancelButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.addButton.label"), 
				  getLocalizedString(locale, "tool.common.addButton.label"),
				  "add();",
				  "images/v3/addIcon.png",
				  "left",
				  "add",
				  true));

		buttons.addAll(getCommonFooterSaveOrCancelButton(toolbarKey, principal, locale, request, disableCloseButton));
				
		return buttons;		
	}


	private List<ToolbarButton> asButtons(ToolbarButton button)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

		buttons.add(button);
				
		return buttons;		
	}

	private ToolbarButton getCommonFooterUseSelectedButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		return new ToolbarButton("useSelected", 
			  getLocalizedString(locale, "tool.contenttool.assetDialog.chooseAttachment"), 
			  getLocalizedString(locale, "tool.contenttool.assetDialog.chooseAttachment"), 
			  "useSelectedAsset();", 
			  "", 
			  "", 
			  "linkInsert", 
			  true, 
			  false, 
			  "", 
			  "", 
			  "");
	}

	private ToolbarButton getCommonFooterSaveButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		return getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton, null, null, null);
	}

	private ToolbarButton getCommonFooterSaveButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton, String javascriptCode, String label, String title)
	{
		if(javascriptCode == null)
			javascriptCode = "save();";
		if(label == null)
			label = getLocalizedString(locale, "tool.contenttool.save.label");
		if(title == null)
			title = getLocalizedString(locale, "tool.contenttool.save.label");
			
		return new ToolbarButton("",
				  label,
				  title,
				  javascriptCode,
				  "images/v3/saveInlineIcon.gif",
				  "left",
				  "save",
				  true);
	}

	private ToolbarButton getCommonFooterSaveAndExitButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton, String exitUrl)
	{
		return new ToolbarButton("",
				  getLocalizedString(locale, "tool.contenttool.saveAndExit.label"), 
				  getLocalizedString(locale, "tool.contenttool.saveAndExit.label"),
				  "saveAndExit(\"" + exitUrl + "\");",
				  "images/v3/saveAndExitInlineIcon.gif",
				  "left",
				  "saveAndExit",
				  true);
	}

	private ToolbarButton getCommonFooterCancelButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		return getDialogCancelButton(toolbarKey, principal, locale, request, disableCloseButton);
	}

	private ToolbarButton getCommonFooterCancelButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton, String cancelUrl, boolean isJavascript)
	{
		return new ToolbarButton("cancelButton",
				getLocalizedString(locale, "tool.contenttool.cancel.label"),
				getLocalizedString(locale, "tool.contenttool.cancel.label"),
				"" + cancelUrl + "",
				"images/v3/cancelIcon.gif",
				"left",
				"cancel",
				isJavascript,
				false, 
				"",
				"",
				"");
	}

	
	/**
	 * This method checks if the content version is read only (ie publish, published or final).
	 */
	
	private boolean isReadOnly(Integer contentVersionId)
	{
		boolean isReadOnly = false;
		
		try
		{
			ContentVersionVO contentVersion = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId);
			if(contentVersion != null && (contentVersion.getStateId().intValue() == 1 || contentVersion.getStateId().intValue() == 2 || contentVersion.getStateId().intValue() == 3))
			{
				isReadOnly = true;	
			}
		}
		catch(Exception e){}
				
		return isReadOnly;
	}
	
	
	/**
	 * Used by the view pages to determine if the current user has sufficient access rights
	 * to perform the action specific by the interception point name.
	 *
	 * @param interceptionPointName THe Name of the interception point to check access rights
	 * @return True is access is allowed, false otherwise
	 */
	public boolean hasAccessTo(InfoGluePrincipal principal, String interceptionPointName, boolean returnSuccessIfInterceptionPointNotDefined)
	{
		logger.info("Checking if " + principal.getName() + " has access to " + interceptionPointName);

		try
		{
			return AccessRightController.getController().getIsPrincipalAuthorized(principal, interceptionPointName, returnSuccessIfInterceptionPointNotDefined);
		}
		catch (SystemException e)
		{
		    logger.warn("Error checking access rights", e);
			return false;
		}
	}

	/**
	 * Used by the view pages to determine if the current user has sufficient access rights
	 * to perform the action specific by the interception point name.
	 *
	 * @param interceptionPointName THe Name of the interception point to check access rights
	 * @return True is access is allowed, false otherwise
	 */
	public boolean hasAccessTo(InfoGluePrincipal principal, String interceptionPointName, String extraParameter)
	{
		logger.info("Checking if " + principal.getName() + " has access to " + interceptionPointName + " with extraParameter " + extraParameter);

		try
		{
		    return AccessRightController.getController().getIsPrincipalAuthorized(principal, interceptionPointName, extraParameter);
		}
		catch (SystemException e)
		{
		    logger.warn("Error checking access rights", e);
			return false;
		}
	}
	
	public String getLocalizedString(Locale locale, String key) 
  	{
		return LabelController.getController(locale).getLocalizedString(locale, key);
    	/*
		StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);

    	return stringManager.getString(key);
    	*/
  	}

	public String getLocalizedString(Locale locale, String key, Object[] args) 
  	{
		return LabelController.getController(locale).getLocalizedString(locale, key, args);
   	}
}
