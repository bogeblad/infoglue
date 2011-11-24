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

import java.awt.Color;
import java.math.BigInteger;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.databeans.InfoglueTool;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.InfoGluePrincipalControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptorController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ThemeController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.InterceptorVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.AuthenticationModule;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.interceptors.InfoGlueInterceptor;
import org.infoglue.cms.services.AdminToolbarService;
import org.infoglue.cms.services.AdminToolsService;
import org.infoglue.cms.services.InterceptionService;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.deliver.controllers.kernel.impl.simple.ExtranetController;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.HttpHelper;
import org.infoglue.deliver.util.graphics.ColorHelper;

import webwork.action.ActionContext;
import webwork.config.Configuration;

/**
 * @author Mattias Bogeblad
 *
 * This is an abstract action used for all InfoGlue actions. Just to not have to put to much in the WebworkAbstractAction.
 */

public abstract class InfoGlueAbstractAction extends WebworkAbstractAction
{
    private final static Logger logger = Logger.getLogger(InfoGlueAbstractAction.class.getName());

	private final static AdminToolbarService toolbarService = AdminToolbarService.getService();

    protected String colorScheme = null; 
    
	/**
	 * This method lets the velocity template get hold of all actions inheriting.
	 * 
	 * @return The action object currently invoked 
	 */
	
	public InfoGlueAbstractAction getThis()
	{
		return this;
	}
	
	/**
	 * This method returns the logout url.
	 * @author Mattias Bogeblad
	 */
	
	public String getLogoutURL() throws Exception
	{
		AuthenticationModule authenticationModule = AuthenticationModule.getAuthenticationModule(null, null, getRequest(), false);
	    return authenticationModule.getLogoutUrl();
	}


	/**
	 * This method returns the actions url base.
	 * @author Mattias Bogeblad
	 */
	
	public String getURLBase()
	{
	    return this.getRequest().getContextPath();
	}

	/**
	 * This method returns the current url.
	 * @author Mattias Bogeblad
	 */
	
	public String getCurrentURL()
	{
		return this.getRequest().getRequestURL() + (this.getRequest().getQueryString() == null ? "" : "?" + this.getRequest().getQueryString());
	}

	public String getOriginalFullURL()
	{
    	String originalRequestURL = this.getRequest().getParameter("originalRequestURL");
    	if(originalRequestURL == null || originalRequestURL.length() == 0)
    		originalRequestURL = this.getRequest().getRequestURL().toString();

    	String originalQueryString = this.getRequest().getParameter("originalQueryString");
    	if(originalQueryString == null || originalQueryString.length() == 0)
    		originalQueryString = this.getRequest().getQueryString();

    	return originalRequestURL + (originalQueryString == null ? "" : "?" + originalQueryString);
	}

	/**
	 * This method returns the session timeout value.
	 */
	
	public int getSessionTimeout()
	{
	    return this.getHttpSession().getMaxInactiveInterval();
	}

	private SecureRandom random = new SecureRandom();

	/**
	 * This method returns the session timeout value.
	 */
	
	public String getSecurityCode()
	{
		String securityCode = (String)this.getHttpSession().getAttribute("securityCode");
		if(securityCode == null)
		{
			securityCode = new BigInteger(130, random).toString(32);
			this.getHttpSession().setAttribute("securityCode", securityCode);
		}
		
	    return securityCode;
	}

	public final static List<String> actionNames = new ArrayList<String>();

	static
	{
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateRepositoryAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.DeleteRepositoryAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.CreateRepositoryAction");

		actionNames.add("org.infoglue.cms.applications.managementtool.actions.CreateLanguageAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.DeleteLanguageAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateLanguageAction");
	
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.CategoryAction");
		
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateInterceptionPointAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.DeleteInterceptionPointAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.CreateInterceptionPointAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateInterceptorAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.DeleteInterceptorAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.CreateInterceptorAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateServiceDefinitionAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.DeleteServiceDefinitionAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.CreateServiceDefinitionAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateSiteNodeTypeDefinitionAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.DeleteSiteNodeTypeDefinitionAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.CreateSiteNodeTypeDefinitionAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateSystemUserAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateSystemUserPasswordAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.DeleteSystemUserAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.CreateSystemUserAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.DeleteSystemUserRoleAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.AddUserRoleAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateUserPropertiesAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.CreateRoleAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.DeleteRoleAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateRoleAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.AddRoleUserAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateRolePropertiesAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.CreateGroupAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.DeleteGroupAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateGroupAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateGroupPropertiesAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateContentTypeDefinitionAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.DeleteContentTypeDefinitionAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.CreateContentTypeDefinitionAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateWorkflowDefinitionAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.DeleteWorkflowDefinitionAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.CreateWorkflowDefinitionAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateRedirectAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.DeleteRedirectAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.CreateRedirectAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.UpdateServerNodeAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.DeleteServerNodeAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.CreateServerNodeAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.ViewServerNodePropertiesAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.CreateEmailAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.ViewArchiveToolAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.ViewThemesAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.ViewLabelsAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.AuthorizationSwitchManagementAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.deployment.ViewVCDeploymentAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.RepositoryLanguageAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.ViewRepositoryPropertiesAction");
		actionNames.add("org.infoglue.cms.applications.managementtool.actions.ViewSystemUserAction");

	}
	
	
	public void protectFromCSSAttacks(String actionName, String commandName) throws Exception
	{
		//logger.debug("Checking if we should protect actionName:" + actionName + " : " + commandName);
		if(commandName != null && (commandName.startsWith("input") || commandName.startsWith("new") || commandName.startsWith("list") || commandName.startsWith("edit")))
		{
			//logger.debug("Was input action - do not protect:" + commandName);
			return;
		}
		//Special cases - please rewrite actions to conform
		if((commandName == null || commandName.equalsIgnoreCase("V3")) && 
				(actionName.equals("org.infoglue.cms.applications.managementtool.actions.ViewServerNodePropertiesAction") ||
				 actionName.equals("org.infoglue.cms.applications.managementtool.actions.ViewRepositoryPropertiesAction") ||
				 actionName.equals("org.infoglue.cms.applications.managementtool.actions.ViewThemesAction") ||
				 actionName.equals("org.infoglue.cms.applications.managementtool.actions.ViewLabelsAction") ||
				 actionName.equals("org.infoglue.cms.applications.managementtool.actions.ViewSystemUserAction")))
		{
			return;
		}
		if(actionNames.contains(actionName))
		{
			if(logger.isDebugEnabled())
				logger.debug("Yes - we decided to protect:" + actionName);
			validateSecurityCode();
		}
	}
	
	public void validateSecurityCode() throws Exception
	{
		String sessionSecurityCode = getSecurityCode();
		String securityCode = this.getRequest().getParameter("igSecurityCode");
		if(!sessionSecurityCode.equals(securityCode))
		{
			throw new SystemException("Your request did not contain the correct checksum value - it was classified as a hack-attempt");
		}
		if(logger.isDebugEnabled())
			logger.debug("Your security code validated");
	}
	
	public List getAuthorizedRepositoryVOList() throws Exception
	{
		return RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
	}
	
	public Integer getRepositoryId()
	{
		Integer repositoryId = (Integer)this.getHttpSession().getAttribute("repositoryId");
		try
		{
			RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);
			repositoryId = repositoryVO.getId();
		}
		catch (Exception e) 
		{
			logger.warn("Not a valid repository");
			repositoryId = null;
		}
		
		logger.info("repositoryId:" + repositoryId);
		if(repositoryId == null)
		{
    		String prefferedRepositoryId = CmsPropertyHandler.getPreferredRepositoryId(this.getInfoGluePrincipal().getName());
    		logger.info("prefferedRepositoryId:" + prefferedRepositoryId);
    		if(prefferedRepositoryId != null && prefferedRepositoryId.length() > 0)
    		{
    			repositoryId = new Integer(prefferedRepositoryId);
    			logger.info("Setting session repositoryId:" + repositoryId);
    			getHttpSession().setAttribute("repositoryId", repositoryId);		
    		}
    		else
    		{
    			try
				{
					List authorizedRepositoryVOList = getAuthorizedRepositoryVOList();
					if(authorizedRepositoryVOList != null && authorizedRepositoryVOList.size() > 0)
					{
						RepositoryVO repositoryVO = (RepositoryVO)authorizedRepositoryVOList.get(0);
						repositoryId = repositoryVO.getId();
						if(logger.isDebugEnabled())
							logger.debug("Setting session repositoryId:" + repositoryId);
		    			getHttpSession().setAttribute("repositoryId", repositoryId);	
					}
				}
				catch (Exception e)
				{
					logger.warn("No repository found for the user: " + e.getMessage());
				}
    		}
		}
		
		return repositoryId;
	}

	/**
	 * Gets a list of tool languages
	 */

	public List getToolLocales()
	{
		return CmsPropertyHandler.getToolLocales();
	}

	public List getToolbarButtons(String toolbarKey, HttpServletRequest request)
	{
		return toolbarService.getToolbarButtons(toolbarKey, getInfoGluePrincipal(), getLocale(), request, false);
	}

	public List getToolbarButtons(String toolbarKey, HttpServletRequest request, boolean disableCloseButton)
	{
		return toolbarService.getToolbarButtons(toolbarKey, getInfoGluePrincipal(), getLocale(), request, disableCloseButton);
	}

	public List getToolbarButtons(String toolbarKey, String primaryKey, String extraParameters)
	{
		try
		{
			HttpHelper helper = new HttpHelper();
			Map extraParametersMap = helper.toMap(extraParameters, "iso-8859-1", "&");
			Iterator extraParametersMapIterator = extraParametersMap.keySet().iterator();
			while(extraParametersMapIterator.hasNext())
			{
				String key = (String)extraParametersMapIterator.next();
				String value = (String)extraParametersMap.get(key);
				getRequest().setAttribute(key, value);
			}
		}
		catch (Exception e) 
		{
			logger.error("Problem parsing extra parameters at url:" + getOriginalFullURL());
		}
		
		return toolbarService.getToolbarButtons(toolbarKey, getInfoGluePrincipal(), getLocale(), getRequest(), false);
	}

	public List getRightToolbarButtons(String toolbarKey, String primaryKey, String extraParameters, boolean disableCloseButton)
	{
		return toolbarService.getRightToolbarButtons(toolbarKey, getInfoGluePrincipal(), getLocale(), getRequest(), disableCloseButton);
	}
	
	public List getFooterToolbarButtons(String toolbarKey, String primaryKey, String extraParameters, boolean disableCloseButton)
	{
		try
		{
			HttpHelper helper = new HttpHelper();
			Map extraParametersMap = helper.toMap(extraParameters, "iso-8859-1", "&");
			Iterator extraParametersMapIterator = extraParametersMap.keySet().iterator();
			while(extraParametersMapIterator.hasNext())
			{
				String key = (String)extraParametersMapIterator.next();
				String value = (String)extraParametersMap.get(key);
				getRequest().setAttribute(key, value);
			}
		}
		catch (Exception e) 
		{
			logger.error("Problem parsing extra parameters at url:" + getOriginalFullURL());
		}

		return toolbarService.getFooterToolbarButtons(toolbarKey, getInfoGluePrincipal(), getLocale(), getRequest(), disableCloseButton);
	}

/*
	public List getToolbarButtons(String toolbarKey, String primaryKey, String extraParameters)
	{
		return toolbarController.getToolbarButtons(toolbarKey, getInfoGluePrincipal(), getLocale(), primaryKey, extraParameters);
	}

	public List getRightToolbarButtons(String toolbarKey, String primaryKey, String extraParameters, boolean disableCloseButton)
	{
		return toolbarController.getRightToolbarButtons(toolbarKey, getInfoGluePrincipal(), getLocale(), primaryKey, extraParameters, disableCloseButton);
	}
	
	public List getFooterToolbarButtons(String toolbarKey, String primaryKey, String extraParameters, boolean disableCloseButton)
	{
		return toolbarController.getFooterToolbarButtons(toolbarKey, getInfoGluePrincipal(), getLocale(), primaryKey, extraParameters, disableCloseButton);
	}
*/

	/**
	 * This method returns a propertyValue for the logged in user.
	 * 
	 * @author Mattias Bogeblad
	 */
	
	public String getPrincipalPropertyValue(String propertyName, boolean escapeSpecialCharacters)
	{		
		return getPrincipalPropertyValue(propertyName, escapeSpecialCharacters, false);
	}

	/**
	 * This method returns a propertyValue for the logged in user.
	 * 
	 * @author Mattias Bogeblad
	 */
	
	public String getPrincipalPropertyValue(InfoGluePrincipal infoGluePrincipal, String propertyName, boolean escapeSpecialCharacters, boolean findLargestValue)
	{
		logger.info("propertyName: " + propertyName);
		logger.info("escapeSpecialCharacters: " + escapeSpecialCharacters);
		logger.info("findLargestValue: " + findLargestValue);
	    
		String value = "";
		
		try
		{
		    LanguageVO languageVO = (LanguageVO)LanguageController.getController().getLanguageVOList().get(0);
		    value = InfoGluePrincipalControllerProxy.getController().getPrincipalPropertyValue(infoGluePrincipal, propertyName, languageVO.getId(), null, false, escapeSpecialCharacters, findLargestValue);
		}
		catch(Exception e)
		{
		    logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return value;
	}

	/**
	 * This method returns a propertyValue for the logged in user.
	 * 
	 * @author Mattias Bogeblad
	 */
	
	public String getPrincipalPropertyValue(String propertyName, boolean escapeSpecialCharacters, boolean findLargestValue)
	{
		return getPrincipalPropertyValue(propertyName, escapeSpecialCharacters, findLargestValue, false);
	}
	
	/**
	 * This method returns a propertyValue for the logged in user.
	 * 
	 * @author Mattias Bogeblad
	 */
	
	public String getPrincipalPropertyValue(String propertyName, boolean escapeSpecialCharacters, boolean findLargestValue, boolean findPrioValue)
	{
		logger.info("propertyName: " + propertyName);
		logger.info("escapeSpecialCharacters: " + escapeSpecialCharacters);
		logger.info("findLargestValue: " + findLargestValue);
	    
		String value = "";
		
		try
		{
		    InfoGluePrincipal infoGluePrincipal = this.getInfoGluePrincipal();
		    LanguageVO languageVO = (LanguageVO)LanguageController.getController().getLanguageVOList().get(0);
		    value = InfoGluePrincipalControllerProxy.getController().getPrincipalPropertyValue(infoGluePrincipal, propertyName, languageVO.getId(), null, false, escapeSpecialCharacters, findLargestValue, findPrioValue);
		}
		catch(Exception e)
		{
		    logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return value;
	}

	/**
	 * Getting a property for a Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well.
	 */
	
	public Map getPrincipalPropertyHashValues(String propertyName, boolean escapeSpecialCharacters)
	{
		Map value = new HashMap();
		
		try
		{
		    InfoGluePrincipal infoGluePrincipal = this.getInfoGluePrincipal();
		    LanguageVO languageVO = (LanguageVO)LanguageController.getController().getLanguageVOList().get(0);
			value = InfoGluePrincipalControllerProxy.getController().getPrincipalPropertyHashValues(infoGluePrincipal, propertyName, languageVO.getId(), null, false, escapeSpecialCharacters);
		}
		catch(Exception e)
		{
		    logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return value;
	}	

	public Principal getAnonymousPrincipal() throws SystemException
	{
	    Principal principal = null;
		try
		{
			principal = (Principal)CacheController.getCachedObject("userCache", "anonymous");
			if(principal == null)
			{
			    Map arguments = new HashMap();
			    arguments.put("j_username", CmsPropertyHandler.getAnonymousUser());
			    arguments.put("j_password", CmsPropertyHandler.getAnonymousPassword());
			    arguments.put("ticket", this.getHttpSession().getAttribute("ticket"));

			    principal = ExtranetController.getController().getAuthenticatedPrincipal(arguments, getRequest());
				
				if(principal != null)
					CacheController.cacheObject("userCache", "anonymous", principal);
			}			
		}
		catch(Exception e) 
		{
		    logger.warn("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
		    throw new SystemException("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
		}

		return principal;
	}
	
	public Principal getInfoGluePrincipal(String userName) throws SystemException
	{
		Principal principal = null;
		try
		{
			principal = (Principal)CacheController.getCachedObject("userCache", userName);
			if(principal == null)
			{
				principal = UserControllerProxy.getController().getUser(userName);
				
				if(principal != null)
					CacheController.cacheObject("userCache", userName, principal);
			}			
		}
		catch(Exception e) 
		{
		    logger.warn("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
		    throw new SystemException("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
		}

		return principal;
	}
	
	/**
	 * Used by the view pages to determine if the current user has sufficient access rights
	 * to perform the action specific by the interception point name.
	 *
	 * @param interceptionPointName THe Name of the interception point to check access rights
	 * @return True is access is allowed, false otherwise
	 */
	public boolean hasAccessTo(String interceptionPointName)
	{
		logger.info("Checking if " + getUserName() + " has access to " + interceptionPointName);

		try
		{
			return AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), interceptionPointName);
		}
		catch (SystemException e)
		{
		    logger.warn("Error checking access rights", e);
			return false;
		}
	}

	/**
	 * Used by the view pages to determine if the current user has sufficient access rights
	 * to perform the action specific by the interception point name.
	 *
	 * @param interceptionPointName THe Name of the interception point to check access rights
	 * @return True is access is allowed, false otherwise
	 */
	public boolean hasAccessTo(String interceptionPointName, boolean returnSuccessIfInterceptionPointNotDefined)
	{
		logger.info("Checking if " + getUserName() + " has access to " + interceptionPointName);

		try
		{
			return AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), interceptionPointName, returnSuccessIfInterceptionPointNotDefined);
		}
		catch (SystemException e)
		{
		    logger.warn("Error checking access rights", e);
			return false;
		}
	}

	/**
	 * Used by the view pages to determine if the current user has sufficient access rights
	 * to perform the action specific by the interception point name.
	 *
	 * @param interceptionPointName THe Name of the interception point to check access rights
	 * @return True is access is allowed, false otherwise
	 */
	public boolean hasAccessTo(String interceptionPointName, String extraParameter)
	{
		logger.info("Checking if " + getUserName() + " has access to " + interceptionPointName + " with extraParameter " + extraParameter);
		try
		{
		    return AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), interceptionPointName, extraParameter);
		}
		catch (SystemException e)
		{
		    logger.warn("Error checking access rights", e);
			return false;
		}
	}
	
	/**
	 * This method fetches the list of ContentTypeDefinitions
	 */
	
	public List<ContentTypeDefinitionVO> getContentTypeDefinitions() throws Exception
	{	
	    List<ContentTypeDefinitionVO> contentTypeVOList = null;
	    
	    String protectContentTypes = CmsPropertyHandler.getProtectContentTypes();
	    if(protectContentTypes != null && protectContentTypes.equalsIgnoreCase("true"))
	        contentTypeVOList = ContentTypeDefinitionController.getController().getAuthorizedContentTypeDefinitionVOList(this.getInfoGluePrincipal());
		else
		    contentTypeVOList = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
	    
	    Collections.sort(contentTypeVOList, new ReflectionComparator("name"));
	    
	    return contentTypeVOList;
	}      

	/**
	 * Gets a protected content id (if any).
	 */
	
	public Integer getProtectedContentId(Integer parentContentId)
	{
		return ContentControllerProxy.getController().getProtectedContentId(parentContentId);
	}

	/**
	 * Gets a protected content id (if any).
	 */
	
	public Integer getProtectedSiteNodeVersionId(Integer parentSiteNodeVersionId)
	{
		return SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(parentSiteNodeVersionId);
	}
	
	/**
	 * Get the username for the currently logged in user
	 */
	public String getUserName()
	{
		return getInfoGluePrincipal().getName();
	}

	public String getInfoGlueVersion()
	{
		return CmsPropertyHandler.getInfoGlueVersion();
	}

	public String getInfoGlueVersionReleaseDate()
	{
		return CmsPropertyHandler.getInfoGlueVersionReleaseDate();
	}

	public String getDatabaseEngine()
	{
		return CmsPropertyHandler.getDatabaseEngine();
	}

	/**
	 * Get a single parameter from the ActionContext (hides Servlet implementation)
	 */
	public final String getSingleParameter(String parameterName)
	{
		return (String) ActionContext.getSingleValueParameters().get(parameterName);
	}

	/**
	 * Get a parameter (could possibly be an array) from the ActionContext (hides Servlet implementation)
	 */
	public final String getParameter(String parameterName)
	{
		return (String) ActionContext.getParameters().get(parameterName);
	}

	public final Integer getUserUploadMaxSize()
	{
		String userUploadMaxSize = getPrincipalPropertyValue("fileUploadMaximumSize", false, true);
		if (userUploadMaxSize != null && !userUploadMaxSize.equals("") && !userUploadMaxSize.equals("-1"))
		{
			try
			{
				Integer userUploadMaxSizeInteger = new Integer(userUploadMaxSize);
				return userUploadMaxSizeInteger;
			} 
			catch (Exception e)
			{
				return getUploadMaxSize();
			}
		} 
		else
		{
			return getUploadMaxSize();
		}
	}

	public static InfoGluePrincipal getSessionInfoGluePrincipal()
	{
		InfoGluePrincipal infoGluePrincipal = null;
		try
		{
			if(ActionContext.getRequest() != null && ActionContext.getRequest().getSession() != null)
				infoGluePrincipal = (InfoGluePrincipal)ActionContext.getRequest().getSession().getAttribute("org.infoglue.cms.security.user");
		}
		catch (Exception e) 
		{
			logger.warn("Problem getting principal from session:" + e.getMessage());
		}
		
		return infoGluePrincipal;
	}

	public final Integer getUploadMaxSize()
	{
		Integer maxSize = new Integer(Integer.MAX_VALUE);
		try
		{
			String maxSizeStr = Configuration.getString("webwork.multipart.maxSize");
			if (maxSizeStr != null)
			{
				maxSize = new Integer(maxSizeStr);
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return maxSize;
	}
	
	public String getPrefferedWYSIWYG()
	{
		return CmsPropertyHandler.getPrefferedWYSIWYG();
	}

	public Boolean getOnlyAllowFolderType()
	{
		return CmsPropertyHandler.getOnlyAllowFolderType();
	}

	public Boolean getSkipResultDialogIfPossible()
	{
		return CmsPropertyHandler.getSkipResultDialogIfPossible();
	}

    public String getColorScheme()
    {
        return colorScheme;
    }
    
    public void setColorScheme(String colorScheme)
    {
        this.colorScheme = colorScheme;
    }

    public String encode(String value)
    {
        return this.getResponse().encodeUrl(value);
    }

    public String getComponentRendererUrl()
    {
        return CmsPropertyHandler.getComponentRendererUrl();
    }
    
    public String getComponentRendererAction()
    {
        return CmsPropertyHandler.getComponentRendererAction();
    }
    
    public String getCMSBaseUrl()
    {
        return CmsPropertyHandler.getCmsBaseUrl();
    }

    public String getDisableImageEditor()
    {
        return CmsPropertyHandler.getDisableImageEditor();
    }

	public String getDisableCustomIcons()
	{
	    return CmsPropertyHandler.getDisableCustomIcons();
	}

	public String getWorkingStyleInformation()
	{
	    return CmsPropertyHandler.getWorkingStyleInformation();
	}

	public String getFinalStyleInformation()
	{
	    return CmsPropertyHandler.getFinalStyleInformation();
	}

	public String getPublishStyleInformation()
	{
	    return CmsPropertyHandler.getPublishStyleInformation();
	}

	public String getPublishedStyleInformation()
	{
	    return CmsPropertyHandler.getPublishedStyleInformation();
	}

	public Map getCustomContentTypeIcons()
	{
	    return CmsPropertyHandler.getCustomContentTypeIcons();
	}
	
    public String getEnableDateTimeDirectEditing()
    {
        return CmsPropertyHandler.getEnableDateTimeDirectEditing();
    }

    public String getAllowPublicationEventFilter()
    {
    	return CmsPropertyHandler.getAllowPublicationEventFilter();
    }

    public Locale getLocale()
    {
        return this.getSession().getLocale();
    }

    public List<LanguageVO> getLanguages() throws SystemException, Bug
    {
    	return LanguageController.getController().getLanguageVOList();
    }

	public String getDefaultGUI()
	{
		String guiVersion = CmsPropertyHandler.getDefaultGUI(getUserName());
		
		return guiVersion;
	}

	public String getTheme()
	{
		String theme = CmsPropertyHandler.getTheme(getUserName());
		
		try
		{
			theme = ThemeController.verifyThemeExistenceOtherwiseFallback(theme);
		}
		catch (Exception e) 
		{
		}
		
		return theme;
	}

	public String getToolbarVariant()
	{
		String toolbarVariant = CmsPropertyHandler.getToolbarVariant(getUserName());
		
		return toolbarVariant;
	}

	public String getInitialToolName()
	{
		return CmsPropertyHandler.getPreferredToolName(getUserName());
	}

	public List<InfoglueTool> getAvailableTools()
	{
		return AdminToolsService.getService().getTools(getInfoGluePrincipal(), getLocale());
	}

    public String getToolName()
    {
        return this.getSession().getToolName();
    }

    public String getLanguageCode()
    {
        return this.getSession().getLocale().getLanguage();
    }
    
	public void setLanguageCode(String languageCode)
	{
		this.getSession().setLocale(new java.util.Locale(languageCode));
	}

	public void setToolName(String toolName)
	{
		this.getSession().setToolName(toolName);
	}

	//--------------------------------------------------------------------------
	// Database/Transaction specific operations
	//--------------------------------------------------------------------------

	/**
	 * Begins a transaction on the supplied database
	 */
	
	public void beginTransaction(Database db) throws SystemException
	{
		try
		{
			db.begin();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SystemException("An error occurred when we tried to begin an transaction. Reason:" + e.getMessage(), e);    
		}
	}
       
	/**
	 * Rollbacks a transaction on the named database
	 */
     
	public void closeTransaction(Database db) throws SystemException
	{
	    //if(db != null && !db.isClosed() && db.isActive())
	        //commitTransaction(db);
	        rollbackTransaction(db);
	}

	
	/**
	 * Ends a transaction on the named database
	 */
	
    public void commitTransaction(Database db) throws SystemException
	{
		try
		{
		    if (db.isActive())
		    {
			    db.commit();
			}
		}
		catch(Exception e)
		{
		    throw new SystemException("An error occurred when we tried to commit an transaction. Reason:" + e.getMessage(), e);    
		}
		finally
		{
		    closeDatabase(db);
		}
	}
	
 
	/**
	 * Rollbacks a transaction on the named database
	 */
     
	public void rollbackTransaction(Database db) throws SystemException
	{
		try
		{
			if (db.isActive())
			{
			    db.rollback();
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred when we tried to rollback an transaction. Reason:" + e.getMessage());
		}
		finally
		{
		    closeDatabase(db);
		}
	}

	/**
	 * Close the database
	 */
     
	public void closeDatabase(Database db) throws SystemException
	{
		try
		{
			db.close();
		}
		catch(Exception e)
		{
			logger.warn("An error occurred when we closed the database. Reason:" + e.getMessage());
			throw new SystemException("An error occurred when we tried to close a database. Reason:" + e.getMessage(), e);    
		}
	}

	public List<LinkBean> getActionLinks(String aUserSessionKey)
	{
		String key = aUserSessionKey + "_actionLinks";
		return (List<LinkBean>)getRequest().getSession().getAttribute(key);
	}

	public void setActionLinks(String aUserSessionKey, List<LinkBean> actionLinks)
	{
		String key = aUserSessionKey + "_actionLinks";
		getRequest().getSession().setAttribute(key, actionLinks);
	}
	
	public void addActionLink(String aUserSessionKey, LinkBean aLinkBean)
	{
		List<LinkBean> actionLinks = getActionLinks(aUserSessionKey);
		if (actionLinks == null)
		{			
			actionLinks = new ArrayList<LinkBean>();
		}
		
		actionLinks.add(aLinkBean);
		
		setActionLinks(aUserSessionKey, actionLinks);
	}

	public void addActionLinkFirst(String aUserSessionKey, LinkBean aLinkBean)
	{
		List<LinkBean> actionLinks = getActionLinks(aUserSessionKey);
		if (actionLinks == null)
		{			
			actionLinks = new ArrayList<LinkBean>();
		}
		
		actionLinks.add(0, aLinkBean);
		
		setActionLinks(aUserSessionKey, actionLinks);
	}

	public void setActionMessage(String aUserSessionKey, String actionMessage)
	{
		String key = aUserSessionKey + "_actionMessage";
		getRequest().getSession().setAttribute(key, actionMessage);
	}

	public String getActionMessage(String aUserSessionKey)
	{
		String key = aUserSessionKey + "_actionMessage";
		return (String)getRequest().getSession().getAttribute(key);
	}

	public void setActionExtraData(String aUserSessionKey, String extraDataKey, String extraData)
	{
		String key = aUserSessionKey + "_" + extraDataKey;
		getRequest().getSession().setAttribute(key, extraData);
	}

	public String getActionExtraData(String aUserSessionKey, String extraDataKey)
	{
		String key = aUserSessionKey + "_" + extraDataKey;
		return (String)getRequest().getSession().getAttribute(key);
	}

	public boolean getDisableCloseButton()
	{
		String disableCloseButton = this.getRequest().getParameter("disableCloseButton");
		if(disableCloseButton != null && !disableCloseButton.equals(""))
		{	
			return Boolean.parseBoolean(disableCloseButton);
		}
		else
			return false;
	}

	//TODO - make other base action for asset aware actions
	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetUrl(Integer digitalAssetId) throws Exception
	{
		String imageHref = null;
		try
		{
       		imageHref = DigitalAssetController.getDigitalAssetUrl(digitalAssetId);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the digitalAsset: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}
	
	
	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetThumbnailUrl(Integer digitalAssetId, int canvasWidth, int canvasHeight, String canvasColorHexCode, String alignment, String valignment, int width, int height, int quality) throws Exception
	{
		String imageHref = null;
		try
		{
			ColorHelper ch = new ColorHelper();
			Color canvasColor = ch.getHexColor(canvasColorHexCode);
       		imageHref = DigitalAssetController.getDigitalAssetThumbnailUrl(digitalAssetId, canvasWidth, canvasHeight, canvasColor, alignment, valignment, width, height, quality);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the thumbnail: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}

	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetThumbnailUrl(Integer contentId, Integer languageId, String assetKey, boolean useLanguageFallback, int canvasWidth, int canvasHeight, String canvasColorHexCode, String alignment, String valignment, int width, int height, int quality) throws Exception
	{
		String imageHref = null;
		try
		{
			ColorHelper ch = new ColorHelper();
			Color canvasColor = ch.getHexColor(canvasColorHexCode);
			DigitalAssetVO assetVO = DigitalAssetController.getDigitalAssetVO(contentId, languageId, assetKey, useLanguageFallback);
       		
			imageHref = DigitalAssetController.getDigitalAssetThumbnailUrl(assetVO.getId(), canvasWidth, canvasHeight, canvasColor, alignment, valignment, width, height, quality);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the thumbnail: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}
	
	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetUrl(Integer contentId, Integer languageId) throws Exception
	{
		String imageHref = null;
		try
		{
       		imageHref = DigitalAssetController.getDigitalAssetUrl(contentId, languageId);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the digitalAsset: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}

	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetUrl(Integer contentId, Integer languageId, String assetKey) throws Exception
	{
		String imageHref = null;
		try
		{
			imageHref = DigitalAssetController.getDigitalAssetUrl(contentId, languageId, assetKey, true);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the digitalAsset: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}

	
	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetThumbnailUrl(Integer contentId, Integer languageId) throws Exception
	{
		String imageHref = null;
		try
		{
       		imageHref = DigitalAssetController.getDigitalAssetThumbnailUrl(contentId, languageId);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the thumbnail: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}


	public Integer getDigitalAssetContentId(Integer digitalAssetId) throws Exception
	{
		return DigitalAssetController.getController().getContentId(digitalAssetId);
	}

	public String getContentPath(Integer contentId) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		
		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
		sb.insert(0, contentVO.getName());
		while(contentVO.getParentContentId() != null)
		{
			contentVO = ContentController.getContentController().getContentVOWithId(contentVO.getParentContentId());
			sb.insert(0, contentVO.getName() + "/");
		}
		sb.insert(0, "/");
		
		return sb.toString();
	}

	public SiteNodeVersionVO getSiteNodeVersionVO(Integer siteNodeVersionId) throws Exception
	{
		return SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getSiteNodeVersionVOWithId(siteNodeVersionId);
	}

	public String getSiteNodePath(Integer siteNodeId) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		
		SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
		while(siteNodeVO != null)
		{
			sb.insert(0, "/" + siteNodeVO.getName());
			if(siteNodeVO.getParentSiteNodeId() != null)
				siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVO.getParentSiteNodeId());
			else
				siteNodeVO = null;
		}
		
		return sb.toString();
	}

	public SiteNodeVO getRepositoryRootSiteNode(Integer repositoryId) throws Exception
	{
		return SiteNodeController.getController().getRootSiteNodeVO(repositoryId);
	}

	public String getRepositoryName() throws Exception
	{
		return RepositoryController.getController().getRepositoryVOWithId(getRepositoryId()).getName();
	}
	
	public void logMessage(String message)
	{
		logger.warn("" + message);
	}
    
    /**
     * This method is called by the controllers to let interceptors listen to events.
     * 
     * @param hashMap
     * @param InterceptionPointName
     * @param infogluePrincipal
     * @throws ConstraintException
     * @throws SystemException
     * @throws Bug
     * @throws Exception
     */
    protected void intercept(Map hashMap, String InterceptionPointName, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException, Bug, Exception
	{
    	intercept(hashMap, InterceptionPointName, infogluePrincipal, true);
	}
	
    protected void intercept(Map hashMap, String InterceptionPointName, InfoGluePrincipal infogluePrincipal, boolean allowCreatorAccess) throws ConstraintException, SystemException, Bug, Exception
	{
		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName(InterceptionPointName);
    	
		if(interceptionPointVO == null)
			throw new SystemException("The InterceptionPoint " + InterceptionPointName + " was not found. The system will not work unless you restore it.");

		List interceptors = InterceptorController.getController().getInterceptorsVOList(interceptionPointVO.getInterceptionPointId());
		Iterator interceptorsIterator = interceptors.iterator();
		while(interceptorsIterator.hasNext())
		{
			InterceptorVO interceptorVO = (InterceptorVO)interceptorsIterator.next();
			logger.info("Adding interceptorVO:" + interceptorVO.getName());
			try
			{
				InfoGlueInterceptor infoGlueInterceptor = InterceptionService.getService().getInterceptor(interceptorVO.getName());
				if(infoGlueInterceptor == null)
					infoGlueInterceptor = (InfoGlueInterceptor)Class.forName(interceptorVO.getClassName()).newInstance();
				System.out.println("infoGlueInterceptor:" + infoGlueInterceptor);
				infoGlueInterceptor.intercept(infogluePrincipal, interceptionPointVO, hashMap, allowCreatorAccess);
			}
			catch(ClassNotFoundException e)
			{
				logger.warn("The interceptor " + interceptorVO.getClassName() + "was not found: " + e.getMessage(), e);
			}
		}
	}
}

