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

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.databeans.OptimizationBeanList;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.content.SmallestContentVersionVO;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinition;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RegistryVO;
import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.DateHelper;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author Mattias Bogeblad
 *
 */

public class ContentVersionController extends BaseController 
{
    private final static Logger logger = Logger.getLogger(ContentVersionController.class.getName());

	private static final ContentCategoryController contentCategoryController = ContentCategoryController.getController();
	private static final ContentVersionController contentVersionController = new ContentVersionController();
	private static final RegistryController registryController = RegistryController.getController();
	
	private static Map<Integer, Integer> contentMap = new ConcurrentHashMap<Integer, Integer>();
	
	/**
	 * Factory method to get object
	 */
	
	public static ContentVersionController getContentVersionController()
	{
		return contentVersionController;
	}
	
	

    public Integer getContentIdForContentVersion(Integer contentVersionId) throws SystemException, Bug
    {
    	Integer contentId = (Integer)contentMap.get(contentVersionId);
    	if(contentId == null)
    	{
    		ContentVersionVO ContentVersionVO = getContentVersionVOWithId(contentVersionId);
    		//ContentVersionVO ContentVersionVO = (ContentVersionVO) getVOWithId(ContentVersionImpl.class, contentVersionId);
    		contentId = ContentVersionVO.getContentId();
    		contentMap.put(contentVersionId, contentId);
    	}
    	
    	return contentId;
    }

    public Integer getContentIdForContentVersion(Integer contentVersionId, Database db) throws SystemException, Bug
    {
    	Integer contentId = (Integer)contentMap.get(contentVersionId);
    	if(contentId == null)
    	{
    		ContentVersionVO ContentVersionVO = getContentVersionVOWithId(contentVersionId, db);
    		//ContentVersionVO ContentVersionVO = (ContentVersionVO) getVOWithId(ContentVersionImpl.class, contentVersionId, db);
    		contentId = ContentVersionVO.getContentId();
    		contentMap.put(contentVersionId, contentId);
    	}
    	
    	return contentId;
    }

    public ContentVersionVO getFullContentVersionVOWithId(Integer contentVersionId) throws SystemException, Bug
    {
		return (ContentVersionVO) getVOWithId(ContentVersionImpl.class, contentVersionId);
    }

    public ContentVersionVO getContentVersionVOWithId(Integer contentVersionId) throws SystemException, Bug
    {
		return (ContentVersionVO) getVOWithId(SmallContentVersionImpl.class, contentVersionId);
    }

    public ContentVersionVO getContentVersionVOWithId(Integer contentVersionId, Database db) throws SystemException, Bug
    {
		return (ContentVersionVO) getVOWithId(SmallContentVersionImpl.class, contentVersionId, db);
    }

    public ContentVersion getContentVersionWithId(Integer contentVersionId, Database db) throws SystemException, Bug
    {
		return (ContentVersion) getObjectWithId(ContentVersionImpl.class, contentVersionId, db);
    }

    public ContentVersion getReadOnlyContentVersionWithId(Integer contentVersionId, Database db) throws SystemException, Bug
    {
		return (ContentVersion) getObjectWithIdAsReadOnly(ContentVersionImpl.class, contentVersionId, db);
    }

    public SmallestContentVersionVO getSmallestContentVersionVOWithId(Integer contentVersionId, Database db) throws SystemException, Bug
    {
		return (SmallestContentVersionVO) getVOWithId(SmallestContentVersionImpl.class, contentVersionId, db);
    }

    public List getContentVersionVOList() throws SystemException, Bug
    {
        return getAllVOObjects(SmallContentVersionImpl.class, "contentVersionId");
    }

	/**
	 * Recursive methods to get all contentVersions of a given state under the specified parent content.
	 */ 
	
    public List getContentVersionVOWithParentRecursive(Integer contentId, Integer stateId, boolean mustBeFirst) throws ConstraintException, SystemException
	{
		return getContentVersionVOWithParentRecursive(contentId, stateId, new ArrayList(), mustBeFirst);
	}
	
	private List getContentVersionVOWithParentRecursive(Integer contentId, Integer stateId, List resultList, boolean mustBeFirst) throws ConstraintException, SystemException
	{
		// Get the versions of this content.
		resultList.addAll(getLatestContentVersionVOWithParent(contentId, stateId, mustBeFirst));
		// Get the children of this content and do the recursion
		List childContentList = ContentController.getContentController().getContentChildrenVOList(contentId, null, false);
		Iterator cit = childContentList.iterator();
		while (cit.hasNext())
		{
			ContentVO contentVO = (ContentVO) cit.next();
			getContentVersionVOWithParentRecursive(contentVO.getId(), stateId, resultList, mustBeFirst);
		}
	
		return resultList;
	}

	
	/**
	 * Recursive methods to get all contentVersions of a given state under the specified parent content.
	 */ 
	
    public List getContentVersionVOWithParentRecursiveAndRelated(Integer contentId, Integer stateId, boolean mustBeFirst) throws ConstraintException, SystemException
	{
        List contentVersionVOList = new ArrayList();
        
        Database db = CastorDatabaseService.getDatabase();

	    beginTransaction(db);

        try
        {
            List contentVersionList = getContentVersionWithParentRecursiveAndRelated(contentId, stateId, new ArrayList(), new ArrayList(), db, mustBeFirst);
            contentVersionVOList = toVOList(contentVersionList);
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
        return contentVersionVOList;
	}
	
	private List getContentVersionWithParentRecursiveAndRelated(Integer contentId, Integer stateId, List resultList, List checkedContents, Database db, boolean mustBeFirst) throws ConstraintException, SystemException, Exception
	{
        checkedContents.add(contentId);
        
		// Get the versions of this content.
		List contentVersions = getLatestContentVersionWithParent(contentId, stateId, db, mustBeFirst);
		resultList.addAll(contentVersions);
	    
		Iterator contentVersionsIterator = contentVersions.iterator();
	    while(contentVersionsIterator.hasNext())
	    {
	        ContentVersion contentVersion = (ContentVersion)contentVersionsIterator.next();
	        List relatedEntities = RegistryController.getController().getMatchingRegistryVOListForReferencingEntity(ContentVersion.class.getName(), contentVersion.getId().toString(), db);
	        Iterator relatedEntitiesIterator = relatedEntities.iterator();
	        
	        while(relatedEntitiesIterator.hasNext())
	        {
	            RegistryVO registryVO = (RegistryVO)relatedEntitiesIterator.next();
	            logger.info("registryVO:" + registryVO.getEntityName() + ":" + registryVO.getEntityId());
	            if(registryVO.getEntityName().equals(Content.class.getName()) && !checkedContents.contains(new Integer(registryVO.getEntityId())))
	            {
	                List relatedContentVersions = getLatestContentVersionWithParent(new Integer(registryVO.getEntityId()), stateId, db, mustBeFirst);
	    		    resultList.addAll(relatedContentVersions);
	    		    checkedContents.add(new Integer(registryVO.getEntityId()));
	            }
	        }
	    }
	    
		
		// Get the children of this content and do the recursion
		List childContentList = ContentController.getContentController().getContentChildrenVOList(contentId, null, false);
		Iterator cit = childContentList.iterator();
		while (cit.hasNext())
		{
			ContentVO contentVO = (ContentVO) cit.next();
			getContentVersionWithParentRecursiveAndRelated(contentVO.getId(), stateId, resultList, checkedContents, db, mustBeFirst);
		}
        
		return resultList;
	}

	
	public List getContentVersionVOWithParent(Integer contentId) throws SystemException, Bug
    {
        List resultList = new ArrayList();
    	Database db = CastorDatabaseService.getDatabase();
    	ContentVersionVO contentVersionVO = null;

        beginTransaction(db);

        try
        {
            List contentVersions = getContentVersionWithParent(contentId, db);
            resultList = toVOList(contentVersions);
            /*
            OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 ORDER BY cv.contentVersionId desc");
        	oql.bind(contentId);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			while (results.hasMore()) 
            {
            	ContentVersion contentVersion = (ContentVersion)results.next();
            	logger.info("found one:" + contentVersion.getValueObject());
            	contentVersionVO = contentVersion.getValueObject();
            	resultList.add(contentVersionVO);
            }
            */
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    	
		return resultList;
    }

	public List getContentVersionWithParent(Integer contentId, Database db) throws SystemException, Bug, Exception
    {
        ArrayList resultList = new ArrayList();
    	ContentVersionVO contentVersionVO = null;

        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 ORDER BY cv.contentVersionId desc");
    	oql.bind(contentId);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		while (results.hasMore()) 
        {
        	ContentVersion contentVersion = (ContentVersion)results.next();
        	resultList.add(contentVersion);
        }
    	
		results.close();
		oql.close();

		return resultList;
    }
	
	/**
	 * This method returns a list of active contentversions, and only one / language in the specified state
	 * 
	 * @param contentId The content to look for versions in
	 * @param stateId  The state of the versions
	 * @return A list of the latest versions matching the given state
	 * @throws SystemException
	 * @throws Bug
	 */

	public List getLatestContentVersionVOWithParent(Integer contentId, Integer stateId, boolean mustBeFirst) throws SystemException, Bug
	{
		List resultList = new ArrayList();
		
		Database db = CastorDatabaseService.getDatabase();
		
		beginTransaction(db);

		try
		{
		    resultList = getLatestContentVersionWithParent(contentId, stateId, db, mustBeFirst);
		    resultList = toVOList(resultList);
		    
			commitTransaction(db);
		}
		catch(Exception e)
		{
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
    	
		return resultList;
	}	
	
	/**
	 * This method returns a list of active contentversions, and only one / language in the specified state
	 * 
	 * @param contentId The content to look for versions in
	 * @param stateId  The state of the versions
	 * @return A list of the latest versions matching the given state
	 * @throws SystemException
	 * @throws Bug
	 */

	public List getLatestContentVersionWithParent(Integer contentId, Integer stateId, Database db, boolean mustBeFirst) throws SystemException, Bug, Exception
	{
		ArrayList resultList = new ArrayList();
		ArrayList langCheck = new ArrayList();
		
		OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 ORDER BY cv.contentVersionId desc");
		oql.bind(contentId);
		// oql.bind(stateId);
    	
		QueryResults results = oql.execute(Database.ReadOnly);
		
		// New improved
		while (results.hasMore()) 
		{
			ContentVersion contentVersion = (ContentVersion)results.next();
			logger.info("contentVersion found: " + contentVersion.getId());

			if(contentVersion.getIsActive().booleanValue())
			{
			    if((contentVersion.getStateId().compareTo(stateId)==0) && (!langCheck.contains(contentVersion.getLanguage().getLanguageId())))
				{
					resultList.add(contentVersion);
					logger.info("contentVersion added: " + contentVersion.getId());
					langCheck.add(contentVersion.getLanguage().getLanguageId());
				}
				
			    if(mustBeFirst)
					langCheck.add(contentVersion.getLanguage().getLanguageId());
			}
		}
    	
		results.close();
		oql.close();

		logger.info("getLatestContentVersionWithParent done...");
		
		return resultList;
	}
   
	
	/**
	 * This method returns the latest contentVersion there is for the given content if it is active and is the latest made.
	 */
	
	public List getLatestActiveContentVersionIfInState(Content content, Integer stateId, Database db) throws SystemException, Exception
	{
		List resultList = new ArrayList();
	    Map lastLanguageVersions = new HashMap();
	    Map languageVersions = new HashMap();
	    
		Collection contentVersions = content.getContentVersions();
		
		Iterator versionIterator = contentVersions.iterator();
		while(versionIterator.hasNext())
		{
		    ContentVersion contentVersionCandidate = (ContentVersion)versionIterator.next();	
			
		    ContentVersion lastVersionInThatLanguage = (ContentVersion)lastLanguageVersions.get(contentVersionCandidate.getLanguage().getId());
			if(lastVersionInThatLanguage == null || (lastVersionInThatLanguage.getId().intValue() < contentVersionCandidate.getId().intValue() && contentVersionCandidate.getIsActive().booleanValue()))
			    lastLanguageVersions.put(contentVersionCandidate.getLanguage().getId(), contentVersionCandidate);
			
			if(contentVersionCandidate.getIsActive().booleanValue() && contentVersionCandidate.getStateId().intValue() == stateId.intValue())
			{
				if(contentVersionCandidate.getOwningContent().getContentId().intValue() == content.getId().intValue())
				{
				    ContentVersion versionInThatLanguage = (ContentVersion)languageVersions.get(contentVersionCandidate.getLanguage().getId());
					if(versionInThatLanguage == null || versionInThatLanguage.getContentVersionId().intValue() < contentVersionCandidate.getId().intValue())
					{
					    languageVersions.put(contentVersionCandidate.getLanguage().getId(), contentVersionCandidate);
					}
				}
			}
		}

		logger.info("Found languageVersions:" + languageVersions.size());
		logger.info("Found lastLanguageVersions:" + lastLanguageVersions.size());
		Iterator i = languageVersions.values().iterator();
		while(i.hasNext())
		{
		    ContentVersion contentVersion = (ContentVersion)i.next();
		    ContentVersion lastVersionInThatLanguage = (ContentVersion)lastLanguageVersions.get(contentVersion.getLanguage().getId());

		    logger.info("contentVersion:" + contentVersion.getId());
		    logger.info("lastVersionInThatLanguage:" + lastVersionInThatLanguage.getId());

		    if(contentVersion == lastVersionInThatLanguage)
			    resultList.add(contentVersion);
		}
		
		return resultList;
	}
	
    
    /**
     * This method returns the latest active content version.
     */
    
   	public ContentVersionVO getLatestActiveContentVersionVO(Integer contentId, Integer languageId) throws SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();
    	ContentVersionVO contentVersionVO = null;

        beginTransaction(db);

        try
        {
        	contentVersionVO = getLatestActiveContentVersionVO(contentId, languageId, db);
            
            rollbackTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    	
		return contentVersionVO;
    }

   	/**
	 * This method returns the latest active content version.
	 */
    
	public ContentVersionVO getLatestActiveContentVersionVO(Integer contentId, Integer languageId, Integer stateId, Database db) throws SystemException, Bug, Exception
	{
    	ContentVersionVO contentVersionVO = null;

       	ContentVersion contentVersion = getLatestActiveContentVersionReadOnly(contentId, languageId, stateId, db);
            
        if(contentVersion != null)
            contentVersionVO = contentVersion.getValueObject();
    	
		return contentVersionVO;
    }

	
   	/**
	 * This method returns the latest active content version.
	 */
    
	public ContentVersionVO getLatestActiveContentVersionVO(Integer contentId, Integer languageId, Database db) throws SystemException, Bug, Exception
	{
		ContentVersionVO contentVersionVO = null;

        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl cv WHERE cv.contentId = $1 AND cv.languageId = $2 AND cv.isActive = $3 ORDER BY cv.contentVersionId desc");
    	oql.bind(contentId);
    	oql.bind(languageId);
		oql.bind(true);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		if (results.hasMore()) 
        {
			ContentVersion contentVersion = (ContentVersion)results.next();
			contentVersionVO = contentVersion.getValueObject();
        }
		
		results.close();
		oql.close();

		return contentVersionVO;
	}

   	/**
	 * This method returns the latest active content version.
	 */
    
	public ContentVersion getLatestActiveContentVersion(Integer contentId, Integer languageId, Database db) throws SystemException, Bug, Exception
	{
		ContentVersion contentVersion = null;
    	
		/*
	    OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.language.languageId = $2 AND cv.isActive = $3 ORDER BY cv.contentVersionId desc");
		oql.bind(contentId);
		oql.bind(languageId);
		oql.bind(true);

		QueryResults results = oql.execute(Database.ReadOnly);
		
		if (results.hasMore()) 
	    {
	    	contentVersion = (ContentVersion)results.next();
	    	logger.info("found one:" + contentVersion.getId());
	    }
	    */
		
		Content content = ContentController.getContentController().getReadOnlyContentWithId(contentId, db);
		Collection contentVersions = content.getContentVersions();
		if(logger.isInfoEnabled())
    	{
	    	logger.info("contentId:" + contentId);
	    	logger.info("languageId:" + languageId);
	    	logger.info("content:" + content.getName());
			logger.info("contentVersions:" + contentVersions.size());
    	}
    	
		Iterator i = contentVersions.iterator();
        while(i.hasNext())
		{
			ContentVersion currentContentVersion = (ContentVersion)i.next();
			if(logger.isInfoEnabled())
				logger.info("found one candidate:" + currentContentVersion.getValueObject());
			if(contentVersion == null || (currentContentVersion.getId().intValue() > contentVersion.getId().intValue()))
			{
				if(logger.isInfoEnabled())
				{
					logger.info("currentContentVersion:" + currentContentVersion.getIsActive());
					logger.info("currentContentVersion:" + currentContentVersion.getLanguage().getId());
				}
				if(currentContentVersion.getIsActive().booleanValue() &&  currentContentVersion.getLanguage().getId().intValue() == languageId.intValue())
					contentVersion = currentContentVersion;
			}
		}

		return contentVersion;
	}

	
   	/**
	 * This method returns the latest active content version.
	 */
    
	public ContentVersion getLatestActiveContentVersionReadOnly(Integer contentId, Integer languageId, Database db) throws SystemException, Bug
	{
		ContentVersion contentVersion = null;
    	
		Content content = ContentController.getContentController().getReadOnlyMediumContentWithId(contentId, db);
		Collection contentVersions = content.getContentVersions();
		if(logger.isInfoEnabled())
		{
			logger.info("contentId:" + contentId);
	    	logger.info("languageId:" + languageId);
	    	logger.info("content:" + content.getName());
			logger.info("contentVersions:" + contentVersions.size());
		}
        
		Iterator i = contentVersions.iterator();
        while(i.hasNext())
		{
			ContentVersion currentContentVersion = (ContentVersion)i.next();
			if(logger.isInfoEnabled())
				logger.info("found one candidate:" + currentContentVersion.getValueObject());
			
			if(contentVersion == null || (currentContentVersion.getId().intValue() > contentVersion.getId().intValue()))
			{
				if(logger.isInfoEnabled())
				{
					logger.info("currentContentVersion:" + currentContentVersion.getIsActive());
					logger.info("currentContentVersion:" + currentContentVersion.getLanguage().getId());
				}
				if(currentContentVersion.getIsActive().booleanValue() &&  currentContentVersion.getLanguage().getId().intValue() == languageId.intValue())
					contentVersion = currentContentVersion;
			}
		}
        
		return contentVersion;
	}

   	/**
	 * This method returns the latest active content version in a certain version.
	 */
    
	public ContentVersion getLatestActiveContentVersionReadOnly(Integer contentId, Integer languageId, Integer stateId, Database db) throws SystemException, Bug
	{
		ContentVersion contentVersion = null;
    	
		Content content = ContentController.getContentController().getReadOnlyMediumContentWithId(contentId, db);
		Collection contentVersions = content.getContentVersions();
		if(logger.isInfoEnabled())
		{
			logger.info("contentId:" + contentId);
	    	logger.info("languageId:" + languageId);
	    	logger.info("content:" + content.getName());
			logger.info("contentVersions:" + contentVersions.size());
		}
        
		Iterator i = contentVersions.iterator();
        while(i.hasNext())
		{
			ContentVersion currentContentVersion = (ContentVersion)i.next();
			if(logger.isInfoEnabled())
				logger.info("found one candidate:" + currentContentVersion.getValueObject());
			
			if(contentVersion == null || (currentContentVersion.getId().intValue() > contentVersion.getId().intValue()))
			{
				if(logger.isInfoEnabled())
				{
					logger.info("currentContentVersion:" + currentContentVersion.getIsActive());
					logger.info("currentContentVersion:" + currentContentVersion.getLanguage().getId());
				}
				if(currentContentVersion.getIsActive().booleanValue() &&  currentContentVersion.getLanguage().getId().intValue() == languageId.intValue() && currentContentVersion.getStateId() >= stateId)
					contentVersion = currentContentVersion;
			}
		}
        
		return contentVersion;
	}

	public ContentVersionVO getLatestContentVersionVO(Integer contentId, Integer languageId) throws SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();
    	ContentVersionVO contentVersionVO = null;

        beginTransaction(db);

        try
        {
        	contentVersionVO = getLatestContentVersionVO(contentId, languageId, db);
        	
			commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
		return contentVersionVO;
    }

	public ContentVersionVO getLatestContentVersionVO(Integer contentId, Integer languageId, Database db) throws SystemException, Bug, Exception
    {
        ContentVersionVO contentVersionVO = null;
        
        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl cv WHERE cv.contentId = $1 AND cv.languageId = $2 ORDER BY cv.contentVersionId desc");
    	oql.bind(contentId);
    	oql.bind(languageId);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		if (results.hasMore()) 
        {
			ContentVersion contentVersion = (ContentVersion)results.next();
			contentVersionVO = contentVersion.getValueObject();
        }
		
		results.close();
		oql.close();
		
		return contentVersionVO;
    }


	public ContentVersion getContentVersionWithId(Integer contentVersionId) throws SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();
    	ContentVersion contentVersion = null;

        beginTransaction(db);

        try
        {
           	contentVersion = getContentVersionWithId(contentVersionId, db);
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    	
		return contentVersion;
    }


	public ContentVersion getLatestContentVersion(Integer contentId, Integer languageId, Database db) throws SystemException, Bug, Exception
    {
        ContentVersion contentVersion = null;
        
        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.language.languageId = $2 ORDER BY cv.contentVersionId desc");
    	oql.bind(contentId);
    	oql.bind(languageId);
    	
    	QueryResults results = oql.execute();
		
		if (results.hasMore()) 
        {
        	contentVersion = (ContentVersion)results.next();
        }
		
		results.close();
		oql.close();
		
		return contentVersion;
    }

   	
	/**
	 * This method created a new contentVersion in the database.
	 */
	
    public ContentVersionVO create(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, Integer oldContentVersionId) throws ConstraintException, SystemException
    {
    	return create(contentId, languageId, contentVersionVO, oldContentVersionId, true, true);
    }
    
    
    /**
	 * This method created a new contentVersion in the database.
	 */
	
    public ContentVersionVO create(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, Integer oldContentVersionId, boolean allowBrokenAssets, boolean duplicateAssets) throws ConstraintException, SystemException
    {
		Database db = CastorDatabaseService.getDatabase();
        ContentVersion contentVersion = null;

        beginTransaction(db);
		try
        {
			contentVersion = create(contentId, languageId, contentVersionVO, oldContentVersionId, allowBrokenAssets, duplicateAssets, db);
			commitTransaction(db);
		}
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
        return contentVersion.getValueObject();
    }     
    
	/**
	 * This method created a new contentVersion in the database. It also updates the owning content
	 * so it recognises the change. 
	 */
    
    public ContentVersion create(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, Integer oldContentVersionId, Database db) throws ConstraintException, SystemException, Exception
    {
    	return create(contentId, languageId, contentVersionVO, oldContentVersionId, true, true, db);
    }
	
    
	/**
	 * This method created a new contentVersion in the database. It also updates the owning content
	 * so it recognises the change. 
	 */
    public ContentVersion create(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, Integer oldContentVersionId, boolean allowBrokenAssets, boolean duplicateAssets, Database db) throws ConstraintException, SystemException, Exception
    {
    	return create(contentId, languageId, contentVersionVO, oldContentVersionId, allowBrokenAssets, duplicateAssets, null, db);
    }
    
    public ContentVersion create(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, Integer oldContentVersionId, boolean allowBrokenAssets, boolean duplicateAssets, Integer excludedAssetId, Database db) throws ConstraintException, SystemException, Exception
    {
		Content content   = ContentController.getContentController().getContentWithId(contentId, db);
    	Language language = LanguageController.getController().getLanguageWithId(languageId, db);

    	return create(content, language, contentVersionVO, oldContentVersionId, allowBrokenAssets, duplicateAssets, excludedAssetId, db);
    }     
    
	/**
	 * This method created a new contentVersion in the database. It also updates the owning content
	 * so it recognises the change. 
	 */
    
    /*
    public ContentVersion create(Content content, Language language, ContentVersionVO contentVersionVO, Integer oldContentVersionId, Database db) throws ConstraintException, SystemException, Exception
    {
    	return create(content, language, contentVersionVO, oldContentVersionId, true, db);
    }
    */
    public ContentVersion create(Content content, Language language, ContentVersionVO contentVersionVO, Integer oldContentVersionId, boolean allowBrokenAssets, boolean duplicateAssets, Database db) throws ConstraintException, SystemException, Exception
    {
    	return create(content, language, contentVersionVO, oldContentVersionId, allowBrokenAssets, duplicateAssets, null, db);
      
    }
    
    public ContentVersion create(Content content, Language language, ContentVersionVO contentVersionVO, Integer oldContentVersionId, boolean allowBrokenAssets, boolean duplicateAssets, Integer excludedAssetId, Database db) throws ConstraintException, SystemException, Exception
    {
    	ContentVersion contentVersion = new ContentVersionImpl();
		contentVersion.setValueObject(contentVersionVO);
        contentVersion.setLanguage((LanguageImpl)language);
		logger.info("Content:" + content.getContentId() + ":" + db.isPersistent(content));
		contentVersion.setOwningContent((ContentImpl)content);
		
		db.create(contentVersion); 

        content.getContentVersions().add(contentVersion);

        if(oldContentVersionId != null && oldContentVersionId.intValue() != -1)
		    copyDigitalAssets(getContentVersionWithId(oldContentVersionId, db), contentVersion, allowBrokenAssets, duplicateAssets, excludedAssetId, db);
		    //contentVersion.setDigitalAssets(getContentVersionWithId(oldContentVersionId, db).getDigitalAssets());

        return contentVersion;
    }     

	/**
	 * This method deletes an contentversion and notifies the owning content.
	 */
	
    public void delete(ContentVersionVO contentVersionVO) throws ConstraintException, SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {
			delete(contentVersionVO, db);
			commitTransaction(db);
		}
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }        
	
	/**
	 * This method deletes an contentversion and notifies the owning content.
	 */
	
    public void delete(ContentVersionVO contentVersionVO, Database db) throws ConstraintException, SystemException, Exception
    {
		ContentVersion contentVersion = getContentVersionWithId(contentVersionVO.getContentVersionId(), db);
		delete(contentVersion, db, false);
    }        

	/**
	 * This method deletes an contentversion and notifies the owning content.
	 */
	
 	public void delete(ContentVersion contentVersion, Database db) throws ConstraintException, SystemException, Exception
	{
 	    delete(contentVersion, db, false);
	}
	
	/**
	 * This method deletes an contentversion and notifies the owning content.
	 */
	
 	public void delete(ContentVersion contentVersion, Database db, boolean forceDelete) throws ConstraintException, SystemException, Exception
	{
		if (!forceDelete && contentVersion.getStateId().intValue() == ContentVersionVO.PUBLISHED_STATE.intValue() && contentVersion.getIsActive().booleanValue() == true)
		{
			throw new ConstraintException("ContentVersion.stateId", "3300", contentVersion.getOwningContent().getName());
		}
		
		contentCategoryController.deleteByContentVersion(contentVersion, db);
		DigitalAssetController.getController().deleteByContentVersion(contentVersion, db);
		
		Content content = contentVersion.getOwningContent();

		if(content != null)
		    content.getContentVersions().remove(contentVersion);

		db.remove(contentVersion);
	}



	/**
	 * This method deletes all contentVersions for the content sent in.
	 * The contentVersion is related to digital assets but we don't remove the asset itself in case 
	 * other versions or contents reference the same asset.
	 */
	
	public void deleteVersionsForContent(Content content, Database db, InfoGluePrincipal principal) throws ConstraintException, SystemException, Bug, Exception
    {
	    deleteVersionsForContent(content, db, false, principal);
    }
	
	/**
	 * This method deletes all contentVersions for the content sent in.
	 * The contentVersion is related to digital assets but we don't remove the asset itself in case 
	 * other versions or contents reference the same asset.
	 */
	
	public void deleteVersionsForContent(Content content, Database db, boolean forceDelete, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException, Bug, Exception
    {
    	//TEST
        if(forceDelete)
        {
	        List contentVersionsVOList = ContentVersionController.getContentVersionController().getPublishedActiveContentVersionVOList(content.getContentId(), db);
	        
	        List events = new ArrayList();
			Iterator it = contentVersionsVOList.iterator();
			while(it.hasNext())
			{
				ContentVersionVO contentVersionVO = (ContentVersionVO)it.next();
				
				EventVO eventVO = new EventVO();
				eventVO.setDescription("Unpublished before forced deletion");
				eventVO.setEntityClass(ContentVersion.class.getName());
				eventVO.setEntityId(contentVersionVO.getContentVersionId());
				eventVO.setName(contentVersionVO.getContentName() + "(" + contentVersionVO.getLanguageName() + ")");
				eventVO.setTypeId(EventVO.UNPUBLISH_LATEST);
				eventVO = EventController.create(eventVO, content.getRepositoryId(), infogluePrincipal);
				events.add(eventVO);
			}
		
		    PublicationVO publicationVO = new PublicationVO();
		    publicationVO.setName("Direct publication by " + infogluePrincipal.getName());
		    publicationVO.setDescription("Unpublished all versions before forced deletion");
		    //publicationVO.setPublisher(this.getInfoGluePrincipal().getName());
		    publicationVO.setRepositoryId(content.getRepositoryId());
		    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, true, infogluePrincipal, db);
        }
        //TEST

        Collection contentVersions = Collections.synchronizedCollection(content.getContentVersions());
       	Iterator contentVersionIterator = contentVersions.iterator();
			
		while (contentVersionIterator.hasNext()) 
        {
        	ContentVersion contentVersion = (ContentVersion)contentVersionIterator.next();
        	        
        	Collection digitalAssetList = contentVersion.getDigitalAssets();
			Iterator assets = digitalAssetList.iterator();
			while (assets.hasNext()) 
            {
            	DigitalAsset digitalAsset = (DigitalAsset)assets.next();
				assets.remove();
				db.remove(digitalAsset);
			}
			
        	logger.info("Deleting contentVersion:" + contentVersion.getContentVersionId());
        	contentVersionIterator.remove();
        	delete(contentVersion, db, forceDelete);
        }
        content.setContentVersions(new ArrayList());
    }

	/**
	 * This method deletes a digitalAsset.
	 */
	
    public void deleteDigitalAsset(Integer contentId, Integer languageId, String assetKey) throws ConstraintException, SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {
		    ContentVersion contentVersion = this.getLatestActiveContentVersion(contentId, languageId, db);
		    
		    Collection digitalAssets = contentVersion.getDigitalAssets();
			Iterator assetIterator = digitalAssets.iterator();
			while(assetIterator.hasNext())
			{
				DigitalAsset currentDigitalAsset = (DigitalAsset)assetIterator.next();
				if(currentDigitalAsset.getAssetKey().equals(assetKey))
				{
					assetIterator.remove();
					db.remove(currentDigitalAsset);
					break;
				}
			}
			
			commitTransaction(db);
		}
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }        
	
	
    /**
     * This method updates the contentversion.
     */
    
    public ContentVersionVO update(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO) throws ConstraintException, SystemException
    {
    	return update(contentId, languageId, contentVersionVO, null);
    }        
	
    public ContentVersionVO update(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, InfoGluePrincipal principal) throws ConstraintException, SystemException
    {
        ContentVersionVO updatedContentVersionVO;
		
        Database db = CastorDatabaseService.getDatabase();

        beginTransaction(db);
        
        try
        {     
        	ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId, db);
        	ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVO.getContentTypeDefinitionId(), db);
        	ConstraintExceptionBuffer ceb = contentVersionVO.validateAdvanced(contentTypeDefinitionVO);
            ceb.throwIfNotEmpty();
            
            ContentVersion contentVersion = null;
            
	        if(contentVersionVO.getId() == null)
	    	{
	    		logger.info("Creating the entity because there was no version at all for: " + contentId + " " + languageId);
	    		contentVersion = create(contentId, languageId, contentVersionVO, null,  db);
	    	}
	    	else
	    	{
				ContentVersionVO oldContentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionVO.getId(), db);
	    	    if(!oldContentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
				{
					List events = new ArrayList();
					contentVersion = ContentStateController.changeState(oldContentVersionVO.getId(), ContentVersionVO.WORKING_STATE, (contentVersionVO.getVersionComment().equals("No comment") ? "new working version" : contentVersionVO.getVersionComment()), false, null, principal, oldContentVersionVO.getContentId(), db, events);
					contentVersion.setVersionValue(contentVersionVO.getVersionValue());
				}
				else
				{
					contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionVO.getId(), db);
					//contentVersionVO.setModifiedDateTime(DateHelper.getSecondPreciseDate());
					contentVersion.setValueObject(contentVersionVO);
				}
	    	}

		    if(principal != null && contentTypeDefinitionVO.getName().equalsIgnoreCase("Meta info"))
		    {
		    	SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithMetaInfoContentId(db, contentId);
		    	//SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithMetaInfoContentId(db, contentId);
				if(siteNodeVO.getMetaInfoContentId() != null && siteNodeVO.getMetaInfoContentId().equals(contentId))
				{
			    	SiteNodeVersionVO latestSiteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(db, siteNodeVO.getId());
			    	latestSiteNodeVersionVO.setVersionModifier(contentVersionVO.getVersionModifier());
			    	latestSiteNodeVersionVO.setModifiedDateTime(DateHelper.getSecondPreciseDate());
					SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().acUpdate(principal, latestSiteNodeVersionVO, db);
				}
			}

	    	registryController.updateContentVersion(contentVersion, db);

	    	updatedContentVersionVO = contentVersion.getValueObject();
	    	
	    	commitTransaction(db);  
        }
        catch(ConstraintException ce)
        {
        	logger.warn("Validation error:" + ce, ce);
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
        
    	return updatedContentVersionVO; //(ContentVersionVO) updateEntity(ContentVersionImpl.class, realContentVersionVO);
    }        

    /**
     * This method updates the contentversion.
     */
    
    public ContentVersionVO update(Integer contentVersionId, ContentVersionVO contentVersionVO) throws ConstraintException, SystemException
    {
    	return update(contentVersionId, contentVersionVO, null);
    }        
	
    public ContentVersionVO update(Integer contentVersionId, ContentVersionVO contentVersionVO, InfoGluePrincipal principal) throws ConstraintException, SystemException
    {
        ContentVersionVO updatedContentVersionVO;
		
        Database db = CastorDatabaseService.getDatabase();

        beginTransaction(db);
        
        try
        {     
            ContentVersion contentVersion = getContentVersionWithId(contentVersionId, db);
	    	contentVersion.setValueObject(contentVersionVO);
	    	
	    	ContentTypeDefinition contentTypeDefinition = contentVersion.getOwningContent().getContentTypeDefinition();
            
		    if(principal != null && contentTypeDefinition.getName().equalsIgnoreCase("Meta info"))
		    {
		    	SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithMetaInfoContentId(db, contentVersion.getValueObject().getContentId());
				if(siteNode.getMetaInfoContentId() != null && siteNode.getMetaInfoContentId().equals(contentVersion.getValueObject().getContentId()))
				{
			    	SiteNodeVersion latestSiteNodeVersion = SiteNodeVersionController.getController().getLatestSiteNodeVersion(db, siteNode.getId(), false);
			    	latestSiteNodeVersion.setVersionModifier(contentVersionVO.getVersionModifier());
			    	latestSiteNodeVersion.setModifiedDateTime(DateHelper.getSecondPreciseDate());
					SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().acUpdate(principal, latestSiteNodeVersion.getValueObject(), db);
				}
			}

	    	registryController.updateContentVersion(contentVersion, db);

	    	updatedContentVersionVO = contentVersion.getValueObject();
	    	
	    	commitTransaction(db);  
        }
        catch(ConstraintException ce)
        {
        	logger.warn("Validation error:" + ce, ce);
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
        
    	return updatedContentVersionVO; //(ContentVersionVO) updateEntity(ContentVersionImpl.class, realContentVersionVO);
    }        

	public List getPublishedActiveContentVersionVOList(Integer contentId) throws SystemException, Bug, Exception
    {
        List contentVersionVOList = new ArrayList();
        
        Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
        try
        {        
	        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.stateId = $2 AND cv.isActive = $3 ORDER BY cv.contentVersionId desc");
	    	oql.bind(contentId);
	    	oql.bind(ContentVersionVO.PUBLISHED_STATE);
	    	oql.bind(true);
	    	
	    	QueryResults results = oql.execute(Database.ReadOnly);
			
			while (results.hasMore()) 
	        {
	        	ContentVersion contentVersion = (ContentVersion)results.next();
	        	contentVersionVOList.add(contentVersion.getValueObject());
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
            
		return contentVersionVOList;
    }

	public List getPublishedActiveContentVersionVOList(Integer contentId, Database db) throws SystemException, Bug, Exception
    {
        List contentVersionVOList = new ArrayList();
        
        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.stateId = $2 AND cv.isActive = $3 ORDER BY cv.contentVersionId desc");
    	oql.bind(contentId);
    	oql.bind(ContentVersionVO.PUBLISHED_STATE);
    	oql.bind(true);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		while (results.hasMore()) 
        {
        	ContentVersion contentVersion = (ContentVersion)results.next();
        	contentVersionVOList.add(contentVersion.getValueObject());
        }
		
		results.close();
		oql.close();
            
		return contentVersionVOList;
    }

    
	public ContentVersion getLatestPublishedContentVersion(Integer contentId) throws SystemException, Bug, Exception
    {
        ContentVersion contentVersion = null;
        
        Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
        try
        {        
	        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.stateId = $2 AND cv.isActive = $3 ORDER BY cv.contentVersionId desc");
	    	oql.bind(contentId);
	    	oql.bind(ContentVersionVO.PUBLISHED_STATE);
	    	oql.bind(true);
	    	
	    	QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
	        {
	        	contentVersion = (ContentVersion)results.next();
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
            
		return contentVersion;
    }


	public ContentVersion getLatestPublishedContentVersion(Integer contentId, Integer languageId) throws SystemException, Bug, Exception
    {
        ContentVersion contentVersion = null;
        
        Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
        try
        {        
	        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.language.languageId = $2 AND cv.stateId = $3 AND cv.isActive = $4 ORDER BY cv.contentVersionId desc");
	    	oql.bind(contentId);
	    	oql.bind(languageId);
	    	oql.bind(ContentVersionVO.PUBLISHED_STATE);
	    	oql.bind(true);
	    	
	    	QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
	        {
	        	contentVersion = (ContentVersion)results.next();
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
            
		return contentVersion;
    }


	public ContentVersion getLatestPublishedContentVersion(Integer contentId, Integer languageId, Database db) throws SystemException, Bug, Exception
    {
        ContentVersion contentVersion = null;
        
        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.language.languageId = $2 AND cv.stateId = $3 AND cv.isActive = $4 ORDER BY cv.contentVersionId desc");
    	oql.bind(contentId);
    	oql.bind(languageId);
    	oql.bind(ContentVersionVO.PUBLISHED_STATE);
    	oql.bind(true);
    	
    	QueryResults results = oql.execute();
		this.logger.info("Fetching entity in read/write mode");

		if (results.hasMore()) 
        {
        	contentVersion = (ContentVersion)results.next();
        }
            
		results.close();
		oql.close();

		return contentVersion;
    }


	/**
	 * This method returns the version previous to the one sent in.
	 */
	
	public ContentVersionVO getPreviousContentVersionVO(Integer contentId, Integer languageId, Integer contentVersionId) throws SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();
    	ContentVersionVO contentVersionVO = null;

        beginTransaction(db);

        try
        {           
            OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.language.languageId = $2 AND cv.contentVersionId < $3 ORDER BY cv.contentVersionId desc");
        	oql.bind(contentId);
        	oql.bind(languageId);
        	oql.bind(contentVersionId);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
            {
            	ContentVersion contentVersion = (ContentVersion)results.next();
            	logger.info("found one:" + contentVersion.getValueObject());
            	contentVersionVO = contentVersion.getValueObject();
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
    	
		return contentVersionVO;
    }


	/**
	 * This method returns the version previous to the one sent in.
	 */
	
	public ContentVersionVO getPreviousActiveContentVersionVO(Integer contentId, Integer languageId, Integer contentVersionId, Database db) throws SystemException, Bug, Exception
    {
    	ContentVersionVO contentVersionVO = null;

        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.language.languageId = $2 AND cv.isActive = $3 AND cv.contentVersionId < $4 ORDER BY cv.contentVersionId desc");
    	oql.bind(contentId);
    	oql.bind(languageId);
    	oql.bind(new Boolean(true));
    	oql.bind(contentVersionId);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		if (results.hasMore()) 
        {
        	ContentVersion contentVersion = (ContentVersion)results.next();
        	logger.info("found one:" + contentVersion.getValueObject());
        	contentVersionVO = contentVersion.getValueObject();
        }
    	
		results.close();
		oql.close();

		return contentVersionVO;
    }


	/**
	 * This method deletes the relation to a digital asset - not the asset itself.
	 */
	public ContentVersionVO deleteDigitalAssetRelation(Integer contentVersionId, Integer digitalAssetId, InfoGluePrincipal principal) throws SystemException, Bug
    {
		ContentVersionVO editedContentVersionVO = null;
			
    	Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);

        try
        {      
        	ContentVersion contentVersion = null;
        	ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId, db);
        	DigitalAssetVO digitalAssetVO = DigitalAssetController.getController().getDigitalAssetVOWithId(digitalAssetId, db);
        	if(!contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
			{
		    	List events = new ArrayList();
		    	contentVersion = ContentStateController.changeState(contentVersionVO.getId(), ContentVersionVO.WORKING_STATE, "new working version", false, null, principal, contentVersionVO.getContentId(), db, events, digitalAssetId);
				//digitalAssetVO = DigitalAssetController.getController().getLatestDigitalAssetVO(contentVersion.getId(), digitalAssetVO.getAssetKey(), db);
			}
			else
			{
				contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionVO.getId(), db);

				DigitalAsset digitalAsset = DigitalAssetController.getDigitalAssetWithId(digitalAssetVO.getId(), db);			
		    	contentVersion.getDigitalAssets().remove(digitalAsset);
		    	digitalAsset.getContentVersions().remove(contentVersion);
	        
	            logger.info("digitalAsset size after:" + digitalAsset.getContentVersions().size());
	            if(digitalAsset.getContentVersions().size() == 0)
	            	db.remove(digitalAsset);
			}
    	    
    	    editedContentVersionVO = contentVersion.getValueObject();
    	    
    	    /*
	    	DigitalAsset digitalAsset = DigitalAssetController.getDigitalAssetWithId(digitalAssetVO.getId(), db);			
	    	contentVersion.getDigitalAssets().remove(digitalAsset);
	    	digitalAsset.getContentVersions().remove(contentVersion);
        
            logger.info("digitalAsset size after:" + digitalAsset.getContentVersions().size());
            if(digitalAsset.getContentVersions().size() == 0)
            	db.remove(digitalAsset);
	        */
    	    commitTransaction(db);
        }
	    catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
        return editedContentVersionVO;
    }
    
	
	/**
	 * This method deletes the relation to a digital asset - not the asset itself.
	 */
	public void deleteDigitalAssetRelation(Integer contentVersionId, DigitalAsset digitalAsset, Database db) throws SystemException, Bug
    {
    	ContentVersion contentVersion = getContentVersionWithId(contentVersionId, db);
		contentVersion.getDigitalAssets().remove(digitalAsset);
        digitalAsset.getContentVersions().remove(contentVersion);
    }
    
	
	/**
	 * This method assigns the same digital assets the old content-version has.
	 * It's ofcourse important that noone deletes the digital asset itself for then it's lost to everyone.
	 */
	/*
	public void copyDigitalAssets(ContentVersion originalContentVersion, ContentVersion newContentVersion, Database db) throws ConstraintException, SystemException, Exception
	{
		copyDigitalAssets(originalContentVersion, newContentVersion, true, db);
	}
	*/
	public void copyDigitalAssets(ContentVersion originalContentVersion, ContentVersion newContentVersion, boolean allowBrokenAssets, boolean duplicateAssets, Integer excludedAssetId, Database db) throws ConstraintException, SystemException, Exception
	{
	    Collection digitalAssets = originalContentVersion.getDigitalAssets();	

	    if(!duplicateAssets)
	    {	
		    Iterator digitalAssetsIterator = digitalAssets.iterator();
			while(digitalAssetsIterator.hasNext())
			{
			    DigitalAsset digitalAsset = (DigitalAsset)digitalAssetsIterator.next();
			    logger.info("Make copy of reference to digitalAssets " + digitalAsset.getAssetKey());
			    if(excludedAssetId == null || !digitalAsset.getId().equals(excludedAssetId))
			    {
			    	newContentVersion.getDigitalAssets().add(digitalAsset);
			    	digitalAsset.getContentVersions().add(newContentVersion);
			    }
			    else
			    {
			    	logger.info("Not copying the excluded asset:" + digitalAsset.getId());
			    }
			}
	    }
	    else
	    {
		    Iterator digitalAssetsIterator = digitalAssets.iterator();
			while(digitalAssetsIterator.hasNext())
			{
			    DigitalAsset digitalAsset = (DigitalAsset)digitalAssetsIterator.next();
			    if(excludedAssetId == null || !digitalAsset.getId().equals(excludedAssetId))
			    {
				    logger.info("Copying digitalAssets " + digitalAsset.getAssetKey());
				    DigitalAssetVO digitalAssetVO = digitalAsset.getValueObject();
				    
				    InputStream is = DigitalAssetController.getController().getAssetInputStream(digitalAsset, true);
			
				    if(is == null && !allowBrokenAssets)
				    	throw new ConstraintException("DigitalAsset.assetBlob", "3308", "Broken asset found on content '" + originalContentVersion.getValueObject().getContentName() + "' with id " + originalContentVersion.getValueObject().getContentId());
				    
			        try
				    {
			        	synchronized (is)
					    {
						    DigitalAssetController.create(digitalAssetVO, is, newContentVersion, db);
						}
				    }
			        catch (Exception e) 
			        {
						e.printStackTrace();
					}
				    finally
				    {
				    	if(is != null)
				    		is.close();
				    }
			    }
			    else
			    {
			    	logger.info("Not copying the excluded asset:" + digitalAsset.getId());
			    }
				logger.info("digitalAssets:" + digitalAssets.size());
			}
	    }
	}	

	public DigitalAssetVO copyDigitalAssetAndRemoveOldReference(ContentVersion contentVersion, DigitalAsset digitalAsset, boolean allowBrokenAssets, Database db) throws ConstraintException, SystemException, Exception
	{
	    logger.info("Copying digitalAsset " + digitalAsset.getAssetKey());
	    DigitalAssetVO digitalAssetVO = digitalAsset.getValueObject();
	    
	    InputStream is = DigitalAssetController.getController().getAssetInputStream(digitalAsset, true);

	    if(is == null && !allowBrokenAssets)
	    	throw new ConstraintException("DigitalAsset.assetBlob", "3308", "Broken asset found on content '" + contentVersion.getValueObject().getContentName() + "' with id " + contentVersion.getValueObject().getContentId());
	    
        try
	    {
        	synchronized (is)
		    {
        		digitalAssetVO = DigitalAssetController.create(digitalAssetVO, is, contentVersion, db);
        		contentVersion.getDigitalAssets().remove(digitalAsset);
        		digitalAsset.getContentVersions().remove(contentVersion);
			}
	    }
        catch (Exception e) 
        {
			e.printStackTrace();
		}
	    finally
	    {
	    	if(is != null)
	    		is.close();
	    }
	    
		logger.info("digitalAssetVO:" + digitalAssetVO);
		
		return digitalAssetVO;
	}	
	
	
	/**
	 * This method fetches a value from the xml that is the contentVersions Value. If the 
	 * contentVersioVO is null the contentVersion has not been created yet and no values are present.
	 */
	public String getAttributeValue(Integer contentVersionId, String attributeName, boolean escapeHTML) throws SystemException
	{
		String value = "";
		ContentVersionVO contentVersionVO = getContentVersionVOWithId(contentVersionId);
		
		if(contentVersionVO != null)
		{
			try
			{
				logger.info("attributeName:" + attributeName);
				logger.info("VersionValue:"  + contentVersionVO.getVersionValue());
				value = getAttributeValue(contentVersionVO, attributeName, escapeHTML);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		//logger.info("value:" + value);
		return value;
	}

	/**
	 * Returns an attribute value from the ContentVersionVO
	 *
	 * @param contentVersionVO The version on which to find the value
	 * @param attributeName THe name of the attribute whose value is wanted
	 * @param escapeHTML A boolean indicating if the result should be escaped
	 * @return The String vlaue of the attribute, or blank if it doe snot exist.
	 */
	public String getAttributeValue(ContentVersionVO contentVersionVO, String attributeName, boolean escapeHTML)
	{
		String value = "";
		String xml = contentVersionVO.getVersionValue();

		int startTagIndex = xml.indexOf("<" + attributeName + ">");
		int endTagIndex   = xml.indexOf("]]></" + attributeName + ">");

		if(startTagIndex > 0 && startTagIndex < xml.length() && endTagIndex > startTagIndex && endTagIndex <  xml.length())
		{
			value = xml.substring(startTagIndex + attributeName.length() + 11, endTagIndex);
			if(escapeHTML)
				value = new VisualFormatter().escapeHTML(value);
		}		

		return value;
	}


	/**
	 * This method fetches a value from the xml that is the contentVersions Value. If the 
	 * contentVersioVO is null the contentVersion has not been created yet and no values are present.
	 */
	 
	public void updateAttributeValue(Integer contentVersionId, String attributeName, String attributeValue, InfoGluePrincipal infogluePrincipal) throws SystemException, Bug
	{
		ContentVersionVO contentVersionVO = getContentVersionVOWithId(contentVersionId);
		
		if(contentVersionVO != null)
		{
			try
			{
				InputSource inputSource = new InputSource(new StringReader(contentVersionVO.getVersionValue()));
				
				DOMParser parser = new DOMParser();
				parser.parse(inputSource);
				Document document = parser.getDocument();
				
				NodeList attributesNodeList = document.getElementsByTagName("attributes");
				if(attributesNodeList.getLength() == 0)
					throw new SystemException("Faulty XML - fix it by hand and retry");
				
				Node attributesNode = attributesNodeList.item(0);
				//Node attributesNode = nl.item(0);
				StringBuffer test = new StringBuffer();
				org.infoglue.cms.util.XMLHelper.serializeDom(attributesNode, test);

				boolean existed = false;
				NodeList nl = attributesNode.getChildNodes();
				for(int i=0; i<nl.getLength(); i++)
				{
					Node n = nl.item(i);
					if(n.getNodeName().equalsIgnoreCase(attributeName))
					{
						logger.info("Found node with name:" + attributeName);
						if(n.getFirstChild() != null && n.getFirstChild().getNodeValue() != null)
						{
							logger.info("Yep");
							n.getFirstChild().setNodeValue(attributeValue);
							existed = true;
							break;
						}
						else
						{
							logger.info("Yep2");
							CDATASection cdata = document.createCDATASection(attributeValue);
							n.appendChild(cdata);
							existed = true;
							break;
						}
					}
				}
				
				if(existed == false)
				{
					logger.info("attributeName:" + attributeName);
					org.w3c.dom.Element attributeElement = document.createElement(attributeName);
					attributesNode.appendChild(attributeElement);
					CDATASection cdata = document.createCDATASection(attributeValue);
					attributeElement.appendChild(cdata);
				}
				
				StringBuffer sb = new StringBuffer();
				org.infoglue.cms.util.XMLHelper.serializeDom(document.getDocumentElement(), sb);
				logger.info("sb:" + sb);
				contentVersionVO.setVersionValue(sb.toString());
				contentVersionVO.setVersionModifier(infogluePrincipal.getName());
				update(contentVersionVO.getContentId(), contentVersionVO.getLanguageId(), contentVersionVO, infogluePrincipal);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new ContentVersionVO();
	}

	/**
	 * Recursive methods to get all contentVersions of a given state under the specified parent content.
	 */ 
	
    public void getContentAndAffectedItemsRecursive(Integer contentId, Integer stateId, List siteNodeVersionVOList, List contenteVersionVOList, boolean mustBeFirst, boolean includeMetaInfo) throws ConstraintException, SystemException
	{
        Database db = CastorDatabaseService.getDatabase();

	    beginTransaction(db);

        try
        {
            Content content = ContentController.getContentController().getContentWithId(contentId, db);

            getContentAndAffectedItemsRecursive(content, stateId, new ArrayList(), new ArrayList(), db, siteNodeVersionVOList, contenteVersionVOList, mustBeFirst, includeMetaInfo);
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
	}
	
	private void getContentAndAffectedItemsRecursive(Content content, Integer stateId, List checkedSiteNodes, List checkedContents, Database db, List siteNodeVersionVOList, List contentVersionVOList, boolean mustBeFirst, boolean includeMetaInfo) throws ConstraintException, SystemException, Exception
	{
	    //checkedSiteNodes.add(content.getId());
	    checkedContents.add(content.getId());
        
	    List contentVersions = getLatestContentVersionWithParent(content.getId(), stateId, db, mustBeFirst);
	    
		Iterator contentVersionsIterator = contentVersions.iterator();
	    while(contentVersionsIterator.hasNext())
	    {
	        ContentVersion contentVersion = (ContentVersion)contentVersionsIterator.next();
	        contentVersionVOList.add(contentVersion.getValueObject());
	        
	        List relatedEntities = RegistryController.getController().getMatchingRegistryVOListForReferencingEntity(ContentVersion.class.getName(), contentVersion.getId().toString(), db);
	        logger.info("relatedEntities:" + relatedEntities);
	        Iterator relatedEntitiesIterator = relatedEntities.iterator();
	        
	        while(relatedEntitiesIterator.hasNext())
	        {
	            RegistryVO registryVO = (RegistryVO)relatedEntitiesIterator.next();
	            logger.info("registryVO:" + registryVO.getEntityName() + ":" + registryVO.getEntityId());
	            if(registryVO.getEntityName().equals(SiteNode.class.getName()) && !checkedSiteNodes.contains(new Integer(registryVO.getEntityId())))
	            {
	                try
	                {
		                SiteNode relatedSiteNode = SiteNodeController.getController().getSiteNodeWithId(new Integer(registryVO.getEntityId()), db);
		                SiteNodeVersion relatedSiteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionIfInState(relatedSiteNode, stateId, db);
		                if(relatedSiteNodeVersion != null && content.getRepository().getId().intValue() == relatedSiteNodeVersion.getOwningSiteNode().getRepository().getId().intValue())
		                {
		                    siteNodeVersionVOList.add(relatedSiteNodeVersion.getValueObject());
		                }
	                }
	                catch(Exception e)
	                {
	                    logger.warn("The related siteNode with id:" + registryVO.getEntityId() + " could not be loaded.", e);
	                }
	                
	    		    checkedSiteNodes.add(new Integer(registryVO.getEntityId()));
	            }
	            else if(registryVO.getEntityName().equals(Content.class.getName()) && !checkedContents.contains(new Integer(registryVO.getEntityId())))
	            {
	                try
	                {
		                Content relatedContent = ContentController.getContentController().getContentWithId(new Integer(registryVO.getEntityId()), db);
		                if(includeMetaInfo || (!includeMetaInfo && (relatedContent.getContentTypeDefinition() == null || !relatedContent.getContentTypeDefinition().getName().equalsIgnoreCase("Meta info"))))
		                {
			                List relatedContentVersions = ContentVersionController.getContentVersionController().getLatestActiveContentVersionIfInState(relatedContent, stateId, db);
			                logger.info("relatedContentVersions:" + relatedContentVersions.size());
			                
			                Iterator relatedContentVersionsIterator = relatedContentVersions.iterator();
			                while(relatedContentVersionsIterator.hasNext())
			                {
			                    ContentVersion relatedContentVersion = (ContentVersion)relatedContentVersionsIterator.next();
				                if(relatedContentVersion != null && content.getRepository().getId().intValue() == relatedContentVersion.getOwningContent().getRepository().getId().intValue())
				                {
				        	        contentVersionVOList.add(relatedContentVersion.getValueObject());
				                    logger.info("Added:" + relatedContentVersion.getId());
					            }
			
			                }
			            }
	                }
	                catch(Exception e)
	                {
	                    logger.warn("The related content with id:" + registryVO.getEntityId() + " could not be loaded.", e);
	                }
	                
	    		    checkedContents.add(new Integer(registryVO.getEntityId()));
	            }
	        }	    

		}
		
	    //	  Get the children of this content and do the recursion
		Collection childContentList = content.getChildren();
		Iterator cit = childContentList.iterator();
		while (cit.hasNext())
		{
			try
			{
				Content citContent = (Content) cit.next();
				getContentAndAffectedItemsRecursive(citContent, stateId, checkedSiteNodes, checkedContents, db, siteNodeVersionVOList, contentVersionVOList, mustBeFirst, includeMetaInfo);
			}
			catch (Exception e) 
			{
				logger.error("Was a problem with content:" + e.getMessage());
			}
		}
		
	}
	
	public void getContentAndAffectedItemsRecursive(Content content, Integer stateId, List checkedSiteNodes, List checkedContents, Database db, Collection siteNodeVersionVOList, Collection contentVersionVOList, boolean mustBeFirst, boolean includeMetaInfo, int maxLevels, int currentLevel) throws ConstraintException, SystemException, Exception
	{
        logger.info("content:" + content.getName());

        //checkedSiteNodes.add(content.getId());
        checkedContents.add(content.getId());
        
	    List contentVersions = getLatestContentVersionWithParent(content.getId(), stateId, db, mustBeFirst);
	    
		Iterator contentVersionsIterator = contentVersions.iterator();
	    while(contentVersionsIterator.hasNext())
	    {
	        ContentVersion contentVersion = (ContentVersion)contentVersionsIterator.next();
	        contentVersionVOList.add(contentVersion.getValueObject());
	        
	        List relatedEntities = RegistryController.getController().getMatchingRegistryVOListForReferencingEntity(ContentVersion.class.getName(), contentVersion.getId().toString(), db);
	        logger.info("relatedEntities:" + relatedEntities);
	        Iterator relatedEntitiesIterator = relatedEntities.iterator();
	        
	        while(relatedEntitiesIterator.hasNext())
	        {
	            RegistryVO registryVO = (RegistryVO)relatedEntitiesIterator.next();
	            logger.info("registryVO:" + registryVO.getEntityName() + ":" + registryVO.getEntityId());
	            if(registryVO.getEntityName().equals(SiteNode.class.getName()) && !checkedSiteNodes.contains(new Integer(registryVO.getEntityId())))
	            {
	                try
	                {
		                SiteNode relatedSiteNode = SiteNodeController.getController().getSiteNodeWithId(new Integer(registryVO.getEntityId()), db);
		                SiteNodeVersion relatedSiteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionIfInState(relatedSiteNode, stateId, db);
		                if(relatedSiteNodeVersion != null && content.getRepository().getId().intValue() == relatedSiteNodeVersion.getOwningSiteNode().getRepository().getId().intValue())
		                {
		                    siteNodeVersionVOList.add(relatedSiteNodeVersion.getValueObject());
		                }
	                }
	                catch(Exception e)
	                {
	                    logger.warn("The related siteNode with id:" + registryVO.getEntityId() + " could not be loaded.", e);
	                }
	                
	    		    checkedSiteNodes.add(new Integer(registryVO.getEntityId()));
	            }
	            else if(registryVO.getEntityName().equals(Content.class.getName()) && !checkedContents.contains(new Integer(registryVO.getEntityId())))
	            {
	                try
	                {
		                Content relatedContent = ContentController.getContentController().getContentWithId(new Integer(registryVO.getEntityId()), db);
		                if(includeMetaInfo || (!includeMetaInfo && (relatedContent.getContentTypeDefinition() == null || !relatedContent.getContentTypeDefinition().getName().equalsIgnoreCase("Meta info"))))
		                {
			                List relatedContentVersions = ContentVersionController.getContentVersionController().getLatestActiveContentVersionIfInState(relatedContent, stateId, db);
			                logger.info("relatedContentVersions:" + relatedContentVersions.size());
			                
			                Iterator relatedContentVersionsIterator = relatedContentVersions.iterator();
			                while(relatedContentVersionsIterator.hasNext())
			                {
			                    ContentVersion relatedContentVersion = (ContentVersion)relatedContentVersionsIterator.next();
				                if(relatedContentVersion != null && content.getRepository().getId().intValue() == relatedContentVersion.getOwningContent().getRepository().getId().intValue())
				                {
				        	        contentVersionVOList.add(relatedContentVersion.getValueObject());
				                    logger.info("Added:" + relatedContentVersion.getId());
				                    if(currentLevel < maxLevels)
				                    	getContentAndAffectedItemsRecursive(relatedContentVersion.getOwningContent(), stateId, checkedSiteNodes, checkedContents, db, siteNodeVersionVOList, contentVersionVOList, mustBeFirst, includeMetaInfo, maxLevels, currentLevel + 1);
					            }
			
			                }
			            }
	                }
	                catch(Exception e)
	                {
	                    logger.warn("The related content with id:" + registryVO.getEntityId() + " could not be loaded.", e);
	                }
	                
	    		    checkedContents.add(new Integer(registryVO.getEntityId()));
	            }
	        }	    

		}
		
	    //	  Get the children of this content and do the recursion
		Collection childContentList = content.getChildren();
		Iterator cit = childContentList.iterator();
		while (cit.hasNext())
		{
			Content citContent = (Content) cit.next();
			//if(currentLevel < maxLevels)
			getContentAndAffectedItemsRecursive(citContent, stateId, checkedSiteNodes, checkedContents, db, siteNodeVersionVOList, contentVersionVOList, mustBeFirst, includeMetaInfo, maxLevels, currentLevel);
		}
		
	}

	/**
	 * This method are here to return all content versions that are x number of versions behind the current active version. This is for cleanup purposes.
	 * 
	 * @param numberOfVersionsToKeep
	 * @return
	 * @throws SystemException 
	 */
	
	public int cleanContentVersions(int numberOfVersionsToKeep, boolean keepOnlyOldPublishedVersions, long minimumTimeBetweenVersionsDuringClean, boolean deleteVersions) throws SystemException 
	{
		int cleanedVersions = 0;
		
		int batchLimit = 50;
		List languageVOList = LanguageController.getController().getLanguageVOList();
		
		Iterator<LanguageVO> languageVOListIterator = languageVOList.iterator();
		while(languageVOListIterator.hasNext())
		{
			LanguageVO languageVO = languageVOListIterator.next();
			List<SmallestContentVersionVO> contentVersionVOList = getSmallestContentVersionVOList(languageVO.getId(), numberOfVersionsToKeep, keepOnlyOldPublishedVersions, minimumTimeBetweenVersionsDuringClean);
			
			logger.info("Deleting " + contentVersionVOList.size() + " versions for language " + languageVO.getName());
			int maxIndex = (contentVersionVOList.size() > batchLimit ? batchLimit : contentVersionVOList.size());
			List partList = contentVersionVOList.subList(0, maxIndex);
			while(partList.size() > 0)
			{
				if(deleteVersions)
					cleanVersions(numberOfVersionsToKeep, partList);
				cleanedVersions = cleanedVersions + partList.size();
				partList.clear();
				maxIndex = (contentVersionVOList.size() > batchLimit ? batchLimit : contentVersionVOList.size());
				partList = contentVersionVOList.subList(0, maxIndex);
			}
		}
		return cleanedVersions;
	}
	
	/**
	 * Cleans the list of versions - even published ones. Use with care only for cleanup purposes.
	 * 
	 * @param numberOfVersionsToKeep
	 * @param contentVersionVOList
	 * @throws SystemException
	 */
	
	private void cleanVersions(int numberOfVersionsToKeep, List<SmallestContentVersionVO> contentVersionVOList) throws SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
    	
        beginTransaction(db);

        try
        {
			Iterator<SmallestContentVersionVO> contentVersionVOIdListIterator = contentVersionVOList.iterator();
			while(contentVersionVOIdListIterator.hasNext())
			{
				SmallestContentVersionVO contentVersionVO = contentVersionVOIdListIterator.next();
				ContentVersion contentVersion = getContentVersionWithId(contentVersionVO.getContentVersionId(), db);
				logger.info("Deleting the contentVersion " + contentVersion.getId() + " on content " + contentVersion.getOwningContent());
				delete(contentVersion, db, true);
			}

			commitTransaction(db);

			Thread.sleep(1000);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        finally
        {
        	closeDatabase(db);
        }
	}
	
	/**
	 * This method are here to return all content versions that are x number of versions behind the current active version. This is for cleanup purposes.
	 * 
	 * @param numberOfVersionsToKeep
	 * @return
	 * @throws SystemException 
	 */
	
	public List<SmallestContentVersionVO> getSmallestContentVersionVOList(Integer languageId, int numberOfVersionsToKeep, boolean keepOnlyOldPublishedVersions, long minimumTimeBetweenVersionsDuringClean) throws SystemException 
	{
		logger.info("numberOfVersionsToKeep:" + numberOfVersionsToKeep);

		Database db = CastorDatabaseService.getDatabase();
    	
    	List<SmallestContentVersionVO> contentVersionsIdList = new ArrayList();

        beginTransaction(db);

        try
        {
            OQLQuery oql = db.getOQLQuery("SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl cv WHERE cv.languageId = $1 ORDER BY cv.contentId, cv.contentVersionId desc");
        	oql.bind(languageId);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
        	int numberOfLaterVersions = 0;
        	Integer previousContentId = null;
        	Date previousDate = null;
        	long difference = -1;
        	List<SmallestContentVersionVO> keptContentVersionVOList = new ArrayList<SmallestContentVersionVO>();
        	List<SmallestContentVersionVO> potentialContentVersionVOList = new ArrayList<SmallestContentVersionVO>();
        	List<SmallestContentVersionVO> versionInitialSuggestions = new ArrayList<SmallestContentVersionVO>();
        	List<SmallestContentVersionVO> versionNonPublishedSuggestions = new ArrayList<SmallestContentVersionVO>();

        	while (results.hasMore())
            {
				SmallestContentVersionImpl version = (SmallestContentVersionImpl)results.next();
				if(previousContentId != null && previousContentId.intValue() != version.getContentId().intValue())
				{
					if(minimumTimeBetweenVersionsDuringClean != -1 && versionInitialSuggestions.size() > numberOfVersionsToKeep)
					{
						Iterator potentialContentVersionVOListIterator = potentialContentVersionVOList.iterator();
						while(potentialContentVersionVOListIterator.hasNext())
						{
							SmallestContentVersionVO potentialContentVersionVO = (SmallestContentVersionVO)potentialContentVersionVOListIterator.next();
							
							SmallestContentVersionVO firstInitialSuggestedContentVersionVO = null;
							Iterator versionInitialSuggestionsIterator = versionInitialSuggestions.iterator();
							while(versionInitialSuggestionsIterator.hasNext())
							{
								SmallestContentVersionVO initialSuggestedContentVersionVO = (SmallestContentVersionVO)versionInitialSuggestionsIterator.next();
								if(initialSuggestedContentVersionVO.getStateId().equals(ContentVersionVO.PUBLISHED_STATE))
								{
									firstInitialSuggestedContentVersionVO = initialSuggestedContentVersionVO;
									break;
								}
							}
							
							if(firstInitialSuggestedContentVersionVO != null)
							{
								keptContentVersionVOList.remove(potentialContentVersionVO);
								keptContentVersionVOList.add(firstInitialSuggestedContentVersionVO);
								versionInitialSuggestions.remove(firstInitialSuggestedContentVersionVO);
								versionInitialSuggestions.add(potentialContentVersionVO);
							}
						}
					}
										
					contentVersionsIdList.addAll(versionNonPublishedSuggestions);
					contentVersionsIdList.addAll(versionInitialSuggestions);
					potentialContentVersionVOList.clear();
					versionInitialSuggestions.clear();
					versionNonPublishedSuggestions.clear();
					keptContentVersionVOList.clear();
					
					numberOfLaterVersions = 0;
					previousDate = null;
					difference = -1;
					potentialContentVersionVOList = new ArrayList();
				}
				else if(previousDate != null)
				{
					difference = previousDate.getTime() - version.getModifiedDateTime().getTime();
				}
				
				if(numberOfLaterVersions > numberOfVersionsToKeep || (keepOnlyOldPublishedVersions && numberOfLaterVersions > 0 && !version.getStateId().equals(ContentVersionVO.PUBLISHED_STATE)))
            	{
					if(version.getStateId().equals(ContentVersionVO.PUBLISHED_STATE))
					{
						versionInitialSuggestions.add(version.getValueObject());
					}
					else
					{
						versionNonPublishedSuggestions.add(version.getValueObject());
					}
            	}
				else if(previousDate != null && difference != -1 && difference < minimumTimeBetweenVersionsDuringClean)
				{
					keptContentVersionVOList.add(version.getValueObject());
					potentialContentVersionVOList.add(version.getValueObject());		
					numberOfLaterVersions++;
				}
				else
				{
					keptContentVersionVOList.add(version.getValueObject());
					previousDate = version.getModifiedDateTime();
					numberOfLaterVersions++;
				}

				previousContentId = version.getContentId();
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
        
		return contentVersionsIdList;
	}

	/**
	 * This method are here to return all content versions that have somewhat heavy digitalAssets
	 * and are x number of versions behind the current active version. This is for archiving purposes.
	 * 
	 * @param numberOfVersionsToKeep
	 * @param assetSizeLimit
	 * @return
	 * @throws SystemException 
	 */
	
	public OptimizationBeanList getHeavyContentVersions(int numberOfVersionsToKeep, int assetSizeLimit, int assetNumberLimit) throws SystemException 
	{
    	Database db = CastorDatabaseService.getDatabase();
    	
    	OptimizationBeanList optimizationBeanList = new OptimizationBeanList();

        beginTransaction(db);

        try
        {
            //OQLQuery oql = db.getOQLQuery( "SELECT da FROM org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl da WHERE da.assetFileSize >= $1 ORDER BY da.digitalAssetId asc");
            OQLQuery oql = db.getOQLQuery( "SELECT da FROM org.infoglue.cms.entities.content.impl.simple.MediumDigitalAssetImpl da WHERE da.assetFileSize >= $1 ORDER BY da.digitalAssetId asc");
        	oql.bind(assetSizeLimit);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
        	int i = 0;
			while (results.hasMore() && i < assetNumberLimit) 
            {
				boolean keep = true;
            	
            	MediumDigitalAssetImpl digitalAsset = (MediumDigitalAssetImpl)results.next();
            	if(digitalAsset.getAssetKey().equals("portletentityregistry.xml"))
            		keep = false;
            	
            	Collection contentVersions = digitalAsset.getContentVersions();
            	Iterator contentVersionsIterator = contentVersions.iterator();
            	ContentVersion contentVersion = null;
            	while(contentVersionsIterator.hasNext())
            	{
            		contentVersion = (ContentVersion)contentVersionsIterator.next();
            		if(!isOldVersion(contentVersion, numberOfVersionsToKeep))
            			keep = false;
            	}
            	
            	if(contentVersion != null && keep)
            	{
            		if(contentVersion.getOwningContent() != null)
            		{
	            		String contentPath = ContentController.getContentController().getContentPath(contentVersion.getOwningContent().getId(), true, true);
	            		optimizationBeanList.addDigitalAsset(digitalAsset);
		            	optimizationBeanList.addEventVersions(toVOList(contentVersions));
		            	optimizationBeanList.setContentPath(digitalAsset.getId(), contentPath);
		            	i++;
            		}
            		else
            		{
            			logger.error("ContentVersion with id:" + contentVersion.getId() + " had no ownningcontent");
            		}
            	}
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
        
		return optimizationBeanList;
	}

	private boolean isOldVersion(ContentVersion contentVersion, int minNewerVersions)
	{
		Integer contentVersionId = contentVersion.getValueObject().getId();
		Integer language = contentVersion.getValueObject().getLanguageId();
		
		boolean isOldVersion = false;
		int numberOfNewerVersions = 0;
		
		Content content = contentVersion.getOwningContent();
		Collection contentVersions = content.getContentVersions();
		Iterator contentVersionsIterator = contentVersions.iterator();
		while(contentVersionsIterator.hasNext())
		{
			ContentVersion currentContentVersion = (ContentVersion)contentVersionsIterator.next();
			if(currentContentVersion.getValueObject().getLanguageId().intValue() == language.intValue())
			{
				if(currentContentVersion.getValueObject().getId().intValue() > contentVersionId.intValue())
				{
					numberOfNewerVersions++;
				}
			}
		}

		return numberOfNewerVersions >= minNewerVersions;
	}

	/**
	 * This method changes the language of a version.
	 * 
	 * @param contentVersionId
	 * @param languageId
	 * @throws Exception
	 */
	
	public void changeVersionLanguage(Integer contentVersionId, Integer languageId) throws Exception
	{
		Database db = CastorDatabaseService.getDatabase();
        
		beginTransaction(db);
        
		try
        {        
			ContentVersion contentVersion = this.getContentVersionWithId(contentVersionId, db);
	    	Language language = LanguageController.getController().getLanguageWithId(languageId, db);
			
			contentVersion.setLanguage((LanguageImpl)language);
			
			commitTransaction(db);            
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
	}
	

    /**
     * 
     * @param contentId a valid ContentId
     * @param languageId a valid languagId
     * @param db a db transaction
     * @return a List of contentVersion depending on parent and language id, if none found an empty List is returned.
     * @throws SystemException if an error occures.
     * @throws Bug if an error occures.
     * @throws Exception if an error occures.
     */
    public List<ContentVersionVO> getContentVersionsWithParentAndLanguage(final Integer contentId,
            final Integer languageId, final Database db) throws SystemException, Bug, Exception
    {
        final List<ContentVersion> resultList = new ArrayList<ContentVersion>();
        final OQLQuery oql = db.getOQLQuery("SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 and cv.language.languageId = $2 ORDER BY cv.contentVersionId desc");
        oql.bind(contentId);
        oql.bind(languageId);
        final QueryResults results = oql.execute();
        while (results.hasMore())
        {
            final ContentVersion contentVersion = (ContentVersion) results.next();
            resultList.add(contentVersion);
        }
        results.close();
        oql.close();
        return toVOList(resultList);
    }

    /**
     * Get a list of lates n contentVersions where the isActive equals true.
     * 
     * @param contentId a valid contentId.
     * @param languageId a valid languageId.
     * @param hitSize the number of contentversions to be retrieved.
     * @param db a db transaction
     * @return returns a empty list if none found. The list is ordered by latest contentVersionId.
     * @throws Exception if an error occurres.
     */
    @SuppressWarnings("unchecked")
    public List<ContentVersionVO> getLatestActiveContentVersionsForHitSize(final Integer contentId,
            final Integer languageId, final int hitSize, final Database db) throws Exception
    {
        if (hitSize == 0)
        {
            throw new SystemException("Illegal argument supplied, argument <hitSize> must be greater then zero.");
        }

        final List<ContentVersion> contentVersionList = new ArrayList<ContentVersion>();
        final OQLQuery oql = db.getOQLQuery("SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.owningContent.contentId = $1 AND cv.language.languageId = $2 AND cv.isActive = $3 ORDER BY cv.contentVersionId desc");
        oql.bind(contentId);
        oql.bind(languageId);
        oql.bind(true);
        final QueryResults results = oql.execute(Database.ReadOnly);
        int cnt = 0;
        while (results.hasMore() && ++cnt <= hitSize)
        {
            contentVersionList.add((ContentVersion) results.next());
        }
        results.close();
        oql.close();
        return toVOList(contentVersionList);
    }

    /**
     * Forces a delete of a specified contentVersion.
     * @param contentVersionVO a valid object.
     * @throws SystemException if an error occurres.
     */
    public void forceDelete(final ContentVersionVO contentVersionVO) throws SystemException {
        final Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
        try {
            final ContentVersion contentVersion = getContentVersionWithId(contentVersionVO.getContentVersionId(), db);
            contentCategoryController.deleteByContentVersion(contentVersion, db);
            final Content content = contentVersion.getOwningContent();
            if (content != null)
            {
                content.getContentVersions().remove(contentVersion);
            }
            db.remove(contentVersion);
            commitTransaction(db);
        }
        catch (Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }
    
	/**
	 * This method deletes the relation to a digital asset - not the asset itself.
	 */
	public ContentVersionVO checkStateAndChangeIfNeeded(Integer contentVersionId, InfoGluePrincipal principal) throws SystemException, Bug
    {
		ContentVersionVO resultingContentVersionVO = null;
			
    	Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);

        try
        {      
        	ContentVersion contentVersion = checkStateAndChangeIfNeeded(contentVersionId, principal, db);
    	    
    	    resultingContentVersionVO = contentVersion.getValueObject();
    	    
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
        return resultingContentVersionVO;
    }

	/**
	 * This method deletes the relation to a digital asset - not the asset itself.
	 */
	public ContentVersion checkStateAndChangeIfNeeded(Integer contentVersionId, InfoGluePrincipal principal, Database db) throws ConstraintException, SystemException, Bug
    {
    	ContentVersion contentVersion = null;
    	ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId, db);
    	if(!contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
		{
			List events = new ArrayList();
			contentVersion = ContentStateController.changeState(contentVersionVO.getId(), ContentVersionVO.WORKING_STATE, "new working version", false, null, principal, contentVersionVO.getContentId(), db, events);
		}
		else
		{
			contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionVO.getId(), db);
		}
    	            
        return contentVersion;
    }
	
	/**
	 * This method deletes the relation to a digital asset - not the asset itself.
	 */
	public DigitalAssetVO checkStateAndChangeIfNeeded(Integer contentVersionId, Integer digitalAssetId, InfoGluePrincipal principal, List<Integer> newContentVersionIdList) throws SystemException, Bug
    {
		DigitalAssetVO resultingDigitalAssetVO = null;
			
    	Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);

        try
        {      
        	ContentVersion contentVersion = null;
        	ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId, db);
        	DigitalAssetVO digitalAssetVO = DigitalAssetController.getController().getDigitalAssetVOWithId(digitalAssetId, db);
    	    if(!contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
			{
				List events = new ArrayList();
				contentVersion = ContentStateController.changeState(contentVersionVO.getId(), ContentVersionVO.WORKING_STATE, "new working version", false, null, principal, contentVersionVO.getContentId(), db, events);
				newContentVersionIdList.add(contentVersion.getId());
				digitalAssetVO = DigitalAssetController.getController().getLatestDigitalAssetVO(contentVersion.getId(), digitalAssetVO.getAssetKey(), db);
			}
    	    
    	    boolean duplicateAssetsBetweenVersions = CmsPropertyHandler.getDuplicateAssetsBetweenVersions();
        	logger.info("duplicateAssetsBetweenVersions:" + duplicateAssetsBetweenVersions);
    	    if(!duplicateAssetsBetweenVersions)
    	    {
    	    	DigitalAsset oldDigitalAsset = DigitalAssetController.getController().getDigitalAssetWithId(digitalAssetId, db);
    	    	logger.info("oldDigitalAsset:" + oldDigitalAsset.getContentVersions().size());
        	    if(oldDigitalAsset.getContentVersions().size() > 1)
    	    	{
        	    	logger.info("Creating new duplicate of this asset as there are other assets using this one:" + oldDigitalAsset.getId());
        	    	logger.info("contentVersion:" + contentVersion);
        	    	logger.info("oldDigitalAsset:" + oldDigitalAsset.getId());
	            	if(contentVersion == null)
	            		contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionId, db);
	            	
	    	    	digitalAssetVO = copyDigitalAssetAndRemoveOldReference(contentVersion, oldDigitalAsset, false, db);
	    	    	logger.info("new digitalAssetVO:" + digitalAssetVO.getId());
    	    	}
    	    }
        		
    	    resultingDigitalAssetVO = digitalAssetVO;
    	    
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        logger.info("resultingDigitalAssetVO:" + resultingDigitalAssetVO.getId());
        
        return resultingDigitalAssetVO;
    }


	/**
	 * This method deletes the relation to a digital asset - not the asset itself.
	 */
	public DigitalAssetVO checkStateAndChangeIfNeeded(Integer contentVersionId, Integer digitalAssetId, InfoGluePrincipal principal, Database db) throws ConstraintException, SystemException, Bug, Exception
    {
    	ContentVersion contentVersion = null;
    	ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId, db);
    	DigitalAssetVO digitalAssetVO = DigitalAssetController.getController().getDigitalAssetVOWithId(digitalAssetId, db);
	    if(!contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
		{
	    	List events = new ArrayList();
			contentVersion = ContentStateController.changeState(contentVersionVO.getId(), ContentVersionVO.WORKING_STATE, "new working version", false, null, principal, contentVersionVO.getContentId(), db, events);
			digitalAssetVO = DigitalAssetController.getController().getLatestDigitalAssetVO(contentVersion.getId(), digitalAssetVO.getAssetKey(), db);
		}
    	    
    	return digitalAssetVO;
    }

}
