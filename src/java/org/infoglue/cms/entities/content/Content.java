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

package org.infoglue.cms.entities.content;

import org.infoglue.cms.entities.kernel.IBaseEntity;

public interface Content extends IBaseEntity
{
        
    public ContentVO getValueObject();
    
    public void setValueObject(ContentVO valueObject);

    
    public java.lang.Integer getContentId();
    
    public void setContentId(java.lang.Integer contentId);
    
    public java.lang.String getName();
    
    public void setName(java.lang.String name);
    
    public java.util.Date getPublishDateTime();
    
    public void setPublishDateTime(java.util.Date publishDateTime);
    
    public java.util.Date getExpireDateTime();
    
    public void setExpireDateTime(java.util.Date expireDateTime);

    public java.lang.Boolean getIsBranch();
    
    public void setIsProtected(Integer isProtected);
    
	public Integer getIsProtected();
    
    public Boolean getIsDeleted();
    
    public void setIsDeleted(Boolean isDeleted);

	public void setIsBranch(java.lang.Boolean isBranch);
    
    public org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl getContentTypeDefinition();
    
    public void setContentTypeDefinition(org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl contentTypeDefinition);
    
    public Integer getContentTypeDefinitionId();

    public void setContentTypeDefinitionId(Integer contentTypeDefinitionId);

    public java.util.Collection getChildren();
    
    public void setChildren(java.util.Collection children);
    
    public org.infoglue.cms.entities.content.impl.simple.ContentImpl getParentContent();
    
    public void setParentContent(org.infoglue.cms.entities.content.impl.simple.ContentImpl parentContent);
    
    public java.util.Collection getContentVersions();
    
    public void setContentVersions(java.util.Collection contentVersions);
    
    public java.lang.String getCreator();
    
    public void setCreator(java.lang.String creator);
    
    public org.infoglue.cms.entities.management.impl.simple.RepositoryImpl getRepository();
    
    public void setRepository(org.infoglue.cms.entities.management.impl.simple.RepositoryImpl repository);

    public void setRepositoryId(Integer repositoryId);

    public Integer getRepositoryId();

    public java.util.Collection getRelatedContents();
    
    public void setRelatedContents(java.util.Collection relatedContents);
    
    public java.util.Collection getRelatedByContents();
    
    public void setRelatedByContents(java.util.Collection relatedByContents);
    
    // This is just to save queries, only for performance reasons
    public Integer getChildCount();
    public void setChildCount(Integer childCount);
        
}
