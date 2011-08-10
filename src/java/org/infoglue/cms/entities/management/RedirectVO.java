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

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.impl.simple.RedirectImpl;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.validators.ValidatorFactory;

public class RedirectVO implements BaseEntityVO
{
    private java.lang.Integer redirectId;
    private java.lang.String url;
    private java.lang.String redirectUrl;
    
    private java.lang.String modifier;
    private java.util.Date createdDateTime = new Date();
    private java.util.Date publishDateTime = new Date();
    private java.util.Date expireDateTime = new Date();
    private Boolean isUserManaged = true;
    
    private Pattern urlCompiledPattern;
    
    public RedirectVO()
  	{
  		//Initilizing the expireDateTime... 
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.MONTH, 3);
  		expireDateTime = cal.getTime();
  	}
    
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	
    public Integer getId() 
	{
		return getRedirectId();
	}

	public String toString()
	{  
		return getUrl();
	}
  
    public java.lang.Integer getRedirectId()
    {
        return this.redirectId;
    }
                
    public void setRedirectId(java.lang.Integer redirectId)
    {
        this.redirectId = redirectId;
    }
    
    public java.lang.String getRedirectUrl()
    {
        return redirectUrl;
    }
    
    public void setRedirectUrl(java.lang.String redirectUrl)
    {
        this.redirectUrl = redirectUrl;
    }
    
    public java.lang.String getUrl()
    {
        return url;
    }

    public void setUrl(java.lang.String url)
    {
        this.url = url;
    }

    public java.lang.String getModifier()
    {
    	return this.modifier;
    }
    
    public void setModifier(java.lang.String modifier) throws ConstraintException
    {
    	this.modifier = modifier;
    }

    public java.util.Date getCreatedDateTime()
    {
    	return this.createdDateTime;
    }
    
    public void setCreatedDateTime(java.util.Date createdDateTime)
    {
    	if(createdDateTime != null)
    		this.createdDateTime = createdDateTime;
    }
    
    public java.util.Date getPublishDateTime()
    {
    	return this.publishDateTime;
    }
    
    public void setPublishDateTime(java.util.Date publishDateTime)
    {
    	if(publishDateTime != null)
    		this.publishDateTime = publishDateTime;
    }
    
    public java.util.Date getExpireDateTime()
    {
    	return this.expireDateTime;
    }
    
    public void setExpireDateTime(java.util.Date expireDateTime)
    {
    	if(expireDateTime != null)
    		this.expireDateTime = expireDateTime;
    }

    public Boolean getIsUserManaged()
    {
    	return this.isUserManaged;
    }
    
    public void setIsUserManaged(Boolean isUserManaged)
    {
    	this.isUserManaged = isUserManaged;
    }
    
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() 
	{
    	ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
    	
    	ValidatorFactory.createStringValidator("Redirect.url", true, 1, 1024, true, RedirectImpl.class, this.getId(), null).validate(this.url, ceb);
        ValidatorFactory.createStringValidator("Redirect.redirectUrl", true, 1, 1024).validate(redirectUrl, ceb); 
    	
    	return ceb;
	}
        
}
        
