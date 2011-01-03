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

public class RolePropertiesVO implements BaseEntityVO
{
	private Integer rolePropertiesId;
	private Integer contentTypeDefinitionId;
	private String roleName;
	private Integer languageId;
	private String value;
  
	public Integer getId() 
	{
		return getRolePropertiesId();
	}

    public Integer getRolePropertiesId()
    {
        return this.rolePropertiesId;
    }
                
    public void setRolePropertiesId(Integer rolePropertiesId)
    {
        this.rolePropertiesId = rolePropertiesId;
    }
    
	public java.lang.Integer getContentTypeDefinitionId()
	{
		return contentTypeDefinitionId;
	}

	public void setContentTypeDefinitionId(java.lang.Integer contentTypeDefinitionId)
	{
		this.contentTypeDefinitionId = contentTypeDefinitionId;
	}

	public java.lang.Integer getLanguageId()
	{
		return languageId;
	}

	public void setLanguageId(java.lang.Integer languageId)
	{
		this.languageId = languageId;
	}

	public String getRoleName()
	{
		return roleName;
	}

	public void setRoleName(String roleName)
	{
		this.roleName = roleName;
	}

    public java.lang.String getValue()
    {
        return this.value;
    }
                
    public void setValue(java.lang.String value)
    {
        this.value = value;
    }

	public ConstraintExceptionBuffer validate() 
	{
    	ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
    	
		return ceb;
	}

}
        
