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


public class CreateWorkflowDefinitionAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
	private WorkflowDefinitionVO workflowDefinitionVO = new WorkflowDefinitionVO();
	private ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		
	public String doInput() throws Exception
    {
    	return "input";
    }
	
	protected String doExecute() throws Exception 
	{
		ceb.add(this.workflowDefinitionVO.validate());
    	ceb.throwIfNotEmpty();	
    				
		this.workflowDefinitionVO = WorkflowDefinitionController.getController().create(this.workflowDefinitionVO);
		
		return "success";
	}

    public Integer getWorkflowDefinitionId()
    {
        return this.workflowDefinitionVO.getId();
    }

	public void setName(String name)
	{
		this.workflowDefinitionVO.setName(name);	
	}

    public String getName()
    {
        return this.workflowDefinitionVO.getName();
    }
	
	public void setValue(String value)
	{
        this.workflowDefinitionVO.setValue(value);
	}

	public String getValue()
	{
		return this.workflowDefinitionVO.getValue();	
	}

}
