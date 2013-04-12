/* ===============================================================================
 *
 * Part of the InfoGlue Properties Management Platform (www.infoglue.org)
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
 *
 * $Id: PropertiesCategoryController.java,v 1.3.4.4 2013/03/12 12:58:06 mattias Exp $
 */
package org.infoglue.cms.controllers.kernel.impl.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.ObjectNotFoundException;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Category;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.entities.management.GroupContentTypeDefinition;
import org.infoglue.cms.entities.management.GroupProperties;
import org.infoglue.cms.entities.management.PropertiesCategory;
import org.infoglue.cms.entities.management.PropertiesCategoryVO;
import org.infoglue.cms.entities.management.impl.simple.CategoryImpl;
import org.infoglue.cms.entities.management.impl.simple.PropertiesCategoryImpl;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.Timer;

/**
 * The PropertiesCategoryController manages all actions related to persistence
 * and querying for PropertiesCategory relationships.
 *
 * TODO: When we convert have Hibernate manage all of these relationships, it will pull it
 * TODO: all back with one query and be a helluva lot faster than this basic implementation
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class PropertiesCategoryController extends BaseController
{
    private final static Logger logger = Logger.getLogger(PropertiesCategoryController.class.getName());

	private static final PropertiesCategoryController instance = new PropertiesCategoryController();

	private static final String findByProperties = new StringBuffer("SELECT c ")
			.append("FROM org.infoglue.cms.entities.management.impl.simple.PropertiesCategoryImpl c ")
			.append("WHERE c.entityName = $1 AND c.entityId = $2").toString();

	private static final String findByPropertiesAttribute = new StringBuffer("SELECT c ")
			.append("FROM org.infoglue.cms.entities.management.impl.simple.PropertiesCategoryImpl c ")
			.append("WHERE c.attributeName = $1 ")
			.append("AND c.entityName = $2")
			.append("AND c.entityId = $3")
			.append("ORDER BY c.category.name").toString();

	private static final String findByCategory = new StringBuffer("SELECT c ")
			.append("FROM org.infoglue.cms.entities.management.impl.simple.PropertiesCategoryImpl c ")
			.append("WHERE c.category.categoryId = $1 ").toString();

	public static PropertiesCategoryController getController()
	{
		return instance;
	}

	private PropertiesCategoryController() {}

	/**
	 * Find a PropertiesCategory by it's identifier.
	 * @param	id The id of the Category to find
	 * @return	The CategoryVO identified by the provided id
	 * @throws	SystemException If an error happens
	 */
	public PropertiesCategoryVO findById(Integer id) throws SystemException
	{
		return (PropertiesCategoryVO)getVOWithId(PropertiesCategoryImpl.class, id);
	}

	/**
	 * Gets and precaches all propertycategory-objects.
	 */
	public void preCacheAllPropertiesCategoryVOList() throws SystemException, Exception
	{
		Database db = CastorDatabaseService.getDatabase();

        beginTransaction(db);

        try
        {
        	Timer t = new Timer();
        	Map<Integer,CategoryVO> categoriesMap = new HashMap<Integer,CategoryVO>();
    		OQLQuery oql1 = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.management.impl.simple.CategoryImpl c ORDER BY c.categoryId");

    		QueryResults results1 = oql1.execute(Database.ReadOnly);
    		while (results1.hasMore()) 
    		{
    			Category category = (Category)results1.next();
    			categoriesMap.put(category.getId(), category.getValueObject());
    		}

    		results1.close();
    		oql1.close();
    		logger.warn("Categories took: " + t.getElapsedTime());
        	//getCastorCategory().setLevel(Level.DEBUG);
    		//getCastorJDOCategory().setLevel(Level.DEBUG);
        	
    		OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.management.impl.simple.SmallPropertiesCategoryImpl c ORDER BY c.propertiesCategoryId");

    		QueryResults results = oql.execute(Database.ReadOnly);
    		while (results.hasMore()) 
    		{
    			PropertiesCategory propertiesCategory = (PropertiesCategory)results.next();
    			
    			String key = "categoryVOList_" + propertiesCategory.getAttributeName() + "_" + propertiesCategory.getEntityName() + "_" + propertiesCategory.getEntityId();
    			List<CategoryVO> categoryVOList = (List<CategoryVO>)CacheController.getCachedObject("propertiesCategoryCache", key);
    			if(categoryVOList == null)
    			{
    				categoryVOList = new ArrayList<CategoryVO>();
    				CacheController.cacheObject("propertiesCategoryCache", key, categoryVOList);
    			}
    			
    			if(propertiesCategory.getValueObject().getCategoryId() != null)
    			{
    				CategoryVO categoryVO = categoriesMap.get(propertiesCategory.getValueObject().getCategoryId());
    				if(categoryVO != null)
    					categoryVOList.add(categoryVO);
    				else
    					logger.info("An inconsistency found. The propertyCategory " + propertiesCategory.getId() + " pointed to a missing categoryID: " + propertiesCategory.getValueObject().getCategoryId());
    				/*
    				try
    				{
	    				CategoryVO categoryVO = CategoryController.getController().findById(propertiesCategory.getValueObject().getCategoryId(), db).getValueObject();
    					categoryVOList.add(categoryVO);
    				}
    				catch (Exception e) 
    				{
    					logger.error("An inconsistency found. The propertyCategory " + propertiesCategory.getId() + " pointed to a missing category:" + e.getMessage());
					}
					*/
    			}

    			/*
    			if(propertiesCategory.getCategory() != null)
    			{
    				categoryVOList.add(propertiesCategory.getCategory().getValueObject());
    				//System.out.println("Category was ok for: " + key);    				
    			}
    			//else
    				//System.out.println("Category was null for: " + key);
    			*/
    		}

        	//getCastorCategory().setLevel(Level.WARN);
    		//getCastorJDOCategory().setLevel(Level.WARN);

    		results.close();
    		oql.close();
    		
    		CacheController.cacheObject("propertiesCategoryCache", "allValuesCached", true);
    		
       		logger.warn("PropCategories took: " + t.getElapsedTime());
			            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred when we tried to fetch the list of PropertiesCategory:" + e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
	}
	
	/**
	 * Find a List of PropertiesCategories for the specific attribute and Properties Version.
	 * @param	attribute The attribute name of the PropertiesCategory to find
	 * @param	versionId The Properties Version id of the PropertiesCategory to find
	 * @return	A list of PropertiesCategoryVO that have the provided properties version and attribute
	 * @throws	SystemException If an error happens
	 */
	public List findByPropertiesAttribute(String attribute, String entityName, Integer entityId) throws SystemException
	{
		List params = new ArrayList();
		params.add(attribute);
		params.add(entityName);
		params.add(entityId);
	    return executeQuery(findByPropertiesAttribute, params);
	}

	/**
	 * Find a List of PropertiesCategories for the specific attribute and Properties Version.
	 * @param	attribute The attribute name of the PropertiesCategory to find
	 * @param	versionId The Properties Version id of the PropertiesCategory to find
	 * @return	A list of PropertiesCategoryVO that have the provided properties version and attribute
	 * @throws	SystemException If an error happens
	 */
	public List findByPropertiesAttribute(String attribute, String entityName, Integer entityId, Database db) throws SystemException
	{
		List params = new ArrayList();
		params.add(attribute);
		params.add(entityName);
		params.add(entityId);
	    return executeQuery(findByPropertiesAttribute, params, db);
	}

	/**
	 * Find a List of PropertiesCategories for the specific attribute and Properties Version.
	 * @param	attribute The attribute name of the PropertiesCategory to find
	 * @param	versionId The Properties Version id of the PropertiesCategory to find
	 * @return	A list of PropertiesCategoryVO that have the provided properties version and attribute
	 * @throws	SystemException If an error happens
	 */
	public List findByPropertiesAttributeReadOnly(String attribute, String entityName, Integer entityId, Database db) throws SystemException
	{
		List params = new ArrayList();
		params.add(attribute);
		params.add(entityName);
		params.add(entityId);
	    return executeQueryReadOnly(findByPropertiesAttribute, params, db);
	}

	/**
	 * Find a List of PropertiesCategories for a Properties Version.
	 * @param	versionId The Properties Version id of the PropertiesCategory to find
	 * @return	A list of PropertiesCategoryVO that have the provided properties version and attribute
	 * @throws	SystemException If an error happens
	 */
	public List findByProperties(String entityName, Integer entityId) throws SystemException
	{
		List params = new ArrayList();
		params.add(entityName);
		params.add(entityId);
		return executeQuery(findByProperties, params);
	}

	/**
	 * Find a List of PropertiesCategories for the specific attribute and Properties Version.
	 * @param	categoryId The Category id of the PropertiesCategory to find
	 * @return	A list of PropertiesCategoryVO that have the provided category id
	 * @throws	SystemException If an error happens
	 */
	public List findByCategory(Integer categoryId) throws SystemException
	{
		List params = new ArrayList();
		params.add(categoryId);
		return executeQuery(findByCategory, params);
	}

	/**
	 * Saves a PropertiesCategoryVO whether it is new or not.
	 * @param	c The PropertiesCategoryVO to save
	 * @return	The saved PropertiesCategoryVO
	 * @throws	SystemException If an error happens
	 */
	public PropertiesCategoryVO save(PropertiesCategoryVO c) throws SystemException
	{
		return c.isUnsaved() ? create(c) : (PropertiesCategoryVO)updateEntity(PropertiesCategoryImpl.class, c);
	}

	/**
	 * Creates a PropertiesCategory from a PropertiesCategoryVO
	 */
	private PropertiesCategoryVO create(PropertiesCategoryVO c) throws SystemException
	{
		Database db = beginTransaction();

		try
		{
			PropertiesCategory propertiesCategory = createWithDatabase(c, db);
			commitTransaction(db);
			return propertiesCategory.getValueObject();
		}
		catch (Exception e)
		{
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}

	public PropertiesCategory createWithDatabase(PropertiesCategoryVO c, Database db) throws SystemException, PersistenceException
	{
		// Need this crappy hack to forge the relationship (castor completely sucks like this)
		// TODO: When hibernate comes, just save the VOs and if it has a child VO with an id set
		// TODO: it is used to make the relationship...ask me for clarification -frank
		Category category = (Category)getObjectWithId(CategoryImpl.class, c.getCategory().getId(), db);

		PropertiesCategory propertiesCategory = new PropertiesCategoryImpl();
		propertiesCategory.setValueObject(c);
		propertiesCategory.setCategory((CategoryImpl)category);
		db.create(propertiesCategory);
		return propertiesCategory;
	}

	/**
	 * Deletes a PropertiesCategory
	 * @param	id The id of the PropertiesCategory to delete
	 * @throws	SystemException If an error happens
	 */
	public void delete(Integer id) throws SystemException
	{
		deleteEntity(PropertiesCategoryImpl.class, id);
	}

	/**
	 * Deletes all PropertiesCategories for a particular PropertiesVersion
	 * @param	versionId The id of the PropertiesCategory to delete
	 * @throws	SystemException If an error happens
	 */
	public void deleteByProperties(String entityName, Integer entityId) throws SystemException
	{
		delete(findByProperties(entityName, entityId));
	}

	/**
	 * Deletes all PropertiesCategories for a particular PropertiesVersion using the provided Database
	 * @param	versionId The id of the PropertiesCategory to delete
	 * @param	db The Database instance to use
	 * @throws	SystemException If an error happens
	 */
	public void deleteByPropertiesVersion(String entityName, Integer entityId, Database db) throws SystemException
	{
		delete(findByProperties(entityName, entityId), db);
	}

	/**
	 * Deletes all PropertiesCategories for a particular Category
	 * @param	categoryId The id of the PropertiesCategory to delete
	 * @throws	SystemException If an error happens
	 */
	public void deleteByCategory(Integer categoryId) throws SystemException
	{
		delete(findByCategory(categoryId));
	}

	/**
	 * Deletes all PropertiesCategories for a particular Category using the provided Database
	 * @param	categoryId The id of the Category to delete
	 * @param	db The Database instance to use
	 * @throws	SystemException If an error happens
	 */
	public void deleteByCategory(Integer categoryId, Database db) throws SystemException
	{
		delete(findByCategory(categoryId), db);
	}

	/**
	 * Deletes all properties categories with a specific attribute for a specific properties version within a single transaction
	 * @param attribute the desired attribute
	 * @param versionId the ID of the desired properties version
	 * @throws SystemException if a database error occurs
	 */
	public void deleteByPropertiesVersionAttribute(String attribute, String entityName, Integer entityId) throws SystemException
	{
		delete(findByPropertiesAttribute(attribute, entityName, entityId));
	}

	/**
	 * Deletes all properties categories with a specific attribute for a specific properties version using the given database
	 * @param attribute the desired attribute
	 * @param versionId the ID of the desired properties version
	 * @param db the database defining the transaction context for this delete
	 * @throws SystemException if a database error occurs
	 */
	public void deleteByPropertiesVersionAttribute(String attribute, String entityName, Integer entityId, Database db) throws SystemException
	{
		delete(findByPropertiesAttribute(attribute, entityName, entityId), db);
	}

	/**
	 * Deletes a collection of properties categories within a single transaction
	 * @param propertiesCategories a collection of PropertiesCategoryVOs to delete
	 * @throws SystemException if a database error occurs
	 */
	private static void delete(Collection propertiesCategories) throws SystemException
	{
		Database db = beginTransaction();

		try
		{
			delete(propertiesCategories, db);
			commitTransaction(db);
		}
		catch (Exception e)
		{
			rollbackTransaction(db);
			throw new SystemException(e);
		}
	}

	/**
	 * Deletes a collection of properties categories using the given database
	 * @param propertiesCategories a collection of PropertiesCategoryVOs to delete
	 * @param db the database to be used for the delete
	 * @throws SystemException if a database error occurs
	 */
	private static void delete(Collection propertiesCategories, Database db) throws SystemException
	{
		for (Iterator i = propertiesCategories.iterator(); i.hasNext();)
			deleteEntity(PropertiesCategoryImpl.class, ((PropertiesCategoryVO)i.next()).getId(), db);
	}

	/**
	 * Implemented for BaseController
	 */
	public BaseEntityVO getNewVO()
	{
		return new PropertiesCategoryVO();
	}
}
