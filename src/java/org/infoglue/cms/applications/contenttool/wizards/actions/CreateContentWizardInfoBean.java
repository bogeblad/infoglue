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

package org.infoglue.cms.applications.contenttool.wizards.actions;

import java.util.HashMap;
import java.util.Map;

import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;

/**
 * @author mattias
 *
 * This class is used to store all needed information during the entire wizard. Much nicer than to
 * put the info in the session without a wrapper.
 */

public class CreateContentWizardInfoBean
{
	private Integer parentContentId 		= null;
	private Integer repositoryId			= null;
	private Integer contentTypeDefinitionId	= null;
	private Integer languageId				= null;
	private Content content 				= new ContentImpl();
	private ContentVO contentVO 			= null;
	private Map contentVersions				= new HashMap();
	private Map digitalAssets				= new HashMap();
	private String returnAddress			= null;
	private String cancelAddress			= null;
	
	public Content getContent()
	{
		return content;
	}

	public Integer getParentContentId()
	{
		return parentContentId;
	}

	public Integer getRepositoryId()
	{
		return repositoryId;
	}

	public Integer getLanguageId()
	{
		return languageId;
	}

	public void setContent(Content content)
	{
		this.content = content;
	}

	public void setParentContentId(Integer parentContentId)
	{
		this.parentContentId = parentContentId;
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
	}

	public Integer getContentTypeDefinitionId()
	{
		return this.contentTypeDefinitionId;
	}

	public void setContentTypeDefinitionId(Integer contentTypeDefinitionId)
	{
		this.contentTypeDefinitionId = contentTypeDefinitionId;
	}

	public Map getDigitalAssets()
	{
		return this.digitalAssets;
	}

	public void setDigitalAssets(Map digitalAssets)
	{
		this.digitalAssets = digitalAssets;
	}

	public Map getContentVersions()
	{
		return this.contentVersions;
	}

	public void setContentVersions(Map contentVersions)
	{
		this.contentVersions = contentVersions;
	}

	public String getReturnAddress()
	{
		return this.returnAddress;
	}

	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}

	public ContentVO getContentVO()
	{
		return contentVO;
	}

	public void setContentVO(ContentVO contentVO)
	{
		this.contentVO = contentVO;
	}

	public String getCancelAddress()
	{
		return cancelAddress;
	}

	public void setCancelAddress(String cancelAddress)
	{
		this.cancelAddress = cancelAddress;
	}

}
