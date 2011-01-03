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

package org.infoglue.cms.applications.contenttool.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGluePropertiesAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
 * This class implements the action class for viewContentProperties.
 * The use-case lets the user see all extra-properties for a content
 * 
 * @author Mattias Bogeblad  
 */

public class ViewContentPropertiesAction extends InfoGluePropertiesAbstractAction
{ 
    private final static Logger logger = Logger.getLogger(ViewContentPropertiesAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private ContentVO contentVO 				= new ContentVO();
	private PropertySet propertySet				= null; 
	private List contentTypeDefinitionVOList 	= null;
	private List languageVOList					= null;
	
	private String allowedContentTypeNames 		= null;
	private String defaultContentTypeName 		= null;	
	private String initialLanguageId			= null;
	
	private String userSessionKey = null;
	private String returnAddress = null;
	private String originalAddress = null;

    protected void initialize(Integer contentId) throws Exception
    {
        this.contentVO = ContentController.getContentController().getContentVOWithId(contentId);
        this.contentTypeDefinitionVOList = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList(ContentTypeDefinitionVO.CONTENT);
        this.languageVOList = LanguageController.getController().getLanguageVOList(this.contentVO.getRepositoryId());
        
        Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);

        if ( ps.exists("content_" + this.getContentId() + "_allowedContentTypeNames" ) )
	    {
            this.allowedContentTypeNames    = ps.getString("content_" + this.getContentId() + "_allowedContentTypeNames");
        }
	    this.defaultContentTypeName		= ps.getString("content_" + this.getContentId() + "_defaultContentTypeName");
	    this.initialLanguageId			= ps.getString("content_" + this.getContentId() + "_initialLanguageId");
	    logger.info("allowedContentTypeNames:" + allowedContentTypeNames);
	    logger.info("defaultContentTypeName:" + defaultContentTypeName);
	    logger.info("initialLanguageId:" + initialLanguageId);
    } 

    /**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doExecute() throws Exception
    {
        this.initialize(getContentId());

        return "success";
    }

    /**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doV3() throws Exception
    {
		String userSessionKey = "" + System.currentTimeMillis();

        this.initialize(getContentId());

		String updateContentPropertiesInlineOperationDoneHeader = getLocalizedString(getLocale(), "tool.contenttool.updateContentPropertiesInlineOperationDoneHeader");
		
	    setActionMessage(userSessionKey, updateContentPropertiesInlineOperationDoneHeader);

        return "successV3";
    }

    /**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doSave() throws Exception
    {
        String allowedContentTypeNames = null;
        String[] allowedContentTypeNameArray = getRequest().getParameterValues("allowedContentTypeName");
        if(allowedContentTypeNameArray != null)
        {
	        logger.info("allowedContentTypeNameArray:" + allowedContentTypeNameArray);
            allowedContentTypeNames = "";
	        for(int i=0; i<allowedContentTypeNameArray.length; i++)
	        {
	            allowedContentTypeNames += allowedContentTypeNameArray[i] + ","; 
	        }
        }
        
    	Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    if(allowedContentTypeNames != null )
	        ps.setString("content_" + this.getContentId() + "_allowedContentTypeNames", allowedContentTypeNames);
	    if(defaultContentTypeName != null)
	        ps.setString("content_" + this.getContentId() + "_defaultContentTypeName", defaultContentTypeName);
	    if(initialLanguageId != null)
	        ps.setString("content_" + this.getContentId() + "_initialLanguageId", initialLanguageId);
	    
    	return "save";
    }

    /**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doSaveV3() throws Exception
    {
    	doSave();
    	
    	return "saveV3";
    }
    
    /**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doSaveAndExit() throws Exception
    {
    	doSave();
    	
        return "saveAndExit";
    }

    /**
     * The main method that fetches the Value-objects for this use-case
     */
    public String doSaveAndExitV3() throws Exception
    {
    	doSave();

    	System.out.println("returnAddress:" + returnAddress);
    	if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
	        String arguments 	= "userSessionKey=" + userSessionKey + "&isAutomaticRedirect=false";
	        String messageUrl 	= returnAddress + (returnAddress.indexOf("?") > -1 ? "&" : "?") + arguments;
	        
	        this.getResponse().sendRedirect(messageUrl);
	        return NONE;
        }
        else
        {
        	return "saveAndExitV3";
        }
    }
    
    public java.lang.Integer getContentId()
    {
        return this.contentVO.getContentId();
    }
        
    public void setContentId(java.lang.Integer contentId) throws Exception
    {
        this.contentVO.setContentId(contentId);
    }

	public ContentVO getContentVO() 
	{
		return contentVO;
	}
    
    public List getContentTypeDefinitionVOList()
    {
        return contentTypeDefinitionVOList;
    }
    
    public String getAllowedContentTypeNames()
    {
        return allowedContentTypeNames;
    }

    public String getDefaultContentTypeName()
    {
        return defaultContentTypeName;
    }
    
    public void setDefaultContentTypeName(String defaultContentTypeName)
    {
        this.defaultContentTypeName = defaultContentTypeName;
    }
    
    public List getLanguageVOList()
    {
        return languageVOList;
    }
    
    public String getInitialLanguageId()
    {
        return initialLanguageId;
    }
    
    public void setInitialLanguageId(String initialLanguageId)
    {
        this.initialLanguageId = initialLanguageId;
    }
    
    public String getUserSessionKey()
	{
		return userSessionKey;
	}

	public void setUserSessionKey(String userSessionKey)
	{
		this.userSessionKey = userSessionKey;
	}
	
	public String getReturnAddress()
	{
		return this.returnAddress;
	}    
	
    public String getOriginalAddress()
	{
		return originalAddress;
	}

	public void setOriginalAddress(String originalAddress)
	{
		this.originalAddress = originalAddress;
	}

	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}
}
