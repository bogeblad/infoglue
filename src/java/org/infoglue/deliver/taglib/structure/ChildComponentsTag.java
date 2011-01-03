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

package org.infoglue.deliver.taglib.structure;

import java.util.List;

import javax.servlet.jsp.JspException;

import org.infoglue.deliver.taglib.component.ComponentLogicTag;

public class ChildComponentsTag extends ComponentLogicTag 
{
	private static final long serialVersionUID = 4050206323348354355L;

	private String slotId = null;
	private boolean seekRecursive = false;
	private String propertyFilterStrings = null;
	
    public ChildComponentsTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {
		List childComponents = null;
		if(!seekRecursive && propertyFilterStrings == null)
		{
			childComponents = getComponentLogic().getChildComponents(slotId);
		}
		else
		{
			childComponents = getComponentLogic().getChildComponents(slotId, seekRecursive, propertyFilterStrings);
		}
		
        setResultAttribute(childComponents);
	    
        seekRecursive = false;
        propertyFilterStrings = null;
        
		return EVAL_PAGE;
    }

	public void setSlotId(String slotId) throws JspException
	{
	    this.slotId = evaluateString("childComponents", "slotId", slotId);
	}

	public void setSeekRecursive(boolean seekRecursive) throws JspException
	{
		this.seekRecursive = seekRecursive;
	}

	public void setPropertyFilterStrings(String propertyFilterStrings) throws JspException
	{
		this.propertyFilterStrings = evaluateString("childComponents", "propertyFilterStrings", propertyFilterStrings);
	}

}
