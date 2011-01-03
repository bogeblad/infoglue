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

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.deliver.portal.PathParser;
import org.infoglue.deliver.portal.PortalControlURL;

public class PortalServletRequest extends HttpServletRequestWrapper 
{
    private static final Log log = LogFactory.getLog(PortalServletRequest.class);

    public static final String MULTI_VALUE = PathParser.MULTI_VALUE;

    private Map paramMap;
    private Principal principal = null;
    
    /**
     * @param req
     */
    
    public PortalServletRequest(HttpServletRequest req) 
    {
        super(req);
        paramMap = PathParser.copyParameters(req.getParameterMap());
        
        // Extend parameter map with infoglue parameters.
        // TODO paramMap should be immutable
        paramMap.putAll(PathParser.parsePathParameters(PortalControlURL.IG, req.getServletPath(), false));

        if (log.isDebugEnabled()) 
        {
            StringBuffer str = new StringBuffer();
            for (Iterator it = paramMap.keySet().iterator(); it.hasNext();) 
            {
                String name = (String) it.next();
                str.append(name);
                str.append(": ");
                str.append(Arrays.asList((String[]) paramMap.get(name)));
                if (it.hasNext())
                    str.append(", ");
            }
            log.debug("Available params: " + str);
        }
        
		this.principal = (InfoGluePrincipal)req.getSession().getAttribute("infogluePrincipal");
		if(req.getUserPrincipal() != null)
			this.principal = req.getUserPrincipal();
    }
    


    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    
    public Map getParameterMap() 
    {
    	return paramMap;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */
    
    public String getParameter(String name) 
    {
        String[] values = (String[]) paramMap.get(name);
        if (values != null && values.length > 0) 
        {
            return values[0];
        }
    
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    
    public Enumeration getParameterNames() 
    {
        return Collections.enumeration(paramMap.keySet());
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    
    public String[] getParameterValues(String name) 
    {
        return (String[]) paramMap.get(name);
    }
    
    public String getRemoteUser()
    {
    	return (this.principal == null ? null : this.principal.getName());
    }
    
    public boolean isUserInRole(String role)
    {
    	boolean isUserInRole = false;
    	
    	if(this.principal != null)
    	{
    		if(this.principal instanceof InfoGluePrincipal)
    		{	
		    	List roles = ((InfoGluePrincipal)this.principal).getRoles();
		    	Iterator i = roles.iterator();
		    	
		    	while(i.hasNext())
		    	{
		    		InfoGlueRole currentRole = (InfoGlueRole)i.next();
		    		if(currentRole.getName().equals(role))
		    		{
		    			isUserInRole = true;
		    			break;
		    		}
		    	}
    		}
    		else
    		{
    			isUserInRole = super.isUserInRole(role);
    		}
    	}
    	
    	return isUserInRole;
    }
    
    public Principal getUserPrincipal()
    {
    	return this.principal;
    }
}