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
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class represents an generic InfoGluePrincipal in InfoGlue. It is used to identify a user no matter what source it was defined in.
 * 
 * @author Mattias Bogeblad
 */

public class InfoGluePrincipalBean implements Principal, Serializable
{
	private static final long serialVersionUID = 7252014421006767620L;
	
	private String name;
	private String displayName;
	private String firstName;
	private String lastName;
	private String email;
	private List roles;
	private List groups;
	private Map metaInformation = Collections.emptyMap();
	private boolean isAdministrator;

	public InfoGluePrincipalBean()
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

	public String getFirstName() 
	{
		return firstName;
	}
	
	public String getLastName() 
	{
		return lastName;
	}

	public String getEmail()
	{
		return email;
	}

	public List getRoles()
	{
		return Collections.unmodifiableList(roles);
	}
	
    public List getGroups()
    {
        return Collections.unmodifiableList(groups);
    }

	public void setGroups(List groups)
	{
		this.groups = groups;
	}

	public void setRoles(List roles)
	{
		this.roles = roles;
	}

	public Map getMetaInformation() 
	{
		return Collections.unmodifiableMap(metaInformation);
	}

	public boolean getIsAdministrator()
	{
		return isAdministrator;
	}
	
	public String toString()
	{
        return name;
        /*
		StringBuffer sb = new StringBuffer("InfoGluePrincipal: " + name + ":" + email + ":" + isAdministrator + '\n');
		for(Iterator i=roles.iterator(); i.hasNext();)
		{ 
			InfoGlueRole role = (InfoGlueRole)i.next();
			sb.append("" + role.getName() + ",");
		}
		sb.append("]");
		
		return sb.toString();
        */
	}

	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof InfoGluePrincipalBean))
			return false;
		
		InfoGluePrincipalBean another = (InfoGluePrincipalBean)obj;
		return name.equals(another.getName());
	}

	public int hasCode()
	{
		return name.hashCode();
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public void setAdministrator(boolean isAdministrator)
	{
		this.isAdministrator = isAdministrator;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public void setMetaInformation(Map metaInformation)
	{
		this.metaInformation = metaInformation;
	}

	public void setName(String name)
	{
		this.name = name;
	}

}

