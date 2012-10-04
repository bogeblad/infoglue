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
package org.infoglue.cms.entities.content.impl.simple;

import org.infoglue.cms.entities.content.ContentCategoryVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.impl.simple.CategoryImpl;

/**
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class SmallContentCategoryImpl extends ContentCategoryImpl
{
    private ContentCategoryVO valueObject = new ContentCategoryVO();

	public SmallContentCategoryImpl()
	{
	}

	public BaseEntityVO getVO()			{ return (BaseEntityVO) getValueObject(); }
	public void setVO(BaseEntityVO vo)	{ setValueObject((ContentCategoryVO) vo); }

	public Integer getId()			{ return getContentCategoryId(); }
	public Object getIdAsObject()	{ return getId(); }

    public ContentCategoryVO getValueObject()		{ return valueObject; }
    public void setValueObject(ContentCategoryVO c)	{ valueObject = c; }

    public Integer getContentCategoryId()		{ return valueObject.getContentCategoryId(); }
    public void setContentCategoryId(Integer i)	{ valueObject.setContentCategoryId(i); }

	public String getAttributeName()		{ return valueObject.getAttributeName(); }
	public void setAttributeName(String s)	{ valueObject.setAttributeName(s); }

    public Integer getContentVersionId()		{ return valueObject.getContentVersionId(); }
    public void setContentVersionId(Integer i)	{ valueObject.setContentVersionId(i); }

    public Integer getCategoryId()
	{
		return valueObject.getCategoryId();
	}

    public void setCategoryId(Integer categoryId)
	{
    	this.valueObject.setCategoryId(categoryId);
	}

    public String toString()
	{
		return valueObject.toStringBuffer().toString();
	}
}
