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
package org.infoglue.cms.taglib.management;

import java.util.Collection;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.applications.workflowtool.function.ContentCreator;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.deliver.taglib.AbstractTag;

/**
 * 
 */
public class RepositoryLanguagesTag extends AbstractTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = -3858607807932357594L;

	private Integer repositoryId;
	
	/**
	 * Default constructor.
	 */
	public RepositoryLanguagesTag()
	{
		super();
	}
	
	/**
	 * 
	 */
	public int doEndTag() throws JspException 
	{
		setResultAttribute(getLanguages());
		return super.doEndTag();
	}
	
	/**
	 * 
	 */
	private Collection getLanguages() throws JspException
	{
		try 
		{
			return LanguageController.getController().getLanguageVOList(repositoryId);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new JspTagException(e.getMessage());
		}
	}

	public void setRepositoryId(String repositoryId) throws JspException
	{
		this.repositoryId = evaluateInteger("repositoryLanguages", "repositoryId", repositoryId);
	}
}
