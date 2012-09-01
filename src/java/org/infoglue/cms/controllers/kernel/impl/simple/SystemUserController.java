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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.QueryResults;
import org.exolab.castor.mapping.AccessMode;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Group;
import org.infoglue.cms.entities.management.Role;
import org.infoglue.cms.entities.management.RoleVO;
import org.infoglue.cms.entities.management.SystemUser;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.entities.management.TableCount;
import org.infoglue.cms.entities.management.impl.simple.SystemUserGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserRoleImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.PasswordGenerator;
import org.infoglue.cms.util.mail.MailServiceFactory;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.Timer;

/**
 * SystemUserController.java
 * Created on 2002-aug-28 
 * @author Stefan Sik, ss@frovi.com 
 * 
 * This class is a helper class for the use case handle SystemUsers
 * 
 */
public class SystemUserController extends BaseController
{
    private final static Logger logger = Logger.getLogger(SystemUserController.class.getName());

	/**
	 * Factory method
	 */

	public static SystemUserController getController()
	{
		return new SystemUserController();
	}
	
	/*
    public static SystemUser getSystemUserWithId(Integer systemUserId, Database db) throws SystemException, Bug
    {
		return (SystemUser) getObjectWithId(SystemUserImpl.class, systemUserId, db);
    }
    
    public SystemUserVO getSystemUserVOWithId(Integer systemUserId) throws SystemException, Bug
    {
		return (SystemUserVO) getVOWithId(SystemUserImpl.class, systemUserId);
    }
	*/

	public SystemUserVO getSystemUserVOWithName(String name)  throws SystemException, Bug
	{
		SystemUserVO systemUserVO = null;
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);

			systemUserVO = getReadOnlySystemUserVOWithName(name, db);

			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return systemUserVO;		
	}	
	
	
	/**
	 * 	Get the SystemUser with the userName
	 */
	 
	public SystemUserVO getReadOnlySystemUserVOWithName(String userName, Database db)  throws SystemException, Bug
	{
		SystemUserVO systemUserVO = null;
		
        try
        {										
        	OQLQuery oql = db.getOQLQuery("SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u WHERE u.userName = $1");
        	oql.bind(userName);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
            {
				SystemUser systemUser = (SystemUser)results.next();
				systemUserVO = systemUser.getValueObject();
            }
			
			results.close();
			oql.close();
        }
        catch(Exception e)
        {
            throw new SystemException("An error occurred when we tried to fetch " + userName + " Reason:" + e.getMessage(), e);    
        }    

		return systemUserVO;		
	}

	
	/**
	 * 	Get the SystemUser with the userName
	 */
	 
	public SystemUser getReadOnlySystemUserWithName(String userName, Database db)  throws SystemException, Bug
	{
		SystemUser systemUser = null;
        OQLQuery	oql;
        try
        {										
        	oql = db.getOQLQuery( "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u WHERE u.userName = $1");
        	oql.bind(userName);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
            {
            	systemUser = (SystemUser)results.next();
            	logger.info("found one:" + systemUser.getFirstName());
            }
			results.close();
			oql.close();
        }
        catch(Exception e)
        {
            throw new SystemException("An error occurred when we tried to fetch " + userName + " Reason:" + e.getMessage(), e);    
        }    

		return systemUser;		
	}

	/**
	 * 	Get if the SystemUser with the userName exists
	 */
	 
	public boolean systemUserExists(String userName, Database db) throws SystemException, Bug
	{
		boolean systemUserExists = false;
		
        try
        {										
        	OQLQuery oql = db.getOQLQuery( "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u WHERE u.userName = $1");
        	oql.bind(userName);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
            {
				systemUserExists = true;
            }
			
			results.close();
			oql.close();
        }
        catch(Exception e)
        {
        	throw new SystemException("An error occurred when we tried to fetch " + userName + " Reason:" + e.getMessage(), e);  
        }    
        
        return systemUserExists;		
	}


	/**
	 * 	Get the SystemUser with the userName
	 */
	 /*
	public SystemUser getSystemUserWithName(String userName, Database db)  throws SystemException, Bug
	{
		SystemUser systemUser = null;
        OQLQuery oql;
        try
        {					
        	oql = db.getOQLQuery( "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u WHERE u.userName = $1");
        	oql.bind(userName);
        	
        	QueryResults results = oql.execute();
    		this.logger.info("Fetching entity in read/write mode" + userName);

			if (results.hasMore()) 
            {
            	systemUser = (SystemUser)results.next();
            	logger.info("found one:" + systemUser.getFirstName());
            }
			
			results.close();
			oql.close();
        }
        catch(Exception e)
        {
            throw new SystemException("An error occurred when we tried to fetch " + userName + " Reason:" + e.getMessage(), e);    
        }    

		return systemUser;		
	}*/


	/**
	 * 	Get the SystemUser with the userName
	 */
	 
	public SystemUserImpl getSystemUserWithName(String userName, Database db)  throws SystemException, Bug, Exception
	{
		SystemUserImpl systemUser = (SystemUserImpl)db.load(org.infoglue.cms.entities.management.impl.simple.SystemUserImpl.class, userName);
    	//logger.warn("found one systemUser:" + systemUser.getFirstName());
    	
		return systemUser;		
	}

	public SystemUserVO getSystemUserVO(String userName, String password)  throws SystemException, Bug
	{
		SystemUserVO systemUserVO = null;

		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);

			systemUserVO = getSystemUserVO(db, userName, password);

			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return systemUserVO;		
	}	


	public SystemUserVO getSystemUserVO(Database db, String userName, String password)  throws SystemException, Exception
	{
		SystemUserVO systemUserVO = null;

		OQLQuery oql = db.getOQLQuery( "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u WHERE u.userName = $1 AND u.password = $2");
		oql.bind(userName);
		oql.bind(password);
    	
		QueryResults results = oql.execute(Database.ReadOnly);
		
		if (results.hasMore()) 
		{
			SystemUser systemUser = (SystemUser)results.next();
			systemUserVO = systemUser.getValueObject();
		}

		results.close();
		oql.close();

		return systemUserVO;		
	}	
    
	public SystemUser getSystemUser(Database db, String userName, String password)  throws SystemException, Exception
	{
		SystemUser systemUser = null;
		
		OQLQuery oql = db.getOQLQuery( "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u WHERE u.userName = $1 AND u.password = $2");
		oql.bind(userName);
		oql.bind(password);
    	
		QueryResults results = oql.execute();
		this.logger.info("Fetching entity in read/write mode" + userName);

		if (results.hasMore()) 
		{
			systemUser = (SystemUser)results.next();
			logger.info("found one:" + systemUser.getFirstName());
		}

		results.close();
		oql.close();

		return systemUser;		
	}	
	
    public List getSystemUserVOList() throws SystemException, Bug
    {
        return getAllVOObjects(SystemUserImpl.class, "userName");
    }

    public List getSystemUserVOList(Database db) throws SystemException, Bug
    {
        return getAllVOObjects(SystemUserImpl.class, "userName", db);
    }

    public List getSystemUserList(Database db) throws SystemException, Bug
    {
        return getAllObjects(SystemUserImpl.class, "userName", db);
    }
    
	public List getFilteredSystemUserVOList(Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws SystemException, Bug
	{
		List filteredList = new ArrayList();
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
								
			filteredList = getFilteredSystemUserList(offset, limit, sortProperty, direction, searchString, db);
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException("An error occurred so we should not complete the transaction:" + e, e);
		}
		
		return toVOList(filteredList);
	}

	public List<SystemUserVO> getSystemUserVOListWithPassword(String password, Database db) throws SystemException, Bug, Exception
	{
		List<SystemUserVO> filteredVOList = new ArrayList<SystemUserVO>();
		
		OQLQuery oql = db.getOQLQuery( "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u WHERE u.password = $1 ORDER BY u.userName");
    	oql.bind(password);
    	
		QueryResults results = oql.execute(Database.ReadOnly);
		
		while (results.hasMore()) 
		{
			SystemUser extranetUser = (SystemUser)results.next();
			filteredVOList.add(extranetUser.getValueObject());
		}
		
		results.close();
		oql.close();

		return filteredVOList;
	}

	public List getFilteredSystemUserList(Integer offset, Integer limit, String sortProperty, String direction, String searchString, Database db) throws SystemException, Bug, Exception
	{
		List filteredList = new ArrayList();
		
		if(sortProperty == null || sortProperty.equals(""))
			sortProperty = "userName";
		if(direction == null || direction.equals(""))
			direction = "ASC";
		
		OQLQuery oql = null;
		if(searchString != null && !searchString.equals(""))
		{
			if(CmsPropertyHandler.getDatabaseEngine().equalsIgnoreCase("sql-server"))
			{
				if(offset != null || limit != null)
				{
					String oqlString = "CALL SQL With cmSystemUserCust AS ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime, ROW_NUMBER() OVER (order by " + sortProperty + " " + direction + ") as RowNumber FROM cmSystemUser WHERE firstName LIKE $1 OR lastName LIKE $2 OR userName LIKE $3 OR email LIKE $4 OR source LIKE $5) select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from cmSystemUserCust Where RowNumber > " + offset + " and RowNumber <= " + (offset + limit) + " AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
				}
				else
				{
					String oqlBase = "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u";
					String orderStatement = " ORDER BY u." + sortProperty + " " + direction;
					oql = db.getOQLQuery( oqlBase + orderStatement);
				}
			}
			else if(CmsPropertyHandler.getDatabaseEngine().equalsIgnoreCase("oracle"))
			{
				if(offset != null || limit != null)
				{
					String oqlString = "CALL SQL SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM ( SELECT su.userName,su.password,su.firstName,su.lastName,su.email,su.source,su.isActive,su.modifiedDateTime, ROWNUM rnum FROM ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM cmSystemUser WHERE firstName LIKE $1 OR lastName LIKE $2 OR userName LIKE $3 OR email LIKE $4 OR source LIKE $5 ORDER BY " + sortProperty + " " + direction + ", userName ) su  WHERE ROWNUM <= " + (offset + limit) + " ) WHERE rnum > " + offset + " AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
				}
				else
				{
					String oqlBase = "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u";
					String orderStatement = " ORDER BY u." + sortProperty + " " + direction;
					oql = db.getOQLQuery( oqlBase + orderStatement);
				}
			}
			else
			{
				String oqlBase = "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u";
				String searchQuery = " WHERE u.firstName LIKE $1 OR u.lastName LIKE $2 OR u.userName LIKE $3 OR u.email LIKE $4 OR u.source LIKE $5";
				String orderStatement = " ORDER BY u." + sortProperty + " " + direction;
				String pagination = " LIMIT $6 OFFSET $7";
				
				if(offset != null || limit != null)
				{
					String oqlString = oqlBase + searchQuery + orderStatement + pagination;
	
					oql = db.getOQLQuery(oqlString);
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind(limit);
					oql.bind(offset);
				}
				else
				{
					oql = db.getOQLQuery( oqlBase + searchQuery + orderStatement);
					oql.bind("%" + searchString + "%");				
					oql.bind("%" + searchString + "%");				
					oql.bind("%" + searchString + "%");				
					oql.bind("%" + searchString + "%");				
					oql.bind("%" + searchString + "%");				
				}
			}
		}
		else
		{
			if(CmsPropertyHandler.getDatabaseEngine().equalsIgnoreCase("sql-server"))
			{
				if(offset != null || limit != null)
				{
					String oqlString = "CALL SQL With cmSystemUserCust AS ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime, ROW_NUMBER() OVER (order by " + sortProperty + " " + direction + ") as RowNumber  FROM cmSystemUser ) select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from cmSystemUserCust Where RowNumber > " + offset + " and RowNumber <= " + (offset + limit) + " AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
				}
				else
				{
					String oqlBase = "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u";
					String orderStatement = " ORDER BY u." + sortProperty + " " + direction;
					oql = db.getOQLQuery( oqlBase + orderStatement);
				}
			}
			else if(CmsPropertyHandler.getDatabaseEngine().equalsIgnoreCase("oracle"))
			{
				if(offset != null || limit != null)
				{
					String oqlString = "CALL SQL SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM ( SELECT su.userName,su.password,su.firstName,su.lastName,su.email,su.source,su.isActive,su.modifiedDateTime, ROWNUM rnum FROM ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM cmSystemUser ORDER BY " + sortProperty + " " + direction + ", userName ) su  WHERE ROWNUM <= " + (offset + limit) + " ) WHERE rnum > " + offset + " AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
				}
				else
				{
					String oqlBase = "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u";
					String orderStatement = " ORDER BY u." + sortProperty + " " + direction;
					oql = db.getOQLQuery( oqlBase + orderStatement);
				}
			}
			else
			{
				String oqlBase = "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u";
				String orderStatement = " ORDER BY u." + sortProperty + " " + direction;
				String pagination = " LIMIT $1 OFFSET $2";
				
				if(offset != null || limit != null)
				{
					String oqlString = oqlBase + orderStatement + pagination;
					oql = db.getOQLQuery(oqlString);
					oql.bind(limit);
					oql.bind(offset);
				}
				else
				{
					oql = db.getOQLQuery( oqlBase + orderStatement);
				}
			}
		}
		
		QueryResults results = oql.execute(Database.ReadOnly);

		while (results.hasMore()) 
		{
			SystemUser extranetUser = (SystemUser)results.next();
			filteredList.add(extranetUser);
		}
		
		results.close();
		oql.close();

		return filteredList;
	}
	
	public List getFilteredSystemUserList(Integer offset, Integer limit, String sortProperty, String direction, String searchString, String roleName, String groupName, Database db) throws SystemException, Bug, Exception
	{
		List filteredList = new ArrayList();
		
		if(sortProperty == null || sortProperty.equals(""))
			sortProperty = "userName";
		if(direction == null || direction.equals(""))
			direction = "ASC";
		
		String connectionTableName = "cmSystemUserRole";
		if(roleName == null)
			connectionTableName = "cmSystemUserGroup";

		String foreignKey = "roleName";
		String foreignKeyValue = roleName;
		if(roleName == null)
		{
			foreignKey = "groupName";
			foreignKeyValue = groupName;
		}
		
		OQLQuery oql = null;
		if(searchString != null && !searchString.equals(""))
		{
			if(CmsPropertyHandler.getDatabaseEngine().equalsIgnoreCase("sql-server"))
			{
				if(offset != null || limit != null)
				{
					String oqlString = "CALL SQL With cmSystemUserCust AS ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime, ROW_NUMBER() OVER (order by " + sortProperty + " " + direction + ") as RowNumber  FROM cmSystemUser su where su.userName IN (select userName from " + connectionTableName + " where " + foreignKey + " = $1) AND (firstName LIKE $2 OR lastName LIKE $3 OR userName LIKE $4 OR email LIKE $5 OR source LIKE $6)) select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from cmSystemUserCust Where RowNumber > " + offset + " and RowNumber <= " + (offset + limit) + " AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
				}
				else
				{
					String oqlString = "CALL SQL With cmSystemUserCust AS ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime, ROW_NUMBER() OVER (order by " + sortProperty + " " + direction + ") as RowNumber  FROM cmSystemUser su where su.userName IN (select userName from " + connectionTableName + " where " + foreignKey + " = $1) AND (firstName LIKE $2 OR lastName LIKE $3 OR userName LIKE $4 OR email LIKE $5 OR source LIKE $6)) select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from cmSystemUserCust AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
				}
			}
			else if(CmsPropertyHandler.getDatabaseEngine().equalsIgnoreCase("oracle"))
			{
				if(offset != null || limit != null)
				{
					String oqlString = "CALL SQL SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM ( SELECT su.userName,su.password,su.firstName,su.lastName,su.email,su.source,su.isActive,su.modifiedDateTime, ROWNUM rnum FROM ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM cmSystemUser su where userName IN (SELECT userName from " + connectionTableName + " where " + foreignKey + " = $1) AND (firstName LIKE $2 OR lastName LIKE $3 OR userName LIKE $4 OR email LIKE $5 OR source LIKE $6) ORDER BY " + sortProperty + " " + direction + ") su  WHERE ROWNUM <= " + (offset + limit) + " ) WHERE rnum > " + offset + " AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
				}
				else
				{
					String oqlString = "CALL SQL SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM ( SELECT su.userName,su.password,su.firstName,su.lastName,su.email,su.source,su.isActive,su.modifiedDateTime, ROWNUM rnum FROM ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM cmSystemUser su where userName IN (SELECT userName from " + connectionTableName + " where " + foreignKey + " = $1) AND (firstName LIKE $2 OR lastName LIKE $3 OR userName LIKE $4 OR email LIKE $5 OR source LIKE $6) ORDER BY " + sortProperty + " " + direction + ") su ) AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery( oqlString);
					oql.bind(foreignKeyValue);
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
				}
			}
			else
			{
				if(offset != null || limit != null)
				{					
					String oqlString = "CALL SQL select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from (select su.* from " + connectionTableName + " sur, cmSystemUser su where sur.userName = su.userName AND sur." + foreignKey + " = $1 AND (su.firstName LIKE $2 OR su.lastName LIKE $3 OR su.userName LIKE $4 OR su.email LIKE $5 OR su.source LIKE $6) ORDER BY su." + sortProperty + " " + direction + " limit " + offset + "," + limit + ") pagedSystemUser AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
				}
				else
				{
					String oqlString = "CALL SQL select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from (select su.* from " + connectionTableName + " sur, cmSystemUser su where sur.userName = su.userName AND sur." + foreignKey + " = $1 AND (su.firstName LIKE $2 OR su.lastName LIKE $3 OR su.userName LIKE $4 OR su.email LIKE $5 OR su.source LIKE $6) ORDER BY su." + sortProperty + " " + direction + ") pagedSystemUser AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
					oql.bind("%" + searchString + "%");				
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
				}
			}
		}
		else
		{
			if(CmsPropertyHandler.getDatabaseEngine().equalsIgnoreCase("sql-server"))
			{
				if(offset != null || limit != null)
				{
					String oqlString = "CALL SQL With cmSystemUserCust AS ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime, ROW_NUMBER() OVER (order by " + sortProperty + " " + direction + ") as RowNumber  FROM cmSystemUser su where su.userName IN (select userName from " + connectionTableName + " where " + foreignKey + " = $1)) select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from cmSystemUserCust Where RowNumber > " + offset + " and RowNumber <= " + (offset + limit) + " AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
				}
				else
				{
					String oqlString = "CALL SQL With cmSystemUserCust AS ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime, ROW_NUMBER() OVER (order by " + sortProperty + " " + direction + ") as RowNumber  FROM cmSystemUser su where su.userName IN (select userName from " + connectionTableName + " where " + foreignKey + " = $1)) select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from cmSystemUserCust AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
				}
			}
			else if(CmsPropertyHandler.getDatabaseEngine().equalsIgnoreCase("oracle"))
			{
				if(offset != null || limit != null)
				{
					String oqlString = "CALL SQL SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM ( SELECT su.userName,su.password,su.firstName,su.lastName,su.email,su.source,su.isActive,su.modifiedDateTime, ROWNUM rnum FROM ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM cmSystemUser su where userName IN (SELECT userName from " + connectionTableName + " where " + foreignKey + " = $1) ORDER BY " + sortProperty + " " + direction + ") su  WHERE ROWNUM <= " + (offset + limit) + " ) WHERE rnum > " + offset + " AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
				}
				else
				{
					String oqlString = "CALL SQL SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM ( SELECT su.userName,su.password,su.firstName,su.lastName,su.email,su.source,su.isActive,su.modifiedDateTime, ROWNUM rnum FROM ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM cmSystemUser su where userName IN (SELECT userName from " + connectionTableName + " where " + foreignKey + " = $1) ORDER BY " + sortProperty + " " + direction + ") su ) AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
				}
			}
			else
			{
				if(offset != null || limit != null)
				{
					String oqlString = "CALL SQL select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from (select su.* from " + connectionTableName + " sur, cmSystemUser su where sur.userName = su.userName AND sur." + foreignKey + " = $1 ORDER BY su." + sortProperty + " " + direction + " limit " + offset + "," + limit + ") pagedSystemUser AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
				}
				else
				{
					String oqlString = "CALL SQL select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from (select su.* from " + connectionTableName + " sur, cmSystemUser su where sur.userName = su.userName AND sur." + foreignKey + " = $1 ORDER BY su." + sortProperty + " " + direction + ") pagedSystemUser AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);						
				}
			}
		}
		
		QueryResults results = oql.execute(Database.ReadOnly);

		while (results.hasMore()) 
		{
			SystemUser extranetUser = (SystemUser)results.next();
			filteredList.add(extranetUser.getValueObject());
		}
		
		results.close();
		oql.close();

		return filteredList;
	}
	
	public List getFilteredSystemUserListInvertedOnRoleOrGroup(Integer offset, Integer limit, String sortProperty, String direction, String searchString, String roleName, String groupName, Database db) throws SystemException, Bug, Exception
	{
		List filteredList = new ArrayList();
		
		if(sortProperty == null || sortProperty.equals(""))
			sortProperty = "userName";
		if(direction == null || direction.equals(""))
			direction = "ASC";
		
		String connectionTableName = "cmSystemUserRole";
		if(roleName == null)
			connectionTableName = "cmSystemUserGroup";

		String foreignKey = "roleName";
		String foreignKeyValue = roleName;
		if(roleName == null)
		{
			foreignKey = "groupName";
			foreignKeyValue = groupName;
		}
		
		OQLQuery oql = null;
		if(searchString != null && !searchString.equals(""))
		{
			if(CmsPropertyHandler.getDatabaseEngine().equalsIgnoreCase("sql-server"))
			{
				if(offset != null || limit != null)
				{
					String oqlString = "CALL SQL With cmSystemUserCust AS ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime, ROW_NUMBER() OVER (order by " + sortProperty + " " + direction + ") as RowNumber  FROM cmSystemUser su where su.userName NOT IN (select userName from " + connectionTableName + " where " + foreignKey + " = $1) AND (firstName LIKE $2 OR lastName LIKE $3 OR userName LIKE $4 OR email LIKE $5 OR source LIKE $6)) select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from cmSystemUserCust Where RowNumber > " + offset + " and RowNumber <= " + (offset + limit) + " AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
				}
				else
				{
					String oqlString = "CALL SQL With cmSystemUserCust AS ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime, ROW_NUMBER() OVER (order by " + sortProperty + " " + direction + ") as RowNumber  FROM cmSystemUser su where su.userName NOT IN (select userName from " + connectionTableName + " where " + foreignKey + " = $1) AND (firstName LIKE $2 OR lastName LIKE $3 OR userName LIKE $4 OR email LIKE $5 OR source LIKE $6)) select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from cmSystemUserCust AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
				}
			}
			else if(CmsPropertyHandler.getDatabaseEngine().equalsIgnoreCase("oracle"))
			{
				if(offset != null || limit != null)
				{
					String oqlString = "CALL SQL SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM ( SELECT su.userName,su.password,su.firstName,su.lastName,su.email,su.source,su.isActive,su.modifiedDateTime, ROWNUM rnum FROM ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM cmSystemUser su where userName NOT IN (SELECT userName from " + connectionTableName + " where " + foreignKey + " = $1) AND (firstName LIKE $2 OR lastName LIKE $3 OR userName LIKE $4 OR email LIKE $5 OR source LIKE $6) ORDER BY " + sortProperty + " " + direction + ") su  WHERE ROWNUM <= " + (offset + limit) + " ) WHERE rnum > " + offset + " AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
				}
				else
				{
					String oqlString = "CALL SQL SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM ( SELECT su.userName,su.password,su.firstName,su.lastName,su.email,su.source,su.isActive,su.modifiedDateTime, ROWNUM rnum FROM ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM cmSystemUser su where userName NOT IN (SELECT userName from " + connectionTableName + " where " + foreignKey + " = $1) AND (firstName LIKE $2 OR lastName LIKE $3 OR userName LIKE $4 OR email LIKE $5 OR source LIKE $6) ORDER BY " + sortProperty + " " + direction + ") su ) AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery( oqlString);
					oql.bind(foreignKeyValue);
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
				}
			}
			else
			{
				if(offset != null || limit != null)
				{					
					String oqlString = "CALL SQL select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from (select su.* from cmSystemUser su where su.userName NOT IN (SELECT userName from " + connectionTableName + " where " + foreignKey + " = $1) AND (su.firstName LIKE $2 OR su.lastName LIKE $3 OR su.userName LIKE $4 OR su.email LIKE $5 OR su.source LIKE $6) ORDER BY su." + sortProperty + " " + direction + " limit " + offset + "," + limit + ") pagedSystemUser AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
				}
				else
				{
					String oqlString = "CALL SQL select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from (select su.* from cmSystemUser su where su.userName NOT IN (SELECT userName from " + connectionTableName + " where " + foreignKey + " = $1) AND (su.firstName LIKE $2 OR su.lastName LIKE $3 OR su.userName LIKE $4 OR su.email LIKE $5 OR su.source LIKE $6) ORDER BY su." + sortProperty + " " + direction + ") pagedSystemUser AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
					oql.bind("%" + searchString + "%");				
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
					oql.bind("%" + searchString + "%");
				}
			}
		}
		else
		{
			if(CmsPropertyHandler.getDatabaseEngine().equalsIgnoreCase("sql-server"))
			{
				if(offset != null || limit != null)
				{
					String oqlString = "CALL SQL With cmSystemUserCust AS ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime, ROW_NUMBER() OVER (order by " + sortProperty + " " + direction + ") as RowNumber  FROM cmSystemUser su where su.userName NOT IN (select userName from " + connectionTableName + " where " + foreignKey + " = $1)) select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from cmSystemUserCust Where RowNumber > " + offset + " and RowNumber <= " + (offset + limit) + " AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";

					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
				}
				else
				{
					String oqlString = "CALL SQL With cmSystemUserCust AS ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime, ROW_NUMBER() OVER (order by " + sortProperty + " " + direction + ") as RowNumber  FROM cmSystemUser su where su.userName NOT IN (select userName from " + connectionTableName + " where " + foreignKey + " = $1)) select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from cmSystemUserCust AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";

					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
				}
			}
			else if(CmsPropertyHandler.getDatabaseEngine().equalsIgnoreCase("oracle"))
			{
				if(offset != null || limit != null)
				{
					String oqlString = "CALL SQL SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM ( SELECT su.userName,su.password,su.firstName,su.lastName,su.email,su.source,su.isActive,su.modifiedDateTime, ROWNUM rnum FROM ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM cmSystemUser su where userName NOT IN (SELECT userName from " + connectionTableName + " where " + foreignKey + " = $1) ORDER BY " + sortProperty + " " + direction + ") su  WHERE ROWNUM <= " + (offset + limit) + " ) WHERE rnum > " + offset + " AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";

					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
				}
				else
				{
					String oqlString = "CALL SQL SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM ( SELECT su.userName,su.password,su.firstName,su.lastName,su.email,su.source,su.isActive,su.modifiedDateTime, ROWNUM rnum FROM ( SELECT userName,password,firstName,lastName,email,source,isActive,modifiedDateTime FROM cmSystemUser su where userName NOT IN (SELECT userName from " + connectionTableName + " where " + foreignKey + " = $1) ORDER BY " + sortProperty + " " + direction + ") su ) AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";

					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
				}
			}
			else
			{
				if(offset != null || limit != null)
				{
					String oqlString = "CALL SQL select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from (select su.* from cmSystemUser su where su.userName NOT IN (SELECT userName from " + connectionTableName + " where " + foreignKey + " = $1) ORDER BY su." + sortProperty + " " + direction + " limit " + offset + "," + limit + ") pagedSystemUser AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);
				}
				else
				{
					String oqlString = "CALL SQL select userName,password,firstName,lastName,email,source,isActive,modifiedDateTime from (select su.* from cmSystemUser su where su.userName NOT IN (SELECT userName from " + connectionTableName + " where " + foreignKey + " = $1) ORDER BY su." + sortProperty + " " + direction + ") pagedSystemUser AS org.infoglue.cms.entities.management.impl.simple.SystemUserImpl";
					oql = db.getOQLQuery(oqlString);
					oql.bind(foreignKeyValue);						
				}
			}
		}
		
		QueryResults results = oql.execute(Database.ReadOnly);
		
		while (results.hasMore()) 
		{
			SystemUser extranetUser = (SystemUser)results.next();
			filteredList.add(extranetUser.getValueObject());
		}
		
		results.close();
		oql.close();

		return filteredList;
	}

	public Integer getFilteredSystemUserCount(String roleName, String groupName, String searchString, Database db) throws SystemException, Bug, Exception
	{
		Integer count = 0;
		
		String connectionTableName = "cmSystemUserRole";
		if(roleName == null)
			connectionTableName = "cmSystemUserGroup";

		String foreignKey = "roleName";
		String foreignKeyValue = roleName;
		if(roleName == null)
		{
			foreignKey = "groupName";
			foreignKeyValue = groupName;
		}

		OQLQuery oql = null;
		if(searchString != null && !searchString.equals(""))
		{
			String oqlString = "CALL SQL select count(su.userName) as count from " + connectionTableName + " sur, cmSystemUser su where sur.userName = su.userName AND sur." + foreignKey + " = $1  AND (su.firstName LIKE $2 OR su.lastName LIKE $3 OR su.userName LIKE $4 OR su.email LIKE $5 OR su.source LIKE $6) AS org.infoglue.cms.entities.management.TableCount";
			oql = db.getOQLQuery(oqlString);
			oql.bind(foreignKeyValue);
			oql.bind("%" + searchString + "%");				
			oql.bind("%" + searchString + "%");				
			oql.bind("%" + searchString + "%");				
			oql.bind("%" + searchString + "%");				
			oql.bind("%" + searchString + "%");				
		}
		else
		{
			String oqlString = "CALL SQL select count(su.userName) as count from " + connectionTableName + " sur, cmSystemUser su where sur.userName = su.userName AND sur." + foreignKey + " = $1 AS org.infoglue.cms.entities.management.TableCount";
			oql = db.getOQLQuery(oqlString);
			oql.bind(foreignKeyValue);				
		}
		
		QueryResults results = oql.execute(Database.ReadOnly);

		while (results.hasMore()) 
		{
			TableCount tableCount = (TableCount)results.next();
			count = tableCount.getCount();
		}

		results.close();
		oql.close();

		return count;
	}
	
	public Integer getFilteredSystemUserCountInverted(String roleName, String groupName, String searchString, Database db) throws SystemException, Bug, Exception
	{
		Integer count = 0;
		
		String connectionTableName = "cmSystemUserRole";
		if(roleName == null)
			connectionTableName = "cmSystemUserGroup";

		String foreignKey = "roleName";
		String foreignKeyValue = roleName;
		if(roleName == null)
		{
			foreignKey = "groupName";
			foreignKeyValue = groupName;
		}

		OQLQuery oql = null;
		if(searchString != null && !searchString.equals(""))
		{
			String oqlString = "CALL SQL select count(su.userName) as count from cmSystemUser su where su.userName NOT IN (SELECT userName from " + connectionTableName + " WHERE " + connectionTableName + "." + foreignKey + " = $1) AND (su.firstName LIKE $2 OR su.lastName LIKE $3 OR su.userName LIKE $4 OR su.email LIKE $5 OR su.source LIKE $6) AS org.infoglue.cms.entities.management.TableCount";
			oql = db.getOQLQuery(oqlString);
			oql.bind(foreignKeyValue);
			oql.bind("%" + searchString + "%");				
			oql.bind("%" + searchString + "%");				
			oql.bind("%" + searchString + "%");				
			oql.bind("%" + searchString + "%");				
			oql.bind("%" + searchString + "%");				
		}
		else
		{
			String oqlString = "CALL SQL select count(su.userName) as count from cmSystemUser su where su.userName NOT IN (SELECT userName from " + connectionTableName + " WHERE " + connectionTableName + "." + foreignKey + " = $1) AS org.infoglue.cms.entities.management.TableCount";
			oql = db.getOQLQuery(oqlString);
			oql.bind(foreignKeyValue);				
		}
		
		QueryResults results = oql.execute(Database.ReadOnly);

		while (results.hasMore()) 
		{
			TableCount tableCount = (TableCount)results.next();
			count = tableCount.getCount();
		}

		results.close();
		oql.close();

		return count;
	}
	

	
	/*
	 * CREATE
	 * 
	 */
    public SystemUserVO create(SystemUserVO systemUserVO) throws ConstraintException, SystemException
    {
		if(CmsPropertyHandler.getUsePasswordEncryption())
		{
	    	String password = systemUserVO.getPassword();
			try
			{
				byte[] encryptedPassRaw = DigestUtils.sha(password);
				String encryptedPass = new String(Base64.encodeBase64(encryptedPassRaw), "ASCII");
				password = encryptedPass;
				systemUserVO.setPassword(password);
			}
			catch (Exception e) 
			{
				logger.error("Error generating password:" + e.getMessage());
			}
		}
    	
    	SystemUser systemUser = new SystemUserImpl();
        systemUser.setValueObject(systemUserVO);
        systemUser = (SystemUser) createEntity(systemUser);
        return systemUser.getValueObject();
    }     

	/*
	 * CREATE
	 * 
	 */
    public SystemUser create(SystemUserVO systemUserVO, Database db) throws ConstraintException, SystemException, Exception
    {
		if(CmsPropertyHandler.getUsePasswordEncryption())
		{
	    	String password = systemUserVO.getPassword();
			
			try
			{
				byte[] encryptedPassRaw = DigestUtils.sha(password);
				String encryptedPass = new String(Base64.encodeBase64(encryptedPassRaw), "ASCII");
				password = encryptedPass;
				systemUserVO.setPassword(password);
			}
			catch (Exception e) 
			{
				logger.error("Error generating password:" + e.getMessage());
			}
		}
		
    	SystemUser systemUser = new SystemUserImpl();
        systemUser.setValueObject(systemUserVO);
        systemUser = (SystemUser) createEntity(systemUser, db);
        
        return systemUser;
    }     

	/*
	 * DELETE
	 * 
	 */
	 
    public void delete(String userName) throws ConstraintException, SystemException
    {
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		SystemUser systemUser = null;

		beginTransaction(db);

		try
		{
			//add validation here if needed
			
		    delete(userName, db);
			
			//If any of the validations or setMethods reported an error, we throw them up now before create.
			ceb.throwIfNotEmpty();
            
			commitTransaction(db);
		}
		catch(ConstraintException ce)
		{
			logger.warn("An error occurred so we should not completes the transaction:" + ce, ce);
			rollbackTransaction(db);
			throw ce;
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not completes the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}

    }        

    /*
	 * DELETE
	 * 
	 */
	 
    public void delete(String userName, Database db) throws ConstraintException, SystemException, Exception
    {
    	deleteRoles(userName, null);
    	deleteGroups(userName, null);
    	deleteEntity(SystemUserImpl.class, userName, db);
    }        

	/**
	 * 	Delete all roles for a user
	 * @throws Exception 
	 */
	 
	public void deleteRoles(String userName, Set<String> roleNamesToKeep/*, Database db*/) throws SystemException, Bug, Exception
	{
        Database db = CastorDatabaseService.getDatabase();

        beginTransaction(db);

        try
        {
        	deleteRoles(userName, roleNamesToKeep, db);
        	
            commitTransaction(db);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
 	}
	
	/**
	 * 	Delete all roles for a user
	 * @throws Exception 
	 */
	 
	public void deleteRoles(String userName, Set<String> roleNamesToKeep, Database db) throws SystemException, Bug, Exception
	{
    	OQLQuery oql = db.getOQLQuery( "SELECT sur FROM org.infoglue.cms.entities.management.impl.simple.SystemUserRoleImpl sur WHERE sur.userName = $1");
    	oql.bind(userName);
    	
    	QueryResults results = oql.execute();
		while (results.hasMore()) 
        {
			SystemUserRoleImpl sur = (SystemUserRoleImpl)results.nextElement();
			if(roleNamesToKeep != null && roleNamesToKeep.contains(sur.getRoleName()))
			{
				logger.info("Keeping " + sur.getUserName() + "/" + sur.getRoleName());
				roleNamesToKeep.remove(sur.getRoleName());
			}
			else
			{
				logger.info("Deleting " + sur.getUserName() + "/" + sur.getRoleName());
				db.remove(sur);
			}
		}
		
		results.close();
		oql.close();
 	}

	/**
	 * 	Delete all roles for a user
	 * @throws Exception 
	 */
	 
	public void deleteGroups(String userName, Set<String> groupNamesToKeep) throws SystemException, Bug, Exception
	{
        Database db = CastorDatabaseService.getDatabase();

        beginTransaction(db);

        try
        {
        	deleteGroups(userName, groupNamesToKeep, db);
        
    		commitTransaction(db);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
 	}

	/**
	 * 	Delete all roles for a user
	 * @throws Exception 
	 */
	 
	public void deleteGroups(String userName, Set<String> groupNamesToKeep, Database db) throws SystemException, Bug, Exception
	{
    	OQLQuery oql = db.getOQLQuery( "SELECT sur FROM org.infoglue.cms.entities.management.impl.simple.SystemUserGroupImpl sur WHERE sur.userName = $1");
    	oql.bind(userName);
    	
    	QueryResults results = oql.execute();
		while (results.hasMore()) 
        {
			SystemUserGroupImpl sur = (SystemUserGroupImpl)results.nextElement();
			if(groupNamesToKeep != null && groupNamesToKeep.contains(sur.getGroupName()))
			{
				logger.info("Keeping " + sur.getUserName() + "/" + sur.getGroupName());
				groupNamesToKeep.remove(sur.getGroupName());
			}
			else
			{
				logger.info("Deleting " + sur.getUserName() + "/" + sur.getGroupName());
				db.remove(sur);
			}
		}
		
		results.close();
		oql.close();
 	}

    public SystemUserVO update(SystemUserVO systemUserVO) throws ConstraintException, SystemException
    {
    	return (SystemUserVO) updateEntity(SystemUserImpl.class, (BaseEntityVO) systemUserVO);
    }        


    public SystemUserVO update(SystemUserVO systemUserVO, String[] roleNames, String[] groupNames) throws ConstraintException, SystemException, Exception
    {
    	/*
    	if(roleNames != null)
    		deleteRoles(systemUserVO.getUserName());
		if(groupNames != null)
			deleteGroups(systemUserVO.getUserName());
		*/
    	
        Database db = CastorDatabaseService.getDatabase();
        
        SystemUser systemUser = null;

        beginTransaction(db);

        try
        {
            systemUser = update(systemUserVO, roleNames, groupNames, db);

            commitTransaction(db);
        }
        catch(ConstraintException ce)
        {
        	ce.printStackTrace();
            logger.warn("An error occurred so we should not completes the transaction:" + ce, ce);
            rollbackTransaction(db);
            throw ce;
        }
        catch(Exception e)
        {
        	e.printStackTrace();
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
                
        return systemUser.getValueObject();
    }        

    public SystemUser update(SystemUserVO systemUserVO, String[] roleNames, String[] groupNames, Database db) throws Bug, Exception
    {
    	Set<String> roleNamesSet = null;
    	Set<String> groupNamesSet = null;
    	
        if(roleNames != null)
    	{
            List<String> roleNamesList = Arrays.asList(roleNames);
            roleNamesSet = new HashSet<String>(roleNamesList);
    		deleteRoles(systemUserVO.getUserName(), roleNamesSet, db);
    	}

        if(groupNames != null)
    	{
            List<String> groupNamesList = Arrays.asList(groupNames);
            groupNamesSet = new HashSet<String>(groupNamesList);
    		deleteGroups(systemUserVO.getUserName(), groupNamesSet, db);
    	}
		
        SystemUser systemUser = getSystemUserWithName(systemUserVO.getUserName(), db);
        
    	systemUserVO.setUserName(systemUser.getUserName());
		systemUserVO.setPassword(systemUser.getPassword());
		systemUser.setValueObject(systemUserVO);
		
		if(roleNames != null)
		{
            for (String roleName : roleNamesSet)
            {
            	if(logger.isInfoEnabled())
            		logger.info("Trying to create:" + systemUserVO.getUserName() + "/" + roleName);
				try 
				{
					SystemUserRoleImpl userRole = new SystemUserRoleImpl();
					userRole.setUserName(""+systemUserVO.getUserName());
					userRole.setRoleName(roleName);
				
					db.create(userRole);
				} 
				catch (PersistenceException e) 
				{
					e.printStackTrace();
				}
            }
		}

    	if(groupNames != null)
		{
			for (String groupName : groupNamesSet)
            {
            	if(logger.isInfoEnabled())
            		logger.info("Trying to create:" + systemUserVO.getUserName() + "/" + groupName);
				try 
				{
					SystemUserGroupImpl userGroup = new SystemUserGroupImpl();
					userGroup.setUserName(""+systemUserVO.getUserName());
					userGroup.setGroupName(groupName);
				
					db.create(userGroup);
				} 
				catch (PersistenceException e) 
				{
					e.printStackTrace();
				}
            }
		}
		
        return systemUser;
    }     

    public SystemUserVO update(SystemUserVO systemUserVO, String oldPassword, String[] roleNames, String[] groupNames) throws ConstraintException, SystemException, Exception
    {
    	/*
        if(roleNames != null)
    	{
            List<String> roleNamesList = Arrays.asList(roleNames);
            Set<String> roleNamesSet = new HashSet<String>(roleNamesList);
    		deleteRoles(systemUserVO.getUserName(), roleNamesSet, db);
    	}

        if(groupNames != null)
    	{
            List<String> groupNamesList = Arrays.asList(groupNames);
            Set<String> groupNamesSet = new HashSet<String>(groupNamesList);
    		deleteGroups(systemUserVO.getUserName(), groupNamesSet, db);
    	}
    	*/
			
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        SystemUser systemUser = null;

        beginTransaction(db);

        try
        {
            systemUser = update(systemUserVO, oldPassword, roleNames, groupNames, db);
            
            commitTransaction(db);
        }
        catch(ConstraintException ce)
        {
            logger.warn("An error occurred so we should not completes the transaction:" + ce, ce);
            rollbackTransaction(db);
            throw ce;
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return systemUser.getValueObject();
    }        

    public SystemUser update(SystemUserVO systemUserVO, String oldPassword, String[] roleNames, String[] groupNames, Database db) throws ConstraintException, SystemException, Exception
    {
    	logger.info("systemUserVO:" + systemUserVO.getUserName());
    	logger.info("oldPassword:" + oldPassword);
    	logger.info("newPassword:" + systemUserVO.getPassword());
    	logger.info("roleNames:" + roleNames);
    	logger.info("groupNames:" + groupNames);
    	if(CmsPropertyHandler.getUsePasswordEncryption())
		{
    		String password = systemUserVO.getPassword();
			try
			{
				byte[] encryptedPassRaw = DigestUtils.sha(password);
				String encryptedPass = new String(Base64.encodeBase64(encryptedPassRaw), "ASCII");
				password = encryptedPass;
				systemUserVO.setPassword(password);
			
				byte[] encryptedOldPasswordRaw = DigestUtils.sha(oldPassword);
				String encryptedOldPassword = new String(Base64.encodeBase64(encryptedOldPasswordRaw), "ASCII");
				oldPassword = encryptedOldPassword;
			}
			catch (Exception e) 
			{
				logger.error("Error generating password:" + e.getMessage());
			}
		}
    	
    	SystemUser systemUser = getSystemUser(db, systemUserVO.getUserName(), oldPassword);
        if(systemUser == null)
        	throw new SystemException("Wrong user or password.");
        
        systemUserVO.setUserName(systemUser.getUserName());

    	Set<String> roleNamesSet = null;
    	Set<String> groupNamesSet = null;

        if(roleNames != null)
    	{
            List<String> roleNamesList = Arrays.asList(roleNames);
            roleNamesSet = new HashSet<String>(roleNamesList);
    		deleteRoles(systemUserVO.getUserName(), roleNamesSet, db);
    	}

        if(groupNames != null)
    	{
            List<String> groupNamesList = Arrays.asList(groupNames);
            groupNamesSet = new HashSet<String>(groupNamesList);
    		deleteGroups(systemUserVO.getUserName(), groupNamesSet, db);
    	}
        
		if(roleNamesSet != null)
		{
			for (String roleName : roleNamesSet)
            {
				logger.info("Adding roleName:" + roleName);
				RoleController.getController().addUser(roleName, systemUser.getUserName(), db);
            }			
		}
		
		if(groupNamesSet != null)
		{
			for (String groupName : groupNamesSet)
            {
				logger.info("Adding group:" + groupName);
				GroupController.getController().addUser(groupName, systemUser.getUserName(), db);
            }
		}
		
		//systemUserVO.setPassword(systemUser.getPassword());
		systemUser.setValueObject(systemUserVO);

        return systemUser;
    }     

    public void updatePassword(String userName) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
            updatePassword(userName, db);
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }        

    public void updatePassword(String userName, Database db) throws ConstraintException, SystemException, Exception
    {
        SystemUser systemUser = getSystemUserWithName(userName, db);
        
        String newPassword = PasswordGenerator.generate();
        
        String password = newPassword;
		if(CmsPropertyHandler.getUsePasswordEncryption())
		{
			try
			{
				byte[] encryptedPassRaw = DigestUtils.sha(password);
				String encryptedPass = new String(Base64.encodeBase64(encryptedPassRaw), "ASCII");
				password = encryptedPass;
			}
			catch (Exception e) 
			{
				logger.error("Error generating password:" + e.getMessage());
			}
		}

        systemUser.setPassword(password);
		
		StringBuffer sb = new StringBuffer();
		sb.append("<div><h2>Password changed</h2></div>");
		sb.append("<div>CMS notification: You or an administrator have requested a new password for your account (" + userName + "). <br/>");
		sb.append("<br/>");
		sb.append("The new password is '" + newPassword + "'.<br/>");
		sb.append("<br/>");
		sb.append("Please notify the administrator if this does not work. <br/>");
		sb.append("<br/>");
		sb.append("-----------------------------------------------------------------------<br/>");
		sb.append("This email was automatically generated and the sender is the CMS-system. <br/>");
		sb.append("Do not reply to this email. </div>");

		String systemEmailSender = CmsPropertyHandler.getSystemEmailSender();
		if(systemEmailSender == null || systemEmailSender.equalsIgnoreCase(""))
			systemEmailSender = "InfoGlueCMS@" + CmsPropertyHandler.getMailSmtpHost();

		try
		{
			MailServiceFactory.getService().send(systemEmailSender, systemUser.getEmail(), null, "InfoGlue Information - Password changed!!", sb.toString());
		}
		catch(Exception e)
		{
			logger.error("The notification was not sent to [" + systemEmailSender + ", " + systemUser.getEmail() + "]. Reason:" + e.getMessage(), e);
		}
    }        

    public void updateAnonymousPassword(String userName) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();

        beginTransaction(db);

        try
        {
            updateAnonymousPassword(userName, db);
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }        

    public void updateAnonymousPassword(String userName, Database db) throws ConstraintException, SystemException, Exception
    {
        SystemUser systemUser = getSystemUserWithName(userName, db);
        String newPassword = "anonymous";
        
        String password = newPassword;
		if(CmsPropertyHandler.getUsePasswordEncryption())
		{
			try
			{
				byte[] encryptedPassRaw = DigestUtils.sha(password);
				String encryptedPass = new String(Base64.encodeBase64(encryptedPassRaw), "ASCII");
				password = encryptedPass;
			}
			catch (Exception e) 
			{
				logger.error("Error generating password:" + e.getMessage());
			}
		}

        systemUser.setPassword(password);		
    }        

    public void updatePassword(String userName, String oldPassword, String newPassword) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
            //If any of the validations or setMethods reported an error, we throw them up now before create.
            ceb.throwIfNotEmpty();

            updatePassword(userName, oldPassword, newPassword, db);
            
            commitTransaction(db);
        }
        catch(ConstraintException ce)
        {
            rollbackTransaction(db);
            throw ce;
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }    
    
    public void updatePassword(String userName, String oldPassword, String newPassword, Database db) throws ConstraintException, SystemException, Exception
    {
        if(newPassword == null)
            throw new ConstraintException("SystemUser.newPassword", "301");

		if(CmsPropertyHandler.getUsePasswordEncryption())
		{
			try
			{
				byte[] encryptedPassRaw = DigestUtils.sha(newPassword);
				String encryptedPass = new String(Base64.encodeBase64(encryptedPassRaw), "ASCII");
				newPassword = encryptedPass;

				byte[] encryptedOldPasswordRaw = DigestUtils.sha(oldPassword);
				String encryptedOldPass = new String(Base64.encodeBase64(encryptedOldPasswordRaw), "ASCII");
				oldPassword = encryptedOldPass;
			}
			catch (Exception e) 
			{
				logger.error("Error generating password:" + e.getMessage());
			}
		}

        SystemUser systemUser = getSystemUser(db, userName, oldPassword);
        if(systemUser == null)
            throw new ConstraintException("SystemUser.oldPassword", "310");
            
        systemUser.setPassword(newPassword);		
    }        

  
	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new SystemUserVO();
	}

}
 
