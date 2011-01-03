/* ===============================================================================
 *
 * Part of the InfoGlue SiteNode Management Platform (www.infoglue.org)
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

package org.infoglue.cms.applications.structuretool.wizards.actions;

import java.net.URLEncoder;
import java.util.List;

import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * This action represents a tree where the user can select where to save his new SiteNode.
 */

public class CreateSiteNodeWizardChooseParentAction extends CreateSiteNodeWizardAbstractAction
{
	//Used by the tree only
	private Integer siteNodeId;
	private String tree;
	private String hideLeafs;
	private Integer contentId;
	private Integer languageId;
	private String componentId;
	private String propertyName;
	private String showSimple;
	
	//private Integer parentSiteNodeId;
	private Integer repositoryId;
	private ConstraintExceptionBuffer ceb;
	
	private String returnAddress;
	private String[] allowedSiteNodeTypeIds	 = null;
	
	private List repositories;
	
	public CreateSiteNodeWizardChooseParentAction()
	{
		this(new SiteNodeVO());
	}
	
	public CreateSiteNodeWizardChooseParentAction(SiteNodeVO SiteNodeVO)
	{
		this.ceb = new ConstraintExceptionBuffer();			
	}	

	public String doExecute() throws Exception
	{
		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);

		return "success";
	}
	
	public Integer getTopRepositoryId() throws ConstraintException, SystemException, Bug
	{		
		Integer topRepositoryId = null;

		if (repositoryId != null)
			topRepositoryId = repositoryId;

		if(repositories.size() > 0)
		{
			topRepositoryId = ((RepositoryVO)repositories.get(0)).getRepositoryId();
		}
  	
		return topRepositoryId;
	}
  
	public void setHideLeafs(String hideLeafs)
	{
		this.hideLeafs = hideLeafs;
	}

	public String getHideLeafs()
	{
		return this.hideLeafs;
	}    

	public String getTree()
	{
		return tree;
	}

	public void setTree(String tree)
	{
		this.tree = tree;
	}

	public void setParentSiteNodeId(Integer parentSiteNodeId)
	{
		this.getCreateSiteNodeWizardInfoBean().setParentSiteNodeId(parentSiteNodeId);
	}

	public Integer getParentSiteNodeId()
	{
		return this.getCreateSiteNodeWizardInfoBean().getParentSiteNodeId();
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public Integer getRepositoryId() 
	{
		try
		{
			if(this.repositoryId == null)
			{	
				this.repositoryId = (Integer)getHttpSession().getAttribute("repositoryId");
					
				if(this.repositoryId == null)
				{
					this.repositoryId = getTopRepositoryId();
					getHttpSession().setAttribute("repositoryId", this.repositoryId);		
				}
			}
		}
		catch(Exception e)
		{
		}
	    	
		return repositoryId;
	}

	public void setContentId(Integer contentId)
	{
		this.contentId = contentId;
	}

	public Integer getContentId()
	{
		return this.contentId;
	}    
	
	public String getReturnAddress()
	{
		return returnAddress;
	}

	public void setReturnAddress(String string)
	{
		returnAddress = string;
	}

    public String getAllowedSiteNodeTypeIdsAsUrlEncodedString() throws Exception
    {
        StringBuffer sb = new StringBuffer();
        
        for(int i=0; i<allowedSiteNodeTypeIds.length; i++)
        {
            if(i > 0)
                sb.append("&");
            
            sb.append("allowedSiteNodeTypeIds=" + URLEncoder.encode(allowedSiteNodeTypeIds[i], "UTF-8"));
        }

        return sb.toString();
    }

    public String[] getAllowedSiteNodeTypeIds()
    {
        return allowedSiteNodeTypeIds;
    }
    
    public void setAllowedSiteNodeTypeIds(String[] allowedSiteNodeTypeIds)
    {
        this.allowedSiteNodeTypeIds = allowedSiteNodeTypeIds;
    }

	public List getRepositories() 
	{
		return repositories;
	}

	public String getComponentId() 
	{
		return componentId;
	}

	public void setComponentId(String componentId) 
	{
		this.componentId = componentId;
	}

	public Integer getLanguageId() 
	{
		return languageId;
	}

	public void setLanguageId(Integer languageId) 
	{
		this.languageId = languageId;
	}

	public String getPropertyName() 
	{
		return propertyName;
	}

	public void setPropertyName(String propertyName) 
	{
		this.propertyName = propertyName;
	}

	public void setRefreshAddress(String refreshAddress)
	{
		getCreateSiteNodeWizardInfoBean().setReturnAddress(refreshAddress);
	}
	
	public String getRefreshAddress()
	{
		return getCreateSiteNodeWizardInfoBean().getReturnAddress();
	}

	public String getEncodedRefreshAddress() throws Exception
	{
		return URLEncoder.encode(getRefreshAddress(), "UTF-8");
	}

	public void setCancelAddress(String cancelAddress)
	{
		getCreateSiteNodeWizardInfoBean().setCancelAddress(cancelAddress);
	}
	
	public String getShowSimple() 
	{
		return showSimple;
	}

	public void setShowSimple(String showSimple) 
	{
		this.showSimple = showSimple;
	}

	public Integer getSiteNodeId() 
	{
		return siteNodeId;
	}

	public void setSiteNodeId(Integer siteNodeId) 
	{
		this.siteNodeId = siteNodeId;
	}

}
