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

package org.infoglue.cms.applications.databeans;

import java.util.LinkedHashMap;

/**
 * This is a pure javabean carrying the information about one content type attribute parameter.
 * The parameters are used to carry information about a certain extra parameter belonging to an 
 * attribute. For example - a simple attribute might have attributes steering which default value it
 * should have or which label the attribute should have visible to the user. To support for example 
 * lists of values in a parameter the lists are allways used.
 */ 


public class ContentTypeAttributeParameter
{
	public static final int SINGLE_VALUE_TYPE = 0;
	public static final int MULTI_VALUE_TYPE = 1;
	
	private String id = "";
	private int type = SINGLE_VALUE_TYPE; 	
	private LinkedHashMap contentTypeAttributeParameterValues = new LinkedHashMap();
	
	public ContentTypeAttributeParameter()
	{
	}	
	
	public String getId()
	{
		return this.id;
	}

	public void setId(String id)
	{
		this.id = id;
	} 

	public int getType()
	{
		return this.type;
	}
	
	public void setType(int type)
	{
		this.type = type;
	}

	public void addContentTypeAttributeParameterValue(String key, ContentTypeAttributeParameterValue contentTypeAttributeParameterValue)
	{
		this.contentTypeAttributeParameterValues.put(key, contentTypeAttributeParameterValue);
		if(this.contentTypeAttributeParameterValues.size() > 1)
			this.type = MULTI_VALUE_TYPE;
	}

	public ContentTypeAttributeParameterValue getContentTypeAttributeParameterValue()
	{
		if(this.contentTypeAttributeParameterValues.values() != null && this.contentTypeAttributeParameterValues.values().toArray().length > 0)
			return (ContentTypeAttributeParameterValue)this.contentTypeAttributeParameterValues.values().toArray()[0];
		else
			return null; 
	}

	public ContentTypeAttributeParameterValue getContentTypeAttributeParameterValue(String key)
	{
		if(this.contentTypeAttributeParameterValues.size() > 0)
			return (ContentTypeAttributeParameterValue)this.contentTypeAttributeParameterValues.get(key);
		else
			return null; 
	}
	
	public LinkedHashMap getContentTypeAttributeParameterValues()
	{
		return this.contentTypeAttributeParameterValues;
	}
	
	

}