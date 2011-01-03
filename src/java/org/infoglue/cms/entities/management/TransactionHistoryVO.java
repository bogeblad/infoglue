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
import org.infoglue.cms.util.NotificationMessage;

public class TransactionHistoryVO  implements BaseEntityVO
{

    private java.lang.Integer transactionHistoryId;
    private java.lang.String name;
    private java.util.Date transactionDateTime;
    private java.lang.Integer transactionTypeId;
    private java.lang.String transactionObjectId;
    private java.lang.String transactionObjectName;
    private java.lang.String systemUserName;
    
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	public Integer getId() {
		return getTransactionHistoryId();
	}


    public java.lang.Integer getTransactionHistoryId()
    {
        return this.transactionHistoryId;
    }
                
    public void setTransactionHistoryId(java.lang.Integer transactionHistoryId)
    {
        this.transactionHistoryId = transactionHistoryId;
    }
    
    public java.lang.String getName()
    {
        return this.name;
    }
                
    public void setName(java.lang.String name)
    {
        this.name = name;
    }
    
    public java.util.Date getTransactionDateTime()
    {
        return this.transactionDateTime;
    }
                
    public void setTransactionDateTime(java.util.Date transactionDateTime)
    {
        this.transactionDateTime = transactionDateTime;
    }
    
    public String getTransactionTypeName()
    {    		
        return NotificationMessage.getTransactionTypeName(getTransactionTypeId());
    }

    public java.lang.Integer getTransactionTypeId()
    {
        return this.transactionTypeId;
    }
                
    public void setTransactionTypeId(java.lang.Integer transactionTypeId)
    {
        this.transactionTypeId = transactionTypeId;
    }
    
    public java.lang.String getTransactionObjectId()
    {
        return this.transactionObjectId;
    }
                
    public void setTransactionObjectId(java.lang.String transactionObjectId)
    {
        this.transactionObjectId = transactionObjectId;
    }
    
    public java.lang.String getTransactionObjectName()
    {
        return this.transactionObjectName;
    }
                
    public void setTransactionObjectName(java.lang.String transactionObjectName)
    {
        this.transactionObjectName = transactionObjectName;
    }

    public java.lang.String getSystemUserName()
    {
        return this.systemUserName;
    }
                
    public void setSystemUserName(java.lang.String systemUserName)
    {
        this.systemUserName = systemUserName;
    }
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() {
		return new ConstraintExceptionBuffer();
	}
    
}
        
