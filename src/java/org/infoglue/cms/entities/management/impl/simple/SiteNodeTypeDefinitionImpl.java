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
import org.infoglue.cms.entities.management.SiteNodeTypeDefinition;
import org.infoglue.cms.entities.management.SiteNodeTypeDefinitionVO;
import org.infoglue.cms.exception.ConstraintException;

public class SiteNodeTypeDefinitionImpl implements SiteNodeTypeDefinition
{
    private SiteNodeTypeDefinitionVO valueObject = new SiteNodeTypeDefinitionVO();
    private java.util.Collection availableServiceBindings = new ArrayList();
    
	public Integer getId()
	{
		return this.getSiteNodeTypeDefinitionId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}
     
	public String toString()
	{
		return this.valueObject.toString();
	}

    public SiteNodeTypeDefinitionVO getValueObject()
    {
        return this.valueObject;
    }

        
    public void setValueObject(SiteNodeTypeDefinitionVO valueObject)
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
		setValueObject((SiteNodeTypeDefinitionVO) valueObject);
	}

    public java.lang.Integer getSiteNodeTypeDefinitionId()
    {
        return this.valueObject.getSiteNodeTypeDefinitionId();
    }
            
    public void setSiteNodeTypeDefinitionId(java.lang.Integer siteNodeTypeDefinitionId)
    {
        this.valueObject.setSiteNodeTypeDefinitionId(siteNodeTypeDefinitionId);
    }
      
    public java.lang.String getInvokerClassName()
    {
        return this.valueObject.getInvokerClassName();
    }
            
    public void setInvokerClassName(java.lang.String invokerClassName) throws ConstraintException
    {
        this.valueObject.setInvokerClassName(invokerClassName);
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
      
    public java.util.Collection getAvailableServiceBindings()
    {
        return this.availableServiceBindings;
    }
            
    public void setAvailableServiceBindings (java.util.Collection availableServiceBindings)
    {
        this.availableServiceBindings = availableServiceBindings;
    }
  }        
