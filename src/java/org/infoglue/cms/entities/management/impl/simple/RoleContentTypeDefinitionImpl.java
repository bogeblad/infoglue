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
import org.infoglue.cms.entities.management.ContentTypeDefinition;
import org.infoglue.cms.entities.management.RoleContentTypeDefinition;
import org.infoglue.cms.entities.management.RoleContentTypeDefinitionVO;


public class RoleContentTypeDefinitionImpl implements RoleContentTypeDefinition
{
	private RoleContentTypeDefinitionVO valueObject = new RoleContentTypeDefinitionVO();
	private ContentTypeDefinition contentTypeDefinition;
	 
	public Integer getId()
	{
		return this.getRoleContentTypeDefinitionId();
	}

	public Object getIdAsObject()
	{
		return getId();
	}

	public String toString()
	{
		return this.valueObject.toString();
	}

    public RoleContentTypeDefinitionVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(RoleContentTypeDefinitionVO valueObject)
    {
        this.valueObject = valueObject;
    }   

	public BaseEntityVO getVO() 
	{
		return (BaseEntityVO) getValueObject();
	}

	public void setVO(BaseEntityVO valueObject) 
	{
		setValueObject((RoleContentTypeDefinitionVO) valueObject);
	}
    
    public java.lang.Integer getRoleContentTypeDefinitionId()
    {
        return this.valueObject.getRoleContentTypeDefinitionId();
    }
            
    public void setRoleContentTypeDefinitionId(java.lang.Integer roleContentTypeDefinitionId)
    {
        this.valueObject.setRoleContentTypeDefinitionId(roleContentTypeDefinitionId);
    }
      
    public String getRoleName()
    {
        return this.valueObject.getRoleName();
    }
            
    public void setRoleName(String roleName)
    {
		this.valueObject.setRoleName(roleName);
    }

	public ContentTypeDefinition getContentTypeDefinition()
	{
		return this.contentTypeDefinition;
	}
            
	public void setContentTypeDefinition(ContentTypeDefinition contentTypeDefinition)
	{
		if(contentTypeDefinition != null) 
			this.valueObject.setContentTypeDefinitionId(contentTypeDefinition.getContentTypeDefinitionId());
		
		this.contentTypeDefinition = contentTypeDefinition;
	}

}        
