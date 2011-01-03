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

import java.util.ArrayList;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;

public class SiteNodeVersionImpl implements SiteNodeVersion
{
    private SiteNodeVersionVO valueObject = new SiteNodeVersionVO();

	private org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl owningSiteNode;
	private java.util.Collection serviceBindings = new ArrayList();

     
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
		setValueObject((SiteNodeVersionVO) valueObject);
	}
 
    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getId()
	 */
	public Integer getId() 
	{
		return getSiteNodeVersionId();
	}
	 
	public Object getIdAsObject()
	{
		return getId();
	}

    public SiteNodeVersionVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(SiteNodeVersionVO valueObject)
    {
        this.valueObject = valueObject;
    }   
    
    public java.lang.Integer getSiteNodeVersionId()
    {
        return this.valueObject.getSiteNodeVersionId();
    }
            
    public void setSiteNodeVersionId(java.lang.Integer siteNodeVersionId)
    {
        this.valueObject.setSiteNodeVersionId(siteNodeVersionId);
    }
      
    public java.lang.Integer getStateId()
    {
        return this.valueObject.getStateId();
    }
            
    public void setStateId(java.lang.Integer stateId)
    {
        this.valueObject.setStateId(stateId);
    }
      
    public java.lang.Integer getVersionNumber()
    {
        return this.valueObject.getVersionNumber();
    }
            
    public void setVersionNumber(java.lang.Integer versionNumber)
    {
        this.valueObject.setVersionNumber(versionNumber);
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
	
    public org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl getOwningSiteNode()
    {
        return this.owningSiteNode;
    }
            
    public void setOwningSiteNode (org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl owningSiteNode)
    {
        this.owningSiteNode = owningSiteNode;
        if(owningSiteNode != null)
        {	
	        this.valueObject.setSiteNodeId(owningSiteNode.getSiteNodeId());
			this.valueObject.setSiteNodeName(owningSiteNode.getName());
        }
    }
    
    public String getVersionModifier()
    {
        return this.valueObject.getVersionModifier();
    }
            
    public void setVersionModifier (String versionModifier)
    {
        this.valueObject.setVersionModifier(versionModifier);
    }
    
    public java.util.Collection getServiceBindings()
    {
    	return this.serviceBindings;
    }
    
    public void setServiceBindings(java.util.Collection serviceBindings)
    {
    	this.serviceBindings = serviceBindings;
    }

	public String getContentType()
	{
		return this.valueObject.getContentType();
	}

	public void setContentType(String contentType)
	{
		this.valueObject.setContentType(contentType);
	}

	public String getPageCacheKey()
	{
		return this.valueObject.getPageCacheKey();
	}

	public void setPageCacheKey(String pageCacheKey)
	{
		this.valueObject.setPageCacheKey(pageCacheKey);
	}

	public String getPageCacheTimeout()
	{
		return this.valueObject.getPageCacheTimeout();
	}

	public void setPageCacheTimeout(String pageCacheTimeout)
	{
		this.valueObject.setPageCacheTimeout(pageCacheTimeout);
	}

	public Integer getDisableEditOnSight()
	{
		return this.valueObject.getDisableEditOnSight();
	}

	public void setDisableEditOnSight(Integer disableEditOnSight)
	{
		this.valueObject.setDisableEditOnSight(disableEditOnSight);
	}

	public Integer getDisablePageCache()
	{
		return this.valueObject.getDisablePageCache();
	}

	public void setDisablePageCache(Integer disablePageCache)
	{
		this.valueObject.setDisablePageCache(disablePageCache);
	}

	public Integer getIsProtected()
	{
		return this.valueObject.getIsProtected();
	}

	public void setIsProtected(Integer isProtected)
	{
		this.valueObject.setIsProtected(isProtected);
	}

	public Integer getDisableLanguages()
	{
		return this.valueObject.getDisableLanguages();
	}

	public void setDisableLanguages(Integer disableLanguages)
	{
		this.valueObject.setDisableLanguages(disableLanguages);
	}

	public Integer getDisableForceIdentityCheck()
	{
		return this.valueObject.getDisableForceIdentityCheck();
	}

	public void setDisableForceIdentityCheck(Integer disableForceIdentityCheck)
	{
		this.valueObject.setDisableForceIdentityCheck(disableForceIdentityCheck);
	}

	public Integer getForceProtocolChange()
	{
		return this.valueObject.getForceProtocolChange();
	}

	public void setForceProtocolChange(Integer forceProtocolChange)
	{
		this.valueObject.setForceProtocolChange(forceProtocolChange);
	}

    public Integer getSortOrder()
    {
        return this.valueObject.getSortOrder();
    }
    
    public void setSortOrder(Integer sortOrder)
    {
        this.valueObject.setSortOrder(sortOrder);
    }

    public Boolean getIsHidden()
    {
        return this.valueObject.getIsHidden();
    }
    
    public void setIsHidden(Boolean isHidden)
    {
        this.valueObject.setIsHidden(isHidden);
    }

}        
