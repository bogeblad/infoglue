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

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.infoglue.cms.security.InfoGluePrincipal;

import com.opensymphony.workflow.WorkflowException;

/**
 * This function is used when an email should be sent to a number of <code>SystemUser</code>:s.
 */
public abstract class UsersAddressProvider extends AddressProvider 
{
    private final static Logger logger = Logger.getLogger(UsersAddressProvider.class.getName());

	/**
	 * Default constructor. 
	 */
	public UsersAddressProvider() 
	{
		super();
	}

	/**
	 * Returns the principals that should be the recipients of the email.
	 * 
	 * @return a Collection of <code>InfogluePrincipal</code>:s.
	 */
	protected abstract Collection getPrincipals() throws WorkflowException;
	
	/**
	 * Add all recipients. Note that empty email-addresses will be discarded
	 * if the <code>required</code> attribute is <code>false</code>.
	 */
	protected void populate() throws WorkflowException 
	{
		for(final Iterator principals = getPrincipals().iterator(); principals.hasNext(); )
		{
			final InfoGluePrincipal principal = (InfoGluePrincipal) principals.next();
			logger.debug("Creating email for user [" + principal.getName() + "].");
			addRecipient(principal.getEmail());
		}
	}
}