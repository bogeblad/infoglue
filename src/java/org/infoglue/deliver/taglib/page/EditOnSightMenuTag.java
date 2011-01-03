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

package org.infoglue.deliver.taglib.page;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;

/**
 * This taglib creates the nice InfoGlue functions-icon with it's menu.
 * 
 * @author Mattias Bogeblad
 */

public class EditOnSightMenuTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3257850991142318897L;
	
	private String html = null;
    private boolean showInPublishedMode = false;
    private Integer contentId = null;
    
    private boolean showEditMetaData = true;
    private boolean showCreateSubpage = true;
    private boolean showEditInline = true;
    private boolean showEditContent = true;
    private boolean showCategorizeContent = true;
    private boolean showPublishPage = true;
    private boolean showNotifyUserOfPage = true;
    private boolean showPageNotifications = true;
    private boolean showContentNotifications = true;
    private boolean showTranslateArticle = true;
    private boolean showCreateNewsFromContent = true;
    private boolean showMySettings = true;
    //private boolean showChooseArticle = true;
    //private boolean showCreateNewArticle = true;

    public int doEndTag() throws JspException
    {
        if(this.getController().getOperatingMode().intValue() != 3 || showInPublishedMode)
        {
	    	StringBuffer sb = new StringBuffer();
	        
	    	try
	    	{
	    		String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
		    	String returnAddress = "" + componentEditorUrl + "ViewInlineOperationMessages.action";
		    	String originalUrl = URLEncoder.encode(this.getController().getOriginalFullURL(), "iso-8859-1");
		    	
		    	String metaDataUrl 			= componentEditorUrl + "ViewAndCreateContentForServiceBinding.action?siteNodeId=" + this.getController().getSiteNodeId() + "&repositoryId=" + this.getController().getSiteNode().getRepositoryId() + "&changeStateToWorking=true";
		    	String createSiteNodeUrl 	= componentEditorUrl + "CreateSiteNode!inputV3.action?isBranch=true&repositoryId=" + this.getController().getSiteNode().getRepositoryId() + "&parentSiteNodeId=" + this.getController().getSiteNodeId() + "&languageId=" + this.getController().getLanguageId() + "&returnAddress=" + URLEncoder.encode(returnAddress, "utf-8") + "&originalAddress=" + URLEncoder.encode(this.getController().getCurrentPageUrl(), "utf-8");
		    	String contentVersionUrl 	= componentEditorUrl + "ViewContentVersion!standalone.action?contentId=" + this.contentId + "&languageId=" + getController().getLanguageId() + "&anchorName=contentVersionBlock";
		    	String categoriesUrl 		= componentEditorUrl + "ViewContentVersion!standalone.action?contentId=" + this.contentId + "&languageId=" + getController().getLanguageId() + "&anchor=categoriesBlock";
		    	String publishUrl 			= componentEditorUrl + "ViewListSiteNodeVersion!v3.action?siteNodeId=" + this.getController().getSiteNodeId() + "&languageId=" + this.getController().getLanguageId() + "&repositoryId=" + this.getController().getSiteNode().getRepositoryId() + "&recurseSiteNodes=false&returnAddress=" + URLEncoder.encode(returnAddress, "utf-8") + "&originalAddress=" + URLEncoder.encode(this.getController().getCurrentPageUrl(), "utf-8");
		    	String notifyUrl 			= componentEditorUrl + "CreateEmail!inputChooseRecipientsV3.action?originalUrl=" + originalUrl + "&amp;returnAddress=" + URLEncoder.encode(returnAddress, "utf-8") + "&amp;extraTextProperty=tool.managementtool.createEmailNotificationPageExtraText.text"; 
		    	String subscriptionUrl 		= componentEditorUrl + "Subscriptions!input.action?interceptionPointCategory=Content&amp;entityName=" + Content.class.getName() + "&amp;entityId=" + this.contentId + "&amp;extraParameters=" + this.contentId + "&amp;returnAddress=" + URLEncoder.encode(returnAddress, "utf-8");
		    	String pageSubscriptionUrl 	= componentEditorUrl + "Subscriptions!input.action?interceptionPointCategory=SiteNodeVersion&amp;entityName=" + SiteNode.class.getName() + "&amp;entityId=" + this.getController().getSiteNodeId() + "&amp;returnAddress=" + URLEncoder.encode(returnAddress, "utf-8");
		    	//String newsFlowUrl 			= componentEditorUrl + "Workflow!startWorkflow.action?workflowName=Skapa+nyhet&finalReturnAddress=" + URLEncoder.encode(returnAddress, "utf-8") + ""; 
		    	String mySettingsUrl 		= componentEditorUrl + "ViewMySettings.action"; 
		    			    	
				InfoGluePrincipal principal = getController().getPrincipal();
			    String cmsUserName = (String)getController().getHttpServletRequest().getSession().getAttribute("cmsUserName");
			    if(cmsUserName != null && !CmsPropertyHandler.getAnonymousUser().equalsIgnoreCase(cmsUserName))
				    principal = getController().getPrincipal(cmsUserName);

				Locale locale = this.getController().getLocaleAvailableInTool(principal);
				
		    	String buttonLabel 					= this.getLocalizedString(locale, "deliver.editOnSight.buttonLabel");
		    	String changePageMetaDataLabel 		= this.getLocalizedString(locale, "deliver.editOnSight.changePageMetaDataLabel");
		    	String createSubPageToCurrentLabel 	= this.getLocalizedString(locale, "deliver.editOnSight.createSubPageToCurrentLabel");
		    	String editContentInlineLabel 		= this.getLocalizedString(locale, "deliver.editOnSight.editContentInlineLabel");
		    	String editContentLabel 			= this.getLocalizedString(locale, "deliver.editOnSight.editContentLabel");
		    	String categorizeContentLabel 		= this.getLocalizedString(locale, "deliver.editOnSight.categorizeContentLabel");
		    	String publishPageLabel 			= this.getLocalizedString(locale, "deliver.editOnSight.publishPageLabel");
		    	String notifyLabel 					= this.getLocalizedString(locale, "deliver.editOnSight.notifyLabel");
		    	String subscribeToContentLabel 		= this.getLocalizedString(locale, "deliver.editOnSight.subscribeToContentLabel");
		    	String subscribeToPageLabel 		= this.getLocalizedString(locale, "deliver.editOnSight.subscribeToPageLabel");
		    	String translateContentLabel 		= this.getLocalizedString(locale, "deliver.editOnSight.translateContentLabel");
		    	String createNewsOnArticleLabel 	= this.getLocalizedString(locale, "deliver.editOnSight.createNewsOnArticleLabel");
		    	String mySettingsLabel 				= this.getLocalizedString(locale, "deliver.editOnSight.mySettingsLabel");
		    	
		    	if(html != null && !html.equals(""))
		    		sb.append(html);
		    	else
		    		sb.append("<p id='igMenuButton" + getComponentId() + "'><a class='igButton' href=\"#\" onclick=\"showIGMenu('editOnSightDiv" + getComponentId() + "', event);\"><span class='igButtonOuterSpan'><span class='linkInfoGlueFunctions'>" + buttonLabel + "</span></span></a></p>");
		    	
		    	sb.append("<div id=\"editOnSightDiv" + getComponentId() + "\" class=\"editOnSightMenuDiv\" style=\"padding: 0px; margin: 0px; padding-top: 0; min-width: 240px; position: absolute; top: 20px; display: none; background-color: white; border: 1px solid #555;\">");

		    	sb.append("    <ul class='editOnSightUL' style='margin: 0px; padding: 0px; list-style-type:none; list-style-image: none;'>");

		    	if(contentId != null)
		    	{
		    		if(showEditInline)
		    			sb.append("    <li style='margin: 0px; margin-left: 4px; padding: 2px 0px 2px 2px; list-style-type:none;'><a href=\"javascript:editInline(" + this.getController().getSiteNode().getRepositoryId() + ", " + this.contentId + ", " + this.getController().getLanguageId() + ", true);\" class=\"editOnSightHref linkEditArticle\">" + editContentInlineLabel + "</a></li>");
		    		if(showEditContent)
		    			sb.append("    <li style='margin: 0px; margin-left: 4px; padding: 2px 0px 2px 2px; list-style-type:none;'><a href=\"javascript:openInlineDiv('" + contentVersionUrl + "', 700, 750, true);\" class=\"editOnSightHref linkEditArticle\">" + editContentLabel + "</a></li>");
			    	if(showCategorizeContent)
			    		sb.append("    <li style='margin: 0px; margin-left: 4px; padding: 2px 0px 2px 2px; list-style-type:none;'><a href=\"javascript:openInlineDiv('" + categoriesUrl + "', 700, 750, true);\" class=\"editOnSightHref linkCategorizeArticle\">" + categorizeContentLabel + "</a></li>");
		    	}

		    	if(showEditMetaData) 
		    		sb.append("        <li style='margin: 0px; margin-left: 4px; padding: 2px 0px 2px 2px; list-style-type:none;'><a href=\"javascript:openInlineDiv('" + metaDataUrl + "', 700, 750, true);\" class=\"editOnSightHref linkMetadata\">" + changePageMetaDataLabel + "</a></li>");
		    	if(showCreateSubpage) 
			    	sb.append("        <li style='margin: 0px; margin-left: 4px; padding: 2px 0px 2px 2px; list-style-type:none;'><a href=\"javascript:openInlineDiv('" + createSiteNodeUrl + "', 700, 750, true);\" class=\"editOnSightHref linkCreatePage\">" + createSubPageToCurrentLabel + "</a></li>");

		    	if(showPublishPage)
		    		sb.append("        <li style='margin: 0px; margin-left: 4px; padding: 2px 0px 2px 2px; list-style-type:none;'><a href=\"javascript:openInlineDiv('" + publishUrl + "', 700, 750, true);\" class=\"editOnSightHref linkPublish\">" + publishPageLabel + "</a></li>");
		    	if(showNotifyUserOfPage)
			    	sb.append("        <li style='margin: 0px; margin-left: 4px; padding: 2px 0px 2px 2px; list-style-type:none;'><a href=\"javascript:openInlineDiv('" + notifyUrl + "', 700, 750, true);\" class=\"editOnSightHref linkNotify\">" + notifyLabel + "</a></li>");
		    	
		    	if(contentId != null && showContentNotifications)
		    	{
		    		sb.append("    <li style='margin: 0px; margin-left: 4px; padding: 2px 0px 2px 2px; list-style-type:none;'><a href=\"javascript:openInlineDiv('" + subscriptionUrl + "', 700, 750, true);\" class=\"editOnSightHref linkTakeContent\">" + subscribeToContentLabel + "</a></li>");
		    	}
		    	
		    	if(showPageNotifications)
		    		sb.append("        <li style='margin: 0px; margin-left: 4px; padding: 2px 0px 2px 2px; list-style-type:none;'><a href=\"javascript:openInlineDiv('" + pageSubscriptionUrl + "', 700, 750, true);\" class=\"editOnSightHref linkTakePage\">" + subscribeToPageLabel + "</a></li>");
		    			    
		    	ContentVersionVO contentVersionVO = this.getController().getContentVersion(contentId, this.getController().getLanguageId(), true);
		    	if(contentVersionVO != null && showTranslateArticle)
		    	{
			    	List languages = this.getController().getPageLanguages();
			    	
			    	Iterator languagesIterator = languages.iterator();
			    	while(languagesIterator.hasNext())
			    	{
			    		LanguageVO languageVO = (LanguageVO)languagesIterator.next();
			    		if(!contentVersionVO.getLanguageId().equals(languageVO.getId()))
			    		{
				    		String translateUrl = componentEditorUrl + "ViewContentVersion!standalone.action?contentId=" + this.contentId + "&languageId=" + languageVO.getLanguageId() + "&anchorName=contentVersionBlock&translate=true&fromLanguageId=" + contentVersionVO.getLanguageId() + "&toLanguageId=" + languageVO.getId(); // + "&KeepThis=true&TB_iframe=true&height=700&width=1000&modal=true";
							
				    		sb.append("	<li style='margin: 0px; margin-left: 4px; padding: 2px 0px 2px 2px; list-style-type:none;'>");
					    	sb.append("    	<a href=\"javascript:openInlineDiv('" + translateUrl + "', 700, 1000, true);\" class=\"editOnSightHref linkTranslate\">" + translateContentLabel + " &quot;" + languageVO.getLocalizedDisplayLanguage() + "&quot;</a>");
					    	sb.append(" </li>");
			    		}
			    	}
		    	}
		    	
		    	//if(showCreateNewsFromContent)
		    	//	sb.append("        <li style='margin: 0px; margin-left: 4px; padding: 2px 0px 2px 2px; list-style-type:none;'><a href=\"javascript:openInlineDiv('" + newsFlowUrl + "', 700, 750, true);\" class=\"editOnSightHref linkCreateNews\">" + createNewsOnArticleLabel + "</a></li>");
		    	if(showMySettings)
		    		sb.append("        <li style='margin: 0px; margin-left: 4px; padding: 2px 0px 2px 2px; list-style-type:none;'><a href=\"javascript:openInlineDiv('" + mySettingsUrl + "', 700, 750, true);\" class=\"editOnSightHref linkMySettings\">" + mySettingsLabel + "</a></li>");
		    	sb.append("    </ul>");

		    	sb.append("</div>");
	    	
				
		        produceResult(sb.toString());
	    	}
	    	catch (Exception e) 
	    	{
	    		e.printStackTrace();
			}
        }
        
        html = null;
        contentId = null;
        
        return EVAL_PAGE;
    }
 
    public void setHtml(final String html) throws JspException
    {
        this.html = evaluateString("EditOnSightMenuTag", "html", html);
    }

    public void setShowInPublishedMode(boolean showInPublishedMode)
    {
        this.showInPublishedMode = showInPublishedMode;
    }

	public void setContentId(final String contentId) throws JspException
	{
        this.contentId = evaluateInteger("EditOnSightMenuTag", "contentId", contentId);
	}

	public void setShowEditMetaData(boolean showEditMetaData)
	{
		this.showEditMetaData = showEditMetaData;
	}

	public void setShowCreateSubpage(boolean showCreateSubpage)
	{
		this.showCreateSubpage = showCreateSubpage;
	}

	public void setShowEditInline(boolean showEditInline)
	{
		this.showEditInline = showEditInline;
	}

	public void setShowEditContent(boolean showEditContent)
	{
		this.showEditContent = showEditContent;
	}

	public void setShowCategorizeContent(boolean showCategorizeContent)
	{
		this.showCategorizeContent = showCategorizeContent;
	}

	public void setShowPublishPage(boolean showPublishPage)
	{
		this.showPublishPage = showPublishPage;
	}

	public void setShowNotifyUserOfPage(boolean showNotifyUserOfPage)
	{
		this.showNotifyUserOfPage = showNotifyUserOfPage;
	}

	public void setShowPageNotifications(boolean showPageNotifications)
	{
		this.showPageNotifications = showPageNotifications;
	}

	public void setShowContentNotifications(boolean showContentNotifications)
	{
		this.showContentNotifications = showContentNotifications;
	}

	public void setShowTranslateArticle(boolean showTranslateArticle)
	{
		this.showTranslateArticle = showTranslateArticle;
	}

	public void setShowCreateNewsFromContent(boolean showCreateNewsFromContent)
	{
		this.showCreateNewsFromContent = showCreateNewsFromContent;
	}

	public void setShowMySettings(boolean showMySettings)
	{
		this.showMySettings = showMySettings;
	}
    
}