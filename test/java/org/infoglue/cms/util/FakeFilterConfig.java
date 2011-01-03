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
 *
 * $Id: FakeFilterConfig.java,v 1.5 2008/01/24 20:18:16 mattias Exp $
 */
package org.infoglue.cms.util;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 * Allows us to configure the InfoGlueAuthorizationFilter for testing outside of a servlet environment.  Parameter
 * keys and values are read from cms.properties.
 * @author <a href=mailto:jedprentice@gmail.com>Jed Prentice</a>
 */
public class FakeFilterConfig implements FilterConfig
{
	private final Hashtable initParameters = new Hashtable();

	public FakeFilterConfig()
	{
		String authenticatorClass 	= CmsPropertyHandler.getServerNodeProperty("deliver", "authenticatorClass", true, null);
		String authorizerClass 		= CmsPropertyHandler.getServerNodeProperty("deliver", "authorizerClass", true, null);
		String invalidLoginUrl 		= CmsPropertyHandler.getServerNodeProperty("deliver", "invalidLoginUrl", true, null);
		String successLoginBaseUrl 	= CmsPropertyHandler.getServerNodeProperty("deliver", "successLoginBaseUrl", true, null);
		String loginUrl 			= CmsPropertyHandler.getServerNodeProperty("deliver", "loginUrl", true, null);
		String logoutUrl 			= CmsPropertyHandler.getServerNodeProperty("deliver", "logoutUrl", true, null);
		String serverName 			= CmsPropertyHandler.getServerNodeProperty("deliver", "serverName", true, null);
		String casRenew 			= CmsPropertyHandler.getServerNodeProperty("deliver", "casRenew", true, null);
		String casServiceUrl 		= CmsPropertyHandler.getServerNodeProperty("deliver", "casServiceUrl", true, null);
		String casValidateUrl 		= CmsPropertyHandler.getServerNodeProperty("deliver", "casValidateUrl", true, null);
		String casProxyValidateUrl 	= CmsPropertyHandler.getServerNodeProperty("deliver", "casProxyValidateUrl", true, null);
		String casLogoutUrl 		= CmsPropertyHandler.getServerNodeProperty("deliver", "casLogoutUrl", true, null);
		String authConstraint 		= CmsPropertyHandler.getServerNodeProperty("deliver", "authConstraint", true, null);
		String extraParametersFile 		= CmsPropertyHandler.getServerNodeProperty("deliver", "extraParametersFile", true, null);

		initParameters.put("org.infoglue.cms.security.loginUrl", loginUrl);
		initParameters.put("org.infoglue.cms.security.invalidLoginUrl",	invalidLoginUrl);
		initParameters.put("org.infoglue.cms.security.authenticatorClass", authenticatorClass);
		initParameters.put("org.infoglue.cms.security.authorizerClass",	authorizerClass);
		initParameters.put("org.infoglue.cms.security.serverName", serverName);
		initParameters.put("org.infoglue.cms.security.authConstraint", authConstraint);
		initParameters.put("org.infoglue.cms.security.extraParametersFile",	extraParametersFile);
		initParameters.put("org.infoglue.cms.security.casValidateUrl", casValidateUrl);
		initParameters.put("org.infoglue.cms.security.casProxyValidateUrl", casProxyValidateUrl);
		initParameters.put("org.infoglue.cms.security.casServiceUrl", casServiceUrl);
	}

	/**
	 * Returns the name of this filter
	 * @return "fakeServletFilter"
	 */
	public String getFilterName()
	{
		return "fakeServletFilter";
	}

	/**
	 * Returns the servlet context
	 * @return a reference to FakeServletContext
	 * @see org.infoglue.cms.util.FakeServletContext
	 */
	public ServletContext getServletContext()
	{
		return FakeServletContext.getContext();
	}

	/**
	 * Returns the init parameter with the given name
	 * @param name the name of the desired init parameter
	 * @return the value of the init parameter associated with name, or null if no init parametrer with name exists.
	 */
	public String getInitParameter(String name)
	{
		return (String)initParameters.get(name);
	}

	/**
	 * Returns an enumeration of all init parameters
	 * @return an enumeration of all init parameters
	 */
	public Enumeration getInitParameterNames()
	{
		return initParameters.keys();
	}
}
