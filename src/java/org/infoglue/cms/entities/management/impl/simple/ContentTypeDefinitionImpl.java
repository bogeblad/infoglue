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
import org.infoglue.cms.entities.management.ContentTypeDefinition;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.exception.ConstraintException;

public class ContentTypeDefinitionImpl implements ContentTypeDefinition
{
    private ContentTypeDefinitionVO valueObject = new ContentTypeDefinitionVO();
    //private Collection contents;
    
    private ContentTypeDefinition parent;
    private Collection<ContentTypeDefinition> children;
    
    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getVO()
	 */
	public BaseEntityVO getVO() {
		return (BaseEntityVO) getValueObject();
	}

	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#setVO(BaseEntityVO)
	 */
	public void setVO(BaseEntityVO valueObject) {
		setValueObject((ContentTypeDefinitionVO) valueObject);
	}
 
    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getId()
	 */
	public Integer getId() {
		return getContentTypeDefinitionId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}

	 
    public ContentTypeDefinitionVO getValueObject()
    {
        return this.valueObject;
    }

        
    public void setValueObject(ContentTypeDefinitionVO valueObject)
    {
        this.valueObject = valueObject;
    }   

    
    
    public java.lang.Integer getContentTypeDefinitionId()
    {
        return this.valueObject.getContentTypeDefinitionId();
    }
            
    public void setContentTypeDefinitionId(java.lang.Integer contentTypeDefinitionId)
    {
        this.valueObject.setContentTypeDefinitionId(contentTypeDefinitionId);
    }

    public java.lang.String getName()
    {
        return this.valueObject.getName();
    }
            
    public void setName(java.lang.String name)
    {
        this.valueObject.setName(name);
    }
      
    public java.lang.String getSchemaValue()
    {
        return this.valueObject.getSchemaValue();
    }
            
    public void setSchemaValue(java.lang.String schemaValue)
    {
        this.valueObject.setSchemaValue(schemaValue);
    }

	public Integer getType()
	{
		return this.valueObject.getType();
	}

	public void setType(Integer type) throws ConstraintException
	{
		this.valueObject.setType(type);
	}
	
	public String getDetailPageResolverClass()
	{
		return this.valueObject.getDetailPageResolverClass();
	}
    
    public void setDetailPageResolverClass(String detailPageResolverClass) throws ConstraintException
    {
    	this.valueObject.setDetailPageResolverClass(detailPageResolverClass);
	}
    
    public String getDetailPageResolverData()
    {
		return this.valueObject.getDetailPageResolverData();
	}
    
    public void setDetailPageResolverData(String detailPageResolverData) throws ConstraintException
    {
    	this.valueObject.setDetailPageResolverData(detailPageResolverData);
	}

    
    public ContentTypeDefinition getParent()
    {
    	return this.parent;
    }
    
    public void setParent(ContentTypeDefinition parent)    
    {
    	if(parent != null)
    	{
    		this.valueObject.setParentId(parent.getId());
    		this.valueObject.setParentName(parent.getName());
    	}
    	else
    	{
    		this.valueObject.setParentId(-1);
    		this.valueObject.setParentName(null);
    	}
    	
    	this.parent = parent;
    }

    public Collection<ContentTypeDefinition> getChildren()
    {
    	return this.children;
    }
    
    public void setChildren(Collection<ContentTypeDefinition> children)
    {
    	this.children = children;
    }

	/*
	public Collection getContents()
	{
		return this.contents;
	}
	
	public void setContents(Collection contents)
	{
		this.contents = contents;
	}
	*/
}        
