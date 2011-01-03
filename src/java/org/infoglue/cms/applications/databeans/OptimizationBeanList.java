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

package org.infoglue.cms.applications.databeans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.infoglue.cms.entities.content.DigitalAsset;

public class OptimizationBeanList extends ArrayList
{
	private Map contentPaths = new HashMap();
	private Set contentVersionVOList = new HashSet();
	private List digitalAssetVOList = new ArrayList();

	private int totalAssets 				= 0;
	private int totalAssetSize 				= 0;
	private int totalAssetsWithoutVersion 	= 0;

	public OptimizationBeanList()
	{
	}
	
	
	public void addEventVersions(List contentVersionVOList)
	{
		this.contentVersionVOList.addAll(contentVersionVOList);
		if(contentVersionVOList.size() == 0)
			totalAssetsWithoutVersion++;
	}
	
	
	public void addDigitalAsset(DigitalAsset digitalAsset)
	{
		totalAssets++;
		totalAssetSize = totalAssetSize + digitalAsset.getAssetFileSize().intValue();
		digitalAssetVOList.add(digitalAsset.getValueObject());
	}

	public Set getContentVersionVOList() 
	{
		return contentVersionVOList;
	}

	public int getTotalAssets() 
	{
		return totalAssets;
	}

	public int getTotalAssetSize() 
	{
		return totalAssetSize;
	}

	public int getTotalAssetSizeInMB() 
	{
		return totalAssetSize / (1000 * 1000);
	}

	public int getTotalAssetsWithoutVersion() 
	{
		return totalAssetsWithoutVersion;
	}

	public List getDigitalAssetVOList() 
	{
		return digitalAssetVOList;
	}

	public String getContentPath(Integer digitalAssetId)
	{
		return (String)contentPaths.get(digitalAssetId);
	}

	public void setContentPath(Integer digitalAssetId, String contentPath)
	{
		this.contentPaths.put(digitalAssetId, contentPath);
	}
}
