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

import java.util.Hashtable;

/**
 * This bean represents all the data needed in the publication queue.
 * Mainly it contains the url to the deliver instance (the bean is unique to each instance), the request parameters
 * and also the serialized parameters. 
 */
public class PublicationQueueBean
{
	private String urlAddress;
	private Hashtable requestParameters = new Hashtable();
	private String serializedParameters = null;
	
	public String getUrlAddress()
	{
		return urlAddress;
	}
	
	public void setUrlAddress(String urlAddress)
	{
		this.urlAddress = urlAddress;
	}
	
	public Hashtable getRequestParameters()
	{
		return requestParameters;
	}
	
	public void setRequestParameters(Hashtable requestParameters)
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
	
    public boolean equals(Object o)
    {
    	PublicationQueueBean other = (PublicationQueueBean)o;
    	if(other.getUrlAddress().equals(urlAddress) && other.getSerializedParameters().equals(serializedParameters))
    		return true;
    	else
    		return false;
    }
    
    public int hashCode()
    {
    	return (urlAddress + serializedParameters).hashCode();
    }

}
