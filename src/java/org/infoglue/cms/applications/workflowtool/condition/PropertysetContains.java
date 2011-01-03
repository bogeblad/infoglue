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
package org.infoglue.cms.applications.workflowtool.condition;

import com.opensymphony.workflow.WorkflowException;

/**
 * Workflow condition used to determine if the value of a propertyset key is as expected.
 */
public class PropertysetContains extends InfoglueCondition
{
	/**
	 * The name of the property set key argument.
	 */
	private static final String NAME_ARGUMENT = "name";
	
	/**
	 * The name of the expected value argument.
	 */
	private static final String VALUE_ARGUMENT = "value";
	
	/**
	 * Default constructor.
	 */
	public PropertysetContains() 
	{ 
		super(); 
	}

	/**
	 * Determines if a condition should signal pass or fail.
	 * 
	 * @return true if the condition passes; false otherwise.
	 * @throws WorkflowException if an error (such as missing required argument) occurs while evaluating the condition.
	 */
	protected boolean passesCondition() throws WorkflowException 
	{
		final String name = getArgument(NAME_ARGUMENT);
		if(propertySetContains(name))
		{
			final String value = getPropertySet().getAsString(name);
			return (value != null) && value.equals(getArgument(VALUE_ARGUMENT));
		}
		return false;
	}
}
