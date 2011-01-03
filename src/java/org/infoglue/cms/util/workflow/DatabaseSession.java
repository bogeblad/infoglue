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
package org.infoglue.cms.util.workflow;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;

import com.opensymphony.workflow.WorkflowException;

/**
 * A database session controls the lifecycle of a wrapped database object. 
 */
public class DatabaseSession 
{
	/**
	 * The class logger.
	 */
    private final static Logger logger = Logger.getLogger(DatabaseSession.class.getName());

	/**
	 * The wrapped database object.
	 */
	private Database db;
	
	/**
	 * Indicates if the database should be rollback when released.
	 */
	private boolean rollbackOnly;

	/**
	 * Default constructor.
	 */
	public DatabaseSession() 
	{ 
		super(); 
	}

	/**
	 * Sets the rollback only flag to true.
	 */
	public void setRollbackOnly() 
	{ 
		rollbackOnly = true; 
	}

	/**
	 * Sets the rollback only flag to true.
	 */
	public boolean getRollbackOnly() 
	{ 
		return rollbackOnly; 
	}

	/**
	 * Returns the wrapped database. The database will be opened and the transaction will be started
	 * when this function is first called.
	 * 
	 * @return the wrapped database object.
	 * @throws WorkflowException if an error occurs during the initializing of the transaction/database.
	 */
	public Database getDB() throws WorkflowException 
	{
		if(db == null) 
		{
		    try 
		    {
				logger.debug("Creating a new database");
				db = CastorDatabaseService.getDatabase();
				db.begin();
		    } 
		    catch(Exception e) 
		    {
				logger.error("Unable to create database", e);
				throw new WorkflowException(e);
		    }
		}
		return db;
	}
	
	/**
	 * Releases the database. If the rollback only flag has been set, the transaction
	 * is rolled back. Otherwise the transaction is commited. Finally the database is closed.
	 * 
	 * @throws WorkflowException if an error occurs while releasing the transaction/database.
	 */
	public void releaseDB() throws WorkflowException 
	{
		logger.debug("releaseDB : " + (rollbackOnly ? "rollback" : "commit"));
		if(rollbackOnly)
		{
			rollback();
		}
		else
		{
			commit();
		}
	}
	
	/**
	 * Rolls back the transaction and closes the database.
	 * 
	 * @throws WorkflowException if an error occurs during the rollback/close operation.
	 */
	private void rollback() throws WorkflowException 
	{
		logger.debug("rollback()");
		try 
		{
			doRollback();
		}
		finally 
		{
			doClose();
		}
	}

	/**
	 * Commits the transaction and closes the database.
	 * 
	 * @throws WorkflowException if an error occurs during the commit/close operation.
	 */
	private void commit() throws WorkflowException 
	{
		logger.debug("commit()");
		try 
		{
			doCommit();
		} 
		catch(Exception e) 
		{
			doRollback();
			throw new WorkflowException(e);
		}
		finally 
		{
			doClose();
		}
	}
	
	/**
	 * 
	 */
	private void doCommit() throws WorkflowException
	{
		if(db != null && db.isActive())
		{
			try
			{
				logger.debug("doCommit()");
				db.commit();
			}
			catch(Exception e)
			{
				logger.error(e);
				throw new WorkflowException(e);
			}
		}
	}

	/**
	 * 
	 */
	private void doRollback() throws WorkflowException
	{
		if(db != null && db.isActive())
		{
			try
			{
				logger.debug("doRollback()");
				db.rollback();
			}
			catch(Exception e)
			{
				logger.error(e);
				throw new WorkflowException(e);
			}
		}
	}

	/**
	 * 
	 */
	private void doClose() throws WorkflowException
	{
		if(db != null && !db.isClosed())
		{
			try
			{
				logger.debug("doClose()");
				db.close();
			}
			catch(Exception e)
			{
				logger.error(e);
				throw new WorkflowException(e);
			}
		}
	}
}
