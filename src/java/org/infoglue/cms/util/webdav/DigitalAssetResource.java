package org.infoglue.cms.util.webdav;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.FileResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

public class DigitalAssetResource implements PropFindableResource, FileResource
{
	private final static Logger logger = Logger.getLogger(DigitalAssetResource.class.getName());

	private final DigitalAssetVO digitalAsset;
	private final ContentVersionVO cv;
	
	public DigitalAssetResource(DigitalAssetVO digitalAsset, ContentVersionVO cv) {
		this.digitalAsset = digitalAsset;
		this.cv = cv;
	}	
	
	public Date getCreateDate() {
		return new Date();
	}

	public Object authenticate(String user, String pwd) {
		return user;
	}

	public boolean authorise(Request arg0, Method arg1, Auth arg2) {
		return true;
	}

	public String checkRedirect(Request arg0) {
		return null;
	}

	public Date getModifiedDate() {
		return new Date();
	}

	public String getName() {
		return digitalAsset.getAssetFileName().toString(); //.getLanguageName();
	}

	public String getRealm() {
		return "infoglue";
	}

	public String getUniqueId() {
		return digitalAsset.getId().toString();
	}

	public void copyTo(CollectionResource arg0, String arg1) 
	{
	}

	public void delete() throws NotAuthorizedException, ConflictException, BadRequestException 
	{
		if(logger.isInfoEnabled())
			logger.info("Deleting asset:" + this.digitalAsset.getId() + " and decoupling it from " + cv.getId());
		
		try
		{
			DigitalAssetController.getController().delete(this.digitalAsset.getId(), "ContentVersion", cv.getId());
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new NotAuthorizedException(this);
		}
	}

	public Long getContentLength() {
		return new Long(digitalAsset.getAssetFileSize());
	}

	public String getContentType(String arg0) {
		return digitalAsset.getAssetContentType();
	}

	public Long getMaxAgeSeconds(Auth arg0) 
	{
		return null;
	}

	public void sendContent(OutputStream out, Range arg1, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException 
	{
		try
		{
			String filePath = DigitalAssetController.getDigitalAssetFilePath(digitalAsset.getId());
			
			if(logger.isInfoEnabled())
				logger.info("filePath:" + filePath);
			
			File file = new File(filePath);
			
			if(logger.isInfoEnabled())
				logger.info("file:" + file.getPath());
			
			FileInputStream fis = new FileInputStream(filePath);
	        BufferedInputStream bin = new BufferedInputStream(fis);
	        final byte[] buffer = new byte[ 1024 ];
	        int n = 0;
	        while( -1 != (n = bin.read( buffer )) ) {
	            out.write( buffer, 0, n );
	        }        
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new IOException("Could not open file:" + e.getMessage());
		}
	}

	public void moveTo(CollectionResource newTargetResource, String newName) throws ConflictException 
	{
		try
		{
			if(!(newTargetResource instanceof ContentVersionResource))
				throw new Exception("Could not move to an entity not based on a content version");
			ContentVersionResource newTargetContentVersionResource = (ContentVersionResource)newTargetResource;
			if(!newTargetContentVersionResource.getUniqueId().equals(this.cv.getId().toString()))
				throw new Exception("We do not support moving assets between content versions yet...");
			digitalAsset.setAssetFileName(newName);
			if(newName.lastIndexOf(".") > -1)
				digitalAsset.setAssetKey(newName.substring(0,newName.lastIndexOf(".")));
			else
				digitalAsset.setAssetKey(newName);
				
			DigitalAssetController.update(digitalAsset, null);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new ConflictException(this);
		}
	}

	public String processForm(Map<String, String> arg0,
			Map<String, FileItem> arg1) throws BadRequestException,
			NotAuthorizedException, ConflictException {
		// TODO Auto-generated method stub
		return null;
	}
}
