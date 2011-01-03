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

import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.management.LanguageVO;

/**
 * This class implements the &lt;iw:textarea&gt; tag, which presents an &lt;textarea ... /&gt; 
 * form element representing a content/content version attribute. 
 * The value of the content/content version attribute is fetched (with the name of the input element as a key) 
 * from the propertyset associated with the workflow. 
 */
public class ContentTextareaFieldTag extends ElementTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 8959111408264926025L;

	private boolean languageDependent = true;

	/**
	 * Default constructor.
	 */
	public ContentTextareaFieldTag() 
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
		return new Element("textarea");
	}

	/**
	 * Sets the readonly attribute of the input element if the specified argument is true.
	 * 
	 * @param isReadonly indicates if the attribute should be set.
	 */
    public void setLanguageDependent(final boolean languageDependent) 
    {
    	this.languageDependent = languageDependent;
    }


	/**
	 * Sets the name attribute of the textarea element. 
	 * As an side-effect, the value will also be set, where the value is
	 * fetched from the propertyset using the specified name.
	 * 
	 * @param name the name to use.
	 */
	public void setName(final String name) 
	{
		String languageCode = null;
		
		if(languageDependent)
		{
			try
			{
				String languageIdString = getPropertySet().getString("languageId");
				if(languageIdString != null)
				{
					LanguageVO languageVO = LanguageController.getController().getLanguageVOWithId(new Integer(languageIdString));
					if(languageVO != null)
						languageCode = languageVO.getLanguageCode();
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		
		getElement().addAttribute("name", name);
		if(languageCode == null || languageCode.equals(""))
			getElement().addText(getPropertySet().getDataString(name));
		else
			getElement().addText(getPropertySet().getDataString(languageCode + "_" + name));	
	}
	
	/**
	 * Sets the rows attribute of the textarea element.
	 * 
	 * @param rows the rows to use.
	 */ 
	public void setRows(final String rows) 
	{
		getElement().addAttribute("rows", rows);
	}

	/**
	 * Sets the cols attribute of the textarea element.
	 * 
	 * @param columns the columns to use.
	 */ 
	public void setColumns(final String columns) 
	{
		getElement().addAttribute("cols", columns);
	}
}
