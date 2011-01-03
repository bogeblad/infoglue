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
package org.infoglue.deliver.applications.actions;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.TransactionAbortedException;
import org.exolab.castor.jdo.TransactionNotInProgressException;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.PortletAssetController;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.portal.deploy.Deploy;
import org.infoglue.deliver.portal.dispatcher.PortalServletDispatcher;
import org.infoglue.deliver.util.CacheController;

/**
 * Deploy a portlet in the servlet container. Requires that the 'portletBase'
 * property points to where the .war-file should be deployed (placed), e.g. 
 * TOMCAT/webapps
 * 
 * @author jand
 *  
 */
public class DeployPortletAction extends InfoGlueAbstractAction {
    private static final Log log = LogFactory.getLog(DeployPortletAction.class);

    // TODO fixme;
    private static final String PORTLET_BASE = CmsPropertyHandler.getPortletBase();

    private Integer digitalAssetId;

    /*
     * (non-Javadoc)
     * 
     * @see org.infoglue.cms.applications.common.actions.WebworkAbstractAction#doExecute()
     */
    protected String doExecute() throws Exception 
    {
        log.debug("Deploying portlet, digitalAssetId=" + digitalAssetId);

        Database db = CastorDatabaseService.getDatabase();

        try
		{
			db.begin();

			DigitalAsset da = PortletAssetController.getDigitalAssetWithId(digitalAssetId, db);
			if (da == null)
			{
				return "error";
			}

			String webappsDir = PORTLET_BASE;
			if (webappsDir == null || webappsDir.length() == 0)
			{
				String tomcat_home = System.getProperty("CATALINA_HOME");
				if (tomcat_home == null)
				{
					tomcat_home = System.getProperty("TOMCAT_HOME");
				}
				if (tomcat_home != null)
				{
					webappsDir = new File(tomcat_home, "webapps").getAbsolutePath();
				}
			}

			String containerName = (String) getRequest().getAttribute(PortalServletDispatcher.PORTLET_CONTAINER_NAME);

			// Deploy portlet
			String warName = da.getAssetFileName();
			log.info("Deploying portlet " + warName + " at " + webappsDir);
			InputStream is = da.getAssetBlob();
			boolean deployed = Deploy.deployArchive(webappsDir, warName, is, containerName);
			is.close();
			if (deployed)
			{
				log.debug("Successful portlet deployment!");
			} 
			else
			{
				log.debug("Portlet already deployed!");
			}
			
			CacheController.clearPortlets();
		}
        catch(Exception e)
        {
        	log.error("An error occurred when deployin portlet:" + e.getMessage(), e);
        }
        finally
        {
	        try
			{
				db.commit();
				db.close();
			} 
	        catch (Exception e)
			{
				e.printStackTrace();
			} 
        }
        
        return "success";
    }

    /**
     * @return
     */
    public Integer getDigitalAssetId() {
        return digitalAssetId;
    }

    /**
     * @param integer
     */
    public void setDigitalAssetId(Integer integer) {
        digitalAssetId = integer;
    }

}