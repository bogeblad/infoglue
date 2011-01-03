package org.infoglue.common.util.cvsclient;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.lib.cvsclient.command.history.HistoryCommand;
import org.netbeans.lib.cvsclient.event.BinaryMessageEvent;
import org.netbeans.lib.cvsclient.event.CVSListener;
import org.netbeans.lib.cvsclient.event.FileAddedEvent;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.netbeans.lib.cvsclient.event.FileRemovedEvent;
import org.netbeans.lib.cvsclient.event.FileToRemoveEvent;
import org.netbeans.lib.cvsclient.event.FileUpdatedEvent;
import org.netbeans.lib.cvsclient.event.MessageEvent;
import org.netbeans.lib.cvsclient.event.ModuleExpansionEvent;
import org.netbeans.lib.cvsclient.event.TerminationEvent;

public class HistoryCommandWithCVSListener extends HistoryCommand
{
	private List<MessageEvent> messageEvents = new ArrayList<MessageEvent>();
	
	public List<MessageEvent> getMessageEvents() 
	{
		return messageEvents;
	}


	public void commandTerminated(TerminationEvent arg0) 
	{
	}

	
	public void fileAdded(FileAddedEvent arg0) 
	{
	}

	
	public void fileInfoGenerated(FileInfoEvent arg0) 
	{
	}

	
	public void fileRemoved(FileRemovedEvent arg0) 
	{
	}

	
	public void fileToRemove(FileToRemoveEvent arg0) 
	{
		//System.out.println("arg0:" + arg0);	
	}

	
	public void fileUpdated(FileUpdatedEvent arg0) 
	{
		//System.out.println("arg0:" + arg0);	
	}

	
	public void messageSent(MessageEvent messageEvent) 
	{
		//System.out.println("messageEvent:" + messageEvent + "\n" + messageEvent.getMessage());
		this.messageEvents.add(messageEvent);
	}

	
	public void messageSent(BinaryMessageEvent arg0) 
	{
		//System.out.println("arg0:" + arg0 + "\n" + arg0.getMessage());
	}

	
	public void moduleExpanded(ModuleExpansionEvent arg0) 
	{
		//System.out.println("arg0:" + arg0);
	}

}
