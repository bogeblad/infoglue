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
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConfigurationError;

/**
 *
 */
public class SimpleStringManager implements StringManager 
{

    private final static Logger logger = Logger.getLogger(SimpleStringManager.class.getName());

    // The ResourceBundle for this StringManager.
    private ResourceBundle bundle;

 
    SimpleStringManager(String bundleName, Locale locale) 
    { 
        if(locale == null || locale.getLanguage() == null || locale.getLanguage().equalsIgnoreCase(""))
        {
            logger.info("No locale sent in - must be a bug:" + locale);
            locale = Locale.ENGLISH;
        }
        
        try 
        { 
            logger.info("Created a SimpleStringManager for package bundleName" + bundleName + ":" + locale);
            this.bundle = ResourceBundle.getBundle(bundleName, locale);
            //ResourceBundle.getBundle(baseName, control)
        } 
        catch(MissingResourceException e) 
        {
            throw new ConfigurationError("Unable to find resource bundle: " + e.getMessage(), e);
        } 
        catch(NullPointerException e) 
        {
            throw new Bug("Unable to create resource bundle.", e);
        }
    }



  // --- [Public] --------------------------------------------------------------
  // --- [org.infoglue.cms.util.StringManager implementation] -----------------

  /**
   *
   */
    public final String getString(String key) 
    {
        try 
        {
            //logger.info("Trying to find a string for key " + key + " in " + this);
            return this.bundle.getString(key);
        } 
        catch(MissingResourceException e) 
        {
        	logger.warn("Error trying to find a string for key " + key, e);
            throw new ConfigurationError("Key not found: " + key, e);
        } 
        catch(Throwable t) 
        {
        	logger.warn("Error trying to find a string for key " + key);
            throw new Bug("Unable to fetch the value for the specified key.", t);
        }
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
  boolean containsKey(String key) {
    try {
      this.bundle.getString(key);
      return true;
    } catch(MissingResourceException e) {
      return false;
    }
  }



  // --- [Private] -------------------------------------------------------------
  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
}
  


