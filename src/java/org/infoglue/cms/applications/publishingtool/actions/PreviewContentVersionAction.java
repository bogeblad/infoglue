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

package org.infoglue.cms.applications.publishingtool.actions;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.ContentVersionVO;

/**
 * @author ss
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

public class PreviewContentVersionAction  extends InfoGlueAbstractAction 
{
	private Integer contentVersionId;
	private ContentVersionVO contentVersionVO;

	/**
	 * @see org.infoglue.cms.applications.common.actions.WebworkAbstractAction#doExecute()
	 */
	protected String doExecute() throws Exception 
	{
		contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId);
		return "success";
	}


	/**
	 * Returns the contentVersionId.
	 * @return Integer
	 */
	public Integer getContentVersionId() {
		return contentVersionId;
	}

	/**
	 * Sets the contentVersionId.
	 * @param contentVersionId The contentVersionId to set
	 */
	public void setContentVersionId(Integer contentVersionId) {
		this.contentVersionId = contentVersionId;
	}

	/**
	 * Returns the contentVersionVO.
	 * @return ContentVersionVO
	 */
	public ContentVersionVO getContentVersionVO() {
		return contentVersionVO;
	}

	/**
	 * Sets the contentVersionVO.
	 * @param contentVersionVO The contentVersionVO to set
	 */
	public void setContentVersionVO(ContentVersionVO contentVersionVO) {
		this.contentVersionVO = contentVersionVO;
	}

}
