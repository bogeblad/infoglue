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

package org.infoglue.deliver.taglib.structure.el;

import javax.servlet.jsp.JspException;

import org.infoglue.deliver.applications.actions.InfoGlueComponent;

public class ComponentPropertyValueTag extends org.infoglue.deliver.taglib.structure.ComponentPropertyValueTag 
{
	private static final long serialVersionUID = 3847940889917100115L;

	public void setComponent(InfoGlueComponent component) throws JspException
    {
    	super.setComponentObject(component);
    }
    
}
