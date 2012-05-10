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

package org.infoglue.cms.applications.contenttool.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.EventController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;

/**
 *
 *  @author Stefan Sik
 * 
 * Present a list of contentVersion under a given content, recursing down in the hierarcy
 * 
 */

public class UnpublishContentVersionAction extends InfoGlueAbstractAction 
{
	private static final long serialVersionUID = 1L;

    private final static Logger logger = Logger.getLogger(UnpublishContentVersionAction.class.getName());

	private List contentVersionVOList = new ArrayList();
	private List contentVOList		  = new ArrayList();
	private Integer contentId;
	private Integer repositoryId;

	private List contentVersionId = new ArrayList();
	private Integer stateId;
	private Integer languageId;
	private String versionComment;
	private boolean overrideVersionModifyer = false;
	private String attemptDirectPublishing = "false";
	
	private String returnAddress;
   	private String userSessionKey;
    private String originalAddress;


	
	public String doInput() throws ConstraintException, SystemException 
	{
		if(this.contentId != null)
		{
		    ContentVO contentVO = ContentController.getContentController().getContentVOWithId(this.contentId);
		    this.repositoryId = contentVO.getRepositoryId();
		    
			AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
			Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
			if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.SubmitToPublish", protectedContentId.toString()))
				ceb.add(new AccessConstraintException("Content.contentId", "1005"));
			
			ceb.throwIfNotEmpty();

			contentVersionVOList = ContentVersionController.getContentVersionController().getContentVersionVOWithParentRecursive(contentId, ContentVersionVO.PUBLISHED_STATE, false);
		}

	    return "input";
	}

	public String doInputV3() throws ConstraintException, SystemException 
	{
		doInput();

        userSessionKey = "" + System.currentTimeMillis();
        
        addActionLink(userSessionKey, new LinkBean("currentPageUrl", getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentPageLinkText"), getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentPageTitleText"), getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentPageTitleText"), this.originalAddress, false, ""));

        setActionExtraData(userSessionKey, "repositoryId", "" + this.repositoryId);
        setActionExtraData(userSessionKey, "contentId", "" + this.contentId);
        setActionExtraData(userSessionKey, "unrefreshedContentId", "" + this.contentId);
        setActionExtraData(userSessionKey, "unrefreshedNodeId", "" + this.contentId);
        setActionExtraData(userSessionKey, "languageId", "" + this.languageId);
        setActionExtraData(userSessionKey, "changeTypeId", "3");

        setActionExtraData(userSessionKey, "disableCloseLink", "true");

	    return "inputV3";
	}

	public String doInputChooseContents() throws ConstraintException, SystemException 
	{
		if(this.contentId != null)
		{
		    ContentVO contentVO = ContentController.getContentController().getContentVOWithId(this.contentId);
		    this.repositoryId = contentVO.getRepositoryId();
		    
			AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
			Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
			if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.SubmitToPublish", protectedContentId.toString()))
				ceb.add(new AccessConstraintException("Content.contentId", "1005"));
			
			ceb.throwIfNotEmpty();

			contentVOList = ContentController.getContentController().getContentVOWithParentRecursive(contentId);
		}

	    return "inputChooseContents";
	}

	public String doInputChooseContentsV3() 
	{
		try
		{
			doInputChooseContents();
			
	        userSessionKey = "" + System.currentTimeMillis();
	        
	        addActionLink(userSessionKey, new LinkBean("currentPageUrl", getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentPageLinkText"), getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentPageTitleText"), getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentPageTitleText"), this.originalAddress, false, ""));

	        setActionExtraData(userSessionKey, "repositoryId", "" + this.repositoryId);
	        setActionExtraData(userSessionKey, "contentId", "" + this.contentId);
	        setActionExtraData(userSessionKey, "unrefreshedContentId", "" + this.contentId);
	        setActionExtraData(userSessionKey, "unrefreshedNodeId", "" + this.contentId);
	        setActionExtraData(userSessionKey, "languageId", "" + this.languageId);
	        setActionExtraData(userSessionKey, "changeTypeId", "3");

	        setActionExtraData(userSessionKey, "disableCloseLink", "true");
        
		}
		catch (Exception e) 
		{
			logger.error("Error in doInputChooseContentsV3:" + e.getMessage(), e);
			return ERROR;
		}
		
	    return "inputChooseContentsV3";
	}

	/**
	 * This method gets called when calling this action. 
	 * If the stateId is 2 which equals that the user tries to prepublish the page. If so we
	 * ask the user for a comment as this is to be regarded as a new version. 
	 * @throws SystemException 
	 * @throws ConstraintException 
	 */
	   
    public String doExecute() throws SystemException, ConstraintException
    {   
		setContentVersionId( getRequest().getParameterValues("sel") );
		Iterator it = getContentVersionId().iterator();
		
		List events = new ArrayList();
		while(it.hasNext())
		{
			Integer contentVersionId = (Integer)it.next();
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getFullContentVersionVOWithId(contentVersionId);
			
			ContentVersionVO latestContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVersionVO.getContentId(), contentVersionVO.getLanguageId());
			if(contentVersionVO.getId().equals(latestContentVersionVO.getId()))
			{
				logger.info("Creating a new working version as there was no active working version left...:" + contentVersionVO.getLanguageName());
				ContentStateController.changeState(contentVersionVO.getId(), ContentVersionVO.WORKING_STATE, "new working version", false, null, this.getInfoGluePrincipal(), contentVersionVO.getContentId(), events);
			}
			
			EventVO eventVO = new EventVO();
			eventVO.setDescription(this.versionComment);
			eventVO.setEntityClass(ContentVersion.class.getName());
			eventVO.setEntityId(contentVersionId);
			eventVO.setName(contentVersionVO.getContentName() + "(" + contentVersionVO.getLanguageName() + ")");
			eventVO.setTypeId(EventVO.UNPUBLISH_LATEST);
			eventVO = EventController.create(eventVO, this.repositoryId, this.getInfoGluePrincipal());
			events.add(eventVO);
		}
		
		if(attemptDirectPublishing.equalsIgnoreCase("true"))
		{
		    PublicationVO publicationVO = new PublicationVO();
		    publicationVO.setName("Direct publication by " + this.getInfoGluePrincipal().getName());
		    publicationVO.setDescription(getVersionComment());
		    //publicationVO.setPublisher(this.getInfoGluePrincipal().getName());
		    publicationVO.setRepositoryId(repositoryId);
		    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, this.overrideVersionModifyer, this.getInfoGluePrincipal());
		}
		
       	return "success";
    }


	/**
	 * This method gets called when calling this action. 
	 * If the stateId is 2 which equals that the user tries to prepublish the page. If so we
	 * ask the user for a comment as this is to be regarded as a new version. 
	 */
	   
    public String doV3()
    {
    	try
    	{
	    	doExecute();
	    	
			if(attemptDirectPublishing.equalsIgnoreCase("true"))
			{
	            setActionMessage(userSessionKey, getLocalizedString(getLocale(), "tool.common.unpublishing.unpublishingInlineOperationDoneHeader"));
			}
			else
			{
				setActionMessage(userSessionKey, getLocalizedString(getLocale(), "tool.common.unpublishing.submitToUnpublishingInlineOperationDoneHeader"));
			}
			
			if(this.returnAddress != null && !this.returnAddress.equals(""))
		    {
		        String arguments 	= "userSessionKey=" + userSessionKey + "&attemptDirectPublishing=" + attemptDirectPublishing + "&isAutomaticRedirect=false";
		        String messageUrl 	= returnAddress + (returnAddress.indexOf("?") > -1 ? "&" : "?") + arguments;
		        
		        this.getResponse().sendRedirect(messageUrl);
		        return NONE;
		    }
	    	return SUCCESS;
    	}
    	catch (Exception e) 
    	{
    		logger.error("Error unpublishing:" + e.getMessage(), e);
    		return ERROR;
		}
    }
    
    
	/**
	 * This method will try to unpublish all liver versions of this content. 
	 */
    public String doUnpublishAll() throws NumberFormatException, SystemException, ConstraintException
    {   
		String[] contentIds = getRequest().getParameterValues("sel");
		
		List events = new ArrayList();

        for(int i=0; i < contentIds.length; i++)
		{
            String contentIdString = contentIds[i];
	        
            List contentVersionsVOList = ContentVersionController.getContentVersionController().getPublishedActiveContentVersionVOList(new Integer(contentIdString));
	        Map checkedLanguages = new HashMap();
			Iterator it = contentVersionsVOList.iterator();
			while(it.hasNext())
			{
				ContentVersionVO contentVersionVO = (ContentVersionVO)it.next();
				
				if(checkedLanguages.get(contentVersionVO.getLanguageId()) == null)
				{
					checkedLanguages.put(contentVersionVO.getLanguageId(), new Boolean(true));
					ContentVersionVO latestContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVersionVO.getContentId(), contentVersionVO.getLanguageId());
					if(contentVersionVO.getId().equals(latestContentVersionVO.getId()))
					{
						logger.info("Creating a new working version as there was no active working version left...:" + contentVersionVO.getLanguageName());
						ContentStateController.changeState(contentVersionVO.getId(), ContentVersionVO.WORKING_STATE, "new working version", false, null, this.getInfoGluePrincipal(), contentVersionVO.getContentId(), events);
					}
				}
				
				EventVO eventVO = new EventVO();
				eventVO.setDescription(this.versionComment);
				eventVO.setEntityClass(ContentVersion.class.getName());
				eventVO.setEntityId(contentVersionVO.getContentVersionId());
				eventVO.setName(contentVersionVO.getContentName() + "(" + contentVersionVO.getLanguageName() + ")");
				eventVO.setTypeId(EventVO.UNPUBLISH_LATEST);
				eventVO = EventController.create(eventVO, this.repositoryId, this.getInfoGluePrincipal());
				events.add(eventVO);
			}
		}	
		
		if(attemptDirectPublishing.equalsIgnoreCase("true"))
		{
		    PublicationVO publicationVO = new PublicationVO();
		    publicationVO.setName("Direct publication by " + this.getInfoGluePrincipal().getName());
		    publicationVO.setDescription(getVersionComment());
		    //publicationVO.setPublisher(this.getInfoGluePrincipal().getName());
		    publicationVO.setRepositoryId(repositoryId);
		    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, this.overrideVersionModifyer, this.getInfoGluePrincipal());
		}
		
       	return "success";
    }

	/**
	 * This method will try to unpublish all liver versions of this content. 
	 */
	   
    public String doUnpublishAllV3()
    {   
    	try
    	{
	    	doUnpublishAll();
	    	
			if(attemptDirectPublishing.equalsIgnoreCase("true"))
			{
	            setActionMessage(userSessionKey, getLocalizedString(getLocale(), "tool.common.unpublishing.unpublishingInlineOperationDoneHeader"));
			}
			else
			{
				setActionMessage(userSessionKey, getLocalizedString(getLocale(), "tool.common.unpublishing.submitToUnpublishingInlineOperationDoneHeader"));
			}
			
			if(this.returnAddress != null && !this.returnAddress.equals(""))
		    {
		        String arguments 	= "userSessionKey=" + userSessionKey + "&attemptDirectPublishing=" + attemptDirectPublishing + "&isAutomaticRedirect=false";
		        String messageUrl 	= returnAddress + (returnAddress.indexOf("?") > -1 ? "&" : "?") + arguments;
		        
		        this.getResponse().sendRedirect(messageUrl);
		        return NONE;
		    }
	    	return SUCCESS;
    	}
    	catch (Exception e) 
    	{
    		logger.error("Error unpublishing:" + e.getMessage(), e);
    		return ERROR;
		}
    }

	public List getContentVersions()
	{
		return this.contentVersionVOList;		
	}
	
    public List getContents()
    {
        return contentVOList;
    }

	public Integer getContentId() 
	{
		return contentId;
	}

	public void setContentId(Integer contentId) 
	{
		this.contentId = contentId;
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
            
	/**
	 * @return
	 */
	public List getContentVersionId() 
	{
		return contentVersionId;
	}

	/**
	 * @param list
	 */
	private void setContentVersionId(String[] list) 
	{
		contentVersionId = new ArrayList();
		for(int i=0; i < list.length; i++)
		{
			contentVersionId.add(new Integer(list[i]));
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
    
    public boolean getOverrideVersionModifyer()
    {
        return overrideVersionModifyer;
    }
    
    public void setOverrideVersionModifyer(boolean overrideVersionModifyer)
    {
        this.overrideVersionModifyer = overrideVersionModifyer;
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

	public String getOriginalAddress()
	{
		return originalAddress;
	}

	public void setOriginalAddress(String originalAddress)
	{
		this.originalAddress = originalAddress;
	}

}
