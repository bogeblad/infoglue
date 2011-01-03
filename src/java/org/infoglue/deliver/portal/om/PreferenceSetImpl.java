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

import javax.portlet.PreferencesValidator;

import org.apache.pluto.om.common.Preference;
import org.apache.pluto.om.common.PreferenceSet;

/**
 * @author jand
 *
 */
public class PreferenceSetImpl implements PreferenceSet {

    private List preferences;
    private PreferencesValidator preferencesValidator;

    public PreferenceSetImpl(List preferences) {
        this.preferences = preferences;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.PreferenceSet#iterator()
     */
    public Iterator iterator() {
        return preferences.iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.PreferenceSet#get(java.lang.String)
     */
    public Preference get(String name) {
        for (Iterator it = preferences.iterator(); it.hasNext();) {
            Preference pref = (Preference) it.next();
            if (pref.getName().equals(name)) {
                return pref;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.PreferenceSet#getPreferencesValidator()
     */
    public PreferencesValidator getPreferencesValidator() {
        return preferencesValidator;
    }

    public String toString() {
        return "PreferenceSetImpl[ values:" + preferences + "]";
    }

}
