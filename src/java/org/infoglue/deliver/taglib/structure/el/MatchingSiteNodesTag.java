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

import java.util.Date;

import javax.servlet.jsp.JspException;

public class MatchingSiteNodesTag extends org.infoglue.deliver.taglib.structure.MatchingSiteNodesTag
{
	private static final long serialVersionUID = -5597292385164600351L;

	public void setFromDate(Date fromDate) throws JspException
	{
		super.setFromDateObject(fromDate);
	}

	public void setToDate(Date toDate) throws JspException
	{
		super.setToDateObject(toDate);
	}

	public void setExpireFromDate(Date expireFromDate) throws JspException
	{
		super.setExpireFromDateObject(expireFromDate);
	}

	public void setExpireToDate(Date expireToDate) throws JspException
	{
		super.setExpireToDateObject(expireToDate);
	}

}