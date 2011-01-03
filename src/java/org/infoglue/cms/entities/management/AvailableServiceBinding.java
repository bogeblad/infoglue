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

public interface AvailableServiceBinding extends IBaseEntity
{
 	public Integer getId();
       
    public AvailableServiceBindingVO getValueObject();
    
    public void setValueObject(AvailableServiceBindingVO valueObject);

    
    public java.lang.Integer getAvailableServiceBindingId();
    
    public void setAvailableServiceBindingId(java.lang.Integer availableServiceBindingId);
    
    public java.lang.String getName();
    
    public void setName(java.lang.String name) throws ConstraintException;
    
    public java.lang.String getDescription();
    
    public void setDescription(java.lang.String description) throws ConstraintException;
    
    public java.lang.String getVisualizationAction();
    
    public void setVisualizationAction(java.lang.String visualizationAction) throws ConstraintException;
    
    public java.lang.Boolean getIsMandatory();
    
    public void setIsMandatory(java.lang.Boolean isMandatory) throws ConstraintException;
    
    public java.lang.Boolean getIsUserEditable();
    
    public void setIsUserEditable(java.lang.Boolean isUserEditable) throws ConstraintException;
    
    public java.lang.Boolean getIsInheritable();
    
    public void setIsInheritable(java.lang.Boolean isInheritable) throws ConstraintException;

    public java.util.Collection getSiteNodeTypeDefinitions();
    
    public void setSiteNodeTypeDefinitions(java.util.Collection siteNodeTypeDefinitions);
    
    public java.util.Collection getServiceDefinitions();
    
    public void setServiceDefinitions(java.util.Collection serviceDefinitions);
    /*    
    public java.util.Collection getServiceBindings();
    
    public void setServiceBindings(java.util.Collection serviceBindings);
	*/
}
