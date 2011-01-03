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

import org.infoglue.cms.controllers.kernel.impl.simple.RedirectController;
import org.infoglue.cms.entities.management.RedirectVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;


/**
  * This is the action-class for UpdateRedirect
  * 
  * @author Mattias Bogeblad
  */
public class UpdateRedirectAction extends ViewRedirectAction //WebworkAbstractAction
{
	
	private RedirectVO redirectVO;
	private Integer redirectId;
	private String userAction = "";
	private ConstraintExceptionBuffer ceb;
	
	public UpdateRedirectAction()
	{
		this(new RedirectVO());
	}
	
	public UpdateRedirectAction(RedirectVO redirectVO)
	{
		this.redirectVO = redirectVO;
		this.ceb = new ConstraintExceptionBuffer();	
	}

       	
	public String doExecute() throws Exception
    {
		super.initialize(getRedirectId());

    	ceb.add(this.redirectVO.validate());
    	ceb.throwIfNotEmpty();		
    	
		RedirectController.getController().update(this.redirectVO);
				
		return "success";
	}

	public String doLocal() throws Exception
    {
		super.initialize(getRedirectId());

		ceb.throwIfNotEmpty();
    	
		RedirectController.getController().update(this.redirectVO);
		
		return "successLocal";
	}

	public String doSaveAndExit() throws Exception
    {
		doExecute();
						
		return "saveAndExit";
	}

	public String doSaveAndExitLocal() throws Exception
    {
		doLocal();
						
		return "saveAndExitLocal";
	}

	public void setRedirectId(Integer redirectId) throws Exception
	{
		this.redirectVO.setRedirectId(redirectId);	
	}

    public java.lang.Integer getRedirectId()
    {
        return this.redirectVO.getRedirectId();
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
    
}
