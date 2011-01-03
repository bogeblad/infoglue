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

package org.infoglue.cms.applications.contenttool.wizards.actions;

import java.net.URLEncoder;
import java.util.List;

import org.infoglue.cms.applications.contenttool.actions.ViewContentTreeActionInterface;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * This action represents a tree where the user can select where to save his new content.
 */

public class CreateContentWizardChooseParentAction extends CreateContentWizardAbstractAction implements ViewContentTreeActionInterface
{
	//Used by the tree only
	private Integer contentId;
	private String tree;
	private String hideLeafs;
	private Integer siteNodeId;
	private Integer languageId;
	private String componentId;
	private String propertyName;
	private String refreshAddress;
	private String showSimple;
	
	private Integer parentContentId;
	private Integer repositoryId;
	private ConstraintExceptionBuffer ceb;
	
	private String returnAddress;
	private String[] allowedContentTypeIds	 = null;
	
	private List repositories;
	
	public CreateContentWizardChooseParentAction()
	{
		this(new ContentVO());
	}
	
	public CreateContentWizardChooseParentAction(ContentVO contentVO)
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

	public void setParentContentId(Integer parentContentId)
	{
		this.parentContentId = parentContentId;
	}

	public Integer getParentContentId()
	{
		return this.parentContentId;
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

    public String getAllowedContentTypeIdsAsUrlEncodedString() throws Exception
    {
        StringBuffer sb = new StringBuffer();
        
        for(int i=0; i<allowedContentTypeIds.length; i++)
        {
            if(i > 0)
                sb.append("&");
            
            sb.append("allowedContentTypeIds=" + URLEncoder.encode(allowedContentTypeIds[i], "UTF-8"));
        }

        return sb.toString();
    }

    public String[] getAllowedContentTypeIds()
    {
        return allowedContentTypeIds;
    }
    
    public void setAllowedContentTypeIds(String[] allowedContentTypeIds)
    {
        this.allowedContentTypeIds = allowedContentTypeIds;
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
		getCreateContentWizardInfoBean().setReturnAddress(refreshAddress);
	}
	
	public String getRefreshAddress()
	{
		return getCreateContentWizardInfoBean().getReturnAddress();
	}

	public String getEncodedRefreshAddress() throws Exception
	{
		return URLEncoder.encode(getRefreshAddress(), "UTF-8");
	}

	public void setCancelAddress(String cancelAddress)
	{
		getCreateContentWizardInfoBean().setCancelAddress(cancelAddress);
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
