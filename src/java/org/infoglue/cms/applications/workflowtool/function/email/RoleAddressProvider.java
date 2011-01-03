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
package org.infoglue.cms.applications.workflowtool.function.email;

import java.util.ArrayList;
import java.util.Collection;

import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;

import com.opensymphony.workflow.WorkflowException;

/**
 * This function is used when an email should be sent to all members of a role.
 */
public class RoleAddressProvider extends UsersAddressProvider {
	/**
	 * The name of the role argument.
	 */
	private static final String ROLE_ARGUMENT = "role";
	
	/**
	 * The name of the role whose members should recieve an email.
	 */
	private String roleName;
	
	/**
	 * Default constructor.
	 */
	public RoleAddressProvider() 
	{
		super();
	}
	
	/**
	 * Returns the principals that should be the recipients of the email.
	 * 
	 * @return a Collection of <code>InfogluePrincipal</code>:s.
	 */
	protected Collection getPrincipals() throws WorkflowException
	{
		try 
		{
			return RoleControllerProxy.getController(getDatabase()).getInfoGluePrincipals(roleName);
		}
		catch(Exception e)
		{
			throwException(e);
		}
		return new ArrayList();
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
		roleName = getArgument(ROLE_ARGUMENT);
	}
}
