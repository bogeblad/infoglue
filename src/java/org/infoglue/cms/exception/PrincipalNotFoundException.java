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
 * <p>Thrown to indicate that a principal did not exist</p>
 *
 * @author <a href="mailto:bogeblad@yahoo.com">Mattias Bogeblad</a>
 */
public class PrincipalNotFoundException extends SystemException
{
	/**
	 * Construct a SystemException with the detailed error message.
	 * @param message the detailed error message.
	 */
	public PrincipalNotFoundException(String message)
	{
		super(message);
	}

	/**
	 * Constructs a SystemException with the given cause
	 * @param cause the root cause of the exception
	 */
	public PrincipalNotFoundException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Construct a SystemException with the detailed error message and cause.
	 * @param message the detailed error message.
	 * @param cause the throwable that caused this SystemException to get thrown.
	 */
	public PrincipalNotFoundException(String message, Throwable cause)
	{
		super(message, cause);
	}
}