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

import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;

public class SmallContentVersionImpl extends ContentVersionImpl
{
    private ContentVersionVO valueObject = new ContentVersionVO();
     
     
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
		setValueObject((ContentVersionVO) valueObject);
	}
 
    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getId()
	 */
	public Integer getId() 
	{
		return getContentVersionId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}

    public ContentVersionVO getValueObject()
    {
        return this.valueObject;
    }

        
    public void setValueObject(ContentVersionVO valueObject)
    {
        this.valueObject = valueObject;
    }   
	
    public java.lang.Integer getContentVersionId()
    {
        return this.valueObject.getContentVersionId();
    }
            
    public void setContentVersionId(java.lang.Integer contentVersionId)
    {
        this.valueObject.setContentVersionId(contentVersionId);
    }
      
    public java.lang.Integer getStateId()
    {
        return this.valueObject.getStateId();
    }
            
    public void setStateId(java.lang.Integer stateId)
    {
        this.valueObject.setStateId(stateId);
    }
            
    public java.util.Date getModifiedDateTime()
    {
        return this.valueObject.getModifiedDateTime();
    }
            
    public void setModifiedDateTime(java.util.Date modifiedDateTime)
    {
        this.valueObject.setModifiedDateTime(modifiedDateTime);
    }
      
    public java.lang.String getVersionComment()
    {
        return this.valueObject.getVersionComment();
    }
            
    public void setVersionComment(java.lang.String versionComment)
    {
        this.valueObject.setVersionComment(versionComment);
    }
      
    public java.lang.Boolean getIsCheckedOut()
    {
        return this.valueObject.getIsCheckedOut();
    }
            
    public void setIsCheckedOut(java.lang.Boolean isCheckedOut)
    {
        this.valueObject.setIsCheckedOut(isCheckedOut);
    }
      
    public java.lang.Boolean getIsActive()
    {
    	return this.valueObject.getIsActive();
	}
    
    public void setIsActive(java.lang.Boolean isActive)
	{
		this.valueObject.setIsActive(isActive);
	}
/*
    public java.lang.Boolean getIsPublishedVersion()
    {
    	return this.valueObject.getIsPublishedVersion();
	}
    
    public void setIsPublishedVersion(java.lang.Boolean isPublishedVersion)
	{
		this.valueObject.setIsPublishedVersion(isPublishedVersion);
	}
*/	
    
    public Integer getContentId()
    {
        return this.valueObject.getContentId();
    }
            
    public void setContentId(Integer contentId)
    {
        this.valueObject.setContentId(contentId);
    }

    public Integer getLanguageId()
    {
        return this.valueObject.getLanguageId();
    }
            
    public void setLanguageId(Integer languageId)
    {
        this.valueObject.setLanguageId(languageId);
    }

    public String getVersionModifier()
    {
        return this.valueObject.getVersionModifier();
    }
            
    public void setVersionModifier(String versionModifier)
    {
        this.valueObject.setVersionModifier(versionModifier);
    }
    
    public java.lang.String getVersionValue()
    {
        return this.valueObject.getVersionValue();
    }
            
    public void setVersionValue(java.lang.String versionValue)
    {
        this.valueObject.setVersionValue(versionValue);
    }

	public java.lang.String getEscapedVersionValue()
	{
		String versionValue = this.valueObject.getVersionValue();
		return versionValue.replaceAll("]]>", "cdataEnd"); 
	}
    
	public void setEscapedVersionValue(java.lang.String escapedVersionValue)
	{
		String versionValue = escapedVersionValue.replaceAll("cdataEnd", "]]>");
		this.valueObject.setVersionValue(versionValue);
	}

}        
