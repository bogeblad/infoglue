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

package org.infoglue.cms.applications.managementtool.actions;

import java.util.ArrayList;
import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.InconsistenciesController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.AccessRight;
import org.infoglue.cms.entities.management.AccessRightGroupVO;
import org.infoglue.cms.entities.management.AccessRightRoleVO;
import org.infoglue.cms.entities.management.AccessRightUserVO;
import org.infoglue.cms.entities.management.AccessRightVO;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.TableCount;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;

/**
 * This class acts as a system tail on the logfiles available.
 * 
 * @author Mattias Bogeblad
 */

public class ViewInconsistenciesAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
	private List inconsistencies = null;
	private Integer registryId = null;
	
	private List<AccessRightVO> accessRightVOList = new ArrayList<AccessRightVO>();
	private String accessRightsStatusText = null;

	private List<AccessRightVO> duplicateAccessRightVOList = new ArrayList<AccessRightVO>(); 
	private List<AccessRightVO> duplicateAutoDeletableAccessRightVOList = new ArrayList<AccessRightVO>(); 
	private List<AccessRightVO> duplicateAutoMergableAccessRightVOList = new ArrayList<AccessRightVO>(); 
		
	private Integer interceptionPointId;
	private String parameters;
	private String[] roleNames = new String[]{};
	private String[] groupNames = new String[]{};
	private String[] userNames = new String[]{};
	
	public String doInput() throws Exception
    {
		logUserActionInfo(getClass(), "doInput");
    	return "input";
    }

    public String doExecute() throws Exception
    {
		logUserActionInfo(getClass(), "doExecute");
    	inconsistencies = InconsistenciesController.getController().getAllInconsistencies();
        
    	return "success";
    }

    public String doRemoveReference() throws Exception
    {
		logUserActionInfo(getClass(), "doRemoveReference");
    	InconsistenciesController.getController().removeReferences(registryId, this.getInfoGluePrincipal());
    	
    	inconsistencies = InconsistenciesController.getController().getAllInconsistencies();
        
    	return "success";
    }
    
    public List getInconsistencies() 
	{
		return inconsistencies;
	}
    
    public String doInputAccessRights() throws Exception
    {
		logUserActionInfo(getClass(), "doInputAccessRights");
    	this.accessRightsStatusText = AccessRightController.getController().getAccessRightsStatusText();
    	AccessRightController.getController().getAllDuplicates(true, true, duplicateAccessRightVOList, duplicateAutoDeletableAccessRightVOList, duplicateAutoMergableAccessRightVOList);
    	    	
    	return "inputAccessRights";
    }

    public String doFixAccessRightInconsistencies() throws Exception
    {
		logUserActionInfo(getClass(), "doFixAccessRightInconsistencies");
    	this.accessRightsStatusText = AccessRightController.getController().fixAccessRightInconsistencies();
    	this.accessRightsStatusText += AccessRightController.getController().getAccessRightsStatusText();
    	AccessRightController.getController().getAllDuplicates(true, true, duplicateAccessRightVOList, duplicateAutoDeletableAccessRightVOList, duplicateAutoMergableAccessRightVOList);
        
    	return "inputAccessRights";
    }

    public String doFixEmptyAccessRightInconsistencies() throws Exception
    {
		logUserActionInfo(getClass(), "doFixEmptyAccessRightInconsistencies");
    	this.accessRightsStatusText = AccessRightController.getController().fixEmptyAccessRightInconsistencies();
    	this.accessRightsStatusText += AccessRightController.getController().getAccessRightsStatusText();
    	AccessRightController.getController().getAllDuplicates(true, true, duplicateAccessRightVOList, duplicateAutoDeletableAccessRightVOList, duplicateAutoMergableAccessRightVOList);
        
    	return "inputAccessRights";
    }

    public String doFixAutoMergableAccessRightInconsistencies() throws Exception
    {
		logUserActionInfo(getClass(), "doFixAutoMergableAccessRightInconsistencies");
    	this.accessRightsStatusText = AccessRightController.getController().fixAutoMergableAccessRightInconsistencies();
    	this.accessRightsStatusText += AccessRightController.getController().getAccessRightsStatusText();
    	AccessRightController.getController().getAllDuplicates(true, true, duplicateAccessRightVOList, duplicateAutoDeletableAccessRightVOList, duplicateAutoMergableAccessRightVOList);
        
    	return "inputAccessRights";
    }

    public String doMergeAccessRight() throws Exception
    {
		logUserActionInfo(getClass(), "doMergeAccessRight");
    	this.accessRightsStatusText = AccessRightController.getController().mergeAccessRight(this.interceptionPointId, this.parameters, this.roleNames, this.groupNames, this.userNames);
    	this.accessRightsStatusText += AccessRightController.getController().getAccessRightsStatusText();
    	AccessRightController.getController().getAllDuplicates(true, true, duplicateAccessRightVOList, duplicateAutoDeletableAccessRightVOList, duplicateAutoMergableAccessRightVOList);
        
    	return "inputAccessRights";
    }

    public String doFixAccessRightDuplicates() throws Exception
    {
		logUserActionInfo(getClass(), "doFixAccessRightDuplicates");
    	/*
    	AccessRightController.getController().fixAccessRightDuplicate(this.accessRightIds);
    	
    	this.accessRightVOList = AccessRightController.getController().getAllDuplicates();
        */
    	return "inputAccessRights";
    }

    public SiteNodeVO getSiteNodeVO(String siteNodeId) throws NumberFormatException, SystemException, Bug
    {
    	return SiteNodeController.getController().getSiteNodeVOWithId(new Integer(siteNodeId));
    }

    public ContentVO getContentVO(String contentId) throws NumberFormatException, SystemException, Bug
    {
    	return ContentController.getContentController().getContentVOWithId(new Integer(contentId));
    }

	public Integer getRegistryId() 
	{
		return registryId;
	}

	public void setRegistryId(Integer registryId) 
	{
		this.registryId = registryId;
	}
	
	public String getAccessRightsStatusText() 
	{
		return accessRightsStatusText;
	}

	public List<AccessRightVO> getDuplicateAccessRightVOList() 
	{
		return duplicateAccessRightVOList;
	}

	public List<AccessRightVO> getDuplicateAutoDeletableAccessRightVOList() 
	{
		return duplicateAutoDeletableAccessRightVOList;
	}

	public List<AccessRightVO> getDuplicateAutoMergableAccessRightVOList() 
	{
		return duplicateAutoMergableAccessRightVOList;
	}

	public void setInterceptionPointId(Integer interceptionPointId) 
	{
		this.interceptionPointId = interceptionPointId;
	}

	public void setParameters(String parameters) 
	{
		this.parameters = parameters;
	}

	public void setRoleNames(String[] roleNames) 
	{	
		this.roleNames = roleNames;
	}

	public void setGroupNames(String[] groupNames) 
	{
		this.groupNames = groupNames;
	}

	public void setUserNames(String[] userNames) 
	{
		this.userNames = userNames;
	}

}
