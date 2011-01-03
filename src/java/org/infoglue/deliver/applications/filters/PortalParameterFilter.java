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
package org.infoglue.deliver.applications.filters;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * @author jand
 *
 */
public class PortalParameterFilter implements Filter 
{
    private static final Log log = LogFactory.getLog(PortalParameterFilter.class);

    private static String FILTER_URIS_PARAMETER = "FilterURIs";

    private FilterConfig filterConfig = null;
    private URIMatcher uriMatcher = null;

    private boolean active = true;

    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException 
    {
        String portalEnabled = CmsPropertyHandler.getEnablePortal();
        active = ((active) && (portalEnabled != null) && portalEnabled.equalsIgnoreCase("true"));

        this.filterConfig = filterConfig;
        String filterURIs = filterConfig.getInitParameter(FILTER_URIS_PARAMETER);
        uriMatcher = URIMatcher.compilePatterns(splitString(filterURIs, ","), false);

        log.info("PortalParameterFilter is active: " + active);
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException 
    {
        HttpServletRequest httpRequest = (HttpServletRequest)req;
        HttpServletResponse httpResponse = (HttpServletResponse)resp;

        if (active) 
        {        	
            String requestURI = URLDecoder.decode(getContextRelativeURI(httpRequest), "UTF-8");

            if (!uriMatcher.matches(requestURI)) 
            {
            	log.debug("wrapping " + ((HttpServletRequest) req).getRequestURI());
            	chain.doFilter(new PortalServletRequest((HttpServletRequest) req), resp);
            }
            else
                chain.doFilter(req, resp);
        } 
        else 
        {
            chain.doFilter(req, resp);
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() 
    {

    }

    
    private String getContextRelativeURI(HttpServletRequest request) 
    {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && requestURI.length() > 0) 
        {
            requestURI = requestURI.substring(contextPath.length(), requestURI.length());
        }
        
        if (requestURI.length() == 0)
            return "/";
        
        return requestURI;
    }
    
    private String[] splitString(String str, String delimiter) 
    {
        List list = new ArrayList();
        StringTokenizer st = new StringTokenizer(str, delimiter);
        while (st.hasMoreTokens()) 
        {
            // Updated to handle portal-url:s
            String t = st.nextToken();
            if (t.startsWith("_")) 
            {
                break;
            } 
            else 
            {
                // Not related to portal - add
                list.add(t.trim());
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

}
