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
package org.infoglue.cms.workflow.taglib;

import org.infoglue.cms.applications.workflowtool.util.InfogluePropertySet;
import org.infoglue.cms.controllers.kernel.impl.simple.WorkflowController;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.deliver.taglib.AbstractTag;

/**
 * Base class for all workflow related tags. 
 * 
 * Provides access to the propertyset associated with the workflow and
 * the parameters used to identify a workflow.
 */
public abstract class WorkflowTag extends AbstractTag 
{
	/**
	 * The name used to identify the current workflow action.
	 */
	public static final String ACTION_ID_PARAMETER   = "actionId";
	
	/**
	 * The name used to identify the current workflow.
	 */
	public static final String WORKFLOW_ID_PARAMETER = "workflowId";

	/**
	 * Default constructor.
	 */
	protected WorkflowTag() 
	{
		super();
	}

	/**
	 * Returns the identifier of the current workflow.
	 * 
	 * @return the workflow identifier.
	 */
	protected final String getWorkflowID() 
	{
		return pageContext.getRequest().getParameter(WORKFLOW_ID_PARAMETER);
	}

	/**
	 * Returns the identifier of the current workflow action.
	 * 
	 * @return the action identifier.
	 */
	protected final String getActionID() 
	{
		return pageContext.getRequest().getParameter(ACTION_ID_PARAMETER);
	}

	/**
	 * Returns the principal associated with the current session.
	 * 
	 * @return the principal associated with the current session.
	 */
	protected final InfoGluePrincipal getPrincipal() 
	{
		return (InfoGluePrincipal) pageContext.getSession().getAttribute("org.infoglue.cms.security.user");
	}

	/**
	 * Returns the propertyset associated with the current workflow.
	 * 
	 * @return the propertyset associated with the current workflow.
	 */
	protected final InfogluePropertySet getPropertySet() 
	{
		return new InfogluePropertySet(WorkflowController.getController().getPropertySet(getPrincipal(), Long.valueOf(getWorkflowID()).longValue()));
	}
}
