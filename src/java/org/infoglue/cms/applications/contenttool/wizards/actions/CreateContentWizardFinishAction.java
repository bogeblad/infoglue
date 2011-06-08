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

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.contenttool.actions.ViewContentVersionAction;
import org.infoglue.cms.applications.databeans.AssetKeyDefinition;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;


/**
 * This action represents the last step in the create content wizard. It creates the content and does all other neccessairy steps
 * defined by the requestor.
 */

public class CreateContentWizardFinishAction extends CreateContentWizardAbstractAction
{
    private final static Logger logger = Logger.getLogger(CreateContentWizardFinishAction.class.getName());

	private ConstraintExceptionBuffer ceb 		= null;
	private String returnAddress 				= "CreateContentWizardFinish!V3.action";
	private Integer contentId					= null;
	private Integer contentVersionId 			= null;
	private String versionDone 					= null;
	private String mandatoryAssetKey			= null;
	private String mandatoryAssetMaximumSize	= null;
	private String inputMoreAssets	 			= null;
	
	public CreateContentWizardFinishAction()
	{
		this.ceb = new ConstraintExceptionBuffer();			
	}
	
	
	public String doExecute() throws Exception
	{
		try
		{
			CreateContentWizardInfoBean createContentWizardInfoBean = getCreateContentWizardInfoBean();
			if(createContentWizardInfoBean.getParentContentId() == null)
			{
				return "stateLocation";
			}
	
			createContentWizardInfoBean.getContent().setCreator(this.getInfoGluePrincipal().getName());
			this.ceb = createContentWizardInfoBean.getContent().getValueObject().validate();
			
			if(!this.ceb.isEmpty())
			{
				return "inputContent";
			}

			Integer repositoryId = createContentWizardInfoBean.getRepositoryId();
			Integer languageId = createContentWizardInfoBean.getLanguageId();
			if(languageId == null)
				languageId = LanguageController.getController().getMasterLanguage(repositoryId).getId();
			
			if(createContentWizardInfoBean.getContentVersions().size() == 0)
			{
				String versionValue = "<?xml version='1.0' encoding='UTF-8'?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes></attributes></article>";
	
				ContentVersionVO initialContentVersionVO = new ContentVersionVO();
				initialContentVersionVO.setVersionComment("Preversion");
				initialContentVersionVO.setVersionModifier(this.getInfoGluePrincipal().getName());
				initialContentVersionVO.setVersionValue(versionValue);
				
				createContentWizardInfoBean.getContentVersions().put(languageId, initialContentVersionVO);
	
		    	ContentVO contentVO = ContentControllerProxy.getController().acCreate(this.getInfoGluePrincipal(), createContentWizardInfoBean);
				this.contentId = contentVO.getContentId();
				createContentWizardInfoBean.setContentVO(contentVO);
			
				ContentVersionVO newContentVersion = (ContentVersionVO)createContentWizardInfoBean.getContentVersions().get(languageId);
				this.contentVersionId = newContentVersion.getId();
			}
			
			ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(createContentWizardInfoBean.getContentTypeDefinitionId());
			List assetKeys = ContentTypeDefinitionController.getController().getDefinedAssetKeys(contentTypeDefinitionVO, true);
			
			Iterator assetKeysIterator = assetKeys.iterator();
			while(assetKeysIterator.hasNext())
			{
				AssetKeyDefinition assetKeyDefinition = (AssetKeyDefinition)assetKeysIterator.next();
				if(assetKeyDefinition.getIsMandatory().booleanValue())
				{
					DigitalAssetVO asset = DigitalAssetController.getController().getDigitalAssetVO(createContentWizardInfoBean.getContentVO().getId(), languageId, assetKeyDefinition.getAssetKey(), false);
					if(asset == null)
					{
						mandatoryAssetKey = assetKeyDefinition.getAssetKey();
						mandatoryAssetMaximumSize = "" + assetKeyDefinition.getMaximumSize();
						return "inputAssets";
					}
				}
			}

			if(inputMoreAssets == null && (versionDone == null || !versionDone.equalsIgnoreCase("true")))
				inputMoreAssets = "true";
			
			if(inputMoreAssets != null && inputMoreAssets.equalsIgnoreCase("true"))
			{
				return "inputAssets";
			}
			
			if(versionDone == null || versionDone.equals("false"))
			{
				String wysiwygEditor = CmsPropertyHandler.getWysiwygEditor();
		    	if(wysiwygEditor == null || wysiwygEditor.equalsIgnoreCase("") || wysiwygEditor.equalsIgnoreCase("HTMLArea"))
		    	    return "inputContentVersions";
		    	else
		    	    return "inputContentVersionsForFCKEditor";
			}
			//ceb.throwIfNotEmpty();
	    						
			String returnAddress = createContentWizardInfoBean.getReturnAddress();
			returnAddress = returnAddress.replaceAll("#entityId", createContentWizardInfoBean.getContentVO().getId().toString());
			returnAddress = returnAddress.replaceAll("#path", createContentWizardInfoBean.getContentVO().getName());
			
			this.invalidateCreateContentWizardInfoBean();
			
			this.getResponse().sendRedirect(returnAddress);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return NONE;
	}

	public String doCancel() throws Exception
	{
		try
		{
			CreateContentWizardInfoBean createContentWizardInfoBean = getCreateContentWizardInfoBean();
			
			String cancelAddress = createContentWizardInfoBean.getCancelAddress();
			if(createContentWizardInfoBean.getContentVO() != null)
			{
				ContentControllerProxy.getController().acDelete(this.getInfoGluePrincipal(), createContentWizardInfoBean.getContentVO());
			}
			
			this.invalidateCreateContentWizardInfoBean();
		
			this.getResponse().sendRedirect(cancelAddress);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return NONE;
	}

	
	public String doV3() throws Exception
	{
		try
		{
			CreateContentWizardInfoBean createContentWizardInfoBean = getCreateContentWizardInfoBean();
			if(createContentWizardInfoBean.getParentContentId() == null)
			{
				return "stateLocation";
			}
	
			createContentWizardInfoBean.getContent().setCreator(this.getInfoGluePrincipal().getName());
			this.ceb = createContentWizardInfoBean.getContent().getValueObject().validate();
			
			if(!this.ceb.isEmpty())
			{
				return "inputContent";
			}

			Integer repositoryId = createContentWizardInfoBean.getRepositoryId();
			Integer languageId = createContentWizardInfoBean.getLanguageId();
			if(languageId == null)
				languageId = LanguageController.getController().getMasterLanguage(repositoryId).getId();
			
			if(createContentWizardInfoBean.getContentVersions().size() == 0)
			{
				String versionValue = "<?xml version='1.0' encoding='UTF-8'?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes></attributes></article>";
	
				ContentVersionVO initialContentVersionVO = new ContentVersionVO();
				initialContentVersionVO.setVersionComment("Preversion");
				initialContentVersionVO.setVersionModifier(this.getInfoGluePrincipal().getName());
				initialContentVersionVO.setVersionValue(versionValue);
				
				createContentWizardInfoBean.getContentVersions().put(languageId, initialContentVersionVO);
	
		    	ContentVO contentVO = ContentControllerProxy.getController().acCreate(this.getInfoGluePrincipal(), createContentWizardInfoBean);
				this.contentId = contentVO.getContentId();
				createContentWizardInfoBean.setContentVO(contentVO);
			
				ContentVersionVO newContentVersion = (ContentVersionVO)createContentWizardInfoBean.getContentVersions().get(languageId);
				this.contentVersionId = newContentVersion.getId();
			}
			
			String returnAddress = createContentWizardInfoBean.getReturnAddress();
			returnAddress = returnAddress.replaceAll("#entityId", createContentWizardInfoBean.getContentVO().getId().toString());
			returnAddress = returnAddress.replaceAll("#path", createContentWizardInfoBean.getContentVO().getName());
			createContentWizardInfoBean.setReturnAddress(returnAddress);
			logger.info("returnAddress:" + returnAddress);

			if(versionDone == null || versionDone.equals("false"))
			{
			    return "inputContentVersionsForFCKEditor";
			}
								
			//String returnAddress = createContentWizardInfoBean.getReturnAddress();
			//returnAddress = returnAddress.replaceAll("#entityId", createContentWizardInfoBean.getContentVO().getId().toString());
			//returnAddress = returnAddress.replaceAll("#path", createContentWizardInfoBean.getContentVO().getName());
			
			this.invalidateCreateContentWizardInfoBean();
			
			this.getResponse().sendRedirect(returnAddress);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return NONE;
	}

	public String doCancelV3() throws Exception
	{
		try
		{
			CreateContentWizardInfoBean createContentWizardInfoBean = getCreateContentWizardInfoBean();
			
			String cancelAddress = createContentWizardInfoBean.getCancelAddress();
			if(createContentWizardInfoBean.getContentVO() != null)
			{
				ContentControllerProxy.getController().acDelete(this.getInfoGluePrincipal(), createContentWizardInfoBean.getContentVO());
			}
			
			this.invalidateCreateContentWizardInfoBean();
		
			this.getResponse().sendRedirect(cancelAddress);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return NONE;
	}
	
	public void setParentContentId(Integer parentContentId)
	{
		getCreateContentWizardInfoBean().setParentContentId(parentContentId);
	}

	public Integer getParentContentId()
	{
		return getCreateContentWizardInfoBean().getParentContentId();
	}

	public void setRepositoryId(Integer repositoryId)
	{
		getCreateContentWizardInfoBean().setRepositoryId(repositoryId);
	}

	public Integer getRepositoryId() 
	{
		return getCreateContentWizardInfoBean().getRepositoryId();
	}

	public void setLanguageId(Integer languageId)
	{
		getCreateContentWizardInfoBean().setLanguageId(languageId);
	}

	public Integer getLanguageId() 
	{
		return getCreateContentWizardInfoBean().getLanguageId();
	}

	public void setContentTypeDefinitionId(Integer contentTypeDefinitionId)
	{
		getCreateContentWizardInfoBean().setContentTypeDefinitionId(contentTypeDefinitionId);
	}

	public Integer getContentTypeDefinitionId()
	{
		return getCreateContentWizardInfoBean().getContentTypeDefinitionId();
	}	
	
	public java.lang.String getName()
	{
		return getCreateContentWizardInfoBean().getContent().getName();
	}

	public String getPublishDateTime()
	{    		
		return new VisualFormatter().formatDate(getCreateContentWizardInfoBean().getContent().getPublishDateTime(), "yyyy-MM-dd HH:mm");
	}
        
	public String getExpireDateTime()
	{
		return new VisualFormatter().formatDate(getCreateContentWizardInfoBean().getContent().getExpireDateTime(), "yyyy-MM-dd HH:mm");
	}

	public Boolean getIsBranch()
	{
		return getCreateContentWizardInfoBean().getContent().getIsBranch();
	}    
            
	public void setName(String name)
	{
		getCreateContentWizardInfoBean().getContent().setName(name);
	}
    	
	public void setPublishDateTime(String publishDateTime)
	{
		getCreateContentWizardInfoBean().getContent().setPublishDateTime(new VisualFormatter().parseDate(publishDateTime, "yyyy-MM-dd HH:mm"));
	}

	public void setExpireDateTime(String expireDateTime)
	{
		getCreateContentWizardInfoBean().getContent().setExpireDateTime(new VisualFormatter().parseDate(expireDateTime, "yyyy-MM-dd HH:mm"));
	}
 
	public void setIsBranch(Boolean isBranch)
	{
		getCreateContentWizardInfoBean().getContent().setIsBranch(isBranch);
	}

	public ContentVO getContentVO()
	{
		return getCreateContentWizardInfoBean().getContent().getValueObject();
	}

	public void setContentVO(ContentVO contentVO)
	{
		getCreateContentWizardInfoBean().getContent().setValueObject(contentVO);
	}

	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}
	
	public String getReturnAddress()
	{
		return returnAddress;
	}

	public void setRefreshAddress(String refreshAddress)
	{
		getCreateContentWizardInfoBean().setReturnAddress(refreshAddress);
	}
	
	public String getRefreshAddress()
	{
		return getCreateContentWizardInfoBean().getReturnAddress();
	}

	public String getEncodedRefreshAddress() throws Exception
	{
		return URLEncoder.encode(getCreateContentWizardInfoBean().getReturnAddress(), "utf-8");
	}

	public Integer getContentId()
	{
		return this.contentId;
	}


	public Integer getContentVersionId()
	{
		return contentVersionId;
	}


	public void setContentVersionId(Integer contentVersionId)
	{
		this.contentVersionId = contentVersionId;
	}


	public String getVersionDone()
	{
		return versionDone;
	}


	public void setVersionDone(String versionDone)
	{
		this.versionDone = versionDone;
	}


	public String getInputMoreAssets()
	{
		return inputMoreAssets;
	}


	public void setInputMoreAssets(String inputMoreAssets)
	{
		this.inputMoreAssets = inputMoreAssets;
	}


	public String getMandatoryAssetKey()
	{
		return mandatoryAssetKey;
	}


	public void setMandatoryAssetKey(String mandatoryAssetKey)
	{
		this.mandatoryAssetKey = mandatoryAssetKey;
	}

	public String getMandatoryAssetMaximumSize()
	{
		return mandatoryAssetMaximumSize;
	}

}
