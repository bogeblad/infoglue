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
package org.infoglue.deliver.portal.information;

import java.util.HashSet;
import java.util.Iterator;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.portalImpl.services.config.Config;
import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.PortletActionProvider;
import org.apache.pluto.services.information.PortletURLProvider;
import org.apache.pluto.services.information.ResourceURLProvider;
import org.infoglue.deliver.portal.PortalControlURL;

/**
 * 
 * @author jand
 *
 */
public class DynamicInformationProviderIG implements DynamicInformationProvider {
    private static final Log log = LogFactory.getLog(DynamicInformationProviderIG.class);

    private final static int NumberOfKnownMimetypes = 15;

    //private HttpServletRequest request;
    private ServletConfig config;
    private PortalControlURL url;

    public DynamicInformationProviderIG(HttpServletRequest request, ServletConfig config) {
        //this.request = request;
        this.config = config;
        this.url = new PortalControlURL(request);
    }

    // DynamicInformationProvider implementation.

    public PortletURLProvider getPortletURLProvider(PortletWindow portletWindow) {
        log.debug("getPortletURLProvider()");
        return new PortletURLProviderIG(this, portletWindow);
    }

    public ResourceURLProvider getResourceURLProvider(PortletWindow portletWindow) {
        log.debug("getResourceURLProvider()");
        return new ResourceURLProviderIG(this, portletWindow);
    }

    public PortletActionProvider getPortletActionProvider(PortletWindow portletWindow) {
        log.debug("getPortletActionProvider()");
        return new PortletActionProviderIG(this, portletWindow);
    }

    public PortletMode getPortletMode(PortletWindow portletWindow) {
        log.debug("getPortletMode()");
        return url.getPortletMode(portletWindow);
    }

    public PortletMode getPreviousPortletMode(PortletWindow portletWindow) {
        log.debug("getPreviousPortletMode()");
        return url.getPreviousPortletMode(portletWindow);
    }

    public WindowState getWindowState(PortletWindow portletWindow) {
        log.debug("getWindowState()");
        return url.getWindowState(portletWindow);
    }

    public WindowState getPreviousWindowState(PortletWindow portletWindow) {
        log.debug("getPreviousWindowState()");
        return url.getPreviousWindowState(portletWindow);
    }

    public String getResponseContentType() {
        log.debug("getResponseContentType()");
        return "text/html";
    }

    public Iterator getResponseContentTypes() {
        log.debug("getResponseContentTypes()");
        HashSet responseMimeTypes = new HashSet(NumberOfKnownMimetypes);
        responseMimeTypes.add("text/html");

        return responseMimeTypes.iterator();
    }

    public boolean isPortletModeAllowed(PortletMode mode) {
        log.debug("isPortletModeAllowed()");
        //checks whether PortletMode is supported as example
        String[] supportedModes = Config.getParameters().getStrings("supported.portletmode");
        for (int i = 0; i < supportedModes.length; i++) {
            if (supportedModes[i].equalsIgnoreCase(mode.toString())) {
                return true;
            }
        }
        return false;
    }

    public boolean isWindowStateAllowed(WindowState state) {
        log.debug("isWindowStateAllowed()");
        //checks whether WindowState is supported as example
        String[] supportedStates = Config.getParameters().getStrings("supported.windowstate");
        for (int i = 0; i < supportedStates.length; i++) {
            if (supportedStates[i].equalsIgnoreCase(state.toString())) {
                return true;
            }
        }
        return false;
    }

    // Others

    /**
     * Return a new portal control URL
     */
    public PortalControlURL getPortalURL() {
        return new PortalControlURL(url);
    }
}
