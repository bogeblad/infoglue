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

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.LuceneController;

public class ChangeNotificationController
{
	private final static Logger logger = Logger.getLogger(ChangeNotificationController.class.getName());

	//The singleton
	private static ChangeNotificationController instance = null;

	//TEST
	private static class ThreadLocalNotifications extends ThreadLocal implements NotificationListener
	{
	    public Object initialValue() 
	    {
	    	return new ArrayList();
	    }

	    public List getList() 
	    { 
	    	return (List) super.get(); 
	    }

		public void setContextParameters(Map map) 
		{
		}

		public void notify(NotificationMessage message) 
		{
			list.getList().add(message);
		}

		public void process() 
		{
			//System.out.println("WHY????");
		}
	}

	private static ThreadLocalNotifications list = new ThreadLocalNotifications();
	
	public void put(NotificationMessage notificationMessage) 
	{
	    list.getList().add(notificationMessage);
	}
	
	public void notifyListeners()
	{
		if(list.getList().size() > 0)
			logger.info("Now as the transaction is done and there are items in the notification list - let's notify the deliver app and other listeners...");
		
		//This part asks the listeners to do it's stuff before we send the info to deliver.
		try 
		{
			synchronized (listeners)
			{
				Iterator i = listeners.iterator();
				while(i.hasNext())
				{
					try
					{
						NotificationListener nl = (NotificationListener)i.next();
						if(!unregisteredlisteners.contains(nl))
						{
							logger.info("Notifying the listener to process:" + nl.getClass().getName());
							nl.process();
						}
					}
					catch(ConcurrentModificationException e)
					{
						logger.error("One of the listeners threw an exception but we carry on with the others. Error: " + e.getMessage());
					}
					catch(Exception e)
					{
						logger.error("One of the listeners threw an exception but we carry on with the others. Error: " + e.getMessage(), e);
					}
				}
				listeners.removeAll(unregisteredlisteners);
			}
			synchronized (unregisteredlisteners)
			{
				unregisteredlisteners.clear();			
			}
		} 
		catch (Exception e) 
		{
			logger.error("Error calling listeners to process:" + e.getMessage(), e);
		}
		
		//Prepare and push notifications to deliver
		List internalMessageList = new ArrayList();
		List publicMessageList = new ArrayList();

		Iterator iterator = list.getList().iterator();
		while(iterator.hasNext())
		{
        	NotificationMessage notificationMessage = (NotificationMessage)iterator.next();
			if(notificationMessage.getType() == NotificationMessage.PUBLISHING || notificationMessage.getType() == NotificationMessage.UNPUBLISHING || notificationMessage.getType() == NotificationMessage.SYSTEM)
			{
				publicMessageList.add(notificationMessage);

				if(notificationMessage.getType() == NotificationMessage.SYSTEM)
				{
					internalMessageList.add(notificationMessage);
				}
			}
			else
			{
				internalMessageList.add(notificationMessage);
			}			
			
			iterator.remove();
		}
		
		Hashtable internalMessage = null;
		Hashtable publicMessage = null;
		if(internalMessageList.size() > 0)
		{
			internalMessage = new Hashtable();
			Iterator internalMessageListIterator = internalMessageList.iterator();
			int i = 0;
			while(internalMessageListIterator.hasNext())
			{
				NotificationMessage notificationMessage = (NotificationMessage)internalMessageListIterator.next();
				internalMessage.put(i + ".userName", notificationMessage.getSystemUserName());
				internalMessage.put(i + ".timestamp", notificationMessage.getTimestamp());
				internalMessage.put(i + ".className", notificationMessage.getClassName());
				internalMessage.put(i + ".objectId", notificationMessage.getObjectId());
				internalMessage.put(i + ".objectName", notificationMessage.getObjectName());
				internalMessage.put(i + ".typeId", "" + notificationMessage.getType());
				if(notificationMessage.getExtraInformation() != null)
				{
					for(String key : notificationMessage.getExtraInformation().keySet())
					{
						internalMessage.put(i + "." + key, notificationMessage.getExtraInformation().get(key));
					}
				}
				i++;
			}
		}

		if(publicMessageList.size() > 0)
		{
			publicMessage = new Hashtable();
			Iterator publicMessageListIterator = publicMessageList.iterator();
			int i = 0;
			while(publicMessageListIterator.hasNext())
			{
				NotificationMessage notificationMessage = (NotificationMessage)publicMessageListIterator.next();

				//For mixed env where we want to do cms upgrade first - remove when we release a new version
				if(i == 0)
				{
					publicMessage.put("userName", notificationMessage.getSystemUserName());
					publicMessage.put("timestamp", notificationMessage.getTimestamp());
					publicMessage.put("className", notificationMessage.getClassName());
					publicMessage.put("objectId", notificationMessage.getObjectId());
					publicMessage.put("objectName", notificationMessage.getObjectName());
					publicMessage.put("typeId", "" + notificationMessage.getType());
				}
				
				publicMessage.put(i + ".userName", notificationMessage.getSystemUserName());
				publicMessage.put(i + ".timestamp", notificationMessage.getTimestamp());
				publicMessage.put(i + ".className", notificationMessage.getClassName());
				publicMessage.put(i + ".objectId", notificationMessage.getObjectId());
				publicMessage.put(i + ".objectName", notificationMessage.getObjectName());
				publicMessage.put(i + ".typeId", "" + notificationMessage.getType());
				i++;
			}
		}
		
		try 
		{
			new RemoteCacheUpdater().updateRemoteCaches(internalMessage, publicMessage);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	}
	//TEST
	
	/**
	 * A factory method that makes sure we operate on a singeton.
	 * We assign a couple of standard listeners like a logger and a transactionHistoryLogger. 
	 */
	
	public static ChangeNotificationController getInstance()
	{
		if(instance == null)
		{
			instance = new ChangeNotificationController();
			//instance.registerListener(new FileLogger());
			
			String logTransactions = CmsPropertyHandler.getLogTransactions();
			if(logTransactions == null || !logTransactions.equalsIgnoreCase("false"))
			    instance.registerListener(new TransactionHistoryWriter());
			
			String internalSearchEngine = CmsPropertyHandler.getInternalSearchEngine();
			if(internalSearchEngine.equalsIgnoreCase("lucene"))
			    instance.registerListener(LuceneController.getController());
			
			//instance.registerListener(new RemoteCacheUpdater());
			instance.registerListener(list);
			//instance.registerListener(new WorkflowEngine());
		}
		
		return instance;		
	}

//-------------------- The object stuff ---------------------//

	//List of all listeners.
	private List listeners = new ArrayList();
	
	//List of all listeners that shall be unregistered
	//(to avoid concurrent modification exceptions, and deadlocks)
	private List unregisteredlisteners = new ArrayList();
	
	/**
	 * The standard constructor is private to force use of factory-method.
	 */
	
	private ChangeNotificationController()
	{
	}

	/**
	 * This method registers a new listener to be notified when a new notifiation is available.
	 */
	
	public void registerListener(NotificationListener notificationListener)
	{
		synchronized (listeners) 
		{
			this.listeners.add(notificationListener);
		}
	}

	/**
	 * This method unregisters an existing listener.
	 */
	
	public void unregisterListener(NotificationListener notificationListener)
	{
		this.unregisteredlisteners.add(notificationListener);
	}
	
	/**
	 * This method gets called when a new notification has come. 
	 * It then iterates through the listeners and notifies them.
	 */
	public void addNotificationMessage(NotificationMessage notificationMessage)
	{
		logger.info("Got a new notification:" + notificationMessage.getName() + ":" + notificationMessage.getType() + ":" + notificationMessage.getObjectId() + ":" + notificationMessage.getObjectName());
		synchronized (listeners)
		{
			Iterator i = listeners.iterator();
			while(i.hasNext())
			{
				try
				{
					NotificationListener nl = (NotificationListener)i.next();
					if(!unregisteredlisteners.contains(nl))
					{
						logger.info("Notifying the listener:" + nl.getClass().getName());
						nl.notify(notificationMessage);
					}
				}
				catch(Exception e)
				{
					logger.error("One of the listeners threw an exception but we carry on with the others. Error: " + e.getMessage(), e);
				}
			}
			listeners.removeAll(unregisteredlisteners);
		}
		synchronized (unregisteredlisteners)
		{
			unregisteredlisteners.clear();			
		}
	}	
			

}