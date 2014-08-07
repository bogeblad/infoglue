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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.structure.SiteNodeVO;
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
	private String inline = "true";
	private String userSessionKey;


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

	public String doInactive() throws Exception
    {
		SiteNodeVersionController.getController().inactivate(this.siteNodeVersionId);
						 
		return "saveAndExit";
	}

	public String doReactivate() throws Exception
	{
		this.userSessionKey = "" + System.currentTimeMillis();
		SiteNodeVersionController.getController().reactivate(this.siteNodeVersionId, this.getInfoGluePrincipal());

		Locale locale = getLocale();
		SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
		String reactiveConfirmationMessage = getLocalizedString(locale, "tool.structuretool.reactivate.succesConfirmationMessage", siteNodeVO.getName());

		List<LinkBean> actionLinks = new LinkedList<LinkBean>();
		String javascriptAction = "refreshParent(null,null,null);parent.parent.closeInlineDiv();";
		actionLinks.add(new LinkBean("refreshParent", "", "", "", javascriptAction, true, "", "", "", ""));
		setActionLinks(userSessionKey, actionLinks);
		setActionExtraData(userSessionKey, "confirmationMessage", reactiveConfirmationMessage);
		setActionExtraData(userSessionKey, "siteNodeId", "" + this.siteNodeId);
		setActionExtraData(userSessionKey, "unrefreshedNodeId", "" + this.siteNodeId);
		return "successAndExit";
	}

	public void setSiteNodeVersionId(Integer siteNodeVersionId)
	{
		this.siteNodeVersionId = siteNodeVersionId;
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
         
	public String getInline() 
	{
		return inline;
	}

	public void setInline(String inline) {
		this.inline = inline;
	}

	public String getUserSessionKey()
	{
		return userSessionKey;
	}

}
