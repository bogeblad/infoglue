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

package org.infoglue.cms.applications.mydesktoptool.actions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ShortcutController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.WorkflowController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.mydesktop.WorkflowActionVO;
import org.infoglue.cms.entities.mydesktop.WorkflowStepVO;
import org.infoglue.cms.entities.mydesktop.WorkflowVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.cms.util.workflow.StepFilter;

import webwork.action.ActionContext;

import com.opensymphony.workflow.InvalidActionException;
import com.opensymphony.workflow.WorkflowException;

/**
 * This class implements the action class for the startpage in the mydesktop tool.
 * 
 * @author Mattias Bogeblad
 */

public class ViewMyDesktopAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewMyDesktopAction.class.getName());

	private static final long serialVersionUID = 6543209932597662088L;

	protected static final String INVALID_ACTION = "invalidAction";

	private static final WorkflowController controller = WorkflowController.getController();
	private static final ShortcutController shortcutController = ShortcutController.getController();
	private static final VisualFormatter formatter = new VisualFormatter();
	
	private List availableWorkflowVOList;
	private List workflowVOList;
	private List availableShortcutVOList;
	private Set<SiteNodeVO> siteNodeVOListLastModifiedByPincipal = null;
	private Set<ContentVO> contentVOListLastModifiedByPincipal = null;
	private List<SiteNodeVO> expiringPagesForPrincipal = null;
	private List<ContentVO> expiringContentsForPrincipal = null;
	
	public String doExecute()
	{
		try
		{
			populateActiveWorkflowVOList();
			this.availableWorkflowVOList = controller.getAvailableWorkflowVOList(getInfoGluePrincipal());
			this.availableShortcutVOList = shortcutController.getAvailableShortcutVOList(getInfoGluePrincipal());
			this.siteNodeVOListLastModifiedByPincipal = SiteNodeController.getController().getSiteNodeVOListLastModifiedByPincipal(getInfoGluePrincipal());
			this.contentVOListLastModifiedByPincipal = ContentController.getContentController().getContentVOListLastModifiedByPincipal(getInfoGluePrincipal(), new String[] {"Meta info"});

			this.expiringPagesForPrincipal = SiteNodeController.getController().getUpcomingExpiringSiteNodes(30, getInfoGluePrincipal());
			this.expiringContentsForPrincipal = ContentController.getContentController().getUpcomingExpiringContents(30, getInfoGluePrincipal(), new String[] {"Meta info"});
		}
		catch (Exception e) 
		{
			logger.warn("An error occurred getting ongoing workflows: " + e.getMessage(), e);
		}
		
		return "success";
	}
	
	public String doAvailable()
	{
		try
		{
			availableWorkflowVOList = controller.getAvailableWorkflowVOList(getInfoGluePrincipal());
		}
		catch (Exception e) 
		{
			logger.warn("An error occurred getting available workflows: " + e.getMessage(), e);
		}
		
		return "available";
	}

	public String doOngoing()
	{
		try
		{
			populateActiveWorkflowVOList();
		}
		catch (Exception e) 
		{
			logger.warn("An error occurred getting ongoing workflows: " + e.getMessage(), e);
		}
		
		return "ongoing";
	}

	public String doShortcuts()
	{
		try
		{
			availableShortcutVOList = shortcutController.getAvailableShortcutVOList(getInfoGluePrincipal());
		}
		catch (Exception e) 
		{
			logger.warn("An error occurred getting shortcuts: " + e.getMessage(), e);
		}
		
		return "shortcuts";
	}

	public String doLatestPages()
	{
		try
		{
			this.siteNodeVOListLastModifiedByPincipal = SiteNodeController.getController().getSiteNodeVOListLastModifiedByPincipal(getInfoGluePrincipal());
		}
		catch (Exception e) 
		{
			logger.error("An error occurred getting my latest pages: " + e.getMessage(), e);
		}
		
		return "latestPages";
	}

	public String doLatestContents()
	{
		try
		{
			this.contentVOListLastModifiedByPincipal = ContentController.getContentController().getContentVOListLastModifiedByPincipal(getInfoGluePrincipal(), new String[] {"Meta info"});
		}
		catch (Exception e) 
		{
			logger.error("An error occurred getting my latest contents: " + e.getMessage(), e);
		}
		
		return "latestContents";
	}

	private void populateActiveWorkflowVOList() throws SystemException
	{
		final String showAllWorkflows = CmsPropertyHandler.getShowAllWorkflows();
		if(showAllWorkflows == null || showAllWorkflows.equalsIgnoreCase("true"))
		{
			workflowVOList = controller.getCurrentWorkflowVOList(getInfoGluePrincipal());
		}
		else
		{
			workflowVOList = controller.getMyCurrentWorkflowVOList(getInfoGluePrincipal());
		}
	}

	public List getAvailableWorkflowVOList()
	{
		return availableWorkflowVOList;
	}

	public List getWorkflowVOList()
	{
		return workflowVOList;
	}

	public List getAvailableShortcutVOList()
	{
		return availableShortcutVOList;
	}

	public List<SiteNodeVO> getSiteNodeVOListLastModifiedByPincipal()
	{
		List<SiteNodeVO> siteNodeVOList = new ArrayList<SiteNodeVO>();
		siteNodeVOList.addAll(siteNodeVOListLastModifiedByPincipal);
		Collections.sort(siteNodeVOList, new ReflectionComparator("id"));

		return siteNodeVOList;
	}

	public List<ContentVO> getContentVOListLastModifiedByPincipal()
	{
		List<ContentVO> contentVOList = new ArrayList<ContentVO>();
		contentVOList.addAll(contentVOListLastModifiedByPincipal);
		Collections.sort(contentVOList, new ReflectionComparator("id"));

		return contentVOList;
	}

	public List<SiteNodeVO> getExpiringPagesForPrincipal()
	{
		return expiringPagesForPrincipal;
	}

	public List<ContentVO> getExpiringContentsForPrincipal()
	{
		return expiringContentsForPrincipal;
	}

	public List getWorkflowActionVOList()
	{
		return getAvailableActions(null);
	}

	public List getWorkflowActionVOList(StepFilter filter)
	{
		return getAvailableActions(filter);
	}

	private List getAvailableActions(StepFilter filter)
	{
		List actions = new ArrayList();
		for (Iterator workflows = workflowVOList.iterator(); workflows.hasNext();)
			actions.addAll(((WorkflowVO)workflows.next()).getAvailableActions(filter));

		return actions;
	}

	/*
	

	private WorkflowVO workflow = new WorkflowVO();
	private int actionId;
	private String finalReturnAddress = "";
	
	public List getWorkflowVOList()
	{
		Collections.sort(workflowVOList, new ReflectionComparator("workflowId"));
		return workflowVOList;
	}

	public List getAvailableWorkflowVOList()
	{
		return availableWorkflowVOList;
	}

    public List getAvailableShortcutVOList()
    {
        return availableShortcutVOList;
    }

	WorkflowVO getWorkflow()
	{
		return workflow;
	}

	public void setWorkflowName(String name)
	{
		workflow.setName(name);
	}

	private String getWorkflowName()
	{
		return workflow.getName();
	}

	public void setActionId(int actionId)
	{
		this.actionId = actionId;
	}

	public void setWorkflowId(long workflowId)
	{
		workflow.setWorkflowId(new Long(workflowId));
	}

	private long getWorkflowId()
	{
		return workflow.getIdAsPrimitive();
	}

	public String doExecute() throws SystemException
	{
		populateLists();
		return SUCCESS;
	}

	public String doTaskList() throws SystemException
	{
		populateActiveWorkflowVOList();
		return "successTaskList";
	}

	public String doStartWorkflow() throws SystemException
	{
		workflow = controller.initializeWorkflow(getInfoGluePrincipal(), getWorkflowName(), actionId, WorkflowController.createWorkflowParameters(ActionContext.getRequest()));
		return redirectToView();
	}

	public String doInvoke() throws SystemException
	{
		logger.info("****************************************");
		logger.info("workflowId:" + getWorkflowId());
		logger.info("actionId:" + actionId);
		logger.info("****************************************");

		try
		{
			if(this.finalReturnAddress != null && !this.finalReturnAddress.equals(""))
			{
				logger.info("Final return address get's set to " + this.finalReturnAddress);
				controller.getPropertySet(getInfoGluePrincipal(), getWorkflowId()).setString("finalReturnAddress", this.finalReturnAddress);
			}
			else
			{
				String finalReturnAddressCandidate = controller.getPropertySet(getInfoGluePrincipal(), getWorkflowId()).getString("finalReturnAddress");
				logger.info("finalReturnAddressCandidate " + finalReturnAddressCandidate);
				if(finalReturnAddressCandidate != null && !finalReturnAddressCandidate.equals(""))
				{
					this.finalReturnAddress = finalReturnAddressCandidate;
					logger.info("Setting Final return address get's set to " + this.finalReturnAddress + " from properties..");
				}
			}

			workflow = controller.invokeAction(getInfoGluePrincipal(), getWorkflowId(), actionId, WorkflowController.createWorkflowParameters(ActionContext.getRequest()));
			return redirectToView();
		}
		catch (InvalidActionException e)
		{
			logger.error("An error occurred when invoking an action:" + e.getMessage(), e);
			return INVALID_ACTION;
		}
		catch (WorkflowException e)
		{
			throw new SystemException(e);
		}
	}

	private String redirectToView() throws SystemException
	{
	    for (Iterator i = workflow.getAvailableActions().iterator(); i.hasNext();)
		{
			String url = getViewUrl((WorkflowActionVO)i.next());
			if (url.length() > 0)
				return redirect(url);
		}

	    if(this.finalReturnAddress != null && !this.finalReturnAddress.equals(""))
			return redirect(finalReturnAddress);
			
		logger.info("No action view, coming back to mydesktop...");
		return doExecute();
	}

	private void populateLists() throws SystemException
	{
		availableWorkflowVOList = controller.getAvailableWorkflowVOList(getInfoGluePrincipal());
		final String showAllWorkflows = CmsPropertyHandler.getShowAllWorkflows();
		if(showAllWorkflows == null || showAllWorkflows.equalsIgnoreCase("true"))
		{
			workflowVOList = controller.getCurrentWorkflowVOList(getInfoGluePrincipal());
		}
		else
		{
			workflowVOList = controller.getMyCurrentWorkflowVOList(getInfoGluePrincipal());
		}
		
		availableShortcutVOList = shortcutController.getAvailableShortcutVOList(getInfoGluePrincipal());
	}


	private String getViewUrl(WorkflowActionVO action) throws SystemException
	{
		if (!action.hasView())
			return "";

		StringBuffer buffer = new StringBuffer(action.getView());
		if (containsQuestionMark(action.getView()))
			buffer.append('&');
		else
			buffer.append('?');

		return buffer.append("workflowId=").append(getWorkflowId()).append("&actionId=").append(action.getId())
				.append("&returnAddress=").append(getReturnAddress()).append("&finalReturnAddress=").append(getFinalReturnAddress()).append('&')
				.append(getRequest().getQueryString()).toString();
	}

	private static boolean containsQuestionMark(String s)
	{
		return s.indexOf("?") >= 0;
	}

	private String getReturnAddress() throws SystemException
	{
		try
		{
			String cmsFullBaseUrl = CmsPropertyHandler.getCmsFullBaseUrl();
			logger.info("cmsFullBaseUrl:" + cmsFullBaseUrl);
			if(cmsFullBaseUrl != null && !cmsFullBaseUrl.equals(""))
				return URLEncoder.encode(cmsFullBaseUrl + "/ViewMyDesktopToolStartPage!invoke.action", "UTF-8");
			else
				return URLEncoder.encode(getURLBase() + "/ViewMyDesktopToolStartPage!invoke.action", "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new SystemException(e);
		}
	}

	private String redirect(String url) throws SystemException
	{
		try
		{
			logger.info("Url in doInvoke:" + url);
			getResponse().sendRedirect(url);
			return NONE;
		}
		catch (IOException e)
		{
			throw new SystemException(e);
		}
	}

	private String getFinalReturnAddress() throws SystemException
	{
		try
		{
			return URLEncoder.encode(this.finalReturnAddress, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new SystemException(e);
		}
	}

	public void setFinalReturnAddress(String finalReturnAddress)
	{
		if(finalReturnAddress != null && !finalReturnAddress.equals("null"))
		{
			this.finalReturnAddress = finalReturnAddress;
		}
	}
	
	
	public String doGetActiveWorkflowProperties() throws Exception
	{
		StringBuffer sb = new StringBuffer();
		
		String activeWorkflowId = getRequest().getParameter("activeWorkflowId");
		
		populateActiveWorkflowVOList();
		
		List currentWorkflowVOList = workflowVOList;
		Iterator activeWorkflowVOListIterator = currentWorkflowVOList.iterator();
		while(activeWorkflowVOListIterator.hasNext())
		{
			WorkflowVO workflowVO = (WorkflowVO)activeWorkflowVOListIterator.next();
			if(activeWorkflowId.equals(workflowVO.getId().toString()))
			{
				sb.append("<div id=\"activeWorkflowDetailsProperties\" class=\"propertiesDiv\" style=\"z-index:10\">");
				sb.append("	<div id=\"activeWorkflowDetailsPropertiesHandle\" class=\"propertiesDivHandle\">");
				sb.append("		<div id=\"propertiesDivLeftHandle\" class=\"propertiesDivLeftHandle\" style=\"width: 300px;\">" + workflowVO.getName() + "&nbsp;-&nbsp;#" + workflowVO.getId() + "</div><div id=\"propertiesDivRightHandle\" class=\"propertiesDivRightHandle\"><a href=\"javascript:hideDiv('activeWorkflowDetailsProperties');\" class=\"white\">close</a></div>");
				sb.append("	</div>");
				sb.append("	<div id=\"activeWorkflowDetailsPropertiesBody\" class=\"propertiesDivBody\">");
				sb.append("		<table border=\"0\" cellpadding=\"4\" cellspacing=\"0\" width=\"100%\">");
				
				Iterator stepsIterator = workflowVO.getSteps().iterator();
				while(stepsIterator.hasNext())
				{
					WorkflowStepVO workflowStepVO = (WorkflowStepVO)stepsIterator.next();
					
					sb.append("		<tr>");
					sb.append("			<td style=\"" + (workflowStepVO.getFinishDate() == null ? "color: black;" : "color: silver;") + "\">" + workflowStepVO.getName() + "</td>");
					sb.append("			<td style=\"" + (workflowStepVO.getFinishDate() == null ? "color: black;" : "color: silver;") + "\">" + (workflowStepVO.getOwner() != null ? workflowStepVO.getOwner() : "Not specified") + "</td>");
					sb.append("			<td style=\"" + (workflowStepVO.getFinishDate() == null ? "color: black;" : "color: silver;") + "\">" + (workflowStepVO.getCaller() != null ? workflowStepVO.getCaller() : "Not specified") + "</td>");
					sb.append("			<td style=\"" + (workflowStepVO.getFinishDate() == null ? "color: black;" : "color: silver;") + "\">" + workflowStepVO.getStatus() + "</td>");
					sb.append("			<td style=\"" + (workflowStepVO.getFinishDate() == null ? "color: black;" : "color: silver;") + "\">" + (workflowStepVO.getStartDate() == null ? "Not started" : formatter.formatDate(workflowStepVO.getStartDate(), "yyyy-MM-dd")) + "</td>");
					sb.append("			<td style=\"" + (workflowStepVO.getFinishDate() == null ? "color: black;" : "color: silver;") + "\">" + (workflowStepVO.getFinishDate() == null ? "Not completed" : formatter.formatDate(workflowStepVO.getFinishDate(), "yyyy-MM-dd")) + "</td>");
					sb.append("		</tr>");
				}
				
				sb.append("		</table>");
				sb.append("		</div>");
				sb.append("	</div>");
				break;
			}
		}	
		
		this.getResponse().setContentType("text/plain");
        this.getResponse().getWriter().println(sb.toString());
		
		return NONE;
	}

	public String doGetAvailableWorkflowProperties() throws Exception
	{
		StringBuffer sb = new StringBuffer();
		
		String workflowName = getRequest().getParameter("workflowName");

		try
		{
			List availableWorkflowVOList = controller.getAvailableWorkflowVOList(getInfoGluePrincipal());
			
			Iterator availableWorkflowVOListIterator = availableWorkflowVOList.iterator();
			while(availableWorkflowVOListIterator.hasNext())
			{
				WorkflowVO availableWorkflowVO = (WorkflowVO)availableWorkflowVOListIterator.next();
				if(workflowName.equals(availableWorkflowVO.getName()))
				{
					sb.append("<div id=\"availableWorkflowDetailsProperties\" class=\"propertiesDiv\" style=\"z-index: 10;\">");
					sb.append("	<div id=\"availableWorkflowDetailsPropertiesHandle\" class=\"propertiesDivHandle\">");
					sb.append("		<div id=\"propertiesDivLeftHandle\" class=\"propertiesDivLeftHandle\">" + availableWorkflowVO.getName() + "</div><div id=\"propertiesDivRightHandle\" class=\"propertiesDivRightHandle\"><a href=\"javascript:hideDiv('availableWorkflowDetailsProperties');\" class=\"white\">close</a></div>");
					sb.append("	</div>");
					sb.append("	<div id=\"availableWorkflowDetailsPropertiesBody\" class=\"propertiesDivBody\">");
					sb.append("		<table border=\"0\" cellpadding=\"4\" cellspacing=\"0\" width=\"100%\">");
						
					Iterator workflowStepVOIterator = availableWorkflowVO.getDeclaredSteps().iterator();
					while(workflowStepVOIterator.hasNext())
					{		
						WorkflowStepVO workflowStepVO = (WorkflowStepVO)workflowStepVOIterator.next();
						
						sb.append("		<tr style=\"background-color: white;\">");
						sb.append("			<td>" + workflowStepVO.getName() + "</td>");
						sb.append("			<td>" + (workflowStepVO.getOwner() != null ? workflowStepVO.getOwner() : "Not specified") + "</td>");
						sb.append("		</tr>");
					
						Iterator workflowActionVOIterator = workflowStepVO.getActions().iterator();
						while(workflowActionVOIterator.hasNext())
						{
							WorkflowActionVO workflowActionVO = (WorkflowActionVO)workflowActionVOIterator.next();
	
							sb.append("<tr style=\"background-color: #eeeeee;\">");
							sb.append("	<td style=\"padding-left: 20px; font-size:10px;\">" + workflowActionVO.getName() + "</td>");
							sb.append("		<td style=\"padding-left: 20px; font-size:10px;\"><!--" + workflowActionVO.getView() + "--></td>");
							sb.append("	</tr>");
									
						}
					}
					sb.append("		</table>");
					sb.append("	</div>");
					sb.append("</div>");
				}
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		this.getResponse().setContentType("text/plain");
		this.getResponse().getWriter().println(sb.toString());
		
		return NONE;
	}
	*/
}
