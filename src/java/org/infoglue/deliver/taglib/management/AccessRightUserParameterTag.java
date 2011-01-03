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
 * This class implements the &lt;management:accessRightUserParameter&gt; tag, which adds an accessRightUser to an accessRight-tag.
 *
 *  Note! This tag must have a &lt;management:accessRightParameter&gt; ancestor.
 */

public class AccessRightUserParameterTag extends AbstractTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 4482006814634520239L;

	/**
	 * The userName.
	 */
	private String userName;

	
	/**
	 * Default constructor. 
	 */
	public AccessRightUserParameterTag()
	{
		super();
	}

	/**
	 * Adds a userName to the parent access right tag of the parent tag.
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
		addUserName();
		
		this.userName = null;

		return EVAL_PAGE;
    }
	
	/**
	 * Adds the userName to the ancestor tag.
	 * 
	 * @throws JspException if the ancestor tag isn't a content version tag.
	 */
	protected void addUserName() throws JspException
	{
		final AccessRightParameterTag parent = (AccessRightParameterTag) findAncestorWithClass(this, AccessRightParameterTag.class);
		if(parent == null)
		{
			throw new JspTagException("AccessRightUserParameterTag must have a AccessRightParameterTag ancestor.");
		}
		
		((AccessRightParameterTag) parent).addAccessRightUser(userName);
	}
	
	/**
	 * Sets the userName attribute.
	 * 
	 * @param userName the userName to use.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setUserName(final String userName) throws JspException
	{
		this.userName = evaluateString("parameter", "userName", userName);
	}

}
