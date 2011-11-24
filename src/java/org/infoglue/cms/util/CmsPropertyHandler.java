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

package org.infoglue.cms.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.infoglue.cms.applications.managementtool.actions.deployment.DeploymentServerBean;
import org.infoglue.cms.applications.managementtool.actions.deployment.VersionControlServerBean;
import org.infoglue.cms.controllers.kernel.impl.simple.InstallationController;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.entities.management.ServerNodeVO;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.NullObject;
import org.infoglue.deliver.util.Timer;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;



/**
 * CMSPropertyHandler.java
 * Created on 2002-sep-12 
 * 
 * This class is used to get properties for the system in a transparent way.
 * The second evolution of this class made it possible for properties to be fetched from the propertyset if there instead. Fallback to file.
 * 
 * @author Stefan Sik, ss@frovi.com
 * @author Mattias Bogeblad 
 */

public class CmsPropertyHandler
{
    private final static Logger logger = Logger.getLogger(CmsPropertyHandler.class.getName());

	private static Properties cachedProperties 		= null;
	private static PropertySet propertySet			= null; 

	private static String serverNodeName			= null;
	
	private static String globalSettingsServerNodeId= "-1";
	private static String localSettingsServerNodeId	= null;
	
	private static String applicationName 			= null;
	private static String contextRootPath 			= null;
	private static String defaultScheme				= null;
	private static Integer defaultPort				= null;
	private static String operatingMode				= null;
	private static File propertyFile 				= null;
	private static Date startupTime					= null;
	private static String servletContext 			= null;
	
	//Variables which are caches very hard but must be cleared roughly
	private static String inputCharacterEncoding 			= null;
	private static String enforceRigidContentAccess 		= null;
	private static String niceURIEncoding					= null;
	private static String defaultNumberOfYearsBeforeExpire 	= null;
	private static String setDerivedLastModifiedInLive 		= null;
	private static String digitalAssetPath0					= null;
	private static String enableNiceURI						= null;
	private static String useImprovedContentCategorySearch	= null;
	private static String enablePortal						= null;
	private static String useAccessBasedProtocolRedirects	= null;
	private static Boolean useHashCodeInCaches 				= null;
	private static Boolean useSynchronizationOnCaches 		= null;
	   
	public static void setApplicationName(String theApplicationName)
	{
		CmsPropertyHandler.applicationName = theApplicationName;
	}

	public static void setContextRootPath(String contextRootPath)
	{
		CmsPropertyHandler.contextRootPath = contextRootPath;
	}

	public static void registerDefaultSchemeAndPort(HttpServletRequest request)
	{
		if(defaultScheme == null || defaultPort == null && request != null)
		{
			defaultScheme = request.getScheme();
			defaultPort = request.getLocalPort();
			logger.info("Registered defaultScheme:" + defaultScheme + " and defaultPort:" + defaultPort);
		}
	}
	
	public static String getServletContext()
	{
		return servletContext;
	}

	public static void setServletContext(String servletContext)
	{
		CmsPropertyHandler.servletContext = servletContext;
	}

	public static void setOperatingMode(String operatingMode)
	{
		CmsPropertyHandler.operatingMode = operatingMode;
	}

	public static String getApplicationName()
	{
		return applicationName;
	}

	public static void setStartupTime(Date startupTime)
	{
		CmsPropertyHandler.startupTime = startupTime;
	}

	public static Date getStartupTime()
	{
		return startupTime;
	}

	public static void setPropertyFile(File aPropertyFile)
	{
		propertyFile = aPropertyFile;
	}
	
	/**
	 * This method initializes the parameter hash with values.
	 */

	public static void initializeProperties()
	{
	    try
		{
	    	Timer timer = new Timer();
	    	timer.setActive(false);
	    	
			System.out.println("Initializing properties from file.....");
			//Thread.dumpStack();
			
			cachedProperties = new Properties();
			if(propertyFile != null)
			    cachedProperties.load(new FileInputStream(propertyFile));
			else
				cachedProperties.load(CmsPropertyHandler.class.getClassLoader().getResource("/" + applicationName + ".properties").openStream());
			    //cachedProperties.load(CmsPropertyHandler.class.getResourceAsStream("/" + applicationName + ".properties"));
			
			if(logger.isInfoEnabled())
				cachedProperties.list(System.out);
			
			Enumeration enumeration = cachedProperties.keys();
			while(enumeration.hasMoreElements())
			{
				String key = (String)enumeration.nextElement();
				if(key.indexOf("webwork.") > 0)
				{
					webwork.config.Configuration.set(key, cachedProperties.getProperty(key)); 
				}
			}
			
		    timer.printElapsedTime("Initializing properties from file took");

	        Map args = new HashMap();
		    args.put("globalKey", "infoglue");
		    try
		    {
		    	propertySet = PropertySetManager.getInstance("jdbc", args);
		    	//logger.info("propertySet:" + propertySet);
		    	//logger.info("propertySet.allowedAdminIP:" + propertySet.getString("allowedAdminIP"));
		    	if(logger.isInfoEnabled())
		    		logger.info("propertySet: " + propertySet);
		    }
		    catch(Exception e)
		    {
		    	propertySet = null;
		    	logger.error("Could not get property set: " + e.getMessage(), e);
		    }
		    
		    timer.printElapsedTime("Initializing properties from jdbc");

		    serverNodeName = cachedProperties.getProperty("serverNodeName");
		    
		    if(serverNodeName == null || serverNodeName.length() == 0)
		    {
		    	try
		    	{
				    InetAddress localhost = InetAddress.getLocalHost();
				    serverNodeName = localhost.getHostName();
		    	}
		    	catch(Exception e)
		    	{
		    		logger.error("Error initializing serverNodeName:" + e.getMessage());
		    	}
		    }
		    
		    logger.info("serverNodeName:" + serverNodeName);
		    
		    initializeLocalServerNodeId();
		    
		    timer.printElapsedTime("Initializing properties from local server jdbc");
		}	
		catch(Exception e)
		{
			cachedProperties = null;
			logger.error("Error loading properties from file " + "/" + applicationName + ".properties" + ". Reason:" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void resetHardCachedSettings()
	{
		logger.info("Resetting hard cached settings...");
		
		String newInputCharacterEncoding = getInputCharacterEncoding(SetCharacterEncodingFilter.defaultEncoding, true);
		String newEnforceRigidContentAccess = getEnforceRigidContentAccess(true);
		String newNiceURIEncodingg = getNiceURIEncoding(true);
		String newDefaultNumberOfYearsBeforeExpire = getDefaultNumberOfYearsBeforeExpire(true);
		String newSetDerivedLastModifiedInLive = getSetDerivedLastModifiedInLive(true);
		String newDigitalAssetPath0 = getDigitalAssetPath0(true);
		String newEnableNiceURI = getEnableNiceURI(true);
		String newUseImprovedContentCategorySearch = getUseImprovedContentCategorySearch(true);
		String newEnablePortal = getEnablePortal(true);
		String newUseAccessBasedProtocolRedirects = getUseAccessBasedProtocolRedirects(true);
		Boolean newUseHashCodeInCaches = getUseHashCodeInCaches(true);
		Boolean newUseSynchronizationOnCaches = getUseSynchronizationOnCaches(true);
		String newOperatingMode = getOperatingMode(true);
		
		inputCharacterEncoding 				= newInputCharacterEncoding;
		enforceRigidContentAccess 			= newEnforceRigidContentAccess;
		niceURIEncoding						= newNiceURIEncodingg;
		defaultNumberOfYearsBeforeExpire 	= newDefaultNumberOfYearsBeforeExpire;
		setDerivedLastModifiedInLive 		= newSetDerivedLastModifiedInLive;
		digitalAssetPath0					= newDigitalAssetPath0;
		enableNiceURI						= newEnableNiceURI;
		useImprovedContentCategorySearch	= newUseImprovedContentCategorySearch;
		enablePortal						= newEnablePortal;
		useAccessBasedProtocolRedirects 	= newUseAccessBasedProtocolRedirects;
		useHashCodeInCaches 				= newUseHashCodeInCaches;
		useSynchronizationOnCaches 			= newUseSynchronizationOnCaches;
		operatingMode 						= newOperatingMode;
		
		logger.info("Done resetting hard cached settings...");
	}
	
	public static String getServerName()
    {
    	String serverName = "Unknown";

    	try
    	{
		    InetAddress localhost = InetAddress.getLocalHost();
		    serverName = localhost.getHostName();
    	}
    	catch(Exception e)
    	{
    		
    	}
    	
	    return serverName;
    }

	/**
	 * This method gets the local server node id if available.
	 */

	public static void initializeLocalServerNodeId()
	{
        try
	    {
	        List serverNodeVOList = ServerNodeController.getController().getServerNodeVOList();
	        Iterator serverNodeVOListIterator = serverNodeVOList.iterator();
	        while(serverNodeVOListIterator.hasNext())
	        {
	            ServerNodeVO serverNodeVO = (ServerNodeVO)serverNodeVOListIterator.next();
	            if(serverNodeVO.getName().equalsIgnoreCase(serverNodeName))
	            {
	                localSettingsServerNodeId = serverNodeVO.getId().toString();
	                break;
	            }
	        }
	    }
	    catch(Exception e)
	    {
	        logger.warn("An error occurred trying to get localSettingsServerNodeId: " + e.getMessage(), e);
	    }
	    
	    System.out.println("localSettingsServerNodeId:" + localSettingsServerNodeId);
	}
	
	/**
	 * This method returns all properties .
	 */

	public static Properties getProperties()
	{
		if(cachedProperties == null)
			initializeProperties();
				
		return cachedProperties;
	}	

 
	/**
	 * This method returns a propertyValue corresponding to the key supplied.
	 */

	public static String getProperty(String key)
	{
		String value;
		if(cachedProperties == null)
			initializeProperties();
		
		value = cachedProperties.getProperty(key);
		if (value != null)
			value = value.trim();
				
		return value;
	}	

	/**
	 * This method returns a propertyValue corresponding to the key supplied.
	 */

	public static String getProperty(String key, String defaultValue)
	{
		String value;
		if(cachedProperties == null)
			initializeProperties();
		
		value = cachedProperties.getProperty(key);
		if (value != null)
			value = value.trim();
		else
			value = defaultValue;
				
		return value;
	}	
	
	/**
	 * This method sets a property during runtime.
	 */

	public static void setProperty(String key, String value)
	{
		if(cachedProperties == null)
			initializeProperties();
		
		cachedProperties.setProperty(key, value);
		
		CacheController.clearServerNodeProperty(false);
	}	

	public static String getServerNodeProperty(String key, boolean inherit)
	{
		return getServerNodeProperty(null, key, inherit, null);
	}

	public static String getServerNodeProperty(String key, boolean inherit, String defaultValue)
	{
		return getServerNodeProperty(null, key, inherit, defaultValue, false);
	}

	public static String getServerNodeProperty(String key, boolean inherit, String defaultValue, boolean skipCaches)
	{
		return getServerNodeProperty(null, key, inherit, defaultValue, skipCaches);
	}

	/**
	 * This method gets the serverNodeProperty but also fallbacks to the old propertyfile if not found in the propertyset.
	 * 
	 * @param key
	 * @param inherit
	 * @return
	 */
	public static String getServerNodeProperty(String prefix, String key, boolean inherit, String defaultValue)
	{
		return getServerNodeProperty(prefix, key, inherit, defaultValue, false);
	}
	
	public static String getServerNodeProperty(String prefix, String key, boolean inherit, String defaultValue, boolean skipCaches)
	{
		String value = null;
	    Object valueObject = null;
	    
	    StringBuffer sb = new StringBuffer();
	    sb.append(prefix).append("_").append(key).append("_").append(inherit);
	    
	    String cacheKey = sb.toString(); //"" + prefix + "_" + key + "_" + inherit;
        String cacheName = "serverNodePropertiesCache";
        
		if(logger.isInfoEnabled())
			logger.info("cacheKey:" + cacheKey);
		
		if(!skipCaches)
			valueObject = CacheController.getCachedObject(cacheName, cacheKey);
		
		if(valueObject != null)
		{
			if(valueObject instanceof NullObject)
				return null;
			else	
				return ((String)valueObject).trim();
		}
	    
		Timer timer = new Timer();
		
		if(logger.isInfoEnabled())
			logger.info("Getting jdbc-property:" + cacheKey);
		
		if(propertySet != null)
		{
			if(localSettingsServerNodeId != null)
		    {
		    	if(prefix != null)
		    	{
			        value = propertySet.getString("serverNode_" + localSettingsServerNodeId + "_" + prefix + "_" + key);
			        if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
			        {
			            value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + prefix + "_" + key);
				        if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
				        {
				            value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + key);
					    }
	
			        }
		    	}
		    	else
		    	{
			        value = propertySet.getString("serverNode_" + localSettingsServerNodeId + "_" + key);
			        if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
			        {
			            value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + key);
				    }	    		
		    	}
		    }
			else
			{
				if(prefix != null)
		    	{
					value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + prefix + "_" + key);
					if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
			        {
			            value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + key);
				    }	    		
		    	}
				else
				{
					value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + key);
				}
		    }
			
			if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
		    {
				value = propertySet.getString(key);
		    }
		}
		
	    if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
	    {
	        value = getProperty(key);
	    }

	    if((value == null || value.indexOf(key) > -1) && defaultValue != null)
	    	value = defaultValue;
	    
	    if(value != null)
	    	value = value.trim();
	    
	    if(!skipCaches)
	    {
		    if(value != null)
		    	CacheController.cacheObject(cacheName, cacheKey, value);
		    else
		    	CacheController.cacheObject(cacheName, cacheKey, new NullObject());
	    }

	    if(logger.isInfoEnabled())
			logger.info("Getting property " + cacheKey + " took:" + timer.getElapsedTime());
	    
	    return value;
	}


	public static String getServerNodeDataProperty(String prefix, String key, boolean inherit, String defaultValue)
	{
		return getServerNodeDataProperty(prefix, key, inherit, defaultValue, false);
	}

	/**
	 * This method gets the serverNodeDataProperty.
	 * 
	 * @param key
	 * @param inherit
	 * @return
	 */
	
	public static String getServerNodeDataProperty(String prefix, String key, boolean inherit, String defaultValue, boolean skipCache)
	{
	    String value = null;
	    
        String cacheKey = "" + prefix + "_" + key + "_" + inherit;
        String cacheName = "serverNodePropertiesCache";
        
        if(logger.isInfoEnabled())
    		logger.info("cacheKey:" + cacheKey);
		
        if(!skipCache)
        {
	        value = (String)CacheController.getCachedObject(cacheName, cacheKey);
			if(value != null)
			{
				return value;
			}
        }
        
		Timer timer = new Timer();

		if(logger.isInfoEnabled())
			logger.info("Getting jdbc-property:" + cacheKey);
	    
		if(localSettingsServerNodeId != null)
	    {
	    	if(prefix != null)
	    	{
		        value = getDataPropertyValue("serverNode_" + localSettingsServerNodeId + "_" + prefix + "_" + key);
		        if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
		        {
		            value = getDataPropertyValue("serverNode_" + globalSettingsServerNodeId + "_" + prefix + "_" + key);
			        if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
			        {
			            value = getDataPropertyValue("serverNode_" + globalSettingsServerNodeId + "_" + key);
			        }

		        }
	    	}
	    	else
	    	{
		        value = getDataPropertyValue("serverNode_" + localSettingsServerNodeId + "_" + key);
		        if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
		        {
		            value = getDataPropertyValue("serverNode_" + globalSettingsServerNodeId + "_" + key);
		        }	    		
	    	}
	    }
		else
		{
			if(prefix != null)
	    	{
				value = getDataPropertyValue("serverNode_" + globalSettingsServerNodeId + "_" + prefix + "_" + key);
	    	}
			else
			{
				value = getDataPropertyValue("serverNode_" + globalSettingsServerNodeId + "_" + key);
			}
	    }
		
	    if(value == null && defaultValue != null)
	    	value = defaultValue;
	    
	    //HŠr skall smartare lšsning in sen fšr att lšsa fallback.
	    if(!skipCache)
	    	CacheController.cacheObject(cacheName, cacheKey, value);
	    
	    if(logger.isInfoEnabled())
			logger.info("Getting property " + cacheKey + " took:" + timer.getElapsedTime());
	    
	    return value;
	}
	
	public static String getDataPropertyValue(String fullKey)
	{
		String result = null;
		
		try
		{
			byte[] valueBytes = getPropertySet().getData(fullKey);
	    
			result = (valueBytes != null ? new String(valueBytes, "utf-8") : "");
		}
		catch(Exception e)
		{
			logger.error("Error getting DataPropertyValue for " + fullKey + ":" + e.getMessage(), e);
		}
		
		return result;
	}
	
	private static void restorePropertySet()
	{
		Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    try
	    {
	    	propertySet = PropertySetManager.getInstance("jdbc", args);
	    	if(propertySet != null)
	    		logger.error("Restored propertyset ");
	    	else
	    		logger.error("Could not restore propertyset ");	
	    }
	    catch(Exception e)
	    {
	    	propertySet = null;
	    	logger.error("Could not get property set: " + e.getMessage(), e);
	    }
	}
	
	public static String getContextRootPath()
	{
	    return contextRootPath; //getProperty("contextRootPath"); Concurrency issues...
	}

	public static String getOperatingMode()
	{
		if(operatingMode == null)
			return getProperty("operatingMode");
		else
			return operatingMode; //getProperty("operatingMode"); Concurrency issues...
	}

	public static String getOperatingMode(boolean skipHardCache)
	{
		if(operatingMode == null || skipHardCache)
		{
			operatingMode = getProperty("operatingMode");
		}
		
		return operatingMode;
	}
	
	public static boolean getActuallyBlockOnBlockRequests()
	{
		if(!getOperatingMode().equalsIgnoreCase("3"))
			return true;
		else
			return false;
	}

	//TODO - refresh if changed....
	public static String getInputCharacterEncoding(String defaultEncoding)
	{
		return getInputCharacterEncoding(defaultEncoding, false);
	}
	
	public static String getInputCharacterEncoding(String defaultEncoding, boolean skipHardCache)
	{
		if(inputCharacterEncoding == null || skipHardCache)
		{
			String applicationName = CmsPropertyHandler.getApplicationName();
			String newInputCharacterEncoding = CmsPropertyHandler.getServerNodeProperty("inputCharacterEncoding", true, defaultEncoding);
			if(!applicationName.equalsIgnoreCase("cms"))
				newInputCharacterEncoding = CmsPropertyHandler.getServerNodeProperty("deliver", "inputCharacterEncoding", true, defaultEncoding);

			inputCharacterEncoding = newInputCharacterEncoding;
		}
		//return newInputCharacterEncoding;
		return inputCharacterEncoding;
	}
	
	public static String getUp2dateUrl()
	{
	    return getProperty("up2dateUrl");
	}

	public static String getURLComposerClass()
	{
	    return getProperty("URLComposerClass");
	}

	public static String getMaxClients()
	{
	    return getProperty("maxClients");
	}

	public static String getAllowedDirectLoginNames()
	{
	    return getServerNodeProperty("allowedDirectLoginNames", true, "eventPublisher");
	}
	
	public static String getAdministratorUserName()
	{
	    return getProperty("administratorUserName");
	}
	/*
	public static String getAdministratorPassword()
	{
	    return getProperty("administratorPassword");
	}
	*/

	public static boolean getMatchesAdministratorPassword(String password)
	{
		try
		{
			if(CmsPropertyHandler.getUsePasswordEncryption())
			{
				byte[] encryptedPassRaw = DigestUtils.sha(password);
				String encryptedPass = new String(new Base64().encode(encryptedPassRaw), "ASCII");
				password = encryptedPass;
			}
			
			String administratorPassword = getProperty("administratorPassword");
		    if(administratorPassword != null)
		    	return administratorPassword.equals(password);
		    else
		    	return false;
		}
		catch (Exception e) 
		{
			logger.error("There was an error when we encrypted the password:" + e.getMessage(), e);
			return false;
		}
	}

	public static boolean getUsePasswordEncryption()
	{
		boolean usePasswordEncryption = false;
		try
		{
			usePasswordEncryption = Boolean.parseBoolean(getServerNodeProperty("usePasswordEncryption", true, "false"));
		}
		catch(Exception e)
		{
			logger.warn("Error parsing usePasswordEncryption:" + e.getMessage());
		}
		
		return usePasswordEncryption;
	}

	public static String getAdministratorEmail()
	{
	    return getProperty("administratorEmail");
	}

	public static String getDbRelease()
	{
	    return getProperty("dbRelease", "");
	}

	public static String getDbUser()
	{
	    return getProperty("dbUser", "");
	}

	public static String getDbPassword()
	{
	    return getProperty("dbPassword", "");
	}

	public static String getMasterServer()
	{
		return getServerNodeProperty("masterServer", true, "");
	}
   
	public static String getSlaveServer()
	{
		return getServerNodeProperty("slaveServer", true, "");
	}
	   
	public static String getBuildName()
	{
	    return getProperty("buildName", "");
	}
	
	public static String getAdminToolsPath()
	{
	    return getProperty("adminToolsPath", "");
	}
	
	public static String getDbScriptPath()
	{
	    return getProperty("dbScriptPath", "");
	}

	public static String getDigitalAssetUploadPath()
	{
	    return getServerNodeProperty("digitalAssetUploadPath", true, "" + getContextRootPath() + "uploads");
	}

	public static String getDisableAssetDeletionInWorkThread()
	{
	    return getServerNodeProperty("disableAssetDeletionInWorkThread", true, "false");
	}

	public static String getDisableAssetDeletionInLiveThread()
	{
	    return getServerNodeProperty("getDisableAssetDeletionInLiveThread", true, "false");
	}

	public static String getExtranetCookieTimeout()
	{
	    return getServerNodeProperty("extranetCookieTimeout", true, "1800");
	}
	
	public static String getWebServicesBaseUrl()
	{
		String webServicesBaseUrl =  getServerNodeProperty("webServicesBaseUrl", true, "" + getCmsFullBaseUrl() + "/services/");
		if(webServicesBaseUrl != null && !webServicesBaseUrl.startsWith("http"))
		{
			webServicesBaseUrl = "" + defaultScheme + "://127.0.0.1:" + defaultPort + "" + webServicesBaseUrl;
			System.out.println("Rewriting webServicesBaseUrl - now:" + webServicesBaseUrl);
		}
		return webServicesBaseUrl;
	}
	
	public static String getLivePublicationThreadClass() 
	{
		return getServerNodeProperty("livePublicationThreadClass", true, "org.infoglue.deliver.util.SelectiveLivePublicationThread");
	}

	public static String getPublicationThreadDelay()
	{
	    return getServerNodeProperty("publicationThreadDelay", true, "5000");
	}

	public static String getPathsToRecacheOnPublishing()
	{
	    return getServerNodeProperty("pathsToRecacheOnPublishing", true);
	}

	public static String getDisableTemplateDebug()
	{
	    return getServerNodeProperty("disableTemplateDebug", true, "false");
	}

	public static String getTree()
	{
	    return getServerNodeProperty("tree", true, "html");
	}

	public static String getTreeMode()
	{
	    return getServerNodeProperty("treemode", true, "dynamic");
	}

	public static String getDisableCustomIcons()
	{
	    return getServerNodeProperty("disableCustomIcons", true, "true");
	}

	public static String getWorkingStyleInformation()
	{
	    return getServerNodeProperty("workingStyleInformation", true, "color: #007900");
	}

	public static String getFinalStyleInformation()
	{
	    return getServerNodeProperty("finalStyleInformation", true, "color: #baaf00");
	}

	public static String getPublishStyleInformation()
	{
	    return getServerNodeProperty("publishStyleInformation", true, "color: #ba0000");
	}

	public static String getPublishedStyleInformation()
	{
	    return getServerNodeProperty("publishedStyleInformation", true, "");
	}
	
	public static String getPreviewDeliveryUrl()
	{
		String previewDeliveryUrl = getServerNodeProperty("previewDeliveryUrl", true);
		return previewDeliveryUrl;
	}

	public static String getStagingDeliveryUrl()
	{
	    return getServerNodeProperty("stagingDeliveryUrl", true);
	}

	public static String getEditionPageSize()
	{
		return getServerNodeProperty("edition.pageSize", true, "10");
	}
	
	public static String getContentTreeSort()
	{
		return getServerNodeProperty("content.tree.sort", true, "name");
	}

	public static String getStructureTreeSort()
	{
		return getServerNodeProperty("structure.tree.sort", true, "name");
	}

	public static String getStructureTreeIsHiddenProperty()
	{
		return getServerNodeProperty("structure.tree.isHidden", true);
	}

	public static String getUseAccessRightsOnContentTree()
	{
		return getServerNodeProperty("content.tree.hideForbidden", true, "false");
	}

	public static String getUseAccessRightsOnStructureTree()
	{
		return getServerNodeProperty("structure.tree.hideForbidden", true, "false");
	}

	public static String getEnforceRigidContentAccess()
	{
		return getEnforceRigidContentAccess(false);
	}
	
	public static String getEnforceRigidContentAccess(boolean skipHardCache)
	{
		if(enforceRigidContentAccess == null || skipHardCache)
		{
			enforceRigidContentAccess = getServerNodeProperty("enforceRigidContentAccess", true, "false");
		}
		
		return enforceRigidContentAccess;
	}

	public static String getDisableEmptyUrls()
	{
		return getServerNodeProperty("disableEmptyUrls", true, "yes");
	}

	public static String getCacheUpdateAction()
	{
		return getServerNodeProperty("cacheUpdateAction", true, "UpdateCache.action");
	}

	public static String getLogPath()
	{
		return getServerNodeProperty("logPath", true, "" + contextRootPath + "logs" + File.separator + applicationName + ".log");
	}

	public static String getLogTransactions()
	{
		return getServerNodeProperty("logTransactions", true, "false");
	}
	
	public static String getEnableExtranetCookies()
	{
		return getServerNodeProperty("enableExtranetCookies", true, "false");
	}
	
	public static String getUseAlternativeBrowserLanguageCheck()
	{
		return getServerNodeProperty("useAlternativeBrowserLanguageCheck", true, "false");
	}
	
	public static String getCaseSensitiveRedirects()
	{
		return getServerNodeProperty("caseSensitiveRedirects", true, "false");
	}
	
	public static String getUseDNSNameInURI()
	{
		return getServerNodeProperty("useDNSNameInURI", true, "false");
	}
    
	public static String getWysiwygEditor()
	{
		return getServerNodeProperty("wysiwygEditor", true, "ckeditor3");
	}

	public static String getDisableImageEditor()
	{
		return getServerNodeProperty("disableImageEditor", true, "false");
	}
	
	public static String getProtectContentTypes()
	{
		return getServerNodeProperty("protectContentTypes", true, "false");
	}

	public static String getProtectWorkflows()
	{
		return getServerNodeProperty("protectWorkflows", true, "false");
	}

	public static String getProtectCategories()
	{
		return getServerNodeProperty("protectCategories", true, "false");
	}

	public static String getHideProtectedProperties()
	{
		return getServerNodeProperty("hideProtectedProperties", true, "true");
	}

	public static String getInternalSearchEngine()
	{
		return getServerNodeProperty("internalSearchEngine", true, "sqlSearch");
	}

	public static String getMaxRows()
	{
		return getServerNodeProperty("maxRows", true, "100");
	}

	public static Integer getMaxNumberOfAssetInSearches()
	{
		Integer maxNumberOfAssetInSearches = 50;
		String maxNumberOfAssetInSearchesString = getServerNodeProperty("maxNumberOfAssetInSearches", true, "50");
		try
		{
			maxNumberOfAssetInSearches = new Integer(maxNumberOfAssetInSearchesString);
		}
		catch (Exception e) 
		{
			logger.warn("Problem parsing maxNumberOfAssetInSearches:" + e.getMessage());
		}
		
		return maxNumberOfAssetInSearches;
	}

	public static String getComponentBindningAssetBrowser()
	{
		return getServerNodeProperty("componentBindningAssetBrowser", true, "default");
	}

	public static String getPrefferedWYSIWYG()
	{
		return getServerNodeProperty("prefferedWYSIWYG", true, "fckeditor2");
	}

	public static String getDefaultNumberOfYearsBeforeExpire()
	{
		return getDefaultNumberOfYearsBeforeExpire(false);
	}
	
	public static String getDefaultNumberOfYearsBeforeExpire(boolean skipHardCache)
	{
		if(defaultNumberOfYearsBeforeExpire == null || skipHardCache)
		{
			defaultNumberOfYearsBeforeExpire = getServerNodeProperty("defaultNumberOfYearsBeforeExpire", true, "50");
		}
		
		return defaultNumberOfYearsBeforeExpire;
	}

	public static String getEnableDateTimeDirectEditing()
	{
		return getServerNodeProperty("enableDateTimeDirectEditing", true, "false");
	}

	public static String getShowContentVersionFirst()
	{
		return getServerNodeProperty("showContentVersionFirst", true, "true");
	}
	
	public static String getShowComponentsFirst()
	{
		return getServerNodeProperty("showComponentsFirst", true, "true");
	}
	
	public static String getDecoratedPageInvoker()
	{
		return getServerNodeProperty("decoratedPageInvoker", true, "simple");
	}

	public static String getShowAllWorkflows()
	{
		return getServerNodeProperty("showAllWorkflows", true, "true");
	}
	
	public static String getIsPageCacheOn()
	{
	    return getServerNodeProperty("isPageCacheOn", true, "true");
	}

	public static String getEditOnSite()
	{
	    return getServerNodeProperty("editOnSite", true, "true");
	}

	public static String getUseSelectivePageCacheUpdate()
	{
	    return getServerNodeProperty("useSelectivePageCacheUpdate", true, "true");
	}

	public static String getExpireCacheAutomatically()
	{
	    return getServerNodeProperty("expireCacheAutomatically", true, "false");
	}

	public static String getCacheExpireInterval()
	{
	    return getServerNodeProperty("cacheExpireInterval", true, "1800");
	}

	public static String getDeliverRequestTimeout()
	{
	    return getServerNodeProperty("deliverRequestTimeout", true, "60000");
	}

	public static String getLiveDeliverRequestTimeout()
	{
	    return getServerNodeProperty("liveDeliverRequestTimeout", true, "60000");
	}

	public static boolean getKillLiveRequestWhichTimedout()
	{
		boolean killLiveRequestWhichTimedout = false;
		try
		{
			killLiveRequestWhichTimedout = Boolean.parseBoolean(getServerNodeProperty("killLiveRequestWhichTimedout", true, "false"));
		}
		catch(Exception e)
		{
			logger.warn("Error parsing killLiveRequestWhichTimedout:" + e.getMessage());
		}
		
		return killLiveRequestWhichTimedout;
	}

	public static Boolean getOnlyAllowFolderType()
	{
		boolean onlyAllowFolderType = true;
		try
		{
			onlyAllowFolderType = Boolean.parseBoolean(getServerNodeProperty("onlyAllowFolderType", true, "true"));
		}
		catch(Exception e)
		{
			logger.warn("Error parsing onlyAllowFolderType:" + e.getMessage());
		}
		
		return onlyAllowFolderType;
	}

	public static Boolean getSkipResultDialogIfPossible()
	{
		boolean skipResultDialogIfPossible = true;
		try
		{
			skipResultDialogIfPossible = Boolean.parseBoolean(getServerNodeProperty("skipResultDialogIfPossible", true, "true"));
		}
		catch(Exception e)
		{
			logger.warn("Error parsing skipResultDialogIfPossible:" + e.getMessage());
		}
		
		return skipResultDialogIfPossible;
	}

	public static Boolean getUseHashCodeInCaches()
	{
		return getUseHashCodeInCaches(false);
	}
	
	public static Boolean getUseHashCodeInCaches(boolean skipHardCache)
	{
		if(useHashCodeInCaches == null || skipHardCache)
		{
			boolean newUseHashCodeInCaches = false;
			try
			{
				newUseHashCodeInCaches = Boolean.parseBoolean(getServerNodeProperty("useHashCodeInCaches", true, "false", true));
			}
			catch(Exception e)
			{
				logger.warn("Error parsing useHashCodeInCaches:" + e.getMessage());
			}
			useHashCodeInCaches = newUseHashCodeInCaches;
		}
		
		return useHashCodeInCaches;
	}

	public static Boolean getUseSynchronizationOnCaches()
	{
		return getUseSynchronizationOnCaches(false);
	}
	
	public static Boolean getUseSynchronizationOnCaches(boolean skipHardCache)
	{
		if(useSynchronizationOnCaches == null || skipHardCache)
		{
			boolean newUseSynchronizationOnCaches = false;
			try
			{
				newUseSynchronizationOnCaches = Boolean.parseBoolean(getServerNodeProperty("useSynchronizationOnCaches", true, "true", true));
			}
			catch(Exception e)
			{
				logger.warn("Error parsing useSynchronizationOnCaches:" + e.getMessage());
			}
			
			logger.info("Slow query for newUseSynchronizationOnCaches:" + newUseSynchronizationOnCaches);
			useSynchronizationOnCaches = newUseSynchronizationOnCaches;
		}

		return useSynchronizationOnCaches;
	}

	public static String getUseHighLoadLimiter()
	{
	    return getServerNodeProperty("useHighLoadLimiter", true, "false");
	}

	public static String getMaxActiveRequests()
	{
	    return getServerNodeProperty("maxActiveRequests", true, "50");
	}

	public static String getMaxRequestTime()
	{
	    return getServerNodeProperty("maxRequestTime", true, "5000");
	}

	public static String getSessionTimeout()
	{
	    return getServerNodeProperty("session.timeout", true, "1800");
	}

	public static String getCompressPageCache()
	{
	    return getServerNodeProperty("compressPageCache", true, "true");
	}

	public static String getCompressPageResponse()
	{
	    return getServerNodeProperty("compressPageResponse", true, "true");
	}

	public static String getSiteNodesToRecacheOnPublishing()
	{
	    return getServerNodeProperty("siteNodesToRecacheOnPublishing", true, "");
	}

	public static String getRecachePublishingMethod()
	{
	    return getServerNodeProperty("recachePublishingMethod", true, "");
	}

	public static String getRecacheUrl()
	{
	    return getServerNodeProperty("recacheUrl", true, "");
	}

	public static String getUseUpdateSecurity()
	{
	    return getServerNodeProperty("useUpdateSecurity", true, "true");
	}
	
	public static String getAllowedAdminIP()
	{
	    return getServerNodeProperty("allowedAdminIP", true, "");
	}

	public static boolean getAllowXForwardedIPCheck()
	{
	    String allowXForwardedIPCheck = getServerNodeProperty("allowXForwardedIPCheck", true, "false");

	    return Boolean.parseBoolean(allowXForwardedIPCheck);
	}
	
	
	public static String getPageKey()
	{
	    return getServerNodeProperty("pageKey", true, "");
	}

	public static String getComponentKey()
	{
	    return getServerNodeProperty("componentKey", true, "");
	}

	public static String getCmsFullBaseUrl()
	{
		String cmsFullBaseUrl = getServerNodeProperty("cmsFullBaseUrl", true);
		if(cmsFullBaseUrl == null || cmsFullBaseUrl.equals(""))
		{
			cmsFullBaseUrl = "" + defaultScheme + "://127.0.0.1:" + defaultPort + "" + getCmsBaseUrl();
		}
		else if(cmsFullBaseUrl != null && !cmsFullBaseUrl.startsWith("http"))
		{
			cmsFullBaseUrl = "" + defaultScheme + "://127.0.0.1:" + defaultPort + "" + getCmsBaseUrl();
		}
		
		return cmsFullBaseUrl;
	}

	public static String getCmsBaseUrl()
	{
	    return getServerNodeProperty("cmsBaseUrl", true, "");
	}

	public static String getComponentEditorUrl()
	{
	    return getServerNodeProperty("componentEditorUrl", true, "");
	}

	public static String getComponentRendererUrl()
	{
	    return getServerNodeProperty("componentRendererUrl", true, "");
	}

	public static String getComponentRendererAction()
	{
	    return getServerNodeProperty("componentRendererAction", true, "ViewPage!renderDecoratedPage.action");
	}

	public static String getEditOnSiteUrl()
	{
	    return getServerNodeProperty("editOnSiteUrl", true, "");
	}

	public static String getUseFreeMarker()
	{
	    return getServerNodeProperty("useFreeMarker", true, "false");
	}

	public static String getWebServerAddress()
	{
	    return getServerNodeProperty("webServerAddress", true, "");
	}

	public static String getApplicationBaseAction()
	{
	    return getServerNodeProperty("applicationBaseAction", true, "");
	}

	public static String getDigitalAssetBaseUrl()
	{
	    return getServerNodeProperty("digitalAssetBaseUrl", true, "");
	}

	public static String getImagesBaseUrl()
	{
	    return getServerNodeProperty("imagesBaseUrl", true, "");
	}

	public static String getDigitalAssetPath()
	{
	    return getServerNodeProperty("digitalAssetPath", true, "" + contextRootPath + "digitalAssets");
	}

	public static String getDigitalAssetPath0()
	{
		return getDigitalAssetPath0(false);
	}
	
	public static String getDigitalAssetPath0(boolean skipHardCache)
	{
		if(digitalAssetPath0 == null || skipHardCache)
		{
			digitalAssetPath0 = getServerNodeProperty("digitalAssetPath.0", true, "" + contextRootPath + "digitalAssets");
		}
		
	    return digitalAssetPath0;
	}

	public static String getEnableDiskAssets()
	{
	    return getServerNodeProperty("enableDiskAssets", true, "false");
	}

	public static String getEnableNiceURI()
	{
		return getEnableNiceURI(false);
	}
	
	public static String getEnableNiceURI(boolean skipHardCache)
	{
		if(enableNiceURI == null || skipHardCache)
		{
			//if(!skipHardCache)
			//	System.out.println("New value read and not because it was a recache");
			String value = null;
			if(!getOperatingMode().equals("3"))
				value = getServerNodeProperty("enableNiceURIInWorking", true, "true");
			else
				value = getServerNodeProperty("enableNiceURI", true, "true");
		
			enableNiceURI = value;
		}
		
		return enableNiceURI;
	}

	public static String getNiceURIDisableNiceURIForContent()
	{
		return getServerNodeProperty("niceURIDisableNiceURIForContent", true, "false");
	}

	public static String getEnableNiceURIForLanguage()
	{
		return getServerNodeProperty("enableNiceURIForLanguage", true, "false");
	}

	public static String getNiceURIEncoding()
	{
		return getNiceURIEncoding(false);
	}
	
	public static String getNiceURIEncoding(boolean skipHardCache)
	{
		if(niceURIEncoding == null || skipHardCache)
		{
			niceURIEncoding = getServerNodeProperty("niceURIEncoding", true, "UTF-8");
		}
		
	    return niceURIEncoding;
	}

	public static String getNiceURIAttributeName()
	{
	    return getServerNodeProperty("niceURIAttributeName", true, "NiceURIName");
	}

	public static String getRequestArgumentDelimiter()
	{
	    return getServerNodeProperty("requestArgumentDelimiter", true, "&amp;");
	}

	public static String getErrorUrl()
	{
	    return getServerNodeProperty("errorUrl", true);
	}

	public static String getErrorBusyUrl()
	{
	    return getServerNodeProperty("errorBusyUrl", true);
	}

	public static String getExternalThumbnailGeneration()
	{
	    return getServerNodeProperty("externalThumbnailGeneration", true);
	}

	public static String getURIEncoding()
	{
	    return getServerNodeProperty("URIEncoding", true, "UTF-8");
	}

	public static String getWorkflowEncoding()
	{
	    return getServerNodeProperty("workflowEncoding", true, "UTF-8");
	}

	public static String getFormsEncoding()
	{
	    return getServerNodeProperty("formsEncoding", true, "UTF-8");
	}

	public static String getUploadFromEncoding()
	{
		return getServerNodeProperty("uploadFromEncoding", true, "ISO-8859-1");
	}

	public static String getUploadToEncoding()
	{
		return getServerNodeProperty("uploadToEncoding", true, "UTF-8");
	}

	public static String getAssetKeyFromEncoding()
	{
		return getServerNodeProperty("assetKeyFromEncoding", true, "ISO-8859-1");
	}

	public static String getAssetKeyToEncoding()
	{
		return getServerNodeProperty("assetKeyToEncoding", true, "UTF-8");
	}

	public static String getUseShortTableNames()
	{
	    return getServerNodeProperty("useShortTableNames", true, "false");
	}

	public static String getUseImprovedContentCategorySearch()
	{
		return getUseImprovedContentCategorySearch(false);
	}
	
	public static String getUseImprovedContentCategorySearch(boolean skipHardCache)
	{
		if(useImprovedContentCategorySearch == null || skipHardCache)
		{
			useImprovedContentCategorySearch = getServerNodeProperty("useImprovedContentCategorySearch", true, "true");
		}
		
	    return useImprovedContentCategorySearch;
	}

	public static String getDatabaseEngine()
	{
	    return getServerNodeProperty("databaseEngine", true, null);
	}

	public static String getInfoGlueVersion()
	{
	    return getServerNodeProperty("infoGlueVersion", true, "3.0.0.0 RC 2");
	}

	public static String getInfoGlueDBVersion()
	{
	    return getServerNodeProperty("infoGlueDBVersion", true, "3.0");
	}

	public static String getInfoGlueVersionReleaseDate()
	{
	    return getServerNodeProperty("infoGlueVersionReleaseDate", true, "2011-08-11");
	}

	public static String getLogDatabaseMessages()
	{
	    return getServerNodeProperty("logDatabaseMessages", true, "false");
	}

	public static String getStatisticsEnabled()
	{
	    return getServerNodeProperty("statistics.enabled", true, "false");
	}

	public static String getStatisticsLogPath()
	{
	    return getServerNodeProperty("statisticsLogPath", true, "" + contextRootPath + "logs");
	}

	public static String getStatisticsLogOneFilePerDay()
	{
	    return getServerNodeProperty("statisticsLogOneFilePerDay", true, "false");
	}

	public static String getStatisticsLogger()
	{
	    return getServerNodeProperty("statisticsLogger", true, "W3CExtendedLogger");
	}

	public static String getEnablePortal()
	{
		return getEnablePortal(false);
	}
	
	public static String getEnablePortal(boolean skipHardCache)
	{
		if(enablePortal == null || skipHardCache)
		{
			enablePortal = getServerNodeProperty("enablePortal", true, "true");
		}
		
	    return enablePortal;
	}

	public static String getPortletBase()
	{
	    return getServerNodeProperty("portletBase", true);
	}

	public static String getMailSmtpHost()
	{
	    return getServerNodeProperty("mail.smtp.host", true);
	}

	public static String getMailSmtpPort()
	{
	    return getServerNodeProperty("mail.smtp.port", true);
	}

	public static String getMailSmtpAuth()
	{
	    return getServerNodeProperty("mail.smtp.auth", true);
	}

	public static String getMailSmtpUser()
	{
	    return getServerNodeProperty("mail.smtp.user", true);
	}

	public static String getMailSmtpPassword()
	{
	    return getServerNodeProperty("mail.smtp.password", true);
	}

	public static String getMailContentType()
	{
	    return getServerNodeProperty("mail.contentType", true, "text/html");
	}
	
	public static String getSystemEmailSender()
	{
	    return getServerNodeProperty("systemEmailSender", true);
	}

	public static String getWarningEmailReceiver()
	{
	    return getServerNodeProperty("warningEmailReceiver", true);
	}

	public static String getEmailRecipientLimit()
	{
	    return getServerNodeProperty("emailRecipientLimit", true, "-1");
	}

	public static String getExportFormat()
	{
	    return getServerNodeProperty("exportFormat", true, "2");
	}

	public static String getHelpUrl()
	{
	    return getServerNodeProperty("helpUrl", true);
	}

	public static String getHeaderHTML()
	{
	    return getServerNodeDataProperty(null, "headerHTML", true, "");
	}

	public static String getProtectDeliverWorking()
	{
	    return getServerNodeProperty("protectDeliverWorking", true, "false");
	}

	public static String getProtectDeliverPreview()
	{
	    return getServerNodeProperty("protectDeliverPreview", true, "false");
	}

	public static String getForceIdentityCheck()
	{
		return getServerNodeProperty("forceIdentityCheck", true, "false");
	}

	public static String getNumberOfVersionsToKeepDuringClean()
	{
		return getServerNodeProperty("numberOfVersionsToKeepDuringClean", true, "-1");
	}

	public static String getKeepOnlyOldPublishedVersionsDuringClean()
	{
		return getServerNodeProperty("keepOnlyOldPublishedVersionsDuringClean", true, "true");
	}

	public static Long getMinimumTimeBetweenVersionsDuringClean()
	{
		long value = -1;
		
		String minimumTimeBetweenVersionsDuringClean = getServerNodeProperty("minimumTimeBetweenVersionsDuringClean", true, "-1");
		try
		{
			value = new Long(minimumTimeBetweenVersionsDuringClean).longValue();
		}
		catch (Exception e) 
		{
			logger.warn("Problem getting minimumTimeBetweenVersionsDuringClean:" + e.getMessage());
		}
		
		return value;
	}

	public static String getAssetUploadTransformationsSettings()
	{
		return getServerNodeDataProperty(null, "assetUploadTransformationsSettings", true, "");
	}

	public static String getAllowPublicationEventFilter()
	{
	    return getServerNodeProperty("allowPublicationEventFilter", true, "false");
	}

	public static String getDefaultPublicationEventFilter()
	{
	    return getServerNodeProperty("defaultPublicationEventFilter", true, "all");
	}

	public static String getPreferredLanguageCode(String userName)
	{
        return getPropertySet().getString("principal_" + userName + "_languageCode");
	}

	public static String getPreferredToolName(String userName)
	{
		String preferredToolName = getPropertySet().getString("principal_" + userName + "_defaultToolName");
	    if(preferredToolName == null || preferredToolName.equals(""))
	    	preferredToolName = "StructureTool";

	    return preferredToolName;
	}

	public static String getDefaultGUI(String userName)
	{
		String defaultGUI = getPropertySet().getString("principal_" + userName + "_defaultGUI");
	    
		return (defaultGUI == null ? "default" : defaultGUI);
	}

	public static String getToolbarVariant(String userName)
	{
		String toolbarVariant = getPropertySet().getString("principal_" + userName + "_toolbarVariant");
	    
		return (toolbarVariant == null ? "compact" : toolbarVariant);
	}
	
	public static String getTheme(String userName)
	{
		String theme = getPropertySet().getString("principal_" + userName + "_theme");

		return (theme == null ? "" : theme);
	}

	public static String getPreferredRepositoryId(String userName)
	{
	    return getPropertySet().getString("principal_" + userName + "_defaultRepositoryId");
	}

	public static String getPropertiesParser()
	{
	    return getServerNodeProperty("propertiesParser", false, "xpp3");
	}

	public static String getAllowCrossSiteSubmitToPublish()
	{
        return getServerNodeProperty("allowCrossSiteSubmitToPublish", true, "false");
	}

	public static String getEncodeValidateUrl()
	{
        return getServerNodeProperty("encodeValidateUrl", true, "false");
	}

	public static String getEncodeCasServiceUrl()
	{
        return getServerNodeProperty("encodeCasServiceUrl", true, "true");
	}

	public static String getSetDerivedLastModifiedInLive()
	{
		return getSetDerivedLastModifiedInLive(false);
	}
	
	public static String getSetDerivedLastModifiedInLive(boolean skipHardCache)
	{
		if(setDerivedLastModifiedInLive == null || skipHardCache)
		{
			setDerivedLastModifiedInLive = getServerNodeProperty("setDerivedLastModifiedInLive", true, "true");
		}
		
		return setDerivedLastModifiedInLive;
	}

	public static Integer getMaxNumberOfVersionsForDerivedLastModifiedInLive()
	{
		Integer max = 50;
		try
		{
			max = new Integer(getServerNodeProperty("maxNumberOfVersionsForDerivedLastModifiedInLive", true, "50"));
		}
		catch (Exception e) 
		{
			logger.error("Problem parsing maxNumberOfVersionsForDerivedLastModifiedInLive:" + e.getMessage());
		}
		return max;
	}

	public static boolean getDuplicateAssetsBetweenVersions()
	{
		String duplicateAssetsBetweenVersions = getServerNodeProperty("duplicateAssetsBetweenVersions", true, "false");
		
		return Boolean.parseBoolean(duplicateAssetsBetweenVersions);
	}

	public static String getWYSIWYGToolbarComboPreviewCSS()
	{
		String WYSIWYGToolbarComboPreviewCSS = getServerNodeDataProperty(null, "WYSIWYGToolbarComboPreviewCSS", true, "p, div, h1, h2, h3, h4, h5, h6, pre, address { font-family: Verdana, Arial, Sans serif; font-size: 10px; font-style: normal; font-weight: normal; }");
		if(WYSIWYGToolbarComboPreviewCSS == null || WYSIWYGToolbarComboPreviewCSS.equals(""))
			WYSIWYGToolbarComboPreviewCSS = "p, div, h1, h2, h3, h4, h5, h6, pre, address { font-family: Verdana, Arial, Sans serif; font-size: 10px; font-style: normal; font-weight: normal; }";

		return WYSIWYGToolbarComboPreviewCSS;
	}

	public static String getWYSIWYGEditorAreaCSS()
	{
		return getServerNodeDataProperty(null, "WYSIWYGEditorAreaCSS", true, "");
	}

	public static List getInternalDeliveryUrls()
	{
		List urls = new ArrayList();
		
		InetAddress localAddress = null;
		try
		{
			localAddress = InetAddress.getLocalHost();
			logger.info("localAddress2:" + localAddress.getHostAddress());
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	    String internalDeliverUrlsString = CmsPropertyHandler.getServerNodeDataProperty(null, "internalDeliveryUrls", true, null);
	    if(internalDeliverUrlsString != null && !internalDeliverUrlsString.equals(""))
		{
	    	try
			{
	    		Properties properties = new Properties();
				properties.load(new ByteArrayInputStream(internalDeliverUrlsString.getBytes("UTF-8")));

				int i = 0;
				String deliverUrl = null;
				while((deliverUrl = properties.getProperty("" + i)) != null)
				{ 
					logger.info("deliverUrl:" + deliverUrl);
					if(deliverUrl.startsWith("unless["))
					{
						String unlessIP = deliverUrl.substring(7, deliverUrl.indexOf("]"));
						logger.info("unlessIP:" + unlessIP);
						if(localAddress == null || !localAddress.getHostAddress().equals(unlessIP))
						{
							urls.add(deliverUrl.substring(deliverUrl.indexOf("=") + 1));
							logger.info("Adding url:" + deliverUrl.substring(deliverUrl.indexOf("=") + 1));							
						}
						else
							logger.info("Not adding url:" + deliverUrl + " as it was ruled not to..");
					}
					else
					{
						urls.add(deliverUrl);
					}
					
					i++;
				}	

			}	
			catch(Exception e)
			{
			    logger.error("Error loading properties from string. Reason:" + e.getMessage());
				//e.printStackTrace();
			}
		}
	    else
	    {
			int i = 0;
			String deliverUrl = null;
			while((deliverUrl = CmsPropertyHandler.getProperty("internalDeliverUrl." + i)) != null)
			{ 
				if(deliverUrl.indexOf("@") == -1)
				{
					logger.info("internalDeliverUrl:" + deliverUrl);
					if(!deliverUrl.startsWith("http"))
					{
						deliverUrl = "" + defaultScheme + "://127.0.0.1:" + defaultPort + "" + deliverUrl;
					}
					logger.info("internalDeliverUrl:" + deliverUrl);
						
					urls.add(deliverUrl);
				}
				i++;
			}	
	    }

	    return urls;
	}

	public static List getPublicDeliveryUrls()
	{
		List urls = new ArrayList();
		
	    String publicDeliverUrlString = CmsPropertyHandler.getServerNodeDataProperty(null, "publicDeliveryUrls", true, null);
	    if(publicDeliverUrlString != null && !publicDeliverUrlString.equals(""))
		{
	    	try
			{
	    		Properties properties = new Properties();
				properties.load(new ByteArrayInputStream(publicDeliverUrlString.getBytes("UTF-8")));

				int i = 0;
				String deliverUrl = null;
				while((deliverUrl = properties.getProperty("" + i)) != null)
				{ 
					urls.add(deliverUrl);
					i++;
				}

			}	
			catch(Exception e)
			{
			    logger.error("Error loading properties from string. Reason:" + e.getMessage());
				e.printStackTrace();
			}
		}
	    else
	    {
			int i = 0;
			String deliverUrl = null;
			while((deliverUrl = CmsPropertyHandler.getProperty("publicDeliverUrl." + i)) != null)
			{ 
				if(deliverUrl.indexOf("@") == -1)
				{
					logger.info("deliverUrl:" + deliverUrl);
					if(!deliverUrl.startsWith("http"))
					{
						deliverUrl = "" + defaultScheme + "://127.0.0.1:" + defaultPort + "" + deliverUrl;
					}
					logger.info("deliverUrl:" + deliverUrl);
						
					urls.add(deliverUrl);
				}
				
				i++;
			}	
	    }
	    
	    return urls;
	}

	public static Map<String, DeploymentServerBean> getDeploymentServers()
	{
		Map<String, DeploymentServerBean> deploymentServers = new HashMap<String, DeploymentServerBean>();
		
	    String deploymentServersString = CmsPropertyHandler.getServerNodeDataProperty(null, "deploymentServers", true, null);
	    if(deploymentServersString != null && !deploymentServersString.equals(""))
		{
	    	try
			{
	    		Properties properties = new Properties();
				properties.load(new ByteArrayInputStream(deploymentServersString.getBytes("UTF-8")));

				int i = 0;
				String deploymentServerName = null;
				while((deploymentServerName = properties.getProperty("" + i)) != null)
				{
					String url = properties.getProperty("" + i + ".url");
					String user = properties.getProperty("" + i + ".user");
					String password = properties.getProperty("" + i + ".password");
					
					if(url == null)
						throw new Exception("Missing url-property for " + deploymentServerName + " (index: " + i + ")");
					if(user == null)
						throw new Exception("Missing user-property for " + deploymentServerName + " (index: " + i + ")");
					
					DeploymentServerBean dsb = new DeploymentServerBean(deploymentServerName, url, user, password);
					deploymentServers.put(deploymentServerName, dsb);
					i++;
				}	
			}	
			catch(Exception e)
			{
			    logger.error("Error loading properties from string. Reason:" + e.getMessage());
				e.printStackTrace();
			}
		}
	    
	    return deploymentServers;
	}

	public static Map<String,VersionControlServerBean> getVCServers()
	{
		Map<String,VersionControlServerBean> servers = new HashMap<String,VersionControlServerBean>();
		
	    String vcServersString = CmsPropertyHandler.getServerNodeDataProperty(null, "vcServers", true, null);
	    if(vcServersString != null && !vcServersString.equals(""))
		{
	    	try
			{
	    		Properties properties = new Properties();
				properties.load(new ByteArrayInputStream(vcServersString.getBytes("UTF-8")));

				int i = 0;
				String vcServerName = null;
				while((vcServerName = properties.getProperty("" + i)) != null)
				{
					String host = properties.getProperty("" + i + ".host");
					String path = properties.getProperty("" + i + ".path");
					String module = properties.getProperty("" + i + ".module");
					String user = properties.getProperty("" + i + ".user");
					String password = properties.getProperty("" + i + ".password");
					String type = properties.getProperty("" + i + ".type");
					String port = properties.getProperty("" + i + ".port");
					
					if(host == null)
						throw new Exception("Missing host-property for " + vcServerName + " (index: " + i + ")");
					if(path == null)
						throw new Exception("Missing path-property for " + vcServerName + " (index: " + i + ")");
					if(module == null)
						throw new Exception("Missing module-property for " + vcServerName + " (index: " + i + ")");
					if(user == null)
						throw new Exception("Missing user-property for " + vcServerName + " (index: " + i + ")");
					if(type == null)
						throw new Exception("Missing type-property for " + vcServerName + " (index: " + i + ")");
					
					VersionControlServerBean vcb = new VersionControlServerBean(vcServerName, host, path, module, user, password, type, port);
					servers.put(vcServerName, vcb);
					i++;
				}	

			}	
			catch(Exception e)
			{
			    logger.error("Error loading properties from string. Reason:" + e.getMessage());
			}
		}
	    
	    return servers;
	}

	public static List getToolLocales()
	{
		List toolLocales = new ArrayList();
		
	    String toolLanguagesString = CmsPropertyHandler.getServerNodeDataProperty(null, "toolLanguages", true, null);
	    if(toolLanguagesString != null && !toolLanguagesString.equals(""))
		{
	    	try
			{
	    		Properties properties = new Properties();
				properties.load(new ByteArrayInputStream(toolLanguagesString.getBytes("UTF-8")));

			    int index = 0;
			    String languageCode = properties.getProperty("" + index);
			    while(languageCode != null)
			    {
			        Locale locale = new java.util.Locale(languageCode);
			        if(locale != null)
			            toolLocales.add(locale);
			        
			        index++;
			        languageCode = properties.getProperty("" + index);
			    }

			}	
			catch(Exception e)
			{
			    logger.error("Error loading properties from string. Reason:" + e.getMessage());
				e.printStackTrace();
			}
		}
	    else
	    {
	    	int index = 0;
		    String languageCode = CmsPropertyHandler.getProperty(index + ".toolLanguageCode");
		    while(languageCode != null)
		    {
		        Locale locale = new java.util.Locale(languageCode);
		        if(locale != null)
		            toolLocales.add(locale);
		        
		        index++;
		        languageCode = CmsPropertyHandler.getProperty(index + ".toolLanguageCode");
		    }
	    }

	    return toolLocales;
	}

	public static Map getCustomContentTypeIcons()
	{
		Map customContentTypeIcons = new HashMap();
		
	    String customContentTypeIconsString = CmsPropertyHandler.getServerNodeDataProperty(null, "customContentTypeIcons", true, null);
	    if(customContentTypeIconsString != null && !customContentTypeIconsString.equals(""))
		{
	    	try
			{
	    		Properties properties = new Properties();
				properties.load(new ByteArrayInputStream(customContentTypeIconsString.getBytes("UTF-8")));
				customContentTypeIcons.putAll(properties);
			}	
			catch(Exception e)
			{
			    logger.error("Error loading properties from string. Reason:" + e.getMessage());
				e.printStackTrace();
			}
		}
	    
	    return customContentTypeIcons;
	}

	public static Map getCacheSettings()
	{
		Map cacheSettings = new HashMap();
		
	    String cacheSettingsString = CmsPropertyHandler.getServerNodeDataProperty(null, "cacheSettings", true, null, true);
	    if(cacheSettingsString != null && !cacheSettingsString.equals(""))
		{
	    	try
			{
	    		Properties properties = new Properties();
				properties.load(new ByteArrayInputStream(cacheSettingsString.getBytes("UTF-8")));
				cacheSettings = properties;
			}	
			catch(Exception e)
			{
			    logger.error("Error loading properties from string. Reason:" + e.getMessage());
				e.printStackTrace();
			}
		}
	    
	    return cacheSettings;
	}

	
	public static List<String> getExtraPublicationPersistentCacheNames() 
	{
		List<String> cacheNames = new ArrayList<String>();
		
	    String extraPublicationPersistentCacheNamesString = CmsPropertyHandler.getServerNodeDataProperty(null, "extraPublicationPersistentCacheNames", true, null, true);
	    if(extraPublicationPersistentCacheNamesString != null && !extraPublicationPersistentCacheNamesString.equals(""))
		{
	    	String[] names = extraPublicationPersistentCacheNamesString.split("(\n|\r|\r\n)");
	    	for(int i=0; i<names.length; i++)
	    	{
	    		String name = names[i].trim();
	    		if(!name.equals(""))
	    			cacheNames.add(name);
	    	}
		}
	    
	    return cacheNames;
	}

	private static PropertySet getPropertySet()
	{
		if(propertySet == null)
			restorePropertySet();
		
		return propertySet;
	}

	public static String getPropertySetValue(String key)
	{
		return getPropertySetValue(key, false);
	}

	public static String getPropertySetValue(String key, boolean skipCaches)
	{
	    String value = null;
	    
        String cacheKey = "" + key;
        String cacheName = "propertySetCache";
		//logger.info("cacheKey:" + cacheKey);
        if(!skipCaches)
        	value = (String)CacheController.getCachedObject(cacheName, cacheKey);
		
        if(value != null)
		{
        	if(logger.isInfoEnabled())
        		logger.info("Returning property " + cacheKey + " value " + value);
			return value;
		}
	    
		value = getPropertySet().getString(key);
		logger.info("propertySetCache did not have value... refetched:" + value);
	    
		if(!skipCaches)
			CacheController.cacheObject(cacheName, cacheKey, value);
	    
	    return value;
	}

	public static String getAnonymousPassword()
	{
		String password = "anonymous";
		//String specifiedPassword = getProperty("security.anonymous.password");
		String specifiedPassword = getServerNodeProperty("deliver", "security.anonymous.password", true, "anonymous");
		if(specifiedPassword != null && !specifiedPassword.equalsIgnoreCase("") && specifiedPassword.indexOf("security.anonymous.password") == -1)
			password = specifiedPassword;
		
		return password;
	}

	public static String getAnonymousUser()
	{
		String userName = "anonymous";
		String specifiedUserName = getServerNodeProperty("deliver", "security.anonymous.username", true, "anonymous");
		if(specifiedUserName != null && !specifiedUserName.equalsIgnoreCase("") && specifiedUserName.indexOf("security.anonymous.username") == -1)
			userName = specifiedUserName;
		
		return userName;
	}

	public static Properties getCharacterReplacingMapping()
	{
		Properties properties = new Properties();

		String characterReplacingMappingString = CmsPropertyHandler.getServerNodeDataProperty(null, "niceURICharacterReplacingMapping", true, null);
		logger.info("characterReplacingMappingString:" + characterReplacingMappingString);
	    if(characterReplacingMappingString != null && !characterReplacingMappingString.equals(""))
		{
	    	try
			{
				properties.load(new ByteArrayInputStream(characterReplacingMappingString.getBytes("ISO-8859-1")));
			}	
			catch(Exception e)
			{
			    logger.error("Error loading properties from string. Reason:" + e.getMessage());
				e.printStackTrace();
			}
		}
	    if(properties.size() == 0)
	    {
	    	properties.put("å", "a");
	    	properties.put("ä", "a");
	    	properties.put("ö", "o");
	    	properties.put("Å", "A");
	    	properties.put("Ä", "A");
	    	properties.put("Ö", "O");
	    }
	    
	    return properties;
	}

	public static boolean getNiceURIUseLowerCase()
	{
		String niceURIUseLowerCase = getServerNodeProperty("niceURIUseLowerCase", true, "false");
		
		return Boolean.parseBoolean(niceURIUseLowerCase);
	}

	public static String getNiceURIDefaultReplacementCharacter()
	{
		return getServerNodeProperty("niceURIDefaultReplacementCharacter", true, "_");
	}

	public static String getNiceURIDefaultReplacementCharacterForContent()
	{
		return getServerNodeProperty("niceURIDefaultReplacementCharacterForContent", true, getNiceURIDefaultReplacementCharacter());
	}

	public static String getUseAccessBasedProtocolRedirects()
	{
		return getUseAccessBasedProtocolRedirects(false);
	}

	public static String getUseAccessBasedProtocolRedirects(boolean skipHardCache)
	{
		if(useAccessBasedProtocolRedirects == null || skipHardCache)
		{
			useAccessBasedProtocolRedirects = getServerNodeProperty("useAccessBasedProtocolRedirects", true, "false");
		}
		
		return useAccessBasedProtocolRedirects;
	}
	
	public static String getDeriveProtocolWhenUsingProtocolRedirects()
	{
		return getServerNodeProperty("deriveProtocolWhenUsingProtocolRedirects", true, "false");
	}

	public static String getUnprotectedProtocolName()
	{
		return getServerNodeProperty("unprotectedProtocolName", true, "http");
	}

	public static String getProtectedProtocolName()
	{
		return getServerNodeProperty("protectedProtocolName", true, "https");
	}

	public static String getUnprotectedProtocolPort()
	{
		return getServerNodeProperty("unprotectedProtocolPort", true, "8080");
	}

	public static String getProtectedProtocolPort()
	{
		return getServerNodeProperty("protectedProtocolPort", true, "8443");
	}

	public static String getAccessBasedProtocolRedirectHTTPCode()
	{
		return getServerNodeProperty("accessBasedProtocolRedirectHTTPCode", true, "301");
	}

	public static String getLoginUrl()
	{
		return getServerNodeProperty("loginUrl", true, "");
	}

	public static String getLogoutUrl()
	{
		return getServerNodeProperty("logoutUrl", true, "");
	}

	public static String getInvalidLoginUrl()
	{
		return getServerNodeProperty("invalidLoginUrl", true, "");
	}

	public static String getSuccessLoginBaseUrl()
	{
		return getServerNodeProperty("successLoginBaseUrl", true, "");
	}

	public static String getAuthenticatorClass()
	{
		return getServerNodeProperty("authenticatorClass", true, "");
	}

	public static String getAuthorizerClass()
	{
		return getServerNodeProperty("authorizerClass", true, "");
	}

	public static String getAuthConstraint()
	{
		return getServerNodeProperty("authConstraint", true, "");
	}

	public static String getExtraSecurityParameters()
	{
		return "";
	}

	public static String getCasValidateUrl()
	{
		return getServerNodeProperty("casValidateUrl", true, "");
	}

	public static String getCasProxyValidateUrl()
	{
		return getServerNodeProperty("casProxyValidateUrl", true, "");
	}

	public static String getCasServiceUrl()
	{
		return getServerNodeProperty("casServiceUrl", true, "");
	}

	public static String getCasLogoutUrl()
	{
		return getServerNodeProperty("casLogoutUrl", true, "");
	}

	public static String getDeliverLoginUrl()
	{
		return getServerNodeProperty("deliver", "loginUrl", true, "");
	}

	public static String getDeliverLogoutUrl()
	{
		return getServerNodeProperty("deliver", "logoutUrl", true, "");
	}

	public static String getDeliverInvalidLoginUrl()
	{
		return getServerNodeProperty("deliver", "invalidLoginUrl", true, "");
	}

	public static String getDeliverSuccessLoginBaseUrl()
	{
		return getServerNodeProperty("deliver", "successLoginBaseUrl", true, "");
	}

	public static String getDeliverAuthenticatorClass()
	{
		return getServerNodeProperty("deliver", "authenticatorClass", true, "");
	}

	public static String getDeliverAuthorizerClass()
	{
		return getServerNodeProperty("deliver", "authorizerClass", true, "");
	}

	public static String getDeliverAuthConstraint()
	{
		return getServerNodeProperty("deliver", "authConstraint", true, "");
	}

	public static String getDeliverExtraSecurityParameters()
	{
		return "";
	}

	public static String getDeliverCasValidateUrl()
	{
		return getServerNodeProperty("deliver", "casValidateUrl", true, "");
	}

	public static String getDeliverCasProxyValidateUrl()
	{
		return getServerNodeProperty("deliver", "casProxyValidateUrl", true, "");
	}

	public static String getDeliverCasServiceUrl()
	{
		return getServerNodeProperty("deliver", "casServiceUrl", true, "");
	}

	public static String getDeliverCasLogoutUrl()
	{
		return getServerNodeProperty("deliver", "casLogoutUrl", true, "");
	}

	public static String getIpAddressesToFallbackToBasicAuth()
	{
		return getServerNodeProperty("ipAddressesToFallbackToBasicAuth", true, "");
	}

	public static boolean getDisableDecoratedFinalRendering()
	{
		String disableDecoratedFinalRendering = getServerNodeProperty("disableDecoratedFinalRendering", true, "false");
		
		return Boolean.parseBoolean(disableDecoratedFinalRendering);
	}
	
	public static boolean getAllowInternalCallsBasedOnIP() 
	{
		String allowInternalCallsBasedOnIP = getServerNodeProperty("allowInternalCallsBasedOnIP", true, "true");
		
		return Boolean.parseBoolean(allowInternalCallsBasedOnIP);
	}

	private static Boolean useSQLServerDialect = null;
	public static boolean getUseSQLServerDialect()
	{
        if(useSQLServerDialect == null)
        {
            String databaseEngine = CmsPropertyHandler.getDatabaseEngine();
            if(databaseEngine == null || (!databaseEngine.equalsIgnoreCase("mssql") && !databaseEngine.equalsIgnoreCase("sql-server") && !databaseEngine.equalsIgnoreCase("SQL Server")))
            {
            	useSQLServerDialect = new Boolean(false);
            }
            else
            {
            	useSQLServerDialect = new Boolean(true);
            }
        }
        
        return useSQLServerDialect.booleanValue();
	}

	public static String getAssetFileNameForm() 
	{
		return getServerNodeProperty("assetFileNameForm", true, "contentId_languageId_assetKey");
	}

	public static String getSQLUpgradePath() 
	{
		return "" + getContextRootPath() + "up2date" + File.separator + "sql";
	}

	private static Boolean configurationFinished = null;
	public static boolean getIsConfigurationFinished()
	{
		try 
		{
			if(configurationFinished == null || configurationFinished == false)
				configurationFinished = InstallationController.getController().validateApplicationFile();
		} 
		catch (Exception e) 
		{
			logger.error("Could not check if infoglue was configured: " + e.getMessage(), e);
		}
		
		return configurationFinished;
	}

	private static Boolean isValidSetup = false;
	public static void setIsValidSetup(Boolean isValidSetup) 
	{
		CmsPropertyHandler.isValidSetup = isValidSetup;
	}
	public static Boolean getIsValidSetup() 
	{
		return CmsPropertyHandler.isValidSetup;
	}
}
