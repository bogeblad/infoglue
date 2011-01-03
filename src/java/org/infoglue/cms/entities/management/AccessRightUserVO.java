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

public class AccessRightUserVO implements BaseEntityVO, Cloneable
{
	private java.lang.Integer accessRightUserId;
	private java.lang.String UserName;
  	
	public Integer getId() 
	{
		return getAccessRightUserId();
	}

    public java.lang.Integer getAccessRightUserId()
    {
        return this.accessRightUserId;
    }
                
    public void setAccessRightUserId(java.lang.Integer accessRightUserId)
    {
        this.accessRightUserId = accessRightUserId;
    }
    
    public java.lang.String getUserName()
    {
        return UserName;
    }

    public void setUserName(java.lang.String UserName)
    {
        this.UserName = UserName;
    }
    
	public ConstraintExceptionBuffer validate() 
	{
    	
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		//if (name != null) ValidatorFactory.createStringValidator("Access.name", true, 3, 50, true, RoleImpl.class, this.getId()).validate(name, ceb);

		return ceb;
	}

	public AccessRightUserVO createCopy() throws Exception
	{
		return (AccessRightUserVO)this.clone();
	}
}
        
