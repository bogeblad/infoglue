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

import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.portalImpl.servlet.ServletRequestFactory;

/**
 * @author jand
 *
 */
public class ServletRequestFactoryIG implements ServletRequestFactory {
    private static final Log log = LogFactory.getLog(ServletRequestFactoryIG.class);

    /* (non-Javadoc)
     * @see org.apache.pluto.portalImpl.servlet.ServletRequestFactory#getServletRequest(javax.servlet.http.HttpServletRequest, org.apache.pluto.om.window.PortletWindow)
     */
    public HttpServletRequest getServletRequest(
        HttpServletRequest request,
        PortletWindow portletWindow) {
        log.debug("constructor");
        return new ServletRequestIG(portletWindow, request);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.factory.Factory#init(javax.servlet.ServletConfig, java.util.Map)
     */
    public void init(ServletConfig config, Map properties) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.factory.Factory#destroy()
     */
    public void destroy() throws Exception {
    }

}
