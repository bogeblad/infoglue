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

package org.infoglue.cms.util.workflow;

import java.util.Map;

import org.infoglue.deliver.util.CacheController;

import com.opensymphony.workflow.AbstractWorkflow;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.basic.BasicWorkflow;
import com.opensymphony.workflow.basic.BasicWorkflowContext;


/**
 * A basic workflow implementation which does not read in
 * the current user from any context, but allows one to be
 * specified via the constructor. Also does not support rollbacks.
 */
public class InfoGlueBasicWorkflow extends BasicWorkflow 
{
    public InfoGlueBasicWorkflow(String caller) 
    {
        super(caller);
    }
	
    public void changeEntryState(long id, int newState) throws WorkflowException 
    {
        super.changeEntryState(id, newState);
        CacheController.clearCache("myActiveWorkflows");
    }

    public void doAction(long id, int actionId, Map inputs) throws WorkflowException 
    {
        super.doAction(id, actionId, inputs);
        CacheController.clearCache("myActiveWorkflows");
    }

    public long initialize(String workflowName, int initialState, Map inputs) throws WorkflowException 
    {
        long id = super.initialize(workflowName, initialState, inputs);
        CacheController.clearCache("myActiveWorkflows");
        return id;
    }

}
