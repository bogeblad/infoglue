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

import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AvailableServiceBindingController;
import org.infoglue.cms.controllers.kernel.impl.simple.ServiceDefinitionController;
import org.infoglue.cms.entities.management.AvailableServiceBindingVO;

public class ViewAvailableServiceBindingAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewAvailableServiceBindingAction.class.getName());

	private static final long serialVersionUID = 1L;

    private AvailableServiceBindingVO availableServiceBindingVO;
	private List availableServiceDefinitionVOList;
    private List serviceDefinitionVOList;
    
    public ViewAvailableServiceBindingAction()
    {
        this(new AvailableServiceBindingVO());
    }
    
    public ViewAvailableServiceBindingAction(AvailableServiceBindingVO availableServiceBindingVO)
    {
        this.availableServiceBindingVO = availableServiceBindingVO;
    }
    
    protected void initialize(Integer availableServiceBindingId) throws Exception
    {
	    //ViewAvailableServiceBindingUCC viewAvailableServiceBindingUCC = ViewAvailableServiceBindingUCCFactory.newViewAvailableServiceBindingUCC();
        //availableServiceBindingVO = viewAvailableServiceBindingUCC.viewAvailableServiceBinding(availableServiceBindingId);
        availableServiceBindingVO = AvailableServiceBindingController.getController().getAvailableServiceBindingVOWithId(availableServiceBindingId);
        
        //availableServiceDefinitionVOList = viewAvailableServiceBindingUCC.getAvailableServiceDefinitions(availableServiceBindingId);
        //serviceDefinitionVOList = viewAvailableServiceBindingUCC.getAllServiceDefinitions();
        availableServiceDefinitionVOList = AvailableServiceBindingController.getController().getServiceDefinitionVOList(availableServiceBindingId);
        serviceDefinitionVOList = ServiceDefinitionController.getController().getServiceDefinitionVOList();
    } 
    
    public String doExecute() throws Exception
    {
        logger.info("Executing doExecute on ViewAvailableServiceBindingAction..");
		initialize(getAvailableServiceBindingId());
		/*
        ViewAvailableServiceBindingUCC viewAvailableServiceBindingUCC = ViewAvailableServiceBindingUCCFactory.newViewAvailableServiceBindingUCC();
        availableServiceBindingVO = viewAvailableServiceBindingUCC.viewAvailableServiceBinding(getAvailableServiceBindingId());
        
        availableServiceDefinitionVOList = viewAvailableServiceBindingUCC.getAvailableServiceDefinitions(getAvailableServiceBindingId());
        serviceDefinitionVOList = viewAvailableServiceBindingUCC.getAllServiceDefinitions();
		*/
        logger.info("Finished executing doExecute on ViewAvailableServiceBindingAction..");
        return "success";
    }
        
    public java.lang.Integer getAvailableServiceBindingId()
    {
        return this.availableServiceBindingVO.getAvailableServiceBindingId();
    }

    public void setAvailableServiceBindingId(Integer availableServiceBindingId)
    {
    	this.availableServiceBindingVO.setAvailableServiceBindingId(availableServiceBindingId);
    }
        
    public java.lang.String getName()
    {
        return this.availableServiceBindingVO.getName();
    }

    public java.lang.String getDescription()
    {
        return this.availableServiceBindingVO.getDescription();
    }

    public java.lang.String getVisualizationAction()
    {
        return this.availableServiceBindingVO.getVisualizationAction();
    }

    public Boolean getIsMandatory()
    {
        return this.availableServiceBindingVO.getIsMandatory();
    }

    public Boolean getIsUserEditable()
    {
        return this.availableServiceBindingVO.getIsUserEditable();
    }

    public Boolean getIsInheritable()
    {
        return this.availableServiceBindingVO.getIsInheritable();
    }

    public List getAvailableServiceDefinitions()
    {
    	return this.availableServiceDefinitionVOList;
    }

    public List getAllServiceDefinitions()
    {    	
    	return this.serviceDefinitionVOList;
    }
}
