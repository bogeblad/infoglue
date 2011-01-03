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

package org.infoglue.cms.security;

import java.io.Serializable;


/**
 * This class represents an generic Group in InfoGlue. It is used to identify a group no matter what source it was defined in.
 * 
 * @author Mattias Bogeblad
 */

public class InfoGlueGroup implements Serializable
{
	private static final long serialVersionUID = -968607054134915601L;

	private final String name;
	private final String displayName;
	private final String description;
	private final AuthorizationModule autorizationModule;
	
	public InfoGlueGroup(String name, String description, AuthorizationModule autorizationModule)
	{
		this.name = name;
		this.displayName = name;
		this.description = description;
		this.autorizationModule = autorizationModule;
	}

	public InfoGlueGroup(String name, String displayName, String description, AuthorizationModule autorizationModule)
	{
		this.name = name;
		this.displayName = displayName;
		this.description = description;
		this.autorizationModule = autorizationModule;
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
		if (!(obj instanceof InfoGlueGroup))
			return false;
		
		InfoGlueGroup another = (InfoGlueGroup)obj;
		return name.toLowerCase().equals(another.getName().toLowerCase());
	}

	public int hasCode()
	{
		return name.hashCode();
	}

	public AuthorizationModule getAutorizationModule()
	{
		this.autorizationModule.setTransactionObject(null);
		return autorizationModule;
	}

}

