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

package org.infoglue.deliver.util.webservices;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpc;
import org.apache.xmlrpc.XmlRpcClient;


/**
 * @author Mattias Bogeblad
 *
 * This class helps using the popular XML-RPC way of accessing data.
 */

public class XMLRPCHelper
{
    private final static Logger logger = Logger.getLogger(XMLRPCHelper.class.getName());

	private String serviceUrl = "";
	private String method = "";
	private Vector parameters = new Vector();
	private Object result = null;

	private String errorCode = "0";
	private String errorMessage = "Ok";

	/**
	 * The constructor for this class.
	 */
	
	public XMLRPCHelper()
	{	
	}
	
	public void makeCall()
	{
		try
		{
			XmlRpcClient xmlrpc = new XmlRpcClient(serviceUrl);
			XmlRpc.setEncoding("ISO-8859-1");
			//Vector params = new Vector ();
			//params.addElement("7");
			//params.addElement("peew9yoop");
			//params.addElement("1");
			
			// this method returns a string
			this.result = xmlrpc.execute(this.method, this.parameters);
			
			logger.info("result:" + result);
		}
		catch(Exception e)
		{
			this.errorCode = "1";
			this.errorMessage = "An error occurred:" + e.getMessage();
			logger.warn("An error occurred:" + e.getMessage(), e);
		}
	}
	
	public void setParameters(Vector parameters)
	{
		this.parameters = parameters;
	}
	
	public Vector getParameters()
	{
		return this.parameters;
	}
	
	public String getServiceUrl()
	{
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl)
	{
		this.serviceUrl = serviceUrl;
	}

	public Object getResult()
	{
		return this.result;
	}

	public String getErrorCode()
	{
		return this.errorCode;
	}

	public String getErrorMessage()
	{
		return this.errorMessage;
	}

	public void setErrorCode(String errorCode)
	{
		this.errorCode = errorCode;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public String getMethod()
	{
		return this.method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

}