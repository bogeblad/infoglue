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

package org.infoglue.cms.applications.structuretool.wizards.actions;

import java.util.HashMap;
import java.util.Map;

import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;

/**
 * @author mattias
 *
 * This class is used to store all needed information during the entire wizard. Much nicer than to
 * put the info in the session without a wrapper.
 */

public class CreateSiteNodeWizardInfoBean
{
	private Integer parentSiteNodeId 			= null;
	private Integer repositoryId				= null;
	private Integer siteNodeTypeDefinitionId 	= null;
	private Integer pageTemplateContentId		= null;
	private SiteNodeVO siteNodeVO 				= new SiteNodeVO();
	private String returnAddress				= null;
	private String cancelAddress				= null;
	
	public Integer getParentSiteNodeId()
	{
		return parentSiteNodeId;
	}

	public Integer getRepositoryId()
	{
		return repositoryId;
	}

	public void setParentSiteNodeId(Integer parentSiteNodeId)
	{
		this.parentSiteNodeId = parentSiteNodeId;
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public String getReturnAddress()
	{
		return this.returnAddress;
	}

	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}

	public SiteNodeVO getSiteNodeVO()
	{
		return siteNodeVO;
	}

	public void setSiteNodeVO(SiteNodeVO siteNodeVO)
	{
		this.siteNodeVO = siteNodeVO;
	}

	public String getCancelAddress()
	{
		return cancelAddress;
	}

	public void setCancelAddress(String cancelAddress)
	{
		this.cancelAddress = cancelAddress;
	}

	public Integer getSiteNodeTypeDefinitionId()
	{
		return siteNodeTypeDefinitionId;
	}

	public void setSiteNodeTypeDefinitionId(Integer siteNodeTypeDefinitionId)
	{
		this.siteNodeTypeDefinitionId = siteNodeTypeDefinitionId;
	}

	public void setPageTemplateContentId(Integer pageTemplateContentId)
	{
		this.pageTemplateContentId = pageTemplateContentId;
	}

	public Integer getPageTemplateContentId()
	{
		return this.pageTemplateContentId;
	}

}
