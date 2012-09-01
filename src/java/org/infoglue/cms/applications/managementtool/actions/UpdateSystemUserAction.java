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

import java.util.List;

import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.UserPropertiesController;
import org.infoglue.cms.entities.management.RoleVO;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * @author Mattias Bogeblad
 */

public class UpdateSystemUserAction extends ViewSystemUserAction //WebworkAbstractAction
{
	private ConstraintExceptionBuffer ceb;
	private SystemUserVO systemUserVO;
	private RoleVO roleVO;
	private InfoGluePrincipal infoGluePrincipal;
	private String[] roller;
	private List defRoles;
	private String action;
	private String oldPassword;
	
	
	public UpdateSystemUserAction()
	{
		this(new SystemUserVO());
	}
	
	public UpdateSystemUserAction(SystemUserVO systemUserVO)
	{
		this.systemUserVO = systemUserVO;
		this.ceb = new ConstraintExceptionBuffer();		
	}
	
	public String doExecute() throws Exception
    {
    	super.initialize(this.getInfoGluePrincipal().getName());
    			
    	ceb = this.systemUserVO.validate();
    	ceb.throwIfNotEmpty();		
	
		String[] roles = getRequest().getParameterValues("roleName");
		String[] groups = getRequest().getParameterValues("groupName");
		if(roles == null)
			roles = new String[]{};
		if(groups == null)
			groups = new String[]{};
		
		String[] contentTypeDefinitionIds = getRequest().getParameterValues("contentTypeDefinitionId");
		
		UserControllerProxy.getController().updateUser(this.systemUserVO, roles, groups);
    	if(contentTypeDefinitionIds != null && contentTypeDefinitionIds.length > 0 && !contentTypeDefinitionIds[0].equals(""))
			UserPropertiesController.getController().updateContentTypeDefinitions(this.getUserName(), contentTypeDefinitionIds);
		
		return "success";

	}

	public String doSaveAndExit() throws Exception
    {
		doExecute();
		
		return "saveAndExit";
	} 

	public String doChangePassword() throws Exception
	{
	    if(this.systemUserVO.getUserName().equals(CmsPropertyHandler.getAnonymousUser()))
			UserControllerProxy.getController().updateAnonymousUserPassword();
	    else
	    	UserControllerProxy.getController().updateUserPassword(this.systemUserVO.getUserName());
		
		return "passwordSentSuccess";
	}

	public String doV3() throws Exception
    {
    	doExecute();
		
		return "successV3";

	}

	public String doSaveAndExitV3() throws Exception
    {
		doV3();
		
		return "saveAndExitV3";
	} 

	public String doChangePasswordV3() throws Exception
	{
		doChangePassword();
		
		return "passwordSentSuccessV3";
	}

	private String[] getRoller()
	{
		return this.roller;	
	}	
       
    public java.lang.String getFirstName()
    {
        return this.systemUserVO.getFirstName();
    }
        
    public void setFirstName(java.lang.String firstName)
    {
        this.systemUserVO.setFirstName(firstName);
    }

    public java.lang.String getLastName()
    {
        return this.systemUserVO.getLastName();
    }
        
    public void setLastName(java.lang.String lastName)
    {
        this.systemUserVO.setLastName(lastName);
    }
        
    public String getUserName()
    {
    	return this.systemUserVO.getUserName();
    }
    
    public void setUserName(String userName)
    {
    	this.systemUserVO.setUserName(userName);
	}

    public java.lang.String getEmail()
    {
    	return this.systemUserVO.getEmail();
    }

    public void setEmail(java.lang.String email) throws Exception
    {
    	this.systemUserVO.setEmail(email);
    }

    public String getSource()
    {
    	return this.systemUserVO.getSource();
    }
    
    public void setSource(String source)
    {
    	this.systemUserVO.setSource(source);
    }

    public Boolean getIsActive()
    {
    	return this.systemUserVO.getIsActive();
    }
    
    public void setIsActive(Boolean isActive)
    {
    	this.systemUserVO.setIsActive(isActive);
    }
    
    public java.lang.String getPassword()
    {
    	return this.systemUserVO.getPassword();
    }

    public void setPassword(java.lang.String password)
    {
    	this.systemUserVO.setPassword(password);
    }

    public void setOldPassword(java.lang.String oldPassword)
    {
    	this.oldPassword = oldPassword;
    }

	public void setAction(String action)
	{
		this.action = action;
	}    
	
	public String getAction()
	{
		return this.action;
	}




}
