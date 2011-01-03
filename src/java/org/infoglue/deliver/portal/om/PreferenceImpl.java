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
import java.util.List;

import org.apache.pluto.om.common.Preference;

/**
 * @author jand
 *
 */
public class PreferenceImpl implements Preference {

    private String name;
    private boolean isReadOnly = false;
    private List values = new ArrayList();

    public PreferenceImpl(String name, List values) {
        this.name = name;
        this.values = values;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.Preference#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.Preference#getValues()
     */
    public Iterator getValues() {
        return values.iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.Preference#isReadOnly()
     */
    public boolean isReadOnly() {
        return isReadOnly;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.Preference#isValueSet()
     */
    public boolean isValueSet() {
        return (values != null && values.size() > 0);
    }

    public String toString() {
        return "PreferenceImpl[name:" + name + " value:" + values + "]";
    }
}
