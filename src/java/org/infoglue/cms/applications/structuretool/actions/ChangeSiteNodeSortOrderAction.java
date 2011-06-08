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
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeControllerProxy;

/**
 * This action changes the sort order in the structure tree by moving a node up or down.
 */

public class ChangeSiteNodeSortOrderAction extends InfoGlueAbstractAction
{
    private Integer siteNodeId;
    private Integer beforeSiteNodeId; //If used overrides direction
    private String direction;

    public String doExecute() throws Exception
    {		
		SiteNodeControllerProxy.getSiteNodeControllerProxy().acChangeSiteNodeSortOrder(this.getInfoGluePrincipal(), siteNodeId, beforeSiteNodeId, direction);

		return "success";
    }

    public String doToggleHidden() throws Exception
    {		
    	try
    	{
    		SiteNodeControllerProxy.getSiteNodeControllerProxy().acToggleHidden(this.getInfoGluePrincipal(), siteNodeId);
    	}
    	catch (Exception e) 
    	{
    		e.printStackTrace();
		}
    	
		return "success";
    }

	public void setSiteNodeId(Integer siteNodeId)
	{
		this.siteNodeId = siteNodeId;
	}

	public void setDirection(String direction)
	{
		this.direction = direction;
	}

	public void setBeforeSiteNodeId(Integer beforeSiteNodeId)
	{
		this.beforeSiteNodeId = beforeSiteNodeId;
	}

}
