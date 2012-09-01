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

import java.util.List;
import java.util.Properties;

import org.infoglue.cms.entities.management.GroupVO;
import org.infoglue.cms.entities.management.RoleVO;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;

/**
 * This interface defines what a autorizationModule has to fulfill.
 * 
 * @author Mattias Bogeblad
 */

public interface AuthorizationModule
{
	/**
	 * Gets is the implementing class can update as well as read 
	 */
	
	public boolean getSupportUpdate();
	
	/**
	 * Gets is the implementing class can delete as well as read 
	 */
	
	public boolean getSupportDelete();
	
	/**
	 * Gets is the implementing class can create as well as read 
	 */
	
	public boolean getSupportCreate();

	/**
	 * Gets an authorized InfoGluePrincipal 
	 */
	
	public InfoGluePrincipal getAuthorizedInfoGluePrincipal(String userName) throws Exception;
	
	/**
	 * Gets an InfoGlueRole 
	 */
	
	public InfoGlueRole getAuthorizedInfoGlueRole(String roleName) throws Exception;

	/**
	 * Gets an InfoGlueGroup 
	 */
	
	public InfoGlueGroup getAuthorizedInfoGlueGroup(String groupName) throws Exception;


	/**
	 * This method returns the number of roles available.  
	 */

	public Integer getRoleCount(String searchString) throws Exception;

	/**
	 * This method returns the number of groups available.   
	 */

	public Integer getGroupCount(String searchString) throws Exception;

	/**
	 * This method returns the number of users available.  
	 */

	public Integer getUserCount(String searchString) throws Exception;

	/**
	 * This method returns the number of users are connected to the given role (optionally filtered by search string).  
	 */

	public Integer getRoleUserCount(String roleName, String searchString) throws Exception;

	/**
	 * This method returns the number of users are connected to the given role (optionally filtered by search string).  
	 */

	public Integer getRoleUserInvertedCount(String roleName, String searchString) throws Exception;

	/**
	 * This method returns the number of users are connected to the given group (optionally filtered by search string).  
	 */

	public Integer getGroupUserCount(String groupName, String searchString) throws Exception;

	/**
	 * This method returns the number of users are connected to the given group (optionally filtered by search string).  
	 */

	public Integer getGroupUserInvertedCount(String groupName, String searchString) throws Exception;

	/**
	 * This method is used to fetch all available roles.  
	 */

	public List<InfoGlueRole> getRoles() throws Exception;

	/**
	 * This method is used to fetch all available groups.  
	 */

	public List<InfoGlueGroup> getGroups() throws Exception;

	/**
	 * This method is used to fetch all users.  
	 */

	public List<InfoGluePrincipal> getUsers() throws Exception;

	/**
	 * This method is used to fetch all users part of the named role.
	 * @deprecated
	 */

	public List<InfoGluePrincipal> getUsers(String roleName) throws Exception;

	/**
	 * This method is used to fetch all users part of the named role.  
	 */

	public List<InfoGluePrincipal> getRoleUsers(String roleName) throws Exception;

	/**
	 * This method is used to fetch all users part of the named role.  
	 */

	public List<InfoGluePrincipal> getRoleUsers(String roleName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception;

	/**
	 * This method is used to fetch all users not part of the named role.  
	 */

	public List<InfoGluePrincipal> getRoleUsersInverted(String roleName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception;

	/**
	 * This method is used to fetch all users part of the named group.  
	 */

	public List<InfoGluePrincipal> getGroupUsers(String groupName) throws Exception;

	/**
	 * This method is used to fetch all users part of the named group.  
	 */

	public List<InfoGluePrincipal> getGroupUsers(String groupName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception;

	/**
	 * This method is used to fetch all users not part of the named group.  
	 */

	public List<InfoGluePrincipal> getGroupUsersInverted(String groupName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception;

	/**
	 * This method is used to get a filtered list of all users.
	 * @param firstName
	 * @param lastName
	 * @param userName
	 * @param email
	 * @param roleIds
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public List<InfoGluePrincipal> getFilteredUsers(Integer offset, Integer limit, String sortProperty, String direction, String searchString, boolean populateRolesAndGroups) throws Exception;

	/**
	 * This method is used to create a new user.  
	 */

	public void createInfoGluePrincipal(SystemUserVO systemUserVO) throws Exception;

	/**
	 * This method is used to update an existing user.  
	 */

	public void updateInfoGluePrincipal(SystemUserVO systemUserVO, String[] roleNames, String[] groupNames) throws Exception;

	/**
	 * This method is used to update an existing user.  
	 */

	public void updateInfoGluePrincipal(SystemUserVO systemUserVO, String oldPassword, String[] roleNames, String[] groupNames) throws Exception;

	/**
	 * This method is used to send out a newpassword to an existing users.  
	 */

	public void updateInfoGluePrincipalPassword(String userName) throws Exception;

	/**
	 * This method is used to send out a newpassword to an existing users.  
	 */

	public void updateInfoGlueAnonymousPrincipalPassword() throws Exception;

	/**
	 * This method is used to send out a newpassword to an existing users.  
	 */

	public void updateInfoGluePrincipalPassword(String userName, String oldPassword, String newPassword) throws Exception;

	/**
	 * This method is used to delete an existing user.  
	 */

	public void deleteInfoGluePrincipal(String userName) throws Exception;


	/**
	 * This method is used to create a new rol.  
	 */

	public void createInfoGlueRole(RoleVO roleVO) throws Exception;

	/**
	 * This method is used to update an existing role.  
	 */

	public void updateInfoGlueRole(RoleVO roleVO, String[] userNames) throws Exception;

	/**
	 * This method is used to delete an existing role.  
	 */

	public void deleteInfoGlueRole(String roleName) throws Exception;


	/**
	 * This method is used to create a new group.  
	 */

	public void createInfoGlueGroup(GroupVO groupVO) throws Exception;

	/**
	 * This method is used to update an existing group.  
	 */

	public void updateInfoGlueGroup(GroupVO roleVO, String[] userNames) throws Exception;

	/**
	 * This method is used to add a user to an existing role.  
	 */
    public void addUserToRole(String roleName, String userName) throws Exception;

	/**
	 * This method is used to add a user to an existing group.  
	 */
    public void addUserToGroup(String groupName, String userName) throws Exception;

	/**
	 * This method is used to remove user from a role.  
	 */
    public void removeUserFromRole(String roleName, String userName) throws Exception;

	/**
	 * This method is used to remove user from a group.  
	 */
    public void removeUserFromGroup(String groupName, String userName) throws Exception;

	/**
	 * This method is used find out if a user exists. Much quicker than getAuthorizedPrincipal 
	 */
    public boolean userExists(String userName) throws Exception;

	/**
	 * This method is used find out if a role exists. Much quicker than getRole 
	 */
    public boolean roleExists(String roleName) throws Exception;

	/**
	 * This method is used find out if a group exists. Much quicker than getGroup 
	 */
    public boolean groupExists(String groupName) throws Exception;

	/**
	 * This method is used to delete an existing group.  
	 */

	public void deleteInfoGlueGroup(String groupName) throws Exception;

	public Properties getExtraProperties();

	public void setExtraProperties(Properties properties);
	
	public void setTransactionObject(Object transactionObject);
	
	public Object getTransactionObject();
}