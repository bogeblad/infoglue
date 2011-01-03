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

package org.infoglue.cms.exception;


/**
 * <p>Thrown to indicate that something completely unexpected has happen.
 * If you ever feel the urge to say "ah, this could never happen, I'll skip testing...",
 * you should probably add the test and throw a Bug if the test fails...</p>
 * <p>This is an internal error so there is no need to localize the error message.</p>
 *
 * @author <a href="mailto:meat_for_the_butcher@yahoo.com">Patrik Nyborg</a>
 */
public class Bug extends Error {
  // --- [Constants] -----------------------------------------------------------
  // --- [Attributes] ----------------------------------------------------------

  // The throwable that caused this Bug to get thrown (null is permitted).
  private Throwable cause;



  // --- [Static] --------------------------------------------------------------
  // --- [Constructors] --------------------------------------------------------

  /**
   * Construct a Bug with the detailed error message.
   *
   * @param message the detailed error message.
   */
  public Bug(String message) {
    super(message);
  }

  /**
   * Construct a Bug with the detailed error message and cause.
   *
   * @param message the detailed error message.
   * @param cause the throwable that caused this Bug to get thrown.
   */
  public Bug(String message, Throwable cause) {
    this(message);
    this.cause = cause;
  }



  // --- [Public] --------------------------------------------------------------

  /**
   * Returns the cause of this Bug or null if the cause is nonexistent or unknown.
   * (The cause is the throwable that caused this ConfigurationError to get thrown).
   *
   * @return the cause of this Bug or null if the cause is nonexistent or unknown.
   */
  public Throwable getCause() {
    return this.cause;
  }



  // --- [X implementation] ----------------------------------------------------
  // --- [X Overrides] ---------------------------------------------------------
  // --- [Package protected] ---------------------------------------------------
  // --- [Private] -------------------------------------------------------------
  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
}