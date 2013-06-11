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

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * This action represents the Move Digital Asset Usecase.
 */

public class MoveDigitalAssetAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
    private final static Logger logger = Logger.getLogger(MoveDigitalAssetAction.class.getName());

    private Integer digitalAssetId;
	private Integer contentId;
	private Integer contentVersionId;
	
	private Integer repositoryId;
   	private String userSessionKey;
    private String originalAddress;
    private String returnAddress;

    private ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();	
   	

	public Integer getContentVersionId() 
	{
		return contentVersionId;
	}

	public void setContentVersionId(Integer contentVersionId) 
	{
		this.contentVersionId = contentVersionId;
	}

    public Integer getContentId() 
	{
		return contentId;
	}

	public void setContentId(Integer contentId) 
	{
		this.contentId = contentId;
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}
	
	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}
	
	public Integer getDigitalAssetId() 
	{
		return digitalAssetId;
	}

    public void setDigitalAssetId(Integer digitalAssetId) 
    {
		this.digitalAssetId = digitalAssetId;
	}

	public String getUserSessionKey()
	{
		return userSessionKey;
	}

	public void setUserSessionKey(String userSessionKey)
	{
		this.userSessionKey = userSessionKey; 
	}

    public String doInput() throws Exception
    {		
        userSessionKey = "" + System.currentTimeMillis();

        
		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(getContentVersionId());
		ContentVO contentVO = ContentControllerProxy.getContentController().getContentVOWithId(contentVersionVO.getContentId());
		setRepositoryId(contentVO.getRepositoryId());
		
        String moveContentInlineOperationDoneHeader = getLocalizedString(getLocale(), "tool.contenttool.moveContentInlineOperationDoneHeader", contentVO.getName());
		String moveContentInlineOperationBackToCurrentPageLinkText = getLocalizedString(getLocale(), "tool.contenttool.moveContentInlineOperationBackToCurrentContentLinkText");
		String moveContentInlineOperationBackToCurrentPageTitleText = getLocalizedString(getLocale(), "tool.contenttool.moveContentInlineOperationBackToCurrentContentTitleText");

	    setActionMessage(userSessionKey, moveContentInlineOperationDoneHeader);
	    addActionLink(userSessionKey, new LinkBean("currentPageUrl", moveContentInlineOperationBackToCurrentPageLinkText, moveContentInlineOperationBackToCurrentPageTitleText, moveContentInlineOperationBackToCurrentPageTitleText, this.originalAddress, false, ""));
		
		return "input";
    }


    public String doExecute() throws Exception
    {
        try
        {
            ceb.throwIfNotEmpty();
        	
    		ContentControllerProxy.getController().acMoveDigitalAsset(this.getInfoGluePrincipal(), this.getDigitalAssetId(), this.getContentId());

    	    addActionLinkFirst(userSessionKey, new LinkBean("parent.parent.refreshView('contentVersionAssets');", "", "", "", "parent.parent.refreshView('contentVersionAssets');closeDialog();", true, ""));
    	    setActionExtraData(userSessionKey, "confirmationMessage", getLocalizedString(getLocale(), "tool.contenttool.assetMoved.confirmation", getContentVO(this.getContentId()).getName()));
        }
        catch(ConstraintException ce)
        {
        	logger.warn("An error occurred so we should not complete the transaction:" + ce);

			ce.setResult(INPUT);
			throw ce;
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            throw new SystemException(e.getMessage());
        }
    	        
        return "success";
    }
	
	public String getReturnAddress()
	{
		return this.returnAddress;
	}    
	
    public String getOriginalAddress()
	{
		return originalAddress;
	}

	public void setOriginalAddress(String originalAddress)
	{
		this.originalAddress = originalAddress;
	}

	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}

}
