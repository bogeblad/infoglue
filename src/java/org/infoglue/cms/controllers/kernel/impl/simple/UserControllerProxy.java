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
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.AuthorizationModule;
import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.NullObject;


/**
 * @author Mattias Bogeblad
 * 
 * This class acts as the proxy for getting the right roles.
 */

public class UserControllerProxy extends BaseController 
{
    private final static Logger logger = Logger.getLogger(UserControllerProxy.class.getName());

	private AuthorizationModule authorizationModule = null;
	private Database transactionObject = null;
	
	public UserControllerProxy(Database transactionObject)
	{
	    this.transactionObject = transactionObject;
	}
	
	public static UserControllerProxy getController()
	{
		return new UserControllerProxy(null);
	}	
	
	public static UserControllerProxy getController(Database transactionObject)
	{
	    return new UserControllerProxy(transactionObject);
	}
	
	/**
	 * This method instantiates the AuthorizationModule.
	 */
	
	private AuthorizationModule getAuthorizationModule() throws SystemException
	{
		//if(authorizationModule == null)
	    //{
			try
	    	{
			    logger.info("InfoGlueAuthenticationFilter.authorizerClass:" + InfoGlueAuthenticationFilter.authorizerClass);
				authorizationModule = (AuthorizationModule)Class.forName(InfoGlueAuthenticationFilter.authorizerClass).newInstance();
				logger.info("authorizationModule:" + authorizationModule);
				authorizationModule.setExtraProperties(InfoGlueAuthenticationFilter.extraProperties);
				authorizationModule.setTransactionObject(this.transactionObject);
				logger.info("InfoGlueAuthenticationFilter.extraProperties:" + InfoGlueAuthenticationFilter.extraProperties);
	    	}
	    	catch(Exception e)
	    	{
	    		//e.printStackTrace();
	    		logger.error("There was an error initializing the authorizerClass:" + e.getMessage(), e);
	    		throw new SystemException("There was an error initializing the authorizerClass:" + e.getMessage(), e);
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
	 * This method returns a complete list of available users
	 */
	
    public List getAllUsers() throws ConstraintException, SystemException, Exception
    {
    	List users = new ArrayList();
    	
		users = getAuthorizationModule().getUsers();
		
		Collections.sort(users, new ReflectionComparator("displayName"));

    	return users;
    }

	/**
	 * This method returns a list of all sought for users
	 */
	
    public List getFilteredUsers(String firstName, String lastName, String userName, String email, String[] roleNames) throws Exception
    {
    	List users = new ArrayList();
    	
		users = getAuthorizationModule().getFilteredUsers(firstName, lastName, userName, email, roleNames);
    	
    	return users;
    }
    
	/**
	 * This method returns a certain user
	 */
	
    public InfoGluePrincipal getUser(String userName) throws ConstraintException, SystemException, Exception
    {
    	Object infoGluePrincipalCandidate = CacheController.getCachedObjectFromAdvancedCache("principalCache", userName);
    	//Object infoGluePrincipalCandidate = CacheController.getCachedObjectFromAdvancedCache("principalCache", userName, 300);
    	//InfoGluePrincipal infoGluePrincipal = (InfoGluePrincipal)CacheController.getCachedObjectFromAdvancedCache("principalCache", userName, 300);
    	if(infoGluePrincipalCandidate != null)
		{
	    	if(infoGluePrincipalCandidate instanceof NullObject)
				return null;
			else
				return (InfoGluePrincipal)infoGluePrincipalCandidate;
		}
    	
    	InfoGluePrincipal infoGluePrincipal = getAuthorizationModule().getAuthorizedInfoGluePrincipal(userName);
	   
		if(infoGluePrincipal != null)
			CacheController.cacheObjectInAdvancedCache("principalCache", userName, infoGluePrincipal, new String[]{}, false);
		else
			CacheController.cacheObjectInAdvancedCache("principalCache", userName, new NullObject(), new String[]{}, false);
			
    	return infoGluePrincipal;
    }
    
	/**
	 * This method returns if a user exists
	 */
	
    public boolean userExists(String userName) throws ConstraintException, SystemException, Exception
    {
    	Boolean userExists = (Boolean)CacheController.getCachedObjectFromAdvancedCache("principalCache", "exists_" + userName);
		if(userExists != null)
			return userExists;
    	
		userExists = getAuthorizationModule().userExists(userName);
	   
		CacheController.cacheObjectInAdvancedCache("principalCache", "exists_" + userName, userExists, new String[]{}, false);
			
    	return userExists;
    }
    
	/**
	 * This method creates a new user
	 */
	
	public InfoGluePrincipal createUser(SystemUserVO systemUserVO) throws ConstraintException, SystemException, Exception
	{
		getAuthorizationModule().createInfoGluePrincipal(systemUserVO);
    	
		return getUser(systemUserVO.getUserName());
	}

	/**
	 * This method updates an existing user
	 */
	
	public void updateUser(SystemUserVO systemUserVO, String[] roleNames, String[] groupNames) throws ConstraintException, SystemException, Exception
	{
		getAuthorizationModule().updateInfoGluePrincipal(systemUserVO, roleNames, groupNames);
	}

	/**
	 * This method updates an existing user
	 */
	
	public void updateUser(SystemUserVO systemUserVO, String oldPassword, String[] roleNames, String[] groupNames) throws ConstraintException, SystemException, Exception
	{
		getAuthorizationModule().updateInfoGluePrincipal(systemUserVO, oldPassword, roleNames, groupNames);
	}

	/**
	 * This method makes a new password and sends it to the user
	 */
	
	public void updateUserPassword(String userName) throws ConstraintException, SystemException, Exception
	{
	    if(userName.equals(CmsPropertyHandler.getAnonymousUser()))
	        throw new SystemException("You must not change password on this user as it's needed by the system.");

		getAuthorizationModule().updateInfoGluePrincipalPassword(userName);
	}

	/**
	 * This method makes a new password and sends it to the user
	 */
	
	public void updateAnonymousUserPassword() throws ConstraintException, SystemException, Exception
	{
		getAuthorizationModule().updateInfoGlueAnonymousPrincipalPassword();
	}
	
	/**
	 * This method makes a new password and sends it to the user
	 */
	
	public void updateUserPassword(String userName, String oldPassword, String newPassword) throws ConstraintException, SystemException, Exception
	{
		getAuthorizationModule().updateInfoGluePrincipalPassword(userName, oldPassword, newPassword);
	}

	/**
	 * This method deletes an existing user
	 */
	
	public void deleteUser(String userName) throws ConstraintException, SystemException, Exception
	{
		getAuthorizationModule().deleteInfoGluePrincipal(userName);
	}
	
	public BaseEntityVO getNewVO()
	{
		return null;
	}
 
}
