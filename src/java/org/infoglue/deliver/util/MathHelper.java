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

package org.infoglue.deliver.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

/**
 * @author ss
 *
 * Various mathematical functions accesible to templateEditors
 */

public class MathHelper 
{
	
	/* Convert a hex string to a decimal value, useful
	 * for color specifications.
	 */
	public int hexToDecimal(String hex) 
	{
		return new Integer(Integer.parseInt(hex,16)).intValue();
	}
	
	/* 
	 * Should be better off in the visual formatter class (ss)
	 */

	public String fileSize(long size)
	{
		return fileSize(size, Locale.getDefault());
	}

	public String fileSize(long size, Locale locale)
	{
		String[] pfix = { "byte", "Kb", "Mb", "Gb" };
		double c = new Long(size).doubleValue();
		int cnt=0;
		
		while (c > 512 && cnt < 3)	{
			c /= 1024; cnt++;
		}
		
		NumberFormat nf = NumberFormat.getInstance(locale);
		if(c > 9) nf.setMaximumFractionDigits(0); else nf.setMaximumFractionDigits(2);
		 
		return nf.format(c) + " " + pfix[cnt];
	}
	
	/* Convert a string value to a Integer object
	 * Useful in template when comparing numbers that
	 * resides in strings.
	 */
	public Integer stringToInteger(String value)
	{
		if(value == null)
			return null;
		
		return new Integer(Integer.parseInt(value));		
	}
	
	/* Convert a string value to a Integer object
	 * Useful in template when comparing numbers that
	 * resides in strings.
	 */
	public Integer floatStringToInteger(String value)
	{
		if(value == null)
			return null;

		return new Integer(new Float(value).intValue());		
	}

	/* Convert a string value to a Float object
	 * Useful in template when comparing numbers that
	 * resides in strings.
	 */
	public Float floatStringToFloat(String value)
	{
		if(value == null)
			return null;

		return new Float(value);		
	}
	
	/**
	 * This method returns a string represented a formatted float value.
	 * The formatting is controlled with the locale and the pattern sent in.
	 */
	
	public String getNumberAsString(Object value, Locale locale, String pattern)
	{
		NumberFormat nf = NumberFormat.getNumberInstance(locale);
		DecimalFormat df = (DecimalFormat)nf;
		df.applyPattern(pattern);
		String output = df.format(value);
		return output;
	}
	
	/**
	 * This method returns a string represented a formatted float value.
	 * The formatting is controlled with the pattern sent in.
	 */
	
	public String getNumberAsString(Object value, String pattern)
	{
		NumberFormat nf = NumberFormat.getNumberInstance();
		DecimalFormat df = (DecimalFormat)nf;
		df.applyPattern(pattern);
		String output = df.format(value);
		return output;
	}

	/* 
	 * Divides to values
	 */

	public Float divide(Float value, int divider)
	{
		if(value == null)
			return null;

		return new Float(value.floatValue() / divider);		
	}
	
	
	/**
	 * Gets a random number.
	 */
	
	public int getRandom()
	{
		Random generator = new Random();
		return generator.nextInt();
	}

	/**
	 * Gets a random number with an upper limit.
	 */
	
	public int getRandom(int upperLimit)
	{
		Random generator = new Random();
		return generator.nextInt(upperLimit);
	}
	
	/**
	 * This method rounds an float to an long.
	 */
	
	public int round(float floatNumber)
	{
		return (int)Math.round(floatNumber);
	}


	/**
	 * This method multiplies two numbers.
	 */
	
	public float multiply(Number first, Number second)
	{
		return first.floatValue() * second.floatValue();
	}
	
	/**
	 * This method multiplies two floats.
	 */
	
	public float multiply(float first, float second)
	{
		return first * second;
	}
	
	/**
	 * This method divides two numbers.
	 */
	
	public float divide(Number first, Number second)
	{
		return first.floatValue() / second.floatValue();
	}
	
	/**
	 * This method divides two numbers.
	 */
	
	public float divide(float first, float second)
	{
		return first / second;
	}
}
