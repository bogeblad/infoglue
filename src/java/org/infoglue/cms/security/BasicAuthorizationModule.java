package org.infoglue.cms.security;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mattias Bogeblad
 *
 * This authorization module can be used by all Authorization modules that cannot provide advanced paging/search directly from the source.
 */

public abstract class BasicAuthorizationModule implements AuthorizationModule
{
	/*
	public abstract List<InfoGlueRole> getRoles() throws Exception;

	public abstract List<InfoGlueGroup> getGroups() throws Exception;

	public abstract List<InfoGluePrincipal> getUsers() throws Exception;

	public abstract List<InfoGluePrincipal> getRoleUsers(String roleName) throws Exception;

	public abstract List<InfoGluePrincipal> getGroupUsers(String roleName) throws Exception;

	public abstract List<InfoGluePrincipal> getFilteredUsers(Integer offset, Integer limit, String sortProperty, String direction, String searchString, boolean populateRolesAndGroups) throws Exception;
	*/
	
	public Integer getRoleCount(String searchString) throws Exception 
	{
		return getRoles().size();
	}

	public Integer getGroupCount(String searchString) throws Exception 
	{
		return getGroups().size();
	}

	public Integer getUserCount(String searchString) throws Exception 
	{
		return getUsers().size();
	}
	
	//Very bad basic implementation - should be overwritten by implementing class so it's effective.
	public Integer getRoleUserCount(String roleName, String searchString) throws Exception 
	{
		return getRoleUsers(roleName, null, null, null, null, searchString).size();
	}

	//Very bad basic implementation - should be overwritten by implementing class so it's effective.
	public Integer getRoleUserInvertedCount(String roleName, String searchString) throws Exception 
	{
		List<InfoGluePrincipal> allUsers = getFilteredUsers(null, null, null, null, searchString, false);
		List<InfoGluePrincipal> assignedUsers = getRoleUsers(roleName, null, null, null, null, searchString);
		
		List<InfoGluePrincipal> newAllUsers = new ArrayList<InfoGluePrincipal>();
		newAllUsers.addAll(allUsers);
		newAllUsers.removeAll(assignedUsers);
		
		return newAllUsers.size();
	}

	//Very bad basic implementation - should be overwritten by implementing class so it's effective.
	public Integer getGroupUserCount(String groupName, String searchString) throws Exception 
	{
		return getGroupUsers(groupName, null, null, null, null, searchString).size();
	}

	//Very bad basic implementation - should be overwritten by implementing class so it's effective.
	public Integer getGroupUserInvertedCount(String groupName, String searchString) throws Exception 
	{
		List<InfoGluePrincipal> allUsers = getFilteredUsers(null, null, null, null, searchString, false);
		List<InfoGluePrincipal> assignedUsers = getGroupUsers(groupName, null, null, null, null, searchString);
		
		List<InfoGluePrincipal> newAllUsers = new ArrayList<InfoGluePrincipal>();
		newAllUsers.addAll(allUsers);
		newAllUsers.removeAll(assignedUsers);
		
		return newAllUsers.size();
	}

	//Very bad basic implementation - should be overwritten by implementing class so it's effective.
	public List<InfoGluePrincipal> getRoleUsers(String roleName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception 
	{
		return getRoleUsers(roleName);
	}

	//Very bad basic implementation - should be overwritten by implementing class so it's effective.
	public List<InfoGluePrincipal> getRoleUsersInverted(String roleName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception 
	{
		List<InfoGluePrincipal> allUsers = getFilteredUsers(null, null, null, null, searchString, false);
		List<InfoGluePrincipal> assignedUsers = getRoleUsers(roleName);
		
		List<InfoGluePrincipal> newAllUsers = new ArrayList<InfoGluePrincipal>();
		newAllUsers.addAll(allUsers);
		newAllUsers.removeAll(assignedUsers);

		return newAllUsers;
	}

	//Very bad basic implementation - should be overwritten by implementing class so it's effective.
	public List<InfoGluePrincipal> getGroupUsers(String groupName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception 
	{
		return getGroupUsers(groupName);
	}

	//Very bad basic implementation - should be overwritten by implementing class so it's effective.
	public List<InfoGluePrincipal> getGroupUsersInverted(String groupName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception 
	{
		List<InfoGluePrincipal> allUsers = getFilteredUsers(null, null, null, null, searchString, false);
		List<InfoGluePrincipal> assignedUsers = getGroupUsers(groupName);
		
		List<InfoGluePrincipal> newAllUsers = new ArrayList<InfoGluePrincipal>();
		newAllUsers.addAll(allUsers);
		newAllUsers.removeAll(assignedUsers);

		return newAllUsers;
	}

}
