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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.Timer;

/**
 *
 *  @author Stefan Sik
 * 
 * Present a list of contentVersion under a given content, recursing down in the hierarcy
 * 
 */

public class ViewListContentVersionAction extends InfoGlueAbstractAction 
{
	private static final long serialVersionUID = -1767277488570719994L;

    private final static Logger logger = Logger.getLogger(ViewListContentVersionAction.class.getName());

	private List<ContentVersionVO> contentVersionVOList = new ArrayList<ContentVersionVO>();
	private List<SiteNodeVersionVO> siteNodeVersionVOList = new ArrayList<SiteNodeVersionVO>();
	private Integer contentId;
	private Integer repositoryId;
	private Integer languageId;

	private String returnAddress;
    private String originalAddress;
   	private String userSessionKey;

	protected String doExecute() throws Exception 
	{
		ProcessBean processBean = ProcessBean.createProcessBean(ViewListContentVersionAction.class.getName(), "" + getInfoGluePrincipal().getName());
		processBean.setStatus(ProcessBean.RUNNING);

		try
		{
			if(this.contentId != null)
			{
				Timer t = new Timer();
			    ContentVO contentVO = ContentController.getContentController().getContentVOWithId(this.contentId);
			    this.repositoryId = contentVO.getRepositoryId();
			    
				AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
			
				Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
				if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.SubmitToPublish", protectedContentId.toString()))
					ceb.add(new AccessConstraintException("Content.contentId", "1005"));
				
				ceb.throwIfNotEmpty();
	
				RequestAnalyser.getRequestAnalyser().registerComponentStatistics("ViewListContentVersion part 1", t.getElapsedTime());
				
				//Set<SiteNodeVersionVO> siteNodeVersionVOList = new HashSet<SiteNodeVersionVO>();
				//Set<ContentVersionVO> contentVersionVOList = new HashSet<ContentVersionVO>();
	
				ContentVersionController.getContentVersionController().getContentAndAffectedItemsRecursive(this.contentId, ContentVersionVO.WORKING_STATE, this.siteNodeVersionVOList, this.contentVersionVOList, true, true, processBean);
				RequestAnalyser.getRequestAnalyser().registerComponentStatistics("ViewListContentVersion getContentAndAffectedItemsRecursive", t.getElapsedTime());
				
				processBean.updateProcess("Found " + this.siteNodeVersionVOList.size() + " pages and " + this.contentVersionVOList.size() + " contents");
				
				/*
				Set<ContentVersionVO> contentVersionVOListSet = new HashSet<ContentVersionVO>();
				for(Integer contentVersionId : contentVersionVOSet)
				{
					contentVersionVOListSet.add(ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId));
				}
				*/
				
				RequestAnalyser.getRequestAnalyser().registerComponentStatistics("ViewListContentVersion versions", t.getElapsedTime());
	
				Database db = CastorDatabaseService.getDatabase();
	
		        beginTransaction(db);
	
		        try
		        {
		        	boolean skipDisplayName = false;
					for(ContentVersionVO contentVersionVO : contentVersionVOList)
					{
						if(contentVersionVO.getStateId() == 0)	
						{
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
							contentVersionVO.setLanguageName(LanguageController.getController().getLanguageVOWithId(contentVersionVO.getLanguageId()).getName());
						}
						else
							logger.info("Not adding contentVersion..");
					}
					
					for(SiteNodeVersionVO snVO : siteNodeVersionVOList)
					{
						if(snVO.getStateId() == 0)
						{
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
					
					commitTransaction(db);
		        }
		        catch(Exception e)
		        {
		            logger.error("An error occurred so we should not complete the transaction:" + e);
		            logger.warn("An error occurred so we should not complete the transaction:" + e, e);
		            rollbackTransaction(db);
		            throw new SystemException(e.getMessage());
		        }
	
				processBean.updateProcess("Added metadata");

			    Collections.sort(contentVersionVOList, Collections.reverseOrder(new ReflectionComparator("modifiedDateTime")));
			    		
				Set siteNodeVersionVOListSet = new HashSet();
				siteNodeVersionVOListSet.addAll(siteNodeVersionVOList);
				siteNodeVersionVOList.clear();
				siteNodeVersionVOList.addAll(siteNodeVersionVOListSet);
	
			    Collections.sort(siteNodeVersionVOList, Collections.reverseOrder(new ReflectionComparator("modifiedDateTime")));
			    
			    RequestAnalyser.getRequestAnalyser().registerComponentStatistics("ViewListContentVersion end", t.getElapsedTime());
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
		doExecute();
		
        userSessionKey = "" + System.currentTimeMillis();
        
        addActionLink(userSessionKey, new LinkBean("currentPageUrl", getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentContentLinkText"), getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentContentTitleText"), getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentContentTitleText"), this.originalAddress, false, ""));
        
        setActionExtraData(userSessionKey, "repositoryId", "" + this.repositoryId);
        setActionExtraData(userSessionKey, "contentId", "" + this.contentId);
        setActionExtraData(userSessionKey, "unrefreshedContentId", "" + this.contentId);
        setActionExtraData(userSessionKey, "unrefreshedNodeId", "" + this.contentId);
        setActionExtraData(userSessionKey, "languageId", "" + this.languageId);
        setActionExtraData(userSessionKey, "changeTypeId", "1");

        setActionExtraData(userSessionKey, "disableCloseLink", "true");
        
	    return "successV3";	
	}

	/**
	 * @return
	 */
	public Integer getContentId() 
	{
		return contentId;
	}

	/**
	 * @param integer
	 */
	public void setContentId(Integer integer) 
	{
		contentId = integer;
	}

    public Integer getRepositoryId()
    {
        return repositoryId;
    }

    public void setLanguageId(Integer languageId) 
	{
		this.languageId = languageId;
	}
    
    public Integer getLanguageId()
    {
        return languageId;
    }

    public List getContentVersionVOList()
    {
        return contentVersionVOList;
    }
    
    public List getSiteNodeVersionVOList()
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
