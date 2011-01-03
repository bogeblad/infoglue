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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.entities.management.Chat;
import org.infoglue.cms.entities.management.Message;
import org.infoglue.cms.util.CmsSessionContextListener;

/**
 * This class represents the message center where you can chat with users
 * 
 * @author mattias
 */

public class ViewMessageCenterAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;

	private static Chat systemMessagesChat = new Chat();
	private static Chat chat = new Chat();
	private Integer lastId;
	private String userName;
	private String message;
	private boolean isSystemMessage = false;
	private List messages;
	
	public static Integer INDEX_MESSAGE_TYPE = new Integer(-1);
	public static Integer SYSTEM_MESSAGE_TYPE = new Integer(0);
	public static Integer CHAT_MESSAGE_TYPE = new Integer(10);
	
	
    public String doExecute() throws Exception
    {
    	if(getInfoGluePrincipal() == null)
    		return ERROR;

    	return "success";
    }

    public String doStandaloneChat() throws Exception
    {
    	if(getInfoGluePrincipal() == null)
    		return ERROR;

    	return "successStandaloneChat";
    }

    public String doGetMessages() throws Exception
    {
    	if(getInfoGluePrincipal() == null)
    		return ERROR;
    	
    	if(lastId == null || lastId.intValue() == -1)
    		messages = chat.getMessages();
    	else
    		messages = chat.getMessages(lastId.intValue());

    	return "successGetMessages";
    }

    public String doGetSystemMessages() throws Exception
    {
    	if(getInfoGluePrincipal() == null)
    		return ERROR;
    	
    	if(lastId == null || lastId.intValue() == -1)
    	{
    	    Message message = new Message(systemMessagesChat.getMessageId(), "administrator", INDEX_MESSAGE_TYPE, "Undefined");
    		messages = new ArrayList();
    		messages.add(message);
    	}
    	else
    		messages = systemMessagesChat.getMessages(lastId.intValue());

    	return "successGetSystemMessages";
    }

    public String doGetSystemMessagesV3() throws Exception
    {
    	if(getInfoGluePrincipal() == null)
    		return ERROR;
    	
    	if(lastId == null || lastId.intValue() == -1)
    	{
    	    Message message = new Message(systemMessagesChat.getMessageId(), "administrator", INDEX_MESSAGE_TYPE, "Undefined");
    		messages = new ArrayList();
    		messages.add(message);
    	}
    	else
    		messages = systemMessagesChat.getMessages(lastId.intValue());

    	Collections.reverse(messages);
    	
    	return "successGetSystemMessagesV3";
    }

    public String doSendMessage() throws Exception
    {
    	if(getInfoGluePrincipal() == null)
    		return ERROR;
    	
    	chat.addMessage(this.getUserName(), CHAT_MESSAGE_TYPE, this.message);

    	if(this.isSystemMessage)
    	    systemMessagesChat.addMessage(this.getUserName(), SYSTEM_MESSAGE_TYPE, "openChat('" + this.message + "');");
    	
        return "successMessageSent";
    }

    public static void addSystemMessage(String userName, Integer messageType, String command) throws Exception
    {
    	systemMessagesChat.addMessage(userName, messageType, command);
    }
    
    public List getSessionInfoBeanList() throws Exception
    {
    	return CmsSessionContextListener.getSessionInfoBeanList();
    }
    
	public String getMessage() 
	{
		return message;
	}

	public void setMessage(String message) 
	{
		this.message = message;
	}

	public List getMessages() 
	{
		return messages;
	}

	public Integer getLastId() 
	{
		return lastId;
	}

	public void setLastId(Integer lastId) 
	{
		this.lastId = lastId;
	}
    
    public void setIsSystemMessage(boolean isSystemMessage)
    {
        this.isSystemMessage = isSystemMessage;
    }
}
