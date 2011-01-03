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

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeControllerProxy;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * This action represents the CreateSiteNode Usecase.
 */

public class MoveSiteNodeAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(MoveSiteNodeAction.class.getName());

    private Integer siteNodeId;
    private Integer parentSiteNodeId;
    private Integer newParentSiteNodeId;
    private Integer repositoryId;
    private Integer changeTypeId;
    private String hideLeafs;
   	private String userSessionKey;
    private String originalAddress;
    private String returnAddress;

    private ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
   	private SiteNodeVO siteNodeVO = new SiteNodeVO();

	public void setSiteNodeId(Integer siteNodeId)
	{
		this.siteNodeId = siteNodeId;
		this.siteNodeVO.setSiteNodeId(siteNodeId);
	}

	public void setNewParentSiteNodeId(Integer newParentSiteNodeId)
	{
		this.newParentSiteNodeId = newParentSiteNodeId;
	}

	public void setParentSiteNodeId(Integer parentSiteNodeId)
	{
		this.parentSiteNodeId = parentSiteNodeId;
	}

	public void setChangeTypeId(Integer changeTypeId)
	{
		this.changeTypeId = changeTypeId;
	}

	public Integer getParentSiteNodeId()
	{
		return this.parentSiteNodeId;
	}
	
	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}
	    
	public Integer getSiteNodeId()
	{
		return siteNodeVO.getSiteNodeId();
	}

	public Integer getNewParentSiteNodeId()
	{
		return this.newParentSiteNodeId;
	}
    
	public Integer getUnrefreshedSiteNodeId()
	{
		return this.newParentSiteNodeId;
	}

	public Integer getChangeTypeId()
	{
		return this.changeTypeId;
	}
      
    public void setHideLeafs(String hideLeafs)
    {
    	this.hideLeafs = hideLeafs;
    }

	public String getHideLeafs()
	{
		return this.hideLeafs;
	}    

	public String getUserSessionKey()
	{
		return userSessionKey;
	}

	public void setUserSessionKey(String userSessionKey)
	{
		this.userSessionKey = userSessionKey;
	}

    public String doInputV3() throws Exception
    {		
        userSessionKey = "" + System.currentTimeMillis();

		SiteNodeVO siteNodeVO = SiteNodeControllerProxy.getController().getSiteNodeVOWithId(getSiteNodeId());

        String createSiteNodeInlineOperationDoneHeader = getLocalizedString(getLocale(), "tool.structuretool.moveSiteNodeInlineOperationDoneHeader", siteNodeVO.getName());
		String createSiteNodeInlineOperationBackToCurrentPageLinkText = getLocalizedString(getLocale(), "tool.structuretool.moveSiteNodeInlineOperationBackToCurrentPageLinkText");
		String createSiteNodeInlineOperationBackToCurrentPageTitleText = getLocalizedString(getLocale(), "tool.structuretool.moveSiteNodeInlineOperationBackToCurrentPageTitleText");

	    setActionMessage(userSessionKey, createSiteNodeInlineOperationDoneHeader);
	    addActionLink(userSessionKey, new LinkBean("currentPageUrl", createSiteNodeInlineOperationBackToCurrentPageLinkText, createSiteNodeInlineOperationBackToCurrentPageTitleText, createSiteNodeInlineOperationBackToCurrentPageTitleText, this.originalAddress, false, ""));

		return "inputV3";
    }
	
    public String doExecute() throws Exception
    {
        ceb.throwIfNotEmpty();
    	
		SiteNodeControllerProxy.getSiteNodeControllerProxy().acMoveSiteNode(this.getInfoGluePrincipal(), this.siteNodeVO, this.newParentSiteNodeId);
	    
		this.returnAddress = "ViewSiteNode.action?siteNodeId=" + this.siteNodeVO.getId() + "&repositoryId=" + this.repositoryId;
		
        return "success";
    }
    
    public String doV3() throws Exception
    {
        try
        {
            ceb.throwIfNotEmpty();
        	
    		SiteNodeVO siteNodeVO = SiteNodeControllerProxy.getController().getSiteNodeVOWithId(getSiteNodeId());

    		this.parentSiteNodeId = siteNodeVO.getParentSiteNodeId();
            System.out.println("parentSiteNodeId:" + parentSiteNodeId);
            
    		SiteNodeControllerProxy.getSiteNodeControllerProxy().acMoveSiteNode(this.getInfoGluePrincipal(), siteNodeVO, this.newParentSiteNodeId);

            setActionExtraData(userSessionKey, "refreshToolbarAndMenu", "" + true);
            setActionExtraData(userSessionKey, "repositoryId", "" + this.repositoryId);
            setActionExtraData(userSessionKey, "siteNodeId", "" + newParentSiteNodeId);
            setActionExtraData(userSessionKey, "siteNodeName", "" + siteNodeVO.getName());
            setActionExtraData(userSessionKey, "unrefreshedSiteNodeId", "" + parentSiteNodeId);
            setActionExtraData(userSessionKey, "unrefreshedNodeId", "" + parentSiteNodeId);
            setActionExtraData(userSessionKey, "changeTypeId", "" + this.changeTypeId);
        }
        catch(ConstraintException ce)
        {
        	logger.warn("An error occurred so we should not complete the transaction:" + ce);

			ce.setResult(INPUT + "V3");
			throw ce;
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            throw new SystemException(e.getMessage());
        }
    	        
        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
	        String arguments 	= "userSessionKey=" + userSessionKey + "&isAutomaticRedirect=false";
	        String messageUrl 	= returnAddress + (returnAddress.indexOf("?") > -1 ? "&" : "?") + arguments;
	        
	        this.getResponse().sendRedirect(messageUrl);
	        return NONE;
        }
        else
        {
        	return "successV3";
        }
    }

    public String doAjax() throws Exception
    {
        try
        {
            ceb.throwIfNotEmpty();
        	
    		SiteNodeVO siteNodeVO = SiteNodeControllerProxy.getController().getSiteNodeVOWithId(getSiteNodeId());

    		this.parentSiteNodeId = siteNodeVO.getParentSiteNodeId();
            System.out.println("parentSiteNodeId:" + parentSiteNodeId);
            
    		SiteNodeControllerProxy.getSiteNodeControllerProxy().acMoveSiteNode(this.getInfoGluePrincipal(), siteNodeVO, this.newParentSiteNodeId);
        }
        catch(ConstraintException ce)
        {
        	logger.warn("An error occurred so we should not complete the transaction:" + ce);

            this.getResponse().setContentType("text/html");
            this.getResponse().getWriter().print("nok:" + ce.getMessage());
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            throw new SystemException(e.getMessage());
        }

        this.getResponse().setContentType("text/html");
        this.getResponse().getWriter().print("ok");

        return NONE;
    }

    public String getErrorKey()
	{
		return "SiteNode.parentSiteNodeId";
	}

    public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}

	public String getReturnAddress()
	{
		return this.returnAddress;
	}   

	public String getOriginalAddress()
	{
		return originalAddress;
	}

	public void setOriginalAddress(String originalAddress)
	{
		this.originalAddress = originalAddress;
	}

}
