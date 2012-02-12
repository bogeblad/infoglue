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
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.InconsistenciesController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;

/**
 * This class acts as a system tail on the logfiles available.
 * 
 * @author Mattias Bogeblad
 */

public class ViewInconsistenciesAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
	private List inconsistencies = null;
	private Integer registryId = null;
	
    public String doInput() throws Exception
    {
    	return "input";
    }

    public String doExecute() throws Exception
    {
    	inconsistencies = InconsistenciesController.getController().getAllInconsistencies();
        
    	return "success";
    }

    public String doRemoveReference() throws Exception
    {
    	InconsistenciesController.getController().removeReferences(registryId, this.getInfoGluePrincipal());
    	
    	inconsistencies = InconsistenciesController.getController().getAllInconsistencies();
        
    	return "success";
    }
    
    public List getInconsistencies() 
	{
		return inconsistencies;
	}
    
    public SiteNodeVO getSiteNodeVO(String siteNodeId) throws NumberFormatException, SystemException
    {
    	return SiteNodeController.getController().getSiteNodeVOWithId(new Integer(siteNodeId));
    }

    public ContentVO getContentVO(String contentId) throws NumberFormatException, SystemException
    {
    	return ContentController.getContentController().getContentVOWithId(new Integer(contentId));
    }

	public Integer getRegistryId() 
	{
		return registryId;
	}

	public void setRegistryId(Integer registryId) 
	{
		this.registryId = registryId;
	}
}
