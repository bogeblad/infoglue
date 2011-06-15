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

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

import webwork.action.Action;

 
/**
  * This is the action-class for Update Access Rights
  * 
  * @author Mattias Bogeblad
  */

public class UpdateAccessRightsAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
	private Integer interceptionPointId;
	private Integer accessRightId;
	private String parameters = "";
	private String[] extraMultiParameter;
	private String roleName;
	private Boolean closeOnLoad = false;
	private String returnAddress;
	private String url;
	
	private String interceptionPointCategory;
	
	private ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
	
	public String doExecute() throws Exception
    {   
		AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
		if(this.extraMultiParameter == null || this.extraMultiParameter.length == 0)
		{
			if(interceptionPointCategory.equalsIgnoreCase("Content"))
			{	
				Integer contentId = new Integer(parameters);
				ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);
				if(!contentVO.getCreatorName().equalsIgnoreCase(this.getInfoGluePrincipal().getName()))
				{
					Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
					if(ContentControllerProxy.getController().getIsContentProtected(contentId) && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.ChangeAccessRights", protectedContentId.toString()))
						ceb.add(new AccessConstraintException("Content.contentId", "1006"));
				}
			}
			else if(interceptionPointCategory.equalsIgnoreCase("SiteNodeVersion"))
			{	
				Integer siteNodeVersionId = new Integer(parameters);
				SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);
				if(!siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(this.getInfoGluePrincipal().getName()))
				{
					Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId);
					if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "SiteNodeVersion.ChangeAccessRights", siteNodeVersionId.toString()))
						ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeId", "1006"));
				}
			}
			
			ceb.throwIfNotEmpty();
		}
		
		//logger.info("this.extraMultiParameters[i]:" + this.extraMultiParameter);
		if(this.extraMultiParameter != null && this.extraMultiParameter.length > 0)
		{
			for(int i=0; i<this.extraMultiParameter.length; i++)
			{
				//logger.info("this.extraMultiParameters[i]:" + this.extraMultiParameter[i]);
				AccessRightController.getController().update(this.extraMultiParameter[i], this.getRequest(), interceptionPointCategory);			
			}
		}
		else
		{
			//logger.info("this.parameters:" + this.parameters);
			AccessRightController.getController().update(this.parameters, this.getRequest(), interceptionPointCategory);			
		}
	
		this.url = getResponse().encodeRedirectURL(this.returnAddress);
		if(this.url.indexOf("ViewAccessRights.action") > -1)
			this.url = this.url + "&saved=true";
		if(this.closeOnLoad)
		{
			this.url = this.url.replaceAll("&KeepThis=true","&closeOnLoad=true&KeepThis=true");
		}
		
		if(this.returnAddress.indexOf("http") == 0)
		{
			getResponse().sendRedirect(url);
			return Action.NONE;
		}
		else
			return "success";
	}
	
	public String doAddGroups() throws Exception
    {   
		AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
		if(interceptionPointCategory.equalsIgnoreCase("Content"))
		{	
			Integer contentId = new Integer(parameters);
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);
			if(!contentVO.getCreatorName().equalsIgnoreCase(this.getInfoGluePrincipal().getName()))
			{
				Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
				if(ContentControllerProxy.getController().getIsContentProtected(contentId) && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.ChangeAccessRights", protectedContentId.toString()))
					ceb.add(new AccessConstraintException("Content.contentId", "1006"));
			}
		}
		else if(interceptionPointCategory.equalsIgnoreCase("SiteNodeVersion"))
		{	
			Integer siteNodeVersionId = new Integer(parameters);
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);
			if(!siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(this.getInfoGluePrincipal().getName()))
			{
				Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId);
				if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "SiteNodeVersion.ChangeAccessRights", siteNodeVersionId.toString()))
					ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeId", "1006"));
			}
		}
		
		ceb.throwIfNotEmpty();
		
		String[] groupNames = this.getRequest().getParameterValues("groupName");
		AccessRightController.getController().updateGroups(this.accessRightId, this.parameters, groupNames);

		this.url = getResponse().encodeRedirectURL(this.returnAddress);
		
		if(this.returnAddress.indexOf("http") == 0)
		{
			getResponse().sendRedirect(url);
			return Action.NONE;
		}
		else
			return "success";
	}

	public String doAddUser() throws Exception
    {   
		AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
		if(this.extraMultiParameter == null || this.extraMultiParameter.length == 0)
		{
			if(interceptionPointCategory.equalsIgnoreCase("Content"))
			{	
				Integer contentId = new Integer(parameters);
				ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);
				if(!contentVO.getCreatorName().equalsIgnoreCase(this.getInfoGluePrincipal().getName()))
				{
					Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
					if(ContentControllerProxy.getController().getIsContentProtected(contentId) && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.ChangeAccessRights", protectedContentId.toString()))
						ceb.add(new AccessConstraintException("Content.contentId", "1006"));
				}
			}
			else if(interceptionPointCategory.equalsIgnoreCase("SiteNodeVersion"))
			{	
				Integer siteNodeVersionId = new Integer(parameters);
				SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);
				if(!siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(this.getInfoGluePrincipal().getName()))
				{
					Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId);
					if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "SiteNodeVersion.ChangeAccessRights", siteNodeVersionId.toString()))
						ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeId", "1006"));
				}
			}
			
			ceb.throwIfNotEmpty();
		}
		
		String userName = this.getRequest().getParameter("userName");
		if(this.extraMultiParameter != null && this.extraMultiParameter.length > 0)
		{
			for(int i=0; i<this.extraMultiParameter.length; i++)
				AccessRightController.getController().addUser(interceptionPointCategory, this.extraMultiParameter[i], userName, this.getRequest());
		}
		else
		{
			AccessRightController.getController().addUser(interceptionPointCategory, this.parameters, userName, this.getRequest());
		}
		
		this.url = getResponse().encodeRedirectURL(this.returnAddress);
		
		if(this.returnAddress.indexOf("http") == 0)
		{
			getResponse().sendRedirect(url);
			return Action.NONE;
		}
		else
			return "success";
	}

	public String doAddUserV3() throws Exception
    {   
		return doAddUser();
    }
	
	public String doDeleteUser() throws Exception
    {   
		AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
		if(this.extraMultiParameter == null || this.extraMultiParameter.length == 0)
		{
			if(interceptionPointCategory.equalsIgnoreCase("Content"))
			{	
				Integer contentId = new Integer(parameters);
				ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);
				if(!contentVO.getCreatorName().equalsIgnoreCase(this.getInfoGluePrincipal().getName()))
				{
					Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
					if(ContentControllerProxy.getController().getIsContentProtected(contentId) && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.ChangeAccessRights", protectedContentId.toString()))
						ceb.add(new AccessConstraintException("Content.contentId", "1006"));
				}
			}
			else if(interceptionPointCategory.equalsIgnoreCase("SiteNodeVersion"))
			{	
				Integer siteNodeVersionId = new Integer(parameters);
				SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);
				if(!siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(this.getInfoGluePrincipal().getName()))
				{
					Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId);
					if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "SiteNodeVersion.ChangeAccessRights", siteNodeVersionId.toString()))
						ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeId", "1006"));
				}
			}
			
			ceb.throwIfNotEmpty();
		}
		
		String userName = this.getRequest().getParameter("userName");
		if(this.extraMultiParameter != null && this.extraMultiParameter.length > 0)
		{
			for(int i=0; i<this.extraMultiParameter.length; i++)
				AccessRightController.getController().deleteUser(interceptionPointCategory, this.extraMultiParameter[i], userName, this.getRequest());
		}
		else
		{
			AccessRightController.getController().deleteUser(interceptionPointCategory, this.parameters, userName, this.getRequest());
		}
		
		this.url = getResponse().encodeRedirectURL(this.returnAddress);
		
		if(this.returnAddress.indexOf("http") == 0)
		{
			getResponse().sendRedirect(url);
			return Action.NONE;
		}
		else
			return "success";
	}

	public String doDeleteUserV3() throws Exception
    {   
		doDeleteUser();
		
		this.url = getResponse().encodeRedirectURL(this.returnAddress);
		
		if(this.returnAddress.indexOf("http") == 0)
		{
			getResponse().sendRedirect(url);
			return Action.NONE;
		}
		else
			return "success";
    }
	
	public String doSaveAndExit() throws Exception
    {
		doExecute();
						
		return "saveAndExit";
	}

	public String doV3() throws Exception
    {
		doExecute();
						
		return "successV3";
	}

	public String doSaveAndExitV3() throws Exception
    {
		String result = doExecute();
		if(result.equals("none"))
			return result;
		else
			return "saveAndExitV3";
	}

	public String getReturnAddress()
	{
		return returnAddress;
	}

	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}

	public Integer getInterceptionPointId()
	{
		return this.interceptionPointId;
	}

	public void setInterceptionPointId(Integer interceptionPointId)
	{
		this.interceptionPointId = interceptionPointId;
	}

    public Integer getAccessRightId()
    {
        return accessRightId;
    }
    
    public void setAccessRightId(Integer accessRightId)
    {
        this.accessRightId = accessRightId;
    }

    public String getParameters()
	{
		return this.parameters;
	}

	public void setParameters(String parameters)
	{
		this.parameters = parameters;
	}

	public String[] getExtraMultiParameter()
	{
		return this.extraMultiParameter;
	}

	public void setExtraMultiParameter(String[] extraMultiParameter)
	{
		this.extraMultiParameter = extraMultiParameter;
	}

	public String getInterceptionPointCategory()
	{
		return interceptionPointCategory;
	}

	public void setInterceptionPointCategory(String interceptionPointCategory)
	{
		this.interceptionPointCategory = interceptionPointCategory;
	}

	public Boolean getCloseOnLoad()
	{
		return closeOnLoad;
	}

	public void setCloseOnLoad(Boolean closeOnLoad)
	{
		this.closeOnLoad = closeOnLoad;
	}
	
	public String getUrl()
	{
		return url;
	}

}
