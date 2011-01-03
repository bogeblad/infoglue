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
import java.util.Vector;

import org.apache.pluto.om.common.Language;
import org.apache.pluto.om.common.LanguageSet;

/**
 * Dummy implementation of interface
 * @author Jöran
 * TODO Implement this!
 */
public class LanguageSetImpl implements LanguageSet {
    private static final Language defaultLanguage = new LanguageImpl();
    private static final Locale defaultLocale = Locale.ENGLISH;
    private static Vector languages = new Vector(); 
    private static Vector locales = new Vector();
    
    public LanguageSetImpl(){
        languages.add(defaultLanguage);
        locales.add(defaultLocale);
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.LanguageSet#iterator()
     */
    public Iterator iterator() {
        return languages.iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.LanguageSet#getLocales()
     */
    public Iterator getLocales() {
        return locales.iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.LanguageSet#get(java.util.Locale)
     */
    public Language get(Locale arg0) {
        return defaultLanguage;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.LanguageSet#getDefaultLocale()
     */
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

}
