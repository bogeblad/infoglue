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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.entity.PortletApplicationEntity;

/**
 * @author jand
 *  
 */
public class PortletApplicationEntityListImpl implements
        org.apache.pluto.om.entity.PortletApplicationEntityList {
    private static final Log LOG = LogFactory.getLog(PortletApplicationEntityListImpl.class);

    private ArrayList applications;

    public PortletApplicationEntityListImpl() {
        this.applications = new ArrayList();
    }

    public PortletApplicationEntityListImpl(ArrayList applications) {
        this.applications = applications;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.om.entity.PortletApplicationEntityList#iterator()
     */
    public Iterator iterator() {
        return applications.iterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pluto.om.entity.PortletApplicationEntityList#get(org.apache.pluto.om.common.ObjectID)
     */
    public PortletApplicationEntity get(ObjectID id) {
        for (Iterator it = applications.iterator(); it.hasNext();) {
            PortletApplicationEntity pae = (PortletApplicationEntity) it.next();
            if (pae.getId().equals(id)) {
                return pae;
            }
        }
        return null;
    }

    public void add(PortletApplicationEntity e) {
        PortletApplicationEntity old = get(e.getId());
        if (old != null) {
            applications.remove(old);
        }
        applications.add(e);
    }

    public String toString() {
        return "PortletApplicationEntityListImpl[ applications:" + applications + "]";
    }
}