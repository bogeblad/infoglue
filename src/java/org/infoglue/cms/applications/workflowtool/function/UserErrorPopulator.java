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
package org.infoglue.cms.applications.workflowtool.function;

import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

import com.opensymphony.workflow.WorkflowException;

/**
 *
 */
public class UserErrorPopulator extends ErrorPopulator 
{
	/**
	 * 
	 */
	private static final String USER_ERROR_PROPERTYSET_PREFIX = ERROR_PROPERTYSET_PREFIX + "systemuser_";
	
	
	/**
	 * 
	 */
	private SystemUserVO systemUserVO;

	/**
	 * 
	 */
	public UserErrorPopulator()
	{
		super();
	}

	/**
	 * 
	 */
	protected void clean() throws WorkflowException 
	{
		clean(USER_ERROR_PROPERTYSET_PREFIX);
	}

	protected void populate() throws WorkflowException 
	{
		final ConstraintExceptionBuffer ceb = systemUserVO.validate();
		populate(ceb, null);
		if(ceb.isEmpty())
		{
			checkUniqueUserName();
		}
	}

	/**
	 * 
	 */
	protected void checkUniqueUserName() throws WorkflowException 
	{
		try
		{
			if(UserControllerProxy.getController().getUser(systemUserVO.getUserName()) != null)
			{
				setPropertySetString(USER_ERROR_PROPERTYSET_PREFIX + UserProvider.USER_NAME_ATTRIBUTE, getStringManager().getString("302"));
			}
		}
		catch(Exception e)
		{
			// ignore...
		}
	}
	
	/**
	 * Method used for initializing the function; will be called before <code>execute</code> is called.
	 * <p><strong>Note</strong>! You must call <code>super.initialize()</code> first.</p>
	 * 
	 * @throws WorkflowException if an error occurs during the initialization.
	 */
	protected void initialize() throws WorkflowException 
	{
		super.initialize();
		systemUserVO = (SystemUserVO) getParameter(UserProvider.USER_PARAMETER);
	}
}
