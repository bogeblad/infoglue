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
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptorController;
import org.infoglue.cms.entities.management.InterceptorVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * This action represents the Create Interceptor Usecase.
 */

public class CreateInterceptorAction extends InfoGlueAbstractAction
{
   	private ConstraintExceptionBuffer ceb;
   	private InterceptorVO interceptorVO;
  
  
  	public CreateInterceptorAction()
	{
		this(new InterceptorVO());
	}
	
	public CreateInterceptorAction(InterceptorVO interceptorVO)
	{
		this.interceptorVO = interceptorVO;
		this.ceb = new ConstraintExceptionBuffer();
			
	}	
	      
    public String doExecute() throws Exception
    {
		ceb.add(this.interceptorVO.validate());
    	ceb.throwIfNotEmpty();				
    	
    	this.interceptorVO = InterceptorController.getController().create(interceptorVO);
	    
        return "success";
    }

    public String doInput() throws Exception
    {
        return "input";
    }

	/**
	 * @return Returns the InterceptorId if it's been created.
	 */
    
	public Integer getInterceptorId() 
	{
		return this.interceptorVO.getInterceptorId();
	}

	/**
	 * @return Returns the ClassName.
	 */
    
	public String getClassName() 
	{
		return this.interceptorVO.getClassName();
	}
	
	/**
	 * @param className The ClassName to set.
	 */
	public void setClassName(String className) 
	{
		this.interceptorVO.setClassName(className);
	}
	
	/**
	 * @return Returns the description.
	 */
	
	public String getDescription() 
	{
		return this.interceptorVO.getDescription();
	}
	
	/**
	 * @param description The description to set.
	 */
	
	public void setDescription(String description) 
	{
		this.interceptorVO.setDescription(description);
	}
	
	/**
	 * @return Returns the name.
	 */
	
	public String getName() 
	{
		return this.interceptorVO.getName();
	}
	
	/**
	 * @param name The name to set.
	 */
	
	public void setName(String name) 
	{
		this.interceptorVO.setName(name);
	}
	
}
