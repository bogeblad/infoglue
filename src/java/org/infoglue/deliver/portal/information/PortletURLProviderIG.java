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

import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.PortletURLProvider;
import org.infoglue.deliver.portal.PortalControlURL;

/**
 * 
 * @author jand
 *
 */
public class PortletURLProviderIG implements PortletURLProvider {
    // TODO why can't I create a logger here?
    // this will throw an commons-logger exception
    //private static final Log log = LogFactory.getLog(PortletURLProviderIG.class);

    private DynamicInformationProviderIG provider;
    private PortletWindow portletWindow;
    private PortletMode mode;
    private WindowState state;
    private boolean action;
    private boolean secure;
    private boolean clearParameters;
    private Map parameters;

    public PortletURLProviderIG(
        DynamicInformationProviderIG provider,
        PortletWindow portletWindow) {
        this.provider = provider;
        this.portletWindow = portletWindow;
    }

    // PortletURLProvider implementation.

    public void setPortletMode(PortletMode mode) {
        this.mode = mode;
    }

    public void setWindowState(WindowState state) {
        this.state = state;
    }

    public void setAction() {
        action = true;
    }

    public void setSecure() {
        secure = true;
    }

    public void clearParameters() {
        clearParameters = true;
    }

    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }

    public String toString() {
        PortalControlURL url = provider.getPortalURL();

        if (mode != null) {
            url.setPortletMode(portletWindow, mode);
        }
        if (state != null) {
            url.setPortletWindowState(portletWindow, state);
        }

        if (clearParameters) {
            url.clearRenderParameters(portletWindow);
        }

        /*
         * If the request does not contain a _pid or an _ac parameter
         * it is a general request from infoglue. The parameters on the query
         * is then assumed to be infoglue parameters such as siteNodeId..
         */
        // Render request... set _ig parameters
        if (!url.isTargeted()) {
            Map params = url.getQueryParameterMap();
            for (Iterator it = params.keySet().iterator(); it.hasNext();) {
                String name = (String) it.next();
                String[] values = (String[]) params.get(name);
                url.setPathParameter(PortalControlURL.IG + name, values);
            }
        } 
        
        // set portlet id for associated request parms
        url.clearActionParameter();
        if (action) {
            url.setActionParameter(portletWindow);
        } else {
            url.setPortletId(portletWindow);
        }

        url.clearQueryParameters();

        if (parameters != null) {
            Iterator names = parameters.keySet().iterator();
            while (names.hasNext()) {
                String name = (String) names.next();
                Object value = parameters.get(name);
                String[] values = value instanceof String ? new String[] {(String) value }
                : (String[]) value;
                if (action) {
                    url.setQueryParameter(portletWindow, name, values);
                } else {
                    url.setRenderParameter(portletWindow, name, values);
                }
            }
        }
        String str = url.toString();
        //log.debug("Generated URL: " + str);
        return str;
    }

}
