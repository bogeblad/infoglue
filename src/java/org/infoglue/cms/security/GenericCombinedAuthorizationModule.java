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
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
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
import org.infoglue.cms.util.sorters.ReflectionComparator;

/**
 * @author Mattias Bogeblad
 *
 * This authorization module is a generic multi-source authorization module. With this you can
 * define any number of underlying authorization modules which is combined by this module. The order of lookup
 * is the same as the order the underlying modules are defined in.
 * 
 * To use this you state org.infoglue.cms.security.GenericCombinedAuthorizationModule as AuthorizationModule
 * and then you add the underlying modules in Extra security parameters as index based properties like this:
 * 
 * 	0.authorizerClassName=org.infoglue.cms.security.JNDIBasicAuthorizationModule
 *	1.authorizerClassName=org.infoglue.cms.security.InfoGlueBasicAuthorizationModule
 *  2.authorizerClassName=com.mycompany.cms.security.MyCustomAuthorizationModule
 *
 *	Then all modules in turn are asked the queries and if not found the next module is asked.
 *  If you for example want to have several JNDI-sources you are free to define the same module several times and
 *  you can differentiate what properties it should use by setting the index in front of the properties.
 *  For example if you have 2 JNDI sources and they differ in among other things 
 *  roleBase=cn=groups,dc=infoglue,dc=org you add the index in front of two lines of this:
 *  
 *  0.roleBase=cn=groups,dc=infoglue,dc=org
 *  1.roleBase=cn=internal,cn=groups,dc=companyx,dc=com
 *  
 *  That way the module with index 0 will get all properties without an index and all properties with index 0 will 
 *  override the properties without index for just that module. That way you can also have some parameters in common
 *  between two of the same modules.
 */

public class GenericCombinedAuthorizationModule implements AuthorizationModule, Serializable
{
    private final static Logger logger = Logger.getLogger(GenericCombinedAuthorizationModule.class.getName());

	protected Properties extraProperties = null;
	private Database transactionObject 	= null;

	private List authorizationModules = new ArrayList();
	//private AuthorizationModule mainAuthorizationModule = null;
	//private AuthorizationModule authorizationModule = null;

	private AuthorizationModule getAuthorizationModule(String authorizationModuleClassName, int index) throws SystemException
	{
		AuthorizationModule authorizationModule = null;
		
		try
    	{
			Properties localProperties = new Properties();
			Iterator propertiesIterator = this.extraProperties.keySet().iterator();
			while(propertiesIterator.hasNext())
			{
				String property = (String)propertiesIterator.next();
				String value = this.extraProperties.getProperty(property);
				if(property.startsWith("" + index + "."))
					property = property.substring(2);
				
				localProperties.setProperty(property, value);
			}
			localProperties.setProperty("authorizerIndex", "" + index);
				
			if(logger.isInfoEnabled())
				logger.info("InfoGlueAuthenticationFilter.authorizerClass:" + authorizationModuleClassName);
			
			authorizationModule = (AuthorizationModule)Class.forName(authorizationModuleClassName).newInstance();
			
			if(logger.isInfoEnabled())
				logger.info("authorizationModule:" + authorizationModule);
			
			//localProperties.list(System.out);
			
			authorizationModule.setExtraProperties(localProperties);
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
		
		int i=0;
		String authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		while(authorizerClassName != null && !authorizerClassName.equals("") && infogluePrincipal == null)
		{
			if(logger.isInfoEnabled())
				logger.info("getAuthorizedInfoGluePrincipal in " + authorizerClassName);
			
			try
			{
				infogluePrincipal = getAuthorizationModule(authorizerClassName, i).getAuthorizedInfoGluePrincipal(userName);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			i++;
			authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		}

		return infogluePrincipal;
	}

	/**
	 * Gets an authorized InfoGlueRole.
	 */

	public InfoGlueRole getAuthorizedInfoGlueRole(String roleName) throws Exception
	{
		InfoGlueRole role = null;

		int i=0;
		String authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		while(authorizerClassName != null && !authorizerClassName.equals("") && role == null)
		{
			try
			{
				role = getAuthorizationModule(authorizerClassName, i).getAuthorizedInfoGlueRole(roleName);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			i++;
			authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		}
		
		return role;
	}

	/**
	 * Gets an authorized InfoGlueGroup.
	 */

	public InfoGlueGroup getAuthorizedInfoGlueGroup(String groupName) throws Exception
	{
		InfoGlueGroup group = null;

		int i=0;
		String authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		while(authorizerClassName != null && !authorizerClassName.equals("") && group == null)
		{
			try
			{
				group = getAuthorizationModule(authorizerClassName, i).getAuthorizedInfoGlueGroup(groupName);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			i++;
			authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		}

		return group;
	}

	
	/**
	 * This method gets a users roles
	 */

	public List authorizeUser(String userName) throws Exception
	{
		List roles = new ArrayList();
		
		int i=0;
		String authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		while(authorizerClassName != null && !authorizerClassName.equals(""))
		{
			try
			{
				roles.addAll(getAuthorizationModule(authorizerClassName, i).authorizeUser(userName));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			i++;
			authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		}

		return roles;
	}

	
	/**
	 * This method gets a list of roles
	 */
	
	public List getRoles() throws Exception
	{
		List roles = new ArrayList();

		int i=0;
		String authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		while(authorizerClassName != null && !authorizerClassName.equals(""))
		{
			try
			{
				List systemRoles = getAuthorizationModule(authorizerClassName, i).getRoles();
				if(logger.isInfoEnabled())
					logger.info("\nFound:" + systemRoles.size() + " roles in " + i);
				roles.addAll(systemRoles);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			i++;
			authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		}

		Collections.sort(roles, new ReflectionComparator("displayName"));

		return roles;
	}

	/**
	 * This method gets a list of groups
	 */

    public List getGroups() throws Exception
    {
		List groups = new ArrayList();

		int i=0;
		String authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		while(authorizerClassName != null && !authorizerClassName.equals(""))
		{
			if(logger.isInfoEnabled())
				logger.info("Looking for user in " + authorizerClassName);
			
			try
			{
				groups.addAll(getAuthorizationModule(authorizerClassName, i).getGroups());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			i++;
			authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		}
		
		Collections.sort(groups, new ReflectionComparator("displayName"));

		return groups;
    }
    
	/**
	 * This method gets a list of users
	 */

	public List getUsers() throws Exception
	{
		List users = new ArrayList();

		int i=0;
		String authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		while(authorizerClassName != null && !authorizerClassName.equals(""))
		{
			if(logger.isInfoEnabled())
				logger.info("Looking for users in " + authorizerClassName);
			
			try
			{
				users.addAll(getAuthorizationModule(authorizerClassName, i).getUsers());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			i++;
			authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		}
		
		Collections.sort(users, new ReflectionComparator("displayName"));

		return users;
	}

	/*
	public List getFilteredUsers(String firstName, String lastName, String userName, String email, String[] roleIds) throws Exception
	{
		throw new Exception("Unsupported operation");
		//return null;
	}
	 */
	
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
		int i=0;
		String authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		while(authorizerClassName != null && !authorizerClassName.equals(""))
		{
			if(logger.isInfoEnabled())
				logger.info("Creating user in " + authorizerClassName);
			
			try
			{
				getAuthorizationModule(authorizerClassName, i).createInfoGluePrincipal(systemUserVO);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			i++;
			authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		}
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
		int i=0;
		String authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		while(authorizerClassName != null && !authorizerClassName.equals(""))
		{
			if(logger.isInfoEnabled())
				logger.info("Creating role in " + authorizerClassName);
			
			try
			{
				getAuthorizationModule(authorizerClassName, i).createInfoGlueRole(roleVO);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			i++;
			authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		}
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
		int i=0;
		String authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		while(authorizerClassName != null && !authorizerClassName.equals(""))
		{
			if(logger.isInfoEnabled())
				logger.info("Creating Group in " + authorizerClassName);
			
			try
			{
				getAuthorizationModule(authorizerClassName, i).createInfoGlueGroup(groupVO);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			i++;
			authorizerClassName = this.extraProperties.getProperty("" + i + ".authorizerClassName");
		}
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
