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

package org.infoglue.cms.entities.structure;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class ServiceBindingVO implements BaseEntityVO
{

	private java.lang.Integer serviceBindingId			= null;;	
    private java.lang.String name 						= "Undefined";
    private java.lang.String path 						= "Undefined";
    private java.lang.Integer bindingTypeId				= new Integer(0);
    private java.lang.Integer availableServiceBindingId	= null;  
    private java.lang.Integer serviceDefinitionId		= null;  
    private java.lang.Integer siteNodeVersionId			= null;  
  
    public java.lang.Integer getServiceBindingId()
    {
        return this.serviceBindingId;
    }
                
    public void setServiceBindingId(java.lang.Integer serviceBindingId)
    {
        this.serviceBindingId = serviceBindingId;
    }
    
    public java.lang.String getName()
    {
        return this.name;
    }
                
    public void setName(java.lang.String name)
    {
        this.name = name;
    }

    public java.lang.String getPath()
    {
        return this.path;
    }
                
    public void setPath(java.lang.String path)
    {
        this.path = path;
    }
    
    public java.lang.Integer getBindingTypeId()
    {
        return this.bindingTypeId;
    }
                
    public void setBindingTypeId(java.lang.Integer bindingTypeId)
    {
        this.bindingTypeId = bindingTypeId;
    }

    public java.lang.Integer getAvailableServiceBindingId()
    {
        return this.availableServiceBindingId;
    }
                
    public void setAvailableServiceBindingId(java.lang.Integer availableServiceBindingId)
    {
        this.availableServiceBindingId = availableServiceBindingId;
    }
 
 	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	public Integer getId() 
	{
		return getServiceBindingId();
	}
	
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() 
	{ 
		return null;
	}

	public java.lang.Integer getServiceDefinitionId()
	{
		return serviceDefinitionId;
	}

	public void setServiceDefinitionId(java.lang.Integer serviceDefinitionId)
	{
		this.serviceDefinitionId = serviceDefinitionId;
	}

	public java.lang.Integer getSiteNodeVersionId()
	{
		return siteNodeVersionId;
	}

	public void setSiteNodeVersionId(java.lang.Integer siteNodeVersionId)
	{
		this.siteNodeVersionId = siteNodeVersionId;
	}     
}
        
