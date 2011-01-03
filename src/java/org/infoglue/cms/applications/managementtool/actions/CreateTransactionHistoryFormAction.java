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

package org.infoglue.cms.applications.managementtool.actions;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;

public class CreateTransactionHistoryFormAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
    private java.lang.Integer transactionHistoryId = null;
    private java.lang.String name = null;
  
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
      
    public String doExecute() throws Exception
    {
        //Here we should put things that are needed for this view. 
        //Could be list of choices or other stuff
        
        return "success";
    }
        
}
