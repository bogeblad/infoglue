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
import org.infoglue.cms.controllers.kernel.impl.simple.WorkflowDefinitionController;
import org.infoglue.cms.entities.workflow.WorkflowDefinitionVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * @author Mattias Bogeblad
 */

public class UpdateWorkflowDefinitionAction extends InfoGlueAbstractAction
{
	private ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
	private WorkflowDefinitionVO workflowDefinitionVO = new WorkflowDefinitionVO();
	
	private Integer workflowDefinitionId;
	private String name;
	private String value;
	
	public String doExecute() throws Exception
    {
    	ceb.add(this.workflowDefinitionVO.validate());
    	ceb.throwIfNotEmpty();		

		WorkflowDefinitionController.getController().update(this.workflowDefinitionVO);
		
		return "success";
	}
	
	public String doSaveAndExit() throws Exception
    {
    	doExecute();
    	
		return "saveAndExit";
	}

    public String getName()
    {
        return workflowDefinitionVO.getName();
    }
    
    public void setName(String name)
    {
        this.workflowDefinitionVO.setName(name);
    }
    
    public String getValue()
    {
        return workflowDefinitionVO.getValue();
    }
    
    public void setValue(String value)
    {
        this.workflowDefinitionVO.setValue(value);
    }
    
    public Integer getWorkflowDefinitionId()
    {
        return workflowDefinitionVO.getWorkflowDefinitionId();
    }
    
    public void setWorkflowDefinitionId(Integer workflowDefinitionId)
    {
        this.workflowDefinitionVO.setWorkflowDefinitionId(workflowDefinitionId);
    }
}
