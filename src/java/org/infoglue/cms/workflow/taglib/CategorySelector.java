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

import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.entities.management.CategoryVO;

/**
 * This class implements the &lt;iw:categorySelector&gt; tag, which presents an &lt;select ... &gt;...&lt;/select&gt; 
 * form element representing a category and where each option element represents a child of the root
 * category.
 * The value of the selected element is fetched (with the name of the select element as a key) 
 * from the propertyset associated with the workflow. 
 */
public class CategorySelector extends ElementTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 7831749037593672903L;

	/**
	 * The category to use when populating the option elements.
	 */
	private CategoryVO rootCategoryVO;
	
	/**
	 * The selected category. 
	 */
	private String selected;
	
	/**
	 * Default constructor.
	 */
	public CategorySelector() 
	{
		super();
	}

	/**
	 * Process the end tag. Creates the option elements and writes the select
	 * element to the output stream.
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an I/O error occurs when writing to the output stream.
	 */
	public int doEndTag() throws JspException 
	{
		createOptions();
		rootCategoryVO  = null;
		selected   = null;
		return super.doEndTag();
	}
	
	/**
	 * Creates the option elements.
	 */
	private void createOptions()
	{
		for(final Iterator i = rootCategoryVO.getChildren().iterator(); i.hasNext();) 
		{
			final CategoryVO categoryVO = (CategoryVO) i.next();
			final String name           = categoryVO.getName();
			final String value          = categoryVO.getId().toString();
			
			getElement().addChild("option")
				.addText(name)
				.addAttribute("value", value)
				.addAttribute("selected", "selected", value != null && selected != null && value.equals(selected));
		}
	}
	
	/**
	 * Creates the element to use when constructing this tag.
	 * 
	 * @return the element to use when constructing this tag.
	 */
	protected Element createElement()
	{
		return new Element("select");
	}
	
	/**
	 * Sets the label of the first option element.
	 * 
	 * @param label the label to use.
	 */
	public void setDefaultLabel(final String label) 
	{
		getElement().addChildFirst("option").addText(label);
	}
	
	/**
	 * Sets the path of the root category.
	 * 
	 * @param path the path to use.
	 * @throws JspException if an error occurs when fetching the category.
	 */
	public void setCategoryPath(final String path) throws JspException
	{
		rootCategoryVO = getRootCategory(path);
	}

	/**
	 * Sets the name attribute of the select element to the specified value. 
	 * 
	 * @param name the name to use.
	 */
	public void setName(final String name) 
	{
		getElement().addAttribute("name", name);
		selected = getPropertySet().getDataString(name);
	}
	
	/**
	 * Finds the category with the specified path.
	 * 
	 * @return the category value object with the children populated.
	 * @throws JspException if an error occurs when fetching the category.
	 */
	private CategoryVO getRootCategory(final String path) throws JspException 
	{
		try 
		{
			final CategoryVO categoryVO = CategoryController.getController().findByPath(path);
			if(categoryVO != null)
				return CategoryController.getController().findWithChildren(categoryVO.getId());
			else
				throw new Exception("No category with path " + path + " was found.");
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
			throw new JspTagException(e.getMessage());
		}
	}
}
