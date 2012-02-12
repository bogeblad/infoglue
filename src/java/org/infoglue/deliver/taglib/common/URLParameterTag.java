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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.apache.log4j.Logger;
import org.infoglue.deliver.taglib.AbstractTag;

/**
 * This class implements the &lt;common:parameter&gt; tag, which adds a parameter
 * to the parameters of the parent tag.
 *
 *  Note! This tag must have a &lt;common:urlBuilder&gt; ancestor.
 */
public class URLParameterTag extends AbstractTag 
{
    private final static Logger logger = Logger.getLogger(URLParameterTag.class.getName());

	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 4482006814634520239L;

	/**
	 * The name of the parameter.
	 */
	private String name;
	
	/**
	 * The value of the parameter.
	 */
	private String value;

	/**
	 * The value of the parameter.
	 */
	private String scope = "requestParameter";

	private String encodeWithEncoding = null;
	
	/**
	 * Default constructor. 
	 */
	public URLParameterTag()
	{
		super();
	}

	/**
	 * Adds a parameter with the specified name and value to the parameters
	 * of the parent tag.
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
		addParameter();
		return EVAL_PAGE;
    }
	
	public void release()
	{
		this.scope = "requestParameter";
		this.encodeWithEncoding = null;
	}
	
	/**
	 * Adds the parameter to the ancestor tag.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	protected void addParameter() throws JspException
	{
		URLTag urlParent = (URLTag) findAncestorWithClass(this, URLTag.class);
		ImportTag importParent = null;
		if(urlParent == null)
			importParent = (ImportTag) findAncestorWithClass(this, ImportTag.class);
		XSLTransformTag transformParent = null;
		if(urlParent == null && importParent == null)
			transformParent = (XSLTransformTag) findAncestorWithClass(this, XSLTransformTag.class);
		ForwardTag forwardParent = null;
		if(urlParent == null && importParent == null && transformParent == null)
			forwardParent = (ForwardTag) findAncestorWithClass(this, ForwardTag.class);
		ProxyTag proxyParent = null;
		if(urlParent == null && importParent == null && transformParent == null && forwardParent == null)
			proxyParent = (ProxyTag) findAncestorWithClass(this, ProxyTag.class);

		if(urlParent == null && importParent == null && transformParent == null && forwardParent == null && proxyParent == null)
		{
			throw new JspTagException("URLParameterTag must either have a URLTag ancestor, a ImportTag ancestor, a ForwardTag, a ProxyTag ancestor or an XSLTransformTag ancestor.");
		}
		
		if(urlParent != null)
		{
			try
			{
				if(encodeWithEncoding != null && !encodeWithEncoding.equals(""))
					urlParent.addParameter(name, URLEncoder.encode(value, encodeWithEncoding));
				else
					urlParent.addParameter(name, value);
			}
			catch (UnsupportedEncodingException e) 
			{
				logger.warn("Unsupported encoding: [" + encodeWithEncoding + "]: " + e.getMessage());
				urlParent.addParameter(name, value);
			}
		}
		if(importParent != null)
		{
			if(this.scope.equalsIgnoreCase("requestParameter"))
			{
				try
				{
					if(encodeWithEncoding != null && !encodeWithEncoding.equals(""))
						importParent.addParameter(name, URLEncoder.encode(value, encodeWithEncoding));
					else
						importParent.addParameter(name, value);
				}
				catch (UnsupportedEncodingException e) 
				{
					logger.warn("Unsupported encoding: [" + encodeWithEncoding + "]: " + e.getMessage());
					importParent.addParameter(name, value);
				}
			}
			else
				importParent.addProperty(name, value);
		}
		if(transformParent != null)
		{
			transformParent.addParameter(name, value, scope);
		}
		if(forwardParent != null)
		{
			try
			{
				if(encodeWithEncoding != null && !encodeWithEncoding.equals(""))
					forwardParent.addParameter(name, URLEncoder.encode(value, encodeWithEncoding));
				else
					forwardParent.addParameter(name, value);
			}
			catch (UnsupportedEncodingException e) 
			{
				logger.warn("Unsupported encoding: [" + encodeWithEncoding + "]: " + e.getMessage());
				forwardParent.addParameter(name, value);
			}
		}
		if(proxyParent != null)
		{
			try
			{
				if(encodeWithEncoding != null && !encodeWithEncoding.equals(""))
					proxyParent.addParameter(name, URLEncoder.encode(value, encodeWithEncoding), scope);
				else
					proxyParent.addParameter(name, value, scope);
			}
			catch (UnsupportedEncodingException e) 
			{
				logger.warn("Unsupported encoding: [" + encodeWithEncoding + "]: " + e.getMessage());
				proxyParent.addParameter(name, value, scope);
			}
		}
	}
	
	/**
	 * Sets the name attribute.
	 * 
	 * @param name the name to use.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setName(final String name) throws JspException
	{
		this.name = evaluateString("parameter", "name", name);
	}

	/**
	 * Sets the value attribute.
	 * 
	 * @param value the value to use.
	 * @throws JspException if an error occurs while evaluating value parameter.
	 */
	public void setValue(final String value) throws JspException
	{
		this.value = evaluateString("parameter", "value", value);
	}

	/**
	 * Sets the value attribute.
	 * 
	 * @param scope the scope to use. In for example common:import you can use requestProperty instead of the normal dafult requestParameter.
	 * @throws JspException if an error occurs while evaluating value parameter.
	 */
	public void setScope(final String scope) throws JspException
	{
		this.scope = evaluateString("parameter", "scope", scope);
	}
	
	/**
	 * Sets the encodeWithEncoding attribute.
	 * 
	 * @param encodeWithEncoding the encodeWithEncoding to use.
	 * @throws JspException if an error occurs while evaluating encodeWithEncoding parameter.
	 */
	public void setEncodeWithEncoding(final String encodeWithEncoding) throws JspException
	{
		this.encodeWithEncoding = evaluateString("parameter", "encodeWithEncoding", encodeWithEncoding);
	}

}
