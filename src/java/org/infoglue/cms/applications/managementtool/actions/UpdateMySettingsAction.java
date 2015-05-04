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
import java.util.Map;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.util.ChangeNotificationController;
import org.infoglue.cms.util.NotificationMessage;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
 * This class implements the action class for viewRepositoryProperties.
 * The use-case lets the user see all extra-properties for a repository
 * 
 * @author Mattias Bogeblad  
 */

public class UpdateMySettingsAction extends InfoGlueAbstractAction
{ 
	private PropertySet propertySet				= null; 
	
	private String languageCode 				= null;
	private String defaultToolName				= null;
	private String defaultRepositoryId			= null;
	private String defaultGUI					= null;
	private String defaultTreeTitleField 		= null;
	private String theme						= null;
	private String toolbarVariant 				= null;
	
    /**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doExecute() throws Exception
    {
        Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    if(languageCode != null)
	    	ps.setString("principal_" + this.getInfoGluePrincipal().getName() + "_languageCode", languageCode);
	    ps.setString("principal_" + this.getInfoGluePrincipal().getName() + "_defaultToolName", defaultToolName);
	    ps.setString("principal_" + this.getInfoGluePrincipal().getName() + "_defaultRepositoryId", defaultRepositoryId);
	    if(defaultGUI != null)
	    	ps.setString("principal_" + this.getInfoGluePrincipal().getName() + "_defaultGUI", defaultGUI);
	    if(theme != null)
	    	ps.setString("principal_" + this.getInfoGluePrincipal().getName() + "_theme", theme);
	    if(toolbarVariant != null)
	    	ps.setString("principal_" + this.getInfoGluePrincipal().getName() + "_toolbarVariant", toolbarVariant);
	    if(defaultTreeTitleField != null)
	    	ps.setString("principal_" + this.getInfoGluePrincipal().getName() + "_defaultTreeTitleField", defaultTreeTitleField);
	    
		NotificationMessage notificationMessage = new NotificationMessage("UpdateMySettingsAction.doExecute():", "ServerNodeProperties", this.getInfoGluePrincipal().getName(), NotificationMessage.SYSTEM, "0", "MySettings");
		ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
		
        return "success";
    }
    

    public void setLanguageCode(String languageCode)
    {
        this.languageCode = languageCode;
    }
    
    public void setDefaultToolName(String defaultToolName)
    {
        this.defaultToolName = defaultToolName;
    }

    public void setDefaultRepositoryId(String defaultRepositoryId)
    {
        this.defaultRepositoryId = defaultRepositoryId;
    }

    public void setDefaultGUI(String defaultGUI)
    {
        this.defaultGUI = defaultGUI;
    }

    public void setDefaultTreeTitleField(String defaultTreeTitleField)
    {
        this.defaultTreeTitleField = defaultTreeTitleField;
    }

    public void setTheme(String theme)
    {
        this.theme = theme;
    }

    public void setToolbarVariant(String toolbarVariant)
    {
        this.toolbarVariant = toolbarVariant;
    }
}
