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

package org.infoglue.deliver.util;

import java.io.*;
import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.CmsJDOCallback;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.mail.MailServiceFactory;

/*
 *  Kill a thread after a given timeout has elapsed
 */

public class SimpleThreadMonitor implements Runnable
{
    private final static Logger logger = Logger.getLogger(SimpleThreadMonitor.class.getName());

    private static long lastThreadDump = -1;
    
	private Thread targetThread;
	private long millis;
	private long started;
	private Thread watcherThread;
	
	private final AtomicBoolean isDoneRunning;
	private final AtomicBoolean loop;
	private final AtomicBoolean enabled;
	private HttpServletRequest request;
	private String message;
	private boolean kill = false;
	private long threadId;
	private static long lastSentTimer = System.currentTimeMillis();
	private long startTimeInNs;

	private long totalMemory;
	private long freeMemory;
	private long maxMemory;
		
	private int numberOfCurrentRequests = 0;
	private int numberOfActiveRequests = 0;
	private int longThreadMonitorsSize = 0;
	
	/// Constructor.  Give it a thread to watch, and a timeout in milliseconds.
	// After the timeout has elapsed, the thread gets killed.  If you want
	// to cancel the kill, just call done().
	public SimpleThreadMonitor(Thread targetThread, long millis, HttpServletRequest request, String message, boolean kill)
	{
		this.targetThread = targetThread;
		this.millis = millis;
		this.started = System.currentTimeMillis();
		watcherThread = new Thread(this);
		isDoneRunning = new AtomicBoolean(false);
		loop = new AtomicBoolean(true);
		enabled = new AtomicBoolean(true);
		this.request = request;
		this.message = message;
		this.kill = kill;
		this.threadId = Thread.currentThread().getId();
		
		this.freeMemory = Runtime.getRuntime().freeMemory();
		this.totalMemory = Runtime.getRuntime().totalMemory();
		this.maxMemory = Runtime.getRuntime().maxMemory();
		
		this.numberOfCurrentRequests = RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests();
		this.numberOfActiveRequests	 = RequestAnalyser.getRequestAnalyser().getNumberOfActiveRequests();
		this.longThreadMonitorsSize  = RequestAnalyser.getLongThreadMonitors().size();

		if(millis > 0)
			watcherThread.start();

		// Hack - pause a bit to let the watcher thread get started.
		/*
		try
		{
			Thread.sleep(100);
		} 
		catch (InterruptedException e)
		{
		}
		*/
	}

	/// Constructor, current thread.
	public SimpleThreadMonitor(long millis, HttpServletRequest request, String message, boolean kill)
	{
		this(Thread.currentThread(), millis, request, message, kill);
	}

	/// Call this when the target thread has finished.
	public synchronized void done()
	{
		loop.set(false);
		enabled.set(false);
		notify();
	}

	/// Call this to restart the wait from zero.
	public void reset()
	{
		loop.set(true);
		notify();
	}

	/// Call this to restart the wait from zero with a different timeout value.
	public void reset(long millis)
	{
		this.millis = millis;
		reset();
	}

	/// The watcher thread - from the Runnable interface.
	// This has to be pretty anal to avoid monitor lockup, lost
	// threads, etc.
	public synchronized void run()
	{
		Thread me = Thread.currentThread();
		me.setPriority(Thread.MAX_PRIORITY);

		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    	startTimeInNs = threadMXBean.getCurrentThreadCpuTime();

		if (enabled.get())
		{
			do
			{
				loop.set(false);
				try
				{
					wait(millis);
				} 
				catch (InterruptedException e)
				{
				}
			} 
			while (enabled.get() && loop.get());
		}
		
		if (enabled.get() && targetThread.isAlive())
		{
			isDoneRunning.set(true);

			printThread();
			if(kill)
			{
				logger.warn("Trying to kill thread with id:" + targetThread.getId() + " but did not as it can cause deadlocks etc.");
				//targetThread.interrupt();
				//targetThread.stop(); //Never kill thread - it can cause other problems
			}
			done();
			isDoneRunning.set(false);
		}
	}

	
	private void printThread()
	{
		long now = System.currentTimeMillis();
		
		long diffLastThreadDump = now - lastThreadDump;
		logger.info("diffLastThreadDump:" + diffLastThreadDump);
		if(diffLastThreadDump > 60000)
		{
			logger.info("had not sent all threads for a while.. will do so now");
			lastThreadDump = now;
			
			try
			{
				ThreadMXBean t = ManagementFactory.getThreadMXBean();
		        long threads[] = t.getAllThreadIds();
		        ThreadInfo[] tinfo = t.getThreadInfo(threads, 40);
		        
		        StringBuilder sb = new StringBuilder("All Threads");
				
		        for (int i=0; i<tinfo.length; i++)
		        {
	                ThreadInfo e = tinfo[i];
	                
	                try
	                {
		                StackTraceElement[] el = e.getStackTrace();
		                sb.append("\n\n" + e.getThreadName() + "\n" + " " + " Thread id = " + e.getThreadId() + " " + e.getThreadState());
		                if(e.getThreadState().equals(State.BLOCKED))
		                {
		                	sb.append("\n\nBlocked info: " + e.getBlockedCount() + ":" + e.getBlockedTime() + ":" + e.getLockName() + ":" + e.getLockOwnerId() + ":" + e.getLockOwnerName() + "\n" + " " + " Thread id = " + e.getThreadId() + " " + e.getThreadState());
		    		        
		                	ThreadInfo eBlockedThread = t.getThreadInfo(e.getLockOwnerId(), 40);
	    	                StackTraceElement[] elBlockedThread = eBlockedThread.getStackTrace();
	    	                sb.append("\n\n    " + e.getThreadName() + "\n" + " " + " Thread id = " + eBlockedThread.getThreadId() + " " + eBlockedThread.getThreadState());
	    	                
	    	                if (elBlockedThread == null || elBlockedThread.length == 0)
	    	                {
	                    		sb.append("        no stack trace available");
	                    	}
	    	                else
	    	                {
		    	                for (int n = 0; n < elBlockedThread.length; n++)
		    	                {
		                            if (n != 0)
		                            	sb.append("\n");
		
		                            StackTraceElement frame = elBlockedThread[n];
		
		                            if (frame == null) {
		                            	sb.append("        null stack frame");
		                                continue;
		                            }
		
		                            sb.append("        ");
		                            sb.append(frame.toString());
		    	                }
	    	                }
	    	            }
		
		                if (el == null || el.length == 0)
		                {
	                		sb.append("    no stack trace available");
	                		continue;
		               	}
		
		                for (int n = 0; n < el.length; n++)
		                {
	                        if (n != 0)
	                        	sb.append("\n");
	
	                        StackTraceElement frame = el[n];
	
	                        if (frame == null) {
	                        	sb.append("    null stack frame");
	                            continue;
	                        }
	
	                        sb.append("    ");
	                        sb.append(frame.toString());
		                }
	                }
	                catch(Exception e2)
	                {
	                }
		        }

		        String warningEmailReceiver = CmsPropertyHandler.getWarningEmailReceiver();
		        if(warningEmailReceiver != null && !warningEmailReceiver.equals("") && warningEmailReceiver.indexOf("@warningEmailReceiver@") == -1)
		        {
					try
					{
				        logger.info("Mailing..");
						MailServiceFactory.getService().sendEmail(CmsPropertyHandler.getMailContentType(), warningEmailReceiver, warningEmailReceiver, null, null, null, null, message, sb.toString().replaceAll("\n", "<br/>"), "utf-8");
					} 
					catch (Exception e)
					{
						logger.error("Could not send mail:" + e.getMessage(), e);
					}
		        }
			}
			catch (Throwable e) 
			{
				logger.error("Error generating message:" + e.getMessage(), e);
			}
				        
		}

		//Only sends if the last stack was sent more than 3 seconds ago.
		if((now - lastSentTimer) > 10000)
		{			
	        lastSentTimer = System.currentTimeMillis();

	    	StackTraceElement[] el = targetThread.getStackTrace();

	        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        	long stopTimeInNs = threadMXBean.getThreadUserTime(targetThread.getId());
	        long diff = (stopTimeInNs - startTimeInNs) / 1000000000;
        	
	        StringBuffer stackString = new StringBuffer("\n\n" + message + "\n\n");
	        stackString.append("ServerName: " + getServerName() + "\n");
	        stackString.append("Maximum memory (MB): " + (maxMemory / 1024 / 1024) + "\n");
	        stackString.append("Used memory (MB): " + ((totalMemory - freeMemory) / 1024 / 1024) + "\n");
	        stackString.append("Free memory (MB): " + (freeMemory / 1024 / 1024) + "\n");
	        stackString.append("Total memory (MB): " + (totalMemory / 1024 / 1024) + "\n");
	        stackString.append("Number of current requests: " + numberOfCurrentRequests + "\n");
	        stackString.append("Number of active requests: " + numberOfActiveRequests + "\n");
	        stackString.append("Number of long requests: " + longThreadMonitorsSize + "\n");
	        stackString.append("Current thread time: " + diff + " seconds\n");
	        stackString.append("Average time: " + RequestAnalyser.getRequestAnalyser().getAverageElapsedTime() + "\n");
	        stackString.append("Longest time: " + RequestAnalyser.getRequestAnalyser().getMaxElapsedTime() + "\n");
	        stackString.append("Original url: " + getOriginalFullURL() + "\n");
	        stackString.append("UserInfo: " + getUserInfo() + "\n");
	        stackString.append("--------------------------------------------\n\n");
	        stackString.append("Thread with id [" + targetThread.getId() + "] at report time:\n");

	        if (el != null && el.length != 0)
	        {
	            for (int j = 0; j < el.length; j++)
	            {
	            	StackTraceElement frame = el[j];
	            	if (frame == null)
	            		stackString.append("    null stack frame" + "\n");
	            	else	
	            		stackString.append("    ").append(frame.toString()).append("\n");
	            	
	            	//if((stackString.indexOf("infoglue") > -1 && j > 20) || j > 35)
	            	//	break;
				}                    
	       	}
	
	        if(targetThread.getState().equals(State.BLOCKED))
            {
				ThreadMXBean t = ManagementFactory.getThreadMXBean();
				
				ThreadInfo e = t.getThreadInfo(targetThread.getId(), 40);
	        	stackString.append("\n\nBlocked info: " + e.getBlockedCount() + ":" + e.getBlockedTime() + ":" + e.getLockName() + ":" + e.getLockOwnerId() + ":" + e.getLockOwnerName() + "\n" + " " + " Thread id = " + e.getThreadId() + " " + e.getThreadState());
		        
            	ThreadInfo eBlockedThread = t.getThreadInfo(e.getLockOwnerId(), 40);
                StackTraceElement[] elBlockedThread = eBlockedThread.getStackTrace();
                stackString.append("\n\nBlocked thread: " + e.getThreadName() + "\n" + " " + " Thread id = " + eBlockedThread.getThreadId() + " " + eBlockedThread.getThreadState());
                
                if (elBlockedThread == null || elBlockedThread.length == 0)
                {
                	stackString.append("        no stack trace available");
            	}
                else
                {
	                for (int n = 0; n < elBlockedThread.length; n++)
	                {
                        if (n != 0)
                        	stackString.append("\n");

                        StackTraceElement frame = elBlockedThread[n];

                        if (frame == null) {
                        	stackString.append("        null stack frame");
                            continue;
                        }

                        stackString.append("        ");
                        stackString.append(frame.toString());
	                }
                }
            }
	        
			stackString.append("\n\n**********************************\nConcurrent long threads (Only an excerpt of all)\n**********************************");
	
		    ThreadMXBean t = ManagementFactory.getThreadMXBean();
	
	        List threadMonitors = RequestAnalyser.getLongThreadMonitors();
	        Iterator threadMonitorsIterator = threadMonitors.iterator();
	        int threadCount = 0;
	        while(threadMonitorsIterator.hasNext() && threadCount < 5)
		    {
				SimpleThreadMonitor tm = (SimpleThreadMonitor)threadMonitorsIterator.next();
				
				if(targetThread.getId() == tm.getThreadId())
					continue;
				
				long threads[] = {tm.getThreadId()};
			    ThreadInfo[] tinfo = t.getThreadInfo(threads, 40);
	
				stackString.append("\n\n---------------------------------\nConcurrent long thread [").append(tm.getThreadId()).append("]:\n");
				stackString.append("Elapsed time:").append(tm.getElapsedTime()).append("\n Thread id: ").append(tm.getThreadId()).append("\n Original url: ").append(tm.getOriginalFullURL()).append(")");
	
		        for (int i=0; i<tinfo.length; i++)
			    {
					ThreadInfo e = tinfo[i];
			
			        el = e.getStackTrace();
			        
			        if (el != null && el.length != 0)
			        {
			            for (int n = 0; n < el.length; n++)
			            {
			            	StackTraceElement frame = el[n];
			            	if (frame == null)
			            		stackString.append("    null stack frame\n");
			            	else	
			            		stackString.append("    null stack frame").append(frame.toString()).append("\n");
						}                    
			       	}
			    }
		        	    
				threadCount++;
		    }
	        		 
	        logger.warn(stackString);
		}
		else
		{
			logger.warn("A thread took to long but the system seems to be really clogged so we don't send this one.");
		}
		
	}
	
	/**
	 * This method returns the exact full url from the original request - not modified
	 * @return
	 */
	
	public String getOriginalFullURL()
	{
    	String originalRequestURL = this.request.getParameter("originalRequestURL");
    	if(originalRequestURL == null || originalRequestURL.length() == 0)
    		originalRequestURL = this.request.getRequestURL().toString();

    	String originalQueryString = this.request.getParameter("originalQueryString");
    	if(originalQueryString == null || originalQueryString.length() == 0)
    		originalQueryString = this.request.getQueryString();

    	return originalRequestURL + "?" + originalQueryString;
	}

	/**
	 * This method returns userinfo from the original request
	 * @return
	 */
	
	public String getUserInfo()
	{
		String userAgent = this.request.getHeader("user-agent");
        if(userAgent != null) 
        	userAgent = userAgent.toLowerCase();
        
        String userIP = this.request.getRemoteAddr();
        
    	return userAgent + " (" + userIP + ")";
	}

	public String getServerName()
    {
    	String serverName = "Unknown";

    	try
    	{
		    InetAddress localhost = InetAddress.getLocalHost();
		    serverName = localhost.getHostName();
    	}
    	catch(Exception e)
    	{
    		
    	}
    	
	    return serverName;
    }


	public long getMillis() 
	{
		return millis;
	}

	public long getStarted() 
	{
		return this.started;
	}
	
	public long getElapsedTime()
	{
		return System.currentTimeMillis() - this.started;
	}
	
	public long getThreadId() 
	{
		return threadId;
	}
	
	public boolean getIsDoneRunning()
	{
		return this.isDoneRunning.get();
	}
}
