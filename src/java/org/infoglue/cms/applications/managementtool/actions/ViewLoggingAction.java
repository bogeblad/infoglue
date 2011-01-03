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

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.FileUploadHelper;
import org.infoglue.cms.util.sorters.FileComparator;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.deliver.applications.actions.ViewApplicationStateAction;
import org.infoglue.deliver.util.MathHelper;

import webwork.action.ActionContext;

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

    public String doExecute() throws Exception
    {
        if(!ServerNodeController.getController().getIsIPAllowed(this.getRequest()))
        {
        	logger.error("A user from an IP(" + this.getRequest().getRemoteAddr() + ") which is not allowed tried to call doReCache.");

        	this.getResponse().setContentType("text/plain");
            this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
            this.getResponse().getWriter().println("You have no access to this view - talk to your administrator if you should.");
            
            return NONE;
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
