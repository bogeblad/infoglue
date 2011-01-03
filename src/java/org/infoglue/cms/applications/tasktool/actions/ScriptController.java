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

package org.infoglue.cms.applications.tasktool.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.infoglue.cms.exception.SystemException;

/**
 * This is the script controller interface. This is the entry point for scripts running tasks.
 */

public interface ScriptController
{

	public void logInfo(String info);
	
	/**
	 * Begins a transaction on the named database
	 */
         
	public void beginTransaction() throws SystemException;

	/**
	 * Ends a transaction on the named database
	 */
     
	public void commitTransaction() throws SystemException;

	/**
	 * A method to get the request for this script
	 */

	public void setRequest(HttpServletRequest request);

	/**
	 * A method to get the request for this script
	 */

	public HttpServletRequest getRequest();

	public void setResponse(HttpServletResponse response);

	public HttpServletResponse getResponse();

}
