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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.databeans.OptimizationBeanList;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.content.SmallestContentVersionVO;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.ExportContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.IndexFriendlyContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinition;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.GeneralOQLResult;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RegistryVO;
import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
import org.infoglue.cms.entities.management.impl.simple.RegistryImpl;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.DateHelper;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.NullObject;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.Timer;
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

    private final static VisualFormatter vf = new VisualFormatter();

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
    		ContentVersionVO ContentVersionVO = getSmallContentVersionVOWithId(contentVersionId, db);
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

    public ContentVersionVO getFullContentVersionVOWithId(Integer contentVersionId, Database db) throws SystemException, Bug
    {
		return (ContentVersionVO) getVOWithId(ContentVersionImpl.class, contentVersionId, db);
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

    public MediumContentVersionImpl getMediumContentVersionWithId(Integer contentVersionId, Database db) throws SystemException, Bug
    {
		return (MediumContentVersionImpl) getObjectWithId(MediumContentVersionImpl.class, contentVersionId, db);
    }

    public MediumContentVersionImpl getReadOnlyMediumContentVersionWithId(Integer contentVersionId, Database db) throws SystemException, Bug
    {
		return (MediumContentVersionImpl) getObjectWithIdAsReadOnly(MediumContentVersionImpl.class, contentVersionId, db);
    }

    public ContentVersion getReadOnlyContentVersionWithId(Integer contentVersionId, Database db) throws SystemException, Bug
    {
		return (ContentVersion) getObjectWithIdAsReadOnly(ContentVersionImpl.class, contentVersionId, db);
    }

    public SmallestContentVersionVO getSmallestContentVersionVOWithId(Integer contentVersionId, Database db) throws SystemException, Bug
    {
		return (SmallestContentVersionVO) getVOWithId(SmallestContentVersionImpl.class, contentVersionId, db);
    }

    /**
	 * This method gets the siteNodeVO with the given id
	 */
	 
    public ContentVersionVO getSmallContentVersionVOWithId(Integer contentVersionId, Database db) throws SystemException, Bug
    {
    	String key = "" + contentVersionId;
		ContentVersionVO contentVersionVO = (ContentVersionVO)CacheController.getCachedObjectFromAdvancedCache("contentVersionCache", key);
		if(contentVersionVO == null)
		{
			contentVersionVO = (ContentVersionVO)getVOWithId(SmallContentVersionImpl.class, contentVersionId, db);
			if(contentVersionVO != null)
				CacheController.cacheObjectInAdvancedCache("contentVersionCache", key, contentVersionVO);
		}
		
		return contentVersionVO;
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
		List childContentList = ContentController.getContentController().getContentChildrenVOList(contentId);
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
		List childContentList = ContentController.getContentController().getContentChildrenVOList(contentId);
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

	public List<ContentVersionVO> getLatestActiveContentVersionVOIfInState(Integer contentId, Integer stateId, Database db) throws SystemException, Exception
	{
		List<ContentVersionVO> resultList = new ArrayList<ContentVersionVO>();
	    Map lastLanguageVersions = new HashMap();
	    Map languageVersions = new HashMap();
	    
	    //TODO - fix
	    Content content = ContentController.getContentController().getContentWithId(contentId, db);
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
			    resultList.add(contentVersion.getValueObject());
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
    
   	public ContentVersionVO getLatestActiveContentVersionVO(Integer contentId) throws SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();
    	ContentVersionVO contentVersionVO = null;

        beginTransaction(db);

        try
        {
        	contentVersionVO = getLatestActiveContentVersionVO(contentId, db);
            
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
    
   	public ContentVersionVO getLatestActiveContentVersionVO(Integer contentId, Integer languageId, Integer stateId) throws SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();
    	ContentVersionVO contentVersionVO = null;

        beginTransaction(db);

        try
        {
        	contentVersionVO = getLatestActiveContentVersionVO(contentId, languageId, stateId, db);
            
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
		Timer t = new Timer();
		
    	ContentVersionVO contentVersionVO = null;
    	
	    String versionKey = "" + contentId + "_" + languageId + "_" + stateId + "_contentVersionVO";

		Object object = CacheController.getCachedObjectFromAdvancedCache("contentVersionCache", versionKey);
		if(object instanceof NullObject)
		{
			logger.info("There was an cached contentVersionVO but it was null:" + object);
		}
		else if(object != null)
		{
			if(object instanceof SmallestContentVersionVO)
			{
				logger.warn("Object was instanceof SmallestContentVersionVO for key:" + versionKey);
				contentVersionVO = (ContentVersionVO)getVOWithId(SmallContentVersionImpl.class, ((SmallestContentVersionVO)object).getId(), db);
				CacheController.cacheObjectInAdvancedCache("contentVersionCache", versionKey, contentVersionVO, new String[]{CacheController.getPooledString(2, contentVersionVO.getId()), CacheController.getPooledString(1, contentVersionVO.getContentId())}, true);
			}
			else
			{
				contentVersionVO = (ContentVersionVO)object;
			}
		}
		else
		{
			OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl cv WHERE cv.contentId = $1 AND cv.languageId = $2 AND cv.stateId >= $3 AND cv.isActive = $4 ORDER BY cv.contentVersionId desc");
	    	oql.bind(contentId);
	    	oql.bind(languageId);
	    	oql.bind(stateId);
	    	oql.bind(true);
	
	    	QueryResults results = oql.execute(Database.ReadOnly);
	    	
			if (results.hasMore()) 
	        {
				ContentVersion contentVersion = (ContentVersion)results.next();
	        	contentVersionVO = contentVersion.getValueObject();
	
				CacheController.cacheObjectInAdvancedCache("contentVersionCache", versionKey, contentVersionVO, new String[]{CacheController.getPooledString(2, contentVersionVO.getId()), CacheController.getPooledString(1, contentVersionVO.getContentId())}, true);
	        }
			/*
	       	ContentVersion contentVersion = getLatestActiveContentVersionReadOnly(contentId, languageId, stateId, db);
	            
	        if(contentVersion != null)
	            contentVersionVO = contentVersion.getValueObject();
	    	*/
		}
		
        RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getLatestActiveContentVersionVO new", t.getElapsedTime());

		return contentVersionVO;
    }


  	/**
	 * This method returns the latest active content version.
	 */
    
	public ContentVersionVO getLatestActiveContentVersionVO(Integer contentId, Database db) throws SystemException, Bug, Exception
	{
		ContentVersionVO contentVersionVO = null;
		
        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl cv WHERE cv.contentId = $1 AND cv.isActive = $2 ORDER BY cv.contentVersionId desc");
    	oql.bind(contentId);
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
    
	public ContentVersionVO getLatestActiveContentVersionVO(Integer contentId, Integer languageId, Database db) throws SystemException, Bug, Exception
	{
		String contentVersionKey = "contentVersionVO_" + contentId + "_" + languageId + "_active";
		ContentVersionVO contentVersionVO = (ContentVersionVO)CacheController.getCachedObjectFromAdvancedCache("contentVersionCache", contentVersionKey);
		if(contentVersionVO != null)
		{
			//logger.info("There was an cached contentVersionVO:" + contentVersionVO.getContentVersionId());
		}
		else
		{
			if(logger.isInfoEnabled())
				logger.info("Querying for contentVersionVO:" + contentVersionKey);

			//ContentVersionVO contentVersionVO = null;
	
	        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl cv WHERE cv.contentId = $1 AND cv.languageId = $2 AND cv.isActive = $3 ORDER BY cv.contentVersionId desc");
	    	oql.bind(contentId);
	    	oql.bind(languageId);
			oql.bind(true);
	    	
	    	QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
	        {
				ContentVersion contentVersion = (ContentVersion)results.next();
				contentVersionVO = contentVersion.getValueObject();
				
				if(contentVersionVO != null)
				{
					CacheController.cacheObjectInAdvancedCache("contentVersionCache", contentVersionKey, contentVersionVO, new String[]{CacheController.getPooledString(2, contentVersionVO.getId()), CacheController.getPooledString(1, contentVersionVO.getContentId())}, true);
				}
	        }
			
			results.close();
			oql.close();
		}

		return contentVersionVO;
	}

   	/**
	 * This method returns the latest active content version.
	 */
    
   	public List<ContentVersionVO> getContentVersionVOList(Integer contentId) throws SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();
    	List<ContentVersionVO> contentVersionVOList = new ArrayList<ContentVersionVO>();

        beginTransaction(db);

        try
        {
        	contentVersionVOList = getContentVersionVOList(contentId, db);
            
            rollbackTransaction(db);
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
   	
   	/**
	 * This method returns the latest active content version.
	 */
    
	public List<ContentVersionVO> getContentVersionVOList(Integer contentId, Database db) throws SystemException, Bug, Exception
	{
		List<ContentVersionVO> contentVersionVOList = new ArrayList<ContentVersionVO>();

        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl cv WHERE cv.contentId = $1 AND cv.isActive = $2 ORDER BY cv.contentVersionId");
    	oql.bind(contentId);
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

   	/**
	 * This method returns the latest active content version.
	 */
    
	public List<Integer> getLatestContentVersionIdsPerLanguage(Integer contentId, Integer stateId, Database db) throws SystemException, Bug, Exception
	{
		List<Integer> contentVersionIdList = new ArrayList<Integer>();
		
		StringBuilder sb = new StringBuilder();
		if(CmsPropertyHandler.getUseShortTableNames().equals("true"))
		{
			sb.append("select max(cv.contVerId) AS id, stateId as column1Value, languageId as column2Value, '' as column3Value, '' as column4Value, '' as column5Value, '' as column6Value, '' as column7Value from cmContVer cv where cv.contId = $1 AND cv.isActive = $2 group by cv.languageId, cv.stateId ");
		}
		else
		{
			sb.append("select max(cv.contentVersionId)  AS id, stateId as column1Value, languageId as column2Value, '' as column3Value, '' as column4Value, '' as column5Value, '' as column6Value, '' as column7Value from cmContentVersion cv where cv.contentId = $1 AND cv.isActive = $2 group by cv.languageId, cv.stateId ");
		}
		OQLQuery oql = db.getOQLQuery("CALL SQL " + sb.toString() + "AS org.infoglue.cms.entities.management.GeneralOQLResult");

    	oql.bind(contentId);
		oql.bind(true);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		while (results.hasMore()) 
        {
			GeneralOQLResult resultBean = (GeneralOQLResult)results.next();
			Integer versionStateId = new Integer(resultBean.getValue1());
			if(resultBean.getId() != null && resultBean.getValue1() != null && versionStateId.equals(stateId))
				contentVersionIdList.add(resultBean.getId());
		}
		
		results.close();
		oql.close();

		return contentVersionIdList;
	}

   	/**
	 * This method returns the latest active content version.
	 */

	public List<ContentVersionVO> getLatestContentVersionVOListPerLanguage(Integer contentId, Database db) throws SystemException, Bug, Exception
	{
		Set<Integer> contentIds = new HashSet<Integer>();
		contentIds.add(contentId);
		
		return getLatestContentVersionVOListPerLanguage(contentIds, db);
	}
	
   	/**
	 * This method returns the latest active content version.
	 */

	public List<ContentVersionVO> getLatestContentVersionVOListPerLanguage(Set<Integer> contentIds/*, Integer stateId*/, Database db) throws SystemException, Bug, Exception
	{
		List<ContentVersionVO> contentVersionIdSet = new ArrayList<ContentVersionVO>();
		if(contentIds == null || contentIds.size() == 0)
			return contentVersionIdSet;
		
		List contentVersionVOListSubList = new ArrayList();
		contentVersionVOListSubList.addAll(contentIds);

    	int slotSize = 500;
    	if(contentVersionVOListSubList.size() > 0)
    	{
    		List<Integer> subList = new ArrayList(contentVersionVOListSubList);
    		if(contentVersionVOListSubList.size() > slotSize)
    			subList = contentVersionVOListSubList.subList(0, slotSize);
	    	while(subList != null && subList.size() > 0)
	    	{
	    		contentVersionIdSet.addAll(getLatestContentVersionIdsPerLanguageImpl(subList, db));
	    		contentVersionVOListSubList = contentVersionVOListSubList.subList(subList.size(), contentVersionVOListSubList.size());
	    	
	    		subList = new ArrayList(contentVersionVOListSubList);
	    		if(contentVersionVOListSubList.size() > slotSize)
	    			subList = contentVersionVOListSubList.subList(0, slotSize);
	    	}
    	}
		
		return contentVersionIdSet;
	}
	
	public List<ContentVersionVO> getLatestContentVersionIdsPerLanguageImpl(List<Integer> contentIds, Database db) throws SystemException, Bug, Exception
	{
		List<ContentVersionVO> contentVersionIdSet = new ArrayList<ContentVersionVO>();
		if(contentIds == null || contentIds.size() == 0)
			return contentVersionIdSet;
		
		List<String> contentHandled = new ArrayList<String>();
		
		StringBuilder variables = new StringBuilder();
		for(int i=0; i<contentIds.size(); i++)
	    	variables.append("?" + (i+1!=contentIds.size() ? "," : ""));
	    
		StringBuilder sb = new StringBuilder();
		if(CmsPropertyHandler.getUseShortTableNames().equals("true"))
		{
			sb.append("select max(cv.contVerId) AS id, cv.contId as column1Value, cv.stateId as column2Value, cv.languageId as column3Value, c.repositoryId as column4Value, c.contentTypeDefId as column5Value, max(cv.versionModifier) as column6Value, max(cv.modifiedDateTime) as column7Value from cmContVer cv, cmCont c where cv.isActive = 1 AND c.contId = cv.contId AND cv.contId IN (" + variables + ") group by cv.contId, cv.languageId, cv.stateId, c.repositoryId, c.contentTypeDefId order by ID desc ");
		}
		else
		{
			sb.append("select max(cv.contentVersionId) AS id, cv.contentId as column1Value, cv.stateId as column2Value, cv.languageId as column3Value, c.repositoryId as column4Value, c.contentTypeDefinitionId as column5Value, max(cv.versionModifier) as column6Value, max(cv.modifiedDateTime) as column7Value from cmContentVersion cv, cmContent c where cv.isActive = 1 AND c.contentId = cv.contentId AND cv.contentId IN (" + variables + ") group by cv.contentId, cv.languageId, cv.stateId, c.repositoryId, c.contentTypeDefinitionId order by ID desc ");
		}

		Connection conn = (Connection) db.getJdbcConnection();
		
		PreparedStatement psmt = conn.prepareStatement(sb.toString());
		int i=1;
    	for(Integer entityId : contentIds)
    	{
    		psmt.setInt(i, entityId);
    		i++;
    	}

		ResultSet rs = psmt.executeQuery();
		while(rs.next())
		{
			Integer id = new Integer(rs.getString(1));
			Integer contentId = new Integer(rs.getString(2));
			Integer versionStateId = new Integer(rs.getString(3));
			Integer languageId = new Integer(rs.getString(4));
			Integer repositoryId = new Integer(rs.getString(5));
			Integer contentTypeDefinitionId = null;
			if(rs.getString(6) != null && !rs.getString(6).equals(""))
				contentTypeDefinitionId = new Integer(rs.getString(6));
			String versionModifier = rs.getString(7);
			String modifiedDateTime = rs.getString(8);
			if(id != null && contentId != null && !contentHandled.contains(contentId + "_" + languageId))
			{
				ContentVersionVO cv = new ContentVersionVO();
				cv.setContentId(contentId);
				cv.setLanguageId(languageId);
				cv.setStateId(versionStateId);
				cv.setContentVersionId(id);
				cv.setRepositoryId(repositoryId);
				if(contentTypeDefinitionId != null)
					cv.setContentTypeDefinitionId(contentTypeDefinitionId);
				cv.setVersionModifier(versionModifier);
				cv.setModifiedDateTime(vf.parseDate(modifiedDateTime, "yyyy-MM-dd HH:mm:ss"));
				
				contentVersionIdSet.add(cv);
				contentHandled.add(contentId + "_" + languageId);
			}
		}
		rs.close();
		psmt.close();
		
		return contentVersionIdSet;
	}
	

   	/**
	 * This method returns selected active content versions.
	 */
    
	public List<ExportContentVersionImpl> getContentVersionList(Integer repositoryId, Integer minimumId, Integer limit, Boolean onlyPublishedVersions, Database db) throws SystemException, Bug, Exception
	{
		List<ExportContentVersionImpl> contentVersionList = new ArrayList<ExportContentVersionImpl>();

        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ExportContentVersionImpl cv WHERE cv.owningContent.repositoryId = $1 AND cv.isActive = $2 AND cv.contentVersionId > $3 " + (onlyPublishedVersions ? " AND cv.stateId = 3 " : "") + " ORDER BY cv.contentVersionId LIMIT $4");
    	oql.bind(repositoryId);
		oql.bind(true);
		oql.bind(minimumId);
		oql.bind(limit);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		while (results.hasMore()) 
        {
			ExportContentVersionImpl contentVersion = (ExportContentVersionImpl)results.next();
			contentVersionList.add(contentVersion);
        }
		
		results.close();
		oql.close();

		return contentVersionList;
	}
	
   	/**
	 * This method returns the latest active content version.
	 */
    
	public ContentVersion getLatestActiveContentVersion(Integer contentId, Integer languageId, Database db) throws SystemException, Bug, Exception
	{
		ContentVersion contentVersion = null;
		
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
    
	public ContentVersion getLatestActiveMediumContentVersion(Integer contentId, Integer languageId, Database db) throws SystemException, Bug, Exception
	{
		ContentVersion contentVersion = null;
		
		Content content = ContentController.getContentController().getMediumContentWithId(contentId, db);
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
				if(currentContentVersion.getIsActive().booleanValue() &&  currentContentVersion.getValueObject().getLanguageId().intValue() == languageId.intValue())
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
		Timer t = new Timer();
		
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
        
        RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getLatestActiveContentVersionReadOnly", t.getElapsedTime());
        
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
		String contentVersionKey = "contentVersionVO_" + contentId + "_" + languageId;
		ContentVersionVO contentVersionVO = (ContentVersionVO)CacheController.getCachedObjectFromAdvancedCache("contentVersionCache", contentVersionKey);
		if(contentVersionVO != null)
		{
			//logger.info("There was an cached contentVersionVO:" + contentVersionVO.getContentVersionId());
		}
		else
		{
			if(logger.isInfoEnabled())
				logger.info("Querying for contentVersionVO:" + contentVersionKey);

	        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl cv WHERE cv.contentId = $1 AND cv.languageId = $2 ORDER BY cv.contentVersionId desc");
	    	oql.bind(contentId);
	    	oql.bind(languageId);
	    	
	    	QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
	        {
				ContentVersion contentVersion = (ContentVersion)results.next();
				contentVersionVO = contentVersion.getValueObject();
				
				if(contentVersionVO != null)
				{
					CacheController.cacheObjectInAdvancedCache("contentVersionCache", contentVersionKey, contentVersionVO, new String[]{CacheController.getPooledString(2, contentVersionVO.getId()), CacheController.getPooledString(1, contentVersionVO.getContentId())}, true);
				}
	        }
			
			results.close();
			oql.close();    	
        }
		
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
	 * This method created a new contentVersion in the database. It also updates the owning content
	 * so it recognises the change. 
	 */
   
    public MediumContentVersionImpl create(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, Integer oldContentVersionId, Database db) throws ConstraintException, SystemException, Exception
    {
    	return create(contentId, languageId, contentVersionVO, oldContentVersionId, true, true, null, db);
    }


    /**
	 * This method created a new contentVersion in the database.
	 */
	
    public ContentVersionVO create(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, Integer oldContentVersionId, boolean allowBrokenAssets, boolean duplicateAssets) throws ConstraintException, SystemException
    {
		Database db = CastorDatabaseService.getDatabase();
        MediumContentVersionImpl contentVersion = null;

        beginTransaction(db);
		try
        {
			contentVersion = create(contentId, languageId, contentVersionVO, oldContentVersionId, allowBrokenAssets, duplicateAssets, null, db);
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
    /*
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
    */    
    
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
    
    public MediumContentVersionImpl create(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, Integer oldContentVersionId, boolean allowBrokenAssets, boolean duplicateAssets, Integer excludedAssetId, Database db) throws ConstraintException, SystemException, Exception
    {
    	Timer t = new Timer();		
    	MediumContentVersionImpl contentVersion = new MediumContentVersionImpl();
		contentVersion.setValueObject(contentVersionVO);
		contentVersion.getValueObject().setLanguageId(languageId);
		contentVersion.getValueObject().setContentId(contentId);
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("create 2", t.getElapsedTime());
		
		db.create(contentVersion); 
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("create 3.0", t.getElapsedTime());

        //content.getContentVersions().add(contentVersion);

        if(oldContentVersionId != null && oldContentVersionId.intValue() != -1)
		    copyDigitalAssets(getMediumContentVersionWithId(oldContentVersionId, db), contentVersion, allowBrokenAssets, duplicateAssets, excludedAssetId, db);

        return contentVersion;
    }     

    public MediumContentVersionImpl createMedium(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, Database db) throws ConstraintException, SystemException, Exception
    {
    	//Beh�vs verkligen content h�r? M�t tiderna ocks�
    	Timer t = new Timer();		
    	MediumContentVersionImpl contentVersion = new MediumContentVersionImpl();
		contentVersion.setValueObject(contentVersionVO);
		contentVersion.getValueObject().setLanguageId(languageId);
		contentVersion.getValueObject().setContentId(contentId);
		
		db.create(contentVersion); 
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("create 3.0", t.getElapsedTime());

        return contentVersion;
    }     
    
    public MediumContentVersionImpl createMedium(MediumContentVersionImpl oldContentVersion, Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, Integer oldContentVersionId, boolean hasAssets, boolean allowBrokenAssets, boolean duplicateAssets, Integer excludedAssetId, Database db) throws ConstraintException, SystemException, Exception
    {
    	//Beh�vs verkligen content h�r? M�t tiderna ocks�
    	Timer t = new Timer();		
    	MediumContentVersionImpl contentVersion = new MediumContentVersionImpl();
		contentVersion.setValueObject(contentVersionVO);
		contentVersion.getValueObject().setLanguageId(languageId);
		contentVersion.getValueObject().setContentId(contentId);
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("create 2", t.getElapsedTime());
		
		db.create(contentVersion); 
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("create 3.1", t.getElapsedTime());

        if(oldContentVersionId != null && oldContentVersionId.intValue() != -1 && hasAssets)
		    copyDigitalAssets(oldContentVersion, contentVersion, allowBrokenAssets, duplicateAssets, excludedAssetId, db);

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
	 * This method deletes an contentversion and notifies the owning content.
	 */
	
 	public void delete(MediumContentVersionImpl contentVersion, Database db, boolean forceDelete) throws ConstraintException, SystemException, Exception
	{
		if (!forceDelete && contentVersion.getStateId().intValue() == ContentVersionVO.PUBLISHED_STATE.intValue() && contentVersion.getIsActive().booleanValue() == true)
		{
			throw new ConstraintException("ContentVersion.stateId", "3300", contentVersion.getOwningContent().getName());
		}
		
		contentCategoryController.deleteByContentVersion(contentVersion, db);
		DigitalAssetController.getController().deleteByContentVersion(contentVersion, db);
		//System.out.println("Removing:" + contentVersion.getId());
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
		    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, true, infogluePrincipal, db, true);
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
		    ContentVersionVO contentVersionVO = this.getLatestActiveContentVersionVO(contentId, languageId, db);
	    	ContentVersion contentVersion = getContentVersionWithId(contentVersionVO.getId(), db);

		    Collection digitalAssets = contentVersion.getDigitalAssets();
			Iterator assetIterator = digitalAssets.iterator();
			while(assetIterator.hasNext())
			{
				DigitalAsset currentDigitalAsset = (DigitalAsset)assetIterator.next();
				if(currentDigitalAsset.getAssetKey().equals(assetKey))
				{
		            currentDigitalAsset.getContentVersions().remove(contentVersion);
					assetIterator.remove();
		            if(currentDigitalAsset.getContentVersions().size() == 0)
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
	 * This method updates a digitalAsset.
	 */
	
    public void createOrUpdateDigitalAsset(DigitalAssetVO assetVO, InputStream is, Integer contentVersionId, InfoGluePrincipal principal) throws ConstraintException, SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {
			MediumContentVersionImpl contentVersion = this.getMediumContentVersionWithId(contentVersionId, db);
		    
		    boolean foundExistingAsset = false;
		    
		    Collection digitalAssets = contentVersion.getDigitalAssets();
			Iterator assetIterator = digitalAssets.iterator();
			while(assetIterator.hasNext())
			{
				DigitalAsset currentDigitalAsset = (DigitalAsset)assetIterator.next();
				if(currentDigitalAsset.getAssetKey().equals(assetVO.getAssetKey()))
				{
					assetVO.setDigitalAssetId(currentDigitalAsset.getId());
					DigitalAssetController.update(assetVO, is, db);
					foundExistingAsset = true;
					break;
				}
			}
			
			if(!foundExistingAsset)
				DigitalAssetController.create(assetVO, is, contentVersion, db);
			
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
     * @throws Exception
     */

    public ContentVersionVO update(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, Database db) throws Exception
    {
		return update(contentId, languageId, contentVersionVO, (InfoGluePrincipal)null, false, db);
    }

    public ContentVersionVO update(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO) throws ConstraintException, SystemException
    {
    	return update(contentId, languageId, contentVersionVO, (InfoGluePrincipal)null);
    }

    public ContentVersionVO update(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, InfoGluePrincipal principal) throws ConstraintException, SystemException
    {
    	return update(contentId, languageId, contentVersionVO, principal, false);
    }

    public ContentVersionVO update(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, InfoGluePrincipal principal, boolean skipValidate) throws ConstraintException, SystemException
    {
//    	Timer t = new Timer();
        ContentVersionVO updatedContentVersionVO;

        Database db = CastorDatabaseService.getDatabase();

        beginTransaction(db);

        try
        {
        	updatedContentVersionVO = update(contentId, languageId, contentVersionVO, principal, skipValidate, db);
	    	commitRegistryAwareTransaction(db);
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

    public ContentVersionVO update(Integer contentId, Integer languageId, ContentVersionVO contentVersionVO, InfoGluePrincipal principal, boolean skipValidate, Database db) throws Exception
    {
    	ContentVersionVO updatedContentVersionVO;

    	ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId, db);
    	ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVO.getContentTypeDefinitionId(), db);
    	ConstraintExceptionBuffer ceb = contentVersionVO.validateAdvanced(contentTypeDefinitionVO);
        logger.info("Skipping validate:" + skipValidate);
    	if(!skipValidate)
    		ceb.throwIfNotEmpty();

    	MediumContentVersionImpl contentVersion = null;
        Integer contentVersionIdToUpdate = contentVersionVO.getId();

		contentVersionVO.setModifiedDateTime(new Date());
        if(contentVersionVO.getId() == null)
    	{
    		logger.info("Creating the entity because there was no version at all for: " + contentId + " " + languageId);
    		contentVersion = createMedium(contentId, languageId, contentVersionVO, db);
    	}
    	else
    	{
			ContentVersionVO oldContentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionVO.getId(), db);
			ContentVersionVO latestContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(oldContentVersionVO.getContentId(), oldContentVersionVO.getLanguageId(), db);
    	    if(!oldContentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE) && !latestContentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
			{
				List<EventVO> events = new ArrayList<EventVO>();
				contentVersion = ContentStateController.changeState(oldContentVersionVO.getId(), contentVO, ContentVersionVO.WORKING_STATE, (contentVersionVO.getVersionComment().equals("No comment") ? "new working version" : contentVersionVO.getVersionComment()), false, null, principal, oldContentVersionVO.getContentId(), db, events);
				contentVersion.setVersionValue(contentVersionVO.getVersionValue());
				/*
				List<String> changedAttributes = getChangedAttributeNames(oldContentVersionVO, contentVersionVO);
				System.out.println("changedAttributes in contentversioncontroller:" + changedAttributes);
	    	    Map extraInfoMap = new HashMap();
	    	    String csList = StringUtils.join(changedAttributes.toArray(), ",");
	    	    extraInfoMap.put("changedAttributeNames", csList);
	    		CacheController.setExtraInfo(ContentVersionImpl.class.getName(), contentVersion.getId().toString(), extraInfoMap);
				*/
			}
			else
			{
				if(latestContentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
				{
					oldContentVersionVO = contentVersionVO;
					contentVersionIdToUpdate = latestContentVersionVO.getId();
				}

		    	List<String> changedAttributes = getChangedAttributeNames(oldContentVersionVO, contentVersionVO);
	    	    Map<String,String> extraInfoMap = new HashMap<String,String>();
	    	    String csList = StringUtils.join(changedAttributes.toArray(), ",");
	    	    //logger.info("csList:" + csList);
	    	    extraInfoMap.put("changedAttributeNames", csList);
	    		CacheController.setExtraInfo(ContentVersionImpl.class.getName(), contentVersionVO.getId().toString(), extraInfoMap);
				contentVersion = ContentVersionController.getContentVersionController().getMediumContentVersionWithId(contentVersionIdToUpdate, db);
				//contentVersionVO.setModifiedDateTime(DateHelper.getSecondPreciseDate());
				Integer existingContentId = contentVersion.getValueObject().getContentId();
				contentVersionVO.setContentId(existingContentId);
				contentVersionVO.setLanguageId(contentVersion.getValueObject().getLanguageId());
				contentVersion.setValueObject(contentVersionVO);
				contentVersion.setContentVersionId(contentVersionIdToUpdate);
				contentVersion.setStateId(ContentVersionVO.WORKING_STATE);
			}
    	}

        SiteNodeVersionVO latestSiteNodeVersionVO = null;
	    if(principal != null && contentTypeDefinitionVO.getName().equalsIgnoreCase("Meta info"))
	    {
    	    SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithMetaInfoContentId(db, contentId);
	    	//SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithMetaInfoContentId(db, contentId);
			if(siteNodeVO.getMetaInfoContentId() != null && siteNodeVO.getMetaInfoContentId().equals(contentId))
			{
		    	latestSiteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(db, siteNodeVO.getId());
		    	latestSiteNodeVersionVO.setVersionModifier(contentVersionVO.getVersionModifier());
		    	latestSiteNodeVersionVO.setModifiedDateTime(DateHelper.getSecondPreciseDate());
				SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().acUpdate(principal, latestSiteNodeVersionVO, db);

				Map extraInfoMap = new HashMap();
			    extraInfoMap.put("skipSiteNodeVersionUpdate", "true");
//	    	    extraInfoMap.put("contentId", ""+contentVO.getId());
//	    	    extraInfoMap.put("parentContentId", ""+contentVO.getParentContentId());
//	    	    extraInfoMap.put("repositoryId", ""+contentVO.getRepositoryId());
				CacheController.setExtraInfo(SiteNodeVersionImpl.class.getName(), latestSiteNodeVersionVO.getId().toString(), extraInfoMap);
			}
		}

    	registryController.updateContentVersionThreaded(contentVersion.getValueObject(), latestSiteNodeVersionVO);

    	updatedContentVersionVO = contentVersion.getValueObject();
    	return updatedContentVersionVO;
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
            ContentVersion contentVersion = getMediumContentVersionWithId(contentVersionId, db);
           
            ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersion.getValueObject().getContentId(), db);
        	ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVO.getContentTypeDefinitionId(), db);

        	contentVersion.setValueObject(contentVersionVO);
        	contentVersion.getValueObject().setContentId(contentVO.getContentId());

        	SiteNodeVersion latestSiteNodeVersion = null;
		    if(principal != null && contentTypeDefinitionVO.getName().equalsIgnoreCase("Meta info"))
		    {
		    	SiteNodeVO siteNode = SiteNodeController.getController().getSiteNodeVOWithMetaInfoContentId(db, contentVO.getContentId());
				if(siteNode.getMetaInfoContentId() != null && siteNode.getMetaInfoContentId().equals(contentVO.getContentId()))
				{
			    	latestSiteNodeVersion = SiteNodeVersionController.getController().getLatestMediumSiteNodeVersion(db, siteNode.getId(), false);
			    	latestSiteNodeVersion.setVersionModifier(contentVersionVO.getVersionModifier());
			    	latestSiteNodeVersion.setModifiedDateTime(DateHelper.getSecondPreciseDate());
					SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().acUpdate(principal, latestSiteNodeVersion.getValueObject(), db);
				}
			}
		    
	    	registryController.updateContentVersionThreaded(contentVersion.getValueObject(), latestSiteNodeVersion == null ? null : latestSiteNodeVersion.getValueObject());
			   
	    	updatedContentVersionVO = contentVersion.getValueObject();
	    	
	    	commitRegistryAwareTransaction(db);  
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

	public List<SmallestContentVersionVO> getPublishedActiveContentVersionVOList(List<Integer> contentIds, Database db) throws SystemException, Bug, Exception
    {
		List<SmallestContentVersionVO> contentVersionIdSet = new ArrayList<SmallestContentVersionVO>();
		if(contentIds == null || contentIds.size() == 0)
			return contentVersionIdSet;
		
		List contentVersionVOListSubList = new ArrayList();
		contentVersionVOListSubList.addAll(contentIds);
	
		int slotSize = 500;
		if(contentVersionVOListSubList.size() > 0)
		{
			List<Integer> subList = new ArrayList(contentVersionVOListSubList);
			if(contentVersionVOListSubList.size() > slotSize)
				subList = contentVersionVOListSubList.subList(0, slotSize);
	    	while(subList != null && subList.size() > 0)
	    	{
	    		contentVersionIdSet.addAll(getPublishedActiveContentVersionVOListBatch(subList, db));
	    		contentVersionVOListSubList = contentVersionVOListSubList.subList(subList.size(), contentVersionVOListSubList.size());
	    	
	    		subList = new ArrayList(contentVersionVOListSubList);
	    		if(contentVersionVOListSubList.size() > slotSize)
	    			subList = contentVersionVOListSubList.subList(0, slotSize);
	    	}
		}
		
		return contentVersionIdSet;
	}
	
	public List<SmallestContentVersionVO> getPublishedActiveContentVersionVOListBatch(List<Integer> contentIds, Database db) throws SystemException, Bug, Exception
    {
		List<SmallestContentVersionVO> contentVersionVOList = new ArrayList<SmallestContentVersionVO>();
		if(contentIds == null || contentIds.size() == 0)
			return contentVersionVOList;
		
		List<String> contentHandled = new ArrayList<String>();
		
		StringBuilder variables = new StringBuilder();
	    for(int i=0; i<contentIds.size(); i++)
	    	variables.append("$" + (i+3) + (i+1!=contentIds.size() ? "," : ""));
		
	    //System.out.println("siteNodeIds:" + siteNodeIds.length);
	    //System.out.println("variables:" + variables);

   		StringBuffer SQL = new StringBuffer();

    	OQLQuery oql = db.getOQLQuery("CALL SQL select cv.contentVersionId, cv.stateId, cv.modifiedDateTime, cv.versionComment, cv.isCheckedOut, cv.isActive, cv.contentId, cv.languageId, cv.versionModifier FROM cmContentVersion cv where cv.isActive = $1 AND  cv.stateId = $2 AND cv.contentId IN (" + variables + ") AS org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl");
    	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
    		oql = db.getOQLQuery("CALL SQL select cv.contVerId, cv.stateId, cv.modifiedDateTime, cv.verComment, cv.isCheckedOut, cv.isActive, cv.contId, cv.languageId, cv.versionModifier FROM cmContVer cv where cv.isActive = $1 AND cv.stateId = $2 AND cv.contId IN (" + variables + ") AS org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl");
	
    	//System.out.println("SQL:" + SQL);
    	//logger.info("SQL:" + SQL);
    	//logger.info("parentSiteNodeId:" + parentSiteNodeId);
    	//logger.info("showDeletedItems:" + showDeletedItems);
    	//OQLQuery oql = db.getOQLQuery(SQL.toString());
		oql.bind(true);
		oql.bind(ContentVersionVO.PUBLISHED_STATE);
		for(Integer entityId : contentIds)
			oql.bind(entityId);

		QueryResults results = oql.execute(Database.ReadOnly);
		while (results.hasMore()) 
		{
			SmallestContentVersionImpl contentVersion = (SmallestContentVersionImpl)results.next();
			contentVersionVOList.add(contentVersion.getValueObject());
		}

		results.close();
		oql.close();

		return contentVersionVOList;
	}
	
	public List getPublishedActiveContentVersionVOList(Integer contentId, Database db) throws SystemException, Bug, Exception
    {
        List contentVersionVOList = new ArrayList();
        
        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl cv WHERE cv.contentId = $1 AND cv.stateId = $2 AND cv.isActive = $3 ORDER BY cv.contentVersionId desc");
    	oql.bind(contentId);
    	oql.bind(ContentVersionVO.PUBLISHED_STATE);
    	oql.bind(true);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		while (results.hasMore()) 
        {
        	SmallContentVersionImpl contentVersion = (SmallContentVersionImpl)results.next();
        	contentVersionVOList.add(contentVersion.getValueObject());
        }
		
		results.close();
		oql.close();
            
		return contentVersionVOList;
    }

	public ContentVersionVO getLatestPublishedContentVersionVO(Integer contentId) throws SystemException, Bug, Exception
    {
        ContentVersionVO contentVersionVO = null;
        
        Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
        try
        {        
	        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl cv WHERE cv.contentId = $1 AND cv.stateId = $2 AND cv.isActive = $3 ORDER BY cv.contentVersionId desc");
	    	oql.bind(contentId);
	    	oql.bind(ContentVersionVO.PUBLISHED_STATE);
	    	oql.bind(true);
	    	
	    	QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
	        {
				SmallContentVersionImpl contentVersion = (SmallContentVersionImpl)results.next();
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
		    	ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId(), db);
		    	contentVersion = ContentStateController.changeState(contentVersionVO.getId(), contentVO, ContentVersionVO.WORKING_STATE, "new working version", false, null, principal, contentVersionVO.getContentId(), db, events, digitalAssetId);
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
	public void copyDigitalAssets(MediumContentVersionImpl originalContentVersion, MediumContentVersionImpl newContentVersion, boolean allowBrokenAssets, boolean duplicateAssets, Integer excludedAssetId, Database db) throws ConstraintException, SystemException, Exception
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

	public DigitalAssetVO copyDigitalAssetAndRemoveOldReference(MediumContentVersionImpl contentVersion, DigitalAsset digitalAsset, boolean allowBrokenAssets, Database db) throws ConstraintException, SystemException, Exception
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
	 * Returns an attribute value from the ContentVersionVO
	 *
	 * @param contentVersionVO The version on which to find the value
	 * @param attributeName THe name of the attribute whose value is wanted
	 * @param escapeHTML A boolean indicating if the result should be escaped
	 * @return The String vlaue of the attribute, or blank if it doe snot exist.
	 */
	public List<String> getChangedAttributeNames(ContentVersionVO contentVersionVO1, ContentVersionVO contentVersionVO2)
	{
		List<String> changes = new ArrayList<String>();
		try
		{
			String xml = contentVersionVO1.getVersionValue();
	
			int attributesStartTagIndex = xml.indexOf("<attributes>");
			int attributesStopTagIndex = xml.lastIndexOf("</attributes>");
			
			String attributes = xml.substring(attributesStartTagIndex+12, attributesStopTagIndex);
			
			//logger.info("attributes1:" + attributes);
			
			int loop = 0;
			int startTagIndex = attributes.indexOf("<");
			while(startTagIndex > -1 && loop < 50)
			{
				String attributeName = attributes.substring(startTagIndex + 1, attributes.indexOf(">",startTagIndex+1));
				int endTagEndIndex = attributes.indexOf("</" + attributeName + ">", startTagIndex);
				
				String value1 = getAttributeValue(contentVersionVO1, attributeName, false);
				String value2 = getAttributeValue(contentVersionVO2, attributeName, false);
				
				//logger.info("value1:" + value1);
				//logger.info("value2:" + value2);
				if(!value1.equals(value2))
					changes.add(attributeName);
				
				startTagIndex = attributes.indexOf("<", endTagEndIndex + 1);
				loop++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return changes;
	}

	/**
	 * Returns an attribute value from the ContentVersionVO
	 *
	 * @param contentVersionVO The version on which to find the value
	 * @param attributeName THe name of the attribute whose value is wanted
	 * @param escapeHTML A boolean indicating if the result should be escaped
	 * @return The String vlaue of the attribute, or blank if it doe snot exist.
	 */
	public List<String> getAttributeNames(ContentVersionVO contentVersionVO1)
	{
		List<String> attributeNames = new ArrayList<String>();
		if(contentVersionVO1 == null)
			return attributeNames;
		
		try
		{
			String xml = contentVersionVO1.getVersionValue();
	
			int attributesStartTagIndex = xml.indexOf("<attributes>");
			int attributesStopTagIndex = xml.indexOf("</attributes>");
			
			String attributes = xml.substring(attributesStartTagIndex+12, attributesStopTagIndex);
			
			//logger.info("attributes1:" + attributes);
			
			int startTagIndex = attributes.indexOf("<");
			while(startTagIndex > -1)
			{
				String attributeName = attributes.substring(startTagIndex + 1, attributes.indexOf(">",startTagIndex+1));
				int endTagEndIndex = attributes.indexOf("</" + attributeName + ">", startTagIndex);
				attributeNames.add(attributeName);
				
				startTagIndex = attributes.indexOf("<", endTagEndIndex + 1);
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return attributeNames;
	}
	/**
	 * This method fetches a value from the xml that is the contentVersions Value. If the 
	 * contentVersioVO is null the contentVersion has not been created yet and no values are present.
	 */
	 
	public void updateAttributeValue(Integer contentVersionId, String attributeName, String attributeValue, InfoGluePrincipal infogluePrincipal) throws SystemException, Bug
	{
		updateAttributeValue(contentVersionId, attributeName, attributeValue, infogluePrincipal, true);
	}

	/**
	 * This method fetches a value from the xml that is the contentVersions Value. If the 
	 * contentVersioVO is null the contentVersion has not been created yet and no values are present.
	 */

	public void updateAttributeValue(Integer contentVersionId, String attributeName, String attributeValue, InfoGluePrincipal infogluePrincipal, Database db) throws SystemException, Bug
	{
		updateAttributeValue(contentVersionId, attributeName, attributeValue, infogluePrincipal, true, db);
	}

	/**
	 * This method fetches a value from the xml that is the contentVersions Value. If the 
	 * contentVersioVO is null the contentVersion has not been created yet and no values are present.
	 */
	public void updateAttributeValue(Integer contentVersionId, String attributeName, String attributeValue, InfoGluePrincipal infogluePrincipal, boolean skipValidate) throws SystemException, Bug
	{
    	Database db = CastorDatabaseService.getDatabase();

		try
		{
			beginTransaction(db);

			updateAttributeValue(contentVersionId, attributeName, attributeValue, infogluePrincipal, skipValidate, db);

		    commitTransaction(db);
		}
		catch (Exception ex)
		{
			rollbackTransaction(db);
		    logger.error("Error when updating content attribute value. Message: " + ex.getMessage());
		    logger.error("Error when updating content attribute value.", ex);
		}
	}

	public void updateAttributeValue(Integer contentVersionId, String attributeName, String attributeValue, InfoGluePrincipal infogluePrincipal, boolean skipValidate, Database db) throws SystemException, Bug
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
				
				NodeList nl = document.getDocumentElement().getChildNodes();
				Node attributesNode = nl.item(0);
				
				boolean existed = false;
				nl = attributesNode.getChildNodes();
				for(int i=0; i<nl.getLength(); i++)
				{
					Node n = nl.item(i);
					if(n.getNodeName().equalsIgnoreCase(attributeName))
					{
						if(n.getFirstChild() != null && n.getFirstChild().getNodeValue() != null)
						{
							n.getFirstChild().setNodeValue(attributeValue);
							existed = true;
							break;
						}
						else
						{
							CDATASection cdata = document.createCDATASection(attributeValue);
							n.appendChild(cdata);
							existed = true;
							break;
						}
					}
				}
				
				if(existed == false)
				{
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
				update(contentVersionVO.getContentId(), contentVersionVO.getLanguageId(), contentVersionVO, infogluePrincipal, skipValidate);
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
	
    public void getContentAndAffectedItemsRecursive(Integer contentId, Integer stateId, List<SiteNodeVersionVO> siteNodeVersionVOList, List<ContentVersionVO> contentVersionVOList, boolean mustBeFirst, boolean includeMetaInfo, ProcessBean processBean) throws ConstraintException, SystemException
	{
        Database db = CastorDatabaseService.getDatabase();

	    beginTransaction(db);

        try
        {
            ContentVO content = ContentController.getContentController().getContentVOWithId(contentId, db);
			processBean.updateProcess("Searching for items to publish");

            getContentAndAffectedItemsRecursive(content, stateId, new ArrayList(), new ArrayList(), db, siteNodeVersionVOList, contentVersionVOList, mustBeFirst, includeMetaInfo, processBean);
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
	}

	private void getContentAndAffectedItemsRecursive(ContentVO contentVO, Integer stateId, List checkedSiteNodes, List checkedContents, Database db, List<SiteNodeVersionVO> siteNodeVersionVOList, List<ContentVersionVO> contentVersionVOList, boolean mustBeFirst, boolean includeMetaInfo, ProcessBean processBean) throws ConstraintException, SystemException, Exception
	{
	    //checkedSiteNodes.add(content.getId());
	    checkedContents.add(contentVO.getId());
        
		List<ContentVersionVO> contentVersions = ContentVersionController.getContentVersionController().getLatestContentVersionVOListPerLanguage(contentVO.getId(), db);
	    //List contentVersions = getLatestContentVersionWithParent(contentVO.getId(), stateId, db, mustBeFirst);
	    
		Iterator<ContentVersionVO> contentVersionsIterator = contentVersions.iterator();
	    while(contentVersionsIterator.hasNext())
	    {
	        ContentVersionVO contentVersion = contentVersionsIterator.next();
	        
            ContentTypeDefinitionVO contentTypeDefinitionVO = null;
            if(contentVO.getContentTypeDefinitionId() != null)
            {
            	try
            	{
            		contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVO.getContentTypeDefinitionId(), db);
            	}
            	catch (Exception e) 
            	{
            		
            		logger.error("No existing content type for: " + ContentController.getContentController().getContentPath(contentVO.getId()) + ":" + contentVO.getId());
				}
            }
            
            if(includeMetaInfo || (!includeMetaInfo && (contentTypeDefinitionVO == null || !contentTypeDefinitionVO.getName().equalsIgnoreCase("Meta info"))))
            {
	        	if(contentVersion.getStateId().equals(ContentVersionVO.WORKING_STATE))
		        	contentVersionVOList.add(contentVersion);
            }
            
	        List relatedEntities = RegistryController.getController().getMatchingRegistryVOListForReferencingEntity(ContentVersion.class.getName(), contentVersion.getId().toString(), db);
	        
	        Iterator relatedEntitiesIterator = relatedEntities.iterator();
	        while(relatedEntitiesIterator.hasNext())
	        {
	            RegistryVO registryVO = (RegistryVO)relatedEntitiesIterator.next();
	            logger.info("registryVO:" + registryVO.getEntityName() + ":" + registryVO.getEntityId());
	            if(registryVO.getEntityName().equals(SiteNode.class.getName()) && !checkedSiteNodes.contains(new Integer(registryVO.getEntityId())))
	            {
	                try
	                {
		                SiteNodeVO relatedSiteNode = SiteNodeController.getController().getSiteNodeVOWithId(new Integer(registryVO.getEntityId()), db);
		                SiteNodeVersionVO relatedSiteNodeVersion = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(db, new Integer(registryVO.getEntityId()));
		                //SiteNodeVersionVO relatedSiteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionIfInState(relatedSiteNode, stateId, db);
		                if(relatedSiteNodeVersion != null && contentVO.getRepositoryId().intValue() == relatedSiteNode.getRepositoryId().intValue())
		                {
		                	if(relatedSiteNodeVersion.getStateId().equals(SiteNodeVersionVO.WORKING_STATE))
		                		siteNodeVersionVOList.add(relatedSiteNodeVersion);
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
		                ContentVO relatedContentVO = ContentController.getContentController().getContentVOWithId(new Integer(registryVO.getEntityId()), db);
		                ContentTypeDefinitionVO relatedContentTypeDefinitionVO = null;
		                if(relatedContentVO.getContentTypeDefinitionId() != null)
		                	relatedContentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(relatedContentVO.getContentTypeDefinitionId(), db);
		                
		                if(includeMetaInfo || (!includeMetaInfo && (relatedContentTypeDefinitionVO == null || !relatedContentTypeDefinitionVO.getName().equalsIgnoreCase("Meta info"))))
		                {
		            		List<ContentVersionVO> relatedContentVersions = ContentVersionController.getContentVersionController().getLatestContentVersionVOListPerLanguage(relatedContentVO.getId(), db);
		            		
		                	//List<ContentVersionVO> relatedContentVersions = ContentVersionController.getContentVersionController().getLatestContentVersionIdsPerLanguage(relatedContentVO.getId(), stateId, db);
		                	//List<ContentVersionVO> relatedContentVersions = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVOIfInState(relatedContentVO.getId(), stateId, db);
			                logger.info("relatedContentVersions:" + relatedContentVersions.size());
			                
			                Iterator<ContentVersionVO> relatedContentVersionsIterator = relatedContentVersions.iterator();
			                while(relatedContentVersionsIterator.hasNext())
			                {
			                	ContentVersionVO relatedContentVersion = relatedContentVersionsIterator.next();
			                	//ContentVO contentVO = ContentController.getContentController().getContentVOWithId(relatedContentVersion.getContentId(), db);
				                if(relatedContentVersion != null && contentVO.getRepositoryId().intValue() == relatedContentVO.getRepositoryId().intValue())
				                {
				                	if(relatedContentVersion.getStateId().equals(ContentVersionVO.WORKING_STATE))
				                		contentVersionVOList.add(relatedContentVersion);
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
		List<ContentVO> childContentVOList = ContentController.getContentController().getContentChildrenVOList(contentVO.getId(), null, db);
	    //Collection childContentList = content.getChildren();
		Iterator<ContentVO> cit = childContentVOList.iterator();
		while (cit.hasNext())
		{
			if (contentVersionVOList.size() % 10 == 0 || siteNodeVersionVOList.size() % 10 == 0)
			{
				processBean.updateLastDescription("Found " + contentVersionVOList.size() + " contents and " + siteNodeVersionVOList.size() + " pages so far...");
			}

			ContentVO citContent = cit.next();
			getContentAndAffectedItemsRecursive(citContent, stateId, checkedSiteNodes, checkedContents, db, siteNodeVersionVOList, contentVersionVOList, mustBeFirst, includeMetaInfo, processBean);
		}
		
	}
	


	/**
	 * This method are here to return all content versions that are x number of versions behind the current active version. This is for cleanup purposes.
	 * 
	 * @param numberOfVersionsToKeep
	 * @return
	 * @throws SystemException 
	 */
	
	public int cleanContentVersions(int numberOfVersionsToKeep, boolean keepOnlyOldPublishedVersions, long minimumTimeBetweenVersionsDuringClean, boolean deleteVersions) throws SystemException, Exception 
	{
		int cleanedVersions = 0;
		
		int batchLimit = 20;
		List languageVOList = LanguageController.getController().getLanguageVOList();
		
		Iterator<LanguageVO> languageVOListIterator = languageVOList.iterator();
		while(languageVOListIterator.hasNext())
		{
			LanguageVO languageVO = languageVOListIterator.next();
			
			Map<Integer,Integer> contentIdMap = getContentIdVersionCountMap(languageVO.getId(), numberOfVersionsToKeep);
			if(!deleteVersions)
			{
				for(Integer contentId : contentIdMap.keySet())
				{
					Integer versionCount = contentIdMap.get(contentId);
					int additions = versionCount - numberOfVersionsToKeep;
					//System.out.println("additions " + contentId + ": " + additions + "/" + versionCount + "/" + numberOfVersionsToKeep);
					cleanedVersions = cleanedVersions + additions;
				}
			}
			else
			{
				List<SmallestContentVersionVO> contentVersionVOList = getSmallestContentVersionVOList(languageVO.getId(), numberOfVersionsToKeep, keepOnlyOldPublishedVersions, minimumTimeBetweenVersionsDuringClean);
				
				//System.out.println("Deleting " + contentVersionVOList.size() + " versions for language " + languageVO.getName());
				int maxIndex = (contentVersionVOList.size() > batchLimit ? batchLimit : contentVersionVOList.size());
				List partList = contentVersionVOList.subList(0, maxIndex);
				while(partList.size() > 0)
				{
					if(deleteVersions)
						cleanVersions(numberOfVersionsToKeep, partList);
					cleanedVersions = cleanedVersions + partList.size();
					logger.info("Cleaned " + cleanedVersions + " of " + contentVersionVOList.size());
					partList.clear();
					maxIndex = (contentVersionVOList.size() > batchLimit ? batchLimit : contentVersionVOList.size());
					partList = contentVersionVOList.subList(0, maxIndex);
				}
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
				//ContentVersion contentVersion = getContentVersionWithId(contentVersionVO.getContentVersionId(), db);
				MediumContentVersionImpl contentVersion = getMediumContentVersionWithId(contentVersionVO.getContentVersionId(), db);
				logger.info("Deleting the contentVersion " + contentVersion.getId() + " on content " + contentVersion.getOwningContent());
				delete(contentVersion, db, true);
			}
			
			commitRegistryAwareTransaction(db);

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
	
	public List<SmallestContentVersionVO> getSmallestContentVersionVOList(Integer contentId) throws SystemException 
	{
		Database db = CastorDatabaseService.getDatabase();
    	
    	List<SmallestContentVersionVO> contentVersionsIdList = new ArrayList();

        beginTransaction(db);

        try
        {
        	contentVersionsIdList = getSmallestContentVersionVOList(contentId, db);
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
	
	
	public List<SmallestContentVersionVO> getSmallestContentVersionVOList(Integer contentId, Database db) throws SystemException, Exception
	{
		List<SmallestContentVersionVO> contentVersionsIdList = new ArrayList();

        OQLQuery oql = db.getOQLQuery("SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl cv WHERE cv.contentId = $1 ORDER BY cv.contentVersionId desc");
    	oql.bind(contentId);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
    	while (results.hasMore())
        {
			SmallestContentVersionImpl version = (SmallestContentVersionImpl)results.next();
			contentVersionsIdList.add(version.getValueObject());
        }
        
		results.close();
		oql.close();
        
		return contentVersionsIdList;
	}

	
	public Map<Integer,Integer> getContentIdVersionCountMap(Integer languageId, int numberOfVersionsToKeep) throws SystemException 
	{
		Map<Integer,Integer> result = new HashMap<Integer,Integer>();
		Database db = CastorDatabaseService.getDatabase();
    	
		beginTransaction(db);

        try
        {
			StringBuilder sql = new StringBuilder();
			if(CmsPropertyHandler.getUseShortTableNames().equals("true"))
				sql.append("select contId as contentId, versionCount from ( select cv.contId, count(*) as versionCount from cmContVer cv where cv.languageId = " + languageId + " group by cv.contId order by versionCount desc ) res where versionCount > " + numberOfVersionsToKeep);
			else
				sql.append("select contentId, versionCount from ( select cv.contentId, count(*) as versionCount from cmContentVersion cv where cv.languageId = " + languageId + " group by cv.contentId order by versionCount desc ) res where versionCount > " + numberOfVersionsToKeep);

			Connection conn = (Connection) db.getJdbcConnection();
			
			PreparedStatement psmt = conn.prepareStatement(sql.toString());

			int totalVersions = 0;
			ResultSet rs = psmt.executeQuery();
			while(rs.next())
			{
				Integer contentId = new Integer(rs.getString("contentId"));
				Integer count = new Integer(rs.getString("versionCount"));
				totalVersions = totalVersions + (count-numberOfVersionsToKeep);
				result.put(contentId, count);
				if(totalVersions > 500)
					break;
			}
			rs.close();
			psmt.close();
			
			commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
        return result;
	}
	
	/**
	 * This method are here to return all content versions that are x number of versions behind the current active version. This is for cleanup purposes.
	 * 
	 * @param numberOfVersionsToKeep
	 * @return
	 * @throws SystemException 
	 */

	public List<SmallestContentVersionVO> getSmallestContentVersionVOList(Integer languageId, int numberOfVersionsToKeep, boolean keepOnlyOldPublishedVersions, long minimumTimeBetweenVersionsDuringClean) throws SystemException, Bug, Exception
	{
		Map<Integer,Integer> contentIdMap = getContentIdVersionCountMap(languageId, numberOfVersionsToKeep);
		//System.out.println("contentVOList:" + contentIdMap.size());

		List<SmallestContentVersionVO> contentVersionVOList = new ArrayList<SmallestContentVersionVO>();
		if(contentIdMap == null || contentIdMap.size() == 0)
			return contentVersionVOList;
		
		List<Integer> contentIdList = new ArrayList<Integer>();
		contentIdList.addAll(contentIdMap.keySet());

		int slotSize = 100;
    	while(contentIdList.size() > 0)
    	{
    		List<Integer> subList = new ArrayList<Integer>();
    		if(contentIdList.size() > slotSize)
    		{
    			subList = contentIdList.subList(0, slotSize);
    			contentIdList = contentIdList.subList(slotSize, contentIdList.size()-1);
    		}
    		else
    		{
    			subList.addAll(contentIdList);
    			contentIdList.clear();
    		}
    		
    		if(subList.size() > 0)
	    	{
	    		contentVersionVOList.addAll(getSmallestContentVersionVOListImpl(subList, languageId, numberOfVersionsToKeep, keepOnlyOldPublishedVersions, minimumTimeBetweenVersionsDuringClean));
	    	}
    	}
    	//System.out.println("contentVOList:" + contentVersionVOList.size());
    	
		return contentVersionVOList;
	}
	
	/**
	 * This method are here to return all content versions that are x number of versions behind the current active version. This is for cleanup purposes.
	 * 
	 * @param numberOfVersionsToKeep
	 * @return
	 * @throws SystemException 
	 */
	public List<SmallestContentVersionVO> getSmallestContentVersionVOListImpl(List<Integer> contentIdList, Integer languageId, int numberOfVersionsToKeep, boolean keepOnlyOldPublishedVersions, long minimumTimeBetweenVersionsDuringClean) throws SystemException 
	{
		logger.info("numberOfVersionsToKeep:" + numberOfVersionsToKeep);

		Database db = CastorDatabaseService.getDatabase();
    	
    	List<SmallestContentVersionVO> contentVersionsIdList = new ArrayList();

        beginTransaction(db);

        try
        {
    		StringBuilder variables = new StringBuilder();
    	    for(int i=0; i<contentIdList.size(); i++)
    	    	variables.append("$" + (i+2) + (i+1!=contentIdList.size() ? "," : ""));

    	    String SQL = "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl cv WHERE cv.languageId = $1 AND cv.contentId IN LIST (" + variables + ") ORDER BY cv.contentId, cv.contentVersionId desc";
            //System.out.println("SQL:" + SQL);
            
            OQLQuery oql = db.getOQLQuery(SQL);
        	oql.bind(languageId);
    		for(Integer contentId : contentIdList)
    		{
    			oql.bind(contentId);
    			//System.out.println("contentId:" + contentId);
    		}

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
										
					//System.out.println("versionNonPublishedSuggestions:" + versionNonPublishedSuggestions.size());
					//System.out.println("versionInitialSuggestions:" + versionInitialSuggestions.size());
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
	public MediumContentVersionImpl checkStateAndChangeIfNeeded(Integer contentVersionId, InfoGluePrincipal principal, Database db) throws ConstraintException, SystemException, Bug
    {
		MediumContentVersionImpl contentVersion = null;
    	ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId, db);
    	if(!contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
		{
			List events = new ArrayList();
			ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId(), db);
			contentVersion = ContentStateController.changeState(contentVersionVO.getId(), contentVO, ContentVersionVO.WORKING_STATE, "new working version", false, null, principal, contentVersionVO.getContentId(), db, events);
		}
		else
		{
			contentVersion = ContentVersionController.getContentVersionController().getMediumContentVersionWithId(contentVersionVO.getId(), db);
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
        	MediumContentVersionImpl contentVersion = null;
        	ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId, db);
        	DigitalAssetVO digitalAssetVO = DigitalAssetController.getController().getDigitalAssetVOWithId(digitalAssetId, db);
    	    if(!contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
			{
				List events = new ArrayList();
				ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId(), db);
				contentVersion = ContentStateController.changeState(contentVersionVO.getId(), contentVO, ContentVersionVO.WORKING_STATE, "new working version", false, null, principal, contentVersionVO.getContentId(), db, events);
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
	            		contentVersion = ContentVersionController.getContentVersionController().getMediumContentVersionWithId(contentVersionId, db);
	            	
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
			ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId(), db);
			contentVersion = ContentStateController.changeState(contentVersionVO.getId(), contentVO, ContentVersionVO.WORKING_STATE, "new working version", false, null, principal, contentVersionVO.getContentId(), db, events);
			digitalAssetVO = DigitalAssetController.getController().getLatestDigitalAssetVO(contentVersion.getId(), digitalAssetVO.getAssetKey(), db);
		}
    	    
    	return digitalAssetVO;
    }

	
	/**
	 * This method gets a contentVersion with a state and a language which is active.
	 */

	public void preCacheContentVersionVOList(Collection contentList, Integer languageId, Integer operatingMode, DeliveryContext deliveryContext, Database db) throws Exception
    {
		List<Content> localContentList = new ArrayList<Content>();
		localContentList.addAll(contentList);

		while(localContentList != null && localContentList.size() > 0)
		{
			List<Content> localContentSubList = new ArrayList<Content>();
			if(localContentList.size() > 50)
			{
				localContentSubList.addAll(localContentList.subList(0, 50));
				localContentList = localContentList.subList(50, localContentList.size()-1);
			}
			else
			{
				localContentSubList.addAll(localContentList);
				localContentList.clear();
			}
			
			StringBuffer contentIds = new StringBuffer();
			for(Object localContent : localContentSubList)
			{
				if(contentIds.length() > 0)
					contentIds.append(",");
				if(localContent instanceof Integer)
					contentIds.append(localContent);
				else if(localContent instanceof ContentVO)
					contentIds.append(((ContentVO)localContent).getId());
				else if(localContent instanceof Content)
					contentIds.append(((Content)localContent).getId());
			}
			//System.out.println("contentIds:" + contentIds);
			
		    //logger.info("Querying for verson: " + versionKey); 
			OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl cv WHERE cv.contentId IN LIST (" + contentIds + ") AND cv.languageId = $1 AND cv.stateId >= $2 AND cv.isActive = $3 ORDER BY cv.contentVersionId desc");
	    	oql.bind(languageId);
	    	oql.bind(operatingMode);
	    	oql.bind(true);

	    	QueryResults results = oql.execute(Database.ReadOnly);
	    	
			while(results.hasMore()) 
	        {
				ContentVersion contentVersion = (ContentVersion)results.next();
	        	ContentVersionVO contentVersionVO = contentVersion.getValueObject();

	        	String versionKey = "" + contentVersionVO.getContentId() + "_" + languageId + "_" + operatingMode + "_contentVersionVO";
	        	CacheController.cacheObjectInAdvancedCache("contentVersionCache", versionKey, contentVersionVO, new String[]{CacheController.getPooledString(2, contentVersionVO.getId()), CacheController.getPooledString(1, contentVersionVO.getContentId())}, true);
	        }
			
			results.close();
			oql.close();
		
			break;
		}
    }

	public List<ContentVersionVO> getContentVersionVOList(Integer contentTypeDefinitionId, Integer excludeContentTypeDefinitionId, Integer languageId, boolean showDeletedItems, Integer stateId, Integer lastContentVersionId, Integer limit, boolean ascendingOrder, boolean includeSiteNode) throws Exception
	{
		List<ContentVersionVO> contentVersionVOList = new ArrayList<ContentVersionVO>();
        
        Database db = CastorDatabaseService.getDatabase();

	    beginTransaction(db);

        try
        {
        	contentVersionVOList = getContentVersionVOList(contentTypeDefinitionId, excludeContentTypeDefinitionId, languageId, showDeletedItems, stateId, lastContentVersionId, limit, ascendingOrder, db, includeSiteNode);            
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
	
	/**
	 * This method precaches a number of meta info content versions to get the site up to speed faster.
	 */
   	
	public List<ContentVersionVO> getContentVersionVOList(Integer languageId, Integer stateId, Integer limit, Database db) throws Exception
	{
		List<ContentVersionVO> contentVersionVOList = new ArrayList<ContentVersionVO>();
		
   		StringBuffer SQL = new StringBuffer();
    	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
    	{
    		SQL.append("CALL SQL select cv.contVerId, cv.stateId, cv.modifiedDateTime, cv.verComment, cv.isCheckedOut, cv.isActive, cv.contId, cv.languageId, cv.versionModifier, cv.verValue, (select count(*) from cmContVerDigitalAsset cvda where cvda.contVerId = cv.contVerId) AS assetCount ");
        	SQL.append(", -1 as siteNodeId, '' as siteNodeName ");
    		SQL.append(" from cmContVer cv, cmCont c ");
    		SQL.append("WHERE "); 
			SQL.append("c.contTypeDefId >= $1 AND ");
			SQL.append("cv.languageId >= $2 AND ");
			SQL.append("cv.stateId >= $3 AND ");
			SQL.append("cv.isActive = $4 AND ");
			SQL.append("cv.contId = c.contId AND ");
			SQL.append("cv.contVerId = (  ");
			SQL.append("	select max(contVerId) from cmContVer cv2  ");
			SQL.append("	WHERE  ");
			SQL.append("	cv2.contId = cv.contId AND  ");
			SQL.append("  	cv2.languageId = cv.languageId AND ");
			SQL.append("	cv2.isActive = cv.isActive AND ");
			SQL.append("	cv2.stateId >= $5 ");
			SQL.append("	)  ");
			
    		SQL.append(" order by cv.contVerId limit $6 AS org.infoglue.cms.entities.content.impl.simple.IndexFriendlyContentVersionImpl");
	   	}
    	else
    	{
    		SQL.append("CALL SQL select cv.contentVersionId, cv.stateId, cv.modifiedDateTime, cv.versionComment, cv.isCheckedOut, cv.isActive, cv.contentId, cv.languageId, cv.versionModifier, cv.versionValue, (select count(*) from cmContentVersionDigitalAsset cvda where cvda.contentVersionId = cv.contentVersionId) AS assetCount ");
       		SQL.append(", -1 as siteNodeId, '' as siteNodeName ");
    		SQL.append(" from cmContent c, cmContentVersion cv ");
    		SQL.append("WHERE "); 
			SQL.append("c.contentTypeDefinitionId >= $1 AND ");
			SQL.append("cv.languageId >= $2 AND ");
			SQL.append("cv.stateId >= $3 AND ");
			SQL.append("cv.isActive = $4 AND ");
			SQL.append("cv.contentId = c.contentId AND ");
			SQL.append("cv.contentVersionId = (  ");
			SQL.append("	select max(contentVersionId) from cmContentVersion cv2  ");
			SQL.append("	WHERE  ");
			SQL.append("	cv2.contentId = cv.contentId AND  ");
			SQL.append("  	cv2.languageId = cv.languageId AND ");
			SQL.append("	cv2.isActive = cv.isActive AND ");
			SQL.append("	cv2.stateId >= $5 ");
			SQL.append("	)  ");
			
    		SQL.append(" order by cv.contentVersionId limit $6 AS org.infoglue.cms.entities.content.impl.simple.IndexFriendlyContentVersionImpl");
       	}

    	//logger.info("SQL:" + SQL);
    	//logger.info("parentSiteNodeId:" + parentSiteNodeId);
    	//logger.info("showDeletedItems:" + showDeletedItems);
    	OQLQuery oql = db.getOQLQuery(SQL.toString());
		oql.bind(2);
		oql.bind(3);
		oql.bind(stateId);
		oql.bind(true);
		oql.bind(stateId);
		oql.bind(limit);
		
		QueryResults results = oql.execute(Database.ReadOnly);
		while (results.hasMore()) 
		{
			IndexFriendlyContentVersionImpl contentVersion = (IndexFriendlyContentVersionImpl)results.next();
			contentVersionVOList.add(contentVersion.getValueObject());
		}

		results.close();
		oql.close();
        
		return contentVersionVOList;
	} 

	
	/**
	 * This method returns a list of the children a siteNode has.
	 */
   	
	public List<ContentVersionVO> getContentVersionVOList(Integer contentTypeDefinitionId, Integer excludeContentTypeDefinitionId, Integer languageId, boolean showDeletedItems, Integer stateId, Integer lastContentVersionId, Integer limit, boolean ascendingOrder, Database db, boolean includeSiteNode) throws Exception
	{
		List<ContentVersionVO> contentVersionVOList = new ArrayList<ContentVersionVO>();
		
   		StringBuffer SQL = new StringBuffer();
    	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
    	{
    		SQL.append("CALL SQL select cv.contVerId, cv.stateId, cv.modifiedDateTime, cv.verComment, cv.isCheckedOut, cv.isActive, cv.contId, cv.languageId, cv.versionModifier, cv.verValue, (select count(*) from cmContVerDigitalAsset cvda where cvda.contVerId = cv.contVerId) AS assetCount ");
    		if(includeSiteNode)
        		SQL.append(", (select sn.siNoId from cmSiNo sn where sn.metaInfoContentId = c.contId) AS siNoId, (select sn.name from cmSiNo sn where sn.metaInfoContentId = c.contId) AS siteNodeName ");
    		else
        		SQL.append(", -1 as siteNodeId, '' as siteNodeName ");
    		SQL.append(" from cmCont c, cmContVer cv ");
    		SQL.append("WHERE "); 
    		SQL.append("c.isDeleted = $1 AND  ");
			SQL.append("cv.stateId >= $2 AND ");
			SQL.append("cv.isActive = $3 AND ");
			SQL.append("cv.contId = c.contId AND ");
			SQL.append("cv.contVerId = (  ");
			SQL.append("	select max(contVerId) from cmContVer cv2  ");
			SQL.append("	WHERE  ");
			SQL.append("	cv2.contId = cv.contId AND  ");
			SQL.append("  	cv2.languageId = cv.languageId AND ");
			SQL.append("	cv2.isActive = cv.isActive AND ");
			SQL.append("	cv2.stateId >= $4 ");
			SQL.append("	)  ");
			
			int index = 5;
    		if(contentTypeDefinitionId != null)
    		{
    			SQL.append(" AND c.contentTypeDefId = $" + index + "");
    			index++;
    		}
    		if(excludeContentTypeDefinitionId != null)
    		{
    			SQL.append(" AND c.contentTypeDefId <> $" + index + "");
    			index++;
    		}
    		if(languageId != null)
    		{
    			SQL.append(" AND cv.languageId = $" + index + "");
    			index++;
    		}
			if(lastContentVersionId != null && lastContentVersionId > 0)
			{
				SQL.append(" AND cv.contVerId > $" + index + "");
    			index++;
			}
			
    		SQL.append(" order by cv.contVerId " + (ascendingOrder ? "" : "DESC") + " limit $" + index + " AS org.infoglue.cms.entities.content.impl.simple.IndexFriendlyContentVersionImpl");
	   	}
    	else
    	{
    		SQL.append("CALL SQL select cv.contentVersionId, cv.stateId, cv.modifiedDateTime, cv.versionComment, cv.isCheckedOut, cv.isActive, cv.contentId, cv.languageId, cv.versionModifier, cv.versionValue, (select count(*) from cmContentVersionDigitalAsset cvda where cvda.contentVersionId = cv.contentVersionId) AS assetCount ");
    		if(includeSiteNode)
        		SQL.append(", (select sn.siteNodeId from cmSiteNode sn where sn.metaInfoContentId = c.contentId) AS siteNodeId, (select sn.name from cmSiteNode sn where sn.metaInfoContentId = c.contentId) AS siteNodeName ");
    		else
        		SQL.append(", -1 as siteNodeId, '' as siteNodeName ");
    		SQL.append(" from cmContent c, cmContentVersion cv ");
    		SQL.append("WHERE "); 
			SQL.append("c.isDeleted = $1 AND  ");
			SQL.append("cv.stateId >= $2 AND ");
			SQL.append("cv.isActive = $3 AND ");
			SQL.append("cv.contentId = c.contentId AND ");
			SQL.append("cv.contentVersionId = (  ");
			SQL.append("	select max(contentVersionId) from cmContentVersion cv2  ");
			SQL.append("	WHERE  ");
			SQL.append("	cv2.contentId = cv.contentId AND  ");
			SQL.append("  	cv2.languageId = cv.languageId AND ");
			SQL.append("	cv2.isActive = cv.isActive AND ");
			SQL.append("	cv2.stateId >= $4 ");
			SQL.append("	)  ");
			
			int index = 5;
    		if(contentTypeDefinitionId != null)
    		{
    			SQL.append(" AND c.contentTypeDefinitionId = $" + index + "");
    			index++;
    		}
    		if(excludeContentTypeDefinitionId != null)
    		{
    			SQL.append(" AND c.contentTypeDefinitionId <> $" + index + "");
    			index++;
    		}
    		if(languageId != null)
    		{
    			SQL.append(" AND cv.languageId = $" + index + "");
    			index++;
    		}
			if(lastContentVersionId != null && lastContentVersionId > 0)
			{
				SQL.append(" AND cv.contentVersionId > $" + index + "");
    			index++;
			}
			
    		SQL.append(" order by cv.contentVersionId " + (ascendingOrder ? "" : "DESC") + " limit $" + index + " AS org.infoglue.cms.entities.content.impl.simple.IndexFriendlyContentVersionImpl");
       	}

    	//logger.info("SQL:" + SQL);
    	//logger.info("parentSiteNodeId:" + parentSiteNodeId);
    	//logger.info("showDeletedItems:" + showDeletedItems);
    	OQLQuery oql = db.getOQLQuery(SQL.toString());
		oql.bind(showDeletedItems);
		oql.bind(stateId);
		oql.bind(true);
		oql.bind(stateId);
		if(contentTypeDefinitionId != null)
    		oql.bind(contentTypeDefinitionId);
		if(excludeContentTypeDefinitionId != null)
    		oql.bind(excludeContentTypeDefinitionId);
		if(languageId != null)
			oql.bind(languageId);
		if(lastContentVersionId != null && lastContentVersionId > 0)
    		oql.bind(lastContentVersionId);
		oql.bind(limit);
		
		QueryResults results = oql.execute(Database.ReadOnly);
		while (results.hasMore()) 
		{
			IndexFriendlyContentVersionImpl contentVersion = (IndexFriendlyContentVersionImpl)results.next();
			contentVersionVOList.add(contentVersion.getValueObject());

			//String versionKey = "" + contentVersion.getValueObject().getContentId() + "_" + contentVersion.getLanguageId() + "_" + stateId + "_contentVersionVO";
        	//CacheController.cacheObjectInAdvancedCache("contentVersionCache", versionKey, contentVersion.getValueObject(), new String[]{CacheController.getPooledString(2, contentVersion.getValueObject().getId()), CacheController.getPooledString(1, contentVersion.getValueObject().getContentId())}, true);
			//CacheController.cacheObjectInAdvancedCache("contentVersionCache", "" + contentVersion.getId(), contentVersion.getValueObject(), new String[]{CacheController.getPooledString(2, contentVersion.getValueObject().getId()), CacheController.getPooledString(1,  contentVersion.getValueObject().getContentId())}, true);
		}

		results.close();
		oql.close();
        
		return contentVersionVOList;
	} 
}
