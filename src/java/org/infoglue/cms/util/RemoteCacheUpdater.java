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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.infoglue.cms.security.InfoGluePrincipal;


/**
 * This class is a class that sends a update-message to all registered instances of delivery-engine.
 *
 * @author Mattias Bogeblad 
 * 
 */

public class RemoteCacheUpdater implements NotificationListener
{	
    private final static Logger logger = Logger.getLogger(RemoteCacheUpdater.class.getName());

    //A list of system changes that will either be published upon the next publication or on a manual publishing of them.
    private static List waitingSystemNotificationMessages = new ArrayList();
    
	/**
	 * Default Constructor	
	 */
	
	public RemoteCacheUpdater()
	{
	}

	/** 
	 * This method sets the context/arguments the listener should operate with. Could be debuglevels and stuff 
	 * like that.
	 */
	
	public void setContextParameters(Map map)
	{
	}
	
	public static List getSystemNotificationMessages()
	{
	    return waitingSystemNotificationMessages;
	}

	public static void clearSystemNotificationMessages()
	{
	    synchronized(waitingSystemNotificationMessages)
	    {
	        waitingSystemNotificationMessages.clear();
	    }
	}

	/**
	 * This method gets called when a new NotificationMessage is available.
	 * The writer just calls the transactionHistoryController which stores it.
	 */
	
	public void notify(NotificationMessage notificationMessage)
	{
		try
		{
			logger.info("Update Remote caches....");
			updateRemoteCaches(notificationMessage);
			logger.info("Done updating remote caches....");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * This method serializes the notificationMessage and calls the remote actions.
	 * As a content-tool can have several preview instances we iterate through the instances that have 
	 * been indexed in the propertyfile starting with 0.
	 */
	
	public void updateRemoteCaches(NotificationMessage notificationMessage) throws Exception
	{
		throw new Exception("Wrong - this message handler should not be called...");
		/*
		Hashtable hashedMessage = notificationMessageToHashtable(notificationMessage);
		
	    List urls = new ArrayList();
		
		if(notificationMessage.getType() == NotificationMessage.PUBLISHING || notificationMessage.getType() == NotificationMessage.UNPUBLISHING || notificationMessage.getType() == NotificationMessage.SYSTEM)
		{
			urls.addAll(CmsPropertyHandler.getPublicDeliveryUrls());

			if(notificationMessage.getType() == NotificationMessage.SYSTEM)
			{
				urls.addAll(CmsPropertyHandler.getInternalDeliveryUrls());
			}

		}
		else
		{
			urls.addAll(CmsPropertyHandler.getInternalDeliveryUrls());
		}
		
		Iterator urlsIterator = urls.iterator();
		while(urlsIterator.hasNext())
		{
		    String deliverUrl = (String)urlsIterator.next();
			String address = deliverUrl + "/" + CmsPropertyHandler.getCacheUpdateAction();
			logger.info("Updating cache at " + address);
			try
			{
				String response = postToUrl(address, hashedMessage);
			}
			catch(Exception e)
			{
				logger.warn("Error updating cache at " + address + ":" + e.getMessage());
			}
		}
		*/
	}
	

	/**
	 * This method serializes the notificationMessage and calls the remote actions.
	 * As a content-tool can have several preview instances we iterate through the instances that have 
	 * been indexed in the propertyfile starting with 0.
	 */
	public void updateRemoteCaches(Hashtable internalMessage, Hashtable publicMessage) throws Exception
	{		
		if((internalMessage == null && publicMessage == null) || (internalMessage.size() == 0 && publicMessage.size() == 0))
			return;
			
		if(internalMessage != null && internalMessage.size() > 0)
		{
			List internalUrls = CmsPropertyHandler.getInternalDeliveryUrls();
		
			Iterator urlsIterator = internalUrls.iterator();
			while(urlsIterator.hasNext())
			{
			    String deliverUrl = (String)urlsIterator.next();
				String address = deliverUrl + "/" + CmsPropertyHandler.getCacheUpdateAction();
				logger.info("Updating cache at " + address);
				if(address.indexOf("@") > -1)
				{
					logger.error("Skipping updating cache at " + address + ". You probably have not defined the correct addresses in application settings.");
				}
				else
				{
					try
					{
						String response = postToUrl(address, internalMessage);
					}
					catch(Exception e)
					{
						logger.error("Error updating cache at " + address + ":" + e.getMessage());
					}
				}
			}
	    }

	    if(publicMessage != null && publicMessage.size() > 0)
	    {
		    List publicUrls = CmsPropertyHandler.getPublicDeliveryUrls();

			Iterator urlsIterator = publicUrls.iterator();
			while(urlsIterator.hasNext())
			{
			    String deliverUrl = (String)urlsIterator.next();
				String address = deliverUrl + "/" + CmsPropertyHandler.getCacheUpdateAction();
				logger.info("Updating cache at " + address);
				if(address.indexOf("@") > -1)
				{
					logger.error("Skipping updating cache at " + address + ". You probably have not defined the correct live addresses in application settings.");
				}
				else
				{
					try
					{
						String response = postToUrl(address, publicMessage);
					}
					catch(Exception e)
					{
						logger.error("Error updating cache at " + address + ":" + e.getMessage());
					}
				}
			}
	    }

	}

	private Hashtable notificationMessageToHashtable(NotificationMessage notificationMessage)
	{
		Hashtable hash = new Hashtable();

		logger.info("Serializing:" + notificationMessage);
		
		hash.put("className", notificationMessage.getClassName());
		hash.put("objectId", notificationMessage.getObjectId());
		hash.put("objectName", notificationMessage.getObjectName());
		hash.put("typeId", "" + notificationMessage.getType());
				
		return hash;		
	}
	

    /**
     * This method post information to an URL and returns a string.It throws
     * an exception if anything goes wrong.
     * (Works like most 'doPost' methods)
     * 
     * @param urlAddress The address of the URL you would like to post to.
     * @param inHash The parameters you would like to post to the URL.
     * @return The result of the postToUrl method as a string.
     * @exception java.lang.Exception
     */
    
    private String postToUrl(String urlAddress, Hashtable inHash) throws Exception
    {        
        URL url = new URL(urlAddress);
        URLConnection urlConn = url.openConnection();
        urlConn.setConnectTimeout(10000);
        urlConn.setReadTimeout(10000);
        urlConn.setAllowUserInteraction(false); 
        urlConn.setDoOutput (true); 
        urlConn.setDoInput (true); 
        urlConn.setUseCaches (false); 
        urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        PrintWriter printout = new PrintWriter(urlConn.getOutputStream(), true); 
        String argString = "";
        if(inHash != null)
        {
            argString = toEncodedString(inHash);
        }
        printout.print(argString);
        printout.flush();
        printout.close (); 
        InputStream inStream = null;
        inStream = urlConn.getInputStream();
        InputStreamReader inStreamReader = new InputStreamReader(inStream);
        BufferedReader buffer = new BufferedReader(inStreamReader);            
        StringBuffer strbuf = new StringBuffer();   
        String line; 
        while((line = buffer.readLine()) != null) 
        {
            strbuf.append(line); 
        }                                              
        String readData = strbuf.toString();   
        buffer.close();
        return readData;             
    }
  
  
    /**
	 * Encodes a hash table to an URL encoded string.
	 * 
	 * @param inHash The hash table you would like to encode
	 * @return A URL encoded string.
	 */
		
	private String toEncodedString(Hashtable inHash) throws Exception
	{
	    StringBuffer buffer = new StringBuffer();
	    Enumeration names = inHash.keys();
	    while(names.hasMoreElements())
	    {
	        String name = names.nextElement().toString();
	        String value = inHash.get(name).toString();
	        buffer.append(URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8"));
	        if(names.hasMoreElements())
	        {
	            buffer.append("&");
	        }
	    }
	    return buffer.toString();
	}
	
	
    /**
     * As the name indicates, this method takes a URL-encoded string and 
     * converts it to a normal javastring. That is, all Mime-encoding is 
     * removed.
     * 
     * @param encoded The encoded string.
     * @return An decoded string.
     */
		
    public String decodeHTTPEncodedString(String encoded) 
    {
		StringBuffer decoded = new StringBuffer(encoded.length());
		int i = 0;
		int j = 0;

		while (i < encoded.length()) {
		    char tecken = encoded.charAt(i);
			i++;

			if (tecken == '+')
			    tecken = ' ';
			else if (tecken == '%') {
			    tecken = (char)Integer.parseInt(encoded.substring(i,i+2), 16);
			    i+=2;
			    }
			decoded.append(tecken);
			j++;
		    }
		return new String(decoded);
    }


	/**
	 * Converts a serialized hashtable in the url-encoded format to
	 * a Hashtable that one can use within the program. 
	 * A good technique when exchanging information between different
	 * applications.
	 * 
	 * @param encodedstrang The url-encoded string.
	 * @return A Hashtable.
	 */

	public Hashtable httpEncodedStringToHashtable(String encodedstrang) 
	{
	    Hashtable anropin = new Hashtable();
	    StringTokenizer andsplitter = new StringTokenizer(encodedstrang,"&");
	    while (andsplitter.hasMoreTokens()) 
	    {
	        String namevaluepair = andsplitter.nextToken();
            StringTokenizer equalsplitter = new StringTokenizer(namevaluepair,"=");
            if (equalsplitter.countTokens() == 2) 
            {
                String name = equalsplitter.nextToken();
                String value = equalsplitter.nextToken();
                anropin.put(decodeHTTPEncodedString(name),decodeHTTPEncodedString(value));
            }
        }
        return anropin;
    }

	public static synchronized void pushAndClearSystemNotificationMessages(InfoGluePrincipal principal)
	{
		List localMessages = new ArrayList();
        List messages = RemoteCacheUpdater.getSystemNotificationMessages();
        synchronized(messages)
        {
        	localMessages.addAll(messages);
        	messages.clear();
        }

        Iterator localMessagesIterator = localMessages.iterator(); 
        while(localMessagesIterator.hasNext())
        {
			NotificationMessage notificationMessage = (NotificationMessage)localMessagesIterator.next();
			notificationMessage = new NotificationMessage("PushAndClearSystemNotificationMessages", "" + notificationMessage.getClassName(), principal.getName(), NotificationMessage.SYSTEM, notificationMessage.getObjectId(), notificationMessage.getObjectName());
	        ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
        }
	}

}
