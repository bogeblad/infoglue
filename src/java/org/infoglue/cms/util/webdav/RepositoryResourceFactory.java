package org.infoglue.cms.util.webdav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SystemUserController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

public class RepositoryResourceFactory implements ResourceFactory 
{
	private final static Logger logger = Logger.getLogger(RepositoryResourceFactory.class.getName());

	public static final String REALM = "infoglue";
	
	
	public RepositoryResourceFactory() 
	{
	}	
	
	@Override
	public Resource getResource(String host, String p) 
	{		
		Path path = Path.path(p).getStripFirst();
		
		Resource resource = InfogluePathResolver.resolvePath(path, this);
		
		if(logger.isInfoEnabled())
			logger.info("Returning resource:" + resource);
		
		return resource;
	}

	public List<Resource> findAllRepositories() 
	{
		if(logger.isInfoEnabled())
			logger.info("Showing all repositories");
		List<Resource> list = new ArrayList<Resource>();
		RepositoryVO repository;
		try 
		{
			List<RepositoryVO> repositories = RepositoryController.getController().getRepositoryVOList();
			
			if(logger.isInfoEnabled())
				logger.info("repositories:" + repositories.size());
			
			Iterator<RepositoryVO> repositoriesIterator = repositories.iterator();
			while(repositoriesIterator.hasNext())
			{
				RepositoryVO repositoryVO = repositoriesIterator.next();
				//System.out.println("repositoryVO:" + repositoryVO.getName());
				list.add( new RepositoryResource(repositoryVO) );
			}
		} 
		catch (ConstraintException e) 
		{
			e.printStackTrace();
		} 
		catch (SystemException e) 
		{
			e.printStackTrace();
		}
		
		return list;
	}
	
	public Resource findRepository(String name, InfoGluePrincipal principal) 
	{
		RepositoryResource resource = null;
		
		if(logger.isInfoEnabled())
			logger.info("find repository:" + name);
		
		RepositoryVO repository = null;
		try 
		{
			repository = RepositoryController.getController().getRepositoryVOWithName(name);
	        boolean hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(principal, "Repository.Read", repository.getId().toString()); 
	        resource = new RepositoryResource(repository);
	        if(!hasAccess)
	        	throw new NotAuthorizedException(resource);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return resource;
	}

	public String getSupportedLevels() 
	{
        return "1,2";
    }
}
