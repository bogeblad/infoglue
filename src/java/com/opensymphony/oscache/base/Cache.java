/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;

import com.opensymphony.oscache.base.algorithm.AbstractConcurrentReadCache;
import com.opensymphony.oscache.base.algorithm.LRUCache;
import com.opensymphony.oscache.base.algorithm.UnlimitedCache;
import com.opensymphony.oscache.base.events.*;
import com.opensymphony.oscache.base.persistence.PersistenceListener;
import com.opensymphony.oscache.util.FastCronParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

import java.text.ParseException;

import java.util.*;

import javax.swing.event.EventListenerList;

/**
 * Provides an interface to the cache itself. Creating an instance of this class
 * will create a cache that behaves according to its construction parameters.
 * The public API provides methods to manage objects in the cache and configure
 * any cache event listeners.
 *
 * @version        $Revision: 1.1 $
 * @author <a href="mailto:mike@atlassian.com">Mike Cannon-Brookes</a>
 * @author <a href="mailto:tgochenour@peregrine.com">Todd Gochenour</a>
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public class Cache implements Serializable {
    /**
     * An event that origininated from within another event.
     */
    public static final String NESTED_EVENT = "NESTED";
    private static transient final Log log = LogFactory.getLog(Cache.class);

    /**
     * A list of all registered event listeners for this cache.
     */
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * The actual cache map. This is where the cached objects are held.
     */
    public AbstractConcurrentReadCache cacheMap = null;

    /**
     * Date of last complete cache flush.
     */
    private Date flushDateTime = null;

    /**
     * A map that holds keys of cache entries that are currently being built, and EntryUpdateState instance as values. This is used to coordinate threads
     * that modify/access a same key in concurrence.
     * 
     * The cache checks against this map when a stale entry is requested, or a cache miss is observed.
     * 
     * If the requested key is in here, we know the entry is currently being
     * built by another thread and hence we can either block and wait or serve
     * the stale entry (depending on whether cache blocking is enabled or not).
     * <p>
     * To avoid data races, values in this map should remain present during the whole time distinct threads deal with the
     * same key. We implement this using explicit reference counting in the EntryUpdateState instance, to be able to clean up
     * the map once all threads have declared they are done accessing/updating a given key.
     * 
     * It is not possible to locate this into the CacheEntry because this would require to have a CacheEntry instance for all cache misses, and
     * may therefore generate a memory leak. More over, the CacheEntry instance may not be hold in memory in the case no
     * memory cache is configured.
     */
    private Map updateStates = new HashMap();

    /**
     * Indicates whether the cache blocks requests until new content has
     * been generated or just serves stale content instead.
     */
    private boolean blocking = false;

    /**
     * Create a new Cache
     *
     * @param useMemoryCaching Specify if the memory caching is going to be used
     * @param unlimitedDiskCache Specify if the disk caching is unlimited
     * @param overflowPersistence Specify if the persistent cache is used in overflow only mode
     */
    public Cache(boolean useMemoryCaching, boolean unlimitedDiskCache, boolean overflowPersistence) {
        this(useMemoryCaching, unlimitedDiskCache, overflowPersistence, false, null, 0);
    }

    /**
     * Create a new Cache.
     *
     * If a valid algorithm class is specified, it will be used for this cache.
     * Otherwise if a capacity is specified, it will use LRUCache.
     * If no algorithm or capacity is specified UnlimitedCache is used.
     *
     * @see com.opensymphony.oscache.base.algorithm.LRUCache
     * @see com.opensymphony.oscache.base.algorithm.UnlimitedCache
     * @param useMemoryCaching Specify if the memory caching is going to be used
     * @param unlimitedDiskCache Specify if the disk caching is unlimited
     * @param overflowPersistence Specify if the persistent cache is used in overflow only mode
     * @param blocking This parameter takes effect when a cache entry has
     * just expired and several simultaneous requests try to retrieve it. While
     * one request is rebuilding the content, the other requests will either
     * block and wait for the new content (<code>blocking == true</code>) or
     * instead receive a copy of the stale content so they don't have to wait
     * (<code>blocking == false</code>). the default is <code>false</code>,
     * which provides better performance but at the expense of slightly stale
     * data being served.
     * @param algorithmClass The class implementing the desired algorithm
     * @param capacity The capacity
     */
    public Cache(boolean useMemoryCaching, boolean unlimitedDiskCache, boolean overflowPersistence, boolean blocking, String algorithmClass, int capacity) {
        // Instantiate the algo class if valid
        if (((algorithmClass != null) && (algorithmClass.length() > 0)) && (capacity > 0)) {
            try {
                cacheMap = (AbstractConcurrentReadCache) Class.forName(algorithmClass).newInstance();
                cacheMap.setMaxEntries(capacity);
            } catch (Exception e) {
                log.error("Invalid class name for cache algorithm class. " + e.toString());
            }
        }

        if (cacheMap == null) {
            // If we have a capacity, use LRU cache otherwise use unlimited Cache
            if (capacity > 0) {
                cacheMap = new LRUCache(capacity);
            } else {
                cacheMap = new UnlimitedCache();
            }
        }

        cacheMap.setUnlimitedDiskCache(unlimitedDiskCache);
        cacheMap.setOverflowPersistence(overflowPersistence);
        cacheMap.setMemoryCaching(useMemoryCaching);

        this.blocking = blocking;
    }
    
    /**
     * @return the maximum number of items to cache can hold.
     */
    public int getCapacity() {
    	return cacheMap.getMaxEntries();
    }

    /**
     * Allows the capacity of the cache to be altered dynamically. Note that
     * some cache implementations may choose to ignore this setting (eg the
     * {@link UnlimitedCache} ignores this call).
     *
     * @param capacity the maximum number of items to hold in the cache.
     */
    public void setCapacity(int capacity) {
        cacheMap.setMaxEntries(capacity);
    }

    /**
     * Checks if the cache was flushed more recently than the CacheEntry provided.
     * Used to determine whether to refresh the particular CacheEntry.
     *
     * @param cacheEntry The cache entry which we're seeing whether to refresh
     * @return Whether or not the cache has been flushed more recently than this cache entry was updated.
     */
    public boolean isFlushed(CacheEntry cacheEntry) {
        if (flushDateTime != null) {
            final long lastUpdate = cacheEntry.getLastUpdate();
            final long flushTime = flushDateTime.getTime();

            // CACHE-241: check flushDateTime with current time also
            return (flushTime <= System.currentTimeMillis()) && (flushTime >= lastUpdate);
        } else {
            return false;
        }
    }

    /**
     * Retrieve an object from the cache specifying its key.
     *
     * @param key             Key of the object in the cache.
     *
     * @return The object from cache
     *
     * @throws NeedsRefreshException Thrown when the object either
     * doesn't exist, or exists but is stale. When this exception occurs,
     * the CacheEntry corresponding to the supplied key will be locked
     * and other threads requesting this entry will potentially be blocked
     * until the caller repopulates the cache. If the caller choses not
     * to repopulate the cache, they <em>must</em> instead call
     * {@link #cancelUpdate(String)}.
     */
    public Object getFromCache(String key) throws NeedsRefreshException {
        return getFromCache(key, CacheEntry.INDEFINITE_EXPIRY, null);
    }

    /**
     * Retrieve an object from the cache specifying its key.
     *
     * @param key             Key of the object in the cache.
     * @param refreshPeriod   How long before the object needs refresh. To
     * allow the object to stay in the cache indefinitely, supply a value
     * of {@link CacheEntry#INDEFINITE_EXPIRY}.
     *
     * @return The object from cache
     *
     * @throws NeedsRefreshException Thrown when the object either
     * doesn't exist, or exists but is stale. When this exception occurs,
     * the CacheEntry corresponding to the supplied key will be locked
     * and other threads requesting this entry will potentially be blocked
     * until the caller repopulates the cache. If the caller choses not
     * to repopulate the cache, they <em>must</em> instead call
     * {@link #cancelUpdate(String)}.
     */
    public Object getFromCache(String key, int refreshPeriod) throws NeedsRefreshException {
        return getFromCache(key, refreshPeriod, null);
    }

    /**
     * Retrieve an object from the cache specifying its key.
     *
     * @param key             Key of the object in the cache.
     * @param refreshPeriod   How long before the object needs refresh. To
     * allow the object to stay in the cache indefinitely, supply a value
     * of {@link CacheEntry#INDEFINITE_EXPIRY}.
     * @param cronExpiry      A cron expression that specifies fixed date(s)
     *                        and/or time(s) that this cache entry should
     *                        expire on.
     *
     * @return The object from cache
     *
     * @throws NeedsRefreshException Thrown when the object either
     * doesn't exist, or exists but is stale. When this exception occurs,
     * the CacheEntry corresponding to the supplied key will be locked
     * and other threads requesting this entry will potentially be blocked
     * until the caller repopulates the cache. If the caller choses not
     * to repopulate the cache, they <em>must</em> instead call
     * {@link #cancelUpdate(String)}.
     */
    public Object getFromCache(String key, int refreshPeriod, String cronExpiry) throws NeedsRefreshException {
    	CacheEntry cacheEntry = this.getCacheEntry(key, null, null);

        Object content = cacheEntry.getContent();
        CacheMapAccessEventType accessEventType = CacheMapAccessEventType.HIT;

        boolean reload = false;

        // Check if this entry has expired or has not yet been added to the cache. If
        // so, we need to decide whether to block, serve stale content or throw a
        // NeedsRefreshException
        if (this.isStale(cacheEntry, refreshPeriod, cronExpiry)) {

        	//Get access to the EntryUpdateState instance and increment the usage count during the potential sleep
        	EntryUpdateState updateState = getUpdateState(key);
        	//System.out.println("Stale:" + updateState.state + EntryUpdateState.NOT_YET_UPDATING);
            try {
            	synchronized (updateState) {
            		if (updateState.isAwaitingUpdate() || updateState.isCancelled()) {
            			// No one else is currently updating this entry - grab ownership
            			updateState.startUpdate();
            			
            			if (cacheEntry.isNew()) {
            				accessEventType = CacheMapAccessEventType.MISS;
            			} else {
            				accessEventType = CacheMapAccessEventType.STALE_HIT;
            			}
            		} else if (updateState.isUpdating()) {
            			// Another thread is already updating the cache. We block if this
            			// is a new entry, or blocking mode is enabled. Either putInCache()
            			// or cancelUpdate() can cause this thread to resume.
						System.out.println("Yes - it's updating...");
            			if (cacheEntry.isNew() || blocking) {
            				do {
            					try {
            						System.out.println("updateState was in:" + cacheEntry.isNew() + ":" + blocking);
            						updateState.wait();
            					} catch (InterruptedException e) {
            					}
            				} while (updateState.isUpdating());
            				
            				if (updateState.isCancelled()) {
            					// The updating thread cancelled the update, let this one have a go. 
            					// This increments the usage count for this EntryUpdateState instance
            					updateState.startUpdate();
            					
            					if (cacheEntry.isNew()) {
            						accessEventType = CacheMapAccessEventType.MISS;
            					} else {
            						accessEventType = CacheMapAccessEventType.STALE_HIT;
            					}
            				} else if (updateState.isComplete()) {
            					reload = true;
            				} else {
            					log.error("Invalid update state for cache entry " + key);
            				}
            			}
            		} else {
            			reload = true;
            		}
            	}
            } finally {
            	//Make sure we release the usage count for this EntryUpdateState since we don't use it anymore. If the current thread started the update, then the counter was
            	//increased by one in startUpdate()
            	releaseUpdateState(updateState, key);
            }
        }

        // If reload is true then another thread must have successfully rebuilt the cache entry
        if (reload) {
            cacheEntry = (CacheEntry) cacheMap.get(key);

            if (cacheEntry != null) {
                content = cacheEntry.getContent();
            } else {
                log.error("Could not reload cache entry after waiting for it to be rebuilt");
            }
        }

        dispatchCacheMapAccessEvent(accessEventType, cacheEntry, null);

        // If we didn't end up getting a hit then we need to throw a NRE
        if (accessEventType != CacheMapAccessEventType.HIT) {
            throw new NeedsRefreshException(content);
        }

        return content;
    }

    /**
     * Set the listener to use for data persistence. Only one
     * <code>PersistenceListener</code> can be configured per cache.
     *
     * @param listener The implementation of a persistance listener
     */
    public void setPersistenceListener(PersistenceListener listener) {
        cacheMap.setPersistenceListener(listener);
    }

    /**
     * Retrieves the currently configured <code>PersistenceListener</code>.
     *
     * @return the cache's <code>PersistenceListener</code>, or <code>null</code>
     * if no listener is configured.
     */
    public PersistenceListener getPersistenceListener() {
        return cacheMap.getPersistenceListener();
    }

    /**
     * Register a listener for Cache events. The listener must implement
     * one of the child interfaces of the {@link CacheEventListener} interface.
     *
     * @param listener  The object that listens to events.
     * @since 2.4
     */
    public void addCacheEventListener(CacheEventListener listener) {
        // listenerList.add(CacheEventListener.class, listener);
        listenerList.add((Class)listener.getClass(), listener);
    }
    
    /**
     * Register a listener for Cache events. The listener must implement
     * one of the child interfaces of the {@link CacheEventListener} interface.
     *
     * @param listener  The object that listens to events.
     * @param clazz the type of the listener to be added
     * @deprecated use {@link #addCacheEventListener(CacheEventListener)}
     */
    public void addCacheEventListener(CacheEventListener listener, Class clazz) {
        if (CacheEventListener.class.isAssignableFrom(clazz)) {
            listenerList.add(clazz, listener);
        } else {
            log.error("The class '" + clazz.getName() + "' is not a CacheEventListener. Ignoring this listener.");
        }
    }
    
    /**
     * Returns the list of all CacheEventListeners.
     * @return the CacheEventListener's list of the Cache
     */
    public EventListenerList getCacheEventListenerList() {
        return listenerList;
    }

    /**
     * Cancels any pending update for this cache entry. This should <em>only</em>
     * be called by the thread that is responsible for performing the update ie
     * the thread that received the original {@link NeedsRefreshException}.<p/>
     * If a cache entry is not updated (via {@link #putInCache} and this method is
     * not called to let OSCache know the update will not be forthcoming, subsequent
     * requests for this cache entry will either block indefinitely (if this is a new
     * cache entry or cache.blocking=true), or forever get served stale content. Note
     * however that there is no harm in cancelling an update on a key that either
     * does not exist or is not currently being updated.
     *
     * @param key The key for the cache entry in question.
     * @throws IllegalStateException if the cache entry isn't in the state UPDATE_IN_PROGRESS
     */
    public void cancelUpdate(String key) {
        EntryUpdateState state;

        if (key != null) {
            synchronized (updateStates) {
                state = (EntryUpdateState) updateStates.get(key);

                if (state != null) {
                    synchronized (state) {
                    	int usageCounter = state.cancelUpdate();
                        state.notify();
                        
                        checkEntryStateUpdateUsage(key, state, usageCounter);
                    }
                } else {
        			if (log.isErrorEnabled()) {
        				log.error("internal error: expected to get a state from key [" + key + "]");
        			}
                }
            }
        }
    }

    /**
     * Utility method to check if the specified usage count is zero, and if so remove the corresponding EntryUpdateState from the updateStates. This is designed to factor common code.
     * 
     * Warning: This method should always be called while holding both the updateStates field and the state parameter
     * @throws Exception
	 */
	private void checkEntryStateUpdateUsage(String key, EntryUpdateState state, int usageCounter) {
		//Clean up the updateStates map to avoid a memory leak once no thread is using this EntryUpdateState instance anymore.
		if (usageCounter ==0) {
			EntryUpdateState removedState = (EntryUpdateState) updateStates.remove(key);
			if (state != removedState) {
				if (log.isErrorEnabled()) {
					try {
						throw new Exception("OSCache: internal error: removed state [" + removedState + "] from key [" + key + "] whereas we expected [" + state + "]");
					} catch (Exception e) {
                        log.error(e);
					}
				}
			}
		}
	}

	/**
     * Flush all entries in the cache on the given date/time.
     *
     * @param date The date at which all cache entries will be flushed.
     */
    public void flushAll(Date date) {
        flushAll(date, null);
    }

    /**
     * Flush all entries in the cache on the given date/time.
     *
     * @param date The date at which all cache entries will be flushed.
     * @param origin The origin of this flush request (optional)
     */
    public void flushAll(Date date, String origin) {
        flushDateTime = date;

        if (listenerList.getListenerCount() > 0) {
            dispatchCachewideEvent(CachewideEventType.CACHE_FLUSHED, date, origin);
        }
    }

    /**
     * Flush the cache entry (if any) that corresponds to the cache key supplied.
     * This call will flush the entry from the cache and remove the references to
     * it from any cache groups that it is a member of. On completion of the flush,
     * a <tt>CacheEntryEventType.ENTRY_FLUSHED</tt> event is fired.
     *
     * @param key The key of the entry to flush
     */
    public void flushEntry(String key) {
        flushEntry(key, null);
    }

    /**
     * Flush the cache entry (if any) that corresponds to the cache key supplied.
     * This call will mark the cache entry as flushed so that the next access
     * to it will cause a {@link NeedsRefreshException}. On completion of the
     * flush, a <tt>CacheEntryEventType.ENTRY_FLUSHED</tt> event is fired.
     *
     * @param key The key of the entry to flush
     * @param origin The origin of this flush request (optional)
     */
    public void flushEntry(String key, String origin) {
        flushEntry(getCacheEntry(key, null, origin), origin);
    }

    /**
     * Flushes all objects that belong to the supplied group. On completion
     * this method fires a <tt>CacheEntryEventType.GROUP_FLUSHED</tt> event.
     *
     * @param group The group to flush
     */
    public void flushGroup(String group) {
        flushGroup(group, null);
    }

    /**
     * Flushes all unexpired objects that belong to the supplied group. On
     * completion this method fires a <tt>CacheEntryEventType.GROUP_FLUSHED</tt>
     * event.
     *
     * @param group The group to flush
     * @param origin The origin of this flush event (optional)
     */
    public void flushGroup(String group, String origin) {
        // Flush all objects in the group
        Set groupEntries = cacheMap.getGroup(group);

        if (groupEntries != null) {
            Iterator itr = groupEntries.iterator();
            String key;
            CacheEntry entry;

            while (itr.hasNext()) {
                key = (String) itr.next();
                entry = (CacheEntry) cacheMap.get(key);

                if ((entry != null) && !entry.needsRefresh(CacheEntry.INDEFINITE_EXPIRY)) {
                    flushEntry(entry, NESTED_EVENT);
                }
            }
        }

        if (listenerList.getListenerCount() > 0) {
            dispatchCacheGroupEvent(CacheEntryEventType.GROUP_FLUSHED, group, origin);
        }
    }

    /**
     * Flush all entries with keys that match a given pattern
     *
     * @param  pattern The key must contain this given value
     * @deprecated For performance and flexibility reasons it is preferable to
     * store cache entries in groups and use the {@link #flushGroup(String)} method
     * instead of relying on pattern flushing.
     */
    public void flushPattern(String pattern) {
        flushPattern(pattern, null);
    }

    /**
     * Flush all entries with keys that match a given pattern
     *
     * @param  pattern The key must contain this given value
     * @param origin The origin of this flush request
     * @deprecated For performance and flexibility reasons it is preferable to
     * store cache entries in groups and use the {@link #flushGroup(String, String)}
     * method instead of relying on pattern flushing.
     */
    public void flushPattern(String pattern, String origin) {
        // Check the pattern
        if ((pattern != null) && (pattern.length() > 0)) {
            String key = null;
            CacheEntry entry = null;
            Iterator itr = cacheMap.keySet().iterator();

            while (itr.hasNext()) {
                key = (String) itr.next();

                if (key.indexOf(pattern) >= 0) {
                    entry = (CacheEntry) cacheMap.get(key);

                    if (entry != null) {
                        flushEntry(entry, origin);
                    }
                }
            }

            if (listenerList.getListenerCount() > 0) {
                dispatchCachePatternEvent(CacheEntryEventType.PATTERN_FLUSHED, pattern, origin);
            }
        } else {
            // Empty pattern, nothing to do
        }
    }

    /**
     * Put an object in the cache specifying the key to use.
     *
     * @param key       Key of the object in the cache.
     * @param content   The object to cache.
     */
    public void putInCache(String key, Object content) {
        putInCache(key, content, null, null, null);
    }

    /**
     * Put an object in the cache specifying the key and refresh policy to use.
     *
     * @param key       Key of the object in the cache.
     * @param content   The object to cache.
     * @param policy   Object that implements refresh policy logic
     */
    public void putInCache(String key, Object content, EntryRefreshPolicy policy) {
        putInCache(key, content, null, policy, null);
    }

    /**
     * Put in object into the cache, specifying both the key to use and the
     * cache groups the object belongs to.
     *
     * @param key       Key of the object in the cache
     * @param content   The object to cache
     * @param groups    The cache groups to add the object to
     */
    public void putInCache(String key, Object content, String[] groups) {
        putInCache(key, content, groups, null, null);
    }

    /**
     * Put an object into the cache specifying both the key to use and the
     * cache groups the object belongs to.
     *
     * @param key       Key of the object in the cache
     * @param groups    The cache groups to add the object to
     * @param content   The object to cache
     * @param policy    Object that implements the refresh policy logic
     */
    public void putInCache(String key, Object content, String[] groups, EntryRefreshPolicy policy, String origin) {
        CacheEntry cacheEntry = this.getCacheEntry(key, policy, origin);
        boolean isNewEntry = cacheEntry.isNew();

        // [CACHE-118] If we have an existing entry, create a new CacheEntry so we can still access the old one later
        if (!isNewEntry) {
            cacheEntry = new CacheEntry(key, policy);
        }

        cacheEntry.setContent(content);
        cacheEntry.setGroups(groups);
        cacheMap.put(key, cacheEntry);

        // Signal to any threads waiting on this update that it's now ready for them
        // in the cache!
        completeUpdate(key);

        if (listenerList.getListenerCount() > 0) {
            CacheEntryEvent event = new CacheEntryEvent(this, cacheEntry, origin);

            if (isNewEntry) {
                dispatchCacheEntryEvent(CacheEntryEventType.ENTRY_ADDED, event);
            } else {
                dispatchCacheEntryEvent(CacheEntryEventType.ENTRY_UPDATED, event);
            }
        }
    }

    /**
     * Unregister a listener for Cache events.
     *
     * @param listener  The object that currently listens to events.
     * @param clazz  The registrated class of listening object.
     * @deprecated use instead {@link #removeCacheEventListener(CacheEventListener)}
     */
    public void removeCacheEventListener(CacheEventListener listener, Class clazz) {
        listenerList.remove(clazz, listener);
    }

    /**
     * Unregister a listener for Cache events.
     *
     * @param listener  The object that currently listens to events.
     * @since 2.4
     */
    public void removeCacheEventListener(CacheEventListener listener) {
        // listenerList.remove(CacheEventListener.class, listener);
        listenerList.remove((Class)listener.getClass(), listener);
    }

    /**
     * Get an entry from this cache or create one if it doesn't exist.
     *
     * @param key    The key of the cache entry
     * @param policy Object that implements refresh policy logic
     * @param origin The origin of request (optional)
     * @return CacheEntry for the specified key.
     */
    protected CacheEntry getCacheEntry(String key, EntryRefreshPolicy policy, String origin) {
        CacheEntry cacheEntry = null;

        // Verify that the key is valid
        if ((key == null) || (key.length() == 0)) {
            throw new IllegalArgumentException("getCacheEntry called with an empty or null key");
        }

        cacheEntry = (CacheEntry) cacheMap.get(key);

        // if the cache entry does not exist, create a new one
        if (cacheEntry == null) {
            if (log.isDebugEnabled()) {
                log.debug("No cache entry exists for key='" + key + "', creating");
            }

            cacheEntry = new CacheEntry(key, policy);
        }

        return cacheEntry;
    }

    /**
     * Indicates whether or not the cache entry is stale.
     *
     * @param cacheEntry     The cache entry to test the freshness of.
     * @param refreshPeriod  The maximum allowable age of the entry, in seconds.
     * @param cronExpiry     A cron expression specifying absolute date(s) and/or time(s)
     * that the cache entry should expire at. If the cache entry was refreshed prior to
     * the most recent match for the cron expression, the entry will be considered stale.
     *
     * @return <code>true</code> if the entry is stale, <code>false</code> otherwise.
     */
    protected boolean isStale(CacheEntry cacheEntry, int refreshPeriod, String cronExpiry) {
        boolean result = cacheEntry.needsRefresh(refreshPeriod) || isFlushed(cacheEntry);

        if ((!result) && (cronExpiry != null) && (cronExpiry.length() > 0)) {
            try {
                FastCronParser parser = new FastCronParser(cronExpiry);
                result = result || parser.hasMoreRecentMatch(cacheEntry.getLastUpdate());
            } catch (ParseException e) {
                log.warn(e);
            }
        }

        return result;
    }

    /**
     * Get the updating cache entry from the update map. If one is not found,
     * create a new one (with state {@link EntryUpdateState#NOT_YET_UPDATING})
     * and add it to the map.
     *
     * @param key The cache key for this entry
     *
     * @return the CacheEntry that was found (or added to) the updatingEntries
     * map.
     */
    protected EntryUpdateState getUpdateState(String key) {
        EntryUpdateState updateState;

        synchronized (updateStates) {
            // Try to find the matching state object in the updating entry map.
            updateState = (EntryUpdateState) updateStates.get(key);

            if (updateState == null) {
                // It's not there so add it.
                updateState = new EntryUpdateState();
                updateStates.put(key, updateState);
            } else {
            	//Otherwise indicate that we start using it to prevent its removal until all threads are done with it.
            	updateState.incrementUsageCounter();
            }
        }

        return updateState;
    }

    /**
     * releases the usage that was made of the specified EntryUpdateState. When this reaches zero, the entry is removed from the map. 
     * @param state the state to release the usage of
     * @param key the associated key.
     */
    protected void releaseUpdateState(EntryUpdateState state, String key) {
        synchronized (updateStates) {
        	int usageCounter = state.decrementUsageCounter();
        	checkEntryStateUpdateUsage(key, state, usageCounter);
        }    	
    }
    
    /**
     * Completely clears the cache.
     */
    protected void clear() {
        cacheMap.clear();
    }

    /**
     * Removes the update state for the specified key and notifies any other
     * threads that are waiting on this object. This is called automatically
     * by the {@link #putInCache} method, so it is possible that no EntryUpdateState was hold
     * when this method is called.
     *
     * @param key The cache key that is no longer being updated.
     */
    protected void completeUpdate(String key) {
        EntryUpdateState state;

        synchronized (updateStates) {
            state = (EntryUpdateState) updateStates.get(key);

            if (state != null) {
                synchronized (state) {
                    int usageCounter = state.completeUpdate();
                    state.notifyAll();
                    
                	checkEntryStateUpdateUsage(key, state, usageCounter);

                }
            } else {
            	//If putInCache() was called directly (i.e. not as a result of a NeedRefreshException) then no EntryUpdateState would be found. 
           	}
        }
    }

    /**
     * Completely removes a cache entry from the cache and its associated cache
     * groups.
     *
     * @param key The key of the entry to remove.
     */
    public void removeEntry(String key) {
        removeEntry(key, null);
    }

    /**
     * Completely removes a cache entry from the cache and its associated cache
     * groups.
     *
     * @param key    The key of the entry to remove.
     * @param origin The origin of this remove request.
     */
    protected void removeEntry(String key, String origin) {
        CacheEntry cacheEntry = (CacheEntry) cacheMap.get(key);
        cacheMap.remove(key);

        if (listenerList.getListenerCount() > 0) {
            CacheEntryEvent event = new CacheEntryEvent(this, cacheEntry, origin);
            dispatchCacheEntryEvent(CacheEntryEventType.ENTRY_REMOVED, event);
        }
    }

    /**
     * Dispatch a cache entry event to all registered listeners.
     *
     * @param eventType   The type of event (used to branch on the proper method)
     * @param event       The event that was fired
     */
    private void dispatchCacheEntryEvent(CacheEntryEventType eventType, CacheEntryEvent event) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i+1] instanceof CacheEntryEventListener) {
                CacheEntryEventListener listener = (CacheEntryEventListener) listeners[i+1];
                if (eventType.equals(CacheEntryEventType.ENTRY_ADDED)) {
                    listener.cacheEntryAdded(event);
                } else if (eventType.equals(CacheEntryEventType.ENTRY_UPDATED)) {
                    listener.cacheEntryUpdated(event);
                } else if (eventType.equals(CacheEntryEventType.ENTRY_FLUSHED)) {
                    listener.cacheEntryFlushed(event);
                } else if (eventType.equals(CacheEntryEventType.ENTRY_REMOVED)) {
                    listener.cacheEntryRemoved(event);
                }
            }
        }
    }

    /**
     * Dispatch a cache group event to all registered listeners.
     *
     * @param eventType The type of event (this is used to branch to the correct method handler)
     * @param group     The cache group that the event applies to
     * @param origin      The origin of this event (optional)
     */
    private void dispatchCacheGroupEvent(CacheEntryEventType eventType, String group, String origin) {
        CacheGroupEvent event = new CacheGroupEvent(this, group, origin);

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i+1] instanceof CacheEntryEventListener) {
                CacheEntryEventListener listener = (CacheEntryEventListener) listeners[i + 1];
                if (eventType.equals(CacheEntryEventType.GROUP_FLUSHED)) {
                    listener.cacheGroupFlushed(event);
                }
            }
        }
    }

    /**
     * Dispatch a cache map access event to all registered listeners.
     *
     * @param eventType     The type of event
     * @param entry         The entry that was affected.
     * @param origin        The origin of this event (optional)
     */
    private void dispatchCacheMapAccessEvent(CacheMapAccessEventType eventType, CacheEntry entry, String origin) {
        CacheMapAccessEvent event = new CacheMapAccessEvent(eventType, entry, origin);

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i+1] instanceof CacheMapAccessEventListener) {
                CacheMapAccessEventListener listener = (CacheMapAccessEventListener) listeners[i + 1];
                listener.accessed(event);
            }
        }
    }

    /**
     * Dispatch a cache pattern event to all registered listeners.
     *
     * @param eventType The type of event (this is used to branch to the correct method handler)
     * @param pattern     The cache pattern that the event applies to
     * @param origin      The origin of this event (optional)
     */
    private void dispatchCachePatternEvent(CacheEntryEventType eventType, String pattern, String origin) {
        CachePatternEvent event = new CachePatternEvent(this, pattern, origin);

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i+1] instanceof CacheEntryEventListener) {
                if (eventType.equals(CacheEntryEventType.PATTERN_FLUSHED)) {
                    CacheEntryEventListener listener = (CacheEntryEventListener) listeners[i+1];
                    listener.cachePatternFlushed(event);
                }
            }
        }
    }

    /**
     * Dispatches a cache-wide event to all registered listeners.
     *
     * @param eventType The type of event (this is used to branch to the correct method handler)
     * @param origin The origin of this event (optional)
     */
    private void dispatchCachewideEvent(CachewideEventType eventType, Date date, String origin) {
        CachewideEvent event = new CachewideEvent(this, date, origin);

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] instanceof CacheEntryEventListener) {
                if (eventType.equals(CachewideEventType.CACHE_FLUSHED)) {
                    CacheEntryEventListener listener = (CacheEntryEventListener) listeners[i+1];
                    listener.cacheFlushed(event);
                }
            }
        }
    }

    /**
     * Flush a cache entry. On completion of the flush, a
     * <tt>CacheEntryEventType.ENTRY_FLUSHED</tt> event is fired.
     *
     * @param entry The entry to flush
     * @param origin The origin of this flush event (optional)
     */
    private void flushEntry(CacheEntry entry, String origin) {
        String key = entry.getKey();

        // Flush the object itself
        entry.flush();

        if (!entry.isNew()) {
            // Update the entry's state in the map
            cacheMap.put(key, entry);
        }

        // Trigger an ENTRY_FLUSHED event. [CACHE-107] Do this for all flushes.
        if (listenerList.getListenerCount() > 0) {
            CacheEntryEvent event = new CacheEntryEvent(this, entry, origin);
            dispatchCacheEntryEvent(CacheEntryEventType.ENTRY_FLUSHED, event);
        }
    }
    
    /**
     * @return the total number of cache entries held in this cache. 
     */
	public int getSize() {
		synchronized(cacheMap) {
			return cacheMap.size();
		}
	}

    /**
     * Test support only: return the number of EntryUpdateState instances within the updateStates map. 
     */
    protected int getNbUpdateState() {
    	synchronized(updateStates) {
    		return updateStates.size();
    	}
    }
    
    
    /**
     * Test support only: return the number of entries currently in the cache map
     * @deprecated use getSize() 
     */
	public int getNbEntries() {
		synchronized(cacheMap) {
			return cacheMap.size();
		}
	}
}
