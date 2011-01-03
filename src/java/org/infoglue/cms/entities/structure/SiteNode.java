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

package org.infoglue.cms.entities.structure;

import org.infoglue.cms.entities.kernel.IBaseEntity;

public interface SiteNode extends IBaseEntity
{
        
    public SiteNodeVO getValueObject();
    
    public void setValueObject(SiteNodeVO valueObject);

    
    public java.lang.Integer getSiteNodeId();
    
    public void setSiteNodeId(java.lang.Integer siteNodeId);
    
    public java.lang.String getName();
    
    public void setName(java.lang.String name);
    
    public java.util.Date getPublishDateTime();
    
    public java.lang.Boolean getIsBranch();
    
    public void setIsBranch(java.lang.Boolean isBranch);

    public Boolean getIsDeleted();
    
    public void setIsDeleted(Boolean isDeleted);

    public void setPublishDateTime(java.util.Date publishDateTime);
    
    public java.util.Date getExpireDateTime();
    
    public void setExpireDateTime(java.util.Date expireDateTime);
    
    public Integer getMetaInfoContentId();
    
    public void setMetaInfoContentId(Integer metaInfoContentId);

    public org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl getParentSiteNode();
    
    public void setParentSiteNode(org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl parentSiteNode);
    
    public java.util.Collection getChildSiteNodes();
    
    public void setChildSiteNodes(java.util.Collection childSiteNodes);
    
    public java.util.Collection getSiteNodeVersions();
    
    public void setSiteNodeVersions(java.util.Collection siteNodeVersions);
    
    public String getCreator();
    
    public void setCreator(String creator);
    
    public org.infoglue.cms.entities.management.impl.simple.RepositoryImpl getRepository();
    
    public void setRepository(org.infoglue.cms.entities.management.impl.simple.RepositoryImpl repository);
    
    public org.infoglue.cms.entities.management.impl.simple.SiteNodeTypeDefinitionImpl getSiteNodeTypeDefinition();
    
    public void setSiteNodeTypeDefinition(org.infoglue.cms.entities.management.impl.simple.SiteNodeTypeDefinitionImpl siteNodeTypeDefinition);
        
}
