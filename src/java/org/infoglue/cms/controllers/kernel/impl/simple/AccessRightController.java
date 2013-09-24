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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.applications.contenttool.actions.databeans.AccessRightsUserRow;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.AccessRight;
import org.infoglue.cms.entities.management.AccessRightGroup;
import org.infoglue.cms.entities.management.AccessRightGroupVO;
import org.infoglue.cms.entities.management.AccessRightRole;
import org.infoglue.cms.entities.management.AccessRightRoleVO;
import org.infoglue.cms.entities.management.AccessRightUser;
import org.infoglue.cms.entities.management.AccessRightUserVO;
import org.infoglue.cms.entities.management.AccessRightVO;
import org.infoglue.cms.entities.management.InterceptionPoint;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.management.TableCount;
import org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallAccessRightImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.DateHelper;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.Timer;

/**
 * This class is a helper class for the use case handle Accesss
 *
 * @author Mattias Bogeblad
 */

public class AccessRightController extends BaseController
{
    private final static Logger logger = Logger.getLogger(AccessRightController.class.getName());

	/**
	 * Factory method
	 */

	public static AccessRightController getController()
	{
		return new AccessRightController();
	}
	
	public AccessRight getAccessRightWithId(Integer accessRightId, Database db) throws SystemException, Bug
	{
		return (AccessRight) getObjectWithId(AccessRightImpl.class, accessRightId, db);
	}

	public AccessRightVO getAccessRightVOWithId(Integer accessRightId) throws SystemException, Bug
	{
		return (AccessRightVO) getVOWithId(AccessRightImpl.class, accessRightId);
	}
  
	public List getAccessRightVOList() throws SystemException, Bug
	{
		return getAllVOObjects(AccessRightImpl.class, "accessRightId");
	}

	public List getAccessRightVOList(Database db) throws SystemException, Bug
	{
		return this.getAllVOObjects(AccessRightImpl.class, "accessRightId", db);
	}

	public List getAccessRightUserVOList(Database db) throws SystemException, Bug
	{
		return this.getAllVOObjects(AccessRightUserImpl.class, "accessRightUserId", db);
	}

	public List getAccessRightRoleVOList(Database db) throws SystemException, Bug
	{
		return this.getAllVOObjects(AccessRightRoleImpl.class, "accessRightRoleId", db);
	}

	public List getAccessRightGroupVOList(Database db) throws SystemException, Bug
	{
		return this.getAllVOObjects(AccessRightGroupImpl.class, "accessRightGroupId", db);
	}
	    
	public void preCacheUserAccessRightVOList(InfoGluePrincipal principal) throws Exception
	{
		if(principal.getIsAdministrator() || preCacheInProcessForUsers.contains(principal.getName()))
		{
			logger.warn("No recaching user access rights now as it's in process allready");
			return;
		}
		
		try
		{
			preCacheInProcessForUsers.add(principal.getName());
			
			Database db = CastorDatabaseService.getDatabase();
	
			try 
			{
				beginTransaction(db);
				
				preCacheUserAccessRightVOList(principal, db);
				
				commitTransaction(db);
			} 
			catch (Exception e) 
			{
			    logger.info("An error occurred so we should not complete the transaction:" + e);
				rollbackTransaction(db);
				throw new SystemException(e.getMessage());					
			}
		}
		finally
		{
			preCacheInProcessForUsers.remove(principal.getName());
		}
	}
	
	
	public void preCacheUserAccessRightVOList(InfoGluePrincipal principal, Database db) throws Exception
	{
		Timer t = new Timer();
		//Ifor detta sen ocksa - cachear alla sidor som ar skyddade..
		/*
		select accessRightId from cmAccessRight ar INNER JOIN
		(  
		select max(siteNodeVersionId) as siteNodeVersionId from cmSiteNodeVersion snv 
		WHERE  
		snv.isActive = 1 AND 
		snv.stateId >= 0 AND
		snv.isProtected = 1 group by siteNodeId) snv
		ON ar.parameters = snv.siteNodeVersionId WHERE ar.interceptionPointId = 8
		*/
		
		StringBuilder sb = new StringBuilder();
		sb.append("CALL SQL select ar.accessRightId, ar.parameters, ar.interceptionPointId from cmAccessRight ar where ");
		sb.append("ar.interceptionPointId in (select interceptionPointId from cmInterceptionPoint where name like 'ComponentEditor.%' OR name LIKE 'Component.%' OR name = 'ComponentPropertyEditor.EditProperty' OR name like '%.Read%' AND name NOT LIKE 'SiteNodeVersion.Read') AND ");
		sb.append("(ar.accessRightId IN (select accessRightId from cmAccessRightUser where userName = '" + principal.getName() + "') OR ");
		sb.append("((ar.accessRightId NOT IN (select accessRightId from cmAccessRightRole where ar.accessRightId = accessRightId) AND ar.accessRightId NOT IN (select accessRightId from cmAccessRightUser where ar.accessRightId = accessRightId)) OR ");
		sb.append("(  ar.accessRightId IN ");
		sb.append("(select accessRightId from cmAccessRightRole where ar.accessRightId = accessRightId AND roleName in ( ");
		int index = 0;
		for(InfoGlueRole role : (Collection<InfoGlueRole>)principal.getRoles())
		{
			if(index > 0)
				sb.append(",");
			sb.append("'" + role.getName() + "'");
			index++;
		}
		sb.append(")) ) ");
		sb.append("AND ");
		sb.append("(  ar.accessRightId NOT IN (select accessRightId from cmAccessRightGroup where ar.accessRightId = accessRightId) ");
		if(principal.getGroups().size() > 0)
		{
			sb.append("OR ar.accessRightId IN  ");
			sb.append("(select accessRightId from cmAccessRightGroup where ar.accessRightId = accessRightId AND groupName in ( ");
			index = 0;
			for(InfoGlueGroup group : (Collection<InfoGlueGroup>)principal.getGroups())
			{
				if(index > 0)
					sb.append(",");
				sb.append("'" + group.getName() + "'");
				index++;
			}
			sb.append(")) ");
		}
		sb.append("))) AS org.infoglue.cms.entities.management.impl.simple.SmallAccessRightImpl");
		
		logger.info("SQL::::::::::" + sb.toString());
		
		OQLQuery oql = db.getOQLQuery(sb.toString());
		//t.printElapsedTime("Executed took");
		
		int duplicates = 0;
		Map<String,Integer> accessRightsMap = new ConcurrentHashMap<String,Integer>();
		QueryResults results = oql.execute(Database.READONLY);
		int i=0;
		while(results.hasMore()) 
	    {
			i++;
			//System.out.print(".");
			AccessRightImpl aru = (SmallAccessRightImpl)results.next();			
			String key = "" + aru.getValueObject().getInterceptionPointId();
			if(aru.getValueObject().getParameters() != null && !aru.getValueObject().getParameters().equals(""))
				key = "" + aru.getValueObject().getInterceptionPointId() + "_" + aru.getValueObject().getParameters();
			
			if(accessRightsMap.get(key) == null)
			{
				accessRightsMap.put(key, 1);
			}
			/*
			else
			{
				accessRightsMap.put(key, -1);
				duplicates++;
				logger.info("Was a duplicate on " + key);
			}
			*/
			//accessRightVOList.add(aru.getValueObject());
		}
		logger.warn("accessRightsMap:" + accessRightsMap.size() + " took " + t.getElapsedTime());
		
		List<AccessRightVO> duplicateAccessRightVOList = new ArrayList<AccessRightVO>();
		List<AccessRightVO> duplicateNonHarmfulAccessRightVOList = new ArrayList<AccessRightVO>();
		List<AccessRightVO> duplicateAutoMergableAccessRightVOList = new ArrayList<AccessRightVO>();
		getAllDuplicates(false, true, duplicateAccessRightVOList, duplicateNonHarmfulAccessRightVOList, duplicateAutoMergableAccessRightVOList, db);
		
		logger.info("duplicateAccessRightVOList:" + duplicateAccessRightVOList.size());
		for(AccessRightVO accessRightVO : duplicateAccessRightVOList)
		{
			if(!duplicateNonHarmfulAccessRightVOList.contains(accessRightVO))
			{
				String key = "" + accessRightVO.getInterceptionPointId() + "_" + accessRightVO.getParameters();
				logger.info("Was a duplicate accessRightVO " + accessRightVO.getId() + ": " + key);
				accessRightsMap.put(key, -1);
			}
			else
			{
				logger.info("Was a duplicate accessRightVO but not harmful" + accessRightVO.getId());
			}
		}
		
		List<AccessRightVO> undefinedAccessRights = getUndefinedAccessRights(db);
		logger.info("undefinedAccessRights:" + undefinedAccessRights.size() + " took " + t.getElapsedTime());
		for(AccessRightVO accessRightVO : undefinedAccessRights)
		{
			String key = "" + accessRightVO.getInterceptionPointId() + "_" + accessRightVO.getParameters();
			logger.info("Was a empty accessRightVO " + accessRightVO.getId() + ": " + key);
			accessRightsMap.put(key, -1);
		}
		
		//CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", "authorizationMap_" + principal.getName(), accessRightsMap);
		CacheController.cacheObject("userAccessCache", "authorizationMap_" + principal.getName(), accessRightsMap);

		//logger.info("accessRightsMap:" + accessRightsMap.size());
		//logger.info("duplicates:" + duplicates);
		//principalAccessRights.put("" + principal.getName(), accessRightsMap);
		logger.warn("accessRightsMap:" + accessRightsMap.size());
		logger.warn("duplicates:" + duplicates);
		//t.printElapsedTime("Read took:" + accessRightVOList.size());
		
		results.close();
		oql.close();

		logger.warn("Recaching access rights in " + CmsPropertyHandler.getContextRootPath() + " for user " + principal.getName() + " took " + t.getElapsedTime());
	}
	
	
	public List getAccessRightVOList(String interceptionPointName, String parameters, Database db) throws SystemException, Bug
	{
		String key = "" + interceptionPointName + "_" + parameters;
		List accessRightVOList = (List)CacheController.getCachedObject("authorizationCache", key);
		if(accessRightVOList != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached accessRightVOList:" + accessRightVOList);
		
			return accessRightVOList;
		}
			
		accessRightVOList = new ArrayList();
		
		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName(interceptionPointName, db);
		if(interceptionPointVO == null)
		{
			logger.info("interceptionPointName:" + interceptionPointName + " not found");
			return new ArrayList();
		}
		
		//Thread.dumpStack();
		logger.info("Reading the hard way from an unexpected place:" + interceptionPointName + ":" + parameters);
		
		List accessRightList = this.getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), parameters, db);
		
		Iterator accessRightListIterator = accessRightList.iterator();
		while(accessRightListIterator.hasNext())
		{
		    AccessRight accessRight = (AccessRight)accessRightListIterator.next();
		    
		    Collection approvedRoles = accessRight.getRoles();
		    Collection approvedGroups = accessRight.getGroups();
		    Collection approvedUsers = accessRight.getUsers();
		
		    AccessRightVO vo = accessRight.getValueObject();
		    vo.getRoles().addAll(toVOList(approvedRoles));
		    vo.getGroups().addAll(toVOList(approvedGroups));
		    vo.getUsers().addAll(toVOList(approvedUsers));
		    
		    accessRightVOList.add(vo);
		}
		
		if(accessRightVOList != null)
			CacheController.cacheObject("authorizationCache", key, accessRightVOList);
		
		return accessRightVOList;	
	}

	public List<AccessRightGroupVO> getAccessRightGroupVOList(Integer accessRightId) throws SystemException, Bug
	{
		List<AccessRightGroupVO> accessRightGroupVOList = new ArrayList<AccessRightGroupVO>();
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl f WHERE f.accessRight = $1 ORDER BY f.accessRightGroupId");
			oql.bind(accessRightId);

			QueryResults results = oql.execute(Database.READONLY);
			while (results.hasMore()) 
			{
				AccessRightGroup accessRightGroup = (AccessRightGroup)results.next();
				accessRightGroupVOList.add(accessRightGroup.getValueObject());
			}
			
			results.close();
			oql.close();
			
	        logger.info("accessRightGroupVOList:" + accessRightGroupVOList.size());
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
		    logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());					
		}
		
		return accessRightGroupVOList;	
	}

	public List<AccessRightGroupVO> getAccessRightGroupVOList(Integer accessRightId, Database db) throws SystemException, Bug, Exception
	{
		List<AccessRightGroupVO> accessRightGroupVOList = new ArrayList<AccessRightGroupVO>();
		
		OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl f WHERE f.accessRight = $1 ORDER BY f.accessRightGroupId");
		oql.bind(accessRightId);

		QueryResults results = oql.execute(Database.READONLY);
		while (results.hasMore()) 
		{
			AccessRightGroup accessRightGroup = (AccessRightGroup)results.next();
			accessRightGroupVOList.add(accessRightGroup.getValueObject());
		}
		
		results.close();
		oql.close();
		
		return accessRightGroupVOList;	
	}

	public List getAccessRightVOList(Integer interceptionPointId, String parameters, String roleName) throws SystemException, Bug
	{
		List accessRightVOList = null;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			accessRightVOList = getAccessRightVOList(db, interceptionPointId, parameters, roleName);

			logger.info("accessRightVOList:" + accessRightVOList.size());
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
		    e.printStackTrace();
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return accessRightVOList;	
	}

	
	public List getAccessRightVOList(Database db, Integer interceptionPointId, String parameters, String roleName) throws SystemException, Bug
	{
		List accessRightVOList = null;
		
		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithId(interceptionPointId);
		if(interceptionPointVO.getUsesExtraDataForAccessControl().booleanValue())
			accessRightVOList = toVOList(getAccessRightList(interceptionPointId, parameters, roleName, db));
		else
			accessRightVOList = toVOList(getAccessRightList(interceptionPointId, roleName, db));

		logger.info("accessRightVOList:" + accessRightVOList.size());
		
		return accessRightVOList;	
	}

	public List getAccessRightVOListOnly(Integer interceptionPointId, String parameters) throws SystemException, Bug
	{
		List accessRightVOList = null;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			accessRightVOList = getAccessRightVOListOnly(db, interceptionPointId, parameters);

			logger.info("accessRightVOList:" + accessRightVOList.size());
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return accessRightVOList;	
	}

	public List getAccessRightVOListOnly(Database db, Integer interceptionPointId, String parameters) throws SystemException, Bug
	{
		List accessRightVOList = null;
		
		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithId(interceptionPointId);
		if(interceptionPointVO.getUsesExtraDataForAccessControl().booleanValue())
			accessRightVOList = toVOList(getAccessRightListOnlyReadOnly(interceptionPointId, parameters, db));
		else
			accessRightVOList = toVOList(getAccessRightList(interceptionPointId, db));

		logger.info("accessRightVOList:" + accessRightVOList.size());
		
		return accessRightVOList;	
	}

	public List getAccessRightList(String interceptionPointName, String parameters, String roleName, Database db) throws SystemException, Bug
	{
		List accessRightList = getAccessRightList(InterceptionPointController.getController().getInterceptionPointVOWithName(interceptionPointName).getId(), parameters, roleName, db);
		
		return accessRightList;		
	}
	
	public List getAccessRightList(Integer interceptionPointId, String parameters, String roleName, Database db) throws SystemException, Bug
	{
		List accessRightList = new ArrayList();
		
		try
		{
			OQLQuery oql = null;
			
			if(parameters == null || parameters.length() == 0)
			{
				oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND (is_undefined(f.parameters) OR f.parameters = $2) AND f.roles.roleName = $3 ORDER BY f.accessRightId");
				oql.bind(interceptionPointId);
				oql.bind(parameters);
				oql.bind(roleName);
			}
			else
			{
		    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND f.parameters = $2 AND f.roles.roleName = $3 ORDER BY f.accessRightId");
				oql.bind(interceptionPointId);
				oql.bind(parameters);
				oql.bind(roleName);
			}
			
			QueryResults results = oql.execute();
			this.logger.info("Fetching entity in read/write mode" + interceptionPointId);

			while (results.hasMore()) 
			{
				AccessRight accessRight = (AccessRight)results.next();
				accessRightList.add(accessRight);
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
		    e.printStackTrace();
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightList;		
	}

	public List getAccessRightListOnly(Integer interceptionPointId, String parameters, Database db) throws SystemException, Bug
	{
		List accessRightList = new ArrayList();
		
		try
		{
			OQLQuery oql = null;
			
			if(parameters == null || parameters.length() == 0)
			{
				oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND (is_undefined(f.parameters) OR f.parameters = $2) ORDER BY f.accessRightId");
				oql.bind(interceptionPointId);
				oql.bind(parameters);
			}
			else
			{
		    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND f.parameters = $2 ORDER BY f.accessRightId");
				oql.bind(interceptionPointId);
				oql.bind(parameters);
			}
			
			QueryResults results = oql.execute();
			this.logger.info("Fetching entity in read/write mode");

			while (results.hasMore()) 
			{
				AccessRight accessRight = (AccessRight)results.next();
				accessRightList.add(accessRight);
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
		    e.printStackTrace();
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightList;		
	}

	public List getAccessRightListOnlyReadOnly(Integer interceptionPointId, Database db) throws SystemException, Bug
	{
		List accessRightList = new ArrayList();
		
		try
		{
			OQLQuery oql = null;
			
	    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 ORDER BY f.accessRightId");
			oql.bind(interceptionPointId);
			
			QueryResults results = oql.execute(Database.READONLY);
			while (results.hasMore()) 
			{
				AccessRight accessRight = (AccessRight)results.next();
				accessRightList.add(accessRight);
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightList;		
	}

	
	public List<AccessRight> getAccessRightListOnlyReadOnly(Integer interceptionPointId, String parameters, Database db) throws SystemException, Bug
	{
		List accessRightList = new ArrayList();
		
		try
		{
			RequestAnalyser.getRequestAnalyser().incApproximateNumberOfDatabaseQueries();

			OQLQuery oql = null;
			
			if(parameters == null || parameters.length() == 0)
			{
				oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 ORDER BY f.accessRightId");
				oql.bind(interceptionPointId);
			}
			else
			{
		    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND f.parameters = $2 ORDER BY f.accessRightId");
				oql.bind(interceptionPointId);
				oql.bind(parameters);
			}
			
			QueryResults results = oql.execute(Database.READONLY);
			while (results.hasMore()) 
			{
				AccessRight accessRight = (AccessRight)results.next();
				accessRightList.add(accessRight);
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			logger.warn("Error getting access rights. Message: " + e.getMessage() + ". Not retrying...");
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
		finally
		{
			RequestAnalyser.getRequestAnalyser().decApproximateNumberOfDatabaseQueries();
		}

		return accessRightList;		
	}

	public List<AccessRight> getContentAccessRightListOnlyReadOnly(Integer repositoryId, Database db) throws SystemException, Bug
	{
		List<AccessRight> accessRightList = new ArrayList<AccessRight>();
		
		try
		{
			RequestAnalyser.getRequestAnalyser().incApproximateNumberOfDatabaseQueries();

			String SQL = "CALL SQL select ar.accessRightId, ar.parameters, ar.interceptionPointId from cmAccessRight ar, cmContent c where ar.interceptionPointId in (select interceptionPointId from cmInterceptionPoint where name like 'Content.%') AND ar.parameters = c.contentId AND c.repositoryId = $1 ORDER BY ar.interceptionPointId, ar.parameters AS org.infoglue.cms.entities.management.impl.simple.SmallAccessRightImpl";
			if(CmsPropertyHandler.getUseShortTableNames().equals("true"))
				SQL = "CALL SQL select ar.accessRightId, ar.parameters, ar.interceptionPointId from cmAccessRight ar, cmCont c where ar.interceptionPointId in (select interceptionPointId from cmInterceptionPoint where name like 'Content.%') AND ar.parameters = to_char(c.contId) AND c.repositoryId = $1 ORDER BY ar.interceptionPointId, ar.parameters AS org.infoglue.cms.entities.management.impl.simple.SmallAccessRightImpl";
			
			OQLQuery oql = db.getOQLQuery(SQL);
			oql.bind(repositoryId);
			
			QueryResults results = oql.execute(Database.READONLY);
			while (results.hasMore()) 
			{
				SmallAccessRightImpl smallAccessRight = (SmallAccessRightImpl)results.next();
				AccessRight accessRight = getAccessRightWithId(smallAccessRight.getAccessRightId(), db);
				
				accessRightList.add(accessRight);
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			logger.warn("Error getting access rights. Message: " + e.getMessage() + ". Not retrying...");
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
		finally
		{
			RequestAnalyser.getRequestAnalyser().decApproximateNumberOfDatabaseQueries();
		}

		return accessRightList;		
	}
	
	public List<AccessRight> getSiteNodeAccessRightListOnlyReadOnly(Integer repositoryId, Database db) throws SystemException, Bug
	{
		List<AccessRight> accessRightList = new ArrayList<AccessRight>();
		
		try
		{
			RequestAnalyser.getRequestAnalyser().incApproximateNumberOfDatabaseQueries();

			String SQL = "CALL SQL select ar.accessRightId, ar.parameters, ar.interceptionPointId from cmAccessRight ar, cmSiteNode sn, cmSiteNodeVersion snv where ar.interceptionPointId in (select interceptionPointId from cmInterceptionPoint where name like 'SiteNodeVersion.%') AND ar.parameters = snv.siteNodeVersionId AND snv.siteNodeId = sn.siteNodeId AND sn.repositoryId = $1 ORDER BY ar.interceptionPointId, ar.parameters AS org.infoglue.cms.entities.management.impl.simple.SmallAccessRightImpl";
			if(CmsPropertyHandler.getUseShortTableNames().equals("true"))
				SQL = "CALL SQL select ar.accessRightId, ar.parameters, ar.interceptionPointId from cmAccessRight ar, cmSiNo sn, cmSiNoVer snv where ar.interceptionPointId in (select interceptionPointId from cmInterceptionPoint where name like 'SiteNodeVersion.%') AND ar.parameters = to_char(snv.siNoVerId) AND snv.siNoId = sn.siNoId AND sn.repositoryId = $1 ORDER BY ar.interceptionPointId, ar.parameters AS org.infoglue.cms.entities.management.impl.simple.SmallAccessRightImpl";
				
			OQLQuery oql = db.getOQLQuery(SQL);
			oql.bind(repositoryId);
			
			QueryResults results = oql.execute(Database.READONLY);
			while (results.hasMore()) 
			{
				SmallAccessRightImpl smallAccessRight = (SmallAccessRightImpl)results.next();
				AccessRight accessRight = getAccessRightWithId(smallAccessRight.getAccessRightId(), db);
				
				accessRightList.add(accessRight);
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			logger.warn("Error getting access rights. Message: " + e.getMessage() + ". Not retrying...");
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
		finally
		{
			RequestAnalyser.getRequestAnalyser().decApproximateNumberOfDatabaseQueries();
		}

		return accessRightList;		
	}

	public List<AccessRightRoleVO> getAccessRightRoleVOList(Integer interceptionPointId, String parameters, Database db) throws SystemException, Bug
	{
		List<AccessRightRoleVO> accessRightRoleList = new ArrayList<AccessRightRoleVO>();
		
		try
		{
			OQLQuery oql = null;
			
			if(parameters == null || parameters.length() == 0)
			{
				oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl f WHERE f.accessRight.interceptionPoint = $1 ORDER BY f.accessRightRoleId");
				oql.bind(interceptionPointId);
			}
			else
			{
		    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl f WHERE f.accessRight.interceptionPoint = $1 AND f.accessRight.parameters = $2 ORDER BY f.accessRightRoleId");
				oql.bind(interceptionPointId);
				oql.bind(parameters);
			}
			
			QueryResults results = oql.execute(Database.READONLY);
			while (results.hasMore()) 
			{
				AccessRightRole accessRightRole = (AccessRightRole)results.next();
				accessRightRoleList.add(accessRightRole.getValueObject());
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			logger.warn("Error getting access rights. Message: " + e.getMessage() + ". Retrying...");
			try
			{
				accessRightRoleList = getAccessRightRoleVOList(interceptionPointId, parameters, db);
			}
			catch(Exception e2)
			{
				throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
			}
		}

		return accessRightRoleList;		
	}

	public List<AccessRightGroupVO> getAccessRightGroupVOList(Integer interceptionPointId, String parameters, Database db) throws SystemException, Bug
	{
		List<AccessRightGroupVO> accessRightGroupList = new ArrayList<AccessRightGroupVO>();
		
		try
		{
			OQLQuery oql = null;
			
			if(parameters == null || parameters.length() == 0)
			{
				oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl f WHERE f.accessRight.interceptionPoint = $1 ORDER BY f.accessRightGroupId");
				oql.bind(interceptionPointId);
			}
			else
			{
		    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl f WHERE f.accessRight.interceptionPoint = $1 AND f.accessRight.parameters = $2 ORDER BY f.accessRightGroupId");
				oql.bind(interceptionPointId);
				oql.bind(parameters);
			}
			
			QueryResults results = oql.execute(Database.READONLY);
			while (results.hasMore()) 
			{
				AccessRightGroup accessRightGroup = (AccessRightGroup)results.next();
				accessRightGroupList.add(accessRightGroup.getValueObject());
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			logger.warn("Error getting access rights. Message: " + e.getMessage() + ". Retrying...");
			try
			{
				accessRightGroupList = getAccessRightGroupVOList(interceptionPointId, parameters, db);
			}
			catch(Exception e2)
			{
				throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
			}
		}

		return accessRightGroupList;		
	}

	
	public List<AccessRightUserVO> getAccessRightUserVOList(Integer interceptionPointId, String parameters, Database db) throws SystemException, Bug
	{
		List<AccessRightUserVO> accessRightUserList = new ArrayList<AccessRightUserVO>();
		
		try
		{
			OQLQuery oql = null;
			
			if(parameters == null || parameters.length() == 0)
			{
				oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl f WHERE f.accessRight.interceptionPoint = $1 ORDER BY f.accessRightUserId");
				oql.bind(interceptionPointId);
			}
			else
			{
		    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl f WHERE f.accessRight.interceptionPoint = $1 AND f.accessRight.parameters = $2 ORDER BY f.accessRightUserId");
				oql.bind(interceptionPointId);
				oql.bind(parameters);
			}
			
			QueryResults results = oql.execute(Database.READONLY);
			while (results.hasMore()) 
			{
				AccessRightUser accessRightUser = (AccessRightUser)results.next();
				accessRightUserList.add(accessRightUser.getValueObject());
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			logger.warn("Error getting access rights. Message: " + e.getMessage() + ". Retrying...");
			try
			{
				accessRightUserList = getAccessRightUserVOList(interceptionPointId, parameters, db);
			}
			catch(Exception e2)
			{
				throw new SystemException("An error occurred when we tried to fetch a list of Access rights users. Reason:" + e.getMessage(), e);    
			}
		}

		return accessRightUserList;		
	}

	public List<AccessRight> getAccessRightListForEntity(Integer interceptionPointId, String parameters, Database db)  throws SystemException, Bug
	{
		List<AccessRight> accessRightList = new ArrayList<AccessRight>();

		try
		{
		    //logger.info("getAccessRightListForEntity(Integer interceptionPointId, String parameters, Database db)");
		    //logger.info("interceptionPointId:" + interceptionPointId);
		    //logger.info("parameters:" + parameters);

			OQLQuery oql = null;

			if(parameters == null || parameters.length() == 0)
			{
				oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND (is_undefined(f.parameters) OR f.parameters = $2) ORDER BY f.accessRightId");
				oql.bind(interceptionPointId);
				oql.bind(parameters);
			}
			else
			{
		    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND f.parameters = $2 ORDER BY f.accessRightId");
				oql.bind(interceptionPointId);
				oql.bind(parameters);
			}

			QueryResults results = oql.execute();
			logger.info("Fetching entity in read/write mode");

			while (results.hasMore()) 
			{
				AccessRight accessRight = (AccessRight)results.next();
				//logger.info("accessRight:" + accessRight.getAccessRightId());
				accessRightList.add(accessRight);
			}

			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of Function. Reason:" + e.getMessage(), e);
		}

		return accessRightList;
	}

	public List<AccessRight> getAccessRightListForEntity(List<InterceptionPoint> interceptionPointVOList, String parameters, Database db, boolean readOnly)  throws SystemException, Bug
	{
		List accessRightList = new ArrayList();
		
		try
		{
			OQLQuery oql = null;

			int lastIndex = 0;;
			StringBuilder variables = new StringBuilder();
		    for(int i=0; i<interceptionPointVOList.size(); i++)
		    {
		    	variables.append("f.interceptionPoint = $" + (i+1) + (i+1!=interceptionPointVOList.size() ? " OR " : ""));
		    	lastIndex++;
		    }

			if(parameters == null || parameters.length() == 0)
			{
				oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE (" + variables + ") AND (is_undefined(f.parameters) OR f.parameters = $2) ORDER BY f.accessRightId");
				for(InterceptionPoint ipVO : interceptionPointVOList)
					oql.bind(ipVO.getId());
				oql.bind(parameters);
			}
			else
			{
		    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE (" + variables + ") AND f.parameters = $" + (lastIndex+1) + " ORDER BY f.accessRightId");
		    	for(InterceptionPoint ipVO : interceptionPointVOList)
		    	{
		    		oql.bind(ipVO.getId());
		    	}
				oql.bind(parameters);
			}
						
			QueryResults results;
			if(readOnly)
				results = oql.execute(Database.READONLY);
			else
				results = oql.execute();
				
			this.logger.info("Fetching entity in read/write mode");

			while (results.hasMore()) 
			{
				AccessRight accessRight = (AccessRight)results.next();
				//logger.info("accessRight:" + accessRight.getAccessRightId());
				accessRightList.add(accessRight);
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of Function. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightList;		
	}

	
	public List getAccessRightList(Integer interceptionPointId, Database db)  throws SystemException, Bug
	{
		List accessRightList = new ArrayList();
		
		try
		{
			logger.info("getAccessRightList(Integer interceptionPointId, Database db)");
			logger.info("interceptionPointId: " + interceptionPointId);
			
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 ORDER BY f.accessRightId");
			oql.bind(interceptionPointId);
			
			QueryResults results = oql.execute();
			this.logger.info("Fetching entity in read/write mode");

			while (results.hasMore()) 
			{
				AccessRight accessRight = (AccessRight)results.next();
				logger.info("accessRight:" + accessRight.getAccessRightId());
				accessRightList.add(accessRight);
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of Function. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightList;		
	}

	public List getAccessRightList(String roleName, Database db)  throws SystemException, Bug
	{
		List accessRightList = new ArrayList();
		
		try
		{
			logger.info("getAccessRightList(String roleName, Database db)");
			logger.info("roleName: " + roleName);
			
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.roles.roleName = $1 ORDER BY f.accessRightId");
			oql.bind(roleName);
			
			QueryResults results = oql.execute();
			this.logger.info("Fetching entity in read/write mode");

			while (results.hasMore()) 
			{
				AccessRight accessRight = (AccessRight)results.next();
				logger.info("accessRight:" + accessRight.getAccessRightId());
				accessRightList.add(accessRight);
			}

			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of Function. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightList;		
	}
	

	public List getAccessRightList(Integer interceptionPointId, String roleName, Database db)  throws SystemException, Bug
	{
		List accessRightList = new ArrayList();
		
		try
		{
		    if(logger.isInfoEnabled())
		    {
				logger.info("getAccessRightList(Integer interceptionPointId, String roleName, Database db)");
				logger.info("interceptionPointId: " + interceptionPointId);
				logger.info("roleName: " + roleName);
		    }

			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND f.roles.roleName = $2 ORDER BY f.accessRightId");
			oql.bind(interceptionPointId);
			oql.bind(roleName);
						
			QueryResults results = oql.execute();
			this.logger.info("Fetching entity in read/write mode");

			while (results.hasMore()) 
			{
				AccessRight accessRight = (AccessRight)results.next();
				accessRightList.add(accessRight);
			}

			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of Function. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightList;		
	}
	
	/**
	 * This method creates an access note.
	 * 
	 * @param accessRightVO
	 * @param db
	 * @return
	 * @throws SystemException
	 * @throws Exception
	 */
	
	public AccessRight create(AccessRightVO accessRightVO, InterceptionPoint interceptionPoint, Database db) throws SystemException, Exception
	{
		AccessRight accessRight = new AccessRightImpl();
		accessRight.setValueObject(accessRightVO);
		
		accessRight.setInterceptionPoint(interceptionPoint);
		
		db.create(accessRight);
					
		return accessRight;
	}     

	
	public AccessRightVO update(AccessRightVO AccessRightVO) throws ConstraintException, SystemException
	{
		return (AccessRightVO) updateEntity(AccessRightImpl.class, AccessRightVO);
	}        

	
	public void update(String parameters, HttpServletRequest request, String interceptionPointCategory) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		
		logger.info("parameters:" + parameters);
		
		try 
		{
			beginTransaction(db);
			
			int accessRights = 0;
			
			int interceptionPointIndex = 0;
			String interceptionPointIdString = request.getParameter(interceptionPointIndex + "_InterceptionPointId");
			while(interceptionPointIdString != null)
			{
				logger.info("interceptionPointIdString:" + interceptionPointIdString);
				AccessRight accessRight = delete(new Integer(interceptionPointIdString), parameters, false, db);
				
				if(accessRight == null)
				{
					logger.info("accessRight after delete was null");
					AccessRightVO accessRightVO = new AccessRightVO();
					accessRightVO.setParameters(parameters);
	
					int roleIndex = 0;
					String roleName = request.getParameter(interceptionPointIdString + "_" + roleIndex + "_roleName");
					while(roleName != null)
					{
					    String hasAccess = request.getParameter(interceptionPointIdString + "_" + roleName + "_hasAccess");
						
						if(hasAccess != null)
						{
						    if(accessRight == null)
						    {
							    InterceptionPoint interceptionPoint = InterceptionPointController.getController().getInterceptionPointWithId(new Integer(interceptionPointIdString), db);
								accessRight = create(accessRightVO, interceptionPoint, db);
						    }
						    
						    AccessRightRoleVO accessRightRoleVO = new AccessRightRoleVO();
						    accessRightRoleVO.setRoleName(roleName);
						    AccessRightRole accessRightRole = createAccessRightRole(db, accessRightRoleVO, accessRight);
						    accessRight.getRoles().add(accessRightRole);
							accessRights++;
						}
						
						roleIndex++;
						roleName = request.getParameter(interceptionPointIdString + "_" + roleIndex + "_roleName");
					}
	
					int groupIndex = 0;
					String groupName = request.getParameter(interceptionPointIdString + "_" + groupIndex + "_groupName");
	
					while(groupName != null)
					{
						logger.info("groupName:" + groupName);
					    if(accessRight == null)
					    {
						    InterceptionPoint interceptionPoint = InterceptionPointController.getController().getInterceptionPointWithId(new Integer(interceptionPointIdString), db);
						    //logger.info("Creating access for:" + interceptionPoint.getName() + "_" + parameters);
							accessRight = create(accessRightVO, interceptionPoint, db);
					    }
						
					    AccessRightGroupVO accessRightGroupVO = new AccessRightGroupVO();
					    accessRightGroupVO.setGroupName(groupName);
					    AccessRightGroup accessRightGroup = createAccessRightGroup(db, accessRightGroupVO, accessRight);
					    accessRight.getGroups().add(accessRightGroup);
						
						accessRights++;
					    groupIndex++;
					    groupName = request.getParameter(interceptionPointIdString + "_" + groupIndex + "_groupName");
					    //logger.info("groupName:" + groupName);
					}
				}
				else
				{
					logger.info("accessRight after delete:" + accessRight.getAccessRightId());

					int roleIndex = 0;
					String roleName = request.getParameter(interceptionPointIdString + "_" + roleIndex + "_roleName");
					while(roleName != null)
					{
					    String hasAccess = request.getParameter(interceptionPointIdString + "_" + roleName + "_hasAccess");
						if(hasAccess != null)
						{
						    AccessRightRoleVO accessRightRoleVO = new AccessRightRoleVO();
						    accessRightRoleVO.setRoleName(roleName);
						    AccessRightRole accessRightRole = createAccessRightRole(db, accessRightRoleVO, accessRight);
						    accessRight.getRoles().add(accessRightRole);
							accessRights++;
						}
						
						roleIndex++;
						roleName = request.getParameter(interceptionPointIdString + "_" + roleIndex + "_roleName");
					}
	
					int groupIndex = 0;
					String groupName = request.getParameter(interceptionPointIdString + "_" + groupIndex + "_groupName");
					while(groupName != null)
					{
						logger.info("groupName:" + groupName);
					    AccessRightGroupVO accessRightGroupVO = new AccessRightGroupVO();
					    accessRightGroupVO.setGroupName(groupName);
					    AccessRightGroup accessRightGroup = createAccessRightGroup(db, accessRightGroupVO, accessRight);
					    accessRight.getGroups().add(accessRightGroup);
						
						accessRights++;
					    groupIndex++;
					    groupName = request.getParameter(interceptionPointIdString + "_" + groupIndex + "_groupName");
					    //logger.info("groupName:" + groupName);
					}
				}

				interceptionPointIndex++;
				interceptionPointIdString = request.getParameter(interceptionPointIndex + "_InterceptionPointId");
			}
			
			if(logger.isDebugEnabled())
				logger.debug("accessRights:" + accessRights);
			if(accessRights > 0)
			{
				if(interceptionPointCategory.equalsIgnoreCase("Content"))
				{	
					Integer contentId = new Integer(parameters);
					Content content = ContentControllerProxy.getController().getContentWithId(contentId, db);
					if(!content.getIsProtected().equals(ContentVO.YES))
						content.setIsProtected(ContentVO.YES);
				}
				else if(interceptionPointCategory.equalsIgnoreCase("SiteNodeVersion"))
				{	
					Integer siteNodeVersionId = new Integer(parameters);
					SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(siteNodeVersionId, db);
					if(logger.isDebugEnabled())
						logger.debug("It was a siteNodeVersion and there are access rights - set it to true:" + accessRights);
					if(!siteNodeVersion.getIsProtected().equals(SiteNodeVersionVO.YES) && !siteNodeVersion.getIsProtected().equals(SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK))
						siteNodeVersion.setIsProtected(SiteNodeVersionVO.YES);
				}
			}
			else
			{
				List accessRightsUsers = getAccessRightsUsers(interceptionPointCategory, parameters, db, true);
				if(logger.isDebugEnabled())
					logger.debug("accessRightsUsers:" + accessRightsUsers.size());
				if(accessRightsUsers == null || accessRightsUsers.size() == 0)
				{
					if(interceptionPointCategory.equalsIgnoreCase("Content"))
					{	
						Integer contentId = new Integer(parameters);
						Content content = ContentControllerProxy.getController().getContentWithId(contentId, db);
						if(content.getIsProtected().equals(ContentVO.YES))
							content.setIsProtected(ContentVO.NO);
					}
					else if(interceptionPointCategory.equalsIgnoreCase("SiteNodeVersion"))
					{	
						Integer siteNodeVersionId = new Integer(parameters);
						SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(siteNodeVersionId, db);
						if(logger.isDebugEnabled())
							logger.debug("It was a siteNodeVersion and there was no access rights - set it to false:" + accessRights + ":" + siteNodeVersion.getIsProtected());
						if(siteNodeVersion.getIsProtected().equals(SiteNodeVersionVO.YES) && !siteNodeVersion.getIsProtected().equals(SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK))
						{
							siteNodeVersion.setIsProtected(SiteNodeVersionVO.NO);
							siteNodeVersion.setModifiedDateTime(DateHelper.getSecondPreciseDate());
						}
					}
				}
			}
				
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
		    e.printStackTrace();
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}			

	
	public void updateGroups(Integer accessRightId, String parameters, String[] groupNames) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		
		logger.info("parameters:" + parameters);
		
		try 
		{
			beginTransaction(db);
			
			AccessRight accessRight = this.getAccessRightWithId(accessRightId, db);

			Iterator groupsIterator = accessRight.getGroups().iterator();
			while(groupsIterator.hasNext())
			{
			    AccessRightGroup accessRightGroup = (AccessRightGroup)groupsIterator.next();
			    groupsIterator.remove();
			    db.remove(accessRightGroup);
			}
			
			if(groupNames != null)
			{
				for(int i=0; i < groupNames.length; i++)
				{
				    String groupName = groupNames[i];
				    AccessRightGroupVO accessRightGroupVO = new AccessRightGroupVO();
				    accessRightGroupVO.setGroupName(groupName);
				    AccessRightGroup accessRightGroup = createAccessRightGroup(db, accessRightGroupVO, accessRight);
				    accessRight.getGroups().add(accessRightGroup);
				}
			}
			
		    commitTransaction(db);
		} 
		catch (Exception e) 
		{
		    e.printStackTrace();
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}
	
	/**
	 * Adds a user to have access
	 * 
	 * @param accessRightId
	 * @param parameters
	 * @param userName
	 * @throws ConstraintException
	 * @throws SystemException
	 */
	public void addUser(String interceptionPointCategory, String parameters, String userName, Integer index, HttpServletRequest request) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();

		logger.info("parameters:" + parameters);

		try
		{
			beginTransaction(db);

			try
			{
			    InfoGluePrincipal infoGluePrincipal = UserControllerProxy.getController(db).getUser(userName);
			    if(infoGluePrincipal == null)
			        throw new SystemException("The user named " + userName + " does not exist in the system.");
			}
			catch(Exception e)
			{
		        throw new SystemException("The user named " + userName + " does not exist in the system.");
			}

		    List accessRightsUsers = getAccessRightsUsers(interceptionPointCategory, parameters, userName, db);
		    Iterator accessRightsUsersIterator = accessRightsUsers.iterator();
		    while(accessRightsUsersIterator.hasNext())
		    {
		        AccessRightUser accessRightUser = (AccessRightUser)accessRightsUsersIterator.next();

		        AccessRight ar = getAccessRightWithId(accessRightUser.getAccessRight().getAccessRightId(), db);

		        ar.getUsers().remove(accessRightUser);
		        accessRightsUsersIterator.remove();
		        db.remove(accessRightUser);
		    }

			int interceptionPointIndex = 0;
			String interceptionPointIdString = request.getParameter((index != null ? index + "_" : "") + interceptionPointIndex + "_InterceptionPointId");
			logger.info("interceptionPointIdString:" + interceptionPointIdString);
			while(interceptionPointIdString != null)
			{
			    String hasAccess = request.getParameter((index != null ? index + "_" : "") + interceptionPointIdString + "_hasAccess");
			    logger.info("interceptionPointIdString:" + interceptionPointIdString);

			    AccessRight accessRight = null;

			    List<AccessRight> accessRights = getAccessRightListForEntity(new Integer(interceptionPointIdString), parameters, db);
				if(hasAccess != null)
				{
				    if(accessRights == null || accessRights.size() == 0)
				    {
						AccessRightVO accessRightVO = new AccessRightVO();
						accessRightVO.setParameters(parameters);

				        InterceptionPoint interceptionPoint = InterceptionPointController.getController().getInterceptionPointWithId(new Integer(interceptionPointIdString), db);
						accessRight = create(accessRightVO, interceptionPoint, db);
				    }
				    else
				    {
				        accessRight = (AccessRight)accessRights.get(0);
				    }

					if(userName != null && accessRight != null)
					{
					    AccessRightUserVO accessRightUserVO = new AccessRightUserVO();
					    accessRightUserVO.setUserName(userName);
					    AccessRightUser accessRightUser = createAccessRightUser(db, accessRightUserVO, accessRight);
					    accessRight.getUsers().add(accessRightUser);
					}
				}

				// If we do not have an access right reference here we try to get one so that we can do some cleaning
				if (accessRight == null)
				{
					if (accessRights.size() < 1)
					{
						logger.info("We have no access right and there is none in the list. IP: " + interceptionPointIdString + ". Parameters: " + parameters);
					}
					else
					{
						accessRight = (AccessRight)accessRights.get(0);
					}
				}
				if (accessRight != null)
				{
					logger.debug("Check if we should remove access right: " + accessRight.getAccessRightId() + ". IP.name: " + accessRight.getInterceptionPointName());
					if (   (accessRight.getRoles() == null || accessRight.getRoles().size() == 0)
						&& (accessRight.getGroups() == null || accessRight.getGroups().size() == 0)
						&& (accessRight.getUsers() == null || accessRight.getUsers().size() == 0))
					{
						logger.info("Cleaning access right since it is not used anymore. AccessRight.id: " + accessRight.getAccessRightId());
						db.remove(accessRight);
					}
				}

				interceptionPointIndex++;
				interceptionPointIdString = request.getParameter((index != null ? index + "_" : "") + interceptionPointIndex + "_InterceptionPointId");
			}

			if(interceptionPointCategory.equalsIgnoreCase("Content"))
			{
				Integer contentId = new Integer(parameters);
				Content content = ContentControllerProxy.getController().getContentWithId(contentId, db);
				if(!content.getIsProtected().equals(ContentVO.YES))
					content.setIsProtected(ContentVO.YES);
			}
			else if(interceptionPointCategory.equalsIgnoreCase("SiteNodeVersion"))
			{
				Integer siteNodeVersionId = new Integer(parameters);
				SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(siteNodeVersionId, db);
				if(!siteNodeVersion.getIsProtected().equals(SiteNodeVersionVO.YES))
					siteNodeVersion.setIsProtected(SiteNodeVersionVO.YES);
			}

		    commitTransaction(db);
		}
		catch (Exception e)
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}

	/**
	 * Adds a user to have access
	 * 
	 * @param accessRightId
	 * @param parameters
	 * @param userName
	 * @throws ConstraintException
	 * @throws SystemException
	 */
	
	public void addUserRights(String[] interceptionPointNames, String parameters, InfoGluePrincipal principal) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			beginTransaction(db);
					
			for(int i=0; i<interceptionPointNames.length; i++)
			{
				String interceptionPointName = interceptionPointNames[i];
				InterceptionPoint interceptionPoint = InterceptionPointController.getController().getInterceptionPointWithName(interceptionPointName, db);
				if(interceptionPoint != null)
				{
					AccessRightVO accessRightVO = new AccessRightVO();
					accessRightVO.setParameters(parameters);
		
					AccessRight accessRight = create(accessRightVO, interceptionPoint, db);
				    
					if(principal != null && accessRight != null)
					{
					    AccessRightUserVO accessRightUserVO = new AccessRightUserVO();
					    accessRightUserVO.setUserName(principal.getName());
					    AccessRightUser accessRightUser = createAccessRightUser(db, accessRightUserVO, accessRight);
					    accessRight.getUsers().add(accessRightUser);
					}
				}
			}			
			
		    commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}

	/**
	 * Adds access for the role with the given <em>roleName</em>. The role is given access to all interception points in the given <em>interceptionPointCategory</em>.
	 * If the given category does not match any category in the system will not do anything.
	 * 
	 * All current access rights to the <em>parameters</em> for the given roleName are removed before applying the new access rights.
	 * 
	 * @param interceptionPointCategory
	 * @param parameters
	 * @param roleName
	 * @throws ConstraintException
	 * @throws SystemException Thrown if a database error occurs or if the given roleName does not match a role in the system.
	 */
	@SuppressWarnings("unchecked")
	public void addRoleRights(String interceptionPointCategory, String parameters, String roleName) throws SystemException
	{
		Database db = CastorDatabaseService.getDatabase();

		try
		{
			beginTransaction(db);

			// Verify that the role exists in the system.
			try
			{
			    InfoGlueRole role = RoleControllerProxy.getController(db).getRole(roleName);
			    if(role == null)
			        throw new SystemException("The role named " + roleName + " does not exist in the system.");
			}
			catch(Exception e)
			{
		        throw new SystemException("The role named " + roleName + " does not exist in the system.");
			}

			// Delete all existing access right for this role on this parameter and category group
			List<AccessRightRole> accessRightsRole = getAccessRightsRole(interceptionPointCategory, roleName, parameters, db, false);
		    Iterator<AccessRightRole> accessRightsRoleIterator = accessRightsRole.iterator();
		    while(accessRightsRoleIterator.hasNext())
		    {
				AccessRightRole accessRightRole = (AccessRightRole)accessRightsRoleIterator.next();

		        db.remove(accessRightRole.getAccessRight());

		        accessRightsRoleIterator.remove();
		        db.remove(accessRightRole);
		    }

		    // Add acces rights for each interception point in the category
			List<InterceptionPoint> interceptionPointList = InterceptionPointController.getController().getInterceptionPointList(interceptionPointCategory, db);
			for(InterceptionPoint interceptionPoint : interceptionPointList)
			{
				AccessRightVO accessRightVO = new AccessRightVO();
				accessRightVO.setParameters(parameters);

				AccessRight accessRight = create(accessRightVO, interceptionPoint, db);

				if(roleName != null && accessRight != null)
				{
					AccessRightRoleVO accessRightRoleVO = new AccessRightRoleVO();
					accessRightRoleVO.setRoleName(roleName);
					AccessRightRole accessRightRole = createAccessRightRole(db, accessRightRoleVO, accessRight);
					accessRight.getRoles().add(accessRightRole);
				}
			}

			commitTransaction(db);
		}
		catch (Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction when add role access rights to Interception point category: '" + interceptionPointCategory + "'. Message: " + e.getMessage() + ". Type: " + e.getClass());
			logger.warn("An error occurred so we should not complete the transaction when add role access rights to Interception point category: '" + interceptionPointCategory, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}

	/**
	 * Adds a user to have access
	 * 
	 * @param accessRightId
	 * @param parameters
	 * @param userName
	 * @throws ConstraintException
	 * @throws SystemException
	 */
	
	public void deleteUser(String interceptionPointCategory, String parameters, String userName, HttpServletRequest request) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		
		logger.info("parameters:" + parameters);
		
		try 
		{
			beginTransaction(db);
			
		    List accessRightsUsers = getAccessRightsUsers(interceptionPointCategory, parameters, userName, db);
		    Iterator accessRightsUsersIterator = accessRightsUsers.iterator();
		    while(accessRightsUsersIterator.hasNext())
		    {
		        AccessRightUser accessRightUser = (AccessRightUser)accessRightsUsersIterator.next();
		        
		        accessRightUser.getAccessRight().getUsers().remove(accessRightUser);
		        //if(accessRightUser.getAccessRight().)
		        //db.remove(accessRightUser.getAccessRight());

		        accessRightsUsersIterator.remove();
		        db.remove(accessRightUser);
		    }
			
		    if(accessRightsUsers.size() == 0)
		    {
				List accessRightsRoles = getAccessRightsRoles(interceptionPointCategory, parameters, db, true);
				List accessRightsGroups = getAccessRightsGroups(interceptionPointCategory, parameters, db, true);
				logger.info("accessRightsRoles:" + accessRightsRoles.size());
				if((accessRightsRoles == null || accessRightsRoles.size() == 0) && (accessRightsGroups == null || accessRightsGroups.size() == 0))
				{
					if(interceptionPointCategory.equalsIgnoreCase("Content"))
					{	
						Integer contentId = new Integer(parameters);
						Content content = ContentControllerProxy.getController().getContentWithId(contentId, db);
						if(content.getIsProtected().equals(ContentVO.YES))
							content.setIsProtected(ContentVO.NO);
					}
					else if(interceptionPointCategory.equalsIgnoreCase("SiteNodeVersion"))
					{	
						Integer siteNodeVersionId = new Integer(parameters);
						SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(siteNodeVersionId, db);
						if(siteNodeVersion.getIsProtected().equals(SiteNodeVersionVO.YES))
							siteNodeVersion.setIsProtected(SiteNodeVersionVO.NO);
					}
				}
		    }
		    
		    commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}

	/**
	 * This method creates a AccessRightRole-object in the database.
	 * @param db
	 * @param accessRightRoleVO
	 * @return
	 * @throws SystemException
	 */
	
	public AccessRightRole createAccessRightRole(Database db, AccessRightRoleVO accessRightRoleVO, AccessRight accessRight) throws SystemException, Exception
	{
	    AccessRightRole accessRightRole = new AccessRightRoleImpl();
	    accessRightRole.setValueObject(accessRightRoleVO);
	    accessRightRole.setAccessRight(accessRight);
	    
	    db.create(accessRightRole);
        
	    return accessRightRole;
	}

	/**
	 * This method creates a AccessRightGroup-object in the database.
	 * @param db
	 * @param accessRightGroupVO
	 * @return
	 * @throws SystemException
	 */
	
	public AccessRightGroup createAccessRightGroup(Database db, AccessRightGroupVO accessRightGroupVO, AccessRight accessRight) throws SystemException, Exception
	{
	    AccessRightGroup accessRightGroup = new AccessRightGroupImpl();
	    accessRightGroup.setValueObject(accessRightGroupVO);
	    accessRightGroup.setAccessRight(accessRight);
	    
	    db.create(accessRightGroup);
	    
        return accessRightGroup;
	}

	/**
	 * This method creates a AccessRightUser-object in the database.
	 * @param db
	 * @param accessRightUserVO
	 * @return
	 * @throws SystemException
	 */
	
	public AccessRightUser createAccessRightUser(Database db, AccessRightUserVO accessRightUserVO, AccessRight accessRight) throws SystemException, Exception
	{
	    AccessRightUser accessRightUser = new AccessRightUserImpl();
	    accessRightUser.setValueObject(accessRightUserVO);
	    accessRightUser.setAccessRight(accessRight);
	    
	    db.create(accessRightUser);
	    
        return accessRightUser;
	}
	
	
	/**
	 * This method deletes all occurrencies of AccessRight which has the interceptionPointId.
	 * 
	 * @param roleName
	 * @throws ConstraintException
	 * @throws SystemException
	 */

	public void delete(String roleName) throws SystemException, Exception
	{
		Database db = CastorDatabaseService.getDatabase();
		
		logger.info("roleName:" + roleName);
		
		try 
		{
			beginTransaction(db);

			List accessRightList = getAccessRightList(roleName, db);
			Iterator i = accessRightList.iterator();
			while(i.hasNext())
			{
				AccessRight accessRight = (AccessRight)i.next();
				
				Iterator accessRightRolesIterator = accessRight.getRoles().iterator();
				while(accessRightRolesIterator.hasNext())
				{
					AccessRightRole accessRightRole = (AccessRightRole)accessRightRolesIterator.next();
					if(roleName.equals(accessRightRole.getRoleName()))
					{
						accessRightRolesIterator.remove();
						db.remove(accessRightRole);
					}
				}
				/*
				Iterator accessRightGroupsIterator = accessRight.getGroups().iterator();
				while(accessRightGroupsIterator.hasNext())
				{
					AccessRightGroup accessRightGroup = (AccessRightGroup)accessRightGroupsIterator.next();
					db.remove(accessRightGroup);
				}
				Iterator accessRightUsersIterator = accessRight.getUsers().iterator();
				while(accessRightRolesIterator.hasNext())
				{
					AccessRightUser accessRightUser = (AccessRightUser)accessRightUsersIterator.next();
					db.remove(accessRightUser);
				}
				*/
				//db.remove(accessRight);
			}
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}        

	/**
	 * This method deletes all occurrencies of AccessRight which has the interceptionPointId.
	 * 
	 * @param roleName
	 * @throws ConstraintException
	 * @throws SystemException
	 */

	public AccessRight delete(Integer interceptionPointId, String parameters, boolean deleteUsers, Database db) throws SystemException, Exception
	{
		AccessRight returnAccessRight = null;
		
		List accessRightList = getAccessRightListOnly(interceptionPointId, parameters, db);
		Iterator i = accessRightList.iterator();
		int index = 0;
		while(i.hasNext())
		{
			AccessRight accessRight = (AccessRight)i.next();
			logger.info("Removing accessRight:" + accessRight.getId() + ":" + accessRight.getUsers().size());

			Iterator rolesIterator = accessRight.getRoles().iterator();
			while(rolesIterator.hasNext())
			{
			    AccessRightRole accessRightRole = (AccessRightRole)rolesIterator.next();
			    rolesIterator.remove();
			    db.remove(accessRightRole);
			}
			
			Iterator groupsIterator = accessRight.getGroups().iterator();
			while(groupsIterator.hasNext())
			{
			    AccessRightGroup accessRightGroup = (AccessRightGroup)groupsIterator.next();
			    groupsIterator.remove();
			    db.remove(accessRightGroup);
			}
			
			if(index > 0)
			{
				Iterator usersIterator = accessRight.getUsers().iterator();
				while(usersIterator.hasNext())
				{
				    AccessRightUser accessRightUser = (AccessRightUser)usersIterator.next();
				    usersIterator.remove();
				    db.remove(accessRightUser);
				}
	
				logger.info("OBS: removing old access right as there were duplicates: " + accessRight.getId());
				db.remove(accessRight);
			}
			else
			{
				if(deleteUsers)
				{
					Iterator usersIterator = accessRight.getUsers().iterator();
					while(usersIterator.hasNext())
					{
					    AccessRightUser accessRightUser = (AccessRightUser)usersIterator.next();
					    usersIterator.remove();
					    db.remove(accessRightUser);
					}
		
					logger.info("Deleting after users removal:" + accessRight.getId());
					db.remove(accessRight);
				}
				else
				{
				    if(accessRight.getUsers() == null || accessRight.getUsers().size() == 0)
				    {
				    	logger.info("Deleting as users was empty:" + accessRight.getId());
				        db.remove(accessRight);
				    }
				    else
				    	returnAccessRight = accessRight;
				}
			}
			
			index++;
		}
		
		return returnAccessRight;
	}        

	/**
	 * This method deletes all occurrencies of AccessRight which has the interceptionPointId.
	 * 
	 * @param roleName
	 * @throws ConstraintException
	 * @throws SystemException
	 */

	public void delete(AccessRight accessRight, Database db) throws SystemException, Exception
	{
		logger.info("Removing accessRight:" + accessRight.getId() + ":" + accessRight.getUsers().size());

		Iterator rolesIterator = accessRight.getRoles().iterator();
		while(rolesIterator.hasNext())
		{
		    AccessRightRole accessRightRole = (AccessRightRole)rolesIterator.next();
		    rolesIterator.remove();
		    db.remove(accessRightRole);
		}
		
		Iterator groupsIterator = accessRight.getGroups().iterator();
		while(groupsIterator.hasNext())
		{
		    AccessRightGroup accessRightGroup = (AccessRightGroup)groupsIterator.next();
		    groupsIterator.remove();
		    db.remove(accessRightGroup);
		}
			
		Iterator usersIterator = accessRight.getUsers().iterator();
		while(usersIterator.hasNext())
		{
		    AccessRightUser accessRightUser = (AccessRightUser)usersIterator.next();
		    usersIterator.remove();
		    db.remove(accessRightUser);
		}

		logger.info("OBS: removing access right: " + accessRight.getId());
		db.remove(accessRight);
	}        

	/**
	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
	 */
	public boolean getIsPrincipalAuthorized(InfoGluePrincipal infoGluePrincipal, String interceptionPointName, String parameters) throws SystemException
	{
	   return getIsPrincipalAuthorized(infoGluePrincipal, interceptionPointName, parameters, true);
	}

	/**
	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
	 */
	public boolean getIsPrincipalAuthorized(InfoGluePrincipal infoGluePrincipal, String interceptionPointName, String parameters, boolean returnTrueIfNoAccessRightsDefined) throws SystemException
	{
		return getIsPrincipalAuthorized(infoGluePrincipal, interceptionPointName, parameters, returnTrueIfNoAccessRightsDefined, false);
	}
	
	/**
	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
	 */
	public boolean getIsPrincipalAuthorized(InfoGluePrincipal infoGluePrincipal, String interceptionPointName, String parameters, boolean returnTrueIfNoAccessRightsDefined, boolean defeatCaches) throws SystemException
	{
	    if(infoGluePrincipal == null)
	        return false;
	        
		if(infoGluePrincipal != null && infoGluePrincipal.getIsAdministrator())
			return true;
			
		boolean isPrincipalAuthorized = false;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			isPrincipalAuthorized = getIsPrincipalAuthorized(db, infoGluePrincipal, interceptionPointName, parameters, returnTrueIfNoAccessRightsDefined, defeatCaches);
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
					
		return isPrincipalAuthorized;
	}
	

	/**
	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
	 */
	/*
	public boolean getIsPrincipalAuthorized(InfoGluePrincipal infoGluePrincipal, String interceptionPointName, String parameters, boolean returnSuccessIfInterceptionPointNotDefined, boolean returnFailureIfInterceptionPointNotDefined) throws SystemException
	{
	    if(infoGluePrincipal == null)
	        return false;
	        
		if(infoGluePrincipal != null && infoGluePrincipal.getIsAdministrator())
			return true;
			
		boolean isPrincipalAuthorized = false;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			isPrincipalAuthorized = getIsPrincipalAuthorized(db, infoGluePrincipal, interceptionPointName, parameters, returnSuccessIfInterceptionPointNotDefined, returnFailureIfInterceptionPointNotDefined);
		
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
					
		return isPrincipalAuthorized;
	}
	*/

	/**
	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
	 */
	
	public boolean getIsPrincipalAuthorized(Database db, InfoGluePrincipal infoGluePrincipal, String interceptionPointName, String extraParameters) throws SystemException
	{
		//Timer t = new Timer();
		
		boolean isAuthorized = getIsPrincipalAuthorized(db, infoGluePrincipal, interceptionPointName, extraParameters, true);
		//boolean isAuthorized = getIsPrincipalAuthorizedNew(db, infoGluePrincipal, interceptionPointName, extraParameters, true);

		//t.printElapsedTime("getIsPrincipalAuthorized took");
		
		return isAuthorized;
	}

	private static List<String> preCacheInProcessForUsers = new ArrayList<String>();
	
	/**
	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
	 */
	public boolean getIsPrincipalAuthorized(Database db, InfoGluePrincipal infoGluePrincipal, String interceptionPointName, String extraParameters, boolean returnTrueIfNoAccessRightsDefined) throws SystemException
	{		
		return getIsPrincipalAuthorized(db, infoGluePrincipal, interceptionPointName, extraParameters, returnTrueIfNoAccessRightsDefined, false);
	}
	
	public boolean getIsPrincipalAuthorized(Database db, InfoGluePrincipal infoGluePrincipal, String interceptionPointName, String extraParameters, boolean returnTrueIfNoAccessRightsDefined, boolean defeatCaches) throws SystemException
	{		
		Timer t = new Timer();
		if(!logger.isInfoEnabled())
			t.setActive(false);
		
		//Map<String,Integer> cachedPrincipalAuthorizationMap = (Map<String,Integer>)CacheController.getCachedObjectFromAdvancedCache("personalAuthorizationCache", "authorizationMap_" + infoGluePrincipal.getName());
		Map<String,Integer> cachedPrincipalAuthorizationMap = (Map<String,Integer>)CacheController.getCachedObject("userAccessCache", "authorizationMap_" + infoGluePrincipal.getName());
		if(!infoGluePrincipal.getIsAdministrator() && cachedPrincipalAuthorizationMap == null && !preCacheInProcessForUsers.contains(infoGluePrincipal.getName()))
		{
			preCacheInProcessForUsers.add(infoGluePrincipal.getName());
			logger.info("Precaching all access rights for this user");
			try 
			{
				preCacheUserAccessRightVOList(infoGluePrincipal, db);
				logger.info("Done precaching all access rights for this user");
				t.printElapsedTime("Done precaching all access rights for this user");
			} 
			catch (Exception e) 
			{
				logger.error("Error precaching all access rights for this user: " + e.getMessage(), e);
			}
			finally
			{
				preCacheInProcessForUsers.remove(infoGluePrincipal.getName());
			}
		}
		
		boolean enableDebug = false;
		/*if(interceptionPointName != null && interceptionPointName.equalsIgnoreCase("Content.Read") && 
				extraParameters != null && (extraParameters.equalsIgnoreCase("163786") || 
				extraParameters.equalsIgnoreCase("163787") || 
				extraParameters.equalsIgnoreCase("163791") || 
				extraParameters.equalsIgnoreCase("163789") ||
				extraParameters.equalsIgnoreCase("163792") ||
				extraParameters.equalsIgnoreCase("11268") ||
				extraParameters.equalsIgnoreCase("6902")))
		{
			logger.info("Was a content we want to check...");
			enableDebug = true;
		}
		*/
		String debugInfo = "";
		if(enableDebug)
			debugInfo += "\n	getIsPrincipalAuthorized with: " + infoGluePrincipal + ", " + interceptionPointName + ", " + extraParameters;
		
		//logger.info("infoGluePrincipal:" + infoGluePrincipal);
		
		if(infoGluePrincipal == null)
	      return false;
	    
	    if(infoGluePrincipal != null && infoGluePrincipal.getIsAdministrator())
			return true;
		
	    //TODO
		
	    String key = "" + infoGluePrincipal.getName() + "_" + interceptionPointName + "_" + extraParameters + "_" + returnTrueIfNoAccessRightsDefined;
	    //logger.info("key:" + key);

	    if(enableDebug)
			debugInfo += "\n	key: " + key;

	    if(logger.isInfoEnabled())
		{
			logger.info("key:" + key);
			logger.info("infoGluePrincipal:" + infoGluePrincipal.getName());
		}

		//Boolean cachedIsPrincipalAuthorized = (Boolean)CacheController.getCachedObject("authorizationCache", key);
	    //logger.info("personalAuthorizationCache:" + CacheController.getCacheSize("personalAuthorizationCache"));
		Boolean cachedIsPrincipalAuthorized = (Boolean)CacheController.getCachedObjectFromAdvancedCache("personalAuthorizationCache", key);

		if(cachedIsPrincipalAuthorized != null)
		{
			if(enableDebug)
				debugInfo += "\n	Principal " + infoGluePrincipal.getName() + " was " + (!cachedIsPrincipalAuthorized ? " NOT " : "") + " allowed to " + interceptionPointName + " on " + extraParameters + " (Cached value)";
			
			//logger.info("\n	Principal " + infoGluePrincipal.getName() + " was " + (!cachedIsPrincipalAuthorized ? " NOT " : "") + " allowed to " + interceptionPointName + " on " + extraParameters + " (Cached value)");
				
			if(logger.isInfoEnabled() && !cachedIsPrincipalAuthorized.booleanValue())
				logger.info("Principal " + infoGluePrincipal.getName() + " was not allowed to " + interceptionPointName + " on " + extraParameters + " (Cached value)");
		    return cachedIsPrincipalAuthorized.booleanValue();
		}

		boolean isPrincipalAuthorized = false;
		boolean limitOnGroups = false;
		boolean principalHasRole = false;
		boolean principalHasGroup = false;
		   
		Collection roles = infoGluePrincipal.getRoles();
		Collection groups = infoGluePrincipal.getGroups();
		if(logger.isInfoEnabled())
		{
			logger.info("roles:" + roles.size());
			logger.info("groups:" + groups.size());
		}
		
		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName(interceptionPointName, db);
		if(interceptionPointVO == null)
		{
			//logger.info("interceptionPointVO null");
			return true;
		}
		
		//ComponentEditor.%' OR name = 'Component.Select' OR name = 'ComponentPropertyEditor.EditProperty' OR name like '%.Read' AND name NOT LIKE 'SiteNodeVersion.Read
		if((interceptionPointName.indexOf("ComponentEditor.") > -1 || interceptionPointName.indexOf("Component.Select") > -1 || interceptionPointName.indexOf("ComponentPropertyEditor.EditProperty") > -1 || interceptionPointName.indexOf(".Read") > -1) && interceptionPointName.indexOf("SiteNodeVersion.Read") == -1)
		{
			//Map<String,Integer> userAccessRightsMap = (Map<String,Integer>)CacheController.getCachedObjectFromAdvancedCache("personalAuthorizationCache", "authorizationMap_" + infoGluePrincipal.getName());
			Map<String,Integer> userAccessRightsMap = (Map<String,Integer>)CacheController.getCachedObject("userAccessCache", "authorizationMap_" + infoGluePrincipal.getName());
			//Map<String,Integer> userAccessRightsMap = principalAccessRights.get("" + infoGluePrincipal.getName());
			if(userAccessRightsMap != null)
			{
				String acKey = "" + interceptionPointVO.getId();
				if(extraParameters != null && !extraParameters.equals(""))
					acKey = "" + interceptionPointVO.getId() + "_" + extraParameters;
				//logger.info("Checking access on: " + acKey);
				
				Integer hasAccess = userAccessRightsMap.get(acKey);
				//if(acKey.indexOf("RightColumnOne") > -1)
				//	logger.info("hasAccess:" + hasAccess + " on " + acKey);
				
				if(hasAccess == null)
				{
					if(returnTrueIfNoAccessRightsDefined && interceptionPointName.indexOf("ContentVersion.") > -1)
						logger.info("Double checking on access as it's a content version and those are often not protected:" + acKey);
					else
					{
					    CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", key, new Boolean(false), new String[]{infoGluePrincipal.getName()}, true);
						return false;
					}
				}
				else if(hasAccess == 1)
				{
				    CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", key, new Boolean(true), new String[]{infoGluePrincipal.getName()}, true);
					return true;
				}
				else if(hasAccess == -1)
				{
					logger.info("Unknown access to " + acKey + " - probably a duplicate access right on it:" + acKey);
				}
			}
		}
		
		logger.info("Reading the hard way:" + interceptionPointVO.getId() + ":" + extraParameters);
		
		List accessRightList = this.getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), extraParameters, db);
		if(logger.isInfoEnabled())
			logger.info("accessRightList:" + accessRightList.size());
		
		if(returnTrueIfNoAccessRightsDefined && accessRightList == null || accessRightList.size() == 0)
		{
			logger.warn("Returned true as there was no access rights defined which means it's not correctly protected.");
		    CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", key, new Boolean(true), new String[]{infoGluePrincipal.getName()}, true);
			return true;
		}
		
		//If no access rights are set for the content version we should assume it was not protected on version level.
		if((interceptionPointName.equalsIgnoreCase("ContentVersion.Read") || 
		   interceptionPointName.equalsIgnoreCase("ContentVersion.Write") || 
		   interceptionPointName.equalsIgnoreCase("ContentVersion.Delete") || 
		   interceptionPointName.equalsIgnoreCase("ContentVersion.Publish") || returnTrueIfNoAccessRightsDefined) && 
		   (accessRightList == null || accessRightList.size() == 0))
		{
		    CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", key, new Boolean(true), new String[]{infoGluePrincipal.getName()}, true);
			return true;
		}

		if(enableDebug)
			debugInfo += "\n  Access right debug:";

		Iterator accessRightListIterator = accessRightList.iterator();
		while(accessRightListIterator.hasNext() && !isPrincipalAuthorized)
		{
		    AccessRight accessRight = (AccessRight)accessRightListIterator.next();
			if(enableDebug)
				debugInfo += "\n	Access right: " + accessRight.getId();

			Collection approvedRoles = accessRight.getRoles();
		    Collection approvedGroups = accessRight.getGroups();
		    Collection approvedUsers = accessRight.getUsers();

			Iterator approvedUsersIterator = approvedUsers.iterator();
			while(approvedUsersIterator.hasNext())
			{
			    AccessRightUser accessRightUser = (AccessRightUser)approvedUsersIterator.next();
			    if(enableDebug)
					debugInfo += "\n		user:" + accessRightUser.getUserName();
			    if(accessRightUser.getUserName().equals(infoGluePrincipal.getName()))
			    {
			        isPrincipalAuthorized = true;
			    }
			}

			if(!isPrincipalAuthorized)
			{
			    Iterator rolesIterator = roles.iterator();
				outer:while(rolesIterator.hasNext())
				{
					InfoGlueRole role = (InfoGlueRole)rolesIterator.next();

					if(enableDebug)
						debugInfo += "\n		role:" + role.getName();

					if(logger.isInfoEnabled())
					    logger.info("role:" + role.getName());

					if(!role.getIsActive())
					{
					    logger.warn("skipping checking for match on role:" + role.getName() + " as it was inactive.");
						continue;
					}

					Iterator approvedRolesIterator = approvedRoles.iterator();
					while(approvedRolesIterator.hasNext())
					{
					    AccessRightRole accessRightRole = (AccessRightRole)approvedRolesIterator.next();
					    
						if(enableDebug)
							debugInfo += "\n		" + role.getName() + " = " + accessRightRole.getRoleName();

					    if(logger.isInfoEnabled())
					    	logger.info("" + role.getName() + " = " + accessRightRole.getRoleName());
					    if(accessRightRole.getRoleName().equals(role.getName()))
					    {
							if(enableDebug)
								debugInfo += "\n		Principal " + infoGluePrincipal.getName() + " has role " + accessRightRole.getRoleName();

					        if(logger.isInfoEnabled())
						    	logger.info("Principal " + infoGluePrincipal.getName() + " has role " + accessRightRole.getRoleName());
						
					        principalHasRole = true;
					        break outer;
					    }
					}
				}
	 
				Iterator approvedGroupsIterator = approvedGroups.iterator();
				outer:while(approvedGroupsIterator.hasNext())
				{
				    AccessRightGroup accessRightGroup = (AccessRightGroup)approvedGroupsIterator.next();
					
				    if(enableDebug)
						debugInfo += "\n		accessRightGroup:" + accessRightGroup.getGroupName();

					if(logger.isInfoEnabled())
					    logger.info("accessRightGroup:" + accessRightGroup.getGroupName());
	
				    limitOnGroups = true;
	
				    Iterator groupsIterator = groups.iterator();
					while(groupsIterator.hasNext())
					{
					    InfoGlueGroup group = (InfoGlueGroup)groupsIterator.next();
						if(enableDebug)
							debugInfo += "\n		group:" + group.getName();

						if(logger.isInfoEnabled())
					    	logger.info("" + group.getName() + " = " + accessRightGroup.getGroupName());

						if(!group.getIsActive())
						{
						    logger.warn("skipping checking for match on group:" + group.getName() + " as it was inactive.");
							continue;
						}

					    if(accessRightGroup.getGroupName().equals(group.getName()))
					    {
					        if(logger.isInfoEnabled())
						    	logger.info("Principal " + infoGluePrincipal.getName() + " has group " + accessRightGroup.getGroupName());

							if(enableDebug)
								debugInfo += "\n		Principal " + infoGluePrincipal.getName() + " has group " + accessRightGroup.getGroupName();

					        principalHasGroup = true;
					        break outer;
					    }
					}
				}
			}
			
			if(enableDebug)
			{
				debugInfo += "\n		principalHasRole: " + principalHasRole;
				debugInfo += "\n		principalHasGroup: " + principalHasGroup;
				debugInfo += "\n		limitOnGroups: " + limitOnGroups;
			}
		}
		
		//getCastorCategory().setLevel(Level.WARN);
		//getCastorJDOCategory().setLevel(Level.WARN);

		if(enableDebug)
		{
			debugInfo += "\n		FINAL principalHasRole: " + principalHasRole;
			debugInfo += "\n		FINAL principalHasGroup: " + principalHasGroup;
			debugInfo += "\n		FINAL limitOnGroups: " + limitOnGroups;
		}

        if(logger.isInfoEnabled())
        {
	    	logger.info("principalHasRole: " + principalHasRole);
	    	logger.info("principalHasGroup: " + principalHasGroup);
	    	logger.info("limitOnGroups: " + limitOnGroups);
        }
        
	    if((principalHasRole && principalHasGroup) || (principalHasRole && !limitOnGroups))
		    isPrincipalAuthorized = true;
		
		if(logger.isInfoEnabled() && !isPrincipalAuthorized)
		{
			logger.info("Principal " + infoGluePrincipal.getName() + " was not allowed to " + interceptionPointName + " on " + extraParameters);
		}
		logger.info("Caching " + isPrincipalAuthorized + " on " + interceptionPointName + " on " + extraParameters);
				
	    CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", key, new Boolean(isPrincipalAuthorized), new String[]{infoGluePrincipal.getName()}, true);

		return isPrincipalAuthorized;
	}
	
	
	/**
	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
	 */
	public boolean getIsPrincipalAuthorizedNew(Database db, InfoGluePrincipal infoGluePrincipal, String interceptionPointName, String extraParameters, boolean returnTrueIfNoAccessRightsDefined) throws SystemException
	{		
		logger.info("Going to check new:" + interceptionPointName + ":" + extraParameters);

		if(infoGluePrincipal == null)
	      return false;
	    
	    if(infoGluePrincipal != null && infoGluePrincipal.getIsAdministrator())
			return true;
		
	    //TODO
	    String key = "" + infoGluePrincipal.getName() + "_" + interceptionPointName + "_" + extraParameters + "_" + returnTrueIfNoAccessRightsDefined;
		if(logger.isInfoEnabled())
		{
			logger.info("key:" + key);
			logger.info("infoGluePrincipal:" + infoGluePrincipal.getName());
		}

		//Boolean cachedIsPrincipalAuthorized = (Boolean)CacheController.getCachedObject("authorizationCache", key);
		Boolean cachedIsPrincipalAuthorized = (Boolean)CacheController.getCachedObjectFromAdvancedCache("personalAuthorizationCache", key);
		if(cachedIsPrincipalAuthorized != null)
		{
			if(logger.isInfoEnabled() && !cachedIsPrincipalAuthorized.booleanValue())
				logger.info("Principal " + infoGluePrincipal.getName() + " was not allowed to " + interceptionPointName + " on " + extraParameters + " (Cached value)");
		    return cachedIsPrincipalAuthorized.booleanValue();
		}

		boolean isPrincipalAuthorized = false;
		boolean limitOnGroups = false;
		boolean principalHasRole = false;
		boolean principalHasGroup = false;
		   
		Collection roles = infoGluePrincipal.getRoles();
		Collection groups = infoGluePrincipal.getGroups();
		if(logger.isInfoEnabled())
		{
			logger.info("roles:" + roles.size());
			logger.info("groups:" + groups.size());
		}
		
		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName(interceptionPointName, db);
		if(interceptionPointVO == null)
			return true;

		List<AccessRightUserVO> accessRightUserVOList = this.getAccessRightUserVOList(interceptionPointVO.getId(), extraParameters, db);
		
		//If no access rights are set for the content version we should assume it was not protected on version level.
		if(interceptionPointName.equalsIgnoreCase("ContentVersion.Read") || 
		   interceptionPointName.equalsIgnoreCase("ContentVersion.Write") || 
		   interceptionPointName.equalsIgnoreCase("ContentVersion.Delete") || 
		   interceptionPointName.equalsIgnoreCase("ContentVersion.Publish"))
		{
			return true;
		}

		for(AccessRightUserVO accessRightUserVO : accessRightUserVOList)
		{
		    if(accessRightUserVO.getUserName().equals(infoGluePrincipal.getName()))
		    {
		        isPrincipalAuthorized = true;
		    }
		}

		if(!isPrincipalAuthorized)
		{
			List<AccessRightRoleVO> accessRightRoleVOList = this.getAccessRightRoleVOList(interceptionPointVO.getId(), extraParameters, db);
			List<AccessRightGroupVO> accessRightGroupVOList = this.getAccessRightGroupVOList(interceptionPointVO.getId(), extraParameters, db);
			
			if(returnTrueIfNoAccessRightsDefined && ((accessRightUserVOList == null || accessRightUserVOList.size() == 0) && (accessRightRoleVOList == null || accessRightRoleVOList.size() == 0) && (accessRightGroupVOList == null || accessRightGroupVOList.size() == 0)))
				return true;
						   
			Iterator rolesIterator = roles.iterator();
			outer:while(rolesIterator.hasNext())
			{
				InfoGlueRole role = (InfoGlueRole)rolesIterator.next();
				if(logger.isInfoEnabled())
				    logger.info("role:" + role.getName());

				if(!role.getIsActive())
				{
				    logger.warn("skipping checking for match on role:" + role.getName() + " as it was inactive.");
					continue;
				}
				
				Iterator approvedRolesIterator = accessRightRoleVOList.iterator();
				while(approvedRolesIterator.hasNext())
				{
					AccessRightRoleVO accessRightRole = (AccessRightRoleVO)approvedRolesIterator.next();
				    if(logger.isInfoEnabled())
				    	logger.info("" + role.getName() + " = " + accessRightRole.getRoleName());
				    if(accessRightRole.getRoleName().equals(role.getName()))
				    {
				        if(logger.isInfoEnabled())
					    	logger.info("Principal " + infoGluePrincipal.getName() + " has role " + accessRightRole.getRoleName());
					
				        principalHasRole = true;
				        break outer;
				    }
				}
			}
 
			Iterator approvedGroupsIterator = accessRightGroupVOList.iterator();
			outer:while(approvedGroupsIterator.hasNext())
			{
			    AccessRightGroupVO accessRightGroup = (AccessRightGroupVO)approvedGroupsIterator.next();
			    if(logger.isInfoEnabled())
				    logger.info("accessRightGroup:" + accessRightGroup.getGroupName());

			    limitOnGroups = true;

			    Iterator groupsIterator = groups.iterator();
				while(groupsIterator.hasNext())
				{
				    InfoGlueGroup group = (InfoGlueGroup)groupsIterator.next();
				    if(logger.isInfoEnabled())
				    	logger.info("" + group.getName() + " = " + accessRightGroup.getGroupName());

					if(!group.getIsActive())
					{
					    logger.warn("skipping checking for match on group:" + group.getName() + " as it was inactive.");
						continue;
					}

				    if(accessRightGroup.getGroupName().equals(group.getName()))
				    {
				        if(logger.isInfoEnabled())
					    	logger.info("Principal " + infoGluePrincipal.getName() + " has group " + accessRightGroup.getGroupName());
					
				        principalHasGroup = true;
				        break outer;
				    }
				}
			}
		}
		
        if(logger.isInfoEnabled())
        {
	    	logger.info("principalHasRole: " + principalHasRole);
	    	logger.info("principalHasGroup: " + principalHasGroup);
	    	logger.info("limitOnGroups: " + limitOnGroups);
        }
        
	    if((principalHasRole && principalHasGroup) || (principalHasRole && !limitOnGroups))
		    isPrincipalAuthorized = true;
		
		if(logger.isInfoEnabled() && !isPrincipalAuthorized)
		{
			logger.info("Principal " + infoGluePrincipal.getName() + " was not allowed to " + interceptionPointName + " on " + extraParameters);
		}
		
	    //CacheController.cacheObject("authorizationCache", key, new Boolean(isPrincipalAuthorized));
	    CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", key, new Boolean(isPrincipalAuthorized), new String[]{infoGluePrincipal.getName()}, true);

		return isPrincipalAuthorized;
	}
	
	/**
	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
	 */

	public boolean getIsPrincipalAuthorized(InfoGluePrincipal infoGluePrincipal, String interceptionPointName) throws SystemException
	{
		return getIsPrincipalAuthorized(infoGluePrincipal, interceptionPointName, false);
	}

	/**
	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
	 */

	public boolean getIsPrincipalAuthorized(InfoGluePrincipal infoGluePrincipal, String interceptionPointName, boolean returnSuccessIfInterceptionPointNotDefined, boolean returnFailureIfInterceptionPointNotDefined) throws SystemException
	{
		if(infoGluePrincipal.getIsAdministrator())
			return true;
			
		String key = "" + infoGluePrincipal.getName() + "_" + interceptionPointName + "_" + returnSuccessIfInterceptionPointNotDefined;
		logger.info("key:" + key);
		//Boolean cachedIsPrincipalAuthorized = (Boolean)CacheController.getCachedObject("authorizationCache", key);
		Boolean cachedIsPrincipalAuthorized = (Boolean)CacheController.getCachedObjectFromAdvancedCache("personalAuthorizationCache", key);
		if(cachedIsPrincipalAuthorized != null)
		{
			if(logger.isInfoEnabled() && !cachedIsPrincipalAuthorized.booleanValue())
				logger.info("Principal " + infoGluePrincipal.getName() + " was not allowed to " + interceptionPointName);

			return cachedIsPrincipalAuthorized.booleanValue();
		}
		
		boolean isPrincipalAuthorized = false;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			isPrincipalAuthorized = getIsPrincipalAuthorized(db, infoGluePrincipal, interceptionPointName, returnSuccessIfInterceptionPointNotDefined, returnFailureIfInterceptionPointNotDefined, true);
			
		    CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", key, new Boolean(isPrincipalAuthorized), new String[]{infoGluePrincipal.getName()}, true);
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			logger.info("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
					
		return isPrincipalAuthorized;
	}

	/**
	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
	 */

	public boolean getIsPrincipalAuthorized(InfoGluePrincipal infoGluePrincipal, String interceptionPointName, boolean returnSuccessIfInterceptionPointNotDefined) throws SystemException
	{
		if(infoGluePrincipal.getIsAdministrator())
			return true;
			
		String key = "" + infoGluePrincipal.getName() + "_" + interceptionPointName + "_" + returnSuccessIfInterceptionPointNotDefined;
		logger.info("key:" + key);
		//Boolean cachedIsPrincipalAuthorized = (Boolean)CacheController.getCachedObject("authorizationCache", key);
		Boolean cachedIsPrincipalAuthorized = (Boolean)CacheController.getCachedObjectFromAdvancedCache("personalAuthorizationCache", key);
		if(cachedIsPrincipalAuthorized != null)
		{
			if(logger.isInfoEnabled() && !cachedIsPrincipalAuthorized.booleanValue())
				logger.info("Principal " + infoGluePrincipal.getName() + " was not allowed to " + interceptionPointName);

			return cachedIsPrincipalAuthorized.booleanValue();
		}
		
		boolean isPrincipalAuthorized = false;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			isPrincipalAuthorized = getIsPrincipalAuthorized(db, infoGluePrincipal, interceptionPointName, returnSuccessIfInterceptionPointNotDefined, false, true);
			
			//CacheController.cacheObject("authorizationCache", key, new Boolean(isPrincipalAuthorized));
		    CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", key, new Boolean(isPrincipalAuthorized), new String[]{infoGluePrincipal.getName()}, true);
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
					
		return isPrincipalAuthorized;
	}

	/**
	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
	 */

	public boolean getIsPrincipalAuthorized(InfoGluePrincipal infoGluePrincipal, String interceptionPointName, boolean returnSuccessIfInterceptionPointNotDefined, boolean returnFailureIfInterceptionPointNotDefined, boolean returnTrueIfNoAccessRightsDefined) throws SystemException
	{
		if(infoGluePrincipal.getIsAdministrator())
			return true;
			
		String key = "" + infoGluePrincipal.getName() + "_" + interceptionPointName + "_" + returnSuccessIfInterceptionPointNotDefined + "_" + returnFailureIfInterceptionPointNotDefined + "_" + returnTrueIfNoAccessRightsDefined;
		logger.info("key:" + key);
		//Boolean cachedIsPrincipalAuthorized = (Boolean)CacheController.getCachedObject("authorizationCache", key);
		Boolean cachedIsPrincipalAuthorized = (Boolean)CacheController.getCachedObjectFromAdvancedCache("personalAuthorizationCache", key);
		if(cachedIsPrincipalAuthorized != null)
		{
			if(logger.isInfoEnabled() && !cachedIsPrincipalAuthorized.booleanValue())
				logger.info("Principal " + infoGluePrincipal.getName() + " was not allowed to " + interceptionPointName);

			return cachedIsPrincipalAuthorized.booleanValue();
		}
		
		boolean isPrincipalAuthorized = false;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			isPrincipalAuthorized = getIsPrincipalAuthorized(db, infoGluePrincipal, interceptionPointName, returnSuccessIfInterceptionPointNotDefined, returnFailureIfInterceptionPointNotDefined, returnTrueIfNoAccessRightsDefined);
			
			//CacheController.cacheObject("authorizationCache", key, new Boolean(isPrincipalAuthorized));
		    CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", key, new Boolean(isPrincipalAuthorized), new String[]{infoGluePrincipal.getName()}, true);
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
					
		return isPrincipalAuthorized;
	}

	/**
	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
	 */

	public boolean getIsPrincipalAuthorized(Database db, InfoGluePrincipal infoGluePrincipal, String interceptionPointName) throws SystemException
	{		
		boolean isAuthorized = getIsPrincipalAuthorized(db, infoGluePrincipal, interceptionPointName, false, false, true);
		
		return isAuthorized;
	}
	
	/**
	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
	 */

	public boolean getIsPrincipalAuthorized(Database db, InfoGluePrincipal infoGluePrincipal, String interceptionPointName, boolean returnSuccessIfInterceptionPointNotDefined, boolean returnFailureIfInterceptionPointNotDefined, boolean returnTrueIfNoAccessRightsDefined) throws SystemException
	{		
		Timer t = new Timer();

		//logger.info("getIsPrincipalAuthorized 2:" + interceptionPointName);
		//Thread.dumpStack();
		
		Map<String,Integer> cachedPrincipalAuthorizationMap = (Map<String,Integer>)CacheController.getCachedObject("userAccessCache", "authorizationMap_" + infoGluePrincipal.getName());
		//Map<String,Integer> cachedPrincipalAuthorizationMap = (Map<String,Integer>)CacheController.getCachedObjectFromAdvancedCache("personalAuthorizationCache", "authorizationMap_" + infoGluePrincipal.getName());
		if(!infoGluePrincipal.getIsAdministrator() && cachedPrincipalAuthorizationMap == null && !preCacheInProcessForUsers.contains(infoGluePrincipal.getName()))
		{
			preCacheInProcessForUsers.add(infoGluePrincipal.getName());
			
			logger.info("Precaching all access rights for this user");
			try 
			{
				preCacheUserAccessRightVOList(infoGluePrincipal, db);
				logger.info("Done precaching all access rights for this user");
				t.printElapsedTime("Done precaching all access rights for this user");
			} 
			catch (Exception e) 
			{
				logger.error("Error precaching all access rights for this user: " + e.getMessage(), e);
			}
			finally
			{
				preCacheInProcessForUsers.remove(infoGluePrincipal.getName());
			}
		}
		
		
	    if(infoGluePrincipal.getIsAdministrator())
			return true;

	    String key = "" + infoGluePrincipal.getName() + "_" + interceptionPointName + "_" + returnTrueIfNoAccessRightsDefined;

		Boolean cachedIsPrincipalAuthorized = (Boolean)CacheController.getCachedObjectFromAdvancedCache("personalAuthorizationCache", key);
		if(cachedIsPrincipalAuthorized != null)
		{
			if(logger.isInfoEnabled() && !cachedIsPrincipalAuthorized.booleanValue())
				logger.info("Principal " + infoGluePrincipal.getName() + " was not allowed to " + interceptionPointName + " (Cached value)");
		    return cachedIsPrincipalAuthorized.booleanValue();
		}

		boolean isPrincipalAuthorized = false;
		boolean limitOnGroups = false;
		boolean principalHasRole = false;
		boolean principalHasGroup = false;
		   
		Collection roles = infoGluePrincipal.getRoles();
		Collection groups = infoGluePrincipal.getGroups();
		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName(interceptionPointName, db);
		if(interceptionPointVO == null && returnSuccessIfInterceptionPointNotDefined)
		{
		    CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", key, new Boolean(true), new String[]{infoGluePrincipal.getName()}, true);
		    return true;
		}
		
		if(interceptionPointVO == null && returnFailureIfInterceptionPointNotDefined)
		{
		    CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", key, new Boolean(false), new String[]{infoGluePrincipal.getName()}, true);
			return false;
		}
		
		if(interceptionPointVO == null)
			return true;
			
		//if(interceptionPointName.indexOf(".Read") > -1 && interceptionPointName.indexOf("SiteNodeVersion.Read") == -1)
		if((interceptionPointName.indexOf("ComponentEditor.") > -1 || interceptionPointName.indexOf("Component.Select") > -1 || interceptionPointName.indexOf("ComponentPropertyEditor.EditProperty") > -1 || interceptionPointName.indexOf(".Read") > -1) && interceptionPointName.indexOf("SiteNodeVersion.Read") == -1)
		{
			Map<String,Integer> userAccessRightsMap = (Map<String,Integer>)CacheController.getCachedObject("userAccessCache", "authorizationMap_" + infoGluePrincipal.getName());
			//Map<String,Integer> userAccessRightsMap = (Map<String,Integer>)CacheController.getCachedObjectFromAdvancedCache("personalAuthorizationCache", "authorizationMap_" + infoGluePrincipal.getName());
			if(userAccessRightsMap != null)
			{
				String acKey = "" + interceptionPointVO.getId();
				//logger.info("Checking access on: " + acKey);
				Integer hasAccess = userAccessRightsMap.get(acKey);
				if(hasAccess == null)
				{
					CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", key, new Boolean(false), new String[]{infoGluePrincipal.getName()}, true);
					return false;
				}
				else if(hasAccess == 1)
				{
					CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", key, new Boolean(true), new String[]{infoGluePrincipal.getName()}, true);
					return true;
				}
				else if(hasAccess == -1)
				{
					logger.info("Unknown access to " + acKey + " - probably a duplicate access right on it:" + acKey);
				}
			}
		}
		
		logger.info("Reading the hard way: " + interceptionPointVO.getName());

		//List accessRightList = this.getAccessRightList(interceptionPointVO.getId(), db);
		List accessRightList = this.getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), db);
		if(logger.isInfoEnabled())
			logger.info("accessRightList:" + accessRightList.size());
		//If no access rights are set for the content version we should assume it was not protected on version level.
		if((interceptionPointName.equalsIgnoreCase("ContentVersion.Read") || 
		   interceptionPointName.equalsIgnoreCase("ContentVersion.Write") || 
		   interceptionPointName.equalsIgnoreCase("ContentVersion.Delete") || 
		   interceptionPointName.equalsIgnoreCase("ContentVersion.Publish") || returnTrueIfNoAccessRightsDefined) && 
		   (accessRightList == null || accessRightList.size() == 0))
		{
			CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", key, new Boolean(true), new String[]{infoGluePrincipal.getName()}, true);
			return true;
		}

		Iterator accessRightListIterator = accessRightList.iterator();
		while(accessRightListIterator.hasNext() && !isPrincipalAuthorized)
		{
		    AccessRight accessRight = (AccessRight)accessRightListIterator.next();
		    Collection approvedRoles = accessRight.getRoles();
		    Collection approvedGroups = accessRight.getGroups();
		    Collection approvedUsers = accessRight.getUsers();

			Iterator approvedUsersIterator = approvedUsers.iterator();
			while(approvedUsersIterator.hasNext())
			{
			    AccessRightUser accessRightUser = (AccessRightUser)approvedUsersIterator.next();
			    if(accessRightUser.getUserName().equals(infoGluePrincipal.getName()))
			    {
			        isPrincipalAuthorized = true;
			    }
			}

			if(!isPrincipalAuthorized)
			{
			    Iterator rolesIterator = roles.iterator();
				outer:while(rolesIterator.hasNext())
				{
					InfoGlueRole role = (InfoGlueRole)rolesIterator.next();
					logger.info("role:" + role.getName());

					if(!role.getIsActive())
					{
					    logger.warn("skipping checking for match on role:" + role.getName() + " as it was inactive.");
						continue;
					}

					Iterator approvedRolesIterator = approvedRoles.iterator();
					while(approvedRolesIterator.hasNext())
					{
					    AccessRightRole accessRightRole = (AccessRightRole)approvedRolesIterator.next();
					    if(accessRightRole.getRoleName().equals(role.getName()))
					    {
					        principalHasRole = true;
					        break outer;
					    }
					}
				}
	 
			    Iterator approvedGroupsIterator = approvedGroups.iterator();
				outer:while(approvedGroupsIterator.hasNext())
				{
				    AccessRightGroup accessRightGroup = (AccessRightGroup)approvedGroupsIterator.next();
				    logger.info("accessRightGroup:" + accessRightGroup.getGroupName());
	
				    limitOnGroups = true;
	
				    Iterator groupsIterator = groups.iterator();
					while(groupsIterator.hasNext())
					{
					    InfoGlueGroup group = (InfoGlueGroup)groupsIterator.next();

					    if(!group.getIsActive())
						{
						    logger.warn("skipping checking for match on group:" + group.getName() + " as it was inactive.");
							continue;
						}

					    if(accessRightGroup.getGroupName().equals(group.getName()))
					    {
					        principalHasGroup = true;
					        break outer;
					    }
					}
				}
			}
		}

	    if((principalHasRole && principalHasGroup) || (principalHasRole && !limitOnGroups))
		    isPrincipalAuthorized = true;
		
		if(logger.isInfoEnabled() && !isPrincipalAuthorized)
			logger.info("Principal " + infoGluePrincipal.getName() + " was not allowed to " + interceptionPointName);
		
		if(logger.isInfoEnabled())
			logger.info("isPrincipalAuthorized:" + isPrincipalAuthorized);

		CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", key, new Boolean(isPrincipalAuthorized), new String[]{infoGluePrincipal.getName()}, true);

		return isPrincipalAuthorized;
	}

	
	public boolean getIsPrincipalAuthorizedNew(Database db, InfoGluePrincipal infoGluePrincipal, String interceptionPointName, boolean returnSuccessIfInterceptionPointNotDefined, boolean returnFailureIfInterceptionPointNotDefined, boolean returnTrueIfNoAccessRightsDefined) throws SystemException
	{		
	    if(infoGluePrincipal.getIsAdministrator())
			return true;
	
		boolean isPrincipalAuthorized = false;
		boolean limitOnGroups = false;
		boolean principalHasRole = false;
		boolean principalHasGroup = false;
		   
		Collection roles = infoGluePrincipal.getRoles();
		Collection groups = infoGluePrincipal.getGroups();
		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName(interceptionPointName, db);
		//InterceptionPoint interceptionPoint = InterceptionPointController.getController().getInterceptionPointWithName(interceptionPointName, db);
		if(interceptionPointVO == null && returnSuccessIfInterceptionPointNotDefined)
			return true;
		
		if(interceptionPointVO == null && returnFailureIfInterceptionPointNotDefined)
			return false;
		
		if(interceptionPointVO == null)
			return true;
			
		List<AccessRightUserVO> accessRightUserVOList = this.getAccessRightUserVOList(interceptionPointVO.getId(), null, db);
		
		//If no access rights are set for the content version we should assume it was not protected on version level.
		if(interceptionPointName.equalsIgnoreCase("ContentVersion.Read") || 
		   interceptionPointName.equalsIgnoreCase("ContentVersion.Write") || 
		   interceptionPointName.equalsIgnoreCase("ContentVersion.Delete") || 
		   interceptionPointName.equalsIgnoreCase("ContentVersion.Publish"))
		{
			return true;
		}

		for(AccessRightUserVO accessRightUserVO : accessRightUserVOList)
		{
		    if(accessRightUserVO.getUserName().equals(infoGluePrincipal.getName()))
		    {
		        isPrincipalAuthorized = true;
		    }
		}
		
		if(!isPrincipalAuthorized)
		{
			List<AccessRightRoleVO> accessRightRoleVOList = this.getAccessRightRoleVOList(interceptionPointVO.getId(), null, db);
			List<AccessRightGroupVO> accessRightGroupVOList = this.getAccessRightGroupVOList(interceptionPointVO.getId(), null, db);
			
			if(returnTrueIfNoAccessRightsDefined && ((accessRightUserVOList == null || accessRightUserVOList.size() == 0) && (accessRightRoleVOList == null || accessRightRoleVOList.size() == 0) && (accessRightGroupVOList == null || accessRightGroupVOList.size() == 0)))
				return true;
						   
			Iterator rolesIterator = roles.iterator();
			outer:while(rolesIterator.hasNext())
			{
				InfoGlueRole role = (InfoGlueRole)rolesIterator.next();
				if(logger.isInfoEnabled())
				    logger.info("role:" + role.getName());

				if(!role.getIsActive())
				{
				    logger.warn("skipping checking for match on role:" + role.getName() + " as it was inactive.");
					continue;
				}
				
				Iterator approvedRolesIterator = accessRightRoleVOList.iterator();
				while(approvedRolesIterator.hasNext())
				{
					AccessRightRoleVO accessRightRole = (AccessRightRoleVO)approvedRolesIterator.next();
				    if(logger.isInfoEnabled())
				    	logger.info("" + role.getName() + " = " + accessRightRole.getRoleName());
				    if(accessRightRole.getRoleName().equals(role.getName()))
				    {
				        if(logger.isInfoEnabled())
					    	logger.info("Principal " + infoGluePrincipal.getName() + " has role " + accessRightRole.getRoleName());
					
				        principalHasRole = true;
				        break outer;
				    }
				}
			}
 
			Iterator approvedGroupsIterator = accessRightGroupVOList.iterator();
			outer:while(approvedGroupsIterator.hasNext())
			{
			    AccessRightGroupVO accessRightGroup = (AccessRightGroupVO)approvedGroupsIterator.next();
			    if(logger.isInfoEnabled())
				    logger.info("accessRightGroup:" + accessRightGroup.getGroupName());

			    limitOnGroups = true;

			    Iterator groupsIterator = groups.iterator();
				while(groupsIterator.hasNext())
				{
				    InfoGlueGroup group = (InfoGlueGroup)groupsIterator.next();
				    if(logger.isInfoEnabled())
				    	logger.info("" + group.getName() + " = " + accessRightGroup.getGroupName());

					if(!group.getIsActive())
					{
					    logger.warn("skipping checking for match on group:" + group.getName() + " as it was inactive.");
						continue;
					}

				    if(accessRightGroup.getGroupName().equals(group.getName()))
				    {
				        if(logger.isInfoEnabled())
					    	logger.info("Principal " + infoGluePrincipal.getName() + " has group " + accessRightGroup.getGroupName());
					
				        principalHasGroup = true;
				        break outer;
				    }
				}
			}
		}
	
	    if((principalHasRole && principalHasGroup) || (principalHasRole && !limitOnGroups))
		    isPrincipalAuthorized = true;
		
		if(logger.isInfoEnabled() && !isPrincipalAuthorized)
			logger.info("Principal " + infoGluePrincipal.getName() + " was not allowed to " + interceptionPointName);
		
		if(logger.isInfoEnabled())
			logger.info("isPrincipalAuthorized:" + isPrincipalAuthorized);
	    
		return isPrincipalAuthorized;
	}

	public Collection<AccessRightsUserRow> getAccessRightsUserRows(String interceptionPointCategory, String parameters) throws SystemException, Bug
	{
		Collection<AccessRightsUserRow> principalVOList = null;

		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);

			principalVOList = getAccessRightsUserRows(interceptionPointCategory, parameters, db);

			commitTransaction(db);
		} 
		catch (Exception e)
		{
			logger.warn("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}

		return principalVOList;
	}

	public Collection<AccessRightsUserRow> getAccessRightsUserRows(String interceptionPointCategory, String parameters, Database db) throws SystemException, Bug
	{
	    Map<String, AccessRightsUserRow> accessRightsUserRows = new HashMap<String, AccessRightsUserRow>();

		try
		{
		    List accessRightUsers = getAccessRightsUsers(interceptionPointCategory, parameters, db, true);

		    Iterator accessRightUsersIterator = accessRightUsers.iterator();
			while (accessRightUsersIterator.hasNext()) 
			{
			    try
			    {
					AccessRightUser accessRightUser = (AccessRightUser)accessRightUsersIterator.next();

					AccessRightsUserRow accessRightsUserRow = (AccessRightsUserRow)accessRightsUserRows.get(accessRightUser.getUserName());
					if(accessRightsUserRow == null)
					{
						InfoGluePrincipal infoGluePrincipal = UserControllerProxy.getController(db).getUser(accessRightUser.getUserName());
						if(infoGluePrincipal != null)
					    {
					        AccessRightsUserRow newAccessRightsUserRow = new AccessRightsUserRow();
					        newAccessRightsUserRow.setUserName(infoGluePrincipal.getName());
					        newAccessRightsUserRow.getAccessRights().put(accessRightUser.getAccessRight().getInterceptionPoint().getId(), new Boolean(true));
					        accessRightsUserRows.put(infoGluePrincipal.getName(), newAccessRightsUserRow);
					    }
					}
					else
					{
					    accessRightsUserRow.getAccessRights().put(accessRightUser.getAccessRight().getInterceptionPoint().getId(), new Boolean(true));
					}
			    }
			    catch(Exception e)
			    {
			        logger.warn("An user did not exist although given access rights:" + e.getMessage());
			    }
			}
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);
		}

		return accessRightsUserRows.values();
	}

	public List getAccessRightsUsers(String interceptionPointCategory, String parameters, Database db, boolean readOnly) throws SystemException, Bug
	{
	    List accessRightsUsers = new ArrayList();
		
		try
		{
			OQLQuery oql = null;
			
			if(parameters == null || parameters.length() == 0)
			{
				oql = db.getOQLQuery("SELECT aru FROM org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl aru WHERE aru.accessRight.interceptionPoint.category = $1 AND (is_undefined(aru.accessRight.parameters) OR aru.accessRight.parameters = $2)");
				oql.bind(interceptionPointCategory);
				oql.bind(parameters);
			}
			else
			{
		    	oql = db.getOQLQuery("SELECT aru FROM org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl aru WHERE aru.accessRight.interceptionPoint.category = $1 AND aru.accessRight.parameters = $2");
				oql.bind(interceptionPointCategory);
				oql.bind(parameters);
			}
			
			QueryResults results = null;
			
			if(readOnly)
				results = oql.execute(Database.READONLY);
			else
				results = oql.execute();
				
			while (results.hasMore()) 
			{
				AccessRightUser accessRightUser = (AccessRightUser)results.next();
				accessRightsUsers.add(accessRightUser);
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightsUsers;		
	}

	public List getAccessRightsUsers(String interceptionPointCategory, String parameters, String userName, Database db) throws SystemException, Bug
	{
	    List accessRightsUsers = new ArrayList();
		
		try
		{
			OQLQuery oql = null;
			
			if(parameters == null || parameters.length() == 0)
			{
				oql = db.getOQLQuery("SELECT aru FROM org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl aru WHERE aru.accessRight.interceptionPoint.category = $1 AND (is_undefined(aru.accessRight.parameters) OR aru.accessRight.parameters = $2) AND aru.userName = $3");
				oql.bind(interceptionPointCategory);
				oql.bind(parameters);
				oql.bind(userName);
			}
			else
			{
		    	oql = db.getOQLQuery("SELECT aru FROM org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl aru WHERE aru.accessRight.interceptionPoint.category = $1 AND aru.accessRight.parameters = $2 AND aru.userName = $3");
				oql.bind(interceptionPointCategory);
				oql.bind(parameters);
				oql.bind(userName);
			}

			QueryResults results = oql.execute();

			while (results.hasMore()) 
			{
				AccessRightUser accessRightUser = (AccessRightUser)results.next();
				accessRightsUsers.add(accessRightUser);
			}

			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightsUsers;		
	}
	
	//TEST
	
	
	public List getAccessRightUserList(String userName, Database db) throws SystemException, Bug
	{
		List accessRightUserList = new ArrayList();
		
		try
		{
			OQLQuery oql = null;
			
	    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl f WHERE f.userName = $1");
			oql.bind(userName);

			QueryResults results = oql.execute();

			while (results.hasMore()) 
			{
				AccessRightUser accessRightUser = (AccessRightUser)results.next();

				//Dummy to get the access right to load correctly - otherwise a but occurrs.
				Integer accessRightId = accessRightUser.getAccessRight().getAccessRightId();

				accessRightUserList.add(accessRightUser);
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
		    e.printStackTrace();
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights users. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightUserList;		
	}

	public List getAccessRightRoleList(String roleName, Database db, boolean readOnly) throws SystemException, Bug
	{
		List accessRightRoleList = new ArrayList();
		
		try
		{
			OQLQuery oql = null;
			
	    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl f WHERE f.roleName = $1");
			oql.bind(roleName);

			QueryResults results;
			if(readOnly)
				results = oql.execute(Database.READONLY);
			else
				results = oql.execute();
				
			while (results.hasMore()) 
			{
				AccessRightRole accessRightRole = (AccessRightRole)results.next();
				//Dummy to get the access right to load correctly - otherwise a but occurrs.
				Integer accessRightId = accessRightRole.getAccessRight().getAccessRightId();

				if(accessRightRole.getAccessRight() == null && !readOnly)
					db.remove(accessRightRole);
				else
					accessRightRoleList.add(accessRightRole);
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
		    e.printStackTrace();
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights users. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightRoleList;		
	}

	public List getAccessRightsRoles(String interceptionPointCategory, String parameters, Database db, boolean readOnly) throws SystemException, Bug
	{
	    List accessRightsRoles = new ArrayList();
		
		try
		{
			OQLQuery oql = null;
			
			if(parameters == null || parameters.length() == 0)
			{
				oql = db.getOQLQuery("SELECT aru FROM org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl aru WHERE aru.accessRight.interceptionPoint.category = $1 AND (is_undefined(aru.accessRight.parameters) OR aru.accessRight.parameters = $2)");
				oql.bind(interceptionPointCategory);
				oql.bind(parameters);
			}
			else
			{
		    	oql = db.getOQLQuery("SELECT aru FROM org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl aru WHERE aru.accessRight.interceptionPoint.category = $1 AND aru.accessRight.parameters = $2");
				oql.bind(interceptionPointCategory);
				oql.bind(parameters);
			}
			
			QueryResults results = null;
			
			if(readOnly)
				results = oql.execute(Database.READONLY);
			else
				results = oql.execute();
				
			while (results.hasMore()) 
			{
				AccessRightRole accessRightRole = (AccessRightRole)results.next();
				accessRightsRoles.add(accessRightRole);
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightsRoles;		
	}

	public List<AccessRightRole> getAccessRightsRole(String interceptionPointCategory, String roleName, String parameters, Database db, boolean readOnly) throws SystemException, Bug
	{
		List<AccessRightRole> accessRightsRoles = new ArrayList<AccessRightRole>();

		try
		{
			OQLQuery oql = null;

			if(parameters == null || parameters.length() == 0)
			{
				oql = db.getOQLQuery("SELECT aru FROM org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl aru WHERE aru.accessRight.interceptionPoint.category = $1 AND aru.roleName = $2 AND (is_undefined(aru.accessRight.parameters) OR aru.accessRight.parameters = $3)");
				oql.bind(interceptionPointCategory);
				oql.bind(roleName);
				oql.bind(parameters);
			}
			else
			{
				oql = db.getOQLQuery("SELECT aru FROM org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl aru WHERE aru.accessRight.interceptionPoint.category = $1 AND aru.roleName = $2 AND aru.accessRight.parameters = $3");
				oql.bind(interceptionPointCategory);
				oql.bind(roleName);
				oql.bind(parameters);
			}

			QueryResults results = null;

			if(readOnly)
				results = oql.execute(Database.READONLY);
			else
				results = oql.execute();

			while (results.hasMore())
			{
				AccessRightRole accessRightRole = (AccessRightRole)results.next();
				accessRightsRoles.add(accessRightRole);
			}

			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights for role <" + roleName + ">. Reason:" + e.getMessage(), e);
		}

		return accessRightsRoles;
	}

	public List getAccessRightsGroups(String interceptionPointCategory, String parameters, Database db, boolean readOnly) throws SystemException, Bug
	{
	    List accessRightsGroups = new ArrayList();
		
		try
		{
			OQLQuery oql = null;
			
			if(parameters == null || parameters.length() == 0)
			{
				oql = db.getOQLQuery("SELECT aru FROM org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl aru WHERE aru.accessRight.interceptionPoint.category = $1 AND (is_undefined(aru.accessRight.parameters) OR aru.accessRight.parameters = $2)");
				oql.bind(interceptionPointCategory);
				oql.bind(parameters);
			}
			else
			{
		    	oql = db.getOQLQuery("SELECT aru FROM org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl aru WHERE aru.accessRight.interceptionPoint.category = $1 AND aru.accessRight.parameters = $2");
				oql.bind(interceptionPointCategory);
				oql.bind(parameters);
			}
			
			QueryResults results = null;
			
			if(readOnly)
				results = oql.execute(Database.READONLY);
			else
				results = oql.execute();
				
			while (results.hasMore()) 
			{
				AccessRightGroup accessRightGroup = (AccessRightGroup)results.next();
				accessRightsGroups.add(accessRightGroup);
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightsGroups;		
	}

	public List getAccessRightGroupList(String groupName, Database db) throws SystemException, Bug
	{
		List accessRightGroupList = new ArrayList();
		
		try
		{
			OQLQuery oql = null;
			
	    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl f WHERE f.groupName = $1");
			oql.bind(groupName);

			QueryResults results = oql.execute();

			while (results.hasMore()) 
			{
				AccessRightGroup accessRightGroup = (AccessRightGroup)results.next();

				//Dummy to get the access right to load correctly - otherwise a but occurrs.
				Integer accessRightId = accessRightGroup.getAccessRight().getAccessRightId();

				accessRightGroupList.add(accessRightGroup);
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
		    e.printStackTrace();
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights users. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightGroupList;		
	}

	/**
	 * Returns all system user names used in any access rights related queries.
	 */
	
	public List<String> getUniqueSystemUserNameListInAccessRightUser(Database db) throws Exception
	{
		List<String> users = new ArrayList<String>();
		
		OQLQuery oql = db.getOQLQuery("CALL SQL select max(accessRightUserId), userName, max(accessRightId) from cmAccessRightUser aru group by userName AS org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl");
 
    	QueryResults results = oql.execute(Database.READONLY);
		while(results.hasMore()) 
        {
			AccessRightUserImpl aru = (AccessRightUserImpl)results.next();
			users.add(aru.getUserName());
		}

		results.close();
		oql.close();
        
        return users;
	}

	/**
	 * This method get all unique names used in cmAccessRightRole
	 * @param db
	 * @return
	 * @throws Exception
	 */
	public List<String> getUniqueRoleNameListInAccessRightRole(Database db) throws Exception
	{
		List<String> roles = new ArrayList<String>();
		
		OQLQuery oql = db.getOQLQuery("CALL SQL select max(accessRightRoleId), roleName, max(accessRightId) from cmAccessRightRole arr group by roleName AS org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl");
		 
    	QueryResults results = oql.execute(Database.READONLY);
		while(results.hasMore()) 
        {
			AccessRightRoleImpl arr = (AccessRightRoleImpl)results.next();
			roles.add(arr.getRoleName());
		}

		results.close();
		oql.close();

		return roles;
	}	

	/**
	 * This method get all unique names used in cmAccessRightGroup
	 * @param db
	 * @return
	 * @throws Exception
	 */
	public List<String> getUniqueGroupNameListInAccessRightGroup(Database db) throws Exception
	{
		List<String> groups = new ArrayList<String>();

		OQLQuery oql = db.getOQLQuery("CALL SQL select max(accessRightGroupId), groupName, max(accessRightId) from cmAccessRightGroup arr group by groupName AS org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl");
		 
    	QueryResults results = oql.execute(Database.READONLY);
		while(results.hasMore()) 
        {
			AccessRightGroupImpl arr = (AccessRightGroupImpl)results.next();
			groups.add(arr.getGroupName());
		}

		results.close();
		oql.close();
		
		return groups;
	}	
	
	public String getAccessRightsStatusText() throws SystemException, Bug
	{
		String accessRightsStatusText = "";
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			accessRightsStatusText = getAccessRightsStatusText(db);
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.error("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightsStatusText;		
	}

	public String getAccessRightsStatusText(Database db) throws SystemException, Bug, Exception
	{
		String accessRightsStatusText = "";
		
		String sqlSNV = "CALL SQL select count(*) from cmAccessRight ar where ar.interceptionPointId in (select interceptionPointId from cmInterceptionPoint where name like 'SiteNodeVersion.%') AND ar.parameters NOT IN (select siteNodeVersionId from cmSiteNodeVersion where siteNodeVersionId = ar.parameters) AS org.infoglue.cms.entities.management.TableCount";
		if(CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
			sqlSNV = "CALL SQL select count(*) from cmAccessRight ar where ar.interceptionPointId in (select interceptionPointId from cmInterceptionPoint where name like 'SiteNodeVersion.%') AND ar.parameters NOT IN (select siNoVerId from cmSiNoVer where siNoVerId = ar.parameters) AS org.infoglue.cms.entities.management.TableCount";
			
		OQLQuery oql = db.getOQLQuery(sqlSNV);
		
		QueryResults results = oql.execute(Database.READONLY);
		while (results.hasMore()) 
		{
			TableCount tableCount = (TableCount)results.next();
			accessRightsStatusText += "SiteNode versions wrongly connected and removable: " + tableCount.getCount() + "\n";  
		}
		/*
		String sql = "CALL SQL select count(*) from cmAccessRight ar where ar.interceptionPointId in (select interceptionPointId from cmInterceptionPoint where name like 'SiteNodeVersion.%') AND ar.parameters NOT IN (select siteNodeVersionId from cmSiteNodeVersion where siteNodeVersionId = ar.parameters) AS org.infoglue.cms.entities.management.TableCount";
		if(CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
			sql = "CALL SQL select count(*) from cmAccessRight ar where ar.interceptionPointId in (select interceptionPointId from cmInterceptionPoint where name like 'SiteNodeVersion.%') AND ar.parameters NOT IN (select siNoVerId from cmSiNoVer where siNoVerId = ar.parameters) AS org.infoglue.cms.entities.management.TableCount";
			
		OQLQuery oql = db.getOQLQuery(sql);
		
		QueryResults results = oql.execute(Database.READONLY);
		while (results.hasMore()) 
		{
			TableCount tableCount = (TableCount)results.next();
			accessRightsStatusText += "SiteNode versions wrongly connected and removable: " + tableCount.getCount() + "\n";  
		}
		*/

		results.close();
		oql.close();
		
		return accessRightsStatusText;		
	}

	public String fixAccessRightInconsistencies() throws SystemException, Bug
	{
		String accessRightsStatusText = "";
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			accessRightsStatusText = fixAccessRightInconsistencies(db);
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.error("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightsStatusText;		
	}

	public String fixAccessRightInconsistencies(Database db) throws SystemException, Bug, Exception
	{
		String accessRightsStatusText = "";
		
		String sqlSNV = "CALL SQL select ar.accessRightId, ar.parameters, ar.interceptionPointId from cmAccessRight ar where ar.interceptionPointId in (select interceptionPointId from cmInterceptionPoint where name like 'SiteNodeVersion.%') AND ar.parameters NOT IN (select siteNodeVersionId from cmSiteNodeVersion where siteNodeVersionId = ar.parameters) AS org.infoglue.cms.entities.management.impl.simple.SmallAccessRightImpl";
		if(CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
			sqlSNV = "CALL SQL select ar.accessRightId, ar.parameters, ar.interceptionPointId from cmAccessRight ar where ar.interceptionPointId in (select interceptionPointId from cmInterceptionPoint where name like 'SiteNodeVersion.%') AND ar.parameters NOT IN (select siNoVerId from cmSiNoVer where siNoVerId = ar.parameters) AS org.infoglue.cms.entities.management.impl.simple.SmallAccessRightImpl";
			
		OQLQuery oql = db.getOQLQuery(sqlSNV);
		
		QueryResults results = oql.execute(Database.READONLY);
		int itemsRemoved = 0;
		while (results.hasMore() && itemsRemoved < 1000) 
		{
			AccessRight accessRight = (AccessRight)results.next();
			//logger.info("Checking if siteNodeVersion really is missing");
			try
			{
				SiteNodeVersionVO snvVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(new Integer(accessRight.getParameters()), db);
				logger.info("OBS::::::::::: snvVO was not missing");
			}
			catch (Exception e) 
			{
				//logger.info("snvVO was missing - ok to remove:" + accessRight.getId());
				delete(accessRight.getValueObject().getInterceptionPointId(), accessRight.getParameters(), true, db);
				itemsRemoved++;
			}
			logger.info("itemsRemoved:" + itemsRemoved);
		}
		accessRightsStatusText += "" + itemsRemoved + " AccessRights were removed as the siteNodeVersion it pointed to was removed\n";  

		/*
		String sql = "CALL SQL select count(*) from cmAccessRight ar where ar.interceptionPointId in (select interceptionPointId from cmInterceptionPoint where name like 'SiteNodeVersion.%') AND ar.parameters NOT IN (select siteNodeVersionId from cmSiteNodeVersion where siteNodeVersionId = ar.parameters) AS org.infoglue.cms.entities.management.TableCount";
		if(CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
			sql = "CALL SQL select count(*) from cmAccessRight ar where ar.interceptionPointId in (select interceptionPointId from cmInterceptionPoint where name like 'SiteNodeVersion.%') AND ar.parameters NOT IN (select siNoVerId from cmSiNoVer where siNoVerId = ar.parameters) AS org.infoglue.cms.entities.management.TableCount";
			
		OQLQuery oql = db.getOQLQuery(sql);
		
		QueryResults results = oql.execute(Database.READONLY);
		while (results.hasMore()) 
		{
			TableCount tableCount = (TableCount)results.next();
			accessRightsStatusText += "SiteNode versions wrongly connected and removable: " + tableCount.getCount() + "\n";  
		}
		*/

		results.close();
		oql.close();
		
		return accessRightsStatusText;		
	}
	
	public String fixEmptyAccessRightInconsistencies() throws SystemException, Bug
	{
		String accessRightsStatusText = "";
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			accessRightsStatusText = fixEmptyAccessRightInconsistencies(db);
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.error("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightsStatusText;		
	}

	public String fixEmptyAccessRightInconsistencies(Database db) throws SystemException, Bug, Exception
	{
		String accessRightsStatusText = "";
		
		List<AccessRightVO> duplicateAccessRightVOList = new ArrayList<AccessRightVO>();
		List<AccessRightVO> duplicateAutoDeletableAccessRightVOList = new ArrayList<AccessRightVO>();
		List<AccessRightVO> duplicateAutoMergableAccessRightVOList = new ArrayList<AccessRightVO>();
		
		getAllDuplicates(true, true, duplicateAccessRightVOList, duplicateAutoDeletableAccessRightVOList, duplicateAutoMergableAccessRightVOList, db);
		for(AccessRightVO arVO : duplicateAutoDeletableAccessRightVOList)
		{
			delete(arVO.getInterceptionPointId(), arVO.getParameters(), true, db);
		}
		
		return accessRightsStatusText;		
	}

	public String fixAutoMergableAccessRightInconsistencies() throws SystemException, Bug
	{
		String accessRightsStatusText = "";
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			accessRightsStatusText = fixAutoMergableAccessRightInconsistencies(db);
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.error("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightsStatusText;		
	}

	public String fixAutoMergableAccessRightInconsistencies(Database db) throws SystemException, Bug, Exception
	{
		String accessRightsStatusText = "";
		
		List<AccessRightVO> duplicateAccessRightVOList = new ArrayList<AccessRightVO>();
		List<AccessRightVO> duplicateAutoDeletableAccessRightVOList = new ArrayList<AccessRightVO>();
		List<AccessRightVO> duplicateAutoMergableAccessRightVOList = new ArrayList<AccessRightVO>();
		
		getAllDuplicates(true, true, duplicateAccessRightVOList, duplicateAutoDeletableAccessRightVOList, duplicateAutoMergableAccessRightVOList, db);
		Iterator duplicateAutoMergableAccessRightVOListIterator = duplicateAutoMergableAccessRightVOList.iterator();
		while(duplicateAutoMergableAccessRightVOListIterator.hasNext())
		{
			AccessRightVO arVO = (AccessRightVO)duplicateAutoMergableAccessRightVOListIterator.next();
			
			List<AccessRight> entityAccessRights = getAccessRightListForEntity(arVO.getInterceptionPointId(), arVO.getParameters(), db);
			List<AccessRightUser> users = new ArrayList<AccessRightUser>();
			
			Iterator entityAccessRightsIterator = entityAccessRights.iterator();
			while(entityAccessRightsIterator.hasNext())
			{
				AccessRight ar = (AccessRight)entityAccessRightsIterator.next();
				if(ar.getUsers().size() > 0 && ar.getRoles().size() == 0 && ar.getGroups().size() == 0)
				{
					users.addAll(ar.getUsers());
					db.remove(ar);
					entityAccessRightsIterator.remove();
					duplicateAutoMergableAccessRightVOListIterator.remove();
					logger.info("Deleted the item with only users...:" + ar.getId());
				}
			}

			entityAccessRightsIterator = entityAccessRights.iterator();
			while(entityAccessRightsIterator.hasNext())
			{
				AccessRight ar = (AccessRight)entityAccessRightsIterator.next();
				if(users.size() > 0)
				{
					logger.info("Adding users " + users.size() + " to " + ar.getId());
					ar.getUsers().addAll(users);
					for(AccessRightUser user : users)
					{
						user.setAccessRight(ar);
					}
				}
			}
		}
		
		return accessRightsStatusText;		
	}

	public String mergeAccessRight(Integer interceptionPointId, String parameters, String[] roleNames, String[] groupNames, String[] userNames) throws SystemException, Bug
	{
		String accessRightsStatusText = "";
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			accessRightsStatusText = mergeAccessRight(interceptionPointId, parameters, roleNames, groupNames, userNames, db);
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.error("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
		
		return accessRightsStatusText;		
	}

	public String mergeAccessRight(Integer interceptionPointId, String parameters, String[] roleNames, String[] groupNames, String[] userNames, Database db) throws SystemException, Bug, Exception
	{
		String accessRightsStatusText = "";
		
		List<AccessRight> entityAccessRights = getAccessRightListForEntity(interceptionPointId, parameters, db);
		
		AccessRight keeperAR = null;
		Iterator entityAccessRightsIterator = entityAccessRights.iterator();
		while(entityAccessRightsIterator.hasNext())
		{
			AccessRight ar = (AccessRight)entityAccessRightsIterator.next();
			if(entityAccessRightsIterator.hasNext())
			{
				//db.remove(ar);
				delete(ar, db);
				entityAccessRightsIterator.remove();
				logger.info("Deleted the duplicates:" + ar.getId());
			}
			else
				keeperAR = ar;
		}
		
		if(keeperAR != null)
		{
			AccessRightVO accessRightVO = new AccessRightVO();
			accessRightVO.setParameters(parameters);
	
		    //InterceptionPoint interceptionPoint = InterceptionPointController.getController().getInterceptionPointWithId(interceptionPointId, db);
			//AccessRight accessRight = create(accessRightVO, interceptionPoint, db);
			
			for(String roleName : roleNames)
			{
				boolean add = true;
				for(AccessRightRole arr : (Collection<AccessRightRole>)keeperAR.getRoles())
				{
					if(arr.getRoleName().equals(roleName)) add = false;
				}
				
				if(add)
				{
					logger.info("roleName:" + roleName);
				    AccessRightRoleVO accessRightRoleVO = new AccessRightRoleVO();
				    accessRightRoleVO.setRoleName(roleName);
				    AccessRightRole accessRightRole = createAccessRightRole(db, accessRightRoleVO, keeperAR);
				    keeperAR.getRoles().add(accessRightRole);
				}
			}
				
			for(String groupName : groupNames)
			{
				boolean add = true;
				for(AccessRightGroup arg : (Collection<AccessRightGroup>)keeperAR.getGroups())
				{
					if(arg.getGroupName().equals(groupName)) add = false;
				}
				
				if(add)
				{
					logger.info("groupName:" + groupName);
				    AccessRightGroupVO accessRightGroupVO = new AccessRightGroupVO();
				    accessRightGroupVO.setGroupName(groupName);
				    AccessRightGroup accessRightGroup = createAccessRightGroup(db, accessRightGroupVO, keeperAR);
				    keeperAR.getGroups().add(accessRightGroup);
				}
			}
			
			for(String userName : userNames)
			{
				boolean add = true;
				for(AccessRightUser aru : (Collection<AccessRightUser>)keeperAR.getUsers())
				{
					if(aru.getUserName().equals(userName)) add = false;
				}
				
				if(add)
				{
					logger.info("userName:" + userName);
				    AccessRightUserVO accessRightUserVO = new AccessRightUserVO();
				    accessRightUserVO.setUserName(userName);
				    AccessRightUser accessRightUser = createAccessRightUser(db, accessRightUserVO, keeperAR);
				    keeperAR.getUsers().add(accessRightUser);
				}
			}
		}
		
		return accessRightsStatusText;		
	}

	public void getAllDuplicates(boolean populateRelated, boolean includeAllDuplicates, List<AccessRightVO> duplicates, List<AccessRightVO> duplicatesEasyToDelete, List<AccessRightVO> duplicateAutoMergableAccessRightVOList) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			getAllDuplicates(populateRelated, includeAllDuplicates, duplicates, duplicatesEasyToDelete, duplicateAutoMergableAccessRightVOList, db);
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.error("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
	}

	public void getAllDuplicates(boolean populateRelated, boolean includeAllDuplicates, List<AccessRightVO> duplicates, List<AccessRightVO> duplicatesEasyToDelete, List<AccessRightVO> duplicatesEasyToMerge, Database db) throws SystemException, Bug, Exception
	{
		String SQL = "CALL SQL select max(ar.accessRightId) as accessRightId, ar.parameters, ar.interceptionPointId from cmAccessRight ar where ar.interceptionPointId in (select interceptionPointId from cmInterceptionPoint where name like '%.Read' AND name not like 'SiteNodeVersion.Read') GROUP BY ar.parameters, ar.interceptionPointId HAVING count(*) > 1 ORDER BY ar.interceptionPointId, ar.parameters AS org.infoglue.cms.entities.management.impl.simple.SmallAccessRightImpl";
		if(includeAllDuplicates)
			SQL = "CALL SQL select ar.accessRightId, ar.parameters, ar.interceptionPointId from cmAccessRight ar INNER JOIN (select ar2.parameters, ar2.interceptionPointId from cmAccessRight ar2 GROUP BY ar2.parameters, ar2.interceptionPointId HAVING count(*) > 1 ORDER BY ar2.interceptionPointId, ar2.parameters, ar2.accessRightId) derived_ar ON derived_ar.parameters = ar.parameters AND derived_ar.interceptionPointId = ar.interceptionPointId AND derived_ar.interceptionPointId in (select interceptionPointId from cmInterceptionPoint where name like '%.Read' AND name not like 'SiteNodeVersion.Read') order by parameters AS org.infoglue.cms.entities.management.impl.simple.SmallAccessRightImpl";
			
		OQLQuery oql = db.getOQLQuery(SQL);
		
		int deletable=0;
		QueryResults results = oql.execute(Database.READONLY);
		while (results.hasMore()) 
		{
			AccessRight accessRight = (AccessRight)results.next();
			AccessRightVO accessRightVO = accessRight.getValueObject();
			
			if(populateRelated)
			{
				AccessRight fullAccessRight = getAccessRightWithId(accessRightVO.getId(), db);
				
				populateDescription(db, accessRightVO, fullAccessRight);
				
				List<AccessRightRoleVO> roles = toVOList(fullAccessRight.getRoles());
				List<AccessRightGroupVO> groups = toVOList(fullAccessRight.getGroups());
				List<AccessRightUserVO> users = toVOList(fullAccessRight.getUsers());
				accessRightVO.getRoles().addAll(roles);
				accessRightVO.getGroups().addAll(groups);
				accessRightVO.getUsers().addAll(users);
				if(roles.size() == 0 && groups.size() == 0 && users.size() == 0)
				{
					duplicates.add(accessRightVO);					
					duplicatesEasyToDelete.add(accessRightVO);
				}
				else if(roles.size() == 0 && groups.size() == 0 && users.size() > 0)
				{
					duplicates.add(accessRightVO);					
					duplicatesEasyToMerge.add(accessRightVO);
				}
				else
				{
					duplicates.add(accessRightVO);					
				}
			}
			else
				duplicates.add(accessRightVO);
		}
		
		results.close();
		oql.close();
	}

	public List<AccessRightVO> getUndefinedAccessRights(Database db) throws SystemException, Bug, Exception
	{
		List<AccessRightVO> undefinedAccessRights = new ArrayList<AccessRightVO>();
		  
		StringBuilder sb = new StringBuilder();
		sb.append("CALL SQL select ar.accessRightId, ar.parameters, ar.interceptionPointId from cmAccessRight ar where ");
		sb.append("ar.interceptionPointId in (select interceptionPointId from cmInterceptionPoint where name like 'ComponentEditor.%' OR name LIKE 'Component.%' OR name = 'ComponentPropertyEditor.EditProperty' OR name like '%.Read%' AND name NOT LIKE 'SiteNodeVersion.Read') AND ");
		sb.append("ar.accessRightId NOT IN (select accessRightId from cmAccessRightUser where ar.accessRightId = accessRightId) AND ");
		sb.append("ar.accessRightId NOT IN (select accessRightId from cmAccessRightRole where ar.accessRightId = accessRightId) AND ");
		sb.append("ar.accessRightId NOT IN (select accessRightId from cmAccessRightGroup where ar.accessRightId = accessRightId)  AS org.infoglue.cms.entities.management.impl.simple.SmallAccessRightImpl");
					
		//System.out.println(sb.toString());
		OQLQuery oql = db.getOQLQuery(sb.toString());
		
		QueryResults results = oql.execute(Database.READONLY);
		while (results.hasMore()) 
		{
			AccessRight accessRight = (AccessRight)results.next();
			AccessRightVO accessRightVO = accessRight.getValueObject();
			undefinedAccessRights.add(accessRightVO);
		}
		
		results.close();
		oql.close();
		
		return undefinedAccessRights;
	}
	
	public void populateDescription(Database db, AccessRightVO accessRightVO, AccessRight fullAccessRight) 
	{
		try
		{
			String entityName = accessRightVO.getParameters();
			if(fullAccessRight.getInterceptionPointName().equalsIgnoreCase("Repository.Read"))
				entityName = RepositoryController.getController().getRepositoryVOWithId(new Integer(accessRightVO.getParameters()), db).getName();
			if(fullAccessRight.getInterceptionPointName().startsWith("Content."))
				entityName = ContentController.getContentController().getContentPath(new Integer(accessRightVO.getParameters()));
			if(fullAccessRight.getInterceptionPointName().equalsIgnoreCase("SiteNodeVersion."))
			{
				SiteNodeVersionVO snvVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(new Integer(accessRightVO.getParameters()), db);
				entityName = SiteNodeController.getController().getSiteNodePath(snvVO.getId(), db);
			}
			
			accessRightVO.setName(fullAccessRight.getInterceptionPointName() + " on " + entityName);
		}
		catch (Exception e) 
		{
			logger.warn("Could not populate access right desc: " + e.getMessage(), e);
			accessRightVO.setName("Unknown entity: Could be missing" + e.getMessage());
		}
	}

	public void fixAccessRightDuplicate(Integer[] accessRightIds) throws SystemException, Bug
	{
		List accessRightList = new ArrayList();
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);

			//sssss

			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.error("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
		}
	}

	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new AccessRightVO();
	}

}
 
