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

package org.infoglue.cms.applications.managementtool.actions;

import java.util.Collections;
import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;
import org.infoglue.cms.util.sorters.ReflectionComparator;


/**
 * @author Mattias Bogeblad
 * 
 *	Action class for usecase ViewListGroup
 * 
 */

public class ViewListGroupAction extends InfoGlueAbstractAction 
{
	private static final long serialVersionUID = 1L;

	private List groups;
		
	protected String doExecute() throws Exception 
	{
	    this.groups = GroupControllerProxy.getController().getAllGroups();

	    return "success";
	}
	
	public String doListManagableGroups() throws Exception 
	{
		this.groups = GroupControllerProxy.getController().getAvailableGroups(this.getInfoGluePrincipal(), "Group.ManageUsers");
	    
	    return "successV3";
	}
	
	public List getGroups()
	{
		return this.groups;		
	}

	
}
