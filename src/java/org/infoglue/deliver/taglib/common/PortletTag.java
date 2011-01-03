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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.apache.log4j.Logger;
import org.apache.pluto.om.window.PortletWindow;
import org.infoglue.deliver.portal.PortletWindowIG;
import org.infoglue.deliver.taglib.TemplateControllerTag;

/**
 * This class implements the &lt;common:urlBuilder&gt; tag, which creates an url
 * from a base url (user supplied or taken from the request), 
 * a query string (user supplied ot taken from the reuest) and
 * any number of parameters specified using nested &lt;common:parameter&gt; tags.
 */
public class PortletTag extends TemplateControllerTag 
{

    private final static Logger logger = Logger.getLogger(PortletTag.class.getName());

	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 4433903132736259601L;

	/**
	 * The base url to use when constructing the url.
	 */
	private String portletName;
	
	/**
	 * The query to use when constructing the url.
	 */
	private String action;
	
	/**
	 * The attributes to use when calling the portlet.
	 */
	private Map<String,Object> attributes = new HashMap<String,Object>();

	/**
	 * The attributes to use when calling the portlet.
	 */
	private Map<String,String> parameters = new HashMap<String,String>();

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
	 * Generates the url and either sets the result attribute or writes the url
	 * to the output stream. 
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
		try
		{
			logger.info("portletName:" + portletName);
			logger.info("ID:" + "p" + getController().getComponentContentId());
			logger.info("action:" + action);
			
			PortletWindowIG pw = getPortalController().getPortletWindow(portletName, "p" + getController().getComponentContentId());
			pw.setParameter("action", action);
			
			Iterator<String> parametersIterator = parameters.keySet().iterator();
			while(parametersIterator.hasNext())
			{
				String parameterName = parametersIterator.next();
				pw.setParameter(parameterName, parameters.get(parameterName));
			}
	
			Iterator<String> attributesIterator = attributes.keySet().iterator();
			while(attributesIterator.hasNext())
			{
				String attributeName = attributesIterator.next();
				pw.setAttribute(attributeName, attributes.get(attributeName));
			}

			produceResult("" + pw.render());
		}
		catch (Exception e) 
		{
			produceResult("Error in portlet:" + e.getMessage());
		}
		
		portletName = null;
		action = null;
		attributes.clear();
		attributes = new HashMap<String,Object>();
		parameters.clear();
		parameters = new HashMap<String,String>();
		
		return EVAL_PAGE;
    }
	
	protected final void addAttribute(final String name, final Object value, final String scope)
	{
		if(scope != null && scope.equalsIgnoreCase("parameter"))
			this.parameters.put(name, value.toString());
		else
			this.attributes.put(name, value);
	}

	public void setPortletName(final String portletName) throws JspException
	{
		this.portletName = evaluateString("portlet", "portletName", portletName);
	}

	public void setAction(final String action) throws JspException
	{
		this.action = evaluateString("portlet", "action", action);
	}
	
}
