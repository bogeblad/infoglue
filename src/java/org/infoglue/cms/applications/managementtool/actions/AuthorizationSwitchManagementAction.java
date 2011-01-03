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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.management.AccessRight;
import org.infoglue.cms.entities.management.AccessRightGroup;
import org.infoglue.cms.entities.management.AccessRightGroupVO;
import org.infoglue.cms.entities.management.AccessRightRole;
import org.infoglue.cms.entities.management.AccessRightRoleVO;
import org.infoglue.cms.entities.management.AccessRightUser;
import org.infoglue.cms.entities.management.AccessRightUserVO;
import org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * This class is responsible for checking and fixing errors when one changes authsystem from one to another.
 */

public class AuthorizationSwitchManagementAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
    private final static Logger logger = Logger.getLogger(AuthorizationSwitchManagementAction.class.getName());

    private List invalidUsers = new ArrayList();
    private List invalidRoles = new ArrayList();
    private List invalidGroups = new ArrayList();

    private List users = new ArrayList();
    private List roles = new ArrayList();
    private List groups = new ArrayList();

    private List accessRights = new ArrayList();

    private String userName;
    private String roleName;
    private String groupName;
    
    private String newUserName;
    private String newRoleName;
    private String newGroupName;
    
    public String doInputUser() throws Exception
    {    	
    	this.users = UserControllerProxy.getController().getAllUsers();
    	this.accessRights = getAccessRightsUser();
    	
        return INPUT + "User";
    }

    public String doInputRole() throws Exception
    {   
    	this.roles = RoleControllerProxy.getController().getAllRoles();
    	this.accessRights = getAccessRightsRole();
    	
        return INPUT + "Role";
    }

    public String doInputGroup() throws Exception
    {
    	this.groups = GroupControllerProxy.getController().getAllGroups();
    	this.accessRights = getAccessRightsGroup();
    	
        return INPUT + "Group";
    }

    public String doUpdateUser() throws Exception
    {    	
    	if(this.getInfoGluePrincipal().getIsAdministrator())
    		updateAccessRightsUser(userName, newUserName);
    	else
    		throw new SystemException("You are not allowed to perform this action.");
    		
    	return "successUser";
    }

    public String doUpdateRole() throws Exception
    {    	
    	if(this.getInfoGluePrincipal().getIsAdministrator())
    		updateAccessRightsRole(roleName, newRoleName);
    	else
    		throw new SystemException("You are not allowed to perform this action.");
    	
    	return "successRole";
    }

    public String doUpdateGroup() throws Exception
    {    	
    	if(this.getInfoGluePrincipal().getIsAdministrator())
    		updateAccessRightsGroup(groupName, newGroupName);
    	else
    		throw new SystemException("You are not allowed to perform this action.");
    	
    	return "successGroup";
    }

    public String doExecute() throws Exception
    {   
    	/*
    	this.invalidUsers = getInvalidAccessRightsUser();
    	this.invalidRoles = getInvalidAccessRightsRole();
    	this.invalidGroups = getInvalidAccessRightsGroup();
    	*/
    	
        return SUCCESS;
    }


    private List getAccessRightsUser() throws Exception
    {
    	List accessRightUserList = new ArrayList();

        Database db = CastorDatabaseService.getDatabase();
        
        db.begin();

        try
        {
	
	        try
	        {
	        	accessRightUserList = AccessRightController.getController().getAccessRightUserList(userName, db);
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        db.commit();
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            db.rollback();
            throw new SystemException(e.getMessage());
        }
        finally
        {
            db.close();
        }
        
        return accessRightUserList;
    }

    private List getAccessRightsRole() throws Exception
    {
    	List accessRightRoleList = new ArrayList();

        Database db = CastorDatabaseService.getDatabase();
        
        db.begin();

        try
        {
	        try
	        {
	        	accessRightRoleList = AccessRightController.getController().getAccessRightRoleList(roleName, db, false);
	        }
	        catch(Exception e)
	        {
	            logger.error("An error occurred so we should not complete the transaction:" + e, e);
	        }
	        
	        db.commit();
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            try
	        {
            	db.rollback();
	        }
	        catch(Exception e2)
	        {
	            logger.error("An error occurred so we should not complete the transaction:" + e2.getMessage());
	        }
            throw new SystemException(e.getMessage());
        }
        finally
        {
        	try
	        {
        		db.close();
	        }
	        catch(Exception e2)
	        {
	            logger.error("An error occurred so we should not complete the transaction:" + e2.getMessage());
	        }
        }
        
        return accessRightRoleList;
    }

    private List getAccessRightsGroup() throws Exception
    {
    	List accessRightGroupList = new ArrayList();

        Database db = CastorDatabaseService.getDatabase();
        
        db.begin();

        try
        {
	
	        try
	        {
	        	accessRightGroupList = AccessRightController.getController().getAccessRightGroupList(groupName, db);
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        db.commit();
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            db.rollback();
            throw new SystemException(e.getMessage());
        }
        finally
        {
            db.close();
        }
        
        return accessRightGroupList;
    }

    
    private List getInvalidAccessRightsUser() throws Exception
    {
    	Set invalidUsers = new HashSet();

        Database db = CastorDatabaseService.getDatabase();
        
        db.begin();

        try
        {
	
	        try
	        {
	        	List users = UserControllerProxy.getController(db).getAllUsers();
	            List systemUserVOList = AccessRightController.getController().getAccessRightUserVOList(db);
	            
	            Iterator i = systemUserVOList.iterator();
	            
	        	while(i.hasNext())
	            {
	                AccessRightUserVO accessRightUserVO = (AccessRightUserVO)i.next(); 
	                
	                boolean isValid = false;
	                
	                Iterator userIterator = users.iterator();
		            
		        	while(userIterator.hasNext())
		            {
		                InfoGluePrincipal principal = (InfoGluePrincipal)userIterator.next();
		                if(principal.getName().equalsIgnoreCase(accessRightUserVO.getUserName()))
		                {
		                	isValid = true;
		                	break;
		                }
		            }
	                
		        	if(!isValid)
		        		invalidUsers.add(accessRightUserVO.getUserName());
	            }	            
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        db.commit();
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            db.rollback();
            throw new SystemException(e.getMessage());
        }
        finally
        {
            db.close();
        }
        
        List invalidUsersList = new ArrayList();
        invalidUsersList.addAll(invalidUsers);

        return invalidUsersList;
    }

    private List getInvalidAccessRightsRole() throws Exception
    {
    	Set invalidRoles = new HashSet();

        Database db = CastorDatabaseService.getDatabase();
        
        db.begin();

        try
        {
	
	        String name = "AccessRightRole names";
	        String description = "Checks if the Role names given exists in the current authorizationModule.";
	
	        try
	        {
	        	List users = RoleControllerProxy.getController(db).getAllRoles();
	            List systemRoleVOList = AccessRightController.getController().getAccessRightRoleVOList(db);
	            
	            Iterator i = systemRoleVOList.iterator();
	            
	        	while(i.hasNext())
	            {
	                AccessRightRoleVO accessRightRoleVO = (AccessRightRoleVO)i.next(); 
	                
	                boolean isValid = false;
	                
	                Iterator userIterator = users.iterator();
		            
		        	while(userIterator.hasNext())
		            {
		        		InfoGlueRole role = (InfoGlueRole)userIterator.next();
		                if(role.getName().equalsIgnoreCase(accessRightRoleVO.getRoleName()))
		                {
		                	isValid = true;
		                	break;
		                }
		            }
	                
		        	if(!isValid)
		        		invalidRoles.add(accessRightRoleVO.getRoleName());
	            }
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        db.commit();
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            db.rollback();
            throw new SystemException(e.getMessage());
        }
        finally
        {
            db.close();
        }

        List invalidRolesList = new ArrayList();
        invalidRolesList.addAll(invalidRoles);

        return invalidRolesList;
    }

    private List getInvalidAccessRightsGroup() throws Exception
    {
    	Set invalidGroups = new HashSet();

        Database db = CastorDatabaseService.getDatabase();
        
        db.begin();

        try
        {
	
	        String name = "AccessRightGroup names";
	        String description = "Checks if the user names given exists in the current authorizationModule.";
	
	        try
	        {	        	
	        	List users = GroupControllerProxy.getController(db).getAllGroups();
	            List systemGroupVOList = AccessRightController.getController().getAccessRightGroupVOList(db);
	            
	            Iterator i = systemGroupVOList.iterator();
	            
	        	while(i.hasNext())
	            {
	                AccessRightGroupVO accessRightGroupVO = (AccessRightGroupVO)i.next(); 
	                
	                boolean isValid = false;
	                
	                Iterator userIterator = users.iterator();
		            
		        	while(userIterator.hasNext())
		            {
		                InfoGlueGroup group = (InfoGlueGroup)userIterator.next();
		                if(group.getName().equalsIgnoreCase(accessRightGroupVO.getGroupName()))
		                {
		                	isValid = true;
		                	break;
		                }
		            }
	                
		        	if(!isValid)
		        		invalidGroups.add(accessRightGroupVO.getGroupName());
	            }
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        db.commit();
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            db.rollback();
            throw new SystemException(e.getMessage());
        }
        finally
        {
            db.close();
        }

        List invalidGroupsList = new ArrayList();
        invalidGroupsList.addAll(invalidGroupsList);

        return invalidGroupsList;
    }
    
    private void updateAccessRightsUser(String userName, String newUserName) throws Exception
    {
        Database db = CastorDatabaseService.getDatabase();
        
        db.begin();

        try
        {
	        try
	        {
	            List accessRightUserList = AccessRightController.getController().getAccessRightUserList(userName, db);
	            
	            Iterator i = accessRightUserList.iterator();
	            
	        	while(i.hasNext())
	            {
	                AccessRightUser accessRightUser = (AccessRightUser)i.next(); 
	                AccessRight accessRight = accessRightUser.getAccessRight();

	                boolean exists = false;
	                Iterator usersIterator = accessRight.getUsers().iterator();
	                while(usersIterator.hasNext())
	                {
	                	AccessRightUser currentAccessRightUser = (AccessRightUser)usersIterator.next();
	                	if(currentAccessRightUser.getUserName().equals(newUserName))
	                		exists = true;
	                }

	                if(!exists)
	                {
		                //accessRightUser.setUserName(newUserName);
					    AccessRightUserVO accessRightUserVO = new AccessRightUserVO();
					    accessRightUserVO.setUserName(newUserName);
					    AccessRightUser newAccessRightUser = AccessRightController.getController().createAccessRightUser(db, accessRightUserVO, accessRight);
					    accessRight.getUsers().add(newAccessRightUser);
	                }
	            }	            
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        db.commit();
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            db.rollback();
            throw new SystemException(e.getMessage());
        }
        finally
        {
            db.close();
        }        
    }

    private void updateAccessRightsRole(String roleName, String newRoleName) throws Exception
    {
        Database db = CastorDatabaseService.getDatabase();
        
        db.begin();

        try
        {
	        try
	        {
	            List accessRightRoleList = AccessRightController.getController().getAccessRightRoleList(roleName, db, false);
	            
	            Iterator i = accessRightRoleList.iterator();
	            
	        	while(i.hasNext())
	            {
	                AccessRightRole accessRightRole = (AccessRightRole)i.next(); 
	                AccessRight accessRight = accessRightRole.getAccessRight();
	                
	                if(accessRight != null)
	                {
		                boolean exists = false;

		                Iterator rolesIterator = accessRight.getRoles().iterator();
		                while(rolesIterator.hasNext())
		                {
		                	AccessRightRole currentAccessRightRole = (AccessRightRole)rolesIterator.next();
		                	if(currentAccessRightRole.getRoleName().equals(newRoleName))
		                		exists = true;
		                }
	                
		                if(!exists)
		                {
			                //accessRightRole.setRoleName(newRoleName);
						    AccessRightRoleVO accessRightRoleVO = new AccessRightRoleVO();
						    accessRightRoleVO.setRoleName(newRoleName);
						    AccessRightRole newAccessRightRole = AccessRightController.getController().createAccessRightRole(db, accessRightRoleVO, accessRight);
						    accessRight.getRoles().add(newAccessRightRole);
		                }
	                }	                
	            }	            
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        db.commit();
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            db.rollback();
            throw new SystemException(e.getMessage());
        }
        finally
        {
            db.close();
        }        
    }

    private void updateAccessRightsGroup(String groupName, String newGroupName) throws Exception
    {
        Database db = CastorDatabaseService.getDatabase();
        
        db.begin();

        try
        {
	        try
	        {
	            List accessRightGroupList = AccessRightController.getController().getAccessRightGroupList(groupName, db);
	            
	            Iterator i = accessRightGroupList.iterator();
	            
	        	while(i.hasNext())
	            {
	                AccessRightGroup accessRightGroup = (AccessRightGroup)i.next(); 
	                AccessRight accessRight = accessRightGroup.getAccessRight();

	                boolean exists = false;
	                Iterator groupsIterator = accessRight.getGroups().iterator();
	                while(groupsIterator.hasNext())
	                {
	                	AccessRightGroup currentAccessRightGroup = (AccessRightGroup)groupsIterator.next();
	                	if(currentAccessRightGroup.getGroupName().equals(newGroupName))
	                		exists = true;
	                }
	                
	                if(!exists)
	                {
		                //accessRightGroup.setGroupName(newGroupName);
					    AccessRightGroupVO accessRightGroupVO = new AccessRightGroupVO();
					    accessRightGroupVO.setGroupName(newGroupName);
					    AccessRightGroup newAccessRightGroup = AccessRightController.getController().createAccessRightGroup(db, accessRightGroupVO, accessRight);
					    accessRight.getGroups().add(newAccessRightGroup);
	                }
	            }	            
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        db.commit();
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            db.rollback();
            throw new SystemException(e.getMessage());
        }
        finally
        {
            db.close();
        }        
    }

	public List getInvalidGroups()
	{
		return invalidGroups;
	}

	public List getInvalidRoles()
	{
		return invalidRoles;
	}

	public List getInvalidUsers()
	{
		return invalidUsers;
	}

	public String getGroupName()
	{
		return groupName;
	}

	public void setGroupName(String groupName)
	{
		this.groupName = groupName;
	}

	public String getRoleName()
	{
		return roleName;
	}

	public void setRoleName(String roleName)
	{
		this.roleName = roleName;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public String getNewGroupName()
	{
		return newGroupName;
	}

	public void setNewGroupName(String newGroupName)
	{
		this.newGroupName = newGroupName;
	}

	public String getNewRoleName()
	{
		return newRoleName;
	}

	public void setNewRoleName(String newRoleName)
	{
		this.newRoleName = newRoleName;
	}

	public String getNewUserName()
	{
		return newUserName;
	}

	public void setNewUserName(String newUserName)
	{
		this.newUserName = newUserName;
	}

	public List getGroups()
	{
		return groups;
	}

	public List getRoles()
	{
		return roles;
	}

	public List getUsers()
	{
		return users;
	}

	public List getAccessRights()
	{
		return accessRights;
	}

}
