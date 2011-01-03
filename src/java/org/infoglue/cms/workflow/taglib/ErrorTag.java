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
 * This class implements the &lt;iw:error&gt; tag, which checks and sets if an error was connected to a certain field ... /&gt; 
 * The value of the content/content version attribute is fetched (with the name of the input element as a key) 
 * from the propertyset associated with the workflow. 
 */
public class ErrorTag extends ElementTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 925996908046500785L;
	
	private boolean languageDependent = true;

	/**
	 * Default constructor.
	 */
	public ErrorTag() 
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
		return new Element("p").addAttribute("class", "error");
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
	 * Sets the name attribute of the input element. 
	 * As an side-effect, the value attribute will also be set, where the value is
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
		
		if(languageCode == null || languageCode.equals(""))
			getElement().addText(getPropertySet().getString(name));
		else
			getElement().addText(getPropertySet().getString(languageCode + "_" + name));
	}

}
