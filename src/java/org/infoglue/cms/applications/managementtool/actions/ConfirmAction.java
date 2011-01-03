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

package org.infoglue.cms.applications.managementtool.actions;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;

/**
 * @author mgu
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

public class ConfirmAction extends InfoGlueAbstractAction
{
	private String yesDestination;
	private String noDestination;
	private String dest;
	private String header;
	private String message;
	private String extraParameters;
	private String choice;
	private String igSecurityCode;

	public ConfirmAction()
	{
		this.choice = "";	
	}

	public void setYesDestination(String yesDestination)
	{
		this.yesDestination = yesDestination;	
	}
	
	public void setNoDestination(String noDestination)
	{
		this.noDestination = noDestination;	
	}
	
	public String getNoDestination()
	{
		return this.noDestination;	
	}
	
	public String getYesDestination()
	{
		return this.yesDestination;	
	}
	
	public void setMessage(String message)
	{
		this.message = message;	
	}
	
	public String getMessage() throws Exception
	{
		return this.message;	
	}
	
    public String getExtraParameters()
    {
        return extraParameters;
    }

    public void setExtraParameters(String extraParameters)
    {
        this.extraParameters = extraParameters;
    }

	public void setHeader(String header)
	{
		this.header = header;	
	}
	
	public String getHeader()
	{
		return this.header;	
	}
	
	public void setChoice(String action)
	{
		this.choice = action;		
	}
	
	public String getChoice()
	{
		return this.choice;	
	}
	
	public String getDest()
	{
		return this.dest;	
	}
	
	public void setDest(String dest)
	{
		this.dest = dest;	
	}

	public String getIgSecurityCode()
	{
		return igSecurityCode;
	}

	public void setIgSecurityCode(String igSecurityCode)
	{
		this.igSecurityCode = igSecurityCode;
	}

	private void Reroute()
	{
		if(this.choice.equalsIgnoreCase("yes"))
		{
			this.dest = this.yesDestination + (this.yesDestination.indexOf("?") > -1 ? "&" : "?") + "igSecurityCode" + this.igSecurityCode;
		}
		else
		{
			this.dest = this.noDestination;
		}
	}
	
	protected String doExecute() throws Exception 
	{
		if(this.choice.length() > 0)
		{
			Reroute();
			System.out.println("this.dest:" + this.dest);
			return "reroute";
		}						
		
		return SUCCESS;
	}

}
