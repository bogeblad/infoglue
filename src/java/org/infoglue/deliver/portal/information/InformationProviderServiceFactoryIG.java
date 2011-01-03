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
package org.infoglue.deliver.portal.information;

import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.portalImpl.core.StaticInformationProviderImpl;
import org.apache.pluto.portalImpl.factory.InformationProviderFactory;
import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.InformationProviderService;
import org.apache.pluto.services.information.StaticInformationProvider;

/**
 * @author jand
 *
 */
public class InformationProviderServiceFactoryIG
    implements InformationProviderFactory, InformationProviderService {
    private static final Log log = LogFactory.getLog(InformationProviderServiceFactoryIG.class);

    private static final String DYNAMIC = DynamicInformationProviderIG.class.getName();
    private static final String STATIC = StaticInformationProviderImpl.class.getName();

    private ServletConfig config;

    public InformationProviderServiceFactoryIG() {
        log.debug("constructor");
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.InformationProviderService#getStaticProvider()
     */
    public StaticInformationProvider getStaticProvider() {
        // TODO stolen from portalImpl
        log.debug("getStaticProvider(): using default from portalImpl, fix this!");
        javax.servlet.ServletContext context = config.getServletContext();

        StaticInformationProvider provider =
            (StaticInformationProvider) context.getAttribute(STATIC);

        if (provider == null) {
            provider = new StaticInformationProviderImpl(config);
            context.setAttribute(STATIC, provider);
        }

        return provider;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.InformationProviderService#getDynamicProvider(javax.servlet.http.HttpServletRequest)
     */
    public DynamicInformationProvider getDynamicProvider(HttpServletRequest request) {
        log.debug("getDynamicProvider(): using infoglue's");
        DynamicInformationProvider provider =
            (DynamicInformationProvider) request.getAttribute(DYNAMIC);
        if (provider == null) {
            provider = new DynamicInformationProviderIG(request, config);
            request.setAttribute(DYNAMIC, provider);
        }
        return provider;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.factory.Factory#init(javax.servlet.ServletConfig, java.util.Map)
     */
    public void init(ServletConfig config, Map properties) throws Exception {
        this.config = config;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.factory.Factory#destroy()
     */
    public void destroy() throws Exception {

    }

}
