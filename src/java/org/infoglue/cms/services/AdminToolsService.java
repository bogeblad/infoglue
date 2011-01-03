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

package org.infoglue.cms.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.ToolbarButton;
import org.infoglue.cms.applications.databeans.InfoglueTool;
import org.infoglue.cms.controllers.kernel.impl.simple.ToolbarController;
import org.infoglue.cms.providers.DefaultToolsProvider;
import org.infoglue.cms.providers.ToolbarProvider;
import org.infoglue.cms.providers.ToolsProvider;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * This service supplies the admin tool with the buttons for the context sensitive toolbar. It is also appropriate for extending the platform with plugins.
 * You can register your own ToolbarProviders if you want.
 */

public class AdminToolsService
{
	private final static Logger logger = Logger.getLogger(AdminToolsService.class.getName());

	private static final long serialVersionUID = 1L;
	
	private List<ToolsProvider> toolsProviders = new ArrayList<ToolsProvider>();
	
	private static AdminToolsService service = null;
	
	public AdminToolsService()
	{
		toolsProviders.add(new DefaultToolsProvider());
	}
	
	public static AdminToolsService getService()
	{
		if(service == null)
			service = new AdminToolsService();
		
		return service;
	}
	
	public void registerToolsProvider(ToolsProvider provider)
	{
		this.toolsProviders.add(provider);
	}
	
	public List<InfoglueTool> getTools(InfoGluePrincipal principal, Locale locale)
	{
		List<InfoglueTool> infoglueTools = new ArrayList<InfoglueTool>();
		for(ToolsProvider toolsProvider : toolsProviders)
		{
			try 
			{
				infoglueTools.addAll(toolsProvider.getTools(principal, locale));
			}
			catch(Throwable e)
			{
				logger.error("Error loading tools from provider:" + toolsProvider.getClass().getName() + ":" + e.getMessage(), e);
			}
		}
		
		return infoglueTools;	
	}

}