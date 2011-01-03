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

public class AccessRightRoleVO implements BaseEntityVO, Cloneable
{
	private java.lang.Integer accessRightRoleId;
	private java.lang.String roleName;
  	
	public Integer getId() 
	{
		return getAccessRightRoleId();
	}

    public java.lang.Integer getAccessRightRoleId()
    {
        return this.accessRightRoleId;
    }
                
    public void setAccessRightRoleId(java.lang.Integer accessRightRoleId)
    {
        this.accessRightRoleId = accessRightRoleId;
    }
    
    public java.lang.String getRoleName()
    {
        return roleName;
    }
    
    public void setRoleName(java.lang.String roleName)
    {
        this.roleName = roleName;
    }

    public ConstraintExceptionBuffer validate() 
	{
    	
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		//if (name != null) ValidatorFactory.createStringValidator("Access.name", true, 3, 50, true, RoleImpl.class, this.getId()).validate(name, ceb);

		return ceb;
	}

	public AccessRightRoleVO createCopy() throws Exception
	{
		return (AccessRightRoleVO)this.clone();
	}
}
        
