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
 * ANY WARRANTY, including the ied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */

package org.infoglue.cms.interceptors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.BaseController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SubscriptionController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.SubscriptionVO;
import org.infoglue.cms.entities.management.TransactionHistoryVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.interceptors.InfoGlueInterceptor;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.mail.MailServiceFactory;


/**
 * @author Mattias Bogeblad
 *
 * This interceptor is used to handle that subscriptions get's delivered.
 */

public class SubscriptionsInterceptor extends BaseController implements InfoGlueInterceptor
{	
	public class TransactionQueueVO extends TransactionHistoryVO
	{
		private String subject;
		private String description;
		private InterceptionPointVO interceptionPointVO = null;

		public InterceptionPointVO getInterceptionPointVO()
		{
			return interceptionPointVO;
		}

		public void setInterceptionPointVO(InterceptionPointVO interceptionPointVO)
		{
			this.interceptionPointVO = interceptionPointVO;
		}

		public String getSubject()
		{
			return subject;
		}

		public void setSubject(String subject)
		{
			this.subject = subject;
		}

		public String getDescription()
		{
			return description;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}
		
		
	}
	
    private final static Logger logger = Logger.getLogger(SubscriptionsInterceptor.class.getName());
    private final static VisualFormatter vf = new VisualFormatter();
    
    private static List<TransactionQueueVO> transactionQueue = Collections.synchronizedList(new ArrayList<TransactionQueueVO>());
    
    public void addTransactionHistory(InterceptionPointVO interceptionPointVO, String name, String userName, Integer typeId, String objectName, String objectId, String subject, String description)
    {
    	TransactionQueueVO transVO = new TransactionQueueVO();
    	transVO.setInterceptionPointVO(interceptionPointVO);
        transVO.setName(name);
        transVO.setSystemUserName(userName);
        transVO.setTransactionDateTime(java.util.Calendar.getInstance().getTime());
        transVO.setTransactionTypeId(typeId);
        transVO.setTransactionObjectId(objectId);
        transVO.setTransactionObjectName(objectName);
    	transVO.setSubject(subject);
    	transVO.setDescription(description);
    	
    	synchronized (transactionQueue)
		{
    		logger.info("Adding transactionQueue:" + interceptionPointVO.getName() + " - " + objectName);
    		transactionQueue.add(transVO);
		}
    }
    
    public void intercept(InfoGluePrincipal infoGluePrincipal, InterceptionPointVO interceptionPointVO, Map extradata) throws ConstraintException, SystemException, Exception
	{
		intercept(infoGluePrincipal, interceptionPointVO, extradata, true);
	}
	
	/**
	 * This method will be called when a interceptionPoint is reached.
	 * 
	 * @param interceptionPoint
	 * @param extradata
	 * @throws ConstraintException
	 * @throws SystemException
	 */

	public void intercept(InfoGluePrincipal infoGluePrincipal, InterceptionPointVO interceptionPointVO, Map extradata, boolean allowCreatorAccess) throws ConstraintException, SystemException, Exception
	{
		Database db = CastorDatabaseService.getDatabase();
        
        try
        {
	        beginTransaction(db);
		         
	        intercept(infoGluePrincipal, interceptionPointVO, extradata, allowCreatorAccess, db);
	        
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
		}
	}

	
	/**
	 * This method will be called when a interceptionPoint is reached and it handle it within a transaction.
	 * 
	 * @param interceptionPoint
	 * @param extradata
	 * @throws ConstraintException
	 * @throws SystemException
	 */

	public void intercept(InfoGluePrincipal infoGluePrincipal, InterceptionPointVO interceptionPointVO, Map extradata, Database db) throws ConstraintException, SystemException, Exception
	{
		intercept(infoGluePrincipal, interceptionPointVO, extradata, true, db);
	}
	
	/**
	 * This method will be called when a interceptionPoint is reached and it handle it within a transaction.
	 * 
	 * @param interceptionPoint
	 * @param extradata
	 * @throws ConstraintException
	 * @throws SystemException
	 */

	public void intercept(InfoGluePrincipal infoGluePrincipal, InterceptionPointVO interceptionPointVO, Map extradata, boolean allowCreatorAccess, Database db) throws ConstraintException, SystemException, Exception
	{
		Locale userLocale = getUserPrefferedLocale(infoGluePrincipal.getName());

		String generalFooter = getLocalizedString(userLocale, "tool.common.subscriptionMail.generalFooter");

		String entityName = null;
		String entityId = null;
	
		/*
		if(interceptionPointVO.getName().equalsIgnoreCase("Content.Read"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			entityName = Content.class.getName();
			entityId = contentId.toString();
		}
		else */if(interceptionPointVO.getName().equalsIgnoreCase("Content.Write"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			entityName = Content.class.getName();
			entityId = contentId.toString();
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.Create"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			entityName = Content.class.getName();
			entityId = contentId.toString();
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.Delete"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			entityName = Content.class.getName();
			entityId = contentId.toString();
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.Move"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			entityName = Content.class.getName();
			entityId = contentId.toString();
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.CreateVersion"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			entityName = Content.class.getName();
			entityId = contentId.toString();
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.SubmitToPublish"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			entityName = Content.class.getName();
			entityId = contentId.toString();
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.ChangeAccessRights"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			entityName = Content.class.getName();
			entityId = contentId.toString();
		}
		/*
		else if(interceptionPointVO.getName().equalsIgnoreCase("ContentVersion.Read"))
		{
			Integer contentVersionId = (Integer)extradata.get("contentVersionId");
			entityName = ContentVersion.class.getName();
			entityId = contentVersionId.toString();
		}
		*/
		else if(interceptionPointVO.getName().equalsIgnoreCase("ContentVersion.Write"))
		{
			Integer contentVersionId = (Integer)extradata.get("contentVersionId");
			entityName = ContentVersion.class.getName();
			entityId = contentVersionId.toString();
			ContentVersionVO contentVersionVO = ContentVersionControllerProxy.getController().getContentVersionVOWithId(contentVersionId, db);
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentVersionVO.getContentId(), db);
			InterceptionPointVO contentInterceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Content.Write", db);

			String generalSubject = getLocalizedString(userLocale, "tool.common.subscriptionMail.generalSubject");
			String generalMessage = getLocalizedString(userLocale, "tool.common.subscriptionMail.generalMessage", entityName, entityId) + generalFooter;
			String subject = getLocalizedString(userLocale, "tool.common.subscriptionMail.contentUpdatedSubject");
			String message = getLocalizedString(userLocale, "tool.common.subscriptionMail.contentUpdatedMessage", contentVO.getName()) + generalFooter;

			addTransactionHistory(contentInterceptionPointVO, "SubscriptionsEvents", infoGluePrincipal.getName(), new Integer(999), Content.class.getName(), contentVersionVO.getContentId().toString(), subject, message);
			addTransactionHistory(interceptionPointVO, "SubscriptionsEvents", infoGluePrincipal.getName(), new Integer(999), entityName, entityId, generalSubject, generalMessage);
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("ContentVersion.Delete"))
		{
			Integer contentVersionId = (Integer)extradata.get("contentVersionId");
			entityName = ContentVersion.class.getName();
			entityId = contentVersionId.toString();
			ContentVersionVO contentVersionVO = ContentVersionControllerProxy.getController().getContentVersionVOWithId(contentVersionId, db);
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentVersionVO.getContentId(), db);
			InterceptionPointVO contentInterceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Content.Write", db); //TODO - is Content.Write correct
			addTransactionHistory(contentInterceptionPointVO, "SubscriptionsEvents", infoGluePrincipal.getName(), new Integer(999), Content.class.getName(), contentVersionVO.getContentId().toString(), "Subscription: Content deleted", "A user has changed the content '" + contentVO.getName() + "'. You subscribe to this event why this message is sent.");
			addTransactionHistory(interceptionPointVO, "SubscriptionsEvents", infoGluePrincipal.getName(), new Integer(999), entityName, entityId, "Subscription notification", "The entity " + entityName + " was deleted. You subscribe to this event why this message is sent.");
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.ExpireDateComingUp"))
		{
			ContentVO contentVO = (ContentVO)extradata.get("contentVO");
			entityName = ContentVersion.class.getName();
			entityId = contentVO.getId().toString();
			String fullPath = ContentController.getContentController().getContentPath(new Integer(entityId), true, true);
			addTransactionHistory(interceptionPointVO, "SubscriptionsEvents", infoGluePrincipal.getName(), new Integer(999), entityName, entityId, "Subscription notification", "The content \"" + fullPath + "\" will expire on " + vf.formatDate(contentVO.getExpireDateTime(), "yyyy-MM-dd HH:ss") + ". <hr/>You either subscribe to this event, created the content or was the last person to modify the content why this message is sent.");
		}
		/*
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.Read"))
		{
			Integer siteNodeVersionId = (Integer)extradata.get("siteNodeVersionId");
			entityName = SiteNodeVersion.class.getName();
			entityId = siteNodeVersionId.toString();
		}
		*/
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNode.ExpireDateComingUp"))
		{
			SiteNodeVO siteNodeVO = (SiteNodeVO)extradata.get("siteNodeVO");
			entityName = ContentVersion.class.getName();
			entityId = siteNodeVO.getId().toString();
			String fullPath = SiteNodeController.getController().getSiteNodePath(new Integer(entityId), db);
			addTransactionHistory(interceptionPointVO, "SubscriptionsEvents", infoGluePrincipal.getName(), new Integer(999), entityName, entityId, "Subscription notification", "The page \"" + fullPath + "\" will expire on " + vf.formatDate(siteNodeVO.getExpireDateTime(), "yyyy-MM-dd HH:ss") + ". <hr/>You either subscribe to this event, created the page or was the last person to modify the page why this message is sent.");
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.Write"))
		{
			Integer siteNodeVersionId = (Integer)extradata.get("siteNodeVersionId");
			entityName = SiteNodeVersion.class.getName();
			entityId = siteNodeVersionId.toString();
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.CreateSiteNode"))
		{
			Integer parentSiteNodeId = (Integer)extradata.get("siteNodeId");
			entityName = SiteNode.class.getName();
			entityId = parentSiteNodeId.toString();
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.DeleteSiteNode"))
		{
			Integer siteNodeId = (Integer)extradata.get("siteNodeId");
			entityName = SiteNode.class.getName();
			entityId = siteNodeId.toString();
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.MoveSiteNode"))
		{
			Integer siteNodeId = (Integer)extradata.get("siteNodeId");
			entityName = SiteNode.class.getName();
			entityId = siteNodeId.toString();
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.SubmitToPublish"))
		{
			Integer siteNodeVersionId = (Integer)extradata.get("siteNodeVersionId");
			entityName = SiteNodeVersion.class.getName();
			entityId = siteNodeVersionId.toString();
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.ChangeAccessRights"))
		{
			Integer siteNodeVersionId = (Integer)extradata.get("siteNodeVersionId");
			entityName = SiteNodeVersion.class.getName();
			entityId = siteNodeVersionId.toString();
		}
		
	}
	
	public List processTransactionQueue() throws Exception
	{
		List<TransactionQueueVO> completeTransactions = new ArrayList<TransactionQueueVO>();
		List<TransactionQueueVO> localTransactionQueue = new ArrayList<TransactionQueueVO>();
		
		synchronized (transactionQueue)
		{
			logger.info("Moving the transactions to this threads local queue...");
			localTransactionQueue.addAll(transactionQueue);
			transactionQueue.clear();
		}
		
		Iterator<TransactionQueueVO> localTransactionQueueIterator = localTransactionQueue.iterator();
		while(localTransactionQueueIterator.hasNext())
		{
			TransactionQueueVO transactionQueueVO = localTransactionQueueIterator.next();
			
			Database db = CastorDatabaseService.getDatabase();
	        
	        try
	        {
		        beginTransaction(db);
			         
				logger.info("InterceptionPointVO:" + transactionQueueVO.getInterceptionPointVO().getName());
				logger.info("	" + transactionQueueVO.getTransactionObjectName() + "=" + transactionQueueVO.getTransactionObjectId());
				List<SubscriptionVO> subscriptionVOList = SubscriptionController.getController().getSubscriptionVOList(transactionQueueVO.getInterceptionPointVO().getId(), null, false, transactionQueueVO.getTransactionObjectName(), transactionQueueVO.getTransactionObjectId(), null, null, db, true);
				logger.info("subscriptionVOList:" + subscriptionVOList.size());
				if(transactionQueueVO.getInterceptionPointVO().getName().equalsIgnoreCase("SiteNode.ExpireDateComingUp"))
				{
					logger.info("It's a expiredate coming up event... let's find add a fake subscription on the last modifier.");
					SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(new Integer(transactionQueueVO.getTransactionObjectId()), db);
					if(siteNodeVO != null)
					{
						SiteNodeVersionVO version = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(db, siteNodeVO.getId());

						SubscriptionVO creatorSubscriptionVO = new SubscriptionVO();
						creatorSubscriptionVO.setEntityId(transactionQueueVO.getTransactionObjectId());
						creatorSubscriptionVO.setEntityName(transactionQueueVO.getTransactionObjectName());
						creatorSubscriptionVO.setInterceptionPointId(transactionQueueVO.getInterceptionPointVO().getId());
						creatorSubscriptionVO.setName("Standard subscription");
						creatorSubscriptionVO.setUserName(siteNodeVO.getCreatorName());
						subscriptionVOList.add(creatorSubscriptionVO);

						if(version != null)
						{
							if(!version.getVersionModifier().equals(siteNodeVO.getCreatorName()))
							{
								SubscriptionVO modifyerSubscriptionVO = new SubscriptionVO();
								modifyerSubscriptionVO.setEntityId(transactionQueueVO.getTransactionObjectId());
								modifyerSubscriptionVO.setEntityName(transactionQueueVO.getTransactionObjectName());
								modifyerSubscriptionVO.setInterceptionPointId(transactionQueueVO.getInterceptionPointVO().getId());
								modifyerSubscriptionVO.setName("Standard subscription");
								modifyerSubscriptionVO.setUserName(version.getVersionModifier());
								subscriptionVOList.add(modifyerSubscriptionVO);
							}
						}
					}
				}
				else if(transactionQueueVO.getInterceptionPointVO().getName().equalsIgnoreCase("Content.ExpireDateComingUp"))
				{
					logger.info("It's a expiredate coming up event... let's find add a fake subscription on the last modifier.");
					ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(transactionQueueVO.getTransactionObjectId()), db);
					
					if(contentVO != null)
					{
						SubscriptionVO creatorSubscriptionVO = new SubscriptionVO();
						creatorSubscriptionVO.setEntityId(transactionQueueVO.getTransactionObjectId());
						creatorSubscriptionVO.setEntityName(transactionQueueVO.getTransactionObjectName());
						creatorSubscriptionVO.setInterceptionPointId(transactionQueueVO.getInterceptionPointVO().getId());
						creatorSubscriptionVO.setName("Standard subscription");
						creatorSubscriptionVO.setUserName(contentVO.getCreatorName());
						subscriptionVOList.add(creatorSubscriptionVO);
	
						List languages = LanguageController.getController().getLanguageVOList(contentVO.getRepositoryId(), db);
						Iterator languagesIterator = languages.iterator();
						while(languagesIterator.hasNext())
						{
							LanguageVO languageVO = (LanguageVO)languagesIterator.next();
							ContentVersionVO version = ContentVersionController.getContentVersionController().getLatestContentVersionVO(contentVO.getId(), languageVO.getId(), db);
							
							if(version != null)
							{							
								if(!version.getVersionModifier().equals(contentVO.getCreatorName()))
								{
									SubscriptionVO modifyerSubscriptionVO = new SubscriptionVO();
									modifyerSubscriptionVO.setEntityId(transactionQueueVO.getTransactionObjectId());
									modifyerSubscriptionVO.setEntityName(transactionQueueVO.getTransactionObjectName());
									modifyerSubscriptionVO.setInterceptionPointId(transactionQueueVO.getInterceptionPointVO().getId());
									modifyerSubscriptionVO.setName("Standard subscription");
									modifyerSubscriptionVO.setUserName(version.getVersionModifier());
									subscriptionVOList.add(modifyerSubscriptionVO);
								}
							}
						}
					}
				}
				
				Iterator<SubscriptionVO> subscriptionVOListIterator = subscriptionVOList.iterator();
				while(subscriptionVOListIterator.hasNext())
				{
					SubscriptionVO subscriptionVO = subscriptionVOListIterator.next();
					boolean subscriptionHandled = handleSubscription(subscriptionVO, transactionQueueVO, db);
					if(subscriptionHandled)
						completeTransactions.add(transactionQueueVO);
				}
	        
	            commitTransaction(db);
	        }
	        catch(Exception e)
	        {
	            logger.error("An error occurred so we should not complete the transaction:" + e, e);
				rollbackTransaction(db);
			}
		}
		
		logger.info("localTransactionQueue:" + localTransactionQueue.size());
		return completeTransactions;
	}

	private boolean handleSubscription(SubscriptionVO subscriptionVO, TransactionQueueVO transactionQueueVO, Database db) throws Exception
	{
		boolean handledSubscription = true;
		
		logger.info("subscriptionVO:" + subscriptionVO);
		if(subscriptionVO.getSubscriptionFilterVOList() == null || subscriptionVO.getSubscriptionFilterVOList().size() == 0)
		{
			//This first part handles simple subscriptions - that is subscriptions on an interception point with id or without filters
			String email = subscriptionVO.getUserEmail();
			if(email == null)
			{
				InfoGluePrincipal principal = UserControllerProxy.getController(db).getUser(subscriptionVO.getUserName());
				if(principal != null)
				{
					logger.info("principal:" + principal.getEmail());
					email = principal.getEmail();
				}
			}
			
			logger.info("Was a simple subscription without filters:" + email);
			
			if(email != null)
				MailServiceFactory.getService().sendEmail("text/html", CmsPropertyHandler.getSystemEmailSender(), email, null, null, null, null, transactionQueueVO.getSubject(), transactionQueueVO.getDescription(), "utf-8");
		}
		else
		{
			//This part handles more complex subscriptions. 
			
		}
		
		return handledSubscription;
	}

	public BaseEntityVO getNewVO()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
