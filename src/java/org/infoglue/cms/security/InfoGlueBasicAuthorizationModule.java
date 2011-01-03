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

package org.infoglue.cms.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.BaseController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupController;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleController;
import org.infoglue.cms.controllers.kernel.impl.simple.SystemUserController;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Group;
import org.infoglue.cms.entities.management.GroupVO;
import org.infoglue.cms.entities.management.Role;
import org.infoglue.cms.entities.management.RoleVO;
import org.infoglue.cms.entities.management.SystemUser;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.util.Timer;

/**
 * @author Mattias Bogeblad
 *
 * This authentication module authenticates an user against the ordinary infoglue database.
 */

public class InfoGlueBasicAuthorizationModule extends BaseController implements AuthorizationModule, Serializable
{
    private final static Logger logger = Logger.getLogger(InfoGlueBasicAuthorizationModule.class.getName());

	private Properties extraProperties = null;
	private transient Database transactionObject = null;
	
	/**
	 * Gets is the implementing class can update as well as read 
	 */
	
	public boolean getSupportUpdate() 
	{
		return true;
	}

	/**
	 * Gets is the implementing class can delete as well as read 
	 */
	
	public boolean getSupportDelete()
	{
		return true;
	}
	
	/**
	 * Gets is the implementing class can create as well as read 
	 */
	
	public boolean getSupportCreate()
	{
		return true;
	}

	/**
	 * Gets an authorized InfoGluePrincipal. If the user has logged in with the root-account
	 * we immediately return - otherwise we populate it.
	 */
	private Category getCategory(String className)
    {
        Enumeration enumeration = Logger.getCurrentCategories();
        while(enumeration.hasMoreElements())
        {
            Category category = (Category)enumeration.nextElement();
            if(category.getName().equalsIgnoreCase(className))
                return category;
        }
        
        Category category = Category.getInstance(className);
       
        return category;
    }
	private void setDebug(Level level, String className)
	{
		Category category = getCategory(className);
		if(category != null)
		{
			category.setLevel(level);
		}
	}

	public InfoGluePrincipal getAuthorizedInfoGluePrincipal(String userName) throws Exception
	{
	    if(userName == null || userName.equals(""))
	    {
	    	try
	    	{
	    		throw new Exception("userName was null or empty - fix your templates:" + userName);
	    	}
	    	catch(Exception e)
	    	{
	    		logger.warn(e.getMessage(), e);
	    	}

	    	return null;
	    }
	    
		InfoGluePrincipal infogluePrincipal = null;
		
		Timer t = new Timer();
		
		String administratorUserName = CmsPropertyHandler.getAdministratorUserName();
		String administratorEmail 	 = CmsPropertyHandler.getAdministratorEmail();
		
		final boolean isAdministrator = (userName != null && userName.equalsIgnoreCase(administratorUserName)) ? true : false;
		if(isAdministrator)
		{
			infogluePrincipal = new InfoGluePrincipal(userName, "System", "Administrator", administratorEmail, new ArrayList(), new ArrayList(), isAdministrator, this);
		}
		else
		{	
			List<InfoGlueRole> roles = new ArrayList<InfoGlueRole>();
			List<InfoGlueGroup> groups = new ArrayList<InfoGlueGroup>();
			
			if(transactionObject == null)
			{
				Database db = CastorDatabaseService.getDatabase();
	
				try 
				{
					beginTransaction(db);
					
					SystemUser systemUser = SystemUserController.getController().getReadOnlySystemUserWithName(userName, db);
					if(logger.isInfoEnabled())
						t.printElapsedTime("systemUser AAA took:");
					//setDebug(Level.DEBUG, "org.exolab.castor.jdo");
					if(systemUser != null)
					{
						Collection<RoleVO> roleVOList = RoleController.getController().getRoleVOList(userName, db);
				    	if(logger.isInfoEnabled())
				    		t.printElapsedTime("getRoleVOList took:");

						Iterator<RoleVO> roleVOListIterator = roleVOList.iterator();
						while(roleVOListIterator.hasNext())
						{
							RoleVO roleVO = roleVOListIterator.next();
							if(logger.isInfoEnabled())
								logger.info("Adding role:" + roleVO.getRoleName());
							InfoGlueRole infoGlueRole = new InfoGlueRole(roleVO.getRoleName(), roleVO.getDescription(), this);
							roles.add(infoGlueRole);
						}
						
						/*
						Iterator roleListIterator = systemUser.getRoles().iterator();
						while(roleListIterator.hasNext())
						{
							Role role = (Role)roleListIterator.next();
							if(logger.isInfoEnabled())
								logger.info("Adding role:" + role.getRoleName());
						    InfoGlueRole infoGlueRole = new InfoGlueRole(role.getRoleName(), role.getDescription(), this);
							roles.add(infoGlueRole);
						}
						*/
		
				    	Collection<GroupVO> groupVOList = GroupController.getController().getGroupVOList(userName, db);
				    	if(logger.isInfoEnabled())
				    		t.printElapsedTime("groupVOList took:");

						Iterator<GroupVO> groupVOListIterator = groupVOList.iterator();
						while(groupVOListIterator.hasNext())
						{
							GroupVO groupVO = groupVOListIterator.next();
							if(logger.isInfoEnabled())
								logger.info("Adding group:" + groupVO.getGroupName());
							InfoGlueGroup infoGlueGroup = new InfoGlueGroup(groupVO.getGroupName(), groupVO.getDescription(), this);
							groups.add(infoGlueGroup);
						}
						
						/*
						Iterator groupListIterator = systemUser.getGroups().iterator();
						while(groupListIterator.hasNext())
						{
						    Group group = (Group)groupListIterator.next();
						    if(logger.isInfoEnabled())
						    	logger.info("Adding group:" + group.getGroupName());
						    InfoGlueGroup infoGlueGroup = new InfoGlueGroup(group.getGroupName(), group.getDescription(), this);
							groups.add(infoGlueGroup);
						}
						*/
						
						infogluePrincipal = new InfoGluePrincipal(userName, systemUser.getFirstName(), systemUser.getLastName(), systemUser.getEmail(), roles, groups, isAdministrator, this);
					}
					else
					{
					    logger.warn("Could not find user with userName '" + userName + "' - fix your template logic.");
					    infogluePrincipal = null;
					}
					//setDebug(Level.ERROR, "org.exolab.castor.jdo");
					
					commitTransaction(db);
				} 
				catch (Exception e) 
				{
					logger.info("An error occurred trying to get SystemUser for " + userName + ":" + e.getMessage());
					rollbackTransaction(db);
					throw new SystemException(e.getMessage());
				}
			}
			else
			{
			    SystemUser systemUser = SystemUserController.getController().getReadOnlySystemUserWithName(userName, transactionObject);
			    if(logger.isInfoEnabled())
			    	t.printElapsedTime("systemUser BBB took:");
			    
			    if(systemUser != null)
			    {
			    	Collection<RoleVO> roleVOList = RoleController.getController().getRoleVOList(userName, transactionObject);
			    	if(logger.isInfoEnabled())
			    		t.printElapsedTime("getRoleVOList took:");

					Iterator<RoleVO> roleVOListIterator = roleVOList.iterator();
					while(roleVOListIterator.hasNext())
					{
						RoleVO roleVO = roleVOListIterator.next();
						if(logger.isInfoEnabled())
							logger.info("Adding role:" + roleVO.getRoleName());
						InfoGlueRole infoGlueRole = new InfoGlueRole(roleVO.getRoleName(), roleVO.getDescription(), this);
						roles.add(infoGlueRole);
					}
					
					/*
				    Iterator roleListIterator = systemUser.getRoles().iterator();
					while(roleListIterator.hasNext())
					{
						Role role = (Role)roleListIterator.next();
						if(logger.isInfoEnabled())
							logger.info("Adding role:" + role.getRoleName());
						InfoGlueRole infoGlueRole = new InfoGlueRole(role.getRoleName(), role.getDescription(), this);
						roles.add(infoGlueRole);
					}
			    	*/

			    	Collection<GroupVO> groupVOList = GroupController.getController().getGroupVOList(userName, transactionObject);
			    	if(logger.isInfoEnabled())
			    		t.printElapsedTime("groupVOList took:");

					Iterator<GroupVO> groupVOListIterator = groupVOList.iterator();
					while(groupVOListIterator.hasNext())
					{
						GroupVO groupVO = (GroupVO)groupVOListIterator.next();
						if(logger.isInfoEnabled())
							logger.info("Adding group:" + groupVO.getGroupName());
						InfoGlueGroup infoGlueGroup = new InfoGlueGroup(groupVO.getGroupName(), groupVO.getDescription(), this);
						groups.add(infoGlueGroup);
					}
					
					/*
					Iterator groupListIterator = systemUser.getGroups().iterator();
					while(groupListIterator.hasNext())
					{
					    Group group = (Group)groupListIterator.next();
					    if(logger.isInfoEnabled())
					    	logger.info("Adding group:" + group.getGroupName());
						InfoGlueGroup infoGlueGroup = new InfoGlueGroup(group.getGroupName(), group.getDescription(), this);
						groups.add(infoGlueGroup);
					}
					*/
					
					infogluePrincipal = new InfoGluePrincipal(userName, systemUser.getFirstName(), systemUser.getLastName(), systemUser.getEmail(), roles, groups, isAdministrator, this);
			    }
				else
				{
				    logger.warn("Could not find user with userName '" + userName + "' - fix your template logic.");
				    infogluePrincipal = null;
				}
			    //setDebug(Level.ERROR, "org.exolab.castor.jdo");
			}
		}
		
		if(logger.isInfoEnabled())
			t.printElapsedTime("systemUser total took:");
		
		return infogluePrincipal;
	}

	/**
	 * Gets an authorized InfoGlueRole.
	 */
	
	public InfoGlueRole getAuthorizedInfoGlueRole(String roleName) throws Exception
	{
		InfoGlueRole infoglueRole = null;

		RoleVO roleVO = null;
		
		if(transactionObject == null)
		{
		    roleVO = RoleController.getController().getRoleVOWithId(roleName);
		}
		else
		{
		    roleVO = RoleController.getController().getRoleWithName(roleName, transactionObject).getValueObject();
		}
		
		infoglueRole = new InfoGlueRole(roleVO.getRoleName(), roleVO.getDescription(), this);
				
		return infoglueRole;
	}

	/**
	 * Gets an authorized InfoGlueGroup.
	 */
	
	public InfoGlueGroup getAuthorizedInfoGlueGroup(String groupName) throws Exception
	{
		InfoGlueGroup infoglueGroup = null;
		
		GroupVO groupVO = null;
		if(transactionObject == null)
		{
		    groupVO = GroupController.getController().getGroupVOWithId(groupName);
		}
		else
		{
		    groupVO = GroupController.getController().getGroupWithName(groupName, transactionObject).getValueObject();
		}

	    infoglueGroup = new InfoGlueGroup(groupVO.getGroupName(), groupVO.getDescription(), this);
				
		return infoglueGroup;
	}

	
	/**
	 * This method gets a users roles
	 */
	
	public List authorizeUser(String userName) throws Exception
	{
		List<InfoGlueRole> roles = new ArrayList<InfoGlueRole>();
		List<InfoGlueGroup> groups = new ArrayList<InfoGlueGroup>();
		
		String administratorUserName = CmsPropertyHandler.getAdministratorUserName();
		
		boolean isAdministrator = userName.equalsIgnoreCase(administratorUserName) ? true : false;
		if(isAdministrator)
			return roles;
		
		if(transactionObject == null)
		{
			List<RoleVO> roleVOList = RoleController.getController().getRoleVOList(userName);
			Iterator<RoleVO> roleVOListIterator = roleVOList.iterator();
			while(roleVOListIterator.hasNext())
			{
				RoleVO roleVO = roleVOListIterator.next();
			    if(logger.isInfoEnabled())
			    	logger.info("Adding role:" + roleVO.getRoleName());

				InfoGlueRole infoGlueRole = new InfoGlueRole(roleVO.getRoleName(), roleVO.getDescription(), this);
				roles.add(infoGlueRole);
			}
	
			List<GroupVO> groupVOList = GroupController.getController().getGroupVOList(userName);
			Iterator<GroupVO> groupVOListIterator = groupVOList.iterator();
			while(groupVOListIterator.hasNext())
			{
			    GroupVO groupVO = groupVOListIterator.next();
			    if(logger.isInfoEnabled())
			    	logger.info("Adding group:" + groupVO.getGroupName());
			    
			    InfoGlueGroup infoGlueGroup = new InfoGlueGroup(groupVO.getGroupName(), groupVO.getDescription(), this);
				groups.add(infoGlueGroup);
			}
		}
		else
		{
			List<RoleVO> roleList = RoleController.getController().getRoleVOList(userName, transactionObject);
			Iterator<RoleVO> roleListIterator = roleList.iterator();
			while(roleListIterator.hasNext())
			{
				RoleVO role = roleListIterator.next();
				if(logger.isInfoEnabled())
			    	logger.info("Adding role:" + role.getRoleName());

				InfoGlueRole infoGlueRole = new InfoGlueRole(role.getRoleName(), role.getDescription(), this);
				roles.add(infoGlueRole);
			}
	
			List<GroupVO> groupList = GroupController.getController().getGroupVOList(userName, transactionObject);
			Iterator<GroupVO> groupListIterator = groupList.iterator();
			while(groupListIterator.hasNext())
			{
				GroupVO group = groupListIterator.next();
				if(logger.isInfoEnabled())
			    	logger.info("Adding group:" + group.getGroupName());

				InfoGlueGroup infoGlueGroup = new InfoGlueGroup(group.getGroupName(), group.getDescription(), this);
				groups.add(infoGlueGroup);
			}


			/*
			Collection roleList = RoleController.getController().getRoleList(userName, transactionObject);
			Iterator roleListIterator = roleList.iterator();
			while(roleListIterator.hasNext())
			{
				Role role = (Role)roleListIterator.next();
				if(logger.isInfoEnabled())
			    	logger.info("Adding role:" + role.getRoleName());

				InfoGlueRole infoGlueRole = new InfoGlueRole(role.getRoleName(), role.getDescription(), this);
				roles.add(infoGlueRole);
			}
	
			Collection groupList = GroupController.getController().getGroupList(userName, transactionObject);
			Iterator groupListIterator = groupList.iterator();
			while(groupListIterator.hasNext())
			{
			    Group group = (Group)groupListIterator.next();
			    if(logger.isInfoEnabled())
			    	logger.info("Adding group:" + group.getGroupName());
			    
			    InfoGlueGroup infoGlueGroup = new InfoGlueGroup(group.getGroupName(), group.getDescription(), this);
				groups.add(infoGlueGroup);
			}
			*/
		}
		
		return groups;
	}

	/**
	 * This method gets a list of roles
	 */
	
	public List getRoles() throws Exception
	{
		List roles = new ArrayList();
		
		if(transactionObject == null)
		{
			List roleVOList = RoleController.getController().getRoleVOList();
			Iterator roleVOListIterator = roleVOList.iterator();
			while(roleVOListIterator.hasNext())
			{
				RoleVO roleVO = (RoleVO)roleVOListIterator.next();
				InfoGlueRole infoGlueRole = new InfoGlueRole(roleVO.getRoleName(), roleVO.getDescription(), this);
				roles.add(infoGlueRole);
			}
		}
		else
		{
			List roleVOList = RoleController.getController().getRoleVOList(this.transactionObject);
			Iterator roleVOListIterator = roleVOList.iterator();
			while(roleVOListIterator.hasNext())
			{
				RoleVO roleVO = (RoleVO)roleVOListIterator.next();
				InfoGlueRole infoGlueRole = new InfoGlueRole(roleVO.getRoleName(), roleVO.getDescription(), this);
				roles.add(infoGlueRole);
			}
		}
		
		return roles;
	}

    public List getGroups() throws Exception
    {
        List groups = new ArrayList();
		
		if(transactionObject == null)
		{
			List groupVOList = GroupController.getController().getGroupVOList();
			Iterator groupVOListIterator = groupVOList.iterator();
			while(groupVOListIterator.hasNext())
			{
			    GroupVO groupVO = (GroupVO)groupVOListIterator.next();
				InfoGlueGroup infoGlueGroup = new InfoGlueGroup(groupVO.getGroupName(), groupVO.getDescription(), this);
				groups.add(infoGlueGroup);
			}
		}
		else
		{
			List groupVOList = GroupController.getController().getGroupVOList(this.transactionObject);
			Iterator groupVOListIterator = groupVOList.iterator();
			while(groupVOListIterator.hasNext())
			{
			    GroupVO groupVO = (GroupVO)groupVOListIterator.next();
				InfoGlueGroup infoGlueGroup = new InfoGlueGroup(groupVO.getGroupName(), groupVO.getDescription(), this);
				groups.add(infoGlueGroup);
			}		    
		}
		
		return groups;
    }

    
	/**
	 * This method gets a list of users
	 */
	
	public List getUsers() throws Exception
	{
		List users = new ArrayList();
		
		if(transactionObject == null)
		{
			Database db = CastorDatabaseService.getDatabase();

			try 
			{
				beginTransaction(db);
				
				List systemUsers = SystemUserController.getController().getSystemUserList(db);
				Iterator systemUserListIterator = systemUsers.iterator();
				while(systemUserListIterator.hasNext())
				{
					SystemUser systemUser = (SystemUser)systemUserListIterator.next();
		
					List roles = new ArrayList();
					Collection roleList = systemUser.getRoles();
					Iterator roleListIterator = roleList.iterator();
					while(roleListIterator.hasNext())
					{
						Role role = (Role)roleListIterator.next();
						InfoGlueRole infoGlueRole = new InfoGlueRole(role.getRoleName(), role.getDescription(), this);
						roles.add(infoGlueRole);
					}
					
					List groups = new ArrayList();
					Collection groupList = systemUser.getGroups();
					Iterator groupListIterator = groupList.iterator();
					while(groupListIterator.hasNext())
					{
					    Group group = (Group)groupListIterator.next();
						InfoGlueGroup infoGlueGroup = new InfoGlueGroup(group.getGroupName(), group.getDescription(), this);
						groups.add(infoGlueGroup);
					}
					
					InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUser.getUserName(), systemUser.getFirstName(), systemUser.getLastName(), systemUser.getEmail(), roles, groups, false, this);
					
					users.add(infoGluePrincipal);
				}
				
				commitTransaction(db);
			} 
			catch (Exception e) 
			{
				logger.error("An error occurred so we should not complete the transaction:" + e);
				rollbackTransaction(db);
				throw new SystemException("An error occurred so we should not complete the transaction:" + e, e);
			}
		}
		else
		{
			List systemUsers = SystemUserController.getController().getSystemUserList(transactionObject);
			Iterator systemUserListIterator = systemUsers.iterator();
			while(systemUserListIterator.hasNext())
			{
				SystemUser systemUser = (SystemUser)systemUserListIterator.next();
	
				List roles = new ArrayList();
				Collection roleList = systemUser.getRoles();
				Iterator roleListIterator = roleList.iterator();
				while(roleListIterator.hasNext())
				{
					Role role = (Role)roleListIterator.next();
					InfoGlueRole infoGlueRole = new InfoGlueRole(role.getRoleName(), role.getDescription(), this);
					roles.add(infoGlueRole);
				}
				
				List groups = new ArrayList();
				Collection groupList = systemUser.getGroups();
				Iterator groupListIterator = groupList.iterator();
				while(groupListIterator.hasNext())
				{
				    Group group = (Group)groupListIterator.next();
					InfoGlueGroup infoGlueGroup = new InfoGlueGroup(group.getGroupName(), group.getDescription(), this);
					groups.add(infoGlueGroup);
				}
				
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUser.getUserName(), systemUser.getFirstName(), systemUser.getLastName(), systemUser.getEmail(), roles, groups, false, this);
				
				users.add(infoGluePrincipal);
			}
		}
		
		return users;
	}

	public List getFilteredUsers(String firstName, String lastName, String userName, String email, String[] roleIds) throws Exception
	{
		List users = new ArrayList();
		
		if(transactionObject == null)
		{
			List systemUserVOList = SystemUserController.getController().getFilteredSystemUserVOList(firstName, lastName, userName, email, roleIds);
			Iterator systemUserVOListIterator = systemUserVOList.iterator();
			while(systemUserVOListIterator.hasNext())
			{
				SystemUserVO systemUserVO = (SystemUserVO)systemUserVOListIterator.next();
				
				List roles = new ArrayList();
				Collection roleVOList = RoleController.getController().getRoleVOList(systemUserVO.getUserName());
				Iterator roleVOListIterator = roleVOList.iterator();
				while(roleVOListIterator.hasNext())
				{
					RoleVO roleVO = (RoleVO)roleVOListIterator.next();
					InfoGlueRole infoGlueRole = new InfoGlueRole(roleVO.getRoleName(), roleVO.getDescription(), this);
					roles.add(infoGlueRole);
				}
				
				List groups = new ArrayList();
				Collection groupVOList = GroupController.getController().getGroupVOList(systemUserVO.getUserName());
				Iterator groupVOListIterator = groupVOList.iterator();
				while(groupVOListIterator.hasNext())
				{
				    GroupVO groupVO = (GroupVO)groupVOListIterator.next();
					InfoGlueGroup infoGlueGroup = new InfoGlueGroup(groupVO.getGroupName(), groupVO.getDescription(), this);
					groups.add(infoGlueGroup);
				}
				
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUserVO.getUserName(), systemUserVO.getFirstName(), systemUserVO.getLastName(), systemUserVO.getEmail(), roles, groups, false, this);
				users.add(infoGluePrincipal);
			}
		}
		else
		{
			List systemUserList = SystemUserController.getController().getFilteredSystemUserList(firstName, lastName, userName, email, roleIds, transactionObject);
			Iterator systemUserListIterator = systemUserList.iterator();
			while(systemUserListIterator.hasNext())
			{
				SystemUser systemUser = (SystemUser)systemUserListIterator.next();
				
				List roles = new ArrayList();
				Collection roleList = RoleController.getController().getRoleList(systemUser.getUserName(), transactionObject);
				Iterator roleListIterator = roleList.iterator();
				while(roleListIterator.hasNext())
				{
					Role role = (Role)roleListIterator.next();
					InfoGlueRole infoGlueRole = new InfoGlueRole(role.getRoleName(), role.getDescription(), this);
					roles.add(infoGlueRole);
				}
				
				List groups = new ArrayList();
				Collection groupList = GroupController.getController().getGroupList(systemUser.getUserName(), transactionObject);
				Iterator groupListIterator = groupList.iterator();
				while(groupListIterator.hasNext())
				{
				    Group group = (Group)groupListIterator.next();
					InfoGlueGroup infoGlueGroup = new InfoGlueGroup(group.getGroupName(), group.getDescription(), this);
					groups.add(infoGlueGroup);
				}
				
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUser.getUserName(), systemUser.getFirstName(), systemUser.getLastName(), systemUser.getEmail(), roles, groups, false, this);
				users.add(infoGluePrincipal);
			}
		}
		
		return users;
	}
	
	public List getUsers(String roleName) throws Exception
	{
		return getRoleUsers(roleName);
	}

    public List getRoleUsers(String roleName) throws Exception
    {
        logger.info("roleName:" + roleName);
		List users = new ArrayList();
		
		if(transactionObject == null)
		{
			List systemUserVOList = RoleController.getController().getRoleSystemUserVOList(roleName);
			Iterator systemUserVOListIterator = systemUserVOList.iterator();
			while(systemUserVOListIterator.hasNext())
			{
				SystemUserVO systemUserVO = (SystemUserVO)systemUserVOListIterator.next();
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUserVO.getUserName(), systemUserVO.getFirstName(), systemUserVO.getLastName(), systemUserVO.getEmail(), new ArrayList(), new ArrayList(), false, this);
				users.add(infoGluePrincipal);
			}
		}
		else
		{
			List systemUserVOList = RoleController.getController().getRoleSystemUserVOList(roleName, transactionObject);
			Iterator systemUserVOListIterator = systemUserVOList.iterator();
			while(systemUserVOListIterator.hasNext())
			{
				SystemUserVO systemUserVO = (SystemUserVO)systemUserVOListIterator.next();
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUserVO.getUserName(), systemUserVO.getFirstName(), systemUserVO.getLastName(), systemUserVO.getEmail(), new ArrayList(), new ArrayList(), false, this);
				users.add(infoGluePrincipal);
			}
		}
		
		return users;
	}

    public List getGroupUsers(String groupName) throws Exception
    {
        logger.info("groupName:" + groupName);
		List users = new ArrayList();
		
		if(transactionObject == null)
		{
			List systemUserVOList = GroupController.getController().getGroupSystemUserVOList(groupName);
			Iterator systemUserVOListIterator = systemUserVOList.iterator();
			while(systemUserVOListIterator.hasNext())
			{
				SystemUserVO systemUserVO = (SystemUserVO)systemUserVOListIterator.next();
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUserVO.getUserName(), systemUserVO.getFirstName(), systemUserVO.getLastName(), systemUserVO.getEmail(), new ArrayList(), new ArrayList(), false, this);
				users.add(infoGluePrincipal);
			}
		}
		else
		{
			List systemUserVOList = GroupController.getController().getGroupSystemUserVOList(groupName, transactionObject);
			Iterator systemUserVOListIterator = systemUserVOList.iterator();
			while(systemUserVOListIterator.hasNext())
			{
				SystemUserVO systemUserVO = (SystemUserVO)systemUserVOListIterator.next();
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUserVO.getUserName(), systemUserVO.getFirstName(), systemUserVO.getLastName(), systemUserVO.getEmail(), new ArrayList(), new ArrayList(), false, this);
				users.add(infoGluePrincipal);
			}
		}

		return users;
    }

	public void createInfoGluePrincipal(SystemUserVO systemUserVO) throws Exception
	{
	    if(transactionObject == null)
		{
	        SystemUserController.getController().create(systemUserVO);
		}
	    else
	    {
	        SystemUserController.getController().create(systemUserVO, transactionObject);
	    }
	}

	public void updateInfoGluePrincipal(SystemUserVO systemUserVO, String[] roleNames, String[] groupNames) throws Exception
	{
	    if(transactionObject == null)
		{
			SystemUserController.getController().update(systemUserVO, roleNames, groupNames);
		}
	    else
	    {
			SystemUserController.getController().update(systemUserVO, roleNames, groupNames, transactionObject);
	    }
	}

	public void updateInfoGluePrincipal(SystemUserVO systemUserVO, String oldPassword, String[] roleNames, String[] groupNames) throws Exception
	{
	    if(transactionObject == null)
		{
			SystemUserController.getController().update(systemUserVO, oldPassword, roleNames, groupNames);
		}
	    else
	    {
			SystemUserController.getController().update(systemUserVO, oldPassword, roleNames, groupNames, transactionObject);
	    }
	}

	/**
	 * This method is used to send out a newpassword to an existing users.  
	 */

	public void updateInfoGluePrincipalPassword(String userName) throws Exception
	{
	    if(transactionObject == null)
		{
	        SystemUserController.getController().updatePassword(userName);
		}
	    else
	    {
	        SystemUserController.getController().updatePassword(userName, transactionObject);
	    }
	}

	/**
	 * This method is used to send out a newpassword to an existing users.  
	 */

	public void updateInfoGlueAnonymousPrincipalPassword() throws Exception
	{
	    if(transactionObject == null)
		{
	        SystemUserController.getController().updateAnonymousPassword(CmsPropertyHandler.getAnonymousUser());
		}
	    else
	    {
	        SystemUserController.getController().updateAnonymousPassword(CmsPropertyHandler.getAnonymousUser(), transactionObject);
	    }
	}

	/**
	 * This method is used to let a user update his password by giving his/her old one first.  
	 */

	public void updateInfoGluePrincipalPassword(String userName, String oldPassword, String newPassword) throws Exception
	{
	    if(transactionObject == null)
		{
			SystemUserController.getController().updatePassword(userName, oldPassword, newPassword);
		}
	    else
	    {
			SystemUserController.getController().updatePassword(userName, oldPassword, newPassword, transactionObject);
	    }
	}
	
	public void deleteInfoGluePrincipal(String userName) throws Exception
	{
	    if(transactionObject == null)
		{
			SystemUserController.getController().delete(userName);
		}
	    else
	    {
			SystemUserController.getController().delete(userName, transactionObject);
	    }
	}

	public void createInfoGlueRole(RoleVO roleVO) throws Exception
	{
	    if(transactionObject == null)
		{
			RoleController.getController().create(roleVO);
		}
	    else
	    {
			RoleController.getController().create(roleVO, transactionObject);
	    }
	}

	public void updateInfoGlueRole(RoleVO roleVO, String[] userNames) throws Exception
	{
	    if(transactionObject == null)
		{
			RoleController.getController().update(roleVO, userNames);
		}
	    else
	    {
			RoleController.getController().update(roleVO, userNames, transactionObject);
	    }
	}

	public void deleteInfoGlueRole(String roleName) throws Exception
	{
	    if(transactionObject == null)
		{
			RoleController.getController().delete(roleName);
		}
	    else
	    {
			RoleController.getController().delete(roleName, transactionObject);
	    }
	}

	public void createInfoGlueGroup(GroupVO groupVO) throws Exception
	{
	    if(transactionObject == null)
		{
		    GroupController.getController().create(groupVO);
		}
	    else
	    {
		    GroupController.getController().create(groupVO, transactionObject);
	    }
	}

	public void updateInfoGlueGroup(GroupVO groupVO, String[] userNames) throws Exception
	{
	    if(transactionObject == null)
		{
		    GroupController.getController().update(groupVO, userNames);
		}
	    else
	    {
		    GroupController.getController().update(groupVO, userNames, transactionObject);
	    }
	}

	public void deleteInfoGlueGroup(String groupName) throws Exception
	{
	    if(transactionObject == null)
		{
	        GroupController.getController().delete(groupName);
	    }
	    else
	    {
	        GroupController.getController().delete(groupName, transactionObject);
	    }
	}

	public void addUserToGroup(String groupName, String userName) throws Exception
	{
	    if(transactionObject == null)
		{
		    GroupController.getController().addUser(groupName, userName);
		}
	    else
	    {
		    GroupController.getController().addUser(groupName, userName, transactionObject);
	    }
	}
	
	public void addUserToRole(String roleName, String userName) throws Exception
	{
	    if(transactionObject == null)
		{
		    RoleController.getController().addUser(roleName, userName);
		}
	    else
	    {
	    	RoleController.getController().addUser(roleName, userName, transactionObject);
	    }
	}

	public void removeUserFromGroup(String groupName, String userName) throws Exception
	{
	    if(transactionObject == null)
		{
		    GroupController.getController().removeUser(groupName, userName);
		}
	    else
	    {
		    GroupController.getController().removeUser(groupName, userName, transactionObject);
	    }
	}
	
	public void removeUserFromRole(String roleName, String userName) throws Exception
	{
	    if(transactionObject == null)
		{
		    RoleController.getController().removeUser(roleName, userName);
		}
	    else
	    {
	    	RoleController.getController().removeUser(roleName, userName, transactionObject);
	    }
	}

	/**
	 * This method is used find out if a user exists. Much quicker than getAuthorizedPrincipal 
	 */
	
    public boolean userExists(String userName) throws Exception
    {
    	if(userName == null || userName.equals(""))
	    {
	    	try
	    	{
	    		throw new Exception("userName was null or empty - fix your templates:" + userName);
	    	}
	    	catch(Exception e)
	    	{
	    		logger.warn(e.getMessage(), e);
	    	}

	    	return false;
	    }
	    
		boolean userExists = false;
		
		String administratorUserName = CmsPropertyHandler.getAdministratorUserName();
		String administratorEmail 	 = CmsPropertyHandler.getAdministratorEmail();
		
		final boolean isAdministrator = (userName != null && userName.equalsIgnoreCase(administratorUserName)) ? true : false;
		if(isAdministrator)
		{
			userExists = true;
		}
		else
		{	
			if(transactionObject == null)
			{
				Database db = CastorDatabaseService.getDatabase();
	
				try 
				{
					beginTransaction(db);
					
					userExists = SystemUserController.getController().systemUserExists(userName, db);
					
					commitTransaction(db);
				} 
				catch (Exception e) 
				{
					logger.info("An error occurred trying to get if a systemUser exists for " + userName + ":" + e.getMessage());
					rollbackTransaction(db);
					throw new SystemException(e.getMessage());
				}
			}
			else
			{
		    	userExists = SystemUserController.getController().systemUserExists(userName, transactionObject);
			}
		}
		
		return userExists;
    }

	/**
	 * This method is used find out if a role exists. Much quicker than getRole 
	 */
    public boolean roleExists(String roleName) throws Exception
    {
    	boolean roleExists = false;

		if(transactionObject == null)
		{
			roleExists = RoleController.getController().roleExists(roleName);
		}
		else
		{
			roleExists = RoleController.getController().roleExists(roleName, transactionObject);
		}
		
		return roleExists;
    }
    
	/**
	 * This method is used find out if a group exists. Much quicker than getGroup 
	 */
    public boolean groupExists(String groupName) throws Exception
    {
    	boolean groupExists = false;

		if(transactionObject == null)
		{
			groupExists = GroupController.getController().groupExists(groupName);
		}
		else
		{
			groupExists = GroupController.getController().groupExists(groupName, transactionObject);
		}
		
		return groupExists;
    }

	public Properties getExtraProperties()
	{
		return extraProperties;
	}

	public void setExtraProperties(Properties extraProperties)
	{
		this.extraProperties = extraProperties;
	}
	
    public Object getTransactionObject()
    {
        return this.transactionObject;
    }

    public void setTransactionObject(Object transactionObject)
    {
        this.transactionObject = (Database)transactionObject; 
    }

	public BaseEntityVO getNewVO()
	{
		return null;
	}

}
