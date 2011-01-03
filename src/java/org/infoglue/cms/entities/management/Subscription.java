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

import java.util.Collection;
import java.util.Date;

import org.infoglue.cms.entities.kernel.IBaseEntity;
import org.infoglue.cms.entities.management.impl.simple.SubscriptionFilterImpl;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;

public interface Subscription extends IBaseEntity
{
  	public Integer getId();
       
    public SubscriptionVO getValueObject();
    
    public void setValueObject(SubscriptionVO valueObject);
    
    public java.lang.Integer getSubscriptionId();
    
    public void setSubscriptionId(java.lang.Integer subscriptionId) throws SystemException;

    public java.lang.Integer getInterceptionPointId();
    
    public void setInterceptionPointId(java.lang.Integer interceptionPointId) throws SystemException;

    public java.lang.String getEntityName();
    
    public void setEntityName(java.lang.String entityName) throws ConstraintException;

    public java.lang.String getEntityId();
    
    public void setEntityId(java.lang.String entityId) throws ConstraintException;

    public java.lang.String getUserName();
    
    public void setUserName(java.lang.String userName) throws ConstraintException;

    public java.lang.String getUserEmail();
    
    public void setUserEmail(java.lang.String userEmail) throws ConstraintException;

    public java.lang.String getName();
    
    public void setName(java.lang.String name) throws ConstraintException;

    public Boolean getIsGlobal();
    
    public void setIsGlobal(Boolean IsGlobal) throws ConstraintException;

    public Date getLastNotifiedDateTime();
    
    public void setLastNotifiedDateTime(Date lastNotifiedDateTime) throws ConstraintException;

    public Collection<SubscriptionFilterImpl> getSubscriptionFilters();
    
    public void setSubscriptionFilters(Collection<SubscriptionFilterImpl> subscriptionFilters) throws ConstraintException;

}
