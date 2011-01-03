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
 
package org.infoglue.cms.plugins.codeeditor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * URL utility class. Used for tasks involving getting url-contents from remote addresses 
 * within a program.
 * 
 * @author Mattias Bogeblad
 * @version 1
 * @since 1999-10-05
 */


public class HttpUtilities 
{            

    /**
     */
    private HttpUtilities()
    {}

	
	public static String getUrlContent(String urlAddress) throws Exception
	{
	    URL url = new URL(urlAddress);
	    URLConnection connection = url.openConnection();
	    connection.setUseCaches(false);
	    InputStream inStream = null;
	    inStream = connection.getInputStream();
	    InputStreamReader inStreamReader = new InputStreamReader(inStream);
	    BufferedReader buffer = new BufferedReader(inStreamReader);            
	    StringBuffer strbuf = new StringBuffer();   
	    String line; 
	    while((line = buffer.readLine()) != null) 
	    {
	        strbuf.append(line); 
	    }                                              
	    String readData = strbuf.toString();   
	    buffer.close();
	    return readData;   
	}

}
