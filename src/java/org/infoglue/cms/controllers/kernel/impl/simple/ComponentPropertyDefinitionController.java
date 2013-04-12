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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.xerces.parsers.DOMParser;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.databeans.ComponentPropertyDefinition;
import org.infoglue.cms.applications.databeans.ComponentPropertyOptionDefinition;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class handles aspects of component properties definitions.
 * 
 * @author Mattias Bogeblad
 */

public class ComponentPropertyDefinitionController extends BaseController
{

    /**
	 * Factory method
	 */

	public static ComponentPropertyDefinitionController getController()
	{
		return new ComponentPropertyDefinitionController();
	}
	
	public List getComponentPropertyDefinitions(Database db, Integer contentId, Integer languageId) throws Exception
	{
		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId, db);
		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, languageId, db);
		if(contentVersionVO == null)
		{
			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId(), db);
			contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, masterLanguageVO.getId(), db);
		}
		
		if(contentVersionVO != null)
		{
			String propertyXML = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, "ComponentProperties", false);
			
			return parseComponentPropertyDefinitions(propertyXML);
		}
		else
		{
			return new ArrayList();
		}
	}
	
	public List parseComponentPropertyDefinitions(String xml)
	{
	    List componentPropertyDefinitions = new ArrayList();
	    
	    if(xml == null || xml.equals(""))
	        return componentPropertyDefinitions;
	    
	    try
	    {
	    	xml = xml.replaceAll("<!--igescaped-->", "");
	    	
	        InputSource xmlSource = new InputSource(new StringReader(xml));

			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			Document document = parser.getDocument();

			NodeList nl = document.getElementsByTagName("properties");
			for(int i=0; i<nl.getLength(); i++)
			{
			    Node propertiesNode = nl.item(i);
			    Element propertiesElement = (Element)propertiesNode;
			    
				NodeList propertyNodeList = propertiesElement.getElementsByTagName("property");
				for(int j=0; j<propertyNodeList.getLength(); j++)
				{
				    Node propertyNode = propertyNodeList.item(j);
				    Element propertyElement = (Element)propertyNode;
				    
				    String name 					= propertyElement.getAttribute("name");
				    String displayName 				= propertyElement.getAttribute("displayName");
				    String type 					= propertyElement.getAttribute("type");
				    String entity 					= propertyElement.getAttribute("entity");
				    String multiple 				= propertyElement.getAttribute("multiple");
				    String assetBinding 			= propertyElement.getAttribute("assetBinding");
				    String assetMask 				= propertyElement.getAttribute("assetMask");
				    String isPuffContentForPage 	= propertyElement.getAttribute("isPuffContentForPage");
				    String allowedContentTypeNames 	= propertyElement.getAttribute("allowedContentTypeDefinitionNames");
				    String description				= propertyElement.getAttribute("description");
				    String defaultValue				= propertyElement.getAttribute("defaultValue");
				    String allowLanguageVariations	= propertyElement.getAttribute("allowLanguageVariations");
				    String dataProvider				= propertyElement.getAttribute("dataProvider");
				    String dataProviderParameters	= propertyElement.getAttribute("dataProviderParameters");
				    String allowMultipleSelections	= propertyElement.getAttribute("allowMultipleSelections");
				    String WYSIWYGEnabled			= propertyElement.getAttribute("WYSIWYGEnabled");
				    String WYSIWYGToolbar			= propertyElement.getAttribute("WYSIWYGToolbar");
				    String autoCreateContent		= propertyElement.getAttribute("autoCreateContent");
				    String autoCreateContentMethod	= propertyElement.getAttribute("autoCreateContentMethod");
				    String autoCreateContentPath	= propertyElement.getAttribute("autoCreateContentPath");
				    String customMarkup				= propertyElement.getAttribute("customMarkup");
				    String supplementingEntityType	= propertyElement.getAttribute("supplementingEntityType");
				    String externalBindingConfig	= propertyElement.getAttribute("externalBindingConfig");
				    if(allowLanguageVariations == null || allowLanguageVariations.equals(""))
				    	allowLanguageVariations = "true";
				    	
				    ComponentPropertyDefinition cpd = new ComponentPropertyDefinition(name, displayName, type, entity, new Boolean(multiple), new Boolean(assetBinding), assetMask, new Boolean(isPuffContentForPage), allowedContentTypeNames, description, defaultValue, new Boolean(allowLanguageVariations), new Boolean(WYSIWYGEnabled), WYSIWYGToolbar, dataProvider, dataProviderParameters, new Boolean(autoCreateContent), autoCreateContentMethod, autoCreateContentPath, customMarkup, new Boolean(allowMultipleSelections), supplementingEntityType, externalBindingConfig);
				    
					NodeList optionsNodeList = propertyElement.getElementsByTagName("option");
					for(int k=0; k<optionsNodeList.getLength(); k++)
					{
					    Node optionNode = optionsNodeList.item(k);
					    Element optionElement = (Element)optionNode;
					    
					    String optionName 	= optionElement.getAttribute("name");
					    String optionValue 	= optionElement.getAttribute("value");
					    				    
					    ComponentPropertyOptionDefinition cpod = new ComponentPropertyOptionDefinition(optionName, optionValue);
					    
					    cpd.getOptions().add(cpod);
					}

					
				    componentPropertyDefinitions.add(cpd);
				}
			}
		}
	    catch(Exception e)
	    {
	        e.printStackTrace();
	    }
	    
	    return componentPropertyDefinitions;
	}

    public BaseEntityVO getNewVO()
    {
        return null;
    }

	
	
}
