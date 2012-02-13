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

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupPropertiesController;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.GroupProperties;
import org.infoglue.cms.entities.management.GroupPropertiesVO;
import org.infoglue.cms.security.InfoGlueGroup;

public class ViewGroupPropertiesAction extends ViewEntityPropertiesAction
{
    private final static Logger logger = Logger.getLogger(ViewGroupPropertiesAction.class.getName());
	private VisualFormatter formatter = new VisualFormatter();

	private static final long serialVersionUID = 1L;
	
	private String groupName;
	private GroupPropertiesVO groupPropertiesVO;
	private List groupPropertiesVOList;
			
	/**
	 * Initializes all properties needed for the usecase.
	 * @param extranetGroupId
	 */
	protected void initialize(String groupName) throws Exception
	{
	    super.initialize();
				
		logger.info("groupName:" + groupName);

		List contentTypeDefinitionVOList = GroupPropertiesController.getController().getContentTypeDefinitionVOList(groupName);
		if(contentTypeDefinitionVOList != null && contentTypeDefinitionVOList.size() > 0)
			this.setContentTypeDefinitionVO((ContentTypeDefinitionVO)contentTypeDefinitionVOList.get(0));
		
		InfoGlueGroup infoGlueGroup = GroupControllerProxy.getController().getGroup(groupName);
		groupPropertiesVOList = GroupPropertiesController.getController().getGroupPropertiesVOList(groupName, this.getLanguageId());
		if(groupPropertiesVOList != null && groupPropertiesVOList.size() > 0)
		{
			this.groupPropertiesVO = (GroupPropertiesVO)groupPropertiesVOList.get(0);
			this.setContentTypeDefinitionId(this.groupPropertiesVO.getContentTypeDefinitionId());
		}
		else
		{
			this.setContentTypeDefinitionId(this.getContentTypeDefinitionVO().getContentTypeDefinitionId());
		}
		
		logger.info("this.groupPropertiesVO:" + this.groupPropertiesVO);
		
		this.setAttributes(ContentTypeDefinitionController.getController().getContentTypeAttributes(this.getContentTypeDefinitionVO().getSchemaValue(), true, getLanguageCode(), getInfoGluePrincipal(), null));	
	
		logger.info("attributes:" + this.getContentTypeAttributes().size());		
		logger.info("availableLanguages:" + this.getAvailableLanguages().size());		
		
	} 

	public String doExecute() throws Exception
	{
		this.initialize(getGroupName());   
		
        this.setCurrentAction("ViewGroupProperties.action");
        this.setUpdateAction("UpdateGroupProperties");
        this.setUpdateAndExitAction("UpdateGroupProperties!saveAndExit");
        this.setCancelAction("ViewGroup.action");
        this.setToolbarKey("tool.managementtool.viewGroupProperties.header");
        this.setTitleKey("tool.managementtool.viewGroupProperties.header");
        
        if(this.groupPropertiesVO != null)
        	this.setArguments("entityId=" + this.groupPropertiesVO.getId());
        else
        	this.setArguments("");
        
        this.setEntityName(GroupProperties.class.getName());

		return "success";
	}

	public String doV3() throws Exception
	{
		this.initialize(getGroupName());
		
        this.setCurrentAction("ViewGroupProperties!v3.action");
        this.setUpdateAction("UpdateGroupProperties!v3");
        this.setUpdateAndExitAction("UpdateGroupProperties!saveAndExitV3");
        this.setCancelAction("ViewGroup!v3.action");
        this.setToolbarKey("tool.managementtool.viewGroupProperties.header");
        this.setTitleKey("tool.managementtool.viewGroupProperties.header");

        if(this.groupPropertiesVO != null)
        	this.setArguments("entityId=" + this.groupPropertiesVO.getId());
        else
        	this.setArguments("");
        
        this.setEntityName(GroupProperties.class.getName());

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
			if(this.groupPropertiesVO != null && this.groupPropertiesVO.getId() != null)
	       	{
	       		digitalAssets = GroupPropertiesController.getController().getDigitalAssetVOList(this.groupPropertiesVO.getId());
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
			if(this.groupPropertiesVO != null && this.groupPropertiesVO.getId() != null)
		    	return getPropertiesCategoryController().findByPropertiesAttribute(attribute, GroupProperties.class.getName(),  this.groupPropertiesVO.getId());
		}
		catch(Exception e)
		{
			logger.warn("We could not fetch the list of defined category keys: " + e.getMessage(), e);
		}

		return Collections.EMPTY_LIST;
	}

	
	public String getXML()
	{
	    return (this.groupPropertiesVO == null) ? null : this.groupPropertiesVO.getValue();
	}

	
	public String getGroupName()
	{
		return groupName;
	}

	public GroupPropertiesVO getGroupPropertiesVO()
	{
		return groupPropertiesVO;
	}

	public List getGroupPropertiesVOList()
	{
		return groupPropertiesVOList;
	}

	public void setGroupName(String groupName) throws Exception
	{
		logger.info("groupName:" + groupName);
		if(!GroupControllerProxy.getController().groupExists(groupName))
		{
			logger.info("groupName did not exist - we try to decode it:" + groupName);
			byte[] bytes = Base64.decodeBase64(groupName);
			String decodedGroupName = new String(bytes, "utf-8");
			logger.info("decodedGroupName:" + decodedGroupName);
			if(GroupControllerProxy.getController().groupExists(decodedGroupName))
			{
				logger.info("decodedGroupName existed:" + decodedGroupName);
				groupName = decodedGroupName;
			}
		}
		
		this.groupName = groupName;
		this.setOwnerEntityId(groupName);
	}
    
    public Integer getEntityId()
    {
        return this.groupPropertiesVO.getId();
    }
    
    public void setOwnerEntityId(String ownerEntityId) throws Exception
    {
    	logger.info("ownerEntityId:" + ownerEntityId);
		if(!GroupControllerProxy.getController().groupExists(ownerEntityId))
		{
			logger.info("groupName did not exist - we try to decode it:" + ownerEntityId);
			byte[] bytes = Base64.decodeBase64(ownerEntityId);
			String decodedGroupName = new String(bytes, "utf-8");
			logger.info("decodedGroupName:" + decodedGroupName);
			if(GroupControllerProxy.getController().groupExists(decodedGroupName))
			{
				logger.info("decodedGroupName existed:" + decodedGroupName);
				ownerEntityId = decodedGroupName;
			}
		}
        super.setOwnerEntityId(ownerEntityId);
        this.groupName = ownerEntityId;
    }
     
    public String getReturnAddress() throws UnsupportedEncodingException
    {
    	return this.getCurrentAction() + "?groupName=" + formatter.encodeBase64(this.groupName) + "&amp;languageId=" + this.getLanguageId();
    }

    public String getCancelAddress() throws UnsupportedEncodingException
    {
    	return this.getCancelAction() + "?groupName=" + formatter.encodeBase64(this.groupName);
    }

}
