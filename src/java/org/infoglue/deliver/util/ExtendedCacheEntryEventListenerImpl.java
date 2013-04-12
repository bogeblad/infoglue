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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.infoglue.cms.entities.content.ContentVersionVO;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.base.events.CacheEntryEvent;
import com.opensymphony.oscache.base.events.CachewideEvent;
import com.opensymphony.oscache.extra.CacheEntryEventListenerImpl;

public class ExtendedCacheEntryEventListenerImpl extends CacheEntryEventListenerImpl 
{
    public final static Logger logger = Logger.getLogger(ExtendedCacheEntryEventListenerImpl.class.getName());
    
    /**
     * Handles the event fired when an entry is added in the cache.
     *
     * @param event The event triggered when a cache entry has been added
     */
    public void cacheEntryAdded(CacheEntryEvent event) 
    {
        super.cacheEntryAdded(event);
    }

    /**
     * Handles the event fired when an entry is flushed from the cache.
     *
     * @param event The event triggered when a cache entry has been flushed
     */
    public void cacheEntryFlushed(CacheEntryEvent event) {
        super.cacheEntryFlushed(event);
    }

    /**
     * Handles the event fired when an entry is removed from the cache.
     *
     * @param event The event triggered when a cache entry has been removed
     */
    public void cacheEntryRemoved(CacheEntryEvent event) {
        super.cacheEntryRemoved(event);
    }

    /**
     * Handles the event fired when an entry is updated in the cache.
     *
     * @param event The event triggered when a cache entry has been updated
     */
    public void cacheEntryUpdated(CacheEntryEvent event) {
        super.cacheEntryRemoved(event);
    }

    /**
     * Handles the event fired when a cache flush occurs.
     *
     * @param event The event triggered when an entire cache is flushed
     */
    public void cacheFlushed(CachewideEvent event) {
        super.cacheFlushed(event);
    }

    /**
     * Returns the internal values in a string form
     */
    public String toString() {
        return ("Added " + getEntryAddedCount() + ", Cache Flushed " + getCacheFlushedCount());
    }
} 