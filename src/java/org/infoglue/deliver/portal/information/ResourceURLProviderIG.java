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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.ResourceURLProvider;

/**
 * 
 * @author jand
 *
 */
public class ResourceURLProviderIG implements ResourceURLProvider {
    private static final Log log = LogFactory.getLog(ResourceURLProviderIG.class);

    //private DynamicInformationProviderIG provider;
    private String stringUrl = "";
    private String base = "";

    public ResourceURLProviderIG(DynamicInformationProviderIG provider, PortletWindow window) {
        //this.provider = provider;   
        this.base =
            window
                .getPortletEntity()
                .getPortletDefinition()
                .getServletDefinition()
                .getWebApplicationDefinition()
                .getContextRoot();
        log.debug("window context base: " + this.base);
    }

    // ResourceURLProvider implementation.

    public void setAbsoluteURL(String path) {
        // TODO what the h*ll is this anyway?
        log.debug("setAbsoluteURL(): " + path);
        stringUrl = path;
    }

    public void setFullPath(String path) {
        //TODO what the h*ll is this anyway?
        log.debug("setFullPath(): " + path);
        //stringUrl = base + path;
        stringUrl = path;
    }

    public String toString() {
        log.debug("Generated resourceURL: " + stringUrl);
        return stringUrl;
        /*
            URL url = null;
            if (!"".equals(stringUrl)) {
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException e) {
                throw new java.lang.IllegalArgumentException("A malformed URL has occured");
            }
        }
        
        return ((url == null) ? "" : url.toString());
        */
    }

}
