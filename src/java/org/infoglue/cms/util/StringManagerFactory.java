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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 *
 *
 * @author <a href="mailto:meat_for_the_butcher@yahoo.com">Patrik Nyborg</a>
 */
public class StringManagerFactory 
{

    private final static Logger logger = Logger.getLogger(StringManagerFactory.class.getName());

    private static final String SYSTEM       = ".SystemStrings";
    private static final String PRESENTATION = ".PresentationStrings";

    // All created StringManager instances.
	private static Map managers = new HashMap();


  /**
   * Static class; don't allow instantiation.
   */
  private StringManagerFactory() {}



  // --- [Public] --------------------------------------------------------------

  /**
   *
   */
  public static synchronized StringManager getSystemStringManager(String packageName) {
    return getStringManager(packageName, SYSTEM, Locale.ENGLISH);
  }

  /**
   *
   */
  public static synchronized StringManager getPresentationStringManager(String packageName, Locale locale) {
    return getStringManager(packageName, PRESENTATION, locale);
  }

  /**
   *
   */
  public static synchronized StringManager getPresentationStringManager(String packageNames[], Locale locale) 
  {
    logger.info("Getting stringManager");  
    final ChainedStringManager chain = new ChainedStringManager();
    for(int i=0; i<packageNames.length; ++i) {
      chain.add(getPresentationStringManager(packageNames[i], locale));
    }
    return chain;
  }



  // --- [X implementation] ----------------------------------------------------
  // --- [X Overrides] ---------------------------------------------------------
  // --- [Package protected] ---------------------------------------------------
  // --- [Private] -------------------------------------------------------------

  /**
   *
   */
  private static StringManager getStringManager(String packageName, String suffix, Locale locale) {
    logger.info("packageName:" + packageName);
    final String name       = getName(packageName, suffix, locale);
    final String bundleName = getBundleName(packageName, suffix);

    StringManager manager = (StringManager) managers.get(name);
    if(manager == null) {
      manager = new SimpleStringManager(bundleName, locale);
      managers.put(name, manager);
    }
    return manager;
  }

  /**
   *
   */
  
  private final static String getName(String packageName, String suffix, Locale locale) 
  {
    return packageName + suffix + "_" + locale.toString() + ".properties";
  }

  /**
   *
   */
  private final static String getBundleName(String packageName, String suffix) {
    return packageName + suffix;
  }



  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
}