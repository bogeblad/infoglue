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

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;
import org.infoglue.cms.util.sorters.ReflectionComparator;

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

	private List contentVersionVOList = new ArrayList();
	private List siteNodeVersionVOList = new ArrayList();
	private Integer contentId;
	private Integer repositoryId;

	private String returnAddress;
    private String originalAddress;
   	private String userSessionKey;

	protected String doExecute() throws Exception 
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

			ContentVersionController.getContentVersionController().getContentAndAffectedItemsRecursive(this.contentId, ContentVersionVO.WORKING_STATE, this.siteNodeVersionVOList, this.contentVersionVOList, true, true);

			Set contentVersionVOListSet = new HashSet();
			contentVersionVOListSet.addAll(contentVersionVOList);
			contentVersionVOList.clear();
			contentVersionVOList.addAll(contentVersionVOListSet);

		    Collections.sort(contentVersionVOList, Collections.reverseOrder(new ReflectionComparator("modifiedDateTime")));
		    		
			Set siteNodeVersionVOListSet = new HashSet();
			siteNodeVersionVOListSet.addAll(siteNodeVersionVOList);
			siteNodeVersionVOList.clear();
			siteNodeVersionVOList.addAll(siteNodeVersionVOListSet);

		    Collections.sort(siteNodeVersionVOList, Collections.reverseOrder(new ReflectionComparator("modifiedDateTime")));
		}

	    return "success";
	}

	public String doV3() throws Exception 
	{
		doExecute();
		
        userSessionKey = "" + System.currentTimeMillis();
        
        addActionLink(userSessionKey, new LinkBean("currentPageUrl", getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentContentLinkText"), getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentContentTitleText"), getLocalizedString(getLocale(), "tool.common.publishing.publishingInlineOperationBackToCurrentContentTitleText"), this.originalAddress, false, ""));
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
