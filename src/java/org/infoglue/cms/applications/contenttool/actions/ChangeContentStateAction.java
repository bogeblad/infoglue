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
import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentStateController;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;


public class ChangeContentStateAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 2617481307791186845L;

	private Integer contentId;
	private Integer contentVersionId;
	private Integer stateId;
	private Integer languageId;
	private String versionComment;
	private boolean overrideVersionModifyer = false;
	private String recipientFilter = null;
	private String attributeName;
	
	//private ContentVO contentVO = new ContentVO();	
	//private ContentVersionVO contentVersionVO = new ContentVersionVO();	
		    
	/**
	 * This method gets called when calling this action. 
	 * If the stateId is 2 which equals that the user tries to prepublish the page. If so we
	 * ask the user for a comment as this is to be regarded as a new version. 
	 */
	   
    public String doExecute() throws Exception
    {
		AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
		Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
		if(this.stateId.intValue() == 2)
		{
			if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.SubmitToPublish", protectedContentId.toString()))
				ceb.add(new AccessConstraintException("Content.contentId", "1005"));
		}
		else
		{
			if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.CreateVersion", protectedContentId.toString()))
				ceb.add(new AccessConstraintException("Content.contentId", "1007"));			
		}
		
		ceb.throwIfNotEmpty();

    	//If the comment is not null we carry out the stateChange
    	if(getStateId().intValue() == 2 && getVersionComment() == null)
    	{
    		return "commentVersion";
    	}

    	List events = new ArrayList();
		ContentStateController.changeState(getContentVersionId(), getStateId(), getVersionComment(), this.overrideVersionModifyer, this.recipientFilter, this.getInfoGluePrincipal(), getContentId(), events);
		
		this.contentVersionId = null;
		
       	return "success";
    }

	public String doStandalone() throws Exception
	{      
		//If the comment is not null we carry out the stateChange
		if(getStateId().intValue() == 2 && getVersionComment() == null)
		{
			return "commentVersionStandalone";
		}

    	List events = new ArrayList();
		ContentStateController.changeState(getContentVersionId(), getStateId(), getVersionComment(), this.overrideVersionModifyer, this.recipientFilter, this.getInfoGluePrincipal(), getContentId(), events);

		this.contentVersionId = null;
		
		return "successStandalone";
	}
        
    public java.lang.Integer getContentVersionId()
    {
        return this.contentVersionId;
    }
    
    public void setContentVersionId(java.lang.Integer contentVersionId)
    {
	    this.contentVersionId = contentVersionId;
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
            
	public String getAttributeName()
	{
		return this.attributeName;
	}

	public void setAttributeName(String attributeName)
	{
		this.attributeName = attributeName;
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
}
