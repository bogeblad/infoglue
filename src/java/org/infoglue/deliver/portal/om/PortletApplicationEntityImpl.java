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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.entity.PortletEntityList;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;

/**
 * @author jand
 *  
 */
public class PortletApplicationEntityImpl implements PortletApplicationEntity {
    private static final Log LOG = LogFactory.getLog(PortletApplicationEntityImpl.class);

    private String warName;
    private List entities = new ArrayList();
    private PortletApplicationDefinition definition;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.om.entity.PortletApplicationEntity#getId()
     */
    public ObjectID getId() {
        return org.apache.pluto.portalImpl.util.ObjectID.createFromString(warName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.om.entity.PortletApplicationEntity#getPortletEntityList()
     */
    public PortletEntityList getPortletEntityList() {
        return new PortletEntityListImpl(entities);
    }

    public void addPortletEntity(PortletEntity e) {
        entities.add(e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.om.entity.PortletApplicationEntity#getPortletApplicationDefinition()
     */
    public PortletApplicationDefinition getPortletApplicationDefinition() {
        return definition;
    }

    public void setPortletApplicationDefinition(PortletApplicationDefinition definition) {
        this.definition = definition;
    }

    public void setId(String id) {
        this.warName = id;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("PortletApplicationEntityImpl[ id:");
        buffer.append(this.warName);
        buffer.append(" entities:");
        buffer.append(this.entities);
        buffer.append("]");

        return buffer.toString();
    }
}