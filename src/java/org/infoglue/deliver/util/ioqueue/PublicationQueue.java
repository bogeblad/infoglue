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

package org.infoglue.deliver.util.ioqueue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.TransactionHistoryController;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.deliver.util.LiveInstanceMonitor;

/*
 *  This class keeps track of all live deliver states by polling them with regular intervals. 
 *  It also acts as a message queue so publication messages are resent if not successful the first time.
 */

public class PublicationQueue implements Runnable
{
    private final static Logger logger = Logger.getLogger(PublicationQueue.class.getName());

	private static PublicationQueue singleton = null;
	
	private Map<String, Set<PublicationQueueBean>> instancePublicationQueueBeans = new HashMap<String, Set<PublicationQueueBean>>();
	private Map<String, String> instancePublicationQueueMeta = new HashMap<String, String>();
    
	private boolean keepRunning = true;
	
	private PublicationQueue()
	{
	}

	/**
	 * Get the singleton and start the thread if not active
	 */
	public static PublicationQueue getPublicationQueue()
	{
		if(singleton == null)
		{
			singleton = new PublicationQueue();
			Thread thread = new Thread (singleton);
			thread.start();
		}
		
		return singleton;
	}
	
	public Map<String, Set<PublicationQueueBean>> getInstancePublicationQueueBeans()
	{
		return instancePublicationQueueBeans;
	}
	
	/**
	 * This method gets when the queue for a specific instance was cleared manually last.
	 */
	public String getInstancePublicationQueueManualClearTimestamp(String serverURL)
	{
		String timestamp = instancePublicationQueueMeta.get(serverURL + "_manualClearTimestamp");
		if(timestamp != null && !timestamp.equals(""))
		{
			VisualFormatter vf = new VisualFormatter();
			return vf.formatDate(Long.parseLong(timestamp), "yyyy-MM-dd HH:mm:ss");
		}
		else
			return "Never cleared";
	}

	/**
	 * This method gets when the queued beans for a specific instance.
	 */
	public Set<PublicationQueueBean> getInstancePublicationQueueBeans(String serverURL)
	{
		if(instancePublicationQueueBeans.containsKey(serverURL))
			return instancePublicationQueueBeans.get(serverURL);
		else
			return new HashSet<PublicationQueueBean>();
	}

	/**
	 * This method allows you to add a publication queue bean and register it against a certain deliver instance.
	 * It makes sure each bean is unique.
	 */
	public void addPublicationQueueBean(String liveInstanceValidationUrl, PublicationQueueBean bean)
	{
		logger.info("Adding url..");
		Set<PublicationQueueBean> publicationQueueBeans = instancePublicationQueueBeans.get(liveInstanceValidationUrl);
		if(publicationQueueBeans == null)
		{
			publicationQueueBeans = Collections.synchronizedSet(new HashSet<PublicationQueueBean>());
			instancePublicationQueueBeans.put(liveInstanceValidationUrl, publicationQueueBeans);
		}
		
		synchronized(publicationQueueBeans)
		{
			if(publicationQueueBeans.size() < 1000)
				publicationQueueBeans.add(bean);
			else
				logger.error("Skipping publication queue for this bean as to many beans allready is in queue - must be something very wrong with the instance");
		}
		logger.info("Done...");
	}

	/**
	 * Allows for manual clearing of a live instance queue.
	 */
	public void clearPublicationQueueBean(String liveInstanceValidationUrl)
	{
		logger.error("Clearing queue manually for " + liveInstanceValidationUrl);
		Set<PublicationQueueBean> publicationQueueBeans = instancePublicationQueueBeans.get(liveInstanceValidationUrl);
		if(publicationQueueBeans == null)
		{
			publicationQueueBeans = Collections.synchronizedSet(new HashSet<PublicationQueueBean>());
			instancePublicationQueueBeans.put(liveInstanceValidationUrl, publicationQueueBeans);
		}
		
		synchronized(publicationQueueBeans)
		{
			publicationQueueBeans.clear();
		}
		instancePublicationQueueMeta.put(liveInstanceValidationUrl + "_manualClearTimestamp", "" + System.currentTimeMillis());
	}

	/**
	 * The thread runner - with each run it goes through all the queues and tries to call (POST) the deliver instance
	 * If the post fails the beans is kept in the queue and retried later if the instance is up at that time.  
	 */
	 
	public synchronized void run()
	{
		logger.info("Running HttpUniqueRequestQueue...");
		while(keepRunning)
		{
			logger.info("Running..");
			Map<String, Set<PublicationQueueBean>> localPublicationQueueBeans = new HashMap<String, Set<PublicationQueueBean>>();
			synchronized (instancePublicationQueueBeans)
			{
				localPublicationQueueBeans.putAll(instancePublicationQueueBeans);
				instancePublicationQueueBeans.clear();
			}
			if(logger.isInfoEnabled())
				logger.info("Released lock - got " + localPublicationQueueBeans.size() + " urls.");
			
			Iterator<String> serverBaseUrlsIterator = localPublicationQueueBeans.keySet().iterator();
			while(serverBaseUrlsIterator.hasNext())
			{
				String serverBaseUrl = serverBaseUrlsIterator.next();
				Boolean status = LiveInstanceMonitor.getInstance().getServerStatus(serverBaseUrl);
				Set<PublicationQueueBean> beans = localPublicationQueueBeans.get(serverBaseUrl);
				if(status != null && status)
				{
					Iterator<PublicationQueueBean> beansIterator = beans.iterator();
					while(beansIterator.hasNext())
					{
						PublicationQueueBean publicationQueueBean = beansIterator.next();
						if(logger.isInfoEnabled())
							logger.info("publicationQueueBean URL:" + publicationQueueBean.getUrlAddress());
						try
						{
							try
							{
								String response = postToUrl(publicationQueueBean.getUrlAddress(), publicationQueueBean.getRequestParameters());
								if(logger.isInfoEnabled())
									logger.info("response:" + response);
																
								NotificationMessage notificationMessage = new NotificationMessage("Publishing notification solved", "Publication", "SYSTEM", NotificationMessage.LIVE_NOTIFICATION_SOLVED, "" + publicationQueueBean.getRequestParameters().get("0.objectId"), "" + serverBaseUrl);
								TransactionHistoryController.getController().create(notificationMessage);

								beansIterator.remove();
							}
							catch(Exception e)
							{
								synchronized (instancePublicationQueueBeans)
								{
									Map<String, Set<PublicationQueueBean>> currentLiveInstancePublicationQueueBeans = instancePublicationQueueBeans;
									Set<PublicationQueueBean> currentPublicationQueueBeans = currentLiveInstancePublicationQueueBeans.get(serverBaseUrl);
									if(currentPublicationQueueBeans == null)
									{
										currentPublicationQueueBeans = new HashSet<PublicationQueueBean>();
										currentLiveInstancePublicationQueueBeans.put(serverBaseUrl, currentPublicationQueueBeans);
									}
									currentPublicationQueueBeans.addAll(beans);
								}

								logger.error("Error updating cache at " + publicationQueueBean.getUrlAddress() + ". We skip further tries for now and queue it:" + e.getMessage());
							}
						}
						catch (Exception e) 
						{
							logger.error("Error posting data to:" + publicationQueueBean.getUrlAddress() + " - reason:" + e.getMessage());
						}
					}
				}
				else
				{
					if(LiveInstanceMonitor.getInstance().getInstanceStatusMap().containsKey(serverBaseUrl))
					{
						synchronized (instancePublicationQueueBeans)
						{
							Map<String, Set<PublicationQueueBean>> currentLiveInstancePublicationQueueBeans = instancePublicationQueueBeans;
							Set<PublicationQueueBean> currentPublicationQueueBeans = currentLiveInstancePublicationQueueBeans.get(serverBaseUrl);
							if(currentPublicationQueueBeans == null)
							{
								currentPublicationQueueBeans = new HashSet<PublicationQueueBean>();
								currentLiveInstancePublicationQueueBeans.put(serverBaseUrl, currentPublicationQueueBeans);
							}
							currentPublicationQueueBeans.addAll(beans);
						}
						//logger.warn("The instance seems to be down. Let's try later instead.");
					}
					else
					{
						logger.warn("The live server at " + serverBaseUrl + " does not seem to be registered any more. Removing it's queue.");
					}
				}
			}
	
			try
			{ 
				Thread.sleep(10000);
		    } 
			catch( InterruptedException e ) 
			{
				logger.error("Interrupted Exception caught");
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
    
    private String postToUrl(String urlAddress, Hashtable map) throws Exception
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
        if(map != null)
        {
            argString = toEncodedString(map);
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
