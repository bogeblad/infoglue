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

import javax.crypto.BadPaddingException;
import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;

public class VerifyGapchaTag extends TextRenderTag 
{
	private final static Logger logger = Logger.getLogger(VerifyGapchaTag.class.getName());
	private static final long serialVersionUID = 867587689654L;

	private String ticket;
	private String value;
	private String password;

	private boolean verifyValue(String ticket, String value, String password) throws JspException
	{
		try
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("Verify Gapcha: ticket: " + ticket + ", value: " + value + ", password: " + password);
			}
			String verificationValue = GapchaTag.decodeTicket(ticket, password);
			return verificationValue.equalsIgnoreCase(value);
		}
		catch (Exception ex)
		{
			logger.error("Error when verifying gapcha. Message: " + ex.getMessage());
			logger.warn("Error when verifying gapcha.", ex);
			if (ex instanceof BadPaddingException)
			{
				throw new JspException("Failed to verify Captcha. Are you using the correct password (the one used to encrypt the ticket)?");
			}
			else
			{
				throw new JspException("Failed to verify Captcha");
			}
		}
	}

	public int doEndTag() throws JspException
	{
		setResultAttribute(verifyValue(ticket, value, password));

		return EVAL_PAGE;
	}

	public void setTicket(String ticket) throws JspException
	{
		this.ticket = evaluateString("gapcha", "ticket", ticket);;
	}

	public void setValue(String value) throws JspException
	{
		this.value = evaluateString("gapcha", "value", value);
	}

	public void setPassword(String password) throws JspException
	{
		this.password = evaluateString("gapcha", "password", password);
	}

}
