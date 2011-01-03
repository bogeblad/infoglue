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
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.RepositoryLanguage;
import org.infoglue.cms.entities.management.RepositoryLanguageVO;
import org.infoglue.cms.entities.management.impl.simple.RepositoryLanguageImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.deliver.util.CacheController;

public class RepositoryLanguageController extends BaseController
{
    private final static Logger logger = Logger.getLogger(RepositoryLanguageController.class.getName());

	/**
	 * Factory method
	 */

	public static RepositoryLanguageController getController()
	{
		return new RepositoryLanguageController();
	}

    public RepositoryLanguage getRepositoryLanguageWithId(Integer id, Database db) throws SystemException, Bug
    {
		return (RepositoryLanguage) getObjectWithId(RepositoryLanguageImpl.class, id, db);
    }


    public RepositoryLanguage getRepositoryLanguageWithId(Integer repositoryLanguageId) throws ConstraintException, SystemException, Bug
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        RepositoryLanguage repositoryLanguage = null;

        try
        {
	        beginTransaction(db);

		    repositoryLanguage = getRepositoryLanguageWithId(repositoryLanguageId, db);
        
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
        
		return repositoryLanguage;
    }
    

	public List getRepositoryLanguageVOList() throws SystemException, Bug
	{
		return getAllVOObjects(RepositoryLanguageImpl.class, "repositoryLanguageImplId");
	}


    public List getRepositoryLanguageVOListWithLanguageId(Integer languageId) throws SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		ArrayList repositoryLanguageList = new ArrayList();
        
	    beginTransaction(db);
	    
        try
        {
         	OQLQuery oql = db.getOQLQuery("SELECT rl FROM org.infoglue.cms.entities.management.impl.simple.RepositoryLanguageImpl rl WHERE rl.language = $1 order by rl.sortOrder, rl.language.languageId");
			oql.bind(languageId);
			
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			while (results.hasMore()) 
            {
                RepositoryLanguage repositoryLanguage = (RepositoryLanguage)results.next();
                repositoryLanguageList.add(repositoryLanguage.getValueObject());
            }

			results.close();
			oql.close();

            ceb.throwIfNotEmpty();
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    
        return repositoryLanguageList;
    }


    public List getRepositoryLanguageVOListWithRepositoryId(Integer repositoryId) throws SystemException, Bug
    {
        List repositoryLanguageList = new ArrayList();
        
		/*
		String repositoryLanguageListKey = "" + repositoryId;
		logger.info("repositoryLanguageListKey:" + repositoryLanguageListKey);
		repositoryLanguageList = (List)CacheController.getCachedObject("repositoryLanguageListCache", repositoryLanguageListKey);
		if(repositoryLanguageList != null)
		{
			logger.info("There was an cached list:" + repositoryLanguageList);
		}
		else
		{
	    */
	    	Database db = CastorDatabaseService.getDatabase();
	        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
			
		    beginTransaction(db);
		    
	        try
	        { 
	            repositoryLanguageList = getRepositoryLanguageVOListWithRepositoryId(repositoryId, db);
	            
	            //if(repositoryLanguageList != null)
	            //{
	            //    CacheController.cacheObject("repositoryLanguageListCache", repositoryLanguageListKey, repositoryLanguageList);
	            //}
	            
	            ceb.throwIfNotEmpty();
	            commitTransaction(db);
	        }
	        catch(Exception e)
	        {
	            logger.error("An error occurred so we should not completes the transaction:" + e, e);
	            rollbackTransaction(db);
	            throw new SystemException(e.getMessage());
	        }
	        //}
		
        return repositoryLanguageList;
    }

	
    public List getRepositoryLanguageVOListWithRepositoryId(Integer repositoryId, Database db) throws SystemException, Bug, Exception
    {
     	ArrayList repositoryLanguageList = new ArrayList();
        
       	OQLQuery oql = db.getOQLQuery("SELECT rl FROM org.infoglue.cms.entities.management.impl.simple.RepositoryLanguageImpl rl WHERE rl.repository = $1 ORDER BY rl.sortOrder, rl.language.languageId");
		oql.bind(repositoryId);
			
       	QueryResults results = oql.execute(Database.ReadOnly);
       		
		while (results.hasMore()) 
        {
		    RepositoryLanguage repositoryLanguage = (RepositoryLanguage)results.next();
            repositoryLanguageList.add(repositoryLanguage);
        }
   
		results.close();
		oql.close();

        return repositoryLanguageList;
    }

    public List<LanguageVO> getLanguageVOListForRepositoryId(Integer repositoryId) throws SystemException, Bug, Exception
    {
        List<LanguageVO> languageVOList = new ArrayList<LanguageVO>();
        
		/*
		String repositoryLanguageListKey = "" + repositoryId;
		logger.info("repositoryLanguageListKey:" + repositoryLanguageListKey);
		repositoryLanguageList = (List)CacheController.getCachedObject("repositoryLanguageListCache", repositoryLanguageListKey);
		if(repositoryLanguageList != null)
		{
			logger.info("There was an cached list:" + repositoryLanguageList);
		}
		else
		{
	    */
	    	Database db = CastorDatabaseService.getDatabase();
	        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
			
		    beginTransaction(db);
		    
	        try
	        { 
	        	languageVOList = getLanguageVOListForRepositoryId(repositoryId, db);
	            
	            //if(repositoryLanguageList != null)
	            //{
	            //    CacheController.cacheObject("repositoryLanguageListCache", repositoryLanguageListKey, repositoryLanguageList);
	            //}
	            
	            ceb.throwIfNotEmpty();
	            commitTransaction(db);
	        }
	        catch(Exception e)
	        {
	            logger.error("An error occurred so we should not completes the transaction:" + e, e);
	            rollbackTransaction(db);
	            throw new SystemException(e.getMessage());
	        }
	        //}
		
        return languageVOList;
    }

    public List<LanguageVO> getLanguageVOListForRepositoryId(Integer repositoryId, Database db) throws SystemException, Bug, Exception
    {
     	List<LanguageVO> languageVOList = new ArrayList<LanguageVO>();
        
       	OQLQuery oql = db.getOQLQuery("SELECT l FROM org.infoglue.cms.entities.management.impl.simple.LanguageImpl l WHERE l.repositoryLanguages.repository = $1 ORDER BY l.repositoryLanguages.sortOrder, l.languageId");
		oql.bind(repositoryId);
			
       	QueryResults results = oql.execute(Database.ReadOnly);
       		
		while (results.hasMore()) 
        {
			Language language = (Language)results.next();
		    languageVOList.add(language.getValueObject());
        }
   
		results.close();
		oql.close();

        return languageVOList;
    }

    
	public List getAvailableLanguageVOListForRepositoryId(Integer repositoryId) throws ConstraintException, SystemException, Exception
	{
		List repositoryLanguageVOList = null;
		
		String repositoryLanguageListKey = "" + repositoryId;
		logger.info("repositoryLanguageListKey:" + repositoryLanguageListKey);
		repositoryLanguageVOList = (List)CacheController.getCachedObject("repositoryLanguageListCache", repositoryLanguageListKey);
		if(repositoryLanguageVOList != null)
		{
			logger.info("There was an cached list:" + repositoryLanguageVOList);
		}
		else
		{
	    	Database db = CastorDatabaseService.getDatabase();
	        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
			
		    beginTransaction(db);
		    
	        try
	        { 
			    List availableRepositoryLanguageList = RepositoryLanguageController.getController().getRepositoryLanguageListWithRepositoryId(repositoryId, db);
				
			    repositoryLanguageVOList = new ArrayList();
			    Iterator i = availableRepositoryLanguageList.iterator();
				while(i.hasNext())
				{
					RepositoryLanguage repositoryLanguage = (RepositoryLanguage)i.next();
					repositoryLanguageVOList.add(repositoryLanguage.getLanguage().getValueObject());
				}
			
                CacheController.cacheObject("repositoryLanguageListCache", repositoryLanguageListKey, repositoryLanguageVOList);
				
				ceb.throwIfNotEmpty();
	            commitTransaction(db);
	        }
	        catch(Exception e)
	        {
	            logger.error("An error occurred so we should not completes the transaction:" + e, e);
	            rollbackTransaction(db);
	            throw new SystemException(e.getMessage());
	        }
		}
		
		return repositoryLanguageVOList;
	}
	
    public List getRepositoryLanguageListWithRepositoryId(Integer repositoryId, Database db) throws SystemException, Bug, Exception
    {
		ArrayList repositoryLanguageList = new ArrayList();
        
       	OQLQuery oql = db.getOQLQuery("SELECT rl FROM org.infoglue.cms.entities.management.impl.simple.RepositoryLanguageImpl rl WHERE rl.repository = $1 ORDER BY rl.sortOrder, rl.language.languageId");
		oql.bind(repositoryId);
			
       	QueryResults results = oql.execute();
		this.logger.info("Fetching entity in read/write mode");

		while (results.hasMore()) 
        {
		    RepositoryLanguage repositoryLanguage = (RepositoryLanguage)results.next();
            repositoryLanguageList.add(repositoryLanguage);
        }
   
		results.close();
		oql.close();

        return repositoryLanguageList;
    }
    
	/**
	 * This method removes a RepositoryLanguage from the system
	 */
	
    public void delete(RepositoryLanguageVO vo) throws ConstraintException, SystemException
    {
    	deleteEntity(RepositoryLanguageImpl.class, vo.getRepositoryLanguageId());
    }    
	
	/*
    public RepositoryLanguage deleteAllRepositoryLanguageWithRepositoryId(Integer repositoryId) throws SystemException, Bug
    {
    	try
    	{
			List repositoryLanguages = getRepositoryLanguageVOListWithRepositoryId(repositoryId);
			Iterator iterator = repositoryLanguages.iterator();
			while(iterator.hasNext()) 
	        {
	            RepositoryLanguageVO repositoryLanguage = (RepositoryLanguageVO)iterator.next();
	            deleteEntity(RepositoryLanguageImpl.class, repositoryLanguage.getRepositoryLanguageId());
	        }
    	}
    	catch(Exception e)
    	{
    		throw new SystemException("An error occurred when we tried to find the matching RepositoryLanguage in the database. Reason: " + e.getMessage(), e);
    	}
    	
		return null;
    }
    */

    
	public void deleteRepositoryLanguages(Repository repository, Database db) throws SystemException, Bug
	{
		try
		{
			Collection repositoryLanguages = repository.getRepositoryLanguages();
			Iterator iterator = repositoryLanguages.iterator();
			while(iterator.hasNext()) 
			{
				RepositoryLanguage repositoryLanguage = (RepositoryLanguage)iterator.next();
				Language language = repositoryLanguage.getLanguage();
				language.getRepositoryLanguages().remove(repositoryLanguage);
				
				db.remove(repositoryLanguage);
				//deleteEntity(RepositoryLanguageImpl.class, repositoryLanguage.getRepositoryLanguageId(), db);
			}
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to find the matching RepositoryLanguage in the database. Reason: " + e.getMessage(), e);
		}
	}
	
    /*
	public RepositoryLanguage deleteAllRepositoryLanguageWithRepositoryId(Integer repositoryId, Database db) throws SystemException, Bug
	{
		try
		{
			List repositoryLanguages = getRepositoryLanguageVOListWithRepositoryId(repositoryId);
			Iterator iterator = repositoryLanguages.iterator();
			while(iterator.hasNext()) 
			{
				RepositoryLanguageVO repositoryLanguage = (RepositoryLanguageVO)iterator.next();
				deleteEntity(RepositoryLanguageImpl.class, repositoryLanguage.getRepositoryLanguageId(), db);
			}
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to find the matching RepositoryLanguage in the database. Reason: " + e.getMessage(), e);
		}
    	
		return null;
	}
	*/

	/**
	 * This method deletes all repositoryLanguages with a certain languageId.
	 */
	
    public RepositoryLanguage deleteAllRepositoryLanguageWithLanguageId(Integer languageId) throws SystemException, Bug
    {
    	try
    	{
			List repositoryLanguages = getRepositoryLanguageVOListWithLanguageId(languageId);
			Iterator iterator = repositoryLanguages.iterator();
			while(iterator.hasNext()) 
	        {
	            RepositoryLanguageVO repositoryLanguage = (RepositoryLanguageVO)iterator.next();
				deleteEntity(RepositoryLanguageImpl.class, repositoryLanguage.getRepositoryLanguageId());
	        }
    	}
    	catch(Exception e)
    	{
    		throw new SystemException("An error occurred when we tried to find the matching RepositoryLanguage in the database. Reason: " + e.getMessage(), e);
    	}
    	
		return null;
    }
    
	/**
	 * This method deletes all repositoryLanguages with a certain languageId.
	 */
	
	public RepositoryLanguage deleteAllRepositoryLanguageWithLanguage(Language language, Database db) throws SystemException, Bug
	{
		try
		{
			Collection repositoryLanguages = language.getRepositoryLanguages();
			Iterator iterator = repositoryLanguages.iterator();
			while(iterator.hasNext()) 
			{
				RepositoryLanguage repositoryLanguage = (RepositoryLanguage)iterator.next();
				Repository repository = repositoryLanguage.getRepository();
				repository.getRepositoryLanguages().remove(repositoryLanguage);
				db.remove(repositoryLanguage);
			}
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to delete a RepositoryLanguage in the database. Reason: " + e.getMessage(), e);
		}
    	
		return null;
	}

	 
	public RepositoryLanguage create(Integer repositoryId, Integer languageId, Integer sortOrder, Database db) throws Exception
	{
		RepositoryLanguage repositoryLanguage = new RepositoryLanguageImpl();

		repositoryLanguage.setIsPublished(new Boolean(false));
		repositoryLanguage.setLanguage(LanguageController.getController().getLanguageWithId(languageId, db));
		repositoryLanguage.setRepository(RepositoryController.getController().getRepositoryWithId(repositoryId, db));
		repositoryLanguage.setSortOrder(sortOrder);
		
		db.create(repositoryLanguage);

		return repositoryLanguage;
	}

	public RepositoryLanguage create(Repository repository, Language language, Integer sortOrder, Database db) throws Exception
	{
		RepositoryLanguage repositoryLanguage = new RepositoryLanguageImpl();

		repositoryLanguage.setIsPublished(new Boolean(false));
		repositoryLanguage.setLanguage(language);
		repositoryLanguage.setRepository(repository);
		repositoryLanguage.setSortOrder(sortOrder);
		
		db.create(repositoryLanguage);

		return repositoryLanguage;
	}

	public void publishRepositoryLanguage(RepositoryLanguageVO repositoryLanguageVO) throws ConstraintException, SystemException
	 {
		 Database db = CastorDatabaseService.getDatabase();
		 ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		 RepositoryLanguage repositoryLanguage = null;

		 beginTransaction(db);

		 try
		 {
			 repositoryLanguage = getRepositoryLanguageWithId(repositoryLanguageVO.getRepositoryLanguageId(), db);
			 repositoryLanguage.setIsPublished(new Boolean(true));
			 commitTransaction(db); 
		 }
		 catch(Exception e)
		 {
			 logger.error("An error occurred so we should not complete the transaction:" + e, e);
			 rollbackTransaction(db);
			 throw new SystemException(e.getMessage());
		 }
	 }    
    
    
	 public void unpublishRepositoryLanguage(RepositoryLanguageVO repositoryLanguageVO) throws ConstraintException, SystemException
	 {
		 Database db = CastorDatabaseService.getDatabase();
		 ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		 RepositoryLanguage repositoryLanguage = null;

		 beginTransaction(db);

		 try
		 {
			 //Here we should do some serious cleanup.... take away all content-attributes that has this language for example?
			 repositoryLanguage = getRepositoryLanguageWithId(repositoryLanguageVO.getRepositoryLanguageId(), db);
			 repositoryLanguage.setIsPublished(new Boolean(false));
			 commitTransaction(db); 
		 }
		 catch(Exception e)
		 {
			 logger.error("An error occurred so we should not complete the transaction:" + e, e);
			 rollbackTransaction(db);
			 throw new SystemException(e.getMessage());
		 }
	 }        
    
	 
	 public void moveRepositoryLanguage(RepositoryLanguageVO repositoryLanguageVO, boolean down) throws ConstraintException, SystemException
	 {
		 Database db = CastorDatabaseService.getDatabase();
		 ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		 beginTransaction(db);

		 try
		 {
		     RepositoryLanguage originalRepositoryLanguage = this.getRepositoryLanguageWithId(repositoryLanguageVO.getRepositoryLanguageId(), db);
		     List repositoryLanguages = this.getRepositoryLanguageListWithRepositoryId(originalRepositoryLanguage.getRepository().getId(), db);
			 
			 for(int i=0; i<repositoryLanguages.size(); i++)
			 {
			     RepositoryLanguage repositoryLanguage = (RepositoryLanguage)repositoryLanguages.get(i);
			     if(repositoryLanguage.getRepositoryLanguageId().intValue() == repositoryLanguageVO.getRepositoryLanguageId().intValue())
			     {
			         if(down && i != repositoryLanguages.size() - 1)
			         {
			             RepositoryLanguage nextRepositoryLanguage = (RepositoryLanguage)repositoryLanguages.get(i+1);
			             Integer currentSortOrder = repositoryLanguage.getSortOrder();
			             repositoryLanguage.setSortOrder(nextRepositoryLanguage.getSortOrder()); 
			             nextRepositoryLanguage.setSortOrder(currentSortOrder);
			         }
			         else if(!down && i != 0)
			         {
			             RepositoryLanguage previousRepositoryLanguage = (RepositoryLanguage)repositoryLanguages.get(i-1);
			             Integer currentSortOrder = repositoryLanguage.getSortOrder();
			             repositoryLanguage.setSortOrder(previousRepositoryLanguage.getSortOrder()); 
			             previousRepositoryLanguage.setSortOrder(currentSortOrder);             
			         }
			     }
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
    

	 public void createRepositoryLanguage(Integer repositoryId, Integer languageId, Integer sortOrder) throws ConstraintException, SystemException
	 {
		 Database db = CastorDatabaseService.getDatabase();
		 ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		 RepositoryLanguage repositoryLanguage = null;

		 beginTransaction(db);

		 try
		 {
			 repositoryLanguage = create(repositoryId, languageId, sortOrder, db);
			 commitTransaction(db); 
		 }
		 catch(Exception e)
		 {
			 logger.error("An error occurred so we should not complete the transaction:" + e, e);
			 rollbackTransaction(db);
			 throw new SystemException(e.getMessage());
		 }
	 }
	
	
	 public void updateRepositoryLanguages(Integer repositoryId, String[] languageValues) throws ConstraintException, SystemException
	 {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {			
    		Repository repository = RepositoryController.getController().getRepositoryWithId(repositoryId, db);
    		deleteRepositoryLanguages(repository, db);

        	//add validation here if needed   			
            List repositoryLanguageList = new ArrayList();
            if(languageValues != null)
			{
				for (int i=0; i < languageValues.length; i++)
	            {
	            	Language language = LanguageController.getController().getLanguageWithId(new Integer(languageValues[i]), db);
	            	RepositoryLanguage repositoryLanguage = create(repository.getId(), new Integer(languageValues[i]), new Integer(i), db);
	            	repositoryLanguageList.add(repositoryLanguage);
	            }
			}
			
			//repository = RepositoryController.getController().getRepositoryWithId(repositoryVO.getRepositoryId(), db);
            //repository.setValueObject(repositoryVO);
            repository.setRepositoryLanguages(repositoryLanguageList);
			
            //If any of the validations or setMethods reported an error, we throw them up now before create.
            ceb.throwIfNotEmpty();
            
            commitTransaction(db);
        }
        catch(ConstraintException ce)
        {
            logger.warn("An error occurred so we should not completes the transaction:" + ce, ce);
            rollbackTransaction(db);
            throw ce;
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    } 
    
	public List getRemainingLanguages(Integer repositoryId) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		List remainingLanguages = new ArrayList();

		beginTransaction(db);

		try
		{
			Repository repository = RepositoryController.getController().getRepositoryWithId(repositoryId, db);
			Collection repositoryLanguageList = repository.getRepositoryLanguages();
			List languageList = LanguageController.getController().getLanguageVOList();
			remainingLanguages.addAll(languageList);
        	
			Iterator languageIterator = languageList.iterator();
			while(languageIterator.hasNext())
			{
				LanguageVO languageVO = (LanguageVO)languageIterator.next();
				logger.info("Language:" + languageVO.getName());		
				Iterator repositoryLanguageIterator = repositoryLanguageList.iterator();
				while(repositoryLanguageIterator.hasNext())
				{
					RepositoryLanguage repositoryLanguage = (RepositoryLanguage)repositoryLanguageIterator.next();
					logger.info("Comparing" + languageVO.getLanguageId().intValue() + " and " + repositoryLanguage.getLanguage().getLanguageId().intValue());
					if(languageVO.getLanguageId().intValue() == repositoryLanguage.getLanguage().getLanguageId().intValue())
					{
						remainingLanguages.remove(languageVO);
					}
				}
			}
        	
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
			e.printStackTrace();
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}

		return remainingLanguages;		
	}


	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new RepositoryLanguageVO();
	}
   
}



 
