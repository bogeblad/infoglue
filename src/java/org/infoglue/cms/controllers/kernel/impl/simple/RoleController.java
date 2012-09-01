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
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Role;
import org.infoglue.cms.entities.management.RoleVO;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.entities.management.impl.simple.RoleImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserRoleImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * RoleHelper.java
 * Created on 2002-aug-28 
 * @author Stefan Sik, ss@frovi.com 
 * 
 * This class is a helper class for the use case handle roles
 */
public class RoleController extends BaseController
{
    private final static Logger logger = Logger.getLogger(RoleController.class.getName());

	/**
	 * Factory method
	 */

	public static RoleController getController()
	{
		return new RoleController();
	}
	
    public Role getRoleWithId(Integer roleId, Database db) throws SystemException, Bug
    {
		return (Role) getObjectWithId(RoleImpl.class, roleId, db);
    }

	public Role getRoleWithName(String roleName, Database db) throws SystemException, Bug
	{
		return (Role)getObjectWithId(RoleImpl.class, roleName, db);
	}
	
    public RoleVO getRoleVOWithId(Integer roleId) throws SystemException, Bug
    {
		return (RoleVO) getVOWithId(RoleImpl.class, roleId);
    }

	public RoleVO getRoleVOWithId(String roleName) throws SystemException, Bug
	{
		return (RoleVO) getVOWithId(RoleImpl.class, roleName);
	}

	public RoleVO getRoleVOWithId(String roleName, Database db) throws SystemException, Bug
	{
		return (RoleVO) getVOWithId(RoleImpl.class, roleName, db);
	}
    
    public List getRoleVOList() throws SystemException, Bug
    {
        return getAllVOObjects(RoleImpl.class, "roleName");
    }

    public List getRoleVOList(Database db) throws SystemException, Bug
    {
        return getAllVOObjects(RoleImpl.class, "roleName", db);
    }

    public RoleVO create(RoleVO roleVO) throws ConstraintException, SystemException
    {
        Role role = new RoleImpl();
        role.setValueObject(roleVO);
        role = (Role) createEntity(role);
        return role.getValueObject();
    }     

    public Role create(RoleVO roleVO, Database db) throws ConstraintException, SystemException, Exception
    {
        Role role = new RoleImpl();
        role.setValueObject(roleVO);
        role = (Role) createEntity(role, db);
        return role;
    }     

    public void delete(RoleVO roleVO) throws ConstraintException, SystemException, Exception
    {
    	removeUsers(roleVO.getRoleName());
    	deleteEntity(RoleImpl.class, roleVO.getRoleName());
    }        

    public void delete(RoleVO roleVO, Database db) throws ConstraintException, SystemException, Exception
    {
    	removeUsers(roleVO.getRoleName());
    	deleteEntity(RoleImpl.class, roleVO.getRoleName(), db);
    }        

	public void delete(String roleName) throws ConstraintException, SystemException, Exception
	{
    	removeUsers(roleName);
		deleteEntity(RoleImpl.class, roleName);
	}        

	public void delete(String roleName, Database db) throws ConstraintException, SystemException, Exception
	{
    	removeUsers(roleName);
		deleteEntity(RoleImpl.class, roleName, db);
	}        


	// Get list of users accosiated with this role
	public List<SystemUserVO> getRoleSystemUserVOList(Integer offset, Integer limit, String sortProperty, String direction, String searchString, String roleName, Database db)  throws SystemException, Bug
	{
		List<SystemUserVO> systemUsersVOList = new ArrayList();
		
		try 
		{
			systemUsersVOList = SystemUserController.getController().getFilteredSystemUserList(offset, limit, sortProperty, direction, searchString, roleName, null, db);
		}
		catch( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}
		
		return systemUsersVOList;		
	}

	public List getRoleSystemUserVOList(Integer offset, Integer limit, String sortProperty, String direction, String searchString, String roleName)  throws SystemException, Bug
	{
		List<SystemUserVO> systemUsersVOList = new ArrayList();
		
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			systemUsersVOList = getRoleSystemUserVOList(offset, limit, sortProperty, direction, searchString, roleName, db);
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}		
		return systemUsersVOList;
	}

	// Get list of users accosiated with this role
	public Integer getRoleSystemUserCount(String roleName, String searchString, Database db)  throws SystemException, Bug
	{
		Integer count = 0;
		
		try 
		{
			count = SystemUserController.getController().getFilteredSystemUserCount(roleName, null, searchString, db);
		}
		catch( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}
		
		return count;		
	}

	public Integer getRoleSystemUserCount(String roleName, String searchString)  throws SystemException, Bug
	{
		Integer count = 0;
		
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			count = getRoleSystemUserCount(roleName, searchString, db);
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}
		
		return count;
	}

	// Get list of users accosiated with this role
	public List<SystemUserVO> getRoleSystemUserVOListInverted(Integer offset, Integer limit, String sortProperty, String direction, String searchString, String roleName, Database db)  throws SystemException, Bug
	{
		List<SystemUserVO> systemUsersVOList = new ArrayList();
		
		try 
		{
			systemUsersVOList = SystemUserController.getController().getFilteredSystemUserListInvertedOnRoleOrGroup(offset, limit, sortProperty, direction, searchString, roleName, null, db);
		}
		catch( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}
		
		return systemUsersVOList;		
	}

	public List<SystemUserVO> getRoleSystemUserVOListInverted(Integer offset, Integer limit, String sortProperty, String direction, String searchString, String roleName)  throws SystemException, Bug
	{
		List<SystemUserVO> systemUsersVOList = new ArrayList();
		
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			systemUsersVOList = getRoleSystemUserVOListInverted(offset, limit, sortProperty, direction, searchString, roleName, db);
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}		
		return systemUsersVOList;
	}
	
	public Integer getRoleSystemUserCountInverted(String roleName, String searchString, Database db)  throws SystemException, Bug
	{
		Integer count = 0;
		
		try 
		{
			count = SystemUserController.getController().getFilteredSystemUserCountInverted(roleName, null, searchString, db);
		}
		catch( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}
		
		return count;		
	}

	public Integer getRoleSystemUserCountInverted(String roleName, String searchString)  throws SystemException, Bug
	{
		Integer count = 0;
		
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			count = getRoleSystemUserCountInverted(roleName, searchString, db);
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}
		
		return count;
	}
/*
    public RoleVO update(RoleVO roleVO) throws ConstraintException, SystemException
    {
    	return (RoleVO) updateEntity(RoleImpl.class, (BaseEntityVO) roleVO);
    }        

    public RoleVO update(RoleVO roleVO, Database db) throws ConstraintException, SystemException
    {
    	return (RoleVO) updateEntity(RoleImpl.class, (BaseEntityVO) roleVO, db);
    }        
*/
    public RoleVO update(RoleVO roleVO, Set<String> userNamesSet) throws ConstraintException, SystemException, Exception
    {
    	if(userNamesSet != null)
    		removeUsers(roleVO.getRoleName());

    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        Role role = null;

        beginTransaction(db);

        try
        {
            //add validation here if needed
			role = update(roleVO, userNamesSet, db);

            //If any of the validations or setMethods reported an error, we throw them up now before create.
            ceb.throwIfNotEmpty();
            
            commitTransaction(db);
        }
        catch(ConstraintException ce)
        {
            logger.warn("An error occurred so we should not complete the transaction:" + ce, ce);
            rollbackTransaction(db);
            throw ce;
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return role.getValueObject();
    }        

    public Role update(RoleVO roleVO, Set<String> userNamesSet, Database db) throws ConstraintException, SystemException, Exception
    {
        Role role = getRoleWithName(roleVO.getRoleName(), db);
        
        if(userNamesSet != null)
    	{
	        for (String userName : userNamesSet)
	        {
				addUser(roleVO.getRoleName(), userName, db);
	        }
    	}
        
        role.setValueObject(roleVO);

        return role;
    }        

    
	/**
	 * This method gets a list of Roles for a particular systemUser.
	 * @param systemUserId
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public List<RoleVO> getRoleVOList(String userName) throws SystemException, Bug
	{
		List<RoleVO> roleVOList = null;
		
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			roleVOList = getRoleVOList(userName, db);
				
			commitTransaction(db);
		}
		catch(Exception e)
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}		
		
		return roleVOList;
	}
	
	/**
	 * This method gets a list of Roles for a particular systemUser.
	 * @param systemUserId
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	/*
	public Collection getRoleList(String userName, Database db)  throws SystemException, Bug
	{
	    Collection roleList = null;
		
		SystemUser systemUser = SystemUserController.getController().getSystemUserWithName(userName, db);
		roleList = systemUser.getRoles();
		
		return roleList;
	}
	*/
	
	/**
	 * 	Get the the roles for a 
	 */
	 
	public List<RoleVO> getRoleVOList(String userName, Database db)  throws SystemException, Bug
	{
		List<RoleVO> roleVOList = new ArrayList<RoleVO>();
		
        OQLQuery oql;
        try
        {										
        	oql = db.getOQLQuery( "CALL SQL SELECT r.roleName, r.description, r.source, r.isActive, r.modifiedDateTime FROM cmRole r, cmSystemUserRole sur WHERE r.roleName = sur.roleName AND sur.userName = $1 AS org.infoglue.cms.entities.management.impl.simple.RoleImpl");
        	oql.bind(userName);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			while(results.hasMore()) 
            {
            	Role role = (Role)results.next();
            	roleVOList.add(role.getValueObject());
            }
			
			results.close();
			oql.close();
        }
        catch(Exception e)
        {
            throw new SystemException("An error occurred when we tried to fetch roleVOList for " + userName + " Reason:" + e.getMessage(), e);    
        }    

		return roleVOList;		
	}

	
    public void addUser(String roleName, String userName) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
            addUser(roleName, userName, db);

            //If any of the validations or setMethods reported an error, we throw them up now before create.
            ceb.throwIfNotEmpty();
            
            commitTransaction(db);
        }
        catch(ConstraintException ce)
        {
            logger.warn("An error occurred so we should not complete the transaction:" + ce, ce);
            rollbackTransaction(db);
            throw ce;
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }        

    public void addUser(String roleName, String userName, Database db) throws ConstraintException, SystemException, Exception
    {
    	if(userName != null)
		{
    		SystemUserRoleImpl sur = new SystemUserRoleImpl();
    		sur.setUserName(userName);
    		sur.setRoleName(roleName);
    		
    		db.create(sur);
		}
    }
    
	/**
	 * 	Get if the Role with the roleName exists
	 */
   
    public boolean roleExists(String roleName) throws SystemException, Bug
    {
        Database db = CastorDatabaseService.getDatabase();

        beginTransaction(db);

        boolean roleExists = false;
        
        try
        {
        	roleExists = roleExists(roleName, db);

            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
        return roleExists;
    }        
    
    public void removeUser(String roleName, String userName) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
        	removeUser(roleName, userName, db);

            //If any of the validations or setMethods reported an error, we throw them up now before create.
            ceb.throwIfNotEmpty();
            
            commitTransaction(db);
        }
        catch(ConstraintException ce)
        {
            logger.warn("An error occurred so we should not complete the transaction:" + ce, ce);
            rollbackTransaction(db);
            throw ce;
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }        

    public void removeUser(String roleName, String userName, Database db) throws ConstraintException, SystemException, Exception
    {
    	OQLQuery oql = db.getOQLQuery( "SELECT sur FROM org.infoglue.cms.entities.management.impl.simple.SystemUserRoleImpl sur WHERE sur.roleName = $1 AND sur.userName = $2");
    	oql.bind(roleName);
    	oql.bind(userName);
    	
    	QueryResults results = oql.execute();
		while (results.hasMore()) 
        {
			SystemUserRoleImpl sur = (SystemUserRoleImpl)results.nextElement();
			db.remove(sur);
        }
		
		results.close();
		oql.close();
    }

    public void removeUsers(String roleName) throws ConstraintException, SystemException, Exception
    {
    	Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
        try
        {
        	OQLQuery oql = db.getOQLQuery( "SELECT sur FROM org.infoglue.cms.entities.management.impl.simple.SystemUserRoleImpl sur WHERE sur.roleName = $1");
        	oql.bind(roleName);
        	
        	QueryResults results = oql.execute();
    		while (results.hasMore()) 
            {
    			SystemUserRoleImpl sur = (SystemUserRoleImpl)results.nextElement();
    			db.remove(sur);
            }
    		
    		results.close();
    		oql.close();

            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }

	/**
	 * 	Get if the Role with the roleName exists
	 */
	 
	public boolean roleExists(String roleName, Database db) throws SystemException, Bug
	{
		boolean roleExists = false;
		
        try
        {										
        	OQLQuery oql = db.getOQLQuery( "SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RoleImpl r WHERE r.roleName = $1");
        	oql.bind(roleName);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
            {
				roleExists = true;
            }
			
			results.close();
			oql.close();
        }
        catch(Exception e)
        {
        	throw new SystemException("An error occurred when we tried to fetch " + roleName + " Reason:" + e.getMessage(), e);  
        }    
        
        return roleExists;		
	}

	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new RoleVO();
	}

}
 