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

package org.infoglue.deliver.util.webloggers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * The CommonLogger class implements the abstract Logger class.
 * The resulting log will conform to the 
 * <a href="http://www.w3.org/Daemon/User/Config/Logging.html#common-logfile-format">common log format</a>).
 */

public class IISLogger extends Logger
{
    private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(IISLogger.class.getName());

	protected static final String monthnames[] = 
	{
	"Jan", "Feb", "Mar", "Apr", "May", "Jun",
	"Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
	};
    
	protected static String noUrl = "*NOURL*";
	    

	private   byte                 msgbuf[]    = null ;
	protected RandomAccessFile     log         = null ;
	protected RandomAccessFile     errlog      = null ;
	protected RandomAccessFile     trace       = null ;
	protected int                  bufsize     = 8192 ;
	protected int                  bufptr      = 0    ;
	protected int                  rotateLevel = 0    ;
	protected byte                 buffer[]    = null ;
	protected int                  year        = -1   ;
	protected int                  month       = -1   ;
	protected int                  day         = -1   ;
	protected int                  hour        = -1   ;
	private   Calendar             cal         = null ;
	private   long                 datestamp   = -1   ;
	private   char                 datecache[] = { 'D','D','/','M','M','M',
						   '/','Y','Y','Y','Y',':',
						   'H','H',':','M','M',':',
						   'S','S',' ','+','0','0',
						   '0','0'};

	private static String hostAddress = null;
	private static String hostName = null;
	
	
	/**
	 * Log the given HTTP transaction.
	 * Not implemented yet!!!
	 */

	public void logRequest(HttpServletRequest request, HttpServletResponse response, String pagePath, long duration) 
	{
		/*    	
	Client client = request.getClient() ;
	long   date   = reply.getDate();

	String user = (String) request.getState(AuthFilter.STATE_AUTHUSER);
	URL urlst = (URL) request.getState(Request.ORIG_URL_STATE);
	String requrl;
	if (urlst == null) {
		URL u = request.getURL();
		if (u == null) {
		requrl = noUrl;
		} else {
		requrl = u.toExternalForm();
		}
	} else {
		requrl = urlst.toExternalForm();
	}
	StringBuffer sb = new StringBuffer(512);
	String logs;
	int status = reply.getStatus();
	if ((status > 999) || (status < 0)) {
		status = 999; // means unknown
	}
	synchronized(sb) {
		byte ib[] = client.getInetAddress().getAddress();
		if (ib.length == 4) {
		boolean doit;
		for (int i=0; i< 4; i++) {
			doit = false;
			int b = ib[i];
			if (b < 0) {
			b += 256;
			}
			if (b > 99) {
			sb.append((char)('0' + (b / 100)));
			b = b % 100;
			doit = true;
			}
			if (doit || (b > 9)) {
			sb.append((char)('0' + (b / 10)));
			b = b % 10;
			}
			sb.append((char)('0'+b));
			if (i < 3) {
			sb.append('.');
			}
		}
		} else { // ipv6, let's be safe :)
		sb.append(client.getInetAddress().getHostAddress());
		}
		sb.append(" - ");
		if (user == null) {
		sb.append("- [");
		} else {
		sb.append(user);
		sb.append(" [");
		}
		dateCache(date, sb);
		sb.append("] \"");
		sb.append(request.getMethod());
		sb.append(' ');
		sb.append(requrl);
		sb.append(' ');
		sb.append(request.getVersion());
		sb.append("\" ");
		sb.append((char)('0'+ status / 100));
		status = status % 100;
		sb.append((char)('0'+ status / 10));
		status = status % 10;
		sb.append((char)('0'+ status));
		sb.append(' ');
		if (nbytes < 0) {
		sb.append('-');
		} else {
		sb.append(nbytes);
		}
		sb.append('\n');
		logs = sb.toString();
	}
	logmsg(logs);
		*/
	}


		
	/**
	 * Initialize this logger for the given server.
	 * This method gets the server properties describe above to
	 * initialize its various log files.
	 * @param server The server to which thiss logger should initialize.
	 */

	public void initialize() 
	{
	}
	
	/**
	 * Construct a new Logger instance.
	 */

	IISLogger() 
	{
		this.msgbuf = new byte[128] ;
	}

	private String headerLine1 = "#Software: Microsoft Internet Information Services 5.0";
	private String headerLine2 = "#Version: 1.0";
	private String headerLine3 = "#Date: " + new Date();
	private String headerLine4 = "#Fields: date time c-ip cs-username s-sitename s-computername s-ip s-port cs-method cs-uri-stem cs-uri-query sc-status sc-win32-status sc-bytes cs-bytes time-taken cs-version cs-host cs(User-Agent) cs(Cookie) cs(Referer)";
	
	
	private List logBuffer = new ArrayList();    
    
	/**
	 * This method writes a request to the logfile
	 */
    
	protected void writeRequest(String date, String row)
	{   
		logBuffer.add(row);
    	
		if(logBuffer.size() > 20)
		{
			String logPath = CmsPropertyHandler.getStatisticsLogPath();
			File file = new File(logPath + File.separator + "stat" + date + ".log");
			boolean isFileCreated = file.exists();
				
			PrintWriter pout = null; 		    
			try
			{
				pout = new PrintWriter(new FileOutputStream(file, true));
				if(!isFileCreated)
				{
					pout.println(headerLine1);
					pout.println(headerLine2);
					pout.println(headerLine3);
					pout.println(headerLine4);
				}
	    	
				Iterator i = logBuffer.iterator();
				while(i.hasNext())
				{	
					pout.println(i.next().toString());    
				}
				pout.close();
			}
			catch(Exception e)
			{
				logger.error(e.getMessage(), e);
			}
			finally
			{
				try
				{
					pout.close();
				}
				catch(Exception e)
				{
				}
			}
	    	
			logBuffer = new ArrayList();
		}
	}
    
    
	/**
	 * This method returns a date as a string.
	 */
    
	public String getCurrentDate(String pattern)
	{	
		/*
		SimpleTimeZone pdt = new SimpleTimeZone(-8 * 60 * 60 * 1000, "PST");
		pdt.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2*60*60*1000);
		pdt.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2*60*60*1000);
		*/
		// Format the current time.
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		String dateString = formatter.format(date);
		return dateString;
	}
    
    
	public String getHostAddress()
	{
		if(hostAddress != null)
			return hostAddress;
    		
		String address = null;
    	
		try
		{
			address = java.net.InetAddress.getLocalHost().getHostAddress();
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
		}
    	
		hostAddress = address;
    	
		return address;
	}

	public String getHostName()
	{
		if(hostName != null)
			return hostName;

		String name = null;
    	
		try
		{
			name = java.net.InetAddress.getLocalHost().getHostName();
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
		}
    	
		hostName = name;
    	
		return name;
	}

    
	public String defaultValueIfNull(String value)
	{
		if(value == null || value.equals(""))
			return "-";
    
		return value;
	}
}



