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
import org.infoglue.cms.exception.SystemException;

public interface ServerNode  extends IBaseEntity
{
  	public Integer getId();
       
    public ServerNodeVO getValueObject();
    
    public void setValueObject(ServerNodeVO valueObject);
    
    public java.lang.Integer getServerNodeId();
    
    public void setServerNodeId(java.lang.Integer serverNodeId) throws SystemException;
    
    public java.lang.String getName();
    
    public void setName(java.lang.String name) throws ConstraintException;
    
    public java.lang.String getDescription();
    
    public void setDescription(java.lang.String description) throws ConstraintException;

    public java.lang.String getDnsName();
    
    public void setDnsName(java.lang.String dnsName) throws ConstraintException;
        
}
