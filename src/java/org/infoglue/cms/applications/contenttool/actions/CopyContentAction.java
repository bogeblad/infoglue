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

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import javax.transaction.SystemException;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.applications.managementtool.actions.ImportRepositoryAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.CopyContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.CopyRepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

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
	private String processId = null;


	private VisualFormatter visualFormatter = new VisualFormatter();

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
		String exportId = "Copy_Content_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd_HHmm");
		ProcessBean processBean = ProcessBean.createProcessBean(ImportRepositoryAction.class.getName(), exportId);
		
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
        
        Long totalSize = ContentController.getContentController().getContentWeightTotal(contentId, true);
        if(totalSize > (500 * 1000 * 1000))
        	throw new SystemException("Folder / content is to large to copy. Please clean some versions or copy subparts.");
		
		CopyContentController.copyContent(this.getInfoGluePrincipal(), contentId, newParentContentId, maxAssetSize, onlyLatestVersions, processBean);

		//ContentControllerProxy.getController().acCopyContent(this.getInfoGluePrincipal(), contentId, newParentContentId, maxAssetSize, onlyLatestVersions);

		return "successRedirectToProcesses";
		
		/*
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
        
        Long totalSize = ContentController.getContentController().getContentWeightTotal(contentId, true);
        if(totalSize > (500 * 1000 * 1000))
        	throw new SystemException("Folder / content is to large to copy. Please clean some versions or copy subparts.");
		
		ContentControllerProxy.getController().acCopyContent(this.getInfoGluePrincipal(), contentId, newParentContentId, maxAssetSize, onlyLatestVersions);

        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
	        String arguments	= "userSessionKey=" + userSessionKey + "&isAutomaticRedirect=false";
	        String messageUrl	= returnAddress + (returnAddress.indexOf("?") > -1 ? "&" : "?") + arguments;

	        this.getResponse().sendRedirect(messageUrl);
	        return NONE;
        }
        else
        {
        	return "success";
        }
        */
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
	
	
	/**
	 * This deletes a process info bean and related files etc.
	 * @return
	 * @throws Exception
	 */	

	public String doDeleteProcessBean() throws Exception
	{
		if(this.processId != null)
		{
			ProcessBean pb = ProcessBean.getProcessBean(ImportRepositoryAction.class.getName(), processId);
			if(pb != null)
				pb.removeProcess();
		}
		
		return "successRedirectToProcesses";
	}

	/**
	 * This refreshes the view.
	 * @return
	 * @throws Exception
	 */	

	public String doShowProcesses() throws Exception
	{
		return "successShowProcesses";
	}

	public String doShowProcessesAsJSON() throws Exception
	{
		// TODO it would be nice we could write JSON to the OutputStream but we get a content already transmitted exception then.
		return "successShowProcessesAsJSON";
	}
	
	public void setProcessId(String processId) 
	{
		this.processId = processId;
	}

	public List<ProcessBean> getProcessBeans()
	{
		return ProcessBean.getProcessBeans(ImportRepositoryAction.class.getName());
	}
	
	public String getStatusAsJSON()
	{
		Gson gson = new GsonBuilder()
			.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
			.setDateFormat("dd MMM HH:mm:ss").create();
		JsonObject object = new JsonObject();

		try
		{
			List<ProcessBean> processes = getProcessBeans();
			Type processBeanListType = new TypeToken<List<ProcessBean>>() {}.getType();
			JsonElement list = gson.toJsonTree(processes, processBeanListType);
			object.add("processes", list);
			object.addProperty("memoryMessage", getMemoryUsageAsText());
		}
		catch (Throwable t)
		{
			logger.error("Error when generating repository export status report as JSON.", t);
			JsonObject error = new JsonObject(); 
			error.addProperty("message", t.getMessage());
			error.addProperty("type", t.getClass().getSimpleName());
			object.add("error", error);
		}

		return gson.toJson(object);
	}
}
