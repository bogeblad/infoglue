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
import org.infoglue.cms.entities.management.Subscription;
import org.infoglue.cms.entities.management.SubscriptionFilter;
import org.infoglue.cms.entities.management.SubscriptionFilterVO;
import org.infoglue.cms.entities.management.SubscriptionVO;
import org.infoglue.cms.entities.management.impl.simple.SubscriptionFilterImpl;
import org.infoglue.cms.entities.management.impl.simple.SubscriptionImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;

public class SubscriptionController extends BaseController
{ 
    private final static Logger logger = Logger.getLogger(SubscriptionController.class.getName());

	/**
	 * Factory method
	 */

	public static SubscriptionController getController()
	{
		return new SubscriptionController();
	}
	
    public SubscriptionVO getSubscriptionVOWithId(Integer subscriptionId) throws SystemException, Bug
    {
		return (SubscriptionVO) getVOWithId(SubscriptionImpl.class, subscriptionId);
    }

    public Subscription getSubscriptionWithId(Integer subscriptionId, Database db) throws SystemException, Bug
    {
		return (Subscription) getObjectWithId(SubscriptionImpl.class, subscriptionId, db);
    }

    public List getSubscriptionVOList() throws SystemException, Bug
    {
		List subscriptionVOList = getAllVOObjects(SubscriptionImpl.class, "subscriptionId");

		return subscriptionVOList;
    }

    public List getSubscriptionVOList(Database db) throws SystemException, Bug
    {
		List subscriptionVOList = getAllVOObjects(SubscriptionImpl.class, "subscriptionId", db);

		return subscriptionVOList;
    }

    public SubscriptionVO create(SubscriptionVO subscriptionVO) throws ConstraintException, SystemException
    {
        Subscription subscription = new SubscriptionImpl();
        subscription.setValueObject(subscriptionVO);
        subscription = (Subscription) createEntity(subscription);
        return subscription.getValueObject();
    }

    public SubscriptionVO create(SubscriptionVO subscriptionVO,List<SubscriptionFilterVO> subscriptionFilterVOList) throws ConstraintException, SystemException
    {
    	SubscriptionVO newSubscriptionVO = null;
    	
    	Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			beginTransaction(db);
			
	        newSubscriptionVO = create(subscriptionVO, subscriptionFilterVOList, db).getValueObject();
	        
	        commitTransaction(db);
		}
		catch (Exception e)		
		{
			e.printStackTrace();
		    logger.warn("An error occurred so we should not complete the transaction:" + e);
		    rollbackTransaction(db);
		}

		return newSubscriptionVO;
    }

    public Subscription create(SubscriptionVO subscriptionVO,List<SubscriptionFilterVO> subscriptionFilterVOList, Database db) throws ConstraintException, SystemException, Exception
    {
    	Subscription subscription = new SubscriptionImpl();
		subscription.setValueObject(subscriptionVO);
        subscription = (Subscription) createEntity(subscription, db);

		Iterator<SubscriptionFilterVO> subscriptionFilterVOListIterator = subscriptionFilterVOList.iterator();
		while(subscriptionFilterVOListIterator.hasNext())
		{
			SubscriptionFilterVO subscriptionFilterVO = subscriptionFilterVOListIterator.next();
	    	SubscriptionFilterImpl subscriptionFilter = new SubscriptionFilterImpl();
	    	subscriptionFilter.setValueObject(subscriptionFilterVO);
	    	subscriptionFilter.setSubscription(subscription);
	    	subscription.getSubscriptionFilters().add(subscriptionFilter);	        				
		}

		return subscription;
    }

    public SubscriptionVO update(SubscriptionVO subscriptionVO, List<SubscriptionFilterVO> subscriptionFilterVOList) throws ConstraintException, SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			beginTransaction(db);
			
			Subscription subscription = getSubscriptionWithId(subscriptionVO.getId(), db);
			//db.remove(subscription);
	        subscription.setValueObject(subscriptionVO);
			subscription.setSubscriptionFilters(new ArrayList());
			
			Iterator<SubscriptionFilterVO> subscriptionFilterVOListIterator = subscriptionFilterVOList.iterator();
			while(subscriptionFilterVOListIterator.hasNext())
			{
				SubscriptionFilterVO subscriptionFilterVO = subscriptionFilterVOListIterator.next();
		    	SubscriptionFilterImpl subscriptionFilter = new SubscriptionFilterImpl();
		    	subscriptionFilter.setValueObject(subscriptionFilterVO);
		    	subscriptionFilter.setSubscription(subscription);
		    	logger.info("Adding subscriptionFilter:" + subscriptionFilter);
		    	subscription.getSubscriptionFilters().add(subscriptionFilter);	        				
			}

			subscriptionVO = subscription.getValueObject();
			
	        commitTransaction(db);
		}
		catch (Exception e)		
		{
			e.printStackTrace();
		    logger.warn("An error occurred so we should not complete the transaction:" + e);
		    rollbackTransaction(db);
		}

		return subscriptionVO;
    }

    public void addFilter(Integer subscriptionId, SubscriptionFilterVO subscriptionFilterVO) throws ConstraintException, SystemException
    {
		Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			beginTransaction(db);
			
	    	Subscription subscription = getSubscriptionWithId(subscriptionId, db);
	    	SubscriptionFilterImpl subscriptionFilter = new SubscriptionFilterImpl();
	    	subscriptionFilter.setValueObject(subscriptionFilterVO);
	    	subscription.getSubscriptionFilters().add(subscriptionFilter);
	    	
	        commitTransaction(db);
		}
		catch (Exception e)		
		{
		    logger.warn("An error occurred so we should not complete the transaction:" + e);
		    rollbackTransaction(db);
		}
    }

    public void delete(SubscriptionVO subscriptionVO) throws ConstraintException, SystemException
    {
    	deleteEntity(SubscriptionImpl.class, subscriptionVO.getSubscriptionId());
    }

    public void delete(Integer subscriptionId) throws ConstraintException, SystemException
    {
    	deleteEntity(SubscriptionImpl.class, subscriptionId);
    }

    public SubscriptionVO update(SubscriptionVO subscriptionVO) throws ConstraintException, SystemException
    {
    	return (SubscriptionVO) updateEntity(SubscriptionImpl.class, subscriptionVO);
    }

    
	/**
	 * Gets matching subscriptions
	 */
	
	public List<SubscriptionVO> getSubscriptionVOList(Integer interceptionPointId, String name, Boolean isGlobal, String entityName, String entityId, String userName, String userEmail) throws SystemException, Exception
	{
		List<SubscriptionVO> subscriptionVOList = new ArrayList<SubscriptionVO>();
		
		Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			beginTransaction(db);
			
			List<Subscription> subscriptionList = getSubscriptionList(interceptionPointId, name, isGlobal, entityName, entityId, userName, userEmail, db, true);
			subscriptionVOList = toVOList(subscriptionList);
			
	        commitTransaction(db);
		}
		catch (Exception e)		
		{
		    logger.warn("An error occurred so we should not complete the transaction:" + e);
		    rollbackTransaction(db);
		}
		
		return subscriptionVOList;
	}

	/**
	 * Gets matching subscriptions
	 */
	
	public List<SubscriptionVO> getSubscriptionVOList(Integer interceptionPointId, String name, Boolean isGlobal, String entityName, String entityId, String userName, String userEmail, Database db, boolean readOnly) throws SystemException, Exception
	{
		List<SubscriptionVO> subscriptionVOList = new ArrayList<SubscriptionVO>();
		
		List<Subscription> subscriptionList = getSubscriptionList(interceptionPointId, name, isGlobal, entityName, entityId, userName, userEmail, db, true);
		subscriptionVOList = toVOList(subscriptionList);
		
		return subscriptionVOList;
	}


	/**
	 * Gets matching subscriptions
	 */
	
	public List<Subscription> getSubscriptionList(Integer interceptionPointId, String name, Boolean isGlobal, String entityName, String entityId, String userName, String userEmail, Database db, boolean readOnly) throws SystemException, Exception
	{
	    List<Subscription> subscriptionList = new ArrayList<Subscription>();
	    
	    StringBuffer sql = new StringBuffer("SELECT s FROM org.infoglue.cms.entities.management.impl.simple.SubscriptionImpl s WHERE ");
	    List bindings = new ArrayList();
	    int bindingIndex = 1;
	    
	    if(interceptionPointId != null)
	    {
	    	if(bindingIndex > 1)
	    		sql.append(" AND ");
	    	sql.append("s.interceptionPointId = $" + bindingIndex);
	    	bindings.add(interceptionPointId);
	    	bindingIndex++;
	    }

	    if(name != null)
	    {
	    	if(bindingIndex > 1)
	    		sql.append(" AND ");
	    	sql.append("s.name = $" + bindingIndex);
	    	bindings.add(name);
	    	bindingIndex++;
	    }

	    if(isGlobal != null)
	    {
	    	if(bindingIndex > 1)
	    		sql.append(" AND ");
	    	sql.append("s.isGlobal = $" + bindingIndex);
	    	bindings.add(isGlobal);
	    	bindingIndex++;
	    }

	    if(entityName != null)
	    {
	    	if(bindingIndex > 1)
	    		sql.append(" AND ");
	    	sql.append("s.entityName = $" + bindingIndex);
	    	bindings.add(entityName);
	    	bindingIndex++;
	    }

	    if(entityId != null)
	    {
	    	if(bindingIndex > 1)
	    		sql.append(" AND ");
	    	sql.append("s.entityId = $" + bindingIndex);
	    	bindings.add(entityId);
	    	bindingIndex++;
	    }

	    if(userName != null)
	    {
	    	if(bindingIndex > 1)
	    		sql.append(" AND ");
	    	sql.append("s.userName = $" + bindingIndex);
	    	bindings.add(userName);
	    	bindingIndex++;
	    }

	    if(userEmail != null)
	    {
	    	if(bindingIndex > 1)
	    		sql.append(" AND ");
	    	sql.append("s.userEmail = $" + bindingIndex);
	    	bindings.add(userEmail);
	    	bindingIndex++;
	    }

	    sql.append(" ORDER BY s.subscriptionId");
	    
		OQLQuery oql = db.getOQLQuery(sql.toString());
		Iterator bindingsIterator = bindings.iterator();
		while(bindingsIterator.hasNext())
			oql.bind(bindingsIterator.next());
		
		QueryResults results = null;
		if(!readOnly)
			results = oql.execute();
		else
			results = oql.execute(Database.ReadOnly);
			
		while (results.hasMore()) 
        {
            Subscription subscription = (Subscription)results.next();
            subscriptionList.add(subscription);
        }            
		
		results.close();
		oql.close();

		return subscriptionList;		
	}
	
	

	public BaseEntityVO getNewVO()
	{
		return null; //new SubscriptionVO();
	}
	
}
 
