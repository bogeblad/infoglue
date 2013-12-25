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
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.infoglue.cms.services.ExtensionFilterService;

/**
 * This filter is more of a plugin filter where any extension can add logic. The reason is you don't want to 
 * mess with the core web.xml or similar to extend Infoglue.
 *
 * @author Mattias Bogeblad
 */

public class ExtensionFilter implements Filter 
{
    public final static Logger logger = Logger.getLogger(ExtensionFilter.class.getName());
 
    private Map<Pattern, Filter> filters = null;

    public void init(FilterConfig filterConfig) throws ServletException 
    {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException 
    {
    	if(filters == null)
    	{
    		this.filters = ExtensionFilterService.getService().getFilters();
        	if(logger.isInfoEnabled())
    			logger.info("filters: " + filters);
    	}
    	if(logger.isInfoEnabled())
    		logger.info("filters:" + filters);
    	
        HttpServletRequest hsr = (HttpServletRequest) request;
        String path = hsr.getRequestURI().replaceFirst(hsr.getContextPath(), "");
        ExtensionFilterChain extensionChain = new ExtensionFilterChain(chain);

        for (Entry<Pattern, Filter> entry : filters.entrySet()) 
        {
        	logger.info("entry.getKey().matches(path):" + entry.getKey() +  "=" + path);
            if (entry.getKey().matches(path)) 
            {
            	extensionChain.addFilter(entry.getValue());
            }
        }

        extensionChain.doFilter(request, response);
    }

    @Override
    public void destroy() 
    {
        for (Filter filter : filters.values()) {
            filter.destroy();
        }
    }
    
}