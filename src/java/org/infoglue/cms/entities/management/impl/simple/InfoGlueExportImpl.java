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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.ExportContentVersionImpl;
import org.infoglue.cms.entities.management.AccessRight;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;

public class InfoGlueExportImpl
{
	private Integer infoGlueExportId;
	private List rootContent = new ArrayList();
	private List rootSiteNode = new ArrayList();
	//private ContentImpl rootContent;
	//private SiteNodeImpl rootSiteNode;
	private Collection<Language> languages = new ArrayList<Language>();
	private Collection contentTypeDefinitions = new ArrayList();
	private Collection categories = new ArrayList();
	private Collection<Repository> repositories = new ArrayList<Repository>();
	private Hashtable<String,String> repositoryProperties = new Hashtable<String,String>();
	private Hashtable<String,String> contentProperties = new Hashtable<String,String>();
	private Hashtable<String,String> siteNodeProperties = new Hashtable<String,String>();
	private Collection<AccessRight> accessRights = new ArrayList<AccessRight>();
	private Collection<Content> contents = new ArrayList<Content>();
	private Collection<ExportContentVersionImpl> contentVersions = new ArrayList<ExportContentVersionImpl>();
	private Collection<SiteNode> siteNodes = new ArrayList<SiteNode>();
	private Collection<SiteNodeVersion> siteNodeVersions = new ArrayList<SiteNodeVersion>();
	private Collection<DigitalAsset> digitalAssets = new ArrayList<DigitalAsset>();
	
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

	public Collection<Language> getLanguages()
	{
		return languages;
	}

	public void setLanguages(Collection<Language> languages)
	{
		this.languages = languages;
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

	public Collection<Repository> getRepositories()
	{
		return repositories;
	}

	public void setRepositories(Collection<Repository> repositories)
	{
		this.repositories = repositories;
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

	public Collection<Content> getContents()
	{
		return this.contents;
	}

	public void setContents(Collection<Content> contents)
	{
		this.contents = contents;
	}

	public Collection<ExportContentVersionImpl> getContentVersions()
	{
		return contentVersions;
	}

	public void setContentVersions(Collection<ExportContentVersionImpl> contentVersions)
	{
		this.contentVersions = contentVersions;
	}

	public Collection<SiteNode> getSiteNodes()
	{
		return this.siteNodes;
	}

	public void setSiteNodes(Collection<SiteNode> siteNodes)
	{
		this.siteNodes = siteNodes;
	}

	public Collection<SiteNodeVersion> getSiteNodeVersions()
	{
		return siteNodeVersions;
	}

	public void setSiteNodeVersions(Collection<SiteNodeVersion> siteNodeVersions)
	{
		this.siteNodeVersions = siteNodeVersions;
	}

	public Collection<DigitalAsset> getDigitalAssets()
	{
		return digitalAssets;
	}

	public void setDigitalAssets(Collection<DigitalAsset> digitalAssets)
	{
		this.digitalAssets = digitalAssets;
	}
}