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

public class NotificationMessage
{
	public final static int TRANS_CREATE      		= 0;
	public final static int TRANS_UPDATE      		= 1;
	public final static int TRANS_DELETE     		= 2;
	public final static int PUBLISHING        		= 10;
	public final static int DENIED_PUBLISHING 		= 20;
	public final static int UNPUBLISHING      		= 30;
	public final static int SYSTEM      	  		= 100;
	public final static int AUTHENTICATION_SUCCESS  = 200;
	public final static int AUTHENTICATION_FAILED   = 201;
	public final static int AUTHORIZATION_FAILED	= 202;
	
	public final static String TRANS_CREATE_TEXT 	= "create";
	public final static String TRANS_UPDATE_TEXT 	= "update";
	public final static String TRANS_DELETE_TEXT		= "delete";
	public final static String PUBLISHING_TEXT   	= "publishing";
	public final static String DENIED_PUBLISHING_TEXT = "publishing denied";
	public final static String UNPUBLISHING_TEXT   	= "unpublishing";
	public final static String SYSTEM_TEXT   	= "general configuration change";
	public final static String AUTHENTICATION_SUCCESS_TEXT   	= "authentication success";
	public final static String AUTHENTICATION_FAILED_TEXT   	= "authentication failed";
	public final static String AUTHORIZATION_FAILED_TEXT   	= "authorization failed";


	private String name;
	private String systemUserName;
	private int type;
	private String className;
	private Object objectId;
	private String objectName;
	
	public NotificationMessage(String name, String className, String systemUserName, int type, Object objectId, String objectName)
	{
		this.name = name;
		this.className = className;
		this.systemUserName = systemUserName;
		this.type = type;
		this.objectId = objectId;
		this.objectName = objectName;	
	}
	

	public String getName()
	{
		return this.name;
	}

	public String getClassName()
	{
		return this.className;
	}

	public String getSystemUserName()
	{
		return this.systemUserName;
	}

	public int getType()
	{
		return this.type;
	}

	public Object getObjectId()
	{
		return this.objectId;
	}

	public String getObjectName()
	{
		return this.objectName;
	}

	public static String getTransactionTypeName(Integer transactionType)
	{
		switch (transactionType.intValue())
		{
			case (int) (TRANS_CREATE):
				return TRANS_CREATE_TEXT;
			case (TRANS_DELETE):
				return TRANS_DELETE_TEXT;
			case (TRANS_UPDATE):
				return TRANS_UPDATE_TEXT;
			case (PUBLISHING):
				return PUBLISHING_TEXT;
			case (DENIED_PUBLISHING):
				return DENIED_PUBLISHING_TEXT;
			case (AUTHENTICATION_SUCCESS):
				return AUTHENTICATION_SUCCESS_TEXT;
			case (AUTHENTICATION_FAILED):
				return AUTHORIZATION_FAILED_TEXT;
			case (AUTHORIZATION_FAILED):
				return AUTHORIZATION_FAILED_TEXT;
		}
		return "unknown - map " + transactionType + " to correct text";
	}
}