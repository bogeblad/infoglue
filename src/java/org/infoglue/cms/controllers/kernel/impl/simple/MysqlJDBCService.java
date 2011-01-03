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

package org.infoglue.cms.controllers.kernel.impl.simple;

/**
 * @author mgu
 *
 * SS 021030 - 	Added support for getting jdbc connection for this builds dbrelease
 * 					and getting connection for corresponding dbrelease on slave server.
 *
 * this one needs to be rewritten.
 */
import java.sql.DriverManager;
import java.sql.SQLException;

import org.infoglue.cms.util.CmsPropertyHandler;

import com.opensymphony.util.TextUtils;

public class MysqlJDBCService
{
   	java.sql.Connection jdbcMasterConnection = null;
   	java.sql.Connection jdbcSlaveConnection = null;

	String dbRelease = CmsPropertyHandler.getDbRelease();
	String dbUser = CmsPropertyHandler.getDbUser();
	String dbPassword = CmsPropertyHandler.getDbPassword();
	String masterServer = CmsPropertyHandler.getMasterServer();
	String slaveServer = CmsPropertyHandler.getSlaveServer();

	private boolean enabled = false;

	public MysqlJDBCService()
	{
		if(TextUtils.stringSet(masterServer) && TextUtils.stringSet(slaveServer))
			enabled = true;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	private java.sql.Connection getConnection() throws Exception
	{

		if (masterServer == null)
			masterServer = "localhost";

		Class driverClass = Class.forName("com.mysql.jdbc.Driver");
      	this.jdbcMasterConnection = DriverManager.getConnection("jdbc:mysql://" + masterServer + "/" + dbRelease, dbUser, dbPassword);
		return jdbcMasterConnection;
	}

	private java.sql.Connection getSlaveConnection() throws Exception
	{
		Class driverClass = Class.forName("com.mysql.jdbc.Driver");
      	this.jdbcSlaveConnection = DriverManager.getConnection("jdbc:mysql://" + slaveServer + "/" + dbRelease, dbUser, dbPassword);
		return jdbcSlaveConnection;
	}

	public java.sql.ResultSet executeSQL(String sql,String numOfRows)throws Exception
	{
		java.sql.Connection conn = getConnection();
		java.sql.Statement stmt = conn.createStatement();
		return stmt.executeQuery(sql + " limit " + numOfRows);
	}


	public java.sql.ResultSet executeMasterSQL(String sql) throws Exception
	{
		java.sql.Connection conn = getConnection();
		java.sql.Statement stmt = conn.createStatement();
		return stmt.executeQuery(sql);
	}
	public java.sql.ResultSet executeSlaveSQL(String sql) throws Exception
	{
		java.sql.Connection conn = getSlaveConnection();
		java.sql.Statement stmt = conn.createStatement();
		return stmt.executeQuery(sql);
	}

	public void closeMaster() throws SQLException
	{
		if (jdbcMasterConnection != null)
			jdbcMasterConnection.close();
	}

	public void closeSlave() throws SQLException
	{
		if (jdbcSlaveConnection != null)
			jdbcSlaveConnection.close();
	}

}
