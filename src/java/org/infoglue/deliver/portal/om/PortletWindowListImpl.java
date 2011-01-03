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
import java.util.Vector;

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.om.window.PortletWindowList;
import org.apache.pluto.om.window.PortletWindowListCtrl;

/**
 * @author jand
 *
 */
public class PortletWindowListImpl implements PortletWindowList, PortletWindowListCtrl{
    
    private List windows = new Vector();

    /* (non-Javadoc)
     * @see org.apache.pluto.om.window.PortletWindowList#iterator()
     */
    public Iterator iterator() {
        return windows.iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.window.PortletWindowList#get(org.apache.pluto.om.common.ObjectID)
     */
    public PortletWindow get(ObjectID id) {
        for(Iterator it = windows.iterator(); it.hasNext();) {
            PortletWindow pw = (PortletWindow)it.next();
            if(pw.getId().equals(id)) {
                return pw;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.window.PortletWindowListCtrl#add(org.apache.pluto.om.window.PortletWindow)
     */
    public void add(PortletWindow window) {
        this.windows.add(window);
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.om.window.PortletWindowListCtrl#remove(org.apache.pluto.om.common.ObjectID)
     */
    public void remove(ObjectID id) {
        for (Iterator iter = this.windows.iterator(); iter.hasNext();) {
            PortletWindow element = (PortletWindow) iter.next();
            if (element.getId().equals(id)) {
                this.windows.remove(element);
                break;
            }
        }
    }
}
