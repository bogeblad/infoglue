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
 *
 * $Id: ViewMyDesktopToolStartPageActionTest.java,v 1.7 2006/03/06 16:54:01 mattias Exp $
 */
package org.infoglue.cms.applications.mydesktoptool.actions;

import java.util.Iterator;
import java.util.List;

import org.infoglue.cms.applications.common.Session;
import org.infoglue.cms.controllers.kernel.impl.simple.WorkflowController;
import org.infoglue.cms.entities.mydesktop.WorkflowActionVO;
import org.infoglue.cms.entities.mydesktop.WorkflowVO;
import org.infoglue.cms.util.FakeHttpServletRequest;
import org.infoglue.cms.util.FakeHttpServletResponse;
import org.infoglue.cms.util.WebWorkTestCase;
import org.infoglue.cms.util.WorkflowTestCase;

import webwork.action.Action;
import webwork.action.ActionContext;

import com.opensymphony.module.propertyset.PropertySet;

/**
 * @author <a href="mailto:jedprentice@gmail.com">Jed Prentice</a>
 */
public class ViewMyDesktopToolStartPageActionTest extends WebWorkTestCase
{
	private static final WorkflowController controller = WorkflowController.getController();

	private ViewMyDesktopToolStartPageAction action = new ViewMyDesktopToolStartPageAction();
	private FakeHttpServletRequest request = new FakeHttpServletRequest();

	protected void setUp() throws Exception
	{
		super.setUp();
		new Session().setInfoGluePrincipal(getAdminPrincipal());

		ActionContext.setRequest(request);
		action.setServletRequest(request);
		action.setServletResponse(new FakeHttpServletResponse());
	}

	public void testExecute() throws Exception
	{
		assertSuccess(action.doExecute());
		assertEquals("Wrong number of available workflows:", 2, action.getAvailableWorkflowVOList().size());
	}

	public void testExecute2ActiveWorkflows() throws Exception
	{
		WorkflowVO workflow1 = controller.initializeWorkflow(getAdminPrincipal(), "Create News", 0, WorkflowController.createWorkflowParameters(request));
		WorkflowVO workflow2 = controller.initializeWorkflow(getAdminPrincipal(), "Create User", 0, WorkflowController.createWorkflowParameters(request));

		try
		{
			assertSuccess(action.doExecute());
			assertEquals("Wrong number of available workflows:", 2, action.getAvailableWorkflowVOList().size());
			assertTrue("There should be at least 2 current workflows", action.getWorkflowVOList().size() >= 2);

			List availableActions = action.getWorkflowActionVOList();
			assertTrue("There should be at least 2 current actions", availableActions.size() >= 2);
			assertContains(availableActions, createCreateNews(workflow1.getWorkflowId()));
			assertContains(availableActions, createRegisterUser(workflow2.getWorkflowId()));
		}
		finally
		{
			finishWorkflow(workflow1.getIdAsPrimitive());
			finishWorkflow(workflow2.getIdAsPrimitive());
		}
	}

	public void testStartWorkflowCreateNews() throws Exception
	{
		startCreateNews();

		try
		{
			assertNull("Available workflows should be null:", action.getAvailableWorkflowVOList());
			assertNull("Current workflows should be null:", action.getWorkflowVOList());
		}
		finally
		{
			finishWorkflow();
		}
	}

	public void testStartWorkflowCreateUser() throws Exception
	{
		startWorkflow("Create User", Action.SUCCESS);

		try
		{
			assertFalse("There should be at least 1 current workflow", action.getWorkflowVOList().isEmpty());
			List workflowActions = action.getWorkflowActionVOList();
			assertFalse("There should be at least 1 current action", workflowActions.isEmpty());
			assertContains(workflowActions, createRegisterUser(action.getWorkflow().getWorkflowId()));
		}
		finally
		{
			finishWorkflow();
		}
	}

	public void testInvokeCreateNews() throws Exception
	{
		startCreateNews();

		try
		{
			assertNone(invokeCreateNews());
			assertNull("Available workflows should be null:", action.getAvailableWorkflowVOList());
			assertNull("Current workflows should be null:", action.getWorkflowVOList());

			PropertySet propertySet = controller.getPropertySet(getAdminPrincipal(), action.getWorkflow().getIdAsPrimitive());
			assertEquals("Wrong name:", request.getParameter("name"), propertySet.getString("name"));
			assertEquals("Wrong title:", request.getParameter("title"), propertySet.getString("title"));
			assertEquals("Wrong navigationTitle:", request.getParameter("navigationTitle"), propertySet.getString("navigationTitle"));
			assertEquals("Wrong leadIn:", request.getParameter("leadIn"), propertySet.getString("leadIn"));
			assertEquals("Wrong fullText:", request.getParameter("fullText"), propertySet.getString("fullText"));
		}
		finally
		{
			finishWorkflow();
		}
	}

	public void testInvokeCreateNewsInactive() throws Exception
	{
		testInvokeCreateNews();
		assertResult(ViewMyDesktopToolStartPageAction.INVALID_ACTION, invokeCreateNews());
	}

	public void testInvokeCreateNewsTwice() throws Exception
	{
		startCreateNews();

		try
		{
			assertNone(invokeCreateNews());
			assertResult(ViewMyDesktopToolStartPageAction.INVALID_ACTION, invokeCreateNews());
		}
		finally
		{
			finishWorkflow();
		}
	}

	/**
	 * Sets up the action for "Create News" and calls action.doInvoke()
	 * @return the result of action.doInvoke()
	 * @throws Exception if an error occurs
	 */
	private String invokeCreateNews() throws Exception
	{
		request.setParameter("name", getName());
		request.setParameter("title", getName());
		request.setParameter("navigationTitle", getName());
		request.setParameter("leadIn", getName());
		request.setParameter("fullText", getName());

		action.setWorkflowId(action.getWorkflow().getIdAsPrimitive());
		action.setActionId(4);
		return action.doInvoke();
	}

	private void startCreateNews() throws Exception
	{
		startWorkflow("Create News", Action.NONE);
	}

	private void startWorkflow(String name, String expectedResult) throws Exception
	{
		action.setWorkflowName(name);
		assertResult(expectedResult, action.doStartWorkflow());
	}

	private void finishWorkflow() throws Exception
	{
		finishWorkflow(action.getWorkflow().getIdAsPrimitive());
	}

	private void finishWorkflow(long workflowId) throws Exception
	{
		controller.invokeAction(getAdminPrincipal(), workflowId, WorkflowTestCase.FINISH_WORKFLOW, WorkflowController.createWorkflowParameters(request));
	}

	private static void assertContains(List actions, WorkflowActionVO expected)
	{
		boolean containsAction = false;
		for (Iterator i = actions.iterator(); i.hasNext() && !containsAction;)
			if (isSameAction(expected, (WorkflowActionVO)i.next()))
				containsAction = true;

		assertTrue("Action " + expected.getId() + ' ' + expected.getName() + " should be in the list of workflow actions",
					  containsAction);
	}

	/**
	 * A loose check to see if 2 actions are the same.  Compares id, workflowId, and name.  For now, this is sufficient.
	 * @param one an action
	 * @param another another action
	 * @return true if both actions have the same id, workflowId, and name; otherwise returns false.
	 */
	private static boolean isSameAction(WorkflowActionVO one, WorkflowActionVO another)
	{
		return one.getId().equals(another.getId())
				&& one.getWorkflowId().equals(another.getWorkflowId())
				&& one.getWorkflowId().equals(another.getWorkflowId())
				&& one.getName().equals(another.getName());
	}

	private static WorkflowActionVO createCreateNews(Long workflowId)
	{
		return new WorkflowActionVO(new Integer(4), workflowId, "Create news content");
	}

	private static WorkflowActionVO createRegisterUser(Long workflowId)
	{
		return new WorkflowActionVO(new Integer(4), workflowId, "Register Name 1");
	}
}
