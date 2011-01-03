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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.deliver.taglib.AbstractTag;

/**
 * This class implements the &lt;common:portletAttribute&gt; tag, which adds an attribute
 * to the attributes of the parent portlet tag.
 *
 *  Note! This tag must have a &lt;common:portlet&gt; ancestor.
 */
public class PortletAttributeTag extends AbstractTag 
{
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
	private Object value;
	
	private String scope = "attribute";
	
	/**
	 * Default constructor. 
	 */
	public PortletAttributeTag()
	{
		super();
	}

	/**
	 * Adds an attribute with the specified name and value to the attributes
	 * of the parent tag.
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
		addAttribute();
		
		name = null;
		value = null;
		scope = "attribute";
		
		return EVAL_PAGE;
    }
	
	/**
	 * Adds the attribute to the ancestor tag.
	 * 
	 * @throws JspException if the ancestor tag isn't a portlet tag.
	 */
	protected void addAttribute() throws JspException
	{
		PortletTag portletParent = (PortletTag) findAncestorWithClass(this, PortletTag.class);
		if(portletParent == null)
		{
			throw new JspTagException("PortletTag must either have a portletTag ancestor.");
		}
		
		if(portletParent != null)
			((PortletTag) portletParent).addAttribute(name, value, scope);
	}
	
	/**
	 * Sets the name attribute.
	 * 
	 * @param name the name to use.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setName(final String name) throws JspException
	{
		this.name = evaluateString("portletAttribute", "name", name);
	}

	/**
	 * Sets the value attribute.
	 * 
	 * @param value the value to use.
	 * @throws JspException if an error occurs while evaluating value parameter.
	 */
	public void setValue(final String value) throws JspException
	{
		this.value = evaluate("portletAttribute", "value", value, Object.class);
	}

	/**
	 * Sets the value attribute.
	 * 
	 * @param value the value to use.
	 * @throws JspException if an error occurs while evaluating value parameter.
	 */
	public void setScope(final String scope) throws JspException
	{
		this.scope = evaluateString("portletAttribute", "scope", scope);
	}
}
