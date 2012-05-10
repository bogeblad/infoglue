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
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;

/**
 * This action handler publications of repositoryLanguage in the system.
 * 
 * @author Mattias Bogeblad
 */

public class RepositoryLanguageAction extends InfoGlueAbstractAction
{
	private RepositoryLanguageVO repositoryLanguageVO;
	private Integer repositoryLanguageId;
	private Integer repositoryId;
	private Integer languageId;
	
	public RepositoryLanguageAction()
	{
		this(new RepositoryLanguageVO());
	}

	public RepositoryLanguageAction(RepositoryLanguageVO repositoryLanguageVO) 
	{
		this.repositoryLanguageVO = repositoryLanguageVO;
	}
	
	protected String doExecute() throws SystemException 
	{
		RepositoryLanguageController.getController().publishRepositoryLanguage(this.repositoryLanguageVO);
		
		return "success";
	}
	
	public String doUnpublish() throws SystemException 
	{
		RepositoryLanguageController.getController().unpublishRepositoryLanguage(this.repositoryLanguageVO);

		return "success";
	}

	public String doMoveDown() throws SystemException 
	{
		RepositoryLanguageController.getController().moveRepositoryLanguage(this.repositoryLanguageVO, true);

		return "success";
	}

	public String doMoveUp() throws SystemException 
	{
		RepositoryLanguageController.getController().moveRepositoryLanguage(this.repositoryLanguageVO, false);

		return "success";
	}

	/*
	public String doCreate() 
	{
		RepositoryLanguageController.getController().createRepositoryLanguage(this.repositoryId, this.languageId);
		return "success";
	}
	*/
	
	public String doUpdate() throws ConstraintException, SystemException
	{
    	String[] values = getRequest().getParameterValues("languageId");
		RepositoryLanguageController.getController().updateRepositoryLanguages(this.repositoryId,values);
			
		return SUCCESS;	
	}
	
	public void setRepositoryLanguageId(Integer repositoryLanguageId)
	{
		this.repositoryLanguageVO.setRepositoryLanguageId(repositoryLanguageId);	
	}

    public java.lang.Integer getRepositoryLanguageId()
    {
        return this.repositoryLanguageVO.getRepositoryLanguageId();
    }
        
	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;	
	}

    public java.lang.Integer getRepositoryId()
    {
        return this.repositoryId;
    }

	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;	
	}

    public java.lang.Integer getLanguageId()
    {
        return this.languageId;
    }
	
}