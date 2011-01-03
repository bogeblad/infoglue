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

package org.infoglue.cms.applications.common.actions;

import org.infoglue.cms.exception.ConfigurationError;

import webwork.util.ServletValueStack;


/**
 * @author mgu
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ConfirmRedirect extends InfoGlueAbstractAction {

	private static final long serialVersionUID = 8512298644737456785L;

	private String unparsedURL;


	public void setUrl(String unparsedURL) 
	{
		this.unparsedURL = unparsedURL;
	}
	
	public String doExecute() throws Exception 
	{
		validateUnparsedURL();
		redirect();
		return SUCCESS;
	}

	
	private void validateUnparsedURL() 
	{
		if (this.unparsedURL == null || this.unparsedURL.trim().length() == 0) 
		{
			throw new ConfigurationError("No url/empty url specified for ExtendedRedirect.action");
		}
	}
	
	
	
	private void redirect() throws Exception 
	{
		final String url = getValueFromCallingAction(this.unparsedURL);
		getResponse().sendRedirect(url);
	}
	
	
	private String getValueFromCallingAction(String fieldName) 
	{
	    Object value = ServletValueStack.getStack(getRequest()).findValue(fieldName);
		//Object value = ValueStack.getStack(getRequest()).findValue(fieldName);
	
		if (value==null)
			value = getRequest().getParameter(fieldName);
		
		if (value == null) 
		{
			throw new ConfigurationError("Unable to find the value for the parameter [" + fieldName + "].");
		}
		
		
		return value.toString();
	}


}
