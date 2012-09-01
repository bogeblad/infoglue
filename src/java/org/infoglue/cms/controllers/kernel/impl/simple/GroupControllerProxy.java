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


package org.infoglue.cms.controllers.kernel.impl.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.exolab.castor.jdo.Database;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.GroupVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.AuthorizationModule;
import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.sorters.ReflectionComparator;

/**
 * @author Mattias Bogeblad
 * 
 * This class acts as the proxy for getting the right groups.
 */

public class GroupControllerProxy extends BaseController 
{
	private AuthorizationModule authorizationModule = null;
	private Database transactionObject = null;

	public GroupControllerProxy(Database transactionObject)
	{
	    this.transactionObject = transactionObject;
	}

	public static GroupControllerProxy getController()
	{
		return new GroupControllerProxy(null);
	}

	public static GroupControllerProxy getController(Database transactionObject)
	{
		return new GroupControllerProxy(transactionObject);
	}

	/**
	 * This method instantiates the AuthorizationModule.
	 */
	
	private AuthorizationModule getAuthorizationModule()
	{
		//if(authorizationModule == null)
	    //{
			try
			{
				authorizationModule = (AuthorizationModule)Class.forName(InfoGlueAuthenticationFilter.authorizerClass).newInstance();
				authorizationModule.setExtraProperties(InfoGlueAuthenticationFilter.extraProperties);
				authorizationModule.setTransactionObject(this.transactionObject);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		//}
	   
		return authorizationModule;
	}

	/**
	 * This method return whether the module in question supports updates to the values.
	 */
	
	public boolean getSupportUpdate() throws ConstraintException, SystemException, Exception
	{
		return getAuthorizationModule().getSupportUpdate();
	}

	/**
	 * This method return whether the module in question supports deletes of users.
	 */
	
	public boolean getSupportDelete() throws ConstraintException, SystemException, Exception
	{
		return getAuthorizationModule().getSupportDelete();
	}

	/**
	 * This method return whether the module in question supports creation of new users.
	 */
	
	public boolean getSupportCreate() throws ConstraintException, SystemException, Exception
	{
		return getAuthorizationModule().getSupportCreate();
	}

	/**
	 * This method returns a specific content-object
	 */
	
    public List getAllGroups() throws ConstraintException, SystemException, Exception
    {
    	List groups = new ArrayList();
    	
		groups = getAuthorizationModule().getGroups();
		
		Collections.sort(groups, new ReflectionComparator("displayName"));

    	return groups;
    }

	/**
	 * This method returns a certain group
	 */
	
	public InfoGlueGroup getGroup(String groupName) throws ConstraintException, SystemException, Exception
	{
		InfoGlueGroup infoGlueGroup = null;
    	
		infoGlueGroup = getAuthorizationModule().getAuthorizedInfoGlueGroup(groupName);
    	
		return infoGlueGroup;
	}
	
	/**
	 * This method returns if a role exists
	 */
	
    public boolean groupExists(String groupName) throws ConstraintException, SystemException, Exception
    {
		return getAuthorizationModule().groupExists(groupName);
    }

	/**
	 * This method returns a list of InfoGlue Principals which are part of this group
	 */
	
	public List getInfoGluePrincipals(String groupName) throws ConstraintException, SystemException, Exception
	{
		List infoGluePrincipals = new ArrayList();
    	
		infoGluePrincipals = getAuthorizationModule().getGroupUsers(groupName);
		Collections.sort(infoGluePrincipals, new ReflectionComparator("name"));

		return infoGluePrincipals;
	}

	/**
	 * This method returns a list of InfoGlue Principals which are part of this group
	 */
	
	public List<InfoGluePrincipal> getInfoGluePrincipals(String groupName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws ConstraintException, SystemException, Exception
	{
		List infoGluePrincipals = new ArrayList();
    	
		infoGluePrincipals = getAuthorizationModule().getGroupUsers(groupName, offset, limit, sortProperty, direction, searchString);
		
		return infoGluePrincipals;
	}

	/**
	 * This method returns a list of InfoGlue Principals which are part of this group
	 */
	
	public Integer getInfoGluePrincipalsCount(String groupName, String searchString) throws ConstraintException, SystemException, Exception
	{
		Integer count = getAuthorizationModule().getGroupUserCount(groupName, searchString);
		
		return count;
	}

	/**
	 * This method returns a list of InfoGlue Principals which are part of this group
	 */
	
	public List<InfoGluePrincipal> getInfoGluePrincipalsNotInGroup(String groupName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws ConstraintException, SystemException, Exception
	{
		List infoGluePrincipals = new ArrayList();
    	
		infoGluePrincipals = getAuthorizationModule().getGroupUsersInverted(groupName, offset, limit, sortProperty, direction, searchString);
		
		return infoGluePrincipals;
	}

	/**
	 * This method returns a list of InfoGlue Principals which are part of this group
	 */
	
	public Integer getInfoGluePrincipalsNotInGroupCount(String groupName, String searchString) throws ConstraintException, SystemException, Exception
	{
		Integer count = getAuthorizationModule().getGroupUserInvertedCount(groupName, searchString);
		
		return count;
	}

    
	/**
	 * This method creates a new group
	 */
	
	public InfoGlueGroup createGroup(GroupVO groupVO) throws ConstraintException, SystemException, Exception
	{
		InfoGlueGroup infoGlueGroup = null;
    	
		getAuthorizationModule().createInfoGlueGroup(groupVO);
    	
		return getGroup(groupVO.getGroupName());
	}

	/**
	 * This method updates an existing group
	 */
	
	public void updateGroup(GroupVO groupVO, String[] userNames) throws ConstraintException, SystemException, Exception
	{
		getAuthorizationModule().updateInfoGlueGroup(groupVO, userNames);
	}

	/**
	 * This method updates an existing group
	 */
	
	public void addUser(String groupName, String userName) throws ConstraintException, SystemException, Exception
	{
		getAuthorizationModule().addUserToGroup(groupName, userName);
	}

	/**
	 * This method removes a user from group
	 */
	
	public void removeUser(String groupName, String userName) throws ConstraintException, SystemException, Exception
	{
		getAuthorizationModule().removeUserFromGroup(groupName, userName);
	}

	/**
	 * This method deletes an existing user
	 */
	
	public void deleteGroup(String groupName) throws ConstraintException, SystemException, Exception
	{
		getAuthorizationModule().deleteInfoGlueGroup(groupName);
		AccessRightController.getController().delete(groupName);
	}
	
	public BaseEntityVO getNewVO()
	{
		return null;
	}
 
	public List getAvailableGroups(InfoGluePrincipal infoGluePrincipal, String interceptionPointName) throws ConstraintException, SystemException, Exception 
	{
		List availableGroups = new ArrayList();
		List allGroups = getAuthorizationModule().getGroups();
		
		if(this.transactionObject == null)
		{
			Database db = CastorDatabaseService.getDatabase();
	
			try 
			{
				beginTransaction(db);
				
				Iterator allRolesIterator = allGroups.iterator();
				while(allRolesIterator.hasNext())
				{
					InfoGlueGroup group = (InfoGlueGroup)allRolesIterator.next();
					boolean hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, interceptionPointName, "" + group.getName());
					if(hasAccess)
						availableGroups.add(group);
				}			
				
				commitTransaction(db);
			} 
			catch (Exception e) 
			{
				rollbackTransaction(db);
				throw new SystemException(e);
			}
		}
		else
		{
			Iterator allRolesIterator = allGroups.iterator();
			while(allRolesIterator.hasNext())
			{
				InfoGlueGroup group = (InfoGlueGroup)allRolesIterator.next();
				boolean hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(this.transactionObject, infoGluePrincipal, interceptionPointName, "" + group.getName());
				if(hasAccess)
					availableGroups.add(group);
			}						
		}
		
		return availableGroups;
	}

}
