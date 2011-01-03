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

package org.infoglue.cms.applications.common;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a button in the CMSTools menu.
 * Just a very simple javabean.
 */

public class ImageButton
{
	private boolean isSelfContained	= false;
	private String actionURL  	    = "";
	private String imageURL         = "";
	private Integer height			= new Integer(22);
	private Integer width			= new Integer(76);
	private String title            = "";
	private String tooltip 			= "";
	private List subButtons			= new ArrayList();

	public ImageButton(String actionURL, String imageURL, String title)
	{
		this.actionURL = actionURL;
		this.imageURL  = imageURL;
		this.title     = title;
	}

	public ImageButton(String actionURL, String imageURL, String title, Integer height, Integer width)
	{
		this.actionURL = actionURL;
		this.imageURL  = imageURL;
		this.title     = title;
		this.height    = height;
		this.width 	   = width;
	}

	public ImageButton(boolean isSelfContained, String actionURL, String imageURL, String title)
	{
		this.isSelfContained = isSelfContained;
		this.actionURL       = actionURL;
		this.imageURL        = imageURL;
		this.title           = title;
	}

	public ImageButton(boolean isSelfContained, String actionURL, String imageURL, String title, String tooltip)
	{
		this.tooltip 	     = tooltip;
		this.isSelfContained = isSelfContained;
		this.actionURL       = actionURL;
		this.imageURL        = imageURL;
		this.title           = title;
	}

	public ImageButton(boolean isSelfContained, String actionURL, String imageURL, String title, String tooltip, Integer height, Integer width)
	{
		this.tooltip         = tooltip;
		this.isSelfContained = isSelfContained;
		this.actionURL       = actionURL;
		this.imageURL        = imageURL;
		this.title           = title;
		this.height 		 = height;
		this.width 			 = width;
	}
	
	public boolean getIsSelfContained()
	{
		return this.isSelfContained;
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
	
	/**
	 * Returns the tooltip.
	 * @return String
	 */
	public String getTooltip() {
		return tooltip;
	}


	public Integer getHeight() 
	{
		return height;
	}
	
	public Integer getWidth() 
	{
		return width;
	}
	
    public List getSubButtons()
    {
        return subButtons;
    }
}