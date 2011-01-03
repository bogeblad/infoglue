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
package org.infoglue.deliver.portal.dispatcher;

import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainerServices;
import org.apache.pluto.portalImpl.core.PortletContainerEnvironment;
import org.apache.pluto.portalImpl.core.PortletContainerFactory;
import org.apache.pluto.portalImpl.factory.FactoryAccess;
import org.apache.pluto.portalImpl.services.ServiceManager;
import org.apache.pluto.portalImpl.services.factorymanager.FactoryManager;
import org.infoglue.deliver.portal.ServletConfigContainer;

/**
 * Overides the webwork(1) servlet dispatcher in order to initiate the pluto
 * services.
 * 
 * @author robert lerner
 * @author jan danils
 * @author jöran stark
 */
public class PortalServletDispatcher extends DeliveryServletDispatcher {

    private static final Log log = LogFactory.getLog(PortalServletDispatcher.class);

    public static final String PORTLET_CONTAINER_NAME = "portal_container_name";

    private static String uniqueContainerName;

    public void init(ServletConfig config) throws ServletException 
    {
        log.debug("init of servlet");
        // -- delegate to webwork servlet dispatcher init
        super.init(config);

        // -- init of infoglue portal singleton container for the servlet config
        //TODO: un-singleton
        ServletConfigContainer.getContainer().setServletConfig(config);

        // -- start: init the pluto services -
        try 
        {
            ServiceManager.init(config);
        } 
        catch (Throwable exc) 
        {
            log.error("Initialization failed!", exc);
            throw (new javax.servlet.UnavailableException("Initialization of one or more services failed."));
        }

        try 
        {
            ServiceManager.postInit(config);
        } 
        catch (Throwable expos) 
        {
            log.error("Post initialization failed!", expos);
            throw (new javax.servlet.UnavailableException("Post initialization of one or more services failed."));
        }

        if (!PortletContainerFactory.getPortletContainer().isInitialized()) 
        {
            uniqueContainerName = "pluto-" + System.currentTimeMillis();

            if (log.isInfoEnabled())
                log.info("Initializing PortletContainer [" + uniqueContainerName + "]...");

            PortletContainerEnvironment environment = new PortletContainerEnvironment();

            environment.addContainerService(org.apache.pluto.portalImpl.services.log.Log.getService());
            environment.addContainerService(FactoryManager.getService());
            environment.addContainerService(FactoryAccess.getInformationProviderContainerService());
            environment.addContainerService(FactoryAccess.getDynamicTitleContainerService());

            Properties properties = new Properties();

            try 
            {
                PortletContainerFactory.getPortletContainer().init(uniqueContainerName, config, environment, properties);
            } 
            catch (Throwable exc) 
            {
                log.warn("Initialization of the portlet container failed!", exc);
                //                throw (
                //                    new javax.servlet.UnavailableException(
                //                        "Initialization of the portlet container failed."));
            }
        } 
        else if (log.isInfoEnabled()) 
        {
            log.info("PortletContainer already initialized");
        }

        // RSS Portlet test-hack-fix
        //System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.transformer.TransformerImpl");

        log.info("Ready to serve you.");
    }

    public void destroy() 
    {
        super.destroy();

        if (log.isInfoEnabled())
            log.info("Shutting down portlet container. . .");
        try 
        {
            PortletContainerFactory.getPortletContainer().shutdown();

            // destroy all services
            ServiceManager.destroy(getServletConfig());
        } 
        catch (Throwable t) 
        {
            log("Destruction failed!", t);
        }
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException 
    {
        if (log.isDebugEnabled()) 
        {
            log.debug("\n******************************************** infogluePortal service()");
            Enumeration enumeration = req.getParameterNames();
            while (enumeration.hasMoreElements()) 
            {
                String name = (String) enumeration.nextElement();
                Object o = req.getParameter(name);
                log.debug(name + "=" + o);
            }

            enumeration = req.getAttributeNames();
            while (enumeration.hasMoreElements()) 
            {
                String name = (String) enumeration.nextElement();
                Object o = req.getAttribute(name);
                log.debug(name + "=" + o);
            }
        }
        // TODO not very nice, or?. Necessary to call before portlet execution.
        PortletContainerServices.prepare(uniqueContainerName);
        // Necessary to allow deliver parts to update portlet container when a new
        // portlet is uploaded.
        req.setAttribute(PORTLET_CONTAINER_NAME, uniqueContainerName);

        // Delegate to super-servlet (infoglue)
        super.service(req, resp);
    }
    

}