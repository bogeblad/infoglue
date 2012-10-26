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

package org.infoglue.cms.applications.contenttool.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.AccessRight;
import org.infoglue.cms.entities.management.AccessRightGroup;
import org.infoglue.cms.entities.management.AccessRightGroupVO;
import org.infoglue.cms.entities.management.AccessRightRole;
import org.infoglue.cms.entities.management.AccessRightUser;
import org.infoglue.cms.entities.management.AccessRightVO;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;
import org.infoglue.deliver.util.Timer;


/** 
 * This class shows which roles has access to the siteNode.
 */

public class ViewAccessRightsAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewAccessRightsAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private Integer interceptionPointId = null;
	private String interceptionPointName = null;
	private String interceptionPointCategory = null;
	private String extraParameters = "";
	private String[] extraMultiParameters;
	private String returnAddress;
	private Boolean showInline = false;
	private String colorScheme;
	private String saved = "false";
	private Boolean closeOnLoad = false;

	private List interceptionPointVOList = new ArrayList();
	private List roleList = null;
	private List groupList = null;
	private Collection accessRightsUserRows = null;
	private Map<Integer,List<AccessRightGroupVO>> accessRightGroupsMap = new HashMap<Integer,List<AccessRightGroupVO>>();
	private Map<String,Object> accessRightHasAccessMap = new HashMap<String,Object>();
	private String extraAccessRightInfo = "";

	public String doV3() throws Exception
    {
    	doExecute();
    	return "successV3";
    }
    
    public String doExecute() throws Exception
    {
    	Timer t = new Timer();
    	
    	AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
		if(interceptionPointCategory.equalsIgnoreCase("Content"))
		{	
			if(extraParameters == null || extraParameters.equals(""))
			    throw new SystemException("The content category must have a content id sent in so don't set 'Use extra data for access control' to no for those interception points.");
			    
		    Integer contentId = new Integer(extraParameters);
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);
			
			if(!contentVO.getCreatorName().equalsIgnoreCase(this.getInfoGluePrincipal().getName()))
			{
				if(ContentControllerProxy.getController().getIsContentProtected(contentId) && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.ChangeAccessRights", contentId.toString()))
				{
					InterceptionPointVO changeInterceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Content.ChangeAccessRights");
					InterceptionPointVO readInterceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Content.Read");
					List changeAccessRightVOList = AccessRightController.getController().getAccessRightVOListOnly(changeInterceptionPointVO.getId(), "" + contentId);
					List readAccessRightVOList = AccessRightController.getController().getAccessRightVOListOnly(readInterceptionPointVO.getId(), "" + contentId);
					logger.info("changeAccessRightVOList:" + changeAccessRightVOList.size());
					logger.info("readAccessRightVOList:" + readAccessRightVOList.size());
					if(changeAccessRightVOList.size() > 0 && readAccessRightVOList.size() > 0)
						ceb.add(new AccessConstraintException("Content.contentId", "1006"));
				}
			}
		}
		else if(interceptionPointCategory.equalsIgnoreCase("SiteNodeVersion"))
		{	
			if(extraParameters == null || extraParameters.equals(""))
			    throw new SystemException("The sitenode category must have a sitenode id sent in so don't set 'Use extra data for access control' to no for those interception points.");

			Integer siteNodeVersionId = new Integer(extraParameters);
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);
			if(!siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(this.getInfoGluePrincipal().getName()))
			{
				boolean isSiteNodeVersionProtected = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getIsSiteNodeVersionProtected(siteNodeVersionVO.getId());
				Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId);
				if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "SiteNodeVersion.ChangeAccessRights", siteNodeVersionId.toString()))
				{
					InterceptionPointVO changeInterceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("SiteNodeVersion.ChangeAccessRights");
					InterceptionPointVO readInterceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("SiteNodeVersion.Read");
					List changeAccessRightVOList = AccessRightController.getController().getAccessRightVOListOnly(changeInterceptionPointVO.getId(), "" + siteNodeVersionVO.getId());
					List readAccessRightVOList = AccessRightController.getController().getAccessRightVOListOnly(readInterceptionPointVO.getId(), "" + siteNodeVersionVO.getId());
					logger.info("changeAccessRightVOList:" + changeAccessRightVOList.size());
					logger.info("readAccessRightVOList:" + readAccessRightVOList.size());
					if(changeAccessRightVOList.size() > 0 && readAccessRightVOList.size() > 0)
						ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeId", "1006"));
				}
			}
		}
				
		this.interceptionPointVOList = InterceptionPointController.getController().getInterceptionPointVOList(interceptionPointCategory);
		this.roleList = RoleControllerProxy.getController().getAllRoles();
		this.groupList = GroupControllerProxy.getController().getAllGroups();
		
		this.accessRightsUserRows = AccessRightController.getController().getAccessRightsUserRows(interceptionPointCategory, extraParameters);
		
		Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);

        try
        {
    		for(InterceptionPointVO interceptionPointVO : (List<InterceptionPointVO>)this.interceptionPointVOList)
    		{
    			this.extraAccessRightInfo += getExtraAccessRightText(interceptionPointVO, getExtraParameters(), db);

    			Integer accessRightId = getAccessRightId(interceptionPointVO.getId(), getExtraParameters(), db);
    			//Integer[] accessRightIds = getAccessRightIds(interceptionPointVO.getId(), getExtraParameters(), db);
    			
    			accessRightHasAccessMap.put("" + interceptionPointVO.getId() + "_" + getExtraParameters(), accessRightId);
    			
    			for(InfoGlueRole role : (List<InfoGlueRole>)this.roleList)
    			{
        			Boolean hasAccess = getHasAccessRight(interceptionPointVO.getId(), getExtraParameters(), role.getName(), db);
        			accessRightHasAccessMap.put("" + interceptionPointVO.getId() + "_" + getExtraParameters() + "_" + role.getName(), hasAccess);
    			}
    			
    			/*
    			for(Integer currentAccessRightId : accessRightIds)
    			{
    				List<AccessRightGroupVO> currentAccessRightGroupVOList = AccessRightController.getController().getAccessRightGroupVOList(currentAccessRightId, db);
    				if(currentAccessRightGroupVOList.size() > 0)
    				{
    					accessRightId = currentAccessRightId;
    				}
    			}
    			*/
    			
    			if(accessRightId != null && accessRightId > -1)
    			{
    				List<AccessRightGroupVO> accessRightGroupVOList = AccessRightController.getController().getAccessRightGroupVOList(accessRightId, db);
    				logger.info("accessRightGroupVOList:" + accessRightGroupVOList.size() + " to " + accessRightId);
    				accessRightGroupsMap.put(accessRightId, accessRightGroupVOList);
    				accessRightGroupsMap.put(interceptionPointVO.getId(), accessRightGroupVOList);
    			}
    		}		
            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        //t.printElapsedTime("Access 10");
		
		ceb.throwIfNotEmpty();

    	return "success";
    }

	private String getExtraAccessRightText(InterceptionPointVO ipVO, String parameters) throws Exception
	{
		String sb = "";

		Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);

        try
        {
        	sb = getExtraAccessRightText(ipVO, parameters, db);
            
        	commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
        return sb;
	}

        	
	private String getExtraAccessRightText(InterceptionPointVO ipVO, String parameters, Database db) throws Exception
	{
		StringBuilder sb = new StringBuilder();

    	sb.append("Access rights for " + ipVO.getName() + "(" + parameters + ")\n");

       	List<AccessRight> accessRights = AccessRightController.getController().getAccessRightListOnlyReadOnly(ipVO.getId(), extraParameters, db);
        for(AccessRight accessRight : accessRights)	
        {
        	sb.append("    Access right " + accessRight.getId() + "\n");
        	sb.append("    Roles\n");
        	for(AccessRightRole arr : (Collection<AccessRightRole>)accessRight.getRoles())
        	{
            	sb.append("        Access right role " + arr.getRoleName() + "(" + arr.getId() + ")\n");            		
        	}
        	sb.append("    Groups\n");
        	for(AccessRightGroup arg : (Collection<AccessRightGroup>)accessRight.getGroups())
        	{
            	sb.append("        Access right group " + arg.getGroupName() + "(" + arg.getId() + ")\n");            		
        	}
        	sb.append("    Users\n");
        	for(AccessRightUser aru : (Collection<AccessRightUser>)accessRight.getUsers())
        	{
            	sb.append("        Access right user " + aru.getUserName() + "(" + aru.getId() + ")\n");            		
        	}
        	sb.append("\n");
        }
        
        return sb.toString();
	}

	public boolean getHasAccessRight(Integer interceptionPointId, String extraParameters, String roleName) throws SystemException, Bug
	{
		Timer t = new Timer();
	    try
	    {
			List accessRights = AccessRightController.getController().getAccessRightVOList(interceptionPointId, extraParameters, roleName);
			boolean hasAccessRight = (accessRights.size() > 0) ? true : false;
			t.printElapsedTime("getHasAccessRight");
			return hasAccessRight;
	    }
	    catch(Exception e)
	    {
	        logger.warn(e);
	        throw new SystemException(e);
	    }
	}

	public boolean getHasAccessRight(Integer interceptionPointId, String extraParameters, String roleName, Database db) throws SystemException, Bug
	{
		//Timer t = new Timer();
	    try
	    {
			List accessRights = AccessRightController.getController().getAccessRightVOList(db, interceptionPointId, extraParameters, roleName);
			boolean hasAccessRight = (accessRights.size() > 0) ? true : false;
			//t.printElapsedTime("getHasAccessRight");
			return hasAccessRight;
	    }
	    catch(Exception e)
	    {
	        logger.warn(e);
	        throw new SystemException(e);
	    }
	}

	public Integer getAccessRightId(Integer interceptionPointId, String extraParameters) throws SystemException, Bug
	{
		//Timer t = new Timer();
		List accessRights = AccessRightController.getController().getAccessRightVOListOnly(interceptionPointId, extraParameters);
		//t.printElapsedTime("getAccessRightId");

		return accessRights.size() > 0 ? ((AccessRightVO)accessRights.get(0)).getAccessRightId() : null;
	}

	public Integer getAccessRightId(Integer interceptionPointId, String extraParameters, Database db) throws SystemException, Bug
	{
		//Timer t = new Timer();
		List accessRights = AccessRightController.getController().getAccessRightVOListOnly(db, interceptionPointId, extraParameters);
		//t.printElapsedTime("getAccessRightId");

		return accessRights.size() > 0 ? ((AccessRightVO)accessRights.get(0)).getAccessRightId() : null;
	}

	public Integer[] getAccessRightIds(Integer interceptionPointId, String extraParameters) throws SystemException, Bug
	{
		//Timer t = new Timer();
		List accessRights = AccessRightController.getController().getAccessRightVOListOnly(interceptionPointId, extraParameters);
		Integer[] accessRightIds = new Integer[accessRights.size()];
		Iterator accessRightsIterator = accessRights.iterator();
		int i=0;
		while(accessRightsIterator.hasNext())
		{
			accessRightIds[i] = ((AccessRightVO)accessRightsIterator.next()).getId();
            i++;
		}
		//t.printElapsedTime("getAccessRightIds");
		return accessRightIds;
	}

	public Integer[] getAccessRightIds(Integer interceptionPointId, String extraParameters, Database db) throws SystemException, Bug
	{
		//Timer t = new Timer();
		List accessRights = AccessRightController.getController().getAccessRightVOListOnly(db, interceptionPointId, extraParameters);
		Integer[] accessRightIds = new Integer[accessRights.size()];
		Iterator accessRightsIterator = accessRights.iterator();
		int i=0;
		while(accessRightsIterator.hasNext())
		{
			accessRightIds[i] = ((AccessRightVO)accessRightsIterator.next()).getId();
            i++;
		}
		//t.printElapsedTime("getAccessRightIds");
		return accessRightIds;
	}

	public List<AccessRightGroupVO> getAccessRightGroups(Integer accessRightId) throws SystemException, Bug
	{
		//Timer t = new Timer();
		List<AccessRightGroupVO> accessRightGroups = AccessRightController.getController().getAccessRightGroupVOList(accessRightId);
		//t.printElapsedTime("accessRightGroups");
		return accessRightGroups;
	}
		
	public List getRoleList()
	{
		return this.roleList;
	}
	
	public List getGroupList()
	{
		return this.groupList;
	}

	public String getReturnAddress()
	{
		return returnAddress;
	}

	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}

	public String getColorScheme()
	{
		return this.colorScheme;
	}

	public void setColorScheme(String colorScheme)
	{
		this.colorScheme = colorScheme;
	}

	public Integer getInterceptionPointId()
	{
		return this.interceptionPointId;
	}

	public void setInterceptionPointId(Integer interceptionPointId)
	{
		this.interceptionPointId = interceptionPointId;
	}

	public String getInterceptionPointName()
	{
		return this.interceptionPointName;
	}

	public String getExtraParameters()
	{
		return this.extraParameters;
	}

	public void setExtraParameters(String extraParameters)
	{
		this.extraParameters = extraParameters;
	}

	public String[] getExtraMultiParameters()
	{
		return this.extraMultiParameters;
	}

	public void setExtraMultiParameters(String[] extraMultiParameters)
	{
		this.extraMultiParameters = extraMultiParameters;
	}

	public void setInterceptionPointName(String interceptionPointName)
	{
		this.interceptionPointName = interceptionPointName;
	}

	public String getInterceptionPointCategory()
	{
		return this.interceptionPointCategory;
	}

	public void setInterceptionPointCategory(String interceptionPointCategory)
	{
		this.interceptionPointCategory = interceptionPointCategory;
	}

	public List getInterceptionPointVOList()
	{
		return this.interceptionPointVOList;
	}

    public Collection getAccessRightsUserRows()
    {
        return accessRightsUserRows;
    }

	public Map<Integer, List<AccessRightGroupVO>> getAccessRightGroupsMap() 
	{
		return accessRightGroupsMap;
	}

	public Map<String, Object> getAccessRightHasAccessMap() 
	{
		return accessRightHasAccessMap;
	}

	public String getSaved()
	{
		return saved;
	}

	public void setSaved(String saved)
	{
		this.saved = saved;
	}

	public Boolean getShowInline()
	{
		return showInline;
	}

	public void setShowInline(Boolean showInline)
	{
		this.showInline = showInline;
	}

	public Boolean getCloseOnLoad()
	{
		return closeOnLoad;
	}

	public void setCloseOnLoad(Boolean closeOnLoad)
	{
		this.closeOnLoad = closeOnLoad;
	}
	
	public String getExtraAccessRightInfo()
	{
		return extraAccessRightInfo;
	}

}