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
import org.infoglue.cms.entities.management.impl.simple.RoleImpl;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.validators.ValidatorFactory;

public class RoleVO  implements BaseEntityVO
{
	private java.lang.String roleName;
	private java.lang.String description;
  
	public String toString()
	{  
		return getRoleName();
	}
	
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	
	public Integer getId() 
	{
		return null;
	}
    
    public java.lang.String getRoleName()
    {
        return this.roleName;
    }
                
    public void setRoleName(java.lang.String roleName)
    {
        this.roleName = roleName;
    }
        
    public java.lang.String getDescription()
    {
        return this.description;
    }
                
    public void setDescription(java.lang.String description)
    {
        this.description = description;
    }
    
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	
	public ConstraintExceptionBuffer validate() 
	{
    	ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
    	if (roleName != null) ValidatorFactory.createStringValidator("Role.roleName", true, 3, 100, true, RoleImpl.class, this.getId(), this.getRoleName()).validate(roleName, ceb);
		if (description != null) ValidatorFactory.createStringValidator("Role.description", true, 1, 100).validate(description, ceb); 

		return ceb;
	}

}
        
