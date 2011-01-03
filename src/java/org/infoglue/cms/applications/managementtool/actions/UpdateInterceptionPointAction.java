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

import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;


/**
 * This is the action-class for UpdateInterceptionPoint
 * 
 * @author Mattias Bogeblad
 */

public class UpdateInterceptionPointAction extends ViewInterceptionPointAction
{
	private InterceptionPointVO interceptionPointVO = new InterceptionPointVO();
	private ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
	
	
	public String doExecute() throws Exception
    {
		super.initialize(getInterceptionPointId());
		
    	ceb.add(this.interceptionPointVO.validate());
    	ceb.throwIfNotEmpty();		
    	
    	String[] values = getRequest().getParameterValues("interceptorId");
    	
		InterceptionPointController.getController().update(this.interceptionPointVO, values);
    	
		return "success";
	}
	
	public String doSaveAndExit() throws Exception
    {
		doExecute();
						
		return "saveAndExit";
	}
	
    

	public Integer getInterceptionPointId()
	{
		return this.interceptionPointVO.getInterceptionPointId();
	}

	public void setInterceptionPointId(Integer interceptionPointId)
	{
		this.interceptionPointVO.setInterceptionPointId(interceptionPointId);
	}

	public InterceptionPointVO getInterceptionPointVO()
	{
		return this.interceptionPointVO;
	}
	
	public void setName(String name)
	{
		this.interceptionPointVO.setName(name);
	}
	
	public void setCategory(String category)
	{
		this.interceptionPointVO.setCategory(category);
	}
	
	public void setDescription(String description)
	{
		this.interceptionPointVO.setDescription(description);
	}

	public void setUsesExtraDataForAccessControl(Boolean usesExtraDataForAccessControl)
	{
		this.interceptionPointVO.setUsesExtraDataForAccessControl(usesExtraDataForAccessControl);
	}

}
