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

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.AssetKeyDefinition;
import org.infoglue.cms.applications.databeans.SessionInfoBean;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupPropertiesController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RolePropertiesController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserPropertiesController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.GroupProperties;
import org.infoglue.cms.entities.management.GroupPropertiesVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RoleProperties;
import org.infoglue.cms.entities.management.RolePropertiesVO;
import org.infoglue.cms.entities.management.UserProperties;
import org.infoglue.cms.entities.management.UserPropertiesVO;
import org.infoglue.cms.util.CmsSessionContextListener;


/**
 * This class represents the form for creating and updating digital assets.
 */

public class ViewDigitalAssetAction extends InfoGlueAbstractAction
{
	private final static Logger logger = Logger.getLogger(ViewDigitalAssetAction.class.getName());

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("rawtypes")
	private List availableLanguages  = null;

	private String entity;
	private Integer entityId;
	private boolean cleanAnchors;
	
	private Integer contentId = null;
	private Integer languageId = null;

	private Integer contentVersionId = null;
	private Integer digitalAssetId   = null;
	private Integer uploadedFilesCounter = new Integer(0);
	
	private ContentVersionVO contentVersionVO;
	protected ContentTypeDefinitionVO contentTypeDefinitionVO;
	private DigitalAssetVO digitalAssetVO;

	public ViewDigitalAssetAction()
	{
		this(new ContentVersionVO());
	}

	public ViewDigitalAssetAction(ContentVersionVO contentVersionVO)
	{
		logger.info("Construction ViewDigitalAssetAction");
		this.contentVersionVO = contentVersionVO;
	}

	public String doExecute() throws Exception
	{
		logUserActionInfo(getClass(), "doExecute");
		if (this.contentVersionId != null)
		{
			this.contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId);
			this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentVersionVO.getContentId());
		}
		else if (this.contentId != null)
		{
			logger.info("Using content-id to view upload asset view. Content-id: " + contentId);
			this.contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, languageId);
			if (this.contentVersionVO == null)
			{
				logger.info("Could not find a language version. Will fallback to master language. Content-id: " + contentId);
				ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
				LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());

				logger.debug("Looking for content version with language: " + masterLanguageVO.getLanguageId());
				this.contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, masterLanguageVO.getId());
			}
			if (this.contentVersionVO != null)
			{
				this.contentVersionId = this.contentVersionVO.getContentVersionId();
				this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentVersionVO.getContentId());
			}
		}
		else
		{
			if(this.entity.equalsIgnoreCase(UserProperties.class.getName()))
			{
				UserPropertiesVO userPropertiesVO = UserPropertiesController.getController().getUserPropertiesVOWithId(this.entityId);
				this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(userPropertiesVO.getContentTypeDefinitionId());            
			}
			else if(this.entity.equalsIgnoreCase(RoleProperties.class.getName()))
			{
				RolePropertiesVO rolePropertiesVO = RolePropertiesController.getController().getRolePropertiesVOWithId(this.entityId);
				this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(rolePropertiesVO.getContentTypeDefinitionId());            
			}
			else if(this.entity.equalsIgnoreCase(GroupProperties.class.getName()))
			{
				GroupPropertiesVO groupPropertiesVO = GroupPropertiesController.getController().getGroupPropertiesVOWithId(this.entityId);
				this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(groupPropertiesVO.getContentTypeDefinitionId());            
			}
		}

		return "success";
	}

	public String doMultiple() throws Exception
	{
		logUserActionInfo(getClass(), "doMultiple");
		doExecute();

		return "successMultiple";
	}

	public String doUpdate() throws Exception
	{
		logUserActionInfo(getClass(), "doUpdate");
		this.digitalAssetVO = DigitalAssetController.getDigitalAssetVOWithId(this.digitalAssetId);

		if(this.contentVersionId != null)
		{
			this.contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId);
			this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentVersionVO.getContentId());
		}
		else
		{
			if(this.entity.equalsIgnoreCase(UserProperties.class.getName()))
			{
				UserPropertiesVO userPropertiesVO = UserPropertiesController.getController().getUserPropertiesVOWithId(this.entityId);
				this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(userPropertiesVO.getContentTypeDefinitionId());            
			}
			else if(this.entity.equalsIgnoreCase(RoleProperties.class.getName()))
			{
				RolePropertiesVO rolePropertiesVO = RolePropertiesController.getController().getRolePropertiesVOWithId(this.entityId);
				this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(rolePropertiesVO.getContentTypeDefinitionId());            
			}
			else if(this.entity.equalsIgnoreCase(GroupProperties.class.getName()))
			{
				GroupPropertiesVO groupPropertiesVO = GroupPropertiesController.getController().getGroupPropertiesVOWithId(this.entityId);
				this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(groupPropertiesVO.getContentTypeDefinitionId());            
			}
		}

		return "update";
	}

	public java.lang.Integer getContentVersionId()
	{
		return this.contentVersionId;
	}

	public void setContentVersionId(java.lang.Integer contentVersionId)
	{
		this.contentVersionId = contentVersionId;
	}

	@SuppressWarnings("rawtypes")
	public List getAvailableLanguages()
	{
		return this.availableLanguages;
	}

	public Integer getUploadedFilesCounter()
	{
		return this.uploadedFilesCounter;
	}

	public List<AssetKeyDefinition> getDefinedAssetKeys()
	{
		return ContentTypeDefinitionController.getController().getDefinedAssetKeys(this.contentTypeDefinitionVO, true);
	}

	public boolean getAllowedSessionId(String requestSessionId) throws Exception
	{
		boolean allowedSessionId = false;
		@SuppressWarnings("unchecked")
		List<SessionInfoBean> activeSessionBeanList = CmsSessionContextListener.getSessionInfoBeanList();
		Iterator<SessionInfoBean> activeSessionsIterator = activeSessionBeanList.iterator();
		//logger.info("activeSessionBeanList:" + activeSessionBeanList.size());
		while(activeSessionsIterator.hasNext())
		{
			SessionInfoBean sessionBean = activeSessionsIterator.next();
			//logger.info("sessionBean:" + sessionBean.getId() + "=" + sessionBean.getPrincipal().getName());
			if(sessionBean.getId().equals(requestSessionId))
			{
				//logger.info("Found a matching sessionId");
				allowedSessionId = true;

				break;
			}
		}
		return allowedSessionId;
	}

	public Integer getDigitalAssetId()
	{
		return digitalAssetId;
	}

	public void setDigitalAssetId(Integer digitalAssetId)
	{
		this.digitalAssetId = digitalAssetId;
	}

	public String getDigitalAssetKey()
	{
		return this.digitalAssetVO.getAssetKey();
	}

	public String getEntity()
	{
		return entity;
	}

	public void setEntity(String entity)
	{
		this.entity = entity;
	}
	
	public boolean getCleanAnchors()
	{
		return cleanAnchors;
	}

	public void setCleanAnchors(boolean cleanAnchors)
	{
		this.cleanAnchors = cleanAnchors;
	}
	
	public Integer getEntityId()
	{
		return entityId;
	}

	public void setEntityId(Integer entityId)
	{
		this.entityId = entityId;
	}

	public ContentTypeDefinitionVO getContentTypeDefinitionVO()
	{
		return contentTypeDefinitionVO;
	}

	public Integer getContentId()
	{
		return contentId;
	}

	public void setContentId(Integer contentId)
	{
		this.contentId = contentId;
	}

	public Integer getLanguageId()
	{
		return languageId;
	}

	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
	}
}
