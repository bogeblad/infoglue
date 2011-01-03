/*
 * ===============================================================================
 * 
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 * 
 * ===============================================================================
 * 
 * Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 * 
 * ===============================================================================
 */
package org.infoglue.deliver.portal;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.portlet.PortletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.portalImpl.core.PortletContainerFactory;
import org.apache.pluto.portalImpl.servlet.ServletObjectAccess;
import org.apache.pluto.portalImpl.servlet.ServletResponseImpl;
import org.infoglue.cms.security.InfoGluePrincipal;

class PortletWindowIGImpl implements PortletWindowIG 
{
    private final Log log = LogFactory.getLog(PortletWindowIGImpl.class);

    private HttpServletResponse response;

    private HttpServletRequest wrappedRequest;

    private PortletWindow renderWindow;

    private PortletContainer portletContainer;

    PortletWindowIGImpl(PortletWindow window, HttpServletRequest request, HttpServletResponse response) throws PortletException, PortletContainerException 
    {
        this.response = response;
        this.renderWindow = window;

        // Locate portlet container
        portletContainer = PortletContainerFactory.getPortletContainer();
        if (portletContainer == null) 
        {
            log.error("Portlet container not found!");
        }

        // -- Ask portlet container to load portlet
        wrappedRequest = ServletObjectAccess.getServletRequest(request, renderWindow);

        log.debug("Loading portlet: " + renderWindow);
        portletContainer.portletLoad(renderWindow, wrappedRequest, response);
        log.debug("Loading OK!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.infoglue.cms.portal.PortletWindowIG#render()
     */
    
    public String render() throws PortalException 
    {
        log.debug("render(" + renderWindow.getPortletEntity().getId() + ", " + renderWindow.getId() + ") invoked");
        
        try 
        {
            // Create a buffered response to "catch" output from rendering the
            // portlet
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ServletResponseImpl wrappedResponse = (ServletResponseImpl) ServletObjectAccess.getStoredServletResponse(response, pw);
            
    		InfoGluePrincipal infogluePrincipal = (InfoGluePrincipal)wrappedRequest.getSession().getAttribute("infogluePrincipal");
            if(infogluePrincipal != null)
    		{
            	wrappedRequest.setAttribute("infogluePrincipal", infogluePrincipal);
            	wrappedRequest.setAttribute("infoglueRemoteUser", infogluePrincipal.getName());
            	wrappedRequest.setAttribute("cmsUserName", infogluePrincipal.getName());
    		}
            
            // -- Ask portlet container to render the portlet (into buffer)
            portletContainer.renderPortlet(renderWindow, wrappedRequest, wrappedResponse);
            log.debug("Rendering OK!");
            
            // Return the contents of rendering
            String contents = sw.toString();
            if (contents.length() == 0) 
            {
                log.warn("Rendering generated an empty string");
            }
            
            log.debug("render(" + renderWindow.getPortletEntity().getId() + ", " + renderWindow.getId() + ") done");

            return contents;
        } 
        catch (Throwable t) 
        {
        	t.printStackTrace();
            throw new PortalException(t);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.infoglue.cms.portal.PortletWindowIG#setParameter(java.lang.String,
     *      java.lang.Object)
     */
    
    public void setAttribute(String key, Object value) 
    {
        wrappedRequest.setAttribute(key, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.infoglue.cms.portal.PortletWindowIG#setParameter(java.lang.String,
     *      java.lang.String[])
     */
    
    public void setParameter(String key, String value) 
    {
        if (wrappedRequest.getParameter(key) == null || wrappedRequest.getParameter(key).equalsIgnoreCase(""))
            wrappedRequest.getParameterMap().put(key, new String[] { value });
    }

}