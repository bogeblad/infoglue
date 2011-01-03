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

package org.infoglue.cms.util.mail;

import java.util.Properties;

import javax.mail.Session;

import org.infoglue.cms.util.CmsPropertyHandler;


/**
 * Factory for creating MailService objects.
 *
 * @author Mattias Bogeblad
 */

public class MailServiceFactory 
{

	// The singleton mail session; shared by all MailService objects.
  	private static Session session;


	/**
   	 * Returns a MailService object.
     */
  	
  	public static synchronized MailService getService() throws Exception 
  	{
	    if(session == null) 
	    {
    		session = initializeSession();
			//session.setDebug(true);
    	}
    
    	return new MailService(session);
  	}


	/**
   	 * Static class; don't allow instantiation.
   	 */
	
  	private MailServiceFactory() {}


  	/**
	 * Initializes and constructs the shared mail session.
	 * Whenever a <code>mail.smtp.auth</code> property key has a <cdoe>true</code>
	 * value - which means that connection needs to be authenticated, keys
	 * <code>mail.smtp.user</code> and <code>mail.smtp.password</code> are
	 * used as principal information to be used to authenticate connection to
	 * specified SMTP server.
	 * @return SMTP session
	 */
	private static Session initializeSession() throws Exception 
	{
		Properties properties = CmsPropertyHandler.getProperties();
		if(CmsPropertyHandler.getMailSmtpHost() != null)
			properties.setProperty("mail.smtp.host", CmsPropertyHandler.getMailSmtpHost());
		if(CmsPropertyHandler.getMailSmtpPort() != null)
			properties.setProperty("mail.smtp.port", CmsPropertyHandler.getMailSmtpPort());
		if(CmsPropertyHandler.getMailSmtpAuth() != null)
			properties.setProperty("mail.smtp.auth", CmsPropertyHandler.getMailSmtpAuth());
		if(CmsPropertyHandler.getMailSmtpUser() != null)
			properties.setProperty("mail.smtp.user", CmsPropertyHandler.getMailSmtpUser());
		if(CmsPropertyHandler.getMailSmtpPassword() != null)
			properties.setProperty("mail.smtp.password", CmsPropertyHandler.getMailSmtpPassword());

		Properties props = new Properties();

	  	boolean needsAuthentication = false;
	  	try 
	  	{
			needsAuthentication = new Boolean((String)properties.get("mail.smtp.auth")).booleanValue();
	  	} 
	  	catch (Exception ex) 
	  	{
			needsAuthentication = false;
	  	}
	  	
	  	if (needsAuthentication) 
	  	{
			final String uName = (String)(String)properties.get("mail.smtp.user");
			final String uPass = (String)(String)properties.get("mail.smtp.password");
			
			javax.mail.Authenticator authenticator = new javax.mail.Authenticator() 
			{
				protected javax.mail.PasswordAuthentication getPasswordAuthentication() 
				{
					return new javax.mail.PasswordAuthentication(uName, uPass);
			  	}
			};
			
			return Session.getInstance(properties, authenticator);
		} 
		else 
		{
			return Session.getInstance(properties);
	  	}
	}
}