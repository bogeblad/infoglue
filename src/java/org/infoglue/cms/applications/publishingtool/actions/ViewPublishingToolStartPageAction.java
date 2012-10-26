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

package org.infoglue.cms.applications.publishingtool.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.AccessRightVO;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.publishing.PublicationDetail;
import org.infoglue.cms.entities.publishing.PublicationDetailVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationDetailImpl;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ChangeNotificationController;
import org.infoglue.cms.util.DateHelper;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.cms.util.RemoteCacheUpdater;
import org.infoglue.deliver.util.SelectiveLivePublicationThread;

/**
 * This class implements the action class for the startpage in the management tool.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewPublishingToolStartPageAction extends InfoGlueAbstractAction
{
    public final static Logger logger = Logger.getLogger(ViewPublishingToolStartPageAction.class.getName());

	private static final long serialVersionUID = 1L;

    private List repositories;

    public String doV3() throws Exception
    {
    	doExecute();
    	
        return "successV3";
    }

    public String doExecute() throws Exception
    {
    	this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
    	
        return "success";
    }
    
    public String doPushSystemNotificationMessages() throws Exception
    {
        List<NotificationMessage> notificationMessagesToSend = new ArrayList<NotificationMessage>();

	    PublicationVO publicationVO = new PublicationVO();
	    publicationVO.setName("System notification");
	    publicationVO.setDescription("Access rights publication");
		List<PublicationDetailVO> publicationDetailVOList = new ArrayList<PublicationDetailVO>();

    	//NotificationMessage notificationMessage = null;
        List<NotificationMessage> messages = RemoteCacheUpdater.getSystemNotificationMessages();
        synchronized(messages)
        {
        	if(messages.size() > 0)
        	{
	        	boolean filledOtherQuota = false;
		        List<String> processedEntities = new ArrayList<String>();

	        	for(NotificationMessage message : messages)
	        	{
	        		if(message.getClassName().indexOf("AccessRightImpl") > -1)
	        		{
	        			try
				    	{
	        				AccessRightVO acVO;
	        				InterceptionPointVO icpVO;
	        				try
	        				{
						    	acVO = AccessRightController.getController().getAccessRightVOWithId((Integer)message.getObjectId());
						    	icpVO = InterceptionPointController.getController().getInterceptionPointVOWithId(acVO.getInterceptionPointId());
	        				}
	        				catch (Exception e) 
	        				{
	        					logger.info("No access right found", e);
	        					continue;
							}
	        				
	        				if(acVO != null && icpVO != null && !processedEntities.contains("" + icpVO.getCategory() + "_" + acVO.getParameters()))
					    	{
						    	//logger.info("icpVO:" + icpVO.getName());
						    	if(icpVO.getName().indexOf("Content.") > -1)
						    	{
						    		//logger.info("Was a content access... let's clear caches for that content.");
						    		String idAsString = acVO.getParameters();
						    		if(idAsString != null && !idAsString.equals(""))
						    		{
						    			ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(idAsString));
						    		    publicationVO.setRepositoryId(new Integer(contentVO.getRepositoryId()));

						    			PublicationDetailVO publicationDetailVO = new PublicationDetailVO();
						    			publicationDetailVO.setCreationDateTime(DateHelper.getSecondPreciseDate());
						    			publicationDetailVO.setDescription("Access rights change publication");
						    			publicationDetailVO.setEntityClass("org.infoglue.cms.entities.content.Content");
						    			publicationDetailVO.setEntityId(contentVO.getId());
						    			publicationDetailVO.setName("" + contentVO.getName());
						    			publicationDetailVO.setTypeId(PublicationDetailVO.PUBLISH);
						    			publicationDetailVO.setCreator(this.getInfoGluePrincipal().getName());

						    			publicationDetailVOList.add(publicationDetailVO);
						    		}
						    	}
						    	else if(icpVO.getName().indexOf("ContentVersion.") > -1)
						    	{
						    		//logger.info("Was a contentversion access... let's clear caches for that content.");
						    		String idAsString = acVO.getParameters();
						    		if(idAsString != null && !idAsString.equals(""))
						    		{
						    			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(new Integer(idAsString));
						    			ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(contentVersionVO.getContentId()));
						    		    publicationVO.setRepositoryId(new Integer(contentVO.getRepositoryId()));

						    			PublicationDetailVO publicationDetailVO = new PublicationDetailVO();
						    			publicationDetailVO.setCreationDateTime(DateHelper.getSecondPreciseDate());
						    			publicationDetailVO.setDescription("Access rights change publication");
						    			publicationDetailVO.setEntityClass("org.infoglue.cms.entities.content.ContentVersion");
						    			publicationDetailVO.setEntityId(contentVersionVO.getId());
						    			publicationDetailVO.setName("" + contentVO.getName() + "/" + contentVersionVO.getId());
						    			publicationDetailVO.setTypeId(PublicationDetailVO.PUBLISH);
						    			publicationDetailVO.setCreator(this.getInfoGluePrincipal().getName());

						    			publicationDetailVOList.add(publicationDetailVO);

						    			PublicationDetailVO publicationDetailVO2 = new PublicationDetailVO();
						    			publicationDetailVO2.setCreationDateTime(DateHelper.getSecondPreciseDate());
						    			publicationDetailVO2.setDescription("Access rights change publication");
						    			publicationDetailVO2.setEntityClass("org.infoglue.cms.entities.content.Content");
						    			publicationDetailVO2.setEntityId(contentVO.getId());
						    			publicationDetailVO2.setName("" + contentVO.getName());
						    			publicationDetailVO2.setTypeId(PublicationDetailVO.PUBLISH);
						    			publicationDetailVO2.setCreator(this.getInfoGluePrincipal().getName());

						    			publicationDetailVOList.add(publicationDetailVO2);
						    		}
						    	}
								else if(icpVO.getName().indexOf("SiteNodeVersion.") > -1)
								{
									//logger.info("Was a sitenode version access... let's clear caches for that siteNodeVersion.");
						    		String idAsString = acVO.getParameters();
						    		if(idAsString != null && !idAsString.equals(""))
						    		{
						    			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(new Integer(idAsString));
						    			SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(new Integer(siteNodeVersionVO.getSiteNodeId()));
						    		    publicationVO.setRepositoryId(new Integer(siteNodeVO.getRepositoryId()));

						    			PublicationDetailVO publicationDetailVO = new PublicationDetailVO();
						    			publicationDetailVO.setCreationDateTime(DateHelper.getSecondPreciseDate());
						    			publicationDetailVO.setDescription("Access rights change publication");
						    			publicationDetailVO.setEntityClass("org.infoglue.cms.entities.structure.SiteNode");
						    			publicationDetailVO.setEntityId(siteNodeVO.getId());
						    			publicationDetailVO.setName("" + siteNodeVO.getName());
						    			publicationDetailVO.setTypeId(PublicationDetailVO.PUBLISH);
						    			publicationDetailVO.setCreator(this.getInfoGluePrincipal().getName());

						    			publicationDetailVOList.add(publicationDetailVO);

						    			PublicationDetailVO publicationDetailVO2 = new PublicationDetailVO();
						    			publicationDetailVO2.setCreationDateTime(DateHelper.getSecondPreciseDate());
						    			publicationDetailVO2.setDescription("Access rights change publication");
						    			publicationDetailVO2.setEntityClass("org.infoglue.cms.entities.structure.SiteNodeVersion");
						    			publicationDetailVO2.setEntityId(siteNodeVersionVO.getId());
						    			publicationDetailVO2.setName("" + siteNodeVO.getName() + "/" + siteNodeVersionVO.getId());
						    			publicationDetailVO2.setTypeId(PublicationDetailVO.PUBLISH);
						    			publicationDetailVO2.setCreator(this.getInfoGluePrincipal().getName());

						    			publicationDetailVOList.add(publicationDetailVO2);}
								}
								else
								{
									//logger.info("****************************");
									//logger.info("* WHAT TO DO WITH: " + icpVO.getName() + " *");
									//logger.info("****************************");
								}
						    	//logger.info("Feeling done with " + "" + icpVO.getCategory() + "_" + acVO.getParameters());
						    	processedEntities.add("" + icpVO.getCategory() + "_" + acVO.getParameters());
					    	}
					    	else
					    	{
					    		//logger.info("Allready processed " + icpVO.getCategory() + "_" + acVO.getParameters());
					    	}
					    }
				    	catch(Exception e2)
				    	{
				    		logger.error("Error handling access right update: " + e2.getMessage(), e2);
				    	}
	        			//notificationMessagesToSend.add(message);
	        		}
	        		else if(!filledOtherQuota)
	        		{
	        	        //logger.info("Adding:" + message.getClassName());
	        			notificationMessagesToSend.add(message);
	        			filledOtherQuota = true;
	        		}
	        	}
        	}
        }
        
        logger.info("What other:" + notificationMessagesToSend.size());
        for(NotificationMessage message : notificationMessagesToSend)
        {
        	NotificationMessage notificationMessage = new NotificationMessage("ViewPublishingToolStartPageAction.doPushSystemNotificationMessages():", "" + message.getClassName(), this.getInfoGluePrincipal().getName(), NotificationMessage.SYSTEM, message.getObjectId(), message.getObjectName());
            ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
        }

        RemoteCacheUpdater.clearSystemNotificationMessages();
        
        if(publicationDetailVOList != null && publicationDetailVOList.size() > 0)
        {
        	logger.info("Sending out a publication with:" + publicationDetailVOList.size() + " details");
        	publicationVO = PublicationController.getController().createAndPublish(publicationVO, publicationDetailVOList, this.getInfoGluePrincipal().getName());
        }

        return doExecute() + "V3";
    }
    
    public List getRepositories()
    {
    	return this.repositories;
    }
     
    /**
     * Returns the events up for publishing.
     */
    public List getPublicationEvents(Integer repositoryId, String filter) throws SystemException, Exception
    {
    	List events = PublicationController.getPublicationEvents(repositoryId, getInfoGluePrincipal(), filter, false);

    	return events;
    }
    
    public List getSystemNotificationMessages()
    {
        return RemoteCacheUpdater.getSystemNotificationMessages();
    }
}
