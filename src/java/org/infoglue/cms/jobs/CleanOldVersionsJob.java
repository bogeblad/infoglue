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
package org.infoglue.cms.jobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.util.ChangeNotificationController;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.NotificationMessage;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author mattias
 *
 * This jobs searches for expiring contents or sitenodes and clears caches if found.
 */

public class CleanOldVersionsJob implements Job
{
    private final static Logger logger = Logger.getLogger(CleanOldVersionsJob.class.getName());

    private static AtomicBoolean running = new AtomicBoolean(false);
    
    public synchronized void execute(JobExecutionContext context) throws JobExecutionException
    {
    	logger.info("*********************************************************************");
    	logger.info("* Starting version cleanup job which should run with nice intervals *");
    	logger.info("* Purpose is to keep the database size at a minimum 				 *");
    	logger.info("*********************************************************************");
    	
    	if(running.compareAndSet(false, true))
    	{
	    	try
			{
	    		Boolean deleteVersions = (Boolean)context.get("deleteVersions");
	    		if(deleteVersions == null)
	    			deleteVersions = new Boolean(true);

	    		Integer redoNumberOfTimes = (Integer)context.get("redoNumberOfTimes");
	    		if(redoNumberOfTimes == null)
	    			redoNumberOfTimes = new Integer(2);

	    		Map<String,Integer> totalCleanedContentVersions = new HashMap<String,Integer>();
	    		//Map<String,Integer> totalCleanedSiteNodeVersions = new HashMap<String,Integer>();
	    		int cleanedSiteNodeVersions = 0;
	    		
				for(int i=0; i<redoNumberOfTimes; i++)
				{
					boolean anyDeletions = false;
					
		    		List<ContentTypeDefinitionVO> contentTypes = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
		    		for(ContentTypeDefinitionVO ctdVO : contentTypes)
		    		{
		    			Integer numberOfVersionsToKeepDuringCleanInteger = -1;
		    			try
		    			{
		    				numberOfVersionsToKeepDuringCleanInteger = new Integer(CmsPropertyHandler.getServerNodeProperty("contentTypeDefinitionId_" + ctdVO.getId() + "_versionsToKeep", false, "-1", true));
		    			}
		    			catch (Exception e) 
		    			{
		    				logger.warn("Could not get number of versions to keep:" + e.getMessage());
		    			}
		    			
		    			logger.info("numberOfVersionsToKeepDuringCleanInteger: " + numberOfVersionsToKeepDuringCleanInteger);
		    			logger.info("deleteVersions:" + deleteVersions);
				    	String keepOnlyOldPublishedVersionsString = CmsPropertyHandler.getKeepOnlyOldPublishedVersionsDuringClean();
				    	logger.info("keepOnlyOldPublishedVersionsString:" + keepOnlyOldPublishedVersionsString);
				    	long minimumTimeBetweenVersionsDuringClean = CmsPropertyHandler.getMinimumTimeBetweenVersionsDuringClean();
				    	logger.info("minimumTimeBetweenVersionsDuringClean:" + minimumTimeBetweenVersionsDuringClean);
				    	boolean keepOnlyOldPublishedVersions = Boolean.parseBoolean(keepOnlyOldPublishedVersionsString);
						
				    	if(numberOfVersionsToKeepDuringCleanInteger.intValue() < 3 && numberOfVersionsToKeepDuringCleanInteger.intValue() > -1)
							numberOfVersionsToKeepDuringCleanInteger = new Integer(3);
				    	
						if(numberOfVersionsToKeepDuringCleanInteger.intValue() > -1)
						{
							int cleanedContentVersions = ContentVersionController.getContentVersionController().cleanContentVersions(numberOfVersionsToKeepDuringCleanInteger.intValue(), keepOnlyOldPublishedVersions, minimumTimeBetweenVersionsDuringClean, deleteVersions, ctdVO.getId());
							if(deleteVersions && cleanedContentVersions > 0)
								anyDeletions = true;
							
							logger.info("cleanedContentVersions:" + cleanedContentVersions);
							if(totalCleanedContentVersions.get(ctdVO.getName()) != null)
								totalCleanedContentVersions.put(ctdVO.getName(), totalCleanedContentVersions.get(ctdVO.getName()) + cleanedContentVersions);
							else
								totalCleanedContentVersions.put(ctdVO.getName(), cleanedContentVersions);
							
							if(ctdVO.getName().equalsIgnoreCase("Meta information"))
							{
								cleanedSiteNodeVersions = SiteNodeController.getController().cleanSiteNodeVersions(numberOfVersionsToKeepDuringCleanInteger.intValue(), keepOnlyOldPublishedVersions, minimumTimeBetweenVersionsDuringClean, deleteVersions);
								if(deleteVersions && cleanedSiteNodeVersions > 0)
									anyDeletions = true;
								
								if(totalCleanedContentVersions.get(ctdVO.getName()) != null)
									totalCleanedContentVersions.put(ctdVO.getName(), totalCleanedContentVersions.get(ctdVO.getName()) + cleanedSiteNodeVersions);
								else
									totalCleanedContentVersions.put(ctdVO.getName(), cleanedSiteNodeVersions);
							}
						}
		    		}
		    		
	    			logger.info("anyDeletions:" + anyDeletions);
		    		if(!anyDeletions)
		    		{
		    			logger.info("No deletions made - done...");
		    			break;
		    		}
		    	}
	    		
				logger.info("totalCleanedContentVersions:" + totalCleanedContentVersions);
				context.setResult(totalCleanedContentVersions);
				
				NotificationMessage notificationMessage = new NotificationMessage("CleanOldVersionsJob.execute():", "ServerNodeProperties", "administrator", NotificationMessage.SYSTEM, "0", "ServerNodeProperties");
			    ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
	        	ChangeNotificationController.getInstance().notifyListeners();
			}
			catch(Exception e)
		    {
		    	logger.error("Could not clean up old versions: " + e.getMessage());
		    }
			finally
			{
				running.set(false);
			}
			
		   	logger.info("Cleanup-job finished");
		}
		else
		{
			System.out.println("CleanOldVersionsJob allready running... skipping.");
		}
    }	    
}
