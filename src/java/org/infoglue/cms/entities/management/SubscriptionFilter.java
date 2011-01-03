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

public interface SubscriptionFilter extends IBaseEntity
{
  	public Integer getId();
       
    public SubscriptionFilterVO getValueObject();
    
    public void setValueObject(SubscriptionFilterVO valueObject);
    
    public void setSubscription(Subscription subscription);
    
    public Subscription getSubscription();
    
    public java.lang.Integer getSubscriptionFilterId();
            
    public void setSubscriptionFilterId(java.lang.Integer subscriptionFilterId) throws SystemException;

	public String getFilterType();

	public String getFilterCondition();

	public Boolean getIsAndCondition();

	public void setFilterType(String filterType) throws ConstraintException;

	public void setFilterCondition(String filterCondition) throws ConstraintException;

	public void setIsAndCondition(Boolean isAndCondition) throws ConstraintException;
	
}
