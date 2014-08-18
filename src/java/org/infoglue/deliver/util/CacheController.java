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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.pluto.portalImpl.services.ServiceManager;
import org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistry;
import org.exolab.castor.jdo.CacheManager;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.ObjectNotFoundException;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ComponentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentCategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupPropertiesController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.PropertiesCategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.content.ContentCategoryVO;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.SmallestContentVersionVO;
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
import org.infoglue.cms.entities.management.AccessRightVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl;
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
import org.infoglue.cms.entities.management.impl.simple.SystemUserGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.UserContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.UserPropertiesImpl;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
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
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.sorters.CacheComparator;
import org.infoglue.cms.util.workflow.InfoGlueJDBCPropertySet;
import org.infoglue.deliver.applications.actions.InfoGlueComponent;
import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
import org.infoglue.deliver.cache.PageCacheHelper;
import org.infoglue.deliver.invokers.PageInvoker;
import org.infoglue.deliver.portal.ServletConfigContainer;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.xpath.Xb1XPath;

import com.opensymphony.oscache.base.AbstractCacheAdministrator;
import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.base.algorithm.ImprovedLRUCache;
import com.opensymphony.oscache.base.events.CacheEntryEventListener;
import com.opensymphony.oscache.base.events.CacheMapAccessEventListener;
import com.opensymphony.oscache.extra.CacheEntryEventListenerImpl;
import com.opensymphony.oscache.extra.CacheMapAccessEventListenerImpl;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;


public class CacheController extends Thread
{ 
	private static long readLockTimeout = 5;
	private static long writeLockTimeout = 50;
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
	public static Pattern pattern = null;
		
    public static Date expireDateTime = null;
    public static Date publishDateTime = null;
     
    private static Map<Integer, Integer> assetContentIdMapping = new HashMap<Integer, Integer>();

    private static CompressionHelper compressionHelper = new CompressionHelper();
   
	private static AtomicInteger numberOfPageCacheFiles = new AtomicInteger(0);
	
	//Force mode only happens when a cache eviction has to wait to long. Then we switch to force mode 
    private static class ThreadLocalCacheEvictionMode extends ThreadLocal 
    {
    	public Object initialValue() 
        {
        	return false;
        }
    }

    //boolean forcedCacheEvictionMode = false;
    private static ThreadLocalCacheEvictionMode cem = new ThreadLocalCacheEvictionMode();

    public static Boolean getForcedCacheEvictionMode() 
    {
        return (Boolean) cem.get();
    }
    public static void setForcedCacheEvictionMode(Boolean cemValue) 
    {
    	if(logger.isInfoEnabled())
    		logger.info("Forcing quick cache eviction...");
        cem.set(cemValue);
    }
    
	//Force defeatCaches-mode 
    private static class ThreadLocalDefeatCacheMode extends ThreadLocal<DefeatCacheParameters> 
    {
    	public DefeatCacheParameters initialValue() 
        {
        	return new DefeatCacheParameters();
        }
    }

    private static ThreadLocalDefeatCacheMode defeatCaches = new ThreadLocalDefeatCacheMode();

    public static DefeatCacheParameters getDefeatCaches() 
    {
        return (DefeatCacheParameters) defeatCaches.get();
    }
    
    public static void setDefeatCaches(boolean defeatCache, Map<Class, List<Object>> entities) 
    {
    	DefeatCacheParameters defeatCachesValue = new DefeatCacheParameters();
    	defeatCachesValue.setDefeatCache(defeatCache);
    	if(entities != null)
    		defeatCachesValue.getEntities().putAll(entities);
    	
    	if(logger.isInfoEnabled())
    		logger.info("Forcing defeatCaches...");

    	defeatCaches.set(defeatCachesValue);

    	boolean skipClearCastor = false;
    	if(defeatCaches.get().getEntities() == null || defeatCaches.get().getEntities().size() == 0)
    		skipClearCastor = true;

    	if(defeatCache && defeatCaches.get() != null && !skipClearCastor)
    	{
    		try 
    		{
				clearCastorCaches(defeatCaches.get());
			} 
    		catch (Exception e) 
    		{
    			logger.error("Error setting defeatCaches:" + e.getMessage(), e);
			}
    	}
    	else if(defeatCache && skipClearCastor)
    	{
    		logger.info("Skipping clearCastorCaches as entities was null");
    	}
    }

	//Store extra info 
    private static class ThreadLocalNotificationExtraInformation extends ThreadLocal<Map<String,Map<String,String>>> 
    {
    	public Map<String,Map<String,String>> initialValue() 
        {
        	return new HashMap<String,Map<String,String>>();
        }
    }

    private static ThreadLocalNotificationExtraInformation extraInfo = new ThreadLocalNotificationExtraInformation();

    public static Map<String, String> getExtraInfo(String entityClassName, String entityId) 
    {
    	Map<String, Map<String,String>> allExtra = (Map<String, Map<String,String>>) extraInfo.get();
    	//System.out.println("Returning allExtra for: " + entityClassName + "_" + entityId + ":" + allExtra.get(entityClassName + "_" + entityId));
        return allExtra.get(entityClassName + "_" + entityId);
    }
    
    public static void setExtraInfo(String entityClassName, String entityId, Map<String,String> extraInfoMap) 
    {
    	//System.out.println("Adding extraInfoMap:" + extraInfoMap + " TO " + entityClassName + "_" + entityId);
    	Map<String, Map<String,String>> allExtra = (Map<String, Map<String,String>>) extraInfo.get();
    	//System.out.println("Adding allExtra for: " + entityClassName + "_" + entityId + ":" + allExtra);
    	Map existingMap = getExtraInfo(entityClassName, entityId);
    	if(existingMap == null)
    		allExtra.put(entityClassName + "_" + entityId, extraInfoMap);
    	else 
    		existingMap.putAll(extraInfoMap);
    }

	private static StringPool stringPool = new StringPool(); 
	public static String getPooledString(Integer type, Object id)
	{
		if(type == null || id == null)
		{
			logger.info("Broken was returned from pool:" + type + ":" + id);
			return "broken";
		}
		
		return stringPool.getCanonicalVersion(type, id.toString());
	}
	public static String getPooledString(int hashCode)
	{
		return stringPool.getCanonicalVersion(hashCode);
	}

	public static Integer getPooledStringSize()
	{
		return stringPool.getPoolSize();
	}
	public static Integer getPooledStringHits()
	{
		return stringPool.getHits();
	}
	public static void clearPooledString()
	{
		stringPool.clearPool();
	}
	
	public CacheController()
	{
		super();
	}

	public static void preCacheCMSEntities() throws Exception 
	{
		Timer t = new Timer();
		ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
		logger.warn("pre cache content types took:" + t.getElapsedTime());

		ComponentController.getController().preCacheComponents(-1);
		logger.warn("preCacheComponents took:" + t.getElapsedTime());
		
		List<SiteNodeVO> snVOList = SiteNodeController.getController().getSiteNodeVOList(false, 0, 30000);
		logger.warn("snVOList:" + snVOList.size() + " fetched and precached in " + t.getElapsedTime() + " ms");

		List<ContentVO> cList = ContentController.getContentController().getContentVOList(10000);
		logger.warn("cList:" + snVOList.size() + " fetched and precached in " + t.getElapsedTime() + " ms");
		
		/*
		List<SiteNodeVO> snVOList = SiteNodeController.getController().getSiteNodeVOList(false, 0, 10000);
		logger.info("snVOList:" + snVOList.size() + " fetched and precached");

		List<SiteNodeVersionVO> snvVOList = SiteNodeController.getController().getSiteNodeVersionVOList(false, 0, 10000);
		logger.info("snvVOList:" + snvVOList.size() + " fetched and precached");

		List<ContentVO> cList = ContentController.getContentController().getContentVOList(false, 10000);
		logger.info("cList:" + cList.size() + " fetched and precached");
		
		List<ContentVersionVO> cvList = ContentVersionController.getContentVersionController().getContentVersionVOList(null, false, 0);
		logger.info("cvList:" + cvList.size() + " fetched and precached");
		*/
	}

	public static void preCacheDeliverEntities() throws Exception 
	{
		Timer t = new Timer();
		
		class PreCacheGroupPropertiesTask implements Runnable 
		{
			PreCacheGroupPropertiesTask() { }
	        
	        public void run() 
	        {
				try
				{
					Timer t = new Timer();
					Database db = CastorDatabaseService.getDatabase();
					try 
					{
						beginTransaction(db);

						GroupPropertiesController.getController().preCacheAllGroupProperties(db);
						logger.warn("preCacheAllGroupProperties took: " + t.getElapsedTime());
						commitTransaction(db);
					} 
					catch (Exception e) 
					{
						logger.error("Error precaching all group properties: " + e.getMessage(), e);
						rollbackTransaction(db);
					}
				}
				catch (Exception e) 
				{
					logger.error("Could not start PreCacheTask:" + e.getMessage(), e);
				}
				finally
				{
					GroupPropertiesController.inCacheProgress.set(false);
				}
	        }
	    }
		if(GroupPropertiesController.inCacheProgress.compareAndSet(false, true))
		{
			Thread thread = new Thread(new PreCacheGroupPropertiesTask());
		    thread.start();
		}
		
		if(CacheController.getCacheSize("propertiesCategoryCache") == 0 && GroupPropertiesController.preCacheInProgress.compareAndSet(false, true))
		{
			try
			{
				PropertiesCategoryController.getController().preCacheAllPropertiesCategoryVOList();
			}
			finally
			{
				GroupPropertiesController.preCacheInProgress.set(false);
			}
			logger.warn("preCacheAllPropertiesCategoryVOList took: " + t.getElapsedTime());
		}
		
		ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
		logger.warn("pre cache content types took:" + t.getElapsedTime());
		
		ComponentController.getController().preCacheComponents(-1);
		logger.warn("preCacheComponents took:" + t.getElapsedTime());
		
		List<SiteNodeVO> snVOList = SiteNodeController.getController().getSiteNodeVOList(false, new Integer(CmsPropertyHandler.getOperatingMode()), 30000);
		logger.warn("snVOList:" + snVOList.size() + " fetched and precached in " + t.getElapsedTime() + " ms");

		List<ContentVO> cList = ContentController.getContentController().getContentVOList(10000);
		logger.warn("cList:" + snVOList.size() + " fetched and precached in " + t.getElapsedTime() + " ms");

		/*
		FileInputStream fis = new FileInputStream(CmsPropertyHandler.getDigitalAssetPath() + File.separator + "startupCache.txt");
        ObjectInputStream ois = new ObjectInputStream(fis);
        Map<Integer,Integer> mostUsedContentId = (Map<Integer,Integer>) ois.readObject();
        ois.close();
        */
		
		/*
		List<SiteNodeVO> snVOList = SiteNodeController.getController().getSiteNodeVOList(false, 0, 100000);
		logger.info("snVOList:" + snVOList.size() + " fetched and precached");

		List<SiteNodeVersionVO> snvVOList = SiteNodeController.getController().getSiteNodeVersionVOList(false, 0, 100000);
		logger.info("snvVOList:" + snvVOList.size() + " fetched and precached");

		List<ContentVO> cList = ContentController.getContentController().getContentVOList(false, 100000);
		logger.info("cList:" + cList.size() + " fetched and precached");
		

		List<ContentVersionVO> cvList = ContentVersionController.getContentVersionController().getContentVersionVOList(null, false, 0, 100000);
		logger.info("cvList:" + cvList.size() + " fetched and precached");
		*/
	}

	
	

	public void setCacheExpireInterval(int cacheExpireInterval)
	{
		this.cacheExpireInterval = cacheExpireInterval;
	}

	public static void renameCache(String cacheName, String newCacheName)
	{
		//synchronized(caches) 
		//{
		    Object cacheInstance = caches.get(cacheName);
		    
		    if(cacheInstance != null)
		    {
		        synchronized(cacheInstance)
		        {
		            caches.put(newCacheName, cacheInstance);
		            //locksCache.put(newCacheName, new ReentrantReadWriteLock());
		            caches.remove(cacheName);
		        }
		    }
		//}
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
			
		//synchronized(caches)
		//{
			if(caches.get(cacheName) == null)
			    caches.put(cacheName, new HashMap());
		//}
			
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
		
		if(getDefeatCaches() != null && getDefeatCaches().getDefeatCache("" + cacheName + "_" + key))
		{
			if(!cacheName.equals("serverNodePropertiesCache") &&
			   !cacheName.equals("contentTypeDefinitionCache")
			)
			{
				if (logger.isInfoEnabled())
				{
					logger.info("Missed cache: " + cacheName + " because of defeat caches. Cache.key: " + key);
				}
				return null;
			}
		}
		
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

	public static void cacheObjectInAdvancedCacheWithGroupsAsSet(String cacheName, Object key, Object value, Set<String> groupsAsList, boolean useGroups)
	{
		String[] groups = groupsAsList.toArray(new String[0]);
		
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
		
		if(defeatCaches.get().getDefeatCache())
			defeatCaches.get().setDefeatedKey("" + cacheName + "_" + key.toString());

		/*
		if(cacheName.equalsIgnoreCase("pageCacheExtra"))
			System.out.println("key pageCacheExtra: " + key);
		if(cacheName.equalsIgnoreCase("pageCache"))
			System.out.println("key pageCache: " + key);
		*/
		
		if(logger.isInfoEnabled() && cacheName.equalsIgnoreCase("pageCacheExtra"))
		{			
			logger.info("cacheName: " + cacheName);
			logger.info("key: " + key);
			logger.info("useGroups: " + useGroups);
			logger.info("useFileCacheFallback: " + useFileCacheFallback);
			logger.info("useMemoryCache: "+ useMemoryCache);
			logger.info("groups: " + (groups == null ? "null" : groups.length));
			if(groups != null)
			{
				for(String group : groups)
					logger.info(group + ",");
			}
		}

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
		    		cacheCapacity = "50000";
		    	if(cacheName != null && cacheName.startsWith("metaInfoContentAttributeCache"))
		    		cacheCapacity = "40000";
		    	//else if(cacheName != null && cacheName.startsWith("contentAttributeCache"))
		    	//	cacheCapacity = "1000";
		    	String pageCacheSize = "100";
		    	String pageCacheExtraSize = "300";
		    	if(CmsPropertyHandler.getOperatingMode().equals("3"))
		    	{
		    		pageCacheSize = "10000";
			    	pageCacheExtraSize = "30000";
		    	}
		    	
		    	if(cacheName != null && cacheName.startsWith("pageCache"))
		    		cacheCapacity = pageCacheSize;
		    	if(cacheName != null && cacheName.startsWith("pageCacheExtra"))
		    		cacheCapacity = pageCacheExtraSize;
				if(cacheName != null && cacheName.equalsIgnoreCase("encodedStringsCache"))
					cacheCapacity = "2000";
				if(cacheName != null && cacheName.equalsIgnoreCase("assetUrlCacheWithGroups"))
					cacheCapacity = "10000";
				if(cacheName != null && cacheName.equalsIgnoreCase("importTagResultCache"))
					cacheCapacity = "500";
				if(cacheName != null && cacheName.equalsIgnoreCase("componentPropertyCache"))
					cacheCapacity = "10000";
				if(cacheName != null && cacheName.equalsIgnoreCase("componentPropertyVersionIdCache"))
					cacheCapacity = "10000";
				if(cacheName != null && cacheName.equalsIgnoreCase("componentPropertyCacheRepoGroups"))
					cacheCapacity = "5000";
				if(cacheName != null && cacheName.equalsIgnoreCase("componentPropertyVersionIdCacheRepoGroups"))
					cacheCapacity = "5000";
				if(cacheName != null && cacheName.equalsIgnoreCase("componentEditorCache"))
		    		cacheCapacity = "3000";
				if(cacheName != null && cacheName.equalsIgnoreCase("componentEditorVersionIdCache"))
		    		cacheCapacity = "3000";
				if(cacheName != null && cacheName.equalsIgnoreCase("contentVersionIdCache"))
		    		cacheCapacity = "30000";
				if(cacheName != null && cacheName.equalsIgnoreCase("contentVersionCache"))
		    		cacheCapacity = "30000";
				if(cacheName != null && cacheName.equalsIgnoreCase("pageComponentsCache"))
		    		cacheCapacity = "10000";
				if(cacheName != null && cacheName.equalsIgnoreCase("boundContentCache"))
		    		cacheCapacity = "5000";
				if(cacheName != null && cacheName.equalsIgnoreCase("childSiteNodesCache"))
		    		cacheCapacity = "20000";
				if(cacheName != null && cacheName.equalsIgnoreCase("childPagesCache"))
		    		cacheCapacity = "20000";
				if(cacheName != null && cacheName.equalsIgnoreCase("siteNodeCache"))
		    		cacheCapacity = "20000";
				if(cacheName != null && cacheName.equalsIgnoreCase("contentCache"))
		    		cacheCapacity = "20000";
				if(cacheName != null && cacheName.equalsIgnoreCase("latestSiteNodeVersionCache"))
		    		cacheCapacity = "20000";
				if(cacheName != null && cacheName.equalsIgnoreCase("pageDeliveryMetaDataCache"))
		    		cacheCapacity = "50000";
				
				/*
				if(cacheCapacity.length() > 4)
					cacheCapacity = "10000";
				*/
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
				//locksCache.put(cacheName, new ReentrantReadWriteLock());
		    }
		    
			GeneralCacheAdministrator cacheAdministrator = (GeneralCacheAdministrator)caches.get(cacheName);
			
			//if(cacheName.startsWith("contentAttribute")/* || cacheName.startsWith("contentVersionIdCache")*/)
			//	useGroups = false;

			//boolean containsSelectiveCacheUpdateNonApplicable = false;
			if(groups != null)
			{
				List<String> groupCandidateListStandard = new ArrayList<String>();
				List<String> groupCandidateListSpecial = new ArrayList<String>();
				for(int i=0; i<groups.length; i++)
				{
					if(groups[i].startsWith("content") || groups[i].startsWith("siteNode"))
						groupCandidateListStandard.add(groups[i]);
					else
						groupCandidateListSpecial.add(groups[i]);
				}
				
				if(!groupCandidateListSpecial.contains("selectiveCacheUpdateNonApplicable"))
				{
					groupCandidateListSpecial.addAll(groupCandidateListStandard);
				}
				String[] newGroups = new String[groupCandidateListSpecial.size()];
				groups = groupCandidateListSpecial.toArray(newGroups);
			}
			
			if(logger.isInfoEnabled() && cacheName.equalsIgnoreCase("pageCacheExtra"))
			{
				logger.info("real groups: " + (groups == null ? "null" : groups.length));
				if(groups != null)
				{
					for(String group : groups)
						logger.info(group + ",");
				}
			}
			
			//Kanske tillbaka om minnet sticker
			if(cacheName.equals("componentPropertyCache") || cacheName.equals("componentPropertyVersionIdCache"))
			{				
				//logger.error("Skipping useGroups on " + cacheName + ". Groups was:" + groups.length + " for " + key);
				useGroups = false;
			}
			/*
			if(cacheName.equals("componentPropertyCache") || cacheName.equals("componentPropertyVersionIdCache"))
			{				
				//logger.error("Skipping useGroups on " + cacheName + ". Groups was:" + groups.length + " for " + key);
				useGroups = false;
			}
			*/
						
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
				synchronized(cacheAdministrator) //Back
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
	            	//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Putting object in " + cacheName, t.getElapsedTime());
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
	    	
	    	//System.out.println("Caching in file...:" + value.toString());
	    	if(cacheName.equals("pageCache") && compressPageCache != null && compressPageCache.equals("true"))
	    		putCachedCompressedContentInFile(cacheName, key.toString(), (byte[])value);				    	
	    	else
	    		putCachedContentInFile(cacheName, key.toString(), value.toString(), fileCacheCharEncoding);				    	
	    }

		
		//logger.info("Done cacheObjectInAdvancedCache");
	}
	/*
	public static void printLockDebug(ReentrantReadWriteLock rwl) {
		System.out.println("CurrentThread hasLock: " + rwl.writeLock().isHeldByCurrentThread());
		System.out.println("CurrentThread getWriteHoldCount: " + rwl.getWriteHoldCount());
		System.out.println("CurrentThread getWriteHoldCount: " + rwl.getReadLockCount());
		System.out.println("CurrentThread getQueueLength: " + rwl.getQueueLength());
		System.out.println("CurrentThread hasQueuedThreads: " + rwl.hasQueuedThreads());
		System.out.println("CurrentThread isWriteLocked: " + rwl.isWriteLocked());
	}	
	*/

	public static void cacheObjectInAdvancedCache(String cacheName, Object key, Object value, boolean useFileCacheFallback, boolean useMemoryCache, String fileCacheCharEncoding, Integer memoryCacheSize, boolean unlimitedDiskCache)
	{
		cacheObjectInAdvancedCache(cacheName, key, value, null, false, useFileCacheFallback, useMemoryCache, fileCacheCharEncoding, memoryCacheSize, unlimitedDiskCache, false);
	}
	
	public static void cacheObjectInAdvancedCache(String cacheName, Object key, Object value, String[] groups, boolean useGroups, boolean useFileCacheFallback, boolean useMemoryCache, String fileCacheCharEncoding, Integer memoryCacheSize, boolean unlimitedDiskCache, boolean persistentCachesBetweenRestarts)
	{
		if(cacheName == null || key == null || value == null || key.toString().length() == 0)
			return;
		
		if(defeatCaches.get().getDefeatCache())
			defeatCaches.get().setDefeatedKey("" + cacheName + "_" + key.toString());

		if(logger.isInfoEnabled())
		{			
			logger.info("cacheName: " + cacheName);
			logger.info("key: " + key);
			logger.info("useFileCacheFallback: " + useFileCacheFallback);
			logger.info("useMemoryCache: "+ useMemoryCache);
			logger.info("fileCacheCharEncoding: "+ fileCacheCharEncoding);
			logger.info("memoryCacheSize: "+ memoryCacheSize);
			logger.info("unlimitedDiskCache: "+ unlimitedDiskCache);
			logger.info("persistentCachesBetweenRestarts: "+ persistentCachesBetweenRestarts);
		}

	    if(!caches.containsKey(cacheName))
	    {
	    	GeneralCacheAdministrator cacheAdministrator = null;
			
			Properties p = new Properties();
			
			p.setProperty(AbstractCacheAdministrator.CACHE_MEMORY_KEY, Boolean.toString(useMemoryCache));
			p.setProperty(AbstractCacheAdministrator.CACHE_DISK_UNLIMITED_KEY, Boolean.toString(unlimitedDiskCache));
			p.setProperty(AbstractCacheAdministrator.CACHE_CAPACITY_KEY, "" + memoryCacheSize);
			p.setProperty(AbstractCacheAdministrator.CACHE_PERSISTENCE_OVERFLOW_KEY, Boolean.toString(useFileCacheFallback));
			
			p.setProperty(AbstractCacheAdministrator.CACHE_ALGORITHM_KEY, "com.opensymphony.oscache.base.algorithm.ImprovedLRUCache");
			p.setProperty("cache.persistence.class", "com.opensymphony.oscache.plugins.diskpersistence.DiskPersistenceListener");
			p.setProperty("cache.path", CmsPropertyHandler.getDigitalAssetPath() + File.separator + (persistentCachesBetweenRestarts ? "persistentCaches" : "caches") + File.separator + cacheName);
			
			cacheAdministrator = new GeneralCacheAdministrator(p);
			
			//Adding one entry just to get the disk cache active
			if(useFileCacheFallback && memoryCacheSize == 1)
				 cacheAdministrator.putInCache("dummy", "value");
			
	        //CacheEntryEventListenerImpl cacheEntryEventListener = new ExtendedCacheEntryEventListenerImpl();
			//CacheMapAccessEventListenerImpl cacheMapAccessEventListener = new CacheMapAccessEventListenerImpl(); 
	        
			//cacheAdministrator.getCache().addCacheEventListener(cacheEntryEventListener, CacheEntryEventListener.class);
			//cacheAdministrator.getCache().addCacheEventListener(cacheMapAccessEventListener, CacheMapAccessEventListener.class);
	        caches.put(cacheName, cacheAdministrator);
	        //locksCache.put(cacheName, new ReentrantReadWriteLock());
	        //eventListeners.put(cacheName + "_cacheEntryEventListener", cacheEntryEventListener);
	        //eventListeners.put(cacheName + "_cacheMapAccessEventListener", cacheMapAccessEventListener);
	    }
	    
		GeneralCacheAdministrator cacheAdministrator = (GeneralCacheAdministrator)caches.get(cacheName);
			
		if(groups != null)
		{
			List<String> groupCandidateListStandard = new ArrayList<String>();
			List<String> groupCandidateListSpecial = new ArrayList<String>();
			for(int i=0; i<groups.length; i++)
			{
				if(groups[i].startsWith("content") || groups[i].startsWith("siteNode"))
					groupCandidateListStandard.add(groups[i]);
				else
					groupCandidateListSpecial.add(groups[i]);
			}
			
			if(!groupCandidateListSpecial.contains("selectiveCacheUpdateNonApplicable"))
			{
				groupCandidateListSpecial.addAll(groupCandidateListStandard);
			}
			String[] newGroups = new String[groupCandidateListSpecial.size()];
			groups = groupCandidateListSpecial.toArray(newGroups);
		}
		
		if(CmsPropertyHandler.getUseSynchronizationOnCaches())
		{
    		synchronized(cacheAdministrator) //Back
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
		
		if(getDefeatCaches().getDefeatCache("" + cacheName + "_" + key))
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
		    		synchronized(cacheAdministrator) //Back
		    		{
		    		    try 
					    {
					    	if(CmsPropertyHandler.getUseHashCodeInCaches())
								value = (cacheAdministrator == null) ? null : cacheAdministrator.getFromCache("" + key.hashCode(), CacheEntry.INDEFINITE_EXPIRY);
							else
								value = (cacheAdministrator == null) ? null : cacheAdministrator.getFromCache(key, CacheEntry.INDEFINITE_EXPIRY);

					    	//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Getting from cache on " + cacheName, t.getElapsedTime());
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

					    	//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Getting object 3 part 2 from " + cacheName, t.getElapsedTime());
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

	public static Object getCachedObjectFromAdvancedCache(String cacheName, String key, boolean useFileCacheFallback, String fileCacheCharEncoding, boolean cacheFileResultInMemory, Object o, Method m, Object[] args, PageInvoker pageInvoker) throws Exception
	{
		if(cacheName == null || key == null || key.length() == 0)
			return null;
		
	    Object value = null;
	    
    	String pageKey = key;
    	//if(CmsPropertyHandler.getUseHashCodeInCaches())
    	//	pageKey = "" + key.hashCode();

	    GeneralCacheAdministrator cacheAdministrator = (GeneralCacheAdministrator)caches.get(cacheName);
	    if(cacheAdministrator == null)
	    {
	    	Map cacheSettings = (Map)getCachedObject("serverNodePropertiesCacheSettings", "cacheSettings");
	    	if(cacheSettings == null)
	    	{
	    		cacheSettings = CmsPropertyHandler.getCacheSettings();
	    		cacheObject("serverNodePropertiesCacheSettings", "cacheSettings", cacheSettings);
	    	}
	    	
	    	String cacheCapacity = "20000";
	    	String cacheCapacityProperty = (String)cacheSettings.get("CACHE_CAPACITY_" + cacheName);
	    	if(cacheCapacityProperty != null && !cacheCapacityProperty.equals(""))
	    		cacheCapacity = cacheCapacityProperty;
	    		
			if(cacheCapacity != null && !cacheCapacity.equals(""))
	    	{
				Properties p = new Properties();
		    	
				logger.info("Creating cache " + cacheName + " with:" + cacheCapacity);
				p.setProperty(AbstractCacheAdministrator.CACHE_ALGORITHM_KEY, "com.opensymphony.oscache.base.algorithm.ImprovedLRUCache");
				p.setProperty(AbstractCacheAdministrator.CACHE_CAPACITY_KEY, cacheCapacity);
				cacheAdministrator = new GeneralCacheAdministrator(p);
			}
			else
			{
				cacheAdministrator = new GeneralCacheAdministrator();
				logger.info("Creating cache without limit" + cacheName);
			}
			
			if(cacheAdministrator.getCache().cacheMap instanceof ImprovedLRUCache)
				((ImprovedLRUCache)cacheAdministrator.getCache().cacheMap).setIsPageCache(true);
			
			CacheEntryEventListenerImpl cacheEntryEventListener = new ExtendedCacheEntryEventListenerImpl();
			CacheMapAccessEventListenerImpl cacheMapAccessEventListener = new CacheMapAccessEventListenerImpl(); 
	        
	        cacheAdministrator.getCache().addCacheEventListener(cacheEntryEventListener, CacheEntryEventListener.class);
			cacheAdministrator.getCache().addCacheEventListener(cacheMapAccessEventListener, CacheMapAccessEventListener.class);
			caches.put(cacheName, cacheAdministrator);
			//locksCache.put(cacheName, new ReentrantReadWriteLock());
	    }
	    
	    
	    if(logger.isInfoEnabled())
	    	logger.info("Getting from file: " + pageKey);
	    Object pageCacheFileName = getCachedObjectFromAdvancedCache(cacheName, pageKey, CacheEntry.INDEFINITE_EXPIRY);
	    if(logger.isInfoEnabled())
	    	logger.info("pageCacheFileName:" + pageCacheFileName + " from " + cacheName + " and key: " + pageKey);
	    if(pageCacheFileName != null && !pageCacheFileName.equals(""))
	    	value = PageCacheHelper.getInstance().getCachedPageString(pageKey, new File(pageCacheFileName.toString()));
	    else
	    	logger.info("No page file name in memory cache:" + pageKey);
	    //System.out.println("value:" + value);
	    //value = getCachedObjectFromAdvancedCache(cacheName, pageKey, CacheEntry.INDEFINITE_EXPIRY);
		if(value == null)
		{
			logger.info("Invoking:" + cacheName);
		    String result = (String)m.invoke(o, args);
		    logger.info("result:" + (result == null ? "null" : result.length()));
			value = result;
			if(result != null)
			{
		    	 cacheNewResult(pageInvoker, cacheAdministrator, pageKey, result);
			}
	    	value = result;
		}
		
	    if(value != null && value instanceof byte[])
	    	value = compressionHelper.decompress((byte[])value);
	    
	    
		return value;
	}

	public static Map<String,Object> getCachedObjectsFromAdvancedCacheFilteredOnKeyEnd(String cacheName, String keyNameFilter)
	{
		if(cacheName == null)
			return null;
		
		if(getDefeatCaches().getDefeatCache())
			return null;
		
		Map<String,Object> cachedValues = new HashMap<String,Object>();
		
		GeneralCacheAdministrator cacheAdministrator = (GeneralCacheAdministrator)caches.get(cacheName);
		if(cacheAdministrator != null)
		{
    		synchronized(cacheAdministrator) //Back
    		{
			    try 
			    {
			    	if(cacheAdministrator != null)
			    	{
			    		for(String key : (Set<String>)cacheAdministrator.getCache().cacheMap.keySet())
			    		{
			    			if(key.toString().endsWith(keyNameFilter))
			    			{
			    				try 
			    			    {
				    				cachedValues.put(key, cacheAdministrator.getCache().getFromCache(key));
			    			    }
			    				catch (NeedsRefreshException nre) 
							    {
			    					logger.info("Stale cache entry");
							        cacheAdministrator.cancelUpdate(key);
								}
			    			}
			    		}
			    	}
			    } 
			    catch (Exception e) 
			    {
			    	logger.error("Problem getting all cache keys and values:" + e.getMessage(), e);
				}
	    	}
		}
	    
		return cachedValues;
	}
	
	private static boolean cacheNewResult(PageInvoker pageInvoker, GeneralCacheAdministrator cacheAdministrator, String pageKey, String value) 
	{
		/*
		int argumentCount = StringUtils.countMatches(pageKey, "&");
		if(argumentCount > 4)
		{
			logger.error("Skipping caching as we don't cache url:s with more than 3 params:" + pageKey);
			return false;
		}
		argumentCount = StringUtils.countMatches(pageKey, "/");
		if(argumentCount > 7)
		{
			logger.error("Skipping caching as we don't cache url:s with more than 7 slashes:" + pageKey);
			return false;
		}
		*/

		boolean isCached = false;
		
		String pageCacheExtraName = "pageCacheExtra";
		if(!pageInvoker.getTemplateController().getIsPageCacheDisabled() && !pageInvoker.getDeliveryContext().getDisablePageCache()) //Caching page if not disabled
		{
			Integer newPageCacheTimeout = pageInvoker.getDeliveryContext().getPageCacheTimeout();
			if(newPageCacheTimeout == null)
				newPageCacheTimeout = pageInvoker.getTemplateController().getPageCacheTimeout();
			
			//String[] allUsedEntitiesCopy = pageInvoker.getDeliveryContext().getAllUsedEntities().clone();
			List<String> allUsedEntitiesSet = pageInvoker.getDeliveryContext().getAllUsedEntitiesAsSet();
			//String[] allUsedEntitiesCopy = allUsedEntitiesSet.toArray(new String[0]);
			//System.out.println("allUsedEntitiesSet:" + allUsedEntitiesSet.size());
			
			String pageCacheExclusionsRegexp = null;
			Integer pageCacheMaxGroups = 500;
			try
			{
				Map cacheSettings = CmsPropertyHandler.getCacheSettings();
				//System.out.println("cacheSettings:" + cacheSettings);
		    	if(cacheSettings != null)
		    	{
			    	pageCacheExclusionsRegexp = (String)cacheSettings.get("PAGE_CACHE_EXCLUSIONS");
			    	String pageCacheMaxGroupsString = (String)cacheSettings.get("PAGE_CACHE_MAX_GROUPS");
			    	if(pageCacheMaxGroupsString != null)
			    		pageCacheMaxGroups = new Integer(pageCacheMaxGroupsString);
			    	
			    	if(pageCacheExclusionsRegexp != null && !pageCacheExclusionsRegexp.equals(""))
						pattern = Pattern.compile(pageCacheExclusionsRegexp);
			    }
			}
			catch (Exception e) 
			{
				logger.warn("cacheSettings was null:" + e.getMessage(), e);
			}
			if(logger.isInfoEnabled())
			{
				logger.info("pageCacheExclusionsRegexp:" + pageCacheExclusionsRegexp);
				logger.info("pageCacheMaxGroups:" + pageCacheMaxGroups);
			}
			
			boolean detailedRelations = true;
			if(allUsedEntitiesSet.size() > pageCacheMaxGroups)
				detailedRelations = false;

			//System.out.println("detailedRelations:" + detailedRelations);
			//System.out.println("allUsedEntitiesSet:" + allUsedEntitiesSet.size());

			Object extraData = pageInvoker.getDeliveryContext().getExtraData();
			List<String> allUsedEntitiesFilteredCopy = new ArrayList<String>();
			for(String s : allUsedEntitiesSet)
			{
				if(detailedRelations)
				{
					if(s.startsWith("content_") && s.indexOf("_", 8) > -1)
					{
						//allUsedEntitiesFilteredCopy.add(getPooledString(s.hashCode()));
						allUsedEntitiesFilteredCopy.add(s); //(getPooledString(s.hashCode()));
						//System.out.println("Added: " + s);
					}
					else if(s.startsWith("siteNode_"))
						allUsedEntitiesFilteredCopy.add(s);
					else if(s.startsWith("selectiveCacheUpdateNonApplicable"))
						allUsedEntitiesFilteredCopy.add(s);
				}
				else
				{
					if(s.startsWith("content_") && s.indexOf("_", 8) == -1)
					{
						allUsedEntitiesFilteredCopy.add(s);
						//allUsedEntitiesFilteredCopy.add(s); //getPooledString(s.hashCode()));
						//System.out.println("Added: " + s);
					}
					else if(s.startsWith("siteNode_"))
						allUsedEntitiesFilteredCopy.add(s);
					else if(s.startsWith("selectiveCacheUpdateNonApplicable"))
						allUsedEntitiesFilteredCopy.add(s);
				}
			}
			//System.out.println("allUsedEntitiesFilteredCopy:" + allUsedEntitiesFilteredCopy.size());

			try
			{
		    	logger.info("pageCacheExclusionsRegexp:" + pageCacheExclusionsRegexp);
		    	logger.info("pageCacheMaxGroups:" + pageCacheMaxGroups);
			
				if(pattern != null)
				{
			        Matcher matcher = pattern.matcher(pageKey);
			        if(matcher.find())
			    	{
				    	logger.info("Skipping caching key 1:" + pageKey);
			    		return false;
			    	}
			    	if(pageCacheMaxGroups != null && !pageCacheMaxGroups.equals("") && allUsedEntitiesFilteredCopy.size() > pageCacheMaxGroups)
			    	{
			    		logger.info("Skipping caching key 2:" + pageKey);
			    		return false;
			    	}
			    	matcher.reset();
				}
			}
			catch (Exception e) 
			{
				logger.warn("cacheSettings was null:" + e.getMessage(), e);
			}

			String[] allUsedEntitiesCopy = allUsedEntitiesFilteredCopy.toArray(new String[0]);
			//System.out.println("allUsedEntitiesCopy:" + allUsedEntitiesCopy.length);
			logger.info("allUsedEntitiesCopy:" + allUsedEntitiesCopy.length);
			logger.info("Caching:" + pageKey);

			synchronized (cacheAdministrator) 
			{
				byte[] compressedData = compressionHelper.compress(value);		
			    //logger.info("Compressing page for pageCache took " + (System.currentTimeMillis() - startCompression) + " with a compressionFactor of " + (this.pageString.length() / compressedData.length));
				if(pageInvoker.getTemplateController().getOperatingMode().intValue() == 3 && !CmsPropertyHandler.getLivePublicationThreadClass().equalsIgnoreCase("org.infoglue.deliver.util.SelectiveLivePublicationThread"))
				{
					PageCacheHelper.getInstance().cachePageString(pageKey, compressedData);
					//cacheAdministrator.putInCache(pageKey, compressedData, allUsedEntitiesCopy);
					isCached = true;
					//CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey, extraData, allUsedEntitiesCopy, false);
					//CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_pageCacheTimeout", newPageCacheTimeout, allUsedEntitiesCopy, false);    
				}
				else
				{
					//FIX THE part above as well
					//System.out.println("Caching in pageCache");
					PageCacheHelper.getInstance().cachePageString(pageKey, compressedData);
					//cacheAdministrator.putInCache(pageKey, compressedData, allUsedEntitiesCopy);
					isCached = true;

					/*
					if(pageInvoker.getTemplateController().getHttpServletRequest().getParameter("listMostUsed") != null)
					{
						System.out.println("These contents were used before:" + ContentDeliveryController.mostUsedContentIds.size());
						Map<Integer,Integer> contentIds = ContentDeliveryController.mostUsedContentIds;
						for(Integer contentId : contentIds.keySet())
						{
							if(contentIds.get(contentId) > 3)
								System.out.println("" + contentId + ":" + contentIds.get(contentId));
						}
					}
					*/
					if(pageInvoker.getTemplateController().getHttpServletRequest().getParameter("listEntities") != null)
					{
						List<String> debug = Arrays.asList(allUsedEntitiesCopy);
						Collections.sort(debug);
						for(String s : debug)
						{
							System.out.println("s:" + s);
						}
					}
					
					CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey, extraData, null, false);
					CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_pageCacheTimeout", newPageCacheTimeout, null, false);    
					//CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_entities", allUsedEntitiesCopy, null, false);
					String allUsedEntitiesCopyAsString = StringUtils.join(allUsedEntitiesCopy, "|");
					if(logger.isInfoEnabled())
					{
						logger.info("pageKey:" + pageKey);
						logger.info("allUsedEntitiesCopyAsString:" + allUsedEntitiesCopyAsString);
					}
					
					if(CmsPropertyHandler.getOperatingMode().equals("0"))
					{
						CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_entitiesAsByte", allUsedEntitiesCopyAsString);
					}
					else
					{
						byte[] compressedAllUsedEntitiesCopyAsString = compressionHelper.compress(allUsedEntitiesCopyAsString);
						CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_entitiesAsByte", compressedAllUsedEntitiesCopyAsString);						
					}
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
		
		if(getDefeatCaches().getDefeatCache("" + cacheName + "_" + key))
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

	/**
	 * This method let's you get a list of caches and entries in which the given entity is located.
	 * @param entityName
	 * @param entityId
	 * @throws Exception 
	 */
	public static String debugCache(String entityName, String entityId, String cacheNamesToDebug, String forceClear) throws Exception
	{
		StringBuffer debug = new StringBuffer();
		
		if(entityName.equalsIgnoreCase("content"))
			entityName = "content";
		if(entityName.equalsIgnoreCase("sitenode"))
			entityName = "siteNode";
		logger.error("Debugging " + entityName + "=" + entityId + " (" + cacheNamesToDebug + ")" + " - forceClear:" + forceClear);
		
		/*
		Boolean isEntityPublicationProcessed = false;
		try
		{
			isEntityPublicationProcessed = PublicationController.getController().getIsEntityPublicationProcessed(entityName, entityId);
			System.out.println("isEntityPublicationProcessed: " + isEntityPublicationProcessed);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		*/
		if(cacheNamesToDebug == null || cacheNamesToDebug.equals(""))
			cacheNamesToDebug = "contentVersionCache,contentVersionIdCache,contentCache,matchingContentsCache,pageCache,pageCacheExtra";
		logger.info("cacheNamesToDebug:" + cacheNamesToDebug);
		
		Set<String> pageCacheNotifications = new HashSet<String>();
		List<String[]> entriesToClear = new ArrayList<String[]>();
		
		String[] cacheNamesToDebugArray = cacheNamesToDebug.split(",");
		List<String> cacheNamesToDebugList = Arrays.asList(cacheNamesToDebugArray);
		synchronized(caches) 
		{
			for (Iterator i = caches.entrySet().iterator(); i.hasNext(); ) 
			{
				Map.Entry e = (Map.Entry) i.next();
				String cachName = (String)e.getKey();
				//System.out.println("Cache: " + cachName);
				if(cacheNamesToDebugList.contains(cachName))
				{
					//debug.append("Checked: " + cachName + ",");
					Object object = e.getValue();
					if(object instanceof Map)
					{
						Map cacheInstance = (Map)e.getValue();
						synchronized(cacheInstance)
						{
							Set<Map.Entry<String,Object>> entrySet = cacheInstance.entrySet();
							logger.info("entrySet:" + entrySet.size());
							Iterator<Map.Entry<String,Object>> entrySetIterator = entrySet.iterator();
							while(entrySetIterator.hasNext())
							{
								Map.Entry<String,Object> entry = entrySetIterator.next();
								if(entry.getKey().toLowerCase().contains(entityName.toLowerCase()) || entry.getKey().contains("selectiveCacheUpdateNonApplicable"))
								{
									debug.append("" + cachName + "=" + entry.getKey() + ",");
									entriesToClear.add(new String[]{cachName, entry.getKey()});
								}
							}
						}
					}
					else
					{
					    GeneralCacheAdministrator cacheInstance = (GeneralCacheAdministrator)e.getValue();
						synchronized(cacheInstance)
						{
							pageCacheNotifications.add(entityName + "_" + entityId);

							Set<String> entries = cacheInstance.getCache().getGroup(entityName + "_" + entityId);
							logger.info("entries:" + entries.size());
							for(String entry : entries)
							{
								//debug.append("" + cachName + "=" + entry + ",");
								entriesToClear.add(new String[]{cachName, entry});
							}
							
							try
							{
								pageCacheNotifications.add("selectiveCacheUpdateNonApplicable");

								if(entityName.toLowerCase().equals("content"))
								{
									List<ContentTypeDefinitionVO> contentTypeDefinitionVOList = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
									for(ContentTypeDefinitionVO ctdVO : contentTypeDefinitionVOList)
									{
										//System.out.println("Group:" + "selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + ctdVO.getId());
										Set<String> selectiveCacheUpdateNonApplicableEntries = cacheInstance.getCache().getGroup("selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + ctdVO.getId());
										for(String entry : selectiveCacheUpdateNonApplicableEntries)
										{
											//debug.append("selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + ctdVO.getId() + "/" + cachName + "=" + entry + ",");
											//debug.append("selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + ctdVO.getId() + ",");
											entriesToClear.add(new String[]{cachName, entry});
											pageCacheNotifications.add("selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + ctdVO.getId());
										}
									}
								}
								Set<String> selectiveCacheUpdateNonApplicableEntries = cacheInstance.getCache().getGroup("selectiveCacheUpdateNonApplicable");
								for(String entry : selectiveCacheUpdateNonApplicableEntries)
								{
									//debug.append("selectiveCacheUpdateNonApplicable: " + cachName + "=" + entry + ",");
									entriesToClear.add(new String[]{cachName, entry});
									pageCacheNotifications.add("selectiveCacheUpdateNonApplicable");
								}
							}
							catch (Exception e2) 
							{
								e2.printStackTrace();
							}
						}
					}
				}
				else
					logger.info("Skipping cache:" + cachName);
			}
		}
				
		if(forceClear != null && forceClear.equalsIgnoreCase("true"))
		{
			logger.info("********** Clearing caches *********");
			String entityNameFull = ContentImpl.class.getName();
			if(entityName.toLowerCase().contains("sitenode"))
				entityNameFull = SiteNodeImpl.class.getName();
			CacheController.clearCaches(entityNameFull, entityId, null);
		}
		
		for(String[] clearEntries : entriesToClear)
		{
			debug.append("" + clearEntries[0] + "=" + clearEntries[1] + ",");
			if(forceClear != null && forceClear.equalsIgnoreCase("true"))
			{
				clearCacheHard(clearEntries[0], clearEntries[1]);
			}
		}
		for(String pageCacheNotification : pageCacheNotifications)
		{
			logger.info("Asking page cache to clear on: " + pageCacheNotification);
			debug.append("PageCache: " + pageCacheNotification + ",");
			if(forceClear != null && forceClear.equalsIgnoreCase("true"))
				PageCacheHelper.getInstance().notify(pageCacheNotification);
		}
		if(forceClear != null && forceClear.equalsIgnoreCase("true"))
			System.out.println("************************************");
				
		if(entityName.contains("siteNode"))
		{
			SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(new Integer(entityId));
			if(siteNodeVO != null && siteNodeVO.getMetaInfoContentId() != null)
			{
				debug.append(debugCache("Content", siteNodeVO.getMetaInfoContentId().toString(), cacheNamesToDebug, forceClear));
			}
		}
		
		return debug.toString();
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
	
	public static void clearUserAccessCache(String accessRightKey)
	{
		if(caches.containsKey("userAccessCache"))
		{
		    Object object = caches.get("userAccessCache");
		    
			Map cacheInstance = (Map)object;
			synchronized(cacheInstance)
			{
				for(Object personalCache : cacheInstance.values())
				{
					//System.out.println("personalCache:" + personalCache);
					if(personalCache instanceof Map)
					{
						//System.out.println("personalCache contains:" + ((Map)personalCache).containsKey(accessRightKey));
						//((Map) personalCache).remove(accessRightKey);
						((Map) personalCache).put(accessRightKey, -1);	
						//System.out.println("personalCache contains after:" + ((Map)personalCache).containsKey(accessRightKey));
					}
				}
			}

		    logger.info("clearCache stop...");
		}
	}
	
	public static int getCacheSize(String cacheName)
	{
		int cacheSize = 0;
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
						cacheSize = cacheInstance.size();
					}
				}
				else
				{
				    GeneralCacheAdministrator cacheInstance = (GeneralCacheAdministrator)object;
		    		synchronized(cacheInstance) //Back
		    		{
				    /*
		    		Timer t = new Timer();
		    		ReentrantReadWriteLock rwl = locksCache.get(cacheName);
		    		boolean lockSuccess = false;
		    		try {
		    			System.out.println("YAAAAAAAAAAAAA 2");
						lockSuccess = rwl.readLock().tryLock(readLockTimeout, TimeUnit.SECONDS);
					} catch (InterruptedException e1) {
						RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Error getting lock in getCacheSize on " + cacheName, t.getElapsedTime());
						printLockDebug(rwl);
						return 0;
					}
		    		//rwl.readLock().lock();
		            try
		            {
		            	if(!lockSuccess)
		            	{
							RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Timeout getting lock in getCacheSize on " + cacheName, t.getElapsedTime());
		            		return 0;
		            	}
			    	*/	
						cacheSize = cacheInstance.getCache().getSize();
						
				    	//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Getting getCacheSize on " + cacheName, t.getElapsedTime());
					}
		    		/*
		            finally
		            {
		            	if(lockSuccess)
		            		rwl.readLock().unlock();
		            }
		            */
				}
			}
		}
		return cacheSize;
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
				    synchronized(cacheInstance) //Back
		    		{
				    /*	
		    		Timer t = new Timer();
		    		ReentrantReadWriteLock rwl = locksCache.get(cacheName);
		    		boolean lockSuccess = false;
		    		try {
						lockSuccess = rwl.writeLock().tryLock(writeLockTimeout, TimeUnit.SECONDS);
						printLockDebug(rwl);
					} catch (InterruptedException e1) {
						return;
					}
		    		//rwl.writeLock().lock();
		            try
		            {
		            	if(!lockSuccess)
		            	{
							printLockDebug(rwl);
		            		return;
		            	}
				    	
				    	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Getting lock for flushing " + cacheName, t.getElapsedTime());
		            	*/
						cacheInstance.flushAll();
				    	cacheInstance.destroy();

				    	//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Flushing " + cacheName, t.getElapsedTime());
					}
				    /*
		            finally
		            {
		            	if(lockSuccess)
		            		rwl.writeLock().unlock();
		            }
		            */
				}
		    	caches.remove(cacheName);
	
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
		    		synchronized(cacheInstance) //Back
		    		{
				    /*
				    Timer t = new Timer();
		    		ReentrantReadWriteLock rwl = locksCache.get(cacheName);
		    		boolean lockSuccess = false;
		    		try {
		    			System.out.println("ddddddddddddddddddd");
		    			lockSuccess = rwl.writeLock().tryLock(writeLockTimeout, TimeUnit.SECONDS);
					} catch (InterruptedException e1) {
						printLockDebug(rwl);
						return;
					}
		    		//rwl.writeLock().lock();
		            try
		            {
		            	if(!lockSuccess)
		            	{
							printLockDebug(rwl);
		            		return;
		            	}

				    	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Getting lock for flushing " + cacheName, t.getElapsedTime());
						*/ 
						cacheInstance.flushAll();
						
				    	//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Flushing " + cacheName, t.getElapsedTime());
					}
		    		/*
		            finally
		            {
		            	if(lockSuccess)
		            		rwl.writeLock().unlock();
		            }
		            */
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
		//synchronized(caches) 
		//{
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
		    		synchronized(cacheInstance) //Back
		    		{
						cacheInstance.flushEntry(key);
					}
				}
			}
		//}
	}

	public static void clearCacheHard(String cacheName, String key)
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
	    		synchronized(cacheInstance) //Back
	    		{
					//cacheInstance.flushEntry(key);
					cacheInstance.removeEntry(key);
	    		}
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
		/*
		if(cacheName.indexOf("pageCache") > -1)
			Thread.dumpStack();
		*/
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
						logger.info("Clearing full cache:" + cacheName + " - the call wanted partly clear for [" + group + "] but the cache was a Map.");
					}
				}
				else
				{
				    GeneralCacheAdministrator cacheInstance = (GeneralCacheAdministrator)object;
		    		synchronized(cacheInstance) //Back
		    		{
		    		/*	
				    Timer t = new Timer();
		    		ReentrantReadWriteLock rwl = locksCache.get(cacheName);
		    		boolean lockSuccess = false;
		    		try {
		    			System.out.println("YEEEEEEEEEEEEEEEEEAAAAAAH");
						lockSuccess = rwl.writeLock().tryLock(writeLockTimeout, TimeUnit.SECONDS);
						printLockDebug(rwl);
					} catch (InterruptedException e1) {
						return;
					}
		    		//rwl.writeLock().lock();
		            try
		            {
		            	if(!lockSuccess)
		            	{
		            		return;
		            	}

				    	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Getting lock for flushGroup in " + cacheName, t.getElapsedTime());
					*/
			    		cacheInstance.flushGroup(group);

				    //	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("FlushGroup on " + cacheName, t.getElapsedTime());
					}
		    		/*
		            finally
		            {
		            	if(lockSuccess)
		            		rwl.writeLock().unlock();
		            }
		            */
				}
			}
		}
	}

	/**
	 * This method clears part of a cache.
	 * @param cacheName
	 * @param groups
	 */
	/*
	public static void clearFileCacheForGroup(GeneralCacheAdministrator cacheInstance, String groupName) throws Exception
	{
        Set groupEntries = cacheInstance.getCache().cacheMap.getGroup(groupName);
        
        if(logger.isInfoEnabled())
        {
	        if(groupEntries != null)
	        	logger.info("groupEntries for " + groupName + ":" + groupEntries.size() + " in cacheInstance:" + cacheInstance);
	        else
	        	logger.info("no groupEntries for " + groupName + " in cacheInstance:" + cacheInstance);
        }
        
        if (groupEntries != null) 
        {
            Iterator groupEntriesIterator = groupEntries.iterator();
            while (groupEntriesIterator.hasNext()) 
            {
            	String key = (String) groupEntriesIterator.next();
            	CacheEntry entry = (CacheEntry) cacheInstance.getCache().cacheMap.get(key);
            	if(logger.isInfoEnabled())
            		logger.info("Removing file with key:" + key);
            	removeCachedContentInFile("pageCache", key);
            	numberOfPageCacheFiles.decrementAndGet();
            }
        }
	}
	*/
	
	public static void clearCaches(String entity, String entityId, String[] cachesToSkip) throws Exception
	{	
		clearCaches(entity, entityId, null, cachesToSkip, false);
	}

	public static void clearCaches(String entity, String entityId, Map<String,String> extraInformation, String[] cachesToSkip) throws Exception
	{	
		clearCaches(entity, entityId, extraInformation, cachesToSkip, false);
	}
	
	public static void clearCaches(String entity, String entityId, Map<String,String> extraInformation, String[] cachesToSkip, boolean forceClear) throws Exception
	{	
		Timer t = new Timer();
		//t.setActive(false);
		
		if(CmsPropertyHandler.getOperatingMode().equals("3"))
		{
			long wait = 0;
			//while(true && !getForcedCacheEvictionMode())
			while(!forceClear && !getForcedCacheEvictionMode() && RequestAnalyser.getRequestAnalyser().getNumberOfActiveRequests() > 0)
			{
		        logger.warn("Number of requests: " + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + " was more than 0 - lets wait a bit.");
		        if(wait > 1000)
				{
					logger.warn("The clearCache method waited over " + ((wait * 10) / 1000) + " seconds but there seems to be " + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + " requests blocking all the time. Continuing anyway.");
					//printThreads();
					break;
				}
	
		        if(wait > 100)
					setForcedCacheEvictionMode(true);
	
				Thread.sleep(10);
				wait++;
		    }
		}
		
	    logger.info("clearCaches start in " + CmsPropertyHandler.getContextRootPath());
		if(entity == null)
		{	
			logger.info("Clearing the caches");
			//synchronized(caches)
			//{
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
						}
						logger.info("Cleared cache:" + e.getKey());
						
				    	i.remove();
					}
				}
			//}
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

		    TreeMap<String, Object> orderedCaches = new TreeMap<String,Object>(new CacheComparator());
		    orderedCaches.putAll(caches);
		    
		    /*
		    for (String key : orderedCaches.keySet()) 
			{
				System.out.println("key:" + key);
			}
			*/
		    //t.printElapsedTime("Start cache eviction");
		    RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Start cache eviction", t.getElapsedTime());
	    	//t.printElapsedTime("START cachesLoop:" + entity + ":" + entityId);

			//synchronized(caches)
		    //{
	    		//for (Iterator i = caches.entrySet().iterator(); i.hasNext(); ) 
		    	cachesLoop:for (Iterator i = orderedCaches.entrySet().iterator(); i.hasNext(); ) 
				{
					RequestAnalyser.getRequestAnalyser().registerComponentStatistics("cache iteration top", t.getElapsedTime());

					Map.Entry e = (Map.Entry) i.next();
					logger.info("e:" + e.getKey());

					boolean clear = false;
					boolean selectiveCacheUpdate = false;
					String cacheName = e.getKey().toString();

					if(cachesToSkip != null)
					{
						for(int index=0; index<cachesToSkip.length; index++)
						{
						    if(cacheName.equals(cachesToSkip[index]))
						    {
						        continue cachesLoop;
						    }
						}
					}
					
					//t.printElapsedTime("clearCaches 3");

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
					if(cacheName.equalsIgnoreCase("categoriesCache") && entity.indexOf("Category") > 0)
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
					if(cacheName.startsWith("metaInfoContentAttributeCache") && entity.indexOf("Content") > -1)
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
					if(cacheName.equalsIgnoreCase("componentPropertyCacheRepoGroups") && (entity.indexOf("SiteNode") > -1 || entity.indexOf("ContentVersion") > -1 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{	
						clear = true;
						if(entity.indexOf("SiteNode") > -1 || entity.indexOf("ContentVersion") > -1)
							selectiveCacheUpdate = true;
					}
					if(cacheName.equalsIgnoreCase("componentPropertyVersionIdCacheRepoGroups") && (entity.indexOf("SiteNode") > -1 || entity.indexOf("ContentVersion") > -1 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{	
						clear = true;
						if(entity.indexOf("SiteNode") > -1 || entity.indexOf("ContentVersion") > -1)
							selectiveCacheUpdate = true;
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
					if((cacheName.equalsIgnoreCase("assetUrlCache") || cacheName.equalsIgnoreCase("assetUrlCacheWithGroups") || cacheName.equalsIgnoreCase("assetThumbnailUrlCache")) && (entity.indexOf("DigitalAsset") > 0 || entity.indexOf("ContentVersion") > 0 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{
						clear = true;
						if(cacheName.equalsIgnoreCase("assetUrlCacheWithGroups") && (entity.indexOf("ContentVersion") > -1 || entity.indexOf("DigitalAsset") > -1))
							selectiveCacheUpdate = true;
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
						selectiveCacheUpdate = true;
					}
					if(cacheName.equalsIgnoreCase("matchingContentsCache") && (entity.indexOf("Content") > 0 || entity.indexOf("ContentVersion") > 0 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
					{
						clear = true;
						selectiveCacheUpdate = true;
					}
					if(cacheName.equalsIgnoreCase("workflowCache") && entity.indexOf("WorkflowDefinition") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("rootSiteNodeCache") && entity.indexOf("SiteNode") > 0)
					{
						if(CmsPropertyHandler.getOperatingMode().equals("0"))
							clear = true;
					}
					if((cacheName.equalsIgnoreCase("siteNodeCache") || cacheName.equalsIgnoreCase("siteNodeVOCache")) && entity.indexOf("SiteNode") > 0)
					{
						clear = true;
						selectiveCacheUpdate = true;
					}
					if(cacheName.equalsIgnoreCase("contentCache") && entity.indexOf("Content") > 0)
					{
						clear = true;
						selectiveCacheUpdate = true;
					}
					if(cacheName.equalsIgnoreCase("rootContentCache") && entity.indexOf("Content") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("componentContentsCache") && entity.indexOf("Content") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("childSiteNodesCache") && (entity.indexOf("SiteNode") > 0 || entity.indexOf("ContentVersion") > 0))
					{
						clear = true;
						selectiveCacheUpdate = true;
					}
					if(cacheName.equalsIgnoreCase("childPagesCache") && (entity.indexOf("SiteNode") > 0 || entity.indexOf("Content") > 0))
					{
						clear = true;
						selectiveCacheUpdate = true;
					}
					if(cacheName.equalsIgnoreCase("siteNodeCacheWithLatestVersion") && entity.indexOf("SiteNode") > 0)
					{
						clear = true;
						selectiveCacheUpdate = true;
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
					if(cacheName.equalsIgnoreCase("groupPropertiesCache") && (entity.indexOf("Group") > 0 || entity.indexOf("PropertiesCategory") > 0))
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("propertiesCategoryCache") && (entity.indexOf("Group") > 0 || entity.indexOf("Role") > 0 || entity.indexOf("User") > 0 || entity.indexOf("PropertiesCategory") > 0))
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
					if(cacheName.equalsIgnoreCase("relatedCategoriesCache") && (entity.indexOf("Group") > 0 || entity.indexOf("Role") > 0 || entity.indexOf("User") > 0 || entity.indexOf("PropertiesCategory") > 0))
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("categoryCache") && entity.indexOf("Category") > 0)
					{
						clear = true;
					}
					if(cacheName.equalsIgnoreCase("contentCategoryCache") && entity.indexOf("ContentVersion") > 0)
					{
						clear = true;
						selectiveCacheUpdate = true;
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
						
						String sentContentId = null;
						String sentParentContentId = null;
						String sentSiteNodeId = null;
						String sentParentSiteNodeId = null;
						String sentRepositoryId = null;
						String sentContentTypeDefinitionId = null;
						String sentContentIsProtected = null;
						if (extraInformation != null)
						{
							sentContentId = extraInformation.get("contentId");
							sentParentContentId = extraInformation.get("parentContentId");
							sentSiteNodeId = extraInformation.get("siteNodeId");
							sentParentSiteNodeId = extraInformation.get("parentSiteNodeId");
							sentRepositoryId = extraInformation.get("repositoryId");
							sentContentTypeDefinitionId = extraInformation.get("contentTypeDefinitionId");
							sentContentIsProtected = extraInformation.get("contentIsProtected");
						}
						
						//System.out.println("sentContentId:" + sentContentId);
						//System.out.println("sentParentContentId:" + sentParentContentId);
						//System.out.println("sentSiteNodeId:" + sentSiteNodeId);
						//System.out.println("sentParentSiteNodeId:" + sentParentSiteNodeId);
						//System.out.println("sentRepositoryId:" + sentRepositoryId);

						if(object instanceof Map)
						{
							Map cacheInstance = (Map)e.getValue();
						    synchronized(cacheInstance)
							{
						    	if(cacheName.equals("componentContentsCache"))
						    	{
						    		try
						    		{
							    		if(entity.indexOf("ContentVersion") > 0)
							    		{
									    	String contentId = sentContentId;
									    	String contentTypeDefinitionId = sentContentTypeDefinitionId;
									    	if(contentId == null || contentId.equals(""))
									    	{
										    	try
										    	{
											    	contentId = ""+ContentVersionController.getContentVersionController().getContentIdForContentVersion(new Integer(entityId));
										    	}
										    	catch (Exception e2) 
										    	{
										    		logger.info("Error loading content with id " + entityId + ":" + e2.getMessage());
												}
									    	}
									    	
									    	
									    	if(contentId != null)
									    	{										    			
									    		//ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId); 
									    		if(contentTypeDefinitionId != null && !contentTypeDefinitionId.equals(""))
									    		{
										    		ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(new Integer(contentTypeDefinitionId));
										    		if(ctdVO.getName().equals("HTMLTemplate") || ctdVO.getName().equals("PagePartTemplate"))
										    		{
														ComponentController.getController().reIndexComponentContentsDelayed(new Integer(contentId));
										    		}
									    		}
									    		else
									    			logger.info("No content type for " + contentId);
									    	}
							    		}
							    		else
							    			logger.info("skipping clearing components as it seems stupid");
						    		}
						    		catch (Exception e2) 
						    		{
						    			logger.warn("Error clearing componentContentsCache:" + e2.getMessage(), e2);
									}
						    	}
						    	else if(!(cacheName.equals("userAccessCache") && cacheInstance.size() < 100))
						    	{
						    		logger.info("clearing ordinary map:" + e.getKey() + " (" + cacheInstance.size() + ")");
						    		cacheInstance.clear();
						    	}
						    	else
						    		logger.info("skipping clearing this as it seems stupid");
						    }
						}
						else
						{
						    GeneralCacheAdministrator cacheInstance = (GeneralCacheAdministrator)e.getValue();
						    synchronized(cacheInstance) //Back
						    {
						    	//t.printElapsedTime("START:" + entity + ":" + entityId);
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
						    	//System.out.println("entity:" + entity);
								if(entity.indexOf("pageCache") == 0)
							    {
									if(entity.indexOf("pageCache:") == 0)
									{
										String groupQualifyer = entity.substring("pageCache:".length());
										logger.info("CacheController: This is a application pageCache-clear request... specific:" + groupQualifyer);
										logger.info("clearing " + e.getKey() + " : " + groupQualifyer);

										PageCacheHelper.getInstance().notify("" + groupQualifyer);
								    	/*
										if(cacheName.equals("pageCacheExtra"))
								    	{
								    		clearFileCacheForGroup(cacheInstance, "" + groupQualifyer);
								    	}
								    	else if(cacheName.equals("pageCache"))
								    	{
									    	cacheInstance.flushGroup("" + groupQualifyer);							    		
								    	}
								    	*/
									}
									else
								    {
										PageCacheHelper.getInstance().notify("selectiveCacheUpdateNonApplicable");
										/*
								    	logger.error("clearing " + e.getKey() + " selectiveCacheUpdateNonApplicable");
								    	if(cacheName.equals("pageCacheExtra"))
								    	{
								    		clearFileCacheForGroup(cacheInstance, "selectiveCacheUpdateNonApplicable");
								    	}
								    	else if(cacheName.equals("pageCache"))
								    	{
									    	cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");							    		
								    	}
								    	*/
								    }
							    }

								if(selectiveCacheUpdate && entity.indexOf("Repository") > 0 && useSelectivePageCacheUpdate)
							    {
							    	if(cacheName.equals("pageCacheExtra"))
							    	{
								    	PageCacheHelper.getInstance().notify("repository_" + entityId);
								    	PageCacheHelper.getInstance().notify("selectiveCacheUpdateNonApplicable");
							    	}
							    	else
							    	{
								    	cacheInstance.flushGroup("repository_" + entityId);
								    	cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");							    		
							    	}
							    	
							    	/*
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
							    	*/
							    }
							    else if(selectiveCacheUpdate && entity.indexOf("SiteNodeVersion") > 0)
							    {
							    	//Thread.dumpStack();
							    	//How to solve this good
							    	if(CmsPropertyHandler.getOperatingMode().equalsIgnoreCase("0"))
							    	{
								    	if(cacheName.equals("pageCacheExtra"))
								    	{
									    	PageCacheHelper.getInstance().notify("siteNodeVersion_" + entityId);
									    	PageCacheHelper.getInstance().notify("selectiveCacheUpdateNonApplicable");

								    		//clearFileCacheForGroup(cacheInstance, "siteNodeVersion_" + entityId);
									    	//clearFileCacheForGroup(cacheInstance, "selectiveCacheUpdateNonApplicable");
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
									    	
									    	String siteNodeId = sentSiteNodeId;
									    	String repositoryId = sentRepositoryId;
									    	String parentSiteNodeId = sentParentSiteNodeId;
									    	//System.out.println("siteNodeId:" + siteNodeId);
									    	//System.out.println("repositoryId:" + repositoryId);
									    	//System.out.println("parentSiteNodeId:" + parentSiteNodeId);
									    	if(siteNodeId == null || siteNodeId.equals(""))
									    	{
										    	try
										    	{
										    		SiteNodeVersionVO snvVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(new Integer(entityId));
										    		siteNodeId = ""+snvVO.getSiteNodeId();
										    		if(repositoryId == null)
										    		{
										    			SiteNodeVO snVO = SiteNodeController.getController().getSiteNodeVOWithId(snvVO.getSiteNodeId());
											    		repositoryId = ""+snVO.getRepositoryId();
											    		parentSiteNodeId = ""+snVO.getParentSiteNodeId();
										    		}
										    	}
										    	catch (Exception e2) 
										    	{
										    		logger.info("Error getting siteNodeVersion " + entityId);
										    	}
									    	}
									    										    	
									    	if(siteNodeId != null)
								    		{
										    	logger.info("Before flushGroup2...");
								    			if(cacheName.equals("pageCacheExtra"))
								    			{
											    	PageCacheHelper.getInstance().notify("siteNode_" + siteNodeId);
										    		//clearFileCacheForGroup(cacheInstance, "siteNode_" + siteNodeId);
								    			}
								    			else
								    			{
								    				cacheInstance.flushGroup("siteNode_" + siteNodeId);
								    				cacheInstance.flushGroup("" + siteNodeId);
									    		}
								    			
								    			if(siteNodeId != null && (cacheName.equals("childSiteNodesCache") || cacheName.equals("childPagesCache") || cacheName.equals("siteNodeCache") || cacheName.equals("componentPropertyCacheRepoGroups") || cacheName.equals("componentPropertyVersionIdCacheRepoGroups")))
								    			{
											    	if(cacheName.equals("componentPropertyCacheRepoGroups") || cacheName.equals("componentPropertyVersionIdCacheRepoGroups"))
										    		{
										    			cacheInstance.flushGroup("" + repositoryId);
										    			logger.info("Clearing componentPropertyCacheRepoGroups for repo:" + repositoryId);
										    		}
										    		if(parentSiteNodeId != null)
											    	{
											    		cacheInstance.flushGroup("siteNode_" + parentSiteNodeId);
											    		cacheInstance.flushGroup("" + parentSiteNodeId);
											    		cacheInstance.flushEntry("" + parentSiteNodeId);
											    		logger.info("Clearing for:" + parentSiteNodeId);
											    	}
											    }
								    			
								    			logger.info("After flushGroup2...");
								    		}
								    	}
								    	catch(Exception se)
								    	{
								    		logger.warn("Missing siteNode version: " + se.getMessage(), se);
								    	}
							    	}
							    	else
							    	{
							    		try
							    		{
								    		if((cacheName.equals("childSiteNodesCache") || cacheName.equals("childPagesCache") || cacheName.equals("siteNodeCache") || cacheName.equals("componentPropertyCacheRepoGroups") || cacheName.equals("componentPropertyVersionIdCacheRepoGroups")))
							    			{
									    		SiteNodeVersionVO snvVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(new Integer(entityId));
								    			SiteNodeVO snVO = SiteNodeController.getController().getSiteNodeVOWithId(snvVO.getSiteNodeId());
								    			
								    			Integer repositoryId = snVO.getRepositoryId();
										    	Integer parentSiteNodeId = snVO.getParentSiteNodeId();
	
								    			if(cacheName.equals("componentPropertyCacheRepoGroups") || cacheName.equals("componentPropertyVersionIdCacheRepoGroups"))
									    		{
									    			cacheInstance.flushGroup("" + repositoryId);
									    			logger.info("Clearing componentPropertyCacheRepoGroups for repo:" + repositoryId);
									    		}
									    		if(parentSiteNodeId != null)
										    	{
										    		cacheInstance.flushGroup("siteNode_" + parentSiteNodeId);
										    		cacheInstance.flushGroup("" + parentSiteNodeId);
										    		cacheInstance.flushEntry("" + parentSiteNodeId);
										    		logger.info("Clearing for:" + parentSiteNodeId);
										    	}
										    }
							    		}
							    		catch (Exception e2) 
							    		{
							    			logger.error("Problem clearing cache for site node version:" + e2.getMessage(), e2);
										}
							    	}
							    }
							    else if(selectiveCacheUpdate && (entity.indexOf("SiteNode") > 0 && entity.indexOf("SiteNodeTypeDefinition") == -1) && useSelectivePageCacheUpdate)
							    {
							    	//System.out.println("Entity: " + entity);
							    	logger.info("Flushing " + "" + entityId);
							    	logger.info("Flushing " + "siteNode_" + entityId);
							    	logger.info("Flushing " + "selectiveCacheUpdateNonApplicable");

							    	if(cacheName.equals("pageCacheExtra"))
							    	{
								    	PageCacheHelper.getInstance().notify("siteNode_" + entityId);
								    	PageCacheHelper.getInstance().notify("selectiveCacheUpdateNonApplicable");

							    		//clearFileCacheForGroup(cacheInstance, "siteNode_" + entityId);
								    	//clearFileCacheForGroup(cacheInstance, "selectiveCacheUpdateNonApplicable");
							    	}
							    	else
							    	{
							    		cacheInstance.flushEntry("" + entityId);
							    		cacheInstance.flushGroup("" + entityId);
								    	cacheInstance.flushGroup("siteNode_" + entityId);
								    	cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");
							    	
						    			if(cacheName.equals("childSiteNodesCache") || cacheName.equals("childPagesCache") || cacheName.equals("siteNodeCache") || cacheName.equals("componentPropertyCacheRepoGroups") || cacheName.equals("componentPropertyVersionIdCacheRepoGroups"))
						    			{
						    				logger.info("Flushing parent also");
						    				
						    				String repositoryId = sentRepositoryId;
						    				String parentSiteNodeId = sentParentSiteNodeId;
						    				try
						    				{
							    				if(repositoryId == null)
							    				{
							    					SiteNodeVO snVO = SiteNodeController.getController().getSiteNodeVOWithId(new Integer(entityId));
										    		if(snVO != null)
										    		{
											    		repositoryId = "" + snVO.getRepositoryId();
											    		parentSiteNodeId = "" + snVO.getParentSiteNodeId();
										    		}
							    				}
										    	if(cacheName.equals("componentPropertyCacheRepoGroups") || cacheName.equals("componentPropertyVersionIdCacheRepoGroups"))
									    		{
									    			cacheInstance.flushGroup("" + repositoryId);
									    			logger.info("Clearing componentPropertyCacheRepoGroups for repo:" + repositoryId);
									    		}

										    	if(parentSiteNodeId != null && !parentSiteNodeId.equals(""))
										    	{
										    		logger.info("Flushing " + "" + entityId);
										    		logger.info("Flushing " + "siteNode_" + entityId);

										    		cacheInstance.flushGroup("siteNode_" + parentSiteNodeId);
										    		cacheInstance.flushGroup("" + parentSiteNodeId);
										    		cacheInstance.flushEntry("" + parentSiteNodeId);
										    		logger.info("Clearing for:" + parentSiteNodeId);
										    	}
									    	}
									    	catch(SystemException se)
									    	{
									    		logger.warn("Missing siteNode: " + se.getMessage(), se);
									    	}
									    }
							    	}

							    	logger.info("clearing " + e.getKey() + " with group " + "siteNode_" + entityId);
								}
							    else if(selectiveCacheUpdate && entity.indexOf("ContentVersion") > 0 && useSelectivePageCacheUpdate)
							    {
							    	//t.printElapsedTime("CV start....");
									logger.info("ContentVersion entity was sent: " + entity + ":" + entityId + " and cacheName:" + cacheName);

							    	logger.info("Getting eventListeners...");
							        //Object cacheEntryEventListener = eventListeners.get(e.getKey() + "_cacheEntryEventListener");
						    		//Object cacheMapAccessEventListener = eventListeners.get(e.getKey() + "_cacheMapAccessEventListener");

							    	//System.out.println("entity:" + entity);
							    	
							    	//System.out.println("Before flushGroup:" +cacheName);
							    	logger.info("Before flushGroup...");
							    	if(cacheName.equals("pageCacheExtra"))
							    	{
							    		//clearFileCacheForGroup(cacheInstance, "contentVersion_" + entityId);
							    		//clearFileCacheForGroup(cacheInstance, "selectiveCacheUpdateNonApplicable");
							    	}
							    	else if(cacheName.equals("pageCache"))
							    	{
								    	logger.info("Skipping clearing pageCache for version");
							    		//cacheInstance.flushGroup("contentVersion_" + entityId);
							    		//cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");
							    	}
							    	else if(cacheName.equals("componentPropertyCacheRepoGroups") || cacheName.equals("componentPropertyVersionIdCacheRepoGroups"))
						    		{
							    		Timer t2 = new Timer();
							    		try
								    	{
								    		String repositoryId = sentRepositoryId;
									    	if(repositoryId == null)
									    	{
									    		String contentId = sentContentId;
										    	if(contentId == null)
										    		contentId = "" + ContentVersionController.getContentVersionController().getContentIdForContentVersion(new Integer(entityId));
									    		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(contentId));
									    		repositoryId = "" + contentVO.getRepositoryId();
									    	}
									    	
									    	cacheInstance.flushGroup("" + repositoryId);
									    	//t2.printElapsedTime("3");
									    	if(cacheName.equals("componentPropertyVersionIdCacheRepoGroups"))
									    		cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");
									    	//t2.printElapsedTime("4");
									    	logger.info("Clearing componentPropertyCacheRepoGroups for repo:" +repositoryId);
								    	}
								    	catch (Exception e2) 
								    	{
								    		logger.info("Error loading content with id " + entityId + ":" + e2.getMessage());
										}
								    	//t.printElapsedTime("componentPropertyCacheRepoGroups");
						    		}
							    	else if(cacheName.equals("assetUrlCacheWithGroups"))
						    		{
							    		try
							    		{
								    		String contentId = sentContentId;
								    		if(contentId == null || contentId.equals(""))
								    			contentId = ""+ContentVersionController.getContentVersionController().getContentIdForContentVersion(new Integer(entityId));
											cacheInstance.flushGroup("content_" + contentId);
									    	logger.info("Clearing assetUrlCacheWithGroups for content:" + "content_" + contentId);
							    		}
							    		catch (Exception e2) 
							    		{
							    			logger.warn("Flushing assetUrlCacheWithGroups as it was a missing entity - was probably a delete");
							    			cacheInstance.flushAll();
										}
							    		//t.printElapsedTime("assetUrlCacheWithGroups");
						    		}
							    	else if(cacheName.equals("childPagesCache") || cacheName.equals("childSiteNodesCache"))
						    		{
							    		//System.out.println("childPagesCache:" + entity + "=" + entityId);
							    		//t.printElapsedTime("childPagesCache start");
							    		try
							    		{
								    		String contentId = sentContentId;
								    		//System.out.println("sentContentId:" + sentContentId);
								    		if(contentId == null || contentId.equals(""))
								    			contentId = ""+ContentVersionController.getContentVersionController().getContentIdForContentVersion(new Integer(entityId));
								    		//System.out.println("contentId:" + contentId);
								    		//t.printElapsedTime("childPagesCache A");
								    		
								    		ContentTypeDefinitionVO metaInfoContentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("Meta info");
								    		//t.printElapsedTime("childPagesCache B");
								    		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(contentId));
								    		//t.printElapsedTime("childPagesCache C");
								    		
								    		if(metaInfoContentTypeDefinitionVO.getId().equals(contentVO.getContentTypeDefinitionId()))
								    		{
									    		try
									    		{
										    		SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithMetaInfoContentId(new Integer(contentId));
										    		//t.printElapsedTime("childPagesCache getSiteNodeVOWithMetaInfoContentId");
										    		if(siteNodeVO != null)
										    		{
										    			cacheInstance.flushGroup("siteNode_" + siteNodeVO.getId());
										    			cacheInstance.flushGroup("siteNode_" + siteNodeVO.getParentSiteNodeId());
										    		}
										    	}
									    		catch (Exception e2) 
									    		{
									    			logger.error("Did not find a sitenode with this meta info:" + contentId);
												}
									    		cacheInstance.flushGroup("content_" + contentId);
									    		cacheInstance.flushGroup("contentVersion_" + entityId);
									    		cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");
									    		//t.printElapsedTime("childPagesCache flush done...");
										    	logger.info("Clearing childPagesCache for content:" + "content_" + contentId);
								    		}
							    		}
							    		catch (Exception e2) 
							    		{
							    			logger.warn("Flushing childPagesCache as it was a missing entity - was probably a delete");
							    			cacheInstance.flushAll();
										}
							    		//t.printElapsedTime("childPagesCache");
						    		}
							    	else if(cacheName.equals("matchingContentsCache"))
						    		{
							    		try
							    		{
								    		String contentId = sentContentId;
								    		if(contentId == null || contentId.equals(""))
								    			contentId = ""+ContentVersionController.getContentVersionController().getContentIdForContentVersion(new Integer(entityId));

								    		try
								    		{
									    		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(contentId));
									    		String contentTypeDefinitionId = "" + contentVO.getContentTypeDefinitionId();
									    		cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentTypeDefinitionId);
									    		cacheInstance.putInCache("recacheMark_" + contentTypeDefinitionId, "" + System.currentTimeMillis());
									    		cacheInstance.putInCache("recacheMark", "" + System.currentTimeMillis());
									    	}
								    		catch (Exception e2) 
								    		{
								    			logger.warn("Flushing all as it was a missing entity - was probably a delete:" + e2.getMessage());
								    			cacheInstance.flushAll();
											}
								    		
								    		cacheInstance.flushGroup("content_" + contentId);
									    	logger.info("Clearing assetUrlCacheWithGroups for content:" + "content_" + contentId);
							    		}
							    		catch (Exception e2) 
							    		{
							    			logger.warn("Flushing all as it was a missing entity - was probably a delete:" + e2.getMessage());
							    			cacheInstance.flushAll();
										}
							    		//t.printElapsedTime("matchingContentsCache");
						    		}
							    	else
							    	{
							    		//t.printElapsedTime("Before");
							    		cacheInstance.flushGroup("contentVersion_" + entityId);
							    		//if(!cacheName.equals("contentCache") && !cacheName.equals("contentVersionCache") && !cacheName.equals("contentAttributeCache") && !cacheName.equals("contentVersionIdCache") && !cacheName.equals("contentCategoryCache") && !cacheName.equals("metaInfoContentAttributeCache"))
							    			cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");
								    	logger.info("clearing " + e.getKey() + " with selectiveCacheUpdateNonApplicable");
								    	//t.printElapsedTime("clearing " + e.getKey() + " with selectiveCacheUpdateNonApplicable");
							    	}
							    	logger.info("clearing " + e.getKey() + " with group " + "contentVersion_" + entityId);
									
							    	//String[] changedAttributes = new String[]{"Title","NavigationTitle"}; 
							    	
							    	try
							    	{
							    		//t.printElapsedTime("Cache 3.4");
								    	logger.info("Before contentVersionVO...");
								    	//System.out.println("cacheName:" + cacheName);
								    	//System.out.println("entity:" + entity);
								    	//System.out.println("entityId:" + entityId);
								    	
							    		String contentIdString = sentContentId;
							    		String contentTypeDefinitionId = sentContentTypeDefinitionId;
							    		String contentIsProtected = sentContentIsProtected;
								    	if(contentIdString == null || contentIdString.equals(""))
								    	{
									    	try
									    	{
									    		contentIdString = "" + ContentVersionController.getContentVersionController().getContentIdForContentVersion(new Integer(entityId));
									    	}
									    	catch (Exception e2) 
									    	{
									    		logger.info("Error loading content with id " + entityId + ":" + e2.getMessage());
											}
								    	}

								    	Integer contentId = null;
								    	if(contentIdString != null)
								    		contentId = new Integer(contentIdString);
								    	
								    	if(contentTypeDefinitionId == null && contentIsProtected == null && contentId != null)
								    	{
								    		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(contentId));
								    		contentTypeDefinitionId = ""+contentVO.getContentTypeDefinitionId();
								    		contentIsProtected = ""+contentVO.getIsProtected().intValue();
								    	}
								    	
								    	//t.printElapsedTime("Cache 3.5");
								    	//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Cache 3.5", t.getElapsedTime());

										if(contentId != null)
							    		{
								    		List<String> changes = Collections.EMPTY_LIST;
								    		//System.out.println("extraInformation:" + extraInformation);
								    		String changedAttributes = extraInformation == null ? null : extraInformation.get("changedAttributeNames");
								    		if(changedAttributes != null && changedAttributes.length() > 0)
								    			changes = new ArrayList<String>(Arrays.asList(StringUtils.split(changedAttributes, ",")));


									    	//ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(contentId));

								    		logger.info("Before flushGroup2...");
								    		if(cacheName.equals("pageCacheExtra"))
									    	{
										    	if(contentIsProtected.equals(""+ContentVO.YES.intValue()))
										    	{
										    		List<InterceptionPointVO> interceptionPointVOList = InterceptionPointController.getController().getInterceptionPointVOList("Content");
										    		for(InterceptionPointVO interceptionPointVO : interceptionPointVOList)
										    		{
										    			if(interceptionPointVO.getName().endsWith(".Read"))
										    			{
													    	String acKey = "" + interceptionPointVO.getId() + "_" + entityId;
											    			CacheController.clearUserAccessCache(acKey);						    			
										    			}
										    		}
										    	}
										    	
										    	PageCacheHelper.getInstance().notify("selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentTypeDefinitionId);
							    				PageCacheHelper.getInstance().notify("content_" + contentId);
							    				//clearFileCacheForGroup(cacheInstance, "selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentVO.getContentTypeDefinitionId());
										    	
									    		if((changes == null || changes.size() == 0) && CmsPropertyHandler.getOperatingMode().equals("3"))
									    		{
									    			ContentVersionVO oldContentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(new Integer(entityId));
										    		ContentVersionVO newContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, oldContentVersionVO.getLanguageId(), new Integer(CmsPropertyHandler.getOperatingMode()));
										    		if(newContentVersionVO != null && oldContentVersionVO != null && newContentVersionVO.getId().equals(oldContentVersionVO.getId()))
										    		{
										    			oldContentVersionVO = null;
										    			//System.out.println("SHIT - same version allready - must find other");
											    		List<SmallestContentVersionVO> contentVersionVOList = ContentVersionController.getContentVersionController().getSmallestContentVersionVOList(new Integer(contentId));
												    	for(SmallestContentVersionVO cvVO : contentVersionVOList)
												    	{
												    		if(!cvVO.getId().equals(newContentVersionVO.getId()) && cvVO.getStateId().equals(new Integer(CmsPropertyHandler.getOperatingMode())) && cvVO.getLanguageId().equals(newContentVersionVO.getLanguageId()) && cvVO.getIsActive() && (oldContentVersionVO == null || oldContentVersionVO.getId() < cvVO.getId()))
												    		{
												    			oldContentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(cvVO.getId());
												    		}
												    	}
										    		}
										    		
										    		//System.out.println("Now we should have current and previous version:" + newContentVersionVO + " / " + oldContentVersionVO);
										    		if(newContentVersionVO != null && oldContentVersionVO != null)
										    			changes = ContentVersionController.getContentVersionController().getChangedAttributeNames(newContentVersionVO, oldContentVersionVO);
									    		}

									    		//System.out.println("changes:" + changes);
									    		for(String changedAttributeName : changes)
									    		{
									    			if(changedAttributeName.indexOf("ComponentStructure") > -1)
									    			{
									    				//Map allreadyFlushedEntries....
									    				Set<String> groupEntries = (Set<String>)cacheInstance.getCache().cacheMap.getGroup("content_" + contentId + "_ComponentStructureDependency");
									    				//System.out.println("groupEntries:" + groupEntries);
									    				if(groupEntries != null)
									    				{
									    					System.out.println("groupEntries:" + groupEntries.size());
										    				outer:for(String key : groupEntries)
										    				{
										    					//System.out.println("key 1:" + key);
										    					try
										    					{
											    					//String[] usedEntities = (String[])cacheInstance.getFromCache(key + "_entities");
											    					byte[] usedEntitiesByteArray = (byte[])cacheInstance.getFromCache(key + "_entitiesAsByte");
											    					String usedEntitiesString = compressionHelper.decompress(usedEntitiesByteArray);
											    					//t.printElapsedTime("Decompress to " + usedEntitiesString.length() + " took");
											    					String[] usedEntities = StringUtils.split(usedEntitiesString, "|");
											    					//t.printElapsedTime("Split to usedEntities " + usedEntities.length + " took");
											    					
													    			ContentVersionVO newContentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(new Integer(entityId));
													    			String newComponentStructure = ContentVersionController.getContentVersionController().getAttributeValue(newContentVersionVO, "ComponentStructure", false);
		
													    			for(String usedEntity : usedEntities)
											    					{
											    						//System.out.println("usedEntity:" + usedEntity);
											    						if(usedEntity.startsWith("content_" + contentId + "_ComponentStructure("))
											    						{
											    							//System.out.println("Match - now lets parse: " + usedEntity);
											    							String arguments = usedEntity.substring(usedEntity.indexOf("(") + 1, usedEntity.indexOf(")"));
											    							Integer oldComponentPropertyHash = new Integer(usedEntity.substring(usedEntity.indexOf("=") + 1));
											    							String[] args = arguments.split(",");
											    							Integer componentId = new Integer(args[0]);
											    							String propertyName = args[1];
											    							Integer siteNodeId = new Integer(args[2]);
											    							Integer languageId = new Integer(args[3]);
											    							//System.out.println("componentId:" + componentId);
											    							//System.out.println("propertyName:" + propertyName);
											    							//System.out.println("siteNodeId:" + siteNodeId);
											    							//System.out.println("languageId:" + languageId);
																			
															    			int newComponentPropertyHash = getPropertyAsStringHashCode(newComponentStructure, componentId, propertyName, siteNodeId, languageId);
															    			//System.out.println("oldComponentPropertyHash:" + oldComponentPropertyHash);
															    			//System.out.println("newComponentPropertyHash:" + newComponentPropertyHash);
															    			if(oldComponentPropertyHash.intValue() != newComponentPropertyHash)
															    			{
															    				//System.out.println("Yes - clearing - must have changed something important:" + usedEntity);
																		    	PageCacheHelper.getInstance().notify(usedEntity);
															    				//clearFileCacheForGroup(cacheInstance, usedEntity);
															    			}
															    			else
															    			{
															    				//System.out.println("Flushing content_" + currentPageMetaInfoContentId + "_ComponentStructure just to catch page itself");
																    			//cacheInstance.flushGroup("content_" + currentPageMetaInfoContentId + "_ComponentStructure");
															    				//System.out.println("Flushing content_" + contentId + "_ComponentStructure just to catch page itself");
																    			//cacheInstance.flushGroup("content_" + contentId + "_ComponentStructure");
															    			}
															    			
											    						}
											    						else if(usedEntity.startsWith("content_" + contentId + "_ComponentStructure:"))
											    						{
											    							//System.out.println("Match - now lets parse component order etc: " + usedEntity);
											    							String xPath = usedEntity.substring(usedEntity.indexOf(":") + 1, usedEntity.lastIndexOf("="));
											    							Integer oldComponentPropertyHash = new Integer(usedEntity.substring(usedEntity.lastIndexOf("=") + 1));
											    							//System.out.println("xPath:" + xPath);
											    							
											    							int newComponentPropertyHash = getComponentsAsStringHashCode(newComponentStructure, xPath);
											    							//System.out.println("oldComponentPropertyHash:" + oldComponentPropertyHash);
											    							//System.out.println("newComponentPropertyHash:" + newComponentPropertyHash);
															    			if(oldComponentPropertyHash.intValue() != newComponentPropertyHash)
															    			{
															    				//System.out.println("Yes - clearing - must have changed order or added/subtracted components:" + usedEntity);
															    				PageCacheHelper.getInstance().notify(usedEntity);
															    				//clearFileCacheForGroup(cacheInstance, usedEntity);
															    			}
											    						}
											    					}
										    					}
										    					catch (Exception ex) 
										    					{
																	//logger.error("Got error trying to update cache:" + ex.getMessage());
													    			logger.warn("Got error trying to update cache:" + ex.getMessage(), ex);

													    			//clearFileCacheForGroup(cacheInstance, "content_" + contentId + "_" + changedAttributeName);
												    				//cacheInstance.flushGroup("content_" + contentId + "_" + changedAttributeName);
													    			//logger.warn("Cleared pageCache for " + "content_" + contentId + "_" + changedAttributeName);

													    			break outer;
										    					}
										    				}
									    				}
									    			}
									    			else
									    			{
									    				PageCacheHelper.getInstance().notify("content_" + contentId + "_" + changedAttributeName);
									    				//clearFileCacheForGroup(cacheInstance, "content_" + contentId + "_" + changedAttributeName);
										    			//System.out.println("Cleared for " + "content_" + contentId + "_" + changedAttributeName);
									    			}
									    		}	

									    		//t.printElapsedTime("Handled page cache extra");
									    		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Handled page cache extra", t.getElapsedTime());	
									    		//clearFileCacheForGroup(cacheInstance, "content_" + contentId);
									    	}
									    	else if(cacheName.equals("pageCache"))
									    	{
									    		//t.printElapsedTime("Page cache start");
									    		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Page cache start", t.getElapsedTime());
									    		logger.info("Flushing pageCache for content type def");

									    		String contentTypeDefKey = "selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentTypeDefinitionId;
									    		cacheInstance.flushGroup(contentTypeDefKey);
									    		//cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentVO.getContentTypeDefinitionId());
									    		//cacheInstance.flushGroup("content_" + contentVO.getId());
									    		//System.out.println("Flushing:" + getPooledString(1, contentVO.getId()));
									    		cacheInstance.flushGroup(getPooledString(1, new Integer(contentId)));
							    				PageCacheHelper.getInstance().notify("content_" + contentId);

									    		ContentVersionVO oldContentVersionVO = null;
									    		ContentVersionVO newContentVersionVO = null;
									    		
									    		String debug = "";
									    		if((changes == null || changes.size() == 0) && CmsPropertyHandler.getOperatingMode().equals("3"))
									    		{
									    			debug += "entityId:" + entityId + "\n";
									    			debug += "contentId:" + contentId + "\n";
									    			oldContentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(new Integer(entityId));
									    			debug += "oldContentVersionVO:" + oldContentVersionVO.getId() + ":" + oldContentVersionVO.getLanguageId() + "\n";
									    			debug += "oldContentVersionVO:" + CmsPropertyHandler.getOperatingMode() + "\n";
										    		newContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, oldContentVersionVO.getLanguageId(), new Integer(CmsPropertyHandler.getOperatingMode()));
									    			debug += "newContentVersionVO:" + newContentVersionVO + "\n";
										    		if(newContentVersionVO != null && oldContentVersionVO != null && newContentVersionVO.getId().equals(oldContentVersionVO.getId()))
										    		{
										    			debug += "newContentVersionVO:" + newContentVersionVO.getId() + "\n";
											    		oldContentVersionVO = null;
											    		debug += "SHIT - same version allready - must find other";
											    		List<SmallestContentVersionVO> contentVersionVOList = ContentVersionController.getContentVersionController().getSmallestContentVersionVOList(new Integer(contentId));
												    	for(SmallestContentVersionVO cvVO : contentVersionVOList)
												    	{
												    		if(!cvVO.getId().equals(newContentVersionVO.getId()) && cvVO.getStateId().equals(new Integer(CmsPropertyHandler.getOperatingMode())) && cvVO.getLanguageId().equals(newContentVersionVO.getLanguageId()) && cvVO.getIsActive() && (oldContentVersionVO == null || oldContentVersionVO.getId() < cvVO.getId()))
												    		{
												    			oldContentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(cvVO.getId());
												    		}
												    	}
										    			debug += "oldContentVersionVO:" + (oldContentVersionVO == null ? "null" : oldContentVersionVO.getId()) + "\n";
										    		}
										    		
										    		//System.out.println("Now we should have current and previous version:" + newContentVersionVO + " / " + oldContentVersionVO);
										    		if(newContentVersionVO != null && oldContentVersionVO != null)
										    			changes = ContentVersionController.getContentVersionController().getChangedAttributeNames(newContentVersionVO, oldContentVersionVO);
									    		}

									    		//t.printElapsedTime("Changes analyzed");
												//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Changes analyzed", t.getElapsedTime());

									    		if((changes == null || changes.size() == 0)  && CmsPropertyHandler.getOperatingMode().equals("3"))
									    		{
									    			if(oldContentVersionVO == null || newContentVersionVO == null)
									    			{
									    				//Hur kan det bli detta????
												    	logger.warn("Fishy 1: " + oldContentVersionVO + ":" + newContentVersionVO + " in " + CmsPropertyHandler.getContextRootPath());
												    	logger.warn("DEBUG: " + debug);
									    			}
									    			else
									    			{
												    	logger.warn("Fishy 2: No changes found between content versions " + newContentVersionVO.getId() + " and " + oldContentVersionVO.getId() + " in " + CmsPropertyHandler.getContextRootPath());
												    	logger.warn("DEBUG: " + debug);
												    	logger.warn("Fishy: newContentVersionVO: " + newContentVersionVO.getVersionValue());
												    	logger.warn("Fishy: newContentVersionVO: " + oldContentVersionVO.getVersionValue());
									    			}
									    			logger.warn("Just to make sure pages are updated we pretend all attributes changed until we find the bug");
									    			changes = ContentVersionController.getContentVersionController().getAttributeNames(newContentVersionVO);
									    		}
									    		
									    		//System.out.println("changes:" + changes);
									    		for(String changedAttributeName : changes)
									    		{
											    	logger.warn("changedAttributeName: " + changedAttributeName);
									    			if(changedAttributeName.indexOf("ComponentStructure") > -1 && cacheName.equals("pageCache"))
									    			{
									    				//Map allreadyFlushedEntries....
									    				//It's something wrong here..
									    				GeneralCacheAdministrator pageCacheExtraInstance = (GeneralCacheAdministrator)caches.get("pageCacheExtra");
									    				String cacheGroupKey = "content_" + contentId + "_ComponentStructureDependency";
									    				//Set<String> groupEntries = (Set<String>)cacheInstance.getCache().cacheMap.getGroup("content_" + contentId + "_ComponentStructureDependency");
									    				Set<String> groupEntries = (Set<String>)cacheInstance.getCache().cacheMap.getGroup(getPooledString(cacheGroupKey.hashCode()));
									    				//System.out.println("groupEntries:" + groupEntries);
									    				if(groupEntries != null)
									    				{
									    					outer:for(String key : groupEntries)
										    				{
																logger.info("key 2:" + key);
																try
																{
																	//String[] usedEntities = (String[])pageCacheExtraInstance.getFromCache(key + "_entities");
											    					byte[] usedEntitiesByteArray = (byte[])pageCacheExtraInstance.getFromCache(key + "_entitiesAsByte");
											    					String usedEntitiesString = compressionHelper.decompress(usedEntitiesByteArray);
											    					//t.printElapsedTime("Decompress to " + usedEntitiesString.length() + " took");
											    					String[] usedEntities = StringUtils.split(usedEntitiesString, ",");
											    					//t.printElapsedTime("Split to usedEntities " + usedEntities.length + " took");
																	
											    					ContentVersionVO newestContentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(new Integer(entityId));
													    			String newComponentStructure = ContentVersionController.getContentVersionController().getAttributeValue(newestContentVersionVO, "ComponentStructure", false);
		
											    					for(String usedEntity : usedEntities)
											    					{
											    						//System.out.println("usedEntity:" + usedEntity);
											    						if(usedEntity.startsWith("content_" + contentId + "_ComponentStructure("))
											    						{
											    							//System.out.println("Match - now lets parse: " + usedEntity);
											    							String arguments = usedEntity.substring(usedEntity.indexOf("(") + 1, usedEntity.indexOf(")"));
											    							Integer oldComponentPropertyHash = new Integer(usedEntity.substring(usedEntity.indexOf("=") + 1));
											    							String[] args = arguments.split(",");
											    							Integer componentId = new Integer(args[0]);
											    							String propertyName = args[1];
											    							Integer siteNodeId = new Integer(args[2]);
											    							Integer languageId = new Integer(args[3]);
											    							//System.out.println("componentId:" + componentId);
											    							//System.out.println("propertyName:" + propertyName);
											    							//System.out.println("siteNodeId:" + siteNodeId);
											    							//System.out.println("languageId:" + languageId);
																			
															    			int newComponentPropertyHash = getPropertyAsStringHashCode(newComponentStructure, componentId, propertyName, siteNodeId, languageId);
															    			//System.out.println("oldComponentPropertyHash:" + oldComponentPropertyHash);
															    			//System.out.println("newComponentPropertyHash:" + newComponentPropertyHash);
															    			if(oldComponentPropertyHash.intValue() != newComponentPropertyHash)
															    			{
															    				//System.out.println("Yes - clearing - must have changed something important:" + usedEntity);
																    			//cacheInstance.flushGroup(usedEntity);
																    			cacheInstance.flushGroup(getPooledString(usedEntity.hashCode()));
															    				//clearFileCacheForGroup(cacheInstance, usedEntity);
															    			}
															    			else
															    			{
															    				//System.out.println("Flushing content_" + currentPageMetaInfoContentId + "_ComponentStructure just to catch page itself");
																    			//cacheInstance.flushGroup("content_" + currentPageMetaInfoContentId + "_ComponentStructure");
															    				//System.out.println("Flushing content_" + contentId + "_ComponentStructure just to catch page itself");
																    			String componentStructureKey = "content_" + contentId + "_ComponentStructure";
															    				//cacheInstance.flushGroup(componentStructureKey);
															    				cacheInstance.flushGroup(getPooledString(componentStructureKey.hashCode()));
																    			cacheInstance.flushGroup(getPooledString(usedEntity.hashCode()));
															    			}
															    			
											    						}
											    						else if(usedEntity.startsWith("content_" + contentId + "_ComponentStructure:"))
											    						{
											    							//System.out.println("Match - now lets parse component order etc: " + usedEntity);
											    							String xPath = usedEntity.substring(usedEntity.indexOf(":") + 1, usedEntity.lastIndexOf("="));
											    							Integer oldComponentPropertyHash = new Integer(usedEntity.substring(usedEntity.lastIndexOf("=") + 1));
											    							//System.out.println("xPath:" + xPath);
											    							
											    							int newComponentPropertyHash = getComponentsAsStringHashCode(newComponentStructure, xPath);
											    							//System.out.println("oldComponentPropertyHash:" + oldComponentPropertyHash);
											    							//System.out.println("newComponentPropertyHash:" + newComponentPropertyHash);
															    			if(oldComponentPropertyHash.intValue() != newComponentPropertyHash)
															    			{
															    				//System.out.println("Yes - clearing - must have changed order or added/subtracted components:" + usedEntity);
																    			cacheInstance.flushGroup(getPooledString(usedEntity.hashCode()));
																    			//cacheInstance.flushGroup(usedEntity);
															    			}
											    						}
		
											    					}
																}
																catch(Exception ex)
																{
																	//logger.error("Got error trying to update cache:" + ex.getMessage());
													    			logger.warn("Got error trying to update cache:" + ex.getMessage(), ex);
													    			
													    			try
													    			{
													    				String attributeKey = "content_" + contentId + "_" + changedAttributeName;
													    				//cacheInstance.flushGroup(attributeKey);
													    				cacheInstance.flushGroup(getPooledString(attributeKey.hashCode()));
													    				logger.warn("Cleared pageCache for " + getPooledString(attributeKey.hashCode()));
													    			}
																	catch(Exception ex2)
																	{
																		logger.error("Got error trying to flushGroup 2:" + ex2.getMessage());
																		cacheInstance.flushAll();
																	}
													    			break outer;
																}
																t.printElapsedTime("Handled group entries");
																//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Handled group entries", t.getElapsedTime());
										    				}
									    				}
									    			}
									    			else
									    			{
									    				String attributeKey = "content_" + contentId + "_" + changedAttributeName;
									    				//cacheInstance.flushGroup("content_" + contentId + "_" + changedAttributeName);
									    				cacheInstance.flushGroup(getPooledString(attributeKey.hashCode()));
										    			logger.info("Cleared pageCache for " + "content_" + contentId + "_" + changedAttributeName);
									    			}
									    		}	
									    		//cacheInstance.flushGroup("content_" + contentId);
									    	}
									    	else
									    	{
									    		String contentTypeDefKey = "selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentTypeDefinitionId;
									    		//cacheInstance.flushGroup(contentTypeDefKey);
									    		cacheInstance.flushGroup(contentTypeDefKey);
									    		//System.out.println("Cleared for " + "content_" + contentId + " on cache " + cacheName);
									    		String contentKey = "content_" + contentId;
									    		cacheInstance.flushGroup(contentKey);
									    		//cacheInstance.flushGroup(contentKey);
									    	}

								    		//t.printElapsedTime("Handled page cache");
											//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Handled page cache", t.getElapsedTime());	
									    	logger.info("After flushGroup2...");
							    		}
								    	//}

										if (cacheName.equals("contentVersionCache"))
										{
											new AssetUpdatingThread(entityId).start();
										}
							    	}
							    	catch(SystemException se)
							    	{
							    		se.printStackTrace();
							    		logger.info("Missing content version: " + se.getMessage());
							    	}
							    	catch(Exception ex)
							    	{
							    		ex.printStackTrace();
							    	}
							    }
							    else if(selectiveCacheUpdate && (entity.indexOf("Content") > 0 && entity.indexOf("ContentTypeDefinition") == -1 && entity.indexOf("ContentCategory") == -1) && useSelectivePageCacheUpdate)
							    {
							    	logger.info("Content entity was sent: " + entity + ":" + entityId);
							    	//System.out.println("Content entity was called and needs to be fixed:" + entity);
							    	
							    	//String[] changedAttributes = new String[]{"Title","NavigationTitle"}; 
							    	/*
							    	ContentVO contentVO = null;
							    	if(isObjectCachedInCastor(SmallContentImpl.class, new Integer(entityId)))
							    		contentVO = ContentController.getContentController().getContentVOWithId(new Integer(entityId));
							    	*/
							    	
							    	String repositoryId = sentRepositoryId;
							    	String contentTypeDefinitionId = sentContentTypeDefinitionId;
							    	String contentIsProtected = sentContentIsProtected;
							    	if(repositoryId == null || contentTypeDefinitionId == null || contentIsProtected == null)
							    	{
							    		//System.out.println("repositoryId:" + repositoryId);
							    		//System.out.println("contentTypeDefinitionId:" + contentTypeDefinitionId);
							    		//System.out.println("contentIsProtected:" + contentIsProtected);
								    	try
								    	{
								    		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(entityId));
								    		//t.printElapsedTime("clearCaches cv u1 contentVO", 10);
											repositoryId = ""+contentVO.getRepositoryId();
											contentTypeDefinitionId = ""+contentVO.getContentTypeDefinitionId();
											contentIsProtected = ""+contentVO.getIsProtected();
								    	}
								    	catch (Exception e2) 
								    	{
								    		logger.info("Error loading content with id " + entityId + ":" + e2.getMessage());
										}
							    	}
							    	
						    		if(cacheName.equals("componentPropertyCacheRepoGroups") || cacheName.equals("componentPropertyVersionIdCacheRepoGroups"))
						    		{
								    	cacheInstance.flushGroup("" + repositoryId);
								    	logger.info("Clearing componentPropertyCacheRepoGroups for repo:" + repositoryId);
						    		}

							    	if(cacheName.equals("pageCacheExtra"))
							    	{
							    		//clearFileCacheForGroup(cacheInstance, "content_" + entityId);
							    		PageCacheHelper.getInstance().notify("content_" + entityId);

							    		try
								    	{
							    			//cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentVO.getContentTypeDefinitionId());
								    		if(contentTypeDefinitionId != null)
								    		{
								    			//clearFileCacheForGroup(cacheInstance, "selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentVO.getContentTypeDefinitionId());
								    			PageCacheHelper.getInstance().notify("selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentTypeDefinitionId);
								    		}
								    	}
								    	catch (Exception e2) 
								    	{
								    		logger.warn("Could not find content type to clear pages based on: " + e2.getMessage(), e2);
										}
							    	}
							    	else if(cacheName.equals("pageCache"))
							    	{
								    	logger.info("Flushing page cache for {" + entityId + "} and {content_" + entityId + "}");
								    	
								    	String entityKey = "" + entityId;
								    	String contentEntityKey = "content_" + entityId;
							    		//cacheInstance.flushGroup("" + entityId);
								    	//cacheInstance.flushGroup(contentEntityKey);
							    		cacheInstance.flushGroup(entityKey);
								    	cacheInstance.flushGroup(contentEntityKey);
							    		try
								    	{
							    			if(contentTypeDefinitionId != null)
							    			{
							    				String contentTypeCacheKey = "selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentTypeDefinitionId;
							    				//cacheInstance.flushGroup(contentTypeCacheKey);
							    				cacheInstance.flushGroup(contentTypeCacheKey);
							    			}
							    		}
								    	catch (Exception e2) 
								    	{
								    		logger.warn("Could not find content type to clear pages based on: " + e2.getMessage(), e2);
										}
							    	}
							    	else
							    	{
								    	cacheInstance.flushGroup("" + entityId);
								    	cacheInstance.flushGroup("content_" + entityId);
								    	logger.info("clearing " + e.getKey() + " with selectiveCacheUpdateNonApplicable");
								    	cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");
							    		if(contentTypeDefinitionId != null)
						    			{
							    			cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentTypeDefinitionId);
						    			}
							    	}
							    	
							    	if(contentTypeDefinitionId != null)
							    	{
							    		//System.out.println("****************************************************************");
								    	if(contentIsProtected.equals(""+ContentVO.YES.intValue()))
								    	{
								    		List<InterceptionPointVO> interceptionPointVOList = InterceptionPointController.getController().getInterceptionPointVOList("Content");
								    		for(InterceptionPointVO interceptionPointVO : interceptionPointVOList)
								    		{
								    			if(interceptionPointVO.getName().endsWith(".Read"))
								    			{
											    	String acKey = "" + interceptionPointVO.getId() + "_" + entityId;
											    	//System.out.println("Clearing access rights for:" + acKey);
									    			CacheController.clearUserAccessCache(acKey);						    			
								    			}
								    		}
								    	}
							    	}
							    	//System.out.println("************************END************************************");

							    	logger.info("clearing " + e.getKey() + " with group " + "content_" + entityId);
								}
							    else if(selectiveCacheUpdate && entity.indexOf("DigitalAsset") > -1)
							    {
							    	logger.info("Asset entity was sent: " + entity + ":" + entityId);
							    	Integer contentId = assetContentIdMapping.get(new Integer(entityId));
							    	if(contentId == null)
							    	{
							    		logger.info("Checking fot cv for asset:" + entityId);
							    		List<SmallestContentVersionVO> contentVersions = DigitalAssetController.getContentVersionVOListConnectedToAssetWithId(new Integer(entityId));
							    		if(contentVersions != null)
							    		{
							    			for(SmallestContentVersionVO contentVersionVO : contentVersions)
									    	{
									    		contentId = contentVersionVO.getContentId();
									    		assetContentIdMapping.put(new Integer(entityId), contentId);
									    		break;
									    	}
							    		}
							    	}
							    	else
							    		logger.info("Using read asset");
							    	
							    	//Integer contentId = null;
							    	
							    	ContentVO contentVO = null;
							    	try
							    	{
							    		contentVO = ContentController.getContentController().getContentVOWithId(contentId);
							    	
							    		if(cacheName.equals("componentPropertyCacheRepoGroups") || cacheName.equals("componentPropertyVersionIdCacheRepoGroups"))
							    		{
									    	cacheInstance.flushGroup("" + contentVO.getRepositoryId());
									    	logger.info("Clearing componentPropertyCacheRepoGroups for repo:" + contentVO.getRepositoryId());
							    		}
							    	}
							    	catch (Exception e2) 
							    	{
							    		logger.info("Error loading content with id " + contentId + ":" + e2.getMessage());
									}
							    	//ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(entityId));
							    	
							    	if(cacheName.equals("pageCacheExtra"))
							    	{
							    		//clearFileCacheForGroup(cacheInstance, "content_" + entityId);
							    		PageCacheHelper.getInstance().notify("content_" + contentId);

							    		try
								    	{
							    			//cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentVO.getContentTypeDefinitionId());
								    		if(contentVO != null)
								    		{
								    			//clearFileCacheForGroup(cacheInstance, "selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentVO.getContentTypeDefinitionId());
								    			PageCacheHelper.getInstance().notify("selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentVO.getContentTypeDefinitionId());
								    		}
								    	}
								    	catch (Exception e2) 
								    	{
								    		logger.warn("Could not find content type to clear pages based on: " + e2.getMessage(), e2);
										}
							    	}
							    	else if(cacheName.equals("pageCache"))
							    	{
								    	logger.info("Flushing page cache for {" + contentId + "} and {content_" + contentId + "}");
								    	
								    	String entityKey = "" + contentId;
								    	String contentEntityKey = "content_" + contentId;
							    		//cacheInstance.flushGroup("" + entityId);
								    	//cacheInstance.flushGroup(contentEntityKey);
							    		cacheInstance.flushGroup(entityKey);
								    	cacheInstance.flushGroup(contentEntityKey);
							    		try
								    	{
							    			if(contentVO != null)
							    			{
							    				String contentTypeCacheKey = "selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentVO.getContentTypeDefinitionId();
							    				//cacheInstance.flushGroup(contentTypeCacheKey);
							    				cacheInstance.flushGroup(contentTypeCacheKey);
							    			}
							    		}
								    	catch (Exception e2) 
								    	{
								    		logger.warn("Could not find content type to clear pages based on: " + e2.getMessage(), e2);
										}
							    	}
							    	else
							    	{
								    	cacheInstance.flushGroup("" + contentId);
								    	cacheInstance.flushGroup("content_" + contentId);
								    	logger.info("clearing " + e.getKey() + " with selectiveCacheUpdateNonApplicable");
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
									    	cacheInstance.flushGroup(CacheController.getPooledString(2, publicationDetailVO.getEntityId().toString()));
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
							    else if(entity.equals("org.infoglue.cms.entities.management.impl.simple.AccessRightImpl"))
							    {
							    	//System.out.println("This was an access right update - do we handle it:" + cacheName);
							    	if(!CmsPropertyHandler.getOperatingMode().equalsIgnoreCase("3"))
							    	{
								    	try
								    	{
									    	AccessRightVO acVO = AccessRightController.getController().getAccessRightVOWithId(new Integer(entityId));
									    	InterceptionPointVO icpVO = InterceptionPointController.getController().getInterceptionPointVOWithId(acVO.getInterceptionPointId());
									    	//System.out.println("icpVO:" + icpVO.getName());
									    	if(icpVO.getName().indexOf("Content.") > -1)
									    	{
									    		//System.out.println("Was a content access... let's clear caches for that content.");
									    		String idAsString = acVO.getParameters();
									    		if(idAsString != null && !idAsString.equals(""))
									    			clearCaches("org.infoglue.cms.entities.content.impl.simple.ContentImpl", idAsString, null, cachesToSkip, forceClear);
									    	}
									    	else if(icpVO.getName().indexOf("ContentVersion.") > -1)
									    	{
									    		//System.out.println("Was a contentversion access... let's clear caches for that content.");
									    		String idAsString = acVO.getParameters();
									    		if(idAsString != null && !idAsString.equals(""))
									    			clearCaches("org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl", idAsString, null, cachesToSkip, forceClear);								    		
									    	}
										    else if(icpVO.getName().indexOf("SiteNode.") > -1)
										    {
										    	//System.out.println("Was a sitenode access... let's clear caches for that content.");
									    		String idAsString = acVO.getParameters();
									    		if(idAsString != null && !idAsString.equals(""))
									    			clearCaches("org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl", idAsString, null, cachesToSkip, forceClear);								    											    	
										    }
											else if(icpVO.getName().indexOf("SiteNodeVersion.") > -1)
											{
												//System.out.println("Was a sitenode version access... let's clear caches for that content.");
									    		String idAsString = acVO.getParameters();
									    		if(idAsString != null && !idAsString.equals(""))
									    			clearCaches("org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl", idAsString, null, cachesToSkip, forceClear);								    											    	
											}
											else
											{
												//System.out.println("****************************");
												//System.out.println("* WHAT TO DO WITH IN CACHECONTROLLER: " + icpVO.getName() + " *");
												//System.out.println("****************************");
											}
								    	}
								    	catch(Exception e2)
								    	{
								    		logger.error("Error handling access right update: " + e2.getMessage(), e2);
								    	}
							    	}
							    	else
							    		logger.info("Skipping it as this is live mode..");
							    }
							    else if(selectiveCacheUpdate && (entity.indexOf("ServiceBinding") > 0 || entity.indexOf("Qualifyer") > 0))
							    {
							    	logger.info("Ignoring this kind of notification... never used anymore:" + cacheName);
							    }
							    else if(entity.indexOf("AccessRightImpl") > -1)
							    {
							    	logger.info("Ignoring handling of entity:" + entity);
							    }
							    else if(cacheName.equalsIgnoreCase("matchingContentsCache") && entity.indexOf("MediumContentCategoryImpl") > 0)
							    {
							    	try
						    		{
						    			String contentId = sentContentId;
						    			//if(contentId == null || contentId.equals(""))
							    			
						    			ContentCategoryVO contentCategoryVO = ContentCategoryController.getController().findById(new Integer(entityId));
						    			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentCategoryVO.getContentVersionId());
							    		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId());
							    		String contentTypeDefinitionId = "" + contentVO.getContentTypeDefinitionId();
							    		cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentTypeDefinitionId);
							    		cacheInstance.putInCache("recacheMark_" + contentTypeDefinitionId, "" + System.currentTimeMillis());
							    		cacheInstance.putInCache("recacheMark", "" + System.currentTimeMillis());
							    	}
						    		catch (Exception e2) 
						    		{
							    		cacheInstance.putInCache("recacheAllMark", "" + System.currentTimeMillis());
						    			logger.warn("Flushing all as it was a missing entity - was probably a delete: " + e2.getMessage());
						    			cacheInstance.flushAll();
									}
							    }
							    else if(entity.indexOf("MediumContentCategoryImpl") > 0)
							    {
							    	logger.info("Special handling - no handling");
							    }
							    else if(cacheName.equalsIgnoreCase("componentEditorVersionIdCache") && entity.indexOf("ContentVersionImpl") > 0)
							    {
							    	logger.info("Special handling componentEditorVersionIdCache");
						    		String contentId = sentContentId;
						    		if(contentId == null || contentId.equals(""))
						    			contentId = ""+ContentVersionController.getContentVersionController().getContentIdForContentVersion(new Integer(entityId));

						    		cacheInstance.flushGroup("content_" + contentId);
						    		cacheInstance.flushGroup("contentVersion_" + entityId);
							    }
							    else if((cacheName.equalsIgnoreCase("componentEditorCache") || 
							    		cacheName.equalsIgnoreCase("componentPropertyCache") ||
							    		cacheName.equalsIgnoreCase("componentPropertyVersionIdCache") ||
							    		cacheName.equalsIgnoreCase("pageComponentsCache")) 
							    		&& entity.indexOf("ContentVersionImpl") > 0)
							    {
							    	try
							    	{
							    		logger.info("Special handling componentEditorVersionIdCache");
							    		String contentId = sentContentId;
							    		if(contentId == null || contentId.equals(""))
							    			contentId = ""+ContentVersionController.getContentVersionController().getContentIdForContentVersion(new Integer(entityId));
							    		
							    		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(contentId));
							    		if(contentVO != null && contentVO.getContentTypeDefinitionId() != null)
							    		{
							    			ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVO.getContentTypeDefinitionId());
							    			if(ctdVO.getName().equals("Meta info"))
							    				cacheInstance.flushAll();
							    			else
							    			{
							    				if(cacheName.equalsIgnoreCase("componentEditorCache") && (ctdVO.getName().equals("HTMLTemplate") || ctdVO.getName().equals("PagePartTemplate")))
							    				{
							    					cacheInstance.flushAll(); //CacheController.clearCache("componentEditorCache");
							    				}
							    				else
							    					logger.info("No need to clear page stuff");
							    			}
							    		}
							    	}
							    	catch (Exception e2) 
							    	{
							    		if(e2.getCause() instanceof ObjectNotFoundException || (e2.getCause() != null && e2.getCause().getCause() instanceof ObjectNotFoundException))
							    			logger.info("Error clearing caches - object was probably deleted.");
							    		else
							    			logger.warn("Error handling mixed caches: " + e2.getMessage(), e2);
									}
							    }
							    else
							    {
							    	if(entity.indexOf("EventImpl") == -1 && entity.indexOf(".Publication") == -1)
							    	{
							    		if(logger.isInfoEnabled())
							    			logger.info("WHOOOAAAAAAAAAA.. clearing all... on " + cacheName + ":" + entity);
								    	//System.out.println("selectiveCacheUpdate:" + selectiveCacheUpdate);
								    	//System.out.println("entity:" + entity);
								    	//System.out.println("cacheName:" + cacheName);
								    	//logger.info("Flushing all:" + cacheName);
								    	cacheInstance.flushAll();
								    	if(logger.isInfoEnabled())
								    		logger.info("clearing:" + e.getKey());
										//t.printElapsedTime("WHOOOAAAAAAAAAA.. clearing all... ", 10);
									}
							    }
							} //BACK
						}
						
						long elapsedTime = t.getElapsedTime();
						if(elapsedTime > 20)
							logger.warn("Clear cache end took " + e.getKey() + ": " + elapsedTime);
						logger.info("Cleared cache:" + e.getKey());
	
						if(!selectiveCacheUpdate)
						    i.remove();
						
					}
					else
					{
						logger.info("Did not clear " + e.getKey());
					}
				}
			//}
			
    		if(!useSelectivePageCacheUpdate || entity.indexOf("AccessRight") > -1)
    		{
    			logger.info("Clearing all pageCaches");
    			CacheController.clearFileCaches("pageCache");
    		}
		}
				
		t.printElapsedTime("clearCaches stop", 100);
		/*
		logger.info("clearCaches stop");
		long time = t.getElapsedTime();
		if(time > 3000)
			logger.warn("clearCaches took long time:" + time);
		*/
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

	public static void clearCastorCaches(DefeatCacheParameters dcp) throws Exception
	{
	    if(CmsPropertyHandler.getApplicationName().equals("cms"))
	    	logger.info("Emptying the Castor Caches - why");
	    
		long wait = 0;
		
		while(!getForcedCacheEvictionMode() && RequestAnalyser.getRequestAnalyser().getApproximateNumberOfDatabaseQueries() > 0)
		//while(!getForcedCacheEvictionMode() && RequestAnalyser.getRequestAnalyser().getNumberOfActiveRequests() > 0)
		{
	    	if(wait > 1000)
			{
				logger.warn("The clearCastorCaches method waited over " + ((wait * 10) / 1000) + " seconds but there seems to be " + RequestAnalyser.getRequestAnalyser().getApproximateNumberOfDatabaseQueries() + " queries blocking all the time. Continuing anyway.");
				break;
			}

	        if(wait > 100)
				setForcedCacheEvictionMode(true);

			Thread.sleep(10);
			wait++;
	    }
	    
		Database db = CastorDatabaseService.getDatabase();
		//CastorDatabaseService.setBlock(true);
		
		try
		{		
		    //db.getCacheManager().expireCache();
			Map<Class,List<Object>> entities = dcp.getEntities();
			Iterator<Class> keyIterator = entities.keySet().iterator();
			while(keyIterator.hasNext())
			{
				Class clazz = keyIterator.next();
				List<Object> ids = entities.get(clazz);
				if(ids.size() > 0)
				{
					clearCache(clazz, ids.toArray(), true, db);
					logger.info("Emptied clazz:" + clazz + " with ids:" + ids);
				}
				else
				{
					clearCache(db, clazz);
					logger.info("Emptied clazz:" + clazz);
				}
			}
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
	
	public static void clearCastorCaches() throws Exception
	{
		if(CmsPropertyHandler.getApplicationName().equals("cms"))
	    	System.out.println("Emptying the Castor Caches... why");
	    	    
		long wait = 0;
		
		while(!getForcedCacheEvictionMode() && RequestAnalyser.getRequestAnalyser().getApproximateNumberOfDatabaseQueries() > 0)
		//while(!getForcedCacheEvictionMode() && RequestAnalyser.getRequestAnalyser().getNumberOfActiveRequests() > 0)
		{
	    	if(wait > 1000)
			{
				logger.warn("The clearCastorCaches method waited over " + ((wait * 10) / 1000) + " seconds but there seems to be " + RequestAnalyser.getRequestAnalyser().getApproximateNumberOfDatabaseQueries() + " queries blocking all the time. Continuing anyway.");
				break;
			}

	        if(wait > 100)
				setForcedCacheEvictionMode(true);

			Thread.sleep(10);
			wait++;
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
			clearCache(db, SystemUserRoleImpl.class);
			clearCache(db, SystemUserGroupImpl.class);
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
			clearCache(db, AccessRightRoleImpl.class);
			clearCache(db, AccessRightGroupImpl.class);
			clearCache(db, AccessRightUserImpl.class);
	
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
	

	public static void clearCache(Class type, Object[] ids) throws Exception
	{
		clearCache(type, ids, false);
	}
	
	public static void clearCache(Class type, Object[] ids, boolean forceClear) throws Exception
	{		
		Timer t = new Timer();
		
		if(CmsPropertyHandler.getOperatingMode().equals("3"))
		{
			long wait = 0;
			while(!forceClear && !getForcedCacheEvictionMode() && RequestAnalyser.getRequestAnalyser().getApproximateNumberOfDatabaseQueries() > 0)
			{
		        logger.warn("Number of requests: " + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + " was more than 0 - lets wait a bit.");
		        if(wait > 1000)
				{
					logger.warn("The clearCache method waited over " + ((wait * 10) / 1000) + " seconds but there seems to be " + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + " requests blocking all the time. Continuing anyway.");
					//printThreads();
					break;
				}
	
		        if(wait > 100)
					setForcedCacheEvictionMode(true);
	
				Thread.sleep(10);
				wait++;
		    }
		}
		
	    Database db = CastorDatabaseService.getDatabase();
		try
		{
			if(type.getName().contains(".SiteNodeVersionImpl") || 
			   type.getName().contains(".MediumSiteNodeVersionImpl") || 
			   type.getName().contains(".SiteNodeImpl") || 
			   type.getName().contains(".LanguageImpl") || 
			   type.getName().contains(".RepositoryImpl") || 
			   type.getName().contains(".RepositoryLanguageImpl") || 
			   type.getName().contains(".DigitalAssetImpl") || 
			   type.getName().contains(".MediumDigitalAssetImpl") || 
			   type.getName().contains(".ContentVersionImpl") || 
			   type.getName().contains(".AccessRightRoleImpl") || 
			   type.getName().contains(".AccessRightGroupImpl") || 
			   type.getName().contains(".AccessRightUserImpl") || 
			   type.getName().contains(".RegistryImpl"))
			{
				logger.info("Skipping clear of " + type);
			}
			else
			{
				logger.info("Clearing " + type);
			    CacheManager manager = db.getCacheManager();
			    manager.expireCache(type, ids);
			    //Class[] types = {type};
			    //db.expireCache(types, ids);
			}
			
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

	public static void clearCache(Class c) throws Exception
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
	    while(!forceClear && !getForcedCacheEvictionMode() && RequestAnalyser.getRequestAnalyser().getApproximateNumberOfDatabaseQueries() > 0)
		{
	        if(wait > 1000)
			{
				logger.warn("The clearCache method waited over " + ((wait * 10) / 1000) + " seconds but there seems to be " + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + " requests blocking all the time. Continuing anyway.");
				//printThreads();
				break;
			}

	        if(wait > 100)
				setForcedCacheEvictionMode(true);

	        Thread.sleep(10);
	        wait++;
	    }
		
		if(type.getName().contains(".SiteNodeVersionImpl") || 
		   type.getName().contains(".MediumSiteNodeVersionImpl") || 
		   type.getName().contains(".SiteNodeImpl") || 
		   type.getName().contains(".LanguageImpl") || 
		   type.getName().contains(".RepositoryImpl") || 
		   type.getName().contains(".RepositoryLanguageImpl") || 
		   type.getName().contains(".DigitalAssetImpl") || 
		   type.getName().contains(".MediumDigitalAssetImpl") || 
		   type.getName().contains(".ContentVersionImpl") || 
		   type.getName().contains(".AccessRightRoleImpl") || 
		   type.getName().contains(".AccessRightGroupImpl") || 
		   type.getName().contains(".AccessRightUserImpl") || 
		   type.getName().contains(".RegistryImpl"))
		{
			logger.info("Skipping clear of " + type);
		}
		else
		{
			logger.info("Clearing for real: " + type);
		    CacheManager manager = db.getCacheManager();
		    manager.expireCache(type, ids);
		    //Class[] types = {type};
		    //db.expireCache(types, ids);
		}
		
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

	public static void clearCache(Database db, Class c) throws Exception
	{
		long wait = 0;
		while(!getForcedCacheEvictionMode() && RequestAnalyser.getRequestAnalyser().getApproximateNumberOfDatabaseQueries() > 0)
		{
	        if(wait > 1000)
			{
				logger.warn("The clearCache method waited over " + ((wait * 10) / 1000) + " seconds but there seems to be " + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + " requests blocking all the time. Continuing anyway.");
				//printThreads();
				break;
			}

	        if(wait > 100)
				setForcedCacheEvictionMode(true);

	        Thread.sleep(10);
	        wait++;
	    }

		if(c.getName().contains(".SiteNodeVersionImpl") || 
		   c.getName().contains(".MediumSiteNodeVersionImpl") || 
		   c.getName().contains(".SiteNodeImpl") || 
		   c.getName().contains(".LanguageImpl") || 
		   c.getName().contains(".RepositoryImpl") || 
		   c.getName().contains(".RepositoryLanguageImpl") || 
		   c.getName().contains(".DigitalAssetImpl") || 
		   c.getName().contains(".MediumDigitalAssetImpl") || 
		   c.getName().contains(".ContentVersionImpl") || 
		   c.getName().contains(".AccessRightRoleImpl") || 
		   c.getName().contains(".AccessRightGroupImpl") || 
		   c.getName().contains(".AccessRightUserImpl") ||
		   c.getName().contains(".RegistryImpl"))
		{
			logger.info("Skipping " + c.getName() + " as they have no castor cache");
		}
		else
		{
			logger.info("Clearing for real: " + c);
			Class[] types = {c};
			CacheManager manager = db.getCacheManager();
			manager.expireCache(types);
			//db.expireCache(types, null);
		}
		
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

	public static void cacheCentralCastorCaches() throws Exception
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

    public static void evictWaitingCache() throws Exception
    {	    
    	Timer t = new Timer();
       	String operatingMode = CmsPropertyHandler.getOperatingMode();
	    synchronized(RequestAnalyser.getRequestAnalyser()) 
	    {
	       	if(RequestAnalyser.getRequestAnalyser().getBlockRequests() && RequestAnalyser.getRequestAnalyser().getBlockRequestTime() < 30000)
		    {
			    logger.warn("evictWaitingCache allready in progress - returning to avoid conflict");
			    return;
		    }
	       	else if(RequestAnalyser.getRequestAnalyser().getBlockRequests() && RequestAnalyser.getRequestAnalyser().getBlockRequestTime() > 30000)
	       		logger.warn("An block must have gone wrong... there has gone over 30 seconds and still not reported done.. let's run anyway.");

	       	RequestAnalyser.getRequestAnalyser().setBlockRequests(true);
		}
	    
	    logger.info("evictWaitingCache starting");
    	logger.info("blocking");
    	synchronized(notifications)
        {
        	if(notifications == null || notifications.size() == 0)
        	{
        		logger.info("No notifications...");
        		RequestAnalyser.getRequestAnalyser().setBlockRequests(false);
        		return;
        	}
        }
    	logger.info("Had some notifications to handle:" + notifications.size());

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
				//RequestAnalyser.getRequestAnalyser().addPublication("" + formatter.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " - " + cacheEvictionBean.getClassName() + " - " + cacheEvictionBean.getObjectId());
				
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
						catch (Throwable throwable) 
						{
							logger.warn("Could not refresh authentication filter:" + throwable.getMessage(), throwable);
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
	    
	    t.printElapsedTime("evict done");

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

		caches.add("redirectCache");
		caches.add("languageCache");
		caches.add("childSiteNodesCache");
		caches.add("childPagesCache");
		caches.add("parentSiteNodeCache");
		caches.add("contentTypeDefinitionCache");
		caches.add("componentContentsCache");
		caches.add("contentVersionIdCache");
		caches.add("rootSiteNodeCache");
		caches.add("contentCache");
		caches.add("relatedCategoriesCache");
		caches.add("matchingContentsCache");
		caches.add("propertiesCategoryCache");
		caches.add("userCache");
		caches.add("siteNodeCache");
		caches.add("contentCategoryCache");
		caches.add("contentVersionCache");
		caches.add("digitalAssetCache");
		caches.add("latestSiteNodeVersionCache");
		caches.add("interceptionPointCache");
		caches.add("contentAttributeCache");		
		caches.add("metaInfoContentAttributeCache");		
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
		caches.add("assetUrlCacheWithGroups");
		caches.add("componentPropertyCacheRepoGroups");
		caches.add("pageDeliveryMetaDataCache");

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

        	    pageKey = pageKey.replaceAll("_\\$session." + sessionAttribute, "" + (sessionAttributeValue == null ? "" : "_" + sessionAttributeValue));    	    
    	    
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
            File file = new File(filePath);
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
	 		            		contents = compressionHelper.decompress(cachedCompressedData);		
		            	}
		            	else
		            	{
			            	//logger.info("getting file anyway:" + updateInterval);
			            	contents = FileHelper.getFileAsStringOpt(file, charEncoding);
		            	}

		            	//contents = FileHelper.getFileAsString(file, charEncoding);
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
 		            		contents = compressionHelper.decompress(cachedCompressedData);
	            	}
	            	else
	            	{
		            	//logger.info("getting file anyway:" + updateInterval);
		            	contents = FileHelper.getFileAsStringOpt(file, charEncoding);
	            	}
	            	//contents = FileHelper.getFileAsString(file, charEncoding);
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

    public static void clearPageCache(String key)
    {        
    	clearCache("pageCache", key);
    	/*
    	try
    	{
        	String firstPart = ("" + key.hashCode()).substring(0, 3);
            String dir = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "caches" + File.separator + "pageCache" + File.separator + firstPart;
            File dirFile = new File(dir);
            dirFile.mkdirs();
            File file = new File(dir + File.separator + key.hashCode());
            if(logger.isInfoEnabled())
            	logger.info("Deleting " + file.getPath() + ":" + file.exists());
            if(file.exists())
            {
            	boolean deleted = file.delete();
            	if(logger.isInfoEnabled())
            		logger.info("Deleted: " + deleted);
            }
    	}
    	catch (Exception e) 
    	{
    		logger.warn("Problem storing data to file:" + e.getMessage());
		}
		*/
    }
    
    private static void removeCachedContentInFile(String cacheName, String key)
    {
    	try
    	{
    		String firstPart = ("" + key).substring(0, 3);
            String dir = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "caches" + File.separator + cacheName + File.separator + firstPart;
            File dirFile = new File(dir);
            dirFile.mkdirs();
            File file = new File(dir + File.separator + key);
            logger.info("Deleting " + file.getPath());
            file.delete();
    	}
    	catch (Exception e) 
    	{
    		logger.warn("Problem storing data to file:" + e.getMessage());
		}
    }

	public static void clearFileCaches()
	{
		String dir = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "caches";
        File dirFile = new File(dir);
        File dirOldFile = new File(dir+"CAN_BE_REMOVED");
        if(dirFile.exists())
        	dirFile.renameTo(dirOldFile);
        
        new Thread(new Runnable() { public void run() {try {CacheController.clearFileCachesImpl(new File(CmsPropertyHandler.getDigitalAssetPath() + File.separator + "caches_CAN_BE_REMOVED"));} catch (Exception e) {}}}).start();
	}
	
	public static void clearFileCachesImpl(File dirFile)
	{
        //System.out.println("dirFile:" + dirFile.exists());
        if(dirFile.exists())
        {
            File[] subCaches = dirFile.listFiles();
            for(int i=0; i<subCaches.length; i++)
            {
            	File subCacheDir = subCaches[i];
            	//System.out.println("subCacheDir:" + subCacheDir.getName());
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
                        		//System.out.println("cacheFile:" + cacheFile.getName());
                    			cacheFile.delete();
                        	}

                    		subCacheDir.delete();
                    	}			                

                		//System.out.println("cacheFile:" + cacheFile.getName());
                    	subSubCacheDir.delete();
                	}

            		subCacheDir.delete();
               }			                
            }
        }
	}

	public static void clearFileCaches(String cacheName)
	{
		CacheController.clearFileCachesImpl(cacheName);
        //new Thread(new Runnable() { public void run() {try {CacheController.clearFileCachesImpl(cacheName);} catch (Exception e) {}}}).start();
	}
	
	public static void clearFileCachesImpl(String cacheName)
	{
        String dir = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "caches";
        File dirFile = new File(dir);
        //System.out.println("dirFile:" + dirFile.exists());
        if(dirFile.exists())
        {
            File[] subCaches = dirFile.listFiles();
            for(int i=0; i<subCaches.length; i++)
            {
            	File subCacheDir = subCaches[i];
            	//System.out.println("subCacheDir:" + subCacheDir.getName());
            	if(subCacheDir.isDirectory() && subCacheDir.getName().equals(cacheName) || (cacheName.equals("pageCache") && subCacheDir.getName().startsWith(cacheName)))
            	{
            		try
            		{
	                	File[] subSubCacheFiles = subCacheDir.listFiles();
	                	for(int j=0; j<subSubCacheFiles.length; j++)
	                	{
	                		File subSubCacheDir = subSubCacheFiles[j];
	                    	if(subSubCacheDir.isDirectory())
	                    	{
	                        	File[] cacheFiles = subSubCacheDir.listFiles();
	                        	if(cacheFiles != null)
	                        	{
		                        	for(int k=0; k<cacheFiles.length; k++)
		                        	{
		                        		File cacheFile = cacheFiles[k];
		                        		//System.out.println("cacheFile:" + cacheFile.getName());
		                    			cacheFile.delete();
		                        	}
	                        	}
	                        	
	                    		subCacheDir.delete();
	                    	}			                
	
	                		//System.out.println("cacheFile:" + cacheFile.getName());
	                    	subSubCacheDir.delete();
	                	}
	            		subCacheDir.delete();

            		}
            		catch (Exception e) 
            		{
            			logger.warn("It seems the cache dir: " + cacheName + " was allready empty or removed. Error: " + e.getMessage());
					}
            	}
            }
        }
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
	
    static void commitTransaction(Database db) throws SystemException
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

	public static int getPropertyAsStringHashCode(String inheritedPageComponentsXML, Integer componentId, String propertyName, Integer siteNodeId, Integer languageId) throws Exception
	{
		Map property = null;
		
        XmlInfosetBuilder builder = XmlInfosetBuilder.newInstance();
        XmlDocument doc = builder.parseReader(new StringReader( inheritedPageComponentsXML ) );
        
		String propertyXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + propertyName + "']";
		
		Xb1XPath xpathObject = new Xb1XPath( propertyXPath );

        List anl = xpathObject.selectNodes( doc );

		//If not found on the same component id - let's check them all and use the first we find.
		if(anl == null || anl.size() == 0)
		{
			String globalPropertyXPath = "(//component/properties/property[@name='" + propertyName + "'])[1]";
			
			Xb1XPath globalXpathObject = new Xb1XPath( globalPropertyXPath );
	        anl = globalXpathObject.selectNodes( doc );
		}			

		StringBuilder sb = new StringBuilder();
		
		Iterator anlIterator = anl.iterator();
		while(anlIterator.hasNext())
		{
			XmlElement infosetItem = (XmlElement)anlIterator.next();
			String propertyXML = builder.serializeToString(infosetItem);
			//System.out.println("propertyXML:" + propertyXML);
			
			sb.append(propertyXML);
		}
		//System.out.println("propertyXML HASH:" + sb.toString().hashCode());
		
		return sb.toString().hashCode();
	}
	
	public static int getComponentsAsStringHashCode(String inheritedPageComponentsXML, String componentXPath) throws Exception
	{
        XmlInfosetBuilder builder = XmlInfosetBuilder.newInstance();
        XmlDocument doc = builder.parseReader(new StringReader( inheritedPageComponentsXML ) );

		Xb1XPath xpathObject = new Xb1XPath( componentXPath );

        //This keeps track of the cached inherited components.
        StringBuilder sb = new StringBuilder();
        
        List componentNodeList = xpathObject.selectNodes( doc.getDocumentElement() );
		Iterator componentNodeListIterator = componentNodeList.iterator();
		while(componentNodeListIterator.hasNext())
		{
			XmlElement child 	= (XmlElement)componentNodeListIterator.next();
			
			String componentString = builder.serializeToString(child).trim();
			sb.append(componentString);
			//System.out.println("componentString:" + componentString);
			
		}
		//System.out.println("propertyXML HASH:" + sb.toString().hashCode());
		
		return sb.toString().hashCode();
	}
	
    public static Map getEventListeners()
    {
        return eventListeners;
    }


}