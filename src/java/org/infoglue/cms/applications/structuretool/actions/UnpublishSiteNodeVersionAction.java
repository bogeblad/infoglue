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
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.EventController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
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

	private List siteNodeVersionVOList = new ArrayList();
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

	
	public String doInput() throws Exception 
	{
		if(this.siteNodeId != null)
		{
		    SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(this.siteNodeId);
		    this.repositoryId = siteNodeVO.getRepositoryId();
		    
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
		if(this.siteNodeId != null)
		{
		    SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(this.siteNodeId);
		    this.repositoryId = siteNodeVO.getRepositoryId();
		    
			AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
			Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId);
			if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "SiteNodeVersion.SubmitToPublish", protectedSiteNodeVersionId.toString()))
				ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeId", "1005"));
			
			ceb.throwIfNotEmpty();

			siteNodeVOList = SiteNodeController.getController().getSiteNodeVOWithParentRecursive(siteNodeId);
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
		Iterator it = getSiteNodeVersionIdList().iterator();
		
		List events = new ArrayList();
		while(it.hasNext())
		{
			Integer siteNodeVersionId = (Integer)it.next();
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getFullSiteNodeVersionVOWithId(siteNodeVersionId);

			SiteNodeVersionVO latestSiteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(siteNodeVersionVO.getSiteNodeId());
			if(siteNodeVersionVO.getId().equals(latestSiteNodeVersionVO.getId()))
			{
				logger.info("Creating a new working version as there was no active working version left...");
				SiteNodeStateController.getController().changeState(siteNodeVersionVO.getId(), SiteNodeVersionVO.WORKING_STATE, "new working version", false, this.getInfoGluePrincipal(), siteNodeVersionVO.getSiteNodeId(), events);
			}

			EventVO eventVO = new EventVO();
			eventVO.setDescription(this.versionComment);
			eventVO.setEntityClass(SiteNodeVersion.class.getName());
			eventVO.setEntityId(siteNodeVersionId);
			eventVO.setName(siteNodeVersionVO.getSiteNodeName());
			eventVO.setTypeId(EventVO.UNPUBLISH_LATEST);
			eventVO = EventController.create(eventVO, this.repositoryId, this.getInfoGluePrincipal());
			events.add(eventVO);
			
			List contentVersionVOList = SiteNodeVersionController.getController().getMetaInfoContentVersionVOList(siteNodeVersionId, this.getInfoGluePrincipal());
			Iterator contentVersionVOListIterator = contentVersionVOList.iterator();
			while(contentVersionVOListIterator.hasNext())
			{
			    ContentVersionVO currentContentVersionVO = (ContentVersionVO)contentVersionVOListIterator.next();
			    
				ContentVersionVO latestContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(currentContentVersionVO.getContentId(), currentContentVersionVO.getLanguageId());
				if(currentContentVersionVO.getId().equals(latestContentVersionVO.getId()))
				{
					logger.info("Creating a new working version as there was no active working version left...:" + currentContentVersionVO.getLanguageName());
					ContentStateController.changeState(currentContentVersionVO.getId(), ContentVersionVO.WORKING_STATE, "new working version", false, null, this.getInfoGluePrincipal(), currentContentVersionVO.getContentId(), events);
				}

				EventVO versionEventVO = new EventVO();
				versionEventVO.setDescription(this.versionComment);
				versionEventVO.setEntityClass(ContentVersion.class.getName());
				versionEventVO.setEntityId(currentContentVersionVO.getId());
				versionEventVO.setName(currentContentVersionVO.getContentName());
				versionEventVO.setTypeId(EventVO.UNPUBLISH_LATEST);
				versionEventVO = EventController.create(versionEventVO, this.repositoryId, this.getInfoGluePrincipal());
				events.add(versionEventVO);			    
			}
		}
		
		if(attemptDirectPublishing.equalsIgnoreCase("true"))
		{
		    PublicationVO publicationVO = new PublicationVO();
		    publicationVO.setName("Direct publication by " + this.getInfoGluePrincipal().getName());
		    publicationVO.setDescription(getVersionComment());
		    //publicationVO.setPublisher(this.getInfoGluePrincipal().getName());
		    publicationVO.setRepositoryId(repositoryId);
		    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, false, this.getInfoGluePrincipal());
		}
		
       	return "success";
    }

	/**
	 * This method will try to unpublish all liver versions of this sitenode. 
	 */
	   
    public String doUnpublishAll() throws Exception
    {   
		String[] siteNodeIds = getRequest().getParameterValues("sel");

		List events = new ArrayList();

        for(int i=0; i < siteNodeIds.length; i++)
		{
            String siteNodeIdString = siteNodeIds[i];
	        List siteNodeVersionVOList = SiteNodeVersionController.getController().getPublishedActiveFullSiteNodeVersionVOList(new Integer(siteNodeIdString));
	        
			Iterator it = siteNodeVersionVOList.iterator();
			
			while(it.hasNext())
			{
				SiteNodeVersionVO siteNodeVersionVO = (SiteNodeVersionVO)it.next();
				
				SiteNodeVersionVO latestSiteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(siteNodeVersionVO.getSiteNodeId());
				if(siteNodeVersionVO.getId().equals(latestSiteNodeVersionVO.getId()))
				{
					logger.info("Creating a new working version as there was no active working version left...");
					SiteNodeStateController.getController().changeState(siteNodeVersionVO.getId(), SiteNodeVersionVO.WORKING_STATE, "new working version", false, this.getInfoGluePrincipal(), siteNodeVersionVO.getSiteNodeId(), events);
				}

				EventVO eventVO = new EventVO();
				eventVO.setDescription(this.versionComment);
				eventVO.setEntityClass(SiteNodeVersion.class.getName());
				eventVO.setEntityId(siteNodeVersionVO.getId());
				eventVO.setName(siteNodeVersionVO.getSiteNodeName());
				eventVO.setTypeId(EventVO.UNPUBLISH_LATEST);
				eventVO = EventController.create(eventVO, this.repositoryId, this.getInfoGluePrincipal());
				events.add(eventVO);
				
				List contentVersionVOList = SiteNodeVersionController.getController().getMetaInfoContentVersionVOList(siteNodeVersionVO.getId(), this.getInfoGluePrincipal());
				Iterator contentVersionVOListIterator = contentVersionVOList.iterator();
				while(contentVersionVOListIterator.hasNext())
				{
				    ContentVersionVO currentContentVersionVO = (ContentVersionVO)contentVersionVOListIterator.next();
				    
					ContentVersionVO latestContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(currentContentVersionVO.getContentId(), currentContentVersionVO.getLanguageId());
					if(currentContentVersionVO.getId().equals(latestContentVersionVO.getId()))
					{
						logger.info("Creating a new working version as there was no active working version left...:" + currentContentVersionVO.getLanguageName());
						ContentStateController.changeState(currentContentVersionVO.getId(), ContentVersionVO.WORKING_STATE, "new working version", false, null, this.getInfoGluePrincipal(), currentContentVersionVO.getContentId(), events);
					}

					EventVO versionEventVO = new EventVO();
					versionEventVO.setDescription(this.versionComment);
					versionEventVO.setEntityClass(ContentVersion.class.getName());
					versionEventVO.setEntityId(currentContentVersionVO.getId());
					versionEventVO.setName(currentContentVersionVO.getContentName());
					versionEventVO.setTypeId(EventVO.UNPUBLISH_LATEST);
					versionEventVO = EventController.create(versionEventVO, this.repositoryId, this.getInfoGluePrincipal());
					events.add(versionEventVO);			    
				}
			}
		}
		
		if(attemptDirectPublishing.equalsIgnoreCase("true"))
		{
		    PublicationVO publicationVO = new PublicationVO();
		    publicationVO.setName("Direct publication by " + this.getInfoGluePrincipal().getName());
		    publicationVO.setDescription(getVersionComment());
		    //publicationVO.setPublisher(this.getInfoGluePrincipal().getName());
		    publicationVO.setRepositoryId(repositoryId);
		    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, false, this.getInfoGluePrincipal());
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

}
