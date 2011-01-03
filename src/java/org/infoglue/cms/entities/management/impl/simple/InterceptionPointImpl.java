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

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.InterceptionPoint;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.exception.ConstraintException;

public class InterceptionPointImpl implements InterceptionPoint
{
    private InterceptionPointVO valueObject = new InterceptionPointVO();
	private java.util.Collection interceptors = new ArrayList();

	public Integer getInterceptionPointId()
	{
		return this.valueObject.getInterceptionPointId();
	}

	public Object getIdAsObject()
	{
		return getId();
	}

	public void setInterceptionPointId(Integer interceptionPointId)
	{
		this.valueObject.setInterceptionPointId(interceptionPointId);
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
      
	public String getCategory()
	{
		return this.valueObject.getCategory();
	}
	
	public void setCategory(String category) throws ConstraintException
	{
		this.valueObject.setCategory(category);	
	}
	
	public Boolean getUsesExtraDataForAccessControl()
	{
		return this.valueObject.getUsesExtraDataForAccessControl();
	}
	
	public void setUsesExtraDataForAccessControl(Boolean usesExtraDataForAccessControl) throws ConstraintException
	{
		this.valueObject.setUsesExtraDataForAccessControl(usesExtraDataForAccessControl);
	}
      
	public Collection getInterceptors()
	{
		return this.interceptors;
	}

	public void setInterceptors(Collection interceptors)
	{
		this.interceptors = interceptors;		
	}

	public Integer getId()
	{
		return this.getInterceptionPointId();
	}

	public String toString()
	{
		return this.valueObject.toString();
	}

	public InterceptionPointVO getValueObject()
	{
		return this.valueObject;
	}

	public void setValueObject(InterceptionPointVO valueObject)
	{
		this.valueObject = valueObject;
	}   

	public BaseEntityVO getVO() 
	{
		return (BaseEntityVO) getValueObject();
	}

	public void setVO(BaseEntityVO valueObject) 
	{
		setValueObject((InterceptionPointVO) valueObject);
	}

}        
