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
import org.infoglue.cms.entities.management.UserContentTypeDefinition;
import org.infoglue.cms.entities.management.UserContentTypeDefinitionVO;

public class UserContentTypeDefinitionImpl implements UserContentTypeDefinition
{
	private UserContentTypeDefinitionVO valueObject = new UserContentTypeDefinitionVO();
	private ContentTypeDefinition contentTypeDefinition;
	 
	public Integer getId()
	{
		return this.getUserContentTypeDefinitionId();
	}

	public Object getIdAsObject()
	{
		return getId();
	}

	public String toString()
	{
		return this.valueObject.toString();
	}

    public UserContentTypeDefinitionVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(UserContentTypeDefinitionVO valueObject)
    {
        this.valueObject = valueObject;
    }   

	public BaseEntityVO getVO() 
	{
		return (BaseEntityVO) getValueObject();
	}

	public void setVO(BaseEntityVO valueObject) 
	{
		setValueObject((UserContentTypeDefinitionVO) valueObject);
	}
    
    public java.lang.Integer getUserContentTypeDefinitionId()
    {
        return this.valueObject.getUserContentTypeDefinitionId();
    }
            
    public void setUserContentTypeDefinitionId(java.lang.Integer roleContentTypeDefinitionId)
    {
        this.valueObject.setUserContentTypeDefinitionId(roleContentTypeDefinitionId);
    }
      
    public String getUserName()
    {
        return this.valueObject.getUserName();
    }
            
    public void setUserName(String roleName)
    {
		this.valueObject.setUserName(roleName);
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
