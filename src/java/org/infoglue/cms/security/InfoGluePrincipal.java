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
import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infoglue.cms.util.DateHelper;


/**
 * This class represents an generic InfoGluePrincipal in InfoGlue. It is used to identify a user no matter what source it was defined in.
 * 
 * @author Mattias Bogeblad
 */

public class InfoGluePrincipal implements Principal, Serializable
{
	private static final long serialVersionUID = 7252014421006767620L;
	
	private final String name;
	private final String displayName;
	private final String firstName;
	private final String lastName;
	private final String email;
	private final String source;
	private final Boolean isActive;
	private final Date modifiedDateTime;
	private final List<InfoGlueRole> roles;
	private final List<InfoGlueGroup> groups;
	private Map metaInformation;
	private final boolean isAdministrator;
	private final AuthorizationModule autorizationModule;

	public InfoGluePrincipal(String name, String firstName, String lastName, String email, List roles, List groups, boolean isAdministrator, AuthorizationModule autorizationModule)
	{
		this(name, name, firstName, lastName, email, roles, groups, isAdministrator, autorizationModule);
	}
	
	public InfoGluePrincipal(String name, String displayName, String firstName, String lastName, String email, List roles, List groups, boolean isAdministrator, AuthorizationModule autorizationModule)
	{
		InfoGlueRole infoGlueRole = new InfoGlueRole("anonymous", "anonymous", "The default anonymous role", autorizationModule);
		if(roles != null)
			roles.add(infoGlueRole);

		this.name = name;
		this.displayName = displayName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.source = "infoglue";
		this.isActive = true;
		this.modifiedDateTime = DateHelper.getSecondPreciseDate();
		this.roles = roles;
		this.groups = groups;
		this.metaInformation = new HashMap();
		this.isAdministrator = isAdministrator;
		this.autorizationModule = autorizationModule;
	}

	public InfoGluePrincipal(String name, String firstName, String lastName, String email, List roles, List groups, Map metaInformation, boolean isAdministrator, AuthorizationModule autorizationModule)
	{
		this(name, name, firstName, lastName, email, roles, groups, metaInformation, isAdministrator, autorizationModule);
	}
	
	public InfoGluePrincipal(String name, String displayName, String firstName, String lastName, String email, List roles, List groups, Map metaInformation, boolean isAdministrator, AuthorizationModule autorizationModule)
	{
		InfoGlueRole infoGlueRole = new InfoGlueRole("anonymous", "anonymous", "The default anonymous role", autorizationModule);
		roles.add(infoGlueRole);

		this.name = name;
		this.displayName = displayName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.source = "infoglue";
		this.isActive = true;
		this.modifiedDateTime = DateHelper.getSecondPreciseDate();
		this.roles = roles;
		this.groups = groups;
		this.metaInformation = metaInformation;
		this.isAdministrator = isAdministrator;
		this.autorizationModule = autorizationModule;
	}

	public InfoGluePrincipal(String name, String displayName, String firstName, String lastName, String email, String source, Boolean isActive, Date modifiedDateTime, List roles, List groups, Map metaInformation, boolean isAdministrator, AuthorizationModule autorizationModule)
	{
		InfoGlueRole infoGlueRole = new InfoGlueRole("anonymous", "anonymous", "The default anonymous role", autorizationModule);
		roles.add(infoGlueRole);

		this.name = name;
		this.displayName = displayName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.source = source;
		this.isActive = isActive;
		this.modifiedDateTime = modifiedDateTime;
		this.roles = roles;
		this.groups = groups;
		this.metaInformation = metaInformation;
		this.isAdministrator = isAdministrator;
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

	public String getSource()
	{
		return source;
	}
		
	public Boolean getIsActive()
	{
		return isActive;
	}
	
	public Date getModifiedDateTime()
	{
		return modifiedDateTime;
	}

	public List<InfoGlueRole> getRoles()
	{
		return Collections.unmodifiableList(roles);
	}
	
    public List<InfoGlueGroup> getGroups()
    {
        return Collections.unmodifiableList(groups);
    }
    
	public Map getMetaInformation() 
	{
		return metaInformation;
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
		if (!(obj instanceof InfoGluePrincipal))
			return false;
		
		InfoGluePrincipal another = (InfoGluePrincipal)obj;
		return name.equals(another.getName());
	}

	public int hasCode()
	{
		return name.hashCode();
	}

	public AuthorizationModule getAutorizationModule()
	{
		autorizationModule.setTransactionObject(null);
		return autorizationModule;
	}

}

