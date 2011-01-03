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

/**
 * This class implements the action class for viewContentTypeDefinition.
 * The use-case lets the user see all information about a specific site/contentTypeDefinition.
 *
 * @author Mattias Bogeblad
 */

public class ViewWorkflowDefinitionAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;

    private Integer workflowDefinitionId;
    
    private WorkflowDefinitionVO workflowDefinitionVO;
    
	
    /**
     * The main method that fetches the Value-object for this use-case
     */

    public String doExecute() throws Exception
    {
        this.workflowDefinitionVO = WorkflowDefinitionController.getController().getWorkflowDefinitionVOWithId(workflowDefinitionId);

        return SUCCESS;
    }

    public Integer getWorkflowDefinitionId()
    {
        return workflowDefinitionId;
    }
    
    public void setWorkflowDefinitionId(Integer workflowDefinitionId)
    {
        this.workflowDefinitionId = workflowDefinitionId;
    }
    
    public WorkflowDefinitionVO getWorkflowDefinitionVO()
    {
        return workflowDefinitionVO;
    }
}
