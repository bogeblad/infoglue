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

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.Timer;

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

	private List<ContentVersionVO> contentVersionVOList = new ArrayList<ContentVersionVO>();
	private List<SiteNodeVersionVO> siteNodeVersionVOList = new ArrayList<SiteNodeVersionVO>();
	
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
   	private boolean anonymousNoAccessWarning = false;

	protected String doExecute() throws Exception 
	{
		logUserActionInfo(getClass(), "doExecute");
		ProcessBean processBean = ProcessBean.createProcessBean(ViewListSiteNodeVersionAction.class.getName(), "" + getInfoGluePrincipal().getName());
		processBean.setStatus(ProcessBean.RUNNING);

		try
		{
			Timer t = new Timer();
			
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
	
				processBean.updateProcess(getLocalizedString(getLocale(), "tool.structuretool.publicationProcess.gettingItems"));
				
				Set<SiteNodeVersionVO> siteNodeVersionVOList = new HashSet<SiteNodeVersionVO>();
				Set<ContentVersionVO> contentVersionVOList = new HashSet<ContentVersionVO>();
				
				SiteNodeVersionController.getController().getSiteNodeAndAffectedItemsRecursive(this.siteNodeId, SiteNodeVersionVO.WORKING_STATE, siteNodeVersionVOList, contentVersionVOList, false, recurseSiteNodes, this.getInfoGluePrincipal(), processBean, getLocale(), -1);
				RequestAnalyser.getRequestAnalyser().registerComponentStatistics("ViewListContentVersion.getSiteNodeAndAffectedItemsRecursive", t.getElapsedTime());
				
				processBean.updateProcess(getLocalizedString(getLocale(), "tool.structuretool.publicationProcess.found", siteNodeVersionVOList.size() + "/" + contentVersionVOList.size()));
				processBean.updateProcess(getLocalizedString(getLocale(), "tool.structuretool.publicationProcess.gettingMetaData"));
				
				Database db = CastorDatabaseService.getDatabase();
	
		        beginTransaction(db);
	
		        try
		        {
		        	boolean skipDisplayName = false;
					for(SiteNodeVersionVO snVO : siteNodeVersionVOList)
					{
						if(snVO.getStateId() == 0)
						{
							Integer pageProtectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(snVO.getId());
							if(pageProtectedSiteNodeVersionId != null)
							{
								boolean hasAnonymousUserAccess = AccessRightController.getController().getIsPrincipalAuthorized(db, UserControllerProxy.getController().getUser(CmsPropertyHandler.getAnonymousUser()), "SiteNodeVersion.Read", pageProtectedSiteNodeVersionId.toString(), false);
								if(!hasAnonymousUserAccess)
								{
									anonymousNoAccessWarning = true;
									snVO.setHasAnonymousUserAccess(false);
								}
							}
							
							if(!skipDisplayName)
							{
								InfoGluePrincipal principal = (InfoGluePrincipal)getInfoGluePrincipal(snVO.getVersionModifier(), db);
								if(principal != null)
								{
									if(principal.getName().equalsIgnoreCase(principal.getDisplayName()))
										skipDisplayName = true;
									
									snVO.setVersionModifierDisplayName(principal.getDisplayName());
								}
							}
							snVO.setPath(getSiteNodePath(snVO.getSiteNodeId(), db));
						}
						else
							logger.info("Not adding siteNodeVersion..");
					}

				    this.siteNodeVersionVOList.addAll(siteNodeVersionVOList);
				    Collections.sort(this.siteNodeVersionVOList, Collections.reverseOrder(new ReflectionComparator("modifiedDateTime")));
	
					processBean.updateProcess("Getting modifier and path to found contents.");
	
					for(ContentVersionVO contentVersionVO : contentVersionVOList)
					{
						if(contentVersionVO.getStateId() == 0)	
						{
							Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId, db);
							if(protectedContentId != null)
							{
								boolean hasAnonymousUserAccess = AccessRightController.getController().getIsPrincipalAuthorized(db, UserControllerProxy.getController().getUser(CmsPropertyHandler.getAnonymousUser()), "Content.Read", protectedContentId.toString(), false);
								if(!hasAnonymousUserAccess)
								{
									anonymousNoAccessWarning = true;
									contentVersionVO.setHasAnonymousUserAccess(false);
								}
							}

							if(!skipDisplayName)
							{
								InfoGluePrincipal principal = (InfoGluePrincipal)getInfoGluePrincipal(contentVersionVO.getVersionModifier(), db);
								if(principal != null)
								{
									if(principal.getName().equalsIgnoreCase(principal.getDisplayName()))
										skipDisplayName = true;
									
									contentVersionVO.setVersionModifierDisplayName(principal.getDisplayName());
								}
							}
							contentVersionVO.setPath(getContentPath(contentVersionVO.getContentId(), db));
						}
						else
							logger.info("Not adding contentVersion..");
					}
	
				    this.contentVersionVOList.addAll(contentVersionVOList);
				    Collections.sort(this.contentVersionVOList, Collections.reverseOrder(new ReflectionComparator("modifiedDateTime")));
					
					commitTransaction(db);
		        }
		        catch(Exception e)
		        {
		            logger.error("An error occurred so we should not complete the transaction:" + e);
		            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
		            rollbackTransaction(db);
		            throw new SystemException(e.getMessage());
		        }
			}
		}
		finally
		{
			processBean.setStatus(ProcessBean.FINISHED);
			processBean.removeProcess();
		}
		
	    return "success";
	}

	public String doV3() throws Exception 
	{
		logUserActionInfo(getClass(), "doV3");
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


	public List<SiteNodeVersionVO> getSiteNodeVersions()
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
    
    public List<ContentVersionVO> getContentVersionVOList()
    {
        return contentVersionVOList;
    }
    
    public List<SiteNodeVersionVO> getSiteNodeVersionVOList()
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

	public boolean getHasAnonymousNoAccessWarning() 
	{
		return anonymousNoAccessWarning;
	}

}
