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
 *
 * $Id: SiteNodeVersionControllerTest.java,v 1.4 2007/10/22 14:35:20 mattias Exp $
 */
package org.infoglue.cms.controllers;

import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.DateHelper;
import org.infoglue.cms.util.InfoGlueTestCase;

public class SiteNodeVersionControllerTest extends InfoGlueTestCase
{


	public void testSiteNodeVersionModifiedDate(InfoGluePrincipal infoGluePrincipal) throws Exception
	{
	    Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        db.begin();

        try
        {
            testSiteNode(infoGluePrincipal, new Integer(100591), db);
    		/*
            
            SiteNodeVersion latestSiteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(new Integer(100569), db);
			latestSiteNodeVersion.setVersionComment("" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(Calendar.MINUTE) + ":" + Calendar.getInstance().get(Calendar.SECOND));
			
			latestSiteNodeVersion.setModifiedDateTime(DateHelper.getSecondPreciseDate());
			*/
            db.commit();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            try
            {
                db.rollback();
            } 
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
        finally
        {
            db.close();
        }
    }

	private void testSiteNode(InfoGluePrincipal principal, Integer siteNodeId, Database db) throws ConstraintException, SystemException, Exception
	{
        SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithId(siteNodeId, db);
        siteNode.getValueObject().setName(siteNode.getValueObject().getName().substring(0,10) + System.currentTimeMillis());

		SiteNodeVersionVO latestSiteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersion(db, siteNode.getSiteNodeId(), false).getValueObject();
		latestSiteNodeVersionVO.setContentType("text/html");
		System.out.println("PageKey:" + latestSiteNodeVersionVO.getPageCacheKey());
		latestSiteNodeVersionVO.setPageCacheKey("");
		latestSiteNodeVersionVO.setPageCacheTimeout(null);
		latestSiteNodeVersionVO.setDisableEditOnSight(new Integer(2));
		latestSiteNodeVersionVO.setDisablePageCache(new Integer(2));
		latestSiteNodeVersionVO.setDisableLanguages(new Integer(2));
		latestSiteNodeVersionVO.setIsProtected(new Integer(2));
		latestSiteNodeVersionVO.setVersionModifier(principal.getName());
		latestSiteNodeVersionVO.setModifiedDateTime(DateHelper.getSecondPreciseDate());
		
		SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().acUpdate(principal, latestSiteNodeVersionVO, db);

	}
}
