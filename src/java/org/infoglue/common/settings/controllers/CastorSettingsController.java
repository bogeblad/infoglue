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

package org.infoglue.common.settings.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.entities.management.InfoGlueProperty;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.NullObject;

public class CastorSettingsController
{
	private DOMBuilder domBuilder = new DOMBuilder();
	private CastorSettingsPersister settingsPersister = null;

	/**
	 * A simple factory method
	 */
	
	public static CastorSettingsController getController(CastorSettingsPersister labelsPersister)
	{
		return new CastorSettingsController(labelsPersister);
	}
	
	private CastorSettingsController(CastorSettingsPersister settingsPersister)
	{
		this.settingsPersister = settingsPersister;
	}

	/**
	 * This method returns a list (of strings) of all label-keys the system uses.
	 */
	
	public List getSystemSettings(String bundleName)
	{
		List labels = new ArrayList();
		
		Properties properties = CmsPropertyHandler.getProperties();
		
		Enumeration enumeration = properties.keys();
		while(enumeration.hasMoreElements())
		{
			String key = (String)enumeration.nextElement();
			labels.add(key);
		}
		
		Collections.sort(labels);
		
		return labels;
	}

	/**
	 * This method returns a list (of locales) of all defined label-languages.
	 */
	
	public List getSettingsVariations(String nameSpace, String name, Database database) throws Exception
	{
		List locales = new ArrayList();
		
		Document document = getPropertyDocument(nameSpace, name, database);
		if(document != null)
		{
			List languageNodes = document.selectNodes("/variations/variation");
			Iterator languageNodesIterator = languageNodes.iterator();
			while(languageNodesIterator.hasNext())
			{
				Node node = (Node)languageNodesIterator.next();
				Element element = (Element)node;
				String id = element.attributeValue("id");
				
				locales.add(id);
			}
		}
		
		return locales;
	}

	
	
	public Document getPropertyDocument(String nameSpace, String name, Database database) throws Exception
	{
		String key = "propertyDocument_" + nameSpace + "_" + name;
		
		Object object = CacheController.getCachedObject(CacheController.SETTINGSPROPERTIESDOCUMENTCACHENAME, key);
		//log.debug("Cached object:" + object);
		if(object instanceof NullObject)
			return null;
		
		Document document = (Document)object;
		
		if(document == null)
		{
			InfoGlueProperty property = settingsPersister.getProperty(nameSpace, name, database);
			if(property != null)
			{
				String xml = property.getValue();
				if(xml != null && xml.length() > 0)
				{
					//log.debug("xml:" + xml);
					try
					{
						document = domBuilder.getDocument(xml);
						//String xml2 = domBuilder.getFormattedDocument(document, false, false, "UTF-8");
						//log.debug("xml2:" + xml2);
					}
					catch(Exception e)
					{
						document = domBuilder.createDocument();
						Element variationsElement = domBuilder.addElement(document, "variations");
						Element languageElement = domBuilder.addElement(variationsElement, "variation");
						domBuilder.addAttribute(languageElement, "id", "default"); 
						Element labelsElement = domBuilder.addElement(languageElement, "setting");						
					}
				}
			}
			else
			{
				//log.debug("Property was null...");
				document = domBuilder.createDocument();
				Element variationsElement = domBuilder.addElement(document, "variations");
				Element languageElement = domBuilder.addElement(variationsElement, "variation");
				domBuilder.addAttribute(languageElement, "id", "default"); 
				Element labelsElement = domBuilder.addElement(languageElement, "setting");
				String xml = domBuilder.getFormattedDocument(document, "UTF-8");
				//log.debug("xml:" + xml);
	
				settingsPersister.createProperty(nameSpace, name, xml, database);
			
	            //log.debug("Creating property:" + xml);
	        	
				document = domBuilder.getDocument(xml);
			}
		
			if(document != null)
			{
				CacheController.cacheObject(CacheController.SETTINGSPROPERTIESDOCUMENTCACHENAME, key, document);
			}
			else
			{
				CacheController.cacheObject(CacheController.SETTINGSPROPERTIESDOCUMENTCACHENAME, key, new NullObject());
			}
		}
		
		return document;
	}

	public void addVariation(String nameSpace, String name, String id, Database database) throws Exception
	{
		if(id == null || id.equals("-1"))
			id = "default";

		Document document = getPropertyDocument(nameSpace, name, database);
		Element variationElement = (Element)document.selectSingleNode("/variations/variation[@id='" + id + "']");
		if(variationElement == null)
		{
			Element variationsElement = (Element)document.selectSingleNode("/variations");
			Element languageElement = domBuilder.addElement(variationsElement, "variation");
			domBuilder.addAttribute(languageElement, "id", id); 
			Element labelsElement = domBuilder.addElement(languageElement, "setting");
	        String xml = domBuilder.getFormattedDocument(document, "UTF-8");
	        //log.debug("xml:" + xml);
	
	        settingsPersister.updateProperty(nameSpace, name, xml, database);
	
	        CacheController.clearCache(CacheController.SETTINGSPROPERTIESCACHENAME);
	        CacheController.clearCache(CacheController.SETTINGSPROPERTIESDOCUMENTCACHENAME);
		}
	}

	public void removeVariation(String nameSpace, String name, String id, Database database) throws Exception
	{
		if(id == null || id.equals("-1"))
			id = "default";

		Document document = getPropertyDocument(nameSpace, name, database);
		Element variationsElement = (Element)document.selectSingleNode("/variations/variation[@id='" + id + "']");
		if(variationsElement != null)
		{
			variationsElement.getParent().remove(variationsElement);
			String xml = domBuilder.getFormattedDocument(document, "UTF-8");
	        //log.debug("xml:" + xml);
	
	        settingsPersister.updateProperty(nameSpace, name, xml, database);
	
	        CacheController.clearCache(CacheController.SETTINGSPROPERTIESCACHENAME);
	        CacheController.clearCache(CacheController.SETTINGSPROPERTIESDOCUMENTCACHENAME);
		}
	}

	public void removeProperty(String nameSpace, String name, String id, String key, Database database) throws Exception
	{
		if(id == null || id.equals("-1"))
			id = "default";

		Document document = getPropertyDocument(nameSpace, name, database);
		Element settingElement = (Element)document.selectSingleNode("/variations/variation[@id='" + id + "']/setting/" + key);
		if(settingElement != null)
		{
			settingElement.getParent().remove(settingElement);
			String xml = domBuilder.getFormattedDocument(document, "UTF-8");
			//log.debug("xml:" + xml);
	        settingsPersister.updateProperty(nameSpace, name, xml, database);

	        CacheController.clearCache(CacheController.SETTINGSPROPERTIESCACHENAME);
	        CacheController.clearCache(CacheController.SETTINGSPROPERTIESDOCUMENTCACHENAME);
		}
	}

	public void updateSettings(String nameSpace, String name, String id, Map properties, Database database) throws Exception
	{
		if(id == null || id.equals("-1"))
			id = "default";

		Document document = getPropertyDocument(nameSpace, name, database);
        //String xml1 = domBuilder.getFormattedDocument(document, "UTF-8");
        //log.debug("xml1:" + xml1);
        String xpath = "/variations/variation[@id='" + id +"']/setting";
        //log.debug("xpath:" + xpath);
        
		Element labelsElement = (Element)document.selectSingleNode(xpath);
		//log.debug("labelsElement:" + labelsElement);
		
		Iterator keyInterator = properties.keySet().iterator();
		while(keyInterator.hasNext())
		{
			String key = (String)keyInterator.next();
			String value = (String)properties.get(key);
			if(!Character.isLetter(key.charAt(0)))
				key = "NP" + key;
			
			if(key != null && value != null && labelsElement != null)
			{
				Element labelElement = labelsElement.element(key);
				if(labelElement == null)
				{
					labelElement = domBuilder.addElement(labelsElement, key);					
				}
				else
				{
					labelElement.clearContent();
				
					List elements = labelElement.elements();
					Iterator elementsIterator = elements.iterator();
					while(elementsIterator.hasNext())
					{
						Element element = (Element)elementsIterator.next();
						//log.debug("Removing element:" + element.asXML());
						labelElement.remove(element);
					}
				}
				
				domBuilder.addCDATAElement(labelElement, value);
			}
		}
		
        String xml = domBuilder.getFormattedDocument(document, "UTF-8");

        settingsPersister.updateProperty(nameSpace, name, xml, database);

        CacheController.clearCache(CacheController.SETTINGSPROPERTIESCACHENAME);
        CacheController.clearCache(CacheController.SETTINGSPROPERTIESDOCUMENTCACHENAME);
        
	}

	/**
	 * This method returns a label from the label system
	 * @param nameSpace
	 * @param key
	 * @param locale
	 * @param database
	 * @return
	 * @throws Exception
	 */
	public String getSetting(String nameSpace, String name, String key, String id, Database database) throws Exception
	{
		String setting = null;

		if(id == null || id.equals("-1"))
			id = "default";
			
		if(key != null && !key.equals(""))
		{
			if(!Character.isLetter(key.charAt(0)))
				key = "NP" + key;
	
			Document document = getPropertyDocument(nameSpace, name, database);
	        if(document != null)
	        {
				if(id == null)
		    		id = "default";
		    	
				String xpath = "/variations/variation[@id='" + id +"']/setting/" + key;
				Element labelElement = (Element)document.selectSingleNode(xpath);
				
				if(labelElement != null)
					setting = labelElement.getText();
	        }
		}
		
		return setting;
	}

}
