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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.SmallestContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.AvailableServiceBinding;
import org.infoglue.cms.entities.management.AvailableServiceBindingVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.GeneralOQLResult;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RegistryVO;
import org.infoglue.cms.entities.structure.ServiceBinding;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.impl.simple.MediumSiteNodeVersionImpl;
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
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.Timer;


public class SiteNodeVersionController extends BaseController 
{
    private final static Logger logger = Logger.getLogger(SiteNodeVersionController.class.getName());

    private final static VisualFormatter vf = new VisualFormatter();

	private final RegistryController registryController = RegistryController.getController();

	private static Map<Integer, Integer> siteNodeIdMap = new ConcurrentHashMap<Integer, Integer>();

	/**
	 * Factory method
	 */

	public static SiteNodeVersionController getController()
	{
		return new SiteNodeVersionController();
	}
	
		/**
	 * Gets the Id of the SiteNode the given <em>siteNodeVersionId</em> belongs to. The method utilizes a
	 * local cache. This means that on subsequent for the same input no look-up of SiteNodeVersionVo will be done.
	 * @param siteNodeVersionId
	 * @return The Id of the SiteNode owning the given SiteNodeVersion.
	 * @throws SystemException
	 * @throws Bug
	 */
	public Integer getSiteNodeIdForSiteNodeVersion(Integer siteNodeVersionId) throws SystemException, Bug
    {
    	Integer siteNodeId = null;
    	if (siteNodeIdMap != null)
    	{
    		siteNodeId = (Integer)siteNodeIdMap.get(siteNodeVersionId);
    	}
    	if(siteNodeId == null)
    	{
    		SiteNodeVersionVO siteNodeVersionVO = getSiteNodeVersionVOWithId(siteNodeVersionId);
    		siteNodeId = siteNodeVersionVO.getSiteNodeId();
    		siteNodeIdMap.put(siteNodeVersionId, siteNodeId);
    	}

    	return siteNodeId;
    }

   	/**
	 * This method returns selected active content versions.
	 */
    
	public List<SiteNodeVersion> getSiteNodeVersionList(Integer repositoryId, Integer minimumId, Integer limit, Boolean onlyPublishedVersions, Database db) throws SystemException, Bug, Exception
	{
		List<SiteNodeVersion> siteNodeVersionList = new ArrayList<SiteNodeVersion>();

        OQLQuery oql = db.getOQLQuery( "SELECT snv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl snv WHERE snv.owningSiteNode.repository = $1 AND snv.isActive = $2 AND snv.siteNodeVersionId > $3 " + (onlyPublishedVersions ? " AND snv.stateId = 3 " : "") + " ORDER BY snv.siteNodeVersionId LIMIT $4");
    	oql.bind(repositoryId);
		oql.bind(true);
		oql.bind(minimumId);
		oql.bind(limit);
    	
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
	 * This method returns the latest active content version.
	 */
    
	public List<SiteNodeVO> getLatestSiteNodeVersionIds(Set<Integer> siteNodeIds/*, Integer stateId*/, Database db) throws SystemException, Bug, Exception
	{
		List<SiteNodeVO> siteNodeVersionIdSet = new ArrayList<SiteNodeVO>();
		if(siteNodeIds == null || siteNodeIds.size() == 0)
			return siteNodeVersionIdSet;
		
		List<Integer> siteNodesHandled = new ArrayList<Integer>();
		
		StringBuilder variables = new StringBuilder();
	    for(int i=0; i<siteNodeIds.size(); i++)
	    	variables.append("?" + (i+2) + (i+1!=siteNodeIds.size() ? "," : ""));
	    	//variables.append("$" + (i+2) + (i+1!=siteNodeIds.size() ? "," : ""));
	    //System.out.println("variables:" + variables);

		StringBuilder sb = new StringBuilder();
		if(CmsPropertyHandler.getUseShortTableNames().equals("true"))
		{
			sb.append("select max(snv.siNoVerId) AS id, snv.siNoId as column1Value, max(snv.stateId) as column2Value, sn.repositoryId as column3Value, max(snv.versionModifier) as column4Value, max(snv.modifiedDateTime) as column5Value, '' as column6Value, '' as column7Value from cmSiNoVer snv, cmSiNo sn where sn.siNoId = snv.siNoId AND snv.isActive = ?1 AND snv.siNoId IN (" + variables + ") group by snv.siNoId, sn.repositoryId ");
		}
		else
		{
			sb.append("select max(snv.siteNodeVersionId) AS id, snv.siteNodeId as column1Value, max(snv.stateId) as column2Value, sn.repositoryId as column3Value, max(snv.versionModifier) as column4Value, max(snv.modifiedDateTime) as column5Value, '' as column6Value, '' as column7Value from cmSiteNodeVersion snv, cmSiteNode sn where sn.siteNodeId = snv.siteNodeId AND snv.isActive = ?1 AND snv.siteNodeId IN (" + variables + ") group by snv.siteNodeId, sn.repositoryId ");
		}

		Connection conn = (Connection) db.getJdbcConnection();
		
		PreparedStatement psmt = conn.prepareStatement(sb.toString());
		
		psmt.setInt(1, 1);
		
		int i=2;
    	for(Integer entityId : siteNodeIds)
    	{
    		psmt.setInt(i, entityId);
    		i++;
    	}

		ResultSet rs = psmt.executeQuery();
		while(rs.next())
		{
			Integer siteNodeId = new Integer(rs.getString(2));
			Integer versionStateId = new Integer(rs.getString(3));
			Integer repositoryId = new Integer(rs.getString(4));
			String versionModifier = rs.getString(5);
			String modifiedDateTime = rs.getString(6);
			//System.out.println(siteNodeId + ":" + versionStateId);
			if(rs.getString(1) != null && rs.getString(2) != null/* && versionStateId.equals(stateId)*/ && !siteNodesHandled.contains(siteNodeId))
			{
				SiteNodeVO siteNodeVO = new SiteNodeVO();
				siteNodeVO.setSiteNodeVersionId(new Integer(rs.getString(1)));
				siteNodeVO.setSiteNodeId(siteNodeId);
				siteNodeVO.setStateId(versionStateId);
				siteNodeVO.setRepositoryId(repositoryId);
				siteNodeVO.setVersionModifier(versionModifier);
				siteNodeVO.setModifiedDateTime(vf.parseDate(modifiedDateTime, "yyyy-MM-dd HH:mm:ss"));
				
				siteNodeVersionIdSet.add(siteNodeVO);
				siteNodesHandled.add(siteNodeId);
			}
		}
		rs.close();
		psmt.close();

		/*
		//System.out.println("CALL SQL " + sb.toString() + "AS org.infoglue.cms.entities.management.GeneralOQLResult");
		OQLQuery oql = db.getOQLQuery("CALL SQL " + sb.toString() + "AS org.infoglue.cms.entities.management.GeneralOQLResult");

    	oql.bind(true);
    	for(Integer entityId : siteNodeIds)
    	{
    		//System.out.println("entityId:" + entityId);
    		oql.bind(entityId.toString());
    	}
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		while (results.hasMore()) 
        {
			GeneralOQLResult resultBean = (GeneralOQLResult)results.next();
			Integer siteNodeId = new Integer(resultBean.getValue1());
			Integer versionStateId = new Integer(resultBean.getValue2());
			Integer repositoryId = new Integer(resultBean.getValue3());
			String versionModifier = resultBean.getValue4();
			String modifiedDateTime = resultBean.getValue5();
			//System.out.println(siteNodeId + ":" + versionStateId);
			if(resultBean.getId() != null && resultBean.getValue1() != null && !siteNodesHandled.contains(siteNodeId))
			{
				SiteNodeVO siteNodeVO = new SiteNodeVO();
				siteNodeVO.setSiteNodeVersionId(resultBean.getId());
				siteNodeVO.setSiteNodeId(siteNodeId);
				siteNodeVO.setStateId(versionStateId);
				siteNodeVO.setRepositoryId(repositoryId);
				siteNodeVO.setVersionModifier(versionModifier);
				siteNodeVO.setModifiedDateTime(vf.parseDate(modifiedDateTime, "yyyy-MM-dd HH:mm:ss"));
				
				siteNodeVersionIdSet.add(siteNodeVO);
				siteNodesHandled.add(siteNodeId);
			}
		}
		
		results.close();
		oql.close();
		*/

		return siteNodeVersionIdSet;
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

	public MediumSiteNodeVersionImpl getMediumSiteNodeVersionWithId(Integer siteNodeVersionId, Database db) throws SystemException, Bug
    {
		return (MediumSiteNodeVersionImpl) getObjectWithId(MediumSiteNodeVersionImpl.class, siteNodeVersionId, db);
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
     * This method removes the siteNodeVersion and also all associated bindings.
     * @param siteNodeVersion
     * @param db
     * @throws ConstraintException
     * @throws SystemException
     */
    public void delete(Integer siteNodeVersionId, Database db) throws ConstraintException, SystemException
    {
		try
		{
			MediumSiteNodeVersionImpl siteNodeVersion = getMediumSiteNodeVersionWithId(siteNodeVersionId, db);
			Collection serviceBindings = siteNodeVersion.getServiceBindings();
		    Iterator serviceBindingsIterator = serviceBindings.iterator();
		    while(serviceBindingsIterator.hasNext())
		    {
		        ServiceBinding serviceBinding = (ServiceBinding)serviceBindingsIterator.next();
		        serviceBindingsIterator.remove();
		        db.remove(serviceBinding);
		    }
		    
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
			int maxSortOrder;
			if (siteNode.getParentSiteNode() != null)
			{
				maxSortOrder = siteNode.getParentSiteNode().getChildSiteNodes().size()-1;
			}
			else
			{
				maxSortOrder = 0;
			}
			//SiteNode siteNode = SiteNodeController.getSiteNodeWithId(siteNodeId, db);
            
			siteNodeVersion = new SiteNodeVersionImpl();
			siteNodeVersion.setIsCheckedOut(new Boolean(false));
			siteNodeVersion.setModifiedDateTime(DateHelper.getSecondPreciseDate());
			siteNodeVersion.setOwningSiteNode((SiteNodeImpl)siteNode);
			siteNodeVersion.setStateId(new Integer(0));
			siteNodeVersion.setVersionComment("Initial version");
			siteNodeVersion.setVersionModifier(infoGluePrincipal.getName());
			siteNodeVersion.setVersionNumber(new Integer(1));
			siteNodeVersion.setSortOrder(maxSortOrder);
        	
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
	
	public static MediumSiteNodeVersionImpl createSmall(Integer siteNodeId, InfoGluePrincipal infoGluePrincipal, SiteNodeVersionVO siteNodeVersionVO, Database db) throws SystemException, Bug, Exception
	{
		MediumSiteNodeVersionImpl siteNodeVersion = new MediumSiteNodeVersionImpl();
		
       	siteNodeVersionVO.setVersionModifier(infoGluePrincipal.getName());
       	siteNodeVersion.setValueObject(siteNodeVersionVO);
       	siteNodeVersion.setSiteNodeId(siteNodeId);
       	siteNodeVersion.setVersionNumber(new Integer(1));
       	db.create(siteNodeVersion);

       	return siteNodeVersion;		
	}
	
	/**
	 * This method creates a new siteNodeVersion for the siteNode sent in.
	 */
	
	public static SiteNodeVersion createFull(Integer siteNodeId, InfoGluePrincipal infoGluePrincipal, SiteNodeVersionVO siteNodeVersionVO, Database db) throws SystemException, Bug, Exception
	{
    	SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithId(siteNodeId, db);
        
    	SiteNodeVersionImpl siteNodeVersion = new SiteNodeVersionImpl();    	
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

	public SiteNodeVersion getLatestActiveSiteNodeVersionReadOnly(Database db, Integer siteNodeId) throws SystemException, Bug, Exception
    {
	    SiteNodeVersion siteNodeVersion = null;
	    
	    OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl cv WHERE cv.owningSiteNode.siteNodeId = $1 AND cv.isActive = $2 ORDER BY cv.siteNodeVersionId desc");
		oql.bind(siteNodeId);
		oql.bind(new Boolean(true));
		
		QueryResults results = oql.execute(Database.ReadOnly);
		
		if (results.hasMore()) 
	    {
	    	siteNodeVersion = (SiteNodeVersion)results.next();
        }

		results.close();
		oql.close();

		return siteNodeVersion;
    }

	
	public SiteNodeVersionVO getLatestSiteNodeVersionVO(Integer siteNodeId) throws SystemException, Bug
    {
		String key = "" + siteNodeId;
		SiteNodeVersionVO siteNodeVersionVO = (SiteNodeVersionVO)CacheController.getCachedObjectFromAdvancedCache("latestSiteNodeVersionCache", key);
		if(siteNodeVersionVO != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached siteNodeVersionVO:" + siteNodeVersionVO);
		}
		else
		{
		    SiteNodeVersion siteNodeVersion = null;
		    
		    Database db = CastorDatabaseService.getDatabase();
	        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

	        beginTransaction(db);

	        try
	        {
			    OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl cv WHERE cv.siteNodeId = $1 ORDER BY cv.siteNodeVersionId desc");
				oql.bind(siteNodeId);
				
				QueryResults results = oql.execute(Database.ReadOnly);
				
				if (results.hasMore()) 
			    {
			    	siteNodeVersion = (SiteNodeVersion)results.next();
		        }
		
				results.close();
				oql.close();
		
			    if(siteNodeVersion != null)
			    	siteNodeVersionVO = siteNodeVersion.getValueObject();
			    else
			    	logger.warn("The siteNode " + siteNodeId + " did not have a latest active siteNodeVersion - very strange.");
				
				if(siteNodeVersionVO != null)
				{
		        	String groupKey1 = CacheController.getPooledString(4, siteNodeVersionVO.getId());
		        	String groupKey2 = CacheController.getPooledString(3, siteNodeId);
		        	CacheController.cacheObjectInAdvancedCache("latestSiteNodeVersionCache", key, siteNodeVersionVO, new String[]{groupKey1, groupKey2}, true);
				}
	        	
				commitTransaction(db);
	        }
	        catch(Exception e)
	        {
	            logger.error("An error occurred so we should not completes the transaction:" + e, e);
	            rollbackTransaction(db);
	            throw new SystemException(e.getMessage());
	        }
		}
	    
		return siteNodeVersionVO;
    }

	public SiteNodeVersionVO getLatestSiteNodeVersionVO(Database db, Integer siteNodeId) throws SystemException, Bug, Exception
    {
		String key = "" + siteNodeId;
		SiteNodeVersionVO siteNodeVersionVO = (SiteNodeVersionVO)CacheController.getCachedObjectFromAdvancedCache("latestSiteNodeVersionCache", key);
		if(siteNodeVersionVO != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached siteNodeVersionVO:" + siteNodeVersionVO);
		}
		else
		{
		    SiteNodeVersion siteNodeVersion = null;
		    
		    OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl cv WHERE cv.siteNodeId = $1 ORDER BY cv.siteNodeVersionId desc");
			oql.bind(siteNodeId);
			
			QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
		    {
		    	siteNodeVersion = (SiteNodeVersion)results.next();
	        }
	
			results.close();
			oql.close();
	
		    if(siteNodeVersion != null)
		    	siteNodeVersionVO = siteNodeVersion.getValueObject();
		    else
		    	logger.warn("The siteNode " + siteNodeId + " did not have a latest active siteNodeVersion - very strange.");
			
			if(siteNodeVersionVO != null)
			{
	        	String groupKey1 = CacheController.getPooledString(4, siteNodeVersionVO.getId());
	        	String groupKey2 = CacheController.getPooledString(3, siteNodeId);
	        	CacheController.cacheObjectInAdvancedCache("latestSiteNodeVersionCache", key, siteNodeVersionVO, new String[]{groupKey1, groupKey2}, true);
			}
			
		}
	    
		return siteNodeVersionVO;
    }


	public SiteNodeVersionVO getLatestActiveSiteNodeVersionVO(Database db, Integer siteNodeId) throws SystemException, Bug, Exception
    {
		String key = "" + siteNodeId + "_active";
		SiteNodeVersionVO siteNodeVersionVO = (SiteNodeVersionVO)CacheController.getCachedObjectFromAdvancedCache("latestSiteNodeVersionCache", key);
		if(siteNodeVersionVO != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached siteNodeVersionVO:" + siteNodeVersionVO);
		}
		else
		{
			SmallSiteNodeVersionImpl siteNodeVersion = null;
		    
		    OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl cv WHERE cv.siteNodeId = $1 AND cv.isActive = $2 ORDER BY cv.siteNodeVersionId desc");
			oql.bind(siteNodeId);
			oql.bind(new Boolean(true));
			
			QueryResults results = oql.execute(Database.ReadOnly);
			
			if (results.hasMore()) 
		    {
		    	siteNodeVersion = (SmallSiteNodeVersionImpl)results.next();
	        }
	
			results.close();
			oql.close();

		    if(siteNodeVersion != null)
		    	siteNodeVersionVO = siteNodeVersion.getValueObject();
		    else
		    	logger.warn("The siteNode " + siteNodeId + " did not have a latest active siteNodeVersion - very strange.");

			if(siteNodeVersionVO != null)
			{
	        	String groupKey1 = CacheController.getPooledString(4, siteNodeVersionVO.getId());
	        	String groupKey2 = CacheController.getPooledString(3, siteNodeId);
	        	CacheController.cacheObjectInAdvancedCache("latestSiteNodeVersionCache", key, siteNodeVersionVO, new String[]{groupKey1, groupKey2}, true);
			}
			
		}
	    
		return siteNodeVersionVO;
    }

    
	/**
	 * This is a method used to get the latest site node version of a sitenode within a given transaction.
	 */
	
	public SiteNodeVersion getLatestSiteNodeVersion(Database db, Integer siteNodeId, boolean ReadOnly) throws SystemException, Bug
	{
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		SiteNodeVersion siteNodeVersion = null;

		try
		{
			OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl cv WHERE cv.owningSiteNode.siteNodeId = $1 ORDER BY cv.siteNodeVersionId desc");
			oql.bind(siteNodeId);
        	
			QueryResults results = null;
			if(ReadOnly)
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


	/**
	 * This is a method used to get the latest site node version of a sitenode within a given transaction.
	 */

	public SiteNodeVersion getLatestMediumSiteNodeVersion(Database db, Integer siteNodeId, boolean ReadOnly) throws SystemException, Bug
	{
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		SiteNodeVersion siteNodeVersion = null;

		try
		{
			OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.MediumSiteNodeVersionImpl cv WHERE cv.siteNodeId = $1 ORDER BY cv.siteNodeVersionId desc");
			oql.bind(siteNodeId);
        	
			QueryResults results = null;
			if(ReadOnly)
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
			returnVO = createSmall(siteNodeId, infoGluePrincipal, siteNodeVersionVO, db).getValueObject();
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
			
			siteNodeVersion = createFull(siteNodeVersion.getValueObject().getSiteNodeId(), infoGluePrincipal, siteNodeVersionVO, db);
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
				    		if(siteNode.getMetaInfoContentId() != null)
				    		{
				    			ContentVO contentVO = ContentController.getContentController().getContentVOWithId(siteNode.getMetaInfoContentId(), db);
				    			ContentController.getContentController().delete(contentVO, db, true, true, true, infoGluePrincipal);
    				    	}
				    		//deleteMetaInfoForSiteNodeVersion(db, serviceBinding, infoGluePrincipal);
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
	
	public static Collection getServiceBindningList(Integer siteNodeVersionId, Database db, boolean ReadOnly) throws ConstraintException, SystemException, Exception
	{
    	Collection serviceBindings = new ArrayList();
    		
		OQLQuery oql = db.getOQLQuery( "SELECT sb FROM org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl sb WHERE sb.siteNodeVersion = $1 ORDER BY sb.serviceBindingId");
		oql.bind(siteNodeVersionId);

		
    	QueryResults results = null;
		if(ReadOnly)
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

	public SiteNodeVersionVO getPreviousActiveSiteNodeVersionVO(Integer siteNodeId, Integer siteNodeVersionId) throws SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();

    	SiteNodeVersionVO siteNodeVersionVO = null;

        beginTransaction(db);

        try
        {           
        	siteNodeVersionVO = getPreviousActiveSiteNodeVersionVO(siteNodeId, siteNodeVersionId, db);

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

	
	public SiteNodeVersionVO getPreviousActiveSiteNodeVersionVO(Integer siteNodeId, Integer siteNodeVersionId, Integer stateId) throws SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();

    	SiteNodeVersionVO siteNodeVersionVO = null;

        beginTransaction(db);

        try
        {           
        	siteNodeVersionVO = getPreviousActiveSiteNodeVersionVO(siteNodeId, siteNodeVersionId, stateId, db);

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
	
	public SiteNodeVersionVO getPreviousActiveSiteNodeVersionVO(Integer siteNodeId, Integer siteNodeVersionId, Integer stateId, Database db) throws SystemException, Bug, Exception
    {
    	SiteNodeVersionVO siteNodeVersionVO = null;

    	OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl cv WHERE cv.siteNodeId = $1 AND cv.isActive = $2 AND cv.stateId >= $3 AND cv.siteNodeVersionId < $4 ORDER BY cv.siteNodeVersionId desc");
    	oql.bind(siteNodeId);
    	oql.bind(new Boolean(true));
    	oql.bind(stateId);
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
		List childSiteNodeList = SiteNodeController.getController().getSiteNodeChildrenVOList(siteNodeId);
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
	
    public void getSiteNodeAndAffectedItemsRecursive(Integer siteNodeId, Integer stateId, Set<SiteNodeVersionVO> siteNodeVersionVOList, Set<ContentVersionVO> contentVersionVOList, boolean includeMetaInfo, InfoGluePrincipal principal, ProcessBean processBean, Locale locale) throws ConstraintException, SystemException
	{
    	getSiteNodeAndAffectedItemsRecursive(siteNodeId, stateId, siteNodeVersionVOList, contentVersionVOList, includeMetaInfo, true, principal, processBean, locale);
	}
	
	/**
	 * Recursive methods to get all contentVersions of a given state under the specified parent content.
	 */ 
	
    public void getSiteNodeAndAffectedItemsRecursive(Integer siteNodeId, Integer stateId, Set<SiteNodeVersionVO> siteNodeVersionVOList, Set<ContentVersionVO> contentVersionVOList, boolean includeMetaInfo, boolean recurseSiteNodes, InfoGluePrincipal principal, ProcessBean processBean, Locale locale) throws ConstraintException, SystemException
	{
    	Timer t = new Timer();
    	
        Database db = CastorDatabaseService.getDatabase();

	    beginTransaction(db);

        try
        {
            processBean.updateProcess(getLocalizedString(locale, "tool.structuretool.publicationProcess.traversingChildNodes"));
            SiteNodeVO siteNode = SiteNodeController.getController().getSiteNodeVOWithIdNoStateCheck(siteNodeId, db);
            List<SiteNodeVersionVO> localSiteNodeVersionVOList = getSiteNodeVersionsRecursive(siteNode, stateId, db, includeMetaInfo, recurseSiteNodes, principal);
            processBean.updateProcess(getLocalizedString(locale, "tool.structuretool.publicationProcess.foundPages", localSiteNodeVersionVOList.size()));

            Set<Integer> localSiteNodeVersionIdSet = new HashSet<Integer>();
            for(SiteNodeVersionVO snvVO : localSiteNodeVersionVOList)
    		{
            	localSiteNodeVersionIdSet.add(snvVO.getSiteNodeVersionId());
    			if(snvVO.getStateId() == 0)
    				siteNodeVersionVOList.add(snvVO);
    		}
            
        	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getSiteNodeAndAffectedItemsRecursive", t.getElapsedTime());

        	//Takes the base list of pages in the structure clicked and fetches the relations. Store them in .
        	getReferencedItemsRecursive(stateId, siteNodeVersionVOList, contentVersionVOList, includeMetaInfo, principal, db, siteNode, localSiteNodeVersionIdSet, new HashSet<Integer>(), 2, 0, processBean, locale);
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
	}

	public void getReferencedItemsRecursive(Integer stateId, Set<SiteNodeVersionVO> siteNodeVersionIdList, Set<ContentVersionVO> contentVersionVOList, boolean includeMetaInfo, InfoGluePrincipal principal, Database db, SiteNodeVO siteNode, Set<Integer> relatedSiteNodeVersionIdsToCheck, Set<Integer> relatedContentVersionIdsToCheck, int limit, int currentLevel, ProcessBean processBean, Locale locale) throws SystemException, Exception, Bug 
	{
    	processBean.updateProcess(getLocalizedString(locale, "tool.structuretool.publicationProcess.checkingForRelatedItems"));

		Timer t = new Timer();
		
		List siteNodeVersionVOListSubList = new ArrayList();
    	siteNodeVersionVOListSubList.addAll(relatedSiteNodeVersionIdsToCheck);
    	
    	List relatedSiteNodes = new ArrayList<Integer>();
    	List relatedContents = new ArrayList<Integer>();
    	
    	int slotSize = 500;
    	if(siteNodeVersionVOListSubList.size() > 0)
    	{
	    	List<Integer> subList = siteNodeVersionVOListSubList.subList(0, (siteNodeVersionVOListSubList.size() > slotSize ? slotSize : siteNodeVersionVOListSubList.size()));
	    	while(subList != null && subList.size() > 0)
	    	{
	    		relatedSiteNodes.addAll(RegistryController.getController().getMatchingRegistryVOListForReferencingEntities(SiteNodeVersion.class.getName(), subList, db));
	    		siteNodeVersionVOListSubList = siteNodeVersionVOListSubList.subList(subList.size()-1, siteNodeVersionVOListSubList.size()-1);
	    		subList =  siteNodeVersionVOListSubList.subList(0, (siteNodeVersionVOListSubList.size() > slotSize ? slotSize : siteNodeVersionVOListSubList.size()));
	    	}
    	}
		processBean.updateProcess(getLocalizedString(locale, "tool.structuretool.publicationProcess.foundRelatedSoFar", relatedSiteNodes.size()));

    	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("first part", t.getElapsedTime());

		Set<Integer> localRelatedSiteNodeIdsToCheck = new HashSet<Integer>();
		Set<Integer> localRelatedContentIdsToCheck = new HashSet<Integer>();
		
		Iterator<RegistryVO> relatedSiteNodesIterator = relatedSiteNodes.iterator();
        while(relatedSiteNodesIterator.hasNext())
        {
            RegistryVO registryVO = relatedSiteNodesIterator.next();
        	if(registryVO.getEntityName().equals(SiteNode.class.getName()))
        	{
        		localRelatedSiteNodeIdsToCheck.add(new Integer(registryVO.getEntityId()));
            }
            else if(registryVO.getEntityName().equals(Content.class.getName()))
            {
            	localRelatedContentIdsToCheck.add(new Integer(registryVO.getEntityId()));
            }
		}
        
        //Now let's get all related contents
    	List<Integer> relatedContentVersionIdsToCheckSubList = new ArrayList<Integer>();
    	relatedContentVersionIdsToCheckSubList.addAll(relatedContentVersionIdsToCheck);
    	if(relatedContentVersionIdsToCheckSubList.size() > 0)
    	{
    		List<Integer> subList = relatedContentVersionIdsToCheckSubList.subList(0, (relatedContentVersionIdsToCheckSubList.size() > slotSize ? slotSize : relatedContentVersionIdsToCheckSubList.size()));
        	while(subList != null && subList.size() > 0)
        	{
        		relatedContents.addAll(RegistryController.getController().getMatchingRegistryVOListForReferencingEntities(ContentVersion.class.getName(), subList, db));
        		relatedContentVersionIdsToCheckSubList = relatedContentVersionIdsToCheckSubList.subList(subList.size()-1, relatedContentVersionIdsToCheckSubList.size() -1);
        		subList = relatedContentVersionIdsToCheckSubList.subList(0, (relatedContentVersionIdsToCheckSubList.size() > slotSize ? slotSize : relatedContentVersionIdsToCheckSubList.size()));
        	}

			Iterator<RegistryVO> relatedContentsIterator = relatedContents.iterator();
	        while(relatedContentsIterator.hasNext())
	        {
	            RegistryVO registryVO = relatedContentsIterator.next();
            	if(registryVO.getEntityName().equals(SiteNode.class.getName()))
            	{
            		localRelatedSiteNodeIdsToCheck.add(new Integer(registryVO.getEntityId()));
	            }
	            else if(registryVO.getEntityName().equals(Content.class.getName()))
	            {
	            	localRelatedContentIdsToCheck.add(new Integer(registryVO.getEntityId()));
	            }
			}
    	}
    	
    	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("third part", t.getElapsedTime());
    	
		//Set<Integer> relatedContentIds = new HashSet<Integer>();
		//Set<Integer> relatedSiteNodeIds = new HashSet<Integer>();
		
		//Now lets batch fetch the relations for all contents and for all sitenodes. Store them in new Sets. Must be possible recursive.
		if(currentLevel < limit)
		{
			List<SiteNodeVO> localRelatedSiteNodeVersionVOList = SiteNodeVersionController.getController().getLatestSiteNodeVersionIds(localRelatedSiteNodeIdsToCheck, db);
			List<ContentVersionVO> localRelatedContentVersionVOList = ContentVersionController.getContentVersionController().getLatestContentVersionVOListPerLanguage(localRelatedContentIdsToCheck, db);
			
	        //Adding it to the full list
			Set<Integer> localRelatedSiteNodeVersionIdSet = new HashSet<Integer>();
			Iterator<SiteNodeVO> localRelatedSiteNodeVersionVOListIterator = localRelatedSiteNodeVersionVOList.iterator();
			while(localRelatedSiteNodeVersionVOListIterator.hasNext())
			{
				SiteNodeVO relatedSiteNodeVO = localRelatedSiteNodeVersionVOListIterator.next();
				localRelatedSiteNodeVersionIdSet.add(relatedSiteNodeVO.getSiteNodeVersionId());
				
                Integer repositoryId = relatedSiteNodeVO.getRepositoryId();
                Integer siteNodeRepositoryId = siteNode.getRepositoryId();
                boolean allowedSiteNodeVersion = repositoryId.intValue() == siteNodeRepositoryId.intValue();
                if(CmsPropertyHandler.getAllowCrossSiteSubmitToPublish().equalsIgnoreCase("true"))
                {
                	if(AccessRightController.getController().getIsPrincipalAuthorized(db, principal, "Repository.Read", "" + repositoryId) || AccessRightController.getController().getIsPrincipalAuthorized(db, principal, "Repository.Write", "" + repositoryId))
                	{
            			Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(relatedSiteNodeVO.getSiteNodeVersionId(), db);
            			if(protectedSiteNodeVersionId == null || AccessRightController.getController().getIsPrincipalAuthorized(db, principal, "SiteNodeVersion.SubmitToPublish", protectedSiteNodeVersionId.toString()))
	        				allowedSiteNodeVersion = true;
                	}
                }

				if(relatedSiteNodeVO.getStateId() == 0 && allowedSiteNodeVersion)
				{
					SiteNodeVersionVO siteNodeVersionVO = new SiteNodeVersionVO();
				    siteNodeVersionVO.setSiteNodeVersionId(relatedSiteNodeVO.getSiteNodeVersionId());
				    siteNodeVersionVO.setSiteNodeId(relatedSiteNodeVO.getSiteNodeId());
				    siteNodeVersionVO.setStateId(relatedSiteNodeVO.getStateId());
				    siteNodeVersionVO.setVersionModifier(relatedSiteNodeVO.getVersionModifier());
				    siteNodeVersionVO.setModifiedDateTime(siteNodeVersionVO.getModifiedDateTime());
				    
					siteNodeVersionIdList.add(siteNodeVersionVO);
				}
			}
			
			Set<Integer> localRelatedContentVersionIdSet = new HashSet<Integer>();
			Iterator<ContentVersionVO> localRelatedContentVersionVOListIterator = localRelatedContentVersionVOList.iterator();
			while(localRelatedContentVersionVOListIterator.hasNext())
			{
				ContentVersionVO contentVersionVO = localRelatedContentVersionVOListIterator.next();
				localRelatedContentVersionIdSet.add(contentVersionVO.getContentVersionId());
								
		        ContentTypeDefinitionVO contentTypeDefinitionVO = null;
		        if(contentVersionVO.getContentTypeDefinitionId() != null)
		        	contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVersionVO.getContentTypeDefinitionId(), db);
		        
		        if(includeMetaInfo || (!includeMetaInfo && (contentTypeDefinitionVO == null || !contentTypeDefinitionVO.getName().equalsIgnoreCase("Meta info"))))
		        {
		            Integer repositoryId = contentVersionVO.getRepositoryId();
		            Integer siteNodeRepositoryId = siteNode.getRepositoryId();
		            boolean allowedContent = repositoryId.intValue() == siteNodeRepositoryId.intValue();
					if(CmsPropertyHandler.getAllowCrossSiteSubmitToPublish().equalsIgnoreCase("true"))
		            {
		            	if(AccessRightController.getController().getIsPrincipalAuthorized(db, principal, "Repository.Read", "" + repositoryId) || AccessRightController.getController().getIsPrincipalAuthorized(db, principal, "Repository.Write", "" + repositoryId))
		            	{
		                	Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentVersionVO.getContentId(), db);
		        			if(protectedContentId == null || AccessRightController.getController().getIsPrincipalAuthorized(db, principal, "Content.SubmitToPublish", protectedContentId.toString()))
		        				allowedContent = true;
		            	}
		            }
			        
					if(contentVersionVO.getStateId() == 0 && allowedContent)
					{
						contentVersionVOList.add(contentVersionVO);
					}
		        }
			}
			processBean.updateProcess(getLocalizedString(locale, "tool.structuretool.publicationProcess.foundSoFar", siteNodeVersionIdList.size() + "/" + contentVersionVOList.size()));
			
			getReferencedItemsRecursive(stateId, siteNodeVersionIdList, contentVersionVOList, includeMetaInfo, principal, db, siteNode, localRelatedSiteNodeVersionIdSet, localRelatedContentVersionIdSet, limit, currentLevel+1, processBean, locale);
		}
	}
	
	private List<SiteNodeVersionVO> getSiteNodeVersionsRecursive(SiteNodeVO siteNodeVO, Integer stateId, Database db, boolean includeMetaInfo, InfoGluePrincipal principal) throws ConstraintException, SystemException, Exception
	{
		return getSiteNodeVersionsRecursive(siteNodeVO, stateId, db, includeMetaInfo, true, principal);
	}
	
	private List<SiteNodeVersionVO> getSiteNodeVersionsRecursive(SiteNodeVO siteNodeVO, Integer stateId, Database db, boolean includeMetaInfo, boolean recurseSiteNodes, InfoGluePrincipal principal) throws ConstraintException, SystemException, Exception
	{
		Timer t = new Timer();
		List<SiteNodeVersionVO> siteNodeVersionVOToCheck = new ArrayList<SiteNodeVersionVO>();
        
	    Integer latestSiteNodeVersionId = siteNodeVO.getSiteNodeVersionId();
	    Integer knownStateId = siteNodeVO.getStateId();
	    
	    SiteNodeVersionVO siteNodeVersionVO = new SiteNodeVersionVO();
	    siteNodeVersionVO.setSiteNodeVersionId(latestSiteNodeVersionId);
	    siteNodeVersionVO.setSiteNodeId(siteNodeVO.getSiteNodeId());
	    siteNodeVersionVO.setStateId(knownStateId);
	    //siteNodeVersionVO.setRepositoryId(siteNodeVO.getRepositoryId());
	    siteNodeVersionVO.setVersionModifierDisplayName(siteNodeVO.getVersionModifier());
	    siteNodeVersionVO.setVersionModifier(siteNodeVO.getVersionModifier());

	    siteNodeVersionVO.setModifiedDateTime(siteNodeVO.getModifiedDateTime());
		
	    siteNodeVersionVOToCheck.add(siteNodeVersionVO);
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getSiteNodeAndAffectedItemsRecursive.getLatestActiveSiteNodeVersionVO", t.getElapsedTime());
		
        if(recurseSiteNodes)
        {
			// Get the children of this siteNode and do the recursion
        	//System.out.println("ChildPages:" + siteNodeVO.getChildCount());
        	if(siteNodeVO.getChildCount() == null || siteNodeVO.getChildCount() > 0)
        	{
	        	List childSiteNodeList = SiteNodeController.getController().getSiteNodeChildrenVOList(siteNodeVO.getId(), db);
				RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getSiteNodeAndAffectedItemsRecursive.getSiteNodeChildrenVOList", t.getElapsedTime());
				//Collection childSiteNodeList = siteNode.getChildSiteNodes();
				Iterator cit = childSiteNodeList.iterator();
				while (cit.hasNext())
				{
					SiteNodeVO childSiteNode = (SiteNodeVO) cit.next();
					siteNodeVersionVOToCheck.addAll(getSiteNodeVersionsRecursive(childSiteNode, stateId, db, includeMetaInfo, principal));
				}
        	}
        }   
        
        return siteNodeVersionVOToCheck;
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
    public List getMetaInfoContentVersionVOList(SiteNodeVersionVO siteNodeVersionVO, SiteNodeVO siteNodeVO, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException, Exception
    {
        List contentVersionVOList = new ArrayList();
        
        Database db = CastorDatabaseService.getDatabase();

	    beginTransaction(db);

        try
        {
            //SiteNodeVersionVO siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId, db);
            contentVersionVOList = getMetaInfoContentVersionVOList(db, siteNodeVersionVO, siteNodeVO, infoGluePrincipal);
            //List contentVersions = getMetaInfoContentVersions(db, siteNodeVersion, infoGluePrincipal);
            //contentVersionVOList = toVOList(contentVersions);
            
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
   
    private List<ContentVersionVO> getMetaInfoContentVersionVOList(Database db, SiteNodeVersionVO siteNodeVersion, SiteNodeVO siteNodeVO, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException, Exception
    {
        List<ContentVersionVO> contentVersions = new ArrayList<ContentVersionVO>();
        
        List<LanguageVO> languageVOList = LanguageController.getController().getAvailableLanguageVOListForRepository(siteNodeVO.getRepositoryId(), db);
		//LanguageVO masterLanguage = LanguageController.getController().getMasterLanguage(siteNodeVersion.getOwningSiteNode().getRepository().getId(), db);
		
		if(siteNodeVO.getMetaInfoContentId() != null)
		{
			Iterator<LanguageVO> languageIterator = languageVOList.iterator();
			while(languageIterator.hasNext())
			{
				LanguageVO language = languageIterator.next();
				ContentVersionVO contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(siteNodeVO.getMetaInfoContentId(), language.getId(), db);
				if(contentVersion != null)
				    contentVersions.add(contentVersion);
			}
		}
		/*
		else
		{
			Integer metaInfoAvailableServiceBindingId = null;
			Integer serviceBindingId = null;
			AvailableServiceBindingVO availableServiceBindingVO = AvailableServiceBindingController.getController().getAvailableServiceBindingVOWithName("Meta information", db);
			if(availableServiceBindingVO != null)
				metaInfoAvailableServiceBindingId = availableServiceBindingVO.getAvailableServiceBindingId();
			
			Collection serviceBindings = siteNodeVersion.getServiceBindings();
			Iterator serviceBindingIterator = serviceBindings.iterator();
			while(serviceBindingIterator.hasNext())
			{
				ServiceBinding serviceBinding = (ServiceBinding)serviceBindingIterator.next();
				if(serviceBinding.getAvailableServiceBinding().getId().intValue() == metaInfoAvailableServiceBindingId.intValue())
				{
					serviceBindingId = serviceBinding.getId();
					break;
				}
			}
	
			if(serviceBindingId != null)
			{
				List boundContents = ContentController.getBoundContents(serviceBindingId); 
				if(boundContents.size() > 0)
				{
					ContentVO contentVO = (ContentVO)boundContents.get(0);
					
					Iterator<LanguageVO> languageIterator = languageVOList.iterator();
					while(languageIterator.hasNext())
					{
						LanguageVO language = languageIterator.next();
						ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentVO.getId(), language.getId(), db);
						
						if(contentVersion != null)
						    contentVersions.add(contentVersion);
					}
				}
			}
		}
		*/
		return contentVersions;
    }

	
	/**
	 * Updates the SiteNodeVersion.
	 */
	
	public SiteNodeVersionVO update(SiteNodeVersionVO siteNodeVersionVO) throws ConstraintException, SystemException, Exception
	{
    	registryController.updateSiteNodeVersionThreaded(siteNodeVersionVO);

		return (SiteNodeVersionVO) updateEntity(SiteNodeVersionImpl.class, (BaseEntityVO)siteNodeVersionVO);
	}  
	
	/**
	 * Updates the SiteNodeVersion within a transaction.
	 */
	
	public SiteNodeVersionVO update(SiteNodeVersionVO siteNodeVersionVO, Database db) throws ConstraintException, SystemException, Exception
	{
		MediumSiteNodeVersionImpl siteNodeVersion = getMediumSiteNodeVersionWithId(siteNodeVersionVO.getId(), db);
    	registryController.updateSiteNodeVersionThreaded(siteNodeVersionVO);

    	siteNodeVersion.setValueObject(siteNodeVersionVO);
    	return siteNodeVersionVO;
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
 
