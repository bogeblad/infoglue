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
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Role;
import org.infoglue.cms.entities.management.RoleVO;
import org.infoglue.cms.entities.management.SystemUser;
import org.infoglue.cms.entities.management.impl.simple.RoleImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallRoleImpl;
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
    
    /*
    public static List getRoleVOList(Database db) throws SystemException, Bug
    {
        return getAllVOObjects(RoleImpl.class, db);
    }
	*/
	
    public RoleVO getRoleVOWithId(Integer roleId) throws SystemException, Bug
    {
		return (RoleVO) getVOWithId(SmallRoleImpl.class, roleId);
    }

	public RoleVO getRoleVOWithId(String roleName) throws SystemException, Bug
	{
		return (RoleVO) getVOWithId(SmallRoleImpl.class, roleName);
	}

	public RoleVO getRoleVOWithId(String roleName, Database db) throws SystemException, Bug
	{
		return (RoleVO) getVOWithId(SmallRoleImpl.class, roleName, db);
	}

    // Simple, without db
	/*
    public static Role getRoleWithId(Integer roleId) throws SystemException, Bug
    {
		return (Role) getObjectWithId(RoleImpl.class, roleId);
    }
    */
    
    public List getRoleVOList() throws SystemException, Bug
    {
        return getAllVOObjects(SmallRoleImpl.class, "roleName");
    }

    public List getRoleVOList(Database db) throws SystemException, Bug
    {
        return getAllVOObjects(SmallRoleImpl.class, "roleName", db);
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

    public void delete(RoleVO roleVO) throws ConstraintException, SystemException
    {
    	deleteEntity(RoleImpl.class, roleVO.getRoleName());
    }        

    public void delete(RoleVO roleVO, Database db) throws ConstraintException, SystemException, Exception
    {
    	deleteEntity(RoleImpl.class, roleVO.getRoleName(), db);
    }        

	public void delete(String roleName) throws ConstraintException, SystemException
	{
		deleteEntity(RoleImpl.class, roleName);
	}        

	public void delete(String roleName, Database db) throws ConstraintException, SystemException, Exception
	{
		deleteEntity(RoleImpl.class, roleName, db);
	}        

	// Get list of users accosiated with this role
	public List getRoleSystemUserVOList(String userName, Database db)  throws SystemException, Bug
	{
		Collection systemUsers = null;
		List systemUsersVO = new ArrayList();
		Role role = null;
		
		try 
		{
			role = getRoleWithName(userName, db);
			systemUsers = role.getSystemUsers();		
			
			Iterator it = systemUsers.iterator();
			while (it.hasNext())
			{
				SystemUser systemUser = (SystemUser) it.next();
				systemUsersVO.add(systemUser.getValueObject());
			}
		}
		catch( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}
		
		return systemUsersVO;		
	}

	public List getRoleSystemUserVOList(String roleName)  throws SystemException, Bug
	{
		List systemUsersVO = null;
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			systemUsersVO = getRoleSystemUserVOList(roleName, db);
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}		
		return systemUsersVO;
	}

    public RoleVO update(RoleVO roleVO) throws ConstraintException, SystemException
    {
    	return (RoleVO) updateEntity(RoleImpl.class, (BaseEntityVO) roleVO);
    }        


    public RoleVO update(RoleVO roleVO, String[] systemUsers) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        Role role = null;

        beginTransaction(db);

        try
        {
            //add validation here if needed
			role = update(roleVO, systemUsers, db);

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

    public Role update(RoleVO roleVO, String[] systemUsers, Database db) throws ConstraintException, SystemException
    {
        Role role = getRoleWithName(roleVO.getRoleName(), db);
		role.getSystemUsers().clear();
		
		if(systemUsers != null)
		{
			for (int i=0; i < systemUsers.length; i++)
            {
        		SystemUser systemUser = SystemUserController.getController().getSystemUserWithName(systemUsers[i], db);
        		
            	role.getSystemUsers().add(systemUser);
				systemUser.getRoles().add(role);
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
			
			//SystemUser systemUser = SystemUserController.getController().getSystemUserWithName(userName, db);
			//roleVOList = toVOList(systemUser.getRoles());
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
	
	public Collection getRoleList(String userName, Database db)  throws SystemException, Bug
	{
	    Collection roleList = null;
		
		SystemUser systemUser = SystemUserController.getController().getSystemUserWithName(userName, db);
		roleList = systemUser.getRoles();
		
		return roleList;
	}

	/**
	 * 	Get the the roles for a 
	 */
	 
	public List<RoleVO> getRoleVOList(String userName, Database db)  throws SystemException, Bug
	{
		List<RoleVO> roleVOList = new ArrayList<RoleVO>();
		
        OQLQuery oql;
        try
        {										
        	oql = db.getOQLQuery( "CALL SQL SELECT r.roleName, r.description FROM cmRole r, cmSystemUserRole sur WHERE r.roleName = sur.roleName AND sur.userName = $1 AS org.infoglue.cms.entities.management.impl.simple.SmallRoleImpl");
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

    public void addUser(String roleName, String userName, Database db) throws ConstraintException, SystemException
    {
		Role role = getRoleWithName(roleName, db);
		
		if(userName != null)
		{
    		SystemUser systemUser = SystemUserController.getController().getSystemUserWithName(userName, db);
    		
        	role.getSystemUsers().add(systemUser);
			systemUser.getRoles().add(role);
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

    public void removeUser(String roleName, String userName, Database db) throws ConstraintException, SystemException
    {
		Role role = getRoleWithName(roleName, db);
		
		if(userName != null)
		{
			SystemUser systemUser = null;
			Iterator systemUsersIterator = role.getSystemUsers().iterator();
			while(systemUsersIterator.hasNext())
			{
				systemUser = (SystemUser)systemUsersIterator.next();
	        	if(systemUser.getUserName().equals(userName))
	        		break;
			}
			
			if(systemUser != null)
			{
				role.getSystemUsers().remove(systemUser);
				systemUser.getRoles().remove(role);
			}
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
        	OQLQuery oql = db.getOQLQuery( "SELECT r FROM org.infoglue.cms.entities.management.impl.simple.SmallRoleImpl r WHERE r.roleName = $1");
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
 