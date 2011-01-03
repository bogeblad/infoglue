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

import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;

public class MailService 
{
	private final static Logger logger = Logger.getLogger(MailService.class.getName());

    // The mail session.
    private Session session;


	/**
     * Creates a MailServices object and initializes it with the specified mail session.
     */

    public MailService(Session session) 
    {
        this.session = session;
    }

    /**
     * 
     */
    public MimeMessage createMessage()
    {
    	return new MimeMessage(this.session);
    }
    
    /**
     * 
     */
    public void send(final Message message) throws SystemException
    {
	    try 
	    {
			Transport.send(message);
	    }
	    catch(MessagingException e) 
	    {
	    	e.printStackTrace();
	      	throw new SystemException("Unable to send message.", e);
	    }
	    
    }

	/**
	 *
	 * @param from the sender of the email.
	 * @param to the recipient of the email.
	 * @param subject the subject of the email.
	 * @param content the body of the email.
	 * @throws SystemException if the email couldn't be sent due to some mail server exception.
	 */
	public void send(String from, String to, String bcc, String subject, String content) throws SystemException 
	{
	    send(createMessage(from, to, bcc, subject, content));
	}

	/**
	 *
	 * @param from the sender of the email.
	 * @param to the recipient of the email.
	 * @param subject the subject of the email.
	 * @param content the body of the email.
	 * @throws SystemException if the email couldn't be sent due to some mail server exception.
	 */
	public void send(String from, String to, String subject, String content, String contentType, String encoding) throws SystemException 
	{
		final Message message = createMessage(from, to, null, subject, content, contentType, encoding);
	 
		try 
		{
			Transport.send(message);
		} 
	    catch(MessagingException e) 
	    {
	    	e.printStackTrace();
	      	throw new SystemException("Unable to send message.", e);
	    }

	}


	/**
	 *
	 * @param from the sender of the email.
	 * @param to the recipient of the email.
	 * @param subject the subject of the email.
	 * @param content the body of the email.
	 * @throws SystemException if the email couldn't be sent due to some mail server exception.
	 */
	/*
	public void sendEmail(String from, String to, String bcc, String subject, String content, String encoding) throws SystemException 
	{
		sendEmail(from, to, null, bcc, subject, content, encoding);
	}
	*/
	
	/**
	 *
	 * @param from the sender of the email.
	 * @param to the recipient of the email.
	 * @param subject the subject of the email.
	 * @param content the body of the email.
	 * @throws SystemException if the email couldn't be sent due to some mail server exception.
	 */
	/*
	public void sendEmail(String from, String to, String cc, String bcc, String replyTo, String subject, String content, String encoding) throws SystemException 
	{
        String contentType = CmsPropertyHandler.getMailContentType();
        if(contentType == null || contentType.length() == 0)
            contentType = "text/html";

	    if(contentType.equalsIgnoreCase("text/html"))
	    	sendHTML(from, to, cc, bcc, replyTo, subject, content, encoding);
	    else
	        sendPlain(from, to, cc, bcc, replyTo, subject, content, encoding);
	}
	*/
	/**
	 *
	 * @param from the sender of the email.
	 * @param to the recipient of the email.
	 * @param subject the subject of the email.
	 * @param content the body of the email.
	 * @throws SystemException if the email couldn't be sent due to some mail server exception.
	 */
	/*
	public void sendEmail(String contentType, String from, String to, String cc, String bcc, String subject, String content, String encoding) throws SystemException 
	{
  	    sendEmail(contentType, from, to, cc, bcc, null, subject, content, encoding);
	}
	*/
	
	/**
	 *
	 * @param from the sender of the email.
	 * @param to the recipient of the email.
	 * @param subject the subject of the email.
	 * @param content the body of the email.
	 * @throws SystemException if the email couldn't be sent due to some mail server exception.
	 */
	public void sendEmail(String contentType, String from, String to, String cc, String bcc, String bounceAddress, String replyTo, String subject, String content, String encoding) throws SystemException 
	{
   	    if(contentType.equalsIgnoreCase("text/html"))
	    	sendHTML(from, to, cc, bcc, bounceAddress, replyTo, subject, content, encoding);
	    else
	        sendPlain(from, to, cc, bcc, bounceAddress, replyTo, subject, content, encoding);
	}

	/**
	 *
	 * @param from the sender of the email.
	 * @param to the recipient of the email.
	 * @param subject the subject of the email.
	 * @param content the body of the email.
	 * @throws SystemException if the email couldn't be sent due to some mail server exception.
	 */
	public void sendHTML(String from, String to, String cc, String bcc, String bounceAddress, String replyTo, String subject, String content, String encoding) throws SystemException 
	{
		try 
		{
			HtmlEmail email = new HtmlEmail();
		    String mailServer = CmsPropertyHandler.getMailSmtpHost();
		    String mailPort = CmsPropertyHandler.getMailSmtpPort();
		    String systemEmailSender = CmsPropertyHandler.getSystemEmailSender();
		    
		    email.setHostName(mailServer);
		    if(mailPort != null && !mailPort.equals(""))
		    	email.setSmtpPort(Integer.parseInt(mailPort));
		    
		  	boolean needsAuthentication = false;
		  	try 
		  	{
				needsAuthentication = new Boolean(CmsPropertyHandler.getMailSmtpAuth()).booleanValue();
		  	} 
		  	catch (Exception ex) 
		  	{
				needsAuthentication = false;
		  	}
		  	
		  	if (needsAuthentication) 
		  	{
				final String userName = CmsPropertyHandler.getMailSmtpUser();
				final String password = CmsPropertyHandler.getMailSmtpPassword();
				
				email.setAuthentication(userName, password);
			} 
		    
		    email.setBounceAddress(systemEmailSender);
		    email.setCharset(encoding);
		   
		    if(logger.isInfoEnabled())
		    {
		    	logger.info("systemEmailSender:" + systemEmailSender);
		    	logger.info("to:" + to);
		    	logger.info("from:" + from);
		    	logger.info("mailServer:" + mailServer);
		    	logger.info("mailPort:" + mailPort);
		    	logger.info("cc:" + cc);
		    	logger.info("bcc:" + bcc);
		    	logger.info("replyTo:" + replyTo);
		    	logger.info("subject:" + subject);
		    }
		    
		    if(to.indexOf(";") > -1)
		    {
		    	cc = to;
		    	to = from;
		    }
		    
		    String limitString = CmsPropertyHandler.getEmailRecipientLimit();
		    if(limitString != null && !limitString.equals("-1"))
		    {
		    	try
		    	{
		    		Integer limit = new Integer(limitString);
		    		int count = 0;
		    		if(cc != null)
		    			count = count + cc.split(";").length;
		    		if(bcc != null)
		    			count = count + bcc.split(";").length;
		    		
		    		logger.info("limit: " + limit + ", count: " + count);
		    		if(count > limit)
		    			throw new Exception("You are not allowed to send mail to more than " + limit + " recipients at a time. This is specified in app settings.");
		    	}
		    	catch (NumberFormatException e) 
		    	{
		    		logger.error("Exception validating number of recipients in mailservice:" + e.getMessage(), e);
				}
		    }
		    
		    email.addTo(to, to);
		    email.setFrom(from, from);
		    if(cc != null)
		    	email.setCc(createInternetAddressesList(cc));
		    if(bcc != null)
		    	email.setBcc(createInternetAddressesList(bcc));
		    if(replyTo != null)
		    	email.setReplyTo(createInternetAddressesList(replyTo));
		    
		    email.setSubject(subject);
		    
		    email.setHtmlMsg(content);
	
		    email.setTextMsg("Your email client does not support HTML messages");
	
		    email.send();

	    	logger.info("Email sent!");
		} 
	    catch (Exception e) 
	    {
	    	logger.error("An error occurred when we tried to send this mail:" + e.getMessage(), e);
	        throw new SystemException("An error occurred when we tried to send this mail:" + e.getMessage(), e);
	    }
	}
 

	/**
	 *
	 * @param from the sender of the email.
	 * @param to the recipient of the email.
	 * @param subject the subject of the email.
	 * @param content the body of the email.
	 * @throws SystemException if the email couldn't be sent due to some mail server exception.
	 */
	public void sendPlain(String from, String to, String cc, String bcc, String bounceAddress, String replyTo, String subject, String content, String encoding) throws SystemException 
	{
		try 
		{
		    SimpleEmail email = new SimpleEmail();
		    String mailServer = CmsPropertyHandler.getMailSmtpHost();
		    String mailPort = CmsPropertyHandler.getMailSmtpPort();
		    String systemEmailSender = CmsPropertyHandler.getSystemEmailSender();
		    
		    email.setHostName(mailServer);
		    if(mailPort != null && !mailPort.equals(""))
		    	email.setSmtpPort(Integer.parseInt(mailPort));
		    
		  	boolean needsAuthentication = false;
		  	try 
		  	{
				needsAuthentication = new Boolean(CmsPropertyHandler.getMailSmtpAuth()).booleanValue();
		  	} 
		  	catch (Exception ex) 
		  	{
				needsAuthentication = false;
		  	}
		  	
		  	if (needsAuthentication) 
		  	{
				final String userName = CmsPropertyHandler.getMailSmtpUser();
				final String password = CmsPropertyHandler.getMailSmtpPassword();
				
				email.setAuthentication(userName, password);
			} 
		    
		    email.setBounceAddress(systemEmailSender);
		    email.setCharset(encoding);
		   
		    if(to.indexOf(";") > -1)
		    {
		    	cc = to;
		    	to = from;
		    }

		    String limitString = CmsPropertyHandler.getEmailRecipientLimit();
		    if(limitString != null && !limitString.equals("-1"))
		    {
		    	try
		    	{
		    		Integer limit = new Integer(limitString);
		    		int count = 0;
		    		if(cc != null)
		    			count = count + cc.split(";").length;
		    		if(bcc != null)
		    			count = count + bcc.split(";").length;
		    		
		    		logger.info("limit: " + limit + ", count: " + count);
		    		if(count > limit)
		    			throw new Exception("You are not allowed to send mail to more than " + limit + " recipients at a time. This is specified in app settings.");
		    	}
		    	catch (NumberFormatException e) 
		    	{
		    		logger.error("Exception validating number of recipients in mailservice:" + e.getMessage(), e);
				}
		    }

		    email.addTo(to, to);
		    email.setFrom(from, from);
		    
		    if(cc != null)
		    	email.setCc(createInternetAddressesList(cc));
		    if(bcc != null)
			    email.setBcc(createInternetAddressesList(bcc));
		    if(replyTo != null)
			    email.setReplyTo(createInternetAddressesList(replyTo));
		    
		    email.setSubject(subject);
		    email.setMsg(content);
	
		    email.send();
		} 
	    catch (Exception e) 
	    {
	    	logger.error("An error occurred when we tried to send this mail:" + e.getMessage(), e);
	    }
	    
	    /*
		final Message message = createMessage(from, to, bcc, subject, content, contentType, encoding);
	 
		try 
		{
			Transport.send(message);
		} 
	    catch(MessagingException e) 
	    {
	    	e.printStackTrace();
	      	throw new SystemException("Unable to send message.", e);
	    }
	    */
	}

	/**
	 *
	 */
	private Message createMessage(String from, String to, String bcc, String subject, String content) throws SystemException
	{
		try 
		{
	    	final Message message = new MimeMessage(this.session);

	    	message.setContent(content, "text/html");
			message.setFrom(createInternetAddress(from));
	    	message.setRecipient(Message.RecipientType.TO, createInternetAddress(to));
			if(bcc != null)
			    message.setRecipients(Message.RecipientType.BCC, createInternetAddresses(bcc));
	        message.setSubject(subject);
	        message.setText(content);
	        message.setDataHandler(new DataHandler(new StringDataSource(content, "text/html"))); 
	
	        return message;
	    } 
	    catch(MessagingException e) 
	    {
	        throw new Bug("Unable to create the message.", e);
	    }
	}

	/**
	 *
	 */
	private Message createMessage(String from, String to, String bcc, String subject, String content, String contentType, String encoding) throws SystemException 
	{
		try 
		{
			final Message message = new MimeMessage(this.session);
	    	String contentTypeWithEncoding = contentType+";charset="+encoding;

			//message.setContent(content, contentType);
			message.setFrom(createInternetAddress(from));
			message.setRecipient(Message.RecipientType.TO, createInternetAddress(to));
			if(bcc != null)
			    message.setRecipients(Message.RecipientType.BCC, createInternetAddresses(bcc));
			//message.setSubject(subject);

	        ((MimeMessage)message).setSubject(subject, encoding);
	        //message.setText(content);
	        message.setDataHandler(new DataHandler(new StringDataSource(content, contentTypeWithEncoding, encoding)));
	        //message.setText(content);
	        //message.setDataHandler(new DataHandler(new StringDataSource(content, "text/html"))); 

			return message;
		} 
		catch(MessagingException e) 
		{
			throw new Bug("Unable to create the message.", e);
		}
	}
	
	/**
	 *
	 */
	private Address createInternetAddress(String address) 
	{
		try 
		{
	        return new InternetAddress(address);
	    } 
	    catch(AddressException e) 
	    {
	        throw new Bug("Badly formatted email address [" + address + "].", e);
	    }
	}
	
	/**
	 *
	 */
	private Address[] createInternetAddresses(String emailAddressString) throws SystemException
	{
	    String[] emailAddresses = emailAddressString.split(";");
	    
	    Address[] addresses = new Address[emailAddresses.length];
	    for(int i=0; i<emailAddresses.length; i++)
	    {
	        String email = emailAddresses[i];
	        try 
			{
	            addresses[i] = new InternetAddress(email);
	        } 
		    catch(AddressException e) 
		    {
		        throw new SystemException("Badly formatted email address [" + email + "].", e);
		    }
	    }
	    
	    return addresses;
	}

	/**
	 *
	 */
	private List createInternetAddressesList(String emailAddressString) throws SystemException
	{
	    String[] emailAddresses = emailAddressString.split(";");
	    
	    List addresses = new ArrayList();
	    for(int i=0; i<emailAddresses.length; i++)
	    {
	        String email = emailAddresses[i];
	        try 
			{
	            addresses.add(new InternetAddress(email));
	        } 
		    catch(AddressException e) 
		    {
		        throw new SystemException("Badly formatted email address [" + email + "].", e);
		    }
	    }
	    
	    return addresses;
	}
	
}