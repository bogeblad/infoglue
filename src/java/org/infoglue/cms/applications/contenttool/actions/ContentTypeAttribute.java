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

package org.infoglue.cms.applications.contenttool.actions;

import java.util.Hashtable;

/**
 * This is a pure javabean carrying the information about one content type attribute.
 */ 

public class ContentTypeAttribute
{
	private String name = "";
	private String inputType = "";
	private Hashtable extraParams;
	
	public ContentTypeAttribute()
	{
		extraParams = new Hashtable();
	}
	
	public String putExtraParam(String key, String value)
	{
		return (String) extraParams.put(key, value);
	}
	public String getExtraParam(String key)
	{
		return (String) extraParams.get(key);
	}
	
	public void setName(String name)
	{
		this.name = name;
	} 
	
	public void setInputType(String inputType)
	{
		this.inputType = inputType;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getInputType()
	{
		return this.inputType;
	}
	
	
	
}