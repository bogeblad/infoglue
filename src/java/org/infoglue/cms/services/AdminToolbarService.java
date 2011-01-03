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
import org.infoglue.cms.controllers.kernel.impl.simple.ToolbarController;
import org.infoglue.cms.providers.ToolbarProvider;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * This service supplies the admin tool with the buttons for the context sensitive toolbar. It is also appropriate for extending the platform with plugins.
 * You can register your own ToolbarProviders if you want.
 */

public class AdminToolbarService
{
	private final static Logger logger = Logger.getLogger(AdminToolbarService.class.getName());

	private static final long serialVersionUID = 1L;
	
	private List<ToolbarProvider> toolbarProviders = new ArrayList<ToolbarProvider>();
	
	private static AdminToolbarService service = null;
	
	public AdminToolbarService()
	{
		toolbarProviders.add(new ToolbarController());
	}
	
	public static AdminToolbarService getService()
	{
		if(service == null)
			service = new AdminToolbarService();
		
		return service;
	}
	
	public void registerToolbarProvider(ToolbarProvider provider)
	{
		this.toolbarProviders.add(provider);
	}
	
	public List<ToolbarButton> getRightToolbarButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		for(ToolbarProvider toolbarProvider : toolbarProviders)
		{
			buttons.addAll(toolbarProvider.getRightToolbarButtons(toolbarKey, principal, locale, request, disableCloseButton));
		}
					
		return buttons;	
	}
	
	public List<ToolbarButton> getToolbarButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		for(ToolbarProvider toolbarProvider : toolbarProviders)
		{
			buttons.addAll(toolbarProvider.getToolbarButtons(toolbarKey, principal, locale, request, disableCloseButton));
		}
					
		return buttons;	
	}
	

	public List<ToolbarButton> getFooterToolbarButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
	{
		System.out.println("toolbarKey:" + toolbarKey);
		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
		for(ToolbarProvider toolbarProvider : toolbarProviders)
		{
			buttons.addAll(toolbarProvider.getFooterToolbarButtons(toolbarKey, principal, locale, request, disableCloseButton));
		}
					
		return buttons;	
	}


}