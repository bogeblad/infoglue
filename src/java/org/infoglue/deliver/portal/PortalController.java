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
package org.infoglue.deliver.portal;

import javax.naming.NameNotFoundException;
import javax.portlet.PortletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.portalImpl.services.ServiceManager;
import org.infoglue.deliver.portal.services.PortletWindowRegistryService;
import org.infoglue.deliver.util.ThreadMonitor;

/**
 * @author robert lerner
 * @author jan danils
 * @author jöran stark
 */
public class PortalController 
{
    public static final String NAME = "portalLogic";

    private final static Logger logger = Logger.getLogger(PortalController.class.getName());

    private HttpServletRequest request;

    private HttpServletResponse response;

    public PortalController(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * Render portlet
     * 
     * @param portletID
     *            Unique identifier (myPortlet.MyPortletName of the portlet to
     *            be rendered
     * @param windowId
     *            Unique identifier of the window that is to be rendered, if a
     *            PortletWindow with windowId don't exist in the registry it's
     *            created and added to the registry.
     * @return the content the portlet produces
     * @throws PortalException
     */
    public String renderFragment(String portletID, String windowID) throws NameNotFoundException, PortalException 
    {
        PortletWindowIG window = getPortletWindow(portletID, windowID);
        
        return window.render();
    }

    /**
     * Gets a PortletWindowIG for a portlet instance.
     * 
     * @param portletID
     *            identifies the portlet instance.
     * @param windowId
     *            Unique identifier of the window that is to be rendered, if a
     *            PortletWindow with windowId don't exist in the registry it's
     *            created and added to the registry.
     * @return a PortletWindowIG for a portlet instance
     * @throws NameNotFoundException
     *             thrown if the identifier is not bound to a portlet instance
     * @throws PortalException
     *             thrown in case of an exception while initializing the
     *             portlet.
     */
    public PortletWindowIG getPortletWindow(String portletID, String windowID) throws NameNotFoundException, PortalException 
    {
        try 
        {
        	if(portletID == null || portletID.equals(""))
        		throw new NameNotFoundException("Undefined or empty portletID not allowed");
        	
            PortletWindowRegistryService windowService = (PortletWindowRegistryService) ServiceManager.getService(PortletWindowRegistryService.class);
            PortletWindow renderWindow = windowService.createPortletWindow(windowID, portletID);
            logger.info("Portlet window of " + portletID + "," + windowID + ": " + renderWindow);
            return new PortletWindowIGImpl(renderWindow, request, response);
        } 
        catch (NameNotFoundException e) 
        {
        	logger.error("Could not find portlet by ID:" + portletID + " and windowID:" + windowID + " on URL:" + request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
        	throw new PortalException(e);
		}
        catch (PortletException e) 
        {
        	logger.error("Error:" + e.getMessage(), e);
            throw new PortalException(e);
        } 
        catch (PortletContainerException e) 
        {
        	logger.error("Error:" + e.getMessage(), e);
            throw new PortalException(e);
        } 
        catch (Throwable e) 
        {
        	logger.error("Error:" + e.getMessage(), e);
            throw new PortalException(e);
        }
    }
}