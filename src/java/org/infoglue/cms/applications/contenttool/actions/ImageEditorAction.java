/* ===============================================================================
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.AssetKeyDefinition;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.graphics.Imaging;

/**
 * @author Mattias Bogeblad
 * @version 1.0
 * @since InfoglueCMS 2.4
 * 
 */

public class ImageEditorAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ImageEditorAction.class.getName());

	private static final long serialVersionUID = 1L;

	private DigitalAssetVO digitalAssetVO = null;
	private String modifiedFileUrl = "";
	private int xpos1, ypos1, xpos2, ypos2 = 0;
	private int width, height = 0;
	private String keepRatio = "false";
	private String bestFit = "true";
	
	private Integer contentVersionId = null;
	private Integer digitalAssetId   = null;
	private String closeOnLoad;
	private ContentVersionVO contentVersionVO;
	private ContentTypeDefinitionVO contentTypeDefinitionVO;
	private String workingFileName;
	
	private String digitalAssetKey   = null;
	private boolean isUpdated       = false;
	private String reasonKey;
	//private DigitalAssetVO updatedDigitalAssetVO = null;
	private Integer contentTypeDefinitionId;
	private boolean refreshAll = false;
	
	private ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
        	
	/**
	 * 
	 */
	
    public String doExecute() throws Exception
    {
    	ceb.throwIfNotEmpty();
	
    	this.digitalAssetVO = DigitalAssetController.getDigitalAssetVOWithId(this.digitalAssetId);
    	this.contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId);
        this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentVersionVO.getContentId());

        if(this.workingFileName == null)
        {
	        String filePath = DigitalAssetController.getDigitalAssetFilePath(this.digitalAssetVO.getDigitalAssetId());
	        BufferedImage original = javax.imageio.ImageIO.read(new File(filePath));
	
	    	workingFileName = "imageEditorWK_" + System.currentTimeMillis() + "_" + this.getInfoGluePrincipal().getName().hashCode() + "_" + digitalAssetVO.getDigitalAssetId() + ".png";
	    	File outputFile = new File(getImageEditorPath() + File.separator + workingFileName);
			javax.imageio.ImageIO.write(original, "PNG", outputFile);
		}
        
    	this.modifiedFileUrl = getImageEditorBaseUrl() + workingFileName;
    	//logger.info("modifiedFileUrl:" + modifiedFileUrl);
    
        return "success";
    }    

    public String doResize() throws Exception
    {
    	ceb.throwIfNotEmpty();

    	this.digitalAssetVO = DigitalAssetController.getDigitalAssetVOWithId(this.digitalAssetId);
    	this.contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId);
        this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentVersionVO.getContentId());

    	File file = new File(getImageEditorPath() + File.separator + workingFileName);
		
    	workingFileName = "imageEditorWK_" + System.currentTimeMillis() + "_" + this.getInfoGluePrincipal().getName().hashCode() + "_" + digitalAssetVO.getDigitalAssetId() + ".png";    	
    	File outputFile = new File(getImageEditorPath() + File.separator + workingFileName);
    	outputFile.mkdirs();
    	
    	logger.info("height: " + height);
    	logger.info("width: " + width);
    	logger.info("keepRatio: " + keepRatio);
    	logger.info("bestFit: " + bestFit);
    	
    	if(keepRatio.equalsIgnoreCase("true"))
    	{
        	Imaging.resize(file, outputFile, width, height, "PNG", true);
        }
    	else //We don't support it for now but when the Imaging-class do it will kick in
        {
    		Imaging.resize(file, outputFile, width, height, "PNG", false);
    	}
    	
    	//logger.info("outputFile:" + outputFile.length());
		this.modifiedFileUrl = getImageEditorBaseUrl() + workingFileName;
		//logger.info("modifiedFileUrl:" + modifiedFileUrl);
		
        return "successResize";
    }    

    public String doCrop() throws Exception
    {
    	ceb.throwIfNotEmpty();

    	this.digitalAssetVO = DigitalAssetController.getDigitalAssetVOWithId(this.digitalAssetId);
    	this.contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId);
        this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentVersionVO.getContentId());

    	File file = new File(getImageEditorPath() + File.separator + workingFileName);
    	BufferedImage original = javax.imageio.ImageIO.read(file);

    	// create a cropped image from the original image
    	BufferedImage image = original.getSubimage(xpos1, ypos1, xpos2 - xpos1, ypos2 - ypos1);

    	//BufferedImage image = imaging.crop(original, xpos1, ypos1, xpos2 - xpos1, ypos2 - ypos1);

    	workingFileName = "imageEditorWK_" + System.currentTimeMillis() + "_" + this.getInfoGluePrincipal().getName().hashCode() + "_" + digitalAssetVO.getDigitalAssetId() + ".png";    	
    	File outputFile = new File(getImageEditorPath() + File.separator + workingFileName);
    	javax.imageio.ImageIO.write(image, "PNG", outputFile);

    	//logger.info("outputFile:" + outputFile.length());
		this.modifiedFileUrl = getImageEditorBaseUrl() + workingFileName;
		//logger.info("modifiedFileUrl:" + modifiedFileUrl);
		
        return "successCrop";
    }    

    public String doSave() throws Exception
    {
    	ceb.throwIfNotEmpty();

    	this.digitalAssetVO = DigitalAssetController.getDigitalAssetVOWithId(this.digitalAssetId);
    	this.contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId);
        this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentVersionVO.getContentId());

    	File file = new File(getImageEditorPath() + File.separator + workingFileName);
    	//logger.info("saving file:" + file.getAbsolutePath());

   		String contentType = digitalAssetVO.getAssetContentType();
   		
		String fromEncoding = CmsPropertyHandler.getUploadFromEncoding();
		if(fromEncoding == null)
			fromEncoding = "iso-8859-1";
		
		String toEncoding = CmsPropertyHandler.getUploadToEncoding();
		if(toEncoding == null)
			toEncoding = "utf-8";
		
		this.digitalAssetKey = new String(digitalAssetKey.getBytes(fromEncoding), toEncoding);

    	DigitalAssetVO newAsset = new DigitalAssetVO();
		newAsset.setAssetContentType(contentType);
		newAsset.setAssetKey(this.digitalAssetKey);
		newAsset.setAssetFileName(digitalAssetVO.getAssetFileName());
		newAsset.setAssetFilePath(digitalAssetVO.getAssetFilePath());
		newAsset.setAssetFileSize(new Integer(new Long(file.length()).intValue()));
		InputStream is = new FileInputStream(file);
		
		if(this.contentTypeDefinitionId != null && digitalAssetKey != null)
		{
			AssetKeyDefinition assetKeyDefinition = ContentTypeDefinitionController.getController().getDefinedAssetKey(contentTypeDefinitionVO, true, digitalAssetKey);
			
			if(assetKeyDefinition != null)
			{
				if(assetKeyDefinition.getMaximumSize().intValue() < new Long(file.length()).intValue())
				{   
				    file.delete();
				    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnSizeText";
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
		
		if(this.contentVersionId != null)
		{
	    	List<Integer> newContentVersionIdList = new ArrayList<Integer>();
		    digitalAssetVO = DigitalAssetController.create(newAsset, is, this.contentVersionId, this.getInfoGluePrincipal(), newContentVersionIdList);
	    	if(newContentVersionIdList.size() > 0)
	    	{
	    		Integer newContentVersionId = newContentVersionIdList.get(0);
	    		logger.debug("newContentVersionId:" + newContentVersionId + ":" + this.contentVersionId);
	    		if(this.contentVersionId != newContentVersionId)
		    		this.refreshAll = true;
	    		setContentVersionId(newContentVersionId);
	    	}
		}

		if(is != null)
			is.close();
		
    	workingFileName = "imageEditorWK_" + System.currentTimeMillis() + "_" + this.getInfoGluePrincipal().getName().hashCode() + "_" + digitalAssetVO.getDigitalAssetId() + ".png";
    	if(CmsPropertyHandler.getEnableDiskAssets().equals("true") && file.exists())
    	{
			String folderName = "" + (digitalAssetVO.getDigitalAssetId().intValue() / 1000);
			String assetFileName = "" + digitalAssetVO.getAssetFilePath() + File.separator + folderName + File.separator + digitalAssetVO.getId() + "_" + digitalAssetVO.getAssetFileName();
			//logger.info("Going to move " + file.getName() + " to " + assetFileName);
    		File finalAssetFile = new File(assetFileName);
    		boolean moved = file.renameTo(finalAssetFile);
			//logger.info("moved:" + finalAssetFile.getAbsolutePath() + ":" + moved);    		
    	}
    	else
    	{
    		boolean deleted = file.delete();
			//logger.info("file:" + file.getAbsolutePath() + ":" + deleted);
    	}

		cleanOldWorkingFiles(true);
		
		closeOnLoad = "true";
		
        return "successSaveAndExit";
    }    

    /**
     * This method preserves space by only allowing 5 historic images and also cleaning up after a save totally.
     * All files older than 1 day are also removed.
     */
    private void cleanOldWorkingFiles(boolean cleanAll) throws Exception
    {
    	File workingAssetsDir = new File(getImageEditorPath());

    	final String matchString = "_" + this.getInfoGluePrincipal().getName() + "_" + this.digitalAssetId;
    	FilenameFilter filter = new FilenameFilter() 
    	{
    		public boolean accept(File dir, String name) 
            {
    			//logger.info("name: " + name + ":" + name.indexOf(matchString));
    			//logger.info("matchString: " + matchString);
    			return name.indexOf(matchString) > -1;
            }
        };
        
    	File[] files = workingAssetsDir.listFiles(filter);
    	for(int i=0; i < files.length; i++)
    	{
    		File file = files[i];
    		//logger.info("file:" + file.getName());
    		boolean deleted = file.delete();
    	}

    	File[] allFiles = workingAssetsDir.listFiles();
    	for(int i=0; i < allFiles.length; i++)
    	{
    		File file = allFiles[i];
    		long modified = file.lastModified();
    		long difference = System.currentTimeMillis() - modified;
    		if(difference > (1000 * 60 * 60 * 12))
    			file.delete();
    	}
    }
    
    private String getImageEditorPath()
    {
    	String path = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "imageEditor";
    	File dir = new File(path);
    	dir.mkdirs();
    	return path;
    }

    private String getImageEditorBaseUrl()
    {
    	return CmsPropertyHandler.getWebServerAddress() + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/imageEditor/";
    }

    public void setDigitalAssetKey(String digitalAssetKey)
	{
		this.digitalAssetKey = digitalAssetKey;
	}
	    
	public List getDefinedAssetKeys()
	{
		return ContentTypeDefinitionController.getController().getDefinedAssetKeys(this.contentTypeDefinitionVO, true);
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
    
    public String getCloseOnLoad()
    {
        return closeOnLoad;
    }
    
    public void setCloseOnLoad(String closeOnLoad)
    {
        this.closeOnLoad = closeOnLoad;
    }

	public String getModifiedFileUrl() 
	{
		return modifiedFileUrl;
	}

	public int getXpos1() 
	{
		return xpos1;
	}

	public void setXpos1(int xpos1) 
	{
		this.xpos1 = xpos1;
	}

	public int getXpos2() 
	{
		return xpos2;
	}

	public void setXpos2(int xpos2) 
	{
		this.xpos2 = xpos2;
	}

	public int getYpos1() 
	{
		return ypos1;
	}

	public void setYpos1(int ypos1) 
	{
		this.ypos1 = ypos1;
	}

	public int getYpos2() 
	{
		return ypos2;
	}

	public void setYpos2(int ypos2) 
	{
		this.ypos2 = ypos2;
	}

	public String getWorkingFileName() 
	{
		return workingFileName;
	}

	public void setWorkingFileName(String workingFileName) 
	{
		this.workingFileName = workingFileName;
	}

	public int getHeight() 
	{
		return height;
	}

	public void setHeight(int height) 
	{
		this.height = height;
	}

	public int getWidth() 
	{
		return width;
	}

	public void setWidth(int width) 
	{
		this.width = width;
	}

	public void setKeepRatio(String keepRatio) 
	{
		this.keepRatio = keepRatio;
	}

	public void setBestFit(String bestFit) 
	{
		this.bestFit = bestFit;
	}
    
	public boolean getRefreshAll()
	{
		return this.refreshAll;
	}

}
