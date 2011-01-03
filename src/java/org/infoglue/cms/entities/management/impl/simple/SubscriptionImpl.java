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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Subscription;
import org.infoglue.cms.entities.management.SubscriptionVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;


public class SubscriptionImpl implements Subscription
{
    private SubscriptionVO valueObject = new SubscriptionVO();
    private Collection<SubscriptionFilterImpl> subscriptionFilters = new ArrayList<SubscriptionFilterImpl>();
    
	public String toString()
	{
		return this.valueObject.toString();
	}
	
	public Integer getId()
	{
		return this.getSubscriptionId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}
	     
    public SubscriptionVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(SubscriptionVO valueObject)
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
		setValueObject((SubscriptionVO) valueObject);
	}  
    
    public java.lang.Integer getSubscriptionId()
    {
        return this.valueObject.getSubscriptionId();
    }
            
    public void setSubscriptionId(java.lang.Integer SubscriptionId) throws SystemException
    {
        this.valueObject.setSubscriptionId(SubscriptionId);
    }

	public String getEntityId()
	{
		return this.valueObject.getEntityId();
	}

	public String getEntityName()
	{
		return this.valueObject.getEntityName();
	}

	public Integer getInterceptionPointId()
	{
		return this.valueObject.getInterceptionPointId();
	}

	public String getUserEmail()
	{
		return this.valueObject.getUserEmail();
	}

	public String getUserName()
	{
		return this.valueObject.getUserName();
	}

	public String getName()
	{
		return this.valueObject.getName();
	}

	public Date getLastNotifiedDateTime()
	{
		return this.valueObject.getLastNotifiedDateTime();
	}

	public void setEntityId(String entityId) throws ConstraintException
	{
		this.valueObject.setEntityId(entityId);
	}

	public void setEntityName(String entityName) throws ConstraintException
	{
		this.valueObject.setEntityName(entityName);
	}

	public void setInterceptionPointId(Integer interceptionPointId) throws SystemException
	{
		this.valueObject.setInterceptionPointId(interceptionPointId);
	}

	public void setUserEmail(String userEmail) throws ConstraintException
	{
		this.valueObject.setUserEmail(userEmail);
	}

	public void setUserName(String userName) throws ConstraintException
	{
		this.valueObject.setUserName(userName);
	}

	public void setName(String name) throws ConstraintException
	{
		this.valueObject.setName(name);
	}

	public Boolean getIsGlobal()
	{
		return this.valueObject.getIsGlobal();
	}

	public void setIsGlobal(Boolean isGlobal) throws ConstraintException
	{
		this.valueObject.setIsGlobal(isGlobal);
	}

	public void setLastNotifiedDateTime(Date lastNotifiedDateTime) throws ConstraintException
	{
		this.valueObject.setLastNotifiedDateTime(lastNotifiedDateTime);
	}

	public Collection<SubscriptionFilterImpl> getSubscriptionFilters()
	{
		return subscriptionFilters;
	}

	public void setSubscriptionFilters(Collection<SubscriptionFilterImpl> subscriptionFilters)
	{
		this.subscriptionFilters = subscriptionFilters;
		Iterator<SubscriptionFilterImpl> subscriptionFiltersIterator = subscriptionFilters.iterator();
		while(subscriptionFiltersIterator.hasNext())
		{
			getValueObject().getSubscriptionFilterVOList().add(subscriptionFiltersIterator.next().getValueObject());
		}
	}

}        
