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

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.sorters.ReflectionComparator;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
 * This action represents the CreateContent Usecase.
 */

public class CreateContentAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;

    private final static Logger logger = Logger.getLogger(CreateContentAction.class.getName());

	private Integer parentContentId;
    private Integer contentTypeDefinitionId;
    private Integer repositoryId;
   	private ConstraintExceptionBuffer ceb;
   	private ContentVO contentVO;
   	private ContentVO newContentVO;
   	private ContentVO parentContentVO;
   	private String defaultFolderContentTypeName;
   	private String allowedContentTypeNames;
   	private String defaultContentTypeName;
  
   	private String userSessionKey;
   	private Integer changeTypeId = new Integer(0);
    private String returnAddress;
    private String originalAddress;

  	public CreateContentAction()
	{
		this(new ContentVO());
	}
	
	public CreateContentAction(ContentVO contentVO)
	{
		this.contentVO = contentVO;
		this.ceb = new ConstraintExceptionBuffer();			
	}	

	public void setParentContentId(Integer parentContentId)
	{
		this.parentContentId = parentContentId;
	}

	public Integer getParentContentId()
	{
		return this.parentContentId;
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}

	public void setContentTypeDefinitionId(Integer contentTypeDefinitionId)
	{
		this.contentTypeDefinitionId = contentTypeDefinitionId;
	}

	public Integer getContentTypeDefinitionId()
	{
		return this.contentTypeDefinitionId;
	}	
	
    public java.lang.String getName()
    {
        return this.contentVO.getName();
    }

    public String getPublishDateTime()
    {    		
        return new VisualFormatter().formatDate(this.contentVO.getPublishDateTime(), "yyyy-MM-dd HH:mm");
    }
        
    public String getExpireDateTime()
    {
        return new VisualFormatter().formatDate(this.contentVO.getExpireDateTime(), "yyyy-MM-dd HH:mm");
    }

   	public long getPublishDateTimeAsLong()
    {    		
        return this.contentVO.getPublishDateTime().getTime();
    }
        
    public long getExpireDateTimeAsLong()
    {
        return this.contentVO.getExpireDateTime().getTime();
    }
    
	public Boolean getIsBranch()
	{
 		return this.contentVO.getIsBranch();
	}    
            
    public void setName(java.lang.String name)
    {
    	this.contentVO.setName(name);
    }
    	
    public void setPublishDateTime(String publishDateTime)
    {
   		this.contentVO.setPublishDateTime(new VisualFormatter().parseDate(publishDateTime, "yyyy-MM-dd HH:mm"));
    }

    public void setExpireDateTime(String expireDateTime)
    {
       	this.contentVO.setExpireDateTime(new VisualFormatter().parseDate(expireDateTime, "yyyy-MM-dd HH:mm"));
	}
 
    public void setIsBranch(Boolean isBranch)
    {
       	this.contentVO.setIsBranch(isBranch);
    }
     
	public Integer getContentId()
	{
		return newContentVO.getContentId();
	}

    public String getDefaultFolderContentTypeName()
    {
        return defaultFolderContentTypeName;
    }

	/**
	 * This method fetches the list of ContentTypeDefinitions
	 */
	
	public List getContentTypeDefinitions() throws Exception
	{	
	    List contentTypeVOList = null;
	    
	    String protectContentTypes = CmsPropertyHandler.getProtectContentTypes();
	    if(protectContentTypes != null && protectContentTypes.equalsIgnoreCase("true"))
	        contentTypeVOList = ContentTypeDefinitionController.getController().getAuthorizedContentTypeDefinitionVOList(this.getInfoGluePrincipal());
		else
		    contentTypeVOList = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
	    
	    Collections.sort(contentTypeVOList, new ReflectionComparator("name"));
	    
	    return contentTypeVOList;
	}      
    
      
    public String doExecute() throws Exception
    {
		this.contentVO.setCreatorName(this.getInfoGluePrincipal().getName());

    	ceb = this.contentVO.validate();
    	ceb.throwIfNotEmpty();
    			
    	newContentVO = ContentControllerProxy.getController().acCreate(this.getInfoGluePrincipal(), parentContentId, contentTypeDefinitionId, repositoryId, contentVO);
		//newContentVO = ContentController.create(parentContentId, contentTypeDefinitionId, repositoryId, contentVO);
    	
        if ( newContentVO.getIsBranch().booleanValue() )
        {
            Map args = new HashMap();
            args.put("globalKey", "infoglue");
            PropertySet ps = PropertySetManager.getInstance("jdbc", args);
    
            String allowedContentTypeNames  = ps.getString("content_" + this.getParentContentId() + "_allowedContentTypeNames");
            String defaultContentTypeName = ps.getString("content_" + this.getParentContentId() + "_defaultContentTypeName");
            String initialLanguageId  = ps.getString("content_" + this.getParentContentId() + "_initialLanguageId");
            
            if ( allowedContentTypeNames != null )
            {
                ps.setString("content_" + this.getContentId() + "_allowedContentTypeNames", allowedContentTypeNames );
            }
            if ( defaultContentTypeName != null )
            {
            ps.setString("content_" + this.getContentId() + "_defaultContentTypeName", defaultContentTypeName );
            }
            if ( initialLanguageId != null )
            {
                ps.setString("content_" + this.getContentId() + "_initialLanguageId", initialLanguageId );
            }
        }        
    	return "success";
    }
    
    public String doXML() throws Exception
    {
    	try
    	{
    		
		this.contentVO.setCreatorName(this.getInfoGluePrincipal().getName());

    	ceb = this.contentVO.validate();
    	ceb.throwIfNotEmpty();
    			
    	newContentVO = ContentControllerProxy.getController().acCreate(this.getInfoGluePrincipal(), parentContentId, contentTypeDefinitionId, repositoryId, contentVO);

		getResponse().setContentType("text/xml");
		PrintWriter out = getResponse().getWriter();
		out.println("" + newContentVO.getId());
    	}
    	catch (Exception e) 
    	{
    		e.printStackTrace();
		}
    	
    	return NONE;
    }

	public String doBindingView() throws Exception
	{
		doExecute();
		return "bindingView";
	}
	
	public String doTreeView() throws Exception
	{
		doExecute();
		return "treeView";
	}

    public String doInput() throws Exception
    {
		AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
		Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(parentContentId);
		if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.Create", protectedContentId.toString()))
			ceb.add(new AccessConstraintException("Content.contentId", "1002"));
		
		Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);

		if(this.getIsBranch().booleanValue())
		{
		    this.defaultFolderContentTypeName = ps.getString("repository_" + this.getRepositoryId() + "_defaultFolderContentTypeName");
		}
		else
		{
		    this.defaultContentTypeName = ps.getString("content_" + this.parentContentId + "_defaultContentTypeName");
		}
        if ( ps.exists( "content_" + this.parentContentId + "_allowedContentTypeNames" ) )
        {
            this.allowedContentTypeNames = ps.getString("content_" + this.parentContentId + "_allowedContentTypeNames");
        }
		ceb.throwIfNotEmpty();
		
		return "input";
    }

    public String doInputV3() throws Exception
    {
		AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
		Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(parentContentId);
		if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.Create", protectedContentId.toString()))
			ceb.add(new AccessConstraintException("Content.contentId", "1002"));
		
		Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);

		if(this.getIsBranch().booleanValue())
		{
		    this.defaultFolderContentTypeName = ps.getString("repository_" + this.getRepositoryId() + "_defaultFolderContentTypeName");
		}
		else
		{
		    this.defaultContentTypeName = ps.getString("content_" + this.parentContentId + "_defaultContentTypeName");
		}

		if (ps.exists("content_" + this.parentContentId + "_allowedContentTypeNames"))
        {
            this.allowedContentTypeNames = ps.getString("content_" + this.parentContentId + "_allowedContentTypeNames");
        }
		
        userSessionKey = "" + System.currentTimeMillis();

		parentContentVO = ContentControllerProxy.getController().getContentVOWithId(parentContentId);

		String createContentInlineOperationDoneHeader = getLocalizedString(getLocale(), "tool.contenttool.createContentInlineOperationDoneHeader", parentContentVO.getName());
		String createContentInlineOperationBackToCurrentContentLinkText = getLocalizedString(getLocale(), "tool.contenttool.createContentInlineOperationBackToCurrentContentText");
		String createContentInlineOperationBackToCurrentContentTitleText = getLocalizedString(getLocale(), "tool.contenttool.createContentInlineOperationBackToCurrentContentTitleText");

	    setActionMessage(userSessionKey, createContentInlineOperationDoneHeader);
	    addActionLink(userSessionKey, new LinkBean("currentContentUrl", createContentInlineOperationBackToCurrentContentLinkText, createContentInlineOperationBackToCurrentContentTitleText, createContentInlineOperationBackToCurrentContentTitleText, this.originalAddress, false, ""));

		ceb.throwIfNotEmpty();
		
		return "inputV3";
    }
    
    public String doExecuteV3() throws Exception
    {
    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
    		this.contentVO.setCreatorName(this.getInfoGluePrincipal().getName());

        	ceb = this.contentVO.validate();
        	ceb.throwIfNotEmpty();
        			
        	newContentVO = ContentControllerProxy.getController().acCreate(this.getInfoGluePrincipal(), parentContentId, contentTypeDefinitionId, repositoryId, contentVO);
        	
            if ( newContentVO.getIsBranch().booleanValue() )
            {
                Map args = new HashMap();
                args.put("globalKey", "infoglue");
                PropertySet ps = PropertySetManager.getInstance("jdbc", args);
        
                String allowedContentTypeNames  = ps.getString("content_" + this.getParentContentId() + "_allowedContentTypeNames");
                String defaultContentTypeName = ps.getString("content_" + this.getParentContentId() + "_defaultContentTypeName");
                String initialLanguageId  = ps.getString("content_" + this.getParentContentId() + "_initialLanguageId");
                
                if ( allowedContentTypeNames != null )
                {
                    ps.setString("content_" + this.getContentId() + "_allowedContentTypeNames", allowedContentTypeNames );
                }
                if ( defaultContentTypeName != null )
                {
                ps.setString("content_" + this.getContentId() + "_defaultContentTypeName", defaultContentTypeName );
                }
                if ( initialLanguageId != null )
                {
                    ps.setString("content_" + this.getContentId() + "_initialLanguageId", initialLanguageId );
                }
            }        
            
            commitTransaction(db);

    		String createContentInlineOperationViewCreatedContentLinkText = getLocalizedString(getLocale(), "tool.contenttool.createContentInlineOperationViewCreatedContentLinkText");
    		String createContentInlineOperationViewCreatedContentTitleText = getLocalizedString(getLocale(), "tool.contenttool.createContentInlineOperationViewCreatedContentTitleText");

    		addActionLinkFirst(userSessionKey, new LinkBean("newPageUrl", createContentInlineOperationViewCreatedContentLinkText, createContentInlineOperationViewCreatedContentTitleText, createContentInlineOperationViewCreatedContentTitleText, "ViewContent!V3.action?contentId=" + newContentVO.getId(), false, "", "content", newContentVO.getName()));
            setActionExtraData(userSessionKey, "refreshToolbarAndMenu", "" + true);
            setActionExtraData(userSessionKey, "repositoryId", "" + newContentVO.getRepositoryId());
            setActionExtraData(userSessionKey, "contentId", "" + newContentVO.getId());
            setActionExtraData(userSessionKey, "unrefreshedContentId", "" + parentContentId);
            setActionExtraData(userSessionKey, "unrefreshedNodeId", "" + parentContentId);
            setActionExtraData(userSessionKey, "changeTypeId", "" + this.changeTypeId);
        }
        catch(ConstraintException ce)
        {
        	logger.warn("An error occurred so we should not complete the transaction:" + ce);
            rollbackTransaction(db);

            parentContentVO = ContentControllerProxy.getController().getContentVOWithId(parentContentId);

			ce.setResult(INPUT + "V3");
			throw ce;
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    	        
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


    public String getAllowedContentTypeNames()
    {
        return allowedContentTypeNames;
    }
    
    public String getDefaultContentTypeName()
    {
        return defaultContentTypeName;
    }
    
	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}

	public String getReturnAddress()
	{
		return returnAddress;
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
	
  	public Integer getChangeTypeId()
	{
		return changeTypeId;
	}

	public void setChangeTypeId(Integer changeTypeId)
	{
		this.changeTypeId = changeTypeId;
	}
}
