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

package org.infoglue.cms.applications.structuretool.actions;

import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.usecases.structuretool.ViewSiteNodeUCC;
import org.infoglue.cms.controllers.usecases.structuretool.ViewSiteNodeUCCFactory;
import org.infoglue.cms.entities.management.SiteNodeTypeDefinitionVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;

public class ViewSiteNodeVersionAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewSiteNodeVersionAction.class.getName());

	private static final long serialVersionUID = 1L;

	private Integer unrefreshedSiteNodeId = new Integer(0);
	private Integer changeTypeId = new Integer(0);
	private SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO;
	private List availableLanguages = null;
	private Integer languageId;
	//private Integer stateId;
		
    private SiteNodeVO siteNodeVO;
    private SiteNodeVersionVO siteNodeVersionVO;

    public ViewSiteNodeVersionAction()
    {
        this(new SiteNodeVO(), new SiteNodeVersionVO());
    }
    
    public ViewSiteNodeVersionAction(SiteNodeVO siteNodeVO, SiteNodeVersionVO siteNodeVersionVO)
    {
		logger.info("Construction ViewSiteNodeAction");
        this.siteNodeVO = siteNodeVO;
        this.siteNodeVersionVO = siteNodeVersionVO;
    }
    
    protected void initialize(Integer siteNodeId, Integer languageId) throws Exception
    {
		ViewSiteNodeUCC viewSiteNodeUCC = ViewSiteNodeUCCFactory.newViewSiteNodeUCC();
        this.siteNodeVO = viewSiteNodeUCC.viewSiteNode(siteNodeId);
        //this.siteNodeTypeDefinitionVO = viewSiteNodeUCC.getSiteNodeTypeDefinition(siteNodeId);
        //this.siteNodeVersionVO = viewSiteNodeUCC.getLatestSiteNodeVersion(siteNodeId, languageId);
     	
     	logger.info("siteNodeVersionVO:" + siteNodeVersionVO);
        logger.info("siteNodeVO:" + siteNodeVO);
        //this.availableLanguages = viewSiteNodeUCC.getRepositoryLanguages(siteNodeId);
    } 

    public String doExecute() throws Exception
    {
        this.initialize(getSiteNodeId(), this.languageId);
        
        return "success";
    }

    public String doPreview() throws Exception
    {
        this.initialize(getSiteNodeId(), this.languageId);
        
        return "preview";
    }

    public String doChangeState() throws Exception
    {
    	logger.info("Gonna change state with comment:" + this.siteNodeVersionVO.getVersionComment());
    	//SiteNodeVersionController.updateStateId(this.siteNodeVersionVO.getSiteNodeVersionId(), getStateId(), this.siteNodeVersionVO.getVersionComment(), getRequest().getRemoteUser(), this.getSiteNodeId(), this.getLanguageId());
    	this.initialize(getSiteNodeId(), this.languageId);
        return "success";
    }
    
    public String doCommentVersion() throws Exception
    {
    	logger.info("Gonna show the comment-view");
        return "commentVersion";
    }
    
    
    public java.lang.Integer getSiteNodeVersionId()
    {
        return this.siteNodeVersionVO.getSiteNodeVersionId();
    }
    
    public void setSiteNodeVersionId(java.lang.Integer siteNodeVersionId)
    {
	    this.siteNodeVersionVO.setSiteNodeVersionId(siteNodeVersionId);
    }
        
    public java.lang.Integer getSiteNodeId()
    {
        return this.siteNodeVO.getSiteNodeId();
    }
        
    public void setSiteNodeId(java.lang.Integer siteNodeId)
    {
	    this.siteNodeVO.setSiteNodeId(siteNodeId);
    }
    
    public java.lang.Integer getSiteNodeTypeDefinitionId()
    {
        return this.siteNodeTypeDefinitionVO.getSiteNodeTypeDefinitionId();
    }
            
   	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
	}

    public java.lang.Integer getLanguageId()
    {
        return this.languageId;
    }
	
	public void setStateId(Integer stateId)
	{
		this.siteNodeVersionVO.setStateId(stateId);
	}

	public void setVersionComment(String versionComment)
	{
		this.siteNodeVersionVO.setVersionComment(versionComment);
	}
	
	public String getVersionComment()
	{
		return this.siteNodeVersionVO.getVersionComment();
	}
	
	public Integer getStateId()
	{
		return this.siteNodeVersionVO.getStateId();
	}
            
    public String getName()
    {
        return this.siteNodeVO.getName();
    }

    public java.lang.Integer getRepositoryId()
    {
        return this.siteNodeVO.getRepositoryId();
    }

	public List getAvailableLanguages()
	{
		return this.availableLanguages;
	}	


}
