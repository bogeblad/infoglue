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
import org.infoglue.cms.entities.workflow.ConsequenceDefinition;
import org.infoglue.cms.entities.workflow.ConsequenceDefinitionVO;

public class ConsequenceDefinitionImpl implements ConsequenceDefinition
{
    private ConsequenceDefinitionVO valueObject = new ConsequenceDefinitionVO();
     
   	public Integer getId()
	{
		return this.getConsequenceDefinitionId();
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
		setValueObject((ConsequenceDefinitionVO) valueObject);
	}

	 
    public ConsequenceDefinitionVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(ConsequenceDefinitionVO valueObject)
    {
        this.valueObject = valueObject;
    }   
        
    
    public java.lang.Integer getConsequenceDefinitionId()
    {
        return this.valueObject.getConsequenceDefinitionId();
    }
            
    public void setConsequenceDefinitionId(java.lang.Integer consequenceDefinitionId)
    {
        this.valueObject.setConsequenceDefinitionId(consequenceDefinitionId);
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
      
    public java.lang.String getInvokerClassName()
    {
        return this.valueObject.getInvokerClassName();
    }
            
    public void setInvokerClassName(java.lang.String invokerClassName)
    {
        this.valueObject.setInvokerClassName(invokerClassName);
    }
  }        
