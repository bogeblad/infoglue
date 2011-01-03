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
 *
 * $Id: ContentCategoryController.java,v 1.23 2010/09/07 08:05:43 mattias Exp $
 */
package org.infoglue.cms.controllers.kernel.impl.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.PersistenceException;
import org.infoglue.cms.entities.content.ContentCategory;
import org.infoglue.cms.entities.content.ContentCategoryVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.impl.simple.ContentCategoryImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Category;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.entities.management.impl.simple.CategoryImpl;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * The ContentCategoryController manages all actions related to persistence
 * and querying for ContentCategory relationships.
 *
 * TODO: When we convert have Hibernate manage all of these relationships, it will pull it
 * TODO: all back with one query and be a helluva lot faster than this basic implementation
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */

public class ContentCategoryController extends BaseController
{
    private final static Logger logger = Logger.getLogger(ContentCategoryController.class.getName());

	private static final ContentCategoryController instance = new ContentCategoryController();

	private static final String findByContentVersion = new StringBuffer("SELECT c ")
			.append("FROM org.infoglue.cms.entities.content.impl.simple.ContentCategoryImpl c ")
			.append("WHERE c.contentVersion.contentVersionId = $1").toString();

	private static final String findByContentVersionAttribute = new StringBuffer("SELECT c ")
			.append("FROM org.infoglue.cms.entities.content.impl.simple.ContentCategoryImpl c ")
			.append("WHERE c.attributeName = $1 ")
			.append("AND c.contentVersion.contentVersionId = $2")
			.append("ORDER BY c.category.name").toString();

	private static final String findByCategory = new StringBuffer("SELECT c ")
			.append("FROM org.infoglue.cms.entities.content.impl.simple.ContentCategoryImpl c ")
			.append("WHERE c.category.categoryId = $1 ").toString();

	public static ContentCategoryController getController()
	{
		return instance;
	}

	private ContentCategoryController() {}

	/**
	 * Find a ContentCategory by it's identifier.
	 * @param	id The id of the Category to find
	 * @return	The CategoryVO identified by the provided id
	 * @throws	SystemException If an error happens
	 */
	public ContentCategoryVO findById(Integer id) throws SystemException
	{
		return (ContentCategoryVO)getVOWithId(ContentCategoryImpl.class, id);
	}

	/**
	 * Find a List of ContentCategories for the specific attribute and Content Version.
	 * @param	attribute The attribute name of the ContentCategory to find
	 * @param	versionId The Content Version id of the ContentCategory to find
	 * @return	A list of ContentCategoryVO that have the provided content version and attribute
	 * @throws	SystemException If an error happens
	 */
	public List findByContentVersionAttribute(String attribute, Integer versionId) throws SystemException
	{	    
	    /*
	    List params = new ArrayList();
		params.add(attribute);
		params.add(versionId);
		return executeQuery(findByContentVersionAttribute, params);
	    */
	    
	    List contentCategoryVOList = new ArrayList();
	    
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		beginTransaction(db);

		try
		{
		    List contentCategories = findByContentVersionAttribute(attribute, versionId, db, true);
			if(contentCategories != null)
			    contentCategoryVOList = toVOList(contentCategories);
			
			commitTransaction(db);	
		}
		catch(Exception e)
		{
			//logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}

		
		return contentCategoryVOList;
	}

	/**
	 * Find a List of ContentCategories for the specific attribute and Content Version.
	 * @param	attribute The attribute name of the ContentCategory to find
	 * @param	versionId The Content Version id of the ContentCategory to find
	 * @return	A list of ContentCategoryVO that have the provided content version and attribute
	 * @throws	SystemException If an error happens
	 */
	public List findByContentVersionAttribute(String attribute, Integer versionId, Database db, boolean readOnly) throws SystemException
	{
	    List contentCategoryList = new ArrayList();
	    
	    ContentVersion contentVersion = null;
	    if(readOnly)
	        contentVersion = ContentVersionController.getContentVersionController().getReadOnlyContentVersionWithId(versionId, db);
		else
		    contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(versionId, db);
		    
	    Collection contentCategories = contentVersion.getContentCategories();
		if(contentCategories != null)
		{
		    Iterator contentCategoriesIterator = contentCategories.iterator();
		    while(contentCategoriesIterator.hasNext())
		    {
		        ContentCategory contentCategory = (ContentCategory)contentCategoriesIterator.next();
		        if(contentCategory.getAttributeName().equals(attribute))
		        {
		            contentCategoryList.add(contentCategory);
		        }
		    }
		}

		return contentCategoryList;
	}

	/**
	 * Find a List of ContentCategories for the specific attribute and Content Version.
	 * @param	attribute The attribute name of the ContentCategory to find
	 * @param	versionId The Content Version id of the ContentCategory to find
	 * @return	A list of ContentCategoryVO that have the provided content version and attribute
	 * @throws	SystemException If an error happens
	 */
	public List findByContentVersionAttribute(String attribute, ContentVersion contentVersion, Database db, boolean readOnly) throws SystemException
	{
	    List contentCategoryList = new ArrayList();
	    
	    if(contentVersion != null)
	    {
		    Collection contentCategories = contentVersion.getContentCategories();
			if(contentCategories != null)
			{
			    Iterator contentCategoriesIterator = contentCategories.iterator();
			    while(contentCategoriesIterator.hasNext())
			    {
			        ContentCategory contentCategory = (ContentCategory)contentCategoriesIterator.next();
			        if(contentCategory.getAttributeName().equals(attribute))
			        {
			            contentCategoryList.add(contentCategory);
			        }
			    }
			}
	    }
	    
		return contentCategoryList;
	}

	/**
	 * Find a List of ContentCategories for a Content Version.
	 * @param	versionId The Content Version id of the ContentCategory to find
	 * @return	A list of ContentCategoryVO that have the provided content version and attribute
	 * @throws	SystemException If an error happens
	 */
	public List findByContentVersion(Integer versionId) throws SystemException
	{
		List params = new ArrayList();
		params.add(versionId);
		return executeQuery(findByContentVersion, params);
	}

	/**
	 * Find a List of ContentCategories for a Content Version.
	 * @param	versionId The Content Version id of the ContentCategory to find
	 * @return	A list of ContentCategoryVO that have the provided content version and attribute
	 * @throws	SystemException If an error happens
	 */
	public List findByContentVersion(Integer versionId, Database db) throws SystemException
	{
		List params = new ArrayList();
		params.add(versionId);
		return executeQuery(findByContentVersion, params, db);
	}

	
	/**
	 * Find a List of ContentCategories for the specific attribute and Content Version.
	 * @param	categoryId The Category id of the ContentCategory to find
	 * @return	A list of ContentCategoryVO that have the provided category id
	 * @throws	SystemException If an error happens
	 */
	public List findByCategory(Integer categoryId) throws SystemException
	{
		List params = new ArrayList();
		params.add(categoryId);
		return executeQuery(findByCategory, params);
	}

	/**
	 * Saves a ContentCategoryVO whether it is new or not.
	 * @param	c The ContentCategoryVO to save
	 * @return	The saved ContentCategoryVO
	 * @throws	SystemException If an error happens
	 */
	public ContentCategoryVO save(ContentCategoryVO c, InfoGluePrincipal principal) throws SystemException
	{
		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().checkStateAndChangeIfNeeded(c.getContentVersionId(), principal);
		c.setContentVersionId(contentVersionVO.getId());
		
		return c.isUnsaved() ? create(c) : (ContentCategoryVO)updateEntity(ContentCategoryImpl.class, c);
	}

	/**
	 * Creates a ContentCategory from a ContentCategoryVO
	 */
	private ContentCategoryVO create(ContentCategoryVO c) throws SystemException
	{
		Database db = beginTransaction();

		try
		{
			ContentCategory contentCategory = createWithDatabase(c, db);
			commitTransaction(db);
			return contentCategory.getValueObject();
		}
		catch (Exception e)
		{
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}

	public ContentCategory createWithDatabase(ContentCategoryVO c, Database db) throws SystemException, PersistenceException
	{
		// Need this crappy hack to forge the relationship (castor completely sucks like this)
		// TODO: When hibernate comes, just save the VOs and if it has a child VO with an id set
		// TODO: it is used to make the relationship...ask me for clarification -frank
		Category category = (Category)getObjectWithId(CategoryImpl.class, c.getCategory().getId(), db);
		ContentVersion contentVersion = (ContentVersion)getObjectWithId(ContentVersionImpl.class, c.getContentVersionId(), db);

		ContentCategory contentCategory = null;
		
		List existingContentCategories = ContentCategoryController.getController().findByContentVersionAttribute(c.getAttributeName(), contentVersion.getContentVersionId(), db, true);
		boolean exists = false;
		Iterator existingContentCategoriesIterator = existingContentCategories.iterator();
		while(existingContentCategoriesIterator.hasNext())
		{
			ContentCategory contentCategoryCandidate = (ContentCategory)existingContentCategoriesIterator.next();
			if(contentCategoryCandidate.getCategoryId().equals(category.getId()))
			{
				exists = true;
				contentCategory = contentCategoryCandidate;
				logger.info("The category " + category.getName() + " was allready set on this version");
				break;
			}
		}
		
		if(!exists)
		{
			logger.info("Creating the category " + category.getName() + " as it was not set on this version");
	
			contentCategory = new ContentCategoryImpl();
			contentCategory.setValueObject(c);
			contentCategory.setCategory((CategoryImpl)category);
			contentCategory.setContentVersion((ContentVersionImpl)contentVersion);
			db.create(contentCategory);
			contentVersion.getContentCategories().add(contentCategory);
		}
		else
		{
			logger.info("Skipping the category " + category.getName() + " as it was allready set on this version");
		}
		
		return contentCategory;
	}

	/**
	 * Creates a number of ContentCategories from a list of categories and a contentVersion.
	 */
	public List create(List categoryVOList, ContentVersionVO contentVersionVO, String attributeName) throws SystemException
	{
		List contentCategoryVOList = new ArrayList();
		
	    Database db = beginTransaction();

		try
		{
			Iterator categoryVOListIterator = categoryVOList.iterator();
			while(categoryVOListIterator.hasNext())
			{
			    CategoryVO categoryVO = (CategoryVO)categoryVOListIterator.next();
				Category category = (Category)getObjectWithId(CategoryImpl.class, categoryVO.getId(), db);
				ContentVersion contentVersion = (ContentVersion)getObjectWithId(ContentVersionImpl.class, contentVersionVO.getId(), db);
				
				List existingContentCategories = ContentCategoryController.getController().findByContentVersionAttribute(attributeName, contentVersion.getContentVersionId(), db, true);
				boolean exists = false;
				Iterator existingContentCategoriesIterator = existingContentCategories.iterator();
				while(existingContentCategoriesIterator.hasNext())
				{
					ContentCategory contentCategory = (ContentCategory)existingContentCategoriesIterator.next();
					if(contentCategory.getCategoryId().equals(category.getId()))
					{
						exists = true;
						logger.info("The category " + category.getName() + " was allready set on this version");
						break;
					}
				}
				
				if(!exists)
				{
					logger.info("Creating the category " + category.getName() + " as it was not set on this version");

				    ContentCategoryVO contentCategoryVO = new ContentCategoryVO();
				    contentCategoryVO.setAttributeName(attributeName);
				    contentCategoryVO.setContentVersionId(contentVersionVO.getId());
				    ContentCategory contentCategory = createWithDatabase(contentCategoryVO, category, contentVersion, db);
						    
				    contentCategoryVOList.add(contentCategory.getValueObject());
				}
				else
				{
					logger.info("Skipping the category " + category.getName() + " as it was allready set on this version");
				}
			}
		    
			commitTransaction(db);
		}
		catch (Exception e)
		{
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return contentCategoryVOList;
	}

	/**
	 * Creates a number of ContentCategories from a list of categories and a contentVersion.
	 */
	
	public List create(List categoryList, ContentVersion contentVersion, String attributeName, Database db) throws SystemException, Exception
	{
		logger.info("Creating categories on " + contentVersion.getId() + " for attributeName:" + attributeName);
		List contentCategoryList = new ArrayList();

		Iterator categoryListIterator = categoryList.iterator();
		while(categoryListIterator.hasNext())
		{
		    Category category = (Category)categoryListIterator.next();
		    
			List existingContentCategories = ContentCategoryController.getController().findByContentVersionAttribute(attributeName, contentVersion.getContentVersionId(), db, true);
			boolean exists = false;
			Iterator existingContentCategoriesIterator = existingContentCategories.iterator();
			while(existingContentCategoriesIterator.hasNext())
			{
				ContentCategory contentCategory = (ContentCategory)existingContentCategoriesIterator.next();
				if(contentCategory.getCategoryId().equals(category.getId()))
				{
					exists = true;
					logger.info("The category " + category.getName() + " was allready set on this version");
					break;
				}
			}
			
			if(!exists)
			{
				logger.info("Creating the category " + category.getName() + " as it was not set on this version");

				ContentCategoryVO contentCategoryVO = new ContentCategoryVO();
			    contentCategoryVO.setAttributeName(attributeName);
			    contentCategoryVO.setContentVersionId(contentVersion.getId());
			    ContentCategory contentCategory = createWithDatabase(contentCategoryVO, category, contentVersion, db);
				contentVersion.getContentCategories().add(contentCategory);
				    
				contentCategoryList.add(contentCategory);
			}
			else
			{
				logger.info("Skipping the category " + category.getName() + " as it was allready set on this version");
			}
		}
		
		return contentCategoryList;
	}

	public ContentCategory createWithDatabase(ContentCategoryVO c, Category category, ContentVersion contentVersion, Database db) throws SystemException, PersistenceException
	{
		ContentCategory contentCategory = new ContentCategoryImpl();
		contentCategory.setValueObject(c);
		contentCategory.setCategory((CategoryImpl)category);
		contentCategory.setContentVersion((ContentVersionImpl)contentVersion);
		db.create(contentCategory);
		return contentCategory;
	}

	/**
	 * Deletes a ContentCategory
	 * @param	id The id of the ContentCategory to delete
	 * @throws	SystemException If an error happens
	 */
	public void delete(Integer id, InfoGluePrincipal principal) throws SystemException
	{
		Database db = beginTransaction();

		try
		{
			ContentCategory contentCategory = (ContentCategory)getObjectWithId(ContentCategoryImpl.class, id, db);
		    ContentVersion contentVersion = ContentVersionController.getContentVersionController().checkStateAndChangeIfNeeded(contentCategory.getContentVersionId(), principal, db);
						
		    //ContentCategory contentCategory = (ContentCategory)getObjectWithId(ContentCategoryImpl.class, id, db);
		    //ContentVersion contentVersion = (ContentVersion)getObjectWithId(ContentVersionImpl.class, contentCategory.getContentVersionId(), db);
		    Iterator contentCategoriesIterator = contentVersion.getContentCategories().iterator();
		    while(contentCategoriesIterator.hasNext())
		    {
		    	ContentCategory currentContentCategory = (ContentCategory)contentCategoriesIterator.next();
		    	ContentCategoryVO currentContentCategoryVO = currentContentCategory.getValueObject();
			    if(currentContentCategoryVO.getAttributeName().equals(contentCategory.getAttributeName()) && currentContentCategory.getCategory().getId().equals(contentCategory.getCategory().getId())) 
		    	{
		    		contentCategoriesIterator.remove();
		    		db.remove(currentContentCategory);
		    		break;
		    	}
		    }
		    
		    //contentVersion.getContentCategories().remove(contentCategory);
		    //db.remove(contentCategory);
		    
			commitTransaction(db);
		}
		catch (Exception e)
		{
			rollbackTransaction(db);
			e.printStackTrace();
			throw new SystemException(e.getMessage());
		}
		
		//deleteEntity(ContentCategoryImpl.class, id);
	}

	/**
	 * Deletes all ContentCategories for a particular ContentVersion
	 * @param	versionId The id of the ContentCategory to delete
	 * @throws	SystemException If an error happens
	 */
	public void deleteByContentVersion(Integer versionId) throws SystemException
	{
		delete(findByContentVersion(versionId));
	}

	/**
	 * Deletes all ContentCategories for a particular ContentVersion
	 * @param	versionId The id of the ContentCategory to delete
	 * @throws	SystemException If an error happens
	 */
	public void deleteByContentVersion(ContentVersion contentVersion, Database db) throws SystemException, Exception
	{
	    Iterator contentVersionIterator = contentVersion.getContentCategories().iterator();
	    while(contentVersionIterator.hasNext())
	    {
	        ContentCategory contentCategory = (ContentCategory)contentVersionIterator.next();
	        contentVersionIterator.remove();
	        db.remove(contentCategory);
	    }
	}

	/**
	 * Deletes all ContentCategories for a particular ContentVersion using the provided Database
	 * @param	versionId The id of the ContentCategory to delete
	 * @param	db The Database instance to use
	 * @throws	SystemException If an error happens
	 */
	public void deleteByContentVersion(Integer versionId, Database db) throws SystemException
	{
		delete(findByContentVersion(versionId), db);
	}

	/**
	 * Deletes all ContentCategories for a particular Category
	 * @param	categoryId The id of the ContentCategory to delete
	 * @throws	SystemException If an error happens
	 */
	public void deleteByCategory(Integer categoryId) throws SystemException
	{
		delete(findByCategory(categoryId));
	}

	/**
	 * Deletes all ContentCategories for a particular Category using the provided Database
	 * @param	categoryId The id of the Category to delete
	 * @param	db The Database instance to use
	 * @throws	SystemException If an error happens
	 */
	public void deleteByCategory(Integer categoryId, Database db) throws SystemException
	{
		delete(findByCategory(categoryId), db);
	}

	/**
	 * Deletes all content categories with a specific attribute for a specific content version within a single transaction
	 * @param attribute the desired attribute
	 * @param versionId the ID of the desired content version
	 * @throws SystemException if a database error occurs
	 */
	public void deleteByContentVersionAttribute(String attribute, Integer versionId) throws SystemException
	{
		delete(findByContentVersionAttribute(attribute, versionId));
	}

	/**
	 * Deletes all content categories with a specific attribute for a specific content version using the given database
	 * @param attribute the desired attribute
	 * @param versionId the ID of the desired content version
	 * @param db the database defining the transaction context for this delete
	 * @throws SystemException if a database error occurs
	 */
	public void deleteByContentVersionAttribute(String attribute, Integer versionId, Database db) throws SystemException
	{
		delete(findByContentVersionAttribute(attribute, versionId), db);
	}

	/**
	 * Deletes a collection of content categories within a single transaction
	 * @param contentCategories a collection of ContentCategoryVOs to delete
	 * @throws SystemException if a database error occurs
	 */
	private static void delete(Collection contentCategories) throws SystemException
	{
		Database db = beginTransaction();

		try
		{
			delete(contentCategories, db);
			commitTransaction(db);
		}
		catch (Exception e)
		{
			rollbackTransaction(db);
			throw new SystemException(e);
		}
	}

	/**
	 * Deletes a collection of content categories using the given database
	 * @param contentCategories a collection of ContentCategoryVOs to delete
	 * @param db the database to be used for the delete
	 * @throws SystemException if a database error occurs
	 */
	private static void delete(Collection contentCategories, Database db) throws SystemException
	{
		for (Iterator i = contentCategories.iterator(); i.hasNext();)
			deleteEntity(ContentCategoryImpl.class, ((ContentCategoryVO)i.next()).getId(), db);
	}

	/**
	 * Implemented for BaseController
	 */
	public BaseEntityVO getNewVO()
	{
		return new ContentCategoryVO();
	}
}
