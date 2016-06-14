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
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.EventController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.workflow.EventVO;


public class ChangeMultiContentStatePublishAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ChangeMultiContentStatePublishAction.class.getName());

	private static final long serialVersionUID = -6759369582248131484L;

	private Integer contentId;
	private List contentVersionId = new ArrayList();
	private List siteNodeVersionId = new ArrayList();
	private Integer stateId;
	private Integer languageId;
	private String versionComment;
	private boolean overrideVersionModifyer = false;
	private String recipientFilter = null;
	private Integer repositoryId;
	private String attemptDirectPublishing = "false";

	private String returnAddress;
   	private String userSessionKey;
    private String originalAddress;
		    
	/**
	 * This method gets called when calling this action. 
	 * If the stateId is 2 which equals that the user tries to prepublish the page. If so we
	 * ask the user for a comment as this is to be regarded as a new version. 
	 */
	   
    public String doExecute() throws Exception
    {
        setSiteNodeVersionId( getRequest().getParameterValues("selSiteNodeVersions") );
		
        List<EventVO> events = new ArrayList<EventVO>();

		Map<Integer,SiteNodeVO> newsiteNodeMap = new HashMap<Integer,SiteNodeVO>();
		Map<Integer,ContentVO> newContentMap = new HashMap<Integer,ContentVO>();

		Map<Integer,SiteNodeVO> siteNodeMap = SiteNodeController.getController().getSiteNodeVOMapWithNoStateCheck(siteNodeVersionId);
		for(Entry<Integer,SiteNodeVO> entry : siteNodeMap.entrySet())
		{
			Integer siteNodeVersionId = entry.getKey();
			logger.info("Publishing:" + siteNodeVersionId);
			SiteNodeVersionVO siteNodeVersion = SiteNodeStateController.getController().changeState(siteNodeVersionId, entry.getValue(), SiteNodeVersionVO.PUBLISH_STATE, getVersionComment(), this.overrideVersionModifyer, this.recipientFilter, this.getInfoGluePrincipal(), events);
			newsiteNodeMap.put(siteNodeVersion.getId(), entry.getValue());
		}
		/*
		Iterator it = siteNodeVersionId.iterator();
		while(it.hasNext())
		{
			Integer siteNodeVersionId = (Integer)it.next();
			logger.info("Publishing:" + siteNodeVersionId);
			SiteNodeVersionVO siteNodeVersion = SiteNodeStateController.getController().changeState(siteNodeVersionId, SiteNodeVersionVO.PUBLISH_STATE, getVersionComment(), overrideVersionModifyer, this.recipientFilter, this.getInfoGluePrincipal(), null, events);
		}
		*/
		
		setContentVersionId( getRequest().getParameterValues("selContentVersions") );
		if(logger.isInfoEnabled())
			logger.info("contentVersionId:" + contentVersionId);
		Map<Integer,ContentVO> contentMap = ContentController.getContentController().getContentVOMapWithNoStateCheck(contentVersionId);
		if(logger.isInfoEnabled())
			logger.info("contentMap:" + contentMap);
		for(Entry<Integer,ContentVO> entry : contentMap.entrySet())
		{
			Integer contentVersionId = entry.getKey();
			if(logger.isInfoEnabled())
				logger.info("contentVersionId:" + contentVersionId);
			logger.info("Publishing:" + siteNodeVersionId);
			ContentVersionVO contentVersion = ContentStateController.changeState(contentVersionId, entry.getValue(), ContentVersionVO.PUBLISH_STATE, getVersionComment(), this.overrideVersionModifyer, this.recipientFilter, this.getInfoGluePrincipal(), null, events);
			newContentMap.put(contentVersion.getId(), entry.getValue());
		}
		/*
		Iterator contentVersionIdsIterator = contentVersionId.iterator();
		while(contentVersionIdsIterator.hasNext())
		{
			Integer contentVersionId = (Integer)contentVersionIdsIterator.next();
			logger.info("Publishing:" + contentVersionId);
			ContentVersionVO contentVersion = ContentStateController.changeState(contentVersionId, ContentVersionVO.PUBLISH_STATE, getVersionComment(), this.overrideVersionModifyer, this.recipientFilter, this.getInfoGluePrincipal(), null, events);
		}
		*/
		
		if(!attemptDirectPublishing.equalsIgnoreCase("true"))
		{
			if(recipientFilter != null && !recipientFilter.equals("") && events != null && events.size() > 0)
				PublicationController.mailPublishNotification(events, repositoryId, getInfoGluePrincipal(), recipientFilter, false);
		}


		if(attemptDirectPublishing.equalsIgnoreCase("true"))
		{
		    PublicationVO publicationVO = new PublicationVO();
		    publicationVO.setName("Direct publication by " + this.getInfoGluePrincipal().getName());
		    publicationVO.setDescription(getVersionComment());
		    publicationVO.setRepositoryId(repositoryId);
		    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, newsiteNodeMap, newContentMap, overrideVersionModifyer, this.getInfoGluePrincipal());
		    //publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, this.overrideVersionModifyer, this.getInfoGluePrincipal());
		}
		
		if(returnAddress != null && !returnAddress.equals(""))
		{
	        String arguments 	= "userSessionKey=" + userSessionKey;
	        String messageUrl 	= returnAddress + (returnAddress.indexOf("?") > -1 ? "&" : "?") + arguments;
	        
	        this.getResponse().sendRedirect(messageUrl);

	       	return NONE;
		}
		
       	return "success";
    }
        
    public java.lang.Integer getContentId()
    {
        return this.contentId;
    }
        
    public void setContentId(java.lang.Integer contentId)
    {
	    this.contentId = contentId;
    }

    public java.lang.Integer getLanguageId()
    {
        return this.languageId;
    }
        
    public void setLanguageId(java.lang.Integer languageId)
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
	    if(list != null)
		{
			for(int i=0; i < list.length; i++)
			{
				contentVersionId.add(new Integer(list[i]));
			}	
		}
	}

	private void setSiteNodeVersionId(String[] list) 
	{
		if(list != null)
		{
			for(int i=0; i < list.length; i++)
			{
				siteNodeVersionId.add(new Integer(list[i]));
			}	
		}
	}
    public Integer getRepositoryId()
    {
        return repositoryId;
    }

    public void setAttemptDirectPublishing(String attemptDirectPublishing)
    {
        this.attemptDirectPublishing = attemptDirectPublishing;
    }
    
    public void setRepositoryId(Integer repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public List getSiteNodeVersionId()
    {
        return siteNodeVersionId;
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

	public String getRecipientFilter() 
	{
		return recipientFilter;
	}

	public void setRecipientFilter(String recipientFilter) 
	{
		this.recipientFilter = recipientFilter;
	}
}
