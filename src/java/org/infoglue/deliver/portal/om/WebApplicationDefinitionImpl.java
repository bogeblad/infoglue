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

import javax.servlet.ServletContext;

import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.DisplayName;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.common.ParameterSet;
import org.apache.pluto.om.common.SecurityRoleSet;
import org.apache.pluto.om.servlet.ServletDefinitionList;
import org.apache.pluto.om.servlet.WebApplicationDefinition;
import org.apache.pluto.portalImpl.om.common.impl.SecurityRoleSetImpl;

/**
 * Dummy implementation of interface
 * @author Jöran
 * TODO Implement this
 *
 */
public class WebApplicationDefinitionImpl implements WebApplicationDefinition {
    private ObjectID oid;
    private ParameterSet parameters = new ParameterSetImpl();
    private ServletDefinitionListImpl servletDefinitions = new ServletDefinitionListImpl();
    private SecurityRoleSetImpl securityRoles = new SecurityRoleSetImpl();
    
    public WebApplicationDefinitionImpl(ObjectID oid){
        this.oid = oid;
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.WebApplicationDefinition#getId()
     */
    public ObjectID getId() {
        return oid;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.WebApplicationDefinition#getDisplayName(java.util.Locale)
     */
    public DisplayName getDisplayName(Locale locale) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.WebApplicationDefinition#getDescription(java.util.Locale)
     */
    public Description getDescription(Locale locale) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.WebApplicationDefinition#getInitParameterSet()
     */
    public ParameterSet getInitParameterSet() {
        return parameters;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.WebApplicationDefinition#getServletDefinitionList()
     */
    public ServletDefinitionList getServletDefinitionList() {
        return servletDefinitions;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.WebApplicationDefinition#getServletContext(javax.servlet.ServletContext)
     */
    public ServletContext getServletContext(ServletContext servletContext) {
        return servletContext;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.WebApplicationDefinition#getContextRoot()
     */
    public String getContextRoot() {
        return "";
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.WebApplicationDefinition#getSecurityRoles()
     */
    public SecurityRoleSet getSecurityRoles() {
        return securityRoles;
    }

}
