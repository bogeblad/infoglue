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
import org.infoglue.cms.entities.structure.ServiceBinding;
import org.infoglue.cms.entities.structure.ServiceBindingVO;

public class SmallServiceBindingImpl extends ServiceBindingImpl implements ServiceBinding
{
    private ServiceBindingVO valueObject = new ServiceBindingVO();
    private java.util.Collection<SmallQualifyerImpl> bindingQualifyers;
    private org.infoglue.cms.entities.management.impl.simple.ServiceDefinitionImpl serviceDefinition;
    //private org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl siteNodeVersion;
	private org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl availableServiceBinding;

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
		setValueObject((ServiceBindingVO) valueObject);
	}

    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getId()
	 */
	public Integer getId() 
	{
		return getServiceBindingId();
	}

	public Object getIdAsObject()
	{
		return getId();
	}

    public ServiceBindingVO getValueObject()
    {
        return this.valueObject;
    }

    public void setValueObject(ServiceBindingVO valueObject)
    {
        this.valueObject = valueObject;
    }

    public java.lang.Integer getServiceBindingId()
    {
        return this.valueObject.getServiceBindingId();
    }

    public void setServiceBindingId(java.lang.Integer serviceBindingId)
    {
        this.valueObject.setServiceBindingId(serviceBindingId);
    }

    public java.lang.String getName()
    {
        return this.valueObject.getName();
    }

    public void setName(java.lang.String name)
    {
        this.valueObject.setName(name);
    }

    public java.lang.String getPath()
    {
        return this.valueObject.getPath();
    }

    public void setPath(java.lang.String path)
    {
        this.valueObject.setPath(path);
    }

    public java.lang.Integer getBindingTypeId()
    {
        return this.valueObject.getBindingTypeId();
    }

    public void setBindingTypeId(java.lang.Integer bindingTypeId)
    {
        this.valueObject.setBindingTypeId(bindingTypeId);
    }

    public java.util.Collection getBindingQualifyers()
    {
        return this.bindingQualifyers;
    }

    public void setBindingQualifyers (java.util.Collection bindingQualifyers)
    {
        this.bindingQualifyers = bindingQualifyers;
    }

    public Integer getAvailableServiceBindingId()
    {
    	return this.valueObject.getAvailableServiceBindingId();
    }

    public void setAvailableServiceBindingId(Integer availableServiceBindingId)
    {
		this.valueObject.setAvailableServiceBindingId(availableServiceBindingId);
    }

    public org.infoglue.cms.entities.management.impl.simple.ServiceDefinitionImpl getServiceDefinition()
    {
        return this.serviceDefinition;
    }

    public void setServiceDefinition (org.infoglue.cms.entities.management.impl.simple.ServiceDefinitionImpl serviceDefinition)
    {
        this.serviceDefinition = serviceDefinition;
    }

    public org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl getAvailableServiceBinding()
    {
    	return this.availableServiceBinding;
    }

    public void setAvailableServiceBinding(org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl availableServiceBinding)
    {
    	this.availableServiceBinding = availableServiceBinding;
		this.valueObject.setAvailableServiceBindingId(availableServiceBinding.getAvailableServiceBindingId());
    }

    public Integer getSiteNodeVersionId()
    {
    	return this.valueObject.getSiteNodeVersionId();
    }

    public void setSiteNodeVersionId(Integer siteNodeVersionId)
	{
		this.valueObject.setSiteNodeVersionId(siteNodeVersionId);
	}
}
