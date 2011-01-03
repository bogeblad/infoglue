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

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.common.actions.SubscriptionsAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.UserPropertiesController;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ConstraintExceptionBuffer;


/**
 * This class represents the create user action
 * 
 * @author Mattias Bogeblad
 */


public class CreateSystemUserAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(CreateSystemUserAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private ConstraintExceptionBuffer ceb;
	private SystemUserVO systemUserVO;
	private InfoGluePrincipal infoGluePrincipal;

	private List availableRoles = new ArrayList();
	private List availableGroups = new ArrayList();
	private List contentTypeDefinitionVOList;   

	public CreateSystemUserAction()
	{
		this(new SystemUserVO());
	}
	
	public CreateSystemUserAction(SystemUserVO systemUserVO)
	{
		this.systemUserVO = systemUserVO;	
		this.ceb = new ConstraintExceptionBuffer();
	}
	
	public String doInput() throws Exception
    {
		this.availableRoles 				= RoleControllerProxy.getController().getAvailableRoles(this.getInfoGluePrincipal(), "Role.ManageUsers");
		this.availableGroups 				= GroupControllerProxy.getController().getAvailableGroups(this.getInfoGluePrincipal(), "Group.ManageUsers");
		this.contentTypeDefinitionVOList 	= ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList(ContentTypeDefinitionVO.EXTRANET_USER_PROPERTIES);
	
    	return "input";
    }

	public String doInputV3() throws Exception
    {
		this.availableRoles 				= RoleControllerProxy.getController().getAvailableRoles(this.getInfoGluePrincipal(), "Role.ManageUsers");
		this.availableGroups 				= GroupControllerProxy.getController().getAvailableGroups(this.getInfoGluePrincipal(), "Group.ManageUsers");
		this.contentTypeDefinitionVOList 	= ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList(ContentTypeDefinitionVO.EXTRANET_USER_PROPERTIES);
	
    	return "inputV3";
    }

	public String doExecute() throws Exception 
	{
		ceb = this.systemUserVO.validate();
    	ceb.throwIfNotEmpty();		
		
		String[] roles = getRequest().getParameterValues("roleName");
		String[] groups = getRequest().getParameterValues("groupName");
		String[] contentTypeDefinitionIds = getRequest().getParameterValues("contentTypeDefinitionId");

		logger.info("roles:" + roles);
		logger.info("groups:" + groups);
		logger.info("contentTypeDefinitionIds:" + contentTypeDefinitionIds);

		this.infoGluePrincipal = UserControllerProxy.getController().createUser(this.systemUserVO);
		if(roles != null && groups != null)
		{
			UserControllerProxy.getController().updateUser(systemUserVO, roles, groups);
		}
		
		if(contentTypeDefinitionIds != null && contentTypeDefinitionIds.length > 0 && !contentTypeDefinitionIds[0].equals(""))
			UserPropertiesController.getController().updateContentTypeDefinitions(this.getUserName(), contentTypeDefinitionIds);

		return "success";
	}

	public String doV3() throws Exception 
	{
		try
		{
			doExecute();
		}
		catch(ConstraintException e) 
        {
			this.availableRoles 				= RoleControllerProxy.getController().getAvailableRoles(this.getInfoGluePrincipal(), "Role.ManageUsers");
			this.availableGroups 				= GroupControllerProxy.getController().getAvailableGroups(this.getInfoGluePrincipal(), "Group.ManageUsers");
			this.contentTypeDefinitionVOList 	= ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList(ContentTypeDefinitionVO.EXTRANET_USER_PROPERTIES);

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

	public void setUserName(String userName)
	{
		this.systemUserVO.setUserName(userName);	
	}

    public String getUserName()
    {
        return this.systemUserVO.getUserName();
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
    
    public java.lang.String getEmail()
    {
    	return this.systemUserVO.getEmail();
    }
    
    public void setEmail(java.lang.String email)
    {
    	this.systemUserVO.setEmail(email);
    }
    
    public java.lang.String getPassword()
    {
    	return this.systemUserVO.getPassword();
    }
    
    public void setPassword(java.lang.String password)
    {
    	this.systemUserVO.setPassword(password);
    }

	public List getAvailableRoles()
	{
		return availableRoles;
	}

	public List getAvailableGroups()
	{
		return availableGroups;
	}

	public List getContentTypeDefinitionVOList()
	{
		return contentTypeDefinitionVOList;
	}
    
}
