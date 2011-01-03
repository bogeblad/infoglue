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
import org.infoglue.cms.entities.management.Interceptor;
import org.infoglue.cms.entities.management.InterceptorVO;


public class InterceptorImpl implements Interceptor
{
    private InterceptorVO valueObject = new InterceptorVO();
    private java.util.Collection interceptionPoints = new ArrayList();
  
    public java.lang.Integer getInterceptorId()
    {
        return this.valueObject.getInterceptorId();
    }
      
	public Object getIdAsObject()
	{
		return getId();
	}
      
    public void setInterceptorId(java.lang.Integer interceptorId)
    {
        this.valueObject.setInterceptorId(interceptorId);
    }
      
    public java.lang.String getName()
    {
        return this.valueObject.getName();
    }
            
    public void setName(java.lang.String name)
    {
        this.valueObject.setName(name);
    }
      
    public java.lang.String getDescription()
    {
        return this.valueObject.getDescription();
    }
            
    public void setDescription(java.lang.String description)
    {
        this.valueObject.setDescription(description);
    }
      
    public java.lang.String getClassName()
    {
        return this.valueObject.getClassName();
    }
            
    public void setClassName(java.lang.String className)
    {
        this.valueObject.setClassName(className);
    }
      
	public Collection getInterceptionPoints()
	{
		return this.interceptionPoints;
	}

	public void setInterceptionPoints(Collection interceptionPoints)
	{
		this.interceptionPoints = interceptionPoints;		
	}
	
	public Integer getId()
	{
		return this.getInterceptorId();
	}

	public String toString()
	{
		return this.valueObject.toString();
	}

	public InterceptorVO getValueObject()
	{
		return this.valueObject;
	}

	public void setValueObject(InterceptorVO valueObject)
	{
		this.valueObject = valueObject;
	}   

	public BaseEntityVO getVO() 
	{
		return (BaseEntityVO) getValueObject();
	}
	
	public void setVO(BaseEntityVO valueObject) 
	{
		setValueObject((InterceptorVO) valueObject);
	}
}        
