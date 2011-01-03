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
package org.infoglue.cms.applications.workflowtool.function;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.infoglue.cms.applications.workflowtool.util.RangeCheck;

import com.opensymphony.workflow.WorkflowException;


/**
 * 
 */
public class CategoryErrorPopulator extends ErrorPopulator 
{
	/**
	 * 
	 */
	private static final String NAME_ARGUMENT = "name";
	
	/**
	 * 
	 */
	private static final String CATEGORY_ERROR_PROPERTYSET_PREFIX = ERROR_PROPERTYSET_PREFIX + "category_";
	
	/**
	 * 
	 */
	private static final String NON_DEFAULT_NAME_ARGUMENT = "nonDefaultName";
	
	/**
	 * 
	 */
	private static final String MIN_ARGUMENT = "min";
	
	/**
	 * 
	 */
	private static final String MAX_ARGUMENT = "max";
	
	/**
	 * 
	 */
	private static final String EXACTLY_MESSAGE_KEY = "3601";

	/**
	 * 
	 */
	private static final String EXACTLY_ONE_MESSAGE_KEY = "3602";
	
	/**
	 * 
	 */
	private static final String LESS_THAN_MESSAGE_KEY = "3603";
	
	/**
	 * 
	 */
	private static final String GREATER_THAN_MESSAGE_KEY = "3604";

	/**
	 * 
	 */
	private static final String GREATER_THAN_ONE_MESSAGE_KEY = "3605";
	
	/**
	 * 
	 */
	private static final String BETWEEN_MESSAGE_KEY = "3606";
	
	/**
	 * 
	 */
	private static final String BETWEEN_ONE_AND_MANY_MESSAGE_KEY = "3607";
	
	/**
	 * 
	 */
	private Map categories;
	
	/**
	 * 
	 */
	private String attributeName;
	
	/**
	 * 
	 */
	private RangeCheck range;
	
	
	
	/**
	 * 
	 */
	public CategoryErrorPopulator() 
	{
		super();
	}
	
	/**
	 * 
	 */
	protected void clean() throws WorkflowException
	{
		clean(getErrorKey());
	}
	
	/**
	 * 
	 */
	protected void populate() throws WorkflowException
	{
		final int count = getCategoryCount();
		final int result = range.check(count);
		if(result != RangeCheck.OK)
		{
			setPropertySetString(getErrorKey(), getErrorKey(result));
		}
	}

	/**
	 * 
	 */
	private String getErrorKey(final int result) throws WorkflowException
	{
		switch(result)
		{
		case RangeCheck.EXACTLY:
			return getStringManager().getString(EXACTLY_MESSAGE_KEY, range.getMin());
		case RangeCheck.EXACTLY_ONE:
			return getStringManager().getString(EXACTLY_ONE_MESSAGE_KEY, range.getMin());
		case RangeCheck.LESS_THAN:
			return getStringManager().getString(LESS_THAN_MESSAGE_KEY, range.getMax());
		case RangeCheck.AT_LEAST:
			return getStringManager().getString(GREATER_THAN_MESSAGE_KEY, range.getMin());
		case RangeCheck.AT_LEAST_ONE:
			return getStringManager().getString(GREATER_THAN_ONE_MESSAGE_KEY, range.getMin());
		case RangeCheck.BETWEEN:
			return getStringManager().getString(BETWEEN_MESSAGE_KEY, range.getMin(), range.getMax());
		case RangeCheck.BETWEEN_ONE_AND_MANY:
			return getStringManager().getString(BETWEEN_ONE_AND_MANY_MESSAGE_KEY, range.getMin(), range.getMax());
		default:
			throwException("Illegal result value [" + result + "]");
		}
		return null;
	}
	
	/**
	 * 
	 */
	private int getCategoryCount()
	{
		final Collection category = (Collection) categories.get(attributeName);
		return (category == null) ? 0 : category.size();
	}
	
	/**
	 * 
	 */
	protected void initialize() throws WorkflowException 
	{
		super.initialize();
		categories = (Map) getParameter(CategoryProvider.CATEGORIES_PARAMETER, new HashMap());
		attributeName = getArgument(NAME_ARGUMENT);
		final Integer min = getIntegerArgument(MIN_ARGUMENT);
		final Integer max = getIntegerArgument(MAX_ARGUMENT);
		checkArguments(min, max);
		range = new RangeCheck(min, max);
	}
	
	/**
	 * 
	 */
	private String getErrorKey() throws WorkflowException
	{ 
		return CATEGORY_ERROR_PROPERTYSET_PREFIX + (argumentExists(NON_DEFAULT_NAME_ARGUMENT) ? getArgument(NON_DEFAULT_NAME_ARGUMENT) : attributeName);	
	}
	
	/**
	 * 
	 */
	private Integer getIntegerArgument(final String key) throws WorkflowException 
	{
		if(argumentExists(key))
		{
			try 
			{
				return new Integer(getArgument(key));
			}
			catch(Exception e)
			{
				throwException(e);
			}
		}
		return null;
	}
	
	/**
	 * 
	 */
	private void checkArguments(final Integer min, final Integer max) throws WorkflowException
	{
		if(min != null && min.intValue() < 0)
		{
			throwException("min must be a natural.");
		}
		if(max != null && max.intValue() < 0)
		{
			throwException("max must be a natural.");
		}
		if(min != null && max != null && min.intValue() > max.intValue())
		{
			throwException("max must be greater than min.");
		}
	}
}
