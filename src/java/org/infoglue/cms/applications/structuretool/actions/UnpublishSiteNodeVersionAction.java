/* ===============================================================================
 *
 * Part of the InfoGlue SiteNode Management Platform (www.infoglue.org)
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

package org.infoglue.cms.applications.structuretool.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.EventController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;

/**
 *
 * @author Mattias Bogeblad
 * 
 * Present a list of siteNodeVersion under a given siteNode, recursing down in the hierarcy
 * 
 */

public class UnpublishSiteNodeVersionAction extends InfoGlueAbstractAction 
{
    private final static Logger logger = Logger.getLogger(UnpublishSiteNodeVersionAction.class.getName());

	private List<SiteNodeVersionVO> siteNodeVersionVOList = new ArrayList<SiteNodeVersionVO>();
	private List siteNodeVOList = new ArrayList();
	private Integer siteNodeId;
	private Integer siteNodeVersionId;
	private Integer repositoryId;

	private List siteNodeVersionIdList = new ArrayList();
	private Integer stateId;
	private Integer languageId;
	private String versionComment;
	private String attemptDirectPublishing = "false";
    private String originalAddress;
	private String returnAddress;
   	private String userSessionKey;
   	private String recipientFilter = null;
	private Boolean unpublishAll = true;
	

	public String doInput() throws Exception 
	{
		if(this.siteNodeId != null)
		{
		    SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(this.siteNodeId);
		    this.repositoryId = siteNodeVO.getRepositoryId();
		    
			if(this.siteNodeVersionId == null)
			{
			    SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getACLatestActiveSiteNodeVersionVO(this.getInfoGluePrincipal(), siteNodeId);
			    if(siteNodeVersionVO != null)
			        this.siteNodeVersionId = siteNodeVersionVO.getId();
			}

			AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
			Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId);
			if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "SiteNodeVersion.SubmitToPublish", protectedSiteNodeVersionId.toString()))
				ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeId", "1005"));
			
			ceb.throwIfNotEmpty();

			//siteNodeVersionVOList = SiteNodeVersionController.getController().getSiteNodeVersionVOWithParentRecursive(siteNodeId, SiteNodeVersionVO.PUBLISHED_STATE);
			siteNodeVersionVOList = SiteNodeVersionController.getController().getPublishedSiteNodeVersionVOWithParentRecursive(siteNodeId);

		}

	    return "input";
	}
	
	public String doInputChooseSiteNodes() throws Exception 
	{
		ProcessBean processBean = ProcessBean.createProcessBean(UnpublishSiteNodeVersionAction.class.getName(), "" + siteNodeId + "_" + getInfoGluePrincipal().getName());
		processBean.setStatus(ProcessBean.RUNNING);
		
		try
		{
		if(this.siteNodeId != null)
		{
		    SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(this.siteNodeId);
		    this.repositoryId = siteNodeVO.getRepositoryId();

			if(this.siteNodeVersionId == null)
			{
			    SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getACLatestActiveSiteNodeVersionVO(this.getInfoGluePrincipal(), siteNodeId);
			    if(siteNodeVersionVO != null)
			        this.siteNodeVersionId = siteNodeVersionVO.getId();
			}

			AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
			
			Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId);
			if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "SiteNodeVersion.SubmitToPublish", protectedSiteNodeVersionId.toString()))
				ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeId", "1005"));
			
			ceb.throwIfNotEmpty();

			siteNodeVOList = SiteNodeController.getController().getSiteNodeVOWithParentRecursive(siteNodeId, processBean);
		}
		}
		finally
		{
			processBean.setStatus(ProcessBean.FINISHED);
			processBean.removeProcess();
		}

	    return "inputChooseSiteNodes";
	}

	public String doInputChooseSiteNodesV3() throws Exception 
	{
		doInputChooseSiteNodes();

        userSessionKey = "" + System.currentTimeMillis();
        
        addActionLink(userSessionKey, new LinkBean("currentPageUrl", getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentPageLinkText"), getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentPageTitleText"), getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentPageTitleText"), this.originalAddress, false, ""));
        
        setActionExtraData(userSessionKey, "repositoryId", "" + this.repositoryId);
        setActionExtraData(userSessionKey, "siteNodeId", "" + this.siteNodeId);
        //setActionExtraData(userSessionKey, "siteNodeName", "" + siteNodeVersionVO);
        setActionExtraData(userSessionKey, "unrefreshedSiteNodeId", "" + this.siteNodeId);
        setActionExtraData(userSessionKey, "unrefreshedNodeId", "" + this.siteNodeId);
        setActionExtraData(userSessionKey, "changeTypeId", "1");

        setActionExtraData(userSessionKey, "disableCloseLink", "true");

	    return "inputChooseSiteNodesV3";
	}

	/**
	 * This method gets called when calling this action. 
	 * If the stateId is 2 which equals that the user tries to prepublish the page. If so we
	 * ask the user for a comment as this is to be regarded as a new version. 
	 */
	   
    public String doExecute() throws Exception
    {
		setSiteNodeVersionIdList( getRequest().getParameterValues("sel") );
		
		Map<Integer,SiteNodeVO> siteNodeMap = SiteNodeController.getController().getSiteNodeVOMapWithNoStateCheck(getSiteNodeVersionIdList());
		Map<Integer,ContentVO> contentMap = new HashMap<Integer,ContentVO>();

		Iterator it = getSiteNodeVersionIdList().iterator();
		
		List events = new ArrayList();
		while(it.hasNext())
		{
			Integer siteNodeVersionId = (Integer)it.next();
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getFullSiteNodeVersionVOWithId(siteNodeVersionId);

			SiteNodeVersionVO latestSiteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(siteNodeVersionVO.getSiteNodeId());
			SiteNodeVO siteNodeVO = siteNodeMap.get(siteNodeVersionVO.getId());
			if(siteNodeVO == null)
				siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVersionVO.getSiteNodeId());
			
			if(attemptDirectPublishing.equals("true"))
			{
				if(siteNodeVersionVO.getId().equals(latestSiteNodeVersionVO.getId()))
				{
					logger.info("Creating a new working version as there was no active working version left...");
					SiteNodeStateController.getController().changeState(siteNodeVersionVO.getId(), siteNodeVO, SiteNodeVersionVO.WORKING_STATE, "new working version", false, this.getInfoGluePrincipal(), events);
				}
			}
			
			EventVO eventVO = new EventVO();
			eventVO.setDescription(this.versionComment);
			eventVO.setEntityClass(SiteNodeVersion.class.getName());
			eventVO.setEntityId(siteNodeVersionId);
			eventVO.setName(siteNodeVersionVO.getSiteNodeName());
			eventVO.setTypeId(EventVO.UNPUBLISH_LATEST);
			eventVO = EventController.create(eventVO, this.repositoryId, this.getInfoGluePrincipal());
			events.add(eventVO);
			
			List contentVersionVOList = SiteNodeVersionController.getController().getMetaInfoContentVersionVOList(siteNodeVersionVO, siteNodeVO, this.getInfoGluePrincipal());
			Iterator contentVersionVOListIterator = contentVersionVOList.iterator();
			while(contentVersionVOListIterator.hasNext())
			{
			    ContentVersionVO currentContentVersionVO = (ContentVersionVO)contentVersionVOListIterator.next();
			    
				ContentVersionVO latestContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(currentContentVersionVO.getContentId(), currentContentVersionVO.getLanguageId());
				ContentVO contentVO = ContentController.getContentController().getContentVOWithId(currentContentVersionVO.getContentId());
				if(attemptDirectPublishing.equals("true"))
				{
					if(currentContentVersionVO.getId().equals(latestContentVersionVO.getId()))
					{
						logger.info("Creating a new working version as there was no active working version left...:" + currentContentVersionVO.getLanguageName());
						ContentStateController.changeState(currentContentVersionVO.getId(), ContentVersionVO.WORKING_STATE, "new working version", false, null, this.getInfoGluePrincipal(), currentContentVersionVO.getContentId(), events);
					}
				}
				
				EventVO versionEventVO = new EventVO();
				versionEventVO.setDescription(this.versionComment);
				versionEventVO.setEntityClass(ContentVersion.class.getName());
				versionEventVO.setEntityId(currentContentVersionVO.getId());
				versionEventVO.setName(contentVO.getName());
				versionEventVO.setTypeId(EventVO.UNPUBLISH_LATEST);
				versionEventVO = EventController.create(versionEventVO, this.repositoryId, this.getInfoGluePrincipal());
				events.add(versionEventVO);			    
			}
		}
		
		if(!attemptDirectPublishing.equalsIgnoreCase("true"))
		{
			if(recipientFilter != null && !recipientFilter.equals("") && events != null && events.size() > 0)
				PublicationController.mailPublishNotification(events, repositoryId, getInfoGluePrincipal(), recipientFilter, true);
		}

		if(attemptDirectPublishing.equalsIgnoreCase("true"))
		{
		    PublicationVO publicationVO = new PublicationVO();
		    publicationVO.setName("Direct publication by " + this.getInfoGluePrincipal().getName());
		    publicationVO.setDescription(getVersionComment());
		    //publicationVO.setPublisher(this.getInfoGluePrincipal().getName());
		    publicationVO.setRepositoryId(repositoryId);
		    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, siteNodeMap, contentMap, false, this.getInfoGluePrincipal());
		}
		
       	return "success";
    }

	/**
	 * This method will try to unpublish all liver versions of this sitenode. 
	 */
	   
    public String doUnpublishAll() throws Exception
    {
    	ProcessBean processBean = ProcessBean.createProcessBean(UnpublishSiteNodeVersionAction.class.getName(), "" + getInfoGluePrincipal().getName());
		processBean.setStatus(ProcessBean.RUNNING);

		try
		{
			String[] siteNodeIds = getRequest().getParameterValues("sel");
	
			List<EventVO> events = new ArrayList<EventVO>();
	
			List<Integer> siteNodeVersionIdList = new ArrayList<Integer>();
			for(int i=0; i < siteNodeIds.length; i++)
				siteNodeVersionIdList.add(new Integer(siteNodeIds[i]));
	
			Map<Integer,SiteNodeVO> siteNodeMap = SiteNodeController.getController().getSiteNodeVOMapWithNoStateCheck(siteNodeVersionIdList);
			Map<Integer,ContentVO> contentMap = new HashMap<Integer,ContentVO>();
	
			processBean.updateProcess("Found " + siteNodeMap.size() + " pages");

			//System.out.println("Read all siteNodes:" + siteNodeMap);
	
			/*
			for(Entry<Integer,SiteNodeVO> entry : siteNodeMap.entrySet())
			{
				Integer siteNodeVersionId = entry.getKey();
				logger.info("Publishing:" + siteNodeVersionId);
				SiteNodeVersionVO siteNodeVersion = SiteNodeStateController.getController().changeState(siteNodeVersionId, entry.getValue(), SiteNodeVersionVO.PUBLISH_STATE, getVersionComment(), this.overrideVersionModifyer, this.recipientFilter, this.getInfoGluePrincipal(), events);
			}
			*/
	
			processBean.updateProcess("Processing " + siteNodeIds.length + " pages");
			
	        for(int i=0; i < siteNodeIds.length; i++)
			{
	        	if (i % 10 == 0)
	        		processBean.updateLastDescription("Unpublished " + i + " pages");

	            String siteNodeIdString = siteNodeIds[i];
		        List siteNodeVersionVOList = SiteNodeVersionController.getController().getPublishedActiveSiteNodeVersionVOList(new Integer(siteNodeIdString));
		        
				Iterator it = siteNodeVersionVOList.iterator();
				
				while(it.hasNext())
				{
					SiteNodeVersionVO siteNodeVersionVO = (SiteNodeVersionVO)it.next();
					
					SiteNodeVersionVO latestSiteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(siteNodeVersionVO.getSiteNodeId());
					//SiteNodeVO siteNodeVO = siteNodeMap.get(siteNodeVersionVO.getId());
					//if(siteNodeVO == null)
					SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVersionVO.getSiteNodeId());
					
					if(attemptDirectPublishing.equals("true"))
					{
						if(siteNodeVersionVO.getId().equals(latestSiteNodeVersionVO.getId()))
						{
							logger.info("Creating a new working version as there was no active working version left...");
							SiteNodeVersionVO newSiteNodeVersionVO = SiteNodeStateController.getController().changeState(siteNodeVersionVO.getId(), siteNodeVO, SiteNodeVersionVO.WORKING_STATE, "new working version", false, this.getInfoGluePrincipal(), events);
							siteNodeMap.put(newSiteNodeVersionVO.getId(), siteNodeVO);
						}
					}
					
					EventVO eventVO = new EventVO();
					eventVO.setDescription(this.versionComment);
					eventVO.setEntityClass(SiteNodeVersion.class.getName());
					eventVO.setEntityId(siteNodeVersionVO.getId());
					eventVO.setName(siteNodeVO.getName());
					eventVO.setTypeId(EventVO.UNPUBLISH_LATEST);
					eventVO = EventController.create(eventVO, this.repositoryId, this.getInfoGluePrincipal());
					events.add(eventVO);
					
					List contentVersionVOList = SiteNodeVersionController.getController().getMetaInfoContentVersionVOList(siteNodeVersionVO, siteNodeVO, this.getInfoGluePrincipal());
					Iterator contentVersionVOListIterator = contentVersionVOList.iterator();
					while(contentVersionVOListIterator.hasNext())
					{
					    ContentVersionVO currentContentVersionVO = (ContentVersionVO)contentVersionVOListIterator.next();
					    
						ContentVersionVO latestContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(currentContentVersionVO.getContentId(), currentContentVersionVO.getLanguageId());
						ContentVO contentVO = ContentController.getContentController().getContentVOWithId(currentContentVersionVO.getContentId());
						contentMap.put(currentContentVersionVO.getId(), contentVO);
						if(attemptDirectPublishing.equals("true"))
						{
							if(currentContentVersionVO.getId().equals(latestContentVersionVO.getId()))
							{
								logger.info("Creating a new working version as there was no active working version left...:" + currentContentVersionVO.getLanguageName());
								ContentStateController.changeState(currentContentVersionVO.getId(), contentVO, ContentVersionVO.WORKING_STATE, "new working version", false, null, this.getInfoGluePrincipal(), currentContentVersionVO.getContentId(), events);
							}
							
							EventVO versionEventVO = new EventVO();
							versionEventVO.setDescription(this.versionComment);
							versionEventVO.setEntityClass(ContentVersion.class.getName());
							versionEventVO.setEntityId(currentContentVersionVO.getId());
							versionEventVO.setName(contentVO.getName());
							versionEventVO.setTypeId(EventVO.UNPUBLISH_LATEST);
							versionEventVO = EventController.create(versionEventVO, this.repositoryId, this.getInfoGluePrincipal());
							events.add(versionEventVO);			    
						}
					}
				}
			}
			
			if(!attemptDirectPublishing.equalsIgnoreCase("true"))
			{
				if(recipientFilter != null && !recipientFilter.equals("") && events != null && events.size() > 0)
					PublicationController.mailPublishNotification(events, repositoryId, getInfoGluePrincipal(), recipientFilter, true);
			}

			if(attemptDirectPublishing.equalsIgnoreCase("true"))
			{
			    PublicationVO publicationVO = new PublicationVO();
			    publicationVO.setName("Direct publication by " + this.getInfoGluePrincipal().getName());
			    publicationVO.setDescription(getVersionComment());
			    //publicationVO.setPublisher(this.getInfoGluePrincipal().getName());
			    publicationVO.setRepositoryId(repositoryId);
			    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, siteNodeMap, contentMap, false, this.getInfoGluePrincipal());
			}
		}
		finally
		{
			processBean.setStatus(ProcessBean.FINISHED);
			processBean.removeProcess();
		}
		
		if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
	        String arguments 	= "userSessionKey=" + userSessionKey + "&attemptDirectPublishing=" + attemptDirectPublishing + "&isAutomaticRedirect=false";
	        String messageUrl 	= returnAddress + (returnAddress.indexOf("?") > -1 ? "&" : "?") + arguments;
	        
	        this.getResponse().sendRedirect(messageUrl);
	        return NONE;
        }
        else
        {
        	return "success";
        }
    }


	/**
	 * This method will try to unpublish latest live versions of this sitenode. 
	 */
	   
    public String doUnpublishLatest() throws Exception
    {
    	ProcessBean processBean = ProcessBean.createProcessBean(UnpublishSiteNodeVersionAction.class.getName(), "" + getInfoGluePrincipal().getName());
		processBean.setStatus(ProcessBean.RUNNING);

		try
		{
			String[] siteNodeIds = getRequest().getParameterValues("sel");
	
			List<EventVO> events = new ArrayList<EventVO>();
	
			List<Integer> siteNodeVersionIdList = new ArrayList<Integer>();
			for(int i=0; i < siteNodeIds.length; i++)
				siteNodeVersionIdList.add(new Integer(siteNodeIds[i]));
	
			Map<Integer,SiteNodeVO> siteNodeMap = SiteNodeController.getController().getSiteNodeVOMapWithNoStateCheck(siteNodeVersionIdList);
			Map<Integer,ContentVO> contentMap = new HashMap<Integer,ContentVO>();
	
			processBean.updateProcess("Found " + siteNodeMap.size() + " pages");	
			processBean.updateProcess("Processing " + siteNodeIds.length + " pages");
			
	        for(int i=0; i < siteNodeIds.length; i++)
			{
	        	if (i % 10 == 0)
	        		processBean.updateLastDescription("Unpublished " + i + " pages");

	            String siteNodeIdString = siteNodeIds[i];
	            SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestPublishedSiteNodeVersionVO(new Integer(siteNodeIdString));
	            
	            if(siteNodeVersionVO != null)
		        {
		            SiteNodeVersionVO latestSiteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(siteNodeVersionVO.getSiteNodeId());
					//SiteNodeVO siteNodeVO = siteNodeMap.get(siteNodeVersionVO.getId());
					//if(siteNodeVO == null)
					SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVersionVO.getSiteNodeId());
					
					if(attemptDirectPublishing.equals("true"))
					{
						if(siteNodeVersionVO.getId().equals(latestSiteNodeVersionVO.getId()))
						{
							logger.info("Creating a new working version as there was no active working version left...");
							SiteNodeVersionVO newSiteNodeVersionVO = SiteNodeStateController.getController().changeState(siteNodeVersionVO.getId(), siteNodeVO, SiteNodeVersionVO.WORKING_STATE, "new working version", false, this.getInfoGluePrincipal(), events);
							siteNodeMap.put(newSiteNodeVersionVO.getId(), siteNodeVO);
						}
					}
					
					EventVO eventVO = new EventVO();
					eventVO.setDescription(this.versionComment);
					eventVO.setEntityClass(SiteNodeVersion.class.getName());
					eventVO.setEntityId(siteNodeVersionVO.getId());
					eventVO.setName(siteNodeVO.getName());
					eventVO.setTypeId(EventVO.UNPUBLISH_LATEST);
					eventVO = EventController.create(eventVO, this.repositoryId, this.getInfoGluePrincipal());
					events.add(eventVO);
					
					List contentVersionVOList = SiteNodeVersionController.getController().getMetaInfoContentVersionVOList(siteNodeVersionVO, siteNodeVO, this.getInfoGluePrincipal());
					Iterator contentVersionVOListIterator = contentVersionVOList.iterator();
					while(contentVersionVOListIterator.hasNext())
					{
					    ContentVersionVO currentContentVersionVO = (ContentVersionVO)contentVersionVOListIterator.next();
					    
						ContentVersionVO latestContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(currentContentVersionVO.getContentId(), currentContentVersionVO.getLanguageId());
						ContentVO contentVO = ContentController.getContentController().getContentVOWithId(currentContentVersionVO.getContentId());
						contentMap.put(currentContentVersionVO.getId(), contentVO);
						if(attemptDirectPublishing.equals("true"))
						{
							if(currentContentVersionVO.getId().equals(latestContentVersionVO.getId()))
							{
								logger.info("Creating a new working version as there was no active working version left...:" + currentContentVersionVO.getLanguageId());
								ContentStateController.changeState(currentContentVersionVO.getId(), contentVO, ContentVersionVO.WORKING_STATE, "new working version", false, null, this.getInfoGluePrincipal(), currentContentVersionVO.getContentId(), events);
							}
							
							EventVO versionEventVO = new EventVO();
							versionEventVO.setDescription(this.versionComment);
							versionEventVO.setEntityClass(ContentVersion.class.getName());
							versionEventVO.setEntityId(currentContentVersionVO.getId());
							versionEventVO.setName(contentVO.getName());
							versionEventVO.setTypeId(EventVO.UNPUBLISH_LATEST);
							versionEventVO = EventController.create(versionEventVO, this.repositoryId, this.getInfoGluePrincipal());
							events.add(versionEventVO);			    
						}
					}
		        }
			}

			
			if(!attemptDirectPublishing.equalsIgnoreCase("true"))
			{
				if(recipientFilter != null && !recipientFilter.equals("") && events != null && events.size() > 0)
					PublicationController.mailPublishNotification(events, repositoryId, getInfoGluePrincipal(), recipientFilter, true);
			}

			if(attemptDirectPublishing.equalsIgnoreCase("true"))
			{
			    PublicationVO publicationVO = new PublicationVO();
			    publicationVO.setName("Direct publication by " + this.getInfoGluePrincipal().getName());
			    publicationVO.setDescription(getVersionComment());
			    //publicationVO.setPublisher(this.getInfoGluePrincipal().getName());
			    publicationVO.setRepositoryId(repositoryId);
			    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, siteNodeMap, contentMap, false, this.getInfoGluePrincipal());
			}
		}
		finally
		{
			processBean.setStatus(ProcessBean.FINISHED);
			processBean.removeProcess();
		}
		
		if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
	        String arguments 	= "userSessionKey=" + userSessionKey + "&attemptDirectPublishing=" + attemptDirectPublishing + "&isAutomaticRedirect=false";
	        String messageUrl 	= returnAddress + (returnAddress.indexOf("?") > -1 ? "&" : "?") + arguments;
	        
	        this.getResponse().sendRedirect(messageUrl);
	        return NONE;
        }
        else
        {
        	return "success";
        }
    }

	public List getSiteNodeVersions()
	{
		return this.siteNodeVersionVOList;		
	}

	public List getSiteNodes()
	{
		return this.siteNodeVOList;		
	}

	public Integer getSiteNodeId() 
	{
		return siteNodeId;
	}

	public void setSiteNodeId(Integer siteNodeId) 
	{
		this.siteNodeId = siteNodeId;
	}

    public Integer getRepositoryId()
    {
        return repositoryId;
    }
    
    public java.lang.Integer getLanguageId()
    {
        return this.languageId;
    }
        
    public void setLanguageId(Integer languageId)
    {
	    this.languageId = languageId;
    }
                	
	public void setStateId(Integer stateId)
	{
		this.stateId = stateId;
	}

	public void setVersionComment(String versionComment)
	{
		this.versionComment = versionComment;
	}
	
	public String getVersionComment()
	{
		return this.versionComment;
	}
	
	public Integer getStateId()
	{
		return this.stateId;
	}
    
	public SiteNodeVersionVO getLatestSiteNodeVersion(SiteNodeVO siteNode) throws Exception
	{
		return SiteNodeVersionController.getController().getLatestPublishedSiteNodeVersionVO(siteNode.getId());
	}
	
	/**
	 * @return
	 */
	public List getSiteNodeVersionIdList() 
	{
		return siteNodeVersionIdList;
	}

	/**
	 * @param list
	 */
	private void setSiteNodeVersionIdList(String[] list) 
	{
		if(list != null)
		{
		    for(int i=0; i < list.length; i++)
			{
				siteNodeVersionIdList.add(new Integer(list[i]));
			}		
		}
	}

	public void setAttemptDirectPublishing(String attemptDirectPublishing)
    {
        this.attemptDirectPublishing = attemptDirectPublishing;
    }
    
    public void setRepositoryId(Integer repositoryId)
    {
        this.repositoryId = repositoryId;
    }
    
    public Integer getSiteNodeVersionId()
    {
        return siteNodeVersionId;
    }
    
    public void setSiteNodeVersionId(Integer siteNodeVersionId)
    {
        this.siteNodeVersionId = siteNodeVersionId;
    }
    
	public String getOriginalAddress()
	{
		return originalAddress;
	}

	public void setOriginalAddress(String originalAddress)
	{
		this.originalAddress = originalAddress;
	}

	public String getReturnAddress() 
	{
		return returnAddress;
	}

	public void setReturnAddress(String returnAddress) 
	{
		this.returnAddress = returnAddress;
	}

	public String getUserSessionKey()
	{
		return userSessionKey;
	}

	public void setUserSessionKey(String userSessionKey)
	{
		this.userSessionKey = userSessionKey;
	}

	public ProcessBean getProcessBean()
	{
		return ProcessBean.getProcessBean(UnpublishSiteNodeVersionAction.class.getName(), "" + getInfoGluePrincipal().getName());
	}

	public String getStatusAsJSON()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<html><body>");
		
		try
		{
			ProcessBean processBean = getProcessBean();
			if(processBean != null && processBean.getStatus() != ProcessBean.FINISHED)
			{
				sb.append("<h2>" + getLocalizedString(getLocale(), "tool.structuretool.publicationProcess.publicationProcessInfo") + "</h2>");

				sb.append("<ol>");
				for(String event : processBean.getProcessEvents())
					sb.append("<li>" + event + "</li>");
				sb.append("</ol>");
				sb.append("<div style='text-align: center'><img src='images/loading.gif' /></div>");
			}
			else
			{
				sb.append("<script type='text/javascript'>hideProcessStatus();</script>");
			}
		}
		catch (Throwable t)
		{
			logger.error("Error when generating repository export status report as JSON.", t);
			sb.append(t.getMessage());
		}
		sb.append("</body></html>");
				
		return sb.toString();
	}
	
	public String doShowProcessesAsJSON() throws Exception
	{
		return "successShowProcessesAsJSON";
	}

	public String getRecipientFilter() 
	{
		return recipientFilter;
	}

	public void setRecipientFilter(String recipientFilter) 
	{
		this.recipientFilter = recipientFilter;
	}

	/**
	 * @return the unpublishAll
	 */
	public Boolean getUnpublishAll() {
		return unpublishAll;
	}

	/**
	 * @param unpublishAll the unpublishAll to set
	 */
	public void setUnpublishAll(Boolean unpublishAll) {
		this.unpublishAll = unpublishAll;
	}
}
