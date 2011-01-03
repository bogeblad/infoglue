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

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.RedirectController;
import org.infoglue.cms.exception.SystemException;

/**
 * This filters urls registered to be redirected.
 *
 * @author Mattias Bogeblad
 */

public class RedirectFilter implements Filter 
{
    public final static Logger logger = Logger.getLogger(RedirectFilter.class.getName());

    private static String FILTER_URIS_PARAMETER = "FilterURIs";
    
    private FilterConfig filterConfig = null;
    private URIMatcher uriMatcher = null;
 
 
    public void init(FilterConfig filterConfig) throws ServletException 
    {
        this.filterConfig = filterConfig;
        String filterURIs = filterConfig.getInitParameter(FILTER_URIS_PARAMETER);
        uriMatcher = URIMatcher.compilePatterns(splitString(filterURIs, ","), false);
   }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException 
    {       
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        String requestURL = httpRequest.getRequestURL().toString();

        logger.info("Redirect filter requestURL:" + requestURL);
        
        try
        {
            String requestURI = URLDecoder.decode(getContextRelativeURI(httpRequest), "UTF-8");

            if (!uriMatcher.matches(requestURI)) 
            {
    	        try
    	        {
    	            String redirectUrl = RedirectController.getController().getRedirectUrl(httpRequest);
    	            if(redirectUrl != null && redirectUrl.length() > 0)
    	            {
    	                httpResponse.sendRedirect(redirectUrl);
    	                return;
    	            }	            
    	        }
    	        catch (SystemException se) 
    	        {
    	        	logger.warn("Error in redirect filter: " + se.getMessage(), se);
    	        	httpRequest.setAttribute("responseCode", "500");
    	            httpRequest.setAttribute("error", se);
    	            httpRequest.getRequestDispatcher("/ErrorPage.action").forward(httpRequest, httpResponse);
    	        }
    	        catch (Exception e) 
    	        {
    	        	logger.warn("Error in redirect filter: " + e.getMessage(), e);
    	            httpRequest.setAttribute("responseCode", "404");
    	            httpRequest.setAttribute("error", e);
    	            httpRequest.getRequestDispatcher("/ErrorPage.action").forward(httpRequest, httpResponse);
    	        }
            }
        }
        catch (Exception e)
        {
            logger.error("RedirectFilter threw error:" + e.getMessage(), e);
        }
        
        filterChain.doFilter(httpRequest, httpResponse);

    }

    public void destroy() 
    {
        this.filterConfig = null;
    }


    // @TODO should I URLDecode the strings first? (incl. context path)
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
        List<String> list = new ArrayList<String>();
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