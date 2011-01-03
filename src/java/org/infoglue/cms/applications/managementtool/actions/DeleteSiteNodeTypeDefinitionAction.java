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
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeTypeDefinitionController;
import org.infoglue.cms.entities.management.SiteNodeTypeDefinitionVO;
import org.infoglue.cms.exception.SystemException;

/**
 * This action removes a siteNodeTypeDefinition from the system.
 * 
 * @author Mattias Bogeblad
 */

public class DeleteSiteNodeTypeDefinitionAction extends InfoGlueAbstractAction
{
	private SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO;
	private Integer siteNodeTypeDefinitionId;
	
	public DeleteSiteNodeTypeDefinitionAction()
	{
		this(new SiteNodeTypeDefinitionVO());
	}

	public DeleteSiteNodeTypeDefinitionAction(SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO) {
		this.siteNodeTypeDefinitionVO = siteNodeTypeDefinitionVO;
	}
	
	protected String doExecute() throws Exception 
	{
		this.siteNodeTypeDefinitionVO.setSiteNodeTypeDefinitionId(this.getSiteNodeTypeDefinitionId());
		SiteNodeTypeDefinitionController.getController().delete(this.siteNodeTypeDefinitionVO);
		
		return "success";
	}
	
	public void setSiteNodeTypeDefinitionId(Integer siteNodeTypeDefinitionId) throws SystemException
	{
		this.siteNodeTypeDefinitionVO.setSiteNodeTypeDefinitionId(siteNodeTypeDefinitionId);	
	}

    public java.lang.Integer getSiteNodeTypeDefinitionId()
    {
        return this.siteNodeTypeDefinitionVO.getSiteNodeTypeDefinitionId();
    }
        
	
}
