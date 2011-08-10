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

package org.infoglue.cms.applications.managementtool.actions;

import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.RedirectController;
import org.infoglue.cms.entities.management.RedirectVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class CreateRedirectAction extends InfoGlueAbstractAction
{
	private RedirectVO redirectVO;
	private ConstraintExceptionBuffer ceb;

	
	public CreateRedirectAction()
	{
		this(new RedirectVO());
	}
	
	public CreateRedirectAction(RedirectVO redirectVO)
	{
		this.redirectVO = redirectVO;
		this.ceb = new ConstraintExceptionBuffer();
			
	}	
	public Integer getRedirectId()
	{
		return this.redirectVO.getId();	
	}
    
    public java.lang.String getUrl()
    {
        return this.redirectVO.getUrl();
    }
        
    public void setUrl(java.lang.String url)
    {
       	this.redirectVO.setUrl(url);
    }
      
    public String getRedirectUrl()
    {
        return this.redirectVO.getRedirectUrl();
    }
        
    public void setRedirectUrl(String redirectUrl)
    {
       	this.redirectVO.setRedirectUrl(redirectUrl);
    }

    public java.lang.String getModifier()
    {
    	return this.redirectVO.getModifier();
    }
    
    public void setModifier(java.lang.String modifier) throws ConstraintException
    {
    	this.redirectVO.setModifier(modifier);
    }
    
    public String getPublishDateTime()
    {
    	return new VisualFormatter().formatDate(this.redirectVO.getPublishDateTime(), "yyyy-MM-dd HH:mm");
    }
    
    public void setPublishDateTime(String publishDateTime)
    {
    	this.redirectVO.setPublishDateTime(new VisualFormatter().parseDate(publishDateTime, "yyyy-MM-dd HH:mm"));
    }
    
    public String getExpireDateTime()
    {
    	return new VisualFormatter().formatDate(this.redirectVO.getExpireDateTime(), "yyyy-MM-dd HH:mm");
    }
    
    public void setExpireDateTime(String expireDateTime)
    {
    	this.redirectVO.setExpireDateTime(new VisualFormatter().parseDate(expireDateTime, "yyyy-MM-dd HH:mm"));
    }

    public Boolean getIsUserManaged()
    {
    	return this.redirectVO.getIsUserManaged();
    }
    
    public void setIsUserManaged(Boolean isUserManaged)
    {
    	this.redirectVO.setIsUserManaged(isUserManaged);
    }
    
    public String doExecute() throws Exception
    {
		ceb.add(this.redirectVO.validate());
    	ceb.throwIfNotEmpty();				
    	
    	this.redirectVO.setModifier(getInfoGluePrincipal().getName());
    	this.redirectVO.setIsUserManaged(true);
		this.redirectVO = RedirectController.getController().create(redirectVO);
		
        return "success";
    }
        
    public String doInput() throws Exception
    {
    	return "input";
    }    
}
