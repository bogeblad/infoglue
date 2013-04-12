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
package org.infoglue.deliver.util;

import java.util.Random;

import org.apache.log4j.Logger;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.cache.PageCacheHelper;

/**
 * @author mattias
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PublicationThread extends Thread
{

    public final static Logger logger = Logger.getLogger(PublicationThread.class.getName());

	public synchronized void run() 
	{
        logger.info("setting block");
        RequestAnalyser.getRequestAnalyser().setBlockRequests(true);

		try
		{
		    int publicationDelay = 5000;
		    String publicationThreadDelay = CmsPropertyHandler.getPublicationThreadDelay();
		    if(publicationThreadDelay != null && !publicationThreadDelay.equalsIgnoreCase("") && publicationThreadDelay.indexOf("publicationThreadDelay") == -1)
		        publicationDelay = Integer.parseInt(publicationThreadDelay);
		    
		    Random r = new Random();
		    int randint = (Math.abs(r.nextInt()) % 11) / 8 * 1000;
		    publicationDelay = publicationDelay + randint;

		    logger.info("\n\n\nSleeping " + publicationDelay + "ms.\n\n\n");
			sleep(publicationDelay);
		
		    logger.info("\n\n\nUpdating all caches as this was a publishing-update\n\n\n");
		    CacheController.clearCastorCaches();

		    String[] excludedCaches = CacheController.getPublicationPersistentCacheNames();
			logger.info("\n\n\nclearing all except " + excludedCaches + " as we are in publish mode..\n\n\n");
			//CacheController.clearCaches(null, null, new String[] {"ServerNodeProperties", "serverNodePropertiesCache", "pageCache", "pageCacheExtra", "componentCache", "NavigationCache", "pagePathCache", "userCache", "pageCacheParentSiteNodeCache", "pageCacheLatestSiteNodeVersions", "pageCacheSiteNodeTypeDefinition", "JNDIAuthorizationCache", "WebServiceAuthorizationCache", "importTagResultCache"});
			CacheController.clearCaches(null, null, excludedCaches);
		    
			logger.info("\n\n\nRecaching all caches as this was a publishing-update\n\n\n");
			CacheController.cacheCentralCastorCaches();

			logger.info("\n\n\nFinally clearing page cache and other caches as this was a publishing-update\n\n\n");
			CacheController.clearCache("ServerNodeProperties");
			CacheController.clearCache("serverNodePropertiesCache");
		    //CacheController.clearFileCaches("pageCache");
			PageCacheHelper.getInstance().clearPageCache();

			CacheController.clearCache("pageCache");
			CacheController.clearCache("pageCacheExtra");
		    CacheController.clearCache("componentCache");
		    CacheController.clearCache("NavigationCache");
		    CacheController.clearCache("pagePathCache");
		    CacheController.clearCache("pageCacheParentSiteNodeCache");
		    CacheController.clearCache("pageCacheLatestSiteNodeVersions");
		    CacheController.clearCache("pageCacheSiteNodeTypeDefinition");

		    CacheController.renameCache("newPagePathCache", "pagePathCache");
		} 
		catch (Exception e)
		{
		    logger.error("An error occurred in the PublicationThread:" + e.getMessage(), e);
		}

		logger.info("released block \n\n DONE---");
		RequestAnalyser.getRequestAnalyser().setBlockRequests(false);

	}
}
