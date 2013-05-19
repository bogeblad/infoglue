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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.PageDeliveryMetaData;
import org.infoglue.cms.entities.management.PageDeliveryMetaDataEntity;
import org.infoglue.cms.entities.management.PageDeliveryMetaDataEntityVO;
import org.infoglue.cms.entities.management.PageDeliveryMetaDataVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.RepositoryLanguage;
import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
import org.infoglue.cms.entities.management.impl.simple.PageDeliveryMetaDataEntityImpl;
import org.infoglue.cms.entities.management.impl.simple.PageDeliveryMetaDataImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryLanguageImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.deliver.util.CacheController;

/**
 * This class handles all interaction with Languages and persistence of them.
 * 
 * @author mattias
 */

public class PageDeliveryMetaDataController extends BaseController
{
    private final static Logger logger = Logger.getLogger(PageDeliveryMetaDataController.class.getName());

	/**
	 * Factory method
	 */

	public static PageDeliveryMetaDataController getController()
	{
		return new PageDeliveryMetaDataController();
	}
	
	
	/**
	 * This method return a LanguageVO
	 */
	
	public PageDeliveryMetaDataVO getPageDeliveryMetaDataVO(Database db, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
	{
		if(contentId == null)
			contentId = -1;

		//System.out.println("siteNodeId:" + siteNodeId);
		//System.out.println("languageId:" + languageId);
		//System.out.println("contentId:" + contentId);

    	String key = "" + siteNodeId + "_" + languageId + "_" + contentId;
    	//System.out.println("key on fetch 1:" + key);
		PageDeliveryMetaDataVO pageDeliveryMetaDataVO = (PageDeliveryMetaDataVO)CacheController.getCachedObjectFromAdvancedCache("pageDeliveryMetaDataCache", key);
		//System.out.println("pageDeliveryMetaDataVO:" + pageDeliveryMetaDataVO);
		
		if(pageDeliveryMetaDataVO == null)
		{
			//System.out.println("key on fetch:" + key);
			try
			{
				OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.PageDeliveryMetaDataImpl f WHERE f.siteNodeId = $1 AND f.languageId = $2 AND f.contentId = $3");
				oql.bind(siteNodeId);
				oql.bind(languageId);
				oql.bind(contentId);
				
				QueryResults results = oql.execute(Database.READONLY);
				if (results.hasMore()) 
				{
					PageDeliveryMetaData pageDeliveryMetaData = (PageDeliveryMetaData)results.next();
					if(pageDeliveryMetaData != null)
					{
						pageDeliveryMetaDataVO = pageDeliveryMetaData.getValueObject();
						CacheController.cacheObjectInAdvancedCache("pageDeliveryMetaDataCache", key, pageDeliveryMetaDataVO);
					}
				}
				
				results.close();
				oql.close();
			}
			catch(Exception e)
			{
				throw new SystemException("An error occurred when we tried to fetch a PageDeliveryMetaData. Reason:" + e.getMessage(), e);    
			}
		}
		
		return pageDeliveryMetaDataVO;
	}
	
    public PageDeliveryMetaDataVO create(PageDeliveryMetaDataVO pageDeliveryMetaDataVO) throws ConstraintException, SystemException
    {
    	PageDeliveryMetaData ent = new PageDeliveryMetaDataImpl();
        ent.setValueObject(pageDeliveryMetaDataVO);
        ent = (PageDeliveryMetaData) createEntity(ent);
        return ent.getValueObject();
    }     

    public PageDeliveryMetaDataVO create(Database db, PageDeliveryMetaDataVO pageDeliveryMetaDataVO, Collection<PageDeliveryMetaDataEntityVO> entitiesCollection) throws ConstraintException, SystemException, Exception
    {
    	PageDeliveryMetaData ent = new PageDeliveryMetaDataImpl();
        ent.setValueObject(pageDeliveryMetaDataVO);
        db.create(ent);
        
        for(PageDeliveryMetaDataEntityVO entity : entitiesCollection)
        {
        	PageDeliveryMetaDataEntity ent2 = new PageDeliveryMetaDataEntityImpl();
        	ent2.setValueObject(entity);
        	ent2.setPageDeliveryMetaData(ent);
            db.create(ent2);
        }
        ent.setEntities(entitiesCollection);
        
        return ent.getValueObject();
    }    
    
    
	/**
	 * This method removes a Language from the system and also cleans out all depending repositoryLanguages.
	 */
	
	public void deletePageDeliveryMetaData(Integer siteNodeId, Integer contentId) throws SystemException, Exception
    {
		Database db = CastorDatabaseService.getDatabase();
		
		try
		{
			beginTransaction(db);

			deletePageDeliveryMetaData(db, siteNodeId, contentId);
			
			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
    } 

	/**
	 * This method return a LanguageVO
	 */
	
	public void deletePageDeliveryMetaData(Database db, Integer siteNodeId, Integer contentId) throws SystemException, Exception
	{
		System.out.println("deletePageDeliveryMetaData....");
		System.out.println("siteNodeId:" + siteNodeId);
		System.out.println("contentId:" + contentId);
		
		try
		{
			String sql = null;
			Integer id = null;
			if(siteNodeId != null)
			{
				sql = "DELETE FROM cmPageDeliveryMetaData WHERE siteNodeId = ?";
				id = siteNodeId;
			}
			if(siteNodeId == null && contentId != null)
			{
				sql = "DELETE FROM cmPageDeliveryMetaData WHERE where contentId = ?";
				id = contentId;
			}
			if(siteNodeId != null && contentId != null)
			{
				sql = "DELETE FROM cmPageDeliveryMetaData WHERE siteNodeId = ? AND contentId = ?";
				id = siteNodeId;
			}
			System.out.println("sql: " + sql);
			if(sql != null)
			{
				Connection conn = (Connection) db.getJdbcConnection();
				
				PreparedStatement psmt = conn.prepareStatement(sql);
	    		psmt.setInt(1, id);
				System.out.println("id: " + id);
	    		if(siteNodeId != null && contentId != null)
	    		{
	    			psmt.setInt(2, contentId);
					System.out.println("contentId: " + contentId);
	    		}
	    		
				int result = psmt.executeUpdate();
				System.out.println("result:" + result);

				String sql2 = "DELETE FROM cmPageDeliveryMetaDataEntity WHERE pageDeliveryMetaDataId NOT IN (select pageDeliveryMetaDataId from cmPageDeliveryMetaData)";
				System.out.println("sql2: " + sql2);
				PreparedStatement psmt2 = conn.prepareStatement(sql2);
				int result2 = psmt2.executeUpdate();
				System.out.println("result2:" + result2);
				CacheController.clearCache("pageDeliveryMetaDataCache");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SystemException("An error occurred when we tried to fetch a PageDeliveryMetaData. Reason:" + e.getMessage(), e);    
		}
		
	}

	/**
	 * This method removes a Language from the system and also cleans out all depending repositoryLanguages.
	 */
	
	public void deletePageDeliveryMetaDataByReferencingEntity(Integer siteNodeId, Integer contentId) throws SystemException, Exception
    {
		Database db = CastorDatabaseService.getDatabase();
		
		try
		{
			beginTransaction(db);

			deletePageDeliveryMetaDataByReferencingEntity(db, siteNodeId, contentId);
			
			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
    } 

	
	/**
	 * This method return a LanguageVO
	 */
	
	public void deletePageDeliveryMetaDataByReferencingEntity(Database db, Integer siteNodeId, Integer contentId) throws SystemException, Exception
	{
		System.out.println("deletePageDeliveryMetaData....");
		System.out.println("siteNodeId:" + siteNodeId);
		System.out.println("contentId:" + contentId);
		
		try
		{
			String sql = null;
			Integer id = null;
			if(siteNodeId != null)
			{
				sql = "DELETE FROM cmPageDeliveryMetaData WHERE pageDeliveryMetaDataId IN (SELECT distinct pageDeliveryMetaDataId FROM cmPageDeliveryMetaDataEntity WHERE siteNodeId = ?)";
				id = siteNodeId;
			}
			if(siteNodeId == null && contentId != null)
			{
				sql = "DELETE FROM cmPageDeliveryMetaData WHERE pageDeliveryMetaDataId IN (SELECT distinct pageDeliveryMetaDataId FROM cmPageDeliveryMetaDataEntity WHERE contentId = ?)";
				id = contentId;
			}
			if(siteNodeId != null && contentId != null)
			{
				sql = "DELETE FROM cmPageDeliveryMetaData WHERE pageDeliveryMetaDataId IN (SELECT distinct pageDeliveryMetaDataId FROM cmPageDeliveryMetaDataEntity WHERE siteNodeId = ? AND contentId = ?)";
				id = siteNodeId;
			}
			System.out.println("sql: " + sql);
			if(sql != null)
			{
				Connection conn = (Connection) db.getJdbcConnection();
				
				PreparedStatement psmt = conn.prepareStatement(sql);
	    		psmt.setInt(1, id);
				System.out.println("id: " + id);
	    		if(siteNodeId != null && contentId != null)
	    		{
	    			psmt.setInt(2, contentId);
					System.out.println("contentId: " + contentId);
	    		}
	    		
				int result = psmt.executeUpdate();
				System.out.println("result:" + result);

				String sql2 = "DELETE FROM cmPageDeliveryMetaDataEntity WHERE pageDeliveryMetaDataId NOT IN (select pageDeliveryMetaDataId from cmPageDeliveryMetaData)";
				System.out.println("sql2: " + sql2);
				PreparedStatement psmt2 = conn.prepareStatement(sql2);
				int result2 = psmt2.executeUpdate();
				System.out.println("result2:" + result2);
				CacheController.clearCache("pageDeliveryMetaDataCache");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SystemException("An error occurred when we tried to fetch a PageDeliveryMetaData. Reason:" + e.getMessage(), e);    
		}
		
	}


	/**
	 * This method removes a Language from the system and also cleans out all depending repositoryLanguages.
	 */
	
    public void delete(Database db, LanguageVO languageVO) throws ConstraintException, SystemException
    {
		Language language = getLanguageWithId(languageVO.getId(), db);
		RepositoryLanguageController.getController().deleteAllRepositoryLanguageWithLanguage(language, db);    		
			
		deleteEntity(LanguageImpl.class, languageVO.getLanguageId(), db);
    }        


    public Language getLanguageWithId(Integer languageId, Database db) throws SystemException, Bug
    {
    	return (Language) getObjectWithId(LanguageImpl.class, languageId, db);
    }


    public Language getLanguageWithId(Integer languageId) throws ConstraintException, SystemException, Bug
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        Language language = null;

        try
        {
			beginTransaction(db);

		    language = getLanguageWithId(languageId, db);
        
            //If any of the validations or setMethods reported an error, we throw them up now before create.
            ceb.throwIfNotEmpty();
            
			commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
		return language;
    }



	/**
	 * This method deletes the Repository sent in from the system.
	 */
	
	public void deleteLanguage(Integer languageId, Database db) throws SystemException, Bug
	{
		try
		{
			db.remove(getLanguageWithId(languageId, db));
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to delete Language in the database. Reason: " + e.getMessage(), e);
		}	
	} 

    public LanguageVO update(LanguageVO languageVO) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        Language language = null;

        beginTransaction(db);

        try
        {
            //add validation here if needed
            language = getLanguageWithId(languageVO.getLanguageId(), db);
            language.setValueObject(languageVO);

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


        return language.getValueObject();
    }        
	
	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new LanguageVO();
	}
	
}
 
