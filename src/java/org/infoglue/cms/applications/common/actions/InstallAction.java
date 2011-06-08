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

package org.infoglue.cms.applications.common.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.InstallationController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.util.CmsPropertyHandler;

import webwork.action.Action;

/** 
 * This class contains methods to handle the installation and or upgrade phase .
 */

public class InstallAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(InstallAction.class.getName());

	private static final long serialVersionUID = 1L;

	private String operation = "";
	
	//All variables connected to the database setup screen
	private String dbProvider = "";
	private String dbName = "";
	private String dbServer = "";
	private String dbPort = "";
	private String dbInstance = "";
	private String dbUser = "";
	private String dbPassword = "";
	private String igUser = "";
	private String igPassword = "";
	private String createDatabase = "";
	private String addExampleRepositories = "";
	
	private String errorMessage = "";
	private String dbVersion = null;
	private String sqlScript = null;
	
	//All variables connected to the server setup screen
	private String appServer = "";
	private String hostName = "";
	private String superUserName = "";
	private String superUserPassword = "";
	private String superUserEmail = "";
	private String operatingMode = "0";
	private String smtpServer = "";
	private String smtpAuth = "";
	private String smtpUser = "";
	private String smtpPassword = "";
	private String systemEmailSender = "";
	
	public String doInput() throws Exception
    {
		return INPUT;
    }

	public String doInputInitiateInstall() throws Exception
    {
		
		return "inputInitiateInstall";
    }

	public String doInputDatabase() throws Exception
    {
		
		return "inputDatabase";
    } 
	
	public String doInputDatabaseExisting() throws Exception
    {
		
		return "inputDatabaseExisting";
    }

	public String doInputDatabaseUpgrade() throws Exception
    {
		
		return "inputDatabaseUpgrade";
    }

	public String doInputServer() throws Exception
    {
		
		return "inputServer";
    }


	public String doExecute() throws Exception
    {
		logger.info("operation:" + operation);
		if(operation.equalsIgnoreCase("updateDatabaseFromExistingConfig"))
		{
			try
			{
				InstallationController.getController().updateDatabaseFromExisting(createDatabase, addExampleRepositories, dbUser, dbPassword, getHttpSession());
			}
			catch (Exception e) 
			{
				e.printStackTrace();
				this.errorMessage = e.getMessage();
				return doInputDatabaseExisting();
			}
		}
		if(operation.equalsIgnoreCase("updateDatabase"))
		{
			try
			{
				InstallationController.getController().updateDatabase(dbProvider, dbName, dbServer, dbPort, dbInstance, createDatabase, addExampleRepositories, dbUser, dbPassword, igUser, igPassword, getHttpSession());
			}
			catch (Exception e) 
			{
				e.printStackTrace();
				this.errorMessage = e.getMessage();
				return doInputDatabase();
			}
		}
		if(operation.equalsIgnoreCase("updateDatabaseTables"))
		{
			try
			{
				InstallationController.getController().updateDatabaseTables(getHttpSession());
			}
			catch (Exception e) 
			{
				e.printStackTrace();
				this.errorMessage = e.getMessage();
				return doInputDatabaseUpgrade();
			}
		}
		else if(operation.equalsIgnoreCase("updateServer"))
		{
			try
			{
				InstallationController.getController().updateServer(appServer, smtpServer, smtpAuth, smtpUser, smtpPassword, systemEmailSender, hostName, superUserName, superUserPassword, superUserEmail, operatingMode, getRequest());
			}
			catch (Exception e) 
			{
				this.errorMessage = e.getMessage();
				return doInputServer();
			}
		}	
		logger.debug("After operations...");
		
		boolean dbConfigExists = false;
		boolean dbConfigOK = false;
		boolean dbUpgradeOK = false;
		boolean serverConfigOK = false;

		try
		{
			int reason = InstallationController.getController().getBrokenDatabaseReason();
			logger.info("reason:" + reason);
			if(reason == InstallationController.DATABASE_SERVER_MISSING_DATABASE || reason == InstallationController.DATABASE_SERVER_MISSING_DATABASE_TABLES)
				dbConfigExists = true;
		}
		catch (Exception e) 
		{
			logger.error("Error checking db configuration:" + e.getMessage());
			//e.printStackTrace();
		}

		try
		{
			InstallationController.getController().validateDatabaseConnection();
			dbConfigOK = true;
		}
		catch (Exception e) 
		{
			logger.error("Error validating database connection:" + e.getMessage());
			//e.printStackTrace();
		}

		try
		{
			this.dbVersion = InstallationController.getController().getCurrentDatabaseVersion(getHttpSession());
			if(this.dbVersion.equalsIgnoreCase(CmsPropertyHandler.getInfoGlueDBVersion()))
				dbUpgradeOK = true;	
			else
			{
				logger.debug("Reported old database schema: " + dbVersion);
				this.sqlScript = InstallationController.getController().getUpgradeScripts(this.dbVersion, getHttpSession());
			}
		}
		catch (ClassNotFoundException e) 
		{
			dbConfigOK = false;
		}
		catch (Exception e) 
		{
			logger.error("Error getting current database version:" + e.getMessage());
			//e.printStackTrace();
		}

		try
		{
			serverConfigOK = InstallationController.getController().validateApplicationFile();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		logger.debug("dbConfigExisting:" + dbConfigExists);
		logger.debug("dbConfigOK:" + dbConfigOK);
		logger.debug("dbUpgradeOK:" + dbUpgradeOK);
		logger.debug("serverConfigOK:" + serverConfigOK);

		
		if(dbConfigExists)
			return doInputDatabaseExisting();
		if(!dbConfigOK)
			return doInputDatabase();
		if(!dbUpgradeOK)
			return doInputDatabaseUpgrade();
		if(!serverConfigOK)
			return doInputServer();

		boolean isValidSetup = InstallationController.getController().validateSetup();
		CmsPropertyHandler.setIsValidSetup(isValidSetup);

		getHttpSession().removeAttribute("install_dbProvider");
		getHttpSession().removeAttribute("install_jdbcDriverName");
		getHttpSession().removeAttribute("install_jdbcURL");
		getHttpSession().removeAttribute("install_dbUser");
		getHttpSession().removeAttribute("install_dbPassword");

		return Action.SUCCESS;
    }

	
	public String getDbVersion() 
	{
		return dbVersion;
	}

	public String getSqlScript() 
	{
		return sqlScript;
	}

	

	public String getAddExampleRepositories() 
	{
		return addExampleRepositories;
	}

	public void setAddExampleRepositories(String addExampleRepositories) 
	{
		this.addExampleRepositories = addExampleRepositories;
	}

	public String getDbProvider() 
	{
		return dbProvider;
	}

	public void setDbProvider(String dbProvider) 
	{
		this.dbProvider = dbProvider;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDbServer() {
		return dbServer;
	}

	public void setDbServer(String dbServer) {
		this.dbServer = dbServer;
	}

	public String getDbPort() {
		return dbPort;
	}

	public void setDbPort(String dbPort) {
		this.dbPort = dbPort;
	}

	public String getDbInstance() {
		return dbInstance;
	}

	public void setDbInstance(String dbInstance) {
		this.dbInstance = dbInstance;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getIgUser() {
		return igUser;
	}

	public void setIgUser(String igUser) {
		this.igUser = igUser;
	}

	public String getIgPassword() {
		return igPassword;
	}

	public void setIgPassword(String igPassword) {
		this.igPassword = igPassword;
	}

	public void setDbVersion(String dbVersion) {
		this.dbVersion = dbVersion;
	}

	public void setSqlScript(String sqlScript) {
		this.sqlScript = sqlScript;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getCreateDatabase() {
		return createDatabase;
	}

	public void setCreateDatabase(String createDatabase) {
		this.createDatabase = createDatabase;
	}

	public String getAppServer() {
		return appServer;
	}

	public void setAppServer(String appServer) {
		this.appServer = appServer;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getSuperUserName() {
		return superUserName;
	}

	public void setSuperUserName(String superUserName) {
		this.superUserName = superUserName;
	}

	public String getSuperUserPassword() {
		return superUserPassword;
	}

	public void setSuperUserPassword(String superUserPassword) {
		this.superUserPassword = superUserPassword;
	}

	public String getSuperUserEmail() {
		return superUserEmail;
	}

	public void setSuperUserEmail(String superUserEmail) {
		this.superUserEmail = superUserEmail;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public String getSmtpAuth() {
		return smtpAuth;
	}

	public void setSmtpAuth(String smtpAuth) {
		this.smtpAuth = smtpAuth;
	}

	public String getSmtpUser() {
		return smtpUser;
	}

	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}

	public String getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	public String getSystemEmailSender() {
		return systemEmailSender;
	}

	public void setSystemEmailSender(String systemEmailSender) {
		this.systemEmailSender = systemEmailSender;
	}
	
	public void setOperatingMode(String operatingMode)
	{
		this.operatingMode = operatingMode;
	}

	public String getOperatingMode()
	{
		return this.operatingMode;
	}
	
	public String getApplicationName()
	{
		return CmsPropertyHandler.getApplicationName();
	}
	
	public String getCurrentJDBCUrl() throws Exception
	{
		return InstallationController.getController().getJDBCParamFromCastorXML("//param[@name='url']");
	}

	public String getJDBCEngine() throws Exception
	{
		return InstallationController.getController().getJDBCEngine();
	}

	public String getScript() throws Exception
	{
		return InstallationController.getController().getScript();
	}
}