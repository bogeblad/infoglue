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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.om.window.PortletWindow;

/**
 * Represents an portal URL including control parameters.
 * 
 * @author jand
 * @author robert 
 */
public class PortalControlURL {
    private static final Log log = LogFactory.getLog(PortalControlURL.class);

    public static final String IG = "_ig_";
    public static final String RENDER = "_rp_";
    public static final String ACTION = "_ac";
    public static final String PID = "_pid";
    public static final String MULTI_VALUE = "__";

    private String contextPath;
    private String realPath;
    private String webWorkAction;
    private Map queryParams; // ?<str>
    private Map pathParams;
    private String local; // #<str>

    /**
     * Constructor
     * 
     * @param req current request
     */
    public PortalControlURL(HttpServletRequest req) {
        // TODO ok to unwrap request? It seems it must be done
        while (req instanceof HttpServletRequestWrapper) {
            req = (HttpServletRequest) ((HttpServletRequestWrapper) req).getRequest();
        }
        this.contextPath = req.getContextPath();

        String path = req.getRequestURI();
        if (contextPath != null && path.length() > 0) {
            path = path.substring(contextPath.length(), path.length());
        }

        if (path.endsWith(".action")) {
            int last = path.lastIndexOf("/");
            this.webWorkAction = path.substring(last + 1);
            path = path.substring(0, last);
        }

        this.realPath = PathParser.getRealPath(path);
        this.queryParams = PathParser.copyParameters(req.getParameterMap());
        this.pathParams = PathParser.parsePathParameters("_", path, true);
        log.debug("realPath: " + realPath);
        log.debug("wwAction: " + webWorkAction);
    }

    /**
     * Copy-constructor
     * 
     * @param url copy this portal control URL
     */
    public PortalControlURL(PortalControlURL url) {
        this.contextPath = url.contextPath;
        this.realPath = url.realPath;
        this.webWorkAction = url.webWorkAction;
        this.queryParams = new HashMap(url.queryParams.size());
        this.queryParams.putAll(url.queryParams);
        this.pathParams = new HashMap(url.pathParams.size());
        this.pathParams.putAll(url.pathParams);
    }

    public PortletMode getPortletMode(PortletWindow window) {
        //      TODO fixme 
        return PortletMode.VIEW;
    }
    public PortletMode getPreviousPortletMode(PortletWindow portletWindow) {
        //      TODO fixme 
        return PortletMode.VIEW;
    }

    public WindowState getWindowState(PortletWindow portletWindow) {
        //      TODO fixme 
        return WindowState.NORMAL;
    }

    public WindowState getPreviousWindowState(PortletWindow portletWindow) {
        //      TODO fixme 
        return WindowState.NORMAL;
    }

    public void setPortletMode(PortletWindow window, PortletMode mode) {
        // TODO implement
    }

    public void setPortletWindowState(PortletWindow window, WindowState state) {
        // TODO implement
    }

    public void setPortletId(PortletWindow window) {
        setPathParameter(PID, new String[] { window.getId().toString()});
    }

    // general param

    public void setPathParameter(String nsName, String[] values) {
        if (values == null || values.length == 0) {
            pathParams.remove(nsName);
        } else {
            pathParams.put(nsName, values);
        }
    }

    // --- ACTION param
    
    /**
     * Get the window-id of current request.
     * 
     * @return window-id or null if no action.
     */
    public String getActionWindowID() {
        String[] values = (String[]) pathParams.get(ACTION);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return null;
    }

    public void setActionParameter(PortletWindow window) {
        setPathParameter(ACTION, new String[] { window.getId().toString()});
    }

    public void clearActionParameter() {
        pathParams.remove(ACTION);
        pathParams.remove(PID);
    }

    
    // --- RENDER params
    
    /**
     * Get all render parameters of a window.
     * Note: the resulting Map will not contain namespaced parameters.
     * 
     * @param window Window
     */
    public Map getRenderParameterMap(PortletWindow window) {
        Map result = new HashMap();
        int nsSize = RENDER.length() + window.getId().toString().length() + 1;
        for (Iterator iter = pathParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            if (name.startsWith(RENDER + window.getId())) {
                String paramName = name.substring(nsSize);
                result.put(paramName, pathParams.get(name));
            }
        }
        return result;
    }

    /**
     * Set render parameter of window.
     *  
     * @param window Window
     * @param name Parameter-name (not namespaced)
     * @param values Parameter-values
     */
    public void setRenderParameter(PortletWindow window, String name, String[] values) {
        String nsName = RENDER + window.getId() + "_" + name;
        setPathParameter(nsName, values);
    }

    /**
     * Clear render parameters of window.
     * 
     * @param window Window
     */
    public void clearRenderParameters(PortletWindow window) {
        for (Iterator iter = pathParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            if (name.startsWith(RENDER + window.getId())) {
                //pathParams.remove(name);
                iter.remove();
            }
        }
    }

    // QUERY params

    /**
     * Get all query (action) parameters of a window.
     * Note: the resulting Map will _not_ contain namespaced parameters.
     * 
     * @param window Window
     */
    public Map getQueryParameterMap(PortletWindow window) {
        return queryParams;
    }

    /**
     * Get all query (action) parameters of a window.
     * Note: the resulting Map will contain namespaced parameters.
     * 
     * @param window Window
     */
    public Map getQueryParameterMap() {
        return queryParams;
    }

    /**
     * Set query (action) parameter of window.
     * Note: current implementation does not namespace parameters
     *  
     * @param window Window
     * @param name Parameter-name (not namespaced)
     * @param values Parameter-values
     */
    public void setQueryParameter(PortletWindow window, String name, String[] values) {
        String nsName = name; // Prepared for namespaced params
        if (values == null || values.length == 0) {
            queryParams.remove(nsName);
        } else {
            queryParams.put(nsName, values);
        }
    }

    public void clearQueryParameters() {
        queryParams.clear();
    }

    /**
     * Generates an portal URL.
     * Format: 'context/realPath/control-params/infoglueServletPath?query-params#local'
     * where 'control-params': _type_pid_name/[value | __value1/__value2]/...
     * separated by '/'.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();

        // Append context-path
        buf.append(contextPath);

        // Append real path (path besides control params, think "niceuri")
        if (realPath != null) {
            buf.append(realPath);
        }

        // Append "control-parameters"
        if (!pathParams.isEmpty()) {
            buf.append("/");
            for (Iterator iter = pathParams.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String[] values = (String[]) pathParams.get(name);
                buf.append(name);
                buf.append("/");
                buf.append(PathParser.encodeValues(values));

                if (iter.hasNext()) {
                    buf.append("/");
                }
            }
        }

        // Append servlet-path (infoglue-action)
        if (webWorkAction != null) {
            buf.append("/");
            buf.append(webWorkAction);
        }

        // Append query-parameters
        if (!queryParams.isEmpty()) {
            buf.append("?");
            for (Iterator iter = queryParams.keySet().iterator(); iter.hasNext();) {
                // Iterating over names
                String name = (String) iter.next();
                String[] values = (String[]) queryParams.get(name);
                for (int i = 0; i < values.length; i++) {
                    // Iterating over values
                    buf.append(name);
                    buf.append("=");
                    buf.append(values[i]);
                    if (i < values.length - 1) {
                        buf.append("&");
                    }
                }
                if (iter.hasNext()) {
                    buf.append("&");
                }
            }
        }

        // Append local navigation (reference)
        if (local != null && local.length() > 0) {
            buf.append("#");
            buf.append(local);
        }
        if (log.isDebugEnabled()) {
            log.debug("Generated URL: " + buf.toString());
        }
        return buf.toString();
    }

    /**
     * @return Returns the targeted.
     */
    public boolean isTargeted() {
        return pathParams.containsKey(ACTION) || pathParams.containsKey(PID);
        //return targeted;
    }
}
