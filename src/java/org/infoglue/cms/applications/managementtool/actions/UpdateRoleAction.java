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

import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.RolePropertiesController;
import org.infoglue.cms.entities.management.RoleVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * @author Mattias Bogeblad
 */

public class UpdateRoleAction extends ViewRoleAction //WebworkAbstractAction
{
	private ConstraintExceptionBuffer ceb;
	private RoleVO roleVO;
	
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
		
		return "success";
	}
	
	public String doSaveAndExit() throws Exception
    {
		doExecute();
		
		return "saveAndExit";
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
	
    public void setRoleName(String roleName)
    {
		this.roleVO.setRoleName(roleName);
    }
    
	public String getRoleName()
	{
		return this.roleVO.getRoleName();	
	}
	
	public void setDescription(java.lang.String description)
	{
        this.roleVO.setDescription(description);
	}  

	public String getDescription()
	{
		return this.roleVO.getDescription();	
	}
}
