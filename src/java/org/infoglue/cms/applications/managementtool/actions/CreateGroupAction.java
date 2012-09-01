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
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupPropertiesController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.GroupVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.DateHelper;


/**
 * @author mgu
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */


public class CreateGroupAction extends InfoGlueAbstractAction
{
	private GroupVO groupVO;
	private List infoGluePrincipals = new ArrayList();
	private List contentTypeDefinitionVOList;
	private ConstraintExceptionBuffer ceb;

	public CreateGroupAction()
	{
		this(new GroupVO());
	}

	public CreateGroupAction(GroupVO GroupVO)
	{
		this.groupVO = GroupVO;	
		this.ceb = new ConstraintExceptionBuffer();
	}
		
	public String doInput() throws Exception
    {
    	return "input";
    }
	
	public String doInputV3() throws Exception
    {
		this.infoGluePrincipals	= UserControllerProxy.getController().getAllUsers();
		this.contentTypeDefinitionVOList = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList(ContentTypeDefinitionVO.EXTRANET_GROUP_PROPERTIES);

    	return "inputV3";
    }

	protected String doExecute() throws Exception 
	{
		ceb.add(this.groupVO.validate());
    	ceb.throwIfNotEmpty();	
    				
		String[] userNames = getRequest().getParameterValues("userName");
		String[] contentTypeDefinitionIds = getRequest().getParameterValues("contentTypeDefinitionId");
		
		GroupControllerProxy.getController().createGroup(this.groupVO);
		if(userNames != null)
		{
			GroupControllerProxy.getController().updateGroup(this.groupVO, userNames);
		}
		
		if(contentTypeDefinitionIds != null && contentTypeDefinitionIds.length > 0 && !contentTypeDefinitionIds[0].equals(""))
			GroupPropertiesController.getController().updateContentTypeDefinitions(this.getGroupName(), contentTypeDefinitionIds);

		return "success";
	}
	
	public String doV3() throws Exception 
	{
		try
		{
			doExecute();
			
			String[] interceptionPointNames = new String[]{"Group.ManageUsers", "Group.ManageAccessRights", "Group.ReadForAssignment"};
			AccessRightController.getController().addUserRights(interceptionPointNames, getGroupName(), getInfoGluePrincipal());
		}
		catch(ConstraintException e) 
        {
			this.infoGluePrincipals	= UserControllerProxy.getController().getAllUsers();
			this.contentTypeDefinitionVOList = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList(ContentTypeDefinitionVO.EXTRANET_ROLE_PROPERTIES);

			e.setResult(INPUT + "V3");
			throw e;
        }
		
		return "successV3";
	}

	public String doSaveAndExitV3() throws Exception 
	{
		doV3();
		
		return "successSaveAndExitV3";
	}

	public void setGroupName(String groupName)
	{
		this.groupVO.setGroupName(groupName);	
	}

    public String getGroupName()
    {
        return this.groupVO.getGroupName();
    }
	
	public void setDescription(java.lang.String description)
	{
        this.groupVO.setDescription(description);
	}

	public String getDescription()
	{
		return this.groupVO.getDescription();	
	}

    public String getSource()
    {
    	return this.groupVO.getSource();
    }
    
    public void setSource(String source)
    {
    	this.groupVO.setSource(source);
    }

    public String getGroupType()
    {
    	return this.groupVO.getGroupType();
    }
    
    public void setGroupType(String groupType)
    {
    	this.groupVO.setGroupType(groupType);
    }

    public Boolean getIsActive()
    {
    	return this.groupVO.getIsActive();
    }
    
    public void setIsActive(Boolean isActive)
    {
    	this.groupVO.setIsActive(isActive);
    }

	public List getInfoGluePrincipals()
	{
		return infoGluePrincipals;
	}

	public List getContentTypeDefinitionVOList()
	{
		return contentTypeDefinitionVOList;
	}
    


}
