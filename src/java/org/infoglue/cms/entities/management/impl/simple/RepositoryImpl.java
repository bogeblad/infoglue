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
import java.util.Collection;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;


public class RepositoryImpl implements Repository
{
    private RepositoryVO valueObject = new RepositoryVO();
    private Collection availableContenTypeDefinition;
    private Collection contents;
    private Collection repositoryLanguages = new ArrayList();

	public String toString()
	{
		return this.valueObject.toString();
	}
	
	public Integer getId()
	{
		return this.getRepositoryId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}
	     
    public RepositoryVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(RepositoryVO valueObject)
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
		setValueObject((RepositoryVO) valueObject);
	}  
    
    public java.lang.Integer getRepositoryId()
    {
        return this.valueObject.getRepositoryId();
    }
            
    public void setRepositoryId(java.lang.Integer repositoryId) throws SystemException
    {
        this.valueObject.setRepositoryId(repositoryId);
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
    
    public java.lang.String getDnsName()
    {
        return this.valueObject.getDnsName();
    }
    
    public void setDnsName(java.lang.String dnsName) throws ConstraintException
	{
        this.valueObject.setDnsName(dnsName);
    }

    public Boolean getIsDeleted()
    {
    	return this.valueObject.getIsDeleted();
	}
    
    public void setIsDeleted(Boolean isDeleted)
	{
    	this.valueObject.setIsDeleted(isDeleted);
	}

    public java.util.Collection getAvailableContenTypeDefinition()
    {
        return this.availableContenTypeDefinition;
    }
            
    public void setAvailableContenTypeDefinition (java.util.Collection availableContenTypeDefinition)
    {
        this.availableContenTypeDefinition = availableContenTypeDefinition;
    }
      
    public java.util.Collection getContents()
    {
        return this.contents;
    }
            
    public void setContents (java.util.Collection contents)
    {
        this.contents = contents;
    }
      
    public java.util.Collection getRepositoryLanguages()
    {
        return this.repositoryLanguages;
    }
            
    public void setRepositoryLanguages (java.util.Collection repositoryLanguages)
    {
        this.repositoryLanguages = repositoryLanguages;
    }
}        
