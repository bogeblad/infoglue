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
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.AuthenticationModule;
import org.infoglue.cms.security.InfoGluePrincipal;

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

public class RepositoryResource implements PropFindableResource, FolderResource
{
	private final static Logger logger = Logger.getLogger(RepositoryResource.class.getName());

	private final RepositoryVO repository;
	private InfoGluePrincipal principal = null;
	
	public RepositoryResource(RepositoryVO repository) 
	{
		this.repository = repository;
	}	
	
	/*
	@Override
	public Date getCreateDate() {
		// Unknown
		return null;
	}
	*/

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

	public boolean authorise(Request arg0, Method arg1, Auth arg2) 
	{
		if(logger.isInfoEnabled())
			logger.info("authorise user in represource:" + this.principal + ":" + arg2.getTag() + ":" + arg2.getUser());
		try 
		{
			boolean hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(this.principal, "Repository.Read", repository.getId().toString());
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
		return repository.getName();
	}

	public String getRealm() {
		return "infoglue";
	}

	public String getUniqueId() {
		return repository.getId().toString();
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
			logger.info("Looking for children on " + this.repository.getId() + ":" + this.repository.getName());
		
		List<ContentResource> contentChildren = new ArrayList<ContentResource>();
		try 
		{
			ContentVO contentVO = ContentController.getContentController().getRootContentVO(this.repository.getId(), "administrator");
			if(logger.isInfoEnabled())
				logger.info("contentVO:" + contentVO);
			
			contentChildren.add( new ContentResource(contentVO) );
		} 
		catch (Exception e) 
		{
			e.printStackTrace();	
		}
		if(logger.isInfoEnabled())
			logger.info("contentChildren:" + contentChildren.size());
		
		return contentChildren;
	}

	public CollectionResource createCollection(String arg0)
			throws NotAuthorizedException, ConflictException {
		// TODO Auto-generated method stub
		return null;
	}

	public Resource createNew(String arg0, InputStream arg1, Long arg2,
			String arg3) throws IOException, ConflictException {
		// TODO Auto-generated method stub
		return null;
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

	public void sendContent(OutputStream arg0, Range arg1,
			Map<String, String> arg2, String arg3) throws IOException,
			NotAuthorizedException, BadRequestException {
		// TODO Auto-generated method stub
	}

	public void moveTo(CollectionResource arg0, String arg1)
			throws ConflictException {
		// TODO Auto-generated method stub
	}

	public Date getCreateDate() {
		// TODO Auto-generated method stub
		return null;
	}
}
