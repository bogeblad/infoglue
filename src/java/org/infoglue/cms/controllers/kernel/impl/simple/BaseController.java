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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.LockNotGrantedException;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.QueryResults;
import org.exolab.castor.jdo.TransactionAbortedException;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.kernel.IBaseEntity;
import org.infoglue.cms.entities.kernel.ValidatableEntityVO;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.InterceptorVO;
import org.infoglue.cms.entities.management.TableCount;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.interceptors.InfoGlueInterceptor;
import org.infoglue.cms.services.InterceptionService;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.StringManager;
import org.infoglue.cms.util.StringManagerFactory;
import org.infoglue.cms.util.validators.Constants;
import org.infoglue.cms.util.validators.ConstraintRule;
import org.infoglue.cms.util.validators.EmailValidator;
import org.infoglue.cms.util.validators.StringValidator;
import org.infoglue.deliver.util.CacheController;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
 * BaseController.java
 * Created on 2002-aug-28 
 * @author Stefan Sik, ss@frovi.com 
 * @author Mattias Bogeblad, mattias.bogeblad@sprawlsolutions.se
 * 
 * Baseclass for ControllerClasses.
 * Various methods to load, create and delete entities
 * 
 * TODO:
 * Now that all entities implements BaseEntity clear all reflection and simplify
 * arguments...
 * 
 * -matbog 2002-09-15: Added and modified new read-only methods for fetching a VO-object. 
 * 					   These method must be called instead of the old ones when just fetching a entity
 * 					   or all entities from a table.
 */

public abstract class BaseController
{
    private final static Logger logger = Logger.getLogger(BaseController.class.getName());

    /**
     * Gets a logger for the action class.
     */
/*
	protected Logger logger 
	{
	    return Logger.getLogger(this.getClass().getName());
	}
*/
    /**
     * This method is called by the controllers to let interceptors listen to events.
     * 
     * @param hashMap
     * @param InterceptionPointName
     * @param infogluePrincipal
     * @throws ConstraintException
     * @throws SystemException
     * @throws Bug
     * @throws Exception
     */
    protected void intercept(Map hashMap, String InterceptionPointName, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException, Bug, Exception
	{
    	intercept(hashMap, InterceptionPointName, infogluePrincipal, true);
	}
	
    protected void intercept(Map hashMap, String InterceptionPointName, InfoGluePrincipal infogluePrincipal, boolean allowCreatorAccess) throws ConstraintException, SystemException, Bug, Exception
	{
		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName(InterceptionPointName);
    	
		if(interceptionPointVO == null)
			throw new SystemException("The InterceptionPoint " + InterceptionPointName + " was not found. The system will not work unless you restore it.");

		List interceptors = InterceptorController.getController().getInterceptorsVOList(interceptionPointVO.getInterceptionPointId());
		Iterator interceptorsIterator = interceptors.iterator();
		while(interceptorsIterator.hasNext())
		{
			InterceptorVO interceptorVO = (InterceptorVO)interceptorsIterator.next();
			logger.info("Adding interceptorVO:" + interceptorVO.getName());
			try
			{
				InfoGlueInterceptor infoGlueInterceptor = InterceptionService.getService().getInterceptor(interceptorVO.getName());
				if(infoGlueInterceptor == null)
					infoGlueInterceptor = (InfoGlueInterceptor)Class.forName(interceptorVO.getClassName()).newInstance();
				infoGlueInterceptor.intercept(infogluePrincipal, interceptionPointVO, hashMap, allowCreatorAccess);
			}
			catch(ClassNotFoundException e)
			{
				logger.warn("The interceptor " + interceptorVO.getClassName() + "was not found: " + e.getMessage(), e);
			}
		}
	}

    
    /**
     * This method is called by the controllers to let interceptors listen to events.
     * 
     * @param hashMap
     * @param InterceptionPointName
     * @param infogluePrincipal
     * @throws ConstraintException
     * @throws SystemException
     * @throws Bug
     * @throws Exception
     */

    protected void intercept(Map hashMap, String InterceptionPointName, InfoGluePrincipal infogluePrincipal, Database db) throws ConstraintException, SystemException, Bug, Exception
	{
		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName(InterceptionPointName, db);
    	
		if(interceptionPointVO == null)
			throw new SystemException("The InterceptionPoint " + InterceptionPointName + " was not found. The system will not work unless you restore it.");

		List interceptors = InterceptorController.getController().getInterceptorsVOList(interceptionPointVO.getInterceptionPointId(), db);
		Iterator interceptorsIterator = interceptors.iterator();
		while(interceptorsIterator.hasNext())
		{
			InterceptorVO interceptorVO = (InterceptorVO)interceptorsIterator.next();
			logger.info("Adding interceptorVO:" + interceptorVO.getName());
			try
			{
				InfoGlueInterceptor infoGlueInterceptor = InterceptionService.getService().getInterceptor(interceptorVO.getName());
				if(infoGlueInterceptor == null)
					infoGlueInterceptor = (InfoGlueInterceptor)Class.forName(interceptorVO.getClassName()).newInstance();
				infoGlueInterceptor.intercept(infogluePrincipal, interceptionPointVO, hashMap, db);
			}
			catch(ClassNotFoundException e)
			{
				logger.warn("The interceptor " + interceptorVO.getClassName() + "was not found: " + e.getMessage(), e);
			}
		}

	}

    protected void intercept(Map hashMap, String InterceptionPointName, InfoGluePrincipal infogluePrincipal, boolean allowCreatorAccess, Database db) throws ConstraintException, SystemException, Bug, Exception
	{
		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName(InterceptionPointName, db);
    	
		if(interceptionPointVO == null)
			throw new SystemException("The InterceptionPoint " + InterceptionPointName + " was not found. The system will not work unless you restore it.");

		List interceptors = InterceptorController.getController().getInterceptorsVOList(interceptionPointVO.getInterceptionPointId(), db);
		Iterator interceptorsIterator = interceptors.iterator();
		while(interceptorsIterator.hasNext())
		{
			InterceptorVO interceptorVO = (InterceptorVO)interceptorsIterator.next();
			logger.info("Adding interceptorVO:" + interceptorVO.getName());
			try
			{
				InfoGlueInterceptor infoGlueInterceptor = InterceptionService.getService().getInterceptor(interceptorVO.getName());
				if(infoGlueInterceptor == null)
					infoGlueInterceptor = (InfoGlueInterceptor)Class.forName(interceptorVO.getClassName()).newInstance();
				infoGlueInterceptor.intercept(infogluePrincipal, interceptionPointVO, hashMap, allowCreatorAccess, db);
			}
			catch(ClassNotFoundException e)
			{
				logger.warn("The interceptor " + interceptorVO.getClassName() + "was not found: " + e.getMessage(), e);
			}
		}

	}

	
	private static Integer getEntityId(Object entity) throws Bug
	{
		Integer entityId = new Integer(-1);
		
		try 
		{
			entityId = ((IBaseEntity) entity).getId();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Bug("Unable to retrieve object id");
		}
		
		/*
		try {
			entityId = (Integer) entity.getClass().getDeclaredMethod("getId", new Class[0]).invoke(entity, new Object[0]);
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		} catch (NoSuchMethodException e) {
		}

		*/		
		return entityId;
	}

	/*************************************************** 
	 * Create, Delete & Update operations
	 ***************************************************/

	// Create entity
	// The validation belongs here
    protected static Object createEntity(Object entity) throws SystemException, Bug
    {
        Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);

        try
        {
            db.create(entity);
            commitTransaction(db);
            //CmsSystem.log(entity,"Created object", CmsSystem.DBG_NORMAL);
			//CmsSystem.transactionLogEntry(entity.getClass().getName(), CmsSystem.TRANS_CREATE, getEntityId(entity), entity.toString());
            
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            //CmsSystem.log(entity,"Failed to create object", CmsSystem.DBG_LOW);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        return entity;
    }     


	// Create entity inside an existing transaction
    protected static Object createEntity(Object entity, Database db) throws SystemException, Bug, Exception
    {
        db.create(entity);
        return entity;
    }     
/*
    protected static Object createEntity(Object entity, Database db) throws SystemException, Bug
    {
        try
        {
            db.create(entity);
            commitTransaction(db);
            //CmsSystem.log(entity,"Created object", CmsSystem.DBG_NORMAL);
			//CmsSystem.transactionLogEntry(entity.getClass().getName(), CmsSystem.TRANS_CREATE, getEntityId(entity), entity.toString());   
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            //CmsSystem.log(entity,"Failed to create object", CmsSystem.DBG_LOW);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        return entity;
    }     
*/

	// Delete entity
    public static void deleteEntity(Class entClass, Integer id) throws Bug, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        Object entity = null;

        beginTransaction(db);

        try
        {
            entity = getObjectWithId(entClass, id, db);
            
            // Delete the entity
            db.remove(entity);
            commitTransaction(db);
            //CmsSystem.log(entity,"Deleted object", CmsSystem.DBG_NORMAL);           
			//CmsSystem.transactionLogEntry(entClass.getName(), CmsSystem.TRANS_DELETE, id, entity.toString());            
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }        


	// Delete entity
	public static void deleteEntity(Class entClass, String id) throws Bug, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		Object entity = null;

		beginTransaction(db);

		try
		{
			entity = getObjectWithId(entClass, id, db);
            
			// Delete the entity
			db.remove(entity);
			commitTransaction(db);
			//CmsSystem.log(entity,"Deleted object", CmsSystem.DBG_NORMAL);           
			//CmsSystem.transactionLogEntry(entClass.getName(), CmsSystem.TRANS_DELETE, id, entity.toString());            
		}
		catch(Exception e)
		{
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}    


	// Delete entity
	public static void deleteEntity(Class entClass, String id, Database db) throws Bug, SystemException, Exception
	{
		Object entity = getObjectWithId(entClass, id, db);
		// Delete the entity
		db.remove(entity);
	}    

	
	// Delete entity
    public static void deleteEntity(Class entClass, Integer id, Database db) throws Bug, SystemException
    {
        Object entity = null;

        try
        {
            entity = getObjectWithId(entClass, id, db);
            // Delete the entity
            db.remove(entity);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            throw new SystemException(e.getMessage());
        }
    }        


    public static BaseEntityVO updateEntity(Class arg, BaseEntityVO vo) throws Bug, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();

        IBaseEntity entity = null;

        beginTransaction(db);

        try
        {
            entity = (IBaseEntity) getObjectWithId(arg, vo.getId(), db);
            entity.setVO(vo);

            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return entity.getVO();
    }        

    
	public static BaseEntityVO updateEntity(Class arg, BaseEntityVO vo, Database db) throws Bug, SystemException
	{
		IBaseEntity entity = null;

		entity = (IBaseEntity) getObjectWithId(arg, vo.getId(), db);
		entity.setVO(vo);

		return entity.getVO();
	}        

	
	/* Update entity and a collection with other entities
	 * Experimental, use with caution
	 * 
	 */
    public static BaseEntityVO updateEntity(Class entClass, BaseEntityVO vo, String collectionMethod, Class manyClass, String[] manyIds) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        IBaseEntity entity = null;

        beginTransaction(db);

        try
        {
            //add validation here if needed
            List manyList = new ArrayList();
            if(manyIds != null)
			{
		        for (int i=0; i < manyIds.length; i++)
	            {
	            	IBaseEntity manyEntity = (IBaseEntity) getObjectWithId(manyClass, new Integer(manyIds[i]), db);
	            	logger.info("!!Using experimental code: BaseController::update. getting " + manyEntity.toString());
	            	manyList.add(manyEntity);
	            }
			}
			
		
            entity = (IBaseEntity) getObjectWithId(entClass, vo.getId(), db);
            entity.setVO(vo);
            
            // Now reflect to set the collection
            Object[] arg = {manyList};
            Class[] parm = {Collection.class};
            entity.getClass().getDeclaredMethod(collectionMethod, parm).invoke(entity, arg);
						
			// DONE
			
            //If any of the validations or setMethods reported an error, we throw them up now before create.
            ceb.throwIfNotEmpty();
            
            commitTransaction(db);
            //CmsSystem.transactionLogEntry(entity.getClass().getName(), CmsSystem.TRANS_UPDATE, vo.getId(), entity.toString());

        }
        catch(ConstraintException ce)
        {
            logger.warn("An error occurred so we should not complete the transaction:" + ce, ce);
            rollbackTransaction(db);
            throw ce;
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return entity.getVO();
    }        

	/* 
	 * Update entity and a collection with other entities
	 * Experimental, use with caution
	 */
    public static IBaseEntity updateEntity(Class entClass, BaseEntityVO vo, String collectionMethod, Class manyClass, String[] manyIds, Database db) throws ConstraintException, SystemException, Exception
    {
        IBaseEntity entity = null;

        List manyList = new ArrayList();
        if(manyIds != null)
		{
	        for (int i=0; i < manyIds.length; i++)
            {
            	IBaseEntity manyEntity = (IBaseEntity) getObjectWithId(manyClass, new Integer(manyIds[i]), db);
            	logger.info("!!Using experimental code: BaseController::update. getting " + manyEntity.toString());
            	manyList.add(manyEntity);
            }
		}
		
        entity = (IBaseEntity) getObjectWithId(entClass, vo.getId(), db);
        entity.setVO(vo);
        
        // Now reflect to set the collection
        Object[] arg = {manyList};
        Class[] parm = {Collection.class};
        entity.getClass().getDeclaredMethod(collectionMethod, parm).invoke(entity, arg);
						
        return entity;
    }        


	/*
	protected static Object getObjectWithId(Class arg, Integer id) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();		
		Object ret = null;
		try
		{
			beginTransaction(db);
			ret = getObjectWithId(arg, id, db);
			commitTransaction(db);
		}
		catch (Exception e)
		{
			rollbackTransaction(db);
            throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
		}
		return ret;
	}
	*/

	/**
	 * This method fetches one object / entity within a transaction.
	 **/
	
    protected static Object getObjectWithId(Class arg, Integer id, Database db) throws SystemException, Bug
    {
        Object object = null;
        try
        {
            if(logger.isInfoEnabled())
            	logger.info("Loading " + arg + " in read/write mode.");
            object = db.load(arg, id);
        }
        catch(Exception e)
        {
            throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
        }
    
        if(object == null)
        {
            throw new Bug("The object with id [" + id + "] was not found. This should never happen.");
        }
    	return object;
    }


	/**
	 * This method fetches one object / entity within a transaction.
	 **/
	
    protected static Object getObjectWithIdAsReadOnly(Class arg, Integer id, Database db) throws SystemException, Bug
    {
        Object object = null;
        try
        {
            object = db.load(arg, id, Database.ReadOnly);    			
        }
        catch(Exception e)
        {
            throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
        }
    
        if(object == null)
        {
            throw new Bug("The object with id [" + id + "] was not found. This should never happen.");
        }
    	return object;
    }

	/**
	 * This method fetches one object / entity within a transaction.
	 **/
	
	protected static Object getObjectWithId(Class arg, String id, Database db) throws SystemException, Bug
	{
		Object object = null;
		try
		{
            logger.info("Loading " + arg + " in read/write mode.");

            object = db.load(arg, id);
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
		}
    
		if(object == null)
		{
			throw new Bug("The object with id [" + id + "] was not found. This should never happen.");
		}
		return object;
	}

	    
  	/**
	 * This method converts a List of entities to a list of value-objects.
	 */
	
	public static List toVOList(Collection baseEntities) throws SystemException, Bug
    {
		List resultVOList = new ArrayList();
		
        if(baseEntities != null)
    	{
			Object o = null;
		    try
			{
				Iterator iterator = baseEntities.iterator();
				while (iterator.hasNext()) 
		        {
					o = (Object)iterator.next();
					// Om metoden getValueObject saknas, kastas ett undantag.            	
	                resultVOList.add(o.getClass().getDeclaredMethod("getValueObject", new Class[0]).invoke(o, new Object[0]));
		        }
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
	 * This method converts a List of entities to a list of value-objects.
	 */
	
	public static List toModifiableVOList(Collection baseEntities) throws SystemException, Bug
    {
		List resultVOList = new ArrayList();
		
        if(baseEntities != null)
    	{
			Object o = null;
		    try
			{
				Iterator iterator = baseEntities.iterator();
				while (iterator.hasNext()) 
		        {
					o = (Object)iterator.next();
					// Om metoden getValueObject saknas, kastas ett undantag.            	
	                resultVOList.add(o.getClass().getDeclaredMethod("getValueObject", new Class[0]).invoke(o, new Object[0]));
		        }
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

	/*************************************************** 
	 * Read only operations
	 ***************************************************/

	/**
	 * This method is used to fetch a ValueObject from the database.
	 */

	public static Object getVOWithId(Class arg, Integer id) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();		
		Object ret = null;
		try
		{
			beginTransaction(db);
			ret = getVOWithId(arg, id, db);
			commitTransaction(db);
		}
		catch (Exception e)
		{
			rollbackTransaction(db);
            throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
		}
		return ret;
	}

	/**
	 * This method fetches one object in read only mode and returns it's value object.
	 */
	
    public static BaseEntityVO getVOWithId(Class arg, Integer id, Database db) throws SystemException, Bug
    {
        IBaseEntity vo = null;
        try
        {
    		vo = (IBaseEntity)db.load(arg, id, Database.ReadOnly);
        }
        catch(Exception e)
        {
            throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
        }
    
        if(vo == null)
        {
            throw new Bug("The object with id [" + id + "] was not found. This should never happen.");
        }
        
    	return vo.getVO();
    }
    
    
	/**
	 * This method is used to fetch a ValueObject from the database.
	 */

	public static Object getVOWithId(Class arg, String id) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();		
		Object ret = null;
		try
		{
			beginTransaction(db);
			ret = getVOWithId(arg, id, db);
			commitTransaction(db);
		}
		catch (Exception e)
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
		}
		return ret;
	}
	
	/**
	 * This method fetches one object in read only mode and returns it's value object.
	 */
	
	public static BaseEntityVO getVOWithId(Class arg, String id, Database db) throws SystemException, Bug
	{
		IBaseEntity vo = null;
		try
		{
			vo = (IBaseEntity)db.load(arg, id, Database.ReadOnly);
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch the object " + arg.getName() + ". Reason:" + e.getMessage(), e);    
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
    /*
    public static List getAllVOObjects(Class arg) throws SystemException, Bug
    {
		Database db = CastorDatabaseService.getDatabase();
		List ret = null;
        try
        {
			beginTransaction(db);
			ret = getAllVOObjects(arg, db);
			commitTransaction(db);
        }
        catch(Exception e)
        {
			rollbackTransaction(db);
            throw new SystemException("An error occurred when we tried to fetch " + arg.getName() + " Reason:" + e.getMessage(), e);    
        }    
        return ret;
    }
    */
    
	/**
	 * This method fetches all object in read only mode and returns a list of value objects.
	 */
    
	public static List getAllVOObjects(Class arg, String orderByAttribute, String direction) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();
		List ret = null;
		try
		{
			beginTransaction(db);
			ret = getAllVOObjects(arg, orderByAttribute, direction, db);
			commitTransaction(db);
		}
		catch(Exception e)
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch " + arg.getName() + " Reason:" + e.getMessage(), e);    
		}    
		return ret;
	}


    
	/**
	 * This method fetches all object in read only mode and returns a list of value objects.
	 */

	public static List getAllVOObjects(Class arg, String orderByField, String direction, Database db) throws SystemException, Bug
	{
		ArrayList resultList = new ArrayList();
		OQLQuery	oql;
		try
		{
        	
			if(logger.isInfoEnabled())
				logger.info("BaseHelper::GetAllObjects for " + arg.getName());
			oql = db.getOQLQuery( "SELECT u FROM " + arg.getName() + " u ORDER BY u." + orderByField + " " + direction);
			QueryResults results = oql.execute(Database.ReadOnly);
			
			while (results.hasMore()) 
			{
				Object o = results.next();

				// Om metoden getValueObject saknas, kastas ett undantag.            	
				resultList.add(o.getClass().getDeclaredMethod("getValueObject", new Class[0]).invoke(o, new Object[0]));
			}
		}
		catch(NoSuchMethodException e)
		{
			throw new Bug("The object [" + arg.getName() + "] is of the wrong type. This should never happen.", e);
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch " + arg.getName() + " Reason:" + e.getMessage(), e);    
		}    

		return resultList;
	}
    
	/**
	 * This method fetches all object in read only mode and returns a list of value objects sorted on primary Key.
	 */
	 
	public List getAllVOObjects(Class arg, String primaryKey) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();
		List ret = null;
		try
		{
			beginTransaction(db);
			ret = getAllVOObjects(arg, primaryKey, db);
			commitTransaction(db);
		}
		catch(Exception e)
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch " + arg.getName() + " Reason:" + e.getMessage(), e);    
		}    
		return ret;
	}


	/**
	 * This method fetches all object in read only mode and returns a list of value objects.
	 */

	public List getAllVOObjects(Class arg, String primaryKey, Database db) throws SystemException, Bug
	{
		ArrayList resultList = new ArrayList();
		OQLQuery	oql;
		try
		{
			oql = db.getOQLQuery( "SELECT u FROM " + arg.getName() + " u ORDER BY u." + primaryKey);
			QueryResults results = oql.execute(Database.ReadOnly);
			
			while (results.hasMore()) 
			{
				IBaseEntity baseEntity = (IBaseEntity)results.next();
				resultList.add(baseEntity.getVO());
			}
		}
		catch(ClassCastException e)
		{
			throw new Bug("The object [" + arg.getName() + "] is of the wrong type. This should never happen.", e);
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch " + arg.getName() + " Reason:" + e.getMessage(), e);    
		}    

		return resultList;
	}

	/**
	 * This method fetches all object in read only mode and returns a list of objects.
	 */

	public List getAllObjects(Class arg, String primaryKey, Database db) throws SystemException, Bug
	{
		ArrayList resultList = new ArrayList();
		OQLQuery	oql;
		try
		{
			oql = db.getOQLQuery( "SELECT u FROM " + arg.getName() + " u ORDER BY u." + primaryKey);
			QueryResults results = oql.execute(Database.ReadOnly);
			
			while (results.hasMore()) 
			{
				IBaseEntity baseEntity = (IBaseEntity)results.next();
				resultList.add(baseEntity);
			}
		}
		catch(ClassCastException e)
		{
			throw new Bug("The object [" + arg.getName() + "] is of the wrong type. This should never happen.", e);
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch " + arg.getName() + " Reason:" + e.getMessage(), e);    
		}    

		return resultList;
	}

	//---------------------------------------------------------------------
	// Dynamic Query specific operations
	//---------------------------------------------------------------------
	/**
	 * Executes a Query with no parameters
	 *
 	 * @param query An OQL Query
	 * @return A VO list of the query results
	 * @throws SystemException If an error occurs
	 */
	protected static List executeQuery(String query) throws SystemException
	{
		return executeQuery(query, Collections.EMPTY_LIST);
	}

	/**
	 * Executes a Query with no parameters
	 *
 	 * @param query An OQL Query
	 * @return A VO list of the query results
	 * @throws SystemException If an error occurs
	 */
	protected static List executeQuery(String query, Database db) throws SystemException
	{
		return executeQuery(query, Collections.EMPTY_LIST, db);
	}

	/**
	 * Executes a Query, also binds the provided parameters
	 *
 	 * @param query An OQL Query
	 * @param params A List of paramters
	 * @return A VO list of the query results
	 * @throws SystemException If an error occurs
	 */
	protected static List executeQuery(String query, List params) throws SystemException
	{
		Database db = beginTransaction();

		try
		{
			List results = new ArrayList();
			results = Collections.list(createQuery(db, query, params).execute(Database.ReadOnly));
			commitTransaction(db);
			return toVOList(results);
		}
		catch (Exception e)
		{
			logger.error("Error executing " + query, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage(), e);
		}
	}

	
	/**
	 * Executes a Query, also binds the provided parameters
	 *
 	 * @param query An OQL Query
	 * @param params A List of paramters
	 * @param db A transaction object
	 * @return A VO list of the query results
	 * @throws SystemException If an error occurs
	 */
	protected static List executeQuery(String query, List params, Database db) throws SystemException
	{
	    try
		{
			List resultList = new ArrayList();
			
			OQLQuery oql = createQuery(db, query, params);
			QueryResults results = oql.execute();
			resultList = Collections.list(results);

			results.close();
			oql.close();

			return resultList;
		}
		catch (Exception e)
		{
			logger.error("Error executing " + query, e);
			throw new SystemException(e.getMessage(), e);
		}
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


	/***************************************************
	 * Validation and integrity check of entities - cre 2002-09-18 / SS
	 * *************************************************/

    public static ConstraintExceptionBuffer validateEntity(ValidatableEntityVO vo)
    {
    	// This method loops through the rulelist and creates
    	// validators according to the settings in each rule.
    	// The old validators are used to do the actual validation
    	// but I have changed them to use less constructor
    	// parameter passing in favour for setters.
    	    	
    	//CmsSystem.log("ValidationController::validate()", CmsSystem.DBG_HIGH);
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		
		// Prepare the object for validation
		vo.PrepareValidation();
		
		// Loop through rules and create validators
    	Iterator iterator = vo.getConstraintRules().iterator();
    	while (iterator.hasNext()) 
    	{
    		ConstraintRule cr = (ConstraintRule) iterator.next();
    		Integer intId = vo.getId();
    		logger.info("Validating object id: " + intId);

			// an ugly switch for now.    		
    		switch (cr.getConstraintType())
    		{
    			case Constants.EMAIL:
    			{
					if (cr.getValue() != null)
					{
						// Create validator
	    				EmailValidator v = new EmailValidator(cr.getFieldName());
	    				
	    				// Set properties
	    				v.setObjectClass(vo.getConstraintRuleList().getEntityClass());
	    				v.setRange(cr.getValidRange());
	    				v.setIsRequired(cr.required);
	    				v.setMustBeUnique(cr.unique);
	    				v.setExcludeId(intId);

						// Do the limbo
	    				v.validate((String) cr.getValue(), ceb);
	    				
	    				// <todo>
	    				// Note: the actual value validated should be extracted
	    				// from the vo using the fieldname with reflection.
	    				// </todo>
	    				
					}		 	    	 
    				break;
    			}
				case Constants.STRING:
    			{
					if (cr.getValue() != null)
					{    				
	    				StringValidator v = new StringValidator(cr.getFieldName());
	    				v.setObjectClass(vo.getConstraintRuleList().getEntityClass());
	    				v.setRange(cr.getValidRange());
	    				v.setIsRequired(cr.required);
	    				v.setMustBeUnique(cr.unique);
	    				v.setExcludeId(intId);

	    				v.validate((String) cr.getValue(), ceb);
					}		 	    	 
    				break;
    			}
    			case Constants.FLOAT:
    			{
    				break;
    			}
    			case Constants.INTEGER:
    			{
    				break;
    			}
    			case Constants.PROPERNOUN:
    			{
    				break;
    			}
    			
    		} // switch
    		    		
    	} // while
				
		return ceb;
    }



	/*************************************************** 
	 * Transaction specifik operations
	 ***************************************************/

	/**
	 * Creates a new database and starts a transaction
	 * @return A reference to a castor database with a new transaction
	 * @throws SystemException if a database error occurs.
	 */
	protected static Database beginTransaction() throws SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		beginTransaction(db);
		return db;
	}

    /**
     * Begins a transaction on the named database
     */
         
    protected static void beginTransaction(Database db) throws SystemException
    {
        try
        {
            //logger.info("Opening a new Transaction in cms...");
            db.begin();
        }
        catch(Exception e)
        {
			throw new SystemException("An error occurred when we tried to begin an transaction. Reason:" + e.getMessage(), e);    
        }
    }
       
    /**
     * Ends a transaction on the named database
     */
     
    protected static void commitTransaction(Database db) throws SystemException
    {
        try
        {
            //logger.info("Closing a transaction in cms...");

            db.commit();
		    db.close();
        }
        catch(TransactionAbortedException tae)
        {
        	if(tae.getCause() instanceof LockNotGrantedException)
                throw new SystemException("The resource you tried to modify have just been updated by another user. Please try again later. System message: " + tae.getCause().getMessage());
        	else
               	throw new SystemException("An error occurred when we tried to commit an transaction. Reason:" + tae.getMessage(), tae);
        }
        catch(LockNotGrantedException lnge)
        {
            throw new SystemException("The resource you tried to modify have just been updated by another user. Please try again later. System message: " + lnge.getMessage());
        }
        catch(Exception e)
        {
           	throw new SystemException("An error occurred when we tried to commit an transaction. Reason:" + e.getMessage(), e);    
        }
    }
 
 
    /**
     * Rollbacks a transaction on the named database
     */
     
    protected static void rollbackTransaction(Database db) throws SystemException
    {
        try
        {
            //logger.info("rollbackTransaction a transaction in cms...");
            
            if (db != null && db.isActive())
        	{
                db.rollback();
				db.close();
        	}
        }
        catch(Exception e)
        {
            logger.warn("An error occurred when we tried to rollback an transaction. Reason:" + e.getMessage());
        }
    }

    /**
     * Rollbacks a transaction on the named database
     */
     
    protected static void closeDatabase(Database db) throws SystemException
    {
        try
        {
            if (db != null)
        	{
				db.close();
        	}
        }
        catch(Exception e)
        {
            logger.warn("An error occurred when we tried to rollback an transaction. Reason:" + e.getMessage());
        }
    }

	public String getLocalizedString(Locale locale, String key) 
  	{
    	StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);

    	return stringManager.getString(key);
  	}

	public String getLocalizedString(Locale locale, String key, Object arg1) 
  	{
    	StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);

    	return stringManager.getString(key, arg1);
  	}

	public String getLocalizedString(Locale locale, String key, Object arg1, Object arg2) 
  	{
    	StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);

    	return stringManager.getString(key, arg1, arg2);
  	}

	public String getLocalizedString(Locale locale, String key, Object arg1, Object arg2, Object arg3) 
  	{
    	StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);

    	return stringManager.getString(key, arg1, arg2, arg3);
  	}

	public Locale getUserPrefferedLocale(String userName)
	{
		Locale locale = Locale.ENGLISH;
		
		if(userName != null)
		{
			try
			{
			    Map args = new HashMap();
			    args.put("globalKey", "infoglue");
			    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
			    
			    String languageCode = ps.getString("principal_" + userName + "_languageCode");
			    locale = getLocaleWithCode(languageCode);
			}
			catch (Exception e) 
			{
				logger.warn("Error getting users prefferred language: " + e.getMessage(), e);
			}
		}
		
		return locale;
	}

	/**
	 * This method returns language with the languageCode sent in. 
	 */
	
	public Locale getLocaleWithCode(String languageCode)
	{
		String key = "" + languageCode;
		logger.info("key:" + key);
		Locale locale = (Locale)CacheController.getCachedObject("localeCache", key);
		if(locale != null)
		{
			logger.info("There was an cached locale:" + locale);
		}
		else
		{
			locale = Locale.getDefault();
			
			if (languageCode != null)
			{
				try 
				{
					locale = new Locale(languageCode);
				} 
				catch (Exception e) 
				{
					logger.error("An error occurred in getLocaleWithCode: getting locale with languageCode:" + languageCode + "," + e, e);
				}	
			}
			
			CacheController.cacheObject("localeCache", key, locale);				
		}
		
		return locale; 
	}

	public static TableCount getTableCount(String tableName) throws Exception
	{
		TableCount tableCount = null;
		
		Database db = CastorDatabaseService.getDatabase();

        beginTransaction(db);

        try
        {
        	OQLQuery oql = db.getOQLQuery("CALL SQL SELECT count(*) FROM " + tableName + " AS org.infoglue.cms.entities.management.TableCount");

        	QueryResults results = oql.execute();
    		if(results.hasMore()) 
            {
    			tableCount = (TableCount)results.next();
    		}

    		results.close();
    		oql.close();
        	
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return tableCount;
	}


	public abstract BaseEntityVO getNewVO();
}