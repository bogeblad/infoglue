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
package org.infoglue.cms.util.workflow;

import java.util.Iterator;

import org.infoglue.cms.applications.common.Session;
import org.infoglue.cms.entities.mydesktop.WorkflowStepVO;
import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * Filters steps according to owner.  If a step has no owner, it is assumed that anyone can view it.
 * An administrator can view all steps regardless of owner.
 * @author <a href="mailto:jedprentice@gmail.com">Jed Prentice</a>
 * @version $Revision: 1.10 $ $Date: 2006/03/06 17:28:55 $
 */
public class OwnerStepFilter implements StepFilter
{
	private final InfoGluePrincipal userPrincipal;

	/**
	 * Constructs an OwnerStepFilter from the user associated with the current session/action context
	 */
	public OwnerStepFilter()
	{
		this(new Session().getInfoGluePrincipal());
	}

	/**
	 * Constructs an owner step filter with the given user principal
	 * @param userPrincipal an InfoGluePrincipal representing a user
	 */
	public OwnerStepFilter(InfoGluePrincipal userPrincipal)
	{
		this.userPrincipal = userPrincipal;
	}

	/**
	 * Indicates whether the step is allowed
	 * @param step a workflow step.
	 * @return true if the step is owned by the current user, the current user is an administrator, or if the owner
	 * of the step is null; otherwise returns false.
	 */
	public boolean isAllowed(WorkflowStepVO step)
	{
		if(!step.hasOwner() || isUserAdministrator())
		{
			return true;
		}
		for(final Iterator owners = OwnerFactory.createAll(userPrincipal).iterator(); owners.hasNext(); )
		{
			final Owner owner = (Owner) owners.next();
			if(step.isOwner(owner.getIdentifier()))
			{
				return true;
			}
		}
		return false;
	}

	protected boolean isUserAdministrator()
	{
		return userPrincipal.getIsAdministrator();
	}

	protected InfoGluePrincipal getUserPrincipal()
	{
		return userPrincipal;
	}
}
