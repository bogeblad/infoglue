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

package org.infoglue.cms.util.validators;

/**
 * ConstraintRule.java
 * Created on 2002-sep-13 
 * @author Stefan Sik, ss@frovi.com 
 * ss
 * 
 * 
 * This class defines a constraint rule for a specified field.
 * 
 */
public class ConstraintRule
{
	// Private storage
	private int constraintType;		
	private String fieldName;
	private Object value = null;
	private Range validRange = new Range();


	// Public options	
	public boolean unique = false;
	public boolean required = false;

	// Constructor
	public ConstraintRule(int constraintType, String fieldName)
	{
		this.constraintType = constraintType;
		this.fieldName = fieldName;
	}

	// Getters

	public int getConstraintType()
	{
		return constraintType;
	}
	public String getFieldName()
	{
		return fieldName;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	public Range getValidRange()
	{
		return validRange;
	}

	public void setValidRange(Range validRange)
	{
		this.validRange = validRange;
	}

	/**
	 * Returns the required.
	 * @return boolean
	 */
	public boolean isRequired()
	{
		return required;
	}

	/**
	 * Returns the unique.
	 * @return boolean
	 */
	public boolean isUnique()
	{
		return unique;
	}

	/**
	 * Sets the required.
	 * @param required The required to set
	 */
	public void setRequired(boolean required)
	{
		this.required = required;
	}

	/**
	 * Sets the unique.
	 * @param unique The unique to set
	 */
	public void setUnique(boolean unique)
	{
		this.unique = unique;
	}

}
