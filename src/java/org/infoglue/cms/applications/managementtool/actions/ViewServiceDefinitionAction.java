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

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ServiceDefinitionController;
import org.infoglue.cms.entities.management.ServiceDefinitionVO;


public class ViewServiceDefinitionAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewServiceDefinitionAction.class.getName());

	private static final long serialVersionUID = 1L;

    private ServiceDefinitionVO serviceDefinitionVO;

    public ViewServiceDefinitionAction()
    {
        this(new ServiceDefinitionVO());
    }
    
    public ViewServiceDefinitionAction(ServiceDefinitionVO serviceDefinitionVO)
    {
        this.serviceDefinitionVO = serviceDefinitionVO;
    }
    
    protected void initialize(Integer serviceDefinitionId) throws Exception
    {
        serviceDefinitionVO = ServiceDefinitionController.getController().getServiceDefinitionVOWithId(serviceDefinitionId);
    } 

    public String doExecute() throws Exception
    {
        logger.info("Executing doExecute on ViewServiceDefinitionAction..");
        this.initialize(getServiceDefinitionId());
        logger.info("Finished executing doExecute on ViewServiceDefinitionAction..");
        return "success";
    }
        
    public java.lang.Integer getServiceDefinitionId()
    {
        return this.serviceDefinitionVO.getServiceDefinitionId();
    }
        
    public void setServiceDefinitionId(java.lang.Integer serviceDefinitionId)
    {
        this.serviceDefinitionVO.setServiceDefinitionId(serviceDefinitionId);
    }
    
    public java.lang.String getName()
    {
        return this.serviceDefinitionVO.getName();
    }

    public java.lang.String getDescription()
    {
        return this.serviceDefinitionVO.getDescription();
    }

    public java.lang.String getClassName()
    {
        return this.serviceDefinitionVO.getClassName();
    }
}
