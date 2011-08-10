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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.sorters.FileComparator;
import org.infoglue.deliver.util.MathHelper;

/**
 * This class acts as a system tail on the logfiles available.
 * 
 * @author Mattias Bogeblad
 */

public class ViewLoggingAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewLoggingAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private String logFragment = "";
	private int logLines = 50;
	private List logFiles = new ArrayList();
	private String logFileName = null;

	public String doDownloadFile() throws Exception
	{
		boolean allowAccess = true;
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
        	java.security.Principal principal = (java.security.Principal)getHttpSession().getAttribute("infogluePrincipal");
    		if(principal == null)
    			principal = getInfoGluePrincipal();
    		
    		if(principal != null && org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController.getController().getIsPrincipalAuthorized((org.infoglue.cms.security.InfoGluePrincipal)principal, "ViewApplicationState.Read", false, true))
    		{
    			allowAccess = true;
    		}
    		else
    		{
    			allowAccess = false;
    			logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the download action. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
               	this.getResponse().setContentType("text/plain");
                this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
                this.getResponse().getWriter().println("You have no access to this view - talk to your administrator if you should. Try go through the ViewApplicationState.action if you have an account that have access.");
                return NONE;
    		}
        }
    	
    	if(logFileName != null && !logFileName.equals(""))
    	{
        	String catalinaBase = System.getProperty("catalina.base");
        	if(catalinaBase == null || catalinaBase.equals(""))
        	{	
        		catalinaBase = CmsPropertyHandler.getContextRootPath().substring(0, CmsPropertyHandler.getContextRootPath().lastIndexOf("/"));
        	}
        	catalinaBase = catalinaBase + File.separator + "logs";

        	String velocityLog = CmsPropertyHandler.getContextRootPath() + File.separator + "velocity.log";

    		List fileList = Arrays.asList(new File(catalinaBase).listFiles());
    		logFiles.addAll(fileList);
    		
    		List debugFileList = Arrays.asList(new File(CmsPropertyHandler.getContextRootPath() + File.separator + "logs").listFiles());
    		logFiles.addAll(debugFileList);
    		
    		File velocityLogFile = new File(velocityLog);
    		if(velocityLogFile.exists())
    		{
    			logFiles.add(velocityLogFile);
    		}

    		if(logFiles != null && logFiles.size() > 0)
    			Collections.sort(logFiles, Collections.reverseOrder(new FileComparator("lastModified")));

    		File file = new File(logFileName);
    		if(file.exists() && logFiles.contains(file))
    		{
	    		HttpServletResponse response = this.getResponse();
	    		response.addHeader("Content-Type", "application/force-download");
	    		response.addHeader("Content-Disposition", "attachment; filename=\"downloadedLog.txt\"");
	    		
		        // print some html
		        ServletOutputStream out = response.getOutputStream();
		        
		        // print the file
		        InputStream in = null;
		        try 
		        {
		            in = new BufferedInputStream(new FileInputStream(file));
		            int ch;
		            while ((ch = in.read()) !=-1) 
		            {
		                out.print((char)ch);
		            }
		        }
		        finally 
		        {
		            if (in != null) in.close();  // very important
		        }
    		}
    		else
    		{
    			this.getResponse().setContentType("text/plain");
                this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
                this.getResponse().getWriter().println("No file or no access to file:" + logFileName);
                return NONE; 
    		}
    	}
    	
		return NONE;
	}
	
    public String doExecute() throws Exception
    {
    	boolean allowAccess = true;
    	if(!ServerNodeController.getController().getIsIPAllowed(this.getRequest()))
        {
    		java.security.Principal principal = (java.security.Principal)getHttpSession().getAttribute("infogluePrincipal");
    		if(principal == null)
    			principal = getInfoGluePrincipal();
    		
    		if(principal != null && org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController.getController().getIsPrincipalAuthorized((org.infoglue.cms.security.InfoGluePrincipal)principal, "ViewApplicationState.Read", false, true))
    		{
    			allowAccess = true;
    		}
    		else
    		{
    			allowAccess = false;
    			this.getResponse().setContentType("text/plain");
                this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
                this.getResponse().getWriter().println("You have no access to this view - talk to your administrator if you should. Try go through the ViewApplicationState.action if you have an account that have access.");
                return NONE;
    		}
        }
    	
    	String catalinaBase = System.getProperty("catalina.base");
    	if(catalinaBase == null || catalinaBase.equals(""))
    	{	
    		catalinaBase = CmsPropertyHandler.getContextRootPath().substring(0, CmsPropertyHandler.getContextRootPath().lastIndexOf("/"));
    	}
    	catalinaBase = catalinaBase + File.separator + "logs";

    	String velocityLog = CmsPropertyHandler.getContextRootPath() + File.separator + "velocity.log";

		List fileList = Arrays.asList(new File(catalinaBase).listFiles());
		logFiles.addAll(fileList);
		
		List debugFileList = Arrays.asList(new File(CmsPropertyHandler.getContextRootPath() + File.separator + "logs").listFiles());
		logFiles.addAll(debugFileList);
		
		File velocityLogFile = new File(velocityLog);
		if(velocityLogFile.exists())
		{
			logFiles.add(velocityLogFile);
		}

		if(logFiles != null && logFiles.size() > 0)
			Collections.sort(logFiles, Collections.reverseOrder(new FileComparator("lastModified")));

		String fileName = "";
		if(logFileName == null || logFileName.equals(""))
		{
			if(logFiles.size() > 0)
			{
				fileName = ((File)logFiles.get(0)).getPath();
				Iterator filesIterator = logFiles.iterator();
				while(filesIterator.hasNext())
				{
					File file = (File)filesIterator.next();
					if(file.getName().equals("catalina.out"))
					{
						fileName = file.getPath();
						break;
					}
				}
			}
		}
		else
		{
			fileName = "An invalid file requested - could be an hack attempt:" + logFileName;
			Iterator filesIterator = logFiles.iterator();
			while(filesIterator.hasNext())
			{
				File file = (File)filesIterator.next();
				if(file.getPath().equals(logFileName))
				{
					fileName = logFileName;
					break;
				}
			}			
		}
					
		File file = new File(fileName);
		
    	logFragment = FileHelper.tail(file, logLines);
    	
        return "success";
    }

	public String getLogFragment() 
	{
		return logFragment;
	}

	public List getLogFiles() 
	{
		return logFiles;
	}

	public int getLogLines() 
	{
		return logLines;
	}

	public void setLogLines(int logLines) 
	{
		this.logLines = logLines;
	}

	public String getLogFileName() 
	{
		return logFileName;
	}

	public void setLogFileName(String logFileName) 
	{
		this.logFileName = logFileName;
	}
    
	public String getLastModifiedDateString(long lastModified)
	{
		Date date = new Date(lastModified);
		String lastModifiedDateString = new VisualFormatter().formatDate(date, "yy-MM-dd HH:ss");
		
		return lastModifiedDateString;
	}

	public String getFileSize(long size)
	{
		String fileSize = new MathHelper().fileSize(size);
		
		return fileSize;
	}
}
