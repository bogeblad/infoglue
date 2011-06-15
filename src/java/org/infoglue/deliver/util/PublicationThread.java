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

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.publishing.PublicationDetailVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.controllers.kernel.impl.simple.DigitalAssetDeliveryController;

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
		    
		    logger.info("\n\n\nSleeping " + publicationDelay + "ms.\n\n\n");
			sleep(publicationDelay);
		
		    logger.info("\n\n\nUpdating all caches as this was a publishing-update\n\n\n");
		    CacheController.clearCastorCaches();

		    logger.info("**************************************");
		    logger.info("*    HERE THE MAGIC SHOULD HAPPEN    *");
		    logger.info("**************************************");
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR, -1);
			List<PublicationVO> publicationVOList = PublicationController.getController().getPublicationsSinceDate(calendar.getTime());
			Iterator<PublicationVO> publicationVOListIterator = publicationVOList.iterator();
			while(publicationVOListIterator.hasNext())
			{
				PublicationVO publicationVO = publicationVOListIterator.next();
				
				List publicationDetailVOList = PublicationController.getController().getPublicationDetailVOList(publicationVO.getId());
				Iterator publicationDetailVOListIterator = publicationDetailVOList.iterator();
				while(publicationDetailVOListIterator.hasNext())
				{
					PublicationDetailVO publicationDetailVO = (PublicationDetailVO)publicationDetailVOListIterator.next();
					logger.info("publicationDetailVO.getEntityClass():" + publicationDetailVO.getEntityClass());
					logger.info("publicationDetailVO.getEntityId():" + publicationDetailVO.getEntityId());
					if(Class.forName(publicationDetailVO.getEntityClass()).getName().equals(ContentVersion.class.getName()))
					{
						logger.info("We clear all caches having references to contentVersion: " + publicationDetailVO.getEntityId());
						Integer contentId = ContentVersionController.getContentVersionController().getContentIdForContentVersion(publicationDetailVO.getEntityId());
						
						String disableAssetDeletionInLiveThread = CmsPropertyHandler.getDisableAssetDeletionInLiveThread();
						logger.info("disableAssetDeletionInLiveThread:" + disableAssetDeletionInLiveThread);
						if(disableAssetDeletionInLiveThread != null && !disableAssetDeletionInLiveThread.equals("true"))
						{
							List digitalAssetVOList = DigitalAssetController.getDigitalAssetVOList(publicationDetailVO.getEntityId());
							Iterator<DigitalAssetVO> digitalAssetVOListIterator = digitalAssetVOList.iterator();
				    		while(digitalAssetVOListIterator.hasNext())
				    		{
				    			DigitalAssetVO digitalAssetVO = digitalAssetVOListIterator.next();
								logger.info("We should delete all images with digitalAssetId " + digitalAssetVO.getId());
								DigitalAssetDeliveryController.getDigitalAssetDeliveryController().deleteDigitalAssets(digitalAssetVO.getId());
				    		}
						}
					}				
				}
			}
			
		    String[] excludedCaches = CacheController.getPublicationPersistentCacheNames();
			logger.info("\n\n\nclearing all except " + excludedCaches + " as we are in publish mode..\n\n\n");
			//CacheController.clearCaches(null, null, new String[] {"ServerNodeProperties", "serverNodePropertiesCache", "pageCache", "pageCacheExtra", "componentCache", "NavigationCache", "pagePathCache", "userCache", "pageCacheParentSiteNodeCache", "pageCacheLatestSiteNodeVersions", "pageCacheSiteNodeTypeDefinition", "JNDIAuthorizationCache", "WebServiceAuthorizationCache", "importTagResultCache"});
			CacheController.clearCaches(null, null, excludedCaches);
		    
			logger.info("\n\n\nRecaching all caches as this was a publishing-update\n\n\n");
			CacheController.cacheCentralCastorCaches();

			logger.info("\n\n\nFinally clearing page cache and other caches as this was a publishing-update\n\n\n");
			CacheController.clearCache("ServerNodeProperties");
			CacheController.clearCache("serverNodePropertiesCache");
		    CacheController.clearFileCaches("pageCache");
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
