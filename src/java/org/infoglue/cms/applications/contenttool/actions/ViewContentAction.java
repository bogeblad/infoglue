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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.EventController;
import org.infoglue.cms.controllers.kernel.impl.simple.InfoGlueSettingsController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RegistryController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryLanguageController;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.sorters.ReflectionComparator;

import webwork.action.Action;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

public class ViewContentAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewContentAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private Integer unrefreshedContentId 	= new Integer(0);
	private Integer changeTypeId         	= new Integer(0);
	private Integer repositoryId         	= null;
	private List availableLanguages      	= null;
	private ContentTypeDefinitionVO contentTypeDefinitionVO;
   	private String defaultFolderContentTypeName;
   	private Integer languageId 				= null;
   	private String stay 					= null;
	private List referenceBeanList 			= new ArrayList();
	private Boolean inline					= false;

    private ContentVO contentVO;


    public ViewContentAction()
    {
        this(new ContentVO());
    }
    
    public ViewContentAction(ContentVO contentVO)
    {
        this.contentVO = contentVO;
    }
    
    protected void initialize(Integer contentId) throws Exception
    {   
		this.contentVO = ContentControllerProxy.getController().getACContentVOWithId(this.getInfoGluePrincipal(), contentId);
        this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentId);
        this.availableLanguages = RepositoryLanguageController.getController().getAvailableLanguageVOListForRepositoryId(this.contentVO.getRepositoryId());
        
        if(this.repositoryId == null)
            this.repositoryId = this.contentVO.getRepositoryId();
        
        if(this.contentVO.getRepositoryId() != null && !hasAccessTo("Repository.Read", "" + this.contentVO.getRepositoryId()))
        {
    		AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
    		ceb.add(new AccessConstraintException("Content.contentId", "1000"));
    		ceb.throwIfNotEmpty();
        }

        if(this.getIsBranch().booleanValue())
		{
       		this.defaultFolderContentTypeName = InfoGlueSettingsController.getInfoGlueSettingsController().getProperty("repository_" + this.getRepositoryId() + "_defaultFolderContentTypeName", "applicationProperties", null, false, false, false, false, null);
		}
        
		this.referenceBeanList = RegistryController.getController().getReferencingObjectsForContent(contentId, 100);
    } 

    public String doExecute() throws Exception
    {
        try
        {
	        ContentVO contentVO = ContentControllerProxy.getController().getACContentVOWithId(this.getInfoGluePrincipal(), getContentId());
	        
	        if(contentVO.getRepositoryId() != null && !hasAccessTo("Repository.Read", "" + contentVO.getRepositoryId()))
	        {
	        	logger.error("The user " + this.getInfoGluePrincipal().getName() + " had no access to Repository.Read and " + this.contentVO.getRepositoryId() + ". Could be an hacker attempt.");
	    		AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
	    		ceb.add(new AccessConstraintException("Content.contentId", "1000"));
	    		ceb.throwIfNotEmpty();
	        }

	        if((this.stay == null || !this.stay.equalsIgnoreCase("true")) && contentVO.getIsBranch().booleanValue() == false && contentVO.getContentTypeDefinitionId() != null && getShowContentVersionFirst().equalsIgnoreCase("true"))
	        {
	            if(this.repositoryId == null)
	                this.repositoryId = contentVO.getRepositoryId();
	            
		        //this.languageId = getMasterLanguageVO().getId();
		        this.languageId = getInitialLanguageVO().getId();
	            return "viewVersion";
	        }
	        else
	        {
	            this.initialize(getContentId());
	            return "success";
	        }
        }
        catch(ConstraintException ce)
        {
            throw ce;
        }
        catch(Exception e) 
        {
            e.printStackTrace();
        }
        
        return Action.NONE;
    }

	public String doStandalone() throws Exception
	{
		this.initialize(getContentId());
		return "standalone";
	}

	public String doV3() throws Exception
	{
		try
        {
	        ContentVO contentVO = ContentControllerProxy.getController().getACContentVOWithId(this.getInfoGluePrincipal(), getContentId());
	        
	        if(contentVO.getRepositoryId() != null && !hasAccessTo("Repository.Read", "" + contentVO.getRepositoryId()))
	        {
	        	logger.error("The user " + this.getInfoGluePrincipal().getName() + " had no access to Repository.Read and " + this.contentVO.getRepositoryId() + ". Could be an hacker attempt.");
	    		AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
	    		ceb.add(new AccessConstraintException("Content.contentId", "1000"));
	    		ceb.throwIfNotEmpty();
	        }

	        if((this.stay == null || !this.stay.equalsIgnoreCase("true")) && contentVO.getIsBranch().booleanValue() == false && contentVO.getContentTypeDefinitionId() != null && getShowContentVersionFirst().equalsIgnoreCase("true"))
	        {
	            if(this.repositoryId == null)
	                this.repositoryId = contentVO.getRepositoryId();
	            
		        this.languageId = getInitialLanguageVO().getId();
	            return "viewVersionV3";
	        }
	        else
	        {
	            this.initialize(getContentId());
	            return "successV3";
	        }
        }
        catch(ConstraintException ce)
        {
            throw ce;
        }
        catch(Exception e) 
        {
            e.printStackTrace();
        }
        
        return Action.NONE;
	}

    public java.lang.Integer getContentId()
    {
        return this.contentVO.getContentId();
    }
        
    public void setContentId(java.lang.Integer contentId)
    {
	    this.contentVO.setContentId(contentId);
    }
    
    public java.lang.Integer getRepositoryId()
    {
        return this.repositoryId;
    }
        
    public void setRepositoryId(java.lang.Integer repositoryId)
    {
	    this.repositoryId = repositoryId;
    }
    
    public java.lang.Integer getUnrefreshedContentId()
    {
        return this.unrefreshedContentId;
    }
        
    public void setUnrefreshedContentId(java.lang.Integer unrefreshedContentId)
    {
	    this.unrefreshedContentId = unrefreshedContentId;
    }

    public java.lang.Integer getChangeTypeId()
    {
        return this.changeTypeId;
    }

    public void setChangeTypeId(java.lang.Integer changeTypeId)
    {
	    this.changeTypeId = changeTypeId;
    }

    public java.lang.Integer getNewContentId()
    {
        return this.contentVO.getId();
    }

    public String getName()
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

	public Integer getIsProtected()
	{
		return this.contentVO.getIsProtected();
	}    

	public ContentTypeDefinitionVO getContentTypeDefinition()
	{
		return this.contentTypeDefinitionVO;
	}	

	public List getAvailableLanguages()
	{
		return this.availableLanguages;
	}	
	
	
	public ContentVersionVO getLatestContentVersionVO(Integer contentId, Integer languageId)
	{
		ContentVersionVO contentVersionVO = null;
		try
		{
			contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, languageId);
		}
		catch(Exception e)
		{
			logger.error("An error occurred when we tried to get the latest version for the content:" + e.getMessage(), e);
		}
		
		return contentVersionVO;
	}


	
	public EventVO getContentVersionEvent(Integer contentVersionId)
	{
		EventVO eventVO = null;
		try
		{
			ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionId);
			List events = EventController.getEventVOListForEntity(ContentVersion.class.getName(), contentVersion.getId());
			if(events != null && events.size() > 0)
				eventVO = (EventVO)events.get(0);
		}
		catch(Exception e)
		{
			logger.error("An error occurred when we tried to get any events for this version:" + e.getMessage(), e);
		}
		
		return eventVO;
	}

	public EventVO getContentEvent(Integer contentId)
	{
		EventVO eventVO = null;
		try
		{
			List events = EventController.getEventVOListForEntity(Content.class.getName(), contentId);
			if(events != null && events.size() > 0)
				eventVO = (EventVO)events.get(0);
		}
		catch(Exception e)
		{
			logger.error("An error occurred when we tried to get any events for this version:" + e.getMessage(), e);
		}
		
		return eventVO;
	}

	public Integer getMasterLanguageId()  
	{
		try 
		{
			return LanguageController.getController().getMasterLanguage(repositoryId).getLanguageId();
		} 
		catch (Exception e) 
		{
			logger.error("Unable to get master language for repository", e);	
		}
		return null;
	}

	public ContentVO getContentVO()
	{
		return contentVO;
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

	
	public List getContentPath()
	{
		ContentVO contentVO = this.contentVO;
		List ret = new ArrayList();
		// ret.add(0, contentVO);
		
		while (contentVO.getParentContentId() != null)
		{
			try {
				contentVO = ContentControllerProxy.getController().getContentVOWithId(contentVO.getParentContentId());
			} catch (SystemException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Bug e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ret.add(0, contentVO);
		}
		return ret;
	}

	public void setContentVO(ContentVO contentVO)
	{
		this.contentVO = contentVO;
	}
	    
    public String getDefaultFolderContentTypeName()
    {
        return defaultFolderContentTypeName;
    }
    
	public String getShowContentVersionFirst()
	{
	    return CmsPropertyHandler.getShowContentVersionFirst();
	}
	
	public LanguageVO getMasterLanguageVO() throws Exception
	{
	    return LanguageController.getController().getMasterLanguage(repositoryId);
	}

	public LanguageVO getInitialLanguageVO() throws Exception
	{
		String initialLanguageId = InfoGlueSettingsController.getInfoGlueSettingsController().getProperty("content_" + this.getContentId() + "_initialLanguageId", "applicationProperties", null, false, false, false, false, null);
		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(this.getContentId());
		
		while((initialLanguageId == null || initialLanguageId.equalsIgnoreCase("-1")) && contentVO.getParentContentId() != null)
	    {
		    initialLanguageId = InfoGlueSettingsController.getInfoGlueSettingsController().getProperty("content_" + contentVO.getParentContentId() + "_initialLanguageId", "applicationProperties", null, false, false, false, false, null);
		    contentVO = ContentController.getContentController().getContentVOWithId(contentVO.getParentContentId());
	    }
	    
		/*
		ContentController.getContentController().getParentContent(this.getContentId()); 
	    while((initialLanguageId == null || initialLanguageId.equalsIgnoreCase("-1")) && parentContentVO != null)
	    {
	    	//initialLanguageId = ps.getString("content_" + parentContentVO.getId() + "_initialLanguageId");
		    initialLanguageId = InfoGlueSettingsController.getInfoGlueSettingsController().getProperty("content_" + parentContentVO.getId() + "_initialLanguageId", "applicationProperties", null, false, false, false, false, null);
	        parentContentVO = ContentController.getContentController().getParentContent(parentContentVO.getId()); 
	    }
	    */
	    
	    if(initialLanguageId != null && !initialLanguageId.equals("") && !initialLanguageId.equals("-1"))
	        return LanguageController.getController().getLanguageVOWithId(new Integer(initialLanguageId));
	    else
	        return LanguageController.getController().getMasterLanguage(repositoryId);
	}

    public Integer getLanguageId()
    {
        return languageId;
    }
    
    public String getStay()
    {
        return stay;
    }
    
    public void setInline(Boolean inline)
    {
        this.inline = inline;
    }

    public Boolean getInline()
    {
        return inline;
    }
    
    public void setStay(String stay)
    {
        this.stay = stay;
    }

    public List getReferenceBeanList()
    {
        return referenceBeanList;
    }
}
