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

package org.infoglue.cms.applications.common.actions;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.applications.managementtool.actions.DeleteRepositoryAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.TrashcanController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import webwork.action.Action;


/** 
 * This class contains methods to handle the trashcan and the item's in it.
 */

public class TrashcanAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(TrashcanAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	//private static SubscriptionController subscriptionsController = SubscriptionController.getController();
	private List<RepositoryVO> repositoriesMarkedForDeletion = new ArrayList<RepositoryVO>();
	private List<ContentVO> contentsMarkedForDeletion = new ArrayList<ContentVO>();
	private List<SiteNodeVO> siteNodesMarkedForDeletion = new ArrayList<SiteNodeVO>();
	
	private Integer repositoryFilter = null;
	
	private String entity = "";
	private Integer entityId = -1;
	private Boolean updateParent = false;	
	private Integer updateEntityId = -1;
	
	private String processId = null;

	private VisualFormatter visualFormatter = new VisualFormatter();

	protected String doExecute() throws Exception
    {
		logUserActionInfo(getClass(), "doExecute");
		this.repositoriesMarkedForDeletion = RepositoryController.getController().getRepositoryVOListMarkedForDeletion(getInfoGluePrincipal());
		this.contentsMarkedForDeletion = ContentController.getContentController().getContentVOListMarkedForDeletion(repositoryFilter, getInfoGluePrincipal(), this.repositoriesMarkedForDeletion);
		this.siteNodesMarkedForDeletion = SiteNodeController.getController().getSiteNodeVOListMarkedForDeletion(repositoryFilter, getInfoGluePrincipal(), this.repositoriesMarkedForDeletion);
		
		return Action.SUCCESS;
    }

	public String doRestore() throws Exception
    {
		logUserActionInfo(getClass(), "doRestore");
		String exportId = "Empty_Trashcan_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd_HHmm");
		ProcessBean processBean = ProcessBean.createProcessBean(TrashcanAction.class.getName(), exportId, "Restore items from trashcan");
		
		TrashcanController.restoreEntity(entity, entityId, this.getInfoGluePrincipal(), processBean);
		
		return "successRedirectToProcesses";
    }

	public String doDelete() throws Exception
    {
		logUserActionInfo(getClass(), "doDelete");
		validateSecurityCode();
		
		String exportId = "Empty_Trashcan_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd_HHmm");
		ProcessBean processBean = ProcessBean.createProcessBean(TrashcanAction.class.getName(), exportId, "Delete items from trashcan");
		
		TrashcanController.deleteEntity(entity, entityId, this.getInfoGluePrincipal(), processBean);
		
		return "successRedirectToProcesses";
    }

	public String doEmpty() throws Exception
    {
		logUserActionInfo(getClass(), "doEmpty");
		validateSecurityCode();

		String exportId = "Empty_Trashcan_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd_HHmm");
		ProcessBean processBean = ProcessBean.createProcessBean(TrashcanAction.class.getName(), exportId, "Empty entire trashcan");
		
		TrashcanController.emptyTrashcan(this.repositoryFilter, this.getInfoGluePrincipal(), processBean);
		
		return "successRedirectToProcesses";
    }

	public List<RepositoryVO> getRepositoriesMarkedForDeletion()
	{
		return repositoriesMarkedForDeletion;
	}

	public List<ContentVO> getContentsMarkedForDeletion()
	{
		return contentsMarkedForDeletion;
	}

	public List<SiteNodeVO> getSiteNodesMarkedForDeletion()
	{
		return siteNodesMarkedForDeletion;
	}

	public String getEntity()
	{
		return entity;
	}

	public Integer getEntityId()
	{
		return entityId;
	}

	public void setEntity(String entity)
	{
		this.entity = entity;
	}

	public void setEntityId(Integer entityId)
	{
		this.entityId = entityId;
	}
    
	public Integer getRepositoryFilter()
	{
		return repositoryFilter;
	}

	public void setRepositoryFilter(Integer repositoryFilter)
	{
		this.repositoryFilter = repositoryFilter;
	}

	public Boolean getUpdateParent() 
	{
		return updateParent;
	}

	public Integer getUpdateEntityId() 
	{
		return updateEntityId;
	}
	
	public String doShowProcesses() throws Exception
	{
		logUserActionInfo(getClass(), "doShowProcesses");
		return "successShowProcesses";
	}

	public String doShowProcessesAsJSON() throws Exception
	{
		logUserActionInfo(getClass(), "doShowProcessesAsJSON");
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
		return ProcessBean.getProcessBeans(TrashcanAction.class.getName());
	}
	
	/**
	 * This deletes a process info bean and related files etc.
	 * @return
	 * @throws Exception
	 */	

	public String doDeleteProcessBean() throws Exception
	{
		logUserActionInfo(getClass(), "doDeleteProcessBean");
		if(this.processId != null)
		{
			ProcessBean pb = ProcessBean.getProcessBean(DeleteRepositoryAction.class.getName(), processId);
			if(pb != null)
				pb.removeProcess();
		}
		
		return "successRedirectToProcesses";
	}

}