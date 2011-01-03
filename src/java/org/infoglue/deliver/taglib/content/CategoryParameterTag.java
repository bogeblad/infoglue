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
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.entities.content.ContentCategoryVO;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.deliver.taglib.TemplateControllerTag;

/**
 * This class implements the &lt;content:categoryParameter&gt; tag, which adds an category
 * to the list of categories in the contentVersion on a certain category attribute.
 *
 *  Note! This tag must have a &lt;content:contentVersionParameter&gt; ancestor.
 */

public class CategoryParameterTag extends TemplateControllerTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 4482006814634520239L;

	/**
	 * The categoryKey of the parameter.
	 */
	private String categoryKey;

	/**
	 * The categoryPath of the parameter.
	 */
	private String fullCategoryName;
	
	/**
	 * Default constructor. 
	 */
	public CategoryParameterTag()
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
		addCategory();
		return EVAL_PAGE;
    }
	
	/**
	 * Adds the digital asset to the ancestor tag.
	 * 
	 * @throws JspException if the ancestor tag isn't a content version tag.
	 */
	protected void addCategory() throws JspException
	{
		final ContentVersionParameterInterface parent = (ContentVersionParameterInterface) findAncestorWithClass(this, ContentVersionParameterInterface.class);
		if(parent == null)
		{
			throw new JspTagException("CategoryParameterTag must have a ContentVersionParameterInterface ancestor.");
		}
		
		try
		{
			((ContentVersionParameterInterface) parent).addContentCategory(categoryKey + "=" + fullCategoryName);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new JspTagException("Error setting contentCategory[" + categoryKey + " --> " + fullCategoryName + "]:" + e.getMessage());
		}
	}
	
	/**
	 * Sets the categoryKey attribute.
	 * 
	 * @param categoryKey the categoryKey to use.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setCategoryKey(final String categoryKey) throws JspException
	{
		this.categoryKey = evaluateString("parameter", "categoryKey", categoryKey);
	}

	/**
	 * Sets the fullCategoryName attribute.
	 * 
	 * @param fullCategoryName the fullCategoryName to use.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setFullCategoryName(final String fullCategoryName) throws JspException
	{
		this.fullCategoryName = evaluateString("parameter", "fullCategoryName", fullCategoryName);
	}

}
