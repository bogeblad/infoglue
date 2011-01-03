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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This javabean carries information about one attributes parameters values. For example an
 * attribute can have a possible parameter called category. Then the category can have multiple
 * possible values, for example division, global, local and so on. Each of these values can have
 * multiple representations visually. Language-dependency is one obvious. All variations are kept in this bean. 
 */ 


public class ContentTypeAttributeParameterValue
{
	private String id = null;
	private Map attributes = new HashMap();
	
	public ContentTypeAttributeParameterValue()
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

	public Map getAttributes()
	{
		return this.attributes;
	}

	public void addAttribute(String key, String value)
	{
		this.attributes.put(key, value);
	}

	public void setAttributes(Map attributes)
	{
		this.attributes = attributes;
	}
	
	public String getValue(String key)
	{
		return (String)this.attributes.get(key);
	}
		
	public String getLocalizedValue(String key, Locale locale)
	{
		String localizedKey = key + "_" + locale.getLanguage();
		if(this.attributes.containsKey(localizedKey))
			return (String)this.attributes.get(localizedKey);
		else
			return (String)this.attributes.get(key);
	}
	
	public String getLocalizedValue(String key, String langugeCode)
	{
		String localizedKey = key + "_" + langugeCode;
		if(this.attributes.containsKey(localizedKey))
			return (String)this.attributes.get(localizedKey);
		else
			return (String)this.attributes.get(key);
	}

	public int getLocalizedValueAsInt(String key, Locale locale)
	{
		try
		{
			String localizedKey = key + "_" + locale.getLanguage();
			if(this.attributes.containsKey(localizedKey))
				return Integer.parseInt((String)this.attributes.get(localizedKey));
			else
				return Integer.parseInt((String)this.attributes.get(key));
		}
		catch(Exception e)
		{
			return 0;
		}
	}
	
	public int getLocalizedValueAsInt(String key, String langugeCode)
	{
		try
		{
			String localizedKey = key + "_" + langugeCode;
			if(this.attributes.containsKey(localizedKey))
				return Integer.parseInt((String)this.attributes.get(localizedKey));
			else
				return Integer.parseInt((String)this.attributes.get(key));
		}
		catch(Exception e)
		{
			return 0;
		}

	}

}