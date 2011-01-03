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


import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Registry;
import org.infoglue.cms.entities.management.RegistryVO;


public class RegistryImpl implements Registry
{
    private RegistryVO valueObject = new RegistryVO();

	public String toString()
	{
		return this.valueObject.toString();
	}
	
	public Integer getId()
	{
		return this.getRegistryId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}
	     
    public RegistryVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(RegistryVO valueObject)
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
		setValueObject((RegistryVO) valueObject);
	}  
 
    public Integer getRegistryId()
    {
        return this.valueObject.getRegistryId();
    }

    public void setRegistryId(Integer registryId)
    {
        this.valueObject.setRegistryId(registryId);  
    }

    public String getEntityId()
    {
        return this.valueObject.getEntityId();
    }

    public void setEntityId(String entityId)
    {
        this.valueObject.setEntityId(entityId);
    }

    public String getEntityName()
    {
        return this.valueObject.getEntityName();
    }

    public void setEntityName(String entityName)
    {
        this.valueObject.setEntityName(entityName);
    }

    public Integer getReferenceType()
    {
        return this.valueObject.getReferenceType();
    }

    public void setReferenceType(Integer referenceType)
    {
        this.valueObject.setReferenceType(referenceType);
    }

    public String getReferencingEntityId()
    {
        return this.valueObject.getReferencingEntityId();
    }

    public void setReferencingEntityId(String referencingEntityId)
    {
        this.valueObject.setReferencingEntityId(referencingEntityId);
    }

    public String getReferencingEntityName()
    {
        return this.valueObject.getReferencingEntityName();
    }

    public void setReferencingEntityName(String referencingEntityName)
    {
        this.valueObject.setReferencingEntityName(referencingEntityName);
    }

    public String getReferencingEntityCompletingId()
    {
        return this.valueObject.getReferencingEntityCompletingId();
    }

    public void setReferencingEntityCompletingId(String referencingEntityCompletingId)
    {
        this.valueObject.setReferencingEntityCompletingId(referencingEntityCompletingId);
    }

    public String getReferencingEntityCompletingName()
    {
        return this.valueObject.getReferencingEntityCompletingName();
    }

    public void setReferencingEntityCompletingName(String referencingEntityCompletingName)
    {
        this.valueObject.setReferencingEntityCompletingName(referencingEntityCompletingName);
    }

}        
