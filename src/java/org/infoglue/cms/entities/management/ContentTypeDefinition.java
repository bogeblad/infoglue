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

import java.util.Collection;
import java.util.List;

import org.infoglue.cms.entities.kernel.IBaseEntity;
import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
import org.infoglue.cms.exception.ConstraintException;

public interface ContentTypeDefinition extends IBaseEntity
{
        
    public ContentTypeDefinitionVO getValueObject();
    
    public void setValueObject(ContentTypeDefinitionVO valueObject);

    public Integer getContentTypeDefinitionId();
    
    public void setContentTypeDefinitionId(Integer contentTypeDefinitionId);
    
    public String getName();
    
    public void setName(String name) throws ConstraintException;

    public String getSchemaValue();
    
    public void setSchemaValue(String schemaValue);

	public Integer getType();
    
	public void setType(Integer type) throws ConstraintException;

	public String getDetailPageResolverClass();
    
    public void setDetailPageResolverClass(String detailPageResolverClass) throws ConstraintException;
    
    public String getDetailPageResolverData();
    
    public void setDetailPageResolverData(String detailPageResolverData) throws ConstraintException;
    
    public ContentTypeDefinition getParent();
    
    public void setParent(ContentTypeDefinition parent);

    public Collection<ContentTypeDefinition> getChildren();
    
    public void setChildren(Collection<ContentTypeDefinition> children);

	//public Collection getContents();
	
	//public void setContents(Collection contents);
	        
}
