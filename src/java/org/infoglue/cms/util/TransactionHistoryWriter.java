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

import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.TransactionHistoryController;


/**
 * This class is a class that logs a message to a Media somewhere (Mostly files).
 * We should utilize log4j or some other logging framework later on.
 *
 * @author Mattias Bogeblad 
 * 
 */
public class TransactionHistoryWriter implements NotificationListener
{	
    private final static Logger logger = Logger.getLogger(TransactionHistoryWriter.class.getName());

    
	/**
	 * Default Constructor	
	 */
	
	public TransactionHistoryWriter()
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
	 * The writer just calls the transactionHistoryController which stores it.
	 */

	public void notify(NotificationMessage notificationMessage)
	{
		try
		{
			logger.info("Update TransactionHistory....");
			TransactionHistoryController.getController().create(notificationMessage);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
