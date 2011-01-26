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
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * This action deletes an systemUser through the authorizerModule.
 * 
 * @author Mattias Bogeblad
 *
 */

public class DeleteSystemUserAction extends InfoGlueAbstractAction
{
	private final static Logger logger = Logger.getLogger(DeleteSystemUserAction.class.getName());

	private SystemUserVO systemUserVO;
	
	public DeleteSystemUserAction()
	{
		this(new SystemUserVO());
	}

	public DeleteSystemUserAction(SystemUserVO systemUserVO) 
	{
		this.systemUserVO = systemUserVO;
	}
	

	protected String doExecute() throws Exception 
	{
	    if(this.systemUserVO.getUserName().equals(CmsPropertyHandler.getAnonymousUser()))
	        throw new SystemException("You must not remove the anonymous user as it's needed by the system.");
		
	    UserControllerProxy.getController().deleteUser(this.systemUserVO.getUserName());			
		
		return "success";
	}

	public String doV3() throws Exception 
	{
		doExecute();		
		
		return "successV3";
	}

	public void setUserName(String userName) throws Exception
	{
		logger.info("userName:" + userName);
		byte[] bytes = Base64.decodeBase64(userName);
		String decodedName = new String(bytes, "utf-8");
		logger.info("decodedName:" + decodedName);
		if(UserControllerProxy.getController().userExists(decodedName))
		{
			userName = decodedName;
		}
		else
		{
			logger.info("No match on base64-based userName:" + userName);
		}
		
		this.systemUserVO.setUserName(userName);	
	}

    public String getUserName()
    {
        return this.systemUserVO.getUserName();
    }
        
	
}
