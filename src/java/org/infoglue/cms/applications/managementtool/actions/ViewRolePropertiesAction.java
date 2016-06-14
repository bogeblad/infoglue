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

import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.RolePropertiesController;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.RoleProperties;
import org.infoglue.cms.entities.management.RolePropertiesVO;
import org.infoglue.cms.security.InfoGlueRole;

public class ViewRolePropertiesAction extends ViewEntityPropertiesAction
{
    private final static Logger logger = Logger.getLogger(ViewRolePropertiesAction.class.getName());
	private VisualFormatter formatter = new VisualFormatter();

	private static final long serialVersionUID = 1L;

	private String roleName;
	private RolePropertiesVO rolePropertiesVO;
	private List rolePropertiesVOList;
	
	
	/**
	 * Initializes all properties needed for the usecase.
	 * @param extranetRoleId
	 * @throws Exception
	 */

	protected void initialize(String roleName) throws Exception
	{
	    super.initialize();
				
		logger.info("roleName:" + roleName);
		
		List contentTypeDefinitionVOList = RolePropertiesController.getController().getContentTypeDefinitionVOList(roleName);
		if(contentTypeDefinitionVOList != null && contentTypeDefinitionVOList.size() > 0)
			this.setContentTypeDefinitionVO((ContentTypeDefinitionVO)contentTypeDefinitionVOList.get(0));
		
		InfoGlueRole infoGlueRole = RoleControllerProxy.getController().getRole(roleName);
		rolePropertiesVOList = RolePropertiesController.getController().getRolePropertiesVOList(roleName, this.getLanguageId());
		if(rolePropertiesVOList != null && rolePropertiesVOList.size() > 0)
		{
			this.rolePropertiesVO = (RolePropertiesVO)rolePropertiesVOList.get(0);
			this.setContentTypeDefinitionId(this.rolePropertiesVO.getContentTypeDefinitionId());
		}
		else
		{
			this.setContentTypeDefinitionId(this.getContentTypeDefinitionVO().getContentTypeDefinitionId());
		}
		
		logger.info("this.rolePropertiesVO:" + this.rolePropertiesVO);
		
		this.setAttributes(ContentTypeDefinitionController.getController().getContentTypeAttributes(this.getContentTypeDefinitionVO().getSchemaValue(), true, getLanguageCode(), getInfoGluePrincipal(), null));	
	
		logger.info("attributes:" + this.getContentTypeAttributes().size());		
		logger.info("availableLanguages:" + this.getAvailableLanguages().size());		
		
	} 

	public String doExecute() throws Exception
	{
		this.initialize(getRoleName());   

        this.setCurrentAction("ViewRoleProperties.action");
        this.setUpdateAction("UpdateRoleProperties");
        this.setUpdateAndExitAction("UpdateRoleProperties!saveAndExit");
        this.setCancelAction("ViewRole.action");
        this.setToolbarKey("tool.managementtool.viewRoleProperties.header");
        this.setTitleKey("tool.managementtool.viewRoleProperties.header");
        
        if(this.rolePropertiesVO != null)
        	this.setArguments("entityId=" + this.rolePropertiesVO.getId());
        else
        	this.setArguments("");

        this.setEntityName(RoleProperties.class.getName());

		return "success";
	}

	public String doV3() throws Exception
	{
		this.initialize(getRoleName());   

		this.setCurrentAction("ViewRoleProperties!v3.action");
        this.setUpdateAction("UpdateRoleProperties!v3");
        this.setUpdateAndExitAction("UpdateRoleProperties!saveAndExitV3");
        this.setCancelAction("ViewRole!v3.action");
        this.setToolbarKey("tool.managementtool.viewRoleProperties.header");
        this.setTitleKey("tool.managementtool.viewRoleProperties.header");

        if(this.rolePropertiesVO != null)
        	this.setArguments("entityId=" + this.rolePropertiesVO.getId());
        else
        	this.setArguments("");

        this.setEntityName(RoleProperties.class.getName());
		
		return "successV3";
	}

	/**
	 * Returns a list of digital assets available for this content version.
	 */
	
	public List getDigitalAssets()
	{
		List digitalAssets = null;
		
		try
		{
			if(this.rolePropertiesVO != null && this.rolePropertiesVO.getId() != null)
	       	{
	       		digitalAssets = RolePropertiesController.getController().getDigitalAssetVOList(this.rolePropertiesVO.getId());
	       	}
		}
		catch(Exception e)
		{
			logger.warn("We could not fetch the list of digitalAssets: " + e.getMessage(), e);
		}
		
		return digitalAssets;
	}	

	
	
	

	/**
	 * Returns all current Category relationships for th specified attrbiute name
	 * @param attribute
	 * @return
	 */
	public List getRelatedCategories(String attribute)
	{
		try
		{
			if(this.rolePropertiesVO != null && this.rolePropertiesVO.getId() != null)
		    	return getPropertiesCategoryController().findByPropertiesAttribute(attribute, RoleProperties.class.getName(),  this.rolePropertiesVO.getId());
		}
		catch(Exception e)
		{
			logger.warn("We could not fetch the list of defined category keys: " + e.getMessage(), e);
		}

		return Collections.EMPTY_LIST;
	}

	
	public String getXML()
	{
	    return (this.rolePropertiesVO == null) ? null : this.rolePropertiesVO.getValue();
	}

	
	public String getRoleName()
	{
		return roleName;
	}

	public RolePropertiesVO getRolePropertiesVO()
	{
		return rolePropertiesVO;
	}

	public List getRolePropertiesVOList()
	{
		return rolePropertiesVOList;
	}

	public void setRoleName(String roleName) throws Exception
	{
		logger.info("roleName:" + roleName);
		if(!RoleControllerProxy.getController().roleExists(roleName))
		{
			logger.info("roleName did not exist - we try to decode it:" + roleName);
			byte[] bytes = Base64.decodeBase64(roleName);
			String decodedRoleName = new String(bytes, "utf-8");
			logger.info("decodedRoleName:" + decodedRoleName);
			if(RoleControllerProxy.getController().roleExists(decodedRoleName))
			{
				logger.info("decodedRoleName existed:" + decodedRoleName);
				roleName = decodedRoleName;
			}
		}

		this.roleName = roleName;
		this.setOwnerEntityId(roleName);
	}
    
    public Integer getEntityId()
    {
        return this.rolePropertiesVO.getId();
    }
    
    public void setOwnerEntityId(String ownerEntityId) throws Exception
    {
		logger.info("ownerEntityId:" + ownerEntityId);
		if(!RoleControllerProxy.getController().roleExists(ownerEntityId))
		{
			logger.info("ownerEntityId did not exist - we try to decode it:" + ownerEntityId);
			byte[] bytes = Base64.decodeBase64(ownerEntityId);
			String decodedRoleName = new String(bytes, "utf-8");
			logger.info("decodedRoleName:" + decodedRoleName);
			if(RoleControllerProxy.getController().roleExists(decodedRoleName))
			{
				logger.info("decodedRoleName existed:" + decodedRoleName);
				ownerEntityId = decodedRoleName;
			}
		}

		super.setOwnerEntityId(ownerEntityId);
        this.roleName = ownerEntityId;
    }
     
    public String getReturnAddress() throws Exception
    {
        return this.getCurrentAction() + "?roleName=" + formatter.encodeBase64(this.roleName) + "&languageId=" + this.getLanguageId();
    }

    public String getCancelAddress() throws Exception
    {
        return this.getCancelAction() + "?roleName=" + formatter.encodeBase64(this.roleName);
    }
}
