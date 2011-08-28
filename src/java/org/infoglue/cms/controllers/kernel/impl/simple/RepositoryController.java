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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.RepositoryLanguage;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.deliver.util.CacheController;

public class RepositoryController extends BaseController
{
    private final static Logger logger = Logger.getLogger(RepositoryController.class.getName());

	/**
	 * Factory method
	 */

	public static RepositoryController getController()
	{
		return new RepositoryController();
	}
	
    public RepositoryVO create(RepositoryVO vo) throws ConstraintException, SystemException
    {
        Repository ent = new RepositoryImpl();
        ent.setValueObject(vo);
        ent = (Repository) createEntity(ent);
        return ent.getValueObject();
    }     

    public Repository create(RepositoryVO vo, Database db) throws ConstraintException, SystemException, Exception
    {
        Repository ent = new RepositoryImpl();
        ent.setValueObject(vo);
        ent = (Repository) createEntity(ent, db);
        return ent;
    }     

	/**
	 * This method removes a Repository from the system and also cleans out all depending repositoryLanguages.
	 */
	
    public void markForDelete(RepositoryVO repositoryVO, String userName, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException
    {
    	markForDelete(repositoryVO, userName, false, infoGluePrincipal);
    }
    
	/**
	 * This method sets a Repository in markedForDelete mode.
	 */
	
    public void markForDelete(RepositoryVO repositoryVO, String userName, boolean forceDelete, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException
    {
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		Repository repository = null;
	
		beginTransaction(db);

		try
		{
			repository = getRepositoryWithId(repositoryVO.getRepositoryId(), db);
			repository.setIsDeleted(true);
			
			/*
			List<Content> contentList = ContentControllerProxy.getContentController().getRepositoryContents(repositoryId, db);
			Iterator<Content> contentListIterator = contentList.iterator();
			while(contentListIterator.hasNext())
			{
				Content content = contentListIterator.next();
				content.setIsDeleted(false);
			}
			
			List<SiteNode> siteNodeList = SiteNodeControllerProxy.getController().getRepositorySiteNodes(repositoryId, db);
			Iterator<SiteNode> siteNodeListIterator = siteNodeList.iterator();
			while(siteNodeListIterator.hasNext())
			{
				SiteNode siteNode = siteNodeListIterator.next();
				siteNode.setIsDeleted(false);
			}
			*/

			ContentVO contentVO = ContentControllerProxy.getController().getRootContentVO(repositoryVO.getRepositoryId(), userName, false);
			if(contentVO != null)
			{
				if(forceDelete)
					ContentController.getContentController().markForDeletion(contentVO, db, true, true, true, infoGluePrincipal);
				else
					ContentController.getContentController().markForDeletion(contentVO, infoGluePrincipal, db);
			}
			
			SiteNodeVO siteNodeVO = SiteNodeController.getController().getRootSiteNodeVO(repositoryVO.getRepositoryId());
			if(siteNodeVO != null)
			{
				if(forceDelete)
					SiteNodeController.getController().markForDeletion(siteNodeVO, db, true, infoGluePrincipal);
				else
					SiteNodeController.getController().markForDeletion(siteNodeVO, db, infoGluePrincipal);
			}
			
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

	/**
	 * This method sets a Repository in markedForDelete mode.
	 */
	
    public void restoreRepository(Integer repositoryId, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException
    {
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		Repository repository = null;
	
		beginTransaction(db);

		try
		{
			repository = getRepositoryWithId(repositoryId, db);
			repository.setIsDeleted(false);
			
			List<Content> contentList = ContentControllerProxy.getContentController().getRepositoryContents(repositoryId, db);
			Iterator<Content> contentListIterator = contentList.iterator();
			while(contentListIterator.hasNext())
			{
				Content content = contentListIterator.next();
				content.setIsDeleted(false);
			}
			
			List<SiteNode> siteNodeList = SiteNodeControllerProxy.getController().getRepositorySiteNodes(repositoryId, db);
			Iterator<SiteNode> siteNodeListIterator = siteNodeList.iterator();
			while(siteNodeListIterator.hasNext())
			{
				SiteNode siteNode = siteNodeListIterator.next();
				siteNode.setIsDeleted(false);
			}
			
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

	/**
	 * This method removes a Repository from the system and also cleans out all depending repositoryLanguages.
	 */
	
    public void delete(RepositoryVO repositoryVO, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException
    {
    	delete(repositoryVO, false, infoGluePrincipal);
    }

    
	/**
	 * This method removes a Repository from the system and also cleans out all depending repositoryLanguages.
	 */
	
    public void delete(Integer repositoryId, boolean forceDelete, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException
    {
    	RepositoryVO repositoryVO = getRepositoryVOWithId(repositoryId);
    	
    	delete(repositoryVO, forceDelete, infoGluePrincipal);
    }
    
	/**
	 * This method removes a Repository from the system and also cleans out all depending repositoryLanguages.
	 */
	
    public void delete(RepositoryVO repositoryVO, boolean forceDelete, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException
    {
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		Repository repository = null;
	
		beginTransaction(db);

		try
		{
			repository = getRepositoryWithId(repositoryVO.getRepositoryId(), db);
			
			RepositoryLanguageController.getController().deleteRepositoryLanguages(repository, db);
			
			ContentVO contentVO = ContentControllerProxy.getController().getRootContentVO(repositoryVO.getRepositoryId(), infoGluePrincipal.getName(), false);
			if(contentVO != null)
			{
				if(forceDelete)
					ContentController.getContentController().delete(contentVO, db, true, true, true, infoGluePrincipal);
				else
					ContentController.getContentController().delete(contentVO, infoGluePrincipal, db);
			}
			
			SiteNodeVO siteNodeVO = SiteNodeController.getController().getRootSiteNodeVO(repositoryVO.getRepositoryId());
			if(siteNodeVO != null)
			{
				if(forceDelete)
					SiteNodeController.getController().delete(siteNodeVO, db, true, infoGluePrincipal);
				else
					SiteNodeController.getController().delete(siteNodeVO, db, true, infoGluePrincipal);
			}
			
			deleteEntity(RepositoryImpl.class, repositoryVO.getRepositoryId(), db);
	
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
    
    
    public RepositoryVO update(RepositoryVO vo) throws ConstraintException, SystemException
    {
    	return (RepositoryVO) updateEntity(RepositoryImpl.class, (BaseEntityVO) vo);
    }        
    
    public RepositoryVO update(RepositoryVO repositoryVO, String[] languageValues) throws ConstraintException, SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
        	Repository repository = RepositoryController.getController().getRepositoryWithId(repositoryVO.getRepositoryId(), db);
        	
        	RepositoryLanguageController.getController().deleteRepositoryLanguages(repository, db);

        	//add validation here if needed   			
            List repositoryLanguageList = new ArrayList();
            if(languageValues != null)
			{
				for (int i=0; i < languageValues.length; i++)
	            {
	            	Language language = LanguageController.getController().getLanguageWithId(new Integer(languageValues[i]), db);
	            	RepositoryLanguage repositoryLanguage = RepositoryLanguageController.getController().create(repositoryVO.getRepositoryId(), new Integer(languageValues[i]), new Integer(i), db);
	            	repositoryLanguageList.add(repositoryLanguage);
					language.getRepositoryLanguages().add(repositoryLanguage);
	            }
			}
			
			repository.setValueObject(repositoryVO);
			repository.setRepositoryLanguages(repositoryLanguageList);
			
			repositoryVO = repository.getValueObject();
			
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

        return repositoryVO;
    }        
    
	// Singe object
    public Repository getRepositoryWithId(Integer id, Database db) throws SystemException, Bug
    {
		return (Repository) getObjectWithId(RepositoryImpl.class, id, db);
    }

    public RepositoryVO getRepositoryVOWithId(Integer repositoryId) throws ConstraintException, SystemException, Bug
    {
		return  (RepositoryVO) getVOWithId(RepositoryImpl.class, repositoryId);        
    }
	
    public RepositoryVO getRepositoryVOWithId(Integer repositoryId, Database db) throws ConstraintException, SystemException, Bug
    {
		return  (RepositoryVO) getVOWithId(RepositoryImpl.class, repositoryId, db);        
    }
    
	/**
	 * Returns the RepositoryVO with the given name.
	 * 
	 * @param name
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public RepositoryVO getRepositoryVOWithName(String name) throws SystemException, Bug
	{
		RepositoryVO repositoryVO = null;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);

			repositoryVO = getRepositoryVOWithName(name, db);
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return repositoryVO;	
	}

	/**
	 * Returns the Repository with the given name fetched within a given transaction.
	 * 
	 * @param name
	 * @param db
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */

	public RepositoryVO getRepositoryVOWithName(String name, Database db) throws SystemException, Bug
	{
		RepositoryVO repositoryVO = null;
		
		try
		{
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.RepositoryImpl f WHERE f.name = $1 AND f.isDeleted = $2");
			oql.bind(name);
			oql.bind(false);
			
			QueryResults results = oql.execute(Database.READONLY);

			if (results.hasMore()) 
			{
				Repository repository = (Repository)results.next();
				repositoryVO = repository.getValueObject();
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a named repository. Reason:" + e.getMessage(), e);    
		}
		
		return repositoryVO;		
	}

	/**
	 * Returns the Repository with the given name fetched within a given transaction.
	 * 
	 * @param name
	 * @param db
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */

	public Repository getRepositoryWithName(String name, Database db) throws SystemException, Bug
	{
		Repository repository = null;
		
		try
		{
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.RepositoryImpl f WHERE f.name = $1");
			oql.bind(name);
			
			QueryResults results = oql.execute();
			this.logger.info("Fetching entity in read/write mode" + name);

			if (results.hasMore()) 
			{
				repository = (Repository)results.next();
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a named repository. Reason:" + e.getMessage(), e);    
		}
		
		return repository;		
	}

	/**
	 * This method can be used by actions and use-case-controllers that only need to have simple access to the
	 * functionality. They don't get the transaction-safety but probably just wants to show the info.
	 */	
    
    public List getRepositoryVOList() throws ConstraintException, SystemException, Bug
    {   
		String key = "repositoryVOList";
		logger.info("key:" + key);
		List cachedRepositoryVOList = (List)CacheController.getCachedObject("repositoryCache", key);
		if(cachedRepositoryVOList != null)
		{
			logger.info("There was an cached authorization:" + cachedRepositoryVOList.size());
			return cachedRepositoryVOList;
		}
				
		List repositoryVOList = getAllVOObjects(RepositoryImpl.class, "repositoryId");

		CacheController.cacheObject("repositoryCache", key, repositoryVOList);
			
		return repositoryVOList;
    }

    
	/**
	 * This method can be used by actions and use-case-controllers that only need to have simple access to the
	 * functionality. They don't get the transaction-safety but probably just wants to show the info.
	 */	
	
	public List getAuthorizedRepositoryVOList(InfoGluePrincipal infoGluePrincipal, boolean isBindingDialog) throws ConstraintException, SystemException, Bug
	{    	
		List accessableRepositories = new ArrayList();
    	
		List allRepositories = this.getRepositoryVOListNotMarkedForDeletion();
		Iterator i = allRepositories.iterator();
		while(i.hasNext())
		{
			RepositoryVO repositoryVO = (RepositoryVO)i.next();
			if(getIsAccessApproved(repositoryVO.getRepositoryId(), infoGluePrincipal, isBindingDialog))
			{
				accessableRepositories.add(repositoryVO);
			}
		}
    	
		Collections.sort(accessableRepositories, new ReflectionComparator("name"));

		return accessableRepositories;
	}


	/**
	 * Returns a repository list marked for deletion.
	 */
	
	public List<RepositoryVO> getRepositoryVOListNotMarkedForDeletion() throws SystemException, Bug
	{
		String key = "repositoryVOListActive";
		logger.info("key:" + key);
		List cachedRepositoryVOList = (List)CacheController.getCachedObject("repositoryCache", key);
		if(cachedRepositoryVOList != null)
		{
			logger.info("There was an cached repositoryList:" + cachedRepositoryVOList.size());
			return cachedRepositoryVOList;
		}

		Database db = CastorDatabaseService.getDatabase();
		
		List<RepositoryVO> repositoryVOListNotMarkedForDeletion = new ArrayList<RepositoryVO>();
		
		try 
		{
			beginTransaction(db);
		
			OQLQuery oql = db.getOQLQuery("SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RepositoryImpl r WHERE r.isDeleted = $1 ORDER BY r.repositoryId");
			oql.bind(false);
			
			QueryResults results = oql.execute(Database.READONLY);
			while (results.hasMore()) 
            {
                Repository repository = (Repository)results.next();
                repositoryVOListNotMarkedForDeletion.add(repository.getValueObject());
            }
            
			results.close();
			oql.close();

			commitTransaction(db);
		}
		catch ( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of deleted repositories. Reason:" + e.getMessage(), e);			
		}

		CacheController.cacheObject("repositoryCache", key, repositoryVOListNotMarkedForDeletion);
			
		return repositoryVOListNotMarkedForDeletion;
	}

	
	/**
	 * Returns a repository list marked for deletion.
	 */
	
	public List<RepositoryVO> getRepositoryVOListMarkedForDeletion() throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();
		
		List<RepositoryVO> repositoryVOListMarkedForDeletion = new ArrayList<RepositoryVO>();
		
		try 
		{
			beginTransaction(db);
		
			OQLQuery oql = db.getOQLQuery("SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RepositoryImpl r WHERE r.isDeleted = $1 ORDER BY r.repositoryId");
			oql.bind(true);
			
			QueryResults results = oql.execute(Database.READONLY);
			while (results.hasMore()) 
            {
                Repository repository = (Repository)results.next();
                repositoryVOListMarkedForDeletion.add(repository.getValueObject());
            }
            
			results.close();
			oql.close();

			commitTransaction(db);
		}
		catch ( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of deleted repositories. Reason:" + e.getMessage(), e);			
		}
		
		return repositoryVOListMarkedForDeletion;		
	}

	
	/**
	 * Return the first of all repositories.
	 */
	
	public RepositoryVO getFirstRepositoryVO()  throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();
		RepositoryVO repositoryVO = null;
		
		try 
		{
			beginTransaction(db);
		
			OQLQuery oql = db.getOQLQuery("SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RepositoryImpl r ORDER BY r.repositoryId");
        	QueryResults results = oql.execute();
			this.logger.info("Fetching entity in read/write mode");

			if (results.hasMore()) 
            {
                Repository repository = (Repository)results.next();
                repositoryVO = repository.getValueObject();
            }
            
			results.close();
			oql.close();

			commitTransaction(db);
		}
		catch ( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of roles in the repository. Reason:" + e.getMessage(), e);			
		}
		return repositoryVO;		
	}
	


	/**
	 * This method deletes the Repository sent in from the system.
	 */	
	public void delete(Integer repositoryId, Database db) throws SystemException, Bug
	{
		try
		{
			db.remove(getRepositoryWithId(repositoryId, db));
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to delete Repository in the database. Reason: " + e.getMessage(), e);
		}	
	} 


	/**
	 * This method returns true if the user should have access to the repository sent in.
	 */
    
	public boolean getIsAccessApproved(Integer repositoryId, InfoGluePrincipal infoGluePrincipal, boolean isBindingDialog) throws SystemException
	{
		logger.info("getIsAccessApproved for " + repositoryId + " AND " + infoGluePrincipal + " AND " + isBindingDialog);
		boolean hasAccess = false;
    	
		Database db = CastorDatabaseService.getDatabase();
       
		beginTransaction(db);

		try
		{ 
		    if(isBindingDialog)
		        hasAccess = (AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "Repository.Read", repositoryId.toString()) || AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "Repository.ReadForBinding", repositoryId.toString()));
		    else
		        hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "Repository.Read", repositoryId.toString()); 
		        
			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return hasAccess;
	}	
    
	
	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new RepositoryVO();
	}
		
}
 
