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
 
package org.infoglue.deliver.controllers.kernel.impl.simple;

import org.infoglue.deliver.applications.actions.InfoGlueComponent;

public class DecoratedComponentLogic extends ComponentLogic 
{
	private TemplateController templateController = null;
	private InfoGlueComponent infoGlueComponent = null;

	public DecoratedComponentLogic(TemplateController templateController, InfoGlueComponent infoGlueComponent)
	{
		super(templateController, infoGlueComponent);
		this.templateController = templateController;
		this.infoGlueComponent = infoGlueComponent;
	}
	
	/**
	 * This method returns a url to the given page. The url is composed of siteNode, language and content
	 */
	/*
	public String getPageUrl(Integer siteNodeId, Integer languageId, Integer contentId)
	{
		String pageUrl = "";
			
		try
		{
			if(siteNodeId == null)
				siteNodeId = new Integer(-1);
	
			if(languageId == null)
				languageId = new Integer(-1);
	
			if(contentId == null)
				contentId = new Integer(-1);
			
			String arguments = "siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId;
			
			String dnsName = CmsPropertyHandler.getComponentRendererUrl();
			
			if(dnsName.endsWith("/"))
			    pageUrl = dnsName + CmsPropertyHandler.getComponentRendererAction() + "?" + arguments;
			else
			    pageUrl = dnsName + "/" + CmsPropertyHandler.getComponentRendererAction() + "?" + arguments;
		}
		catch(Exception e)
		{
			e.printStackTrace();		
		}
		
		return pageUrl;
	}
	*/
}