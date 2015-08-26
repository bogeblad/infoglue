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
 * This class implements the &lt;common:sublist&gt; tag, which stores the sublist of a specified list
 * in the page context variable specified by the <code>id</code> attribute.  
 */
public class SublistTag extends org.infoglue.deliver.taglib.common.SublistTag 
{
	private static final long serialVersionUID = 4412711076276208352L;

	public void setList(final java.util.List list) throws JspException
    {
        super.setListObject(list);
    }

}
