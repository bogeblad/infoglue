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
 * $Id: WorkflowControllerTest.java,v 1.3 2006/03/06 16:54:01 mattias Exp $
 */
package org.infoglue.cms.controllers;

import java.util.List;

import org.infoglue.cms.controllers.kernel.impl.simple.WorkflowController;
import org.infoglue.cms.entities.mydesktop.WorkflowStepVO;
import org.infoglue.cms.entities.mydesktop.WorkflowVO;
import org.infoglue.cms.workflow.NewsWorkflowTestCase;

/**
 * @author <a href=mailto:jedprentice@gmail.com>Jed Prentice</a>
 * @version $Revision: 1.3 $ $Date: 2006/03/06 16:54:01 $
 */
public class WorkflowControllerTest extends NewsWorkflowTestCase
{
	protected void tearDown() throws Exception
	{
		finishWorkflow();
	}
	
	public void testGetAvailableWorkflowVOList() throws Exception
	{
		List workflows = WorkflowController.getController().getAvailableWorkflowVOList(getUserPrincipal());
		assertEquals("Wrong number of available workflows:", 2, workflows.size());

		WorkflowVO workflow = findWorkflowByName(workflows);
		assertNull("There should not be a workflow ID", workflow.getWorkflowId());
		assertEquals("Wrong name:", getWorkflowName(), workflow.getName());
		assertEquals("Wrong number of declared steps:", 3, workflow.getDeclaredSteps().size());
		assertTrue("There should be no current steps:", workflow.getCurrentSteps().isEmpty());
		assertTrue("There should be no history steps:", workflow.getHistorySteps().isEmpty());
		assertTrue("There should be no steps:", workflow.getSteps().isEmpty());
		assertTrue("There should be no available actions:", workflow.getAvailableActions().isEmpty());
		assertTrue("There should be no global actions:", workflow.getGlobalActions().isEmpty());
	}

	public void testGetCurrentWorkflowVOList() throws Exception
	{
		List workflows = WorkflowController.getController().getCurrentWorkflowVOList(getUserPrincipal());
		assertFalse("There should be at least one active workflow", workflows.isEmpty());
		assertNotNull("Current workflow should be in the list", findCurrentWorkflow());
	}

	public void testGetAllSteps() throws Exception
	{
		assertEquals("Wrong number of steps:", 3,
						 WorkflowController.getController().getAllSteps(getUserPrincipal(), getWorkflowId()).size());
	}

	public void testGetCurrentSteps() throws Exception
	{
		List steps = WorkflowController.getController().getCurrentSteps(getUserPrincipal(), getWorkflowId());
		assertEquals("Wrong number of steps:", 1, steps.size());
		assertEquals("Wrong name:", "Create news content", ((WorkflowStepVO)steps.get(0)).getName());

		invokeCreateNews();
		steps = WorkflowController.getController().getCurrentSteps(getUserPrincipal(), getWorkflowId());
		assertEquals("Wrong number of steps:", 1, steps.size());
		assertEquals("Wrong name:", "Preview news and approve", ((WorkflowStepVO)steps.get(0)).getName());
	}

	public void testGetHistorySteps() throws Exception
	{
		assertEquals("Wrong number of steps:", 0,
						 WorkflowController.getController().getHistorySteps(getUserPrincipal(), getWorkflowId()).size());

		invokeCreateNews();
		List steps = WorkflowController.getController().getHistorySteps(getUserPrincipal(), getWorkflowId());
		assertEquals("Wrong number of steps:", 1, steps.size());
		assertEquals("Wrong name:", "Create news content", ((WorkflowStepVO)steps.get(0)).getName());
	}
}
