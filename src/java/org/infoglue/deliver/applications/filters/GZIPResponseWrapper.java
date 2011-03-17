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

package org.infoglue.deliver.applications.filters;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import java.util.Set;
import java.util.HashSet;

public class GZIPResponseWrapper extends HttpServletResponseWrapper 
{
	public final static Logger logger = Logger.getLogger(ResourceFilter.class.getName());

	protected HttpServletResponse origResponse = null;
	protected ServletOutputStream stream = null;
	protected PrintWriter writer = null;
	protected int error = 0;

	public GZIPResponseWrapper(HttpServletResponse response) 
	{
		super(response);
		origResponse = response;
	}

	public ServletOutputStream createOutputStream() throws IOException 
	{
		return (new GZIPResponseStream(origResponse));
	}

	public void finishResponse() {
		try {
			if (writer != null) {
				writer.close();
			} else {
				if (stream != null) {
					stream.close();
				}
			}
		} catch (IOException e) {
			logger.info("finishResponse", e);
		}
	}

	public void flushBuffer() throws IOException {
		stream.flush();
	}

	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null) {
			throw new IllegalStateException(
					"getWriter() has already been called!");
		}

		if (stream == null) {
			stream = createOutputStream();
		}

		return (stream);
	}

	public PrintWriter getWriter() throws IOException {
		// If access denied, don't create new stream or write because
		// it causes the web.xml's 403 page to not render
		if (this.error == HttpServletResponse.SC_FORBIDDEN) {
			return super.getWriter();
		}

		if (writer != null) {
			return (writer);
		}

		if (stream != null) {
			throw new IllegalStateException(
					"getOutputStream() has already been called!");
		}

		stream = createOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(stream,
				origResponse.getCharacterEncoding()));

		return (writer);
	}

	public void setContentLength(int length) {
		// no action here
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#sendError(int,
	 *      java.lang.String)
	 */
	public void sendError(int err, String message) throws IOException {
		super.sendError(err, message);
		this.error = err;

		if (logger.isDebugEnabled()) {
			logger.debug("sending error: " + err + " [" + message + "]");
		}
	}
}
