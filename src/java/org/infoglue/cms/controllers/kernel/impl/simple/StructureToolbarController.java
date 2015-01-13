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

import java.util.Locale;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.ToolbarButton;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.StringManager;
import org.infoglue.cms.util.StringManagerFactory;

public class StructureToolbarController 
{
    private final static Logger logger = Logger.getLogger(StructureToolbarController.class.getName());

   	private static VisualFormatter formatter = new VisualFormatter();

	public static ToolbarButton getPreviewButtons(Integer repositoryId, Integer siteNodeId, String siteNodeVersionId, Locale locale) throws Exception
	{
		RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);
		
		String dnsName = repositoryVO.getDnsName();

		String workingUrl = getWorkingUrl(dnsName);
	    
	    ToolbarButton previewPage = new ToolbarButton("previewPage",
				  getLocalizedString(locale, "tool.structuretool.toolbarV3.previewPageLabel"), 
				  getLocalizedString(locale, "tool.structuretool.toolbarV3.previewPageLabel"),
				  "javascript:openPopup('" + workingUrl + "?siteNodeId=" + siteNodeId + (siteNodeVersionId != null ? "&siteNodeVersionId=" + siteNodeVersionId : "") + "', 'Import', 'resizable=yes,toolbar=yes,scrollbars=yes,status=yes,location=yes,menubar=yes');",
				  "",
				  "left",
				  "preview",
				  true);
	    	    
	    return previewPage;
	}
	
	public static ToolbarButton getPreviewMediumScreenButtons(Integer repositoryId, Integer siteNodeId, String siteNodeVersionId, Locale locale) throws Exception
	{
		RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);
		
		String dnsName = repositoryVO.getDnsName();

	    String workingUrl = getWorkingUrl(dnsName);
	    /*width of this is common breakpoint + 17px to compensate for scrollbars*/
	    ToolbarButton previewPage = new ToolbarButton("previewMediumScreenPage",
				  getLocalizedString(locale, "tool.structuretool.toolbarV3.previewMediumScreenPageLabel"), 
				  getLocalizedString(locale, "tool.structuretool.toolbarV3.previewMediumScreenPageLabel"),
				  "javascript:openInlineDiv('" + workingUrl + "?siteNodeId=" + siteNodeId + (siteNodeVersionId != null ? "&siteNodeVersionId=" + siteNodeVersionId : "") + "', 1010, 660, false, true,'Tablet (660 pixels width)'); return false;",
				  "",
				  "left",
				  "preview",
				  true);
	    	    
	    return previewPage;
	}
	
	public static ToolbarButton getPreviewSmallScreenButtons(Integer repositoryId, Integer siteNodeId, String siteNodeVersionId, Locale locale) throws Exception
	{
		RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);
		
		String dnsName = repositoryVO.getDnsName();

		String workingUrl = getWorkingUrl(dnsName);
		/*width of this is common breakpoint + 17px to compensate for scrollbars*/
	    ToolbarButton previewPage = new ToolbarButton("previewSmallScreenPage",
				  getLocalizedString(locale, "tool.structuretool.toolbarV3.previewSmallScreenPageLabel"), 
				  getLocalizedString(locale, "tool.structuretool.toolbarV3.previewSmallScreenPageLabel"),
				  "javascript:openInlineDiv('" + workingUrl + "?siteNodeId=" + siteNodeId + (siteNodeVersionId != null ? "&siteNodeVersionId=" + siteNodeVersionId : "") + "', 480, 320, false, true,'Mobile phone(320 pixels width)'); return false;",
				  "",
				  "left",
				  "preview",
				  true);
	    	    
	    return previewPage;
	}
	
	private static String getWorkingUrl(String dnsName) {
	    String workingUrl = null;
	    
	    String keyword = "working=";
	    int startIndex = (dnsName == null) ? -1 : dnsName.indexOf(keyword);
	    if(startIndex != -1)
	    {
	        int endIndex = dnsName.indexOf(",", startIndex);
		    if(endIndex > -1)
	            dnsName = dnsName.substring(startIndex, endIndex);
	        else
	            dnsName = dnsName.substring(startIndex);

		    String hostName = dnsName.split("=")[1];
		    if(hostName.indexOf("localhost") == -1)
			    workingUrl = hostName + CmsPropertyHandler.getComponentRendererUrl() + "ViewPage.action";
		    else
		    	workingUrl = CmsPropertyHandler.getComponentRendererUrl() + "ViewPage.action";
	    }
	    else
	    {
	        workingUrl = CmsPropertyHandler.getPreviewDeliveryUrl();
	    }
	    return workingUrl;
		
	}
	public static ToolbarButton getPreviewButton(Integer repositoryId, Integer siteNodeId, Locale locale) throws Exception
	{
		RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);
		
		String dnsName = repositoryVO.getDnsName();

	    String workingUrl = null;
	    
	    String keyword = "working=";
	    int startIndex = (dnsName == null) ? -1 : dnsName.indexOf(keyword);
	    if(startIndex != -1)
	    {
	        int endIndex = dnsName.indexOf(",", startIndex);
		    if(endIndex > -1)
	            dnsName = dnsName.substring(startIndex, endIndex);
	        else
	            dnsName = dnsName.substring(startIndex);

		    String hostName = dnsName.split("=")[1];
		    if(hostName.indexOf("localhost") == -1)
			    workingUrl = hostName + CmsPropertyHandler.getComponentRendererUrl() + "ViewPage.action";
		    else
		    	workingUrl = CmsPropertyHandler.getComponentRendererUrl() + "ViewPage.action";
	    }
	    else
	    {
	        workingUrl = CmsPropertyHandler.getPreviewDeliveryUrl();
	    }
	    
	    ToolbarButton previewPage = new ToolbarButton("previewPage",
				  getLocalizedString(locale, "tool.structuretool.toolbarV3.previewPageLabel"), 
				  getLocalizedString(locale, "tool.structuretool.toolbarV3.previewPageLabel"),
				  "previewPage('" + workingUrl + "?siteNodeId=" + siteNodeId + "');",
				  "",
				  "left",
				  "preview",
				  true);
	    	    
	    return previewPage;
	}
	
	public static ToolbarButton getPageDetailButtons(Integer repositoryId, Integer siteNodeId, Locale locale, InfoGluePrincipal principal)
	{
		return new ToolbarButton("pageDetail",
				getLocalizedString(locale, "tool.structuretool.toolbarV3.pageDetailLabel"), 
				getLocalizedString(locale, "tool.structuretool.toolbarV3.pageDetailTitle"),
				"ViewSiteNode!V3.action?siteNodeId=" + siteNodeId + "&stay=true",
				"",
				"pageDetails");

		/*
		try
		{
		    boolean isMetaInfoInWorkingState = false;
			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(repositoryId);
			Integer languageId = masterLanguageVO.getLanguageId();

			SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
			if(siteNodeVO.getMetaInfoContentId() != null && siteNodeVO.getMetaInfoContentId().intValue() != -1)
			{
				ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(siteNodeVO.getMetaInfoContentId(), languageId);
				if(contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
					isMetaInfoInWorkingState = true;
			}

			logger.info("isMetaInfoInWorkingState:" + isMetaInfoInWorkingState);
			if(isMetaInfoInWorkingState)
			{
				return new ToolbarButton("pageDetail",
						getLocalizedString(locale, "tool.structuretool.toolbarV3.pageDetailLabel"), 
						getLocalizedString(locale, "tool.structuretool.toolbarV3.pageDetailTitle"),
						"ViewSiteNode!V3.action?siteNodeId=" + siteNodeId + "&stay=true",
						"",
						"pageDetails");
			}
			else
			{
				return new ToolbarButton("pageDetail",
						getLocalizedString(locale, "tool.structuretool.toolbarV3.pageDetailLabel"), 
						getLocalizedString(locale, "tool.structuretool.toolbarV3.pageDetailTitle"),
						"javascript:alert('Cannot edit this page. You must first set the meta info to working. Do this by entering node properties and changing the state to working.');",
						"",
						"pageDetails");
			}
		}
		catch(Exception e)
		{
			return new ToolbarButton("pageDetail",
					getLocalizedString(locale, "tool.structuretool.toolbarV3.pageDetailLabel"), 
					getLocalizedString(locale, "tool.structuretool.toolbarV3.pageDetailTitle"),
					"javascript:alert('Cannot edit this page. You must first set the meta info to working. Do this by entering node properties and changing the state to working.');",
					"",
					"pageDetails");
		}
		*/
	}

	public static ToolbarButton getPageDetailSimpleButtons(Integer repositoryId, Integer siteNodeId, Locale locale, InfoGluePrincipal principal)
	{
		try
		{
			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(repositoryId);
			Integer languageId = masterLanguageVO.getLanguageId();
	
			return new ToolbarButton("pageStructure",
						getLocalizedString(locale, "tool.structuretool.toolbarV3.pageStructureSimpleLabel"), 
						getLocalizedString(locale, "tool.structuretool.toolbarV3.pageStructureSimpleLabel"),
						"" + CmsPropertyHandler.getComponentRendererUrl() + "ViewPage!renderDecoratedPage.action?siteNodeId=" + siteNodeId + "&languageId=" + masterLanguageVO.getId() + "&contentId=-1&showSimple=true" + "&cmsUserName=" + formatter.encodeURI(principal.getName()),
						"",
						"pageStructureSimple");
		}
		catch (Exception e) 
		{
			logger.error("Problem generating button:" + e.getMessage(), e);
		}
		
		return null;
	}

	public static ToolbarButton getCoverButtons(Integer repositoryId, Integer siteNodeId, Locale locale, InfoGluePrincipal principal)
	{
		try
		{
		    boolean isMetaInfoInWorkingState = false;
			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(repositoryId);
			Integer languageId = masterLanguageVO.getLanguageId();

			SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
			if(siteNodeVO.getMetaInfoContentId() != null && siteNodeVO.getMetaInfoContentId().intValue() != -1)
			{
				ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(siteNodeVO.getMetaInfoContentId(), languageId);
				if(contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
					isMetaInfoInWorkingState = true;
			}

			logger.info("isMetaInfoInWorkingState:" + isMetaInfoInWorkingState);
			if(isMetaInfoInWorkingState)
			{
				return new ToolbarButton("",
						getLocalizedString(locale, "tool.structuretool.toolbarV3.pageCoverLabel"), 
						getLocalizedString(locale, "tool.structuretool.toolbarV3.pageCoverTitle"),
						"ViewSiteNode!V3.action?siteNodeId=" + siteNodeId + "&stay=true",
						"",
						"pageCover");
			}
			else
			{
				return new ToolbarButton("",
						getLocalizedString(locale, "tool.structuretool.toolbarV3.pageCoverLabel"), 
						getLocalizedString(locale, "tool.structuretool.toolbarV3.pageCoverTitle"),
						"javascript:alert('Cannot edit this page. You must first set the meta info to working. Do this by entering node properties and changing the state to working.');",
						"",
						"pageCover");
			}
		}
		catch(Exception e)
		{
			return new ToolbarButton("",
					getLocalizedString(locale, "tool.structuretool.toolbarV3.pageCoverLabel"), 
					getLocalizedString(locale, "tool.structuretool.toolbarV3.pageCoverTitle"),
					"javascript:alert('Cannot edit this page. You must first set the meta info to working. Do this by entering node properties and changing the state to working.');",
					"",
					"pageCover");
		}
	}

	/*
	coverButton.getSubButtons().add(getSimplePageComponentsButton());
	buttons.add(coverButton);	

	if(!isReadOnly())
	{
	    ImageButton pageComponentsButton = getViewPageComponentsButton();
	    pageComponentsButton.getSubButtons().add(getSimplePageComponentsButton());
	    buttons.add(pageComponentsButton);	
	}
	
	buttons.add(getExecuteTaskButton());
	 */
	
	public static ToolbarButton getPublishButtons(Integer repositoryId, Integer siteNodeId, Locale locale)
	{
		return new ToolbarButton("publishPageStructure",
				getLocalizedString(locale, "tool.structuretool.toolbarV3.publishPagesLabel"), 
				getLocalizedString(locale, "tool.structuretool.toolbarV3.publishPagesTitle"),
				"ViewListSiteNodeVersion!v3.action?siteNodeId=" + siteNodeId + "&repositoryId=" + repositoryId + "&recurseSiteNodes=true&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				"",
				"publishPages");
	}

	public static ToolbarButton getPublishCurrentNodeButton(Integer repositoryId, Integer siteNodeId, Locale locale)
	{
		return new ToolbarButton("publishCurrentPage",
				getLocalizedString(locale, "tool.structuretool.toolbarV3.publishPageLabel"), 
				getLocalizedString(locale, "tool.structuretool.toolbarV3.publishPageTitle"),
				"ViewListSiteNodeVersion!v3.action?siteNodeId=" + siteNodeId + "&repositoryId=" + repositoryId + "&recurseSiteNodes=false&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				"",
				"publishPage");
	}

	public static ToolbarButton getUnpublishButton(Integer repositoryId, Integer siteNodeId, Locale locale, boolean recursive, boolean onlyLatestVersion)
	{
		String labelKey = "tool.common.unpublishing.unpublishButtonLabel";
		if(recursive)
			labelKey = "tool.common.unpublishing.unpublishPageButtonLabel";
		if(onlyLatestVersion)
			labelKey = "tool.contenttool.toolbarV3.unpublishContentsLabel";
		
		return new ToolbarButton("unpublishPage",
				getLocalizedString(locale, labelKey), 
				getLocalizedString(locale, labelKey),
				getUnpublishButtonLink(siteNodeId, recursive, onlyLatestVersion),
				"",
				"unpublishPage");
	}


	public static String getUnpublishButtonLink(Integer siteNodeId, boolean recursive, boolean onlyLatestVersion)
	{
		return "UnpublishSiteNodeVersion!inputChooseSiteNodesV3.action?siteNodeId=" + siteNodeId + "&recurseSiteNodes=" + recursive + "&unpublishAll=" + !onlyLatestVersion + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent";
	}
	
	/*
	public static ToolbarButton getTasksButtons(Integer repositoryId, Integer siteNodeId, Locale locale)
	{
		return new ToolbarButton("",
				getLocalizedString(locale, "tool.structuretool.toolbarV3.previewPageLabel"), 
				getLocalizedString(locale, "tool.structuretool.toolbarV3.previewPageTitle"),
				"" + workingUrl + "?siteNodeId=" + siteNodeId,
				"",
				"preview");
	}
	*/

	
	public static String getLocalizedString(Locale locale, String key) 
  	{
    	StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);

    	return stringManager.getString(key);
  	}

}
 
