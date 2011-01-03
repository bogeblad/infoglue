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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.contenttool.actions.ViewMultiSelectContentTreeForServiceBindingAction;
import org.infoglue.cms.applications.databeans.AssetKeyDefinition;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.util.CmsPropertyHandler;

import webwork.action.ActionContext;
import webwork.multipart.MultiPartRequestWrapper;


public class CreateContentWizardInputAssetsAction extends CreateContentWizardAbstractAction
{
    private final static Logger logger = Logger.getLogger(CreateContentWizardInputAssetsAction.class.getName());

	private String mandatoryAssetKey						= null;
	private String mandatoryAssetMaximumSize				= null;
	private String digitalAssetKey   						= "";
	private Integer uploadedFilesCounter 					= new Integer(0);
	private ContentTypeDefinitionVO contentTypeDefinitionVO	= null;
	private Integer languageId 								= null;
	private Integer contentVersionId 						= null;
	private String inputMoreAssets							= null;
	
	private VisualFormatter formatter = new VisualFormatter();

    public CreateContentWizardInputAssetsAction()
    {
    }
        	
    public String doInput() throws Exception
    {
		CreateContentWizardInfoBean createContentWizardInfoBean = this.getCreateContentWizardInfoBean();
        this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(createContentWizardInfoBean.getContentTypeDefinitionId());
		    
		List assetKeys = ContentTypeDefinitionController.getController().getDefinedAssetKeys(this.contentTypeDefinitionVO, true);
		
		if(this.languageId == null)
		{
			this.languageId = createContentWizardInfoBean.getLanguageId();
			if(this.languageId == null)
			{
				LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(createContentWizardInfoBean.getRepositoryId());
				this.languageId = masterLanguageVO.getLanguageId();
			}
		}

		if(this.contentVersionId == null)
		{
			ContentVersionVO newContentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(createContentWizardInfoBean.getContentVO().getId(), languageId);
			this.contentVersionId = newContentVersion.getId();
		}

		boolean hasMandatoryAssets = false;
		boolean missingAsset = false;
		Iterator assetKeysIterator = assetKeys.iterator();
		while(assetKeysIterator.hasNext())
		{
			AssetKeyDefinition assetKeyDefinition = (AssetKeyDefinition)assetKeysIterator.next();
			if(assetKeyDefinition.getIsMandatory().booleanValue())
			{
				hasMandatoryAssets = true;
				DigitalAssetVO asset = DigitalAssetController.getController().getDigitalAssetVO(createContentWizardInfoBean.getContentVO().getId(), languageId, assetKeyDefinition.getAssetKey(), false);
				if(asset == null)
				{
					mandatoryAssetKey = assetKeyDefinition.getAssetKey();
					mandatoryAssetMaximumSize = "" + assetKeyDefinition.getMaximumSize();
					missingAsset = true;
					break;
				}
			}
		}

		if(!hasMandatoryAssets && !inputMoreAssets.equalsIgnoreCase("false"))
			inputMoreAssets = "true";

		if(missingAsset)
		{
			inputMoreAssets = "false";
			return "input";
		}
		else
		{
			if(inputMoreAssets != null && inputMoreAssets.equalsIgnoreCase("true"))
			{
				return "input";				
			}
			else
			{
	    		return "success";
			}
		}

    }

	public String doExecute() throws Exception
	{
		InputStream is = null;
		File renamedFile = null;
		DigitalAssetVO digitalAssetVO = null;
		
		try 
		{
			MultiPartRequestWrapper mpr = ActionContext.getContext().getMultiPartRequest();
			if(mpr != null)
			{ 
				String fromEncoding = CmsPropertyHandler.getUploadFromEncoding();
				if(fromEncoding == null)
					fromEncoding = "iso-8859-1";
				
				String toEncoding = CmsPropertyHandler.getUploadToEncoding();
				if(toEncoding == null)
					toEncoding = "utf-8";
				
				this.digitalAssetKey = new String(this.digitalAssetKey.getBytes(fromEncoding), toEncoding);

				Enumeration names = mpr.getFileNames();
				while (names.hasMoreElements()) 
				{
					String name 		  = (String)names.nextElement();
					String contentType    = mpr.getContentType(name);
					String fileSystemName = mpr.getFilesystemName(name);
					
					File file = mpr.getFile(name);
					String fileName = fileSystemName;
					//String fileName = digitalAssetKey + "_" + System.currentTimeMillis() + "_" + fileSystemName;
					//String tempFileName = "tmp_" + fileName;
					//tempFileName = new VisualFormatter().replaceNonAscii(fileName, '_');
					
					String filePath = CmsPropertyHandler.getDigitalAssetPath();
					//fileSystemName = filePath + File.separator + tempFileName;
	            	fileName = formatter.replaceNiceURINonAsciiWithSpecifiedChars(fileName, CmsPropertyHandler.getNiceURIDefaultReplacementCharacter());

					if(file != null)
					{
						DigitalAssetVO newAsset = new DigitalAssetVO();
						newAsset.setAssetContentType(contentType);
						newAsset.setAssetKey(digitalAssetKey);
						newAsset.setAssetFileName(fileName);
						newAsset.setAssetFilePath(filePath);
						newAsset.setAssetFileSize(new Integer(new Long(file.length()).intValue()));
						//is = new FileInputStream(renamedFile);
						is = new FileInputStream(file);
						//DigitalAssetController.create(newAsset, is, this.contentVersionId);
						//CreateContentWizardInfoBean createContentWizardInfoBean = this.getCreateContentWizardInfoBean();
						//createContentWizardInfoBean.getDigitalAssets().put(digitalAssetKey + "_" + this.languageId, newAsset);
					    digitalAssetVO = DigitalAssetController.create(newAsset, is, this.contentVersionId, this.getInfoGluePrincipal());
						
						this.uploadedFilesCounter = new Integer(this.uploadedFilesCounter.intValue() + 1);
					}
				}
			}
			else
			{
				logger.error("File upload failed for some reason.");
			}
		} 
		catch (Exception e) 
		{
		    logger.error("An error occurred when we tried to upload a new asset:" + e.getMessage(), e);
		}
		finally
		{
			try
			{
				is.close();
				
			    if(CmsPropertyHandler.getEnableDiskAssets().equals("true"))
				{
					//String assetFileName = "" + digitalAssetVO.getAssetFilePath() + File.separator + digitalAssetVO.getId() + "_" + digitalAssetVO.getAssetFileName();
					String folderName = "" + (digitalAssetVO.getDigitalAssetId().intValue() / 1000);
					String assetFileName = "" + digitalAssetVO.getAssetFilePath() + File.separator + folderName + File.separator + digitalAssetVO.getId() + "_" + digitalAssetVO.getAssetFileName();
					File assetFile = new File(assetFileName);
					renamedFile.renameTo(assetFile);
				}
			    else
			    {
			    	renamedFile.delete();
			    }
			}
			catch(Exception e){}
		}
		
		return doInput();
	}
    
	public String doFinish() throws Exception
	{
		return "success";
	}
	
	public void setDigitalAssetKey(String digitalAssetKey)
	{
		this.digitalAssetKey = digitalAssetKey;
	}
	
	public void setUploadedFilesCounter(Integer uploadedFilesCounter)
	{
		this.uploadedFilesCounter = uploadedFilesCounter;
	}

	public Integer getUploadedFilesCounter()
	{
		return this.uploadedFilesCounter;
	}
	   
	public List getDefinedAssetKeys()
	{
		return ContentTypeDefinitionController.getController().getDefinedAssetKeys(this.contentTypeDefinitionVO, true);
	}


	public String getMandatoryAssetKey()
	{
		return mandatoryAssetKey;
	}

	public void setMandatoryAssetKey(String string)
	{
		mandatoryAssetKey = string;
	}

	public String getMandatoryAssetMaximumSize()
	{
		return mandatoryAssetMaximumSize;
	}

	public Integer getLanguageId()
	{
		return languageId;
	}

	public void setLanguageId(Integer integer)
	{
		languageId = integer;
	}

	public Integer getContentVersionId()
	{
		return contentVersionId;
	}

	public void setContentVersionId(Integer contentVersionId)
	{
		this.contentVersionId = contentVersionId;
	}

	public String getInputMoreAssets()
	{
		return inputMoreAssets;
	}

	public void setInputMoreAssets(String inputMoreAssets)
	{
		this.inputMoreAssets = inputMoreAssets;
	}

}