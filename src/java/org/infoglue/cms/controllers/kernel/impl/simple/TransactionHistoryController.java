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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.TransactionHistory;
import org.infoglue.cms.entities.management.TransactionHistoryVO;
import org.infoglue.cms.entities.management.impl.simple.TransactionHistoryImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.NotificationMessage;

public class TransactionHistoryController extends BaseController
{
    private final static Logger logger = Logger.getLogger(TransactionHistoryController.class.getName());

	/**
	 * Factory method
	 */

	public static TransactionHistoryController getController()
	{
		return new TransactionHistoryController();
	}
	
	public TransactionHistoryVO getTransactionHistoryVOWithId(Integer transactionHistoryId) throws SystemException, Bug
	{
		return (TransactionHistoryVO) getVOWithId(TransactionHistoryImpl.class, transactionHistoryId);
	}

    public TransactionHistory getTransactionHistoryWithId(Integer transactionHistoryId, Database db) throws SystemException, Bug
    {
		return (TransactionHistory) getObjectWithId(TransactionHistoryImpl.class, transactionHistoryId, db);
    }


	public List getTransactionHistoryVOList() throws SystemException, Bug
	{
		return getAllVOObjects(TransactionHistoryImpl.class, "transactionHistoryId");
	}


	/**
	 * This method deletes the TransactionHistory sent in from the system.
	 */
	
	public void deleteTransactionHistory(Integer transactionHistoryId, Database db) throws SystemException, Bug
	{
		try
		{
			db.remove(getTransactionHistoryWithId(transactionHistoryId, db));
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to delete TransactionHistory in the database. Reason: " + e.getMessage(), e);
		}	
	} 

	public TransactionHistoryVO getLatestTransactionHistoryVOForEntity(Class entClass, Integer entityId) throws SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		TransactionHistoryVO transactionHistoryVO = null;
		beginTransaction(db);

		try
		{
		    
			OQLQuery oql = db.getOQLQuery( "SELECT th FROM org.infoglue.cms.entities.management.impl.simple.TransactionHistoryImpl th WHERE th.transactionObjectName LIKE $1 AND th.transactionObjectId = $2 ORDER BY th.transactionDateTime desc");
			oql.bind(entClass.getName() + "%");
			oql.bind(entityId);
			QueryResults results = oql.execute(Database.ReadOnly);

			if (results.hasMore()) 
			{
				TransactionHistory transactionHistory = (TransactionHistory)results.next();
				transactionHistoryVO = transactionHistory.getValueObject();
			}
			
			results.close();
			oql.close();

			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not completes the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
    	
		return transactionHistoryVO;
	}

	public List getLatestTransactionHistoryVOListForEntity(Class entClass, Integer entityId, List transactionType, Date startDateTime, Date endDateTime, int rowLimit) throws SystemException
	{
		List resultList = new ArrayList();

		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		TransactionHistoryVO transactionHistoryVO = null;
		beginTransaction(db);

		try
		{
		    String baseSQL = "SELECT th FROM org.infoglue.cms.entities.management.impl.simple.TransactionHistoryImpl th";
		    List bindings = new ArrayList();
		    int i = 1;
		    String whereArguments = "";
		    if(entClass != null)
		    {
		    	whereArguments += (whereArguments.equals("") ? "" : " AND ") + " th.transactionObjectName LIKE $" + i + " ";
		    	bindings.add(entClass.getName() + "%");
		    	i++;
		    }
		    if(entityId != null)
		    {
		    	whereArguments += (whereArguments.equals("") ? "" : " AND ") + " th.transactionObjectId = $" + i + " ";
		    	bindings.add(entityId);
		    	i++;
		    }
		    if(transactionType != null)
		    {
		    	whereArguments += (whereArguments.equals("") ? "" : " AND ") + " th.transactionTypeId IN LIST (";
		    	int localIndex = 0;
		    	Iterator transactionTypeIterator = transactionType.iterator();
		    	while(transactionTypeIterator.hasNext())
		    	{
		    		if(localIndex > 0)
		    			whereArguments += ",";
		    		whereArguments += "$" + i;
			    	bindings.add(transactionTypeIterator.next());
			    	i++;
			    	localIndex++;
		    	}
		    	whereArguments += ") ";
		    }
		    if(startDateTime != null)
		    {
		    	whereArguments += (whereArguments.equals("") ? "" : " AND ") + " th.transactionDateTime >= $" + i + " ";
		    	bindings.add(startDateTime);
		    	i++;
		    }
		    if(endDateTime != null)
		    {
		    	whereArguments += (whereArguments.equals("") ? "" : " AND ") + " th.transactionDateTime <= $" + i + " ";
		    	bindings.add(endDateTime);
		    	i++;
		    }

		    String sql = baseSQL + (whereArguments.equals("") ? "" : " WHERE ") + whereArguments + " ORDER BY th.transactionDateTime desc";
		    //logger.info("sql:" + sql);

		    OQLQuery oql = db.getOQLQuery(sql);
			Iterator bindingIterator = bindings.iterator();
			while(bindingIterator.hasNext())
			{
				Object value = bindingIterator.next();
				oql.bind(value);
			}
		    
		    //OQLQuery oql = db.getOQLQuery( "SELECT th FROM org.infoglue.cms.entities.management.impl.simple.TransactionHistoryImpl th WHERE th.transactionObjectName LIKE $1 AND th.transactionObjectId = $2 ORDER BY th.transactionDateTime desc");
		    //oql.bind(entClass.getName() + "%");
		    //oql.bind(entityId);
			
			int currentRow = 0;
		    QueryResults results = oql.execute(Database.ReadOnly);
			while(results.hasMore() && currentRow < rowLimit) 
			{
				TransactionHistory transactionHistory = (TransactionHistory)results.next();
				//transactionHistoryVO = transactionHistory.getValueObject();
				resultList.add(transactionHistory.getValueObject());
				currentRow++;
			}
			
			results.close();
			oql.close();

			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not completes the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
    	
		return resultList;
	}

    public TransactionHistoryVO update(TransactionHistoryVO transactionHistoryVO) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        TransactionHistory transactionHistory = null;

        beginTransaction(db);

        try
        {
            //add validation here if needed
            transactionHistory = getTransactionHistoryWithId(transactionHistoryVO.getTransactionHistoryId(), db);
            transactionHistory.setValueObject(transactionHistoryVO);

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

        return transactionHistory.getValueObject();
    }        
	
	
	/**
	 * This method is a bit different from other creates as it does not use the common base-class-method.
	 * Using it would result in a recursive loop of new notificationMessages.
	 */
	
	public Integer create(NotificationMessage notificationMessage) throws SystemException
	{
		logger.info("Creating a transactionHistory object...");
        Database db = CastorDatabaseService.getDatabase();
        TransactionHistory transactionHistory = null;

        try
        {
	        beginTransaction(db);
			logger.info("Began transaction...");
	        
          	TransactionHistoryVO transVO = new TransactionHistoryVO();  
            transactionHistory = new TransactionHistoryImpl();

            transVO.setName(notificationMessage.getName());
            transVO.setSystemUserName(notificationMessage.getSystemUserName());
            transVO.setTransactionDateTime(java.util.Calendar.getInstance().getTime());
            transVO.setTransactionTypeId(new Integer(notificationMessage.getType()));
            transVO.setTransactionObjectId(notificationMessage.getObjectId().toString());
            transVO.setTransactionObjectName(notificationMessage.getObjectName());
            
			transactionHistory.setValueObject(transVO);	
			if(logger.isInfoEnabled())
			{
				logger.info("Created the transaction object and filled it with values...");
				logger.info("transactionHistory.getId():" + transactionHistory.getId());
				logger.info("transactionHistory.getName():" + transactionHistory.getName());
				logger.info("transactionHistory.getSystemUserName():" + transactionHistory.getSystemUserName());
				logger.info("transactionHistory.getTransactionDateTime():" + transactionHistory.getTransactionDateTime());
				logger.info("transactionHistory.getTransactionObjectId():" + transactionHistory.getTransactionObjectId());
				logger.info("transactionHistory.getTransactionObjectName():" + transactionHistory.getTransactionObjectName());
				logger.info("transactionHistory.getTransactionTypeId():" + transactionHistory.getTransactionTypeId());
				logger.info("isActive=" + db.isActive());
			}
			
			db.create(transactionHistory);
			logger.info("Created the transaction object in the database..");
            
            commitTransaction(db);
            logger.info("Committed the transaction..");
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			return null;
        }

		logger.info("TransactionHistory object all done..");

        return transactionHistory.getValueObject().getTransactionHistoryId();
	}
	
	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new TransactionHistoryVO();
	}

}
 
