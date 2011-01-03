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

package org.infoglue.cms.exception;


/**
 * <p>Thrown to indicate that some configuration resource is not setup correctly, is missing etc.</p>
 * <p>Examples includes missing key in resource bundle, missing element/attribute in xml
 * file, missing property file...</p>
 *
 * <p>This is an internal error so there is no need to localize the error message.</p>
 *
 * @author <a href="mailto:meat_for_the_butcher@yahoo.com">Patrik Nyborg</a>
 */
 
public class ConfigurationError extends Error 
{

    // The throwable that caused this ConfigurationError to get thrown (null is permitted).
    private Throwable cause;


    /**
     * Construct a ConfigurationError with the detailed error message.
     *
     * @param message the detailed error message.
     */
    public ConfigurationError(String message) 
    {
        super(message);
    }

    /**
     * Construct a ConfigurationError with the detailed error message and cause.
     *
     * @param message the detailed error message.
     * @param cause the throwable that caused this ConfigurationError to get thrown.
     */
    
    public ConfigurationError(String message, Throwable cause) 
    {
        super(message);
        this.cause = cause;
    }

    /**
     * Returns the cause of this ConfigurationError or null if the cause is nonexistent or unknown.
     * (The cause is the throwable that caused this ConfigurationError to get thrown).
     *
     * @return the cause of this ConfigurationError or null if the cause is nonexistent or unknown.
     */
    public Throwable getCause() 
    {
        return this.cause;
    }
}