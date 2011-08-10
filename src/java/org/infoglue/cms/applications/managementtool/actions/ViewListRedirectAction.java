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

package org.infoglue.cms.applications.managementtool.actions;

import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.RedirectController;
import org.infoglue.cms.entities.management.RedirectVO;


/**
 * 	Action class for usecase ViewListRedirectUCC 
 *
 *  @author Mattias Bogeblad
 */

public class ViewListRedirectAction extends InfoGlueAbstractAction 
{
	private static final long serialVersionUID = 1L;

	private List<RedirectVO> userRedirects;
	private List<RedirectVO> systemRedirects;
	private String tabId = "userManaged";
	

	protected String doExecute() throws Exception 
	{
		this.userRedirects = RedirectController.getController().getUserRedirectVOList();
		this.systemRedirects = RedirectController.getController().getSystemRedirectVOList();
    	return "success";
	}
	

	public List getUserRedirects()
	{
		return this.userRedirects;		
	}
	
	public List getSystemRedirects()
	{
		return this.systemRedirects;		
	}

	public String getTabId() 
	{
		return tabId;
	}
	
	public void setTabId(String tabId) 
	{
		this.tabId = tabId;
	}
}
