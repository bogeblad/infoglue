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
import org.infoglue.cms.entities.management.Redirect;
import org.infoglue.cms.entities.management.RedirectVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;


public class RedirectImpl implements Redirect
{
	private RedirectVO valueObject = new RedirectVO();

	public String toString()
	{
		return this.valueObject.toString();
	}

	public Integer getId()
	{
		return this.getRedirectId();
	}

	public Object getIdAsObject()
	{
		return getId();
	}

	public RedirectVO getValueObject()
	{
		return this.valueObject;
	}

	public void setValueObject(RedirectVO valueObject)
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
		setValueObject((RedirectVO) valueObject);
	}

	public java.lang.Integer getRedirectId()
	{
		return this.valueObject.getRedirectId();
	}

	public void setRedirectId(java.lang.Integer RedirectId) throws SystemException
	{
		this.valueObject.setRedirectId(RedirectId);
	}

	public java.lang.String getUrl()
	{
		return this.valueObject.getUrl();
	}

	public void setUrl(java.lang.String url) throws ConstraintException
	{
		this.valueObject.setUrl(url);
	}

	public java.lang.String getRedirectUrl()
	{
		return this.valueObject.getRedirectUrl();
	}

	public void setRedirectUrl(java.lang.String redirectUrl) throws ConstraintException
	{
		this.valueObject.setRedirectUrl(redirectUrl);
	}

	public java.lang.String getModifier()
	{
		return this.valueObject.getModifier();
	}

	public void setModifier(java.lang.String modifier) throws ConstraintException
	{
		this.valueObject.setModifier(modifier);
	}

	public java.util.Date getCreatedDateTime()
	{
		return this.valueObject.getCreatedDateTime();
	}

	public void setCreatedDateTime(java.util.Date createdDateTime)
	{
		this.valueObject.setCreatedDateTime(createdDateTime);
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

	public Boolean getIsUserManaged()
	{
		return this.valueObject.getIsUserManaged();
	}

	public void setIsUserManaged(Boolean isUserManaged)
	{
		this.valueObject.setIsUserManaged(isUserManaged);
	}

	@Override
	public Integer getSiteNodeId()
	{
		return this.valueObject.getSiteNodeId();
	}

	@Override
	public void setSiteNodeId(Integer siteNodeId)
	{
		this.valueObject.setSiteNodeId(siteNodeId);
	}

	@Override
	public Integer getLanguageId()
	{
		return this.valueObject.getLanguageId();
	}

	@Override
	public void setLanguageId(Integer languageId)
	{
		this.valueObject.setLanguageId(languageId);
	}

}
