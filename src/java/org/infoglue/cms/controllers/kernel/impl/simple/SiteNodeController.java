
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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.ObjectNotFoundException;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentCategory;
import org.infoglue.cms.entities.content.ContentCategoryVO;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.SmallestContentVersionVO;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.AccessRight;
import org.infoglue.cms.entities.management.AccessRightGroup;
import org.infoglue.cms.entities.management.AccessRightRole;
import org.infoglue.cms.entities.management.AccessRightUser;
import org.infoglue.cms.entities.management.AccessRightVO;
import org.infoglue.cms.entities.management.Category;
import org.infoglue.cms.entities.management.ContentTypeDefinition;
import org.infoglue.cms.entities.management.InterceptionPoint;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.Registry;
import org.infoglue.cms.entities.management.RegistryVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.ServiceDefinition;
import org.infoglue.cms.entities.management.ServiceDefinitionVO;
import org.infoglue.cms.entities.management.SiteNodeTypeDefinition;
import org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl;
import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.management.impl.simple.SiteNodeTypeDefinitionImpl;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.structure.Qualifyer;
import org.infoglue.cms.entities.structure.ServiceBinding;
import org.infoglue.cms.entities.structure.ServiceBindingVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.impl.simple.MediumSiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallQualifyerImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallServiceBindingImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl;
import org.infoglue.cms.entities.workflow.Event;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.DateHelper;
import org.infoglue.cms.util.XMLHelper;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.RequestAnalyser;
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
	 * This method returns selected active content versions.
	 */
    
	public List<SiteNode> getSiteNodeList(Integer repositoryId, Integer minimumId, Integer limit, Database db) throws SystemException, Bug, Exception
	{
		List<SiteNode> siteNodeList = new ArrayList<SiteNode>();

        OQLQuery oql = db.getOQLQuery( "SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl sn WHERE sn.repositoryId = $1 AND sn.siteNodeId > $2 ORDER BY sn.siteNodeId LIMIT $3");
    	oql.bind(repositoryId);
		oql.bind(minimumId);
		oql.bind(limit);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		while (results.hasMore()) 
        {
			SiteNode siteNode = (SiteNode)results.next();
			siteNodeList.add(siteNode);
        }
		
		results.close();
		oql.close();

		return siteNodeList;
	}

	public SiteNodeVO getSiteNodeVOWithId(Integer siteNodeId) throws SystemException
    {
    	SiteNodeVO siteNodeVO = null;
    	
		Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {	
			siteNodeVO = getSiteNodeVOWithId(siteNodeId, db, false);
			
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
	 * This method gets the siteNodeVO with the given id
	 */
	 
	public SiteNodeVO getSiteNodeVOWithId(Integer siteNodeId, boolean skipCaching) throws SystemException
    {
		String key = "" + siteNodeId;
		SiteNodeVO siteNodeVO = (SiteNodeVO)CacheController.getCachedObjectFromAdvancedCache("siteNodeCache", key);
		if(siteNodeVO != null && !skipCaching)
		{
			return siteNodeVO;
		}
		
		Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {	
			siteNodeVO = getSiteNodeVOWithId(siteNodeId, db, skipCaching);
			if(siteNodeVO != null && !skipCaching)
				CacheController.cacheObjectInAdvancedCache("siteNodeCache", key, siteNodeVO);

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
	 * This method gets the siteNodeVO with the given id
	 */
	 
	public SiteNodeVO getSiteNodeVOWithIdIfInCache(Integer siteNodeId, Database db) throws SystemException, Bug, Exception
	{
		String key = "" + siteNodeId;
		SiteNodeVO siteNodeVO = (SiteNodeVO)CacheController.getCachedObjectFromAdvancedCache("siteNodeCache", key);
		if(siteNodeVO != null)
		{
			return siteNodeVO;
		}
		return null;
	}

	/**
	 * This method gets the siteNodeVO with the given id
	 */
	 
	public static SiteNodeVO getSiteNodeVOWithId(Integer siteNodeId, Database db) throws SystemException, Bug, Exception
	{
		return getSiteNodeVOWithId(siteNodeId, db, false);
	}
	
	/**
	 * This method gets the siteNodeVO with the given id
	 */
	 
	public static SiteNodeVO getSiteNodeVOWithId(Integer siteNodeId, Database db, boolean skipCaching) throws SystemException, Bug, Exception
	{
		if(siteNodeId == null || siteNodeId == 0)
		{
			logger.info("Returned as " + siteNodeId + " was requested");
			return null;
		}
		
		String key = "" + siteNodeId;
		SiteNodeVO siteNodeVO = (SiteNodeVO)CacheController.getCachedObjectFromAdvancedCache("siteNodeCache", key);
		if(siteNodeVO != null)
		{
			if(siteNodeVO.getChildCount() == null)
				logger.error("Fail: a siteNodeVO was read the old way...");
			//logger.info("There was an cached siteNodeVO:" + siteNodeVO);
		}
		else
		{
			StringBuffer SQL = new StringBuffer();
	    
	   		Timer t = new Timer();

	    	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
	    	{
		   		SQL.append("CALL SQL select sn.siNoId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiNoId, sn.metaInfoContentId, sn.repositoryId, sn.siNoTypeDefId, sn.creator, (select count(*) from cmSiNo sn2 where sn2.parentSiNoId = sn.siNoId) AS childCount, snv.siNoVerId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, 0 AS languageId, '' as attributes from cmSiNo sn, cmSiNoVer snv ");
		   		SQL.append("where ");
		   		SQL.append("sn.siNoId = $1 ");
		   		SQL.append("AND snv.siNoId = sn.siNoId ");
		   		SQL.append("AND snv.siNoVerId = ( ");
		   		SQL.append("	select max(siNoVerId) from cmSiNoVer snv2 ");
		   		SQL.append("	WHERE ");
		   		SQL.append("	snv2.siNoId = snv.siNoId AND ");
		   		//SQL.append("	snv2.isActive = $2 AND snv2.stateId >= $3 ");
		   		SQL.append("	snv2.stateId >= $2 ");
		   		SQL.append("	) ");
		   		SQL.append("order by sn.siNoId DESC AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");
	    	}
	    	else
	    	{
		   		SQL.append("CALL SQL select sn.siteNodeId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiteNodeId, sn.metaInfoContentId, sn.repositoryId, sn.siteNodeTypeDefinitionId, sn.creator, (select count(*) from cmSiteNode sn2 where sn2.parentSiteNodeId = sn.siteNodeId) AS childCount, snv.siteNodeVersionId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, 0 AS languageId, '' as attributes from cmSiteNode sn, cmSiteNodeVersion snv ");
		   		SQL.append("where ");
		   		SQL.append("sn.siteNodeId = $1 ");
		   		SQL.append("AND snv.siteNodeId = sn.siteNodeId ");
		   		SQL.append("AND snv.siteNodeVersionId = ( ");
		   		SQL.append("	select max(siteNodeVersionId) from cmSiteNodeVersion snv2 ");
		   		SQL.append("	WHERE ");
		   		SQL.append("	snv2.siteNodeId = snv.siteNodeId AND ");
		   		//SQL.append("	snv2.isActive = $2 AND snv2.stateId >= $3 ");
		   		SQL.append("	snv2.stateId >= $2 ");
		   		SQL.append("	) ");
		   		SQL.append("order by sn.siteNodeId DESC AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");    		
	    	}
	    	
	    	//System.out.println("SQL:" + SQL);
	    	//logger.info("SQL:" + SQL);
	    	//logger.info("parentSiteNodeId:" + parentSiteNodeId);
	    	//logger.info("showDeletedItems:" + showDeletedItems);
	    	OQLQuery oql = db.getOQLQuery(SQL.toString());
			oql.bind(siteNodeId);
			//oql.bind(true);
			oql.bind(new Integer(CmsPropertyHandler.getOperatingMode()));
	
			QueryResults results = oql.execute(Database.ReadOnly);
			//t.printElapsedTime("Executed query.....");
			if (results.hasMore()) 
			{
				SiteNode siteNode = (SiteNode)results.next();
				siteNodeVO = siteNode.getValueObject();			
	
				if(!skipCaching)
				{
					String siteNodeCacheKey = "" + siteNode.getValueObject().getId();
					CacheController.cacheObjectInAdvancedCache("siteNodeCache", siteNodeCacheKey, siteNode.getValueObject());
				}
			}
			else
				logger.warn("SiteNode not found: " + siteNodeId);
			
			results.close();
			oql.close();
			
			if(siteNodeVO == null)
			{
				logger.info("Falling back to old forgiving logic for siteNodeId: " + siteNodeId + ". It must be in trouble.");
				siteNodeVO = getSiteNodeVOWithIdIfFailed(siteNodeId, db);
			}
			
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getSmallestSiteNodeVOWithId", t.getElapsedTime());
		}
		
		if(siteNodeVO != null && siteNodeVO.getMetaInfoContentId() != null)
			metaInfoSiteNodeIdMap.put(siteNodeVO.getMetaInfoContentId(), siteNodeVO.getId());
		
		return siteNodeVO;
	}
	
	/**
	 * This method gets the siteNodeVO with the given id
	 */
	 
	public SiteNodeVO getSiteNodeVOWithIdNoStateCheck(Integer siteNodeId, Database db) throws SystemException, Bug, Exception
	{
		String key = "" + siteNodeId;
		SiteNodeVO siteNodeVO = (SiteNodeVO)CacheController.getCachedObjectFromAdvancedCache("siteNodeCacheWithLatestVersion", key);
		if(siteNodeVO != null)
		{
			if(siteNodeVO.getChildCount() == null)
				logger.error("Fail: a siteNodeVO was read the old way...");
			//logger.info("There was an cached siteNodeVO:" + siteNodeVO);
		}
		else
		{
	   		StringBuffer SQL = new StringBuffer();
	    
	   		Timer t = new Timer();

	    	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
	    	{
		   		SQL.append("CALL SQL select sn.siNoId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiNoId, sn.metaInfoContentId, sn.repositoryId, sn.siNoTypeDefId, sn.creator, (select count(*) from cmSiNo sn2 where sn2.parentSiNoId = sn.siNoId) AS childCount, snv.siNoVerId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, 0 AS languageId, '' as attributes from cmSiNo sn, cmSiNoVer snv ");
		   		SQL.append("where ");
		   		SQL.append("sn.siNoId = $1 ");
		   		SQL.append("AND snv.siNoId = sn.siNoId ");
		   		SQL.append("AND snv.siNoVerId = ( ");
		   		SQL.append("	select max(siNoVerId) from cmSiNoVer snv2 ");
		   		SQL.append("	WHERE ");
		   		SQL.append("	snv2.siNoId = snv.siNoId");
		   		SQL.append("	) ");
		   		SQL.append("order by sn.siNoId DESC AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");
	    	}
	    	else
	    	{
		   		SQL.append("CALL SQL select sn.siteNodeId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiteNodeId, sn.metaInfoContentId, sn.repositoryId, sn.siteNodeTypeDefinitionId, sn.creator, (select count(*) from cmSiteNode sn2 where sn2.parentSiteNodeId = sn.siteNodeId) AS childCount, snv.siteNodeVersionId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, 0 AS languageId, '' as attributes from cmSiteNode sn, cmSiteNodeVersion snv ");
		   		SQL.append("where ");
		   		SQL.append("sn.siteNodeId = $1 ");
		   		SQL.append("AND snv.siteNodeId = sn.siteNodeId ");
		   		SQL.append("AND snv.siteNodeVersionId = ( ");
		   		SQL.append("	select max(siteNodeVersionId) from cmSiteNodeVersion snv2 ");
		   		SQL.append("	WHERE ");
		   		SQL.append("	snv2.siteNodeId = snv.siteNodeId");
		   		SQL.append("	) ");
		   		SQL.append("order by sn.siteNodeId DESC AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");    		
	    	}
	    	//System.out.println("SQL:" + SQL);
	    	//System.out.println("siteNodeId:" + siteNodeId);
	    	//logger.info("SQL:" + SQL);
	    	//logger.info("parentSiteNodeId:" + parentSiteNodeId);
	    	//logger.info("showDeletedItems:" + showDeletedItems);
	    	OQLQuery oql = db.getOQLQuery(SQL.toString());
			oql.bind(siteNodeId);
	
			QueryResults results = oql.execute(Database.ReadOnly);
			//t.printElapsedTime("Executed query.....");
			if (results.hasMore()) 
			{
				SmallestSiteNodeImpl siteNode = (SmallestSiteNodeImpl)results.next();
				//System.out.println("siteNode:" + siteNode.getSiteNodeVersionId() + ":" + siteNode.getValueObject().getSiteNodeVersionId() + ":" + siteNode.getValueObject().getVersionModifier() + ":" + siteNode.getValueObject().getModifiedDateTime());
				siteNodeVO = siteNode.getValueObject();			
	
				String siteNodeCacheKey = "" + siteNode.getValueObject().getId();
				CacheController.cacheObjectInAdvancedCache("siteNodeCacheWithLatestVersion", siteNodeCacheKey, siteNode.getValueObject(), new String[]{CacheController.getPooledString(3, siteNode.getValueObject().getId())}, true);
			}
			
			results.close();
			oql.close();
			
			if(siteNodeVO == null)
			{
				logger.error("Falling back to old forgiving logic for siteNodeId: " + siteNodeId + ". It must be in trouble.");
				siteNodeVO = getSiteNodeVOWithIdIfFailed(siteNodeId, db);
			}
			
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getSmallestSiteNodeVOWithId", t.getElapsedTime());
		}
		
		return siteNodeVO;
	}

	public Map<Integer,SiteNodeVO> getSiteNodeVOMap(Integer[] siteNodeIds, Database db) throws SystemException, Bug, Exception
	{
	    Timer t = new Timer();
	    Map<Integer,SiteNodeVO> siteNodeVOMap = new HashMap<Integer,SiteNodeVO>();
	    
	    //System.out.println("siteNodeIds to fetch:" + siteNodeIds);

	    List<Integer> uncachedSiteNodeIds = new ArrayList<Integer>();
	    for(Integer siteNodeId : siteNodeIds)
	    {
	    	SiteNodeVO siteNodeVO = getSiteNodeVOWithIdIfInCache(siteNodeId,db);
	    	if(siteNodeVO != null)
	    	{
	    		siteNodeVOMap.put(siteNodeId, siteNodeVO);
	    	}
	    	else
	    		uncachedSiteNodeIds.add(siteNodeId);
	    }
	    siteNodeIds = uncachedSiteNodeIds.toArray(new Integer[uncachedSiteNodeIds.size()]);
	    //System.out.println("siteNodeIds to really fetch:" + siteNodeIds);
	    
	    StringBuilder variables = new StringBuilder();
	    for(int i=0; i<siteNodeIds.length; i++)
	    	variables.append("$" + (i+3) + (i+1!=siteNodeIds.length ? "," : ""));
		
	    //System.out.println("siteNodeIds:" + siteNodeIds.length);
	    //System.out.println("variables:" + variables);

   		StringBuffer SQL = new StringBuffer();
	    
    	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
    	{
	   		SQL.append("CALL SQL select sn.siNoId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiNoId, sn.metaInfoContentId, sn.repositoryId, sn.siNoTypeDefId, sn.creator, (select count(*) from cmSiNo sn2 where sn2.parentSiNoId = sn.siNoId) AS childCount, snv.siNoVerId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, 0 AS languageId, '' as attributes from cmSiNo sn, cmSiNoVer snv ");
	   		SQL.append("where ");
	   		SQL.append("snv.siNoId = sn.siNoId ");
	   		SQL.append("AND snv.siNoVerId = ( ");
	   		SQL.append("	select max(siNoVerId) from cmSiNoVer snv2 ");
	   		SQL.append("	WHERE ");
	   		SQL.append("	snv2.siNoId = snv.siNoId AND ");
	   		SQL.append("	snv2.isActive = $1 AND snv2.stateId >= $2 ");
	   		SQL.append("	) ");
	   		SQL.append("AND sn.siNoId IN (" + variables + ") ");
	   		SQL.append("order by sn.siNoId DESC AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");
    	}
    	else
    	{
	   		SQL.append("CALL SQL select sn.siteNodeId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiteNodeId, sn.metaInfoContentId, sn.repositoryId, sn.siteNodeTypeDefinitionId, sn.creator, (select count(*) from cmSiteNode sn2 where sn2.parentSiteNodeId = sn.siteNodeId) AS childCount, snv.siteNodeVersionId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, 0 AS languageId, '' as attributes from cmSiteNode sn, cmSiteNodeVersion snv ");
	   		SQL.append("where ");
	   		SQL.append("snv.siteNodeId = sn.siteNodeId ");
	   		SQL.append("AND snv.siteNodeVersionId = ( ");
	   		SQL.append("	select max(siteNodeVersionId) from cmSiteNodeVersion snv2 ");
	   		SQL.append("	WHERE ");
	   		SQL.append("	snv2.siteNodeId = snv.siteNodeId AND ");
	   		SQL.append("	snv2.isActive = $1 AND snv2.stateId >= $2 ");
	   		SQL.append("	) ");
	   		SQL.append("AND sn.siNoId IN (" + variables + ") ");
	   		SQL.append("order by sn.siteNodeId DESC AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");    		
    	}
	
    	//System.out.println("SQL:" + SQL);
    	//logger.info("SQL:" + SQL);
    	//logger.info("parentSiteNodeId:" + parentSiteNodeId);
    	//logger.info("showDeletedItems:" + showDeletedItems);
    	OQLQuery oql = db.getOQLQuery(SQL.toString());
		oql.bind(true);
		oql.bind(new Integer(CmsPropertyHandler.getOperatingMode()));
		for(Integer entityId : siteNodeIds)
			oql.bind(entityId);

		QueryResults results = oql.execute(Database.ReadOnly);
		while (results.hasMore()) 
		{
			SiteNode siteNode = (SiteNode)results.next();
			SiteNodeVO siteNodeVO = siteNode.getValueObject();			
			siteNodeVOMap.put(siteNodeVO.getId(), siteNodeVO);
		}

		results.close();
		oql.close();
				
		return siteNodeVOMap;		
	}

	/**
	 * This method gets the siteNodeVO with the given id
	 */
	 
	public Map<Integer,SiteNodeVO> getSiteNodeVOMapWithNoStateCheck(List<Integer> siteNodeIds) throws SystemException, Bug, Exception
    {
		Map<Integer,SiteNodeVO> siteNodeVOMap = null;
    	
		Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {	
			siteNodeVOMap = getSiteNodeVOMapWithNoStateCheck(siteNodeIds, db);
			
	    	commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }      
        
        return siteNodeVOMap;
    }        
	
	
	public Map<Integer,SiteNodeVO> getSiteNodeVOMapWithNoStateCheck(List<Integer> siteNodeVersionIds, Database db) throws SystemException, Bug, Exception
	{
	    Timer t = new Timer();
	    Map<Integer,SiteNodeVO> siteNodeVOMap = new HashMap<Integer,SiteNodeVO>();
	    if(siteNodeVersionIds == null || siteNodeVersionIds.size() == 0)
	    	return siteNodeVOMap;

	    //System.out.println("siteNodeVersionIds to really fetch:" + siteNodeVersionIds);
	    
	    StringBuilder variables = new StringBuilder();
	    for(int i=0; i<siteNodeVersionIds.size(); i++)
	    	variables.append("$" + (i+1) + (i+1!=siteNodeVersionIds.size() ? "," : ""));
		
	    //System.out.println("siteNodeIds:" + siteNodeIds.length);
	    //System.out.println("variables:" + variables);

   		StringBuffer SQL = new StringBuffer();
	    
    	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
    	{
	   		SQL.append("CALL SQL select sn.siNoId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiNoId, sn.metaInfoContentId, sn.repositoryId, sn.siNoTypeDefId, sn.creator, (select count(*) from cmSiNo sn2 where sn2.parentSiNoId = sn.siNoId) AS childCount, snv.siNoVerId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, 0 AS languageId, '' as attributes from cmSiNo sn, cmSiNoVer snv ");
	   		SQL.append("where ");
	   		SQL.append("snv.siNoId = sn.siNoId ");
	   		SQL.append("AND snv.siNoVerId IN (" + variables + ") ");
	   		SQL.append("order by sn.siNoId DESC AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");
    	}
    	else
    	{
	   		SQL.append("CALL SQL select sn.siteNodeId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiteNodeId, sn.metaInfoContentId, sn.repositoryId, sn.siteNodeTypeDefinitionId, sn.creator, (select count(*) from cmSiteNode sn2 where sn2.parentSiteNodeId = sn.siteNodeId) AS childCount, snv.siteNodeVersionId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, 0 AS languageId, '' as attributes from cmSiteNode sn, cmSiteNodeVersion snv ");
	   		SQL.append("where ");
	   		SQL.append("snv.siteNodeId = sn.siteNodeId ");
	   		SQL.append("AND snv.siteNodeVersionId IN (" + variables + ") ");
	   		SQL.append("order by sn.siteNodeId DESC AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");    		
    	}
	
    	OQLQuery oql = db.getOQLQuery(SQL.toString());
		for(Integer entityId : siteNodeVersionIds)
			oql.bind(entityId);

		QueryResults results = oql.execute(Database.ReadOnly);
		//t.printElapsedTime("Executed query.....");
		while (results.hasMore()) 
		{
			SmallestSiteNodeImpl siteNode = (SmallestSiteNodeImpl)results.next();
			SiteNodeVO siteNodeVO = siteNode.getValueObject();			
			siteNodeVOMap.put(siteNodeVO.getSiteNodeVersionId(), siteNodeVO);
		}
		//t.printElapsedTime("siteNodeVOMap populated:" + siteNodeVOMap.size());

		results.close();
		oql.close();
				
		return siteNodeVOMap;		
	}
	
    /**
	 * This method gets the siteNodeVO with the given id
	 */
	 
    public SiteNodeVO getSmallSiteNodeVOWithId(Integer siteNodeId, Database db) throws SystemException, Bug, Exception
    {
		return getSiteNodeVOWithId(siteNodeId, db); //(SiteNodeVO) getVOWithId(SmallSiteNodeImpl.class, siteNodeId, db);
    }

    /**
	 * This method gets the siteNodeVO with the given id
	 */
	 
    public static SiteNodeVO getSiteNodeVOWithIdIfFailed(Integer siteNodeId, Database db) throws SystemException, Bug, Exception
    {
    	try
    	{
    		return (SiteNodeVO) getVOWithId(SmallSiteNodeImpl.class, siteNodeId, db);
    	}
    	catch (Exception e) 
    	{
    		return null;
		}
    }

    public SiteNode getSiteNodeWithId(Integer siteNodeId, Database db) throws SystemException, Bug
    {
        return getSiteNodeWithId(siteNodeId, db, false);
    }
    
    public static SiteNode getSiteNodeWithId(Integer siteNodeId, Database db, boolean readOnly) throws SystemException, Bug
    {
        SiteNode siteNode = null;
        try
        {
			RequestAnalyser.getRequestAnalyser().incApproximateNumberOfDatabaseQueries();

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
			logger.warn("An error occurred when we tried to fetch the SiteNode. Reason:" + e.getMessage() + ". Not retrying...");
            throw new SystemException("An error occurred when we tried to fetch the SiteNode. Reason:" + e.getMessage(), e);    
        }
		finally
		{
			RequestAnalyser.getRequestAnalyser().decApproximateNumberOfDatabaseQueries();
		}

        if(siteNode == null)
        {
            throw new Bug("The SiteNode with id [" + siteNodeId + "] was not found in SiteNodeHelper.getSiteNodeWithId. This should never happen.");
        }
    
        return siteNode;
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
			logger.error("An error occurred so we should not complete the transaction: " + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction: " + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }        
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
	 * This method deletes a siteNode and also erases all the children and all versions.
	 */
	    
    public void delete(SiteNodeVO siteNodeVO, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {	
			delete(siteNodeVO, db, infogluePrincipal);	
			
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
			logger.error("An error occurred so we should not complete the transaction: " + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction: " + e.getMessage(), e);
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
	 * This method returns true if the sitenode does not have any published siteNodeversions or 
	 * are restricted in any other way.
	 */
	
	private static boolean getIsDeletable(SiteNode siteNode, InfoGluePrincipal infogluePrincipal, Database db) throws SystemException, Exception
	{
		boolean isDeletable = true;
		
		SiteNodeVersion latestSiteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionReadOnly(db, siteNode.getId());
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
			logger.error("An error occurred so we should not complete the transaction: " + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction: " + e.getMessage(), e);
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
        	parentSiteNodeVO = getParentSiteNodeVO(siteNodeId, db);
            commitTransaction(db);
        }
        catch(Exception e)
        {
			logger.error("An error occurred so we should not complete the transaction: " + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction: " + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
		return parentSiteNodeVO;    	
    }
    
	/**
	 * This method returns the value-object of the parent of a specific siteNode. 
	 */
	
	public static SiteNodeVO getParentSiteNodeVO(Integer siteNodeId, Database db) throws SystemException, Bug, Exception
	{
		SiteNodeVO parent = null;
		 
		SiteNodeVO siteNodeVO = getSiteNodeVOWithId(siteNodeId, db);
		if(siteNodeVO != null && siteNodeVO.getParentSiteNodeId() != null)
			parent = getSiteNodeVOWithId(siteNodeVO.getParentSiteNodeId(), db);
		
		return parent;    	
	}

    
	/**
	 * This method returns a list of the children a siteNode has.
	 */
   	
	public List getSiteNodeChildrenVOList(Integer parentSiteNodeId) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		List childrenVOList = null;

		beginTransaction(db);

		try
		{
			/*
			SiteNode siteNode = getSiteNodeWithId(parentSiteNodeId, db);
			Collection children = siteNode.getChildSiteNodes();
			childrenVOList = SiteNodeController.toVOList(children);
        	*/
			childrenVOList = getSiteNodeChildrenVOList(parentSiteNodeId, db);
				
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
			logger.error("An error occurred so we should not complete the transaction: " + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction: " + e.getMessage(), e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
        
		return childrenVOList;
	} 
	
	/**
	 * This method returns a list of the children a siteNode has.
	 */
   	
	public List<SiteNodeVO> getSiteNodeChildrenVOList(Integer parentSiteNodeId, Database db) throws Exception
	{
   		String key = "" + parentSiteNodeId;
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
		/*
        StringBuffer SQL = new StringBuffer();
    	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
    	{
	   		SQL.append("CALL SQL select sn.siNoId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.parentSiNoId, sn.metaInfoContentId, sn.repositoryId, sn.siNoTypeDefId, sn.creator, (select count(*) from cmSiNo sn2 where sn2.parentSiNoId = sn.siNoId) AS childCount, 0 as stateId, 2 as isProtected from cmSiNo sn ");
	   		SQL.append("where sn.parentSiNoId = $1 ");
	   		SQL.append("order by sn.siNoId AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");
    	}
    	else
    	{
	   		SQL.append("CALL SQL select sn.siteNodeId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.parentSiteNodeId, sn.metaInfoContentId, sn.repositoryId, sn.siteNodeTypeDefinitionId, sn.creator, (select count(*) from cmSiteNode sn2 where sn2.parentSiteNodeId = sn.siteNodeId) AS childCount, 0 AS stateId, 2 AS isProtected from cmSiteNode sn ");
	   		SQL.append("where sn.parentSiteNodeId = $1 ");
	   		SQL.append("order by sn.siteNodeId AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");    		
    	}
    	*/
    	
    	StringBuffer SQL = new StringBuffer();
    	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
    	{
	   		SQL.append("CALL SQL select sn.siNoId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiNoId, sn.metaInfoContentId, sn.repositoryId, sn.siNoTypeDefId, sn.creator, (select count(*) from cmSiNo sn2 where sn2.parentSiNoId = sn.siNoId) AS childCount, snv.siNoVerId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, 0 AS languageId, '' as attributes from cmSiNo sn, cmSiNoVer snv ");
	   		SQL.append("where ");
	   		SQL.append("sn.parentSiNoId = $1 ");
	   		SQL.append("AND snv.siNoId = sn.siNoId ");
	   		SQL.append("AND snv.siNoVerId = ( ");
	   		SQL.append("	select max(siNoVerId) from cmSiNoVer snv2 ");
	   		SQL.append("	WHERE ");
	   		SQL.append("	snv2.siNoId = snv.siNoId ");
	   		SQL.append("	) ");
	   		SQL.append("order by sn.siNoId AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");
    	}
    	else
    	{
	   		SQL.append("CALL SQL select sn.siteNodeId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiteNodeId, sn.metaInfoContentId, sn.repositoryId, sn.siteNodeTypeDefinitionId, sn.creator, (select count(*) from cmSiteNode sn2 where sn2.parentSiteNodeId = sn.siteNodeId) AS childCount, snv.siteNodeVersionId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, 0 AS languageId, '' as attributes from cmSiteNode sn, cmSiteNodeVersion snv ");
	   		SQL.append("where ");
	   		SQL.append("sn.parentSiteNodeId = $1 ");
	   		SQL.append("AND snv.siteNodeId = sn.siteNodeId ");
	   		SQL.append("AND snv.siteNodeVersionId = ( ");
	   		SQL.append("	select max(siteNodeVersionId) from cmSiteNodeVersion snv2 ");
	   		SQL.append("	WHERE ");	
	   		SQL.append("	snv2.siteNodeId = snv.siteNodeId");
	   		SQL.append("	) ");
	   		SQL.append("order by sn.siteNodeId AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");    		
    	}
    	
		//OQLQuery oql = db.getOQLQuery( "SELECT s FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl s WHERE s.parentSiteNode = $1 ORDER BY s.siteNodeId");
    	OQLQuery oql = db.getOQLQuery(SQL.toString());
    	oql.bind(parentSiteNodeId);
		
		QueryResults results = oql.execute(Database.ReadOnly);

		while (results.hasMore()) 
		{
			SiteNode siteNode = (SiteNode)results.next();
			//System.out.println("SiteNode:" + siteNode.getValueObject().getModifiedDateTime());
			childrenVOList.add(siteNode.getValueObject());
		}

		results.close();
		oql.close();
        
		CacheController.cacheObjectInAdvancedCache("childSiteNodesCache", key, childrenVOList, new String[]{CacheController.getPooledString(3, parentSiteNodeId)}, true);
        
		return childrenVOList;
	} 
    
    	/**
	 * This method returns a list of the children a siteNode has.
	 */
   	
	public List<SiteNodeVersionVO> getSiteNodeVersionVOList(boolean showDeletedItems, Integer stateId, Integer limit) throws Exception
	{
		List<SiteNodeVersionVO> childrenVOList = new ArrayList<SiteNodeVersionVO>();
        
        Database db = CastorDatabaseService.getDatabase();

	    beginTransaction(db);

        try
        {
        	childrenVOList = getSiteNodeVersionVOList(stateId, limit, db);            
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
   	
	public List<SiteNodeVersionVO> getSiteNodeVersionVOList(Integer stateId, Integer limit, Database db) throws Exception
	{
		List<SiteNodeVersionVO> childrenVOList = new ArrayList<SiteNodeVersionVO>();
		
   		Timer t = new Timer();

   		StringBuffer SQL = new StringBuffer();
    	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
    	{
	   		SQL.append("CALL SQL select * from (select snv.siNoVerId, snv.stateId, snv.verNumber, snv.modifiedDateTime, snv.verComment, snv.isCheckedOut, snv.isActive, snv.isProtected, snv.disablePageCache, snv.disableEditOnSight, snv.disableLanguages, snv.contentType, snv.pageCacheKey, snv.pageCacheTimeout, snv.disableForceIDCheck, snv.forceProtocolChange, snv.siNoId, snv.versionModifier, snv.stateId, snv.isProtected from cmSiNo sn, cmSiNoVer snv ");
	   		SQL.append("where ");
	   		SQL.append("snv.siNoId = sn.siNoId ");
	   		SQL.append("AND snv.siNoVerId = ( ");
	   		SQL.append("	select max(siNoVerId) from cmSiNoVer snv2 ");
	   		SQL.append("	WHERE ");
	   		SQL.append("	snv2.siNoId = snv.siNoId AND ");
	   		SQL.append("	snv2.isActive = $1 AND snv2.stateId >= $2 ");
	   		SQL.append("	) ");
	   		SQL.append("order by sn.siNoId DESC) where ROWNUM <= $3 AS org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl");    		
    	}
    	else
    	{
	   		SQL.append("CALL SQL select snv.siteNodeVersionId, snv.stateId, snv.versionNumber, snv.modifiedDateTime, snv.versionComment, snv.isCheckedOut, snv.isActive, snv.isProtected, snv.disablePageCache, snv.disableEditOnSight, snv.disableLanguages, snv.contentType, snv.pageCacheKey, snv.pageCacheTimeout, snv.disableForceIDCheck, snv.forceProtocolChange, snv.siteNodeId, snv.versionModifier, snv.stateId, snv.isProtected from cmSiteNode sn, cmSiteNodeVersion snv ");
	   		SQL.append("where ");
	   		SQL.append("snv.siteNodeId = sn.siteNodeId ");
	   		SQL.append("AND snv.siteNodeVersionId = ( ");
	   		SQL.append("	select max(siteNodeVersionId) from cmSiteNodeVersion snv2 ");
	   		SQL.append("	WHERE ");
	   		SQL.append("	snv2.siteNodeId = snv.siteNodeId AND ");
	   		SQL.append("	snv2.isActive = $1 AND snv2.stateId >= $2 ");
	   		SQL.append("	) ");
	   		SQL.append("order by sn.siteNodeId DESC LIMIT $3 AS org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl");    		
    	}

    	//logger.info("SQL:" + SQL);
    	//logger.info("parentSiteNodeId:" + parentSiteNodeId);
    	//logger.info("showDeletedItems:" + showDeletedItems);
    	OQLQuery oql = db.getOQLQuery(SQL.toString());
		oql.bind(true);
		oql.bind(stateId);
		oql.bind(limit);

		QueryResults results = oql.execute(Database.ReadOnly);
		t.printElapsedTime("Executed query.....");
		while (results.hasMore()) 
		{
			SiteNodeVersion siteNodeVersion = (SiteNodeVersion)results.next();
			childrenVOList.add(siteNodeVersion.getValueObject());
		
			String key = "" + siteNodeVersion.getValueObject().getSiteNodeId();
			String groupKey1 = CacheController.getPooledString(4, siteNodeVersion.getId());
			String groupKey2 = CacheController.getPooledString(3, siteNodeVersion.getValueObject().getSiteNodeId());

			CacheController.cacheObjectInAdvancedCache("latestSiteNodeVersionCache", key, siteNodeVersion.getValueObject(), new String[]{groupKey1.toString(), groupKey2.toString()}, true);
		}
		
		results.close();
		oql.close();

   		t.printElapsedTime("getSiteNodeVersionVOList " + childrenVOList.size() + " took");
   		
		return childrenVOList;
	}
	
	/**
	 * This method returns a list of the children a siteNode has.
	 */
   	
	public List<SiteNodeVO> getSiteNodeVOList(boolean showDeletedItems, Integer stateId, Integer limit) throws Exception
	{
		List<SiteNodeVO> childrenVOList = new ArrayList<SiteNodeVO>();
        
        Database db = CastorDatabaseService.getDatabase();

	    beginTransaction(db);

        try
        {
        	childrenVOList = getSiteNodeVOList(stateId, limit, db);            
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
        
        return childrenVOList;
	}
	
	
	/**
	 * This method returns a list of the children a siteNode has.
	 */
   	
	public List<SiteNodeVO> getSiteNodeVOList(Integer stateId, Integer limit, Database db) throws Exception
	{
		List<SiteNodeVO> childrenVOList = new ArrayList<SiteNodeVO>();
		
   		StringBuffer SQL = new StringBuffer();
   		if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
    	{
	   		SQL.append("CALL SQL select * from (select sn.siNoId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiNoId, sn.metaInfoContentId, sn.repositoryId, sn.siNoTypeDefId, sn.creator, (select count(*) from cmSiNo sn2 where sn2.parentSiNoId = sn.siNoId) AS childCount, snv.siNoVerId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, 0 AS languageId, '' as attributes from cmSiNo sn, cmSiNoVer snv ");
	   		SQL.append("where ");
	   		SQL.append("snv.siNoId = sn.siNoId ");
	   		SQL.append("AND snv.siNoVerId = ( ");
	   		SQL.append("	select max(siNoVerId) from cmSiNoVer snv2 ");
	   		SQL.append("	WHERE ");
	   		SQL.append("	snv2.siNoId = snv.siNoId AND ");
	   		SQL.append("	snv2.isActive = $1 AND snv2.stateId >= $2 ");
	   		SQL.append("	) ");
	   		SQL.append("order by sn.parentSiNoId ASC, snv.sortOrder ASC, sn.name ASC, sn.siNoId DESC) where ROWNUM <= $3 AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");
    	}
    	else
    	{
	   		SQL.append("CALL SQL select sn.siteNodeId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiteNodeId, sn.metaInfoContentId, sn.repositoryId, sn.siteNodeTypeDefinitionId, sn.creator, (select count(*) from cmSiteNode sn2 where sn2.parentSiteNodeId = sn.siteNodeId) AS childCount, snv.siteNodeVersionId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, 0 AS languageId, '' as attributes from cmSiteNode sn, cmSiteNodeVersion snv ");
	   		SQL.append("where ");
	   		SQL.append("snv.siteNodeId = sn.siteNodeId ");
	   		SQL.append("AND snv.siteNodeVersionId = ( ");
	   		SQL.append("	select max(siteNodeVersionId) from cmSiteNodeVersion snv3 ");
	   		SQL.append("	WHERE ");
	   		SQL.append("	snv3.siteNodeId = snv.siteNodeId AND ");
	   		SQL.append("	snv3.isActive = $1 AND snv3.stateId >= $2 ");
	   		SQL.append("	) ");
	   		SQL.append("order by sn.parentSiteNodeId ASC, snv.sortOrder ASC, sn.name ASC, sn.siteNodeId DESC LIMIT $3 AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");    		
    	}
   		logger.info("SQL:" + SQL);
    	//logger.info("parentSiteNodeId:" + parentSiteNodeId);
    	//logger.info("showDeletedItems:" + showDeletedItems);
    	OQLQuery oql = db.getOQLQuery(SQL.toString());
		oql.bind(true);
		oql.bind(stateId);
		oql.bind(limit);

		QueryResults results = oql.execute(Database.ReadOnly);
		//t.printElapsedTime("Executed query.....");
		
		int lastBegunParentSiteNodeId = -1;
		while (results.hasMore()) 
		{
			SiteNode siteNode = (SiteNode)results.next();
			childrenVOList.add(siteNode.getValueObject());
			
			if(siteNode.getValueObject().getParentSiteNodeId() !=null && lastBegunParentSiteNodeId != siteNode.getValueObject().getParentSiteNodeId())
				lastBegunParentSiteNodeId = siteNode.getValueObject().getParentSiteNodeId();
			
			String key = "" + siteNode.getValueObject().getParentSiteNodeId();
			List<SiteNodeVO> siteNodeChildrenVOList = (List<SiteNodeVO>)CacheController.getCachedObjectFromAdvancedCache("childSiteNodesCache", key);
	   		if(siteNodeChildrenVOList == null)
			{
		   		siteNodeChildrenVOList = new ArrayList<SiteNodeVO>();
				CacheController.cacheObjectInAdvancedCache("childSiteNodesCache", key, siteNodeChildrenVOList, new String[] {CacheController.getPooledString(3, siteNode.getValueObject().getParentSiteNodeId())}, true);
			}
	   		boolean contains = false;
	   		for(SiteNodeVO existingSiteNodeVO : siteNodeChildrenVOList)
	   		{
	   			if(existingSiteNodeVO.getId().equals(siteNode.getValueObject().getId()))
	   			{
	   				contains = true;
	   				break;
	   			}
	   		}
	   		if(!contains)
	   		{
	   			siteNodeChildrenVOList.add(siteNode.getValueObject());
				String siteNodeCacheKey = "" + siteNode.getValueObject().getId();
				CacheController.cacheObjectInAdvancedCache("siteNodeCache", siteNodeCacheKey, siteNode.getValueObject());
	   		}
		}

		logger.info("Clearing last node as we are probably not done with all it's children");
		CacheController.clearCacheForGroup("childSiteNodesCache", CacheController.getPooledString(3, lastBegunParentSiteNodeId));
		CacheController.clearCache("siteNodeCache", CacheController.getPooledString(3, lastBegunParentSiteNodeId));
		
		results.close();
		oql.close();
   		
		return childrenVOList;
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
			logger.error("An error occurred so we should not complete the transaction: " + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction: " + e.getMessage(), e);
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
	   		SQL.append("CALL SQL select sn.siNoId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiNoId, sn.metaInfoContentId, sn.repositoryId, sn.siNoTypeDefId, sn.creator, (select count(*) from cmSiNo sn2 where sn2.parentSiNoId = sn.siNoId) AS childCount, snv.siNoVerId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, 0 AS languageId, '' as attributes from cmSiNo sn, cmSiNoVer snv ");
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
	   		SQL.append("CALL SQL select sn.siteNodeId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiteNodeId, sn.metaInfoContentId, sn.repositoryId, sn.siteNodeTypeDefinitionId, sn.creator, (select count(*) from cmSiteNode sn2 where sn2.parentSiteNodeId = sn.siteNodeId) AS childCount, snv.siteNodeVersionId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, 0 AS languageId, '' as attributes from cmSiteNode sn, cmSiteNodeVersion snv ");
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

    	//logger.info("SQL 2:" + SQL);
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
			//logger.info("Name:" + siteNode.getName() + ":" + siteNode.getValueObject().getStateId() + ":" + siteNode.getValueObject().getIsProtected() + ":" + siteNode.getValueObject().getIsProtected());
			childrenVOList.add(siteNode.getValueObject());
		}
		
		results.close();
		oql.close();
        
		CacheController.cacheObjectInAdvancedCache("childSiteNodesCache", key, childrenVOList, new String[]{CacheController.getPooledString(3, parentSiteNodeId)}, true);
        
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
	
    public List getSiteNodeVOList(HashMap argumentHashMap, Database db) throws SystemException, Bug, Exception
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
				siteNodes.add(getSiteNodeVOWithId(siteNodeId, db));
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
    		else
    			logger.error("repositoryId:" + repositoryId + " had no root");

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
            
            /*
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
            */
            
            changeRepositoryRecursive(siteNode, newParentSiteNode.getRepository(), principal, db);
            //siteNode.setRepository(newParentSiteNode.getRepository());
			newParentSiteNode.getChildSiteNodes().add(siteNode);
			oldParentSiteNode.getChildSiteNodes().remove(siteNode);

			try
			{
				SiteNodeVersionVO publishedVersion = SiteNodeVersionController.getController().getLatestPublishedSiteNodeVersionVO(siteNode.getId(), db);
				SiteNodeVersionVO publishedVersionNewParent = SiteNodeVersionController.getController().getLatestPublishedSiteNodeVersionVO(newParentSiteNode.getId(), db);
				SiteNodeVersionVO publishedVersionOldParent = SiteNodeVersionController.getController().getLatestPublishedSiteNodeVersionVO(oldParentSiteNode.getId(), db);
				List<EventVO> events = new ArrayList<EventVO>();

				logger.info("publishedVersion:" + publishedVersion);
				if(publishedVersion != null)
	            {
		            EventVO eventVO = new EventVO();
					eventVO.setDescription("Moved page");
					eventVO.setEntityClass(SiteNodeVersion.class.getName());
					eventVO.setEntityId(publishedVersion.getId());
					eventVO.setName(siteNode.getName());
					eventVO.setTypeId(EventVO.MOVED);
					eventVO = EventController.create(eventVO, newParentSiteNode.getRepository().getId(), principal);
					events.add(eventVO);
	            }
				
				logger.info("publishedVersionOldParent:" + publishedVersionOldParent);
				if(publishedVersionNewParent != null)
	            {
					EventVO eventVO = new EventVO();
					eventVO.setDescription("New parent page");
					eventVO.setEntityClass(SiteNodeVersion.class.getName());
					eventVO.setEntityId(publishedVersionNewParent.getId());
					eventVO.setName(newParentSiteNode.getName());
					eventVO.setTypeId(EventVO.PUBLISH);
					eventVO = EventController.create(eventVO, newParentSiteNode.getRepository().getId(), principal);
					events.add(eventVO);
	            }
				
				logger.info("publishedVersionOldParent:" + publishedVersionOldParent);
				if(publishedVersionOldParent != null)
	            {
					EventVO eventVO = new EventVO();
					eventVO.setDescription("Move page");
					eventVO.setEntityClass(SiteNodeVersion.class.getName());
					eventVO.setEntityId(publishedVersionOldParent.getId());
					eventVO.setName(oldParentSiteNode.getName());
					eventVO.setTypeId(EventVO.PUBLISH);
					eventVO = EventController.create(eventVO, oldParentSiteNode.getRepository().getId(), principal);
					events.add(eventVO);
	            }

				/*
				EventVO eventVO = new EventVO();
				eventVO.setDescription("Moved page");
				eventVO.setEntityClass(SiteNodeVersion.class.getName());
				eventVO.setEntityId(publishedVersion.getId());
				eventVO.setName(siteNode.getName());
				eventVO.setTypeId(EventVO.MOVED);
				eventVO = EventController.create(eventVO, siteNode.getRepository().getId(), principal);
				events.add(eventVO);
				*/
				
				logger.info("events:" + events.size());
				if(events != null && events.size() > 0)
				{
					//Create publication if nodes has published version
					PublicationVO publicationVO = new PublicationVO();
					publicationVO.setName("Move of page - auto publication");
					publicationVO.setDescription("System did an automatic publication");
					publicationVO.setPublisher(principal.getName());
					publicationVO.setRepositoryId(newParentSiteNode.getRepository().getId());
					
					publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, false, principal);
	            }
			}
			catch (Exception e) 
			{
				logger.error("Error publishing move:" + e.getMessage(), e);
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
			logger.error("An error occurred so we should not complete the transaction: " + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction: " + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }           
    
	/**
	 * Recursively sets the sitenodes repositoryId.
	 * @param sitenode
	 * @param newRepository
	 */

	private void changeRepositoryRecursive(SiteNode siteNode, Repository newRepository, InfoGluePrincipal principal, Database db) throws Exception
	{
	    if(siteNode.getRepository().getId().intValue() != newRepository.getId().intValue())
	    {
	        Integer metaInfoContentId = siteNode.getMetaInfoContentId();
        	ContentVO metaInfoContent = ContentController.getContentController().getContentVOWithId(metaInfoContentId, db);
        	//String previousPath = ContentController.getContentController().getContentPath(metaInfoContentId, db);
        	
        	String siteNodePath = SiteNodeController.getController().getSiteNodePath(siteNode.getId(), db);
        	logger.info("siteNodePath:" + siteNodePath);
        	if(siteNodePath.indexOf("/", 2) > -1)
        		siteNodePath = siteNodePath.substring(siteNodePath.indexOf("/", 2));
        	logger.info("siteNodePath:" + siteNodePath);
        	
        	ContentVO newParentContent = ContentController.getContentController().getContentVOWithPath(newRepository.getId(), "Meta info folder" + siteNodePath, true, principal, db);
        	if(metaInfoContent != null && newParentContent != null)
        	{
        		logger.info("Moving:" + metaInfoContent.getName() + " to " + newParentContent.getName());
        		//newParentContent.getChildren().add(metaInfoContent);
        		//Content previousParentContent = metaInfoContent.getParentContent();
        		metaInfoContent.setParentContentId(newParentContent.getId());
        		//previousParentContent.getChildren().remove(metaInfoContent);
        		
        		LanguageVO oldMasterLanguage = LanguageController.getController().getMasterLanguage(metaInfoContent.getRepositoryId(), db);
        		LanguageVO newMasterLanguage = LanguageController.getController().getMasterLanguage(newParentContent.getRepositoryId(), db);
        		
        		ContentVersionVO oldMasterContentVersionVO = ContentVersionController.getContentVersionController().getLatestContentVersionVO(metaInfoContent.getId(), oldMasterLanguage.getId(), db);
        		ContentVersionVO newMasterContentVersionVO = ContentVersionController.getContentVersionController().getLatestContentVersionVO(metaInfoContent.getId(), newMasterLanguage.getId(), db);
        		if(oldMasterContentVersionVO != null && newMasterContentVersionVO == null)
        		{
        			ContentVersionController.getContentVersionController().create(metaInfoContentId, newMasterLanguage.getId(), oldMasterContentVersionVO, null, db);
        		}
        		else if(oldMasterContentVersionVO != null && newMasterContentVersionVO != null)
        		{
        			String oldComponentStructure = ContentVersionController.getContentVersionController().getAttributeValue(oldMasterContentVersionVO, "ComponentStructure", false);
        			String newComponentStructure = ContentVersionController.getContentVersionController().getAttributeValue(newMasterContentVersionVO, "ComponentStructure", false);
        			if(oldComponentStructure != null && !oldComponentStructure.equals("") && (newComponentStructure == null || newComponentStructure.equals("")))
        			{
        				ContentVersionController.getContentVersionController().updateAttributeValue(newMasterContentVersionVO.getId(), "ComponentStructure", oldComponentStructure, principal, true);
        			}
        		}
        		metaInfoContent.setRepositoryId(newParentContent.getRepositoryId());
        		//changeRepositoryRecursiveForContent(metaInfoContent, newRepository);
        	}

	        siteNode.setRepository((RepositoryImpl)newRepository);
		    Iterator ChildSiteNodesIterator = siteNode.getChildSiteNodes().iterator();
		    while(ChildSiteNodesIterator.hasNext())
		    {
		        SiteNode childSiteNode = (SiteNode)ChildSiteNodesIterator.next();
		        changeRepositoryRecursive(childSiteNode, newRepository, principal, db);
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
		
		OQLQuery oql = db.getOQLQuery("SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl sn WHERE sn.repository.repositoryId = $1 ORDER BY sn.siteNodeId");
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
	
    public MediumContentImpl createSiteNodeMetaInfoContent(Database db, SiteNodeVO newSiteNode, Integer repositoryId, InfoGluePrincipal principal, Integer pageTemplateContentId, List<ContentVersion> newContentVersions) throws SystemException, Bug, Exception, ConstraintException
    {
    	Timer t = new Timer();
    	MediumContentImpl content = null;
        
        String basePath = "Meta info folder";
        String path = "";
        
        //SiteNode parentSiteNode = newSiteNode.getParentSiteNode();
        Integer parentSiteNodeId = newSiteNode.getParentSiteNodeId();
        while(parentSiteNodeId != null)
        {
        	SiteNodeVO parentSiteNode = getSmallSiteNodeVOWithId(parentSiteNodeId, db);
            path = "/" + parentSiteNode.getName() + path;
            parentSiteNodeId = parentSiteNode.getParentSiteNodeId();
            //parentSiteNode = parentSiteNode.getParentSiteNode();
        }
        path = basePath + path;
        
        RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Getting path", t.getElapsedTime());
        
        SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(db, newSiteNode.getId());
        LanguageVO masterLanguage 			= LanguageController.getController().getMasterLanguage(repositoryId, db);
  	   
        RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getLatestSiteNodeVersionVO", t.getElapsedTime());
        
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
        RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getAvailableServiceBindingVOWithName", t.getElapsedTime());

        ContentVO rootContent = ContentControllerProxy.getController().getRootContentVO(db, repositoryId, principal.getName(), true);
        RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getRootContentVO", t.getElapsedTime());

        if(rootContent != null)
        {
            ContentVO parentFolderContent = ContentController.getContentController().getContentVOWithPath(repositoryId, path, true, principal, db);
            RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getContentVOWithPath", t.getElapsedTime());
            
        	ContentVO contentVO = new ContentVO();
        	contentVO.setCreatorName(principal.getName());
        	contentVO.setIsBranch(new Boolean(false));
        	contentVO.setName(newSiteNode.getName() + " Metainfo");
        	contentVO.setRepositoryId(repositoryId);

        	content = ContentControllerProxy.getController().create(db, parentFolderContent.getId(), metaInfoContentTypeDefinitionId, repositoryId, contentVO);
            RequestAnalyser.getRequestAnalyser().registerComponentStatistics("content.create", t.getElapsedTime());
        	
        	newSiteNode.setMetaInfoContentId(contentVO.getId());
        	
        	String componentStructure = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><components></components>";
        	if(pageTemplateContentId != null)
        	{
        	    Integer masterLanguageId = LanguageController.getController().getMasterLanguage(repositoryId, db).getId();
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
            	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("meta info create 1", t.getElapsedTime());
        	}
        	
        	//Create initial content version also... in masterlanguage
        	String versionValue = "<?xml version='1.0' encoding='UTF-8'?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes><Title><![CDATA[" + newSiteNode.getName() + "]]></Title><NavigationTitle><![CDATA[" + newSiteNode.getName() + "]]></NavigationTitle><NiceURIName><![CDATA[" + new VisualFormatter().replaceNiceURINonAsciiWithSpecifiedChars(newSiteNode.getName(), CmsPropertyHandler.getNiceURIDefaultReplacementCharacter()) + "]]></NiceURIName><Description><![CDATA[" + newSiteNode.getName() + "]]></Description><MetaInfo><![CDATA[" + newSiteNode.getName() + "]]></MetaInfo><ComponentStructure><![CDATA[" + componentStructure + "]]></ComponentStructure></attributes></article>";
        	ContentVersionVO contentVersionVO = new ContentVersionVO();
        	contentVersionVO.setVersionComment("Autogenerated version");
        	contentVersionVO.setVersionModifier(principal.getName());
        	contentVersionVO.setVersionValue(versionValue);
        	//ContentVersionController.getContentVersionController().create(contentVO.getId(), masterLanguage.getId(), contentVersionVO, null, db);
        	MediumContentVersionImpl contentVersionImpl = ContentVersionController.getContentVersionController().createMedium(contentVO.getId(), masterLanguage.getId(), contentVersionVO, db);
        	if(newContentVersions != null)
        		newContentVersions.add(contentVersionImpl);

        	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("meta info create 2", t.getElapsedTime());

        	//Also created a version in the local master language for this part of the site if any
        	LanguageVO localMasterLanguageVO = getInitialLanguageVO(db, parentFolderContent.getId(), repositoryId);
        	if(localMasterLanguageVO.getId().intValue() != masterLanguage.getId().intValue())
        	{
	        	String versionValueLocalMaster = "<?xml version='1.0' encoding='UTF-8'?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes><Title><![CDATA[" + newSiteNode.getName() + "]]></Title><NavigationTitle><![CDATA[" + newSiteNode.getName() + "]]></NavigationTitle><NiceURIName><![CDATA[" + new VisualFormatter().replaceNiceURINonAsciiWithSpecifiedChars(newSiteNode.getName(), CmsPropertyHandler.getNiceURIDefaultReplacementCharacter()) + "]]></NiceURIName><Description><![CDATA[" + newSiteNode.getName() + "]]></Description><MetaInfo><![CDATA[" + newSiteNode.getName() + "]]></MetaInfo><ComponentStructure><![CDATA[]]></ComponentStructure></attributes></article>";
	            ContentVersionVO contentVersionVOLocalMaster = new ContentVersionVO();
	        	contentVersionVOLocalMaster.setVersionComment("Autogenerated version");
	        	contentVersionVOLocalMaster.setVersionModifier(principal.getName());
	        	contentVersionVOLocalMaster.setVersionValue(versionValueLocalMaster);
	        	//ContentVersionController.getContentVersionController().create(contentVO.getId(), localMasterLanguageVO.getId(), contentVersionVOLocalMaster, null, db);
	        	MediumContentVersionImpl contentVersionImplLocal = ContentVersionController.getContentVersionController().createMedium(contentVO.getId(), localMasterLanguageVO.getId(), contentVersionVOLocalMaster, db);
	        	if(newContentVersions != null)
	        		newContentVersions.add(contentVersionImplLocal);
	        	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("meta info create 3", t.getElapsedTime());
        	}
        	
        	ServiceBindingVO serviceBindingVO = new ServiceBindingVO();
        	serviceBindingVO.setName(newSiteNode.getName() + " Metainfo");
        	serviceBindingVO.setPath("/None specified/");
        
        	String qualifyerXML = "<?xml version='1.0' encoding='UTF-8'?><qualifyer><contentId>" + contentVO.getId() + "</contentId></qualifyer>";
        
        	//ServiceBindingController.getController().create(db, serviceBindingVO, qualifyerXML, availableServiceBindingId, siteNodeVersionVO.getId(), singleServiceDefinitionVO.getId());	
        	//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("meta info service bind", t.getElapsedTime());
        }

        return content;
    }

	public LanguageVO getInitialLanguageVO(Database db, Integer contentId, Integer repositoryId) throws Exception
	{
		Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);

	    String initialLanguageId = ps.getString("content_" + contentId + "_initialLanguageId");
	    ContentVO content = ContentController.getContentController().getContentVOWithId(contentId, db);
	    ContentVO parentContent = ContentController.getContentController().getContentVOWithId(content.getParentContentId(), db); 
	    while((initialLanguageId == null || initialLanguageId.equalsIgnoreCase("-1")) && parentContent != null)
	    {
	        initialLanguageId = ps.getString("content_" + parentContent.getId() + "_initialLanguageId");
	        if(parentContent.getParentContentId() != null)
	        	parentContent = ContentController.getContentController().getContentVOWithId(parentContent.getParentContentId(), db); 
	        else
	        	parentContent = null;
	        //parentContent = parentContent.getParentContent(); 
	    }
	    
	    if(initialLanguageId != null && !initialLanguageId.equals("") && !initialLanguageId.equals("-1"))
	        return LanguageController.getController().getLanguageVOWithId(new Integer(initialLanguageId), db);
	    else
	        return LanguageController.getController().getMasterLanguage(repositoryId, db);
	}

	/**
	 * Recursive methods to get all sitenodes under the specific sitenode.
	 */ 
	
    public List getSiteNodeVOWithParentRecursive(Integer siteNodeId, ProcessBean processBean) throws ConstraintException, SystemException
	{
		return getSiteNodeVOWithParentRecursive(siteNodeId, processBean, new ArrayList());
	}
	
	private List getSiteNodeVOWithParentRecursive(Integer siteNodeId, ProcessBean processBean, List resultList) throws ConstraintException, SystemException
	{
		// Get the versions of this content.
		resultList.add(getSiteNodeVOWithId(siteNodeId));
		
		// Get the children of this content and do the recursion
		List childSiteNodeList = SiteNodeController.getController().getSiteNodeChildrenVOList(siteNodeId);
		Iterator cit = childSiteNodeList.iterator();
		while (cit.hasNext())
		{
		    SiteNodeVO siteNodeVO = (SiteNodeVO) cit.next();
		    
		    if (resultList.size() % 50 == 0)
		    	processBean.updateProcess("Found " + resultList.size() + " pages so far...");
			getSiteNodeVOWithParentRecursive(siteNodeVO.getId(), processBean, resultList);
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
			logger.error("An error occurred so we should not complete the transaction: " + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction: " + e.getMessage(), e);
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
			logger.error("An error occurred so we should not complete the transaction: " + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction: " + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
        return siteNodeVOList;
    }       

    public List getSiteNodesWithoutMetaInfoContentId(Database db) throws ConstraintException, SystemException, Exception
    {
		List siteNodes = new ArrayList();

		OQLQuery oql = db.getOQLQuery("SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl sn WHERE sn.metaInfoContentId = $1 ORDER BY sn.siteNodeId");
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


    public List getSiteNodeVOListWithMetaInfoContentId() throws ConstraintException, SystemException
    {
		List siteNodeVOList = new ArrayList();

		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
            List siteNodes = getSiteNodeVOListWithMetaInfoContentId(db);
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

    public List getSiteNodeVOListWithMetaInfoContentId(Database db) throws ConstraintException, SystemException, Exception
    {
		List siteNodes = new ArrayList();

		OQLQuery oql = db.getOQLQuery("SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl sn WHERE sn.metaInfoContentId > $1 ORDER BY sn.siteNodeId");
    	oql.bind(new Integer(0));
    	
    	QueryResults results = oql.execute();
		
		while(results.hasMore()) 
        {
        	SiteNode siteNode = (SiteNode)results.next();
        	siteNodes.add(siteNode);
        }

		results.close();
		oql.close();

		return siteNodes;
    }

    public SiteNodeVO getSiteNodeVOWithMetaInfoContentId(Integer contentId) throws ConstraintException, SystemException
    {
		SiteNodeVO siteNodeVO = null;

		Integer cachedSiteNodeId = metaInfoSiteNodeIdMap.get(contentId);
		if(cachedSiteNodeId != null)
		{
			siteNodeVO = getSiteNodeVOWithId(cachedSiteNodeId, false);
		}
		else
		{
			Database db = CastorDatabaseService.getDatabase();
	        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
	
	        beginTransaction(db);
	
	        try
	        {
	        	siteNodeVO = getSiteNodeVOWithMetaInfoContentId(db, contentId);
	        	
	            commitTransaction(db);
	        }
	        catch(Exception e)
	        {
				logger.error("An error occurred so we should not complete the transaction: " + e.getMessage());
				logger.warn("An error occurred so we should not complete the transaction: " + e.getMessage(), e);
	            rollbackTransaction(db);
	            throw new SystemException(e.getMessage());
	        }
		}
		
        return siteNodeVO;
    }       

    private static Map<Integer,Integer> metaInfoSiteNodeIdMap = new HashMap<Integer,Integer>();
    
    public SiteNodeVO getSiteNodeVOWithMetaInfoContentId(Database db, Integer contentId) throws ConstraintException, SystemException, Exception
    {
		SiteNodeVO siteNodeVO = null;
		
		Integer cachedSiteNodeId = metaInfoSiteNodeIdMap.get(contentId);
		if(cachedSiteNodeId != null)
		{
			siteNodeVO = getSiteNodeVOWithId(cachedSiteNodeId, db);
		}
		else
		{
			logger.error("Asking for heavy lookup on meta info content id:" + contentId);
			//System.out.println("Asking for mapping:" + contentId);
			OQLQuery oql = db.getOQLQuery("SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl sn WHERE sn.metaInfoContentId = $1 ORDER BY sn.siteNodeId");
	    	oql.bind(contentId);
	    	
	    	QueryResults results = oql.execute(Database.ReadOnly);
			
	    	if(results.hasMore()) 
	        {
				SmallSiteNodeImpl siteNode = (SmallSiteNodeImpl)results.next();
				siteNodeVO = siteNode.getValueObject();
				logger.info("Caching " + siteNodeVO.getId() + " on " + contentId);
				metaInfoSiteNodeIdMap.put(contentId, siteNodeVO.getId());
	        }
	
			results.close();
			oql.close();
		}
		
		return siteNodeVO;
    }
    /*
    public SiteNode getSiteNodeWithMetaInfoContentId(Database db, Integer contentId) throws ConstraintException, SystemException, Exception
    {
		SiteNode siteNode = null;

		OQLQuery oql = db.getOQLQuery("SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl sn WHERE sn.metaInfoContentId = $1 ORDER BY sn.siteNodeId");
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
    */
    
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
				else if(siteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK.intValue())
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

	/**
	 * This method returns true if the if the siteNode in question is protected.
	 */
    
	public Integer getProtectedSiteNodeVersionId(Integer siteNodeId, Database db)
	{
		Integer protectedSiteNodeVersionId = null;
	
		try
		{
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(db, siteNodeId);

			if(siteNodeVersionVO.getIsProtected() != null)
			{	
				if(siteNodeVersionVO.getIsProtected().intValue() == NO.intValue())
					protectedSiteNodeVersionId = null;
				else if(siteNodeVersionVO.getIsProtected().intValue() == YES.intValue())
					protectedSiteNodeVersionId = siteNodeVersionVO.getId();
				else if(siteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK.intValue())
					protectedSiteNodeVersionId = siteNodeVersionVO.getId();
				else if(siteNodeVersionVO.getIsProtected().intValue() == INHERITED.intValue())
				{
					SiteNodeVO parentSiteNodeVO = getParentSiteNodeVO(siteNodeId, db);
					if(parentSiteNodeVO != null)
						protectedSiteNodeVersionId = getProtectedSiteNodeVersionId(parentSiteNodeVO.getId(), db); 
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


	/**
	 * This method are here to return all content versions that are x number of versions behind the current active version. This is for cleanup purposes.
	 * 
	 * @param numberOfVersionsToKeep
	 * @return
	 * @throws SystemException 
	 */
	
	public int cleanSiteNodeVersions(int numberOfVersionsToKeep, boolean keepOnlyOldPublishedVersions, long minimumTimeBetweenVersionsDuringClean, boolean deleteVersions) throws SystemException, Exception 
	{
		if(numberOfVersionsToKeep > 10)
			numberOfVersionsToKeep = 10;
		
		int cleanedVersions = 0;
		
		int batchLimit = 20;

		Map<Integer,Integer> siteNodeIdMap = getSiteNodeIdVersionCountMap(numberOfVersionsToKeep);
		if(!deleteVersions)
		{
			for(Integer siteNodeId : siteNodeIdMap.keySet())
			{
				Integer versionCount = siteNodeIdMap.get(siteNodeId);
				int additions = versionCount - numberOfVersionsToKeep;
				//System.out.println("additions " + siteNodeId + ": " + additions + "/" + versionCount + "/" + numberOfVersionsToKeep);

				cleanedVersions = cleanedVersions + additions;
			}
		}
		else
		{
			List<SiteNodeVersionVO> siteNodeVersionVOList = getSiteNodeVersionVOList(numberOfVersionsToKeep, keepOnlyOldPublishedVersions, minimumTimeBetweenVersionsDuringClean);
				
			//System.out.println("Deleting " + siteNodeVersionVOList.size() + " versions");
			int maxIndex = (siteNodeVersionVOList.size() > batchLimit ? batchLimit : siteNodeVersionVOList.size());
			List partList = siteNodeVersionVOList.subList(0, maxIndex);
			while(partList.size() > 0)
			{
				if(deleteVersions)
					cleanVersions(numberOfVersionsToKeep, partList);
				cleanedVersions = cleanedVersions + partList.size();
				logger.info("Cleaned " + cleanedVersions + " of " + siteNodeVersionVOList.size());
				partList.clear();
				maxIndex = (siteNodeVersionVOList.size() > batchLimit ? batchLimit : siteNodeVersionVOList.size());
				partList = siteNodeVersionVOList.subList(0, maxIndex);
			}
		}
		
		return cleanedVersions;
	}
	

	public Map<Integer,Integer> getSiteNodeIdVersionCountMap(int numberOfVersionsToKeep) throws SystemException 
	{
		Map<Integer,Integer> result = new HashMap<Integer,Integer>();
		/*
		if(true)
		{
			result.put(582178, 10);
			result.put(625088, 15);
			result.put(582177, 9);
			return result;
		}
		*/

		Database db = CastorDatabaseService.getDatabase();
    	
		beginTransaction(db);

        try
        {
			StringBuilder sql = new StringBuilder();
			if(CmsPropertyHandler.getUseShortTableNames().equals("true"))
				sql.append("select siNoId as siteNodeId, versionCount from ( select snv.siNoId, count(*) as versionCount from cmSiNoVer snv group by snv.siNoId order by versionCount desc ) res where versionCount > " + numberOfVersionsToKeep);
			else
				sql.append("select siteNodeId, versionCount from ( select snv.siteNodeId, count(*) as versionCount from cmSiteNodeVersion snv group by snv.siteNodeId order by versionCount desc ) res where versionCount > " + numberOfVersionsToKeep);

			Connection conn = (Connection) db.getJdbcConnection();
			
			PreparedStatement psmt = conn.prepareStatement(sql.toString());

			int totalVersions = 0;
			ResultSet rs = psmt.executeQuery();
			while(rs.next())
			{
				Integer siteNodeId = new Integer(rs.getString("siteNodeId"));
				Integer count = new Integer(rs.getString("versionCount"));
				totalVersions = totalVersions + (count-numberOfVersionsToKeep);
				result.put(siteNodeId, count);
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
	
	public List<SiteNodeVersionVO> getSiteNodeVersionVOList(int numberOfVersionsToKeep, boolean keepOnlyOldPublishedVersions, long minimumTimeBetweenVersionsDuringClean) throws SystemException, Bug, Exception
	{
		Map<Integer,Integer> siteNodeIdMap = getSiteNodeIdVersionCountMap(numberOfVersionsToKeep);
		//System.out.println("contentVOList:" + contentVOList.size());

		List<SiteNodeVersionVO> siteNodeVersionVOList = new ArrayList<SiteNodeVersionVO>();
		if(siteNodeIdMap == null || siteNodeIdMap.size() == 0)
			return siteNodeVersionVOList;
		
		List<Integer> siteNodeIdList = new ArrayList<Integer>();
		siteNodeIdList.addAll(siteNodeIdMap.keySet());
		
    	int slotSize = 500;
    	while(siteNodeIdList.size() > 0)
    	{
    		List<Integer> subList = new ArrayList<Integer>();
    		if(siteNodeIdList.size() > slotSize)
    		{
    			subList = siteNodeIdList.subList(0, slotSize);
    			siteNodeIdList = siteNodeIdList.subList(slotSize, siteNodeIdList.size()-1);
    		}
    		else
    		{
    			subList.addAll(siteNodeIdList);
    			siteNodeIdList.clear();
    		}
    		
    		if(subList.size() > 0)
	    	{
	    		siteNodeVersionVOList.addAll(getSiteNodeVersionVOListImpl(subList, numberOfVersionsToKeep, keepOnlyOldPublishedVersions, minimumTimeBetweenVersionsDuringClean));
	    	}
    	}
		
		return siteNodeVersionVOList;
	}

	/**
	 * This method are here to return all content versions that are x number of versions behind the current active version. This is for cleanup purposes.
	 * 
	 * @param numberOfVersionsToKeep
	 * @return
	 * @throws SystemException 
	 */
	
	public List<SiteNodeVersionVO> getSiteNodeVersionVOListImpl(List<Integer> siteNodeIdList, int numberOfVersionsToKeep, boolean keepOnlyOldPublishedVersions, long minimumTimeBetweenVersionsDuringClean) throws SystemException 
	{
		logger.info("numberOfVersionsToKeep:" + numberOfVersionsToKeep);

		Database db = CastorDatabaseService.getDatabase();
    	
    	List<SiteNodeVersionVO> siteNodeVersionsIdList = new ArrayList();

        beginTransaction(db);

        try
        {
    		StringBuilder variables = new StringBuilder();
    	    for(int i=0; i<siteNodeIdList.size(); i++)
    	    	variables.append("$" + (i+1) + (i+1!=siteNodeIdList.size() ? "," : ""));

            OQLQuery oql = db.getOQLQuery("SELECT snv FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl snv WHERE snv.siteNodeId IN LIST (" + variables + ") ORDER BY snv.siteNodeId, snv.siteNodeVersionId desc");
    		for(Integer siteNodeId : siteNodeIdList)
    			oql.bind(siteNodeId);

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
			logger.error("An error occurred so we should not complete the transaction: " + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction: " + e.getMessage(), e);
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
				//SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(siteNodeVersionVO.getId(), db);
				MediumSiteNodeVersionImpl siteNodeVersion = SiteNodeVersionController.getController().getMediumSiteNodeVersionWithId(siteNodeVersionVO.getId(), db);
				logger.info("Deleting the siteNodeVersion " + siteNodeVersion.getId() + " on siteNode " + siteNodeVersion.getSiteNodeId());
				delete(siteNodeVersion, db, true);
			}

			commitTransaction(db);

			Thread.sleep(1000);
        }
        catch(Exception e)
        {
			logger.error("An error occurred so we should not complete the transaction: " + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction: " + e.getMessage(), e);
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
	    SiteNodeStateController.getController().deleteAccessRights("SiteNodeVersion", siteNodeVersion.getId(), db);
	    
		SiteNode siteNode = siteNodeVersion.getOwningSiteNode();

		if(siteNode != null)
			siteNode.getSiteNodeVersions().remove(siteNodeVersion);

		db.remove(siteNodeVersion);
	}

 	public void delete(MediumSiteNodeVersionImpl siteNodeVersion, Database db, boolean forceDelete) throws ConstraintException, SystemException, Exception
	{
		if (!forceDelete && siteNodeVersion.getStateId().intValue() == ContentVersionVO.PUBLISHED_STATE.intValue() && siteNodeVersion.getIsActive().booleanValue() == true)
		{
			SiteNodeVO snVO = getSiteNodeVOWithId(siteNodeVersion.getSiteNodeId(), db);
			throw new ConstraintException("SiteNodeVersion.stateId", "3300", snVO.getName());
		}
		
		Collection serviceBindnings = siteNodeVersion.getServiceBindings();
		Iterator serviceBindningsIterator = serviceBindnings.iterator();
		while(serviceBindningsIterator.hasNext())
		{
			SmallServiceBindingImpl serviceBinding = (SmallServiceBindingImpl)serviceBindningsIterator.next();
			//logger.info("serviceBinding:" + serviceBinding.getServiceBindingId());
			Collection qualifyers = serviceBinding.getBindingQualifyers();
			Iterator qualifyersIterator = qualifyers.iterator();
			while(qualifyersIterator.hasNext())
			{	
				SmallQualifyerImpl qualifyer = (SmallQualifyerImpl)qualifyersIterator.next();
				qualifyersIterator.remove();
				serviceBinding.getBindingQualifyers().remove(qualifyer);
			}
			db.remove(serviceBinding);
		}
		
		//ServiceBindingController.deleteServiceBindingsReferencingSiteNodeVersion(siteNodeVersion, db);
	    SiteNodeStateController.getController().deleteAccessRights("SiteNodeVersion", siteNodeVersion.getId(), db);
	    
		db.remove(siteNodeVersion);
	}

 	
 	//Copy logic
	/**
	 * This method copies a siteNode after first making a couple of controls that the move is valid.
	 */

    public void copySiteNode(SiteNodeVO siteNodeVO, Integer newParentSiteNodeId, InfoGluePrincipal principal, ProcessBean processBean) throws ConstraintException, SystemException
    {
    	Timer t = new Timer();
    	
    	logger.info("siteNodeVO:" + siteNodeVO);
    	logger.info("newParentSiteNodeId:" + newParentSiteNodeId);
    	logger.info("principal:" + principal);
    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

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
            
            SiteNodeVO siteNode = getSiteNodeVOWithId(siteNodeVO.getSiteNodeId(), db);
            SiteNodeVO oldParentSiteNode = getSiteNodeVOWithId(siteNodeVO.getParentSiteNodeId(), db); //siteNode.getParentSiteNode();
            SiteNodeVO newParentSiteNode = getSiteNodeVOWithId(newParentSiteNodeId, db);

            SiteNodeVO tempSiteNode = getSiteNodeVOWithId(newParentSiteNode.getParentSiteNodeId(), db); //siteNode.getParentSiteNode();
            //SiteNode tempSiteNode = newParentSiteNode.getParentSiteNode();
			while(tempSiteNode != null)
			{
				if(tempSiteNode.getId().intValue() == siteNode.getId().intValue())
				{
					logger.warn("You cannot move the node to a child under it......");
            		throw new ConstraintException("SiteNode.parentSiteNodeId", "3402");
				}
				tempSiteNode = getSiteNodeVOWithId(tempSiteNode.getParentSiteNodeId(), db); //siteNode.getParentSiteNode();
				//tempSiteNode = tempSiteNode.getParentSiteNode();
			}	
			
			processBean.updateProcess("Checked for constraints");
			
			Map<Integer,Integer> siteNodeIdsMapping = new HashMap<Integer,Integer>();

			Map<Integer,Integer> contentIdsMapping = new HashMap<Integer,Integer>();
			Set<Integer> siteNodeIdsToCopy = new HashSet<Integer>();
			Set<Integer> contentIdsToCopy = new HashSet<Integer>();
			List<ContentVersion> newCreatedContentVersions = new ArrayList<ContentVersion>();
			
			copySiteNodeRecursive(siteNode, newParentSiteNode, principal, siteNodeIdsMapping, contentIdsMapping, contentIdsToCopy, newCreatedContentVersions, db, processBean);
	        //After this all sitenodes main should be copied but then we have to round up some related nodes later
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("copySiteNodeRecursive", t.getElapsedTime());
			
			processBean.updateProcess("Now we search for related contents to copy");
			//Now let's go through all contents
			for(ContentVersion version : newCreatedContentVersions)
	        {
	        	getRelatedEntities(newParentSiteNode, principal, version.getVersionValue(), siteNodeVO.getRepositoryId(), newParentSiteNode.getRepositoryId(), siteNodeIdsMapping, contentIdsMapping, siteNodeIdsToCopy, contentIdsToCopy, newCreatedContentVersions, 0, 3, db, processBean);
	        	//getContentRelationsChain(siteNodesIdsMapping, contentIdsMapping, newParentSiteNode.getRepository().getId(), oldSiteNodeVO.getRepositoryId(), principal, versions, 0, 3, db);
	        }
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getRelatedEntities", t.getElapsedTime());
	        
			processBean.updateProcess("Copying " + contentIdsToCopy.size() + " contents");

			//After this all related sitenodes should have been created and all related contents accounted for
			copyContents(newParentSiteNode, principal, contentIdsToCopy, siteNodeVO.getRepositoryId(), newParentSiteNode.getRepositoryId(), contentIdsMapping, newCreatedContentVersions, db);
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("copyContents", t.getElapsedTime());

			processBean.updateProcess("Remapping relations");
            rewireBindingsAndRelations(siteNodeIdsMapping, contentIdsMapping, newCreatedContentVersions, db);
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("rewireBindingsAndRelations", t.getElapsedTime());
			
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
			logger.error("An error occurred so we should not complete the transaction: " + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction: " + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }   

	private void copySiteNodeRecursive(SiteNodeVO siteNode, SiteNodeVO newParentSiteNode, InfoGluePrincipal principal, Map<Integer,Integer> siteNodesIdsMapping, Map<Integer,Integer> contentIdsMapping, Set<Integer> contentIdsToCopy, List<ContentVersion> newCreatedContentVersions, Database db, ProcessBean processBean) throws Exception
    {
	   	processBean.updateLastDescription("Copied " + siteNodesIdsMapping.size() + "...");

		Timer t = new Timer();
		if(siteNodesIdsMapping.containsKey(siteNode.getId()))
		{
			logger.warn("Returning as this sitenode has allready been copied... no recusion please");
			return;
		}
		
		SiteNodeVO oldSiteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNode.getId(), db);
        ContentVO oldMetaInfoContentVO = ContentController.getContentController().getContentVOWithId(oldSiteNodeVO.getMetaInfoContentId(), db);

        LanguageVO masterLanguage = LanguageController.getController().getMasterLanguage(siteNode.getRepositoryId(), db);
        ContentVersionVO oldCVVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(oldMetaInfoContentVO.getId(), masterLanguage.getId(), db);
        RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getLatestActiveContentVersionVO", t.getElapsedTime());

		SiteNodeVO newSiteNodeVO = new SiteNodeVO();
		newSiteNodeVO.setName(oldSiteNodeVO.getName());
		newSiteNodeVO.setIsBranch(oldSiteNodeVO.getIsBranch());
        newSiteNodeVO.setCreatorName(oldSiteNodeVO.getCreatorName());
        newSiteNodeVO.setExpireDateTime(oldSiteNodeVO.getExpireDateTime());
        newSiteNodeVO.setIsProtected(oldSiteNodeVO.getIsProtected());
        newSiteNodeVO.setPublishDateTime(oldSiteNodeVO.getPublishDateTime());
        newSiteNodeVO.setStateId(oldSiteNodeVO.getStateId());
		
        SiteNodeVO newSiteNode = SiteNodeControllerProxy.getSiteNodeControllerProxy().acCreate(principal, newParentSiteNode.getId(), siteNode.getSiteNodeTypeDefinitionId(), newParentSiteNode.getRepositoryId(), newSiteNodeVO, db).getValueObject();
        
        RequestAnalyser.getRequestAnalyser().registerComponentStatistics("acCreate", t.getElapsedTime());
        
        SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(db, oldSiteNodeVO.getId());
        RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getLatestActiveSiteNodeVersionVO", t.getElapsedTime());
		if(siteNodeVersionVO != null)
		{
			Integer oldSiteNodeVersionId = siteNodeVersionVO.getId();
			
			SiteNodeVersionVO newSiteNodeVersionVO = new SiteNodeVersionVO();
			newSiteNodeVersionVO.setContentType(siteNodeVersionVO.getContentType());
			newSiteNodeVersionVO.setPageCacheKey(siteNodeVersionVO.getPageCacheKey());
			newSiteNodeVersionVO.setPageCacheTimeout(siteNodeVersionVO.getPageCacheTimeout());
			newSiteNodeVersionVO.setDisableEditOnSight(siteNodeVersionVO.getDisableEditOnSight());
			newSiteNodeVersionVO.setDisableLanguages(siteNodeVersionVO.getDisableLanguages());
			newSiteNodeVersionVO.setDisablePageCache(siteNodeVersionVO.getDisablePageCache());
			newSiteNodeVersionVO.setDisableForceIdentityCheck(siteNodeVersionVO.getDisableForceIdentityCheck());
			newSiteNodeVersionVO.setForceProtocolChange(siteNodeVersionVO.getForceProtocolChange());
			newSiteNodeVersionVO.setIsProtected(siteNodeVersionVO.getIsProtected());
			newSiteNodeVersionVO.setVersionModifier(principal.getName());
			newSiteNodeVersionVO.setStateId(0);
			newSiteNodeVersionVO.setIsActive(true);
			newSiteNodeVersionVO.setModifiedDateTime(siteNodeVersionVO.getModifiedDateTime());
			
			SiteNodeVersionVO siteNodeVersion = SiteNodeVersionController.getController().createSmall(newSiteNode.getId(), principal, newSiteNodeVersionVO, db).getValueObject();
	        RequestAnalyser.getRequestAnalyser().registerComponentStatistics("createSmall", t.getElapsedTime());
			
	        Map args = new HashMap();
		    args.put("globalKey", "infoglue");
		    PropertySet ps = PropertySetManager.getInstance("jdbc", args);

		    String disabledLanguagesString = ps.getString("siteNode_" + oldSiteNodeVO.getId() + "_disabledLanguages");
		    String enabledLanguagesString = ps.getString("siteNode_" + oldSiteNodeVO.getId() + "_enabledLanguages");
		    logger.info("disabledLanguagesString:" + disabledLanguagesString);
		    logger.info("enabledLanguagesString:" + enabledLanguagesString);
		    
		    if(disabledLanguagesString != null && !disabledLanguagesString.equals(""))
		    	ps.setString("siteNode_" + newSiteNode.getId() + "_disabledLanguages", disabledLanguagesString);
		    if(enabledLanguagesString != null && !enabledLanguagesString.equals(""))
		    	ps.setString("siteNode_" + newSiteNode.getId() + "_enabledLanguages", enabledLanguagesString);

		    RequestAnalyser.getRequestAnalyser().registerComponentStatistics("ps", t.getElapsedTime());

		    //Copy all access rights...
		    SiteNodeStateController.getController().copyAccessRights("SiteNodeVersion", oldSiteNodeVersionId, siteNodeVersion.getId(), db);
		    RequestAnalyser.getRequestAnalyser().registerComponentStatistics("copyAccessRights", t.getElapsedTime());
		    /*
		    copyAccessRights(db, oldSiteNodeVersionId.toString(), siteNodeVersion.getId().toString(), "SiteNodeVersion.Read");
		    copyAccessRights(db, oldSiteNodeVersionId.toString(), siteNodeVersion.getId().toString(), "SiteNodeVersion.Write");
		    copyAccessRights(db, oldSiteNodeVersionId.toString(), siteNodeVersion.getId().toString(), "SiteNodeVersion.CreateSiteNode");
		    copyAccessRights(db, oldSiteNodeVersionId.toString(), siteNodeVersion.getId().toString(), "SiteNodeVersion.DeleteSiteNode");
		    copyAccessRights(db, oldSiteNodeVersionId.toString(), siteNodeVersion.getId().toString(), "SiteNodeVersion.MoveSiteNode");
		    copyAccessRights(db, oldSiteNodeVersionId.toString(), siteNodeVersion.getId().toString(), "SiteNodeVersion.SubmitToPublish");
		    copyAccessRights(db, oldSiteNodeVersionId.toString(), siteNodeVersion.getId().toString(), "SiteNodeVersion.ChangeAccessRights");
		    copyAccessRights(db, oldSiteNodeVersionId.toString(), siteNodeVersion.getId().toString(), "SiteNodeVersion.Publish");
		    */
		    //t.printElapsedTime("copy access");
		}
        
		//ContentVersion newCV = null;
		List<ContentVersion> newContentVersions = new ArrayList<ContentVersion>();
		
		Content newMetaInfoContent = SiteNodeController.getController().createSiteNodeMetaInfoContent(db, newSiteNode, newParentSiteNode.getRepositoryId(), principal, null, newContentVersions);
	    RequestAnalyser.getRequestAnalyser().registerComponentStatistics("createSiteNodeMetaInfoContent", t.getElapsedTime());
		//t.printElapsedTime("newMetaInfoContent:" + newMetaInfoContent);
        
        //ContentVersionVO newCV = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(newMetaInfoContent.getId(), masterLanguage.getId(), db);
        //t.printElapsedTime("newCV:" + newCV);
        
        logger.info("Adding content version " + newContentVersions.size());
        newCreatedContentVersions.addAll(newContentVersions);

        logger.info("Mapping siteNode " + oldSiteNodeVO.getId() + " to " + newSiteNode.getId());
        siteNodesIdsMapping.put(oldSiteNodeVO.getId(), newSiteNode.getId());
        
        String versionValue = oldCVVO.getVersionValue();
        if(newContentVersions.size() > 0)
        {
        	ContentVersion newCV = newContentVersions.get(0);
        	newCV.getValueObject().setVersionValue(versionValue);
        }
        
        for(SiteNodeVO childNode : (Collection<SiteNodeVO>)getChildSiteNodeVOList(siteNode.getId(), false, db))
        {
        	copySiteNodeRecursive(childNode, newSiteNode, principal, siteNodesIdsMapping, contentIdsMapping, contentIdsToCopy, newCreatedContentVersions, db, processBean);
        }
    }
 	
	private void getRelatedEntities(SiteNodeVO newParentSiteNode, InfoGluePrincipal principal, String versionValue, Integer oldRepositoryId, Integer newRepositoryId, Map<Integer,Integer> siteNodeIdsMapping, Map<Integer,Integer> contentIdsMapping, Set<Integer> siteNodeIdsToCopy, Set<Integer> contentIdsToCopy, List<ContentVersion> versions, int depth, int maxDepth, Database db, ProcessBean processBean) throws Exception
	{
		if(depth > maxDepth)
			return;
		
		Set<Integer> relatedSiteNodeIds = new HashSet<Integer>();
		Set<Integer> relatedContentIds = RegistryController.getController().getComponents(versionValue);
		
		RegistryController.getController().getComponentBindings(versionValue, relatedSiteNodeIds, relatedContentIds);
		logger.info("Searching for related sitenodes");
		RegistryController.getController().getInlineSiteNodes(versionValue, relatedSiteNodeIds, relatedContentIds);
		logger.info("Searching for related contents");
		RegistryController.getController().getInlineContents(versionValue, relatedSiteNodeIds, relatedContentIds);

		contentIdsToCopy.addAll(relatedContentIds);
		siteNodeIdsToCopy.addAll(relatedSiteNodeIds);
		
		logger.info("A relatedSiteNodeIds:" + relatedSiteNodeIds.size());
		logger.info("A relatedContentIds:" + relatedContentIds.size());
		
		Iterator relatedContentIdsIterator = relatedContentIds.iterator();
		while(relatedContentIdsIterator.hasNext())
		{
			Integer contentId = (Integer)relatedContentIdsIterator.next();
			logger.info("contentId:" + contentId);
			try
			{
				ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId, db);
				
				if(contentVO != null)
				{
					logger.info("The contentVO 1: " + contentVO.getName() + " from repo " + contentVO.getRepositoryId());
					if(contentVO.getRepositoryId().intValue() == oldRepositoryId.intValue())
					{
						logger.info("The related content was in the old repo as well - let's copy that as well");
						
						/*
						if(contentIdsToCopy.contains(contentId))
						{
							logger.warn("Allready transferred content so skipping:" + contentVO.getName());
							continue;
						}
						*/
						List<ContentVersionVO> latestContentVersionVOListAllLanguages = ContentVersionController.getContentVersionController().getLatestContentVersionVOListPerLanguage(contentId, db);
						for(ContentVersionVO contentVersionVO : latestContentVersionVOListAllLanguages)
						{
							getRelatedEntities(newParentSiteNode, principal, contentVersionVO.getVersionValue(), oldRepositoryId, newRepositoryId, siteNodeIdsMapping, contentIdsMapping, siteNodeIdsToCopy, contentIdsToCopy, versions, depth+1, maxDepth, db, processBean);
						}
					}
					else
						logger.info("The related content was outside the old repo so we skip it");
				}
				else
					logger.warn("The related content was not found");
			}
			catch (SystemException e) 
			{
				logger.warn("Error getting related content:" + e.getMessage(), e);
			}
		}
		
		Iterator relatedSiteNodeIdsIterator = relatedSiteNodeIds.iterator();
		while(relatedSiteNodeIdsIterator.hasNext())
		{
			Integer siteNodeId = (Integer)relatedSiteNodeIdsIterator.next();
			
			SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId, db);
			if(siteNodeVO != null)
			{
				logger.info("siteNodeVO: " + siteNodeVO + " for " + siteNodeId);
				logger.info("The siteNodeVO: " + siteNodeVO.getName() + " from repo " + siteNodeVO.getRepositoryId());
				if(siteNodeVO.getRepositoryId().intValue() == oldRepositoryId.intValue())
				{
					logger.info("The related siteNode was in the old repo as well - let's copy that as well - hope we don't get recursive on our ass");
					if(siteNodeIdsToCopy.contains(siteNodeId))
					{
						logger.info("Allready transferred siteNode so skipping:" + siteNodeVO.getName());
						continue;
					}
					
		            SiteNodeVO siteNode = getSiteNodeVOWithId(siteNodeVO.getSiteNodeId(), db);
		            SiteNodeVO oldParentSiteNode = getSiteNodeVOWithId(siteNode.getParentSiteNodeId(), db);
		            //SiteNode oldParentSiteNode = siteNode.getParentSiteNode();
		            
					copySiteNodeRecursive(siteNode, newParentSiteNode, principal, siteNodeIdsMapping, contentIdsMapping, contentIdsToCopy, versions, db, processBean);
				}
				else
					logger.info("The related content was outside the old repo so we skip it");
			}
			else
				logger.warn("Skipping related sitenode as it was missing.");
		}
	}

	
	private void copyContents(SiteNodeVO newParentSiteNode, InfoGluePrincipal principal, Set<Integer> contentIdsToCopy, /*String versionValue, */Integer oldRepositoryId, Integer newRepositoryId, Map<Integer,Integer> contentIdsMapping, List<ContentVersion> versions, Database db) throws Exception
	{
		logger.info("contentIdsToCopy:" + contentIdsToCopy.size());
		
		for(Integer contentId : contentIdsToCopy)
		{
			logger.info("contentId:" + contentId);
			try
			{
				ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId, db);
				if(contentVO != null && newRepositoryId.intValue() != contentVO.getRepositoryId().intValue())
				{
					logger.info("The contentVO: " + contentVO.getName() + " from repo " + contentVO.getRepositoryId());
					if(contentVO.getRepositoryId().intValue() == oldRepositoryId.intValue())
					{
						logger.info("The related content was in the old repo as well - let's copy that as well");
						if(contentIdsMapping.containsKey(contentId))
						{
							logger.info("Allready transferred content so skipping:" + contentVO.getName());
							continue;
						}
						
						String path = ContentController.getContentController().getContentPath(contentId, true, false, db);
						logger.info("path:" + path);
						ContentVO copiedContent = ContentController.getContentController().getContentVOWithPath(newRepositoryId, path, true, principal, db);
						logger.info("copiedContent:" + copiedContent);
						
			            logger.info("Mapping content " + contentVO.getId() + " to " + copiedContent.getId());
			            contentIdsMapping.put(contentVO.getId(), copiedContent.getId());
						
						copiedContent.setName(contentVO.getName());
						copiedContent.setExpireDateTime(contentVO.getExpireDateTime());
						copiedContent.setIsBranch(contentVO.getIsBranch());
						copiedContent.setIsProtected(contentVO.getIsProtected());
						copiedContent.setPublishDateTime(contentVO.getPublishDateTime());
						copiedContent.setCreatorName(principal.getName());
						
					    SiteNodeStateController.getController().copyAccessRights("Content", contentVO.getId(), copiedContent.getId(), db);

						/*
					    copyAccessRights(db, contentVO.getId().toString(), copiedContent.getId().toString(), "Content.Read");
					    copyAccessRights(db, contentVO.getId().toString(), copiedContent.getId().toString(), "Content.Write");
					    copyAccessRights(db, contentVO.getId().toString(), copiedContent.getId().toString(), "Content.Create");
					    copyAccessRights(db, contentVO.getId().toString(), copiedContent.getId().toString(), "Content.Delete");
					    copyAccessRights(db, contentVO.getId().toString(), copiedContent.getId().toString(), "Content.Move");
					    copyAccessRights(db, contentVO.getId().toString(), copiedContent.getId().toString(), "Content.SubmitToPublish");
					    copyAccessRights(db, contentVO.getId().toString(), copiedContent.getId().toString(), "Content.ChangeAccessRights");
					    copyAccessRights(db, contentVO.getId().toString(), copiedContent.getId().toString(), "Content.CreateVersion");
					    */
					    
					    Map<Integer,Integer> assetIdMap = new HashMap<Integer,Integer>();
						Collection<ContentVersionVO> contentVersionVOList = ContentVersionController.getContentVersionController().getContentVersionVOList(contentVO.getId());
						for(ContentVersionVO contentVersionVO : contentVersionVOList)
						{
							logger.info("contentVersionVO:" + contentVersionVO.getId());
							Integer oldContentVersionId = contentVersionVO.getId();
							logger.info("oldContentVersionId:" + oldContentVersionId);
							//List assets = DigitalAssetController.getDigitalAssetVOList(contentVersionVO.getId());
							List<ContentCategory> contentCategories = ContentCategoryController.getController().findByContentVersionReadOnly(contentVersionVO.getId(), db);
							//logger.info("assets:" + assets);
							logger.info("contentCategories:" + contentCategories);
							
							//ContentTypeDefinition ctd = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithId(contentVO.getContentTypeDefinitionId(), db);
							contentVersionVO.setStateId(0);
							
							ContentVersionVO newContentVersionVO = new ContentVersionVO();
							newContentVersionVO.setStateId(0);
							newContentVersionVO.setVersionModifier(contentVersionVO.getVersionModifier());
							newContentVersionVO.setModifiedDateTime(contentVersionVO.getModifiedDateTime());
							newContentVersionVO.setVersionValue(contentVersionVO.getVersionValue());

							ContentVersion contentVersion = ContentVersionController.getContentVersionController().createMedium(copiedContent.getId(), contentVersionVO.getLanguageId(), contentVersionVO, db);
							
							//contentVersion.getOwningContent().setContentTypeDefinition((ContentTypeDefinitionImpl)ctd);
							
				            logger.info("contentVO.getId():" + contentVO.getId() + "");

				            logger.info("Adding content version " + contentVersion);
				            versions.add(contentVersion);
							
							DigitalAssetController.getController().createByCopy(oldContentVersionId, (MediumContentVersionImpl)contentVersion, assetIdMap, db);
							for(ContentCategory cc : contentCategories)
							{
								ContentCategoryVO contentCategoryVO = new ContentCategoryVO();
								contentCategoryVO.setAttributeName(cc.getAttributeName());
								ContentCategoryController.getController().createWithDatabase(contentCategoryVO, cc.getCategory(), contentVersion, db);
							}
							
							logger.info("Created contentVersion:" + contentVersion);
						}
					}
					else
						logger.info("The related content was outside the old repo so we skip it");
				}
				else
				{
					logger.info("The related content was not found or the content was in the same repo so we reuse the old one");
				}
			}
			catch (SystemException e) 
			{
				logger.warn("Error getting related content:" + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * This method copies a siteNode after first making a couple of controls that the move is valid.
	 */
	
	private Map<String,InterceptionPoint> interceptionPoints = new HashMap<String,InterceptionPoint>();
	public void copyAccessRights(Database db, String oldParameter, String newParameter, String interceptionPointName) throws Exception 
	{
		InterceptionPoint interceptionPoint = interceptionPoints.get(interceptionPointName);
		if(interceptionPoint == null)
		{			
			interceptionPoint = InterceptionPointController.getController().getInterceptionPointWithName(interceptionPointName, db);
			interceptionPoints.put(interceptionPointName, interceptionPoint);
		}
		
		List<AccessRight> accessRights = AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPoint.getId(), oldParameter, db);
		for(AccessRight accessRight : accessRights)
		{		
			AccessRightImpl arImpl = new AccessRightImpl();
			arImpl.setInterceptionPoint(interceptionPoint);
			arImpl.setParameters(newParameter);
			
			db.create(arImpl);

			Iterator accessRightRoleIterator = accessRight.getRoles().iterator();
			while(accessRightRoleIterator.hasNext())
			{
				AccessRightRole accessRightRole = (AccessRightRole)accessRightRoleIterator.next();

				AccessRightRoleImpl arrImpl = new AccessRightRoleImpl();
				arrImpl.setAccessRight(arImpl);
				arrImpl.setRoleName(accessRightRole.getRoleName());
				arImpl.getRoles().add(arrImpl);

				db.create(arrImpl);
			}

			Iterator accessRightGroupIterator = accessRight.getGroups().iterator();
			while(accessRightGroupIterator.hasNext())
			{
				AccessRightGroup accessRightGroup = (AccessRightGroup)accessRightGroupIterator.next();

				AccessRightGroupImpl argImpl = new AccessRightGroupImpl();
				argImpl.setAccessRight(arImpl);
				argImpl.setGroupName(accessRightGroup.getGroupName());
				arImpl.getGroups().add(argImpl);

				db.create(argImpl);
			}

			Iterator accessRightUserIterator = accessRight.getUsers().iterator();
			while(accessRightUserIterator.hasNext())
			{
				AccessRightUser accessRightUser = (AccessRightUser)accessRightUserIterator.next();

				AccessRightUserImpl aruImpl = new AccessRightUserImpl();
				aruImpl.setAccessRight(arImpl);
				aruImpl.setUserName(accessRightUser.getUserName());
				arImpl.getUsers().add(aruImpl);

				db.create(aruImpl);
			}
		}
	}
	
    private void rewireBindingsAndRelations(Map<Integer,Integer> siteNodesIdsMapping, Map<Integer,Integer> contentIdsMapping, List<ContentVersion> versions, Database db) 
    {
    	for(ContentVersion version : versions)
    	{
    		logger.info("Rewiring version:" + version.getId() + ":" + version.getVersionValue());
    		String contentVersionValue = version.getVersionValue();
    		
	        contentVersionValue = contentVersionValue.replaceAll("contentId=\"", "contentId=\"oldContentId_");
	        contentVersionValue = contentVersionValue.replaceAll("\\?contentId=", "\\?contentId=oldContentId_");
	        contentVersionValue = contentVersionValue.replaceAll("getInlineAssetUrl\\(", "getInlineAssetUrl\\(oldContentId_");
	        contentVersionValue = contentVersionValue.replaceAll("languageId,", "languageId,oldContentId_");
	        contentVersionValue = contentVersionValue.replaceAll("entity=\"Content\" entityId=\"", "entity=\"Content\" entityId=\"oldContentId_");
	        //contentVersionValue = contentVersionValue.replaceAll("entity='Content'><id>", "entity='Content'><id>oldContentId_");
	        contentVersionValue = contentVersionValue.replaceAll("siteNodeId=\"", "siteNodeId=\"oldSiteNodeId_");
	        contentVersionValue = contentVersionValue.replaceAll("detailSiteNodeId=\"", "detailSiteNodeId=\"oldSiteNodeId_");
	        contentVersionValue = contentVersionValue.replaceAll("getPageUrl\\((\\d)", "getPageUrl\\(oldSiteNodeId_$1");
	        contentVersionValue = contentVersionValue.replaceAll("entity=\"SiteNode\" entityId=\"", "entity=\"SiteNode\" entityId=\"oldSiteNodeId_");
			
			Iterator<Integer> contentIdMapIterator = contentIdsMapping.keySet().iterator();
	        while (contentIdMapIterator.hasNext()) 
	        {
	            Integer oldContentId = contentIdMapIterator.next();
	            Integer newContentId = contentIdsMapping.get(oldContentId);
	            
	            logger.info("Replacing all:" + oldContentId + " with " + newContentId);
	            
	            contentVersionValue = contentVersionValue.replaceAll("contentId=\"oldContentId_" + oldContentId + "\"", "contentId=\"" + newContentId + "\"");
	            contentVersionValue = contentVersionValue.replaceAll("\\?contentId=oldContentId_" + oldContentId + "&", "\\?contentId=" + newContentId + "&");
	            contentVersionValue = contentVersionValue.replaceAll("getInlineAssetUrl\\(oldContentId_" + oldContentId + ",", "getInlineAssetUrl\\(" + newContentId + ",");
	            contentVersionValue = contentVersionValue.replaceAll("languageId,oldContentId_" + oldContentId + "\\)", "languageId," + newContentId + "\\)");
	            contentVersionValue = contentVersionValue.replaceAll("entity=\"Content\" entityId=\"oldContentId_" + oldContentId + "\"", "entity=\"Content\" entityId=\"" + newContentId + "\"");
	            contentVersionValue = contentVersionValue.replaceAll("<id>oldContentId_" + oldContentId + "</id>", "<id>" + newContentId + "</id>");
	        }
	        
	        Iterator<Integer> siteNodeIdMapIterator = siteNodesIdsMapping.keySet().iterator();
	        while (siteNodeIdMapIterator.hasNext()) 
	        {
	            Integer oldSiteNodeId = siteNodeIdMapIterator.next();
	            Integer newSiteNodeId = siteNodesIdsMapping.get(oldSiteNodeId);
	            
	            logger.info("Replacing all:" + oldSiteNodeId + " with " + newSiteNodeId);
	            
	            contentVersionValue = contentVersionValue.replaceAll("siteNodeId=\"oldSiteNodeId_" + oldSiteNodeId + "\"", "siteNodeId=\"" + newSiteNodeId + "\"");
	            contentVersionValue = contentVersionValue.replaceAll("detailSiteNodeId=\"oldSiteNodeId_" + oldSiteNodeId + "\"", "detailSiteNodeId=\"" + newSiteNodeId + "\"");
	            contentVersionValue = contentVersionValue.replaceAll("getPageUrl\\(oldSiteNodeId_" + oldSiteNodeId + ",", "getPageUrl\\(" + newSiteNodeId + ",");
	            contentVersionValue = contentVersionValue.replaceAll("entity=\"SiteNode\" entityId=\"oldSiteNodeId_" + oldSiteNodeId + "\"", "entity=\"SiteNode\" entityId=\"" + newSiteNodeId + "\"");
	            contentVersionValue = contentVersionValue.replaceAll("<id>oldSiteNodeId_" + oldSiteNodeId + "</id>", "<id>" + newSiteNodeId + "</id>");
	        }
	        
	        //Now replace all occurrances of old as they are stuff not moved and should be restored.
	        contentVersionValue = contentVersionValue.replaceAll("oldContentId_", "");
	        contentVersionValue = contentVersionValue.replaceAll("oldSiteNodeId_", "");
	        
	        version.setVersionValue(contentVersionValue);
    	}
    }
    
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
	
	public List<SiteNodeVO> getUpcomingExpiringSiteNodes(int numberOfWeeks) throws Exception
	{
		List<SiteNodeVO> siteNodeVOList = new ArrayList<SiteNodeVO>();

		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
    		OQLQuery oql = db.getOQLQuery("SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl sn WHERE " +
    				"sn.expireDateTime > $1 AND sn.expireDateTime < $2 AND sn.publishDateTime < $3 ORDER BY sn.siteNodeId");

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
			logger.error("An error occurred so we should not complete the transaction: " + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction: " + e.getMessage(), e);
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

    public void changeSiteNodeSortOrder(Integer siteNodeId, Integer beforeSiteNodeId, String direction, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();

        beginTransaction(db);

        try
        {
        	SiteNodeVO siteNodeVO = getSiteNodeVOWithId(siteNodeId, db);
			//Fixes a nice ordered list
			List<SiteNodeVO> childrenVOList = SiteNodeController.getController().getChildSiteNodeVOList(siteNodeVO.getParentSiteNodeId(), false, db);
			Iterator<SiteNodeVO> childrenVOListIterator = childrenVOList.iterator();
			int index = 0;
			while(childrenVOListIterator.hasNext())
			{
				SiteNodeVO childSiteNodeVO = childrenVOListIterator.next();
				SiteNodeVersion latestChildSiteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersion(db, childSiteNodeVO.getId());
				logger.info("latestChildSiteNodeVersion.getSortOrder():" + latestChildSiteNodeVersion.getSortOrder() + "=" + index);
				if(latestChildSiteNodeVersion.getSortOrder() != index)
				{
					latestChildSiteNodeVersion = SiteNodeVersionController.getController().updateStateId(latestChildSiteNodeVersion, SiteNodeVersionVO.WORKING_STATE, "Changed sortOrder", infoGluePrincipal, db);
	
					//Integer currentSortOrder = latestChildSiteNodeVersion.getSortOrder();
					//logger.info("currentSortOrder:" + currentSortOrder + " on " + childSiteNodeVO.getName());
					//if(currentSortOrder != 100)
					//{
					logger.info("Setting sort order to:" + index + " on " + latestChildSiteNodeVersion.getId());
						latestChildSiteNodeVersion.setSortOrder(index);
					//}
				}
				index++;
			}
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
	    db = CastorDatabaseService.getDatabase();

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
				
            	logger.info("OldSortOrder:" + oldSortOrder);
				List<SiteNodeVO> childrenVOList = SiteNodeController.getController().getChildSiteNodeVOList(siteNodeVO.getParentSiteNodeId(), false, db);
				Iterator<SiteNodeVO> childrenVOListIterator = childrenVOList.iterator();
				while(childrenVOListIterator.hasNext())
				{
					SiteNodeVO childSiteNodeVO = childrenVOListIterator.next();
					logger.info("childSiteNodeVO:" + childSiteNodeVO.getId() + ":" + childSiteNodeVO.getSortOrder());
					SiteNodeVersion latestChildSiteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersion(db, childSiteNodeVO.getId());
					//logger.info("latestChildSiteNodeVersion:" + latestChildSiteNodeVersion.getId());
					Integer currentSortOrder = latestChildSiteNodeVersion.getSortOrder();
					logger.info("currentSortOrder:" + currentSortOrder);
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

}
 
