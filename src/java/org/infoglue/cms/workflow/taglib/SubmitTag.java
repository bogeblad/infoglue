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

/**
 * This class implements the &lt;iw:submit&gt; tag, which presents an &lt;button type="submit" ... /&gt; 
 * form element which is used for executing a specific workflow action. 
 */
public class SubmitTag extends ElementTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = -7990348229256840043L;

	/**
	 * Default constructor.
	 */
    public SubmitTag() 
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
		return new Element("input").addAttribute("type", "submit");
	}
    
	/**
	 * Sets the id of the action that will be executed when pressing the button.
	 * 
	 * @param id the action id.
	 */
	public void setActionID(final String id) 
	{
		getElement().addAttribute("onclick", "document.getElementById('" + ACTION_ID_PARAMETER + "').value=" + id + ";");
    }

	/**
	 * Sets the value attribute of the button element.
	 * 
	 * @param value the value to use.
	 */ 
	public void setValue(final String value) 
	{
		getElement().addAttribute("value", value);
    }
	
	public void setOnclick(final String onclick)
	{
		getElement().addAttribute("onclick", onclick);
	}
}