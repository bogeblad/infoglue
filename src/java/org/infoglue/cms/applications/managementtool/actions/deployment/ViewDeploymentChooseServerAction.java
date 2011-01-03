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

package org.infoglue.cms.applications.managementtool.actions.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.util.CmsPropertyHandler;

public class ViewDeploymentChooseServerAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
		
	private Map<String, DeploymentServerBean> deploymentServers = new HashMap<String, DeploymentServerBean>();
	private String deploymentServerName = null;
	private String synchronizationMethod = "pull";

	//Variables used by the quick deploy feature
	private Integer contentId;
	
	public String doInputQuickV3() throws Exception
    {
    	this.deploymentServers = CmsPropertyHandler.getDeploymentServers();
    	
    	return "inputQuickV3";
    }

    public String doInput() throws Exception
    {
    	this.deploymentServers = CmsPropertyHandler.getDeploymentServers();
    	
    	return "input";
    }

    public String doExecute() throws Exception
    {
    	return "success";
    }

	public Map<String, DeploymentServerBean> getDeploymentServers()
	{
		return deploymentServers;
	}

	public String getDeploymentServerName()
	{
		return deploymentServerName;
	}

	public void setDeploymentServerName(String deploymentServerName)
	{
		this.deploymentServerName = deploymentServerName;
	}

	public String getSynchronizationMethod()
	{
		return synchronizationMethod;
	}

	public void setSynchronizationMethod(String synchronizationMethod)
	{
		this.synchronizationMethod = synchronizationMethod;
	}
        
	public Integer getContentId() 
    {
		return contentId;
	}

	public void setContentId(Integer contentId) 
	{
		this.contentId = contentId;
	}

}
