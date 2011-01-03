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

public interface Qualifyer extends IBaseEntity
{
        
    public QualifyerVO getValueObject();
    
    public void setValueObject(QualifyerVO valueObject);

    
    public java.lang.Integer getQualifyerId();
    
    public void setQualifyerId(java.lang.Integer qualifyerId);
    
    public java.lang.String getName();
    
    public void setName(java.lang.String name);
    
    public java.lang.String getValue();
    
    public void setValue(java.lang.String value);
    
    public java.lang.Integer getSortOrder();
    
    public void setSortOrder(java.lang.Integer sortOrder);

    public org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl getServiceBinding();
    
    public void setServiceBinding(org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl serviceBinding);
        
}
