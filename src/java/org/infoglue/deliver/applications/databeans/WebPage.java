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

package org.infoglue.deliver.applications.databeans;

/**
 * This bean is a convenience storage for different kinds of collections of siteNodes.
 * To be able to return a convenient list of for example a siteNodes children we create a list
 * of WebPage-objects. Much cleaner than to iterate through the siteNodeVO:s and then ask the
 * deliveryengine again if something is not there.
 * 
 * @author Mattias Bogeblad
 */

public class WebPage
{
	private Integer siteNodeId;
	private Integer languageId;
	private Integer contentId;
	private Integer metaInfoContentId;
	//private String extraParameters;
	private String url;
	private String navigationTitle;
	private Integer sortOrder;
	private Boolean isHidden;
	
	public WebPage()
	{
	}
	
	public void setSiteNodeId(Integer siteNodeId)
	{
		this.siteNodeId = siteNodeId;
	}
	
	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
	}
	
	public void setContentId(Integer contentId)
	{
		this.contentId = contentId;
	}

	public Integer getSiteNodeId()
	{
		return this.siteNodeId;
	}

	public Integer getLanguageId()
	{
		return this.languageId;
	}

	public Integer getContentId()
	{
		return this.contentId;
	}
	
	public void setUrl(String url)
	{
		this.url = url;
	}

	public void setNavigationTitle(String navigationTitle)
	{
		this.navigationTitle = navigationTitle;
	}

	public String getUrl()
	{
		return this.url;
	}

	public String getNavigationTitle()
	{
		return this.navigationTitle;
	}

	public Integer getMetaInfoContentId()
	{
		return this.metaInfoContentId;
	}

	public void setMetaInfoContentId(Integer metaInfoContentId)
	{
		this.metaInfoContentId = metaInfoContentId;
	}

	public Integer getSortOrder()
	{
		return this.sortOrder;
	}

	public Boolean getIsHidden()
	{
		return this.isHidden;
	}

	public void setSortOrder(Integer sortOrder)
	{
		this.sortOrder = sortOrder;
	}

	public void setIsHidden(Boolean isHidden)
	{
		this.isHidden = isHidden;
	}

}