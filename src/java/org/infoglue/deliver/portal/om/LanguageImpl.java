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
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.pluto.om.common.Language;

/**
 * Dummy implementation of interface
 * @author Jöran
 * TODO Implement this!
 */
public class LanguageImpl implements Language {

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.Language#getLocale()
     */
    public Locale getLocale() {
        return Locale.ENGLISH;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.Language#getTitle()
     */
    public String getTitle() {
        return "English";
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.Language#getShortTitle()
     */
    public String getShortTitle() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.Language#getKeywords()
     */
    public Iterator getKeywords() {
        Vector keywords = new Vector();
        return keywords.iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.Language#getResourceBundle()
     */
    public ResourceBundle getResourceBundle() {
        return null;
    }

}
