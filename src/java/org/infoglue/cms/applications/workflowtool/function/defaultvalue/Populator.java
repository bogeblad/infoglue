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
package org.infoglue.cms.applications.workflowtool.function.defaultvalue;

import org.infoglue.cms.applications.workflowtool.function.InfoglueFunction;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public abstract class Populator extends InfoglueFunction 
{
	/**
	 * 
	 */
	private static final String NAME_ARGUMENT = "name";

	/**
	 * 
	 */
	private static final String VALUE_ARGUMENT = "value";

	
	
	/**
	 * 
	 */
	protected void execute() throws WorkflowException 
	{
		final String name  = getArgument(NAME_ARGUMENT, null);
		final String value = getArgument(VALUE_ARGUMENT, null);
		
		if(name == null)
		{
			populate();
		}
		else if(value != null)
		{
			populate(name, value);
		}
		else
		{
			populate(name);
		}
	}

	/**
	 * 
	 */
	protected void doPopulate(final String name, final String value) throws WorkflowException 
	{
		setPropertySetDataString(name, value);
	}

	/**
	 * 
	 */
	protected abstract void populate() throws WorkflowException;

	/**
	 * 
	 */
	protected abstract void populate(final String name) throws WorkflowException;
	
	/**
	 * 
	 */
	protected abstract void populate(final String name, final String value) throws WorkflowException;
}
