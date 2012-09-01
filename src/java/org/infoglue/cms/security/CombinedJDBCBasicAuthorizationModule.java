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
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.entities.management.GroupVO;
import org.infoglue.cms.entities.management.RoleVO;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * @author Mattias Bogeblad
 *
 * This authorization module works firstly against an JDBC source and second against the ordinary infoglue database.
 */

public class CombinedJDBCBasicAuthorizationModule extends BasicAuthorizationModule implements AuthorizationModule, Serializable
{
    private final static Logger logger = Logger.getLogger(CombinedJDBCBasicAuthorizationModule.class.getName());

	protected Properties extraProperties = null;
	private Database transactionObject 	= null;

	private AuthorizationModule authorizationModule = null;

	private AuthorizationModule getMainAuthorizationModule() throws SystemException
	{
		try
    	{
			logger.info("InfoGlueAuthenticationFilter.authorizerClass:" + InfoGlueJDBCAuthorizationModule.class.getName());
			authorizationModule = (AuthorizationModule)Class.forName(InfoGlueJDBCAuthorizationModule.class.getName()).newInstance();
			logger.info("authorizationModule:" + authorizationModule);
			authorizationModule.setExtraProperties(this.extraProperties);
			authorizationModule.setTransactionObject(this.getTransactionObject());
    	}
    	catch(Exception e)
    	{
    		logger.error("There was an error initializing the authorizerClass:" + e.getMessage(), e);
    		throw new SystemException("There was an error initializing the authorizerClass:" + e.getMessage(), e);
    	}
	   
		return authorizationModule;
	}

	private AuthorizationModule getFallbackAuthorizationModule() throws SystemException
	{
		try
    	{
			logger.info("InfoGlueAuthenticationFilter.authorizerClass:" + InfoGlueBasicAuthorizationModule.class.getName());
			authorizationModule = (AuthorizationModule)Class.forName(InfoGlueBasicAuthorizationModule.class.getName()).newInstance();
			logger.info("authorizationModule:" + authorizationModule);
			authorizationModule.setExtraProperties(this.extraProperties);
			authorizationModule.setTransactionObject(this.getTransactionObject());
    	}
    	catch(Exception e)
    	{
    		logger.error("There was an error initializing the authorizerClass:" + e.getMessage(), e);
    		throw new SystemException("There was an error initializing the authorizerClass:" + e.getMessage(), e);
    	}
	   
		return authorizationModule;
	}

	/**
	 * Gets an authorized InfoGluePrincipal. If the user has logged in with the root-account
	 * we immediately return - otherwise we populate it.
	 */
	
	public InfoGluePrincipal getAuthorizedInfoGluePrincipal(String userName) throws Exception
	{
		InfoGluePrincipal infogluePrincipal = null;
		
		try
		{
			infogluePrincipal = getMainAuthorizationModule().getAuthorizedInfoGluePrincipal(userName);
		}
		catch(Exception e)
		{
		}

		if(infogluePrincipal == null)
			infogluePrincipal = getFallbackAuthorizationModule().getAuthorizedInfoGluePrincipal(userName);

		return infogluePrincipal;
	}

	/**
	 * Gets an authorized InfoGlueRole.
	 */

	public InfoGlueRole getAuthorizedInfoGlueRole(String roleName) throws Exception
	{
		InfoGlueRole role = null;
		
		try
		{
			role = getMainAuthorizationModule().getAuthorizedInfoGlueRole(roleName);
		}
		catch(Exception e)
		{
		}

		if(role == null)
			role = getFallbackAuthorizationModule().getAuthorizedInfoGlueRole(roleName);
		
		return role;
	}

	/**
	 * Gets an authorized InfoGlueGroup.
	 */

	public InfoGlueGroup getAuthorizedInfoGlueGroup(String groupName) throws Exception
	{
		InfoGlueGroup group = null;

		try
		{
			group = getMainAuthorizationModule().getAuthorizedInfoGlueGroup(groupName);
		}
		catch(Exception e)
		{
		}

		if(group == null)
			group = getFallbackAuthorizationModule().getAuthorizedInfoGlueGroup(groupName);

		return group;
	}

	
	
	/**
	 * This method gets a list of roles
	 */
	
	public List getRoles() throws Exception
	{
		List roles = new ArrayList();

		try
		{
			roles.addAll(getMainAuthorizationModule().getRoles());
		}		
		catch(Exception e)
		{
		}

		try
		{
			roles.addAll(getFallbackAuthorizationModule().getRoles());
		}		
		catch(Exception e)
		{
		}
		
		return roles;
	}

	/**
	 * This method gets a list of groups
	 */

    public List getGroups() throws Exception
    {
		List groups = new ArrayList();

		try
		{
			groups.addAll(getMainAuthorizationModule().getGroups());
		}		
		catch(Exception e)
		{
		}

		try
		{
			groups.addAll(getFallbackAuthorizationModule().getGroups());
		}		
		catch(Exception e)
		{
		}
		
		return groups;
    }
    
	/**
	 * This method gets a list of users
	 */

	public List getUsers() throws Exception
	{
		List users = new ArrayList();
		
		try
		{
			users.addAll(getMainAuthorizationModule().getUsers());
		}		
		catch(Exception e)
		{
		}

		try
		{
			users.addAll(getFallbackAuthorizationModule().getUsers());
		}		
		catch(Exception e)
		{
		}
		
		return users;
	}
	
	public List getFilteredUsers(Integer offset, Integer limit,	String sortProperty, String direction, String searchString, boolean populateRolesAndGroups) throws Exception 
	{
		List users = new ArrayList();
		
		try
		{
			users.addAll(getMainAuthorizationModule().getFilteredUsers(offset, limit, sortProperty, direction, searchString, populateRolesAndGroups));
		}		
		catch(Exception e)
		{
		}

		try
		{
			users.addAll(getFallbackAuthorizationModule().getFilteredUsers(offset, limit, sortProperty, direction, searchString, populateRolesAndGroups));
		}		
		catch(Exception e)
		{
		}
		
		return users;
	}

	
	public List getUsers(String roleName) throws Exception
	{
		return getRoleUsers(roleName);
	}

    public List getRoleUsers(String roleName) throws Exception
    {
		List users = new ArrayList();
		
		InfoGlueRole role = getAuthorizedInfoGlueRole(roleName);
		
		users.addAll(role.getAutorizationModule().getRoleUsers(roleName));
		
    	return users;
	}

    public List getGroupUsers(String groupName) throws Exception
    {
    	List users = new ArrayList();
		
    	InfoGlueGroup group = getAuthorizedInfoGlueGroup(groupName);

    	users.addAll(group.getAutorizationModule().getGroupUsers(groupName));
    	
    	return users;
    }

    
	public void createInfoGluePrincipal(SystemUserVO systemUserVO) throws Exception
	{		
		getFallbackAuthorizationModule().createInfoGluePrincipal(systemUserVO);
	}

	public void updateInfoGluePrincipal(SystemUserVO systemUserVO, String[] roleNames, String[] groupNames) throws Exception
	{
		InfoGluePrincipal principal = getAuthorizedInfoGluePrincipal(systemUserVO.getUserName());
		
		principal.getAutorizationModule().updateInfoGluePrincipal(systemUserVO, roleNames, groupNames);
	}

	public void updateInfoGluePrincipal(SystemUserVO systemUserVO, String oldPassword, String[] roleNames, String[] groupNames) throws Exception
	{
		InfoGluePrincipal principal = getAuthorizedInfoGluePrincipal(systemUserVO.getUserName());
		
		principal.getAutorizationModule().updateInfoGluePrincipal(systemUserVO, oldPassword, roleNames, groupNames);
	}

	/**
	 * This method is used to send out a newpassword to an existing users.  
	 */

	public void updateInfoGluePrincipalPassword(String userName) throws Exception
	{
		InfoGluePrincipal principal = getAuthorizedInfoGluePrincipal(userName);
		
		principal.getAutorizationModule().updateInfoGluePrincipalPassword(userName);
	}

	/**
	 * This method is used to send out a newpassword to an existing users.  
	 */

	public void updateInfoGlueAnonymousPrincipalPassword() throws Exception
	{
		InfoGluePrincipal principal = getAuthorizedInfoGluePrincipal(CmsPropertyHandler.getAnonymousUser());
		
		principal.getAutorizationModule().updateInfoGlueAnonymousPrincipalPassword();
	}

	/**
	 * This method is used to let a user update his password by giving his/her old one first.  
	 */

	public void updateInfoGluePrincipalPassword(String userName, String oldPassword, String newPassword) throws Exception
	{
		InfoGluePrincipal principal = getAuthorizedInfoGluePrincipal(userName);
		
		principal.getAutorizationModule().updateInfoGluePrincipalPassword(userName, oldPassword, newPassword);
	}
	
	public void deleteInfoGluePrincipal(String userName) throws Exception
	{
		InfoGluePrincipal principal = getAuthorizedInfoGluePrincipal(userName);
		
		principal.getAutorizationModule().deleteInfoGluePrincipal(userName);
	}

	public void createInfoGlueRole(RoleVO roleVO) throws Exception
	{
		getFallbackAuthorizationModule().createInfoGlueRole(roleVO);
	}

	public void deleteInfoGlueRole(String roleName) throws Exception
	{
		InfoGlueRole role = getAuthorizedInfoGlueRole(roleName);

		role.getAutorizationModule().deleteInfoGlueRole(roleName);
	}

	public void updateInfoGlueRole(RoleVO roleVO, String[] userNames) throws Exception
	{
		InfoGlueRole role = getAuthorizedInfoGlueRole(roleVO.getRoleName());
		
		role.getAutorizationModule().updateInfoGlueRole(roleVO, userNames);
	}

	public void createInfoGlueGroup(GroupVO groupVO) throws Exception
	{
		getFallbackAuthorizationModule().createInfoGlueGroup(groupVO);
	}

	public void deleteInfoGlueGroup(String groupName) throws Exception
	{
		InfoGlueGroup group = getAuthorizedInfoGlueGroup(groupName);

		group.getAutorizationModule().deleteInfoGlueGroup(groupName);
	}

	public void updateInfoGlueGroup(GroupVO groupVO, String[] userNames) throws Exception
	{
		InfoGlueGroup group = getAuthorizedInfoGlueGroup(groupVO.getGroupName());
		
		group.getAutorizationModule().updateInfoGlueGroup(groupVO, userNames);
	}
	
	public void addUserToGroup(String groupName, String userName) throws Exception
	{
		InfoGlueGroup group = getAuthorizedInfoGlueGroup(groupName);

		group.getAutorizationModule().addUserToGroup(groupName, userName);
	}
	
	public void addUserToRole(String roleName, String userName) throws Exception
	{
		InfoGlueRole role = getAuthorizedInfoGlueRole(roleName);
		
		role.getAutorizationModule().addUserToRole(roleName, userName);
	}

	/**
	 * This method is used to remove user from a role.  
	 */
    public void removeUserFromRole(String roleName, String userName) throws Exception
    {
		InfoGlueRole role = getAuthorizedInfoGlueRole(roleName);

		role.getAutorizationModule().removeUserFromRole(roleName, userName);
    }

	/**
	 * This method is used to remove user from a group.  
	 */
    public void removeUserFromGroup(String groupName, String userName) throws Exception
    {
		InfoGlueGroup group = getAuthorizedInfoGlueGroup(groupName);

		group.getAutorizationModule().removeUserFromGroup(groupName, userName);    	
    }

    
	/**
	 * This method is used find out if a user exists. Much quicker than getAuthorizedPrincipal 
	 */
	
    public boolean userExists(String userName) throws Exception
    {
    	return (getAuthorizedInfoGluePrincipal(userName) == null ? false : true);
    }

	/**
	 * This method is used find out if a role exists. Much quicker than getRole 
	 */
    public boolean roleExists(String roleName) throws Exception
    {
    	return (getAuthorizedInfoGlueRole(roleName) == null ? false : true);
    }
    
	/**
	 * This method is used find out if a group exists. Much quicker than getGroup 
	 */
    public boolean groupExists(String groupName) throws Exception
    {
    	return (getAuthorizedInfoGlueGroup(groupName) == null ? false : true);
    }

	public boolean getSupportUpdate()
	{
		return true;
	}

	public boolean getSupportDelete()
	{
		return true;
	}

	public boolean getSupportCreate()
	{
		return true;
	}

	public List getFilteredUsers(String firstName, String lastName, String userName, String email, String[] roleIds) throws Exception
	{
		return null;
	}

	public Properties getExtraProperties()
	{
		return this.extraProperties;
	}

	public void setExtraProperties(Properties properties)
	{
		this.extraProperties = properties;
	}

    public void setTransactionObject(Object transactionObject)
    {
    	this.transactionObject = (Database)transactionObject;
    }

    public Object getTransactionObject()
    {
        return this.transactionObject;
    }

}
