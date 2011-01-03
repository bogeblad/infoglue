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

import org.apache.log4j.Logger;
import org.infoglue.deliver.util.CacheController;

public class CachingIOResultHandler implements IOResultHandler
{
    public final static Logger logger = Logger.getLogger(CachingIOResultHandler.class.getName());

	private String cacheName;
	private String cacheKey;
	private String fileCacheCharEncoding;
	private Boolean useMemoryCache;
	private Boolean useFileCacheFallback;

	public String getCacheName()
	{
		return cacheName;
	}
	public void setCacheName(String cacheName)
	{
		this.cacheName = cacheName;
	}
	public String getCacheKey()
	{
		return cacheKey;
	}
	public void setCacheKey(String cacheKey)
	{
		this.cacheKey = cacheKey;
	}
	public String getFileCacheCharEncoding()
	{
		return fileCacheCharEncoding;
	}
	public void setFileCacheCharEncoding(String fileCacheCharEncoding)
	{
		this.fileCacheCharEncoding = fileCacheCharEncoding;
	}
	public Boolean getUseFileCacheFallback()
	{
		return useFileCacheFallback;
	}
	public void setUseFileCacheFallback(Boolean useFileCacheFallback)
	{
		this.useFileCacheFallback = useFileCacheFallback;
	}
	public Boolean getMemoryCache()
	{
		return useMemoryCache;
	}
	public void setUseMemoryCache(Boolean useMemoryCache)
	{
		this.useMemoryCache = useMemoryCache;
	}

	public void handleResult(String resultData)
	{	
		if(resultData.length() < 500000)
			CacheController.cacheObjectInAdvancedCache(cacheName, cacheKey, resultData, null, false, useFileCacheFallback, useMemoryCache, fileCacheCharEncoding);
		else
		{
			logger.warn("Result data was so large it is not a good idea to store it in memory cache.. using file cache instead. Size was:" + resultData.length() + " for " + cacheKey);
			CacheController.cacheObjectInAdvancedCache(cacheName, cacheKey, resultData, null, false, true, false, fileCacheCharEncoding);
		}
	}

}
