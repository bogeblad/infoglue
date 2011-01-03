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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;

import com.opensymphony.workflow.WorkflowException;

public class RolesProvider extends InfoglueFunction 
{
    private final static Logger logger = Logger.getLogger(RolesProvider.class.getName());

	/**
	 * 
	 */
	public static final String ROLES_PARAMETER = "roles";
	
	/**
	 * 
	 */
	private static final String ROLES_PROPERTYSET_PREFIX = "roles_";
	
	/**
	 * 
	 */
	private static final String MODE_ARGUMENT = "mode";
	
	/**
	 * 
	 */
	private static final int REQUEST_MODE = 0;
	
	/**
	 * 
	 */
	private static final String REQUEST_MODE_NAME = "request";
	
	/**
	 * 
	 */
	private static final int ARGUMENT_MODE = 1;
	
	/**
	 * 
	 */
	private static final String ARGUMENT_MODE_NAME = "argument";
	
	/**
	 * 
	 */
	private static final int PRINCIPAL_MODE = 2;
	
	/**
	 * 
	 */
	private static final String PRINCIPAL_MODE_NAME = "principal";
	
	/**
	 * 
	 */
	private static final String ROLES_ARGUMENT = "roles";

	/**
	 * 
	 */
	private int mode;
	
	/**
	 * 
	 */
	private List roles = new ArrayList();
	
	/**
	 * 
	 */
	public RolesProvider()
	{
		super();
	}
	
	/**
	 * 
	 */
	protected void execute() throws WorkflowException 
	{
		switch(mode)
		{
		case ARGUMENT_MODE:
			populateRolesFromAttribute();
			break;
		case PRINCIPAL_MODE:
			populateRolesFromPrincipal();
			break;
		default:
			populateRolesFromRequest();
			break;
		}
		setParameter(ROLES_PARAMETER, roles);
	}
	
	/**
	 * 
	 */
	private void populateRolesFromAttribute() throws WorkflowException
	{
		logger.debug("Populating from attribute.");
		for(final StringTokenizer st = new StringTokenizer(getArgument(ROLES_ARGUMENT), ","); st.hasMoreTokens(); )
		{
			populateRole(st.nextToken());
		}
	}
	
	/**
	 * 
	 */
	private void populateRolesFromPrincipal() throws WorkflowException
	{
		logger.debug("Populating from principal.");
		for(final Iterator i = getPrincipal().getRoles().iterator(); i.hasNext(); )
		{
			roles.add(i.next());
		}
	}
	
	/**
	 * 
	 */
	private void populateRolesFromRequest() throws WorkflowException
	{
		logger.debug("Populating from request.");
		for(final Iterator i = getParameters().keySet().iterator(); i.hasNext(); ) 
		{
			final String key = i.next().toString();
			if(key.startsWith(ROLES_PROPERTYSET_PREFIX))
			{
				populateRole(key.substring(ROLES_PROPERTYSET_PREFIX.length()));
			}
		}
	}

	/**
	 * 
	 */
	private void populateRole(final String roleName) throws WorkflowException
	{
		try
		{
			roles.add(RoleControllerProxy.getController(getDatabase()).getRole(roleName));
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
		this.mode = getMode(getArgument(MODE_ARGUMENT, REQUEST_MODE_NAME));
	}
	
	/**
	 * 
	 */
	private int getMode(final String modeName)
	{
		final Map modes = new HashMap();
		modes.put(REQUEST_MODE_NAME, new Integer(REQUEST_MODE));
		modes.put(ARGUMENT_MODE_NAME, new Integer(ARGUMENT_MODE));
		modes.put(PRINCIPAL_MODE_NAME, new Integer(PRINCIPAL_MODE));
		return modes.containsKey(modeName) ? ((Integer) modes.get(modeName)).intValue() : REQUEST_MODE;
	}
}
