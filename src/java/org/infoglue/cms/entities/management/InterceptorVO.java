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
import org.infoglue.cms.util.validators.ValidatorFactory;

public class InterceptorVO implements BaseEntityVO
{
    private java.lang.Integer interceptorId;
    private java.lang.String name;
    private java.lang.String description;
    private java.lang.String className;

  
    public java.lang.Integer getInterceptorId()
    {
        return this.interceptorId;
    }
                
    public void setInterceptorId(java.lang.Integer interceptorId)
    {
        this.interceptorId = interceptorId;
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
        return this.description;
    }
                
    public void setDescription(java.lang.String description)
    {
         this.description = description;
    }
    
	public java.lang.String getClassName()
	{
		return className;
	}

	public void setClassName(java.lang.String className)
	{
		this.className = className;
	}

	public ConstraintExceptionBuffer validate() 
	{
    	ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		
		ValidatorFactory.createStringValidator("Interceptor.name", true, 2, 100).validate(name, ceb);
		ValidatorFactory.createStringValidator("Interceptor.description", true, 2, 100).validate(description, ceb);
		ValidatorFactory.createStringValidator("Interceptor.className", true, 2, 100).validate(className, ceb);

    	return ceb;
	}
    
	public String toString()
	{  
		return getName();
	}
   
	public Integer getId() 
	{
		return getInterceptorId();
	}

}
        
