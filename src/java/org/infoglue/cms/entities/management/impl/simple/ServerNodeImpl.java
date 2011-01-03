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
import org.infoglue.cms.entities.management.ServerNode;
import org.infoglue.cms.entities.management.ServerNodeVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;


public class ServerNodeImpl implements ServerNode
{
    private ServerNodeVO valueObject = new ServerNodeVO();

	public String toString()
	{
		return this.valueObject.toString();
	}
	
	public Integer getId()
	{
		return this.getServerNodeId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}
	     
    public ServerNodeVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(ServerNodeVO valueObject)
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
		setValueObject((ServerNodeVO) valueObject);
	}  
    
    public java.lang.Integer getServerNodeId()
    {
        return this.valueObject.getServerNodeId();
    }
            
    public void setServerNodeId(java.lang.Integer serverNodeId) throws SystemException
    {
        this.valueObject.setServerNodeId(serverNodeId);
    }
      
    public java.lang.String getName()
    {
        return this.valueObject.getName();
    }
            
    public void setName(java.lang.String name) throws ConstraintException
    {
        this.valueObject.setName(name);
    }
      
    public java.lang.String getDescription()
    {
        return this.valueObject.getDescription();
    }
    
    public void setDescription(java.lang.String description) throws ConstraintException
	{
        this.valueObject.setDescription(description);
    }
    
    public java.lang.String getDnsName()
    {
        return this.valueObject.getDnsName();
    }
    
    public void setDnsName(java.lang.String dnsName) throws ConstraintException
	{
        this.valueObject.setDnsName(dnsName);
    }

}        
