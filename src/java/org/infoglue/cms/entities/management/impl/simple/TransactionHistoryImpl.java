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

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.TransactionHistory;
import org.infoglue.cms.entities.management.TransactionHistoryVO;

public class TransactionHistoryImpl implements TransactionHistory
{
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getId()
	 */
	public Integer getId() 
	{
		return getTransactionHistoryId();
	}

	public Object getIdAsObject()
	{
		return getId();
	}

    private TransactionHistoryVO valueObject = new TransactionHistoryVO();
     
    public TransactionHistoryVO getValueObject()
    {
        return this.valueObject;
    }

        
    public void setValueObject(TransactionHistoryVO valueObject)
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
		setValueObject((TransactionHistoryVO) valueObject);
	}

    private org.infoglue.cms.entities.management.impl.simple.SystemUserImpl user;
  
    
    public java.lang.Integer getTransactionHistoryId()
    {
        return this.valueObject.getTransactionHistoryId();
    }
            
    public void setTransactionHistoryId(java.lang.Integer transactionHistoryId)
    {
        this.valueObject.setTransactionHistoryId(transactionHistoryId);
    }
      
    public java.lang.String getName()
    {
        return this.valueObject.getName();
    }
            
    public void setName(java.lang.String name)
    {
        this.valueObject.setName(name);
    }
      
    public java.util.Date getTransactionDateTime()
    {
        return this.valueObject.getTransactionDateTime();
    }
            
    public void setTransactionDateTime(java.util.Date transactionDateTime)
    {
        this.valueObject.setTransactionDateTime(transactionDateTime);
    }
      
    public java.lang.Integer getTransactionTypeId()
    {
        return this.valueObject.getTransactionTypeId();
    }
            
    public void setTransactionTypeId(java.lang.Integer transactionTypeId)
    {
        this.valueObject.setTransactionTypeId(transactionTypeId);
    }
      
    public java.lang.String getTransactionObjectId()
    {
        return this.valueObject.getTransactionObjectId();
    }
            
    public void setTransactionObjectId(java.lang.String transactionObjectId)
    {
        this.valueObject.setTransactionObjectId(transactionObjectId);
    }
      
    public java.lang.String getTransactionObjectName()
    {
        return this.valueObject.getTransactionObjectName();
    }
            
    public void setTransactionObjectName(java.lang.String transactionObjectName)
    {
        this.valueObject.setTransactionObjectName(transactionObjectName);
    }
      
    public java.lang.String getSystemUserName()
    {
        return this.valueObject.getSystemUserName();
    }
            
    public void setSystemUserName (java.lang.String systemUserName)
    {
        this.valueObject.setSystemUserName(systemUserName);
    }

}        
