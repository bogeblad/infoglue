/*
 * WebWork, Web Application Framework
 *
 * Distributable under Apache license.
 * See terms of license at opensource.org
 */
package webwork.config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.DOMException;
import org.apache.commons.logging.*;
import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.WebworkAbstractAction;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import webwork.util.ClassLoaderUtils;

/**
 * Access view configuration from an XML file and adds it to existing webwork config so plugins can register actions as well.
 */

public class XMLActionConfigurationExtendor
{

    private final static Logger logger = Logger.getLogger(XMLActionConfigurationExtendor.class.getName());

   public void getMappingsFromString(String xmlActions, Set<String> actionClassNames)
   {
	   Map infoglueActionMappings = null;
	   
	   System.out.println("xmlActions:" + xmlActions);
	  
	  System.out.println("Configuration.listImpl():" + Configuration.getConfiguration().getClass());
	  System.out.println("Configuration.listImpl():" + Configuration.getConfiguration());
	  
	  if(Configuration.getConfiguration() instanceof DefaultConfiguration)
	  {
		  DefaultConfiguration config = (DefaultConfiguration)Configuration.getConfiguration();
		  System.out.println("config:" + config.config);
		  System.out.println("config:" + config.configurationImpl);
		  if(config.config instanceof DelegatingConfiguration)
		  {
			  DelegatingConfiguration delConfig = (DelegatingConfiguration)config.config;
			  System.out.println("delConfig:" + delConfig.configList);
			  for(Configuration c : delConfig.configList)
			  {
				  System.out.println("Configuration:" + c);
				  System.out.println("Configuration.configurationImpl:" + c.configurationImpl);
				  if(c instanceof XMLActionConfiguration)
				  {
					  XMLActionConfiguration xmlActionConfiguration = (XMLActionConfiguration)c;
					  System.out.println("Configuration:" + c);
					  infoglueActionMappings = xmlActionConfiguration.actionMappings;
				  }
			  }
		  }
	  }
	  
	  /*
	  Iterator listImplIterator = Configuration.getConfiguration().listImpl();
      while(listImplIterator.hasNext())
      {
    	  Object o = listImplIterator.next();
    	  System.out.println("Configuration:" + o);
      }

	  System.out.println("Configuration.list():");
	  Iterator listIterator = Configuration.list();
      while(listIterator.hasNext())
      {
    	  Object o = listIterator.next();
    	  System.out.println("o:" + o);
      }
		*/
      
      try
      {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         // Parse document
         StringReader reader = new StringReader( xmlActions );
         InputSource inputSource = new InputSource( reader );
         
         Document document = factory.newDocumentBuilder().parse( inputSource );
         reader.close();
         
         // Get list of actions
         NodeList actions = document.getElementsByTagName("action");

         // Build list of views
         int length = actions.getLength();
         for (int i = 0; i < length; i++)
         {
            Element action = (Element)actions.item(i);
            String actionName = action.getAttribute("name");
            String actionAlias = action.getAttribute("alias");

            actionClassNames.add(actionName);
            
            // Build views for this action
            {
               NodeList views = action.getElementsByTagName("view");
               for (int j = 0; j < views.getLength(); j++)
               {
                  Element view = (Element)views.item(j);

                  // This is to avoid listing "view" elements
                  // that are associated with the commands
                  // of this action
                  if (!view.getParentNode().equals(action))
                     break;

                  // View mappings for this action
                  NodeList viewMapping = view.getChildNodes();
                  StringBuffer mapping = new StringBuffer();
                  for (int k = 0; k < viewMapping.getLength(); k++)
                  {
                     Node mappingNode = viewMapping.item(k);
                     if (mappingNode instanceof Text)
                     {
                        mapping.append(mappingNode.getNodeValue());
                     }
                  }

                  String actionViewName;
                  if ("".equals(actionAlias))
                  {
                      if(!"".equals(actionName))
                      {
                          actionViewName = actionName+"."+view.getAttribute("name");
                      }
                      else
                      {
                          actionViewName = view.getAttribute("name");
                      }
                  }
                  else
                  {
                     actionViewName = actionAlias+"."+view.getAttribute("name");
                     logger.debug("Adding action alias "+actionAlias+"="+actionName);
                     infoglueActionMappings.put(actionAlias+".action",
                           actionName);
                  }

                  String actionViewMapping = mapping.toString().trim();
                  logger.debug("Adding view mapping "+actionViewName+"="+actionViewMapping);
                  infoglueActionMappings.put(actionViewName, actionViewMapping);
               }
            }

            // Commands
            NodeList commands = action.getElementsByTagName("command");
            for (int j = 0; j < commands.getLength(); j++)
            {
               Element command = (Element)commands.item(j);
               String commandName = command.getAttribute("name");
               String commandAlias = command.getAttribute("alias");

               if (!commandAlias.equals(""))
               {
            	   logger.debug("Adding command alias "+commandAlias+"="+actionName+"!"+commandName);
            	   infoglueActionMappings.put(commandAlias+".action",
                        actionName+"!"+commandName);
               }

               // Build views for this action
               NodeList views = command.getElementsByTagName("view");
               for (int k = 0; k < views.getLength(); k++)
               {
                  Element view = (Element)views.item(k);

                  // View mappings for this action
                  NodeList viewMapping = view.getChildNodes();
                  StringBuffer mapping = new StringBuffer();
                  for (int l = 0; l < viewMapping.getLength(); l++)
                  {
                     Node mappingNode = viewMapping.item(l);
                     if (mappingNode instanceof Text)
                     {
                        mapping.append(mappingNode.getNodeValue());
                     }
                  }

                  String commandViewName;
                  if (commandAlias.equals(""))
                  {
                     if (actionAlias.equals(""))
                        commandViewName = actionName+"!"+commandName+"."+view.getAttribute("name");
                     else
                        commandViewName = actionAlias+"!"+commandName+"."+view.getAttribute("name");
                  }
                  else
                  {
                     commandViewName = commandAlias+"."+view.getAttribute("name");
                  }

                  String commandViewMapping = mapping.toString().trim();
                  logger.debug("Adding command view mapping "+commandViewName+"="+commandViewMapping);
                  infoglueActionMappings.put(commandViewName, commandViewMapping);
               }
            }
         }
      } catch (SAXException e)
      {
    	  logger.error("SAX exception", e);
         throw new IllegalArgumentException("Could not parse XML action configuration");
      } catch (IOException e)
      {
    	  logger.error("IO exception", e);
         throw new IllegalArgumentException("Could not load XML action configuration");
      } catch (ParserConfigurationException e)
      {
    	  logger.error("Parser conf exception", e);
         throw new IllegalArgumentException("Could not load XML action configuration");
      } catch (DOMException e)
      {
    	  logger.error("DOM exception", e);
         throw new IllegalArgumentException("Could not load XML action configuration");
      }
      
      //XMLActionConfiguration.actionMappings;
   }

}
