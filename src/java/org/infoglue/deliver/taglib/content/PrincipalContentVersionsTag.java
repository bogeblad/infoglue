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

package org.infoglue.deliver.taglib.content;

import java.util.Date;

import org.infoglue.deliver.taglib.TemplateControllerTag;

import javax.servlet.jsp.JspException;


public class PrincipalContentVersionsTag extends TemplateControllerTag 
{
	private static final long serialVersionUID = 3257003254859576632L;

	private String contentTypeDefinitionName;
	private String userName;
	private Date publishStartDate;
	private Date publishEndDate;
	private Date expireStartDate;
	private Date expireEndDate;
	
	public int doEndTag() throws JspException
    {
		if(userName == null)
			userName = this.getController().getPrincipal().getName();
		
		setResultAttribute(this.getController().getPrincipalContentVersions(contentTypeDefinitionName, userName, publishStartDate, publishEndDate, expireStartDate, expireEndDate));
        
		return EVAL_PAGE;
    }

	public void setContentTypeDefinitionName(String contentTypeDefinitionName) throws JspException
	{
        this.contentTypeDefinitionName = evaluateString("contentVersionSearchTag", "contentTypeDefinitionName", contentTypeDefinitionName);
	}

	public void setUserName(String userName) throws JspException
	{
        this.userName = evaluateString("contentVersionSearchTag", "userName", userName);
	}

	public void setPublishStartDate(String publishStartDate) throws JspException
	{
        this.publishStartDate = (Date)evaluate("contentVersionSearchTag", "publishStartDate", publishStartDate, Date.class);
	}

	public void setPublishEndDate(String publishEndDate) throws JspException
	{
        this.publishEndDate = (Date)evaluate("contentVersionSearchTag", "publishEndDate", publishEndDate, Date.class);
	}

	public void setExpireStartDate(String expireStartDate) throws JspException
	{
        this.expireStartDate = (Date)evaluate("contentVersionSearchTag", "expireStartDate", expireStartDate, Date.class);
	}

	public void setExpireEndDate(String expireEndDate) throws JspException
	{
        this.expireEndDate = (Date)evaluate("contentVersionSearchTag", "expireEndDate", expireEndDate, Date.class);
	}

}
