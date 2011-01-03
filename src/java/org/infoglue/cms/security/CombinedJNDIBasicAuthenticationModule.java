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
import java.util.Map;
import java.util.Properties;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.infoglue.cms.exception.SystemException;

/**
 * @author Mattias Bogeblad
 *
 * This authentication module authenticates an user against the ordinary infoglue database.
 */

public class CombinedJNDIBasicAuthenticationModule extends JNDIBasicAuthenticationModule
{
    private final static Logger logger = Logger.getLogger(CombinedJNDIBasicAuthenticationModule.class.getName());
    
    
	public static AuthenticationModule getFallbackAuthenticationModule(Object transactionObject, String successLoginUrl) throws SystemException
	{
		AuthenticationModule authenticationModule = null;
		
		try
		{
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

		    authenticationModule = new InfoGlueBasicAuthenticationModule();
			authenticationModule.setAuthenticatorClass(InfoGlueBasicAuthenticationModule.class.getName());
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
		catch(Exception e)
		{
			logger.error("An error occurred when we tried to get an authenticationModule:" + e, e);
			throw new SystemException("An error occurred when we tried to get an authenticationModule: " + e.getMessage(), e);
		}
		
		return authenticationModule;
	}

    /**
     * This method handles all of the logic for checking how to handle a login.
     */
    
    public String authenticateUser(HttpServletRequest request, HttpServletResponse response, FilterChain fc) throws Exception
    {
    	String authenticatedUserName = null;
 
    	try
		{
    		logger.info("authenticateUser 1");
    		request.setAttribute("disableRedirect", "true");
    		authenticatedUserName = super.authenticateUser(request, response, fc);
    		logger.info("authenticatedUserName from JNDI:" + authenticatedUserName);
        	if(authenticatedUserName == null)
        	{
        		authenticatedUserName = getFallbackAuthenticationModule(null, null).authenticateUser(request, response, fc);
        	   	logger.info("authenticatedUserName from BASIC:" + authenticatedUserName);
        	}
		}
    	catch(Exception e)
    	{
    		logger.info("NO authenticatedUserName from JNDI");
       		authenticatedUserName = getFallbackAuthenticationModule(null, null).authenticateUser(request, response, fc);
        	logger.info("authenticatedUserName from BASIC:" + authenticatedUserName);
    	}
    		
    	return authenticatedUserName;
    }
    
    
    /**
     * This method handles all of the logic for checking how to handle a login.
     */
    
    public String authenticateUser(Map request) throws Exception
    {
        String authenticatedUserName = null;
    	try
		{
    		logger.info("authenticateUser 2");
    		request.put("disableRedirect", "true");
    		authenticatedUserName = super.authenticateUser(request);
    		logger.info("authenticatedUserName from JNDI:" + authenticatedUserName);
    		if(authenticatedUserName == null)
    		{
	    		authenticatedUserName = getFallbackAuthenticationModule(null, null).authenticateUser(request);
	    		logger.info("authenticatedUserName from BASIC:" + authenticatedUserName);
    		}
		}
    	catch(Exception e)
    	{
    		logger.info("NO authenticatedUserName from JNDI");
    		authenticatedUserName = getFallbackAuthenticationModule(null, null).authenticateUser(request); 
    		logger.info("authenticatedUserName from BASIC:" + authenticatedUserName);
    	}
    	
        return authenticatedUserName;
    }
    
    
	/**
	 * This method handles all of the logic for checking how to handle a login.
	 */
	
	public String getLoginDialogUrl(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		return super.getLoginDialogUrl(request, response);
	}
    
	public Principal loginUser(HttpServletRequest request, HttpServletResponse response, Map status) throws Exception 
	{
		return null;
	}

	public boolean logoutUser(HttpServletRequest request, HttpServletResponse response) throws Exception 
	{
		return false;
	}
        
    
}