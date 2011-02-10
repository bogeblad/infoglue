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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.exolab.castor.mapping.Mapping;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.InterceptionPoint;
import org.infoglue.cms.entities.management.Interceptor;
import org.infoglue.cms.entities.management.InterceptorVO;
import org.infoglue.cms.entities.management.impl.simple.InterceptorImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.FileUploadHelper;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.infoglue.cms.util.workflow.InfoGlueJDBCPropertySet;
import org.infoglue.deliver.util.CacheController;
import org.jfree.util.Log;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

import webwork.action.ActionContext;

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
			e.printStackTrace();
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}
	
	public String getCurrentDatabaseVersion(HttpSession session) throws Exception
	{
		//if(true)
		//	return "2.0";
		
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
		
		String previousVersion = "3.0";

	    try
	    {
	        String sql = "SELECT * FROM cmSiteNodeVersion";
	        if(dbProvider.equalsIgnoreCase("oracle") || dbProvider.equalsIgnoreCase("db2"))
	            sql = "SELECT * FROM cmSiNoVer";
	        
	        PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			
			rs.getString("isHidden"); //If this throws exception then it's older than 2.3
			return "3.0";
	    }
	    catch(Exception e)
	    {
	        e.printStackTrace();
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
			
			rs.getString("forceProtocolChange"); //If this throws exception then it's older than 2.3
			return "2.9.8.7";
	    }
	    catch(Exception e)
	    {
	        e.printStackTrace();
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
	        e.printStackTrace();
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
	        e.printStackTrace();
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
	        e.printStackTrace();
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
	        e.printStackTrace();
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
	        e.printStackTrace();
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
		//System.out.println("cmsFilePath:" + cmsFilePath);
		String contents = FileHelper.getFileAsString(new File(cmsFilePath));
		//System.out.println("contents:" + contents.substring(0, 200));
		
	    if(contents.indexOf("configured=true") > -1)
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

		String jdbcEngine = getJDBCEngine();
		
		//cms.properties
		String cmsFilePath = CastorDatabaseService.class.getResource("/cms.properties").getPath();
		System.out.println("cmsFilePath:" + cmsFilePath);
		String contents = FileHelper.getStreamAsString(CastorDatabaseService.class.getResourceAsStream("/cms.properties"));
		System.out.println("contents:" + contents);
		
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

		contents = contents.replaceAll("@useShortTableNames@", ((jdbcEngine.equalsIgnoreCase("oracle") || jdbcEngine.equalsIgnoreCase("db2")) ? "true" : "false"));
		contents = contents.replaceAll("@database.driver.engine@", jdbcEngine);
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
		
		contents = contents.replaceAll("@databaseEngine@", jdbcEngine);
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
		
		System.out.println("contents after:" + contents);
		System.out.println("Want to write to:" + cmsFilePath);
		File targetFile = new File(cmsFilePath);
		System.out.println("targetFile:" + targetFile.exists());
		
		FileHelper.writeToFile(targetFile, contents, false);
		
		CacheController.clearCache("serverNodePropertiesCache");
		CmsPropertyHandler.initializeProperties();
		CmsPropertyHandler.resetHardCachedSettings();
		InfoGlueAuthenticationFilter.initializeCMSProperties();
		//END cms.properties

		//deliver.properties
		String deliverFilePath = CastorDatabaseService.class.getResource("/deliver.properties").getPath();
		System.out.println("deliverFilePath:" + deliverFilePath);
		String contentsDeliver = FileHelper.getStreamAsString(CastorDatabaseService.class.getResourceAsStream("/deliver.properties"));
		System.out.println("contentsDeliver:" + contentsDeliver);
		
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
		contentsDeliver = contentsDeliver.replaceAll("@operatingMode.deliver@", "0");

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
		
		contentsDeliver = contentsDeliver.replaceAll("@useShortTableNames@", ((jdbcEngine.equalsIgnoreCase("oracle") || jdbcEngine.equalsIgnoreCase("db2")) ? "true" : "false"));
		contentsDeliver = contentsDeliver.replaceAll("@database.driver.engine@", jdbcEngine);
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

		contentsDeliver = contentsDeliver.replaceAll("@databaseEngine@", jdbcEngine);
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

		System.out.println("contentsDeliver after:" + contentsDeliver);
		System.out.println("Want to write to:" + deliverFilePath);
		File targetFileDeliver = new File(deliverFilePath);
		System.out.println("targetFileDeliver:" + targetFileDeliver.exists());
		
		FileHelper.writeToFile(targetFileDeliver, contentsDeliver, false);
		
		CacheController.clearCache("serverNodePropertiesCache");
		CmsPropertyHandler.initializeProperties();
		CmsPropertyHandler.resetHardCachedSettings();
		InfoGlueAuthenticationFilter.initializeProperties();
		System.out.println("Operatingmode:" + CmsPropertyHandler.getOperatingMode());
		System.out.println("adminEmail:" + CmsPropertyHandler.getAdministratorEmail());
		//END deliver.properties

	}

	private String getJDBCEngine() throws Exception
	{
		String contents = FileHelper.getStreamAsString(CastorDatabaseService.class.getResourceAsStream("/database.xml"));
		System.out.println("contents:" + contents);
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

	private String getJDBCParamFromCastorXML(String xpath) throws Exception
	{
		String contents = FileHelper.getStreamAsString(CastorDatabaseService.class.getResourceAsStream("/database.xml"));
		System.out.println("contents:" + contents);
		DOMBuilder domBuilder = new DOMBuilder();
		Document doc = domBuilder.getDocument(contents);
		
		Element element = (Element)doc.selectSingleNode(xpath);
		if(element != null)
			return element.attributeValue("value");
			
		return "";
	}

	public void updateDatabase(String dbProvider, String dbName, String dbServer, String dbPort, String dbInstance, String createDatabase, String addExampleRepositories, String dbUser, String dbPassword, String igUser, String igPassword, HttpSession session) throws Exception 
	{
		if(dbProvider.equals("") || 
		   dbName.equals("") || 
		   dbServer.equals("") || 
		   dbPort.equals("") ||  
		   igUser.equals("") || 
		   igPassword.equals("") || 
		   (createDatabase.equalsIgnoreCase("true") && (
			dbUser.equals("") || 
		    dbPassword.equals(""))))
		{
			throw new Exception("Mandatory field(s) missing");
		}

		String castorProviderName = getCastorProviderName(dbProvider);
		String jdbcDriverName = getJDBCDriverName(dbProvider);
		String jdbcURL = getJDBCURL(dbProvider, dbName, dbServer, dbPort, dbInstance);
		String databaseMappingFile = getDatabaseMappingFile(dbProvider);
		
		if(!createDatabase.equalsIgnoreCase("true"))
			validateConnection(jdbcDriverName, jdbcURL, igUser, igPassword);

		System.out.println("createDatabase:" + createDatabase);
		if(createDatabase.equalsIgnoreCase("true"))
			createDatabaseAndUsers(jdbcDriverName, jdbcURL, dbProvider, dbName, dbServer, dbPort, dbInstance, dbUser, dbPassword, igUser, igPassword);

		session.setAttribute("install_dbProvider", dbProvider);
		session.setAttribute("install_jdbcDriverName", jdbcDriverName);
		session.setAttribute("install_jdbcURL", jdbcURL);
		session.setAttribute("install_dbUser", igUser);
		session.setAttribute("install_dbPassword", igPassword);
		
		//Castor database.xml
		String castorDatabaseXMLPath = CastorDatabaseService.class.getResource("/database.xml").getPath();
		System.out.println("castorDatabaseXMLPath:" + castorDatabaseXMLPath);
		String contents = FileHelper.getStreamAsString(CastorDatabaseService.class.getResourceAsStream("/database.xml"));
		System.out.println("contents:" + contents);
		
		contents = contents.replaceAll("@database.driver.engine@", castorProviderName);
		contents = contents.replaceAll("@database.driver.class@", jdbcDriverName);
		contents = contents.replaceAll("@database.user@", igUser);
		contents = contents.replaceAll("@database.password@", igPassword);
		contents = contents.replaceAll("@database.url@", jdbcURL);
		contents = contents.replaceAll("@database.validationQuery@", "select 1 from cmInfoGlueProperties");
		contents = contents.replaceAll("@database.maxConnections@", "300");
		contents = contents.replaceAll("@database.mapping@", databaseMappingFile);
		
		System.out.println("contents after:" + contents);
		System.out.println("Want to write to:" + castorDatabaseXMLPath);
		File targetFile = new File(castorDatabaseXMLPath);
		System.out.println("targetFile:" + targetFile.exists());
		
		FileHelper.writeToFile(new File(castorDatabaseXMLPath), contents, false);
		//END Castor database.xml

		//Hibernate config
		String hibernateXMLPath = CastorDatabaseService.class.getResource("/hibernate.cfg.xml").getPath();
		System.out.println("castorDatabaseXMLPath:" + castorDatabaseXMLPath);
		String hibernateConents = FileHelper.getStreamAsString(CastorDatabaseService.class.getResourceAsStream("/hibernate.cfg.xml"));
		System.out.println("hibernateConents:" + hibernateConents);
		
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
		
		System.out.println("hibernateConents after:" + hibernateConents);
		System.out.println("Want to write to:" + hibernateXMLPath);
		File hibernateTargetFile = new File(hibernateXMLPath);
		System.out.println("hibernateTargetFile:" + hibernateTargetFile.exists());
		
		FileHelper.writeToFile(new File(hibernateXMLPath), hibernateConents, false);
		//END hibernate
		
		//Hibernate config
		String propertySetXMLPath = CastorDatabaseService.class.getResource("/propertyset.xml").getPath();
		System.out.println("propertySetXMLPath:" + propertySetXMLPath);
		String propertysetContents = FileHelper.getStreamAsString(CastorDatabaseService.class.getResourceAsStream("/propertyset.xml"));
		System.out.println("propertysetContents:" + propertysetContents);
		
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
		
		System.out.println("propertysetContents after:" + propertysetContents);
		System.out.println("Want to write to:" + propertySetXMLPath);
		File propertySetTargetFile = new File(propertySetXMLPath);
		System.out.println("propertySetTargetFile:" + propertySetTargetFile.exists());
		
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

	private void createDatabaseAndUsers(String jdbcDriverName, String jdbcURL, String dbProvider, String dbName, String dbServer, String dbPort, String dbInstance, String dbUser, String dbPassword, String igUser, String igPassword) throws Exception 
	{
		if(dbProvider.equalsIgnoreCase("mysql"))
		{
			String mysqlJdbcURL = getJDBCURL(dbProvider, "mysql", dbServer, dbPort, dbInstance);

			validateConnection(jdbcDriverName, mysqlJdbcURL, dbUser, dbPassword);
			
			issueCommand(getConnection(jdbcDriverName, mysqlJdbcURL, dbUser, dbPassword), "CREATE DATABASE " + dbName + ";");
			createUsersMYSQL(jdbcDriverName, dbServer, dbPort, dbUser, dbPassword, dbName, igUser, igPassword);
			createTables(jdbcDriverName, dbServer, dbPort, dbUser, dbPassword, dbName, igUser, igPassword);
			createInitialData(jdbcDriverName, dbServer, dbPort, dbUser, dbPassword, dbName, igUser, igPassword);
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
			JDBCURL = "jdbc:jtds:sqlserver://" + dbServer + ":" + dbPort + ((dbInstance.equalsIgnoreCase("") ? ";DatabaseName=" + dbName + ";SelectMethod=Cursor" : ";INSTANCE=" + dbInstance + ";DatabaseName=" + dbName + ";SelectMethod=Cursor"));
			
		return JDBCURL;
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

		issueCommand(conn, "GRANT ALL PRIVILEGES ON `" + driverClass + "`.* TO '" + igUser + "'@'%' IDENTIFIED BY '" + igPassword + "';");
		issueCommand(conn, "GRANT ALL PRIVILEGES ON `" + driverClass + "`.* TO '" + igUser + "'@'localhost' IDENTIFIED BY '" + igPassword + "';");
		issueCommand(conn, "GRANT ALL PRIVILEGES ON `" + driverClass + "`.* TO '" + igUser + "'@'127.0.0.1' IDENTIFIED BY '" + igPassword + "';");
		issueCommand(conn, "GRANT ALL PRIVILEGES ON `" + driverClass + "`.* TO '" + igUser + "'@'" + databaseHostName + "' IDENTIFIED BY '" + igPassword + "';");
		issueCommand(conn, "GRANT ALL PRIVILEGES ON `" + driverClass + "`.* TO '" + igUser + "'@'" + getHostAddress() + "' IDENTIFIED BY '" + igPassword + "';");
	}

	private void createTables(String driverClass, String databaseHostName, String databasePortNumber, String dbUser, String dbPassword, String dbName, String igUser, String igPassword) throws Exception
	{
		String url = "jdbc:mysql://" + databaseHostName + ":" + databasePortNumber + "/" + dbName + "";
		Connection conn = getConnection(driverClass, url, igUser, igPassword);
		
		logger.warn("Setting up a new database....");		
		
		try
		{
			File coreSchemaFile = new File(CmsPropertyHandler.getSQLUpgradePath() + File.separator + "infoglue_core_schema_mysql.sql");
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
			
			StringTokenizer st = new StringTokenizer(script, ";");
		    while (st.hasMoreTokens()) 
		    {
		    	String command = st.nextToken();
		    	//Logger.logInfo("Command: " + command);
				issueCommand(conn, command + ";");
		    }
				
		}
		catch(Exception e)
		{
			logger.error("Problem running sql script:" + e.getMessage(), e);
			throw e;
		}
	}

	private void createInitialData(String driverClass, String databaseHostName, String databasePortNumber, String dbUser, String dbPassword, String dbName, String igUser, String igPassword) throws Exception
	{
		String url = "jdbc:mysql://" + databaseHostName + ":" + databasePortNumber + "/" + dbName + "";
		Connection conn = getConnection(driverClass, url, igUser, igPassword);
		
		try
		{
			File initialDataFile = new File(CmsPropertyHandler.getSQLUpgradePath() + File.separator + "infoglue_initial_data_mysql.sql");
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
			
			String[] commands = script.split("#endquery");
			logger.info("Parsed " + commands.length + " commands from script");
			
			for(int i=0; i<commands.length; i++)
			{
				String command = commands[i];
				if(command.indexOf("SPECIAL") > -1)
					issueSpecialCommand(conn, command.trim());		
				else
					issueCommand(conn, command.trim());
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
		String sql = getUpgradeScripts(dbVersion, session);
		
		StringTokenizer st = new StringTokenizer(sql, ";");
	    while (st.hasMoreTokens()) 
	    {
	    	String command = st.nextToken();
	    	//Logger.logInfo("Command: " + command);
			issueCommand(conn, command + ";");
	   	}
	}

	public Connection getConnection(String driverClass, String url, String userName, String password) throws Exception
	{
		Connection conn = null;
    
        // Load the JDBC driver
        System.out.println("Loading JDBC driver " + driverClass + "\n");
        Class.forName(driverClass).newInstance();
    	
        // Connect to the databse
        System.out.println("Connecting to database on " + url);
        conn = DriverManager.getConnection(url, userName, password);
        System.out.println("Connected...");
        
        return conn;
	}

	/**
	 * This method issues command to the db.
	 * @throws SQLException 
	 */
	
	private void issueCommand(Connection conn, String sql) throws SQLException
	{
		if(sql == null || sql.trim().length() == 0 || sql.trim().equalsIgnoreCase(";"))
			return;
			              
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


}