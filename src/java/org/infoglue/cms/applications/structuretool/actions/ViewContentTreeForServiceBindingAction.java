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

import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AvailableServiceBindingController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.management.ServiceDefinitionVO;
import org.infoglue.cms.entities.structure.ServiceBindingVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * This action shows the Content-tree when binding stuff.
 */

public class ViewContentTreeForServiceBindingAction extends InfoGlueAbstractAction
{
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
	private String tree;	
	private List repositories;
	
   	private ServiceBindingVO serviceBindingVO = null;
   
  
  	public ViewContentTreeForServiceBindingAction()
	{
		this(new ServiceBindingVO());
	}
	
	public ViewContentTreeForServiceBindingAction(ServiceBindingVO serviceBindingVO)
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
     
	public String getTree()
	{
		return tree;
	}

	public void setTree(String string)
	{
		tree = string;
	}
	
	public String getCurrentAction()
	{
		return "ViewContentTreeForServiceBinding.action";
	}
	
    public String doExecute() throws Exception
    {
		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);
		
    	if(this.repositoryId == null)
    		this.repositoryId = RepositoryController.getController().getFirstRepositoryVO().getRepositoryId();
    	
    	List serviceDefinitions = AvailableServiceBindingController.getController().getServiceDefinitionVOList(this.availableServiceBindingId);
    	if(serviceDefinitions == null || serviceDefinitions.size() == 0)
    	{
	    	//throw new SystemException();
	        return "error";

	    }
    	else if(serviceDefinitions.size() == 1)
    	{
	        this.singleServiceDefinitionVO = (ServiceDefinitionVO)serviceDefinitions.get(0);	    
	        return "success";
    	}
    	else
    	{
    		return "chooseService";
    	}
    }
       
	public List getRepositories()
	{
		return repositories;
	}

}
