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

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class AccessRightGroupVO implements BaseEntityVO, Cloneable
{
	private java.lang.Integer accessRightGroupId;
	private java.lang.String groupName;
  	
	public Integer getId() 
	{
		return getAccessRightGroupId();
	}

    public java.lang.Integer getAccessRightGroupId()
    {
        return this.accessRightGroupId;
    }
                
    public void setAccessRightGroupId(java.lang.Integer accessRightGroupId)
    {
        this.accessRightGroupId = accessRightGroupId;
    }
    
    public java.lang.String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(java.lang.String groupName)
    {
        this.groupName = groupName;
    }
    
	public ConstraintExceptionBuffer validate() 
	{
    	
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		//if (name != null) ValidatorFactory.createStringValidator("Access.name", true, 3, 50, true, RoleImpl.class, this.getId()).validate(name, ceb);

		return ceb;
	}

	public AccessRightGroupVO createCopy() throws Exception
	{
		return (AccessRightGroupVO)this.clone();
	}
}
        
