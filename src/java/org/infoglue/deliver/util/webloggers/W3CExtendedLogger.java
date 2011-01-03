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

package org.infoglue.deliver.util.webloggers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The W3CExtendedLogger class is a class that writes down a request in the W3C Extended Log Format.
 */

public class W3CExtendedLogger extends CommonLogger
{
    /**
     * Construct a new Logger instance.
     */

    public W3CExtendedLogger() 
    {
    }   

    /**
     * Logs the given request.
     */

    public synchronized void logRequest(HttpServletRequest request, HttpServletResponse response, String pagePath, long duration)
    {	
    	StringBuffer sb = new StringBuffer();
		
		sb.append(defaultValueIfNull(getCurrentDate("yyyy-MM-dd HH:mm:ss")));  	//date + time
		sb.append(" ");
		sb.append(defaultValueIfNull(request.getRemoteAddr())); 				//c-ip
		sb.append(" ");
		sb.append(defaultValueIfNull(request.getRemoteUser())); 				//cs-username
		sb.append(" ");
		sb.append("W3SVC1"); 													//s-sitename
		sb.append(" ");
		sb.append(defaultValueIfNull(getHostName())); 							//s-computername
		sb.append(" ");
		sb.append(defaultValueIfNull(getHostAddress())); 						//s-ip
		sb.append(" ");
		sb.append(defaultValueIfNull("" + request.getServerPort())); 			//s-port
		sb.append(" ");
		sb.append(defaultValueIfNull(request.getMethod()));						//cs-method
		sb.append(" ");
		sb.append(defaultValueIfNull(pagePath));								//cs-uri-stem (translated to our pagePath)
		sb.append(" ");
		sb.append(defaultValueIfNull(request.getQueryString()));				//cs-uri-query
		sb.append(" ");
		sb.append("-"); //Status - we can't tell from here						//sc-status
		sb.append(" ");
		sb.append("-"); //Status - we can't tell from here						//sc-win32-status
		sb.append(" ");
		sb.append(defaultValueIfNull("" + response.getBufferSize()));			//sc-bytes
		sb.append(" ");
		sb.append(defaultValueIfNull(request.getHeader("Content-Length")));		//cs-bytes
		sb.append(" ");
		sb.append(defaultValueIfNull("" + duration));							//time-taken
		sb.append(" ");
		sb.append(defaultValueIfNull(request.getProtocol()));					//cs-version
		sb.append(" ");
		sb.append(defaultValueIfNull(request.getRemoteHost()));					//cs-host
		sb.append(" ");
		sb.append(defaultValueIfNull(request.getHeader("User-Agent")));			//cs(User-Agent)
		sb.append(" ");
		sb.append(defaultValueIfNull(request.getHeader("Cookie")));				//cs(Cookie)
		sb.append(" ");
		sb.append(defaultValueIfNull(request.getHeader("Referer")));			//cs(Referer)

		writeRequest(getCurrentDate("yyyy-MM-dd"), sb.toString());
    }


}

