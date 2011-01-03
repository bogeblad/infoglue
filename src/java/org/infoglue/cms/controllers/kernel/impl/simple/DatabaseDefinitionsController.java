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

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xpath.XPathAPI;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.CategoryAttribute;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.workflow.Event;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.entities.workflow.impl.simple.EventImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author Mattias Bogeblad
 *
 * This class implements all operations we can do on the cmEvent-entity.
 */

public class DatabaseDefinitionsController 
{
    private final static Logger logger = Logger.getLogger(DatabaseDefinitionsController.class.getName());

    public static DatabaseDefinitionsController getController()
    {
    	return new DatabaseDefinitionsController();
    }
    
	/**
	 * Returns a parsed database definitions document
	 */
	private Document getDatabaseDefinitionsDocument()
	{
		Document document = null;
		
        try
        {
    		String databaseDefinitions = CmsPropertyHandler.getContextRootPath() + File.separator + "WEB-INF" + File.separator + "classes" + File.separator + "databaseDefinitions.xml";
    		
    		File file = new File(databaseDefinitions);

        	if(file != null && file.exists())
        	{
		        InputSource xmlSource = new InputSource(new FileInputStream(file));

				DOMParser parser = new DOMParser();
				parser.parse(xmlSource);
				document = parser.getDocument();
        	}
        }
        catch(Exception e)
        {
        	logger.warn("An error occurred when trying to fetch the asset keys:" + e.getMessage(), e);
        }

		return document;
	}

	/**
	 * Returns a List of DatabaseDefintions from the file
	 */
	
	public Map getDatabaseDefinitions()
	{
		Map databases = new HashMap();
		
		Document document = getDatabaseDefinitionsDocument();
		
		if(document != null)
		{
			NodeList databaseNodeList = document.getDocumentElement().getElementsByTagName("database");
			
			for(int i=0; i < databaseNodeList.getLength(); i++)
			{
				Element databaseElement = (Element)databaseNodeList.item(i);
				String id = databaseElement.getAttribute("id");
				NodeList propertyNodeList = databaseElement.getElementsByTagName("property");
				
				Map database = new HashMap();
				database.put("id", id);
				
				for(int j=0; j < propertyNodeList.getLength(); j++)
				{
					Element propertyElement = (Element)propertyNodeList.item(j);
					String name = propertyElement.getAttribute("name");
					String value = propertyElement.getAttribute("value");
					
					database.put(name, value);
				}
				
				databases.put(id, database);
			}
		}
		
		return databases;
	}

	/**
	 * Returns a List of DatabaseDefintions from the file
	 */
	
	public Map getDatabaseDefinition(String id)
	{
		Map definitions = getDatabaseDefinitions();
		
		return (Map)definitions.get(id);
	}

	
	/**
	 * Returns a Castor database definition xml string
	 */
	
	public File getCastorDatabaseDefinitionFile(String id) 
	{
		Map databaseDefinition = DatabaseDefinitionsController.getController().getDatabaseDefinition("default");
    	String engine = (String)databaseDefinition.get("driverEngine");
    	String driverClass = (String)databaseDefinition.get("driverClass");
    	String url = (String)databaseDefinition.get("url");
    	String user = (String)databaseDefinition.get("user");
    	String password = (String)databaseDefinition.get("password");
    	String useUnicode = (String)databaseDefinition.get("useUnicode");
    	String encoding = (String)databaseDefinition.get("encoding");
    	String maxConnections = (String)databaseDefinition.get("maxConnections");
    	String mapping = (String)databaseDefinition.get("mapping");
    	
    	url = url.replaceAll("&", "&amp;");
    	
    	/*
    	 <jdo-conf>
	     <database name="INFOGLUE_CMS" engine="\"" + engine + "\"">
	        <jndi name="java:comp/env/jdbc/mydb" />
	     </database>
			<transaction-demarcation mode=\"local\"/>
	  </jdo-conf>
    	 */
    	
    	StringBuffer sb = new StringBuffer();
		sb.append("<jdo-conf>");
		sb.append("	<database name=\"INFOGLUE_CMS\" engine=\"" + engine + "\">");
		sb.append("		<data-source class-name=\"org.apache.commons.dbcp.BasicDataSource\">");
		sb.append("			<param name=\"driver-class-name\" value=\"" + driverClass + "\"/>");
		sb.append("			<param name=\"username\" value=\"" + user + "\"/>");
		sb.append("			<param name=\"password\" value=\"" + password + "\"/>");
		sb.append("			<param name=\"url\" value=\"" + url + "\"/>");
		sb.append("			<param name=\"max-active\" value=\"" + maxConnections + "\"/>");
		sb.append("			<param name=\"connection-properties\" value=\"useUnicode=" + useUnicode + ";characterEncoding=" + encoding + "\"/>");
		sb.append("		</data-source>");
		sb.append("		<mapping href=\"classes/" + mapping + "\"/>");
		sb.append("	</database>");
		sb.append(" <transaction-demarcation mode=\"local\"/>");
		sb.append("</jdo-conf>");
		
		String xml = sb.toString();
		
		String databaseDefinitions = CmsPropertyHandler.getContextRootPath() + File.separator + "WEB-INF" + File.separator + "classes" + File.separator + "currentDatabase.xml";
		
		File file = new File(databaseDefinitions);
		
		try 
		{
			FileHelper.writeToFile(file, xml, false);
		} 
		catch (Exception e) 
		{
			logger.error("Could not write currentDatabase.xml:" + e.getMessage(), e);
		}
		
		return file;
	}
}
