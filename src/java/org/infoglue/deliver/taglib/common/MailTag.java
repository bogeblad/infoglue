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

package org.infoglue.deliver.taglib.common;

import javax.mail.internet.AddressException;
import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.infoglue.cms.util.mail.MailServiceFactory;
import org.infoglue.deliver.taglib.TemplateControllerTag;

/**
 * A new simple MailTag to replace in part the apache commons mail-taglib which don't report errors.		
 */

public class MailTag extends TemplateControllerTag 
{
    private final static Logger logger = Logger.getLogger(MailTag.class.getName());

	private static final long serialVersionUID = 4050206323348354355L;
	
	private String defaultEmailRegexp = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
	private String emailRegexp;
	
	private String from;
	private String to;
	private String cc;
	private String bcc;
	private String recipients;
	private String bounceAddresses;
	private String replyToAddress;
	private String subject;
	private String type;
	private String charset;
	private String message;
	private String validationRegexp;
	
    public MailTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {		
		try
        {
			if(validationRegexp != null)
				emailRegexp = validationRegexp;
			else
				emailRegexp = defaultEmailRegexp;
					
			if(bounceAddresses != null)
				bounceAddresses = bounceAddresses.trim().toLowerCase();
			
			if(replyToAddress != null)
				replyToAddress = replyToAddress.trim().toLowerCase();

			from = from.trim().toLowerCase();
			to = to.trim().toLowerCase();
			
			boolean fromOk = from.matches(emailRegexp);
			if(!fromOk)
				throw new AddressException("Invalid from address:" + from);

			boolean toOk = to.matches(emailRegexp);
			if(!toOk)
				throw new AddressException("Invalid to address:" + to);
			
			if(cc != null && !cc.equals(""))
			{
				StringBuffer sb = new StringBuffer();
				String[] emailAddresses = cc.split(";");
			    for(int i=0; i<emailAddresses.length; i++)
			    {
			        String email = emailAddresses[i].trim().toLowerCase();
		        	boolean emailOk = email.matches(emailRegexp);
	    			if(!emailOk && emailAddresses.length == 1)
		        	{
		        		throw new AddressException("Invalid cc address:" + email);
		        	}
		        	else if(emailOk)
		        	{
		        		if(sb.length() > 0)
		        			sb.append(";");
		        		sb.append(email);
		        	}
			    }
			    
			    cc = sb.toString();
			    if(cc.equals(""))
			    	cc = null;
			}
			
			if(cc != null && cc.equals(""))
		    	cc = null;

			if(bcc == null && recipients != null)
				bcc = recipients;
				
			if(bcc != null && !bcc.equals(""))
			{
				StringBuffer sb = new StringBuffer();
				String[] emailAddresses = bcc.split(";");
			    for(int i=0; i<emailAddresses.length; i++)
			    {
			        String email = emailAddresses[i].trim().toLowerCase();
		        	boolean emailOk = email.matches(emailRegexp);
	    			if(!emailOk && emailAddresses.length == 1)
		        	{
		        		throw new AddressException("Invalid bcc/recipients address:" + email);
		        	}
		        	else if(emailOk)
		        	{
		        		if(sb.length() > 0)
		        			sb.append(";");
		        		sb.append(email);
		        	}
			    }
			    
			    bcc = sb.toString();
			    if(bcc.equals(""))
			    	bcc = null;
			}
			
		    if(bcc != null && bcc.equals(""))
		    	bcc = null;

			if(type == null)
				type = "text/html";
			if(charset == null)
				charset = "utf-8";
						
			MailServiceFactory.getService().sendEmail(type, from, to, cc, bcc, bounceAddresses, replyToAddress, subject, message, charset);
			setResultAttribute(true);
        } 
		catch (AddressException e)
        {
			logger.warn("Problem sending mail due to faulty addresses:" + e.getMessage());
			logger.warn("	from:" + from);
			logger.warn("	to:" + to);
			logger.warn("	cc:" + cc);
			logger.warn("	bcc:" + bcc);
			logger.warn("	Subject:" + subject);
			logger.warn("	message:" + message);
			setResultAttribute(false);
			pageContext.setAttribute("commonMailTagException", e);
        }
		catch (Exception e)
        {
			e.printStackTrace();
			logger.error("Problem sending mail:" + e.getMessage());
			logger.error("	from:" + from);
			logger.error("	to:" + to);
			logger.error("	cc:" + cc);
			logger.error("	bcc:" + bcc);
			logger.error("	bounceAddresses:" + bounceAddresses);
			logger.error("	replyToAddress:" + replyToAddress);
			logger.error("	Subject:" + subject);
			logger.error("	message:" + message);
			setResultAttribute(false);
			pageContext.setAttribute("commonMailTagException", e);
        }
		
		type = null;
		charset = null;
		recipients = null;
		bounceAddresses = null;
		replyToAddress = null;
		cc = null;
		bcc = null;
		validationRegexp = null;
		
        return EVAL_PAGE;
    }

	public void setFrom(String from) throws JspException
	{
		this.from = evaluateString("MailTag", "from", from);
	}

	public void setTo(String to) throws JspException
	{
		this.to = evaluateString("MailTag", "to", to);
	}
	
	/**
	 * @deprecated - use bcc instead
	 * @param recipients
	 * @throws JspException
	 */
	public void setRecipients(String recipients) throws JspException
	{
		this.recipients = evaluateString("MailTag", "recipients", recipients);
	}

	public void setCc(String cc) throws JspException
	{
		this.cc = evaluateString("MailTag", "cc", cc);
	}

	public void setBcc(String bcc) throws JspException
	{
		this.bcc = evaluateString("MailTag", "bcc", bcc);
	}

	public void setReplyToAddress(String replyToAddress) throws JspException
	{
		this.replyToAddress = evaluateString("MailTag", "replyToAddress", replyToAddress);
	}

	public void setBounceAddresses(String bounceAddresses) throws JspException
	{
		this.bounceAddresses = evaluateString("MailTag", "bounceAddresses", bounceAddresses);
	}

	public void setSubject(String subject) throws JspException
	{
		this.subject = evaluateString("MailTag", "subject", subject);
	}

	public void setType(String type) throws JspException
	{
		this.type = evaluateString("MailTag", "type", type);
	}

	public void setCharset(String charset) throws JspException
	{
		this.charset = evaluateString("MailTag", "charset", charset);
	}

	public void setMessage(String message) throws JspException
	{
		this.message = evaluateString("MailTag", "message", message);
	}
	
	public void setValidationRegexp(String validationRegexp) throws JspException
	{
		this.validationRegexp = evaluateString("MailTag", "validationRegexp", validationRegexp);
	}
}
