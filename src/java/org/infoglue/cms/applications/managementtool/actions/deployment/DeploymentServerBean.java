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

package org.infoglue.cms.applications.managementtool.actions.deployment;

public class DeploymentServerBean 
{
	private String name = null;
	private String url = null;
	private String user = null;
	private String password = null;
	
	public DeploymentServerBean(String name, String url, String user, String password)
	{
		this.name = name;
		this.url = url;
		this.user = user;
		this.password = password;
	}
	
	public String getName() 
	{
		return name;
	}
	
	public String getUrl() 
	{
		return url;
	}
	
	public String getUser() 
	{
		return user;
	}
	
	public String getPassword() 
	{
		return password;
	}

	public void setUser(String user) 
	{
		this.user = user;
	}

	public void setPassword(String password) 
	{
		this.password = password;
	}
}
