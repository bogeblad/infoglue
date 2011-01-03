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
 * <todo>Should only return a Validator interface</todo>
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
public class ValidatorFactory {
  // --- [Constants] -----------------------------------------------------------
  // --- [Attributes] ----------------------------------------------------------
  // --- [Static] --------------------------------------------------------------
  // --- [Constructors] --------------------------------------------------------

  /**
   *
   */
  private ValidatorFactory() {}



  // --- [Public] --------------------------------------------------------------

  /**
   *
   */
  public static final StringValidator createStringValidator(String fieldName, boolean isRequired, int upperLengthLimit) {
    return new StringValidator(fieldName, isRequired, upperLengthLimit);
  }

  /**
   *
   */
  public static final StringValidator createStringValidator(String fieldName, boolean isRequired, int lowerLengthLimit, int upperLengthLimit) {
    return new StringValidator(fieldName, isRequired, lowerLengthLimit, upperLengthLimit);
  }

  public static final StringValidator createStringValidator(String fieldName,boolean isRequired, int lowerLengthLimit, int upperLengthLimit,boolean mustBeUnique,Class objectClass, Integer excludeId, Object excludedObject) {
    return new StringValidator(fieldName, isRequired, lowerLengthLimit, upperLengthLimit, mustBeUnique, objectClass, excludeId, excludedObject);
  }

  public static final StringValidator createStringValidator(String fieldName,boolean isRequired,boolean mustBeUnique,Class objectClass, Integer excludeId, Object excludedObject) {
    return new StringValidator(fieldName, isRequired, mustBeUnique, objectClass, excludeId, excludedObject);
  }


  /**
   *
   */
  public static final EmailValidator createEmailValidator(String fieldName, boolean isRequired, int upperLengthLimit) {
    return new EmailValidator(fieldName, isRequired, upperLengthLimit);
  }

  /**
   *
   */
  public static final IntegerValidator createNonNegativeIntegerValidator(String fieldName, boolean isRequired) {
    return new IntegerValidator(fieldName, isRequired, 0, Integer.MAX_VALUE);
  }

  /**
   *
   */
  public static final FloatValidator createNonNegativeFloatValidator(String fieldName, boolean isRequired) {
    return new FloatValidator(fieldName, isRequired, new Float(0.0).floatValue(), Float.MAX_VALUE);
  }



  // --- [X implementation] ----------------------------------------------------
  // --- [X Overrides] ---------------------------------------------------------
  // --- [Package protected] ---------------------------------------------------
  // --- [Private] -------------------------------------------------------------
  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
}