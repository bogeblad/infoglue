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
import org.infoglue.cms.controllers.kernel.impl.simple.GroupControllerProxy;

/**
 * @author Mattias Bogeblad
 */

public class DeleteGroupAction extends InfoGlueAbstractAction
{    
	private final static Logger logger = Logger.getLogger(DeleteGroupAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private String groupName;
	
	protected String doExecute() throws Exception 
	{
	    GroupControllerProxy.getController().deleteGroup(groupName);

		return "success";
	}
	
	public String doV3() throws Exception 
	{
		GroupControllerProxy.getController().deleteGroup(groupName);

		return "successV3";
	}

	public void setGroupName(String groupName) throws Exception
	{
		logger.info("groupName:" + groupName);
		byte[] bytes = Base64.decodeBase64(groupName);
		String decodedGroupName = new String(bytes, "utf-8");
		logger.info("decodedGroupName:" + decodedGroupName);
		if(GroupControllerProxy.getController().groupExists(decodedGroupName))
		{
			groupName = decodedGroupName;
		}
		else
		{
			logger.info("No match on base64-based groupName:" + groupName);
		}
		logger.info("groupName2:" + groupName);
		
	    this.groupName = groupName;
	}

}
