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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.ReferenceBean;
import org.infoglue.cms.controllers.kernel.impl.simple.RegistryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;

import webwork.action.Action;

public class ViewSiteNodeReferencesAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewSiteNodeReferencesAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private Integer siteNodeId = null;
	private List<ReferenceBean> referenceBeanList = new ArrayList<ReferenceBean>();
	private List<ReferenceBean> referencingBeanList = new ArrayList<ReferenceBean>();

    public String doExecute() throws Exception
    {
		this.referenceBeanList = RegistryController.getController().getReferencingObjectsForSiteNode(siteNodeId, 200, false, true);
		SiteNodeVersionVO latestSiteNodeVersion = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getLatestActiveSiteNodeVersionVO(siteNodeId);
	    this.referencingBeanList = RegistryController.getController().getReferencedObjects(SiteNodeVersion.class.getName(), latestSiteNodeVersion.getSiteNodeVersionId().toString());

        return Action.SUCCESS;
    }

    public Integer getSiteNodeId()
    {
        return this.siteNodeId;
    }
        
    public void setSiteNodeId(Integer siteNodeId)
    {
	    this.siteNodeId =  siteNodeId;
    }
	
    public List<ReferenceBean> getReferenceBeanList()
    {
        return referenceBeanList;
    }

    public List<ReferenceBean> getReferencingBeanList()
    {
        return referencingBeanList;
    }
}
