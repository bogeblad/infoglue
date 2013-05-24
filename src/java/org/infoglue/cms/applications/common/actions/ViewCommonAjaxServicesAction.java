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

package org.infoglue.cms.applications.common.actions;

import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;

/**
 * This class implements the action class for the framed page in the content tool.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewCommonAjaxServicesAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewCommonAjaxServicesAction.class.getName());

	private static final long serialVersionUID = 1L;

	private List repositories = null;
	
	public String doRepositories() throws Exception
    {
		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(getInfoGluePrincipal(), false);
        
		return "successRepositories";
    }

	public String doSiteNodeIdPath() throws Exception
    {
		String siteNodeIdPath = SiteNodeController.getController().getSiteNodeIdPath(new Integer(getRequest().getParameter("siteNodeId")));
		this.getResponse().getWriter().print("" + siteNodeIdPath);
		
		return NONE;
    }

	public String doContentIdPath() throws Exception
    {
		String contentIdPath = ContentController.getContentController().getContentIdPath(new Integer(getRequest().getParameter("contentId")));
		this.getResponse().getWriter().print("" + contentIdPath);
		
		return NONE;
    }

	public String doExecute() throws Exception
    {
		
        return "success";
    }

	
	public List getRepositories()
	{
		return repositories;
	}

}
