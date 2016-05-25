/**
 * 
 */
package org.infoglue.cms.applications.managementtool.actions;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.CmsSessionContextListener;

import webwork.action.Action;
import webwork.action.ActionContext;

/**
 * @author Erik Stenbäcka <stenbacka@gmail.com>
 *
 */
public class UpdateSystemUserUserNameAction extends InfoGlueAbstractAction
{
	private static final Logger logger = Logger.getLogger(UpdateSystemUserUserNameAction.class);
	private String userName;
	private String newUserName;
	private String returnAddress;

	private boolean hasAccessTo(InfoGluePrincipal principal, String interceptionPointName, boolean returnSuccessIfInterceptionPointNotDefined)
	{
		logger.info("Checking if " + principal.getName() + " has access to " + interceptionPointName);

		try
		{
			return AccessRightController.getController().getIsPrincipalAuthorized(principal, interceptionPointName, returnSuccessIfInterceptionPointNotDefined);
		}
		catch (SystemException e)
		{
		    logger.warn("Error checking access rights", e);
			return false;
		}
	}

	public String doInput() throws Exception
	{
		logUserActionInfo(getClass(), "doInput");
		boolean hasAccessToRenameSystemUser = hasAccessTo(getInfoGluePrincipal(), "SystemUser.changeUsername", false);
		if (!hasAccessToRenameSystemUser) // getInfoGluePrincipal().getIsAdministrator()
		{
			throw new SystemException("You are not allowed to change user names");
		}

		return Action.INPUT;
	}

	public String doExecute() throws Exception
	{
		logUserActionInfo(getClass(), "doExecute");
		if(userName.equals(CmsPropertyHandler.getAnonymousUser()) || userName.equals(CmsPropertyHandler.getAdministratorUserName()))
		{
	        throw new SystemException("You must not change the user name on this user as it's needed by the system.");
		}

		boolean hasAccessToRenameSystemUser = hasAccessTo(getInfoGluePrincipal(), "SystemUser.changeUsername", false);
		if (!hasAccessToRenameSystemUser) // getInfoGluePrincipal().getIsAdministrator()
		{
			throw new SystemException("You are not allowed to change user names");
		}

		logger.debug("About to change user name for user: " + userName);
		UserControllerProxy.getController().changeUserName(userName, newUserName);

		CmsSessionContextListener.invalidateSession(userName);

		if(this.returnAddress != null && !this.returnAddress.equalsIgnoreCase(""))
	    {
	        ActionContext.getResponse().sendRedirect(returnAddress);
	        return Action.NONE;
	    }
	    else
	    {
	        return Action.SUCCESS;
	    }
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

	public String getNewUserName()
	{
		return newUserName;
	}

	public void setNewUserName(String newUserName)
	{
		this.newUserName = newUserName;
	}

	public String getReturnAddress()
    {
        return returnAddress;
    }

    public void setReturnAddress(String returnAddress)
    {
        this.returnAddress = returnAddress;
    }

}
