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

package org.infoglue.cms.applications.managementtool.actions;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.util.VelocityTemplateProcessor;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
 * This class/action returns the WYSIWYG configuration in full.
 *
 * @author Mattias Bogeblad
 */

public class WYSIWYGPropertiesAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(WYSIWYGPropertiesAction.class.getName());

	private static final long serialVersionUID = 1L;

	private Integer repositoryId = null;

	private String WYSIWYGProperties = "";
	private String StylesXML = "";
	
    public String doExecute() throws Exception
    {
    	return "success";
    }

    public String doViewStylesXML() throws Exception
    {
    	return "successStylesXML";
    }

	/**
	 * This method gets the WYSIWYG Properties
	 */
	
	public String getWYSIWYGProperties() throws Exception
	{
	    try
	    {
		    this.WYSIWYGProperties = getPrincipalPropertyValue("WYSIWYGConfig", false, false, true);
		    logger.info("WYSIWYGProperties:" + WYSIWYGProperties);
		    if(this.WYSIWYGProperties == null || this.WYSIWYGProperties.equalsIgnoreCase("") && this.repositoryId != null)
		    {
		        logger.info("Getting WYSIWYGProperties for repository...");
				Map args = new HashMap();
			    args.put("globalKey", "infoglue");
			    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
			    
			    byte[] WYSIWYGConfigBytes = ps.getData("repository_" + this.repositoryId + "_WYSIWYGConfig");
			    logger.info("WYSIWYGConfigBytes:" + WYSIWYGConfigBytes);
			    if(WYSIWYGConfigBytes != null)
			    {
			    	this.WYSIWYGProperties = new String(WYSIWYGConfigBytes, "UTF-8");
			    	if(!this.WYSIWYGProperties.trim().equals(""))
			    	{
				    	this.WYSIWYGProperties = this.WYSIWYGProperties + "//Repo properties: " + this.repositoryId;
			    	}
			    }
		    }
		     
		    logger.info("this.WYSIWYGProperties:" + this.WYSIWYGProperties);
	    }
	    catch(Exception e)
	    {
	        logger.error("Could not fetch WYSIWYG Configuration: " + e.getMessage(), e);
	    }
	    finally
	    {
	        try
            {
                if(this.WYSIWYGProperties == null || this.WYSIWYGProperties.equals(""))
                {
                    this.WYSIWYGProperties = FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "cms/contenttool/WYSIWYGConfig.js"));
			    	this.WYSIWYGProperties = this.WYSIWYGProperties + "//File properties";
                }
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
	        
	    }

	    try
	    {
		    Map parameters = new HashMap();
		    parameters.put("request", this.getRequest());
			if(this.getRequest().getParameter("contentVersionId") == null)
			{
				logger.info("No content version sent in.. we fetch latest instead");
				String contentId = this.getRequest().getParameter("contentId");
				String languageId = this.getRequest().getParameter("languageId");
				logger.info("contentId:" + contentId);
				logger.info("languageId:" + languageId);
				if(contentId != null && !contentId.equals("-1") && languageId != null && !languageId.equals("-1"))
				{
					ContentVersionVO cvo = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(new Integer(contentId), new Integer(languageId));
					if(cvo != null)
						parameters.put("contentVersionId", cvo.getId());
				}
			}
			else
			{
				parameters.put("contentVersionId", this.getRequest().getParameter("contentVersionId"));
			}
	
			String languageCode = CmsPropertyHandler.getPreferredLanguageCode(getInfoGluePrincipal().getName());
			parameters.put("principalLanguageCode", languageCode);
			
			StringWriter tempString = new StringWriter();
			PrintWriter pw = new PrintWriter(tempString);
			logger.info("this.WYSIWYGProperties:" + this.WYSIWYGProperties);
			VelocityTemplateProcessor vtp = new VelocityTemplateProcessor();
			vtp.renderTemplate(parameters, pw, this.WYSIWYGProperties, true);
			this.WYSIWYGProperties = tempString.toString();
	    	
		    this.getResponse().setContentType("text/javascript");
	    }
	    catch (Throwable t1)
        {
            logger.error("Error parsing WYSIWYGProperties:" + t1.getMessage());
            logger.warn("Error parsing WYSIWYGProperties:" + t1.getMessage(), t1);
        }
	    
	    return this.WYSIWYGProperties;
	}

	/**
	 * This method gets the Styles XML
	 */
	
	public String getStylesXML()
	{
	    try
	    {
	        this.StylesXML = getPrincipalPropertyValue("StylesXML", false);
		    logger.info("this.StylesXML:" + this.StylesXML);
		    if(this.StylesXML == null || this.StylesXML.equalsIgnoreCase("") && this.repositoryId != null)
		    {
		        logger.info("Getting StylesXML for repository...");
				Map args = new HashMap();
			    args.put("globalKey", "infoglue");
			    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
			    
			    byte[] StylesXMLBytes = ps.getData("repository_" + this.repositoryId + "_StylesXML");
			    if(StylesXMLBytes != null)
			    {
			    	this.StylesXML = new String(StylesXMLBytes, "UTF-8");
			    }
		    }
	    }
	    catch(Exception e)
	    {
	        logger.error("Could not fetch Styles XML: " + e.getMessage(), e);
	    }
	    finally
	    {
	        try
            {
	            if(this.StylesXML == null || this.StylesXML.equals(""))
	                this.StylesXML = FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "cms/contenttool/StylesXML.xml"));
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
	    }
	    
	    this.getResponse().setContentType("text/xml");
	    return this.StylesXML;
	}

	/**
	 * This method gets the toolbar css.
	 */
	
	public String doWYSIWYGToolbarComboPreviewCSS()
	{
		String WYSIWYGToolbarComboPreviewCSS = CmsPropertyHandler.getWYSIWYGToolbarComboPreviewCSS();
		try
		{
			this.getResponse().setContentType("text/css");
		    this.getResponse().getWriter().println(WYSIWYGToolbarComboPreviewCSS);
		    this.getResponse().getWriter().flush();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	    return NONE;
	}

	/**
	 * This method gets the toolbar css.
	 */
	
	public String doWYSIWYGEditorAreaCSS()
	{
		String WYSIWYGEditorAreaCSS = CmsPropertyHandler.getWYSIWYGEditorAreaCSS();
		try
		{
			this.getResponse().setContentType("text/css");
		    this.getResponse().getWriter().println(WYSIWYGEditorAreaCSS);
		    this.getResponse().getWriter().flush();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	    return NONE;
	}

	public String getWYSIWYGSettings()
	{
		try
		{
			return FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "cms/contenttool/WYSIWYGSettings.js"));
		}
		catch (Exception ex)
		{
			logger.error("Error when getting WYSIWYG settings. Message: " + ex.getMessage());
			logger.warn("Error when getting WYSIWYG settings.", ex);
			return "/* WYSIWYG settings threw an exception. Message: " + ex.getMessage() + " */";
		}
	}
	
    public void setRepositoryId(Integer repositoryId)
    {
        this.repositoryId = repositoryId;
    }
}
