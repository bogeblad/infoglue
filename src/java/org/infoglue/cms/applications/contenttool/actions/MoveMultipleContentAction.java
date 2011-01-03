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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.dom.DOMBuilder;

/**
 * This action represents the CreateContent Usecase.
 */

public class MoveMultipleContentAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
    //Initial params
    private Integer originalContentId;
    private Integer repositoryId;
    private Integer contentId;
    private Integer parentContentId;
    private List qualifyers = new ArrayList();
    private boolean errorsOccurred = false;
	protected List repositories = null;
    
   	private String userSessionKey;
    private String originalAddress;
    private String returnAddress;

    //Move params
    protected String qualifyerXML = null;
    private Integer newParentContentId;
    
    //Tree params
    private Integer changeTypeId;
    private Integer topContentId;

    private ConstraintExceptionBuffer ceb;
   	private ContentVO contentVO;
  
  
  	public MoveMultipleContentAction()
	{
		this(new ContentVO());
	}
	
	public MoveMultipleContentAction(ContentVO contentVO)
	{
		this.contentVO = contentVO;
		this.ceb = new ConstraintExceptionBuffer();			
	}	

	public void setContentId(Integer contentId)
	{
		this.contentVO.setContentId(contentId);
	}

	public void setNewParentContentId(Integer newParentContentId)
	{
		this.newParentContentId = newParentContentId;
	}

	public void setParentContentId(Integer parentContentId)
	{
		this.parentContentId = parentContentId;
	}

	public void setChangeTypeId(Integer changeTypeId)
	{
		this.changeTypeId = changeTypeId;
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}
	
	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}

	public Integer getParentContentId()
	{
		return this.parentContentId;
	}
	    
	public Integer getContentId()
	{
		return contentVO.getContentId();
	}

	public Integer getNewParentContentId()
	{
		return this.newParentContentId;
	}
    
	public Integer getUnrefreshedContentId()
	{
		return this.newParentContentId;
	}

	public Integer getChangeTypeId()
	{
		return this.changeTypeId;
	}
     
    public String doInput() throws Exception
    {    	
		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);

		if(this.qualifyerXML != null && !this.qualifyerXML.equals(""))
        {
            this.qualifyers = parseContentsFromXML(this.qualifyerXML);
        }
        else
        {
			ContentVO contentVO = ContentController.getContentController().getContentVOWithId(getContentId());
	        this.qualifyers.add(contentVO);
        }
    	this.returnAddress = "ViewContent.action?contentId=" + this.contentVO.getId() + "&repositoryId=" + this.repositoryId;

        return "input";
    }

    public String doInputV3() throws Exception
    {    	
		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);

		if(this.qualifyerXML != null && !this.qualifyerXML.equals(""))
        {
            this.qualifyers = parseContentsFromXML(this.qualifyerXML);
        }
        else
        {
			ContentVO contentVO = ContentController.getContentController().getContentVOWithId(getContentId());
	        this.qualifyers.add(contentVO);
        }
        
        return "inputV3";
    }

    public String doExecute() throws Exception
    {
        if(this.newParentContentId == null)
        {
    		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
            return "chooseDestination";
        }
        
        ceb.throwIfNotEmpty();
    	
        try
		{
		    if(this.qualifyerXML != null && this.qualifyerXML.length() != 0)
		    {
		        Document document = new DOMBuilder().getDocument(this.qualifyerXML);
				List contents = parseContentsFromXML(this.qualifyerXML);
				Iterator i = contents.iterator();
				while(i.hasNext())
				{
				    ContentVO contentVO = (ContentVO)i.next();
				    try
					{
				        ContentControllerProxy.getController().acMoveContent(this.getInfoGluePrincipal(), contentVO, this.newParentContentId);
					}
					catch(Exception e)
					{
					    this.errorsOccurred = true;
					}
		    	}
		    }
		}
		catch(Exception e)
		{
		    e.printStackTrace();
		}
		
		this.topContentId = ContentController.getContentController().getRootContentVO(this.repositoryId, this.getInfoGluePrincipal().getName()).getContentId();
		    
    	this.returnAddress = "ViewContent.action?contentId=" + this.contentVO.getId() + "&repositoryId=" + this.repositoryId;

        return "success";
    }

    public String doV3() throws Exception
    {
        userSessionKey = "" + System.currentTimeMillis();

        String moveContentInlineOperationDoneHeader = getLocalizedString(getLocale(), "tool.contenttool.moveContentsInlineOperationDoneHeader", contentVO.getName());
		String moveContentInlineOperationBackToCurrentPageLinkText = getLocalizedString(getLocale(), "tool.contenttool.moveContentsInlineOperationBackToCurrentContentLinkText");
		String moveContentInlineOperationBackToCurrentPageTitleText = getLocalizedString(getLocale(), "tool.contenttool.moveContentsInlineOperationBackToCurrentContentTitleText");

	    setActionMessage(userSessionKey, moveContentInlineOperationDoneHeader);
	    addActionLink(userSessionKey, new LinkBean("currentPageUrl", moveContentInlineOperationBackToCurrentPageLinkText, moveContentInlineOperationBackToCurrentPageTitleText, moveContentInlineOperationBackToCurrentPageTitleText, this.originalAddress, false, ""));
        setActionExtraData(userSessionKey, "refreshToolbarAndMenu", "" + true);
        setActionExtraData(userSessionKey, "repositoryId", "" + this.repositoryId);
        setActionExtraData(userSessionKey, "contentId", "" + newParentContentId);
        setActionExtraData(userSessionKey, "unrefreshedContentId", "" + parentContentId);
        setActionExtraData(userSessionKey, "unrefreshedNodeId", "" + parentContentId);
        setActionExtraData(userSessionKey, "changeTypeId", "" + this.changeTypeId);
        System.out.println("newParentContentId:" + newParentContentId);
        System.out.println("parentContentId(contentId):" + parentContentId);
        System.out.println("changeTypeId:" + changeTypeId);
        
    	System.out.println("newParentContentId:" + newParentContentId);
        if(this.newParentContentId == null)
        {
    		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
            return "chooseDestinationV3";
        }
        
        ceb.throwIfNotEmpty();
    	
        try
		{
        	System.out.println("this.qualifyerXML:" + this.qualifyerXML);
            if(this.qualifyerXML != null && this.qualifyerXML.length() != 0)
		    {
		        Document document = new DOMBuilder().getDocument(this.qualifyerXML);
				List contents = parseContentsFromXML(this.qualifyerXML);
				Iterator i = contents.iterator();
				while(i.hasNext())
				{
				    ContentVO contentVO = (ContentVO)i.next();
				    try
					{
				        ContentControllerProxy.getController().acMoveContent(this.getInfoGluePrincipal(), contentVO, this.newParentContentId);
					}
					catch(Exception e)
					{
					    this.errorsOccurred = true;
					}
		    	}
		    }
		}
		catch(Exception e)
		{
		    e.printStackTrace();
		}
		
		this.topContentId = ContentController.getContentController().getRootContentVO(this.repositoryId, this.getInfoGluePrincipal().getName()).getContentId();
		    
        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
	        String arguments 	= "userSessionKey=" + userSessionKey + "&isAutomaticRedirect=false";
	        String messageUrl 	= returnAddress + (returnAddress.indexOf("?") > -1 ? "&" : "?") + arguments;
	        
	        this.getResponse().sendRedirect(messageUrl);
	        return NONE;
        }
        else
        {
        	return "successV3";
        }
    }

    
	private List parseContentsFromXML(String qualifyerXML)
	{
		List contents = new ArrayList(); 
    	
		try
		{
			Document document = new DOMBuilder().getDocument(qualifyerXML);
			
			String entity = document.getRootElement().attributeValue("entity");
			
			Map addedContents = new HashMap();
			
			List children = document.getRootElement().elements();
			Iterator i = children.iterator();
			while(i.hasNext())
			{
				Element child = (Element)i.next();
				String id = child.getStringValue();
				String path = child.attributeValue("path");
				
				if(!addedContents.containsKey(id))
				{
				    ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(id));
				    contents.add(contentVO);
			        addedContents.put(id, contentVO);
				}    
			}		        	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return contents;
	}
    
    public String getErrorKey()
	{
		return "Content.parentContentId";
	}
	
	public String getReturnAddress()
	{
		return this.returnAddress;
	}    
	
    public String getQualifyerXML()
    {
        return qualifyerXML;
    }
    
    public void setQualifyerXML(String qualifyerXML)
    {
        this.qualifyerXML = qualifyerXML;
    }
    
    public List getQualifyers()
    {
        return qualifyers;
    }
    
    public Integer getOriginalContentId()
    {
        return originalContentId;
    }
    
    public void setOriginalContentId(Integer originalContentId)
    {
        this.originalContentId = originalContentId;
    }
    
    public Integer getTopContentId()
    {
        return topContentId;
    }
    
    public boolean getErrorsOccurred()
    {
        return errorsOccurred;
    }
    
    public List getRepositories()
    {
        return repositories;
    }
    
	public String getUserSessionKey()
	{
		return userSessionKey;
	}

	public void setUserSessionKey(String userSessionKey)
	{
		this.userSessionKey = userSessionKey;
	}
	
    public String getOriginalAddress()
	{
		return originalAddress;
	}

	public void setOriginalAddress(String originalAddress)
	{
		this.originalAddress = originalAddress;
	}

	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}

}
