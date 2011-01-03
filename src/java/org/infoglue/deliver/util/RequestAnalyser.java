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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * @author Mattias Bogeblad
 */
public class RequestAnalyser
{
    private static RequestAnalyser instance = new RequestAnalyser();
    
	private static Map threadMonitors = new HashMap();
	//private static int maxClientsInt = 0;
	private static boolean blockRequests = false;
	/*
	static
	{
	    final String maxClients = CmsPropertyHandler.getMaxClients();
        if(maxClients != null && !maxClients.equals("") && maxClients.indexOf("@") == -1)
        {
            try
            {
                maxClientsInt = new Integer(maxClients).intValue();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
	}
	*/
	public static RequestAnalyser getRequestAnalyser()
	{
	    return instance;
	}
	
    public int getNumberOfCurrentRequests()
    {
        return Counter.getNumberOfCurrentRequests();
    }

    public int getNumberOfActiveRequests()
    {
        return Counter.getNumberOfActiveRequests();
    }

    public int getTotalNumberOfRequests()
    {
        return Counter.getTotalNumberOfRequests();
    }

    public long getAverageElapsedTime()
    {
        return Counter.getAverageElapsedTime();
    }

    public long getMaxElapsedTime()
    {
        return Counter.getMaxElapsedTime();
    }

    public List getLatestPublications()
    {
        return Counter.getLatestPublications();
    }

    public void addPublication(String description)
    {
        Counter.addPublication(description);
    }

    public void incNumberOfCurrentRequests(ThreadMonitor tk)
    {
    	if(tk == null)
    		Counter.incNumberOfCurrentRequests(false);
    	else
    		Counter.incNumberOfCurrentRequests(true);
    		
    	if(tk != null)
        {
	        synchronized(threadMonitors)
	        {
	        	threadMonitors.put("" + Thread.currentThread().getId(), tk);
	        }
        }
    }

    public synchronized void decNumberOfCurrentRequests(long elapsedTime)
    {
        Counter.decNumberOfCurrentRequests(elapsedTime);
        synchronized(threadMonitors)
        {
        	threadMonitors.remove("" + Thread.currentThread().getId());
        }
    }

    public static List getLongThreadMonitors()
    {
    	List longThreads = new ArrayList();
        synchronized(threadMonitors)
        {
	        Iterator i = threadMonitors.values().iterator();
	        while(i.hasNext())
	        {
	        	ThreadMonitor tm = (ThreadMonitor)i.next();
	        	long passedTime = System.currentTimeMillis() - tm.getStarted();
	        	if(passedTime > 10000)
	        		longThreads.add(tm);
	        }
        }
        
        return longThreads;
    }

    public static List getThreadMonitors()
    {
    	List threads = new ArrayList();
        synchronized(threadMonitors)
        {
	        Iterator i = threadMonitors.values().iterator();
	        while(i.hasNext())
	        {
	        	ThreadMonitor tm = (ThreadMonitor)i.next();
	        	threads.add(tm);
	        }
        }
        
        return threads;
    }

    public void registerComponentStatistics(String componentName, long elapsedTime)
    {
        Counter.registerComponentStatistics(componentName, elapsedTime);
    }

    public void registerPageStatistics(String pageUrl, long elapsedTime)
    {
        Counter.registerPageStatistics(pageUrl, elapsedTime);
    }

    public static Set getAllComponentNames()
    {
    	return Counter.getAllComponentNames();
    }

    public static long getComponentAverageElapsedTime(String componentName)
    {
        return Counter.getAverageElapsedTime(componentName);
    }

    public static int getComponentNumberOfHits(String componentName)
    {
        return Counter.getNumberOfHits(componentName);
    }

    public static Set getAllPageUrls()
    {
    	return Counter.getAllPageUrls();
    }

    public static long getPageAverageElapsedTime(String pageUrl)
    {
        return Counter.getPageAverageElapsedTime(pageUrl);
    }

    public static int getPageNumberOfHits(String pageUrl)
    {
        return Counter.getPageNumberOfHits(pageUrl);
    }
    
    public static void resetComponentStatistics()
    {
    	Counter.resetComponentStatistics();
    }

    public static void resetPageStatistics()
    {
    	Counter.resetPageStatistics();
    }

    public static void shortenPageStatistics()
    {
    	Counter.shortenPageStatistics();
    }

    public static void resetAverageResponseTimeStatistics()
    {
    	Counter.resetAverageResponseTimeStatistics();
    }
    
	/*
    public static int getNumberOfCurrentRequests()
    {
        synchronized(currentRequests)
        {
            return currentRequests.size();
        }
    }

    public static HttpServletRequest getLongestRequests()
    {
        HttpServletRequest longestRequest = null;
        
        long firstStart = System.currentTimeMillis();
        synchronized(currentRequests)
        {
	        Iterator i = currentRequests.iterator();
	        while(i.hasNext())
	        {
	            HttpServletRequest request = (HttpServletRequest)i.next();
	            Long startTime = (Long)request.getAttribute("startTime");
	            if(startTime.longValue() < firstStart)
	                longestRequest = request;
	        }
        }
        
        return longestRequest;
    }

    public static int getAverageTimeSpentOnOngoingRequests()
    {
        if(getNumberOfCurrentRequests() > 0)
        {
	        long elapsedTime = 0;
	        long now = System.currentTimeMillis();
	        synchronized(currentRequests)
	        {
	            Iterator i = currentRequests.iterator();
		        while(i.hasNext())
		        {
		            HttpServletRequest request = (HttpServletRequest)i.next();
		            Long startTime = (Long)request.getAttribute("startTime");
		            elapsedTime = elapsedTime + (now - startTime.longValue());
		        }
	        }
	        
	        return (int)elapsedTime / getNumberOfCurrentRequests();
        }
        return 0;
    }

    public static long getMaxTimeSpentOnOngoingRequests()
    {
        HttpServletRequest request = getLongestRequests();
        if(request != null)
        {
            Long firstStart = (Long)request.getAttribute("startTime");
            long now = System.currentTimeMillis();
            
            return (now - firstStart.longValue());
        }    
        
        return 0;
    }
    
    public static int getMaxClients()
    {
        return maxClientsInt;
    }
    
    public static void setMaxClients(int maxClientsInt)
    {
        RequestAnalyser.maxClientsInt = maxClientsInt;
    }
    */
    
    /*
    public static List getCurrentRequests()
    {
        return currentRequests;
    }
    */
    
    /*
    public static boolean getBlockRequests()
    {
        return blockRequests;
    }
    */
    
    public boolean getBlockRequests()
    {
        return Blocker.getIsBlocking();
    }

    public void setBlockRequests(boolean blockRequests)
    {
    	Blocker.setBlocking(blockRequests);
    }

/*
    public static void setBlockRequests(boolean blockRequests)
    {
        RequestAnalyser.blockRequests = blockRequests;
    }
*/    
    /*
    public static HttpServletRequest getLastRequest()
    {
        return lastRequest;
    }
    
    public static void setLastRequest(HttpServletRequest lastRequest)
    {
        RequestAnalyser.lastRequest = lastRequest;
    }
    
    public static HttpServletResponse getLastResponse()
    {
        return lastResponse;
    }
    
    public static void setLastResponse(HttpServletResponse lastResponse)
    {
        RequestAnalyser.lastResponse = lastResponse;
    }
    */
}
