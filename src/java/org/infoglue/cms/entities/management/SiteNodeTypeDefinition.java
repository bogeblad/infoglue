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
import org.infoglue.cms.exception.ConstraintException;

public interface SiteNodeTypeDefinition  extends IBaseEntity
{
 	public Integer getId();
        
    public SiteNodeTypeDefinitionVO getValueObject();
    
    public void setValueObject(SiteNodeTypeDefinitionVO valueObject);

    public java.lang.Integer getSiteNodeTypeDefinitionId();
    
    public void setSiteNodeTypeDefinitionId(java.lang.Integer siteNodeTypeDefinitionId);
    
    public java.lang.String getInvokerClassName();
    
    public void setInvokerClassName(java.lang.String invokerClassName) throws ConstraintException;
    
    public java.lang.String getName();
    
    public void setName(java.lang.String name) throws ConstraintException;
    
    public java.lang.String getDescription();
    
    public void setDescription(java.lang.String description) throws ConstraintException;
    
    public java.util.Collection getAvailableServiceBindings();
    
    public void setAvailableServiceBindings(java.util.Collection availableServiceBindings);
        
}
