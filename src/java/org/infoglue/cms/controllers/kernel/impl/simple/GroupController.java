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
import org.infoglue.cms.entities.management.Group;
import org.infoglue.cms.entities.management.GroupVO;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.entities.management.impl.simple.GroupImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserGroupImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.deliver.util.CacheController;

/**
 * GroupHelper.java
 * Created on 2002-aug-28 
 * @author Stefan Sik, ss@frovi.com 
 * @author Mattias Bogeblad
 * This class is a helper class for the use case handle groups
 */
public class GroupController extends BaseController
{
    private final static Logger logger = Logger.getLogger(GroupController.class.getName());

	/**
	 * Factory method
	 */

	public static GroupController getController()
	{
		return new GroupController();
	}
	
	public Group getGroupWithId(String groupName, Database db) throws SystemException, Bug
	{
		return (Group)getObjectWithId(GroupImpl.class, groupName, db);
	}
	
    public GroupVO getGroupVOWithId(Integer groupId) throws SystemException, Bug
    {
		return (GroupVO) getVOWithId(GroupImpl.class, groupId);
    }

	public GroupVO getGroupVOWithId(String groupName) throws SystemException, Bug
	{
		return (GroupVO) getVOWithId(GroupImpl.class, groupName);
	}

	public GroupVO getGroupVOWithId(String groupName, Database db) throws SystemException, Bug
	{
		return (GroupVO) getVOWithId(GroupImpl.class, groupName, db);
	}
    
    public List getGroupVOList() throws SystemException, Bug
    {
        return getAllVOObjects(GroupImpl.class, "groupName");
    }

    public List getGroupVOList(Database db) throws SystemException, Bug
    {
		String cacheKey = "allGroupVO";
		logger.info("cacheKey in getGroupVOList:" + cacheKey);
		List groupVOList = (List)CacheController.getCachedObject("groupVOListCache", cacheKey);
		if(groupVOList != null)
		{
			logger.info("There was an cached list of GroupVO:" + groupVOList.size());
		}
		else
		{
		    groupVOList = getAllVOObjects(GroupImpl.class, "groupName", db);
			if(groupVOList != null)
			    CacheController.cacheObject("groupVOListCache", cacheKey, groupVOList);
		}
		
		return groupVOList;
	}

    public GroupVO create(GroupVO groupVO) throws ConstraintException, SystemException
    {
        Group group = new GroupImpl();
        group.setValueObject(groupVO);
        group = (Group) createEntity(group);
        return group.getValueObject();
    }     

    public Group create(GroupVO groupVO, Database db) throws ConstraintException, SystemException, Exception
    {
        Group group = new GroupImpl();
        group.setValueObject(groupVO);
        group = (Group) createEntity(group, db);
        return group;
    }     

    public void delete(GroupVO groupVO) throws ConstraintException, SystemException, Exception
    {
    	removeUsers(groupVO.getGroupName());
    	deleteEntity(GroupImpl.class, groupVO.getGroupName());
    }        

	public void delete(String groupName) throws ConstraintException, SystemException, Exception
	{
    	removeUsers(groupName);
		deleteEntity(GroupImpl.class, groupName);
	}        

	public void delete(String groupName, Database db) throws ConstraintException, SystemException, Exception
	{
    	removeUsers(groupName);
		deleteEntity(GroupImpl.class, groupName, db);
	}        

	
	// Get list of users accosiated with this group
	public List<SystemUserVO> getGroupSystemUserVOList(Integer offset, Integer limit, String sortProperty, String direction, String searchString, String groupName, Database db)  throws SystemException, Bug
	{
		List<SystemUserVO> systemUsersVOList = new ArrayList();
		
		try 
		{
			systemUsersVOList = SystemUserController.getController().getFilteredSystemUserList(offset, limit, sortProperty, direction, searchString, null, groupName, db);
		}
		catch( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of users in this group. Reason:" + e.getMessage(), e);			
		}
		
		return systemUsersVOList;		
	}

	public List getGroupSystemUserVOList(Integer offset, Integer limit, String sortProperty, String direction, String searchString, String groupName)  throws SystemException, Bug
	{
		List<SystemUserVO> systemUsersVOList = new ArrayList();
		
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			systemUsersVOList = getGroupSystemUserVOList(offset, limit, sortProperty, direction, searchString, groupName, db);
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this group. Reason:" + e.getMessage(), e);			
		}		
		return systemUsersVOList;
	}
	
	public Integer getGroupSystemUserCount(String groupName, String searchString, Database db)  throws SystemException, Bug
	{
		Integer count = 0;
		
		try 
		{
			count = SystemUserController.getController().getFilteredSystemUserCount(null, groupName, searchString, db);
		}
		catch( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of users in this group. Reason:" + e.getMessage(), e);			
		}
		
		return count;		
	}

	public Integer getGroupSystemUserCount(String groupName, String searchString)  throws SystemException, Bug
	{
		Integer count = 0;
		
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			count = getGroupSystemUserCount(groupName, searchString, db);
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this group. Reason:" + e.getMessage(), e);			
		}
		
		return count;
	}
	
	// Get list of users accosiated with this group
	public List<SystemUserVO> getGroupSystemUserVOListInverted(Integer offset, Integer limit, String sortProperty, String direction, String searchString, String groupName, Database db)  throws SystemException, Bug
	{
		List<SystemUserVO> systemUsersVOList = new ArrayList();
		
		try 
		{
			systemUsersVOList = SystemUserController.getController().getFilteredSystemUserListInvertedOnRoleOrGroup(offset, limit, sortProperty, direction, searchString, null, groupName, db);
		}
		catch( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of users in this group. Reason:" + e.getMessage(), e);			
		}
		
		return systemUsersVOList;		
	}

	public List<SystemUserVO> getGroupSystemUserVOListInverted(Integer offset, Integer limit, String sortProperty, String direction, String searchString, String groupName)  throws SystemException, Bug
	{
		List<SystemUserVO> systemUsersVOList = new ArrayList();
		
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			systemUsersVOList = getGroupSystemUserVOListInverted(offset, limit, sortProperty, direction, searchString, groupName, db);
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this group. Reason:" + e.getMessage(), e);			
		}		
		return systemUsersVOList;
	}
	
	public Integer getGroupSystemUserCountInverted(String groupName, String searchString, Database db)  throws SystemException, Bug
	{
		Integer count = 0;
		
		try 
		{
			count = SystemUserController.getController().getFilteredSystemUserCountInverted(null, groupName, searchString, db);
		}
		catch( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of users in this group. Reason:" + e.getMessage(), e);			
		}
		
		return count;		
	}

	public Integer getGroupSystemUserCountInverted(String groupName, String searchString)  throws SystemException, Bug
	{
		Integer count = 0;
		
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			count = getGroupSystemUserCountInverted(groupName, searchString, db);
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this group. Reason:" + e.getMessage(), e);			
		}
		
		return count;
	}
	
    public GroupVO update(GroupVO groupVO, Set<String> userNamesSet) throws ConstraintException, SystemException, Exception
    {
    	if(userNamesSet != null)
    		removeUsers(groupVO.getGroupName());
        
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        Group group = null;

        beginTransaction(db);

        try
        {
            //add validation here if needed
			group = update(groupVO, userNamesSet, db);

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

        return group.getValueObject();
    }        

    public Group update(GroupVO groupVO, Set<String> userNamesSet, Database db) throws ConstraintException, SystemException, Exception
    {
    	Group group = getGroupWithId(groupVO.getGroupName(), db);
    	
    	if(userNamesSet != null)
    	{
	        for (String userName : userNamesSet)
	        {
				addUser(groupVO.getGroupName(), userName, db);
	        }
    	}
    	
        group.setValueObject(groupVO);

        return group;
    }        
	
	/**
	 * This method gets a list of Groups for a particular systemUser.
	 * @param systemUserId
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public List<GroupVO> getGroupVOList(String userName)  throws SystemException, Bug
	{
		List<GroupVO> groupVOList = null;
		
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			groupVOList = getGroupVOList(userName, db);
			
			commitTransaction(db);
		}
		catch(Exception e)
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this group. Reason:" + e.getMessage(), e);			
		}		
		
		return groupVOList;
	}
	
	
	/**
	 * 	Get the the groups for a user (very light)
	 */
	 
	public List<GroupVO> getGroupVOList(String userName, Database db)  throws SystemException, Bug
	{
		List<GroupVO> groupVOList = new ArrayList<GroupVO>();
		
        OQLQuery oql;
        try
        {										
        	oql = db.getOQLQuery( "CALL SQL SELECT r.groupName, r.description, r.source, r.groupType, r.isActive, r.modifiedDateTime FROM cmGroup r, cmSystemUserGroup sur WHERE r.groupName = sur.groupName AND sur.userName = $1 AS org.infoglue.cms.entities.management.impl.simple.GroupImpl");
        	oql.bind(userName);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			while(results.hasMore()) 
            {
            	Group group = (Group)results.next();
            	groupVOList.add(group.getValueObject());
            }
			
			results.close();
			oql.close();
        }
        catch(Exception e)
        {
            throw new SystemException("An error occurred when we tried to fetch groupVOList for " + userName + " Reason:" + e.getMessage(), e);    
        }    

		return groupVOList;		
	}

    public void addUser(String groupName, String userName) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
            addUser(groupName, userName, db);

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

    public void addUser(String groupName, String userName, Database db) throws ConstraintException, SystemException, Exception
    {
    	if(groupName != null && userName != null)
		{
    		SystemUserGroupImpl sug = new SystemUserGroupImpl();
    		sug.setUserName(userName);
    		sug.setGroupName(groupName);
    		
    		db.create(sug);
		}
    }

    public void removeUser(String groupName, String userName) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
        	removeUser(groupName, userName, db);

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

    public void removeUser(String groupName, String userName, Database db) throws ConstraintException, SystemException, Exception
    {
    	OQLQuery oql = db.getOQLQuery( "SELECT sur FROM org.infoglue.cms.entities.management.impl.simple.SystemUserGroupImpl sur WHERE sur.groupName = $1 AND sur.userName = $2");
    	oql.bind(groupName);
    	oql.bind(userName);
    	
    	QueryResults results = oql.execute();
		while (results.hasMore()) 
        {
			SystemUserGroupImpl sur = (SystemUserGroupImpl)results.nextElement();
			db.remove(sur);
        }
		
		results.close();
		oql.close();
    }

    public void removeUsers(String groupName) throws ConstraintException, SystemException, Exception
    {
    	Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
        try
        {
        	OQLQuery oql = db.getOQLQuery( "SELECT sur FROM org.infoglue.cms.entities.management.impl.simple.SystemUserGroupImpl sur WHERE sur.groupName = $1");
        	oql.bind(groupName);
        	
        	QueryResults results = oql.execute();
    		while (results.hasMore()) 
            {
    			SystemUserGroupImpl sur = (SystemUserGroupImpl)results.nextElement();
    			logger.info("Deleting " + sur.getUserName() + "/" + sur.getGroupName());
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
	 * 	Get if the Group with the groupName exists
	 */

    public boolean groupExists(String groupName) throws SystemException, Bug
    {
        Database db = CastorDatabaseService.getDatabase();

        beginTransaction(db);

        boolean groupExists = false;
        
        try
        {
        	groupExists = groupExists(groupName, db);

            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
        return groupExists;
    }        

	/**
	 * 	Get if the Group with the groupName exists
	 */
	 
	public boolean groupExists(String groupName, Database db) throws SystemException, Bug
	{
		boolean groupExists = false;
		
        try
        {										
        	OQLQuery oql = db.getOQLQuery( "SELECT g FROM org.infoglue.cms.entities.management.impl.simple.GroupImpl g WHERE g.groupName = $1");
        	oql.bind(groupName);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
            {
				groupExists = true;
            }
			
			results.close();
			oql.close();
        }
        catch(Exception e)
        {
        	throw new SystemException("An error occurred when we tried to fetch " + groupExists + " Reason:" + e.getMessage(), e);  
        }    
        
        return groupExists;		
	}

	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new GroupVO();
	}

}
 