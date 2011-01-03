package org.infoglue.cms.util.webdav;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryLanguageController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.FileResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.ResourceFactory;
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
	
	@Override
	public Date getCreateDate() {
		return new Date();
	}

	@Override
	public Object authenticate(String user, String pwd) {
		return user;
	}

	@Override
	public boolean authorise(Request arg0, Method arg1, Auth arg2) {
		return true;
	}

	@Override
	public String checkRedirect(Request arg0) {
		return null;
	}

	@Override
	public Date getModifiedDate() {
		return new Date();
	}

	@Override
	public String getName() {
		return digitalAsset.getAssetFileName().toString(); //.getLanguageName();
	}

	@Override
	public String getRealm() {
		return "infoglue";
	}

	@Override
	public String getUniqueId() {
		return digitalAsset.getId().toString();
	}



	@Override
	public void copyTo(CollectionResource arg0, String arg1) 
	{
		
	}

	@Override
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

	@Override
	public Long getContentLength() {
		return new Long(digitalAsset.getAssetFileSize());
	}

	@Override
	public String getContentType(String arg0) {
		return digitalAsset.getAssetContentType();
	}

	@Override
	public Long getMaxAgeSeconds(Auth arg0) 
	{
		return null;
	}

	@Override
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

	@Override
	public void moveTo(CollectionResource newTargetResource, String newName) throws ConflictException 
	{
		try
		{
			if(!(newTargetResource instanceof ContentVersionResource))
				throw new Exception("Could not move to an entity not based on a content version");
			else
			{
				ContentVersionResource newTargetContentVersionResource = (ContentVersionResource)newTargetResource;
				if(!newTargetContentVersionResource.getUniqueId().equals(this.cv.getId().toString()))
					throw new Exception("We do not support moving assets between content versions yet...");
			}
				
				
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

	@Override
	public String processForm(Map<String, String> arg0,
			Map<String, FileItem> arg1) throws BadRequestException,
			NotAuthorizedException, ConflictException {
		// TODO Auto-generated method stub
		return null;
	}

}
