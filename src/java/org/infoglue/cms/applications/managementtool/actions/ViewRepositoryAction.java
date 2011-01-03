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
import org.infoglue.cms.entities.management.RepositoryVO;

/**
 * This class implements the action class for viewRepository.
 * The use-case lets the user see all information about a specific site/repository.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewRepositoryAction extends InfoGlueAbstractAction
{ 
	private static final long serialVersionUID = 1L;

    private RepositoryVO repositoryVO;

	private List<LanguageVO> languageVOList;
    
    public ViewRepositoryAction()
    {
        this(new RepositoryVO());
    }
    
    public ViewRepositoryAction(RepositoryVO repositoryVO)
    {
        this.repositoryVO = repositoryVO;
    }
    
    protected void initialize(Integer repositoryId) throws Exception
    {
        repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);
        
        this.languageVOList = RepositoryLanguageController.getController().getLanguageVOListForRepositoryId(repositoryId);
    } 

    /**
     * The main method that fetches the Value-object for this use-case
     */
    
    public String doExecute() throws Exception
    {
        this.initialize(getRepositoryId());

        return "success";
    }

    /**
     * The main method that fetches the Value-object for this use-case
     */
    
    public String doLocalView() throws Exception
    {
        this.initialize(getRepositoryId());

        return "successLocal";
    }
          
    public java.lang.Integer getRepositoryId()
    {
        return this.repositoryVO.getRepositoryId();
    }
        
    public void setRepositoryId(java.lang.Integer repositoryId) throws Exception
    {
        this.repositoryVO.setRepositoryId(repositoryId);
    }

    public java.lang.String getName()
    {
        return this.repositoryVO.getName();
    }

    public java.lang.String getDescription()
    {
        return this.repositoryVO.getDescription();
    }

    public java.lang.String getDnsName()
    {
        return this.repositoryVO.getDnsName();
    }
	
    public List<LanguageVO> getLanguageVOList()
    {
        return languageVOList;
    }
    
	public RepositoryVO getRepositoryVO()
	{
		return repositoryVO;
	}

}
