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
 * $Id: FakeHttpServletResponse.java,v 1.4 2007/11/19 20:40:41 mattias Exp $
 */
package org.infoglue.cms.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * A quick-and-dirty stub of HttpServletRequest.  All methods are no-ops, which can be implemented as needed later.
 * @author <a href=mailto:jedprentice@gmail.com>Jed Prentice</a>
 */
public class FakeHttpServletResponse implements HttpServletResponse
{
    
	public PrintWriter getWriter() throws IOException 
	{ 
	    return new PrintWriter(new ByteArrayOutputStream(2048)); 
	}

    public String getCharacterEncoding() { return null; }
	public ServletOutputStream getOutputStream() throws IOException { return null; }
	public void setContentLength(int i) {}
	public void setContentType(String s) {}
	public void setBufferSize(int i) {}
	public int getBufferSize() { return 0; }
	public void flushBuffer() throws IOException {}
	public void resetBuffer() {}
	public boolean isCommitted() { return false; }
	public void reset() {}
	public void setLocale(Locale locale) {}
	public Locale getLocale() { return null; }

	public void addCookie(Cookie cookie) {}
	public boolean containsHeader(String s) {return false; }
	public String encodeURL(String s) { return null; }
	public String encodeRedirectURL(String s) {return null; }
	public String encodeUrl(String s) {return null; }
	public String encodeRedirectUrl(String s) {return null; }
	public void sendError(int i, String s) throws IOException {}
	public void sendError(int i) throws IOException {}
	public void sendRedirect(String s) throws IOException {}
	public void setDateHeader(String s, long l) {}
	public void addDateHeader(String s, long l) {}
	public void setHeader(String s, String s1) {}
	public void addHeader(String s, String s1) {}
	public void setIntHeader(String s, int i) {}
	public void addIntHeader(String s, int i) {}
	public void setStatus(int i) {}
	public void setStatus(int i, String s) {}

	public String getContentType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setCharacterEncoding(String arg0)
	{
		// TODO Auto-generated method stub
		
	}
}
