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

package org.infoglue.deliver.cache;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.TransactionHistoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;
import org.infoglue.deliver.controllers.kernel.impl.simple.IntegrationDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.LiveInstanceMonitor;

/*
 *  This class keeps track of all live deliver states by polling them with regular intervals. 
 *  It also acts as a message queue so publication messages are resent if not successful the first time.
 */

public class MatchingContentsQueue implements Runnable
{
    private final static Logger logger = Logger.getLogger(MatchingContentsQueue.class.getName());

	private static MatchingContentsQueue singleton = null;
	
	private Map<String, MatchingContentsQueueBean> instanceMatchingContentsQueueBeans = new HashMap<String, MatchingContentsQueueBean>();
	private Map<String, String> instancePublicationQueueMeta = new HashMap<String, String>();
    
	private boolean keepRunning = true;
	
	private MatchingContentsQueue()
	{
	}

	/**
	 * Get the singleton and start the thread if not active
	 */
	public static MatchingContentsQueue getMatchingContentsQueue()
	{
		if(singleton == null)
		{
			singleton = new MatchingContentsQueue();
			Thread thread = new Thread (singleton);
			thread.setName("MatchingContentsQueue-Worker");
			thread.start();
		}
		
		return singleton;
	}
	
	public Map<String, MatchingContentsQueueBean> getInstanceMatchingContentsQueueBeans()
	{
		return instanceMatchingContentsQueueBeans;
	}
	
	/**
	 * This method gets when the queued beans for a specific instance.
	 */
	public MatchingContentsQueueBean getInstanceMatchingContentsQueueBeans(String cacheKey)
	{
		synchronized (instanceMatchingContentsQueueBeans) 
		{
			if(instanceMatchingContentsQueueBeans.containsKey(cacheKey))
				return instanceMatchingContentsQueueBeans.get(cacheKey);
			else
				return null;
		}		
	}

	/**
	 * This method allows you to add a publication queue bean and register it against a certain deliver instance.
	 * It makes sure each bean is unique.
	 */
	public void addMatchingContentsQueueBean(String cacheKey, MatchingContentsQueueBean bean)
	{

		//MatchingContentsQueueBean matchingContentsQueueBean = instanceMatchingContentsQueueBeans.get(cacheKey);
		//if(matchingContentsQueueBean == null)
		//{
		synchronized (instanceMatchingContentsQueueBeans) 
		{
			MatchingContentsQueueBean currentBean = instanceMatchingContentsQueueBeans.get(cacheKey);

			if (currentBean != null)
			{
				logger.info("There was a bean for the given cache key. Updating last fetched and sets the new value");
				bean.setLastFetched(currentBean.getLastFetched());
			}

			instanceMatchingContentsQueueBeans.put(cacheKey, bean);
		}
		//}
		/*
		synchronized(matchingContentsQueueBeans)
		{
			if(matchingContentsQueueBeans < 1000)
				matchingContentsQueueBeans.put(cacheKey, bean);
			else
				logger.error("Skipping queue for this bean as to many beans allready is in queue - must be something very wrong with the instance");
		}
		*/
		logger.info("Done...");
	}
	
	private void removeMatchingContentsQueueBean(String cacheKey)
	{
		logger.info("Removing url:" + cacheKey);
		synchronized (instanceMatchingContentsQueueBeans) 
		{
			instanceMatchingContentsQueueBeans.remove(cacheKey);
		}
	}

	/**
	 * Allows for manual clearing of a live instance queue.
	 */
	public void clearMatchingContentsQueueBean(String cacheKey)
	{
		logger.warn("Clearing queue manually for " + cacheKey);
		synchronized(instanceMatchingContentsQueueBeans)
		{
			instanceMatchingContentsQueueBeans.remove(cacheKey);
		}
		instancePublicationQueueMeta.put(cacheKey + "_manualClearTimestamp", "" + System.currentTimeMillis());
	}

	/**
	 * The thread runner - with each run it goes through all the queues and tries to call (POST) the deliver instance
	 * If the post fails the beans is kept in the queue and retried later if the instance is up at that time.  
	 */
	 
	public synchronized void run()
	{
		logger.info("Running HttpUniqueRequestQueue...");
		while(keepRunning)
		{
			try
			{
				logger.info("Running..: " + instanceMatchingContentsQueueBeans.size());
				if(instanceMatchingContentsQueueBeans.size() > 1000)
				{
					logger.warn("Too many objects in matching contents queue. Clearing queue");
					synchronized (instanceMatchingContentsQueueBeans) 
					{
						instanceMatchingContentsQueueBeans.clear();	
					}
				}

				Map<String, MatchingContentsQueueBean> localMatchingContentsQueueBeans = new HashMap<String, MatchingContentsQueueBean>();
				synchronized (instanceMatchingContentsQueueBeans) 
				{
					logger.info("About to copy beans to local list. Number of beans: " + instanceMatchingContentsQueueBeans.size());
					for (Map.Entry<String, MatchingContentsQueueBean> entry : instanceMatchingContentsQueueBeans.entrySet())
					{
						localMatchingContentsQueueBeans.put(entry.getKey(), entry.getValue());
					}
					logger.info("Done copying beans to local list. Number of beans in local list: " + localMatchingContentsQueueBeans.size());
				}

				Iterator<String> cacheKeysIterator = localMatchingContentsQueueBeans.keySet().iterator();
				while(cacheKeysIterator.hasNext())
				{
					String cacheKey = cacheKeysIterator.next();
					MatchingContentsQueueBean bean = localMatchingContentsQueueBeans.get(cacheKey);
					
					if(logger.isInfoEnabled())
						logger.info("MatchingContentsQueueBean cacheKey:" + cacheKey);
					
					boolean removeQueueBean = false;
					boolean forceRecache = false;
					
					try
					{
						long diff = (System.currentTimeMillis() - bean.getLastFetched()) / 1000;
	
						List cachedMatchingContents = null;
						DatabaseWrapper dbWrapperCached = new DatabaseWrapper(CastorDatabaseService.getDatabase());
						try
						{
							dbWrapperCached.getDatabase().begin();
							
							InfoGluePrincipal user = UserControllerProxy.getController(dbWrapperCached.getDatabase()).getUser(bean.getUserName());
							BasicTemplateController tc = new BasicTemplateController(dbWrapperCached, user);
							DeliveryContext deliveryContext = DeliveryContext.getDeliveryContext(false);
							tc.setDeliveryControllers(NodeDeliveryController.getNodeDeliveryController(null, null, null), null, null);	
							tc.setDeliveryContext(deliveryContext);
	
							cachedMatchingContents = tc.getMatchingContents(bean.getContentTypeDefinitionNames(), 
									   bean.getCategoryCondition(), 
									   bean.getFreeText(), 
									   bean.getFreeTextAttributeNamesList(), 
									   bean.getFromDate(), 
									   bean.getToDate(), 
									   bean.getExpireFromDate(),
									   bean.getExpireToDate(),
									   bean.getVersionModifier(), 
									   bean.getMaximumNumberOfItems(), 
									   true, 
									   true, 
									   bean.getCacheInterval(), 
									   bean.getCacheName(), 
									   bean.getCacheKey(), 
									   bean.getScheduleFetch(),
									   bean.getScheduleInterval(),
									   bean.getRepositoryIdsList(), 
									   bean.getLanguageId(), 
									   bean.getSkipLanguageCheck(), 
									   bean.getStartNodeId(),
									   bean.getSortColumn(),
									   bean.getSortOrder(), 
									   false,
									   bean.getValidateAccessRightsAsAnonymous(), 
									   true,
									   true);

							
							//This part check the matching contents cache for directives to recache... it is a cache entry marking when the cache-refresh request was made
							if(bean.getContentTypeDefinitionNames() != null && !bean.getContentTypeDefinitionNames().equals(""))
							{
								String[] contentTypeDefinitionNames = bean.getContentTypeDefinitionNames().split(",");
								for(String contentTypeDefinitionName : contentTypeDefinitionNames)
								{
									try
									{
										ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName(contentTypeDefinitionName, dbWrapperCached.getDatabase());
										if(contentTypeDefinitionVO != null)
										{
											logger.info("Do not throw page cache on this if it's not a content of type:" + contentTypeDefinitionVO.getName());
									    	String recacheMark = (String)CacheController.getCachedObjectFromAdvancedCache("matchingContentsCache", "recacheAllMark");
									    	logger.info("recacheMark:" + recacheMark);
									    	if(recacheMark == null)
									    		recacheMark = (String)CacheController.getCachedObjectFromAdvancedCache("matchingContentsCache", "recacheMark_" + contentTypeDefinitionVO.getId());
									    	
									    	logger.info("recacheMark:" + recacheMark);
									    	if(recacheMark != null)
									    	{
									    		long markTime = Long.parseLong(recacheMark);
									    		long diffMark = System.currentTimeMillis() - markTime;
									    		logger.info("It was " + diffMark + " since the recache directive was added.");
									    		logger.info("Bean was last fetched " + bean.getLastFetched() + ".");
									    		if(diffMark > 30000)
									    		{
									    			logger.info("Deleting the mark..");
									    			CacheController.clearCache("matchingContentsCache", "recacheMark_" + contentTypeDefinitionVO.getId());
									    		}
									    		else if(markTime > bean.getLastFetched())
									    		{
									    			logger.info("Forcing a recache as the mark was later than the last fetched.");
									    			forceRecache = true;
									    		}
									    		else
									    		{
									    			logger.info("Doing nothing:" + markTime + "/" + bean.getLastFetched() + "/" + diffMark);
									    		}
									    	}
									    }
									}
									catch (Exception e) 
									{
										logger.warn("Error reading content type: " + e.getMessage(), e);
									}
								}
							}
							else
							{
						    	String recacheMark = (String)CacheController.getCachedObjectFromAdvancedCache("matchingContentsCache", "recacheMark");
						    	logger.info("recacheMark:" + recacheMark);
						    	if(recacheMark != null)
						    	{
						    		long markTime = Long.getLong(recacheMark);
						    		long diffMark = System.currentTimeMillis() - markTime;
						    		logger.info("It was " + diffMark + " since the recache directive was added.");
						    		logger.info("Bean was last fetched " + bean.getLastFetched() + ".");
						    		if(diffMark > 3600000)
						    		{
						    			logger.info("Deleting the mark..");
						    			CacheController.clearCache("matchingContentsCache", "recacheMark");
						    		}
						    		else if(markTime > bean.getLastFetched())
						    		{
						    			logger.info("Forcing a recache as the mark was later than the last fetched.");
						    			forceRecache = true;
						    		}
						    		else
						    		{
						    			logger.info("Doing nothing:" + markTime + "/" + bean.getLastFetched() + "/" + diffMark);
						    		}
						    	}
							}
							//END TEST

							dbWrapperCached.getDatabase().rollback();
						}
						catch (Exception e) 
						{
							removeQueueBean = true;
							dbWrapperCached.getDatabase().rollback();
							logger.error("Error in matching contents:" + e.getMessage(), e);
						}
						finally
						{
							dbWrapperCached.getDatabase().close();
						}
												
						logger.info("diff:" + diff);
						logger.info("bean.getScheduleInterval()" + bean.getScheduleInterval());
						logger.info("Cached matches:" + (cachedMatchingContents == null ? "null" : cachedMatchingContents.size()));
						logger.info("removeQueueBean:" + removeQueueBean);

						logger.info("cachedMatchingContents:" + (cachedMatchingContents == null ? "null" : cachedMatchingContents.size()));

						if(!removeQueueBean && (diff > bean.getScheduleInterval() || cachedMatchingContents == null || forceRecache)) //|| cachedMatchingContents.size() == 0
						{
							logger.info("Running match either because the time was now or because no cached result was found or there was a recache directive in the cache");
							logger.info("forceRecache:" + forceRecache);
							logger.info("removeQueueBean:" + removeQueueBean);
							logger.info("diff:" + diff);
							logger.info("bean.getScheduleInterval():" + bean.getScheduleInterval());
								
							DatabaseWrapper dbWrapper = new DatabaseWrapper(CastorDatabaseService.getDatabase());
							try
							{
								dbWrapper.getDatabase().begin();
								
								InfoGluePrincipal user = UserControllerProxy.getController(dbWrapper.getDatabase()).getUser(bean.getUserName());
								BasicTemplateController tc = new BasicTemplateController(dbWrapper, user);
								DeliveryContext deliveryContext = DeliveryContext.getDeliveryContext(false);
								tc.setDeliveryControllers(NodeDeliveryController.getNodeDeliveryController(null, null, null), null, null);	
								tc.setDeliveryContext(deliveryContext);
								
								List matchingContents = tc.getMatchingContents(bean.getContentTypeDefinitionNames(), 
													   bean.getCategoryCondition(), 
													   bean.getFreeText(), 
													   bean.getFreeTextAttributeNamesList(), 
													   bean.getFromDate(), 
													   bean.getToDate(), 
													   bean.getExpireFromDate(),
													   bean.getExpireToDate(),
													   bean.getVersionModifier(), 
													   bean.getMaximumNumberOfItems(), 
													   true, 
													   true, 
													   bean.getCacheInterval(), 
													   bean.getCacheName(), 
													   bean.getCacheKey(), 
													   bean.getScheduleFetch(),
													   bean.getScheduleInterval(),
													   bean.getRepositoryIdsList(), 
													   bean.getLanguageId(), 
													   bean.getSkipLanguageCheck(), 
													   bean.getStartNodeId(),
													   bean.getSortColumn(),
													   bean.getSortOrder(), 
													   true,
													   bean.getValidateAccessRightsAsAnonymous(), 
													   false, 
													   true);
								
								if(bean.getContentTypeDefinitionNames() != null && !bean.getContentTypeDefinitionNames().equals(""))
								{
									String[] contentTypeDefinitionNames = bean.getContentTypeDefinitionNames().split(",");
									for(String contentTypeDefinitionName : contentTypeDefinitionNames)
									{
										try
										{
											ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName(contentTypeDefinitionName, dbWrapper.getDatabase());
											if(contentTypeDefinitionVO != null)
											{
												logger.info("Do not throw page cache on this if it's not a content of type:" + contentTypeDefinitionVO.getName());
												String contentTypeDefKey = "selectiveCacheUpdateNonApplicable_contentTypeDefinitionId_" + contentTypeDefinitionVO.getId();
									    		CacheController.clearCache("pageCache", contentTypeDefKey);
											}
										}
										catch (Exception e) 
										{
											logger.warn("Error reading content type: " + e.getMessage(), e);
										}
									}
								}
								else
								{
						    		CacheController.clearCache("pageCache", "selectiveCacheUpdateNonApplicable");
								}
								
								bean.setLastFetched(System.currentTimeMillis());
								
								logger.info("matchingContents in queue:" + matchingContents.size());
								
								dbWrapper.getDatabase().rollback();
							}
							catch (Exception e) 
							{
	//							cacheKeysIterator.remove();
								removeQueueBean = true;
								dbWrapper.getDatabase().rollback();
								logger.error("Error in matching contents:" + e.getMessage());
								logger.warn("Error in matching contents.", e);
							}
							finally
							{
								dbWrapper.getDatabase().close();
							}
						}
					}
					catch(Exception e)
					{
						/*
						synchronized (instanceMatchingContentsQueueBeans)
						{
							Map<String, Set<MatchingContentsQueueBean>> currentLiveInstanceMatchingContentsQueueBeans = instanceMatchingContentsQueueBeans;
							Set<MatchingContentsQueueBean> currentMatchingContentsQueueBeans = currentLiveInstanceMatchingContentsQueueBeans.get(serverBaseUrl);
							if(currentMatchingContentsQueueBeans == null)
							{
								currentMatchingContentsQueueBeans = new HashSet<MatchingContentsQueueBean>();
								currentLiveInstanceMatchingContentsQueueBeans.put(serverBaseUrl, currentMatchingContentsQueueBeans);
							}
							currentMatchingContentsQueueBeans.addAll(beans);
						}
						*/
						removeQueueBean = true;
						logger.error("Error updating cache at " + cacheKey + ". We skip further tries for now and queue it:" + e.getMessage());
						logger.warn("Error updating cache at " + cacheKey + ". We skip further tries for now and queue it.", e);
					}
					finally
					{
						if (removeQueueBean)
						{
							logger.info("Removing queue bean because it was making trouble. Cache key: " + cacheKey);
							//cacheKeysIterator.remove();
							removeMatchingContentsQueueBean(cacheKey);
						}
					}
				}

				try
				{
					Thread.sleep(5000);
			    }
				catch( InterruptedException e ) 
				{
					logger.error("Interrupted Exception caught");
			    }
			}
			catch (Throwable tr)
			{
				logger.error("Error in matching contents queue. Will catch and continous going. Type: " + tr.getClass() + ". Message: " + tr.getMessage());
				logger.warn("Error in matching contents queue. Will catch and continous going.", tr);
			}
		}
		logger.error("MATCHING CONTENT QUEUE STOPPED!");
	}

}
