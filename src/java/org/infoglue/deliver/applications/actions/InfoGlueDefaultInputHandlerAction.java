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

import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.structuretool.actions.ViewStructureTreeForInlineLinkAction;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.applications.inputhandlers.InfoGlueInputHandler;
import org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.ExtranetController;

/**
 * This is the action that receives most input from the outside and invokes the appropriate class
 * to ensure that the data is treated correctly.
 *
 * @author Mattias Bogeblad
 */

public class InfoGlueDefaultInputHandlerAction extends InfoGlueAbstractAction 
{
    private final static Logger logger = Logger.getLogger(InfoGlueDefaultInputHandlerAction.class.getName());

	private Integer siteNodeId = null;
	private Integer languageId = null;
	private Integer contentId = null;
	
	private String redirectAddress  		= null;
	private Integer formContentId 			= null;
	
	
	public String doExecute() throws Exception
    {
    	invokeHandler();
    	getResponse().sendRedirect(this.redirectAddress);
    	
    	return NONE;
    }
    
    
    /**
     * This method invokes the right handler for this input. The handler is declared by the caller but it must
     * conform with the interface of InfoGlueInputHandler. 
     */
    
    protected void invokeHandler() throws Exception
    {
        DatabaseWrapper dbWrapper = new DatabaseWrapper(CastorDatabaseService.getDatabase());
    	//Database db = CastorDatabaseService.getDatabase();
		
		beginTransaction(dbWrapper.getDatabase());

		try
		{
    	    Principal principal = (Principal)this.getHttpSession().getAttribute("infogluePrincipal");
			if(principal == null)
			{
				try
				{
				    Map arguments = new HashMap();
				    arguments.put("j_username", CmsPropertyHandler.getAnonymousUser());
				    arguments.put("j_password", CmsPropertyHandler.getAnonymousPassword());
				    
				    principal = ExtranetController.getController().getAuthenticatedPrincipal(dbWrapper.getDatabase(), arguments);
				}
				catch(Exception e) 
				{
				    throw new SystemException("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
				}
			}
			
			String inputHandlerClassName = ContentDeliveryController.getContentDeliveryController().getContentAttribute(dbWrapper.getDatabase(), formContentId, languageId, "InputHandlerClassName", siteNodeId, true, DeliveryContext.getDeliveryContext(), (InfoGluePrincipal)principal, false);

    		logger.info("Trying to invoke " + inputHandlerClassName);
	    	Object object =	Class.forName(inputHandlerClassName).newInstance();
    		InfoGlueInputHandler infoGlueInputHandler = (InfoGlueInputHandler)object;
    		HashMap parameters = requestToHashtable(this.getRequest()); 
    		infoGlueInputHandler.processInput(dbWrapper, this.siteNodeId, this.languageId, this.contentId, this.formContentId, parameters, this.getRequest(), (InfoGluePrincipal)principal);
    	
    		closeTransaction(dbWrapper.getDatabase());
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(dbWrapper.getDatabase());
			throw new SystemException(e.getMessage());
		}
    	
    }
    
    /**
     * A simple convenience method that converts the request-values into a hashmap.
     */
    
    private HashMap requestToHashtable(HttpServletRequest req) 
	{	
        HashMap parameters = new HashMap();
				
	    for (Enumeration e = req.getParameterNames(); e.hasMoreElements() ;) 
	    {		        
			String name  = (String)e.nextElement();
	        String value = (String)req.getParameter(name);
	        parameters.put(name, value);
	    }
        
        return parameters;	
		
	}
 
	public String getRedirectAddress()
	{
		return this.redirectAddress;
	}

	public void setRedirectAddress(String redirectAddress)
	{
		this.redirectAddress = redirectAddress;
	}

	public Integer getContentId()
	{
		return contentId;
	}

	public Integer getFormContentId()
	{
		return formContentId;
	}

	public Integer getLanguageId()
	{
		return languageId;
	}

	public Integer getSiteNodeId()
	{
		return siteNodeId;
	}

	public void setContentId(Integer contentId)
	{
		this.contentId = contentId;
	}

	public void setFormContentId(Integer formContentId)
	{
		this.formContentId = formContentId;
	}

	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
	}

	public void setSiteNodeId(Integer siteNodeId)
	{
		this.siteNodeId = siteNodeId;
	}

}