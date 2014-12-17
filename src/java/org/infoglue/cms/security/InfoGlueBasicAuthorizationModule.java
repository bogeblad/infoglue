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
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
	private static final long serialVersionUID = 1L;

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
							InfoGlueRole infoGlueRole = new InfoGlueRole(roleVO.getRoleName(), roleVO.getRoleName(), roleVO.getDescription(), roleVO.getSource(), roleVO.getIsActive(), roleVO.getModifiedDateTime(), this);
							roles.add(infoGlueRole);
						}
								
				    	Collection<GroupVO> groupVOList = GroupController.getController().getGroupVOList(userName, db);
				    	if(logger.isInfoEnabled())
				    		t.printElapsedTime("groupVOList took:");

						Iterator<GroupVO> groupVOListIterator = groupVOList.iterator();
						while(groupVOListIterator.hasNext())
						{
							GroupVO groupVO = groupVOListIterator.next();
							if(logger.isInfoEnabled())
								logger.info("Adding group:" + groupVO.getGroupName());
							InfoGlueGroup infoGlueGroup = new InfoGlueGroup(groupVO.getGroupName(), groupVO.getGroupName(), groupVO.getDescription(), groupVO.getSource(), groupVO.getGroupType(), groupVO.getIsActive(), groupVO.getModifiedDateTime(), this);
							groups.add(infoGlueGroup);
						}
							
						infogluePrincipal = new InfoGluePrincipal(userName, userName, systemUser.getFirstName(), systemUser.getLastName(), systemUser.getEmail(), systemUser.getSource(), systemUser.getIsActive(), systemUser.getModifiedDateTime(), roles, groups, new HashMap(), isAdministrator, this);
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
						InfoGlueRole infoGlueRole = new InfoGlueRole(roleVO.getRoleName(), roleVO.getRoleName(), roleVO.getDescription(), roleVO.getSource(), roleVO.getIsActive(), roleVO.getModifiedDateTime(), this);
						roles.add(infoGlueRole);
					}
					
			    	Collection<GroupVO> groupVOList = GroupController.getController().getGroupVOList(userName, transactionObject);
			    	if(logger.isInfoEnabled())
			    		t.printElapsedTime("groupVOList took:");

					Iterator<GroupVO> groupVOListIterator = groupVOList.iterator();
					while(groupVOListIterator.hasNext())
					{
						GroupVO groupVO = (GroupVO)groupVOListIterator.next();
						if(logger.isInfoEnabled())
							logger.info("Adding group:" + groupVO.getGroupName());
						InfoGlueGroup infoGlueGroup = new InfoGlueGroup(groupVO.getGroupName(), groupVO.getGroupName(), groupVO.getDescription(), groupVO.getSource(), groupVO.getGroupType(), groupVO.getIsActive(), groupVO.getModifiedDateTime(), this);
						groups.add(infoGlueGroup);
					}
					
					infogluePrincipal = new InfoGluePrincipal(userName, userName, systemUser.getFirstName(), systemUser.getLastName(), systemUser.getEmail(), systemUser.getSource(), systemUser.getIsActive(), systemUser.getModifiedDateTime(), roles, groups, new HashMap(), isAdministrator, this);
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

		infoglueRole = new InfoGlueRole(roleVO.getRoleName(), roleVO.getRoleName(), roleVO.getDescription(), roleVO.getSource(), roleVO.getIsActive(), roleVO.getModifiedDateTime(), this);		
		
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
		    groupVO = GroupController.getController().getGroupVOWithId(groupName, transactionObject);
		}

	    infoglueGroup = new InfoGlueGroup(groupVO.getGroupName(), groupVO.getGroupName(), groupVO.getDescription(), groupVO.getSource(), groupVO.getGroupType(), groupVO.getIsActive(), groupVO.getModifiedDateTime(), this);
				
		return infoglueGroup;
	}


	/**
	 * This method gets a list of roles
	 */
	
	public List<InfoGlueRole> getRoles() throws Exception
	{
		List<InfoGlueRole> roles = new ArrayList<InfoGlueRole>();
		
		if(transactionObject == null)
		{
			List roleVOList = RoleController.getController().getRoleVOList();
			Iterator roleVOListIterator = roleVOList.iterator();
			while(roleVOListIterator.hasNext())
			{
				RoleVO roleVO = (RoleVO)roleVOListIterator.next();
				InfoGlueRole infoGlueRole = new InfoGlueRole(roleVO.getRoleName(), roleVO.getRoleName(), roleVO.getDescription(), roleVO.getSource(), roleVO.getIsActive(), roleVO.getModifiedDateTime(), this);
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
				InfoGlueRole infoGlueRole = new InfoGlueRole(roleVO.getRoleName(), roleVO.getRoleName(), roleVO.getDescription(), roleVO.getSource(), roleVO.getIsActive(), roleVO.getModifiedDateTime(), this);
				roles.add(infoGlueRole);
			}
		}
		
		return roles;
	}


	/**
	 * This method gets a list of roles
	 */
	
    public List<InfoGlueGroup> getGroups() throws Exception
    {
        List<InfoGlueGroup> groups = new ArrayList<InfoGlueGroup>();
		
		if(transactionObject == null)
		{
			List groupVOList = GroupController.getController().getGroupVOList();
			Iterator groupVOListIterator = groupVOList.iterator();
			while(groupVOListIterator.hasNext())
			{
			    GroupVO groupVO = (GroupVO)groupVOListIterator.next();
				InfoGlueGroup infoGlueGroup = new InfoGlueGroup(groupVO.getGroupName(), groupVO.getGroupName(), groupVO.getDescription(), groupVO.getSource(), groupVO.getGroupType(), groupVO.getIsActive(), groupVO.getModifiedDateTime(), this);
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
				InfoGlueGroup infoGlueGroup = new InfoGlueGroup(groupVO.getGroupName(), groupVO.getGroupName(), groupVO.getDescription(), groupVO.getSource(), groupVO.getGroupType(), groupVO.getIsActive(), groupVO.getModifiedDateTime(), this);
				groups.add(infoGlueGroup);
			}		    
		}
		
		return groups;
    }

    
	/**
	 * This method gets a list of users
	 */
	
	public List<InfoGluePrincipal> getUsers() throws Exception
	{
		List<InfoGluePrincipal> users = new ArrayList<InfoGluePrincipal>();
		
		if(transactionObject == null)
		{
			Database db = CastorDatabaseService.getDatabase();

			try 
			{
				beginTransaction(db);
				
				List<RoleVO> roleVOList = RoleController.getController().getRoleVOList(db);
				Map<String,RoleVO> rolesMap = new HashMap<String,RoleVO>();
				for(RoleVO role : roleVOList)
				{
					rolesMap.put(role.getRoleName(), role);
				}

				List<GroupVO> groupVOList = GroupController.getController().getGroupVOList(db);
				Map<String,GroupVO> groupsMap = new HashMap<String,GroupVO>();
				for(GroupVO group : groupVOList)
				{
					groupsMap.put(group.getGroupName().toLowerCase(), group);
				}
				
				Map<String,List<String>> systemRoleMapping = RoleController.getController().getSystemUserRoleMapping(db);
				Map<String,List<String>> systemGroupMapping = GroupController.getController().getSystemUserGroupMappingLowerCase(db);
				
				
				List<SystemUserVO> systemUserVOList = SystemUserController.getController().getSystemUserVOList(db);
				Iterator systemUserVOListIterator = systemUserVOList.iterator();
				while(systemUserVOListIterator.hasNext())
				{
					SystemUserVO systemUserVO = (SystemUserVO)systemUserVOListIterator.next();
					
					List<InfoGlueRole> roles = new ArrayList<InfoGlueRole>();
					List<String> roleNames = systemRoleMapping.get(systemUserVO.getUserName());
					if(roleNames != null)
					{
						for(String roleName : roleNames)
						{
							if(rolesMap.get(roleName) != null)
							{
								InfoGlueRole infoGlueRole = new InfoGlueRole(rolesMap.get(roleName).getRoleName(), rolesMap.get(roleName).getRoleName(), rolesMap.get(roleName).getDescription(), rolesMap.get(roleName).getSource(), rolesMap.get(roleName).getIsActive(), rolesMap.get(roleName).getModifiedDateTime(), this);
								roles.add(infoGlueRole);
							}
						}
					}
					
					List<InfoGlueGroup> groups = new ArrayList<InfoGlueGroup>();
					List<String> groupNames = systemGroupMapping.get(systemUserVO.getUserName().toLowerCase());
					if(groupNames != null)
					{
						for(String groupName : groupNames)
						{
							if(groupsMap.get(groupName.toLowerCase()) != null)
							{
								InfoGlueGroup infoGlueGroup = new InfoGlueGroup(groupsMap.get(groupName.toLowerCase()).getGroupName(), groupsMap.get(groupName.toLowerCase()).getGroupName(), groupsMap.get(groupName.toLowerCase()).getDescription(), groupsMap.get(groupName.toLowerCase()).getSource(), groupsMap.get(groupName.toLowerCase()).getGroupType(), groupsMap.get(groupName.toLowerCase()).getIsActive(), groupsMap.get(groupName.toLowerCase()).getModifiedDateTime(), this);
								groups.add(infoGlueGroup);
							}
						}
					}
					
					InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUserVO.getUserName(), systemUserVO.getFirstName(), systemUserVO.getLastName(), systemUserVO.getEmail(), roles, groups, false, this);
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
			List<RoleVO> roleVOList = RoleController.getController().getRoleVOList(transactionObject);
			Map<String,RoleVO> rolesMap = new HashMap<String,RoleVO>();
			for(RoleVO role : roleVOList)
			{
				rolesMap.put(role.getRoleName(), role);
			}

			List<GroupVO> groupVOList = GroupController.getController().getGroupVOList(transactionObject);
			Map<String,GroupVO> groupsMap = new HashMap<String,GroupVO>();
			for(GroupVO group : groupVOList)
			{
				groupsMap.put(group.getGroupName().toLowerCase(), group);
			}
			
			Map<String,List<String>> systemRoleMapping = RoleController.getController().getSystemUserRoleMapping(transactionObject);
			Map<String,List<String>> systemGroupMapping = GroupController.getController().getSystemUserGroupMappingLowerCase(transactionObject);
			
			List<SystemUserVO> systemUserVOList = SystemUserController.getController().getSystemUserVOList(transactionObject);
			
			Iterator systemUserVOListIterator = systemUserVOList.iterator();
			while(systemUserVOListIterator.hasNext())
			{
				SystemUserVO systemUser = (SystemUserVO)systemUserVOListIterator.next();
				
				List<InfoGlueRole> roles = new ArrayList<InfoGlueRole>();
				List<String> roleNames = systemRoleMapping.get(systemUser.getUserName());
				if(roleNames != null)
				{
					for(String roleName : roleNames)
					{
						if(rolesMap.get(roleName) != null)
						{
							InfoGlueRole infoGlueRole = new InfoGlueRole(rolesMap.get(roleName).getRoleName(), rolesMap.get(roleName).getRoleName(), rolesMap.get(roleName).getDescription(), rolesMap.get(roleName).getSource(), rolesMap.get(roleName).getIsActive(), rolesMap.get(roleName).getModifiedDateTime(), this);
							roles.add(infoGlueRole);
						}
					}
				}

				List<InfoGlueGroup> groups = new ArrayList<InfoGlueGroup>();
				List<String> groupNames = systemGroupMapping.get(systemUser.getUserName().toLowerCase());
				if(groupNames != null)
				{
					for(String groupName : groupNames)
					{
						if(groupsMap.get(groupName.toLowerCase()) != null)
						{
							InfoGlueGroup infoGlueGroup = new InfoGlueGroup(groupsMap.get(groupName.toLowerCase()).getGroupName(), groupsMap.get(groupName.toLowerCase()).getGroupName(), groupsMap.get(groupName.toLowerCase()).getDescription(), groupsMap.get(groupName.toLowerCase()).getSource(), groupsMap.get(groupName.toLowerCase()).getGroupType(), groupsMap.get(groupName.toLowerCase()).getIsActive(), groupsMap.get(groupName.toLowerCase()).getModifiedDateTime(), this);
							groups.add(infoGlueGroup);
						}
					}
				}
				
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUser.getUserName(), systemUser.getFirstName(), systemUser.getLastName(), systemUser.getEmail(), roles, groups, false, this);
				users.add(infoGluePrincipal);
			}
			logger.info("Returning:" + users.size());
		}
		
		return users;
	}
	

	/**
	 * This method is used to fetch all users filtered on a text or not.  
	 * If the index is not created or older than set interval the index is created.
	 */

	public List<InfoGluePrincipal> getFilteredUsers(String searchString) throws Exception
	{
		return getFilteredUsers(null, null, null, null, searchString, true);
	}

	/**
	 * This method is used to fetch all or a subset of sorted users either filtered on a text or not.  
	 * If the index is not created or older than set interval the index is created.
	 */

	public List<InfoGluePrincipal> getFilteredUsers(Integer offset, Integer limit, String sortProperty, String direction, String searchString, boolean populateRolesAndGroups) throws Exception
	{
		List<InfoGluePrincipal> users = new ArrayList<InfoGluePrincipal>();
		if(transactionObject == null)
		{
			List systemUserVOList = SystemUserController.getController().getFilteredSystemUserVOList(offset, limit, sortProperty, direction, searchString);
			Iterator systemUserVOListIterator = systemUserVOList.iterator();
			while(systemUserVOListIterator.hasNext())
			{
				SystemUserVO systemUserVO = (SystemUserVO)systemUserVOListIterator.next();
				
				List<InfoGlueRole> roles = new ArrayList<InfoGlueRole>();
				List<InfoGlueGroup> groups = new ArrayList<InfoGlueGroup>();

				if(populateRolesAndGroups)
				{
					List<RoleVO> roleList = RoleController.getController().getRoleVOList(systemUserVO.getUserName());
					for(RoleVO role : roleList)
					{
						InfoGlueRole infoGlueRole = new InfoGlueRole(role.getRoleName(), role.getRoleName(), role.getDescription(), role.getSource(), role.getIsActive(), role.getModifiedDateTime(), this);
						roles.add(infoGlueRole);
					}
					
					List<GroupVO> groupList = GroupController.getController().getGroupVOList(systemUserVO.getUserName());
					for(GroupVO group : groupList)
					{
						InfoGlueGroup infoGlueGroup = new InfoGlueGroup(group.getGroupName(), group.getGroupName(), group.getDescription(), group.getSource(), group.getGroupType(), group.getIsActive(), group.getModifiedDateTime(), this);
						groups.add(infoGlueGroup);
					}
				}
				
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUserVO.getUserName(), systemUserVO.getUserName(), systemUserVO.getFirstName(), systemUserVO.getLastName(), systemUserVO.getEmail(), systemUserVO.getSource(), systemUserVO.getIsActive(), systemUserVO.getModifiedDateTime(), roles, groups, new HashMap(), false, this);
				users.add(infoGluePrincipal);
			}
		}
		else
		{
			List systemUserList = SystemUserController.getController().getFilteredSystemUserList(offset, limit, sortProperty, direction, searchString, transactionObject);
			Iterator systemUserListIterator = systemUserList.iterator();
			while(systemUserListIterator.hasNext())
			{
				SystemUser systemUser = (SystemUser)systemUserListIterator.next();
				
				List<InfoGlueRole> roles = new ArrayList<InfoGlueRole>();
				List<InfoGlueGroup> groups = new ArrayList<InfoGlueGroup>();

				if(populateRolesAndGroups)
				{
					List<RoleVO> roleList = RoleController.getController().getRoleVOList(systemUser.getUserName());
					for(RoleVO role : roleList)
					{
						InfoGlueRole infoGlueRole = new InfoGlueRole(role.getRoleName(), role.getRoleName(), role.getDescription(), role.getSource(), role.getIsActive(), role.getModifiedDateTime(), this);
						roles.add(infoGlueRole);
					}
					
					List<GroupVO> groupList = GroupController.getController().getGroupVOList(systemUser.getUserName(), transactionObject);
					for(GroupVO group : groupList)
					{
						InfoGlueGroup infoGlueGroup = new InfoGlueGroup(group.getGroupName(), group.getGroupName(), group.getDescription(), group.getSource(), group.getGroupType(), group.getIsActive(), group.getModifiedDateTime(), this);
						groups.add(infoGlueGroup);
					}
				}
				
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUser.getUserName(), systemUser.getUserName(), systemUser.getFirstName(), systemUser.getLastName(), systemUser.getEmail(), systemUser.getSource(), systemUser.getIsActive(), systemUser.getModifiedDateTime(), roles, groups, new HashMap(), false, this);
				users.add(infoGluePrincipal);
			}
		}
		
		return users;
	}
	
	/**
	 * This method gets a list of users part of a given role
	 */

	public List<InfoGluePrincipal> getUsers(String roleName) throws Exception
	{
		return getRoleUsers(roleName, null, null, null, null, null);
	}

	/**
	 * This method gets a list of users part of a given role
	 */

	public List<InfoGluePrincipal> getRoleUsers(String roleName) throws Exception
	{
		return getRoleUsers(roleName, null, null, null, null, null);
	}

	/**
	 * A method returning the all/subset of sorted users part of stated role and optionally contains searched text
	 */

    public List<InfoGluePrincipal> getRoleUsers(String roleName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception
    {
        logger.info("roleName:" + roleName);
		List<InfoGluePrincipal> users = new ArrayList<InfoGluePrincipal>();
		
		if(transactionObject == null)
		{
			List systemUserVOList = RoleController.getController().getRoleSystemUserVOList(offset, limit, sortProperty, direction, searchString, roleName);
			Iterator systemUserVOListIterator = systemUserVOList.iterator();
			while(systemUserVOListIterator.hasNext())
			{
				SystemUserVO systemUserVO = (SystemUserVO)systemUserVOListIterator.next();
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUserVO.getUserName(), systemUserVO.getUserName(), systemUserVO.getFirstName(), systemUserVO.getLastName(), systemUserVO.getEmail(), systemUserVO.getSource(), systemUserVO.getIsActive(), systemUserVO.getModifiedDateTime(), new ArrayList(), new ArrayList(), new HashMap(), false, this);
				users.add(infoGluePrincipal);
			}
		}
		else
		{
			List systemUserVOList = RoleController.getController().getRoleSystemUserVOList(offset, limit, sortProperty, direction, searchString, roleName, transactionObject);
			Iterator systemUserVOListIterator = systemUserVOList.iterator();
			while(systemUserVOListIterator.hasNext())
			{
				SystemUserVO systemUserVO = (SystemUserVO)systemUserVOListIterator.next();
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUserVO.getUserName(), systemUserVO.getUserName(), systemUserVO.getFirstName(), systemUserVO.getLastName(), systemUserVO.getEmail(), systemUserVO.getSource(), systemUserVO.getIsActive(), systemUserVO.getModifiedDateTime(), new ArrayList(), new ArrayList(), new HashMap(), false, this);
				users.add(infoGluePrincipal);
			}
		}
		
		return users;
	}
    
	/**
	 * A method returning the number of users matching a roleName and also contains searched text
	 */

    public Integer getRoleUserCount(String roleName, String searchString) throws Exception
    {
        logger.info("roleName:" + roleName);
        Integer count = 0;
        
		if(transactionObject == null)
		{
			count = RoleController.getController().getRoleSystemUserCount(roleName, searchString);
		}
		else
		{
			count = RoleController.getController().getRoleSystemUserCount(roleName, searchString, transactionObject);
		}
		
		return count;
	}

	/**
	 * A method returning the all/subset of sorted users not part of stated role and optionally contains searched text
	 */

    public List<InfoGluePrincipal> getRoleUsersInverted(String roleName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception
    {
        logger.info("roleName:" + roleName);
		List users = new ArrayList();
		
		if(transactionObject == null)
		{
			List systemUserVOList = RoleController.getController().getRoleSystemUserVOListInverted(offset, limit, sortProperty, direction, searchString, roleName);
			Iterator systemUserVOListIterator = systemUserVOList.iterator();
			while(systemUserVOListIterator.hasNext())
			{
				SystemUserVO systemUserVO = (SystemUserVO)systemUserVOListIterator.next();
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUserVO.getUserName(), systemUserVO.getUserName(), systemUserVO.getFirstName(), systemUserVO.getLastName(), systemUserVO.getEmail(), systemUserVO.getSource(), systemUserVO.getIsActive(), systemUserVO.getModifiedDateTime(), new ArrayList(), new ArrayList(), new HashMap(), false, this);
				users.add(infoGluePrincipal);
			}
		}
		else
		{
			List systemUserVOList = RoleController.getController().getRoleSystemUserVOListInverted(offset, limit, sortProperty, direction, searchString, roleName, transactionObject);
			Iterator systemUserVOListIterator = systemUserVOList.iterator();
			while(systemUserVOListIterator.hasNext())
			{
				SystemUserVO systemUserVO = (SystemUserVO)systemUserVOListIterator.next();
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUserVO.getUserName(), systemUserVO.getUserName(), systemUserVO.getFirstName(), systemUserVO.getLastName(), systemUserVO.getEmail(), systemUserVO.getSource(), systemUserVO.getIsActive(), systemUserVO.getModifiedDateTime(), new ArrayList(), new ArrayList(), new HashMap(), false, this);
				users.add(infoGluePrincipal);
			}
		}

		return users;
    }

	/**
	 * A method returning the number of users not matching a roleName and also contains searched text
	 */

    public Integer getRoleUserInvertedCount(String roleName, String searchString) throws Exception
    {
        logger.info("roleName:" + roleName);
        Integer count = 0;
        
		if(transactionObject == null)
		{
			count = RoleController.getController().getRoleSystemUserCountInverted(roleName, searchString);
		}
		else
		{
			count = RoleController.getController().getRoleSystemUserCountInverted(roleName, searchString, transactionObject);
		}
		
		return count;
	}

	/**
	 * A method returning the all/subset of sorted users part of stated group and optionally contains searched text
	 */

    public List<InfoGluePrincipal> getGroupUsers(String groupName) throws Exception
    {
    	return getGroupUsers(groupName, null, null, null, null, null);
    }

	/**
	 * A method returning the all/subset of sorted users part of stated group and optionally contains searched text
	 */

    public List<InfoGluePrincipal> getGroupUsers(String groupName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception
    {
        logger.info("groupName:" + groupName);
		List<InfoGluePrincipal> users = new ArrayList<InfoGluePrincipal>();
		
		if(transactionObject == null)
		{
			List systemUserVOList = GroupController.getController().getGroupSystemUserVOList(offset, limit, sortProperty, direction, searchString, groupName);
			Iterator systemUserVOListIterator = systemUserVOList.iterator();
			while(systemUserVOListIterator.hasNext())
			{
				SystemUserVO systemUserVO = (SystemUserVO)systemUserVOListIterator.next();
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUserVO.getUserName(), systemUserVO.getUserName(), systemUserVO.getFirstName(), systemUserVO.getLastName(), systemUserVO.getEmail(), systemUserVO.getSource(), systemUserVO.getIsActive(), systemUserVO.getModifiedDateTime(), new ArrayList(), new ArrayList(), new HashMap(), false, this);
				users.add(infoGluePrincipal);
			}
		}
		else
		{
			List systemUserVOList = GroupController.getController().getGroupSystemUserVOList(offset, limit, sortProperty, direction, searchString, groupName, transactionObject);
			Iterator systemUserVOListIterator = systemUserVOList.iterator();
			while(systemUserVOListIterator.hasNext())
			{
				SystemUserVO systemUserVO = (SystemUserVO)systemUserVOListIterator.next();
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUserVO.getUserName(), systemUserVO.getUserName(), systemUserVO.getFirstName(), systemUserVO.getLastName(), systemUserVO.getEmail(), systemUserVO.getSource(), systemUserVO.getIsActive(), systemUserVO.getModifiedDateTime(), new ArrayList(), new ArrayList(), new HashMap(), false, this);
				users.add(infoGluePrincipal);
			}
		}

		return users;
    }

	/**
	 * A method returning the number of users matching a groupName and also contains searched text
	 */

    public Integer getGroupUserCount(String groupName, String searchString) throws Exception
    {
        logger.info("groupName:" + groupName);
        Integer count = 0;
        
		if(transactionObject == null)
		{
			count = GroupController.getController().getGroupSystemUserCount(groupName, searchString);
		}
		else
		{
			count = GroupController.getController().getGroupSystemUserCount(groupName, searchString, transactionObject);
		}
		
		return count;
	}

	/**
	 * A method returning the all/subset of sorted users not part of stated group and optionally contains searched text
	 */

    public List<InfoGluePrincipal> getGroupUsersInverted(String groupName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception
    {
        logger.info("groupName:" + groupName);
		List<InfoGluePrincipal> users = new ArrayList<InfoGluePrincipal>();
		
		if(transactionObject == null)
		{
			List systemUserVOList = GroupController.getController().getGroupSystemUserVOListInverted(offset, limit, sortProperty, direction, searchString, groupName);
			Iterator systemUserVOListIterator = systemUserVOList.iterator();
			while(systemUserVOListIterator.hasNext())
			{
				SystemUserVO systemUserVO = (SystemUserVO)systemUserVOListIterator.next();
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUserVO.getUserName(), systemUserVO.getUserName(), systemUserVO.getFirstName(), systemUserVO.getLastName(), systemUserVO.getEmail(), systemUserVO.getSource(), systemUserVO.getIsActive(), systemUserVO.getModifiedDateTime(), new ArrayList(), new ArrayList(), new HashMap(), false, this);
				users.add(infoGluePrincipal);
			}
		}
		else
		{
			List systemUserVOList = GroupController.getController().getGroupSystemUserVOListInverted(offset, limit, sortProperty, direction, searchString, groupName, transactionObject);
			Iterator systemUserVOListIterator = systemUserVOList.iterator();
			while(systemUserVOListIterator.hasNext())
			{
				SystemUserVO systemUserVO = (SystemUserVO)systemUserVOListIterator.next();
				InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(systemUserVO.getUserName(), systemUserVO.getUserName(), systemUserVO.getFirstName(), systemUserVO.getLastName(), systemUserVO.getEmail(), systemUserVO.getSource(), systemUserVO.getIsActive(), systemUserVO.getModifiedDateTime(), new ArrayList(), new ArrayList(), new HashMap(), false, this);
				users.add(infoGluePrincipal);
			}
		}

		return users;
    }

	/**
	 * A method returning the number of users not matching a groupName and also contains searched text
	 */

    public Integer getGroupUserInvertedCount(String groupName, String searchString) throws Exception
    {
        logger.info("groupName:" + groupName);
        Integer count = 0;
        
		if(transactionObject == null)
		{
			count = GroupController.getController().getGroupSystemUserCountInverted(groupName, searchString);
		}
		else
		{
			count = GroupController.getController().getGroupSystemUserCountInverted(groupName, searchString, transactionObject);
		}
		
		return count;
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

	/**
	 * This method is used to change the user name of a user. All references to the old <em>userName</em> are changed
	 * to the <em>newUserName</em>.
	 */
	
	public void changeInfoGluePrincipalUserName(String userName, String newUserName) throws Exception
	{
		if(transactionObject == null)
		{
			SystemUserController.getController().changeUserName(userName, newUserName);
		}
		else
		{
			SystemUserController.getController().changeUserName(userName, newUserName, transactionObject);
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
		Set<String> userNamesSet = null;
		if(userNames != null)
		{
			List<String> userNamesList = Arrays.asList(userNames);
			userNamesSet = new HashSet<String>(userNamesList);
		}

	    if(transactionObject == null)
		{
			RoleController.getController().update(roleVO, userNamesSet);
		}
	    else
	    {
			RoleController.getController().update(roleVO, userNamesSet, transactionObject);
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
		Set<String> userNamesSet = null;
		if(userNames != null)
		{
			List<String> userNamesList = Arrays.asList(userNames);
			userNamesSet = new HashSet<String>(userNamesList);
		}
		
	    if(transactionObject == null)
		{
		    GroupController.getController().update(groupVO, userNamesSet);
		}
	    else
	    {
		    GroupController.getController().update(groupVO, userNamesSet, transactionObject);
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

	public Integer getRoleCount(String searchString) throws Exception 
	{
		return RoleController.getController().getTableCount("cmRole").getCount();
	}

	public Integer getGroupCount(String searchString) throws Exception 
	{
		return GroupController.getController().getTableCount("cmGroup").getCount();
	}

	public Integer getUserCount(String searchString) throws Exception 
	{
		if(searchString == null || searchString.equals(""))
			return SystemUserController.getController().getTableCount("cmSystemUser").getCount();
		else
			return SystemUserController.getController().getFilteredSystemUserVOList(0, 1000000, null, null, searchString).size();
	}

}
