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

package org.infoglue.cms.entities.management.impl.simple;

import java.util.ArrayList;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.SystemUser;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.exception.ConstraintException;

public class SystemUserImpl implements SystemUser
{
	private java.util.Collection roles = new ArrayList();
	private java.util.Collection groups = new ArrayList();
    private SystemUserVO valueObject = new SystemUserVO();
     
    public SystemUserVO getValueObject()
    {
        return this.valueObject;
    }

	public Object getIdAsObject()
	{
		return getUserName();
	}

	public Integer getId()
	{
		return null;
	}
	
	public String toString()
	{
		return this.valueObject.toString();
	}
        
    public void setValueObject(SystemUserVO valueObject)
    {
        this.valueObject = valueObject;
    }   
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getVO()
	 */
	public BaseEntityVO getVO() 
	{
		return (BaseEntityVO) getValueObject();
	}
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#setVO(BaseEntityVO)
	 */
	public void setVO(BaseEntityVO valueObject) 
	{
		setValueObject((SystemUserVO) valueObject);
	}

    public java.lang.String getUserName()
    {
        return this.valueObject.getUserName();
    }
            
    public void setUserName(java.lang.String userName) throws ConstraintException
    {
        this.valueObject.setUserName(userName);
    }
      
    public java.lang.String getPassword()
    {
        return this.valueObject.getPassword();
    }
            
    public void setPassword(java.lang.String password) throws ConstraintException
    {
        this.valueObject.setPassword(password);
    }
      
    public java.lang.String getFirstName()
    {
        return this.valueObject.getFirstName();
    }
            
    public void setFirstName(java.lang.String firstName) throws ConstraintException
    {
        this.valueObject.setFirstName(firstName);
    }
      
    public java.lang.String getLastName()
    {
        return this.valueObject.getLastName();
    }
            
    public void setLastName(java.lang.String lastName) throws ConstraintException
    {
        this.valueObject.setLastName(lastName);
    }
      
    public java.lang.String getEmail()
    {
        return this.valueObject.getEmail();
    }
            
    public void setEmail(java.lang.String email) throws ConstraintException
    {
        this.valueObject.setEmail(email);
    }
      
    public java.util.Collection getRoles()
    {
        return this.roles;
    }
            
    public void setRoles (java.util.Collection roles)
    {
        this.roles = roles;
    }
    
    public java.util.Collection getGroups()
    {
        return groups;
    }
    
    public void setGroups(java.util.Collection groups)
    {
        this.groups = groups;
    }
}        
