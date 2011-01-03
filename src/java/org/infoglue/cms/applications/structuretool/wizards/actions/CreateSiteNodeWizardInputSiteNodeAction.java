/* ===============================================================================
 *
 * Part of the InfoGlue SiteNode Management Platform (www.infoglue.org)
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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.structuretool.actions.CreateSiteNodeAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.PageTemplateController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeTypeDefinitionController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.sorters.ReflectionComparator;

/**
 * This action represents the create SiteNode step in the wizards.
 */

public class CreateSiteNodeWizardInputSiteNodeAction extends CreateSiteNodeWizardAbstractAction
{
    private final static Logger logger = Logger.getLogger(CreateSiteNodeWizardInputSiteNodeAction.class.getName());

	private static final long serialVersionUID = 1L;

	private String returnAddress;
	private SiteNodeVO siteNodeVO 				= new SiteNodeVO();;
	private Integer siteNodeTypeDefinitionId 	= null;
	
	private ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
	
	/**
	 * This method presents the user with the initial input screen for creating a SiteNode.
	 * 
	 * @return
	 * @throws Exception
	 */
	 
	public String doInput() throws Exception
	{
		return "input";
	}

	/**
	 * This method validates the input and handles any deviations.
	 * 
	 * @return
	 * @throws Exception
	 */
	 
	public String doExecute() throws Exception
	{
		this.siteNodeVO.setCreatorName(this.getInfoGluePrincipal().getName());

		ceb = this.siteNodeVO.validate();
		
		ceb.throwIfNotEmpty();

		this.getCreateSiteNodeWizardInfoBean().setSiteNodeVO(this.siteNodeVO);

		return "success";
	}

	/**
	 * This method returns the contents that are of contentTypeDefinition "PageTemplate" sorted on the property given.
	 */
	
	public List getSortedPageTemplates(String sortProperty) throws Exception
	{
		SiteNodeVO parentSiteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(this.getCreateSiteNodeWizardInfoBean().getParentSiteNodeId());
		LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(parentSiteNodeVO.getRepositoryId());

		List components = PageTemplateController.getController().getPageTemplates(this.getInfoGluePrincipal(), masterLanguageVO.getId());
		
		Collections.sort(components, new ReflectionComparator(sortProperty));
		
		return components;
	}
		
	
	/**
	 * This method fetches an url to the asset for the component.
	 */
	
	public String getDigitalAssetUrl(Integer contentId, String key) throws Exception
	{
		String imageHref = null;
		try
		{
			LanguageVO masterLanguage = LanguageController.getController().getMasterLanguage(ContentController.getContentController().getContentVOWithId(contentId).getRepositoryId());
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, masterLanguage.getId());
			List digitalAssets = DigitalAssetController.getDigitalAssetVOList(contentVersionVO.getId());
			Iterator i = digitalAssets.iterator();
			while(i.hasNext())
			{
				DigitalAssetVO digitalAssetVO = (DigitalAssetVO)i.next();
				if(digitalAssetVO.getAssetKey().equals(key))
				{
					imageHref = DigitalAssetController.getDigitalAssetUrl(digitalAssetVO.getId()); 
					break;
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the digitalAsset: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}
	
	/**
	 * This method fetches the list of SiteNodeTypeDefinitions
	 */
	
	public List getSiteNodeTypeDefinitions() throws Exception
	{
		return SiteNodeTypeDefinitionController.getController().getSortedSiteNodeTypeDefinitionVOList();
	}      

	public java.lang.String getName()
	{
		return this.siteNodeVO.getName();
	}

	public void setName(String name)
	{
		this.siteNodeVO.setName(name);
	}

	public String getPublishDateTime()
	{    		
		return new VisualFormatter().formatDate(this.siteNodeVO.getPublishDateTime(), "yyyy-MM-dd HH:mm");
	}

	public void setPublishDateTime(String publishDateTime)
	{
		this.siteNodeVO.setPublishDateTime(new VisualFormatter().parseDate(publishDateTime, "yyyy-MM-dd HH:mm"));
	}
        
	public String getExpireDateTime()
	{
		return new VisualFormatter().formatDate(this.siteNodeVO.getExpireDateTime(), "yyyy-MM-dd HH:mm");
	}

	public void setExpireDateTime(String expireDateTime)
	{
		this.siteNodeVO.setExpireDateTime(new VisualFormatter().parseDate(expireDateTime, "yyyy-MM-dd HH:mm"));
	}

	public long getPublishDateTimeAsLong()
	{    		
		return this.siteNodeVO.getPublishDateTime().getTime();
	}
        
	public long getExpireDateTimeAsLong()
	{
		return this.siteNodeVO.getExpireDateTime().getTime();
	}
    
	public Integer getSiteNodeTypeDefinitionId()
	{
		return this.siteNodeTypeDefinitionId;
	}

	public void setSiteNodeTypeDefinitionId(Integer siteNodeTypeDefinitionId)
	{
		this.siteNodeTypeDefinitionId = siteNodeTypeDefinitionId;
	}
	
	public void setPageTemplateContentId(Integer pageTemplateContentId)
	{
		this.getCreateSiteNodeWizardInfoBean().setPageTemplateContentId(pageTemplateContentId);
	}

	public Integer getPageTemplateContentId()
	{
		return this.getCreateSiteNodeWizardInfoBean().getPageTemplateContentId();
	}

	public String getReturnAddress()
	{
		return returnAddress;
	}

	public void setReturnAddress(String string)
	{
		returnAddress = string;
	}

}
