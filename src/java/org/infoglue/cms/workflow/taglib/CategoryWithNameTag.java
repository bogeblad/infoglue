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
package org.infoglue.cms.workflow.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.deliver.taglib.AbstractTag;

/**
 * This class implements the &lt;iw:categoryWithName&gt; tag, which stores a category value
 * object (with the children populated) in a page context variable.
 */
public class CategoryWithNameTag extends AbstractTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 6455221936074988498L;
	
	/**
	 * The name of the category.
	 */
	private String name;
	
	/**
	 * Default constructor.
	 */
	public CategoryWithNameTag()
	{
		super();
	}
	
	/**
	 * Process the end tag. Stores the category value object in a page context variable.
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
    public int doEndTag() throws JspException 
    {
    	setResultAttribute(findCategory());
        return EVAL_PAGE;
    }

	/**
	 * Finds the category with the specified name.
	 * 
	 * @return the category value object with the children populated.
	 * @throws JspException if an error occurs when fetching the category.
	 */
	private CategoryVO findCategory() throws JspException 
	{
		try 
		{
			final CategoryVO categoryVO = CategoryController.getController().findByPath(name);
			return CategoryController.getController().findWithChildren(categoryVO.getId());
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
			throw new JspTagException(e.getMessage());
		}
	}
	
	/**
	 * Sets the name attribute to the specified name.
	 * 
	 * @param name the name to use.
	 */
	public void setName(final String name) 
	{
		this.name = name;
	}
}
