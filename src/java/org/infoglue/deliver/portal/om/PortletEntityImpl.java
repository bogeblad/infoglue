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
package org.infoglue.deliver.portal.om;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.common.PreferenceSet;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.window.PortletWindowList;
import org.apache.pluto.portalImpl.services.portletdefinitionregistry.PortletDefinitionRegistry;

/**
 * @author jand
 *  
 */
public class PortletEntityImpl implements PortletEntity {
    private static final Log log = LogFactory.getLog(PortletEntityImpl.class);

    private String portletName;
    private PortletApplicationEntity applicationEntity;
    private ArrayList preferences = new ArrayList();

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.om.entity.PortletEntity#getId()
     */
    public ObjectID getId() {
        return org.apache.pluto.portalImpl.util.ObjectID.createFromString(portletName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.om.entity.PortletEntity#getPreferenceSet()
     */
    public PreferenceSet getPreferenceSet() {
        return new PreferenceSetImpl(preferences);
    }

    public void setPreferenceSet(PreferenceSet p) {
        this.preferences = new ArrayList();
        for (Iterator it = p.iterator(); it.hasNext();) {
            preferences.add(it.next());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.om.entity.PortletEntity#getPortletDefinition()
     */
    public PortletDefinition getPortletDefinition() {
        PortletDefinition def = PortletDefinitionRegistry.getPortletDefinition(getId());
        if (def == null) {
            log.error("Failed to lookup portlet-definition: " + portletName);
            log.debug("Available portlet-definitions: ");
            Iterator iterator =
                PortletDefinitionRegistry.getPortletApplicationDefinitionList().iterator();
            while (iterator.hasNext()) {
                PortletApplicationDefinition papp = (PortletApplicationDefinition) iterator.next();

                // fill portletsKeyObjectId
                Iterator portlets = papp.getPortletDefinitionList().iterator();
                while (portlets.hasNext()) {
                    PortletDefinition portlet = (PortletDefinition) portlets.next();
                    log.debug("Ok: " + portlet.getId());
                }

            }
        }
        return def;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.om.entity.PortletEntity#getPortletApplicationEntity()
     */
    public PortletApplicationEntity getPortletApplicationEntity() {
        return applicationEntity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.om.entity.PortletEntity#getPortletWindowList()
     */
    public PortletWindowList getPortletWindowList() {
        return new PortletWindowListImpl();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.om.entity.PortletEntity#getDescription(java.util.Locale)
     */
    public Description getDescription(Locale locale) {
        return getPortletDefinition().getDescription(locale);
    }

    /**
     * @param portletApplicationEntity
     *                   The portletApplicationEntity to set.
     */
    public void setPortletApplicationEntity(PortletApplicationEntity applicationEntity) {
        this.applicationEntity = applicationEntity;
    }

    /**
     * @param id The id to set.
     */
    public void setId(String id) {
        this.portletName = id;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("PortletEntityImpl[ id:");
        buffer.append(this.portletName.toString());
        buffer.append(" preferences:");
        buffer.append(this.preferences);
        buffer.append("]");

        return buffer.toString();
    }
}