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

import java.util.Date;

import javax.servlet.jsp.JspException;


public class PrincipalContentVersionsTag extends org.infoglue.deliver.taglib.content.PrincipalContentVersionsTag 
{
	private static final long serialVersionUID = -4209840744773457644L;

	public void setPublishStartDate(Date publishStartDate) throws JspException
	{
        super.setPublishStartDateObject(publishStartDate);
	}

	public void setPublishEndDate(Date publishEndDate) throws JspException
	{
		 super.setPublishEndDateObject(publishEndDate);
	}

	public void setExpireStartDate(Date expireStartDate) throws JspException
	{
		 super.setExpireStartDateObject(expireStartDate);
	}

	public void setExpireEndDate(Date expireEndDate) throws JspException
	{
		 super.setExpireEndDateObject(expireEndDate);
	}

}
