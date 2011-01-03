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

import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
  * This is the action-class for UpdateSiteNodeVersionVersion
  * 
  * @author Mattias Bogeblad
  */

public class UpdateSiteNodeVersionAction extends ViewSiteNodeVersionAction 
{
	
	private SiteNodeVersionVO siteNodeVersionVO;
	private Integer siteNodeId;
	private Integer languageId;
	private Integer siteNodeVersionId;

	private ConstraintExceptionBuffer ceb;
	
	public UpdateSiteNodeVersionAction()
	{
		this(new SiteNodeVersionVO());
	}
	
	public UpdateSiteNodeVersionAction(SiteNodeVersionVO siteNodeVersionVO)
	{
		this.siteNodeVersionVO = siteNodeVersionVO;
		this.ceb = new ConstraintExceptionBuffer();	
	}
	
	public String doExecute() throws Exception
    {
		super.initialize(this.siteNodeId, this.languageId);
		
		ceb.throwIfNotEmpty();
    	
		return "success";
	}

	public String doSaveAndExit() throws Exception
    {
		doExecute();
						 
		return "saveAndExit";
	}

	public void setSiteNodeVersionId(Integer siteNodeVersionId)
	{
		this.siteNodeVersionVO.setSiteNodeVersionId(siteNodeVersionId);	
	}

    public java.lang.Integer getSiteNodeVersionId()
    {
        return this.siteNodeVersionVO.getSiteNodeVersionId();
    }

	public void setStateId(Integer stateId)
	{
		this.siteNodeVersionVO.setStateId(stateId);	
	}

    public java.lang.Integer getStateId()
    {
        return this.siteNodeVersionVO.getStateId();
    }

	public void setSiteNodeId(Integer siteNodeId)
	{
		this.siteNodeId = siteNodeId;	
	}

    public java.lang.Integer getSiteNodeId()
    {
        return this.siteNodeId;
    }

	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
	}

    public java.lang.Integer getLanguageId()
    {
        return this.languageId;
    }
            

}
