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

public class PageDeliveryMetaDataEntityVO implements BaseEntityVO
{
	private Integer pageDeliveryMetaDataEntityId;
	private Integer pageDeliveryMetaDataId;
	private Integer siteNodeId;
	private Integer contentId;
	
	@Override
	public Integer getId() {
		return getPageDeliveryMetaDataEntityId();
	}
	/**
	 * @param pageDeliveryMetaDataId the pageDeliveryMetaDataId to set
	 */
	public void setId(Integer id) {
		setPageDeliveryMetaDataEntityId(id);
	}

	/**
	 * @return the pageDeliveryMetaDataId
	 */
	public Integer getPageDeliveryMetaDataEntityId() {
		return pageDeliveryMetaDataEntityId;
	}
	/**
	 * @param pageDeliveryMetaDataId the pageDeliveryMetaDataId to set
	 */
	public void setPageDeliveryMetaDataEntityId(Integer pageDeliveryMetaDataEntityId) {
		this.pageDeliveryMetaDataEntityId = pageDeliveryMetaDataEntityId;
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
	 * @return the contentId
	 */
	public Integer getContentId() {
		return contentId;
	}
	/**
	 * @param contentId the contentId to set
	 */
	public void setContentId(Integer contentId) {
		this.contentId = contentId;
	}

	@Override
	public ConstraintExceptionBuffer validate() {
		// TODO Auto-generated method stub
		return null;
	}
}        
