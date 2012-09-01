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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * @author Mattias Bogeblad
 *
 * This authorization module is a generic multi-source authentication module. With this you can
 * define any number of underlying authentication modules which is combined by this module. The order of lookup
 * is the same as the order the underlying modules are defined in. The module also supports ip-ranged based login.
 * 
 * To use this you state org.infoglue.cms.security.GenericCombinedAuthenticationModule as AuthenticationModule
 * and then you add the underlying modules in Extra security parameters as index based properties like this:
 * 
 * 	0.authenticationClassName=org.infoglue.cms.security.CASBasicAuthenticationModule
 *	1.authenticationClassName=org.infoglue.cms.security.InfoGlueBasicAuthenticationModule
 *  2.authenticationClassName=com.mycompany.cms.security.MyCustomAuthenticationModule
 *
 *	Then all modules in turn are asked the queries and if not found the next module is asked.
 *  If you for example want to have several JNDI-sources you are free to define the same module several times and
 *  you can differentiate what properties it should use by setting the index in front of the properties.


 *  
 *  For example if you have 2 JNDI sources and they differ in among other things 
 *  roleBase=cn=groups,dc=infoglue,dc=org you add the index in front of two lines of this:
 *  
 *  0.roleBase=cn=groups,dc=infoglue,dc=org
 *  1.roleBase=cn=internal,cn=groups,dc=companyx,dc=com
 *  
 *  That way the module with index 0 will get all properties without an index and all properties with index 0 will 
 *  override the properties without index for just that module. That way you can also have some parameters in common
 *  between two of the same modules.
 */
//TODO - document last part above

public class GenericCombinedAuthenticationModule extends AuthenticationModule
{
    private final static Logger logger = Logger.getLogger(GenericCombinedAuthenticationModule.class.getName());

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
	private String disallowedIPAddresses = null;
	private Properties extraProperties 	= null;
    
	private Database transactionObject 	= null;

	private List authorizationModules = new ArrayList();

	
	private AuthenticationModule getAuthenticationModule(String authenticationModuleClassName, int index) throws SystemException
	{
		AuthenticationModule authenticationModule = null;
		
		try
    	{
		    Properties extraProperties 	= InfoGlueAuthenticationFilter.extraProperties;
		    
			if(logger.isInfoEnabled())
				logger.info("InfoGlueAuthenticationFilter.authorizerClass:" + authenticationModuleClassName);
			
			authenticationModule = (AuthenticationModule)Class.forName(authenticationModuleClassName).newInstance();
			logger.info("authenticationModule: " + authenticationModule);

			if(logger.isInfoEnabled())
				logger.info("authenticationModule:" + authenticationModule);

			Properties localProperties = new Properties();
			Iterator propertiesIterator = this.extraProperties.keySet().iterator();
			while(propertiesIterator.hasNext())
			{
				String property = (String)propertiesIterator.next();
				String value = this.extraProperties.getProperty(property);
				if(property.startsWith("" + index + "."))
				{
					property = property.substring(2);
					logger.info("" + property + "=" + value);
					if(property.equalsIgnoreCase("invalidLoginUrl"))
						authenticationModule.setInvalidLoginUrl(value);
					else if(property.equalsIgnoreCase("loginUrl"))
						authenticationModule.setLoginUrl(value);
					else if(property.equalsIgnoreCase("logoutUrl"))
						authenticationModule.setLogoutUrl(value);
					else if(property.equalsIgnoreCase("serverName"))
						authenticationModule.setServerName(value);
					else if(property.equalsIgnoreCase("casRenew"))
						authenticationModule.setCasRenew(value);
					else if(property.equalsIgnoreCase("casServiceUrl"))
						authenticationModule.setCasServiceUrl(value);
					else if(property.equalsIgnoreCase("casValidateUrl"))
						authenticationModule.setCasValidateUrl(value);
					else if(property.equalsIgnoreCase("casProxyValidateUrl"))
						authenticationModule.setCasProxyValidateUrl(value);
					else if(property.equalsIgnoreCase("casLogoutUrl"))
						authenticationModule.setCasLogoutUrl(value);
				}
				
				localProperties.setProperty(property, value);
			}
			localProperties.setProperty("authenticatorIndex", "" + index);
							
			authenticationModule.setExtraProperties(localProperties);
			authenticationModule.setTransactionObject(this.getTransactionObject());
    	}
    	catch(Exception e)
    	{
    		logger.error("There was an error initializing the authorizerClass:" + e.getMessage(), e);
    		throw new SystemException("There was an error initializing the authorizerClass:" + e.getMessage(), e);
    	}
	   
		return authenticationModule;
	}

	
	

    /**
     * This method handles all of the logic for checking how to handle a login.
     */
    
    public String authenticateUser(HttpServletRequest request, HttpServletResponse response, FilterChain fc) throws Exception
    {
    	logger.info("authenticateUser in Generic");
    	if(logger.isInfoEnabled())
    		this.extraProperties.list(System.out);
    	
    	String authenticatedUserName = null;
		
		int i=0;
		String authenticatorClassName = this.extraProperties.getProperty("" + i + ".authenticatorClassName");
		while(authenticatorClassName != null && !authenticatorClassName.equals("") && authenticatedUserName == null)
		{
			if(logger.isInfoEnabled())
				logger.info("authenticateUser in " + authenticatorClassName);
			
			try
			{
				authenticatedUserName = getAuthenticationModule(authenticatorClassName, i).authenticateUser(request, response, fc);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			i++;
			authenticatorClassName = this.extraProperties.getProperty("" + i + ".authenticatorClassName");
		}

		return authenticatedUserName;
    }
    
    
    /**
     * This method handles all of the logic for checking how to handle a login.
     */
    
    public String authenticateUser(Map request) throws Exception
    {
    	String authenticatedUserName = null;
		
		int i=0;
		String authenticatorClassName = this.extraProperties.getProperty("" + i + ".authenticatorClassName");
		while(authenticatorClassName != null && !authenticatorClassName.equals("") && authenticatedUserName == null)
		{
			if(logger.isInfoEnabled())
				logger.info("authenticateUser in " + authenticatorClassName);
			
			try
			{
				authenticatedUserName = getAuthenticationModule(authenticatorClassName, i).authenticateUser(request);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			i++;
			authenticatorClassName = this.extraProperties.getProperty("" + i + ".authenticatorClassName");
		}

		return authenticatedUserName;
    }
    
    
	/**
	 * This method handles all of the logic for checking how to handle a login.
	 */
	
	public String getLoginDialogUrl(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		String loginDialogUrl = null;
		
		int i=0;
		String authenticatorClassName = this.extraProperties.getProperty("" + i + ".authenticatorClassName");
		while(authenticatorClassName != null && !authenticatorClassName.equals("") && loginDialogUrl == null)
		{
			if(logger.isInfoEnabled())
				logger.info("authenticateUser in " + authenticatorClassName);
			
			try
			{
				loginDialogUrl = getAuthenticationModule(authenticatorClassName, i).getLoginDialogUrl(request, response);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			i++;
			authenticatorClassName = this.extraProperties.getProperty("" + i + ".authenticatorClassName");
		}

		return loginDialogUrl;
	}
    
	public Principal loginUser(HttpServletRequest request, HttpServletResponse response, Map status) throws Exception 
	{
		Principal principal = null;
		
		int i=0;
		String authenticatorClassName = this.extraProperties.getProperty("" + i + ".authenticatorClassName");
		while(authenticatorClassName != null && !authenticatorClassName.equals("") && principal == null)
		{
			if(logger.isInfoEnabled())
				logger.info("authenticateUser in " + authenticatorClassName);
			
			try
			{
				principal = getAuthenticationModule(authenticatorClassName, i).loginUser(request, response, status);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			i++;
			authenticatorClassName = this.extraProperties.getProperty("" + i + ".authenticatorClassName");
		}

		return principal;
	}

	public boolean logoutUser(HttpServletRequest request, HttpServletResponse response) throws Exception 
	{
		boolean redirected = false;
		
		int i=0;
		String authenticatorClassName = this.extraProperties.getProperty("" + i + ".authenticatorClassName");
		while(authenticatorClassName != null && !authenticatorClassName.equals("") && redirected == false)
		{
			if(logger.isInfoEnabled())
				logger.info("authenticateUser in " + authenticatorClassName);
			
			try
			{
				redirected = getAuthenticationModule(authenticatorClassName, i).logoutUser(request, response);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			i++;
			authenticatorClassName = this.extraProperties.getProperty("" + i + ".authenticatorClassName");
		}

		return redirected;
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

	public void setDisallowedIPAddresses(String disallowedIPAddresses) 
	{
		this.disallowedIPAddresses = disallowedIPAddresses;
	}
	public String getDisallowedIPAddresses() 
	{
		return this.disallowedIPAddresses;
	}
	   
}