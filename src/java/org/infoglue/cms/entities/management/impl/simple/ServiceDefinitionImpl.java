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

import java.util.Collection;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ServiceDefinition;
import org.infoglue.cms.entities.management.ServiceDefinitionVO;
import org.infoglue.cms.exception.ConstraintException;

public class ServiceDefinitionImpl implements ServiceDefinition
{
    private ServiceDefinitionVO valueObject = new ServiceDefinitionVO();
    private Collection availableServiceBindnings = null;
    //private Collection serviceBindings = null;
     
	public Integer getId()
	{
		return this.getServiceDefinitionId();
	}

	public Object getIdAsObject()
	{
		return getId();
	}

	public String toString()
	{
		return this.valueObject.toString();
	}

    public ServiceDefinitionVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(ServiceDefinitionVO valueObject)
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
		setValueObject((ServiceDefinitionVO) valueObject);
	}
        
    public java.lang.Integer getServiceDefinitionId()
    {
        return this.valueObject.getServiceDefinitionId();
    }
            
    public void setServiceDefinitionId(java.lang.Integer serviceDefinitionId)
    {
        this.valueObject.setServiceDefinitionId(serviceDefinitionId);
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

    public java.lang.String getClassName()
    {
        return this.valueObject.getClassName();
    }
            
    public void setClassName(java.lang.String className) throws ConstraintException
    {
        this.valueObject.setClassName(className);
    }

    public java.util.Collection getAvailableServiceBindings()
    {
    	return this.availableServiceBindnings;
    }
    
    public void setAvailableServiceBindings(java.util.Collection availableServiceBindings)
    {
    	this.availableServiceBindnings = availableServiceBindings;
    }
    
    /*
    public java.util.Collection getServiceBindings()
    {
        return this.serviceBindings;
    }
            
    public void setServiceBindings(java.util.Collection serviceBindings)
    {
        this.serviceBindings = serviceBindings;
    }
    */
}        
