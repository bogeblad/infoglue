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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConfigurationError;


/**
 *
 */
class ChainedStringManager implements StringManager {
  // --- [Constants] -----------------------------------------------------------
  // --- [Attributes] ----------------------------------------------------------
 
  //
  private List managers = new ArrayList();



  // --- [Static] --------------------------------------------------------------

  // The string manager for this package.
  private static final StringManager sm = StringManagerFactory.getSystemStringManager(Constants.PACKAGE_NAME);



  // --- [Constructors] --------------------------------------------------------
  // --- [Public] --------------------------------------------------------------
  // --- [org.infoglue.cms.util.StringManager implementation] -----------------

  /**
   *
   */
  public final String getString(String key) {
    for(Iterator iterator = this.managers.iterator(); iterator.hasNext(); ) {
      SimpleStringManager manager = (SimpleStringManager) iterator.next();
      if(manager.containsKey(key)) {
        return manager.getString(key);
      }
    }
    throw new ConfigurationError(sm.getString("chain.getString.not_found_error", key));
  }

  /**
   *
   */
  public final String getString(String key, Object args[]) {
    return MessageFormat.format(getString(key), args);
  }

  /**
   *
   */
  public final String getString(String key, Object arg) {
    return getString(key, new Object[]{ arg });
  }

  /**
   *
   */
  public final String getString(String key, Object arg1, Object arg2) {
    return getString(key, new Object[]{ arg1, arg2 });
  }

  /**
   *
   */
  public final String getString(String key, Object arg1, Object arg2, Object arg3) {
    return getString(key, new Object[]{ arg1, arg2, arg3 });
  }



  // --- [X Overrides] ---------------------------------------------------------
  // --- [Package protected] ---------------------------------------------------

  /**
   *
   */
  final void add(StringManager manager) {
    if(manager == null) {
      throw new Bug(sm.getString("chain.add.null_parameter_error"));
    }
    this.managers.add(manager);
  }



  // --- [Private] -------------------------------------------------------------
  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
}
