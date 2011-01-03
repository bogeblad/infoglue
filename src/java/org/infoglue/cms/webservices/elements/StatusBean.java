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

package org.infoglue.cms.webservices.elements;

import java.util.ArrayList;
import java.util.List;

/**
 * This bean contains information about the webservice results if any.
 * For example - if you use the remoteContent-WS this will contain any created ID:s etc.
 */

public class StatusBean //implements DynamicWebserviceElement
{
	private Boolean status = false;
	private String message = "";
	private List<CreatedEntityBean> createdBeans = new ArrayList<CreatedEntityBean>();
	
	public StatusBean()
	{
	}
	
	public StatusBean(Boolean status, String message)
	{
		this.status = status;
		this.message = message;
	}
	
	public Boolean getStatus()
	{
		return status;
	}
	
	public void setStatus(Boolean status)
	{
		this.status = status;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public void setMessage(String message)
	{
		this.message = message;
	}

	public List<CreatedEntityBean> getCreatedBeans()
	{
		return createdBeans;
	}
	
	public void setCreatedBeans(List<CreatedEntityBean> createdBeans)
	{
		this.createdBeans = createdBeans;
	}
	
	/**
	 * 
	 */
	/*
	public List serialize() 
	{
		final List list = new ArrayList();
		list.add(getStatus());
		list.add(getMessage());
		//list.add(getCreatedBeans());
		return list;
	}
	*/

	/**
	 * 
	 */
	
	/* (non-Javadoc)
	 * @see org.infoglue.deliver.util.webservices.DynamicWebserviceElement#deserialize(java.util.List)
	 */
	//public void deserialize(final List list) 
	//{
		/*
		if(list.size() != 3)
		{
			throw new IllegalArgumentException("Illegal size");
		}
		*/
		//setStatus(new Boolean(list.get(0).toString()));
		//setMessage(list.get(1).toString());
		//setCreatedBeans(list.get(2).toString());
	//}
	
}
