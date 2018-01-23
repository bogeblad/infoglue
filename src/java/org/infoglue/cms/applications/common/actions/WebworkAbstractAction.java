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

package org.infoglue.cms.applications.common.actions;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.Session;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.controllers.kernel.impl.simple.LabelController;
import org.infoglue.cms.entities.management.SystemUser;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConfigurationError;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.cms.util.ChangeNotificationController;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.StringManager;
import org.infoglue.cms.util.StringManagerFactory;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
import org.infoglue.deliver.util.BrowserBean;
import org.infoglue.deliver.util.ThreadMonitor;
import org.infoglue.deliver.util.Timer;
import org.jfree.util.Log;

import webwork.action.Action;
import webwork.action.CommandDriven;
import webwork.action.ResultException;
import webwork.action.ServletRequestAware;
import webwork.action.ServletResponseAware;

/**
 * @author Mattias Bogeblad
 * @author Frank Febbraro (frank@phase2technology.com)
 */

public abstract class WebworkAbstractAction implements Action, ServletRequestAware, ServletResponseAware, CommandDriven 
{
	private static final String  VIEW_MESSAGE_CENTER_ACTION = "ViewMessageCenter";
	private static final String  UPDATE_CACHE_ACTION = "UpdateCache";
	private static final String  USER_ACTION_FORMAT  = "%-10s %-25s %-35s %-30s %s";
	private static final Pattern USER_ACTION_PATTERN = Pattern.compile("^https?://[^/]+/(.*)/([^/!.]*)(!([^.]+))?.*$");
	private static final int     ACTION_GROUP_INDEX  = 2;
	private static final int     CONTEXT_GROUP_INDEX = 1;
	private static final int     METHOD_GROUP_INDEX  = 4;
	private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i).*(password).*");
	private static final int MAX_PARAMETER_VALUE_LENGTH = 50;
	private static final String GA_CMS_ID ="ga.cms.id";
	private static final String GA_CMS_URL ="ga.cms.url";
	private static final String GA_EXLUDED_ACTIONS ="ga.cms.excluded.actions";
	
	private final static Logger logger = Logger.getLogger(WebworkAbstractAction.class.getName());
	private final static Logger USER_ACTION_LOGGER = Logger.getLogger("User Action");

	private final String ACCESS_DENIED = "accessDenied";

	private Error error;
	private Errors errors = new Errors();
	private List<LinkBean> linkBeans = new ArrayList<LinkBean>();

	private HttpServletRequest request;
	private HttpServletResponse response;
	private String commandName;


	/**
	 *
	 */
	public Error getError() 
	{
		if(logger.isInfoEnabled())
			logger.info("Fetching error from error-template:" + this.error);

		return this.error;
	}

	/**
	 *
	 */
	public Errors getErrors() 
	{
		if(logger.isInfoEnabled())
			logger.info("Errors:" + this.errors);

		return errors;
	}

	/**
	 *
	 */
	public List<LinkBean> getLinkBeans() 
	{
		return linkBeans;
	}

	/**
	 *
	 */
	public String doDefault() throws Exception 
	{
		return INPUT;
	}

	public abstract void protectFromCSSAttacks(String actionName, String commandName) throws Exception;


	/**
	 * This is the main execution point for any webwork action.
	 * Lately we added statistics on each action for debugging and optimization purposes.
	 */
	public String execute() throws Exception 
	{
		logUserAction(Level.INFO);
		
		Timer t = new Timer();

		String result = "";

		ThreadMonitor tm = new ThreadMonitor(10000L, request, "Action took to long", false);
		/*
    	ThreadMonitor tm = null;
    	if(getRequest().getParameter("trackThread") != null && getRequest().getParameter("trackThread").equals("true"))
    		tm = new ThreadMonitor(10000L, request, "Action took to long", false);
		 */

		try 
		{
			//This registers what the system needs to know about the port etc to be able to call update cache actions etc. 
			CmsPropertyHandler.registerDefaultSchemeAndPort(getRequest());

			protectFromCSSAttacks(this.getClass().getName(), this.commandName);

			result = isCommand() ? invokeCommand() : doExecute();
			setStandardResponseHeaders();

			long elapsedTime = t.getElapsedTime();
			long memoryDiff = t.getMemoryDifferenceAsMegaBytes();
			if(elapsedTime > 5000 || memoryDiff > 100)
			{
				logger.warn("The " + CmsPropertyHandler.getApplicationName() + " request: " + this.getUnencodedCurrentURIWithParameters() + " took " + elapsedTime + " ms to render and seems to have allocated " + memoryDiff + " MB of memory)");
			}
		} 
		catch(ResultException e) 
		{
			logger.error("ResultException " + e.getMessage());
			logger.warn("ResultException " + e.getMessage(), e);
			result = e.getResult();
		} 
		catch(AccessConstraintException e) 
		{
			logger.info("AccessConstraintException " + e, e);
			setErrors(e);
			result = ACCESS_DENIED;
		} 
		catch(ConstraintException e) 
		{
			logger.info("ConstraintException " + e, e);
			List<LinkBean> linkBeans = e.getLinkBeans();
			if(linkBeans != null && linkBeans.size() > 0)
			{
				getResponse().sendRedirect(linkBeans.get(0).getActionURL());
				result = NONE;
			}
			else
			{
				setErrors(e);
				if(e.getResult() != null && !e.getResult().equals(""))
					result = e.getResult();
				else
					result = INPUT;
			}
		} 
		catch(Bug e) 
		{
			logger.error("Bug " + e.getMessage());
			logger.warn("Bug  " + e.getMessage(), e);
			setError(e, e.getCause());
			result = ERROR;
		} 
		catch(ConfigurationError e) 
		{
			logger.error("ConfigurationError " + e.getMessage());
			logger.warn("ConfigurationError " + e.getMessage(), e);
			setError(e, e.getCause());
			result = ERROR;
		} 
		catch(SystemException e) 
		{
			if(e.getMessage().indexOf("correct checksum") > -1)
			{
				if(getUnencodedCurrentUrl().indexOf("Confirm.action") > -1)
					logger.warn("SystemException on url: " + getUnencodedCurrentUrl() + " with " + this.getRequest().getParameter("yesDestination") + "\n" + e.getMessage(), e);				
				else
					logger.warn("SystemException on url: " + getUnencodedCurrentUrl() + "\n" + e.getMessage(), e);				
			}
			else
			{
				logger.error("SystemException: " + e.getMessage());
				logger.warn("SystemException: " + e.getMessage(), e);
			}
			setError(e, e.getCause());
			result = ERROR;
		} 
		catch(Throwable e) 
		{
			logger.error("Throwable " + e.getMessage());
			logger.warn("Throwable " + e.getMessage(), new Exception(e));
			final Bug bug = new Bug("Uncaught exception!", e);
			setError(bug, bug.getCause());
			result = ERROR;
		}
		finally
		{
			if(tm != null && !tm.getIsDoneRunning())
				tm.done();
		}

		try
		{
			if(CmsPropertyHandler.getApplicationName().equalsIgnoreCase("cms"))
				ChangeNotificationController.getInstance().notifyListeners();
		}
		catch(Exception e)
		{
			logger.error("Error notifying listener " + e.getMessage());
			logger.warn("Error notifying listener " + e.getMessage(), e);
		}

		return result;
	}

	/**
	 * This method returns the url to the current page.
	 * Could be used in case of reload for example or for logging reasons.
	 */

	public String getCurrentUrl() throws Exception
	{
		String urlBase = getRequest().getRequestURL().toString();
		String urlParameters = getRequest().getQueryString();

		return URLEncoder.encode(urlBase + "?" + urlParameters, "UTF-8");
	}

	/**
	 * This method returns the url to the current page.
	 * Could be used in case of reload for example or for logging reasons.
	 */

	public String getUnencodedCurrentUrl() throws Exception
	{
		String urlBase = getRequest().getRequestURL().toString();
		String urlParameters = getRequest().getQueryString();
		if(urlBase.contains("/cms/common/viewAccessRights.vm"))
			urlBase = urlBase.replaceFirst("/cms/common/viewAccessRights.vm", "/ViewAccessRights!V3.action");

		return urlBase + (urlParameters != null ? "?" + urlParameters : "");
	}

	/**
	 * This method returns the URI to the current page.
	 */

	public String getUnencodedCurrentURI() throws Exception
	{
		String urlBase = getRequest().getRequestURI().toString();

		return urlBase;
	}

	/**
	 *
	 */
	public void setCommand(String commandName) 
	{
		this.commandName = commandName;
	}

	/**
	 *
	 */
	public void setServletRequest(HttpServletRequest request) 
	{
		this.request = request;
	}



	/**
	 *
	 */
	public void setServletResponse(HttpServletResponse response) 
	{
		this.response = response;
	}



	/**
	 *
	 */
	public void setError(Throwable throwable, Throwable cause) 
	{
		this.error = new Error(throwable, cause);
	}


	/**
	 *
	 */
	private void setErrors(ConstraintException exception)
	{
		final Locale locale = getSession().getLocale();
		for (ConstraintException ce = exception; ce != null; ce = ce.getChainedException())
		{
			final String fieldName = ce.getFieldName();
			final String errorCode = ce.getErrorCode();
			final String extraInformation = ce.getExtraInformation();
			String localizedErrorMessage 		 = "";
			logger.warn("Error class: " + ce.getClass().getName());
			if(errorCode.length() > 5)
				localizedErrorMessage = errorCode;
			else
				localizedErrorMessage = getLocalizedErrorMessage(locale, errorCode);
			//System.out.println("localizedErrorMessage:" + localizedErrorMessage);

			//final String localizedErrorMessage = getLocalizedErrorMessage(locale, errorCode);
			getErrors().addError(fieldName, localizedErrorMessage + (extraInformation.length() > 0 ? " " + extraInformation + " " : ""));

			getLinkBeans().addAll(ce.getLinkBeans());
		}
		logger.debug(getErrors().toString());
	}

	/**
	 * <todo>Move to a ConstraintExceptionHelper class?</todo>
	 * @throws NullPointerException		If any of the arguments are null
	 * @throws MissingResourceException	If no key matches the given <em>errorCode</em>
	 */
	protected String getLocalizedErrorMessage(Locale locale, String errorCode) throws NullPointerException, MissingResourceException
	{
		// <todo>fetch packagename from somewhere</todo>
		StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.entities", locale);

		// check if a specific error message exists - <todo/>
		// nah, use the general error message
		return stringManager.getString(errorCode);
	}


	public String getLocalizedString(Locale locale, String key) 
	{
		StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);

		return stringManager.getString(key);
	}

	public String getLocalizedString(Locale locale, String key, Object arg1) 
	{
		return LabelController.getController(locale).getLocalizedString(locale, key, arg1);
	}

	public String getLocalizedString(Locale locale, String key, Object[] parameters) 
	{
		return LabelController.getController(locale).getLocalizedString(locale, key, parameters);
	}

	/**
	 *
	 */

	private boolean isCommand() 
	{
		return this.commandName != null && commandName.trim().length() > 0 && (this instanceof CommandDriven);
	}

	/**
	 * This is a complement to the normal webwork execution which allows for a command-based execution of actions. 
	 */

	private String invokeCommand() throws Exception 
	{
		//Timer t = new Timer();

		//ThreadMonitor tm = null;
		//if(getRequest().getParameter("trackThread") != null && getRequest().getParameter("trackThread").equals("true"))
		//	tm = new ThreadMonitor(10L, request, "Command took to long", false);
		ThreadMonitor tm = new ThreadMonitor(10000L, request, "Command took to long", false);

		final StringBuffer methodName = new StringBuffer("do" + this.commandName);
		methodName.setCharAt(2, Character.toUpperCase(methodName.charAt(2)));

		String result = "";

		try 
		{
			protectFromCSSAttacks(this.getClass().getName(), this.commandName);

			final Method method = getClass().getMethod(methodName.toString(), new Class[0]);
			result = (String) method.invoke(this, new Object[0]);
		} 
		catch(Exception ie) 
		{
			if(ie.getMessage() != null)
				logger.error("Exception in top action:" + ie.getMessage(), ie);

			try
			{
				throw ie.getCause();
			}
			catch(ResultException e)
			{
				logger.error("ResultException " + e, e);
				result = e.getResult();
			}
			catch(AccessConstraintException e) 
			{
				logger.info("AccessConstraintException " + e, e);
				setErrors(e);
				result = ACCESS_DENIED;
			} 
			catch(ConstraintException e) 
			{
				logger.info("ConstraintException " + e, e);
				List<LinkBean> linkBeans = e.getLinkBeans();
				if(linkBeans != null && linkBeans.size() > 0)
				{
					logger.info("linkBean:" + linkBeans.get(0).getActionURL());
					getResponse().sendRedirect(linkBeans.get(0).getActionURL());
				}
				else
				{
					setErrors(e);
					if(e.getResult() != null && !e.getResult().equals(""))
						result = e.getResult();
					else
						result = INPUT;
				}
			} 
			catch(Bug e) 
			{
				logger.error("Bug " + e.getMessage(), e);
				setError(e, e.getCause());
				result = ERROR;
			} 
			catch(ConfigurationError e) 
			{
				logger.error("ConfigurationError " + e);
				setError(e, e.getCause());
				result = ERROR;
			} 
			catch(SystemException e) 
			{
				if(e.getMessage() != null && e.getMessage().indexOf("correct checksum") > -1)
				{
					if(getUnencodedCurrentUrl().indexOf("Confirm.action") > -1)
						logger.warn("SystemException on url: " + getUnencodedCurrentUrl() + " with " + this.getRequest().getParameter("yesDestination") + "\n" + e.getMessage(), e);				
					else
						logger.warn("SystemException on url: " + getUnencodedCurrentUrl() + "\n" + e.getMessage(), e);				
				}
				else
					logger.error("SystemException: " + e.getMessage(), e);
				setError(e, e.getCause());
				result = ERROR;
			} 
			catch(Throwable e) 
			{
				logger.error("Throwable " + e.getMessage(), e);
				final Bug bug = new Bug("Uncaught exception!", e);
				setError(bug, bug.getCause());
				result = ERROR;
			}	        
		}
		finally
		{
			if(tm != null && !tm.getIsDoneRunning())
				tm.done();
		}

		try
		{
			if(!this.getRequest().getRequestURI().contains("UpdateCache!test"))
			{
				//System.out.println("URL:" + this.getRequest().getRequestURI());
				ChangeNotificationController.getInstance().notifyListeners();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return result;

	}

	public void setStandardResponseHeaders() 
	{
		try
		{
			Map<String, String> standardHeaders = CmsPropertyHandler.getStandardResponseHeaders();
			HttpServletResponse response = getResponse();
			for (Map.Entry<String, String> entry : standardHeaders.entrySet())
			{
				response.setHeader(entry.getKey(), entry.getValue());
			}
			response.setHeader("Cache-Control", "no-cache");
		}
		catch (Exception e) 
		{
			logger.warn("Could not set headers:" + e.getMessage());
		}
	}

	public final String getRoot() 
	{
		return request.getContextPath();
	}

	/**
	 * This method returns a logged in principal if existing.
	 */

	public final InfoGluePrincipal getInfoGluePrincipal()
	{
		return getSession().getInfoGluePrincipal();
	}

	/**
	 * Subclasses implement this
	 */

	protected abstract String doExecute() throws Exception;

	/**
	 * 
	 */

	public final BrowserBean getBrowserBean()
	{
		BrowserBean browserBean = new BrowserBean();
		browserBean.setRequest(this.request);
		return browserBean;
	}

	/**
	 * Returns the HttpServletRequest
	 * TODO: Hide the implementation behind WebWorks abstractions in ActionContext
	 */
	protected final HttpServletRequest getRequest()
	{
		return this.request;
	}

	/** 
	 * Returns the HttpServletResponse
	 * TODO: Hide the implementation behind WebWorks abstractions in ActionContext
	 */

	protected final HttpServletResponse getResponse()
	{
		return this.response;
	}

	/**
	 * Returns the HttpSession
	 * TODO: Hide the implementation behind WebWorks abstractions in ActionContext
	 */
	protected final HttpSession getHttpSession()
	{
		return getRequest().getSession();
	}

	/**
	 * Use the ActionContext to initialize the Session and remove the dependence
	 * on HTTP and the Servlet Spec. Makes it much easier for testing.
	 */

	public final Session getSession() 
	{
		return new Session(getHttpSession());
	}

	public final String getSessionId()
	{
		return getHttpSession().getId();
	}

	/**
	 * This method returns the URI to the current page.
	 */

	public String getUnencodedCurrentURIWithParameters() throws Exception
	{
		String urlBase = getRequest().getRequestURI().toString();
		String queryString = "";
		Enumeration parameterNames = getRequest().getParameterNames();
		while(parameterNames.hasMoreElements())
		{
			String name = (String)parameterNames.nextElement();
			queryString += "&" + name + "=" + getRequest().getParameter(name);
		}
		if(queryString != null && !queryString.equals(""))
			urlBase = urlBase + "?" + queryString.substring(1);

		return urlBase;
	}

	private void logUserAction(Level level)
	{
		// An error in this log method must not propagate since it 
		// will halt the execution of the current action
		String action = "unknown action";
		try
		{
			
				// Default values
			
				String method = "";
				String context = "unknown context";
				
				final String userName = getOptionalUserName();
				String url = request.getRequestURL().toString();
				String parameters = getParametersString();
				
				if (url != null)
				{				
					Matcher matcher = USER_ACTION_PATTERN.matcher(url);
					if (matcher.find())
					{
						String contextMatch = matcher.group(CONTEXT_GROUP_INDEX);
						if (contextMatch != null)
						{
							context = contextMatch;
						}
						String actionMatch = matcher.group(ACTION_GROUP_INDEX);
						if (actionMatch != null)
						{
							action = actionMatch;
						}
						String methodMatch = matcher.group(METHOD_GROUP_INDEX);
						if (methodMatch != null)
						{
							method = methodMatch;
						}
					}
				}
				
				final String tid = getGeneralSetting(GA_CMS_ID, null);
				final String gaUrl = getGeneralSetting(GA_CMS_URL, null);
				final String actionFinal = action;
	    		boolean isExcludedAction = false;
	    		
    			String excludedActions = getGeneralSetting(GA_EXLUDED_ACTIONS, null);
	    		if (excludedActions != null) {	
	    			isExcludedAction = excludedActions.matches(".*(^|,)" + action + "(,|$).*");
	    		}

	    		
	    		
				if (!isExcludedAction && action != null && tid != null && !tid.equalsIgnoreCase("") && gaUrl != null && !gaUrl.equalsIgnoreCase("")) {
					Thread thread = new Thread(new Runnable() {
						public void run() {
						
								sendToGA(actionFinal, userName, tid, gaUrl);
							
						}
					});
					thread.start(); 
					
				}
	    		
				logger.debug("action: " + action + ", method: " + method + ", context: " + context + ", userName: " + userName + ", parameters: " + parameters);

				// Some actions are called too often, exclude them.
				if (!action.equals(UPDATE_CACHE_ACTION) && !action.equals(VIEW_MESSAGE_CENTER_ACTION))
				{
					// For all other actions, log to the USER_ACTION_LOGGER
					USER_ACTION_LOGGER.log(level, String.format(USER_ACTION_FORMAT, userName, context, action, method, parameters));
				}
			
		} catch (Throwable t)
		{
			logger.error("Error thrown in log method", t); 
		}
	}
	
	private static String getGeneralSetting(String key, String defaultValue) {
		Properties generalSettings = CmsPropertyHandler.getGeneralSettings(false);
		if (generalSettings != null) {
			return generalSettings.getProperty(key, defaultValue);
		} else {
			return defaultValue;
		}
	}
	public void sendToGA(String action, String userName, String tid, String gaUrl) {

		String urlParameters  = "";
		HttpSession session = request.getSession();
		if (session != null && (session.getAttribute("GASession") == null || session.getAttribute("GASession").toString().equalsIgnoreCase(""))) {
			Double random = Math.random();
			session.setAttribute("GASession", random.toString());
		} 
		InfoGluePrincipal igPrincipal = getInfoGluePrincipal();
		String role = "Uknown";
		if (igPrincipal != null && igPrincipal.getRoles() != null && igPrincipal.getRoles().size() > 0) {
			role = igPrincipal.getRoles().get(0).getDisplayName();

		}

		urlParameters = "v=1&tid=" + tid + "&cid=" + session.getAttribute("GASession") + "&t=event&ec=" + role + "&ea=" + action;
		byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
		String request        = gaUrl;
		URL url;
		try {
			url = new URL( request );

			HttpURLConnection conn= (HttpURLConnection) url.openConnection();
			
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setUseCaches( false );
			
			try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
			   wr.write( postData );
			}
			
			} catch (IOException e) {
		
			logger.warn("Could send analytics data for action:" + action + " and data:" + postData);
		}
		
	}

	static public String join(String delimiter, List<String> list)
	{
	   StringBuilder sb = new StringBuilder();
	   boolean first = true;
	   
	   for (String elem : list)
	   {
	      if (first) 
	      {
	         first = false;
	      }
	      else
	      {
	         sb.append(delimiter);
	      }
	      
	      sb.append(elem);
	   }
	   return sb.toString();
	}
	
	
	private String getParametersString()
	{
		@SuppressWarnings("unchecked")
		Map<String, String[]> parameterMap = getRequest().getParameterMap();
		List<String> params = new LinkedList<String>();
		
		if(parameterMap.size() > 0)
		{
			Set<String> keys = (Set<String>) parameterMap.keySet();
			
			for(String key : keys)
			{
				String value;
				if (PASSWORD_PATTERN.matcher(key).matches()) 
				{
					value = "______";
				}
				else
				{
					value = join(",", Arrays.asList(parameterMap.get(key)));
				}
				
				if (value.length() > MAX_PARAMETER_VALUE_LENGTH)
				{
					value = value.substring(0, MAX_PARAMETER_VALUE_LENGTH);
				}
				
				String param = key + "=" + value;
				
				params.add(param);
			}
			
		}
		
		return join("&", params);
	}

	private String getOptionalUserName() {
		String name = "????????";
		Session session = getSession();
		Principal user = null;

		// Try to fetch the user from the session
		if (session != null) {
			logger.debug("Got session");
			user = session.getInfoGluePrincipal();
			logger.debug("Got InfogluePrincipal from session: " + user);
		}

		// If we got no user, try to get it from the HTTP session
		if (user == null) {
			logger.debug("Got HTTP session");
			HttpSession httpSession = this.getHttpSession();
			if (httpSession != null) {
				user = (Principal) httpSession.getAttribute("infogluePrincipal");
				logger.debug("Got InfogluePrincipal from HTTP session: " + user);
			}
		}

		// If we still have got no user, try to get a TemplateController from the request
		// to get the user from there
		if (user == null) {
			HttpServletRequest request = getRequest();
			if (request != null) {
				logger.debug("Got request");

				TemplateController controller = (TemplateController) request.getAttribute("org.infoglue.cms.deliver.templateLogic");
				if (controller != null) {
					logger.debug("Got controller");

					user = controller.getPrincipal();
					logger.debug("Got InfogluePrincipal from request: " + user);
				}
			}
		}

		if (user != null) {
			logger.debug("Got user");

			// We have a user, get the name of the user
			name = user.getName();
			if (user instanceof InfoGluePrincipal && ((InfoGluePrincipal) user).getIsAdministrator()) {
				name = name + "*";
			}
		} else if (session != null) {
			// We have no principal user, fall back on a SystemUser if available
			SystemUser systemUser = session.getUser();
			if (systemUser != null) {
				logger.debug("Got SystemUser from session: " + systemUser);

				name = "(" + systemUser.getUserName() + ")";
			} else {
				// There might be a username in the parameters, let's try to use that, 
				// but mark it as potentially being fake
				String paramName = request.getParameter("j_username");
				if (paramName == null) {
					paramName = request.getParameter("cmsUserName");
				}
				if (paramName != null) {
					name = paramName + "?";
				}
			}
		}

		return name;
	}
}
