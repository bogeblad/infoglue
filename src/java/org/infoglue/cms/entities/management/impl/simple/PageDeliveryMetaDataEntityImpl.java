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
import org.infoglue.cms.entities.management.PageDeliveryMetaData;
import org.infoglue.cms.entities.management.PageDeliveryMetaDataEntity;
import org.infoglue.cms.entities.management.PageDeliveryMetaDataEntityVO;
import org.infoglue.cms.entities.management.PageDeliveryMetaDataVO;

public class PageDeliveryMetaDataEntityImpl implements PageDeliveryMetaDataEntity
{
    private PageDeliveryMetaDataEntityVO valueObject = new PageDeliveryMetaDataEntityVO();
    private PageDeliveryMetaData pageDeliveryMetaData;
    
	public Integer getId()
	{
		return this.getPageDeliveryMetaDataEntityId();
	}

	public Integer getPageDeliveryMetaDataEntityId() 
	{
		return this.valueObject.getPageDeliveryMetaDataEntityId();
	}

	public void setPageDeliveryMetaDataEntityId(Integer pageDeliveryMetaDataEntityId) 
	{
		this.valueObject.setPageDeliveryMetaDataEntityId(pageDeliveryMetaDataEntityId);
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}

	public String toString()
	{
		return this.valueObject.toString();
	}

    public PageDeliveryMetaDataEntityVO getValueObject()
    {
        return this.valueObject;
    }

        
    public void setValueObject(PageDeliveryMetaDataEntityVO valueObject)
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
		setValueObject((PageDeliveryMetaDataEntityVO) valueObject);
	}
    
    public java.lang.Integer getSiteNodeId()
    {
        return this.valueObject.getSiteNodeId();
    }
            
    public void setSiteNodeId(java.lang.Integer siteNodeId)
    {
        this.valueObject.setSiteNodeId(siteNodeId);
    }

    public java.lang.Integer getContentId()
    {
        return this.valueObject.getContentId();
    }
            
    public void setContentId(java.lang.Integer contentId)
    {
        this.valueObject.setContentId(contentId);
    }

	public Integer getPageDeliveryMetaDataId() 
	{
		return this.valueObject.getPageDeliveryMetaDataId();
	}

	public void setPageDeliveryMetaDataId(Integer pageDeliveryMetaDataId) 
	{
		this.valueObject.setPageDeliveryMetaDataId(pageDeliveryMetaDataId);
	}

	public PageDeliveryMetaData getPageDeliveryMetaData() 
	{
		return this.pageDeliveryMetaData;
	}

	public void setPageDeliveryMetaData(PageDeliveryMetaData pageDeliveryMetaData) 
	{
		this.pageDeliveryMetaData = pageDeliveryMetaData;
	}
}        
