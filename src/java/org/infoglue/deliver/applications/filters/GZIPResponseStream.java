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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Wraps Response Stream for GZipFilter
 * 
 * @author Matt Raible
 * @version $Revision: 1.2 $ $Date: 2004/05/22 12:24:23 $
 */
public class GZIPResponseStream extends ServletOutputStream
{
	public final static Logger logger = Logger.getLogger(GZIPResponseStream.class.getName());

	// abstraction of the output stream used for compression
	protected OutputStream bufferedOutput = null;

	// state keeping variable for if close() has been called
	protected boolean closed = false;

	// reference to original response
	protected HttpServletResponse response = null;

	// reference to the output stream to the client's browser
	protected ServletOutputStream output = null;

	// default size of the in-memory buffer
	private int bufferSize = 50000;

	private File tempFile;

	public GZIPResponseStream(HttpServletResponse response) throws IOException {
		super();
		closed = false;
		this.response = response;
		this.output = response.getOutputStream();
		bufferedOutput = new ByteArrayOutputStream();
	}

	public void close() throws IOException {
		if (closed) {
			throw new IOException("This output stream has already been closed");
		}

		try
		{
			// if we buffered everything in memory, gzip it
			if (bufferedOutput instanceof ByteArrayOutputStream) {
				// get the content
				ByteArrayOutputStream baos = (ByteArrayOutputStream) bufferedOutput;

				// prepare a gzip stream
				ByteArrayOutputStream compressedContent = new ByteArrayOutputStream();
				GZIPOutputStream gzipstream = new GZIPOutputStream(compressedContent);
				byte[] bytes = baos.toByteArray();
				gzipstream.write(bytes);
				gzipstream.finish();

				// get the compressed content
				byte[] compressedBytes = compressedContent.toByteArray();

				// set appropriate HTTP headers
				response.setContentLength(compressedBytes.length);
				response.addHeader("Content-Encoding", "gzip");
				output.write(compressedBytes);
				output.flush();
				output.close();
				closed = true;
			}
			// if things were not buffered in memory, finish the GZIP stream and
			// response
			else if (bufferedOutput instanceof GZIPOutputStream) {
				// cast to appropriate type
				GZIPOutputStream gzipstream = (GZIPOutputStream) bufferedOutput;

				// finish the compression
				gzipstream.finish();
				gzipstream.flush();
				gzipstream.close();

				response.setContentLength((int)tempFile.length());

				FileInputStream tempFileStream = new FileInputStream(tempFile);
				IOUtils.copy(tempFileStream, output);

				// finish the response
				output.flush();
				output.close();
				closed = true;
			}
		}
		finally
		{
			if (tempFile != null)
			{
				tempFile.delete();
				tempFile = null;
			}
		}
	}

	public void flush() throws IOException {
		if (closed) {
			throw new IOException("Cannot flush a closed output stream");
		}

		bufferedOutput.flush();
	}

	public void write(int b) throws IOException {
		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}

		// make sure we aren't over the buffer's limit
		checkBufferSize(1);

		// write the byte to the temporary output
		bufferedOutput.write((byte) b);
	}

	private void switchToFilebacking(byte[] initialBytes) throws FileNotFoundException, IOException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("Resource exceeds buffer size. Switching to file-backed mode.");
		}
		tempFile = File.createTempFile("gzip-", ".tmp");

		// make new gzip stream using the response output stream
		OutputStream gzipstream = new GZIPOutputStream(new FileOutputStream(tempFile));
		gzipstream.write(initialBytes);

		// we are no longer buffering, send content via gzipstream
		bufferedOutput = gzipstream;

		response.addHeader("Content-Encoding", "gzip");
	}

	private void checkBufferSize(int length) throws IOException {
		// check if we are buffering too large of a file
		if (bufferedOutput instanceof ByteArrayOutputStream) {
			ByteArrayOutputStream baos = (ByteArrayOutputStream) bufferedOutput;

			if ((baos.size() + length) > bufferSize) {
				// files too large to keep in memory are sent to the client without
				// Content-Length specified
				switchToFilebacking(baos.toByteArray());
			}
		}
	}

	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}

		// make sure we aren't over the buffer's limit
		checkBufferSize(len);

		// write the content to the buffer
		bufferedOutput.write(b, off, len);
	}

	public boolean closed() {
		return (this.closed);
	}

	public void reset() {
		//noop
	}
}
