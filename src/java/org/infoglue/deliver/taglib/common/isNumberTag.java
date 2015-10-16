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
package org.infoglue.deliver.taglib.common;

import javax.servlet.jsp.JspException;

import org.infoglue.deliver.taglib.AbstractTag;

/**
 *	This tag checks whether a string is a number or not
 *	return boolean
 */
public class isNumberTag extends AbstractTag 
{

	private static final long serialVersionUID = 8603406098980150888L;

	private String text;

	public isNumberTag() 
	{
		super();
	}

	public int doEndTag() throws JspException
    {
		setResultAttribute(isNumber());

        return EVAL_PAGE;
    }
    
    private boolean isNumber() {
    	try {
       		Integer.parseInt(text);
   
       	} catch (NumberFormatException numberFormatException) {
       		return false;
       	}
		return true;
	}

	public void setText(final String text) throws JspException
    {
        this.text = evaluateString("size", "text", text);
    }
}
