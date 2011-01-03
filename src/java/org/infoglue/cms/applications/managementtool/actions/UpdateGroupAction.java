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

import org.infoglue.cms.controllers.kernel.impl.simple.GroupControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupPropertiesController;
import org.infoglue.cms.entities.management.GroupVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * @author Mattias Bogeblad
 */

public class UpdateGroupAction extends ViewGroupAction
{
	private ConstraintExceptionBuffer ceb;
	private GroupVO groupVO;
	
	public UpdateGroupAction()
	{
		this(new GroupVO());
	}
	
	public UpdateGroupAction(GroupVO GroupVO)
	{
		this.groupVO = GroupVO;
		this.ceb = new ConstraintExceptionBuffer();
	}
	
	public String doExecute() throws Exception
    {
    	super.initialize(getGroupName());
    	
    	ceb.add(this.groupVO.validate());
    	ceb.throwIfNotEmpty();		

		String[] values = getRequest().getParameterValues("userName");
		String[] contentTypeDefinitionIds = getRequest().getParameterValues("contentTypeDefinitionId");

		GroupControllerProxy.getController().updateGroup(this.groupVO, values);
		
		if(contentTypeDefinitionIds != null && contentTypeDefinitionIds.length > 0 && !contentTypeDefinitionIds[0].equals(""))
			GroupPropertiesController.getController().updateContentTypeDefinitions(this.getGroupName(), contentTypeDefinitionIds);
		
		return "success";
	}
	
	public String doSaveAndExit() throws Exception
    {
		doExecute();
						
		return "saveAndExit";
	}

	public String doV3() throws Exception
    {
		try
		{
			doExecute();
		}
		catch(ConstraintException e) 
        {
			e.setResult(INPUT + "V3");
			throw e;
        }
		
		return "successV3";
	}

	public String doSaveAndExitV3() throws Exception
    {
		try
		{
			doExecute();
		}
		catch(ConstraintException e) 
        {
			e.setResult(INPUT + "V3");
			throw e;
        }
		
		return "saveAndExitV3";
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
	

}
