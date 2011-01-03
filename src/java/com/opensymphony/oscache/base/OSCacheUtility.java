package com.opensymphony.oscache.base;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.util.CacheController;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.web.ServletCacheAdministrator;

public final class OSCacheUtility
{
    public final static Logger logger = Logger.getLogger(OSCacheUtility.class.getName());

	public static void setServletCacheParams(ServletContext context)
	{
		ServletCacheAdministrator servletCacheAdministrator = ServletCacheAdministrator.getInstance(context);
		servletCacheAdministrator.flushAll();
		Cache cache = servletCacheAdministrator.getAppScopeCache(context);
		
		servletCacheAdministrator.setAlgorithmClass("com.opensymphony.oscache.base.algorithm.ImprovedLRUCache");
		
		int capacity = 5000;

		String cacheCapacity = (String)CmsPropertyHandler.getCacheSettings().get("CACHE_CAPACITY_oscache_ServletCache");
		if(logger.isInfoEnabled())
			logger.info("cacheCapacity from application settings: " + cacheCapacity);
		
		if(cacheCapacity != null && !cacheCapacity.equals(""))
    	{
			try
			{
				capacity = Integer.parseInt(cacheCapacity);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		if(logger.isInfoEnabled())
			logger.info("Setting OSCache servlet cache to " + capacity);
		
		cache.setCapacity(capacity);    		
	}
	
	public static void clear(Cache cache)
	{
		cache.clear();
	}
	
	
	/*
	public static synchronized String getStatistics(Cache cache)
	{
		cache
	}
	*/
}
