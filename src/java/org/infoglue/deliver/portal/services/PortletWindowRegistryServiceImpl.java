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

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.naming.NameNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletApplicationEntityList;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.om.window.PortletWindowListCtrl;
import org.apache.pluto.portalImpl.services.Service;
import org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistry;
import org.apache.pluto.portalImpl.util.ObjectID;
import org.infoglue.deliver.portal.om.PortletWindowImpl;

/**
 * This is the registry service for PortletWindow:s.
 * 
 * @author jand
 *  
 */
public class PortletWindowRegistryServiceImpl extends Service implements PortletWindowRegistryService 
{
    private static final Log log = LogFactory.getLog(PortletWindowRegistryServiceImpl.class);

    /** Window-id to window */
    private static Map wid2win = Collections.synchronizedMap(new Hashtable());

    public synchronized PortletWindow createPortletWindow(String windowID, String entityID) throws NameNotFoundException 
    {
        PortletWindow window = (PortletWindow) wid2win.get(windowID);

        if (window == null) 
        {
            log.debug("Found no portletwindow with id[" + windowID + "], registring new instance");

            PortletApplicationEntityList applicationList = PortletEntityRegistry.getPortletApplicationEntityList();
            Iterator portletApplicationEntityListIterator = applicationList.iterator();
            while(portletApplicationEntityListIterator.hasNext())
            {
                PortletApplicationEntity pae = (PortletApplicationEntity)portletApplicationEntityListIterator.next();
                log.debug("Available application: " + pae.getId());
            }
            
            PortletEntity entity = PortletEntityRegistry.getPortletEntity(ObjectID.createFromString(entityID));

            if (entity == null) 
            {
                log.fatal("Couldn't find entity with id: " + entityID);
                throw new NameNotFoundException("Portlet entity not found: " + entityID);
            }
            
            window = new PortletWindowImpl(windowID, entity);

            ((PortletWindowListCtrl) entity.getPortletWindowList()).add(window);

            wid2win.put(windowID, window);
        }

        return window;
    }

    /**
     * Get a portlet-window by id.
     * 
     * @param id
     *            Id of window
     * @return portlet-window with attached portlet-entity, or null
     */
    public PortletWindow getPortletWindow(String id) {
        return (PortletWindow) wid2win.get(id);
    }

}