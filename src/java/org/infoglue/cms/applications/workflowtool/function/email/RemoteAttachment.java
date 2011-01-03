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
package org.infoglue.cms.applications.workflowtool.function.email;

import java.util.ArrayList;
import java.util.List;

import org.apache.axis.encoding.Base64;
import org.infoglue.deliver.util.webservices.DynamicWebserviceElement;

/**
 * 
 */
public final class RemoteAttachment extends AbstractAttachment implements DynamicWebserviceElement 
{
	/**
	 * 
	 */
	public RemoteAttachment()
	{
	}
	
	/**
	 * 
	 */
	public RemoteAttachment(final String name, final String contentType, final byte[] bytes)
	{
		super(name, contentType, bytes);
	}
	
	/**
	 * 
	 */
	public List serialize() 
	{
		final List list = new ArrayList();
		list.add(getName());
		list.add(getContentType());
		list.add(Base64.encode(getBytes()));
		return list;
	}

	/**
	 * 
	 */
	public void deserialize(final List list) 
	{
		if(list.size() != 3)
		{
			throw new IllegalArgumentException("Illegal size");
		}
		setName(list.get(0).toString());
		setContentType(list.get(1).toString());
		setBytes(Base64.decode(list.get(2).toString()));
	}
}
