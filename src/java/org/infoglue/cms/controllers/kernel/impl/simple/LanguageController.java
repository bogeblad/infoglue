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
import java.util.Locale;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.RepositoryLanguage;
import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
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

public class LanguageController extends BaseController
{
    private final static Logger logger = Logger.getLogger(LanguageController.class.getName());

	/**
	 * Factory method
	 */

	public static LanguageController getController()
	{
		return new LanguageController();
	}

	/**
	 * This method returns a specific LanguageVO object
	 */
	/*
	public LanguageVO getLanguageVOWithId(Integer languageId) throws SystemException, Bug
	{
		return (LanguageVO)getVOWithId(LanguageImpl.class, languageId);
	} 
	*/
	
	/**
	 * This method return a LanguageVO
	 */
	
	public LanguageVO getLanguageVOWithId(Integer languageId) throws SystemException, Exception
	{
		String key = "" + languageId;
		logger.info("key:" + key);
		LanguageVO languageVO = (LanguageVO)CacheController.getCachedObject("languageCache", key);
		if(languageVO != null)
		{
			logger.info("There was an cached languageVO:" + languageVO);
		}
		else
		{
		    languageVO = (LanguageVO)getVOWithId(LanguageImpl.class, languageId);
		    
			CacheController.cacheObject("languageCache", key, languageVO);				
		}
				
		return languageVO;
	}

	
	/**
	 * This method returns a specific LanguageVO object
	 */
	
	public LanguageVO getLanguageVOWithId(Integer languageId, Database db) throws SystemException, Bug
	{
		return (LanguageVO)getVOWithId(LanguageImpl.class, languageId, db);
	} 

	/**
	 * This method returns language with the languageCode sent in. 
	 */
	
	public Locale getLocaleWithId(Integer languageId)
	{
		Locale locale = Locale.getDefault();
		
		if (languageId != null)
		{
			try 
			{
				LanguageVO languageVO = getLanguageVOWithId(languageId);
				locale = new Locale(languageVO.getLanguageCode());
			} 
			catch (Exception e) 
			{
				logger.error("An error occurred in getLocaleWithId: getting locale with languageid:" + languageId + "," + e, e);
			}	
		}
		
		return locale; 
	}

	/**
	 * This method returns language with the languageCode sent in. 
	 */
	
	public Locale getLocaleWithId(Integer languageId, Database db)
	{
		Locale locale = Locale.getDefault();
		
		if (languageId != null)
		{
			try 
			{
				LanguageVO languageVO = getLanguageVOWithId(languageId, db);
				locale = new Locale(languageVO.getLanguageCode());
			} 
			catch (Exception e) 
			{
				logger.error("An error occurred in getLocaleWithId: getting locale with languageid:" + languageId + "," + e, e);
			}	
		}
		
		return locale; 
	}

	/**
	 * Returns the LanguageVO with the given name.
	 * 
	 * @param name
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public LanguageVO getLanguageVOWithName(String name) throws SystemException, Bug
	{
		LanguageVO languageVO = null;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);

			Language language = getLanguageWithName(name, db);
			if(language != null)
				languageVO = language.getValueObject();
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return languageVO;	
	}
	
	/**
	 * Returns the Language with the given name fetched within a given transaction.
	 * 
	 * @param name
	 * @param db
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */

	public Language getLanguageWithName(String name, Database db) throws SystemException, Bug
	{
		Language language = null;
		
		try
		{
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.LanguageImpl f WHERE f.name = $1");
			oql.bind(name);
			
			QueryResults results = oql.execute();
			this.logger.info("Fetching entity in read/write mode" + name);

			if (results.hasMore()) 
			{
				language = (Language)results.next();
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a named language. Reason:" + e.getMessage(), e);    
		}
		
		return language;		
	}

	/**
	 * Returns the LanguageVO with the given languageCode.
	 * 
	 * @param code
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public LanguageVO getLanguageVOWithCode(String code, Database db) throws SystemException, Bug
	{
		LanguageVO languageVO = null;
		
		Language language = getLanguageWithCode(code, db);
		if(language != null)
			languageVO = language.getValueObject();
		
		return languageVO;	
	}

	/**
	 * Returns the LanguageVO with the given languageCode.
	 * 
	 * @param code
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public LanguageVO getLanguageVOWithCode(String code) throws SystemException, Bug
	{
		String key = "" + code;
		LanguageVO languageVO = (LanguageVO)CacheController.getCachedObject("languageCache", key);
		if(languageVO != null)
		{
			logger.info("There was an cached languageVO:" + languageVO);
		}
		else
		{
			Database db = CastorDatabaseService.getDatabase();

			try 
			{
				beginTransaction(db);

				Language language = getLanguageWithCode(code, db);
				if(language != null)
					languageVO = language.getValueObject();
				
				commitTransaction(db);
			} 
			catch (Exception e) 
			{
				logger.info("An error occurred so we should not complete the transaction:" + e);
				rollbackTransaction(db);
				throw new SystemException(e.getMessage());
			}

			CacheController.cacheObject("languageCache", key, languageVO);				
		}

		return languageVO;	
	}
	
	/**
	 * Returns the Language with the given languageCode fetched within a given transaction.
	 * 
	 * @param code
	 * @param db
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */

	public Language getLanguageWithCode(String code, Database db) throws SystemException, Bug
	{
		Language language = null;
		
		try
		{
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.LanguageImpl f WHERE f.languageCode = $1");
			oql.bind(code);
			
			QueryResults results = oql.execute();
			this.logger.info("Fetching entity in read/write mode" + code);

			if (results.hasMore()) 
			{
				language = (Language)results.next();
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a named language. Reason:" + e.getMessage(), e);    
		}
		
		return language;		
	}
	
    public LanguageVO create(LanguageVO languageVO) throws ConstraintException, SystemException
    {
        Language ent = new LanguageImpl();
        ent.setValueObject(languageVO);
        ent = (Language) createEntity(ent);
        return ent.getValueObject();
    }     

    public LanguageVO create(Database db, LanguageVO languageVO) throws ConstraintException, SystemException, Exception
    {
        Language ent = new LanguageImpl();
        ent.setValueObject(languageVO);
        db.create(ent);
        return ent.getValueObject();
    }     

	/**
	 * This method removes a Language from the system and also cleans out all depending repositoryLanguages.
	 */
	
    public void delete(LanguageVO languageVO) throws ConstraintException, SystemException
    {
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		Language language = null;

		try
		{
			beginTransaction(db);

			delete(db, languageVO);
			
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


	public LanguageVO getLanguageVOWithRepositoryLanguageId(Integer repositoryLanguageId) throws ConstraintException, SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		LanguageVO languageVO = null;

		try
		{
			beginTransaction(db);

			languageVO = getLanguageWithRepositoryLanguageId(repositoryLanguageId, db).getValueObject();
			
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
        
		return languageVO;
	}

    public Language getLanguageWithRepositoryLanguageId(Integer repositoryLanguageId, Database db) throws ConstraintException, SystemException, Bug
    {
		RepositoryLanguage repositoryLanguage = (RepositoryLanguage) getObjectWithId(RepositoryLanguageImpl.class, repositoryLanguageId, db);
		Language language = repositoryLanguage.getLanguage();
        
		return language;
    }

    
    public List getLanguageVOList(Integer repositoryId) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        List languageVOList = new ArrayList();

        beginTransaction(db);

        try
        {
        	languageVOList = getLanguageVOList(repositoryId, db);
        	
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

        return languageVOList;
    }
    
    public List getLanguageVOList(Integer repositoryId, Database db) throws ConstraintException, SystemException
    {
		String key = "" + repositoryId + "_allLanguages";
		logger.info("key:" + key);
		List<LanguageVO> list = (List<LanguageVO>)CacheController.getCachedObject("languageCache", key);
		if(list != null)
		{
			return list;
		}

        List languageVOList = new ArrayList();

		Repository repository = RepositoryController.getController().getRepositoryWithId(repositoryId, db);
        Collection repositoryLanguageList = repository.getRepositoryLanguages();
    	Iterator repositoryLanguageIterator = repositoryLanguageList.iterator();
    	while(repositoryLanguageIterator.hasNext())
		{
			RepositoryLanguage repositoryLanguage = (RepositoryLanguage)repositoryLanguageIterator.next();
			languageVOList.add(repositoryLanguage.getLanguage().getValueObject());
		}
        
    	if(languageVOList != null)
    		CacheController.cacheObject("languageCache", key, languageVOList);
    	
        return languageVOList;
    }


	/**
	 * This method returns all languages for a certain repository.
	 * 
	 * @param repositoryId
	 * @return
	 * @throws SystemException
	 * @throws Exception
	 */

	public List<LanguageVO> getAvailableLanguageVOListForRepository(Integer repositoryId, Database db) throws SystemException, Exception
    {
		String key = "" + repositoryId + "_allLanguages";
		logger.info("key:" + key);
		List<LanguageVO> list = (List<LanguageVO>)CacheController.getCachedObject("languageCache", key);
		if(list != null)
		{
			logger.info("There was an cached list:" + list);
		}
		else
		{
			list = new ArrayList();
			
			OQLQuery oql = db.getOQLQuery( "SELECT l FROM org.infoglue.cms.entities.management.impl.simple.LanguageImpl l WHERE l.repositoryLanguages.repository = $1 ORDER BY l.repositoryLanguages.sortOrder, l.languageId");
			oql.bind(repositoryId);
			
	    	QueryResults results = oql.execute(Database.ReadOnly);
			while(results.hasMore()) 
	        {
				Language language = (Language)results.next();
                list.add(language.getValueObject());
	        }
	          
			results.close();
			oql.close();
			
	        if(list.size() > 0)
	            CacheController.cacheObject("languageCache", key, list);				
		}
	        
        return list;
    } 
	
	private List<Language> getLanguageList(Integer repositoryId, Database db) throws ConstraintException, SystemException
	{
		List<Language> languageList = new ArrayList<Language>();

		Repository repository = RepositoryController.getController().getRepositoryWithId(repositoryId, db);
		Collection repositoryLanguageList = repository.getRepositoryLanguages();
		Iterator repositoryLanguageIterator = repositoryLanguageList.iterator();
		while(repositoryLanguageIterator.hasNext())
		{
			RepositoryLanguage repositoryLanguage = (RepositoryLanguage)repositoryLanguageIterator.next();
			languageList.add(repositoryLanguage.getLanguage());
		}

		return languageList;
	}

    public List getLanguageList(Database db) throws SystemException, Bug
    {
    	return getAllObjects(LanguageImpl.class, "languageId", db);
    }

    public List getLanguageVOList(Database db) throws SystemException, Bug
    {
		String key = "allLanguageVOList";
		List languageVOList = (List)CacheController.getCachedObject("languageCache", key);
		if(languageVOList != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached languageVOList:" + languageVOList.size());
		}
		else
		{
			languageVOList = getAllVOObjects(LanguageImpl.class, "languageId", db);
			CacheController.cacheObject("languageCache", key, languageVOList);				
		}
		
		return languageVOList;
		
        //return getAllVOObjects(LanguageImpl.class, "languageId", db);
    }

    public List getLanguageVOList() throws SystemException, Bug
    {
		String key = "allLanguageVOList";
		List languageVOList = (List)CacheController.getCachedObject("languageCache", key);
		if(languageVOList != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached languageVOList:" + languageVOList.size());
		}
		else
		{
			languageVOList = getAllVOObjects(LanguageImpl.class, "languageId");
			CacheController.cacheObject("languageCache", key, languageVOList);				
		}
		
		return languageVOList;
    }

	/**
	 * This method returns the master language. 
	 * todo - add attribute on repositoryLanguage to be able to sort them... and then fetch the first
	 */
	
	public LanguageVO getMasterLanguage(Integer repositoryId) throws SystemException, Exception
	{ 
		LanguageVO languageVO = null;

		String languageKey = "" + repositoryId;
		logger.info("languageKey:" + languageKey);
		languageVO = (LanguageVO)CacheController.getCachedObject("masterLanguageCache", languageKey);
		if(languageVO != null)
		{
			logger.info("There was an cached master language:" + languageVO.getName());
		}
		else
		{
			Database db = CastorDatabaseService.getDatabase();
			ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
	
			Language language = null;
	
			beginTransaction(db);
	
			try
			{
				language = getMasterLanguage(db, repositoryId);
	            
				//If any of the validations or setMethods reported an error, we throw them up now before create. 
				ceb.throwIfNotEmpty();

				if(language != null)
				{
				    languageVO = language.getValueObject();
				    CacheController.cacheObject("masterLanguageCache", languageKey, languageVO);
				}
				
				commitTransaction(db);
			}
			catch(Exception e)
			{
				logger.error("An error occurred so we should not complete the transaction:" + e, e);
				rollbackTransaction(db);
				throw new SystemException(e.getMessage());
			}
		}
		
		return languageVO;	
	}

	
	/**
	 * This method returns the master language. 
	 * todo - add attribute on repositoryLanguage to be able to sort them... and then fetch the first
	 */
	
	public LanguageVO getMasterLanguage(Integer repositoryId, Database db) throws SystemException, Exception
	{ 
		LanguageVO languageVO = null;

		String languageKey = "" + repositoryId;
		logger.info("languageKey:" + languageKey);
		languageVO = (LanguageVO)CacheController.getCachedObject("masterLanguageCache", languageKey);
		if(languageVO != null)
		{
			logger.info("There was an cached master language:" + languageVO.getName());
		}
		else
		{
			Language language = getMasterLanguage(db, repositoryId);
	            
			if(language != null)
			{
			    languageVO = language.getValueObject();
			    CacheController.cacheObject("masterLanguageCache", languageKey, languageVO);
			}				
		}
		
		return languageVO;	
	}

	
	/**
	 * This method returns the master language within an transaction. 
	 */
	
	private Language getMasterLanguage(Database db, Integer repositoryId) throws SystemException, Exception
	{ 
		Language language = null;

		OQLQuery oql = db.getOQLQuery( "SELECT l FROM org.infoglue.cms.entities.management.impl.simple.LanguageImpl l WHERE l.repositoryLanguages.repository.repositoryId = $1 ORDER BY l.repositoryLanguages.sortOrder, l.languageId");
		oql.bind(repositoryId);
		
		QueryResults results = oql.execute(Database.ReadOnly);
		
		if (results.hasMore()) 
		{
			language = (Language)results.next();
		}
        
		results.close();
		oql.close();

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
 
