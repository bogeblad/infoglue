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

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.om.window.PortletWindow;
import org.infoglue.deliver.portal.PortalControlURL;

/**
 * @author jand
 *
 */
public class ServletRequestIG extends HttpServletRequestWrapper {
    private static final Log log = LogFactory.getLog(ServletRequestIG.class);

    private Map paramMap;
    private Map attributeMap = new HashMap();

    public ServletRequestIG(PortletWindow window, HttpServletRequest req) {
        super(req);
        PortalControlURL url = new PortalControlURL(req);
        this.paramMap = url.getRenderParameterMap(window);
        this.paramMap.putAll(url.getQueryParameterMap(window)); // in case of namespace-params.

        if (log.isDebugEnabled()) {
            StringBuffer str = new StringBuffer();
            for (Iterator it = paramMap.keySet().iterator(); it.hasNext();) {
                String name = (String) it.next();
                str.append(name);
                str.append(": ");
                str.append(Arrays.asList((String[]) paramMap.get(name)));
                if (it.hasNext())
                    str.append(", ");
            }
            log.debug("Available params: " + str);
        }
    }

    protected void finalize() throws Throwable {
  	  super.finalize();
  	  this.paramMap.clear();
  	  this.paramMap = null;

  	  this.attributeMap.clear();
  	  this.attributeMap = null;
}

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */
    public String getParameter(String name) {
        String[] values = (String[]) paramMap.get(name);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {
        return paramMap;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    public Enumeration getParameterNames() {
        return Collections.enumeration(paramMap.keySet());
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String name) {
        return (String[]) paramMap.get(name);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String key, Object value) 
    {
    	this.attributeMap.put(key, value);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String key) 
    {
    	this.attributeMap.remove(key);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        Object o = attributeMap.get(name);
        if (o == null) {
            o = super.getAttribute(name);
        }
        if (o == null) {
            o = super.getRequest().getAttribute(name);
        }
        return o;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        Vector v = new Vector();

        for (Iterator iter = attributeMap.keySet().iterator(); iter.hasNext();) {
            v.add(iter.next());
        }
        for (Enumeration enumeration = super.getAttributeNames(); enumeration.hasMoreElements();) {
            v.add(enumeration.nextElement());
        }
        for (Enumeration enumeration = super.getRequest().getAttributeNames(); enumeration.hasMoreElements();) {
            v.add(enumeration.nextElement());
        }
        return v.elements();
    }

}
