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
public class IntegerValidator extends AbstractValidator 
{
  // --- [Constants] -----------------------------------------------------------

  // error codes
  private static final String ILLEGAL_VALUE_ERROR_CODE = "306";



  // --- [Attributes] ----------------------------------------------------------

  //
  private Range valueSpace;

  

  // --- [Static] --------------------------------------------------------------
  // --- [Constructors] --------------------------------------------------------

  /**
   *
   */
  IntegerValidator(String fieldName, boolean isRequired, int lowerLimit, int upperLimit) {
    super(fieldName, isRequired);
    this.valueSpace = new Range(lowerLimit, upperLimit);
  }



  // --- [Public] --------------------------------------------------------------

  /**
   *
   */
  public void validate(Integer value) throws ConstraintException {
    validateIsRequired(value);
    if(value == null) { // no validation needed + no need for further null checking
      return;
    }
    validateValueSpace(value);
    failIfAnyExceptionsFound();
  }

  /**
   */
  public void validate(Integer value, ConstraintExceptionBuffer ceb) {
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
  private void validateValueSpace(Integer value) {
    if(!this.valueSpace.isWithinLimits(value.intValue())) {
      addConstraintException(ILLEGAL_VALUE_ERROR_CODE);
    }
  }



  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
}