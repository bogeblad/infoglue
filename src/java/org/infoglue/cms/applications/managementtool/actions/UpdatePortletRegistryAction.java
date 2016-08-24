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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistry;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.PortletAssetController;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.cms.util.RemoteCacheUpdater;
import org.infoglue.deliver.portal.services.PortletEntityRegistryServiceDBImpl;

/**
 * @author Mattias Bogeblad
 */

public class UpdatePortletRegistryAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(UpdatePortletRegistryAction.class.getName());

	private String portletRegistry;
	
	public String doExecute() throws Exception
    {
		store();
		
		return "success";
	}

	public String doSaveAndExit() throws Exception
    {
		store();
		
		return "saveAndExit";
	}

    private void store() throws IOException 
    {
        logger.info("Storing PortletEntityRegistry...\n" + portletRegistry);
        
        try 
        {
            Database db = CastorDatabaseService.getDatabase();
            db.begin();

            byte[] serial = portletRegistry.getBytes();
            InputStream is = new ByteArrayInputStream(serial);

            DigitalAsset da = PortletAssetController.getPortletAssetController().getPortletRegistryAsset(db);
            if (da == null) 
            {
            	logger.info("Creating new " + PortletEntityRegistryServiceDBImpl.PORTLET_REGISTRY_CONTENT_NAME);

                String filePath = CmsPropertyHandler.getDigitalAssetPath();
                DigitalAssetVO newAsset = new DigitalAssetVO();
                newAsset.setAssetContentType("text/xml");
                newAsset.setAssetKey(PortletEntityRegistryServiceDBImpl.PORTLET_REGISTRY_CONTENT_NAME);
                newAsset.setAssetFileName(PortletEntityRegistryServiceDBImpl.PORTLET_REGISTRY_CONTENT_NAME);
                newAsset.setAssetFilePath(filePath);
                newAsset.setAssetFileSize(new Integer(serial.length));

                da = PortletAssetController.getPortletAssetController().create(db, newAsset, is);
                logger.warn(PortletEntityRegistryServiceDBImpl.PORTLET_REGISTRY_CONTENT_NAME + " stored as id=" + da.getId());
            } 
            else 
            {
                logger.info("Updating " + PortletEntityRegistryServiceDBImpl.PORTLET_REGISTRY_CONTENT_NAME);

                DigitalAssetVO daVO = da.getValueObject();
                daVO.setAssetFileSize(new Integer(serial.length));

                PortletAssetController.update(daVO, is);
            }
            
            is.close();

            PortletEntityRegistry.load();

    		NotificationMessage notificationMessage = new NotificationMessage("UpdatePortletRegistryAction.store():", "PortletRegistry", this.getInfoGluePrincipal().getName(), NotificationMessage.SYSTEM, "0", "PortletRegistry");
    		RemoteCacheUpdater.getSystemNotificationMessages().add(notificationMessage);

            db.commit();
            db.close();
            logger.debug("Stored PortletEntityRegistry successfully");
        } 
        catch (Throwable e) 
        {
            logger.error("Failed to store PortletEntityRegistry", e);
        }
    }

	
	public String getPortletRegistry() 
	{
		return portletRegistry;
	}

	public void setPortletRegistry(String portletRegistry) 
	{
		this.portletRegistry = portletRegistry;
	}
	

}
