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

package org.infoglue.cms.entities.management;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.infoglue.cms.applications.databeans.GenericOptionDefinition;

/**
 * This is a pure javabean carrying the information about one content type attribute.
 */ 

public class ContentTypeAttribute
{
	/*
	public static final String TEXTFIELD = "textfield";
	public static final String TEXTAREA = "textarea";
	public static final String TEXTFIELD = "textfield";
	public static final String TEXTFIELD = "textfield";
	public static final String TEXTFIELD = "textfield";
	public static final String TEXTFIELD = "textfield";
	public static final String TEXTFIELD = "textfield";
	public static final String TEXTFIELD = "textfield";
	public static final String TEXTFIELD = "textfield";
	public static final String TEXTFIELD = "textfield";
	*/
	
	private int position 	 = 0;
	private String name 	 = "";
	private String inputType = "";
	private Boolean inherited = false;
	private Map contentTypeAttributeParameters = new LinkedHashMap();
	private List validators  = new ArrayList();
	private List<GenericOptionDefinition> options = new ArrayList<GenericOptionDefinition>();
	
	public ContentTypeAttribute()
	{
	}	

	public int getPosition()
	{
		return this.position;
	}

	public void setPosition(int position)
	{
		this.position = position;
	}
	
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	} 

	public String getInputType()
	{
		return this.inputType;
	}
	
	public void setInputType(String inputType)
	{
		this.inputType = inputType;
	}

	public Boolean getInherited() 
	{
		return inherited;
	}

	public void setInherited(Boolean inherited) 
	{
		this.inherited = inherited;
	}
	
	public void putContentTypeAttributeParameter(String key, ContentTypeAttributeParameter contentTypeAttributeParameter)
	{
		this.contentTypeAttributeParameters.put(key, contentTypeAttributeParameter);
	}
	
	public ContentTypeAttributeParameter getContentTypeAttribute(String key)
	{
		return (ContentTypeAttributeParameter)contentTypeAttributeParameters.get(key);
	}

	public List getContentTypeAttributeParameters()
	{
		List extraParameters = new ArrayList();
		for (Iterator i = contentTypeAttributeParameters.entrySet().iterator(); i.hasNext();) 
		{
			Map.Entry e = (Map.Entry) i.next();
			extraParameters.add(e);
		}
				
		return extraParameters;
	}
	
    public List getValidators()
    {
        return validators;
    }

    public List<GenericOptionDefinition> getOptions()
    {
    	return options;
    }

}