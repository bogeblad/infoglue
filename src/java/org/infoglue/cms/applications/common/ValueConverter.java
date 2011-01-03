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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.infoglue.cms.exception.ConstraintException;

/**
 *
 * @author <a href="mailto:meat_for_the_butcher@yahoo.com">Patrik Nyborg</a>
 */
public class ValueConverter 
{
  // --- [Constants] -----------------------------------------------------------
  // --- [Attributes] ----------------------------------------------------------
  // --- [Static] --------------------------------------------------------------
  // --- [Constructors] --------------------------------------------------------

  /**
   *
   */
  public ValueConverter() {}



  // --- [Public] --------------------------------------------------------------

  /**
   *
   */
  public static final boolean isDate(String value) {
    return toDate(value) != null;
  }

  /**
   * <todo/>
   */
  public static final Date toDate(String value) 
  {
    return new Date();
  }


    /**
     *
     */
     public static final Date getDate(String dateString, String fieldName) throws ConstraintException
     {
        Date publishDate = null;
        try
        {
            publishDate = toDate(dateString, "yyyy-MM-dd");
        }
        catch(Exception e)
        {
            throw new ConstraintException(fieldName, "305");
        }    
        return publishDate;
     }

    /**
     * 
     */
    
    public static final Date toDate(String dateString, String pattern) throws Exception 
    {
        if(dateString == null || dateString.length() == 0)
            return null;
            
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.parse(dateString);
    }

  /**
   *
   */
  public static final boolean isBoolean(String value) {
    return toBoolean(value) != null;
  }

  /**
   * <todo/>
   */
  public static final Boolean toBoolean(String value) {
    return Boolean.valueOf(value);
  }

  /**
   *
   */
  public static final boolean isNonNegativeInteger(String value) {
    return toNonNegativeInteger(value) != null;
  }

  /**
   *
   */
  public static final Integer toNonNegativeInteger(String value) {
    try {
      final int intValue = Integer.parseInt(value);
      return (intValue < 0) ? null : new Integer(intValue);
    } catch(NumberFormatException e) {
      return null;
    }
  }

  /**
   *
   */
  public static final boolean isNonNegativeFloat(String value) {
    return toNonNegativeFloat(value) != null;
  }

  /**
   *
   */
  public static final Float toNonNegativeFloat(String value) {
    try {
      final float floatValue = Float.parseFloat(value);
      return (floatValue < 0.0) ? null : new Float(floatValue);
    } catch(NumberFormatException e) {
      return null;
    }
  }

  public List getListFromArray(String[] strings)
  {
	  if(strings == null)
		  return Collections.EMPTY_LIST;
	  
	  return Arrays.asList(strings);
  }

  // --- [X implementation] ----------------------------------------------------
  // --- [X Overrides] ---------------------------------------------------------
  // --- [Package protected] ---------------------------------------------------
  // --- [Private] -------------------------------------------------------------
  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
}