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

package org.infoglue.cms.entities.content.impl.simple;

import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;

public class ContentImpl implements Content
{
    private ContentVO valueObject = new ContentVO();
     
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
		setValueObject((ContentVO) valueObject);
	}
 
    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getId()
	 */
	public Integer getId() 
	{
		return getContentId();
	}

	public Object getIdAsObject()
	{
		return getId();
	}

    public ContentVO getValueObject()
    {
        return this.valueObject;
    }

        
    public void setValueObject(ContentVO valueObject)
    {
        this.valueObject = valueObject;
    }   

    private org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl contentTypeDefinition;
    private java.util.Collection children = new java.util.ArrayList();
    private org.infoglue.cms.entities.content.impl.simple.ContentImpl parentContent;
    private java.util.Collection contentVersions = new java.util.ArrayList();
    private org.infoglue.cms.entities.management.impl.simple.RepositoryImpl repository;
    private java.util.Collection relatedContents = new java.util.ArrayList();
    private java.util.Collection relatedByContents = new java.util.ArrayList();

    public java.lang.Integer getContentId()
    {
        return this.valueObject.getContentId();
    }
            
    public void setContentId(java.lang.Integer contentId)
    {
        this.valueObject.setContentId(contentId);
    }
      
    public java.lang.String getName()
    {
        return this.valueObject.getName();
    }
            
    public void setName(java.lang.String name)
    {
        this.valueObject.setName(name);
    }
      
    public java.util.Date getPublishDateTime()
    {
        return this.valueObject.getPublishDateTime();
    }
            
    public void setPublishDateTime(java.util.Date publishDateTime)
    {
        this.valueObject.setPublishDateTime(publishDateTime);
    }
      
    public java.util.Date getExpireDateTime()
    {
        return this.valueObject.getExpireDateTime();
    }
            
    public void setExpireDateTime(java.util.Date expireDateTime)
    {
        this.valueObject.setExpireDateTime(expireDateTime);
    }
      
    public java.lang.Boolean getIsBranch()
    {
    	return this.valueObject.getIsBranch();
	}
    
    public void setIsBranch(java.lang.Boolean isBranch)
	{
		this.valueObject.setIsBranch(isBranch);
	}

  
    public org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl getContentTypeDefinition()
    {
        return this.contentTypeDefinition;
    }

    public void setContentTypeDefinition (org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl contentTypeDefinition)
    {
        this.contentTypeDefinition = contentTypeDefinition;
        if(contentTypeDefinition != null)
            this.valueObject.setContentTypeDefinitionId(contentTypeDefinition.getId());
    }
     
    public Integer getContentTypeDefinitionId()
    {
    	if(this.contentTypeDefinition != null)
    		return this.contentTypeDefinition.getId();
    	else if(this.getValueObject().getContentTypeDefinitionId() != null)
    		return this.getValueObject().getContentTypeDefinitionId();
    	else
    		return null;
    }

    public void setContentTypeDefinitionId(Integer contentTypeDefinitionId)
    {
    	this.getValueObject().setContentTypeDefinitionId(contentTypeDefinitionId);
    }

    public java.util.Collection getChildren()
    {
        return this.children;
    }
            
    public void setChildren (java.util.Collection children)
    {
        this.children = children;
        if (this.getChildCount()!= null) 
        {
        	if(this.getChildCount().intValue() == 0)
		        this.setChildCount(new Integer(children.size()) );
        }
        else
	        this.setChildCount(new Integer(children.size()) );
    }
      
    public org.infoglue.cms.entities.content.impl.simple.ContentImpl getParentContent()
    {
        return this.parentContent;
    }
     
           
    public void setParentContent (org.infoglue.cms.entities.content.impl.simple.ContentImpl parentContent)
    {
        this.parentContent = parentContent;
        
        if(parentContent != null)
        	this.valueObject.setParentContentId(parentContent.getId());
    }
      
    public java.util.Collection getContentVersions()
    {
        return this.contentVersions;
    }
            
    public void setContentVersions (java.util.Collection contentVersions)
    {
        this.contentVersions = contentVersions;
        if (this.getChildCount()!= null) 
        {
        	if(this.getChildCount().intValue() == 0)
	        	this.setChildCount(new Integer(contentVersions.size()) );
        }
        else
        	this.setChildCount(new Integer(contentVersions.size()) );
    }
      
    public java.lang.String getCreator()
    {
        return this.valueObject.getCreatorName();
    }
            
    public void setCreator (java.lang.String creator)
    {
        this.valueObject.setCreatorName(creator);
    }
      
    public org.infoglue.cms.entities.management.impl.simple.RepositoryImpl getRepository()
    {
        return this.repository;
    }
            
    public void setRepository (org.infoglue.cms.entities.management.impl.simple.RepositoryImpl repository)
    {
        this.repository = repository;
        this.valueObject.setRepositoryId(repository.getRepositoryId());
    }

    public Integer getRepositoryId()
    {
    	if(this.repository != null)
    		return this.repository.getId();
    	else if(this.getValueObject().getRepositoryId() != null)
    		return this.getValueObject().getRepositoryId();
    	else
    		return null;
    }
    
    public void setRepositoryId(Integer repositoryId)
    {
    	this.getValueObject().setRepositoryId(repositoryId);
    }


    public java.util.Collection getRelatedContents()
    {
        return this.relatedContents;
    }
            
    public void setRelatedContents (java.util.Collection relatedContents)
    {
        this.relatedContents = relatedContents;
    }
      
    public java.util.Collection getRelatedByContents()
    {
        return this.relatedByContents;
    }
            
    public void setRelatedByContents (java.util.Collection relatedByContents)
    {
        this.relatedByContents = relatedByContents;
    }
	/**
	 * Returns the childCount.
	 * @return Integer
	 */
	public Integer getChildCount()
	{
		return this.valueObject.getChildCount();
	}

	/**
	 * Sets the childCount.
	 * @param childCount The childCount to set
	 */
	public void setChildCount(Integer childCount)
	{
		this.valueObject.setChildCount(childCount);
	}

	public void setIsProtected(Integer isProtected)
	{
		this.valueObject.setIsProtected(isProtected);
	}

	public Integer getIsProtected()
	{
		return this.valueObject.getIsProtected();
	}
	
    public Boolean getIsDeleted()
    {
    	return this.valueObject.getIsDeleted();
	}
    
    public void setIsDeleted(Boolean isDeleted)
	{
    	this.valueObject.setIsDeleted(isDeleted);
	}
	
	/*
	public String toString()
	{
	    return "Content: " + this.getName() + "[" + this.getId() + "]" + this.getCreator() + this.getContentTypeDefinition() + this.getExpireDateTime() + this.getIsBranch() + this.getIsBranch() + this.getIsProtected() + this.getPublishDateTime();
	}
	*/
}        
