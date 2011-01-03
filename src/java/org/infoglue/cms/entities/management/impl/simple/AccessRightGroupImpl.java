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
import org.infoglue.cms.entities.management.AccessRightGroup;
import org.infoglue.cms.entities.management.AccessRightGroupVO;

public class AccessRightGroupImpl implements AccessRightGroup
{
	private AccessRightGroupVO valueObject = new AccessRightGroupVO();
	private AccessRight accessRight = null;
	
    public java.lang.Integer getAccessRightGroupId()
    {
        return this.valueObject.getAccessRightGroupId();
    }
      
	public Object getIdAsObject()
	{
		return getId();
	}
        
    public void setAccessRightGroupId(java.lang.Integer accessRightGroupId)
    {
        this.valueObject.setAccessRightGroupId(accessRightGroupId);
    }
      
	public java.lang.String getGroupName()
	{
		return this.valueObject.getGroupName(); 
	}
            
    public void setGroupName(java.lang.String groupName)
	{
		this.valueObject.setGroupName(groupName);
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
		return this.getAccessRightGroupId();
	}

	public AccessRightGroupVO getValueObject()
	{
		return this.valueObject;
	}
        
	public void setValueObject(AccessRightGroupVO valueObject)
	{
		this.valueObject = valueObject;
	}   

	public BaseEntityVO getVO() 
	{
		return (BaseEntityVO) getValueObject();
	}

	public void setVO(BaseEntityVO valueObject) 
	{
		setValueObject((AccessRightGroupVO) valueObject);
	}
	
}        
