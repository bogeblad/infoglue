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

package org.infoglue.cms.util;

import java.util.Random;


/**
 * Utility class for generating passwords.
 *
 * @author <a href="mailto:meat_for_the_butcher@yahoo.com">Patrik Nyborg</a>
 */

public class PasswordGenerator 
{
  // --- [Constants] -----------------------------------------------------------

  /**
   * We want to have a random string with a length of 10 characters.
   * Since we encode it base-36, we modulo the random number with this value.
   */

	private static final long MAX_RANDOM_LENGTH = 3656158440062976L; // 36 ** 10



  // --- [Attributes] ----------------------------------------------------------

  // Used for generating a stream of pseudorandom numbers.
  private static final Random random = new Random();



  // --- [Static] --------------------------------------------------------------
  // --- [Constructors] --------------------------------------------------------

  /**
   * Static class; don't allow instantiation.
   */
  private PasswordGenerator() {}



  // --- [Public] --------------------------------------------------------------
 
  /**
   * Generates a random 10 characters password.
   *
   * @return the generated password.
   */
  public static synchronized String generate() 
  {
    return Long.toString(Math.abs(random.nextLong()) % MAX_RANDOM_LENGTH, Character.MAX_RADIX);
  }



  // --- [X implementation] ----------------------------------------------------
  // --- [X Overrides] ---------------------------------------------------------
  // --- [Package protected] ---------------------------------------------------
  // --- [Private] -------------------------------------------------------------
  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
}