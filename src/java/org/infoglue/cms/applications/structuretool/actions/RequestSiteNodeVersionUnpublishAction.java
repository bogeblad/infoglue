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

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.EventController;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * This action adds the given siteNodeVersion to the event-list and thereby notifying the 
 * editor.
 */

public class RequestSiteNodeVersionUnpublishAction extends InfoGlueAbstractAction
{

   	private ConstraintExceptionBuffer ceb;
	private EventVO eventVO;  
	private Integer repositoryId;
	private Integer siteNodeId;
		  
  	public RequestSiteNodeVersionUnpublishAction()
	{
		this(new EventVO());
	}
	
	public RequestSiteNodeVersionUnpublishAction(EventVO eventVO)
	{
		this.eventVO = eventVO;
		this.ceb = new ConstraintExceptionBuffer();			
	}	

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}

	public void setSiteNodeId(Integer siteNodeId)
	{
		this.siteNodeId = siteNodeId;
	}

	public Integer getSiteNodeId()
	{
		return this.siteNodeId;
	}

    public void setName(String name)
    {
        this.eventVO.setName(name);
    }

    public void setDescription(String description)
    {
        this.eventVO.setDescription(description);
    }

    public String getName()
    {
        return this.eventVO.getName();
    }
    
    public void setEntityClass(String entityClass)
    {
        this.eventVO.setEntityClass(entityClass);
    }

	public void setEntityId(Integer entityId)
	{
		this.eventVO.setEntityId(entityId);
	}

	public void setTypeId(Integer typeId)
	{
		this.eventVO.setTypeId(typeId);
	}
      
    public String doExecute() throws Exception
    {
    	ceb = this.eventVO.validate();
    	ceb.throwIfNotEmpty();
    	
		EventController.create(this.eventVO, this.repositoryId, this.getInfoGluePrincipal());  	
    	
        return "success";
    }
        
}
