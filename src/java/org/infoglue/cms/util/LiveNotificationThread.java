package org.infoglue.cms.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This class implements a simple queue for updating the live servers. If a update did not reach a server it can be specified to retry with regular intervals.
 * 
 * @author Mattias Bogeblad
 */
public class LiveNotificationThread implements Runnable
{
    private final static Logger logger = Logger.getLogger(LiveNotificationThread.class.getName());

	private static LiveNotificationThread singleton = null;
	private boolean keepRunning = true;
	
	private Map<String, List<FailedNotification>> serversFailedNotifications = new HashMap<String, List<FailedNotification>>();
	
	public static LiveNotificationThread getLiveNotificationThread()
	{
		if(singleton == null)
		{
			singleton = new LiveNotificationThread();
			Thread thread = new Thread (singleton);
			thread.start();
		}
		
		return singleton;
	}
	
	public void addServerFailedNotifications(String url, Hashtable message)
	{
		List<FailedNotification> serverFailedNotifications = serversFailedNotifications.get(url);
		if(serverFailedNotifications == null)
		{
			serverFailedNotifications = new ArrayList<FailedNotification>();
			serversFailedNotifications.put(url, serverFailedNotifications);
		}
		serverFailedNotifications.add(new FailedNotification(url, message));
	}
	
	public void addServerFailedNotifications(FailedNotification failedNotification)
	{
		List<FailedNotification> serverFailedNotifications = serversFailedNotifications.get(failedNotification.getUrl());
		if(serverFailedNotifications == null)
		{
			serverFailedNotifications = new ArrayList<FailedNotification>();
			serversFailedNotifications.put(failedNotification.getUrl(), serverFailedNotifications);
		}
		serverFailedNotifications.add(failedNotification);
	}

	public synchronized void run()
	{
		logger.info("Running HttpUniqueRequestQueue...");
		long timeLastLongRun = -1;
		long timeLastVeryLongRun = -1;
		while(keepRunning)
		{
			try
			{ 
				Thread.sleep(30000);
		    } 
			catch( InterruptedException e ) 
			{
				logger.error("Interrupted Exception caught");
		    }

			logger.info("Running..");
			Map<String, List<FailedNotification>> localServersFailedNotifications = new HashMap<String, List<FailedNotification>>();
			synchronized (serversFailedNotifications)
			{
				localServersFailedNotifications.putAll(serversFailedNotifications);
				serversFailedNotifications.clear();
			}
			
			for(String serverUrl : localServersFailedNotifications.keySet())
			{
				logger.info("serverUrl:" + serverUrl);
				boolean skipMoreOnThisServer = false;
				
				List<FailedNotification> failedNotifications = localServersFailedNotifications.get(serverUrl);
				
				Iterator<FailedNotification> failedNotificationsIterator = failedNotifications.iterator();
				while(failedNotificationsIterator.hasNext())
				{
					FailedNotification failedNotification = failedNotificationsIterator.next();
					try
					{
						if(!failedNotification.shouldRun())
						{
							logger.error("Skipping any more updates to this server right now it has been downprio " + failedNotification.getUrl() + "(" + failedNotification.getFailures() + ")");
							addServerFailedNotifications(failedNotification);
						}
						else if(skipMoreOnThisServer)
						{
							logger.error("Skipping any more updates to this server right now as communications seems down " + failedNotification.getUrl());
							failedNotification.increaseFailuresAndSetRunTime();
							addServerFailedNotifications(failedNotification);
						}
						else
						{
							logger.info("Gonna try again with " + failedNotification.getUrl() + " and " + failedNotification.getMessage());
							String response = postToUrl(failedNotification.getUrl(), failedNotification.getMessage());
							logger.warn("Success when updating previously failed cache at " + failedNotification.getUrl() + ":" + response);
						}
					}
					catch(Exception e)
					{
						logger.error("Error updating cache at " + failedNotification.getUrl() + ":" + e.getMessage());
						e.printStackTrace();
						skipMoreOnThisServer = true;
						failedNotification.increaseFailuresAndSetRunTime();
						addServerFailedNotifications(failedNotification);
					}
					finally
					{
						failedNotificationsIterator.remove();
					}
				}
			}		
		}
	}
	
    /**
     * This method post information to an URL and returns a string.It throws
     * an exception if anything goes wrong.
     * (Works like most 'doPost' methods)
     * 
     * @param urlAddress The address of the URL you would like to post to.
     * @param inHash The parameters you would like to post to the URL.
     * @return The result of the postToUrl method as a string.
     * @exception java.lang.Exception
     */
    
    private String postToUrl(String urlAddress, Hashtable inHash) throws Exception
    {        
        URL url = new URL(urlAddress);
        URLConnection urlConn = url.openConnection();
        urlConn.setConnectTimeout(10000);
        urlConn.setReadTimeout(10000);
        urlConn.setAllowUserInteraction(false); 
        urlConn.setDoOutput (true); 
        urlConn.setDoInput (true); 
        urlConn.setUseCaches (false); 
        urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        PrintWriter printout = new PrintWriter(urlConn.getOutputStream(), true); 
        String argString = "";
        if(inHash != null)
        {
            argString = toEncodedString(inHash);
        }
        printout.print(argString);
        printout.flush();
        printout.close (); 
        InputStream inStream = null;
        inStream = urlConn.getInputStream();
        InputStreamReader inStreamReader = new InputStreamReader(inStream);
        BufferedReader buffer = new BufferedReader(inStreamReader);            
        StringBuffer strbuf = new StringBuffer();   
        String line; 
        while((line = buffer.readLine()) != null) 
        {
            strbuf.append(line); 
        }                                              
        String readData = strbuf.toString();   
        buffer.close();
        return readData;             
    }
    
    /**
	 * Encodes a hash table to an URL encoded string.
	 * 
	 * @param inHash The hash table you would like to encode
	 * @return A URL encoded string.
	 */
		
	private String toEncodedString(Hashtable inHash) throws Exception
	{
	    StringBuffer buffer = new StringBuffer();
	    Enumeration names = inHash.keys();
	    while(names.hasMoreElements())
	    {
	        String name = names.nextElement().toString();
	        String value = inHash.get(name).toString();
	        buffer.append(URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8"));
	        if(names.hasMoreElements())
	        {
	            buffer.append("&");
	        }
	    }
	    return buffer.toString();
	}
}

class FailedNotification
{
	private String url;
	private Hashtable message;
	private Integer failures = 1;
	private Long lastRun = System.currentTimeMillis();
	
	public FailedNotification(String url, Hashtable message)
	{
		this.url = url;
		this.message = message;
	}
	
	/**
	 * @return the url
	 */
	public String getUrl() 
	{
		return url;
	}

	/**
	 * @return the publicMessage
	 */
	public Hashtable getMessage() 
	{
		return message;
	}

	/**
	 * @return the failures
	 */
	public Integer getFailures() 
	{
		return failures;
	}

	/**
	 * @param failures the failures to set
	 */
	public void increaseFailuresAndSetRunTime()
	{
		this.failures++;
		this.lastRun = System.currentTimeMillis();
	}

	/**
	 * @param failures the failures to set
	 */
	public boolean shouldRun()
	{
		if(this.failures < 3)
			return true;
		else if(this.failures < 10 && (System.currentTimeMillis() - this.lastRun > 120000))
			return true;
		else if(this.failures > 10 && (System.currentTimeMillis() - this.lastRun > 1500000))
			return true;
		else
			return false;
	}

}