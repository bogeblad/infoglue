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

package org.infoglue.cms.applications.structuretool.actions;


import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.ComponentPropertyDefinition;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ComponentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ComponentPropertyDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.XMLHelper;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.applications.databeans.Slot;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.PageEditorHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;



public class ViewSiteNodePageComponentsAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewSiteNodePageComponentsAction.class.getName());

	private static final long serialVersionUID = 1L;

	public static final String CATEGORY_TREE = "showCategoryTree";
	public static final String CATEGORY_TREE_MULTIPLE = "showCategoryTreeForMultipleBinding";

	private Integer repositoryId = null;
	private Integer siteNodeId = null;
	private Integer languageId = null;
	private Integer contentId = null;
	private String assetKey = null;
	private Integer parentComponentId = null;
	private Integer componentId = null;
	private Integer newComponentContentId = null;
	private String propertyName = null;
	private String path 		= null;
	private String slotId		= null;
	private String specifyBaseTemplate = null;
	private String url			= null;
	private Integer direction 	= null;
	private Integer newPosition = null;
	private boolean showSimple 	= false;
	private Integer pageTemplateContentId;
	private String showDecorated = "true";
	private String slotPositionComponentId = null;
	private Integer pagePartContentId = null;
	private boolean hideComponentPropertiesOnLoad = false;
	
	LanguageVO masterLanguageVO = null;
	
	private List repositories 				 	= null;
	private String currentAction 		 	 	= null;
	private Integer filterRepositoryId 		 	= null; 
	private String sortProperty 			 	= "name";
	private String[] allowedContentTypeIds	 	= null;
	private String[] allowedComponentNames 	 	= null;
	private String[] disallowedComponentNames	= null;
	private String[] allowedComponentGroupNames = null;
	
	private Boolean stateChanged = false;	
	
	public ViewSiteNodePageComponentsAction()
	{
	}

	private void initialize() throws Exception
	{
		initialize(true);
	}
	
	private void initialize(boolean changeState) throws Exception
	{
		SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getACLatestActiveSiteNodeVersionVO(this.getInfoGluePrincipal(), this.siteNodeId);
		logger.info("siteNodeVersionVO:" + siteNodeVersionVO.getId() + ":" + siteNodeVersionVO.getIsActive());
		if(changeState && siteNodeVersionVO.getStateId().intValue() != SiteNodeVersionVO.WORKING_STATE.intValue())
		{
	    	List events = new ArrayList();
			SiteNodeStateController.getController().changeState(siteNodeVersionVO.getId(), SiteNodeVersionVO.WORKING_STATE, "Edit on sight editing", true, this.getInfoGluePrincipal(), this.siteNodeId, events);
			this.stateChanged = true;
		}
		
		Integer currentRepositoryId = SiteNodeController.getController().getSiteNodeVOWithId(this.siteNodeId).getRepositoryId();
		this.masterLanguageVO = LanguageController.getController().getMasterLanguage(currentRepositoryId);		
		SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(this.siteNodeId);
		
		if(filterRepositoryId == null)
		{
			Map args = new HashMap();
		    args.put("globalKey", "infoglue");
		    PropertySet ps = PropertySetManager.getInstance("jdbc", args);

		    String defaultTemplateRepository = ps.getString("repository_" + currentRepositoryId + "_defaultTemplateRepository");
		    if(defaultTemplateRepository != null && !defaultTemplateRepository.equals(""))
		        filterRepositoryId = new Integer(defaultTemplateRepository);
		    else
		        filterRepositoryId = currentRepositoryId;
		}
	}

	/**
	 * This method initializes the tree
	 */
	
	private void initializeTreeView(String currentAction) throws Exception
	{
		this.currentAction = currentAction;
		
		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);
		
		if(this.repositoryId == null)
			this.repositoryId = RepositoryController.getController().getFirstRepositoryVO().getRepositoryId();
	}

	    
	/**
	 * This method which is the default one only serves to show a list 
	 * of tasks to the user so he/she can select one to run. 
	 */
    
	public String doExecute() throws Exception
	{
		initialize(false);
		return "success";
	}


	/**
	 * This method shows the user a list of Components(HTML Templates). 
	 */
    
	public String doListComponents() throws Exception
	{
		logger.info("queryString:" + this.getRequest().getQueryString());
		initialize(false);

		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);

		return "listComponents";
	}

	/**
	 * This method shows the user a list of Components(HTML Templates). 
	 */
    
	public String doListComponentsForChange() throws Exception
	{
		logger.info("queryString:" + this.getRequest().getQueryString());
		initialize(false);

		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);

		return "listComponentsForChange";
	}

	/**
	 * This method shows the user a list of Components(HTML Templates). 
	 */
    
	public String doListComponentsForPalette() throws Exception
	{
		initialize(false);
		return "listComponentsForPalette";
	}
	
	/**
	 * This method shows the user a list of Contents. 
	 */
    
	public String doShowContentTree() throws Exception
	{
		initialize(false);
		initializeTreeView("ViewSiteNodePageComponents!showContentTree.action");

		getHttpSession().setAttribute("" + siteNodeId + "_hideComponentPropertiesOnLoad", new Boolean(hideComponentPropertiesOnLoad));
		
		return "showContentTree";
	}

	/**
	 * This method shows the user a list of Contents. 
	 */
    
	public String doShowContentTreeV3() throws Exception
	{
		initialize(false);
		initializeTreeView("ViewSiteNodePageComponents!showContentTreeV3.action");

		getHttpSession().setAttribute("" + siteNodeId + "_hideComponentPropertiesOnLoad", new Boolean(hideComponentPropertiesOnLoad));
		
		return "showContentTreeV3";
	}

	/**
	 * This method shows the user a interface to choose multiple contents. 
	 */
    
	public String doShowContentTreeForMultipleBinding() throws Exception
	{
		initialize(false);
		initializeTreeView("ViewSiteNodePageComponents!showContentTreeForMultipleBinding.action");
		
		getHttpSession().setAttribute("" + siteNodeId + "_hideComponentPropertiesOnLoad", new Boolean(hideComponentPropertiesOnLoad));

		return "showContentTreeForMultipleBinding";
	}

	/**
	 * This method shows the user a interface to choose multiple contents. 
	 */
    
	public String doShowContentTreeForMultipleBindingV3() throws Exception
	{
		initialize(false);
		initializeTreeView("ViewSiteNodePageComponents!showContentTreeForMultipleBindingV3.action");
		
		getHttpSession().setAttribute("" + siteNodeId + "_hideComponentPropertiesOnLoad", new Boolean(hideComponentPropertiesOnLoad));

		return "showContentTreeForMultipleBindingV3";
	}

	/**
	 * This method shows the user a interface to choose multiple contents. 
	 */
    
	public String doShowContentTreeForMultipleAssetBinding() throws Exception
	{
		initialize(false);
		initializeTreeView("ViewSiteNodePageComponents!showContentTreeForMultipleAssetBinding.action");
		
		getHttpSession().setAttribute("" + siteNodeId + "_hideComponentPropertiesOnLoad", new Boolean(hideComponentPropertiesOnLoad));

		return "showContentTreeForMultipleAssetBinding";
	}

	/**
	 * This method shows the user a list of SiteNodes. 
	 */
    
	public String doShowStructureTree() throws Exception
	{
		initialize(false);
		initializeTreeView("ViewSiteNodePageComponents!showStructureTree.action");

		getHttpSession().setAttribute("" + siteNodeId + "_hideComponentPropertiesOnLoad", new Boolean(hideComponentPropertiesOnLoad));
		
		return "showStructureTree";
	}
	
	/**
	 * This method shows the user a list of Contents. 
	 */
    
	public String doShowStructureTreeV3() throws Exception
	{
		initialize(false);
		initializeTreeView("ViewSiteNodePageComponents!showStructureTreeV3.action");

		getHttpSession().setAttribute("" + siteNodeId + "_hideComponentPropertiesOnLoad", new Boolean(hideComponentPropertiesOnLoad));
		
		return "showStructureTreeV3";
	}

	/**
	 * This method shows the user a interface to choose multiple sitenodes. 
	 */
    
	public String doShowStructureTreeForMultipleBinding() throws Exception
	{
		initialize(false);
		initializeTreeView("ViewSiteNodePageComponents!showStructureTreeForMultipleBinding.action");
	
		getHttpSession().setAttribute("" + siteNodeId + "_hideComponentPropertiesOnLoad", new Boolean(hideComponentPropertiesOnLoad));
		
		return "showStructureTreeForMultipleBinding";
	}

	/**
	 * This method shows the user a interface to choose multiple sitenodes. 
	 */
    
	public String doShowStructureTreeForMultipleBindingV3() throws Exception
	{
		initialize(false);
		initializeTreeView("ViewSiteNodePageComponents!showStructureTreeForMultipleBindingV3.action");
	
		getHttpSession().setAttribute("" + siteNodeId + "_hideComponentPropertiesOnLoad", new Boolean(hideComponentPropertiesOnLoad));
		
		return "showStructureTreeForMultipleBindingV3";
	}

	/**
	 * This method shows the user a list of Categories.
	 */
	public String doShowCategoryTree() throws Exception
	{
		initialize(false);
		initializeTreeView("ViewSiteNodePageComponents!showCategoryTree.action");
		return CATEGORY_TREE;
	}

	/**
	 * This method shows the user a list of Categories to chose multiple.
	 */
	public String doShowCategoryTreeForMultipleBinding() throws Exception
	{
		initialize(false);
		initializeTreeView("ViewSiteNodePageComponents!showCategoryTreeForMultipleBinding.action");
		return CATEGORY_TREE_MULTIPLE;
	}


	public List getRepositories()
	{
		return this.repositories;
	}

	public String getCurrentAction()
	{
		return this.currentAction;
	}

	public String getContentAttribute(Integer contentId, String attributeName) throws Exception
	{
	    String attribute = "Undefined";
	    
	    ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
		
		LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId).getRepositoryId());
		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), masterLanguageVO.getId());

		attribute = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, attributeName, false);
		
		return attribute;
	}	
	
	
	/**
	 * This method adds a page template to a sitenode. 
	 */
    
	public String doAddPageTemplate() throws Exception
	{
		logger.info("************************************************************");
		logger.info("* ADDING PAGE TEMPLATE                                     *");
		logger.info("************************************************************");
		logger.info("siteNodeId:" + this.siteNodeId);
		logger.info("languageId:" + this.languageId);
		logger.info("repositoryId:" + this.repositoryId);
		logger.info("contentId:" + this.contentId);
		logger.info("pageTemplateContentId:" + this.pageTemplateContentId);
		logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);

		initialize();

		Integer newComponentId = new Integer(0);

		NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
		
		if(this.pageTemplateContentId != null)
		{
		    Integer languageId = LanguageController.getController().getMasterLanguage(this.repositoryId).getId();
			ContentVersionVO pageTemplateContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(this.pageTemplateContentId, languageId);
			
		    String componentXML = ContentVersionController.getContentVersionController().getAttributeValue(pageTemplateContentVersionVO.getId(), "ComponentStructure", false);
		    
			Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
			
			String componentXPath = "//component";

			logger.info("componentXPath:" + componentXPath);
			
			NodeList componentNodes = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
			logger.info("Found componentNodes:" + componentNodes.getLength());
			for(int i=0; i < componentNodes.getLength(); i++)
			{
				Element element = (Element)componentNodes.item(i);
				String componentId = element.getAttribute("id");
				String componentContentId = element.getAttribute("contentId");
				logger.info("componentId:" + componentId);
				logger.info("componentContentId:" + componentContentId);
				
				ComponentController.getController().checkAndAutoCreateContents(this.siteNodeId, languageId, this.masterLanguageVO.getId(), this.assetKey, new Integer(componentId), document, new Integer(componentContentId), getInfoGluePrincipal());
				componentXML = XMLHelper.serializeDom(document, new StringBuffer()).toString();
			}
				
			ContentVO pageMetaInfoContentVO = nodeDeliveryController.getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
			//ContentVO templateContentVO = nodeDeliveryController.getBoundContent(siteNodeId, "Meta information");		
			
			//logger.info("templateContentVO:" + templateContentVO);
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(pageMetaInfoContentVO.getId(), this.masterLanguageVO.getId());
			if(contentVersionVO == null)
			{
				SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
	        	String versionValue = "<?xml version='1.0' encoding='UTF-8'?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes><Title><![CDATA[" + siteNodeVO.getName() + "]]></Title><NavigationTitle><![CDATA[" + siteNodeVO.getName() + "]]></NavigationTitle><NiceURIName><![CDATA[" + new VisualFormatter().replaceNiceURINonAsciiWithSpecifiedChars(siteNodeVO.getName(), CmsPropertyHandler.getNiceURIDefaultReplacementCharacter()) + "]]></NiceURIName><Description><![CDATA[" + siteNodeVO.getName() + "]]></Description><MetaInfo><![CDATA[" + siteNodeVO.getName() + "]]></MetaInfo><ComponentStructure><![CDATA[]]></ComponentStructure></attributes></article>";
	        	contentVersionVO = new ContentVersionVO();
	        	contentVersionVO.setVersionComment("Autogenerated version");
	        	contentVersionVO.setVersionModifier(getInfoGluePrincipal().getName());
	        	contentVersionVO.setVersionValue(versionValue);
				contentVersionVO = ContentVersionController.getContentVersionController().create(pageMetaInfoContentVO.getId(), this.masterLanguageVO.getId(), contentVersionVO, null);
			}
			
			ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", componentXML, new InfoGluePrincipal("ComponentEditor", "none", "none", "none", new ArrayList(), new ArrayList(), true, null));
		}
		
		this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&activatedComponentId=" + newComponentId + "&showSimple=" + this.showSimple + "&stateChanged=" + this.stateChanged;
		//this.getResponse().sendRedirect(url);		
		
		this.url = this.getResponse().encodeURL(url);
		this.getResponse().sendRedirect(url);
	    return NONE; 
	}

	/**
	 * This method adds a component to the page. 
	 */
    
	public String doAddComponent() throws Exception
	{
		logger.info("************************************************************");
		logger.info("* ADDING COMPONENT                                         *");
		logger.info("************************************************************");
		logger.info("siteNodeId:" + this.siteNodeId);
		logger.info("languageId:" + this.languageId);
		logger.info("contentId:" + this.contentId);
		logger.info("queryString:" + this.getRequest().getQueryString());
		logger.info("parentComponentId:" + this.parentComponentId);
		logger.info("componentId:" + this.componentId);
		logger.info("slotId:" + this.slotId);
		logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
		
		initialize();
		logger.info("masterLanguageId:" + this.masterLanguageVO.getId());

		Integer newComponentId = new Integer(0);
		
		NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
		
		if(this.specifyBaseTemplate.equalsIgnoreCase("true"))
		{
			String componentXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><components><component contentId=\"" + componentId + "\" id=\"" + newComponentId + "\" name=\"base\"><properties></properties><bindings></bindings><components></components></component></components>";
			ContentVO templateContentVO = nodeDeliveryController.getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
			
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(templateContentVO.getId(), this.masterLanguageVO.getId());
			if(contentVersionVO == null)
			{
				SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
	        	String versionValue = "<?xml version='1.0' encoding='UTF-8'?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes><Title><![CDATA[" + siteNodeVO.getName() + "]]></Title><NavigationTitle><![CDATA[" + siteNodeVO.getName() + "]]></NavigationTitle><NiceURIName><![CDATA[" + new VisualFormatter().replaceNiceURINonAsciiWithSpecifiedChars(siteNodeVO.getName(), CmsPropertyHandler.getNiceURIDefaultReplacementCharacter()) + "]]></NiceURIName><Description><![CDATA[" + siteNodeVO.getName() + "]]></Description><MetaInfo><![CDATA[" + siteNodeVO.getName() + "]]></MetaInfo><ComponentStructure><![CDATA[]]></ComponentStructure></attributes></article>";
	        	contentVersionVO = new ContentVersionVO();
	        	contentVersionVO.setVersionComment("Autogenerated version");
	        	contentVersionVO.setVersionModifier(getInfoGluePrincipal().getName());
	        	contentVersionVO.setVersionValue(versionValue);
				contentVersionVO = ContentVersionController.getContentVersionController().create(templateContentVO.getId(), languageId, contentVersionVO, null);
			}
			
			ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", componentXML, new InfoGluePrincipal("ComponentEditor", "none", "none", "none", new ArrayList(), new ArrayList(), true, null));
		}
		else
		{
		    String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId());			
		    
			Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
			String componentXPath = "//component[@id=" + this.parentComponentId + "]/components";

			NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
			if(anl.getLength() > 0)
			{
				Element component = (Element)anl.item(0);
				
				String componentsXPath = "//component";
				NodeList nodes = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentsXPath);
				for(int i=0; i < nodes.getLength(); i++)
				{
					Element element = (Element)nodes.item(i);
					if(new Integer(element.getAttribute("id")).intValue() > newComponentId.intValue())
						newComponentId = new Integer(element.getAttribute("id"));
				}
				newComponentId = new Integer(newComponentId.intValue() + 1);
				
				ContentVO templateContentVO = ContentController.getContentController().getContentVOWithId(this.componentId);
				ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(templateContentVO.getContentTypeDefinitionId());
				boolean isPagePartReference = false;
				if(contentTypeDefinitionVO.getName().equals("PagePartTemplate"))
					isPagePartReference = true;
				
				if(slotPositionComponentId != null && !slotPositionComponentId.equals(""))
				{
					NodeList childNodes = component.getChildNodes();
					for(int i=0; i< childNodes.getLength(); i++)
					{
						Node node = childNodes.item(i);
						if(node.getNodeType() == Node.ELEMENT_NODE)
						{
							Element element = (Element)node;
							if(element.getAttribute("id").equals(slotPositionComponentId))
							{
								logger.info("Inserting component before: " + element);
								Element newComponent = addComponentElementBefore(component, element, new Integer(newComponentId.intValue()), this.slotId, this.componentId, isPagePartReference);
								//component.insertBefore(component, element);
								break;
							}
						}
					}
				}
				else
				{
					Element newComponent = addComponentElement(component, new Integer(newComponentId.intValue()), this.slotId, this.componentId, isPagePartReference);
				}

				ComponentController.getController().checkAndAutoCreateContents(this.siteNodeId, languageId, this.masterLanguageVO.getId(), this.assetKey, newComponentId, document, templateContentVO.getId(), getInfoGluePrincipal());

				String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 

				ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, this.masterLanguageVO.getId(), contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, this.masterLanguageVO.getId(), true, "Meta information", DeliveryContext.getDeliveryContext());
				ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());
				
				logger.info("Updating ComponentStructure on " + contentVersionVO.getContentVersionId());
				ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
			}
		}
		
		logger.info("newComponentId:" + newComponentId);
		
		this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&focusElementId=" + newComponentId + "&activatedComponentId=" + newComponentId + "&componentContentId=" + this.componentId + "&showSimple=" + this.showSimple + "&stateChanged=" + this.stateChanged;
		//logger.info("this.url:" + this.url);
		//this.getResponse().sendRedirect(url);		
		
		this.url = this.getResponse().encodeURL(url);
		this.getResponse().sendRedirect(url);
	    return NONE; 
	}


	/**
	 * This method adds a component to the page. 
	 */
    
	public String doAddOrReplaceComponent() throws Exception
	{
		logger.info("************************************************************");
		logger.info("* ADDING OR REPLACING COMPONENT                            *");
		logger.info("************************************************************");
		logger.info("siteNodeId:" + this.siteNodeId);
		logger.info("languageId:" + this.languageId);
		logger.info("contentId:" + this.contentId);
		logger.info("queryString:" + this.getRequest().getQueryString());
		logger.info("parentComponentId:" + this.parentComponentId);
		//logger.info("componentId:" + this.componentId);
		logger.info("slotId:" + this.slotId);
		logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
		logger.info("pagePartContentId:" + this.pagePartContentId);

		try
		{
			initialize();
	
			logger.info("masterLanguageId:" + this.masterLanguageVO.getId());
	
			Integer newComponentId = new Integer(0);
	
		    String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId());			
	
			Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
			String componentXPath = "//component[@id=" + this.parentComponentId + "]";
	
			Node componentNode = org.apache.xpath.XPathAPI.selectSingleNode(document.getDocumentElement(), componentXPath);
			if(componentNode != null)
			{
				//Element componentElement = (Element)componentNode;
				
				String componentsXPath = "//component";
				NodeList nodes = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentsXPath);
				for(int i=0; i < nodes.getLength(); i++)
				{
					Element element = (Element)nodes.item(i);
					if(new Integer(element.getAttribute("id")).intValue() > newComponentId.intValue())
						newComponentId = new Integer(element.getAttribute("id"));
				}
				newComponentId = new Integer(newComponentId.intValue() + 1);
				
				NodeList childNodes = componentNode.getChildNodes();
				logger.info("childNodes:" + childNodes.getLength());
				
				Node child = componentNode.getFirstChild();
				while (child != null)
				{
					logger.info("Removing:" + child);
		        	componentNode.removeChild(child);
		        	child = componentNode.getFirstChild();
				}
	
				logger.info("childNodes:" + childNodes.getLength());
				//StringBuffer sb = new StringBuffer();
				//XMLHelper.serializeDom(componentNode, sb);
				//logger.info("SB:" + sb);
				
				if(this.pagePartContentId != null)
				{
					ContentVersionVO pagePartContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(this.pagePartContentId, this.masterLanguageVO.getId());
					String componentStructure = ContentVersionController.getContentVersionController().getAttributeValue(pagePartContentVersionVO.getId(), "ComponentStructure", false);
					
					componentStructure = componentStructure.replaceAll(" isInherited=\"true\"", "");
					componentStructure = componentStructure.replaceAll(" pagePartTemplateContentId=\"-1\"", "");
					componentStructure = componentStructure.replaceAll("<property name=\"pagePartContentId\" path=\".*?\"></property>", "");
					componentStructure = componentStructure.replaceAll("<property name=\"pagePartContentId\" path=\".*?\"/>", "");
					componentStructure = componentStructure.replaceAll("<properties>", "<properties><property name=\"pagePartContentId\" path=\"" + pagePartContentId + "\"/>");
					logger.info("componentStructure:" + componentStructure);
									
					Document componentStructureDocument = XMLHelper.readDocumentFromByteArray(componentStructure.getBytes("UTF-8"));
					Node rootNode = componentStructureDocument.getDocumentElement();
					
					componentNode.appendChild(document.importNode(rootNode, true));
	
					String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
	
					ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, this.masterLanguageVO.getId(), contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, this.masterLanguageVO.getId(), true, "Meta information", DeliveryContext.getDeliveryContext());
					ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());
					
					ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
				}						
			}
			
			logger.info("newComponentId:" + newComponentId);
			
			this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&focusElementId=" + newComponentId + "&activatedComponentId=" + newComponentId + "&componentContentId=" + this.componentId + "&showSimple=" + this.showSimple + "&stateChanged=" + stateChanged;
			//this.getResponse().sendRedirect(url);		

			this.url = this.getResponse().encodeURL(url);
			this.getResponse().sendRedirect(url);
		    return NONE; 
		}
		catch (Exception e) 
		{
			logger.error("Error adding/changing component:" + e.getMessage(), e);
			return ERROR;
		}
	}

	/**
	 * This method adds a component to the page. 
	 */
    
	public String doMoveComponentToSlot() throws Exception
	{
		logger.info("************************************************************");
		logger.info("* MOVING COMPONENT TO ANOTHER SLOT                         *");
		logger.info("************************************************************");
		logger.info("siteNodeId:" + this.siteNodeId);
		logger.info("languageId:" + this.languageId);
		logger.info("contentId:" + this.contentId);
		logger.info("queryString:" + this.getRequest().getQueryString());
		logger.info("parentComponentId:" + this.parentComponentId);
		logger.info("componentId:" + this.componentId);
		logger.info("slotId:" + this.slotId);
		logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);

		initialize();

		logger.info("masterLanguageId:" + this.masterLanguageVO.getId());

		ContentVO componentContentVO = null;

		if(this.specifyBaseTemplate.equalsIgnoreCase("true"))
		{
			throw new SystemException("Not possible to move component to base slot");
		}
		else
		{
			String componentXML = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId());			
	
			Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
			
			String componentXPath = "//component[@id=" + this.componentId + "]";
			String parentComponentXPath = "//component[@id=" + this.parentComponentId + "]/components";

			logger.info("componentXPath:" + componentXPath);
			logger.info("parentComponentXPath:" + parentComponentXPath);
			
			Node componentNode = org.apache.xpath.XPathAPI.selectSingleNode(document.getDocumentElement(), componentXPath);
			logger.info("Found componentNode:" + componentNode);
			
			Node parentComponentComponentsNode = org.apache.xpath.XPathAPI.selectSingleNode(document.getDocumentElement(), parentComponentXPath);
			logger.info("Found parentComponentComponentsNode:" + parentComponentComponentsNode);

			if(componentNode != null && parentComponentComponentsNode != null)
			{
				Element component = (Element)componentNode;
				Element currentParentElement = (Element)componentNode.getParentNode();
				Element parentComponentComponentsElement = (Element)parentComponentComponentsNode;
				Element parentComponentElement = (Element)parentComponentComponentsNode.getParentNode();
			
				Integer componentContentId = new Integer(component.getAttribute("contentId"));
				Integer parentComponentContentId = new Integer(parentComponentElement.getAttribute("contentId"));
				logger.info("componentContentId:" + componentContentId);
				logger.info("parentComponentContentId:" + parentComponentContentId);
				componentContentVO = ContentController.getContentController().getContentVOWithId(componentContentId);
				
				PageEditorHelper peh = new PageEditorHelper();
				List<Slot> slots = peh.getSlots(parentComponentContentId, languageId, this.getInfoGluePrincipal());
				boolean allowed = true;
				Iterator<Slot> slotsIterator = slots.iterator();
				while(slotsIterator.hasNext())
				{
					Slot slot = slotsIterator.next();
					logger.info(slot.getId() + "=" + slotId);
					if(slot.getId().equals(slotId))
					{
						String[] allowedComponentNames = slot.getAllowedComponentsArray();
						String[] disallowedComponentNames = slot.getDisallowedComponentsArray();
						if(allowedComponentNames != null && allowedComponentNames.length > 0)
						{
							allowed = false;
							for(int i = 0; i < allowedComponentNames.length; i++)
							{
								if(allowedComponentNames[i].equalsIgnoreCase(componentContentVO.getName()))
									allowed = true;
							}
						}
						if(disallowedComponentNames != null && disallowedComponentNames.length > 0)
						{
							for(int i = 0; i < disallowedComponentNames.length; i++)
							{
								if(disallowedComponentNames[i].equalsIgnoreCase(componentContentVO.getName()))
									allowed = false;
							}
						}
					}
					break;
				}
				
				logger.info("Should the component:" + componentContentVO + " be allowed to be put in " + slotId + ":" + allowed);
				logger.info("currentParentElement:" + currentParentElement.getNodeName() + ":" + currentParentElement.hashCode());
				logger.info("parentComponentComponentsElement:" + parentComponentComponentsElement.getNodeName() + ":" + parentComponentComponentsElement.hashCode());
				
				logger.info("slotPositionComponentId:" + slotPositionComponentId);
				if((component.getParentNode() == parentComponentComponentsElement && slotId.equalsIgnoreCase(component.getAttribute("name"))))
				{
					logger.info("Yes...");

					component.getParentNode().removeChild(component);
					component.setAttribute("name", slotId);
					
					logger.info("slotPositionComponentId:" + slotPositionComponentId);

					if(slotPositionComponentId != null && !slotPositionComponentId.equals(""))
					{
						logger.info("Moving component to slot: " + slotPositionComponentId);

						Element afterElement = null;
						
						NodeList childNodes = parentComponentComponentsElement.getChildNodes();
						for(int i=0; i< childNodes.getLength(); i++)
						{
							Node node = childNodes.item(i);
							if(node.getNodeType() == Node.ELEMENT_NODE)
							{
								Element element = (Element)node;
								if(element.getAttribute("id").equals(slotPositionComponentId))
								{
									afterElement = element;
									break;
								}
							}
						}
						
						if(afterElement != null)
						{
							logger.info("Inserting component before: " + afterElement);
							parentComponentComponentsElement.insertBefore(component, afterElement);
						}
						else
						{
							parentComponentComponentsElement.appendChild(component);													
						}
					}
					else
					{
						logger.info("Appending component...");
						parentComponentComponentsElement.appendChild(component);						
					}
					
					String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
	
					ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, this.masterLanguageVO.getId(), contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, this.masterLanguageVO.getId(), true, "Meta information", DeliveryContext.getDeliveryContext());
					ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());
					
					ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());

					this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&focusElementId=" + componentId + "&componentContentId=" + componentContentVO.getId() + "&showSimple=" + this.showSimple + "&stateChanged=" + stateChanged;
				}
				else if(allowed && (component.getParentNode() != parentComponentComponentsElement || !slotId.equalsIgnoreCase(component.getAttribute("name"))))
				{
					logger.info("Moving component...");

					component.getParentNode().removeChild(component);
					component.setAttribute("name", slotId);

					if(slotPositionComponentId != null && !slotPositionComponentId.equals(""))
					{
						NodeList childNodes = parentComponentComponentsElement.getChildNodes();
						for(int i=0; i< childNodes.getLength(); i++)
						{
							Node node = childNodes.item(i);
							if(node.getNodeType() == Node.ELEMENT_NODE)
							{
								Element element = (Element)node;
								if(element.getAttribute("id").equals(slotPositionComponentId))
								{
									logger.info("Inserting component before: " + element);
									parentComponentComponentsElement.insertBefore(component, element);
									break;
								}
							}
						}
					}
					else
					{
						parentComponentComponentsElement.appendChild(component);						
					}

					String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
	
					ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, this.masterLanguageVO.getId(), contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, this.masterLanguageVO.getId(), true, "Meta information", DeliveryContext.getDeliveryContext());
					ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());
					
					ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());

					this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&focusElementId=" + componentId + "&componentContentId=" + componentContentVO.getId() + "&showSimple=" + this.showSimple + "&stateChanged=" + stateChanged;
				}
				else
				{
					this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&showSimple=" + this.showSimple + "&stateChanged=" + stateChanged;
				}
			}
		}
		
		//this.getResponse().sendRedirect(url);		
		
		this.url = this.getResponse().encodeURL(url);
		this.getResponse().sendRedirect(url);
	    return NONE; 
	}

	/**
	 * This method moves the component up a step if possible within the same slot. 
	 */
    
	public String doMoveComponent() throws Exception
	{
		initialize();
			
		String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId());			
		//logger.info("componentXML:" + componentXML);
		
		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
		String componentXPath = "//component[@id=" + this.componentId + "]";
		logger.info("componentXPath:" + componentXPath);
		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
		if(anl.getLength() > 0)
		{
			Element component = (Element)anl.item(0);
			String name = component.getAttribute("name");
			logger.info("id: " + component.getAttribute("id") + " - name:" + name);
			//logger.info(XMLHelper.serializeDom(component, new StringBuffer()));
			Node parentNode = component.getParentNode();
			
			boolean hasChanged = false;
			if(this.newPosition != null)
			{
				Node previousNode = null;
				List<Node> siblings = new ArrayList<Node>();
				NodeList nl = parentNode.getChildNodes();
				for(int i=0; i<nl.getLength(); i++)
				{
					Element childElement = (Element)nl.item(i);
					logger.info("Child:" + childElement.getAttribute("id") + ":" + childElement.getAttribute("name"));
					if(childElement.getAttribute("name").equalsIgnoreCase(name) && childElement != component)
						siblings.add(childElement);
				}
				
				logger.info("this.newPosition:" + this.newPosition);
				logger.info("siblings:" + siblings.size());
				if(siblings.size() > this.newPosition)
				{
					previousNode = siblings.get(this.newPosition);
					if(previousNode != component)
					{
						logger.info("parentNode:" + ((Element)parentNode).getAttribute("id") + ":" + ((Element)parentNode).getAttribute("name"));	
						if(previousNode != null)
						{
							logger.info("previousNode:" + ((Element)previousNode).getAttribute("id") + ":" + ((Element)previousNode).getAttribute("name"));	
							parentNode.removeChild(component);
						    parentNode.insertBefore(component, previousNode);
						    hasChanged = true;
						}
					}
					else
						logger.info("Cannot move to same place...");
				}
				else if(siblings.size() == this.newPosition)
				{
					if(previousNode != component)
					{
						logger.info("parentNode:" + ((Element)parentNode).getAttribute("id") + ":" + ((Element)parentNode).getAttribute("name"));	
						parentNode.removeChild(component);
					    parentNode.appendChild(component);
					    hasChanged = true;
					}
					else
						logger.info("Cannot move to same place...");
				}
				
			}
			else if(this.direction.intValue() == 0) //Up
			{
			    Node previousNode = component.getPreviousSibling();
		        
			    while(previousNode != null && previousNode.getNodeType() != Node.ELEMENT_NODE)
		        {
				    previousNode = previousNode.getPreviousSibling();
		        	//break;
		        }
			    
			    Element element = ((Element)previousNode);
				while(element != null && !element.getAttribute("name").equalsIgnoreCase(name))
			    {
			        previousNode = previousNode.getPreviousSibling();
			        while(previousNode != null && previousNode.getNodeType() != Node.ELEMENT_NODE)
			        {
					    previousNode = previousNode.getPreviousSibling();
			        	//break;
			        }
					element = ((Element)previousNode);
			    }
				
				if(previousNode != null)
				{
					parentNode.removeChild(component);
				    parentNode.insertBefore(component, previousNode);
				    hasChanged = true;
				}
			}
			else if(this.direction.intValue() == 1) //Down
			{
			    Node nextNode = component.getNextSibling();
			    
		        while(nextNode != null && nextNode.getNodeType() != Node.ELEMENT_NODE)
		        {
		        	nextNode = nextNode.getNextSibling();
		        	break;
		        }
			    
			    Element element = ((Element)nextNode);
				while(element != null && !element.getAttribute("name").equalsIgnoreCase(name))
			    {
				    nextNode = nextNode.getNextSibling();
					element = ((Element)nextNode);
			    }
				
				if(nextNode != null)
				    nextNode = nextNode.getNextSibling();
				
				if(nextNode != null)
				{
					parentNode.removeChild(component);
				    parentNode.insertBefore(component, nextNode);
				    hasChanged = true;
				}
				else
				{
				    parentNode.removeChild(component);
				    parentNode.appendChild(component);
				    hasChanged = true;
				}
			}		
			
			if(hasChanged)
			{
				String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
				//logger.info("modifiedXML:" + modifiedXML);
				
				ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, this.masterLanguageVO.getId(), true, "Meta information", DeliveryContext.getDeliveryContext());
				ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());
				ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
			}
		}
				
		this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&focusElementId=" + this.componentId + "&showSimple=" + this.showSimple + "&stateChanged=" + stateChanged;
		//this.getResponse().sendRedirect(url);		
		
		this.url = this.getResponse().encodeURL(url);
		this.getResponse().sendRedirect(url);
	    return NONE; 
	}
	

	/**
	 * This method updates the given properties with new values. 
	 */
    
	public String doUpdateComponentProperty() throws Exception
	{
		if(logger.isInfoEnabled())
		{
			logger.info("************************************************************");
			logger.info("* doUpdateComponentProperty                           		*");
			logger.info("************************************************************");
			logger.info("siteNodeId:" + this.siteNodeId);
			logger.info("languageId:" + this.languageId);
			logger.info("contentId:" + this.contentId);
			logger.info("componentId:" + this.componentId);
			logger.info("slotId:" + this.slotId);
			logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
		}

		try
		{
			initialize();
	
			Locale locale = LanguageController.getController().getLocaleWithId(languageId);
			
			String componentXML = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId());			
			//logger.info("componentXML:" + componentXML);
			
			ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());
	
			Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
			
			String characterEncoding = this.getRequest().getCharacterEncoding();
			characterEncoding= this.getResponse().getCharacterEncoding();
		
			String componentContentId = null;
	
			String propertyName = this.getRequest().getParameter("propertyName");
			String propertyValue = "";
			if(propertyName != null && !propertyName.equals(""))
			{
				String[] propertyValues = this.getRequest().getParameterValues(propertyName);
				
				if(propertyValues != null && propertyValues.length == 1)
				{
					propertyValue = propertyValues[0];
				}
				else if(propertyValues != null)
				{
					StringBuffer sb = new StringBuffer();
					for(int i=0; i<propertyValues.length;i++)
					{
						if(i > 0)
							sb.append(",");
						sb.append(propertyValues[i]);
					}
					propertyValue = sb.toString();
				}
	
				logger.info("propertyName:" + propertyName);
				logger.info("propertyValue:" + propertyValue);
				String separator = System.getProperty("line.separator");
				propertyValue = propertyValue.replaceAll(separator, "igbr");
				logger.info("propertyValue1:" + propertyValue);
	        	propertyValue = PageEditorHelper.untransformAttribute(propertyValue);
				logger.info("propertyValue2:" + propertyValue);
	 			
				if(propertyValue != null && !propertyValue.equals("") && !propertyValue.equalsIgnoreCase("undefined"))
				{
					String componentPropertyXPath = "//component[@id=" + this.componentId + "]/properties/property[@name='" + propertyName + "']";
					//logger.info("componentPropertyXPath:" + componentPropertyXPath);
					NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
					if(anl.getLength() == 0)
					{
						String componentXPath = "//component[@id=" + this.componentId + "]/properties";
						//logger.info("componentXPath:" + componentXPath);
						NodeList componentNodeList = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
						if(componentNodeList.getLength() > 0)
						{
							Element componentProperties = (Element)componentNodeList.item(0);
							addPropertyElement(componentProperties, propertyName, propertyValue, "textfield", locale);
							anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
						}
					}
			
					logger.info("anl:" + anl);
					if(anl.getLength() > 0)
					{
						Element component = (Element)anl.item(0);
						componentContentId = ((Element)component.getParentNode().getParentNode()).getAttribute("contentId");
						
						ContentVO componentContentVO = ContentController.getContentController().getContentVOWithId(new Integer(componentContentId));
						LanguageVO componentMasterLanguageVO = LanguageController.getController().getMasterLanguage(componentContentVO.getRepositoryId());
						ContentVersionVO cv = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(new Integer(componentContentId), componentMasterLanguageVO.getId());
						String componentProperties = ContentVersionController.getContentVersionController().getAttributeValue(cv, "ComponentProperties", false);
						List componentPropertiesList = ComponentPropertyDefinitionController.getController().parseComponentPropertyDefinitions(componentProperties);
						Iterator componentPropertiesListIterator = componentPropertiesList.iterator();
						boolean allowLanguageVariations = true;
						while(componentPropertiesListIterator.hasNext())
						{
							ComponentPropertyDefinition componentPropertyDefinition = (ComponentPropertyDefinition)componentPropertiesListIterator.next();
							if(componentPropertyDefinition.getName().equalsIgnoreCase(propertyName))
							{
								allowLanguageVariations = componentPropertyDefinition.getAllowLanguageVariations();
								break;
							}
						}
						
						if(allowLanguageVariations)
						{
							logger.info("Setting a propertyValue to path_" + locale.getLanguage() + ":" + path);
							component.setAttribute("path_" + locale.getLanguage(), propertyValue);
						    logger.info("Setting 'path_" + locale.getLanguage() + ":" + propertyValue);
						}
						else
						{
							logger.info("Setting a propertyValue to path:" + path);
							component.setAttribute("path", propertyValue);
						    logger.info("Setting 'path:" + propertyValue);
						    component.removeAttribute("path_" + locale.getLanguage());
						}
					}
					else
					{
					    logger.warn("No property could be updated... must be wrong.");
					}
				}
			}
	
			String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
				
			logger.info("contentVersionVO:" + contentVersionVO.getContentVersionId());
			ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
			
			String returnStatus = this.getRequest().getParameter("returnStatus");
			if(returnStatus != null && returnStatus.equalsIgnoreCase("true"))
			{
		        this.getResponse().setContentType("text/html");
		        this.getResponse().getWriter().println("<html><body>Property " + propertyName + " was set to " + propertyValue + "</body></html>");
			}
			else
			{
				this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&focusElementId=" + this.componentId + (!hideComponentPropertiesOnLoad ? "&activatedComponentId=" + this.componentId : "") + "&componentContentId=" + componentContentId + "&showSimple=" + this.showSimple + "&stateChanged=" + stateChanged;
				//this.getResponse().sendRedirect(url);		
	
				this.url = this.getResponse().encodeURL(url);
				this.getResponse().sendRedirect(url);
			}
			return NONE; 
		}
		catch(Exception e)
		{
			logger.error("Error setting property:" + e.getMessage(), e);
			return ERROR;
		}
	}

	
	/**
	 * This method updates the given properties with new values. 
	 */
    
	public String doUpdateComponentProperties() throws Exception
	{
		if(logger.isInfoEnabled())
		{
			logger.info("************************************************************");
			logger.info("* doUpdateComponentProperties                              *");
			logger.info("************************************************************");
			logger.info("siteNodeId:" + this.siteNodeId);
			logger.info("languageId:" + this.languageId);
			logger.info("contentId:" + this.contentId);
			logger.info("componentId:" + this.componentId);
			logger.info("slotId:" + this.slotId);
			logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
		}

		try
		{
			initialize();
	
			Iterator parameterNames = this.getRequest().getParameterMap().keySet().iterator();
			while(parameterNames.hasNext())
			{
				String name = (String)parameterNames.next();
				String value = (String)this.getRequest().getParameter(name);
				logger.info(name + "=" + value);
			}
	
			Integer siteNodeId 	= new Integer(this.getRequest().getParameter("siteNodeId"));
			Integer languageId 	= new Integer(this.getRequest().getParameter("languageId"));
			
			Locale locale = LanguageController.getController().getLocaleWithId(languageId);
			
			String entity  		= this.getRequest().getParameter("entity");
			
			String componentXML = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId());			
			//logger.info("componentXML:" + componentXML);
			
			ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());
	
			Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
			
			String characterEncoding= this.getRequest().getCharacterEncoding();
			characterEncoding= this.getResponse().getCharacterEncoding();
		
			logger.info("siteNodeId:" + siteNodeId);
			logger.info("languageId:" + languageId);
			logger.info("entity:" + entity);
	
			String componentContentId = null;
	
			int propertyIndex = 0;	
			String propertyName = this.getRequest().getParameter(propertyIndex + "_propertyName");
			while(propertyName != null && !propertyName.equals(""))
			{
				String[] propertyValues = this.getRequest().getParameterValues(propertyName);
				String propertyValue = "";
				
				if(propertyValues != null && propertyValues.length == 1)
					propertyValue = propertyValues[0];
				else if(propertyValues != null)
				{
					StringBuffer sb = new StringBuffer();
					for(int i=0; i<propertyValues.length;i++)
					{
						if(i > 0)
							sb.append(",");
						sb.append(propertyValues[i]);
					}
					propertyValue = sb.toString();
				}
	
				logger.info("propertyName:" + propertyName);
				logger.info("propertyValue:" + propertyValue);
				String separator = System.getProperty("line.separator");
				propertyValue = propertyValue.replaceAll(separator, "igbr");
				logger.info("propertyValue1:" + propertyValue);
	        	propertyValue = PageEditorHelper.untransformAttribute(propertyValue);
				logger.info("propertyValue2:" + propertyValue);
	 			
				if(propertyValue != null && !propertyValue.equals("") && !propertyValue.equalsIgnoreCase("undefined"))
				{
					String componentPropertyXPath = "//component[@id=" + this.componentId + "]/properties/property[@name='" + propertyName + "']";
					//logger.info("componentPropertyXPath:" + componentPropertyXPath);
					NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
					if(anl.getLength() == 0)
					{
						String componentXPath = "//component[@id=" + this.componentId + "]/properties";
						//logger.info("componentXPath:" + componentXPath);
						NodeList componentNodeList = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
						if(componentNodeList.getLength() > 0)
						{
							Element componentProperties = (Element)componentNodeList.item(0);
							addPropertyElement(componentProperties, propertyName, propertyValue, "textfield", locale);
							anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
						}
					}
			
					logger.info("anl:" + anl);
					if(anl.getLength() > 0)
					{
						Element component = (Element)anl.item(0);
						componentContentId = ((Element)component.getParentNode().getParentNode()).getAttribute("contentId");
						
						ContentVO componentContentVO = ContentController.getContentController().getContentVOWithId(new Integer(componentContentId));
						LanguageVO componentMasterLanguageVO = LanguageController.getController().getMasterLanguage(componentContentVO.getRepositoryId());
						ContentVersionVO cv = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(new Integer(componentContentId), componentMasterLanguageVO.getId());
						String componentProperties = ContentVersionController.getContentVersionController().getAttributeValue(cv, "ComponentProperties", false);
						List componentPropertiesList = ComponentPropertyDefinitionController.getController().parseComponentPropertyDefinitions(componentProperties);
						Iterator componentPropertiesListIterator = componentPropertiesList.iterator();
						boolean allowLanguageVariations = true;
						while(componentPropertiesListIterator.hasNext())
						{
							ComponentPropertyDefinition componentPropertyDefinition = (ComponentPropertyDefinition)componentPropertiesListIterator.next();
							if(componentPropertyDefinition.getName().equalsIgnoreCase(propertyName))
							{
								allowLanguageVariations = componentPropertyDefinition.getAllowLanguageVariations();
								break;
							}
						}
						
						if(allowLanguageVariations)
						{
							component.setAttribute("path_" + locale.getLanguage(), propertyValue);
						    logger.info("Setting 'path_" + locale.getLanguage() + ":" + propertyValue);
						}
						else
						{
							component.setAttribute("path", propertyValue);
						    logger.info("Setting 'path:" + propertyValue);
						    component.removeAttribute("path_" + locale.getLanguage());
						}
					}
					else
					{
					    logger.warn("No property could be updated... must be wrong.");
					}
				}
				
				propertyIndex++;
				
				propertyName = this.getRequest().getParameter(propertyIndex + "_propertyName");
			}
	
			String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
				
			logger.info("contentVersionVO:" + contentVersionVO.getContentVersionId());
			ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
			this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&focusElementId=" + this.componentId + (!hideComponentPropertiesOnLoad ? "&activatedComponentId=" + this.componentId : "") + "&componentContentId=" + componentContentId + "&showSimple=" + this.showSimple + "&stateChanged=" + stateChanged;
			//this.getResponse().sendRedirect(url);		

			this.url = this.getResponse().encodeURL(url);
			this.getResponse().sendRedirect(url);
		    return NONE; 
		}
		catch(Exception e)
		{
			logger.error("Error setting property:" + e.getMessage(), e);
			return ERROR;
		}
	}


	/**
	 * This method shows the user a list of Components(HTML Templates). 
	 */
    
	public String doDeleteComponent() throws Exception
	{
		initialize();
		//logger.info("************************************************************");
		//logger.info("* DELETING COMPONENT                                         *");
		//logger.info("************************************************************");
		//logger.info("siteNodeId:" + this.siteNodeId);
		//logger.info("languageId:" + this.languageId);
		//logger.info("contentId:" + this.contentId);
		//logger.info("componentId:" + this.componentId);
		//logger.info("slotId:" + this.slotId);
		//logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
				
		logger.info("doDeleteComponent:" + this.getRequest().getQueryString());
		
		String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId());			

		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
		String componentXPath = "//component[@id=" + this.componentId + "]";
		//logger.info("componentXPath:" + componentXPath);
		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
		//logger.info("anl:" + anl.getLength());
		if(anl.getLength() > 0)
		{
			Element component = (Element)anl.item(0);
			component.getParentNode().removeChild(component);
			
			String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
			
			ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());

			ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
		}
		
		this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&showSimple=" + this.showSimple + "&stateChanged=" + stateChanged;
		
		this.url = this.getResponse().encodeURL(url);
		this.getResponse().sendRedirect(url);
	    return NONE; 
	}
	
	/**
	 * This method shows the user a list of Components(HTML Templates). 
	 */
    
	public String doChangeComponent() throws Exception
	{
		initialize();
		//logger.info("************************************************************");
		//logger.info("* DELETING COMPONENT                                         *");
		//logger.info("************************************************************");
		//logger.info("siteNodeId:" + this.siteNodeId);
		//logger.info("languageId:" + this.languageId);
		//logger.info("contentId:" + this.contentId);
		//logger.info("componentId:" + this.componentId);
		//logger.info("slotId:" + this.slotId);
		//logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
				
		logger.info("doChangeComponent:" + this.getRequest().getQueryString());
		
		logger.info("masterLanguageId:" + this.masterLanguageVO.getId());

		Integer newComponentId = new Integer(0);

		String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId());			
		logger.info("componentXML:" + componentXML);
		
		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
		String componentXPath = "//component[@id=" + this.componentId + "]";
		
		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
		if(anl.getLength() > 0 && this.newComponentContentId != null)
		{
			Element component = (Element)anl.item(0);
			
			ContentVO contentVO = ContentController.getContentController().getContentVOWithId(this.newComponentContentId);
			ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVO.getContentTypeDefinitionId());
			boolean isPagePartReference = false;
			if(contentTypeDefinitionVO.getName().equals("PagePartTemplate"))
				isPagePartReference = true;

			ContentVersionVO newComponentContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(this.newComponentContentId, this.masterLanguageVO.getId());
			if(newComponentContentVersionVO == null)
			{			
				LanguageVO contentMasterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());
				newComponentContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(this.newComponentContentId, contentMasterLanguageVO.getId());
			}
			
			if(newComponentContentVersionVO != null)
			{
				String template = ContentVersionController.getContentVersionController().getAttributeValue(newComponentContentVersionVO, "Template", false);
				logger.info("template:" + template);
				
				String subComponentsXPath = "//component[@id=" + this.componentId + "]//component";
				NodeList subComponents = org.apache.xpath.XPathAPI.selectNodeList(component, subComponentsXPath);
				logger.info("subComponents:" + subComponents.getLength());
				for(int i=0; i<subComponents.getLength(); i++)
				{
					Element subComponent = (Element)subComponents.item(i);
					if(isPagePartReference)
					{
						//Removing children if it was a pagePartReference
						NodeList propertiesNodeList = subComponent.getElementsByTagName("properties");
						if(propertiesNodeList.getLength() > 0)
						{
							Element propertiesElement = (Element)propertiesNodeList.item(0);
							NodeList propertyNodeList = propertiesElement.getElementsByTagName("property");
							for(int j=0; j<propertyNodeList.getLength(); j++)
							{
								Element property = (Element)propertyNodeList.item(j);
								Node parentNode = property.getParentNode();
								parentNode.removeChild(property);
							}
						}
						
						Node parentNode = subComponent.getParentNode();
						parentNode.removeChild(subComponent);
					}
					else
					{
						String slotId = subComponent.getAttribute("name");
						logger.info("subComponent slotId:" + slotId);	
						if(template.indexOf("id=\"" + slotId + "\"") == -1)
						{
							logger.info("deleting subComponent as it was not part of the new template");
							Node parentNode = subComponent.getParentNode();
							parentNode.removeChild(subComponent);
						}	
					}
				}
			}
			
			component.setAttribute("contentId", "" + this.newComponentContentId);
			if(isPagePartReference)
				component.setAttribute("isPagePartReference", "true");
				
			String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
			logger.info("modifiedXML:" + modifiedXML);
			
			ContentVO boundContentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, this.masterLanguageVO.getId(), contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, this.masterLanguageVO.getId(), true, "Meta information", DeliveryContext.getDeliveryContext());
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(boundContentVO.getId(), this.masterLanguageVO.getId());
			
			ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
		}
		
		logger.info("newComponentId:" + newComponentId);
		
		this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&focusElementId=" + this.componentId + "&activatedComponentId=" + this.componentId + "&showSimple=" + this.showSimple + "&stateChanged=" + stateChanged;
		//this.getResponse().sendRedirect(url);		
		
		this.url = this.getResponse().encodeURL(url);
		this.getResponse().sendRedirect(url);
		
	    return NONE; 
	}

	
	/**
	 * This method shows the user a list of Components(HTML Templates). 
	 */
    
	public String doAddComponentPropertyBinding() throws Exception
	{
		initialize();
		//logger.info("************************************************************");
		//logger.info("* doAddComponentPropertyBinding                            *");
		//logger.info("************************************************************");
		//logger.info("siteNodeId:" + this.siteNodeId);
		//logger.info("languageId:" + this.languageId);
		//logger.info("contentId:" + this.contentId);
		//logger.info("componentId:" + this.componentId);
		//logger.info("slotId:" + this.slotId);
		//logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
		//logger.info("assetKey:" + assetKey);
		
		Integer siteNodeId = new Integer(this.getRequest().getParameter("siteNodeId"));
		Integer languageId = this.masterLanguageVO.getId();
		if(this.getRequest().getParameter("languageId") != null && !this.getRequest().getParameter("languageId").equals(""))
		{
			languageId = new Integer(this.getRequest().getParameter("languageId"));
		}
		
		Locale locale = LanguageController.getController().getLocaleWithId(languageId);
		
		String entity = this.getRequest().getParameter("entity");
		Integer entityId  = new Integer(this.getRequest().getParameter("entityId"));
		String propertyName = this.getRequest().getParameter("propertyName");
			
		String componentXML = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId());

		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
		String componentPropertyXPath = "//component[@id=" + this.componentId + "]/properties/property[@name='" + propertyName + "']";
		//logger.info("componentPropertyXPath:" + componentPropertyXPath);
		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
		if(anl.getLength() == 0)
		{
			String componentXPath = "//component[@id=" + this.componentId + "]/properties";
			//logger.info("componentXPath:" + componentXPath);
			NodeList componentNodeList = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
			if(componentNodeList.getLength() > 0)
			{
				Element componentProperties = (Element)componentNodeList.item(0);
				if(entity.equalsIgnoreCase("SiteNode"))
				    addPropertyElement(componentProperties, propertyName, path, "siteNodeBinding", locale);
				else if(entity.equalsIgnoreCase("Content"))
					addPropertyElement(componentProperties, propertyName, path, "contentBinding", locale);
				else if(entity.equalsIgnoreCase("Category"))
					addPropertyElement(componentProperties, propertyName, path, "categoryBinding", locale);
				
				anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
			}
		}
		
		//logger.info("anl:" + anl);
		if(anl.getLength() > 0)
		{
			Element component = (Element)anl.item(0);
			if(entity.equalsIgnoreCase("SiteNode"))
			{
				SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(entityId);
				path = siteNodeVO.getName();
			}
			else if(entity.equalsIgnoreCase("Content"))
			{
				ContentVO contentVO = ContentController.getContentController().getContentVOWithId(entityId);
				path = contentVO.getName();
			}
			else if(entity.equalsIgnoreCase("Category"))
			{
				CategoryVO categoryVO = CategoryController.getController().findById(entityId);
				path = categoryVO.getDisplayName();
			}

			component.setAttribute("path", path);
			NamedNodeMap attributes = component.getAttributes();
			logger.debug("NumberOfAttributes:" + attributes.getLength() + ":" + attributes);
						
			List removableAttributes = new ArrayList();
			for(int i=0; i<attributes.getLength(); i++)
			{
				Node node = attributes.item(i);
				logger.debug("Node:" + node.getNodeName());
				if(node.getNodeName().startsWith("path_"))
				{
					removableAttributes.add("" + node.getNodeName());
				}
			}
			
			Iterator removableAttributesIterator = removableAttributes.iterator();
			while(removableAttributesIterator.hasNext())
			{
				String attributeName = (String)removableAttributesIterator.next();
				logger.debug("Removing node:" + attributeName);
				component.removeAttribute(attributeName);
			}
			
			NodeList children = component.getChildNodes();
			for(int i=0; i < children.getLength(); i++)
			{
				Node node = children.item(i);
				component.removeChild(node);
			}
			
			if(assetKey != null)
			{
				logger.debug("assetKey:" + assetKey);
				String fromEncoding = CmsPropertyHandler.getUploadFromEncoding();
				if(fromEncoding == null)
					fromEncoding = "iso-8859-1";
				
				String toEncoding = CmsPropertyHandler.getUploadToEncoding();
				if(toEncoding == null)
					toEncoding = "utf-8";
				
				this.assetKey = new String(this.assetKey.getBytes(fromEncoding), toEncoding);
				logger.debug("assetKey:" + assetKey);
			}
			
			Element newComponent = addBindingElement(component, entity, entityId, assetKey);
			String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
			//logger.info("modifiedXML:" + modifiedXML);
			
			ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());

			ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
		}
					
		Boolean hideComponentPropertiesOnLoad = (Boolean)getHttpSession().getAttribute("" + siteNodeId + "_hideComponentPropertiesOnLoad");
		if(hideComponentPropertiesOnLoad == null) 
			hideComponentPropertiesOnLoad = false;
		else
			getHttpSession().removeAttribute("" + siteNodeId + "_hideComponentPropertiesOnLoad");
			
		if(showDecorated == null || !showDecorated.equalsIgnoreCase("false"))
			this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&focusElementId=" + this.componentId + (!hideComponentPropertiesOnLoad ? "&activatedComponentId=" + this.componentId : "") + "&showSimple=" + this.showSimple + "&stateChanged=" + stateChanged;
		else
			this.url = getComponentRendererUrl() + "ViewPage.action?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&focusElementId=" + this.componentId + (!hideComponentPropertiesOnLoad ? "&activatedComponentId=" + this.componentId : "") + "&showSimple=" + this.showSimple + "&stateChanged=" + stateChanged;
		
		this.url = this.getResponse().encodeURL(url);
		this.getResponse().sendRedirect(url);
	    return NONE; 
	}


	/**
	 * This method shows the user a list of Components(HTML Templates). 
	 */
    
	public String doAddComponentPropertyBindingWithQualifyer() throws Exception
	{
		initialize();
		//logger.info("************************************************************");
		//logger.info("* doAddComponentPropertyBindingWithQualifyer               *");
		//logger.info("************************************************************");
		//logger.info("siteNodeId:" + this.siteNodeId);
		//logger.info("languageId:" + this.languageId);
		//logger.info("contentId:" + this.contentId);
		//logger.info("componentId:" + this.componentId);
		//logger.info("slotId:" + this.slotId);
		//logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
		
		Integer siteNodeId 	= new Integer(this.getRequest().getParameter("siteNodeId"));
		Integer languageId 	= new Integer(this.getRequest().getParameter("languageId"));
		Integer contentId 	= new Integer(this.getRequest().getParameter("contentId"));
		
		Locale locale = LanguageController.getController().getLocaleWithId(languageId);

		String qualifyerXML = this.getRequest().getParameter("qualifyerXML");
		String propertyName = this.getRequest().getParameter("propertyName");
		
		//logger.info("siteNodeId:" + siteNodeId);
		//logger.info("languageId:" + languageId);
		//logger.info("contentId:" + contentId);
		//logger.info("qualifyerXML:" + qualifyerXML);
		//logger.info("propertyName:" + propertyName);
			
		NodeDeliveryController nodeDeliveryController			    = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
		
		String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId());			
		//logger.info("componentXML:" + componentXML);

		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
		String componentPropertyXPath = "//component[@id=" + this.componentId + "]/properties/property[@name='" + propertyName + "']";
		//logger.info("componentPropertyXPath:" + componentPropertyXPath);
		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
		if(anl.getLength() > 0)
		{
			Node propertyNode = anl.item(0);
			propertyNode.getParentNode().removeChild(propertyNode);
		}

		String componentXPath = "//component[@id=" + this.componentId + "]/properties";
		//logger.info("componentXPath:" + componentXPath);
		NodeList componentNodeList = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
		if(componentNodeList.getLength() > 0)
		{
			Element componentProperties = (Element)componentNodeList.item(0);
			addPropertyElement(componentProperties, propertyName, path, "contentBinding", locale);
			anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
		}
		//}
		
		if(anl.getLength() > 0)
		{
			Element component = (Element)anl.item(0);
			component.setAttribute("path", path);
			component.setAttribute("path_" + locale.getLanguage(), path);
			
			addBindingElement(component, qualifyerXML);
			String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
			
			ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());

			ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
		}
					
		if(showDecorated == null || showDecorated.equalsIgnoreCase("true"))
			this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&focusElementId=" + this.componentId + "&activatedComponentId=" + this.componentId + "&showSimple=" + this.showSimple + "&stateChanged=" + stateChanged;
		else
			this.url = getComponentRendererUrl() + "ViewPage.action?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&focusElementId=" + this.componentId + "&activatedComponentId=" + this.componentId + "&showSimple=" + this.showSimple + "&stateChanged=" + stateChanged;
		
		this.url = this.getResponse().encodeURL(url);
		this.getResponse().sendRedirect(url);
	    return NONE; 
	}
	
	/**
	 * This method shows the user a list of Components(HTML Templates). 
	 */
    
	public String doDeleteComponentBinding() throws Exception
	{
		initialize();
		//logger.info("************************************************************");
		//logger.info("* doDeleteComponentBinding               *");
		//logger.info("************************************************************");
		//logger.info("siteNodeId:" + this.siteNodeId);
		//logger.info("languageId:" + this.languageId);
		//logger.info("contentId:" + this.contentId);
		//logger.info("componentId:" + this.componentId);
		//logger.info("slotId:" + this.slotId);
		//logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);

		Integer siteNodeId 	= new Integer(this.getRequest().getParameter("siteNodeId"));
		Integer languageId 	= new Integer(this.getRequest().getParameter("languageId"));
		Integer contentId  	= new Integer(this.getRequest().getParameter("contentId"));
		Integer bindingId  	= new Integer(this.getRequest().getParameter("bindingId"));
		
		//logger.info("siteNodeId:" + siteNodeId);
		//logger.info("languageId:" + languageId);
		//logger.info("contentId:" + contentId);
			
		//String templateString = getPageTemplateString(templateController, siteNodeId, languageId, contentId); 
		String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId());			
		//logger.info("componentXML:" + componentXML);

		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
		String componentXPath = "//component[@id=" + this.componentId + "]/bindings/binding[@id=" + bindingId + "]";
		//logger.info("componentXPath:" + componentXPath);
		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
		//logger.info("anl:" + anl.getLength());
		if(anl.getLength() > 0)
		{
			Element component = (Element)anl.item(0);
			component.getParentNode().removeChild(component);
			String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
			
			ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());

			ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
		}
			
		this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&focusElementId=" + this.componentId + "&activatedComponentId=" + this.componentId + "&showSimple=" + this.showSimple + "&stateChanged=" + stateChanged;
		//this.getResponse().sendRedirect(url);		
		
		this.url = this.getResponse().encodeURL(url);
		//this.getResponse().sendRedirect(url);
	    return NONE; 
	}
		    
		    
	/**
	 * This method shows the user a list of Components(HTML Templates). 
	 */
    
	public List getComponentBindings() throws Exception
	{
		List bindings = new ArrayList();
			
		try
		{
			Integer siteNodeId = new Integer(this.getRequest().getParameter("siteNodeId"));
			Integer languageId = new Integer(this.getRequest().getParameter("languageId"));
			Integer contentId  = new Integer(this.getRequest().getParameter("contentId"));
			String propertyName = this.getRequest().getParameter("propertyName");
	
			//logger.info("**********************************************************************************");
			//logger.info("siteNodeId:" + siteNodeId);
			//logger.info("languageId:" + languageId);
			//logger.info("contentId:" + contentId);
			//logger.info("**********************************************************************************");
							
			//String templateString = getPageTemplateString(templateController, siteNodeId, languageId, contentId); 
			String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId());			
			//logger.info("componentXML:" + componentXML);
	
			Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
			String componentXPath = "//component[@id=" + this.componentId + "]/properties/property[@name='" + propertyName + "']/binding";
			//logger.info("componentXPath:" + componentXPath);
			NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
			//logger.info("anl:" + anl.getLength());
			for(int i=0; i<anl.getLength(); i++)
			{
				Element component = (Element)anl.item(i);
				String entityName = component.getAttribute("entity");
				String entityId = component.getAttribute("entityId");
				String assetKey = component.getAttribute("assetKey");
				
				try
				{
					String path = "Undefined";
					if(entityName.equalsIgnoreCase("SiteNode"))
					{
						SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(new Integer(entityId));
						path = siteNodeVO.getName();
					}
					else if(entityName.equalsIgnoreCase("Content")) 
					{
						ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(entityId));
						path = contentVO.getName();
					}
					
					Map binding = new HashMap();
					binding.put("entityName", entityName);
					binding.put("entityId", entityId);
					binding.put("assetKey", assetKey);
					binding.put("path", path);
					bindings.add(binding);
				}
				catch(Exception e) 
				{
				    logger.warn("There was " + entityName + " bound to property '" + propertyName + "' on siteNode " + siteNodeId + " which appears to have been deleted.");
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return bindings;
	}
			    
	//Nice code
	
	/**
	 * This method deletes a component property value. This is to enable users to quickly remove a property value no matter what type.
	 */
    
	public String doDeleteComponentPropertyValue() throws Exception
	{
		initialize();
	
		Integer siteNodeId 	= new Integer(this.getRequest().getParameter("siteNodeId"));
		Integer languageId 	= new Integer(this.getRequest().getParameter("languageId"));
		Integer contentId  	= new Integer(this.getRequest().getParameter("contentId"));
		String propertyName	= this.getRequest().getParameter("propertyName");
		
		Locale locale = LanguageController.getController().getLocaleWithId(languageId);

		//logger.info("siteNodeId:" + siteNodeId);
		//logger.info("languageId:" + languageId);
		//logger.info("contentId:" + contentId);
		//logger.info("propertyName:" + propertyName);
			
		String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId());			
		//logger.info("componentXML:" + componentXML);

		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
		String componentPropertyXPath = "//component[@id=" + this.componentId + "]/properties/property[@name='" + propertyName + "']";
		//logger.info("componentPropertyXPath:" + componentPropertyXPath);
		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
		if(anl.getLength() > 0)
		{
			Node propertyNode = anl.item(0);
			Element propertyElement = (Element)propertyNode;
			
			propertyElement.removeAttribute("path");
			propertyElement.removeAttribute("path_" + locale.getLanguage());
			if(propertyElement.getAttributes().getLength() == 0);
			{
				propertyNode.getParentNode().removeChild(propertyNode);
			}
		}

		String detailSiteNodeIdPropertyXPath = "//component[@id=" + this.componentId + "]/properties/property[@name='" + propertyName + "_detailSiteNodeId']";
		//logger.info("componentPropertyXPath:" + componentPropertyXPath);
		NodeList anl2 = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), detailSiteNodeIdPropertyXPath);
		if(anl2.getLength() > 0)
		{
			Node propertyNode = anl2.item(0);
			Element propertyElement = (Element)propertyNode;
			
			propertyElement.removeAttribute("path");
			propertyElement.removeAttribute("path_" + locale.getLanguage());
			if(propertyElement.getAttributes().getLength() == 0);
			{
				propertyNode.getParentNode().removeChild(propertyNode);
			}
		}

		String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
		//logger.info("modifiedXML:" + modifiedXML);
		
		ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());

		ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());

		this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&focusElementId=" + this.componentId + "&activatedComponentId=" + this.componentId + "&showSimple=" + this.showSimple + "&stateChanged=" + stateChanged;
		//this.getResponse().sendRedirect(url);		
		
		this.url = this.getResponse().encodeURL(url);
		this.getResponse().sendRedirect(url);
	    return NONE; 
	}
		    
	/**
	 * This method creates a parameter for the given input type.
	 * This is to support form steering information later.
	 */
	
	private Element addPropertyElement(Element parent, String name, String path, String type, Locale locale)
	{
		Element element = parent.getOwnerDocument().createElement("property");
		element.setAttribute("name", name);
		
		if(type.equalsIgnoreCase("siteNodeBinding") || type.equalsIgnoreCase("contentBinding"))
		{
			element.setAttribute("path", path);
			element.setAttribute("path_" + locale.getLanguage(), path);
		}
		else
		{
			element.setAttribute("path_" + locale.getLanguage(), path);
		}
		
		element.setAttribute("type", type);
		parent.appendChild(element);
		return element;
	}

	/**
	 * This method creates a parameter for the given input type.
	 * This is to support form steering information later.
	 */
	
	private Element addComponentElement(Element parent, Integer id, String name, Integer contentId, boolean isPagePartReference)
	{
		Element element = parent.getOwnerDocument().createElement("component");
		if(isPagePartReference)
			element.setAttribute("isPagePartReference", "true");
			
		element.setAttribute("id", id.toString());
		element.setAttribute("contentId", contentId.toString());
		element.setAttribute("name", name);
		Element properties = parent.getOwnerDocument().createElement("properties");
		element.appendChild(properties);
		Element subComponents = parent.getOwnerDocument().createElement("components");
		element.appendChild(subComponents);
		parent.appendChild(element);
		return element;
	}

	/**
	 * This method creates a parameter for the given input type.
	 * This is to support form steering information later.
	 */
	
	private Element addComponentElementBefore(Element parent, Element beforeElement, Integer id, String name, Integer contentId, boolean isPagePartReference)
	{
		Element element = parent.getOwnerDocument().createElement("component");
		if(isPagePartReference)
			element.setAttribute("isPagePartReference", "true");
			
		element.setAttribute("id", id.toString());
		element.setAttribute("contentId", contentId.toString());
		element.setAttribute("name", name);
		Element properties = parent.getOwnerDocument().createElement("properties");
		element.appendChild(properties);
		Element subComponents = parent.getOwnerDocument().createElement("components");
		element.appendChild(subComponents);
		parent.insertBefore(element, beforeElement);
		return element;
	}

	/**
	 * This method creates a parameter for the given input type.
	 * This is to support form steering information later.
	 */
	
	private Element addBindingElement(Element parent, String entity, Integer entityId, String assetKey)
	{
		Element element = parent.getOwnerDocument().createElement("binding");
		element.setAttribute("entityId", entityId.toString());
		element.setAttribute("entity", entity);
		if(assetKey != null && !assetKey.equals(""))
			element.setAttribute("assetKey", assetKey);
		
		parent.appendChild(element);
		return element;
	}

	/**
	 * This method creates a parameter for the given input type.
	 * This is to support form steering information later.
	 */
	
	private void addBindingElement(Element parent, String qualifyerXML) throws Exception
	{
		Document document = XMLHelper.readDocumentFromByteArray(qualifyerXML.getBytes("utf-8"));
		NodeList nl = document.getChildNodes().item(0).getChildNodes();
		for(int i=0; i<nl.getLength(); i++)
		{
			Element qualifyerElement = (Element)nl.item(i);
			//logger.info("qualifyerElement:" + qualifyerElement);
			String entityName = qualifyerElement.getNodeName();
			String assetKey = qualifyerElement.getAttribute("assetKey");
			String entityId = qualifyerElement.getFirstChild().getNodeValue();
			//logger.info("entityName:" + entityName);
			//logger.info("entityId:" + entityId);
			
			Element element = parent.getOwnerDocument().createElement("binding");
			element.setAttribute("entityId", entityId);
			element.setAttribute("entity", entityName);
			element.setAttribute("assetKey", assetKey);
			parent.appendChild(element);
		}
	}
	
	
	/**
	 * This method returns the contents that are of contentTypeDefinition "HTMLTemplate" sorted on the property given.
	 */
	
	public List getSortedComponents(String sortProperty) throws Exception
	{
	    List componentVOList = null;
	    
	    try
	    {
	        String direction = "asc";
	        componentVOList = ComponentController.getController().getComponentVOList(sortProperty, direction, allowedComponentNames, disallowedComponentNames, allowedComponentGroupNames, this.getInfoGluePrincipal());
	    }
	    catch(Exception e)
	    {
	        logger.error("Error getting sorted components. Message: " + e.getMessage(), e);
	    }
		
	    return componentVOList;
	}
	     	     
	/**
	 * This method fetches the template-string.
	 */
    
	private String getPageComponentsString(Integer siteNodeId, Integer languageId) throws SystemException, Exception
	{
		String template = null;
    	
		try
		{
			ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());

			if(contentVO == null)
				throw new SystemException("There was no template bound to this page which makes it impossible to render.");	
			
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageId);
			if(contentVersionVO == null)
			{
				SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
				LanguageVO masterLanguage = LanguageController.getController().getMasterLanguage(siteNodeVO.getRepositoryId());
				contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), masterLanguage.getLanguageId());
			}
			
			template = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO.getId(), "ComponentStructure", false);
			
			if(template == null)
				throw new SystemException("There was no template bound to this page which makes it impossible to render.");	
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}

		return template;
	}
		
	/**
	 * This method fetches an url to the asset for the component.
	 */
	
	public String getDigitalAssetUrl(Integer contentId, String key) throws Exception
	{
		String imageHref = null;
		try
		{
			LanguageVO masterLanguage = LanguageController.getController().getMasterLanguage(ContentController.getContentController().getContentVOWithId(contentId).getRepositoryId());
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, masterLanguage.getId());
			if(contentVersionVO != null)
			{
				List digitalAssets = DigitalAssetController.getDigitalAssetVOList(contentVersionVO.getId());
				Iterator i = digitalAssets.iterator();
				while(i.hasNext())
				{
					DigitalAssetVO digitalAssetVO = (DigitalAssetVO)i.next();
					if(digitalAssetVO.getAssetKey().equals(key))
					{
						imageHref = DigitalAssetController.getDigitalAssetUrl(digitalAssetVO.getId()); 
						break;
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the digitalAsset: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}
	
	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetThumbnailUrl(Integer contentId, String key) throws Exception
	{
		String imageHref = null;
		try
		{
			LanguageVO masterLanguage = LanguageController.getController().getMasterLanguage(ContentController.getContentController().getContentVOWithId(contentId).getRepositoryId());
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, masterLanguage.getId());
			if(contentVersionVO != null)
			{
				List digitalAssets = DigitalAssetController.getDigitalAssetVOList(contentVersionVO.getId());
				Iterator i = digitalAssets.iterator();
				while(i.hasNext())
				{
					DigitalAssetVO digitalAssetVO = (DigitalAssetVO)i.next();
					if(digitalAssetVO.getAssetKey().equals(key))
					{
						imageHref = DigitalAssetController.getDigitalAssetThumbnailUrl(digitalAssetVO.getId()); 
						break;
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("We could not get the thumbnail url of the digitalAsset: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}

	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetThumbnailUrl(Integer contentId) throws Exception
	{
		String imageHref = null;
		try
		{
			LanguageVO masterLanguage = LanguageController.getController().getMasterLanguage(ContentController.getContentController().getContentVOWithId(contentId).getRepositoryId());
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, masterLanguage.getId());
			if(contentVersionVO != null)
			{
				List digitalAssets = DigitalAssetController.getDigitalAssetVOList(contentVersionVO.getId());
				Iterator i = digitalAssets.iterator();
				while(i.hasNext())
				{
					DigitalAssetVO digitalAssetVO = (DigitalAssetVO)i.next();
					imageHref = DigitalAssetController.getDigitalAssetThumbnailUrl(digitalAssetVO.getId()); 
					break;
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("We could not get the thumbnail url of the digitalAsset: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}
	
	public Integer getContentId()
	{
		return contentId;
	}

	public void setContentId(Integer integer)
	{
		contentId = integer;
	}

	public Integer getComponentId()
	{
		return this.componentId;
	}

	public void setComponentId(Integer componentId)
	{
		this.componentId = componentId;
	}
	
	public Integer getParentComponentId() 
	{
		return parentComponentId;
	}
	
    public void setParentComponentId(Integer parentComponentId) 
    {
		this.parentComponentId = parentComponentId;
	}

	public Integer getLanguageId()
	{
		return this.languageId;
	}

	public Integer getSiteNodeId()
	{
		return this.siteNodeId;
	}

	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
	}

	public void setSiteNodeId(Integer siteNodeId)
	{
		this.siteNodeId = siteNodeId;
	}

	public String getSlotId()
	{
		return this.slotId;
	}

	public void setSlotId(String slotId)
	{
		this.slotId = slotId;
	}

	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

    public Integer getFilterRepositoryId()
    {
        return filterRepositoryId;
    }
    
    public void setFilterRepositoryId(Integer filterRepositoryId)
    {
        this.filterRepositoryId = filterRepositoryId;
    }

	public String getSpecifyBaseTemplate()
	{
		return this.specifyBaseTemplate;
	}

	public void setSpecifyBaseTemplate(String specifyBaseTemplate)
	{
		this.specifyBaseTemplate = specifyBaseTemplate;
	}

	public String getPropertyName()
	{
		return this.propertyName;
	}

	public void setPropertyName(String propertyName)
	{
		this.propertyName = propertyName;
	}

	public String getPath()
	{
		return this.path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}
	
	public LanguageVO getMasterLanguageVO()
	{
		return masterLanguageVO;
	}
	
    public String getUrl()
    {
        return url;
    }
	
    public String getSortProperty()
    {
        return sortProperty;
    }
    
    public void setSortProperty(String sortProperty)
    {
        this.sortProperty = sortProperty;
    }
    
    public Integer getDirection()
    {
        return direction;
    }
    
    public void setDirection(Integer direction)
    {
        this.direction = direction;
    }
    
    public String[] getAllowedContentTypeIds()
    {
        return allowedContentTypeIds;
    }
    
    public void setAllowedContentTypeIds(String[] allowedContentTypeIds)
    {
        this.allowedContentTypeIds = allowedContentTypeIds;
    }

    public String getAllowedContentTypeIdsAsUrlEncodedString() throws Exception
    {
        StringBuffer sb = new StringBuffer();
        
        for(int i=0; i<allowedContentTypeIds.length; i++)
        {
            if(i > 0)
                sb.append("&");
            
            sb.append("allowedContentTypeIds=" + URLEncoder.encode(allowedContentTypeIds[i], "UTF-8"));
        }

        return sb.toString();
    }

    public boolean getShowSimple()
    {
        return showSimple;
    }
    
    public void setShowSimple(boolean showSimple)
    {
        this.showSimple = showSimple;
    }
    
    public Integer getPageTemplateContentId()
    {
        return pageTemplateContentId;
    }
    
    public void setPageTemplateContentId(Integer pageTemplateContentId)
    {
        this.pageTemplateContentId = pageTemplateContentId;
    }
    
    public String[] getAllowedComponentNames()
    {
        return allowedComponentNames;
    }
    
    public void setAllowedComponentNames(String[] allowedComponentNames)
    {
        this.allowedComponentNames = allowedComponentNames;
    }

    public String[] getDisallowedComponentNames()
    {
        return disallowedComponentNames;
    }
    
    public void setDisallowedComponentNames(String[] disallowedComponentNames)
    {
        this.disallowedComponentNames = disallowedComponentNames;
    }

    public String getAllowedComponentNamesAsUrlEncodedString() throws Exception
    {
        StringBuffer sb = new StringBuffer("");
        
        if(allowedComponentNames != null)
        {
	        for(int i=0; i<allowedComponentNames.length; i++)
	        {
	            if(i > 0)
	                sb.append("&");
	            
	            sb.append("allowedComponentNames=" + URLEncoder.encode(allowedComponentNames[i], "UTF-8"));
	        }
        }
        
        return sb.toString();
    }

    public String getDisallowedComponentNamesAsUrlEncodedString() throws Exception
    {
        StringBuffer sb = new StringBuffer("");
        
        if(disallowedComponentNames != null)
        {
		    for(int i=0; i<disallowedComponentNames.length; i++)
	        {
	            if(i > 0)
	                sb.append("&");
	            
	            sb.append("disallowedComponentNames=" + URLEncoder.encode(disallowedComponentNames[i], "UTF-8"));
	        }
        }
        
        return sb.toString();
    }

    public String[] getAllowedComponentGroupNames()
    {
        return allowedComponentNames;
    }
    
    public void setAllowedComponentGroupNames(String[] allowedComponentGroupNames)
    {
        this.allowedComponentGroupNames = allowedComponentGroupNames;
    }

    public String getAllowedComponentGroupNamesAsUrlEncodedString() throws Exception
    {
        StringBuffer sb = new StringBuffer("");
        
        if(allowedComponentGroupNames != null)
        {
	        for(int i=0; i<allowedComponentGroupNames.length; i++)
	        {
	            if(i > 0)
	                sb.append("&");
	            
	            sb.append("allowedComponentGroupNames=" + URLEncoder.encode(allowedComponentGroupNames[i], "UTF-8"));
	        }
        }
        
        return sb.toString();
    }

	public String getAssetKey()
	{
		return assetKey;
	}

	public void setAssetKey(String assetKey)
	{
		this.assetKey = assetKey;
	}

	public void setNewComponentContentId(Integer newComponentContentId)
	{
		this.newComponentContentId = newComponentContentId;
	}

	public String getShowDecorated()
	{
		return showDecorated;
	}

	public void setShowDecorated(String showDecorated)
	{
		this.showDecorated = showDecorated;
	}

	public String getSlotPositionComponentId()
	{
		return slotPositionComponentId;
	}

	public void setSlotPositionComponentId(String slotPositionComponentId)
	{
		this.slotPositionComponentId = slotPositionComponentId;
	}

	public Integer getPagePartContentId()
	{
		return pagePartContentId;
	}

	public void setPagePartContentId(Integer pagePartContentId)
	{
		this.pagePartContentId = pagePartContentId;
	}
	
	public void setNewPosition(Integer newPosition)
	{
		this.newPosition = newPosition;
	}

	public boolean getHideComponentPropertiesOnLoad()
	{
		return hideComponentPropertiesOnLoad;
	}

	public void setHideComponentPropertiesOnLoad(boolean hideComponentPropertiesOnLoad)
	{
		this.hideComponentPropertiesOnLoad = hideComponentPropertiesOnLoad;
	}
	
	public boolean getIsPagePartTemplate(Integer contentTypeDefinitionId)
	{
		try
		{
			ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentTypeDefinitionId);
			if(ctdVO != null && ctdVO.getName().equalsIgnoreCase("PagePartTemplate"))
				return true;
			else 
				return false;			
		}
		catch (Exception e) 
		{
			logger.warn("Error looking up content type:" + e.getMessage());
			return false;
		}
	}
}
