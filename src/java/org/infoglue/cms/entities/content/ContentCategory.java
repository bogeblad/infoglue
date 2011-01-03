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
 * $Id: ContentCategory.java,v 1.3 2006/12/03 19:32:07 mattias Exp $
 */
package org.infoglue.cms.entities.content;

import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.kernel.IBaseEntity;
import org.infoglue.cms.entities.management.impl.simple.CategoryImpl;

/**
 * Interface for a Content and Category relationship
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public interface ContentCategory extends IBaseEntity
{
	public ContentCategoryVO getValueObject();
	public void setValueObject(ContentCategoryVO valueObject);

    public Integer getContentCategoryId();
    public void setContentCategoryId(Integer i);

	public String getAttributeName();
	public void setAttributeName(String s);

    public Integer getContentVersionId();
    public void setContentVersionId(Integer i);

    public Integer getCategoryId();
    public void setCategoryId(Integer categoryId);
    
	public CategoryImpl getCategory();
	public void setCategory(CategoryImpl c);
	
	public ContentVersionImpl getContentVersion();
    public void setContentVersion(ContentVersionImpl contentVersion);
}

