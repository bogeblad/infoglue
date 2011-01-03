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

import java.util.Calendar;
import java.util.Map;
import java.util.logging.Logger;


/**
 * This class is a class that logs a message to a Media somewhere (Mostly files).
 * We should utilize log4j or some other logging framework later on.
 *
 * @author Mattias Bogeblad 
 * 
 */
public class FileLogger implements NotificationListener
{	
    private final static Logger logger = Logger.getLogger(FileLogger.class.getName());

	/**
	 * Default Constructor	
	 */
	
	public FileLogger()
	{
	}

	/** 
	 * This method sets the context/arguments the Logger should operate with. Could be debuglevels and stuff 
	 * like that.
	 */
	
	public void setContextParameters(Map map)
	{
	}
	
	/**
	 * This method gets called when a new NotificationMessage is available.
	 * I've decided to use the Java 1.4.1 Logging Api for this. Change this later to put these in a different file.
	 */

	public void notify(NotificationMessage notificationMessage)
	{
		String hostName = "tempHost.se";
		String message = "[" + Calendar.getInstance().getTime().toString() + "] - [" + notificationMessage.getSystemUserName() + "@" + hostName + "]:[" + notificationMessage.getName() + "]";
		logger.info(message);		
	}

}
