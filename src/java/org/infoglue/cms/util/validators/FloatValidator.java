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

import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
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
 * @@author <a href="meat_for_the_butcher@@yahoo.com">Patrik Nyborg</a>
 */
public class FloatValidator extends AbstractValidator 
{
  // --- [Constants] -----------------------------------------------------------

  // error codes
  private static final String ILLEGAL_VALUE_ERROR_CODE = "306";



  // --- [Attributes] ----------------------------------------------------------

  // <todo>fix so Range supports Number and values lower than 0</todo>
  private float lowerLimit;
  private float upperLimit;

  

  // --- [Static] --------------------------------------------------------------
  // --- [Constructors] --------------------------------------------------------

  /**
   *
   */
  FloatValidator(String fieldName, boolean isRequired, float lowerLimit, float upperLimit) {
    super(fieldName, isRequired);

    if(lowerLimit > upperLimit) {
      throw new Bug("Illegal arguments : lowerLimit=" + lowerLimit + ", upperLimit=" + upperLimit + ".");
    }
    this.lowerLimit = lowerLimit;
    this.upperLimit = upperLimit;
  }



  // --- [Public] --------------------------------------------------------------

  /**
   *
   */
  public void validate(Float value) throws ConstraintException {
    validateIsRequired(value);
    if(value == null) { // no validation needed + no need for further null checking
      return;
    }
    validateValueSpace(value.floatValue());
    failIfAnyExceptionsFound();
  }

  /**
   */
  public void validate(Float value, ConstraintExceptionBuffer ceb) {
    try {
      validate(value);
    } catch(ConstraintException e) {
      ceb.add(e);
    }
  }  

  // --- [X implementation] ----------------------------------------------------
  // --- [X Overrides] ---------------------------------------------------------
  // --- [Package protected] ---------------------------------------------------
  // --- [Private] -------------------------------------------------------------

  /**
   *
   */
  private void validateValueSpace(float value) {
    if(value < this.lowerLimit || value > this.upperLimit) {
      addConstraintException(ILLEGAL_VALUE_ERROR_CODE);
    }
  }



  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
}