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
 * $Id: FakeServletContext.java,v 1.2 2006/03/06 16:54:41 mattias Exp $
 */
package org.infoglue.cms.util;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;

import org.infoglue.cms.security.InfoGlueAuthenticationFilter;

/**
 * Allows us to initialize the CmsContextListener outside of a servlet environment.  Only the critical methods
 * are implemented, the rest are no-ops.
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class FakeServletContext implements ServletContext
{
	private static final FakeServletContext context = new FakeServletContext();

	private boolean initialized;

	private FakeServletContext() {}

	/**
	 * Factory method to return a reference to the context
	 * @return a reference to a FakeServletContext
	 */
	public static FakeServletContext getContext()
	{
		return context;
	}

	/**
	 * Initializes the InfoGlue ServletContextListener and the InfoglueAuthenticationFilter
	 */
	public void init()
	{
		if (!initialized)
		{
			initializeContextListener();
			initializeAuthenticationFilter();
			initialized = true;
		}
	}

	/**
	 * Initializes the InfoGlue ServletContextListener with a new ServletContextEvent created from this
	 */
	private void initializeContextListener()
	{
		new CmsContextListener().contextInitialized(new ServletContextEvent(this));
	}

	/**
	 * Initializes the InfoGlueAuthentocationFilter with a fake filter config.  Relies on the fact that
	 * InfoGlueAuthenticationFilter stores the init params statically.
	 */
	private void initializeAuthenticationFilter()
	{
		try
		{
			new InfoGlueAuthenticationFilter().init(new FakeFilterConfig());
		}
		catch (ServletException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Implement this method to provide a path to the CmsContextListener.
	 * This value is passed in via the ant task <code><junit></code> as a <code><sysproperty></code>
	 */
	public String getRealPath(String s)
	{
		return System.getProperty("webapp.dir") + s;
	}

	public ServletContext getContext(String s)	{ return null; }
	public int getMajorVersion()				{ return 0; }
	public int getMinorVersion()				{ return 1; }
	public String getMimeType(String s)			{ return null; }
	public Set getResourcePaths(String s)		{ return null; }
	public URL getResource(String s) throws MalformedURLException	{ return null; }
	public InputStream getResourceAsStream(String s)				{ return null; }
	public RequestDispatcher getRequestDispatcher(String s)			{ return null; }
	public RequestDispatcher getNamedDispatcher(String s)			{ return null; }
	public Servlet getServlet(String s) throws ServletException		{ return null; }
	public Enumeration getServlets()				{ return null; }
	public Enumeration getServletNames()			{ return null; }
	public void log(String s)						{}
	public void log(Exception e, String s)			{}
	public void log(String s, Throwable throwable)	{}
	public String getServerInfo() 					{ return null; }
	public String getInitParameter(String s) 		{ return null; }
	public Enumeration getInitParameterNames()		{ return null; }
	public Object getAttribute(String s)			{ return null; }
	public Enumeration getAttributeNames()			{ return null; }
	public void setAttribute(String s, Object o)	{}
	public void removeAttribute(String s)			{}
	public String getServletContextName()			{ return null; }
}
