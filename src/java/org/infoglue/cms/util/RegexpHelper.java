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

import org.apache.log4j.Logger;
import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.apache.oro.text.perl.Perl5Util;
import org.infoglue.cms.exception.Bug;


/**
 * Utility class for regular expressions.
 *
 * @author Mattias Bogeblad
 */
public class RegexpHelper 
{

	// The string manager for this package.
	private static final StringManager sm = StringManagerFactory.getSystemStringManager(Constants.PACKAGE_NAME);
	
	// The logger for this class.
	private static final Logger logger = Logger.getLogger(RegexpHelper.class.getName());
	
	// The regexp delegate.
	private static final Perl5Util regexpDelegate = new Perl5Util();
	
	/**
	 * Static class; don't allow instantiation.
	 */
	
	private RegexpHelper() 
	{
	}



	/**
	 * Checks if the specified string contains the specified pattern.
	 *
	 * @param pattern the pattern to search for (perl5 native format).
	 * @param string the string to perform the search on.
	 * @return true if the string contains the pattern; false otherwise.
	 */

	public static synchronized boolean match(String pattern, String string) 
	{
		logger.debug(sm.getString("regexp.match.info", pattern, string));
		try 
		{
			boolean result = regexpDelegate.match("/" + pattern + "/", string);
			//getLogger().info("pattern:" + pattern);
			//getLogger().info("string:" + string);
			//getLogger().info("result:" + result);
			return result;
		} 
		catch (MalformedPerl5PatternException e) 
		{
			throw new Bug(sm.getString("regexp.match.pattern_error", pattern), e);
		} 
		catch (NullPointerException e) 
		{
			throw new Bug(sm.getString("regexp.match.illegal_parameters_error", pattern, string), e);
		}	
	}

}