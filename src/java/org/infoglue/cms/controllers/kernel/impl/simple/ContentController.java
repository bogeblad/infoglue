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

import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.ObjectNotFoundException;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.applications.contenttool.wizards.actions.CreateContentWizardInfoBean;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.applications.databeans.ReferenceBean;
import org.infoglue.cms.applications.databeans.ReferenceVersionBean;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.SmallestContentVersionVO;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallStateContentImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinition;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.GeneralOQLResult;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.RepositoryLanguage;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.management.ServiceDefinition;
import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.structure.Qualifyer;
import org.infoglue.cms.entities.structure.ServiceBinding;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.services.BaseService;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.mail.MailServiceFactory;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.Timer;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
 * @author Mattias Bogeblad
 */

public class ContentController extends BaseController 
{
    private final static Logger logger = Logger.getLogger(ContentController.class.getName());

    //private static ContentController controller = null;
    
	/**
	 * Factory method
	 */
	
	public static ContentController getContentController()
	{
		/*
		if(controller == null)
			controller = new ContentController();
		
		return controller;
		*/
		
		return new ContentController();
	}

   	/**
	 * This method returns selected active content versions.
	 */
    
	public List<Content> getContentList(Integer repositoryId, Integer minimumId, Integer limit, Database db) throws SystemException, Bug, Exception
	{
		List<Content> contentList = new ArrayList<Content>();

        OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.content.impl.simple.SmallContentImpl c WHERE c.repositoryId = $1 AND c.contentId > $2 ORDER BY c.contentId LIMIT $3");
    	oql.bind(repositoryId);
		oql.bind(minimumId);
		oql.bind(limit);
    	
    	QueryResults results = oql.execute(Database.READONLY);
		while (results.hasMore()) 
        {
			Content content = (Content)results.next();
			contentList.add(content);
        }
		
		results.close();
		oql.close();

		return contentList;
	}

	/**
	 * This method gets the siteNodeVO with the given id
	 */
	 
	public Map<Integer,ContentVO> getContentVOMapWithNoStateCheck(List<Integer> contentVersionIds) throws SystemException, Bug, Exception
    {
		Map<Integer,ContentVO> contentVOMap = null;
    	
		Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {	
			contentVOMap = getContentVOMapWithNoStateCheck(contentVersionIds, db);
			
	    	commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }      
        
        return contentVOMap;
    }        
	
	
	public Map<Integer,ContentVO> getContentVOMapWithNoStateCheck(List<Integer> contentVersionIds, Database db) throws SystemException, Bug, Exception
    {
		Map<Integer,ContentVO> contentVOMap = new HashMap<Integer,ContentVO>();
		if(contentVersionIds == null || contentVersionIds.size() == 0)
			return contentVOMap;
		
		List contentVersionIdsSubList = new ArrayList();
		contentVersionIdsSubList.addAll(contentVersionIds);
	
		int slotSize = 500;
		if(contentVersionIdsSubList.size() > 0)
		{
			List<Integer> subList = new ArrayList(contentVersionIdsSubList);
			if(contentVersionIdsSubList.size() > slotSize)
				subList = contentVersionIdsSubList.subList(0, slotSize);
	    	while(subList != null && subList.size() > 0)
	    	{
	    		contentVOMap.putAll(getContentVOMapWithNoStateCheckImpl(subList, db));
	    		contentVersionIdsSubList = contentVersionIdsSubList.subList(subList.size(), contentVersionIdsSubList.size());
	    	
	    		subList = new ArrayList(contentVersionIdsSubList);
	    		if(contentVersionIdsSubList.size() > slotSize)
	    			subList = contentVersionIdsSubList.subList(0, slotSize);
	    	}
		}
		
		return contentVOMap;
	}
	
	public Map<Integer,ContentVO> getContentVOMapWithNoStateCheckImpl(List<Integer> contentVersionIds, Database db) throws SystemException, Bug, Exception
	{
	    Timer t = new Timer();
	    Map<Integer,ContentVO> siteNodeVOMap = new HashMap<Integer,ContentVO>();
	    
	    if(contentVersionIds == null || contentVersionIds.size() == 0)
	    	return siteNodeVOMap;
	    
	    StringBuilder variables = new StringBuilder();
	    for(int i=0; i<contentVersionIds.size(); i++)
	    	variables.append("?" + (i+1!=contentVersionIds.size() ? "," : ""));
//	    	variables.append("?" + (i+1) + (i+1!=contentVersionIds.size() ? "," : ""));
	    
		StringBuilder sql = new StringBuilder();
		if(CmsPropertyHandler.getUseShortTableNames().equals("true"))
		{
			sql.append("select cv.contVerId AS id, c.name as column1Value, c.isProtected as column2Value, c.contentTypeDefId as column3Value, c.repositoryId as column4Value, c.parentContId as column5Value, cv.stateId as column6Value, c.contId as column7Value FROM cmCont c, cmContVer cv where c.contId = cv.contId AND cv.contVerId IN (" + variables + ") ");
		}
		else
		{
			sql.append("select cv.contentVersionId AS id, c.name as column1Value, c.isProtected as column2Value, c.contentTypeDefinitionId as column3Value, c.repositoryId as column4Value, c.parentContentId as column5Value, cv.stateId as column6Value, c.contentId as column7Value FROM cmContent c, cmContentVersion cv WHERE c.contentId = cv.contentId AND cv.contentVersionId IN (" + variables + ") ");
		}
		//String SQL = "CALL SQL " + sql.toString() + " AS org.infoglue.cms.entities.management.GeneralOQLResult";
		String SQL = "" + sql.toString() + "";
		logger.info("SQL:" + SQL);
		
		Connection conn = (Connection) db.getJdbcConnection();
		
		PreparedStatement psmt = conn.prepareStatement(SQL.toString());
		int i=1;
    	for(Integer entityId : contentVersionIds)
    	{
    		psmt.setInt(i, entityId);
    		i++;
    	}

		ResultSet rs = psmt.executeQuery();
		while(rs.next())
		{
			SmallStateContentImpl content = new SmallStateContentImpl();
			content.setContentVersionId(new Integer(rs.getString(1)));
			content.setContentId(new Integer(rs.getString(8)));
			content.setName(rs.getString(2));
			content.setIsProtected(new Integer(rs.getString(3)));
			if(rs.getString(4) != null && !rs.getString(4).equals(""))
				content.setContentTypeDefinitionId(new Integer(rs.getString(4)));
			content.setRepositoryId(new Integer(rs.getString(5)));
			if(rs.getString(6) == null)
			{
				logger.warn("Parent content ID was null on " + content.getId());
				content.setParentContentId(null);
			}
			else
				content.setParentContentId(new Integer(rs.getString(6)));
			content.setStateId(new Integer(rs.getString(7)));
            siteNodeVOMap.put(content.getValueObject().getContentVersionId(), content.getValueObject());
            logger.info("Adding:" + content.getValueObject().getContentVersionId() + "=" + content.getValueObject());
		}
		rs.close();
		psmt.close();

		/*
		OQLQuery oql = db.getOQLQuery(SQL);
		for(Integer entityId : contentVersionIds)
		{
			oql.bind(entityId);
		    System.out.println("entityId:" + entityId);
		}

		QueryResults results = oql.execute(Database.READONLY);
		while (results.hasMore()) 
        {
			GeneralOQLResult resultBean = (GeneralOQLResult)results.next();
			SmallStateContentImpl content = new SmallStateContentImpl();
			content.setContentVersionId(resultBean.getId());
			content.setContentId(new Integer(resultBean.getValue7()));
			content.setName(resultBean.getValue1());
			content.setIsProtected(new Integer(resultBean.getValue2()));
			if(resultBean.getValue3() != null && !resultBean.getValue3().equals(""))
				content.setContentTypeDefinitionId(new Integer(resultBean.getValue3()));
			content.setRepositoryId(new Integer(resultBean.getValue4()));
			content.setParentContentId(new Integer(resultBean.getValue5()));
			content.setStateId(new Integer(resultBean.getValue6()));
            siteNodeVOMap.put(content.getValueObject().getContentVersionId(), content.getValueObject());
        }       
		results.close();
		oql.close();
		*/
				
		return siteNodeVOMap;		
	}
	
	
	/**
	 * Gets matching references
	 */
	public List<ContentVO> getContentVOList(Set<Integer> contentIds, Database db) throws SystemException, Exception
	{
		List<ContentVO> contentVOList = new ArrayList<ContentVO>();
		   
		StringBuilder variables = new StringBuilder();
	    for(int i=0; i<contentIds.size(); i++)
	    	variables.append("$" + (i+1) + (i+1!=contentIds.size() ? "," : ""));
		
	    OQLQuery oql = null;
		if(CmsPropertyHandler.getUseShortTableNames().equals("true"))
			oql = db.getOQLQuery("CALL SQL SELECT c.contId, c.name, c.publishDateTime, c.expireDateTime, c.isBranch, c.isProtected, c.isDeleted, c.creator, c.contentTypeDefId, c.repositoryId, c.parentContId FROM cmCont c where contId IN (" + variables + ") AS org.infoglue.cms.entities.content.impl.simple.SmallContentImpl");
		else
			oql = db.getOQLQuery("CALL SQL SELECT c.contentId, c.name, c.publishDateTime, c.expireDateTime, c.isBranch, c.isProtected, c.isDeleted, c.creator, c.contentTypeDefinitionId, c.repositoryId, c.parentContentId FROM cmContent c WHERE contentId IN (" + variables + ") AS org.infoglue.cms.entities.content.impl.simple.SmallContentImpl");

		for(Integer entityId : contentIds)
			oql.bind(entityId.toString());
		
		QueryResults results = oql.execute(Database.READONLY);
		
		while (results.hasMore()) 
        {
            SmallContentImpl content = (SmallContentImpl)results.next();
            contentVOList.add(content.getValueObject());
        }       
		
		results.close();
		oql.close();

		return contentVOList;		
	}

	public ContentVO getContentVOWithId(Integer contentId, boolean skipCaching) throws SystemException, Bug
    {
		String key = "" + contentId;
		ContentVO contentVO = (ContentVO)CacheController.getCachedObjectFromAdvancedCache("contentCache", key);
		if(contentVO != null)
		{
			//logger.info("There was an cached contentVO:" + contentVO);
		}
		else
		{
			contentVO = (ContentVO) getVOWithId(SmallContentImpl.class, contentId);

			if(contentVO != null && !skipCaching)
			{
				CacheController.cacheObjectInAdvancedCache("contentCache", key, contentVO, new String[]{CacheController.getPooledString(1, contentId)}, true);
			}
		}
		
		return contentVO;
    } 

	public ContentVO getContentVOWithId(Integer contentId) throws SystemException, Bug
    {
		String key = "" + contentId;
		ContentVO contentVO = (ContentVO)CacheController.getCachedObjectFromAdvancedCache("contentCache", key);
		if(contentVO != null)
		{
			//logger.info("There was an cached contentVO:" + contentVO);
		}
		else
		{
			contentVO = (ContentVO) getVOWithId(SmallContentImpl.class, contentId);

			if(contentVO != null)
			{
				CacheController.cacheObjectInAdvancedCache("contentCache", key, contentVO, new String[]{CacheController.getPooledString(1, contentId)}, true);
			}
		}
		
		return contentVO;
    } 

	public ContentVO getContentVOWithId(Integer contentId, Database db) throws SystemException, Bug
    {
		String key = "" + contentId;
		ContentVO contentVO = (ContentVO)CacheController.getCachedObjectFromAdvancedCache("contentCache", key);
		if(contentVO != null)
		{
			//logger.info("There was an cached contentVO:" + contentVO);
		}
		else
		{
			contentVO = (ContentVO) getVOWithId(SmallContentImpl.class, contentId, db);
			if(contentVO != null)
			{
				CacheController.cacheObjectInAdvancedCache("contentCache", key, contentVO, new String[]{CacheController.getPooledString(1, contentId)}, true);
			}
		}
		
		return contentVO;    
	} 

	public ContentVO getSmallContentVOWithIdDirectly(Integer contentId, Database db) throws SystemException, Bug
    {
    	return (ContentVO) getVOWithId(SmallContentImpl.class, contentId, db);
    } 

	/**
	 * This method return a contentVO
	 */
	
	public ContentVO getSmallContentVOWithId(Integer contentId, Database db, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		if(contentId == null || contentId.intValue() < 1)
			return null;

		if(deliveryContext != null)
			deliveryContext.addUsedContent(CacheController.getPooledString(1, contentId));

		String key = "" + contentId;
		ContentVO contentVO = (ContentVO)CacheController.getCachedObjectFromAdvancedCache("contentCache", key);
		if(contentVO != null)
		{
			//logger.info("There was an cached contentVO:" + contentVO);
		}
		else
		{
			contentVO = getSmallContentVOWithIdDirectly(contentId, db);
			if(contentVO != null)
			{
				CacheController.cacheObjectInAdvancedCache("contentCache", key, contentVO, new String[]{CacheController.getPooledString(1, contentId)}, true);
			}
		}
		
		return contentVO;
	}
	
	public ContentVO getSmallContentVOWithId(Integer contentId, Database db) throws SystemException, Bug
    {
    	return (ContentVO) getVOWithId(SmallContentImpl.class, contentId, db);
    } 

    public Content getContentWithId(Integer contentId, Database db) throws SystemException, Bug
    {
		return (Content) getObjectWithId(ContentImpl.class, contentId, db);
    }

    public Content getReadOnlyContentWithId(Integer contentId, Database db) throws SystemException, Bug
    {
		return (Content) getObjectWithIdAsReadOnly(ContentImpl.class, contentId, db);
    }

    public Content getMediumContentWithId(Integer contentId, Database db) throws SystemException, Bug
    {
		return (Content) getObjectWithId(MediumContentImpl.class, contentId, db);
    }

    public Content getReadOnlyMediumContentWithId(Integer contentId, Database db) throws SystemException, Bug
    {
		return (Content) getObjectWithIdAsReadOnly(MediumContentImpl.class, contentId, db);
    }

    
    public List getContentVOList() throws SystemException, Bug
    {
        return getAllVOObjects(ContentImpl.class, "contentId");
    }
	
	/**
	 * This method finishes what the create content wizard initiated and resulted in.
	 */
	
	public ContentVO create(CreateContentWizardInfoBean createContentWizardInfoBean) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		Content content = null;

		beginTransaction(db);

		try
		{
			content = create(db, createContentWizardInfoBean.getParentContentId(), createContentWizardInfoBean.getContentTypeDefinitionId(), createContentWizardInfoBean.getRepositoryId(), createContentWizardInfoBean.getContent().getValueObject());
			
			Iterator it = createContentWizardInfoBean.getContentVersions().keySet().iterator();
			while (it.hasNext()) 
			{
				Integer languageId = (Integer)it.next();
				logger.info("languageId:" + languageId);
				ContentVersionVO contentVersionVO = (ContentVersionVO)createContentWizardInfoBean.getContentVersions().get(languageId);
				contentVersionVO = ContentVersionController.getContentVersionController().createMedium(content.getContentId(), languageId, contentVersionVO, db).getValueObject();
			}
			
			//Bind if needed?
			
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
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}

		return content.getValueObject();
	}
	
	/**
	 * This method creates a new content-entity and references the entities it should know about.
	 * As castor is lousy at this in my opinion we also add the new entity to the surrounding entities.
	 */
	
    public ContentVO create(Integer parentContentId, Integer contentTypeDefinitionId, Integer repositoryId, ContentVO contentVO) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        Content content = null;

        beginTransaction(db);

        try
        {
            content = create(db, parentContentId, contentTypeDefinitionId, repositoryId, contentVO);
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
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return content.getValueObject();
    }

	/**
	 * This method creates a new content-entity and references the entities it should know about.
	 * As castor is lousy at this in my opinion we also add the new entity to the surrounding entities.
	 */
	    
    public /*synchronized*/ MediumContentImpl create(Database db, Integer parentContentId, Integer contentTypeDefinitionId, Integer repositoryId, ContentVO contentVO) throws ConstraintException, SystemException, Exception
    {
    	MediumContentImpl content = null;
		
        try
        {            
            ContentVO parentContentVO = null;

            if(parentContentId != null)
            {
            	parentContentVO = getContentVOWithId(parentContentId, db);
            	
            	if(repositoryId == null)
					repositoryId = parentContentVO.getRepositoryId();	
            	
            	if(parentContentVO.getIsBranch() == false)
            	{
            		Content parentContent = getMediumContentWithId(parentContentVO.getId(), db);
            		parentContent.setIsBranch(new Boolean(true));
            	}
            }
            
            //RepositoryVO repository = RepositoryController.getController().getRepositoryVOWithId(repositoryId, db);

			/*
        	synchronized (controller)
			{
        		//db.lock(parentContent);
			*/
	            content = new MediumContentImpl();
	            content.setValueObject(contentVO);
	            content.setParentContentId(parentContentId);
	            content.setRepositoryId(repositoryId);
	            content.setContentTypeDefinitionId(contentTypeDefinitionId);
	            
				db.create(content);
				
				//Now we add the content to the knowledge of the related entities.
				/*
				if(parentContent != null)
				{
					parentContent.getChildren().add(content);
					parentContent.setIsBranch(new Boolean(true));
				}
				*/
        	//}

			//repository.getContents().add(content);			
        }
        catch(Exception e)
        {
        	//logger.error("An error occurred so we should not complete the transaction:" + e, e);
            throw new SystemException(e.getMessage());    
        }
        
        return content;
    }
       

	/**
	 * This method deletes a content and also erases all the children and all versions.
	 */
	    
    public void delete(ContentVO contentVO, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException
    {
    	delete(contentVO, infogluePrincipal, false);
    }
    
	/**
	 * This method deletes a content and also erases all the children and all versions.
	 * @throws Exception 
	 * @throws Bug 
	 */
	    
    public void delete(Integer contentId, boolean forceDelete, InfoGluePrincipal infogluePrincipal) throws Bug, Exception
    {
    	ContentVO contentVO = ContentControllerProxy.getController().getACContentVOWithId(infogluePrincipal, contentId);
    	
    	delete(contentVO, infogluePrincipal, forceDelete);
    }
    
	/**
	 * This method deletes a content and also erases all the children and all versions.
	 */
	    
    public void delete(ContentVO contentVO, InfoGluePrincipal infogluePrincipal, boolean forceDelete) throws ConstraintException, SystemException
    {
	    Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {		
	    	delete(contentVO, db, (forceDelete ? true : false), false, forceDelete, infogluePrincipal);
	    	
	    	commitTransaction(db);
            
        }
        catch(ConstraintException ce)
        {
        	logger.warn("An error occurred so we should not complete the transaction:" + ce);
            rollbackTransaction(db);
            throw ce;
        }
        catch(Exception e)
        {
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

    }  
    

	/**
	 * This method deletes a content and also erases all the children and all versions.
	 */
	    
	public void delete(ContentVO contentVO, InfoGluePrincipal infogluePrincipal, Database db) throws ConstraintException, SystemException, Exception
	{
	    delete(contentVO, db, false, false, false, infogluePrincipal);
	}
	
	/**
	 * This method deletes a content and also erases all the children and all versions.
	 */
	    
	public void delete(ContentVO contentVO, Database db, boolean skipRelationCheck, boolean skipServiceBindings, boolean forceDelete, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException, Exception
	{
		Content content = null;
		try
		{
			content = getContentWithId(contentVO.getContentId(), db);
		}
		catch(SystemException e)
		{
			return;
		}
		
		Content parent = content.getParentContent();
		if(parent != null)
		{
			/*
			synchronized (controller)
			{
				//db.lock(controller);
			*/	
				Iterator childContentIterator = parent.getChildren().iterator();
				while(childContentIterator.hasNext())
				{
				    Content candidate = (Content)childContentIterator.next();
				    if(candidate.getId().equals(contentVO.getContentId()))
				    {
				        deleteRecursive(content, childContentIterator, db, skipRelationCheck, skipServiceBindings, forceDelete, infogluePrincipal);
				    }
				}
			/*
			}
			*/
		}
		else
		{
		    deleteRecursive(content, null, db, skipRelationCheck, skipServiceBindings, forceDelete, infogluePrincipal);
		}
	}        

	/**
	 * Recursively deletes all contents and their versions. Also updates related entities about the change.
	 */
	
    private static void deleteRecursive(Content content, Iterator parentIterator, Database db, boolean skipRelationCheck, boolean skipServiceBindings, boolean forceDelete, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException, Exception
    {
        if(!skipRelationCheck)
        {
	        List referenceBeanList = RegistryController.getController().getReferencingObjectsForContent(content.getId(), -1, false, false, db);
			if(referenceBeanList != null && referenceBeanList.size() > 0)
				throw new ConstraintException("ContentVersion.stateId", "3305");
        }
        
        Collection children = content.getChildren();
		Iterator childrenIterator = children.iterator();
		while(childrenIterator.hasNext())
		{
			Content childContent = (Content)childrenIterator.next();
			deleteRecursive(childContent, childrenIterator, db, skipRelationCheck, skipServiceBindings, forceDelete, infogluePrincipal);   			
   		}
		content.setChildren(new ArrayList());
		
		boolean isDeletable = getIsDeletable(content, infogluePrincipal, db);
   		if(forceDelete || isDeletable)
	    {
			ContentVersionController.getContentVersionController().deleteVersionsForContent(content, db, forceDelete, infogluePrincipal);    	
			
			if(!skipServiceBindings)
			    ServiceBindingController.deleteServiceBindingsReferencingContent(content, db);
			
			if(parentIterator != null) 
			    parentIterator.remove();
	    	
	    	db.remove(content);
            
            Map args = new HashMap();
            args.put("globalKey", "infoglue");
            PropertySet ps = PropertySetManager.getInstance("jdbc", args);

            ps.remove( "content_" + content.getContentId() + "_allowedContentTypeNames");
            ps.remove( "content_" + content.getContentId() + "_defaultContentTypeName");
            ps.remove( "content_" + content.getContentId() + "_initialLanguageId");

	    }
	    else
    	{
    		throw new ConstraintException("ContentVersion.stateId", "3300", content.getName());
    	}			
    }        

	
	/**
	 * This method returns true if the content does not have any published contentversions or 
	 * are restricted in any other way.
	 */
	
	private static boolean getIsDeletable(Content content, InfoGluePrincipal infogluePrincipal, Database db) throws SystemException
	{
		boolean isDeletable = true;
	
		if(content.getIsProtected().equals(ContentVO.YES))
		{
			boolean hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(db, infogluePrincipal, "Content.Delete", "" + content.getId());
			if(!hasAccess)
				return false;
		}
		
        Collection contentVersions = content.getContentVersions();
    	Iterator versionIterator = contentVersions.iterator();
		while (versionIterator.hasNext()) 
        {
        	ContentVersion contentVersion = (ContentVersion)versionIterator.next();
        	if(contentVersion.getStateId().intValue() == ContentVersionVO.PUBLISHED_STATE.intValue() && contentVersion.getIsActive().booleanValue() == true)
        	{
        		logger.info("The content had a published version so we cannot delete it..");
				isDeletable = false;
        		break;
        	}
	    }		
			
		return isDeletable;	
	}

	
    public ContentVO update(ContentVO contentVO) throws ConstraintException, SystemException
    {
        return update(contentVO, null);
    }        


    public ContentVO update(ContentVO contentVO, Integer contentTypeDefinitionId) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();

        Content content = null;

        beginTransaction(db);

        try
        {
            content = (Content)getObjectWithId(ContentImpl.class, contentVO.getId(), db);
    		content.setVO(contentVO);
    		
            if(contentTypeDefinitionId != null)
            {
                ContentTypeDefinition contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithId(contentTypeDefinitionId, db);
                content.setContentTypeDefinition((ContentTypeDefinitionImpl)contentTypeDefinition);
            }
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return content.getValueObject();
    }        

    public void changeRepository(Integer contentId, Integer repositoryId) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();

        Content content = null;

        beginTransaction(db);

        try
        {
            content = (Content)getObjectWithId(ContentImpl.class, contentId, db);
            if(repositoryId != null)
            	content.setRepository((RepositoryImpl)RepositoryController.getController().getRepositoryWithId(repositoryId, db));
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }  
    
	public List<LanguageVO> getAvailableLanguagesForContentWithId(Integer contentId, Database db) throws ConstraintException, SystemException, Exception
	{
		List<LanguageVO> availableLanguageVOList = new ArrayList<LanguageVO>();
		
		ContentVO content = getContentVOWithId(contentId, db);
		if(content != null)
		{
			//Repository repository = content.getRepository();
			if(content.getRepositoryId() != null)
			{
				availableLanguageVOList = LanguageController.getController().getAvailableLanguageVOListForRepository(content.getRepositoryId(), db);
			    /*
				List availableRepositoryLanguageList = RepositoryLanguageController.getController().getRepositoryLanguageListWithRepositoryId(content.getRepositoryId(), db);
				Iterator i = availableRepositoryLanguageList.iterator();
				while(i.hasNext())
				{
					RepositoryLanguage repositoryLanguage = (RepositoryLanguage)i.next();
					availableLanguageVOList.add(repositoryLanguage.getLanguage().getValueObject());
				}
				*/
			}
		}
		
		return availableLanguageVOList;
	}
/*
	public List getAvailableLanguagesForContentWithId(Integer contentId, Database db) throws ConstraintException, SystemException
	{
		List availableLanguageVOList = new ArrayList();
		
		Content content = getContentWithId(contentId, db);
		if(content != null)
		{
			Repository repository = content.getRepository();
			if(repository != null)
			{
				Collection availableLanguages = repository.getRepositoryLanguages();
				Iterator i = availableLanguages.iterator();
				while(i.hasNext())
				{
					RepositoryLanguage repositoryLanguage = (RepositoryLanguage)i.next();
					
					int position = 0;
					Iterator availableLanguageVOListIterator = availableLanguageVOList.iterator();
					while(availableLanguageVOListIterator.hasNext())
					{
						LanguageVO availableLanguageVO = (LanguageVO)availableLanguageVOListIterator.next();
						if(repositoryLanguage.getLanguage().getValueObject().getId().intValue() < availableLanguageVO.getId().intValue())
							break; 
						
						position++;
					}
					
					availableLanguageVOList.add(position, repositoryLanguage.getLanguage().getValueObject());
				}
			}
		}
		
		return availableLanguageVOList;
	}
*/
	
	/**
	 * This method returns the value-object of the parent of a specific content. 
	 */
	
    public static ContentVO getParentContent(ContentVO contentVO) throws SystemException, Bug
    {
    	if(contentVO.getParentContentId() == null)
    		return null;

        Database db = CastorDatabaseService.getDatabase();
		ContentVO parentContentVO = null;
		
        beginTransaction(db);

        try
        {
			parentContentVO = getContentController().getContentVOWithId(contentVO.getParentContentId(), db);
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return parentContentVO;    	
    }
	
	/**
	 * This method returns the value-object of the parent of a specific content. 
	 */
    public static ContentVO getParentContent(Integer contentId) throws SystemException, Bug
    {
        Database db = CastorDatabaseService.getDatabase();
		ContentVO parentContentVO = null;
		
        beginTransaction(db);

        try
        {
        	//Content content = (Content) getObjectWithId(ContentImpl.class, contentId, db);
			//Content parent = content.getParentContent();

        	ContentVO content = getContentController().getContentVOWithId(contentId, db);

			if(content != null && content.getParentContentId() != null)
				parentContentVO = getContentController().getContentVOWithId(content.getParentContentId(), db);
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
		return parentContentVO;    	
    }

	/**
	 * This method returns the value-object of the parent of a specific content. 
	 */
	
    public ContentVO getSmallParentContent(ContentVO contentVO, Database db) throws SystemException, Bug, Exception
    {
    	if(contentVO.getParentContentId() != null && contentVO.getParentContentId() > -1)
    		return getSmallContentVOWithId(contentVO.getParentContentId(), db, null);
    	else 
    		return null;
    }

    
	public static void addChildContent(ContentVO parentVO, ContentVO childVO)
		throws ConstraintException, SystemException
	{

        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
			Content parent = (Content) getObjectWithId(ContentImpl.class, parentVO.getContentId(), db);
			Content child = (Content) getObjectWithId(ContentImpl.class, childVO.getContentId(), db);
			parent.getChildren().add(child);

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
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
		
	}

	public static void removeChildContent(ContentVO parentVO, ContentVO childVO)
		throws ConstraintException, SystemException
	{

        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
			Content parent = (Content) getObjectWithId(ContentImpl.class, parentVO.getContentId(), db);
			Content child = (Content) getObjectWithId(ContentImpl.class, childVO.getContentId(), db);
			parent.getChildren().remove(child);

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
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
		
	}

	

	/**
	 * This method moves a content from one parent-content to another. First we check so no illegal actions are 
	 * in process. For example the target folder must not be the item to be moved or a child to the item.
	 * Such actions would result in model-errors.
	 */
		
	public void moveContent(ContentVO contentVO, Integer newParentContentId) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();

        beginTransaction(db);

        try
        {
            moveContent(contentVO, newParentContentId, db);
            
            commitTransaction(db);
        }
        catch(ConstraintException ce)
        {
			logger.error("An error occurred so we should not complete the transaction:" + ce.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + ce.getMessage(), ce);
            rollbackTransaction(db);
            throw new SystemException(ce.getMessage());
        }
        catch(Exception e)
        {
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

    }   

	/**
	 * This method moves a content from one parent-content to another. First we check so no illegal actions are 
	 * in process. For example the target folder must not be the item to be moved or a child to the item.
	 * Such actions would result in model-errors.
	 */
		
	public void moveContent(ContentVO contentVO, Integer newParentContentId, Database db) throws ConstraintException, SystemException
    {
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        //Content content = null;
		//Content newParentContent = null;
		//Content oldParentContent = null;

        //Validation that checks the entire object
        contentVO.validate();
		
		if(newParentContentId == null)
        {
        	logger.warn("You must specify the new parent-content......");
        	throw new ConstraintException("Content.parentContentId", "3303");
        }

        if(contentVO.getId().intValue() == newParentContentId.intValue())
        {
        	logger.warn("You cannot have the content as it's own parent......");
        	throw new ConstraintException("Content.parentContentId", "3301");
        }
		
		Content content          = getMediumContentWithId(contentVO.getContentId(), db);
        //oldParentContent = content.getParentContent();
        //newParentContent = getContentWithId(newParentContentId, db);
                    
        if(content.getValueObject().getParentContentId() == null || content.getValueObject().getParentContentId().intValue() == newParentContentId.intValue())
        {
        	logger.warn("You cannot specify the same folder as it originally was located in......");
        	throw new ConstraintException("Content.parentContentId", "3304");
        }

		//ContentVO tempContent = newParentContent.getParentContent();
		ContentVO newParentContentVO = getContentVOWithId(newParentContentId, db);
		
		Integer parentContentId = newParentContentVO.getParentContentId();
		while(parentContentId != null)
		{
			ContentVO tempContent = getContentVOWithId(parentContentId, db);
			if(tempContent.getId().intValue() == content.getId().intValue())
			{
				logger.warn("You cannot move the content to a child under it......");
        		throw new ConstraintException("Content.parentContentId", "3302");
			}
			parentContentId = tempContent.getParentContentId();
		}				            
        
        //oldParentContent.getChildren().remove(content);
        //content.setParentContent((ContentImpl)newParentContent);
        content.getValueObject().setParentContentId(newParentContentId);
        changeRepositoryRecursive(content, newParentContentVO.getRepositoryId());
        //content.setRepository(newParentContent.getRepository());
        //newParentContent.getChildren().add(content);
        
        //If any of the validations or setMethods reported an error, we throw them up now before create.
        ceb.throwIfNotEmpty();
    }  
	
	/**
	 * This method moves a content from one parent-content to another. First we check so no illegal actions are 
	 * in process. For example the target folder must not be the item to be moved or a child to the item.
	 * Such actions would result in model-errors.
	 */
		
	public void moveDigitalAsset(InfoGluePrincipal principal, Integer digitalAssetId, Integer contentId, Boolean fixReferences) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();

        beginTransaction(db);

        try
        {
        	moveDigitalAsset(principal, digitalAssetId, contentId, fixReferences, db);
            
            commitTransaction(db);
        }
        catch(ConstraintException ce)
        {
			logger.error("An contraint occurred so we should not complete the transaction:" + ce.getMessage());
			logger.warn("An contraint occurred so we should not complete the transaction:" + ce.getMessage(), ce);
            rollbackTransaction(db);
            throw ce;
        }
        catch(Exception e)
        {
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

    }   

	/**
	 * This method moves a content from one parent-content to another. First we check so no illegal actions are 
	 * in process. For example the target folder must not be the item to be moved or a child to the item.
	 * Such actions would result in model-errors.
	 */
		
	public void moveDigitalAsset(InfoGluePrincipal principal, Integer digitalAssetId, Integer contentId, Boolean fixReferences, Database db) throws ConstraintException, SystemException, Exception
    {
		if(contentId == null)
        {
        	logger.warn("You must specify the new content......");
			throw new ConstraintException("Content.parentContentId", "3303");
        }
		
		List<SmallestContentVersionVO> versions = DigitalAssetController.getController().getContentVersionVOListConnectedToAssetWithId(digitalAssetId);
		Integer languageId = null;
		for(SmallestContentVersionVO version : versions)
		{
			languageId = version.getLanguageId();
		}
		
		if(languageId == null)
		{
			logger.warn("You must specify the new content......");
			throw new ConstraintException("Content.mustHaveSameLanguage", "3309");
		}
		
		MediumContentVersionImpl selfNewVersion = null;
		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, languageId, db);

		if(contentVersionVO == null)
		{
			logger.warn("There must be a version....");
			throw new ConstraintException("Content.mustHaveSameLanguage", "3309");
		}
	
		MediumContentVersionImpl contentVersion = ContentVersionController.getContentVersionController().checkStateAndChangeIfNeeded(contentVersionVO.getId(), principal, db);
		if(contentVersion.getId() > contentVersionVO.getId())
		{
			selfNewVersion = contentVersion;
		}
		
		MediumDigitalAssetImpl asset = DigitalAssetController.getController().getMediumDigitalAssetWithId(digitalAssetId, db);

		Map<Integer,Integer> replaceMap = new HashMap<Integer,Integer>();
		
		List<Integer> selfContentVersionIDList = new ArrayList<Integer>();
		
		Iterator<MediumContentVersionImpl> versionIterator = asset.getContentVersions().iterator();
		while(versionIterator.hasNext())
		{
			MediumContentVersionImpl version = versionIterator.next();
			for(SmallestContentVersionVO cvVO : versions)
			{
				if(version.getContentVersionId().equals(cvVO.getId()))
				{
					logger.info("Removing from:" + cvVO.getId());
					selfContentVersionIDList.add(version.getContentVersionId());
					versionIterator.remove();
					replaceMap.put(version.getContentId(), contentId);
					break;
				}
			}
		}
		contentVersion.getDigitalAssets().add(asset);
		asset.getContentVersions().add(contentVersion);
	    
		if(fixReferences)
		{
			for(Integer oldContentId : replaceMap.keySet())
			{
				Integer newContentId = replaceMap.get(oldContentId);
				logger.info("We should replace all instances of " + oldContentId + "(" + asset.getAssetKey() + ") --> " + newContentId + "(" + asset.getAssetKey() + ")");
   				List<ReferenceBean> referenceBeans = RegistryController.getController().getReferencingObjectsForContentAsset(oldContentId, asset.getAssetKey(), 100, true, true, false);

   				logger.info("referenceBeans:" + referenceBeans.size());
   				for(Integer cvID : selfContentVersionIDList)
   				{
   					ContentVersionVO cvVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(cvID, db);
   					
   					ReferenceBean bean = new ReferenceBean();
   					ReferenceVersionBean versionBean = new ReferenceVersionBean();
   					versionBean.setReferencingObject(cvVO);
   					//bean.setReferencingCompletingObject(versionBean);
   					bean.getVersions().add(versionBean);
   					referenceBeans.add(bean);
   				}
   				
   				logger.info("referenceBeans:" + referenceBeans.size());
   				for(ReferenceBean referenceBean : referenceBeans)
   				{
   					logger.info("ReferenceBean:" + referenceBean.getName() + ":" + referenceBean.getReferencingCompletingObject());
   	   				
   					for(ReferenceVersionBean referenceVersionBean : referenceBean.getVersions())
   					{
   						Object o = referenceVersionBean.getReferencingObject();
   						logger.info("o:" + o.getClass().getName());
   						try
   						{
	   						if(o instanceof ContentVersionVO)
	   						{
	   							ContentVersionVO cv = (ContentVersionVO)o;
	   							logger.info("Replacing in:" + cv.getId());
	   	   						String newVersionValue = cv.getVersionValue(); //.replaceAll("\"" + oldContentId + "\"", "\"" + newContentId + "\"");
	   							
	   							Pattern p = Pattern.compile("<binding.*?>");
	   						    Matcher m = p.matcher(newVersionValue);
	   						    while (m.find()) 
		   						{
	   						    	logger.info("Found a " + m.group() + ".");
		   							String binding = m.group();
		   							if(binding.contains("\"" + oldContentId + "\"") && binding.contains("\"" + asset.getAssetKey() + "\""))
		   							{
		   								binding = binding.replaceFirst("\"" + oldContentId + "\"", "\"" + newContentId + "\"");
	
		   								logger.info("Replacing:" + m.group() + ":" + binding);
			   							newVersionValue = StringUtils.replace(newVersionValue, m.group(), binding);
			   							//newVersionValue = newVersionValue.replaceAll(m.group(), binding);
			   							logger.info("newVersionValue: " + newVersionValue);
		   							}
		   						}
	   							
	   						    Pattern pInlineAssets = Pattern.compile("getInlineAssetUrl\\(.*?\\)");
							    Matcher mInlineAssets = pInlineAssets.matcher(newVersionValue);
							    while (mInlineAssets.find()) 
		   						{
							    	logger.info("Found a " + mInlineAssets.group() + ".");
		   							String assetCall = mInlineAssets.group();
		   							if(assetCall.contains(oldContentId + ",") && (assetCall.contains("\"" + asset.getAssetKey() + "\"") || URLDecoder.decode(assetCall, "utf-8").contains("\"" + asset.getAssetKey() + "\"")))
		   							{
		   								logger.info("Replacing:" + mInlineAssets.group() + ":" + assetCall);
	
			   							assetCall = assetCall.replaceFirst("" + oldContentId + ",", "" + newContentId + ",");
			   							newVersionValue = StringUtils.replace(newVersionValue, mInlineAssets.group(), assetCall);
			   							//newVersionValue = newVersionValue.replaceAll(mInlineAssets.group(), assetCall);
			   							logger.info("newVersionValue: " + newVersionValue);
		   							}
		   						}
							    
	   							//newVersionValue = newVersionValue.replaceAll("getInlineAssetUrl\\(" + oldContentId + ",", "getInlineAssetUrl(" + newContentId + ",");
	   							ContentVersion cvReal = ContentVersionController.getContentVersionController().getMediumContentVersionWithId(cv.getId(), db);
	   							logger.info("cvReal:" + cvReal.getId());
	   						    if(selfNewVersion != null && selfNewVersion.getContentId().intValue() == cvReal.getValueObject().getContentId().intValue() && selfNewVersion.getLanguageId().intValue() == cvReal.getValueObject().getLanguageId().intValue() && selfNewVersion.getId().intValue() > cvReal.getId().intValue())
	   						    {
	   						    	logger.info("Was itself - lets use the new version instead...");
	   						    	cvReal = selfNewVersion;
	   						    }

	   							cvReal.setVersionValue(newVersionValue);
	   							cvReal.setVersionComment("Asset moved...");
	   							cvReal.setVersionModifier(principal.getName());
	   							cvReal.setModifiedDateTime(new Date());
	
	   							RegistryController.getController().updateContentVersion(cvReal.getValueObject(), null, db);
	   						}
	   						else if(o instanceof SiteNodeVersionVO)
	   						{
	   							SiteNodeVersionVO snvo = (SiteNodeVersionVO)o;
	   							logger.info("Replacing in sn:" + snvo.getId());
	   							
	   			                SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(snvo.getSiteNodeId(), db);
	   				    		LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(siteNodeVO.getRepositoryId(), db);
	   				    		ContentVersionVO cv = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(siteNodeVO.getMetaInfoContentId(), masterLanguageVO.getId(), db);
	   				    		logger.info("Replacing in:" + cv.getVersionValue());
	   							
	   							String newVersionValue = cv.getVersionValue(); //.replaceAll("\"" + oldContentId + "\"", "\"" + newContentId + "\"");
	   							
	   							Pattern p = Pattern.compile("<binding.*?>");
	   						    Matcher m = p.matcher(newVersionValue);
	   						    while (m.find()) 
		   						{
	   						    	logger.info("Found a " + m.group() + ".");
		   							String binding = m.group();
		   							if(binding.contains("\"" + oldContentId + "\"") && binding.contains("\"" + asset.getAssetKey() + "\""))
		   							{
		   								binding = binding.replaceFirst("\"" + oldContentId + "\"", "\"" + newContentId + "\"");
	
		   								logger.info("Replacing:" + m.group() + ":" + binding);
			   							newVersionValue = StringUtils.replace(newVersionValue, m.group(), binding);
			   							logger.info("newVersionValue: " + newVersionValue);
		   							}
		   						}
	   							
	   							ContentVersion cvReal = ContentVersionController.getContentVersionController().getMediumContentVersionWithId(cv.getId(), db);
	   							cvReal.setVersionValue(newVersionValue);
	   							cvReal.setVersionComment("Asset moved...");
	   							cvReal.setVersionModifier(principal.getName());
	   							cvReal.setModifiedDateTime(new Date());
	   							
	   							RegistryController.getController().updateContentVersion(cvReal.getValueObject(), snvo, db);
	   						}
   						}
   						catch (Throwable e) 
   						{
   							e.printStackTrace();
						}
   					}
   				}
			}
		}
    }  

	/**
	 * This method copies the content and possibly subcontents/folders into a target folder
	 * @param contentId
	 * @param newParentContentId
	 */
	public void copyContent(Integer contentId, Integer newParentContentId, Integer assetMaxSize, String onlyLatestVersions) throws Exception
	{
		ExportImportController.getController().exportContents(contentId, newParentContentId, assetMaxSize, onlyLatestVersions);
	}
	
	/**
	 * Recursively sets the contents repositoryId.
	 * @param content
	 * @param newRepository
	 */

	private void changeRepositoryRecursive(Content content, Integer newRepositoryId)
	{
	    if(content.getValueObject().getRepositoryId().intValue() != newRepositoryId.intValue())
	    {
		    content.getValueObject().setRepositoryId(newRepositoryId);
		    Iterator childContentsIterator = content.getChildren().iterator();
		    while(childContentsIterator.hasNext())
		    {
		        Content childContent = (Content)childContentsIterator.next();
		        changeRepositoryRecursive(childContent, newRepositoryId);
		    }
	    }
	}
	
	/**
	 * Returns all Contents having the specified ContentTypeDefintion.
	 */
	
	public List getContentVOWithContentTypeDefinition(String contentTypeDefinitionName) throws SystemException
	{
        Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
        try
        {
			List result = getContentVOWithContentTypeDefinition(contentTypeDefinitionName, db);
            commitTransaction(db);
			return result;
        }
        catch(Exception e)
        {
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
	}
	
	/**
	 * Returns all Contents having the specified ContentTypeDefintion.
	 */
	public List getContentVOWithContentTypeDefinition(String contentTypeDefinitionName, Database db) throws SystemException 
	{
		HashMap arguments = new HashMap();
		arguments.put("method", "selectListOnContentTypeName");

		List argumentList = new ArrayList();
		String[] names = contentTypeDefinitionName.split(",");
		for(int i = 0; i < names.length; i++)
		{
			HashMap argument = new HashMap();
			argument.put("contentTypeDefinitionName", names[i]);
			argumentList.add(argument);
		}
		
		arguments.put("arguments", argumentList);
        try 
		{
			return getContentVOList(arguments, db);
		}
        catch(SystemException e)
		{
			throw e;
		}
        catch(Exception e)
		{
			throw new SystemException(e.getMessage());
		}
	}
	
	/**
	 * This method is sort of a sql-query-like method where you can send in arguments in form of a list
	 * of things that should match. The input is a Hashmap with a method and a List of HashMaps.
	 */
	
    public List getContentVOList(HashMap argumentHashMap) throws SystemException, Bug
    {
    	List contents = null;
    	
    	String method = (String)argumentHashMap.get("method");
    	logger.info("method:" + method);
    	
    	if(method.equalsIgnoreCase("selectContentListOnIdList"))
    	{
			contents = new ArrayList();
			List arguments = (List)argumentHashMap.get("arguments");
			logger.info("Arguments:" + arguments.size());  
			Iterator argumentIterator = arguments.iterator();
			while(argumentIterator.hasNext())
			{ 		
				HashMap argument = (HashMap)argumentIterator.next(); 
				Integer contentId = new Integer((String)argument.get("contentId"));
				logger.info("Getting the content with Id:" + contentId);
				contents.add(getContentVOWithId(contentId));
			}
    	}
        else if(method.equalsIgnoreCase("selectListOnContentTypeName"))
    	{
			List arguments = (List)argumentHashMap.get("arguments");
			logger.info("Arguments:" + arguments.size());   		
			contents = getContentVOListByContentTypeNames(arguments);
    	}
        return contents;
    }
	
    
	/**
	 * This method returns a list of the children a siteNode has.
	 */
   	
	public List<ContentVO> getContentVOList(Integer limit) throws Exception
	{
		List<ContentVO> childrenVOList = new ArrayList<ContentVO>();
        
        Database db = CastorDatabaseService.getDatabase();

	    beginTransaction(db);

        try
        {
        	childrenVOList = getContentVOList(limit, db);            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
        return childrenVOList;
	}
	
	/**
	 * This method returns a list of the children a siteNode has.
	 */
   	
	public List<ContentVO> getContentVOList(Integer limit, Database db) throws Exception
	{
		List<ContentVO> childrenVOList = new ArrayList<ContentVO>();
		
		OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.content.impl.simple.SmallContentImpl c ORDER BY c.contentId DESC LIMIT $1");
		oql.bind(limit);

		QueryResults results = oql.execute(Database.READONLY);
		while (results.hasMore()) 
		{
			Content content = (Content)results.next();
			childrenVOList.add(content.getValueObject());
			
			String key = "" + content.getValueObject().getId();
			CacheController.cacheObjectInAdvancedCache("contentCache", key, content.getValueObject(), new String[]{CacheController.getPooledString(1, content.getValueObject().getId())}, true);
		}
		
		results.close();
		oql.close();
   		
		return childrenVOList;
	}
	
	/**
	 * This method is sort of a sql-query-like method where you can send in arguments in form of a list
	 * of things that should match. The input is a Hashmap with a method and a List of HashMaps.
	 */
	
    public List getContentVOList(HashMap argumentHashMap, Database db) throws SystemException, Exception
    {
    	List contents = null;
    	
    	String method = (String)argumentHashMap.get("method");
    	logger.info("method:" + method);
    	
    	if(method.equalsIgnoreCase("selectContentListOnIdList"))
    	{
			contents = new ArrayList();
			List arguments = (List)argumentHashMap.get("arguments");
			logger.info("Arguments:" + arguments.size());  
			Iterator argumentIterator = arguments.iterator();
			while(argumentIterator.hasNext())
			{ 		
				HashMap argument = (HashMap)argumentIterator.next(); 
				Integer contentId = new Integer((String)argument.get("contentId"));
				logger.info("Getting the content with Id:" + contentId);
				contents.add(getSmallContentVOWithId(contentId, db));
			}
    	}
        else if(method.equalsIgnoreCase("selectListOnContentTypeName"))
    	{
			List arguments = (List)argumentHashMap.get("arguments");
			logger.info("Arguments:" + arguments.size());   		
			contents = getContentVOListByContentTypeNames(arguments, db);
    	}
        return contents;
    }
    

	/**
	 * The input is a list of <em>HashMap</em>s. Each <em>HashMap</em> must contain the key <em>contentTypeDefinitionName</em>.
	 */
	protected List<ContentVO> getContentVOListByContentTypeNames(List<HashMap<String, Object>> arguments) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();
		List<ContentVO> contents = new ArrayList<ContentVO>();
		beginTransaction(db);
		try
		{
			Iterator<HashMap<String, Object>> i = arguments.iterator();
			while(i.hasNext())
			{
				HashMap<String, Object> argument = i.next();
				String contentTypeDefinitionName = (String)argument.get("contentTypeDefinitionName");
				ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName(contentTypeDefinitionName, db);

				OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.MediumContentImpl c WHERE c.contentTypeDefinitionId = $1 AND c.isDeleted = $2 ORDER BY c.contentId");
				oql.bind(ctdVO.getId());
				oql.bind(0);

				QueryResults results = oql.execute(Database.READONLY);
				while(results.hasMore()) 
				{
					MediumContentImpl content = (MediumContentImpl)results.next();
					contents.add(content.getValueObject());
				}
				results.close();
				oql.close();
			}
			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction. Message: " + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction.", e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return contents;
	}
	
	
	/**
	 * The input is a list of hashmaps.
	 */
	protected List getContentVOListByContentTypeNames(List arguments, Database db) throws SystemException, Exception
	{
		List contents = new ArrayList();

		Iterator i = arguments.iterator();
    	while(i.hasNext())
    	{
    		Timer t = new Timer();
    		HashMap argument = (HashMap)i.next();
    		String contentTypeDefinitionName = (String)argument.get("contentTypeDefinitionName");
    		Integer ctdId = -1;
    		if(contentTypeDefinitionName != null && !contentTypeDefinitionName.equals(""))
    		{
    			ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName(contentTypeDefinitionName, db);
    			if(ctdVO != null)
    				ctdId = ctdVO.getId();
    			else
    			{
    				logger.error("No content type called: " + contentTypeDefinitionName);
    				continue;
    			}
    		}
    		OQLQuery oql = null;

    		if(CmsPropertyHandler.getUseShortTableNames().equals("true"))
				oql = db.getOQLQuery("CALL SQL SELECT c.contId, c.name, c.publishDateTime, c.expireDateTime, c.isBranch, c.isProtected, c.isDeleted, c.creator, c.contentTypeDefId, c.repositoryId, c.parentContId FROM cmCont c where c.contentTypeDefId = $1 AS org.infoglue.cms.entities.content.impl.simple.SmallContentImpl");
			else
				oql = db.getOQLQuery("CALL SQL SELECT c.contentId, c.name, c.publishDateTime, c.expireDateTime, c.isBranch, c.isProtected, c.isDeleted, c.creator, c.contentTypeDefinitionId, c.repositoryId, c.parentContentId FROM cmContent c where c.contentTypeDefinitionId = $1 AS org.infoglue.cms.entities.content.impl.simple.SmallContentImpl");

        	oql.bind(ctdId);
        	
        	QueryResults results = oql.execute(Database.READONLY);
        	if(logger.isInfoEnabled())
        		t.printElapsedTime("Query for " + contentTypeDefinitionName);
			
			while(results.hasMore()) 
            {
            	//MediumContentImpl content = (MediumContentImpl)results.next();
            	SmallContentImpl content = (SmallContentImpl)results.next();
            	contents.add(content.getValueObject());
            }
			if(logger.isInfoEnabled())
				t.printElapsedTime("Getting all for " + contentTypeDefinitionName + ":" + contents.size());
			
			results.close();
			oql.close();
	   	}
    	
		return contents;    	
	}
	
	/**
	 * The input is a list of hashmaps.
	 */
	
	protected List getContentVOListByContentTypeIds(List<Integer> contentTypeDefinitionIdList, Database db) throws SystemException, Exception
	{
		List contents = new ArrayList();

		StringBuffer sb = new StringBuffer();
		for(Integer ctdId : contentTypeDefinitionIdList)
		{
			sb.append(ctdId+",");
		}
		sb.deleteCharAt(sb.length()-1);
		
		OQLQuery oql = null;

		if(CmsPropertyHandler.getUseShortTableNames().equals("true"))
			oql = db.getOQLQuery("CALL SQL SELECT c.contId, c.name, c.publishDateTime, c.expireDateTime, c.isBranch, c.isProtected, c.isDeleted, c.creator, c.contentTypeDefId, c.repositoryId, c.parentContId FROM cmCont c where c.contentTypeDefId IN(" + sb.toString() + ") AS org.infoglue.cms.entities.content.impl.simple.SmallContentImpl");
		else
			oql = db.getOQLQuery("CALL SQL SELECT c.contentId, c.name, c.publishDateTime, c.expireDateTime, c.isBranch, c.isProtected, c.isDeleted, c.creator, c.contentTypeDefinitionId, c.repositoryId, c.parentContentId FROM cmContent c where c.contentTypeDefinitionId IN(" + sb.toString() + ") AS org.infoglue.cms.entities.content.impl.simple.SmallContentImpl");

    	QueryResults results = oql.execute(Database.READONLY);
 		while(results.hasMore()) 
        {
        	SmallContentImpl content = (SmallContentImpl)results.next();
        	contents.add(content.getValueObject());
        }
		
		results.close();
		oql.close();
    	
		return contents;    	
	}

	protected List<ContentVersionVO> getLatestContentVersionVOListByContentTypeId(Integer[] contentTypeDefinitionIds, Database db) throws SystemException, Exception
	{
		List<ContentVersionVO> contentVersionVOList = new ArrayList<ContentVersionVO>();

		StringBuilder sb = new StringBuilder();
		if(CmsPropertyHandler.getUseShortTableNames().equals("true"))
		{
			sb.append("select cv.contVerId, cv.stateId, cv.modifiedDateTime, cv.VerComment, cv.isCheckedOut, cv.isActive, cv.ContId, cv.languageId, cv.versionModifier, cv.VerValue from cmContVer cv where cv.contid IN ");
			sb.append("( ");
			sb.append("   select contId from cmCont c2 where ( "); 
			for(int i=0; i<contentTypeDefinitionIds.length; i++)
			{
				if(i>0)
					sb.append(" OR ");
				sb.append("c2.contenttypedefid = " + contentTypeDefinitionIds[i]);
			}
			sb.append("		) ");
			sb.append(") ");
			sb.append("AND cv.contverid =  ");
			sb.append("( ");
			sb.append("    select max(ContVerId) from cmContVer cv2, cmCont c where cv2.contId = c.contId AND cv2.contId = cv.contId AND cv2.isActive = 1 AND cv2.languageId =  ");
			sb.append("    ( ");
			sb.append("      select languageId from  ");
			sb.append("      ( ");
			sb.append("        select repositoryid, languageId from cmrepositorylanguage order by repositoryid, sortorder ");
			sb.append("      ) ");
			sb.append("      where repositoryid = c.repositoryId and rownum = 1 ");
			sb.append("    ) ");
			sb.append(") ");
			sb.append("ORDER BY cv.contverid ");
		}
		else
		{
			sb.append("select cv.contentVersionId, cv.stateId, cv.modifiedDateTime, cv.versionComment, cv.isCheckedOut, cv.isActive, cv.contentId, cv.languageId, cv.versionModifier, cv.versionValue from cmContentVersion cv where cv.contentId IN  ");
			sb.append("( ");
			sb.append("   select contentId from cmContent c2 where ( "); 
			for(int i=0; i<contentTypeDefinitionIds.length; i++)
			{
				if(i>0)
					sb.append(" OR ");
				sb.append("c2.contentTypeDefinitionId = " + contentTypeDefinitionIds[i]);
			}
			sb.append("		) ");
			sb.append(") ");
			sb.append("AND cv.contentVersionId =  ");
			sb.append("( ");
			sb.append("    select max(contentVersionId) from cmContentVersion cv2, cmContent c where cv2.contentId = c.contentId AND cv2.contentId = cv.contentId AND cv2.isActive = 1 AND cv2.languageId =  ");
			sb.append("    ( ");
			sb.append("      select languageId from  ");
			sb.append("      ( ");
			sb.append("        select repositoryId, languageId from cmRepositoryLanguage order by repositoryId, sortorder ");
			sb.append("      ) langView ");
			sb.append("      where langView.repositoryId = c.repositoryId LIMIT 1 ");
			sb.append("    ) ");
			sb.append(") ");
			sb.append("ORDER BY cv.contentVersionId ");
		}

		Timer t = new Timer();
		OQLQuery oql = db.getOQLQuery("CALL SQL " + sb.toString() + "AS org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl");
		logger.warn("Query for component content versions took:" + t.getElapsedTime());

    	QueryResults results = oql.execute(Database.READONLY);
		while (results.hasMore()) 
        {
			ContentVersion contentVersion = (ContentVersion)results.next();
			contentVersionVOList.add(contentVersion.getValueObject());
	    }

		logger.warn("Read of component content versions took:" + t.getElapsedTime());

		results.close();
		oql.close();
    	
		return contentVersionVOList;    	
	}

	protected List<ContentVersionVO> getLatestContentVersionVOListByContentTypeIdForSmallCollections(Integer[] contentTypeDefinitionIds, Database db) throws SystemException, Exception
	{
		List<ContentVersionVO> contentVersionVOList = new ArrayList<ContentVersionVO>();
		Timer t = new Timer();
		
		List<ContentVO> contents = ContentController.getContentController().getContentVOListByContentTypeIds(Arrays.asList(contentTypeDefinitionIds), db);
		logger.info("contents:" + contents.size());
		logger.info("getContentVOListByContentTypeIds took: " + t.getElapsedTime());

		Set<Integer> contentIds = new HashSet<Integer>();
		for(ContentVO contentVO : contents)
		{
			contentIds.add(contentVO.getId());
		}
		
		contentVersionVOList = ContentVersionController.getContentVersionController().getLatestContentVersionVOListPerLanguage(contentIds, db);
		logger.info("getContentVOListByContentTypeIds took: " + t.getElapsedTime());
		
		return contentVersionVOList;    	
	}
	
	
   	/**
	 * This method fetches the root content for a particular repository.
	 * If there is no such content we create one as all repositories need one to work.
	 */
	        
   	public ContentVO getRootContentVO(Integer repositoryId, String userName) throws ConstraintException, SystemException
   	{
   		if(repositoryId == null || repositoryId.intValue() < 1)
   			return null;
   		
		String key = "root_" + repositoryId;
		ContentVO contentVO = (ContentVO)CacheController.getCachedObjectFromAdvancedCache("rootContentCache", key);
		if(contentVO != null)
		{
			return contentVO;
		}

        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
            logger.info("Fetching the root content for the repository " + repositoryId);
			//OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.content.impl.simple.ContentImpl c WHERE is_undefined(c.parentContent) AND c.repository.repositoryId = $1");
			OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.content.impl.simple.SmallContentImpl c WHERE is_undefined(c.parentContentId) AND c.repositoryId = $1");
			oql.bind(repositoryId);
			
        	QueryResults results = oql.execute(Database.READONLY);			
			if (results.hasMore()) 
            {
				Content content = (Content)results.next();
				contentVO = content.getValueObject();
	        }
            else
            {
				//None found - we create it and give it the name of the repository.
				logger.info("Found no rootContent so we create a new....");
				ContentVO rootContentVO = new ContentVO();
				RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);
				rootContentVO.setCreatorName(userName);
				rootContentVO.setName(repositoryVO.getName());
				rootContentVO.setIsBranch(new Boolean(true));
            	Content content = create(db, null, null, repositoryId, rootContentVO);
            	contentVO = content.getValueObject();
            }
            
			results.close();
			oql.close();

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
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

		if(contentVO != null)
			CacheController.cacheObjectInAdvancedCache("rootContentCache", key, contentVO);

        return contentVO;
   	}


   	
	/**
	 * This method fetches the root content for a particular repository.
	 * If there is no such content we create one as all repositories need one to work.
	 */
	        
	public ContentVO getRootContentVO(Integer repositoryId, String userName, boolean createIfNonExisting) throws ConstraintException, SystemException
	{
		String key = "root_" + repositoryId;
		ContentVO contentVO = (ContentVO)CacheController.getCachedObjectFromAdvancedCache("rootContentCache", key);
		if(contentVO != null)
		{
			return contentVO;
		}

		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		beginTransaction(db);

		try
		{
			contentVO = getRootContentVO(db, repositoryId, userName, createIfNonExisting);
			
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
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}

		if(contentVO != null)
			CacheController.cacheObjectInAdvancedCache("rootContentCache", key, contentVO);

		return contentVO;
	}
   	
	
	/**
	 * This method fetches the root content for a particular repository within a transaction.
	 * If there is no such content we create one as all repositories need one to work.
	 */
	        
	public ContentVO getRootContentVO(Database db, Integer repositoryId, String userName, boolean createIfNonExisting) throws ConstraintException, SystemException, Exception
	{
		String key = "root_" + repositoryId;
		ContentVO contentVO = (ContentVO)CacheController.getCachedObjectFromAdvancedCache("rootContentCache", key);
		if(contentVO != null)
		{
			return contentVO;
		}

		logger.info("Fetching the root content for the repository " + repositoryId);
		OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.content.impl.simple.SmallContentImpl c WHERE is_undefined(c.parentContentId) AND c.repositoryId = $1");
		oql.bind(repositoryId);
			
		QueryResults results = oql.execute(Database.READONLY);			
		if (results.hasMore()) 
		{
			SmallContentImpl contentImpl = (SmallContentImpl)results.next();
			contentVO = contentImpl.getValueObject();
		}
		else
		{
			if(createIfNonExisting)
			{
				//None found - we create it and give it the name of the repository.
				logger.info("Found no rootContent so we create a new....");
				ContentVO rootContentVO = new ContentVO();
				RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);
				rootContentVO.setCreatorName(userName);
				rootContentVO.setName(repositoryVO.getName());
				rootContentVO.setIsBranch(new Boolean(true));
				contentVO = create(db, null, null, repositoryId, rootContentVO).getValueObject();
			}
		}
		
		if(contentVO != null)
			CacheController.cacheObjectInAdvancedCache("rootContentCache", key, contentVO);

		results.close();
		oql.close();
		
		return contentVO;
	}

	
	/**
	 * This method fetches the root content for a particular repository within a transaction.
	 * If there is no such content we create one as all repositories need one to work.
	 */
	        
	public Content getRootContent(Database db, Integer repositoryId, String userName, boolean createIfNonExisting) throws ConstraintException, SystemException, Exception
	{
		Content content = null;

		logger.info("Fetching the root content for the repository " + repositoryId);
		OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.content.impl.simple.ContentImpl c WHERE is_undefined(c.parentContent) AND c.repository.repositoryId = $1");
		oql.bind(repositoryId);
			
		QueryResults results = oql.execute(Database.READONLY);			
		if (results.hasMore()) 
		{
			content = (Content)results.next();
		}
		else
		{
			if(createIfNonExisting)
			{
				//None found - we create it and give it the name of the repository.
				logger.info("Found no rootContent so we create a new....");
				ContentVO rootContentVO = new ContentVO();
				RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);
				rootContentVO.setCreatorName(userName);
				rootContentVO.setName(repositoryVO.getName());
				rootContentVO.setIsBranch(new Boolean(true));
				content = create(db, null, null, repositoryId, rootContentVO);
			}
		}
		
		results.close();
		oql.close();
		
		return content;
	}

   	
	/**
	 * This method fetches the root content for a particular repository.
	 * If there is no such content we create one as all repositories need one to work.
	 */
	        
	public Content getRootContent(Integer repositoryId, Database db) throws ConstraintException, SystemException, Exception
	{
		Content content = null;

		OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.content.impl.simple.ContentImpl c WHERE is_undefined(c.parentContent) AND c.repository.repositoryId = $1");
		oql.bind(repositoryId);
			
		QueryResults results = oql.execute();		
		this.logger.info("Fetching entity in read/write mode" + repositoryId);

		if (results.hasMore()) 
		{
			content = (Content)results.next();
		}

		results.close();
		oql.close();

		return content;
	}
	
	
	
   	/**
   	 * This method returns a list of the children a content has.
   	 */
   	
   	public List<ContentVO> getContentChildrenVOList(Integer parentContentId) throws ConstraintException, SystemException
    {
   		return getContentChildrenVOList(parentContentId, null);
    }

   	/**
   	 * This method returns a list of the children a content has.
   	 */
   	
   	public List<ContentVO> getContentChildrenVOList(Integer parentContentId, String[] allowedContentTypeIds, Boolean showDeletedItems) throws ConstraintException, SystemException
    {
   		return getContentChildrenVOList(parentContentId, new ArrayList<LanguageVO>(), allowedContentTypeIds, showDeletedItems);
    }

   	/**
   	 * This method returns a list of the children a content has.
   	 */
   	
   	public List<ContentVO> getContentChildrenVOList(Integer parentContentId, List<LanguageVO> languageVOList, String[] allowedContentTypeIds, Boolean showDeletedItems) throws ConstraintException, SystemException
    {
		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        List childrenVOList = new ArrayList();

        beginTransaction(db);
        try
        {
        	childrenVOList = getContentChildrenVOList(parentContentId, languageVOList, allowedContentTypeIds, showDeletedItems, db);	
            
        	//If any of the validations or setMethods reported an error, we throw them up now before create.
            ceb.throwIfNotEmpty();
            
            commitTransaction(db);
        }
        catch(ConstraintException ce)
        {
        	logger.warn("An error occurred so we should not complete the transaction in getContentChildrenVOList:" + ce, ce);
            rollbackTransaction(db);
            throw ce;
        }
        catch(Exception e)
        {
        	e.printStackTrace();
			logger.error("An error occurred so we should not complete the transaction in getContentChildrenVOList:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction in getContentChildrenVOList:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return childrenVOList;
    } 

   	/**
   	 * This method returns a list of the children a content has.
   	 */
   	
   	public List<ContentVO> getContentChildrenVOList(Integer parentContentId, String[] allowedContentTypeIds) throws ConstraintException, SystemException
    {
		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        List childrenVOList = new ArrayList();

        beginTransaction(db);
        try
        {
        	
        	childrenVOList = getContentChildrenVOList(parentContentId, allowedContentTypeIds, db); //getContentChildrenVOList(parentContentId, allowedContentTypeIds, db);	

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
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
   		        
        return childrenVOList;
    } 
	
   	
   	public List<ContentVO> getContentChildrenVOList(Integer parentContentId, String[] allowedContentTypeIds, Boolean showDeletedItems, Database db) throws ConstraintException, SystemException, Exception
    {
   		return getContentChildrenVOList(parentContentId, new ArrayList<LanguageVO>(), allowedContentTypeIds, showDeletedItems, db);
    }
   	
   	/**
   	 * This method returns a list of the children a content has.
   	 */
   	
   	public List<ContentVO> getContentChildrenVOList(Integer parentContentId, List<LanguageVO> languageVOList, String[] allowedContentTypeIds, Boolean showDeletedItems, Database db) throws ConstraintException, SystemException, Exception
    {
   		String allowedContentTypeIdsString = "";
   		if(allowedContentTypeIds != null)
   		{
	   		for(int i=0; i < allowedContentTypeIds.length; i++)
	   			allowedContentTypeIdsString += "_" + allowedContentTypeIds[i];
   		}
   		
   		String key = "" + parentContentId + allowedContentTypeIdsString + "_" + showDeletedItems;
		if(logger.isInfoEnabled())
			logger.info("key:" + key);
		
		List<ContentVO> cachedChildContentVOList = (List<ContentVO>)CacheController.getCachedObjectFromAdvancedCache("childContentCache", key);
		if(cachedChildContentVOList != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached childContentVOList:" + cachedChildContentVOList.size());
			return cachedChildContentVOList;
		}
		
        List childrenVOList = new ArrayList();
        
        boolean shortTables = false;
        if(CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
        	shortTables = true;

    	String contentTypeINClause = "";
    	if(allowedContentTypeIds != null && allowedContentTypeIds.length > 0)
    	{
        	contentTypeINClause = " AND (isBranch = 1 OR " + (shortTables ? "contentTypeDefId" : "contentTypeDefinitionId") + " IN (";
        	for(int i=0; i < allowedContentTypeIds.length; i++)
        	{
        		if(i > 0)
        			contentTypeINClause += ",";
        		contentTypeINClause += "$" + (i+3);
        	}
        	contentTypeINClause += "))";
    	}
    	
    	String showDeletedItemsClause = " AND isDeleted = $2";

    	StringBuilder sb = new StringBuilder();

    	if(CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
    	{
	    	sb.append("CALL SQL select c.contId, c.name, c.publishDateTime, c.expireDateTime, c.isBranch, c.isProtected, c.isDeleted, ");
	    	sb.append("c.creator, c.contentTypeDefId, c.repositoryId, c.parentContId, ");
	    	sb.append("(");
	    	sb.append("  select min(stateId) from cmContVer cv2 where  ");
			sb.append("  cv2.contId = c.contId ");
			if(languageVOList.size() > 0)
			{
				sb.append("AND  (    ");
		
		    	int index = 0;
		    	for(LanguageVO language : languageVOList)
		    	{
		    		if(index > 0)
		        		sb.append(" OR ");
		    			
		    		sb.append("  cv2.ContVerId in ");
		        	sb.append("  (select max(ContVerId) from cmContVer where contId = c.contId AND isActive = 1 AND languageId = " + language.getLanguageId() + ")");
		        	index++;
		    	}
		
				sb.append(" )");
			}
			
			sb.append(") ");
			sb.append("AS stateId, ");
	    	sb.append("(select count(contId) from cmCont where parentContId = c.contId) AS childCount, cv.contVerId ");
	    	sb.append("from ");
	    	sb.append("cmCont c LEFT OUTER JOIN cmContVer cv ON c.contId = cv.contId ");
	    	sb.append("WHERE parentContId = $1 " + showDeletedItemsClause + contentTypeINClause + " ORDER BY c.contId asc AS org.infoglue.cms.entities.content.impl.simple.SmallStateContentImpl ");
    	}
    	else
    	{
	    	sb.append("CALL SQL select c.contentId, c.name, c.publishDateTime, c.expireDateTime, c.isBranch, c.isProtected, c.isDeleted, ");
	    	sb.append("c.creator, c.contentTypeDefinitionId, c.repositoryId, c.parentContentId, ");
	    	sb.append("(");
	    	sb.append("  select stateId from cmContentVersion cv2 where  ");
			sb.append("  cv2.contentId = c.contentId ");
			if(languageVOList.size() > 0)
			{
				sb.append("AND   (    ");
		
		    	int index = 0;
		    	for(LanguageVO language : languageVOList)
		    	{
		    		if(index > 0)
		        		sb.append(" OR ");
		    			
		    		sb.append("  cv2.contentVersionId in ");
		        	sb.append("  (select max(contentVersionId) from cmContentVersion where contentId = c.contentId AND isActive = 1 AND languageId = " + language.getLanguageId() + ")");
		        	index++;
		    	}
	
		    	sb.append(" )");
			}

			sb.append("  group by cv2.contentId order by cv2.contentVersionId desc");
			sb.append(") ");
			sb.append("AS stateId, ");
	    	sb.append("(select count(*) from cmContent where parentContentId = c.contentId) AS childCount, cv.contentVersionId ");
	    	sb.append("from ");
	    	sb.append("cmContent c LEFT OUTER JOIN cmContentVersion cv ON c.contentId = cv.contentId ");
	    	sb.append("WHERE parentContentId = $1 " + showDeletedItemsClause + contentTypeINClause + " group by c.contentId ORDER BY c.contentId asc AS org.infoglue.cms.entities.content.impl.simple.SmallStateContentImpl ");
    	}
    	
    	String SQL = sb.toString();
    	logger.info(SQL);
    	
    	//String SQL = "SELECT content FROM org.infoglue.cms.entities.content.impl.simple.SmallishContentImpl content WHERE content.parentContentId = $1 " + showDeletedItemsClause + contentTypeINClause + " ORDER BY content.contentId";
    	//logger.info("SQL:" + SQL);
    	OQLQuery oql = db.getOQLQuery(SQL);
		//OQLQuery oql = db.getOQLQuery( "SELECT content FROM org.infoglue.cms.entities.content.impl.simple.SmallishContentImpl content WHERE content.parentContentId = $1 ORDER BY content.contentId");
		oql.bind(parentContentId);
		oql.bind(showDeletedItems);
		if(allowedContentTypeIds != null)
		{
        	for(int i=0; i < allowedContentTypeIds.length; i++)
        	{
        		logger.info("allowedContentTypeIds[i]:" + allowedContentTypeIds[i]);
        		oql.bind(allowedContentTypeIds[i]);
        	}
		}

		QueryResults results = oql.execute(Database.READONLY);
		while (results.hasMore()) 
		{
			Content content = (Content)results.next();
			childrenVOList.add(content.getValueObject());
		}
		
		if(childrenVOList != null)
			CacheController.cacheObjectInAdvancedCache("childContentCache", key, childrenVOList, new String[]{CacheController.getPooledString(1, parentContentId)}, true);

		results.close();
		oql.close();

        return childrenVOList;
    } 

   	/**
   	 * This method returns a list of the children a content has.
   	 */
   	
   	public List<ContentVO> getContentChildrenVOList(Integer parentContentId, String[] allowedContentTypeIds, Database db) throws ConstraintException, SystemException, Exception
    {
   		String allowedContentTypeIdsString = "";
   		if(allowedContentTypeIds != null)
   		{
	   		for(int i=0; i < allowedContentTypeIds.length; i++)
	   			allowedContentTypeIdsString += "_" + allowedContentTypeIds[i];
   		}
   		
   		String key = "" + parentContentId + allowedContentTypeIdsString;
		if(logger.isInfoEnabled())
			logger.info("key:" + key);
		
		List<ContentVO> cachedChildContentVOList = (List<ContentVO>)CacheController.getCachedObjectFromAdvancedCache("childContentCache", key);
		if(cachedChildContentVOList != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached childContentVOList:" + cachedChildContentVOList.size());
			return cachedChildContentVOList;
		}
		
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        List childrenVOList = new ArrayList();

    	String contentTypeINClause = "";
    	if(allowedContentTypeIds != null && allowedContentTypeIds.length > 0)
    	{
        	contentTypeINClause = " AND (content.isBranch = 1 OR content.contentTypeDefinitionId IN LIST (";
        	for(int i=0; i < allowedContentTypeIds.length; i++)
        	{
        		if(i > 0)
        			contentTypeINClause += ",";
        		contentTypeINClause += "$" + (i+3);
        	}
        	contentTypeINClause += "))";
    	}
    	
    	String SQL = "SELECT content FROM org.infoglue.cms.entities.content.impl.simple.SmallishContentImpl content WHERE content.parentContentId = $1 " + contentTypeINClause + " ORDER BY content.contentId";
    	OQLQuery oql = db.getOQLQuery(SQL);
		oql.bind(parentContentId);

		if(allowedContentTypeIds != null)
		{
        	for(int i=0; i < allowedContentTypeIds.length; i++)
        	{
        		logger.info("allowedContentTypeIds[i]:" + allowedContentTypeIds[i]);
        		oql.bind(allowedContentTypeIds[i]);
        	}
		}
		
		QueryResults results = oql.execute(Database.READONLY);
		while (results.hasMore()) 
		{
			Content content = (Content)results.next();
			childrenVOList.add(content.getValueObject());
		}
		
		if(childrenVOList != null)
			CacheController.cacheObjectInAdvancedCache("childContentCache", key, childrenVOList, new String[]{CacheController.getPooledString(1, parentContentId)}, true);

		results.close();
		oql.close();
        	
   		        
        return childrenVOList;
    } 
   	
	/**
	 * This method returns the contentTypeDefinitionVO which is associated with this content.
	 */
	
	public ContentTypeDefinitionVO getContentTypeDefinition(Integer contentId) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
        
        ContentTypeDefinitionVO contentTypeDefinitionVO = null;
        
        beginTransaction(db);

        try
        {
        	ContentVO smallContentVO = getSmallContentVOWithId(contentId, db);
        	if(smallContentVO != null && smallContentVO.getContentTypeDefinitionId() != null)
	        	contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(smallContentVO.getContentTypeDefinitionId(), db);
        	
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
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
    	return contentTypeDefinitionVO;
    }        

	/**
	 * This method reurns a list of available languages for this content.
	 */
	
    public List getRepositoryLanguages(Integer contentId) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        List languages = null;
        
        beginTransaction(db);

        try
        {
            languages = getAvailableLanguagesForContentWithId(contentId, db);
            
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
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return languages;
    }        

    public List getRepositoryLanguagesEx(Integer contentId) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        List languages = null;
        
        beginTransaction(db);

        try
        {
        	Content content = getContentWithId(contentId, db);
    		if(content != null)
    		{
    			Repository repository = content.getRepository();
    			if(repository != null)
    			{
    				 languages = LanguageController.getController().getLanguageVOList(repository.getId(), db);
            
    				 //If any of the validations or setMethods reported an error, we throw them up now before create.
    				 ceb.throwIfNotEmpty();
            
    				 commitTransaction(db);
    			}
    		}
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

        return languages;
    }       
    
	/**
	 * This method returns the bound contents based on a servicebinding.
	 */
	
	public static List getBoundContents(Integer serviceBindingId) throws SystemException, Exception
	{
		List result = new ArrayList();
		
		Database db = CastorDatabaseService.getDatabase();

		beginTransaction(db);
		
		try
		{
		    result = getBoundContents(db, serviceBindingId);
		    
			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return result;
	}


	/**
	 * This method returns the bound contents based on a servicebinding.
	 */
	
	public static List getBoundContents(Database db, Integer serviceBindingId) throws SystemException, Exception
	{
		List result = new ArrayList();
		
		ServiceBinding serviceBinding = ServiceBindingController.getServiceBindingWithId(serviceBindingId, db);
        
		if(serviceBinding != null)
		{
			ServiceDefinition serviceDefinition = serviceBinding.getServiceDefinition();
			if(serviceDefinition != null)
			{
				String serviceClassName = serviceDefinition.getClassName();
				BaseService service = (BaseService)Class.forName(serviceClassName).newInstance();
        		 
				HashMap arguments = new HashMap();
				arguments.put("method", "selectContentListOnIdList");
            		
				List qualifyerList = new ArrayList();
				Collection qualifyers = serviceBinding.getBindingQualifyers();

				qualifyers = sortQualifyers(qualifyers);

				Iterator iterator = qualifyers.iterator();
				while(iterator.hasNext())
				{
					Qualifyer qualifyer = (Qualifyer)iterator.next();
					HashMap argument = new HashMap();
					argument.put(qualifyer.getName(), qualifyer.getValue());
					qualifyerList.add(argument);
				}
				arguments.put("arguments", qualifyerList);
        		
				List contents = service.selectMatchingEntities(arguments);
        		
				if(contents != null)
				{
					Iterator i = contents.iterator();
					while(i.hasNext())
					{
						ContentVO candidate = (ContentVO)i.next();
						result.add(candidate);        		
					}
				}
			}
		}
	       	  		
		return result;
	}

	
	public static List getInTransactionBoundContents(Database db, Integer serviceBindingId) throws SystemException, Exception
	{
		List result = new ArrayList();
		
		ServiceBinding serviceBinding = ServiceBindingController.getController().getReadOnlyServiceBindingWithId(serviceBindingId, db);
		//ServiceBinding serviceBinding = ServiceBindingController.getServiceBindingWithId(serviceBindingId, db);
        
		if(serviceBinding != null)
		{
			ServiceDefinition serviceDefinition = serviceBinding.getServiceDefinition();
			if(serviceDefinition != null)
			{
				String serviceClassName = serviceDefinition.getClassName();
				BaseService service = (BaseService)Class.forName(serviceClassName).newInstance();
        		 
				HashMap arguments = new HashMap();
				arguments.put("method", "selectContentListOnIdList");
            		
				List<Map> qualifyerList = new ArrayList<Map>();
				
				Collection qualifyers = serviceBinding.getBindingQualifyers();

				qualifyers = sortQualifyers(qualifyers);

				Iterator iterator = qualifyers.iterator();
				while(iterator.hasNext())
				{
					Qualifyer qualifyer = (Qualifyer)iterator.next();
					Map argument = new HashMap();
					argument.put(qualifyer.getName(), qualifyer.getValue());
					qualifyerList.add(argument);
				}
				arguments.put("arguments", qualifyerList);
        		
				try
				{
					List contents = service.selectMatchingEntities(arguments, db);
	        		
					if(contents != null)
					{
						Iterator i = contents.iterator();
						while(i.hasNext())
						{
							ContentVO candidate = (ContentVO)i.next();
							result.add(candidate);        		
						}
					}
				}
				catch (SystemException e) 
				{
					logger.warn("Error getting entities:" + e.getMessage());
					logger.info("Error getting entities:" + e.getMessage(), e);
					for(Map arg : qualifyerList)
					{
						logger.info("arg:" + arg);
					}
					//throw e;
				}
			}
		}
	       	  		
		return result;
	}


	/**
	 * This method just sorts the list of qualifyers on sortOrder.
	 */
	
	private static List sortQualifyers(Collection qualifyers)
	{
		List sortedQualifyers = new ArrayList();

		try
		{		
			Iterator iterator = qualifyers.iterator();
			while(iterator.hasNext())
			{
				Qualifyer qualifyer = (Qualifyer)iterator.next();
				int index = 0;
				Iterator sortedListIterator = sortedQualifyers.iterator();
				while(sortedListIterator.hasNext())
				{
					Qualifyer sortedQualifyer = (Qualifyer)sortedListIterator.next();
					if(sortedQualifyer.getSortOrder().intValue() > qualifyer.getSortOrder().intValue())
					{
						break;
					}
					index++;
				}
				sortedQualifyers.add(index, qualifyer);
			    					
			}
		}
		catch(Exception e)
		{
			logger.warn("The sorting of qualifyers failed:" + e.getMessage(), e);
		}
			
		return sortedQualifyers;
	}
 
	/**
	 * This method returns the contents belonging to a certain repository.
	 */
	
	public List getRepositoryContents(Integer repositoryId, Database db) throws SystemException, Exception
	{
		List contents = new ArrayList();
		
		OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.MediumContentImpl c WHERE c.repositoryId = $1 ORDER BY c.contentId");
    	oql.bind(repositoryId);
    	
    	QueryResults results = oql.execute(Database.READONLY);
		
		while(results.hasMore()) 
        {
        	MediumContentImpl content = (MediumContentImpl)results.next();
			contents.add(content);
        }

		results.close();
		oql.close();

		return contents;    	
	}

	
	/**
	 * Returns the content belonging to the specified repository and with the specified path.
	 * Note! If a folder contains more than one child with a requested name, then one of the children
	 *       will be used (non-deterministic).
	 *
	 * Example:
	 *   If we have the following repository (id=100):
	 *     <root id="1">
	 *       <news id="2">
	 *         <imported id="3">
	 *       <calendar id="4">
	 *   then:
	 *     getContentVOWithPath(100, "", true, db)              => returns content "1"
	 *     getContentVOWithPath(100, "news", true, db)          => returns content "2"
	 *     getContentVOWithPath(100, "news/imported", true, db) => returns content "3"
	 *     getContentVOWithPath(100, "news/other", true, db)    => will create a new content with the name "other" with content "2" as parent
	 *     getContentVOWithPath(100, "news/other", false, db)   => will throw an exception
	 * 
	 * @param repositoryId the repository identifier
	 * @param path the path of the content starting from the root of the repository 
	 * @param forceFolders if true then non-existing folders will be created; otherwise an exception will be thrown
	 * @param db the database to use
	 */
	public ContentVO getContentVOWithPath(Integer repositoryId, String path, boolean forceFolders, InfoGluePrincipal creator) throws SystemException, Exception 
	{
		ContentVO contentVO = null;
		
		Database db = CastorDatabaseService.getDatabase();

		beginTransaction(db);
		
		try
		{
			contentVO = getContentVOWithPath(repositoryId, path, forceFolders, creator, db);
		    
			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return contentVO;
	}
	
	/**
	 * Returns the content belonging to the specified repository and with the specified path.
	 * Note! If a folder contains more than one child with a requested name, then one of the children
	 *       will be used (non-deterministic).
	 *
	 * Example:
	 *   If we have the following repository (id=100):
	 *     <root id="1">
	 *       <news id="2">
	 *         <imported id="3">
	 *       <calendar id="4">
	 *   then:
	 *     getContentVOWithPath(100, "", true, db)              => returns content "1"
	 *     getContentVOWithPath(100, "news", true, db)          => returns content "2"
	 *     getContentVOWithPath(100, "news/imported", true, db) => returns content "3"
	 *     getContentVOWithPath(100, "news/other", true, db)    => will create a new content with the name "other" with content "2" as parent
	 *     getContentVOWithPath(100, "news/other", false, db)   => will throw an exception
	 * 
	 * @param repositoryId the repository identifier
	 * @param path the path of the content starting from the root of the repository 
	 * @param forceFolders if true then non-existing folders will be created; otherwise an exception will be thrown
	 * @param db the database to use
	 */
	public ContentVO getContentVOWithPath(Integer repositoryId, String path, boolean forceFolders, InfoGluePrincipal creator, Database db) throws SystemException, Exception 
	{
		ContentVO content = getRootContentVO(db, repositoryId, creator.getName(), false);
		final String paths[] = path.split("/");
		if(path.equals(""))
			return content;
		
		for(int i=0; i<paths.length; ++i) 
		{
			final String name = paths[i];
			if(!name.equals(""))
			{
				final ContentVO childContent = getChildVOWithName(content.getContentId(), name, db);
				if(childContent != null)
					content = childContent;
				else if(childContent == null && !forceFolders)
					throw new SystemException("There exists no content with the path [" + path + "].");
				else 
				{
					ContentVO contentVO = new ContentVO();
					contentVO.setIsBranch(Boolean.TRUE);
					contentVO.setCreatorName(creator.getName());
					contentVO.setName(name);
					Content newContent = create(db, content.getId(), null, repositoryId, contentVO);
					if(newContent != null)
						content = newContent.getValueObject();
				}
			}
		}
		return content;
	}
	

	public Content getContentWithPath(Integer repositoryId, String path, boolean forceFolders, InfoGluePrincipal creator, Database db) throws SystemException, Exception 
	{
		Content content = getRootContent(repositoryId, db);
		final String paths[] = path.split("/");
		if(path.equals(""))
			return content;
		
		for(int i=0; i<paths.length; ++i) 
		{
			final String name = paths[i];
			final Content childContent = getChildWithName(content.getId(), name, db);
			if(childContent != null)
				content = childContent;
			else if(childContent == null && !forceFolders)
				throw new SystemException("There exists no content with the path [" + path + "].");
			else 
			{
			    logger.info("   CREATE " + name);
				ContentVO contentVO = new ContentVO();
				contentVO.setIsBranch(Boolean.TRUE);
				contentVO.setCreatorName(creator.getName());
				contentVO.setName(name);
				Content newContent = create(db, content.getId(), null, repositoryId, contentVO);
				if(newContent != null)
					content = newContent;
			}
		}
		return content;
	}

	/**
	 * 
	 */
	private ContentVO getChildVOWithName(Integer parentContentId, String name, Database db) throws Exception
	{
   		String key = "" + parentContentId + name;

		ContentVO cachedChildContentVO = (ContentVO)CacheController.getCachedObjectFromAdvancedCache("childContentCache", key);
		if(cachedChildContentVO != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached cachedChildContentVO:" + cachedChildContentVO);
			return cachedChildContentVO;
		}

		ContentVO contentVO = null;
		
		OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.SmallContentImpl c WHERE c.parentContentId = $1 AND c.name = $2");
    	oql.bind(parentContentId);
    	oql.bind(name);
    	
    	QueryResults results = oql.execute(Database.READONLY);
		
		if(results.hasMore()) 
        {
			SmallContentImpl content = (SmallContentImpl)results.next();
        	contentVO = content.getValueObject();
        }

		if(contentVO != null)
			CacheController.cacheObjectInAdvancedCache("childContentCache", key, contentVO, new String[]{CacheController.getPooledString(1, parentContentId)}, true);

		results.close();
		oql.close();
		
		return contentVO;
	}

	/**
	 * 
	 */
	private Content getChildWithName(Integer parentContentId, String name, Database db) throws Exception
	{
		Content content = null;
		
		OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.ContentImpl c WHERE c.parentContent.contentId = $1 AND c.name = $2");
    	oql.bind(parentContentId);
    	oql.bind(name);
    	
    	QueryResults results = oql.execute();
		
		if(results.hasMore()) 
        {
        	content = (ContentImpl)results.next();
        }

		results.close();
		oql.close();
		
		return content;
	}

	/**
	 * 
	 */
	/*
	private Content getChildWithName(Content content, String name, Database db)
	{
		for(Iterator i=content.getChildren().iterator(); i.hasNext(); )
		{
			final Content childContent = (Content) i.next();
			if(childContent.getName().equals(name))
				return childContent;
		}
		return null;
	}
	*/

	
	/**
	 * Recursive methods to get all contentVersions of a given state under the specified parent content.
	 */ 
	
    public List getContentVOWithParentRecursive(Integer contentId, ProcessBean processBean) throws ConstraintException, SystemException
	{
    	List<ContentVO> contentVOList = new ArrayList<ContentVO>();
    	
    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
        	contentVOList = getContentVOWithParentRecursive(contentId, processBean, contentVOList, db);
    		
            commitTransaction(db);
        }
        catch(Exception e)
        {
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        } 

        return contentVOList;
	}
	
	private List getContentVOWithParentRecursive(Integer contentId, ProcessBean processBean, List resultList, Database db) throws ConstraintException, SystemException, Exception
	{
		// Get the versions of this content.
		resultList.add(getContentVOWithId(contentId, db));
		
		if (resultList.size() % 10 == 0)
		{
			if(resultList.size() > 10)
				processBean.updateLastDescription("Found " + resultList.size() + " so far...");
			else
				processBean.updateProcess("Found " + resultList.size() + " so far...");
		}
		
		// Get the children of this content and do the recursion
		List childContentList = ContentController.getContentController().getContentChildrenVOList(contentId, null, db);
		Iterator cit = childContentList.iterator();
		while (cit.hasNext())
		{
			ContentVO contentVO = (ContentVO) cit.next();
			getContentVOWithParentRecursive(contentVO.getId(), processBean, resultList, db);
		}

		return resultList;
	}

	public String getContentAttribute(Integer contentId, Integer languageId, String attributeName) throws Exception
	{
	    String attribute = "Undefined";
	    
	    ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
		
		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageId);
		if(contentVersionVO != null)
			attribute = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, attributeName, false);
		
		return attribute;
	}	

	public String getContentAttribute(Database db, Integer contentId, Integer languageId, String attributeName) throws Exception
	{
	    String attribute = "Undefined";
	    
	    ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId, db);
		
		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageId, db);
		if(contentVersionVO != null)
			attribute = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, attributeName, false);
		
		return attribute;
	}	

	public String getContentAttribute(Database db, Integer contentId, Integer languageId, String attributeName, boolean useLanguageFallBack) throws Exception
	{
	    String attribute = "Undefined";
	    
	    ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId, db);
		
		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageId, db);
		if(contentVersionVO == null && useLanguageFallBack)
		{
			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId(), db);
			contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), masterLanguageVO.getId(), db);
		}
		
		if(contentVersionVO != null)
			attribute = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, attributeName, false);
		
		return attribute;
	}	

	public String getContentAttribute(Database db, Integer contentId, Integer languageId, Integer stateId, String attributeName, boolean useLanguageFallBack) throws Exception
	{
	    String attribute = "Undefined";
	    
	    ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId, db);
		
		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageId, stateId, db);
		if(contentVersionVO == null && useLanguageFallBack)
		{
			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId(), db);
			contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), masterLanguageVO.getId(), stateId, db);
		}
		
		if(contentVersionVO != null)
			attribute = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, attributeName, false);
		
		return attribute;
	}	

	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new ContentVO();
	}
	
	
	public String getContentIdPath(Integer contentId) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		
		ContentVO contentVO = getContentVOWithId(contentId);
		sb.insert(0, contentVO.getId());
		while(contentVO != null && contentVO.getParentContentId() != null)
		{
			sb.insert(0, contentVO.getParentContentId() + ",");
			if(contentVO.getParentContentId() != null)
				contentVO = getContentVOWithId(contentVO.getParentContentId());
			else
				contentVO = null;
		}
			
		return sb.toString();
	}
	 
	/**
	 * Returns the path to, and including, the supplied content.
	 * 
	 * @param contentId the content to 
	 * 
	 * @return String the path to, and including, this content "library/library/..."
	 * 
	 */
	public String getContentPath(Integer contentId) throws ConstraintException, SystemException, Bug, Exception
    {
		return getContentPath(contentId, false, false);
    }

	/**
	 * Calls {@link #getContentPath(Integer, boolean, boolean, Database)} with <em>includeRootContent</em> and <em>includeRepositoryName</em> set
	 * to false.
	 */
	public String getContentPath(Integer contentId, Database db) throws ConstraintException, SystemException, Bug, Exception
	{
		return getContentPath(contentId, false, false, db);
	}


	/**
	 * Returns the path to, and including, the supplied content.
	 * 
	 * @param contentId the content to 
	 * 
	 * @return String the path to, and including, this content "library/library/..."
	 * 
	 */
	public String getContentPath(Integer contentId, boolean includeRootContent, boolean includeRepositoryName) throws ConstraintException, SystemException, Bug, Exception
	{
		String contentPath = null;
		Database db = null;

		try
		{
			db = CastorDatabaseService.getDatabase();
			beginTransaction(db);
			contentPath = getContentPath(contentId, includeRootContent, includeRepositoryName, db);
			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred when computing the content path so we should not complete the transaction. Message: " + e.getMessage());
			logger.warn("An error occurred when computing the content path so we should not complete the transaction.", e);
			rollbackTransaction(db);
			throw new SystemException("An error occurred when computing the content path");
		}

		return contentPath;
	}
	
	/**
	 * Returns the path to, and including, the supplied content.
	 * 
	 * @param contentId the content to 
	 * 
	 * @return String the path to, and including, this content "library/library/..."
	 * 
	 */
	public String getContentPath(Integer contentId, boolean includeRootContent, boolean includeRepositoryName, Database db) throws ConstraintException, SystemException, Bug, Exception
	{
		StringBuffer sb = new StringBuffer();

		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId, db);

		sb.insert(0, contentVO.getName());

		while (contentVO.getParentContentId() != null)
		{
			contentVO = ContentController.getContentController().getContentVOWithId(contentVO.getParentContentId(), db);

			if (includeRootContent || contentVO.getParentContentId() != null)
			{
				sb.insert(0, contentVO.getName() + "/");
			}
		}

		if (includeRepositoryName)
		{
			RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(contentVO.getRepositoryId(), db);
			if(repositoryVO != null)
				sb.insert(0, repositoryVO.getName() + "/");
		}
		
		return sb.toString();
	}



	public List<ContentVO> getRelatedContents(Database db, Integer contentId, Integer languageId, String attributeName, boolean useLanguageFallback) throws Exception
	{
		String qualifyerXML = getContentAttribute(db, contentId, languageId, attributeName, useLanguageFallback);

		return getRelatedContentsFromXML(qualifyerXML);
	}

	public List<SiteNodeVO> getRelatedSiteNodes(Database db, Integer contentId, Integer languageId, String attributeName, boolean useLanguageFallback) throws Exception
	{
		String qualifyerXML = getContentAttribute(db, contentId, languageId, attributeName, useLanguageFallback);

		return getRelatedSiteNodesFromXML(qualifyerXML);
	}

	/**
	 * This method gets the related contents from an XML.
	 */

	private String idElementStart = "<id>";
	private String idElementEnd = "</id>";
	private String idAttribute1Start = "id=\"";
	private String idAttribute1End = "\"";
	private String idAttribute2Start = "id='";
	private String idAttribute2End = "'";
	
	private List<ContentVO> getRelatedContentsFromXML(String qualifyerXML)
	{
		if(logger.isInfoEnabled())
			logger.info("qualifyerXML:" + qualifyerXML);
		
		Timer t = new Timer();
		
		List<ContentVO> relatedContentVOList = new ArrayList<ContentVO>();

		try
		{
			if(qualifyerXML != null && !qualifyerXML.equals(""))
			{
				String startExpression = idElementStart;
				String endExpression = idElementEnd;

				int idIndex = qualifyerXML.indexOf(startExpression);
				if(idIndex == -1)
				{
					startExpression = idAttribute1Start;
					idIndex = qualifyerXML.indexOf(startExpression);
					if(idIndex == -1)
					{
						startExpression = idAttribute2Start;
						endExpression = idAttribute2End;
						idIndex = qualifyerXML.indexOf(startExpression);						
					}
					else
					{
						endExpression = idAttribute1End;
					}
				}
				
				while(idIndex > -1)
				{
					int endIndex = qualifyerXML.indexOf(endExpression, idIndex + 4);
						
					String id = qualifyerXML.substring(idIndex + 4, endIndex);
					
					try
					{
						Integer contentId = new Integer(id);
						relatedContentVOList.add(getContentVOWithId(contentId));
					}
					catch(Exception e)
					{
					    logger.info("An error occurred when looking up one of the related contents FromXML:" + e.getMessage(), e);
					}

					idIndex = qualifyerXML.indexOf(startExpression, idIndex + 5);
				}
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get related contents from qualifyerXML " + qualifyerXML + ":" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
		}
		
		return relatedContentVOList;
	}

	/**
	 * This method gets the related pages from an XML.
	 */
	
	private List<SiteNodeVO> getRelatedSiteNodesFromXML(String qualifyerXML)
	{
		if(logger.isInfoEnabled())
			logger.info("qualifyerXML:" + qualifyerXML);

		Timer t = new Timer();

		List<SiteNodeVO> relatedPages = new ArrayList<SiteNodeVO>();
		
		try
		{
			if(qualifyerXML != null && !qualifyerXML.equals(""))
			{
				String startExpression = idElementStart;
				String endExpression = idElementEnd;

				int idIndex = qualifyerXML.indexOf(startExpression);
				if(idIndex == -1)
				{
					startExpression = idAttribute1Start;
					idIndex = qualifyerXML.indexOf(startExpression);
					if(idIndex == -1)
					{
						startExpression = idAttribute2Start;
						endExpression = idAttribute2End;
						idIndex = qualifyerXML.indexOf(startExpression);						
					}
					else
					{
						endExpression = idAttribute1End;
					}
				}
				
				while(idIndex > -1)
				{
					int endIndex = qualifyerXML.indexOf(endExpression, idIndex + 4);
						
					String id = qualifyerXML.substring(idIndex + 4, endIndex);
					
					try
					{
						SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(new Integer(id));
						relatedPages.add(siteNodeVO);
					}
					catch(Exception e)
					{
					    logger.info("An error occurred when looking up one of the related Pages FromXML:" + e.getMessage(), e);
					}

					idIndex = qualifyerXML.indexOf(startExpression, idIndex + 5);
				}
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get related contents from qualifyerXML " + qualifyerXML + ":" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
		}

		return relatedPages;
	}
	
	/**
	 * This method checks if there are published versions available for the contentVersion.
	 */
	
	public boolean hasPublishedVersion(Integer contentId)
	{
		boolean hasPublishedVersion = false;
		
		try
		{
			ContentVersionVO contentVersion = ContentVersionController.getContentVersionController().getLatestPublishedContentVersionVO(contentId);
			//ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestPublishedContentVersion(contentId);
			if(contentVersion != null)
			{
				hasPublishedVersion = true;
			}
		}
		catch(Exception e){}
				
		return hasPublishedVersion;
	}
	
	/**
	 * This method deletes a content and also erases all the children and all versions.
	 */
	    
    public void markForDeletion(ContentVO contentVO, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException
    {
    	markForDeletion(contentVO, infogluePrincipal, false);
    }
    
	/**
	 * This method deletes a content and also erases all the children and all versions.
	 */
	    
    public void markForDeletion(ContentVO contentVO, InfoGluePrincipal infogluePrincipal, boolean forceDelete) throws ConstraintException, SystemException
    {
    	Map<ContentVO, List<ReferenceBean>> contactPersons = new HashMap<ContentVO, List<ReferenceBean>>();
    	
	    Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {		
			markForDeletion(contentVO, db, false, false, forceDelete, infogluePrincipal, contactPersons);
	    	
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
        
		if (contactPersons.size() > 0)
		{
			logger.info("Will notifiy people about SiteNode removals. Number of nodes: " + contactPersons.size());
			Database contactDb = CastorDatabaseService.getDatabase();
			try
	        {
				beginTransaction(contactDb);
				notifyContactPersonsForContent(contactPersons, contactDb);
		    	commitTransaction(contactDb);
	        }
	        catch(Exception ex)
	        {
	        	rollbackTransaction(contactDb);
	            logger.error("An error occurred so we should not contact people about SiteNode removal. Message: " + ex.getMessage());
	            logger.warn("An error occurred so we should not contact people about SiteNode removal.", ex);
	            throw new SystemException(ex.getMessage());
	        }
		}

    }  
    

	/**
	 * This method deletes a content and also erases all the children and all versions.
	 */
	    /*
	public void markForDeletion(ContentVO contentVO, InfoGluePrincipal infogluePrincipal, Database db) throws ConstraintException, SystemException, Exception
	{
		markForDeletion(contentVO, db, false, false, false, infogluePrincipal);
	}
	*/
    
	/**
	 * This method deletes a content and also erases all the children and all versions.
	 */
	    
	public void markForDeletion(ContentVO contentVO, Database db, boolean skipRelationCheck, boolean skipServiceBindings, boolean forceDelete, InfoGluePrincipal infogluePrincipal, Map<ContentVO, List<ReferenceBean>> contactPersons) throws ConstraintException, SystemException, Exception
	{
		Content content = null;
		try
		{
			content = getContentWithId(contentVO.getContentId(), db);
		}
		catch(SystemException e)
		{
			return;
		}
		
		boolean notifyResponsibleOnReferenceChange = CmsPropertyHandler.getNotifyResponsibleOnReferenceChange();
		
		Content parent = content.getParentContent();
		if(parent != null)
		{
			Iterator childContentIterator = parent.getChildren().iterator();
			while(childContentIterator.hasNext())
			{
				Content candidate = (Content)childContentIterator.next();
			    if(candidate.getId().equals(contentVO.getContentId()))
			    {
					markForDeletionRecursive(content, childContentIterator, db, skipRelationCheck, skipServiceBindings, forceDelete, infogluePrincipal, contactPersons, notifyResponsibleOnReferenceChange);
			    }
			}
		}
		else
		{
			markForDeletionRecursive(content, null, db, skipRelationCheck, skipServiceBindings, forceDelete, infogluePrincipal, contactPersons, notifyResponsibleOnReferenceChange);
		}
	}        

	/**
	 * Recursively deletes all contents and their versions. Also updates related entities about the change.
	 */
	
    private static void markForDeletionRecursive(Content content, Iterator parentIterator, Database db, boolean skipRelationCheck, boolean skipServiceBindings, boolean forceDelete, InfoGluePrincipal infogluePrincipal, Map<ContentVO, List<ReferenceBean>> contactPersons, boolean notifyResponsibleOnReferenceChange) throws ConstraintException, SystemException, Exception
    {
        if(!skipRelationCheck && !content.getIsDeleted())
        {
	        List referenceBeanList = RegistryController.getController().getReferencingObjectsForContent(content.getId(), -1, true, false, db);
			if(referenceBeanList != null && referenceBeanList.size() > 0 && !forceDelete)
				throw new ConstraintException("ContentVersion.stateId", "3305", "<br/><br/>" + content.getName() + " (" + content.getId() + ")");
        }
        
        Collection children = content.getChildren();
		Iterator childrenIterator = children.iterator();
		while(childrenIterator.hasNext())
		{
			Content childContent = (Content)childrenIterator.next();
			markForDeletionRecursive(childContent, childrenIterator, db, skipRelationCheck, skipServiceBindings, forceDelete, infogluePrincipal, contactPersons, notifyResponsibleOnReferenceChange);   			
   		}
		
		boolean isDeletable = getIsDeletable(content, infogluePrincipal, db);
   		if(forceDelete || isDeletable)
	    {
   			List<ReferenceBean> contactList = RegistryController.getController().getReferencingObjectsForContent(content.getId(), 100, true, true);
   			//List<ReferenceBean> contactList = RegistryController.getController().deleteAllForContent(content.getId(), infoGluePrincipal, clean, CmsPropertyHandler.getOnlyShowReferenceIfLatestVersion(), db);
			if (notifyResponsibleOnReferenceChange)
			{
				if (contactList != null)
				{
					contactPersons.put(content.getValueObject(), contactList);
				}
			}
			//ContentVersionController.getContentVersionController().deleteVersionsForContent(content, db, forceDelete, infogluePrincipal);    	
			
			//if(!skipServiceBindings)
   			//    ServiceBindingController.deleteServiceBindingsReferencingContent(content, db);
			
   			//if(parentIterator != null) 
			//    parentIterator.remove();
	    	
	    	content.setIsDeleted(true);
	    }
	    else
    	{
    		throw new ConstraintException("ContentVersion.stateId", "3300", content.getName());
    	}			
    }        

    /**
	 * This method restored a content.
	 */
	    
    public void restoreContent(Integer contentId, InfoGluePrincipal infogluePrincipal, Database db) throws ConstraintException, SystemException
    {
		Content content = getContentWithId(contentId, db);
		content.setIsDeleted(false);
    }  

    /**
	 * This method restored a content.
	 */
	    
    public void restoreContent(Integer contentId, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException
    {
	    Database db = CastorDatabaseService.getDatabase();
        
	    beginTransaction(db);
		
        try
        {
			Content content = getContentWithId(contentId, db);
			content.setIsDeleted(false);
			
			while(content.getParentContent() != null && content.getParentContent().getIsDeleted())
			{
				content = content.getParentContent();
				content.setIsDeleted(false);
			}
	    	
	    	commitTransaction(db);
        }
        catch(Exception e)
        {
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }  

	public List<ContentVO> getContentVOListMarkedForDeletion(Integer repositoryId, InfoGluePrincipal infoGluePrincipal) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();
		
		List<ContentVO> contentVOListMarkedForDeletion = new ArrayList<ContentVO>();
		
		try 
		{
			beginTransaction(db);

			String sql = "SELECT c FROM org.infoglue.cms.entities.content.impl.simple.ContentImpl c WHERE c.isDeleted = $1 AND is_defined(c.repository.name) ORDER BY c.contentId";
			if(repositoryId != null && repositoryId != -1)
				sql = "SELECT c FROM org.infoglue.cms.entities.content.impl.simple.ContentImpl c WHERE c.isDeleted = $1 AND is_defined(c.repository.name) AND c.repository = $2 ORDER BY c.contentId";
			
			OQLQuery oql = db.getOQLQuery(sql);
			oql.bind(true);
			if(repositoryId != null && repositoryId != -1)
				oql.bind(repositoryId);
			
			QueryResults results = oql.execute(Database.READONLY);
			while(results.hasMore()) 
            {
				Content content = (Content)results.next();
				Integer contentRepositoryId = content.getRepositoryId();
				Integer contentId = content.getContentId();

				if((AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "Repository.Read", contentRepositoryId.toString()) && AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "Repository.Write", contentRepositoryId.toString()))
					&& (AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "Content.Read", contentId.toString()) && AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "Content.Write", contentId.toString())))
				{
					content.getValueObject().getExtraProperties().put("repositoryMarkedForDeletion", content.getRepository().getIsDeleted());
					contentVOListMarkedForDeletion.add(content.getValueObject());
				}
			}
            
			results.close();
			oql.close();

			commitTransaction(db);
		}
		catch ( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of deleted pages. Reason:" + e.getMessage(), e);			
		}
		
		return contentVOListMarkedForDeletion;		
	}
	
	/**
	 * Returns a repository list marked for deletion.
	 */
	
	public Set<ContentVO> getContentVOListLastModifiedByPincipal(InfoGluePrincipal principal, String[] excludedContentTypes) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();
		
		Set<ContentVO> contentVOList = new HashSet<ContentVO>();
		
		try 
		{
			beginTransaction(db);
		
			OQLQuery oql = db.getOQLQuery("SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl cv WHERE cv.versionModifier = $1 ORDER BY cv.modifiedDateTime DESC LIMIT $2");
			oql.bind(principal.getName());
			oql.bind(50);

			QueryResults results = oql.execute(Database.READONLY);
			while(results.hasMore()) 
            {
				SmallestContentVersionImpl contentVersion = (SmallestContentVersionImpl)results.next();
				ContentVO contentVO = getContentVOWithId(contentVersion.getValueObject().getContentId(), db);
				boolean isValid = true;
				for(int i=0; i<excludedContentTypes.length; i++)
				{
					String contentTypeDefinitionNameToExclude = excludedContentTypes[i];
					ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName(contentTypeDefinitionNameToExclude, db);
					if(contentVO.getContentTypeDefinitionId().equals(ctdVO.getId()))
					{
						isValid = false;
						break;
					}
				}
				
				if(isValid)
					contentVOList.add(contentVO);
            }
            
			results.close();
			oql.close();

			commitTransaction(db);
		}
		catch ( Exception e)		
		{
			e.printStackTrace();
			throw new SystemException("An error occurred when we tried to fetch a list contents last modified by the user. Reason:" + e.getMessage(), e);			
		}
		
		return contentVOList;		
	}

	public List<ContentVO> getUpcomingExpiringContents(int numberOfWeeks) throws Exception
	{
		List<ContentVO> contentVOList = new ArrayList<ContentVO>();

		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
    		OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.SmallContentImpl c WHERE " +
    				"c.expireDateTime > $1 AND c.expireDateTime < $2 AND c.publishDateTime < $3 ORDER BY c.contentId");

        	Calendar now = Calendar.getInstance();
        	Date currentDate = now.getTime();
        	oql.bind(currentDate);
        	now.add(Calendar.DAY_OF_YEAR, numberOfWeeks);
        	Date futureDate = now.getTime();
           	oql.bind(futureDate);
           	oql.bind(currentDate);

        	QueryResults results = oql.execute(Database.READONLY);
    		while(results.hasMore()) 
            {
    			Content content = (ContentImpl)results.next();
    			contentVOList.add(content.getValueObject());
            }

    		results.close();
    		oql.close();
        	
            commitTransaction(db);
        }
        catch(Exception e)
        {
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
        return contentVOList;
	}

	public List<ContentVO> getUpcomingExpiringContents(int numberOfDays, InfoGluePrincipal principal, String[] excludedContentTypes) throws Exception
	{
		List<ContentVO> contentVOList = new ArrayList<ContentVO>();

		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
    		OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.MediumContentImpl c WHERE " +
        			"c.expireDateTime > $1 AND c.expireDateTime < $2 AND c.publishDateTime < $3 AND c.contentVersions.versionModifier = $4 ORDER BY c.contentId");
    		
        	Calendar now = Calendar.getInstance();
        	Date currentDate = now.getTime();
        	oql.bind(currentDate);
        	now.add(Calendar.DAY_OF_YEAR, numberOfDays);
        	Date futureDate = now.getTime();
        	oql.bind(futureDate);
           	oql.bind(currentDate);
           	oql.bind(principal.getName());

        	QueryResults results = oql.execute(Database.READONLY);
    		while(results.hasMore()) 
            {
    			Content content = (ContentImpl)results.next();
    			
				boolean isValid = true;
				for(int i=0; i<excludedContentTypes.length; i++)
				{
					String contentTypeDefinitionNameToExclude = excludedContentTypes[i];
					ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName(contentTypeDefinitionNameToExclude, db);
					if(content.getContentTypeDefinitionId().equals(ctdVO.getId()))
					{
						isValid = false;
						break;
					}
				}
				
				if(isValid)
					contentVOList.add(content.getValueObject());
            }

    		results.close();
    		oql.close();
        	
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return contentVOList;
	}

	private Map<String, List<ReferenceBean>> groupByContactPerson(List<ReferenceBean> contactPersons)
	{
		Map<String, List<ReferenceBean>> result = new HashMap<String, List<ReferenceBean>>();

		for (ReferenceBean referenceBean : contactPersons)
		{
			if (referenceBean.getContactPersonEmail() == null || referenceBean.getContactPersonEmail().equals(""))
			{
				continue;
			}
			List<ReferenceBean> personsList = result.get(referenceBean.getContactPersonEmail());
			if (personsList == null)
			{
				personsList = new ArrayList<ReferenceBean>();
				result.put(referenceBean.getContactPersonEmail(), personsList);
			}
			personsList.add(referenceBean);
		}

		return result;
	}

	private Map<String, Map<ContentVO, List<ReferenceBean>>> groupByContactPerson(Map<ContentVO, List<ReferenceBean>> contactPersons)
	{
		Map<String, Map<ContentVO, List<ReferenceBean>>> result = new HashMap<String, Map<ContentVO,  List<ReferenceBean>>>();
		for (Map.Entry<ContentVO, List<ReferenceBean>> entry : contactPersons.entrySet())
		{
			ContentVO contentVO = entry.getKey();
			Map<String, List<ReferenceBean>> referencesByContact = groupByContactPerson(entry.getValue());
			for (Map.Entry<String, List<ReferenceBean>> contactsForSiteNode : referencesByContact.entrySet())
			{
				String contactPerson = contactsForSiteNode.getKey();
				Map<ContentVO,  List<ReferenceBean>> value = result.get(contactPerson);
				if (value == null)
				{
					value = new HashMap<ContentVO,  List<ReferenceBean>>();
					result.put(contactPerson, value);
				}
				value.put(contentVO, contactsForSiteNode.getValue());
			}
		}
		return result;
	}
	
    private void notifyContactPersonsForContent(ContentVO contentVO, List<ReferenceBean> contacts, Database db) throws SystemException, Exception
    {
    	notifyContactPersonsForContent(Collections.singletonMap(contentVO, contacts), db);
    }

    private void notifyContactPersonsForContent(Map<ContentVO, List<ReferenceBean>> contacts, Database db) throws SystemException, Exception
    {
    	Map<String, Map<ContentVO, List<ReferenceBean>>> contactMap = groupByContactPerson(contacts);

    	if (logger.isInfoEnabled())
    	{
    		logger.info("Will notify people about registry change. " + contactMap);
    	}

    	String registryContactMailLanguage = CmsPropertyHandler.getRegistryContactMailLanguage();
    	Locale locale = new Locale(registryContactMailLanguage);

		try
		{
			String from = CmsPropertyHandler.getSystemEmailSender();
    		String subject = getLocalizedString(locale, "tool.contenttool.registry.notificationEmail.subject");

    		// This loop iterate once for each contact person
			for (Map.Entry<String, Map<ContentVO, List<ReferenceBean>>> entry : contactMap.entrySet())
			{
				String contactPersonEmail = entry.getKey();
				Set<ContentVO> contentsForPerson = entry.getValue().keySet();
				Map<ContentVO, List<ReferenceBean>> affectedNodes = entry.getValue();
	    		StringBuilder mailContent = new StringBuilder();

	    		mailContent.append(getLocalizedString(locale, "tool.contenttool.registry.notificationEmail.intro"));
	    		mailContent.append("<p style=\"color:black;\">");
	    		mailContent.append(getLocalizedString(locale, "tool.contenttool.registry.notificationEmail.siteNodeLabel"));
	    		mailContent.append("<ul>");
	    		for (ContentVO contentVO : contentsForPerson)
	    		{
					mailContent.append("<li>");
					// Putting a-tags around each entry will prevent email clients from trying to linkify the entries
					mailContent.append("<a style=\"color:black;\">");
					mailContent.append(getContentPath(contentVO.getId(), false, true, db));
					mailContent.append("</a>");
					mailContent.append("</li>");
	    		}
				mailContent.append("</ul>");
				mailContent.append("</p>");

				boolean hasInformation = false;
		    	for (Map.Entry<ContentVO, List<ReferenceBean>> affectedNode : affectedNodes.entrySet())
		    	{
					StringBuilder sb = new StringBuilder();
					sb.append("<h4 style=\"margin-bottom:4px;color:black;\">");
					sb.append("<a style=\"color:black;\">");
					sb.append(getContentPath(affectedNode.getKey().getId(), false, true, db));
					sb.append("</a>");

					String path;
					String url;
					StringBuilder siteNodeBuilder = new StringBuilder();
					StringBuilder contentBuilder = new StringBuilder();
					for (ReferenceBean reference : affectedNode.getValue())
					{
						if (reference.getPath() != null && !reference.getPath().equals(""))
						{
							path = reference.getPath();
						}
						else
						{
							path = reference.getName();
						}

						if (reference.getReferencingCompletingObject().getClass().getName().indexOf("Content") != -1)
						{
							Integer languageId;
							if (reference.getVersions().size() == 0)
							{
								if (reference.getReferencingCompletingObject() instanceof ContentVO)
								{
									languageId = LanguageController.getController().getMasterLanguage(((ContentVO)reference.getReferencingCompletingObject()).getRepositoryId(), db).getLanguageId();
								}
								else
								{
									languageId = ((LanguageVO)LanguageController.getController().getLanguageVOList(db).get(0)).getLanguageId();
								}
								url = CmsPropertyHandler.getCmsFullBaseUrl() + "/Admin.action?contentId=" + ((ContentVO)reference.getReferencingCompletingObject()).getContentId() + "&languageId=" + languageId;
								contentBuilder.append("<li><a href=\"" + url + "\">" + path + "</a></li>");
							}
							else
							{
								LanguageVO languageVO;
								for(ReferenceVersionBean versionBean : reference.getVersions())
								{
									ContentVersionVO version = (ContentVersionVO)versionBean.getReferencingObject();
									languageId = version.getLanguageId();
									languageVO = LanguageController.getController().getLanguageVOWithId(languageId, db);
									url = CmsPropertyHandler.getCmsFullBaseUrl() + "/Admin.action?contentId=" + ((ContentVO)reference.getReferencingCompletingObject()).getContentId() + "&languageId=" + languageId;
									contentBuilder.append("<li><a href=\"" + url + "\">" + path + "</a>" + (languageVO == null ? "" : " (" + languageVO.getLocalizedDisplayLanguage() + ")") + "</li>");
								}
							}
						}
						else
						{
							url = CmsPropertyHandler.getCmsFullBaseUrl() + "/Admin.action?siteNodeId=" + ((SiteNodeVO)reference.getReferencingCompletingObject()).getSiteNodeId();
							siteNodeBuilder.append("<li><a href=\"" + url + "\">" + path + "</a></li>");
						}
					}
					if (contentBuilder.length() > 0)
					{
						hasInformation = true;
						sb.append(getLocalizedString(locale, "tool.contenttool.registry.notificationEmail.listHeader.content"));
						sb.append("<ul>");
						sb.append(contentBuilder);
						sb.append("</ul>");
					}
					if (siteNodeBuilder.length() > 0)
					{
						hasInformation = true;
						sb.append(getLocalizedString(locale, "tool.contenttool.registry.notificationEmail.listHeader.siteNode"));
						sb.append("<ul>");
						sb.append(siteNodeBuilder);
						sb.append("</ul>");
					}
					sb.append("</p>");
					mailContent.append(sb);
				} // end loop: one SiteNode for one contact person

		    	mailContent.append(getLocalizedString(locale, "tool.contenttool.registry.notificationEmail.footer"));
				if (hasInformation)
				{
					logger.debug("Sending notification email to: " + contactPersonEmail);
					MailServiceFactory.getService().sendEmail("text/html", from, contactPersonEmail, null, null, null, null, subject, mailContent.toString(), "utf-8");
				}
				else
				{
					logger.warn("No Contents or SiteNodes were found for the given person. This is very strange. Contact person: " + contactPersonEmail + ", SiteNode.ids: " + contacts.keySet());
				}
			} // end-loop: contact person
    	}
    	catch (Exception ex)
    	{
    		logger.error("Failed to generate email for contact person notfication. Message: " + ex.getMessage() + ". Type: " + ex.getClass());
			logger.warn("Failed to generate email for contact person notfication.", ex);
			throw ex;
    	}
    }

	public boolean getDoesContentExist(Integer contentId) throws Exception
	{
		boolean exists = true;
		
		Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {	
			db.load(SmallContentImpl.class, contentId, Database.READONLY);
	    	commitTransaction(db);
        }
        catch(ObjectNotFoundException onfe)
        {
        	exists = false;
        	if(logger.isInfoEnabled())
        		logger.info("An error occurred so we should not complete the transaction:" + onfe, onfe);
            rollbackTransaction(db);
        }  
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
        }  
        
        return exists;
	}
}
