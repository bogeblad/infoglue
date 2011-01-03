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

package org.infoglue.cms.entities.management.impl.simple;

import java.util.ArrayList;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.AvailableServiceBinding;
import org.infoglue.cms.entities.management.AvailableServiceBindingVO;
import org.infoglue.cms.exception.ConstraintException;

public class AvailableServiceBindingImpl implements AvailableServiceBinding
{
    private AvailableServiceBindingVO valueObject = new AvailableServiceBindingVO();
    private java.util.Collection siteNodeTypeDefinitions = new ArrayList();
    private java.util.Collection serviceDefinitions = new ArrayList();
    private java.util.Collection serviceBindings = new ArrayList();

	public Integer getId()
	{
		return this.getAvailableServiceBindingId();
	}

	public Object getIdAsObject()
	{
		return getId();
	}

	public String toString()
	{
		return this.valueObject.toString();
	}
     
    public AvailableServiceBindingVO getValueObject()
    {
        return this.valueObject;
    }

        
    public void setValueObject(AvailableServiceBindingVO valueObject)
    {
        this.valueObject = valueObject;
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
		setValueObject((AvailableServiceBindingVO) valueObject);
	}  
    
    public java.lang.Integer getAvailableServiceBindingId()
    {
        return this.valueObject.getAvailableServiceBindingId();
    }
            
    public void setAvailableServiceBindingId(java.lang.Integer availableServiceBindingId)
    {
        this.valueObject.setAvailableServiceBindingId(availableServiceBindingId);
    }
      
    public java.lang.String getName()
    {
        return this.valueObject.getName();
    }
            
    public void setName(java.lang.String name) throws ConstraintException
    {
        this.valueObject.setName(name);
    }
      
    public java.lang.String getDescription()
    {
        return this.valueObject.getDescription();
    }
            
    public void setDescription(java.lang.String description) throws ConstraintException
    {
        this.valueObject.setDescription(description);
    }

    public java.lang.String getVisualizationAction()
    {
        return this.valueObject.getVisualizationAction();
    }
            
    public void setVisualizationAction(java.lang.String visualizationAction) throws ConstraintException
    {
        this.valueObject.setVisualizationAction(visualizationAction);
    }
    
     
    public java.lang.Boolean getIsMandatory()
    {
        return this.valueObject.getIsMandatory();
    }
            
    public void setIsMandatory(java.lang.Boolean isMandatory) throws ConstraintException
    {
        this.valueObject.setIsMandatory(isMandatory);
    }
      
    public java.lang.Boolean getIsUserEditable()
    {
        return this.valueObject.getIsUserEditable();
    }
            
    public void setIsUserEditable(java.lang.Boolean isUserEditable) throws ConstraintException
    {
        this.valueObject.setIsUserEditable(isUserEditable);
    }

    public java.lang.Boolean getIsInheritable()
    {
        return this.valueObject.getIsInheritable();
    }
            
    public void setIsInheritable(java.lang.Boolean isInheritable) throws ConstraintException
    {
        this.valueObject.setIsInheritable(isInheritable);
    }
      
    public java.util.Collection getSiteNodeTypeDefinitions()
    {
        return this.siteNodeTypeDefinitions;
    }
            
    public void setSiteNodeTypeDefinitions (java.util.Collection siteNodeTypeDefinitions)
    {
        this.siteNodeTypeDefinitions = siteNodeTypeDefinitions;
    }
      
    public java.util.Collection getServiceDefinitions()
    {
        return this.serviceDefinitions;
    }
            
    public void setServiceDefinitions (java.util.Collection serviceDefinitions)
    {
        this.serviceDefinitions = serviceDefinitions;
    }
    
    public java.util.Collection getServiceBindings()
    {
        return this.serviceBindings;
    }
            
    public void setServiceBindings(java.util.Collection serviceBindings)
    {
        this.serviceBindings = serviceBindings;
    }
}        
