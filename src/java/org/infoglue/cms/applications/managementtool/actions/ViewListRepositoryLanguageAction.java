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

import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryLanguageController;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryLanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;

/**
 * 	Action class for usecase ViewListLanguageUCC 
 *
 *  @author Mattias Bogeblad
 */

public class ViewListRepositoryLanguageAction extends InfoGlueAbstractAction 
{
	private static final long serialVersionUID = 1L;

	private RepositoryVO repositoryVO;
	private List<RepositoryLanguageVO> repositoryLanguageVOList;
	private List<LanguageVO> allRemainingLanguageVOList;
	private Integer repositoryId;

	protected String doExecute() throws Exception 
	{
		this.repositoryVO = RepositoryController.getController().getRepositoryVOWithId(this.repositoryId);
		this.repositoryLanguageVOList = RepositoryLanguageController.getController().getRepositoryLanguageVOListWithRepositoryId(repositoryId);
		this.allRemainingLanguageVOList = LanguageController.getController().getRemainingLanguages(repositoryId);
		
        return "success";
	}
	
	public RepositoryVO getRepository()
	{
		return this.repositoryVO;
	}

	public List getAvailableLanguages()
	{
		return this.repositoryLanguageVOList;		
	}
	
	public List getAllRemainingLanguages()
	{
		return this.allRemainingLanguageVOList;		
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;		
	}

	public LanguageVO getLanguage(Integer repositoryLanguageId) throws Exception
	{
		return LanguageController.getController().getLanguageVOWithRepositoryLanguageId(repositoryLanguageId);
	}
	
}
