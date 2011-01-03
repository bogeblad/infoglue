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
package org.infoglue.cms.workflow.taglib;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility class for creating html elements.
 * Most functions returns the element itself to allow chained operations.
 */
class Element 
{
	/**
	 * Pattern for an attribute.
	 */
	private static final String ATTRIBUTE = "{0}=\"{1}\"";
	
	/**
	 * Pattern for an element with both attributes and children.
	 */
	private static final String CHILDREN_AND_ATTRIBUTES = "<{0} {1}>{2}</{0}>";
	
	/**
	 * Pattern for an element with children but no attributes.
	 */
	private static final String CHILDREN_AND_NO_ATTRIBUTES = "<{0}>{1}</{0}>";
	
	/**
	 * Pattern for an element with neither children or attributes.
	 */
	private static final String NO_CHILDREN_NO_ATTRIBUTES = "<{0}></{0}>";
	
	/**
	 * Pattern for an element with attributes but no children.
	 */
	private static final String NO_CHILDREN_AND_ATTRIBUTES = "<{0} {1}></{0}>";
	
	/**
	 * The name of the element.
	 */
	private final String name;
	
	/**
	 * The parent of the element.
	 */
	private Element parent;
	
	/**
	 * The attributes of the element.
	 */
	private final Map attributes = new HashMap();
	
	/**
	 * The children of the element.
	 */
	private final List children = new ArrayList();
	
	/**
	 * Constructs an element with the specified name and with no parent element.
	 * 
	 * @param name the name of the element.
	 */
	public Element(final String name) 
	{
		this(null, name);
	}
	
	/**
	 * Constructs an element with the specified name and parent element.
	 * 
	 * @param parent the parent of the element (null is permitted).
	 * @param name the name of the element.
	 */
	public Element(final Element parent, final String name) 
	{
		super();
		this.parent = parent;
		this.name = name;
	}
	
	/**
	 * Adds an attribute with the specified name and value if the value isn't null. 
	 * If an attribute with the specified name exists, it will be overwritten. 
	 * 
	 * @param name the name of the attribute.
	 * @param value the value of the attribute.
	 * @return the current element.
	 */
	public Element addAttribute(final String name, final String value)
	{
		return addAttribute(name, value, true);
	}
	
	/**
	 * Adds an attribute with the specified name and value if the specified condition
	 * is true and the value isn't null. 
	 * If an attribute with the specified name exists, it will be overwritten.
	 *  
	 * @param name the name of the attribute.
	 * @param value the value of the attribute.
	 * @param condition the condition to check.
	 * @return the current element.
	 */
	public Element addAttribute(final String name, String value, final boolean condition)
	{
		if(condition && value != null)
		{
			Object o = (Object)attributes.get(name);
			if(o != null && o instanceof String)
				value = o + " " + value;
			
			attributes.put(name, value);
		}
		return this;
	}
	
	/**
	 * Creates and adds a child with the specified name to the end of the children list.
	 * The parent of the child will be the current element.
	 * 
	 * @param name the name of the child element.
	 * @return the child element.
	 */
	public Element addChild(final String name) 
	{
		final Element child = new Element(this, name);
		children.add(child);
		return child;
	}

	/**
	 * Creates and adds a child with the specified name to the start of the children list.
	 * The parent of the child will be the current element.
	 * 
	 * @param name the name of the child element.
	 * @return the child element.
	 */
	public Element addChildFirst(final String name) 
	{
		final Element child = new Element(this, name);
		children.add(0, child);
		return child;
	}
	
	/**
	 * Adds the specified child to the end of the children list if non-null.
	 * The parent of the child will be set to the current element.
	 * 
	 * @param child the child element.
	 * @return the current element.
	 */
	public Element addChild(final Element child) 
	{
		if(child != null)
		{
			child.parent = this;
			children.add(child);
		}
		return this;
	}
	
	/**
	 * Creates and adds a text-child to the end of the children list if non-empty.
	 *
	 * @param text the text of the child.
	 * @return the current element.
	 */
	public Element addText(final String text)
	{
		if(text != null && text.length() > 0)
		{
			children.add(text);
		}
		return this;
	}
	
	/**
	 * Returns true if the element has an attribute with the specified name; false otherwise.
	 * 
	 * @return true if the element has an attribute with the specified name; false otherwise.
	 */
	public boolean hasAttribute(final String name)
	{
		return name != null && attributes.containsKey(name);
	}
	
	/**
	 * Returns true if the element has any attributes; false otherwise.
	 * 
	 * @return true if the element has any attributes; false otherwise.
	 */
	public boolean hasAttributes()
	{
		return !attributes.isEmpty();
	}

	/**
	 * Returns true if the element has any children; false otherwise.
	 * 
	 * @return true if the element has any children; false otherwise.
	 */
	public boolean hasChildren()
	{
		return !children.isEmpty();
	}
	
	/**
	 * Returns the parent of the element.
	 * 
	 * @return the parent of the element.
	 */
	public Element pop()
	{
		return parent;
	}
	
	/**
	 * Returns the string representation of the element.
	 * 
	 * @return the string representation of the element.
	 */
	public String toString()
	{
		if(hasAttributes() && hasChildren())
		{
			return MessageFormat.format(CHILDREN_AND_ATTRIBUTES, new Object[] { name, attributesString(), childrenString()});
		}
		if(hasAttributes() && !hasChildren())
		{
			return MessageFormat.format(NO_CHILDREN_AND_ATTRIBUTES, new Object[] { name, attributesString() });
		}
		if(!hasAttributes() && hasChildren())
		{
			return MessageFormat.format(CHILDREN_AND_NO_ATTRIBUTES, new Object[] { name, childrenString() });
		}
		return MessageFormat.format(NO_CHILDREN_NO_ATTRIBUTES, new Object[] { name });
	}
	
	/**
	 * Returns the string representation of the attributes of the element.
	 * 
	 * @return the string representation of the attributes of the element.
	 */
	private String attributesString()
	{
		final StringBuffer sb = new StringBuffer();
		for(final Iterator i = attributes.keySet().iterator(); i.hasNext(); )
		{
			final Object key   = i.next();
			final Object value = attributes.get(key);
			sb.append((sb.length() > 0 ? " " : "") + MessageFormat.format(ATTRIBUTE, new Object[] { key, value }));
		}
		return sb.toString();
	}

	/**
	 * Returns the string representation of the children of the element.
	 * 
	 * @return the string representation of the children of the element.
	 */
	private String childrenString()
	{
		final StringBuffer sb = new StringBuffer();
		for(final Iterator i = children.iterator(); i.hasNext(); )
		{
			sb.append(i.next());
		}
		return sb.toString();
	}
}