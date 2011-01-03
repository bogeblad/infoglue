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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.AvailableServiceBinding;
import org.infoglue.cms.entities.management.AvailableServiceBindingVO;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.RegistryVO;
import org.infoglue.cms.entities.structure.ServiceBinding;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.DateHelper;
import org.infoglue.deliver.util.Timer;

public class SiteNodeVersionController extends BaseController 
{
    private final static Logger logger = Logger.getLogger(SiteNodeVersionController.class.getName());

	private final RegistryController registryController = RegistryController.getController();

	/**
	 * Factory method
	 */

	public static SiteNodeVersionController getController()
	{
		return new SiteNodeVersionController();
	}
	
    public SiteNodeVersionVO getFullSiteNodeVersionVOWithId(Integer siteNodeVersionId) throws SystemException, Bug
    {
		return (SiteNodeVersionVO) getVOWithId(SiteNodeVersionImpl.class, siteNodeVersionId);
    }

    public SiteNodeVersionVO getSiteNodeVersionVOWithId(Integer siteNodeVersionId) throws SystemException, Bug
    {
		return (SiteNodeVersionVO) getVOWithId(SmallSiteNodeVersionImpl.class, siteNodeVersionId);
    }

    public SiteNodeVersionVO getSiteNodeVersionVOWithId(Integer siteNodeVersionId, Database db) throws SystemException, Bug
    {
		return (SiteNodeVersionVO) getVOWithId(SmallSiteNodeVersionImpl.class, siteNodeVersionId, db);
    }
   	
	public SiteNodeVersion getSiteNodeVersionWithId(Integer siteNodeVersionId, Database db) throws SystemException, Bug
    {
		return (SiteNodeVersion) getObjectWithId(SiteNodeVersionImpl.class, siteNodeVersionId, db);
    }

    public static SiteNodeVersion getSiteNodeVersionWithIdAsReadOnly(Integer siteNodeVersionId, Database db) throws SystemException, Bug
    {
		return (SiteNodeVersion) getObjectWithIdAsReadOnly(SiteNodeVersionImpl.class, siteNodeVersionId, db);
    }

    public List getSiteNodeVersionVOList() throws SystemException, Bug
    {
        return getAllVOObjects(SmallSiteNodeVersionImpl.class, "siteNodeVersionId");
    }

    public static void delete(SiteNodeVersionVO siteNodeVersionVO) throws ConstraintException, SystemException
    {
    	deleteEntity(SiteNodeVersionImpl.class, siteNodeVersionVO.getSiteNodeVersionId());
    }        

	/**
	 * This method creates a new siteNodeVersion for the siteNode sent in.
	 */
	
	public List<SiteNodeVersionVO> getSiteNodeVersionVOList(Integer siteNodeId) throws SystemException, Bug
	{
		List<SiteNodeVersionVO> siteNodeVersionVOList = new ArrayList<SiteNodeVersionVO>();
		
		Database db = CastorDatabaseService.getDatabase();
        
        beginTransaction(db);
        try
        {
        	siteNodeVersionVOList = getSiteNodeVersionVOList(db, siteNodeId);
            
        	commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            throw new SystemException(e.getMessage());
        }
    	
		return siteNodeVersionVOList;		
	}

	public List<SiteNodeVersionVO> getSiteNodeVersionVOList(Database db, Integer siteNodeId) throws SystemException, Bug, Exception
    {
		List<SiteNodeVersionVO> siteNodeVersionVOList = new ArrayList<SiteNodeVersionVO>();
		
	    OQLQuery oql = db.getOQLQuery( "SELECT snv FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl snv WHERE snv.siteNodeId = $1 ORDER BY snv.siteNodeVersionId desc");
		oql.bind(siteNodeId);
		
		QueryResults results = oql.execute(Database.READONLY);
		
		while(results.hasMore()) 
	    {
	    	SiteNodeVersion siteNodeVersion = (SiteNodeVersion)results.next();
	    	siteNodeVersionVOList.add(siteNodeVersion.getValueObject());
	    }

		results.close();
		oql.close();

		return siteNodeVersionVOList;
    }

    /**
     * This method removes the siteNodeVersion and also all associated bindings.
     * @param siteNodeVersion
     * @param db
     * @throws ConstraintException
     * @throws SystemException
     */
    public void delete(SiteNodeVersion siteNodeVersion, Database db) throws ConstraintException, SystemException
    {
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		try
		{
			SiteNode siteNode = siteNodeVersion.getOwningSiteNode();
		    Collection serviceBindings = siteNodeVersion.getServiceBindings();
		    Iterator serviceBindingsIterator = serviceBindings.iterator();
		    while(serviceBindingsIterator.hasNext())
		    {
		        ServiceBinding serviceBinding = (ServiceBinding)serviceBindingsIterator.next();
		        serviceBindingsIterator.remove();
		        db.remove(serviceBinding);
		    }
		    
		    if(siteNode != null)
		        siteNode.getSiteNodeVersions().remove(siteNodeVersion);
			
		    db.remove(siteNodeVersion);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not completes the transaction:" + e, e);
			throw new SystemException(e.getMessage());
		}
    }        
	

	/**
	 * This method creates an initial siteNodeVersion for the siteNode sent in and within the transaction sent in.
	 */
	
	public static SiteNodeVersion createInitialSiteNodeVersion(Database db, SiteNode siteNode, InfoGluePrincipal infoGluePrincipal) throws SystemException, Bug
	{
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		SiteNodeVersion siteNodeVersion = null;

		try
		{
			//SiteNode siteNode = SiteNodeController.getSiteNodeWithId(siteNodeId, db);
            
			siteNodeVersion = new SiteNodeVersionImpl();
			siteNodeVersion.setIsCheckedOut(new Boolean(false));
			siteNodeVersion.setModifiedDateTime(DateHelper.getSecondPreciseDate());
			siteNodeVersion.setOwningSiteNode((SiteNodeImpl)siteNode);
			siteNodeVersion.setStateId(new Integer(0));
			siteNodeVersion.setVersionComment("Initial version");
			siteNodeVersion.setVersionModifier(infoGluePrincipal.getName());
			siteNodeVersion.setVersionNumber(new Integer(1));
        	
			db.create((SiteNodeVersion)siteNodeVersion);
			
			List siteNodeVersions = new ArrayList();
			siteNodeVersions.add(siteNodeVersion);
			siteNode.setSiteNodeVersions(siteNodeVersions);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not completes the transaction:" + e, e);
			throw new SystemException(e.getMessage());
		}
    	
		return siteNodeVersion;		
	}

	/**
	 * This method creates a new siteNodeVersion for the siteNode sent in.
	 */
	
	public static SiteNodeVersion create(Integer siteNodeId, InfoGluePrincipal infoGluePrincipal, SiteNodeVersionVO siteNodeVersionVO) throws SystemException, Bug
	{
    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

    	SiteNodeVersion siteNodeVersion = null;

        beginTransaction(db);

        try
        {
        	SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithId(siteNodeId, db);
            
        	siteNodeVersion = new SiteNodeVersionImpl();
        	siteNodeVersion.setOwningSiteNode((SiteNodeImpl)siteNode);
        	siteNodeVersion.setVersionModifier(infoGluePrincipal.getName());
        	siteNodeVersion.setValueObject(siteNodeVersionVO);
        	
        	//Remove later and use a lookup....
        	siteNodeVersion.setVersionNumber(new Integer(1));
        	
        	siteNodeVersion = (SiteNodeVersion)createEntity(siteNodeVersion, db);
            //commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            //rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    	
		return siteNodeVersion;		
	}


	/**
	 * This method creates a new siteNodeVersion for the siteNode sent in.
	 */
	
	public static SiteNodeVersion create(Integer siteNodeId, InfoGluePrincipal infoGluePrincipal, SiteNodeVersionVO siteNodeVersionVO, Database db) throws SystemException, Bug, Exception
	{
    	SiteNodeVersion siteNodeVersion = null;

    	SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithId(siteNodeId, db);
        
    	siteNodeVersion = new SiteNodeVersionImpl();
    	siteNodeVersion.setOwningSiteNode((SiteNodeImpl)siteNode);
    	siteNodeVersion.setVersionModifier(infoGluePrincipal.getName());
    	siteNodeVersion.setValueObject(siteNodeVersionVO);
    	//Remove later and use a lookup....
    	siteNodeVersion.setVersionNumber(new Integer(1));
    	
    	db.create(siteNodeVersion);
    	
		return siteNodeVersion;		
	}

	public SiteNodeVersionVO getAndRepairLatestSiteNodeVersionVO(Integer siteNodeId) throws SystemException, Bug
    {
		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

    	SiteNodeVersionVO siteNodeVersionVO = null;

        beginTransaction(db);

        try
        {
        	SiteNodeVersion siteNodeVersion = getAndRepairLatestSiteNodeVersion(db, siteNodeId);
        	if(siteNodeVersion != null)
        		siteNodeVersionVO = siteNodeVersion.getValueObject();
        	
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
		return siteNodeVersionVO;
    }

	public SiteNodeVersion getAndRepairLatestSiteNodeVersion(Database db, Integer siteNodeId) throws SystemException, Bug
    {
    	SiteNodeVersion siteNodeVersion = getLatestSiteNodeVersion(db, siteNodeId, false);
    	if(siteNodeVersion != null)
    	{
    		siteNodeVersion.setIsActive(true);
    		siteNodeVersion.setStateId(SiteNodeVersionVO.WORKING_STATE);
        }
        
		return siteNodeVersion;
    }

	
	public ContentVersionVO getAndRepairLatestContentVersionVO(Integer contentId, Integer languageId) throws SystemException, Bug
    {
		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

    	ContentVersionVO contentVersionVO = null;

        beginTransaction(db);

        try
        {
        	ContentVersion contentVersion = getAndRepairLatestContentVersion(db, contentId, languageId);
        	if(contentVersion != null)
        		contentVersionVO = contentVersion.getValueObject();
        			
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
		return contentVersionVO;
    }

	public ContentVersion getAndRepairLatestContentVersion(Database db, Integer contentId, Integer languageId) throws SystemException, Bug, Exception
    {
    	ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestContentVersion(contentId, languageId, db);
    	if(contentVersion != null)
    	{
    		contentVersion.setIsActive(true);
    		contentVersion.setStateId(ContentVersionVO.WORKING_STATE);
        }
        
		return contentVersion;
    }

	
	public SiteNodeVersionVO getLatestActiveSiteNodeVersionVO(Integer siteNodeId) throws SystemException, Bug
    {
		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

    	SiteNodeVersionVO siteNodeVersionVO = null;

        beginTransaction(db);

        try
        {
        	siteNodeVersionVO = getLatestActiveSiteNodeVersionVO(db, siteNodeId);
            /*
        	SiteNodeVersion siteNodeVersion = getLatestActiveSiteNodeVersion(db, siteNodeId);
            if(siteNodeVersion != null)
                siteNodeVersionVO = siteNodeVersion.getValueObject();
            */
        		
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
		return siteNodeVersionVO;
    }

	public SiteNodeVersion getLatestActiveSiteNodeVersion(Database db, Integer siteNodeId) throws SystemException, Bug, Exception
    {
	    SiteNodeVersion siteNodeVersion = null;
	    
	    OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl cv WHERE cv.owningSiteNode.siteNodeId = $1 AND cv.isActive = $2 ORDER BY cv.siteNodeVersionId desc");
		oql.bind(siteNodeId);
		oql.bind(new Boolean(true));
		
		QueryResults results = oql.execute();
		
		if (results.hasMore()) 
	    {
	    	siteNodeVersion = (SiteNodeVersion)results.next();
        }

		results.close();
		oql.close();

		return siteNodeVersion;
    }

	public SiteNodeVersionVO getLatestActiveSiteNodeVersionVO(Database db, Integer siteNodeId) throws SystemException, Bug, Exception
    {
	    SiteNodeVersionVO siteNodeVersionVO = null;
	    
	    SiteNodeVersion siteNodeVersion = null;
	    
	    OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl cv WHERE cv.siteNodeId = $1 AND cv.isActive = $2 ORDER BY cv.siteNodeVersionId desc");
		oql.bind(siteNodeId);
		oql.bind(new Boolean(true));
		
		QueryResults results = oql.execute(Database.ReadOnly);
		
		if (results.hasMore()) 
	    {
	    	siteNodeVersion = (SiteNodeVersion)results.next();
        }

		results.close();
		oql.close();

		/*
	    SiteNodeVersion siteNodeVersion = getLatestActiveSiteNodeVersion(db, siteNodeId);
	    */
	    if(siteNodeVersion != null)
	    	siteNodeVersionVO = siteNodeVersion.getValueObject();
	    else
	    	logger.warn("The siteNode " + siteNodeId + " did not have a latest active siteNodeVersion - very strange.");
	    
		return siteNodeVersionVO;
    }

	public SiteNodeVersionVO getLatestSiteNodeVersionVO(Integer siteNodeId) throws SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

    	SiteNodeVersionVO siteNodeVersionVO = null;

        beginTransaction(db);

        try
        {
            OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl cv WHERE cv.siteNodeId = $1 ORDER BY cv.siteNodeVersionId desc");
        	oql.bind(siteNodeId);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
            {
            	SiteNodeVersion siteNodeVersion = (SiteNodeVersion)results.next();
            	logger.info("found one:" + siteNodeVersion.getValueObject());
            	siteNodeVersionVO = siteNodeVersion.getValueObject();
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
    	
		return siteNodeVersionVO;
    }
    
	
	public SiteNodeVersionVO getLatestSiteNodeVersionVO(Database db, Integer siteNodeId) throws SystemException, Bug, Exception
    {
    	SiteNodeVersionVO siteNodeVersionVO = null;

        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl cv WHERE cv.siteNodeId = $1 ORDER BY cv.siteNodeVersionId desc");
    	oql.bind(siteNodeId);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		if (results.hasMore()) 
        {
        	SiteNodeVersion siteNodeVersion = (SiteNodeVersion)results.next();
        	logger.info("found one:" + siteNodeVersion.getValueObject());
        	siteNodeVersionVO = siteNodeVersion.getValueObject();
        }
    	
		results.close();
		oql.close();

		return siteNodeVersionVO;
    }

	/**
	 * This is a method used to get the latest site node version of a sitenode within a given transaction.
	 */

	public SiteNodeVersion getLatestSiteNodeVersion(Database db, Integer siteNodeId, boolean readOnly) throws SystemException, Bug
	{
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		SiteNodeVersion siteNodeVersion = null;

		try
		{
			OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl cv WHERE cv.owningSiteNode.siteNodeId = $1 ORDER BY cv.siteNodeVersionId desc");
			oql.bind(siteNodeId);
        	
			QueryResults results = null;
			if(readOnly)
			    results = oql.execute(Database.ReadOnly);
			else
			{
				this.logger.info("Fetching entity in read/write mode");
				results = oql.execute();
			}
			
			if (results.hasMore()) 
			{
				siteNodeVersion = (SiteNodeVersion)results.next();
			}

			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not completes the transaction:" + e, e);
			throw new SystemException(e.getMessage());
		}
    	
		return siteNodeVersion;
	}

    
    public SiteNodeVersionVO updateStateId(Integer siteNodeVersionId, Integer stateId, String versionComment, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId) throws ConstraintException, SystemException
    {
    	SiteNodeVersionVO siteNodeVersionVO = getSiteNodeVersionVOWithId(siteNodeVersionId);
    	SiteNodeVersionVO returnVO = null;
    	
    	//Here we just updates the state if it's a publish-state-change.
    	if(stateId.intValue() == 2)
    	{    		
	    	siteNodeVersionVO.setStateId(stateId);
	    	siteNodeVersionVO.setVersionComment(versionComment);
	    	returnVO = (SiteNodeVersionVO) updateEntity(SiteNodeVersionImpl.class, siteNodeVersionVO);
    	}
    	    	
    	//Here we create a new version if it was a state-change back to working
    	if(stateId.intValue() == 0)
    	{
			siteNodeVersionVO.setStateId(stateId);
			siteNodeVersionVO.setVersionComment("");
			create(siteNodeId, infoGluePrincipal, siteNodeVersionVO);
			returnVO = getLatestSiteNodeVersionVO(siteNodeId);
    	}
    	
    	return returnVO;
    }        

    public SiteNodeVersionVO updateStateId(Integer siteNodeVersionId, Integer stateId, String versionComment, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Database db) throws ConstraintException, SystemException, Exception
    {
    	SiteNodeVersionVO siteNodeVersionVO = getSiteNodeVersionWithId(siteNodeVersionId, db).getValueObject();
    	SiteNodeVersionVO returnVO = null;
    	
    	//Here we just updates the state if it's a publish-state-change.
    	if(stateId.intValue() == 2)
    	{    		
	    	siteNodeVersionVO.setStateId(stateId);
	    	siteNodeVersionVO.setVersionComment(versionComment);
	    	returnVO = (SiteNodeVersionVO) updateEntity(SiteNodeVersionImpl.class, siteNodeVersionVO, db);
    	}
    	    	
    	//Here we create a new version if it was a state-change back to working
    	if(stateId.intValue() == 0)
    	{
			siteNodeVersionVO.setStateId(stateId);
			siteNodeVersionVO.setVersionComment("");
			returnVO = create(siteNodeId, infoGluePrincipal, siteNodeVersionVO, db).getValueObject();
			//returnVO = getLatestSiteNodeVersionVO(siteNodeId, db);
    	}
    	
    	return returnVO;
    }        
    
    public SiteNodeVersion updateStateId(SiteNodeVersion siteNodeVersion, Integer stateId, String versionComment, InfoGluePrincipal infoGluePrincipal, Database db) throws ConstraintException, SystemException, Exception
    {
    	if(siteNodeVersion.getStateId().equals(stateId))
    	{
    		siteNodeVersion.setVersionComment(versionComment);
    		siteNodeVersion.setVersionModifier(infoGluePrincipal.getName());
    		return siteNodeVersion;
    	}
    	
    	//Here we just updates the state if it's a publish-state-change.
    	if(stateId.intValue() == 2)
    	{    		
    		siteNodeVersion.setStateId(stateId);
    		siteNodeVersion.setVersionComment(versionComment);
    		siteNodeVersion.setVersionModifier(infoGluePrincipal.getName());
    	}
    	    	
    	//Here we create a new version if it was a state-change back to working
    	if(stateId.intValue() == 0)
    	{
    		SiteNodeVersionVO siteNodeVersionVO = new SiteNodeVersionVO();
    		siteNodeVersionVO.setContentType(siteNodeVersion.getContentType());
    		siteNodeVersionVO.setDisableEditOnSight(siteNodeVersion.getDisableEditOnSight());
    		siteNodeVersionVO.setDisableForceIdentityCheck(siteNodeVersion.getDisableForceIdentityCheck());
    		siteNodeVersionVO.setForceProtocolChange(siteNodeVersion.getForceProtocolChange());
    		System.out.println("OLD getForceProtocolChange:" + siteNodeVersion.getForceProtocolChange());
    		siteNodeVersionVO.setDisableLanguages(siteNodeVersion.getDisableLanguages());
    		siteNodeVersionVO.setDisablePageCache(siteNodeVersion.getDisablePageCache());
    		siteNodeVersionVO.setIsActive(siteNodeVersion.getIsActive());
    		siteNodeVersionVO.setIsCheckedOut(siteNodeVersion.getIsCheckedOut());
    		siteNodeVersionVO.setIsHidden(siteNodeVersion.getIsHidden());
    		siteNodeVersionVO.setIsProtected(siteNodeVersion.getIsProtected());
    		siteNodeVersionVO.setPageCacheKey(siteNodeVersion.getPageCacheKey());
    		siteNodeVersionVO.setPageCacheTimeout(siteNodeVersion.getPageCacheTimeout());
    		siteNodeVersionVO.setSortOrder(siteNodeVersion.getSortOrder());
    		siteNodeVersionVO.setVersionModifier(infoGluePrincipal.getName());
    		siteNodeVersionVO.setVersionNumber(siteNodeVersion.getVersionNumber() + 1);
    		siteNodeVersionVO.setStateId(stateId);
			siteNodeVersionVO.setVersionComment(versionComment);
			
			siteNodeVersion = create(siteNodeVersion.getValueObject().getSiteNodeId(), infoGluePrincipal, siteNodeVersionVO, db);
    	}
    	
    	return siteNodeVersion;
    }        

	public static void deleteVersionsForSiteNodeWithId(Integer siteNodeId) throws ConstraintException, SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);
		List siteNodeVersions = new ArrayList();
        try
        {
            OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl cv WHERE cv.owningSiteNode.siteNodeId = $1");
        	oql.bind(siteNodeId);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			while (results.hasMore()) 
            {
            	SiteNodeVersion siteNodeVersion = (SiteNodeVersion)results.next();
				siteNodeVersions.add(siteNodeVersion.getValueObject());
            }
            
			results.close();
			oql.close();

			commitTransaction(db);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

		Iterator i = siteNodeVersions.iterator();
		while(i.hasNext())
		{
			SiteNodeVersionVO siteNodeVersionVO = (SiteNodeVersionVO)i.next();
			delete(siteNodeVersionVO);
		}    	
    }

	/** 
	 * This methods deletes all versions for the siteNode sent in
	 */

	public static void deleteVersionsForSiteNode(SiteNode siteNode, Database db, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException, Bug, Exception
    {
       	Collection siteNodeVersions = Collections.synchronizedCollection(siteNode.getSiteNodeVersions());
       	Iterator siteNodeVersionIterator = siteNodeVersions.iterator();
			
       	boolean metaInfoContentDeleted = false;
		while (siteNodeVersionIterator.hasNext()) 
        {
        	SiteNodeVersion siteNodeVersion = (SiteNodeVersion)siteNodeVersionIterator.next();
			Collection serviceBindings = Collections.synchronizedCollection(siteNodeVersion.getServiceBindings());
			Iterator serviceBindingIterator = serviceBindings.iterator();
			while(serviceBindingIterator.hasNext())
			{
				ServiceBinding serviceBinding = (ServiceBinding)serviceBindingIterator.next();
				if(serviceBinding.getAvailableServiceBinding().getName().equalsIgnoreCase("Meta information"))
				{
				    if(!metaInfoContentDeleted)
				    {
				    	try
				    	{
				    		deleteMetaInfoForSiteNodeVersion(db, serviceBinding, infoGluePrincipal);
				    	}
				    	catch(Exception e)
				    	{
				    		logger.error("An error was thrown when we tried to delete the meta info for the version. Could be deleted allready");
				    	}
				    	metaInfoContentDeleted = true;
				    }
				    serviceBindingIterator.remove();
				    db.remove(serviceBinding);
				}
				else
				{			
				    serviceBindingIterator.remove();
				    db.remove(serviceBinding);
				}
			}
	    	
			logger.info("Deleting siteNodeVersion:" + siteNodeVersion.getSiteNodeVersionId());
        	siteNodeVersionIterator.remove();
			db.remove(siteNodeVersion);
        }		    	
    }

	/**
	 * Deletes the meta info for the sitenode version.
	 * 
	 * @param siteNodeVersionId
	 * @return
	 * @throws ConstraintException
	 * @throws SystemException
	 */
	private static void deleteMetaInfoForSiteNodeVersion(Database db, ServiceBinding serviceBinding, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException, Bug, Exception
	{
		List boundContents = ContentController.getBoundContents(db, serviceBinding.getId()); 			
		if(boundContents.size() > 0)
		{
			ContentVO contentVO = (ContentVO)boundContents.get(0);
			ContentController.getContentController().delete(contentVO, db, true, true, true, infoGluePrincipal);
		}
	}
	
	
   	/**
	 * This method returns a list with AvailableServiceBidningVO-objects which are available for the
	 * siteNodeTypeDefinition sent in
	 */
	
	public static List getServiceBindningVOList(Integer siteNodeVersionId) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        List serviceBindningVOList = null;

        beginTransaction(db);

        try
        {
            Collection serviceBindningList = getServiceBindningList(siteNodeVersionId, db, true);
        	serviceBindningVOList = toVOList(serviceBindningList);
        	
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

        return serviceBindningVOList;
	}

	
   	/**
	 * This method returns a list with AvailableServiceBidningVO-objects which are available for the
	 * siteNodeTypeDefinition sent in
	 */
	
	public static List getServiceBindningVOList(Integer siteNodeVersionId, Database db) throws ConstraintException, SystemException, Exception
	{
        List serviceBindningVOList = null;
        
        Collection serviceBindningList = getServiceBindningList(siteNodeVersionId, db, true);

        serviceBindningVOList = toVOList(serviceBindningList);

        return serviceBindningVOList;
	}

   	/**
	 * This method returns a list with AvailableServiceBidningVO-objects which are available for the
	 * siteNodeTypeDefinition sent in
	 */
	
	public static Collection getServiceBindningList(Integer siteNodeVersionId, Database db, boolean readOnly) throws ConstraintException, SystemException, Exception
	{
    	Collection serviceBindings = new ArrayList();
    		
		OQLQuery oql = db.getOQLQuery( "SELECT sb FROM org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl sb WHERE sb.siteNodeVersion = $1 ORDER BY sb.serviceBindingId");
		oql.bind(siteNodeVersionId);

		
    	QueryResults results = null;
		if(readOnly)
			results = oql.execute(Database.ReadOnly);
		else
			results = oql.execute();
			
		logger.info("Fetching entity in read/write mode");

		while (results.hasMore()) 
        {
			serviceBindings.add((ServiceBinding)results.next());
        }
            
		results.close();
		oql.close();

    	//SiteNodeVersion siteNodeVersion = getSiteNodeVersionWithIdAsReadOnly(siteNodeVersionId, db);
    	//Collection serviceBindings = siteNodeVersion.getServiceBindings();
        
    	return serviceBindings;
	}

	
	public static SiteNodeVersion getLatestPublishedSiteNodeVersion(Integer siteNodeId) throws SystemException, Bug, Exception
    {
        SiteNodeVersion siteNodeVersion = null;
        
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);
        try
        {        
	        siteNodeVersion = getLatestPublishedSiteNodeVersion(siteNodeId, db);
			
            commitTransaction(db);            
        }
        catch(Exception e)
        {
        	logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
            
		return siteNodeVersion;
    }

	
	public static SiteNodeVersion getLatestPublishedSiteNodeVersion(Integer siteNodeId, Database db) throws SystemException, Bug, Exception
    {
        SiteNodeVersion siteNodeVersion = null;
        
        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl cv WHERE cv.owningSiteNode.siteNodeId = $1  AND cv.stateId = $2 AND cv.isActive = $3 ORDER BY cv.siteNodeVersionId desc");
    	oql.bind(siteNodeId);
    	oql.bind(SiteNodeVersionVO.PUBLISHED_STATE);
    	oql.bind(true);
    	
    	QueryResults results = oql.execute();
		logger.info("Fetching entity in read/write mode");

		if (results.hasMore()) 
        {
        	siteNodeVersion = (SiteNodeVersion)results.next();
        }
            
		results.close();
		oql.close();

		return siteNodeVersion;
    }
	
	
	public static SiteNodeVersionVO getLatestPublishedSiteNodeVersionVO(Integer siteNodeId) throws SystemException, Bug, Exception
    {
        SiteNodeVersionVO siteNodeVersion = null;
        
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);
        try
        {        
	        siteNodeVersion = getLatestPublishedSiteNodeVersionVO(siteNodeId, db);
			
            commitTransaction(db);            
        }
        catch(Exception e)
        {
        	logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
            
		return siteNodeVersion;
    }

	public static SiteNodeVersionVO getLatestPublishedSiteNodeVersionVO(Integer siteNodeId, Database db) throws SystemException, Bug, Exception
    {
        SiteNodeVersion siteNodeVersion = null;
        
        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl cv WHERE cv.siteNodeId = $1 AND cv.stateId = $2 AND cv.isActive = $3 ORDER BY cv.siteNodeVersionId desc");
    	oql.bind(siteNodeId);
    	oql.bind(SiteNodeVersionVO.PUBLISHED_STATE);
    	oql.bind(true);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);

		if (results.hasMore()) 
        {
        	siteNodeVersion = (SiteNodeVersion)results.next();
        }
            
		results.close();
		oql.close();

		if(siteNodeVersion != null)
			return siteNodeVersion.getValueObject();
		else 
			return null;
    }


	/**
	 * This method returns the version previous to the one sent in.
	 */
	
	public static SiteNodeVersionVO getPreviousSiteNodeVersionVO(Integer siteNodeId, Integer siteNodeVersionId) throws SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

    	SiteNodeVersionVO siteNodeVersionVO = null;

        beginTransaction(db);

        try
        {           
            OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl cv WHERE cv.siteNodeId = $1 AND cv.siteNodeVersionId < $2 ORDER BY cv.siteNodeVersionId desc");
        	oql.bind(siteNodeId);
        	oql.bind(siteNodeVersionId);
        	
        	QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
            {
            	SiteNodeVersion siteNodeVersion = (SiteNodeVersion)results.next();
            	logger.info("found one:" + siteNodeVersion.getValueObject());
            	siteNodeVersionVO = siteNodeVersion.getValueObject();
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
    	
		return siteNodeVersionVO;
    }


	/**
	 * This method returns the version previous to the one sent in.
	 */
	
	public SiteNodeVersionVO getPreviousActiveSiteNodeVersionVO(Integer siteNodeId, Integer siteNodeVersionId, Database db) throws SystemException, Bug, Exception
    {
    	SiteNodeVersionVO siteNodeVersionVO = null;

    	OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl cv WHERE cv.siteNodeId = $1 AND cv.isActive = $2 AND cv.siteNodeVersionId < $3 ORDER BY cv.siteNodeVersionId desc");
    	oql.bind(siteNodeId);
    	oql.bind(new Boolean(true));
    	oql.bind(siteNodeVersionId);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		if (results.hasMore()) 
        {
        	SiteNodeVersion siteNodeVersion = (SiteNodeVersion)results.next();
        	logger.info("found one:" + siteNodeVersion.getValueObject());
        	siteNodeVersionVO = siteNodeVersion.getValueObject();
        }
    	
		results.close();
		oql.close();

		return siteNodeVersionVO;
    }

	/**
	 * This method returns the version previous to the one sent in.
	 */
	
	public SiteNodeVersion getPreviousActiveSiteNodeVersion(Integer siteNodeId, Integer siteNodeVersionId, Database db) throws SystemException, Bug, Exception
    {
    	SiteNodeVersion siteNodeVersion = null;

    	OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl cv WHERE cv.owningSiteNode.siteNodeId = $1 AND cv.isActive = $2 AND cv.siteNodeVersionId < $3 ORDER BY cv.siteNodeVersionId desc");
    	oql.bind(siteNodeId);
    	oql.bind(new Boolean(true));
    	oql.bind(siteNodeVersionId);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		if (results.hasMore()) 
        {
        	siteNodeVersion = (SiteNodeVersion)results.next();
        	logger.info("found one:" + siteNodeVersion.getValueObject());
        }
    	
		results.close();
		oql.close();

		return siteNodeVersion;
    }

	
	/**
	 * Recursive methods to get all siteNodeVersions of a given state
	 * under the specified parent siteNode including the given siteNode.
	 */ 
	
	public List getSiteNodeVersionVOWithParentRecursive(Integer siteNodeId, Integer stateId) throws ConstraintException, SystemException
	{
		return getSiteNodeVersionVOWithParentRecursive(siteNodeId, stateId, new ArrayList());
	}
	
	private List getSiteNodeVersionVOWithParentRecursive(Integer siteNodeId, Integer stateId, List resultList) throws ConstraintException, SystemException
	{
		SiteNodeVersionVO siteNodeVersionVO = getLatestSiteNodeVersionVO(siteNodeId);
		if(siteNodeVersionVO.getStateId().intValue() == stateId.intValue())
			resultList.add(siteNodeVersionVO);
		
		// Get the children of this sitenode and do the recursion
		List childSiteNodeList = SiteNodeController.getController().getSiteNodeChildren(siteNodeId);
		Iterator childSiteNodeListIterator = childSiteNodeList.iterator();
		while(childSiteNodeListIterator.hasNext())
		{
			SiteNodeVO siteNodeVO = (SiteNodeVO)childSiteNodeListIterator.next();
			getSiteNodeVersionVOWithParentRecursive(siteNodeVO.getId(), stateId, resultList);
		}
	
		return resultList;
	}

	/**
	 * Recursive methods to get all siteNodeVersions of a given state
	 * under the specified parent siteNode including the given siteNode.
	 */ 
	
	public List getPublishedSiteNodeVersionVOWithParentRecursive(Integer siteNodeId) throws ConstraintException, SystemException
	{
	    List publishedSiteNodeVersionVOList = new ArrayList();
	    
	    Database db = CastorDatabaseService.getDatabase();

	    beginTransaction(db);

        try
        {
            SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithId(siteNodeId, db);
            List publishedSiteNodeVersions = new ArrayList();
            getPublishedSiteNodeVersionWithParentRecursive(siteNode, publishedSiteNodeVersions, db);
            publishedSiteNodeVersionVOList = toVOList(publishedSiteNodeVersions);
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
        return publishedSiteNodeVersionVOList;
	}
	
	private List getPublishedSiteNodeVersionWithParentRecursive(SiteNode siteNode, List resultList, Database db) throws ConstraintException, SystemException, Exception
	{
	    SiteNodeVersion siteNodeVersion = getLatestPublishedSiteNodeVersion(siteNode.getId(), db);
		if(siteNodeVersion != null)
			resultList.add(siteNodeVersion);
		
		// Get the children of this sitenode and do the recursion
		Collection childSiteNodeList = siteNode.getChildSiteNodes();
		Iterator childSiteNodeListIterator = childSiteNodeList.iterator();
		while(childSiteNodeListIterator.hasNext())
		{
			SiteNode childSiteNode = (SiteNode)childSiteNodeListIterator.next();
			getPublishedSiteNodeVersionWithParentRecursive(childSiteNode, resultList, db);
		}
		
		return resultList;
	}
	
	/**
	 * Recursive methods to get all contentVersions of a given state under the specified parent content.
	 */ 
	
    public void getSiteNodeAndAffectedItemsRecursive(Integer siteNodeId, Integer stateId, Set siteNodeVersionVOList, Set contenteVersionVOList, boolean includeMetaInfo, InfoGluePrincipal principal) throws ConstraintException, SystemException
	{
    	getSiteNodeAndAffectedItemsRecursive(siteNodeId, stateId, siteNodeVersionVOList, contenteVersionVOList, includeMetaInfo, true, principal);
	}
	
	/**
	 * Recursive methods to get all contentVersions of a given state under the specified parent content.
	 */ 
	
    public void getSiteNodeAndAffectedItemsRecursive(Integer siteNodeId, Integer stateId, Set siteNodeVersionVOList, Set contenteVersionVOList, boolean includeMetaInfo, boolean recurseSiteNodes, InfoGluePrincipal principal) throws ConstraintException, SystemException
	{
        Database db = CastorDatabaseService.getDatabase();

	    beginTransaction(db);

        try
        {
            SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithId(siteNodeId, db);

            getSiteNodeAndAffectedItemsRecursive(siteNode, stateId, new ArrayList(), new ArrayList(), db, siteNodeVersionVOList, contenteVersionVOList, includeMetaInfo, recurseSiteNodes, principal);
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
	}

	private void getSiteNodeAndAffectedItemsRecursive(SiteNode siteNode, Integer stateId, List checkedSiteNodes, List checkedContents, Database db, Set siteNodeVersionVOList, Set contentVersionVOList, boolean includeMetaInfo, InfoGluePrincipal principal) throws ConstraintException, SystemException, Exception
	{
		getSiteNodeAndAffectedItemsRecursive(siteNode, stateId, checkedSiteNodes, checkedContents, db, siteNodeVersionVOList, contentVersionVOList, includeMetaInfo, true, principal);
	}
	
	private void getSiteNodeAndAffectedItemsRecursive(SiteNode siteNode, Integer stateId, List checkedSiteNodes, List checkedContents, Database db, Set siteNodeVersionVOList, Set contentVersionVOList, boolean includeMetaInfo, boolean recurseSiteNodes, InfoGluePrincipal principal) throws ConstraintException, SystemException, Exception
	{
	    checkedSiteNodes.add(siteNode.getId());
        
		// Get the versions of this siteNode.
		//SiteNodeVersion siteNodeVersion = getLatestActiveSiteNodeVersionIfInState(siteNode, stateId, db);
		SiteNodeVersion siteNodeVersion = getLatestActiveSiteNodeVersion(db, siteNode.getId());
		if(siteNodeVersion != null && siteNodeVersion.getStateId().intValue() == stateId.intValue())
		{			
		    siteNodeVersionVOList.add(siteNodeVersion.getValueObject());
		}
		
		if(siteNodeVersion != null)
		{			
			List relatedEntities = RegistryController.getController().getMatchingRegistryVOListForReferencingEntity(SiteNodeVersion.class.getName(), siteNodeVersion.getId().toString(), db);
	        Iterator relatedEntitiesIterator = relatedEntities.iterator();
	        
	        while(relatedEntitiesIterator.hasNext())
	        {
	            RegistryVO registryVO = (RegistryVO)relatedEntitiesIterator.next();
	            if(registryVO.getEntityName().equals(SiteNode.class.getName()) && !checkedSiteNodes.contains(new Integer(registryVO.getEntityId())))
	            {
	                try
	                {
	                    SiteNodeVO relatedSiteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(new Integer(registryVO.getEntityId()), db);
	
	                    //SiteNodeVersion relatedSiteNodeVersion = getLatestActiveSiteNodeVersionIfInState(relatedSiteNode, stateId, db);
		                SiteNodeVersion relatedSiteNodeVersion = getLatestActiveSiteNodeVersion(db, new Integer(registryVO.getEntityId()));
		                
		                Integer repositoryId = relatedSiteNodeVO.getRepositoryId();
		                Integer siteNodeRepositoryId = siteNode.getRepository().getId();
		                boolean allowedSiteNodeVersion = repositoryId.intValue() == siteNodeRepositoryId.intValue();
		                if(CmsPropertyHandler.getAllowCrossSiteSubmitToPublish().equalsIgnoreCase("true"))
		                {
		                	if(AccessRightController.getController().getIsPrincipalAuthorized(db, principal, "Repository.Read", "" + repositoryId))
		                	{
		            			Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(relatedSiteNodeVersion.getId(), db);
		            			if(protectedSiteNodeVersionId == null || AccessRightController.getController().getIsPrincipalAuthorized(principal, "SiteNodeVersion.SubmitToPublish", protectedSiteNodeVersionId.toString()))
			        				allowedSiteNodeVersion = true;
		                	}
		                }
	
		                //if(relatedSiteNodeVersion != null && relatedSiteNodeVersion.getStateId().intValue() == stateId.intValue() && siteNode.getRepository().getId().intValue() == relatedSiteNodeVersion.getOwningSiteNode().getRepository().getId().intValue())
		                if(relatedSiteNodeVersion != null && allowedSiteNodeVersion && relatedSiteNodeVersion.getStateId().intValue() == stateId.intValue())
		    	        {
		                    siteNodeVersionVOList.add(relatedSiteNodeVersion.getValueObject());
		                }
	                }
	                catch(Exception e)
	                {
	                    logger.warn("A siteNode referenced by ID:" + registryVO.getEntityId() + " was not found - must be a invalid reference from " + siteNode.getName() + "[" + siteNode.getId() + "].", e);
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
			                Integer repositoryId = relatedContent.getRepository().getId();
			                Integer siteNodeRepositoryId = siteNode.getRepository().getId();
			                boolean allowedContent = repositoryId.intValue() == siteNodeRepositoryId.intValue();
			                if(CmsPropertyHandler.getAllowCrossSiteSubmitToPublish().equalsIgnoreCase("true"))
			                {
			                	if(AccessRightController.getController().getIsPrincipalAuthorized(db, principal, "Repository.Read", "" + repositoryId))
			                	{
				                	Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(relatedContent.getId(), db);
				        			if(protectedContentId == null || AccessRightController.getController().getIsPrincipalAuthorized(db, principal, "Content.SubmitToPublish", protectedContentId.toString()))
				        				allowedContent = true;
			                	}
			                }
			                
		                	List relatedContentVersions = ContentVersionController.getContentVersionController().getLatestActiveContentVersionIfInState(relatedContent, stateId, db);
	
			                Iterator relatedContentVersionsIterator = relatedContentVersions.iterator();
			                while(relatedContentVersionsIterator.hasNext())
			                {
			                    ContentVersion relatedContentVersion = (ContentVersion)relatedContentVersionsIterator.next();
				                //if(relatedContentVersion != null && siteNode.getRepository().getId().intValue() == relatedContentVersion.getOwningContent().getRepository().getId().intValue())
					            if(relatedContentVersion != null && allowedContent)
				                {
				                    contentVersionVOList.add(relatedContentVersion.getValueObject());
				                    logger.info("relatedContentVersion:" + relatedContentVersion.getOwningContent().getName());
				                    ContentVersionController.getContentVersionController().getContentAndAffectedItemsRecursive((Content)relatedContentVersion.getOwningContent(), ContentVersionVO.WORKING_STATE, checkedSiteNodes, checkedContents, db, siteNodeVersionVOList, contentVersionVOList, true, false, 3, 0);
				                }
			                }
		                }
	                }
	                catch(Exception e)
	                {
	                    logger.warn("A content referenced by ID:" + registryVO.getEntityId() + " was not found - must be a invalid reference from " + siteNode.getName() + "[" + siteNode.getId() + "].", e);
	                }
	                
	    		    checkedContents.add(new Integer(registryVO.getEntityId()));
	            }
	        //}	    
			}
		}
		
        if(recurseSiteNodes)
        {
			// Get the children of this siteNode and do the recursion
			Collection childSiteNodeList = siteNode.getChildSiteNodes();
			Iterator cit = childSiteNodeList.iterator();
			while (cit.hasNext())
			{
				SiteNode childSiteNode = (SiteNode) cit.next();
				getSiteNodeAndAffectedItemsRecursive(childSiteNode, stateId, checkedSiteNodes, checkedContents, db, siteNodeVersionVOList, contentVersionVOList, includeMetaInfo, principal);
			}
        }   
	}

	/**
	 * This method returns the latest sitenodeVersion there is for the given siteNode.
	 */
	
	public SiteNodeVersion getLatestActiveSiteNodeVersionIfInState(SiteNode siteNode, Integer stateId, Database db) throws SystemException, Exception
	{
		SiteNodeVersion siteNodeVersion = null;
		
		Collection siteNodeVersions = siteNode.getSiteNodeVersions();

		SiteNodeVersion latestSiteNodeVersion = null;
		
		Iterator versionIterator = siteNodeVersions.iterator();
		while(versionIterator.hasNext())
		{
		    SiteNodeVersion siteNodeVersionCandidate = (SiteNodeVersion)versionIterator.next();	
			
			if(latestSiteNodeVersion == null || (latestSiteNodeVersion.getId().intValue() < siteNodeVersionCandidate.getId().intValue() && siteNodeVersionCandidate.getIsActive().booleanValue()))
			    latestSiteNodeVersion = siteNodeVersionCandidate;
			
			if(siteNodeVersionCandidate.getIsActive().booleanValue() && siteNodeVersionCandidate.getStateId().intValue() == stateId.intValue())
			{
				if(siteNodeVersionCandidate.getOwningSiteNode().getSiteNodeId().intValue() == siteNode.getId().intValue())
				{
					if(siteNodeVersion == null || siteNodeVersion.getSiteNodeVersionId().intValue() < siteNodeVersionCandidate.getId().intValue())
					{
						siteNodeVersion = siteNodeVersionCandidate;
					}
				}
			}
		}

		if(siteNodeVersion != latestSiteNodeVersion)
		    siteNodeVersion = null;
		    
		return siteNodeVersion;
	}

	
	   /**
     * This method gets the meta info for the siteNodeVersion.
     * @param db
     * @throws ConstraintException
     * @throws SystemException
     * @throws Exception
     */
    public List getMetaInfoContentVersionVOList(Integer siteNodeVersionId, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException, Exception
    {
        List contentVersionVOList = new ArrayList();
        
        Database db = CastorDatabaseService.getDatabase();

	    beginTransaction(db);

        try
        {
        	SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId, db);
        	SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVersionVO.getSiteNodeId());
        	
        	//SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(siteNodeVersionId, db);
            List contentVersions = getMetaInfoContentVersions(db, siteNodeVO, infoGluePrincipal);
            contentVersionVOList = toVOList(contentVersions);
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
		return contentVersionVOList;
    }

    /**
     * This method gets the meta info for the siteNodeVersion.
     * @param db
     * @throws ConstraintException
     * @throws SystemException
     * @throws Exception
     */
    private List getMetaInfoContentVersions(Database db, SiteNodeVO siteNodeVO, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException, Exception
    {
        List contentVersions = new ArrayList();
        
        List languages = LanguageController.getController().getLanguageList(siteNodeVO.getRepositoryId(), db);
		Language masterLanguage = LanguageController.getController().getMasterLanguage(db, siteNodeVO.getRepositoryId());

		if(siteNodeVO.getMetaInfoContentId() != null)
		{
			ContentVO contentVO = ContentController.getContentController().getContentVOWithId(siteNodeVO.getMetaInfoContentId(), db);
			
			Iterator languageIterator = languages.iterator();
			while(languageIterator.hasNext())
			{
				Language language = (Language)languageIterator.next();
				ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentVO.getId(), language.getId(), db);
				
				if(contentVersion != null)
				    contentVersions.add(contentVersion);
			}
		}
		
		return contentVersions;
    }

	
	/**
	 * Updates the SiteNodeVersion.
	 */
	
	public SiteNodeVersionVO update(SiteNodeVersionVO siteNodeVersionVO) throws ConstraintException, SystemException
	{
    	registryController.updateSiteNodeVersion(siteNodeVersionVO);

		return (SiteNodeVersionVO) updateEntity(SiteNodeVersionImpl.class, (BaseEntityVO)siteNodeVersionVO);
	}  
	
	/**
	 * Updates the SiteNodeVersion within a transaction.
	 */
	
	public SiteNodeVersionVO update(SiteNodeVersionVO siteNodeVersionVO, Database db) throws ConstraintException, SystemException, Exception
	{
	    SiteNodeVersion siteNodeVersion = getSiteNodeVersionWithId(siteNodeVersionVO.getId(), db);
    	registryController.updateSiteNodeVersion(siteNodeVersion, db);

    	siteNodeVersion.setValueObject(siteNodeVersionVO);
    	return siteNodeVersionVO;
		//return (SiteNodeVersionVO) updateEntity(SiteNodeVersionImpl.class, (BaseEntityVO)siteNodeVersionVO, db);
	}    
	
	
	/**
     * This method gets the meta info for the siteNodeVersion.
     * @param db
     * @throws ConstraintException
     * @throws SystemException
     * @throws Exception
     */
	public List getPublishedActiveSiteNodeVersionVOList(Integer siteNodeId) throws SystemException, Bug, Exception
	{
        List siteNodeVersionVOList = new ArrayList();
        
        Database db = CastorDatabaseService.getDatabase();

	    beginTransaction(db);

        try
        {
            List siteNodeVersionList = getPublishedActiveSiteNodeVersionVOList(siteNodeId, db);
            siteNodeVersionVOList = toVOList(siteNodeVersionList);
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
		return siteNodeVersionVOList;
    }

	/**
     * This method gets the meta info for the siteNodeVersion.
     * @param db
     * @throws ConstraintException
     * @throws SystemException
     * @throws Exception
     */
	public List getPublishedActiveFullSiteNodeVersionVOList(Integer siteNodeId) throws SystemException, Bug, Exception
	{
        List siteNodeVersionVOList = new ArrayList();
        
        Database db = CastorDatabaseService.getDatabase();

	    beginTransaction(db);

        try
        {
            List siteNodeVersionList = getPublishedActiveFullSiteNodeVersionVOList(siteNodeId, db);
            siteNodeVersionVOList = toVOList(siteNodeVersionList);
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
		return siteNodeVersionVOList;
    }	
	
	public List getPublishedActiveSiteNodeVersionVOList(Integer siteNodeId, Database db) throws SystemException, Bug, Exception
    {
        List siteNodeVersionList = new ArrayList();
        
        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl cv WHERE cv.siteNodeId = $1  AND cv.stateId = $2 AND cv.isActive = $3 ORDER BY cv.siteNodeVersionId desc");
    	oql.bind(siteNodeId);
    	oql.bind(SiteNodeVersionVO.PUBLISHED_STATE);
    	oql.bind(true);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);

		while (results.hasMore()) 
        {
        	SiteNodeVersion siteNodeVersion = (SiteNodeVersion)results.next();
        	siteNodeVersionList.add(siteNodeVersion);
        }
            
		results.close();
		oql.close();

		return siteNodeVersionList;
    }

	public List getPublishedActiveFullSiteNodeVersionVOList(Integer siteNodeId, Database db) throws SystemException, Bug, Exception
    {
        List siteNodeVersionList = new ArrayList();
        
        OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl cv WHERE cv.owningSiteNode.siteNodeId = $1  AND cv.stateId = $2 AND cv.isActive = $3 ORDER BY cv.siteNodeVersionId desc");
    	oql.bind(siteNodeId);
    	oql.bind(SiteNodeVersionVO.PUBLISHED_STATE);
    	oql.bind(true);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);

		while (results.hasMore()) 
        {
        	SiteNodeVersion siteNodeVersion = (SiteNodeVersion)results.next();
        	siteNodeVersionList.add(siteNodeVersion);
        }
            
		results.close();
		oql.close();

		return siteNodeVersionList;
    }

	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new SiteNodeVersionVO();
	}


}
 
