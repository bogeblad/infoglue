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

package org.infoglue.cms.providers;

import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.RegistryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * The default implementation checks first if the content-type has a fixed page assigned for this purpose and returns that page if that is the case.
 * If not it looks into if the content has any direct bindings anywhere on the site (component bindings that is) and returns the first of those pages.
 */

public class DefaultContentDetailPageResolver implements ContentDetailPageResolver
{
    private final static Logger logger = Logger.getLogger(DefaultContentDetailPageResolver.class.getName());

	public SiteNodeVO getDetailSiteNodeVO(InfoGluePrincipal principal, Integer contentId, String resolverData, Database db) throws Exception
	{
		SiteNodeVO detailSiteNodeVO = null;
		
		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId, db);
		
		if(resolverData != null && !resolverData.equals(""))
		{
			logger.info("resolverData:" + resolverData);
			try
			{
				Integer detailSiteNodeId = new Integer(resolverData);
				detailSiteNodeVO = SiteNodeController.getController().getSmallSiteNodeVOWithId(detailSiteNodeId, db);
			}
			catch (Exception e) 
			{
				logger.warn("Error getting sitenode based on resolverData: " + resolverData + ". Error: " + e.getMessage());
			}
		}
		
		if(detailSiteNodeVO == null)
		{
			List<SiteNodeVO> referencingSiteNodeVOList = RegistryController.getController().getReferencingSiteNodes(contentVO.getId(), 50, db);
			if(referencingSiteNodeVOList.size() == 1)
			{
				detailSiteNodeVO = referencingSiteNodeVOList.get(0);
			}
		}
		
		return detailSiteNodeVO;
	}

	@Override
	public Boolean usesSiteNodeSelectionDialog() 
	{
		return true;
	}

	@Override
	public String getName() 
	{
		return "Standard detail page resolver";
	}

	@Override
	public String getDescription() 
	{
		return "This resolver first checks if you have set a detail page for this content-type hard. If not it looks into the registry for any page binding directly to this content and uses the first one";
	}
}
