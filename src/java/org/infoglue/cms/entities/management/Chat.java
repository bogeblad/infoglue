package org.infoglue.cms.entities.management;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Chat
{
    private int messageId = 0;
    
    private LinkedList messages = new LinkedList();

    public List addMessage(String userName, Integer type, String text)
    {
        messageId++;
        
        if (text != null && text.trim().length() > 0)
        {
            messages.addFirst(new Message(messageId, userName, type, text));
            //while (messages.size() > 10)
            //{
            //messages.removeLast();
                //}
        }

        return messages;
    }

    public List getMessages()
    {
        return messages;
    }

    public List getMessages(int lastIndex)
    {
    	if(messageId == lastIndex)
    		return new ArrayList();
    	
    	if((messageId - lastIndex) > messages.size())
    		return new ArrayList();

    	if((messageId - lastIndex) < 1)
    		return new ArrayList();

        return messages.subList(0, messageId - (lastIndex));
    }

    public int getMessageId()
    {
        return messageId;
    }
}