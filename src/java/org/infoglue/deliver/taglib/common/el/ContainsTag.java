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
package org.infoglue.deliver.taglib.common.el;

import javax.servlet.jsp.JspException;

/**
 * This class checks if an object exists in another object. It tries to be a bit smart and should handle
 * arrays, collections and simple strings etc.  
 */
public class ContainsTag extends org.infoglue.deliver.taglib.common.ContainsTag 
{
	private static final long serialVersionUID = -2142667647978144588L;

	public void setObject(final Object object) throws JspException
    {
        super.setObjectObject(object);
    }

    public void setValue(final Object value) throws JspException
    {
    	super.setValueObject(value);
    }
}
