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

//import com.icl.saxon.sort.LowercaseFirstComparer;

import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.RegexpHelper;

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
 * @@author <a href="meat_for_the_butcher@@yahoo.com">Patrik Nyborg</a>
 */
public class StringValidator extends AbstractValidator {
  // --- [Constants] -----------------------------------------------------------

  // error codes
  private static final String INVALID_LENGTH_ERROR_CODE = "301";



  // --- [Attributes] ----------------------------------------------------------

  //
  private Range range;
  //
  private String pattern;
  //
  private String patternErrorCode;

  

  // --- [Static] --------------------------------------------------------------
  // --- [Constructors] --------------------------------------------------------

  /**
   * <todo>remove?</todo>
   */
  public StringValidator(String fieldName) {
    this(fieldName, true);
  }

  /**
   * <todo>remove?</todo>
   */
  public StringValidator(String fieldName, boolean isRequired) {
    this(fieldName, isRequired, new Range());
  }

  /**
   *
   */
  public StringValidator(String fieldName, boolean isRequired, int upperLengthLimit) {
    this(fieldName, isRequired, 0, upperLengthLimit);
  }

  /**
   *
   */
  public StringValidator(String fieldName, boolean isRequired, int lowerLengthLimit, int upperLengthLimit) {
    this(fieldName, isRequired, new Range(lowerLengthLimit, upperLengthLimit));
  }

	/**
	 * Constructor for StringValidator.
	 * @param fieldName
	 * @param isRequired
	 * @param mustBeUnique
	 * @param objectClass
	 */
	public StringValidator(String fieldName,boolean isRequired,boolean mustBeUnique,Class objectClass, Integer excludeId, Object excludedObject) 
	{
		super(fieldName, isRequired, mustBeUnique, objectClass, excludeId, excludedObject);
		this.range = new Range();
	}
	public StringValidator(String fieldName,boolean isRequired, int lowerLengthLimit, int upperLengthLimit,boolean mustBeUnique,Class objectClass, Integer excludeId, Object excludedObject) 
	{
		super(fieldName, isRequired, mustBeUnique, objectClass, excludeId, excludedObject);
		this.range = new Range(lowerLengthLimit, upperLengthLimit);
	}

  /**
   *
   */
  public StringValidator(String fieldName, boolean isRequired, Range range) {
    super(fieldName, isRequired);
    this.range = range;
  }




  // --- [Public] --------------------------------------------------------------

  /**
   *
   */
  public void validate(String value) throws ConstraintException {
    validateIsRequired(value);
    if(value == null) { // no validation needed + no need for further null checking
      return;
    }
    validateLength(value.trim());
    validatePattern(value.trim());

	// ss
    try {
		validateUniqueness(value);
	}  catch (SystemException e) {
	}
    
    
    failIfAnyExceptionsFound();
  }

  /**
   */
  public void validate(String value, ConstraintExceptionBuffer ceb) {
    try {
      validate(value);
    } catch(ConstraintException e) {
      ceb.add(e);
    }
  }  

  /**
   *
   */
  private void validateLength(String value) {
    if(!this.range.isWithinLimits(value.length())) {
      addConstraintException(INVALID_LENGTH_ERROR_CODE);
    }
  }

  /**
   *
   */
  private void validatePattern(String value) {
    if(this.pattern != null && !RegexpHelper.match(this.pattern, value)) {
      addConstraintException(this.patternErrorCode);
    }
  }



  // --- [X implementation] ----------------------------------------------------
  // --- [AbstractValidator Overrides] -----------------------------------------

  /**
   *
   */
  protected void validateIsRequired(Object value) throws ConstraintException {
    super.validateIsRequired(value);
    if(value != null) {
      // <todo>this should be safe but add a Bug test anyway?</todo>
      String stringValue = (String) value;
      if(stringValue.trim().length() == 0) {
        super.validateIsRequired(null);
      }
    }
  }



  // --- [Package protected] ---------------------------------------------------
  // --- [Private] -------------------------------------------------------------
  // --- [Protected] -----------------------------------------------------------

  /**
   *
   */
  protected void initializePattern(String pattern, String patternErrorCode) {
    this.pattern          = pattern;
    this.patternErrorCode = patternErrorCode;
  }



  // --- [Inner classes] -------------------------------------------------------


  // --- [Setters / Getters] -------------------------------------------------------

	/**
	 * Returns the range.
	 * @return Range
	 */
	public Range getRange()
	{
		return range;
	}
	
	/**
	 * Sets the range.
	 * @param range The range to set
	 */
	public void setRange(Range range)
	{
		this.range = range;
	}

}