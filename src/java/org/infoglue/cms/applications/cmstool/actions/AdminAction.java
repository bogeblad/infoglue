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

package org.infoglue.cms.applications.cmstool.actions;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * This class implements the action class for the base fram for the entire tool.
 * 
 * @author Mattias Bogeblad
 */

public class AdminAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = -2904286525405758091L;

    private final static Logger logger = Logger.getLogger(AdminAction.class.getName());

	public String doExecute() throws Exception
    {
		logUserActionInfo(getClass(), "doExecute");
		String preferredGUI = CmsPropertyHandler.getDefaultGUI(getUserName());
		if(preferredGUI.equalsIgnoreCase("classic"))
			return "successClassic";
		else
			return "success";
    }

	public String doEmbla() throws Exception
    {
		logUserActionInfo(getClass(), "doEmbla");
		String preferredGUI = CmsPropertyHandler.getDefaultGUI(getUserName());
		if(preferredGUI.equalsIgnoreCase("classic"))
			return "successClassic";
		else
			return "successEmbla";
    }

	public String doResetGUI() throws Exception
    {
		logUserActionInfo(getClass(), "doResetGUI");
		this.getHttpSession().removeAttribute("repositoryId");
		setLanguageCode(CmsPropertyHandler.getPreferredLanguageCode(getUserName()));
		
		return "successReset";
    }
	
	public Integer getSiteNodeId()
	{
		try
		{
			if(getRequest().getParameter("siteNodeId") != null && !getRequest().getParameter("siteNodeId").equals(""))
				return new Integer(getRequest().getParameter("siteNodeId"));
		}
		catch (Exception e) 
		{
			logger.error("Error getting siteNodeId: " + e.getMessage());
		}
		return null;
	}
	
	public Integer getContentId()
	{
		try
		{
			if(getRequest().getParameter("contentId") != null && !getRequest().getParameter("contentId").equals(""))
				return new Integer(getRequest().getParameter("contentId"));
		}
		catch (Exception e) 
		{
			logger.error("Error getting contentId: " + e.getMessage());
		}
		return null;
	}
	
	public Integer getLanguageId()
	{
		try
		{
			if(getRequest().getParameter("languageId") != null && !getRequest().getParameter("languageId").equals(""))
				return new Integer(getRequest().getParameter("languageId"));
		}
		catch (Exception e) 
		{
			logger.error("Error getting languageId: " + e.getMessage());
		}
		return null;
	}
	
	public String getRepositoryName(String toolName) throws Exception
    {
		String repositoryName = "";
		Integer repositoryId = getRepositoryId();
		if(toolName.equals("ContentTool"))
		{
			repositoryId = getContentRepositoryId();
		}
		else if(toolName.equals("StructureTool"))
		{
			repositoryId = getStructureRepositoryId();
		}
		
		RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);
		if(repositoryVO != null)
		{
			repositoryName = repositoryVO.getName();
		}
		
		return repositoryName;
    }
}
