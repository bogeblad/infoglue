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

import java.net.URLEncoder;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.SystemUserController;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * @author Mattias Bogeblad
 *
 * This authentication module authenticates an user against the ordinary infoglue database.
 */

public class InfoGlueBasicAuthenticationModule extends AuthenticationModule
{
    private final static Logger logger = Logger.getLogger(InfoGlueBasicAuthenticationModule.class.getName());

	private String loginUrl 			= null;
	private String logoutUrl			= null;
	private String invalidLoginUrl 		= null;
	private String successLoginUrl		= null;
	private String authenticatorClass 	= null;
	private String authorizerClass 		= null;
	private String serverName			= null;
	private String casServiceUrl		= null;
	private String casRenew				= null;
	private String casValidateUrl		= null;
	private String casProxyValidateUrl	= null;
	private String casLogoutUrl			= null;
	private String casAuthorizedProxy 	= null;
	private Properties extraProperties 	= null;
	private transient Database transactionObject = null;
	
	/**
	 * This method handles all of the logic for checking how to handle a login.
	 */
	
	public String authenticateUser(HttpServletRequest request, HttpServletResponse response, FilterChain fc) throws Exception
	{
		String authenticatedUserName = null;

		HttpSession session = ((HttpServletRequest)request).getSession();

		//otherwise, we need to authenticate somehow
		String userName = request.getParameter("j_username");
		String password = request.getParameter("j_password");
		
		// no userName?  abort request processing and redirect
		if (userName == null || userName.equals("")) 
		{
			if (loginUrl == null) 
			{
				throw new ServletException(
						"When InfoGlueFilter protects pages that do not receive a 'userName' " +
						"parameter, it needs a org.infoglue.cms.security.loginUrl " +
						"filter parameter");
			}
  
			String requestURI = request.getRequestURI();
			
			String requestQueryString = request.getQueryString();
			if(requestQueryString != null)
			    requestQueryString = "?" + requestQueryString;
			else
			    requestQueryString = "";
			
			logger.info("requestQueryString:" + requestQueryString);

			String redirectUrl = "";			    
			    
			if(requestURI.indexOf("?") > 0)
				redirectUrl = loginUrl + "&referringUrl=" + URLEncoder.encode(requestURI + requestQueryString, "UTF-8");
			else
				redirectUrl = loginUrl + "?referringUrl=" + URLEncoder.encode(requestURI + requestQueryString, "UTF-8");
	
			logger.info("redirectUrl:" + redirectUrl);
			response.sendRedirect(redirectUrl);

			return null;
		} 
	   	
		boolean isAuthenticated = authenticate(userName, password, new HashMap());
		logger.info("authenticated:" + isAuthenticated);
		authenticatedUserName = userName;
		
		if(!isAuthenticated)
   		{
			String referringUrl = request.getRequestURI();
			if(request.getParameter("referringUrl") != null)
				referringUrl = request.getParameter("referringUrl");
		
			String requestQueryString = request.getQueryString();
			if(requestQueryString != null)
			    requestQueryString = "?" + requestQueryString;
			else
			    requestQueryString = "";
			
			logger.info("requestQueryString:" + requestQueryString);

			String redirectUrl = "";

			if(referringUrl.indexOf("?") > 0)
				redirectUrl = invalidLoginUrl + "?userName=" + URLEncoder.encode(userName, "UTF-8") + "&errorMessage=" + URLEncoder.encode("Invalid login - please try again..", "UTF-8") + "&referringUrl=" + URLEncoder.encode(referringUrl + requestQueryString, "UTF-8");
			else
				redirectUrl = invalidLoginUrl + "?userName=" + URLEncoder.encode(userName, "UTF-8") + "?errorMessage=" + URLEncoder.encode("Invalid login - please try again..", "UTF-8") + "&referringUrl=" + URLEncoder.encode(referringUrl + requestQueryString, "UTF-8");
			
			//String redirectUrl = invalidLoginUrl + "?userName=" + URLEncoder.encode(userName, "UTF-8") + "&errorMessage=" + URLEncoder.encode("Invalid login - please try again..", "UTF-8") + "&referringUrl=" + URLEncoder.encode(referringUrl + requestQueryString, "UTF-8");
			logger.info("redirectUrl:" + redirectUrl);
			response.sendRedirect(redirectUrl);
			return null;
   		}

		//fc.doFilter(request, response);
		return authenticatedUserName;
	}
	
	
	/**
	 * This method handles all of the logic for checking how to handle a login.
	 */
	
	public String authenticateUser(Map request) throws Exception
	{
		String authenticatedUserName = null;

		//otherwise, we need to authenticate somehow
		String userName = (String)request.get("j_username");
		String password = (String)request.get("j_password");

		logger.info("authenticateUser:userName:" + userName);
		
		// no userName?  abort request processing and redirect
		if (userName == null || userName.equals("")) 
		{
			return null;
		} 
	   	
		boolean isAuthenticated = authenticate(userName, password, new HashMap());
		logger.info("authenticated:" + isAuthenticated);
		
		if(!isAuthenticated)
   		{
			return null;
   		}

		authenticatedUserName = userName;
		
		return authenticatedUserName;
	}
	
	/**
	 * This method handles all of the logic for checking how to handle a login.
	 */
	
	public String getLoginDialogUrl(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		String returnAddress = null;

	    String referer = request.getHeader("Referer");
		
		if(referer == null || referer.indexOf("ViewStructureToolToolBar.action") != -1)
			referer = "/"; 

		logger.info("successLoginUrl:" + successLoginUrl);
		if(successLoginUrl != null)
		{
			returnAddress = successLoginUrl;
		}
		else
		{
			returnAddress = request.getRequestURL().toString() + "?" + request.getQueryString() + "&referer=" + URLEncoder.encode(referer, "UTF-8") + "&date=" + System.currentTimeMillis();
		}
		
		logger.info("returnAddress:" + returnAddress);
		return request.getContextPath() + "/ExtranetLogin!loginForm.action?returnAddress=" + URLEncoder.encode(returnAddress, "UTF-8");
	}
	
	/**
	 * This method authenticates against the infoglue extranet user database.
	 */
	
	private boolean authenticate(String userName, String password, Map parameters) throws Exception
	{
		boolean isAuthenticated = false;
		
		String administratorUserName = CmsPropertyHandler.getAdministratorUserName();
		//String administratorPassword = CmsPropertyHandler.getAdministratorPassword();
		//boolean isAdministrator = (userName.equalsIgnoreCase(administratorUserName) && password.equalsIgnoreCase(administratorPassword)) ? true : false;

		boolean matchesRootPassword = CmsPropertyHandler.getMatchesAdministratorPassword(password);
		boolean isAdministrator = (userName.equalsIgnoreCase(administratorUserName) && matchesRootPassword) ? true : false;

		if(CmsPropertyHandler.getUsePasswordEncryption())
		{
			try
			{
				byte[] encryptedPassRaw = DigestUtils.sha(password);
				String encryptedPass = new String(new Base64().encode(encryptedPassRaw), "ASCII");
				password = encryptedPass;
			}
			catch (Exception e) 
			{
				logger.error("Error encrypting password before auth:" + e.getMessage());
			}
		}
		
		if(this.transactionObject != null)
		{
			if(isAdministrator || SystemUserController.getController().getSystemUserVO(this.transactionObject, userName, password) != null)
				isAuthenticated = true;
		}
		else
		{
			if(isAdministrator || SystemUserController.getController().getSystemUserVO(userName, password) != null)
				isAuthenticated = true;		    
		}

		return isAuthenticated;
	}

	public Principal loginUser(HttpServletRequest request, HttpServletResponse response, Map status) throws Exception 
	{
		return null;
	}

	public boolean logoutUser(HttpServletRequest request, HttpServletResponse response) throws Exception 
	{
		String returnAddress = null;

		logger.info("loginUrl:" + this.loginUrl);
		logger.info("logoutUrl:" + this.logoutUrl);
		logger.info("successLoginUrl:" + this.successLoginUrl);

		if(this.logoutUrl != null && this.logoutUrl.equals("Login!logout.action"))
		{
		    String referer = request.getHeader("Referer");
			
			if(referer == null || referer.indexOf("ViewStructureToolToolBar.action") != -1)
				referer = "/"; 
	
			logger.info("successLoginUrl:" + successLoginUrl);
			if(successLoginUrl != null)
			{
				returnAddress = "" + successLoginUrl;
			}
			else
			{
				returnAddress = "" + request.getContextPath() + "/ViewCMSTool.action";
			}
				
			logger.info("returnAddress:" + returnAddress);
			//String redirectAddress = request.getContextPath() + "/ExtranetLogin!loginForm.action?returnAddress=" + URLEncoder.encode(returnAddress, "UTF-8");
	
			String redirectAddress = "" + this.loginUrl + "?referringUrl=" + URLEncoder.encode(returnAddress, "utf-8");
			logger.info("redirectAddress in InfoGlueBasicAuth module:" + returnAddress);
			response.sendRedirect(returnAddress);
			
			return true;
		}
		else
		{
			if(CmsPropertyHandler.getApplicationName().equals("cms"))
			{
				String redirectAddress = "" + this.logoutUrl + "?returnAddress=" + URLEncoder.encode(request.getContextPath() + "/ViewCMSTool.action", "utf-8");
				logger.info("redirectAddress in InfoGlueBasicAuth module:" + redirectAddress);
				response.sendRedirect(redirectAddress);
				
				return true;
			}
			else
				return false;
		}
	}


	public String getAuthenticatorClass()
	{
		return authenticatorClass;
	}

	public void setAuthenticatorClass(String authenticatorClass)
	{
		this.authenticatorClass = authenticatorClass;
	}

	public String getAuthorizerClass()
	{
		return authorizerClass;
	}

	public void setAuthorizerClass(String authorizerClass)
	{
		this.authorizerClass = authorizerClass;
	}

	public String getInvalidLoginUrl()
	{
		return invalidLoginUrl;
	}

	public void setInvalidLoginUrl(String invalidLoginUrl)
	{
		this.invalidLoginUrl = invalidLoginUrl;
	}

	public String getLoginUrl()
	{
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl)
	{
		this.loginUrl = loginUrl;
	}

	public String getLogoutUrl()
	{
		return logoutUrl;
	}

	public void setLogoutUrl(String logoutUrl)
	{
		this.logoutUrl = logoutUrl;
	}

    public String getSuccessLoginUrl()
    {
        return successLoginUrl;
    }
    
    public void setSuccessLoginUrl(String successLoginUrl)
    {
        this.successLoginUrl = successLoginUrl;
    }

	public String getServerName()
	{
		return this.serverName;
	}

	public void setServerName(String serverName)
	{
		this.serverName = serverName;
	}

	public Properties getExtraProperties()
	{
		return extraProperties;
	}

	public void setExtraProperties(Properties extraProperties)
	{
		this.extraProperties = extraProperties;
	}
	
	public String getCasRenew()
	{
		return casRenew;
	}

	public void setCasRenew(String casRenew)
	{
		this.casRenew = casRenew;
	}

	public String getCasServiceUrl()
	{
		return casServiceUrl;
	}

	public void setCasServiceUrl(String casServiceUrl)
	{
		this.casServiceUrl = casServiceUrl;
	}

	public String getCasValidateUrl()
	{
		return casValidateUrl;
	}

	public void setCasValidateUrl(String casValidateUrl)
	{
		this.casValidateUrl = casValidateUrl;
	}

	public String getCasProxyValidateUrl()
	{
		return casProxyValidateUrl;
	}

	public void setCasProxyValidateUrl(String casProxyValidateUrl)
	{
		this.casProxyValidateUrl = casProxyValidateUrl;
	}

	public String getCasAuthorizedProxy()
	{
		return casAuthorizedProxy;
	}

	public void setCasAuthorizedProxy(String casAuthorizedProxy)
	{
		this.casAuthorizedProxy = casAuthorizedProxy;
	}

    public Object getTransactionObject()
    {
        return this.transactionObject;
    }

    public void setTransactionObject(Object transactionObject)
    {
        this.transactionObject = (Database)transactionObject; 
    }


	public String getCasLogoutUrl() 
	{
		return casLogoutUrl;
	}


	public void setCasLogoutUrl(String casLogoutUrl) 
	{
		this.casLogoutUrl = casLogoutUrl;
	}

	public boolean enforceJ2EEContainerPrincipal() 
	{
		return false;
	}
	
	/**
	 * This method handles all of the logic for checking how to handle a login.
	 */
	
	public String getSSOUserName(HttpServletRequest request) throws Exception
	{
		return null;
	}
}
