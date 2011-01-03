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
package org.infoglue.cms.applications.workflowtool.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Bean class used for populating content version attributes from
 * the request and/or the property set.
 */
public class ContentVersionValues 
{
	/**
	 * A mapping from attribute names to attribute values.
	 */
	private final Map values = new HashMap(); // <String> -> <String>
	
	/**
	 * Default constructor. 
	 */
	public ContentVersionValues() 
	{ 
		super(); 
	}
	
	/**
	 * Returns the value of the attribute with the specified name. 
	 * 
	 * @param name the name of the attribute.
	 * @return the value of the attribute with the specified name. 
	 */
	public String get(final String name) 
	{ 
		return (String) values.get(name); 
	}

	/**
	 * Sets the value of the specified attribute.
	 * 
	 * @param name the name of the attribute.
	 * @param value the new value.
	 */
	public void set(final String name, final String value) 
	{ 
		values.put(name, value == null ? "" : value); 
	}

	/**
	 * Returns true if the attribute with the specified name exists; false otherwise.
	 * 
	 * @param the name of the attribute.
	 * @return true if the attribute with the specified name exists; false otherwise.
	 */
	public boolean contains(final String name) 
	{ 
		return values.containsKey(name); 
	}
}