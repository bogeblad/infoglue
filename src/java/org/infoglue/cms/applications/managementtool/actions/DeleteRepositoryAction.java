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

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.applications.databeans.ReferenceBean;
import org.infoglue.cms.controllers.kernel.impl.simple.DeleteRepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * This action removes a repository from the system.
 * 
 * @author Mattias Bogeblad
 */

public class DeleteRepositoryAction extends InfoGlueAbstractAction
{
	private RepositoryVO repositoryVO;
	private String returnAddress = null;
	private String message = null;
	private Boolean byPassTrashcan = false;
	private Map<BaseEntityVO,List<ReferenceBean>> refs = null;

	private String processId = null;

	private VisualFormatter visualFormatter = new VisualFormatter();

	public DeleteRepositoryAction()
	{
		this(new RepositoryVO());
	}

	public DeleteRepositoryAction(RepositoryVO repositoryVO) 
	{
		this.repositoryVO = repositoryVO;
	}

	public String doMarkForDeleteChooseMethod() throws ConstraintException, Exception 
	{
		return "successChooseMethod";
	}

	public String doInput() throws ConstraintException, Exception 
	{
		this.refs = DeleteRepositoryController.getRepositoryReferences(this.repositoryVO, this.getInfoGluePrincipal());
		return "input";
	}

	public String doMarkForDelete() throws ConstraintException, Exception 
	{
		boolean hasAccessToManagementTool = hasAccessTo("ManagementTool.Read");
		if(!hasAccessToManagementTool)
			throw new AccessConstraintException("Repository.delete", "1003");

		validateSecurityCode();

		this.repositoryVO.setRepositoryId(this.getRepositoryId());
		try
		{
			RepositoryVO repoVO = RepositoryController.getController().getRepositoryVOWithId(this.repositoryVO.getId());
			String exportId = "Deleting_" + visualFormatter.escapeForAdvancedJavascripts(repoVO.getName()).replaceAll("\\.", "_") + "_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd_HHmm");
			ProcessBean processBean = ProcessBean.createProcessBean(DeleteRepositoryAction.class.getName(), exportId, getLocalizedString(getLocale(), "tool.common.delete.label") + " '" + repoVO.getName() + "'");
			//processBean.setRedirectUrl("Redirecting...", "" + "DeleteRepository!input.action?repositoryId=" + this.repositoryVO.getId());

			DeleteRepositoryController.deleteRepositories(this.repositoryVO, this.getInfoGluePrincipal(), this.byPassTrashcan, false, processBean);

			return "successRedirectToProcesses";
		}
		catch(ConstraintException ce)
		{
			returnAddress = "ViewRepository.action?repositoryId=" + this.repositoryVO.getId();
			if(ce.getErrorCode().equals("3300") && ce.getFieldName().equals("ContentVersion.stateId"))	
				throw new ConstraintException("ContentVersion.stateId", "3307", ce.getExtraInformation());
			else if(ce.getErrorCode().equals("3400") && ce.getFieldName().equals("SiteNodeVersion.stateId"))	
				throw new ConstraintException("ContentVersion.stateId", "3406", ce.getExtraInformation());
			else
				throw ce;
		}
	}
	
	public String doMarkForDeleteByForce() throws ConstraintException, Exception 
	{
		boolean hasAccessToManagementTool = hasAccessTo("ManagementTool.Read");
		if(!hasAccessToManagementTool)
			throw new AccessConstraintException("Repository.delete", "1003");

		validateSecurityCode();

		this.repositoryVO.setRepositoryId(this.getRepositoryId());
		try
		{
			RepositoryVO repoVO = RepositoryController.getController().getRepositoryVOWithId(this.repositoryVO.getId());
			String exportId = "Deleting_" + visualFormatter.escapeForAdvancedJavascripts(repoVO.getName()).replaceAll("\\.", "_") + "_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd_HHmm");
			ProcessBean processBean = ProcessBean.createProcessBean(DeleteRepositoryAction.class.getName(), exportId, getLocalizedString(getLocale(), "tool.common.delete.label") + " '" + repoVO.getName() + "'");
			DeleteRepositoryController.deleteRepositories(this.repositoryVO, this.getInfoGluePrincipal(), this.byPassTrashcan, true, processBean);

			return "successRedirectToProcesses";			
		}
		catch(ConstraintException ce)
		{
			returnAddress = "ViewRepository.action?repositoryId=" + this.repositoryVO.getId();
			if(ce.getErrorCode().equals("3300") && ce.getFieldName().equals("ContentVersion.stateId"))	
				throw new ConstraintException("ContentVersion.stateId", "3307", ce.getExtraInformation());
			else if(ce.getErrorCode().equals("3400") && ce.getFieldName().equals("SiteNodeVersion.stateId"))	
				throw new ConstraintException("ContentVersion.stateId", "3406", ce.getExtraInformation());
			else
				throw ce;
		}
	}

	protected String doExecute() throws ConstraintException, Exception 
	{
		boolean hasAccessToManagementTool = hasAccessTo("ManagementTool.Read");
		if(!hasAccessToManagementTool)
			throw new AccessConstraintException("Repository.delete", "1003");

		validateSecurityCode();

		this.repositoryVO.setRepositoryId(this.getRepositoryId());
		try
		{
			RepositoryController.getController().delete(this.repositoryVO, this.getInfoGluePrincipal(), null);

		    ViewMessageCenterAction.addSystemMessage(this.getInfoGluePrincipal().getName(), ViewMessageCenterAction.SYSTEM_MESSAGE_TYPE, "refreshRepositoryList();");

			return "success";
		}
		catch(ConstraintException ce)
		{
			returnAddress = "ViewRepository.action?repositoryId=" + this.repositoryVO.getId();
			if(ce.getErrorCode().equals("3300") && ce.getFieldName().equals("ContentVersion.stateId"))	
				throw new ConstraintException("ContentVersion.stateId", "3307", ce.getExtraInformation());
			else if(ce.getErrorCode().equals("3400") && ce.getFieldName().equals("SiteNodeVersion.stateId"))	
				throw new ConstraintException("ContentVersion.stateId", "3406", ce.getExtraInformation());
			else
				throw ce;
		}
	}

	public String doExecuteByForce() throws ConstraintException, Exception 
	{
		boolean hasAccessToManagementTool = hasAccessTo("ManagementTool.Read");
		if(!hasAccessToManagementTool)
			throw new AccessConstraintException("Repository.delete", "1003");
			
		validateSecurityCode();

	    this.repositoryVO.setRepositoryId(this.getRepositoryId());
		try
		{
			RepositoryController.getController().delete(this.repositoryVO, true, this.getInfoGluePrincipal(), null);

			ViewMessageCenterAction.addSystemMessage(this.getInfoGluePrincipal().getName(), ViewMessageCenterAction.SYSTEM_MESSAGE_TYPE, "refreshRepositoryList();");
			
			return "success";
		}
		catch(ConstraintException ce)
		{
			returnAddress = "ViewRepository.action?repositoryId=" + this.repositoryVO.getId();
			if(ce.getErrorCode().equals("3300") && ce.getFieldName().equals("ContentVersion.stateId"))	
				throw new ConstraintException("ContentVersion.stateId", "3307", ce.getExtraInformation());
			else if(ce.getErrorCode().equals("3400") && ce.getFieldName().equals("SiteNodeVersion.stateId"))	
				throw new ConstraintException("ContentVersion.stateId", "3406", ce.getExtraInformation());
			else
				throw ce;
		}
	}

	public void setRepositoryId(Integer repositoryId) throws SystemException
	{
		this.repositoryVO.setRepositoryId(repositoryId);	
	}

    public java.lang.Integer getRepositoryId()
    {
        return this.repositoryVO.getRepositoryId();
    }
        
    public void setByPassTrashcan(Boolean byPassTrashcan) throws SystemException
	{
		this.byPassTrashcan = byPassTrashcan;	
	}

    public java.lang.Boolean getByPassTrashcan()
    {
        return this.byPassTrashcan;
    }
    
    
	public String getReturnAddress() 
	{
		return this.returnAddress;
	}
	
	public String doShowProcesses() throws Exception
	{
		return "successShowProcesses";
	}

	public String doShowProcessesAsJSON() throws Exception
	{
		// TODO it would be nice we could write JSON to the OutputStream but we get a content already transmitted exception then.
		return "successShowProcessesAsJSON";
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
			 
			Iterator<ProcessBean> beanIterator = processes.iterator();
			while(beanIterator.hasNext())
			{
				ProcessBean bean = beanIterator.next();
				if(bean.getStatus() == ProcessBean.REDIRECTED || bean.getStatus() == ProcessBean.FINISHED)
				{	
					bean.setStatus(ProcessBean.FINISHED);
					bean.removeProcess();
				}
			}
		}
		catch (Throwable t)
		{
			JsonObject error = new JsonObject(); 
			error.addProperty("message", t.getMessage());
			error.addProperty("type", t.getClass().getSimpleName());
			object.add("error", error);
		}

		return gson.toJson(object);
	}
	
	public List<ProcessBean> getProcessBeans()
	{
		return ProcessBean.getProcessBeans(DeleteRepositoryAction.class.getName());
	}
	
	/**
	 * This deletes a process info bean and related files etc.
	 * @return
	 * @throws Exception
	 */	

	public String doDeleteProcessBean() throws Exception
	{
		System.out.println("this.processId:" + this.processId);
		if(this.processId != null)
		{
			ProcessBean pb = ProcessBean.getProcessBean(DeleteRepositoryAction.class.getName(), processId);
			System.out.println("pb:" + pb);
			if(pb != null)
				pb.removeProcess();
		}
		
		return "successRedirectToProcesses";
	}

	public void setProcessId(String processId) 
	{
		this.processId = processId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Map<BaseEntityVO,List<ReferenceBean>> getRefs() {
		return refs;
	}

}
