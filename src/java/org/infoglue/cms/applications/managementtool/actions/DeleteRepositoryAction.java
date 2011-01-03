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

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;

/**
 * This action removes a repository from the system.
 * 
 * @author Mattias Bogeblad
 */

public class DeleteRepositoryAction extends InfoGlueAbstractAction
{
	private RepositoryVO repositoryVO;
	private Integer repositoryId;
	private String returnAddress = null;
	
	public DeleteRepositoryAction()
	{
		this(new RepositoryVO());
	}

	public DeleteRepositoryAction(RepositoryVO repositoryVO) 
	{
		this.repositoryVO = repositoryVO;
	}

	public String doMarkForDelete() throws ConstraintException, Exception 
	{
		boolean hasAccessToManagementTool = hasAccessTo("ManagementTool.Read");
		if(!hasAccessToManagementTool)
			throw new AccessConstraintException("Repository.delete", "1003");

		validateSecurityCode();

		this.repositoryVO.setRepositoryId(this.getRepositoryId());
		try
		{
			RepositoryController.getController().markForDelete(this.repositoryVO, this.getInfoGluePrincipal().getName(), this.getInfoGluePrincipal());
			return "success";
		}
		catch(ConstraintException ce)
		{
			returnAddress = "ViewRepository.action?repositoryId=" + this.repositoryVO.getId();
			if(ce.getErrorCode().equals("3300") && ce.getFieldName().equals("ContentVersion.stateId"))	
				throw new ConstraintException("ContentVersion.stateId", "3307", ce.getExtraInformation());
			else if(ce.getErrorCode().equals("3400") && ce.getFieldName().equals("SiteNodeVersion.stateId"))	
				throw new ConstraintException("ContentVersion.stateId", "3406", ce.getExtraInformation());
			else
				throw ce;
		}
	}
	
	protected String doExecute() throws ConstraintException, Exception 
	{
		boolean hasAccessToManagementTool = hasAccessTo("ManagementTool.Read");
		if(!hasAccessToManagementTool)
			throw new AccessConstraintException("Repository.delete", "1003");

		validateSecurityCode();

		this.repositoryVO.setRepositoryId(this.getRepositoryId());
		try
		{
			RepositoryController.getController().delete(this.repositoryVO, this.getInfoGluePrincipal().getName(), this.getInfoGluePrincipal());

		    ViewMessageCenterAction.addSystemMessage(this.getInfoGluePrincipal().getName(), ViewMessageCenterAction.SYSTEM_MESSAGE_TYPE, "refreshRepositoryList();");

			return "success";
		}
		catch(ConstraintException ce)
		{
			returnAddress = "ViewRepository.action?repositoryId=" + this.repositoryVO.getId();
			if(ce.getErrorCode().equals("3300") && ce.getFieldName().equals("ContentVersion.stateId"))	
				throw new ConstraintException("ContentVersion.stateId", "3307", ce.getExtraInformation());
			else if(ce.getErrorCode().equals("3400") && ce.getFieldName().equals("SiteNodeVersion.stateId"))	
				throw new ConstraintException("ContentVersion.stateId", "3406", ce.getExtraInformation());
			else
				throw ce;
		}
	}

	public String doExecuteByForce() throws ConstraintException, Exception 
	{
		boolean hasAccessToManagementTool = hasAccessTo("ManagementTool.Read");
		if(!hasAccessToManagementTool)
			throw new AccessConstraintException("Repository.delete", "1003");
			
		validateSecurityCode();

	    this.repositoryVO.setRepositoryId(this.getRepositoryId());
		try
		{
			RepositoryController.getController().delete(this.repositoryVO, this.getInfoGluePrincipal().getName(), true, this.getInfoGluePrincipal());

			ViewMessageCenterAction.addSystemMessage(this.getInfoGluePrincipal().getName(), ViewMessageCenterAction.SYSTEM_MESSAGE_TYPE, "refreshRepositoryList();");
			
			return "success";
		}
		catch(ConstraintException ce)
		{
			returnAddress = "ViewRepository.action?repositoryId=" + this.repositoryVO.getId();
			if(ce.getErrorCode().equals("3300") && ce.getFieldName().equals("ContentVersion.stateId"))	
				throw new ConstraintException("ContentVersion.stateId", "3307", ce.getExtraInformation());
			else if(ce.getErrorCode().equals("3400") && ce.getFieldName().equals("SiteNodeVersion.stateId"))	
				throw new ConstraintException("ContentVersion.stateId", "3406", ce.getExtraInformation());
			else
				throw ce;
		}
	}

	public void setRepositoryId(Integer repositoryId) throws SystemException
	{
		this.repositoryVO.setRepositoryId(repositoryId);	
	}

    public java.lang.Integer getRepositoryId()
    {
        return this.repositoryVO.getRepositoryId();
    }
        
	public String getReturnAddress() 
	{
		return this.returnAddress;
	}

}
