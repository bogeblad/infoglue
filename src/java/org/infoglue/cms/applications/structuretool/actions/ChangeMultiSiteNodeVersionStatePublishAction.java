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
import org.infoglue.cms.controllers.kernel.impl.simple.ContentStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeStateController;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * This class implements submit to publish on many sitenode versions at once.
 *  
 * @author Mattias Bogeblad
 */

public class ChangeMultiSiteNodeVersionStatePublishAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ChangeMultiSiteNodeVersionStatePublishAction.class.getName());

	private Integer siteNodeId;
	private Integer languageId;
	private List siteNodeVersionId = new ArrayList();
	private List contentVersionId = new ArrayList();
	private Integer stateId;
	private String versionComment;
	private boolean overrideVersionModifyer = false;
	private String recipientFilter = null;
	private Integer repositoryId;
	private String attemptDirectPublishing = "false";
	private String returnAddress;
   	private String userSessionKey;
   	
			    
	/**
	 * This method gets called when calling this action. 
	 * If the stateId is 2 which equals that the user tries to prepublish the page. If so we
	 * ask the user for a comment as this is to be regarded as a new version. 
	 */
	   
    public String doExecute() throws Exception
    {      
		setSiteNodeVersionId( getRequest().getParameterValues("selSiteNodeVersions") );
		Iterator it = siteNodeVersionId.iterator();

		List events = new ArrayList();
		while(it.hasNext())
		{
			Integer siteNodeVersionId = (Integer)it.next();
			logger.info("Publishing:" + siteNodeVersionId);
			SiteNodeVersion siteNodeVersion = SiteNodeStateController.getController().changeState(siteNodeVersionId, SiteNodeVersionVO.PUBLISH_STATE, getVersionComment(), this.overrideVersionModifyer, this.recipientFilter, this.getInfoGluePrincipal(), null, events);
		}

		setContentVersionId( getRequest().getParameterValues("selContentVersions") );
		Iterator contentVersionIdsIterator = contentVersionId.iterator();

		while(contentVersionIdsIterator.hasNext())
		{
			Integer contentVersionId = (Integer)contentVersionIdsIterator.next();
			logger.info("Publishing:" + contentVersionId);
			ContentVersion contentVersion = ContentStateController.changeState(contentVersionId, ContentVersionVO.PUBLISH_STATE, getVersionComment(), this.overrideVersionModifyer, this.recipientFilter, this.getInfoGluePrincipal(), null, events);
		}

        RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);
        String liveAddressBaseUrl = repositoryVO.getLiveBaseUrl() + "";

        String liveAddress = null;
        if(CmsPropertyHandler.getPublicDeliveryUrls().size() > 0)
        {
	        String firstPublicDeliveryUrl = (String)CmsPropertyHandler.getPublicDeliveryUrls().get(0);
	        logger.debug("firstPublicDeliveryUrl:" + firstPublicDeliveryUrl);
	        String[] firstPublicDeliveryUrlSplit = firstPublicDeliveryUrl.split("/");
	        
	        String context = firstPublicDeliveryUrlSplit[firstPublicDeliveryUrlSplit.length - 1];
	        logger.debug("context:" + context);
	        liveAddress = liveAddressBaseUrl + "/" + context + "/ViewPage.action" + "?siteNodeId=" + this.getSiteNodeId() + "&languageId=" + this.languageId;
        }
        
		if(attemptDirectPublishing.equalsIgnoreCase("true"))
		{
            setActionMessage(userSessionKey, getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationDoneHeader"));
        	if(liveAddress != null)
        		addActionLink(userSessionKey, new LinkBean("publishedPageUrl", getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationViewPublishedPageLinkText"), getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationViewPublishedPageTitleText"), getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationViewPublishedPageTitleText"), liveAddress, false, "", "_blank"));
        	else
        		addActionLink(userSessionKey, new LinkBean("publishedPageUrl", "No public servers stated in cms", "No public servers stated in cms", "No public servers stated in cms", "#", false, "", "_blank"));
        		
			PublicationVO publicationVO = new PublicationVO();
		    publicationVO.setName("Direct publication by " + this.getInfoGluePrincipal().getName());
		    publicationVO.setDescription(getVersionComment());
		    publicationVO.setRepositoryId(repositoryId);
		    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, overrideVersionModifyer, this.getInfoGluePrincipal());
		}
		else
		{
            setActionMessage(userSessionKey, getLocalizedString(getLocale(), "tool.common.publishing.submitToPublishingInlineOperationDoneHeader"));
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
        /*
		if(this.returnAddress != null && !this.returnAddress.equals(""))
		{
			this.returnAddress = this.getResponse().encodeURL(returnAddress);
			this.getResponse().sendRedirect(returnAddress);
	
			return NONE;
		}
		else
		{
	       	return "success";
		}
		*/
    }
        
    public java.lang.Integer getSiteNodeId()
    {
        return this.siteNodeId;
    }
        
    public void setSiteNodeId(java.lang.Integer siteNodeId)
    {
	    this.siteNodeId = siteNodeId;
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

	
	public Integer getRepositoryId()
    {
        return repositoryId;
    }
    
    public void setRepositoryId(Integer repositoryId)
    {
        this.repositoryId = repositoryId;
    }
    
	public Integer getLanguageId()
	{
		return languageId;
	}

	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
	}
    
    public void setAttemptDirectPublishing(String attemptDirectPublishing)
    {
        this.attemptDirectPublishing = attemptDirectPublishing;
    }
    
    public boolean getOverrideVersionModifyer()
    {
        return overrideVersionModifyer;
    }
    
    public void setOverrideVersionModifyer(boolean overrideVersionModifyer)
    {
        this.overrideVersionModifyer = overrideVersionModifyer;
    }

	public String getRecipientFilter() 
	{
		return recipientFilter;
	}

	public void setRecipientFilter(String recipientFilter) 
	{
		this.recipientFilter = recipientFilter;
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
