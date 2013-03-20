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

package org.infoglue.cms.entities.content;

import java.util.Date;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.DateHelper;
import org.infoglue.cms.util.validators.ContentVersionValidator;
import org.infoglue.deliver.util.CompressionHelper;

public class ContentVersionAttributeVO implements BaseEntityVO
{
	private Integer contentVersionAttributeId;
	private Integer contentVersionId;
	private String attributeName;
	private String attributeValue;

	public Integer getId() {
		return this.contentVersionAttributeId;
	}

	/**
	 * @return the contentVersionAttributeId
	 */
	public Integer getContentVersionAttributeId() {
		return contentVersionAttributeId;
	}
	/**
	 * @param contentVersionAttributeId the contentVersionAttributeId to set
	 */
	public void setContentVersionAttributeId(Integer contentVersionAttributeId) {
		this.contentVersionAttributeId = contentVersionAttributeId;
	}
	/**
	 * @return the contentVersionId
	 */
	public Integer getContentVersionId() {
		return contentVersionId;
	}
	/**
	 * @param contentVersionId the contentVersionId to set
	 */
	public void setContentVersionId(Integer contentVersionId) {
		this.contentVersionId = contentVersionId;
	}
	/**
	 * @return the attributeName
	 */
	public String getAttributeName() {
		return attributeName;
	}
	/**
	 * @param attributeName the attributeName to set
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	/**
	 * @return the attributeValue
	 */
	public String getAttributeValue() {
		return attributeValue;
	}
	/**
	 * @param attributeValue the attributeValue to set
	 */
	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}
	
	public ConstraintExceptionBuffer validate() {
		return null;
	}

	
}
        
