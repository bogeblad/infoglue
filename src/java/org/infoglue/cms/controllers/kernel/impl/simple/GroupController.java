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
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Group;
import org.infoglue.cms.entities.management.GroupVO;
import org.infoglue.cms.entities.management.SystemUser;
import org.infoglue.cms.entities.management.impl.simple.GroupImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallGroupImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.deliver.util.CacheController;

/**
 * GroupHelper.java
 * Created on 2002-aug-28 
 * @author Stefan Sik, ss@frovi.com 
 * 
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
	
    public Group getGroupWithId(Integer groupId, Database db) throws SystemException, Bug
    {
		return (Group) getObjectWithId(GroupImpl.class, groupId, db);
    }

	public Group getGroupWithName(String groupName, Database db) throws SystemException, Bug
	{
		return (Group)getObjectWithId(GroupImpl.class, groupName, db);
	}
    
    /*
    public static List getGroupVOList(Database db) throws SystemException, Bug
    {
        return getAllVOObjects(GroupImpl.class, db);
    }
	*/
	
    public GroupVO getGroupVOWithId(Integer groupId) throws SystemException, Bug
    {
		return (GroupVO) getVOWithId(SmallGroupImpl.class, groupId);
    }

	public GroupVO getGroupVOWithId(String groupName) throws SystemException, Bug
	{
		return (GroupVO) getVOWithId(SmallGroupImpl.class, groupName);
	}

	public GroupVO getGroupVOWithId(String groupName, Database db) throws SystemException, Bug
	{
		return (GroupVO) getVOWithId(SmallGroupImpl.class, groupName, db);
	}

    // Simple, without db
	/*
    public static Group getGroupWithId(Integer groupId) throws SystemException, Bug
    {
		return (Group) getObjectWithId(GroupImpl.class, groupId);
    }
    */
    
    public List getGroupVOList() throws SystemException, Bug
    {
        return getAllVOObjects(SmallGroupImpl.class, "groupName");
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
		    groupVOList = getAllVOObjects(SmallGroupImpl.class, "groupName", db);
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

    public void delete(GroupVO groupVO) throws ConstraintException, SystemException
    {
    	deleteEntity(GroupImpl.class, groupVO.getGroupName());
    }        

	public void delete(String groupName) throws ConstraintException, SystemException
	{
		deleteEntity(GroupImpl.class, groupName);
	}        

	public void delete(String groupName, Database db) throws ConstraintException, SystemException, Exception
	{
		deleteEntity(GroupImpl.class, groupName, db);
	}        

	// Get list of users accosiated with this group
	public List getGroupSystemUserVOList(String userName, Database db)  throws SystemException, Bug
	{
		Collection systemUsers = null;
		List systemUsersVO = new ArrayList();
		Group group = null;
		
		try 
		{
			group = getGroupWithName(userName, db);
			systemUsers = group.getSystemUsers();		
			
			Iterator it = systemUsers.iterator();
			while (it.hasNext())
			{
				SystemUser systemUser = (SystemUser) it.next();
				systemUsersVO.add(systemUser.getValueObject());
			}
		}
		catch( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of users in this group. Reason:" + e.getMessage(), e);			
		}
		
		return systemUsersVO;		
	}

	public List getGroupSystemUserVOList(String groupName)  throws SystemException, Bug
	{
		List systemUsersVO = null;
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			systemUsersVO = getGroupSystemUserVOList(groupName, db);
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this group. Reason:" + e.getMessage(), e);			
		}		
		return systemUsersVO;
	}

    public GroupVO update(GroupVO groupVO) throws ConstraintException, SystemException
    {
    	return (GroupVO) updateEntity(GroupImpl.class, (BaseEntityVO) groupVO);
    }        

    public GroupVO update(GroupVO groupVO, Database db) throws ConstraintException, SystemException
    {
    	return (GroupVO) updateEntity(GroupImpl.class, (BaseEntityVO) groupVO, db);
    }        


    public GroupVO update(GroupVO groupVO, String[] systemUsers) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        Group group = null;

        beginTransaction(db);

        try
        {
            //add validation here if needed
			
            group = update(groupVO, systemUsers, db);

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

    public Group update(GroupVO groupVO, String[] systemUsers, Database db) throws ConstraintException, SystemException
    {
		Group group = getGroupWithName(groupVO.getGroupName(), db);
		group.getSystemUsers().clear();
		
		if(systemUsers != null)
		{
			for (int i=0; i < systemUsers.length; i++)
            {
        		SystemUser systemUser = SystemUserController.getController().getSystemUserWithName(systemUsers[i], db);
        		
            	group.getSystemUsers().add(systemUser);
				systemUser.getGroups().add(group);
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
			
			//SystemUser systemUser = SystemUserController.getController().getSystemUserWithName(userName, db);
			//groupVOList = toVOList(systemUser.getGroups());
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
	 * This method gets a list of Groups for a particular systemUser.
	 * @param systemUserId
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public Collection getGroupList(String userName, Database db)  throws SystemException, Bug
	{
		Collection groupList = null;
		
		SystemUser systemUser = SystemUserController.getController().getSystemUserWithName(userName, db);
		groupList = systemUser.getGroups();
		
		return groupList;
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
        	oql = db.getOQLQuery( "CALL SQL SELECT r.groupName, r.description FROM cmGroup r, cmSystemUserGroup sur WHERE r.groupName = sur.groupName AND sur.userName = $1 AS org.infoglue.cms.entities.management.impl.simple.SmallGroupImpl");
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

        Group group = null;

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

    public void addUser(String groupName, String userName, Database db) throws ConstraintException, SystemException
    {
		Group group = getGroupWithName(groupName, db);
		
		if(userName != null)
		{
    		SystemUser systemUser = SystemUserController.getController().getSystemUserWithName(userName, db);
    		
        	group.getSystemUsers().add(systemUser);
			systemUser.getGroups().add(group);
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

    public void removeUser(String groupName, String userName, Database db) throws ConstraintException, SystemException
    {
		Group group = getGroupWithName(groupName, db);
		
		if(userName != null)
		{
			SystemUser systemUser = null;
			Iterator systemUsersIterator = group.getSystemUsers().iterator();
			while(systemUsersIterator.hasNext())
			{
				systemUser = (SystemUser)systemUsersIterator.next();
	        	if(systemUser.getUserName().equals(userName))
	        		break;
			}
			
			if(systemUser != null)
			{
				group.getSystemUsers().remove(systemUser);
				systemUser.getGroups().remove(group);
			}
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
        	OQLQuery oql = db.getOQLQuery( "SELECT g FROM org.infoglue.cms.entities.management.impl.simple.SmallGroupImpl g WHERE g.groupName = $1");
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
 