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

import org.infoglue.cms.providers.ContentDetailPageResolver;
import org.infoglue.cms.providers.DefaultContentDetailPageResolver;

/**
 * This service supplies the tool with content detail page resolver classes available. It is also appropriate for extending the platform with plugins.
 * You can register your own ToolbarProviders if you want.
 */

public class ContentDetailPageResolversService
{
	private static final long serialVersionUID = 1L;
	
	private List<ContentDetailPageResolver> contentDetailPageResolvers = new ArrayList<ContentDetailPageResolver>();
	
	private static ContentDetailPageResolversService service = null;
	
	public ContentDetailPageResolversService()
	{
		contentDetailPageResolvers.add(new DefaultContentDetailPageResolver());
	}
	
	public static ContentDetailPageResolversService getService()
	{
		if(service == null)
			service = new ContentDetailPageResolversService();
		
		return service;
	}
	
	public void registerContentDetailPageResolver(ContentDetailPageResolver contentDetailPageResolver)
	{
		this.contentDetailPageResolvers.add(contentDetailPageResolver);
	}
	
	public List<ContentDetailPageResolver> geContentDetailPageResolvers()
	{
		return contentDetailPageResolvers;	
	}

}