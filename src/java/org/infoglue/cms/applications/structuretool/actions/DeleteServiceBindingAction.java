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
import org.infoglue.cms.controllers.kernel.impl.simple.ServiceBindingController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.structure.ServiceBindingVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * This action represents the CreateSiteNode Usecase.
 */

public class DeleteServiceBindingAction extends InfoGlueAbstractAction
{

    private Integer siteNodeVersionId;
   	private Integer siteNodeId;
   	private ConstraintExceptionBuffer ceb;
   	
   	private ServiceBindingVO serviceBindingVO = null;
   
  
  	public DeleteServiceBindingAction()
	{
		this(new ServiceBindingVO());
	}
	
	public DeleteServiceBindingAction(ServiceBindingVO serviceBindingVO)
	{
		this.serviceBindingVO = serviceBindingVO;
		this.ceb = new ConstraintExceptionBuffer();			
	}	

	public void setSiteNodeVersionId(Integer siteNodeVersionId)
	{
		this.siteNodeVersionId = siteNodeVersionId;
	}

	public void setServiceBindingId(Integer serviceBindingId)
	{
		this.serviceBindingVO.setServiceBindingId(serviceBindingId);
	}
		
	public Integer getSiteNodeVersionId()
	{
		return this.siteNodeVersionId;
	}

	public Integer getSiteNodeId()
	{
		return this.siteNodeId;
	}
	         
    /**
     * This method deletes a serviceBinding.
     */
     
    public String doExecute() throws Exception
    {
    	ServiceBindingController.delete(this.serviceBindingVO);	
	    this.siteNodeId = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(this.siteNodeVersionId).getSiteNodeId();
	    return "success";	
    }
        
}
