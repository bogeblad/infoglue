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
/*
 * Created on 2005-mar-10
 */
package org.infoglue.deliver.portal.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletApplicationEntityList;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistryService;
import org.apache.pluto.portalImpl.util.Properties;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.PortletAssetController;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.portal.OmBuilder;
import org.infoglue.deliver.portal.OmBuilderXStreamImpl;
import org.infoglue.deliver.portal.om.PortletApplicationEntityListImpl;

/**
 * Infoglue native portlet entity registry service. The registry is
 * loaded/stored in the infoglue database as a digital asset named
 * "portletentityregistry.xml".
 * 
 * @author jand
 */
public class PortletEntityRegistryServiceDBImpl extends PortletEntityRegistryService 
{
    private static final Log LOG = LogFactory.getLog(PortletEntityRegistryServiceDBImpl.class);

    public static final String PORTLET_REGISTRY_CONTENT_NAME = "portletentityregistry.xml";

    private ServletContext aContext;

    private OmBuilder builder = new OmBuilderXStreamImpl();

    private PortletApplicationEntityListImpl applications;

    private boolean needRefresh = true;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistryService#getPortletApplicationEntityList()
     */
    public PortletApplicationEntityList getPortletApplicationEntityList() {
        if (needRefresh) {
            try {
                load();
            } catch (IOException e) {
                LOG.error("Failed to load PortletEntityRegistry", e);
            }
        }
        return applications;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistryService#getPortletEntity(org.apache.pluto.om.common.ObjectID)
     */
    public PortletEntity getPortletEntity(ObjectID id) {
        if (needRefresh) {
            try {
                load();
            } catch (IOException e) {
                LOG.error("Failed to load PortletEntityRegistry", e);
                return null;
            }
        }

        String oid = id.toString();
        int dot = oid.lastIndexOf(".");
        if (dot < 0) {
            LOG.warn("ID does not contain '.' to separate application- and portlet-id: " + id);
            return null;
        }

        ObjectID appID = org.apache.pluto.portalImpl.util.ObjectID.createFromString(oid.substring(
                0, dot));

        PortletApplicationEntity appEntity = applications.get(appID);
        if (appEntity == null) {
            LOG.warn("Application not found: " + appID);
            LOG.warn(toString());
            return null;
        }
        PortletEntity portletEntity = appEntity.getPortletEntityList().get(id);
        if (portletEntity == null) {
            LOG.warn("Portlet not found: " + id);
            LOG.warn(toString());
        }

        return portletEntity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistryService#store()
     */
    public void store() throws IOException {
        String xml = builder.toXML(applications);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Storing PortletEntityRegistry...\n" + xml);
        }

        try {
            Database db = CastorDatabaseService.getDatabase();
            db.begin();

            byte[] serial = xml.getBytes();
            InputStream is = new ByteArrayInputStream(serial);

            DigitalAsset da = getPortletRegistry();
            if (da == null) {
                LOG.info("Creating new " + PORTLET_REGISTRY_CONTENT_NAME);

                String filePath = CmsPropertyHandler.getDigitalAssetPath();
                DigitalAssetVO newAsset = new DigitalAssetVO();
                newAsset.setAssetContentType("text/xml");
                newAsset.setAssetKey(PORTLET_REGISTRY_CONTENT_NAME);
                newAsset.setAssetFileName(PORTLET_REGISTRY_CONTENT_NAME);
                newAsset.setAssetFilePath(filePath);
                newAsset.setAssetFileSize(new Integer(serial.length));

                da = PortletAssetController.create(newAsset, is);
                LOG.warn(PORTLET_REGISTRY_CONTENT_NAME + " stored as id=" + da.getId());
            } else {
                LOG.info("Updating " + PORTLET_REGISTRY_CONTENT_NAME);

                DigitalAssetVO daVO = da.getValueObject();
                daVO.setAssetFileSize(new Integer(serial.length));

                PortletAssetController.update(daVO, is);
            }
            is.close();

            db.commit();
            db.close();
            LOG.debug("Stored PortletEntityRegistry successfully");
        } catch (Throwable e) {
            LOG.error("Failed to store PortletEntityRegistry", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistryService#load()
     */
    public void load() throws IOException {
        LOG.warn("Loading PortletEntityRegistry start...");
        try {
            DigitalAsset da = getPortletRegistry();
            if (da == null) {
                applications = new PortletApplicationEntityListImpl();
            } else {
                InputStream is = da.getAssetBlob();
                applications = builder.getPortletApplicationEntityList(is);
                is.close();
            }
            needRefresh = false;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Applications: " + toString());
            }
        } catch (Throwable e) {
            LOG.error("Failed to load PortletEntityRegistry", e);
        }
        LOG.warn("Loaded PortletEntityRegistry done...");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistryService#refresh(org.apache.pluto.om.entity.PortletEntity)
     */
    public void refresh(PortletEntity entity) {
        // TODO Auto-generated method stub
    }

    public String toString() {
        return builder.toXML(getPortletApplicationEntityList());
    }

    private DigitalAsset getPortletRegistry() throws Exception {
        List das = PortletAssetController.getDigitalAssetByName(PORTLET_REGISTRY_CONTENT_NAME);
        if (das != null && das.size() > 0) {
            DigitalAsset da = (DigitalAsset) das.get(0);
            LOG.debug("Registry located as id=" + da.getId());
            return da;
        } else {
            LOG.info("Portlet Registry not found");
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.portalImpl.services.Service#init(javax.servlet.ServletConfig,
     *      org.apache.pluto.portalImpl.util.Properties)
     */
    protected void init(ServletConfig conf, Properties props) throws Exception {
        LOG.debug("Calling init()");
        super.init(conf, props);
        needRefresh = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.portalImpl.services.Service#postInit(javax.servlet.ServletConfig)
     */
    protected void postInit(ServletConfig conf) throws Exception {
        LOG.debug("Calling postInit()");
        super.postInit(conf);
        needRefresh = true;
    }
}