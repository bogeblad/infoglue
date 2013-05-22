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

package org.infoglue.cms.entities.management;
		 
import java.util.Date;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class PageDeliveryMetaDataVO implements BaseEntityVO
{
	private Integer pageDeliveryMetaDataId;
	private Integer siteNodeId = -1;
	private Integer languageId = -1;
	private Integer contentId = -1;
	private Date lastModifiedDateTime;
	private Boolean selectiveCacheUpdateNotApplicable = false;
	private Date lastModifiedTimeout = null;
	private String usedEntities;
	
	@Override
	public Integer getId() {
		return getPageDeliveryMetaDataId();
	}
	/**
	 * @param pageDeliveryMetaDataId the pageDeliveryMetaDataId to set
	 */
	public void setId(Integer id) {
		setPageDeliveryMetaDataId(id);
	}

	/**
	 * @return the pageDeliveryMetaDataId
	 */
	public Integer getPageDeliveryMetaDataId() {
		return pageDeliveryMetaDataId;
	}
	/**
	 * @param pageDeliveryMetaDataId the pageDeliveryMetaDataId to set
	 */
	public void setPageDeliveryMetaDataId(Integer pageDeliveryMetaDataId) {
		this.pageDeliveryMetaDataId = pageDeliveryMetaDataId;
	}
	/**
	 * @return the siteNodeId
	 */
	public Integer getSiteNodeId() {
		return siteNodeId;
	}
	/**
	 * @param siteNodeId the siteNodeId to set
	 */
	public void setSiteNodeId(Integer siteNodeId) {
		this.siteNodeId = siteNodeId;
	}
	/**
	 * @return the languageId
	 */
	public Integer getLanguageId() {
		return languageId;
	}
	/**
	 * @param languageId the languageId to set
	 */
	public void setLanguageId(Integer languageId) {
		if(languageId != null)
			this.languageId = languageId;
	}
	/**
	 * @return the contentId
	 */
	public Integer getContentId() {
		return contentId;
	}
	/**
	 * @param contentId the contentId to set
	 */
	public void setContentId(Integer contentId) {
		if(contentId != null)
			this.contentId = contentId;
	}
	/**
	 * @return the lastModifiedDateTime
	 */
	public Date getLastModifiedDateTime() {
		return lastModifiedDateTime;
	}
	/**
	 * @param lastModifiedDateTime the lastModifiedDateTime to set
	 */
	public void setLastModifiedDateTime(Date lastModifiedDateTime) {
		this.lastModifiedDateTime = lastModifiedDateTime;
	}
	/**
	 * @return the selectiveCacheUpdateNotApplicable
	 */
	public Boolean getSelectiveCacheUpdateNotApplicable() {
		return selectiveCacheUpdateNotApplicable;
	}
	/**
	 * @param selectiveCacheUpdateNotApplicable the selectiveCacheUpdateNotApplicable to set
	 */
	public void setSelectiveCacheUpdateNotApplicable(
			Boolean selectiveCacheUpdateNotApplicable) {
		this.selectiveCacheUpdateNotApplicable = selectiveCacheUpdateNotApplicable;
	}
	/**
	 * @return the lastModifiedTimeout
	 */
	public Date getLastModifiedTimeout() {
		return lastModifiedTimeout;
	}
	/**
	 * @param lastModifiedTimeout the lastModifiedTimeout to set
	 */
	public void setLastModifiedTimeout(Date lastModifiedTimeout) {
		this.lastModifiedTimeout = lastModifiedTimeout;
	}

	/**
	 * @return the usedEntities
	 */
	public String getUsedEntities() {
		return usedEntities;
	}
	/**
	 * @param usedEntities the usedEntities to set
	 */
	public void setUsedEntities(String usedEntities) {
		this.usedEntities = usedEntities;
	}

	@Override
	public ConstraintExceptionBuffer validate() {
		// TODO Auto-generated method stub
		return null;
	}
}        
