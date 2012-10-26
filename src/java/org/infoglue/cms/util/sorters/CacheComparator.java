package org.infoglue.cms.util.sorters;

import java.util.Comparator;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.infoglue.deliver.applications.databeans.WebPage;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;

/**
 * Sort on a particular property, using reflection to find the value
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class CacheComparator implements Comparator
{
    private final static Logger logger = Logger.getLogger(CacheComparator.class.getName());

    private String namesInOrderString = "contentCache,siteNodeCache,siteNodeVOCache,contentVersionCache,latestSiteNodeVersionCache,contentAttributeCache,contentVersionIdCache,pageCacheLatestSiteNodeVersions";
    
	public CacheComparator()
	{
	}

	public int compare(Object o1, Object o2)
	{
		Comparable cacheName1 = (String) o1;
		Comparable cacheName2 = (String) o2;

		boolean after = after(cacheName1, cacheName2);

	    if(after)
		    return 1;
		else
		    return -1;
	}

	private boolean after(Comparable cacheName1, Comparable cacheName2)
	{	    
	    int index1 = namesInOrderString.indexOf(cacheName1.toString());
	    int index2 = namesInOrderString.indexOf(cacheName2.toString());
	    if(cacheName1.toString().indexOf("pageCache") > -1)
	    	index2 = 100;
	    if(cacheName2.toString().indexOf("pageCache") > -1)
	    	index1 = 100;
	    	
	    if(index1 != -1 && index2 != -1)
	    {
	        if(index1 > index2)
	            return true;
	        else
	            return false;
	    }
	    else
	    {
	        if(index1 == -1 && index2 != -1)
	            return true;
	        else if(index2 == -1 && index1 != -1)
	            return false;
	        else
	        {
	        	int result = 0;
	        	
	            if((cacheName1 != null && !cacheName1.toString().equalsIgnoreCase("")) && (cacheName2 == null || cacheName2.toString().equalsIgnoreCase("")))
    		        result = -1;
    		    if((cacheName2 != null && !cacheName2.toString().equalsIgnoreCase("")) && (cacheName1 == null || cacheName1.toString().equalsIgnoreCase("")))
    		        result = 1;
    		    else
    		    	result = cacheName1.compareTo(cacheName2);
	    		
	    	    if(result > 0)
	    	        return true;
	    	    else
	    	        return false;	    	    
	        }
	    }
	}

}
