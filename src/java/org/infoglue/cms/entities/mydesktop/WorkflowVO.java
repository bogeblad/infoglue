/* ===============================================================================
*
* Part of the InfoGlue Content Management Platform (www.infoglue.org)
*
* ===============================================================================
*
* Copyright (C) Mattias Bogeblad
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

package org.infoglue.cms.entities.mydesktop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.workflow.StepFilter;

/**
 * This is the general action description object. Can be used by any workflow engine hopefully.
 *
 * @author Mattias Bogeblad
 */

public class WorkflowVO implements BaseEntityVO
{
	private static final long serialVersionUID = 1L;

	public static final int STATUS_OK = 0;
	public static final int STATUS_NOT_OK = 1;
	
	private Long workflowId;
	private String name;  // the name of the workflow
	private String title; // the name of the workflow instance
	private List declaredSteps = new ArrayList();
	private List currentSteps = new ArrayList();
	private List historySteps = new ArrayList();
	private List initialActions = new ArrayList();
	private List globalActions = new ArrayList();

	private int status = STATUS_OK;
	private String statusMessage = "";

	public WorkflowVO() {}

	public WorkflowVO(Long workflowId, String name)
	{
		setWorkflowId(workflowId);
		setName(name);
	}

	public Integer getId()
	{
		return new Integer(workflowId.intValue());
	}

	public void setId(Integer id)
	{
		setWorkflowId(new Long(id.longValue()));
	}

	public long getIdAsPrimitive()
	{
		return (workflowId == null) ? 0 : workflowId.longValue();
	}

	public Long getWorkflowId()
	{
		return workflowId;
	}

	public void setWorkflowId(Long workflowId)
	{
		this.workflowId = workflowId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getTitle() 
	{
		return title;
	}
	
	public void setTitle(final String title)
	{
		this.title = title;
	}
	
	public List getDeclaredSteps()
	{
		return declaredSteps;
	}

	public void setDeclaredSteps(List steps)
	{
		declaredSteps = (steps == null) ? new ArrayList() : steps;
	}

	public List getCurrentSteps()
	{
		return currentSteps;
	}

	/**
	 * Returns all the current steps allowed by the given filter.  Useful to restrict the current steps for display, e.g.
	 * return only the steps owned by the current user.
	 * @param filter a StepFilter
	 * @return the current steps allowed by filter
	 */
	public List getCurrentSteps(StepFilter filter)
	{
		List filteredSteps = new ArrayList();
		for (Iterator steps = currentSteps.iterator(); steps.hasNext();)
		{
			WorkflowStepVO step = (WorkflowStepVO)steps.next();
			if (filter.isAllowed(step))
				filteredSteps.add(step);
		}

		return filteredSteps;
	}

	public void setCurrentSteps(List steps)
	{
		currentSteps = (steps == null) ? new ArrayList() : steps;
	}

	public List getHistorySteps()
	{
		return historySteps;
	}

	public void setHistorySteps(List steps)
	{
		historySteps = (steps == null) ? new ArrayList() : steps;
	}

	public List getInitialActions()
	{
		return initialActions;
	}

	public void setInitialActions(List actions)
	{
		initialActions = (actions == null) ? new ArrayList() : actions;
	}

	public List getGlobalActions()
	{
		return globalActions;
	}

	public void setGlobalActions(List actions)
	{
		globalActions = (actions == null) ? new ArrayList() : actions;
	}

	/**
	 * Returns the current and history steps
	 * @return a list of WorkflowStepVOs representing all current and history steps for this workflow
	 */
	public List getSteps()
	{
		List steps = new ArrayList();
		steps.addAll(currentSteps);
		steps.addAll(historySteps);
		return steps;
	}

	/**
	 * Returns the available actions, i.e., the actions associated with the current steps.
	 * @return a list of WorkflowActionVOs representing the available actions in this workflow.
	 */
	public List getAvailableActions()
	{
		return getAvailableActions(null);
	}

	/**
	 * Returns the actions associated with the current steps allowed by the given step filter
	 * @param filter a step filter that allows the desired steps to pass through
	 * @return a list of WorkflowActionVOs representing the actions associated with the current steps allowed by filter
	 * @see #getCurrentSteps(StepFilter)
	 */
	public List getAvailableActions(StepFilter filter)
	{
		List steps = (filter == null) ? currentSteps : getCurrentSteps(filter);
		List availableActions = new ArrayList();

		for (Iterator i = steps.iterator(); i.hasNext();)
			availableActions.addAll(((WorkflowStepVO)i.next()).getActions());

		return availableActions;
	}

	/**
	 * Returns the initial action with the given ID.  Since the number of initial actions expected to be small, an
	 * iterative match works fine here.  We don't need to introduce the overhead of a map until we get more than 5
	 * initial actions, which seems far-fetched for a realistic workflow.
	 * @param id the id of the desired initial action
	 * @return the initial action with id
	 * @throws IllegalArgumentException if no initial action exists with id
	 * @throws NullPointerException if id is null.
	 */
	public WorkflowActionVO getInitialAction(Integer id)
	{
		for (Iterator actions = initialActions.iterator(); actions.hasNext();)
		{
			WorkflowActionVO action = (WorkflowActionVO)actions.next();
			if (id.equals(action.getId()))
				return action;
		}

		throw new IllegalArgumentException("Initial action " + id + " does not exist in workflow " + name);
	}

	public String toString()
	{
		return new StringBuffer(getClass().getName())
				.append(" name=").append(name)
				.append(" workflowId=").append(workflowId)
				.append(" declaredSteps=").append(declaredSteps.size())
				.append(" currentSteps=").append(currentSteps.size())
				.append(" historySteps=").append(historySteps.size())
				.append(" historySteps=").append(historySteps.size())
				.append(" globalActions=").append(globalActions.size()).toString();
	}

	public ConstraintExceptionBuffer validate()
	{
		return new ConstraintExceptionBuffer();
	}

	public String getStatusMessage()
	{
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage)
	{
		this.statusMessage = statusMessage;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}
}
