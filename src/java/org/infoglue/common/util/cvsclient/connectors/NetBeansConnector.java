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

package org.infoglue.common.util.cvsclient.connectors;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.DeploymentController;
import org.infoglue.common.util.cvsclient.CVSRoot;
import org.infoglue.common.util.cvsclient.HistoryCommandWithCVSListener;
import org.infoglue.common.util.cvsclient.MyConnection;
import org.infoglue.common.util.vc.connectors.VCConnector;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.commit.CommitCommand;
import org.netbeans.lib.cvsclient.command.tag.TagCommand;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.commandLine.BasicListener;
import org.netbeans.lib.cvsclient.connection.Scrambler;
import org.netbeans.lib.cvsclient.connection.StandardScrambler;
import org.netbeans.lib.cvsclient.event.MessageEvent;

public class NetBeansConnector implements VCConnector
{
    private final static Logger logger = Logger.getLogger(NetBeansConnector.class.getName());

	private GlobalOptions globalOptions = new GlobalOptions();
	private String CVSRoot;
	private String localPath;
	private String password;
	
	public NetBeansConnector(String CVSRoot, String localPath, String password)
	{
		this.CVSRoot = CVSRoot;
		this.localPath = localPath;
		this.password = password;
	}
	
	public void checkOutModuleFromHead(String aModuleName) throws Exception
	{
	    checkOut(aModuleName, null);
	}
	
	public List<File> checkOutModuleFromTag(String aModuleName, String aTagName) throws Exception
	{
		return checkOut(aModuleName, aTagName);
	}
	
	private List<File> checkOut(String aModuleName, String aTagName) throws Exception
	{
		List<File> files = new ArrayList<File>();
		
		//------------------------
		// Connect to the CVS
		//------------------------
	    
	    Client client = getClient();
	    
		//------------------------
		// Setup the command
		//------------------------

		CheckoutCommand command = new CheckoutCommand();
		command.setBuilder(null);
		command.setRecursive(true);
		command.setModule(aModuleName);
		command.setPruneDirectories(true);
		command.setUseHeadIfNotFound(false);
		
		logger.info("CVS COMMAND: " + command.getCVSCommand());
		
		if (aTagName != null)
		{
			command.setCheckoutByRevision(aTagName);
		}
		
		//------------------------
		// Execute the command
		//------------------------

		client.executeCommand(command, globalOptions);
		
		File moduleRoot = new File(localPath);
		if(moduleRoot.exists())
		{
			addFilesRecursive(moduleRoot, files);
		}
		
		logger.info("CHECK OUT COMPLETED");
		
		return files;
	}
	
	private void addFilesRecursive(File parentFile, List files)
	{
		File[] childFiles = parentFile.listFiles();
		for(int i=0; i<childFiles.length; i++)
		{
			File childFile = childFiles[i];
			logger.info("childFile:" + childFile.getName());
			if(childFile.isDirectory())
				addFilesRecursive(childFile, files);
			
			files.add(childFile);	
		}
	}
	
	public void updateFilesFromHead(File[] files) throws Exception
	{
		logger.info("ABOUT TO UPDATE FROM HEAD");
		
	    update(files, null);
	}
	
	public void updateFilesFromTag(File[] aFiles, String aTagName) throws Exception
	{
		logger.info("ABOUT TO UPDATE FROM TAG: " + aTagName);
		
		update(aFiles, aTagName);
	}
	
	private void update(File[] aFiles, String aTagName) throws Exception
	{
		logger.info("UPDATING");
		
		//------------------------
		// Connect to the CVS
		//------------------------
	    
	    Client client = getClient();
	   
		//------------------------
		// Setup the command
		//------------------------
		
	    UpdateCommand command = new UpdateCommand();
		command.setBuilder(null);
		command.setRecursive(true);
		command.setPruneDirectories(true);
		command.setUseHeadIfNotFound(false);
		command.setBuildDirectories(true);
		
		logger.info("FILE PATH: " + aFiles[0].getAbsolutePath());
		
		if (aFiles != null)
		{
			logger.info("SETTING FILES: ");
			
			for (int i = 0; i < aFiles.length; i ++)
			{
				logger.info("- " + aFiles[i].getAbsolutePath());
			}
			command.setFiles(aFiles);
		}
		
		if (aTagName != null)
		{
			command.setUpdateByRevision(aTagName);
		}
		
		logger.info("CVS COMMAND: " + command.getCVSCommand());
		
		//------------------------
		// Execute the command
		//------------------------

		client.executeCommand(command, globalOptions);
		
		logger.info("UPDATE COMPLETED");
	}
	
	public void commitFilesToHead(File[] aFiles) throws Exception
	{
		logger.info("ABOUT TO COMMIT TO HEAD");
		
		commit(aFiles, null);
	}
	
	public void commitFilesToTag(File[] aFiles, String aTagName) throws Exception
	{
		logger.info("ABOUT TO COMMIT TO TAG: " + aTagName);
		
		commit(aFiles, aTagName);
	}
	
	public void commit(File[] aFiles, String aTagName) throws Exception
	{
		logger.info("COMMITING");
		
		//------------------------
		// Connect to the CVS
		//------------------------
	    
	    Client client = getClient();
	    
		//------------------------
		// Setup the command
		//------------------------
		
	    CommitCommand command = new CommitCommand();
		command.setBuilder(null);
		command.setRecursive(true);
		
		if (aFiles != null)
		{
			logger.info("SETTING FILES: ");
			
			for (int i = 0; i < aFiles.length; i ++)
			{
				logger.info("- " + aFiles[i].getAbsolutePath());
			}
			command.setFiles(aFiles);
		}
		
		if (aTagName != null)
		{
			command.setToRevisionOrBranch(aTagName);
		}
		
		logger.info("CVS COMMAND: " + command.getCVSCommand());
		
		//------------------------
		// Execute the command
		//------------------------

		client.executeCommand(command, globalOptions);	
		
		logger.info("COMMIT COMPLETED");
	}	
	
	public void tagFiles(File[] aFiles, String aTagName) throws Exception
	{
		logger.info("ABOUT TO TAG");
		
		//------------------------
		// Connect to the CVS
		//------------------------
	    
	    Client client = getClient();
	    
		//------------------------
		// Setup the command
		//------------------------
		
	    TagCommand command = new TagCommand();
		command.setBuilder(null);
		command.setRecursive(true);
		command.setTag(aTagName);

		if (aFiles != null)
		{
			logger.info("SETTING FILES: ");
			
			for (int i = 0; i < aFiles.length; i ++)
			{
				logger.info("- " + aFiles[i].getAbsolutePath());
			}
			command.setFiles(aFiles);
		}
		
		logger.info("CVS COMMAND: " + command.getCVSCommand());
		
		//------------------------
		// Execute the command
		//------------------------

		client.executeCommand(command, globalOptions);
		
		logger.info("TAG COMPLETED");
	}
	

	public List<String> getTags(String moduleName) throws Exception 
	{
		List<String> tags = new ArrayList<String>();
		logger.info("ABOUT TO GET ALL TAGS on module [" + moduleName + "]");
		
	    Client client = getClient();
	    	    
	    HistoryCommandWithCVSListener command = new HistoryCommandWithCVSListener();
	    command.setReportTags(true);
	    command.setForAllUsers(true);

		logger.info("CVS COMMAND: " + command.getCVSCommand());
		
		client.executeCommand(command, globalOptions);
	    List<MessageEvent> messageEvents = command.getMessageEvents();
	    Iterator<MessageEvent> messageEventsIterator = messageEvents.iterator();
	    while(messageEventsIterator.hasNext())
	    {
	    	MessageEvent event = messageEventsIterator.next();
	    	String rawTag = event.getMessage();
			logger.info("rawTag:" + rawTag);
			if(rawTag != null && !rawTag.equals("") && rawTag.indexOf(moduleName) > -1)
			{
				int start = rawTag.indexOf("[");
				if(start > -1)
				{
					int end = rawTag.indexOf(":", start);
					//logger.info("start:" + start);
					//logger.info("end:" + end);
					String tagName = rawTag.substring(start + 1, end);
					tags.add(tagName);
				}
			}
		}
		
		logger.info("GOT ALL TAGS");
		
		return tags;
	}

	public Client getClient() throws Exception
	{	
		if (CVSRoot == null)
		{
			throw new Exception ("The CVS root has not been set. Please call the method CVSConnector.setCVSRoot(cvsRootString) before attempting to perform CVS operations.");
		}
		
		if (localPath == null)
		{
			throw new Exception ("The local path has not been set. Please call the method CVSConnector.setLocalPath(localPathString) before attempting to perform CVS operations.");
		}
		
		if (password == null)
		{
			throw new Exception ("The password has not been set. Please call the method CVSConnector.setPassword(passwordString) before attempting to perform CVS operations.");
		}
		
		//----------------------
		// Parse the CVS source
		//----------------------
		
		CVSRoot cvsRoot 	= new CVSRoot(CVSRoot);
		
	    //---------------------
	    // Encode the password
	    //---------------------
	    
		Scrambler scr 		= StandardScrambler.getInstance();
		MyConnection conn 	= new MyConnection();
		
		//---------------------
		// Open the connection
		//---------------------
		
		globalOptions.setCVSRoot(CVSRoot);
		
		conn.setUserName(cvsRoot.getUser());
		conn.setEncodedPassword(scr.scramble(password));
		conn.setHostName(cvsRoot.getHost());
		conn.setRepository(cvsRoot.getRepository());
		conn.verify();
		conn.open();
	    	    
		//------------------------
		// Setup the client
		//------------------------
		
		Client client = new Client(conn, new StandardAdminHandler());		
		client.getEventManager().addCVSListener(new BasicListener());
		client.setLocalPath(localPath);
		
	    return client;
	}

}
