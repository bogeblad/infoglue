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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infoglue.cms.applications.common.actions.InfoGluePropertiesAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.RepositoryVO;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
 * This class implements the action class for viewRepositoryProperties.
 * The use-case lets the user see all extra-properties for a repository
 * 
 * @author Mattias Bogeblad  
 */

public class ViewRepositoryPropertiesAction extends InfoGluePropertiesAbstractAction
{ 
	private static final long serialVersionUID = 1L;

	private RepositoryVO repositoryVO 			= new RepositoryVO();
	private PropertySet propertySet				= null; 
	private List contentTypeDefinitionVOList 	= null;
	private List repositoryVOList				= null;
	
	private String WYSIWYGConfig 				= null;
	private String stylesXML					= null;
	private String extraProperties				= null;
	private String defaultFolderContentTypeName = null;	
	private String defaultTemplateRepository 	= null;	
	private String parentRepository 			= null;	

	
    public ViewRepositoryPropertiesAction()
    {
    }
        
    protected void initialize(Integer repositoryId) throws Exception
    {
        this.repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);
        this.contentTypeDefinitionVOList = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList(ContentTypeDefinitionVO.CONTENT);
        this.repositoryVOList = RepositoryController.getController().getRepositoryVOListNotMarkedForDeletion();
            
        Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    byte[] WYSIWYGConfigBytes = ps.getData("repository_" + this.getRepositoryId() + "_WYSIWYGConfig");
	    if(WYSIWYGConfigBytes != null)
	    	this.WYSIWYGConfig = new String(WYSIWYGConfigBytes, "utf-8");

	    byte[] StylesXMLBytes = ps.getData("repository_" + this.getRepositoryId() + "_StylesXML");
	    if(StylesXMLBytes != null)
	    	this.stylesXML = new String(StylesXMLBytes, "utf-8");

	    byte[] extraPropertiesBytes = ps.getData("repository_" + this.getRepositoryId() + "_extraProperties");
	    if(extraPropertiesBytes != null)
	    	this.extraProperties = new String(extraPropertiesBytes, "utf-8");

	    this.defaultFolderContentTypeName 	= ps.getString("repository_" + this.getRepositoryId() + "_defaultFolderContentTypeName");
	    this.defaultTemplateRepository	 	= ps.getString("repository_" + this.getRepositoryId() + "_defaultTemplateRepository");
	    this.parentRepository	 			= ps.getString("repository_" + this.getRepositoryId() + "_parentRepository");
    } 

    /**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doExecute() throws Exception
    {
        this.initialize(getRepositoryId());

        return "success";
    }
    
    /**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doSave() throws Exception
    {
    	Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    ps.setData("repository_" + this.getRepositoryId() + "_WYSIWYGConfig", WYSIWYGConfig.getBytes("utf-8"));
	    ps.setData("repository_" + this.getRepositoryId() + "_StylesXML", stylesXML.getBytes("utf-8"));
	    ps.setData("repository_" + this.getRepositoryId() + "_extraProperties", extraProperties.getBytes("utf-8"));
	    ps.setString("repository_" + this.getRepositoryId() + "_defaultFolderContentTypeName", defaultFolderContentTypeName);
	    ps.setString("repository_" + this.getRepositoryId() + "_defaultTemplateRepository", defaultTemplateRepository);
	    ps.setString("repository_" + this.getRepositoryId() + "_parentRepository", parentRepository);
	    
	    //TODO - hack to get the caches to be updated when properties are affected..
	    RepositoryVO repositoryVO = RepositoryController.getController().getFirstRepositoryVO();
	    repositoryVO.setDescription(repositoryVO.getDescription() + ".");
	    RepositoryController.getController().update(repositoryVO);
	    
    	return "save";
    }

    /**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doSaveAndExit() throws Exception
    {
    	doSave();
    	
        return "saveAndExit";
    }

    public java.lang.Integer getRepositoryId()
    {
        return this.repositoryVO.getRepositoryId();
    }
        
    public void setRepositoryId(java.lang.Integer repositoryId) throws Exception
    {
        this.repositoryVO.setRepositoryId(repositoryId);
    }

	public RepositoryVO getRepositoryVO() 
	{
		return repositoryVO;
	}
	
	public String getWYSIWYGConfig() 
	{
		return WYSIWYGConfig;
	}
	
	public void setWYSIWYGConfig(String config) 
	{
		WYSIWYGConfig = config;
	}

	public String getWYSIWYGStyles()
    {
        return stylesXML;
    }
    
    public void setWYSIWYGStyles(String stylesXML)
    {
        this.stylesXML = stylesXML;
    }

	public String getExtraProperties()
    {
        return extraProperties;
    }
    
    public void setExtraProperties(String extraProperties)
    {
        this.extraProperties = extraProperties;
    }

	public PropertySet getPropertySet() 
	{
		return propertySet;
	}
	
    public String getDefaultFolderContentTypeName()
    {
        return defaultFolderContentTypeName;
    }
    
    public void setDefaultFolderContentTypeName(String defaultFolderContentTypeName)
    {
        this.defaultFolderContentTypeName = defaultFolderContentTypeName;
    }
    
    public String getDefaultTemplateRepository()
    {
        return defaultTemplateRepository;
    }
    
    public void setDefaultTemplateRepository(String defaultTemplateRepository)
    {
        this.defaultTemplateRepository = defaultTemplateRepository;
    }

    public String getParentRepository()
    {
        return parentRepository;
    }
    
    public void setParentRepository(String parentRepository)
    {
        this.parentRepository = parentRepository;
    }

    public List getContentTypeDefinitionVOList()
    {
        return contentTypeDefinitionVOList;
    }    

    public List getRepositoryVOList()
    {
        return repositoryVOList;
    }
}
