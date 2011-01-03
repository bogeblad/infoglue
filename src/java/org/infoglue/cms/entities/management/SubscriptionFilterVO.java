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

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class SubscriptionFilterVO implements BaseEntityVO
{
    private Integer subscriptionFilterId;
    private Integer subscriptionId;
    private String filterType;
    private String filterCondition;
    private Boolean isAndCondition = new Boolean(false);
    
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	
    public Integer getId() 
	{
		return getSubscriptionFilterId();
	}

	public String toString()
	{  
		StringBuffer sb = new StringBuffer();
		sb.append("subscriptionFilterId: " + subscriptionFilterId + "\n");
		sb.append("subscriptionId: " + subscriptionId + "\n");
		sb.append("filterType: " + filterType + "\n");
		sb.append("filterCondition: " + filterCondition + "\n");
		sb.append("isAndCondition: " + isAndCondition + "\n");
		return sb.toString();
	}
  
    public java.lang.Integer getSubscriptionFilterId()
    {
        return this.subscriptionFilterId;
    }
                
    public void setSubscriptionFilterId(java.lang.Integer subscriptionFilterId)
    {
        this.subscriptionFilterId = subscriptionFilterId;
    }
    
	public Integer getSubscriptionId()
	{
		return subscriptionId;
	}

	public void setSubscriptionId(Integer subscriptionId)
	{
		this.subscriptionId = subscriptionId;
	}

	public String getFilterType()
	{
		return filterType;
	}

	public void setFilterType(String filterType)
	{
		this.filterType = filterType;
	}

	public String getFilterCondition()
	{
		return filterCondition;
	}

	public void setFilterCondition(String filterCondition)
	{
		this.filterCondition = filterCondition;
	}

	public Boolean getIsAndCondition()
	{
		return isAndCondition;
	}

	public void setIsAndCondition(Boolean isAndCondition)
	{
		if(isAndCondition != null)
			this.isAndCondition = isAndCondition;
	}

	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() 
	{
    	ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
    	
    	return ceb;
	}

}
        
