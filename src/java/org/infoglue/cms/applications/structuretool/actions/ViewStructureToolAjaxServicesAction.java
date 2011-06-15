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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.ImageButton;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * This class implements the action class for the framed page in the siteNode tool.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewStructureToolAjaxServicesAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewStructureToolAjaxServicesAction.class.getName());

	private static final long serialVersionUID = 1L;

	private String title = "";
	private String name  = "";
	private String toolbarKey = "";
	private String url   = "";
	private Boolean isBranch = new Boolean(false);
		
	//All id's that are used
	private Integer repositoryId = null;
	private Integer siteNodeId = null;
	private Integer siteNodeVersionId = null;
	private Integer lastPublishedSiteNodeVersionId = null;
	private Integer metaInfoAvailableServiceBindingId = null;
	private SiteNodeVersionVO siteNodeVersionVO = null;
	private SiteNodeVO siteNodeVO = null;
	
   	private VisualFormatter formatter = new VisualFormatter();

	public String doContextMenu() throws Exception
    {
	    try
	    {
			if(siteNodeVersionId != null)
	    	{
				this.siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);
				this.siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(this.siteNodeVersionVO.getSiteNodeId());
				logger.info("ChildCount:" + this.siteNodeVO.getChildCount());
				this.repositoryId = this.siteNodeVO.getRepositoryId();
			}
			else if(siteNodeId != null)
			{
				this.siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
				logger.info("ChildCount:" + this.siteNodeVO.getChildCount());
				this.repositoryId = this.siteNodeVO.getRepositoryId();
				this.siteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(siteNodeId);
				if(this.siteNodeVersionVO != null)
					this.siteNodeVersionId = this.siteNodeVersionVO.getSiteNodeVersionId();
			}
	    }
	    catch(Exception e)
	    {
	        e.printStackTrace();
	    }
	    
        return "successTreeContextMenu";
    }

	/**
	 * This execute method first of all gets the id of the available service-binding 
	 * the meta-info-content-type has. Then we check if there is a meta-info allready bound.
	 */
	
	public String doExecute() throws Exception
    {
	    try
	    {
			if(siteNodeVersionId != null)
	    	{
				this.siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);
				this.siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(this.siteNodeVersionVO.getSiteNodeId());
			}
			else if(siteNodeId != null)
			{
				this.siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
				this.siteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(siteNodeId);
				if(this.siteNodeVersionVO != null)
					this.siteNodeVersionId = this.siteNodeVersionVO.getSiteNodeVersionId();
			}
	    }
	    catch(Exception e)
	    {
	        e.printStackTrace();
	    }
	    
        return "success";
    }

	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}                   

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public Integer getSiteNodeId()
	{
		return this.siteNodeId;
	}                   

	public void setSiteNodeId(Integer siteNodeId)
	{
		this.siteNodeId = siteNodeId;
	}

	public Integer getSiteNodeVersionId()
	{
		return this.siteNodeVersionId;
	}                   
	
	public void setSiteNodeVersionId(Integer siteNodeVersionId)
	{
		this.siteNodeVersionId = siteNodeVersionId;
	}                   

	public String getTitle()
	{
		return this.title;
	}                   
	
	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getName()
	{
		return this.name;
	}                   
	
	public void setName(String name)
	{
		this.name = name;
	}

	public Boolean getIsBranch()
	{
		return this.isBranch;
	}                   
	
	public void setIsBranch(Boolean isBranch)
	{
		this.isBranch = isBranch;
	}
	
	public String getToolbarKey()
	{
		return this.toolbarKey;
	}                   

	public void setToolbarKey(String toolbarKey)
	{
		this.toolbarKey = toolbarKey;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}
	
	public String getUrl()
	{
		return this.url;
	}

	/**
	 * This method checks if the site node version is read only (ie publish, published or final).
	 */
	
	private boolean isReadOnly()
	{
		boolean isReadOnly = false;
		
		try
		{
			//SiteNodeVersionVO siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(this.siteNodeVersionId);
			if(this.siteNodeVersionVO != null && (this.siteNodeVersionVO.getStateId().intValue() == 1 || this.siteNodeVersionVO.getStateId().intValue() == 2 || this.siteNodeVersionVO.getStateId().intValue() == 3))
			{
				isReadOnly = true;	
			}
		}
		catch(Exception e){e.printStackTrace();}
				
		return isReadOnly;
	}

	public List getButtons()
	{
		logger.info("Title:" + this.title);
		logger.info("toolbarKey:" + this.toolbarKey);
		try
		{		
		    if(this.toolbarKey.equalsIgnoreCase("tool.structuretool.siteNodeDetailsHeader") || this.toolbarKey.equalsIgnoreCase("tool.structuretool.siteNodeComponentsHeader"))
			{
			    if(this.isBranch.booleanValue())
					return getBranchSiteNodeButtons();
				else
					return getSiteNodeButtons();
			}	
			else if(this.toolbarKey.equalsIgnoreCase("tool.structuretool.siteNodeVersionHeader"))
			{
				return this.getSiteNodeVersionButtons();
			}
		}
		catch(Exception e)
		{
		    e.printStackTrace();
			logger.warn("Exception when generating buttons:" + e.getMessage(), e);
		}
							
		return null;				
	}
	
	
	/**
	 * This method checks if there are published versions available for the siteNodeVersion.
	 */
	
	private boolean hasPublishedVersion()
	{
		boolean hasPublishedVersion = false;
		
		try
		{
			SiteNodeVersionVO siteNodeVersion = SiteNodeVersionController.getLatestPublishedSiteNodeVersionVO(this.siteNodeId);
			if(siteNodeVersion != null)
			{
				SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVersion.getSiteNodeId());
				hasPublishedVersion = true;
				lastPublishedSiteNodeVersionId = siteNodeVersion.getId();
				this.repositoryId = siteNodeVO.getRepositoryId();
				this.name = siteNodeVO.getName();
				this.siteNodeId = siteNodeVO.getId();
			}
			/*
			SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getLatestPublishedSiteNodeVersion(this.siteNodeId);
			if(siteNodeVersion != null)
			{
				hasPublishedVersion = true;
				lastPublishedSiteNodeVersionId = siteNodeVersion.getId();
				this.repositoryId = siteNodeVersion.getOwningSiteNode().getRepository().getId();
				this.name = siteNodeVersion.getOwningSiteNode().getName();
				this.siteNodeId = siteNodeVersion.getOwningSiteNode().getId();
			}
			*/
		}
		catch(Exception e)
		{
			logger.warn("Exception when generating buttons:" + e.getMessage(), e);
		}
				
		return hasPublishedVersion;
	}
	
	

	private List getBranchSiteNodeButtons() throws Exception
	{
		List buttons = new ArrayList();
		buttons.add(new ImageButton(this.getCMSBaseUrl() + "/CreateSiteNode!input.action?isBranch=true&parentSiteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.newSiteNode"), "New SiteNode"));	
		
		ImageButton moveButton = getMoveButton();
		moveButton.getSubButtons().add(getMoveMultipleButton());
		buttons.add(moveButton);	

		//buttons.add(getMoveButton());	
		//buttons.add(getMoveMultipleButton());	

		if(!hasPublishedVersion())
		    buttons.add(new ImageButton(this.getCMSBaseUrl() + "/Confirm.action?header=tool.structuretool.deleteSiteNode.header&yesDestination=" + URLEncoder.encode(URLEncoder.encode("DeleteSiteNode.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&changeTypeId=4", "UTF-8"), "UTF-8") + "&noDestination=" + URLEncoder.encode(URLEncoder.encode("ViewSiteNode.action?title=SiteNode&siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId, "UTF-8"), "UTF-8") + "&message=tool.structuretool.deleteSiteNode.message", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.deleteSiteNode"), "Delete SiteNode"));
		
		//String serviceBindingIdString = this.serviceBindingId == null ? "" : this.serviceBindingId.toString();
		//buttons.add(new ImageButton(true, "javascript:openPopup('ViewAndCreateContentForServiceBinding.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&siteNodeVersionId=" + this.siteNodeVersionVO.getId() + "&availableServiceBindingId=" + this.metaInfoAvailableServiceBindingId + "&serviceBindingId=" + serviceBindingIdString + "', 'PageProperties', 'width=750,height=700,resizable=no,status=yes,scrollbars=yes');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.editSiteNodeProperties"), "Edit siteNode properties"));
		
		//SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(this.siteNodeId);
		if(this.siteNodeVersionVO != null && this.siteNodeVersionVO.getStateId().equals(SiteNodeVersionVO.WORKING_STATE))
			buttons.add(new ImageButton(true, "javascript:openPopup('ViewAndCreateContentForServiceBinding.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&siteNodeVersionId=" + this.siteNodeVersionVO.getId() + "', 'PageProperties', 'width=750,height=700,resizable=no,status=yes,scrollbars=yes');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.editSiteNodeProperties"), "Edit siteNode properties"));
		else if(this.siteNodeVersionVO != null)
			buttons.add(new ImageButton(true, "javascript:openPopupWithOptionalParameter('ViewAndCreateContentForServiceBinding.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&siteNodeVersionId=" + this.siteNodeVersionVO.getId() + "', 'PageProperties', 'width=750,height=700,resizable=no,status=yes,scrollbars=yes', '" + getLocalizedString(getSession().getLocale(), "tool.structuretool.changeSiteNodeStateToWorkingQuestion") + "', 'changeStateToWorking=true');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.editSiteNodeProperties"), "Edit siteNode properties"));

		buttons.add(getPreviewButtons());
		
		if(hasPublishedVersion())
		{
		    ImageButton unpublishButton = new ImageButton(this.getCMSBaseUrl() + "/UnpublishSiteNodeVersion!input.action?siteNodeId=" + this.siteNodeId + "&siteNodeVersionId=" + this.siteNodeVersionId, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.unpublishVersion"), "tool.contenttool.unpublishVersion.header");
		    ImageButton unpublishAllButton = new ImageButton(this.getCMSBaseUrl() + "/UnpublishSiteNodeVersion!inputChooseSiteNodes.action?siteNodeId=" + this.siteNodeId + "&siteNodeVersionId=" + this.siteNodeVersionId, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.unpublishAllVersion"), "tool.contenttool.unpublishAllVersion.header");
		    unpublishButton.getSubButtons().add(unpublishAllButton);
		
		    buttons.add(unpublishButton);
		}
		
		ImageButton coverButton = new ImageButton(this.getCMSBaseUrl() + "/ViewSiteNode.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&stay=true", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeCover"), "SiteNode Cover");	
		coverButton.getSubButtons().add(getSimplePageComponentsButton());
		buttons.add(coverButton);	

		if(!isReadOnly())
		{
		    ImageButton pageComponentsButton = getViewPageComponentsButton();
		    pageComponentsButton.getSubButtons().add(getSimplePageComponentsButton());
		    buttons.add(pageComponentsButton);	
		}
		
		ImageButton publishButton = getPublishCurrentNodeButton();
	    publishButton.getSubButtons().add(getPublishButton());
	    buttons.add(publishButton);	
	    
		/*
	    ImageButton publishButton = getPublishButton();
	    publishButton.getSubButtons().add(getPublishCurrentNodeButton());
	    buttons.add(publishButton);	
	    */
	    //buttons.add(getPublishButton());
		
		buttons.add(getExecuteTaskButton());

		if(this.siteNodeVersionVO != null && this.siteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES.intValue())
			buttons.add(getAccessRightsButton());	
			
		return buttons;
	}

	private ImageButton getPreviewButtons() throws Exception
	{
		RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(this.repositoryId);
		
		String dnsName = repositoryVO.getDnsName();

	    String workingUrl = null;
	    
	    String keyword = "working=";
	    int startIndex = (dnsName == null) ? -1 : dnsName.indexOf(keyword);
	    if(startIndex != -1)
	    {
	        int endIndex = dnsName.indexOf(",", startIndex);
		    if(endIndex > -1)
	            dnsName = dnsName.substring(startIndex, endIndex);
	        else
	            dnsName = dnsName.substring(startIndex);

		    String hostName = dnsName.split("=")[1];
		    if(hostName.indexOf("localhost") == -1)
			    workingUrl = hostName + CmsPropertyHandler.getComponentRendererUrl() + "ViewPage.action";
		    else
			    workingUrl = CmsPropertyHandler.getComponentRendererUrl() + "ViewPage.action";
	    }
	    else
	    {
	        workingUrl = CmsPropertyHandler.getPreviewDeliveryUrl();
	    }
	    
		ImageButton imageButton = new ImageButton(true, "javascript:openPopup('" + workingUrl + "?siteNodeId=" + this.siteNodeId/* + "&repositoryName=" + URLEncoder.encode(repositoryVO.getName(), "UTF-8") + "&cmsUserName=" + formatter.encodeURI(this.getInfoGluePrincipal().getName())*/ + "' , 'SiteNode', 'resizable=yes,toolbar=yes,scrollbars=yes,status=yes,location=yes,menubar=yes');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.previewSiteNode"), "Preview siteNode");
		
		
		return imageButton;
	}
	
	private List getSiteNodeButtons() throws Exception
	{
		List buttons = new ArrayList();
		buttons.add(new ImageButton("Confirm.action?header=Delete%20siteNode&yesDestination=" + URLEncoder.encode(URLEncoder.encode("DeleteSiteNode.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&changeTypeId=4", "UTF-8"), "UTF-8") + "&noDestination=" + URLEncoder.encode(URLEncoder.encode("ViewSiteNode.action?title=SiteNode&siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId, "UTF-8"), "UTF-8") + "&message=" + URLEncoder.encode("Do you really want to delete the siteNode " + this.name + " and all its children", "UTF-8"), getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.deleteSiteNode"), "Delete SiteNode"));
		buttons.add(getMoveButton());	
		buttons.add(getMoveMultipleButton());	
		buttons.add(getPublishButton());
		return buttons;				
	}

	private List getSiteNodeVersionButtons() throws Exception
	{
		List buttons = new ArrayList();
		if(this.siteNodeVersionId != null)
		{
			buttons.add(new ImageButton(true, "javascript:openPopup('ViewSiteNodeVersion!preview.action?siteNodeVersionId=" + this.siteNodeVersionId + "&siteNodeId=" + this.siteNodeId + "', 'SiteNodePreview', 'width=600,height=600,resizable=yes');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.previewSiteNode"), "Preview siteNode version"));	
		}
		
		return buttons;				
	}

	private ImageButton getMoveButton() throws Exception
	{
		return new ImageButton(true, "javascript:openPopup('ViewSiteNodeTree.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&hideLeafs=true', 'SiteNode', 'width=400,height=600,resizable=no');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.moveSiteNode"), "Move siteNode");
	}
	
	private ImageButton getMoveMultipleButton()
	{
		return new ImageButton(true, "javascript:openPopup('MoveMultipleSiteNodes!input.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "', 'MoveMultipleSiteNodes', 'width=400,height=640,resizable=no');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.moveMultipleSiteNodes"), "tool.structuretool.moveMultipleSiteNodes.header");	
	}

	private ImageButton getViewPageComponentsButton() throws Exception
	{
		try
		{
		    boolean isMetaInfoInWorkingState = false;
			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(this.repositoryId);
			Integer languageId = masterLanguageVO.getLanguageId();
			
			/*
			if(serviceBindingId != null)
			{
				List boundContents = ContentController.getBoundContents(serviceBindingId); 			
				if(boundContents.size() > 0)
				{
					ContentVO contentVO = (ContentVO)boundContents.get(0);
					ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageId);
					if(contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
						isMetaInfoInWorkingState = true;
				}
			}
			*/
			SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
			if(siteNodeVO.getMetaInfoContentId() != null && siteNodeVO.getMetaInfoContentId().intValue() != -1)
			{
				ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(siteNodeVO.getMetaInfoContentId(), languageId);
				if(contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
					isMetaInfoInWorkingState = true;
			}	
			
			logger.info("isMetaInfoInWorkingState:" + isMetaInfoInWorkingState);
			if(isMetaInfoInWorkingState)
			    return new ImageButton(CmsPropertyHandler.getComponentRendererUrl() + "ViewPage!renderDecoratedPage.action?siteNodeId=" + this.siteNodeId + "&languageId=" + masterLanguageVO.getId() + "&contentId=-1" + "&cmsUserName=" + formatter.encodeURI(this.getInfoGluePrincipal().getName()), getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeComponents"), "Site Node Components");
			    //return new ImageButton("ViewSiteNodePageComponents.action?siteNodeId=" + this.siteNodeId, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeComponents"), "Site Node Components");
			else
				return new ImageButton(true, "javascript:alert('Cannot edit this page. You must first set the meta info to working. Do this by entering node properties and changing the state to working.');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeComponents"), "Site Node Components");
		}
		catch(Exception e)
		{
			return new ImageButton(true, "javascript:alert('Cannot edit this page. You must first assign a metainfo content. Do this by entering node properties and fill in the information requested.');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeComponents"), "Site Node Components");
		}
	}

	private ImageButton getSimplePageComponentsButton() throws Exception
	{
		try
		{
		    boolean isMetaInfoInWorkingState = false;
			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(this.repositoryId);
			Integer languageId = masterLanguageVO.getLanguageId();

			/*
			if(serviceBindingId != null)
			{
				List boundContents = ContentController.getBoundContents(serviceBindingId); 			
				if(boundContents.size() > 0)
				{
					ContentVO contentVO = (ContentVO)boundContents.get(0);
					ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageId);
					if(contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
						isMetaInfoInWorkingState = true;
				}
			}	
			*/
			
			SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
			if(siteNodeVO.getMetaInfoContentId() != null && siteNodeVO.getMetaInfoContentId().intValue() != -1)
			{
				ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(siteNodeVO.getMetaInfoContentId(), languageId);
				if(contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
					isMetaInfoInWorkingState = true;
			}

			logger.info("isMetaInfoInWorkingState:" + isMetaInfoInWorkingState);
			if(isMetaInfoInWorkingState)
			    return new ImageButton(CmsPropertyHandler.getComponentRendererUrl() + "ViewPage!renderDecoratedPage.action?siteNodeId=" + this.siteNodeId + "&languageId=" + masterLanguageVO.getId() + "&contentId=-1&showSimple=true" + "&cmsUserName=" + formatter.encodeURI(this.getInfoGluePrincipal().getName()), getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeStructure"), "Site Node Structure");
			    //return new ImageButton("ViewSiteNodePageComponents.action?siteNodeId=" + this.siteNodeId, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeComponents"), "Site Node Components");
			else
				return new ImageButton(true, "javascript:alert('Cannot edit this page. You must first set the meta info to working. Do this by entering node properties and changing the state to working.');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeStructure"), "Site Node Structure");
		}
		catch(Exception e)
		{
			return new ImageButton(true, "javascript:alert('Cannot edit this page. You must first assign a metainfo content. Do this by entering node properties and fill in the information requested.');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeStructure"), "Site Node Structure");
		}
	}

	private ImageButton getExecuteTaskButton()
	{
		return new ImageButton(true, "javascript:openPopup('ViewExecuteTask.action?contentId=" + this.siteNodeId + "', 'SiteNode', 'width=400,height=600,resizable=yes,scrollbars=yes');", getLocalizedString(getSession().getLocale(), "images.global.buttons.executeTask"), "tool.common.executeTask.header");	
	}
	
	private ImageButton getPublishButton()
	{
		return new ImageButton(this.getCMSBaseUrl() + "/ViewListSiteNodeVersion.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.publishSiteNode"), "tool.structuretool.publishSiteNode.header");	
	}

	private ImageButton getPublishCurrentNodeButton()
	{
		return new ImageButton(this.getCMSBaseUrl() + "/ViewListSiteNodeVersion.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&recurseSiteNodes=false", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.publishCurrentSiteNode"), "tool.structuretool.publishCurrentSiteNode.header");	
	}

	private ImageButton getAccessRightsButton() throws Exception
	{
		String returnAddress = URLEncoder.encode(URLEncoder.encode("ViewSiteNode.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&stay=true", "UTF-8"), "UTF-8");
		return new ImageButton(this.getCMSBaseUrl() + "/ViewAccessRights.action?interceptionPointCategory=SiteNodeVersion&extraParameters=" + this.siteNodeVersionId +"&colorScheme=StructureTool&returnAddress=" + returnAddress, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeAccessRights"), "Site Node Access Rights");
	}
	
	public String getStateDescription()
	{
		String state = "";
		
		if(this.siteNodeVersionVO != null)
		{
			if(this.siteNodeVersionVO.getStateId().equals(SiteNodeVersionVO.WORKING_STATE))
				state = "<span style=\"color:#333333; font-weight: bold;\">(" + getLocalizedString(getSession().getLocale(), "tool.contenttool.state.working") + ")</span>";
			else if(this.siteNodeVersionVO.getStateId().equals(SiteNodeVersionVO.FINAL_STATE))
				state = "<span style=\"color:#AAAAAA; font-weight: bold;\">(" + getLocalizedString(getSession().getLocale(), "tool.contenttool.state.final") + ")</span>";
			else if(this.siteNodeVersionVO.getStateId().equals(SiteNodeVersionVO.PUBLISH_STATE))
				state = "<span style=\"color:#888888; font-weight: bold;\">(" + getLocalizedString(getSession().getLocale(), "tool.contenttool.state.publish") + ")</span>";
			else if(this.siteNodeVersionVO.getStateId().equals(SiteNodeVersionVO.PUBLISHED_STATE))
				state = "<span style=\"color:#666666; font-weight: bold;\">(" + getLocalizedString(getSession().getLocale(), "tool.contenttool.state.published") + ")</span>";
		}
		
		return state;
	}

	public SiteNodeVersionVO getSiteNodeVersionVO()
	{
		return siteNodeVersionVO;
	}

	public SiteNodeVO getSiteNodeVO()
	{
		return siteNodeVO;
	}
}
