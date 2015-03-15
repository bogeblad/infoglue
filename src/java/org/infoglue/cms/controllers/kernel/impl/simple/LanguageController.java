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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryLanguage;
import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryLanguageImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.Timer;

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
			Database db = CastorDatabaseService.getDatabase();
			ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
	
			beginTransaction(db);
	
			try
			{
				languageVO = getLanguageVOWithId(languageId, db);
	            
				//If any of the validations or setMethods reported an error, we throw them up now before create. 
				ceb.throwIfNotEmpty();

				if(languageVO != null)
				{
				    CacheController.cacheObject("languageCache", key, languageVO);
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
	 * This method returns a specific LanguageVO object
	 */
	
	public LanguageVO getLanguageVOWithId(Integer languageId, Database db) throws SystemException, Bug, Exception
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
			String SQL = "SELECT l.languageId, l.name, l.languageCode, l.charset from cmLanguage l where l.languageId = ?";
			logger.info("SQL:" + SQL);
			
			Connection conn = (Connection) db.getJdbcConnection();
			
			PreparedStatement psmt = conn.prepareStatement(SQL.toString());
			psmt.setInt(1, languageId);
	
			ResultSet rs = psmt.executeQuery();
			if(rs.next())
			{
				languageVO = new LanguageVO();
				languageVO.setLanguageId(new Integer(rs.getString(1)));
				languageVO.setName(rs.getString(2));
				languageVO.setLanguageCode(rs.getString(3));
				languageVO.setCharset(rs.getString(4));
				
				logger.info("Found:" + languageVO);
			}
			rs.close();
			psmt.close();
		
			if(languageVO != null)
			{
			    CacheController.cacheObject("languageCache", key, languageVO);
			}
		}
		
		return languageVO;
		//return (LanguageVO)getVOWithId(LanguageImpl.class, languageId, db);
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

			languageVO = getLanguageVOWithName(name, db);
			
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

	public LanguageVO getLanguageVOWithName(String name, Database db) throws SystemException, Bug, Exception
	{
		LanguageVO languageVO = null;
		
		String SQL = "SELECT l.languageId, l.name, l.languageCode, l.charset from cmLanguage l where l.name = ?";
		logger.info("SQL:" + SQL);
		
		Connection conn = (Connection) db.getJdbcConnection();
		
		PreparedStatement psmt = conn.prepareStatement(SQL.toString());
		psmt.setString(1, name);

		ResultSet rs = psmt.executeQuery();
		if(rs.next())
		{
			languageVO = new LanguageVO();
			languageVO.setLanguageId(new Integer(rs.getString(1)));
			languageVO.setName(rs.getString(2));
			languageVO.setLanguageCode(rs.getString(3));
			languageVO.setCharset(rs.getString(4));
			
			logger.info("Found:" + languageVO);
		}
		rs.close();
		psmt.close();
		
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
	
	public Language getLanguageWithCode(String code, Database db) throws SystemException, Bug, Exception
	{
		Language language = null;
		
		LanguageVO languageVO = getLanguageVOWithCode(code, db);
		if(languageVO != null)
			language = getLanguageWithId(languageVO.getId(), db);
		
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

				languageVO = getLanguageVOWithCode(code, db);
				
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

	public LanguageVO getLanguageVOWithCode(String code, Database db) throws SystemException, Bug, Exception
	{
		String key = "" + code;
		LanguageVO languageVO = (LanguageVO)CacheController.getCachedObject("languageCache", key);
		if(languageVO != null)
		{
			logger.info("There was an cached languageVO:" + languageVO);
		}
		else
		{
			String SQL = "SELECT l.languageId, l.name, l.languageCode, l.charset from cmLanguage l where l.languageCode = ?";
			logger.info("SQL:" + SQL);
			
			Connection conn = (Connection) db.getJdbcConnection();
			
			PreparedStatement psmt = conn.prepareStatement(SQL.toString());
			psmt.setString(1, code);
	
			ResultSet rs = psmt.executeQuery();
			if(rs.next())
			{
				languageVO = new LanguageVO();
				languageVO.setLanguageId(new Integer(rs.getString(1)));
				languageVO.setName(rs.getString(2));
				languageVO.setLanguageCode(rs.getString(3));
				languageVO.setCharset(rs.getString(4));
				
				logger.info("Found:" + languageVO);
			}
			rs.close();
			psmt.close();

			if(languageVO != null)
				CacheController.cacheObject("languageCache", key, languageVO);				
		}

		
		return languageVO;		
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

    
    public List<LanguageVO> getLanguageVOList(Integer repositoryId) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        List<LanguageVO> languageVOList = new ArrayList<LanguageVO>();

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
    
    public List<LanguageVO> getLanguageVOList(Integer repositoryId, Database db) throws Exception
    {
		String key = "" + repositoryId + "_allLanguages";
		logger.info("key:" + key);
		List<LanguageVO> list = (List<LanguageVO>)CacheController.getCachedObject("languageCache", key);
		if(list != null)
		{
			return list;
		}

		List<LanguageVO> languageVOList = getAvailableLanguageVOListForRepository(repositoryId, db); 
		//RepositoryLanguageController.getController().getLanguageVOListForRepositoryId(repositoryId, db);
		
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
			list = new ArrayList<LanguageVO>();
			
			Timer t = new Timer();
			String SQL = "SELECT l.languageId, l.name, l.languageCode, l.charset from cmLanguage l, cmRepositoryLanguage rl where rl.languageId = l.languageId AND rl.repositoryId = ? ORDER BY rl.sortOrder, rl.languageId";
			logger.info("SQL:" + SQL);
			
			Connection conn = (Connection) db.getJdbcConnection();
			
			PreparedStatement psmt = conn.prepareStatement(SQL.toString());
    		psmt.setInt(1, repositoryId);

			ResultSet rs = psmt.executeQuery();
			while(rs.next())
			{
				LanguageVO languageVO = new LanguageVO();
				languageVO.setLanguageId(new Integer(rs.getString(1)));
				languageVO.setName(rs.getString(2));
				languageVO.setLanguageCode(rs.getString(3));
				languageVO.setCharset(rs.getString(4));
				
				logger.info("Found:" + languageVO);
				list.add(languageVO);
			}
			rs.close();
			psmt.close();

			if(list.size() > 0)
	            CacheController.cacheObject("languageCache", key, list);				
		}
	        
        return list;
    } 


    public List getLanguageList(Database db) throws SystemException, Bug
    {
    	return getAllObjects(LanguageImpl.class, "languageId", db);
    }

    public List<LanguageVO> getLanguageVOList(Database db) throws SystemException, Bug, Exception
    {
		String key = "allLanguageVOList";
		List<LanguageVO> languageVOList = (List<LanguageVO>)CacheController.getCachedObject("languageCache", key);
		if(languageVOList != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached languageVOList:" + languageVOList.size());
		}
		else
		{
			languageVOList = new ArrayList<LanguageVO>();
			
			Timer t = new Timer();
			String SQL = "SELECT l.languageId, l.name, l.languageCode, l.charset from cmLanguage l ORDER BY l.languageId";
			logger.info("SQL:" + SQL);
			
			Connection conn = (Connection) db.getJdbcConnection();
			
			PreparedStatement psmt = conn.prepareStatement(SQL.toString());
    		
			ResultSet rs = psmt.executeQuery();
			while(rs.next())
			{
				LanguageVO languageVO = new LanguageVO();
				languageVO.setLanguageId(new Integer(rs.getString(1)));
				languageVO.setName(rs.getString(2));
				languageVO.setLanguageCode(rs.getString(3));
				languageVO.setCharset(rs.getString(4));
				
				logger.info("Found:" + languageVO);
				languageVOList.add(languageVO);
			}
			rs.close();
			psmt.close();
			
			if(languageVOList.size() > 0)
				CacheController.cacheObject("languageCache", key, languageVOList);				
		}
		
		return languageVOList;
    }

    public List<LanguageVO> getLanguageVOList() throws SystemException, Bug
    {
    	String key = "allLanguageVOList";
		List<LanguageVO> languageVOList = (List<LanguageVO>)CacheController.getCachedObject("languageCache", key);
		if(languageVOList != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached languageVOList:" + languageVOList.size());
		}
		else
		{
			languageVOList = new ArrayList<LanguageVO>();
	
			Database db = CastorDatabaseService.getDatabase();
			beginTransaction(db);
	
			try
			{
				languageVOList = getLanguageVOList(db);
				
				if(languageVOList.size() > 0)
					CacheController.cacheObject("languageCache", key, languageVOList);				

				commitTransaction(db);
			}
			catch(Exception e)
			{
				logger.error("An error occurred so we should not complete the transaction:" + e, e);
				rollbackTransaction(db);
				throw new SystemException(e.getMessage());
			}
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
	
			beginTransaction(db);
	
			try
			{
				languageVO = getMasterLanguage(repositoryId, db);
	            
				//If any of the validations or setMethods reported an error, we throw them up now before create. 
				ceb.throwIfNotEmpty();

				if(languageVO != null)
				{
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
			Timer t = new Timer();
			String SQL = "SELECT l.languageId, l.name, l.languageCode, l.charset from cmLanguage l, cmRepositoryLanguage rl where rl.languageId = l.languageId AND rl.repositoryId = ?";
			logger.info("SQL:" + SQL);
			
			Connection conn = (Connection) db.getJdbcConnection();
			
			PreparedStatement psmt = conn.prepareStatement(SQL.toString());
    		psmt.setInt(1, repositoryId);

			ResultSet rs = psmt.executeQuery();
			if(rs.next())
			{
				languageVO = new LanguageVO();
				languageVO.setLanguageId(new Integer(rs.getString(1)));
				languageVO.setName(rs.getString(2));
				languageVO.setLanguageCode(rs.getString(3));
				languageVO.setCharset(rs.getString(4));
				
				logger.info("Found:" + languageVO);
			}
			rs.close();
			psmt.close();
			
			if(logger.isInfoEnabled())
				t.printElapsedTime("Pure JDBC took...");
			if(languageVO != null)
			{
			    CacheController.cacheObject("masterLanguageCache", languageKey, languageVO);
			}						
		}
		
		return languageVO;	
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
	
    
	public List<LanguageVO> getRemainingLanguages(Integer repositoryId) throws ConstraintException, SystemException
	{
		List<LanguageVO> remainingLanguages = new ArrayList<LanguageVO>();

		List<LanguageVO> repositoryLanguageList = LanguageController.getController().getLanguageVOList(repositoryId);
		List<LanguageVO> languageList = LanguageController.getController().getLanguageVOList();
		remainingLanguages.addAll(languageList);
        	
		Iterator<LanguageVO> languageIterator = languageList.iterator();
		while(languageIterator.hasNext())
		{
			LanguageVO languageVO = (LanguageVO)languageIterator.next();
			logger.info("Language:" + languageVO.getName());		
			Iterator<LanguageVO> repositoryLanguageIterator = repositoryLanguageList.iterator();
			while(repositoryLanguageIterator.hasNext())
			{
				LanguageVO repositoryLanguageVO = repositoryLanguageIterator.next();
				logger.info("Comparing" + languageVO.getLanguageId().intValue() + " and " + repositoryLanguageVO.getId().intValue());
				if(languageVO.getLanguageId().intValue() == repositoryLanguageVO.getLanguageId().intValue())
				{
					remainingLanguages.remove(languageVO);
				}
			}
		}

		return remainingLanguages;		
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
 
