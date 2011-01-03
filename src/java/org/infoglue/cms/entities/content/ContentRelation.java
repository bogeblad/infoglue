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

public interface ContentRelation
{
        
    public ContentRelationVO getValueObject();
    
    public void setValueObject(ContentRelationVO valueObject);

    
    public java.lang.Integer getContentRelationId();
    
    public void setContentRelationId(java.lang.Integer contentRelationId);
    
    public java.lang.String getRelationInternalName();
    
    public void setRelationInternalName(java.lang.String relationInternalName);
    
    public java.lang.Integer getRelationTypeId();
    
    public void setRelationTypeId(java.lang.Integer relationTypeId);
    
    public org.infoglue.cms.entities.content.impl.simple.ContentImpl getSourceContent();
    
    public void setSourceContent(org.infoglue.cms.entities.content.impl.simple.ContentImpl sourceContent);
    
    public org.infoglue.cms.entities.content.impl.simple.ContentImpl getDestinationContent();
    
    public void setDestinationContent(org.infoglue.cms.entities.content.impl.simple.ContentImpl destinationContent);
        
}
