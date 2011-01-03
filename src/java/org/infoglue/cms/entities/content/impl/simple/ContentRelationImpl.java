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

import org.infoglue.cms.entities.content.ContentRelation;
import org.infoglue.cms.entities.content.ContentRelationVO;

public class ContentRelationImpl implements ContentRelation
{
    private ContentRelationVO valueObject = new ContentRelationVO();
     
    public ContentRelationVO getValueObject()
    {
        return this.valueObject;
    }

        
    public void setValueObject(ContentRelationVO valueObject)
    {
        this.valueObject = valueObject;
    }   

            private org.infoglue.cms.entities.content.impl.simple.ContentImpl sourceContent;
        private org.infoglue.cms.entities.content.impl.simple.ContentImpl destinationContent;
  
    
    public java.lang.Integer getContentRelationId()
    {
        return this.valueObject.getContentRelationId();
    }
            
    public void setContentRelationId(java.lang.Integer contentRelationId)
    {
        this.valueObject.setContentRelationId(contentRelationId);
    }
      
    public java.lang.String getRelationInternalName()
    {
        return this.valueObject.getRelationInternalName();
    }
            
    public void setRelationInternalName(java.lang.String relationInternalName)
    {
        this.valueObject.setRelationInternalName(relationInternalName);
    }
      
    public java.lang.Integer getRelationTypeId()
    {
        return this.valueObject.getRelationTypeId();
    }
            
    public void setRelationTypeId(java.lang.Integer relationTypeId)
    {
        this.valueObject.setRelationTypeId(relationTypeId);
    }
      
    public org.infoglue.cms.entities.content.impl.simple.ContentImpl getSourceContent()
    {
        return this.sourceContent;
    }
            
    public void setSourceContent (org.infoglue.cms.entities.content.impl.simple.ContentImpl sourceContent)
    {
        this.sourceContent = sourceContent;
    }
      
    public org.infoglue.cms.entities.content.impl.simple.ContentImpl getDestinationContent()
    {
        return this.destinationContent;
    }
            
    public void setDestinationContent (org.infoglue.cms.entities.content.impl.simple.ContentImpl destinationContent)
    {
        this.destinationContent = destinationContent;
    }
  }        
