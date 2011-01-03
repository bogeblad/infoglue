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
package org.infoglue.cms.applications.workflowtool.function;

import org.infoglue.cms.entities.management.SystemUserVO;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public class UserProvider extends InfoglueFunction 
{
	/**
	 * 
	 */
	public static final String USER_PARAMETER = "systemuser";
	
	/**
	 * 
	 */
	private static final String USER_PROPERTYSET_PREFIX = "systemuser_";

	/**
	 * 
	 */
	public static final String FIRST_NAME_ATTRIBUTE =  "firstName";
	
	/**
	 * 
	 */
	public static final String LAST_NAME_ATTRIBUTE = "lastName";
	
	/**
	 * 
	 */
	public static final String USER_NAME_ATTRIBUTE = "userName";
	
	/**
	 * 
	 */
	public static final String PASSWORD_ATTRIBUTE = "password";
	
	/**
	 * 
	 */
	public static final String EMAIL_ATTRIBUTE = "email";
	
	/**
	 * 
	 */
	private SystemUserVO systemUserVO = new SystemUserVO();
	
	/**
	 * 
	 */
	public UserProvider()
	{
		super();
	}
	
	/**
	 * 
	 */
	protected void execute() throws WorkflowException 
	{
		populate();
		setParameter(USER_PARAMETER, systemUserVO);
	}
	
	/**
	 * 
	 */
	private void populate()
	{
		final String firstName = getPropertySetDataString(USER_PROPERTYSET_PREFIX + FIRST_NAME_ATTRIBUTE); 
		final String lastName  = getPropertySetDataString(USER_PROPERTYSET_PREFIX + LAST_NAME_ATTRIBUTE); 
		final String userName  = getPropertySetDataString(USER_PROPERTYSET_PREFIX + USER_NAME_ATTRIBUTE); 
		final String password  = getPropertySetDataString(USER_PROPERTYSET_PREFIX + PASSWORD_ATTRIBUTE); 
		final String email     = getPropertySetDataString(USER_PROPERTYSET_PREFIX + EMAIL_ATTRIBUTE); 
		
		systemUserVO.setFirstName(firstName == null ? "" : firstName);
		systemUserVO.setLastName(lastName == null ? "" : lastName);
		systemUserVO.setUserName(userName == null ? "" : userName);
		systemUserVO.setPassword(password == null ? "" : password);
		systemUserVO.setEmail(email == null ? "" : email);
	}
}
