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

/**
 * Base class for all workflow related tags writing to the output stream.
 * 
 * An <code>Element</code> is used for constructing the output. 
 */
public abstract class ElementTag extends WorkflowTag 
{
	/**
	 * The element used for constructing the output.
	 */
	private Element element;
	
	/**
	 * Default constructor.
	 */
	ElementTag()
	{
		super();
	}
	
	/**
	 * Process the end tag. Writes the element to the output stream.
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an I/O error occurs when writing to the output stream.
	 */
	public int doEndTag() throws JspException 
	{
		if(getElement() != null)
		{
			write(getElement().toString());
		}
		element = null;
		return EVAL_PAGE;
	}
	
	/**
	 * Returns the element used for constructing the output.
	 * 
	 * @return the element used for constructing the output.
	 */
	protected final Element getElement()
	{
		if(element == null)
		{
			element = createElement();
		}
		return element;
	}
	
	/**
	 * Creates the element used for constructing the output.
	 * 
	 * @return the element used for constructing the output.
	 */
	protected abstract Element createElement();

	// -------------------------------------------------------------------------	
	// --- core html attributes ------------------------------------------------
	// -------------------------------------------------------------------------	
	
	/**
	 * Sets the id attribute of the html element.
	 * 
	 * @param id the id to use.
	 * @throws JspException if an error occurs while evaluating the attribute.
	 */ 
    public void setIdAttr(final String id) throws JspException
    {
    	getElement().addAttribute("id", evaluateString("element", "idAttr", id));
    }

	/**
	 * Sets the class attribute of the html element.
	 * 
	 * @param cssClass the class to use.
	 * @throws JspException if an error occurs while evaluating the attribute.
	 */ 
    public void setCssClass(final String cssClass) throws JspException
    {
    	getElement().addAttribute("class", evaluateString("element", "cssClass", cssClass));
    }

	/**
	 * Sets the title attribute of the html element.
	 * 
	 * @param title the title to use.
	 * @throws JspException if an error occurs while evaluating the attribute.
	 */ 
    public void setTitle(final String title) throws JspException
    {
    	getElement().addAttribute("title", evaluateString("element", "title", title));
    }

	/**
	 * Sets the style attribute of the html element.
	 * 
	 * @param style the style to use.
	 * @throws JspException if an error occurs while evaluating the attribute.
	 */ 
    public void setStyle(final String style) throws JspException
    {
    	getElement().addAttribute("style", evaluateString("element", "style", style));
    }
}
