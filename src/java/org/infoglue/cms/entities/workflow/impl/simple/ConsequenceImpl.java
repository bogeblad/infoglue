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
import org.infoglue.cms.entities.workflow.Consequence;
import org.infoglue.cms.entities.workflow.ConsequenceVO;

public class ConsequenceImpl implements Consequence
{
    private ConsequenceVO valueObject = new ConsequenceVO();


   	public Integer getId()
	{
		return this.getConsequenceId();
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
		setValueObject((ConsequenceVO) valueObject);
	}

     
    public ConsequenceVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(ConsequenceVO valueObject)
    {
        this.valueObject = valueObject;
    }   

    private org.infoglue.cms.entities.workflow.impl.simple.ConsequenceDefinitionImpl consequenceDefinition;
    private org.infoglue.cms.entities.workflow.impl.simple.ActionImpl action;
  
    
    public java.lang.Integer getConsequenceId()
    {
        return this.valueObject.getConsequenceId();
    }
            
    public void setConsequenceId(java.lang.Integer consequenceId)
    {
        this.valueObject.setConsequenceId(consequenceId);
    }
      
    public org.infoglue.cms.entities.workflow.impl.simple.ConsequenceDefinitionImpl getConsequenceDefinition()
    {
        return this.consequenceDefinition;
    }
            
    public void setConsequenceDefinition (org.infoglue.cms.entities.workflow.impl.simple.ConsequenceDefinitionImpl consequenceDefinition)
    {
        this.consequenceDefinition = consequenceDefinition;
    }
      
    public org.infoglue.cms.entities.workflow.impl.simple.ActionImpl getAction()
    {
        return this.action;
    }
            
    public void setAction (org.infoglue.cms.entities.workflow.impl.simple.ActionImpl action)
    {
        this.action = action;
    }
  }        
