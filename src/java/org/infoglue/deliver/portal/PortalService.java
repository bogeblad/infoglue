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

import javax.portlet.PortletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.portalImpl.core.PortletContainerFactory;
import org.apache.pluto.portalImpl.services.ServiceManager;
import org.apache.pluto.services.information.InformationProviderAccess;
import org.infoglue.deliver.portal.information.DynamicInformationProviderIG;
import org.infoglue.deliver.portal.services.PortletWindowRegistryService;

/**
 * This class handles portlet action requests. Determines if a request is a portlet action
 * and executes the action if so.
 * 
 * @author robert lerner
 * @author jan danils
 * @author jöran stark
 */
public class PortalService 
{
    private static final Log log = LogFactory.getLog(PortalService.class);

    /**
     * Determines wether a portlet action request is beeing sent and delegates that to the 
     * portlet container.
     * In case of an action request a redirect is issued in order to encode state parameters in
     * the path-part of the url.
     * @param request
     * @param response
     * @return true if an action request is sent false otherwise.
     * @throws PortalException
     */
    
    public boolean service(HttpServletRequest request, HttpServletResponse response) throws PortalException 
    {
        log.debug("*** service start");
        ServletConfig cfg = ServletConfigContainer.getContainer().getServletConfig();
        if (cfg == null) 
        {
            throw new RuntimeException("ServletConfig is null (the PortalServletDispatcher should initiate it)");
        }

        // -- Check if the request is a portlet action...
        String portletWindowId = null;
        
        try 
        {
            DynamicInformationProviderIG provider = (DynamicInformationProviderIG) InformationProviderAccess.getDynamicProvider(request);
            portletWindowId = provider.getPortalURL().getActionWindowID();
        } 
        catch (Throwable e) 
        {
            log.error("Failed to locate DynamicInformationProviderIG", e);
            throw new PortalException(e);
        }

        // Do we have a portlet-action?
        if (portletWindowId == null) 
        {
            log.debug("null actionwindow - no actionrequest returning false");
            return false;
        } 
        else 
        {
            log.debug("actionwindow found [" + portletWindowId + "]");
        }

        // Locate portlet-window instance
        PortletWindowRegistryService windowService = (PortletWindowRegistryService) ServiceManager.getService(PortletWindowRegistryService.class);
        PortletWindow actionWindow = windowService.getPortletWindow(portletWindowId);

        if (actionWindow == null) 
        {
            log.error("PortletWindow action requested but not found: " + portletWindowId);
            return false;
        }

        // -- now that we got everything we need - send the action to the portletcontainer
        try 
        {
            log.debug("ask container to process portlet action");
    		PortletContainerFactory.getPortletContainer().processPortletAction(actionWindow, request, response);
            log.debug("action sent and executed without exception");
        } 
        catch (PortletException e) 
        {
            log.error("PortletException: ", e);
            throw new PortalException(e);
        } 
        catch (PortletContainerException e) 
        {
            log.error("PortletContainerException: ", e);
            throw new PortalException(e);
        }
        // This catch block is for compliance
        // of TCK's Portlet.ProcessActionIOExceptionTest
        catch (Exception e) 
        {
            log.error("Unknown exception [" + e.getClass().getName() + "]", e);
            throw new PortalException(e);
        }

        return true;
    }

}