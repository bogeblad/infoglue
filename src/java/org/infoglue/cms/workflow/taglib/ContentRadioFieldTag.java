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
 * This class implements the &lt;iw:radio&gt; tag, which presents an &lt;input type="radio" ... /&gt; 
 * form element representing a content/content version attribute. 
 * The value of the content/content version attribute is fetched (with the name of the input element as a key) 
 * from the propertyset associated with the workflow. 
 */
public class ContentRadioFieldTag extends ContentBooleanFieldTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 8350821042869589564L;

	/**
	 * Default constructor.
	 */
	public ContentRadioFieldTag() 
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
		return new Element("input").addAttribute("type", "radio");
	}
	
	public void setSelected(final String selected) 
	{
		getElement().addAttribute("selected", selected);
	}

}