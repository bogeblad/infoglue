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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.infoglue.cms.exception.ConfigurationError;


/**
 * Utility class for loading/manipulating resources (must be accessible from the classpath).
 *
 * @author <a href="mailto:meat_for_the_butcher@yahoo.com">Patrik Nyborg</a>
 */
public class ResourceHelper {
  // --- [Constants] -----------------------------------------------------------

  // The size of the buffer to use when working with I/O (4 kB).
  private static final int CHAR_BUFFER_SIZE = 4096;



  // --- [Attributes] ----------------------------------------------------------
  // --- [Static] --------------------------------------------------------------

  // The string manager for this package.
  private static final StringManager sm = StringManagerFactory.getSystemStringManager(Constants.PACKAGE_NAME);

  // The logger for this class.
  private static final Logger logger = Logger.getLogger(RegexpHelper.class.getName());



  // --- [Constructors] --------------------------------------------------------

  /**
   * Static class; don't allow instantiation.
   */
  private ResourceHelper() {}



  // --- [Public] --------------------------------------------------------------

  /**
   * <p>Loads the specified property file into a Properties object.</p>
   *
   * <p>Example: <code>ResourceHelper.loadProperties("se/sprawl/cms/CMSProperties.properties");</code></p>
   *
   * @param name the name of the resource ("/"-separated path name).
   */
  public static synchronized Properties loadProperties(String name) {
    logger.debug(sm.getString("resource.loadProperties.info", name));
    try {
      final Properties properties = new Properties();
      properties.load(getResourceAsStream(name, ResourceHelper.class));
      return properties;
    } catch(Exception e) {
      throw new ConfigurationError(sm.getString("resource.loadProperties.error", name), e);
    }
  }

  /**
   * <p>Loads the specified resource file and returns the content as a string.</p>
   *
   * <p>Example: <code>ResourceHelper.readResource("se/sprawl/cms/CMSSQL.sql");</code></p>
   *
   * @param name the name of the resource ("/"-separated path name).
   */
  public static synchronized String readResource(String name) {
    logger.debug(sm.getString("resource.readResource.info", name));
    try {
      final StringBuffer sb = new StringBuffer();
      final Reader reader   = new InputStreamReader(getResourceAsStream(name, ResourceHelper.class));
      char[] buf            = new char[CHAR_BUFFER_SIZE];
      int count             = 0;
      while((count = reader.read(buf, 0, CHAR_BUFFER_SIZE)) > 0) {
        sb.append(buf, 0, count);
      }
      return sb.toString();
    } catch(Exception e) {
      throw new ConfigurationError(sm.getString("resource.readResource.error", name), e);
    }
  }

	/**
	 * Find a resource with a variety of fallback ClassLoaders.
	 *
	 * @param resourceName The name of the resource to find. ("/"-separated path name)
	 * @param callingClass The class looking for the resource
	 * @return The InputStream for the given resource name, or null if one is not found
	 */
	public static InputStream getResourceAsStream(String resourceName, Class callingClass)
	{
		URL url = null;

		url = Thread.currentThread().getContextClassLoader().getResource(resourceName);

		if (url == null)
			url = ResourceHelper.class.getClassLoader().getResource(resourceName);

		if (url == null)
			url = callingClass.getClassLoader().getResource(resourceName);

		try
		{
			return url != null ? url.openStream() : null;
		}
		catch (IOException e)
		{
			return null;
		}
	}
}