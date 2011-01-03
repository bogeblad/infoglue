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

import org.infoglue.cms.controllers.kernel.impl.simple.GroupControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.cms.util.workflow.OwnerFactory;

import com.opensymphony.workflow.WorkflowException;

/**
 * This function is used for creating an owner object that can be used to set the owner in the workflow definition file. 
 * <p>
 * If the <code>group</code> argument is specified, then all users that are members of both the role and the group will be owners.
 * Otherwise all users that are members of the role will be owners. 
 * </p>
 * <p>
 *   <code>
 *     &lt;result old-status="Finished" status="Queued" step="1" owner="${<strong>owner</strong>}"&gt;
 *   </code>
 * </p>
 * <h1 class="workflow">Context in</h1>
 * <table class="workflow">
 *   <thead class="workflow">
 *     <tr class="workflow"><th class="workflow">Name</th><th class="workflow">Type</th><th class="workflow">Class</th><th class="workflow">Required</th><th class="workflow">Default</th><th class="workflow">Comments</th></tr>
 *   </thead>
 *   <tbody class="workflow">
 *     <tr class="workflow"><td class="workflow">role</td><td class="workflow">argument</td><td class="workflow">String</td><td class="workflow">true</td><td class="workflow">-</td><td class="workflow_comment">The name of the InfoGlueRole.</td></tr>
 *     <tr class="workflow"><td class="workflow">group</td><td class="workflow">argument</td><td class="workflow">String</td><td class="workflow">false</td><td class="workflow">-</td><td class="workflow_comment">The name of the InfoGlueGroup.</td></tr>
 *   </tbody>
 * </table>
 * <h1 class="workflow">Context out</h1>
 * <table class="workflow">
 *   <thead class="workflow">
 *     <tr class="workflow"><th class="workflow">Name</th><th class="workflow">Type</th><th class="workflow">Class</th><th class="workflow">Comments</th></tr>
 *   </thead>
 *   <tbody class="workflow">
 *     <tr class="workflow"><td class="workflow">owner</td><td class="workflow">parameter</td><td class="workflow">org.infoglue.cms.util.workflow.Owner</td><td class="workflow_comment">The owner object.</td></tr>
 *   </tbody>
 * </table>
 */
public class Owner extends InfoglueFunction 
{
	/**
	 * The key used by the <code>owner</code> in the <code>parameters</code>.
	 */
	private static final String OWNER_PARAMETER = "owner";
	
	/**
	 * The name of the role argument.
	 */
	private static final String ROLE_ARGUMENT = "role";
	
	/**
	 * The name of the group argument.
	 */
	private static final String GROUP_ARGUMENT = "group";
	
	/**
	 * The name of the owner role.
	 */
	private String roleName;
	
	/**
	 * The name of the owner group (optional).
	 */
	private String groupName;
	
	/**
	 * Default constructor.
	 */
	public Owner() 
	{
		super();
	}

	/**
	 * Checks if the group is specified, creates the owner object and stores it in the parameters.
	 * 
	 * @throws WorkflowException if an error occurs during the execution.
	 */
	protected void execute() throws WorkflowException 
	{
		try 
		{
			final InfoGlueRole role = RoleControllerProxy.getController(getDatabase()).getRole(roleName);
			if(groupName == null)
			{
				setParameter(OWNER_PARAMETER, OwnerFactory.create(role).getIdentifier());
			}
			else
			{
				final InfoGlueGroup group = GroupControllerProxy.getController(getDatabase()).getGroup(groupName);
				setParameter(OWNER_PARAMETER, OwnerFactory.create(role, group).getIdentifier());
			}
		}
		catch(Exception e)
		{
			throwException(e);
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
		this.roleName  = getArgument(ROLE_ARGUMENT);
		this.groupName = getArgument(GROUP_ARGUMENT, null);
	}
}
