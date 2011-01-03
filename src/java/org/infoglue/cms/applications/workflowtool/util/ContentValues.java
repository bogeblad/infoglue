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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Bean class used for populating content attributes from the request and/or the property set.
 */
public class ContentValues 
{
	/**
	 * The key used when populating the publish date attribute.
	 */
	public static final String PUBLISH_DATE_TIME = "PublishDateTime";

	/**
	 * The key used when populating the expire date attribute.
	 */
	public static final String EXPIRE_DATE_TIME = "ExpireDateTime";

	/**
	 * The key used when populating the name attribute.
	 */
	public static final String NAME = "Name";
	
	/**
	 * The publish date of the content.
	 */
	private Date publishDateTime;

	/**
	 * The expire date of the content.
	 */
	private Date expireDateTime;

	/**
	 * The name of the content. 
	 */
	private String name;
	
	/**
	 * Default constructor.
	 */
	public ContentValues() 
	{ 
		super(); 
	}

	/**
	 * Returns the name of the content.
	 * 
	 * @return the name of the content.
	 */
	public String getName() 
	{ 
		return name; 
	}
	
	/**
	 * Returns the publish date of the content.
	 * 
	 * @return the publish date of the content.
	 */
	public Date getPublishDateTime() 
	{ 
		return publishDateTime; 
	}

	/**
	 * Returns the expire date of the content.
	 * 
	 * @return the expire date of the content.
	 */
	public Date getExpireDateTime() 
	{ 
		return expireDateTime; 
	}

	/**
	 * Sets the name of the content to the specified value.
	 * 
	 * @param name the new name.
	 */
	public void setName(final String name) 
	{
		this.name = name;
	}
	
	/**
	 * Sets the publish date of the content to the specified value.
	 * 
	 * @param name the new name.
	 */
	public void setPublishDateTime(final String publishDateTime) 
	{
		this.publishDateTime = getDate(publishDateTime);
	}
	
	/**
	 * Sets the expire date of the content to the specified value.
	 * 
	 * @param name the new name.
	 */
	public void setExpireDateTime(final String expireDateTime) 
	{
		this.expireDateTime = getDate(expireDateTime);
	}

	/**
	 * Converts the specified string to a date or null if the string is unparsable.
	 * 
	 * @param dateString the string to parse.
	 * @return the date.
	 */
	private static Date getDate(final String dateString) 
	{
		try 
		{
			return (dateString == null) ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dateString);
		} 
		catch(Exception e) 
		{
			return null;
		}
	}
}