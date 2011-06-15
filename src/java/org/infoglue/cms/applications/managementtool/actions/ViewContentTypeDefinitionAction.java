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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xpath.XPathAPI;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.management.CategoryAttribute;
import org.infoglue.cms.entities.management.ContentTypeAttribute;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.providers.ContentDetailPageResolver;
import org.infoglue.cms.services.ContentDetailPageResolversService;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class implements the action class for viewContentTypeDefinition.
 * The use-case lets the user see all information about a specific site/contentTypeDefinition.
 *
 * @author Mattias Bogeblad
 */

public class ViewContentTypeDefinitionAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewContentTypeDefinitionAction.class.getName());

	private static final long serialVersionUID = 1L;

	public static final String USE_EDITOR = "useEditor";

	private static CategoryController categoryController = CategoryController.getController();

    private ContentTypeDefinitionVO contentTypeDefinitionVO;
	private String currentContentTypeEditorViewLanguageCode;
    private List<ContentTypeAttribute> attributes = null;
    private ContentTypeAttribute attribute = null;
    private List availableLanguages = null;
    private List languageVOList;
    private String title;
    private String inputTypeId;
    private String attributeName;
    private String newAttributeName;
	private String attributeParameterId;
	private String attributeParameterValueId;
	private String newAttributeParameterValueId;
	private String attributeParameterValueLabel;
	private String attributeParameterValueLocale;
	private String attributeToExpand;
	private String assetKey;
	private String newAssetKey;
	private String categoryKey;
	private String newCategoryKey;
	
	private Boolean isMandatory = new Boolean(false);
	private String description = "";
	private Integer maximumSize;
	private String allowedContentTypes = "any";
	private String imageWidth;
	private String imageHeight;
	private String assetUploadTransformationsSettings = "";
	
	private List activatedName = new ArrayList();
	private Integer tabToActivate = -1;

    public ViewContentTypeDefinitionAction()
    {
        this(new ContentTypeDefinitionVO());
    }

    public ViewContentTypeDefinitionAction(ContentTypeDefinitionVO contentTypeDefinitionVO)
    {
        this.contentTypeDefinitionVO = contentTypeDefinitionVO;
    }

    protected void initialize(Integer contentTypeDefinitionId) throws Exception
    {
        this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentTypeDefinitionId);
    	//logger.info("Initializing:" + this.contentTypeDefinitionVO.getSchemaValue());

		this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().validateAndUpdateContentType(this.contentTypeDefinitionVO);
        this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentTypeDefinitionId);
		this.attributes = ContentTypeDefinitionController.getController().getContentTypeAttributes(this.contentTypeDefinitionVO, true);
		this.availableLanguages = LanguageController.getController().getLanguageVOList();
    }

    /**
     * The main method that fetches the Value-object for this use-case
     */

    public String doExecute() throws Exception
    {
        this.initialize(getContentTypeDefinitionId());
        return USE_EDITOR;
    }

	/**
	 * The method that initializes all for the editor mode
	 */

	public String doUseEditor() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}

	/**
	 * The method that initializes all for the simple mode
	 */

	public String doUseSimple() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());
		return SUCCESS;
	}

	public String doInsertAttribute() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());
		String newSchemaValue = ContentTypeDefinitionController.getController().insertContentTypeAttribute(this.contentTypeDefinitionVO.getSchemaValue(), this.inputTypeId, this.activatedName);
		this.contentTypeDefinitionVO.setSchemaValue(newSchemaValue);
		ContentTypeDefinitionController.getController().update(this.contentTypeDefinitionVO);

		this.initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}

	public String doViewAttribute() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());
		if(this.attributeName == null || this.attributeName.equals(""))
			throw new Exception("Must supply parameter 'attributeName'");

		for(ContentTypeAttribute attribute : this.attributes)
		{
			if(attribute.getName().equals(this.attributeName))
				this.attribute = attribute;
		}
		
		return "successViewAttribute";
	}


	public String doDeleteAttribute() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());

		try
		{
			Document document = createDocumentFromDefinition();

			String attributesXPath = "/xs:schema/xs:complexType/xs:all/xs:element/xs:complexType/xs:all/xs:element[@name='" + this.attributeName + "']";
			NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), attributesXPath);
			if(anl != null && anl.getLength() > 0)
			{
				Element element = (Element)anl.item(0);
				element.getParentNode().removeChild(element);
			}

		    String validatorsXPath = "/xs:schema/xs:complexType[@name = 'Validation']/xs:annotation/xs:appinfo/form-validation/formset/form/field[@property = '" + attributeName + "']";
		    NodeList anl2 = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), validatorsXPath);
			for(int i=0; i<anl2.getLength(); i++)
			{
				Element element = (Element)anl2.item(i);
				element.getParentNode().removeChild(element);
			}

			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		this.initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}

	/**
	 * This method moves an content type attribute up one step.
	 */

	public String doMoveAttributeUp() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());

		try
		{
			Document document = createDocumentFromDefinition();

			String attributesXPath = "/xs:schema/xs:complexType/xs:all/xs:element/xs:complexType/xs:all/xs:element";
			NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), attributesXPath);
			Element previousElement = null;
			for(int i=0; i < anl.getLength(); i++)
			{
				Element element = (Element)anl.item(i);
				if(element.getAttribute("name").equalsIgnoreCase(this.attributeName) && previousElement != null)
				{
					Element parent = (Element)element.getParentNode();
					parent.removeChild(element);
					parent.insertBefore(element, previousElement);
				}
				previousElement = element;
			}

			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		this.initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}


	/**
	 * This method moves an content type attribute down one step.
	 */

	public String doMoveAttributeDown() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());

		try
		{
			Document document = createDocumentFromDefinition();

			String attributesXPath = "/xs:schema/xs:complexType/xs:all/xs:element/xs:complexType/xs:all/xs:element";
			NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), attributesXPath);
			Element parent = null;
			Element elementToMove = null;
			boolean isInserted = false;
			int position = 0;
			for(int i=0; i < anl.getLength(); i++)
			{
				Element element = (Element)anl.item(i);
				parent = (Element)element.getParentNode();

				if(elementToMove != null)
				{
					if(position == 2)
					{
						parent.insertBefore(elementToMove, element);
						isInserted = true;
						break;
					}
					else
						position++;
				}

				if(element.getAttribute("name").equalsIgnoreCase(this.attributeName))
				{
					elementToMove = element;
					parent.removeChild(elementToMove);
					position++;
				}
			}

			if(!isInserted && elementToMove != null)
				parent.appendChild(elementToMove);

			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		this.initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}

	
	/**
	 * This method moves an content type assetKey up one step.
	 */

	public String doMoveAssetKeyUp() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());

		try
		{
			Document document = createDocumentFromDefinition();

			String attributesXPath = "/xs:schema/xs:simpleType[@name = '" + ContentTypeDefinitionController.ASSET_KEYS + "']/xs:restriction/xs:enumeration[@value='" + this.assetKey + "']";
			NodeList anl = XPathAPI.selectNodeList(document.getDocumentElement(), attributesXPath);
			if(anl != null && anl.getLength() > 0)
			{
				Element element = (Element)anl.item(0);
				Node parentElement = element.getParentNode();
				Node previuosSibling = element.getPreviousSibling();
				if(previuosSibling != null)
				{
				    parentElement.removeChild(element);
				    parentElement.insertBefore(element, previuosSibling);
				}
			}
						
			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		this.initialize(getContentTypeDefinitionId());
		
		return USE_EDITOR;
	}


	/**
	 * This method moves an content type asset key down one step.
	 */

	public String doMoveAssetKeyDown() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());

		try
		{
			Document document = createDocumentFromDefinition();

			String attributesXPath = "/xs:schema/xs:simpleType[@name = '" + ContentTypeDefinitionController.ASSET_KEYS + "']/xs:restriction/xs:enumeration[@value='" + this.assetKey + "']";
			NodeList anl = XPathAPI.selectNodeList(document.getDocumentElement(), attributesXPath);
			if(anl != null && anl.getLength() > 0)
			{
				Element element = (Element)anl.item(0);
				Node parentElement = element.getParentNode();
				Node nextSibling = element.getNextSibling();
				if(nextSibling != null)
				{
			        parentElement.removeChild(nextSibling);
			        parentElement.insertBefore(nextSibling, element);
				}
			}

			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		this.initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}


	public String doDeleteAttributeParameterValue() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());

		try
		{
			Document document = createDocumentFromDefinition();

			String attributesXPath = "/xs:schema/xs:complexType/xs:all/xs:element/xs:complexType/xs:all/xs:element[@name='" + this.attributeName + "']/xs:annotation/xs:appinfo/params/param[@id='" + this.attributeParameterId +"']/values/value[@id='" + this.attributeParameterValueId + "']";
			NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), attributesXPath);
			if(anl != null && anl.getLength() > 0)
			{
				Element element = (Element)anl.item(0);
				element.getParentNode().removeChild(element);
			}

			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		this.initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}

	public String doInsertAttributeParameterValue() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());

		try
		{
			Document document = createDocumentFromDefinition();

			String attributesXPath = "/xs:schema/xs:complexType/xs:all/xs:element/xs:complexType/xs:all/xs:element[@name='" + this.attributeName + "']/xs:annotation/xs:appinfo/params/param[@id='" + this.attributeParameterId +"']/values";
			NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), attributesXPath);
			if(anl != null && anl.getLength() > 0)
			{
				Element element = (Element)anl.item(0);
				Element newValue = document.createElement("value");
				newValue.setAttribute("id", getRandomName());
				newValue.setAttribute("label", getRandomName());
				element.appendChild(newValue);
			}

			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		this.initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}

	/**
	 * We validate that ' ', '.', ''', '"' is not used in the attribute name as that will break the javascripts later.
	 */

	public String doUpdateAttribute() throws Exception
	{
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		if(this.newAttributeName.indexOf(" ") > -1 || this.newAttributeName.indexOf(".") > -1 || this.newAttributeName.indexOf("'") > -1  || this.newAttributeName.indexOf("\"") > -1)
		{
			ceb.add(new ConstraintException("ContentTypeAttribute.updateAction", "3500"));
		}

		ceb.throwIfNotEmpty();


		this.initialize(getContentTypeDefinitionId());

		try
		{
			Document document = createDocumentFromDefinition();

			//Updating the content attribute
			String[] extraParameterNames = getRequest().getParameterValues("parameterNames");
			if(extraParameterNames != null)
			{
				for(int i=0; i < extraParameterNames.length; i++)
				{
					String extraParameterName = extraParameterNames[i];
					String value = getRequest().getParameter(extraParameterName);

					String extraParametersXPath = "/xs:schema/xs:complexType/xs:all/xs:element/xs:complexType/xs:all/xs:element[@name='" + this.attributeName + "']/xs:annotation/xs:appinfo/params/param[@id='" + extraParameterName +"']/values/value";
					NodeList extraParamsNodeList = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), extraParametersXPath);
					if(extraParamsNodeList != null && extraParamsNodeList.getLength() > 0)
					{
						Element element = (Element)extraParamsNodeList.item(0);

						if(extraParameterName.equalsIgnoreCase("values") && (this.inputTypeId.equalsIgnoreCase("select") || this.inputTypeId.equalsIgnoreCase("checkbox") || this.inputTypeId.equalsIgnoreCase("radiobutton")))
						{
							((Element)element.getParentNode().getParentNode()).setAttribute("inputTypeId", "1");
						}
						else if(extraParameterName.equalsIgnoreCase("Markup") && this.inputTypeId.equalsIgnoreCase("customfield"))
						{
							((Element)element.getParentNode().getParentNode()).setAttribute("inputTypeId", "2");
						}
						else
						{
							((Element)element.getParentNode().getParentNode()).setAttribute("inputTypeId", "0");
						}

						String inputTypeId = ((Element)element.getParentNode().getParentNode()).getAttribute("inputTypeId");
						if(inputTypeId.equals("0") || inputTypeId.equals("2"))
						{
							if(this.currentContentTypeEditorViewLanguageCode != null && this.currentContentTypeEditorViewLanguageCode.length() > 0)
							{
								element.setAttribute("label_" + this.currentContentTypeEditorViewLanguageCode, value);
							}
							else
							{
								element.setAttribute("label", value);
							}
						}
					}
				}
			}

			//Updating the name and type
			String attributeXPath = "/xs:schema/xs:complexType/xs:all/xs:element/xs:complexType/xs:all/xs:element[@name='" + this.attributeName + "']";
			NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), attributeXPath);
			if(anl != null && anl.getLength() > 0)
			{
				Element element = (Element)anl.item(0);
				element.setAttribute("name", this.newAttributeName);
				element.setAttribute("type", this.inputTypeId);
			}

			try
			{
				//Updating the validation part
				String validationXPath = "//xs:complexType[@name='Validation']/xs:annotation/xs:appinfo/form-validation/formset/form/field[@property='" + this.attributeName + "']";
				NodeList fieldNodeList = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), validationXPath);
				if(fieldNodeList != null && fieldNodeList.getLength() > 0)
				{
					Element element = (Element)fieldNodeList.item(0);
					element.setAttribute("property", this.newAttributeName);
				}

				//Updating the dependent part
				String validationDependentXPath = "//xs:complexType[@name='Validation']/xs:annotation/xs:appinfo/form-validation/formset/form/field[@depends='requiredif']/var/var-value";
				NodeList requiredIfValueNodeList = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), validationDependentXPath);
				if(requiredIfValueNodeList != null && requiredIfValueNodeList.getLength() > 0)
				{
				    for(int i=0; i<requiredIfValueNodeList.getLength(); i++)
				    {
				        Element element = (Element)requiredIfValueNodeList.item(0);
				        if(element.getFirstChild() != null && element.getFirstChild().getNodeValue() != null && element.getFirstChild().getNodeValue().equals(this.attributeName))
				            element.getFirstChild().setNodeValue(this.newAttributeName);
				    }
				}
			}
			catch(Exception ve)
			{
			    ve.printStackTrace();
			}
			
			this.attributeToExpand = this.newAttributeName;
			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		this.initialize(getContentTypeDefinitionId());

		return USE_EDITOR;
	}


	public String doUpdateAttributeParameterValue() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());

		try
		{
			Document document = createDocumentFromDefinition();

			String parameterValueXPath = "/xs:schema/xs:complexType/xs:all/xs:element/xs:complexType/xs:all/xs:element[@name='" + this.attributeName + "']/xs:annotation/xs:appinfo/params/param[@id='" + this.attributeParameterId +"']/values/value[@id='" + this.attributeParameterValueId + "']";
			NodeList parameterValuesNodeList = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), parameterValueXPath);
			if(parameterValuesNodeList != null && parameterValuesNodeList.getLength() > 0)
			{
				Element element = (Element)parameterValuesNodeList.item(0);
				element.setAttribute("id", this.newAttributeParameterValueId);

				logger.info("currentContentTypeEditorViewLanguageCode:" + currentContentTypeEditorViewLanguageCode);
				logger.info("attributeParameterValueLabel:" + attributeParameterValueLabel);
				if(this.currentContentTypeEditorViewLanguageCode != null && this.currentContentTypeEditorViewLanguageCode.length() > 0)
					element.setAttribute("label_" + this.currentContentTypeEditorViewLanguageCode, this.attributeParameterValueLabel);
				else
					element.setAttribute("label", this.attributeParameterValueLabel);
			}

			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		this.initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}


	public String doInsertAttributeValidator() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());

		try
		{
			Document document = createDocumentFromDefinition();

			String validatorName = this.getRequest().getParameter("validatorName");
			
			if(validatorName != null && !validatorName.equalsIgnoreCase(""))
			{
			    String validatorsXPath = "/xs:schema/xs:complexType[@name = 'Validation']/xs:annotation/xs:appinfo/form-validation/formset/form";
				Node formNode = org.apache.xpath.XPathAPI.selectSingleNode(document.getDocumentElement(), validatorsXPath);
				if(formNode != null)
				{
					Element element = (Element)formNode;
					
					Element newField = document.createElement("field");
					newField.setAttribute("property", this.attributeName);
					newField.setAttribute("depends", validatorName);

					String errorKey = null;
					
					if(validatorName.equals("required"))
					{
					    errorKey = "300"; //Required
					}
					else if(validatorName.equals("requiredif"))
					{
					    errorKey = "300"; //Required

						Element newVar = document.createElement("var");
					    Element varNameElement = createTextElement(document, "var-name", "dependent");
						Element varValueElement = createTextElement(document, "var-value", "AttributeName");
						newVar.appendChild(varNameElement);
						newVar.appendChild(varValueElement);
						newField.appendChild(newVar);
					}
					else if(validatorName.equals("matchRegexp"))
					{
					    errorKey = "307"; //Invalid format

						Element newVar = document.createElement("var");
					    Element varNameElement = createTextElement(document, "var-name", "regexp");
						Element varValueElement = createTextElement(document, "var-value", ".*");
						newVar.appendChild(varNameElement);
						newVar.appendChild(varValueElement);
						newField.appendChild(newVar);
					}

					Element newMessage = document.createElement("msg");
					newMessage.setAttribute("name", validatorName);
					newMessage.setAttribute("key", errorKey);
					newField.appendChild(newMessage);
					
					element.appendChild(newField);
				}
			}
			
			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		this.initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}

	public String doUpdateAttributeValidatorArguments() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());

		try
		{
			Document document = createDocumentFromDefinition();

			int i = 0;
			String attributeValidatorName = this.getRequest().getParameter("attributeValidatorName");
			String argumentName = this.getRequest().getParameter(i + "_argumentName");
			
			while(argumentName != null && !argumentName.equalsIgnoreCase(""))
			{
			    String argumentValue = this.getRequest().getParameter(i + "_argumentValue");
			    
			    String validatorsXPath = "/xs:schema/xs:complexType[@name = 'Validation']/xs:annotation/xs:appinfo/form-validation/formset/form/field[@property = '" + attributeName + "'][@depends = '" + attributeValidatorName + "']";
				Node fieldNode = org.apache.xpath.XPathAPI.selectSingleNode(document.getDocumentElement(), validatorsXPath);
				if(fieldNode != null)
				{
					Element element = (Element)fieldNode;
					NodeList nl = element.getElementsByTagName("var");
					for(int nlIndex=0; nlIndex < nl.getLength(); nlIndex++)
					{
					    Node node = (Node)nl.item(nlIndex);
					    element.removeChild(node);
					}
					
					Element newVar = document.createElement("var");
					
					Element varNameElement = createTextElement(document, "var-name", argumentName);
					Element varValueElement = createTextElement(document, "var-value", argumentValue);
					newVar.appendChild(varNameElement);
					newVar.appendChild(varValueElement);
					
					element.appendChild(newVar);
				}
				
				i++;
				argumentName = this.getRequest().getParameter(i + "_argumentName");
			}
			
			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		this.initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}

	public String doDeleteAttributeValidator() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());

		try
		{
			Document document = createDocumentFromDefinition();

			int i = 0;
			String attributeValidatorName = this.getRequest().getParameter("attributeValidatorName");
			if(attributeValidatorName != null && !attributeValidatorName.equalsIgnoreCase(""))
			{
			    String validatorsXPath = "/xs:schema/xs:complexType[@name = 'Validation']/xs:annotation/xs:appinfo/form-validation/formset/form/field[@property = '" + attributeName + "'][@depends = '" + attributeValidatorName + "']";
				Node fieldNode = org.apache.xpath.XPathAPI.selectSingleNode(document.getDocumentElement(), validatorsXPath);
				if(fieldNode != null)
				{
					Element element = (Element)fieldNode;
					element.getParentNode().removeChild(element);
				}
			}
			
			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		this.initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}

	
	//-------------------------------------------------------------------------------------
	// Methods dealing with extra keys
	//
	// TODO: I think it makes sense to move all methods dealing with ContentTypeDefinition XML
	// TODO: into the ContentTypeDefinitionController, that way only ONE class knows about the
	// TODO: XML structure and we can make adequate tests
	//-------------------------------------------------------------------------------------
	/**
	 * Gets the list of defined assetKeys.
	 */
	public List getDefinedAssetKeys()
	{
		return ContentTypeDefinitionController.getController().getDefinedAssetKeys(contentTypeDefinitionVO, true);
	}

	/**
	 * Gets the list of defined categoryKeys, also populate the category name for the UI.
	 */
	public List getDefinedCategoryKeys() throws Exception
	{
		List categoryKeys = ContentTypeDefinitionController.getController().getDefinedCategoryKeys(contentTypeDefinitionVO, true);
		for (Iterator iter = categoryKeys.iterator(); iter.hasNext();)
		{
			CategoryAttribute info = (CategoryAttribute) iter.next();
			if(info.getCategoryId() != null)
				info.setCategoryName(getCategoryName(info.getCategoryId()));
			else
				info.setCategoryName("Undefined");
		}
		return categoryKeys;
	}

	/**
	 * Return the Category name, if we cannot find the category name (id not an int, bad id, etc)
	 * then do not barf, but return a user friendly name. This can happen if someone removes a
	 * category that is references by a content type definition.
	 */
	public String getCategoryName(Integer id)
	{
		try
		{
			return categoryController.findById(id).getName();
		}
		catch(SystemException e)
		{
			return "Category not found";
		}
	}

	public String doInsertAssetKey() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());

		try
		{
			Document document = createDocumentFromDefinition();
			Element enumeration = ContentTypeDefinitionController.getController().createNewEnumerationKey(document, ContentTypeDefinitionController.ASSET_KEYS);
			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			logger.warn("Error adding asset key: ", e);
		}

		this.initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}

	public String doInsertCategoryKey() throws Exception
	{
		this.initialize(getContentTypeDefinitionId());

		try
		{
			Document document = createDocumentFromDefinition();
			Element enumeration = ContentTypeDefinitionController.getController().createNewEnumerationKey(document, ContentTypeDefinitionController.CATEGORY_KEYS);

			Element annotation = document.createElement("xs:annotation");
			Element appinfo = document.createElement("xs:appinfo");
			Element params = document.createElement("params");

			enumeration.appendChild(annotation);
			annotation.appendChild(appinfo);
			appinfo.appendChild(params);
			params.appendChild(createTextElement(document, "title", getRandomName()));
			params.appendChild(createTextElement(document, "description", getRandomName()));
			params.appendChild(createTextElement(document, "categoryId", ""));

			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			logger.warn("Error adding categories key: ", e);
		}

		this.initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}



	public String doUpdateAssetKey() throws Exception
	{
		initialize(getContentTypeDefinitionId());
		try
		{
			Document document = createDocumentFromDefinition();
			updateAssetEnumerationKey(document, ContentTypeDefinitionController.ASSET_KEYS, getAssetKey(), getNewAssetKey(), this.isMandatory, this.description, this.maximumSize, this.allowedContentTypes, this.imageWidth, this.imageHeight, this.assetUploadTransformationsSettings);
			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			logger.warn("Error updating asset key: ", e);
		}

		initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}

	public String doUpdateCategoryKey() throws Exception
	{
		initialize(getContentTypeDefinitionId());

		try
		{
			Document document = createDocumentFromDefinition();
			Element enumeration = updateEnumerationKey(document, ContentTypeDefinitionController.CATEGORY_KEYS, getCategoryKey(), getNewCategoryKey());

			if(enumeration != null)
			{
				Element title = (Element)XPathAPI.selectSingleNode(enumeration, "xs:annotation/xs:appinfo/params/title");
				setTextElement(title, getSingleParameter("title"));

				Element description = (Element)XPathAPI.selectSingleNode(enumeration, "xs:annotation/xs:appinfo/params/description");
				setTextElement(description, getSingleParameter("description"));

				Element categoryId = (Element)XPathAPI.selectSingleNode(enumeration, "xs:annotation/xs:appinfo/params/categoryId");
				setTextElement(categoryId, getSingleParameter("categoryId"));
			}

			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			logger.warn("Error updating category key: ", e);
		}

		initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}

	public String doDeleteAssetKey() throws Exception
	{
		return deleteKey(ContentTypeDefinitionController.ASSET_KEYS, getAssetKey());
	}

	public String doDeleteCategoryKey() throws Exception
	{
		return deleteKey(ContentTypeDefinitionController.CATEGORY_KEYS, getCategoryKey());
	}

	private String deleteKey(String keyType, String key) throws Exception
	{
		this.initialize(getContentTypeDefinitionId());

		try
		{
			Document document = createDocumentFromDefinition();

			String attributesXPath = "/xs:schema/xs:simpleType[@name = '" + keyType + "']/xs:restriction/xs:enumeration[@value='" + key + "']";
			NodeList anl = XPathAPI.selectNodeList(document.getDocumentElement(), attributesXPath);
			if(anl != null && anl.getLength() > 0)
			{
				Element element = (Element)anl.item(0);
				element.getParentNode().removeChild(element);
			}

			saveUpdatedDefinition(document);
		}
		catch(Exception e)
		{
			logger.warn("Error updating key: " + keyType, e);
		}

		this.initialize(getContentTypeDefinitionId());
		return USE_EDITOR;
	}

	/**
	 * Returns the CategoryController, used by the ContentTypeDefinitionEditor
	 */
	public CategoryController getCategoryController()
	{
		return categoryController;
	}

 	/**
	 * Gets the list of all system categories.
	 */
	public List getAllCategories() throws SystemException
	{
		return getCategoryController().findAllActiveCategories();
	}

	//-------------------------------------------------------------------------------------
	// XML Helper Methods
	//-------------------------------------------------------------------------------------
	/**
	 * Consolidate the Document creation
	 */
	private Document createDocumentFromDefinition() throws SAXException, IOException
	{
		String contentTypeDefinitionString = this.contentTypeDefinitionVO.getSchemaValue();
		InputSource xmlSource = new InputSource(new StringReader(contentTypeDefinitionString));
		DOMParser parser = new DOMParser();
		parser.parse(xmlSource);
		return parser.getDocument();
	}

	/**
	 * Consolidate the update of a ContentTypeDefinition Document to the persistence mechanism
	 */
	private void saveUpdatedDefinition(Document document) throws ConstraintException, SystemException
	{
		StringBuffer sb = new StringBuffer();
		XMLHelper.serializeDom(document.getDocumentElement(), sb);
		this.contentTypeDefinitionVO.setSchemaValue(sb.toString());
		ContentTypeDefinitionController.getController().update(contentTypeDefinitionVO);
	}



	/**
	 * Find an <xs:enumeration> element and update the key value.
	 * @return The Element if child changes are needed, null if the element is not found
	 */
	private Element updateEnumerationKey(Document document, String keyType, String oldKey, String newKey) throws TransformerException
	{
		String attributesXPath = "/xs:schema/xs:simpleType[@name = '" + keyType + "']/xs:restriction/xs:enumeration[@value='" + oldKey + "']";
		NodeList anl = XPathAPI.selectNodeList(document.getDocumentElement(), attributesXPath);
		if(anl != null && anl.getLength() > 0)
		{
			Element enumeration = (Element)anl.item(0);
			enumeration.setAttribute("value", newKey);
			return enumeration;
		}

		return null;
	}

	/**
	 * Find an <xs:enumeration> element and update the key value.
	 * @return The Element if child changes are needed, null if the element is not found
	 */
	private Element updateAssetEnumerationKey(Document document, String keyType, String oldKey, String newKey, Boolean isMandatory, String description, Integer maximumSize, String allowedContentTypes, String imageWidth, String imageHeight, String assetUploadTransformationsSettings) throws TransformerException
	{
		if(isMandatory == null)
			isMandatory = new Boolean(false);
		if(description == null)
	        description = "Undefined";
	    if(maximumSize == null)
	        maximumSize = new Integer(100);
	    if(allowedContentTypes == null)
	        allowedContentTypes = "*";
	    if(imageWidth == null)
	        imageWidth = "*";
	    if(imageHeight == null)
	        imageHeight = "*";
	    
	    
		String attributesXPath = "/xs:schema/xs:simpleType[@name = '" + keyType + "']/xs:restriction/xs:enumeration[@value='" + oldKey + "']";
		NodeList anl = XPathAPI.selectNodeList(document.getDocumentElement(), attributesXPath);
		if(anl != null && anl.getLength() > 0)
		{
			Element enumeration = (Element)anl.item(0);
			enumeration.setAttribute("value", newKey);
			
			Element isMandatoryElement = (Element)XPathAPI.selectSingleNode(enumeration, "xs:annotation/xs:appinfo/params/isMandatory");
			Element descriptionElement = (Element)XPathAPI.selectSingleNode(enumeration, "xs:annotation/xs:appinfo/params/description");
			Element maximumSizeElement = (Element)XPathAPI.selectSingleNode(enumeration, "xs:annotation/xs:appinfo/params/maximumSize");
			Element allowedContentTypesElement = (Element)XPathAPI.selectSingleNode(enumeration, "xs:annotation/xs:appinfo/params/allowedContentTypes");
			Element imageWidthElement = (Element)XPathAPI.selectSingleNode(enumeration, "xs:annotation/xs:appinfo/params/imageWidth");
			Element imageHeightElement = (Element)XPathAPI.selectSingleNode(enumeration, "xs:annotation/xs:appinfo/params/imageHeight");
			Element assetUploadTransformationsSettingsElement = (Element)XPathAPI.selectSingleNode(enumeration, "xs:annotation/xs:appinfo/params/assetUploadTransformationsSettings");

			if(isMandatoryElement == null && descriptionElement != null)
			{
				isMandatoryElement = createTextElement(document, "isMandatory", isMandatory.toString());
				
				descriptionElement.getParentNode().appendChild(isMandatoryElement);
			}

			if(assetUploadTransformationsSettingsElement == null && descriptionElement != null)
			{
				assetUploadTransformationsSettingsElement = createTextElement(document, "assetUploadTransformationsSettings", assetUploadTransformationsSettings.toString());
				
				descriptionElement.getParentNode().appendChild(assetUploadTransformationsSettingsElement);
			}

			if(descriptionElement == null)
			{
				Element annotation = document.createElement("xs:annotation");
				Element appinfo = document.createElement("xs:appinfo");
				Element params = document.createElement("params");
	
				enumeration.appendChild(annotation);
				annotation.appendChild(appinfo);
				appinfo.appendChild(params);
				
				descriptionElement = createTextElement(document, "description", getRandomName());
				maximumSizeElement = createTextElement(document, "maximumSize", maximumSize.toString());
				allowedContentTypesElement = createTextElement(document, "allowedContentTypes", allowedContentTypes);
			    imageWidthElement = createTextElement(document, "imageWidth", imageWidth);
			    imageHeightElement = createTextElement(document, "imageHeight", imageHeight);
				isMandatoryElement = createTextElement(document, "isMandatory", isMandatory.toString());
				assetUploadTransformationsSettingsElement = createTextElement(document, "assetUploadTransformationsSettings", assetUploadTransformationsSettings.toString());
				
				params.appendChild(descriptionElement);
				params.appendChild(maximumSizeElement);
				params.appendChild(allowedContentTypesElement);
				params.appendChild(imageWidthElement);
				params.appendChild(imageHeightElement);
				params.appendChild(isMandatoryElement);
			}
			else
			{
				setTextElement(isMandatoryElement, isMandatory.toString());
				setTextElement(descriptionElement, description);
				setTextElement(maximumSizeElement, maximumSize.toString());
				setTextElement(allowedContentTypesElement, allowedContentTypes);
				setTextElement(imageWidthElement, imageWidth);
				setTextElement(imageHeightElement, imageHeight);
				setTextElement(assetUploadTransformationsSettingsElement, assetUploadTransformationsSettings);
			}
			
			return enumeration;
		}

		return null;
	}
	
	/**
	 * Creates a new text element
	 */
	private Element createTextElement(Document document, String tagName, String value)
	{
		Element e = document.createElement(tagName);
		e.appendChild(document.createTextNode(value));
		return e;
	}

	/**
	 * Updates the text child of an element, creating it if it needs to.
	 */
	private void setTextElement(Element e, String value)
	{
		if(e.getFirstChild() != null)
			e.getFirstChild().setNodeValue(value);
		else
			 e.appendChild(e.getOwnerDocument().createTextNode(value));
	}


	/**
	 * Generates a random name
	 */
	private String getRandomName()
	{
		return "undefined" + (int)(Math.random() * 100);
	}


	//-------------------------------------------------------------------------------------
	// Attribute Accessors
	//-------------------------------------------------------------------------------------
    public Integer getContentTypeDefinitionId()
    {
        return this.contentTypeDefinitionVO.getContentTypeDefinitionId();
    }

    public void setContentTypeDefinitionId(Integer contentTypeDefinitionId) throws Exception
    {
        this.contentTypeDefinitionVO.setContentTypeDefinitionId(contentTypeDefinitionId);
    }

	public String getTitle()
	{
		return this.title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

    public String getName()
    {
        return this.contentTypeDefinitionVO.getName();
    }

    public String getSchemaValue()
    {
        return this.contentTypeDefinitionVO.getSchemaValue();
    }

	public Integer getType()
	{
		return this.contentTypeDefinitionVO.getType();
	}

	public String getDetailPageResolverClass()
	{
		return this.contentTypeDefinitionVO.getDetailPageResolverClass();
	}

	public String getDetailPageResolverData()
	{
		return this.contentTypeDefinitionVO.getDetailPageResolverData();
	}

	public Integer getParentId()
	{
		return this.contentTypeDefinitionVO.getParentId();
	}

	public void setParentContentTypeDefinitionId(Integer parentContentTypeDefinitionId)
	{
		this.contentTypeDefinitionVO.setParentId(parentContentTypeDefinitionId);
	}

	/**
	 * This method returns the attributes in the content type definition for generation.
	 */

	public List<ContentTypeAttribute> getContentTypeAttributes()
	{
		return this.attributes;
	}

	public ContentTypeAttribute getAttribute()
	{
		return this.attribute;
	}

	public String getInputTypeId()
	{
		return inputTypeId;
	}

	public void setInputTypeId(String string)
	{
		inputTypeId = string;
	}

	public String getAttributeName()
	{
		return this.attributeName;
	}

	public void setAttributeName(String attributeName)
	{
		this.attributeName = attributeName;
	}

	public void setNewAttributeName(String newAttributeName)
	{
		this.newAttributeName = newAttributeName;
	}

	public String getAttributeParameterValueId()
	{
		return attributeParameterValueId;
	}

	public void setAttributeParameterValueId(String string)
	{
		attributeParameterValueId = string;
	}

	public String getAttributeParameterId()
	{
		return attributeParameterId;
	}

	public void setAttributeParameterId(String string)
	{
		attributeParameterId = string;
	}

	public String getAttributeParameterValueLabel()
	{
		return attributeParameterValueLabel;
	}

	public String getNewAttributeParameterValueId()
	{
		return newAttributeParameterValueId;
	}

	public void setAttributeParameterValueLabel(String string)
	{
		attributeParameterValueLabel = string;
	}

	public void setNewAttributeParameterValueId(String string)
	{
		newAttributeParameterValueId = string;
	}

	public String getAttributeParameterValueLocale()
	{
		return attributeParameterValueLocale;
	}

	public void setAttributeParameterValueLocale(String string)
	{
		attributeParameterValueLocale = string;
	}

	public String getAttributeToExpand()
	{
		return attributeToExpand;
	}

	public void setAttributeToExpand(String string)
	{
		attributeToExpand = string;
	}

	public String getCurrentContentTypeEditorViewLanguageCode()
	{
		return currentContentTypeEditorViewLanguageCode;
	}

	public void setCurrentContentTypeEditorViewLanguageCode(String string)
	{
		currentContentTypeEditorViewLanguageCode = string;
	}

	public List getAvailableLanguages()
	{
		return availableLanguages;
	}

	public String getAssetKey()			{ return assetKey; }
	public void setAssetKey(String s)	{ assetKey = s; }

	public String getNewAssetKey()			{ return newAssetKey; }
	public void setNewAssetKey(String s)	{ newAssetKey = s; }

	public String getCategoryKey()			{ return categoryKey; }
	public void setCategoryKey(String s)	{ categoryKey = s; }

	public String getNewCategoryKey()		{ return newCategoryKey; }
	public void setNewCategoryKey(String s)	{ newCategoryKey = s; }

	public String getErrorKey()
	{
		return "ContentTypeAttribute.updateAction";
	}

	public String getReturnAddress()
	{
		return "ViewListContentTypeDefinition.action";
	}

    public String getAllowedContentTypes()
    {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(String assetContentType)
    {
        this.allowedContentTypes = assetContentType;
    }
    
    public String getImageHeight()
    {
        return imageHeight;
    }
    
    public void setImageHeight(String imageHeight)
    {
        this.imageHeight = imageHeight;
    }
    
    public String getImageWidth()
    {
        return imageWidth;
    }
    
    public void setImageWidth(String imageWidth)
    {
        this.imageWidth = imageWidth;
    }
    
    public Integer getMaximumSize()
    {
        return maximumSize;
    }
    
    public void setMaximumSize(Integer maximumSize)
    {
        this.maximumSize = maximumSize;
    }
    
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    public List getActivatedName()
    {
        return activatedName;
    }

	public Boolean getIsMandatory()
	{
		return isMandatory;
	}

	public void setIsMandatory(Boolean isMandatory)
	{
		this.isMandatory = isMandatory;
	}
	
    public String getAssetUploadTransformationsSettings()
	{
		return assetUploadTransformationsSettings;
	}
    
	public void setAssetUploadTransformationsSettings(String assetUploadTransformationsSettings)
	{
		this.assetUploadTransformationsSettings = assetUploadTransformationsSettings;
	}
	
	public Integer getTabToActivate()
	{
		return tabToActivate;
	}
    
	public void setTabToActivate(Integer tabToActivate)
	{
		this.tabToActivate = tabToActivate;
	}
	
	public List<ContentTypeDefinitionVO> getContentTypeDefinitions() throws Exception
	{
		return ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList(ContentTypeDefinitionVO.CONTENT);
	}

	public List<ContentDetailPageResolver> getContentPageResolvers()
	{
		return ContentDetailPageResolversService.getService().geContentDetailPageResolvers();
	}
}
