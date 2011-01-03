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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.sorters.ReflectionComparator;

/**
 * This action represents the Create InterceptionPoint Usecase.
 */

public class CreateInterceptionPointAction extends InfoGlueAbstractAction
{
   	private ConstraintExceptionBuffer ceb;
   	private InterceptionPointVO interceptionPointVO;
   	private String interceptionPointVOName;
   	
   	private List<InterceptionPointVO> inactiveInterceptionPointVOList = null;
  
  	public CreateInterceptionPointAction()
	{
		this(new InterceptionPointVO());
	}
	
	public CreateInterceptionPointAction(InterceptionPointVO interceptionPointVO)
	{
		this.interceptionPointVO = interceptionPointVO;
		this.ceb = new ConstraintExceptionBuffer();
			
	}	
	      
    public String doExecute() throws Exception
    {
    	if(interceptionPointVOName != null && !interceptionPointVOName.equals(""))
    	{
    		this.interceptionPointVO = (InterceptionPointVO)InterceptionPointController.systemInterceptionPoints.get(interceptionPointVOName);
    	}
    	
		ceb.add(this.interceptionPointVO.validate());
    	ceb.throwIfNotEmpty();				
    	
    	this.interceptionPointVO = InterceptionPointController.getController().create(interceptionPointVO);
	    
        return "success";
    }

    public String doInput() throws Exception
    {
    	this.inactiveInterceptionPointVOList = InterceptionPointController.getController().getInactiveInterceptionPointVOList();
    	
		Collections.sort(this.inactiveInterceptionPointVOList, new ReflectionComparator("name"));

    	return "input";
    }

	/**
	 * @return Returns the InterceptionPointId if it's been created.
	 */
    
	public Integer getInterceptionPointId() 
	{
		return this.interceptionPointVO.getInterceptionPointId();
	}

	/**
	 * @return Returns the category.
	 */
    
	public String getCategory() 
	{
		return interceptionPointVO.getCategory();
	}
	
	/**
	 * @param category The category to set.
	 */
	public void setCategory(String category) 
	{
		this.interceptionPointVO.setCategory(category);
	}
	
	/**
	 * @return Returns the description.
	 */
	
	public String getDescription() 
	{
		return this.interceptionPointVO.getDescription();
	}
	
	/**
	 * @param description The description to set.
	 */
	
	public void setDescription(String description) 
	{
		this.interceptionPointVO.setDescription(description);
	}
	
	/**
	 * @return Returns the name.
	 */
	
	public String getName() 
	{
		return this.interceptionPointVO.getName();
	}
	
	/**
	 * @param name The name to set.
	 */
	
	public void setName(String name) 
	{
		this.interceptionPointVO.setName(name);
	}
	
	/**
	 * @return Returns the usesExtraDataForAccessControl.
	 */
	
	public Boolean getUsesExtraDataForAccessControl() 
	{
		return this.interceptionPointVO.getUsesExtraDataForAccessControl();
	}
	
	/**
	 * @param usesExtraDataForAccessControl The usesExtraDataForAccessControl to set.
	 */
	
	public void setUsesExtraDataForAccessControl(Boolean usesExtraDataForAccessControl) 
	{
		this.interceptionPointVO.setUsesExtraDataForAccessControl(usesExtraDataForAccessControl);
	}

	public Collection getInactiveInterceptionPointVOList() 
	{
		return inactiveInterceptionPointVOList;
	}

	public String getInterceptionPointVOName() 
	{
		return interceptionPointVOName;
	}

	public void setInterceptionPointVOName(String interceptionPointVOName) 
	{
		this.interceptionPointVOName = interceptionPointVOName;
	}
}
