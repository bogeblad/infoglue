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

package org.infoglue.cms.entities.workflow.impl.simple;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.workflow.Workflow;
import org.infoglue.cms.entities.workflow.WorkflowVO;

public class WorkflowImpl implements Workflow
{
    private WorkflowVO valueObject = new WorkflowVO();
     
    public Integer getId()
	{
		return this.getWorkflowId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}

	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getVO()
	 */
	public BaseEntityVO getVO() 
	{
		return (BaseEntityVO) getValueObject();
	}
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#setVO(BaseEntityVO)
	 */
	public void setVO(BaseEntityVO valueObject) 
	{
		setValueObject((WorkflowVO) valueObject);
	}
 
    public WorkflowVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(WorkflowVO valueObject)
    {
        this.valueObject = valueObject;
    }   

    private java.util.Collection actions;
  
    
    public java.lang.Integer getWorkflowId()
    {
        return this.valueObject.getWorkflowId();
    }
            
    public void setWorkflowId(java.lang.Integer workflowId)
    {
        this.valueObject.setWorkflowId(workflowId);
    }
      
    public java.lang.String getName()
    {
        return this.valueObject.getName();
    }
            
    public void setName(java.lang.String name)
    {
        this.valueObject.setName(name);
    }
      
    public java.lang.String getDescription()
    {
        return this.valueObject.getDescription();
    }
            
    public void setDescription(java.lang.String description)
    {
        this.valueObject.setDescription(description);
    }
      
    public java.util.Collection getActions()
    {
        return this.actions;
    }
            
    public void setActions (java.util.Collection actions)
    {
        this.actions = actions;
    }
  }        
