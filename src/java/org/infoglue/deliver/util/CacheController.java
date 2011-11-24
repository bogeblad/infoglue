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

//import org.exolab.castor.jdo.CacheManager;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.pluto.portalImpl.services.ServiceManager;
import org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistry;
import org.exolab.castor.jdo.CacheManager;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.content.impl.simple.ContentCategoryImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentRelationImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallishContentImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightImpl;
import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.CategoryImpl;
import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.FormEntryImpl;
import org.infoglue.cms.entities.management.impl.simple.FormEntryValueImpl;
import org.infoglue.cms.entities.management.impl.simple.GroupContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.GroupImpl;
import org.infoglue.cms.entities.management.impl.simple.GroupPropertiesImpl;
import org.infoglue.cms.entities.management.impl.simple.InterceptionPointImpl;
import org.infoglue.cms.entities.management.impl.simple.InterceptorImpl;
import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
import org.infoglue.cms.entities.management.impl.simple.PropertiesCategoryImpl;
import org.infoglue.cms.entities.management.impl.simple.RedirectImpl;
import org.infoglue.cms.entities.management.impl.simple.RegistryImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryLanguageImpl;
import org.infoglue.cms.entities.management.impl.simple.RoleContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.RoleImpl;
import org.infoglue.cms.entities.management.impl.simple.RolePropertiesImpl;
import org.infoglue.cms.entities.management.impl.simple.ServerNodeImpl;
import org.infoglue.cms.entities.management.impl.simple.ServiceDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.SiteNodeTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallAvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallSystemUserImpl;
import org.infoglue.cms.entities.management.impl.simple.SubscriptionImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserImpl;
import org.infoglue.cms.entities.management.impl.simple.UserContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.UserPropertiesImpl;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl;
import org.infoglue.cms.entities.structure.impl.simple.QualifyerImpl;
import org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl;
import org.infoglue.cms.entities.workflow.impl.simple.ActionDefinitionImpl;
import org.infoglue.cms.entities.workflow.impl.simple.ActionImpl;
import org.infoglue.cms.entities.workflow.impl.simple.ActorImpl;
import org.infoglue.cms.entities.workflow.impl.simple.ConsequenceDefinitionImpl;
import org.infoglue.cms.entities.workflow.impl.simple.ConsequenceImpl;
import org.infoglue.cms.entities.workflow.impl.simple.EventImpl;
import org.infoglue.cms.entities.workflow.impl.simple.WorkflowDefinitionImpl;
import org.infoglue.cms.entities.workflow.impl.simple.WorkflowImpl;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.workflow.InfoGlueJDBCPropertySet;
import org.infoglue.deliver.applications.actions.InfoGlueComponent;
import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
import org.infoglue.deliver.invokers.PageInvoker;
import org.infoglue.deliver.portal.ServletConfigContainer;

import com.opensymphony.oscache.base.AbstractCacheAdministrator;
import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.base.events.CacheEntryEventListener;
import com.opensymphony.oscache.base.events.CacheMapAccessEventListener;
import com.opensymphony.oscache.extra.CacheEntryEventListenerImpl;
import com.opensymphony.oscache.extra.CacheMapAccessEventListenerImpl;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;


public class CacheController extends Thread
{ 
    public final static Logger logger = Logger.getLogger(CacheController.class.getName());
	private static VisualFormatter formatter = new VisualFormatter();

	public static final String SETTINGSPROPERTIESCACHENAME = "serverNodePropertiesCache";
	public static final String SETTINGSPROPERTIESDOCUMENTCACHENAME = "serverNodePropertiesDocumentCache";

    public static List notifications = Collections.synchronizedList(new ArrayList());
    
    private static Map eventListeners = new HashMap();
	//private static Map caches = new HashMap();
	private static ConcurrentMap caches = new ConcurrentHashMap();
	
	//private static Map caches = Collections.synchronizedMap(new HashMap());
	private boolean expireCacheAutomatically = false;
	private int cacheExpireInterval = 1800000;
	private boolean continueRunning = true;
	
	private static GeneralCacheAdministrator generalCache = new GeneralCacheAdministrator();
	
    public static Date expireDateTime = null;
    public static Date publishDateTime = null;
     
	private static CompressionHelper compressionHelper = new CompressionHelper();
   
	private static AtomicInteger numberOfPageCacheFiles = new AtomicInteger(0);
	
	public CacheController()
	{
		super();
	}

	public void setCacheExpireInterval(int cacheExpireInterval)
	{
		this.cacheExpireInterval = cacheExpireInterval;
	}

	public static void renameCache(String cacheName, String newCacheName)
	{
		synchronized(caches) 
		{
		    Object cacheInstance = caches.get(cacheName);
		    
		    if(cacheInstance != null)
		    {
		        synchronized(cacheInstance)
		        {
		            caches.put(newCacheName, cacheInstance);
		            caches.remove(cacheName);
		        }
		    }
		}
	}	
	
	public static void clearServerNodeProperty(boolean reCache)
	{
		if(reCache)
			InfoGlueJDBCPropertySet.reCache();
   		else
   			InfoGlueJDBCPropertySet.clearCaches();
		clearCache("serverNodePropertiesCache");
		clearCache("encodedStringsCache");
		clearCache("principalToolPropertiesCache");
		CmsPropertyHandler.resetHardCachedSettings();
   	}
	
	public static void cacheObject(String cacheName, Object key, Object value)
	{
		if(cacheName == null || key == null || value == null)
			return;

		synchronized(caches)
		{
			if(!caches.containsKey(cacheName))
				//caches.put(cacheName, Collections.synchronizedMap(new HashMap()));
			    caches.put(cacheName, new HashMap());
		}
			
		//synchronized(caches)
		//{
			Map cacheInstance = (Map)caches.get(cacheName);
			if(cacheInstance != null && key != null && value != null)
		    {
				if(CmsPropertyHandler.getUseSynchronizationOnCaches())
				{
					synchronized(cacheInstance)
					{
				    	if(CmsPropertyHandler.getUseHashCodeInCaches())
				    		cacheInstance.put("" + key.hashCode(), value);
				    	else
				    		cacheInstance.put(key, value);
				    }
				}
				else
				{
				   	if(CmsPropertyHandler.getUseHashCodeInCaches())
			    		cacheInstance.put("" + key.hashCode(), value);
			    	else
			    		cacheInstance.put(key, value);
			 	}
		    }
		//}
	}	
	
	public static Object getCachedObject(String cacheName, Object key)
	{
		if(cacheName == null || key == null)
			return null;
		
		//synchronized(caches)
		//{
			Map cacheInstance = (Map)caches.get(cacheName);
			if(cacheInstance != null)
		    {
				//TODO
				if(CmsPropertyHandler.getUseSynchronizationOnCaches())
				{
					synchronized(cacheInstance)
					{
						if(CmsPropertyHandler.getUseHashCodeInCaches())
							return cacheInstance.get("" + key.hashCode());
						else
							return cacheInstance.get(key);
					}
				}
				else
				{
					if(CmsPropertyHandler.getUseHashCodeInCaches())
						return cacheInstance.get("" + key.hashCode());
					else
						return cacheInstance.get(key);
				}
		    }
		//}
		
        return null;
    }

	public static void cacheObjectInAdvancedCacheWithGroupsAsSet(String cacheName, Object key, Object value, Set groupsAsList, boolean useGroups)
	{
		Object[] o = groupsAsList.toArray();
		String[] groups = new String[o.length];
		for (int i=0; i<groups.length;i++)
		{
			groups[i] = o[i].toString();
		}
		
		cacheObjectInAdvancedCache(cacheName, key, value, groups, useGroups);
	}

	public static void cacheObjectInAdvancedCache(String cacheName, Object key, Object value)
	{
		cacheObjectInAdvancedCache(cacheName, key, value, null, false);
	}

	public static void cacheObjectInAdvancedCache(String cacheName, Object key, Object value, boolean useFileCacheFallback, String fileCacheCharEncoding)
	{
		cacheObjectInAdvancedCache(cacheName, key, value, null, false, useFileCacheFallback, true, fileCacheCharEncoding);
	}

	public static void cacheObjectInAdvancedCache(String cacheName, Object key, Object value, String[] groups, boolean useGroups)
	{
		cacheObjectInAdvancedCache(cacheName, key, value, groups, useGroups, false, true, null);
	}
	
	public static void cacheObjectInAdvancedCache(String cacheName, Object key, Object value, String[] groups, boolean useGroups, boolean useFileCacheFallback, boolean useMemoryCache, String fileCacheCharEncoding)
	{
		if(cacheName == null || key == null || value == null || key.toString().length() == 0)
			return;

		if(useMemoryCache) 
		{
		    if(!caches.containsKey(cacheName))
		    {
		    	GeneralCacheAdministrator cacheAdministrator = null;
		    	Map cacheSettings = (Map)getCachedObject("serverNodePropertiesCacheSettings", "cacheSettings");
		    	if(cacheSettings == null)
		    	{
		    		cacheSettings = CmsPropertyHandler.getCacheSettings();
		    		cacheObject("serverNodePropertiesCacheSettings", "cacheSettings", cacheSettings);
		    	}
		    	
		    	String cacheCapacity = (String)cacheSettings.get("CACHE_CAPACITY_" + cacheName);
		    	if(cacheCapacity == null || !cacheCapacity.equals(""))
		    		cacheCapacity = "15000";
		    	
		    	if(cacheName != null && cacheName.startsWith("contentAttributeCache"))
		    		cacheCapacity = "100000";
		    	//else if(cacheName != null && cacheName.startsWith("contentAttributeCache"))
		    	//	cacheCapacity = "1000";
		    	if(cacheName != null && cacheName.startsWith("pageCache"))
		    		cacheCapacity = "2000";
		    	if(cacheName != null && cacheName.startsWith("pageCacheExtra"))
		    		cacheCapacity = "4000";
				if(cacheName != null && cacheName.equalsIgnoreCase("encodedStringsCache"))
					cacheCapacity = "2000";
				if(cacheName != null && cacheName.equalsIgnoreCase("importTagResultCache"))
					cacheCapacity = "200";
				if(cacheName != null && cacheName.equalsIgnoreCase("componentPropertyCache"))
					cacheCapacity = "10000";
				if(cacheName != null && cacheName.equalsIgnoreCase("componentPropertyVersionIdCache"))
					cacheCapacity = "10000";
				if(cacheName != null && cacheName.equalsIgnoreCase("componentEditorCache"))
		    		cacheCapacity = "3000";
				if(cacheName != null && cacheName.equalsIgnoreCase("componentEditorVersionIdCache"))
		    		cacheCapacity = "3000";
				if(cacheName != null && cacheName.equalsIgnoreCase("contentVersionIdCache"))
		    		cacheCapacity = "40000";
				if(cacheName != null && cacheName.equalsIgnoreCase("contentVersionCache"))
		    		cacheCapacity = "30000";
				if(cacheName != null && cacheName.equalsIgnoreCase("pageComponentsCache"))
		    		cacheCapacity = "10000";
				if(cacheName != null && cacheName.equalsIgnoreCase("boundContentCache"))
		    		cacheCapacity = "5000";
				if(cacheName != null && cacheName.equalsIgnoreCase("childSiteNodesCache"))
		    		cacheCapacity = "5000";
				if(cacheName != null && cacheName.equalsIgnoreCase("latestSiteNodeVersionCache"))
		    		cacheCapacity = "7500";
					
				/*
				if(cacheName != null && (cacheName.equalsIgnoreCase("contentAttributeCache_Title") || 
										 cacheName.equalsIgnoreCase("contentAttributeCache_NavigationTitle") || 
										 cacheName.equalsIgnoreCase("contentAttributeCache_hideInNavigation") || 
										 cacheName.equalsIgnoreCase("contentAttributeCache_SortOrder")))
				{
					cacheCapacity = "100000";
				}
				*/
				
				if(cacheCapacity != null && !cacheCapacity.equals(""))
		    	{
					Properties p = new Properties();
			    	
					String cacheAlgorithm = (String)cacheSettings.get("CACHE_ALGORITHM_" + cacheName);
					if(cacheAlgorithm == null || cacheAlgorithm.equals(""))
						p.setProperty(AbstractCacheAdministrator.CACHE_ALGORITHM_KEY, "com.opensymphony.oscache.base.algorithm.ImprovedLRUCache");
					else
						p.setProperty(AbstractCacheAdministrator.CACHE_ALGORITHM_KEY, cacheAlgorithm);
					
					//p.setProperty(AbstractCacheAdministrator.CACHE_ALGORITHM_KEY, "com.opensymphony.oscache.base.algorithm.LRUCache");
					p.setProperty(AbstractCacheAdministrator.CACHE_CAPACITY_KEY, cacheCapacity);
					cacheAdministrator = new GeneralCacheAdministrator(p);
				}
				else
				{
					cacheAdministrator = new GeneralCacheAdministrator();
				}
		        
		        CacheEntryEventListenerImpl cacheEntryEventListener = new ExtendedCacheEntryEventListenerImpl();
		        CacheMapAccessEventListenerImpl cacheMapAccessEventListener = new CacheMapAccessEventListenerImpl(); 
		        
		        cacheAdministrator.getCache().addCacheEventListener(cacheEntryEventListener, CacheEntryEventListener.class);
		        cacheAdministrator.getCache().addCacheEventListener(cacheMapAccessEventListener, CacheMapAccessEventListener.class);
		        caches.put(cacheName, cacheAdministrator);
		        eventListeners.put(cacheName + "_cacheEntryEventListener", cacheEntryEventListener);
		        eventListeners.put(cacheName + "_cacheMapAccessEventListener", cacheMapAccessEventListener);
		    }
		    
			GeneralCacheAdministrator cacheAdministrator = (GeneralCacheAdministrator)caches.get(cacheName);
			
			//if(cacheName.startsWith("contentAttribute")/* || cacheName.startsWith("contentVersionIdCache")*/)
			//	useGroups = false;

			boolean containsSelectiveCacheUpdateNonApplicable = false;
			if(groups != null)
			{
				for(int i=0; i<groups.length; i++)
				{
					if(groups[i].equalsIgnoreCase("selectiveCacheUpdateNonApplicable"))
					{
						groups = new String[]{"selectiveCacheUpdateNonApplicable"};
						break;
					}
				}
			}
			
			//Kanske tillbaka om minnet sticker
			if(cacheName.startsWith("componentPropertyCache") || cacheName.startsWith("componentPropertyVersionIdCache"))
			{				
				//logger.error("Skipping useGroups on " + cacheName + ". Groups was:" + groups.length + " for " + key);
				useGroups = false;
			}
						
			/*
			if(cacheName.startsWith("pageCache"))
			{
				//logger.error("Skipping useGroups on pageCache. Groups was:" + groups.length + " for " + key);
				useGroups = false;				
			}
			*/
				
			//TODO
			if(CmsPropertyHandler.getUseSynchronizationOnCaches())
			{
				synchronized(cacheAdministrator)
				{
					try
					{
						if(useGroups)
						{
							if(CmsPropertyHandler.getUseHashCodeInCaches())
								cacheAdministrator.putInCache("" + key.toString().hashCode(), value, groups);
							else
								cacheAdministrator.putInCache(key.toString(), value, groups);
						}
						else
						{
							if(CmsPropertyHandler.getUseHashCodeInCaches())
								cacheAdministrator.putInCache("" + key.toString().hashCode(), value);
							else
							    cacheAdministrator.putInCache(key.toString(), value);
						}
					}
					catch (Exception e) 
					{
						logger.warn("Error putting in cache:" + e.getMessage());
					}
				}
			}
			else
			{
				try
				{
					if(useGroups)
					{
						if(CmsPropertyHandler.getUseHashCodeInCaches())
							cacheAdministrator.putInCache("" + key.toString().hashCode(), value, groups);
						else
							cacheAdministrator.putInCache(key.toString(), value, groups);
					}
					else
					{
						if(CmsPropertyHandler.getUseHashCodeInCaches())
							cacheAdministrator.putInCache("" + key.toString().hashCode(), value);
						else
						    cacheAdministrator.putInCache(key.toString(), value);
					}
				}
				catch (Exception e) 
				{
					logger.warn("Error putting in cache:" + e.getMessage(), e);
				}
			}
		}
				
		//if(cacheName.equals("pageCache"))
		//	logger.info("numberOfPageCacheFiles:" + numberOfPageCacheFiles.get());
		if(cacheName.equals("pageCache") && numberOfPageCacheFiles.get() > 30000)
		{
			if(logger.isInfoEnabled())
				logger.info("Skipping file cache as to many files were allready there");
			useFileCacheFallback = false;
		}

		//if(cacheName.equals("pageCache"))
		//	logger.info("useFileCacheFallback:" + useFileCacheFallback + " - useGroups:" + useGroups);
	    
		if(useFileCacheFallback && !useGroups)
	    {
	    	if(logger.isInfoEnabled())
    			logger.info("Caching value to disk also");
	    	
	    	String compressPageCache = CmsPropertyHandler.getCompressPageCache();
	    	
	    	if(cacheName.equals("pageCache") && compressPageCache != null && compressPageCache.equals("true"))
	    		putCachedCompressedContentInFile(cacheName, key.toString(), (byte[])value);				    	
	    	else
	    		putCachedContentInFile(cacheName, key.toString(), value.toString(), fileCacheCharEncoding);				    	
	    }
		
		//logger.info("Done cacheObjectInAdvancedCache");
	}	

	public static Object getCachedObjectFromAdvancedCache(String cacheName, String key)
	{
		return getCachedObjectFromAdvancedCache(cacheName, key, false, "UTF-8", false);
	}

	public static Object getCachedObjectFromAdvancedCache(String cacheName, String key, boolean useFileCacheFallback, String fileCacheCharEncoding, boolean cacheFileResultInMemory)
	{
		if(cacheName == null || key == null || key.length() == 0)
			return null;
		
	    Object value = null;
	    boolean stopUseFileCacheFallback = false;
	    
	    //synchronized(caches) 
	    //{
		    GeneralCacheAdministrator cacheAdministrator = (GeneralCacheAdministrator)caches.get(cacheName);
		    if(cacheAdministrator != null)
		    {
		    	//TODO
		    	if(CmsPropertyHandler.getUseSynchronizationOnCaches())
				{
		    		synchronized(cacheAdministrator)
		    		{
					    try 
					    {
							if(CmsPropertyHandler.getUseHashCodeInCaches())
								value = (cacheAdministrator == null) ? null : cacheAdministrator.getFromCache("" + key.hashCode(), CacheEntry.INDEFINITE_EXPIRY);
							else
								value = (cacheAdministrator == null) ? null : cacheAdministrator.getFromCache(key, CacheEntry.INDEFINITE_EXPIRY);
					    } 
					    catch (NeedsRefreshException nre) 
					    {
					    	if(useFileCacheFallback && nre.getCacheContent() != null)
					    	{
					    		stopUseFileCacheFallback = true;
					    	}
					    	
					    	try
					    	{
								if(CmsPropertyHandler.getUseHashCodeInCaches())
									cacheAdministrator.cancelUpdate("" + key.hashCode());
								else
									cacheAdministrator.cancelUpdate(key);
					    	}
					    	catch (Exception e) 
					    	{
					    		logger.error("Error:" + e.getMessage());
							}
						}
		    		}
				}
		    	else
		    	{
				    try 
				    {
						if(CmsPropertyHandler.getUseHashCodeInCaches())
							value = (cacheAdministrator == null) ? null : cacheAdministrator.getFromCache("" + key.hashCode(), CacheEntry.INDEFINITE_EXPIRY);
						else
							value = (cacheAdministrator == null) ? null : cacheAdministrator.getFromCache(key, CacheEntry.INDEFINITE_EXPIRY);
				    } 
				    catch (NeedsRefreshException nre) 
				    {
				    	if(useFileCacheFallback && nre.getCacheContent() != null)
				    	{
				    		stopUseFileCacheFallback = true;
				    	}
				    	
				    	try
				    	{
							if(CmsPropertyHandler.getUseHashCodeInCaches())
								cacheAdministrator.cancelUpdate("" + key.hashCode());
							else
								cacheAdministrator.cancelUpdate(key);
				    	}
				    	catch (Exception e) 
				    	{
				    		logger.error("Error:" + e.getMessage());
						}
					}
		    	}
		    }

		    if(value == null && useFileCacheFallback && !stopUseFileCacheFallback)
	    	{				
		    	Timer t = new Timer();
	    		if(logger.isInfoEnabled())
	    			logger.info("Getting cache content from file..");
	    		value = getCachedContentFromFile(cacheName, key, fileCacheCharEncoding);
	    		if(value != null && cacheFileResultInMemory)
	    		{
	    			if(logger.isInfoEnabled())
	        			logger.info("Got cached content from file as it did not exist in memory...:" + value.toString().length());
					if(CmsPropertyHandler.getUseHashCodeInCaches())
						cacheObjectInAdvancedCache(cacheName, "" + key.hashCode(), value);
					else
						cacheObjectInAdvancedCache(cacheName, key, value);
	    		}
	    		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("File cache", t.getElapsedTime());
	    	}
		//}
	    
		return value;
	}
	
	public static Object getCachedObjectFromAdvancedCache(String cacheName, String key, boolean useFileCacheFallback, String fileCacheCharEncoding, boolean cacheFileResultInMemory, Object o, Method m, Object[] args, PageInvoker pageInvoker)
	{
		if(cacheName == null || key == null || key.length() == 0)
			return null;
		
	    Object value = null;
	    
    	String pageKey = key;
    	if(CmsPropertyHandler.getUseHashCodeInCaches())
    		pageKey = "" + key.hashCode();

	    GeneralCacheAdministrator cacheAdministrator = (GeneralCacheAdministrator)caches.get(cacheName);
	    if(cacheAdministrator == null)
	    {
	    	Map cacheSettings = (Map)getCachedObject("serverNodePropertiesCacheSettings", "cacheSettings");
	    	if(cacheSettings == null)
	    	{
	    		cacheSettings = CmsPropertyHandler.getCacheSettings();
	    		cacheObject("serverNodePropertiesCacheSettings", "cacheSettings", cacheSettings);
	    	}
	    	
	    	String cacheCapacity = "2000";
	    	String cacheCapacityProperty = (String)cacheSettings.get("CACHE_CAPACITY_" + cacheName);
	    	if(cacheCapacityProperty != null && !cacheCapacityProperty.equals(""))
	    		cacheCapacity = cacheCapacityProperty;
	    		
			if(cacheCapacity != null && !cacheCapacity.equals(""))
	    	{
				Properties p = new Properties();
		    	
				p.setProperty(AbstractCacheAdministrator.CACHE_ALGORITHM_KEY, "com.opensymphony.oscache.base.algorithm.ImprovedLRUCache");
				p.setProperty(AbstractCacheAdministrator.CACHE_CAPACITY_KEY, cacheCapacity);
				cacheAdministrator = new GeneralCacheAdministrator(p);
			}
			else
			{
				cacheAdministrator = new GeneralCacheAdministrator();
			}
	        
	        CacheEntryEventListenerImpl cacheEntryEventListener = new ExtendedCacheEntryEventListenerImpl();
	        CacheMapAccessEventListenerImpl cacheMapAccessEventListener = new CacheMapAccessEventListenerImpl(); 
	        
	        cacheAdministrator.getCache().addCacheEventListener(cacheEntryEventListener, CacheEntryEventListener.class);
	        cacheAdministrator.getCache().addCacheEventListener(cacheMapAccessEventListener, CacheMapAccessEventListener.class);
	        caches.put(cacheName, cacheAdministrator);
	        eventListeners.put(cacheName + "_cacheEntryEventListener", cacheEntryEventListener);
	        eventListeners.put(cacheName + "_cacheMapAccessEventListener", cacheMapAccessEventListener);
	    }
	    
	    
	    if(cacheAdministrator != null)
	    {		    	
		    try 
		    {
		    	value = (cacheAdministrator == null) ? null : cacheAdministrator.getFromCache(pageKey, CacheEntry.INDEFINITE_EXPIRY);
		    } 
		    catch (NeedsRefreshException nre) 
		    {
		    	//logger.info("Nothing in cache - lets redo it...");
		    	//logger.info("Old content:" + nre.getCacheContent());
		    	boolean isUpdated = false;
		    	try 
		    	{
					String result = (String)m.invoke(o, args);
					//logger.info("result:" + result);
					value = result;
					if(result != null)
					{
				    	isUpdated = cacheNewResult(pageInvoker, cacheAdministrator, pageKey, result);
					}
					//logger.info("result:" + result);
				} 
		    	catch (Throwable t) 
		    	{
					t.printStackTrace();
				}

		    	try
		    	{
		    		if(!isUpdated)
		    		{
		    			cacheAdministrator.cancelUpdate(pageKey);
		    		}
		    	}
		    	catch (Exception e) 
		    	{
		    		logger.error("Error:" + e.getMessage());
				}
			}
	    }
	    
	    if(value instanceof byte[])
	    	value = compressionHelper.decompress((byte[])value);
	    
	    if(value == null && useFileCacheFallback)
    	{
	    	Timer t = new Timer();
	    	logger.info("Falling back to filecache");
    		value = getCachedContentFromFile(cacheName, key, fileCacheCharEncoding);
    		if(value != null && cacheFileResultInMemory)
    		{
    	    	logger.info("Got cached content from file as it did not exist in memory...:" + value.toString().length());
    			if(logger.isInfoEnabled())
        			logger.info("Got cached content from file as it did not exist in memory...:" + value.toString().length());
				cacheObjectInAdvancedCache(cacheName, pageKey, value);
    		}
    		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("File cache", t.getElapsedTime());
    	}
	    
		return value;
	}

	private static boolean cacheNewResult(PageInvoker pageInvoker, GeneralCacheAdministrator cacheAdministrator, String pageKey, String value) 
	{
		boolean isCached = false;
		
		String pageCacheExtraName = "pageCacheExtra";
		
		if(!pageInvoker.getTemplateController().getIsPageCacheDisabled() && !pageInvoker.getDeliveryContext().getDisablePageCache()) //Caching page if not disabled
		{
			Integer newPageCacheTimeout = pageInvoker.getDeliveryContext().getPageCacheTimeout();
			if(newPageCacheTimeout == null)
				newPageCacheTimeout = pageInvoker.getTemplateController().getPageCacheTimeout();
			
			String[] allUsedEntitiesCopy = pageInvoker.getDeliveryContext().getAllUsedEntities().clone();
			Object extraData = pageInvoker.getDeliveryContext().getExtraData();
			
			String compressPageCache = CmsPropertyHandler.getCompressPageCache();
		    if(compressPageCache != null && compressPageCache.equalsIgnoreCase("true"))
			{
				long startCompression = System.currentTimeMillis();
				byte[] compressedData = compressionHelper.compress(value);		
			    //logger.info("Compressing page for pageCache took " + (System.currentTimeMillis() - startCompression) + " with a compressionFactor of " + (this.pageString.length() / compressedData.length));
				if(pageInvoker.getTemplateController().getOperatingMode().intValue() == 3 && !CmsPropertyHandler.getLivePublicationThreadClass().equalsIgnoreCase("org.infoglue.deliver.util.SelectiveLivePublicationThread"))
				{
					cacheAdministrator.putInCache(pageKey, compressedData, allUsedEntitiesCopy);
					isCached = true;
					//CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey, extraData, allUsedEntitiesCopy, false);
					//CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_pageCacheTimeout", newPageCacheTimeout, allUsedEntitiesCopy, false);    
				}
				else
				{
					//logger.info("cacheAdministrator:" + cacheAdministrator);
					//logger.info("pageKey:" + pageKey);
					//logger.info("compressedData:" + compressedData);
					//logger.info("allUsedEntitiesCopy:" + allUsedEntitiesCopy);
					cacheAdministrator.putInCache(pageKey, compressedData, allUsedEntitiesCopy);
					isCached = true;
					CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey, extraData, allUsedEntitiesCopy, true);
					CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_pageCacheTimeout", newPageCacheTimeout, allUsedEntitiesCopy, true);    
				}
			}
		    else
		    {
		        if(pageInvoker.getTemplateController().getOperatingMode().intValue() == 3 && !CmsPropertyHandler.getLivePublicationThreadClass().equalsIgnoreCase("org.infoglue.deliver.util.SelectiveLivePublicationThread"))
		        {
		        	cacheAdministrator.putInCache(pageKey, value, allUsedEntitiesCopy);
					isCached = true;
		        	CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey, extraData, allUsedEntitiesCopy, false);
		        	CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_pageCacheTimeout", newPageCacheTimeout, allUsedEntitiesCopy, false);    
		        }
		    	else
		    	{
		    		cacheAdministrator.putInCache(pageKey, value, allUsedEntitiesCopy);
					isCached = true;
		    		CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey, extraData, allUsedEntitiesCopy, true);
		    		CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_pageCacheTimeout", newPageCacheTimeout, allUsedEntitiesCopy, true);    
		    	}
		    }
		}
		else
		{
			if(logger.isInfoEnabled())
				logger.info("Page caching was disabled for the page " + pageInvoker.getDeliveryContext().getSiteNodeId() + " with pageKey " + pageInvoker.getDeliveryContext().getPageKey() + " - modifying the logic to enable page caching would boast performance.");
		}
		return isCached;
	}

	public static Object getCachedObjectFromAdvancedCache(String cacheName, String key, int updateInterval)
	{
		return getCachedObjectFromAdvancedCache(cacheName, key, updateInterval, false, "UTF-8", false);
	}
	
	public static Object getCachedObjectFromAdvancedCache(String cacheName, String key, int updateInterval, boolean useFileCacheFallback, String fileCacheCharEncoding, boolean cacheFileResultInMemory)
	{
		if(cacheName == null || key == null)
			return null;
		
	    //logger.info("getCachedObjectFromAdvancedCache start:" + cacheName + ":" + key + ":" + updateInterval);

	    //return getCachedObject(cacheName, key);
	    Object value = null;
	    boolean stopUseFileCacheFallback = false;

	    //synchronized(caches) 
	    //{
		    GeneralCacheAdministrator cacheAdministrator = (GeneralCacheAdministrator)caches.get(cacheName);
		    if(cacheAdministrator != null)
		    {
		    	//TODO
		    	if(CmsPropertyHandler.getUseSynchronizationOnCaches())
		    	{
		    		synchronized(cacheAdministrator)
		    		{
					    try 
					    {
					        if(CmsPropertyHandler.getUseHashCodeInCaches())
					        	value = (cacheAdministrator == null) ? null : cacheAdministrator.getFromCache("" + key.hashCode(), updateInterval);
					        else
					        	value = (cacheAdministrator == null) ? null : cacheAdministrator.getFromCache(key, updateInterval);
					    } 
					    catch (NeedsRefreshException nre) 
					    {
					    	if(useFileCacheFallback && nre.getCacheContent() != null)
					    	{
					    		stopUseFileCacheFallback = true;
					    	}
					    	
					        if(CmsPropertyHandler.getUseHashCodeInCaches())
					        	cacheAdministrator.cancelUpdate("" + key.hashCode());
					        else
					        	cacheAdministrator.cancelUpdate(key);
						}
		    		}
		    	}
		    	else
		    	{
		    		try 
				    {
				        if(CmsPropertyHandler.getUseHashCodeInCaches())
				        	value = (cacheAdministrator == null) ? null : cacheAdministrator.getFromCache("" + key.hashCode(), updateInterval);
				        else
				        	value = (cacheAdministrator == null) ? null : cacheAdministrator.getFromCache(key, updateInterval);
				    } 
				    catch (NeedsRefreshException nre) 
				    {
				    	if(useFileCacheFallback && nre.getCacheContent() != null)
				    	{
				    		stopUseFileCacheFallback = true;
				    	}
				    	
				        if(CmsPropertyHandler.getUseHashCodeInCaches())
				        	cacheAdministrator.cancelUpdate("" + key.hashCode());
				        else
				        	cacheAdministrator.cancelUpdate(key);
					}
		    	}
		    	
				if(useFileCacheFallback && !stopUseFileCacheFallback)
		    	{				    		
		    		if(logger.isInfoEnabled())
		    			logger.info("Getting cache content from file..");
		    		value = getCachedContentFromFile(cacheName, key, updateInterval, fileCacheCharEncoding);
		    		if(value != null && cacheFileResultInMemory)
		    		{
		    			if(logger.isInfoEnabled())
		        			logger.info("Got cached content from file as it did not exist in memory...:" + value.toString().length());
				        if(CmsPropertyHandler.getUseHashCodeInCaches())
				        	cacheObjectInAdvancedCache(cacheName, "" + key.hashCode(), value);
				        else
				        	cacheObjectInAdvancedCache(cacheName, key, value);
		    		}
		    	}
		    }
		//}
	    
		return value;
	}

	public static void clearCachesStartingWith(String cacheNamePrefix)
	{
		Set keySet = new HashSet();
		keySet.addAll(caches.keySet());
		
		Iterator cachesIterator = keySet.iterator();
		while(cachesIterator.hasNext())
		{
			String cacheName = (String)cachesIterator.next();
			if(cacheName.startsWith(cacheNamePrefix))
				clearCache(cacheName);
		}
	}

	public static void clearCache(String cacheName)
	{
		logger.info("Clearing the cache called " + cacheName);
		synchronized(caches) 
		{
			if(caches.containsKey(cacheName))
			{
			    Object object = caches.get(cacheName);
			    if(object instanceof Map)
				{
					Map cacheInstance = (Map)object;
					synchronized(cacheInstance) 
					{
						cacheInstance.clear();
					}
				}
				else
				{
				    GeneralCacheAdministrator cacheInstance = (GeneralCacheAdministrator)object;
					synchronized(cacheInstance)
					{
						cacheInstance.flushAll();
					}
				}
		    	caches.remove(cacheName);
			    eventListeners.remove(cacheName + "_cacheEntryEventListener");
			    eventListeners.remove(cacheName + "_cacheMapAccessEventListener");
	
			    logger.info("clearCache stop...");
			}
		}
	}

	public static void flushCache(String cacheName)
	{
		logger.info("Flushing the cache called " + cacheName);
		synchronized(caches) 
		{
			if(caches.containsKey(cacheName))
			{
			    Object object = caches.get(cacheName);
			    if(object instanceof Map)
				{
					Map cacheInstance = (Map)object;
					synchronized(cacheInstance) 
					{
						cacheInstance.clear();
					}
				}
				else
				{
				    GeneralCacheAdministrator cacheInstance = (GeneralCacheAdministrator)object;
					synchronized(cacheInstance)
					{
						cacheInstance.flushAll();
					}
				}
		    	//caches.remove(cacheName);
			    //eventListeners.remove(cacheName + "_cacheEntryEventListener");
			    //eventListeners.remove(cacheName + "_cacheMapAccessEventListener");
	
			    logger.info("clearCache stop...");
			}
		}
	}
	
	public static void clearCache(String cacheName, String key)
	{
		logger.info("Clearing the cache called " + cacheName + " and key: " + key);
		synchronized(caches) 
		{
			if(caches.containsKey(cacheName))
			{
			    Object object = caches.get(cacheName);
			    if(object instanceof Map)
				{
					Map cacheInstance = (Map)object;
					synchronized(cacheInstance) 
					{
						cacheInstance.remove(key);
					}
				}
				else
				{
				    GeneralCacheAdministrator cacheInstance = (GeneralCacheAdministrator)object;
					synchronized(cacheInstance)
					{
						cacheInstance.flushEntry(key);
					}
				}
	
			    logger.info("clearCache stop...");
			}
		}
	}

	/**
	 * This method clears part of a cache.
	 * @param cacheName
	 * @param groups
	 */
	public static void clearCacheForGroup(String cacheName, String group)
	{
		synchronized(caches) 
		{
			if(caches.containsKey(cacheName))
			{
			    Object object = caches.get(cacheName);
			    if(object instanceof Map)
				{
					Map cacheInstance = (Map)object;
					synchronized(cacheInstance)
					{
						cacheInstance.clear();
						logger.warn("Clearing full cache:" + cacheName + " - the call wanted partly clear for [" + group + "] but the cache was a Map.");
					}
				}
				else
				{
				    GeneralCacheAdministrator cacheInstance = (GeneralCacheAdministrator)object;
					synchronized(cacheInstance)
					{
			    		cacheInstance.flushGroup(group);
						logger.info("Clearing cache for group:" + cacheName + " - " + group);
					}
				}
			}
		}
	}

	/**
	 * This method clears part of a cache.
	 * @param cacheName
	 * @param groups
	 */
	public static void clearFileCacheForGroup(GeneralCacheAdministrator cacheInstance, String groupName) throws Exception
	{
		//logger.info("Cache entry set:" + cacheInstance.getCache().cacheMap.entrySet());
		
        Set groupEntries = cacheInstance.getCache().cacheMap.getGroup(groupName);
        /*
        if(groupEntries != null)
        	logger.info("groupEntries for " + groupName + ":" + groupEntries.size());
        else
        	logger.info("no groupEntries for " + groupName);
        */
        
		if (groupEntries != null) 
        {
            Iterator groupEntriesIterator = groupEntries.iterator();
            while (groupEntriesIterator.hasNext()) 
            {
            	String key = (String) groupEntriesIterator.next();
            	CacheEntry entry = (CacheEntry) cacheInstance.getCache().cacheMap.get(key);
            	//logger.info("Removing file with key:" + key);
            	removeCachedContentInFile("pageCache", key);
            	numberOfPageCacheFiles.decrementAndGet();
            }
        }
	}

	public static void clearCaches(String entity, String entityId, String[] cachesToSkip) throws Exception
	{	
		clearCaches(entity, entityId, cachesToSkip, false);
	}
	
	public static void clearCaches(String entity, String entityId, String[] cachesToSkip, boolean forceClear) throws Exception
	{	
		Timer t = new Timer();
		
		long wait = 0;
		//while(RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() > 0)
		while(!forceClear && RequestAnalyser.getRequestAnalyser().getNumberOfActiveRequests() > 0)
	    {
	        //logger.warn("Number of requests: " + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + " was more than 0 - lets wait a bit.");
	        if(wait > 3000)
			{
				logger.warn("The clearCache method waited over " + ((wait * 10) / 1000) + " seconds but there seems to be " + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + " requests blocking all the time. Continuing anyway.");
				//printThreads();
				break;
			}
			Thread.sleep(10);
			wait++;
	    }

	    logger.info("clearCaches start in " + CmsPropertyHandler.getContextRootPath());
		if(entity == null)
		{	
			logger.info("Clearing the caches");
			synchronized(caches)
			{
				for (Iterator i = caches.entrySet().iterator(); i.hasNext(); ) 
				{
					Map.Entry e = (Map.Entry) i.next();
					logger.info("e:" + e.getKey());
					boolean skip = false;
					if(cachesToSkip != null)
					{
						for(int index=0; index<cachesToSkip.length; index++)
						{
						    if(e.getKey().equals(cachesToSkip[index]))
						    {
						        skip = true;
						        break;
						    }
						}
					}
					
					if(!skip)
					{
						Object object = e.getValue();
						if(object instanceof Map)
						{
							Map cacheInstance = (Map)e.getValue();
							synchronized(cacheInstance)
							{
								cacheInstance.clear();
							}
						}
						else
						{
						    GeneralCacheAdministrator cacheInstance = (GeneralCacheAdministrator)e.getValue();
							synchronized(cacheInstance)
							{
						    	cacheInstance.flushAll();
							}
					        eventListeners.clear();
						}
						logger.info("Cleared cache:" + e.getKey());
						
				    	i.remove();
					}
				}
			}
		}
	    else if(entity.equalsIgnoreCase("CacheNames"))
	    {
	    	String[] cacheNames = entityId.split(",");
	    	for(int i=0; i<cacheNames.length; i++)
	    	{
	    		String cacheName = cacheNames[i];
	    		CacheController.clearCache(cacheName);
	    	}
	    }
		else
		{
			logger.info("Clearing some caches");
			logger.info("entity:" + entity);

		    String useSelectivePageCacheUpdateString = CmsPropertyHandler.getUseSelectivePageCacheUpdate();
		    boolean useSelectivePageCacheUpdate = false;
		    if(useSelectivePageCacheUpdateString != null && useSelectivePageCacheUpdateString.equalsIgnoreCase("true"))
		        useSelectivePageCacheUpdate = true;
		        
		    String operatingMode = CmsPropertyHandler.getOperatingMode();

			synchronized(caches)
			{
				for (Iterator i = caches.entrySet().iterator(); i.hasNext(); ) 
				{
					Map.Entry e = (Map.Entry) i.next();
					logger.info("e:" + e.getKey());
					boolean clear = false;
					boolean selectiveCacheUpdate = false;
					String cacheName = e.getKey().toString();
					
					if(cacheName.equalsIgnoreCase("serviceDefinitionCache") && entity.indexOf("ServiceBinding") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("qualifyerListCache") && (entity.indexOf("Qualifyer") > 0 || entity.indexOf("ServiceBinding") > 0))
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("availableServiceBindingCache") && entity.indexOf("AvailableServiceBinding") > 0)
					{	
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("repositoryCache") && entity.indexOf("Repository") > 0)
					{	
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("languageCache") && entity.indexOf("Language") > 0)
					{	
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("localeCache") && entity.indexOf("Language") > 0)
					{	
						clear = true;
					}
					if((cacheName.equalsIgnoreCase("latestSiteNodeVersionCache") || cacheName.equalsIgnoreCase("pageCacheLatestSiteNodeVersions") || cacheName.equalsIgnoreCase("pageCacheSiteNodeTypeDefinition")) && entity.indexOf("SiteNode") > 0)
					{	
						clear = true;
						selectiveCacheUpdate = true;
					}
					if((cacheName.equalsIgnoreCase("parentSiteNodeCache") || cacheName.equalsIgnoreCase("pageCacheParentSiteNodeCache")) && entity.indexOf("SiteNode") > 0)
					{	
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("NavigationCache") && (entity.indexOf("SiteNode") > 0 || entity.indexOf("Content") > 0))
					{	
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("pagePathCache") && (entity.indexOf("SiteNode") > 0 || entity.indexOf("Content") > 0))
					{	
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("componentEditorCache") && (entity.indexOf("SiteNode") > 0 || entity.indexOf("Content") > 0))
					{	
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("componentEditorVersionIdCache") && (entity.indexOf("SiteNode") > 0 || entity.indexOf("Content") > 0))
					{	
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("masterLanguageCache") && (entity.indexOf("Repository") > 0 || entity.indexOf("Language") > 0))
					{	
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("parentRepository") && entity.indexOf("Repository") > 0)
					{	
						clear = true;
					}
					if(cacheName.startsWith("contentAttributeCache") && (entity.indexOf("Content") > -1 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{	
						clear = true;
						selectiveCacheUpdate = true;
					}
					if(cacheName.equalsIgnoreCase("contentVersionCache") && (entity.indexOf("Content") > -1 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{	
						clear = true;
						selectiveCacheUpdate = true;
					}
					if(cacheName.startsWith("contentVersionIdCache") && (entity.indexOf("Content") > -1 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{	
						clear = true;
						selectiveCacheUpdate = true;
					}
					if(cacheName.equalsIgnoreCase("referencingPagesCache") && (entity.indexOf("ContentVersion") > -1 || entity.indexOf("Qualifyer") > 0))
					{	
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("boundSiteNodeCache") && (entity.indexOf("ServiceBinding") > 0 || entity.indexOf("Qualifyer") > 0 || entity.indexOf("SiteNodeVersion") > 0 || entity.indexOf("SiteNodeVersion") > 0 || entity.indexOf("SiteNode") > 0 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("boundContentCache") && (entity.indexOf("ServiceBinding") > 0 || entity.indexOf("Qualifyer") > 0 || entity.indexOf("SiteNodeVersion") > 0 || entity.indexOf("ContentVersion") > 0 || entity.indexOf("Content") > 0 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{
						clear = true;
					}
					if(cacheName.startsWith("pageCache") && entity.indexOf("Registry") == -1)
					{	
						clear = true;
						selectiveCacheUpdate = true;
					}
					if(cacheName.startsWith("pageCacheExtra") && entity.indexOf("Registry") == -1)
					{	
						clear = true;
						selectiveCacheUpdate = true;
					}
					if(cacheName.equalsIgnoreCase("componentCache") && entity.indexOf("Registry") == -1)
					{	
						clear = true;
						selectiveCacheUpdate = true;
					}
					if(cacheName.equalsIgnoreCase("componentPropertyCache") && (entity.indexOf("SiteNode") > -1 || entity.indexOf("ContentVersion") > -1 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{	
						clear = true;
						//selectiveCacheUpdate = true;
					}
					if(cacheName.equalsIgnoreCase("componentPropertyVersionIdCache") && (entity.indexOf("SiteNode") > -1 || entity.indexOf("ContentVersion") > -1 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{	
						clear = true;
						//selectiveCacheUpdate = true;
					}
					if(cacheName.equalsIgnoreCase("pageComponentsCache") && (entity.indexOf("ContentVersion") > -1 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{	
						clear = true;
						//selectiveCacheUpdate = true;
					}
					if(cacheName.equalsIgnoreCase("includeCache"))
					{	
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("authorizationCache") && (entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0 || entity.indexOf("Intercept") > 0))
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("personalAuthorizationCache") && (entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0 || entity.indexOf("Intercept") > 0))
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("componentPaletteDivCache") && (entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{	
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("userCache") && (entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("principalCache") && (entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{
						clear = true;
					}
					if((cacheName.equalsIgnoreCase("assetUrlCache") || cacheName.equalsIgnoreCase("assetThumbnailUrlCache")) && (entity.indexOf("DigitalAsset") > 0 || entity.indexOf("ContentVersion") > 0 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("digitalAssetCache") && (entity.indexOf("DigitalAsset") > 0 || entity.indexOf("ContentVersion") > 0))
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("sortedChildContentsCache") && (entity.indexOf("Content") > 0 || entity.indexOf("ContentVersion") > 0 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("childContentCache") && (entity.indexOf("Content") > 0 || entity.indexOf("ContentVersion") > 0 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("matchingContentsCache") && (entity.indexOf("Content") > 0 || entity.indexOf("ContentVersion") > 0 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("workflowCache") && entity.indexOf("WorkflowDefinition") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("rootSiteNodeCache") && entity.indexOf("SiteNode") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("siteNodeCache") && entity.indexOf("SiteNode") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("contentCache") && entity.indexOf("Content") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("childSiteNodesCache") && entity.indexOf("SiteNode") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("propertySetCache") && entity.indexOf("SiteNode") > 0)
					{
					    clear = true;
					}
					if(cacheName.equalsIgnoreCase("groupVOListCache") && entity.indexOf("Group") > 0)
					{								
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("roleListCache") && entity.indexOf("Role") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("groupPropertiesCache") && entity.indexOf("Group") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("rolePropertiesCache") && entity.indexOf("Role") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("principalPropertyValueCache") && (entity.indexOf("Group") > 0 || entity.indexOf("Role") > 0 || entity.indexOf("User") > 0))
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("relatedCategoriesCache") && (entity.indexOf("Group") > 0 || entity.indexOf("Role") > 0 || entity.indexOf("User") > 0))
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("redirectCache") && entity.indexOf("Redirect") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("interceptorsCache") && entity.indexOf("Intercept") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("interceptionPointCache") && entity.indexOf("Intercept") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("siteNodeLanguageCache") && (entity.indexOf("Repository") > 0 || entity.indexOf("Language") > 0 || entity.indexOf("SiteNode") > 0))
					{
						clear = true;
						selectiveCacheUpdate = true;
					}
					if(cacheName.equalsIgnoreCase("contentTypeDefinitionCache") && entity.indexOf("ContentTypeDefinition") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("ServerNodeProperties"))
					{
						clear = true;
					}
					
					if(!cacheName.equalsIgnoreCase("serverNodePropertiesCache") && entity.equalsIgnoreCase("ServerNodeProperties"))
					{
						clear = true;						
					}
					if(!cacheName.equalsIgnoreCase("encodedStringsCache") && entity.equalsIgnoreCase("ServerNodeProperties"))
					{
						clear = true;						
					}
					
					if(logger.isInfoEnabled())
						logger.info("clear:" + clear);

					if(clear)
					{	
						if(logger.isInfoEnabled())
						    logger.info("clearing:" + e.getKey());

						Object object = e.getValue();
						
						if(object instanceof Map)
						{
							Map cacheInstance = (Map)e.getValue();
						    synchronized(cacheInstance)
						    {
						    	//logger.error("clearing ordinary map:" + e.getKey() + " (" + cacheInstance.size() + ")");
						    	cacheInstance.clear();
						    }
						}
						else
						{
						    GeneralCacheAdministrator cacheInstance = (GeneralCacheAdministrator)e.getValue();
						    synchronized(cacheInstance)
						    {
						    	//ADD logic to flush correct on sitenode and sitenodeversion
						    	/*
						    	if(selectiveCacheUpdate && entity.indexOf("SiteNode") > 0)
							    {
							    	cacheInstance.flushAll();
							    	eventListeners.remove(cacheName + "_cacheEntryEventListener");
								    eventListeners.remove(cacheName + "_cacheMapAccessEventListener");
							    	logger.info("clearing:" + e.getKey());
							    }
							    */
								if(selectiveCacheUpdate && entity.indexOf("Repository") > 0 && useSelectivePageCacheUpdate)
							    {
							    	logger.info("clearing " + e.getKey() + " with group " + "repository_" + entityId);
							    	if(cacheName.equals("pageCacheExtra"))
							    	{
							    		clearFileCacheForGroup(cacheInstance, "repository_" + entityId);
							    		clearFileCacheForGroup(cacheInstance, "selectiveCacheUpdateNonApplicable");
							    	}
							    	else
							    	{
								    	cacheInstance.flushGroup("repository_" + entityId);
								    	cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");							    		
							    	}
							    }
							    else if(selectiveCacheUpdate && entity.indexOf("SiteNodeVersion") > 0)
							    {
							    	logger.info("Getting eventListeners...");
							        Object cacheEntryEventListener = eventListeners.get(e.getKey() + "_cacheEntryEventListener");
						    		Object cacheMapAccessEventListener = eventListeners.get(e.getKey() + "_cacheMapAccessEventListener");

							    	if(cacheName.equals("pageCacheExtra"))
							    	{
							    		clearFileCacheForGroup(cacheInstance, "siteNodeVersion_" + entityId);
							    		clearFileCacheForGroup(cacheInstance, "selectiveCacheUpdateNonApplicable");
							    	}
							    	else
							    	{
							    		cacheInstance.flushGroup("siteNodeVersion_" + entityId);
							    		cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");
							    	}
							    	logger.info("clearing " + e.getKey() + " with group " + "siteNodeVersion_" + entityId);
							    	
							    	try
							    	{
								    	logger.info("BeforesiteNodeVersionVO...");
								    	Integer siteNodeId = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(new Integer(entityId)).getSiteNodeId();
								    	if(siteNodeId != null)
							    		{
									    	logger.info("Before flushGroup2...");
							    			if(cacheName.equals("pageCacheExtra"))
									    		clearFileCacheForGroup(cacheInstance, "siteNode_" + siteNodeId);
							    			else
							    				cacheInstance.flushGroup("siteNode_" + siteNodeId);
									    	logger.info("After flushGroup2...");
							    		}
							    	}
							    	catch(SystemException se)
							    	{
							    		logger.info("Missing content version: " + se.getMessage());
							    	}
							    }
							    else if(selectiveCacheUpdate && (entity.indexOf("SiteNode") > 0 && entity.indexOf("SiteNodeTypeDefinition") == -1) && useSelectivePageCacheUpdate)
							    {
							    	if(cacheName.equals("pageCacheExtra"))
							    	{
							    		clearFileCacheForGroup(cacheInstance, "siteNode_" + entityId);
							    		clearFileCacheForGroup(cacheInstance, "selectiveCacheUpdateNonApplicable");
							    	}
							    	else
							    	{
								    	cacheInstance.flushGroup("siteNode_" + entityId);
								    	cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");
							    	}
							    	logger.info("clearing " + e.getKey() + " with group " + "siteNode_" + entityId);
								}
							    else if(selectiveCacheUpdate && entity.indexOf("ContentVersion") > 0 && useSelectivePageCacheUpdate)
							    {
							    	logger.info("Getting eventListeners...");
							        Object cacheEntryEventListener = eventListeners.get(e.getKey() + "_cacheEntryEventListener");
						    		Object cacheMapAccessEventListener = eventListeners.get(e.getKey() + "_cacheMapAccessEventListener");

							    	logger.info("Before flushGroup...");

							    	if(cacheName.equals("pageCacheExtra"))
							    	{
							    		clearFileCacheForGroup(cacheInstance, "contentVersion_" + entityId);
							    		clearFileCacheForGroup(cacheInstance, "selectiveCacheUpdateNonApplicable");
							    	}
							    	else
							    	{
							    		cacheInstance.flushGroup("contentVersion_" + entityId);
							    		cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");
							    	}
							    	logger.info("clearing " + e.getKey() + " with group " + "contentVersion_" + entityId);
							    	
							    	try
							    	{
								    	logger.info("Before contentVersionVO...");
								    	Integer contentId = ContentVersionController.getContentVersionController().getContentIdForContentVersion(new Integer(entityId));
								    	if(contentId != null)
							    		{
									    	logger.info("Before flushGroup2...");
									    	if(cacheName.equals("pageCacheExtra"))
									    		clearFileCacheForGroup(cacheInstance, "content_" + contentId);
									    	else
									    		cacheInstance.flushGroup("content_" + contentId);

									    	logger.info("After flushGroup2...");
							    		}
							    	}
							    	catch(SystemException se)
							    	{
							    		logger.info("Missing content version: " + se.getMessage());
							    	}
							    }
							    else if(selectiveCacheUpdate && (entity.indexOf("Content") > 0 && entity.indexOf("ContentTypeDefinition") == -1) && useSelectivePageCacheUpdate)
							    {
							    	if(cacheName.equals("pageCacheExtra"))
							    	{
							    		clearFileCacheForGroup(cacheInstance, "content_" + entityId);
							    		clearFileCacheForGroup(cacheInstance, "selectiveCacheUpdateNonApplicable");
							    	}
							    	else
							    	{
								    	cacheInstance.flushGroup("content_" + entityId);
								    	cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");
							    	}
							    	logger.info("clearing " + e.getKey() + " with group " + "content_" + entityId);
								}
							    else if(selectiveCacheUpdate && entity.indexOf("Publication") > 0 && useSelectivePageCacheUpdate && (operatingMode != null && operatingMode.equalsIgnoreCase("3")) && CmsPropertyHandler.getLivePublicationThreadClass().equalsIgnoreCase("org.infoglue.deliver.util.SelectiveLivePublicationThread"))
							    {
							    	logger.info("Now we will ease out the publication...");
									/*
							    	List publicationDetailVOList = PublicationController.getController().getPublicationDetailVOList(new Integer(entityId));
									Iterator publicationDetailVOListIterator = publicationDetailVOList.iterator();
									while(publicationDetailVOListIterator.hasNext())
									{
										PublicationDetailVO publicationDetailVO = (PublicationDetailVO)publicationDetailVOListIterator.next();
										logger.info("publicationDetailVO.getEntityClass():" + publicationDetailVO.getEntityClass());
										logger.info("publicationDetailVO.getEntityId():" + publicationDetailVO.getEntityId());
										if(Class.forName(publicationDetailVO.getEntityClass()).getName().equals(ContentVersion.class.getName()))
										{
											logger.error("We clear all caches having references to contentVersion: " + publicationDetailVO.getEntityId());
											Integer contentId = ContentVersionController.getContentVersionController().getContentIdForContentVersion(publicationDetailVO.getEntityId());

									    	cacheInstance.flushGroup("content_" + contentId);
									    	cacheInstance.flushGroup("contentVersion_" + publicationDetailVO.getEntityId().toString());
									    	cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");
									    	logger.info("clearing " + e.getKey() + " with group " + "content_" + contentId);
									    	logger.info("clearing " + e.getKey() + " with group " + "content_" + contentId);
										}
										else if(Class.forName(publicationDetailVO.getEntityClass()).getName().equals(SiteNodeVersion.class.getName()))
										{
											Integer siteNodeId = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(publicationDetailVO.getEntityId()).getSiteNodeId();
										    CacheController.clearCaches(publicationDetailVO.getEntityClass(), publicationDetailVO.getEntityId().toString(), null);
										}
										
									}
									*/
								}
							    else
							    {
							    	//logger.info("Flushing all:" + cacheName);
							    	cacheInstance.flushAll();
							    	eventListeners.remove(cacheName + "_cacheEntryEventListener");
								    eventListeners.remove(cacheName + "_cacheMapAccessEventListener");
									logger.info("clearing:" + e.getKey());
							    }
							}
						}
						
						logger.info("Cleared cache:" + e.getKey());
	
						if(!selectiveCacheUpdate)
						    i.remove();
						
					}
					else
					{
						logger.info("Did not clear " + e.getKey());
					}
				}
			}
			
    		if(!useSelectivePageCacheUpdate || entity.indexOf("AccessRight") > -1)
    		{
    			logger.info("Clearing all pageCaches");
    			CacheController.clearFileCaches("pageCache");
    		}

		}
				
		logger.info("clearCaches stop");
		long time = t.getElapsedTime();
		if(time > 3000)
			logger.warn("clearCaches took long time:" + time);
	}
	
	private static void printThreads()
	{
    	ThreadGroup tg = Thread.currentThread().getThreadGroup();
	    int n = tg.activeCount();
        logger.warn("Number of active threads: " + n);
	    Thread[] threadArray = new Thread[n];
        n = tg.enumerate(threadArray, false);
        for (int i=0; i<n; i++) 
        {
           	String currentThreadId = "" + threadArray[i].getId();
        	Thread t = threadArray[i];
        	Map stacks = t.getAllStackTraces();
	        
        	Iterator stacksIterator = stacks.values().iterator();
        	while(stacksIterator.hasNext())
        	{
	        	StackTraceElement[] el = (StackTraceElement[])stacksIterator.next();
		        
		        String stackString = "";
		        if (el != null && el.length != 0)
		        {
		            for (int j = 0; j < el.length; j++)
		            {
		            	StackTraceElement frame = el[j];
		            	if (frame == null)
		            		stackString += "    null stack frame" + "<br/>";
		            	else	
		                	stackString += "    null stack frame" + frame.toString() + "<br/>";
					}                    
		            logger.warn("\n\nThreads:\n\n " + stackString);
		       	}
        	}
        }  
	}

	public static synchronized void clearCastorCaches() throws Exception
	{
	    logger.info("Emptying the Castor Caches");
	    
	    //while(RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() > 0)
	    while(RequestAnalyser.getRequestAnalyser().getNumberOfActiveRequests() > 0)
	    {
	        //logger.warn("Number of requests: " + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + " was more than 0 - lets wait a bit.");
			//logger.info("The clearCastorCaches method waited");
	    	Thread.sleep(10);
	    }
	    
		Database db = CastorDatabaseService.getDatabase();
		//CastorDatabaseService.setBlock(true);
		
		try
		{		
		    //db.getCacheManager().expireCache();

		    clearCache(db, SmallContentImpl.class);
		    clearCache(db, SmallishContentImpl.class);
			clearCache(db, MediumContentImpl.class);
			clearCache(db, ContentImpl.class);
			clearCache(db, ContentRelationImpl.class);
			clearCache(db, SmallContentVersionImpl.class);
			clearCache(db, SmallestContentVersionImpl.class);
			clearCache(db, ContentVersionImpl.class);
			clearCache(db, DigitalAssetImpl.class);
			clearCache(db, SmallDigitalAssetImpl.class);
			clearCache(db, MediumDigitalAssetImpl.class);
			clearCache(db, SmallAvailableServiceBindingImpl.class);
			clearCache(db, AvailableServiceBindingImpl.class);
			clearCache(db, ContentTypeDefinitionImpl.class);
			clearCache(db, LanguageImpl.class);
			clearCache(db, RepositoryImpl.class);
			clearCache(db, RepositoryLanguageImpl.class);
			clearCache(db, RoleImpl.class);
			clearCache(db, GroupImpl.class);
			clearCache(db, SmallRoleImpl.class);
			clearCache(db, SmallGroupImpl.class);
			clearCache(db, ServiceDefinitionImpl.class);
			clearCache(db, SiteNodeTypeDefinitionImpl.class);
			clearCache(db, SystemUserImpl.class);
			clearCache(db, SmallSystemUserImpl.class);
			clearCache(db, QualifyerImpl.class);
			clearCache(db, ServiceBindingImpl.class);
			clearCache(db, SmallSiteNodeImpl.class);
			clearCache(db, SiteNodeImpl.class);
			clearCache(db, SiteNodeVersionImpl.class);
			clearCache(db, SmallSiteNodeVersionImpl.class);
			clearCache(db, PublicationImpl.class);
			//clearCache(db, PublicationDetailImpl.class); // This class depends on publication
			clearCache(db, ActionImpl.class);
			clearCache(db, ActionDefinitionImpl.class);
			clearCache(db, ActorImpl.class);
			clearCache(db, ConsequenceImpl.class);
			clearCache(db, ConsequenceDefinitionImpl.class);
			clearCache(db, EventImpl.class);
			clearCache(db, WorkflowImpl.class);
			clearCache(db, WorkflowDefinitionImpl.class);
			clearCache(db, CategoryImpl.class);
			clearCache(db, ContentCategoryImpl.class);
			clearCache(db, RegistryImpl.class);
			clearCache(db, RedirectImpl.class);
			
			clearCache(db, InterceptionPointImpl.class);
			clearCache(db, InterceptorImpl.class);
			clearCache(db, AccessRightImpl.class);
	
			clearCache(db, RolePropertiesImpl.class);
			clearCache(db, UserPropertiesImpl.class);
			clearCache(db, GroupPropertiesImpl.class);
			clearCache(db, UserContentTypeDefinitionImpl.class);
			clearCache(db, RoleContentTypeDefinitionImpl.class);
			clearCache(db, GroupContentTypeDefinitionImpl.class);			

			clearCache(db, PropertiesCategoryImpl.class);
			
			clearCache(db, ServerNodeImpl.class);			

			clearCache(db, SubscriptionImpl.class);
			clearCache(db, FormEntryImpl.class);
			clearCache(db, FormEntryValueImpl.class);

		    //commitTransaction(db);

			logger.info("Emptied the Castor Caches");
		}
		catch(Exception e)
		{
		    logger.error("Exception when tried empty the Castor Caches");
		    rollbackTransaction(db);
		}
		finally
		{
			db.close();
			//CastorDatabaseService.setBlock(false);
		}
	}
	

	public static synchronized void clearCache(Class type, Object[] ids) throws Exception
	{
		clearCache(type, ids, false);
	}
	
	public static synchronized void clearCache(Class type, Object[] ids, boolean forceClear) throws Exception
	{
		long wait = 0;
		//while(RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() > 0)
		while(!forceClear && RequestAnalyser.getRequestAnalyser().getNumberOfActiveRequests() > 0)
	    {
	        //logger.warn("Number of requests: " + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + " was more than 0 - lets wait a bit.");
	        if(wait > 3000)
			{
				logger.warn("The clearCache method waited over " + ((wait * 10) / 1000) + " seconds but there seems to be " + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + " requests blocking all the time. Continuing anyway.");
				//printThreads();
				break;
			}
			
			Thread.sleep(10);
			wait++;
	    }
	    
	    Database db = CastorDatabaseService.getDatabase();

		try
		{
		    CacheManager manager = db.getCacheManager();
		    manager.expireCache(type, ids);
		    //Class[] types = {type};
		    //db.expireCache(types, ids);
		    
		    if(type.getName().equalsIgnoreCase(SmallContentImpl.class.getName()) || 
		       type.getName().equalsIgnoreCase(SmallishContentImpl.class.getName()) ||
		       type.getName().equalsIgnoreCase(MediumContentImpl.class.getName()) ||
		       type.getName().equalsIgnoreCase(ContentImpl.class.getName()) ||
		       type.getName().equalsIgnoreCase(SmallSiteNodeImpl.class.getName()) || 
			   type.getName().equalsIgnoreCase(SiteNodeImpl.class.getName()))
		    {
		        expireDateTime = null;
		        publishDateTime = null;
		    }
		}
		catch(Exception e)
		{
			logger.error("Error clearing cache:" + e.getMessage() + " for type:" + type + ":" + ids + ":" + forceClear);
		}
		finally
		{
			try
			{
				db.close();
			}
			catch (Exception e) 
			{
				logger.error("Error closing database: " + e.getMessage());
			}
		}
	}

	public static synchronized void clearCache(Class c) throws Exception
	{
	    Database db = CastorDatabaseService.getDatabase();

		try
		{
			clearCache(db, c);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			db.close();			
		}
	}	
	
	public static void clearCache(Class type, Object[] ids, Database db) throws Exception
	{
		clearCache(type, ids, false, db);
	}
	
	public static void clearCache(Class type, Object[] ids, boolean forceClear, Database db) throws Exception
	{
		long wait = 0;
	    //while(RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() > 0)
	    while(!forceClear && RequestAnalyser.getRequestAnalyser().getNumberOfActiveRequests() > 0)
	    {
	        if(wait > 3000)
			{
				logger.warn("The clearCache method waited over " + ((wait * 10) / 1000) + " seconds but there seems to be " + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + " requests blocking all the time. Continuing anyway.");
				//printThreads();
				break;
			}

	        Thread.sleep(10);
	        wait++;
	    }

	    CacheManager manager = db.getCacheManager();
	    manager.expireCache(type, ids);
	    //Class[] types = {type};
	    //db.expireCache(types, ids);
	    
	    if(type.getName().equalsIgnoreCase(SmallContentImpl.class.getName()) || 
	 	   type.getName().equalsIgnoreCase(SmallishContentImpl.class.getName()) ||
	       type.getName().equalsIgnoreCase(MediumContentImpl.class.getName()) ||
	       type.getName().equalsIgnoreCase(ContentImpl.class.getName()) ||
	       type.getName().equalsIgnoreCase(SmallSiteNodeImpl.class.getName()) || 
		   type.getName().equalsIgnoreCase(SiteNodeImpl.class.getName()))
	    {
	        expireDateTime = null;
	        publishDateTime = null;
	    }
	}

	private static synchronized void clearCache(Database db, Class c) throws Exception
	{
		Class[] types = {c};
		CacheManager manager = db.getCacheManager();
		manager.expireCache(types);
		//db.expireCache(types, null);
		
	    if(c.getName().equalsIgnoreCase(SmallContentImpl.class.getName()) || 
	 	       c.getName().equalsIgnoreCase(SmallishContentImpl.class.getName()) ||
	       c.getName().equalsIgnoreCase(MediumContentImpl.class.getName()) ||
	       c.getName().equalsIgnoreCase(ContentImpl.class.getName()) ||
	       c.getName().equalsIgnoreCase(SmallSiteNodeImpl.class.getName()) || 
		   c.getName().equalsIgnoreCase(SiteNodeImpl.class.getName()))
	    {
	        expireDateTime = null;
	        publishDateTime = null;
	    }
	}

	
	public void run() 
	{
		while(this.continueRunning && expireCacheAutomatically)
		{
			logger.info("Clearing caches");
			try
			{
			    clearCastorCaches();
			}
			catch(Exception e)
			{
			    logger.error("Error clearing cache in expireCacheAutomatically thread:" + e.getMessage(), e);
			}
			logger.info("Castor cache cleared");
			try
			{
				clearCaches(null, null, null);
			}
			catch(Exception e)
			{
			    logger.error("Error clearing other caches in expireCacheAutomatically thread:" + e.getMessage(), e);
			}
			logger.info("All other caches cleared");
			
			try
			{
				sleep(cacheExpireInterval);
			} 
			catch (InterruptedException e){}
		}
	}

	public static synchronized void cacheCentralCastorCaches() throws Exception
	{
	    Database db = CastorDatabaseService.getDatabase();

	    DatabaseWrapper dbWrapper = new DatabaseWrapper(db);

		try
		{
	    	
	    	beginTransaction(db);
		    
	    	String siteNodesToRecacheOnPublishing = CmsPropertyHandler.getSiteNodesToRecacheOnPublishing();
	    	String recachePublishingMethod = CmsPropertyHandler.getRecachePublishingMethod();
	    	logger.info("siteNodesToRecacheOnPublishing:" + siteNodesToRecacheOnPublishing);
	    	if(siteNodesToRecacheOnPublishing != null && !siteNodesToRecacheOnPublishing.equals("") && siteNodesToRecacheOnPublishing.indexOf("siteNodesToRecacheOnPublishing") == -1)
	    	{
	    	    String[] siteNodeIdArray = siteNodesToRecacheOnPublishing.split(",");
	    	    for(int i=0; i<siteNodeIdArray.length; i++)
	    	    {
	    	        Integer siteNodeId = new Integer(siteNodeIdArray[i]);
	    	    	logger.info("siteNodeId to recache:" + siteNodeId);
	    	    	if(recachePublishingMethod != null && recachePublishingMethod.equalsIgnoreCase("contentCentric"))
	    	    	    new ContentCentricCachePopulator().recache(dbWrapper, siteNodeId);
	    	    	else if(recachePublishingMethod != null && recachePublishingMethod.equalsIgnoreCase("requestCentric"))
	    	    	    new RequestCentricCachePopulator().recache(dbWrapper, siteNodeId);
	    	    	else if(recachePublishingMethod != null && recachePublishingMethod.equalsIgnoreCase("requestAndMetaInfoCentric"))
	    	    	    new RequestAndMetaInfoCentricCachePopulator().recache(dbWrapper, siteNodeId);
	    	    	else
	    	    	    logger.warn("No recaching was made during publishing - set the parameter recachePublishingMethod to 'contentCentric' or 'requestCentric' to recache.");
	    	    }
	    	}
		    
		    commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred when we tried to rebuild the castor cache:" + e.getMessage(), e);
		    rollbackTransaction(db);
		}
		finally
		{
		    closeDatabase(db);
		}
	}
	
	
	public void stopThread()
	{
		this.continueRunning = false;
	}

	public boolean getExpireCacheAutomatically() 
	{
		return expireCacheAutomatically;
	}

	public void setExpireCacheAutomatically(boolean expireCacheAutomatically) 
	{
		this.expireCacheAutomatically = expireCacheAutomatically;
	}

    public static Map getCaches()
    {
        return caches;
    }

    public static Map getEventListeners()
    {
        return eventListeners;
    }

    public static GeneralCacheAdministrator getGeneralCache()
    {
        return generalCache;
    }
        
    public static void validateCaches() throws Exception
    {
    	Iterator cacheKeyIterator = caches.keySet().iterator();
    	while(cacheKeyIterator.hasNext())
    	{
    		String key = (String)cacheKeyIterator.next();
    		Object cacheObject = caches.get(key);
    		//logger.info("cacheObject:" + cacheObject.getClass());
    		if(cacheObject instanceof GeneralCacheAdministrator)
    		{
    			GeneralCacheAdministrator generalCacheAdministrator = (GeneralCacheAdministrator)cacheObject;
    			//logger.info("cacheObject:" + generalCacheAdministrator.getCache().getCacheEventListenerList().getListenerCount());
    			Object cacheEntryEventListener = CacheController.getEventListeners().get(key + "_cacheEntryEventListener");
    			//logger.info("EventListener:" + cacheEntryEventListener);
    			if(cacheEntryEventListener == null)
    			{
    				logger.warn("cacheEntryEventListener was null - lets clear it: " + key);
    				clearCache(key);
    			}
    		}
    	}
    }
    
    public static void evictWaitingCache() throws Exception
    {	    
       	String operatingMode = CmsPropertyHandler.getOperatingMode();
	    synchronized(RequestAnalyser.getRequestAnalyser()) 
	    {
	       	if(RequestAnalyser.getRequestAnalyser().getBlockRequests())
		    {
			    //logger.info("evictWaitingCache allready in progress - returning to avoid conflict");
			    logger.info("evictWaitingCache allready in progress - returning to avoid conflict");
		        return;
		    }

	       	RequestAnalyser.getRequestAnalyser().setBlockRequests(true);
		}

	    logger.info("evictWaitingCache starting");
    	logger.info("blocking");

    	WorkingPublicationThread wpt = new WorkingPublicationThread();
    	
    	SelectiveLivePublicationThread pt = null;
    	String livePublicationThreadClass = "";
    	try
    	{
    		livePublicationThreadClass = CmsPropertyHandler.getLivePublicationThreadClass();
	    	if(operatingMode != null && operatingMode.equalsIgnoreCase("3")) //If published-mode we update entire cache to be sure..
			{
	        	if(livePublicationThreadClass.equalsIgnoreCase("org.infoglue.deliver.util.SelectiveLivePublicationThread"))
	    			pt = new SelectiveLivePublicationThread(notifications);
	        }
    	}
    	catch (Exception e) 
    	{
			logger.error("Could not get livePublicationThreadClass:" + e.getMessage(), e);
		}
    	
    	List localNotifications = new ArrayList();
    	
    	boolean startedThread = false;

    	if(pt == null)
    	{
	    	logger.info("before notifications:" + notifications.size());
		    synchronized(notifications)
	        {
		    	localNotifications.addAll(notifications);
		    	notifications.clear();
	        }
		    
	    	Iterator i = localNotifications.iterator();
			while(i.hasNext())
			{
			    CacheEvictionBean cacheEvictionBean = (CacheEvictionBean)i.next();
			    String className = cacheEvictionBean.getClassName();
			    
				logger.info("className:" + className);
				logger.info("pt:" + pt);
				RequestAnalyser.getRequestAnalyser().addPublication("" + formatter.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " - " + cacheEvictionBean.getClassName() + " - " + cacheEvictionBean.getObjectId());
				
			    if(pt == null)
			    	wpt.getCacheEvictionBeans().add(cacheEvictionBean);
			    //else
			    //	pt.getCacheEvictionBeans().add(cacheEvictionBean);
			       
				try
			    {
					//Here we do what we need to if the server properties has changed.
				    if(className != null && className.equalsIgnoreCase("ServerNodeProperties"))
				    {
						try 
						{
							logger.info("clearing InfoGlueAuthenticationFilter");
							clearServerNodeProperty(true);
							logger.info("cleared InfoGlueAuthenticationFilter");
							InfoGlueAuthenticationFilter.initializeProperties();
							logger.info("initialized InfoGlueAuthenticationFilter");
							logger.info("Shortening page stats");
							RequestAnalyser.shortenPageStatistics();
						} 
						catch (Exception e1) 
						{
							logger.warn("Could not refresh authentication filter:" + e1.getMessage(), e1);
						}
						catch (Throwable t) 
						{
							logger.warn("Could not refresh authentication filter:" + t.getMessage(), t);
						}
				    }
	
				    if(operatingMode != null && !operatingMode.equalsIgnoreCase("3") && className != null && className.equalsIgnoreCase("PortletRegistry"))
				    {
						logger.info("clearing portletRegistry");
						clearPortlets();
						logger.info("cleared portletRegistry");
				    }
	
					if(operatingMode != null && operatingMode.equalsIgnoreCase("3")) //If published-mode we update entire cache to be sure..
					{
						if(!livePublicationThreadClass.equalsIgnoreCase("org.infoglue.deliver.util.SelectiveLivePublicationThread"))
						{	
				    	    logger.info("Starting publication thread...");
			            	PublicationThread lpt = new PublicationThread();
			            	lpt.setPriority(Thread.MIN_PRIORITY);
			            	lpt.start();
			            	startedThread = true;
			            	logger.info("Done starting publication thread...");
			            }
		            }
			    }
			    catch(Exception e)
			    {
			        logger.error("Cache eviction reported an error:" + e.getMessage(), e);
			    }
	
		        logger.info("Cache evicted..");
	
				i.remove();
			}
    	}
    	
		if(operatingMode != null && !operatingMode.equalsIgnoreCase("3"))
		{
			logger.info("Starting the work method");
			//wpt.setPriority(Thread.MAX_PRIORITY);
			//wpt.start();
    		wpt.work();
    		startedThread = true;
        	logger.info("Done starting working publication thread...");
		}

		if(operatingMode != null && operatingMode.equalsIgnoreCase("3") && pt != null) //If published-mode we update entire cache to be sure..
		{
			int size = 0;
			synchronized(notifications)
	        {
				size = notifications.size();
	        }
			
			if(size > 0)
			{
				logger.info("Starting selective publication thread [" + pt.getClass().getName() + "]");
		    	pt.setPriority(Thread.MIN_PRIORITY);
		    	pt.start();
		    	startedThread = true;
		    	logger.info("Done starting publication thread...");
			}
		}
		
	    if(!startedThread)
	    	RequestAnalyser.getRequestAnalyser().setBlockRequests(false);
	    
        logger.info("evictWaitingCache stop");
    }

	public static void clearPortlets()
	{
 		//run registry services to load new portlet info from the registry files
		String[] svcs = 
		{
 			"org.apache.pluto.portalImpl.services.portletdefinitionregistry.PortletDefinitionRegistryService",
 			"org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistryService"
 		};
 	 		
 		int len = svcs.length;
 		for (int i = 0; i < len; i++) 
 		{				
 			try 
 			{
				ServiceManager.hotInit(ServletConfigContainer.getContainer().getServletConfig(), svcs[i]);
 			} 
 			catch (Throwable e) 
 			{
 				String svc = svcs[i].substring(svcs[i].lastIndexOf('.') + 1);
 				String msg = "Initialization of " + svc + " service for hot deployment failed!"; 
 				logger.error(msg);
 				break;
 			}
 	
 			try 
 			{
				ServiceManager.postHotInit(ServletConfigContainer.getContainer().getServletConfig(), svcs[i]);
 			} 
 			catch (Throwable e) 
 			{
 				String svc = svcs[i].substring(svcs[i].lastIndexOf('.') + 1);
 				String msg = "Post initialization of " + svc + " service for hot deployment failed!"; 
 				logger.error(msg);
 				break;
 			}
		}		
 		
        try
		{
			PortletEntityRegistry.load();
		} 
        catch (IOException e)
		{
			e.printStackTrace();
		}

	}
	
	public static String[] getPublicationPersistentCacheNames()
	{
		List<String> caches = new ArrayList<String>();

		caches.add("ServerNodeProperties");
		caches.add("serverNodePropertiesCache");
		caches.add("pageCache");
		caches.add("pageCacheExtra");
		caches.add("componentCache");
		caches.add("NavigationCache");
		caches.add("pagePathCache");
		caches.add("userCache");
		caches.add("pageCacheParentSiteNodeCache");
		caches.add("pageCacheLatestSiteNodeVersions");
		caches.add("pageCacheSiteNodeTypeDefinition");
		caches.add("JNDIAuthorizationCache");
		caches.add("WebServiceAuthorizationCache");
		caches.add("importTagResultCache");

		List<String> userCaches = CmsPropertyHandler.getExtraPublicationPersistentCacheNames();
		logger.info("Adding ExtraPublicationPersistentCacheNames:" + userCaches);
		caches.addAll(userCaches);
		
		String[] cachesArr = caches.toArray(new String[caches.size()]);  
		
		return cachesArr;
	}
	
	
    /**
     * Composer of the pageCacheKey.
     * 
     * @param siteNodeId
     * @param languageId
     * @param contentId
     * @param userAgent
     * @param queryString
     * @return
     */
    
    public static String getPageCacheKey(HttpSession session, HttpServletRequest request, Integer siteNodeId, Integer languageId, Integer contentId, String userAgent, String queryString, String extra)
    {    		
    	String originalRequestURL = request.getParameter("originalRequestURL");
    	if(originalRequestURL == null || originalRequestURL.length() == 0)
    		originalRequestURL = request.getRequestURL().toString();

    	String pageKey = null;
    	String pageKeyProperty = CmsPropertyHandler.getPageKey();
    	if(pageKeyProperty != null && pageKeyProperty.length() > 0)
    	{    
    	    pageKey = pageKeyProperty;
    	    pageKey = pageKey.replaceAll("\\$siteNodeId", "" + siteNodeId);
    	    pageKey = pageKey.replaceAll("\\$languageId", "" + languageId);
    	    pageKey = pageKey.replaceAll("\\$contentId", "" + contentId);
    	    pageKey = pageKey.replaceAll("\\$useragent", "" + userAgent);
    	    pageKey = pageKey.replaceAll("\\$queryString", "" + queryString);
    	    
    	    if(logger.isInfoEnabled())
    			logger.info("Raw pageKey:" + pageKey);
    			
    	    int sessionAttributeStartIndex = pageKey.indexOf("$session.");
    	    while(sessionAttributeStartIndex > -1)
    	    {
        	    int sessionAttributeEndIndex = pageKey.indexOf("_", sessionAttributeStartIndex);
        	    String sessionAttribute = null;
        	    if(sessionAttributeEndIndex > -1)
        	        sessionAttribute = pageKey.substring(sessionAttributeStartIndex + 9, sessionAttributeEndIndex);
        	    else
        	        sessionAttribute = pageKey.substring(sessionAttributeStartIndex + 9);

        	    Object sessionAttributeValue = session.getAttribute(sessionAttribute);
        	    if(sessionAttributeValue == null && sessionAttribute.equalsIgnoreCase("principal"))
        	    	sessionAttributeValue = session.getAttribute("infogluePrincipal");
        	    
        	    if(logger.isInfoEnabled())
        	    	logger.info("sessionAttribute:" + sessionAttribute);

        	    pageKey = pageKey.replaceAll("\\$session." + sessionAttribute, "" + sessionAttributeValue);    	    
    	    
        	    sessionAttributeStartIndex = pageKey.indexOf("$session.");
    	    }
    	   
    	    if(logger.isInfoEnabled())
    			logger.info("after session pageKey:" + pageKey);

    	    int cookieAttributeStartIndex = pageKey.indexOf("$cookie.");
    	    while(cookieAttributeStartIndex > -1)
    	    {
        	    int cookieAttributeEndIndex = pageKey.indexOf("_", cookieAttributeStartIndex);
        	    String cookieAttribute = null;
        	    if(cookieAttributeEndIndex > -1)
        	        cookieAttribute = pageKey.substring(cookieAttributeStartIndex + 8, cookieAttributeEndIndex);
        	    else
        	        cookieAttribute = pageKey.substring(cookieAttributeStartIndex + 8);

        	    HttpHelper httpHelper = new HttpHelper();
        	    pageKey = pageKey.replaceAll("\\$cookie." + cookieAttribute, "" + httpHelper.getCookie(request, cookieAttribute));    	    
    	    
        	    cookieAttributeStartIndex = pageKey.indexOf("$cookie.");
    	    }

    	}
    	else
    	    pageKey  = "" + siteNodeId + "_" + languageId + "_" + contentId + "_" + userAgent + "_" + queryString;
    	
    	return originalRequestURL + "_" + pageKey + extra;
    }
    
    /**
     * Composer of the componentCacheKey.
     * 
     * @param siteNodeId
     * @param languageId
     * @param contentId
     * @param userAgent
     * @param queryString
     * @return
     */
    
    public static String getComponentCacheKey(String keyPattern, String pageKey, HttpSession session, HttpServletRequest request, Integer siteNodeId, Integer languageId, Integer contentId, String userAgent, String queryString, InfoGlueComponent component, String extra)
    {    		
    	String originalRequestURL = request.getParameter("originalRequestURL");
    	if(originalRequestURL == null || originalRequestURL.length() == 0)
    		originalRequestURL = request.getRequestURL().toString();

    	String componentKey = null;
    	if(keyPattern != null && keyPattern.length() > 0)
    	{    
    		componentKey = keyPattern;
    		componentKey = componentKey.replaceAll("\\$siteNodeId", "" + siteNodeId);
    		componentKey = componentKey.replaceAll("\\$languageId", "" + languageId);
    		componentKey = componentKey.replaceAll("\\$contentId", "" + contentId);
    		componentKey = componentKey.replaceAll("\\$useragent", "" + userAgent);
    		componentKey = componentKey.replaceAll("\\$queryString", "" + queryString);
    	    
    		componentKey = componentKey.replaceAll("\\$pageKey", "" + pageKey);
    		componentKey = componentKey.replaceAll("\\$component.id", "" + component.getId());
    		componentKey = componentKey.replaceAll("\\$component.slotName", "" + component.getSlotName());
    		componentKey = componentKey.replaceAll("\\$component.contentId", "" + component.getContentId());
    		componentKey = componentKey.replaceAll("\\$component.isInherited", "" + component.getIsInherited());

    	    int sessionAttributeStartIndex = componentKey.indexOf("$session.");
    	    while(sessionAttributeStartIndex > -1)
    	    {
        	    int sessionAttributeEndIndex = componentKey.indexOf("_", sessionAttributeStartIndex);
        	    String sessionAttribute = null;
        	    if(sessionAttributeEndIndex > -1)
        	        sessionAttribute = componentKey.substring(sessionAttributeStartIndex + 9, sessionAttributeEndIndex);
        	    else
        	        sessionAttribute = componentKey.substring(sessionAttributeStartIndex + 9);

        	    Object sessionAttributeValue = session.getAttribute(sessionAttribute);
        	    if(sessionAttributeValue == null && sessionAttribute.equalsIgnoreCase("principal"))
        	    	sessionAttributeValue = session.getAttribute("infogluePrincipal");

        	    componentKey = componentKey.replaceAll("\\$session." + sessionAttribute, "" + sessionAttributeValue);    	    
    	    
        	    sessionAttributeStartIndex = componentKey.indexOf("$session.");
    	    }
    	    
    	    int cookieAttributeStartIndex = componentKey.indexOf("$cookie.");
    	    while(cookieAttributeStartIndex > -1)
    	    {
        	    int cookieAttributeEndIndex = componentKey.indexOf("_", cookieAttributeStartIndex);
        	    String cookieAttribute = null;
        	    if(cookieAttributeEndIndex > -1)
        	        cookieAttribute = componentKey.substring(cookieAttributeStartIndex + 8, cookieAttributeEndIndex);
        	    else
        	        cookieAttribute = componentKey.substring(cookieAttributeStartIndex + 8);

        	    HttpHelper httpHelper = new HttpHelper();
        	    componentKey = componentKey.replaceAll("\\$cookie." + cookieAttribute, "" + httpHelper.getCookie(request, cookieAttribute));    	    
    	    
        	    cookieAttributeStartIndex = componentKey.indexOf("$cookie.");
    	    }
    	    
    	}
    	
    	return componentKey;
    }

    private static String getCachedContentFromFile(String cacheName, String key, String charEncoding)
    {
    	return getCachedContentFromFile(cacheName, key, null, charEncoding);
    }
    
    private static String getCachedContentFromFile(String cacheName, String key, Integer updateInterval, String charEncoding)
    {
    	Timer t = new Timer();
    	if(!logger.isInfoEnabled())
    		t.setActive(false);

		String compressPageCache = CmsPropertyHandler.getCompressPageCache();

    	String contents = null;
    	try
    	{
    		String firstPart = ("" + key.hashCode()).substring(0, 3);
            String filePath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "caches" + File.separator + cacheName + File.separator + firstPart + File.separator + key.hashCode();
        	//logger.info("Getting from file:" + filePath);

            File file = new File(filePath);
        	//logger.info("Existed:" + file.exists());
            if(file.exists())
            {
	        	//logger.info("updateInterval:" + updateInterval);
	            if(updateInterval != null)
	            {
		            long updateDateTime = file.lastModified();
		            long now = System.currentTimeMillis();
		            //logger.info("diff:" + (now - updateDateTime) / 1000);
		            if((now - updateDateTime) / 1000 < updateInterval)
		            {
		            	if(cacheName.equals("pageCache") && compressPageCache != null && compressPageCache.equals("true"))
		            	{
		            		byte[] cachedCompressedData = FileHelper.getFileBytes(file);
	 		            	if(cachedCompressedData != null && cachedCompressedData.length > 0)
		            		{
		            			contents = compressionHelper.decompress(cachedCompressedData);		
		            		}
		            	}
		            	else
		            	{
			            	//logger.info("getting file anyway:" + updateInterval);
			            	contents = FileHelper.getFileAsStringOpt(file, charEncoding);
		            	}

		            	//contents = FileHelper.getFileAsString(file, charEncoding);
		            	t.printElapsedTime("getFileAsString took");
		            }
		            else
		            {
		            	//logger.info("Old file - skipping:" + ((now - updateDateTime) / 1000));
		            	if(logger.isInfoEnabled())
		        			logger.info("Old file - skipping:" + ((now - updateDateTime) / 1000));
		            }
		        }
	            else
	            {
	            	//logger.info("getting file:" + file);
	            	if(cacheName.equals("pageCache") && compressPageCache != null && compressPageCache.equals("true"))
	            	{
	            		byte[] cachedCompressedData = FileHelper.getFileBytes(file);
 		            	if(cachedCompressedData != null && cachedCompressedData.length > 0)
	            		{
 		            		contents = compressionHelper.decompress(cachedCompressedData);		
	            		}
	            	}
	            	else
	            	{
		            	//logger.info("getting file anyway:" + updateInterval);
		            	contents = FileHelper.getFileAsStringOpt(file, charEncoding);
	            	}
	            	//contents = FileHelper.getFileAsString(file, charEncoding);
	            	t.printElapsedTime("getFileAsString took");
	            }
            }
            else
            {
            	if(logger.isInfoEnabled())
        			logger.info("No filecache existed:" + filePath);
            }
    	}
    	catch (Exception e) 
    	{
    		logger.warn("Problem loading data from file:" + e.getMessage());
    	}
    	
    	t.printElapsedTime("Reading file from disk took");
    	
    	return contents;
    }
    
    private static void putCachedContentInFile(String cacheName, String key, String value, String fileCacheCharEncoding)
    {
    	try
    	{
    		String firstPart = ("" + key.hashCode()).substring(0, 3);
            String dir = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "caches" + File.separator + cacheName + File.separator + firstPart;
            File dirFile = new File(dir);
            dirFile.mkdirs();
            File file = new File(dir + File.separator + key.hashCode());
            File tmpOutputFile = new File(dir + File.separator + Thread.currentThread().getId() + "_tmp_" + key.hashCode());

    		FileHelper.write(tmpOutputFile, value, false, fileCacheCharEncoding);
			
    		if(logger.isInfoEnabled())
    			logger.info("Wrote file..");
            if(tmpOutputFile.exists())
			{
				if(tmpOutputFile.length() == 0)
				{
					tmpOutputFile.delete();
				}
				else
				{
					if(logger.isInfoEnabled())
		    			logger.info("Renaming file " + tmpOutputFile.getAbsolutePath() + " to " + file.getAbsolutePath());
					if(logger.isInfoEnabled())
		    			logger.info("file:" + file.exists() + ":" + file.length());
					if(file.exists())
						file.delete();
					boolean renamed = tmpOutputFile.renameTo(file);
					if(logger.isInfoEnabled())
		    			logger.info("renamed:" + renamed);
					if(cacheName.equals("pageCache"))
						numberOfPageCacheFiles.incrementAndGet();
				}	
			}
    	}
    	catch (Exception e) 
    	{
    		logger.warn("Problem storing data to file:" + e.getMessage());
		}
    }

    private static void putCachedCompressedContentInFile(String cacheName, String key, byte[] value)
    {
    	try
    	{
    		String firstPart = ("" + key.hashCode()).substring(0, 3);
            String dir = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "caches" + File.separator + cacheName + File.separator + firstPart;
            File dirFile = new File(dir);
            dirFile.mkdirs();
            File file = new File(dir + File.separator + key.hashCode());
            File tmpOutputFile = new File(dir + File.separator + Thread.currentThread().getId() + "_tmp_" + key.hashCode());

    		FileHelper.writeToFile(tmpOutputFile, value);
			
    		if(logger.isInfoEnabled())
    			logger.info("Wrote file..");
    		
            if(tmpOutputFile.exists())
			{
				if(tmpOutputFile.length() == 0)
				{
					tmpOutputFile.delete();
				}
				else
				{
					if(logger.isInfoEnabled())
		    			logger.info("Renaming file " + tmpOutputFile.getAbsolutePath() + " to " + file.getAbsolutePath());
					if(logger.isInfoEnabled())
		    			logger.info("file:" + file.exists() + ":" + file.length());
					if(file.exists())
						file.delete();
					boolean renamed = tmpOutputFile.renameTo(file);
					if(logger.isInfoEnabled())
		    			logger.info("renamed:" + renamed);
					if(cacheName.equals("pageCache"))
						numberOfPageCacheFiles.incrementAndGet();
				}	
			}
    	}
    	catch (Exception e) 
    	{
    		logger.warn("Problem storing data to file:" + e.getMessage());
		}
    }

    private static void removeCachedContentInFile(String cacheName, String key)
    {
    	try
    	{
    		String firstPart = ("" + key.hashCode()).substring(0, 3);
            String dir = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "caches" + File.separator + cacheName + File.separator + firstPart;
            File dirFile = new File(dir);
            dirFile.mkdirs();
            File file = new File(dir + File.separator + key.hashCode());
            logger.info("Deleting " + file.getPath());
            boolean deleted = file.delete();
            logger.info("Deleted:" + deleted);
    	}
    	catch (Exception e) 
    	{
    		logger.warn("Problem storing data to file:" + e.getMessage());
		}
    }

    public static void removeScriptExtensionBundles()
    {
		int i = 0;
		String filePath = CmsPropertyHandler.getDigitalAssetPath0();
		while(filePath != null)
		{
			try
			{
				File extensionsDirectory = new File(filePath + File.separator + "extensions");
				extensionsDirectory.mkdirs();
				
				File[] files = extensionsDirectory.listFiles();
				for(int j=0; j<files.length; j++)
				{
					File file = files[j];
					if(!file.isDirectory())
					{
						if(logger.isInfoEnabled())
							logger.info("Deleting file as it is a bundle probably:" + file.getName());
						file.delete();
					}
				}
				
				i++;
				filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
			}
			catch (Exception e) 
			{
				logger.warn("Error trying to write bundled scripts:" + e.getMessage());
			}
		}
    }

	public static void clearFileCaches()
	{
        String dir = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "caches";
        File dirFile = new File(dir);
        Timer t = new Timer();
        if(!logger.isInfoEnabled())
        	t.setActive(false);
        
        //logger.info("dirFile:" + dirFile.exists());
        if(dirFile.exists())
        {
            File[] subCaches = dirFile.listFiles();
            for(int i=0; i<subCaches.length; i++)
            {
            	File subCacheDir = subCaches[i];
            	//logger.info("subCacheDir:" + subCacheDir.getName());
            	if(subCacheDir.isDirectory())
            	{
                	File[] subSubCacheFiles = subCacheDir.listFiles();
                	for(int j=0; j<subSubCacheFiles.length; j++)
                	{
                		File subSubCacheDir = subSubCacheFiles[j];
                    	if(subSubCacheDir.isDirectory())
                    	{
                        	File[] cacheFiles = subSubCacheDir.listFiles();
                        	for(int k=0; k<cacheFiles.length; k++)
                        	{
                        		File cacheFile = cacheFiles[k];
                        		//logger.info("cacheFile:" + cacheFile.getName());
                    			cacheFile.delete();
                        	}

                    		subCacheDir.delete();
                    	}			                

                		//logger.info("cacheFile:" + cacheFile.getName());
                    	subSubCacheDir.delete();
                	}

            		subCacheDir.delete();
               }			                
            }
        }
        t.printElapsedTime("Clearing fileCache took");
	}

	public static void clearFileCaches(String cacheName)
	{
		Timer t = new Timer();
        if(!logger.isInfoEnabled())
        	t.setActive(false);

        String dir = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "caches";
        File dirFile = new File(dir);
        //logger.info("dirFile:" + dirFile.exists());
        if(dirFile.exists())
        {
            File[] subCaches = dirFile.listFiles();
            for(int i=0; i<subCaches.length; i++)
            {
            	File subCacheDir = subCaches[i];
            	//logger.info("subCacheDir:" + subCacheDir.getName());
            	if(subCacheDir.isDirectory() && subCacheDir.getName().equals(cacheName))
            	{
                	File[] subSubCacheFiles = subCacheDir.listFiles();
                	for(int j=0; j<subSubCacheFiles.length; j++)
                	{
                		File subSubCacheDir = subSubCacheFiles[j];
                    	if(subSubCacheDir.isDirectory())
                    	{
                        	File[] cacheFiles = subSubCacheDir.listFiles();
                        	for(int k=0; k<cacheFiles.length; k++)
                        	{
                        		File cacheFile = cacheFiles[k];
                        		//logger.info("cacheFile:" + cacheFile.getName());
                    			cacheFile.delete();
                        	}

                    		subCacheDir.delete();
                    	}			                

                		//logger.info("cacheFile:" + cacheFile.getName());
                    	subSubCacheDir.delete();
                	}

            		subCacheDir.delete();
            	}
            	if(cacheName.equals("pageCache") && numberOfPageCacheFiles.get() > 0)
            		numberOfPageCacheFiles.decrementAndGet();
            }
        }
        t.printElapsedTime("Clearing fileCache took");
	}

	/**
	 * Rollbacks a transaction on the named database
	 */
     /*
	public static void closeTransaction(Database db) throws SystemException
	{
	    //if(db != null && !db.isClosed() && db.isActive())
	        commitTransaction(db);
	}
*/
    
	/**
	 * Begins a transaction on the named database
	 */
         
	public static void beginTransaction(Database db) throws SystemException
	{
		try
		{
			db.begin();
		}
		catch(Exception e)
		{
			logger.error("An error occurred when we tried to begin an transaction. Reason:" + e.getMessage());
			throw new SystemException("An error occurred when we tried to begin an transaction. Reason:" + e.getMessage(), e);    
		}
	}

	/**
	 * Ends a transaction on the named database
	 */
	
    private static void commitTransaction(Database db) throws SystemException
	{
		try
		{
		    if (db.isActive())
		    {
			    db.commit();
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred when we tried to commit an transaction. Reason:" + e.getMessage());
			throw new SystemException("An error occurred when we tried to commit an transaction. Reason:" + e.getMessage(), e);    
		}
	}

 
	/**
	 * Rollbacks a transaction on the named database
	 */
    
	public static void rollbackTransaction(Database db)
	{
		try
		{
			if (db.isActive())
			{
			    db.rollback();
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred when we tried to rollback an transaction. Reason:" + e.getMessage());
		}
	}

	/**
	 * Close the database
	 */
     
	public static void closeDatabase(Database db)
	{
		try
		{
			db.close();
		}
		catch(Exception e)
		{
			logger.error("An error occurred when we tried to close a database. Reason:" + e.getMessage(), e);    
		}
	}


}

