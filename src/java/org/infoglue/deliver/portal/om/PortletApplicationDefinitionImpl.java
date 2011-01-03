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

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.portlet.PortletDefinitionList;
import org.apache.pluto.om.servlet.WebApplicationDefinition;

/**
 * Dummy implementation of interface
 * @author Jöran
 * TODO Implement this
 *
 */
public class PortletApplicationDefinitionImpl implements
        PortletApplicationDefinition {

    private ObjectID oid;
    private PortletDefinitionList portletDefinitions = new PortletDefinitionListImpl();
    private WebApplicationDefinitionImpl webAppDefinition;
    
    /**
     * @param oid
     * @param portletDefinitions
     */
    public PortletApplicationDefinitionImpl(ObjectID oid) {
        this.oid = oid;
        webAppDefinition = new WebApplicationDefinitionImpl(oid);
    }
    
    

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletApplicationDefinition#getId()
     */
    public ObjectID getId() {
        return oid;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletApplicationDefinition#getVersion()
     */
    public String getVersion() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletApplicationDefinition#getPortletDefinitionList()
     */
    public PortletDefinitionList getPortletDefinitionList() {
        return portletDefinitions;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletApplicationDefinition#getWebApplicationDefinition()
     */
    public WebApplicationDefinition getWebApplicationDefinition() {
        return webAppDefinition;
    }

}
