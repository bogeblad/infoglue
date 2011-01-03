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
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.exception.SystemException;

/**
 * This interface defines what a authenticationmodule has to fulfill.
 * 
 * @author Mattias Bogeblad
 */

public abstract class AuthenticationModule
{
    private final static Logger logger = Logger.getLogger(AuthenticationModule.class.getName());

	public static AuthenticationModule getAuthenticationModule(Object transactionObject, String successLoginUrl) throws SystemException
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
		catch(Exception e)
		{
			logger.error("An error occurred when we tried to get an authenticationModule:" + e, e);
			throw new SystemException("An error occurred when we tried to get an authenticationModule: " + e.getMessage(), e);
		}
		
		return authenticationModule;
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