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

import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ComponentPropertyDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;


/**
 * This action handles all interaction a user wants to add/change/remove things in a components properties.
 */ 

public class ViewComponentPropertiesEditorAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
    private Integer contentVersionId;
    private Integer contentId;
    private String attributeName;
    private String propertiesXML;
    private List componentPropertyDefinitions;
    private List contentTypeDefinitions;

	private String closeOnLoad = "false";
    
    private void initialize() throws Exception
    {
        this.contentId = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId).getContentId();
        String componentPropertiesXML = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionId, attributeName, false);
        this.componentPropertyDefinitions = ComponentPropertyDefinitionController.getController().parseComponentPropertyDefinitions(componentPropertiesXML);        
        this.contentTypeDefinitions = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList(ContentTypeDefinitionVO.CONTENT);
    }
    
    public String doExecute() throws Exception
    {
        initialize();
        
        return SUCCESS;
    }

    public String doUpdate() throws Exception
    {
        ContentVersionController.getContentVersionController().updateAttributeValue(this.contentVersionId, this.attributeName, this.propertiesXML, this.getInfoGluePrincipal());
        
        return "update";
    }


    public String getAttributeName()
    {
        return attributeName;
    }
    
    public void setAttributeName(String attributeName)
    {
        this.attributeName = attributeName;
    }
    
    public Integer getContentVersionId()
    {
        return contentVersionId;
    }
    
    public void setContentVersionId(Integer contentVersionId)
    {
        this.contentVersionId = contentVersionId;
    }
    
    public List getComponentPropertyDefinitions()
    {
        return componentPropertyDefinitions;
    }
    
    public void setPropertiesXML(String propertiesXML)
    {
        this.propertiesXML = propertiesXML;
    }
    
    public List getContentTypeDefinitions()
    {
        return contentTypeDefinitions;
    }
    
    public Integer getContentId()
    {
        return contentId;
    }
    
    public void setContentId(Integer contentId)
    {
        this.contentId = contentId;
    }
    
    public String getCloseOnLoad()
    {
        return closeOnLoad;
    }
    
    public void setCloseOnLoad(String closeOnLoad)
    {
        this.closeOnLoad = closeOnLoad;
    }

}
