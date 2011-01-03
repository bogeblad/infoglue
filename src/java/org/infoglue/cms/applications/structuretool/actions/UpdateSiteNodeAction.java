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

package org.infoglue.cms.applications.structuretool.actions;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.usecases.structuretool.UpdateSiteNodeUCC;
import org.infoglue.cms.controllers.usecases.structuretool.UpdateSiteNodeUCCFactory;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.util.ChangeNotificationController;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.cms.util.RemoteCacheUpdater;
import org.infoglue.deliver.util.CacheController;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

 
/**
  * This is the action-class for UpdateSiteNode
  * 
  * @author Mattias Bogeblad
  */
public class UpdateSiteNodeAction extends ViewSiteNodeAction //WebworkAbstractAction
{
	private SiteNodeVO siteNodeVO;
	private Integer siteNodeId;
	private Integer repositoryId;
	private Integer siteNodeTypeDefinitionId;
    private String name;
    private Boolean isBranch;
    
	private Integer isProtected;
	private Boolean isHidden;
	private Integer disablePageCache;
	private Integer disableEditOnSight;
	private Integer disableLanguages;
	private Integer disableForceIdentityCheck;
	private Integer forceProtocolChange;
	private String contentType;
	private String pageCacheKey;
	private String pageCacheTimeout;

    private String actionUrl;
    private String targetTitle;

	private ConstraintExceptionBuffer ceb;
	
	public UpdateSiteNodeAction()
	{
		this(new SiteNodeVO());
	}
	
	public UpdateSiteNodeAction(SiteNodeVO siteNodeVO)
	{
		this.siteNodeVO = siteNodeVO;
		this.ceb 		= new ConstraintExceptionBuffer();	
	}
	
	public String doExecute() throws Exception
    {
		//try
		//{
			super.initialize(getSiteNodeId());
			SiteNodeVO oldSiteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(getSiteNodeId());

			this.siteNodeVO.setCreatorName(this.getInfoGluePrincipal().getName());
			this.siteNodeVO.setMetaInfoContentId(oldSiteNodeVO.getMetaInfoContentId());
			ceb = this.siteNodeVO.validate();
	    	
			ceb.throwIfNotEmpty();

			SiteNodeVersionVO siteNodeVersionVO = new SiteNodeVersionVO();
			siteNodeVersionVO.setContentType(this.getContentType());
			siteNodeVersionVO.setPageCacheKey(this.getPageCacheKey());
			siteNodeVersionVO.setPageCacheTimeout(this.getPageCacheTimeout());
			siteNodeVersionVO.setDisableEditOnSight(this.getDisableEditOnSight());
			siteNodeVersionVO.setDisableLanguages(this.disableLanguages);
			siteNodeVersionVO.setDisablePageCache(this.getDisablePageCache());
			siteNodeVersionVO.setDisableForceIdentityCheck(this.disableForceIdentityCheck);
			siteNodeVersionVO.setForceProtocolChange(this.forceProtocolChange);
			siteNodeVersionVO.setIsProtected(this.getIsProtected());
			siteNodeVersionVO.setIsHidden(this.getIsHidden());
			siteNodeVersionVO.setVersionModifier(this.getInfoGluePrincipal().getName());
			
			UpdateSiteNodeUCC updateSiteNodeUCC = UpdateSiteNodeUCCFactory.newUpdateSiteNodeUCC();
			updateSiteNodeUCC.updateSiteNode(this.getInfoGluePrincipal(), this.siteNodeVO, this.siteNodeTypeDefinitionId, siteNodeVersionVO);		

	    	Map args = new HashMap();
		    args.put("globalKey", "infoglue");
		    PropertySet ps = PropertySetManager.getInstance("jdbc", args);

	    	String[] values = getRequest().getParameterValues("disabledLanguageId");
	    	String valueString = "";
	    	if(values != null)
	    	{
	    		for(int i=0; i<values.length; i++)
		    	{
		    	    if(i > 0)
		    	        valueString = valueString + ",";
		    	    valueString = valueString + values[i];  
		    	}
	    	}
	    	ps.setString("siteNode_" + getSiteNodeId() + "_disabledLanguages", valueString);

	    	values = getRequest().getParameterValues("enabledLanguageId");
	    	valueString = "";
	    	if(values != null)
	    	{
	    		for(int i=0; i<values.length; i++)
		    	{
		    	    if(i > 0)
		    	        valueString = valueString + ",";
		    	    valueString = valueString + values[i];  
		    	}
	    	}
	    	ps.setString("siteNode_" + getSiteNodeId() + "_enabledLanguages", valueString);

			NotificationMessage notificationMessage = new NotificationMessage("UpdateSiteNodeAction", "ServerNodeProperties", this.getInfoGluePrincipal().getName(), NotificationMessage.SYSTEM, "0", "ServerNodeProperties");
			ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
			//RemoteCacheUpdater.getSystemNotificationMessages().add(notificationMessage);

	    	//}
		//catch(Exception e)
		//{
		//	e.printStackTrace();
		//}
			
		return "success";
	}

	public String doV3() throws Exception
    {
		doExecute();
						
		return "successV3";
	}

	public String doSaveAndExit() throws Exception
    {
		doExecute();
						
		return "saveAndExit";
	}

	public String doSaveAndExitV3() throws Exception
    {
		doExecute();
						
		return "saveAndExitV3";
	}

	public String doSaveAndExitV3Inline() throws Exception
    {
		doExecute();

		String userSessionKey = "" + System.currentTimeMillis();
        setActionExtraData(userSessionKey, "unrefreshedNodeId", "" + this.siteNodeVO.getSiteNodeId());

		this.actionUrl = "ViewSiteNode.action?siteNodeId=" + this.siteNodeVO.getSiteNodeId(); // + "newSiteNodeId=" + this.siteNodeId;
		this.targetTitle = "AAAAAAAAA";
		
		return "saveAndExitV3Inline";
	}

    public String doUpdateSiteNodeTypeDefinition() throws Exception
    {
    	SiteNodeController.getController().updateSiteNodeTypeDefinition(getSiteNodeId(), this.siteNodeTypeDefinitionId);
    	
    	return "successChooseSiteNodeTypeDefinition";
    }

	public void setSiteNodeId(Integer siteNodeId)
	{
		this.siteNodeVO.setSiteNodeId(siteNodeId);	
	}

    public java.lang.Integer getSiteNodeId()
    {
        return this.siteNodeVO.getSiteNodeId();
    }

    public java.lang.String getName()
    {
        return this.siteNodeVO.getName();
    }
 
	public Boolean getIsBranch()
	{
 		return this.siteNodeVO.getIsBranch();
	}    
        
    public void setName(java.lang.String name)
    {
        this.siteNodeVO.setName(name);
    }
    
   	public void setPublishDateTime(String publishDateTime)
    {
   		this.siteNodeVO.setPublishDateTime(new VisualFormatter().parseDate(publishDateTime, "yyyy-MM-dd HH:mm"));
    }

    public void setExpireDateTime(String expireDateTime)
    {
       	this.siteNodeVO.setExpireDateTime(new VisualFormatter().parseDate(expireDateTime, "yyyy-MM-dd HH:mm"));
	}

    public void setIsBranch(Boolean isBranch)
    {
       	this.siteNodeVO.setIsBranch(isBranch);
    }

	public void setSiteNodeTypeDefinitionId(Integer siteNodeTypeDefinitionId)
	{
		this.siteNodeTypeDefinitionId = siteNodeTypeDefinitionId;	
	}

    public java.lang.Integer getSiteNodeTypeDefinitionId()
    {
        return this.siteNodeTypeDefinitionId;
    }
    
    public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}

	public String getContentType()
	{
		return this.contentType;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	public Integer getDisableEditOnSight()
	{
		return this.disableEditOnSight;
	}

	public void setDisableEditOnSight(Integer disableEditOnSight)
	{
		this.disableEditOnSight = disableEditOnSight;
	}

	public Integer getDisableForceIdentityCheck()
	{
		return this.disableForceIdentityCheck;
	}

	public void setDisableForceIdentityCheck(Integer disableForceIdentityCheck)
	{
		this.disableForceIdentityCheck = disableForceIdentityCheck;
	}

	public Integer getForceProtocolChange()
	{
		return this.forceProtocolChange;
	}

	public void setForceProtocolChange(Integer forceProtocolChange)
	{
		this.forceProtocolChange = forceProtocolChange;
	}

	public Integer getDisableLanguages()
	{
		return this.disableLanguages;
	}

	public void setDisableLanguages(Integer disableLanguages)
	{
		this.disableLanguages = disableLanguages;
	}

	public Integer getDisablePageCache()
	{
		return this.disablePageCache;
	}

	public void setDisablePageCache(Integer disablePageCache)
	{
		this.disablePageCache = disablePageCache;
	}

	public Integer getIsProtected()
	{
		return this.isProtected;
	}

	public void setIsProtected(Integer isProtected)
	{
		this.isProtected = isProtected;
	}

	public Boolean getIsHidden()
	{
		return this.isHidden;
	}

	public void setIsHidden(Boolean isHidden)
	{
		this.isHidden = isHidden;
	}

    public String getPageCacheKey()
    {
        return pageCacheKey;
    }
    
    public void setPageCacheKey(String pageCacheKey)
    {
        this.pageCacheKey = pageCacheKey;
    }

    public String getPageCacheTimeout()
    {
        return pageCacheTimeout;
    }
    
    public void setPageCacheTimeout(String pageCacheTimeout)
    {
        this.pageCacheTimeout = pageCacheTimeout;
    }
    
	public String getActionUrl()
	{
		return actionUrl;
	}

	public String getTargetTitle()
	{
		return targetTitle;
	}

}
