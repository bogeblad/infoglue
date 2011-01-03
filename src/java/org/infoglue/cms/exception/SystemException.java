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
 * <p>Thrown to indicate that something unexpected but recoverable has happen. Examples
 * include temporarly unreachable mail server, trying to manipulate entities locked by
 * another user ...</p>
 * <p>This is an internal error so there is no need to localize the error message.</p>
 *
 * @author <a href="mailto:meat_for_the_butcher@yahoo.com">Patrik Nyborg</a>
 */
public class SystemException extends Exception
{
	/**
	 * Construct a SystemException with the detailed error message.
	 * @param message the detailed error message.
	 */
	public SystemException(String message)
	{
		super(message);
	}

	/**
	 * Constructs a SystemException with the given cause
	 * @param cause the root cause of the exception
	 */
	public SystemException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Construct a SystemException with the detailed error message and cause.
	 * @param message the detailed error message.
	 * @param cause the throwable that caused this SystemException to get thrown.
	 */
	public SystemException(String message, Throwable cause)
	{
		super(message, cause);
	}
}