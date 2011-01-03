package org.infoglue.cms.util.webdav;

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

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryLanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.exception.ConstraintException;
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
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

public class ContentResource implements PropFindableResource, FolderResource
{
	private final static Logger logger = Logger.getLogger(ContentResource.class.getName());

	private InfoGluePrincipal principal = null;
	private final ContentVO content;
	
	public ContentResource(ContentVO content) 
	{
		this.content = content;
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
			boolean hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(this.principal, "Content.Read", content.getId().toString());
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
		return content.getName();
	}

	@Override
	public String getRealm() {
		return "infoglue";
	}

	@Override
	public String getUniqueId() {
		return content.getId().toString();
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
			logger.info("Looking for children on content:" + this.content.getId() + ":" + this.content.getName());
		
		List<Resource> contentChildren = new ArrayList<Resource>();
		try 
		{
			List<LanguageVO> languageVOList = RepositoryLanguageController.getController().getAvailableLanguageVOListForRepositoryId(this.content.getRepositoryId());
			if(logger.isInfoEnabled())
				logger.info("languageVOList:" + languageVOList.size());
			for(LanguageVO languageVO : languageVOList)
			{
				ContentVersionVO cv = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(this.content.getId(), languageVO.getId());
				if(logger.isInfoEnabled())
					logger.info("cv:" + cv);
				
				if(cv != null)
				{
					cv.setLanguageName(languageVO.getName());
					contentChildren.add( new ContentVersionResource(cv) );					
				}
			}

			List<ContentVO> children = ContentController.getContentController().getContentChildrenVOList(content.getId(), null, false);
			if(logger.isInfoEnabled())
				logger.info("children:" + children.size());
			Iterator<ContentVO> childrenIterator = children.iterator();
			while(childrenIterator.hasNext())
			{
				ContentVO contentVO = childrenIterator.next();
	        	contentChildren.add( new ContentResource(contentVO) );
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		if(logger.isInfoEnabled())
			logger.info("contentChildren:" + contentChildren.size());
		
		return contentChildren;
	}

	@Override
	public CollectionResource createCollection(String arg0)
			throws NotAuthorizedException, ConflictException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource createNew(String arg0, InputStream arg1, Long arg2,
			String arg3) throws IOException, ConflictException {
		// TODO Auto-generated method stub
		return null;
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
	public void sendContent(OutputStream arg0, Range arg1,
			Map<String, String> arg2, String arg3) throws IOException,
			NotAuthorizedException, BadRequestException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveTo(CollectionResource arg0, String arg1)
			throws ConflictException {
		// TODO Auto-generated method stub
		
	}

}
