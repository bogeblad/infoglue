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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.infoglue.cms.util.sorters.AverageInvokingTimeComparator;
import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
import org.infoglue.deliver.invokers.ComponentBasedHTMLPageInvoker;

/**
 * @author mattias
 *
 * This class contains a lot of statistics. Badly implemented now that Atomic operations are part of Java.
 */

public class Counter
{
    private final static Logger logger = Logger.getLogger(Counter.class.getName());

    private static Integer count = new Integer(0);
    private static Integer activeCount = new Integer(0);
    private static Integer totalCount = new Integer(0);
    private static Integer approximateNumberOfDatabaseQueries = new Integer(0);
    private static Long totalElapsedTime = new Long(0);
    private static Long maxElapsedTime = new Long(0);
    private static Map allComponentsStatistics = new HashMap();
    private static Map allPageStatistics = new HashMap();
    private static LinkedBlockingQueue<String> latestPageStatistics = new LinkedBlockingQueue<String>(100);
    private static LinkedBlockingQueue<CacheEvictionBean> latestPublications = new LinkedBlockingQueue<CacheEvictionBean>(200);
    private static List<CacheEvictionBean> ongoingPublications = new ArrayList<CacheEvictionBean>();
    private static Integer numberOfPublicationsSinceStart = new Integer(0);
    
    private Counter(){}

    static int getNumberOfCurrentRequests()
    {
        return count.intValue();
    }

    static int getNumberOfActiveRequests()
    {
        return activeCount.intValue();
    }

    static int getTotalNumberOfRequests()
    {
        return totalCount.intValue();
    }

    static long getAverageElapsedTime()
    {
    	if(totalElapsedTime != null && totalCount.intValue() != 0)
    		return totalElapsedTime.longValue() / totalCount.intValue();
    	else
    		return 0;
    }

    static long getMaxElapsedTime()
    {
        return maxElapsedTime.longValue();
    }

    static int getApproximateNumberOfDatabaseQueries()
    {
        return approximateNumberOfDatabaseQueries.intValue();
    }

    static void incApproximateNumberOfDatabaseQueries()
    {
    	approximateNumberOfDatabaseQueries = new Integer(approximateNumberOfDatabaseQueries.intValue() + 1);
    }

    static void decApproximateNumberOfDatabaseQueries()
    {
    	if(approximateNumberOfDatabaseQueries > 0)
    		approximateNumberOfDatabaseQueries = new Integer(approximateNumberOfDatabaseQueries.intValue() - 1);
    }

    
    static List<CacheEvictionBean> getLatestPublications()
    {
    	List<CacheEvictionBean> latestPublicationsList = new ArrayList<CacheEvictionBean>();
    	synchronized (latestPublications)
		{
    		Iterator<CacheEvictionBean> latestPublicationsIterator = latestPublications.iterator();
    		while(latestPublicationsIterator.hasNext())
    		{
    			latestPublicationsList.add(latestPublicationsIterator.next());
    		}
		}
        return latestPublicationsList;
    }

    static List<String> getLatestPageStatistics()
    {
    	List<String> latestPagesList = new ArrayList<String>();
    	synchronized (latestPageStatistics)
		{
    		Iterator<String> latestPageStatisticsIterator = latestPageStatistics.iterator();
    		while(latestPageStatisticsIterator.hasNext())
    		{
    			latestPagesList.add(latestPageStatisticsIterator.next());
    		}
		}
        return latestPagesList;
    }
    
    static void resetLatestPublications()
    {
    	synchronized (latestPublications)
		{
    		latestPublications.clear();
    		numberOfPublicationsSinceStart = 0;
		}
    }

    static List<CacheEvictionBean> getOngoingPublications()
    {
    	List<CacheEvictionBean> ongoingPublicationsList = new ArrayList<CacheEvictionBean>();
    	synchronized (ongoingPublications)
		{
    		Iterator<CacheEvictionBean> ongoingPublicationsIterator = ongoingPublications.iterator();
    		while(ongoingPublicationsIterator.hasNext())
    		{
    			ongoingPublicationsList.add(ongoingPublicationsIterator.next());
    		}
		}
        return ongoingPublicationsList;
    }

    static Integer getNumberOfPublicationsSinceStart()
    {
    	synchronized (latestPublications)
		{
    		return latestPublications.size();
		}
    	//return numberOfPublicationsSinceStart;
    }

    synchronized static void addPublication(CacheEvictionBean bean)
    {
    	if(bean.getClassName().indexOf("ServerNodeProperties") == -1)
    		numberOfPublicationsSinceStart++;

    	synchronized (latestPublications)
		{
    		if(latestPublications.remainingCapacity() == 0)
    			latestPublications.poll();
    		
    		latestPublications.add(bean);
		}
    }

    synchronized static void addOngoingPublication(CacheEvictionBean bean)
    {
    	synchronized (ongoingPublications)
		{
    		ongoingPublications.add(bean);
		}
    }

    synchronized static void removeOngoingPublication(CacheEvictionBean bean)
    {
    	synchronized (ongoingPublications)
		{
    		ongoingPublications.remove(bean);
		}
    }

    synchronized static void incNumberOfCurrentRequests(boolean active)
    {
        count = new Integer(count.intValue() + 1);
        if(active)
        	activeCount = new Integer(activeCount.intValue() + 1);
    }

    synchronized static void decNumberOfCurrentRequests(long elapsedTime)
    {
    	if(count > 0)
    		count = new Integer(count.intValue() - 1);
        
    	if(elapsedTime != -1 && activeCount > 0)
        	activeCount = new Integer(activeCount.intValue() - 1);
    		
    	totalCount = new Integer(totalCount.intValue() + 1);

        if(elapsedTime != -1)
        {
	    	totalElapsedTime = new Long(totalElapsedTime.longValue() + elapsedTime);
	    	if(elapsedTime > maxElapsedTime.longValue())
	    		maxElapsedTime = new Long(elapsedTime);
        }
    }
    
    synchronized static Set getAllComponentNames()
    {
    	synchronized (allComponentsStatistics) 
    	{
    		return allComponentsStatistics.keySet();
		}
    }

    synchronized static Set getAllPageUrls()
    {
    	synchronized (allPageStatistics) 
    	{
    		return allPageStatistics.keySet();
		}
    }

    synchronized private static Map getComponentStatistics(String componentName)
    {
    	Map componentStatistics = (Map)allComponentsStatistics.get(componentName);
    	if(componentStatistics == null)
    	{
    		componentStatistics = new HashMap();
    		componentStatistics.put("totalElapsedTime", new Long(0));
    		componentStatistics.put("totalNumberOfInvokations", new Integer(0));
    		allComponentsStatistics.put(componentName, componentStatistics);
        }
    	
    	return componentStatistics;
    }

    synchronized private static Map getPageStatistics(String pageUrl)
    {
    	Map pageStatistics = (Map)allPageStatistics.get(pageUrl);
    	if(pageStatistics == null)
    	{
    		pageStatistics = new HashMap();
    		pageStatistics.put("totalElapsedTime", new Long(0));
    		pageStatistics.put("totalNumberOfInvokations", new Integer(0));
    		allPageStatistics.put(pageUrl, pageStatistics);
        }
    	
    	return pageStatistics;
    }

    static void registerComponentStatistics(String componentName, long elapsedTime)
    {
    	Map componentStatistics = getComponentStatistics(componentName);   
    	//synchronized (componentStatistics) 
    	try
    	{
        	Long oldTotalElapsedTime = (Long)componentStatistics.get("totalElapsedTime");
        	Long totalElapsedTime = new Long(oldTotalElapsedTime.longValue() + elapsedTime);			
        	componentStatistics.put("totalElapsedTime", totalElapsedTime);

        	Integer oldTotalNumberOfInvokations = (Integer)componentStatistics.get("totalNumberOfInvokations");
        	Integer totalNumberOfInvokations = new Integer(oldTotalNumberOfInvokations.intValue() + 1);			
        	componentStatistics.put("totalNumberOfInvokations", totalNumberOfInvokations);
    	}  
    	catch (Exception e) 
    	{
    		logger.error("Error in registerComponentStatistics: " + e.getMessage());
		}
    }

    static void registerPageStatistics(String pageUrl, long elapsedTime)
    {
    	Map pageStatistics = getPageStatistics(pageUrl);   
    	//synchronized (pageStatistics) 
    	try
    	{
        	Long oldTotalElapsedTime = (Long)pageStatistics.get("totalElapsedTime");
           	Long totalElapsedTime = new Long(oldTotalElapsedTime.longValue() + elapsedTime);			
        	pageStatistics.put("totalElapsedTime", totalElapsedTime);
        	pageStatistics.put("lastEventDate", new Date());

        	Integer oldTotalNumberOfInvokations = (Integer)pageStatistics.get("totalNumberOfInvokations");
        	Integer totalNumberOfInvokations = new Integer(oldTotalNumberOfInvokations.intValue() + 1);			
        	pageStatistics.put("totalNumberOfInvokations", totalNumberOfInvokations);
    	}    	
    	catch (Exception e) 
    	{
    		logger.error("Error in registerPageStatistics: " + e.getMessage());
		}
    }

    static void registerLatestPageStatistics(String pageUrl)
    {
    	synchronized (latestPageStatistics)
		{
    		if(latestPageStatistics.remainingCapacity() == 0)
    			latestPageStatistics.poll();
    		
    		latestPageStatistics.add(pageUrl);
		}
    }

    static long getAverageElapsedTime(String componentName)
    {
    	Map componentStatistics = getComponentStatistics(componentName);
    	synchronized (componentStatistics) 
    	{
        	Long totalElapsedTime = (Long)componentStatistics.get("totalElapsedTime");
        	Integer oldTotalNumberOfInvokations = (Integer)componentStatistics.get("totalNumberOfInvokations");
 
        	return totalElapsedTime.longValue() / oldTotalNumberOfInvokations.intValue();			
		}
    }

    static int getNumberOfHits(String componentName)
    {
    	Map componentStatistics = getComponentStatistics(componentName);
    	synchronized (componentStatistics) 
    	{
        	return ((Integer)componentStatistics.get("totalNumberOfInvokations")).intValue();
 		}
    }
    
    static long getPageAverageElapsedTime(String pageUrl)
    {
    	Map pageStatistics = getPageStatistics(pageUrl);
    	synchronized (pageStatistics) 
    	{
        	Long totalElapsedTime = (Long)pageStatistics.get("totalElapsedTime");
        	Integer oldTotalNumberOfInvokations = (Integer)pageStatistics.get("totalNumberOfInvokations");
        	if(totalElapsedTime != null && totalElapsedTime != 0 && oldTotalNumberOfInvokations != null && oldTotalNumberOfInvokations != 0)
        		return totalElapsedTime.longValue() / oldTotalNumberOfInvokations.intValue();		
        	else
        		return -1;
		}
    }

    static Date getLastEventDate(String pageUrl)
    {
    	Map pageStatistics = getPageStatistics(pageUrl);
    	synchronized (pageStatistics) 
    	{
        	Date lastEventDate = (Date)pageStatistics.get("lastEventDate");
        	return lastEventDate;
		}
    }

    static int getPageNumberOfHits(String pageUrl)
    {
    	Map pageStatistics = getPageStatistics(pageUrl);
    	synchronized (pageStatistics) 
    	{
        	return ((Integer)pageStatistics.get("totalNumberOfInvokations")).intValue();
 		}
    }

    static void resetComponentStatistics()
    {
    	synchronized (allComponentsStatistics) 
    	{
    		allComponentsStatistics.clear();
    	}
   	}

    static void resetPageStatistics()
    {
    	synchronized (allPageStatistics) 
    	{
    		allPageStatistics.clear();
    	}
   	}

    static void shortenPageStatistics()
    {
		logger.info("shortenPageStatistics");

    	try
    	{
	    	synchronized (allPageStatistics) 
	    	{
	    		if(allPageStatistics.size() > 50)
	    		{
		    		Map shortPageStatistics = new HashMap();
		
		            List unsortedPageUrls = new ArrayList();
		            Set pageUrls = allPageStatistics.keySet();
		            Iterator pageUrlsIterator = pageUrls.iterator();
		            while(pageUrlsIterator.hasNext())
		            {
		            	String pageUrl = (String)pageUrlsIterator.next();
		            	Map pageStatistics = (Map)allPageStatistics.get(pageUrl);
		            	synchronized (pageStatistics) 
		            	{
		                	Long totalElapsedTime = (Long)pageStatistics.get("totalElapsedTime");
		                	Integer pageNumberOfHits = (Integer)pageStatistics.get("totalNumberOfInvokations");
		                	long pageAverageElapsedTime = totalElapsedTime / pageNumberOfHits;
		                	unsortedPageUrls.add(getList("" + pageUrl, new Long(pageAverageElapsedTime)));
		            	}
		            }
		
		            Collections.sort(unsortedPageUrls, new AverageInvokingTimeComparator());
		            
		            if(unsortedPageUrls.size() > 50)
		            	unsortedPageUrls = unsortedPageUrls.subList(0, 50);
		            
		            Iterator unsortedPageUrlsIterator = unsortedPageUrls.iterator();
		            while(unsortedPageUrlsIterator.hasNext())
		            {
		            	List item = (List)unsortedPageUrlsIterator.next();
		            	if(item.size() > 1)
		            	{
		            		String pageUrl = (String)item.get(0);
		            		shortPageStatistics.put(pageUrl, allPageStatistics.get(pageUrl));
		            	}
		            }
		            
		            allPageStatistics = shortPageStatistics;
	    		}
	    	}
    	}
    	catch (Exception e) 
    	{
    		logger.error("Error in shortenPageStatistics:" + e.getMessage());
		}
    	
    	try
    	{
    		logger.info("Shortening allComponentsStatistics: " + allComponentsStatistics.size());
	    	synchronized (allComponentsStatistics) 
	    	{
	    		if(allComponentsStatistics.size() > 500)
	    		{
		    		Map shortComponentsStatistics = new HashMap();
		
		            List unsortedComponentStatistics = new ArrayList();
		            Set componentUrls = allComponentsStatistics.keySet();
		            Iterator componentUrlsIterator = componentUrls.iterator();
		            while(componentUrlsIterator.hasNext())
		            {
		            	String componentUrl = (String)componentUrlsIterator.next();
		            	Map componentStatistics = (Map)allComponentsStatistics.get(componentUrl);
		            	synchronized (componentStatistics) 
		            	{
		                	Long totalElapsedTime = (Long)componentStatistics.get("totalElapsedTime");
		                	Integer componentNumberOfHits = (Integer)componentStatistics.get("totalNumberOfInvokations");
		                	long componentAverageElapsedTime = totalElapsedTime / componentNumberOfHits;
		                	unsortedComponentStatistics.add(getList("" + componentUrl, new Long(componentAverageElapsedTime)));
		            	}
		            }
		
		            Collections.sort(unsortedComponentStatistics, new AverageInvokingTimeComparator());
		            
		            if(unsortedComponentStatistics.size() > 500)
		            	unsortedComponentStatistics = unsortedComponentStatistics.subList(0, 500);
		            
		            Iterator unsortedComponentUrlsIterator = unsortedComponentStatistics.iterator();
		            while(unsortedComponentUrlsIterator.hasNext())
		            {
		            	List item = (List)unsortedComponentUrlsIterator.next();
		            	if(item.size() > 1)
		            	{
		            		String componentUrl = (String)item.get(0);
		            		shortComponentsStatistics.put(componentUrl, allComponentsStatistics.get(componentUrl));
		            	}
		            }
		            
		            allComponentsStatistics = shortComponentsStatistics;
	    		}
	    	}
	    	logger.info("After resizing....:" + allComponentsStatistics.size());
    	}
    	catch (Exception e) 
    	{
    		logger.error("Error in shortenComponentStatistics:" + e.getMessage());
		}    	
   	}

    static List getList(String key, Object value)
    {
        List list = new ArrayList();
        list.add(key);
        list.add(value);

        return list;
    }
    
    static void resetAverageResponseTimeStatistics()
    {
    	totalElapsedTime = new Long(0);
    	totalCount = new Integer(0);
   	}
}
