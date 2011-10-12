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

package org.infoglue.deliver.invokers;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.tree.DefaultAttribute;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.PageTemplateController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.StringManager;
import org.infoglue.cms.util.StringManagerFactory;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.infoglue.deliver.applications.actions.InfoGlueComponent;
import org.infoglue.deliver.applications.databeans.ComponentBinding;
import org.infoglue.deliver.applications.databeans.ComponentProperty;
import org.infoglue.deliver.applications.databeans.ComponentPropertyOption;
import org.infoglue.deliver.applications.databeans.ComponentTask;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.applications.databeans.Slot;
import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;
import org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.DecoratedComponentLogic;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.Timer;
import org.infoglue.deliver.util.VelocityTemplateProcessor;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlInfosetBuilder;

/**
* @author Mattias Bogeblad
*
* This class delivers a normal html page by using the component-based method but also decorates it
* so it can be used by the structure tool to manage the page components.
*/

public class AjaxDecoratedComponentBasedHTMLPageInvoker extends ComponentBasedHTMLPageInvoker
{
	private final static DOMBuilder domBuilder = new DOMBuilder();
	private final static VisualFormatter formatter = new VisualFormatter();
	
    private final static Logger logger = Logger.getLogger(AjaxDecoratedComponentBasedHTMLPageInvoker.class.getName());

	//private String propertiesDivs 	= "";
	//private String tasksDivs 		= "";
	
	/**
	 * This is the method that will render the page. It uses the new component based structure. 
	 */ 
	
	public void invokePage() throws SystemException, Exception
	{
		Timer timer = new Timer();
		timer.setActive(false);
		
		String decoratePageTemplate = "";
		
		NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(this.getDeliveryContext());
		
		timer.printElapsedTime("Initialized controllers");
		
		Integer repositoryId = nodeDeliveryController.getSiteNode(getDatabase(), this.getDeliveryContext().getSiteNodeId()).getRepository().getId();
		String componentXML = getPageComponentsString(getDatabase(), this.getTemplateController(), this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId());
		//logger.info("componentXML:" + componentXML);
		
		componentXML = appendPagePartTemplates(componentXML, this.getDeliveryContext().getSiteNodeId());
		
		timer.printElapsedTime("After getPageComponentsString");
		
		Timer decoratorTimer = new Timer();
		decoratorTimer.setActive(false);

		InfoGlueComponent baseComponent = null;
		
		if(componentXML == null || componentXML.length() == 0)
		{
			decoratePageTemplate = showInitialBindingDialog(this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId());
		}
		else
		{
   			List unsortedPageComponents = new ArrayList();

   			try
		    {
   				//DOM4J
   				/*
   				Document document = domBuilder.getDocument(componentXML);
				List pageComponents = getPageComponentsWithDOM4j(getDatabase(), componentXML, document.getRootElement(), "base", this.getTemplateController(), null, unsortedPageComponents);
				*/
   				
	   			//XPP3
		        XmlInfosetBuilder builder = XmlInfosetBuilder.newInstance();
		        XmlDocument doc = builder.parseReader(new StringReader( componentXML ) );
				List pageComponents = getPageComponentsWithXPP3(getDatabase(), componentXML, doc.getDocumentElement(), "base", this.getTemplateController(), null, unsortedPageComponents);

				preProcessComponents(nodeDeliveryController, repositoryId, unsortedPageComponents, pageComponents);
				
				if(pageComponents.size() > 0)
				{
					baseComponent = (InfoGlueComponent)pageComponents.get(0);
				}
		    }
		    catch(Exception e)
		    {
		        throw new SystemException("There was a problem parsing the component structure on the page. Could be invalid XML in the ComponentStructure attribute. Message:" + e.getMessage(), e);
		    }
		    
			decoratorTimer.printElapsedTime("After getting basecomponent");
			
			if(baseComponent == null)
			{
				decoratePageTemplate = showInitialBindingDialog(this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId());
			}
			else
			{
				//if(this.getDeliveryContext().getShowSimple() == true)
			    //{
			    //    decoratePageTemplate = showSimplePageStructure(this.getTemplateController(), repositoryId, this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), baseComponent);
			    //}
			    //else
			    //{
				    ContentVO metaInfoContentVO = nodeDeliveryController.getBoundContent(getDatabase(), this.getTemplateController().getPrincipal(), this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), true, "Meta information", this.getDeliveryContext());
					decoratePageTemplate = decorateComponent(baseComponent, this.getTemplateController(), repositoryId, this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId()/*, metaInfoContentVO.getId()*/, 15, 0);
					decoratePageTemplate = decorateTemplate(this.getTemplateController(), decoratePageTemplate, this.getDeliveryContext(), baseComponent);
				//}
				
				if(logger.isInfoEnabled())
					logger.info("EvaluateFullDecorated:" + this.getDeliveryContext().getEvaluateFullPage() + ":" + CmsPropertyHandler.getDisableDecoratedFinalRendering());
				if(this.getDeliveryContext().getEvaluateFullPage() || !CmsPropertyHandler.getDisableDecoratedFinalRendering())
				{	
					if(decoratePageTemplate.length() > 300000)
						logger.warn("The page at " + this.getTemplateController().getOriginalFullURL() + " was huge and the extra rendering of decorated pages takes some time and memory. If possible please make sure the components handles all rendering they need themselves and disable this step.");
					
					if(logger.isInfoEnabled())
						logger.info("Running extra decoration");
					Map context = getDefaultContext();
					String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
					context.put("componentEditorUrl", componentEditorUrl);
					boolean oldUseFullUrl = this.getTemplateController().getDeliveryContext().getUseFullUrl();
					this.getTemplateController().getDeliveryContext().setUseFullUrl(true);
					//context.put("currentUrl", URLEncoder.encode(this.getTemplateController().getCurrentPageUrl(), "UTF-8"));
					context.put("currentUrl", URLEncoder.encode(this.getTemplateController().getOriginalFullURL(), "UTF-8"));
					context.put("contextName", this.getRequest().getContextPath());
					this.getTemplateController().getDeliveryContext().setUseFullUrl(oldUseFullUrl);
					StringWriter cacheString = new StringWriter();
					PrintWriter cachedStream = new PrintWriter(cacheString);
					new VelocityTemplateProcessor().renderTemplate(context, cachedStream, decoratePageTemplate, false, baseComponent);
					decoratePageTemplate = cacheString.toString();
				}

				//TODO - TEST
				//decoratePageTemplate += propertiesDivs + tasksDivs;	
				int indexOfBODYStartTag = decoratePageTemplate.indexOf("</body");
				if(indexOfBODYStartTag == -1)
					indexOfBODYStartTag = decoratePageTemplate.indexOf("</BODY");
		
				if(indexOfBODYStartTag > -1)
				{
					StringBuffer sb = new StringBuffer(decoratePageTemplate);
					decoratePageTemplate = sb.insert(indexOfBODYStartTag, "<div id=\"availableComponents\"></div><div id=\"componentTasks\"><div id=\"componentMenu\" class=\"skin0\">Loading menu...</div></div><div id=\"componentPropertiesDiv\"></div><div id=\"componentStructure\"></div>").toString();
				}
			}		
		}
		
		timer.printElapsedTime("After main decoration");
				
		String pageString = decoratePageTemplate;
		//pageString = decorateHeadAndPageWithVarsFromComponents(pageString);
		
		this.setPageString(pageString);
		
		timer.printElapsedTime("End invokePage");
	}
	
	 /**
	  * This method prints out the first template dialog.
	  */

	 private String showInitialBindingDialog(Integer siteNodeId, Integer languageId, Integer contentId)
	 {
		 String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
		 //String url = "javascript:window.open('" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?eee=1&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + (contentId == null ? "-1" : contentId) + "&specifyBaseTemplate=true&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "', 'BaseTemplate', 'width=600,height=700,left=50,top=50,toolbar=no,status=no,scrollbars=yes,location=no,menubar=no,directories=no,resizable=yes');";
		 
		 String url = "" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?eee=1&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + (contentId == null ? "-1" : contentId) + "&specifyBaseTemplate=true&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "";
		 url = "javascript:openInlineDiv('" + url + "', 600, 800, true, true, 'BaseTemplate');";
		 
		 String pageTemplateHTML = " or choose a page template below.<br><br>";
		 
	     boolean foundPageTemplate = false;

	     try
		 {
	    	 SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
	    	 LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(siteNodeVO.getRepositoryId());
	    	 
	    	 InfoGluePrincipal principal = this.getTemplateController().getPrincipal();
	    	 String cmsUserName = (String)this.getTemplateController().getHttpServletRequest().getSession().getAttribute("cmsUserName");
	    	 if(cmsUserName != null)
	    		 principal = this.getTemplateController().getPrincipal(cmsUserName);
		    
		     List sortedPageTemplates = PageTemplateController.getController().getPageTemplates(principal, masterLanguageVO.getId());
			 Iterator sortedPageTemplatesIterator = sortedPageTemplates.iterator();
			 int index = 0;
			 pageTemplateHTML += "<table border=\"0\" width=\"80%\" cellspacing=\"0\"><tr>";
			 
		     while(sortedPageTemplatesIterator.hasNext())
			 {
			     ContentVO contentVO = (ContentVO)sortedPageTemplatesIterator.next();
			     ContentVersionVO contentVersionVO = this.getTemplateController().getContentVersion(contentVO.getId(), LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(getDatabase(), siteNodeId).getId(), false);
			     if(contentVersionVO != null)
			     {
				     String imageUrl = this.getTemplateController().getAssetUrl(contentVO.getId(), "thumbnail");
				     if(imageUrl == null || imageUrl.equals(""))
				         imageUrl = this.getRequest().getContextPath() + "/images/undefinedPageTemplate.jpg";
				 
				     pageTemplateHTML += "<td style=\"font-family:verdana, sans-serif; font-size:10px; border: 1px solid #C2D0E2; padding: 5px 5px 5px 5px;\" valign=\"bottom\" align=\"center\"><a href=\"" + componentEditorUrl + "ViewSiteNodePageComponents!addPageTemplate.action?repositoryId=" + contentVO.getRepositoryId() + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&pageTemplateContentId=" + contentVO.getId() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "\"><img src=\"" + imageUrl + "\" border=\"0\" style=\"width: 100px;\"><br>";
				     pageTemplateHTML += contentVO.getName() + "</a>";
				     pageTemplateHTML += "</td>";	

				     index++;
				     if(index >= 5)
				     {
				    	 index = 0;
				    	 pageTemplateHTML += "</tr><tr>";
				     }
				     
				     foundPageTemplate = true;
			     }
			 }
			 pageTemplateHTML += "</tr></table>";

		 }
		 catch(Exception e)
		 {
		     logger.warn("A problem arouse when getting the page templates:" + e.getMessage(), e);
		 }
		 
		 this.getTemplateController().getDeliveryContext().setContentType("text/html");
		 this.getTemplateController().getDeliveryContext().setDisablePageCache(true);
		 return "<html><body style=\"font-family:verdana, sans-serif; font-size:10px;\">The page has no base component assigned yet. Click <a href=\"" + url + "\">here</a> to assign one" + (foundPageTemplate ? pageTemplateHTML : "") + "</body></html>";
	 }


	/**
	 * This method adds the neccessairy html to a template to make it right-clickable.
	 */	

	private String decorateTemplate(TemplateController templateController, String template, DeliveryContext deliveryContext, InfoGlueComponent component)
	{
		Timer timer = new Timer();
		timer.setActive(false);
 
		String decoratedTemplate = template;
		
		try
		{
			String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();

			InfoGluePrincipal principal = templateController.getPrincipal();
		    String cmsUserName = (String)templateController.getHttpServletRequest().getSession().getAttribute("cmsUserName");
		    if(cmsUserName != null && !CmsPropertyHandler.getAnonymousUser().equalsIgnoreCase(cmsUserName))
			    principal = templateController.getPrincipal(cmsUserName);

		    boolean hasAccessToAccessRights = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.ChangeSlotAccess", "");
			boolean hasAccessToAddComponent = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.AddComponent", "" + component.getContentId() + "_" + component.getSlotName());
			boolean hasAccessToDeleteComponent = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.DeleteComponent", "" + component.getContentId() + "_" + component.getSlotName());
			boolean hasAccessToChangeComponent = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.ChangeComponent", "" + component.getContentId() + "_" + component.getSlotName());
			boolean hasSaveTemplateAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "StructureTool.SaveTemplate", "");
		    
			boolean hasSubmitToPublishAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.SubmitToPublish", "");
		    boolean hasPageStructureAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.PageStructure", "");
		    boolean hasOpenInNewWindowAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.OpenInNewWindow", "");
		    boolean hasViewSourceAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.ViewSource", "");

		    String extraHeader 	= FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "preview/ajax/pageComponentEditorHeader.vm"), "iso-8859-1");
		    //String extraBody 	= FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "preview/ajax/pageComponentEditorBody.vm"), "iso-8859-1");
		    String extraBody 	= "";
			
			boolean oldUseFullUrl = this.getTemplateController().getDeliveryContext().getUseFullUrl();
			this.getTemplateController().getDeliveryContext().setUseFullUrl(true);

			String parameters = "repositoryId=" + templateController.getSiteNode().getRepositoryId() + "&siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&componentId=" + this.getRequest().getParameter("activatedComponentId") + "&componentContentId=" + this.getRequest().getParameter("componentContentId") + "&showSimple=false&showLegend=false&originalUrl=" + URLEncoder.encode(this.getTemplateController().getCurrentPageUrl(), "UTF-8");
			
			String WYSIWYGEditorFile = "ckeditor/ckeditor.js";
			if(!CmsPropertyHandler.getPrefferedWYSIWYG().equals("ckeditor3"))
				WYSIWYGEditorFile = "FCKEditor/fckeditor.js";
			
			StringBuffer path = getPagePathAsCommaseparatedIds(templateController);
			
			extraHeader = extraHeader.replaceAll("\\$\\{focusElementId\\}", "" + this.getRequest().getParameter("focusElementId"));
			extraHeader = extraHeader.replaceAll("\\$\\{contextName\\}", this.getRequest().getContextPath());
			extraHeader = extraHeader.replaceAll("\\$\\{componentEditorUrl\\}", componentEditorUrl);
			if(principal.getName().equalsIgnoreCase(CmsPropertyHandler.getAnonymousUser()))
				extraHeader = extraHeader.replaceAll("\\$\\{limitedUserWarning\\}", "alert('Your session must have expired as you are now in decorated mode as " + principal.getName() + ". Please close browser and login again.');");
			else
				extraHeader = extraHeader.replaceAll("\\$\\{limitedUserWarning\\}", "");
			//extraHeader = extraHeader.replaceAll("\\$\\{currentUrl\\}", URLEncoder.encode(this.getTemplateController().getCurrentPageUrl(), "UTF-8"));
			extraHeader = extraHeader.replaceAll("\\$\\{currentUrl\\}", URLEncoder.encode(this.getTemplateController().getOriginalFullURL(), "UTF-8"));
			extraHeader = extraHeader.replaceAll("\\$\\{activatedComponentId\\}", "" + this.getRequest().getParameter("activatedComponentId"));
			extraHeader = extraHeader.replaceAll("\\$\\{parameters\\}", parameters);
			extraHeader = extraHeader.replaceAll("\\$\\{siteNodeId\\}", "" + templateController.getSiteNodeId());
			extraHeader = extraHeader.replaceAll("\\$\\{languageId\\}", "" + templateController.getLanguageId());
			extraHeader = extraHeader.replaceAll("\\$\\{contentId\\}", "" + templateController.getContentId());
			extraHeader = extraHeader.replaceAll("\\$\\{metaInfoContentId\\}", "" + templateController.getMetaInformationContentId());
			extraHeader = extraHeader.replaceAll("\\$\\{parentSiteNodeId\\}", "" + templateController.getSiteNode().getParentSiteNodeId());
			extraHeader = extraHeader.replaceAll("\\$\\{repositoryId\\}", "" + templateController.getSiteNode().getRepositoryId());
			extraHeader = extraHeader.replaceAll("\\$\\{path\\}", "" + path.substring(1));
			extraHeader = extraHeader.replaceAll("\\$\\{userPrefferredLanguageCode\\}", "" + CmsPropertyHandler.getPreferredLanguageCode(principal.getName()));
			extraHeader = extraHeader.replaceAll("\\$\\{userPrefferredWYSIWYG\\}", "" + CmsPropertyHandler.getPrefferedWYSIWYG());
			extraHeader = extraHeader.replaceAll("\\$\\{WYSIWYGEditorJS\\}", WYSIWYGEditorFile);

			this.getTemplateController().getDeliveryContext().setUseFullUrl(oldUseFullUrl);

		    extraBody = extraBody + "<script type=\"text/javascript\">initializeComponentEventHandler('base0_" + component.getId() + "Comp', '" + component.getId() + "', 'base', " + templateController.getSiteNode().getRepositoryId() + ", " + templateController.getSiteNodeId() + ", " + templateController.getLanguageId() + ", " + templateController.getContentId() + ", " + component.getId() + ", " + component.getContentId() + ", '" + URLEncoder.encode(templateController.getOriginalFullURL(), "UTF-8") + "');</script>";

		    //Locale locale = templateController.getLocale();
		    Locale locale = templateController.getLocaleAvailableInTool(principal);
		    
			String submitToPublishHTML = getLocalizedString(locale, "deliver.editOnSight.submitToPublish");
		    String addComponentHTML = getLocalizedString(locale, "deliver.editOnSight.addComponentHTML");
			String deleteComponentHTML = getLocalizedString(locale, "deliver.editOnSight.deleteComponentHTML");
			String changeComponentHTML = getLocalizedString(locale, "deliver.editOnSight.changeComponentHTML");
			String accessRightsHTML = getLocalizedString(locale, "deliver.editOnSight.accessRightsHTML");
			String pageComponentsHTML = getLocalizedString(locale, "deliver.editOnSight.pageComponentsHTML");
			String viewSourceHTML = getLocalizedString(locale, "deliver.editOnSight.viewSourceHTML");
			String componentEditorInNewWindowHTML = getLocalizedString(locale, "deliver.editOnSight.componentEditorInNewWindowHTML");
			String savePageTemplateHTML = getLocalizedString(locale, "deliver.editOnSight.savePageTemplateHTML");
			String savePagePartTemplateHTML = getLocalizedString(locale, "deliver.editOnSight.savePagePartTemplateHTML");
			String favouriteComponentsHeader = getLocalizedString(locale, "tool.common.favouriteComponentsHeader");

			String saveTemplateUrl = "saveComponentStructure('" + componentEditorUrl + "CreatePageTemplate!input.action?contentId=" + templateController.getSiteNode(deliveryContext.getSiteNodeId()).getMetaInfoContentId() + "');";
			String savePartTemplateUrl = "savePartComponentStructure('" + componentEditorUrl + "CreatePageTemplate!input.action?contentId=" + templateController.getSiteNode(deliveryContext.getSiteNodeId()).getMetaInfoContentId() + "');";
			if(!hasSaveTemplateAccess)
			{
				saveTemplateUrl = "alert('Not authorized to save template');";
				savePartTemplateUrl = "alert('Not authorized to save part template');";
			}
			
			extraBody = extraBody.replaceAll("\\$siteNodeId", "" + templateController.getSiteNodeId());
			extraBody = extraBody.replaceAll("\\$repositoryId", "" + templateController.getSiteNode().getRepositoryId());
			extraBody = extraBody.replaceAll("\\$originalFullURL", URLEncoder.encode(templateController.getOriginalFullURL(), "UTF-8"));
			
			extraBody = extraBody.replaceAll("\\$submitToPublishHTML", submitToPublishHTML);
			extraBody = extraBody.replaceAll("\\$addComponentHTML", addComponentHTML);
			extraBody = extraBody.replaceAll("\\$deleteComponentHTML", deleteComponentHTML);
			extraBody = extraBody.replaceAll("\\$changeComponentHTML", changeComponentHTML);
		    extraBody = extraBody.replaceAll("\\$accessRightsHTML", accessRightsHTML);
		    
		    extraBody = extraBody.replaceAll("\\$pageComponents", pageComponentsHTML);
		    extraBody = extraBody.replaceAll("\\$componentEditorInNewWindowHTML", componentEditorInNewWindowHTML);
		    extraBody = extraBody.replaceAll("\\$savePageTemplateHTML", savePageTemplateHTML);
		    extraBody = extraBody.replaceAll("\\$savePagePartTemplateHTML", savePagePartTemplateHTML);
		    extraBody = extraBody.replaceAll("\\$saveTemplateUrl", saveTemplateUrl);
		    extraBody = extraBody.replaceAll("\\$savePartTemplateUrl", savePartTemplateUrl);
		    extraBody = extraBody.replaceAll("\\$viewSource", viewSourceHTML);
			extraBody = extraBody.replaceAll("\\$favouriteComponentsHeader", favouriteComponentsHeader);
		    
		    extraBody = extraBody.replaceAll("\\$addComponentJavascript", "var hasAccessToAddComponent" + component.getId() + "_" + component.getSlotName() + " = " + hasAccessToAddComponent + ";");
		    extraBody = extraBody.replaceAll("\\$deleteComponentJavascript", "var hasAccessToDeleteComponent" + component.getSlotName() + " = " + hasAccessToDeleteComponent + ";");
		    extraBody = extraBody.replaceAll("\\$changeComponentJavascript", "var hasAccessToChangeComponent" + component.getSlotName() + " = " + hasAccessToChangeComponent + ";");
		    extraBody = extraBody.replaceAll("\\$changeAccessJavascript", "var hasAccessToAccessRights" + component.getSlotName() + " = " + hasAccessToAccessRights + ";");
		    		    
		    extraBody = extraBody.replaceAll("\\$submitToPublishJavascript", "var hasAccessToSubmitToPublish = " + hasSubmitToPublishAccess + ";");
		    extraBody = extraBody.replaceAll("\\$pageStructureJavascript", "var hasPageStructureAccess = " + hasPageStructureAccess + ";");
		    extraBody = extraBody.replaceAll("\\$openInNewWindowJavascript", "var hasOpenInNewWindowAccess = " + hasOpenInNewWindowAccess + ";");
		    extraBody = extraBody.replaceAll("\\$allowViewSourceJavascript", "var hasAccessToViewSource = " + hasViewSourceAccess + ";");

		    //List tasks = getTasks();
			//component.setTasks(tasks);
			
			//String tasks = templateController.getContentAttribute(component.getContentId(), "ComponentTasks", true);
			
			/*
			Map context = new HashMap();
			context.put("templateLogic", templateController);
			StringWriter cacheString = new StringWriter();
			PrintWriter cachedStream = new PrintWriter(cacheString);
			new VelocityTemplateProcessor().renderTemplate(context, cachedStream, extraBody);
			extraBody = cacheString.toString();
			*/
			
			//extraHeader.replaceAll()
			
			timer.printElapsedTime("Read files");
			
			StringBuffer modifiedTemplate = new StringBuffer(template);
			
			//Adding stuff in the header
			int indexOfHeadEndTag = modifiedTemplate.indexOf("<head");
			if(indexOfHeadEndTag == -1)
				indexOfHeadEndTag = modifiedTemplate.indexOf("<HEAD");
			
			if(indexOfHeadEndTag > -1)
			{
				modifiedTemplate = modifiedTemplate.replace(indexOfHeadEndTag, modifiedTemplate.indexOf(">", indexOfHeadEndTag) + 1, extraHeader);
			}
			else
			{
				int indexOfHTMLStartTag = modifiedTemplate.indexOf("<html");
				if(indexOfHTMLStartTag == -1)
					indexOfHTMLStartTag = modifiedTemplate.indexOf("<HTML");
		
				if(indexOfHTMLStartTag > -1)
				{
					modifiedTemplate = modifiedTemplate.insert(modifiedTemplate.indexOf(">", indexOfHTMLStartTag) + 1, "" + extraHeader + "</head>");
				}
				else
				{
					logger.info("The current template is not a valid document. It does not comply with the simplest standards such as having a correct header.");
				}
			}

			timer.printElapsedTime("Header handled");

			//Adding stuff in the body	
			int indexOfBodyStartTag = modifiedTemplate.indexOf("<body");
			if(indexOfBodyStartTag == -1)
				indexOfBodyStartTag = modifiedTemplate.indexOf("<BODY");
			
			if(indexOfBodyStartTag > -1)
			{
			    //String pageComponentStructureDiv = "";
				//String pageComponentStructureDiv = getPageComponentStructureDiv(templateController, deliveryContext.getSiteNodeId(), deliveryContext.getLanguageId(), component);
				timer.printElapsedTime("pageComponentStructureDiv");
				String componentPaletteDiv = getComponentPaletteDiv(deliveryContext.getSiteNodeId(), deliveryContext.getLanguageId(), templateController);
				//String componentPaletteDiv = "";
				timer.printElapsedTime("componentPaletteDiv");
				modifiedTemplate = modifiedTemplate.insert(modifiedTemplate.indexOf(">", indexOfBodyStartTag) + 1, extraBody + componentPaletteDiv);
				//modifiedTemplate = modifiedTemplate.insert(modifiedTemplate.indexOf(">", indexOfBodyStartTag) + 1, extraBody + componentPaletteDiv);
			}
			else
			{
				logger.info("The current template is not a valid document. It does not comply with the simplest standards such as having a correct body.");
			}
			
			timer.printElapsedTime("Body handled");

			decoratedTemplate = modifiedTemplate.toString();
		}
		catch(Exception e)
		{
			logger.warn("An error occurred when deliver tried to decorate your template to enable onSiteEditing. Reason " + e.getMessage(), e);
		}
		
		return decoratedTemplate;
	}

	private StringBuffer getPagePathAsCommaseparatedIds(TemplateController templateController)
	{
		StringBuffer path = new StringBuffer("");
		
		SiteNodeVO currentSiteNode = templateController.getSiteNode();
		while(currentSiteNode != null)
		{
			path.insert(0, "," + currentSiteNode.getId().toString());
			if(currentSiteNode.getParentSiteNodeId() != null)
				currentSiteNode = templateController.getSiteNode(currentSiteNode.getParentSiteNodeId());
			else
				currentSiteNode = null;
		}
		return path;
	}

   
	private String decorateComponent(InfoGlueComponent component, TemplateController templateController, Integer repositoryId, Integer siteNodeId, Integer languageId, Integer contentId/*, Integer metainfoContentId*/, int maxDepth, int currentDepth) throws Exception
	{
		if(currentDepth > maxDepth)
		{
			logger.error("A page with to many levels (possibly infinite loop) was found on " + templateController.getOriginalFullURL());
			return "";
		}
		
		String decoratedComponent = "";
		
		//logger.info("decorateComponent.contentId:" + contentId);

		//logger.info("decorateComponent:" + component.getName());
		
		String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();

		Timer timer = new Timer();
		timer.setActive(false);

		try
		{
			String componentString = getComponentString(templateController, component.getContentId(), component); 

			if(component.getParentComponent() == null && templateController.getDeliveryContext().getShowSimple())
			{
			    templateController.getDeliveryContext().setContentType("text/html");
			    templateController.getDeliveryContext().setDisablePageCache(true);
			    componentString = "<html><head></head><body onload=\"toggleDiv('pageComponents');\">" + componentString + "</body></html>";
			}
			
			templateController.setComponentLogic(new DecoratedComponentLogic(templateController, component));
			Map context = super.getDefaultContext();
			context.put("templateLogic", templateController);
			context.put("model", component.getModel());
			StringWriter cacheString = new StringWriter();
			PrintWriter cachedStream = new PrintWriter(cacheString);
			new VelocityTemplateProcessor().renderTemplate(context, cachedStream, componentString, false, component);
			componentString = cacheString.toString();
	
			int bodyIndex = componentString.indexOf("<body");
			if(bodyIndex == -1)
				bodyIndex = componentString.indexOf("<BODY");
		
			if(component.getParentComponent() == null && bodyIndex > -1)
			{
				String onContextMenu = " id=\"base0_0Comp\" onload=\"javascript:setToolbarInitialPosition();\"";
				if(templateController.getDeliveryContext().getShowSimple())
					onContextMenu = " id=\"base0_0Comp\" onload=\"javascript:setToolbarInitialPosition();\"";
				
				
				StringBuffer sb = new StringBuffer(componentString);
				sb.insert(bodyIndex + 5, onContextMenu);
				componentString = sb.toString();

				Document componentPropertiesDocument = getComponentPropertiesDOM4JDocument(templateController, siteNodeId, languageId, component.getContentId()); 
				//this.propertiesDivs += getComponentPropertiesDiv(templateController, repositoryId, siteNodeId, languageId, contentId, component.getId(), component.getContentId(), componentPropertiesDocument, component);

				Document componentTasksDocument = getComponentTasksDOM4JDocument(templateController, siteNodeId, languageId, component.getContentId()); 
				//this.tasksDivs += getComponentTasksDiv(repositoryId, siteNodeId, languageId, contentId, component, 0, 1, componentTasksDocument, templateController);
			}
	
			int offset = 0;
			int slotStartIndex = componentString.indexOf("<ig:slot", offset);
			//logger.info("slotStartIndex:" + slotStartIndex);
			while(slotStartIndex > -1)
			{
				decoratedComponent += componentString.substring(offset, slotStartIndex);
				int slotStopIndex = componentString.indexOf("</ig:slot>", slotStartIndex);
				
				String slot = componentString.substring(slotStartIndex, slotStopIndex + 10);
				String id = slot.substring(slot.indexOf("id") + 4, slot.indexOf("\"", slot.indexOf("id") + 4));
				
				Slot slotBean = new Slot();
			    slotBean.setId(id);

			    String[] allowedComponentNamesArray = null;
			    int allowedComponentNamesIndex = slot.indexOf(" allowedComponentNames");
				if(allowedComponentNamesIndex > -1)
				{    
				    String allowedComponentNames = slot.substring(allowedComponentNamesIndex + 24, slot.indexOf("\"", allowedComponentNamesIndex + 24));
				    allowedComponentNamesArray = allowedComponentNames.split(",");
				    slotBean.setAllowedComponentsArray(allowedComponentNamesArray);
				}

				String[] disallowedComponentNamesArray = null;
				int disallowedComponentNamesIndex = slot.indexOf(" disallowedComponentNames");
				if(disallowedComponentNamesIndex > -1)
				{    
				    String disallowedComponentNames = slot.substring(disallowedComponentNamesIndex + 27, slot.indexOf("\"", disallowedComponentNamesIndex + 27));
				    disallowedComponentNamesArray = disallowedComponentNames.split(",");
				    slotBean.setDisallowedComponentsArray(disallowedComponentNamesArray);
				}

				String[] allowedComponentGroupNamesArray = null;
				int allowedComponentGroupNamesIndex = slot.indexOf(" allowedComponentGroupNames");
				if(allowedComponentGroupNamesIndex > -1)
				{    
				    String allowedComponentGroupNames = slot.substring(allowedComponentGroupNamesIndex + 29, slot.indexOf("\"", allowedComponentGroupNamesIndex + 29));
				    allowedComponentGroupNamesArray = allowedComponentGroupNames.split(",");
				    slotBean.setAllowedComponentGroupsArray(allowedComponentGroupNamesArray);
				}

				boolean inherit = true;
				int inheritIndex = slot.indexOf("inherit");
				if(inheritIndex > -1)
				{    
				    String inheritString = slot.substring(inheritIndex + 9, slot.indexOf("\"", inheritIndex + 9));
				    inherit = Boolean.parseBoolean(inheritString);
				}
				slotBean.setInherit(inherit);
				
				boolean disableAccessControl = false;
				int disableAccessControlIndex = slot.indexOf("disableAccessControl");
				if(disableAccessControlIndex > -1)
				{    
				    String disableAccessControlString = slot.substring(disableAccessControlIndex + "disableAccessControl".length() + 2, slot.indexOf("\"", disableAccessControlIndex + "disableAccessControl".length() + 2));
				    disableAccessControl = Boolean.parseBoolean(disableAccessControlString);
				}

				String addComponentText = null;
				int addComponentTextIndex = slot.indexOf("addComponentText");
				if(addComponentTextIndex > -1)
				{    
				    addComponentText = slot.substring(addComponentTextIndex + "addComponentText".length() + 2, slot.indexOf("\"", addComponentTextIndex + "addComponentText".length() + 2));
				}

				String addComponentLinkHTML = null;
				int addComponentLinkHTMLIndex = slot.indexOf("addComponentLinkHTML");
				if(addComponentLinkHTMLIndex > -1)
				{    
				    addComponentLinkHTML = slot.substring(addComponentLinkHTMLIndex + "addComponentLinkHTML".length() + 2, slot.indexOf("\"", addComponentLinkHTMLIndex + "addComponentLinkHTML".length() + 2));
				}
				
				if(addComponentLinkHTML == null || addComponentLinkHTML.equals(""))
				{
					Locale locale = templateController.getLocaleAvailableInTool(templateController.getPrincipal());
					addComponentLinkHTML = getLocalizedString(locale, "deliver.editOnSight.slotInstructionHTML");
				}
				
				int allowedNumberOfComponentsInt = -1;
				int allowedNumberOfComponentsIndex = slot.indexOf("allowedNumberOfComponents");
				if(allowedNumberOfComponentsIndex > -1)
				{    
					String allowedNumberOfComponents = slot.substring(allowedNumberOfComponentsIndex + "allowedNumberOfComponents".length() + 2, slot.indexOf("\"", allowedNumberOfComponentsIndex + "allowedNumberOfComponents".length() + 2));
					try
					{
						allowedNumberOfComponentsInt = new Integer(allowedNumberOfComponents);
					}
					catch (Exception e) 
					{
						allowedNumberOfComponentsInt = -1;
					}
				}

				int disableSlotDecorationIndex = slot.indexOf("disableSlotDecoration");
				Boolean disableSlotDecoration = false;
				if(disableSlotDecorationIndex > -1)
				{    
					String disableSlotDecorationString = slot.substring(disableSlotDecorationIndex + "disableSlotDecoration".length() + 2, slot.indexOf("\"", disableSlotDecorationIndex + "disableSlotDecoration".length() + 2));
					if(disableSlotDecorationString.equalsIgnoreCase("true"))
						disableSlotDecoration = true;
				}

				slotBean.setDisableAccessControl(disableAccessControl);
				slotBean.setAddComponentLinkHTML(addComponentLinkHTML);
			    slotBean.setAddComponentText(addComponentText);
			    slotBean.setAllowedNumberOfComponents(new Integer(allowedNumberOfComponentsInt));
				component.setContainerSlot(slotBean);
				
				String subComponentString = "";
				
				if(!disableSlotDecoration)
				{
					if(component.getIsInherited())
						subComponentString += "<div id=\"" + component.getId() + "_" + id + "\" class=\"inheritedComponentDiv\");\">";
					else
					{
					    subComponentString += "<div id=\"" + component.getId() + "_" + id + "\" class=\"componentDiv slotPosition\">";
					    
					    subComponentString += "\n<script type=\"text/javascript\">\n";
					    subComponentString += "    $(document).ready(function() {";
	
					    subComponentString += "    var element = document.getElementById(\"" + component.getId() + "_" + id + "\");\n";
					    subComponentString += "    if (document.addEventListener != null)\n";
						subComponentString += "		 	element.addEventListener('mouseup', function (event){if(!$('#" + component.getId() + "_" + id + "').hasClass(\"slotPosition\")) { return;} assignComponent(event, '" + siteNodeId + "', '" + languageId + "', '" + contentId + "', '" + component.getId() + "', '" + id + "', '" + false + "', '" + slotBean.getAllowedComponentsArrayAsUrlEncodedString() + "', '" + slotBean.getDisallowedComponentsArrayAsUrlEncodedString() + "', '" + slotBean.getAllowedComponentGroupsArrayAsUrlEncodedString() + "', '');}, false);\n";
					    subComponentString += "    else{\n";
					    subComponentString += "		 	element.attachEvent(\"onmouseup\", function (evt){if(!$('#" + component.getId() + "_" + id + "').hasClass(\"slotPosition\")) { return;} assignComponent(evt, '" + siteNodeId + "', '" + languageId + "', '" + contentId + "', '" + component.getId() + "', '" + id + "', '" + false + "', '" + slotBean.getAllowedComponentsArrayAsUrlEncodedString() + "', '" + slotBean.getDisallowedComponentsArrayAsUrlEncodedString() + "', '" + slotBean.getAllowedComponentGroupsArrayAsUrlEncodedString() + "', '');\n";
					    subComponentString += "		});";
					    subComponentString += "    }\n";
					    subComponentString += " });\n";
					    subComponentString += "</script>\n";
					}
				}
				
				List subComponents = getInheritedComponents(getDatabase(), templateController, component, templateController.getSiteNodeId(), id, inherit);

			    InfoGluePrincipal principal = templateController.getPrincipal();
			    String cmsUserName = (String)templateController.getHttpServletRequest().getSession().getAttribute("cmsUserName");
			    if(cmsUserName != null && !CmsPropertyHandler.getAnonymousUser().equalsIgnoreCase(cmsUserName))
				    principal = templateController.getPrincipal(cmsUserName);

				String clickToAddHTML = "";
				boolean hasAccessToAddComponent = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.AddComponent", "" + component.getContentId() + "_" + id);
				if(slotBean.getDisableAccessControl())
					hasAccessToAddComponent = true;
				
			    boolean hasMaxComponents = false;
			    //logger.info("Checking for max components on: " + id);
				if(component.getSlotList() != null)
				{
					Iterator slotListIterator = component.getSlotList().iterator();
					while(slotListIterator.hasNext())
					{
						Slot parentSlot = (Slot)slotListIterator.next();
						if(parentSlot.getId().equalsIgnoreCase(id))
						{
							//logger.info("parentSlot.getAllowedNumberOfComponents(): " + parentSlot.getAllowedNumberOfComponents());
							//logger.info("parentSlot.getComponents().size(): " + parentSlot.getComponents().size());
							if(parentSlot.getAllowedNumberOfComponents() != -1 && parentSlot.getComponents().size() >= parentSlot.getAllowedNumberOfComponents())
								hasMaxComponents = true;
						}
					}
				}
				
				if(hasAccessToAddComponent && !hasMaxComponents)
				{
					if(slotBean.getAddComponentText() != null)
					{
						clickToAddHTML = slotBean.getAddComponentText();
						//logger.info("Fick:" + clickToAddHTML);
					}
					else
					{
						Locale locale = templateController.getLocaleAvailableInTool(principal);
						clickToAddHTML = getLocalizedString(locale, "deliver.editOnSight.slotInstructionHTML");
						//logger.info("Fack:" + clickToAddHTML + locale);
					}
				}
				else
				{
					addComponentLinkHTML = "";
					clickToAddHTML = "";
				}
				//logger.info("addComponentLinkHTML:" + addComponentLinkHTML);
				//logger.info("clickToAddHTML:" + clickToAddHTML);
				//logger.info("hasMaxComponents:" + hasMaxComponents);
				
				//logger.info("subComponents for " + id + ":" + subComponents);
				if(subComponents != null && subComponents.size() > 0)
				{
					//logger.info("SUBCOMPONENTS:" + subComponents.size());
					int index = 0;
					Iterator subComponentsIterator = subComponents.iterator();
					while(subComponentsIterator.hasNext())
					{
						InfoGlueComponent subComponent = (InfoGlueComponent)subComponentsIterator.next();
						if(subComponent != null)
						{
							component.getComponents().put(subComponent.getSlotName(), subComponent);
							if(subComponent.getIsInherited() && subComponent.getPagePartTemplateComponent() == null)
							{
								//logger.info("Inherited..." + contentId);
								String childComponentsString = decorateComponent(subComponent, templateController, repositoryId, siteNodeId, languageId, contentId/*, metainfoContentId*/, maxDepth, currentDepth + 1);
								if(!this.getTemplateController().getDeliveryContext().getShowSimple()/* && !disableSlotDecoration */)
								    subComponentString += "<a id=\"" + index + "Comp\" name=\"" + index + "Comp\"></a><span id=\""+ id + index + "Comp\" class=\"inheritedslot\">" + childComponentsString + "</span>";
								else
								    subComponentString += childComponentsString;
								    
								Document componentPropertiesDocument = getComponentPropertiesDOM4JDocument(templateController, siteNodeId, languageId, component.getContentId()); 
								//this.propertiesDivs += getComponentPropertiesDiv(templateController, repositoryId, siteNodeId, languageId, contentId, new Integer(siteNodeId.intValue()*100 + subComponent.getId().intValue()), subComponent.getContentId(), componentPropertiesDocument, subComponent);
								
								Document componentTasksDocument = getComponentTasksDOM4JDocument(templateController, siteNodeId, languageId, subComponent.getContentId()); 
								//this.tasksDivs += getComponentTasksDiv(repositoryId, siteNodeId, languageId, contentId, subComponent, index, subComponents.size() - 1, componentTasksDocument, templateController);
							}
							else
							{
								//logger.info("Not inherited..." + contentId);
								String childComponentsString = decorateComponent(subComponent, templateController, repositoryId, siteNodeId, languageId, contentId/*, metainfoContentId*/, maxDepth, currentDepth + 1);
								//logger.info("childComponentsString:" + childComponentsString);
								
								if(!this.getTemplateController().getDeliveryContext().getShowSimple() && !disableSlotDecoration)
								{    
								    String allowedComponentNamesAsEncodedString = null;
								    String disallowedComponentNamesAsEncodedString = null;
								    String allowedComponentGroupNamesAsEncodedString = null;

								    for(int i=0; i < subComponent.getParentComponent().getSlotList().size(); i++)
								    {
								        Slot subSlotBean = (Slot)subComponent.getParentComponent().getSlotList().get(i);
								        
								        if(subSlotBean.getId() != null && subSlotBean.getId().equals(subComponent.getSlotName()))
								        {
								            allowedComponentNamesAsEncodedString = subSlotBean.getAllowedComponentsArrayAsUrlEncodedString();
								            disallowedComponentNamesAsEncodedString = subSlotBean.getDisallowedComponentsArrayAsUrlEncodedString();
								            allowedComponentGroupNamesAsEncodedString = subSlotBean.getAllowedComponentGroupsArrayAsUrlEncodedString();
								            subComponent.setContainerSlot(subSlotBean);
								        }
								    }

								    
								    if(subComponent.getPagePartTemplateComponent() != null)
								    {
								    	subComponent.setComponentDivId("" + id + index + "_" + subComponent.getPagePartTemplateComponent().getId() + "Comp");
									    subComponentString += "<a id=\"" + subComponent.getPagePartTemplateComponent().getId() + "Comp\" name=\"" + subComponent.getPagePartTemplateComponent().getId() + "Comp\"></a><span class=\"dragableComponent slotPosition\" id=\"" + id + index + "_" + subComponent.getPagePartTemplateComponent().getId() + "Comp\">" + childComponentsString + "<script type=\"text/javascript\">initializeComponentEventHandler('" + id + index + "_" + subComponent.getPagePartTemplateComponent().getId() + "Comp', '" + subComponent.getPagePartTemplateComponent().getId() + "', '" + id + "', " + templateController.getSiteNode().getRepositoryId() + ", " + templateController.getSiteNodeId() + ", " + templateController.getLanguageId() + ", " + templateController.getContentId() + ", " + subComponent.getPagePartTemplateComponent().getId() + ", " + subComponent.getPagePartTemplateComponent().getContentId() + ", '" + URLEncoder.encode(templateController.getOriginalFullURL(), "UTF-8") + "');</script></span>";
								    }
								    else
								    {
								    	subComponent.setComponentDivId("" + id + index + "_" + subComponent.getId() + "Comp");
									    subComponentString += "<a id=\"" + subComponent.getId() + "Comp\" name=\"" + subComponent.getId() + "Comp\"></a><span class=\"dragableComponent slotPosition\" id=\"" + id + index + "_" + subComponent.getId() + "Comp\">" + childComponentsString + "<script type=\"text/javascript\">initializeComponentEventHandler('" + id + index + "_" + subComponent.getId() + "Comp', '" + subComponent.getId() + "', '" + id + "', " + templateController.getSiteNode().getRepositoryId() + ", " + templateController.getSiteNodeId() + ", " + templateController.getLanguageId() + ", " + templateController.getContentId() + ", " + subComponent.getId() + ", " + subComponent.getContentId() + ", '" + URLEncoder.encode(templateController.getOriginalFullURL(), "UTF-8") + "'); " + "registerOnMouseUp('" + id + index + "_" + subComponent.getId() + "Comp', " + templateController.getSiteNodeId() + ", " + templateController.getLanguageId() + ", " + templateController.getContentId() + ", '" + component.getId() + "', '" + id + "', false, '" + allowedComponentNamesAsEncodedString + "', '" + disallowedComponentNamesAsEncodedString + "','" + allowedComponentGroupNamesAsEncodedString + "', " + subComponent.getId() + ");</script></span>";
								    }
								}
								else
								{
								    subComponentString += childComponentsString;
								}
								
								if(subComponent.getPagePartTemplateComponent() != null)
							    {
									Document componentPropertiesDocument = getComponentPropertiesDOM4JDocument(templateController, siteNodeId, languageId, subComponent.getPagePartTemplateComponent().getContentId()); 
									//this.propertiesDivs += getComponentPropertiesDiv(templateController, repositoryId, siteNodeId, languageId, contentId, subComponent.getPagePartTemplateComponent().getId(), subComponent.getPagePartTemplateComponent().getContentId(), componentPropertiesDocument, subComponent.getPagePartTemplateComponent());
									Document componentTasksDocument = getComponentTasksDOM4JDocument(templateController, siteNodeId, languageId, subComponent.getPagePartTemplateComponent().getContentId()); 
									
									//this.tasksDivs += getComponentTasksDiv(repositoryId, siteNodeId, languageId, contentId, subComponent.getPagePartTemplateComponent(), index, subComponents.size() - 1, componentTasksDocument, templateController);
							    }
							    else
							    {
									Document componentPropertiesDocument = getComponentPropertiesDOM4JDocument(templateController, siteNodeId, languageId, subComponent.getContentId()); 
									//this.propertiesDivs += getComponentPropertiesDiv(templateController, repositoryId, siteNodeId, languageId, contentId, subComponent.getId(), subComponent.getContentId(), componentPropertiesDocument, subComponent);
									Document componentTasksDocument = getComponentTasksDOM4JDocument(templateController, siteNodeId, languageId, subComponent.getContentId()); 

									//this.tasksDivs += getComponentTasksDiv(repositoryId, siteNodeId, languageId, contentId, subComponent, index, subComponents.size() - 1, componentTasksDocument, templateController);
							    }
							}
						}
						index++;
					}
					
					if(component.getContainerSlot().getAddComponentLinkHTML() != null && !component.getIsInherited() && !hasMaxComponents && !disableSlotDecoration)
					{
					    String allowedComponentNamesAsEncodedString = null;
					    String disallowedComponentNamesAsEncodedString = null;
					    String allowedComponentGroupNamesAsEncodedString = null;
					    
					    for(int i=0; i < component.getSlotList().size(); i++)
					    {
					        Slot subSlotBean = (Slot)component.getSlotList().get(i);
					        if(subSlotBean.getId() != null && subSlotBean.getId().equals(id))
					        {
					            allowedComponentNamesAsEncodedString = subSlotBean.getAllowedComponentsArrayAsUrlEncodedString();
					            disallowedComponentNamesAsEncodedString = subSlotBean.getDisallowedComponentsArrayAsUrlEncodedString();
					            allowedComponentGroupNamesAsEncodedString = subSlotBean.getAllowedComponentGroupsArrayAsUrlEncodedString();
					        }
					    }

						String linkUrl = componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + (contentId == null ? "-1" : contentId) + "&parentComponentId=" + component.getId() + "&slotId=" + id + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + ((allowedComponentNamesAsEncodedString != null) ? "&" + allowedComponentNamesAsEncodedString : "") + ((disallowedComponentNamesAsEncodedString != null) ? "&" + disallowedComponentNamesAsEncodedString : "") + ((allowedComponentGroupNamesAsEncodedString != null) ? "&" + allowedComponentGroupNamesAsEncodedString : "");
						//logger.info("clickToAddHTML 1:" + component.getContainerSlot().getAddComponentLinkHTML().replaceAll("\\$linkUrl", linkUrl));
						subComponentString += "" + component.getContainerSlot().getAddComponentLinkHTML().replaceAll("\\$linkUrl", linkUrl);
					}
					else if(!component.getIsInherited() && !hasMaxComponents && !disableSlotDecoration)
					{
						//logger.info("clickToAddHTML 2:" + clickToAddHTML);
						subComponentString += "" + clickToAddHTML;
					}
				}
				else
				{
					if(component.getContainerSlot().getAddComponentLinkHTML() != null && !component.getIsInherited() && !hasMaxComponents && !disableSlotDecoration)
					{
					    String allowedComponentNamesAsEncodedString = null;
					    String disallowedComponentNamesAsEncodedString = null;
					    String allowedComponentGroupNamesAsEncodedString = null;
					    
					    for(int i=0; i < component.getSlotList().size(); i++)
					    {
					        Slot subSlotBean = (Slot)component.getSlotList().get(i);
					        if(subSlotBean.getId() != null && subSlotBean.getId().equals(id))
					        {
					            allowedComponentNamesAsEncodedString = subSlotBean.getAllowedComponentsArrayAsUrlEncodedString();
					            disallowedComponentNamesAsEncodedString = subSlotBean.getDisallowedComponentsArrayAsUrlEncodedString();
					            allowedComponentGroupNamesAsEncodedString = subSlotBean.getAllowedComponentGroupsArrayAsUrlEncodedString();
					        }
					    }

						String linkUrl = componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?BBB=1&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + (contentId == null ? "-1" : contentId) + "&parentComponentId=" + component.getId() + "&slotId=" + id + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + ((allowedComponentNamesAsEncodedString != null) ? "&" + allowedComponentNamesAsEncodedString : "") + ((disallowedComponentNamesAsEncodedString != null) ? "&" + disallowedComponentNamesAsEncodedString : "") + ((allowedComponentGroupNamesAsEncodedString != null) ? "&" + allowedComponentGroupNamesAsEncodedString : "");
						//logger.info("clickToAddHTML 3:" + component.getContainerSlot().getAddComponentLinkHTML().replaceAll("\\$linkUrl", linkUrl));
						subComponentString += "" + component.getContainerSlot().getAddComponentLinkHTML().replaceAll("\\$linkUrl", linkUrl);
					}
					else if(!component.getIsInherited() && !hasMaxComponents && !disableSlotDecoration)
					{
						//logger.info("clickToAddHTML 4:" + clickToAddHTML);
						subComponentString += "" + clickToAddHTML;
					}
				}
				
				if(!disableSlotDecoration)
				{
					if(!component.getIsInherited())
					{
					    String allowedComponentNamesAsEncodedString = null;
					    String disallowedComponentNamesAsEncodedString = null;
					    String allowedComponentGroupNamesAsEncodedString = null;
					    
					    for(int i=0; i < component.getSlotList().size(); i++)
					    {
					        Slot subSlotBean = (Slot)component.getSlotList().get(i);
					        if(subSlotBean.getId() != null && subSlotBean.getId().equals(id))
					        {
					            allowedComponentNamesAsEncodedString = subSlotBean.getAllowedComponentsArrayAsUrlEncodedString();
					            disallowedComponentNamesAsEncodedString = subSlotBean.getDisallowedComponentsArrayAsUrlEncodedString();
					            allowedComponentGroupNamesAsEncodedString = subSlotBean.getAllowedComponentGroupsArrayAsUrlEncodedString();
					        }
					    }
	
					    subComponentString += "<script type=\"text/javascript\">initializeSlotEventHandler('" + component.getId() + "_" + id + "', '" + id + "', '" + component.getContentId() + "', " + templateController.getSiteNode().getRepositoryId() + ", " + templateController.getSiteNodeId() + ", " + templateController.getLanguageId() + ", " + templateController.getContentId() + ", " + component.getId() + ", " + component.getContentId() + ", '" + URLEncoder.encode(templateController.getOriginalFullURL(), "UTF-8") + "');</script></div>";
					}
					else
					    subComponentString += "</div>";
				}
				
				decoratedComponent += subComponentString;
							
				offset = slotStopIndex + 10;
				slotStartIndex = componentString.indexOf("<ig:slot", offset);
			}
			
			//logger.info("offset:" + offset);
			decoratedComponent += componentString.substring(offset);
		}
		catch(Exception e)
		{		
			logger.warn("An component with either an empty template or with no template in the sitelanguages was found:" + e.getMessage(), e);	
		}
		
		return decoratedComponent;
	}


	

	/**
	 * This method creates the tabpanel for the component-palette.
	 */
	
	private String getComponentPaletteDiv(Integer siteNodeId, Integer languageId, TemplateController templateController) throws Exception
	{		
		InfoGluePrincipal principal = templateController.getPrincipal();
	    String cmsUserName = (String)templateController.getHttpServletRequest().getSession().getAttribute("cmsUserName");
	    if(cmsUserName != null)
		    principal = templateController.getPrincipal(cmsUserName);

		if(!templateController.getDeliveryContext().getShowSimple())
	    {
		    boolean hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "StructureTool.Palette", false, true, false);
		    if(!hasAccess || templateController.getRequestParameter("skipToolbar") != null && templateController.getRequestParameter("skipToolbar").equalsIgnoreCase("true"))
		        return "";
	    }
	    
		ContentVO contentVO = templateController.getBoundContent(BasicTemplateController.META_INFO_BINDING_NAME);

		//Cache
		String key = "" + templateController.getPrincipal().getName();
		String componentPaletteDiv = (String)CacheController.getCachedObject("componentPaletteDivCache", key);
		if(componentPaletteDiv != null)
		{
			if(componentPaletteDiv != null && (templateController.getRequestParameter("refresh") == null || !templateController.getRequestParameter("refresh").equalsIgnoreCase("true")))
			{
				return componentPaletteDiv.replaceAll("CreatePageTemplate\\!input.action\\?contentId=.*?'", "CreatePageTemplate!input.action?contentId=" + contentVO.getContentId() + "'");
			}
		}
		//End Cache
		
		StringBuffer sb = new StringBuffer();
			
		String componentEditorUrl 		= CmsPropertyHandler.getComponentEditorUrl();
		String componentRendererUrl 	= CmsPropertyHandler.getComponentRendererUrl();
		String componentRendererAction 	= CmsPropertyHandler.getComponentRendererAction();
		
		
		sb.append("<div id=\"buffer\" style=\"top: 0px; left: 0px; z-index:200;\"><img src=\"" + this.getRequest().getContextPath() + "/images/componentDraggedIcon.gif\"></div>");
		
		Map componentGroups = getComponentGroups(getComponentContents(), templateController);
		
		sb.append("<div id=\"paletteDiv\">");
		 
		sb.append("<div id=\"paletteHandle\">");
		sb.append("	<div id=\"leftPaletteHandle\">Component palette</div><div id=\"rightPaletteHandle\"><a href=\"javascript:hideDiv('paletteDiv');\" class=\"white\">close</a></div>");
		sb.append("</div>");

		sb.append("<div id=\"paletteBody\">");
		sb.append("<table class=\"tabPanel\" cellpadding=\"0\" cellspacing=\"0\">");
		sb.append(" <tr class=\"igtr\">");
		
		Iterator groupIterator = componentGroups.keySet().iterator();
		int index = 0;
		String groupName = "";
		String initialGroupName = "";
		while(groupIterator.hasNext())
		{
			groupName = (String)groupIterator.next();
			
			if(index == 0)
			{	
				sb.append("  <td id=\"" + groupName + "Tab\" valign=\"top\" class=\"thistab\" onclick=\"javascript:changeTab('" + groupName + "');\" height=\"20\"><nobr>" + groupName + "</nobr></td>");
				initialGroupName = groupName;
			}
			else if(!groupIterator.hasNext())
				sb.append("  <td id=\"" + groupName + "Tab\" valign=\"top\" class=\"igtab\" style=\"border-right: solid thin black\" onclick=\"javascript:changeTab('" + groupName + "');\"><nobr>" + groupName + "</nobr></td>");
			else
				sb.append("  <td id=\"" + groupName + "Tab\" valign=\"top\" class=\"igtab\" onclick=\"javascript:changeTab('" + groupName + "');\"><nobr>" + groupName + "</nobr></td>");

			index++;
		}
		
	    boolean hasSaveTemplateAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "StructureTool.SaveTemplate", "");
	    
		sb.append("  <td class=\"igpalettetd\" width=\"90%\" style=\"text-align: right; border-right: solid thin gray; border-bottom: solid thin white\" align=\"right\">&nbsp;<a href=\"javascript:refreshComponents(document.location.href);\" class=\"white\"><img src=\"" + this.getRequest().getContextPath() + "/images/refresh.gif\" alt=\"Refresh palette\" border=\"0\"></a>&nbsp;<a href=\"javascript:moveDivDown('paletteDiv');\" class=\"white\"><img src=\"" + this.getRequest().getContextPath() + "/images/arrowDown.gif\" alt=\"Move down\" border=\"0\"></a>&nbsp;<a href=\"javascript:moveDivUp('paletteDiv');\" class=\"white\"><img src=\"" + this.getRequest().getContextPath() + "/images/arrowUp.gif\" alt=\"Move up\" border=\"0\"></a>&nbsp;<a href=\"javascript:toggleDiv('pageComponents');\" class=\"white\"><img src=\"" + this.getRequest().getContextPath() + "/images/pageStructure.gif\" alt=\"Toggle page structure\" border=\"0\"></a>&nbsp;");
		if(hasSaveTemplateAccess)
		    sb.append("<a href=\"javascript:saveComponentStructure('" + componentEditorUrl + "CreatePageTemplate!input.action?contentId=" + contentVO.getId() + "');\" class=\"white\"><img src=\"" + this.getRequest().getContextPath() + "/images/saveComponentStructure.gif\" alt=\"Save the page as a template page\" border=\"0\"></a>&nbsp;");
		
		sb.append("<a href=\"javascript:window.open(document.location.href, 'PageComponents', '');\"><img src=\"" + this.getRequest().getContextPath() + "/images/fullscreen.gif\" alt=\"Pop up in a large window\" border=\"0\"></a>&nbsp;</td>");
		
		sb.append(" </tr>");
		sb.append("</table>");
		sb.append("</div>");
		
		sb.append("<script type=\"text/javascript\">");
		sb.append("var currentGroup = \"" + initialGroupName + "\";");
		sb.append("</script>");
				
		String openGroupName = "";

		groupIterator = componentGroups.keySet().iterator();
		index = 0;
		while(groupIterator.hasNext())
		{
			groupName = (String)groupIterator.next();

			if(index == 0)
			{
				sb.append("<div id=\"" + groupName + "ComponentsBg\" class=\"componentsBackground\" style=\"zIndex:3; visibility: inherited;\">");
				openGroupName = groupName;
			}
			else
			    sb.append("<div id=\"" + groupName + "ComponentsBg\" class=\"componentsBackground\" style=\"zIndex:2; visibility: inherited;\">");	
			
			sb.append("<div id=\"" + groupName + "Components\" style=\"visibility:inherit; position:absolute; top:1px; left:5px; height:50px; \">");
			sb.append("	<table class=\"igtable\" style=\"width:100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
			sb.append("	<tr class=\"igtr\">");
			//sb.append("	<td width=\"100%\"><nobr>");
			
			String imageUrl = this.getRequest().getContextPath() + "/images/componentIcon.gif";
			List components = (List)componentGroups.get(groupName); //getComponentContents();
			Iterator componentIterator = components.iterator();
			int componentIndex = 0;
			while(componentIterator.hasNext())
			{
				ContentVO componentContentVO = (ContentVO)componentIterator.next();
	
				//String imageUrlTemp = getDigitalAssetUrl(componentContentVO.getId(), "thumbnail");
				//if(imageUrlTemp != null && imageUrlTemp.length() > 0)
				//	imageUrl = imageUrlTemp;
				sb.append("	<td class=\"igpalettetd\">");
				sb.append("		<div id=\"" + componentIndex + "\" style=\"display: block; visibility: inherited;\"><nobr><img src=\"" + imageUrl + "\" width=\"16\" height=\"16\" border=\"0\">");
				sb.append("		<span onMouseDown=\"grabIt(event);\" onmouseover=\"showDetails('" + componentContentVO.getName() + "');\" id=\""+ componentContentVO.getId() + "\" class=\"draggableItem\" nowrap=\"1\">" + ((componentContentVO.getName().length() > 22) ? componentContentVO.getName().substring(0, 17) : componentContentVO.getName()) + "...</span>");
				sb.append("     </nobr></div>"); 
				sb.append("	</td>");
				
				imageUrl = this.getRequest().getContextPath() + "/images/componentIcon.gif";
			}
			sb.append("  <td class=\"igpalettetd\" width=\"90%\">&nbsp;</td>");
			
			//sb.append("	</nobr></td>");
			sb.append("	</tr>");
			sb.append("	</table>");
			sb.append("</div>");
			
			sb.append("</div>");
			
			sb.append("<script type=\"text/javascript\"> if (bw.bw) tabInit('" + groupName + "Components'); </script>");

			
			index++;
		}
		
		sb.append("<div id=\"statusListBg\">");
		sb.append("<table class=\"igtable\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
		sb.append("<tr class=\"igtr\">");
		sb.append("	<td class=\"igpalettetd\" align=\"left\" width=\"15px\">&nbsp;<a href=\"#\" onclick=\"moveLeft(currentGroup)\" return false\" onfocus=\"if(this.blur)this.blur()\"><img src=\"" + this.getRequest().getContextPath() + "/images/arrowleft.gif\" alt=\"previous\" border=\"0\"></a></td>");
		sb.append("	<td class=\"igpalettetd\" align=\"left\" width=\"95%\"><span class=\"componentsStatusText\">Details: </span><span id=\"statusText\" class=\"componentsStatusText\">&nbsp;</span></td>");
		sb.append("	<td class=\"igpalettetd\" align=\"right\"><a href=\"#\" onclick=\"moveRight(currentGroup)\" return false\" onfocus=\"if(this.blur)this.blur()\"><img src=\"" + this.getRequest().getContextPath() + "/images/arrowright.gif\" alt=\"next\" border=\"0\"></a>&nbsp;</td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</div>");

		sb.append("	<script type=\"text/javascript\">");
		sb.append("	  	changeTab('" + openGroupName + "');");
		
		sb.append("		var theHandle = document.getElementById(\"paletteHandle\");");
		sb.append("		var theRoot   = document.getElementById(\"paletteDiv\");");

		sb.append("		$(theHandle).css('cursor', 'move');\n");
		sb.append("		$(theRoot).draggable({handle: theHandle, cursor: 'move', distance: 10});\n");

		sb.append("	</script>");

		sb.append("</div>");
		
		//Caching the result
		componentPaletteDiv = sb.toString();
		CacheController.cacheObject("componentPaletteDivCache", key, componentPaletteDiv);				
		
		return componentPaletteDiv;
	}

	/**
	 * This method gets all component groups from the available components.
	 * This is dynamically so if one states a different group in the component the group is created.
	 */

	private Map getComponentGroups(List components, TemplateController templateController)
	{
		Map componentGroups = new HashMap();
		
		Iterator componentIterator = components.iterator();
		while(componentIterator.hasNext())
		{
			ContentVO componentContentVO = (ContentVO)componentIterator.next();
			String groupName = templateController.getContentAttribute(componentContentVO.getId(), "GroupName", true);
			if(groupName == null || groupName.equals(""))
				groupName = "Other";
			
			List groupComponents = (List)componentGroups.get(groupName);
			if(groupComponents == null)
			{
				groupComponents = new ArrayList();
				componentGroups.put(groupName, groupComponents);
			}
			
			groupComponents.add(componentContentVO);
		}
		
		return componentGroups;
	}

	/**
	 * This method returns the contents that are of contentTypeDefinition "HTMLTemplate"
	 */
	
	public List getComponentContents() throws Exception
	{
		HashMap arguments = new HashMap();
		arguments.put("method", "selectListOnContentTypeName");
		
		List argumentList = new ArrayList();
		HashMap argument = new HashMap();
		argument.put("contentTypeDefinitionName", "HTMLTemplate");
		argumentList.add(argument);
		arguments.put("arguments", argumentList);
		
		return ContentController.getContentController().getContentVOList(arguments, getDatabase());
	}
		
	protected Document getComponentPropertiesDOM4JDocument(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
	{ 
		String cacheName 	= "componentEditorCache";
		String cacheKey		= "componentPropertiesDocument_" + siteNodeId + "_" + templateController.getLanguageId() + "_" + contentId;
		Document cachedComponentPropertiesDocument = (Document)CacheController.getCachedObjectFromAdvancedCache(cacheName, cacheKey);
		if(cachedComponentPropertiesDocument != null)
			return cachedComponentPropertiesDocument;
		
		Document componentPropertiesDocument = null;
   	
		try
		{
			String xml = this.getComponentPropertiesString(templateController, siteNodeId, languageId, contentId);
			//logger.info("xml: " + xml);
			if(xml != null && xml.length() > 0)
			{
				componentPropertiesDocument = domBuilder.getDocument(xml);
				
				CacheController.cacheObjectInAdvancedCache(cacheName, cacheKey, componentPropertiesDocument);
			}
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
		
		return componentPropertiesDocument;
	}

	/**
	 * This method fetches the template-string.
	 */
   
	private String getComponentPropertiesString(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
	{
		String cacheName 	= "componentEditorCache";
		String cacheKey		= "componentPropertiesString_" + siteNodeId + "_" + templateController.getLanguageId() + "_" + contentId;
		String cachedComponentPropertiesString = (String)CacheController.getCachedObjectFromAdvancedCache(cacheName, cacheKey);
		if(cachedComponentPropertiesString != null)
			return cachedComponentPropertiesString;
			
		String componentPropertiesString = null;
   	
		try
		{
		    Integer masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(templateController.getDatabase(), siteNodeId).getId();
		    //logger.info("masterLanguageId:" + masterLanguageId);
		    componentPropertiesString = templateController.getContentAttribute(contentId, masterLanguageId, "ComponentProperties", true);

			if(componentPropertiesString == null)
				throw new SystemException("There was no properties assigned to this content.");
		
			CacheController.cacheObjectInAdvancedCache(cacheName, cacheKey, componentPropertiesString);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}

		return componentPropertiesString;
	}
	
		
	protected Document getComponentTasksDOM4JDocument(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
	{ 	    
		String cacheName 	= "componentEditorCache";
		String cacheKey		= "componentTasksDocument_" + siteNodeId + "_" + templateController.getLanguageId() + "_" + contentId;
		Document cachedComponentTasksDocument = (Document)CacheController.getCachedObjectFromAdvancedCache(cacheName, cacheKey);
		if(cachedComponentTasksDocument != null)
			return cachedComponentTasksDocument;
		
		Document componentTasksDocument = null;
   	
		try
		{
			String xml = this.getComponentTasksString(templateController, siteNodeId, languageId, contentId);
			if(xml != null && xml.length() > 0)
			{
			    componentTasksDocument = domBuilder.getDocument(xml);
				
				CacheController.cacheObjectInAdvancedCache(cacheName, cacheKey, componentTasksDocument);
			}
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
		
		return componentTasksDocument;
	}

	/**
	 * This method fetches the tasks for a certain component.
	 */
   
	private String getComponentTasksString(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
	{
		String cacheName 	= "componentEditorCache";
		String cacheKey		= "componentTasksString_" + siteNodeId + "_" + templateController.getLanguageId() + "_" + contentId;
		String cachedComponentTasksString = (String)CacheController.getCachedObjectFromAdvancedCache(cacheName, cacheKey);
		if(cachedComponentTasksString != null)
			return cachedComponentTasksString;
			
		String componentTasksString = null;
   	
		try
		{
		    Integer masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(getDatabase(), siteNodeId).getId();
		    componentTasksString = templateController.getContentAttribute(contentId, masterLanguageId, "ComponentTasks", true);

			if(componentTasksString == null)
				throw new SystemException("There was no tasks assigned to this content.");
		
			CacheController.cacheObjectInAdvancedCache(cacheName, cacheKey, componentTasksString);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}

		return componentTasksString;
	}
	
	public Collection getComponentProperties(Integer componentId, TemplateController templateController, Integer siteNodeId, Integer languageId, Integer componentContentId) throws Exception
	{
		Document componentPropertiesDocument = getComponentPropertiesDOM4JDocument(templateController, siteNodeId, languageId, componentContentId);
		return getComponentProperties(componentId, componentPropertiesDocument, templateController);
	}
	
	/*
	 * This method returns a bean representing a list of ComponentProperties that the component has.
	 */
	 
	private List getComponentProperties(Integer componentId, Document document) throws Exception
	{
		//logger.info("componentPropertiesXML:" + componentPropertiesXML);
		List componentProperties = new ArrayList();
		Timer timer = new Timer();
		timer.setActive(false);

		try
		{
			if(document != null)
			{
				timer.printElapsedTime("Read document");

				String propertyXPath = "//property";
				//logger.info("propertyXPath:" + propertyXPath);
				List anl = document.selectNodes(propertyXPath);
				timer.printElapsedTime("Set property xpath");
				//logger.info("*********************************************************anl:" + anl.getLength());
				Iterator anlIterator = anl.iterator();
				while(anlIterator.hasNext())
				{
					Element binding = (Element)anlIterator.next();
					
					String name							 = binding.attributeValue("name");
					String displayName					 = binding.attributeValue("displayName");
					String description					 = binding.attributeValue("description");
					String defaultValue					 = binding.attributeValue("defaultValue");
					String allowLanguageVariations		 = binding.attributeValue("allowLanguageVariations");
					String dataProvider					 = binding.attributeValue("dataProvider");
					String type							 = binding.attributeValue("type");
					String allowedContentTypeNamesString = binding.attributeValue("allowedContentTypeDefinitionNames");
					String visualizingAction 			 = binding.attributeValue("visualizingAction");
					String createAction 				 = binding.attributeValue("createAction");
					//logger.info("name:" + name);
					//logger.info("type:" + type);

					ComponentProperty property = new ComponentProperty();
					property.setComponentId(componentId);
					property.setName(name);
					property.setDisplayName(displayName);
					property.setDescription(description);
					property.setDefaultValue(defaultValue);
					property.setAllowLanguageVariations(new Boolean(allowLanguageVariations));
					property.setDataProvider(dataProvider);
					property.setType(type);
					property.setVisualizingAction(visualizingAction);
					property.setCreateAction(createAction);
					if(allowedContentTypeNamesString != null && allowedContentTypeNamesString.length() > 0)
					{
					    String[] allowedContentTypeNamesArray = allowedContentTypeNamesString.split(",");
					    property.setAllowedContentTypeNamesArray(allowedContentTypeNamesArray);
					}
					
					if(type.equalsIgnoreCase(ComponentProperty.BINDING))
					{
						String entity 	= binding.attributeValue("entity");
						boolean isMultipleBinding 		= new Boolean(binding.attributeValue("multiple")).booleanValue();
						boolean isAssetBinding 	  		= new Boolean(binding.attributeValue("assetBinding")).booleanValue();
						String assetMask				= binding.attributeValue("assetMask");
						boolean isPuffContentForPage 	= new Boolean(binding.attributeValue("isPuffContentForPage")).booleanValue();

						property.setEntityClass(entity);
						String value = getComponentPropertyValue(componentId, name, property.getAllowLanguageVariations());
						timer.printElapsedTime("Set property1");

						property.setValue(value);
						property.setIsMultipleBinding(isMultipleBinding);
						property.setIsAssetBinding(isAssetBinding);
						property.setAssetMask(assetMask);
						property.setIsPuffContentForPage(isPuffContentForPage);
						List<ComponentBinding> bindings = getComponentPropertyBindings(componentId, name, this.getTemplateController());
						property.setBindings(bindings);
					}
					else if(type.equalsIgnoreCase(ComponentProperty.TEXTFIELD))	
					{		
						String value = getComponentPropertyValue(componentId, name, property.getAllowLanguageVariations());
						timer.printElapsedTime("Set property2");
						property.setValue(value);
					}
					else if(type.equalsIgnoreCase(ComponentProperty.DATEFIELD))	
					{		
						String value = getComponentPropertyValue(componentId, name, property.getAllowLanguageVariations());
						timer.printElapsedTime("Set property2");
						property.setValue(value);
					}
					else if(type.equalsIgnoreCase(ComponentProperty.CUSTOMFIELD))	
					{		
						String value = getComponentPropertyValue(componentId, name, property.getAllowLanguageVariations());
						String customMarkup = binding.attributeValue("customMarkup");
						String processedMarkup =  customMarkup.replaceAll("propertyName", name);
						processedMarkup = processedMarkup.replaceAll("propertyValue", value);

						property.setCustomMarkup(processedMarkup);
						property.setValue(value);
					}
					else if(type.equalsIgnoreCase(ComponentProperty.TEXTAREA))	
					{		
						boolean WYSIWYGEnabled = new Boolean(binding.attributeValue("WYSIWYGEnabled")).booleanValue();
						property.setWYSIWYGEnabled(WYSIWYGEnabled);
						String WYSIWYGToolbar = binding.attributeValue("WYSIWYGToolbar");
						property.setWYSIWYGToolbar(WYSIWYGToolbar);

						String value = getComponentPropertyValue(componentId, name, property.getAllowLanguageVariations());
						timer.printElapsedTime("Set property2");
						property.setValue(value);
					}
					else if(type.equalsIgnoreCase(ComponentProperty.SELECTFIELD))	
					{		
						String value = getComponentPropertyValue(componentId, name, property.getAllowLanguageVariations());
						timer.printElapsedTime("Set property2");
						
						List optionList = binding.elements("option");
						Iterator optionListIterator = optionList.iterator();
						while(optionListIterator.hasNext())
						{
							Element option = (Element)optionListIterator.next();
							String optionName	= option.attributeValue("name");
							String optionValue	= option.attributeValue("value");
							ComponentPropertyOption cpo = new ComponentPropertyOption();
							cpo.setName(optionName);
							cpo.setValue(optionValue);
							property.getOptions().add(cpo);
						}
						
						//logger.info("value:" + value);
						property.setValue(value);
					}
					else if(type.equalsIgnoreCase(ComponentProperty.CHECKBOXFIELD))	
					{		
						String value = getComponentPropertyValue(componentId, name, property.getAllowLanguageVariations());
						timer.printElapsedTime("Set property3");
						
						List optionList = binding.elements("option");
						Iterator optionListIterator = optionList.iterator();
						while(optionListIterator.hasNext())
						{
							Element option = (Element)optionListIterator.next();
							String optionName	= option.attributeValue("name");
							String optionValue	= option.attributeValue("value");
							ComponentPropertyOption cpo = new ComponentPropertyOption();
							cpo.setName(optionName);
							cpo.setValue(optionValue);
							property.getOptions().add(cpo);
						}
						
						//logger.info("value:" + value);
						property.setValue(value);
					}
					
					componentProperties.add(property);
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("The component with id " + componentId + " had a incorrect xml defining it's properties:" + e.getMessage(), e);
		}
							
		return componentProperties;
	}


	/*
	 * This method returns a bean representing a list of ComponentProperties that the component has.
	 */
	 
	private List getComponentProperties(Integer componentId, Document document, TemplateController templateController) throws Exception
	{
		//TODO - h�r kan vi s�kert cache:a.
		
		//logger.info("componentPropertiesXML:" + componentPropertiesXML);
		List componentProperties = new ArrayList();
		Timer timer = new Timer();
		timer.setActive(false);

		try
		{
			if(document != null)
			{
			//if(componentPropertiesXML != null && componentPropertiesXML.length() > 0)
			//{
				//org.w3c.dom.Document document = XMLHelper.readDocumentFromByteArray(componentPropertiesXML.getBytes("UTF-8"));

				timer.printElapsedTime("Read document");

				String propertyXPath = "//property";
				//logger.info("propertyXPath:" + propertyXPath);
				List anl = document.selectNodes(propertyXPath);
				timer.printElapsedTime("Set property xpath");
				//logger.info("*********************************************************anl:" + anl.getLength());
				Iterator anlIterator = anl.iterator();
				while(anlIterator.hasNext())
				{
					Element binding = (Element)anlIterator.next();
					
					String name							 = binding.attributeValue("name");
					String displayName					 = binding.attributeValue("displayName");
					String description					 = binding.attributeValue("description");
					String defaultValue					 = binding.attributeValue("defaultValue");
					String allowLanguageVariations		 = binding.attributeValue("allowLanguageVariations");
					String dataProvider					 = binding.attributeValue("dataProvider");
					String type							 = binding.attributeValue("type");
					String allowedContentTypeNamesString = binding.attributeValue("allowedContentTypeDefinitionNames");
					String visualizingAction 			 = binding.attributeValue("visualizingAction");
					String createAction 				 = binding.attributeValue("createAction");
					//logger.info("name:" + name);
					//logger.info("type:" + type);

					ComponentProperty property = new ComponentProperty();
					property.setComponentId(componentId);
					property.setName(name);
					property.setDisplayName(displayName);
					property.setDescription(description);
					property.setDefaultValue(defaultValue);
					property.setAllowLanguageVariations(new Boolean(allowLanguageVariations));
					property.setDataProvider(dataProvider);
					property.setType(type);
					property.setVisualizingAction(visualizingAction);
					property.setCreateAction(createAction);
					if(allowedContentTypeNamesString != null && allowedContentTypeNamesString.length() > 0)
					{
					    String[] allowedContentTypeNamesArray = allowedContentTypeNamesString.split(",");
					    property.setAllowedContentTypeNamesArray(allowedContentTypeNamesArray);
					}
					
					if(type.equalsIgnoreCase(ComponentProperty.BINDING))
					{
						String entity 					= binding.attributeValue("entity");
						boolean isMultipleBinding 		= new Boolean(binding.attributeValue("multiple")).booleanValue();
						boolean isAssetBinding 			= new Boolean(binding.attributeValue("assetBinding")).booleanValue();
						String assetMask				= binding.attributeValue("assetMask");
						boolean isPuffContentForPage 	= new Boolean(binding.attributeValue("isPuffContentForPage")).booleanValue();

						property.setEntityClass(entity);
						String value = getComponentPropertyValue(componentId, name, templateController, property.getAllowLanguageVariations());
						timer.printElapsedTime("Set property1");

						property.setValue(value);
						property.setIsMultipleBinding(isMultipleBinding);
						property.setIsAssetBinding(isAssetBinding);
						property.setAssetMask(assetMask);
						property.setIsPuffContentForPage(isPuffContentForPage);
						List<ComponentBinding> bindings = getComponentPropertyBindings(componentId, name, templateController);
						property.setBindings(bindings);
					}
					else if(type.equalsIgnoreCase(ComponentProperty.TEXTFIELD))	
					{		
						String value = getComponentPropertyValue(componentId, name, templateController, property.getAllowLanguageVariations());
						timer.printElapsedTime("Set property2");
						//logger.info("value:" + value);
						property.setValue(value);
					}
					else if(type.equalsIgnoreCase(ComponentProperty.DATEFIELD))	
					{		
						String value = getComponentPropertyValue(componentId, name, property.getAllowLanguageVariations());
						timer.printElapsedTime("Set property2");
						property.setValue(value);
					}
					else if(type.equalsIgnoreCase(ComponentProperty.CUSTOMFIELD))	
					{		
						String value = getComponentPropertyValue(componentId, name, property.getAllowLanguageVariations());
						String customMarkup = binding.attributeValue("customMarkup");
						String processedMarkup =  customMarkup.replaceAll("propertyName", name);
						processedMarkup = processedMarkup.replaceAll("propertyValue", value);

						property.setCustomMarkup(processedMarkup);
						property.setValue(value);
					}
					else if(type.equalsIgnoreCase(ComponentProperty.TEXTAREA))	
					{		
						boolean WYSIWYGEnabled = new Boolean(binding.attributeValue("WYSIWYGEnabled")).booleanValue();
						property.setWYSIWYGEnabled(WYSIWYGEnabled);
						String WYSIWYGToolbar = binding.attributeValue("WYSIWYGToolbar");
						property.setWYSIWYGToolbar(WYSIWYGToolbar);

						String value = getComponentPropertyValue(componentId, name, templateController, property.getAllowLanguageVariations());
						timer.printElapsedTime("Set property2");
						//logger.info("value:" + value);
						property.setValue(value);
					}
					else if(type.equalsIgnoreCase(ComponentProperty.SELECTFIELD))	
					{		
						String value = getComponentPropertyValue(componentId, name, templateController, property.getAllowLanguageVariations());
						timer.printElapsedTime("Set property2");
						
						List optionList = binding.elements("option");
						Iterator optionListIterator = optionList.iterator();
						while(optionListIterator.hasNext())
						{
							Element option = (Element)optionListIterator.next();
							String optionName	= option.attributeValue("name");
							String optionValue	= option.attributeValue("value");
							ComponentPropertyOption cpo = new ComponentPropertyOption();
							cpo.setName(optionName);
							cpo.setValue(optionValue);
							property.getOptions().add(cpo);
						}
						
						//logger.info("value:" + value);
						property.setValue(value);
					}
					
					componentProperties.add(property);
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("The component with id " + componentId + " had a incorrect xml defining it's properties:" + e.getMessage(), e);
			e.printStackTrace();
		}
							
		return componentProperties;
	}

	/*
	 * This method returns a bean representing a list of ComponentProperties that the component has.
	 */
	 
	private List getComponentTasks(Integer componentId, Document document) throws Exception
	{
		List componentTasks = new ArrayList();
		Timer timer = new Timer();
		timer.setActive(false);

		try
		{
			if(document != null)
			{
				timer.printElapsedTime("Read document");

				String propertyXPath = "//task";
				//logger.info("propertyXPath:" + propertyXPath);
				List anl = document.selectNodes(propertyXPath);
				timer.printElapsedTime("Set property xpath");
				Iterator anlIterator = anl.iterator();
				while(anlIterator.hasNext())
				{
					Element binding = (Element)anlIterator.next();
					
					String name			= binding.attributeValue("name");
					String view			= binding.attributeValue("view");
					String openInPopup 	= binding.attributeValue("openInPopup");
					String icon			= binding.attributeValue("icon");
					if(openInPopup == null || (!openInPopup.equals("true") && !openInPopup.equals("false")))
						openInPopup = "true";
					
					if(logger.isInfoEnabled())
					{
						logger.info("name:" + name);
						logger.info("view:" + view);
						logger.info("openInPopup:" + openInPopup);
					}
					
					ComponentTask task = new ComponentTask();
					task.setName(name);
					task.setView(view);
					task.setIcon(icon);
					task.setOpenInPopup(new Boolean(openInPopup));
					task.setComponentId(componentId);
					
					componentTasks.add(task);
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("The component with id " + componentId + " had a incorrect xml defining it's properties:" + e.getMessage(), e);
		}
							
		return componentTasks;
	}


	/**
	 * This method returns a value for a property if it's set. The value is collected in the
	 * properties for the page.
	 */
	
	private String getComponentPropertyValue(Integer componentId, String name, boolean allowLanguageVariations) throws Exception
	{
		String value = "Undefined";
		
		Timer timer = new Timer();
		timer.setActive(false);
				
		Integer siteNodeId = null;
		Integer languageId = null;
		
		if(this.getRequest().getParameter("siteNodeId") != null && this.getRequest().getParameter("siteNodeId").length() > 0)
			siteNodeId = new Integer(this.getRequest().getParameter("siteNodeId"));
		else
		{
			siteNodeId = this.getTemplateController().getDeliveryContext().getSiteNodeId();
		}
		
		NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, null);

		if(this.getRequest().getParameter("languageId") != null && this.getRequest().getParameter("languageId").length() > 0)
		{
			languageId = new Integer(this.getRequest().getParameter("languageId"));
			if(!languageId.equals(this.getTemplateController().getDeliveryContext().getLanguageId()))
			{
				languageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNodeWithValityCheck(getDatabase(), nodeDeliveryController, siteNodeId).getId();				
			}
		}
		else
		{
			languageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNodeWithValityCheck(getDatabase(), nodeDeliveryController, siteNodeId).getId();
		}
		
		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(getDatabase(), languageId);
		
		Integer contentId  = new Integer(-1);
		if(this.getRequest().getParameter("contentId") != null && this.getRequest().getParameter("contentId").length() > 0)
			contentId  = new Integer(this.getRequest().getParameter("contentId"));
		
		Document document = getPageComponentsDOM4JDocument(getDatabase(), this.getTemplateController(), siteNodeId, languageId, contentId);
		
		String componentXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + name + "']";
		//logger.info("componentXPath:" + componentXPath);
		List anl = document.selectNodes(componentXPath);
		Iterator anlIterator = anl.iterator();
		while(anlIterator.hasNext())
		{
			Element property = (Element)anlIterator.next();
			
			String id 			= property.attributeValue("type");
			String path 		= property.attributeValue("path");
			
			if(property.attribute("path_" + locale.getLanguage()) != null)
				path = property.attributeValue("path_" + locale.getLanguage());
			else if(!allowLanguageVariations)
			{
				Iterator attributesIterator = property.attributeIterator();
				while(attributesIterator.hasNext())
				{
					DefaultAttribute attribute = (DefaultAttribute)attributesIterator.next();
					if(attribute.getName().startsWith("path_"))
					{
						path = attribute.getValue();
					}
				}
			}

			value 				= path;
		}

		
		return value;
	}

	/**
	 * This method returns a value for a property if it's set. The value is collected in the
	 * properties for the page.
	 */
	
	private List<ComponentBinding> getComponentPropertyBindings(Integer componentId, String name, TemplateController templateController) throws Exception
	{
		List<ComponentBinding> componentBindings = new ArrayList<ComponentBinding>();
		
		Timer timer = new Timer();
		timer.setActive(false);
				
		Integer siteNodeId = null;
		Integer languageId = null;
		
		if(this.getRequest() != null && this.getRequest().getParameter("siteNodeId") != null && this.getRequest().getParameter("siteNodeId").length() > 0)
			siteNodeId = new Integer(this.getRequest().getParameter("siteNodeId"));
		else
		{
			siteNodeId = templateController.getDeliveryContext().getSiteNodeId();
		}
		
		if(this.getRequest() != null && this.getRequest().getParameter("languageId") != null && this.getRequest().getParameter("languageId").length() > 0)
		{
			languageId = new Integer(this.getRequest().getParameter("languageId"));
			if(!languageId.equals(templateController.getDeliveryContext().getLanguageId()))
			{
				languageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(templateController.getDatabase(), siteNodeId).getId();				
			}
		}
		else
		    languageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(templateController.getDatabase(), siteNodeId).getId();
		        
		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);
		
		Integer contentId  = new Integer(-1);
		if(this.getRequest() != null && this.getRequest().getParameter("contentId") != null && this.getRequest().getParameter("contentId").length() > 0)
			contentId  = new Integer(this.getRequest().getParameter("contentId"));

		Document document = getPageComponentsDOM4JDocument(templateController.getDatabase(), templateController, siteNodeId, languageId, contentId);
		
		String componentXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + name + "']/binding";
		//logger.info("componentXPath:" + componentXPath);
		List anl = document.selectNodes(componentXPath);
		Iterator anlIterator = anl.iterator();
		while(anlIterator.hasNext())
		{
			Element property = (Element)anlIterator.next();
			
			String entity   = property.attributeValue("entity");
			String entityId = property.attributeValue("entityId");
			String assetKey = property.attributeValue("assetKey");
			
			ComponentBinding componentBinding = new ComponentBinding();
			componentBinding.setEntityClass(entity);
			componentBinding.setEntityId(new Integer(entityId));
			componentBinding.setAssetKey(assetKey);

			componentBindings.add(componentBinding);
		}
		
		return componentBindings;
	}

	/**
	 * This method returns a value for a property if it's set. The value is collected in the
	 * properties for the page.
	 */
	
	private String getComponentPropertyValue(Integer componentId, String name, TemplateController templateController, boolean allowLanguageVariations) throws Exception
	{
		String value = "Undefined";
		
		Timer timer = new Timer();
		timer.setActive(false);
				
		Integer languageId = null;
		if(this.getRequest() != null && this.getRequest().getParameter("languageId") != null && this.getRequest().getParameter("languageId").length() > 0)
		    languageId = new Integer(this.getRequest().getParameter("languageId"));
		else
		    languageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(templateController.getDatabase(), templateController.getSiteNodeId()).getId();
		        
		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);
		
		Integer contentId  = new Integer(-1);
		if(this.getRequest() != null && this.getRequest().getParameter("contentId") != null && this.getRequest().getParameter("contentId").length() > 0)
			contentId  = new Integer(this.getRequest().getParameter("contentId"));

		NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(templateController.getSiteNodeId(), languageId, contentId);

		Document document = getPageComponentsDOM4JDocument(templateController.getDatabase(), templateController, templateController.getSiteNodeId(), languageId, contentId);
		
		String componentXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + name + "']";
		//logger.info("componentXPath:" + componentXPath);
		List anl = document.selectNodes(componentXPath);
		Iterator anlIterator = anl.iterator();
		while(anlIterator.hasNext())
		{
			Element property = (Element)anlIterator.next();
			
			String id 			= property.attributeValue("type");
			String path 		= property.attributeValue("path");
			
			if(property.attribute("path_" + locale.getLanguage()) != null)
				path = property.attributeValue("path_" + locale.getLanguage());
			else if(!allowLanguageVariations)
			{
				Iterator attributesIterator = property.attributeIterator();
				while(attributesIterator.hasNext())
				{
					DefaultAttribute attribute = (DefaultAttribute)attributesIterator.next();
					if(attribute.getName().startsWith("path_"))
					{
						path = attribute.getValue();
					}
				}
			}

			value 				= path;
		}

		return value;
	}


	/*
	 * This method returns a bean representing a list of bindings that the component has.
	 */
	 
	private List getContentBindnings(Integer componentId) throws Exception
	{
		List contentBindings = new ArrayList();
		
		Integer siteNodeId = new Integer(this.getRequest().getParameter("siteNodeId"));
		Integer languageId = new Integer(this.getRequest().getParameter("languageId"));
		Integer contentId  = new Integer(this.getRequest().getParameter("contentId"));

		String componentXML = getPageComponentsString(getDatabase(), this.getTemplateController(), siteNodeId, languageId, contentId);			
		////logger.info("componentXML:" + componentXML);

		Document document = domBuilder.getDocument(componentXML);
		String componentXPath = "//component[@id=" + componentId + "]/bindings/binding";
		//logger.info("componentXPath:" + componentXPath);
		List anl = document.selectNodes(componentXPath);
		Iterator anlIterator = anl.iterator();
		while(anlIterator.hasNext())
		{
			Element binding = (Element)anlIterator.next();
			//logger.info(XMLHelper.serializeDom(binding, new StringBuffer()));
			//logger.info("YES - we read the binding properties...");		
			
			String id 			= binding.attributeValue("id");
			String entityClass 	= binding.attributeValue("entity");
			String entityId 	= binding.attributeValue("entityId");
			String assetKey 	= binding.attributeValue("assetKey");
			//logger.info("id:" + id);
			//logger.info("entityClass:" + entityClass);
			//logger.info("entityId:" + entityId);
			
			if(entityClass.equalsIgnoreCase("Content"))
			{
				ContentVO contentVO = ContentDeliveryController.getContentDeliveryController().getContentVO(new Integer(entityId), getDatabase());
				ComponentBinding componentBinding = new ComponentBinding();
				componentBinding.setId(new Integer(id));
				componentBinding.setComponentId(componentId);
				componentBinding.setEntityClass(entityClass);
				componentBinding.setEntityId(new Integer(entityId));
				componentBinding.setAssetKey(assetKey);
				componentBinding.setBindingPath(contentVO.getName());
				
				contentBindings.add(componentBinding);
			}
		}
			
		return contentBindings;
	}

  	public String getLocalizedString(Locale locale, String key) 
  	{
    	StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);

    	return stringManager.getString(key);
  	}

}