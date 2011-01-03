package org.infoglue.deliver.integration.dataproviders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.databeans.GenericOptionDefinition;
import org.infoglue.cms.entities.management.ContentTypeAttributeOptionDefinition;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.deliver.applications.databeans.ComponentPropertyOption;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.cms.util.dom.DOMBuilder;

public class RemoteXMLPropertyOptionsDataProvider implements PropertyOptionsDataProvider
{
    private final static Logger logger = Logger.getLogger(RemoteXMLPropertyOptionsDataProvider.class.getName());
	
	public List<GenericOptionDefinition> getOptions(Map parameters, String languageCode, InfoGluePrincipal principal, Database db) throws Exception
	{
		String serviceUrl = (String)parameters.get("serviceUrl");
		if(serviceUrl == null)
			throw new Exception("Must supply a valid parameter [serviceUrl] to this provider.");
		
		String charset = (String)parameters.get("charset");
		if(charset == null)
			charset = "utf-8";
			
		logger.info("RemoteXMLPropertyOptionsDataProvider");
		logger.info("serviceUrl:" + serviceUrl);
		logger.info("charset:" + charset);
		List<GenericOptionDefinition> options = (List<GenericOptionDefinition>)CacheController.getCachedObjectFromAdvancedCache("propertyOptionsCache", "properties_" + serviceUrl + "_" + charset + "_" + languageCode, 300);
		if(options != null)
		{
			logger.info("Using cached options...");
			return options;			
		}
		
		try
		{
			logger.info("Getting options from remote service...");

			options = new ArrayList<GenericOptionDefinition>();
			
			org.infoglue.deliver.util.HttpHelper httpHelper = new org.infoglue.deliver.util.HttpHelper();
			String xml = httpHelper.getUrlContent(serviceUrl, Collections.EMPTY_MAP, parameters, charset, 10000);
			logger.info("xml from service:\n" + xml);
			
			Document document = new DOMBuilder().getDocument(xml);
			List properties = document.getRootElement().selectNodes("property");
			logger.info("properties:" + properties.size());
			
			Iterator propertiesIterator = properties.iterator();
			while(propertiesIterator.hasNext())
			{
				Element propertyElement = (Element)propertiesIterator.next();
				logger.info("propertyElement:" + propertyElement);
				ComponentPropertyOption option = new ComponentPropertyOption();
				if(propertyElement.attributeValue("name_" + languageCode) != null && !propertyElement.attributeValue("name_" + languageCode).equals(""))
					option.setName(propertyElement.attributeValue("name_" + languageCode));
				else
					option.setName(propertyElement.attributeValue("name"));
				
				option.setValue(propertyElement.attributeValue("value"));
				options.add(option);
			}

			if(options != null)
	        	CacheController.cacheObjectInAdvancedCache("propertyOptionsCache", "properties_" + serviceUrl + "_" + charset, options);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			options = (List<GenericOptionDefinition>)CacheController.getCachedObjectFromAdvancedCache("propertyOptionsCache", "properties_" + serviceUrl + "_" + charset);
		}
		
		return options;
	}
}
