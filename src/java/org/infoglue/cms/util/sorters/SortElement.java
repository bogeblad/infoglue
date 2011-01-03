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

import org.apache.commons.beanutils.PropertyUtils;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;

/**
 * A <code>SortElement</code> object is created for each content/content version
 * to be sorted. If the <code>SortElement</code> is constructed using a content 
 * and the content version is needed, the content version to use is decided by
 * fetching the language used by the <code>TemplateController</code>.
 * <p>
 * Strictly speaking we never sort on a content or a content version, we are
 * sorting on a content <strong>and</strong> the content version indicated by the
 * language used by the <code>TemplateController</code> at the same time.
 * </p>
 */

public class SortElement implements Comparable 
{
	/**
	 * The controller to use when interacting with the model.
	 */
	private final TemplateController controller;
	
	/**
	 * The content version to sort.
	 */
	private ContentVersionVO contentVersionVO;

	/**
	 * The content to sort.
	 */
	private ContentVO contentVO;
	
	/**
	 * The comparable of the element to sort. 
	 */
	private final CompoundComparable comparable = new CompoundComparable(); 

	/**
	 * Constructs a sort element for the specified content.
	 * 
	 * @param contentVO the content to sort.
	 * @param controller the controller to use when interacting with the model.
	 */
	SortElement(final TemplateController controller, final ContentVO contentVO) 
	{
		this.controller = controller;
		this.contentVO  = contentVO;
	}

	/**
	 * Constructs a sort element for the specified content version.
	 * 
	 * @param contentVO the content to sort.
	 * @param controller the controller to use when interacting with the model.
	 */
	SortElement(final TemplateController controller, final ContentVersionVO contentVersionVO) 
	{
		this.controller       = controller;
		this.contentVersionVO = contentVersionVO;
	}

	/**
	 * Use the specified content property in the sort.
	 * 
	 * @paran name the name of the content property.
	 * @param ascending indicates the sort order to use when sorting on the specified property.
	 */
	public void addContentProperty(final String name, final boolean ascending) 
	{
		comparable.add(getProperty(getContentVO(), name), ascending);
	}

	/**
	 * Use the specified content version property in the sort.
	 * 
	 * @paran name the name of the content version property.
	 * @param ascending indicates the sort order to use when sorting on the specified property.
	 */
	public void addContentVersionProperty(final String name, final boolean ascending) 
	{
		comparable.add(getProperty(getContentVersionVO(), name), ascending);
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
		final Integer contentId  = getContentId();
		String stringValue = controller.getContentAttribute(contentId, controller.getLanguageId(), name);
		if(!caseSensitive)
			stringValue = stringValue.toLowerCase();
		
		comparable.add(castAttribute(name, clazz, stringValue), ascending);
	}

	/**
	 * Cast the specified string value to the specified class (which must be a <code>Comparable</code>.
	 * 
	 * @param name the name of the content version attribute (used for debug only).
	 * @param clazz the class to cast to. Must implement the <code>Comparable</code> interface.
	 * @param stringValue the value to cast.
	 * @throws IllegalArgumentException if the cast fails.
	 */
	private Comparable castAttribute(final String name, final Class clazz, final String stringValue) 
	{
		if(String.class.equals(clazz))
		{
			return stringValue;
		}
		try 
		{
			final Constructor ctor = clazz.getConstructor(new Class[] { String.class });
			final String s = (Number.class.isAssignableFrom(clazz) && stringValue.equals("")) ? "0" : stringValue;
			return (Comparable) ctor.newInstance(new Object[] { s });
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
			throw new IllegalArgumentException("Unable to cast [" + name + "] to [" + clazz.getName() + "].");
		}
	}
	
	/**
	 * Returns the identifier of the content element.
	 * 
	 * @return the identifier.
	 */
	private Integer getContentId() 
	{
		return (contentVO != null) ? getContentVO().getContentId() : getContentVersionVO().getContentId();
	}
	
	/**
	 * Returns the value of the specified property of the specified object.
	 * 
	 * @param o object whose property is to be extracted.
	 * @param name the name of the property to be extracted.
	 * @returns the value of the specified property of the specified object.
	 */
	private Comparable getProperty(final Object o, final String name) 
	{
		try 
		{
			return (Comparable) PropertyUtils.getProperty(o, name);
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
			throw new IllegalArgumentException("Illegal property [" + name + "] : " + e);
		}
	}
	
	/**
	 * Returns the content version value object. This is a convenience method as we don't
	 * if the element was constructed using a content or a content version.
	 * 
	 * @return the content version value object.
	 */
	public ContentVersionVO getContentVersionVO() 
	{
		if(contentVersionVO == null) 
		{
			contentVersionVO = controller.getContentVersion(contentVO.getContentId());
		}
		return contentVersionVO;
	}
	
	/**
	 * Returns the content value object. This is a convenience method as we don't
	 * if the element was constructed using a content or a content version.
	 * 
	 * @return the content value object.
	 */
	public ContentVO getContentVO() 
	{
		if(contentVO == null)
		{
			contentVO = controller.getContent(contentVersionVO.getContentId());
		}
		return contentVO;
	}

	/**
	 * Compares two <code>SortStruct</code> objects by comparing their <code>SortComparable</code>:s.
	 * 
	 * @param o the object to ne compared.
	 * @return a negative integer, zero, or a positive integer if this object is less than, 
	 *         equal to, or greater than the specified object.
	 */
	public final int compareTo(Object o) 
	{
		if(!(o instanceof SortElement))
		{
			throw new ClassCastException();
		}
		final SortElement other = (SortElement) o;
		return comparable.compareTo(other.comparable);
	}
}