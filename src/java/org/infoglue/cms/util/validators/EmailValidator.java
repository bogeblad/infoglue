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


public class EmailValidator extends StringValidator 
{
  // --- [Constants] -----------------------------------------------------------

  // perl5 patterns
  private static final String SEPARATOR = "\\.";
  private static final String WORD      = "[a-zA-Z][a-zA-Z0-9_-]*";
  private static final String WORDS     = WORD + "(" + SEPARATOR + WORD + ")*";
  private static final String IP_PART   = "[0-9]{1,3}";
  private static final String IP        = IP_PART + "(" + SEPARATOR + IP_PART + "){3}";
  private static final String DOMAIN    = "(" + IP + "|" + WORDS + ")";
  private static final String LOCAL     = WORDS;
  private static final String ADDRESS   = "^" + LOCAL + "@" + DOMAIN + "$";

  // error codes
  private static final String INVALID_EMAIL_ADDRESS_ERROR_CODE = "305";



  // --- [Attributes] ----------------------------------------------------------
  // --- [Static] --------------------------------------------------------------
  // --- [Constructors] --------------------------------------------------------

  /**
   * <todo>remove?</todo>
   */
  public EmailValidator(String fieldName) {
    this(fieldName, true);
  }

  /**
   * <todo>remove?</todo>
   */
  public EmailValidator(String fieldName, boolean isRequired) {
    super(fieldName, isRequired);
    initializePattern(ADDRESS, INVALID_EMAIL_ADDRESS_ERROR_CODE);
  }

  /**
   *
   */
  public EmailValidator(String fieldName, boolean isRequired, int upperLengthLimit) {
    super(fieldName, isRequired, upperLengthLimit);
    initializePattern(ADDRESS, INVALID_EMAIL_ADDRESS_ERROR_CODE);
  }



  // --- [Public] --------------------------------------------------------------
  // --- [X implementation] ----------------------------------------------------
  // --- [X Overrides] ---------------------------------------------------------
  // --- [Package protected] ---------------------------------------------------
  // --- [Private] -------------------------------------------------------------
  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
}