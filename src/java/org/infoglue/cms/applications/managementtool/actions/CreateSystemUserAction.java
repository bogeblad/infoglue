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
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.UserPropertiesController;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.exception.ConstraintException;
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
		/*	
		for(int i=40000; i<100000; i++)
		{
			try
	    	{
				SystemUserVO systemUserVO2 = new SystemUserVO();
				systemUserVO2.setEmail("mattias.bogeblad" + i + "@gmail.com");
				systemUserVO2.setFirstName("Auto" + i);
				systemUserVO2.setLastName("User" + i);
				systemUserVO2.setIsActive(true);
				systemUserVO2.setModifiedDateTime(new Date());
				systemUserVO2.setPassword("password" + i + "");
				systemUserVO2.setSource("Infoglue autogen");
				systemUserVO2.setUserName("autouser" + i);
				
	        	String[] roles = new String[]{"cmsUser", "administrators"};
				String[] groups = new String[]{"A111", "Avd fšr analys och utvŠrdering", "X219"};
				String[] contentTypeDefinitionIds = null;
		
				UserControllerProxy.getController().createUser(systemUserVO2);
				if(roles != null || groups != null)
				{
					UserControllerProxy.getController().updateUser(systemUserVO2, roles, groups);
				}
				if(i % 100 == 0)
					logger.warn(".");
			}
			catch(ConstraintException e) 
		    {
				e.printStackTrace();
				break;
		    }
		}
		*/
    	try
    	{
    		ceb = this.systemUserVO.validate();
        	ceb.throwIfNotEmpty();		

        	String[] roles = getRequest().getParameterValues("roleName");
			String[] groups = getRequest().getParameterValues("groupName");
			if(roles == null)
				roles = new String[]{};
			if(groups == null)
				groups = new String[]{};

			String[] contentTypeDefinitionIds = getRequest().getParameterValues("contentTypeDefinitionId");

			logger.info("roles:" + roles);
			logger.info("groups:" + groups);
			logger.info("contentTypeDefinitionIds:" + contentTypeDefinitionIds);
	
			UserControllerProxy.getController().createUser(this.systemUserVO);
			UserControllerProxy.getController().updateUser(systemUserVO, roles, groups);
			
			if(contentTypeDefinitionIds != null && contentTypeDefinitionIds.length > 0 && !contentTypeDefinitionIds[0].equals(""))
				UserPropertiesController.getController().updateContentTypeDefinitions(this.getUserName(), contentTypeDefinitionIds);
		}
		catch(ConstraintException e) 
	    {
			this.availableRoles 				= RoleControllerProxy.getController().getAvailableRoles(this.getInfoGluePrincipal(), "Role.ManageUsers");
			this.availableGroups 				= GroupControllerProxy.getController().getAvailableGroups(this.getInfoGluePrincipal(), "Group.ManageUsers");
			this.contentTypeDefinitionVOList 	= ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList(ContentTypeDefinitionVO.EXTRANET_USER_PROPERTIES);
	
			e.setResult(INPUT);
			throw e;
	    }

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
