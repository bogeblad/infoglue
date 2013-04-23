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

import java.util.Collections;
import java.util.List;

import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.InfoGlueSettingsController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.sorters.ReflectionComparator;


/**
  * This is the action-class for UpdateContent
  * 
  * @author Mattias Bogeblad
  */
public class UpdateContentAction extends ViewContentAction //WebworkAbstractAction
{
	private static final long serialVersionUID = 1L;
	
	private ContentVO contentVO = null;
    private Integer repositoryId;
    private Integer contentTypeDefinitionId;
    private Integer newContentTypeDefinitionId;
	
    private ConstraintExceptionBuffer ceb;
	
	public UpdateContentAction()
	{
		this(new ContentVO());
	}
	
	public UpdateContentAction(ContentVO contentVO)
	{
		this.contentVO = contentVO;
		this.ceb = new ConstraintExceptionBuffer();	
	}

	public String doInputContentType() throws Exception
	{
		super.initialize(getContentId());
		
		return "successInputContentType";
	}

	public String doChangeContentType() throws Exception
	{
		super.initialize(getContentId());

		ContentVO oldContentVO = ContentController.getContentController().getContentVOWithId(getContentId());

		ceb = oldContentVO.validate();
		
		ceb.throwIfNotEmpty();

    	ContentControllerProxy.getController().acUpdate(this.getInfoGluePrincipal(), oldContentVO, this.newContentTypeDefinitionId);

		return "successContentTypeChanged";
	}

	public String doSwitchToFolder() throws Exception
	{
		super.initialize(getContentId());

		ContentVO oldContentVO = ContentController.getContentController().getContentVOWithId(getContentId());
		oldContentVO.setIsBranch(true);
		
    	ContentControllerProxy.getController().acUpdate(this.getInfoGluePrincipal(), oldContentVO, null);

		return "successV3";
	}

	public String doSwitchToContent() throws Exception
	{
		super.initialize(getContentId());

		ContentVO oldContentVO = ContentController.getContentController().getContentVOWithId(getContentId());
		oldContentVO.setIsBranch(false);
		
    	ContentControllerProxy.getController().acUpdate(this.getInfoGluePrincipal(), oldContentVO, null);

		return "successV3";
	}

	public String doExecute() throws Exception
	{
		super.initialize(getContentId());
		ContentVO oldContentVO = ContentController.getContentController().getContentVOWithId(getContentId());
		
		this.contentVO.setCreatorName(this.getInfoGluePrincipal().getName());
		//this.contentVO.setCreatorName(oldContentVO.getCreatorName());
		this.contentVO.setIsBranch(oldContentVO.getIsBranch());
		
		if(oldContentVO.getIsProtected() == 1 && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.ChangeAccessRights", "" + getContentId()))
		{
			this.contentVO.setIsProtected(oldContentVO.getIsProtected());
			this.contentVO.setCreatorName(oldContentVO.getCreatorName());
		}

		ceb = this.contentVO.validate();
		
		ceb.throwIfNotEmpty();
    	
    	ContentControllerProxy.getController().acUpdate(this.getInfoGluePrincipal(), this.contentVO, this.contentTypeDefinitionId);
		//ContentController.update(this.contentVO);
		
		return "success";
	}
	
	public String doV3() throws Exception
	{
		super.initialize(getContentId());
		ContentVO oldContentVO = ContentController.getContentController().getContentVOWithId(getContentId());
		
		this.contentVO.setCreatorName(this.getInfoGluePrincipal().getName());
		this.contentVO.setIsBranch(oldContentVO.getIsBranch());
		
		if(oldContentVO.getIsProtected() == 1 && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.ChangeAccessRights", "" + getContentId()))
		{
			this.contentVO.setIsProtected(oldContentVO.getIsProtected());
			this.contentVO.setCreatorName(oldContentVO.getCreatorName());
		}

		ceb = this.contentVO.validate();
		
		ceb.throwIfNotEmpty();
    	
    	ContentControllerProxy.getController().acUpdate(this.getInfoGluePrincipal(), this.contentVO, this.contentTypeDefinitionId);
		
		return "successV3";
	}


	public String doStandalone() throws Exception
	{
		doExecute();
						
		return "successStandalone";
	}
		
	public String doSaveAndExit() throws Exception
    {
		doExecute();
						
		return "saveAndExit";
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

    public String getDefaultFolderContentTypeName()
    {
    	String defaultFolderContentTypeName = InfoGlueSettingsController.getInfoGlueSettingsController().getProperty("repository_" + this.getRepositoryId() + "_defaultFolderContentTypeName", "applicationProperties", null, false, false, false, false, null);
	    if(defaultFolderContentTypeName == null || defaultFolderContentTypeName.equals(""))
	    	defaultFolderContentTypeName = "Folder";

        return defaultFolderContentTypeName;
    }

	public void setContentId(Integer contentId)
	{
		this.contentVO.setContentId(contentId);	
	}

    public java.lang.Integer getContentId()
    {
        return this.contentVO.getContentId();
    }
        
    public java.lang.String getName()
    {
        return this.contentVO.getName();
    }

	public Boolean getIsBranch()
	{
 		return this.contentVO.getIsBranch();
	}    
        
    public void setName(java.lang.String name)
    {
        this.contentVO.setName(name);
    }

	public Integer getIsProtected()
	{
		return this.contentVO.getIsProtected();
	}    
        
	public void setIsProtected(java.lang.Integer isProtected)
	{
		this.contentVO.setIsProtected(isProtected);
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

    public void setContentTypeDefinitionId(Integer contentTypeDefinitionId)
    {
       	this.contentTypeDefinitionId = contentTypeDefinitionId;
    }

    public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}

	public Integer getNewContentTypeDefinitionId()
	{
		return newContentTypeDefinitionId;
	}

	public void setNewContentTypeDefinitionId(Integer newContentTypeDefinitionId)
	{
		this.newContentTypeDefinitionId = newContentTypeDefinitionId;
	}

}
