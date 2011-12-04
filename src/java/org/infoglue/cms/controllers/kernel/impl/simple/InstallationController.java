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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.ConnectException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import oracle.sql.BLOB;
import oracle.sql.CLOB;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.mapping.Mapping;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.infoglue.cms.util.workflow.InfoGlueJDBCPropertySet;
import org.infoglue.deliver.util.CacheController;

/**
 * This class is a helper class for all installation-related actions
 *
 * @author Mattias Bogeblad
 */

public class InstallationController extends BaseController
{
    private final static Logger logger = Logger.getLogger(InstallationController.class.getName());

	/**
	 * Factory method
	 */

	public static InstallationController getController()
	{
		return new InstallationController();
	}

	public void validateDatabaseConnection() throws Exception
	{
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			LanguageController.getController().getLanguageList(db);
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("--------------------------------------------------");
			logger.info("Error:" + e.getMessage());
			logger.info("--------------------------------------------------");
			//e.printStackTrace();
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}
	
	public String getCurrentDatabaseVersion(HttpSession session) throws Exception
	{
		//if(true)
		//	return "2.0";
		String dbProvider = null;
		String jdbcDriverName = null;
		String jdbcURL = null;
		String igUser = null;
		String igPassword = null;

		if(session != null)
		{
			dbProvider = (String)session.getAttribute("install_dbProvider");
			jdbcDriverName = (String)session.getAttribute("install_jdbcDriverName");
			jdbcURL = (String)session.getAttribute("install_jdbcURL");
			igUser = (String)session.getAttribute("install_dbUser");
			igPassword = (String)session.getAttribute("install_dbPassword");
		}
		
		if(dbProvider == null)
		{
			dbProvider = getJDBCEngine();
			if(session != null) session.setAttribute("install_dbProvider", dbProvider);
		}
		
		if(jdbcDriverName == null)
		{
			jdbcDriverName = getJDBCParamFromCastorXML("//param[@name='driver-class-name']");
			if(session != null) session.setAttribute("install_jdbcDriverName", jdbcDriverName);
		}

		if(jdbcURL == null)
		{
			jdbcURL = getJDBCParamFromCastorXML("//param[@name='url']");
			if(session != null) session.setAttribute("install_jdbcURL", jdbcURL);
		}

		if(igUser == null)
		{
			igUser = getJDBCParamFromCastorXML("//param[@name='username']");
			if(session != null) session.setAttribute("install_dbUser", igUser);
		}

		if(igPassword == null)
		{
			igPassword = getJDBCParamFromCastorXML("//param[@name='password']");
			if(session != null) session.setAttribute("install_dbPassword", igPassword);
		}
		
		Connection conn = getConnection(jdbcDriverName, jdbcURL, igUser, igPassword);
		
		String previousVersion = "3.0";

	    try
	    {
	        String sql = "SELECT * FROM cmInfoGlueProperties where name=?";
	        
	        PreparedStatement pstmt = conn.prepareStatement(sql);
	        pstmt.setString(1, "version");
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			
			String versionValue = rs.getString("value");
			logger.info("versionValue:" + versionValue);
			if(versionValue.equals("3.0"))
				return "3.0";
	    }
	    catch(Exception e)
	    {
	        logger.error("Error in table lookup:" + e.getMessage());
	    }

	    try
	    {
	        String sql = "SELECT * FROM cmSiteNodeVersion";
	        if(dbProvider.equalsIgnoreCase("oracle") || dbProvider.equalsIgnoreCase("db2"))
	            sql = "SELECT * FROM cmSiNoVer";
	        
	        PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			
			logger.info("ColCount:" + rs.getMetaData().getColumnCount());

			for(int i=1; i<=rs.getMetaData().getColumnCount(); i++)
			{
				String columnName = rs.getMetaData().getColumnName(i);
				logger.info("columnName:" + columnName);
				if(columnName.equalsIgnoreCase("isHidden"))
					return "3.0";
			}
			
			rs.getString("isHidden"); //If this throws exception then it's older than 2.3
			return "3.0";
	    }
	    catch(Exception e)
	    {
	        logger.error("Error in table lookup:" + e.getMessage());
		    previousVersion = "2.9.8.7";
	    }

	    try
	    {
	        String sql = "SELECT * FROM cmSiteNodeVersion";
	        if(dbProvider.equalsIgnoreCase("oracle") || dbProvider.equalsIgnoreCase("db2"))
	            sql = "SELECT * FROM cmSiNoVer";
	        
	        PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			
			
			for(int i=1; i<=rs.getMetaData().getColumnCount(); i++)
			{
				String columnName = rs.getMetaData().getColumnName(i);
				logger.info("columnName:" + columnName);
				if(columnName.equalsIgnoreCase("forceProtocolChange"))
					return "2.9.8.7";
			}
			rs.getString("forceProtocolChange"); //If this throws exception then it's older than 2.3
			return "2.9.8.7";
	    }
	    catch(Exception e)
	    {
	        logger.error("Error in table lookup:" + e.getMessage());
		    previousVersion = "2.9.7.1";
	    }
	    
	    try
	    {
	        String sql = "SELECT * FROM cmFormEntryAsset";
	        
	        PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			
			if(rs.getMetaData().getColumnCount() > 0) //If this is above 0 then the table is there and the version is 1.2.
			{
				return "2.9.7.1";
			}
	    }
	    catch(Exception e)
	    {
	        logger.error("Error in table lookup:" + e.getMessage());
		    previousVersion = "2.9";
	    }

	    try
	    {
	        String sql = "SELECT * FROM cmSubscription";
	        
	        PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			
			if(rs.getMetaData().getColumnCount() > 0) //If this is above 0 then the table is there and the version is 1.2.
			{
				return "2.9";
			}
	    }
	    catch(Exception e)
	    {
	        logger.error("Error in table lookup:" + e.getMessage());
		    previousVersion = "2.8";
	    }

	    try
	    {
	        String sql = "SELECT * FROM cmSiteNodeVersion";
	        if(dbProvider.equalsIgnoreCase("oracle") || dbProvider.equalsIgnoreCase("db2"))
	            sql = "SELECT * FROM cmSiNoVer";
	        
	        PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			
			rs.getString("pageCacheTimeout"); //If this throws exception then it's older than 2.3
			return "2.8";
	    }
	    catch(Exception e)
	    {
	        logger.error("Error in table lookup:" + e.getMessage());
		    previousVersion = "2.3";
	    }

	    try
	    {
	        String sql = "SELECT * FROM cmSiteNodeVersion";
	        if(dbProvider.equalsIgnoreCase("oracle") || dbProvider.equalsIgnoreCase("db2"))
	            sql = "SELECT * FROM cmSiNoVer";
	        
	        PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			
			rs.getString("disableLanguages"); //If this throws exception then it's older than 2.3
			return "2.3";
	    }
	    catch(Exception e)
	    {
	        logger.error("Error in table lookup:" + e.getMessage());
		    previousVersion = "2.1";
	    }

	    try
	    {
	        String sql = "SELECT * FROM cmSiteNodeVersion";
	        if(dbProvider.equalsIgnoreCase("oracle") || dbProvider.equalsIgnoreCase("db2"))
	            sql = "SELECT * FROM cmSiNoVer";
	        
	        PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			
			rs.getString("pageCacheKey"); //If this throws exception then it's older than 2.1
			return "2.1";
	    }
	    catch(Exception e)
	    {
	        logger.error("Error in table lookup:" + e.getMessage());
		    previousVersion = "2.0";
	    }
	    
	    return previousVersion;
	}

	public String getUpgradeScripts(String dbVersion, HttpSession session) throws Exception
	{
		String dbProvider = (String)session.getAttribute("install_dbProvider");

		if(dbProvider == null)
		{
			dbProvider = getJDBCEngine();
			session.setAttribute("install_dbProvider", dbProvider);
		}

		String completeSQL = "";
		
		if(dbVersion.equalsIgnoreCase("2.0"))
		{
			completeSQL += getScript(dbProvider, "2.1");
			completeSQL += getScript(dbProvider, "2.3");
			completeSQL += getScript(dbProvider, "2.8");
			completeSQL += getScript(dbProvider, "2.9");
			completeSQL += getScript(dbProvider, "2.9.7.1");
			completeSQL += getScript(dbProvider, "2.9.8.7");
			completeSQL += getScript(dbProvider, "3.0");
		}
		if(dbVersion.equalsIgnoreCase("2.1"))
		{
			completeSQL += getScript(dbProvider, "2.3");
			completeSQL += getScript(dbProvider, "2.8");
			completeSQL += getScript(dbProvider, "2.9");
			completeSQL += getScript(dbProvider, "2.9.7.1");
			completeSQL += getScript(dbProvider, "2.9.8.7");
			completeSQL += getScript(dbProvider, "3.0");
		}
		if(dbVersion.equalsIgnoreCase("2.3"))
		{
			completeSQL += getScript(dbProvider, "2.8");
			completeSQL += getScript(dbProvider, "2.9");
			completeSQL += getScript(dbProvider, "2.9.7.1");
			completeSQL += getScript(dbProvider, "2.9.8.7");
			completeSQL += getScript(dbProvider, "3.0");
		}
		if(dbVersion.equalsIgnoreCase("2.8"))
		{
			completeSQL += getScript(dbProvider, "2.9");
			completeSQL += getScript(dbProvider, "2.9.7.1");
			completeSQL += getScript(dbProvider, "2.9.8.7");
			completeSQL += getScript(dbProvider, "3.0");
		}
		if(dbVersion.equalsIgnoreCase("2.9"))
		{
			completeSQL += getScript(dbProvider, "2.9.7.1");
			completeSQL += getScript(dbProvider, "2.9.8.7");
			completeSQL += getScript(dbProvider, "3.0");
		}
		if(dbVersion.equalsIgnoreCase("2.9.7.1"))
		{
			completeSQL += getScript(dbProvider, "2.9.8.7");
			completeSQL += getScript(dbProvider, "3.0");
		}
		if(dbVersion.equalsIgnoreCase("2.9.8.7"))
		{
			completeSQL += getScript(dbProvider, "3.0");
		}
		
		return completeSQL;
	}
	
	public String getScript() throws Exception
	{
		String dbProvider = getJDBCEngine();
		
		String coreScript = FileHelper.getFileAsString(new File(CmsPropertyHandler.getSQLUpgradePath() + File.separator + "infoglue_core_schema_" + dbProvider + ".sql"));
		String initialDataScript = FileHelper.getFileAsString(new File(CmsPropertyHandler.getSQLUpgradePath() + File.separator + "infoglue_initial_data_" + dbProvider + ".sql"));
		
		String script = coreScript + "\n" + initialDataScript;
		script = script.replaceAll("#endquery", "");
		VisualFormatter formatter = new VisualFormatter();
		script = formatter.escapeHTML(script);
		
		return script;
	}

	public String getScript(String dbProvider, String version) throws Exception
	{
		String script = FileHelper.getFileAsString(new File(CmsPropertyHandler.getSQLUpgradePath() + File.separator + "upgrade_" + version + "_" + dbProvider + ".sql"));
		return script;
	}

	public BaseEntityVO getNewVO() 
	{
		return null;
	}

	public boolean validateApplicationFile() throws Exception
	{
		String cmsFilePath = CastorDatabaseService.class.getResource("/" + CmsPropertyHandler.getApplicationName() + ".properties").getPath();
		//logger.info("cmsFilePath:" + cmsFilePath);
		String contents = FileHelper.getFileAsString(new File(cmsFilePath));
		//logger.info("contents:" + contents.substring(0, 200));
		
	    if(contents.indexOf("configured=true") > -1 || contents.indexOf("databaseEngine=@database.driver.engine@") == -1)
	    	return true;

	    return false;
	}

	public boolean validateInitialData(boolean addExampleRepositories) throws Exception
	{
		boolean isDatabasePopulated = true;
		
		if(addExampleRepositories)
		{
			List repos = RepositoryController.getController().getRepositoryVOList();
			if(repos.size() == 0)
				isDatabasePopulated = false;
		}
		
		List contentTypes = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
		if(contentTypes.size() == 0)
			isDatabasePopulated = false;
		
		return isDatabasePopulated;
	}
	
	
	public void updateServer(String appServer, String smtpServer, String smtpAuth, String smtpUser, String smtpPassword, String systemEmailSender, String hostName, String superUserName, String superUserPassword, String superUserEmail, String operatingMode, HttpServletRequest request) throws Exception
	{
		if(appServer.equals("") || 
		   hostName.equals("") || 
		   superUserName.equals("") || 
		   superUserPassword.equals("") || 
		   superUserEmail.equals("") || 
		   operatingMode.equals(""))
		{
			throw new Exception("Mandatory field(s) missing");
		}

		String dbProvider = (String)request.getSession().getAttribute("install_dbProvider");
		//String jdbcDriverName = (String)request.getSession().getAttribute("install_jdbcDriverName");
		//String jdbcURL = (String)request.getSession().getAttribute("install_jdbcURL");
		//String igUser = (String)request.getSession().getAttribute("install_dbUser");
		//String igPassword = (String)request.getSession().getAttribute("install_dbPassword");

		if(dbProvider == null)
		{
			dbProvider = getJDBCEngine();
			request.getSession().setAttribute("install_dbProvider", dbProvider);
		}
		
		//String jdbcEngine = getJDBCEngine();
		
		//cms.properties
		String cmsFilePath = CastorDatabaseService.class.getResource("/cms.properties").getPath();
		if(logger.isInfoEnabled())
			logger.info("cmsFilePath:" + cmsFilePath);
		String contents = FileHelper.getStreamAsString(CastorDatabaseService.class.getResourceAsStream("/cms.properties"));
		if(logger.isInfoEnabled())
			logger.info("contents:" + contents);
		
		contents = contents.replaceAll("@configured@", "true");
		contents = contents.replaceAll("@useUpdateSecurity@", "true");
		contents = contents.replaceAll("@externalWebServerAddress@", "");
		contents = contents.replaceAll("@webServerAddress@", "http://" + hostName);
		contents = contents.replaceAll("@digitalAssetPath@", "");
		contents = contents.replaceAll("@URIEncoding@", "UTF-8");
		if(CmsPropertyHandler.getApplicationName().equalsIgnoreCase("cms"))
		{
			contents = contents.replaceAll("@context.root.cms@", request.getContextPath().replaceAll("/", ""));
			contents = contents.replaceAll("@context.root.working@", "infoglueDeliverWorking");
			contents = contents.replaceAll("@context.root.preview@", "infoglueDeliverPreview");
			contents = contents.replaceAll("@context.root.live@", "infoglueDeliverLive");			
		}
		else
		{
			if(CmsPropertyHandler.getOperatingMode().equalsIgnoreCase("0"))
			{
				contents = contents.replaceAll("@context.root.cms@", "infoglueCMS");
				contents = contents.replaceAll("@context.root.working@", request.getContextPath().replaceAll("/", ""));
				contents = contents.replaceAll("@context.root.preview@", "infoglueDeliverPreview");
				contents = contents.replaceAll("@context.root.live@", "infoglueDeliverLive");			
			}
			else if(CmsPropertyHandler.getOperatingMode().equalsIgnoreCase("2"))
			{
				contents = contents.replaceAll("@context.root.cms@", "infoglueCMS");
				contents = contents.replaceAll("@context.root.working@", "infoglueDeliverWorking");
				contents = contents.replaceAll("@context.root.preview@", request.getContextPath().replaceAll("/", ""));
				contents = contents.replaceAll("@context.root.live@", "infoglueDeliverLive");			
			}
			else if(CmsPropertyHandler.getOperatingMode().equalsIgnoreCase("3"))
			{
				contents = contents.replaceAll("@context.root.cms@", "infoglueCMS");
				contents = contents.replaceAll("@context.root.working@", "infoglueDeliverWorking");
				contents = contents.replaceAll("@context.root.preview@", "infoglueDeliverPreview");
				contents = contents.replaceAll("@context.root.live@", request.getContextPath().replaceAll("/", ""));			
			}
			else
			{
				contents = contents.replaceAll("@context.root.cms@", "infoglueCMS");
				contents = contents.replaceAll("@context.root.working@", request.getContextPath().replaceAll("/", ""));
				contents = contents.replaceAll("@context.root.preview@", "infoglueDeliverPreview");
				contents = contents.replaceAll("@context.root.live@", "infoglueDeliverLive");			
			}
		}

		String applicationServerRoot = CmsPropertyHandler.getContextRootPath();
		applicationServerRoot = applicationServerRoot.substring(0, applicationServerRoot.lastIndexOf("/"));
		applicationServerRoot = applicationServerRoot.substring(0, applicationServerRoot.lastIndexOf("/") + 1);

		contents = contents.replaceAll("@useShortTableNames@", ((dbProvider.equalsIgnoreCase("oracle") || dbProvider.equalsIgnoreCase("db2")) ? "true" : "false"));
		contents = contents.replaceAll("@database.driver.engine@", dbProvider);
		contents = contents.replaceAll("@warningEmailReceiver@", superUserEmail);
		contents = contents.replaceAll("@operatingMode.cms@", "0");
		contents = contents.replaceAll("@administratorUserName@", superUserName);
		contents = contents.replaceAll("@administratorPassword@", superUserPassword);
		contents = contents.replaceAll("@administratorEmail@", superUserEmail);		

		contents = contents.replaceAll("@loginUrl@", "Login.action");	
		contents = contents.replaceAll("@logoutUrl@", "");	
		contents = contents.replaceAll("@invalidLoginUrl@", "Login!invalidLogin.action");	
		contents = contents.replaceAll("@authenticatorClass@", "org.infoglue.cms.security.InfoGlueBasicAuthenticationModule");	
		contents = contents.replaceAll("@authorizerClass@", "org.infoglue.cms.security.InfoGlueBasicAuthorizationModule");	
		contents = contents.replaceAll("@serverName@", "" + hostName);
		contents = contents.replaceAll("@authConstraint@", "cmsUser");
		
		contents = contents.replaceAll("@databaseEngine@", dbProvider);
		contents = contents.replaceAll("@logTransactions@", "false");
		contents = contents.replaceAll("@errorUrl@", "/error.jsp");
		contents = contents.replaceAll("@tree@", "html");
		contents = contents.replaceAll("@showContentVersionFirst@", "true");
		contents = contents.replaceAll("@showComponentsFirst@", "true");
		contents = contents.replaceAll("@protectContentTypes@", "false");
		contents = contents.replaceAll("@protectWorkflows@", "false");
		contents = contents.replaceAll("@protectCategories@", "false");
		contents = contents.replaceAll("@wysiwygEditor@", "FCKEditor");
		contents = contents.replaceAll("@edition.pageSize@", "20");
		contents = contents.replaceAll("@masterServer@", "");
		contents = contents.replaceAll("@slaveServer@", "");
		contents = contents.replaceAll("@up2dateUrl@", "http://www.infoglue.org/ViewPage.action?siteNodeId=23");
				
		contents = contents.replaceAll("@portletBase@", applicationServerRoot);
		contents = contents.replaceAll("@mail.smtp.host@", smtpServer);
		contents = contents.replaceAll("@mail.smtp.auth@", smtpAuth);
		contents = contents.replaceAll("@mail.smtp.user@", smtpUser);
		contents = contents.replaceAll("@mail.smtp.password@", smtpPassword);
		contents = contents.replaceAll("@mail.contentType@", "text/html");
		contents = contents.replaceAll("@systemEmailSender@", systemEmailSender);
		contents = contents.replaceAll("@niceURIEncoding@", "utf-8");
		contents = contents.replaceAll("@logDatabaseMessages@", "false");
		contents = contents.replaceAll("@enablePortal@", "true");
		
		if(logger.isInfoEnabled())
			logger.info("contents after:" + contents);
		if(logger.isInfoEnabled())
			logger.info("Want to write to:" + cmsFilePath);
		File targetFile = new File(cmsFilePath);
		if(logger.isInfoEnabled())
			logger.info("targetFile:" + targetFile.exists());
		
		FileHelper.writeToFile(targetFile, contents, false);
		
		CacheController.clearCache("serverNodePropertiesCache");
		CmsPropertyHandler.initializeProperties();
		CmsPropertyHandler.resetHardCachedSettings();
		InfoGlueAuthenticationFilter.initializeCMSProperties();
		//END cms.properties

		//deliver.properties
		String deliverFilePath = CastorDatabaseService.class.getResource("/deliver.properties").getPath();
		if(logger.isInfoEnabled())
			logger.info("deliverFilePath:" + deliverFilePath);
		String contentsDeliver = FileHelper.getStreamAsString(CastorDatabaseService.class.getResourceAsStream("/deliver.properties"));
		if(logger.isInfoEnabled())
			logger.info("contentsDeliver:" + contentsDeliver);
		
		contentsDeliver = contentsDeliver.replaceAll("@configured@", "true");
		contentsDeliver = contentsDeliver.replaceAll("@useSelectivePageCacheUpdate@", "true");
		contentsDeliver = contentsDeliver.replaceAll("@livePublicationThreadClass@", "org.infoglue.deliver.util.SelectiveLivePublicationThread");
		contentsDeliver = contentsDeliver.replaceAll("@compressPageCache@", "true");
		contentsDeliver = contentsDeliver.replaceAll("@siteNodesToRecacheOnPublishing@", "");
		contentsDeliver = contentsDeliver.replaceAll("@pathsToRecacheOnPublishing@", "/");
		contentsDeliver = contentsDeliver.replaceAll("@recachePublishingMethod@", "");
		contentsDeliver = contentsDeliver.replaceAll("@recacheUrl@", "");
		contentsDeliver = contentsDeliver.replaceAll("@useUpdateSecurity@", "true");
		contentsDeliver = contentsDeliver.replaceAll("@forceImportTagFileCaching@", "false");
		contentsDeliver = contentsDeliver.replaceAll("@operatingMode.deliver@", operatingMode);

		if(CmsPropertyHandler.getApplicationName().equalsIgnoreCase("cms"))
		{
			contentsDeliver = contentsDeliver.replaceAll("@context.root.cms@", request.getContextPath().replaceAll("/", ""));
			contentsDeliver = contentsDeliver.replaceAll("@context.root.working@", "infoglueDeliverWorking");
			contentsDeliver = contentsDeliver.replaceAll("@context.root.preview@", "infoglueDeliverPreview");
			contentsDeliver = contentsDeliver.replaceAll("@context.root.live@", "infoglueDeliverLive");			
		}
		else
		{
			if(CmsPropertyHandler.getOperatingMode().equalsIgnoreCase("0"))
			{
				contentsDeliver = contentsDeliver.replaceAll("@context.root.cms@", "infoglueCMS");
				contentsDeliver = contentsDeliver.replaceAll("@context.root.working@", request.getContextPath().replaceAll("/", ""));
				contentsDeliver = contentsDeliver.replaceAll("@context.root.preview@", "infoglueDeliverPreview");
				contentsDeliver = contentsDeliver.replaceAll("@context.root.live@", "infoglueDeliverLive");			
			}
			else if(CmsPropertyHandler.getOperatingMode().equalsIgnoreCase("2"))
			{
				contentsDeliver = contentsDeliver.replaceAll("@context.root.cms@", "infoglueCMS");
				contentsDeliver = contentsDeliver.replaceAll("@context.root.working@", "infoglueDeliverWorking");
				contentsDeliver = contentsDeliver.replaceAll("@context.root.preview@", request.getContextPath().replaceAll("/", ""));
				contentsDeliver = contentsDeliver.replaceAll("@context.root.live@", "infoglueDeliverLive");			
			}
			else if(CmsPropertyHandler.getOperatingMode().equalsIgnoreCase("3"))
			{
				contentsDeliver = contentsDeliver.replaceAll("@context.root.cms@", "infoglueCMS");
				contentsDeliver = contentsDeliver.replaceAll("@context.root.working@", "infoglueDeliverWorking");
				contentsDeliver = contentsDeliver.replaceAll("@context.root.preview@", "infoglueDeliverPreview");
				contentsDeliver = contentsDeliver.replaceAll("@context.root.live@", request.getContextPath().replaceAll("/", ""));			
			}
			else
			{
				contentsDeliver = contentsDeliver.replaceAll("@context.root.cms@", "infoglueCMS");
				contentsDeliver = contentsDeliver.replaceAll("@context.root.working@", request.getContextPath().replaceAll("/", ""));
				contentsDeliver = contentsDeliver.replaceAll("@context.root.preview@", "infoglueDeliverPreview");
				contentsDeliver = contentsDeliver.replaceAll("@context.root.live@", "infoglueDeliverLive");			
			}
		}

		contentsDeliver = contentsDeliver.replaceAll("@URIEncoding@", "UTF-8");
		contentsDeliver = contentsDeliver.replaceAll("@externalWebServerAddress@", "");
		contentsDeliver = contentsDeliver.replaceAll("@webServerAddress@", "http://" + hostName);
		contentsDeliver = contentsDeliver.replaceAll("@digitalAssetPath@", "");
		
		contentsDeliver = contentsDeliver.replaceAll("@useShortTableNames@", ((dbProvider.equalsIgnoreCase("oracle") || dbProvider.equalsIgnoreCase("db2")) ? "true" : "false"));
		contentsDeliver = contentsDeliver.replaceAll("@database.driver.engine@", dbProvider);
		contentsDeliver = contentsDeliver.replaceAll("@warningEmailReceiver@", superUserEmail);
		contentsDeliver = contentsDeliver.replaceAll("@operatingMode.cms@", "0");
		contentsDeliver = contentsDeliver.replaceAll("@administratorUserName@", superUserName);
		contentsDeliver = contentsDeliver.replaceAll("@administratorPassword@", superUserPassword);
		contentsDeliver = contentsDeliver.replaceAll("@administratorEmail@", superUserEmail);	
		
		contentsDeliver = contentsDeliver.replaceAll("@loginUrl@", "Login.action");	
		contentsDeliver = contentsDeliver.replaceAll("@logoutUrl@", "");	
		contentsDeliver = contentsDeliver.replaceAll("@invalidLoginUrl@", "Login!invalidLogin.action");	
		contentsDeliver = contentsDeliver.replaceAll("@authenticatorClass@", "org.infoglue.cms.security.InfoGlueBasicAuthenticationModule");	
		contentsDeliver = contentsDeliver.replaceAll("@authorizerClass@", "org.infoglue.cms.security.InfoGlueBasicAuthorizationModule");	
		contentsDeliver = contentsDeliver.replaceAll("@serverName@", "" + hostName);
		contentsDeliver = contentsDeliver.replaceAll("@authConstraint@", "cmsUser");

		contentsDeliver = contentsDeliver.replaceAll("@databaseEngine@", dbProvider);
		contentsDeliver = contentsDeliver.replaceAll("@logTransactions@", "false");
		contentsDeliver = contentsDeliver.replaceAll("@errorUrl@", "/error.jsp");
		contentsDeliver = contentsDeliver.replaceAll("@tree@", "html");
		contentsDeliver = contentsDeliver.replaceAll("@showContentVersionFirst@", "true");
		contentsDeliver = contentsDeliver.replaceAll("@showComponentsFirst@", "true");
		contentsDeliver = contentsDeliver.replaceAll("@protectContentTypes@", "false");
		contentsDeliver = contentsDeliver.replaceAll("@protectWorkflows@", "false");
		contentsDeliver = contentsDeliver.replaceAll("@protectCategories@", "false");
		contentsDeliver = contentsDeliver.replaceAll("@wysiwygEditor@", "FCKEditor");
		contentsDeliver = contentsDeliver.replaceAll("@edition.pageSize@", "20");
		contentsDeliver = contentsDeliver.replaceAll("@masterServer@", "");
		contentsDeliver = contentsDeliver.replaceAll("@slaveServer@", "");
		contentsDeliver = contentsDeliver.replaceAll("@up2dateUrl@", "http://www.infoglue.org/ViewPage.action?siteNodeId=23");
		contentsDeliver = contentsDeliver.replaceAll("@portletBase@", applicationServerRoot);
		contentsDeliver = contentsDeliver.replaceAll("@mail.smtp.host@", smtpServer);
		contentsDeliver = contentsDeliver.replaceAll("@mail.smtp.auth@", smtpAuth);
		contentsDeliver = contentsDeliver.replaceAll("@mail.smtp.user@", smtpUser);
		contentsDeliver = contentsDeliver.replaceAll("@mail.smtp.password@", smtpPassword);
		contentsDeliver = contentsDeliver.replaceAll("@mail.contentType@", "text/html");
		contentsDeliver = contentsDeliver.replaceAll("@systemEmailSender@", systemEmailSender);
		contentsDeliver = contentsDeliver.replaceAll("@niceURIEncoding@", "utf-8");
		contentsDeliver = contentsDeliver.replaceAll("@logDatabaseMessages@", "false");
		contentsDeliver = contentsDeliver.replaceAll("@enablePortal@", "true");

		if(logger.isInfoEnabled())
			logger.info("contentsDeliver after:" + contentsDeliver);
		if(logger.isInfoEnabled())
			logger.info("Want to write to:" + deliverFilePath);
		File targetFileDeliver = new File(deliverFilePath);
		if(logger.isInfoEnabled())
			logger.info("targetFileDeliver:" + targetFileDeliver.exists());
		
		FileHelper.writeToFile(targetFileDeliver, contentsDeliver, false);
		
		CacheController.clearCache("serverNodePropertiesCache");
		CmsPropertyHandler.initializeProperties();
		CmsPropertyHandler.resetHardCachedSettings();
		InfoGlueAuthenticationFilter.initializeProperties();
		if(logger.isInfoEnabled())
			logger.info("Operatingmode:" + CmsPropertyHandler.getOperatingMode());
		if(logger.isInfoEnabled())
			logger.info("adminEmail:" + CmsPropertyHandler.getAdministratorEmail());
		//END deliver.properties

	}

	public String getJDBCEngine() throws Exception
	{
		String contents = FileHelper.getStreamAsString(CastorDatabaseService.class.getResourceAsStream("/database.xml"));
		if(logger.isInfoEnabled())
			logger.info("contents:" + contents);
		if(contents.indexOf("engine=\"mysql\"") > -1)
			return "mysql";
		if(contents.indexOf("engine=\"oracle\"") > -1)
			return "oracle";
		if(contents.indexOf("engine=\"db2\"") > -1)
			return "db2";
		if(contents.indexOf("engine=\"sql-server\"") > -1)
			return "sql-server";
		
		return "";
	}

	public String getJDBCParamFromCastorXML(String xpath) throws Exception
	{
		String contents = FileHelper.getStreamAsString(CastorDatabaseService.class.getResourceAsStream("/database.xml"));
		if(logger.isInfoEnabled())
			logger.info("contents:" + contents);
		DOMBuilder domBuilder = new DOMBuilder();
		Document doc = domBuilder.getDocument(contents, false);
		
		Element element = (Element)doc.selectSingleNode(xpath);
		if(element != null)
			return element.attributeValue("value");
			
		return "";
	}

	public void updateDatabaseFromExisting(String createDatabase, String addExampleRepositories, String dbUser, String dbPassword, HttpSession session) throws Exception 
	{
		int reason = getBrokenDatabaseReason();
		logger.info("reason:" + reason);
		
		String dbProvider = getJDBCEngine();
		
		if(dbProvider.equals("") || 
		   (createDatabase.equalsIgnoreCase("true") && (
			dbUser.equals("") || 
		    dbPassword.equals(""))))
		{
			throw new Exception("Mandatory field(s) missing");
		}

		String jdbcDriverName = getJDBCParamFromCastorXML("//param[@name='driver-class-name']");
		String jdbcURL = getJDBCParamFromCastorXML("//param[@name='url']");
		String igUser = getJDBCParamFromCastorXML("//param[@name='username']");
		String igPassword = getJDBCParamFromCastorXML("//param[@name='password']");

		String dbName = getDBName(jdbcURL, dbProvider);
		String dbServer = getDBServer(jdbcURL, dbProvider);
		String dbPort = getDBPort(jdbcURL, dbProvider);
		String dbInstance = getDBInstance(jdbcURL, dbProvider);
		logger.info("dbName:" + dbName);
		logger.info("dbServer:" + dbServer);
		logger.info("dbPort:" + dbPort);
		logger.info("dbInstance:" + dbInstance);
		//String databaseMappingFile = getDatabaseMappingFile(dbProvider);
		
		if(!createDatabase.equalsIgnoreCase("true"))
			validateConnection(jdbcDriverName, jdbcURL, igUser, igPassword);

		if(createDatabase.equalsIgnoreCase("true"))
			createDatabaseAndUsers(jdbcDriverName, dbProvider, dbName, dbServer, dbPort, dbInstance, dbUser, dbPassword, igUser, igPassword, reason);
				
		CastorDatabaseService.reconnectDatabase();
		InfoGlueJDBCPropertySet.initReloadConfiguration();

		if(addExampleRepositories.equalsIgnoreCase("true"))
			createSampleSites();
	}
	
	private String getDBInstance(String jdbcURL, String dbProvider) 
	{
		return null;
		/*
		String dbPort = "";
		//jdbc:mysql://localhost:3307/infoglue333?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8
		if(dbProvider.equals("mysql"))
			dbPort = jdbcURL.substring(jdbcURL.indexOf(":", 12), jdbcURL.indexOf("/", 13));
		if(dbProvider.equals("mssqlserver"))
			dbPort = jdbcURL.substring(jdbcURL.indexOf(":", 12), jdbcURL.indexOf("/", 13));

		logger.info("dbPort:" + dbPort);
		return dbPort;
		*/
	}

	private String getDBPort(String jdbcURL, String dbProvider) 
	{
		String dbPort = "";
		//jdbc:mysql://localhost:3307/infoglue333?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8
		if(dbProvider.equals("mysql"))
			dbPort = jdbcURL.substring(jdbcURL.indexOf(":", 12) + 1, jdbcURL.indexOf("/", 13));
		if(dbProvider.equals("mssqlserver"))
			dbPort = jdbcURL.substring(jdbcURL.indexOf(":", 12) + 1, jdbcURL.indexOf("/", 13));

		logger.info("dbPort:" + dbPort);
		return dbPort;
	}

	private String getDBServer(String jdbcURL, String dbProvider) 
	{
		String dbServer = "";
		//jdbc:mysql://localhost:3307/infoglue333?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8
		if(dbProvider.equals("mysql"))
			dbServer = jdbcURL.substring(jdbcURL.indexOf("://") + 3, jdbcURL.indexOf(":", 12));
		if(dbProvider.equals("mssqlserver"))
			dbServer = jdbcURL.substring(jdbcURL.indexOf("://") + 3, jdbcURL.indexOf(":", 12));

		logger.info("dbServer:" + dbServer);
		return dbServer;
	}

	private String getDBName(String jdbcURL, String dbProvider) 
	{
		String dbName = "";
		int endIndex = jdbcURL.indexOf("?");
		if(endIndex == -1)
			endIndex = jdbcURL.length() - 1;
		
		//jdbc:mysql://localhost:3307/infoglue333?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8
		if(dbProvider.equals("mysql"))
			dbName = jdbcURL.substring(jdbcURL.lastIndexOf("/") + 1, endIndex);
		if(dbProvider.equals("mssqlserver"))
			dbName = jdbcURL.substring(jdbcURL.lastIndexOf("/") + 1, endIndex);

		logger.info("dbName:" + dbName);
		return dbName;
	}

	public void updateDatabase(String dbProvider, String dbName, String dbServer, String dbPort, String dbInstance, String createDatabase, String addExampleRepositories, String dbUser, String dbPassword, String igUser, String igPassword, HttpSession session) throws Exception 
	{
		if(dbProvider.equals("mysql") || dbProvider.equals("mssqlserver"))
		{
			if(dbProvider.equals("") || 
			   dbName.equals("") || 
			   dbServer.equals("") || 
			   dbPort.equals("") ||  
			   igUser.equals("") || 
			   igPassword.equals("") || 
			   (createDatabase.equalsIgnoreCase("true") && (
				dbUser.equals("") /*|| 
			    dbPassword.equals("")*/)))
			{
				throw new Exception("Mandatory field(s) missing");
			}
		}
		else
		{
			if(dbProvider.equals("") || 
				dbName.equals("") ||
				dbServer.equals("") || 
				dbPort.equals("") ||  
				igUser.equals("") || 
				igPassword.equals(""))
			{
				logger.info("dbProvider:" + dbProvider);
				logger.info("dbServer:" + dbServer);
				logger.info("dbPort:" + dbPort);
				logger.info("igUser:" + igUser);
				logger.info("igPassword:" + igPassword);
				throw new Exception("Mandatory field(s) missing");
			}
		}
		
		String castorProviderName = getCastorProviderName(dbProvider);
		String jdbcDriverName = getJDBCDriverName(dbProvider);
		String jdbcURL = getJDBCURL(dbProvider, dbName, dbServer, dbPort, dbInstance);
		String databaseMappingFile = getDatabaseMappingFile(dbProvider);
		
		if(!createDatabase.equalsIgnoreCase("true"))
			validateConnection(jdbcDriverName, jdbcURL, igUser, igPassword);

		if(logger.isInfoEnabled())
			logger.info("createDatabase:" + createDatabase);
		if(createDatabase.equalsIgnoreCase("true"))
			createDatabaseAndUsers(jdbcDriverName, dbProvider, dbName, dbServer, dbPort, dbInstance, dbUser, dbPassword, igUser, igPassword, -1);
		
		//if(true)
		//	return;
		
		session.setAttribute("install_dbProvider", dbProvider);
		session.setAttribute("install_jdbcDriverName", jdbcDriverName);
		session.setAttribute("install_jdbcURL", jdbcURL);
		session.setAttribute("install_dbUser", igUser);
		session.setAttribute("install_dbPassword", igPassword);
		
		//Castor database.xml
		String castorDatabaseXMLPath = CastorDatabaseService.class.getResource("/database.xml").getPath();
		if(logger.isInfoEnabled())
			logger.info("castorDatabaseXMLPath:" + castorDatabaseXMLPath);
		String contents = FileHelper.getStreamAsString(CastorDatabaseService.class.getResourceAsStream("/database.xml"));
		if(logger.isInfoEnabled())
			logger.info("contents:" + contents);
		
		contents = contents.replaceAll("@database.driver.engine@", castorProviderName);
		contents = contents.replaceAll("@database.driver.class@", jdbcDriverName);
		contents = contents.replaceAll("@database.user@", igUser);
		contents = contents.replaceAll("@database.password@", igPassword);
		contents = contents.replaceAll("@database.url@", jdbcURL);
		contents = contents.replaceAll("@database.validationQuery@", "select 1 from cmInfoGlueProperties");
		contents = contents.replaceAll("@database.maxConnections@", "300");
		contents = contents.replaceAll("@database.mapping@", databaseMappingFile);
		
		if(logger.isInfoEnabled())
			logger.info("contents after:" + contents);
		if(logger.isInfoEnabled())
			logger.info("Want to write to:" + castorDatabaseXMLPath);
		File targetFile = new File(castorDatabaseXMLPath);
		if(logger.isInfoEnabled())
			logger.info("targetFile:" + targetFile.exists());
		
		FileHelper.writeToFile(new File(castorDatabaseXMLPath), contents, false);
		//END Castor database.xml

		//Hibernate config
		String hibernateXMLPath = CastorDatabaseService.class.getResource("/hibernate.cfg.xml").getPath();
		if(logger.isInfoEnabled())
			logger.info("castorDatabaseXMLPath:" + castorDatabaseXMLPath);
		String hibernateConents = FileHelper.getStreamAsString(CastorDatabaseService.class.getResourceAsStream("/hibernate.cfg.xml"));
		if(logger.isInfoEnabled())
			logger.info("hibernateConents:" + hibernateConents);
		
		hibernateConents = hibernateConents.replaceAll("@database.driver.engine@", castorProviderName);
		hibernateConents = hibernateConents.replaceAll("@database.driver.class@", jdbcDriverName);
		hibernateConents = hibernateConents.replaceAll("@database.user@", igUser);
		hibernateConents = hibernateConents.replaceAll("@database.password@", igPassword);
		hibernateConents = hibernateConents.replaceAll("@database.url@", jdbcURL);
		hibernateConents = hibernateConents.replaceAll("@database.validationQuery@", "select 1 from cmInfoGlueProperties");
		hibernateConents = hibernateConents.replaceAll("@database.maxConnections@", "300");
		hibernateConents = hibernateConents.replaceAll("@database.mapping@", databaseMappingFile);
		hibernateConents = hibernateConents.replaceAll("@hibernate.dialect@", "net.sf.hibernate.dialect.MySQLDialect");
		hibernateConents = hibernateConents.replaceAll("@hibernate.show_sql@", "false");
		
		if(logger.isInfoEnabled())
			logger.info("hibernateConents after:" + hibernateConents);
		if(logger.isInfoEnabled())
			logger.info("Want to write to:" + hibernateXMLPath);
		File hibernateTargetFile = new File(hibernateXMLPath);
		if(logger.isInfoEnabled())
			logger.info("hibernateTargetFile:" + hibernateTargetFile.exists());
		
		FileHelper.writeToFile(new File(hibernateXMLPath), hibernateConents, false);
		//END hibernate
		
		//Hibernate config
		String propertySetXMLPath = CastorDatabaseService.class.getResource("/propertyset.xml").getPath();
		if(logger.isInfoEnabled())
			logger.info("propertySetXMLPath:" + propertySetXMLPath);
		String propertysetContents = FileHelper.getStreamAsString(CastorDatabaseService.class.getResourceAsStream("/propertyset.xml"));
		if(logger.isInfoEnabled())
			logger.info("propertysetContents:" + propertysetContents);
		
		propertysetContents = propertysetContents.replaceAll("@database.driver.engine@", castorProviderName);
		propertysetContents = propertysetContents.replaceAll("@database.driver.class@", jdbcDriverName);
		propertysetContents = propertysetContents.replaceAll("@database.user@", igUser);
		propertysetContents = propertysetContents.replaceAll("@database.password@", igPassword);
		propertysetContents = propertysetContents.replaceAll("@database.url@", jdbcURL);
		propertysetContents = propertysetContents.replaceAll("@database.validationQuery@", "select 1 from cmInfoGlueProperties");
		propertysetContents = propertysetContents.replaceAll("@database.maxConnections@", "300");
		propertysetContents = propertysetContents.replaceAll("@database.mapping@", databaseMappingFile);
		propertysetContents = propertysetContents.replaceAll("@hibernate.dialect@", "net.sf.hibernate.dialect.MySQLDialect");
		propertysetContents = propertysetContents.replaceAll("@hibernate.show_sql@", "false");
		
		if(logger.isInfoEnabled())
			logger.info("propertysetContents after:" + propertysetContents);
		if(logger.isInfoEnabled())
			logger.info("Want to write to:" + propertySetXMLPath);
		File propertySetTargetFile = new File(propertySetXMLPath);
		if(logger.isInfoEnabled())
			logger.info("propertySetTargetFile:" + propertySetTargetFile.exists());
		
		FileHelper.writeToFile(propertySetTargetFile, propertysetContents, false);
		//END hibernate		
		
		CastorDatabaseService.reconnectDatabase();
		InfoGlueJDBCPropertySet.initReloadConfiguration();

		if(addExampleRepositories.equalsIgnoreCase("true"))
			createSampleSites();
	}

	private void createSampleSites() throws SystemException 
	{
		Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			//now restore the value and list what we get
			File file = new File(CmsPropertyHandler.getSQLUpgradePath() + File.separator + "www.infoglue.org.xml");
			if(file == null || !file.exists())
				throw new SystemException("The file upload must have gone bad as no example site was found.");
			
			String encoding = "UTF-8";
			int version = 2;
			
            Mapping map = new Mapping();
		    logger.info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_site_2.5.xml").toString());
		    map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_site_2.5.xml").toString());	

			// All ODMG database access requires a transaction
			db.begin();
			
			Map contentIdMap = new HashMap();
			Map siteNodeIdMap = new HashMap();
			List allContentIds = new ArrayList();

			Map<String,String> replaceMap = new HashMap<String,String>();
			ImportController.getController().importRepository(db, map, file, encoding, version, "false", false, contentIdMap, siteNodeIdMap, allContentIds, replaceMap, false);
			
			db.commit();
			db.close();
			
			Iterator allContentIdsIterator = allContentIds.iterator();
			while(allContentIdsIterator.hasNext())
			{
				Integer contentId = (Integer)allContentIdsIterator.next();
				try
				{
					db = CastorDatabaseService.getDatabase();
					db.begin();
	
					Content content = ContentController.getContentController().getContentWithId(contentId, db);
					ImportController.getController().updateContentVersions(content, contentIdMap, siteNodeIdMap, "false", new HashMap());
	
					db.commit();
				}
				catch(Exception e)
				{
					try
					{
						db.rollback();
					}
					catch(Exception e2) { e2.printStackTrace(); }
	                logger.error("An error occurred when updating content version for content: " + e.getMessage(), e);					
				}
				finally
				{
					db.close();					
				}
			}
		} 
		catch ( Exception e) 
		{
			try
            {
                db.rollback();
                db.close();
            } 
			catch (Exception e1)
            {
                logger.error("An error occurred when importing a repository: " + e.getMessage(), e);
    			throw new SystemException("An error occurred when importing a repository: " + e.getMessage(), e);
            }
			
			logger.error("An error occurred when importing a repository: " + e.getMessage(), e);
			throw new SystemException("An error occurred when importing a repository: " + e.getMessage(), e);
		}
	}

	private void validateConnection(String jdbcDriverName, String jdbcURL, String igUser, String igPassword) throws Exception 
	{
		Connection conn = getConnection(jdbcDriverName, jdbcURL, igUser, igPassword);
		//issueCommand(conn, "select count(*) from user");
	}

	private void createDatabaseAndUsers(String jdbcDriverName, String dbProvider, String dbName, String dbServer, String dbPort, String dbInstance, String dbUser, String dbPassword, String igUser, String igPassword, int reason) throws Exception 
	{
		if(dbProvider.equalsIgnoreCase("mysql"))
		{
			String mysqlJdbcURL = getJDBCURL(dbProvider, "mysql", dbServer, dbPort, dbInstance);
			String mysqlJdbcIGURL = getJDBCURL(dbProvider, dbName, dbServer, dbPort, dbInstance);

			validateConnection(jdbcDriverName, mysqlJdbcURL, dbUser, dbPassword);
			if(logger.isInfoEnabled())
				logger.info("Efter....");
			
			if(reason == DATABASE_SERVER_MISSING_DATABASE_TABLES)
			{
				createTables(jdbcDriverName, mysqlJdbcIGURL, dbProvider, dbUser, dbPassword, dbName, igUser, igPassword);
				createInitialData(jdbcDriverName, mysqlJdbcIGURL, dbProvider, dbUser, dbPassword, dbName, igUser, igPassword);
			}
			else
			{
				issueCommand(getConnection(jdbcDriverName, mysqlJdbcURL, dbUser, dbPassword), "CREATE DATABASE " + dbName + ";");
				createUsersMYSQL(jdbcDriverName, dbServer, dbPort, dbUser, dbPassword, dbName, igUser, igPassword);
				createTables(jdbcDriverName, mysqlJdbcIGURL, dbProvider, dbUser, dbPassword, dbName, igUser, igPassword);
				createInitialData(jdbcDriverName, mysqlJdbcIGURL, dbProvider, dbUser, dbPassword, dbName, igUser, igPassword);
			}
		}
		else if(dbProvider.equalsIgnoreCase("oracle"))
		{
			String oracleJdbcIGURL = getJDBCURL(dbProvider, dbName, dbServer, dbPort, dbInstance);

			validateConnection(jdbcDriverName, oracleJdbcIGURL, igUser, igPassword);
			
			createTables(jdbcDriverName, oracleJdbcIGURL, dbProvider, igUser, igPassword, dbName, igUser, igPassword);
			createInitialData(jdbcDriverName, oracleJdbcIGURL, dbProvider, igUser, igPassword, dbName, igUser, igPassword);
		}
		else if(dbProvider.equalsIgnoreCase("mssqlserver"))
		{
			String sqlServerJdbcURL = getJDBCURL(dbProvider, null, dbServer, dbPort, dbInstance);
			String sqlServerIGJdbcURL = getJDBCURL(dbProvider, dbName, dbServer, dbPort, dbInstance);

			validateConnection(jdbcDriverName, sqlServerJdbcURL, dbUser, dbPassword);
			
			if(reason == DATABASE_SERVER_MISSING_DATABASE_TABLES)
			{
				createTables(jdbcDriverName, sqlServerIGJdbcURL, dbProvider, dbUser, dbPassword, dbName, igUser, igPassword);
				createInitialData(jdbcDriverName, sqlServerIGJdbcURL, dbProvider, dbUser, dbPassword, dbName, igUser, igPassword);
			}
			else
			{
				try
				{
					Connection conn = getConnection(jdbcDriverName, sqlServerIGJdbcURL, dbUser, dbPassword);
					
				    try
				    {
				        String sql = "SELECT * FROM cmSiteNodeVersion";
				        if(dbProvider.equalsIgnoreCase("oracle") || dbProvider.equalsIgnoreCase("db2"))
				            sql = "SELECT * FROM cmSiNoVer";
				        
				        PreparedStatement pstmt = conn.prepareStatement(sql);
						ResultSet rs = pstmt.executeQuery();
						rs.next();
						
						rs.getString("isHidden"); //If this throws exception then it's older than 2.3
				    }
				    catch(Exception e)
				    {
				        e.printStackTrace();
				    }
				}
				catch(Exception e)
				{
					issueCommand(getConnection(jdbcDriverName, sqlServerJdbcURL, dbUser, dbPassword), "CREATE DATABASE " + dbName + ";");
					//callProcedure(getConnection(jdbcDriverName, sqlServerJdbcURL, dbUser, dbPassword), "sp_dbcmptlevel", dbName, "80");
				}
				
				createUsersSQLServer(jdbcDriverName, dbServer, dbPort, dbInstance, dbUser, dbPassword, dbName, igUser, igPassword);
				createTables(jdbcDriverName, sqlServerIGJdbcURL, dbProvider, dbUser, dbPassword, dbName, igUser, igPassword);
				createInitialData(jdbcDriverName, sqlServerIGJdbcURL, dbProvider, dbUser, dbPassword, dbName, igUser, igPassword);
			}
		}
		
	}

	private String getDatabaseMappingFile(String dbProvider) 
	{
		String databaseMappingFile = "mapping.xml";
		if(dbProvider.equalsIgnoreCase("oracle"))
			databaseMappingFile = "oracle_mapping.xml";
		if(dbProvider.equalsIgnoreCase("db2"))
			databaseMappingFile = "oracle_mapping.xml";
			
		return databaseMappingFile;
	}

	private String getJDBCURL(String dbProvider, String dbName, String dbServer, String dbPort, String dbInstance) 
	{
		String JDBCURL = "";
		if(dbProvider.equalsIgnoreCase("mysql"))
			JDBCURL = "jdbc:mysql://" + dbServer + ":" + dbPort + "/" + dbName + "";
		if(dbProvider.equalsIgnoreCase("oracle"))
			JDBCURL = "jdbc:oracle:thin:@" + dbServer + ":" + dbPort + ":" + dbName + "";
		if(dbProvider.equalsIgnoreCase("db2"))
			JDBCURL = "jdbc:db2://" + dbServer + ":" + dbPort + "/" + dbName + "";
		if(dbProvider.equalsIgnoreCase("mssqlserver"))
			JDBCURL = "jdbc:jtds:sqlserver://" + dbServer + ":" + dbPort + ((dbInstance.equalsIgnoreCase("") ? (dbName == null ? "" : ";DatabaseName=" + dbName) + ";SelectMethod=Cursor" : ";INSTANCE=" + dbInstance + (dbName == null ? "" : ";DatabaseName=" + dbName) + ";SelectMethod=Cursor"));
			
		return JDBCURL;
	}

	public String getTDSSpecficUrl(String hostName, String databasePortNumber, String database, String instance)
	{
		String url = "jdbc:jtds:sqlserver://" + hostName + ":" + databasePortNumber + ((instance.equalsIgnoreCase("") ? ";DatabaseName=" + database + ";SelectMethod=Cursor" : ";INSTANCE=" + instance + ";DatabaseName=" + database + ";SelectMethod=Cursor"));
		return url;
	}

	private String getJDBCDriverName(String dbProvider) 
	{
		if(dbProvider.equalsIgnoreCase("mysql"))
			return "com.mysql.jdbc.Driver";
		if(dbProvider.equalsIgnoreCase("db2"))
			return "com.ibm.db2.jcc.DB2Driver";
		if(dbProvider.equalsIgnoreCase("oracle"))
			return "oracle.jdbc.driver.OracleDriver";
		if(dbProvider.equalsIgnoreCase("mssqlserver"))
			return "net.sourceforge.jtds.jdbc.Driver";
		
		return "";
	}

	private String getCastorProviderName(String dbProvider) 
	{
		if(dbProvider.equalsIgnoreCase("mysql"))
			return "mysql";
		if(dbProvider.equalsIgnoreCase("db2"))
			return "db2";
		if(dbProvider.equalsIgnoreCase("oracle"))
			return "oracle";
		if(dbProvider.equalsIgnoreCase("mssqlserver"))
			return "sql-server";
		
		return "";
	}
	
	
	private void createUsersMYSQL(String driverClass, String databaseHostName, String databasePortNumber, String dbUser, String dbPassword, String dbName, String igUser, String igPassword) throws Exception
	{
		String url = "jdbc:mysql://" + databaseHostName + ":" + databasePortNumber + "/mysql";
		Connection conn = getConnection(driverClass, url, dbUser, dbPassword);
		
		try
		{
			issueCommand(conn, "GRANT ALL PRIVILEGES ON `" + dbName + "`.* TO '" + igUser + "'@'%' IDENTIFIED BY '" + igPassword + "';");
			issueCommand(conn, "GRANT ALL PRIVILEGES ON `" + dbName + "`.* TO '" + igUser + "'@'localhost' IDENTIFIED BY '" + igPassword + "';");
			issueCommand(conn, "GRANT ALL PRIVILEGES ON `" + dbName + "`.* TO '" + igUser + "'@'127.0.0.1' IDENTIFIED BY '" + igPassword + "';");
			issueCommand(conn, "GRANT ALL PRIVILEGES ON `" + dbName + "`.* TO '" + igUser + "'@'" + databaseHostName + "' IDENTIFIED BY '" + igPassword + "';");
			issueCommand(conn, "GRANT ALL PRIVILEGES ON `" + dbName + "`.* TO '" + igUser + "'@'" + getHostAddress() + "' IDENTIFIED BY '" + igPassword + "';");
		}
		catch (Exception e) 
		{
			logger.error("Error creating users or database:" + e.getMessage() + ".");
			try{conn.close();}catch(Exception e2){}
			throw e;
		}
	}

	private void createUsersSQLServer(String driverClass, String databaseHostName, String databasePortNumber, String databaseInstance, String dbUser, String dbPassword, String dbName, String igUser, String igPassword) throws Exception
	{
		String url = "jdbc:jtds:sqlserver://" + databaseHostName + ":" + databasePortNumber + ((databaseInstance.equalsIgnoreCase("")) ? "" : ";INSTANCE=" + databaseInstance);
		Connection conn = getConnection(driverClass, url, dbUser, dbPassword);
		
		try
		{
			issueCommand(conn, "use master;");
			issueCommand(conn, "drop database " + dbName + ";");
			issueCommand(conn, "drop login " + igUser + ";");
		}
		catch (Exception e) 
		{
			logger.error("Dropping old objects threw error:" + e.getMessage() + ". Let's continue...");
		}
		
		try
		{
			issueCommand(conn, "CREATE LOGIN " + igUser + " WITH PASSWORD = '" + igPassword + "';");
			issueCommand(conn, "CREATE DATABASE " + dbName + ";");
			issueCommand(conn, "ALTER LOGIN " + igUser + " WITH DEFAULT_DATABASE=" + dbName + ";");
			issueCommand(conn, "use " + dbName + ";");
			callProcedure(conn, "sp_changedbowner", igUser, "false");
		}
		catch (Exception e) 
		{
			logger.error("Error creating users or database:" + e.getMessage() + ".");
			try{conn.close();}catch(Exception e2){}
			throw e;
		}
		
		/* AAAA#2sss4
		//callProcedure(conn, "sp_addlogin", igUser, igPassword, dbName);
		issueCommand(conn, "CREATE LOGIN " + igUser + " WITH PASSWORD = '" + igPassword + "', default_database = " + dbName + ";");
		
		conn.close();
		
		url = getJDBCURL("mssqlserver", dbName, databaseHostName, databasePortNumber, databaseInstance);
		
		conn = getConnection(driverClass, url, dbUser, dbPassword);

		issueCommand(conn, "CREATE USER " + igUser + " FOR LOGIN " + igUser + " WITH DEFAULT_SCHEMA=" + dbName + ";");
		issueCommand(conn, "GRANT ALTER TO " + igUser + ";");
		issueCommand(conn, "GRANT CONTROL TO " + igUser + ";");
		
		//callProcedure(conn, "sp_grantdbaccess", igUser, dbName);
		//callProcedure(conn, "sp_changedbowner", igUser);		
		callProcedure(conn, "sp_addrolemember", "db_owner", igUser);		
		//callProcedure(conn, "sp_addrolemember", "db_owner", dbName);
		*/
	}

	private void createTables(String driverClass, String url, String dbProvider, String dbUser, String dbPassword, String dbName, String igUser, String igPassword) throws Exception
	{
		if(logger.isInfoEnabled())
			logger.info("Creating tables:" + CmsPropertyHandler.getSQLUpgradePath() + File.separator + "infoglue_core_schema_" + dbProvider + ".sql");

		Connection conn = getConnection(driverClass, url, dbUser, dbPassword);
		
		logger.warn("Setting up a new database....");		
		
		try
		{
			File coreSchemaFile = new File(CmsPropertyHandler.getSQLUpgradePath() + File.separator + "infoglue_core_schema_" + dbProvider + ".sql");
			FileInputStream fis = new FileInputStream(coreSchemaFile);
			StringBuffer sb = new StringBuffer();
			int c;
			while((c = fis.read()) != -1)
			{
				char character = (char)c;
				sb.append(character);
			}
			String script = sb.toString();
			logger.info("script:" + script);
			
			int rows = 0;
			StringTokenizer st = new StringTokenizer(script, ";");
		    while (st.hasMoreTokens()) 
		    {
		    	String command = st.nextToken();
		    	//Logger.logInfo("Command: " + command);
		    	try
		    	{
			    	if(dbProvider.equals("oracle"))
				    	issueCommand(conn, command);
			    	else
			    		issueCommand(conn, command + ";");
		    	}
		    	catch (SQLException sqle) 
		    	{
		    		logger.error("An sql error:" + sqle.getMessage());
		    		if(sqle.getMessage().indexOf("sequence does not exist") > -1 || sqle.getMessage().indexOf("ORA-02289") > -1 || sqle.getMessage().indexOf("ORA-00942") > -1 || command.trim().toLowerCase().startsWith("drop "))
		    			logger.error("Was an unharmful error - proceed");
		    		else
		    			throw sqle;
				}
				rows++;
				//if(rows > 10)
				//	break;
		    }
				
		}
		catch(Exception e)
		{
			logger.error("Problem running sql script:" + e.getMessage(), e);
			throw e;
		}
	}

	private void createInitialData(String driverClass, String url, String dbProvider, String dbUser, String dbPassword, String dbName, String igUser, String igPassword) throws Exception
	{
		logger.info("Creating initial data:" + CmsPropertyHandler.getSQLUpgradePath() + File.separator + "infoglue_initial_data_" + dbProvider + ".sql");
		//String url = "jdbc:mysql://" + databaseHostName + ":" + databasePortNumber + "/" + dbName + "";
		Connection conn = getConnection(driverClass, url, igUser, igPassword);
		
		try
		{
			File initialDataFile = new File(CmsPropertyHandler.getSQLUpgradePath() + File.separator + "infoglue_initial_data_" + dbProvider + ".sql");
			FileInputStream fis = new FileInputStream(initialDataFile);
			StringBuffer sb = new StringBuffer();
			int c;
			while((c = fis.read()) != -1)
			{
				char character = (char)c;
				sb.append(character);
			}
			String script = sb.toString();
			//Logger.logInfo("script:" + script);
			
			if(!dbProvider.equals("oracle") && !dbProvider.equals("db2"))
			{
				String[] commands = script.split("#endquery");
				logger.info("Parsed " + commands.length + " commands from script");
				
				for(int i=0; i<commands.length; i++)
				{
					String command = commands[i];
					logger.info("command:" + command);
					try
					{
						if(command.indexOf("SPECIAL") > -1)
							issueSpecialCommand(conn, command.trim());		
						else
							issueCommand(conn, command.trim());
					}
					catch (Exception e) 
					{
						logger.error("Error:" + e.getMessage());
					}
				}
			}
			else
			{
				String[] commands = script.split("--endquery");
				logger.info("Parsed " + commands.length + " commands from script");
				
				for(int i=0; i<commands.length; i++)
				{
					String command = commands[i];
					if(command.indexOf("SPECIAL") > -1)
						issueSpecialCommand(conn, command.trim());		
					if(command.indexOf("BLOB") > -1)
						issueSpecialBlobCommand(conn, command.trim());		
					else
					{	
						command = command.trim();
						if(!command.equalsIgnoreCase(""))
						{
						    command = command.substring(0, command.length()-1);
							issueCommand(conn, command);
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.error("Problem running sql script:" + e.getMessage(), e);
			throw e;
		}	
	}

	public void updateDatabaseTables(HttpSession session) throws Exception
	{
		String dbProvider = (String)session.getAttribute("install_dbProvider");
		String jdbcDriverName = (String)session.getAttribute("install_jdbcDriverName");
		String jdbcURL = (String)session.getAttribute("install_jdbcURL");
		String igUser = (String)session.getAttribute("install_dbUser");
		String igPassword = (String)session.getAttribute("install_dbPassword");

		if(dbProvider == null)
		{
			dbProvider = getJDBCEngine();
			session.setAttribute("install_dbProvider", dbProvider);
		}
		
		if(jdbcDriverName == null)
		{
			jdbcDriverName = getJDBCParamFromCastorXML("//param[@name='driver-class-name']");
			session.setAttribute("install_jdbcDriverName", jdbcDriverName);
		}

		if(jdbcURL == null)
		{
			jdbcURL = getJDBCParamFromCastorXML("//param[@name='url']");
			session.setAttribute("install_jdbcURL", jdbcURL);
		}

		if(igUser == null)
		{
			igUser = getJDBCParamFromCastorXML("//param[@name='username']");
			session.setAttribute("install_dbUser", igUser);
		}

		if(igPassword == null)
		{
			igPassword = getJDBCParamFromCastorXML("//param[@name='password']");
			session.setAttribute("install_dbPassword", igPassword);
		}

		Connection conn = getConnection(jdbcDriverName, jdbcURL, igUser, igPassword);
		
		String dbVersion = getCurrentDatabaseVersion(session);
		logger.info("dbVersion:" + dbVersion);
		String sql = getUpgradeScripts(dbVersion, session);
		logger.info("sql:" + sql);
		sql = sql.replaceAll("--.*", "");
		logger.info("sql:" + sql);
		
		StringTokenizer st = new StringTokenizer(sql, ";");
	    while (st.hasMoreTokens()) 
	    {
	    	try
	    	{
	    		String command = st.nextToken();
	    		//Logger.logInfo("Command: " + command);
				issueCommand(conn, command + ";");
		    }
	        catch(SQLException ex) 
	        {
	        	logger.error("Command failed: " + ex.getMessage() + "\n" + "SQL: " + sql);
	        }
	   	}
	}

	public Connection getConnection(String driverClass, String url, String userName, String password) throws Exception
	{
		Connection conn = null;
    
        // Load the JDBC driver
		logger.info("Loading JDBC driver " + driverClass + "\n");
        Class.forName(driverClass).newInstance();
    	
        // Connect to the databse
        logger.info("Connecting to database on " + url);
        conn = DriverManager.getConnection(url, userName, password);
        logger.info("Connected...");
        
        return conn;
	}

	/**
	 * This method issues command to the db.
	 * @throws SQLException 
	 */
	
	private void issueCommand(Connection conn, String sql) throws SQLException
	{
		sql = sql.trim();
		
		if(sql == null || sql.trim().length() == 0 || sql.trim().equalsIgnoreCase(";"))
			return;
			  
		//logger.info("sql: " + sql);
		
        try 
        {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.execute();  
            pstmt.close();
        }
        catch(SQLException ex) 
        {
        	logger.error("Command failed: " + ex.getMessage() + "\n" + "SQL: " + sql);
        	throw ex;
        }
	}
	
	/**
	 * This method issues special command to the db. I had to build my own adoption of sql to make this feature.
	 * @throws Exception 
	 */
	
	protected void issueSpecialCommand(Connection conn, String sql) throws Exception
	{
        logger.warn("Command:" + sql);
                
        try 
        {
        	String tableName 	= null;
        	String columnName 	= null;
        	String image		= null;
        	String idColumn		= null;
        	String idValue 		= null;
        	
        	StringTokenizer st = new StringTokenizer(sql, " ");
			int i = 0;
		    while (st.hasMoreTokens()) 
		    {
		    	String part = st.nextToken();
		    	//Logger.logInfo("Part: " + part);
		    	if(i == 2)
		    		tableName = part;
		    	if(i == 4)
		    		columnName = part;
		    	if(i == 6)
		    		image = part;
		    	if(i == 8)
		    		idColumn = part;
		    	if(i == 10)
		    		idValue = part;
		    	
		    	i++;
			}
			
			File file = new File(image);
	        FileInputStream fis = new FileInputStream(file);
	        byte[] imageByteArray = new byte[(int)file.length()];
	        fis.read(imageByteArray);	
			
			        	
            sql = "UPDATE " + tableName + " SET " + columnName + " = ? WHERE " + idColumn + " = " + idValue + "";
			//Logger.logInfo("newSQL:" + newSQL);
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setBytes(1, imageByteArray);
			ps.executeUpdate();
        }
        catch(Exception ex) 
        {
        	logger.error("Command failed: " + ex.getMessage() + "\n" + "SQL: " + sql);
        	throw ex;
        }
	}

	/**
	 * This method issues special blob-inserts command to the db. 
	 * I had to build my own adoption of sql to make this feature.
	 */

	protected void issueSpecialBlobCommand(Connection conn, String originalSql) throws Exception
	{
		String sql = originalSql;
		//Logger.logInfo("SpecialBlob Command:" + sql);
            
		try 
		{
			String valuesPart = sql.substring(sql.indexOf("VALUES") + 6).trim();
			sql = sql.substring(0, sql.indexOf("VALUES") + 6);
			//logger.info("sql:" + sql);
			//logger.info("valuesPart:" + valuesPart);
			
			String tableName 		= null;
			int blobColumn			= 0;
			List columns 			= null;
			List columnValues 		= null;
			
			StringTokenizer st = new StringTokenizer(sql, " ");
			int i = 0;
			while (st.hasMoreTokens()) 
			{
				String part = st.nextToken();
				//Logger.logInfo("Part: " + part);
				
				if(i == 1)
					blobColumn = new Integer(part).intValue();
				if(i == 4)
					tableName = part;
				if(i == 5)
				{	
					columns = parseColumns(part);
				}
				
				i++;
			}
			
			columnValues = parseValues(valuesPart);
			
			String columnsString = "";
			String valuesString = "";
			Iterator columnsIterator = columns.iterator();
			while(columnsIterator.hasNext())
			{
				columnsString += (columnsString.equals("")) ? (String)columnsIterator.next() : "," + columnsIterator.next();
				valuesString += (valuesString.equals("")) ? "?" : ",?";
			}
			
			sql = "INSERT INTO " + tableName + "(" + columnsString + ") VALUES (" + valuesString + ")";
			
			
			PreparedStatement ps = conn.prepareStatement(sql);
			
			int index = 1;
			int loopCount = 0;
			Iterator columnValuesIterator = columnsIterator = columns.iterator();
			while(columnsIterator.hasNext())
			{
				columnsIterator.next();
				String value = (String)columnValues.get(loopCount);
				
				if(index == 1 || value.indexOf("'") == -1)
				{
					ps.setInt(index, new Integer(value).intValue());
				}
				else if(index == blobColumn)
				{	
					//Logger.logInfo("value:" + value);
					value = value.substring(1, value.length() - 1);
					//Logger.logInfo("value:" + value);
					
					if(value.indexOf("assetBlob:") > -1)
					{
						String fileName = value.substring(10);
						FileInputStream fis = new FileInputStream(fileName);
						
						BLOB bl = BLOB.createTemporary(conn, true, BLOB.DURATION_CALL);
						bl.open(BLOB.MODE_READWRITE);

						BufferedOutputStream out = new BufferedOutputStream(bl.getBinaryOutputStream());
		
						byte[] buffer = new byte[1024];
						int len;

						while((len = fis.read(buffer)) >= 0)
							out.write(buffer, 0, len);

						out.flush();
						fis.close();
						out.close();

						ps.setBlob(index, bl);
					}
					else
					{	
						CLOB cl = CLOB.createTemporary(conn, true, CLOB.DURATION_CALL);
						cl.putString(1,value);
						ps.setClob(index, cl);
					}
				}
				else if(value.indexOf("date:") > -1)
				{
					value = value.substring(6);
					Date date = parseDate(value, "yyyy-MM-dd HH:mm:ss");
					
					ps.setDate(index, new java.sql.Date(date.getTime()));
				}
				else
				{	
					//Logger.logInfo("value:" + value);
					value = value.substring(1, value.length() - 1);
					//Logger.logInfo("value:" + value);
					ps.setString(index, value);
				}
				
				index++;
				loopCount++;
			}

			ps.executeUpdate();
		}
		catch(Exception ex) 
		{
			logger.error("Command failed: " + ex.getMessage());
			logger.error("SQL: " + originalSql);
			throw ex;
		}
	}
	
	/**
	 * This method issues command to the db.
	 */
	
	private void callProcedure(Connection conn, String procedure, String arg1, String arg2, String arg3) throws Exception
	{
        logger.debug("procedure: " + procedure + " (" + arg1 + "," + arg2 + "," + arg3 + ")");
                
        try 
        {
            CallableStatement cs = conn.prepareCall("{call " + procedure + " (?,?,?)}");
			cs.setString(1, arg1);
			cs.setString(2, arg2);
			cs.setString(3, arg3);
			cs.execute();
			//cs.executeQuery();
            cs.close();
        	//conn.close();
            
            //logger.info("After procedure:" + rs);
        }
        catch(SQLException ex) 
        {
        	logger.error("callProcedure failed: " + ex.getMessage());
        	logger.error("procedure: " + procedure + " (" + arg1 + "," + arg2 + ")");
        	throw ex;
        }
	}

	
	/**
	 * This method issues command to the db.
	 */
	
	private void callProcedure(Connection conn, String procedure, String arg1) throws Exception
	{
		logger.debug("procedure: " + procedure + " (" + arg1 + ")");
                
        try 
        {
            CallableStatement cs = conn.prepareCall("{call " + procedure + " (?)}");
			cs.setString(1, arg1);
			cs.execute();
			//cs.executeQuery();
            cs.close();
        	//conn.close();
        }
        catch(SQLException ex) 
        {
        	ex.printStackTrace();
        	logger.error("callProcedure failed: " + ex.getMessage());
        	logger.error("procedure: " + procedure + " (" + arg1 + ")");
        	throw ex;
        }
	}

	/**
	 * This method issues command to the db.
	 */
	
	private void callProcedure(Connection conn, String procedure, String arg1, String arg2) throws Exception
	{
		logger.debug("procedure: " + procedure + " (" + arg1 + "," + arg2 + ")");
                
        try 
        {
            CallableStatement cs = conn.prepareCall("{call " + procedure + " (?,?)}");
			cs.setString(1, arg1);
			cs.setString(2, arg2);
			cs.execute();
			//cs.executeQuery();
            cs.close();
        	//conn.close();
        }
        catch(SQLException ex) 
        {
        	ex.printStackTrace();
        	logger.error("callProcedure failed: " + ex.getMessage());
        	logger.error("procedure: " + procedure + " (" + arg1 + "," + arg2 + ")");
        	throw ex;
        }
	}
	
	protected List parseColumns(String columnDefinition)
	{
		List columns = new ArrayList();
		
		//logger.info("columnDefinition:" + columnDefinition);
		columnDefinition = columnDefinition.substring(1, columnDefinition.length() - 1);
		//logger.info("columnDefinition:" + columnDefinition);
		
		StringTokenizer st = new StringTokenizer(columnDefinition, ",");
		while (st.hasMoreTokens()) 
		{
			String part = st.nextToken();
			//Logger.logInfo("Part: " + part);
			columns.add(part);
		}
		
		return columns;
	}
	
	public Date parseDate(String dateString, String pattern)
	{	
		if(dateString == null)
			return new Date();
    
		Date date = new Date();    
    
		try
		{
			SimpleDateFormat formatter = new SimpleDateFormat(pattern);
			date = formatter.parse(dateString);
		}
		catch(Exception e)
		{
			logger.error("Error parsing date:" + dateString);
		}
    
		return date;
	}
	
	protected List parseValues(String values)
	{
		List valueList = new ArrayList();
		
		//logger.info("values:" + values);
		values = values.substring(1, values.length() - 2);
		//logger.info("values:" + values);
		
		int offset = 0;
		int index = values.indexOf("[,]", offset);
		//StringTokenizer st = new StringTokenizer(values, "[,]");
		while (index > -1) 
		{
			String part = values.substring(offset, index);
			//String part = st.nextToken();
			//Logger.logInfo("Part: " + part);
			valueList.add(part);
			offset = index + 3;
			index = values.indexOf("[,]", offset);
		}
		
		String part = values.substring(offset);
		//Logger.logInfo("Part: " + part);
		valueList.add(part);
		
		return valueList;
	}
	
	public String getHostAddress()
    {
    	String address = null;
    	
    	try
    	{
    		address = java.net.InetAddress.getLocalHost().getHostAddress();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	return address;
    }

	/**
	 * This method should validate all aspects of the system. First validate if the database connection files are valid, next if the database is up2date and last if the server config files are valid.
	 * @return
	 */
	
	public boolean validateSetup() 
	{
		Boolean serverConfigOK = false;
		try { serverConfigOK = validateApplicationFile(); } catch (Exception e) { e.printStackTrace(); }
		
		boolean isValid = true;
		try
		{
			validateDatabaseConnection();
		}
		catch (Exception e) 
		{
			//e.printStackTrace();
			logger.error("Exception reading database: " + e.getMessage() + ". Let's check the database.xml for config options.");
			try 
			{
				int reason = getBrokenDatabaseReason();
				logger.error("Reason:" + reason);
				if(reason == DATABASE_PARAMETERS_MISSING)
					isValid = false;
				else if(reason == DATABASE_SERVER_DOWN)
					logger.error("Cannot classify this as an config issue");
				else if(reason == DATABASE_SERVER_MISSING_DATABASE || reason == DATABASE_SERVER_MISSING_DATABASE_TABLES)
					isValid = false;

				/*
				String url = getJDBCParamFromCastorXML("//param[@name='url']");
				if(url.indexOf("@database.url@") > -1)
				{
					isValid = false;
				}
				else
				{
					String applicationName = CmsPropertyHandler.getApplicationName();
					logger.info("applicationName:" + applicationName);
					logger.info("serverConfigOK:" + serverConfigOK);
					if(applicationName.equals("cms") && serverConfigOK)
					{
						logger.info("url:" + url);
						logger.info("e.getMessage():" + e.getMessage());
						logger.info("e.getCause().getMessage():" + (e.getCause() != null ? e.getCause().getMessage() : ""));
						
						int reason = getBrokenDatabaseReason();
						logger.info("Reason:" + reason);
						if(e.getMessage().indexOf("Unknown database") > -1 || e.getMessage().indexOf("Could not create connection to database server") > -1)
							isValid = false;
						else if(e.getCause() != null && e.getCause().getMessage().indexOf("Unknown database") > -1 || e.getCause().getMessage().indexOf("Could not create connection to database server") > -1)
							isValid = false;					
					}
				}
				*/
			} 
			catch (Exception e1) 
			{
				e1.printStackTrace();
				isValid = false;
			}
		}

		logger.info("Was it valid based on database connection:" + isValid);
		if(isValid)
		{
			try
			{
				String dbVersion = getCurrentDatabaseVersion(null);
				if(!dbVersion.equalsIgnoreCase(CmsPropertyHandler.getInfoGlueDBVersion()))
					isValid = false;
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}

		logger.info("Was it valid based on database version:" + isValid);
		if(isValid)
		{
			try
			{
				serverConfigOK = validateApplicationFile();
				if(!serverConfigOK)
					isValid = false;
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		
		logger.info("Was it valid based on application properties:" + isValid);
		
		return isValid;
	}


	public static final int ALL_OK = 0;
	public static final int DATABASE_PARAMETERS_MISSING = 1;
	public static final int DATABASE_SERVER_DOWN = 2;
	public static final int DATABASE_SERVER_MISSING_DATABASE = 3;
	public static final int DATABASE_SERVER_MISSING_DATABASE_TABLES = 4;
	
	public int getBrokenDatabaseReason()
	{
		//Lets find out why error
		try
	    {
			String jdbcDriverName = getJDBCParamFromCastorXML("//param[@name='driver-class-name']");
			String jdbcURL = getJDBCParamFromCastorXML("//param[@name='url']");
			String igUser = getJDBCParamFromCastorXML("//param[@name='username']");
			String igPassword = getJDBCParamFromCastorXML("//param[@name='password']");
			
			if(jdbcDriverName.indexOf("@database.url@") > -1)
				return DATABASE_PARAMETERS_MISSING;
			
			Connection conn = getConnection(jdbcDriverName, jdbcURL, igUser, igPassword);
			
			try
		    {
		        String sql = "SELECT * FROM cmInfoGlueProperties";
		        
		        PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery();
				rs.next();
				
				rs.close();
				pstmt.close();
		    }
		    catch(Exception e)
		    {
		        logger.error("Was missing database tables:" + e.getMessage());
		        return DATABASE_SERVER_MISSING_DATABASE_TABLES;
		    }
	    }
	    catch(Exception e)
	    {
	    	logger.error("Error getting pure connection:" + e.getMessage());
	    	
	    	if(e.getMessage().indexOf("Unknown database") > -1 || e.getCause() != null && e.getCause().getMessage().indexOf("Unknown database") > -1)
	    	{
	    		logger.error("Was a missing database error based on errorMessage");
	    		return DATABASE_SERVER_MISSING_DATABASE;
	    	}
	    	else if(e instanceof ConnectException || e.getCause() != null && e.getCause() instanceof ConnectException || e.getCause().getCause() != null && e.getCause().getCause() instanceof ConnectException)
	    	{
	    		logger.error("Was a connection error based on ConnectException");
	    		return DATABASE_SERVER_DOWN;
	    	}
	    	/*
	    	else if(e.getMessage().indexOf("Could not create connection to database server") > -1)
	    	{
	    		logger.error("Was a connection error based on errorMessage");
	    		return DATABASE_SERVER_DOWN;
	    	}
	    	*/
	    	
	    	return DATABASE_SERVER_MISSING_DATABASE;
	    	//return DATABASE_SERVER_MISSING_DATABASE;
	    }

		return ALL_OK;
	}
		
	
}