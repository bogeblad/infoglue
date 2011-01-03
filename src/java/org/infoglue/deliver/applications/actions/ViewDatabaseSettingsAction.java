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

package org.infoglue.deliver.applications.actions;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.DatabaseDefinitionsController;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * This is the action that shows the application state and also can be used to set up surveilence.
 * The idea is to have one command which allways returns a known resultpage if it's ok. Otherwise it prints
 * an error-statement. This action is then called every x minutes by the surveilence and an alarm is raised if something is wrong.
 * We also have a command which can list more status about the application.
 *
 * @author Mattias Bogeblad
 */

public class ViewDatabaseSettingsAction extends InfoGlueAbstractAction 
{
	private String databasesXML = null;
	
	private Map databases = null;
	
	public String doExecute() throws Exception
	{
		String databaseDefinitions = CmsPropertyHandler.getContextRootPath() + File.separator + "WEB-INF" + File.separator + "classes" + File.separator + "databaseDefinitions.xml";
		File file = new File(databaseDefinitions);
		if(file.exists())
			databasesXML = FileHelper.getFileAsString(file);
		
		databases = DatabaseDefinitionsController.getController().getDatabaseDefinitions();
		
		return SUCCESS;
	}

	/*
	<?xml version="1.0" encoding="ISO-8859-1"?>
<databases>
  <database id="default">
    <property name="url" value="jdbc:mysql://localhost/infoglue20?autoReconnect=true&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
    <property name="driverClass" value="com.mysql.jdbc.Driver"/>
    <property name="driverEngine" value="mysql"/>
    <property name="user" value="root"/>
    <property name="password" value="ijikal"/>
    <property name="useUnicode" value="true"/>
    <property name="encoding" value="UTF-8"/>
    <property name="maxConnections" value="100"/>
    <property name="mapping" value="mapping.xml"/>
  </database>
</databases>
	 */
	
	public String doUpdate() throws Exception
	{
		String databaseDefinitions = CmsPropertyHandler.getContextRootPath() + File.separator + "WEB-INF" + File.separator + "classes" + File.separator + "databaseDefinitions.xml";
		
		File file = new File(databaseDefinitions);
		
		FileHelper.writeToFile(file, this.databasesXML, false);
		
		return SUCCESS;
	}

	public String getDatabasesXML()
	{
		return databasesXML;
	}

	public void setDatabasesXML(String databasesXML)
	{
		this.databasesXML = databasesXML;
	}

	public Map getDatabases()
	{
		return databases;
	}

}
