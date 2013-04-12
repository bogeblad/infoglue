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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class represents a string-pool keeping hashes of groupKey-strings as they are otherwise duplicated memorywise when OS-cache keeps it's register.
 * 
 * @author mattias.bogeblad@gmail.com
 */

public class StringPool 
{
	//A simple record of how many hits we get...
	int hits = 0;
	int hitsHashCode = 0;

	private ConcurrentMap<Integer,String> hashCodeMap = new ConcurrentHashMap<Integer,String>(20000);

	private ConcurrentMap<String,String> contentMap = new ConcurrentHashMap<String,String>(20000);
	private ConcurrentMap<String,String> contentVersionMap = new ConcurrentHashMap<String,String>(20000);
	private ConcurrentMap<String,String> siteNodeMap = new ConcurrentHashMap<String,String>(20000);
	private ConcurrentMap<String,String> siteNodeVersionMap = new ConcurrentHashMap<String,String>(20000);

	public String getCanonicalVersion(Integer type, String id) 
	{
		ConcurrentMap<String,String> map = contentMap;
		if(type == 2)
			map = contentVersionMap;
		else if(type == 3)
			map = siteNodeMap;
		else if(type == 4)
			map = siteNodeVersionMap;
		
		if (map.size() > 300000) 
		{
			//logger.warn("Many strings in the pool:" + map.size());
			map.clear();
		}

		String canon = map.get(id);
		if(canon == null)
		{
			String prefix = "content_";
			if(type == 2)
				prefix = "contentVersion_";
			if(type == 3)
				prefix = "siteNode_";
			if(type == 4)
				prefix = "siteNodeVersion_";

			canon = new StringBuilder().append(prefix).append(id).toString();
			
			map.put(id, canon);
		}
		else
			hits++;
		
		return canon;
	}

	public String getCanonicalVersion(Integer hashCode) 
	{
		if (hashCodeMap.size() > 300000) 
		{
			//logger.warn("Many strings in the pool:" + map.size());
			hashCodeMap.clear();
		}

		String canon = hashCodeMap.get(hashCode);
		if(canon == null)
		{
			canon = hashCode.toString();
			hashCodeMap.put(hashCode, canon);
		}
		else
			hitsHashCode++;
		
		return canon;
	}

	public Integer getPoolSize() 
	{
		return contentMap.size() + contentVersionMap.size() + siteNodeMap.size() + siteNodeVersionMap.size();
	}

	public Integer getHits() 
	{
		return hits;
	}

	public void clearPool() 
	{
		hits = 0;
		contentMap.clear();
		contentVersionMap.clear();
		siteNodeMap.clear();
		siteNodeVersionMap.clear();
	}

}