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

package org.infoglue.cms.applications.contenttool.actions;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.databeans.AssetKeyDefinition;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupPropertiesController;
import org.infoglue.cms.controllers.kernel.impl.simple.RolePropertiesController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserPropertiesController;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.GroupProperties;
import org.infoglue.cms.entities.management.GroupPropertiesVO;
import org.infoglue.cms.entities.management.RoleProperties;
import org.infoglue.cms.entities.management.RolePropertiesVO;
import org.infoglue.cms.entities.management.UserProperties;
import org.infoglue.cms.entities.management.UserPropertiesVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

import webwork.action.ActionContext;
import webwork.multipart.MultiPartRequestWrapper;


/**
  * This is the action-class for UpdateDigitalAssetVersion
  * 
  * @author Mattias Bogeblad
  */

public class UpdateDigitalAssetAction extends ViewDigitalAssetAction 
{
    private final static Logger logger = Logger.getLogger(UpdateDigitalAssetAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private Integer contentVersionId = null;
	private Integer digitalAssetId   = null;
	private String digitalAssetKey   = null;
	private boolean isUpdated       = false;
	private String reasonKey;
	private String uploadMaxSize;
	private DigitalAssetVO digitalAssetVO = null;
	private DigitalAssetVO updatedDigitalAssetVO = null;
	private String closeOnLoad;
	private Integer contentTypeDefinitionId;

	private String entity;
	private Integer entityId;
	private UserPropertiesVO userPropertiesVO;
	private UserPropertiesVO rolePropertiesVO;
	private ContentVersionVO contentVersionVO;
	protected ContentTypeDefinitionVO contentTypeDefinitionVO;

	private VisualFormatter formatter = new VisualFormatter();
	
	private ConstraintExceptionBuffer ceb;
	
    public UpdateDigitalAssetAction()
    {
    	//this.digitalAssetVO = new DigitalAssetVO();
    	this.ceb = new ConstraintExceptionBuffer();
    }
        	
    public void setDigitalAssetKey(String digitalAssetKey)
	{
		this.digitalAssetKey = digitalAssetKey;
	}
		   
    public String doExecute() throws Exception
    {
    	initialize();

    	ceb.throwIfNotEmpty();
		
    	InputStream is = null;
		File file = null;
		
		try
        {
            MultiPartRequestWrapper mpr = ActionContext.getMultiPartRequest();
            if(mpr == null)
            {
                this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnSizeText";
                this.uploadMaxSize = "(Max " + formatter.formatFileSize(getUploadMaxSize()) + " - system wide)";
                return "uploadFailed";
            }
            
			String fromEncoding = CmsPropertyHandler.getUploadFromEncoding();
			if(fromEncoding == null)
				fromEncoding = "iso-8859-1";
			
			String toEncoding = CmsPropertyHandler.getUploadToEncoding();
			if(toEncoding == null)
				toEncoding = "utf-8";
			
			this.digitalAssetKey = new String(this.digitalAssetKey.getBytes(fromEncoding), toEncoding);

			DigitalAssetVO digitalAssetVO = ContentVersionController.getContentVersionController().checkStateAndChangeIfNeeded(contentVersionId, digitalAssetId, getInfoGluePrincipal());
    	    //DigitalAssetVO digitalAssetVO = DigitalAssetController.getDigitalAssetVOWithId(this.digitalAssetId);
    	    digitalAssetVO.setAssetKey(this.digitalAssetKey);

    		if(mpr != null)
    		{ 
	    		Enumeration names = mpr.getFileNames();
	         	while (names.hasMoreElements()) 
	         	{
	            	String name = (String)names.nextElement();
						            	
	            	file = mpr.getFile(name);
	            	if(file != null)
	            	{
		            	String contentType    = mpr.getContentType(name);
						String fileSystemName = mpr.getFilesystemName(name);
					
						String fileName = fileSystemName;
		            	fileName = formatter.replaceNiceURINonAsciiWithSpecifiedChars(fileName, CmsPropertyHandler.getNiceURIDefaultReplacementCharacter());
						//fileName = new VisualFormatter().replaceNonAscii(fileName, '_');

						String tempFileName = "tmp_" + System.currentTimeMillis() + "_" + fileName;
						//String filePath = file.getParentFile().getPath();
		            	String filePath = CmsPropertyHandler.getDigitalAssetPath();
		            	fileSystemName =  filePath + File.separator + tempFileName;
		            	
		            	digitalAssetVO.setAssetContentType(contentType);
						digitalAssetVO.setAssetFileName(fileName);
						digitalAssetVO.setAssetFilePath(filePath);
						digitalAssetVO.setAssetFileSize(new Integer(new Long(file.length()).intValue()));
						is = new FileInputStream(file);    	
						
						String fileUploadMaximumSize = getPrincipalPropertyValue("fileUploadMaximumSize", false, true);
						logger.info("fileUploadMaximumSize:" + fileUploadMaximumSize);
						
						if(!fileUploadMaximumSize.equalsIgnoreCase("-1") && new Integer(fileUploadMaximumSize).intValue() < new Long(file.length()).intValue())
						{
						    file.delete();
						    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnSizeText";
			                this.uploadMaxSize = "(Max " + formatter.formatFileSize(fileUploadMaximumSize) + ")";
		                	return "uploadFailed";
						}

						if(this.contentTypeDefinitionId != null && digitalAssetKey != null)
						{
							this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(this.contentTypeDefinitionId);
							AssetKeyDefinition assetKeyDefinition = ContentTypeDefinitionController.getController().getDefinedAssetKey(contentTypeDefinitionVO, true, digitalAssetKey);
							
							if(assetKeyDefinition != null)
							{
								if(assetKeyDefinition.getMaximumSize().intValue() < new Long(file.length()).intValue())
								{   
								    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnSizeText";
								    this.uploadMaxSize = "(Max " + formatter.formatFileSize(assetKeyDefinition.getMaximumSize()) + ")";
				                	return "uploadFailed";
								}
								if(assetKeyDefinition.getAllowedContentTypes().startsWith("image"))
								{
								    if(!contentType.startsWith("image"))
								    {
									    file.delete();
									    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnTypeNotImageText";
					                	return "uploadFailed";						        
								    }
		
								    Image image = javax.imageio.ImageIO.read(file);
								    int width = image.getWidth(null);
								    int height = image.getHeight(null);
								    
								    String allowedWidth = assetKeyDefinition.getImageWidth();
								    String allowedHeight = assetKeyDefinition.getImageHeight();
								    
								    if(!allowedWidth.equals("*"))
								    {
								        Integer allowedWidthNumber = new Integer(allowedWidth.substring(1));
								        if(allowedWidth.startsWith("<") && width >= allowedWidthNumber.intValue())
								        {
									        file.delete();
										    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnImageToWideText";
						                	return "uploadFailed";			
								        }
								        if(allowedWidth.startsWith(">") && width <= allowedWidthNumber.intValue())
								        {
									        file.delete();
										    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnImageNotWideEnoughText";
						                	return "uploadFailed";			
								        }
								        if(!allowedWidth.startsWith(">") && !allowedWidth.startsWith("<") && width != new Integer(allowedWidth).intValue())
								        {
								            file.delete();
										    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnImageWrongWidthText";
						                	return "uploadFailed";	
								        }
								    }
								    
								    if(!allowedHeight.equals("*"))
								    {
								        Integer allowedHeightNumber = new Integer(allowedHeight.substring(1));
								        if(allowedHeight.startsWith("<") && height >= allowedHeightNumber.intValue())
								        {
									        file.delete();
										    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnImageToHighText";
						                	return "uploadFailed";			
								        }
								        if(allowedHeight.startsWith(">") && height <= allowedHeightNumber.intValue())
								        {
									        file.delete();
										    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnImageNotHighEnoughText";
						                	return "uploadFailed";			
								        }
								        if(!allowedHeight.startsWith(">") && !allowedHeight.startsWith("<") && height != new Integer(allowedHeight).intValue())
								        {
								            file.delete();
										    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnImageWrongHeightText";
						                	return "uploadFailed";	
								        }
								    }
								}
							}
						}
	            	}
	         	}
    		}
    		else
    		{
    		    logger.error("File upload failed for some reason.");
    		}
    		
    		updatedDigitalAssetVO = DigitalAssetController.update(digitalAssetVO, is);
			isUpdated = true;

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
				file.delete();
			}
			catch(Exception e){}
		}
		
        return "success";
    }

	private void initialize() throws SystemException, Bug, ConstraintException
	{
		try
		{
			this.digitalAssetVO = DigitalAssetController.getDigitalAssetVOWithId(this.digitalAssetId);
	
	    	if(this.contentVersionId != null)
	        {
	        	this.contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId);
	            this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentVersionVO.getContentId());
	        }
	        else
	        {
	            if(this.entity.equalsIgnoreCase(UserProperties.class.getName()))
	            {
	                UserPropertiesVO userPropertiesVO = UserPropertiesController.getController().getUserPropertiesVOWithId(this.entityId);
	                this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(userPropertiesVO.getContentTypeDefinitionId());            
	            }
	            else if(this.entity.equalsIgnoreCase(RoleProperties.class.getName()))
	            {
	                RolePropertiesVO rolePropertiesVO = RolePropertiesController.getController().getRolePropertiesVOWithId(this.entityId);
	                this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(rolePropertiesVO.getContentTypeDefinitionId());            
	            }
	            else if(this.entity.equalsIgnoreCase(GroupProperties.class.getName()))
	            {
	                GroupPropertiesVO groupPropertiesVO = GroupPropertiesController.getController().getGroupPropertiesVOWithId(this.entityId);
	                this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(groupPropertiesVO.getContentTypeDefinitionId());            
	            }
	        }
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}    

	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetUrl() throws Exception
	{
		String imageHref = null;
		try
		{
       		imageHref = DigitalAssetController.getDigitalAssetUrl(updatedDigitalAssetVO.getDigitalAssetId());
		}
		catch(Exception e)
		{
		    logger.warn("We could not get the url of the digitalAsset: " + e.getMessage(), e);
		}
		
		return imageHref;
	}
	
    public String getAssetThumbnailUrl()
    {
        String imageHref = null;
		try
		{
       		imageHref = DigitalAssetController.getDigitalAssetThumbnailUrl(updatedDigitalAssetVO.getDigitalAssetId());
		}
		catch(Exception e)
		{
		    logger.warn("We could not get the url of the thumbnail: " + e.getMessage(), e);
		}
		
		return imageHref;
    }
    
	public Integer getDigitalAssetId()
	{
		return digitalAssetId;
	}

	public void setDigitalAssetId(Integer digitalAssetId)
	{
		this.digitalAssetId = digitalAssetId;
	}

	public String getDigitalAssetKey()
	{
		return digitalAssetKey;
	}

	public boolean getIsUpdated()
	{
		return isUpdated;
	}

	public Integer getContentVersionId()
	{
		return contentVersionId;
	}

	public void setContentVersionId(Integer contentVersionId)
	{
		this.contentVersionId = contentVersionId;
	}

    public String getReasonKey()
    {
        return reasonKey;
    }
    
    public String getCloseOnLoad()
    {
        return closeOnLoad;
    }
    
    public void setCloseOnLoad(String closeOnLoad)
    {
        this.closeOnLoad = closeOnLoad;
    }
    
    public void setContentTypeDefinitionId(Integer contentTypeDefinitionId)
    {
        this.contentTypeDefinitionId = contentTypeDefinitionId;
    }

    public String getUploadErrorMaxSize()
	{
		return uploadMaxSize;
	}
    
	public Integer getContentTypeDefinitionId()
	{
		return contentTypeDefinitionId;
	}

	public String getEntity()
	{
		return entity;
	}

	public Integer getEntityId()
	{
		return entityId;
	}

	public ContentTypeDefinitionVO getContentTypeDefinitionVO()
	{
		return contentTypeDefinitionVO;
	}

	public List getDefinedAssetKeys()
	{
		return ContentTypeDefinitionController.getController().getDefinedAssetKeys(this.contentTypeDefinitionVO, true);
	}
    
}
