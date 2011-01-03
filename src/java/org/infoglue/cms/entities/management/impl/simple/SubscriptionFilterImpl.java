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

package org.infoglue.cms.entities.management.impl.simple;


import java.util.Collection;
import java.util.Date;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Subscription;
import org.infoglue.cms.entities.management.SubscriptionFilter;
import org.infoglue.cms.entities.management.SubscriptionFilterVO;
import org.infoglue.cms.entities.management.SubscriptionVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;


public class SubscriptionFilterImpl implements SubscriptionFilter
{
    private SubscriptionFilterVO valueObject = new SubscriptionFilterVO();
    private Subscription subscription = null;
    
	public String toString()
	{
		return this.valueObject.toString();
	}
	
	public Integer getId()
	{
		return this.getSubscriptionFilterId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}
	     
    public SubscriptionFilterVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(SubscriptionFilterVO valueObject)
    {
        this.valueObject = valueObject;
    }   
	
    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getVO()
	 */
	public BaseEntityVO getVO() 
	{
		return (BaseEntityVO) getValueObject();
	}
	
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#setVO(BaseEntityVO)
	 */
	public void setVO(BaseEntityVO valueObject) 
	{
		setValueObject((SubscriptionFilterVO) valueObject);
	}  
    
    public java.lang.Integer getSubscriptionFilterId()
    {
        return this.valueObject.getSubscriptionFilterId();
    }
            
    public void setSubscriptionFilterId(java.lang.Integer subscriptionFilterId) throws SystemException
    {
        this.valueObject.setSubscriptionId(subscriptionFilterId);
    }

	public String getFilterType()
	{
		return this.valueObject.getFilterType();
	}

	public String getFilterCondition()
	{
		return this.valueObject.getFilterCondition();
	}

	public Boolean getIsAndCondition()
	{
		return this.valueObject.getIsAndCondition();
	}

	public void setFilterType(String filterType) throws ConstraintException
	{
		this.valueObject.setFilterType(filterType);
	}

	public void setFilterCondition(String filterCondition) throws ConstraintException
	{
		this.valueObject.setFilterCondition(filterCondition);
	}

	public void setIsAndCondition(Boolean isAndCondition) throws ConstraintException
	{
		this.valueObject.setIsAndCondition(isAndCondition);
	}

	public Subscription getSubscription()
	{
		return subscription;
	}

	public void setSubscription(Subscription subscription)
	{
		this.subscription = subscription;
	}

}        
