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

import java.util.regex.Pattern;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.impl.simple.RedirectImpl;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.validators.ValidatorFactory;

public class RedirectVO implements BaseEntityVO
{
    private java.lang.Integer redirectId;
    private java.lang.String url;
    private java.lang.String redirectUrl;
    private Pattern urlCompiledPattern;
    
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
        
