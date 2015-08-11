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

import java.util.ArrayList;
import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.usecases.structuretool.ViewSiteNodeTreeUCC;
import org.infoglue.cms.controllers.usecases.structuretool.ViewSiteNodeTreeUCCFactory;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;

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
    List<RepositoryVO> duplicatedRepositoryVOList;
    
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
        
        this.duplicatedRepositoryVOList = getRepositoryDuplicates(repositoryVO);
        
        this.languageVOList = LanguageController.getController().getLanguageVOList(repositoryId); 
        
    	if(this.repositoryVO != null)
    	{
	    	ViewSiteNodeTreeUCC ucc = ViewSiteNodeTreeUCCFactory.newViewSiteNodeTreeUCC();	
	    	ucc.getRootSiteNode(this.repositoryVO.getId(), getInfoGluePrincipal());
    	}

    } 

    /**
     * The main method that fetches the Value-object for this use-case
     */
    
    public String doExecute() throws Exception
    {
        this.initialize(getRepositoryId());

        return "success";
    }
    private List<RepositoryVO> getRepositoryDuplicates (RepositoryVO repositoryVO) throws ConstraintException, SystemException, Bug {
    	List<RepositoryVO> duplicatedRepositories = new ArrayList<RepositoryVO>();
    	List<RepositoryVO> repositoryVOList = RepositoryController.getController().getRepositoryVOList();
    	for (RepositoryVO repoVO : repositoryVOList) {
    		
    		if (!repoVO.getRepositoryId().toString().trim().equalsIgnoreCase(repositoryVO.getRepositoryId().toString().trim())) {
    			
	    		if (repoVO.getWorkingBaseUrl().equalsIgnoreCase(repositoryVO.getWorkingBaseUrl()) && repoVO.getWorkingPath().equalsIgnoreCase(repositoryVO.getWorkingPath())) {
	    			duplicatedRepositories.add(repoVO);
	    		} else if (repoVO.getLiveBaseUrl().equalsIgnoreCase(repositoryVO.getLiveBaseUrl()) && repoVO.getPath().equalsIgnoreCase(repositoryVO.getPath())) {
	    			duplicatedRepositories.add(repoVO);
	    		}
    		}
    	}
    	return duplicatedRepositories;
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
	
	public List<RepositoryVO> getDuplicatedRepositoryVOList()
	{
		return duplicatedRepositoryVOList;
	}

}
