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

import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AvailableServiceBindingController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeTypeDefinitionController;
import org.infoglue.cms.entities.management.SiteNodeTypeDefinitionVO;

public class ViewSiteNodeTypeDefinitionAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;

    private SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO;
    private List assignedAvailableServiceBindingVOList;
    private List allAvailableServiceBindingVOList;

    public ViewSiteNodeTypeDefinitionAction()
    {
        this(new SiteNodeTypeDefinitionVO());
    }
    
    public ViewSiteNodeTypeDefinitionAction(SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO)
    {
        this.siteNodeTypeDefinitionVO = siteNodeTypeDefinitionVO;
    }
    
    
    protected void initialize(Integer siteNodeTypeDefinitionId) throws Exception
    {
        siteNodeTypeDefinitionVO = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionVOWithId(siteNodeTypeDefinitionId);
        assignedAvailableServiceBindingVOList = AvailableServiceBindingController.getController().getAssignedAvailableServiceBindings(siteNodeTypeDefinitionId);
        allAvailableServiceBindingVOList = AvailableServiceBindingController.getController().getAvailableServiceBindingVOList();
    } 

    public String doExecute() throws Exception
    {
        this.initialize(getSiteNodeTypeDefinitionId());
        return "success";
    }
        
    public java.lang.Integer getSiteNodeTypeDefinitionId()
    {
        return this.siteNodeTypeDefinitionVO.getSiteNodeTypeDefinitionId();
    }
        
    public void setSiteNodeTypeDefinitionId(java.lang.Integer siteNodeTypeDefinitionId)
    {
        this.siteNodeTypeDefinitionVO.setSiteNodeTypeDefinitionId(siteNodeTypeDefinitionId);
    }
    
    public java.lang.String getName()
    {
        return this.siteNodeTypeDefinitionVO.getName();
    }
     
    public java.lang.String getDescription()
    {
        return this.siteNodeTypeDefinitionVO.getDescription();
    }
    
    public java.lang.String getInvokerClassName()
    {
        return this.siteNodeTypeDefinitionVO.getInvokerClassName();
    }    
    
    public List getAllAvailableServiceBindings()
    {
    	return this.allAvailableServiceBindingVOList;
    }

    public List getAssignedAvailableServiceBindings()
    {
    	return this.assignedAvailableServiceBindingVOList;
    }

}
