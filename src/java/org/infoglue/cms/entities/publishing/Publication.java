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

package org.infoglue.cms.entities.publishing;

import org.infoglue.cms.entities.kernel.IBaseEntity;

public interface Publication extends IBaseEntity
{
        
    public PublicationVO getValueObject();
    
    public void setValueObject(PublicationVO valueObject);

    
    public java.lang.Integer getPublicationId();
    
    public void setPublicationId(java.lang.Integer publicationId);
    
    public java.lang.String getName();
    
    public void setName(java.lang.String name);
    
    public java.lang.String getDescription();
    
    public void setDescription(java.lang.String description);
    
    public java.util.Date getPublicationDateTime();
    
    public void setPublicationDateTime(java.util.Date publicationDateTime);
    
    public java.util.Collection getPublicationDetails();
    
    public void setPublicationDetails(java.util.Collection publicationDetails);

    public java.util.Collection getContentVersionsToPublish();
    
    public void setContentVersionsToPublish(java.util.Collection contentVersionsToPublish);
    
    public java.util.Collection getSiteNodeVersionsToPublish();
    
    public void setSiteNodeVersionsToPublish(java.util.Collection siteNodeVersionsToPublish);
    
    public String getPublisher();
    
    public void setPublisher(String publisher);
    
    public java.lang.Integer getRepositoryId();
    
    public void setRepositoryId(java.lang.Integer repositoryId);
    
        
}
