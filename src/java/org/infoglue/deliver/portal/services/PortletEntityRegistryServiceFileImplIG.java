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
package org.infoglue.deliver.portal.services;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletApplicationEntityList;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistryService;
import org.apache.pluto.portalImpl.util.Properties;
import org.infoglue.deliver.portal.OmBuilder;
import org.infoglue.deliver.portal.OmBuilderXStreamImpl;
import org.infoglue.deliver.portal.om.PortletApplicationEntityListImpl;

/**
 * @author joran
 *
 * @version $Revision: 1.4 $
 */
public class PortletEntityRegistryServiceFileImplIG extends PortletEntityRegistryService {
    private static final Log LOG = LogFactory.getLog(PortletEntityRegistryServiceFileImplIG.class);

    private ServletContext aContext;
    private String filename = "WEB-INF/data/portletentityregistryIG.xml";
    private OmBuilder builder = new OmBuilderXStreamImpl();
    private PortletApplicationEntityListImpl applications;

    /* (non-Javadoc)
     * @see org.apache.pluto.portalImpl.services.Service#init(javax.servlet.ServletContext, org.apache.pluto.portalImpl.util.Properties)
     */
    protected void init(ServletContext aContext, Properties aProperties) throws Exception {
        // TODO Auto-generated method stub
        super.init(aContext, aProperties);
        this.aContext = aContext;
        load();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistryService#getPortletApplicationEntityList()
     */
    public PortletApplicationEntityList getPortletApplicationEntityList() {
        return applications;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistryService#getPortletEntity(org.apache.pluto.om.common.ObjectID)
     */
    public PortletEntity getPortletEntity(ObjectID id) {
        String oid = id.toString();
        int dot = oid.lastIndexOf(".");
        if (dot < 0) {
            LOG.warn("ID does not contain '.' to separate application- and portlet-id: " + id);
            return null;
        }

        ObjectID appID =
            org.apache.pluto.portalImpl.util.ObjectID.createFromString(oid.substring(0, dot));

        PortletApplicationEntity appEntity = applications.get(appID);
        if (appEntity == null) {
            LOG.warn("Application not found: " + appID);
            return null;
        }
        PortletEntity portletEntity = appEntity.getPortletEntityList().get(id);
        if (portletEntity == null) {
            LOG.warn("Portlet not found: " + id);
        }

        return portletEntity;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistryService#store()
     */
    public void store() throws IOException {
        String xml = builder.toXML(applications);
        LOG.info(xml);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistryService#load()
     */
    public void load() throws IOException {
        LOG.info("Start building PortletApplicationList.....");
        InputStream is = aContext.getResourceAsStream(filename);
        if (is == null) {
            throw new IOException("Unable to find " + filename);
        }

        applications = builder.getPortletApplicationEntityList(is);
        is.close();
        LOG.info("Applications: " + applications);
        LOG.info("DONE!");
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistryService#refresh(org.apache.pluto.om.entity.PortletEntity)
     */
    public void refresh(PortletEntity portletEntity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();

    }

    public static void main(String[] args) {
        try {
            PortletEntityRegistryServiceFileImplIG reg =
                new PortletEntityRegistryServiceFileImplIG();
            reg.load();

            reg.store();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
