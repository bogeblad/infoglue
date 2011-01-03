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

package org.infoglue.cms.entities.workflow;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class ConsequenceDefinitionVO implements BaseEntityVO
{

  	private java.lang.Integer consequenceDefinitionId;
    private java.lang.String name;
    private java.lang.String description;
    private java.lang.String invokerClassName;
  
  
    public java.lang.Integer getConsequenceDefinitionId()
    {
        return this.consequenceDefinitionId;
    }
                
    public void setConsequenceDefinitionId(java.lang.Integer consequenceDefinitionId)
    {
        this.consequenceDefinitionId = consequenceDefinitionId;
    }
    
    public java.lang.String getName()
    {
        return this.name;
    }
                
    public void setName(java.lang.String name)
    {
        this.name = name;
    }
    
    public java.lang.String getDescription()
    {
        return this.description;
    }
                
    public void setDescription(java.lang.String description)
    {
        this.description = description;
    }
    
    public java.lang.String getInvokerClassName()
    {
        return this.invokerClassName;
    }
                
    public void setInvokerClassName(java.lang.String invokerClassName)
    {
        this.invokerClassName = invokerClassName;
    }
  
  
    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	public Integer getId() 
	{
		return getConsequenceDefinitionId();
	}
	
	        
  	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() 
	{
    	ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		return ceb;
	}    
}
        
