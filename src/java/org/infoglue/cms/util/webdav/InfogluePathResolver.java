package org.infoglue.cms.util.webdav;

import org.apache.log4j.Logger;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Resource;

/**
 * This class is responsible for parsing and getting the resource in question. Not sure this is the way to go.
 * 
 * @author mattiasbogeblad
 *
 */

public class InfogluePathResolver 
{
	private final static Logger logger = Logger.getLogger(InfogluePathResolver.class.getName());

	public static Resource resolvePath(Path path, RepositoryResourceFactory resourceFactory)
	{
		Resource resource = null;
		
		int i=0;
		for(String part : path.getParts())
		{
			i++;
			
			if(logger.isInfoEnabled())
				logger.info("part:" + part + ":" + path.getParts().length + ":" + i);
			
			if(part.startsWith("webdavedit") || (part.startsWith("repositories") && i < 3))
				resource = new AllRepositoryResource(resourceFactory); 
			else
			{
				if(logger.isInfoEnabled())
					logger.info("resource:" + resource + ":" + (resource instanceof FolderResource));
				
				if(resource instanceof FolderResource)
				{
					FolderResource folderResource = (FolderResource)resource;
					resource = folderResource.child(part);
					if(logger.isInfoEnabled())
						logger.info("resource child:" + resource);
				}
				else if(resource instanceof FileResource)
				{
					if(logger.isInfoEnabled())
						logger.info("resource was a file:" + resource);
				}
			}
		}
		
		return resource;
	}
}
