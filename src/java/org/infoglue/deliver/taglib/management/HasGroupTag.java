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
 * Checks if a user has a specific <code>group</code>
 * 
 * @author Johan Larsson, Mattias Bogeblad
 */

public class HasGroupTag extends TemplateControllerTag
{
	private static final long serialVersionUID = 4050206323348354355L;

	private String groupName;
	private String userName;

	public HasGroupTag()
	{
		super();
	}

	public int doEndTag() throws JspException
	{
		boolean hasGroup = false;
		
		if(groupName != null)
		{
			InfoGluePrincipal infoGluePrincipal = null;
			if(userName == null && !userName.equals(""))
				infoGluePrincipal = getController().getPrincipal();
			else
				infoGluePrincipal = getController().getPrincipal(userName);
			
			hasGroup = getController().getHasPrincipalGroup(infoGluePrincipal, groupName);
		}

		produceResult(hasGroup);

		return EVAL_PAGE;
	}

	public void setUserName(final String userName) throws JspException
	{
		this.userName = evaluateString("hasGroup", "userName", userName);
	}

	public void setGroupName(final String groupName) throws JspException
	{
		this.groupName = evaluateString("hasGroup", "groupName", groupName);
	}
}
