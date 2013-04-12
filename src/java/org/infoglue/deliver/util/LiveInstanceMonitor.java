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

package org.infoglue.deliver.util;

import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.TransactionHistoryController;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.DateHelper;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.cms.util.mail.MailServiceFactory;

/**
 *  This thread tests the live servers continuously for signs of downtime or unresponsiveness.
 */

public class LiveInstanceMonitor implements Runnable
{
    private final static Logger logger = Logger.getLogger(LiveInstanceMonitor.class.getName());

    private static LiveInstanceMonitor instance = null;
    
    public static LiveInstanceMonitor getInstance()
    {
    	if(instance == null)
    	{
    		instance = new LiveInstanceMonitor();
    		Thread thread = new Thread(instance);
			thread.start();
    	}
    	
    	return instance;
    }
    
    private Map<String,Boolean> instanceStatus = new HashMap<String,Boolean>();
    private Map<String,Integer> instanceErrorInformation = new HashMap<String,Integer>();

    public Map<String,Boolean> getInstanceStatusMap()
    {
    	return instanceStatus;
    }

    public Boolean getServerStatus(String instanceUrl)
    {
    	return instanceStatus.get(instanceUrl);
    }
    
	private LiveInstanceMonitor()
	{
	}

	public synchronized void run()
	{
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		System.out.println("Starting LiveInstanceMonitor....");
		while(true)
		{
			try
			{
				Thread.sleep(30000);
				logger.info("Validating instances");
				
				validateInstances();
			}
			catch (Exception e) 
			{
				logger.error("Error in monitor:" + e.getMessage(), e);
			}
		}
	}

	/**
	 * This works by calling the status action on each registered deliver instance. 
	 */
	private void validateInstances()
    {
		//System.out.println("validateInstances");
		Map<String,Boolean> newInstanceStatus = new HashMap<String,Boolean>();
	    Map<String,Integer> newInstanceErrorInformation = new HashMap<String,Integer>();
	    newInstanceErrorInformation.putAll(instanceErrorInformation);
	    
		List internalDeliveryUrls = CmsPropertyHandler.getInternalDeliveryUrls();
		Iterator internalDeliveryUrlsIterator = internalDeliveryUrls.iterator();
		while(internalDeliveryUrlsIterator.hasNext())
		{
			String serverBase = "" + internalDeliveryUrlsIterator.next();
			String address = serverBase + "/UpdateCache!test.action";
			
			try
			{
    		    HttpHelper httpHelper = new HttpHelper();
    			String response = httpHelper.getUrlContent(address, new HashMap(), null, 5000);
    			if(response.indexOf("test ok") == -1)
    			    throw new Exception("Got wrong answer");
    			else
    			{
    				Boolean wasError = newInstanceStatus.get(serverBase);
    				newInstanceStatus.put(serverBase, true);
    				newInstanceErrorInformation.remove(serverBase);
    				if(wasError != null && wasError)
    				{
	    				NotificationMessage serverErrorMessage = new NotificationMessage("Server available after being unavailable", serverBase, "SYSTEM", NotificationMessage.SERVER_UNAVAILABLE_SOLVED, "n/a", serverBase);
						TransactionHistoryController.getController().create(serverErrorMessage);
    				}
    			}
    		}
			catch(Exception e)
			{		
				logger.error("Error in instance monitor: " + serverBase + ", Message: " + e.getMessage());

				try
				{
					String cause = "" + e.getMessage();
					if(e instanceof FileNotFoundException)
						cause = "Application not found";
					else if(e instanceof SocketTimeoutException)
						cause = "" + e.getMessage();
					
					NotificationMessage serverErrorMessage = new NotificationMessage("Server down!: " + cause, serverBase, "SYSTEM", NotificationMessage.SERVER_UNAVAILABLE, "n/a", serverBase);
					TransactionHistoryController.getController().create(serverErrorMessage);
				}
				catch (Exception e2) 
				{
					logger.error("Error adding transaction history log for error: " + e2.getMessage(), e);
				}
				
				newInstanceStatus.put(serverBase, false);
				Integer retries = newInstanceErrorInformation.get(serverBase);
				if(retries == null)
				{
					retries = 0;
				}
				newInstanceErrorInformation.put(serverBase, retries+1);

				boolean isDivisibleBy10 = retries % 10 == 0;
				if(retries == 0 || isDivisibleBy10)
				{
					logger.error("A deliver instance is down or unresponsive. Tested instance: " + address + ". Message: " + e.getMessage());
					sendWarningMail(serverBase);
				}
				
				/*
				if(retries == 20)
				{
				    ViewMessageCenterAction.addSystemMessage("administrator", ViewMessageCenterAction.SYSTEM_MESSAGE_TYPE, "alert('One server is down: " + serverBase + "');");
				}
				*/
			}
		}
		
		List publicDeliveryUrls = CmsPropertyHandler.getPublicDeliveryUrls();
		Iterator publicDeliveryUrlsIterator = publicDeliveryUrls.iterator();
		while(publicDeliveryUrlsIterator.hasNext())
		{
			String serverBase = "" + publicDeliveryUrlsIterator.next();
			String address = serverBase + "/UpdateCache!test.action";
			
			try
			{
    		    HttpHelper httpHelper = new HttpHelper();
    			String response = httpHelper.getUrlContent(address, new HashMap(), null, 5000);
    			if(response.indexOf("test ok") == -1)
    			    throw new Exception("Got wrong answer");
    			else
    			{
    				Boolean wasError = newInstanceStatus.get(serverBase);
    				newInstanceStatus.put(serverBase, true);
    				newInstanceErrorInformation.remove(serverBase);
    				if(wasError != null && wasError)
    				{
	    				NotificationMessage serverErrorMessage = new NotificationMessage("Server available after being unavailable", serverBase, "SYSTEM", NotificationMessage.SERVER_UNAVAILABLE_SOLVED, "n/a", serverBase);
						TransactionHistoryController.getController().create(serverErrorMessage);
    				}
    			}
			}
			catch(Exception e)
			{
				logger.error("Error in instance monitor: " + serverBase + ", Message: " + e.getMessage());
				try
				{
					String cause = "" + e.getMessage();
					if(e instanceof FileNotFoundException)
						cause = "Application not found";
					else if(e instanceof SocketTimeoutException)
						cause = "" + e.getMessage();
					
					NotificationMessage serverErrorMessage = new NotificationMessage("Server down!: " + cause, serverBase, "SYSTEM", NotificationMessage.SERVER_UNAVAILABLE, "n/a", serverBase);
					TransactionHistoryController.getController().create(serverErrorMessage);
				}
				catch (Exception e2) 
				{
					logger.error("Error adding transaction history log for error: " + e2.getMessage(), e);
				}
								
				newInstanceStatus.put(serverBase, false);
				Integer retries = newInstanceErrorInformation.get(serverBase);
				if(retries == null)
				{
					retries = 0;
				}
				newInstanceErrorInformation.put(serverBase, retries+1);
				boolean isDivisibleBy10 = retries % 10 == 0;
				if(retries == 0 || isDivisibleBy10)
				{
					logger.error("A deliver instance is down or unresponsive. Tested instance: " + address + ". Message: " + e.getMessage());
				    sendWarningMail(serverBase);
				}

				/*
				if(retries == 20)
				{
				    ViewMessageCenterAction.addSystemMessage("administrator", ViewMessageCenterAction.SYSTEM_MESSAGE_TYPE, "alert('One server is down: " + serverBase + "');");
				}
				*/
			}
		}
		
		instanceStatus = newInstanceStatus;
		instanceErrorInformation = newInstanceErrorInformation;
    }

	
	
	private void sendWarningMail(String serverBase) 
	{
		try
		{
			String subject = "Infoglue Warning[" + serverBase + "]: A deliver instance is down or unresponsive";
			StringBuilder message = new StringBuilder();
			message.append("The cms notices that one of the live instances was unreachable or unresponsive at: " + DateHelper.getFormattedCurrentDateTime("yyyy-MM-dd HH:ss") + "\n");
			message.append("The url tested was:" + serverBase + "\n\n");
			message.append("This should be investigated to ensure that this server receives publications.\n");
			
			MailServiceFactory.getService().sendWarningMail(subject, message.toString());
		}
		catch (Exception e) 
		{
			logger.error("Problem sending warning mail:" + e.getMessage());
		}
	}
}
