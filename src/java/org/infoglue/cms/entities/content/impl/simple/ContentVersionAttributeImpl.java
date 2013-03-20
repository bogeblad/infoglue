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

import org.infoglue.cms.entities.content.ContentVersionAttribute;
import org.infoglue.cms.entities.content.ContentVersionAttributeVO;
import org.infoglue.cms.entities.content.SmallestContentVersion;
import org.infoglue.cms.entities.content.SmallestContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;

public class ContentVersionAttributeImpl implements ContentVersionAttribute
{
    private ContentVersionAttributeVO valueObject = new ContentVersionAttributeVO();
     
     
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
		setValueObject((ContentVersionAttributeVO) valueObject);
	}
 
    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getId()
	 */
	public Integer getId() 
	{
		return getContentVersionAttributeId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}

    public ContentVersionAttributeVO getValueObject()
    {
        return this.valueObject;
    }

        
    public void setValueObject(ContentVersionAttributeVO valueObject)
    {
        this.valueObject = valueObject;
    }   
	
    public java.lang.Integer getContentVersionAttributeId()
    {
        return this.valueObject.getContentVersionAttributeId();
    }
            
    public void setContentVersionAttributeId(java.lang.Integer contentVersionAttributeId)
    {
        this.valueObject.setContentVersionAttributeId(contentVersionAttributeId);
    }
      
    public java.lang.Integer getContentVersionId()
    {
        return this.valueObject.getContentVersionId();
    }
            
    public void setContentVersionId(java.lang.Integer contentVersionId)
    {
        this.valueObject.setContentVersionId(contentVersionId);
    }
      
    public java.lang.String getAttributeName()
    {
        return this.valueObject.getAttributeName();
    }
            
    public void setAttributeName(java.lang.String attributeName)
    {
        this.valueObject.setAttributeName(attributeName);
    }
      
    public java.lang.String getAttributeValue()
    {
        return this.valueObject.getAttributeValue();
    }
            
    public void setAttributeValue(java.lang.String attributeValue)
    {
        this.valueObject.setAttributeValue(attributeValue);
    }
    	
}        
