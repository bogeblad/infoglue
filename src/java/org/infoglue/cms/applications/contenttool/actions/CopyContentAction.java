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
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;

/**
 * This action represents the Copy Content Usecase.
 */

public class CopyContentAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
    private final static Logger logger = Logger.getLogger(CopyContentAction.class.getName());

    private Integer contentId;
    private Integer newParentContentId;
    private Integer repositoryId;
    private Integer maxAssetSize = -1;
	private String onlyLatestVersions = "false";
	protected List repositories = null;

   	private String userSessionKey;
    private String originalAddress;
    private String returnAddress;

	public void setContentId(Integer contentId)
	{
		this.contentId = contentId;
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public void setNewParentContentId(Integer newParentContentId)
	{
		this.newParentContentId = newParentContentId;
	}

	public Integer getContentId()
	{
		return this.contentId;
	}

	public Integer getNewParentContentId()
	{
		return this.newParentContentId;
	}
    
	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}

	public Integer getMaxAssetSize() 
	{
		return maxAssetSize;
	}

	public void setMaxAssetSize(Integer maxAssetSize) 
	{
		this.maxAssetSize = maxAssetSize;
	}

	public String getOnlyLatestVersions() 
	{
		return onlyLatestVersions;
	}

	public void setOnlyLatestVersions(String onlyLatestVersions) 
	{
		this.onlyLatestVersions = onlyLatestVersions;
	}

    public List getRepositories()
    {
        return repositories;
    }

    public String doInput() throws Exception
    {    	
		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
    	this.returnAddress = "ViewInlineOperationMessages.action"; //ViewContent!V3.action?contentId=" + contentId + "&repositoryId=" + this.repositoryId;
    	return "input";
    }
	
    public String doExecute() throws Exception
    {
        userSessionKey = "" + System.currentTimeMillis();

        String copyContentInlineOperationDoneHeader = getLocalizedString(getLocale(), "tool.contenttool.copyContentsInlineOperationDoneHeader");
		String copyContentInlineOperationBackToCurrentPageLinkText = getLocalizedString(getLocale(), "tool.contenttool.copyContentsInlineOperationBackToCurrentContentLinkText");
		String copyContentInlineOperationBackToCurrentPageTitleText = getLocalizedString(getLocale(), "tool.contenttool.copyContentsInlineOperationBackToCurrentContentTitleText");
		
	    setActionMessage(userSessionKey, copyContentInlineOperationDoneHeader);
	    addActionLink(userSessionKey, new LinkBean("currentPageUrl", copyContentInlineOperationBackToCurrentPageLinkText, copyContentInlineOperationBackToCurrentPageTitleText, copyContentInlineOperationBackToCurrentPageTitleText, this.originalAddress, false, ""));
        setActionExtraData(userSessionKey, "refreshToolbarAndMenu", "" + true);
        setActionExtraData(userSessionKey, "repositoryId", "" + this.repositoryId);
        setActionExtraData(userSessionKey, "contentId", "" + newParentContentId);
        setActionExtraData(userSessionKey, "unrefreshedContentId", "" + newParentContentId);
        setActionExtraData(userSessionKey, "unrefreshedNodeId", "" + newParentContentId);
        setActionExtraData(userSessionKey, "changeTypeId", "" + 3);
        setActionExtraData(userSessionKey, "confirmationMessage", getLocalizedString(getLocale(), "tool.contenttool.contentCopied.confirmation", getContentVO(newParentContentId).getName()));

        if(this.newParentContentId == null)
        {
    		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
            return "input";
        }
        
		ContentControllerProxy.getController().acCopyContent(this.getInfoGluePrincipal(), contentId, newParentContentId, maxAssetSize, onlyLatestVersions);
		
        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
	        String arguments 	= "userSessionKey=" + userSessionKey + "&isAutomaticRedirect=false";
	        String messageUrl 	= returnAddress + (returnAddress.indexOf("?") > -1 ? "&" : "?") + arguments;
	        
	        this.getResponse().sendRedirect(messageUrl);
	        return NONE;
        }
        else
        {
        	return "success";
        }
    }
    
	public String getUserSessionKey()
	{
		return userSessionKey;
	}

	public void setUserSessionKey(String userSessionKey)
	{
		this.userSessionKey = userSessionKey;
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

	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}
}
