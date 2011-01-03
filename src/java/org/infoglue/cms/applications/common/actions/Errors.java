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

package org.infoglue.cms.applications.common.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 *
 *
 * @author <a href="mailto:meat_for_the_butcher@yahoo.com">Patrik Nyborg</a>
 */
public final class Errors {
  // --- [Constants] -----------------------------------------------------------
  // --- [Attributes] ----------------------------------------------------------

  // <field name> -> <Collection of errorMessages>
  private final Map errors = new HashMap();



  // --- [Static] --------------------------------------------------------------
  // --- [Constructors] --------------------------------------------------------
  // --- [Public] --------------------------------------------------------------

  /**
   *
   */
  public final boolean hasErrors() {
    return !this.errors.isEmpty();
  }

  /**
   *
   */
  public final boolean hasErrors(String fieldName) {
    return this.errors.containsKey(fieldName);
  }

  /**
   *
   */
  public final Collection getErrors(String fieldName) {
    return (Collection) this.errors.get(fieldName);
  }

  /**
   *
   */
  public final Collection getAllErrors() {
	return (Collection) this.errors.values();
  }


  /**
   *
   */
  public final void addError(String fieldName, String errorMessage) {
    if(getErrors(fieldName) == null) {
      this.errors.put(fieldName, new ArrayList());
    } 
    getErrors(fieldName).add(errorMessage);
  }



  // --- [X implementation] ----------------------------------------------------
  // --- [java.lang.Object Overrides] ------------------------------------------

  /**
   *
   */
  public String toString() {
    final StringBuffer sb = new StringBuffer("<Errors>[ ");
    
    for(Iterator fieldNames = this.errors.keySet().iterator(); fieldNames.hasNext(); ) {
      final String fieldName       = (String) fieldNames.next();
      final Iterator errorMessages = getErrors(fieldName).iterator();

      sb.append(fieldName + "=> { ");
      sb.append(toString(errorMessages));      
      sb.append("}" + (fieldNames.hasNext() ? ", " : " "));
    }
    sb.append("]");
    return sb.toString();
  }



  // --- [Package protected] ---------------------------------------------------
  // --- [Private] -------------------------------------------------------------

  /**
   *
   */
  private String toString(Iterator errorMessages) {
    final StringBuffer sb = new StringBuffer();

    while(errorMessages.hasNext()) {
      final String errorMessage = (String) errorMessages.next();
      sb.append("\"" + errorMessage + "\"" + (errorMessages.hasNext() ? ", " : " "));
    }
    return sb.toString();
  }



  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
}