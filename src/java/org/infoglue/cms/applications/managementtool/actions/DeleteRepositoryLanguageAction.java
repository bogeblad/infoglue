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

package org.infoglue.cms.applications.managementtool.actions;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryLanguageController;
import org.infoglue.cms.entities.management.RepositoryLanguageVO;
import org.infoglue.cms.exception.SystemException;

/**
 * This action removes a repositoryLanguage from the system.
 * 
 * @author Mattias Bogeblad
 */

public class DeleteRepositoryLanguageAction extends InfoGlueAbstractAction
{
	private RepositoryLanguageVO repositoryLanguageVO;
	private Integer repositoryLanguageId;
	private Integer repositoryId;
	
	public DeleteRepositoryLanguageAction()
	{
		this(new RepositoryLanguageVO());
	}

	public DeleteRepositoryLanguageAction(RepositoryLanguageVO repositoryLanguageVO) 
	{
		this.repositoryLanguageVO = repositoryLanguageVO;
	}
	
	protected String doExecute() throws Exception 
	{
		this.repositoryLanguageVO.setRepositoryLanguageId(this.getRepositoryLanguageId());
		RepositoryLanguageController.getController().delete(this.repositoryLanguageVO);
		
		return "success";
	}
	
	public void setRepositoryLanguageId(Integer repositoryLanguageId) throws SystemException
	{
		this.repositoryLanguageVO.setRepositoryLanguageId(repositoryLanguageId);	
	}

    public java.lang.Integer getRepositoryLanguageId()
    {
        return this.repositoryLanguageVO.getRepositoryLanguageId();
    }
        
	public void setRepositoryId(Integer repositoryId) throws SystemException
	{
		this.repositoryId = repositoryId;	
	}

    public java.lang.Integer getRepositoryId()
    {
        return this.repositoryId;
    }
	
}
