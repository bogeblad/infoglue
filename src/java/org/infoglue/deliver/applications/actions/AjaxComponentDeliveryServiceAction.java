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

import java.io.File;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.CatchTag;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ComponentController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
import org.infoglue.deliver.controllers.kernel.impl.simple.ExtranetController;
import org.infoglue.deliver.controllers.kernel.impl.simple.PageEditorHelper;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.ThreadMonitor;


/**
 * This is the action supplying all ajax calls for component aspects for the delivery engine.
 *
 * @author Mattias Bogeblad
 */

public class AjaxComponentDeliveryServiceAction extends InfoGlueAbstractAction 
{
    private final static Logger logger = Logger.getLogger(AjaxComponentDeliveryServiceAction.class.getName());
  
    /**
     * This method will return all properties for a component. 
     */
         
    public String doGetComponentPropertyDiv() throws Exception
    {
    	try
    	{
    	StringBuffer propertiesDiv = new StringBuffer();

    	String repositoryIdString 		= this.getRequest().getParameter("repositoryId");
    	String siteNodeIdString 		= this.getRequest().getParameter("siteNodeId");
    	String languageIdString 		= this.getRequest().getParameter("languageId");
    	String componentIdString 		= this.getRequest().getParameter("componentId");
    	String contentIdString 			= this.getRequest().getParameter("contentId");
    	String componentContentIdString = this.getRequest().getParameter("componentContentId");
    	String slotName 				= this.getRequest().getParameter("slotName");
    	String showSimple 				= this.getRequest().getParameter("showSimple");
    	String showLegend 				= this.getRequest().getParameter("showLegend");
    	String targetDiv 				= this.getRequest().getParameter("targetDivId");
    	String originalUrl				= this.getRequest().getParameter("originalUrl");
    	String fullHtml 				= this.getRequest().getParameter("fullHtml");

    	//System.out.println("componentIdString..." + componentIdString);
    	//System.out.println("componentContentIdString..." + componentContentIdString);
    	if(componentContentIdString == null || componentContentIdString.equals("") || componentContentIdString.equals("null"))
    		componentContentIdString = "-1";
    		
    	Integer repositoryId = new Integer(repositoryIdString);
    	Integer siteNodeId = new Integer(siteNodeIdString);
    	Integer languageId = new Integer(languageIdString);
    	Integer componentId = new Integer(componentIdString);
    	Integer contentId = new Integer(contentIdString);

    	Integer componentContentId = null;
    	if(componentContentIdString != null && !componentContentIdString.equals("null") && !componentContentIdString.equals("-1"))
    		componentContentId = new Integer(componentContentIdString);
    	else
    		return NONE;
    	
    	Database db = CastorDatabaseService.getDatabase();
    	
    	try
    	{
    		beginTransaction(db);
    	
        	InfoGluePrincipal principal = this.getInfoGluePrincipal();
        	String cmsUserName = (String)this.getHttpSession().getAttribute("cmsUserName");
       	 	if(cmsUserName != null)
       	 		principal = UserControllerProxy.getController(db).getUser(cmsUserName);
        	else
        		principal = (InfoGluePrincipal)this.getAnonymousPrincipal();
        	
       	 	logger.info("cmsUserName:" + cmsUserName);
     	
        	Locale locale = this.getLocale();
        	if(languageId != null)
        		locale = LanguageController.getController().getLocaleWithId(languageId);
        	
        	if(slotName == null)
        		slotName = "";
        	
    		PageEditorHelper peh = new PageEditorHelper();
	    	String componentPropertiesDiv = peh.getComponentPropertiesDiv(db, principal, this.getRequest(), locale, repositoryId, siteNodeId, languageId, contentId, componentId, componentContentId, slotName, showSimple, originalUrl, showLegend, targetDiv);
	    	propertiesDiv.append(componentPropertiesDiv);
    	
	    	commitTransaction(db);
    	}
    	catch (Exception e) 
    	{
    		rollbackTransaction(db);
    		e.printStackTrace();
		}
     	    	
    	logger.info("Returning:" + propertiesDiv.toString());
    	
    	if(fullHtml == null || !fullHtml.equals("true"))
    	{
            if(logger.isInfoEnabled())
                logger.info("Returning:" + propertiesDiv.toString());

            this.getResponse().setContentType("text/plain");
            this.getResponse().setCharacterEncoding("utf-8");
    		this.getResponse().getWriter().println(propertiesDiv.toString());
    	}
    	else
    	{
            if(logger.isInfoEnabled())
                logger.info("Returning:" + propertiesDiv.toString());
    		
		    String template = FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "preview/ajax/componentPropertiesTemplate.vm"), "iso-8859-1");
		    //System.out.println("replacing in " + template + "\n with " + propertiesDiv.toString());
		    String firstPart = template.substring(0,template.indexOf("$propertiesDiv"));
		    String secondPart = template.substring(template.indexOf("$propertiesDiv") + 14);
		    template = firstPart + "" + propertiesDiv.toString() + secondPart;
            this.getResponse().setContentType("text/html");
            this.getResponse().setCharacterEncoding("utf-8");
    		this.getResponse().getWriter().println(template);
    	}
    	
        
    	}
    	catch (Throwable e) 
    	{
    		e.printStackTrace();
		}
    	
        return NONE;
    }
    
    /**
     * This method will return all tasks available for a component. 
     */
         
    public String doGetComponentTasksDiv() throws Exception
    {
    	StringBuffer tasksDiv = new StringBuffer();

    	String repositoryIdString 		= this.getRequest().getParameter("repositoryId");
    	String siteNodeIdString 		= this.getRequest().getParameter("siteNodeId");
    	String languageIdString 		= this.getRequest().getParameter("languageId");
    	String componentIdString 		= this.getRequest().getParameter("componentId");
    	String contentIdString 			= this.getRequest().getParameter("contentId");
    	String componentContentIdString = this.getRequest().getParameter("componentContentId");
    	String slotName 				= this.getRequest().getParameter("slotName");
    	String slotId 					= this.getRequest().getParameter("slotId");
    	String showSimple 				= this.getRequest().getParameter("showSimple");
    	String showLegend 				= this.getRequest().getParameter("showLegend");
    	String targetDiv 				= this.getRequest().getParameter("targetDivId");
    	String slotClicked 				= this.getRequest().getParameter("slotClicked");
    	String treeItemString			= this.getRequest().getParameter("treeItem");
    	String originalFullURL			= this.getRequest().getParameter("originalUrl");
    	
    	if(contentIdString == null || contentIdString.equals("") || contentIdString.equals("null"))
    		contentIdString = "-1";
    	if(languageIdString == null || languageIdString.equals("") || languageIdString.equals("null"))
    		languageIdString = "-1";
    		
    	Integer repositoryId 			= new Integer(repositoryIdString);
    	Integer siteNodeId 				= new Integer(siteNodeIdString);
    	Integer languageId 				= new Integer(languageIdString);
    	Integer componentId 			= new Integer(componentIdString);
    	Integer contentId 				= new Integer(contentIdString);
    	Integer componentContentId 		= new Integer(componentContentIdString);
    	
    	boolean treeItem = false;
    	if(treeItemString != null && treeItemString.equals("true"))
    		treeItem = true;
    		    
    	Database db = CastorDatabaseService.getDatabase();
    	
    	try
    	{
    		beginTransaction(db);
    	
        	InfoGluePrincipal principal = this.getInfoGluePrincipal();
        	String cmsUserName = (String)this.getHttpSession().getAttribute("cmsUserName");
        	if(cmsUserName != null)
       	 		principal = UserControllerProxy.getController(db).getUser(cmsUserName);
        	else
        		principal = (InfoGluePrincipal)this.getAnonymousPrincipal();
        	
        	logger.info("cmsUserName:" + cmsUserName);
        	
        	Locale locale = this.getLocale();
        	if(languageId != null)
        		locale = LanguageController.getController().getLocaleWithId(languageId);
        	
        	if(slotName == null)
        		slotName = "";
        	
    		PageEditorHelper peh = new PageEditorHelper();
	    	String componentTasksDiv = peh.getComponentTasksDiv(db, principal, this.getRequest(), locale, repositoryId, siteNodeId, languageId, contentId, componentId, componentContentId, slotName, slotId, showSimple, originalFullURL, showLegend, targetDiv, slotClicked, treeItem);
	    	tasksDiv.append(componentTasksDiv);

	    	commitTransaction(db);
    	}
    	catch (Exception e) 
    	{
    		rollbackTransaction(db);
    		e.printStackTrace();
		}
    	
        this.getResponse().setContentType("text/plain");
        this.getResponse().setCharacterEncoding("utf-8");
        this.getResponse().getWriter().println(tasksDiv.toString());

        if(logger.isInfoEnabled())
            logger.info("Returning:" + tasksDiv.toString());
        
        return NONE;
    }

    /**
     * This method will return all properties for a component. 
     */
         
    public String doGetAvailableComponentsDiv() throws Exception
    {
    	String availableComponentDiv = "";

    	String repositoryIdString 		= this.getRequest().getParameter("repositoryId");
    	String languageIdString 		= this.getRequest().getParameter("languageId");
    	String componentContentIdString = this.getRequest().getParameter("componentContentId");
    	String slotName 				= this.getRequest().getParameter("slotName");
    	String showLegend 				= this.getRequest().getParameter("showLegend");
    	String showComponentNames 		= this.getRequest().getParameter("showComponentNames");
    	String targetDiv 				= this.getRequest().getParameter("targetDivId");

    	Integer repositoryId = null;
    	if(repositoryIdString != null)
    		repositoryId = new Integer(repositoryIdString);
    	
    	Integer languageId = null;
    	if(languageIdString != null)
    		languageId = new Integer(languageIdString);
    	
    	Integer componentContentId = null;
    	if(componentContentIdString != null)
    		componentContentId = new Integer(componentContentIdString);
    		    
    	Database db = CastorDatabaseService.getDatabase();
    	
    	try
    	{
    		beginTransaction(db);
    	
    		InfoGluePrincipal principal = this.getInfoGluePrincipal();
        	String cmsUserName = (String)this.getHttpSession().getAttribute("cmsUserName");

        	if(cmsUserName != null)
       	 		principal = UserControllerProxy.getController(db).getUser(cmsUserName);
        	else
        		principal = (InfoGluePrincipal)this.getAnonymousPrincipal();
        	
        	logger.info("cmsUserName:" + cmsUserName);
        	
        	Locale locale = this.getLocale();
        	if(languageId != null)
        		locale = LanguageController.getController().getLocaleWithId(languageId);
        	
        	if(slotName == null)
        		slotName = "";
        	
        	PageEditorHelper peh = new PageEditorHelper();
	    	availableComponentDiv = peh.getAvailableComponentsDiv(db, principal, locale, repositoryId, languageId, componentContentId, slotName, showLegend, showComponentNames, targetDiv);
        	
	    	commitTransaction(db);
    	}
    	catch (Exception e) 
    	{
    		rollbackTransaction(db);
    		e.printStackTrace();
		}
     	    	
        this.getResponse().setContentType("text/plain");
        this.getResponse().setCharacterEncoding("utf-8");
        this.getResponse().getWriter().println(availableComponentDiv);

        if(logger.isInfoEnabled())
        	logger.info("Returning:" + availableComponentDiv);
        
        return NONE;
    }
    
    
    /**
     * This method will return the page component structure. 
     */
         
    public String doGetComponentStructureDiv() throws Exception
    {
    	StringBuffer componentStructureDiv = new StringBuffer();

    	String repositoryIdString 		= this.getRequest().getParameter("repositoryId");
    	String siteNodeIdString 		= this.getRequest().getParameter("siteNodeId");
    	String languageIdString 		= this.getRequest().getParameter("languageId");
    	String contentIdString 			= this.getRequest().getParameter("contentId");
    	String showSimple 				= this.getRequest().getParameter("showSimple");
    	String showLegend 				= this.getRequest().getParameter("showLegend");
    	String targetDiv 				= this.getRequest().getParameter("targetDivId");
    	String originalFullURL			= this.getRequest().getParameter("originalUrl");

    	Integer repositoryId 			= new Integer(repositoryIdString);
    	Integer siteNodeId 				= new Integer(siteNodeIdString);
    	Integer languageId 				= new Integer(languageIdString);
    	Integer contentId 				= new Integer(contentIdString);
    		    
    	Database db = CastorDatabaseService.getDatabase();
    	
    	try
    	{
    		beginTransaction(db);
    	
        	InfoGluePrincipal principal = this.getInfoGluePrincipal();
        	String cmsUserName = (String)this.getHttpSession().getAttribute("cmsUserName");
       	 	if(cmsUserName != null)
       	 		principal = UserControllerProxy.getController(db).getUser(cmsUserName);
        	else
        		principal = (InfoGluePrincipal)this.getAnonymousPrincipal();
        	
       	 	logger.info("cmsUserName:" + cmsUserName);
        	Locale locale = this.getLocale();
        	if(languageId != null)
        		locale = LanguageController.getController().getLocaleWithId(languageId);
        	
    		PageEditorHelper peh = new PageEditorHelper();
	    	String componentStructure = peh.getComponentStructureDiv(db, principal, this.getRequest(), locale, repositoryId, siteNodeId, languageId, contentId, showSimple, originalFullURL, showLegend, targetDiv);
	    	componentStructureDiv.append(componentStructure);
    	
	    	commitTransaction(db);
    	}
    	catch (Exception e) 
    	{
    		rollbackTransaction(db);
    		e.printStackTrace();
		}
    	
        this.getResponse().setContentType("text/plain");
        this.getResponse().setCharacterEncoding("utf-8");
        this.getResponse().getWriter().println(componentStructureDiv.toString());

        if(logger.isInfoEnabled())
        	logger.info("Returning:" + componentStructureDiv.toString());
        
        return NONE;
    }

 
    /**
     * This method is the application entry-point. The parameters has been set through the setters
     * and now we just have to render the appropriate output. 
     */
         
    public String doExecute() throws Exception
    {
        return SUCCESS;
    }
    
}
