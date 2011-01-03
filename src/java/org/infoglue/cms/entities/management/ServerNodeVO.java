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
import org.infoglue.cms.entities.management.impl.simple.ServerNodeImpl;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.validators.ValidatorFactory;

public class ServerNodeVO implements BaseEntityVO
{
    private java.lang.Integer serverNodeId = -1;
    private java.lang.String name;
    private java.lang.String description;
    private java.lang.String dnsName;
        
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	
    public Integer getId() 
	{
		return getServerNodeId();
	}

	public String toString()
	{  
		return getName();
	}
  
    public java.lang.Integer getServerNodeId()
    {
        return this.serverNodeId;
    }
                
    public void setServerNodeId(java.lang.Integer serverNodeId)
    {
        this.serverNodeId = serverNodeId;
    }
    
    public java.lang.String getName()
    {
        return this.name;
    }
                
    public void setName(java.lang.String name)
    {
        this.name = name;		
    }

    public java.lang.String getDescription()
    {
        return description;
    }
    public void setDescription(java.lang.String description)
    {
        this.description = description;
    }
  
    public java.lang.String getDnsName()
    {
        return this.dnsName;
    }
                
    public void setDnsName(java.lang.String dnsName)
    {
        this.dnsName = dnsName;
    }

	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() 
	{
    	ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
    	
    	ValidatorFactory.createStringValidator("ServerNode.name", true, 6, 20, true, ServerNodeImpl.class, this.getId(), null).validate(this.name, ceb);
        ValidatorFactory.createStringValidator("ServerNode.description", true, 1, 100).validate(description, ceb); 
    	if(dnsName != null)
    	    ValidatorFactory.createStringValidator("ServerNode.dnsName", false, 0, 200).validate(dnsName, ceb); 
    	
    	return ceb;
	}
        
}
        
