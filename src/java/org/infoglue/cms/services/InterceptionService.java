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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.infoglue.cms.applications.common.ToolbarButton;
import org.infoglue.cms.controllers.kernel.impl.simple.ToolbarController;
import org.infoglue.cms.entities.management.Interceptor;
import org.infoglue.cms.providers.ToolbarProvider;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.interceptors.InfoGlueInterceptor;

/**
 * This service supplies the admin tool with the buttons for the context sensitive toolbar. It is also appropriate for extending the platform with plugins.
 * You can register your own ToolbarProviders if you want.
 */

public class InterceptionService
{
	private static final long serialVersionUID = 1L;
	
	private Map<String,InfoGlueInterceptor> interceptors = new HashMap<String,InfoGlueInterceptor>();
	
	private static InterceptionService service = null;
	
	public InterceptionService()
	{
	}
	
	public static InterceptionService getService()
	{
		if(service == null)
			service = new InterceptionService();
		
		return service;
	}
	
	public void registerInterceptor(String name, InfoGlueInterceptor interceptor)
	{
		this.interceptors.put(name, interceptor);
	}
	
	public InfoGlueInterceptor getInterceptor(String name)
	{
		return this.interceptors.get(name);
	}



}