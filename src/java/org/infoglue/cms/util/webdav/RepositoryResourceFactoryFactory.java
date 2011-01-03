package org.infoglue.cms.util.webdav;

import java.util.List;

import org.apache.log4j.Logger;

import com.bradmcevoy.http.AuthenticationService;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.ResourceFactoryFactory;
import com.bradmcevoy.http.webdav.DefaultWebDavResponseHandler;
import com.bradmcevoy.http.webdav.WebDavResponseHandler;


public class RepositoryResourceFactoryFactory implements ResourceFactoryFactory 
{
	private final static Logger logger = Logger.getLogger(RepositoryResourceFactoryFactory.class.getName());

	private static AuthenticationService authenticationService;
	private static RepositoryResourceFactory resourceFactory;

	@Override
	public ResourceFactory createResourceFactory() {
		return resourceFactory;
	}

	@Override
	public WebDavResponseHandler createResponseHandler() {
		return new DefaultWebDavResponseHandler(authenticationService);
	}

	@Override
	public void init() 
	{
		
		if(logger.isInfoEnabled())
			logger.info("init ContentResourceFactoryFactory");
		
		if( authenticationService == null ) {
			authenticationService = new AuthenticationService(); 
			resourceFactory = new RepositoryResourceFactory();			
		}
	}

}
