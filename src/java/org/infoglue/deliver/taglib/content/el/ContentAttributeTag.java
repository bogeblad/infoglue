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

package org.infoglue.deliver.taglib.content.el;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.entities.content.ContentVersionVO;

/**
 * This is an attempt to make an TagLib for attempts to get a ContentAttribute from a content referenced by a component
 * in a JSP.
 * 
 * <%@ taglib uri="infoglue" prefix="infoglue" %>
 * 
 * <infoglue:component.ContentAttribute propertyName="Article" attributeName="Title"/>
 *
 * @author Mattias Bogeblad
 * 
 * 2005-12-22 Added mapKeyName which extracts a value from a properties.file formated text content. / per.jonsson@it-huset.se
 *
 * 
 */

public class ContentAttributeTag extends org.infoglue.deliver.taglib.content.ContentAttributeTag
{
	private static final long serialVersionUID = -6594084999544926100L;

	public void setContentVersion(ContentVersionVO contentVersion) throws JspException
	{
		super.setContentVersionObject(contentVersion);
	}
}
