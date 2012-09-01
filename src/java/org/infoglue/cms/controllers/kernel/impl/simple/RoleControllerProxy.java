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
import org.infoglue.cms.entities.management.RoleVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.AuthorizationModule;
import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.cms.util.sorters.ReflectionComparator;

/**
 * @author Mattias Bogeblad
 * 
 * This class acts as the proxy for getting the right roles.
 */

public class RoleControllerProxy extends BaseController 
{
	private AuthorizationModule authorizationModule = null;
	private Database transactionObject = null;

	public RoleControllerProxy(Database transactionObject)
	{
	    this.transactionObject = transactionObject;
	}

	public static RoleControllerProxy getController()
	{
		return new RoleControllerProxy(null);
	}
	
	public static RoleControllerProxy getController(Database transactionObject)
	{
	    return new RoleControllerProxy(transactionObject);
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
	
    public List getAllRoles() throws ConstraintException, SystemException, Exception
    {
    	List roles = new ArrayList();
    	
		roles = getAuthorizationModule().getRoles();
		
		Collections.sort(roles, new ReflectionComparator("displayName"));
		
    	return roles;
    }

	/**
	 * This method returns a certain role
	 */
	
	public InfoGlueRole getRole(String roleName) throws ConstraintException, SystemException, Exception
	{
		InfoGlueRole infoGlueRole = null;
    	
		infoGlueRole = getAuthorizationModule().getAuthorizedInfoGlueRole(roleName);
    	
		return infoGlueRole;
	}

	/**
	 * This method returns if a role exists
	 */
	
    public boolean roleExists(String roleName) throws ConstraintException, SystemException, Exception
    {
		return getAuthorizationModule().roleExists(roleName);
    }

	/**
	 * This method returns a list of InfoGlue Principals which are part of this role
	 */
	
	public List getInfoGluePrincipals(String roleName) throws ConstraintException, SystemException, Exception
	{
		List infoGluePrincipals = new ArrayList();
    	
		infoGluePrincipals = getAuthorizationModule().getUsers(roleName);
		
		Collections.sort(infoGluePrincipals, new ReflectionComparator("displayName"));
    	
		return infoGluePrincipals;
	}

	/**
	 * This method returns a list of InfoGlue Principals which are part of this role
	 */
	
	public List<InfoGluePrincipal> getInfoGluePrincipals(String roleName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws ConstraintException, SystemException, Exception
	{
		List infoGluePrincipals = new ArrayList();
    	
		infoGluePrincipals = getAuthorizationModule().getRoleUsers(roleName, offset, limit, sortProperty, direction, searchString);
		
		return infoGluePrincipals;
	}

	/**
	 * This method returns a list of InfoGlue Principals which are part of this role
	 */
	
	public Integer getInfoGluePrincipalsCount(String roleName, String searchString) throws ConstraintException, SystemException, Exception
	{
		Integer count = getAuthorizationModule().getRoleUserCount(roleName, searchString);
		
		return count;
	}

	/**
	 * This method returns a list of InfoGlue Principals which are not part of this role
	 */
	
	public List<InfoGluePrincipal> getInfoGluePrincipalsNotInRole(String roleName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws ConstraintException, SystemException, Exception
	{
		List infoGluePrincipals = new ArrayList();
    	
		infoGluePrincipals = getAuthorizationModule().getRoleUsersInverted(roleName, offset, limit, sortProperty, direction, searchString);
		
		return infoGluePrincipals;
	}

	/**
	 * This method returns a count of InfoGlue Principals which are not part of this role
	 */
	
	public Integer getInfoGluePrincipalsNotInRoleCount(String roleName, String searchString) throws ConstraintException, SystemException, Exception
	{
		Integer count = getAuthorizationModule().getRoleUserInvertedCount(roleName, searchString);
		
		return count;
	}

    
	/**
	 * This method creates a new role
	 */
	
	public InfoGlueRole createRole(RoleVO roleVO) throws ConstraintException, SystemException, Exception
	{
		InfoGlueRole infoGlueRole = null;
    	
		getAuthorizationModule().createInfoGlueRole(roleVO);
    	
		return getRole(roleVO.getRoleName());
	}

	/**
	 * This method updates an existing role
	 */
	
	public void updateRole(RoleVO roleVO, String[] userNames) throws ConstraintException, SystemException, Exception
	{
		getAuthorizationModule().updateInfoGlueRole(roleVO, userNames);
	}

	/**
	 * This method updates an existing group
	 */
	
	public void addUser(String roleName, String userName) throws ConstraintException, SystemException, Exception
	{
		getAuthorizationModule().addUserToRole(roleName, userName);
	}

	/**
	 * This method removes a user from group
	 */
	
	public void removeUser(String roleName, String userName) throws ConstraintException, SystemException, Exception
	{
		getAuthorizationModule().removeUserFromRole(roleName, userName);
	}

	/**
	 * This method deletes an existing user
	 */
	
	public void deleteRole(String roleName) throws ConstraintException, SystemException, Exception
	{
		getAuthorizationModule().deleteInfoGlueRole(roleName);
		AccessRightController.getController().delete(roleName);
	}
	
	public BaseEntityVO getNewVO()
	{
		return null;
	}

	public List getAvailableRoles(InfoGluePrincipal infoGluePrincipal, String interceptionPointName) throws ConstraintException, SystemException, Exception 
	{
		List availableRoles = new ArrayList();
		List allRoles = getAuthorizationModule().getRoles();
		
		if(this.transactionObject == null)
		{
			Database db = CastorDatabaseService.getDatabase();
	
			try 
			{
				beginTransaction(db);
				
				Iterator allRolesIterator = allRoles.iterator();
				while(allRolesIterator.hasNext())
				{
					InfoGlueRole role = (InfoGlueRole)allRolesIterator.next();
					boolean hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, interceptionPointName, "" + role.getName());
					if(hasAccess)
						availableRoles.add(role);
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
			Iterator allRolesIterator = allRoles.iterator();
			while(allRolesIterator.hasNext())
			{
				InfoGlueRole role = (InfoGlueRole)allRolesIterator.next();
				boolean hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(this.transactionObject, infoGluePrincipal, interceptionPointName, "" + role.getName());
				if(hasAccess)
					availableRoles.add(role);
			}						
		}
		
		return availableRoles;
	}
 
}
