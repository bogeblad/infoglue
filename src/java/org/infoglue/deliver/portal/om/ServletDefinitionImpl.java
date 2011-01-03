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

import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.DisplayName;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.common.ParameterSet;
import org.apache.pluto.om.servlet.ServletDefinition;
import org.apache.pluto.om.servlet.WebApplicationDefinition;
import org.apache.pluto.portalImpl.om.servlet.impl.WebApplicationDefinitionImpl;

/**
 * Dummy implementation of interface
 * @author Jöran
 * TODO Implement this
 *
 */
public class ServletDefinitionImpl implements ServletDefinition {
    private ObjectID oid;
    private String servletName;
    private ParameterSet params;
    private WebApplicationDefinitionImpl webApplicationDefinition = new WebApplicationDefinitionImpl();
    
    
    public ServletDefinitionImpl(ObjectID oid){
        this.oid = oid;
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.ServletDefinition#getId()
     */
    public ObjectID getId() {
        return oid;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.ServletDefinition#getServletName()
     */
    public String getServletName() {
        return servletName;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.ServletDefinition#getDisplayName(java.util.Locale)
     */
    public DisplayName getDisplayName(Locale locale) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.ServletDefinition#getDescription(java.util.Locale)
     */
    public Description getDescription(Locale locale) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.ServletDefinition#getServletClass()
     */
    public String getServletClass() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.ServletDefinition#getInitParameterSet()
     */
    public ParameterSet getInitParameterSet() {
        return params;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.ServletDefinition#getWebApplicationDefinition()
     */
    public WebApplicationDefinition getWebApplicationDefinition() {
        return webApplicationDefinition;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.ServletDefinition#getRequestDispatcher(javax.servlet.ServletContext)
     */
    public RequestDispatcher getRequestDispatcher(ServletContext servletContext) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.ServletDefinition#getAvailable()
     */
    public long getAvailable() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.ServletDefinition#isUnavailable()
     */
    public boolean isUnavailable() {
        return false;
    }

}
