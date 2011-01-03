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

package org.infoglue.cms.util;

import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;

import webwork.multipart.MultiPartRequestWrapper;


/**
  * This is the action-class for UpdateDigitalAssetVersion
  * 
  * @author Mattias Bogeblad
  */

public class FileUploadHelper
{
    private final static Logger logger = Logger.getLogger(FileUploadHelper.class.getName());

    public FileUploadHelper()
    {
        // don't instanciate, use static methods
    }
    
	public static File getUploadedFile(MultiPartRequestWrapper mpr)
	{
		File renamedFile = null;
		
		try 
		{
			if(mpr != null)
			{
				Enumeration names = mpr.getFileNames();
				while (names.hasMoreElements()) 
				{
					String name = (String)names.nextElement();
						            	
					File file = mpr.getFile(name);
					logger.info("file:" + file.getPath() + ":" + file.exists());
					if(file != null)
					{
						//String contentType    = mpr.getContentType(name);
						String fileSystemName = mpr.getFilesystemName(name);
						
						String fileName = "Import_" + System.currentTimeMillis() + fileSystemName;
						//fileName = new VisualFormatter().replaceNonAscii(fileName, '_');
						fileName = new VisualFormatter().replaceNiceURINonAsciiWithSpecifiedChars(fileName, CmsPropertyHandler.getNiceURIDefaultReplacementCharacter());
						
						String filePath = CmsPropertyHandler.getDigitalAssetUploadPath();
						fileSystemName =  filePath + File.separator + fileName;
						logger.info("fileSystemName:" + fileSystemName);
						
						renamedFile = new File(fileSystemName);
						boolean isRenamed = file.renameTo(renamedFile);
						logger.info("renamed file:" + renamedFile.getPath() + ":" + renamedFile.exists() + ":" + isRenamed);
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
			logger.error("An error occurred when we get and rename an uploaded file:" + e.getMessage(), e);
		}
		
		return renamedFile;
	}
	
	/**
	 * Lists the files
	 */

	public static void listMultiPartFiles(HttpServletRequest req)
	{
	    try
	    {
	        File tempDir = new File("c:/temp/uploads");
	        logger.info("tempDir:" + tempDir.exists());
	        
	        DiskFileItemFactory factory = new DiskFileItemFactory(1000, tempDir);
	        ServletFileUpload upload = new ServletFileUpload(factory);
	        if(ServletFileUpload.isMultipartContent((HttpServletRequest)req))
	        {
	            List fileItems = upload.parseRequest((HttpServletRequest)req);
	            logger.info("******************************");
	            logger.info("fileItems:" + fileItems.size());
	            logger.info("******************************");
	            req.setAttribute("Test", "Mattias Testar");
	            
	            Iterator i = fileItems.iterator();
		        while(i.hasNext())
		        {
		            Object o = i.next();
		            DiskFileItem dfi = (DiskFileItem)o;
		            logger.info("dfi:" + dfi.getFieldName());
		            logger.info("dfi:" + dfi);
		            
		            if (!dfi.isFormField()) {
		                String fieldName = dfi.getFieldName();
		                String fileName = dfi.getName();
		                String contentType = dfi.getContentType();
		                boolean isInMemory = dfi.isInMemory();
		                long sizeInBytes = dfi.getSize();
		                
		                logger.info("fieldName:" + fieldName);
		                logger.info("fileName:" + fileName);
		                logger.info("contentType:" + contentType);
		                logger.info("isInMemory:" + isInMemory);
		                logger.info("sizeInBytes:" + sizeInBytes);
		                File uploadedFile = new File("c:/temp/uploads/" + fileName);
		                dfi.write(uploadedFile);
	
			            req.setAttribute(dfi.getFieldName(), uploadedFile.getAbsolutePath());
		            }
		            
		        }
	        }
	    } 
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }
	}
}