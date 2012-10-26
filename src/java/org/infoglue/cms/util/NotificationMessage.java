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
import java.util.Map;

/**
 * This bean contains all information about what kind of notification message to send to
 * the deliver instaces.
 */
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
	
	public final static int WORKING_NOTIFICATION_QUEUED = 300;
	public final static int WORKING_NOTIFICATION_FAILED = 301;
	public final static int LIVE_NOTIFICATION_QUEUED 	= 302;
	public final static int LIVE_NOTIFICATION_FAILED 	= 303;
	public final static int SERVER_UNAVAILABLE			= 304;
	public final static int LIVE_NOTIFICATION_SOLVED 	= 305;
	public final static int SERVER_UNAVAILABLE_SOLVED	= 306;
	
	public final static String TRANS_CREATE_TEXT 		= "Create";
	public final static String TRANS_UPDATE_TEXT 		= "Update";
	public final static String TRANS_DELETE_TEXT		= "Delete";
	public final static String PUBLISHING_TEXT   		= "Publishing";
	public final static String DENIED_PUBLISHING_TEXT	= "Publishing denied";
	public final static String UNPUBLISHING_TEXT   		= "Unpublishing";
	public final static String SYSTEM_TEXT   			= "General configuration change";
	public final static String AUTHENTICATION_SUCCESS_TEXT   	= "Authentication success";
	public final static String AUTHENTICATION_FAILED_TEXT   	= "Authentication failed";
	public final static String AUTHORIZATION_FAILED_TEXT   		= "Authorization failed";

	public final static String LIVE_NOTIFICATION_SOLVED_TEXT 	= "Publication republished";
	public final static String LIVE_NOTIFICATION_QUEUED_TEXT   	= "Publication queued";
	public final static String LIVE_NOTIFICATION_FAILED_TEXT   	= "Publication failed";
	public final static String SERVER_UNAVAILABLE_TEXT   		= "Server unavailable";
	public final static String SERVER_UNAVAILABLE_SOLVED_TEXT   = "Server available again";

	private long timeStamp = System.currentTimeMillis();
	private String name;
	private String systemUserName;
	private int type;
	private String className;
	private Object objectId;
	private String objectName;
	private Map<String,String> extraInformation = new HashMap<String,String>();

	public NotificationMessage(String name, String className, String systemUserName, int type, Object objectId, String objectName)
	{
		this(name, className, systemUserName, type, objectId, objectName, new HashMap());
	}
	
	public NotificationMessage(String name, String className, String systemUserName, int type, Object objectId, String objectName, Map extraInformation)
	{
		this.name = name;
		this.className = className;
		this.systemUserName = systemUserName;
		this.type = type;
		this.objectId = objectId;
		this.objectName = objectName;	
		this.extraInformation = extraInformation;
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

	public long getTimestamp()
	{
		return this.timeStamp;
	}

	public Map<String, String> getExtraInformation() 
	{
		return extraInformation;
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

			case (LIVE_NOTIFICATION_QUEUED):
				return LIVE_NOTIFICATION_QUEUED_TEXT;
			case (LIVE_NOTIFICATION_FAILED):
				return LIVE_NOTIFICATION_FAILED_TEXT;
			case (LIVE_NOTIFICATION_SOLVED):
				return LIVE_NOTIFICATION_SOLVED_TEXT;
			case (SERVER_UNAVAILABLE):
				return SERVER_UNAVAILABLE_TEXT;
			case (SERVER_UNAVAILABLE_SOLVED):
				return SERVER_UNAVAILABLE_SOLVED_TEXT;
		}
		return "unknown - map " + transactionType + " to correct text";
	}
}