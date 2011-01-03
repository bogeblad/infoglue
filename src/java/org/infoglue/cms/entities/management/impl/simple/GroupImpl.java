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
import org.infoglue.cms.entities.management.Group;
import org.infoglue.cms.entities.management.GroupVO;
import org.infoglue.cms.exception.ConstraintException;

public class GroupImpl implements Group
{
	private GroupVO valueObject = new GroupVO();
	private java.util.Collection systemUsers = new ArrayList();
     
	public Integer getId()
	{
		return null;
	}

	public Object getIdAsObject()
	{
		return getGroupName();
	}

	public String toString()
	{
		return this.valueObject.toString();
	}

    public GroupVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(GroupVO valueObject)
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
		setValueObject((GroupVO) valueObject);
	}
      
    public java.lang.String getGroupName()
    {
        return this.valueObject.getGroupName(); 
    }
            
    public void setGroupName(java.lang.String groupName) throws ConstraintException
    {
        this.valueObject.setGroupName(groupName);
    }

	public String getDescription()
	{
        return this.valueObject.getDescription();
	}

	public void setDescription(String description) throws ConstraintException
	{
        this.valueObject.setDescription(description);
	}
      
    public java.util.Collection getSystemUsers()
    {
        return this.systemUsers;
    }
            
    public void setSystemUsers (java.util.Collection systemUsers)
    {
        this.systemUsers = systemUsers;
    }
      
}        
