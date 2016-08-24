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
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;

/**
 * @author Mattias Bogeblad
 */

public class DeleteRoleAction extends InfoGlueAbstractAction
{
	private final static Logger logger = Logger.getLogger(DeleteRoleAction.class.getName());

	private String roleName;
	
	protected String doExecute() throws Exception 
	{
		RoleControllerProxy.getController().deleteRole(roleName);

		return "success";
	}

	public String doV3() throws Exception 
	{
		RoleControllerProxy.getController().deleteRole(roleName);

		return "successV3";
	}

	public void setRoleName(String roleName) throws Exception
	{
		logger.info("roleName:" + roleName);
		byte[] bytes = Base64.decodeBase64(roleName);
		String decodedName = new String(bytes, "utf-8");
		logger.info("decodedName:" + decodedName);
		if(RoleControllerProxy.getController().roleExists(decodedName))
		{
			roleName = decodedName;
		}
		else
		{
			logger.info("No match on base64-based roleName:" + roleName);
		}
		
		this.roleName = roleName;
	}

}
