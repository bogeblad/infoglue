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
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.management.RepositoryVO;

/**
 * This class implements the action class for the menu in the management tool.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewManagementToolMenuAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;

    private RepositoryVO repositoryVO;
    private Integer repositoryId;
	private String name;
    
    public void setRepositoryId(Integer repositoryId)
    {
    	this.repositoryId = repositoryId;
    }
    
    public Integer getRepositoryId()
    { 
    	return this.repositoryId;
    }
    
    private void setName(String name)
    {
    	this.name = name;	
    }
    
    public String getName()
    {
    	return this.name;	
    }
    
    public String doExecute() throws Exception
    {
    	if(this.repositoryId != null && this.repositoryId.intValue() > 0)
    	{
	    	this.repositoryVO = RepositoryController.getController().getRepositoryVOWithId(this.repositoryId);
	    	this.setName(this.repositoryVO.getName());
    	}
    	
        return "success";
    }

    public String doV3() throws Exception
    {
    	System.out.println("AAAAAAAAAA");
    	if(this.repositoryId != null && this.repositoryId.intValue() > 0)
    	{
	    	this.repositoryVO = RepositoryController.getController().getRepositoryVOWithId(this.repositoryId);
	    	this.setName(this.repositoryVO.getName());
    	}
    	
        return "successV3";
    }

}
