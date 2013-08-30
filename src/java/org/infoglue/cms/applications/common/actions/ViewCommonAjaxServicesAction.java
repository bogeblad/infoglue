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

package org.infoglue.cms.applications.common.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.applications.databeans.ReferenceBean;
import org.infoglue.cms.applications.databeans.ReferenceVersionBean;
import org.infoglue.cms.applications.structuretool.actions.ViewListSiteNodeVersionAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentCategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.EventController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.controllers.kernel.impl.simple.RegistryController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.util.HttpHelper;

/**
 * This class implements the action class for the framed page in the content tool.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewCommonAjaxServicesAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewCommonAjaxServicesAction.class.getName());

	private static final long serialVersionUID = 1L;

	private List repositories = null;
	
	public String doRepositories() throws Exception
    {
		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(getInfoGluePrincipal(), false);
        
		return "successRepositories";
    }

	public String doSiteNodeIdPath() throws Exception
    {
		String siteNodeIdPath = SiteNodeController.getController().getSiteNodeIdPath(new Integer(getRequest().getParameter("siteNodeId")));
		this.getResponse().setContentType("text/plain");
		this.getResponse().getWriter().print("" + siteNodeIdPath);
		
		return NONE;
    }

	public String doSiteNodePath() throws Exception
    {
		String siteNodePath = SiteNodeController.getController().getSiteNodePath(new Integer(getRequest().getParameter("siteNodeId")), false, true);
		this.getResponse().setContentType("text/plain");
		this.getResponse().getWriter().print("" + siteNodePath);
		
		return NONE;
    }

	public String doContentIdPath() throws Exception
    {
		String contentIdPath = ContentController.getContentController().getContentIdPath(new Integer(getRequest().getParameter("contentId")));
		this.getResponse().setContentType("text/plain");
		this.getResponse().getWriter().print("" + contentIdPath);
		
		return NONE;
    }

	public String doContentPath() throws Exception
    {
		String contentPath = ContentController.getContentController().getContentPath(new Integer(getRequest().getParameter("contentId")), false, true);
		this.getResponse().setContentType("text/plain");
		this.getResponse().getWriter().print("" + contentPath);
		
		return NONE;
    }

	public String doRepositoryName() throws Exception
    {
		String repositoryName = "";
		Integer repositoryId = null;
		if(getRequest().getParameter("repositoryId") != null && getRequest().getParameter("repositoryId").equals(""))
			repositoryId = new Integer(getRequest().getParameter("repositoryId"));
		if(repositoryId == null)
		{
			String toolName = getRequest().getParameter("toolName");
			if(toolName.equals("ContentTool"))
			{
				repositoryId = getContentRepositoryId();
			}
			else if(toolName.equals("StructureTool"))
			{
				repositoryId = getStructureRepositoryId();
			}
		}
		if(repositoryId != null)
			repositoryName = RepositoryController.getController().getRepositoryVOWithId(new Integer(repositoryId)).getName();
		
		this.getResponse().setContentType("text/plain");
		this.getResponse().getWriter().print("" + repositoryName);
		
		return NONE;
    }

	public String doReferenceCount() throws Exception
    {
		List<Integer> uniqueList = new ArrayList<Integer>();
		List<ReferenceBean> refList = RegistryController.getController().getReferencingObjectsForContent(new Integer(getRequest().getParameter("contentId")), 100, true, true);
		for(ReferenceBean bean : refList)
		{
			if(bean.getReferencingCompletingObject() instanceof ContentVO)
			{
				for(ReferenceVersionBean versionBean : bean.getVersions())
				{
					uniqueList.add(((BaseEntityVO)versionBean.getReferencingObject()).getId());
				}
			}
			else
			{
				uniqueList.add(((BaseEntityVO)bean.getReferencingCompletingObject()).getId());
			}
		}
		this.getResponse().setContentType("text/plain");
		this.getResponse().getWriter().print("" + uniqueList.size());
		
		return NONE;
    }

	public String doAssetCount() throws Exception
    {
		List digitalAssets = DigitalAssetController.getDigitalAssetVOList(new Integer(getRequest().getParameter("contentVersionId")));

		this.getResponse().setContentType("text/plain");
		this.getResponse().getWriter().print("" + digitalAssets.size());
		
		return NONE;
    }

	public String doCategoryCount() throws Exception
    {
		List categories = ContentCategoryController.getController().findByContentVersion(new Integer(getRequest().getParameter("contentVersionId")));
		
		this.getResponse().setContentType("text/plain");
		this.getResponse().getWriter().print("" + categories.size());
		
		return NONE;
    }

	public String doPublishableNumberOfItems() throws Exception
    {
		String approveEntityName = getRequest().getParameter("approveEntityName");
		logger.info("approveEntityName:" + approveEntityName);
		String approveEntityId = getRequest().getParameter("approveEntityId");
		logger.info("approveEntityId:" + approveEntityId);
		String eventId = getRequest().getParameter("eventId");
		logger.info("eventId:" + eventId);
		
		Set<SiteNodeVersionVO> siteNodeVersionVOList = new HashSet<SiteNodeVersionVO>();
		Set<ContentVersionVO> contentVersionVOList = new HashSet<ContentVersionVO>();

		Set<SiteNodeVersionVO> siteNodeVersionVOListForApproval = new HashSet<SiteNodeVersionVO>();
		Set<ContentVersionVO> contentVersionVOListForApproval = new HashSet<ContentVersionVO>();

		Integer contentId = null;
		if(getRequest().getParameter("contentId") != null && !getRequest().getParameter("contentId").equals("") && !getRequest().getParameter("contentId").equals("null"))
			contentId = new Integer(getRequest().getParameter("contentId"));

		Integer siteNodeId = null;
		if(getRequest().getParameter("siteNodeId") != null && !getRequest().getParameter("siteNodeId").equals("") && !getRequest().getParameter("siteNodeId").equals("null"))
			siteNodeId = new Integer(getRequest().getParameter("siteNodeId"));
	
		SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
		SiteNodeVersionVO latestVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(siteNodeId);

		boolean isApprover = false;
		boolean hasAccessToPublishingTool = hasAccessTo("Common.ApproveDenyPublications", false);
		if(!hasAccessToPublishingTool)
			hasAccessToPublishingTool = hasAccessTo("PublishingTool.Read", false);
		if(hasAccessToPublishingTool)
		{
			boolean hasAccessToRepository = hasAccessTo("Repository.Read", "" + siteNodeVO.getRepositoryId());
			if(!hasAccessToRepository)
				hasAccessToRepository = hasAccessTo("Repository.Write", "" + siteNodeVO.getRepositoryId());
			
			if(hasAccessToRepository)
				isApprover = true;
		}
		
		List<EventVO> eventVOList = null;
		boolean showEvent = false;
		if(siteNodeId != null && isApprover)
		{
			if(latestVersion.getStateId().intValue() == SiteNodeVersionVO.PUBLISH_STATE)
			{
				eventVOList = EventController.getEventVOListForEntity(SiteNodeVersion.class.getName(), latestVersion.getId());
				showEvent = true;
			}
		}
		
		if(eventVOList != null && eventVOList.size() > 0)
		{
			eventId = eventVOList.get(0).getId().toString();
		}
		
		if(eventId != null && !eventId.equals(""))
		{
			try
			{
				EventVO event = EventController.getEventVOWithId(new Integer(eventId));
				logger.info("event:" + event.getEntityClass() + ":" + event.getEntityId());
				if(event.getTypeId() != 2)
				{
					if(event.getEntityClass().contains("SiteNodeVersion"))
					{
						SiteNodeVersionVO snvVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(event.getEntityId());
						if(snvVO.getSiteNodeId().toString().equals(approveEntityId) || showEvent)
						{
							this.getResponse().setContentType("text/plain");
							this.getResponse().setCharacterEncoding("UTF-8");
							this.getResponse().getWriter().print("" + getLocalizedString(getLocale(), "deliver.editOnSight.pendingPageApproval.title") + " <a href='#' onclick='$(\"#comment\").toggle();' style='background-image: url(css/images/v3/info.png); width: 16px; height:16px; display: inline-block;'>&nbsp;</a><div id='comment' style='display: none; position: fixed; padding: 6px; bottom: 42px; margin-left: -55px; width: 250px; height: 70px; color: black; background-color: white; box-shadow: rgba(0, 0, 0, 0.2) 0px -3px 3px;'><b>" + getLocalizedString(getLocale(), "tool.contenttool.versionComment") + ":</b> " + event.getDescription() + "<br/><b>" + getLocalizedString(getLocale(), "tool.contenttool.versionModifier") + ":</b> " + event.getCreator() + "</div><!-- approve=true eventId=" + event.getId() + " -->");
							return NONE;
						}
					}
					else if(event.getEntityClass().contains("ContentVersion"))
					{
						ContentVersionVO cvVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(event.getEntityId());
						logger.info("cvVO:" + cvVO);
						if(cvVO.getContentId().toString().equals(approveEntityId) || showEvent)
						{
							this.getResponse().setContentType("text/plain");
							this.getResponse().setCharacterEncoding("UTF-8");
							this.getResponse().getWriter().print("" + getLocalizedString(getLocale(), "deliver.editOnSight.pendingContentApproval.title") + " <a href='#' onclick='$(\"#comment\").toggle();' style='background-image: url(css/images/v3/info.png); width: 16px; height:16px; display: inline-block;'>&nbsp;</a><div id='comment' style='display: none; position: fixed; padding: 6px; bottom: 42px; margin-left: -55px; width: 250px; height: 70px; color: black; background-color: white; box-shadow: rgba(0, 0, 0, 0.2) 0px -3px 3px;'><b>" + getLocalizedString(getLocale(), "tool.contenttool.versionComment") + ":</b> " + event.getDescription() + "<br/><b>" + getLocalizedString(getLocale(), "tool.contenttool.versionModifier") + ":</b> " + event.getCreator() + "</div><!-- approve=true eventId=" + event.getId() + " -->");
							return NONE;
						}
					}
				}
			}
			catch (Exception e) 
			{
				logger.info("No event found probably:" + e.getMessage());
			}
		}
		
		if(contentId != null && contentId > -1)
		{
			ContentVO contentVO = ContentControllerProxy.getController().getACContentVOWithId(getInfoGluePrincipal(), contentId);
			List languageVOList = LanguageController.getController().getLanguageVOList(contentVO.getRepositoryId());
			Iterator languageVOListIterator = languageVOList.iterator();
			while(languageVOListIterator.hasNext())
			{
				LanguageVO language = (LanguageVO)languageVOListIterator.next();
				ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, language.getId());
				if(contentVersionVO != null && contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
				{
					contentVersionVOList.add(contentVersionVO);
				}
				else if(contentVersionVO != null && contentVersionVO.getStateId().equals(ContentVersionVO.PUBLISH_STATE))
				{
					contentVersionVOListForApproval.add(contentVersionVO);
				}
			}
		}

		ProcessBean processBean = ProcessBean.createProcessBean(ViewListSiteNodeVersionAction.class.getName(), "" + getInfoGluePrincipal().getName());
		SiteNodeVersionController.getController().getSiteNodeAndAffectedItemsRecursive(siteNodeId, SiteNodeVersionVO.WORKING_STATE, siteNodeVersionVOList, contentVersionVOList, false, false, this.getInfoGluePrincipal(), processBean, getLocale());
		
		this.getResponse().setContentType("text/plain");
		this.getResponse().setCharacterEncoding("UTF-8");
		if(latestVersion.getStateId().intValue() == SiteNodeVersionVO.PUBLISH_STATE)
		{
			String text = getLocalizedString(getLocale(), "deliver.editOnSight.toolbarStateWorking.text", new Integer[]{siteNodeVersionVOList.size(), contentVersionVOList.size()});
			if(siteNodeVersionVOList.size() > 0 || contentVersionVOList.size() > 0)
				this.getResponse().getWriter().print("" + text + " " + getLocalizedString(getLocale(), "deliver.editOnSight.pendingPageApproval.title"));			
			else
				this.getResponse().getWriter().print("" + getLocalizedString(getLocale(), "deliver.editOnSight.pendingPageApproval.title"));
		}
		else if(siteNodeVersionVOList.size() > 0 || contentVersionVOList.size() > 0)
		{
			String text = getLocalizedString(getLocale(), "deliver.editOnSight.toolbarStateWorking.text", new Integer[]{siteNodeVersionVOList.size(), contentVersionVOList.size()});
			this.getResponse().getWriter().print("" + text);
		}
		else
			this.getResponse().getWriter().print("" + (siteNodeVersionVOList.size() + contentVersionVOList.size()) + "");

		return NONE;
    }

	public String doValidateW3C() throws Exception
    {
		String markup = getRequest().getParameter("markup");
		
		if(markup != null && !markup.equals(""))
		{
			Hashtable inHash = new Hashtable();
			inHash.put("fragment", markup);
			inHash.put("output", "json");
			
			HttpHelper httpHelper = new HttpHelper();
			String result = httpHelper.postToUrl(CmsPropertyHandler.getW3CValidationServiceUrl(), inHash, "utf-8");
			logger.info("result:" + result);
			
			this.getResponse().setContentType("text/json");
			this.getResponse().getWriter().print(result);
		}
		
		return NONE;
    }

	public String doApprovePublication() throws Exception
	{
		PublicationVO publicationVO = new PublicationVO();
		publicationVO.setName("Auto publishing from EOS");
		publicationVO.setDescription("N/A");
		publicationVO.setPublicationDateTime(new Date());
		publicationVO.setPublisher(getUserName());
		publicationVO.setRepositoryId(new Integer(getRequest().getParameter("repositoryId")));
		String eventId = getRequest().getParameter("eventId");
		List<EventVO> events = new ArrayList<EventVO>();
		EventVO eventVO = EventController.getEventVOWithId(new Integer(eventId));
		events.add(eventVO);
		
    	publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, false, this.getInfoGluePrincipal());

    	this.getResponse().setContentType("text/plain");
		this.getResponse().getWriter().print("ok");
		
		return NONE;
	}
	
	public String doDenyPublication() throws Exception
	{
		PublicationVO publicationVO = new PublicationVO();
		publicationVO.setName("Denied publishing from EOS");
		publicationVO.setDescription("N/A");
		publicationVO.setPublicationDateTime(new Date());
		publicationVO.setPublisher(getUserName());
		publicationVO.setRepositoryId(new Integer(getRequest().getParameter("repositoryId")));
		String eventId = getRequest().getParameter("eventId");
		List<EventVO> events = new ArrayList<EventVO>();
		EventVO eventVO = EventController.getEventVOWithId(new Integer(eventId));
		events.add(eventVO);
		
		PublicationController.denyPublicationRequest(events, this.getInfoGluePrincipal(), "No comment", getApplicationBaseUrl(getRequest()));

    	this.getResponse().setContentType("text/plain");
		this.getResponse().getWriter().print("ok");
		
		return NONE;
	}
	
	public String doExecute() throws Exception
    {
		
        return "success";
    }

	
	public List getRepositories()
	{
		return repositories;
	}

	private String getApplicationBaseUrl(HttpServletRequest request)
	{
		return request.getRequestURL().toString().substring(0, request.getRequestURL().lastIndexOf("/") + 1) + "ViewCMSTool.action";
	}

}
