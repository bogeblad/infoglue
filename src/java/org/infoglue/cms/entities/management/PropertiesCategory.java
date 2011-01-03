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
 * $Id: PropertiesCategory.java,v 1.1 2005/04/20 16:06:33 mattias Exp $
 */
package org.infoglue.cms.entities.management;

import org.infoglue.cms.entities.kernel.IBaseEntity;
import org.infoglue.cms.entities.management.impl.simple.CategoryImpl;

/**
 * Interface for a Properties and Category relationship
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public interface PropertiesCategory extends IBaseEntity
{
	public PropertiesCategoryVO getValueObject();
	public void setValueObject(PropertiesCategoryVO valueObject);

    public Integer getPropertiesCategoryId();
    public void setPropertiesCategoryId(Integer i);

	public String getAttributeName();
	public void setAttributeName(String s);

	public String getEntityName();
	public void setEntityName(String s);

	public Integer getEntityId();
    public void setEntityId(Integer i);

	public CategoryImpl getCategory();
	public void setCategory(CategoryImpl c);
}

