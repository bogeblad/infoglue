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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryLanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.AuthenticationModule;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.LockedException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.PreConditionFailedException;

public class ContentVersionResource implements PropFindableResource, FolderResource
{
	private final static Logger logger = Logger.getLogger(ContentVersionResource.class.getName());

	private InfoGluePrincipal principal = null;
	private final ContentVersionVO contentVersion;
	
	public ContentVersionResource(ContentVersionVO contentVersion) {
		this.contentVersion = contentVersion;
	}	
	
	@Override
	public Date getCreateDate() {
		// Unknown
		return null;
	}

	@Override
	public Object authenticate(String user, String pwd) 
	{
		if(logger.isInfoEnabled())
			logger.info("authenticate user in represource:" + user);

		try 
		{
		    Map loginMap = new HashMap();
	        loginMap.put("j_username", user);
	        loginMap.put("j_password", pwd);
			String authenticatedUserName = AuthenticationModule.getAuthenticationModule(null, null).authenticateUser(loginMap);
			if(authenticatedUserName != null)
				this.principal = UserControllerProxy.getController().getUser(authenticatedUserName);

			return authenticatedUserName;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		 
		return null;
	}
	
	@Override
	public boolean authorise(Request arg0, Method arg1, Auth arg2) 
	{
		try 
		{
			boolean hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(this.principal, "ContentVersion.Read", contentVersion.getId().toString());
			if(!hasAccess)
				return false;
		} 
		catch (SystemException e) 
		{
			e.printStackTrace();
		} 
		
		return true;
	}
	
	@Override
	public String checkRedirect(Request arg0) {
		// No redirects
		return null;
	}

	@Override
	public Date getModifiedDate() {
		// Unknown
		return null;
	}

	@Override
	public String getName() {
		return contentVersion.getLanguageName();
	}

	@Override
	public String getRealm() {
		return "infoglue";
	}

	@Override
	public String getUniqueId() {
		return contentVersion.getId().toString();
	}

	@Override
	public Resource child(String name) 
	{
		if(logger.isInfoEnabled())
			logger.info("child name:" + name);
		
		List<? extends Resource> children = getChildren();
		Iterator<? extends Resource> childrenIterator = children.iterator();
		while(childrenIterator.hasNext())
		{
			Resource resource = childrenIterator.next();
			if(resource.getName().equals(name))
			{
				return resource;
			}
		}
		return null;
	}

	@Override
	public List<? extends Resource> getChildren() 
	{
		if(logger.isInfoEnabled())
			logger.info("Looking for children on contentversion:" + this.contentVersion.getId() + ":" + this.contentVersion.getLanguageName());
		List<DigitalAssetResource> assets = new ArrayList<DigitalAssetResource>();
		
		try 
		{
			List<DigitalAssetVO> assetVOList = DigitalAssetController.getController().getDigitalAssetVOList(this.contentVersion.getId());
			
			if(logger.isInfoEnabled())
				logger.info("assetVOList:" + assetVOList.size());
			
			for(DigitalAssetVO asset : assetVOList)
			{
				assets.add( new DigitalAssetResource(asset, this.contentVersion) );
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return assets;
	}

	@Override
	public CollectionResource createCollection(String arg0)
			throws NotAuthorizedException, ConflictException {
		// TODO Auto-generated method stub
		return null;
	}

	public InfoGluePrincipal getInfoGluePrincipal()
	{
		List roles = new ArrayList();
		roles.add(new InfoGlueRole("administrators", "", null));
		roles.add(new InfoGlueRole("cmsUser", "", null));

		List groups = new ArrayList();
		groups.add(new InfoGlueGroup("administrators", "", null));
		groups.add(new InfoGlueGroup("cmsUser", "", null));

		InfoGluePrincipal p = new InfoGluePrincipal("mattias", "mattias", "Mattias", "Bogeblad", "mattias.bogeblad@gmail.com", roles, groups, false, null);
		
		return p;
	}

	@Override
	public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException 
	{
		if(logger.isInfoEnabled())
			logger.info("Creating new asset:" + newName + ":" + length + ":" + contentType);
		
		Resource resource = null;
		try
		{
			DigitalAssetVO digitalAsset = null;
			
			List<DigitalAssetVO> assetVOList = DigitalAssetController.getController().getDigitalAssetVOList(this.contentVersion.getId());
			if(logger.isInfoEnabled())
				logger.info("assetVOList:" + assetVOList.size());
			for(DigitalAssetVO asset : assetVOList)
			{
				if(asset.getAssetFileName().equalsIgnoreCase(newName))
				{
					digitalAsset = asset;

					digitalAsset.setAssetContentType(contentType);
					digitalAsset.setAssetFileName(newName);
					digitalAsset.setAssetFilePath("");
					digitalAsset.setAssetFileSize(length.intValue());
					if(newName.lastIndexOf(".") > -1)
						digitalAsset.setAssetKey(newName.substring(0,newName.lastIndexOf(".")));
					else
						digitalAsset.setAssetKey(newName);
						
					digitalAsset = DigitalAssetController.update(digitalAsset, inputStream);
				}
			}

			if(digitalAsset == null)
			{
				digitalAsset = new DigitalAssetVO();
				digitalAsset.setAssetContentType(contentType);
				digitalAsset.setAssetFileName(newName);
				digitalAsset.setAssetFilePath("");
				digitalAsset.setAssetFileSize(length.intValue());
				if(newName.lastIndexOf(".") > -1)
					digitalAsset.setAssetKey(newName.substring(0,newName.lastIndexOf(".")));
				else
					digitalAsset.setAssetKey(newName);
					
				DigitalAssetVO asset = DigitalAssetController.create(digitalAsset, inputStream, this.contentVersion.getId(), getInfoGluePrincipal());
			}
			if(logger.isInfoEnabled())
				logger.info("digitalAsset:" + digitalAsset);
			resource = new DigitalAssetResource(digitalAsset, this.contentVersion);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new IOException("Could not open file:" + e.getMessage());
		}
		return resource;
	}

	@Override
	public void copyTo(CollectionResource arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete() throws NotAuthorizedException, ConflictException,
			BadRequestException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Long getContentLength() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContentType(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getMaxAgeSeconds(Auth arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendContent(OutputStream arg0, Range arg1, Map<String, String> arg2, String arg3) throws IOException, NotAuthorizedException, BadRequestException 
	{
		
	}

	@Override
	public void moveTo(CollectionResource arg0, String arg1)
			throws ConflictException {
		// TODO Auto-generated method stub
		
	}

}
