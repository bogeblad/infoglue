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

import java.util.ArrayList;
import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.RolePropertiesController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.RoleVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;


/**
 * @author mgu
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */


public class CreateRoleAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
	private RoleVO roleVO;
	private List infoGluePrincipals = new ArrayList();
	private List contentTypeDefinitionVOList;
	private ConstraintExceptionBuffer ceb;

	public CreateRoleAction()
	{
		this(new RoleVO());
	}

	public CreateRoleAction(RoleVO RoleVO)
	{
		this.roleVO = RoleVO;	
		this.ceb = new ConstraintExceptionBuffer();
	}
		
	public String doInput() throws Exception
    {
    	return "input";
    }

	public String doInputV3() throws Exception
    {
		this.infoGluePrincipals	= UserControllerProxy.getController().getAllUsers();
		this.contentTypeDefinitionVOList = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList(ContentTypeDefinitionVO.EXTRANET_ROLE_PROPERTIES);

    	return "inputV3";
    }

	protected String doExecute() throws Exception 
	{
		ceb.add(this.roleVO.validate());
    	ceb.throwIfNotEmpty();	
    				
		String[] userNames = getRequest().getParameterValues("userName");
		String[] contentTypeDefinitionIds = getRequest().getParameterValues("contentTypeDefinitionId");

		RoleControllerProxy.getController().createRole(this.roleVO);
		if(userNames != null)
		{
			RoleControllerProxy.getController().updateRole(this.roleVO, userNames);
		}
		
		if(contentTypeDefinitionIds != null && contentTypeDefinitionIds.length > 0 && !contentTypeDefinitionIds[0].equals(""))
			RolePropertiesController.getController().updateContentTypeDefinitions(this.getRoleName(), contentTypeDefinitionIds);

		return "success";
	}

	public String doV3() throws Exception 
	{
		try
		{
			doExecute();
			
			String[] interceptionPointNames = new String[]{"Role.ManageUsers", "Role.ManageAccessRights", "Role.ReadForAssignment"};
			AccessRightController.getController().addUserRights(interceptionPointNames, getRoleName(), getInfoGluePrincipal());
		}
		catch(ConstraintException e) 
        {
			this.infoGluePrincipals	= UserControllerProxy.getController().getAllUsers();
			this.contentTypeDefinitionVOList = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList(ContentTypeDefinitionVO.EXTRANET_ROLE_PROPERTIES);

			e.setResult(INPUT + "V3");
			throw e;
        }
		
		return "successV3";
	}

	public String doSaveAndExitV3() throws Exception 
	{
		doV3();
		
		return "successSaveAndExitV3";
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

    public List getInfoGluePrincipals()
	{
		return infoGluePrincipals;
	}

	public List getContentTypeDefinitionVOList()
	{
		return contentTypeDefinitionVOList;
	}

}
