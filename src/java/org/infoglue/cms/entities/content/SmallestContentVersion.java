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

public interface SmallestContentVersion extends IBaseEntity
{
        
    public SmallestContentVersionVO getValueObject();
    
    public void setValueObject(SmallestContentVersionVO valueObject);

    
    public java.lang.Integer getContentVersionId();
    
    public void setContentVersionId(java.lang.Integer contentVersionId);
    
    public java.lang.Integer getStateId();
    
    public void setStateId(java.lang.Integer stateId);
        
    public java.util.Date getModifiedDateTime();
    
    public void setModifiedDateTime(java.util.Date modifiedDateTime);
    
    public java.lang.String getVersionComment();
    
    public void setVersionComment(java.lang.String versionComment);
    
    public java.lang.Boolean getIsCheckedOut();
    
    public void setIsCheckedOut(java.lang.Boolean isCheckedOut);
    
   	public java.lang.Boolean getIsActive();
    
    public void setIsActive(java.lang.Boolean isActive);

    public String getVersionModifier();
    
    public void setVersionModifier(String versionModifier);
}
