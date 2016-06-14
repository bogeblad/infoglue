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

import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Node;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.UserPropertiesController;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.GroupProperties;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.UserPropertiesVO;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.dom.DOMBuilder;

public class ViewSystemUserPropertiesAction extends ViewEntityPropertiesAction
{
    private final static Logger logger = Logger.getLogger(ViewSystemUserPropertiesAction.class.getName());
	private VisualFormatter formatter = new VisualFormatter();

	private static final long serialVersionUID = 1L;
	
	private String userName;
	private UserPropertiesVO userPropertiesVO;
	private List userPropertiesVOList;
	private List availableLanguages;
	private List contentTypeDefinitionVOList;
	private List attributes;
	private ContentTypeDefinitionVO contentTypeDefinitionVO;
	private Integer contentTypeDefinitionId;
	private Integer languageId;
	private Integer currentEditorId;
	private String attributeName = "";
	private String textAreaId = "";
	
	
	/**
	 * Initializes all properties needed for the usecase.
	 * @param systemUserId
	 * @throws Exception
	 */

	protected void initialize(String userName) throws Exception
	{
		this.availableLanguages = LanguageController.getController().getLanguageVOList();
		
		if(this.languageId == null && this.availableLanguages.size() > 0)
			this.languageId = ((LanguageVO)this.availableLanguages.get(0)).getLanguageId();
		
		logger.info("Language:" + this.languageId);
		
		List contentTypeDefinitionVOList = UserPropertiesController.getController().getContentTypeDefinitionVOList(userName);
		if(contentTypeDefinitionVOList != null && contentTypeDefinitionVOList.size() > 0)
			this.contentTypeDefinitionVO = (ContentTypeDefinitionVO)contentTypeDefinitionVOList.get(0);
		
		logger.info("contentTypeDefinitionVO:" + contentTypeDefinitionVO.getName());
		
		InfoGluePrincipal infoGluePrincipal = UserControllerProxy.getController().getUser(userName);
		userPropertiesVOList = UserPropertiesController.getController().getUserPropertiesVOList(userName, this.languageId);
		if(userPropertiesVOList != null && userPropertiesVOList.size() > 0)
		{
			this.userPropertiesVO = (UserPropertiesVO)userPropertiesVOList.get(0);
			this.contentTypeDefinitionId = this.userPropertiesVO.getLanguageId();
		}
		else
		{
			this.contentTypeDefinitionId = this.contentTypeDefinitionVO.getContentTypeDefinitionId();
		}

		this.attributes = ContentTypeDefinitionController.getController().getContentTypeAttributes(this.contentTypeDefinitionVO, true);	
	
		logger.info("attributes:" + this.attributes.size());		
		logger.info("availableLanguages:" + this.availableLanguages.size());		
	} 

	public String doExecute() throws Exception
	{
		this.initialize(getUserName());   

		this.setCurrentAction("ViewSystemUserProperties.action");
        this.setUpdateAction("UpdateSystemUserProperties");
        this.setUpdateAndExitAction("UpdateSystemUserProperties!saveAndExitV3");
        this.setCancelAction("ViewSystemUser.action");
        this.setToolbarKey("tool.managementtool.viewUserProperties.header");
        this.setTitleKey("tool.managementtool.viewUserProperties.header");
        
        if(this.userPropertiesVO != null)
            this.setArguments("entityId=" + this.userPropertiesVO.getId());
        else
        	this.setArguments("");

        this.setEntityName(GroupProperties.class.getName());

		return "success";
	}

	public String doV3() throws Exception
	{
		this.initialize(getUserName());   

		this.setCurrentAction("ViewSystemUserProperties!v3.action");
        this.setUpdateAction("UpdateSystemUserProperties!v3");
        this.setUpdateAndExitAction("UpdateSystemUserProperties!saveAndExitV3");
        this.setCancelAction("ViewSystemUser!v3.action");
        this.setToolbarKey("tool.managementtool.viewUserProperties.header");
        this.setTitleKey("tool.managementtool.viewUserProperties.header");
        
        if(this.userPropertiesVO != null)
            this.setArguments("entityId=" + this.userPropertiesVO.getId());
        else
        	this.setArguments("");

        this.setEntityName(GroupProperties.class.getName());

		return "successV3";
	}

	/**
	 * This method fetches a value from the xml that is the contentVersions Value. If the 
	 * contentVersioVO is null the contentVersion has not been created yet and no values are present.
	 */
	 
	public String getAttributeValue(String key)
	{
		logger.info("Getting: " + key);
		String value = "";
		try
		{
			String xml = this.getXML();
			if(xml != null)
			{	
				logger.info("key:" + key);
				logger.info("XML:" + this.getXML());
				
				DOMBuilder domBuilder = new DOMBuilder();
				
				Document document = domBuilder.getDocument(this.getXML());
				logger.info("rootElement:" + document.getRootElement().asXML());
				
				Node node = document.getRootElement().selectSingleNode("attributes/" + key);
				if(node != null)
				{
					value = node.getStringValue();
					logger.info("Getting value: " + value);
					if(value != null)
						value = new VisualFormatter().escapeHTML(value);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return value;
	}
	
	public List getAvailableLanguages()
	{
		return this.availableLanguages;
	}
	
	public Integer getContentTypeDefinitionId()
	{
		return this.contentTypeDefinitionId;
	}
	
	public UserPropertiesVO getUserPropertiesVO()
	{
		return userPropertiesVO;
	}

	public String getXML()
	{
		return (this.userPropertiesVO == null) ? null : this.userPropertiesVO.getValue();
	}

	public Integer getLanguageId()
	{
		return languageId;
	}

	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
	}

	public Integer getCurrentEditorId()
	{
		return currentEditorId;
	}

	public void setCurrentEditorId(Integer integer)
	{
		currentEditorId = integer;
	}

	public String getAttributeName()
	{
		return this.attributeName;
	}

	public void setAttributeName(String attributeName)
	{
		this.attributeName = attributeName;
	}

	public String getTextAreaId()
	{
		return this.textAreaId;
	}

	public void setTextAreaId(String textAreaId)
	{
		this.textAreaId = textAreaId;
	}
	
	/**
	 * This method returns the attributes in the content type definition for generation.
	 */
	
	public List getContentTypeAttributes()
	{   		
		return this.attributes;
	}
	
	public String getUserName()
	{
		return this.userName;
	}

	public void setUserName(String userName) throws Exception
	{
		logger.info("userName:" + userName);
		if(!UserControllerProxy.getController().userExists(userName))
		{
			logger.info("userName did not exist - we try to decode it:" + userName);
			byte[] bytes = Base64.decodeBase64(userName);
			String decodedName = new String(bytes, "utf-8");
			logger.info("decodedName:" + decodedName);
			if(UserControllerProxy.getController().userExists(decodedName))
			{
				logger.info("decodedName existed:" + decodedName);
				userName = decodedName;
			}
		}


		this.userName = userName;
	}

    public void setOwnerEntityId(String ownerEntityId) throws Exception
    {
    	logger.info("ownerEntityId:" + ownerEntityId);
		if(!UserControllerProxy.getController().userExists(ownerEntityId))
		{
			logger.info("groupName did not exist - we try to decode it:" + ownerEntityId);
			byte[] bytes = Base64.decodeBase64(ownerEntityId);
			String decodedName = new String(bytes, "utf-8");
			logger.info("decodedName:" + decodedName);
			if(UserControllerProxy.getController().userExists(decodedName))
			{
				logger.info("decodedName existed:" + decodedName);
				ownerEntityId = decodedName;
			}
		}
        super.setOwnerEntityId(ownerEntityId);
        this.userName = ownerEntityId;
    }
    
    public String getReturnAddress() throws Exception
    {
        return this.getCurrentAction() + "?userName=" + formatter.encodeBase64(this.userName) + "&amp;languageId=" + this.getLanguageId();
    }

    public String getCancelAddress() throws Exception
    {
        return this.getCancelAction() + "?userName=" + formatter.encodeBase64(this.userName);
    }

}
