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

package org.infoglue.cms.entities.management.impl.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.infoglue.cms.entities.management.AccessRight;

public class InfoGlueExportImpl
{
	private Integer infoGlueExportId;
	private List rootContent = new ArrayList();
	private List rootSiteNode = new ArrayList();
	//private ContentImpl rootContent;
	//private SiteNodeImpl rootSiteNode;
	private Collection contentTypeDefinitions = new ArrayList();
	private Collection categories = new ArrayList();
	private Hashtable<String,String> repositoryProperties = new Hashtable<String,String>();
	private Hashtable<String,String> contentProperties = new Hashtable<String,String>();
	private Hashtable<String,String> siteNodeProperties = new Hashtable<String,String>();
	private Collection<AccessRight> accessRights = new ArrayList<AccessRight>();
	
	public Integer getInfoGlueExportId()
	{
		return infoGlueExportId;
	}

	public void setInfoGlueExportId(Integer infoGlueExportId)
	{
		this.infoGlueExportId = infoGlueExportId;
	}

	/*
	public ContentImpl getRootContent()
	{
		return rootContent;
	}

	public SiteNodeImpl getRootSiteNode()
	{
		return rootSiteNode;
	}

	public void setRootContent(ContentImpl impl)
	{
		rootContent = impl;
	}

	public void setRootSiteNode(SiteNodeImpl impl)
	{
		rootSiteNode = impl;
	}
	*/

	public List getRootContent() 
	{
		return rootContent;
	}

	public List getRootSiteNode() 
	{
		return rootSiteNode;
	}

	public Collection getContentTypeDefinitions()
	{
		return contentTypeDefinitions;
	}

	public void setContentTypeDefinitions(Collection contentTypeDefinitions)
	{
		this.contentTypeDefinitions = contentTypeDefinitions;
	}

	public Collection getCategories()
	{
		return categories;
	}

	public void setCategories(Collection categories)
	{
		this.categories = categories;
	}

	public Hashtable<String, String> getContentProperties()
	{
		return contentProperties;
	}

	public void setContentProperties(Hashtable<String, String> contentProperties)
	{
		this.contentProperties = contentProperties;
	}

	public Hashtable<String, String> getSiteNodeProperties()
	{
		return siteNodeProperties;
	}

	public void setSiteNodeProperties(Hashtable<String, String> siteNodeProperties)
	{
		this.siteNodeProperties = siteNodeProperties;
	}

	public Hashtable<String, String> getRepositoryProperties()
	{
		return repositoryProperties;
	}

	public void setRepositoryProperties(Hashtable<String, String> repositoryProperties)
	{
		this.repositoryProperties = repositoryProperties;
	}

	public Collection<AccessRight> getAccessRights()
	{
		return accessRights;
	}

	public void setAccessRights(Collection<AccessRight> accessRights)
	{
		this.accessRights = accessRights;
	}

}