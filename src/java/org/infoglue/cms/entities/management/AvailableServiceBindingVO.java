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

package org.infoglue.cms.entities.management;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.validators.ValidatorFactory;

public class AvailableServiceBindingVO implements BaseEntityVO
{

    private java.lang.Integer availableServiceBindingId;
    private java.lang.String name;
    private java.lang.String description;
    private java.lang.String visualizationAction;
    private java.lang.Boolean isMandatory;
    private java.lang.Boolean isUserEditable;
    private java.lang.Boolean isInheritable;

	public String toString()
	{  
		return getName();
	}
   
  	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	public Integer getId() {
		return getAvailableServiceBindingId();
	}
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#setId(Integer)
	 */
	public void setId(Integer id) {
		setAvailableServiceBindingId(id);
	}

   
    public java.lang.Integer getAvailableServiceBindingId()
    {
        return this.availableServiceBindingId;
    }
                
    public void setAvailableServiceBindingId(java.lang.Integer availableServiceBindingId)
    {
        this.availableServiceBindingId = availableServiceBindingId;
    }
    
    public java.lang.String getName()
    {
        return this.name;
    }
                
    public void setName(java.lang.String name) throws ConstraintException
    {
 		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 		
    	ValidatorFactory.createStringValidator("AvailableServiceBinding.name", true, 6, 30).validate(name, ceb);
 

 		ceb.throwIfNotEmpty();		
        this.name = name;
    }
    
    public java.lang.String getDescription()
    {
        return this.description;
    }
                
    public void setDescription(java.lang.String description) throws ConstraintException
    {
 		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
    	ValidatorFactory.createStringValidator("AvailableServiceBinding.description", true, 6, 255).validate(description, ceb); 
 

 		ceb.throwIfNotEmpty();		
        this.description = description;
    }
    
    public java.lang.String getVisualizationAction()
    {
        return this.visualizationAction;
    }
            
    public void setVisualizationAction(java.lang.String visualizationAction) throws ConstraintException
    {
        this.visualizationAction = visualizationAction;
    }
    
    public java.lang.Boolean getIsMandatory()
    {
        return this.isMandatory;
    }
                
    public void setIsMandatory(java.lang.Boolean isMandatory) throws ConstraintException
    {
        this.isMandatory = isMandatory;
    }
    
    public java.lang.Boolean getIsUserEditable()
    {
        return this.isUserEditable;
    }
                
    public void setIsUserEditable(java.lang.Boolean isUserEditable) throws ConstraintException
    {
        this.isUserEditable = isUserEditable;
    }

    public java.lang.Boolean getIsInheritable()
    {
        return this.isInheritable;
    }
                
    public void setIsInheritable(java.lang.Boolean isInheritable) throws ConstraintException
    {
        this.isInheritable = isInheritable;
    }

	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() {
    	
    	ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
    	if (name != null) ValidatorFactory.createStringValidator("AvailableServiceBinding.name", true, 6, 30, true, AvailableServiceBindingImpl.class, this.getId(), null).validate(name, ceb);
		return ceb;
	}
      
}
        
