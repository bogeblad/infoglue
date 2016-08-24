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

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptorController;
import org.infoglue.cms.entities.management.InterceptorVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;


/**
 * This is the action-class for UpdateInterceptor
 * 
 * @author Mattias Bogeblad
 */

public class UpdateInterceptorAction extends ViewInterceptorAction
{
    private final static Logger logger = Logger.getLogger(UpdateInterceptorAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private InterceptorVO interceptorVO = new InterceptorVO();
    private String configuration = null;
	private ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
	
	
	public String doExecute() throws Exception
    {
		super.initialize(getInterceptorId());
		
    	ceb.add(this.interceptorVO.validate());
    	ceb.throwIfNotEmpty();		
    	
    	String[] values = getRequest().getParameterValues("interceptionPointId");
    	logger.info("values:" + values);
		
		InterceptorController.getController().update(this.interceptorVO, values, configuration);
    	
		return "success";
	}
	
	public String doSaveAndExit() throws Exception
    {
		doExecute();
						
		return "saveAndExit";
	}
	
    

	public Integer getInterceptorId()
	{
		return this.interceptorVO.getInterceptorId();
	}

	public void setInterceptorId(Integer interceptorId)
	{
		this.interceptorVO.setInterceptorId(interceptorId);
	}

	public InterceptorVO getInterceptorVO()
	{
		return this.interceptorVO;
	}
	
	public void setName(String name)
	{
		this.interceptorVO.setName(name);
	}
		
	public void setDescription(String description)
	{
		this.interceptorVO.setDescription(description);
	}

	public void setClassName(String className)
	{
		this.interceptorVO.setClassName(className);
	}

	public void setConfiguration(String configuration)
	{
		this.configuration = configuration;
	}

}
