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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * This is the general action description object. Can be used by any workflow engine hopefully.
 *
 * @author Mattias Bogeblad
 */

public class WorkflowStepVO implements BaseEntityVO
{
	private static final long serialVersionUID = 1L;

	private WorkflowVO workflow;
	private Long workflowId;
	private Integer id;
	private Integer stepId;
	private String name;
	private String status;
	private String owner;
	private String caller;
	private Date startDate;
	private Date finishDate;
	private List actions = new ArrayList();

	public WorkflowStepVO() {}
	public WorkflowStepVO(final WorkflowVO workflow) 
	{
		this.workflow = workflow;
	}
	
	public WorkflowVO getWorkflow() 
	{
		return this.workflow;
	}
	
	public Integer getId()
	{
		return this.id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public Long getWorkflowId()
	{
		return workflowId;
	}

	public void setWorkflowId(Long workflowId)
	{
		this.workflowId = workflowId;
	}

	public Integer getStepId()
	{
		return this.stepId;
	}

	public void setStepId(Integer stepId)
	{
		this.stepId = stepId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public String getCaller()
	{
		return caller;
	}

	public void setCaller(String caller)
	{
		this.caller = caller;
	}

	public Date getFinishDate()
	{
		return this.finishDate;
	}

	public Date getStartDate()
	{
		return this.startDate;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setFinishDate(Date finishDate)
	{
		this.finishDate = finishDate;
	}

	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public List getActions()
	{
		return Collections.unmodifiableList(actions);
	}

	/**
	 * Adds the given action to this step, associating this step with the given action.
	 * @param action a WorkflowActionVO representing a workflow action
	 */
	public void addAction(WorkflowActionVO action)
	{
		if (actions == null)
			actions = new ArrayList();

		action.setStep(this);
		actions.add(action);
	}

	/**
	 * Indicates whether this step has an owner
	 * @return true if owner is not null and owner.length() > 0, otherwise returns false
	 */
	public boolean hasOwner()
	{
		return owner != null && owner.length() > 0;
	}

	/**
	 * Indicates whether this step is owned by the given user.
	 * @param user a user
	 * @return true if user matches owner, otherwise returns false.
	 */
	public boolean isOwner(String user)
	{
		return (owner == null) ? owner == user : owner.equalsIgnoreCase(user);
	}

	public ConstraintExceptionBuffer validate()
	{
		return new ConstraintExceptionBuffer();
	}

	public String toString()
	{
		return new StringBuffer(getClass().getName())
				.append(" stepId=").append(stepId)
				.append(" name=").append(name)
				.append(" status=").append(status)
				.append(" owner=").append(owner)
				.append(" caller=").append(caller)
				.append(" startDate=").append(startDate)
				.append(" finishDate=").append(finishDate)
				.append(" id=").append(id)
				.append(" workflowId=").append(workflowId).toString();
	}
}
