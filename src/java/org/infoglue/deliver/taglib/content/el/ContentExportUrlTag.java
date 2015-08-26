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

/**
 * This tag exports one or many contents into a xml-file.
 * 
 * <%@ taglib uri="infoglue" prefix="infoglue" %>
 * 
 * <content:contentExportUrl id="myExportUrl" contentId="2423" fileNamePrefix="KomponentX"/>
 *
 * @author Mattias Bogeblad
 */

public class ContentExportUrlTag extends org.infoglue.deliver.taglib.content.ContentExportUrlTag
{

	private static final long serialVersionUID = -4677675978029264733L;

	public void setContentIdList(java.util.List contentIdList) throws JspException
    {
    	super.setContentIdListObject(contentIdList);
    }
}