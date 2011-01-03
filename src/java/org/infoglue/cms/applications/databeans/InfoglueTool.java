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

package org.infoglue.cms.applications.databeans;

import java.util.ArrayList;
import java.util.List;

/**
 * This bean represents a tool in InfoGlue.
 */

public class InfoglueTool
{
	private String toolName  	    = "";
	private String actionURL  	    = "";
	private String imageURL         = "";
	private String cssClass         = "";
	private String title            = "";
	private String tooltip 			= "";
	private String target 			= null;

	public InfoglueTool(String toolName, String actionURL, String imageURL, String title, String tooltip, String cssClass, String target)
	{
		this.toolName   = toolName;
		this.tooltip	= tooltip;
		this.actionURL  = actionURL;
		this.imageURL   = imageURL;
		this.title      = title;
		this.cssClass   = cssClass;
		this.target		= target;
	}

	public String getToolName()
	{
		return this.toolName;
	}

	public String getActionUrl()
	{
		return this.actionURL;
	}
	
	public String getImageUrl()
	{
		return this.imageURL;
	}
	
	public String getTitle()
	{
		return this.title;
	}
	
	public String getTooltip() 
	{
		return tooltip;
	}

	public String getCssClass() 
	{
		return cssClass;
	}

	public String getTarget() 
	{
		return target;
	}


}