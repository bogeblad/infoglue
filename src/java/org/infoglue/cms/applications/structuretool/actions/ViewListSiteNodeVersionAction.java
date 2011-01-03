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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.sorters.ReflectionComparator;

/**
 *
 * @author Mattias Bogeblad
 * 
 * Present a list of siteNodeVersions under a given siteNode, recursing down in the hierarcy.
 * Used to publish an entire hierarchy of pages.
 * 
 */

public class ViewListSiteNodeVersionAction extends InfoGlueAbstractAction 
{
    private final static Logger logger = Logger.getLogger(ViewListSiteNodeVersionAction.class.getName());

	private static final long serialVersionUID = 1L;

	private Set siteNodeVersionVOList = new TreeSet(Collections.reverseOrder(new ReflectionComparator("modifiedDateTime")));
	private Set contentVersionVOList = new TreeSet(Collections.reverseOrder(new ReflectionComparator("modifiedDateTime")));
	private Integer siteNodeVersionId;
	private Integer siteNodeId;
	private Integer languageId;
	private Integer contentId;

	private Integer repositoryId;
	private boolean recurseSiteNodes = true;
	private String returnAddress;
    private String originalAddress;
   	private String userSessionKey;
   	private String attemptDirectPublishing;

	protected String doExecute() throws Exception 
	{
		logger.info("siteNodeId:" + this.siteNodeId);
		logger.info("siteNodeVersionId:" + this.siteNodeVersionId);
		if(this.siteNodeVersionId == null)
		{
		    SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getACLatestActiveSiteNodeVersionVO(this.getInfoGluePrincipal(), siteNodeId);
		    if(siteNodeVersionVO != null)
		        this.siteNodeVersionId = siteNodeVersionVO.getId();
		}
		
		if(this.siteNodeVersionId != null)
		{
			AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
			Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId);
			if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "SiteNodeVersion.SubmitToPublish", protectedSiteNodeVersionId.toString()))
				ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeVersionId", "1005"));
		
			ceb.throwIfNotEmpty();

			if(contentId != null && contentId > -1)
			{
				Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
				if(protectedContentId == null || AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.SubmitToPublish", protectedContentId.toString()))
				{
					ContentVO contentVO = ContentControllerProxy.getController().getACContentVOWithId(getInfoGluePrincipal(), contentId);
					List languageVOList = LanguageController.getController().getLanguageVOList(contentVO.getRepositoryId());
					Iterator languageVOListIterator = languageVOList.iterator();
					while(languageVOListIterator.hasNext())
					{
						LanguageVO language = (LanguageVO)languageVOListIterator.next();
						ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, language.getId());
						if(contentVersionVO != null && contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
						{
							this.contentVersionVOList.add(contentVersionVO);
						}
					}
				}
			}
			
			SiteNodeVersionController.getController().getSiteNodeAndAffectedItemsRecursive(this.siteNodeId, SiteNodeVersionVO.WORKING_STATE, this.siteNodeVersionVOList, this.contentVersionVOList, false, recurseSiteNodes, this.getInfoGluePrincipal());
		}

	    return "success";
	}

	public String doV3() throws Exception 
	{
		doExecute();
		
        userSessionKey = "" + System.currentTimeMillis();
        
        addActionLink(userSessionKey, new LinkBean("currentPageUrl", getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentPageLinkText"), getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentPageTitleText"), getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentPageTitleText"), this.originalAddress, false, ""));
        
        setActionExtraData(userSessionKey, "repositoryId", "" + this.repositoryId);
        setActionExtraData(userSessionKey, "siteNodeId", "" + this.siteNodeId);
        //setActionExtraData(userSessionKey, "siteNodeName", "" + siteNodeVersionVO);
        setActionExtraData(userSessionKey, "unrefreshedSiteNodeId", "" + this.siteNodeId);
        setActionExtraData(userSessionKey, "unrefreshedNodeId", "" + this.siteNodeId);
        setActionExtraData(userSessionKey, "changeTypeId", "1");

        setActionExtraData(userSessionKey, "disableCloseLink", "true");
        
	    return "successV3";
	}


	public Set getSiteNodeVersions()
	{
		return this.siteNodeVersionVOList;		
	}
	

	public Integer getSiteNodeId()
	{
		return siteNodeId;
	}

	public void setSiteNodeId(Integer siteNodeId)
	{
		this.siteNodeId = siteNodeId;
	}

	public Integer getLanguageId()
	{
		return languageId;
	}

	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
	}

	public void setContentId(Integer contentId)
	{
		this.contentId = contentId;
	}

	public Integer getSiteNodeVersionId()
	{
		return siteNodeVersionId;
	}

	public void setSiteNodeVersionId(Integer siteNodeVersionId)
	{
		this.siteNodeVersionId = siteNodeVersionId;
	}

    public Integer getRepositoryId()
    {
        return repositoryId;
    }
    
    public void setRepositoryId(Integer repositoryId)
    {
        this.repositoryId = repositoryId;
    }
    
    public Set getContentVersionVOList()
    {
        return contentVersionVOList;
    }
    
    public Set getSiteNodeVersionVOList()
    {
        return siteNodeVersionVOList;
    }

	public String getReturnAddress() 
	{
		return returnAddress;
	}

	public void setReturnAddress(String returnAddress) 
	{
		this.returnAddress = returnAddress;
	}

	public boolean isRecurseSiteNodes() 
	{
		return recurseSiteNodes;
	}

	public void setRecurseSiteNodes(boolean recurseSiteNodes) 
	{
		this.recurseSiteNodes = recurseSiteNodes;
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
	
	public String getAttemptDirectPublishing()
	{
		return attemptDirectPublishing;
	}

	public void setAttemptDirectPublishing(String attemptDirectPublishing)
	{
		this.attemptDirectPublishing = attemptDirectPublishing;
	}

}
