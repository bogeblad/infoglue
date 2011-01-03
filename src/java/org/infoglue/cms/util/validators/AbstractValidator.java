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

import java.util.regex.Pattern;

import org.infoglue.cms.controllers.kernel.impl.simple.ValidationController;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;


/**
 * <todo>
 *  Time is running out, the illusion fades away...
 *  This package will be refactored/extended after iteration 1.
 *  - move to com.holigrow.yoda.util.validators ?
 *  - interfaces + factory
 *  - constructor madness (setXXX instead of N constructors?)
 *  - more validators
 *  - more fun
 * </todo> 
 *
 * @@author Mattias Bogeblad
 */

public abstract class AbstractValidator 
{

  private static final String REQUIRED_FIELD_ERROR_CODE = "300";
  private static final String NOTUNIQUE_FIELD_ERROR_CODE = "302";


  // All constraint exceptions found during the validation.
  private ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
  // The name of the field being validated.
  private String fieldName;
  // Indicates if this field is required.
  private boolean isRequired;
  
  private boolean mustBeUnique;
  
  private Class objectClass = null;
  
  private Integer excludeId = null;
  private Object excludeObject = null;

  protected AbstractValidator(String fieldName) 
  {
  	this(fieldName, true);
  }


  /**
   *
   */
  protected AbstractValidator(String fieldName, boolean isRequired) 
  {
    this.fieldName  = fieldName;
    this.isRequired = isRequired;
  }

  protected AbstractValidator(String fieldName, boolean isRequired, boolean mustBeUnique, Class objectClass, Integer excludeId, Object excludeObject) 
  {
    this.fieldName  = fieldName;
    this.isRequired = isRequired;
    this.mustBeUnique = mustBeUnique;
    this.objectClass = objectClass;
    this.excludeId = excludeId;
    this.excludeObject = excludeObject;
  }


  /**
   *
   */
  private final ConstraintException createConstraintException(String errorCode) 
  {
    return new ConstraintException(this.fieldName, errorCode);
  }

  
  /**
   *
   */
  protected void validateIsRequired(Object value) throws ConstraintException 
  {
    if(this.isRequired && value == null) 
    {
      throw createConstraintException(REQUIRED_FIELD_ERROR_CODE);
    }
  }

  protected void validateUniqueness(String value) throws ConstraintException, SystemException 
  {
  	if (this.mustBeUnique)
  	{
  		Pattern p = Pattern.compile("[.\\s]+");
  		String[] arrString = p.split(fieldName);
  		String cleanField = arrString[arrString.length-1];
  		
		if(ValidationController.fieldValueExists(objectClass, cleanField, value, excludeId, excludeObject)) 
		{
		  throw createConstraintException(NOTUNIQUE_FIELD_ERROR_CODE);
		}
  	}
  }

  /**
   *
   */
  protected final void addConstraintException(String errorCode) 
  {
    this.ceb.add(createConstraintException(errorCode));
  }

  /**
   *
   */
  protected final void failIfAnyExceptionsFound() throws ConstraintException 
  {
    this.ceb.throwIfNotEmpty();
  }

	/**
	 * Returns the fieldName.
	 * @return String
	 */
	public String getFieldName()
	{
		return fieldName;
	}
	
	/**
	 * Sets the fieldName.
	 * @param fieldName The fieldName to set
	 */
	public void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}
	
	/**
	 * Returns the isRequired.
	 * @return boolean
	 */
	public boolean isRequired()
	{
		return isRequired;
	}
	
	/**
	 * Sets the isRequired.
	 * @param isRequired The isRequired to set
	 */
	public void setIsRequired(boolean isRequired)
	{
		this.isRequired = isRequired;
	}
	
	/**
	 * Returns the mustBeUnique.
	 * @return boolean
	 */
	public boolean isMustBeUnique()
	{
		return mustBeUnique;
	}
	
	/**
	 * Sets the mustBeUnique.
	 * @param mustBeUnique The mustBeUnique to set
	 */
	public void setMustBeUnique(boolean mustBeUnique)
	{
		this.mustBeUnique = mustBeUnique;
	}
	
	/**
	 * Returns the objectClass.
	 * @return Class
	 */
	public Class getObjectClass()
	{
		return objectClass;
	}
	
	/**
	 * Sets the objectClass.
	 * @param objectClass The objectClass to set
	 */
	public void setObjectClass(Class objectClass)
	{
		this.objectClass = objectClass;
	}
	
	/**
	 * Returns the excludeId.
	 * @return int
	 */
	public Integer getExcludeId()
	{
		return excludeId;
	}
	
	/**
	 * Sets the excludeId.
	 * @param excludeId The excludeId to set
	 */
	public void setExcludeId(Integer excludeId)
	{
		this.excludeId = excludeId;
	}

	/**
	 * Returns the excludeName.
	 * @return int
	 */
	public Object getExcludeObject()
	{
		return excludeObject;
	}
	
	/**
	 * Sets the excludeId.
	 * @param excludeId The excludeId to set
	 */
	public void setExcludeObject(Object excludeObject)
	{
		this.excludeObject = excludeObject;
	}

}
