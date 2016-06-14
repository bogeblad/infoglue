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

import org.apache.commons.codec.binary.Base64;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.RolePropertiesController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.management.RoleVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * @author Mattias Bogeblad
 */

public class UpdateRoleAction extends ViewRoleAction //WebworkAbstractAction
{
	private VisualFormatter formatter = new VisualFormatter();
	
	private ConstraintExceptionBuffer ceb;
	private RoleVO roleVO;
	
	private String userName;
	
	public UpdateRoleAction()
	{
		this(new RoleVO());
	}
	
	public UpdateRoleAction(RoleVO RoleVO)
	{
		this.roleVO = RoleVO;
		this.ceb = new ConstraintExceptionBuffer();
	}
	
	public String doExecute() throws Exception
    {
    	super.initialize(getRoleName());
    	
    	ceb.add(this.roleVO.validate());
    	ceb.throwIfNotEmpty();

		String[] values = getRequest().getParameterValues("userName");
		String[] contentTypeDefinitionIds = getRequest().getParameterValues("contentTypeDefinitionId");
		RoleControllerProxy.getController().updateRole(this.roleVO, values);
		
		if(contentTypeDefinitionIds != null && contentTypeDefinitionIds.length > 0 && !contentTypeDefinitionIds[0].equals(""))
			RolePropertiesController.getController().updateContentTypeDefinitions(this.getRoleName(), contentTypeDefinitionIds);
		
		return "successRedirect";
	}
	
	public String doSaveAndExit() throws Exception
    {
		doExecute();
		
		return "saveAndExit";
	}

	public String doDeleteUser() throws Exception
    {
		RoleControllerProxy.getController().removeUser(getRoleName(), this.userName);
		
		return "successRedirect";
	}

	public String doAddUser() throws Exception
    {
		RoleControllerProxy.getController().addUser(getRoleName(), this.userName);
		
		return "successRedirect";
	}

	public String doV3() throws Exception
    {
		try
		{
			doExecute();
		}
		catch(ConstraintException e) 
        {
			e.setResult(INPUT + "V3");
			throw e;
        }
		
		return "successV3";
	}

	public String doSaveAndExitV3() throws Exception
    {
		try
		{
			doExecute();
		}
		catch(ConstraintException e) 
        {
			e.setResult(INPUT + "V3");
			throw e;
        }
		
		return "saveAndExitV3";
	}
	
	public void setRoleName(String roleName) throws Exception
	{	
		if(roleName != null)
		{
			byte[] bytes = Base64.decodeBase64(roleName);
			String decodedRoleName = new String(bytes, "utf-8");
			if(RoleControllerProxy.getController().roleExists(decodedRoleName))
			{
				roleName = decodedRoleName;
			}
			else
			{
				String fromEncoding = CmsPropertyHandler.getURIEncoding();
				String toEncoding = "utf-8";
				
				String testRoleName = new String(roleName.getBytes(fromEncoding), toEncoding);
				if(testRoleName.indexOf((char)65533) == -1)
					roleName = testRoleName;
			}
		}
		
		this.roleVO.setRoleName(roleName);
	}
	
    public void setUserName(String userName) throws Exception
    {
		if(userName != null)
		{
			byte[] bytes = Base64.decodeBase64(userName);
			String decodedUserName = new String(bytes, "utf-8");
			if(UserControllerProxy.getController().userExists(decodedUserName))
			{
				userName = decodedUserName;
			}
		}

		this.userName = userName;
    }

	public String getRoleName()
	{
		return this.roleVO.getRoleName();	
	}

	public String getEncodedRoleName() throws Exception
	{
		return formatter.encodeBase64(this.roleVO.getRoleName());	
	}

	public void setDescription(java.lang.String description)
	{
        this.roleVO.setDescription(description);
	}  

	public String getDescription()
	{
		return this.roleVO.getDescription();	
	}
	
	public String getSource()
    {
		return this.roleVO.getSource();
    }
    
    public void setSource(String source)
    {
    	this.roleVO.setSource(source);
    }

    public Boolean getIsActive()
    {
    	return this.roleVO.getIsActive();
    }
    
    public void setIsActive(Boolean isActive)
    {
    	this.roleVO.setIsActive(isActive);
    }

}
