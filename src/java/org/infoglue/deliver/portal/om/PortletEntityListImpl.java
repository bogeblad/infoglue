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

import java.util.Iterator;
import java.util.List;

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.entity.PortletEntityList;

/**
 * @author jand
 *
 */
public class PortletEntityListImpl implements PortletEntityList {

    private List entities;

    public PortletEntityListImpl(List entities) {
        this.entities = entities;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntityList#iterator()
     */
    public Iterator iterator() {
        return entities.iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntityList#get(org.apache.pluto.om.common.ObjectID)
     */
    public PortletEntity get(ObjectID id) {
        for (Iterator it = entities.iterator(); it.hasNext();) {
            PortletEntity pe = (PortletEntity) it.next();
            if (pe.getId().equals(id)) {
                return pe;
            }
        }
        return null;
    }

    public void add(PortletEntity e) {
        entities.add(e);
    }
}
