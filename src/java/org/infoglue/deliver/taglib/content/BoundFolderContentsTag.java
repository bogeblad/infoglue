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

package org.infoglue.deliver.taglib.content;

import javax.servlet.jsp.JspException;

import org.infoglue.deliver.taglib.TemplateControllerTag;

/**
 * Tag for org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController.getBoundFolderContents(String, boolean, String, String);
 */
public class BoundFolderContentsTag extends TemplateControllerTag
{
	private static final long serialVersionUID = 3905242346756059449L;

	private String structureBindningName;
	private String sortAttribute;
	private String sortOrder = "asc";
	private boolean searchRecursive = false;
	
    public BoundFolderContentsTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {
		setResultAttribute(getController().getBoundFolderContents(structureBindningName, searchRecursive, sortAttribute, sortOrder));
        return EVAL_PAGE;
    }
	
	public void setStructureBindningName(String name)
	{
		this.structureBindningName = name;
	}

	public void setSortAttribute(String sortAttribute)
	{
		this.sortAttribute = sortAttribute;
	}

	public void setSortOrder(String sortOrder)
	{
		this.sortOrder = sortOrder;
	}

	public void setSearchRecursive(boolean searchRecursive)
	{
		this.searchRecursive = searchRecursive;
	}
}
