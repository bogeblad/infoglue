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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.infoglue.cms.entities.management.GroupVO;
import org.infoglue.cms.entities.management.RoleVO;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.NullObject;
import org.infoglue.deliver.util.webservices.WebServiceHelper;

/**
 * @author Mattias Bogeblad
 *
 * This authentication module authenticates an user against the ordinary infoglue database.
 */

public class WebServiceAuthorizationModule implements AuthorizationModule, Serializable
{
    private final static Logger logger = Logger.getLogger(WebServiceAuthorizationModule.class.getName());
    private final static DOMBuilder domHelper = new DOMBuilder();
   
	protected Properties extraProperties = null;
	
	/**
	 * This method gets the webservice utility.
	 */
	
	public WebServiceHelper getWebServiceHelper() throws SystemException
	{
		WebServiceHelper wsh = new WebServiceHelper();
		
		String serviceUrl = extraProperties.getProperty("ws.serviceUrl");
		if(logger.isDebugEnabled())
			logger.info("serviceUrl:" + serviceUrl);
		
		if(serviceUrl == null || serviceUrl.equals(""))
			throw new SystemException("The parameter ws.serviceUrl was not found in extra parameters. The url must be defined.");
			
		wsh.setServiceUrl(serviceUrl);
		
		return wsh;
	}
	
	/**
	 * Gets is the implementing class can update as well as read 
	 */
	
	public boolean getSupportUpdate() 
	{
		return false;
	}
	
	/**
	 * Gets is the implementing class can delete as well as read 
	 */
	
	public boolean getSupportDelete()
	{
		return false;
	}
	
	/**
	 * Gets is the implementing class can create as well as read 
	 */
	
	public boolean getSupportCreate()
	{
		return false;
	}
	
	/**
	 * Gets an authorized InfoGluePrincipal 
	 */
	
	public InfoGluePrincipal getAuthorizedInfoGluePrincipal(String userName) throws Exception
	{
		String userCacheTimeout = this.extraProperties.getProperty("userCacheTimeout", "1800");

		String authorizerIndex = this.extraProperties.getProperty("authorizerIndex");
	    if(authorizerIndex == null)
	    	authorizerIndex = "";

	    String key = "user_" + userName + authorizerIndex;
	    InfoGluePrincipal infogluePrincipal = null;
	    Object infogluePrincipalObject = CacheController.getCachedObjectFromAdvancedCache("WebServiceAuthorizationCache", key, new Integer(userCacheTimeout).intValue());
		if(infogluePrincipalObject != null)
		{
			if(infogluePrincipalObject instanceof NullObject)
			{
				return null;
			}
			else
			{
				infogluePrincipal = (InfoGluePrincipal)infogluePrincipalObject;
				logger.warn("Returning cached user:" + userName + ":" + infogluePrincipal);
				if(logger.isDebugEnabled())
					logger.info("Returning cached user:" + userName + ":" + infogluePrincipal);
				return infogluePrincipal;
			}
		}

		String administratorUserName = CmsPropertyHandler.getAdministratorUserName();
		String administratorEmail 	 = CmsPropertyHandler.getAdministratorEmail();
		//String administratorUserName = CmsPropertyHandler.getProperty("administratorUserName");
		//String administratorEmail 	 = CmsPropertyHandler.getProperty("administratorEmail");
		
		final boolean isAdministrator = userName.equalsIgnoreCase(administratorUserName) ? true : false;
		if(isAdministrator)
		{
			infogluePrincipal = new InfoGluePrincipal(userName, "System", "Administrator", administratorEmail, new ArrayList(), new ArrayList(), isAdministrator, this);
		}
		else
		{		
			try
			{
				String xml = getWebServiceHelper().getString("getAuthorizedInfoGluePrincipal", userName);
				if(logger.isDebugEnabled())
					logger.info("xml:" + xml);
				
				if(xml != null && !xml.equals(""))
				{
					Document document = domHelper.getDocument(xml);

					Element errorIdElement 		= (Element)document.selectSingleNode("/error/id");
					if(errorIdElement != null)
					{
						Element errorMessageElement = (Element)document.selectSingleNode("/error/message");
						throw new Exception("Error reported by webservice: " + errorMessageElement.getText() + "(" + errorIdElement.getText() + ") - for user '" + userName + "'\n\n" + xml);
					}
					
					Element firstNameElement 	= (Element)document.selectSingleNode("/user/firstName");
					Element lastNameElement 	= (Element)document.selectSingleNode("/user/lastName");
					Element emailElement 		= (Element)document.selectSingleNode("/user/email");
					
					String firstName 			= firstNameElement.getText();
					String lastName 			= lastNameElement.getText();
					String email 				= emailElement.getText();
		
					Map metaMap = new HashMap();
					Element metaElement 		= (Element)document.selectSingleNode("/user/meta");
					List metaChildNodes 		= metaElement.elements();
					Iterator metaChildNodesIterator = metaChildNodes.iterator();
					while(metaChildNodesIterator.hasNext())
					{
						Element valueElement = (Element)metaChildNodesIterator.next();
						String name = valueElement.getName();
						String value = valueElement.getText();
						metaMap.put(name, value);
					}
	
					List roles = new ArrayList();
					List groups = new ArrayList();
	
					List rolesElementList 				= document.selectNodes("/user/roles/role");

					Iterator rolesElementListIterator 	= rolesElementList.iterator();
					while(rolesElementListIterator.hasNext())
					{
						Element roleElement = (Element)rolesElementListIterator.next();
			
						Element nameElement 		= (Element)roleElement.selectSingleNode("name");
						Element displayNameElement 	= (Element)roleElement.selectSingleNode("displayName");
						Element descriptionElement 	= (Element)roleElement.selectSingleNode("description");
	
						String name 				= nameElement.getText();
						
						String displayName 			= name;
						if(displayNameElement != null && !displayNameElement.getText().equals(""))
							displayName				= displayNameElement.getText();
							
						String description 			= "No description";
						if(descriptionElement != null && !descriptionElement.getText().equals(""))
							description 			= descriptionElement.getText();
	
						if(logger.isDebugEnabled())
							logger.info("name:" + name);
						
						InfoGlueRole infoglueRole 	= new InfoGlueRole(name, displayName, description, this);
						roles.add(infoglueRole);
					}
	
					List groupsElementList 				= document.selectNodes("/user/groups/group");
					Iterator groupsElementListIterator 	= groupsElementList.iterator();
					while(groupsElementListIterator.hasNext())
					{
						Element groupElement = (Element)groupsElementListIterator.next();
			
						Element nameElement 		= (Element)groupElement.selectSingleNode("name");
						Element displayNameElement 	= (Element)groupElement.selectSingleNode("displayName");
						Element descriptionElement 	= (Element)groupElement.selectSingleNode("description");
	
						String name 				= nameElement.getText();
	
						String displayName 			= name;
						if(displayNameElement != null && !displayNameElement.getText().equals(""))
							displayName				= displayNameElement.getText();
						
						String description 			= "No description";
						if(descriptionElement != null && !descriptionElement.getText().equals(""))
							description 			= descriptionElement.getText();
						
						if(logger.isDebugEnabled())
							logger.info("name:" + name);
						
						InfoGlueGroup infoglueGroup = new InfoGlueGroup(name, displayName, description, this);
						groups.add(infoglueGroup);
					}
					
					infogluePrincipal = new InfoGluePrincipal(userName, firstName, lastName, email, roles, groups, metaMap, isAdministrator, this);
		
				    if(infogluePrincipal != null)
				    	CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, infogluePrincipal, null, false);
				}
			}
			catch(Exception e)
			{
				logger.warn("Error:" + e.getMessage(), e);
				//e.printStackTrace();
			    CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, new NullObject(), null, false);
			}
		}
	    	
		return infogluePrincipal;
	}

	
	/**
	 * Gets an authorized InfoGlueRole.
	 */
	
	public InfoGlueRole getAuthorizedInfoGlueRole(String roleName) throws Exception
	{
		InfoGlueRole infoglueRole = null;

		List roles = getRoles();
		Iterator rolesIterator = roles.iterator();
		while(rolesIterator.hasNext())
		{
			InfoGlueRole infoglueRoleCandidate = (InfoGlueRole)rolesIterator.next();
			if(infoglueRoleCandidate.getName().equals(roleName))
			{
				infoglueRole = infoglueRoleCandidate;
				break;
			}
		}
		
		return infoglueRole;
	}

	
	/**
	 * Gets an authorized InfoGlueGroup.
	 */
	
	public InfoGlueGroup getAuthorizedInfoGlueGroup(String groupName) throws Exception
	{
		InfoGlueGroup infoglueGroup = null;

		List groups = getGroups();
		Iterator groupsIterator = groups.iterator();
		while(groupsIterator.hasNext())
		{
			InfoGlueGroup infoglueGroupCandidate = (InfoGlueGroup)groupsIterator.next();
			if(infoglueGroupCandidate.getName().equals(groupName))
			{
				infoglueGroup = infoglueGroupCandidate;
				break;
			}
		}
		
		return infoglueGroup;
	}

	/**
	 * This method gets a users roles
	 */
	
	public List authorizeUser(String userName) throws Exception
	{
		return getRoles(userName);
	}
	


	
	/**
	 * Return a List of roles associated with the given User. Any
	 * roles present in the user's directory entry are supplemented by
	 * a directory search. If no roles are associated with this user,
	 * a zero-length List is returned.
	 *
	 * @param context The directory context we are searching
	 * @param user The User to be checked
	 *
	 * @exception NamingException if a directory server error occurs
	 */
	
	protected List getRoles(String userName) throws Exception 
	{
		List roles = new ArrayList();
		
		InfoGluePrincipal principal = getAuthorizedInfoGluePrincipal(userName);
		if(principal != null && principal.getRoles() != null)
			roles = principal.getRoles();
		
		return roles;
	}
	
	
	
	/**
	 * Return a List of roles associated with the given User. Any
	 * roles present in the user's directory entry are supplemented by
	 * a directory search. If no roles are associated with this user,
	 * a zero-length List is returned.
	 *
	 * @param context The directory context we are searching
	 * @param user The User to be checked
	 *
	 * @exception NamingException if a directory server error occurs
	 */
	
	protected List getGroups(String userName) throws Exception 
	{
		List groups = new ArrayList();
		
		InfoGluePrincipal principal = getAuthorizedInfoGluePrincipal(userName);
		if(principal != null && principal.getRoles() != null)
			groups = principal.getGroups();

		return groups;
	}
	
	
	/**
	 * This method gets a list of roles
	 */
	
	public List getRoles() throws Exception
	{
	    logger.info("getRoles start....");

		String roleCacheTimeout = this.extraProperties.getProperty("roleCacheTimeout", "1800");

		String authorizerIndex = this.extraProperties.getProperty("authorizerIndex");
	    if(authorizerIndex == null)
	    	authorizerIndex = "";

	    String key = "allRoles" + authorizerIndex;
		List roles = (List)CacheController.getCachedObjectFromAdvancedCache("JNDIAuthorizationCache", key, new Integer(roleCacheTimeout).intValue());
		if(roles != null)
			return roles;
		
		roles = new ArrayList();

		String xml = getWebServiceHelper().getString("getRoles");
		if(logger.isDebugEnabled())
			logger.info("Roles xml:" + xml);
		
		if(xml != null && !xml.equals(""))
		{
			Document document = domHelper.getDocument(xml);
			List roleNodes = document.selectNodes("/roles/role");
			Iterator rolesIterator = roleNodes.iterator();
			while(rolesIterator.hasNext())
			{
				Element roleElement 		= (Element)rolesIterator.next();
				
				Element nameElement 		= (Element)roleElement.selectSingleNode("name");
				Element displayNameElement 	= (Element)roleElement.selectSingleNode("displayName");
				Element descriptionElement 	= (Element)roleElement.selectSingleNode("description");
				String name 				= nameElement.getText();
				
				String displayName 			= name;
				if(displayNameElement != null && !displayNameElement.getText().equals(""))
					displayName				= displayNameElement.getText();
				
				String description 			= "No description";
				if(descriptionElement != null && !descriptionElement.getText().equals(""))
					description 			= descriptionElement.getText();
					
				InfoGlueRole infoglueRole = new InfoGlueRole(name, displayName, description, this);
				roles.add(infoglueRole);
			}
		}
		
	    if(roles != null)
	    	CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, roles, null, false);

		return roles;
	}

	/**
	 * This method gets a list of users
	 */
	
	public List getUsers() throws Exception
	{
		logger.info("*******************");
	    logger.info("* getUsers start  *");
	    logger.info("*******************");
	    
		String userCacheTimeout = this.extraProperties.getProperty("userCacheTimeout", "1800");

		String authorizerIndex = this.extraProperties.getProperty("authorizerIndex");
	    if(authorizerIndex == null)
	    	authorizerIndex = "";

		String key = "allUsers" + authorizerIndex;
		List users = (List)CacheController.getCachedObjectFromAdvancedCache("JNDIAuthorizationCache", key, new Integer(userCacheTimeout).intValue());
		if(users != null)
			return users;
		
		users = new ArrayList();
		
		String xml = getWebServiceHelper().getString("getUsers");
		
		if(xml != null && !xml.equals(""))
		{
			try
			{
				Document document = domHelper.getDocument(xml);
				List usersNodes = document.selectNodes("/users");
				
				List userNodes = document.selectNodes("/users/user");
				Iterator usersIterator = userNodes.iterator();
				while(usersIterator.hasNext())
				{
					Element userElement 		= (Element)usersIterator.next();
					
					Element userNameElement 	= (Element)userElement.selectSingleNode("userName");
					if(userNameElement == null)
						userNameElement 		= (Element)userElement.selectSingleNode("username");
						
					Element firstNameElement 	= (Element)userElement.selectSingleNode("firstName");
					Element lastNameElement 	= (Element)userElement.selectSingleNode("lastName");
					Element emailElement 		= (Element)userElement.selectSingleNode("email");
					String userName 			= userNameElement.getText();
					
					if(logger.isDebugEnabled())
						logger.info("userName:" + userName);
					
					String firstName 			= firstNameElement.getText();
					String lastName 			= lastNameElement.getText();
					String email 				= emailElement.getText();
					
					/*
					Map metaMap = new HashMap();
					Element metaElement 		= (Element)document.selectSingleNode("/user/meta");
					List metaChildNodes 		= metaElement.elements();
					Iterator metaChildNodesIterator = metaChildNodes.iterator();
					while(metaChildNodesIterator.hasNext())
					{
						Element valueElement = (Element)metaChildNodesIterator.next();
						String name = valueElement.getName();
						String value = valueElement.getText();
						metaMap.put(name, value);
					}
					*/
		
					List roles = new ArrayList();
					List groups = new ArrayList();
		
					List rolesElementList 				= userElement.selectNodes("roles/role");
					
					Iterator rolesElementListIterator 	= rolesElementList.iterator();
					while(rolesElementListIterator.hasNext())
					{
						Element roleElement = (Element)rolesElementListIterator.next();
			
						Element nameElement 		= (Element)roleElement.selectSingleNode("name");
						Element displayNameElement 	= (Element)roleElement.selectSingleNode("displayName");
						Element descriptionElement 	= (Element)roleElement.selectSingleNode("description");
	
						String name 				= nameElement.getText();

						String displayName 			= name;
						if(displayNameElement != null && !displayNameElement.getText().equals(""))
							displayName				= displayNameElement.getText();
						
						String description 			= "No description";
						if(descriptionElement != null && !descriptionElement.getText().equals(""))
							description 			= descriptionElement.getText();
						
						InfoGlueRole infoglueRole 	= new InfoGlueRole(name, displayName, description, this);
						roles.add(infoglueRole);
						
						List groupsElementList 				= roleElement.selectNodes("groups/group");
						Iterator groupsElementListIterator 	= groupsElementList.iterator();
						while(groupsElementListIterator.hasNext())
						{
							Element groupElement = (Element)groupsElementListIterator.next();
				
							Element groupNameElement 		= (Element)groupElement.selectSingleNode("name");
							Element groupDisplayNameElement = (Element)groupElement.selectSingleNode("displayName");
							Element groupDescriptionElement = (Element)groupElement.selectSingleNode("description");
		
							String groupName 				= nameElement.getText();
							
							String groupDisplayName 		= groupName;
							if(groupDisplayNameElement != null && !groupDisplayNameElement.getText().equals(""))
								groupDisplayName			= groupDisplayNameElement.getText();
							
							String groupDescription 		= "No description";
							if(groupDescriptionElement != null && !groupDescriptionElement.getText().equals(""))
								groupDescription 			= groupDescriptionElement.getText();
							
							InfoGlueGroup infoglueGroup = new InfoGlueGroup(groupName, groupDisplayName, groupDescription, this);
							groups.add(infoglueGroup);
						}

					}
							
					List groupsElementList 				= userElement.selectNodes("groups/group");
					
					Iterator groupsElementListIterator 	= groupsElementList.iterator();
					while(groupsElementListIterator.hasNext())
					{
						Element groupElement = (Element)groupsElementListIterator.next();
			
						Element nameElement 		= (Element)groupElement.selectSingleNode("name");
						Element displayNameElement 	= (Element)groupElement.selectSingleNode("displayName");
						Element descriptionElement 	= (Element)groupElement.selectSingleNode("description");
	
						String name 				= nameElement.getText();
						
						String displayName 			= name;
						if(displayNameElement != null && !displayNameElement.getText().equals(""))
							displayName				= displayNameElement.getText();
						
						String description 			= "No description";
						if(descriptionElement != null && !descriptionElement.getText().equals(""))
							description 			= descriptionElement.getText();
						
						InfoGlueGroup infoglueGroup = new InfoGlueGroup(name, displayName, description, this);
						groups.add(infoglueGroup);
					}
			
					InfoGluePrincipal infogluePrincipal = new InfoGluePrincipal(userName, firstName, lastName, email, roles, groups, false, this);
					users.add(infogluePrincipal);
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		
		logger.info("getUsers end...");

	    if(users != null)
	    	CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, users, null, false);
	    
		return users;
	}
	
	public List getFilteredUsers(String firstName, String lastName, String userName, String email, String[] roleIds) throws SystemException, Bug
	{
		List users = new ArrayList();
		//TODO		
		return users;
	}

    /* (non-Javadoc)
     * @see org.infoglue.cms.security.AuthorizationModule#getRoleUsers(java.lang.String)
     */
    public List getUsers(String roleName) throws Exception
    {
        return getRoleUsers(roleName);
    }

	
	public List getRoleUsers(String roleName) throws Exception
	{
		List users = new ArrayList();
		
		List allUsers = this.getUsers();
		Iterator allUsersIterator = allUsers.iterator();
		while(allUsersIterator.hasNext())
		{
			InfoGluePrincipal userCandidate = (InfoGluePrincipal)allUsersIterator.next();
			if(userCandidate.getRoles().contains(getAuthorizedInfoGlueRole(roleName)))
			{
				users.add(userCandidate);
			}
		}
		
	    return users;
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
    }

    public Object getTransactionObject()
    {
        return null;
    }

    
    /**
     * This method returns a list of all groups available to InfoGlue.
     */
    public List getGroups() throws Exception
    {
	    logger.info("getGroups start....");

		String groupCacheTimeout = this.extraProperties.getProperty("groupCacheTimeout", "1800");

		String authorizerIndex = this.extraProperties.getProperty("authorizerIndex");
	    if(authorizerIndex == null)
	    	authorizerIndex = "";

		String key = "allGroups" + authorizerIndex;
		List groups = (List)CacheController.getCachedObjectFromAdvancedCache("JNDIAuthorizationCache", key, new Integer(groupCacheTimeout).intValue());
		if(groups != null)
			return groups;
		
		groups = new ArrayList();

		String xml = getWebServiceHelper().getString("getGroups");

		if(logger.isDebugEnabled())
			logger.info("Groups xml:" + xml);
		
		Document document = domHelper.getDocument(xml);

		List groupNodes = document.selectNodes("/groups/group");
		Iterator groupsIterator = groupNodes.iterator();
		while(groupsIterator.hasNext())
		{
			Element groupElement 		= (Element)groupsIterator.next();
			
			Element nameElement 		= (Element)groupElement.selectSingleNode("name");
			Element displayNameElement 	= (Element)groupElement.selectSingleNode("displayName");
			Element descriptionElement 	= (Element)groupElement.selectSingleNode("description");

			String name 				= nameElement.getText();
			
			String displayName 			= name;
			if(displayNameElement != null && !displayNameElement.getText().equals(""))
				displayName				= displayNameElement.getText();
			
			String description 			= "No description";
			if(descriptionElement != null && !descriptionElement.getText().equals(""))
				description 			= descriptionElement.getText();
				
			InfoGlueGroup infoglueGroup = new InfoGlueGroup(name, displayName, description, this);
			groups.add(infoglueGroup);
		}

	    if(groups != null)
	    	CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, groups, null, false);

		return groups;
    }

    /** 
     * Gets a list of users which is memebers of the given group
     */
    public List getGroupUsers(String groupName) throws Exception
    {
		List users = new ArrayList();
		List allUsers = this.getUsers();
		Iterator allUsersIterator = allUsers.iterator();
		while(allUsersIterator.hasNext())
		{
			InfoGluePrincipal userCandidate = (InfoGluePrincipal)allUsersIterator.next();
			if(userCandidate.getGroups().contains(getAuthorizedInfoGlueGroup(groupName)))
			{
				users.add(userCandidate);
			}
		}
		
		return users;
	}

    
	public void createInfoGluePrincipal(SystemUserVO systemUserVO) throws Exception
	{
		throw new SystemException("The WebService Authorization module does not support creation of users yet...");
	}

	public void updateInfoGluePrincipalPassword(String userName) throws Exception 
	{
		throw new SystemException("The WebService Authorization module does not support updates of users yet...");
	}

	public void updateInfoGlueAnonymousPrincipalPassword() throws Exception 
	{
		throw new SystemException("The WebService Authorization module does not support updates of user password yet....");
	}

	public void updateInfoGluePrincipalPassword(String userName, String oldPassword, String newPassword) throws Exception
	{
		throw new SystemException("The WebService Authorization module does not support updates of user password yet...");
	}
	
	public void deleteInfoGluePrincipal(String userName) throws Exception
	{
		throw new SystemException("The WebService Authorization module does not support deletion of users yet...");
	}
	
	public void createInfoGlueRole(RoleVO roleVO) throws Exception
	{
		throw new SystemException("The WebService Authorization module does not support creation of users yet...");
	}

	public void updateInfoGlueRole(RoleVO roleVO, String[] userNames) throws Exception
	{
	}

	public void deleteInfoGlueRole(String roleName) throws Exception
	{
		throw new SystemException("The WebService Authorization module does not support deletion of roles yet...");
	}

    public void updateInfoGluePrincipal(SystemUserVO systemUserVO, String[] roleNames, String[] groupNames) throws Exception
    {
    }

	public void updateInfoGluePrincipal(SystemUserVO systemUserVO, String oldPassword, String[] roleNames, String[] groupNames) throws Exception
	{
	}

    public void createInfoGlueGroup(GroupVO groupVO) throws Exception
    {
		throw new SystemException("The WebService Authorization module does not support creation of groups yet...");        
    }

    public void updateInfoGlueGroup(GroupVO roleVO, String[] userNames) throws Exception
    {
    }

    public void deleteInfoGlueGroup(String groupName) throws Exception
    {
		throw new SystemException("The WebService Authorization module does not support deletion of groups yet...");        
    }

	public void addUserToGroup(String groupName, String userName) throws Exception 
	{
		throw new SystemException("The WebService Authorization module does not support adding of users to groups yet...");
	}

	public void addUserToRole(String roleName, String userName) throws Exception 
	{
		throw new SystemException("The WebService Authorization module does not support adding of users to roles yet...");
	}

	public void removeUserFromGroup(String groupName, String userName) throws Exception 
	{
		throw new SystemException("The WebService Authorization module does not support removing users from groups yet...");
	}

	public void removeUserFromRole(String roleName, String userName) throws Exception 
	{
		throw new SystemException("The WebService Authorization module does not support removing users from roles yet...");
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

}
