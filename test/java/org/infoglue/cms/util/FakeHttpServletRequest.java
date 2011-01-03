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
 * $Id: FakeHttpServletRequest.java,v 1.11 2007/11/19 20:40:41 mattias Exp $
 */
package org.infoglue.cms.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.management.LanguageVO;

/**
 * A quick-and-dirty stub of HttpServletRequest.  We support attributes and parameters and implement the rest
 * of the methods as no-ops.
 * @author <a href=mailto:jedprentice@gmail.com>Jed Prentice</a>
 */
public class FakeHttpServletRequest implements HttpServletRequest
{
	private final Hashtable attributes = new Hashtable();
	private final Hashtable parameters = new Hashtable();
	private HttpSession session;
	private ServletContext servletContext;

	private String requestURI = null;
	
	public FakeHttpServletRequest()
	{
		this(new FakeHttpSession());
	}

	public FakeHttpServletRequest(HttpSession session)
	{
		setSession(session);
	}

	public FakeHttpServletRequest(Map parameters)
	{
		this.parameters.putAll(parameters);
	}
	
	public Object getAttribute(String name)
	{
		return attributes.get(name);
	}

	public Enumeration getAttributeNames()
	{
		return attributes.keys();
	}

	public void setAttribute(String name, Object value)
	{
		attributes.put(name, value);
	}

	public void removeAttribute(String name)
	{
		attributes.remove(name);
	}

	public String getParameter(String name)
	{
		return (String)parameters.get(name);
	}

	/**
	 * Not part of HttpServletRequest, but required so tests can hook up the parameters
	 */
	public void setParameter(String name, String value)
	{
		parameters.put(name, value);
	}

	public Enumeration getParameterNames()
	{
		return parameters.keys();
	}

	public String[] getParameterValues(String name)
	{
		return (String[])parameters.get(name);
	}

	public Map getParameterMap()
	{
		return parameters;
	}

	public HttpSession getSession(boolean create)
	{
		if (create && session == null)
			session = new FakeHttpSession();

		return getSession();
	}

	public HttpSession getSession()
	{
		return session;
	}

	/**
	 * Not part of HttpServletRequest, but required so tests can hook up the session whenever it is convenient
	 */
	public void setSession(HttpSession session)
	{
		this.session = session;
	}
	public void setRequestURI(String requestURI)
	{
		this.requestURI = requestURI;
	}
	public void setRequestRequestURI(String requestURI)
	{
		this.requestURI = requestURI;
	}
	public void setServletContext(ServletContext servletContext)
	{
		this.servletContext = servletContext;
	}
	
	/**
	 * Hardcoding all such request to GET.
	 */
	public String getMethod()                 
	{ 
	    return "GET"; 
	}

	/**
	 * Hardcoding all such request to GET.
	 */
	public String getRequestURI()             
	{ 
	    return "/ViewPage.action"; //requestURI; 
	}

	public RequestDispatcher getRequestDispatcher(String path) 
	{ 
	    return servletContext.getRequestDispatcher(path);
	}
	
	public Enumeration getLocales()  
	{ 
	    Vector vector = new Vector();
	    
	    try
	    {
		    LanguageVO languageVO = LanguageController.getController().getLanguageVOWithId(new Integer(this.getParameter("languageId")));
		    Locale locale = new Locale(languageVO.getLanguageCode());
		    vector.add(locale);
	    }
	    catch(Exception e) 
	    {
	        vector.add(Locale.getDefault());
	    }
	    
	    return vector.elements(); 
	}

	public Enumeration getHeaders(String name)  
	{ 
	    Vector vector = new Vector();
	    
	    try
	    {
		    if(name.equals("accept-language"))
		        vector.add("sv");
	    }
	    catch(Exception e) 
	    {
	        vector.add(Locale.getDefault());
	    }
	    
	    return vector.elements(); 
	}

	public StringBuffer getRequestURL()				
	{ 
	    return new StringBuffer("http://localhost/ViewPage.action"); 
	}

	public String getServletPath()            
	{ 
	    return "/"; 
	}

	public String getServerName() { return null; }
	public int getServerPort()    { return 0; }

	public String getCharacterEncoding()   { return null; }
	public int getContentLength()          { return 0; }
	public String getContentType()         { return null; }
	public ServletInputStream getInputStream() throws IOException  { return null; }
	public String getProtocol()   { return null; }
	public String getScheme()     { return "http"; }
	public BufferedReader getReader() throws IOException { return null; }
	public String getRemoteAddr() { return null; }
	public String getRemoteHost() { return null; }
	public Locale getLocale()        { return null; }
	public boolean isSecure()        { return false; }
	public String getRealPath(String path)   { return null; }

	public String getAuthType()      { return null; }
	public Cookie[] getCookies()     { return null; }
	public long getDateHeader(String name)   { return 0; }
	public String getHeader(String name)  { return null; }
	public Enumeration getHeaderNames()       { return null; }
	public int getIntHeader(String name)        { return 0; }
	public String getPathInfo()               { return null; }
	public String getPathTranslated()         { return null; }
	public boolean isUserInRole(String role)  { return false; }
	public Principal getUserPrincipal()       { return null; }
	public String getContextPath()            { return null; }
	public String getQueryString()            { return null; }
	public String getRemoteUser()             { return null; }
	public String getRequestedSessionId()     { return null; }
	public boolean isRequestedSessionIdValid() 	{ return false; }
	public boolean isRequestedSessionIdFromCookie() { return false; }
	public boolean isRequestedSessionIdFromURL()    { return false; }
	public boolean isRequestedSessionIdFromUrl()    { return false; }	
	public void setCharacterEncoding(String s) throws UnsupportedEncodingException {}

	public String getLocalAddr()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getLocalName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public int getLocalPort()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public int getRemotePort()
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
