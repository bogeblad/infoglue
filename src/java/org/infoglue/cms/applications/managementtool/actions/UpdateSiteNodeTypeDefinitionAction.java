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

import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeTypeDefinitionController;
import org.infoglue.cms.entities.management.SiteNodeTypeDefinitionVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;


/**
  * This is the action-class for UpdateSiteNodeTypeDefinition
  * 
  * @author Mattias Bogeblad
  */
public class UpdateSiteNodeTypeDefinitionAction extends ViewSiteNodeTypeDefinitionAction //WebworkAbstractAction
{
	
	private SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO;
	private Integer siteNodeTypeDefinitionId;
	private String name;
	private String description;
	private String invokerClassName;
	private ConstraintExceptionBuffer ceb;
	
	public UpdateSiteNodeTypeDefinitionAction()
	{
		this(new SiteNodeTypeDefinitionVO());
	}
	
	public UpdateSiteNodeTypeDefinitionAction(SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO)
	{
		this.siteNodeTypeDefinitionVO = siteNodeTypeDefinitionVO;
		this.ceb = new ConstraintExceptionBuffer();	
	}
	
	public String doExecute() throws Exception
    {
		super.initialize(getSiteNodeTypeDefinitionId());

    	ceb.add(this.siteNodeTypeDefinitionVO.validate());
    	ceb.throwIfNotEmpty();		
    	
    	String[] values = getRequest().getParameterValues("availableServiceBindingId");
		
		SiteNodeTypeDefinitionController.getController().update(this.siteNodeTypeDefinitionVO, values);		
		
		return "success";
	}

	public String doSaveAndExit() throws Exception
    {
		doExecute();
						
		return "saveAndExit";
	}

	public void setSiteNodeTypeDefinitionId(Integer siteNodeTypeDefinitionId)
	{
		this.siteNodeTypeDefinitionVO.setSiteNodeTypeDefinitionId(siteNodeTypeDefinitionId);	
	}

    public java.lang.Integer getSiteNodeTypeDefinitionId()
    {
        return this.siteNodeTypeDefinitionVO.getSiteNodeTypeDefinitionId();
    }
        
    public java.lang.String getName()
    {
		if(this.name != null)
    		return this.name;
    
    	return this.siteNodeTypeDefinitionVO.getName();
    }
        
    public void setName(java.lang.String name)
    {
    	try
    	{
        	this.siteNodeTypeDefinitionVO.setName(name);
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
    		
        return this.siteNodeTypeDefinitionVO.getDescription();
    }
        
    public void setDescription(String description)
    {
    	try
    	{
        	this.siteNodeTypeDefinitionVO.setDescription(description);
    	}
    	catch(ConstraintException ce)
    	{
    		this.description = description;
    		this.ceb.add(new ConstraintExceptionBuffer(ce));
    	}
    }
    
    public String getInvokerClassName()
    {
    	if(this.invokerClassName != null)
    		return this.invokerClassName;
    		
        return this.siteNodeTypeDefinitionVO.getInvokerClassName();
    }
        
    public void setInvokerClassName(String invokerClassName)
    {
    	try
    	{
        	this.siteNodeTypeDefinitionVO.setInvokerClassName(invokerClassName);
    	}
    	catch(ConstraintException ce)
    	{
    		this.invokerClassName = invokerClassName;
    		this.ceb.add(new ConstraintExceptionBuffer(ce));
    	}
    }
}
