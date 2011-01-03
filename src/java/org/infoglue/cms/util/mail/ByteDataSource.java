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
 
package org.infoglue.cms.util.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * A simple DataSource implementation
 */
public class ByteDataSource implements DataSource 
{
	/**
	 * 
	 */
    private byte[] data;
    
    /**
     * 
     */
    private String type;	
    
    /**
     * 
     */
    public ByteDataSource(final byte[] data, final String type)
    {
    	this.data = data;
    	this.type = type;
    }

    /**
     * 
     */
    public InputStream getInputStream() throws IOException 
    {
		if (data == null)
		{
	    	throw new IOException("no data");
		}

		return new ByteArrayInputStream(data);
    }

    public OutputStream getOutputStream() throws IOException 
    {
		throw new IOException("cannot do this");
    }

    public String getContentType() 
    {
        return type;
    }

    public String getName() 
    {
        return "dummy";
    }
} 