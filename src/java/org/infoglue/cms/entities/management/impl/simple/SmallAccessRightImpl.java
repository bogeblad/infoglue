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
import org.infoglue.cms.entities.management.AccessRightVO;

public class SmallAccessRightImpl extends AccessRightImpl
{
	private AccessRightVO valueObject = new AccessRightVO();
	
    public java.lang.Integer getAccessRightId()
    {
        return this.valueObject.getAccessRightId();
    }
      
	public Object getIdAsObject()
	{
		return getId();
	}
        
    public void setAccessRightId(java.lang.Integer accessRightId)
    {
        this.valueObject.setAccessRightId(accessRightId);
    }
      
    public java.lang.String getName()
    {
        return this.valueObject.getName(); 
    }
            
    public void setName(java.lang.String name)
    {
        this.valueObject.setName(name);
    }

	public String getParameters()
	{
        return this.valueObject.getParameters();
	}

	public void setParameters(String parameters)
	{
        this.valueObject.setParameters(parameters);
	}

	public Integer getInterceptionPointId()
	{
		return this.valueObject.getInterceptionPointId();
	}
	
	public void setInterceptionPointId(Integer interceptionPointId)
	{
	    this.valueObject.setInterceptionPointId(interceptionPointId);
	}
	
	public Integer getId()
	{
		return this.getAccessRightId();
	}

	public AccessRightVO getValueObject()
	{
		return this.valueObject;
	}
        
	public void setValueObject(AccessRightVO valueObject)
	{
		this.valueObject = valueObject;
	}   

	public BaseEntityVO getVO() 
	{
		return (BaseEntityVO) getValueObject();
	}

	public void setVO(BaseEntityVO valueObject) 
	{
		setValueObject((AccessRightVO) valueObject);
	}
	
}        
