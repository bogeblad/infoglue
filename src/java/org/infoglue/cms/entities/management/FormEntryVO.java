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

package org.infoglue.cms.entities.management;

import java.util.Date;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class FormEntryVO implements BaseEntityVO
{
    private Integer formEntryId;
    private String originAddress;
    private String formName;
    private Integer formContentId;
    private String userIP;
    private String userAgent;
    private String userName;
    private Date registrationDateTime = new Date();
    
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	
    public Integer getId() 
	{
		return getFormEntryId();
	}

	public java.lang.Integer getFormEntryId()
	{
		return formEntryId;
	}


	public void setFormEntryId(java.lang.Integer formEntryId)
	{
		this.formEntryId = formEntryId;
	}


	public java.lang.String getOriginAddress()
	{
		return originAddress;
	}


	public void setOriginAddress(java.lang.String originAddress)
	{
		this.originAddress = originAddress;
	}


	public java.lang.String getFormName()
	{
		return formName;
	}


	public void setFormName(java.lang.String formName)
	{
		this.formName = formName;
	}


	public java.lang.Integer getFormContentId()
	{
		return formContentId;
	}


	public void setFormContentId(java.lang.Integer formContentId)
	{
		this.formContentId = formContentId;
	}


	public java.lang.String getUserIP()
	{
		return userIP;
	}


	public void setUserIP(java.lang.String userIP)
	{
		this.userIP = userIP;
	}


	public java.lang.String getUserAgent()
	{
		return userAgent;
	}


	public String getUserName() 
	{
		return userName;
	}

	public void setUserName(String userName) 
	{
		this.userName = userName;
	}

	public Date getRegistrationDateTime() 
	{
		return registrationDateTime;
	}

	public void setRegistrationDateTime(Date registrationDateTime) 
	{
		this.registrationDateTime = registrationDateTime;
	}

	public void setUserAgent(java.lang.String userAgent)
	{
		this.userAgent = userAgent;
	}
        
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() 
	{
    	ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
    	
    	return ceb;
	}

}
        
