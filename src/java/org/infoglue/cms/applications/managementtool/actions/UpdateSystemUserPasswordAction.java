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
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;

import webwork.action.Action;
import webwork.action.ActionContext;

/**
 * This action makes it possible to change a users password for him/her.
 * 
 * @author Mattias Bogeblad
 */

public class UpdateSystemUserPasswordAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(UpdateSystemUserPasswordAction.class.getName());

    private String userName;
    private String oldPassword;
    private String newPassword;
    private String verifiedNewPassword;
    private String returnAddress;
    
    private boolean showInline = false;

	public String doInput()
    {
    	return Action.INPUT;
    }

    public String doInputV3()
    {
    	return Action.INPUT + "V3";
    }

    public String doInputStandalone()
    {
    	return "inputStandalone";
    }

    public String doInputStandaloneV3()
    {
    	showInline = true;
    	
    	return "inputStandaloneV3";
    }

	protected String doExecute() throws Exception 
	{
	    if(userName.equals(CmsPropertyHandler.getAnonymousUser()))
	        throw new SystemException("You must not change password on this user as it's needed by the system.");

	    if(!newPassword.equals(verifiedNewPassword))
	        throw new ConstraintException("SystemUser.newPassword", "309");
	    
	    UserControllerProxy.getController().updateUserPassword(this.userName, this.oldPassword, this.newPassword);
		
	    if(this.returnAddress != null && !this.returnAddress.equalsIgnoreCase(""))
	    {
	        ActionContext.getResponse().sendRedirect(returnAddress);
	        return Action.NONE;
	    }
        return Action.SUCCESS;
	}

	public String doV3() throws Exception 
	{
		try
		{
			if(userName.equals(CmsPropertyHandler.getAnonymousUser()))
		        throw new SystemException("You must not change password on this user as it's needed by the system.");

		    if(!newPassword.equals(verifiedNewPassword))
		        throw new ConstraintException("SystemUser.newPassword", "309");
		    
		    UserControllerProxy.getController().updateUserPassword(this.userName, this.oldPassword, this.newPassword);
		}
		catch(ConstraintException e) 
        {
			e.setResult(INPUT + "V3");
			throw e;
        }
		
	    if(this.returnAddress != null && !this.returnAddress.equalsIgnoreCase(""))
	    {
	        ActionContext.getResponse().sendRedirect(returnAddress);
	        return Action.NONE;
	    }
        return Action.SUCCESS + "V3";	    
	}

    public String getNewPassword()
    {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword)
    {
        this.newPassword = newPassword;
    }
    
    public String getOldPassword()
    {
        return oldPassword;
    }
    
    public void setOldPassword(String oldPassword)
    {
        this.oldPassword = oldPassword;
    }
    
    public String getUserName()
    {
        return userName;
    }
    
    public void setUserName(String userName) throws Exception
    {
    	logger.info("userName:" + userName);
		if(!UserControllerProxy.getController().userExists(userName))
		{
			logger.info("userName did not exist - we try to decode it:" + userName);
			byte[] bytes = Base64.decodeBase64(userName);
			String decodedName = new String(bytes, "utf-8");
			logger.info("decodedName:" + decodedName);
			if(UserControllerProxy.getController().userExists(decodedName))
			{
				logger.info("decodedName existed:" + decodedName);
				userName = decodedName;
			}
		}
		
        this.userName = userName;
    }
    
    public String getVerifiedNewPassword()
    {
        return verifiedNewPassword;
    }
    
    public void setVerifiedNewPassword(String verifiedNewPassword)
    {
        this.verifiedNewPassword = verifiedNewPassword;
    }
    
    public String getReturnAddress()
    {
        return returnAddress;
    }
    
    public void setReturnAddress(String returnAddress)
    {
        this.returnAddress = returnAddress;
    }
        
    public boolean getShowInline()
	{
		return showInline;
	}

	public void setShowInline(boolean showInline)
	{
		this.showInline = showInline;
	}

}
