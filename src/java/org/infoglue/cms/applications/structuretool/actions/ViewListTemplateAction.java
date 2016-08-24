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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AvailableServiceBindingController;
import org.infoglue.cms.controllers.kernel.impl.simple.ServiceDefinitionController;
import org.infoglue.cms.entities.management.ServiceDefinitionVO;
import org.infoglue.cms.entities.structure.ServiceBindingVO;
import org.infoglue.cms.services.BaseService;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * This action represents the CreateSiteNode Usecase.
 */

public class ViewListTemplateAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewListTemplateAction.class.getName());

	private static final long serialVersionUID = 1L;

    private Integer siteNodeVersionId;
    private Integer repositoryId;
    private Integer availableServiceBindingId;
    private Integer serviceDefinitionId;
    private Integer bindingTypeId;
    private ConstraintExceptionBuffer ceb;
   	private Integer siteNodeId;
   	private ServiceDefinitionVO singleServiceDefinitionVO;
   	private String qualifyerXML;
   	
   	private ServiceBindingVO serviceBindingVO = null;
   
  
  	public ViewListTemplateAction()
	{
		this(new ServiceBindingVO());
	}
	
	public ViewListTemplateAction(ServiceBindingVO serviceBindingVO)
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

	public void setBindingTypeId(Integer bindingTypeId)
	{
		this.serviceBindingVO.setBindingTypeId(bindingTypeId);
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
	 * This method returns the contents that are of contentTypeDefinition "HTMLTemplate"
	 */
	public List getTemplates()
	{
		HashMap arguments = new HashMap();
		arguments.put("method", "selectListOnContentTypeName");
		
		List argumentList = new ArrayList();
		HashMap argument = new HashMap();
		argument.put("contentTypeDefinitionName", "HTMLTemplate");
		argumentList.add(argument);
		arguments.put("arguments", argumentList);
		
		return queryServiceDefinition(arguments, this.singleServiceDefinitionVO.getServiceDefinitionId());
	}
	     
     
    /**
     * This method instansiates and queries a serviceDefinition.
     */
    private List queryServiceDefinition(HashMap arguments, Integer serviceDefinitionId)
    {
    	List response = null;
    	
    	try
    	{
    		ServiceDefinitionVO serviceDefinitionVO = ServiceDefinitionController.getController().getServiceDefinitionVOWithId(serviceDefinitionId);
			String serviceDefinitionClassName = serviceDefinitionVO.getClassName();
			logger.info("serviceDefinitionClassName:" + serviceDefinitionClassName);
			
			BaseService service = (BaseService)Class.forName(serviceDefinitionClassName).newInstance();
    		response = service.selectMatchingEntities(arguments);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	return response;
    }
     
     
    /**
     * This method creates the new serviceBinding.
     */
     
    public String doExecute() throws Exception
    {
    	List serviceDefinitions = AvailableServiceBindingController.getController().getServiceDefinitionVOList(this.availableServiceBindingId);
    	if(serviceDefinitions == null || serviceDefinitions.size() == 0)
    	{
	    	logger.info("Returning error");
    		//throw new SystemException();
	        return "error";
	    }
    	else if(serviceDefinitions.size() == 1)
    	{
	    	logger.info("Returning success");
	        this.singleServiceDefinitionVO = (ServiceDefinitionVO)serviceDefinitions.get(0);	    
	        return "success"; 
    	} 
    	else
    	{
	    	logger.info("Returning chooseService");
    		return "chooseService";
    	}
    }

        
}
