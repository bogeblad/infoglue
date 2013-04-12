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
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.EventController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.SmallestContentVersionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.AccessConstraintException;
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


	
	public String doInput() throws Exception 
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

	public String doInputV3() throws Exception 
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

	public String doInputChooseContents() throws Exception 
	{
		ProcessBean processBean = ProcessBean.createProcessBean(UnpublishContentVersionAction.class.getName(), "" + getInfoGluePrincipal().getName());
		processBean.setStatus(ProcessBean.RUNNING);

		try
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
	
				processBean.updateProcess("Getting child contents available for unpublish");
				
				contentVOList = ContentController.getContentController().getContentVOWithParentRecursive(contentId, processBean);
			}
		}
		finally
		{
			processBean.setStatus(ProcessBean.FINISHED);
			processBean.removeProcess();
		}

	    return "inputChooseContents";
	}


	public String doInputChooseContentsV3() throws Exception 
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
	 */
	   
    public String doExecute() throws Exception
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
	   
    public String doV3() throws Exception
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
		    else
		    {
		    	return SUCCESS;
		    }
    	}
    	catch (Exception e) 
    	{
    		e.printStackTrace();
    		logger.error("Error unpublishing:" + e.getMessage(), e);
    		return ERROR;
		}
    }
    
    
	/**
	 * This method will try to unpublish all liver versions of this content. 
	 */
	   
    public String doUnpublishAll() throws Exception
    {   
		ProcessBean processBean = ProcessBean.createProcessBean(UnpublishContentVersionAction.class.getName(), "" + getInfoGluePrincipal().getName());
		processBean.setStatus(ProcessBean.RUNNING);

		String[] contentIdStrings = getRequest().getParameterValues("sel");
		
		List events = new ArrayList();

		Database db = CastorDatabaseService.getDatabase();

        beginTransaction(db);

        try
        {
        	List<Integer> contentIds = new ArrayList<Integer>();
        	for(String contentIdString : contentIdStrings)
        	{
        		contentIds.add(new Integer(contentIdString));
        	}
        	
        	processBean.updateProcess("Searching for all published versions.");
        	System.out.println("1");
        	List<SmallestContentVersionVO> contentVersionsVOList = ContentVersionController.getContentVersionController().getPublishedActiveContentVersionVOList(contentIds, db);
        	System.out.println("2");
        	processBean.updateProcess("Found " + contentVersionsVOList.size() + " versions");
        	
        	for(SmallestContentVersionVO contentVersionVO : contentVersionsVOList)
        	{
        		//ContentVersion contentVersion = ContentVersionController.getContentVersionController().getMediumContentVersionWithId(contentVersionVO.getId(), db);
        		//contentVersion.setStateId(0);
        		//contentVersion.setVersionComment(this.versionComment);
        		//contentVersion.setVersionModifier(this.getInfoGluePrincipal().getName());
				
				ContentStateController.changeState(contentVersionVO.getId(), ContentVersionVO.WORKING_STATE, "new working version", false, this.getInfoGluePrincipal(), contentVersionVO.getContentId(), db, events);
	        	System.out.println("3");
        		
				EventVO eventVO = new EventVO();
				eventVO.setDescription(this.versionComment);
				eventVO.setEntityClass(ContentVersion.class.getName());
				eventVO.setEntityId(contentVersionVO.getContentVersionId());
				eventVO.setName(contentVersionVO.getContentId() + "(" + contentVersionVO.getLanguageId() + ")");
				eventVO.setTypeId(EventVO.UNPUBLISH_LATEST);
				eventVO = EventController.create(eventVO, this.repositoryId, this.getInfoGluePrincipal(), db);
				events.add(eventVO);
        	
				if(events.size() % 10 == 0)
					processBean.updateLastDescription("Updated " + events.size() + " versions");
        	}

			processBean.updateLastDescription("Creating publication");

			if(attemptDirectPublishing.equalsIgnoreCase("true"))
			{
			    PublicationVO publicationVO = new PublicationVO();
			    publicationVO.setName("Direct publication by " + this.getInfoGluePrincipal().getName());
			    publicationVO.setDescription(getVersionComment());
			    publicationVO.setPublisher(this.getInfoGluePrincipal().getName());
			    publicationVO.setRepositoryId(repositoryId);
	        	System.out.println("4");

			    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, this.overrideVersionModifyer, this.getInfoGluePrincipal(), db);
			}
			
		    commitTransaction(db);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        finally
		{
			processBean.setStatus(ProcessBean.FINISHED);
			processBean.removeProcess();
		}
        
       	return "success";
    }

	/**
	 * This method will try to unpublish all liver versions of this content. 
	 */
	   
    public String doUnpublishAllV3() throws Exception
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
		    else
		    {
		    	return SUCCESS;
		    }
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
