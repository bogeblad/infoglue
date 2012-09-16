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

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.LuceneController;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author mattias
 *
 * This jobs searches for expiring contents or sitenodes and clears caches if found.
 */

public class IndexContentsJob implements Job
{
    private final static Logger logger = Logger.getLogger(IndexContentsJob.class.getName());

    private static AtomicBoolean running = new AtomicBoolean(false);

    public synchronized void execute(JobExecutionContext context) throws JobExecutionException
    {
    	logger.info("*********************************************************************");
    	logger.info("* Starting content index job which should run with nice intervals 	 *");
    	logger.info("* Purpose is to keep the lucene index up2date				 		 *");
    	logger.info("*********************************************************************");

    	logger.info("running:" + running.get());

    	if (running.compareAndSet(false, true)) 
    	{
        	try
    		{
        		logger.info("*********************************************************************");
        		logger.info("Starting index contents...:" + CmsPropertyHandler.getApplicationName() + ":" + CmsPropertyHandler.getContextRootPath());
        		logger.info("*********************************************************************");
				try 
				{
					LuceneController.getController().notifyListeners(true, true); 
					LuceneController.getController().index();
				} 
				catch (Exception e) 
				{}
    		}
    		catch(Exception e)
    	    {
    	    	logger.error("Could not index everything: " + e.getMessage());
    	    }
    		finally
    		{
    			logger.info("Finished indexing ?????????????????????????????????????????");
    			running.set(false);
    		}
    	}	
    	else
    	{
        	logger.warn("IndexContentsJob allready running... skipping.");
    		return;
    	}
    	
	   	logger.info("Index-job finished");
    }
    
}
