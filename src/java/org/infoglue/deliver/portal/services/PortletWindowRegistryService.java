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

import javax.naming.NameNotFoundException;

import org.apache.pluto.om.window.PortletWindow;

/**
 * @author jand
 *
 */
public interface PortletWindowRegistryService {
    
  
    /**
     * Get a portlet-window by id.
     * 
     * @param id Id of window
     * @return portlet-window with attached portlet-entity, or null
     */
    public PortletWindow getPortletWindow(String id);
    
    /**
     * Creates a Portlet window associated with a Portlet entity.
     * @param windowID the identifier of the window
     * @param entityID identifies the Portlet entity
     */
    public PortletWindow createPortletWindow(String windowID, String entityID) throws NameNotFoundException;
}