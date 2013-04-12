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
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.ServiceDefinitionImpl;
import org.infoglue.cms.entities.structure.Qualifyer;
import org.infoglue.cms.entities.structure.ServiceBinding;
import org.infoglue.cms.entities.structure.ServiceBindingVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallServiceBindingImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;


/**
 * @author ss
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ServiceBindingController extends BaseController 
{
    private final static Logger logger = Logger.getLogger(ServiceBindingController.class.getName());

    public static ServiceBindingController getController()
    {
        return new ServiceBindingController();
    }
    
    public static ServiceBindingVO getServiceBindingVOWithId(Integer serviceBindingId) throws SystemException, Bug
    {
		return (ServiceBindingVO) getVOWithId(ServiceBindingImpl.class, serviceBindingId);
    }

	/*
    public static ServiceBinding getServiceBindingWithId(Integer serviceBindingId) throws SystemException, Bug
    {
		return (ServiceBinding) getObjectWithId(ServiceBindingImpl.class, serviceBindingId);
    }
	*/
	
    public static ServiceBinding getServiceBindingWithId(Integer serviceBindingId, Database db) throws SystemException, Bug
    {
		return (ServiceBinding) getObjectWithId(ServiceBindingImpl.class, serviceBindingId, db);
    }

    public static ServiceBinding getReadOnlyServiceBindingWithId(Integer serviceBindingId, Database db) throws SystemException, Bug
    {
		return (ServiceBinding) getObjectWithIdAsReadOnly(ServiceBindingImpl.class, serviceBindingId, db);
    }

    public List getServiceBindingVOList() throws SystemException, Bug
    {
        return getAllVOObjects(ServiceBindingImpl.class, "serviceBindingId");
    }

	/**
	 * This method deletes all service bindings pointing to a content.
	 */

	public List getServiceBindingList(Integer availableServiceBindingId, Database db) throws ConstraintException, SystemException, Exception
	{		
		List serviceBindings = new ArrayList();
		
		OQLQuery oql = db.getOQLQuery( "SELECT sb FROM org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl sb WHERE sb.availableServiceBinding = $1 ORDER BY sb.serviceBindingId");
		oql.bind(availableServiceBindingId);
		
		QueryResults results = oql.execute();
		logger.info("Fetching entity in read/write mode");

		while(results.hasMore()) 
		{
			ServiceBinding serviceBinding = (ServiceBindingImpl)results.next();
			
			serviceBindings.add(serviceBinding);
		}	
		
		results.close();
		oql.close();
		
		return serviceBindings;
	}

	/**
	 * Returns a list of ServiceBindings that are bound to the given SiteNodeVersion.
	 * 
	 * @param siteNodeVersionId
	 * @return
	 * @throws SystemException If something goes wrong in the operation (most likely a database related error).
	 */
	public List<SmallServiceBindingImpl> getSmallServiceBindingsListForSiteNodeVersion(Integer siteNodeVersionId) throws SystemException
	{
		Database db = null;
		List<SmallServiceBindingImpl> serviceBindings = new ArrayList<SmallServiceBindingImpl>();
		try
		{
			db = CastorDatabaseService.getDatabase();
			beginTransaction(db);

			serviceBindings = getSmallServiceBindingsListForSiteNodeVersion(siteNodeVersionId, db);

            rollbackTransaction(db);
        }
        catch(Exception ex)
        {
            logger.error("An error occurred so we should not complete the transaction when getting service bindings for SiteNodeVersion. Message: " + ex.getMessage() + ". Type: " + ex.getClass());
            logger.warn("An error occurred so we should not complete the transaction when getting service bindings for SiteNodeVersion", ex);
            rollbackTransaction(db);
            throw new SystemException(ex.getMessage());
        }

		return serviceBindings;
	}

	public List<SmallServiceBindingImpl> getSmallServiceBindingsListForSiteNodeVersion(Integer siteNodeVersionId, Database db) throws PersistenceException
	{
		List<SmallServiceBindingImpl> serviceBindings = new ArrayList<SmallServiceBindingImpl>();

		OQLQuery oql = db.getOQLQuery( "SELECT sb FROM org.infoglue.cms.entities.structure.impl.simple.SmallServiceBindingImpl sb WHERE sb.siteNodeVersionId = $1 ORDER BY sb.serviceBindingId");
		oql.bind(siteNodeVersionId);

		QueryResults results = oql.execute();
		//logger.info("Fetching entity in read/write mode");

		while(results.hasMore())
		{
			SmallServiceBindingImpl serviceBinding = (SmallServiceBindingImpl)results.next();

			serviceBindings.add(serviceBinding);
		}

		if (logger.isDebugEnabled())
		{
			logger.debug("Lookup of Service bindings for SiteNodeVersion: " + siteNodeVersionId + " got results: " + results.size());
		}

		results.close();
		oql.close();

		return serviceBindings;
	}

    public static ServiceBindingVO create(ServiceBindingVO serviceBindingVO, String qualifyerXML, Integer availableServiceBindingId, Integer siteNodeVersionId, Integer serviceDefinitionId) throws ConstraintException, SystemException
    {
    	logger.info("Creating a serviceBinding with the following...");

    	logger.info("name:" + serviceBindingVO.getName());
    	logger.info("bindingTypeId:" + serviceBindingVO.getBindingTypeId());
    	logger.info("availableServiceBindingId:" + availableServiceBindingId);
    	logger.info("siteNodeVersionId:" + siteNodeVersionId);
    	logger.info("serviceDefinitionId:" + serviceDefinitionId);

    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		
		ServiceBinding serviceBinding = null;
		
        beginTransaction(db);

        try
        { 
        	SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(siteNodeVersionId, db);
	        serviceBinding = new ServiceBindingImpl();
	        serviceBinding.setValueObject(serviceBindingVO);
	        serviceBinding.setAvailableServiceBinding((AvailableServiceBindingImpl)AvailableServiceBindingController.getController().getAvailableServiceBindingWithId(availableServiceBindingId, db));
	        serviceBinding.setServiceDefinition((ServiceDefinitionImpl)ServiceDefinitionController.getController().getServiceDefinitionWithId(serviceDefinitionId, db));
	        serviceBinding.setSiteNodeVersion((SiteNodeVersionImpl)siteNodeVersion);
			
			//siteNodeVersion.getServiceBindings().add(serviceBinding);
			
	        logger.info("createEntity: " + serviceBinding.getSiteNodeVersion().getSiteNodeVersionId());
	                    
            serviceBinding.setBindingQualifyers(QualifyerController.createQualifyers(qualifyerXML, serviceBinding));
	        db.create(serviceBinding);
            
			siteNodeVersion.getServiceBindings().add(serviceBinding);

            RegistryController.getController().updateSiteNodeVersionThreaded(siteNodeVersion.getValueObject());

            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            e.printStackTrace();
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return serviceBinding.getValueObject();
    }      

	/**
	 * This is a method that lets you create a new service binding within a transaction.
	 */
	
	public ServiceBindingVO create(Database db, ServiceBindingVO serviceBindingVO, String qualifyerXML, Integer availableServiceBindingId, Integer siteNodeVersionId, Integer serviceDefinitionId) throws ConstraintException, SystemException
	{
		logger.info("Creating a serviceBinding with the following...");

		logger.info("name:" + serviceBindingVO.getName());
		logger.info("bindingTypeId:" + serviceBindingVO.getBindingTypeId());
		logger.info("availableServiceBindingId:" + availableServiceBindingId);
		logger.info("siteNodeVersionId:" + siteNodeVersionId);
		logger.info("serviceDefinitionId:" + serviceDefinitionId);

		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		
		ServiceBinding serviceBinding = null;
		
		try
		{ 
			SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(siteNodeVersionId, db);
			serviceBinding = new ServiceBindingImpl();
			serviceBinding.setValueObject(serviceBindingVO);
			serviceBinding.setAvailableServiceBinding((AvailableServiceBindingImpl)AvailableServiceBindingController.getController().getAvailableServiceBindingWithId(availableServiceBindingId, db));
			serviceBinding.setServiceDefinition((ServiceDefinitionImpl)ServiceDefinitionController.getController().getServiceDefinitionWithId(serviceDefinitionId, db));
			serviceBinding.setSiteNodeVersion((SiteNodeVersionImpl)siteNodeVersion);
			
			//siteNodeVersion.getServiceBindings().add(serviceBinding);
			
			logger.info("createEntity: " + serviceBinding.getSiteNodeVersion().getSiteNodeVersionId());
	                    
			serviceBinding.setBindingQualifyers(QualifyerController.createQualifyers(qualifyerXML, serviceBinding));
			db.create((ServiceBinding)serviceBinding);
			
			siteNodeVersion.getServiceBindings().add(serviceBinding);
			
            RegistryController.getController().updateSiteNodeVersionThreaded(siteNodeVersion.getValueObject());
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			e.printStackTrace();
			throw new SystemException(e.getMessage());
		}

		return serviceBinding.getValueObject();
	}      


    protected static SmallServiceBindingImpl create(ServiceBindingVO serviceBindingVO, Integer availableServiceBindingId, Integer siteNodeVersionId, Integer serviceDefinitionId, Database db) throws ConstraintException, SystemException, Exception
    {
    	logger.info("Creating a serviceBinding with the following...");

    	logger.info("name:" + serviceBindingVO.getName());
    	logger.info("bindingTypeId:" + serviceBindingVO.getBindingTypeId());
    	logger.info("availableServiceBindingId:" + availableServiceBindingId);
    	logger.info("siteNodeVersionId:" + siteNodeVersionId);
    	logger.info("serviceDefinitionId:" + serviceDefinitionId);

    	SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId, db);
    	
    	SmallServiceBindingImpl serviceBinding = new SmallServiceBindingImpl();
        serviceBinding.setValueObject(serviceBindingVO);
        serviceBinding.setAvailableServiceBinding((AvailableServiceBindingImpl)AvailableServiceBindingController.getController().getAvailableServiceBindingWithId(availableServiceBindingId, db));
        serviceBinding.setServiceDefinition((ServiceDefinitionImpl)ServiceDefinitionController.getController().getServiceDefinitionWithId(serviceDefinitionId, db));
        //serviceBinding.setSiteNodeVersion((SiteNodeVersionImpl)siteNodeVersion);
        serviceBinding.setSiteNodeVersionId(siteNodeVersionId);

        db.create(serviceBinding);
            
        RegistryController.getController().updateSiteNodeVersionThreaded(siteNodeVersionVO);

        return serviceBinding;
    }      

    /*
    protected static ServiceBinding create(ServiceBindingVO serviceBindingVO, Integer availableServiceBindingId, Integer siteNodeVersionId, Integer serviceDefinitionId, Database db) throws ConstraintException, SystemException, Exception
    {
    	logger.info("Creating a serviceBinding with the following...");

    	logger.info("name:" + serviceBindingVO.getName());
    	logger.info("bindingTypeId:" + serviceBindingVO.getBindingTypeId());
    	logger.info("availableServiceBindingId:" + availableServiceBindingId);
    	logger.info("siteNodeVersionId:" + siteNodeVersionId);
    	logger.info("serviceDefinitionId:" + serviceDefinitionId);

    	SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(siteNodeVersionId, db);
    	
    	ServiceBinding serviceBinding = new ServiceBindingImpl();
        serviceBinding.setValueObject(serviceBindingVO);
        serviceBinding.setAvailableServiceBinding((AvailableServiceBindingImpl)AvailableServiceBindingController.getController().getAvailableServiceBindingWithId(availableServiceBindingId, db));
        serviceBinding.setServiceDefinition((ServiceDefinitionImpl)ServiceDefinitionController.getController().getServiceDefinitionWithId(serviceDefinitionId, db));
        serviceBinding.setSiteNodeVersion((SiteNodeVersionImpl)siteNodeVersion);
        serviceBinding.setSiteNodeVersion(siteNodeVersionId);

        logger.info("createEntity: " + serviceBinding.getSiteNodeVersion().getSiteNodeVersionId());
        
        db.create(serviceBinding);
            
        RegistryController.getController().updateSiteNodeVersion(siteNodeVersion.getValueObject(), db);

        return serviceBinding;
    }      
	*/
    
    public static ServiceBindingVO update(ServiceBindingVO serviceBindingVO, String qualifyerXML) throws ConstraintException, SystemException
    {
    	logger.info("Updating a serviceBinding with the following...");

    	logger.info("name:" + serviceBindingVO.getName());
    	logger.info("bindingTypeId:" + serviceBindingVO.getBindingTypeId());
    	
    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		
		ServiceBinding serviceBinding = null;
		
        beginTransaction(db);

        try
        { 
	        serviceBinding = getServiceBindingWithId(serviceBindingVO.getServiceBindingId(), db);
	        serviceBinding.setPath(serviceBindingVO.getPath());
	        serviceBinding.getBindingQualifyers().clear();
	        Collection newQualifyers = QualifyerController.createQualifyers(qualifyerXML, serviceBinding);
            serviceBinding.setBindingQualifyers(newQualifyers);
            
            RegistryController.getController().updateSiteNodeVersionThreaded(serviceBinding.getSiteNodeVersion().getValueObject());

            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            e.printStackTrace();
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return serviceBinding.getValueObject();
    }      


	/**
	 * This method deletes all service bindings pointing to a content.
	 */

	public static void deleteServiceBindingsReferencingContent(Content content, Database db) throws ConstraintException, SystemException, Exception
	{		
		OQLQuery oql = db.getOQLQuery( "SELECT sb FROM org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl sb WHERE sb.bindingQualifyers.name = $1 AND sb.bindingQualifyers.value = $2 ORDER BY sb.serviceBindingId");
		oql.bind("contentId");
		oql.bind(content.getContentId().toString());
		
		QueryResults results = oql.execute();
		logger.info("Fetching entity in read/write mode");

		while(results.hasMore()) 
		{
			ServiceBinding serviceBinding = (ServiceBindingImpl)results.next();
			//logger.info("serviceBinding:" + serviceBinding.getServiceBindingId());
			Collection qualifyers = serviceBinding.getBindingQualifyers();
			Iterator qualifyersIterator = qualifyers.iterator();
			while(qualifyersIterator.hasNext())
			{	
				Qualifyer qualifyer = (Qualifyer)qualifyersIterator.next();
				//logger.info("qualifyer:" + qualifyer.getName() + ":" + qualifyer.getValue() + " == " + qualifyer.getValue().equals(content.getContentId().toString()));
				if(qualifyer.getName().equalsIgnoreCase("contentId") && qualifyer.getValue().equals(content.getContentId().toString()))
				{
					//db.remove(qualifyer);
					qualifyersIterator.remove();
					//logger.info("Qualifyers:" + serviceBinding.getBindingQualifyers().size());
					serviceBinding.getBindingQualifyers().remove(qualifyer);

					//logger.info("Qualifyers2:" + serviceBinding.getBindingQualifyers().size());
					if(serviceBinding.getBindingQualifyers() == null || serviceBinding.getBindingQualifyers().size() == 0)
					{
						//logger.info("Removing service binding...");
						db.remove(serviceBinding);
					}
				}
			}
			
			SiteNodeVersion siteNodeVersion = serviceBinding.getSiteNodeVersion();
			if(siteNodeVersion.getOwningSiteNode() == null)
			    SiteNodeVersionController.getController().delete(siteNodeVersion, db);
		}	
		
		results.close();
		oql.close();
	}       
	
	
	/**
	 * This method deletes all service bindings pointing to a content.
	 */

	public static void deleteServiceBindingsReferencingSiteNode(SiteNode siteNode, Database db) throws ConstraintException, SystemException, Exception
	{		
		OQLQuery oql = db.getOQLQuery( "SELECT sb FROM org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl sb WHERE sb.bindingQualifyers.name = $1 AND sb.bindingQualifyers.value = $2 ORDER BY sb.serviceBindingId");
		oql.bind("siteNodeId");
		oql.bind(siteNode.getSiteNodeId().toString());
		
		QueryResults results = oql.execute();
		logger.info("Fetching entity in read/write mode");

		while(results.hasMore()) 
		{
			ServiceBinding serviceBinding = (ServiceBindingImpl)results.next();
			//logger.info("serviceBinding:" + serviceBinding.getServiceBindingId());
			Collection qualifyers = serviceBinding.getBindingQualifyers();
			Iterator qualifyersIterator = qualifyers.iterator();
			while(qualifyersIterator.hasNext())
			{	
				Qualifyer qualifyer = (Qualifyer)qualifyersIterator.next();
				//logger.info("qualifyer:" + qualifyer.getName() + ":" + qualifyer.getValue() + " == " + qualifyer.getValue().equals(content.getContentId().toString()));
				if(qualifyer.getName().equalsIgnoreCase("siteNodeId") && qualifyer.getValue().equals(siteNode.getSiteNodeId().toString()))
				{
					//db.remove(qualifyer);
					qualifyersIterator.remove();
					//logger.info("Qualifyers:" + serviceBinding.getBindingQualifyers().size());
					serviceBinding.getBindingQualifyers().remove(qualifyer);

					//logger.info("Qualifyers2:" + serviceBinding.getBindingQualifyers().size());
					if(serviceBinding.getBindingQualifyers() == null || serviceBinding.getBindingQualifyers().size() == 0)
					{
						//logger.info("Removing service binding...");
						db.remove(serviceBinding);
					}
				}
			}
		}
		
		results.close();
		oql.close();
	}       

	/**
	 * This method deletes all service bindings pointing to a site node version.
	 */

	public static void deleteServiceBindingsReferencingSiteNodeVersion(SiteNodeVersion siteNodeVersion, Database db) throws ConstraintException, SystemException, Exception
	{		
		OQLQuery oql = db.getOQLQuery( "SELECT sb FROM org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl sb WHERE sb.siteNodeVersion = $1 ORDER BY sb.serviceBindingId");
		oql.bind(siteNodeVersion);
		
		QueryResults results = oql.execute();
		logger.info("Fetching entity in read/write mode");

		while(results.hasMore()) 
		{
			ServiceBinding serviceBinding = (ServiceBindingImpl)results.next();
			//logger.info("serviceBinding:" + serviceBinding.getServiceBindingId());
			Collection qualifyers = serviceBinding.getBindingQualifyers();
			Iterator qualifyersIterator = qualifyers.iterator();
			while(qualifyersIterator.hasNext())
			{	
				Qualifyer qualifyer = (Qualifyer)qualifyersIterator.next();
				qualifyersIterator.remove();
				serviceBinding.getBindingQualifyers().remove(qualifyer);
			}
			db.remove(serviceBinding);
		}
		
		results.close();
		oql.close();
	}       

	/**
	 * This method deletes a service binding an all associated qualifyers.
	 */
	
    public static void delete(ServiceBindingVO serviceBindingVO) throws ConstraintException, SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();
    	beginTransaction(db);
    	try
        {	
        	ServiceBinding serviceBinding = ServiceBindingController.getServiceBindingWithId(serviceBindingVO.getServiceBindingId(), db);
			//QualifyerController.deleteQualifyersForServiceBinding(serviceBinding, db);
			//deleteEntity(ServiceBindingImpl.class, serviceBindingVO.getServiceBindingId(), db);
        	SiteNodeVersion siteNodeVersion = serviceBinding.getSiteNodeVersion();
        	
        	db.remove(serviceBinding);
        	
        	siteNodeVersion.getServiceBindings().remove(serviceBinding);
        	
            RegistryController.getController().updateSiteNodeVersionThreaded(siteNodeVersion.getValueObject());

        	commitTransaction(db);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        	rollbackTransaction(db);
        	throw new SystemException("An error occurred when we tried to remove a serviceBinding and it's qualifyers.");
        }
    }       
    
	/**
	 * This method deletes a service binding an all associated qualifyers.
	 */
	
	public static void delete(ServiceBindingVO serviceBindingVO, Database db) throws ConstraintException, SystemException, Exception
	{
		ServiceBinding serviceBinding = ServiceBindingController.getServiceBindingWithId(serviceBindingVO.getServiceBindingId(), db);
		
		db.remove(serviceBinding);
	
        RegistryController.getController().updateSiteNodeVersionThreaded(serviceBinding.getSiteNodeVersion().getValueObject());
	}        

	
	/**
	 * This method returns a list with QualifyerVO-objects which are available for the
	 * serviceBinding sent in
	 */
	
	public static List getQualifyerVOList(Integer serviceBindingId) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        List qualifyerVOList = null;

        beginTransaction(db);

        try
        {
        	ServiceBinding serviceBinding = getReadOnlyServiceBindingWithId(serviceBindingId, db);
            Collection qualifyerList = serviceBinding.getBindingQualifyers();
        	qualifyerVOList = toVOList(qualifyerList);
        	
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

        return qualifyerVOList;
	}

	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new ServiceBindingVO();
	}

}
