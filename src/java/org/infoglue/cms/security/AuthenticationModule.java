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

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.IPMatcher;
import org.infoglue.deliver.util.Timer;


/**
 * This abstract class defines what a authentication module has to implement.
 * It also acts as a factory class for the authentication framework.
 * 
 * @author Mattias Bogeblad
 */

public abstract class AuthenticationModule
{
    private final static Logger logger = Logger.getLogger(AuthenticationModule.class.getName());

    /**
     * This is the authentication framework factory method which looks at the system configuration and instantiates an implementation based on this.
     * A deviation from the normal pattern exists and it is related to how SSO-implementations (in this case CAS initially) is handled. As CAS requires the 
     * complete delegation of the request process (login form etc) to the CAS-service it is harder to login other systems or search spiders etc and a strong requirement 
     * from customers has been to allow for certain IP-ranges to be directed to the basic authentication module instead (Infoglue's own) which is simpler to automate.   
     * So for this to work the metod contains matching on IP and an optional parameter which forces fallback. 
     * 
     * @param transactionObject Optional database object if the authentication should be carried out within a transaction.
     * @param successLoginUrl The url to return the user to when the login is successful. 
     * @param request The user's HttpServletRequest.
     * @param forceBasicModule In some cases - like when running WebDAV and CAS is not an option (or is it).
     * @return The implementation of AuthenticationModule in question.
     * @throws SystemException
     */
	public static AuthenticationModule getAuthenticationModule(Object transactionObject, String successLoginUrl, HttpServletRequest request, boolean forceBasicModule) throws SystemException
	{
		AuthenticationModule authenticationModule = null;
		
		try
		{
		    String authenticatorClass 	= InfoGlueAuthenticationFilter.authenticatorClass;
		    String authorizerClass 	  	= InfoGlueAuthenticationFilter.authorizerClass;
		    String invalidLoginUrl 		= InfoGlueAuthenticationFilter.invalidLoginUrl;
		    String loginUrl 			= InfoGlueAuthenticationFilter.loginUrl;
		    String logoutUrl 			= InfoGlueAuthenticationFilter.logoutUrl;
		    String serverName 			= InfoGlueAuthenticationFilter.serverName;
		    Properties extraProperties 	= InfoGlueAuthenticationFilter.extraProperties;
		    String casRenew 			= InfoGlueAuthenticationFilter.casRenew;
		    String casServiceUrl 		= InfoGlueAuthenticationFilter.casServiceUrl;
		    String casValidateUrl 		= InfoGlueAuthenticationFilter.casValidateUrl;
		    String casProxyValidateUrl 	= InfoGlueAuthenticationFilter.casProxyValidateUrl;
		    String casLogoutUrl 		= InfoGlueAuthenticationFilter.casLogoutUrl;
		    
		    if(authenticatorClass.equals("org.infoglue.cms.security.CASBasicAuthenticationModule") && (forceBasicModule || fallBackToBasicBasedOnIP(request)))
		    {
		    	authenticationModule = (AuthenticationModule)Class.forName("org.infoglue.cms.security.InfoGlueBasicAuthenticationModule").newInstance();
				authenticationModule.setAuthenticatorClass("org.infoglue.cms.security.InfoGlueBasicAuthenticationModule");
				authenticationModule.setAuthorizerClass(authorizerClass);
				authenticationModule.setInvalidLoginUrl("Login!invalidLogin.action");
				authenticationModule.setLoginUrl("Login.action");
				authenticationModule.setLogoutUrl("Login!logout.action");
				authenticationModule.setServerName(serverName);
				authenticationModule.setExtraProperties(extraProperties);
				authenticationModule.setCasRenew(casRenew);
				
				if(successLoginUrl != null && successLoginUrl.length() > 0)
				{
					int index = successLoginUrl.indexOf("&ticket=");
					if(index > -1)
					{
						successLoginUrl = successLoginUrl.substring(0, index);
					}
					int index2 = successLoginUrl.indexOf("?ticket=");
					if(index2 > -1)
					{
						successLoginUrl = successLoginUrl.substring(0, index2);
					}
					logger.info("successLoginUrl:" + successLoginUrl);

					authenticationModule.setCasServiceUrl(successLoginUrl);
					authenticationModule.setSuccessLoginUrl(successLoginUrl);
				}
				else
					authenticationModule.setCasServiceUrl(casServiceUrl);
				
				authenticationModule.setCasValidateUrl(casValidateUrl);
				authenticationModule.setCasProxyValidateUrl(casProxyValidateUrl);
				authenticationModule.setCasLogoutUrl(casLogoutUrl);
				authenticationModule.setTransactionObject(transactionObject);
		    }
		    else
		    {
			    authenticationModule = (AuthenticationModule)Class.forName(authenticatorClass).newInstance();
				authenticationModule.setAuthenticatorClass(authenticatorClass);
				authenticationModule.setAuthorizerClass(authorizerClass);
				authenticationModule.setInvalidLoginUrl(invalidLoginUrl);
				authenticationModule.setLoginUrl(loginUrl);
				authenticationModule.setLogoutUrl(logoutUrl);
				authenticationModule.setServerName(serverName);
				authenticationModule.setExtraProperties(extraProperties);
				authenticationModule.setCasRenew(casRenew);
				
				if(successLoginUrl != null && successLoginUrl.length() > 0)
				{
					int index = successLoginUrl.indexOf("&ticket=");
					if(index > -1)
					{
						successLoginUrl = successLoginUrl.substring(0, index);
					}
					int index2 = successLoginUrl.indexOf("?ticket=");
					if(index2 > -1)
					{
						successLoginUrl = successLoginUrl.substring(0, index2);
					}
					logger.info("successLoginUrl:" + successLoginUrl);
	
					authenticationModule.setCasServiceUrl(successLoginUrl);
					authenticationModule.setSuccessLoginUrl(successLoginUrl);
				}
				else
					authenticationModule.setCasServiceUrl(casServiceUrl);
				
				authenticationModule.setCasValidateUrl(casValidateUrl);
				authenticationModule.setCasProxyValidateUrl(casProxyValidateUrl);
				authenticationModule.setCasLogoutUrl(casLogoutUrl);
				authenticationModule.setTransactionObject(transactionObject);
		    }
		}
		catch(Exception e)
		{
			logger.error("An error occurred when we tried to get an authenticationModule:" + e, e);
			throw new SystemException("An error occurred when we tried to get an authenticationModule: " + e.getMessage(), e);
		}
		
		return authenticationModule;
	}
	
	/**
	 * This method takes a request object and matches the clients IP (or X-Forwarded IP if allowed) against the system defined IP:s.
	 */
	private static boolean fallBackToBasicBasedOnIP(HttpServletRequest request) 
	{
		Timer t = new Timer();
		if(request == null)
			return false;
			
		String ipAddressesToFallbackToBasicAuth = CmsPropertyHandler.getIpAddressesToFallbackToBasicAuth();
		logger.info("ipAddressesToFallbackToBasicAuth: " + ipAddressesToFallbackToBasicAuth);
		if(ipAddressesToFallbackToBasicAuth == null || ipAddressesToFallbackToBasicAuth.equals("") || ipAddressesToFallbackToBasicAuth.indexOf("ipAddressesToFallbackToBasicAuth") > -1)
			return false;
		
		String[] ipAddressesToFallbackToBasicAuthArray = ipAddressesToFallbackToBasicAuth.split(",");
		List<String> list = Arrays.asList(ipAddressesToFallbackToBasicAuthArray);
				
		boolean allowXForwardedIPCheck = CmsPropertyHandler.getAllowXForwardedIPCheck();
		
		String ipRemote = request.getRemoteAddr();
	    String ipRequest = null;
	    
	    if (allowXForwardedIPCheck) 
	    {
	    	ipRemote = request.getHeader("X-Forwarded-For");
	    }
	    
	    logger.info("ipAddressesToFallbackToBasicAuth: " + ipAddressesToFallbackToBasicAuth + "\nRequest: "+ipRequest+", Remote: "+ipRemote);
		
		boolean isInList = IPMatcher.isIpInList(list, ipRemote, ipRequest);
		if(logger.isInfoEnabled())
			t.printElapsedTime("Auth took");
		return isInList;
	}

	public abstract String authenticateUser(HttpServletRequest request, HttpServletResponse response, FilterChain fc) throws Exception;

	public abstract String authenticateUser(Map request) throws Exception;

	public abstract Principal loginUser(HttpServletRequest request, HttpServletResponse response, Map status) throws Exception;

	public abstract boolean logoutUser(HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	public abstract String getLoginDialogUrl(HttpServletRequest request, HttpServletResponse response) throws Exception;

	public abstract String getAuthenticatorClass();

	public abstract void setAuthenticatorClass(String authenticatorClass);

	public abstract String getAuthorizerClass();

	public abstract void setAuthorizerClass(String authorizerClass);

	public abstract String getInvalidLoginUrl();

	public abstract void setInvalidLoginUrl(String invalidLoginUrl);

	public abstract String getSSOUserName(HttpServletRequest request) throws Exception;

	public abstract String getLoginUrl();

	public abstract void setLoginUrl(String loginUrl);

    public abstract String getSuccessLoginUrl();
    
    public abstract void setSuccessLoginUrl(String successLoginUrl);

	public abstract String getLogoutUrl();

	public abstract void setLogoutUrl(String logoutUrl);

	public abstract String getServerName();

	public abstract void setServerName(String serverName);

	public abstract Properties getExtraProperties();

	public abstract void setExtraProperties(Properties properties);
	
	public abstract String getCasRenew();

	public abstract void setCasRenew(String casRenew);

	public abstract String getCasServiceUrl();

	public abstract void setCasServiceUrl(String casServiceUrl);

	public abstract String getCasValidateUrl();

	public abstract void setCasValidateUrl(String casValidateUrl);

	public abstract String getCasProxyValidateUrl();

	public abstract void setCasProxyValidateUrl(String casProxyValidateUrl);

	public abstract String getCasLogoutUrl();

	public abstract void setCasLogoutUrl(String casLogoutUrl);

	public abstract String getCasAuthorizedProxy();

	public abstract void setCasAuthorizedProxy(String casAuthorizedProxy);

	public abstract Object getTransactionObject();

	public abstract void setTransactionObject(Object transactionObject);

	public abstract boolean enforceJ2EEContainerPrincipal();
}