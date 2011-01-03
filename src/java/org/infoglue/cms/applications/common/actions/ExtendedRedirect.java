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

import java.util.StringTokenizer;

import org.infoglue.cms.exception.ConfigurationError;

import webwork.util.ServletValueStack;

/**
 *
 * @author Mattias Bogeblad
 */

public class ExtendedRedirect extends InfoGlueAbstractAction 
{

	private static final long serialVersionUID = -8254805372363786462L;
	
	private static final String UNPARSED_PARAMETER_DELIMITER = "#";
  	
	private String unparsedURL;

	/**
	 *
	 */

	public void setUrl(String unparsedURL) 
	{
		this.unparsedURL = unparsedURL;
	}

	/**
	 *
	 */
	
	public String doExecute() throws Exception 
	{
		validateUnparsedURL();
		redirect();
		return NONE;
	}


	/**
	 *
	 */
	
	private void validateUnparsedURL() 
	{
		if (this.unparsedURL == null || this.unparsedURL.trim().length() == 0) 
		{
			throw new ConfigurationError("No url/empty url specified for ExtendedRedirect.action");
		}
	}
	
	
	/**
	 *
	 */
	
	private void redirect() throws Exception 
	{
	    final String url = getResponse().encodeRedirectURL(parse(this.unparsedURL));
		getResponse().sendRedirect(url);
	}
	
	/**
	 *
	 */
	
	private String parse(String unparsedURL) 
	{
		final StringTokenizer st =	new StringTokenizer(unparsedURL.trim(), UNPARSED_PARAMETER_DELIMITER);
		final StringBuffer sb = new StringBuffer(st.nextToken()); // action
		sb.append(st.hasMoreTokens() ? "?" : "");
		while (st.hasMoreTokens()) 
		{
			sb.append(createParameterString(st.nextToken()));
			sb.append(st.hasMoreTokens() ? "&" : "");
		}
		return sb.toString();
	}
	
	/**
	 *
	 */
	
	private String createParameterString(String name) 
	{
		return name + "=" + getValueFromCallingAction(name);
	}
	
	/**
	 * 
	 */

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
