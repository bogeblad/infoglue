/* ===============================================================================
*
* Part of the InfoGlue Content Management Platform (www.infoglue.org)
*
* ===============================================================================
*
* Copyright (C) Mattias Bogeblad
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
* $Id: WorkflowVOTest.java,v 1.5 2006/03/06 16:54:41 mattias Exp $
*/
package org.infoglue.cms.entities.mydesktop;

import java.util.Iterator;

import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.InfoGlueTestCase;
import org.infoglue.cms.util.workflow.OwnerStepFilter;
import org.infoglue.cms.util.workflow.StepFilter;

public class WorkflowVOTest extends InfoGlueTestCase
{
	private static final StepFilter adminFilter = new OwnerStepFilter(getAdminPrincipal());
	private static final StepFilter userFilter = new OwnerStepFilter(getCmsUserPrincipal());

	private WorkflowVO workflow = new WorkflowVO();

	protected void setUp() throws Exception
	{
		workflow.getCurrentSteps().add(createStep(getAdminPrincipal()));
		workflow.getCurrentSteps().add(createStep(getCmsUserPrincipal()));
		workflow.getCurrentSteps().add(new WorkflowStepVO());

		for (Iterator steps = workflow.getCurrentSteps().iterator(); steps.hasNext();)
			((WorkflowStepVO)steps.next()).addAction(new WorkflowActionVO());

		for (int i = 0; i < 2; ++i)
			workflow.getInitialActions().add(new WorkflowActionVO(new Integer(i)));
	}

	public void testGetAvailableActions() throws Exception
	{
		assertEquals("Wrong number of available actions:", 3, workflow.getAvailableActions().size());
	}

	public void testGetAvailableActionsFiltered() throws Exception
	{
		assertEquals("Wrong number of admin actions:", 3, workflow.getAvailableActions(adminFilter).size());
		assertEquals("Wrong number of user actions:", 2, workflow.getAvailableActions(userFilter).size());
	}

	public void testGetCurrentStepsFiltered() throws Exception
	{
		assertEquals("Wrong number of admin steps:", 3, workflow.getCurrentSteps(adminFilter).size());
		assertEquals("Wrong number of user steps:", 2, workflow.getCurrentSteps(userFilter).size());
	}

	public void testGetInitialAction() throws Exception
	{
		for (int i = 0; i < workflow.getInitialActions().size(); ++i)
		{
			WorkflowActionVO action= (WorkflowActionVO)workflow.getInitialActions().get(i);
			assertEquals("Wrong action at " + i + ": ", action, workflow.getInitialAction(action.getId()));
		}
	}

	public void testGetInitialActionNonexistent() throws Exception
	{
		try
		{
			workflow.getInitialAction(new Integer(-1));
			fail("IllegalArgumentException should have been thrown");
		}
		catch (IllegalArgumentException e)
		{
			// Expected
		}
	}

	public void testGetInitialActionNull() throws Exception
	{
		try
		{
			workflow.getInitialAction(null);
			fail("NullPointerException should have been thrown");
		}
		catch (NullPointerException e)
		{
			// Expected
		}
	}

	private static WorkflowStepVO createStep(InfoGluePrincipal owner)
	{
		WorkflowStepVO step = new WorkflowStepVO();
		step.setOwner(owner.getName());
		return step;
	}
}
