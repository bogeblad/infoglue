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

package org.infoglue.cms.entities.management;

import java.util.ArrayList;
import java.util.List;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class AccessRightVO implements BaseEntityVO, Cloneable
{
	private java.lang.Integer accessRightId;
	private java.lang.String name;
	private java.lang.Integer interceptionPointId;
	private java.lang.String interceptionPointName;
	private java.lang.String parameters = "";

	private List roles = new ArrayList();
	private List groups = new ArrayList();
	private List users = new ArrayList();
	
	public Integer getId() 
	{
		return getAccessRightId();
	}

    public java.lang.Integer getAccessRightId()
    {
        return this.accessRightId;
    }
                
    public void setAccessRightId(java.lang.Integer accessRightId)
    {
        this.accessRightId = accessRightId;
    }
    
	public java.lang.String getName()
	{
		return this.name;
	}

	public void setName(java.lang.String name)
	{
		this.name = name;
	}

	public java.lang.String getParameters()
	{
		return this.parameters;
	}

	public void setParameters(java.lang.String parameters)
	{
		this.parameters = parameters;
	}
/*
	public java.lang.String getRoleName()
	{
		return this.roleName;
	}

	public void setRoleName(java.lang.String roleName)
	{
		this.roleName = roleName;
	}
*/

	public ConstraintExceptionBuffer validate() 
	{
    	
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		//if (name != null) ValidatorFactory.createStringValidator("Access.name", true, 3, 50, true, RoleImpl.class, this.getId()).validate(name, ceb);

		return ceb;
	}

	public AccessRightVO createCopy() throws Exception
	{
		return (AccessRightVO)this.clone();
	}
	
    public Integer getInterceptionPointId()
    {
        return interceptionPointId;
    }
    
    public void setInterceptionPointId(Integer interceptionPointId)
    {
        this.interceptionPointId = interceptionPointId;
    }
    
    public String getInterceptionPointName()
    {
        return interceptionPointName;
    }
    
    public void setInterceptionPointName(String interceptionPointName)
    {
        this.interceptionPointName = interceptionPointName;
    }

	public List getGroups()
	{
		return groups;
	}

	public List getRoles()
	{
		return roles;
	}

	public List getUsers()
	{
		return users;
	}
}
        
