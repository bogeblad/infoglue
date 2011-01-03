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
 * This class implements the &lt;iw:action&gt; tag, which presents an &lt;href ... /&gt; 
 * form element which is used for executing a specific workflow action. 
 */
public class ActionLinkTag extends ElementTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = -7990348229256840043L;

	/**
	 * Default constructor.
	 */
    public ActionLinkTag() 
    {
        super();
    }

	/**
	 * Creates the element to use when constructing this tag.
	 * 
	 * @return the element to use when constructing this tag.
	 */
	protected Element createElement()
	{
		return new Element("a").addAttribute("href", "#");
	}
    
	/**
	 * Sets the id of the action that will be executed when pressing the button.
	 * 
	 * @param id the action id.
	 */
	public void setActionID(final String id) 
	{
		getElement().addAttribute("onclick", "document.getElementById('" + ACTION_ID_PARAMETER + "').value=" + id + "; document.form.submit();");
    }

	/**
	 * Sets the value attribute of the button element.
	 * 
	 * @param value the value to use.
	 */ 
	public void setText(final String text) throws JspException
	{
		getElement().addText(this.evaluateString("actionLink", "text", text));
    }

	public void setOnclick(final String onclick) throws JspException
	{
		getElement().addAttribute("onclick", this.evaluateString("actionLink", "onclick", onclick));
	}
}