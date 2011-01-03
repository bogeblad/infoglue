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

package org.infoglue.cms.applications.structuretool.actions;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ServiceBindingController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.management.ServiceDefinitionVO;
import org.infoglue.cms.entities.structure.ServiceBindingVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;


/**
 * This action represents the CreateSiteNode Usecase.
 */

public class CreateServiceBindingAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(CreateServiceBindingAction.class.getName());

    private Integer siteNodeVersionId;
    private Integer repositoryId;
    private Integer availableServiceBindingId;
    //private Integer serviceBindingId;
    private Integer serviceDefinitionId;
    private Integer bindingTypeId;
    private ConstraintExceptionBuffer ceb;
   	private Integer siteNodeId;
   	private ServiceDefinitionVO singleServiceDefinitionVO;
   	private String qualifyerXML;
   	
   	private ServiceBindingVO serviceBindingVO = null;
   
  
  	public CreateServiceBindingAction()
	{
		this(new ServiceBindingVO());
	}
	
	public CreateServiceBindingAction(ServiceBindingVO serviceBindingVO)
	{
		this.serviceBindingVO = serviceBindingVO;
		this.ceb = new ConstraintExceptionBuffer();			
	}	

	public void setSiteNodeVersionId(Integer siteNodeVersionId)
	{
		this.siteNodeVersionId = siteNodeVersionId;
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public void setAvailableServiceBindingId(Integer availableServiceBindingId)
	{
		this.availableServiceBindingId = availableServiceBindingId;
	}

	public void setServiceDefinitionId(Integer serviceDefinitionId)
	{
		this.serviceDefinitionId = serviceDefinitionId;
	}

	public void setServiceBindingId(Integer serviceBindingId)
	{
		this.serviceBindingVO.setServiceBindingId(serviceBindingId);
	}

	public void setBindingTypeId(Integer bindingTypeId)
	{
		this.serviceBindingVO.setBindingTypeId(bindingTypeId);
	}

	public void setPath(String path)
	{
		this.serviceBindingVO.setPath(path);
	}
	
	public Integer getSiteNodeVersionId()
	{
		return this.siteNodeVersionId;
	}

	public Integer getSiteNodeId()
	{
		return this.siteNodeId;
	}
	    
	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}

	public Integer getAvailableServiceBindingId()
	{
		return this.availableServiceBindingId;
	}
    
	public Integer getServiceDefinitionId()
	{
		return this.singleServiceDefinitionVO.getServiceDefinitionId();
	}
	
	public Integer getBindingTypeId()
	{
		return this.bindingTypeId;
	}
 
	public ServiceDefinitionVO getSingleServiceDefinitionVO()
	{
		return this.singleServiceDefinitionVO;
	}

	public void setQualifyerXML(String qualifyerXML)
	{
		this.qualifyerXML = qualifyerXML;
	}
	
	public String getQualifyerXML()
	{
		return this.qualifyerXML;
	}
     
    /**
     * This method creates the new serviceBinding.
     */
     
    public String doExecute() throws Exception
    {
    	logger.info("-------------------------->" + this.serviceBindingVO.getServiceBindingId());
    	if(this.serviceBindingVO.getServiceBindingId() == null)
    	{
    		ServiceBindingController.create(this.serviceBindingVO, qualifyerXML, this.availableServiceBindingId, this.siteNodeVersionId, this.serviceDefinitionId);	
    	}
    	else
    	{
    		ServiceBindingController.update(this.serviceBindingVO, qualifyerXML);	
    	}
	 
	    this.siteNodeId = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(this.siteNodeVersionId).getSiteNodeId();
	    return "success";	
    }
        
}
