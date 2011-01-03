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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;

import edu.yale.its.tp.cas.client.ProxyTicketValidator;
import edu.yale.its.tp.cas.client.Util;

/**
 * @author Mattias Bogeblad
 *
 * This authentication module authenticates an user against CAS (edu.yale.its.tp.cas)
 * which is a singe sign on service.
 */

public class CASBasicAuthenticationModule extends AuthenticationModule//, AuthorizationModule
{
    private final static Logger logger = Logger.getLogger(CASBasicAuthenticationModule.class.getName());

	private String loginUrl 			= null;
	private String logoutUrl 			= null;
	private String invalidLoginUrl 		= null;
	private String authenticatorClass 	= null;
	private String authorizerClass 		= null;
	private String successLoginUrl		= null;
	private String serverName			= null;
	private String casValidateUrl		= null;
	private String casProxyValidateUrl	= null;
	private String casServiceUrl		= null;
	private String casLogoutUrl			= null;
	private String casAuthorizedProxy 	= null;
	private String casRenew				= null;
	private Properties extraProperties	= null;
	
	/**
	 * This method handles all of the logic for checking how to handle a login.
	 */
	
	public String authenticateUser(HttpServletRequest request, HttpServletResponse response, FilterChain fc) throws Exception
	{
		String authenticatedUserName = null;

		HttpSession session = ((HttpServletRequest)request).getSession();

		String ticket = request.getParameter("ticket");
		logger.info("ticket:" + ticket);
		
		// no ticket?  abort request processing and redirect
		if (ticket == null || ticket.equals("")) 
		{
			if (loginUrl == null) 
			{
				throw new ServletException(
						"When InfoGlueFilter protects pages that do not receive a 'userName' " +
						"parameter, it needs a org.infoglue.cms.security.loginUrl " +
						"filter parameter");
			}
  
			String requestURI = request.getRequestURI();
			String queryString = "" + request.getQueryString();
			logger.info("requestURI:" + requestURI);

			String redirectUrl = "";

			if(CmsPropertyHandler.getApplicationName() == null || 
			   CmsPropertyHandler.getApplicationName().equalsIgnoreCase("deliver") ||
			   requestURI.indexOf("ViewCMSTool.action") > -1 ||
			   requestURI.indexOf("standalone") > -1 ||
			   requestURI.indexOf("workflows") > -1 ||
			   requestURI.indexOf("ViewDigitalAsset") > -1 ||
			   requestURI.indexOf("Editor") > -1 ||
			   requestURI.indexOf("binding") > -1 || 
			   queryString.indexOf("directView") > -1
			   )
			{
				if(requestURI.indexOf("?") > 0)
					redirectUrl = loginUrl + "&service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
				else
					redirectUrl = loginUrl + "?service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
	
				logger.info("redirectUrl 1:" + redirectUrl);
				response.sendRedirect(redirectUrl);
			}
			else
			{
				response.sendRedirect("index-cms.html");
			}
				
			return null;
		} 
	   	
		authenticatedUserName = authenticate(ticket);
		logger.info("authenticatedUserName:" + authenticatedUserName);
		if(authenticatedUserName == null)
		{
			String requestURI = request.getRequestURI();
			logger.info("requestURI:" + requestURI);
	
			String redirectUrl = "";
	
			if(requestURI.indexOf("?") > 0)
				redirectUrl = loginUrl + "&service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
			else
				redirectUrl = loginUrl + "?service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
		
			logger.info("redirectUrl 2:" + redirectUrl);
			response.sendRedirect(redirectUrl);
	
			return null;
		}

		//request.getSession().setAttribute("ticket", ticket);
	
		//fc.doFilter(request, response);
		return authenticatedUserName;
	}
	
	
	/**
	 * This method handles all of the logic for checking how to handle a login.
	 */
	
	public String authenticateUser(Map request) throws Exception
	{
		String authenticatedUserName = null;

		String j_userName = (String)request.get("j_username");
		String j_password = (String)request.get("j_password");
		if(j_userName != null && j_password != null)
		{
			String userName = CmsPropertyHandler.getAdministratorUserName();
			//String password = CmsPropertyHandler.getAdministratorPassword();
			
			boolean matchesRootPassword = CmsPropertyHandler.getMatchesAdministratorPassword(j_password);
			if(j_userName.equals(userName) && matchesRootPassword)
				return j_userName;
			/*
			if(j_userName.equals(userName) && j_password.equals(password))
				return j_userName;
			*/
			
			String anonymousUserName = CmsPropertyHandler.getAnonymousUser();
			String anonymousPassword = CmsPropertyHandler.getAnonymousPassword();
			
			if(j_userName.equals(anonymousUserName) && j_password.equals(anonymousPassword))
				return j_userName;
		}
		
		String ticket = (String)request.get("ticket");
		logger.info("ticket:" + ticket);
		
		// no ticket?  abort request processing and redirect
		if (ticket == null || ticket.equals("")) 
		{
			return null;
		} 
		
		authenticatedUserName = authenticate(ticket);
		if(logger.isInfoEnabled())
		{
			logger.info("authenticatedUserName:" + authenticatedUserName);
			try
			{
				throw new Exception("CAS was called from authenticateUser:" + authenticatedUserName);
			}
			catch (Exception e) 
			{
				if(logger.isInfoEnabled())
					logger.info("DEBUG:" + e.getMessage(), e);
			}
		}
		
		return authenticatedUserName;
	}
	
	
	/**
	 * This method handles all of the logic for checking how to handle a login.
	 */
	
	public String getSSOUserName(HttpServletRequest request) throws Exception
	{
		String authenticatedUserName = null;

		String ticket = request.getParameter("ticket");
		logger.info("ticket:" + ticket);
		
		if(ticket != null)
		{
			authenticatedUserName = authenticate(ticket);
			logger.info("authenticatedUserName:" + authenticatedUserName);
		}
		
		return authenticatedUserName;
	}

	/**
	 * This method handles all of the logic for checking how to handle a login.
	 */
	
	public String getLoginDialogUrl(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		String url = null;

		HttpSession session = ((HttpServletRequest)request).getSession();

		String ticket = request.getParameter("ticket");
		logger.info("ticket:" + ticket);
		
		// no ticket?  abort request processing and redirect
		if (ticket == null || ticket.equals("")) 
		{
			if (loginUrl == null) 
			{
				throw new ServletException(
						"When InfoGlueFilter protects pages that do not receive a 'userName' " +
						"parameter, it needs a org.infoglue.cms.security.loginUrl " +
						"filter parameter");
			}
  
			String requestURI = request.getRequestURI();
			logger.info("requestURI:" + requestURI);
			
			String redirectUrl = "";

			if(requestURI.indexOf("?") > 0)
				redirectUrl = loginUrl + "&service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
			else
				redirectUrl = loginUrl + "?service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
	
			logger.info("redirectUrl 3:" + redirectUrl);

			return redirectUrl;
		} 
	   	
		String authenticatedUserName = authenticate(ticket);
		logger.info("authenticatedUserName:" + authenticatedUserName);
		if(authenticatedUserName == null)
		{
			String requestURI = request.getRequestURI();
			logger.info("requestURI:" + requestURI);
	
			String redirectUrl = "";
	
			if(requestURI.indexOf("?") > 0)
				redirectUrl = loginUrl + "&service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
			else
				redirectUrl = loginUrl + "?service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
		
			logger.info("redirectUrl 4:" + redirectUrl);
			response.sendRedirect(redirectUrl);
	
			return redirectUrl;
		}

		return url;
	}
	
	
	

	/**
	 * This method authenticates against the infoglue extranet user database.
	 */
	
	private String authenticate(String ticket) throws Exception
	{
		boolean isAuthenticated = false;
		
		if(logger.isInfoEnabled())
		{
			try
			{
				throw new Exception("authenticate called with ticket:" + ticket);
			}
			catch (Exception e) 
			{
				if(logger.isInfoEnabled())
					logger.info("DEBUG:" + e.getMessage(), e);
			}
		}

	    logger.info("ticket:" + ticket);
	    TrustManager[] trustAllCerts = new TrustManager[]
		{
			new X509TrustManager() 
			{
				public java.security.cert.X509Certificate[] getAcceptedIssuers() 
				{
					return null;
				}
				
				public void checkClientTrusted
				(
					java.security.cert.X509Certificate[] certs, String authType) {
				    logger.info("Checking if client is trusted...");
				}
				/*
				public void checkServerTrusted(X509Certificate[] certs, String authType)
				{
					logger.info("Checking if server is trusted...");				
				}
				*/
				
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
                {
                    // TODO Auto-generated method stub
                    
                }
			}
		};

		HostnameVerifier hv = new HostnameVerifier() {
		    public boolean verify(String urlHostName, SSLSession session) {
		        System.out.println("Warning: URL Host: "+urlHostName+" vs. "+session.getPeerHost());
		        return true;
		    }
		};
		 
		HttpsURLConnection.setDefaultHostnameVerifier(hv);

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		
		String authenticatedUserName = null;
		
		/* instantiate a new ProxyTicketValidator */
		ProxyTicketValidator pv = new ProxyTicketValidator();
		
		/* set its parameters */
		//pv.setCasValidateUrl(casValidateUrl);
		
		String encodeValidateUrl = CmsPropertyHandler.getEncodeValidateUrl();
		String encodeCasServiceUrl = CmsPropertyHandler.getEncodeCasServiceUrl();
		
		if(ticket != null && ticket.substring(0, 2).equals("PT"))
		{
			if(encodeValidateUrl != null && encodeValidateUrl.equals("true"))
				casProxyValidateUrl = URLEncoder.encode(casProxyValidateUrl, "iso-8859-1");
				
			logger.info("setting casProxyValidateUrl: " + casProxyValidateUrl);
			pv.setCasValidateUrl(casProxyValidateUrl);
			
		}
		else
		{
			if(encodeValidateUrl != null && encodeValidateUrl.equals("true"))
				casValidateUrl = URLEncoder.encode(casValidateUrl, "iso-8859-1");

			pv.setCasValidateUrl(casValidateUrl);
			logger.info("setting casValidateUrl: " + casValidateUrl);
		}
		
		if(encodeCasServiceUrl != null && encodeCasServiceUrl.equals("true"))
			casServiceUrl = URLEncoder.encode(casServiceUrl, "iso-8859-1");
		
		logger.info("validating: " + casServiceUrl);
		pv.setService(casServiceUrl);

		pv.setServiceTicket(ticket);

		/* 
		 * If we want to be able to acquire proxy tickets (requires callback servlet to be set up  
		 * in web.xml - see below)
		 */

		/* contact CAS and validate */			
		
		try
		{
			//----------------------------------------------------------------------
			// If this is a CAS 2.0 installation the response will be in XML format.
			// If this is a CAS 1.0 the response will be a simple String and the 
			// pv.validate() method will throw an exception 
			// ("content not allowed in prolog"). If this exception is thrown we 
			// will do a further check in the catch and see if the user was 
			// verified even though the response was not in XML.
			//-----------------------------------------------------------------------
			
			pv.validate();
			
			/* if we want to look at the raw response, we can use getResponse() */
			String xmlResponse = pv.getResponse();
			logger.info("xmlResponse:" + xmlResponse);
			
			/* read the response */
			if(pv.isAuthenticationSuccesful()) 
			{
				String user = pv.getUser();
				List proxyList = pv.getProxyList();
				authenticatedUserName = pv.getUser();
				
				logger.info("The user " + user + " was authenticated successfully.");
			} 
			else 
			{
				String errorCode = pv.getErrorCode();
				String errorMessage = pv.getErrorMessage();				
				/* handle the error */
			}

			/* The user is now authenticated. */
			/* If we did set the proxy callback url, we can get proxy tickets with: */

			logger.info("proxies:\n " + pv.getProxyList()); 
		}
		catch (Exception e)
		{
			logger.info("-------------------------------------------------------------");
			logger.info("(Johans utskrift)  Felmeddelande: " + e.getMessage());
			logger.info("(Johans utskrift)  pv.getCasValidateUrl: " + pv.getCasValidateUrl());
			logger.info("(Johans utskrift)  pv.getErrorCode(): " + pv.getErrorCode());
			logger.info("(Johans utskrift)  pv.getErrorMessage: " + pv.getErrorMessage());
			logger.info("(Johans utskrift)  pv.getPgtIou: " + pv.getPgtIou());
			logger.info("(Johans utskrift)  pv.getProxyCallbackUrl: " + pv.getProxyCallbackUrl());
			logger.info("(Johans utskrift)  pv.getResponse: " + pv.getResponse());
			logger.info("(Johans utskrift)  pv.getUser: " + pv.getUser());
			logger.info("(Johans utskrift)  pv.getClass: " + pv.getClass());
			logger.info("(Johans utskrift)  pv.getProxyList: " + pv.getProxyList());
			logger.info("-------------------------------------------------------------");
			
			//--------------------------------------------------------------------
			// Check if the user was authenticated even though an exception occured.
			// This will be the case if the CAS is a 1.0 installation.
			//--------------------------------------------------------------------
			
			String casResponse = pv.getResponse();
			
			logger.info("CAS RESPONSE: " + casResponse.substring(0, 3));

			String response = casResponse.substring(0, casResponse.indexOf('\n'));
			String userId	= casResponse.substring(casResponse.indexOf('\n') + 1);
			
			if (response.equals("yes"))
			{				
				logger.info("The user " + userId + " was authenticated successfully against a CAS 1.0 installation");
				
				authenticatedUserName 	= userId.trim();
			}
			else if (response.equals("no"))
			{
				logger.info("Permission denied for the user " + userId + " against a CAS 1.0 installation");
				// Do nothing
			}
			else
			{
				//------------------------------------------------------------
				// Some other error occured and we throw the Exception again.
				//------------------------------------------------------------
				
				throw e;
			}
		}

		return authenticatedUserName;
	} 

	public Principal loginUser(HttpServletRequest request, HttpServletResponse response, Map status) throws SystemException, Exception 
	{
		Principal principal = null;
		
		String authenticatedUserName = getAuthenticatedUserName(request, response, status);
		if(authenticatedUserName != null)
		{
			principal = UserControllerProxy.getController().getUser(authenticatedUserName);
			if(principal == null)
				throw new SystemException("The CAS-authenticated user " + authenticatedUserName + " was not located in the authorization system's user database.");
		}
		
		return principal;
	}

	
	public boolean logoutUser(HttpServletRequest request, HttpServletResponse response) throws Exception 
	{
	    String referer = request.getHeader("Referer");
	    String service = getService(request);
		if(referer != null && !referer.equals(""))
		{
			if(referer.lastIndexOf("/") > 0)
				referer = referer.substring(0, referer.lastIndexOf("/"));
			
			service = referer;
		}
			  	
		response.sendRedirect(this.getCasLogoutUrl() + "?service=" + service);
		
		return true;
	}

	/**
	 * Returns either the configured service or figures it out for the current
	 * request.  The returned service is URL-encoded.
	 */
	private String getService(HttpServletRequest request) throws ServletException, Exception 
	{
		// ensure we have a server name or service name
	  	if (serverName == null && casServiceUrl == null)
			throw new ServletException("need one of the following configuration "
		  	+ "parameters: edu.yale.its.tp.cas.client.filter.serviceUrl or "
		  	+ "edu.yale.its.tp.cas.client.filter.serverName");

		if(this.casServiceUrl.equals("$currentUrl"))
		{
		  	String originalFullURL = getCurrentURL(request);
		  	
		  	String referer = request.getHeader("Referer");
		  	
		  	logger.info("originalFullURL:" + originalFullURL);
		  	this.casServiceUrl = originalFullURL;
		}

		String returnUrl = "";
	  	
		if (casServiceUrl != null && casServiceUrl.length() > 0)
		{
			String gateway 	= (String)request.getAttribute("gateway");
			if(gateway != null)
			{
				if(casServiceUrl.indexOf("?") == -1)
					casServiceUrl = casServiceUrl + "?skipSSOCheck=true";
				else
					casServiceUrl = casServiceUrl + "&skipSSOCheck=true";	
			}
			
			returnUrl = URLEncoder.encode(casServiceUrl, "UTF-8");
		}
	  	else
	  		returnUrl = Util.getService(request, serverName);
					
		return returnUrl;
		/*
		if (casServiceUrl != null && casServiceUrl.length() > 0)
			return URLEncoder.encode(casServiceUrl, "UTF-8");
	  	else
			return Util.getService(request, serverName);
		*/
	} 

	public String getCurrentURL(HttpServletRequest request)
	{
		return request.getRequestURL() + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
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

	public String getServerName()
	{
		return serverName;
	}

	public void setServerName(String string)
	{
		serverName = string;
	}

	public Properties getExtraProperties()
	{
		return this.extraProperties;
	}

	public void setExtraProperties(Properties properties)
	{
		this.extraProperties = properties;
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

	public String getCasLogoutUrl()
	{
		return casLogoutUrl;
	}

	public void setCasLogoutUrl(String casLogoutUrl)
	{
		this.casLogoutUrl = casLogoutUrl;
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
        return null;
    }

    public void setTransactionObject(Object transactionObject)
    {
    }


	/**
	 * This method handles all of the logic for checking how to handle a login.
	 */
	
	private String getAuthenticatedUserName(HttpServletRequest request, HttpServletResponse response, Map status) throws Exception
	{
		String authenticatedUserName = null;

		String ticket 	= request.getParameter("ticket");
		String gateway 	= (String)request.getAttribute("gateway");
		logger.info("ticket:" + ticket);
		logger.info("gateway:" + gateway);
		
		// no ticket?  abort request processing and redirect
		if (ticket == null || ticket.equals("")) 
		{
			if (loginUrl == null) 
			{
				throw new ServletException(
						"When InfoGlueFilter protects pages that do not receive a 'userName' " +
						"parameter, it needs a org.infoglue.cms.security.loginUrl " +
						"filter parameter");
			}
  
			String requestURI = request.getRequestURI();
			logger.info("requestURI:" + requestURI);
			
			String redirectUrl = "";

			if(requestURI.indexOf("?") > 0)
				redirectUrl = loginUrl + "&service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "") + ((gateway != null && !gateway.equals("")) ? "&gateway="+ gateway : "");
			else
				redirectUrl = loginUrl + "?service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "") + ((gateway != null && !gateway.equals("")) ? "&gateway="+ gateway : "");
			
			logger.info("redirectUrl 6:" + redirectUrl);
			
			response.sendRedirect(redirectUrl);
			status.put("redirected", new Boolean(true));
			return null;
		} 
	   	
		authenticatedUserName = authenticate(ticket);
		logger.info("authenticatedUserName:" + authenticatedUserName);
		if(authenticatedUserName == null)
		{
			String requestURI = request.getRequestURI();
			logger.info("requestURI:" + requestURI);
	
			String redirectUrl = "";
	
			if(requestURI.indexOf("?") > 0)
				redirectUrl = loginUrl + "&service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "" + ((gateway != null && !gateway.equals("")) ? "&gateway="+ gateway : ""));
			else
				redirectUrl = loginUrl + "?service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "" + ((gateway != null && !gateway.equals("")) ? "&gateway="+ gateway : ""));
		
			logger.info("redirectUrl 7:" + redirectUrl);
		
			response.sendRedirect(redirectUrl);
	
			status.put("redirected", new Boolean(true));

			return null;
		}

		return authenticatedUserName;
	}


	public String getSuccessLoginUrl()
	{
		return successLoginUrl;
	}


	public void setSuccessLoginUrl(String successLoginUrl)
	{
		this.successLoginUrl = successLoginUrl;
	}

	public boolean enforceJ2EEContainerPrincipal() 
	{
		return false;
	}

}
