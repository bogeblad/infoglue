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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.PropertiesCategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.structure.QualifyerVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.dom.DOMBuilder;

public abstract class ViewEntityPropertiesAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewEntityPropertiesAction.class.getName());

    private static CategoryController categoryController = CategoryController.getController();
	private static PropertiesCategoryController propertiesCategoryController = PropertiesCategoryController.getController();
	
	private String currentAction		= "";
	private String updateAction 		= "";
	private String updateAndExitAction 	= "";
	private String cancelAction 		= "";

	private String toolbarKey 		= "";
	private String titleKey 		= "";
	private String arguments 		= "";
	
	private String entityName 		= null;
	private Integer entityId 		= null;
	
	private String ownerEntityName 	= null;
	private String ownerEntityId 	= null;
	
	private List availableLanguages;
	private List contentTypeDefinitionVOList;
	private List contentTypeAttributes;
	private Integer contentTypeDefinitionId;
	private Integer languageId;
	private String attributeName = "";
	private String textAreaId = "";

	private ContentTypeDefinitionVO contentTypeDefinitionVO;
	
	public abstract String getXML();
	
    public abstract String getCancelAddress() throws Exception;

    public abstract String getReturnAddress() throws Exception;

    public void initialize() throws SystemException
    {
	    this.setAvailableLanguages(LanguageController.getController().getLanguageVOList());
		
		if(this.getLanguageId() == null && this.getAvailableLanguages().size() > 0)
			this.setLanguageId(((LanguageVO)this.getAvailableLanguages().get(0)).getLanguageId());
		
		logger.info("Language:" + this.languageId);
    }
    
	
	/**
	 * This method fetches a value from the xml that is the contentVersions Value. If the 
	 * xml is null the property has not been created yet and no values are present.
	 */
	 
	public String getAttributeValue(String key)
	{
		String value = "";
		try
		{
			String xml = this.getXML();
			if(xml != null)
			{	
				DOMBuilder domBuilder = new DOMBuilder();
				
				Document document = domBuilder.getDocument(xml);
				
				Node node = document.getRootElement().selectSingleNode("attributes/" + key);
				if(node != null)
				{
					value = node.getStringValue();
					if(value != null)
						value = new VisualFormatter().escapeHTML(value);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return value;
	}

	/**
	 * This method fetches a value from the xml that is the property Value. If the 
	 * xml is null the contentVersion has not been created yet and no values are present.
	 */
	 
	public String getUnescapedAttributeValue(String key)
	{
		String value = "";
		try
		{
		    String xml = this.getXML();
		    
			int startTagIndex = xml.indexOf("<" + key + ">");
			int endTagIndex   = xml.indexOf("]]></" + key + ">");

			if(startTagIndex > 0 && startTagIndex < xml.length() && endTagIndex > startTagIndex && endTagIndex <  xml.length())
			{
				value = xml.substring(startTagIndex + key.length() + 11, endTagIndex);
			}					
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return value;
	}

	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetUrl(Integer digitalAssetId) throws Exception
	{
		String imageHref = null;
		try
		{
       		imageHref = DigitalAssetController.getDigitalAssetUrl(digitalAssetId);
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
	
	public String getDigitalAssetThumbnailUrl(Integer digitalAssetId) throws Exception
	{
		String imageHref = null;
		try
		{
       		imageHref = DigitalAssetController.getDigitalAssetThumbnailUrl(digitalAssetId);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the thumbnail: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}

	/**
	 * Gets the path to a content/sitenode.
	 * @param entity
	 * @param entityId
	 * @return
	 */

	public String getQualifyerPath(String entity, String entityId)
	{	
		StringBuffer sb = new StringBuffer("");
		try
		{	
			if(entity.equalsIgnoreCase("Content"))
			{
				ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(entityId));
				sb.insert(0, contentVO.getName() + "/");
				while(contentVO.getParentContentId() != null)
				{
					contentVO = ContentController.getContentController().getContentVOWithId(contentVO.getParentContentId());
					sb.insert(0, contentVO.getName() + "/");
				}
			}
			else if(entity.equalsIgnoreCase("SiteNode"))
			{
				SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(new Integer(entityId));
				sb.insert(0, siteNodeVO.getName() + "/");
				while(siteNodeVO.getParentSiteNodeId() != null)
				{
					siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVO.getParentSiteNodeId());
					sb.insert(0, siteNodeVO.getName() + "/");
				}
			}
			sb.deleteCharAt(sb.length() -1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	/**
	 * Returns the content relation qualifyers
	 * @param qualifyerXML
	 * @return
	 */

	public List getContentRelationQualifyers(String qualifyerXML)
	{
		logger.info("Content qualifyerXML:" + qualifyerXML);
	    return parseQualifyersFromXML(qualifyerXML, "contentId");
	}

	/**
	 * Returns the sitenode relation qualifyers
	 * @param qualifyerXML
	 * @return
	 */

	public List getSiteNodeRelationQualifyers(String qualifyerXML)
	{
		logger.info("Content qualifyerXML:" + qualifyerXML);
	    return parseQualifyersFromXML(qualifyerXML, "siteNodeId");
	}

	/**
	 * Parses qualifyers from an XML
	 * @param qualifyerXML
	 * @return
	 */

	private List parseQualifyersFromXML(String qualifyerXML, String currentEntityIdentifyer)
	{
		List qualifyers = new ArrayList(); 
    	
		if(qualifyerXML == null || qualifyerXML.length() == 0)
			return qualifyers;
		
		try
		{
			Document document = new DOMBuilder().getDocument(qualifyerXML);
			
			String entity = document.getRootElement().attributeValue("entity");
			
			List children = document.getRootElement().elements();
			Iterator i = children.iterator();
			while(i.hasNext())
			{
				Element child = (Element)i.next();
				String id = child.getStringValue();
				
				QualifyerVO qualifyerVO = new QualifyerVO();
				qualifyerVO.setName(currentEntityIdentifyer);
				qualifyerVO.setValue(id);    
				qualifyerVO.setPath(this.getQualifyerPath(entity, id));
				//qualifyerVO.setSortOrder(new Integer(i));
				qualifyers.add(qualifyerVO);     	
			}		        	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return qualifyers;
	}

	
	/**
	 * Return the listing of Category attributes for this type of Content
	 */

	public List getDefinedCategoryKeys()
	{
		try
		{
			if(getContentTypeDefinitionVO() != null)
				return ContentTypeDefinitionController.getController().getDefinedCategoryKeys(getContentTypeDefinitionVO(), true);
		}
		catch(Exception e)
		{
			logger.warn("We could not fetch the list of defined category keys: " + e.getMessage(), e);
		}

		return Collections.EMPTY_LIST;
	}

	/**
	 * Returns the Category tree for the given Category id.
	 * @param categoryId The base Category
	 * @return A list of all Children (and their children, etc)
	 */
	
	public List getAvailableCategories(Integer categoryId)
	{
		try
		{	
		    String protectCategories = CmsPropertyHandler.getProtectCategories();
		    if(protectCategories != null && protectCategories.equalsIgnoreCase("true"))
		        return getCategoryController().getAuthorizedActiveChildren(categoryId, this.getInfoGluePrincipal());
		    return getCategoryController().findAllActiveChildren(categoryId);
		}
		catch(Exception e)
		{
			logger.warn("We could not fetch the list of categories: " + e.getMessage(), e);
		}

		return Collections.EMPTY_LIST;
	}

	
    public CategoryController getCategoryController()
    {
        return categoryController;
    }
    
    public PropertiesCategoryController getPropertiesCategoryController()
    {
        return propertiesCategoryController;
    }

    public String getArguments()
    {
        return arguments;
    }
    
    public String getTitleKey()
    {
        return titleKey;
    }
    
    public String getToolbarKey()
    {
        return toolbarKey;
    }

    public String getCurrentAction()
    {
        return currentAction;
    }
    
    public void setCurrentAction(String currentAction)
    {
        this.currentAction = currentAction;
    }
    
    public String getEntityName()
    {
        return entityName;
    }
    
    public void setEntityName(String entityName)
    {
        this.entityName = entityName;
    }
    
    public String getUpdateAction()
    {
        return updateAction;
    }
    
    public void setUpdateAction(String updateAction)
    {
        this.updateAction = updateAction;
    }

    public String getUpdateAndExitAction()
    {
        return updateAndExitAction;
    }
    
    public void setUpdateAndExitAction(String updateAndExitAction)
    {
        this.updateAndExitAction = updateAndExitAction;
    }

    public void setArguments(String arguments)
    {
        this.arguments = arguments;
    }
    
    public void setTitleKey(String titleKey)
    {
        this.titleKey = titleKey;
    }
    
    public void setToolbarKey(String toolbarKey)
    {
        this.toolbarKey = toolbarKey;
    }
    
    public Integer getEntityId()
    {
        return entityId;
    }
    
    public void setEntityId(Integer entityId)
    {
        this.entityId = entityId;
    }
    
    public String getOwnerEntityId()
    {
        return ownerEntityId;
    }
    
    public void setOwnerEntityId(String ownerEntityId) throws Exception
    {
        this.ownerEntityId = ownerEntityId;
    }
    
    public String getOwnerEntityName()
    {
        return ownerEntityName;
    }
    
    public void setOwnerEntityName(String ownerEntityName)
    {
        this.ownerEntityName = ownerEntityName;
    }
    
    public List getAvailableLanguages()
    {
        return availableLanguages;
    }
    
    public void setAvailableLanguages(List availableLanguages)
    {
        this.availableLanguages = availableLanguages;
    }
    
    public Integer getContentTypeDefinitionId()
    {
        return contentTypeDefinitionId;
    }
    
    public void setContentTypeDefinitionId(Integer contentTypeDefinitionId)
    {
        this.contentTypeDefinitionId = contentTypeDefinitionId;
    }
    
    public ContentTypeDefinitionVO getContentTypeDefinitionVO()
    {
        return contentTypeDefinitionVO;
    }
    
    public void setContentTypeDefinitionVO(ContentTypeDefinitionVO contentTypeDefinitionVO)
    {
        this.contentTypeDefinitionVO = contentTypeDefinitionVO;
    }
    
    public List getContentTypeDefinitionVOList()
    {
        return contentTypeDefinitionVOList;
    }
    
    public void setContentTypeDefinitionVOList(List contentTypeDefinitionVOList)
    {
        this.contentTypeDefinitionVOList = contentTypeDefinitionVOList;
    }
    
    public Integer getLanguageId()
    {
        return languageId;
    }
    
    public void setLanguageId(Integer languageId)
    {
        this.languageId = languageId;
    }
    
    public String getTextAreaId()
    {
        return textAreaId;
    }
    
    public void setTextAreaId(String textAreaId)
    {
        this.textAreaId = textAreaId;
    }
    
    public String getAttributeName()
    {
        return attributeName;
    }
    
    public void setAttributeName(String attributeName)
    {
        this.attributeName = attributeName;
    }
    
    public List getContentTypeAttributes()
    {
        return contentTypeAttributes;
    }
    
    public void setAttributes(List contentTypeAttributes)
    {
        this.contentTypeAttributes = contentTypeAttributes;
    }
    
    
    public String getCancelAction()
    {
        return cancelAction;
    }
    
    public void setCancelAction(String cancelAction)
    {
        this.cancelAction = cancelAction;
    }
}
