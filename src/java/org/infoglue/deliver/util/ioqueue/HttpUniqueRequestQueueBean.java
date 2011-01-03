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

package org.infoglue.deliver.util.ioqueue;

import java.util.HashMap;
import java.util.Map;

import org.infoglue.cms.entities.content.ContentCategoryVO;
import org.infoglue.cms.util.DomainUtils;

public class HttpUniqueRequestQueueBean
{
	private String urlAddress;
	private Map requestProperties 	= new HashMap();
	private Map requestParameters 	= new HashMap();
	private String serializedParameters = null;
	private String encoding 		= "iso-8859-1";
	private int timeout 			= 30000;
	private IOFetcher fetcher		= null;
	private IOResultHandler	handler	= null;

	public String getUrlAddress()
	{
		return urlAddress;
	}
	public void setUrlAddress(String urlAddress)
	{
		this.urlAddress = urlAddress;
	}
	public Map getRequestProperties()
	{
		return requestProperties;
	}
	public void setRequestProperties(Map requestProperties)
	{
		this.requestProperties = requestProperties;
	}
	public Map getRequestParameters()
	{
		return requestParameters;
	}
	public void setRequestParameters(Map requestParameters)
	{
		this.requestParameters = requestParameters;
	}
	public String getSerializedParameters()
	{
		return serializedParameters;
	}
	public void setSerializedParameters(String serializedParameters)
	{
		this.serializedParameters = serializedParameters;
	}
	
	public String getEncoding()
	{
		return encoding;
	}
	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}
	public int getTimeout()
	{
		return timeout;
	}
	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}
	public IOResultHandler getHandler()
	{
		return handler;
	}
	public void setHandler(IOResultHandler handler)
	{
		this.handler = handler;
	}
	public IOFetcher getFetcher()
	{
		return fetcher;
	}
	public void setFetcher(IOFetcher fetcher)
	{
		this.fetcher = fetcher;
	}

    public boolean equals(Object o)
    {
    	HttpUniqueRequestQueueBean other = (HttpUniqueRequestQueueBean)o;
    	if(other.getUrlAddress().equals(urlAddress) && other.getSerializedParameters().equals(serializedParameters) && other.getEncoding().equals(encoding))
    		return true;
    	else
    		return false;
    }
    
    public int hashCode()
    {
    	return (urlAddress + serializedParameters + encoding).hashCode();
    }

}
