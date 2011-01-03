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

package org.infoglue.cms.applications.mydesktoptool.actions;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.WorkflowController;
import org.infoglue.cms.exception.SystemException;

/**
 * This class implements the action class for starting a new workflow of a certain type.
 * @deprecated This class is no longer used; it should be removed in the next release, along with the corresponding
 * action element in actions.xml.
 * @author Mattias Bogeblad
 */

public class CreateWorkflowInstanceAction extends InfoGlueAbstractAction
{
	private String name;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String doExecute() throws SystemException
	{
		WorkflowController.getController().initializeWorkflow(this.getInfoGluePrincipal(), this.name, 0, null);
		return SUCCESS;
	}
}
