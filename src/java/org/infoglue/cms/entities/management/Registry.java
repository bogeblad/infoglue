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

import org.infoglue.cms.entities.kernel.IBaseEntity;

public interface Registry extends IBaseEntity
{
  	public Integer getId();
       
    public RegistryVO getValueObject();
    
    public void setValueObject(RegistryVO valueObject);
    
    public Integer getRegistryId();
    
    public void setRegistryId(Integer registryId);

    public String getEntityId();
    
    public void setEntityId(String entityId);
    
    public String getEntityName();
    
    public void setEntityName(String entityName);
    
    public Integer getReferenceType();
    
    public void setReferenceType(Integer referenceType);
    
    public String getReferencingEntityId();
    
    public void setReferencingEntityId(String referencingEntityId);
    
    public String getReferencingEntityName();
    
    public void setReferencingEntityName(String referencingEntityName);       

    public String getReferencingEntityCompletingId();
    
    public void setReferencingEntityCompletingId(String referencingEntityCompletingId);
    
    public String getReferencingEntityCompletingName();
    
    public void setReferencingEntityCompletingName(String referencingEntityCompletingName);       

}
