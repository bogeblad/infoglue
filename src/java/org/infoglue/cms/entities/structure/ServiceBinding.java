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

public interface ServiceBinding extends IBaseEntity
{
        
    public ServiceBindingVO getValueObject();
    
    public void setValueObject(ServiceBindingVO valueObject);

    public java.lang.Integer getServiceBindingId();
    
    public void setServiceBindingId(java.lang.Integer serviceBindingId);
    
    public java.lang.String getName();
    
    public void setName(java.lang.String name);

    public java.lang.String getPath();
    
    public void setPath(java.lang.String path);
    
    public java.lang.Integer getBindingTypeId();
    
    public void setBindingTypeId(java.lang.Integer bindingTypeId);
    
    public java.util.Collection getBindingQualifyers();
    
    public void setBindingQualifyers(java.util.Collection bindingQualifyers);
    
    public org.infoglue.cms.entities.management.impl.simple.ServiceDefinitionImpl getServiceDefinition();
    
    public void setServiceDefinition(org.infoglue.cms.entities.management.impl.simple.ServiceDefinitionImpl serviceDefinition);

    public org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl getAvailableServiceBinding();
    
    public void setAvailableServiceBinding(org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl availableServiceBinding);

    public org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl getSiteNodeVersion();
        
    public void setSiteNodeVersion(org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl siteNodeVersion);
        
}
