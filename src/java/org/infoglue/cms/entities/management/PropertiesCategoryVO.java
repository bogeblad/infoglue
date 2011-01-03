/* ===============================================================================
 *
 * Part of the InfoGlue Properties Management Platform (www.infoglue.org)
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
 * $Id: PropertiesCategoryVO.java,v 1.2 2006/03/06 17:19:50 mattias Exp $
 */
package org.infoglue.cms.entities.management;

import org.infoglue.cms.entities.kernel.Persistent;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.DomainUtils;

/**
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class PropertiesCategoryVO extends Persistent
{
	private Integer propertiesCategoryId;
	private String attributeName;
	private String entityName;
	private Integer entityId;
	private CategoryVO category = new CategoryVO();

	public PropertiesCategoryVO() {}

	public PropertiesCategoryVO(String attributeName, String entityName, Integer entityId, CategoryVO category)
	{
		setAttributeName(attributeName);
		setEntityName(entityName);
		setEntityId(entityId);
		setCategory(category);
	}

	public PropertiesCategoryVO(Integer id, String attributeName, String entityName, Integer entityId, CategoryVO category)
	{
		this(attributeName, entityName, entityId, category);
		setPropertiesCategoryId(id);
	}

	public Integer getId()
	{
		return getPropertiesCategoryId();
	}

	public Integer getPropertiesCategoryId()
	{
		return propertiesCategoryId;
	}

	public void setPropertiesCategoryId(Integer i)
	{
		propertiesCategoryId = i;
	}

	public String getAttributeName()
	{
		return attributeName;
	}

	public void setAttributeName(String s)
	{
		attributeName = s;
	}

    public Integer getEntityId()
    {
        return entityId;
    }
    
    public void setEntityId(Integer entityId)
    {
        this.entityId = entityId;
    }
    
    public String getEntityName()
    {
        return entityName;
    }
    
    public void setEntityName(String entityName)
    {
        this.entityName = entityName;
    }

	public CategoryVO getCategory()
	{
		return category;
	}

	public void setCategory(CategoryVO c)
	{
		category = (c != null) ? c : new CategoryVO();
	}

	/**
	 * Not used
	 */
	public ConstraintExceptionBuffer validate()
	{
		return new ConstraintExceptionBuffer();
	}

	public boolean equals(Object o)
	{
		if (super.equals(o))
		{
			PropertiesCategoryVO vo = (PropertiesCategoryVO)o;
			return DomainUtils.equals(propertiesCategoryId, vo.propertiesCategoryId)
					&& DomainUtils.equals(attributeName, vo.attributeName)
					&& DomainUtils.equals(entityName, vo.entityName)
					&& DomainUtils.equals(entityId, vo.entityId)
					&& DomainUtils.equals(category.getId(), vo.category.getId());
		}
		return false;
	}

	public StringBuffer toStringBuffer()
	{
		StringBuffer sb = super.toStringBuffer();
		sb.append(" attributeName=").append(attributeName)
				.append(" entityName=").append(entityName)
				.append(" entityId=").append(entityId)
				.append(" categoryId=").append(category.getId());
		return sb;
	}

}

