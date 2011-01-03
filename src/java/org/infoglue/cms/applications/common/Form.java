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

package org.infoglue.cms.applications.common;

import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;


/**
 *
 * @author <a href="mailto:meat_for_the_butcher@yahoo.com">Patrik Nyborg</a>
 */
public class Form {
  // --- [Constants] -----------------------------------------------------------

  //
  private static final String ERROR_CODE = "306";



  // --- [Attributes] ----------------------------------------------------------
  // --- [Static] --------------------------------------------------------------
  // --- [Constructors] --------------------------------------------------------
  // --- [Public] --------------------------------------------------------------
  // --- [X implementation] ----------------------------------------------------
  // --- [X Overrides] ---------------------------------------------------------
  // --- [Package protected] ---------------------------------------------------
  // --- [Private] -------------------------------------------------------------

  /**
   *
   */
  private void validateField(boolean isIllegalValue, String fieldName, ConstraintExceptionBuffer ceb) {
    if(isIllegalValue) {
      ceb.add(new ConstraintException(fieldName, ERROR_CODE));
    }
  }

  // --- [Protected] -----------------------------------------------------------

  /**
   *
   */
  protected void validateDateField(String value, String fieldName, ConstraintExceptionBuffer ceb) {
    validateField(!ValueConverter.isDate(value), fieldName, ceb);
  }

  /**
   *
   */
  protected void validateNonNegativeFloatField(String value, String fieldName, ConstraintExceptionBuffer ceb) {
    validateField(!ValueConverter.isNonNegativeFloat(value), fieldName, ceb);
  }

  /**
   *
   */
  protected void validateNonNegativeIntegerField(String value, String fieldName, ConstraintExceptionBuffer ceb) {
    validateField(!ValueConverter.isNonNegativeInteger(value), fieldName, ceb);
  }



  // --- [Inner classes] -------------------------------------------------------
}