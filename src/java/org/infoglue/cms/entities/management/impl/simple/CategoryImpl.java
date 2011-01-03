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
 *
 * $Id: CategoryImpl.java,v 1.3 2009/05/11 19:08:20 mattias Exp $
 */

package org.infoglue.cms.entities.management.impl.simple;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Category;
import org.infoglue.cms.entities.management.CategoryVO;

public class CategoryImpl implements Category
{
    private CategoryVO valueObject = new CategoryVO();

	public CategoryImpl()
	{}

	public CategoryImpl(Integer id)
	{
		setCategoryId(id);
	}

	public CategoryImpl(CategoryVO vo)
	{
		valueObject = (vo != null)? vo : new CategoryVO();
	}

	public Integer getId()			{ return valueObject.getId(); }
	public Object getIdAsObject()	{ return getId(); }

	public Integer getCategoryId()		{ return valueObject.getCategoryId(); }
	public void setCategoryId(Integer i){ valueObject.setCategoryId(i); }

	public String getName()				{ return valueObject.getName(); }
	public void setName(String s)		{ valueObject.setName(s); }

	public String getDisplayName()		{ return valueObject.getDisplayName(); }
	public void setDisplayName(String s){ valueObject.setDisplayName(s); }

	public String getLocalizedDisplayName(String languageCode, String fallbackLanguageCode) { return valueObject.getLocalizedDisplayName(languageCode, fallbackLanguageCode); }

	public String getDescription()		{ return valueObject.getDescription(); }
	public void setDescription(String s){ valueObject.setDescription(s); }

	public boolean isActive()			{ return valueObject.isActive(); }
	public void setActive(boolean b)	{ valueObject.setActive(b); }

	public Integer getParentId()		{ return valueObject.getParentId(); }
	public void setParentId(Integer i)	{ valueObject.setParentId(i); }

	public CategoryVO getValueObject()			{ return valueObject; }
	public void setValueObject(CategoryVO vo)	{ valueObject = vo; }

	public BaseEntityVO getVO()			{ return (BaseEntityVO) getValueObject(); }
	public void setVO(BaseEntityVO vo)	{ setValueObject((CategoryVO) vo); }

	public String toString()
	{
		return valueObject.toString();
	}
}
