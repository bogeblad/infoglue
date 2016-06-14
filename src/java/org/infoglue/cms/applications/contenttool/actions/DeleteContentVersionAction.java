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

package org.infoglue.cms.applications.contenttool.actions;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionControllerProxy;
import org.infoglue.cms.entities.content.ContentVersionVO;

/**
 * This action removes a contentversion from the system.
 * 
 * @author Stefan Sik
 */

public class DeleteContentVersionAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
	private ContentVersionVO contentVersionVO;
	private Integer contentId;
	private Integer repositoryId;
	
	public DeleteContentVersionAction()
	{
		this(new ContentVersionVO());
	}

	public DeleteContentVersionAction(ContentVersionVO contentVersionVO) 
	{
		this.contentVersionVO = contentVersionVO;
	}
	
	protected String doExecute() throws Exception 
	{
		ContentVersionControllerProxy.getController().acDelete(this.getInfoGluePrincipal(), this.contentVersionVO);
		//ContentVersionController.delete(contentVersionVO);
		
		return "success";
	}
	
	public void setContentVersionId(Integer contentId)
	{
		this.contentVersionVO.setContentVersionId(contentId);
	}

	public Integer getContentVersionId()
	{
		return this.contentVersionVO.getContentVersionId();
	}
	
	/**
	 * @return
	 */
	public Integer getRepositoryId() {
		return repositoryId;
	}

	/**
	 * @param integer
	 */
	public void setRepositoryId(Integer integer) {
		repositoryId = integer;
	}

	/**
	 * @return
	 */
	public Integer getContentId() {
		return contentId;
	}

	/**
	 * @param integer
	 */
	public void setContentId(Integer integer) {
		contentId = integer;
	}

}
