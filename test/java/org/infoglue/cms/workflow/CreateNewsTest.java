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
 * $Id: CreateNewsTest.java,v 1.9 2006/03/06 16:55:12 mattias Exp $
 */
package org.infoglue.cms.workflow;

import java.util.Iterator;
import java.util.Map;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidActionException;

/**
 * Tests the Create News sample workflow
 * @author <a href=mailto:jedprentice@gmail.com>Jed Prentice</a>
 */
public class CreateNewsTest extends NewsWorkflowTestCase
{
	public void testCreateNewsAndApprove() throws Exception
	{
		checkCreateNews();
		checkPreviewNewsAndApprove();
	}

	public void testCreateNewsAndApproveInactive() throws Exception
	{
		testCreateNewsAndApprove();

		try
		{
			invokeCreateNews();
			fail("InvalidActionException should have been thrown");
		}
		catch (InvalidActionException e)
		{
			// Expected
		}
	}

	public void testCreateNewsTwice() throws Exception
	{
		checkCreateNews();

		try
		{
			invokeCreateNews();
			fail("InvalidActionException should have been thrown");
		}
		catch (InvalidActionException e)
		{
			// Expected
		}
		finally
		{
			finishWorkflow();
		}
	}

	private void checkPreviewNewsAndApprove() throws Exception
	{
		invokePreviewNewsAndApprove();
		assertWorkflowFinished();
	}

	private void checkCreateNews() throws Exception
	{
		invokeCreateNews();
		checkWorkflow(1, 1, 1);

		PropertySet propertySet = getPropertySet();
		Map params = getCreateNewsParams();
		for (Iterator names = params.keySet().iterator(); names.hasNext();)
		{
			String name = (String)names.next();
			assertEquals("Wrong " + name + ':', params.get(name), propertySet.getString(name));
		}
	}
}
