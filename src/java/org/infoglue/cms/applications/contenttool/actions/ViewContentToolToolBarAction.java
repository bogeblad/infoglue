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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.ImageButton;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * This class implements the action class for the framed page in the content tool.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewContentToolToolBarAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewContentToolToolBarAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private String title = "";
	private String name  = "";
	private String toolbarKey = "";
	private String url   = "";
	private Boolean isBranch = new Boolean(false);
		
	//All id's that are used
	private Integer repositoryId = null;
	private Integer siteNodeId = null;
	private Integer languageId = null;
	private Integer contentId = null;
	private Integer contentVersionId = null;
	private Integer lastPublishedContentVersionId = null;
	private String languageName = "";
	
	private String extraInformation = "";
	
	private ContentVO contentVO = null;
	
	public String doExecute() throws SystemException
    {
		if(this.contentId != null)
		{
			this.contentVO = ContentController.getContentController().getContentVOWithId(this.contentId);
		}
			
    	if(this.repositoryId == null && this.contentId != null)
    	{
			this.repositoryId = ContentController.getContentController().getContentVOWithId(this.contentId).getRepositoryId();
	    	SiteNodeVO rootSiteNodeVO = SiteNodeController.getController().getRootSiteNodeVO(this.repositoryId);
			if(rootSiteNodeVO != null)
				this.siteNodeId = rootSiteNodeVO.getId();
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

	public Integer getContentId()
	{
		return this.contentId;
	}                   

	public void setContentId(Integer contentId)
	{
		this.contentId = contentId;
	}

	public Integer getContentVersionId()
	{
		return this.contentVersionId;
	}                   
	
	public void setContentVersionId(Integer contentVersionId)
	{
		this.contentVersionId = contentVersionId;
	}                   

	public Integer getLanguageId()
	{
		return this.languageId;
	}                   

	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
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

	public List getButtons()
	{
		logger.info("Title:" + this.title);
		logger.info("toolbarKey:" + this.toolbarKey);
		
		if(this.toolbarKey.equalsIgnoreCase("content details"))
		{
			if(this.isBranch.booleanValue())
				return getBranchContentButtons();
			return getContentButtons();
		}	
		if(this.toolbarKey.equalsIgnoreCase("content version"))
		{
			return this.getContentVersionButtons();
		}
		if(this.toolbarKey.equalsIgnoreCase("ContentVersionHistory"))
		{
			return this.getContentVersionHistoryButtons();
		}
					
		return null;				
	}

	/**
	 * This method checks if there are published versions available for the contentVersion.
	 */
	
	private boolean hasAnyPublishedVersion()
	{
		boolean hasPublishedVersion = false;
		
		try
		{
			ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestPublishedContentVersion(this.contentId);
			if(contentVersion != null)
			{
				hasPublishedVersion = true;
				lastPublishedContentVersionId = contentVersion.getContentVersionId();
				this.repositoryId = contentVersion.getOwningContent().getRepository().getId();
				this.name = contentVersion.getOwningContent().getName();
				this.languageName = contentVersion.getLanguage().getName();
				this.contentId = contentVersion.getOwningContent().getId();
				this.languageId = contentVersion.getLanguage().getId();
			}
		}
		catch(Exception e){}
				
		return hasPublishedVersion;
	}
	
	/**
	 * This method checks if there are published versions available for the contentVersion.
	 */
	
	private boolean hasPublishedVersion()
	{
		boolean hasPublishedVersion = false;
		
		try
		{
			ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestPublishedContentVersion(this.contentId, this.languageId);
			if(contentVersion != null)
			{
				hasPublishedVersion = true;
				lastPublishedContentVersionId = contentVersion.getContentVersionId();
				this.repositoryId = contentVersion.getOwningContent().getRepository().getId();
				this.name = contentVersion.getOwningContent().getName();
				this.languageName = contentVersion.getLanguage().getName();
				this.contentId = contentVersion.getOwningContent().getId();
				this.languageId = contentVersion.getLanguage().getId();
			}
		}
		catch(Exception e){}
				
		return hasPublishedVersion;
	}
	
	/**
	 * This method checks if there are a version available.
	 */
	
	private boolean hasVersion()
	{
		boolean hasVersion = false;
		
		try
		{
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(this.contentId, this.languageId);
			if(contentVersionVO != null)
			{
				hasVersion = true;
			}
		}
		catch(Exception e){}
				
		return hasVersion;
	}
	
	/**
	 * This method checks if the content version is read only (ie publish, published or final).
	 */
	
	private boolean isReadOnly()
	{
		boolean isReadOnly = false;
		
		try
		{
			ContentVersionVO contentVersion = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId);
			if(contentVersion != null && (contentVersion.getStateId().intValue() == 1 || contentVersion.getStateId().intValue() == 2 || contentVersion.getStateId().intValue() == 3))
			{
				isReadOnly = true;	
			}
		}
		catch(Exception e){}
				
		return isReadOnly;
	}


	private List getBranchContentButtons()
	{
		List buttons = new ArrayList();
		
		try
		{
			buttons.add(new ImageButton("CreateContent!input.action?isBranch=false&parentContentId=" + this.contentId + "&repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.newContent"), "tool.contenttool.newContent.header"));	
			buttons.add(new ImageButton("CreateContent!input.action?isBranch=true&parentContentId=" + this.contentId + "&repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.newContentFolder"), "tool.contenttool.newContentFolder.header"));	
			
			ImageButton moveButton = getMoveButton();
			moveButton.getSubButtons().add(getMoveMultipleButton());
			buttons.add(moveButton);
			
			ImageButton deleteButton = getDeleteButton();
			ImageButton deleteChildrenButton = getDeleteChildrenButton();
			deleteButton.getSubButtons().add(deleteChildrenButton);
			
			buttons.add(deleteButton);
			
			buttons.add(getPublishButton());
			//if(hasAnyPublishedVersion())
			//{
		    ImageButton unpublishButton = getUnpublishButton();
		    ImageButton unpublishAllButton = getUnpublishAllButton();
		    unpublishButton.getSubButtons().add(unpublishAllButton);
		    
		    buttons.add(unpublishButton);
			//}
		
			buttons.add(new ImageButton("ViewContentProperties.action?contentId=" + this.contentId, getLocalizedString(getSession().getLocale(), "images.global.buttons.editProperties"), "Edit Properties", new Integer(22), new Integer(80)));

			if(this.contentVO.getIsProtected().intValue() == ContentVO.YES.intValue())
			{
			    ImageButton accessRightsButton = getAccessRightsButton();
				buttons.add(accessRightsButton);
			}

			if(this.contentVO.getContentTypeDefinitionId() != null)
			{
				ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(this.contentVO.getContentTypeDefinitionId());
				if(contentTypeDefinitionVO != null && (contentTypeDefinitionVO.getName().equalsIgnoreCase("HTMLTemplate") || contentTypeDefinitionVO.getName().equalsIgnoreCase("PageTemplate") || contentTypeDefinitionVO.getName().equalsIgnoreCase("PagePartTemplate")))
				{
					buttons.add(getComponentAccessRightsButton());
					buttons.add(getDeployComponentButton());
				}
			}
			
			buttons.add(new ImageButton("ViewContentVersionHistory.action?contentId=" + this.contentId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.viewHistory"), "History", new Integer(22), new Integer(80)));

			buttons.add(getSyncTreeButton());

			buttons.add(getExecuteTaskButton());

			//if(this.getInfoGluePrincipal().getIsAdministrator())
				buttons.add(new ImageButton("UpdateContent!inputContentType.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.changeContentType"), "tool.contenttool.changeContentType.header"));	

			if(AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "ContentTool.ImportExport", true))
			{
				ImageButton exportButton = new ImageButton("ExportContent!input.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.exportContent"), "tool.contenttool.exportContent.header");
				ImageButton importButton = new ImageButton("ImportContent!input.action?parentContentId=" + this.contentId + "&repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.importContent"), "tool.contenttool.importContent.header");
				exportButton.getSubButtons().add(importButton);
				buttons.add(exportButton);
			}
		}
		catch(Exception e)
		{
			logger.warn("Exception when generating buttons:" + e.getMessage(), e);
		}
		
		return buttons;
	}
	
	private List getContentButtons()
	{
		List buttons = new ArrayList();
		try
		{
			buttons.add(getDeleteButton());	
			
			ImageButton moveButton = getMoveButton();
			moveButton.getSubButtons().add(getMoveMultipleButton());
			buttons.add(moveButton);
			
			buttons.add(getPublishButton());
			if(hasAnyPublishedVersion())
			{
			    ImageButton unpublishButton = getUnpublishButton();
			    ImageButton unpublishAllButton = getUnpublishAllButton();
			    unpublishButton.getSubButtons().add(unpublishAllButton);
			
			    buttons.add(unpublishButton);
			}
			
			if(this.contentVO.getIsProtected().intValue() == ContentVO.YES.intValue())
			{
			    ImageButton accessRightsButton = getAccessRightsButton();
				buttons.add(accessRightsButton);
			}

			if(this.contentVO.getContentTypeDefinitionId() != null)
			{
				ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(this.contentVO.getContentTypeDefinitionId());
				if(contentTypeDefinitionVO != null && (contentTypeDefinitionVO.getName().equalsIgnoreCase("HTMLTemplate") || contentTypeDefinitionVO.getName().equalsIgnoreCase("PageTemplate") || contentTypeDefinitionVO.getName().equalsIgnoreCase("PagePartTemplate")))
				{
					buttons.add(getComponentAccessRightsButton());
					buttons.add(getDeployComponentButton());
				}
			}
			
			buttons.add(new ImageButton("ViewContentVersionHistory.action?contentId=" + this.contentId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.viewHistory"), "History", new Integer(22), new Integer(80)));
			
			buttons.add(getSyncTreeButton());
			
			buttons.add(getExecuteTaskButton());
			
			//if(this.getInfoGluePrincipal().getIsAdministrator())
				buttons.add(new ImageButton("UpdateContent!inputContentType.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.changeContentType"), "tool.contenttool.changeContentType.header"));
		
			ImageButton exportButton = new ImageButton("ExportContent!input.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.exportContent"), "tool.contenttool.exportContent.header");
			ImageButton importButton = new ImageButton("ImportContent!input.action?parentContentId=" + this.contentId + "&repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.importContent"), "tool.contenttool.importContent.header");
			exportButton.getSubButtons().add(importButton);
			buttons.add(exportButton);
		
		}
		catch(Exception e)
		{
			logger.warn("Exception when generating buttons:" + e.getMessage(), e);
		}

		return buttons;				
	}


	private List getContentVersionButtons()
	{
		List buttons = new ArrayList();
		
		try
		{
		    boolean latest = true;
		    if(this.contentVersionId != null)
		    {
		        ContentVersionVO currentContentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId);
		        ContentVersionVO latestContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(currentContentVersionVO.getContentId(), currentContentVersionVO.getLanguageId());
		        if(currentContentVersionVO != null && latestContentVersionVO != null && currentContentVersionVO.getId().intValue() != latestContentVersionVO.getId().intValue())
		            latest = false;
		        
		        extraInformation = "" + getStateDescription(currentContentVersionVO);
		    }
		    
		    buttons.add(getCoverButton());
		    
		    if(latest)
		    {
				buttons.add(getDeleteButton());
			    
			    if(this.contentVersionId != null)
				{
			        if(!isReadOnly())
						buttons.add(new ImageButton(true, "javascript:openPopup('ViewDigitalAsset.action?contentVersionId=" + this.contentVersionId + "', 'FileUpload', 'width=400,height=200,resizable=no');", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.newAsset"), "tool.contenttool.uploadDigitalAsset.header"));	
									
					if(hasPublishedVersion())
					{
					    ImageButton unpublishButton = getUnpublishButton();
					    ImageButton unpublishAllButton = getUnpublishAllButton();
					    unpublishButton.getSubButtons().add(unpublishAllButton);

					    buttons.add(unpublishButton);
					}
						
					if(!isReadOnly())
						buttons.add(getPublishButton());
					
					if(this.contentVO.getIsProtected().intValue() == ContentVO.YES.intValue())
					{
					    ImageButton accessRightsButton = getAccessRightsButton();
					    accessRightsButton.getSubButtons().add(getContentVersionAccessRightsButton());
					    
						buttons.add(accessRightsButton);
					}
	
					if(this.contentVO.getContentTypeDefinitionId() != null)
					{
						ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(this.contentVO.getContentTypeDefinitionId());
						if(contentTypeDefinitionVO != null && (contentTypeDefinitionVO.getName().equalsIgnoreCase("HTMLTemplate") || contentTypeDefinitionVO.getName().equalsIgnoreCase("PageTemplate") || contentTypeDefinitionVO.getName().equalsIgnoreCase("PagePartTemplate")))
						{
							List interceptionPointVOList = InterceptionPointController.getController().getInterceptionPointVOList("Component");
							if(interceptionPointVOList != null && interceptionPointVOList.size() > 0)
								buttons.add(getComponentAccessRightsButton());
							
							buttons.add(getDeployComponentButton());
						}
					}

					if(this.siteNodeId != null)
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
					    
					    ImageButton previewSiteButton = new ImageButton(true, "javascript:openPopup('" + workingUrl + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "', 'SitePreview', 'width=800,height=600,resizable=yes,toolbar=yes,scrollbars=yes,status=yes,location=yes,menubar=yes');", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.previewSite"), "tool.contenttool.previewSite.header");
						ImageButton previewContentButton = new ImageButton(true, "javascript:openPopup('ViewContentVersion!preview.action?contentVersionId=" + this.contentVersionId + "&contentId=" + this.contentId + "&languageId=" + this.languageId + "', 'ContentPreview', 'width=800,height=600,resizable=yes,toolbar=yes,scrollbars=yes,status=yes,location=yes,menubar=yes');", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.previewContent"), "tool.contenttool.previewContent.header");	
						previewSiteButton.getSubButtons().add(previewContentButton);
	
						buttons.add(previewSiteButton);			
					}

				}
				buttons.add(getSyncTreeButton());
				
				if(hasVersion())
				{
					buttons.add(getChangeLanguageButton());
					buttons.add(getShowXMLButton());
				}
		    }
		    else
		    {
				buttons.add(getDeleteVersionButton());
		    }
		}
		catch(Exception e)
		{
			logger.warn("Exception when generating buttons:" + e.getMessage(), e);
		}
		
		return buttons;				
	}

	

	private List getContentVersionHistoryButtons()
	{
		List buttons = new ArrayList();
		
		try
		{
		    buttons.add(getCompareButton());
		}
		catch(Exception e)
		{
			logger.warn("Exception when generating buttons:" + e.getMessage(), e);
		}

		return buttons;				
	}

	private ImageButton getCompareButton()
	{
	    return new ImageButton(true, "javascript:compareVersions('contentVersion');", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.compareVersions"), "tool.contenttool.compareVersions.header");
		
	    //return new ImageButton(true, "javascript:openPopup('ViewContentVersionDifference.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId + "&hideLeafs=true', 'MoveContent', 'width=400,height=600,resizable=no');", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.moveContent"), "tool.contenttool.moveContent.header");	
	}

	
	private ImageButton getCoverButton()
	{
		try
		{
			return new ImageButton("ViewContent.action?contentId=" + this.contentId + "&stay=true", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.contentCover"), "tool.contenttool.contentDetailsHeader");
		}
		catch(Exception e){}

		return null;
	}
	
	private ImageButton getUnpublishButton()
	{
		try
		{
			return new ImageButton("UnpublishContentVersion!input.action?contentId=" + this.contentId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.unpublishVersion"), "tool.contenttool.unpublishVersion.header");
		}
		catch(Exception e){}

		return null;
	}

	private ImageButton getUnpublishAllButton()
	{
	    try
		{
			return new ImageButton("UnpublishContentVersion!inputChooseContents.action?contentId=" + this.contentId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.unpublishAllVersion"), "tool.contenttool.unpublishAllVersion.header");
		}
		catch(Exception e){}

		return null;

		//return new ImageButton(true, "javascript:openPopup('MoveMultipleContent!input.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId + "', 'MoveMultipleContent', 'width=400,height=600,resizable=no');", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.moveMultipleContent"), "tool.contenttool.moveMultipleContent.header");	
	}

	private ImageButton getDeleteButton()
	{
		try
		{
			String url = "Confirm.action?header=tool.contenttool.deleteContent.header&yesDestination=" + URLEncoder.encode(URLEncoder.encode("DeleteContent.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId + "&changeTypeId=4", "UTF-8"), "UTF-8") + "&noDestination=" + URLEncoder.encode(URLEncoder.encode("ViewContent.action?title=Content&contentId=" + this.contentId + "&repositoryId=" + this.repositoryId, "UTF-8"), "UTF-8") + "&message=tool.contenttool.deleteContent.text";
			return new ImageButton(url, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.deleteContent"), "tool.contenttool.deleteContent.header");
		}
		catch(Exception e){e.printStackTrace();}

		return null;
	}

	private ImageButton getDeleteChildrenButton()
	{
		try
		{
			String url = "Confirm.action?header=tool.contenttool.deleteContentChildren.header&yesDestination=" + URLEncoder.encode(URLEncoder.encode("DeleteContentChildren.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId + "&changeTypeId=4", "UTF-8"), "UTF-8") + "&noDestination=" + URLEncoder.encode(URLEncoder.encode("ViewContent.action?title=Content&contentId=" + this.contentId + "&repositoryId=" + this.repositoryId, "UTF-8"), "UTF-8") + "&message=tool.contenttool.deleteContentChildren.text";
			return new ImageButton(url, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.deleteChildren"), "tool.contenttool.deleteChildren.header");
		}
		catch(Exception e){e.printStackTrace();}

		return null;
	}

	private ImageButton getDeleteVersionButton()
	{
		try
		{
			String url = "Confirm.action?header=tool.contenttool.deleteContentVersion.header&yesDestination=" + URLEncoder.encode(URLEncoder.encode("DeleteContentVersion.action?contentVersionId=" + this.contentVersionId + "&repositoryId=" + this.repositoryId + "&contentId=" + this.contentId, "UTF-8"), "UTF-8") + "&noDestination=" + URLEncoder.encode(URLEncoder.encode("ViewContentVersionHistory.action", "UTF-8"), "UTF-8") + "&message=tool.contenttool.deleteContentVersion.text";
			return new ImageButton(url, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.deleteContentVersion"), "tool.contenttool.deleteContentVersion.header");
		}
		catch(Exception e){e.printStackTrace();}

		return null;
	}

	private ImageButton getMoveButton()
	{
		return new ImageButton(true, "javascript:openPopup('ViewContentTree.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId + "&hideLeafs=true', 'MoveContent', 'width=400,height=600,resizable=no');", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.moveContent"), "tool.contenttool.moveContent.header");	
	}

	private ImageButton getMoveMultipleButton()
	{
		return new ImageButton(true, "javascript:openPopup('MoveMultipleContent!input.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId + "', 'MoveMultipleContent', 'width=400,height=640,resizable=no');", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.moveMultipleContent"), "tool.contenttool.moveMultipleContent.header");	
	}

	private ImageButton getSyncTreeButton()
	{
		return new ImageButton(true, "javascript:parent.frames['main'].syncWithTree();", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.revealInTree"), "tool.contenttool.revealInTree.header");	
	}

	private ImageButton getChangeLanguageButton()
	{
		return new ImageButton("ChangeVersionLanguage!input.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId + "&contentVersionId=" + this.contentVersionId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.changeVersionLanguage"), "tool.contenttool.changeVersionLanguage.header");	
	}

	private ImageButton getShowXMLButton()
	{
		return new ImageButton("ViewContentVersion!asXML.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId + "&contentVersionId=" + this.contentVersionId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.showVersionAsXml"), "tool.contenttool.showVersionAsXml.header");	
	}

	private ImageButton getPublishButton()
	{
		return new ImageButton("ViewListContentVersion.action?contentId=" + this.contentId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.publishContent"), "tool.contenttool.publishContent.header");	
	}

	private ImageButton getExecuteTaskButton()
	{
		return new ImageButton(true, "javascript:openPopup('ViewExecuteTask.action?contentId=" + this.contentId + "', 'ExecuteTask', 'width=400,height=600,resizable=yes,scrollbars=yes');", getLocalizedString(getSession().getLocale(), "images.global.buttons.executeTask"), "tool.common.executeTask.header");	
	}
	
	private ImageButton getAccessRightsButton() throws UnsupportedEncodingException
	{
		String returnAddress = URLEncoder.encode(URLEncoder.encode("ViewContent.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId + "&stay=true", "UTF-8"), "UTF-8");
		//return new ImageButton("ViewAccessRights.action?name=Content&value=" + this.contentId + "&returnAddress=" + returnAddress, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.contentAccessRights"), "Content Access Rights");
		return new ImageButton("ViewAccessRights.action?interceptionPointCategory=Content&extraParameters=" + this.contentId +"&colorScheme=ContentTool&returnAddress=" + returnAddress, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.contentAccessRights"), "tool.contenttool.contentAccessRights.header");
	}

	private ImageButton getContentVersionAccessRightsButton() throws UnsupportedEncodingException
	{
		String returnAddress = URLEncoder.encode(URLEncoder.encode("ViewContentVersion.action?contentVersionId=" + this.contentVersionId + "&contentId=" + contentId + "&languageId=" + languageId, "UTF-8"), "UTF-8");
		return new ImageButton("ViewAccessRights.action?interceptionPointCategory=ContentVersion&extraParameters=" + this.contentVersionId +"&colorScheme=ContentTool&returnAddress=" + returnAddress, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.contentAccessRights"), "tool.contenttool.contentVersionAccessRights.header");
	}

	private ImageButton getComponentAccessRightsButton() throws UnsupportedEncodingException
	{
		String returnAddress = URLEncoder.encode(URLEncoder.encode("ViewContent.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId + "&stay=true", "UTF-8"), "UTF-8");
		return new ImageButton("ViewAccessRights.action?interceptionPointCategory=Component&extraParameters=" + this.contentId +"&colorScheme=ContentTool&returnAddress=" + returnAddress, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.componentAccessRights"), "tool.contenttool.componentAccessRights.header");
	}

	private ImageButton getDeployComponentButton()
	{
		return new ImageButton(true, "javascript:if(top.openInlineDiv) top.openInlineDiv('ViewDeploymentChooseServer!inputQuickV3.action?contentId=" + this.contentId + "', 600, 800, true, true, 'Deploy local component'); else openPopup('ViewDeploymentChooseServer!inputQuickV3.action?contentId=" + this.contentId + "', 'Deploy', 'width=800,height=600,resizable=yes,scrollbars=yes');", getLocalizedString(getSession().getLocale(), "images.global.buttons.deployComponent"), "tool.common.deployComponent.header");	
		//return new ImageButton(true, "javascript:openPopup('ViewDeploymentChooseServer!inputQuickV3.action?contentId=" + this.contentId + "', 'Deploy', 'width=800,height=600,resizable=yes,scrollbars=yes');", getLocalizedString(getSession().getLocale(), "images.global.buttons.deployComponent"), "tool.common.deployComponent.header");	
	}

	public String getStateDescription(ContentVersionVO contentVersionVO)
	{
		String state = "";
		
		if(contentVersionVO != null)
		{
			if(contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
				state = "<span style=\"color:#333333; font-weight: bold;\">(" + getLocalizedString(getSession().getLocale(), "tool.contenttool.state.working") + ")</span>";
			else if(contentVersionVO.getStateId().equals(ContentVersionVO.FINAL_STATE))
				state = "<span style=\"color:#AAAAAA; font-weight: bold;\">(" + getLocalizedString(getSession().getLocale(), "tool.contenttool.state.final") + ")</span>";
			else if(contentVersionVO.getStateId().equals(ContentVersionVO.PUBLISH_STATE))
				state = "<span style=\"color:#888888; font-weight: bold;\">(" + getLocalizedString(getSession().getLocale(), "tool.contenttool.state.publish") + ")</span>";
			else if(contentVersionVO.getStateId().equals(ContentVersionVO.PUBLISHED_STATE))
				state = "<span style=\"color:#666666; font-weight: bold;\">(" + getLocalizedString(getSession().getLocale(), "tool.contenttool.state.published") + ")</span>";
		}
		
		return state;
	}

	public String getExtraInformation()
	{
		return extraInformation;
	}

}
