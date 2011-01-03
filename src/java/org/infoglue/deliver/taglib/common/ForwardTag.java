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
package org.infoglue.deliver.taglib.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.jsp.JspException;

import org.infoglue.deliver.taglib.TemplateControllerTag;

/**
 * This tag will forward to another page
 */

public class ForwardTag extends TemplateControllerTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 8603406098980150888L;
	
	/**
	 * The cookie name.
	 */
	private String url;
	private Map requestParameters = new HashMap();

	/**
	 * Initializes the parameters to make it accessible for the children tags (if any).
	 * 
	 * @return indication of whether to evaluate the body or not.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doStartTag() throws JspException 
	{
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Process the end tag. Sends the redirect.  
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
		try
		{
		    org.infoglue.deliver.applications.filters.PortalServletRequest filterRequest = (org.infoglue.deliver.applications.filters.PortalServletRequest)this.getController().getDeliveryContext().getHttpServletRequest();
		    
		    Iterator requestParametersIterator = requestParameters.keySet().iterator();
		    while(requestParametersIterator.hasNext())
		    {
		    	String key = (String)requestParametersIterator.next();
		    	String value = (String)requestParameters.get(key);
		    	///System.out.println("key:"+ key + " - " + value);
			    filterRequest.getParameterMap().put(key, new String[]{value});		    	
		    }
		    //System.out.println("originalRequest in forward:" + originalRequest.getClass().getName() + " on " + url);
		    
		    RequestDispatcher dispatcher = this.getController().getDeliveryContext().getHttpServletRequest().getRequestDispatcher(url);
			dispatcher.forward(filterRequest, this.getController().getDeliveryContext().getHttpServletResponse());
		}
		catch (Exception e) 
		{
			throw new JspException("Error when redirecting: " + e.getMessage());
		}
        
		return EVAL_PAGE;
    }

    public void setUrl(String url) throws JspException
    {
        this.url = evaluateString("Forward", "url", url);
    }
    
    protected final void addParameter(final String name, final String value)
	{
		requestParameters.put(name, value);
	}

}
