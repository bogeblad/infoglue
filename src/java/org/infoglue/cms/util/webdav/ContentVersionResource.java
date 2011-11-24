package org.infoglue.cms.util.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.AuthenticationModule;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

public class ContentVersionResource implements PropFindableResource, FolderResource
{
	private final static Logger logger = Logger.getLogger(ContentVersionResource.class.getName());

	private InfoGluePrincipal principal = null;
	private final ContentVersionVO contentVersion;
	
	public ContentVersionResource(ContentVersionVO contentVersion) {
		this.contentVersion = contentVersion;
	}	
	
	public Date getCreateDate() {
		// Unknown
		return null;
	}

	public Object authenticate(String user, String pwd) 
	{
		if(logger.isInfoEnabled())
			logger.info("authenticate user in represource:" + user);

		try 
		{
		    Map loginMap = new HashMap();
	        loginMap.put("j_username", user);
	        loginMap.put("j_password", pwd);
			String authenticatedUserName = AuthenticationModule.getAuthenticationModule(null, null, null, true).authenticateUser(loginMap);
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
	
	public String checkRedirect(Request arg0) {
		// No redirects
		return null;
	}

	public Date getModifiedDate() {
		// Unknown
		return null;
	}

	public String getName() {
		return contentVersion.getLanguageName();
	}

	public String getRealm() {
		return "infoglue";
	}

	public String getUniqueId() {
		return contentVersion.getId().toString();
	}

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

	public void copyTo(CollectionResource arg0, String arg1) {
		// TODO Auto-generated method stub
	}

	public void delete() throws NotAuthorizedException, ConflictException,
			BadRequestException {
		// TODO Auto-generated method stub
	}

	public Long getContentLength() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getContentType(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Long getMaxAgeSeconds(Auth arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void sendContent(OutputStream arg0, Range arg1, Map<String, String> arg2, String arg3) throws IOException, NotAuthorizedException, BadRequestException 
	{
	}

	public void moveTo(CollectionResource arg0, String arg1)
			throws ConflictException {
		// TODO Auto-generated method stub
	}
}
