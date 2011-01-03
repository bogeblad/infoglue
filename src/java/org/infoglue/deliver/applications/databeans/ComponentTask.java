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

package org.infoglue.deliver.applications.databeans;


/**
 * 
 */

public class ComponentTask
{
	private String name;
	private String view;
	private String icon;
	private Integer componentId;	
	private boolean openInPopup;
		
    public Integer getComponentId()
    {
        return componentId;
    }
    
    public void setComponentId(Integer componentId)
    {
        this.componentId = componentId;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getView()
    {
        return view;
    }
    
    public void setView(String view)
    {
        this.view = view;
    }

	public boolean getOpenInPopup()
	{
		return openInPopup;
	}

	public void setOpenInPopup(boolean openInPopup)
	{
		this.openInPopup = openInPopup;
	}

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}
}