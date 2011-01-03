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

import javax.portlet.PortletMode;

import org.apache.pluto.om.portlet.ContentType;
import org.apache.pluto.om.portlet.ContentTypeSet;

/**
 * @author jand
 *
 */
public class ContentTypeSetImpl implements ContentTypeSet {

    private List contentTypes;
    private boolean supportsPortletMode;

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.ContentTypeSet#iterator()
     */
    public Iterator iterator() {
        return contentTypes.iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.ContentTypeSet#get(java.lang.String)
     */
    public ContentType get(String contentType) {
        for(Iterator it = contentTypes.iterator(); it.hasNext();) {
            ContentType type = (ContentType)it.next();
            if(type.getContentType().equals(contentType)) {
                return type;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.ContentTypeSet#supportsPortletMode(javax.portlet.PortletMode)
     */
    public boolean supportsPortletMode(PortletMode portletMode) {
        return supportsPortletMode;
    }

}
