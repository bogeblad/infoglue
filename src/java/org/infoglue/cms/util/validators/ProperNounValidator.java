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
 * Egennamn...
 *
 * @@author <a href="meat_for_the_butcher@@yahoo.com">Patrik Nyborg</a>
 */
public class ProperNounValidator extends StringValidator {
  // --- [Constants] -----------------------------------------------------------

  // perl5 patterns
  private static final String SEPARATOR           = "(\\s+|-)";
  private static final String PROPER_NOUN_PART    = "(\\w+'?\\w+)"; // <todo>"\w" allows "_" - incorrect</todo>
  private static final String PROPER_NOUN_PATTERN = "^" + PROPER_NOUN_PART + "(" + SEPARATOR + PROPER_NOUN_PART + ")*$";

  // error codes
  private static final String INVALID_PROPER_NOUN_ERROR_CODE = "305";



  // --- [Attributes] ----------------------------------------------------------
  // --- [Static] --------------------------------------------------------------
  // --- [Constructors] --------------------------------------------------------

  /**
   * <todo>remove?</todo>
   */
  public ProperNounValidator(String fieldName) {
    this(fieldName, true);
  }

  /**
   * <todo>remove?</todo>
   */
  public ProperNounValidator(String fieldName, boolean isRequired) {
    super(fieldName, isRequired);
    initializePattern(PROPER_NOUN_PATTERN, INVALID_PROPER_NOUN_ERROR_CODE);
  }

  /**
   *
   */
  public ProperNounValidator(String fieldName, boolean isRequired, int upperLengthLimit) {
    super(fieldName, isRequired, upperLengthLimit);
    initializePattern(PROPER_NOUN_PATTERN, INVALID_PROPER_NOUN_ERROR_CODE);
  }



  // --- [Public] --------------------------------------------------------------
  // --- [X implementation] ----------------------------------------------------
  // --- [X Overrides] ---------------------------------------------------------
  // --- [Package protected] ---------------------------------------------------
  // --- [Private] -------------------------------------------------------------
  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
}