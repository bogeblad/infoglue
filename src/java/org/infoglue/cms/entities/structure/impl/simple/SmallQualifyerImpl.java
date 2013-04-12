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

package org.infoglue.cms.entities.structure.impl.simple;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.kernel.IBaseEntity;
import org.infoglue.cms.entities.structure.Qualifyer;
import org.infoglue.cms.entities.structure.QualifyerVO;

public class SmallQualifyerImpl implements IBaseEntity
{
    private QualifyerVO valueObject = new QualifyerVO();
     
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
		setValueObject((QualifyerVO) valueObject);
	}
 
    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getId()
	 */
	public Integer getId() 
	{
		return getQualifyerId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}
 
    public QualifyerVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(QualifyerVO valueObject)
    {
        this.valueObject = valueObject;
    }   

    private org.infoglue.cms.entities.structure.impl.simple.SmallServiceBindingImpl serviceBinding;
      
    public java.lang.Integer getQualifyerId()
    {
        return this.valueObject.getQualifyerId();
    }
            
    public void setQualifyerId(java.lang.Integer qualifyerId)
    {
        this.valueObject.setQualifyerId(qualifyerId);
    }
      
    public java.lang.String getName()
    {
        return this.valueObject.getName();
    }
            
    public void setName(java.lang.String name)
    {
        this.valueObject.setName(name);
    }
      
    public java.lang.String getValue()
    {
        return this.valueObject.getValue();
    }
            
    public void setValue(java.lang.String value)
    {
        this.valueObject.setValue(value);
    }

    public java.lang.Integer getSortOrder()
    {
    	return this.valueObject.getSortOrder();
    }
    
    public void setSortOrder(java.lang.Integer sortOrder)
    {
    	this.valueObject.setSortOrder(sortOrder);
    }

      
    public org.infoglue.cms.entities.structure.impl.simple.SmallServiceBindingImpl getServiceBinding()
    {
        return this.serviceBinding;
    }
            
    public void setServiceBinding (org.infoglue.cms.entities.structure.impl.simple.SmallServiceBindingImpl serviceBinding)
    {
        this.serviceBinding = serviceBinding;
    }
  }        
