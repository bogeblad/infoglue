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

/**
 * Utility class for checking if an integer is within a (possible open-ended) range.
 */
public class RangeCheck 
{
	/**
	 * Return code if the checked value is inside the range.
	 */
	public static final int OK = 0;
	
	/**
	 * Return code if the checked value is outside the range and the range is [x,x].
	 */
	public static final int EXACTLY = 1;
	
	/**
	 * Return code if the checked value is outside the range and the range is [1,1].
	 */
	public static final int EXACTLY_ONE = 2;
	
	/**
	 * Return code if the checked value is outside the range and the range is ],x].
	 */
	public static final int LESS_THAN = 3;
	
	/**
	 * Return code if the checked value is outside the range and the range is [x,[.
	 */
	public static final int AT_LEAST = 4;

	/**
	 * Return code if the checked value is outside the range and the range is [1,[.
	 */
	public static final int AT_LEAST_ONE = 5;
	
	/**
	 * Return code if the checked value is outside the range and the range is [x,y].
	 */
	public static final int BETWEEN = 6;
	
	/**
	 * Return code if the checked value is outside the range and the range is [1,y].
	 */
	public static final int BETWEEN_ONE_AND_MANY = 7;
	
	/**
	 * The lower limit (a null value indicates an open end).
	 */
	private final Integer min;
	
	/**
	 * The upper limit (a null value indicates an open end).
	 */
	private final Integer max;
	
	/**
	 * Constructs an object with the specified limits.
	 * 
	 *  @param min the lower limit; a null value indicates an open end.
	 *  @param min the upper limit; a null value indicates an open end.
	 */
	public RangeCheck(final Integer min, final Integer max)
	{
		this.min = min;
		this.max = max;
	}
	
	/**
	 * Returns the lower limit or null if open-ended.
	 * 
	 * @return the lower limit or null if open-ended.
	 */
	public final Integer getMin()
	{
		return min;
	}
	
	/**
	 * Returns the upper limit or null if open-ended.
	 * 
	 * @return the upper limit or null if open-ended.
	 */
	public final Integer getMax()
	{
		return max;
	}
	
	/**
	 * Checks if the specified value is within the range.
	 * 
	 * @param value the value to check.
	 * @return a code representing the result.
	 */
	public final int check(final int value)
	{
		boolean one = (min != null && min.intValue() == 1);
		if(min == null && max == null)
		{
			return OK;
		}
		if(min == null)
		{
			return (value > max.intValue()) ? LESS_THAN : OK;
		}
		if(max == null)
		{
			return (value < min.intValue()) ? (one ? AT_LEAST_ONE : AT_LEAST) : OK;
		}
		if(min.equals(max) && value != min.intValue())
		{
			return one ? EXACTLY_ONE : EXACTLY;
		}
		if(value < min.intValue() || value > max.intValue())
		{
			return one ? BETWEEN_ONE_AND_MANY : BETWEEN;
		}
		return OK;
	}
}
