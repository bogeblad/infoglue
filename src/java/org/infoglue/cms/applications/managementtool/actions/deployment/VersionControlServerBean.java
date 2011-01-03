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

public class VersionControlServerBean 
{
	private String name = null;
	private String host = null;
	private String path = null;
	private String moduleName = null;
	private String user = null;
	private String password = null;
	private String type = null;
	private String port = null;
	
	public VersionControlServerBean(String name, String host, String path, String moduleName, String user, String password, String type, String port)
	{
		this.name = name;
		this.host = host;
		this.path = path;
		this.moduleName = moduleName;
		this.user = user;
		this.password = password;
		this.type = type;
		this.port = port;
	}
	
	public String getCVSROOT()
	{
		//":pserver:mattias@dev1.sprawlsolutions.se:/home/cvsroot";
		return ":" + type + ":" + user + "@" + host + ":" + (port == null ? "" : port) + path;
	}
	
	public String getName() 
	{
		return name;
	}
	
	public String getHost() 
	{
		return host;
	}
	
	public String getPath() 
	{
		return path;
	}

	public String getModuleName() 
	{
		return moduleName;
	}

	public String getUser() 
	{
		return user;
	}
	
	public String getPassword() 
	{
		return password;
	}

	public void setPassword(String password) 
	{
		this.password = password;
	}

	public String getType() 
	{
		return type;
	}
	public String getPort() 
	{
		return port;
	}
}
