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

import org.infoglue.cms.entities.kernel.IBaseEntity;

public interface SiteNodeVersion extends IBaseEntity
{
        
    public SiteNodeVersionVO getValueObject();
    
    public void setValueObject(SiteNodeVersionVO valueObject);

    
    public java.lang.Integer getSiteNodeVersionId();
    
    public void setSiteNodeVersionId(java.lang.Integer siteNodeVersionId);
    
    public java.lang.Integer getStateId();
    
    public void setStateId(java.lang.Integer stateId);
    
    public java.lang.Integer getVersionNumber();
    
    public void setVersionNumber(java.lang.Integer versionNumber);
    
    public java.util.Date getModifiedDateTime();
    
    public void setModifiedDateTime(java.util.Date modifiedDateTime);
    
    public java.lang.String getVersionComment();
    
    public void setVersionComment(java.lang.String versionComment);
    
    public java.lang.Boolean getIsCheckedOut();
    
    public void setIsCheckedOut(java.lang.Boolean isCheckedOut);
    
    public java.lang.Boolean getIsActive();
    
    public void setIsActive(java.lang.Boolean isActive);
    
	public String getContentType();

	public void setContentType(String contentType);

	public String getPageCacheKey();

	public void setPageCacheKey(String pageCacheKey);

	public String getPageCacheTimeout();

	public void setPageCacheTimeout(String pageCacheTimeout);

	public Integer getDisableEditOnSight();

    public Integer getSortOrder();
    
    public void setSortOrder(Integer sortOrder);

    public Boolean getIsHidden();
    
    public void setIsHidden(Boolean isHidden);

	public void setDisableEditOnSight(Integer disableEditOnSight);

	public Integer getDisableLanguages();

	public void setDisableLanguages(Integer disableLanguages);

	public Integer getDisablePageCache();

	public void setDisablePageCache(Integer disablePageCache);

	public Integer getDisableForceIdentityCheck();

	public void setDisableForceIdentityCheck(Integer disableForceIdentityCheck);

	public Integer getForceProtocolChange();

	public void setForceProtocolChange(Integer forceProtocolChange);

	public Integer getIsProtected();

	public void setIsProtected(Integer isProtected);
    
    public org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl getOwningSiteNode();
    
    public void setOwningSiteNode(org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl owningSiteNode);
    
    public String getVersionModifier();
    
    public void setVersionModifier(String versionModifier);

    public java.util.Collection getServiceBindings();
    
    public void setServiceBindings(java.util.Collection serviceBindings);
         
}
