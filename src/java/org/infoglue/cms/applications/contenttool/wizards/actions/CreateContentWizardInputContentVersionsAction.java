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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * This action represents the create content versions including assets in the wizards.
 */

public class CreateContentWizardInputContentVersionsAction extends CreateContentWizardAbstractAction
{
	private ContentTypeDefinitionVO contentTypeDefinitionVO = null;
	private List contentTypeAttributes						= null;
	private Integer currentEditorId 						= null;
	private Integer languageId 								= null;
	private ContentVersionVO contentVersionVO 				= new ContentVersionVO();
	private Collection digitalAssets						= new ArrayList();
	
	public CreateContentWizardInputContentVersionsAction()
	{
	}
	

	/**
	 * This method presents the user with the initial input screen for creating a content.
	 * 
	 * @return
	 * @throws Exception
	 */
	 
	public String doInput() throws Exception
	{
		CreateContentWizardInfoBean createContentWizardInfoBean = getCreateContentWizardInfoBean();
		
		Integer contentTypeDefinitionId = createContentWizardInfoBean.getContentTypeDefinitionId();
		this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentTypeDefinitionId);
		
		this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().validateAndUpdateContentType(this.contentTypeDefinitionVO);
		
		if(this.languageId == null)
		{
			this.languageId = createContentWizardInfoBean.getLanguageId();
			if(this.languageId == null)
			{			
				LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(createContentWizardInfoBean.getRepositoryId());
				this.languageId = masterLanguageVO.getLanguageId();
			}
		}
		
		if(this.contentVersionVO != null && this.contentVersionVO.getContentVersionId() != null)
       		digitalAssets = DigitalAssetController.getDigitalAssetVOList(this.contentVersionVO.getId());

		/*
		boolean missingAsset = false;
		Iterator assetKeysIterator = assetKeys.iterator();
		while(assetKeysIterator.hasNext())
		{
			AssetKeyDefinition assetKeyDefinition = (AssetKeyDefinition)assetKeysIterator.next();
			if(!createContentWizardInfoBean.getDigitalAssets().containsKey(assetKeyDefinition.getAssetKey() + "_" + masterLanguageVO.getId()))
				return "inputAssets";
		}
		*/
		
		this.contentTypeAttributes = ContentTypeDefinitionController.getController().getContentTypeAttributes(this.contentTypeDefinitionVO, true);
		
    	String wysiwygEditor = CmsPropertyHandler.getWysiwygEditor();
    	if(wysiwygEditor == null || wysiwygEditor.equalsIgnoreCase("") || wysiwygEditor.equalsIgnoreCase("HTMLArea"))
    	    return "inputContentVersions";
    	else
    	    return "inputContentVersionsForFCKEditor";
	}

	/**
	 * This method validates the input and handles any deviations.
	 * 
	 * @return
	 * @throws Exception
	 */
	 
	public String doExecute() throws Exception
	{
		CreateContentWizardInfoBean createContentWizardInfoBean = getCreateContentWizardInfoBean();
		
		this.contentVersionVO.setVersionModifier(this.getInfoGluePrincipal().getName());

		ContentVersionController.getContentVersionController().update(this.contentVersionVO.getId(), this.contentVersionVO, this.getInfoGluePrincipal());

		return "success";
	}

	public List getContentTypeAttributes()
	{
		return this.contentTypeAttributes;
	}

	public ContentTypeDefinitionVO getContentTypeDefinitionVO()
	{
		return this.contentTypeDefinitionVO;
	}

	public Integer getCurrentEditorId()
	{
		return this.currentEditorId;
	}

	public Integer getLanguageId()
	{
		return this.languageId;
	}

	public Integer getContentId()
	{
		return getCreateContentWizardInfoBean().getContentVO().getId();
	}

	public void setCurrentEditorId(Integer currentEditorId)
	{
		this.currentEditorId = currentEditorId;
	}

	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
		this.contentVersionVO.setLanguageId(languageId);
	}

	public void setVersionValue(String versionValue)
	{
		this.contentVersionVO.setVersionValue(versionValue);
	}

	public void setContentVersionId(Integer contentVersionId)
	{
		this.contentVersionVO.setContentVersionId(contentVersionId);
	}
	
	public Integer getContentVersionId()
	{
		return this.contentVersionVO.getContentVersionId();
	}


	public Collection getDigitalAssets()
	{
		return digitalAssets;
	}


}
