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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ThemeController;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.util.CmsPropertyHandler;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
 * This class implements the action class for viewRepositoryProperties.
 * The use-case lets the user see all extra-properties for a repository
 * 
 * @author Mattias Bogeblad  
 */

public class ViewMySettingsAction extends InfoGlueAbstractAction
{ 
	private static final long serialVersionUID = 1L;

	private PropertySet propertySet				= null; 
	
	private String languageCode 				= null;
	private String defaultToolName				= null;
	private String defaultRepositoryId			= null;
	private String defaultGUI					= null;
	private String theme						= null;
	private String toolbarVariant 				= null;
	
	private List repositories = null;
	private List themes = null;
	
	private Boolean settingsSaved = false;
	
	/**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doExecute() throws Exception
    {
        Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    this.languageCode 			= ps.getString("principal_" + this.getInfoGluePrincipal().getName() + "_languageCode");
	    this.defaultToolName 		= ps.getString("principal_" + this.getInfoGluePrincipal().getName() + "_defaultToolName");
	    this.defaultRepositoryId 	= ps.getString("principal_" + this.getInfoGluePrincipal().getName() + "_defaultRepositoryId");
	    this.defaultGUI 			= ps.getString("principal_" + this.getInfoGluePrincipal().getName() + "_defaultGUI");
	    //this.theme 					= ps.getString("principal_" + this.getInfoGluePrincipal().getName() + "_theme");
	    this.theme 					= CmsPropertyHandler.getTheme(this.getInfoGluePrincipal().getName());
	    this.toolbarVariant			= ps.getString("principal_" + this.getInfoGluePrincipal().getName() + "_toolbarVariant");

	    /* Provide the list of repositories but do not include the system tool repositories */
		List<RepositoryVO> acceptedDefaultRepositoryVOList = new ArrayList<RepositoryVO>();
		List<RepositoryVO> authorizedRepositoryVOList =RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
		
		for (RepositoryVO repositoryVO : authorizedRepositoryVOList) {
			System.out.println(" repositoryVO.getName():" + repositoryVO.getName());
			if (!repositoryVO.getName().equalsIgnoreCase("Infoglue calendar system") && !repositoryVO.getName().equalsIgnoreCase("Infoglue form system")) {
				acceptedDefaultRepositoryVOList.add(repositoryVO);
			}
		}
		this.repositories = acceptedDefaultRepositoryVOList;
		this.themes = ThemeController.getController().getAvailableThemes();
		
        return "success";
    }

    public String getLanguageCode()
    {
        return languageCode;
    }
    
    public String getDefaultToolName()
    {
        return defaultToolName;
    }

	public String getDefaultRepositoryId()
	{
		return defaultRepositoryId;
	}

	public String getDefaultGUI()
	{
		return defaultGUI;
	}
	
	public String getTheme()
	{
		return theme;
	}
	
	public String getToolbarVariant()
	{
		return toolbarVariant;
	}

	public List getRepositories()
	{
		return repositories;
	}
	
    public List getThemes()
	{
		return themes;
	}

	public Boolean getSettingsSaved()
	{
		return settingsSaved;
	}

	public void setSettingsSaved(Boolean settingsSaved)
	{
		this.settingsSaved = settingsSaved;
	}

	
}
