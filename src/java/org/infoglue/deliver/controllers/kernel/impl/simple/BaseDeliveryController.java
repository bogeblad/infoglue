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

package org.infoglue.deliver.controllers.kernel.impl.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.kernel.IBaseEntity;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.deliver.util.RequestAnalyser;


/**
 * BaseDeliveryController.java
 * 
 * Baseclass for Castor Controller Classes.
 * Various methods to handle transactions and so on
 *  
 */

public abstract class BaseDeliveryController
{
    private final static Logger logger = Logger.getLogger(BaseDeliveryController.class.getName());

	/**
	 * This method fetches one object / entity within a transaction.
	 **/
	
	protected Object getObjectWithId(Class arg, Integer id, Database db) throws SystemException, Bug
	{
		return getObjectWithId(arg, id, db, true);
	}
	
	/**
	 * This method fetches one object / entity within a transaction.
	 **/
	
	protected Object getObjectWithId(Class arg, Integer id, Database db, boolean retry) throws SystemException, Bug
	{
		Object object = null;
		try
		{
			RequestAnalyser.getRequestAnalyser().incApproximateNumberOfDatabaseQueries();
			object = db.load(arg, id, Database.ReadOnly);
		}
		catch(Exception e)
		{
			try
			{
				if(retry)
				{
					logger.warn("Error getting object. Message: " + e.getMessage() + ". Retrying...");
					object = getObjectWithId(arg, id, db, false);
				}
				else
				{
					logger.warn("Error getting object. Message: " + e.getMessage() + ". Not retrying...");
					throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);
				}
			}
			catch(Exception e2)
			{
				throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
			}
		}
		finally
		{
			RequestAnalyser.getRequestAnalyser().decApproximateNumberOfDatabaseQueries();
		}
		
		if(object == null)
		{
			throw new Bug("The object with id [" + id + "] was not found. This should never happen.");
		}
		return object;
	}
	

	/**
	 * This method fetches one object in read only mode and returns it's value object.
	 */
	
	protected BaseEntityVO getVOWithId(Class arg, Integer id, Database db) throws SystemException, Bug
	{
		return getVOWithId(arg, id, db, true);
	}
	
	/**
	 * This method fetches one object in read only mode and returns it's value object.
	 */
	
	protected BaseEntityVO getVOWithId(Class arg, Integer id, Database db, boolean retry) throws SystemException, Bug
	{
		IBaseEntity vo = null;
		try
		{
			RequestAnalyser.getRequestAnalyser().incApproximateNumberOfDatabaseQueries();
			vo = (IBaseEntity)db.load(arg, id, Database.ReadOnly);
		}
		catch(Exception e)
		{
			try
			{
				if(retry)
				{
					logger.warn("Error getting object. Message: " + e.getMessage() + ". Retrying...");
					vo = (IBaseEntity)getVOWithId(arg, id, db, false);
				}
				else
				{
					logger.warn("Error getting object. Message: " + e.getMessage() + ". Not retrying...");
					throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);
				}
			}
			catch(Exception e2)
			{
				throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
			}
		}
		finally
		{
			RequestAnalyser.getRequestAnalyser().decApproximateNumberOfDatabaseQueries();
		}
    
		if(vo == null)
		{
			throw new Bug("The object with id [" + id + "] was not found. This should never happen.");
		}
        
		return vo.getVO();
	}


	/**
	 * This method fetches all object in read only mode and returns a list of value objects.
	 */

	public List getAllVOObjects(Class arg, Database db) throws SystemException, Bug
	{
		ArrayList resultList = new ArrayList();
		
		try
		{
        	logger.info("BaseHelper::GetAllObjects for " + arg.getName());
			OQLQuery oql = db.getOQLQuery( "SELECT u FROM " + arg.getName() + " u" );
			QueryResults results = oql.execute(Database.ReadOnly);
			
			while (results.hasMore()) 
			{
				Object o = results.next();

				// Om metoden getValueObject saknas, kastas ett undantag.            	
				resultList.add(o.getClass().getDeclaredMethod("getValueObject", new Class[0]).invoke(o, new Object[0]));
			}
			
			results.close();
			oql.close();
		}
		catch(NoSuchMethodException e)
		{
			throw new Bug("The object [" + arg.getName() + "] is of the wrong type. This should never happen.", e);
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch " + arg.getName() + " Reason:" + e.getMessage(), e);    
		}    

		return Collections.unmodifiableList(resultList);
	}
    
	/**
	 * This method fetches all object in read only mode and returns a list of value objects.
	 */

	public List getAllVOObjects(Class arg, String orderByField, String direction, Database db) throws SystemException, Bug
	{
		ArrayList resultList = new ArrayList();
		
		try
		{
        	
			logger.info("BaseHelper::GetAllObjects for " + arg.getName());
			OQLQuery oql = db.getOQLQuery( "SELECT u FROM " + arg.getName() + " u ORDER BY u." + orderByField + " " + direction);
			QueryResults results = oql.execute(Database.ReadOnly);
			
			while (results.hasMore()) 
			{
				Object o = results.next();

				// Om metoden getValueObject saknas, kastas ett undantag.            	
				resultList.add(o.getClass().getDeclaredMethod("getValueObject", new Class[0]).invoke(o, new Object[0]));
			}
			
			results.close();
			oql.close();
		}
		catch(NoSuchMethodException e)
		{
			throw new Bug("The object [" + arg.getName() + "] is of the wrong type. This should never happen.", e);
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch " + arg.getName() + " Reason:" + e.getMessage(), e);    
		}    

		return Collections.unmodifiableList(resultList);
	}
	
	//---------------------------------------------------------------------
	// Dynamic Query specific operations
	//---------------------------------------------------------------------
	/**
	 * Executes a Query with no parameters
	 *
 	 * @param query An OQL Query
	 * @return A list of the query results as Impls
	 * @throws SystemException If an error occurs
	 */
	protected static List executeQuery(Database db, String query) throws SystemException, Exception
	{
		return executeQuery(db, query, Collections.EMPTY_LIST);
	}
	

	/**
	 * Executes a Query, also binds the provided parameters
	 *
 	 * @param query An OQL Query
	 * @param params A List of paramters
	 * @return A list of the query results as Castor Impl
	 * @throws SystemException If an error occurs
	 */
	protected static List executeQuery(Database db, String query, List params) throws Exception
	{
		List resultList = new ArrayList();
		
		OQLQuery oql = createQuery(db, query, params);
		QueryResults results = oql.execute(Database.ReadOnly);
		resultList = Collections.list(results);
		
		results.close();
		oql.close();

		return resultList;
	}
         
	/**
	 * Creates an OQLQuery for the provided Database and binds the parameters to it.
	 *
	 * @param db The Database to create the OQLQuery on
	 * @param query The String OQL query
	 * @param params A List of Objects to bind to the query sequentially
	 * @return An OQLQuery instance that can be executer
	 * @throws PersistenceException
	 */
	protected static OQLQuery createQuery(Database db, String query, List params) throws PersistenceException
	{
		OQLQuery oql = db.getOQLQuery(query);
		if (params != null)
			for (Iterator i = params.iterator(); i.hasNext();)
				oql.bind(i.next());

		return oql;
	}

	/**
	 * This method converts a List of entities to a list of value-objects.
	 */
	protected static List toVOList(Collection entities) throws SystemException, Bug
	{
		List resultVOList = new ArrayList();

		if(entities == null)
			return Collections.EMPTY_LIST;

		Iterator iterator = entities.iterator();
		while (iterator.hasNext())
		{
			Object o = (Object)iterator.next();

			try
			{
				resultVOList.add(o.getClass().getDeclaredMethod("getValueObject", new Class[0]).invoke(o, new Object[0]));
			}
			catch(NoSuchMethodException e)
			{
				throw new Bug("The object in list was of the wrong type: " + o.getClass().getName() + ". This should never happen.", e);
			}
			catch(Exception e)
			{
				throw new SystemException("An error occurred when we tried to convert the collection to a valueList. Reason:" + e.getMessage(), e);
			}
		}

		return resultVOList;
	}

	/**
	 * Begins a transaction on the named database
	 */
         
	public static void beginTransaction(Database db) throws SystemException
	{
		try
		{
			db.begin();
			logger.info("Opening a new Transaction...");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SystemException("An error occurred when we tried to begin an transaction. Reason:" + e.getMessage(), e);    
		}
	}
	
	/**
	 * Rollbacks a transaction on the named database
	 */
     
	public static void closeTransaction(Database db) throws SystemException
	{
	    logger.info("closeTransaction a transaction and closing it...");
	    //rollbackTransaction(db);
	    commitTransaction(db);
	}
       
	/**
	 * Ends a transaction on the named database
	 */
     
	public static void commitTransaction(Database db) throws SystemException
	{
		try
		{
		    logger.info("Committing a transaction and closing it...");
		    
		    db.commit();
			db.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SystemException("An error occurred when we tried to commit an transaction. Reason:" + e.getMessage(), e);    
		}
	}
 
 
	/**
	 * Rollbacks a transaction on the named database
	 */
     
	public static void rollbackTransaction(Database db) throws SystemException
	{
	    logger.info("Rollback a transaction...");

		try
		{
			if (db.isActive())
			{
				db.rollback();
			    db.close();
			}
		}
		catch(Exception e)
		{
			logger.info("An error occurred when we tried to rollback an transaction. Reason:" + e.getMessage());
			//throw new SystemException("An error occurred when we tried to rollback an transaction. Reason:" + e.getMessage(), e);    
		}
	}

	/**
	 * Close the database
	 */
     
	public static void closeDatabase(Database db) throws SystemException
	{
		try
		{
		    logger.info("Closing database...");

			db.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SystemException("An error occurred when we tried to close a database. Reason:" + e.getMessage(), e);    
		}
	}

}