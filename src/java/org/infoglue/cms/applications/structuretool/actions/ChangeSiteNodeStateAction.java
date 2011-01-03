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

package org.infoglue.cms.applications.structuretool.actions;

import java.util.ArrayList;
import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeStateController;


public class ChangeSiteNodeStateAction extends InfoGlueAbstractAction
{
	private Integer siteNodeId;
	private Integer siteNodeVersionId;
	private Integer stateId;
	private String versionComment;
	private boolean overrideVersionModifyer = false;
	private String recipientFilter = null;
	
	//private SiteNodeVO siteNodeVO = new SiteNodeVO();	
	//private SiteNodeVersionVO siteNodeVersionVO = new SiteNodeVersionVO();	
		    
	/**
	 * This method gets called when calling this action. 
	 * If the stateId is 2 which equals that the user tries to prepublish the page. If so we
	 * ask the user for a comment as this is to be regarded as a new version. 
	 */
	   
    public String doExecute() throws Exception
    {      
    	//If the comment is not null we carry out the stateChange
    	if(getStateId().intValue() == 2 && getVersionComment() == null)
    	{
    		return "commentVersion";
    	}

    	List events = new ArrayList();

    	SiteNodeStateController.getController().changeState(getSiteNodeVersionId(), getStateId(), getVersionComment(), this.overrideVersionModifyer, this.recipientFilter, this.getInfoGluePrincipal(), getSiteNodeId(), events);
		
       	return "success";
    }
        
    public java.lang.Integer getSiteNodeVersionId()
    {
        return this.siteNodeVersionId;
    }
    
    public void setSiteNodeVersionId(java.lang.Integer siteNodeVersionId)
    {
	    this.siteNodeVersionId = siteNodeVersionId;
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
