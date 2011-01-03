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

public interface TransactionHistory  extends IBaseEntity
{
        
    public TransactionHistoryVO getValueObject();
    
    public void setValueObject(TransactionHistoryVO valueObject);

    
    public java.lang.Integer getTransactionHistoryId();
    
    public void setTransactionHistoryId(java.lang.Integer transactionHistoryId);
    
    public java.lang.String getName();
    
    public void setName(java.lang.String name);
    
    public java.util.Date getTransactionDateTime();
    
    public void setTransactionDateTime(java.util.Date transactionDateTime);
    
    public java.lang.Integer getTransactionTypeId();
    
    public void setTransactionTypeId(java.lang.Integer transactionTypeId);
    
    public java.lang.String getTransactionObjectId();
    
    public void setTransactionObjectId(java.lang.String transactionObjectId);
    
    public java.lang.String getTransactionObjectName();
    
    public void setTransactionObjectName(java.lang.String transactionObjectName);
    
    public java.lang.String getSystemUserName();
    
    public void setSystemUserName(java.lang.String systemUserName);
        
}
