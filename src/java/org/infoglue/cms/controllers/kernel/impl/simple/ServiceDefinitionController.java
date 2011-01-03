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

import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ServiceDefinition;
import org.infoglue.cms.entities.management.ServiceDefinitionVO;
import org.infoglue.cms.entities.management.impl.simple.ServiceDefinitionImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class ServiceDefinitionController extends BaseController
{
    private final static Logger logger = Logger.getLogger(ServiceDefinitionController.class.getName());

	/*
	 * Factory method
	 */
	
	public static ServiceDefinitionController getController()
	{
		return new ServiceDefinitionController();
	}

    public ServiceDefinitionVO getServiceDefinitionVOWithId(Integer serviceDefinitionId) throws SystemException, Bug
    {
		return (ServiceDefinitionVO) getVOWithId(ServiceDefinitionImpl.class, serviceDefinitionId);
    }

    public ServiceDefinitionVO getServiceDefinitionVOWithId(Integer serviceDefinitionId, Database db) throws SystemException, Bug
    {
		return (ServiceDefinitionVO) getVOWithId(ServiceDefinitionImpl.class, serviceDefinitionId, db);
    }
	
    public ServiceDefinitionVO create(ServiceDefinitionVO vo) throws ConstraintException, SystemException
    {
        ServiceDefinition ent = new ServiceDefinitionImpl();
        ent.setValueObject(vo);
        ent = (ServiceDefinition) createEntity(ent);
        return ent.getValueObject();
    }     

	/**
	 * The service definition can only be removed if no serviceBinding uses it.
	 */
	
    public void delete(ServiceDefinitionVO vo) throws ConstraintException, SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		
        beginTransaction(db);

        try
        { 
        	ServiceDefinition serviceDefinition = getServiceDefinitionWithId(vo.getServiceDefinitionId(), db);
    		if(serviceDefinition.getName().equalsIgnoreCase("Core content service") || serviceDefinition.getName().equalsIgnoreCase("Core structure service"))
    		{
    			throw new ConstraintException("ServiceDefinition.deleteAction", "3200");
    		}	
        }
        catch(ConstraintException ce)
        {
        	throw ce;
        }
        catch(SystemException se)
        {
        	throw se;
        }
        catch(Exception e)
        {
        	throw new SystemException("An error occurred in ServiceDefinitionController.delete(). Reason:" + e.getMessage(), e);
        }
        finally
        {
        	commitTransaction(db);
        }
        
    	deleteEntity(ServiceDefinitionImpl.class, vo.getServiceDefinitionId());
    }        

    public ServiceDefinition getServiceDefinitionWithId(Integer serviceDefinitionId, Database db) throws SystemException, Bug
    {
		return (ServiceDefinition) getObjectWithId(ServiceDefinitionImpl.class, serviceDefinitionId, db);
    }

    public List getServiceDefinitionVOList() throws SystemException, Bug
    {
        return getAllVOObjects(ServiceDefinitionImpl.class, "serviceDefinitionId");
    }

	/**
	 * This method deletes the ServiceDefinition sent in from the system.
	 */
	
	public void deleteServiceDefinition(Integer serviceDefinitionId, Database db) throws SystemException, Bug
	{
		try
		{
			db.remove(getServiceDefinitionWithId(serviceDefinitionId, db));
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to delete ServiceDefinition in the database. Reason: " + e.getMessage(), e);
		}	
	} 
	
    public ServiceDefinitionVO update(ServiceDefinitionVO serviceDefinitionVO) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        ServiceDefinition serviceDefinition = null;

        beginTransaction(db);

        try
        {
            //add validation here if needed
            serviceDefinition = getServiceDefinitionWithId(serviceDefinitionVO.getServiceDefinitionId(), db);
            serviceDefinition.setValueObject(serviceDefinitionVO);

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


        return serviceDefinition.getValueObject();
    }        
	
	
	/**
	 * This method fetches an ServiceDefinition with the given name.
	 * 
	 * @throws SystemException
	 * @throws Bug
	 */
    
	public ServiceDefinitionVO getServiceDefinitionVOWithName(String name) throws SystemException, Bug
	{
		ServiceDefinitionVO serviceDefinitionVO = null;
		
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		beginTransaction(db);

		try
		{
			ServiceDefinition serviceDefinition = getServiceDefinitionWithName(name, db, true);
			if(serviceDefinition != null)
				serviceDefinitionVO = serviceDefinition.getValueObject();
			
			commitTransaction(db);
		}
		catch(Exception e)
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a ServiceDefinition by name. Reason:" + e.getMessage(), e);    
		}
    
		return serviceDefinitionVO;
	}

	
	/**
	 * Returns the ServiceDefinition with the given name fetched within a given transaction.
	 * 
	 * @param name
	 * @param database
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */

	public ServiceDefinition getServiceDefinitionWithName(String name, Database db, boolean readOnly) throws SystemException, Bug
	{
		ServiceDefinition serviceDefinition = null;
		
		try
		{
			OQLQuery oql = db.getOQLQuery("SELECT a FROM org.infoglue.cms.entities.management.impl.simple.ServiceDefinitionImpl a WHERE a.name = $1");
			oql.bind(name);
						
			QueryResults results = null;
			if(readOnly)
			    results = oql.execute(Database.ReadOnly);
			else
			{
			    this.logger.info("Fetching entity in read/write mode" + name);
				results = oql.execute();
			}
			
			if(results.hasMore()) 
			{
				serviceDefinition = (ServiceDefinition)results.next();
			}

			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a named AvailableServiceBinding. Reason:" + e.getMessage(), e);    
		}
		
		return serviceDefinition;		
	}
	
	
	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new ServiceDefinitionVO();
	}

}
 
