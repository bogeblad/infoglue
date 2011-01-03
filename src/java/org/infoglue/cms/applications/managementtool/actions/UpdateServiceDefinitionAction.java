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

import org.infoglue.cms.controllers.kernel.impl.simple.ServiceDefinitionController;
import org.infoglue.cms.entities.management.ServiceDefinitionVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;


/**
  * This is the action-class for UpdateServiceDefinition
  * 
  * @author Mattias Bogeblad
  */
public class UpdateServiceDefinitionAction extends ViewServiceDefinitionAction // WebworkAbstractAction
{
	
	private ServiceDefinitionVO serviceDefinitionVO;
	private Integer serviceDefinitionId;
	private String name;
	private String description;
	private String className;
	private ConstraintExceptionBuffer ceb;
	
	public UpdateServiceDefinitionAction()
	{
		this(new ServiceDefinitionVO());
	}
	
	public UpdateServiceDefinitionAction(ServiceDefinitionVO serviceDefinitionVO)
	{
		this.serviceDefinitionVO = serviceDefinitionVO;
		this.ceb = new ConstraintExceptionBuffer();	
	}
	
	public String doExecute() throws Exception
    {
		super.initialize(getServiceDefinitionId());

    	ceb.add(this.serviceDefinitionVO.validate());
    	ceb.throwIfNotEmpty();		
    	
		ServiceDefinitionController.getController().update(this.serviceDefinitionVO);		
		
		return "success";
	}

	public String doSaveAndExit() throws Exception
    {
		doExecute();
						
		return "saveAndExit";
	}

	public void setServiceDefinitionId(Integer serviceDefinitionId)
	{
		this.serviceDefinitionVO.setServiceDefinitionId(serviceDefinitionId);	
	}

    public java.lang.Integer getServiceDefinitionId()
    {
        return this.serviceDefinitionVO.getServiceDefinitionId();
    }
        
    public java.lang.String getName()
    {
		if(this.name != null)
    		return this.name;
    
    	return this.serviceDefinitionVO.getName();
    }
        
    public void setName(java.lang.String name)
    {
    	try
    	{
        	this.serviceDefinitionVO.setName(name);
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
    		
        return this.serviceDefinitionVO.getDescription();
    }
        
    public void setDescription(String description)
    {
    	try
    	{
        	this.serviceDefinitionVO.setDescription(description);
    	}
    	catch(ConstraintException ce)
    	{
    		this.description = description;
    		this.ceb.add(new ConstraintExceptionBuffer(ce));
    	}
    }
    
    public String getClassName()
    {
    	if(this.className != null)
    		return this.className;
    		
        return this.serviceDefinitionVO.getClassName();
    }
        
    public void setClassName(String className)
    {
    	try
    	{
        	this.serviceDefinitionVO.setClassName(className);
    	}
    	catch(ConstraintException ce)
    	{
    		this.className = className;
    		this.ceb.add(new ConstraintExceptionBuffer(ce));
    	}
    }
}
