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
 */

public class ToolbarButton
{
	private String id            			= "";
	private String text            			= "";
	private String title            		= "";
	private String backgroundImageURL   	= "";
	private String backgroundImageAlignment	= "left";
	private String cssClass   				= "";
	private String actionURL  	    		= "";
	private String target					= "inlineDiv";
	private Integer inlineDivPrefferredWidth  = 0;
	private Integer inlineDivPrefferredHeight = 0;
	private boolean isJavascript			= false;
	private boolean useConfirmation			= false;
	private String confirmationTitle		= null;
	private String confirmationText			= null;
	private List<ToolbarButton> subButtons 	= new ArrayList<ToolbarButton>();

	public ToolbarButton(String id, String text, String title, String actionURL, String cssClass)
	{
		this(id, text, title, actionURL, "", "left", cssClass, false, false, "", "", "inlineDiv", null, null);
	}

	public ToolbarButton(String id, String text, String title, String actionURL, String backgroundImageURL, String cssClass)
	{
		this(id, text, title, actionURL, backgroundImageURL, "left", cssClass, false, false, "", "", "inlineDiv", null, null);
	}

	public ToolbarButton(String id, String text, String title, String actionURL, String backgroundImageURL, String cssClass, String target)
	{
		this(id, text, title, actionURL, backgroundImageURL, "left", cssClass, false, false, "", "", target, null, null);
	}

	public ToolbarButton(String id, String text, String title, String actionURL, String backgroundImageURL, String backgroundImageAlignment, String cssClass, boolean isJavascript)
	{
		this(id, text, title, actionURL, backgroundImageURL, backgroundImageAlignment, cssClass, isJavascript, false, "", "", "inlineDiv", null, null);
	}

	public ToolbarButton(String id, String text, String title, String actionURL, String backgroundImageURL, String backgroundImageAlignment, String cssClass, boolean isJavascript, boolean useConfirmation, String confirmationTitle, String confirmationText, String target)
	{
		this(id, text, title, actionURL, backgroundImageURL, backgroundImageAlignment, cssClass, isJavascript, useConfirmation, confirmationTitle, confirmationText, target, null, null);
	}

	public ToolbarButton(String id, String text, String title, String actionURL, String backgroundImageURL, String backgroundImageAlignment, String cssClass, boolean isJavascript, boolean useConfirmation, String confirmationTitle, String confirmationText, String target, Integer inlineDivPrefferredWidth, Integer inlineDivPrefferredHeight)
	{
		this.id 						= id;
		this.text 						= text;
		this.title     					= title;
		this.backgroundImageURL 		= backgroundImageURL;
		this.backgroundImageAlignment 	= backgroundImageAlignment;
		this.cssClass					= cssClass;
		this.actionURL 					= actionURL;
		this.isJavascript 				= isJavascript;
		this.useConfirmation			= useConfirmation;
		this.confirmationTitle			= confirmationTitle;
		this.confirmationText			= confirmationText;
		this.target 					= target;
		this.inlineDivPrefferredHeight  = inlineDivPrefferredHeight;
		this.inlineDivPrefferredWidth	= inlineDivPrefferredWidth;
	}

	public String getId()
	{
		return id;
	}

	public String getText()
	{
		return text;
	}

	public String getTitle()
	{
		return title;
	}

	public String getBackgroundImageURL()
	{
		return backgroundImageURL;
	}

	public String getActionURL()
	{
		return actionURL;
	}

	public String getTarget()
	{
		return target;
	}

	public boolean getIsJavascript()
	{
		return isJavascript;
	}

	public List<ToolbarButton> getSubButtons()
	{
		return subButtons;
	}

	public String getBackgroundImageAlignment()
	{
		return backgroundImageAlignment;
	}

	public String getCssClass()
	{
		return cssClass;
	}
	
	public boolean useConfirmation() 
	{
		return useConfirmation;
	}

	public String getConfirmationTitle() 
	{
		return confirmationTitle;
	}

	public String getConfirmationText() 
	{
		return confirmationText;
	}

	public Integer getInlineDivPrefferredWidth() 
	{
		if(inlineDivPrefferredWidth == null)
			return 900;
		else
			return inlineDivPrefferredWidth;
	}

	public Integer getInlineDivPrefferredHeight() 
	{
		if(inlineDivPrefferredHeight == null)
			return 700;
		else
			return inlineDivPrefferredHeight;
	}


}