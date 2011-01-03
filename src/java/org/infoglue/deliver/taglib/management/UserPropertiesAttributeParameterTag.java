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
package org.infoglue.deliver.taglib.management;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.deliver.taglib.AbstractTag;

/**
 * This class implements the &lt;content:contentVersionAttributeParameter&gt; tag, which adds a attributes
 * to the list of attributes in the contentVersion.
 *
 *  Note! This tag must have a &lt;content:contentVersionParameter&gt; ancestor.
 */
public class UserPropertiesAttributeParameterTag extends AbstractTag 
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
	private String value;
	
	/**
	 * Default constructor. 
	 */
	public UserPropertiesAttributeParameterTag()
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
		addUserPropertiesAttribute();
		return EVAL_PAGE;
    }


	/**
	 * Adds the content version to the ancestor tag.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	protected void addUserPropertiesAttribute() throws JspException
	{
		final RemoteUserPropertiesServiceTag parent = (RemoteUserPropertiesServiceTag) findAncestorWithClass(this, RemoteUserPropertiesServiceTag.class);
		if(parent == null)
		{
			throw new JspTagException("UserPropertiesAttributeParameterTag must have a RemoteUserPropertiesServiceTag ancestor.");
		}
		((RemoteUserPropertiesServiceTag) parent).addUserPropertiesAttribute(name, value);
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
}
