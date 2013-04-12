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
 */
package org.infoglue.cms.entities.management.impl.simple;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.PropertiesCategory;
import org.infoglue.cms.entities.management.PropertiesCategoryVO;

/**
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class SmallPropertiesCategoryImpl extends PropertiesCategoryImpl
{
    private PropertiesCategoryVO valueObject = new PropertiesCategoryVO();

	public SmallPropertiesCategoryImpl()
	{}

	public SmallPropertiesCategoryImpl(Integer id)
	{
		setPropertiesCategoryId(id);
	}

	public SmallPropertiesCategoryImpl(PropertiesCategoryVO vo)
	{
		valueObject = (vo != null)? vo : new PropertiesCategoryVO();
	}


	public BaseEntityVO getVO()			{ return (BaseEntityVO) getValueObject(); }
	public void setVO(BaseEntityVO vo)	{ setValueObject((PropertiesCategoryVO) vo); }

	public Integer getId()			{ return getPropertiesCategoryId(); }
	public Object getIdAsObject()	{ return getId(); }

    public PropertiesCategoryVO getValueObject()		{ return valueObject; }
    public void setValueObject(PropertiesCategoryVO c)	{ valueObject = c; }

    public Integer getPropertiesCategoryId()		{ return valueObject.getPropertiesCategoryId(); }
    public void setPropertiesCategoryId(Integer i)	{ valueObject.setPropertiesCategoryId(i); }

	public String getAttributeName()		{ return valueObject.getAttributeName(); }
	public void setAttributeName(String s)	{ valueObject.setAttributeName(s); }

	public String getEntityName()		{ return valueObject.getEntityName(); }
	public void setEntityName(String s)	{ valueObject.setEntityName(s); }

	public Integer getEntityId()		{ return valueObject.getEntityId(); }
	public void setEntityId(Integer entityId)		{ valueObject.setEntityId(entityId); }

	public Integer getCategoryId()		{ return valueObject.getCategoryId(); }
	public void setCategoryId(Integer categoryId)	{ valueObject.setCategoryId(categoryId); }

}
