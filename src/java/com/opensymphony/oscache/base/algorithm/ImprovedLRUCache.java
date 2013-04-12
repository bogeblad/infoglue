/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.algorithm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infoglue.deliver.cache.PageCacheHelper;

import java.util.*;
//import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>LRU (Least Recently Used) algorithm for the cache.</p>
 *
 * <p>Since release 2.3 this class requires Java 1.4 
 * to use the <code>LinkedHashSet</code>. Use prior OSCache release which
 * require the Jakarta commons-collections <code>SequencedHashMap</code>
 * class or the <code>LinkedList</code> class if neither of the above
 * classes are available.</p>
 *
 * <p>No synchronization is required in this class since the
 * <code>AbstractConcurrentReadCache</code> already takes care of any
 * synchronization requirements.</p>
 *
 * @version        $Revision: 1.2.4.4 $
 * @author <a href="mailto:salaman@teknos.com">Victor Salaman</a>
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public class ImprovedLRUCache extends AbstractConcurrentReadCache //implements Runnable
{
    private static final Log log = LogFactory.getLog(ImprovedLRUCache.class);
    //private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    private boolean isPageCache = false;
    
    /**
     * Cache queue containing all cache keys.
     */
    protected Collection list = new LinkedHashSet();

    /**
     * A flag indicating whether there is a removal operation in progress.
     */
    private volatile boolean removeInProgress = false;

    /**
     * Constructs an LRU Cache.
     */
    public ImprovedLRUCache() {
        super();
    }

    /**
     * Constructors a LRU Cache of the specified capacity.
     *
     * @param capacity The maximum cache capacity.
     */
    public ImprovedLRUCache(int capacity) {
        this();
        maxEntries = capacity;
    }
    
        
    /**
     * An item was retrieved from the list. The LRU implementation moves
     * the retrieved item's key to the front of the list.
     *
     * @param key The cache key of the item that was retrieved.
     */
    protected void itemRetrieved(Object key) {

    	// Prevent list operations during remove
        while (removeInProgress) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ie) {
            }
        }

        // We need to synchronize here because AbstractConcurrentReadCache
        // doesn't prevent multiple threads from calling this method simultaneously.
        synchronized (list) {
	    	list.remove(key);
	    	list.add(key);
        }
        /*
    	boolean lockSuccess = false;
		try {
			lockSuccess = lock.writeLock().tryLock(50, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e1) {
			return;
		}
        try
        {
        	if(!lockSuccess)
        	{
            	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Returned in ImprovedLRUCache because of lock timeout...", 1);
        		return;
        	}
        	
        	list.remove(key);
            list.add(key);
        }
        finally
        {
        	if(lockSuccess)
        		lock.writeLock().unlock();
        }
        */
    }

    /**
     * An object was put in the cache. This implementation adds/moves the
     * key to the end of the list.
     *
     * @param key The cache key of the item that was put.
     */
    protected void itemPut(Object key) {
    	
        // Since this entry was just accessed, move it to the back of the list.
    	
    	synchronized (list) { // A further fix for CACHE-44
    		list.remove(key);
            list.add(key);
        }
    	
    	/*
    	boolean lockSuccess = false;
		try {
			lockSuccess = lock.writeLock().tryLock(50, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e1) {
			return;
		}
        try
        {
        	if(!lockSuccess)
        	{
            	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Returned in ImprovedLRUCache because of lock timeout...", 1);
        		return;
        	}
        	
    		list.remove(key);
            list.add(key);
        }
        finally
        {
        	if(lockSuccess)
        		lock.writeLock().unlock();
        }
		*/
    }

    /**
     * An item needs to be removed from the cache. The LRU implementation
     * removes the first element in the list (ie, the item that was least-recently
     * accessed).
     *
     * @return The key of whichever item was removed.
     */
    protected Object removeItem() {
    	//logger.info("this.getGroupsForReading().size():" + this.getGroupsForReading().size());
    	/*
    	if(this.getGroupsForReading().size() > maxGroups)
    		maxGroups = this.getGroupsForReading().size();
		*/
    	
        Object toRemove = null;

        removeInProgress = true;
        try {
        	while (toRemove == null) {
                try {
                    toRemove = removeFirst();
                } catch (Exception e) {
                    // List is empty.
                    // this is theorically possible if we have more than the size concurrent
                    // thread in getItem. Remove completed but add not done yet.
                    // We simply wait for add to complete.
                    do {
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException ie) {
                        }
                    } while (list.isEmpty());
                }
        	}
        } finally {
            removeInProgress = false;
        }
        
        return toRemove;
    }

    /**
     * Remove specified key since that object has been removed from the cache.
     *
     * @param key The cache key of the item that was removed.
     */
    protected void itemRemoved(Object key) 
    {
    	if(isPageCache && key != null)
    	{
    		//Object fileName = this.get(key);
    		//if(fileName != null)
    			PageCacheHelper.getInstance().notifyKey(""+key);
    		//else
    		//	System.out.println("No filename found for : " + key);
    		//PageCacheHelper.getInstance().clearPageCacheInThread("" + key);
    	}
    	
        list.remove(key); 
        if(size() == 0)
        {
            groups.clear();
            this.list.clear();
        }
    }

    /**
     * Removes the first object from the list of keys.
     *
     * @return the object that was removed
     */
    
    private Object removeFirst() {
    	Object toRemove = null;

    	synchronized (list) { // A further fix for CACHE-44 and CACHE-246

        	Iterator it = list.iterator();
        	toRemove = it.next();
        	it.remove();
        	this.remove(toRemove);
    	}

    	/*
    	boolean lockSuccess = false;
		try {
			lockSuccess = lock.writeLock().tryLock(50, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e1) {
			return null;
		}
        try
        {
        	if(!lockSuccess)
        	{
            	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Returned in ImprovedLRUCache.removeFirst because of lock timeout...", 1);
        		return null;
        	}
        	
        	Iterator it = list.iterator();
        	toRemove = it.next();
        	it.remove();
        	this.remove(toRemove);
        	
        	if(maxGroups > 1000 && maxGroups > (this.getGroupsForReading().size() * 5))
        	{
        		System.out.println("The group map must be made smaller:" + maxGroups);
        		HashMap newGroups = new HashMap();
        		newGroups.putAll(groups);
        		groups = newGroups;
        		maxGroups = 0;
        	}
        }
        finally
        {
        	if(lockSuccess)
        		lock.writeLock().unlock();
        }
		*/
    	
        return toRemove;
    }
    
    /**
	 * @return the cacheName
	 */
	public boolean isPageCache() 
	{
		return isPageCache;
	}

	/**
	 * @param cacheName the cacheName to set
	 */
	public void setIsPageCache(boolean isPageCache) 
	{
		this.isPageCache = isPageCache;
	}

}
