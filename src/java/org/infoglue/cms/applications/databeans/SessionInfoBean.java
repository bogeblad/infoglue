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
package org.infoglue.cms.applications.databeans;

import java.util.Date;

import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * This bean is just to contain neccessairy info about the sessions.
 * 
 * @author mattias
 */

public class SessionInfoBean
{
    private String id;
    private InfoGluePrincipal principal;
    private Date lastAccessedDate;
    
    private SessionInfoBean()
    {
    }
    
    public SessionInfoBean(String id)
    {
    	this.id = id;
    }
    
	public Date getLastAccessedDate() 
	{
		return lastAccessedDate;
	}

	public void setLastAccessedDate(Date lastAccessedDate) 
	{
		this.lastAccessedDate = lastAccessedDate;
	}

	public InfoGluePrincipal getPrincipal() 
	{
		return principal;
	}

	public void setPrincipal(InfoGluePrincipal principal) 
	{
		this.principal = principal;
	}

	public String getId()
	{
		return id;
	}
}
