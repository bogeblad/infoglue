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

package org.infoglue.cms.applications.common.actions;

import java.util.ArrayList;
import java.util.List;

import org.infoglue.cms.applications.common.ToolbarButton;
import org.infoglue.cms.applications.common.ToolbarButtonGroup;


/**
 * @author Mattias Bogeblad
 *
 *	This action returns a set of toolbar buttons suitable for the toolbar key sent in.
 */

public class ViewToolbarButtonsAction extends InfoGlueAbstractAction 
{
	private static final long serialVersionUID = 8512298644737456785L;

	private String toolbarKey;
	private Boolean useEmblaToolbar;
	private List<ToolbarButton> buttons;
	private List<ToolbarButtonGroup> groups;

	public List<ToolbarButtonGroup> getGroups() 
	{
		return groups;
	}

	public List<ToolbarButton> getButtons() 
	{
		return buttons;
	}

	public String getToolbarKey() 
	{
		return toolbarKey;
	}

	public void setToolbarKey(String toolbarKey) 
	{
		this.toolbarKey = toolbarKey;
	}

	public Boolean getUseEmblaToolbar() 
	{
		return useEmblaToolbar;
	}

	public String doExecute() throws Exception 
	{
		logUserActionInfo(getClass(), "doExecute");
		try
		{
			this.buttons = this.getToolbarButtons(toolbarKey, getRequest());			
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return SUCCESS;
	}

	public String doEmbla() throws Exception 
	{
		logUserActionInfo(getClass(), "doEmbla");
		try
		{
			this.useEmblaToolbar = true;
			
			this.buttons = this.getToolbarButtons(toolbarKey, getRequest());			

			if(!getToolbarVariant().equals("compact"))
			{
				groupButtons(this.buttons, this.toolbarKey);
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return SUCCESS;
	}
	
	public void groupButtons(List<ToolbarButton> buttons, String toolbarKey)
	{
		this.groups = new ArrayList<ToolbarButtonGroup>();
		
		if(toolbarKey.equalsIgnoreCase("tool.structuretool.siteNodeComponentsHeader"))
		{
			ToolbarButtonGroup crudGroup = new ToolbarButtonGroup("CRUD", "CRUD", "CRUD operations");
			crudGroup.addButton(findButton(buttons, "createSiteNode"));
			crudGroup.addButton(findButton(buttons, "pageDetail"));
			crudGroup.addButton(findButton(buttons, "deleteSiteNode"));
			crudGroup.addButton(findButton(buttons, "moveSiteNode"));
			this.groups.add(crudGroup);

			ToolbarButtonGroup previewGroup = new ToolbarButtonGroup("preview", "preview", "preview operations");
			previewGroup.addButton(findButton(buttons, "previewPage"));
			previewGroup.addButton(findButton(buttons, "previewMediumScreenPage"));	
			previewGroup.addButton(findButton(buttons, "previewSmallScreenPage"));	
			
			this.groups.add(previewGroup);

			ToolbarButtonGroup publishGroup = new ToolbarButtonGroup("publish", "publish", "publish operations");
			publishGroup.addButton(findButton(buttons, "publishPageStructure"));
			publishGroup.addButton(findButton(buttons, "publishCurrentPage"));
			publishGroup.addButton(findButton(buttons, "unpublishPage"));
			this.groups.add(publishGroup);
		}
	}
	
	public ToolbarButton findButton(List<ToolbarButton> buttons, String id)
	{
		for(ToolbarButton buttonCandidate : buttons)
		{
			if(buttonCandidate.getId().equals(id))
			{
				return buttonCandidate;
			}
			else
			{
				for(ToolbarButton subButtonCandidate : buttonCandidate.getSubButtons())
				{
					if(subButtonCandidate.getId().equals(id))
						return subButtonCandidate;
				}					
			}
		}
		
		return null;
	}
}
