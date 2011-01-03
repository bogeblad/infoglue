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

package org.infoglue.cms.applications.publishingtool.actions;

import java.util.List;

import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.entities.publishing.PublicationVO;

public class DeleteEditionAction extends ViewPublicationsAction
{

	private PublicationVO publicationVO;
    private Integer repositoryId;
    private Integer publicationId;
    private List problematicPublicationDetails;

	public List getProblematicPublicationDetails()
	{
		return this.problematicPublicationDetails;
	}

	public void setPublicationId(Integer publicationId)
	{
		this.publicationId = publicationId;
	}

	public Integer getPublicationId()
	{
		return this.publicationId;
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}
	
	/**
	 * This method checks if some of the versions has later working-versions and if so we show another dialog 
	 * asking the editor to confirm what to do with the interfering version. If there are no interfering versions
	 * we unpublish all.
	 */
	
	public String doCheckForModifiedVersions() throws Exception
	{
		return doExecute();
	}

	/**
	 * This method checks if some of the versions has later working-versions and if so we show another dialog 
	 * asking the editor to confirm what to do with the interfering version. If there are no interfering versions
	 * we unpublish all.
	 */
	
	public String doV3() throws Exception
	{
		doExecute();
		
		 return "successV3";
	}

	/**
	 * This method checks if some of the versions has later working-versions and if so we show another dialog 
	 * asking the editor to confirm what to do with the interfering version. If there are no interfering versions
	 * we unpublish all.
	 */

    public String doExecute() throws Exception
    {
		// Editions to delete (support many, for now template allows only one)
        if(publicationId != null)
		{
			this.publicationVO = PublicationController.unPublish(publicationId, this.getInfoGluePrincipal());
		}
        
        return "success";
    }
        
}
