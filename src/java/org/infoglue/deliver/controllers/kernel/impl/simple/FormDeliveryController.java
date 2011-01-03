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

package org.infoglue.deliver.controllers.kernel.impl.simple;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.infoglue.cms.entities.management.ContentTypeAttribute;
import org.infoglue.cms.entities.management.ContentTypeAttributeParameter;
import org.infoglue.cms.entities.management.ContentTypeAttributeParameterValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class FormDeliveryController
{
    private final static Logger logger = Logger.getLogger(FormDeliveryController.class.getName());

	/**
	 * Private constructor to enforce factory-use
	 */
	
	private FormDeliveryController()
	{
	}
	
	/**
	 * Factory method
	 */
	
	public static FormDeliveryController getFormDeliveryController()
	{
		return new FormDeliveryController();
	}
	
	/**
	 * This method returns the attributes in the content type definition for generation.
	 */
		
	public List getContentTypeAttributes(String schemaValue)
	{
		logger.info("schemaValue:" + schemaValue);
		List attributes = new ArrayList();
			
		try
		{
			InputSource xmlSource = new InputSource(new StringReader(schemaValue));
				
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			Document document = parser.getDocument();
				
			String attributesXPath = "/xs:schema/xs:complexType/xs:all/xs:element/xs:complexType/xs:all/xs:element";
			NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), attributesXPath);
			for(int i=0; i < anl.getLength(); i++)
			{
				Element child = (Element)anl.item(i);
				String attributeName = child.getAttribute("name");
				String attributeType = child.getAttribute("type");
									
				ContentTypeAttribute contentTypeAttribute = new ContentTypeAttribute();
				contentTypeAttribute.setPosition(i);
				contentTypeAttribute.setName(attributeName);
				contentTypeAttribute.setInputType(attributeType);
	
				// Get extra parameters
				Node paramsNode = org.apache.xpath.XPathAPI.selectSingleNode(child, "xs:annotation/xs:appinfo/params");
				if (paramsNode != null)
				{	
					NodeList childnl = ((Element)paramsNode).getElementsByTagName("param");
					for(int ci=0; ci < childnl.getLength(); ci++)
					{
						Element param = (Element)childnl.item(ci);
						String paramId = param.getAttribute("id");
						String paramInputTypeId = param.getAttribute("inputTypeId");
							
						ContentTypeAttributeParameter contentTypeAttributeParameter = new ContentTypeAttributeParameter();
						contentTypeAttributeParameter.setId(paramId);
						if(paramInputTypeId != null && paramInputTypeId.length() > 0)
							contentTypeAttributeParameter.setType(Integer.parseInt(paramInputTypeId));
					
						contentTypeAttribute.putContentTypeAttributeParameter(paramId, contentTypeAttributeParameter);
							
						NodeList valuesNodeList = param.getElementsByTagName("values");
						for(int vsnli=0; vsnli < valuesNodeList.getLength(); vsnli++)
						{
							Element values = (Element)valuesNodeList.item(vsnli);
	
							NodeList valueNodeList = param.getElementsByTagName("value");
							for(int vnli=0; vnli < valueNodeList.getLength(); vnli++)
							{
								Element value = (Element)valueNodeList.item(vnli);
								String valueId = value.getAttribute("id");
									
								ContentTypeAttributeParameterValue contentTypeAttributeParameterValue = new ContentTypeAttributeParameterValue();
								contentTypeAttributeParameterValue.setId(valueId);
									
								NamedNodeMap nodeMap = value.getAttributes();
								for(int nmi =0; nmi < nodeMap.getLength(); nmi++)
								{
									Node attribute = (Node)nodeMap.item(nmi);
									String valueAttributeName = attribute.getNodeName();
									String valueAttributeValue = attribute.getNodeValue();
									contentTypeAttributeParameterValue.addAttribute(valueAttributeName, valueAttributeValue);
								}
									
								contentTypeAttributeParameter.addContentTypeAttributeParameterValue(valueId, contentTypeAttributeParameterValue);
							}
						}								
					}
				}
				// End extra parameters
	
				attributes.add(contentTypeAttribute);
			}
				
		}
		catch(Exception e)
		{
			logger.error("An error occurred when we tried to get the attributes of the content type: " + e.getMessage(), e);	
		}
		    		
		return attributes;
	}

}