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

import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryLanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeControllerProxy;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class CreateRepositoryAction extends InfoGlueAbstractAction
{
	private RepositoryVO repositoryVO;
	private ConstraintExceptionBuffer ceb;
   	private String userSessionKey;
    private String returnAddress;

	public CreateRepositoryAction()
	{
		this(new RepositoryVO());
	}
	
	public CreateRepositoryAction(RepositoryVO repositoryVO)
	{
		this.repositoryVO = repositoryVO;
		this.ceb = new ConstraintExceptionBuffer();
			
	}	
	public Integer getRepositoryId()
	{
		return this.repositoryVO.getId();	
	}
    
    public java.lang.String getName()
    {
        return this.repositoryVO.getName();
    }
        
    public void setName(java.lang.String name)
    {
       	this.repositoryVO.setName(name);
    }
      
    public String getDescription()
    {
        return this.repositoryVO.getDescription();
    }
        
    public void setDescription(String description)
    {
       	this.repositoryVO.setDescription(description);
    }

	public String getDnsName()
    {
        return this.repositoryVO.getDnsName();
    }
        
    public void setDnsName(String dnsName)
    {
       	this.repositoryVO.setDnsName(dnsName);
    }

	public String getUserSessionKey()
	{
		return userSessionKey;
	}

	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}

	public String getReturnAddress()
	{
		return returnAddress;
	}

	public List getAvailableLanguages() throws Exception
	{
		return LanguageController.getController().getLanguageVOList();
	}
	
	public void setUserSessionKey(String userSessionKey)
	{
		this.userSessionKey = userSessionKey;
	}

    public String doExecute() throws Exception
    {
		ceb.add(this.repositoryVO.validate());
    	ceb.throwIfNotEmpty();				
    	
		this.repositoryVO = RepositoryController.getController().create(repositoryVO);
		
    	String[] values = getRequest().getParameterValues("languageId");
    	if(values != null && values.length > 0)
    	{
    		RepositoryLanguageController.getController().updateRepositoryLanguages(this.repositoryVO.getId(),values);
    	}
    	
	    ViewMessageCenterAction.addSystemMessage(this.getInfoGluePrincipal().getName(), ViewMessageCenterAction.SYSTEM_MESSAGE_TYPE, "refreshRepositoryList();");

        return "success";
    }

    public String doInput() throws Exception
    {
    	return "input";
    }    

    public String doV3() throws Exception
    {
		ceb.add(this.repositoryVO.validate());
    	ceb.throwIfNotEmpty();				
    	
		this.repositoryVO = RepositoryController.getController().create(repositoryVO);

		String[] values = getRequest().getParameterValues("languageId");
    	if(values != null && values.length > 0)
    	{
    		RepositoryLanguageController.getController().updateRepositoryLanguages(this.repositoryVO.getId(),values);
    	}

	    ViewMessageCenterAction.addSystemMessage(this.getInfoGluePrincipal().getName(), ViewMessageCenterAction.SYSTEM_MESSAGE_TYPE, "refreshRepositoryList();");

		//String createSiteNodeInlineOperationViewCreatedPageLinkText = getLocalizedString(getLocale(), "tool.structuretool.createSiteNodeInlineOperationViewCreatedPageLinkText");
		//String createSiteNodeInlineOperationViewCreatedPageTitleText = getLocalizedString(getLocale(), "tool.structuretool.createSiteNodeInlineOperationViewCreatedPageTitleText");

		//addActionLink(userSessionKey, new LinkBean("newPageUrl", createSiteNodeInlineOperationViewCreatedPageLinkText, createSiteNodeInlineOperationViewCreatedPageTitleText, createSiteNodeInlineOperationViewCreatedPageTitleText, getDecoratedPageUrl(newSiteNodeVO.getId()), false, "", "structure"));
    	        
        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
	        String arguments 	= "userSessionKey=" + userSessionKey + "&isAutomaticRedirect=false";
	        String messageUrl 	= returnAddress + (returnAddress.indexOf("?") > -1 ? "&" : "?") + arguments;
	        
	        this.getResponse().sendRedirect(messageUrl);
	        return NONE;
        }
        else
        {
        	return "success";
        }
    }


    public String doInputV3() throws Exception
    {    	
        userSessionKey = "" + System.currentTimeMillis();

		//String createSiteNodeInlineOperationDoneHeader = getLocalizedString(getLocale(), "tool.structuretool.createSiteNodeInlineOperationDoneHeader", parentSiteNodeVO.getName());
        //String createSiteNodeInlineOperationBackToCurrentPageLinkText = getLocalizedString(getLocale(), "tool.structuretool.createSiteNodeInlineOperationBackToCurrentPageLinkText");
        //String createSiteNodeInlineOperationBackToCurrentPageTitleText = getLocalizedString(getLocale(), "tool.structuretool.createSiteNodeInlineOperationBackToCurrentPageTitleText");

        //setActionMessage(userSessionKey, createSiteNodeInlineOperationDoneHeader);
        //addActionLink(userSessionKey, new LinkBean("currentPageUrl", createSiteNodeInlineOperationBackToCurrentPageLinkText, createSiteNodeInlineOperationBackToCurrentPageTitleText, createSiteNodeInlineOperationBackToCurrentPageTitleText, this.originalAddress, false, ""));

		return "inputV3";
    }
}
