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

package org.infoglue.deliver.applications.actions;

import java.net.URLEncoder;
import java.security.Principal;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.security.AuthenticationModule;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.DesEncryptionHelper;
import org.infoglue.deliver.controllers.kernel.impl.simple.ExtranetController;
import org.infoglue.deliver.util.HttpUtilities;

import webwork.action.ActionContext;

/**
 * This class is meant to be the authentication central for extranet users.
 * It has methods for login-forms, authenticate-methods and much more.
 */
 
public final class ExtranetLoginAction extends InfoGlueAbstractAction
{
	private final static Logger logger = Logger.getLogger(ExtranetLoginAction.class.getName());

	private String userName     	= null;
	private String password     	= null;
	private String errorMessage 	= "";
	private String returnAddress 	= null;
	private String referer 			= null;
	private String storeUserInfoCookie = null;
	
	public String doExecute() throws Exception 
	{
		return "success";
	}	

	public String doLoginForm() throws Exception 
	{
		return "loginForm";
	}	

	public String doNoAccess() throws Exception 
	{
		return "noAccess";
	}
	
	public String doInvalidLogin() throws Exception 
	{
		return "invalidLogin";
	}
	
	// To check access 
	public String doCheckUser() throws Exception
	{
	    Map arguments = HttpUtilities.requestToHashtable((HttpServletRequest)this.getRequest());

		if(ExtranetController.getController().getAuthenticatedPrincipal(arguments,this.getRequest())!=null)
			return "granted";
		else
			return "denied";
	}
	
	public String doAuthenticateUser() throws Exception 
	{
		boolean isAuthenticated = false;
		
		HttpServletRequest hreq  = this.getRequest();
		HttpServletResponse hres = this.getResponse();
		
		Principal principal = null;
		try
		{
		    Map arguments = HttpUtilities.requestToHashtable(hreq);
		    
			principal = ExtranetController.getController().getAuthenticatedPrincipal(arguments, hreq);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		if(principal != null) 
		{
			isAuthenticated = true;
		}

		if(isAuthenticated)
		{
			this.getHttpSession().setAttribute("infogluePrincipal", principal);
			this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
			this.getHttpSession().setAttribute("cmsUserName", principal.getName());
			
			handleCookies();
			
		    this.getResponse().sendRedirect(this.returnAddress);
		}
		else
		{
			String invalidLoginUrl = getInvalidLoginUrl();
			logger.info("invalidLoginUrl:" + invalidLoginUrl);
			errorMessage = "The logon information given was incorrect, please verify and try again.";
			if(invalidLoginUrl == null || invalidLoginUrl.equals("") || invalidLoginUrl.equals("default") || invalidLoginUrl.equals("Login!invalidLogin.action"))
			{
				return "invalidLogin";
			}
			else
			{
				String fullRedirect = invalidLoginUrl + (invalidLoginUrl.indexOf("?") > -1 ? "&" : "?") + "returnAddress=" + URLEncoder.encode(returnAddress, "UTF-8");
				logger.info("fullRedirect:" + fullRedirect);
				this.getResponse().sendRedirect(fullRedirect);
			}
		}
		
		return NONE;
	}	
	
	/**
	 * This command invalidates the current session and then calls the authentication module logout method so it can
	 * do it's stuff. Sometimes it involves redirecting the user somewhere and then we returns nothing in this method.
	 */

	public String doLogout() throws Exception
	{
		getHttpSession().invalidate();
		
		Cookie cookie_iguserid = new Cookie("iguserid", "none");
		cookie_iguserid.setPath("/");
		cookie_iguserid.setMaxAge(0); 
		getResponse().addCookie(cookie_iguserid);
	    
	    Cookie cookie_igpassword = new Cookie ("igpassword", "none");
	    cookie_igpassword.setPath("/");
	    cookie_igpassword.setMaxAge(0);
	    getResponse().addCookie(cookie_igpassword);

		AuthenticationModule authenticationModule = AuthenticationModule.getAuthenticationModule(null, null, getRequest(), false);
		boolean redirected = authenticationModule.logoutUser(getRequest(), getResponse());
		
		if(redirected)
		{
			return NONE;
		}
		else
		{
			this.getResponse().sendRedirect(this.returnAddress);
			return NONE;
		}
	}

	public String urlEncode(String string, String encoding)
	{
		String endodedString = string;
		try
		{
			endodedString = URLEncoder.encode(string, encoding);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return endodedString;
	}
	
	/**
	 * This method gets if extranet cookies are allowed or not.
	 * @return
	 */
	
	public boolean getEnableExtranetCookies()
	{
	    boolean enableExtranetCookies = false;
	    String enableExtranetCookiesString = CmsPropertyHandler.getEnableExtranetCookies();
	    if(enableExtranetCookiesString != null && enableExtranetCookiesString.equalsIgnoreCase("true"))
	    {
	        enableExtranetCookies = true;
	    }
	    
	    return enableExtranetCookies;
	}
	
	public void setUserName(String userName)
	{
		this.userName = userName;
	}
	
	public String getUserName()
	{
		return this.userName;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public String getPassword()
	{
		return this.password;
	}
	
	public void setJ_username(String userName)
	{
		this.userName = userName;
	}
	
	public String getJ_username()
	{
		return this.userName;
	}

	public void setJ_password(String password)
	{
		this.password = password;
	}
	
	public String getJ_password()
	{
		return this.password;
	}

	public String getErrorMessage()
	{
		return this.errorMessage;
	}

	public String getReturnAddress()
	{
		return this.returnAddress;
	}

	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}

	public String getReferer()
	{
		return referer;
	}

	public void setReferer(String referer)
	{
		this.referer = referer;
	}
	
	private void handleCookies() throws Exception
	{
	    DesEncryptionHelper encHelper = new DesEncryptionHelper();
		String userName = this.getRequest().getParameter("j_username");
	    String encryptedName = encHelper.encrypt(userName);
		String password = this.getRequest().getParameter("j_password");
	    String encryptedPassword = encHelper.encrypt(password);
	    
	    String encryptedNameAsBase64 = Base64.encodeBase64URLSafeString(encryptedName.getBytes("utf-8"));
	    String encryptedPasswordAsBase64 = Base64.encodeBase64URLSafeString(encryptedPassword.getBytes("utf-8"));
	    
	    //logger.info("encryptedName:" + encryptedName);
	    //logger.info("encryptedPassword:" + encryptedPassword);
	    
	    try
	    {
			String cmsBaseUrl = CmsPropertyHandler.getCmsFullBaseUrl();
			//logger.info("cmsBaseUrl:" + cmsBaseUrl);
			String[] parts = cmsBaseUrl.split("/");
			
			cmsBaseUrl = "/" + parts[parts.length -1];
			//logger.info("used cmsBaseUrl:" + cmsBaseUrl);
			
		    ServletContext servletContext = ActionContext.getServletContext().getContext(cmsBaseUrl);
		    //logger.info("servletContext:" + servletContext.getServletContextName() + ":" + servletContext.getServletNames());
		    
		    if (servletContext == null)
		    {
		    	logger.error("Could not autologin to CMS. Set cross context = true in Tomcat config.");
		    }
		    else
		    {
		    	servletContext.setAttribute(encryptedName, userName);
		    }
		   
		    //logger.info(encryptedName + "=" + userName);
		    //logger.info("After attribute:" + servletContext.getAttribute(encryptedName));
	    }
	    catch (Exception e) 
	    {	    	
	    	logger.error("Error: " + e.getMessage(), e);
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
	    
	    try
	    {
			//Cookie cookie_iguserid = new Cookie("iguserid", encryptedName.replaceAll("=", "IGEQ"));
			Cookie cookie_iguserid = new Cookie("iguserid", encryptedNameAsBase64);
			cookie_iguserid.setPath("/");
			cookie_iguserid.setMaxAge(cmsCookieTimeout); 
		    this.getResponse().addCookie(cookie_iguserid);
		    
		    //Cookie cookie_igpassword = new Cookie ("igpassword", encryptedPassword.replaceAll("=", "IGEQ"));
		    Cookie cookie_igpassword = new Cookie ("igpassword", encryptedPasswordAsBase64);
		    cookie_igpassword.setPath("/");
		    cookie_igpassword.setMaxAge(cmsCookieTimeout);
		    this.getResponse().addCookie(cookie_igpassword);
	    }
	    catch (Exception e) 
	    {
	    	logger.error("Could not set cookies:" + e.getMessage(), e);
		}
	    //END CMS COOKIE
	    
	    if(storeUserInfoCookie == null || !storeUserInfoCookie.equalsIgnoreCase("true"))
	        return;
	    
	    boolean enableExtranetCookies = getEnableExtranetCookies();
	    int extranetCookieTimeout = 43200; //30 days default
	    String extranetCookieTimeoutString = CmsPropertyHandler.getExtranetCookieTimeout();
	    if(extranetCookieTimeoutString != null)
	    {
	        try
		    {
	            extranetCookieTimeout = Integer.parseInt(extranetCookieTimeoutString.trim());
		    }
	        catch(Exception e) {}
		}
	
	    if(enableExtranetCookies )
	    {
			//Cookie cookie_userid = new Cookie("igextranetuserid", encryptedName);
			Cookie cookie_userid = new Cookie("igextranetuserid", encryptedNameAsBase64);
		    cookie_userid.setMaxAge(30 * 24 * 60 * 60); //30 days
		    this.getResponse().addCookie(cookie_userid);
		    
		    //Cookie cookie_password = new Cookie ("igextranetpassword", encryptedPassword);
		    Cookie cookie_password = new Cookie ("igextranetpassword", encryptedPasswordAsBase64);
		    cookie_password.setMaxAge(30 * 24 * 60 * 60);  //30 days
		    this.getResponse().addCookie(cookie_password);
	    }
	}
	
  	private String getInvalidLoginUrl() throws ServletException, Exception 
  	{
		String url = AuthenticationModule.getAuthenticationModule(null, null, getRequest(), false).getInvalidLoginUrl();
		
		return url;
  	}

    public void setStoreUserInfoCookie(String storeUserInfoCookie)
    {
        this.storeUserInfoCookie = storeUserInfoCookie;
    }
}