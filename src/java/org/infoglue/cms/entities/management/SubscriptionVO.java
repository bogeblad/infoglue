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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.impl.simple.SubscriptionImpl;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.validators.ValidatorFactory;

public class SubscriptionVO implements BaseEntityVO
{
    private Integer subscriptionId;
    private Integer interceptionPointId;
    private String entityName;
    private String entityId;
    private String userName;
    private String userEmail;
    private String name;
    private Boolean isGlobal = new Boolean(false);
    private Date lastNotifiedDateTime = new Date();
    private Collection<SubscriptionFilterVO> subscriptionFilterVO = new ArrayList();
    
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	
    public Integer getId() 
	{
		return getSubscriptionId();
	}

	public String toString()
	{  
		return "" + subscriptionId;
	}
  
    public java.lang.Integer getSubscriptionId()
    {
        return this.subscriptionId;
    }
                
    public void setSubscriptionId(java.lang.Integer subscriptionId)
    {
        this.subscriptionId = subscriptionId;
    }
    
	public java.lang.Integer getInterceptionPointId()
	{
		return interceptionPointId;
	}

	public void setInterceptionPointId(java.lang.Integer interceptionPointId)
	{
		this.interceptionPointId = interceptionPointId;
	}

	public java.lang.String getEntityName()
	{
		return entityName;
	}

	public void setEntityName(java.lang.String entityName)
	{
		this.entityName = entityName;
	}

	public java.lang.String getEntityId()
	{
		return entityId;
	}

	public void setEntityId(java.lang.String entityId)
	{
		this.entityId = entityId;
	}

	public java.lang.String getUserName()
	{
		return userName;
	}

	public void setUserName(java.lang.String userName)
	{
		this.userName = userName;
	}

	public java.lang.String getUserEmail()
	{
		return userEmail;
	}

	public void setUserEmail(java.lang.String userEmail)
	{
		this.userEmail = userEmail;
	}
        
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Boolean getIsGlobal()
	{
		return isGlobal;
	}

	public void setIsGlobal(Boolean isGlobal)
	{
		this.isGlobal = isGlobal;
	}
	
	public Date getLastNotifiedDateTime()
	{
		return lastNotifiedDateTime;
	}

	public void setLastNotifiedDateTime(Date lastNotifiedDateTime)
	{
		this.lastNotifiedDateTime = lastNotifiedDateTime;
	}

	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() 
	{
    	ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
    	
    	//ValidatorFactory.createStringValidator("Subscription.url", true, 1, 1024, true, SubscriptionImpl.class, this.getId(), null).validate(this.url, ceb);
    	//ValidatorFactory.createStringValidator("Subscription.subscriptionUrl", true, 1, 1024).validate(subscriptionUrl, ceb); 
    	
    	return ceb;
	}

	public Collection<SubscriptionFilterVO> getSubscriptionFilterVOList()
	{
		return subscriptionFilterVO;
	}

}
        
