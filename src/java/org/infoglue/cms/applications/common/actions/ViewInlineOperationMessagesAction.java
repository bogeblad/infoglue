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

package org.infoglue.cms.applications.common.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.databeans.LinkBean;

/**
 * This action is used as a jump-point from inline actions in deliver edit on sight back to other actions.
 *
 * @author Mattias Bogeblad
 * @author Johan Dahlgren
 */

public class ViewInlineOperationMessagesAction extends InfoGlueAbstractAction 
{
	private static final long serialVersionUID = -739264056619967471L;
	private final static Logger logger = Logger.getLogger(ViewInlineOperationMessagesAction.class.getName());
    
    private String message;
    private String userSessionKey;
    private List<LinkBean> actionLinks;
    private boolean isAutomaticRedirect = false;
    private boolean automaticClose = false;
    private String actionUrl;
	private String target;
    private String targetTitle;
    
    /**
     * This method is the application entry-point. The parameters has been set through the setters
     * and now we just have to render the appropriate output. 
     */
         
    public String doExecute() throws Exception
    {    	
    	actionLinks 						= new ArrayList<LinkBean>();
    	message								= getRequest().getParameter("message");
		String actionLinkString				= getRequest().getParameter("actionLinks");	
		String isAutomaticRedirectString	= getRequest().getParameter("isAutomaticRedirect");	
		//String userSessionKey 				= getRequest().getParameter("userSessionKey");
		
		if(this.getSkipResultDialogIfPossible())
			automaticClose = true;
		
		
		if (isAutomaticRedirectString != null)
		{
			try
			{
				isAutomaticRedirect = Boolean.parseBoolean(isAutomaticRedirectString);
			}
			catch (Exception e)
			{
				// Do nothing. Use the default value.
			}
		}
		
		String[] elements;
		String[] values;

		//-----------------------------------------------------------
		// Retrieve any actionLinks previously stored in the session
		//-----------------------------------------------------------
		
		if (userSessionKey != null && !userSessionKey.trim().equals(""))
		{
			if (getActionLinks(userSessionKey) != null)
			{
				actionLinks = getActionLinks(userSessionKey);
			}
			else
			{
				logger.warn("You submitted a userSessionKey but there are no action links stored in the session variable\"" + userSessionKey + "_actionLinks\".");
			}
		}

		String disableCloseLink = getActionExtraData(userSessionKey, "disableCloseLink");
		if(disableCloseLink == null || !disableCloseLink.equals("true"))
			actionLinks.add(new LinkBean("closeDialog", getLocalizedString(getLocale(), "tool.common.closeDialogLinkText"), getLocalizedString(getLocale(), "tool.common.closeDialogLinkTitle"), getLocalizedString(getLocale(), "tool.common.closeDialogLinkTitle"), "javascript:closeDialog();", true, ""));

		//-----------------------------------------------------------
		// Add any actionLinks submitted in the request
		//-----------------------------------------------------------
		
		if (actionLinkString != null)
		{			
			elements = actionLinkString.split(";");
			
			String attr1 = "";
			String attr2 = "";
			String attr3 = "";
			String attr4 = "";
			String attr5 = "";
			String attr6 = "";
			
    		for (String element : elements)
    		{    			
    			values 							= element.split(","); 
    			
    			if (values.length > 0) attr1 	= values[0];
    			if (values.length > 1) attr2	= values[1];
    			if (values.length > 2) attr3	= values[2];
    			if (values.length > 3) attr4	= values[3];
    			if (values.length > 4) attr5	= values[4]; 
    			if (values.length > 5) attr6	= values[5];
    			
    			boolean isJavascriptLink = false;
    			if(attr5.indexOf("javascript:") > -1)
    				isJavascriptLink = true;
    			
    			LinkBean myLinkBean 			= new LinkBean(attr1, attr2, attr3, attr4, attr5, isJavascriptLink, attr6);
    			actionLinks.add(myLinkBean);
    		}
		}
		
		return SUCCESS;
    }

    public String getMessage()
	{				
    	if (message == null)
    	{
    		message = getActionMessage(userSessionKey);
    		if(message == null || message.equals(""))
    			message = getLocalizedString(getLocale(), "tool.common.inlineOperationDoneHeader");
    	}
    	
		return message;
	}
    
    public void setMessage(String message)
    {
    	this.message = message;
    }

    public String getActionExtraData(String extraDataKey)
	{				
		return this.getActionExtraData(getUserSessionKey(), extraDataKey);
	}

	public List<LinkBean> getActionLinks()
	{				
		return actionLinks;
	}
	
	public LinkBean getFirstActionLink()
	{		
		return actionLinks.get(0);
	}
	
	public boolean getIsAutomaticRedirect()
	{
		return isAutomaticRedirect;
	}
	
	public void setIsAutomaticRedirect(boolean isAutomaticRedirect)
	{
		this.isAutomaticRedirect = isAutomaticRedirect;
	}

	public String getUserSessionKey()
	{
		return userSessionKey;
	}

	public void setUserSessionKey(String userSessionKey)
	{
		this.userSessionKey = userSessionKey;
	}
	
    public boolean getAutomaticClose()
	{
		return automaticClose;
	}

	public void setAutomaticClose(boolean automaticClose)
	{
		this.automaticClose = automaticClose;
	}

	public String getActionUrl()
	{
		return actionUrl;
	}

	public void setActionUrl(String actionUrl)
	{
		this.actionUrl = actionUrl;
	}

	public String getTarget()
	{
		return target;
	}

	public void setTarget(String target)
	{
		this.target = target;
	}

	public String getTargetTitle()
	{
		return targetTitle;
	}

	public void setTargetTitle(String targetTitle)
	{
		this.targetTitle = targetTitle;
	}

}
