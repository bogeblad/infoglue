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

package org.infoglue.common.security.beans;

import java.io.Serializable;


/**
 * This class represents an generic Group in InfoGlue. It is used to identify a group no matter what source it was defined in.
 * 
 * @author Mattias Bogeblad
 */

public class InfoGlueGroupBean implements Serializable
{
	private static final long serialVersionUID = -968607054134915601L;

	private String name;
	private String displayName;
	private String description;
	
	public InfoGlueGroupBean()
	{
	}
	
	public String getName()
	{
		return name;
	}

	public String getDisplayName() 
	{
		return displayName;
	}

	public String getDescription()
	{
		return description;
	}
	
	public String toString()
	{
		return "InfoGlueGroup: " + name + "(" + displayName + ")";
	}

	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof InfoGlueGroupBean))
			return false;
		
		InfoGlueGroupBean another = (InfoGlueGroupBean)obj;
		return name.toLowerCase().equals(another.getName().toLowerCase());
	}

	public int hasCode()
	{
		return name.hashCode();
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public void setName(String name)
	{
		this.name = name;
	}

}

