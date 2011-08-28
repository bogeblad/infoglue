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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.applications.contenttool.wizards.actions.CreateContentWizardInfoBean;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinition;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.management.ServiceDefinition;
import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.structure.Qualifyer;
import org.infoglue.cms.entities.structure.ServiceBinding;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.services.BaseService;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
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

	public ContentVO getContentVOWithId(Integer contentId) throws SystemException, Bug
    {
    	return (ContentVO) getVOWithId(SmallContentImpl.class, contentId);
    } 

	public ContentVO getContentVOWithId(Integer contentId, Database db) throws SystemException, Bug
    {
    	return (ContentVO) getVOWithId(SmallContentImpl.class, contentId, db);
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

    public Content getReadOnlyMediumContentWithId(Integer contentId, Database db) throws SystemException, Bug
    {
		return (Content) getObjectWithIdAsReadOnly(MediumContentImpl.class, contentId, db);
    }
    
    
    public List getContentVOList() throws SystemException, Bug
    {
        return getAllVOObjects(ContentImpl.class, "contentId");
    }
	
    
	public List<ContentVO> getContentVOListMarkedForDeletion(Integer repositoryId) throws SystemException, Bug
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
				content.getValueObject().getExtraProperties().put("repositoryMarkedForDeletion", content.getRepository().getIsDeleted());
				contentVOListMarkedForDeletion.add(content.getValueObject());
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
				contentVersionVO = ContentVersionController.getContentVersionController().create(content.getContentId(), languageId, contentVersionVO, null, db).getValueObject();
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
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
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
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return content.getValueObject();
    }

	/**
	 * This method creates a new content-entity and references the entities it should know about.
	 * As castor is lousy at this in my opinion we also add the new entity to the surrounding entities.
	 */
	    
    public /*synchronized*/ Content create(Database db, Integer parentContentId, Integer contentTypeDefinitionId, Integer repositoryId, ContentVO contentVO) throws ConstraintException, SystemException, Exception
    {
	    Content content = null;
		
        try
        {            
            Content parentContent = null;
          	ContentTypeDefinition contentTypeDefinition = null;

            if(parentContentId != null)
            {
            	parentContent = getContentWithId(parentContentId, db);
            	
            	if(repositoryId == null)
					repositoryId = parentContent.getRepository().getRepositoryId();	
            }
            
            if(contentTypeDefinitionId != null)
            	contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithId(contentTypeDefinitionId, db);

            Repository repository = RepositoryController.getController().getRepositoryWithId(repositoryId, db);

			/*
        	synchronized (controller)
			{
        		//db.lock(parentContent);
			*/
	            content = new ContentImpl();
	            content.setValueObject(contentVO);
	            content.setParentContent((ContentImpl)parentContent);
	            content.setRepository((RepositoryImpl)repository);
	            content.setContentTypeDefinition((ContentTypeDefinitionImpl)contentTypeDefinition);
	            
				db.create(content);
				
				//Now we add the content to the knowledge of the related entities.
				if(parentContent != null)
				{
					parentContent.getChildren().add(content);
					parentContent.setIsBranch(new Boolean(true));
				}
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
	 * This method creates a new content-entity and references the entities it should know about.
	 * As castor is lousy at this in my opinion we also add the new entity to the surrounding entities.
	 */
	    
    public Content create(Database db, Integer parentContentId, Integer contentTypeDefinitionId, Repository repository, ContentVO contentVO) throws ConstraintException, SystemException, Exception
    {
	    Content content = null;
		
        try
        {            
            Content parentContent = null;
          	ContentTypeDefinition contentTypeDefinition = null;

            if(parentContentId != null)
            {
            	parentContent = getContentWithId(parentContentId, db);
            }
            
            if(contentTypeDefinitionId != null)
            	contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithId(contentTypeDefinitionId, db);

            content = new ContentImpl();
            content.setValueObject(contentVO);
            content.setParentContent((ContentImpl)parentContent);
            content.setRepository((RepositoryImpl)repository);
            content.setContentTypeDefinition((ContentTypeDefinitionImpl)contentTypeDefinition);
            
			db.create(content);
				
			//Now we add the content to the knowledge of the related entities.
			if(parentContent != null)
			{
				parentContent.getChildren().add(content);
				parentContent.setIsBranch(new Boolean(true));
			}
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
	    	delete(contentVO, db, false, false, forceDelete, infogluePrincipal);
	    	
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
	        List referenceBeanList = RegistryController.getController().getReferencingObjectsForContent(content.getId(), -1, true, db);
			if(referenceBeanList != null && referenceBeanList.size() > 0 && !forceDelete)
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
	    Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {		
			markForDeletion(contentVO, db, false, false, forceDelete, infogluePrincipal);
	    	
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

    }  
    

	/**
	 * This method deletes a content and also erases all the children and all versions.
	 */
	    
	public void markForDeletion(ContentVO contentVO, InfoGluePrincipal infogluePrincipal, Database db) throws ConstraintException, SystemException, Exception
	{
		markForDeletion(contentVO, db, false, false, false, infogluePrincipal);
	}
	
	/**
	 * This method deletes a content and also erases all the children and all versions.
	 */
	    
	public void markForDeletion(ContentVO contentVO, Database db, boolean skipRelationCheck, boolean skipServiceBindings, boolean forceDelete, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException, Exception
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
			Iterator childContentIterator = parent.getChildren().iterator();
			while(childContentIterator.hasNext())
			{
			    Content candidate = (Content)childContentIterator.next();
			    if(candidate.getId().equals(contentVO.getContentId()))
			    {
			    	markForDeletionRecursive(content, childContentIterator, db, skipRelationCheck, skipServiceBindings, forceDelete, infogluePrincipal);
			    }
			}
		}
		else
		{
			markForDeletionRecursive(content, null, db, skipRelationCheck, skipServiceBindings, forceDelete, infogluePrincipal);
		}
	}        

	/**
	 * Recursively deletes all contents and their versions. Also updates related entities about the change.
	 */
	
    private static void markForDeletionRecursive(Content content, Iterator parentIterator, Database db, boolean skipRelationCheck, boolean skipServiceBindings, boolean forceDelete, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException, Exception
    {
        if(!skipRelationCheck)
        {
	        List referenceBeanList = RegistryController.getController().getReferencingObjectsForContent(content.getId(), -1, true, db);
			if(referenceBeanList != null && referenceBeanList.size() > 0 && !forceDelete)
				throw new ConstraintException("ContentVersion.stateId", "3305", "<br/><br/>" + content.getName() + " (" + content.getId() + ")");
        }
        
        Collection children = content.getChildren();
		Iterator childrenIterator = children.iterator();
		while(childrenIterator.hasNext())
		{
			Content childContent = (Content)childrenIterator.next();
			markForDeletionRecursive(childContent, childrenIterator, db, skipRelationCheck, skipServiceBindings, forceDelete, infogluePrincipal);   			
   		}
		
		boolean isDeletable = getIsDeletable(content, infogluePrincipal, db);
   		if(forceDelete || isDeletable)
	    {
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
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return content.getValueObject();
    }        

	public List<LanguageVO> getAvailableLanguagVOListForContentWithId(Integer contentId, Database db) throws ConstraintException, SystemException, Exception
	{
		//List<LanguageVO> availableLanguageVOList = new ArrayList<LanguageVO>();
		
		Content content = getContentWithId(contentId, db);
		List<LanguageVO> availableLanguageVOList = LanguageController.getController().getLanguageVOList(content.getRepositoryId(), db);
		
		/*
		if(content != null)
		{
			Repository repository = content.getRepository();
			if(repository != null)
			{
			    List availableRepositoryLanguageList = RepositoryLanguageController.getController().getRepositoryLanguageListWithRepositoryId(repository.getId(), db);
				Iterator i = availableRepositoryLanguageList.iterator();
				while(i.hasNext())
				{
					RepositoryLanguage repositoryLanguage = (RepositoryLanguage)i.next();
					availableLanguageVOList.add(repositoryLanguage.getLanguage().getValueObject());
				}
			}
		}
		*/
		
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
	
    public static ContentVO getParentContent(Integer contentId) throws SystemException, Bug
    {
        Database db = CastorDatabaseService.getDatabase();
		ContentVO parentContentVO = null;
		
        beginTransaction(db);

        try
        {
			Content content = (Content) getObjectWithId(ContentImpl.class, contentId, db);
			Content parent = content.getParentContent();
			if(parent != null)
				parentContentVO = parent.getValueObject();
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
		return parentContentVO;    	
    }

	/**
	 * This method returns the value-object of the parent of a specific content. 
	 */
	
    public static ContentVO getParentContent(Integer contentId, Database db) throws SystemException, Bug
    {
		ContentVO parentContentVO = null;
		
		Content content = (Content) getObjectWithId(ContentImpl.class, contentId, db);
		logger.info("CONTENT:" + content.getName());
		Content parent = content.getParentContent();
		if(parent != null)
			parentContentVO = parent.getValueObject();

		return parentContentVO;    	
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
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
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
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
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
            rollbackTransaction(db);
            throw new SystemException(ce.getMessage());
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
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

        Content content = null;
		Content newParentContent = null;
		Content oldParentContent = null;

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
		
		content          = getContentWithId(contentVO.getContentId(), db);
        oldParentContent = content.getParentContent();
        newParentContent = getContentWithId(newParentContentId, db);
                    
        if(oldParentContent.getId().intValue() == newParentContentId.intValue())
        {
        	logger.warn("You cannot specify the same folder as it originally was located in......");
        	throw new ConstraintException("Content.parentContentId", "3304");
        }

		Content tempContent = newParentContent.getParentContent();
		while(tempContent != null)
		{
			if(tempContent.getId().intValue() == content.getId().intValue())
			{
				logger.warn("You cannot move the content to a child under it......");
        		throw new ConstraintException("Content.parentContentId", "3302");
			}
			tempContent = tempContent.getParentContent();
		}				            
        
        oldParentContent.getChildren().remove(content);
        content.setParentContent((ContentImpl)newParentContent);
        
        changeRepositoryRecursive(content, newParentContent.getRepository());
        //content.setRepository(newParentContent.getRepository());
        newParentContent.getChildren().add(content);
        
        //If any of the validations or setMethods reported an error, we throw them up now before create.
        ceb.throwIfNotEmpty();
    }   

	/**
	 * Recursively sets the contents repositoryId.
	 * @param content
	 * @param newRepository
	 */

	private void changeRepositoryRecursive(Content content, Repository newRepository)
	{
	    if(content.getRepository().getId().intValue() != newRepository.getId().intValue())
	    {
		    content.setRepository((RepositoryImpl)newRepository);
		    Iterator childContentsIterator = content.getChildren().iterator();
		    while(childContentsIterator.hasNext())
		    {
		        Content childContent = (Content)childContentsIterator.next();
		        changeRepositoryRecursive(childContent, newRepository);
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
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
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
	 * The input is a list of hashmaps.
	 */
	
	protected List getContentVOListByContentTypeNames(List arguments) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();
	
		List contents = new ArrayList();
		
        beginTransaction(db);

        try
        {
			Iterator i = arguments.iterator();
	    	while(i.hasNext())
	    	{
		        HashMap argument = (HashMap)i.next();
	    		String contentTypeDefinitionName = (String)argument.get("contentTypeDefinitionName");
	    		
				//OQLQuery oql = db.getOQLQuery("CALL SQL SELECT c.contentId, c.name, c.publishDateTime, c.expireDateTime, c.isBranch, c.isProtected, c.creator, ctd.contentTypeDefinitionId, r.repositoryId FROM cmContent c, cmContentTypeDefinition ctd, cmRepository r where c.repositoryId = r.repositoryId AND c.contentTypeDefinitionId = ctd.contentTypeDefinitionId AND ctd.name = $1 AS org.infoglue.cms.entities.content.impl.simple.SmallContentImpl");
				//OQLQuery oql = db.getOQLQuery("CALL SQL SELECT contentId, name FROM cmContent c, cmContentTypeDefinition ctd WHERE c.contentTypeDefinitionId = ctd.contentTypeDefinitionId AND ctd.name = $1 AS org.infoglue.cms.entities.content.impl.simple.ContentImpl");
	    		OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.MediumContentImpl c WHERE c.contentTypeDefinition.name = $1 AND c.isDeleted = $2 ORDER BY c.contentId");
	        	oql.bind(contentTypeDefinitionName);
	        	oql.bind(false);
	        	
	        	QueryResults results = oql.execute(Database.ReadOnly);
				
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
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
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
	        HashMap argument = (HashMap)i.next();
    		String contentTypeDefinitionName = (String)argument.get("contentTypeDefinitionName");
			//OQLQuery oql = db.getOQLQuery("CALL SQL SELECT c.contentId, c.name, c.publishDateTime, c.expireDateTime, c.isBranch, c.isProtected, c.creator, ctd.contentTypeDefinitionId, r.repositoryId FROM cmContent c, cmContentTypeDefinition ctd, cmRepository r where c.repositoryId = r.repositoryId AND c.contentTypeDefinitionId = ctd.contentTypeDefinitionId AND ctd.name = $1 AS org.infoglue.cms.entities.content.impl.simple.SmallContentImpl");
			//OQLQuery oql = db.getOQLQuery("CALL SQL SELECT contentId, name FROM cmContent c, cmContentTypeDefinition ctd WHERE c.contentTypeDefinitionId = ctd.contentTypeDefinitionId AND ctd.name = $1 AS org.infoglue.cms.entities.content.impl.simple.ContentImpl");
    		OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.MediumContentImpl c WHERE c.contentTypeDefinition.name = $1 AND c.isDeleted = $2 ORDER BY c.contentId");
        	oql.bind(contentTypeDefinitionName);
        	oql.bind(false);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			while(results.hasMore()) 
            {
            	MediumContentImpl content = (MediumContentImpl)results.next();
				contents.add(content.getValueObject());
            }
			
			results.close();
			oql.close();
	   	}
    	
		return contents;    	
	}

   	
   	/**
	 * This method fetches the root content for a particular repository.
	 * If there is no such content we create one as all repositories need one to work.
	 */
	        
   	public ContentVO getRootContentVO(Integer repositoryId, String userName) throws ConstraintException, SystemException
   	{
   		if(repositoryId == null || repositoryId.intValue() < 1)
   			return null;
   		
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        Content content = null;

        beginTransaction(db);

        try
        {
            logger.info("Fetching the root content for the repository " + repositoryId);
			//OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.content.impl.simple.ContentImpl c WHERE is_undefined(c.parentContent) AND c.repository.repositoryId = $1");
			OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.content.impl.simple.SmallContentImpl c WHERE is_undefined(c.parentContentId) AND c.repositoryId = $1");
			oql.bind(repositoryId);
			
        	QueryResults results = oql.execute(Database.ReadOnly);			
			if (results.hasMore()) 
            {
            	content = (Content)results.next();
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
            	content = create(db, null, null, repositoryId, rootContentVO);
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
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return (content == null) ? null : content.getValueObject();
   	}


   	
	/**
	 * This method fetches the root content for a particular repository.
	 * If there is no such content we create one as all repositories need one to work.
	 */
	        
	public ContentVO getRootContentVO(Integer repositoryId, String userName, boolean createIfNonExisting) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		Content content = null;

		beginTransaction(db);

		try
		{
		    content = getRootContent(db, repositoryId, userName, createIfNonExisting);
            
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

		return (content == null) ? null : content.getValueObject();
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
			
		QueryResults results = oql.execute(Database.ReadOnly);			
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
	 * This method fetches the root content for a particular repository within a transaction.
	 * If there is no such content we create one as all repositories need one to work.
	 */
	        
	public Content createRootContent(Database db, Repository repository, String userName) throws ConstraintException, SystemException, Exception
	{
		Content content = null;

		ContentVO rootContentVO = new ContentVO();
		rootContentVO.setCreatorName(userName);
		rootContentVO.setName(repository.getName());
		rootContentVO.setIsBranch(new Boolean(true));
		content = create(db, null, null, repository, rootContentVO);
		
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
/*   	
   	public List getContentChildrenVOList(Integer parentContentId) throws ConstraintException, SystemException
    {
   		String key = "" + parentContentId;
		logger.info("key:" + key);
		List cachedChildContentVOList = (List)CacheController.getCachedObject("childContentCache", key);
		if(cachedChildContentVOList != null)
		{
			logger.info("There was an cached childContentVOList:" + cachedChildContentVOList.size());
			return cachedChildContentVOList;
		}
		
		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        List childrenVOList = null;

        beginTransaction(db);

        try
        {
            Content content = getReadOnlyContentWithId(parentContentId, db);
            Collection children = content.getChildren();
        	childrenVOList = ContentController.toVOList(children);
        	
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
        
		CacheController.cacheObject("childContentCache", key, childrenVOList);
        
        return childrenVOList;
    } 
*/
	
   	/**
   	 * This method returns a list of the children a content has.
   	 */
   	
   	public List<ContentVO> getContentChildrenVOList(Integer parentContentId, String[] allowedContentTypeIds, Boolean showDeletedItems) throws ConstraintException, SystemException
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
		
		List<ContentVO> cachedChildContentVOList = (List<ContentVO>)CacheController.getCachedObject("childContentCache", key);
		if(cachedChildContentVOList != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached childContentVOList:" + cachedChildContentVOList.size());
			return cachedChildContentVOList;
		}
		
		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        List childrenVOList = new ArrayList();

        beginTransaction(db);

   		Timer t = new Timer();
   		
   		/*
        try
        {
            Content content = getReadOnlyContentWithId(parentContentId, db);
            Collection children = content.getChildren();
        	childrenVOList = ContentController.toVOList(children);
        	logger.info("childrenVOList under:" + content.getName() + ":" + childrenVOList.size());
        	
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
		*/
   		
        try
        {
        	String contentTypeINClause = "";
        	if(allowedContentTypeIds != null && allowedContentTypeIds.length > 0)
        	{
	        	contentTypeINClause = " AND (content.isBranch = true OR content.contentTypeDefinitionId IN LIST (";
	        	for(int i=0; i < allowedContentTypeIds.length; i++)
	        	{
	        		if(i > 0)
	        			contentTypeINClause += ",";
	        		contentTypeINClause += "$" + (i+3);
	        	}
	        	contentTypeINClause += "))";
        	}
        	
        	String showDeletedItemsClause = " AND content.isDeleted = $2";
        	
        	String SQL = "SELECT content FROM org.infoglue.cms.entities.content.impl.simple.SmallishContentImpl content WHERE content.parentContentId = $1 " + showDeletedItemsClause + contentTypeINClause + " ORDER BY content.contentId";
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
    		
    		QueryResults results = oql.execute(Database.ReadOnly);
    		while (results.hasMore()) 
    		{
    			Content content = (Content)results.next();
    			childrenVOList.add(content.getValueObject());
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
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
   		
		CacheController.cacheObject("childContentCache", key, childrenVOList);
        
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

        	/*
	        Content content = getReadOnlyMediumContentWithId(contentId, db);
        	if(content != null && content.getContentTypeDefinition() != null)
	        	contentTypeDefinitionVO = content.getContentTypeDefinition().getValueObject();
    		*/
        	
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
            languages = LanguageController.getController().getLanguageVOList(contentId, db);
            
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
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
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
	
	public List getRepositoryMediumContents(Integer repositoryId, Database db) throws SystemException, Exception
	{
		List contents = new ArrayList();
		
		OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.MediumContentImpl c WHERE c.repositoryId = $1 ORDER BY c.contentId");
    	oql.bind(repositoryId);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
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
	 * This method returns the contents belonging to a certain repository.
	 */
	
	public List getRepositoryContents(Integer repositoryId, Database db) throws SystemException, Exception
	{
		List contents = new ArrayList();
		
		OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.ContentImpl c WHERE c.repository = $1 ORDER BY c.contentId");
    	oql.bind(repositoryId);
    	
    	QueryResults results = oql.execute();
		
		while(results.hasMore()) 
        {
        	ContentImpl content = (ContentImpl)results.next();
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
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
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
		ContentVO content = getRootContent(repositoryId, db).getValueObject();
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
		ContentVO contentVO = null;
		
		OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.MediumContentImpl c WHERE c.parentContentId = $1 AND c.name = $2");
    	oql.bind(parentContentId);
    	oql.bind(name);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		if(results.hasMore()) 
        {
        	MediumContentImpl content = (MediumContentImpl)results.next();
        	contentVO = content.getValueObject();
        }

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
	
    public List getContentVOWithParentRecursive(Integer contentId) throws ConstraintException, SystemException
	{
		return getContentVOWithParentRecursive(contentId, new ArrayList());
	}
	
	private List getContentVOWithParentRecursive(Integer contentId, List resultList) throws ConstraintException, SystemException
	{
		// Get the versions of this content.
		resultList.add(getContentVOWithId(contentId));
		
		// Get the children of this content and do the recursion
		List childContentList = ContentController.getContentController().getContentChildrenVOList(contentId, null, false);
		Iterator cit = childContentList.iterator();
		while (cit.hasNext())
		{
			ContentVO contentVO = (ContentVO) cit.next();
			getContentVOWithParentRecursive(contentVO.getId(), resultList);
		}
	
		return resultList;
	}

	public String getContentAttribute(Integer contentId, Integer languageId, String attributeName) throws Exception
	{
	    String attribute = "Undefined";
	    
	    ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
		
		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageId);

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
	 * Returns the path to, and including, the supplied content.
	 * 
	 * @param contentId the content to 
	 * 
	 * @return String the path to, and including, this content "library/library/..."
	 * 
	 */
	public String getContentPath(Integer contentId, boolean includeRootContent, boolean includeRepositoryName) throws ConstraintException, SystemException, Bug, Exception
	{
		StringBuffer sb = new StringBuffer();

		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);

		sb.insert(0, contentVO.getName());

		while (contentVO.getParentContentId() != null)
		{
			contentVO = ContentController.getContentController().getContentVOWithId(contentVO.getParentContentId());

			if (includeRootContent || contentVO.getParentContentId() != null)
			{
				sb.insert(0, contentVO.getName() + "/");
			}
		}

		if (includeRepositoryName)
		{
			RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(contentVO.getRepositoryId());
			if(repositoryVO != null)
				sb.insert(0, repositoryVO.getName() + " - /");
		}
		
		return sb.toString();
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
    				"c.expireDateTime > $1 AND c.expireDateTime < $2 AND c.publishDateTime < $3");

        	Calendar now = Calendar.getInstance();
        	Date currentDate = now.getTime();
        	oql.bind(currentDate);
        	now.add(Calendar.DAY_OF_YEAR, numberOfWeeks);
        	Date futureDate = now.getTime();
           	oql.bind(futureDate);
           	oql.bind(currentDate);

        	QueryResults results = oql.execute(Database.ReadOnly);
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
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
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
        			"c.expireDateTime > $1 AND c.expireDateTime < $2 AND c.publishDateTime < $3 AND c.contentVersions.versionModifier = $4");
    		
        	Calendar now = Calendar.getInstance();
        	Date currentDate = now.getTime();
        	oql.bind(currentDate);
        	now.add(Calendar.DAY_OF_YEAR, numberOfDays);
        	Date futureDate = now.getTime();
        	oql.bind(futureDate);
           	oql.bind(currentDate);
           	oql.bind(principal.getName());

        	QueryResults results = oql.execute(Database.ReadOnly);
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

	/**
	 * This method checks if there are published versions available for the contentVersion.
	 */
	
	public boolean hasPublishedVersion(Integer contentId)
	{
		boolean hasPublishedVersion = false;
		
		try
		{
			ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestPublishedContentVersion(contentId);
			if(contentVersion != null)
			{
				hasPublishedVersion = true;
			}
		}
		catch(Exception e){}
				
		return hasPublishedVersion;
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
			logger.error("An error occurred trying to get related contents from qualifyerXML " + qualifyerXML + ":" + e.getMessage(), e);
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
			logger.error("An error occurred trying to get related contents from qualifyerXML " + qualifyerXML + ":" + e.getMessage(), e);
		}

		return relatedPages;
	}
}
