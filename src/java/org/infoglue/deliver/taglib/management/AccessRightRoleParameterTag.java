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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.deliver.taglib.AbstractTag;

/**
 * This class implements the &lt;management:accessRightRoleParameter&gt; tag, which adds an accessRightRole to an accessRight-tag.
 *
 *  Note! This tag must have a &lt;management:accessRightParameter&gt; ancestor.
 */

public class AccessRightRoleParameterTag extends AbstractTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 4482006814634520239L;

	/**
	 * The roleName.
	 */
	private String roleName;

	
	/**
	 * Default constructor. 
	 */
	public AccessRightRoleParameterTag()
	{
		super();
	}

	/**
	 * Adds a roleName to the parent access right tag of the parent tag.
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
		addRoleName();
		
		this.roleName = null;

		return EVAL_PAGE;
    }
	
	/**
	 * Adds the roleName to the ancestor tag.
	 * 
	 * @throws JspException if the ancestor tag isn't a content version tag.
	 */
	protected void addRoleName() throws JspException
	{
		final AccessRightParameterTag parent = (AccessRightParameterTag) findAncestorWithClass(this, AccessRightParameterTag.class);
		if(parent == null)
		{
			throw new JspTagException("AccessRightRoleParameterTag must have a AccessRightParameterTag ancestor.");
		}
		
		((AccessRightParameterTag) parent).addAccessRightRole(roleName);
	}
	
	/**
	 * Sets the roleName attribute.
	 * 
	 * @param roleName the roleName to use.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setRoleName(final String roleName) throws JspException
	{
		this.roleName = evaluateString("parameter", "roleName", roleName);
	}

}
