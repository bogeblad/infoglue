/*
 * Copyright 2003,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.infoglue.deliver.portal.services;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.portlet.PortletApplicationDefinitionList;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.portalImpl.om.portlet.impl.PortletApplicationDefinitionImpl;
import org.apache.pluto.portalImpl.om.portlet.impl.PortletApplicationDefinitionListImpl;
import org.apache.pluto.portalImpl.om.servlet.impl.WebApplicationDefinitionImpl;
import org.apache.pluto.portalImpl.services.log.Log;
import org.apache.pluto.portalImpl.services.portletdefinitionregistry.PortletDefinitionRegistry;
import org.apache.pluto.portalImpl.services.portletdefinitionregistry.PortletDefinitionRegistryService;
import org.apache.pluto.portalImpl.util.Properties;
import org.apache.pluto.portalImpl.xml.XmlParser;
import org.apache.pluto.services.log.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

/**
 * A simple XML Castor file based implementation of the
 * <code>PortletRegistryService</config>.
 *
 * <p>This store persit the PortletRegistry informations.</p>
 *
 */

public class PortletDefinitionRegistryServiceFileImplIG extends PortletDefinitionRegistryService
{

    private static String fileSeparator = System.getProperty("file.separator");

    /**
     * The initial portion of the web module prefix used by JBoss.
     */
    private final static String INITIAL_TMP_PREFIX = "tmp";

    /**
     * The length of the full web module prefix used by JBoss ({@link
     * #INITIAL_TMP_PREFIX} plus numeric portion).
     */
    private final static int FULL_TMP_PREFIX_LEN = INITIAL_TMP_PREFIX.length() + 5;

    /**
     * The file extension for web application archives (including the
     * leading dot).
     */
    private final static String WAR_FILE_EXT = ".war";
    
    // default configuration values
    public final static String DEFAULT_MAPPING_PORTLETXML = "WEB-INF/data/xml/portletdefinitionmapping.xml";
    public final static String DEFAULT_MAPPING_WEBXML     = "WEB-INF/data/xml/servletdefinitionmapping.xml";
    // configuration keys
    private final static String CONFIG_MAPPING_PORTLETXML = "mapping.portletxml.configfile";
    private final static String CONFIG_MAPPING_WEBXML     = "mapping.webxml.configfile";

    // Castor mapping file
    private Mapping mappingPortletXml = null;
    private Mapping mappingWebXml = null;
    // Servlet Context
    private ServletContext servletContext = null;
    // Base Dir where all web modules are located
    private String baseWMDir = null;
    private Logger log = null;

    // Helper lists and hashtables to access the data as fast as possible
    // List containing all portlet applications available in the system
    protected static PortletApplicationDefinitionListImpl registry = new PortletApplicationDefinitionListImpl();
    protected static Map portletsKeyObjectId = new HashMap();

    public void init (ServletConfig config, Properties properties) throws Exception
    {
        log = Log.getService().getLogger(getClass());
        log.debug("Initializing portlet registry....:" + config);
        servletContext = config.getServletContext();

        if (properties.getBoolean("non-servlet")==Boolean.TRUE)
        {
            String root = config.getServletContext().getRealPath("/"); //root
            baseWMDir = root + fileSeparator + 
		"WEB-INF" + fileSeparator + 
		"portletapps" + fileSeparator; //org.apache.pluto.portalImpl.services.deploy.DeployServiceFileImpl.DEFAULT_PROTECTED;
            if(log.isDebugEnabled())
                log.debug("baseWMDir = " + baseWMDir + " fileSeparator = " + fileSeparator);
        }
        else
        {
            this.baseWMDir = this.servletContext.getRealPath("");
            // BEGIN PATCH for IBM WebSphere 
            if (this.baseWMDir.endsWith(fileSeparator)) {
                this.baseWMDir = this.baseWMDir.substring(0, this.baseWMDir.length()-1);
            }
            // END PATCH for IBM WebSphere 
            
            this.baseWMDir = this.baseWMDir.substring(0, this.baseWMDir.lastIndexOf(fileSeparator))+fileSeparator;
            if (log.isDebugEnabled()) 
            {
                log.debug("servletContext.getRealPath('') =" + this.servletContext.getRealPath(""));
                log.debug("baseWMDir = " + this.baseWMDir);
            }            
        }

        // get portlet xml mapping file
        String _mapping = properties.getString(CONFIG_MAPPING_PORTLETXML, DEFAULT_MAPPING_PORTLETXML);
        log.debug("_mapping = " + _mapping);
        File f = new File(_mapping);
        if (!f.isAbsolute()) _mapping = servletContext.getRealPath(_mapping);
        this.mappingPortletXml = new Mapping();
        try
        {
            this.mappingPortletXml.loadMapping(_mapping);
        }
        catch (Exception e)
        {
            log.error("Failed to load mapping file "+_mapping,e);
            throw e;
        }
        // get web xml mapping file
        _mapping = properties.getString(CONFIG_MAPPING_WEBXML, DEFAULT_MAPPING_WEBXML);
        f = new File(_mapping);
        if (!f.isAbsolute()) _mapping = servletContext.getRealPath(_mapping);
        this.mappingWebXml = new Mapping();
        try
        {
            this.mappingWebXml.loadMapping(_mapping);
        }
        catch (Exception e)
        {
            log.error("Failed to load mapping file "+_mapping,e);
            throw e;
        }

        load();

        fill();
    }

    public PortletApplicationDefinitionList getPortletApplicationDefinitionList()
    {
        return registry;
    }

    public PortletDefinition getPortletDefinition(ObjectID id)
    {
    	log.debug("Trying to get portlet from portletsKeyObjectId [" + portletsKeyObjectId.hashCode() + "] by " + id);
    	PortletDefinition portletDefinition = (PortletDefinition)portletsKeyObjectId.get(id);
    	log.debug("portletDefinition: " + portletDefinition);
    	if(portletDefinition == null)
    	{
    		log.debug("\n\nList contains:\n\n");
    		Iterator portletsKeyObjectIdIterator = portletsKeyObjectId.keySet().iterator();
    		while(portletsKeyObjectIdIterator.hasNext())
    		{
    			ObjectID key = (ObjectID)portletsKeyObjectIdIterator.next();
    			PortletDefinition listPortletDefinition = (PortletDefinition)portletsKeyObjectId.get(key);
    			log.debug("" + key + "=" + listPortletDefinition);		
    		}
    		log.debug("\n\n-------------------:\n\n");
    	}
    	return portletDefinition;
    }

    private void load() throws Exception
    {
    	log.debug("baseWMDir in load(): " + baseWMDir);
    	
        File f = new File(baseWMDir);
        String[] entries = f.list();
        for (int i=0; i<entries.length; i++)
        {
            File entry = new File(baseWMDir+entries[i]);
            if (entry.isDirectory())
            {
                if (log.isDebugEnabled()) 
                {
                    log.debug("Searching in directory: " + entries[i]);
                }
                load(baseWMDir, entries[i]);
            }
        }
    }

    /**
     * Handles resolution of a web module's file system name to its
     * URI identifier.
     *
     * @param webModule The file system name.
     * @return The URI part.
     */
    private String resolveURI(String webModule)
    {
        // For JBoss compatibility, change webModule from the form
        // of "tmp12345foo.war" to "foo". The first char is the first char of the webapp
        int len = webModule.length();
        if (webModule.endsWith(WAR_FILE_EXT) && webModule.startsWith(INITIAL_TMP_PREFIX) && len > FULL_TMP_PREFIX_LEN + WAR_FILE_EXT.length()) 
        {
            //webModule = webModule.substring(FULL_TMP_PREFIX_LEN, len - WAR_FILE_EXT.length());
            webModule = webModule.substring(INITIAL_TMP_PREFIX.length(), len - WAR_FILE_EXT.length());
            char c = webModule.charAt(0);
            while(Character.isDigit(c))
            {
            	webModule = webModule.substring(1);
            	c = webModule.charAt(0);
            }
        }
        // else assumed literal.
        return webModule;
    }

    private void load(String baseDir, String webModule) throws Exception
    {
        String directory = baseDir+webModule+fileSeparator+"WEB-INF"+fileSeparator;

        File portletXml = new File(directory+"portlet.xml");
        File webXml = new File(directory+"web.xml");

        // check for the porlet.xml. If there is no portlet.xml this is not a
        // portlet application web module
        if (portletXml.exists()) // && (webXml.exists()))
        {
        	if (log.isDebugEnabled())
            {
                log.debug("Loading the following Portlet Applications XML files..."+portletXml+", "+webXml);
            }

            InputSource source = new InputSource(new FileInputStream(portletXml));
            source.setSystemId(portletXml.toURL().toExternalForm());
            
            Unmarshaller unmarshaller = new Unmarshaller(this.mappingPortletXml);
			unmarshaller.setIgnoreExtraElements(true);
            PortletApplicationDefinitionImpl portletApp = 
                (PortletApplicationDefinitionImpl)unmarshaller.unmarshal( source );

            WebApplicationDefinitionImpl webApp = null;

            if (webXml.exists())
            {
                org.w3c.dom.Document webDocument = 
                XmlParser.parseWebXml(new FileInputStream(webXml));

                unmarshaller = new Unmarshaller(this.mappingWebXml);
				unmarshaller.setIgnoreExtraElements(true);
                webApp = (WebApplicationDefinitionImpl)unmarshaller.unmarshal(webDocument);

                Vector structure = new Vector();
                structure.add(portletApp);
                structure.add("/" + resolveURI(webModule));

                webApp.postLoad(structure);

                // refill structure with necessary information
                webApp.preBuild(structure);

                webApp.postBuild(structure);

                if (log.isDebugEnabled())
                {
                    log.debug(webApp.toString());
                }
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("no web.xml...");
                }
                Vector structure = new Vector();
                structure.add("/" + resolveURI(webModule));
                structure.add(null);
                structure.add(null);

                portletApp.postLoad(structure);
                
                portletApp.preBuild(structure);
                
                portletApp.postBuild(structure);
            }

            log.debug("Adding portletApp to registry[" + registry + "]:" + portletApp.toString());
            log.debug("Before:" + registry.size());
            registry.add( portletApp );
            log.debug("After:" + registry.size());

            if (log.isDebugEnabled())
            {
                if (webApp!=null)
                {
                    log.debug("Dumping content of web.xml...");
                    log.debug(webApp.toString());
                }
                log.debug("Dumping content of portlet.xml...");
                log.debug(portletApp.toString());
            }
        }
        else
        {
        	log.debug("Could not find " + portletXml);
        }

    }

    private void fill()
    {

        Iterator iterator = registry.iterator();
        while (iterator.hasNext())
        {
            PortletApplicationDefinition papp = (PortletApplicationDefinition)iterator.next();
            
            // fill portletsKeyObjectId
            Iterator portlets = papp.getPortletDefinitionList().iterator();
            while (portlets.hasNext())
            {
                PortletDefinition portlet = (PortletDefinition)portlets.next();
            
                portletsKeyObjectId.put(portlet.getId(), portlet);
                log.debug("Putting in portletsKeyObjectId[" + portletsKeyObjectId.hashCode() + "]:" + portlet.getId() + "=" + portlet.toString() + "[" + papp.getId() + "]");
            }

        }

    }

    //method added for hot deploy
     public void postInit() throws Exception
     {
     	PortletDefinitionRegistry.setPortletDefinitionRegistryService();
     }
    
}
