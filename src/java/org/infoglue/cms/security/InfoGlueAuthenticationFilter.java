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

package org.infoglue.cms.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.Session;
import org.infoglue.cms.controllers.kernel.impl.simple.SystemUserController;
import org.infoglue.cms.controllers.kernel.impl.simple.TransactionHistoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.DesEncryptionHelper;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.deliver.applications.filters.URIMatcher;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.HttpHelper;

/**
 * This filter protects actions withing InfoGlue from access without authentication. 
 * It is very generic and can use any authentication module. The filter is responsible for reading the
 * settings and invoking the right authentication module.
 */

public class InfoGlueAuthenticationFilter implements Filter 
{
    private final static Logger logger = Logger.getLogger(InfoGlueAuthenticationFilter.class.getName());

	public final static String INFOGLUE_FILTER_USER = "org.infoglue.cms.security.user";
	
 	public static String loginUrl 				= null;
 	public static String logoutUrl 				= null;
	public static String invalidLoginUrl 		= null;
	public static String successLoginBaseUrl	= null;
	public static String authenticatorClass 	= null;
	public static String authorizerClass 		= null;
	public static String serverName	 			= null;
 	public static String authConstraint			= null;
	public static String extraParametersFile	= null;
 	public static Properties extraProperties	= null;
	public static String casValidateUrl			= null;
	public static String casProxyValidateUrl	= null;
	public static String casServiceUrl			= null;
	public static String casLogoutUrl			= null;
	public static String casRenew				= null;
 	
    private static String FILTER_URIS_PARAMETER = "FilterURIs";

    private FilterConfig filterConfig = null;
    private URIMatcher uriMatcher = null;
	private HttpHelper httpHelper = new HttpHelper();

	public void init(FilterConfig config) throws ServletException 
	{
		loginUrl 			= config.getInitParameter("org.infoglue.cms.security.loginUrl");
		logoutUrl 			= config.getInitParameter("org.infoglue.cms.security.logoutUrl");
		invalidLoginUrl 	= config.getInitParameter("org.infoglue.cms.security.invalidLoginUrl");
		successLoginBaseUrl = config.getInitParameter("org.infoglue.cms.security.successLoginBaseUrl");
		authenticatorClass 	= config.getInitParameter("org.infoglue.cms.security.authenticatorClass");
		authorizerClass 	= config.getInitParameter("org.infoglue.cms.security.authorizerClass");
		serverName  		= config.getInitParameter("org.infoglue.cms.security.serverName");
		authConstraint 		= config.getInitParameter("org.infoglue.cms.security.authConstraint");
		extraParametersFile	= config.getInitParameter("org.infoglue.cms.security.extraParametersFile");
		casValidateUrl		= config.getInitParameter("org.infoglue.cms.security.casValidateUrl");
		casProxyValidateUrl	= config.getInitParameter("org.infoglue.cms.security.casProxyValidateUrl");
		casServiceUrl		= config.getInitParameter("org.infoglue.cms.security.casServiceUrl");
		casLogoutUrl		= config.getInitParameter("org.infoglue.cms.security.casLogoutUrl");
		//casRenew			= config.getInitParameter("org.infoglue.cms.security.casRenew");
			    
		if(extraParametersFile != null)
		{
			try
			{
				extraProperties = new Properties();
				extraProperties.load(CmsPropertyHandler.class.getResourceAsStream("/" + extraParametersFile));	
			}	
			catch(Exception e)
			{
				logger.error("Error loading properties from file " + "/" + extraParametersFile + ". Reason:" + e.getMessage());
				e.printStackTrace();
			}
		}

		try
		{
			initializeCMSProperties();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
        this.filterConfig = config;
        String filterURIs = filterConfig.getInitParameter(FILTER_URIS_PARAMETER);
        uriMatcher = URIMatcher.compilePatterns(splitString(filterURIs, ","), false);
        
        try
        {
			boolean anonymousExists = UserControllerProxy.getController().userExists(CmsPropertyHandler.getAnonymousUser());
			if(!anonymousExists)
				logger.error("The anonymous user '" + CmsPropertyHandler.getAnonymousUser() + "' was not found. Add it immediately.");
        }
        catch (Exception e) 
        {
        	logger.error("Error checking if the anonymous user '" + CmsPropertyHandler.getAnonymousUser() + "' was not found. Message:" + e.getMessage());
		}
	}
    
	
	private static Boolean configurationFinished = null;

	/**
	 * This filter is basically what secures Infoglue and enforces the authentication framework.
	 */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) throws ServletException, IOException 
    {	
    	HttpServletRequest httpServletRequest = (HttpServletRequest)request;
		HttpServletResponse httpServletResponse = (HttpServletResponse)response;

		try
		{			
    		if (CmsPropertyHandler.getServletContext() == null) 
	        {
	        	CmsPropertyHandler.setServletContext(httpServletRequest.getContextPath());
	        }

			String URI = httpServletRequest.getRequestURI();
			String URL = httpServletRequest.getRequestURL().toString();
			if(logger.isInfoEnabled())
			{
				logger.info("URI: + " + URI);
				logger.info("URL: + " + URL);
			}
	
			String requestURI = URLDecoder.decode(getContextRelativeURI(httpServletRequest), "UTF-8");
			if(URI == null)
				logger.error("URI was null - requestURI:" + requestURI);
			if(URL == null)
				logger.error("URL was null - requestURI:" + requestURI);
			if(requestURI == null)
				logger.error("requestURI was null");
			
			if(loginUrl == null)
			{
				logger.error("loginUrl was null - fix this.");
				loginUrl = "Login.action";
			}
			if(invalidLoginUrl == null)
			{
				logger.error("invalidLoginUrl was null - fix this.");
				invalidLoginUrl = "Login!invalidLogin.action";
			}
			if(logoutUrl == null)
			{
				logger.error("logoutUrl was null - fix this.");
				logoutUrl = "ExtranetLogin!logout.action";
			}
			
			if(uriMatcher == null)
			{
				logger.error("uriMatcher was null - fix this.");
		        String filterURIs = filterConfig.getInitParameter(FILTER_URIS_PARAMETER);
		        uriMatcher = URIMatcher.compilePatterns(splitString(filterURIs, ","), false);
			}
						
			if(!CmsPropertyHandler.getIsValidSetup() && (URI.indexOf("Install") == -1 && URI.indexOf(".action") > -1))
			{
				httpServletResponse.sendRedirect("Install!input.action");
				return;
			}
			
			//Here are the url:s/paths that must be skipped by the security framework for it to work. Login screens etc must be reachable naturally.
			if(URI != null && URL != null && 
					(
							URI.indexOf(loginUrl) > -1 || 
							URL.indexOf(loginUrl) > -1 || 
							URI.indexOf("Login.action") > -1 || 
							URL.indexOf("Login.action") > -1 || 
							URI.indexOf(invalidLoginUrl) > -1 || 
							URL.indexOf(invalidLoginUrl) > -1 || 
							URI.indexOf("Login!invalidLogin.action") > -1 || 
							URL.indexOf("Login!invalidLogin.action") > -1 || 
							URI.indexOf(logoutUrl) > -1 || 
							URI.indexOf("Login!logout.action") > -1 || 
							URL.indexOf(logoutUrl) > -1 || 
							URI.indexOf("UpdateCache") > -1 || 
							URI.indexOf("protectedRedirect.jsp") > -1 || 
							uriMatcher.matches(requestURI)
					))
			{
				fc.doFilter(request, response); 
				return;
	   	 	}
	
			// make sure we've got an HTTP request
			if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse))
			  throw new ServletException("InfoGlue Filter protects only HTTP resources");
		
			HttpSession session = ((HttpServletRequest)request).getSession();
			
			String sessionTimeout = CmsPropertyHandler.getSessionTimeout();
			try { Integer.parseInt(sessionTimeout); } catch (Exception e) {sessionTimeout = "1800";}
			if(sessionTimeout == null)
			    sessionTimeout = "1800";
			
			session.setMaxInactiveInterval(new Integer(sessionTimeout).intValue());
	
			// if our attribute's already present, don't do anything
			//logger.info("User:" + session.getAttribute(INFOGLUE_FILTER_USER));
			if (session != null && session.getAttribute(INFOGLUE_FILTER_USER) != null) 
			{
			    //logger.info("Found user in session:" + session.getAttribute(INFOGLUE_FILTER_USER));
			    //if(successLoginBaseUrl != null && !URL.startsWith(successLoginBaseUrl))
			    //{
			    //    checkSuccessRedirect(request, response, URL);
			    //}
			    //else
			    //{
				  	fc.doFilter(request, response);
				    return;
				//}
			}
			
			// otherwise, we need to authenticate somehow
			boolean isAdministrator = false;
			
			String userName = request.getParameter("j_username");
			String password = request.getParameter("j_password");
			
			if(userName != null && password != null)
			{
				String administratorUserName = CmsPropertyHandler.getAdministratorUserName();
				
				boolean matchesRootPassword = CmsPropertyHandler.getMatchesAdministratorPassword(password);
				isAdministrator = (userName.equalsIgnoreCase(administratorUserName) && matchesRootPassword) ? true : false;
			}

			//First we check if the user is logged in to the container context
			if(!isAdministrator)
			{
				logger.info("Principal:" + httpServletRequest.getUserPrincipal());
			    if(httpServletRequest.getUserPrincipal() != null && !(httpServletRequest.getUserPrincipal() instanceof InfoGluePrincipal))
			    {
			    	userName = httpServletRequest.getUserPrincipal().getName();
			    	logger.info("Now trusting the container logged in identity...");
			    }
			}

			String authenticatedUserName = userName;
			
			if(!isAdministrator)
			{
				String encodedUserNameCookie = httpHelper.getCookie(httpServletRequest, "iguserid");
				logger.info("encodedUserNameCookie:" + encodedUserNameCookie);
				if(encodedUserNameCookie != null && !encodedUserNameCookie.equals(""))
				{
					byte[] bytes = Base64.decodeBase64(encodedUserNameCookie);
					encodedUserNameCookie = new String(bytes, "utf-8");
					//encodedUserNameCookie = encodedUserNameCookie.replaceAll("IGEQ", "=");
					logger.info("encodedUserNameCookie:" + encodedUserNameCookie);
					String servletContextUserName = (String)filterConfig.getServletContext().getAttribute(encodedUserNameCookie);
					logger.info("servletContextUserName:" + servletContextUserName);
					if(servletContextUserName != null && !servletContextUserName.equals(""))
					{
						authenticatedUserName = servletContextUserName;
					}
					else
					{
						Cookie cookie_iguserid = new Cookie("iguserid", "none");
						cookie_iguserid.setPath("/");
						cookie_iguserid.setMaxAge(0); 
						httpServletResponse.addCookie(cookie_iguserid);
					    
					    Cookie cookie_igpassword = new Cookie ("igpassword", "none");
					    cookie_igpassword.setPath("/");
					    cookie_igpassword.setMaxAge(0);
					    httpServletResponse.addCookie(cookie_igpassword);

					    authenticatedUserName = authenticateUser(httpServletRequest, httpServletResponse, fc);
					}
				}
				else
				{
					authenticatedUserName = authenticateUser(httpServletRequest, httpServletResponse, fc);
				}
			}
			
			logger.info("authenticatedUserName:" + authenticatedUserName);
			
			if(authenticatedUserName != null)
			{	
				logger.info("Getting the principal from user name:" + authenticatedUserName);
				
				InfoGluePrincipal user = getAuthenticatedUser(authenticatedUserName);
				if(user == null || (!user.getIsAdministrator() && !hasAuthorizedRole(user)))
				{	
					//throw new Exception("This user is not authorized to log in...");
					httpServletResponse.sendRedirect("unauthorizedLogin.jsp");

					NotificationMessage notificationMessage = new NotificationMessage("Authorization failed:", "Authorization", authenticatedUserName, NotificationMessage.AUTHORIZATION_FAILED, "" + authenticatedUserName, "name");
					TransactionHistoryController.getController().create(notificationMessage);
					
					return;
				}
				
				//TODO - we must fix so these caches are individual to the person - now a login will slow down for all
				//CacheController.clearCache("authorizationCache");
				//CacheController.clearCache("personalAuthorizationCache", user.getName());
				CacheController.clearCacheForGroup("personalAuthorizationCache", user.getName());

				// Store the authenticated user in the session
				if(session != null)
				{
					session.setAttribute(INFOGLUE_FILTER_USER, user);
					setUserProperties(session, user);
				}
				
				//TEST - transferring auth to deliverworking
			    try
			    {
			    	if(userName != null && password != null)
			    	{
					    DesEncryptionHelper encHelper = new DesEncryptionHelper();
						String encryptedName = encHelper.encrypt(userName);
						String encryptedPassword = encHelper.encrypt(password);
	
					    String encryptedNameAsBase64 = Base64.encodeBase64URLSafeString(encryptedName.getBytes("utf-8"));
					    String encryptedPasswordAsBase64 = Base64.encodeBase64URLSafeString(encryptedPassword.getBytes("utf-8"));

						String deliverBaseUrl = CmsPropertyHandler.getComponentRendererUrl();
						String[] parts = deliverBaseUrl.split("/");
						
						deliverBaseUrl = "/" + parts[parts.length -1];
						//logger.info("used cmsBaseUrl:" + cmsBaseUrl);
						
					    ServletContext servletContext = filterConfig.getServletContext().getContext(deliverBaseUrl);
					    if (servletContext == null)
					    {
					    	logger.error("Could not autologin to " + deliverBaseUrl + ". Set cross context = true in Tomcat config.");
					    }
					    else
					    {
					    	logger.info("Added encryptedName:" + encryptedName + " = " + user.getName() + " to deliver context");
					    	servletContext.setAttribute(encryptedName, user.getName());
					    }
					   
					    int cmsCookieTimeout = 1800; //30 minutes default
					    String cmsCookieTimeoutString = null; //CmsPropertyHandler.getCmsCookieTimeout();
					    if(cmsCookieTimeoutString != null)
					    {
					        try
						    {
					        	cmsCookieTimeout = Integer.parseInt(cmsCookieTimeoutString.trim());
						    }
					        catch(Exception e) {}
						}
					
					    
						//Cookie cookie_iguserid = new Cookie("iguserid", encryptedName.replaceAll("=", "IGEQ"));
					    Cookie cookie_iguserid = new Cookie("iguserid", encryptedNameAsBase64);
						cookie_iguserid.setPath("/");
						cookie_iguserid.setMaxAge(cmsCookieTimeout); 
						httpServletResponse.addCookie(cookie_iguserid);
					    
					    //Cookie cookie_igpassword = new Cookie ("igpassword", encryptedPassword.replaceAll("=", "IGEQ"));
					    Cookie cookie_igpassword = new Cookie ("igpassword", encryptedPasswordAsBase64);
					    cookie_igpassword.setPath("/");
					    cookie_igpassword.setMaxAge(cmsCookieTimeout);
					    httpServletResponse.addCookie(cookie_igpassword);
	
					    //logger.info(encryptedName + "=" + userName);
					    //logger.info("After attribute:" + servletContext.getAttribute(encryptedName));
			    	}
			    }
			    catch (Exception e) 
			    {	    	
			    	logger.error("Error: " + e.getMessage(), e);
				}
			    //END TEST
			    
				String logUserName = userName;
				if(logUserName == null || logUserName.equals("") && user != null)
					logUserName = user.getName();
				if(logUserName == null || logUserName.equals(""))
					logUserName = authenticatedUserName;
				if(logUserName == null || logUserName.equals(""))
					logUserName = "Unknown";
				
				NotificationMessage notificationMessage = new NotificationMessage("Login success:", "Authentication", logUserName, NotificationMessage.AUTHENTICATION_SUCCESS, "" + authenticatedUserName, "name");
				TransactionHistoryController.getController().create(notificationMessage);

			    if(successLoginBaseUrl != null && !URL.startsWith(successLoginBaseUrl))
			    {
			        checkSuccessRedirect(request, response, URL);
			    }
			    else
			    {
				  	fc.doFilter(request, response);
				    return;
			    }
			}
			else
			{
				if(userName != null && !userName.equals(""))
				{
					NotificationMessage notificationMessage = new NotificationMessage("Login failed:", "Authentication", userName, NotificationMessage.AUTHENTICATION_FAILED, "" + userName, "name");
					TransactionHistoryController.getController().create(notificationMessage);
				}
			}
		}
		catch(Exception e)
		{
			logger.error("Error authenticating user:" + e.getMessage(), e);
			httpServletRequest.setAttribute("error", new Exception("Error in authentication filter - look at the server error log (usually catalina.out) for reason but the most common one is problem connecting to the database or a faulty connection user or limited access for that account."));
			httpServletResponse.sendError(500);
			return;
		}
  	}

    /**
     * Here we set all user preferences given.
     * @param session
     * @param user
     */

    private void setUserProperties(HttpSession session, InfoGluePrincipal user)
	{
		String preferredLanguageCode = CmsPropertyHandler.getPreferredLanguageCode(user.getName());
	    if(preferredLanguageCode != null && preferredLanguageCode.length() > 0)
			session.setAttribute(Session.LOCALE, new java.util.Locale(preferredLanguageCode));
	    else
	        session.setAttribute(Session.LOCALE, java.util.Locale.ENGLISH);
	
		String preferredToolName = CmsPropertyHandler.getPreferredToolName(user.getName());
	    if(preferredToolName != null && preferredToolName.length() > 0)
			session.setAttribute(Session.TOOL_NAME, preferredToolName);
	    else
	        session.setAttribute(Session.TOOL_NAME, "StructureTool");
	}
    
  	public void destroy() { }

  	private void checkSuccessRedirect(ServletRequest request, ServletResponse response, String URL) throws ServletException, IOException, UnsupportedEncodingException
  	{
	    String requestURI = ((HttpServletRequest)request).getRequestURI();
		
		String requestQueryString = ((HttpServletRequest)request).getQueryString();
		if(requestQueryString != null)
		    requestQueryString = "?" + requestQueryString;
		else
		    requestQueryString = "";
		
		String redirectUrl = "";			    
		    
		/*
		if(requestURI.indexOf("?") > 0)
			redirectUrl = loginUrl + "&referringUrl=" + URLEncoder.encode(requestURI + requestQueryString, "UTF-8");
		else
			redirectUrl = loginUrl + "?referringUrl=" + URLEncoder.encode(requestURI + requestQueryString, "UTF-8");
		*/
		if(requestURI.indexOf("?") > -1)
			redirectUrl = successLoginBaseUrl + requestURI + URLEncoder.encode(requestQueryString, "UTF-8");
		else
			redirectUrl = successLoginBaseUrl + requestURI + URLEncoder.encode(requestQueryString, "UTF-8");
		
		logger.info("redirectUrl:" + redirectUrl);
		((HttpServletResponse)response).sendRedirect(redirectUrl);
	}

  	private boolean hasAuthorizedRole(InfoGluePrincipal user)
  	{
  	    boolean isAuthorized = false;

        logger.info("authConstraint:" + authConstraint);
       
  	    if(authConstraint == null || authConstraint.equalsIgnoreCase(""))
  	        return true;
  	    
  	    String[] authConstraints = authConstraint.split(";");
  	    
  	    Iterator rolesIterator = user.getRoles().iterator();
  	    outer:while(rolesIterator.hasNext())
  	    {
  	        InfoGlueRole role = (InfoGlueRole)rolesIterator.next();
  	        logger.info("role:" + role);
  	        for (int i = 0; i < authConstraints.length; i++) 
  	        {
  	  	        if(role.getName().equalsIgnoreCase(authConstraints[i]))
  	  	        {
  	  	            isAuthorized = true;
  	  	            break outer;
  	  	        }				
			}
  	    }
  	    
  	    return isAuthorized;
  	}

  	private String authenticateUser(HttpServletRequest request, HttpServletResponse response, FilterChain fc) throws ServletException, Exception 
  	{
  		String authenticatedUserName = null;
  		
  		String currentUrl = null;
		if(this.casServiceUrl != null && this.casServiceUrl.equals("$currentUrl"))
		{
			currentUrl = request.getRequestURL() + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
		}

  		AuthenticationModule authenticationModule = AuthenticationModule.getAuthenticationModule(null, currentUrl, request, false);
		authenticatedUserName = authenticationModule.authenticateUser(request, response, fc);
		
		return authenticatedUserName;
  	}
  	
  	
  	/**
  	 * This method fetches the roles and other stuff for the user by invoking the autorizer-module.
  	 */
  	
	private InfoGluePrincipal getAuthenticatedUser(String userName) throws ServletException, Exception 
	{
		AuthorizationModule authorizationModule = null;
		try
		{
			authorizationModule = (AuthorizationModule)Class.forName(authorizerClass).newInstance();
		}
		catch(Exception e)
		{
			logger.error("The authorizationModule-class was wrong:" + e.getMessage() + ": defaulting to infoglue:s own", e);
			authorizationModule = (AuthorizationModule)Class.forName(InfoGlueBasicAuthorizationModule.class.getName()).newInstance();
		}
		
		authorizationModule.setExtraProperties(extraProperties);
		logger.info("authorizerClass:" + authorizerClass + ":" + authorizationModule.getClass().getName());
		
		InfoGluePrincipal infoGluePrincipal = authorizationModule.getAuthorizedInfoGluePrincipal(userName);
		logger.info("infoGluePrincipal:" + infoGluePrincipal);
		if(infoGluePrincipal != null)
		{
			logger.info("roles:" + infoGluePrincipal.getRoles());
			logger.info("groups:" + infoGluePrincipal.getGroups());
		}
		
		return infoGluePrincipal;		
  	}

	
	//TODO - These getters are an ugly way of getting security properties unless initialized by the filter.
	//We should handle this different later on.
	
	public static void initializeProperties() throws SystemException
	{
	    try
		{
		    authenticatorClass 	= CmsPropertyHandler.getServerNodeProperty("deliver", "authenticatorClass", true, null);
		    authorizerClass 	= CmsPropertyHandler.getServerNodeProperty("deliver", "authorizerClass", true, null);
		    invalidLoginUrl 	= CmsPropertyHandler.getServerNodeProperty("deliver", "invalidLoginUrl", true, null);
		    successLoginBaseUrl = CmsPropertyHandler.getServerNodeProperty("deliver", "successLoginBaseUrl", true, null);
		    loginUrl 			= CmsPropertyHandler.getServerNodeProperty("deliver", "loginUrl", true, null);
		    logoutUrl 			= CmsPropertyHandler.getServerNodeProperty("deliver", "logoutUrl", true, null);
		    serverName 			= CmsPropertyHandler.getServerNodeProperty("deliver", "serverName", true, null);
		    casRenew 			= CmsPropertyHandler.getServerNodeProperty("deliver", "casRenew", true, null);
		    casServiceUrl 		= CmsPropertyHandler.getServerNodeProperty("deliver", "casServiceUrl", true, null);
		    casValidateUrl 		= CmsPropertyHandler.getServerNodeProperty("deliver", "casValidateUrl", true, null);
		    casProxyValidateUrl = CmsPropertyHandler.getServerNodeProperty("deliver", "casProxyValidateUrl", true, null);
		    casLogoutUrl 		= CmsPropertyHandler.getServerNodeProperty("deliver", "casLogoutUrl", true, null);
		    
		    String extraPropertiesString = CmsPropertyHandler.getServerNodeDataProperty("deliver", "extraSecurityParameters", true, null);
		    if(extraPropertiesString != null)
			{
			    logger.info("Loading extra properties from propertyset. extraPropertiesString:" + extraPropertiesString);
		    	try
				{
		    		extraProperties = new Properties();
					extraProperties.load(new ByteArrayInputStream(extraPropertiesString.getBytes("UTF-8")));
				}	
				catch(Exception e)
				{
				    logger.error("Error loading properties from string. Reason:" + e.getMessage());
				}
			}
		    else
		    {
			    String extraPropertiesFile = CmsPropertyHandler.getProperty("extraParametersFile");
			    logger.info("Trying to load extra properties from file. extraPropertiesFile:" + extraPropertiesFile);
			    if(extraPropertiesFile != null)
				{
					try
					{
						extraProperties = new Properties();
						extraProperties.load(CmsPropertyHandler.class.getResourceAsStream("/" + extraPropertiesFile));	
					}	
					catch(Exception e)
					{
					    logger.error("Error loading properties from file " + "/" + extraPropertiesFile + ". Reason:" + e.getMessage());
						e.printStackTrace();
					}
				}

		    }
			    
		    logger.info("authenticatorClass:" + authenticatorClass);
		    logger.info("authorizerClass:" + authorizerClass);
		    logger.info("invalidLoginUrl:" + invalidLoginUrl);
		    logger.info("successLoginBaseUrl:" + successLoginBaseUrl);
		    logger.info("loginUrl:" + loginUrl);
		    logger.info("logoutUrl:" + logoutUrl);
		    logger.info("serverName:" + serverName);
		    logger.info("casRenew:" + casRenew);
		    logger.info("casServiceUrl:" + casServiceUrl);
		    logger.info("casValidateUrl:" + casValidateUrl);
		    logger.info("casProxyValidateUrl:" + casProxyValidateUrl);
		    logger.info("casLogoutUrl:" + casLogoutUrl);
		    if(logger.isDebugEnabled())
		    {
		    	if(extraProperties != null)
			    	extraProperties.list(System.out);
		    	else
		    		logger.info("extraProperties:" + extraProperties);		
		    }
		}
		catch(Exception e)
		{
		    logger.error("An error occurred so we should not complete the transaction:" + e, e);
			throw new SystemException("Setting the security parameters failed: " + e.getMessage(), e);
		}
	}

	//TODO - These getters are an ugly way of getting security properties unless initialized by the filter.
	//We should handle this different later on.
	
	public static void initializeCMSProperties() throws SystemException
	{
	    try
		{
		    String authenticatorClass 	= CmsPropertyHandler.getServerNodeProperty("authenticatorClass", true, "org.infoglue.cms.security.InfoGlueBasicAuthenticationModule");
		    String authorizerClass		= CmsPropertyHandler.getServerNodeProperty("authorizerClass", true, "org.infoglue.cms.security.InfoGlueBasicAuthorizationModule");
		    String invalidLoginUrl 		= CmsPropertyHandler.getServerNodeProperty("invalidLoginUrl", true, "Login!invalidLogin.action");
		    String successLoginBaseUrl 	= CmsPropertyHandler.getServerNodeProperty("successLoginBaseUrl", true, null);
		    String loginUrl 			= CmsPropertyHandler.getServerNodeProperty("loginUrl", true, "Login.action");
		    String logoutUrl 			= CmsPropertyHandler.getServerNodeProperty("logoutUrl", true, "Login!logout.action");
		    String serverName 			= CmsPropertyHandler.getServerNodeProperty("serverName", true, null);
		    String casRenew 			= CmsPropertyHandler.getServerNodeProperty("casRenew", true, null);
		    String casServiceUrl 		= CmsPropertyHandler.getServerNodeProperty("casServiceUrl", true, null);
		    String casValidateUrl 		= CmsPropertyHandler.getServerNodeProperty("casValidateUrl", true, null);
		    String casProxyValidateUrl 	= CmsPropertyHandler.getServerNodeProperty("casProxyValidateUrl", true, null);
		    String casLogoutUrl 		= CmsPropertyHandler.getServerNodeProperty("casLogoutUrl", true, null);
		    String authConstraint		= CmsPropertyHandler.getServerNodeProperty("authConstraint", true, "cmsUser");
		    
	    	InfoGlueAuthenticationFilter.authenticatorClass = authenticatorClass;
	    	InfoGlueAuthenticationFilter.authorizerClass = authorizerClass;
	    	InfoGlueAuthenticationFilter.invalidLoginUrl = invalidLoginUrl;
	    	InfoGlueAuthenticationFilter.successLoginBaseUrl = successLoginBaseUrl;
	    	InfoGlueAuthenticationFilter.loginUrl = loginUrl;
	    	InfoGlueAuthenticationFilter.logoutUrl = logoutUrl;
	    	InfoGlueAuthenticationFilter.serverName = serverName;
	    	InfoGlueAuthenticationFilter.casRenew = casRenew;
	    	InfoGlueAuthenticationFilter.authConstraint = authConstraint;
	    	InfoGlueAuthenticationFilter.casServiceUrl = casServiceUrl;
	    	InfoGlueAuthenticationFilter.casValidateUrl = casValidateUrl;
	    	InfoGlueAuthenticationFilter.casProxyValidateUrl = casProxyValidateUrl;
	    	InfoGlueAuthenticationFilter.casLogoutUrl = casLogoutUrl;

		    String extraPropertiesString = CmsPropertyHandler.getServerNodeDataProperty("deliver", "extraSecurityParameters", true, null);
		    logger.info("extraPropertiesString 1:" + extraPropertiesString);
		    if(extraPropertiesString == null || extraPropertiesString.equals(""))
		    {
		    	extraPropertiesString = CmsPropertyHandler.getServerNodeDataProperty(null, "extraSecurityParameters", true, null);
		    	logger.info("extraPropertiesString 2:" + extraPropertiesString);
		    }
		    
		    if(extraPropertiesString != null)
			{
			    logger.info("Loading extra properties from propertyset. extraPropertiesString:" + extraPropertiesString);
		    	try
				{
		    		InfoGlueAuthenticationFilter.extraProperties = new Properties();
		    		InfoGlueAuthenticationFilter.extraProperties.load(new ByteArrayInputStream(extraPropertiesString.getBytes("UTF-8")));
				}	
				catch(Exception e)
				{
				    logger.error("Error loading properties from string. Reason:" + e.getMessage());
					e.printStackTrace();
				}
			}
		    else
		    {
			    String extraPropertiesFile = CmsPropertyHandler.getProperty("extraParametersFile");
			    logger.info("Trying to load extra properties from file. extraPropertiesFile:" + extraPropertiesFile);
			    if(extraPropertiesFile != null)
				{
					try
					{
						InfoGlueAuthenticationFilter.extraProperties = new Properties();
						InfoGlueAuthenticationFilter.extraProperties.load(CmsPropertyHandler.class.getResourceAsStream("/" + extraPropertiesFile));	
					}	
					catch(Exception e)
					{
					    logger.error("Error loading properties from file " + "/" + extraPropertiesFile + ". Reason:" + e.getMessage());
						e.printStackTrace();
					}
				}
		    }
		    
		    logger.info("\n\nRELOADED THE AUTH FILTER PROPS...:" + extraProperties);
		    
		    if(logger.isDebugEnabled())
		    	extraProperties.list(System.out);
		    
		    logger.info("authenticatorClass:" + authenticatorClass);
		    logger.info("authorizerClass:" + authorizerClass);
		    logger.info("invalidLoginUrl:" + invalidLoginUrl);
		    logger.info("successLoginBaseUrl:" + successLoginBaseUrl);
		    logger.info("loginUrl:" + loginUrl);
		    logger.info("logoutUrl:" + logoutUrl);
		    logger.info("serverName:" + serverName);
		    logger.info("authConstraint:" + authConstraint);
		    logger.info("casRenew:" + casRenew);
		    logger.info("casServiceUrl:" + casServiceUrl);
		    logger.info("casValidateUrl:" + casValidateUrl);
		    logger.info("casProxyValidateUrl:" + casProxyValidateUrl);
		    logger.info("casLogoutUrl:" + casLogoutUrl);
		    if(logger.isDebugEnabled())
		    {
		    	if(extraProperties != null)
			    	extraProperties.list(System.out);
		    	else
		    		logger.info("extraProperties:" + extraProperties);		
		    }
		}
		catch(Exception e)
		{
		    logger.error("An error occurred so we should not complete the transaction:" + e, e);
			throw new SystemException("Setting the security parameters failed: " + e.getMessage(), e);
		}
		
		UserControllerProxy.initializedImportClass = false;
	}

    private String getContextRelativeURI(HttpServletRequest request) 
    {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && requestURI.length() > 0) 
        {
            requestURI = requestURI.substring(contextPath.length(), requestURI.length());
        }
        
        if (requestURI.length() == 0)
            return "/";
        
        return requestURI;
    }

    private String[] splitString(String str, String delimiter) 
    {
        List list = new ArrayList();
        StringTokenizer st = new StringTokenizer(str, delimiter);
        while (st.hasMoreTokens()) 
        {
            // Updated to handle portal-url:s
            String t = st.nextToken();
            if (t.startsWith("_")) 
            {
                break;
            } 
            else 
            {
                // Not related to portal - add
                list.add(t.trim());
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

}
 