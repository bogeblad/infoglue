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
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.util.CacheController;

/**
 * This filter adds expires headers on resources.
 * 
 * @author Mattias Bogeblad
 */

public class ResourceFilter implements Filter 
{
	public final static Logger logger = Logger.getLogger(ResourceFilter.class.getName());

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException 
    {  
    	HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

    	String requestURI = req.getRequestURI();
    	
    	if(requestURI.indexOf(".png") > -1 || requestURI.indexOf(".jpg") > -1 || requestURI.indexOf(".gif") > -1)
    	{
    		resp.setDateHeader("Expires", System.currentTimeMillis() + 3000*24*60*60*1000);  

    		// check for the HTTP header that
            // signifies GZIP support
            String ae = req.getHeader("accept-encoding");
            if (ae != null && ae.indexOf("gzip") != -1) 
            {
            	GZIPResponseWrapper wrappedResponse = new GZIPResponseWrapper(resp);
            	chain.doFilter(request, wrappedResponse);
            	wrappedResponse.finishResponse();
            	return;
            }
        }
    	else if(requestURI.indexOf(".js") > -1 || requestURI.indexOf(".css") > -1)
    	{
    		resp.setDateHeader("Expires", System.currentTimeMillis() + 1*24*60*60*1000);  
    		// check for the HTTP header that
            // signifies GZIP support
            String ae = req.getHeader("accept-encoding");
            if (ae != null && ae.indexOf("gzip") != -1) 
            {
            	GZIPResponseWrapper wrappedResponse = new GZIPResponseWrapper(resp);
            	chain.doFilter(request, wrappedResponse);
            	wrappedResponse.finishResponse();
            	return;
            }
        }

        // Continue  
	    chain.doFilter(request, response);  
	}
	
    public void destroy() 
	{
	}

	public void init(FilterConfig arg0) throws ServletException 
	{
	}  
}