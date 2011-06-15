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

import javax.servlet.http.HttpServletRequest;

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
import org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightImpl;
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
import org.infoglue.cms.util.DateHelper;
import org.infoglue.deliver.util.CacheController;
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
		
		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName(interceptionPointName);
		if(interceptionPointVO == null)
		{
			logger.info("interceptionPointName:" + interceptionPointName + " not found");
			return new ArrayList();
		}
		
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

	public List getAccessRightGroupVOList(Integer accessRightId) throws SystemException, Bug
	{
		List accessRightGroupVOList = new ArrayList();
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			AccessRight accessRight = this.getAccessRightWithId(accessRightId, db);
			if(accessRight != null)
			    accessRightGroupVOList = toVOList(accessRight.getGroups());
			
	        logger.info("accessRightGroupVOList:" + accessRightGroupVOList.size());
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
		    e.printStackTrace();
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
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
		    e.printStackTrace();
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
				oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND (is_undefined(f.parameters) OR f.parameters = $2) AND f.roles.roleName = $3");
				oql.bind(interceptionPointId);
				oql.bind(parameters);
				oql.bind(roleName);
			}
			else
			{
		    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND f.parameters = $2 AND f.roles.roleName = $3");
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
				oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND (is_undefined(f.parameters) OR f.parameters = $2)");
				oql.bind(interceptionPointId);
				oql.bind(parameters);
			}
			else
			{
		    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND f.parameters = $2");
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
			
	    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1");
			oql.bind(interceptionPointId);
			
			QueryResults results = oql.execute(Database.ReadOnly);
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

	
	public List getAccessRightListOnlyReadOnly(Integer interceptionPointId, String parameters, Database db) throws SystemException, Bug
	{
		List accessRightList = new ArrayList();
		
		try
		{
			OQLQuery oql = null;
			
			if(parameters == null || parameters.length() == 0)
			{
				oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1");
				oql.bind(interceptionPointId);
			}
			else
			{
		    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND f.parameters = $2");
				oql.bind(interceptionPointId);
				oql.bind(parameters);
			}
			
			QueryResults results = oql.execute(Database.ReadOnly);
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

	public List getAccessRightListForEntity(Integer interceptionPointId, String parameters, Database db)  throws SystemException, Bug
	{
		List accessRightList = new ArrayList();
		
		try
		{
		    //logger.info("getAccessRightListForEntity(Integer interceptionPointId, String parameters, Database db)");
		    //logger.info("interceptionPointId:" + interceptionPointId);
		    //logger.info("parameters:" + parameters);
			
			OQLQuery oql = null;
			
			if(parameters == null || parameters.length() == 0)
			{
				oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND (is_undefined(f.parameters) OR f.parameters = $2)");
				oql.bind(interceptionPointId);
				oql.bind(parameters);
			}
			else
			{
		    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND f.parameters = $2");
				oql.bind(interceptionPointId);
				oql.bind(parameters);
			}
						
			QueryResults results = oql.execute();
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
			
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1");
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
			
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.roles.roleName = $1");
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

			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND f.roles.roleName = $2");
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
				delete(new Integer(interceptionPointIdString), parameters, false, db);

				AccessRightVO accessRightVO = new AccessRightVO();
				accessRightVO.setParameters(parameters);

				AccessRight accessRight = null;
				
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
					if(!siteNodeVersion.getIsProtected().equals(SiteNodeVersionVO.YES))
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
						if(siteNodeVersion.getIsProtected().equals(SiteNodeVersionVO.YES))
						{
							if(logger.isDebugEnabled())
								logger.debug("YEPPPPPPP");
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
	
	public void addUser(String interceptionPointCategory, String parameters, String userName, HttpServletRequest request) throws ConstraintException, SystemException
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

		        db.remove(accessRightUser.getAccessRight());

		        accessRightsUsersIterator.remove();
		        db.remove(accessRightUser);
		    }
		    
			int interceptionPointIndex = 0;
			String interceptionPointIdString = request.getParameter(interceptionPointIndex + "_InterceptionPointId");
			while(interceptionPointIdString != null)
			{
			    String hasAccess = request.getParameter(interceptionPointIdString + "_hasAccess");
				
			    AccessRight accessRight = null;
			     
				if(hasAccess != null)
				{
					List accessRights = getAccessRightListForEntity(new Integer(interceptionPointIdString), parameters, db);
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
				
				interceptionPointIndex++;
				interceptionPointIdString = request.getParameter(interceptionPointIndex + "_InterceptionPointId");
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
				if(accessRightsRoles == null || accessRightsRoles.size() == 0 && accessRightsGroups == null || accessRightsGroups.size() == 0)
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

	public void delete(Integer interceptionPointId, String parameters, boolean deleteUsers, Database db) throws SystemException, Exception
	{
		List accessRightList = getAccessRightListOnly(interceptionPointId, parameters, db);
		Iterator i = accessRightList.iterator();
		while(i.hasNext())
		{
			AccessRight accessRight = (AccessRight)i.next();
			
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

			if(deleteUsers)
			{
				Iterator usersIterator = accessRight.getUsers().iterator();
				while(usersIterator.hasNext())
				{
				    AccessRightUser accessRightUser = (AccessRightUser)usersIterator.next();
				    usersIterator.remove();
				    db.remove(accessRightUser);
				}
	
				db.remove(accessRight);
			}
			else
			{
			    if(accessRight.getUsers() == null || accessRight.getUsers().size() == 0)
			        db.remove(accessRight);
			}
		}
		
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
	    if(infoGluePrincipal == null)
	        return false;
	        
		if(infoGluePrincipal != null && infoGluePrincipal.getIsAdministrator())
			return true;
			
		boolean isPrincipalAuthorized = false;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			isPrincipalAuthorized = getIsPrincipalAuthorized(db, infoGluePrincipal, interceptionPointName, parameters, returnTrueIfNoAccessRightsDefined);
		
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
		return getIsPrincipalAuthorized(db, infoGluePrincipal, interceptionPointName, extraParameters, true);
	}
	
	/**
	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
	 */
	public boolean getIsPrincipalAuthorized(Database db, InfoGluePrincipal infoGluePrincipal, String interceptionPointName, String extraParameters, boolean returnTrueIfNoAccessRightsDefined) throws SystemException
	{		
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
				
		List accessRightList = this.getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), extraParameters, db);
		if(logger.isInfoEnabled())
			logger.info("accessRightList:" + accessRightList.size());
		//If no access rights are set for the content version we should assume it was not protected on version level.
		if((interceptionPointName.equalsIgnoreCase("ContentVersion.Read") || 
		   interceptionPointName.equalsIgnoreCase("ContentVersion.Write") || 
		   interceptionPointName.equalsIgnoreCase("ContentVersion.Delete") || 
		   interceptionPointName.equalsIgnoreCase("ContentVersion.Publish") || returnTrueIfNoAccessRightsDefined) && 
		   (accessRightList == null || accessRightList.size() == 0))
		{
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
					if(logger.isInfoEnabled())
					    logger.info("role:" + role.getName());
					
					Iterator approvedRolesIterator = approvedRoles.iterator();
					while(approvedRolesIterator.hasNext())
					{
					    AccessRightRole accessRightRole = (AccessRightRole)approvedRolesIterator.next();
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
	 
				Iterator approvedGroupsIterator = approvedGroups.iterator();
				outer:while(approvedGroupsIterator.hasNext())
				{
				    AccessRightGroup accessRightGroup = (AccessRightGroup)approvedGroupsIterator.next();
				    if(logger.isInfoEnabled())
					    logger.info("accessRightGroup:" + accessRightGroup.getGroupName());
	
				    limitOnGroups = true;
	
				    Iterator groupsIterator = groups.iterator();
					while(groupsIterator.hasNext())
					{
					    InfoGlueGroup group = (InfoGlueGroup)groupsIterator.next();
					    if(logger.isInfoEnabled())
					    	logger.info("" + group.getName() + " = " + accessRightGroup.getGroupName());
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

	public boolean getIsPrincipalAuthorized(Database db, InfoGluePrincipal infoGluePrincipal, String interceptionPointName) throws SystemException
	{		
		return getIsPrincipalAuthorized(db, infoGluePrincipal, interceptionPointName, false, false, true);
	}
	
	/**
	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
	 */

	public boolean getIsPrincipalAuthorized(Database db, InfoGluePrincipal infoGluePrincipal, String interceptionPointName, boolean returnSuccessIfInterceptionPointNotDefined, boolean returnFailureIfInterceptionPointNotDefined, boolean returnTrueIfNoAccessRightsDefined) throws SystemException
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
			
		Timer t = new Timer();
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
					    if(accessRightGroup.getGroupName().equals(group.getName()))
					    {
					        principalHasGroup = true;
					        break outer;
					    }
					}
				}
			}
		}

		if(logger.isInfoEnabled())
			t.printElapsedTime("Getting hasAccess took");
		
	    if((principalHasRole && principalHasGroup) || (principalHasRole && !limitOnGroups))
		    isPrincipalAuthorized = true;
		
		if(logger.isInfoEnabled() && !isPrincipalAuthorized)
			logger.info("Principal " + infoGluePrincipal.getName() + " was not allowed to " + interceptionPointName);
		
		if(logger.isInfoEnabled())
			logger.info("isPrincipalAuthorized:" + isPrincipalAuthorized);
	    
		return isPrincipalAuthorized;
	}

	
	
	public Collection getAccessRightsUserRows(String interceptionPointCategory, String parameters) throws SystemException, Bug
	{
		Collection principalVOList = null;
		
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

	public Collection getAccessRightsUserRows(String interceptionPointCategory, String parameters, Database db) throws SystemException, Bug
	{
	    Map accessRightsUserRows = new HashMap();
		
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
				results = oql.execute(Database.ReadOnly);
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
				results = oql.execute(Database.ReadOnly);
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
				results = oql.execute(Database.ReadOnly);
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
				results = oql.execute(Database.ReadOnly);
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
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new AccessRightVO();
	}

}
 