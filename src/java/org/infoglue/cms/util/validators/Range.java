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
public class Range {
  // --- [Constants] -----------------------------------------------------------
  // --- [Attributes] ----------------------------------------------------------

  //
  private int lowerLimit = 0;
  //
  private int upperLimit = 0;
  // 
  private boolean hasUpperLimit = true;



  // --- [Static] --------------------------------------------------------------
  // --- [Constructors] --------------------------------------------------------

  /**
   *
   */
  Range() {
    this.hasUpperLimit = false;
  }

  /**
   *
   */
  public Range(int upperLimit) {
    this(0, upperLimit);
  }

  /**
   *
   */
  public Range(int lowerLimit, int upperLimit) {
    if(lowerLimit < 0 || lowerLimit > upperLimit) {
      throw new Bug("Illegal arguments : lowerLimit=" + lowerLimit + ", upperLimit=" + upperLimit + ".");
    }
    this.lowerLimit = lowerLimit;
    this.upperLimit = upperLimit;
  }



  // --- [Public] --------------------------------------------------------------
  // --- [X implementation] ----------------------------------------------------
  // --- [X Overrides] ---------------------------------------------------------
  // --- [Package protected] ---------------------------------------------------

  /**
   *
   */
  boolean isWithinLimits(int value) {
    return (value >= this.lowerLimit) && (!this.hasUpperLimit || value <= this.upperLimit);
  }  



  // --- [Private] -------------------------------------------------------------
  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
/**
 * Returns the lowerLimit.
 * @return int
 */
public int getLowerLimit()
{
	return lowerLimit;
}

/**
 * Returns the upperLimit.
 * @return int
 */
public int getUpperLimit()
{
	return upperLimit;
}

/**
 * Returns the hasUpperLimit.
 * @return boolean
 */
public boolean getHasUpperLimit()
{
	return hasUpperLimit;
}

}