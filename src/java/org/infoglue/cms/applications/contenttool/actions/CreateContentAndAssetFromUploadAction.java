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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.AssetKeyDefinition;
import org.infoglue.cms.applications.databeans.SessionInfoBean;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.CmsSessionContextListener;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.infoglue.cms.util.graphics.Imaging;

import webwork.action.ActionContext;
import webwork.multipart.MultiPartRequestWrapper;

public class CreateContentAndAssetFromUploadAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(CreateContentAndAssetFromUploadAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private Integer parentContentId = null;
	private Integer repositoryId = null;
	
	private String digitalAssetKey   = "";
	private Integer uploadedFilesCounter = new Integer(0);

	private ContentTypeDefinitionVO contentTypeDefinitionVO;
	private String reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnSizeText";
	private String uploadMaxSize;
	private DigitalAssetVO digitalAssetVO = null;
	private String closeOnLoad;
	private Integer contentTypeDefinitionId;
	private InfoGluePrincipal principal = null;
	private boolean useFileNameAsContentTypeBase = false;
	private boolean refreshAll = false;
	
	private VisualFormatter formatter = new VisualFormatter();
		
    public String doInput() throws Exception 
    {
    	logger.info("Input state");
    	
    	return INPUT;
    }
	
    public String doMultiple() throws Exception
    {
    	logger.info("Uploading file....");
    	this.principal = getInfoGluePrincipal();
    	
		logger.info("QueryString:" + this.getRequest().getQueryString());
		String requestSessionId = this.getRequest().getParameter("JSESSIONID");
		logger.info("JSESSIONID:" + requestSessionId);
		boolean allowedSessionId = false;
		List activeSessionBeanList = CmsSessionContextListener.getSessionInfoBeanList();
		Iterator activeSessionsIterator = activeSessionBeanList.iterator();
		logger.info("activeSessionBeanList:" + activeSessionBeanList.size());
		while(activeSessionsIterator.hasNext())
		{
			SessionInfoBean sessionBean = (SessionInfoBean)activeSessionsIterator.next();
			logger.info("sessionBean:" + sessionBean.getId() + "=" + sessionBean.getPrincipal().getName());
			if(sessionBean.getId().equals(requestSessionId))
			{
				logger.info("Found a matching sessionId");
				allowedSessionId = true;
		    	this.principal = sessionBean.getPrincipal();

				break;
			}
		}
		
		if(!allowedSessionId)
		{
			return "uploadFailed";	
		}
		
		useFileNameAsContentTypeBase = true;
		
		String result = doExecute();
		
		if(result.equals("success"))
		{
			String assetUrl = getDigitalAssetUrl();
			logger.info("assetUrl:" + assetUrl);
			String assetThumbnailUrl = getAssetThumbnailUrl();
			logger.info("assetThumbnailUrl:" + assetThumbnailUrl);
			this.getResponse().setContentType("text/plain");
	        this.getResponse().getWriter().println(assetThumbnailUrl + ":" + this.digitalAssetKey);
	        return NONE;
		}
		else
		{
			this.getResponse().setContentType("text/plain");
            this.getResponse().setStatus(this.getResponse().SC_INTERNAL_SERVER_ERROR);
            this.getResponse().getWriter().println("Error uploading to " + this.digitalAssetKey);
            return NONE;
		}
	}
    
    public String doExecute()
    {
    	if(this.principal == null)
    		this.principal = getInfoGluePrincipal();

        try
        {
            MultiPartRequestWrapper mpr = ActionContext.getMultiPartRequest();
            if(mpr == null)
            {
                this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnSizeText";
                this.uploadMaxSize = "(Max " + formatter.formatFileSize(getUploadMaxSize()) + " - system wide)";
                return "uploadFailed";
            }
            
			File file = null;
			
	    	try 
	    	{
	    		if(mpr != null)
	    		{ 
		    		Enumeration names = mpr.getFileNames();
		         	while (names.hasMoreElements()) 
		         	{
		            	String name 		  = (String)names.nextElement();
						String contentType    = mpr.getContentType(name);
						String fileSystemName = mpr.getFilesystemName(name);
						
						logger.info("contentType:" + contentType);
						logger.info("fileSystemName:" + fileSystemName);
						if(fileSystemName.endsWith(".zip"))
						{
							file = mpr.getFile(name);
							
							String folder = CmsPropertyHandler.getDigitalAssetUploadPath() + File.separator + "zip" + System.currentTimeMillis();
							List<File> unzippedFiles = FileHelper.unzipFile(file, folder);
							for(File unzippedFile : unzippedFiles)
							{
								handleFile(null, name, null, unzippedFile.getName(), unzippedFile);
							}
						}
						else
						{
							String fromEncoding = CmsPropertyHandler.getUploadFromEncoding();
							if(fromEncoding == null)
								fromEncoding = "iso-8859-1";
							
							String toEncoding = CmsPropertyHandler.getUploadToEncoding();
							if(toEncoding == null)
								toEncoding = "utf-8";
							
			            	digitalAssetKey = new String(digitalAssetKey.getBytes(fromEncoding), toEncoding);
			            	
			            	file = mpr.getFile(name);
			        		
			            	return handleFile(digitalAssetKey, name, contentType, fileSystemName, file);
						}
		         	}
	    		}
	    		else
	    		{
	    		    logger.error("File upload failed for some reason.");
	    		}
	      	} 
	      	catch (Throwable e) 
	      	{
	      	    logger.error("An error occurred when we tried to upload a new asset:" + e.getMessage(), e);
	      	}
        }
        catch(Throwable e)
        { 
      	    logger.error("An error occurred when we tried to upload a new asset:" + e.getMessage(), e);
        }
                
        return "success";
    }

    private String handleFile(String digitalAssetKey, String name, String contentType, String fileSystemName, File file) throws Exception
    {
    	InputStream is = null;
    	try
    	{
	    	logger.info("digitalAssetKey:" + digitalAssetKey);
	    	logger.info("name:" + name);
	    	logger.info("contentType:" + contentType);
	    	logger.info("fileSystemName:" + fileSystemName);
	    	if(digitalAssetKey == null || digitalAssetKey.equals(""))
	    	{
	    		if(fileSystemName.lastIndexOf(".") > -1)
	    			digitalAssetKey = fileSystemName.substring(0, fileSystemName.lastIndexOf("."));
	    		
	    		digitalAssetKey = formatter.replaceNiceURINonAsciiWithSpecifiedChars(digitalAssetKey, CmsPropertyHandler.getNiceURIDefaultReplacementCharacter());
	    		//digitalAssetKey = formatter.replaceNonAscii(digitalAssetKey, '_');
	    	}
	    	logger.info("digitalAssetKey:" + digitalAssetKey);
	    	
	    	if(useFileNameAsContentTypeBase || contentType == null)
	    	{
	    		if(fileSystemName.lastIndexOf(".") > -1)
	    		{
	    			String extension = fileSystemName.substring(fileSystemName.lastIndexOf(".") + 1);
	    			logger.info("extension:" + extension);	
	
	    			if(extension.equalsIgnoreCase("gif"))
	        			contentType = "image/gif";
	        		else if(extension.equalsIgnoreCase("jpg"))
	        			contentType = "image/jpg";
	        		else if(extension.equalsIgnoreCase("png"))
	        			contentType = "image/png";
	        		
	        		else if(extension.equalsIgnoreCase("pdf"))
	        			contentType = "application/pdf";
	        		else if(extension.equalsIgnoreCase("doc"))
	        			contentType = "application/msword";
	        		else if(extension.equalsIgnoreCase("xls"))
	        			contentType = "application/vnd.ms-excel";
	        		else if(extension.equalsIgnoreCase("ppt"))
	        			contentType = "application/vnd.ms-powerpoint";
	        		else if(extension.equalsIgnoreCase("zip"))
	        			contentType = "application/zip";
	        		else if(extension.equalsIgnoreCase("xml"))
	        			contentType = "text/xml";		            		
	    		}
	    	}
	    	logger.info("contentType:" + contentType);
	    	
	    	String fileName = fileSystemName;
	
	    	logger.info("fileSystemName:" + fileSystemName);
	
	    	fileName = formatter.replaceNiceURINonAsciiWithSpecifiedChars(fileName, CmsPropertyHandler.getNiceURIDefaultReplacementCharacter());
			//fileName = formatter.replaceNonAscii(fileName, '_');
			
			String tempFileName = "tmp_" + System.currentTimeMillis() + "_" + fileName;
	    	//String filePath = file.getParentFile().getPath();
	    	String filePath = CmsPropertyHandler.getDigitalAssetPath();
	    	fileSystemName = filePath + File.separator + tempFileName;
	    	
	    	DigitalAssetVO newAsset = new DigitalAssetVO();
			newAsset.setAssetContentType(contentType);
			newAsset.setAssetKey(digitalAssetKey);
			newAsset.setAssetFileName(fileName);
			newAsset.setAssetFilePath(filePath);
			newAsset.setAssetFileSize(new Integer(new Long(file.length()).intValue()));
			
			if(CmsPropertyHandler.getEnableDiskAssets().equals("false"))
				is = new FileInputStream(file);
	
			String fileUploadMaximumSize = getPrincipalPropertyValue(this.principal, "fileUploadMaximumSize", false, true);
			logger.info("fileUploadMaximumSize in create:" + fileUploadMaximumSize);
			if(!fileUploadMaximumSize.equalsIgnoreCase("-1") && new Integer(fileUploadMaximumSize).intValue() < new Long(file.length()).intValue())
			{
			    file.delete();
			    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnSizeText";
	            this.uploadMaxSize = "(Max " + formatter.formatFileSize(fileUploadMaximumSize) + ")";
	        	return "uploadFailed";
			}
			
			logger.info("contentTypeDefinitionId:" + contentTypeDefinitionId);
	        if(contentTypeDefinitionId == null)
	        {
	        	logger.info("Defaulting to Image");
	        	this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("Image");
	        	if(this.contentTypeDefinitionVO != null)
	        		contentTypeDefinitionId = this.contentTypeDefinitionVO.getId();
	        }
	        else
	        {
	        	this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentTypeDefinitionId);
	        }
	        
			if(this.contentTypeDefinitionId != null && digitalAssetKey != null)
			{
	
				//this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(this.contentTypeDefinitionId);
				AssetKeyDefinition assetKeyDefinition = ContentTypeDefinitionController.getController().getDefinedAssetKey(contentTypeDefinitionVO, true, digitalAssetKey);
				
				if(assetKeyDefinition != null)
				{
					if(assetKeyDefinition.getMaximumSize().intValue() < new Long(file.length()).intValue())
					{   
					    file.delete();
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
			
			boolean keepOriginal = true;
	
	    	LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(repositoryId);
	
	    	logger.info("parentContentId: " + this.parentContentId);
	    	logger.info("repositoryId: " + this.repositoryId);
	        logger.info("contentTypeDefinition: " + this.contentTypeDefinitionId);
	        logger.info("masterLanguageVO: " + masterLanguageVO.getId());
	        		    	        
	    	ContentVO contentVO = new ContentVO();
	    	contentVO.setName(fileName);
	    	contentVO.setCreatorName(this.principal.getName());
	    	ContentVO createdContentVO = ContentControllerProxy.getController().acCreate(this.principal, parentContentId, contentTypeDefinitionId, repositoryId, contentVO);
	
			//Create initial content version also... in masterlanguage
			String versionValue = "<?xml version='1.0' encoding='UTF-8'?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes><Name><![CDATA[" + fileName + "]]></Name></attributes></article>";
	
	    	ContentVersionVO cvVO = new ContentVersionVO();
	    	cvVO.setVersionComment("Initial version");
	    	cvVO.setVersionModifier(this.principal.getName());
	    	cvVO.setVersionValue(versionValue);
	
	    	ContentVersionVO newContentVersion = ContentVersionController.getContentVersionController().create(contentVO.getId(), masterLanguageVO.getId(), cvVO, null);
	
			if(newContentVersion != null)
			{
				AssetKeyDefinition assetKeyDefinition = ContentTypeDefinitionController.getController().getDefinedAssetKey(this.contentTypeDefinitionVO, true, digitalAssetKey);
				
				keepOriginal = handleTransformations(newAsset, file, contentType, assetKeyDefinition, newContentVersion.getId());
			    if(keepOriginal)
			    {
			    	List<Integer> newContentVersionIdList = new ArrayList<Integer>();
			    	digitalAssetVO = DigitalAssetController.create(newAsset, is, newContentVersion.getId(), this.getInfoGluePrincipal(), newContentVersionIdList);
			    }
			}
	
			if(CmsPropertyHandler.getEnableDiskAssets().equals("true"))
			{
				if(keepOriginal)
				{
					//String assetFileName = "" + digitalAssetVO.getAssetFilePath() + File.separator + digitalAssetVO.getId() + "_" + digitalAssetVO.getAssetFileName();
					String folderName = "" + (digitalAssetVO.getDigitalAssetId().intValue() / 1000);
					String assetFileName = "" + digitalAssetVO.getAssetFilePath() + File.separator + folderName + File.separator + digitalAssetVO.getId() + "_" + digitalAssetVO.getAssetFileName();
					//logger.info("newAsset:" + assetFileName);
					File assetFile = new File(assetFileName);
					//logger.info("Renaming:" + file.getAbsolutePath() + " to " + assetFile.getAbsolutePath());
					file.renameTo(assetFile);
					//logger.info("apaFile:" + assetFile.exists());
				}
			}
			
			this.uploadedFilesCounter = new Integer(this.uploadedFilesCounter.intValue() + 1);	
    	}
    	finally
		{
			try
			{
				if(CmsPropertyHandler.getEnableDiskAssets().equals("false"))
				{	
					is.close();
					file.delete();
				}
			}
			catch(Throwable e)
			{ 
			    logger.error("An error occurred when we tried to close the fileinput stream and delete the file:" + e.getMessage(), e);
			}
		}
    	
		return "success";
    }
    
    
	private boolean handleTransformations(DigitalAssetVO originalAssetVO, File file, String contentType, AssetKeyDefinition assetKeyDefinition, Integer contentVersionId)
	{
		boolean keepOriginal = true;
		try
		{
			String transformationsXML = null;
			if(assetKeyDefinition != null)
				transformationsXML = assetKeyDefinition.getAssetUploadTransformationsSettings();

			if(transformationsXML == null || transformationsXML.equals(""))
				transformationsXML = CmsPropertyHandler.getAssetUploadTransformationsSettings();

			if(transformationsXML == null || transformationsXML.equals("") || transformationsXML.equals("none"))
				return keepOriginal;
			
			DOMBuilder domBuilder = new DOMBuilder();
			Document document = domBuilder.getDocument(transformationsXML);
		    Element rootElement = document.getRootElement();
		    
			String transformationXPath = "//transformation";
			List transformationElements = rootElement.selectNodes(transformationXPath);
			logger.info("transformationElements:" + transformationElements.size());
			
			/*
			<transformations>
			  <transformation inputFilePattern=".*(jpeg|jpg|gif|png).*" keepOriginal="false">
			    <tranformResult type="scaleImage" width="100" height="100" outputFormat="png" assetSuffix="medium"/>
			    <tranformResult type="scaleImage" width="50" height="50" outputFormat="jpg" assetSuffix="small"/>
			  </transformation>
			</transformations>
			*/
			
			Iterator transformationElementsIterator = transformationElements.iterator();
			while(transformationElementsIterator.hasNext())
			{
				Element transformationElement = (Element)transformationElementsIterator.next();
			
				String assetKeyPattern  = transformationElement.attributeValue("assetKeyPattern");
				String includedContentTypeDefinitionNames = transformationElement.attributeValue("includedContentTypeDefinitionNames");
				String inputFilePattern  = transformationElement.attributeValue("inputFilePattern");
				logger.info("includedContentTypeDefinitionNames: " + includedContentTypeDefinitionNames);
				logger.info("inputFilePattern: " + inputFilePattern);
				
				boolean assetKeyMatch = false;
				if(assetKeyPattern == null || assetKeyPattern.equals(""))
					assetKeyMatch = true;
				else if(originalAssetVO.getAssetKey().matches(assetKeyPattern))
					assetKeyMatch = true;
						
				boolean contentTypeDefinitionNameMatch = true;
				if(contentTypeDefinitionVO.getName() != null && includedContentTypeDefinitionNames != null && includedContentTypeDefinitionNames.indexOf(contentTypeDefinitionVO.getName()) == -1)
					contentTypeDefinitionNameMatch = false;

				if(contentTypeDefinitionNameMatch && assetKeyMatch && contentType.matches(inputFilePattern))
				{
					logger.info("We got a match on contentType:" + contentType + " : " + inputFilePattern);

					String keepOriginalAsset = transformationElement.attributeValue("keepOriginal");
					if(keepOriginalAsset != null && keepOriginalAsset.equalsIgnoreCase("false"))
						keepOriginal = false;
					
					logger.info("keepOriginal:" + keepOriginal);

					List tranformResultElements = transformationElement.elements("tranformResult");
					Iterator tranformResultElementsIterator = tranformResultElements.iterator();
					while(tranformResultElementsIterator.hasNext())
					{
						Element tranformResultElement = (Element)tranformResultElementsIterator.next();
					
						String type 		= tranformResultElement.attributeValue("type");
						String width 		= tranformResultElement.attributeValue("width");
						String height 		= tranformResultElement.attributeValue("height");
						String outputFormat	= tranformResultElement.attributeValue("outputFormat");
						String assetSuffix 	= tranformResultElement.attributeValue("assetSuffix");
	
						logger.info("type: " + type);
						logger.info("width: " + width);
						logger.info("height: " + height);
						logger.info("outputFormat: " + outputFormat);
						logger.info("assetSuffix: " + assetSuffix);
						
						if(type.equalsIgnoreCase("scaleImage"))
							scaleAndSaveImage(originalAssetVO, file, Integer.parseInt(width), Integer.parseInt(height), outputFormat, assetSuffix, contentVersionId);
					}
				}
				else
				{
					logger.info("NOOOO match on contentType:" + contentType + " : " + inputFilePattern);
				}
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return keepOriginal;
	}

	private void scaleAndSaveImage(DigitalAssetVO originalAssetVO, File file, int width, int height, String outputFormat, String assetSuffix, Integer contentVersionId) throws Exception
	{
    	String workingFileName = "" + originalAssetVO.getDigitalAssetId() + "_" + assetSuffix + "." + outputFormat.toLowerCase();
    	long timeStamp = System.currentTimeMillis();
    	if(originalAssetVO.getDigitalAssetId() == null)
    		workingFileName = "" + timeStamp + "_" + assetSuffix + "." + outputFormat.toLowerCase();
    	
    	File outputFile = new File(CmsPropertyHandler.getDigitalAssetPath() + File.separator + File.separator + workingFileName);
		
    	logger.info("Scaling image to new format:" + originalAssetVO + ":" + outputFormat);
		Imaging.resize(file, outputFile, width, height, outputFormat, true);
		
		String assetContentType = "image/png";
		if(outputFormat.equalsIgnoreCase("gif"))
			assetContentType = "image/gif";
		if(outputFormat.equalsIgnoreCase("jpg"))
			assetContentType = "image/jpeg";
		
		DigitalAssetVO digitalAssetVO = new DigitalAssetVO();
		digitalAssetVO.setAssetContentType(assetContentType);
		digitalAssetVO.setAssetFileName(outputFile.getName());
		digitalAssetVO.setAssetFilePath(outputFile.getPath());
		digitalAssetVO.setAssetFileSize(new Integer((int)outputFile.length()));
		digitalAssetVO.setAssetKey(originalAssetVO.getAssetKey() + "_" + assetSuffix);
		
		InputStream is = new FileInputStream(outputFile);
		
    	List<Integer> newContentVersionIdList = new ArrayList<Integer>();
    	this.digitalAssetVO = DigitalAssetController.create(digitalAssetVO, is, contentVersionId, this.getInfoGluePrincipal(), newContentVersionIdList);
    	
    	is.close();
		
		//logger.info("this.digitalAssetVO in scale:" + this.digitalAssetVO.getId());
    	String folderName = "" + (this.digitalAssetVO.getId().intValue() / 1000);
		String newWorkingFileName = "" + this.digitalAssetVO.getId() + "_" + timeStamp + "_" + assetSuffix + "." + outputFormat.toLowerCase();
    	File finalOutputFile = new File(CmsPropertyHandler.getDigitalAssetPath() + File.separator + folderName + File.separator + newWorkingFileName);
    	outputFile.renameTo(finalOutputFile);
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
       		imageHref = DigitalAssetController.getDigitalAssetUrl(digitalAssetVO.getDigitalAssetId());
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
       		imageHref = DigitalAssetController.getDigitalAssetThumbnailUrl(digitalAssetVO.getDigitalAssetId());
		}
		catch(Exception e)
		{
		    logger.warn("We could not get the url of the thumbnail: " + e.getMessage(), e);
		}
		
		return imageHref;
    }
    
    public boolean getAllowedSessionId(String requestSessionId) throws Exception
    {
		boolean allowedSessionId = false;
		List activeSessionBeanList = CmsSessionContextListener.getSessionInfoBeanList();
		Iterator activeSessionsIterator = activeSessionBeanList.iterator();
		//logger.info("activeSessionBeanList:" + activeSessionBeanList.size());
		while(activeSessionsIterator.hasNext())
		{
			SessionInfoBean sessionBean = (SessionInfoBean)activeSessionsIterator.next();
			//logger.info("sessionBean:" + sessionBean.getId() + "=" + sessionBean.getPrincipal().getName());
			if(sessionBean.getId().equals(requestSessionId))
			{
				//logger.info("Found a matching sessionId");
				allowedSessionId = true;
		    	
				break;
			}
		}
		return allowedSessionId;
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
    
    public Integer getContentTypeDefinitionId()
    {
        return contentTypeDefinitionId;
    }
    
    public void setContentTypeDefinitionId(Integer contentTypeDefinitionId)
    {
        this.contentTypeDefinitionId = contentTypeDefinitionId;
    }    
    
    public ContentTypeDefinitionVO getContentTypeDefinitionVO()
    {
        return this.contentTypeDefinitionVO;
    }
    
	public String getUploadErrorMaxSize()
	{
		return uploadMaxSize;
	}

	public boolean getRefreshAll()
	{
		return this.refreshAll;
	}

    public void setDigitalAssetKey(String digitalAssetKey)
	{
		this.digitalAssetKey = digitalAssetKey;
	}

    public String getDigitalAssetKey()
    {
        return digitalAssetKey;
    }

	public void setUploadedFilesCounter(Integer uploadedFilesCounter)
	{
		this.uploadedFilesCounter = uploadedFilesCounter;
	}

	public Integer getUploadedFilesCounter()
	{
		return this.uploadedFilesCounter;
	}

	public Integer getParentContentId() 
	{
		return parentContentId;
	}

	public void setParentContentId(Integer parentContentId) 
	{
		this.parentContentId = parentContentId;
	}

	public Integer getRepositoryId() 
	{
		return repositoryId;
	}

	public void setRepositoryId(Integer repositoryId) 
	{
		this.repositoryId = repositoryId;
	}
	   
}