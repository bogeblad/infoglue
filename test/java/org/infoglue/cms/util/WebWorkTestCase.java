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
 * $Id: WebWorkTestCase.java,v 1.3 2006/03/06 16:54:41 mattias Exp $
 */
package org.infoglue.cms.util;

import java.util.Map;

import org.infoglue.cms.applications.common.actions.WebworkAbstractAction;

import webwork.action.Action;
import webwork.action.ActionContext;

/**
 * Base ServletTestCase that sets up WebWork facilities so that WebWork
 * Actions can be effectively tested.
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public abstract class WebWorkTestCase extends InfoGlueTestCase
{
	//---------------------------------------------------------------------
	// Bunch of helper methods for testing Action.execute() results
	//---------------------------------------------------------------------
	public static void assertSuccess(Action action) throws Exception
	{
		assertSuccess(action.execute());
	}

	public static void assertSuccess(WebworkAbstractAction action, String command) throws Exception
	{
		action.setCommand(command);
		assertSuccess(action.execute());
	}

	public static void assertSuccess(String result)
	{
		assertResult(Action.SUCCESS, result);
	}

	public static void assertError(Action action) throws Exception
	{
		assertError(action.execute());
	}

	public static void assertError(WebworkAbstractAction action, String command) throws Exception
	{
		action.setCommand(command);
		assertError(action.execute());
	}

	public static void assertError(String result)
	{
		assertResult(Action.ERROR, result);
	}

	public static void assertInput(Action action) throws Exception
	{
		assertInput(action.execute());
	}

	public static void assertInput(WebworkAbstractAction action, String command) throws Exception
	{
		action.setCommand(command);
		assertInput(action.execute());
	}

	public static void assertInput(String result)
	{
		assertResult(Action.INPUT, result);
	}

	public static void assertNone(Action action) throws Exception
	{
		assertNone(action.execute());
	}

	public static void assertNone(WebworkAbstractAction action, String command) throws Exception
	{
		action.setCommand(command);
		assertNone(action.execute());
	}

	public static void assertNone(String result)
	{
		assertResult(Action.NONE, result);
	}

	public static void assertResult(String expected, String result)
	{
		assertEquals("Wrong result:", expected, result);
	}

	/**
	 * Sets the "request" parameters on the ActionContext
	 */
	protected void setParameters(Map params)
	{
		ActionContext.setParameters(params);
	}

	/**
	 * Sets the "request" parameters (all single, no arrays) on the ActionContext
	 */
	protected void setSingleValueParameters(Map params)
	{
		ActionContext.setSingleValueParameters(params);
	}
}
