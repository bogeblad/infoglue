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

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.managementtool.actions.WYSIWYGPropertiesAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ShortcutController;
import org.infoglue.cms.controllers.kernel.impl.simple.WorkflowController;
import org.infoglue.cms.entities.mydesktop.WorkflowActionVO;
import org.infoglue.cms.entities.mydesktop.WorkflowStepVO;
import org.infoglue.cms.entities.mydesktop.WorkflowVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.cms.util.workflow.StepFilter;

import webwork.action.ActionContext;

import com.opensymphony.workflow.InvalidActionException;
import com.opensymphony.workflow.WorkflowException;

/**
 * This class implements the action class for the startpage in the mydesktop tool.
 * @author Mattias Bogeblad
 * @author <a href="mailto:jedprentice@gmail.com">Jed Prentice</a>
 */
public class ViewMyDesktopToolStartPageAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewMyDesktopToolStartPageAction.class.getName());

	private static final long serialVersionUID = 6543209932597662088L;

	protected static final String INVALID_ACTION = "invalidAction";

	private static final WorkflowController controller = WorkflowController.getController();
	private static final ShortcutController shortcutController = ShortcutController.getController();
	private static final VisualFormatter formatter = new VisualFormatter();
	
	private List availableWorkflowVOList;
	private List workflowVOList;
	private List availableShortcutVOList;

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

	public List getWorkflowActionVOList()
	{
		return getAvailableActions(null);
	}

	public List getWorkflowActionVOList(StepFilter filter)
	{
		return getAvailableActions(filter);
	}

	/**
	 * Provides access to the underlying workflow
	 * @return a reference to the underlying workflow
	 */
	WorkflowVO getWorkflow()
	{
		return workflow;
	}

	/**
	 * Allows the workflow name to be set via the request parameter "workflowName".
	 * @param name the name of the desired workflow
	 */
	public void setWorkflowName(String name)
	{
		workflow.setName(name);
	}

	private String getWorkflowName()
	{
		return workflow.getName();
	}

	/**
	 * Allows the action ID to be set via the request parameter "actionId"
	 * @param actionId the ID of the action to execute
	 */
	public void setActionId(int actionId)
	{
		this.actionId = actionId;
	}

	/**
	 * Allows the workflowID to be set via the request parameter "workflowId"
	 * @param workflowId the ID of the desired workflow
	 */
	public void setWorkflowId(long workflowId)
	{
		workflow.setWorkflowId(new Long(workflowId));
	}

	private long getWorkflowId()
	{
		return workflow.getIdAsPrimitive();
	}

	/**
	 * Populates the lists of workflow and workflow action VOs.
	 * @return Action.SUCCESS
	 * @throws SystemException if a workflow error occurs
	 */
	public String doExecute() throws SystemException
	{
		populateLists();
		return SUCCESS;
	}

	/**
	 * Populates the lists of workflow and workflow action VOs.
	 * @return Action.SUCCESS
	 * @throws SystemException if a workflow error occurs
	 */
	public String doTaskList() throws SystemException
	{
		populateActiveWorkflowVOList();
		return "successTaskList";
	}

	/**
	 * Starts the workflow specified in the request parameter "workflowName" with the initial action identified by
	 * the request parameter "actionId" and redirects to the view page for the desired initial action.  If no actionId
	 * is passed in the request, we assume that the ID of the initial action is zero.
	 * @return Action.NONE if the desired initial action has a view, otherwise the effect is the same as calling
	 * doExecute()
	 * @throws SystemException
	 * @see #doExecute
	 */
	public String doStartWorkflow() throws SystemException
	{
		workflow = controller.initializeWorkflow(getInfoGluePrincipal(), getWorkflowName(), actionId, WorkflowController.createWorkflowParameters(ActionContext.getRequest()));
		return redirectToView();
	}

	/**
	 * Invokes an action in a workflow based on the values of the actionId and workflowId request parameters and
	 * redirects to the action's view if one has been defined.  If there is no action view, the user will be taken to
	 * ViewMyDesktopToolStartPage.action, i.e., the effect is the same as calling doExecute().
	 * <b>NOTE:</b> Assumes there will be one current action with a view; we iterate over the available actions until we
	 * find one with a view, then redirect to that view.  If there are other actions with different views, they will be
	 * ignored.
	 * @return NONE if there is an available action with a view and we redirect to the action's view page, otherwise
	 * performs the same operations as doExecute().
	 * @throws SystemException if an error occurs invoking the action or URL-encoding the action view
	 * @see #doExecute
	 */
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

	/**
	 * Redirects to view defined for the current action(s) in the given workflow.
	 * <b>NOTE:</b> Assumes there will be one current action with a view; we iterate over the available actions until we
	 * find one with a view, then redirect to that view.  If there are other actions with different views, they will be
	 * ignored.  If we go through the entire list without finding a view, the user will be sent to
	 * ViewMyDesktopToolStartPage.action, i.e., the effect will be the same as calling doExecute();
	 * @return NONE if there is an available action with a view and we redirect to the action's view page, otherwise
	 * performs the same operations as doExecute().
	 * @throws SystemException if an error occurs invoking the action or URL-encoding the action view
	 * @see #doExecute
	 * <b>TODO:</b> find a better heuristic for determining which view to go to.
	 */
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

	/**
	 * Populates availableWorkflowVOList, workflowVOList, and workflowActionVOList.
	 * @throws SystemException if a workflow error occurs
	 */
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

	/**
	 * Builds a list of all available actions for all workflows using the given step filter.  Assumes workflowVOList has
	 * been populated.
	 * @return a list of all available actions
	 * @throws NullPointerException if workflowVOList has not been populated
	 */
	private List getAvailableActions(StepFilter filter)
	{
		List actions = new ArrayList();
		for (Iterator workflows = workflowVOList.iterator(); workflows.hasNext();)
			actions.addAll(((WorkflowVO)workflows.next()).getAvailableActions(filter));

		return actions;
	}

	/**
	 * Creates the view URL from the given workflow action.
	 * @param action a workflow action
	 * @return the view URL
	 * @throws SystemException if an error occurs while encoding the URL
	 */
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

	/**
	 * Returns the return address
	 * @return the return address
	 * @throws SystemException if an error occurs while encoding the URL
	 */
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

	/**
	 * Returns the final return address
	 * @return the final return address
	 * @throws SystemException if an error occurs while encoding the URL
	 */
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
	
	/**
	 * Here are some ajax-methods
	 */
	
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

	/**
	 * Gets a description div of a workflow
	 */
	
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

}
