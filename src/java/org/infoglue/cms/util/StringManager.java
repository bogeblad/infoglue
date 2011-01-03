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


/**
 *
 * @author <a href="mailto:meat_for_the_butcher@yahoo.com">Patrik Nyborg</a>
 */
public interface StringManager {
  /**
   * Gets a string for the given key from the underlying resource bundle.
   *
   * @param key the key for the desired string.
   * @exception java.lang.NullPointerException if key is null.
   * @exception java.util.MissingResourceException if no object for the given key can be found.
   */
  public String getString(String key);

  /**
   *
   */
  public String getString(String key, Object args[]);

  /**
   *
   */
  public String getString(String key, Object arg);

  /**
   *
   */
  public String getString(String key, Object arg1, Object arg2);

  /**
   *
   */
  public String getString(String key, Object arg1, Object arg2, Object arg3);
}