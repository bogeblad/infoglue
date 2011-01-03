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

import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.tasktool.actions.ViewExecuteTaskAction;
import org.infoglue.cms.applications.workflowtool.util.InfoglueWorkflowBase;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.WorkflowException;

/**
 * Base class for all infoglue workflow conditions.
 */
public abstract class InfoglueCondition extends InfoglueWorkflowBase implements Condition 
{
    private final static Logger logger = Logger.getLogger(InfoglueCondition.class.getName());

	/**
	 * Default constructor.
	 */
	protected InfoglueCondition() 
	{ 
		super(); 
	}

	/**
	 * Determines if a condition should signal pass or fail.
	 * 
	 * @param transientVars the transient variables of the current execution context.
	 * @param args the arguments of the function.
	 * @param ps the propertyset associated with the current workflow.
	 * @return true if the condition passes; false otherwise.
	 * @throws WorkflowException if an error (such as missing required argument) occurs while evaluating the condition.
	 */
	public final boolean passesCondition(final Map transientVars, final Map args, final PropertySet ps) throws WorkflowException 
	{
		boolean result = false;
		try 
		{
			storeContext(transientVars, args, ps);
			logger.debug(getClass().getName() + ".passesCondition()--------- START");
			initialize();
			result = passesCondition();
			logger.debug(getClass().getName() + ".passesCondition()--------- STOP (" + result + ")");
		}
		catch(Exception e)
		{
			throwException(e);
		}
		return result;
	}
	
	/**
	 * Determines if a condition should signal pass or fail.
	 * 
	 * @return true if the condition passes; false otherwise.
	 * @throws WorkflowException if an error (such as missing required argument) occurs while evaluating the condition.
	 */
	protected abstract boolean passesCondition() throws WorkflowException;
}
