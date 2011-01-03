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

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.AccessRight;
import org.infoglue.cms.entities.management.AccessRightRole;
import org.infoglue.cms.entities.management.AccessRightRoleVO;

public class AccessRightRoleImpl implements AccessRightRole
{
	private AccessRightRoleVO valueObject = new AccessRightRoleVO();
	private AccessRight accessRight = null;
	
    public java.lang.Integer getAccessRightRoleId()
    {
        return this.valueObject.getAccessRightRoleId();
    }
      
	public Object getIdAsObject()
	{
		return getId();
	}
        
    public void setAccessRightRoleId(java.lang.Integer accessRightRoleId)
    {
        this.valueObject.setAccessRightRoleId(accessRightRoleId);
    }
      
	public java.lang.String getRoleName()
	{
		return this.valueObject.getRoleName(); 
	}
            
    public void setRoleName(java.lang.String roleName)
	{
		this.valueObject.setRoleName(roleName);
	}

    public AccessRight getAccessRight()
	{
		return this.accessRight;
	}
	
	public void setAccessRight(AccessRight accessRight)
	{
	    this.accessRight = accessRight;
	}
	
	public Integer getId()
	{
		return this.getAccessRightRoleId();
	}

	public AccessRightRoleVO getValueObject()
	{
		return this.valueObject;
	}
        
	public void setValueObject(AccessRightRoleVO valueObject)
	{
		this.valueObject = valueObject;
	}   

	public BaseEntityVO getVO() 
	{
		return (BaseEntityVO) getValueObject();
	}

	public void setVO(BaseEntityVO valueObject) 
	{
		setValueObject((AccessRightRoleVO) valueObject);
	}
	
}        
