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

package org.infoglue.cms.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.databeans.InfoglueTool;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.LabelController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.deliver.util.Timer;

public class DefaultToolsProvider implements ToolsProvider 
{
    private final static Logger logger = Logger.getLogger(DefaultToolsProvider.class.getName());

	public List<InfoglueTool> getTools(InfoGluePrincipal principal, Locale locale) 
	{
		Timer t = new Timer();
		
		List<InfoglueTool> tools = new ArrayList<InfoglueTool>();
		
		if(hasAccessTo(principal, "StructureTool.Read", true))
			tools.add(new InfoglueTool("StructureTool", "ViewStructureTool!V3.action", "", LabelController.getController(locale).getLocalizedString(locale, "tool.common.structureTool.name"), "A place to manage your sites", "structure", ""));
		if(hasAccessTo(principal, "ContentTool.Read", true))
			tools.add(new InfoglueTool("ContentTool", "ViewContentTool!V3.action", "", LabelController.getController(locale).getLocalizedString(locale, "tool.common.contentTool.name"), "A place to manage your sites", "content", ""));
		if(hasAccessTo(principal, "ManagementTool.Read", true))
			tools.add(new InfoglueTool("ManagementTool", "ViewManagementTool!V3.action", "", LabelController.getController(locale).getLocalizedString(locale, "tool.common.managementTool.name"), "A place to manage your sites", "management", ""));
		if(hasAccessTo(principal, "MyDesktopTool.Read", true))
			tools.add(new InfoglueTool("MyDesktopTool", "ViewMyDesktopTool!V3.action", "", LabelController.getController(locale).getLocalizedString(locale, "tool.common.myDesktopTool.name"), "A place to manage your sites", "mydesktop", ""));
		if(hasAccessTo(principal, "PublishingTool.Read", true))
			tools.add(new InfoglueTool("PublishingTool", "ViewPublishingTool!V3.action", "", LabelController.getController(locale).getLocalizedString(locale, "tool.common.publishingTool.name"), "A place to manage your sites", "publishing", ""));
		if(hasAccessTo(principal, "FormsTool.Read", true))
		{
			try 
			{
				RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithName("Infoglue form system");
				if(repositoryVO != null)
				{
					SiteNodeVO siteNodeVO = SiteNodeController.getController().getRootSiteNodeVO(repositoryVO.getId());
					if(siteNodeVO != null)
					{
						tools.add(new InfoglueTool("FormsTool", "ViewSiteNode.action?siteNodeId=" + siteNodeVO.getId(), "", LabelController.getController(locale).getLocalizedString(locale, "tool.common.formEditorTool.name"), "A place to manage your forms", "formeditor", ""));
					}
				}
			} 
			catch (Exception e) 
			{
				logger.error("Problem loading form system:" + e.getMessage(), e);
			}
		}
		if(hasAccessTo(principal, "CalendarTool.Read", true))
		{
			try 
			{
				RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithName("Infoglue calendar system");
				if(repositoryVO != null)
				{
					SiteNodeVO siteNodeVO = SiteNodeController.getController().getRootSiteNodeVO(repositoryVO.getId());
					LanguageVO languageVO = LanguageController.getController().getLanguageVOWithCode(locale.getLanguage());
					if(siteNodeVO != null)
					{
						tools.add(new InfoglueTool("CalendarTool", "ViewSiteNode.action?siteNodeId=" + siteNodeVO.getId() + "&languageId=" + languageVO.getLanguageId(), "", LabelController.getController(locale).getLocalizedString(locale, "tool.common.calendarTool.name"), "A place to manage your calendars", "calendareditor", ""));
					}
				}
			} 
			catch (Exception e) 
			{
				logger.error("Problem loading calendar system:" + e.getMessage(), e);
			}
		}
		if(hasAccessTo(principal, "SearchTool.Read", true))
			tools.add(new InfoglueTool("SearchTool", "Search.action?initSearch=true", "", LabelController.getController(locale).getLocalizedString(locale, "tool.common.searchTool.name"), "Global search", "search", ""));

		return tools;
	}

	public boolean hasAccessTo(InfoGluePrincipal principal, String interceptionPointName, boolean returnSuccessIfInterceptionPointNotDefined)
	{
		try
		{
			return AccessRightController.getController().getIsPrincipalAuthorized(principal, interceptionPointName, returnSuccessIfInterceptionPointNotDefined);
		}
		catch (SystemException e)
		{
			return false;
		}
	}
}
