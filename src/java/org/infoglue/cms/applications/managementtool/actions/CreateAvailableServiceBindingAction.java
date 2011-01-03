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
import org.infoglue.cms.controllers.kernel.impl.simple.AvailableServiceBindingController;
import org.infoglue.cms.entities.management.AvailableServiceBindingVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class CreateAvailableServiceBindingAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
	private AvailableServiceBindingVO availableServiceBindingVO;
	private ConstraintExceptionBuffer ceb;
    private String name;
    private String description;
    private String visualizationAction;
    private Boolean isMandatory;
    private Boolean isUserEditable;
    private Boolean isInheritable;

	
	public CreateAvailableServiceBindingAction()
	{
		this(new AvailableServiceBindingVO());
	}
	
	public CreateAvailableServiceBindingAction(AvailableServiceBindingVO availableServiceBindingVO)
	{
		this.availableServiceBindingVO = availableServiceBindingVO;
		this.ceb = new ConstraintExceptionBuffer();
			
	}	
    public Integer getAvailableServiceBindingId()	
    {
    	return this.availableServiceBindingVO.getAvailableServiceBindingId();
    }
    public java.lang.String getName()
    {
    	if(this.name != null)
    		return this.name;
    		
        return this.availableServiceBindingVO.getName();
    }
        
    public void setName(java.lang.String name)
    {
    	try
    	{
        	this.availableServiceBindingVO.setName(name);
    	}
    	catch(ConstraintException ce)
    	{
    		this.name = name;
    		this.ceb.add(new ConstraintExceptionBuffer(ce));
    	}
    }
      

    public String getDescription()
    {
    	if(this.description != null)
    		return this.description;
    		
        return this.availableServiceBindingVO.getDescription();
    }
        
    public void setDescription(String description)
    {
    	try
    	{
        	this.availableServiceBindingVO.setDescription(description);
    	}
    	catch(ConstraintException ce)
    	{
    		this.description = description;
    		this.ceb.add(new ConstraintExceptionBuffer(ce));
    	}
    }

    public String getVisualizationAction()
    {
    	if(this.visualizationAction != null)
    		return this.visualizationAction;
    		
        return this.availableServiceBindingVO.getVisualizationAction();
    }
        
    public void setVisualizationAction(String visualizationAction)
    {
    	try
    	{
        	this.availableServiceBindingVO.setVisualizationAction(visualizationAction);
    	}
    	catch(ConstraintException ce)
    	{
    		this.visualizationAction = visualizationAction;
    		this.ceb.add(new ConstraintExceptionBuffer(ce));
    	}
    }


    public Boolean getIsMandatory()
    {
    	if(this.isMandatory != null)
    		return this.isMandatory;
    		
        return this.availableServiceBindingVO.getIsMandatory();
    }
        
    public void setIsMandatory(Boolean isMandatory)
    {
    	try
    	{
        	this.availableServiceBindingVO.setIsMandatory(isMandatory);
    	}
    	catch(ConstraintException ce)
    	{
    		this.isMandatory = isMandatory;
    		this.ceb.add(new ConstraintExceptionBuffer(ce));
    	}
    }


    public Boolean getIsUserEditable()
    {
    	if(this.isUserEditable != null)
    		return this.isUserEditable;
    		
        return this.availableServiceBindingVO.getIsUserEditable();
    }
        
    public void setIsUserEditable(Boolean isUserEditable)
    {
    	try
    	{
        	this.availableServiceBindingVO.setIsUserEditable(isUserEditable);
    	}
    	catch(ConstraintException ce)
    	{
    		this.isUserEditable = isUserEditable;
    		this.ceb.add(new ConstraintExceptionBuffer(ce));
    	}
    }

    public Boolean getIsInheritable()
    {
    	if(this.isInheritable != null)
    		return this.isInheritable;
    		
        return this.availableServiceBindingVO.getIsInheritable();
    }
        
    public void setIsInheritable(Boolean isInheritable)
    {
    	try
    	{
        	this.availableServiceBindingVO.setIsInheritable(isInheritable);
    	}
    	catch(ConstraintException ce)
    	{
    		this.isUserEditable = isInheritable;
    		this.ceb.add(new ConstraintExceptionBuffer(ce));
    	}
    }


    public String doExecute() throws Exception
    {
		ceb.add(this.availableServiceBindingVO.validate());
    	ceb.throwIfNotEmpty();				
    	
	    this.availableServiceBindingVO = AvailableServiceBindingController.getController().create(this.availableServiceBindingVO);
        return "success";
    }
        
    public String doInput() throws Exception
    {
    	return "input";
    }    
}
