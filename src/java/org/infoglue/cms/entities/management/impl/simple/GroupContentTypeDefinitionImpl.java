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
import org.infoglue.cms.entities.management.GroupContentTypeDefinition;
import org.infoglue.cms.entities.management.GroupContentTypeDefinitionVO;


public class GroupContentTypeDefinitionImpl implements GroupContentTypeDefinition
{
	private GroupContentTypeDefinitionVO valueObject = new GroupContentTypeDefinitionVO();
	private ContentTypeDefinition contentTypeDefinition;
	 
	public Integer getId()
	{
		return this.getGroupContentTypeDefinitionId();
	}

	public Object getIdAsObject()
	{
		return getId();
	}

	public String toString()
	{
		return this.valueObject.toString();
	}

    public GroupContentTypeDefinitionVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(GroupContentTypeDefinitionVO valueObject)
    {
        this.valueObject = valueObject;
    }   

	public BaseEntityVO getVO() 
	{
		return (BaseEntityVO) getValueObject();
	}

	public void setVO(BaseEntityVO valueObject) 
	{
		setValueObject((GroupContentTypeDefinitionVO) valueObject);
	}
    
    public java.lang.Integer getGroupContentTypeDefinitionId()
    {
        return this.valueObject.getGroupContentTypeDefinitionId();
    }
            
    public void setGroupContentTypeDefinitionId(java.lang.Integer groupContentTypeDefinitionId)
    {
        this.valueObject.setGroupContentTypeDefinitionId(groupContentTypeDefinitionId);
    }
      
    public String getGroupName()
    {
        return this.valueObject.getGroupName();
    }
            
    public void setGroupName(String groupName)
    {
		this.valueObject.setGroupName(groupName);
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
