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

package org.infoglue.deliver.controllers.kernel.impl.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.RepositoryLanguage;
import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.NullObject;
import org.infoglue.deliver.util.Timer;


public class LanguageDeliveryController extends BaseDeliveryController
{
    private final static Logger logger = Logger.getLogger(LanguageDeliveryController.class.getName());
    
    private final static LanguageDeliveryController languageDeliveryController = new LanguageDeliveryController();

	/**
	 * Private constructor to enforce factory-use
	 */
	
	private LanguageDeliveryController()
	{
	}
	
	/**
	 * Factory method
	 */
	
	public static LanguageDeliveryController getLanguageDeliveryController()
	{
		return languageDeliveryController;
	}
	
	
	/**
	 * This method return a LanguageVO
	 */
	
	public LanguageVO getLanguageVO(Database db, Integer languageId) throws SystemException, Exception
	{
		if(languageId == null || languageId.intValue() == 0)
			return null;
			
		String key = "" + languageId;
		logger.info("key:" + key);
		LanguageVO languageVO = (LanguageVO)CacheController.getCachedObject("languageCache", key);
		if(languageVO != null)
		{
			logger.info("There was an cached languageVO:" + languageVO);
		}
		else
		{
			Language language = (Language)getObjectWithId(LanguageImpl.class, languageId, db);
				
			if(language != null)
				languageVO = language.getValueObject();
            
			CacheController.cacheObject("languageCache", key, languageVO);				
		}
				
		return languageVO;
	}

	/**
	 * This method returns all languages for a certain repository.
	 * 
	 * @param repositoryId
	 * @return
	 * @throws SystemException
	 * @throws Exception
	 */

	public List getAvailableLanguagesForRepository(Database db, Integer repositoryId) throws SystemException, Exception
    {
		String key = "" + repositoryId + "_allLanguages";
		logger.info("key:" + key);
		List list = (List)CacheController.getCachedObject("languageCache", key);
		if(list != null)
		{
			logger.info("There was an cached list:" + list);
		}
		else
		{
			list = new ArrayList();
			/* */ 
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
			/* */

			/*
			Repository repository = (Repository) getObjectWithId(RepositoryImpl.class, repositoryId, db);
	        if(logger.isInfoEnabled())
	        	logger.info("repository:" + repository);
	        
	        if (repository != null) 
	        {
	        	if(logger.isInfoEnabled())
		        	logger.info("repository:" + repository);
		        
	        	Collection repositoryLanguages = repository.getRepositoryLanguages();
	        	if(logger.isInfoEnabled())
		        	logger.info("repositoryLanguages:" + repositoryLanguages.size());
	        	
	        	Iterator repositoryLanguagesIterator = repositoryLanguages.iterator();
	        	while (repositoryLanguagesIterator.hasNext()) 
	            {
	                RepositoryLanguage repositoryLanguage = (RepositoryLanguage) repositoryLanguagesIterator.next();
		        	if(logger.isInfoEnabled())
			        	logger.info("repositoryLanguage:" + repositoryLanguage);

		        	Language language = repositoryLanguage.getLanguage();
	                if (language != null)
	                    list.add(language.getValueObject());
	            }
	        }
        	*/
			
	        if(list.size() > 0)
	            CacheController.cacheObject("languageCache", key, list);				
		}
	        
        return list;
    } 
	
	/**
	 * This method returns the languages assigned to a respository. 
	 */
	
	public List getAvailableLanguages(Database db, Integer siteNodeId) throws SystemException, Exception
	{ 
		logger.info("getAvailableLanguages for " + siteNodeId + " start.");

		List languageVOList = new ArrayList();

        logger.info("siteNodeId:" + siteNodeId);

        SiteNode siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
			
		if(siteNode != null)
		{
			List repositoryLanguages = getAvailableLanguagesForRepository(db, siteNode.getValueObject().getRepositoryId());

			if(logger.isInfoEnabled())
				logger.info("repositoryLanguages:" + repositoryLanguages.size());
 			Iterator repositoryLanguagesIterator = repositoryLanguages.iterator();
 			while(repositoryLanguagesIterator.hasNext())
 			{
 				LanguageVO language = (LanguageVO)repositoryLanguagesIterator.next();
 				if(language != null)
 				{
 					logger.info("Adding " + language.getName() + " to the list of available languages");
     				languageVOList.add(language);
 				}
 			}
 			
			/*
			Repository repository = siteNode.getRepository();
     		if(repository != null)
			{
     		    logger.info("repository:" + repository.getName());

     		    Collection repositoryLanguages = repository.getRepositoryLanguages();
     		    logger.info("repositoryLanguages:" + repositoryLanguages.size());
     			Iterator repositoryLanguagesIterator = repositoryLanguages.iterator();
     			while(repositoryLanguagesIterator.hasNext())
     			{
     				RepositoryLanguage repositoryLanguage = (RepositoryLanguage)repositoryLanguagesIterator.next();
     				Language language = repositoryLanguage.getLanguage();
     				if(language != null)
     				{
     					logger.info("Adding " + language.getName() + " to the list of available languages");
         				languageVOList.add(language.getValueObject());
     				}
     			}
			}
			*/
		}

		logger.info("getAvailableLanguages for " + siteNodeId + " end.");

        return languageVOList;	
	}


	/**
	 * This method returns the master language. 
	 * todo - add attribute on repositoryLanguage to be able to sort them... and then fetch the first
	 */
	
	public LanguageVO getMasterLanguage(Database db, String repositoryName) throws SystemException, Exception
	{ 
        Language language = null;

     	OQLQuery oql = db.getOQLQuery( "SELECT l FROM org.infoglue.cms.entities.management.impl.simple.LanguageImpl l WHERE l.repositoryLanguages.repository.name = $1 ORDER BY l.repositoryLanguages.sortOrder, l.languageId");
		oql.bind(repositoryName);
		
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		if (results.hasMore()) 
        {
        	language = (Language)results.next();
        }
          
		results.close();
		oql.close();

        return (language == null) ? null : language.getValueObject();	
	}
	

	/**
	 * This method returns the master language. 
	 * todo - add attribute on repositoryLanguage to be able to sort them... and then fetch the first
	 */
	
	public LanguageVO getMasterLanguageForRepository(Database db, Integer repositoryId) throws SystemException, Exception
	{ 
		String languageKey = "" + repositoryId;
		if(logger.isInfoEnabled())
			logger.info("languageKey in getMasterLanguageForRepository:" + languageKey);
		LanguageVO languageVO = (LanguageVO)CacheController.getCachedObject("masterLanguageCache", languageKey);
		if(languageVO != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached master language:" + languageVO.getName());
		}
		else
		{
			OQLQuery oql = db.getOQLQuery( "SELECT l FROM org.infoglue.cms.entities.management.impl.simple.LanguageImpl l WHERE l.repositoryLanguages.repository.repositoryId = $1 ORDER BY l.repositoryLanguages.sortOrder, l.languageId");
			oql.bind(repositoryId);
			
			QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
			{
				Language language = (Language)results.next();
				languageVO = language.getValueObject();
			}
			
			results.close();
			oql.close();

			CacheController.cacheObject("masterLanguageCache", languageKey, languageVO);
		}

		return languageVO;	
	}

	/**
	 * This method returns the master language. 
	 * todo - add attribute on repositoryLanguage to be able to sort them... and then fetch the first
	 */
	
	public LanguageVO getMasterLanguageForRepository(Integer repositoryId, Database db) throws SystemException, Exception
	{ 
		LanguageVO languageVO = null;

		String languageKey = "" + repositoryId;
		logger.info("languageKey in getMasterLanguageForRepository:" + languageKey);
		languageVO = (LanguageVO)CacheController.getCachedObject("masterLanguageCache", languageKey);
		if(languageVO != null)
		{
			logger.info("There was an cached master language:" + languageVO.getName());
		}
		else
		{
			OQLQuery oql = db.getOQLQuery( "SELECT l FROM org.infoglue.cms.entities.management.impl.simple.LanguageImpl l WHERE l.repositoryLanguages.repository.repositoryId = $1 ORDER BY l.repositoryLanguages.sortOrder, l.languageId");
			oql.bind(repositoryId);
			
			QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
			{
				Language language = (Language)results.next();
				languageVO = language.getValueObject();
			}
			
			results.close();
			oql.close();

			CacheController.cacheObject("masterLanguageCache", languageKey, languageVO);
		}

		return languageVO;	
	}

	/**
	 * This method returns the master language. 
	 * todo - add attribute on repositoryLanguage to be able to sort them... and then fetch the first
	 */
	public LanguageVO getMasterLanguageForSiteNode(Database db, Integer siteNodeId) throws SystemException, Exception
	{ 
		SiteNodeVO smallestSiteNodeVO = SiteNodeController.getSmallSiteNodeVOWithId(siteNodeId, db);
		return getMasterLanguageForRepository(smallestSiteNodeVO.getRepositoryId(), db);
	}

	/**
	 * This method returns the master language. 
	 * todo - add attribute on repositoryLanguage to be able to sort them... and then fetch the first
	 */
	/*
	public LanguageVO getMasterLanguageForSiteNode(Database db, Integer siteNodeId) throws SystemException, Exception
	{ 
	    String languageKey = "siteNodeId_" + siteNodeId;
		logger.info("languageKey in getMasterLanguageForSiteNode:" + languageKey);
		LanguageVO languageVO = (LanguageVO)CacheController.getCachedObject("masterLanguageCache", languageKey);
		if(languageVO != null)
		{
		    logger.info("There was an cached master language:" + languageVO.getName());
		}
		else
		{
			SiteNode siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
			Integer repositoryId = siteNode.getRepository().getRepositoryId();
         	
			OQLQuery oql = db.getOQLQuery( "SELECT l FROM org.infoglue.cms.entities.management.impl.simple.LanguageImpl l WHERE l.repositoryLanguages.repository.repositoryId = $1 ORDER BY l.repositoryLanguages.sortOrder, l.languageId");
			oql.bind(repositoryId);
			
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
            {
				Language language = (Language)results.next();
				languageVO = language.getValueObject();
            }
			
			results.close();
			oql.close();
			
			CacheController.cacheObject("masterLanguageCache", languageKey, languageVO);
		}
		
        return languageVO;	
	}
	*/
	
	/**
	 * This method returns the master language. 
	 * todo - add attribute on repositoryLanguage to be able to sort them... and then fetch the first
	 */
	
	public LanguageVO getMasterLanguageForSiteNodeWithValityCheck(Database db, NodeDeliveryController ndc, Integer siteNodeId) throws SystemException, Exception
	{ 
	    String languageKey = "validLanguage_siteNodeId_" + siteNodeId;
		logger.info("languageKey in getMasterLanguageForSiteNode:" + languageKey);
		LanguageVO languageVO = (LanguageVO)CacheController.getCachedObject("masterLanguageCache", languageKey);
		if(languageVO != null)
		{
		    logger.info("There was an cached master language:" + languageVO.getName());
		}
		else
		{
			SiteNode siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
			Integer repositoryId = siteNode.getRepository().getRepositoryId();
         	
			OQLQuery oql = db.getOQLQuery( "SELECT l FROM org.infoglue.cms.entities.management.impl.simple.LanguageImpl l WHERE l.repositoryLanguages.repository.repositoryId = $1 ORDER BY l.repositoryLanguages.sortOrder, l.languageId");
			oql.bind(repositoryId);
			
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			while (results.hasMore()) 
            {
				Language language = (Language)results.next();
				LanguageVO languageVOCandidate = language.getValueObject();
				if(getIsValidLanguage(db, ndc, siteNode, languageVOCandidate.getId()))
				{
					languageVO = languageVOCandidate;		
					break;
				}
            }
			
			results.close();
			oql.close();

			if(languageVO != null)
				CacheController.cacheObject("masterLanguageCache", languageKey, languageVO);
		}
		
        return languageVO;	
	}


	/**
	 * This method returns language with the languageCode sent in. 
	 */
	
	public Locale getLocaleWithId(Database db, Integer languageId)
	{
		String key = "" + languageId;
		logger.info("key:" + key);
		Locale locale = (Locale)CacheController.getCachedObject("localeCache", key);
		if(locale != null)
		{
			logger.info("There was an cached locale:" + locale);
		}
		else
		{
			locale = Locale.getDefault();
			
			if (languageId != null)
			{
				try 
				{
					LanguageVO languageVO = getLanguageVO(db, languageId);
					locale = new Locale(languageVO.getLanguageCode());
				} 
				catch (Exception e) 
				{
					logger.error("An error occurred in getLocaleWithId: getting locale with languageid:" + languageId + "," + e, e);
				}	
			}
			
			CacheController.cacheObject("localeCache", key, locale);				
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


	/**
	 * This method returns language with the languageCode sent in. 
	 */
	
	public LanguageVO getLanguageWithCode(Database db, String languageCode) throws SystemException, Exception
	{ 
		String key = "" + languageCode;
		logger.info("key:" + key);
		LanguageVO languageVO = (LanguageVO)CacheController.getCachedObject("languageCache", key);
		if(languageVO != null)
		{
			logger.info("There was an cached languageVO:" + languageVO);
		}
		else
		{
			Language language = null;
	
			OQLQuery oql = db.getOQLQuery( "SELECT l FROM org.infoglue.cms.entities.management.impl.simple.LanguageImpl l WHERE l.languageCode = $1");
			oql.bind(languageCode);
			
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
            {
            	language = (Language)results.next();
				languageVO = language.getValueObject();
	        }
            
			results.close();
			oql.close();

			CacheController.cacheObject("languageCache", key, languageVO);
		}
		
        return languageVO;	
	}


	/**
	 * This method returns language with the languageCode sent in if it is allowed/supported in the current repository. 
	 */
	
	public LanguageVO getLanguageIfRepositorySupportsIt(Database db, String languageCodes, Integer siteNodeId) throws SystemException, Exception
	{
		if (languageCodes == null) return null;
		int index = Integer.MAX_VALUE;
		int currentIndex = 0;
		logger.info("Coming in with languageCodes:" + languageCodes);
		
        Language language = null;

    	SiteNode siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
		Repository repository = siteNode.getRepository();
		if(repository != null)
		{
			Collection languages = repository.getRepositoryLanguages();
			Iterator languageIterator = languages.iterator();
			while(languageIterator.hasNext())
			{
				RepositoryLanguage repositoryLanguage = (RepositoryLanguage)languageIterator.next();
				Language currentLanguage = repositoryLanguage.getLanguage();
				logger.info("CurrentLanguageCode:" + currentLanguage.getLanguageCode());
				currentIndex = languageCodes.toLowerCase().indexOf(currentLanguage.getLanguageCode().toLowerCase());
				if( currentIndex > -1 && currentIndex < index)
				{
					index = currentIndex;
					logger.info("Found the language in the list of supported languages for this site: " + currentLanguage.getName() + " - priority:" + index);
					language = currentLanguage;
					if (index==0) break; // Continue and try to find a better candidate unless index is 0 (first prio)
				}
			}
		}

		return (language == null) ? null : language.getValueObject();	
	}

	/**
	 * This method returns language with the languageCode sent in if it is allowed/supported in the current repository. 
	 */
	
	public LanguageVO getLanguageIfSiteNodeSupportsIt(Database db, String languageCodes, Integer siteNodeId, InfoGluePrincipal principal) throws SystemException, Exception
	{
	    if (languageCodes == null) 
	    	return null;
		
		String key = "" + siteNodeId + "_" + languageCodes;		
		Object languageVOCandidate = CacheController.getCachedObjectFromAdvancedCache("siteNodeLanguageCache", key);
		//Object languageVOCandidate = CacheController.getCachedObject("siteNodeLanguageCache", key);
		if(languageVOCandidate != null)
		{
			if(languageVOCandidate instanceof NullObject)
				return null;
			else
				return (LanguageVO)languageVOCandidate;
		}
		
	    int index = Integer.MAX_VALUE;
		int currentIndex = 0;
		logger.info("Coming in with languageCodes:" + languageCodes);
		
        LanguageVO language = null;

    	SiteNode siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
		
    	List repositoryLanguages = getAvailableLanguagesForRepository(db, siteNode.getValueObject().getRepositoryId());

		Iterator languageIterator = repositoryLanguages.iterator();
		while(languageIterator.hasNext())
		{
			LanguageVO currentLanguage = (LanguageVO)languageIterator.next();
			logger.info("CurrentLanguageCode:" + currentLanguage.getLanguageCode());
			
			NodeDeliveryController ndc = NodeDeliveryController.getNodeDeliveryController(siteNodeId, currentLanguage.getId(), new Integer(-1));
			
			currentIndex = languageCodes.toLowerCase().indexOf(currentLanguage.getLanguageCode().toLowerCase());
			if(getIsValidLanguage(db, ndc, siteNode, currentLanguage.getId()) && currentIndex > -1 && currentIndex < index)
			{
				index = currentIndex;
				logger.info("Found the language in the list of supported languages for this site: " + currentLanguage.getName() + " - priority:" + index);

				DeliveryContext deliveryContext = DeliveryContext.getDeliveryContext();
		    	ContentVO contentVO = ndc.getBoundContent(db, principal, siteNodeId, currentLanguage.getId(), false, BasicTemplateController.META_INFO_BINDING_NAME, deliveryContext);		
				if(contentVO != null)
				{
			    	ContentVersionVO contentVersionVO = ContentDeliveryController.getContentDeliveryController().getContentVersionVO(db, siteNodeId, contentVO.getId(), currentLanguage.getId(), false, deliveryContext, principal);
			    	if(contentVersionVO != null)
			    	{
						language = currentLanguage;
						logger.info("Language now: " + language.getName());
			    	}
			    }
				
				if (index==0) break; // Continue and try to find a better candidate unless index is 0 (first prio)
			}
		}

    	StringBuilder groupKey1 = new StringBuilder("repository_").append(siteNode.getValueObject().getRepositoryId());
    	StringBuilder groupKey2 = new StringBuilder("siteNode_").append(siteNodeId);
    	
		if(language != null)
			CacheController.cacheObjectInAdvancedCache("siteNodeLanguageCache", key, language, new String[]{groupKey1.toString(), groupKey2.toString()}, true);
		else
			CacheController.cacheObjectInAdvancedCache("siteNodeLanguageCache", key, new NullObject(), new String[]{groupKey1.toString(), groupKey2.toString()}, true);
		
		logger.info("Returning language: " + language);
		
		return language;	

    	/*
    	Repository repository = siteNode.getRepository();
		if(repository != null)
		{
			Collection languages = repository.getRepositoryLanguages();
			Iterator languageIterator = languages.iterator();
			while(languageIterator.hasNext())
			{
				RepositoryLanguage repositoryLanguage = (RepositoryLanguage)languageIterator.next();
				Language currentLanguage = repositoryLanguage.getLanguage();
				logger.info("CurrentLanguageCode:" + currentLanguage.getLanguageCode());
				
				NodeDeliveryController ndc = NodeDeliveryController.getNodeDeliveryController(siteNodeId, currentLanguage.getId(), new Integer(-1));
				
				currentIndex = languageCodes.toLowerCase().indexOf(currentLanguage.getLanguageCode().toLowerCase());
				if(getIsValidLanguage(db, ndc, siteNode, currentLanguage.getId()) && currentIndex > -1 && currentIndex < index)
				{
					index = currentIndex;
					logger.info("Found the language in the list of supported languages for this site: " + currentLanguage.getName() + " - priority:" + index);

					DeliveryContext deliveryContext = DeliveryContext.getDeliveryContext();
			    	ContentVO contentVO = ndc.getBoundContent(db, principal, siteNodeId, currentLanguage.getId(), false, BasicTemplateController.META_INFO_BINDING_NAME, deliveryContext);		
					if(contentVO != null)
					{
				    	ContentVersionVO contentVersionVO = ContentDeliveryController.getContentDeliveryController().getContentVersionVO(db, siteNodeId, contentVO.getId(), currentLanguage.getId(), false, deliveryContext, principal);
				    	if(contentVersionVO != null)
				    	{
							language = currentLanguage;
							logger.info("Language now: " + language.getName());
				    	}
				    }
					
					if (index==0) break; // Continue and try to find a better candidate unless index is 0 (first prio)
				}
			}
		}

    	StringBuilder groupKey1 = new StringBuilder("repository_").append(repository.getId());
    	StringBuilder groupKey2 = new StringBuilder("siteNode_").append(siteNodeId);
    	
		if(language != null)
			CacheController.cacheObjectInAdvancedCache("siteNodeLanguageCache", key, language.getValueObject(), new String[]{groupKey1.toString(), groupKey2.toString()}, true);
		else
			CacheController.cacheObjectInAdvancedCache("siteNodeLanguageCache", key, new NullObject(), new String[]{groupKey1.toString(), groupKey2.toString()}, true);
		
		logger.info("Returning language: " + language);
		
		return (language == null) ? null : language.getValueObject();	
		*/
	}

 
	/**
	 * This method returns language with the languageCode sent in if it is allowed/supported in the current repository. 
	 */
	
	public LanguageVO getLanguageIfSiteNodeSupportsIt(Database db, Integer languageId, Integer siteNodeId) throws SystemException, Exception
	{
		if (languageId == null) 
		    return null;

		String key = "" + siteNodeId + "_" + languageId;		
		Object languageVOCandidate = CacheController.getCachedObjectFromAdvancedCache("siteNodeLanguageCache", key);
		//Object languageVOCandidate = CacheController.getCachedObject("siteNodeLanguageCache", key);
		if(languageVOCandidate != null)
		{
			if(languageVOCandidate instanceof NullObject)
				return null;
			else
				return (LanguageVO)languageVOCandidate;
		}
		
		NodeDeliveryController ndc = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, new Integer(-1));

		if(logger.isInfoEnabled())
			logger.info("Coming in with languageId:" + languageId);
		
        LanguageVO language = null;

    	SiteNode siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db);

		if(!getIsValidLanguage(db, ndc, siteNode, languageId))
		    return null;		
    	
		List repositoryLanguages = getAvailableLanguagesForRepository(db, siteNode.getValueObject().getRepositoryId());

		Iterator languageIterator = repositoryLanguages.iterator();
		logger.info("languages on :" + siteNode.getId() + ":" + siteNode.getValueObject().getRepositoryId() + "=" + repositoryLanguages.size());
		while(languageIterator.hasNext())
		{
			LanguageVO currentLanguage = (LanguageVO)languageIterator.next();
			logger.info("CurrentLanguage:" + currentLanguage.getId());
			if(currentLanguage.getId().intValue() == languageId.intValue())
			{
			    logger.info("Found the language in the list of supported languages for this site: " + currentLanguage.getName());
				if(getIsValidLanguage(db, ndc, siteNode, currentLanguage.getId()))
				{
				    language = currentLanguage;
				    break;
				}
			}
		}
		
    	StringBuilder groupKey1 = new StringBuilder("repository_").append(siteNode.getValueObject().getRepositoryId());
    	StringBuilder groupKey2 = new StringBuilder("siteNode_").append(siteNodeId);

		if(language != null)
			CacheController.cacheObjectInAdvancedCache("siteNodeLanguageCache", key, language, new String[]{groupKey1.toString(), groupKey2.toString()}, true);
		else
			CacheController.cacheObjectInAdvancedCache("siteNodeLanguageCache", key, new NullObject(), new String[]{groupKey1.toString(), groupKey2.toString()}, true);

		logger.info("Returning language: " + language);

		return language;	

		/*
    	Repository repository = siteNode.getRepository();
    	if(repository != null)
		{
			Collection languages = repository.getRepositoryLanguages();
	    	
			Iterator languageIterator = languages.iterator();
			logger.info("languages on :" + siteNode.getId() + ":" + repository.getId() + "=" + languages.size());
			while(languageIterator.hasNext())
			{
				RepositoryLanguage repositoryLanguage = (RepositoryLanguage)languageIterator.next();
				Language currentLanguage = repositoryLanguage.getLanguage();
				logger.info("CurrentLanguage:" + currentLanguage.getId());
				if(currentLanguage.getId().intValue() == languageId.intValue())
				{
				    logger.info("Found the language in the list of supported languages for this site: " + currentLanguage.getName());
					if(getIsValidLanguage(db, ndc, siteNode, currentLanguage.getId()))
					{
					    language = currentLanguage;
					    break;
					}
				}
			}
		}
    	StringBuilder groupKey1 = new StringBuilder("repository_").append(repository.getId());
    	StringBuilder groupKey2 = new StringBuilder("siteNode_").append(siteNodeId);

		if(language != null)
			CacheController.cacheObjectInAdvancedCache("siteNodeLanguageCache", key, language.getValueObject(), new String[]{groupKey1.toString(), groupKey2.toString()}, true);
		else
			CacheController.cacheObjectInAdvancedCache("siteNodeLanguageCache", key, new NullObject(), new String[]{groupKey1.toString(), groupKey2.toString()}, true);

		logger.info("Returning language: " + language);

		return (language == null) ? null : language.getValueObject();	

		*/
		
    	
	}

	/**
	 * This method returns all languages available for a site node. 
	 */
	
	public List getLanguagesForSiteNode(Database db, Integer siteNodeId, InfoGluePrincipal principal) throws SystemException, Exception
	{
		String key = "" + siteNodeId;		
		List languageVOList = (List)CacheController.getCachedObjectFromAdvancedCache("siteNodeLanguageCache", key);
		//List languageVOList = (List)CacheController.getCachedObject("siteNodeLanguageCache", key);
		if(languageVOList != null)
			return languageVOList;
		
		logger.info("Looking for languages on page with id:" + siteNodeId);
		
        languageVOList = new ArrayList();

    	SiteNode siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
		//SiteNodeVO siteNodeVO = SiteNodeController.getController().getSmallSiteNodeVOWithId(siteNodeId, db);
        
		List repositoryLanguages = getAvailableLanguagesForRepository(db, siteNode.getValueObject().getRepositoryId());
		
		Iterator languageIterator = repositoryLanguages.iterator();
		while(languageIterator.hasNext())
		{
			LanguageVO currentLanguage = (LanguageVO)languageIterator.next();
			logger.info("CurrentLanguageCode:" + currentLanguage.getLanguageCode());
			
			NodeDeliveryController ndc = NodeDeliveryController.getNodeDeliveryController(siteNodeId, currentLanguage.getId(), new Integer(-1));
			if(getIsValidLanguage(db, ndc, siteNode, currentLanguage.getId()))
			{
				logger.info("Found the language in the list of supported languages for this site: " + currentLanguage.getName());
				languageVOList.add(currentLanguage);
			}
		}

		if(languageVOList != null)
		{
	    	StringBuilder groupKey1 = new StringBuilder("repository_").append(siteNode.getValueObject().getRepositoryId());
	    	StringBuilder groupKey2 = new StringBuilder("siteNode_").append(siteNodeId);

			CacheController.cacheObjectInAdvancedCache("siteNodeLanguageCache", key, languageVOList, new String[]{groupKey1.toString(), groupKey2.toString()}, true);
			//CacheController.cacheObject("siteNodeLanguageCache", key, languageVOList);
		}

		/*
    	Repository repository = siteNode.getRepository();
		if(repository != null)
		{
			Collection languages = repository.getRepositoryLanguages();
			Iterator languageIterator = languages.iterator();
			while(languageIterator.hasNext())
			{
				RepositoryLanguage repositoryLanguage = (RepositoryLanguage)languageIterator.next();
				Language currentLanguage = repositoryLanguage.getLanguage();
				logger.info("CurrentLanguageCode:" + currentLanguage.getLanguageCode());
				
				NodeDeliveryController ndc = NodeDeliveryController.getNodeDeliveryController(siteNodeId, currentLanguage.getId(), new Integer(-1));
				
				if(getIsValidLanguage(db, ndc, siteNode, currentLanguage.getId()))
				{
					logger.info("Found the language in the list of supported languages for this site: " + currentLanguage.getName());
					languageVOList.add(currentLanguage.getValueObject());
				}
			}
		}
		if(languageVOList != null)
		{
	    	StringBuilder groupKey1 = new StringBuilder("repository_").append(repository.getId());
	    	StringBuilder groupKey2 = new StringBuilder("siteNode_").append(siteNodeId);

			CacheController.cacheObjectInAdvancedCache("siteNodeLanguageCache", key, languageVOList, new String[]{groupKey1.toString(), groupKey2.toString()}, true);
			//CacheController.cacheObject("siteNodeLanguageCache", key, languageVOList);
		}
		*/
		
		
		logger.info("Returning languageVOList: " + languageVOList.size());
		
		return languageVOList;	
	}

	
	public boolean getIsValidLanguage(Database db, NodeDeliveryController ndc, /*Integer siteNodeId, */SiteNode siteNode, Integer languageId) throws Exception
	{
	    boolean isValidLanguage = true;
	    									
    	Integer siteNodeId = siteNode.getId();
	    Integer disabledLanguagesSiteNodeVersionId = ndc.getDisabledLanguagesSiteNodeVersionId(db, siteNodeId);
	    logger.info("disabledLanguagesSiteNodeVersionId:" + disabledLanguagesSiteNodeVersionId);

	    if(disabledLanguagesSiteNodeVersionId != null)
	    {
	        SiteNodeVersionVO disabledLanguagesSiteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(disabledLanguagesSiteNodeVersionId, db);
	        
	        String disabledLanguagesString = CmsPropertyHandler.getPropertySetValue("siteNode_" + disabledLanguagesSiteNodeVersionVO.getSiteNodeId() + "_disabledLanguages");
		    logger.info("disabledLanguagesString:" + disabledLanguagesString);
		    
		    if(disabledLanguagesString != null && !disabledLanguagesString.equalsIgnoreCase(""))
		    {
		        String[] disabledLanguagesStringArray = disabledLanguagesString.split(",");
		        for(int i=0; i<disabledLanguagesStringArray.length; i++)
		        {
		            logger.info("languageId.intValue():" + languageId.intValue());
		            logger.info("disabledLanguagesStringArray:" + disabledLanguagesStringArray);
				    if(languageId.intValue() == new Integer(disabledLanguagesStringArray[i]).intValue())
		            {
		                isValidLanguage = false;
			            logger.info("isValidLanguage:" + isValidLanguage);
		                break;
		            }
		        }
		    }

	        String enabledLanguagesString = CmsPropertyHandler.getPropertySetValue("siteNode_" + disabledLanguagesSiteNodeVersionVO.getSiteNodeId() + "_enabledLanguages");
		    
		    if(enabledLanguagesString != null && !enabledLanguagesString.equalsIgnoreCase(""))
		    {
		    	isValidLanguage = false;
		    	
		        String[] enabledLanguagesStringArray = enabledLanguagesString.split(",");
		        for(int i=0; i<enabledLanguagesStringArray.length; i++)
		        {
		          if(languageId.intValue() == new Integer(enabledLanguagesStringArray[i]).intValue())
		            {
		                isValidLanguage = true;
			            break;
		            }
		        }
		    }

		}
	    logger.info("languageId:" + languageId + " was valid:" + isValidLanguage);
		
		return isValidLanguage;
	}

	
	
}