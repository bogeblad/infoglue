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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.TransactionNotInProgressException;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.InfoGluePrincipalControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptorController;
import org.infoglue.cms.controllers.kernel.impl.simple.RedirectController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.TransactionHistoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.InterceptorVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.interceptors.SubscriptionsInterceptor;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.interceptors.InfoGlueInterceptor;
import org.infoglue.cms.util.ChangeNotificationController;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.cms.util.RemoteCacheUpdater;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
 * @author mattias
 *
 * This jobs searches for expiring contents or sitenodes and clears caches if found.
 */

public class SubscriptionsJob implements Job
{
    private final static Logger logger = Logger.getLogger(SubscriptionsJob.class.getName());
    private final static VisualFormatter vf = new VisualFormatter();
    
    public synchronized void execute(JobExecutionContext context) throws JobExecutionException
    {
    	logger.info("*********************************************************************");
    	logger.info("* Starting job which sends subscriptions when conditions are met    *");
    	logger.info("*********************************************************************");
		
    	InfoGluePrincipalControllerProxy.getController().getTestPrincipal();
    	
		try
		{
			logger.info("SubscriptionsJob...");
		    
			Map args = new HashMap();
		    args.put("globalKey", "infoglueSubscriptions");
		    PropertySet ps = PropertySetManager.getInstance("jdbc", args);

		    int numberOfDays = 30;
		    
		    cleanPropertySet(ps, numberOfDays);
		    
		    handleExpirationEvents(ps, numberOfDays);
		    
		    SubscriptionsInterceptor interceptor = new SubscriptionsInterceptor();
		    List<SubscriptionsInterceptor.TransactionQueueVO> processedTransactions = interceptor.processTransactionQueue();

		    registerDoneSubscriptionProcessing(ps, numberOfDays, processedTransactions);
		    
			logger.info("SubscriptionsJob ended...");
		    
		}
		catch(Exception e)
	    {
	    	logger.error("Could not notify subscribers: " + e.getMessage());
	    }
	   
	   	logger.info("Refresh-job finished");
    }


	private void registerDoneSubscriptionProcessing(PropertySet ps, int numberOfDays, List<SubscriptionsInterceptor.TransactionQueueVO> processedTransactions)
	{
		Iterator<SubscriptionsInterceptor.TransactionQueueVO> processedTransactionsIterator = processedTransactions.iterator();
		while(processedTransactionsIterator.hasNext())
		{
			SubscriptionsInterceptor.TransactionQueueVO transactionQueueVO = processedTransactionsIterator.next();
			if(transactionQueueVO.getInterceptionPointVO().getName().equalsIgnoreCase("Content.ExpireDateComingUp"))
			{
				String key = "content_" + transactionQueueVO.getTransactionObjectId() + "_" + numberOfDays + "_days_isProcessed";
				logger.info("Setting key so we don't get the same warning again: " + key);
			    ps.setString(key, vf.formatDate(new Date(), "yyyy-MM-dd"));
			}
			else if(transactionQueueVO.getInterceptionPointVO().getName().equalsIgnoreCase("SiteNode.ExpireDateComingUp"))
			{
				String key = "siteNode_" + transactionQueueVO.getTransactionObjectId() + "_" + numberOfDays + "_days_isProcessed";
				logger.info("Setting key so we don't get the same warning again: " + key);
			    ps.setString(key, vf.formatDate(new Date(), "yyyy-MM-dd"));
			}
		}
	}


	private void handleExpirationEvents(PropertySet ps, int numberOfDays) throws Exception, ConstraintException, SystemException, Bug
	{
		List<SiteNodeVO> upcomingSiteNodes = SiteNodeController.getController().getUpcomingExpiringSiteNodes(numberOfDays);
		Iterator<SiteNodeVO> upcomingSiteNodesIterator = upcomingSiteNodes.iterator();
		while(upcomingSiteNodesIterator.hasNext())
		{
			SiteNodeVO siteNodeVO = upcomingSiteNodesIterator.next();
			logger.info("siteNodeVO:" + siteNodeVO.getName() + " - " + siteNodeVO.getExpireDateTime());
			String key = "siteNode_" + siteNodeVO.getId() + "_" + numberOfDays + "_days_isProcessed";
			logger.info("key:" + key);
			String isProcessed = ps.getString(key);
		    if(isProcessed == null || isProcessed.equals(""))
		    {
		    	logger.info("The node " + siteNodeVO.getName() + " has not been processed.");
		    	
		    	Map data = new HashMap();
		    	data.put("siteNodeVO", siteNodeVO);
		    	
		    	intercept(data, "SiteNode.ExpireDateComingUp");
		    }
		}
		
		List upcomingContents = ContentController.getContentController().getUpcomingExpiringContents(numberOfDays);
		Iterator<ContentVO> upcomingContentIterator = upcomingContents.iterator();
		while(upcomingContentIterator.hasNext())
		{
			ContentVO contentVO = upcomingContentIterator.next();
			logger.info("contentVO:" + contentVO.getName() + " - " + contentVO.getExpireDateTime());
			String key = "content_" + contentVO.getId() + "_" + numberOfDays + "_days_isProcessed";
			logger.info("key:" + key);
			String dateProcessed = ps.getString(key);
		    if(dateProcessed == null || dateProcessed.equals(""))
		    {
		    	logger.info("The node " + contentVO.getName() + " has not been processed.");
		    	
		    	Map data = new HashMap();
		    	data.put("contentVO", contentVO);
		    	
		    	intercept(data, "Content.ExpireDateComingUp");
		    }
		}
	}


	private void cleanPropertySet(PropertySet ps, int numberOfDays)
	{
		Collection keys = ps.getKeys(null);
		Iterator keysIterator = keys.iterator();
		while(keysIterator.hasNext())
		{
			String key = (String)keysIterator.next();
			String value = ps.getString(key);
			logger.info("Found key:" + key + "=" + value);
			Date lastDate = vf.parseDate(value, "yyyy-MM-dd"); 
			Calendar removeDate = Calendar.getInstance();
			removeDate.add(Calendar.DAY_OF_YEAR, -(numberOfDays+10));
			if(lastDate.before(removeDate.getTime()))
			{
				logger.info("Removing key:" + key);
				ps.remove(key);
			}		    	
		}
	}
    
    
    protected void intercept(Map hashMap, String InterceptionPointName) throws ConstraintException, SystemException, Bug, Exception
	{
    	InfoGluePrincipal principal = UserControllerProxy.getController().getUser(CmsPropertyHandler.getAdministratorUserName());
		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName(InterceptionPointName);
    	
		if(interceptionPointVO == null)
			throw new SystemException("The InterceptionPoint " + InterceptionPointName + " was not found. The system will not work unless you restore it.");

		List interceptors = InterceptorController.getController().getInterceptorsVOList(interceptionPointVO.getInterceptionPointId());
		Iterator interceptorsIterator = interceptors.iterator();
		while(interceptorsIterator.hasNext())
		{
			InterceptorVO interceptorVO = (InterceptorVO)interceptorsIterator.next();
			logger.info("Adding interceptorVO:" + interceptorVO.getName());
			try
			{
				InfoGlueInterceptor infoGlueInterceptor = (InfoGlueInterceptor)Class.forName(interceptorVO.getClassName()).newInstance();
				infoGlueInterceptor.intercept(principal, interceptionPointVO, hashMap, false);
			}
			catch(ClassNotFoundException e)
			{
				logger.warn("The interceptor " + interceptorVO.getClassName() + "was not found: " + e.getMessage(), e);
			}
		}

	}
}
