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

package org.infoglue.deliver.applications.filters;

import org.infoglue.deliver.util.CacheController;


/**
 * Created by IntelliJ IDEA.
 * User: lbj
 * Date: 09-02-2004
 * Time: 09:25:28
 * To change this template use Options | File Templates.
 */

public class URIMapperCache
{
    private static URIMapperCache instance = null;
    
    final String CACHE_NAME = "NavigationCache";
    
    public URIMapperCache()
    {
    }

    public synchronized static URIMapperCache getInstance()
    {
        if (instance == null) {
            instance = new URIMapperCache();
        }
        return instance;
    }

    public void clear()
    {
    }

    public Integer getCachedSiteNodeId(Integer repositoryId, String[] path, int upToIndex)
    {
        if (repositoryId == null || path == null)
            return null;
        String cacheKey = createCacheKey(repositoryId, path, upToIndex);
        return (Integer)CacheController.getCachedObject(CACHE_NAME, cacheKey);
    }

    public boolean addCachedSiteNodeId(Integer repositoryId, String[] path, int upToIndex, Integer siteNodeId)
    {
        if (repositoryId == null || path == null || siteNodeId == null)
            return false;
        String cacheKey = createCacheKey(repositoryId, path, upToIndex);
        CacheController.cacheObject(CACHE_NAME, cacheKey, siteNodeId);
        return true;
    }

    private String createCacheKey(Integer repositoryId, String[] path, int upToIndex)
    {
    	StringBuilder sb = new StringBuilder(128);
        sb.append(String.valueOf(repositoryId)).append(":/");
        for (int i=0;i < path.length && i < upToIndex ;i++) {
            sb.append(path[i].toLowerCase()).append("/");
        }
        return sb.toString();
    }
} 