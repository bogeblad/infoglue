
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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.ObjectNotFoundException;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.applications.databeans.ReferenceBean;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentCategory;
import org.infoglue.cms.entities.content.ContentCategoryVO;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.AccessRight;
import org.infoglue.cms.entities.management.AccessRightGroup;
import org.infoglue.cms.entities.management.AccessRightRole;
import org.infoglue.cms.entities.management.AccessRightUser;
import org.infoglue.cms.entities.management.InterceptionPoint;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.management.ServiceDefinition;
import org.infoglue.cms.entities.management.ServiceDefinitionVO;
import org.infoglue.cms.entities.management.SiteNodeTypeDefinition;
import org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.management.impl.simple.SiteNodeTypeDefinitionImpl;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.structure.ServiceBindingVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.impl.simple.MediumSiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallQualifyerImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallServiceBindingImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.XMLHelper;
import org.infoglue.cms.util.mail.MailServiceFactory;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.Timer;
import org.infoglue.deliver.util.VelocityTemplateProcessor;
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
    	
    	QueryResults results = oql.execute(Database.READONLY);
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
				logger.info("Fail: a siteNodeVO was read the old way...");
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
	
			QueryResults results = oql.execute(Database.READONLY);
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
				logger.info("Fail: a siteNodeVO was read the old way...");
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
	
			QueryResults results = oql.execute(Database.READONLY);
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

		QueryResults results = oql.execute(Database.READONLY);
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

		QueryResults results = oql.execute(Database.READONLY);
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

    public SmallSiteNodeImpl getSmallSiteNodeWithId(Integer siteNodeId, Database db) throws SystemException, Bug
    {
		return (SmallSiteNodeImpl) getObjectWithId(SmallSiteNodeImpl.class, siteNodeId, db);
    }

    public static SiteNode getSiteNodeWithId(Integer siteNodeId, Database db, boolean readOnly) throws SystemException, Bug
    {
        SiteNode siteNode = null;
        try
        {
			RequestAnalyser.getRequestAnalyser().incApproximateNumberOfDatabaseQueries();

			if(readOnly)
	            siteNode = (SiteNode)db.load(org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl.class, siteNodeId, Database.READONLY);
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
        	logger.warn("An error occurred so we should not complete the transaction:" + ce);
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
        List referenceBeanList = RegistryController.getController().getReferencingObjectsForSiteNode(siteNodeVO.getId(), -1, false, db);
		if(referenceBeanList != null && referenceBeanList.size() > 0 && !forceDelete)
			throw new ConstraintException("SiteNode.stateId", "3405");

		List<SiteNodeVO> children = getChildSiteNodeVOList(siteNodeVO.getId(), true, db, null);
		for(SiteNodeVO childSiteNode : children)
		{
			delete(childSiteNode, db, forceDelete, infogluePrincipal);
   		}
		
		if(forceDelete || getIsDeletable(siteNodeVO, infogluePrincipal, db))
	    {		 
			SiteNodeVersionController.deleteVersionsForSiteNode(siteNodeVO, db, infogluePrincipal);
			//ServiceBindingController.deleteServiceBindingsReferencingSiteNode(siteNodeVO, db);

			SmallSiteNodeImpl siteNode = getSmallSiteNodeWithId(siteNodeVO.getSiteNodeId(), db);
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
        	logger.info("An error occurred so we should not complete the transaction:" + ce, ce);
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
	 * This method returns true if the sitenode does not have any published siteNodeversions or 
	 * are restricted in any other way.
	 */
	
	private static boolean getIsDeletable(SiteNodeVO siteNodeVO, InfoGluePrincipal infogluePrincipal, Database db) throws SystemException, Exception
	{
		boolean isDeletable = true;
		
		SiteNodeVersionVO latestSiteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(db, siteNodeVO.getId());
		if(latestSiteNodeVersion != null && latestSiteNodeVersion.getIsProtected().equals(SiteNodeVersionVO.YES))
		{
			boolean hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(db, infogluePrincipal, "SiteNodeVersion.DeleteSiteNode", "" + latestSiteNodeVersion.getId());
			if(!hasAccess)
				return false;
		}
		
        List<SiteNodeVersionVO> siteNodeVersions = SiteNodeVersionController.getController().getSiteNodeVersionVOList(db, siteNodeVO.getId());
    	if(siteNodeVersions != null)
    	{
	        Iterator<SiteNodeVersionVO> versionIterator = siteNodeVersions.iterator();
			while (versionIterator.hasNext()) 
	        {
				SiteNodeVersionVO siteNodeVersion = versionIterator.next();
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

        if(parentSiteNodeId != null && parentSiteNodeId != -1)
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
		
		QueryResults results = oql.execute(Database.READONLY);

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

		QueryResults results = oql.execute(Database.READONLY);
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

		QueryResults results = oql.execute(Database.READONLY);
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
   	/*
	public List<SiteNodeVO> getChildSiteNodeVOList(Integer parentSiteNodeId, boolean showDeletedItems, Database db) throws Exception
	{
		return getChildSiteNodeVOList(parentSiteNodeId, showDeletedItems, db, null);
	}
*/    
	/**
	 * This method returns a list of the children a siteNode has.
	 */
   	
	public List<SiteNodeVO> getChildSiteNodeVOList(Integer parentSiteNodeId, boolean showDeletedItems, Database db, Integer sortLanguageId) throws Exception
	{
   		String key = "" + parentSiteNodeId + "_" + showDeletedItems + "_" + sortLanguageId;
   		if(!CmsPropertyHandler.getAllowLocalizedSortAndVisibilityProperties())
   			key = "" + parentSiteNodeId + "_" + showDeletedItems;
   		
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

		QueryResults results = oql.execute(Database.READONLY);
		while (results.hasMore()) 
		{
			SiteNode siteNode = (SiteNode)results.next();

			if(CmsPropertyHandler.getAllowLocalizedSortAndVisibilityProperties() && sortLanguageId != null)
			{	
				//logger.info("Name:" + siteNode.getName() + ":" + siteNode.getValueObject().getStateId() + ":" + siteNode.getValueObject().getIsProtected() + ":" + siteNode.getValueObject().getIsProtected());
				ContentVersionVO latestVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(siteNode.getMetaInfoContentId(), sortLanguageId, db);
				if(latestVersionVO == null)
				{
					//LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(siteNode.getValueObject().getRepositoryId(), db);
					//latestVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(siteNode.getMetaInfoContentId(), masterLanguageVO.getLanguageId(), db);
				}
				if(latestVersionVO != null)
				{
					String localizedIsHidden = ContentVersionController.getContentVersionController().getAttributeValue(latestVersionVO, "HideInNavigation", false);
					String localizedSortOrder = ContentVersionController.getContentVersionController().getAttributeValue(latestVersionVO, "SortOrder", false);
		
					//System.out.println("localizedIsHidden:" + localizedIsHidden);
					//System.out.println("localizedSortOrder:" + localizedSortOrder);
					if(localizedIsHidden != null/* && !localizedIsHidden.equals("")*/)
					{
						if(localizedIsHidden.equals("true"))
							siteNode.getValueObject().setLocalizedIsHidden(true);
						else
							siteNode.getValueObject().setLocalizedIsHidden(false);
					}
					
					if(localizedSortOrder != null && !localizedSortOrder.equals(""))
					{
						siteNode.getValueObject().setLocalizedSortOrder(new Integer(localizedSortOrder));
					}
					else
						siteNode.getValueObject().setLocalizedSortOrder(new Integer(100));
				}
				else
					siteNode.getValueObject().setLocalizedSortOrder(new Integer(100));
			}
			
			childrenVOList.add(siteNode.getValueObject());
		}
		
		results.close();
		oql.close();
        
		if(CmsPropertyHandler.getAllowLocalizedSortAndVisibilityProperties())
			Collections.sort(childrenVOList, new ReflectionComparator("sortOrder"));

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
    			logger.warn("repositoryId:" + repositoryId + " had no root");

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
		
		QueryResults results = oql.execute(Database.READONLY);
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
		
		Map<String,String> pageUrls = new HashMap<String, String>();

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

			pageUrls = RedirectController.getController().getNiceURIMapBeforeMove(db, siteNodeVO.getRepositoryId(), siteNodeVO.getSiteNodeId(), principal);

			logger.info("Setting the new Parent siteNode:" + siteNode.getSiteNodeId() + " " + newParentSiteNode.getSiteNodeId());
			siteNode.setParentSiteNode((SiteNodeImpl)newParentSiteNode);

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

			// If any of the validations or setMethods reported an error, we throw them up now before create.
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

		if (pageUrls.size() > 0)
		{
			Database redirectDb = null;
			try
			{
				redirectDb = CastorDatabaseService.getDatabase();
				beginTransaction(redirectDb);
				RedirectController.getController().createSystemRedirect(pageUrls, siteNodeVO.getRepositoryId(), siteNodeVO.getSiteNodeId(), principal, redirectDb);
				commitTransaction(redirectDb);
			}
			catch (Throwable tr)
			{
				rollbackTransaction(redirectDb);
				logger.error("An error occured when creating system redirect. Messge: " + tr.getMessage());
				logger.warn("An error occured when creating system redirect.", tr);
			}
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
        	MediumContentImpl metaInfoContent = (MediumContentImpl)ContentController.getContentController().getMediumContentWithId(metaInfoContentId, db);
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

	public List<SiteNodeVO> getRepositorySiteNodeVOList(Integer repositoryId, Database db) throws SystemException, Exception
    {
		List<SiteNodeVO> siteNodes = new ArrayList<SiteNodeVO>();
		
		OQLQuery oql = db.getOQLQuery("SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl sn WHERE sn.repositoryId = $1 ORDER BY sn.siteNodeId");
    	oql.bind(repositoryId);
    	
    	QueryResults results = oql.execute(Database.READONLY);
		
		while(results.hasMore()) 
        {
        	SiteNode siteNode = (SiteNodeImpl)results.next();
        	siteNodes.add(siteNode.getValueObject());
        }
		
		results.close();
		oql.close();

		return siteNodes;    	
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
    	
    	QueryResults results = oql.execute(Database.READONLY);
		
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
		return createSiteNodeMetaInfoContent(db, newSiteNode, null, repositoryId, principal, pageTemplateContentId, newContentVersions, false, null, null);
	}

	public MediumContentImpl createSiteNodeMetaInfoContent(Database db, SiteNodeVO newSiteNode, Integer repositoryId, InfoGluePrincipal principal, Integer pageTemplateContentId, List<ContentVersion> newContentVersions, boolean checkIfMetaInfoIsBroken) throws SystemException, Bug, Exception, ConstraintException
	{
		return createSiteNodeMetaInfoContent(db, newSiteNode, null, repositoryId, principal, pageTemplateContentId, newContentVersions, checkIfMetaInfoIsBroken, null, null);
	}

	public MediumContentImpl createSiteNodeMetaInfoContent(Database db, SiteNodeVO newSiteNode, Map<String,String> metaAttributes, Integer repositoryId, InfoGluePrincipal principal, Integer pageTemplateContentId, List<ContentVersion> newContentVersions, boolean checkIfMetaInfoIsBroken) throws SystemException, Bug, Exception, ConstraintException
	{
		return createSiteNodeMetaInfoContent(db, newSiteNode, metaAttributes, repositoryId, principal, pageTemplateContentId, newContentVersions, checkIfMetaInfoIsBroken, null, null);
	}

    public MediumContentImpl createSiteNodeMetaInfoContent(Database db, SiteNodeVO newSiteNode, Map<String,String> metaAttributes, Integer repositoryId, InfoGluePrincipal principal, Integer pageTemplateContentId, List<ContentVersion> newContentVersions, boolean checkIfMetaInfoIsBroken, ContentVO oldMetaInfoContentVO, String navigationTitleSuffix) throws SystemException, Bug, Exception, ConstraintException
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
        
        LanguageVO masterLanguage 			= LanguageController.getController().getMasterLanguage(repositoryId, db);
  	   
        RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getLatestSiteNodeVersionVO", t.getElapsedTime());
        
        Integer metaInfoContentTypeDefinitionId = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("Meta info", db).getId();
        Integer availableServiceBindingId = AvailableServiceBindingController.getController().getAvailableServiceBindingVOWithName("Meta information", db).getId();
        
        List serviceDefinitions = AvailableServiceBindingController.getController().getServiceDefinitionVOList(db, availableServiceBindingId);
        if(serviceDefinitions == null || serviceDefinitions.size() == 0)
        {
            ServiceDefinition serviceDefinition = ServiceDefinitionController.getController().getServiceDefinitionWithName("Core content service", db, false);
            String[] values = {serviceDefinition.getId().toString()};
            AvailableServiceBindingController.getController().update(availableServiceBindingId, values, db);
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
        	
    	    LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(repositoryId, db);

        	String componentStructure = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><components></components>";
        	if(pageTemplateContentId != null)
        	{
        		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(pageTemplateContentId, masterLanguageVO.getId(), db);
        		
        	    componentStructure = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, "ComponentStructure", false);
        	
    			Document document = XMLHelper.readDocumentFromByteArray(componentStructure.getBytes("UTF-8"));
    			String componentXPath = "//component";
    			NodeList componentNodes = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
    			for(int i=0; i < componentNodes.getLength(); i++)
    			{
    				Element element = (Element)componentNodes.item(i);
    				String componentId = element.getAttribute("id");
    				String componentContentId = element.getAttribute("contentId");
    				
    				ComponentController.getController().checkAndAutoCreateContents(db, newSiteNode.getId(), masterLanguageVO.getId(), masterLanguageVO.getId(), null, new Integer(componentId), document, new Integer(componentContentId), principal);
    				componentStructure = XMLHelper.serializeDom(document, new StringBuffer()).toString();
    			}
            	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("meta info create 1", t.getElapsedTime());
        	}
        	
        	List<LanguageVO> languageVOList = RepositoryLanguageController.getController().getLanguageVOListForRepositoryId(repositoryId, db);
        	for(LanguageVO languageVO : languageVOList)
        	{
	        	//Create initial content version also... in languageVO
	        	String versionValue = "<?xml version='1.0' encoding='UTF-8'?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes>";
	        	if(metaAttributes == null || !metaAttributes.containsKey(languageVO.getLanguageCode() + "_Title"))
	        		versionValue += "<Title><![CDATA[" + newSiteNode.getName() + "]]></Title>";
	        	if(metaAttributes == null || !metaAttributes.containsKey(languageVO.getLanguageCode() + "_NavigationTitle"))
	            	versionValue += "<NavigationTitle><![CDATA[" + newSiteNode.getName() + "]]></NavigationTitle>";
	        	if(metaAttributes == null || !metaAttributes.containsKey(languageVO.getLanguageCode() + "_NiceURIName"))
	            	versionValue += "<NiceURIName><![CDATA[" + new VisualFormatter().replaceNiceURINonAsciiWithSpecifiedChars(newSiteNode.getName(), CmsPropertyHandler.getNiceURIDefaultReplacementCharacter()) + "]]></NiceURIName>";
	        	if(metaAttributes == null || !metaAttributes.containsKey(languageVO.getLanguageCode() + "_Description"))
	            	versionValue += "<Description><![CDATA[" + newSiteNode.getName() + "]]></Description>";
	        	if(metaAttributes == null || !metaAttributes.containsKey(languageVO.getLanguageCode() + "_MetaInfo"))
	            	versionValue += "<MetaInfo><![CDATA[" + newSiteNode.getName() + "]]></MetaInfo>";
	        	
	        	boolean saveVersion = false;
	        	boolean realValue = false;
	        	if(metaAttributes != null)
	        	{
		        	for(String metaAttributeName : metaAttributes.keySet())
		        	{
		        		if(metaAttributeName.startsWith(languageVO.getLanguageCode() + "_"))
		        		{
		        			versionValue += "<" + metaAttributeName.replaceFirst(languageVO.getLanguageCode() + "_", "") + "><![CDATA[" + metaAttributes.get(metaAttributeName) + "]]></" + metaAttributeName.replaceFirst(languageVO.getLanguageCode() + "_", "") + ">";
		        			saveVersion = true;
		        			realValue = true;
		        		}
		        	}
	        	}
	        		        	
	        	if(!realValue) //No other values
	        		versionValue = "<?xml version='1.0' encoding='UTF-8'?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes>";
	        	
	        	if(languageVO.getId().equals(masterLanguageVO.getId()))
	        	{
	        		versionValue += "<ComponentStructure><![CDATA[" + componentStructure + "]]></ComponentStructure>";
        			saveVersion = true;
	        	}
	        	
	        	versionValue += "</attributes></article>";
	        	
	        	if(saveVersion)
	        	{
		        	ContentVersionVO contentVersionVO = new ContentVersionVO();
		        	contentVersionVO.setVersionComment("Autogenerated version");
		        	contentVersionVO.setVersionModifier(principal.getName());
		        	contentVersionVO.setVersionValue(versionValue);
		        	//ContentVersionController.getContentVersionController().create(contentVO.getId(), masterLanguage.getId(), contentVersionVO, null, db);
		        	MediumContentVersionImpl contentVersionImpl = ContentVersionController.getContentVersionController().createMedium(contentVO.getId(), languageVO.getId(), contentVersionVO, db);
		        	if(newContentVersions != null)
		        		newContentVersions.add(contentVersionImpl);
	
		        	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("meta info create 2", t.getElapsedTime());
	        	}
        	}
        	
        	
        	LanguageVO localMasterLanguageVO = getInitialLanguageVO(db, parentFolderContent.getId(), repositoryId);
        	//Also created a version in the local master language for this part of the site if any
        	/*
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
        	*/

			// If there is an old meta info content this is a copy action and we should take appropriate actions
			if (oldMetaInfoContentVO != null)
			{
				List<ContentVersionVO> contentVersions = ContentVersionController.getContentVersionController().getContentVersionVOList(oldMetaInfoContentVO.getId(), db);
				if (logger.isDebugEnabled())
				{
					StringBuilder sb = new StringBuilder();
					sb.append("Copying ContentVersions to new Meta info. Old Content.id: " + oldMetaInfoContentVO.getContentId());
					sb.append("\n\tNumber of contentVersions: " + contentVersions.size());
					sb.append("\n\tSite master language.id: " + masterLanguage.getId());
					sb.append("\n\tParent folder master language: " + localMasterLanguageVO.getId());
					logger.debug(sb);
				}
				else
				{
					logger.info("Copying ContentVersions to new Meta info. Old Content.id: " + oldMetaInfoContentVO.getContentId());
				}
				for(ContentVersionVO cv : contentVersions)
				{
					logger.debug("cv " + cv.getLanguageId() + ":" + cv.getId());
					if(!cv.getLanguageId().equals(masterLanguage.getId()) && !cv.getLanguageId().equals(localMasterLanguageVO.getId()))
					{
						logger.info("Should create version for content <" + contentVO.getId() + "> with language.id " + cv.getLanguageId());
						String versionValueOtherVersion = cv.getVersionValue();
						ContentVersionVO contentVersionVOLocalMaster = new ContentVersionVO();
						contentVersionVOLocalMaster.setVersionComment("Autogenerated version");
						contentVersionVOLocalMaster.setVersionModifier(principal.getName());
						contentVersionVOLocalMaster.setVersionValue(versionValueOtherVersion);
						MediumContentVersionImpl contentVersionImplLocal = ContentVersionController.getContentVersionController().createMedium(contentVO.getId(), cv.getLanguageId(), contentVersionVOLocalMaster, db);
						if(newContentVersions != null)
							newContentVersions.add(contentVersionImplLocal);
						RequestAnalyser.getRequestAnalyser().registerComponentStatistics("meta info create 4", t.getElapsedTime());
					}
				}

				logger.info("Transforming NavigationTitle for copied Meta info. Content.id " + contentVO.getId());
				List<ContentVersionVO> newContentsVersions = ContentVersionController.getContentVersionController().getContentVersionVOList(contentVO.getId(), db);
				Pattern navigationTransformPattern = Pattern.compile(Pattern.quote("<NavigationTitle><![CDATA[") + "(.+?)" + Pattern.quote("]]></NavigationTitle>"));
				for (ContentVersionVO contentVersionVO : newContentsVersions)
				{
					String versionValue = contentVersionVO.getVersionValue();
					Matcher matcher = navigationTransformPattern.matcher(versionValue);

					versionValue = matcher.replaceFirst(Matcher.quoteReplacement("<NavigationTitle><![CDATA[") + "$1" + navigationTitleSuffix + Matcher.quoteReplacement("]]></NavigationTitle>"));
					contentVersionVO.setVersionValue(versionValue);
					if (logger.isTraceEnabled())
					{
						logger.trace("Version value after copy transformation (ContentVersion.id: " + contentVersionVO.getContentVersionId() + "). VersionValue: " + contentVersionVO.getVersionValue());
					}
					ContentVersionController.getContentVersionController().update(contentVO.getId(), contentVersionVO.getLanguageId(), contentVersionVO, db);
				}
			}

        	ServiceBindingVO serviceBindingVO = new ServiceBindingVO();
        	serviceBindingVO.setName(newSiteNode.getName() + " Metainfo");
        	serviceBindingVO.setPath("/None specified/");
        
        	String qualifyerXML = "<?xml version='1.0' encoding='UTF-8'?><qualifyer><contentId>" + contentVO.getId() + "</contentId></qualifyer>";
        
        	//ServiceBindingController.getController().create(db, serviceBindingVO, qualifyerXML, availableServiceBindingId, siteNodeVersionVO.getId(), singleServiceDefinitionVO.getId());	
        	//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("meta info service bind", t.getElapsedTime());
        	
        	if(checkIfMetaInfoIsBroken)
        		SiteNodeController.getController().update(newSiteNode, db);
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

    private static Map<Integer,Integer> metaInfoSiteNodeIdMap = new ConcurrentHashMap<Integer,Integer>();
    
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
			//logger.error("Asking for heavy lookup on meta info content id:" + contentId);
			//Thread.dumpStack();
			//System.out.println("Asking for mapping:" + contentId);
			OQLQuery oql = db.getOQLQuery("SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl sn WHERE sn.metaInfoContentId = $1 ORDER BY sn.siteNodeId");
	    	oql.bind(contentId);
	    	
	    	QueryResults results = oql.execute(Database.READONLY);
			
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

	public String getSiteNodeIdPath(Integer siteNodeId) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		
		SiteNodeVO siteNodeVO = getSiteNodeVOWithId(siteNodeId);
		sb.insert(0, siteNodeVO.getId());
		while(siteNodeVO != null && siteNodeVO.getParentSiteNodeId() != null)
		{
			sb.insert(0, siteNodeVO.getParentSiteNodeId() + ",");
			if(siteNodeVO.getParentSiteNodeId() != null)
				siteNodeVO = getSiteNodeVOWithId(siteNodeVO.getParentSiteNodeId());
			else
				siteNodeVO = null;
		}
			
		return sb.toString();
	}
	
	public String getSiteNodePath(Integer siteNodeId, Database db) throws Exception
	{
		return getSiteNodePath(siteNodeId, true, false, db);
	}


	public String getSiteNodePath(Integer siteNodeId, boolean includeRootSiteNode, boolean includeRepositoryName) throws ConstraintException, SystemException
    {
		String path = "";
		
		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
        	path = getSiteNodePath(siteNodeId, includeRootSiteNode, includeRepositoryName, db);
        	
            commitTransaction(db);
        }
        catch(Exception e)
        {
			logger.error("An error occurred so we should not complete the transaction: " + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction: " + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
		
        return path;
    } 
    
	public String getSiteNodePath(Integer siteNodeId, boolean includeRootSiteNode, boolean includeRepositoryName, Database db) throws Exception
	{
		SiteNodeVO siteNodeVO = getSiteNodeVOWithId(siteNodeId, db);
		if (siteNodeVO != null)
		{
			return getSiteNodePath(siteNodeVO, includeRootSiteNode, includeRepositoryName, db);
		}
		else
		{
			logger.warn("Tried to compute path for SiteNode but did not find the SiteNode. SiteNode.id: " + siteNodeId);
			return "";
		}
	}

	public String getSiteNodePath(SiteNodeVO siteNodeVO, boolean includeRootSiteNode, boolean includeRepositoryName, Database db) throws Exception
	{
		StringBuffer sb = new StringBuffer();

		RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(siteNodeVO.getRepositoryId(), db);
		while(siteNodeVO != null)
		{
			if (includeRootSiteNode || siteNodeVO.getParentSiteNodeId() != null)
			{
				sb.insert(0, "/" + siteNodeVO.getName());
			}
			if(siteNodeVO.getParentSiteNodeId() != null)
				siteNodeVO = getSiteNodeVOWithId(siteNodeVO.getParentSiteNodeId(), db);
			else
				siteNodeVO = null;
		}

		if (includeRepositoryName)
		{
			if(repositoryVO != null)
				sb.insert(0, repositoryVO.getName() + "/");
		}

		return sb.toString().replaceAll("//", "/");
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

        	QueryResults results = oql.execute(Database.READONLY);
			
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
            /*
            if(siteNodeVO.getId().intValue() == newParentSiteNodeId.intValue())
            {
            	logger.warn("You cannot have the siteNode as it's own parent......");
            	throw new ConstraintException("SiteNode.parentSiteNodeId", "3401");
            }
            */

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
			
			Map args = new HashMap();
		    args.put("globalKey", "infoglue");
		    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
			Collection keys = ps.getKeys();
			logger.info("keys:" + keys.size());
			processBean.updateProcess("Propertyset fetched...");
			
			Map<Integer,Integer> siteNodeIdsMapping = new HashMap<Integer,Integer>();

			Map<Integer,Integer> contentIdsMapping = new HashMap<Integer,Integer>();
			Set<Integer> siteNodeIdsToCopy = new HashSet<Integer>();
			Set<Integer> contentIdsToCopy = new HashSet<Integer>();
			List<ContentVersion> newCreatedContentVersions = new ArrayList<ContentVersion>();

			String newNameSuffix = getNewNameSuffixForCopy(principal, db, newParentSiteNode);

			copySiteNodeRecursive(siteNode, newParentSiteNode, principal, siteNodeIdsMapping, contentIdsMapping, contentIdsToCopy, newCreatedContentVersions, newNameSuffix, db, processBean);
			//After this all sitenodes main should be copied but then we have to round up some related nodes later
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("copySiteNodeRecursive", t.getElapsedTime());
			
			processBean.updateProcess("Now we search for related contents to copy");
			//Now let's go through all contents
			for(ContentVersion version : newCreatedContentVersions)
	        {
				getRelatedEntities(newParentSiteNode, principal, version.getVersionValue(), siteNodeVO.getRepositoryId(), newParentSiteNode.getRepositoryId(), siteNodeIdsMapping, contentIdsMapping, siteNodeIdsToCopy, contentIdsToCopy, newCreatedContentVersions, 0, 3, newNameSuffix, db, processBean);
	        	//getContentRelationsChain(siteNodesIdsMapping, contentIdsMapping, newParentSiteNode.getRepository().getId(), oldSiteNodeVO.getRepositoryId(), principal, versions, 0, 3, db);
	        }
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getRelatedEntities", t.getElapsedTime());
	        
			processBean.updateProcess("Copying " + contentIdsToCopy.size() + " contents");

			//After this all related sitenodes should have been created and all related contents accounted for
			copyContents(newParentSiteNode, principal, contentIdsToCopy, siteNodeVO.getRepositoryId(), newParentSiteNode.getRepositoryId(), contentIdsMapping, newCreatedContentVersions, db, ps);
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

	private String getNewNameSuffixForCopy(InfoGluePrincipal principal, Database db, SiteNodeVO newParentSiteNode) throws SystemException, Exception
	{
		LanguageVO newParentLanguageVO = null;
		List<LanguageVO> siteNodeLanguages = LanguageDeliveryController.getLanguageDeliveryController().getLanguagesForSiteNode(db, newParentSiteNode.getSiteNodeId(), principal);
		if (logger.isDebugEnabled())
		{
			logger.debug("Available sitenode languages for new parent sitenode (used for new name suffix). siteNodeLanguages: " + siteNodeLanguages);
		}
		if (siteNodeLanguages != null && siteNodeLanguages.size() > 0)
		{
			newParentLanguageVO = siteNodeLanguages.get(0);
		}
		if (newParentLanguageVO == null)
		{
			newParentLanguageVO = LanguageController.getController().getMasterLanguage(newParentSiteNode.getSiteNodeId(), db);
		}
		if (newParentLanguageVO != null)
		{
			return LabelController.getController(newParentLanguageVO.getLocale()).getString("tool.structuretool.importPage.suffix");
		}
		logger.debug("Did not find a name suffix for copied sitenodes. SiteNode: " + newParentSiteNode.getSiteNodeId());
		return "";
	}

	private void copySiteNodeRecursive(SiteNodeVO siteNode, SiteNodeVO newParentSiteNode, InfoGluePrincipal principal, Map<Integer,Integer> siteNodesIdsMapping, Map<Integer,Integer> contentIdsMapping, Set<Integer> contentIdsToCopy, List<ContentVersion> newCreatedContentVersions, String newNameSuffix, Database db, ProcessBean processBean) throws Exception
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
        if(oldCVVO == null)
        	logger.warn("No meta info version on: " + oldSiteNodeVO.getName() + ":" + oldMetaInfoContentVO.getId());
        RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getLatestActiveContentVersionVO", t.getElapsedTime());

		SiteNodeVO newSiteNodeVO = new SiteNodeVO();
		newSiteNodeVO.setName(oldSiteNodeVO.getName() + newNameSuffix);
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
			newSiteNodeVersionVO.setIsHidden(siteNodeVersionVO.getIsHidden());
			newSiteNodeVersionVO.setSortOrder(siteNodeVersionVO.getSortOrder());
			
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
		    //t.printElapsedTime("copy access");
		}

		//ContentVersion newCV = null;
		List<ContentVersion> newContentVersions = new ArrayList<ContentVersion>();

		Content newMetaInfoContent = SiteNodeController.getController().createSiteNodeMetaInfoContent(db, newSiteNode, null, newParentSiteNode.getRepositoryId(), principal, null, newContentVersions, false, oldMetaInfoContentVO, newNameSuffix);
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("createSiteNodeMetaInfoContent", t.getElapsedTime());

        //ContentVersionVO newCV = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(newMetaInfoContent.getId(), masterLanguage.getId(), db);
        //t.printElapsedTime("newCV:" + newCV);
        
	    logger.info("Adding content version " + newContentVersions.size());
        newCreatedContentVersions.addAll(newContentVersions);

        logger.info("Mapping siteNode " + oldSiteNodeVO.getId() + " to " + newSiteNode.getId());
        siteNodesIdsMapping.put(oldSiteNodeVO.getId(), newSiteNode.getId());

        if(oldCVVO != null)
        {
			String versionValue = oldCVVO.getVersionValue();
			if(newContentVersions.size() > 0)
			{
				ContentVersion newCV = newContentVersions.get(0);
				versionValue = versionValue.replaceFirst(Pattern.quote("<NavigationTitle><![CDATA[") + "(.+?)" + Pattern.quote("]]></NavigationTitle>"), Matcher.quoteReplacement("<NavigationTitle><![CDATA[") + "$1" + newNameSuffix + Matcher.quoteReplacement("]]></NavigationTitle>"));
				newCV.getValueObject().setVersionValue(versionValue);
			}
        }

        for(SiteNodeVO childNode : (Collection<SiteNodeVO>)getChildSiteNodeVOList(siteNode.getId(), false, db, null))
        {
        	copySiteNodeRecursive(childNode, newSiteNode, principal, siteNodesIdsMapping, contentIdsMapping, contentIdsToCopy, newCreatedContentVersions, newNameSuffix, db, processBean);
        }
    }
 	
	private void getRelatedEntities(SiteNodeVO newParentSiteNode, InfoGluePrincipal principal, String versionValue, Integer oldRepositoryId, Integer newRepositoryId, Map<Integer,Integer> siteNodeIdsMapping, Map<Integer,Integer> contentIdsMapping, Set<Integer> siteNodeIdsToCopy, Set<Integer> contentIdsToCopy, List<ContentVersion> versions, int depth, int maxDepth, String newNameSuffix, Database db, ProcessBean processBean) throws Exception
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
							getRelatedEntities(newParentSiteNode, principal, contentVersionVO.getVersionValue(), oldRepositoryId, newRepositoryId, siteNodeIdsMapping, contentIdsMapping, siteNodeIdsToCopy, contentIdsToCopy, versions, depth+1, maxDepth, newNameSuffix, db, processBean);
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
		            
					copySiteNodeRecursive(siteNode, newParentSiteNode, principal, siteNodeIdsMapping, contentIdsMapping, contentIdsToCopy, versions, newNameSuffix, db, processBean);
				}
				else
					logger.info("The related content was outside the old repo so we skip it");
			}
			else
				logger.warn("Skipping related sitenode as it was missing.");
		}
	}

	
	private void copyContents(SiteNodeVO newParentSiteNode, InfoGluePrincipal principal, Set<Integer> contentIdsToCopy, /*String versionValue, */Integer oldRepositoryId, Integer newRepositoryId, Map<Integer,Integer> contentIdsMapping, List<ContentVersion> versions, Database db, PropertySet ps) throws Exception
	{
		copyContents(newParentSiteNode, principal, contentIdsToCopy, oldRepositoryId, newRepositoryId, contentIdsMapping, versions, true, "false", null, db, ps);
	}
	
	private void copyContents(SiteNodeVO newParentSiteNode, InfoGluePrincipal principal, Set<Integer> contentIdsToCopy, /*String versionValue, */Integer oldRepositoryId, Integer newRepositoryId, Map<Integer,Integer> contentIdsMapping, List<ContentVersion> versions, boolean includeRootContentInPath, String onlyLatestVersions, ProcessBean processBean, Database db, PropertySet ps) throws Exception
	{
		logger.info("contentIdsToCopy:" + contentIdsToCopy.size());
		int totalCount = 0;
		int count = 0;
		for(Integer contentId : contentIdsToCopy)
		{
			if(count > 100)
			{
				count = 0;
				if(processBean != null)
					processBean.updateProcess("Copied " + totalCount + " contents so far.");
				db.commit();
				db.begin();
			}
			count++;
			totalCount++;
			logger.info("contentId:" + contentId);
			try
			{
				ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId, db);
				if(contentVO != null && newRepositoryId.intValue() != contentVO.getRepositoryId().intValue())
				{
					logger.info("The contentVO: " + contentVO.getName() + " from repo " + contentVO.getRepositoryId() + " ctd:" + contentVO.getContentTypeDefinitionId());
					if(contentVO.getRepositoryId().intValue() == oldRepositoryId.intValue())
					{
						logger.info("The related content was in the old repo as well - let's copy that as well");
						if(contentIdsMapping.containsKey(contentId))
						{
							logger.info("Allready transferred content so skipping:" + contentVO.getName());
							continue;
						}
						
						String path = ContentController.getContentController().getContentPath(contentId, includeRootContentInPath, false, db);
						logger.info("path:" + path);
						ContentVO copiedContent = ContentController.getContentController().getContentVOWithPath(newRepositoryId, path, true, !contentVO.getIsBranch(), principal, db);
						
			            logger.info("Mapping content " + contentVO.getId() + " to " + copiedContent.getId());
			            contentIdsMapping.put(contentVO.getId(), copiedContent.getId());
						
						copiedContent.setName(contentVO.getName());
						copiedContent.setExpireDateTime(contentVO.getExpireDateTime());
						copiedContent.setIsBranch(contentVO.getIsBranch());
						copiedContent.setIsProtected(contentVO.getIsProtected());
						copiedContent.setPublishDateTime(contentVO.getPublishDateTime());
						copiedContent.setCreatorName(principal.getName());
						copiedContent.setContentTypeDefinitionId(contentVO.getContentTypeDefinitionId());
						
						if(contentVO.getIsBranch())
							ContentController.getContentController().copyContentProperties(ps, contentVO.getId(), copiedContent.getId());
						
					    SiteNodeStateController.getController().copyAccessRights("Content", contentVO.getId(), copiedContent.getId(), db);

					    Map<Integer,Integer> assetIdMap = new HashMap<Integer,Integer>();
					    //Map<Integer,Integer> assetIdMap = new HashMap<Integer,Integer>();
					    //ContentVersionController.getContentVersionController().getLatestContentVersionVOListPerLanguage(contentIds, db);
					    Collection<ContentVersionVO> contentVersionVOList = ContentVersionController.getContentVersionController().getContentVersionVOList(contentVO.getId());
					    
					    if(onlyLatestVersions.equals("true"))
					    {
						    Map<Integer,ContentVersionVO> latestVersions = new HashMap<Integer,ContentVersionVO>();
						    for(ContentVersionVO contentVersionVO : contentVersionVOList)
						    {
						    	if(contentVersionVO.getIsActive() && contentVersionVO.getLanguageId() != null)
						    	{
							    	if(!latestVersions.containsKey(contentVersionVO.getLanguageId()) || latestVersions.get(contentVersionVO.getLanguageId()).getId() < contentVersionVO.getId())
							    	{
							    		latestVersions.put(contentVersionVO.getLanguageId(), contentVersionVO);
							    	}
						    	}
						    }
						    contentVersionVOList.clear();
						    for(Integer key : latestVersions.keySet())
						    	contentVersionVOList.add(latestVersions.get(key));
					    }
					    
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
								ContentCategoryController.getController().createMediumWithDatabase(contentCategoryVO, cc.getCategory(), contentVersion, db);
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

    private void rewireBindingsAndRelationsInTransaction(Map<Integer,Integer> siteNodesIdsMapping, Map<Integer,Integer> contentIdsMapping, List<ContentVersion> versions, Map<String,String> replaceMap, Database db) throws SystemException, Bug 
    {
    	for(ContentVersion version : versions)
    	{
    		logger.info("Rewiring version:" + version.getId() + ":" + version.getVersionValue());
    		String contentVersionValue = version.getVersionValue();

			boolean modified = false;
    		if(replaceMap != null)
    		{
    			int hashBefore = contentVersionValue.hashCode();
	    		Iterator<String> replaceMapIterator = replaceMap.keySet().iterator();
	            while(replaceMapIterator.hasNext())
	            {
					String key = replaceMapIterator.next();
					String value = (String)replaceMap.get(key);
					contentVersionValue = contentVersionValue.replaceAll(key, value);
	            }
	            if(contentVersionValue.hashCode() != hashBefore)
	            	modified = true;
    		}
    		
    		if(!modified &&
    				!contentVersionValue.contains("contentId") && 
    				!contentVersionValue.contains("getInlineAssetUrl") && 
    				!contentVersionValue.contains("languageId") && 
    				!contentVersionValue.contains("entity") && 
    				!contentVersionValue.contains("siteNodeId") && 
    				!contentVersionValue.contains("siteNodeId") && 
    				!contentVersionValue.contains("getPageUrl"))
    			continue;
    			
    		if(contentVersionValue.contains("contentId=\""))
    			contentVersionValue = contentVersionValue.replaceAll("contentId=\"", "contentId=\"oldContentId_");
    		if(contentVersionValue.contains("?contentId="))
        		contentVersionValue = contentVersionValue.replaceAll("\\?contentId=", "\\?contentId=oldContentId_");
    		if(contentVersionValue.contains("getInlineAssetUrl"))
        		contentVersionValue = contentVersionValue.replaceAll("getInlineAssetUrl\\(", "getInlineAssetUrl\\(oldContentId_");
    		if(contentVersionValue.contains("languageId,"))
        		contentVersionValue = contentVersionValue.replaceAll("languageId,", "languageId,oldContentId_");
    		if(contentVersionValue.contains("entity="))
        		contentVersionValue = contentVersionValue.replaceAll("entity=\"Content\" entityId=\"", "entity=\"Content\" entityId=\"oldContentId_");
    		//if(contentVersionValue.contains("entity="))
        		//contentVersionValue = contentVersionValue.replaceAll("entity='Content'><id>", "entity='Content'><id>oldContentId_");
    		if(contentVersionValue.contains("siteNodeId="))
        		contentVersionValue = contentVersionValue.replaceAll("siteNodeId=\"", "siteNodeId=\"oldSiteNodeId_");
    		if(contentVersionValue.contains("detailSiteNodeId="))
        		contentVersionValue = contentVersionValue.replaceAll("detailSiteNodeId=\"", "detailSiteNodeId=\"oldSiteNodeId_");
    		if(contentVersionValue.contains("getPageUrl"))
        		contentVersionValue = contentVersionValue.replaceAll("getPageUrl\\((\\d)", "getPageUrl\\(oldSiteNodeId_$1");
    		if(contentVersionValue.contains("entity="))
        		contentVersionValue = contentVersionValue.replaceAll("entity=\"SiteNode\" entityId=\"", "entity=\"SiteNode\" entityId=\"oldSiteNodeId_");
			
			Iterator<Integer> contentIdMapIterator = contentIdsMapping.keySet().iterator();
	        while (contentIdMapIterator.hasNext()) 
	        {
	            Integer oldContentId = contentIdMapIterator.next();
	            Integer newContentId = contentIdsMapping.get(oldContentId);
	            
	            logger.info("Replacing all:" + oldContentId + " with " + newContentId);
	            
	            if(contentVersionValue.contains("contentId=\""))
	   	            contentVersionValue = contentVersionValue.replaceAll("contentId=\"oldContentId_" + oldContentId + "\"", "contentId=\"" + newContentId + "\"");
	            if(contentVersionValue.contains("?contentId=oldContentId"))
		   	        contentVersionValue = contentVersionValue.replaceAll("\\?contentId=oldContentId_" + oldContentId + "&", "\\?contentId=" + newContentId + "&");
	            if(contentVersionValue.contains("getInlineAssetUrl"))
		   	        contentVersionValue = contentVersionValue.replaceAll("getInlineAssetUrl\\(oldContentId_" + oldContentId + ",", "getInlineAssetUrl\\(" + newContentId + ",");
	            if(contentVersionValue.contains("languageId,oldContentId_"))
		   	        contentVersionValue = contentVersionValue.replaceAll("languageId,oldContentId_" + oldContentId + "\\)", "languageId," + newContentId + "\\)");
	            if(contentVersionValue.contains("entity="))
		   	        contentVersionValue = contentVersionValue.replaceAll("entity=\"Content\" entityId=\"oldContentId_" + oldContentId + "\"", "entity=\"Content\" entityId=\"" + newContentId + "\"");
	            if(contentVersionValue.contains("<id>oldContentId_"))
		   	        contentVersionValue = contentVersionValue.replaceAll("<id>oldContentId_" + oldContentId + "</id>", "<id>" + newContentId + "</id>");
	        }
	        
	        Iterator<Integer> siteNodeIdMapIterator = siteNodesIdsMapping.keySet().iterator();
	        while (siteNodeIdMapIterator.hasNext()) 
	        {
	            Integer oldSiteNodeId = siteNodeIdMapIterator.next();
	            Integer newSiteNodeId = siteNodesIdsMapping.get(oldSiteNodeId);
	            
	            logger.info("Replacing all:" + oldSiteNodeId + " with " + newSiteNodeId);
	            
	            if(contentVersionValue.contains("siteNodeId="))
	            	contentVersionValue = contentVersionValue.replaceAll("siteNodeId=\"oldSiteNodeId_" + oldSiteNodeId + "\"", "siteNodeId=\"" + newSiteNodeId + "\"");
	            if(contentVersionValue.contains("detailSiteNodeId="))
	            	contentVersionValue = contentVersionValue.replaceAll("detailSiteNodeId=\"oldSiteNodeId_" + oldSiteNodeId + "\"", "detailSiteNodeId=\"" + newSiteNodeId + "\"");
	            if(contentVersionValue.contains("getPageUrl"))
	            	contentVersionValue = contentVersionValue.replaceAll("getPageUrl\\(oldSiteNodeId_" + oldSiteNodeId + ",", "getPageUrl\\(" + newSiteNodeId + ",");
	            if(contentVersionValue.contains("entity="))
	            	contentVersionValue = contentVersionValue.replaceAll("entity=\"SiteNode\" entityId=\"oldSiteNodeId_" + oldSiteNodeId + "\"", "entity=\"SiteNode\" entityId=\"" + newSiteNodeId + "\"");
	            if(contentVersionValue.contains("<id>oldSiteNodeId_"))
	            	contentVersionValue = contentVersionValue.replaceAll("<id>oldSiteNodeId_" + oldSiteNodeId + "</id>", "<id>" + newSiteNodeId + "</id>");
	        }
	        
	        //Now replace all occurrances of old as they are stuff not moved and should be restored.
	        if(contentVersionValue.contains("oldContentId_"))
	        	contentVersionValue = contentVersionValue.replaceAll("oldContentId_", "");
	        if(contentVersionValue.contains("oldSiteNodeId_"))
	        	contentVersionValue = contentVersionValue.replaceAll("oldSiteNodeId_", "");
	        
	        version.setVersionValue(contentVersionValue);
	        
	        ContentVersionController.getContentVersionController().getMediumContentVersionWithId(version.getId(), db).setVersionValue(contentVersionValue);
    	}
    }

    public void markForDeletion(SiteNodeVO siteNodeVO, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException
    {
    	markForDeletion(siteNodeVO, infogluePrincipal, false);
    }

    public void markForDeletion(SiteNodeVO siteNodeVO, InfoGluePrincipal infogluePrincipal, boolean forceDelete) throws ConstraintException, SystemException
    {
    	Map<SiteNodeVO, List<ReferenceBean>> contactPersons = new HashMap<SiteNodeVO, List<ReferenceBean>>();

    	Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {
			markForDeletion(siteNodeVO, db, forceDelete, infogluePrincipal, contactPersons, ProcessBean.createProcessBean("Dummy", "DeleteContent"));

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
        logger.info("Number of people:" + contactPersons.size());
		if (contactPersons.size() > 0)
		{
			logger.info("Will notifiy people about SiteNode removals. Number of nodes: " + contactPersons.size());
			Database contactDb = CastorDatabaseService.getDatabase();
			try
	        {
				beginTransaction(contactDb);
				notifyContactPersonsForSiteNode(contactPersons, contactDb);
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

	public void markForDeletion(SiteNodeVO siteNodeVO, Database db, boolean forceDelete, InfoGluePrincipal infogluePrincipal, ProcessBean processBean) throws ConstraintException, SystemException, Exception
	{
		markForDeletion(siteNodeVO, db, forceDelete, infogluePrincipal, new HashMap<SiteNodeVO, List<ReferenceBean>>(), processBean);
	}

	public void markForDeletion(SiteNodeVO siteNodeVO, Database db, InfoGluePrincipal infogluePrincipal, ProcessBean processBean) throws ConstraintException, SystemException, Exception
	{
		markForDeletion(siteNodeVO, db, false, infogluePrincipal, new HashMap<SiteNodeVO, List<ReferenceBean>>(), processBean);
	}

	/**
	 * This method deletes a siteNode and also erases all the children and all versions.
	 */
	public void markForDeletion(SiteNodeVO siteNodeVO, Database db, boolean forceDelete, InfoGluePrincipal infogluePrincipal, Map<SiteNodeVO, List<ReferenceBean>> contactPersons, ProcessBean processBean) throws ConstraintException, SystemException, Exception
	{
		boolean notifyResponsibleOnReferenceChange = CmsPropertyHandler.getNotifyResponsibleOnReferenceChange();

		List<Integer> siteNodeIds = getSiteNodeIdsForAllChildren(siteNodeVO.getId(), db);
		siteNodeIds.add(siteNodeVO.getId());
		if(logger.isDebugEnabled())
        	logger.info("siteNodeIds:" + siteNodeIds.size());

		processBean.updateProcess("Moving " + siteNodeIds.size() + " pages to trashcan");

		int i = 0;
		for(Integer childSiteNodeId : siteNodeIds)
		{
			i++;
			if(i % 1000 == 0)
			{
				processBean.updateProcess("Moved " + i + " pages to trashcan");
			}

			try
			{
				SmallSiteNodeImpl siteNode = getSmallSiteNodeWithId(childSiteNodeId, db);
		        if(logger.isDebugEnabled())
		        	logger.info("Marking " + siteNode.getName() + " for delete");

		        List<ReferenceBean> referenceBeanList = RegistryController.getController().getReferencingObjectsForSiteNode(siteNode.getId(), -1, false, db);
				if(referenceBeanList != null && referenceBeanList.size() > 0 && !forceDelete)
					throw new ConstraintException("SiteNode.stateId", "3405", "<br/><br/>" + siteNode.getName() + " (" + siteNode.getId() + ")");

				boolean isDeletable = true;
		        if(!forceDelete)
		        	isDeletable = getIsDeletable(siteNode.getValueObject(), infogluePrincipal, db);
		        
				if(forceDelete || isDeletable)
			    {
					siteNode.setIsDeleted(true);
					boolean clean = true;
					if (notifyResponsibleOnReferenceChange)
					{
						clean = false;
					}

					List<ReferenceBean> contactList = RegistryController.getController().deleteAllForSiteNode(siteNode.getSiteNodeId(), infogluePrincipal, clean, CmsPropertyHandler.getOnlyShowReferenceIfLatestVersion(), db);
					if (notifyResponsibleOnReferenceChange)
					{
						if (contactList != null)
						{
							logger.info("Found " + contactList.size() + " people to notify about SiteNode removal. SiteNode.id: " + siteNode.getSiteNodeId());
							contactPersons.put(siteNode.getValueObject(), contactList);
						}
					}
			    }
			    else
		    	{
		    		throw new ConstraintException("SiteNodeVersion.stateId", "3400");
		    	}
			}
			catch(SystemException e)
			{
				e.printStackTrace();
				logger.warn("Problem marking content: " + childSiteNodeId + " as deleted. Message: " + e.getMessage(), e);
			}
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
        	SmallSiteNodeImpl siteNode = getSmallSiteNodeWithId(siteNodeId, db);
        	//SiteNode siteNode = getSiteNodeWithId(siteNodeId, db);
        	siteNode.setIsDeleted(false);
	    	
        	if(siteNode.getParentSiteNodeId() != null)
	        {
	        	SmallSiteNodeImpl parentSiteNode = getSmallSiteNodeWithId(siteNode.getParentSiteNodeId(), db);
				while(parentSiteNode != null && parentSiteNode.getIsDeleted())
				{
					parentSiteNode.setIsDeleted(false);
					parentSiteNode = getSmallSiteNodeWithId(siteNode.getParentSiteNodeId(), db);
				}
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
	 * @param infoGluePrincipal 
	 */
	
	public List<SiteNodeVO> getSiteNodeVOListMarkedForDeletion(Integer repositoryId, InfoGluePrincipal infoGluePrincipal, List<RepositoryVO> excludeReposVOList) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();
		
		List<SiteNodeVO> siteNodeVOListMarkedForDeletion = new ArrayList<SiteNodeVO>();
		
		try 
		{
			beginTransaction(db);
		
			int startIndex = 2;
			if(repositoryId != null && repositoryId != -1)
				startIndex++;
			
			StringBuffer excludeReposWithIdSQL = new StringBuffer("");
			if(excludeReposVOList != null && excludeReposVOList.size() > 0)
			{
				excludeReposWithIdSQL.append(" AND NOT (sn.repositoryId IN LIST (");
				for(int i=0; i<excludeReposVOList.size(); i++)
					excludeReposWithIdSQL.append("" + (i>0 ? "," : "") + "$"+(startIndex+i));
				excludeReposWithIdSQL.append("))");
			}

			String sql = "SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl sn WHERE sn.isDeleted = $1 " + excludeReposWithIdSQL + " ORDER BY sn.siteNodeId";
			if(repositoryId != null && repositoryId != -1)
				sql = "SELECT sn FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl sn WHERE sn.isDeleted = $1 AND sn.repositoryId = $2 " + excludeReposWithIdSQL + " ORDER BY sn.siteNodeId";
			
			OQLQuery oql = db.getOQLQuery(sql);
			oql.bind(true);
			if(repositoryId != null && repositoryId != -1)
				oql.bind(repositoryId);
				
			for(RepositoryVO excludedRepoVO : excludeReposVOList)
				oql.bind(excludedRepoVO.getId());

			QueryResults results = oql.execute(Database.READONLY);
			while(results.hasMore()) 
            {
				SmallSiteNodeImpl siteNode = (SmallSiteNodeImpl)results.next();
				Integer siteNodeRepositoryId = siteNode.getValueObject().getRepositoryId();
				RepositoryVO repositoryVO = null;
				
				try
				{
					repositoryVO = RepositoryController.getController().getRepositoryVOWithId(siteNodeRepositoryId, db);
				}
				catch(Exception e)
				{
					logger.warn("There was a repo referenced that is allready deleted. Skipping rights deletion.");
				}
				Integer siteNodeId = siteNode.getId();

				if((AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "Repository.Read", siteNodeRepositoryId.toString()) && AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "Repository.Write", siteNodeRepositoryId.toString()))
					&& (AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "SiteNodeVersion.Read", siteNodeId.toString()) && AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "SiteNodeVersion.Write", siteNodeId.toString())))
				{
					if(repositoryVO != null)
						siteNode.getValueObject().getExtraProperties().put("repositoryMarkedForDeletion", repositoryVO.getIsDeleted());
					siteNodeVOListMarkedForDeletion.add(siteNode.getValueObject());
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

        	QueryResults results = oql.execute(Database.READONLY);
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

        	QueryResults results = oql.execute(Database.READONLY);
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

    public void changeSiteNodeSortOrder(Integer siteNodeId, Integer beforeSiteNodeId, String direction, InfoGluePrincipal infoGluePrincipal, Integer sortLanguageId) throws ConstraintException, SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();

        beginTransaction(db);

        try
        {
        	SiteNodeVO siteNodeVO = getSiteNodeVOWithId(siteNodeId, db);
			//Fixes a nice ordered list
			List<SiteNodeVO> childrenVOList = SiteNodeController.getController().getChildSiteNodeVOList(siteNodeVO.getParentSiteNodeId(), false, db, sortLanguageId);
			Iterator<SiteNodeVO> childrenVOListIterator = childrenVOList.iterator();
			int index = 0;
			while(childrenVOListIterator.hasNext())
			{
				SiteNodeVO childSiteNodeVO = childrenVOListIterator.next();
				SiteNodeVersion latestChildSiteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersion(db, childSiteNodeVO.getId());
				logger.info("latestChildSiteNodeVersion.getSortOrder():" + latestChildSiteNodeVersion.getSortOrder() + "=" + index);
				if(latestChildSiteNodeVersion.getSortOrder() != index)
				{
					latestChildSiteNodeVersion = SiteNodeStateController.getController().updateStateId(latestChildSiteNodeVersion, SiteNodeVersionVO.WORKING_STATE, "Changed sortOrder", infoGluePrincipal, db);
	
					//Integer currentSortOrder = latestChildSiteNodeVersion.getSortOrder();
					//logger.info("currentSortOrder:" + currentSortOrder + " on " + childSiteNodeVO.getName());
					//if(currentSortOrder != 100)
					//{
					logger.info("Setting sort order to:" + index + " on " + latestChildSiteNodeVersion.getId());
					latestChildSiteNodeVersion.setSortOrder(index);
					
					if(CmsPropertyHandler.getAllowLocalizedSortAndVisibilityProperties())
					{
						ContentVersionVO cvVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(childSiteNodeVO.getMetaInfoContentId(), sortLanguageId, db);	
						if(cvVO != null)
							ContentVersionController.getContentVersionController().updateAttributeValue(cvVO.getContentVersionId(), "SortOrder", "" + index, infoGluePrincipal, db, true);
					}
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
            	Integer oldSortOrder = 0;
            	Integer newSortOrder = 0;

                SiteNodeVO siteNodeVO = getSiteNodeVOWithId(siteNodeId, db);
            	SiteNodeVO beforeSiteNodeVO = getSiteNodeVOWithId(beforeSiteNodeId, db);
            	if(beforeSiteNodeVO.getParentSiteNodeId().intValue() != siteNodeVO.getParentSiteNodeId().intValue())
            	{
            		logger.info("Was new parent - let's fix that as well");
            	}
            	else
            	{
            		logger.info("Parent was the same...");

            		List<SiteNodeVO> newChildSiteNodeList = new ArrayList<SiteNodeVO>();
                	
            		int insertPosition = 0;
            		List<SiteNodeVO> childrenVOList = SiteNodeController.getController().getChildSiteNodeVOList(siteNodeVO.getParentSiteNodeId(), false, db, sortLanguageId);
    				Iterator<SiteNodeVO> childrenVOListIterator = childrenVOList.iterator();
    				while(childrenVOListIterator.hasNext())
    				{
    					SiteNodeVO childSiteNodeVO = childrenVOListIterator.next();
    					if(childSiteNodeVO.getSiteNodeId().equals(beforeSiteNodeId))
    					{
    						insertPosition = newChildSiteNodeList.size();
    					}

    					if(!childSiteNodeVO.getSiteNodeId().equals(siteNodeId))
    					{
    						newChildSiteNodeList.add(childSiteNodeVO);
    					}
    				}

    				newChildSiteNodeList.add(insertPosition, siteNodeVO);
    				
    				Iterator<SiteNodeVO> newChildSiteNodeListIterator = newChildSiteNodeList.iterator();
    				int i=0;
    				int highestSortOrder = 0;
    				while(newChildSiteNodeListIterator.hasNext())
    				{
    					logger.info("i:" + i);
    					logger.info("highestSortOrder:" + highestSortOrder);
    					SiteNodeVO orderedSiteNodeVO = newChildSiteNodeListIterator.next();
    					
    					List events = new ArrayList();
    					SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(db, orderedSiteNodeVO.getId());
    					Integer localizedSortOrder = 100;
    					if(CmsPropertyHandler.getAllowLocalizedSortAndVisibilityProperties())
						{
    						ContentVersionVO cvVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(orderedSiteNodeVO.getMetaInfoContentId(), sortLanguageId, db);	
    						if(cvVO != null)
    						{
    							String localizedSortOrderString = ContentVersionController.getContentVersionController().getAttributeValue(cvVO.getId(), "SortOrder", false);
    							if(localizedSortOrderString != null && !localizedSortOrderString.equals(""))
    								localizedSortOrder = new Integer(localizedSortOrderString);
    						}
    					}
    					
    					//if(siteNodeVersionVO.getSortOrder() < highestSortOrder || localizedSortOrder < highestSortOrder)
    					if(siteNodeVersionVO.getSortOrder() < highestSortOrder || localizedSortOrder < highestSortOrder || i != localizedSortOrder)
    	    			{
    						if(!siteNodeVersionVO.getStateId().equals(SiteNodeVersionVO.WORKING_STATE))
    						{	
	    						siteNodeVersionVO = SiteNodeStateController.getController().changeState(siteNodeVersionVO.getId(), SiteNodeVersionVO.WORKING_STATE, "Changed sortOrder", false, infoGluePrincipal, siteNodeVersionVO.getSiteNodeId(), events);
    						}
    						siteNodeVersionVO.setSortOrder(i);
    						
    						if(CmsPropertyHandler.getAllowLocalizedSortAndVisibilityProperties())
    						{
	    						ContentVersionVO cvVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(orderedSiteNodeVO.getMetaInfoContentId(), sortLanguageId, db);	
	    						if(cvVO != null)
	    							ContentVersionController.getContentVersionController().updateAttributeValue(cvVO.getContentVersionId(), "SortOrder", "" + i, infoGluePrincipal, db, true);
    						}

    						SiteNodeVersionController.getController().update(siteNodeVersionVO);
    					}
    					else
    						logger.info("No action - sort order was ok");
    					/*
    					if(CmsPropertyHandler.getAllowLocalizedSortAndVisibilityProperties())
						{
	    					if(localizedSortOrder < 100 && localizedSortOrder > highestSortOrder)
	    						highestSortOrder = localizedSortOrder;
	    				
	    					if(highestSortOrder > i)
	    						i = highestSortOrder;
    					}
    					else
    					{
    						highestSortOrder = siteNodeVersionVO.getSortOrder();
        					if(highestSortOrder > i)
	    						i = highestSortOrder;
    					}
    					*/
    					i++;
    				}
            	}
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
				List<SiteNodeVO> childrenVOList = SiteNodeController.getController().getChildSiteNodeVOList(siteNodeVO.getParentSiteNodeId(), false, db, sortLanguageId);
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
						latestChildSiteNodeVersion = SiteNodeStateController.getController().updateStateId(latestChildSiteNodeVersion, SiteNodeVersionVO.WORKING_STATE, "Changed sortOrder", infoGluePrincipal, db);
						latestChildSiteNodeVersion.setSortOrder(newSortOrder);
						//logger.info("Changed sort order on:" + latestChildSiteNodeVersion.getId() + " into " + newSortOrder);

						if(CmsPropertyHandler.getAllowLocalizedSortAndVisibilityProperties())
						{
							ContentVersionVO cvVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(childSiteNodeVO.getMetaInfoContentId(), sortLanguageId, db);	
							if(cvVO != null)
								ContentVersionController.getContentVersionController().updateAttributeValue(cvVO.getContentVersionId(), "SortOrder", "" + newSortOrder, infoGluePrincipal, db, true);
						}
					}
					else if(currentSortOrder.equals(newSortOrder))
					{
						latestChildSiteNodeVersion = SiteNodeStateController.getController().updateStateId(latestChildSiteNodeVersion, SiteNodeVersionVO.WORKING_STATE, "Changed sortOrder", infoGluePrincipal, db);
						latestChildSiteNodeVersion.setSortOrder(oldSortOrder);
						//logger.info("Changed sort order on:" + latestChildSiteNodeVersion.getId() + " into " + oldSortOrder);

						if(CmsPropertyHandler.getAllowLocalizedSortAndVisibilityProperties())
						{
							ContentVersionVO cvVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(childSiteNodeVO.getMetaInfoContentId(), sortLanguageId, db);	
							if(cvVO != null)
								ContentVersionController.getContentVersionController().updateAttributeValue(cvVO.getContentVersionId(), "SortOrder", "" + oldSortOrder, infoGluePrincipal, db, true);
						}
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

    public SiteNodeVO update(SiteNodeVO siteNodeVO, Database db) throws ConstraintException, SystemException
    {
    	SiteNode siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeVO.getId(), db);
		siteNode.setVO(siteNodeVO);

        return siteNode.getValueObject();
    }        
    
	/**
	 * This method moves a siteNode after first making a couple of controls that the move is valid.
	 */
	
    public void toggleSiteNodeHidden(Integer siteNodeId, InfoGluePrincipal infoGluePrincipal, Integer sortLanguageId) throws ConstraintException, SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

    	if(CmsPropertyHandler.getAllowLocalizedSortAndVisibilityProperties())
		{
	        beginTransaction(db);
	
	        try
	        {
				SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId, db);
				ContentVersionVO cvVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(siteNodeVO.getMetaInfoContentId(), sortLanguageId, db);	
				if(cvVO != null)
				{
					String hideInNavigation = ContentVersionController.getContentVersionController().getAttributeValue(cvVO, "HideInNavigation", false);
					//System.out.println("hideInNavigation:" + hideInNavigation + " on " + sortLanguageId + "/" + cvVO.getContentVersionId());
					if(hideInNavigation == null || hideInNavigation.equals("") || hideInNavigation.equalsIgnoreCase("false"))
						ContentVersionController.getContentVersionController().updateAttributeValue(cvVO.getContentVersionId(), "HideInNavigation", "" + true, infoGluePrincipal, db);
					else
						ContentVersionController.getContentVersionController().updateAttributeValue(cvVO.getContentVersionId(), "HideInNavigation", "" + false, infoGluePrincipal, db);
				}
	            
	            commitTransaction(db);
	        }
	        catch(Exception e)
	        {
	            logger.error("An error occurred so we should not complete the transaction:" + e, e);
	            rollbackTransaction(db);
	            throw new SystemException(e.getMessage());
	        }
		}
        
        db = CastorDatabaseService.getDatabase();

        beginTransaction(db);

        try
        {
            SiteNodeVersion latestSiteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersion(db, siteNodeId);
            //logger.info("latestSiteNodeVersion:" + latestSiteNodeVersion);
            if(latestSiteNodeVersion != null)
			{
        		latestSiteNodeVersion = SiteNodeStateController.getController().updateStateId(latestSiteNodeVersion, SiteNodeVersionVO.WORKING_STATE, "Changed hidden", infoGluePrincipal, db);
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

	private Map<String, Map<SiteNodeVO, List<ReferenceBean>>> groupByContactPerson(Map<SiteNodeVO, List<ReferenceBean>> contactPersons)
	{
		Map<String, Map<SiteNodeVO, List<ReferenceBean>>> result = new HashMap<String, Map<SiteNodeVO,  List<ReferenceBean>>>();
		for (Map.Entry<SiteNodeVO, List<ReferenceBean>> entry : contactPersons.entrySet())
		{
			SiteNodeVO siteNodeVO = entry.getKey();
			Map<String, List<ReferenceBean>> referencesByContact = groupByContactPerson(entry.getValue());
			for (Map.Entry<String, List<ReferenceBean>> contactsForSiteNode : referencesByContact.entrySet())
			{
				String contactPerson = contactsForSiteNode.getKey();
				Map<SiteNodeVO,  List<ReferenceBean>> value = result.get(contactPerson);
				if (value == null)
				{
					value = new HashMap<SiteNodeVO,  List<ReferenceBean>>();
					result.put(contactPerson, value);
				}
				value.put(siteNodeVO, contactsForSiteNode.getValue());
			}
		}
		return result;
	}

	private void notifyContactPersonsForSiteNode(SiteNodeVO siteNodeVO, List<ReferenceBean> contacts, Database db) throws SystemException, Exception
	{
		notifyContactPersonsForSiteNode(Collections.singletonMap(siteNodeVO, contacts), db);
	}

	private void notifyContactPersonsForSiteNode(Map<SiteNodeVO, List<ReferenceBean>> contacts, Database db) throws SystemException, Exception
	{
		Map<String, Map<SiteNodeVO, List<ReferenceBean>>> contactMap = groupByContactPerson(contacts);
		if (logger.isInfoEnabled())
		{
			logger.info("Will notify people about registry change. " + contactMap);
		}
		String registryContactMailLanguage = CmsPropertyHandler.getRegistryContactMailLanguage();
		Locale locale = new Locale(registryContactMailLanguage);
		LabelController lc = LabelController.getController(locale);
		try
		{
			String from = CmsPropertyHandler.getSystemEmailSender();
			String subject = getLocalizedString(locale, "tool.structuretool.registry.notificationEmail.subject");

			// This loop iterate once for each contact person
			for (Map.Entry<String, Map<SiteNodeVO, List<ReferenceBean>>> entry : contactMap.entrySet())
			{
				String contactPersonEmail = entry.getKey();
				Map<String,Object> parameters = new HashMap<String,Object>();
				Set<SiteNodeVO> siteNodesForPerson = entry.getValue().keySet();
				Map<SiteNodeVO, List<ReferenceBean>> affectedNodes = entry.getValue();

				parameters.put("removedSiteNodes", siteNodesForPerson);
				parameters.put("baseURL", CmsPropertyHandler.getCmsFullBaseUrl());
				parameters.put("db", db);
				parameters.put("this", this);
				parameters.put("ui", lc);

				Map<String, Map<String, List<ReferenceBean>>> nodes = new HashMap<String, Map<String,List<ReferenceBean>>>();
				boolean hasInformation = false;
				for (Map.Entry<SiteNodeVO, List<ReferenceBean>> affectedNode : affectedNodes.entrySet())
				{
					String removedSiteNodePath = getSiteNodePath(affectedNode.getKey(), false, true, db);
					List<ReferenceBean> affectedContents = new LinkedList<ReferenceBean>();
					List<ReferenceBean> affectedSiteNodes = new LinkedList<ReferenceBean>();

					for (ReferenceBean reference : affectedNode.getValue())
					{
						if (reference.getReferencingCompletingObject().getClass().getName().indexOf("Content") != -1)
						{
							affectedContents.add(reference);
						}
						else
						{
							affectedSiteNodes.add(reference);
						}
					}

					Map<String, List<ReferenceBean>> nodeMap = new HashMap<String, List<ReferenceBean>>();
					nodeMap.put("content", affectedContents);
					nodeMap.put("siteNode", affectedSiteNodes);
					hasInformation = hasInformation || !affectedContents.isEmpty() || !affectedSiteNodes.isEmpty();
					nodes.put(removedSiteNodePath, nodeMap);
				} // end loop: one SiteNode for one contact person
				parameters.put("affectedNodes", nodes);

				if (hasInformation)
				{
					logger.debug("Sending notification email to: " + contactPersonEmail);
					String emailTemplate = getDeleteNotificationEmailTemplate();
					if (logger.isTraceEnabled())
					{
						logger.trace("Generating delete notification email with parameters: " + parameters);
					}
					String email = generateEmailBodyForDeleteNotification(emailTemplate, parameters);
					MailServiceFactory.getService().sendEmail("text/html", from, contactPersonEmail, null, null, null, null, subject, email, "utf-8");
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

	private String generateEmailBodyForDeleteNotification(String template, Map<String, Object> parameters) throws Exception
	{
		StringWriter tempString = new StringWriter();
		PrintWriter pw = new PrintWriter(tempString);
		new VelocityTemplateProcessor().renderTemplate(parameters, pw, template, true);
		String email = tempString.toString();
		return email;
	}

	private String getDeleteNotificationEmailTemplate() throws Exception
	{
		String template;
		String contentType = CmsPropertyHandler.getMailContentType();
		if(contentType == null || contentType.length() == 0)
		{
			contentType = "text/html";
		}
		if(contentType.equalsIgnoreCase("text/plain"))
		{
			template = FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "cms/structuretool/deletedEntityNotification_plain.vm"));
		}
		else
		{
			template = FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "cms/structuretool/deletedEntityNotification_html.vm"));
		}
		return template;
	}

	public List<String> getErroneousSiteNodeNames(List<Integer> erroneousSiteNodeIds) throws Exception, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
	
		List<String> erroneousSiteNodePaths = new ArrayList<String>();
		try
		{
			beginTransaction(db);
			
			for (Integer siteNodeId : erroneousSiteNodeIds)
			{
				try
				{
					erroneousSiteNodePaths.add(SiteNodeController.getController().getSiteNodePath(siteNodeId, false, false, db));
					erroneousSiteNodePaths.add(SiteNodeController.getController().getSiteNodePath(siteNodeId, db));
				}
				catch (Exception e)
				{
					erroneousSiteNodePaths.add("Failed to compute SiteNode path for erroneous SiteNode with id: " + siteNodeId);
				}
			}
			commitTransaction(db);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			rollbackTransaction(db);
			throw new SystemException("Error getting faulty paths: " + e.getMessage(), e);
		}
		
		return erroneousSiteNodePaths;
	}
	
	public boolean getDoesSiteNodeExist(Integer siteNodeId) throws Exception
	{
		boolean exists = true;
		
		Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {	
			db.load(SmallSiteNodeImpl.class, siteNodeId, Database.READONLY);
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
	
	/**
	 * This method returns a list of all languages available on the current site/repository.
	 */
	
	public Boolean getIsLanguageAvailable(Integer siteNodeId, Integer languageId, Database db, InfoGluePrincipal principal)
	{
		if(languageId == null)
			return true;
		
		Boolean isLanguageAvailable = false;
		
		try
		{
			List<LanguageVO> availableLanguages = LanguageDeliveryController.getLanguageDeliveryController().getLanguagesForSiteNode(db, siteNodeId, principal);
			logger.info("languageId:" + languageId);
			for(LanguageVO language : availableLanguages)
			{
				logger.info("language.getId():" + language.getId());
				if(language.getId().equals(languageId))
				{
					isLanguageAvailable = true;
					break;
				}
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get all available languages:" + e.getMessage(), e);
		}
				
		return isLanguageAvailable;
	}
	
 	//Copy logic
 	
    public void copyRepository(RepositoryVO oldRepositoryVO, RepositoryVO repositoryVO, InfoGluePrincipal principal, String onlyLatestVersions, String standardReplacement, Map<String,String> replaceMap, ProcessBean processBean, Database db, PropertySet ps) throws ConstraintException, SystemException, Exception
    {
    	Timer t = new Timer();
    	
    	logger.info("repositoryVO:" + repositoryVO);
    	logger.info("principal:" + principal);
        logger.info("oldRepositoryVO:" + oldRepositoryVO);
        SiteNodeVO siteNode = SiteNodeController.getController().getRootSiteNodeVO(oldRepositoryVO.getId(), db);
        logger.info("siteNode:" + siteNode);
		SiteNodeVO newParentSiteNode = new SiteNodeVO();
		newParentSiteNode.setRepositoryId(repositoryVO.getId());
		
		processBean.updateProcess("Checked for constraints");
		
		Map<Integer,Integer> siteNodeIdsMapping = new HashMap<Integer,Integer>();

		Map<Integer,Integer> contentIdsMapping = new HashMap<Integer,Integer>();
		Set<Integer> siteNodeIdsToCopy = new HashSet<Integer>();
		Set<Integer> contentIdsToCopy = new HashSet<Integer>();
		List<ContentVersion> newCreatedContentVersions = new ArrayList<ContentVersion>();

		String newNameSuffix = ""; //getNewNameSuffixForCopy(principal, db, newParentSiteNode);
		
		copySiteNodeRecursive(siteNode, newParentSiteNode, principal, siteNodeIdsMapping, contentIdsMapping, contentIdsToCopy, newCreatedContentVersions, newNameSuffix, db, processBean);
		//After this all sitenodes main should be copied but then we have to round up some related nodes later
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("copySiteNodeRecursive", t.getElapsedTime());
		
		db.commit();
		db.begin();
		
		processBean.updateProcess("Now we search for related contents to copy");
		//Now let's go through all contents
		for(ContentVersion version : newCreatedContentVersions)
        {
			getRelatedEntities(newParentSiteNode, principal, version.getVersionValue(), oldRepositoryVO.getId(), newParentSiteNode.getRepositoryId(), siteNodeIdsMapping, contentIdsMapping, siteNodeIdsToCopy, contentIdsToCopy, newCreatedContentVersions, 0, 3, newNameSuffix, db, processBean);
        	//getContentRelationsChain(siteNodesIdsMapping, contentIdsMapping, newParentSiteNode.getRepository().getId(), oldSiteNodeVO.getRepositoryId(), principal, versions, 0, 3, db);
        }
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getRelatedEntities", t.getElapsedTime());
        
		processBean.updateProcess("Copying " + contentIdsToCopy.size() + " contents");
		
		db.commit();
		db.begin();

		List<ContentVO> contentVOListForRepository = ContentController.getContentController().getRepositoryContentVOList(oldRepositoryVO.getId(), db);
		for(ContentVO contentVO : contentVOListForRepository)
		{
			contentIdsToCopy.add(contentVO.getId());
		}
		processBean.updateProcess("Analyzed all contents to copy. Was " + contentIdsToCopy.size());
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("fetched all repository contents", t.getElapsedTime());
			
		db.commit();
		db.begin();

		//After this all related sitenodes should have been created and all related contents accounted for
		copyContents(newParentSiteNode, principal, contentIdsToCopy, oldRepositoryVO.getId(), newParentSiteNode.getRepositoryId(), contentIdsMapping, newCreatedContentVersions, false, onlyLatestVersions, processBean, db, ps);
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("copyContents", t.getElapsedTime());

		db.commit();
		db.begin();
		
		processBean.updateProcess("Remapping relations");
		rewireBindingsAndRelationsInTransaction(siteNodeIdsMapping, contentIdsMapping, newCreatedContentVersions, replaceMap, db);
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("rewireBindingsAndRelations", t.getElapsedTime());
    }
	
	public List<LanguageVO> getEnabledLanguageVOListForSiteNode(Integer siteNodeId) throws SystemException, Exception
	{ 
		SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(siteNodeId);
	
		SiteNodeVO parentSiteNodeVO;
		/*recursiv search for enabled languages*/
		while (siteNodeVersionVO != null && siteNodeVersionVO.getDisableLanguages() == 2) {
			parentSiteNodeVO = getParentSiteNode(siteNodeId);
			siteNodeVersionVO  = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(parentSiteNodeVO.getId());
		}
		logger.info("Disabled languages for siteNodeVersion:" + siteNodeVersionVO.getDisableLanguages());
		logger.info("The chosen sitenodeId:" + siteNodeVersionVO.getSiteNodeId());
		
		List<LanguageVO> enabledPageLanguages = new ArrayList<LanguageVO>();

    	Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	   
	    String enabledLanguages = "" + ps.getString("siteNode_" + siteNodeVersionVO.getSiteNodeId() + "_enabledLanguages");
    	
    	SiteNodeVO siteNodeVO = getSiteNodeVOWithId(siteNodeVersionVO.getSiteNodeId());
		String[] enabledLanguageIdStringList = enabledLanguages.split(",");
		//System.out.println("Enabled siteNodeVO.getRepositoryId() siteNodeId:" + siteNodeVO.getRepositoryId());
		List<LanguageVO> repositoryLanguageVOList = LanguageController.getController().getLanguageVOList(siteNodeVO.getRepositoryId());

		//System.out.println("Enabled repositoryLanguageVOList siteNodeId:" + repositoryLanguageVOList);
		Integer enabledLanguageId = null;
		LanguageVO enabledLanguageVO;
		
		if (enabledLanguageIdStringList[0].equalsIgnoreCase("")) 
		{
			enabledPageLanguages.addAll(repositoryLanguageVOList);
		} 
		else 
		{
			for (String enabledLanguageIdString : enabledLanguageIdStringList) 
			{
				//System.out.println("Enabled enabledLanguageIdString:" + enabledLanguageIdString);
				enabledLanguageId = Integer.parseInt(enabledLanguageIdString);
				enabledLanguageVO = (LanguageVO) LanguageController.getController().getLanguageVOWithId(enabledLanguageId);
				enabledPageLanguages.add(enabledLanguageVO);
			}
		}
	
		return enabledPageLanguages;
	}

	public List<LanguageVO> getDisabledLanguageVOListForSiteNode(Integer siteNodeId) throws SystemException, Exception
	{ 

    	List<LanguageVO> enabledLanguageVOList = getEnabledLanguageVOListForSiteNode(siteNodeId);

    	SiteNodeVO siteNodeVO = getSiteNodeVOWithId(siteNodeId);
    	List<LanguageVO> repositoryLanguageVOList = LanguageController.getController().getLanguageVOList(siteNodeVO.getRepositoryId());

    	List<LanguageVO> filteredRepositoryLanguageVOList = new ArrayList<LanguageVO>();
    	filteredRepositoryLanguageVOList.addAll(repositoryLanguageVOList);
    	filteredRepositoryLanguageVOList.removeAll(enabledLanguageVOList);

		return filteredRepositoryLanguageVOList;
	}

	public SiteNodeVO getBaseForLanguageSiteNodeVO(Integer siteNodeId, Database db) throws Bug, Exception 
	{
		SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId, db);
		
		SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(db, siteNodeVO.getSiteNodeId());
		while(siteNodeVO != null && siteNodeVersionVO.getDisableLanguages() == 2)
		{
			if(siteNodeVO != null && siteNodeVO.getParentSiteNodeId() != null) {

				siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVO.getParentSiteNodeId(), db);
				if(siteNodeVO != null) 
				{
					siteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(db, siteNodeVO.getSiteNodeId());
				}
			} 
			else 
			{
				siteNodeVO = null;
			}
		}
		
		return siteNodeVO;
		
	}

	public List<Integer> getSiteNodeIdsForAllChildren(Integer siteNodeId, Database db) throws Exception
	{
		List<Integer> childIds = new ArrayList<Integer>();
		List<SiteNodeVO> childSiteNodes = getChildSiteNodeVOList(siteNodeId, false, db, null);
		for(SiteNodeVO child : childSiteNodes)
		{
			childIds.addAll(getSiteNodeIdsForAllChildren(child.getId(), db));
			childIds.add(child.getId());
		}
		
		return childIds;
	}

}
