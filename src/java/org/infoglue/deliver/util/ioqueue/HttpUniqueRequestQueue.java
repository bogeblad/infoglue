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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This is a queue thread. It works by trying again and again with 10 seconds intervals 
 * to request http data from whatever source the queue bean defines. Each bean can define it's own fetcher so this 
 * class is ignorant of what the call consists of or what consitutes success.
 */

public class HttpUniqueRequestQueue implements Runnable
{
    private final static Logger logger = Logger.getLogger(HttpUniqueRequestQueue.class.getName());

	private static HttpUniqueRequestQueue singleton = null;
	
    private Set<HttpUniqueRequestQueueBean> urls = Collections.synchronizedSet(new HashSet<HttpUniqueRequestQueueBean>());
    
	private boolean keepRunning = true;
	
	private HttpUniqueRequestQueue()
	{
	}

	/**
	 * Singleton getter
	 */
	public static HttpUniqueRequestQueue getHttpUniqueRequestQueue()
	{
		if(singleton == null)
		{
			singleton = new HttpUniqueRequestQueue();
			Thread thread = new Thread (singleton);
			thread.start();
		}
		
		return singleton;
	}

	/**
	 * This method adds a new bean to the queue
	 */
	public void addHttpUniqueRequestQueueBean(HttpUniqueRequestQueueBean bean)
	{
		logger.info("Adding url..");
		synchronized(urls)
		{
			urls.add(bean);
		}
		logger.info("Done...");
	}
	
	/**
	 * The thread/runnable implementation. It basically just loops forever each 10 seconds and on each run it
	 * takes all queued beans and ask the bean to invoke it's fetcher. If the fetcher does not report an error 
	 * the bean is regarded as finished and is removed.
	 */
	public synchronized void run()
	{
		logger.info("Running HttpUniqueRequestQueue...");
		while(keepRunning)
		{
			logger.info("Running..");
			Set<HttpUniqueRequestQueueBean> localUrls = new HashSet<HttpUniqueRequestQueueBean>();
			synchronized (urls)
			{
				localUrls.addAll(urls);
				urls.clear();
			}
			if(logger.isInfoEnabled())
				logger.info("Released lock - got " + localUrls.size() + " urls.");
			
			Iterator<HttpUniqueRequestQueueBean> localUrlsIterator = localUrls.iterator();
			while(localUrlsIterator.hasNext())
			{
				HttpUniqueRequestQueueBean localUrlBean = localUrlsIterator.next();
				if(logger.isInfoEnabled())
					logger.info("localUrl:" + localUrlBean.getUrlAddress());
				try
				{
					String result = localUrlBean.getFetcher().fetchData(localUrlBean);
					if(logger.isInfoEnabled())
						logger.info("result:" + result.length());
					if(result != null && !result.trim().equals(""))
					{
						localUrlBean.getHandler().handleResult(result);
						if(logger.isInfoEnabled())
							logger.info("handled... - throwing away!");
					}
				}
				catch (Exception e) 
				{
					logger.error("Error fetching data from:" + localUrlBean.getUrlAddress() + " - reason:" + e.getMessage());
				}
				finally
				{
					localUrlsIterator.remove();
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


}
