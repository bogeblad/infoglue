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

public class ContentTypeDefinitionVO implements BaseEntityVO
{ 

	public static final Integer CONTENT = new Integer(0);
	public static final Integer EXTRANET_ROLE_PROPERTIES	= new Integer(1);
	public static final Integer EXTRANET_USER_PROPERTIES  	= new Integer(2);     
	public static final Integer EXTRANET_GROUP_PROPERTIES 	= new Integer(3);     

	private Integer contentTypeDefinitionId;
	private String name;
    private String schemaValue;
    private Integer type = CONTENT;
    private Integer parentId;
    private String parentName;
    private String detailPageResolverClass;
    private String detailPageResolverData;
  
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	
	public Integer getId() 
	{
		return getContentTypeDefinitionId();
	}

	
    public java.lang.Integer getContentTypeDefinitionId()
    {
        return this.contentTypeDefinitionId;
    }
                
    public void setContentTypeDefinitionId(java.lang.Integer contentTypeDefinitionId)
    {
        this.contentTypeDefinitionId = contentTypeDefinitionId;
    }
    
    public java.lang.String getName()
    {
        return this.name;
    }
                
    public void setName(java.lang.String name)
    {
        this.name = name;
    }
    
    public java.lang.String getSchemaValue()
    {
        return this.schemaValue;
    }
                
    public void setSchemaValue(java.lang.String schemaValue)
    {
        this.schemaValue = schemaValue;
    }
    
	public java.lang.Integer getType()
	{
		return type;
	}

	public void setType(java.lang.Integer type)
	{
		this.type = type;
	}

	public Integer getParentId() 
	{
		return parentId;
	}

	public void setParentId(Integer parentId) 
	{
		this.parentId = parentId;
	}

	public String getParentName() 
	{
		return parentName;
	}

	public void setParentName(String parentName) 
	{
		this.parentName = parentName;
	}

	public String getDetailPageResolverClass() 
	{
		return detailPageResolverClass;
	}


	public void setDetailPageResolverClass(String detailPageResolverClass) 
	{
		this.detailPageResolverClass = detailPageResolverClass;
	}


	public String getDetailPageResolverData() 
	{
		return detailPageResolverData;
	}


	public void setDetailPageResolverData(String detailPageResolverData) 
	{
		this.detailPageResolverData = detailPageResolverData;
	}


	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	
	public ConstraintExceptionBuffer validate() 
	{ 
		return null;
	}
}
        
