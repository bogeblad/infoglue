/* ===============================================================================
*
* Part of the InfoGlue Content Management Platform (www.infoglue.org)
*
* ===============================================================================
*
* Copyright (C) Mattias Bogeblad
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

package org.infoglue.cms.entities.mydesktop;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * This contains a shortcut object.
 *
 * @author Mattias Bogeblad
 */

public class ShortcutVO implements BaseEntityVO
{
	private String name;  
	private String URL; 
	private boolean popup = true;
	
	private ShortcutVO() {}

	public ShortcutVO(String name, String URL, boolean popup)
	{
		setName(name);
		setURL(URL);
		setPopup(popup);
	}

    public Integer getId()
    {
        return null;
    }

	public String getName()
	{
		return name;
	}

	private void setName(String name)
	{
		this.name = name;
	}

    public String getURL()
    {
        return URL;
    }

    private void setURL(String url)
    {
        URL = url;
    }

    public boolean getPopup()
    {
        return popup;
    }

    private void setPopup(boolean popup)
    {
        this.popup = popup;
    }

	public ConstraintExceptionBuffer validate()
	{
		return new ConstraintExceptionBuffer();
	}

}
