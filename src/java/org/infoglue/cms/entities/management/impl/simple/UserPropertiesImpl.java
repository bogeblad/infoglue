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

import java.util.Collection;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinition;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.UserProperties;
import org.infoglue.cms.entities.management.UserPropertiesVO;

public class UserPropertiesImpl implements UserProperties
{
	private UserPropertiesVO valueObject = new UserPropertiesVO();
	private ContentTypeDefinition contentTypeDefinition;
	private Language language;
	private Collection digitalAssets;
	 
	public Integer getId()
	{
		return this.getUserPropertiesId();
	}

	public Object getIdAsObject()
	{
		return getId();
	}

	public String toString()
	{
		return this.valueObject.toString();
	}

    public UserPropertiesVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(UserPropertiesVO valueObject)
    {
        this.valueObject = valueObject;
    }   

	public BaseEntityVO getVO() 
	{
		return (BaseEntityVO) getValueObject();
	}

	public void setVO(BaseEntityVO valueObject) 
	{
		setValueObject((UserPropertiesVO) valueObject);
	}
    
    public Integer getUserPropertiesId()
    {
        return this.valueObject.getUserPropertiesId();
    }
            
    public void setUserPropertiesId(java.lang.Integer userPropertiesId)
    {
        this.valueObject.setUserPropertiesId(userPropertiesId);
    }
      
	public String getValue()
	{
        return this.valueObject.getValue();
	}

	public void setValue(String value)
	{
        this.valueObject.setValue(value);
	}
      
    public String getUserName()
    {
        return this.valueObject.getUserName();
    }
            
    public void setUserName(String userName)
    {
		this.valueObject.setUserName(userName);
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

	public Language getLanguage()
	{
		return this.language;
	}
            
	public void setLanguage(Language language)
	{
		if(language != null) 
			this.valueObject.setLanguageId(language.getLanguageId());

		this.language = language;
	}
	 
    public Collection getDigitalAssets()
    {
        return digitalAssets;
    }
    
    public void setDigitalAssets(Collection digitalAssets)
    {
        this.digitalAssets = digitalAssets;
    }
}        
