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

package org.infoglue.cms.applications.contenttool.actions;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.io.FileHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * This class implements the action class for the new Form Editor.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewFormEditorAction extends InfoGlueAbstractAction //extends ViewContentTypeDefinitionAction
{ 
    private final static Logger logger = Logger.getLogger(ViewFormEditorAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private Integer contentVersionId;
	private String contentVersionAttributeName;
	private String attributeName;
	private String formDefinition;
	private List attributes;
	
	private String inputTypeId;
	private String newAttributeParameterValueId;
	private String currentContentTypeEditorViewLanguageCode;
	private String attributeParameterValueLabel;
	private String attributeParameterId;
	private String attributeParameterValueId;
	private String newAttributeName;
	private String attributeParameterValueLocale;
	private String attributeToExpand;

	private List availableLanguages = null;

	
	public ViewFormEditorAction()
	{
	}
        
	protected void initialize() throws Exception
	{
		this.formDefinition = ContentVersionController.getContentVersionController().getAttributeValue(getContentVersionId(), getContentVersionAttributeName(), false);
		logger.info("this.formDefinition:" + this.formDefinition);
		
		boolean isFormDefinitionValid = true;
		try
		{
			InputSource xmlSource = new InputSource(new StringReader(this.formDefinition));
			
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			Document document = parser.getDocument();
		}
		catch(Exception e)
		{
			isFormDefinitionValid = false;
		}
		
		if(this.formDefinition == null || this.formDefinition.equals("") || isFormDefinitionValid == false)
		{
			logger.info("Trying to get the default definition...");
			String schemaValue = "";
			try
			{
				String newFormDefinition = FileHelper.getStreamAsString(this.getClass().getResourceAsStream("/org/infoglue/cms/applications/defaultContentTypeDefinition.xml"));
				ContentVersionController.getContentVersionController().updateAttributeValue(getContentVersionId(), getContentVersionAttributeName(), newFormDefinition, this.getInfoGluePrincipal());		
				this.formDefinition = ContentVersionController.getContentVersionController().getAttributeValue(getContentVersionId(), getContentVersionAttributeName(), false);
			}
			catch(Exception e)
			{
				logger.error("The system could not find the default content type definition:" + e.getMessage(), e);
			}
		}
		
		this.attributes = ContentTypeDefinitionController.getController().getContentTypeAttributes(formDefinition);
		this.availableLanguages = LanguageController.getController().getLanguageVOList();
	} 

	/**
	 * The main method that just initializes the editor with the correct contentVersion and attribute.
	 */
    
	public String doExecute() throws Exception
	{
		this.initialize();
		return "success";
	}
            
	public String doInsertAttribute() throws Exception
	{
		this.initialize();
		String newFormDefinition = ContentTypeDefinitionController.getController().insertContentTypeAttribute(this.formDefinition, this.inputTypeId, new ArrayList());
		ContentVersionController.getContentVersionController().updateAttributeValue(getContentVersionId(), getContentVersionAttributeName(), newFormDefinition, this.getInfoGluePrincipal());		
				
		this.initialize();
		
		return "success";
	}
	
	
	public String doDeleteAttribute() throws Exception
	{
		this.initialize();
		
		try
		{
			InputSource xmlSource = new InputSource(new StringReader(this.formDefinition));
			
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			Document document = parser.getDocument();
			
			String attributesXPath = "/xs:schema/xs:complexType/xs:all/xs:element/xs:complexType/xs:all/xs:element[@name='" + this.attributeName + "']";
			NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), attributesXPath);
			if(anl != null && anl.getLength() > 0)
			{
				Element element = (Element)anl.item(0);
				element.getParentNode().removeChild(element);
			}
			
			StringBuffer sb = new StringBuffer();
			org.infoglue.cms.util.XMLHelper.serializeDom(document.getDocumentElement(), sb);
			
			ContentVersionController.getContentVersionController().updateAttributeValue(getContentVersionId(), getContentVersionAttributeName(), sb.toString(), this.getInfoGluePrincipal());			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		this.initialize();
		
		return "success";
	}

	/**
	 * This method moves an content type attribute up one step.
	 */
	
	public String doMoveAttributeUp() throws Exception
	{
		this.initialize();
		
		try
		{
			InputSource xmlSource = new InputSource(new StringReader(this.formDefinition));
			
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			Document document = parser.getDocument();
			
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
			
			StringBuffer sb = new StringBuffer();
			org.infoglue.cms.util.XMLHelper.serializeDom(document.getDocumentElement(), sb);

			ContentVersionController.getContentVersionController().updateAttributeValue(getContentVersionId(), getContentVersionAttributeName(), sb.toString(), this.getInfoGluePrincipal());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		this.initialize();
		return "success";
	}


	/**
	 * This method moves an content type attribute down one step.
	 */
	
	public String doMoveAttributeDown() throws Exception
	{
		this.initialize();
		
		try
		{
			InputSource xmlSource = new InputSource(new StringReader(this.formDefinition));
			
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			Document document = parser.getDocument();
									
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
				
			StringBuffer sb = new StringBuffer();
			org.infoglue.cms.util.XMLHelper.serializeDom(document.getDocumentElement(), sb);

			ContentVersionController.getContentVersionController().updateAttributeValue(getContentVersionId(), getContentVersionAttributeName(), sb.toString(), this.getInfoGluePrincipal());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		this.initialize();

		return "success";
	}


	public String doUpdateAttribute() throws Exception
	{
		this.initialize();
		
		try
		{
			InputSource xmlSource = new InputSource(new StringReader(this.formDefinition));
			
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			Document document = parser.getDocument();
			
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
						else
						{
							((Element)element.getParentNode().getParentNode()).setAttribute("inputTypeId", "0");
						}
						
						if(((Element)element.getParentNode().getParentNode()).getAttribute("inputTypeId").equals("0"))
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
		
			StringBuffer sb = new StringBuffer();
			org.infoglue.cms.util.XMLHelper.serializeDom(document.getDocumentElement(), sb);

			ContentVersionController.getContentVersionController().updateAttributeValue(getContentVersionId(), getContentVersionAttributeName(), sb.toString(), this.getInfoGluePrincipal());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		this.initialize();
		
		return "success";
	}


	public String doInsertAttributeParameterValue() throws Exception
	{
		this.initialize();
		
		try
		{
			InputSource xmlSource = new InputSource(new StringReader(this.formDefinition));
			
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			Document document = parser.getDocument();
			
			String attributesXPath = "/xs:schema/xs:complexType/xs:all/xs:element/xs:complexType/xs:all/xs:element[@name='" + this.attributeName + "']/xs:annotation/xs:appinfo/params/param[@id='" + this.attributeParameterId +"']/values";
			NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), attributesXPath);
			if(anl != null && anl.getLength() > 0)
			{
				Element element = (Element)anl.item(0);
				Element newValue = document.createElement("value");
				newValue.setAttribute("id", "undefined" + (int)(Math.random() * 100));
				newValue.setAttribute("label", "undefined" + (int)(Math.random() * 100));
				element.appendChild(newValue);
			}
			
			StringBuffer sb = new StringBuffer();
			org.infoglue.cms.util.XMLHelper.serializeDom(document.getDocumentElement(), sb);

			ContentVersionController.getContentVersionController().updateAttributeValue(getContentVersionId(), getContentVersionAttributeName(), sb.toString(), this.getInfoGluePrincipal());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		this.initialize();
		
		return "success";
	}


	public String doDeleteAttributeParameterValue() throws Exception
	{
		this.initialize();
		
		try
		{
			InputSource xmlSource = new InputSource(new StringReader(this.formDefinition));
			
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			Document document = parser.getDocument();
			
			String attributesXPath = "/xs:schema/xs:complexType/xs:all/xs:element/xs:complexType/xs:all/xs:element[@name='" + this.attributeName + "']/xs:annotation/xs:appinfo/params/param[@id='" + this.attributeParameterId +"']/values/value[@id='" + this.attributeParameterValueId + "']";
			NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), attributesXPath);
			if(anl != null && anl.getLength() > 0)
			{
				Element element = (Element)anl.item(0);
				element.getParentNode().removeChild(element);
			}
			
			StringBuffer sb = new StringBuffer();
			org.infoglue.cms.util.XMLHelper.serializeDom(document.getDocumentElement(), sb);

			ContentVersionController.getContentVersionController().updateAttributeValue(getContentVersionId(), getContentVersionAttributeName(), sb.toString(), this.getInfoGluePrincipal());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		this.initialize();
		
		return "success";
	}



	public String doUpdateAttributeParameterValue() throws Exception
	{
		this.initialize();
		
		try
		{
			InputSource xmlSource = new InputSource(new StringReader(this.formDefinition));
			
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			Document document = parser.getDocument();
			
			String parameterValueXPath = "/xs:schema/xs:complexType/xs:all/xs:element/xs:complexType/xs:all/xs:element[@name='" + this.attributeName + "']/xs:annotation/xs:appinfo/params/param[@id='" + this.attributeParameterId +"']/values/value[@id='" + this.attributeParameterValueId + "']";
			NodeList parameterValuesNodeList = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), parameterValueXPath);
			if(parameterValuesNodeList != null && parameterValuesNodeList.getLength() > 0)
			{
				Element element = (Element)parameterValuesNodeList.item(0);
				element.setAttribute("id", this.newAttributeParameterValueId);
				
				if(this.currentContentTypeEditorViewLanguageCode != null && this.currentContentTypeEditorViewLanguageCode.length() > 0)
					element.setAttribute("label_" + this.currentContentTypeEditorViewLanguageCode, this.attributeParameterValueLabel);
				else
					element.setAttribute("label", this.attributeParameterValueLabel);
			}
		
			StringBuffer sb = new StringBuffer();
			org.infoglue.cms.util.XMLHelper.serializeDom(document.getDocumentElement(), sb);

			ContentVersionController.getContentVersionController().updateAttributeValue(getContentVersionId(), getContentVersionAttributeName(), sb.toString(), this.getInfoGluePrincipal());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		this.initialize();

		return "success";
	}

	public List getAvailableLanguages()
	{
		return this.availableLanguages;
	}
	
	public String getAttributeName()
	{
		return this.attributeName;
	}

	public Integer getContentVersionId()
	{
		return this.contentVersionId;
	}

	public String getFormDefinition()
	{
		return this.formDefinition;
	}

	public void setAttributeName(String attributeName)
	{
		this.attributeName = attributeName;
	}

	public void setContentVersionId(Integer contentVersionId)
	{
		this.contentVersionId = contentVersionId;
	}

	public void setFormDefinition(String formDefinition)
	{
		this.formDefinition = formDefinition;
	}

	public List getAttributes()
	{
		return attributes;
	}

	public void setAttributes(List attributes)
	{
		this.attributes = attributes;
	}

	public String getInputTypeId()
	{
		return this.inputTypeId;
	}

	public void setInputTypeId(String inputTypeId)
	{
		this.inputTypeId = inputTypeId;
	}

	public String getContentVersionAttributeName()
	{
		return this.contentVersionAttributeName;
	}

	public void setContentVersionAttributeName(String contentVersionAttributeName)
	{
		this.contentVersionAttributeName = contentVersionAttributeName;
	}

	public String getAttributeParameterId()
	{
		return this.attributeParameterId;
	}

	public String getAttributeParameterValueId()
	{
		return this.attributeParameterValueId;
	}

	public String getAttributeParameterValueLabel()
	{
		return this.attributeParameterValueLabel;
	}

	public String getAttributeParameterValueLocale()
	{
		return this.attributeParameterValueLocale;
	}

	public String getAttributeToExpand()
	{
		return this.attributeToExpand;
	}

	public String getCurrentContentTypeEditorViewLanguageCode()
	{
		return this.currentContentTypeEditorViewLanguageCode;
	}

	public String getNewAttributeName()
	{
		return this.newAttributeName;
	}

	public String getNewAttributeParameterValueId()
	{
		return this.newAttributeParameterValueId;
	}

	public void setAttributeParameterId(String attributeParameterId)
	{
		this.attributeParameterId = attributeParameterId;
	}

	public void setAttributeParameterValueId(String attributeParameterValueId)
	{
		this.attributeParameterValueId = attributeParameterValueId;
	}

	public void setAttributeParameterValueLabel(String attributeParameterValueLabel)
	{
		this.attributeParameterValueLabel = attributeParameterValueLabel;
	}

	public void setAttributeParameterValueLocale(String attributeParameterValueLocale)
	{
		this.attributeParameterValueLocale = attributeParameterValueLocale;
	}

	public void setAttributeToExpand(String attributeToExpand)
	{
		this.attributeToExpand = attributeToExpand;
	}

	public void setCurrentContentTypeEditorViewLanguageCode(String currentContentTypeEditorViewLanguageCode)
	{
		this.currentContentTypeEditorViewLanguageCode = currentContentTypeEditorViewLanguageCode;
	}

	public void setNewAttributeName(String newAttributeName)
	{
		this.newAttributeName = newAttributeName;
	}

	public void setNewAttributeParameterValueId(String newAttributeParameterValueId)
	{
		this.newAttributeParameterValueId = newAttributeParameterValueId;
	}

}
