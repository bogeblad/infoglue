/*
 * Copyright 2003,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* 

 */

package org.infoglue.deliver.portal.information;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainerServices;
import org.apache.pluto.core.impl.PortletRequestImpl;
import org.apache.pluto.factory.PortletObjectAccess;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.log.LogService;
import org.apache.pluto.services.log.Logger;

/**

 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class RenderRequestIG extends PortletRequestImpl implements RenderRequest 
{
    private static final Log LOG = LogFactory.getLog(RenderRequestIG.class);

    /**
     * Holds the portlet preferences
     */
    
    private PortletPreferences portletPreferences = null;
    private Logger log = null;

    private HttpServletRequest origRequest;

    public RenderRequestIG(PortletWindow portletWindow, HttpServletRequest servletRequest) 
    {
        super(portletWindow, servletRequest);
        this.log = ((LogService) PortletContainerServices.get(LogService.class)).getLogger(getClass());

        log.debug("Original servletRequest of type [" + servletRequest.getClass().getName() + "]");
        origRequest = servletRequest;
    }

    // additional methods -------------------------------------------------------------------------
    /**
     * @see javax.servlet.ServletRequest#getReader()
     */
    
    public BufferedReader getReader() throws IOException 
    {
        return super.getReader();
    }

    public PortletPreferences getPreferences() 
    {
        if (log.isDebugEnabled()) 
        {
            log.debug("Getting Preferences: " + portletPreferences);
        }

        if (portletPreferences == null) 
        {
            portletPreferences = PortletObjectAccess.getPortletPreferences(org.apache.pluto.Constants.METHOD_RENDER, super.getInternalPortletWindow().getPortletEntity());
        }
        
        if (log.isDebugEnabled()) 
        {
            log.debug("Returning Preferences: " + portletPreferences);
            Enumeration e = portletPreferences.getNames();
            while (e.hasMoreElements()) 
            {
                String name = (String) e.nextElement();
                log.debug(" - Preference: name = " + name);
            }
        }

        return portletPreferences;
    }
    // --------------------------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) 
    {
        Object o = super.getAttribute(name);
        if (o == null) {
            o = super.getRequest().getAttribute(name);
        }
        return o;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getAttributeNames()
     */
    public Enumeration getAttributeNames() 
    {
        Vector v = new Vector();

        for (Enumeration enumeration = super.getAttributeNames(); enumeration.hasMoreElements();) {
            v.add(enumeration.nextElement());
        }
        for (Enumeration enumeration = super.getRequest().getAttributeNames(); enumeration.hasMoreElements();) {
            v.add(enumeration.nextElement());
        }
        return v.elements();
    }

    /*
    public String getParameter(String name) {
        String result = super.getParameter(name);
        if (result == null) {
            result = origRequest.getParameter(name);
        }
        return result;
    }
    
    public Map getParameterMap() {
        Map map = super.getParameterMap();
        Map queryMap = origRequest.getParameterMap();
        queryMap.putAll(map);
        return queryMap;
    }
    
    public Enumeration getParameterNames() {
        Enumeration superEnum = super.getParameterNames();
        Enumeration origEnum = origRequest.getParameterNames();
        Vector v = new Vector();
        
        while (superEnum.hasMoreElements()) {
            String name = (String) superEnum.nextElement();
            if (!v.contains(name)) {
                v.add(name);
            }
        }        
        while (origEnum.hasMoreElements()) {
            String name = (String) origEnum.nextElement();
            if (!v.contains(name)) {
                v.add(name);
            }
        }
        
        return v.elements();
    }
    
    public String[] getParameterValues(String name) {
        String[] str = super.getParameterValues(name);
        if (str == null) {
            str = origRequest.getParameterValues(name);
        }
        return str;
    }*/
}
