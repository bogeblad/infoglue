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

package org.infoglue.cms.entities.publishing.impl.simple;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.publishing.Publication;
import org.infoglue.cms.entities.publishing.PublicationVO;

public class PublicationImpl implements Publication
{
	
	/**
	 * @see org.infoglue.cms.entities.publishing.Publication#setRepositoryId(Integer)
	 */
	
	public void setRepositoryId(Integer repositoryId)
	{
		this.valueObject.setRepositoryId(repositoryId);
	}
	
	/**
	 * @see org.infoglue.cms.entities.publishing.Publication#getRepositoryId()
	 */
	
	public Integer getRepositoryId()
	{
		return this.valueObject.getRepositoryId();
	}

	public Object getIdAsObject()
	{
		return getId();
	}

	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getId()
	 */
	public Integer getId()
	{
		return getPublicationId();
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
		setValueObject((PublicationVO) valueObject);
	}

	
    private PublicationVO valueObject = new PublicationVO();
     
    public PublicationVO getValueObject()
    {
        return this.valueObject;
    }

        
    public void setValueObject(PublicationVO valueObject)
    {
        this.valueObject = valueObject;
    }   
	
	private java.util.Collection publicationDetails;
    private java.util.Collection contentVersionsToPublish;
    private java.util.Collection siteNodeVersionsToPublish;
  
    
    public java.lang.Integer getPublicationId()
    {
        return this.valueObject.getPublicationId();
    }
            
    public void setPublicationId(java.lang.Integer publicationId)
    {
        this.valueObject.setPublicationId(publicationId);
    }
      
    public java.lang.String getName()
    {
        return this.valueObject.getName();
    }
            
    public void setName(java.lang.String name)
    {
        this.valueObject.setName(name);
    }
      
    public java.lang.String getDescription()
    {
        return this.valueObject.getDescription();
    }
            
    public void setDescription(java.lang.String description)
    {
        this.valueObject.setDescription(description);
    }
      
    public java.util.Date getPublicationDateTime()
    {
        return this.valueObject.getPublicationDateTime();
    }
            
    public void setPublicationDateTime(java.util.Date publicationDateTime)
    {
        this.valueObject.setPublicationDateTime(publicationDateTime);
    }
      
    public java.util.Collection getPublicationDetails()
    {
    	return this.publicationDetails;
	}
    
    public void setPublicationDetails(java.util.Collection publicationDetails)
    {
    	this.publicationDetails = publicationDetails;
    }
    
    public java.util.Collection getContentVersionsToPublish()
    {
        return this.contentVersionsToPublish;
    }
            
    public void setContentVersionsToPublish (java.util.Collection contentVersionsToPublish)
    {
        this.contentVersionsToPublish = contentVersionsToPublish;
    }
      
    public java.util.Collection getSiteNodeVersionsToPublish()
    {
        return this.siteNodeVersionsToPublish;
    }
            
    public void setSiteNodeVersionsToPublish (java.util.Collection siteNodeVersionsToPublish)
    {
        this.siteNodeVersionsToPublish = siteNodeVersionsToPublish;
    }
      
    public String getPublisher()
    {
        return this.valueObject.getPublisher();
    }
            
    public void setPublisher (String publisher)
    {
        this.valueObject.setPublisher(publisher);
    }
  }        
