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
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.ServiceDefinition;
import org.infoglue.cms.entities.management.ServiceDefinitionVO;
import org.infoglue.cms.entities.management.SiteNodeTypeDefinition;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.management.impl.simple.SiteNodeTypeDefinitionImpl;
import org.infoglue.cms.entities.structure.ServiceBindingVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.XMLHelper;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.applications.filters.FilterConstants;
import org.infoglue.deliver.applications.filters.ViewPageFilter;
import org.infoglue.deliver.controllers.kernel.URLComposer;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.Timer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

public class SiteNodeController extends BaseController 
{
    private final static Logger logger = Logger.getLogger(SiteNodeController.class.getName());

	protected static final Integer NO 			= new Integer(0);
	protected static final Integer YES 			= new Integer(1);
	protected static final Integer INHERITED 	= new Integer(2);

	/**
	 * Factory method
	 */

	public static SiteNodeController getController()
	{
		return new SiteNodeController();
	}

	/**
	 * This method gets the siteNodeVO with the given id
	 */
	 
    public SiteNodeVO getSiteNodeVOWithId(Integer siteNodeId) throws SystemException, Bug
    {
		return (SiteNodeVO) getVOWithId(SiteNodeImpl.class, siteNodeId);
    }

	/**
	 * This method gets the siteNodeVO with the given id
	 */
	 
    public static SiteNodeVO getSiteNodeVOWithId(Integer siteNodeId, Database db) throws SystemException, Bug
    {
		return (SiteNodeVO) getVOWithId(SiteNodeImpl.class, siteNodeId, db);
    }

    /**
	 * This method gets the siteNodeVO with the given id
	 */
	 
    public static SiteNodeVO getSmallSiteNodeVOWithId(Integer siteNodeId, Database db) throws SystemException, Bug
    {
    	String key = "" + siteNodeId;
		SiteNodeVO siteNodeVO = (SiteNodeVO)CacheController.getCachedObjectFromAdvancedCache("siteNodeCache", key);
		if(siteNodeVO != null)
		{
			//logger.info("There was an cached siteNodeVO:" + siteNodeVO);
		}
		else
		{
			siteNodeVO = (SiteNodeVO)getVOWithId(SmallSiteNodeImpl.class, siteNodeId, db);
			if(siteNodeVO != null)
				CacheController.cacheObjectInAdvancedCache("siteNodeCache", key, siteNodeVO);
		}
		
		return siteNodeVO;
		//return (SiteNodeVO) getVOWithId(SmallSiteNodeImpl.class, siteNodeId, db);
    }


    public SiteNode getSiteNodeWithId(Integer siteNodeId, Database db) throws SystemException, Bug
    {
        return getSiteNodeWithId(siteNodeId, db, false);
    }

    public SiteNodeVersion getSiteNodeVersionWithId(Integer siteNodeVersionId, Database db) throws SystemException, Bug
    {
		return (SiteNodeVersion) getObjectWithId(SiteNodeVersionImpl.class, siteNodeVersionId, db);
    }

    public static SiteNode getSiteNodeWithId(Integer siteNodeId, Database db, boolean readOnly) throws SystemException, Bug
    {
        SiteNode siteNode = null;
        try
        {
        	if(readOnly)
	            siteNode = (SiteNode)db.load(org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl.class, siteNodeId, Database.ReadOnly);
    		else
    		{
                logger.info("Loading " + siteNodeId + " in read/write mode.");
	            siteNode = (SiteNode)db.load(org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl.class, siteNodeId);
    		}
    	}
        catch(Exception e)
        {
            throw new SystemException("An error occurred when we tried to fetch the SiteNode. Reason:" + e.getMessage(), e);    
        }
    
        if(siteNode == null)
        {
            throw new Bug("The SiteNode with id [" + siteNodeId + "] was not found in SiteNodeHelper.getSiteNodeWithId. This should never happen.");
        }
    
        return siteNode;
    }
    
	/**
	 * Returns a repository list marked for deletion.
	 */
	
	public List<SiteNodeVO> getSiteNodeVOListMarkedForDeletion(Integer repositoryId) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();
		
		List<SiteNodeVO> siteNodeVOListMarkedForDeletion = new ArrayList<SiteNodeVO>();
		
		try 
		{
			beginTransaction(db);
		
			String sql = "SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl sn WHERE sn.isDeleted = $1 ORDER BY sn.siteNodeId";
			if(repositoryId != null && repositoryId != -1)
				sql = "SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl sn WHERE sn.isDeleted = $1 AND sn.repository = $2 ORDER BY sn.siteNodeId";
			
			OQLQuery oql = db.getOQLQuery(sql);
			oql.bind(true);
			if(repositoryId != null && repositoryId != -1)
				oql.bind(repositoryId);
				
			QueryResults results = oql.execute(Database.READONLY);
			while(results.hasMore()) 
            {
				SiteNode siteNode = (SiteNode)results.next();
				siteNode.getValueObject().getExtraProperties().put("repositoryMarkedForDeletion", siteNode.getRepository().getIsDeleted());
                siteNodeVOListMarkedForDeletion.add(siteNode.getValueObject());
            }
            
			results.close();
			oql.close();

			commitTransaction(db);
		}
		catch ( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of deleted pages. Reason:" + e.getMessage(), e);			
		}
		
		return siteNodeVOListMarkedForDeletion;		
	}

	/**
	 * Returns a repository list marked for deletion.
	 */
	
	public Set<SiteNodeVO> getSiteNodeVOListLastModifiedByPincipal(InfoGluePrincipal principal) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();
		
		Set<SiteNodeVO> siteNodeVOList = new HashSet<SiteNodeVO>();
		
		try 
		{
			beginTransaction(db);
		
			OQLQuery oql = db.getOQLQuery("SELECT snv FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl snv WHERE snv.versionModifier = $1 ORDER BY snv.modifiedDateTime DESC LIMIT $2");
			oql.bind(principal.getName());
			oql.bind(30);
			
			QueryResults results = oql.execute(Database.READONLY);
			while(results.hasMore()) 
            {
				SiteNodeVersion siteNodeVersion = (SiteNodeVersion)results.next();
				SiteNodeVO siteNodeVO = getSiteNodeVOWithId(siteNodeVersion.getValueObject().getSiteNodeId(), db);
				siteNodeVOList.add(siteNodeVO);
            }
            
			results.close();
			oql.close();

			commitTransaction(db);
		}
		catch ( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list pages last modified by the user. Reason:" + e.getMessage(), e);			
		}
		
		return siteNodeVOList;		
	}

	/**
	 * This method deletes a siteNode and also erases all the children and all versions.
	 */
	    
    public void delete(Integer siteNodeId, boolean forceDelete, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException
    {
    	SiteNodeVO siteNodeVO = SiteNodeControllerProxy.getController().getSiteNodeVOWithId(siteNodeId);
    	
    	delete(siteNodeVO, infogluePrincipal, forceDelete);
    }
    
	/**
	 * This method deletes a siteNode and also erases all the children and all versions.
	 */
	    
    public void delete(SiteNodeVO siteNodeVO, InfoGluePrincipal infogluePrincipal, boolean forceDelete) throws ConstraintException, SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {	
			delete(siteNodeVO, db, forceDelete, infogluePrincipal);	
			
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
	 * This method deletes a siteNode and also erases all the children and all versions.
	 */
	    
	public void delete(SiteNodeVO siteNodeVO, Database db, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException, Exception
	{
		delete(siteNodeVO, db, false, infogluePrincipal);
	}
	
	/**
	 * This method deletes a siteNode and also erases all the children and all versions.
	 */
	    
	public void delete(SiteNodeVO siteNodeVO, Database db, boolean forceDelete, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException, Exception
	{
		SiteNode siteNode = getSiteNodeWithId(siteNodeVO.getSiteNodeId(), db);
		SiteNode parent = siteNode.getParentSiteNode();
		if(parent != null)
		{
			Iterator childSiteNodeIterator = parent.getChildSiteNodes().iterator();
			while(childSiteNodeIterator.hasNext())
			{
			    SiteNode candidate = (SiteNode)childSiteNodeIterator.next();
			    if(candidate.getId().equals(siteNodeVO.getSiteNodeId()))
			        deleteRecursive(siteNode, childSiteNodeIterator, db, forceDelete, infogluePrincipal);
			}
		}
		else
		{
		    deleteRecursive(siteNode, null, db, forceDelete, infogluePrincipal);
		}
	}        


	/**
	 * Recursively deletes all siteNodes and their versions.
	 * This method is a mess as we had a problem with the lazy-loading and transactions. 
	 * We have to begin and commit all the time...
	 */
	
    private static void deleteRecursive(SiteNode siteNode, Iterator parentIterator, Database db, boolean forceDelete, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException, Exception
    {
        List referenceBeanList = RegistryController.getController().getReferencingObjectsForSiteNode(siteNode.getId(), -1, db);
		if(referenceBeanList != null && referenceBeanList.size() > 0 && !forceDelete)
			throw new ConstraintException("SiteNode.stateId", "3405");

        Collection children = siteNode.getChildSiteNodes();
		Iterator i = children.iterator();
		while(i.hasNext())
		{
			SiteNode childSiteNode = (SiteNode)i.next();
			deleteRecursive(childSiteNode, i, db, forceDelete, infoGluePrincipal);
   		}
		siteNode.setChildSiteNodes(new ArrayList());
		
		if(forceDelete || getIsDeletable(siteNode, infoGluePrincipal, db))
	    {		 
			SiteNodeVersionController.deleteVersionsForSiteNode(siteNode, db, infoGluePrincipal);
			
			ServiceBindingController.deleteServiceBindingsReferencingSiteNode(siteNode, db);

			if(parentIterator != null) 
			    parentIterator.remove();
			
			db.remove(siteNode);
	    }
	    else
    	{
    		throw new ConstraintException("SiteNodeVersion.stateId", "3400");
    	}			
    }        

    
	/**
	 * This method deletes a siteNode and also erases all the children and all versions.
	 */
	    
    public void markForDeletion(SiteNodeVO siteNodeVO, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {	
			markForDeletion(siteNodeVO, db, infogluePrincipal);	
			
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
	 * This method deletes a siteNode and also erases all the children and all versions.
	 */
	    
	public void markForDeletion(SiteNodeVO siteNodeVO, Database db, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException, Exception
	{
		markForDeletion(siteNodeVO, db, false, infogluePrincipal);
	}
	
	/**
	 * This method deletes a siteNode and also erases all the children and all versions.
	 */
	    
	public void markForDeletion(SiteNodeVO siteNodeVO, Database db, boolean forceDelete, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException, Exception
	{
		SiteNode siteNode = getSiteNodeWithId(siteNodeVO.getSiteNodeId(), db);
		SiteNode parent = siteNode.getParentSiteNode();
		if(parent != null)
		{
			Iterator childSiteNodeIterator = parent.getChildSiteNodes().iterator();
			while(childSiteNodeIterator.hasNext())
			{
			    SiteNode candidate = (SiteNode)childSiteNodeIterator.next();
			    if(candidate.getId().equals(siteNodeVO.getSiteNodeId()))
			    	markForDeletionRecursive(siteNode, childSiteNodeIterator, db, forceDelete, infogluePrincipal);
			}
		}
		else
		{
			markForDeletionRecursive(siteNode, null, db, forceDelete, infogluePrincipal);
		}
	}        


	/**
	 * Recursively deletes all siteNodes and their versions.
	 * This method is a mess as we had a problem with the lazy-loading and transactions. 
	 * We have to begin and commit all the time...
	 */
	
    private static void markForDeletionRecursive(SiteNode siteNode, Iterator parentIterator, Database db, boolean forceDelete, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException, Exception
    {
        List referenceBeanList = RegistryController.getController().getReferencingObjectsForSiteNode(siteNode.getId(), -1, db);
		if(referenceBeanList != null && referenceBeanList.size() > 0 && !forceDelete)
			throw new ConstraintException("SiteNode.stateId", "3405", "<br/><br/>" + siteNode.getName() + " (" + siteNode.getId() + ")");

        Collection children = siteNode.getChildSiteNodes();
		Iterator i = children.iterator();
		while(i.hasNext())
		{
			SiteNode childSiteNode = (SiteNode)i.next();
			markForDeletionRecursive(childSiteNode, i, db, forceDelete, infoGluePrincipal);
   		}
		
		if(forceDelete || getIsDeletable(siteNode, infoGluePrincipal, db))
	    {		 
			//SiteNodeVersionController.deleteVersionsForSiteNode(siteNode, db, infoGluePrincipal);
			
			//ServiceBindingController.deleteServiceBindingsReferencingSiteNode(siteNode, db);

			//if(parentIterator != null) 
			//    parentIterator.remove();
			
			//db.remove(siteNode);
			siteNode.setIsDeleted(true);
	    }
	    else
    	{
    		throw new ConstraintException("SiteNodeVersion.stateId", "3400");
    	}			
    }        

    /**
	 * This method restores a siteNode.
	 */
	    
    public void restoreSiteNode(Integer siteNodeId, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException
    {
	    Database db = CastorDatabaseService.getDatabase();
        
	    beginTransaction(db);
		
        try
        {
        	SiteNode siteNode = getSiteNodeWithId(siteNodeId, db);
        	siteNode.setIsDeleted(false);
	    	
			while(siteNode.getParentSiteNode() != null && siteNode.getParentSiteNode().getIsDeleted())
			{
				siteNode = siteNode.getParentSiteNode();
				siteNode.setIsDeleted(false);
			}

	    	commitTransaction(db);
        }
        catch(Exception e)
        {
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }  
    
	/**
	 * This method returns true if the sitenode does not have any published siteNodeversions or 
	 * are restricted in any other way.
	 */
	
	private static boolean getIsDeletable(SiteNode siteNode, InfoGluePrincipal infogluePrincipal, Database db) throws SystemException, Exception
	{
		boolean isDeletable = true;
		
		SiteNodeVersion latestSiteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersion(db, siteNode.getId());
		if(latestSiteNodeVersion != null && latestSiteNodeVersion.getIsProtected().equals(SiteNodeVersionVO.YES))
		{
			boolean hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(db, infogluePrincipal, "SiteNodeVersion.DeleteSiteNode", "" + latestSiteNodeVersion.getId());
			if(!hasAccess)
				return false;
		}
		
        Collection siteNodeVersions = siteNode.getSiteNodeVersions();
    	if(siteNodeVersions != null)
    	{
	        Iterator versionIterator = siteNodeVersions.iterator();
			while (versionIterator.hasNext()) 
	        {
	        	SiteNodeVersion siteNodeVersion = (SiteNodeVersion)versionIterator.next();
	        	if(siteNodeVersion.getStateId().intValue() == SiteNodeVersionVO.PUBLISHED_STATE.intValue() && siteNodeVersion.getIsActive().booleanValue() == true)
	        	{
	        		logger.warn("The siteNode had a published version so we cannot delete it..");
					isDeletable = false;
	        		break;
	        	}
		    }		
    	}
    	
		return isDeletable;	
	}

	
	public SiteNodeVO create(Integer parentSiteNodeId, Integer siteNodeTypeDefinitionId, InfoGluePrincipal infoGluePrincipal, Integer repositoryId, SiteNodeVO siteNodeVO) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		SiteNode siteNode = null;

		beginTransaction(db);

		try
		{
			//Here you might want to add some validate functonality?
			siteNode = create(db, parentSiteNodeId, siteNodeTypeDefinitionId, infoGluePrincipal, repositoryId, siteNodeVO);
             
			//If any of the validations or setMethods reported an error, we throw them up now before create.
			ceb.throwIfNotEmpty();
            
			commitTransaction(db);
		}
		catch(ConstraintException ce)
		{
			logger.warn("An error occurred so we should not complete the transaction:" + ce, ce);
			//rollbackTransaction(db);
			throw ce;
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			//rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}

		return siteNode.getValueObject();
	}     
    
    public SiteNode create(Database db, Integer parentSiteNodeId, Integer siteNodeTypeDefinitionId, InfoGluePrincipal infoGluePrincipal, Integer repositoryId, SiteNodeVO siteNodeVO) throws SystemException, Exception
    {
	    SiteNode siteNode = null;

        logger.info("******************************************");
        logger.info("parentSiteNode:" + parentSiteNodeId);
        logger.info("siteNodeTypeDefinition:" + siteNodeTypeDefinitionId);
        logger.info("repository:" + repositoryId);
        logger.info("******************************************");
        
        //Fetch related entities here if they should be referenced        
        
        SiteNode parentSiteNode = null;
      	SiteNodeTypeDefinition siteNodeTypeDefinition = null;

        if(parentSiteNodeId != null)
        {
       		parentSiteNode = getSiteNodeWithId(parentSiteNodeId, db);
			if(repositoryId == null)
				repositoryId = parentSiteNode.getRepository().getRepositoryId();	
        }		
        
        if(siteNodeTypeDefinitionId != null)
        	siteNodeTypeDefinition = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionWithId(siteNodeTypeDefinitionId, db);

        Repository repository = RepositoryController.getController().getRepositoryWithId(repositoryId, db);

        
        siteNode = new SiteNodeImpl();
        siteNode.setValueObject(siteNodeVO);
        siteNode.setParentSiteNode((SiteNodeImpl)parentSiteNode);
        siteNode.setRepository((RepositoryImpl)repository);
        siteNode.setSiteNodeTypeDefinition((SiteNodeTypeDefinitionImpl)siteNodeTypeDefinition);
        siteNode.setCreator(infoGluePrincipal.getName());

        db.create(siteNode);
        
        if(parentSiteNode != null)
        	parentSiteNode.getChildSiteNodes().add(siteNode);
        
        //commitTransaction(db);
        //siteNode = (SiteNode) createEntity(siteNode, db);
        
        //No siteNode is an island (humhum) so we also have to create an siteNodeVersion for it. 
        SiteNodeVersionController.createInitialSiteNodeVersion(db, siteNode, infoGluePrincipal);
                    
        return siteNode;
    }

	/**
	 * This method creates a new SiteNode and an siteNodeVersion. It does not commit the transaction however.
	 * 
	 * @param db
	 * @param parentSiteNodeId
	 * @param siteNodeTypeDefinitionId
	 * @param userName
	 * @param repositoryId
	 * @param siteNodeVO
	 * @return
	 * @throws SystemException
	 */
	
	public SiteNode createNewSiteNode(Database db, Integer parentSiteNodeId, Integer siteNodeTypeDefinitionId, InfoGluePrincipal infoGluePrincipal, Integer repositoryId, SiteNodeVO siteNodeVO) throws SystemException
	{
		SiteNode siteNode = null;

		try
		{
			logger.info("******************************************");
			logger.info("parentSiteNode:" + parentSiteNodeId);
			logger.info("siteNodeTypeDefinition:" + siteNodeTypeDefinitionId);
			logger.info("repository:" + repositoryId);
			logger.info("******************************************");
            
        	//Fetch related entities here if they should be referenced        
			
			SiteNode parentSiteNode = null;
			SiteNodeTypeDefinition siteNodeTypeDefinition = null;

			if(parentSiteNodeId != null)
			{
				parentSiteNode = getSiteNodeWithId(parentSiteNodeId, db);
				if(repositoryId == null)
					repositoryId = parentSiteNode.getRepository().getRepositoryId();	
			}		
			
			if(siteNodeTypeDefinitionId != null)
				siteNodeTypeDefinition = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionWithId(siteNodeTypeDefinitionId, db);
			
			Repository repository = RepositoryController.getController().getRepositoryWithId(repositoryId, db);

			siteNode = new SiteNodeImpl();
			siteNode.setValueObject(siteNodeVO);
			siteNode.setParentSiteNode((SiteNodeImpl)parentSiteNode);
			siteNode.setRepository((RepositoryImpl)repository);
			siteNode.setSiteNodeTypeDefinition((SiteNodeTypeDefinitionImpl)siteNodeTypeDefinition);
			siteNode.setCreator(infoGluePrincipal.getName());

			//siteNode = (SiteNode) createEntity(siteNode, db);
			db.create((SiteNode)siteNode);
		
			//No siteNode is an island (humhum) so we also have to create an siteNodeVersion for it.
			SiteNodeVersion siteNodeVersion = SiteNodeVersionController.createInitialSiteNodeVersion(db, siteNode, infoGluePrincipal);
		
			List siteNodeVersions = new ArrayList();
			siteNodeVersions.add(siteNodeVersion);
			siteNode.setSiteNodeVersions(siteNodeVersions);
		}
		catch(Exception e)
		{
		    throw new SystemException("An error occurred when we tried to create the SiteNode in the database. Reason:" + e.getMessage(), e);    
		}
        
		return siteNode;
	}


	/**
	 * This method returns the value-object of the parent of a specific siteNode. 
	 */
	
    public static SiteNodeVO getParentSiteNode(Integer siteNodeId) throws SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		SiteNodeVO parentSiteNodeVO = null;
		
        beginTransaction(db);

        try
        {
			SiteNode parent = getParentSiteNode(siteNodeId, db);
			if(parent != null)
				parentSiteNodeVO = parent.getValueObject();
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
		return parentSiteNodeVO;    	
    }
    
	/**
	 * This method returns the value-object of the parent of a specific siteNode. 
	 */
	
	public static SiteNode getParentSiteNode(Integer siteNodeId, Database db) throws SystemException, Bug
	{
		SiteNode siteNode = (SiteNode) getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
		SiteNode parent = siteNode.getParentSiteNode();

		return parent;    	
	}
    
	/**
	 * This method returns a list of the children a siteNode has.
	 */
   	
	public List getSiteNodeChildren(Integer parentSiteNodeId) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		List childrenVOList = null;

		beginTransaction(db);

		try
		{
			SiteNode siteNode = getSiteNodeWithId(parentSiteNodeId, db);
			Collection children = siteNode.getChildSiteNodes();
			childrenVOList = SiteNodeController.toVOList(children);
        	
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
        
		return childrenVOList;
	} 
	
    
	/**
	 * This method returns a list of the children a siteNode has.
	 */
   	
	public List<SiteNodeVO> getChildSiteNodeVOList(Integer parentSiteNodeId, boolean showDeletedItems, Database db) throws Exception
	{
   		String key = "" + parentSiteNodeId + "_" + showDeletedItems;
		if(logger.isInfoEnabled())
			logger.info("key:" + key);
		
		List<SiteNodeVO> childrenVOList = (List<SiteNodeVO>)CacheController.getCachedObjectFromAdvancedCache("childSiteNodesCache", key);
		if(childrenVOList != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached cachedChildSiteNodeVOList:" + childrenVOList.size());
			return childrenVOList;
		}
		
		childrenVOList = new ArrayList<SiteNodeVO>();
		
   		Timer t = new Timer();

   		StringBuffer SQL = new StringBuffer();
    	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
    	{
	   		SQL.append("CALL SQL select sn.siNoId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.metaInfoContentId, sn.creator, (select count(*) from cmSiNo sn2 where sn2.parentSiNoId = sn.siNoId) AS childCount, snv.sortOrder, snv.isHidden from cmSiNo sn, cmSiNoVer snv ");
	   		SQL.append("where ");
	   		SQL.append("sn.parentSiNoId = $1 ");
	   		SQL.append("AND sn.isDeleted = $2 ");
	   		SQL.append("AND snv.siNoId = sn.siNoId ");
	   		SQL.append("AND snv.siNoVerId = ( ");
	   		SQL.append("	select max(siNoVerId) from cmSiNoVer snv2 ");
	   		SQL.append("	WHERE ");
	   		SQL.append("	snv2.siNoId = snv.siNoId AND ");
	   		SQL.append("	snv2.isActive = $3 AND snv2.stateId >= $4 ");
	   		SQL.append("	) ");
	   		SQL.append("order by snv.sortOrder ASC, sn.name ASC, sn.siNoId DESC AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");
    	}
    	else
    	{
	   		SQL.append("CALL SQL select sn.siteNodeId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.metaInfoContentId, sn.creator, (select count(*) from cmSiteNode sn2 where sn2.parentSiteNodeId = sn.siteNodeId) AS childCount, snv.sortOrder, snv.isHidden from cmSiteNode sn, cmSiteNodeVersion snv ");
	   		SQL.append("where ");
	   		SQL.append("sn.parentSiteNodeId = $1 ");
	   		SQL.append("AND sn.isDeleted = $2 ");
	   		SQL.append("AND snv.siteNodeId = sn.siteNodeId ");
	   		SQL.append("AND snv.siteNodeVersionId = ( ");
	   		SQL.append("	select max(siteNodeVersionId) from cmSiteNodeVersion snv2 ");
	   		SQL.append("	WHERE ");
	   		SQL.append("	snv2.siteNodeId = snv.siteNodeId AND ");
	   		SQL.append("	snv2.isActive = $3 AND snv2.stateId >= $4 ");
	   		SQL.append("	) ");
	   		SQL.append("order by snv.sortOrder ASC, sn.name ASC, sn.siteNodeId DESC AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");    		
    	}

    	//logger.info("SQL:" + SQL);
    	//logger.info("parentSiteNodeId:" + parentSiteNodeId);
    	//logger.info("showDeletedItems:" + showDeletedItems);
    	OQLQuery oql = db.getOQLQuery(SQL.toString());
		oql.bind(parentSiteNodeId);
		oql.bind(showDeletedItems);
		oql.bind(true);
		oql.bind(0);

		QueryResults results = oql.execute(Database.ReadOnly);
		while (results.hasMore()) 
		{
			SiteNode siteNode = (SiteNode)results.next();
			childrenVOList.add(siteNode.getValueObject());
		}
		
		results.close();
		oql.close();
        
		CacheController.cacheObjectInAdvancedCache("childSiteNodesCache", key, childrenVOList);
        
		return childrenVOList;
	} 
	
    /**
	 * This method is sort of a sql-query-like method where you can send in arguments in form of a list
	 * of things that should match. The input is a Hashmap with a method and a List of HashMaps.
	 */
	
    public List getSiteNodeVOList(HashMap argumentHashMap) throws SystemException, Bug
    {
    	List siteNodes = null;
    	
    	String method = (String)argumentHashMap.get("method");
    	logger.info("method:" + method);
    	
    	if(method.equalsIgnoreCase("selectSiteNodeListOnIdList"))
    	{
			siteNodes = new ArrayList();
			List arguments = (List)argumentHashMap.get("arguments");
			logger.info("Arguments:" + arguments.size());  
			Iterator argumentIterator = arguments.iterator();
			while(argumentIterator.hasNext())
			{ 		
				HashMap argument = (HashMap)argumentIterator.next(); 
				logger.info("argument:" + argument.size());
				 
				Iterator iterator = argument.keySet().iterator();
			    while ( iterator.hasNext() )
			       logger.info( "   " + iterator.next() );


				Integer siteNodeId = new Integer((String)argument.get("siteNodeId"));
				logger.info("Getting the siteNode with Id:" + siteNodeId);
				siteNodes.add(getSiteNodeVOWithId(siteNodeId));
			}
    	}
        
        return siteNodes;
    }

    /**
	 * This method is sort of a sql-query-like method where you can send in arguments in form of a list
	 * of things that should match. The input is a Hashmap with a method and a List of HashMaps.
	 */
	
    public static List getSiteNodeVOList(HashMap argumentHashMap, Database db) throws SystemException, Bug
    {
    	List siteNodes = null;
    	
    	String method = (String)argumentHashMap.get("method");
    	logger.info("method:" + method);
    	
    	if(method.equalsIgnoreCase("selectSiteNodeListOnIdList"))
    	{
			siteNodes = new ArrayList();
			List arguments = (List)argumentHashMap.get("arguments");
			logger.info("Arguments:" + arguments.size());  
			Iterator argumentIterator = arguments.iterator();
			while(argumentIterator.hasNext())
			{ 		
				HashMap argument = (HashMap)argumentIterator.next(); 
				logger.info("argument:" + argument.size());
				 
				Iterator iterator = argument.keySet().iterator();
			    while ( iterator.hasNext() )
			       logger.info( "   " + iterator.next() );


				Integer siteNodeId = new Integer((String)argument.get("siteNodeId"));
				logger.info("Getting the siteNode with Id:" + siteNodeId);
				siteNodes.add(getSmallSiteNodeVOWithId(siteNodeId, db));
			}
    	}
        
        return siteNodes;
    }

    /**
	 * This method fetches the root siteNode for a particular repository.
	 */
	        
   	public SiteNodeVO getRootSiteNodeVO(Integer repositoryId) throws ConstraintException, SystemException
   	{
   		String key = "rootSiteNode_" + repositoryId;
   		SiteNodeVO cachedRootNodeVO = (SiteNodeVO)CacheController.getCachedObject("repositoryRootNodesCache", key);
		if(cachedRootNodeVO != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cachedRootNodeVO:" + cachedRootNodeVO);
			return cachedRootNodeVO;
		}

        Database db = CastorDatabaseService.getDatabase();

        SiteNodeVO siteNodeVO = null;

        beginTransaction(db);

        try
        {
        	siteNodeVO = getRootSiteNodeVO(repositoryId, db);
			
    		if(siteNodeVO != null)
    			CacheController.cacheObject("repositoryRootNodesCache", key, siteNodeVO);

			commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return siteNodeVO;
   	}

	/**
	 * This method fetches the root siteNode for a particular repository within a certain transaction.
	 */
	        
	public SiteNodeVO getRootSiteNodeVO(Integer repositoryId, Database db) throws ConstraintException, SystemException, Exception
	{
		SiteNodeVO siteNodeVO = null;
		
		OQLQuery oql = db.getOQLQuery( "SELECT s FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl s WHERE is_undefined(s.parentSiteNode) AND s.repositoryId = $1");
		oql.bind(repositoryId);
		
		QueryResults results = oql.execute(Database.ReadOnly);
		if (results.hasMore()) 
		{
			SiteNode siteNode = (SiteNode)results.next();
			siteNodeVO = siteNode.getValueObject();
		}

		results.close();
		oql.close();

		return siteNodeVO;
	}

	/**
	 * This method fetches the root siteNode for a particular repository within a certain transaction.
	 */
	        
	public SiteNode getRootSiteNode(Integer repositoryId, Database db) throws ConstraintException, SystemException, Exception
	{
		SiteNode siteNode = null;
		
		OQLQuery oql = db.getOQLQuery( "SELECT s FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl s WHERE is_undefined(s.parentSiteNode) AND s.repository.repositoryId = $1");
		oql.bind(repositoryId);
		
		QueryResults results = oql.execute();
		this.logger.info("Fetching entity in read/write mode" + repositoryId);

		if (results.hasMore()) 
		{
			siteNode = (SiteNode)results.next();
		}

		results.close();
		oql.close();

		return siteNode;
	}


	/**
	 * This method moves a siteNode after first making a couple of controls that the move is valid.
	 */
	
    public void moveSiteNode(SiteNodeVO siteNodeVO, Integer newParentSiteNodeId, InfoGluePrincipal principal) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        SiteNode siteNode          = null;
		SiteNode newParentSiteNode = null;
		SiteNode oldParentSiteNode = null;
		
        beginTransaction(db);

        try
        {
            //Validation that checks the entire object
            siteNodeVO.validate();
            
            if(newParentSiteNodeId == null)
            {
            	logger.warn("You must specify the new parent-siteNode......");
            	throw new ConstraintException("SiteNode.parentSiteNodeId", "3403");
            }

            if(siteNodeVO.getId().intValue() == newParentSiteNodeId.intValue())
            {
            	logger.warn("You cannot have the siteNode as it's own parent......");
            	throw new ConstraintException("SiteNode.parentSiteNodeId", "3401");
            }
            
            siteNode          = getSiteNodeWithId(siteNodeVO.getSiteNodeId(), db);
            oldParentSiteNode = siteNode.getParentSiteNode();
            newParentSiteNode = getSiteNodeWithId(newParentSiteNodeId, db);
            
            if(oldParentSiteNode.getId().intValue() == newParentSiteNodeId.intValue())
            {
            	logger.warn("You cannot specify the same node as it originally was located in......");
            	throw new ConstraintException("SiteNode.parentSiteNodeId", "3404");
            }

			SiteNode tempSiteNode = newParentSiteNode.getParentSiteNode();
			while(tempSiteNode != null)
			{
				if(tempSiteNode.getId().intValue() == siteNode.getId().intValue())
				{
					logger.warn("You cannot move the node to a child under it......");
            		throw new ConstraintException("SiteNode.parentSiteNodeId", "3402");
				}
				tempSiteNode = tempSiteNode.getParentSiteNode();
			}	
			
            logger.info("Setting the new Parent siteNode:" + siteNode.getSiteNodeId() + " " + newParentSiteNode.getSiteNodeId());
            siteNode.setParentSiteNode((SiteNodeImpl)newParentSiteNode);
            
            Integer metaInfoContentId = siteNode.getMetaInfoContentId();
            //logger.info("metaInfoContentId:" + metaInfoContentId);
            if(!siteNode.getRepository().getId().equals(newParentSiteNode.getRepository().getId()) && metaInfoContentId != null)
            {
            	Content metaInfoContent = ContentController.getContentController().getContentWithId(metaInfoContentId, db);
            	Content newParentContent = ContentController.getContentController().getContentWithPath(newParentSiteNode.getRepository().getId(), "Meta info folder", true, principal, db);
                if(metaInfoContent != null && newParentContent != null)
            	{
            		//logger.info("Moving:" + metaInfoContent.getName() + " to " + newParentContent.getName());
            		newParentContent.getChildren().add(metaInfoContent);
            		Content previousParentContent = metaInfoContent.getParentContent();
            		metaInfoContent.setParentContent((ContentImpl)newParentContent);
            		previousParentContent.getChildren().remove(metaInfoContent);

            		changeRepositoryRecursiveForContent(metaInfoContent, newParentSiteNode.getRepository());
				}
            }
            
            changeRepositoryRecursive(siteNode, newParentSiteNode.getRepository());
            //siteNode.setRepository(newParentSiteNode.getRepository());
            
            //Test registering system redirects for the old location
            Map<String,String> pageUrls = RedirectController.getController().getNiceURIMapBeforeMove(db, siteNode.getRepository().getId(), siteNode.getId(), principal);
            
			newParentSiteNode.getChildSiteNodes().add(siteNode);
			oldParentSiteNode.getChildSiteNodes().remove(siteNode);
			
            //If any of the validations or setMethods reported an error, we throw them up now before create.
            ceb.throwIfNotEmpty();
            
            commitTransaction(db);
            
            //Test registering system redirects for the old location
            RedirectController.getController().createSystemRedirect(pageUrls, siteNode.getRepository().getId(), siteNode.getId(), principal);
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
	 * This method moves a siteNode after first making a couple of controls that the move is valid.
	 */
	
    public void changeSiteNodeSortOrder(Integer siteNodeId, Integer beforeSiteNodeId, String direction, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException
    {
	    Database db = CastorDatabaseService.getDatabase();

        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
            if(beforeSiteNodeId == null && direction == null)
            {
            	logger.warn("You must specify the either new location with beforeSiteNodeId or sortDirection");
            	throw new ConstraintException("SiteNode.parentSiteNodeId", "3403"); //TODO
            }
            
            //logger.info("siteNodeId:" + siteNodeId);
            //logger.info("beforeSiteNodeId:" + beforeSiteNodeId);
            //logger.info("direction:" + direction);
            
            if(beforeSiteNodeId != null)
            {
                SiteNode beforeSiteNode = getSiteNodeWithId(beforeSiteNodeId, db);
            }
            else if(direction.equalsIgnoreCase("up") || direction.equalsIgnoreCase("down"))
            {
            	Integer oldSortOrder = 0;
            	Integer newSortOrder = 0;
            	SiteNodeVO siteNodeVO = getSiteNodeVOWithId(siteNodeId, db);

                SiteNodeVersion latestSiteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersion(db, siteNodeId);
            	if(latestSiteNodeVersion != null)
				{
					oldSortOrder = latestSiteNodeVersion.getSortOrder();
					if(direction.equalsIgnoreCase("up"))
						newSortOrder = oldSortOrder - 1;
					else if(direction.equalsIgnoreCase("down"))
						newSortOrder = oldSortOrder + 1;
				}
				
				List<SiteNodeVO> childrenVOList = SiteNodeController.getController().getChildSiteNodeVOList(siteNodeVO.getParentSiteNodeId(), false, db);
				Iterator<SiteNodeVO> childrenVOListIterator = childrenVOList.iterator();
				while(childrenVOListIterator.hasNext())
				{
					SiteNodeVO childSiteNodeVO = childrenVOListIterator.next();
					SiteNodeVersion latestChildSiteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersion(db, childSiteNodeVO.getId());
					//logger.info("latestChildSiteNodeVersion:" + latestChildSiteNodeVersion.getId());
					Integer currentSortOrder = latestChildSiteNodeVersion.getSortOrder();
					if(currentSortOrder.equals(oldSortOrder))
					{
						latestChildSiteNodeVersion = SiteNodeVersionController.getController().updateStateId(latestChildSiteNodeVersion, SiteNodeVersionVO.WORKING_STATE, "Changed sortOrder", infoGluePrincipal, db);
						latestChildSiteNodeVersion.setSortOrder(newSortOrder);
						//logger.info("Changed sort order on:" + latestChildSiteNodeVersion.getId() + " into " + newSortOrder);
					}
					else if(currentSortOrder.equals(newSortOrder))
					{
						latestChildSiteNodeVersion = SiteNodeVersionController.getController().updateStateId(latestChildSiteNodeVersion, SiteNodeVersionVO.WORKING_STATE, "Changed sortOrder", infoGluePrincipal, db);
						latestChildSiteNodeVersion.setSortOrder(oldSortOrder);
						//logger.info("Changed sort order on:" + latestChildSiteNodeVersion.getId() + " into " + oldSortOrder);
					}
				}
            }

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
    }       

    
	/**
	 * This method moves a siteNode after first making a couple of controls that the move is valid.
	 */
	
    public void toggleSiteNodeHidden(Integer siteNodeId, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException
    {
	    Database db = CastorDatabaseService.getDatabase();

        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
        	//logger.info("siteNodeId:" + siteNodeId);
            
            SiteNodeVersion latestSiteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersion(db, siteNodeId);
            //logger.info("latestSiteNodeVersion:" + latestSiteNodeVersion);
            if(latestSiteNodeVersion != null)
			{
        		latestSiteNodeVersion = SiteNodeVersionController.getController().updateStateId(latestSiteNodeVersion, SiteNodeVersionVO.WORKING_STATE, "Changed hidden", infoGluePrincipal, db);
        		if(latestSiteNodeVersion.getIsHidden())
        			latestSiteNodeVersion.setIsHidden(false);
        		else
        			latestSiteNodeVersion.setIsHidden(true);
			}
			
            ceb.throwIfNotEmpty();
            
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
	 * Recursively sets the sitenodes repositoryId.
	 * @param sitenode
	 * @param newRepository
	 */

	private void changeRepositoryRecursive(SiteNode siteNode, Repository newRepository)
	{
	    if(siteNode.getRepository().getId().intValue() != newRepository.getId().intValue())
	    {
	        siteNode.setRepository((RepositoryImpl)newRepository);
		    Iterator ChildSiteNodesIterator = siteNode.getChildSiteNodes().iterator();
		    while(ChildSiteNodesIterator.hasNext())
		    {
		        SiteNode childSiteNode = (SiteNode)ChildSiteNodesIterator.next();
		        changeRepositoryRecursive(childSiteNode, newRepository);
		    }
	    }
	}
	
	/**
	 * Recursively sets the sitenodes repositoryId.
	 * @param sitenode
	 * @param newRepository
	 */

	private void changeRepositoryRecursiveForContent(Content content, Repository newRepository)
	{
	    if(content.getRepository() == null || content.getRepository().getId().intValue() != newRepository.getId().intValue())
	    {
	    	content.setRepository((RepositoryImpl)newRepository);
	    	if(content.getChildren() != null)
	    	{
			    Iterator childContentsIterator = content.getChildren().iterator();
			    while(childContentsIterator.hasNext())
			    {
			    	Content childContent = (Content)childContentsIterator.next();
			    	changeRepositoryRecursiveForContent(childContent, newRepository);
			    }
			}
	    }
	}
	
	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new SiteNodeVO();
	}

	/**
	 * This method returns a list of all siteNodes in a repository.
	 */

	public List getRepositorySiteNodes(Integer repositoryId, Database db) throws SystemException, Exception
    {
		List siteNodes = new ArrayList();
		
		OQLQuery oql = db.getOQLQuery("SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl sn WHERE sn.repository.repositoryId = $1");
    	oql.bind(repositoryId);
    	
    	QueryResults results = oql.execute();
		
		while(results.hasMore()) 
        {
        	SiteNode siteNode = (SiteNodeImpl)results.next();
        	siteNodes.add(siteNode);
        }
		
		results.close();
		oql.close();

		return siteNodes;    	
    }

	/**
	 * This method returns a list of all siteNodes in a repository.
	 */

	public List getRepositorySiteNodesReadOnly(Integer repositoryId, Database db) throws SystemException, Exception
    {
		List siteNodes = new ArrayList();
		
		OQLQuery oql = db.getOQLQuery("SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl sn WHERE sn.repository.repositoryId = $1");
    	oql.bind(repositoryId);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		while(results.hasMore()) 
        {
        	SiteNode siteNode = (SiteNodeImpl)results.next();
        	siteNodes.add(siteNode);
        }
		
		results.close();
		oql.close();

		return siteNodes;    	
    }
	
	/**
	 * This method creates a meta info content for the new sitenode.
	 * 
	 * @param db
	 * @param path
	 * @param newSiteNode
	 * @throws SystemException
	 * @throws Bug
	 * @throws Exception
	 * @throws ConstraintException
	 */
	
    public Content createSiteNodeMetaInfoContent(Database db, SiteNode newSiteNode, Integer repositoryId, InfoGluePrincipal principal, Integer pageTemplateContentId) throws SystemException, Bug, Exception, ConstraintException
    {
        Content content = null;
        
        String basePath = "Meta info folder";
        String path = "";
        
        SiteNode parentSiteNode = newSiteNode.getParentSiteNode();
        while(parentSiteNode != null)
        {
            path = "/" + parentSiteNode.getName() + path;
            parentSiteNode = parentSiteNode.getParentSiteNode();
        }
        path = basePath + path;
        
        SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getLatestSiteNodeVersion(db, newSiteNode.getId(), false);
        Language masterLanguage 		= LanguageController.getController().getMasterLanguage(db, repositoryId);
  	   
        ServiceDefinitionVO singleServiceDefinitionVO 	= null;
        
        Integer metaInfoContentTypeDefinitionId = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("Meta info", db).getId();
        Integer availableServiceBindingId = AvailableServiceBindingController.getController().getAvailableServiceBindingVOWithName("Meta information", db).getId();
        
        List serviceDefinitions = AvailableServiceBindingController.getController().getServiceDefinitionVOList(db, availableServiceBindingId);
        if(serviceDefinitions == null || serviceDefinitions.size() == 0)
        {
            ServiceDefinition serviceDefinition = ServiceDefinitionController.getController().getServiceDefinitionWithName("Core content service", db, false);
            String[] values = {serviceDefinition.getId().toString()};
            AvailableServiceBindingController.getController().update(availableServiceBindingId, values, db);
            singleServiceDefinitionVO = serviceDefinition.getValueObject();
        }
        else if(serviceDefinitions.size() == 1)
        {
        	singleServiceDefinitionVO = (ServiceDefinitionVO)serviceDefinitions.get(0);	    
        }
        
        ContentVO parentFolderContentVO = null;
        
        Content rootContent = ContentControllerProxy.getController().getRootContent(db, repositoryId, principal.getName(), true);
        if(rootContent != null)
        {
            ContentVO parentFolderContent = ContentController.getContentController().getContentVOWithPath(repositoryId, path, true, principal, db);
            
        	ContentVO contentVO = new ContentVO();
        	contentVO.setCreatorName(principal.getName());
        	contentVO.setIsBranch(new Boolean(false));
        	contentVO.setName(newSiteNode.getName() + " Metainfo");
        	contentVO.setRepositoryId(repositoryId);

        	content = ContentControllerProxy.getController().create(db, parentFolderContent.getId(), metaInfoContentTypeDefinitionId, repositoryId, contentVO);
        	
        	newSiteNode.setMetaInfoContentId(contentVO.getId());
        	
        	String componentStructure = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><components></components>";
        	if(pageTemplateContentId != null)
        	{
        	    Integer masterLanguageId = LanguageController.getController().getMasterLanguage(db, repositoryId).getId();
        		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(pageTemplateContentId, masterLanguageId, db);
        		
        	    componentStructure = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, "ComponentStructure", false);
        	
    			Document document = XMLHelper.readDocumentFromByteArray(componentStructure.getBytes("UTF-8"));
    			String componentXPath = "//component";
    			NodeList componentNodes = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
    			for(int i=0; i < componentNodes.getLength(); i++)
    			{
    				Element element = (Element)componentNodes.item(i);
    				String componentId = element.getAttribute("id");
    				String componentContentId = element.getAttribute("contentId");
    				
    				ComponentController.getController().checkAndAutoCreateContents(db, newSiteNode.getId(), masterLanguageId, masterLanguageId, null, new Integer(componentId), document, new Integer(componentContentId), principal);
    				componentStructure = XMLHelper.serializeDom(document, new StringBuffer()).toString();
    			}
        	}

        	//Create initial content version also... in masterlanguage
        	String versionValue = "<?xml version='1.0' encoding='UTF-8'?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes><Title><![CDATA[" + newSiteNode.getName() + "]]></Title><NavigationTitle><![CDATA[" + newSiteNode.getName() + "]]></NavigationTitle><NiceURIName><![CDATA[" + new VisualFormatter().replaceNiceURINonAsciiWithSpecifiedChars(newSiteNode.getName(), CmsPropertyHandler.getNiceURIDefaultReplacementCharacter()) + "]]></NiceURIName><Description><![CDATA[" + newSiteNode.getName() + "]]></Description><MetaInfo><![CDATA[" + newSiteNode.getName() + "]]></MetaInfo><ComponentStructure><![CDATA[" + componentStructure + "]]></ComponentStructure></attributes></article>";
        	ContentVersionVO contentVersionVO = new ContentVersionVO();
        	contentVersionVO.setVersionComment("Autogenerated version");
        	contentVersionVO.setVersionModifier(principal.getName());
        	contentVersionVO.setVersionValue(versionValue);
        	ContentVersionController.getContentVersionController().create(contentVO.getId(), masterLanguage.getId(), contentVersionVO, null, db);

        	//Also created a version in the local master language for this part of the site if any
        	LanguageVO localMasterLanguageVO = getInitialLanguageVO(db, parentFolderContent.getId(), repositoryId);
        	if(localMasterLanguageVO.getId().intValue() != masterLanguage.getId().intValue())
        	{
	        	String versionValueLocalMaster = "<?xml version='1.0' encoding='UTF-8'?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes><Title><![CDATA[" + newSiteNode.getName() + "]]></Title><NavigationTitle><![CDATA[" + newSiteNode.getName() + "]]></NavigationTitle><NiceURIName><![CDATA[" + new VisualFormatter().replaceNiceURINonAsciiWithSpecifiedChars(newSiteNode.getName(), CmsPropertyHandler.getNiceURIDefaultReplacementCharacter()) + "]]></NiceURIName><Description><![CDATA[" + newSiteNode.getName() + "]]></Description><MetaInfo><![CDATA[" + newSiteNode.getName() + "]]></MetaInfo><ComponentStructure><![CDATA[]]></ComponentStructure></attributes></article>";
	            ContentVersionVO contentVersionVOLocalMaster = new ContentVersionVO();
	        	contentVersionVOLocalMaster.setVersionComment("Autogenerated version");
	        	contentVersionVOLocalMaster.setVersionModifier(principal.getName());
	        	contentVersionVOLocalMaster.setVersionValue(versionValueLocalMaster);
	        	ContentVersionController.getContentVersionController().create(contentVO.getId(), localMasterLanguageVO.getId(), contentVersionVOLocalMaster, null, db);
        	}
        	
        	ServiceBindingVO serviceBindingVO = new ServiceBindingVO();
        	serviceBindingVO.setName(newSiteNode.getName() + " Metainfo");
        	serviceBindingVO.setPath("/None specified/");
        
        	String qualifyerXML = "<?xml version='1.0' encoding='UTF-8'?><qualifyer><contentId>" + contentVO.getId() + "</contentId></qualifyer>";
        
        	ServiceBindingController.getController().create(db, serviceBindingVO, qualifyerXML, availableServiceBindingId, siteNodeVersion.getId(), singleServiceDefinitionVO.getId());	
        }

        return content;
    }

	public LanguageVO getInitialLanguageVO(Database db, Integer contentId, Integer repositoryId) throws Exception
	{
		Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);

	    String initialLanguageId = ps.getString("content_" + contentId + "_initialLanguageId");
	    Content content = ContentController.getContentController().getContentWithId(contentId, db);
	    Content parentContent = content.getParentContent(); 
	    while((initialLanguageId == null || initialLanguageId.equalsIgnoreCase("-1")) && parentContent != null)
	    {
	        initialLanguageId = ps.getString("content_" + parentContent.getId() + "_initialLanguageId");
		    parentContent = parentContent.getParentContent(); 
	    }
	    
	    if(initialLanguageId != null && !initialLanguageId.equals("") && !initialLanguageId.equals("-1"))
	        return LanguageController.getController().getLanguageVOWithId(new Integer(initialLanguageId));
	    else
	        return LanguageController.getController().getMasterLanguage(repositoryId);
	}

	/**
	 * Recursive methods to get all sitenodes under the specific sitenode.
	 */ 
	
    public List getSiteNodeVOWithParentRecursive(Integer siteNodeId) throws ConstraintException, SystemException
	{
		return getSiteNodeVOWithParentRecursive(siteNodeId, new ArrayList());
	}
	
	private List getSiteNodeVOWithParentRecursive(Integer siteNodeId, List resultList) throws ConstraintException, SystemException
	{
		// Get the versions of this content.
		resultList.add(getSiteNodeVOWithId(siteNodeId));
		
		// Get the children of this content and do the recursion
		List childSiteNodeList = SiteNodeController.getController().getSiteNodeChildren(siteNodeId);
		Iterator cit = childSiteNodeList.iterator();
		while (cit.hasNext())
		{
		    SiteNodeVO siteNodeVO = (SiteNodeVO) cit.next();
			getSiteNodeVOWithParentRecursive(siteNodeVO.getId(), resultList);
		}
	
		return resultList;
	}


    public void setMetaInfoContentId(Integer siteNodeId, Integer metaInfoContentId) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
        	setMetaInfoContentId(siteNodeId, metaInfoContentId, db);
        	
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

    }       

    public void setMetaInfoContentId(Integer siteNodeId, Integer metaInfoContentId, Database db) throws ConstraintException, SystemException
    {
        SiteNode siteNode = getSiteNodeWithId(siteNodeId, db);
        siteNode.setMetaInfoContentId(metaInfoContentId);
    }       
    
    
    public List getSiteNodeVOListWithoutMetaInfoContentId() throws ConstraintException, SystemException
    {
		List siteNodeVOList = new ArrayList();

		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
            List siteNodes = getSiteNodesWithoutMetaInfoContentId(db);
            siteNodeVOList = toVOList(siteNodes);
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
        return siteNodeVOList;
    }       

    public List getSiteNodesWithoutMetaInfoContentId(Database db) throws ConstraintException, SystemException, Exception
    {
		List siteNodes = new ArrayList();

		OQLQuery oql = db.getOQLQuery("SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl sn WHERE sn.metaInfoContentId = $1");
    	oql.bind(new Integer(-1));
    	
    	QueryResults results = oql.execute();
		
		while(results.hasMore()) 
        {
        	SiteNode siteNode = (SiteNodeImpl)results.next();
        	siteNodes.add(siteNode);
        }

		results.close();
		oql.close();

		return siteNodes;
    }


    public SiteNodeVO getSiteNodeVOWithMetaInfoContentId(Integer contentId) throws ConstraintException, SystemException
    {
		SiteNodeVO siteNodeVO = null;

		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
        	SiteNode siteNode = getSiteNodeWithMetaInfoContentId(db, contentId);
        	if(siteNode != null)
        		siteNodeVO = siteNode.getValueObject();
        	
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
        return siteNodeVO;
    }       

    public SiteNodeVO getSiteNodeVOWithMetaInfoContentId(Database db, Integer contentId) throws ConstraintException, SystemException, Exception
    {
		SiteNodeVO siteNodeVO = null;

		OQLQuery oql = db.getOQLQuery("SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl sn WHERE sn.metaInfoContentId = $1");
    	oql.bind(contentId);
    	
    	QueryResults results = oql.execute(Database.READONLY);
		
		if(results.hasMore()) 
        {
			SiteNode siteNode = (SiteNodeImpl)results.next();
			siteNodeVO = siteNode.getValueObject();
        }

		results.close();
		oql.close();

		return siteNodeVO;
    }

    public SiteNode getSiteNodeWithMetaInfoContentId(Database db, Integer contentId) throws ConstraintException, SystemException, Exception
    {
		SiteNode siteNode = null;

		OQLQuery oql = db.getOQLQuery("SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl sn WHERE sn.metaInfoContentId = $1");
    	oql.bind(contentId);
    	
    	QueryResults results = oql.execute();
		
		if(results.hasMore()) 
        {
			siteNode = (SiteNodeImpl)results.next();
        }

		results.close();
		oql.close();

		return siteNode;
    }
    
	/**
	 * This method returns true if the if the siteNode in question is protected.
	 */
    
	public Integer getProtectedSiteNodeVersionId(Integer siteNodeId)
	{
		Integer protectedSiteNodeVersionId = null;
	
		try
		{
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(siteNodeId);

			if(siteNodeVersionVO.getIsProtected() != null)
			{	
				if(siteNodeVersionVO.getIsProtected().intValue() == NO.intValue())
					protectedSiteNodeVersionId = null;
				else if(siteNodeVersionVO.getIsProtected().intValue() == YES.intValue())
					protectedSiteNodeVersionId = siteNodeVersionVO.getId();
				else if(siteNodeVersionVO.getIsProtected().intValue() == INHERITED.intValue())
				{
					SiteNodeVO parentSiteNodeVO = getParentSiteNode(siteNodeId);
					if(parentSiteNodeVO != null)
						protectedSiteNodeVersionId = getProtectedSiteNodeVersionId(parentSiteNodeVO.getId()); 
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get which (if any) site node is protected:" + e.getMessage(), e);
		}
			
		return protectedSiteNodeVersionId;
	}

	public String getSiteNodePath(Integer siteNodeId, Database db) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		
		SiteNodeVO siteNodeVO = getSiteNodeVOWithId(siteNodeId, db);
		while(siteNodeVO != null)
		{
			sb.insert(0, "/" + siteNodeVO.getName());
			if(siteNodeVO.getParentSiteNodeId() != null)
				siteNodeVO = getSiteNodeVOWithId(siteNodeVO.getParentSiteNodeId(), db);
			else
				siteNodeVO = null;
		}
			
		return sb.toString();
	}

	public List<SiteNodeVO> getUpcomingExpiringSiteNodes(int numberOfWeeks) throws Exception
	{
		List<SiteNodeVO> siteNodeVOList = new ArrayList<SiteNodeVO>();

		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
    		OQLQuery oql = db.getOQLQuery("SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl sn WHERE " +
    				"sn.expireDateTime > $1 AND sn.expireDateTime < $2 AND sn.publishDateTime < $3");

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
    			SiteNode siteNode = (SiteNodeImpl)results.next();
    			siteNodeVOList.add(siteNode.getValueObject());
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
        
        return siteNodeVOList;
	}

	public List<SiteNodeVO> getUpcomingExpiringSiteNodes(int numberOfDays, InfoGluePrincipal principal) throws Exception
	{
		List<SiteNodeVO> siteNodeVOList = new ArrayList<SiteNodeVO>();

		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
    		OQLQuery oql = db.getOQLQuery("SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl sn WHERE " +
    				"sn.expireDateTime > $1 AND sn.expireDateTime < $2 AND sn.publishDateTime < $3 AND sn.siteNodeVersions.versionModifier = $4");
    		
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
    			SiteNode siteNode = (SiteNodeImpl)results.next();
    			siteNodeVOList.add(siteNode.getValueObject());
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
        
        return siteNodeVOList;
	}

	/**
	 * This method are here to return all content versions that are x number of versions behind the current active version. This is for cleanup purposes.
	 * 
	 * @param numberOfVersionsToKeep
	 * @return
	 * @throws SystemException 
	 */
	
	public int cleanSiteNodeVersions(int numberOfVersionsToKeep, boolean keepOnlyOldPublishedVersions, long minimumTimeBetweenVersionsDuringClean, boolean deleteVersions) throws SystemException 
	{
		int cleanedVersions = 0;
		
		int batchLimit = 20;

		List<SiteNodeVersionVO> siteNodeVersionVOList = getSiteNodeVersionVOList(numberOfVersionsToKeep, keepOnlyOldPublishedVersions, minimumTimeBetweenVersionsDuringClean);
			
		logger.info("Deleting " + siteNodeVersionVOList.size() + " versions");
		int maxIndex = (siteNodeVersionVOList.size() > batchLimit ? batchLimit : siteNodeVersionVOList.size());
		List partList = siteNodeVersionVOList.subList(0, maxIndex);
		while(partList.size() > 0)
		{
			if(deleteVersions)
				cleanVersions(numberOfVersionsToKeep, partList);
			cleanedVersions = cleanedVersions + partList.size();
			partList.clear();
			maxIndex = (siteNodeVersionVOList.size() > batchLimit ? batchLimit : siteNodeVersionVOList.size());
			partList = siteNodeVersionVOList.subList(0, maxIndex);
		}

		return cleanedVersions;
	}
	
	
	/**
	 * This method are here to return all content versions that are x number of versions behind the current active version. This is for cleanup purposes.
	 * 
	 * @param numberOfVersionsToKeep
	 * @return
	 * @throws SystemException 
	 */
	
	public List<SiteNodeVersionVO> getSiteNodeVersionVOList(int numberOfVersionsToKeep, boolean keepOnlyOldPublishedVersions, long minimumTimeBetweenVersionsDuringClean) throws SystemException 
	{
		logger.info("numberOfVersionsToKeep:" + numberOfVersionsToKeep);

		Database db = CastorDatabaseService.getDatabase();
    	
    	List<SiteNodeVersionVO> siteNodeVersionsIdList = new ArrayList();

        beginTransaction(db);

        try
        {
            OQLQuery oql = db.getOQLQuery("SELECT snv FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl snv ORDER BY snv.siteNodeId, snv.siteNodeVersionId desc");
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
        	int numberOfLaterVersions = 0;
        	Integer previousSiteNodeId = null;
        	Date previousDate = null;
        	long difference = -1;
        	List keptSiteNodeVersionVOList = new ArrayList();
        	List potentialSiteNodeVersionVOList = new ArrayList();
        	List versionInitialSuggestions = new ArrayList();
        	List versionNonPublishedSuggestions = new ArrayList();

        	while (results.hasMore())
            {
        		SmallSiteNodeVersionImpl version = (SmallSiteNodeVersionImpl)results.next();
				if(previousSiteNodeId != null && previousSiteNodeId.intValue() != version.getSiteNodeId().intValue())
				{
					logger.info("previousSiteNodeId:" + previousSiteNodeId);
					if(minimumTimeBetweenVersionsDuringClean != -1 && versionInitialSuggestions.size() > numberOfVersionsToKeep)
					{						
						Iterator potentialSiteNodeVersionVOListIterator = potentialSiteNodeVersionVOList.iterator();
						while(potentialSiteNodeVersionVOListIterator.hasNext())
						{
							SiteNodeVersionVO potentialSiteNodeVersionVO = (SiteNodeVersionVO)potentialSiteNodeVersionVOListIterator.next();
							
							SiteNodeVersionVO firstInitialSuggestedSiteNodeVersionVO = null;
							Iterator versionInitialSuggestionsIterator = versionInitialSuggestions.iterator();
							while(versionInitialSuggestionsIterator.hasNext())
							{
								SiteNodeVersionVO initialSuggestedSiteNodeVersionVO = (SiteNodeVersionVO)versionInitialSuggestionsIterator.next();
								if(initialSuggestedSiteNodeVersionVO.getStateId().equals(ContentVersionVO.PUBLISHED_STATE))
								{
									firstInitialSuggestedSiteNodeVersionVO = initialSuggestedSiteNodeVersionVO;
									break;
								}
							}
							
							if(firstInitialSuggestedSiteNodeVersionVO != null)
							{
								keptSiteNodeVersionVOList.remove(potentialSiteNodeVersionVO);
								keptSiteNodeVersionVOList.add(firstInitialSuggestedSiteNodeVersionVO);
								versionInitialSuggestions.remove(firstInitialSuggestedSiteNodeVersionVO);
								versionInitialSuggestions.add(potentialSiteNodeVersionVO);
							}
						}
					}
					
					siteNodeVersionsIdList.addAll(versionNonPublishedSuggestions);
					siteNodeVersionsIdList.addAll(versionInitialSuggestions);
					potentialSiteNodeVersionVOList.clear();
					versionInitialSuggestions.clear();
					versionNonPublishedSuggestions.clear();
					keptSiteNodeVersionVOList.clear();
					
					numberOfLaterVersions = 0;
					previousDate = null;
					difference = -1;
					potentialSiteNodeVersionVOList = new ArrayList();
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
					keptSiteNodeVersionVOList.add(version.getValueObject());
					potentialSiteNodeVersionVOList.add(version.getValueObject());		
					numberOfLaterVersions++;
				}
				else
				{
					keptSiteNodeVersionVOList.add(version.getValueObject());
					previousDate = version.getModifiedDateTime();
					numberOfLaterVersions++;
				}

				previousSiteNodeId = version.getSiteNodeId();
            }
            
			results.close();
			oql.close();

			commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
		return siteNodeVersionsIdList;
	}

	/**
	 * Cleans the list of versions - even published ones. Use with care only for cleanup purposes.
	 * 
	 * @param numberOfVersionsToKeep
	 * @param contentVersionVOList
	 * @throws SystemException
	 */
	
	private void cleanVersions(int numberOfVersionsToKeep, List siteNodeVersionVOList) throws SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
    	
        beginTransaction(db);

        try
        {
			Iterator<SiteNodeVersionVO> siteNodeVersionVOListIterator = siteNodeVersionVOList.iterator();
			while(siteNodeVersionVOListIterator.hasNext())
			{
				SiteNodeVersionVO siteNodeVersionVO = siteNodeVersionVOListIterator.next();
				SiteNodeVersion siteNodeVersion = getSiteNodeVersionWithId(siteNodeVersionVO.getId(), db);
				logger.info("Deleting the siteNodeVersion " + siteNodeVersion.getId() + " on siteNode " + siteNodeVersion.getOwningSiteNode());
				delete(siteNodeVersion, db, true);
			}

			commitTransaction(db);

			Thread.sleep(1000);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        finally
        {
        	closeDatabase(db);
        }
	}

	/**
	 * This method deletes an contentversion and notifies the owning content.
	 */
	
 	public void delete(SiteNodeVersion siteNodeVersion, Database db, boolean forceDelete) throws ConstraintException, SystemException, Exception
	{
		if (!forceDelete && siteNodeVersion.getStateId().intValue() == ContentVersionVO.PUBLISHED_STATE.intValue() && siteNodeVersion.getIsActive().booleanValue() == true)
		{
			throw new ConstraintException("SiteNodeVersion.stateId", "3300", siteNodeVersion.getOwningSiteNode().getName());
		}
		
		ServiceBindingController.deleteServiceBindingsReferencingSiteNodeVersion(siteNodeVersion, db);

		SiteNode siteNode = siteNodeVersion.getOwningSiteNode();

		if(siteNode != null)
			siteNode.getSiteNodeVersions().remove(siteNodeVersion);

		db.remove(siteNodeVersion);
	}

	public void updateSiteNodeTypeDefinition(Integer siteNodeId, Integer siteNodeTypeDefinitionId) throws ConstraintException, SystemException, Exception
	{
		Database db = CastorDatabaseService.getDatabase();
    	
        beginTransaction(db);

        try
        {
        	SiteNode siteNode = getSiteNodeWithId(siteNodeId, db);
        	SiteNodeTypeDefinition sntd = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionWithId(siteNodeTypeDefinitionId, db);
        	if(siteNode != null && sntd != null)
        		siteNode.setSiteNodeTypeDefinition((SiteNodeTypeDefinitionImpl)sntd);
        	
        	commitTransaction(db);

			Thread.sleep(1000);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        finally
        {
        	closeDatabase(db);
        }
	}
	
	/**
	 * This method checks if there are published versions available for the siteNodeVersion.
	 */
	
	public boolean hasPublishedVersion(Integer siteNodeId)
	{
		boolean hasPublishedVersion = false;
		
		try
		{
			SiteNodeVersionVO siteNodeVersion = SiteNodeVersionController.getLatestPublishedSiteNodeVersionVO(siteNodeId);
			if(siteNodeVersion != null)
			{
				hasPublishedVersion = true;
			}
		}
		catch(Exception e)
		{
			logger.warn("Exception when generating buttons:" + e.getMessage(), e);
		}
				
		return hasPublishedVersion;
	}
}
 
