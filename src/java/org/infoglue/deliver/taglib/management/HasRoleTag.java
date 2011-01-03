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
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.deliver.taglib.TemplateControllerTag;

/**
 * Checks if a user is in a specific <code>role</code>
 * 
 * @author Johan Larsson, Mattias Bogeblad
 */

public class HasRoleTag extends TemplateControllerTag
{
	private static final long serialVersionUID = 4050206323348354355L;

	private String roleName;
	private String userName;

	public HasRoleTag()
	{
		super();
	}

	public int doEndTag() throws JspException
	{
		boolean hasRole = false;
		
		if(roleName != null)
		{
			InfoGluePrincipal infoGluePrincipal = null;
			if(userName == null && !userName.equals(""))
				infoGluePrincipal = getController().getPrincipal();
			else
				infoGluePrincipal = getController().getPrincipal(userName);
			
			hasRole = getController().getHasPrincipalRole(infoGluePrincipal, roleName);
		}

		produceResult(hasRole);

		return EVAL_PAGE;
	}

	public void setUserName(final String userName) throws JspException
	{
		this.userName = evaluateString("hasRole", "userName", userName);
	}

	public void setRoleName(final String roleName) throws JspException
	{
		this.roleName = evaluateString("hasRole", "roleName", roleName);
	}
}
