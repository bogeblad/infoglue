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
import org.infoglue.cms.controllers.kernel.impl.simple.ServiceDefinitionController;
import org.infoglue.cms.entities.management.ServiceDefinitionVO;
import org.infoglue.cms.exception.SystemException;

/**
 * This action removes a serviceDefinition from the system.
 * 
 * @author Mattias Bogeblad
 */

public class DeleteServiceDefinitionAction extends InfoGlueAbstractAction
{
	private ServiceDefinitionVO serviceDefinitionVO;
	private Integer serviceDefinitionId;
	
	public DeleteServiceDefinitionAction()
	{
		this(new ServiceDefinitionVO());
	}

	public DeleteServiceDefinitionAction(ServiceDefinitionVO serviceDefinitionVO) {
		this.serviceDefinitionVO = serviceDefinitionVO;
	}
	
	protected String doExecute() throws Exception 
	{
		ServiceDefinitionController.getController().delete(this.serviceDefinitionVO);
		
		return "success";
	}

	public void setServiceDefinitionId(Integer serviceDefinitionId) throws SystemException
	{
		this.serviceDefinitionVO.setServiceDefinitionId(serviceDefinitionId);	
	}

    public java.lang.Integer getServiceDefinitionId()
    {
        return this.serviceDefinitionVO.getServiceDefinitionId();
    }
	
	public String getErrorKey()
	{
		return "ServiceDefinition.deleteAction";
	}
	
	public String getReturnAddress()
	{
		return "ViewListServiceDefinition.action";
	}
}
