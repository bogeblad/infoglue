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
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.AvailableServiceBinding;
import org.infoglue.cms.entities.management.SiteNodeTypeDefinition;
import org.infoglue.cms.entities.management.SiteNodeTypeDefinitionVO;
import org.infoglue.cms.entities.management.impl.simple.SiteNodeTypeDefinitionImpl;
import org.infoglue.cms.entities.structure.ServiceBinding;
import org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.deliver.util.Timer;


public class SiteNodeTypeDefinitionController extends BaseController
{
    private final static Logger logger = Logger.getLogger(SiteNodeTypeDefinitionController.class.getName());

	/**
	 * Factory method
	 */

	public static SiteNodeTypeDefinitionController getController()
	{
		return new SiteNodeTypeDefinitionController();
	}
	
    public SiteNodeTypeDefinitionVO getSiteNodeTypeDefinitionVOWithId(Integer siteNodeTypeDefinitionId) throws SystemException, Bug
    {
		return (SiteNodeTypeDefinitionVO) getVOWithId(SiteNodeTypeDefinitionImpl.class, siteNodeTypeDefinitionId);
    }

    public SiteNodeTypeDefinitionVO getSiteNodeTypeDefinitionVOWithId(Integer siteNodeTypeDefinitionId, Database db) throws SystemException, Bug
    {
		return (SiteNodeTypeDefinitionVO) getVOWithId(SiteNodeTypeDefinitionImpl.class, siteNodeTypeDefinitionId, db);
    }

    public SiteNodeTypeDefinitionVO create(SiteNodeTypeDefinitionVO vo) throws ConstraintException, SystemException
    {
        SiteNodeTypeDefinition ent = new SiteNodeTypeDefinitionImpl();
        ent.setValueObject(vo);
        ent = (SiteNodeTypeDefinition) createEntity(ent);
        return ent.getValueObject();
    }     

    public void delete(SiteNodeTypeDefinitionVO vo) throws ConstraintException, SystemException
    {
    	deleteEntity(SiteNodeTypeDefinitionImpl.class, vo.getSiteNodeTypeDefinitionId());
    }        

    public SiteNodeTypeDefinition getSiteNodeTypeDefinitionWithId(Integer siteNodeTypeDefinitionId, Database db) throws SystemException, Bug
    {
		return (SiteNodeTypeDefinition) getObjectWithId(SiteNodeTypeDefinitionImpl.class, siteNodeTypeDefinitionId, db);
    }

    public SiteNodeTypeDefinition getSiteNodeTypeDefinitionWithIdAsReadOnly(Integer siteNodeTypeDefinitionId, Database db) throws SystemException, Bug
    {
		return (SiteNodeTypeDefinition) getObjectWithIdAsReadOnly(SiteNodeTypeDefinitionImpl.class, siteNodeTypeDefinitionId, db);
    }

    public List getSiteNodeTypeDefinitionVOList() throws SystemException, Bug
    {
        return getAllVOObjects(SiteNodeTypeDefinitionImpl.class, "siteNodeTypeDefinitionId");
    }

    public List getSiteNodeTypeDefinitionVOList(Database db) throws SystemException, Bug
    {
        return getAllVOObjects(SiteNodeTypeDefinitionImpl.class, "siteNodeTypeDefinitionId", db);
    }

    public List getSiteNodeTypeDefinitionList(Database db) throws SystemException, Bug
    {
        return getAllObjects(SiteNodeTypeDefinitionImpl.class, "siteNodeTypeDefinitionId", db);
    }

    public List getSortedSiteNodeTypeDefinitionVOList() throws SystemException, Bug
    {
    	List siteNodeTypeDefinitionVOList = getSiteNodeTypeDefinitionVOList();
    	
	    Collections.sort(siteNodeTypeDefinitionVOList, new ReflectionComparator("name"));

        return siteNodeTypeDefinitionVOList;
    }

	/**
	 * This method gets a SiteNodeTypeDefinition based on it's name.
	 * @param name
	 * @param db
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */

	public SiteNodeTypeDefinition getSiteNodeTypeDefinitionWithName(String name, Database db, boolean readOnly) throws SystemException, Bug
	{
		SiteNodeTypeDefinition siteNodeTypeDefinition = null;
		
		try
		{
			OQLQuery oql = db.getOQLQuery("SELECT s FROM org.infoglue.cms.entities.management.impl.simple.SiteNodeTypeDefinitionImpl s WHERE s.name = $1");
			oql.bind(name);
			
			QueryResults results = null;
			if(readOnly)
			    results = oql.execute(Database.ReadOnly);
		    else
		    {
		        this.logger.info("Fetching entity in read/write mode" + name);
			    results = oql.execute();
		    }
			
			if (results.hasMore()) 
			{
				siteNodeTypeDefinition = (SiteNodeTypeDefinition)results.next();
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a SiteNodeTypeDefinition based on name. Reason:" + e.getMessage(), e);    
		}
    
		return siteNodeTypeDefinition;
	}

	/**
	 * This method deletes the SiteNodeTypeDefinition sent in from the system.
	 */
	
	public void deleteSiteNodeTypeDefinition(Integer siteNodeTypeDefinitionId, Database db) throws SystemException, Bug
	{
		try
		{
			db.remove(getSiteNodeTypeDefinitionWithId(siteNodeTypeDefinitionId, db));
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to delete SiteNodeTypeDefinition in the database. Reason: " + e.getMessage(), e);
		}	
	} 

    public SiteNodeTypeDefinitionVO update(SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        SiteNodeTypeDefinition siteNodeTypeDefinition = null;

        beginTransaction(db);

        try
        {
            //add validation here if needed
            siteNodeTypeDefinition = getSiteNodeTypeDefinitionWithId(siteNodeTypeDefinitionVO.getSiteNodeTypeDefinitionId(), db);
            siteNodeTypeDefinition.setValueObject(siteNodeTypeDefinitionVO);

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

        return siteNodeTypeDefinition.getValueObject();
    }        
    
    public SiteNodeTypeDefinitionVO update(SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO, String[] availableServiceBindingValues) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        SiteNodeTypeDefinition siteNodeTypeDefinition = null;

        beginTransaction(db);

        try
        {
            siteNodeTypeDefinition = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionWithId(siteNodeTypeDefinitionVO.getSiteNodeTypeDefinitionId(), db);
            siteNodeTypeDefinition.setValueObject(siteNodeTypeDefinitionVO);

            //add validation here if needed
            List availableServiceBindingList = new ArrayList();
            if(availableServiceBindingValues != null)
			{
		        for (int i=0; i < availableServiceBindingValues.length; i++)
	            {
	            	AvailableServiceBinding availableServiceBinding = AvailableServiceBindingController.getController().getAvailableServiceBindingWithId(new Integer(availableServiceBindingValues[i]), db);
	            	availableServiceBindingList.add(availableServiceBinding);
	            	//siteNodeTypeDefinition.getAvailableServiceBindings().add(availableServiceBinding);
	            }
			}
			
			//logger.info("availableServiceBindingList:" + availableServiceBindingList);
			siteNodeTypeDefinition.setAvailableServiceBindings(availableServiceBindingList);
			
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
        finally
        {
        	closeDatabase(db);
        }

        return siteNodeTypeDefinition.getValueObject();
    }        

	/**
	 * This method returns a list with AvailableServiceBidningVO-objects which are available for the
	 * siteNodeTypeDefinition sent in
	 */
	
	public List getAvailableServiceBindingVOList(Integer siteNodeTypeDefinitionId) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        List availableServiceBindingVOList = null;

        beginTransaction(db);

        try
        {
        	/*
        	OQLQuery oql = db.getOQLQuery( "SELECT asb FROM org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl asb WHERE asb.siteNodeTypeDefinition.siteNodeTypeDefinitionId = $1");
        	oql.bind(siteNodeTypeDefinitionId);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			while (results.hasMore()) 
            {
            	AvailableServiceBinding availableServiceBinding = (AvailableServiceBinding)results.next();
				availableServiceBindingVOList.add(availableServiceBinding.getValueObject());
            }
            */

        	SiteNodeTypeDefinition siteNodeTypeDefinition = getSiteNodeTypeDefinitionWithIdAsReadOnly(siteNodeTypeDefinitionId, db);
        	Collection availableServiceBindingList = siteNodeTypeDefinition.getAvailableServiceBindings();
        	availableServiceBindingVOList = toVOList(availableServiceBindingList);

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

        return availableServiceBindingVOList;
	}

	
	/**
	 * This method returns a list with AvailableServiceBidningVO-objects which are available for the
	 * siteNodeTypeDefinition sent in
	 */
	
	public List getAvailableServiceBindingVOList(Integer siteNodeTypeDefinitionId, Database db) throws ConstraintException, SystemException
	{
        List availableServiceBindingVOList = null;
    	/*
    	OQLQuery oql = db.getOQLQuery( "SELECT asb FROM org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl asb WHERE asb.siteNodeTypeDefinition.siteNodeTypeDefinitionId = $1");
    	oql.bind(siteNodeTypeDefinitionId);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		while (results.hasMore()) 
        {
        	AvailableServiceBinding availableServiceBinding = (AvailableServiceBinding)results.next();
			availableServiceBindingVOList.add(availableServiceBinding.getValueObject());
        }
        */

    	SiteNodeTypeDefinition siteNodeTypeDefinition = getSiteNodeTypeDefinitionWithIdAsReadOnly(siteNodeTypeDefinitionId, db);
        Collection availableServiceBindingList = siteNodeTypeDefinition.getAvailableServiceBindings();
    	availableServiceBindingVOList = toVOList(availableServiceBindingList);

        return availableServiceBindingVOList;
	}

	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new SiteNodeTypeDefinitionVO();
	}

}
 
