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

import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.DisplayName;
import org.apache.pluto.om.common.LanguageSet;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.common.ParameterSet;
import org.apache.pluto.om.common.PreferenceSet;
import org.apache.pluto.om.common.SecurityRoleRefSet;
import org.apache.pluto.om.portlet.ContentTypeSet;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.servlet.ServletDefinition;

/**
 * @author jand
 *
 */
public class PortletDefinitionImpl implements PortletDefinition {
    private LanguageSetImpl languages;
    private ParameterSetImpl parameters;
    private SecurityRoleRefSetImpl securityRoleRefs;
    private DescriptionImpl description;
    private PreferenceSetImpl preferences;
    private ContentTypeSetImpl contentTypes;
    private PortletApplicationDefinition portletApplicationDefinition;
    private ServletDefinition servletDefinition;
    
    private ObjectID oid;
    private String className ="se.skolutveckling.portlet.TestPortlet";
    private String name ="";
    
    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletDefinition#getId()
     */
    public ObjectID getId() {
        return oid;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletDefinition#getClassName()
     */
    public String getClassName() {
        return className;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletDefinition#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletDefinition#getDescription(java.util.Locale)
     */
    public Description getDescription(Locale locale) {
        return description;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletDefinition#getLanguageSet()
     */
    public LanguageSet getLanguageSet() {
        return languages;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletDefinition#getInitParameterSet()
     */
    public ParameterSet getInitParameterSet() {
        return parameters;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletDefinition#getInitSecurityRoleRefSet()
     */
    public SecurityRoleRefSet getInitSecurityRoleRefSet() {
        return securityRoleRefs;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletDefinition#getPreferenceSet()
     */
    public PreferenceSet getPreferenceSet() {
        return preferences;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletDefinition#getContentTypeSet()
     */
    public ContentTypeSet getContentTypeSet() {
        return contentTypes;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletDefinition#getPortletApplicationDefinition()
     */
    public PortletApplicationDefinition getPortletApplicationDefinition() {
        return portletApplicationDefinition;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletDefinition#getServletDefinition()
     */
    public ServletDefinition getServletDefinition() {
        return servletDefinition;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletDefinition#getDisplayName(java.util.Locale)
     */
    public DisplayName getDisplayName(Locale locale) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletDefinition#getExpirationCache()
     */
    public String getExpirationCache() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletDefinition#getPortletClassLoader()
     */
    public ClassLoader getPortletClassLoader() {
        return null;
    }
    

}
