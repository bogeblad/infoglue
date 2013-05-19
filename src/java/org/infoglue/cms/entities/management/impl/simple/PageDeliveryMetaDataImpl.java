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
import org.infoglue.cms.entities.management.PageDeliveryMetaData;
import org.infoglue.cms.entities.management.PageDeliveryMetaDataVO;

public class PageDeliveryMetaDataImpl implements PageDeliveryMetaData
{
    private PageDeliveryMetaDataVO valueObject = new PageDeliveryMetaDataVO();
    private Collection entities = null;
    
	public Integer getId()
	{
		return this.getPageDeliveryMetaDataId();
	}

	public void setPageDeliveryMetaDataId(Integer pageDeliveryMetaDataId) 
	{
		this.valueObject.setPageDeliveryMetaDataId(pageDeliveryMetaDataId);
	}

	public Integer getPageDeliveryMetaDataId() 
	{
		return this.valueObject.getPageDeliveryMetaDataId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}

	public String toString()
	{
		return this.valueObject.toString();
	}

    public PageDeliveryMetaDataVO getValueObject()
    {
        return this.valueObject;
    }

        
    public void setValueObject(PageDeliveryMetaDataVO valueObject)
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
		setValueObject((PageDeliveryMetaDataVO) valueObject);
	}
    
    public java.lang.Integer getSiteNodeId()
    {
        return this.valueObject.getSiteNodeId();
    }
            
    public void setSiteNodeId(java.lang.Integer siteNodeId)
    {
        this.valueObject.setSiteNodeId(siteNodeId);
    }

    public java.lang.Integer getLanguageId()
    {
        return this.valueObject.getLanguageId();
    }
            
    public void setLanguageId(java.lang.Integer languageId)
    {
        this.valueObject.setLanguageId(languageId);
    }

    public java.lang.Integer getContentId()
    {
        return this.valueObject.getContentId();
    }
            
    public void setContentId(java.lang.Integer contentId)
    {
        this.valueObject.setContentId(contentId);
    }
    
    public java.util.Date getLastModifiedDateTime()
    {
        return this.valueObject.getLastModifiedDateTime();
    }
            
    public void setLastModifiedDateTime(java.util.Date lastModifiedDateTime)
    {
        this.valueObject.setLastModifiedDateTime(lastModifiedDateTime);
    }
    
    public java.lang.Integer getLastModifiedTimeout()
    {
        return this.valueObject.getLastModifiedTimeout();
    }
            
    public void setLastModifiedTimeout(java.lang.Integer lastModifiedTimeout)
    {
        this.valueObject.setLastModifiedTimeout(lastModifiedTimeout);
    }
    
    public java.lang.Boolean getSelectiveCacheUpdateNotApplicable()
    {
    	return this.valueObject.getSelectiveCacheUpdateNotApplicable();
	}
    
    public void setSelectiveCacheUpdateNotApplicable(java.lang.Boolean selectiveCacheUpdateNotApplicable)
	{
		this.valueObject.setSelectiveCacheUpdateNotApplicable(selectiveCacheUpdateNotApplicable);
	}

	public String getUsedEntities() 
	{
		return this.valueObject.getUsedEntities();
	}
	
	public void setUsedEntities(String usedEntities) 
	{
		this.valueObject.setUsedEntities(usedEntities);
	}

	public Collection getEntities() 
	{
		return entities;
	}

	public void setEntities(Collection entities) 
	{
		this.entities = entities;
	}

}        
