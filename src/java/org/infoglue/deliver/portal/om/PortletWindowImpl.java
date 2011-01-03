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
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.window.PortletWindow;

/**
 * @author jand
 *
 */
public class PortletWindowImpl implements PortletWindow {

    private String id; 
    private PortletEntity portletEntity;
    private String logicName;
    
    /**
	 * 
	 */
	public PortletWindowImpl(String id, PortletEntity portletEntity) {
		this.id = id;
		this.portletEntity = portletEntity;
	}
	
    
    /* (non-Javadoc)
     * @see org.apache.pluto.om.window.PortletWindow#getId()
     */
    public ObjectID getId() {
        return org.apache.pluto.portalImpl.util.ObjectID.createFromString(id);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.window.PortletWindow#getPortletEntity()
     */
    public PortletEntity getPortletEntity() {
        return portletEntity;
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("PortletWindowImpl[ id:");
        buffer.append(this.id.toString());
        buffer.append(" portletEntity.id:");
        buffer.append(this.portletEntity.getId());
        buffer.append("]");
        
        return buffer.toString();
    }

    public void setPortletEntity(PortletEntity entity) {
        this.portletEntity = entity;
    }

    /**
     * @return Returns the logicName.
     */
    public String getLogicName() {
        return logicName;
    }
    
    /**
     * @param logicName The logicName to set.
     */
    public void setLogicName(String logicName) {
        this.logicName = logicName;
    }
}
