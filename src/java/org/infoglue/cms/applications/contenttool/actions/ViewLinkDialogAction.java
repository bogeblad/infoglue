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

import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.structure.SiteNodeVO;

/**
 * This action shows the Content-tree when binding stuff.
 */ 

public class ViewLinkDialogAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewLinkDialogAction.class.getName());

    /**
	 * 
	 */
	private static final long serialVersionUID = -5831748113035322067L;
	private Integer repositoryId;
    private Integer contentId;
    private Integer languageId;
    private String textAreaId;
    private String method;
    private Integer oldContentId;
    private String assetKey;
    private Integer oldSiteNodeId;
    
	private String tree;	
	private List repositories;
	
	
    public String doExecute() throws Exception
    {
		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);

		if(this.repositoryId == null)
		{
			this.repositoryId = (Integer)getHttpSession().getAttribute("repositoryId");
			if(this.repositoryId == null)
				this.repositoryId = RepositoryController.getController().getFirstRepositoryVO().getRepositoryId();		
		}
		
		return "success";					
    }
      
    public String doViewLinkDialogForFCKEditor() throws Exception
    {
        this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);

		if(this.repositoryId == null)
		{
			this.repositoryId = (Integer)getHttpSession().getAttribute("repositoryId");
			if(this.repositoryId == null)
				this.repositoryId = RepositoryController.getController().getFirstRepositoryVO().getRepositoryId();		
		}
		
		return "viewLinkDialogForFCKEditor";					        
    }

    public String doViewLinkDialogForFCKEditorV3() throws Exception
    {
        this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);

		if(this.repositoryId == null)
		{
			this.repositoryId = (Integer)getHttpSession().getAttribute("repositoryId");
			if(this.repositoryId == null)
				this.repositoryId = RepositoryController.getController().getFirstRepositoryVO().getRepositoryId();		
		}
		
		return "viewLinkDialogForFCKEditorV3";					        
    }

	public List getRepositories()
	{
		return repositories;
	}  
		
	public String getExpansion(Integer oldSiteNodeId)
	{
	    String expansion = "/";
	    
	    if(oldSiteNodeId == null)
	        return "";
	    
	    try
	    {
	        SiteNodeVO parentSiteNodeVO = SiteNodeController.getController().getParentSiteNode(oldSiteNodeId);
		    while(parentSiteNodeVO != null)
		    {
		        expansion += parentSiteNodeVO.getId() + "/";
		        parentSiteNodeVO = SiteNodeController.getController().getParentSiteNode(parentSiteNodeVO.getId());
		    }
	    }
	    catch(Exception e)
	    {
	        logger.warn("Expansion not possible:" + e.getMessage(), e);
	    }
	    
	    return expansion;
	}
	
	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}
   
	public String getTree()
	{
		return tree;
	}

	public void setTree(String string)
	{
		tree = string;
	}

	public String getTextAreaId()
	{
		return textAreaId;
	}

	public void setTextAreaId(String string)
	{
		textAreaId = string;
	}

	public Integer getOldSiteNodeId() 
	{
		return oldSiteNodeId;
	}
	
	public void setOldSiteNodeId(Integer oldSiteNodeId) 
	{
		this.oldSiteNodeId = oldSiteNodeId;
	}

    public Integer getOldContentId()
    {
        return oldContentId;
    }
    
    public void setOldContentId(Integer oldContentId)
    {
        this.oldContentId = oldContentId;
    }

    public String getAssetKey()
    {
        return assetKey;
    }
    
    public void setAssetKey(String assetKey)
    {
        this.assetKey = assetKey;
    }
    
    public Integer getContentId()
    {
        return contentId;
    }
    
    public void setContentId(Integer contentId)
    {
        this.contentId = contentId;
    }
    
    public Integer getLanguageId()
    {
        return languageId;
    }
    
    public void setLanguageId(Integer languageId)
    {
        this.languageId = languageId;
    }
    
    public String getMethod()
    {
        return method;
    }
    
    public void setMethod(String method)
    {
        this.method = method;
    }
    
}
