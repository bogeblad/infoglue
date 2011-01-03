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

public class GroupPropertiesVO implements BaseEntityVO
{
	private Integer groupPropertiesId;
	private Integer contentTypeDefinitionId;
	private String groupName;
	private Integer languageId;
	private String value;
  
	public Integer getId() 
	{
		return getGroupPropertiesId();
	}

    public Integer getGroupPropertiesId()
    {
        return this.groupPropertiesId;
    }
                
    public void setGroupPropertiesId(Integer groupPropertiesId)
    {
        this.groupPropertiesId = groupPropertiesId;
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

	public String getGroupName()
	{
		return groupName;
	}

	public void setGroupName(String groupName)
	{
		this.groupName = groupName;
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
        
