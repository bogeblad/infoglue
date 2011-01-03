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
 * $Id: ContentCategoryVO.java,v 1.3 2006/12/03 19:32:29 mattias Exp $
 */
package org.infoglue.cms.entities.content;

import org.infoglue.cms.entities.kernel.Persistent;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.DomainUtils;

/**
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class ContentCategoryVO extends Persistent
{
	private Integer contentCategoryId;
	private String attributeName;
	private Integer contentVersionId;
	private Integer categoryId;
	private CategoryVO category = new CategoryVO();

	public ContentCategoryVO() {}

	public ContentCategoryVO(String attributeName, Integer contentVersionId, CategoryVO category)
	{
		setAttributeName(attributeName);
		setContentVersionId(contentVersionId);
		setCategory(category);
		setCategoryId(category.getId());
	}

	public ContentCategoryVO(Integer id, String attributeName, Integer contentVersionId, CategoryVO category)
	{
		this(attributeName, contentVersionId, category);
		setContentCategoryId(id);
	}

	public Integer getId()
	{
		return getContentCategoryId();
	}

	public Integer getContentCategoryId()
	{
		return contentCategoryId;
	}

	public void setContentCategoryId(Integer i)
	{
		contentCategoryId = i;
	}

	public String getAttributeName()
	{
		return attributeName;
	}

	public void setAttributeName(String s)
	{
		attributeName = s;
	}

	public Integer getContentVersionId()
	{
		return contentVersionId;
	}

	public void setContentVersionId(Integer i)
	{
		contentVersionId = i;
	}

	public Integer getCategoryId() 
	{
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) 
	{
		this.categoryId = categoryId;
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
			ContentCategoryVO vo = (ContentCategoryVO)o;
			return DomainUtils.equals(contentCategoryId, vo.contentCategoryId)
					&& DomainUtils.equals(attributeName, vo.attributeName)
					&& DomainUtils.equals(contentVersionId, vo.contentVersionId)
					&& DomainUtils.equals(category.getId(), vo.category.getId());
		}
		return false;
	}

	public StringBuffer toStringBuffer()
	{
		StringBuffer sb = super.toStringBuffer();
		sb.append(" attributeName=").append(attributeName)
				.append(" contentVersionId=").append(contentVersionId)
				.append(" categoryId=").append(category.getId());
		return sb;
	}
}

