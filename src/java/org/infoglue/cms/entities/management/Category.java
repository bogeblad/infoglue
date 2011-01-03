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
 * $Id: Category.java,v 1.4 2009/05/11 19:08:20 mattias Exp $
 */

package org.infoglue.cms.entities.management;

import org.infoglue.cms.entities.kernel.IBaseEntity;

/**
 * This is a Category to be used for categorizing Content
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public interface Category extends IBaseEntity
{
	public Integer getCategoryId();
	public void setCategoryId(Integer s);

	public String getName();
	public void setName(String s);

	public String getDisplayName();
	public void setDisplayName(String s);

	public String getLocalizedDisplayName(String languageCode, String fallbackLanguageCode);
	
	public String getDescription();
	public void setDescription(String s);

	public boolean isActive();
	public void setActive(boolean b);

	public Integer getParentId();
	public void setParentId(Integer i);

	public CategoryVO getValueObject();
	public void setValueObject(CategoryVO c);
}
