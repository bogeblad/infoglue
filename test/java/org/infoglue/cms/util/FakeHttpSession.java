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
 * $Id: FakeHttpSession.java,v 1.3 2006/03/06 16:54:41 mattias Exp $
 */
package org.infoglue.cms.util;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * A quick-and-dirty stub of HttpSession.
 * @author <a href=mailto:jedprentice@gmail.com>Jed Prentice</a>
 */
public class FakeHttpSession implements HttpSession
{
	private final Hashtable attributes = new Hashtable();

	public Object getAttribute(String name)
	{
		return attributes.get(name);
	}

	public void setAttribute(String name, Object value)
	{
		attributes.put(name, value);
	}

	public void removeAttribute(String name)
	{
		attributes.remove(name);
	}

	public Enumeration getAttributeNames()
	{
		return attributes.keys();
	}

	public ServletContext getServletContext()
	{
		return FakeServletContext.getContext();
	}

	public Object getValue(String name)
	{
		return getAttribute(name);
	}

	public String[] getValueNames()
	{
		String[] names = new String[attributes.size()];
		Enumeration enumeration = getAttributeNames();
		for (int i = 0; i < names.length && enumeration.hasMoreElements(); ++i)
			names[i] = (String)enumeration.nextElement();

		return names;
	}

	public void putValue(String name, Object value)
	{
		setAttribute(name, value);
	}

	public void removeValue(String name)
	{
		removeAttribute(name);
	}

	public long getCreationTime()                   { return 0; }
	public String getId()                           { return null; }
	public long getLastAccessedTime()               { return 0; }
	public void setMaxInactiveInterval(int n)       {}
	public int getMaxInactiveInterval()             { return 0; }
	public HttpSessionContext getSessionContext()   { return null; }
	public void invalidate()                        {}
	public boolean isNew()                          { return false; }
}