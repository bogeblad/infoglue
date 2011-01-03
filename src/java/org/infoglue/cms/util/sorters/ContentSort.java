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
package org.infoglue.cms.util.sorters;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;

/**
 * A <code>CompoundComparable</code> is a compound comparable.
 */
class CompoundComparable implements Comparable 
{
	/**
	 * The list of contained comparables.
	 */
	private final List comparables = new ArrayList();

	/**
	 * Each order in this list indicates how the comparable at the same position should be sorted (ascending or not).
	 */
	private final List orders = new ArrayList(); // type: <Boolean>
	
	/**
	 * Compares two <code>CompoundComparable</code> objects by comparing all
	 * contained comparable against each other.
	 * 
	 * @param o the object to ne compared.
	 * @return a negative integer, zero, or a positive integer if this object is less than, 
	 *         equal to, or greater than the specified object.
	 */
	public final int compareTo(final Object o) 
	{
		if(!(o instanceof CompoundComparable))
		{
			throw new ClassCastException();
		}
		final CompoundComparable other = (CompoundComparable) o;
		if(other.comparables.size() != comparables.size())
		{
			throw new IllegalStateException("Trying to compare SortComparable with different number of elements.");
		}
		for(int i=0; i<comparables.size(); i++) 
		{
			final int result = compareTo(other, i);
			if(result != 0)
			{
				return result;
			}
		}
		return 0;
	}
	
	/**
	 * Compares the n:th comparable of two <code>SortComparable</code> objects.
	 * 
	 * @param other the other <code>CompoundComparable</code> object.
	 * @param index indicates which contained comparable to compare.
	 * @return a negative integer, zero, or a positive integer if the n:th comparable of this object 
	 *         is less than, equal to, or greater than the n:th comparable of the specified object.
	 */
	private final int compareTo(final CompoundComparable other, final int index) 
	{
		final Comparable c1      = (Comparable) comparables.get(index);
		final Comparable c2      = (Comparable) other.comparables.get(index);
		final Boolean ascending  = (Boolean) orders.get(index);
		return ascending.booleanValue() ? c1.compareTo(c2) : c2.compareTo(c1);
	}
	
	/**
	 * Adds the specified comparable. The comparable will be sorted in the specified order.
	 * 
	 * @param c the comparable to add.
	 * @param ascending indicates the sort order of the comparable.
	 */
	public final void add(final Comparable c, final boolean ascending) 
	{
		comparables.add(c);
		orders.add(new Boolean(ascending));
	}
}


/**
 * Utility class for sorting content/content version objects. 
 * Any number of properties and/or attributes of the content/content versions can be used in the sort.
 */
public class ContentSort 
{
	/**
	 * The controller to use when interacting with the model.
	 */
	TemplateController controller;
	
	/**
	 * The elements to sort.
	 */
	private final List elements = new ArrayList(); // type: <SortElement>
	
	/**
	 * Constructs a sorter for the specified elements.
	 * 
	 * @param controller the controller to use when interacting with the model.
	 * @param elements the list of objects to sort.
	 */
	public ContentSort(final TemplateController controller, final Collection elements) 
	{
		this.controller = controller;
		addElements(elements);
	}
	
	public void clear()
	{
		this.controller = null;
		this.elements.clear();
	}
	
	/**
	 * Sets the elements to sort.
	 * 
	 * @param elements the list of objects to sort.
	 */
	private final void addElements(final Collection elements) 
	{
		if(elements == null || elements.isEmpty())
		{
			return;
		}
		final Object element = elements.toArray()[0];
		if(element instanceof Content)
		{
			initializeWithContent(elements);
		}
		if(element instanceof ContentVO)
		{
			initializeWithContentVO(elements);
		}
		if(element instanceof ContentVersion)
		{
			initializeWithContentVersion(elements);
		}
		if(element instanceof ContentVersionVO)
		{
			initializeWithContentVersionVO(elements);
		}
	}
	
	/**
	 * Sets the elements to sort.
	 * 
	 * @param the list of <code>Content</code> objects to sort.
	 */
	private final void initializeWithContent(final Collection elements) 
	{
		for(final Iterator i=elements.iterator(); i.hasNext(); )
		{
			addElement(((Content) i.next()).getValueObject());
		}
	}
	
	/**
	 * Sets the elements to sort.
	 * 
	 * @param the list of <code>ContentVO</code> objects to sort.
	 */
	private final void initializeWithContentVO(final Collection elements) 
	{
		for(final Iterator i=elements.iterator(); i.hasNext(); )
		{
			addElement((ContentVO) i.next());
		}
	}
	
	/**
	 * Sets the elements to sort.
	 * 
	 * @param the list of <code>ContentVersion</code> objects to sort.
	 */
	private final void initializeWithContentVersion(final Collection elements) 
	{
		for(final Iterator i=elements.iterator(); i.hasNext(); )
		{
			addElement(((ContentVersion) i.next()).getValueObject());
		}
	}
	
	/**
	 * Sets the elements to sort.
	 * 
	 * @param the list of <code>ContentVersionVO</code> objects to sort.
	 */
	private final void initializeWithContentVersionVO(final Collection elements) 
	{
		for(final Iterator i=elements.iterator(); i.hasNext(); )
		{
			addElement((ContentVersionVO) i.next());
		}
	}
	
	/**
	 * Use the specified content property in the sort.
	 * 
	 * @paran name the name of the content property.
	 * @param ascending indicates the sort order to use when sorting on the specified property.
	 */
	public void addContentProperty(final String name, final boolean ascending) 
	{
		for(final Iterator i=elements.iterator(); i.hasNext(); )
		{
			((SortElement) i.next()).addContentProperty(name, ascending);
		}
	}
	
	/**
	 * Use the specified content version property in the sort.
	 * 
	 * @paran name the name of the content version property.
	 * @param ascending indicates the sort order to use when sorting on the specified property.
	 */
	public void addContentVersionProperty(final String name, final boolean ascending) 
	{
		for(final Iterator i=elements.iterator(); i.hasNext(); )
		{
			((SortElement) i.next()).addContentVersionProperty(name, ascending);
		}
	}
	
	/**
	 * Use the specified content version attribute in the sort. The type (Comparable) used when
	 * sorting on the attribute will be <code>String</code>.
	 * 
	 * @paran name the name of the content version attribute.
	 * @param ascending indicates the sort order to use when sorting on the specified attribute.
	 */
	public void addContentVersionAttribute(final String name, final boolean ascending, final boolean caseSensitive) 
	{
		addContentVersionAttribute(name, String.class, ascending, caseSensitive);
	}
	
	/**
	 * Use the specified content version attribute in the sort. The type (Comparable) used when
	 * sorting on the attribute will be the specified class.
	 * 
	 * @paran name the name of the content version attribute.
	 * @param className indicates the name of the <code>Comparable</code> to use when sorting on the attribute.
	 * @param ascending indicates the sort order to use when sorting on the specified attribute.
	 */
	public void addContentVersionAttribute(final String name, final String className, final boolean ascending, final boolean caseSensitive) 
	{
		try 
		{
			addContentVersionAttribute(name, Class.forName(className), ascending, caseSensitive);
		} 
		catch(ClassNotFoundException e) 
		{
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		}
	}
	
	/**
	 * Use the specified content version attribute in the sort. The type (Comparable) used when
	 * sorting on the attribute will be the specified class.
	 * 
	 * @paran name the name of the content version attribute.
	 * @param clazz indicates the <code>Comparable</code> to use when sorting on the attribute.
	 * @param ascending indicates the sort order to use when sorting on the specified attribute.
	 */
	public void addContentVersionAttribute(final String name, final Class clazz, final boolean ascending, final boolean caseSensitive) 
	{
		for(final Iterator i=elements.iterator(); i.hasNext(); )
		{
			((SortElement) i.next()).addContentVersionAttribute(name, clazz, ascending, caseSensitive);
		}
	}

	/**
	 * Adds the specified <code>ContentVO</code> object to the elements to sort.
	 * 
	 * @param contentVO the element to sort.
	 */
	private void addElement(final ContentVO contentVO) 
	{
		elements.add(new SortElement(controller, contentVO));
	}
	
	/**
	 * Adds the specified <code>ContentVersionVO</code> object to the elements to sort.
	 * 
	 * @param contentVO the element to sort.
	 */
	private void addElement(final ContentVersionVO contentVersionVO) 
	{
		elements.add(new SortElement(controller, contentVersionVO));
	}
	
	/**
	 * Returns a list of sorted <code>ContentVO</code> objects.
	 * 
	 * @return the sorted list.
	 */
	public List getContentResult() 
	{
		Collections.sort(elements);
		final List result = new ArrayList();
		for(final Iterator i=elements.iterator(); i.hasNext(); ) 
		{
			final SortElement struct = (SortElement) i.next();
			result.add(struct.getContentVO());
		}
		return result;
	}

	
	/**
	 * Returns a list of sorted <code>ContentVO</code> objects.
	 * @param sorts the list using the comparatorClass specified. The comparator must implement the interface org.infoglue.cms.util.sorters.TemplateControllerAwareComparator
	 * @return the sorted list.
	 */
	public List getContentResult(String comparatorClass) 
	{
		TemplateControllerAwareComparator comp = null;
		if(comparatorClass!=null && !comparatorClass.equals("")) 
		{
			try 
			{
				Class clazz = Class.forName(comparatorClass);
				if(clazz!=null) 
				{					
					try 
					{
						comp = (TemplateControllerAwareComparator) clazz.newInstance();
						comp.setController(this.controller);
					} 
					catch (InstantiationException e) 
					{
						throw new IllegalArgumentException("Couldnt instantiate comparator class " + comparatorClass + " " + e.getMessage());
					} catch (IllegalAccessException e) 
					{
						throw new IllegalArgumentException("Couldnt access comparator class " + comparatorClass + " " + e.getMessage());
					}
				}
			} 
			catch (ClassNotFoundException e) 
			{
				throw new IllegalArgumentException("Couldnt find comparator class " + comparatorClass + " " + e.getMessage());
			}
		}
		else 
		{
			throw new IllegalArgumentException("Must specify a comparator classname");
		}

		Collections.sort(elements,comp);
		
		final List result = new ArrayList();
		for(final Iterator i=elements.iterator(); i.hasNext(); ) 
		{
			final SortElement struct = (SortElement) i.next();
			result.add(struct.getContentVO());
		}
		
		return result;
	}

	/**
	 * Returns a list of sorted <code>ContentVersionVO</code> objects.
	 * 
	 * @return the sorted list.
	 */
	public List getContentVersionResult() 
	{
		Collections.sort(elements);
		final List result = new ArrayList();
		for(final Iterator i=elements.iterator(); i.hasNext(); ) 
		{
			final SortElement struct = (SortElement) i.next();
			result.add(struct.getContentVersionVO());
		}
		return result;
	}
}
