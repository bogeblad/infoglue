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

package org.infoglue.cms.applications.contenttool.wizards.actions;

import java.util.ArrayList;
import java.util.List;

import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.contenttool.actions.ViewContentTreeActionInterface;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * This action represents the CreateContent with help of a wizard. It guides the user through creating a new content
 * and allows a developer to control it's flow and basic parameters a bit so it steers the user to make correct descisions.
 */

public class CreateContentWizardAction extends InfoGlueAbstractAction implements ViewContentTreeActionInterface
{
	private static final long serialVersionUID = 1L;
	
	//Used by the tree only
	private Integer contentId;
	private String tree;
	private String hideLeafs;
	
	//Used by the second screen only
	private String[] allowedContentTypeDefinitionId;
	private List contentTypeDefinitionVOList = new ArrayList();

	//Used by the content version screen only
	public ContentTypeDefinitionVO contentTypeDefinitionVO;
	public List availableLanguages = null;
	private Integer languageId;
	private Integer contentVersionId;
	private Integer currentEditorId;
	private String textAreaId = "";
	private ContentVersionVO contentVersionVO;
	public List attributes = null;

	//Common
	private Integer parentContentId;
	private Integer contentTypeDefinitionId;
	private Integer repositoryId;
	private ConstraintExceptionBuffer ceb;
	private ContentVO contentVO;
	private ContentVO newContentVO;

	protected void initialize(Integer contentVersionId, Integer contentId, Integer languageId) throws Exception
	{
		this.contentVO = ContentControllerProxy.getController().getACContentVOWithId(this.getInfoGluePrincipal(), contentId);
		//this.contentVO = ContentController.getContentVOWithId(contentId);
		this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentId);
		this.availableLanguages = ContentController.getContentController().getRepositoryLanguages(contentId);
		
		this.languageId = ((LanguageVO)this.availableLanguages.get(0)).getLanguageId();
		/*
		if(contentVersionId == null)
		{	
			//this.contentVersionVO = ContentVersionControllerProxy.getController().getACLatestActiveContentVersionVO(this.getInfoGluePrincipal(), contentId, languageId);
			//this.contentVersionVO = ContentVersionController.getLatestActiveContentVersionVO(contentId, languageId);
			this.contentVersionVO = ContentVersionController.getLatestActiveContentVersionVO(contentId, languageId);
			if(this.contentVersionVO != null)
				contentVersionId = contentVersionVO.getContentVersionId();
		}

		if(contentVersionId != null)	
			this.contentVersionVO = ContentVersionControllerProxy.getController().getACContentVersionVOWithId(this.getInfoGluePrincipal(), contentVersionId);    		 	
			//this.contentVersionVO = ContentVersionController.getContentVersionVOWithId(contentVersionId);    		 	
		*/
		
		this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().validateAndUpdateContentType(this.contentTypeDefinitionVO);
		this.attributes = ContentTypeDefinitionController.getController().getContentTypeAttributes(this.contentTypeDefinitionVO, true);
	} 
	
	/**
	 * This method presents the user with the initial input screen for creating a content.
	 * 
	 * @return
	 * @throws Exception
	 */
	 
	public String doInput() throws Exception
	{
		if(parentContentId == null)
		{
			return "stateLocation";
		}
		
		AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
	
		Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(parentContentId);
		if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.Create", protectedContentId.toString()))
			ceb.add(new AccessConstraintException("Content.contentId", "1002"));

		//if(ContentControllerProxy.getController().getIsContentProtected(parentContentId) && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.Create", parentContentId.toString()))
		//	ceb.add(new AccessConstraintException("Content.contentId", "1002"));

		ceb.throwIfNotEmpty();

		if(allowedContentTypeDefinitionId == null)
		{
			this.contentTypeDefinitionVOList = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
		}
		else
		{
			for(int i=0; i < allowedContentTypeDefinitionId.length; i++)
			{
				String allowedContentTypeDefinitionIdString = allowedContentTypeDefinitionId[i];
				this.contentTypeDefinitionVOList.add(ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(new Integer(allowedContentTypeDefinitionIdString)));
			}
		}
	
		return "input";
	}

	public String doCreateContent() throws Exception
	{
		this.contentVO.setCreatorName(this.getInfoGluePrincipal().getName());

		ceb = this.contentVO.validate();
		ceb.throwIfNotEmpty();
    			
		newContentVO = ContentControllerProxy.getController().acCreate(this.getInfoGluePrincipal(), parentContentId, contentTypeDefinitionId, repositoryId, contentVO);
		
		this.initialize(this.contentVersionId, newContentVO.getContentId(), this.languageId);

		return "createContentSuccess";
	}

	public String doExecute() throws Exception
	{
		this.contentVO.setCreatorName(this.getInfoGluePrincipal().getName());

		ceb = this.contentVO.validate();
		ceb.throwIfNotEmpty();
    			
		newContentVO = ContentControllerProxy.getController().acCreate(this.getInfoGluePrincipal(), parentContentId, contentTypeDefinitionId, repositoryId, contentVO);
		
		return "success";
	}
	
	public Integer getTopRepositoryId() throws ConstraintException, SystemException, Bug
	{
		List repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
		
		Integer topRepositoryId = null;

		if (repositoryId != null)
			topRepositoryId = repositoryId;

		if(repositories.size() > 0)
		{
			topRepositoryId = ((RepositoryVO)repositories.get(0)).getRepositoryId();
		}
  	
		return topRepositoryId;
	}
  
	public CreateContentWizardAction()
	{
		this(new ContentVO());
	}
	
	public CreateContentWizardAction(ContentVO contentVO)
	{
		this.contentVO = contentVO;
		this.ceb = new ConstraintExceptionBuffer();			
	}	

	public void setContentId(Integer contentId)
	{
		this.contentId = contentId;
	}

	public Integer getContentId()
	{
		return this.contentId;
	}    

	public void setHideLeafs(String hideLeafs)
	{
		this.hideLeafs = hideLeafs;
	}

	public String getHideLeafs()
	{
		return this.hideLeafs;
	}    

	public String getTree()
	{
		return tree;
	}

	public void setTree(String tree)
	{
		this.tree = tree;
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

	/**
	 * Returns the repositoryId either sent in or last used by the user or lastly the top one.
	 */

	public Integer getRepositoryId() 
	{
		try
		{
			if(this.repositoryId == null)
			{	
				this.repositoryId = (Integer)getHttpSession().getAttribute("repositoryId");
					
				if(this.repositoryId == null)
				{
					this.repositoryId = getTopRepositoryId();
					getHttpSession().setAttribute("repositoryId", this.repositoryId);		
				}
			}
		}
		catch(Exception e)
		{
		}
	    	
		return repositoryId;
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
     
	public Integer getNewContentId()
	{
		return newContentVO.getContentId();
	}

	
	public List getContentTypeDefinitions() throws Exception
	{
		return ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
	}      
	 
	/*

    
      
    public String doExecute() throws Exception
    {
		this.contentVO.setCreatorName(this.getInfoGluePrincipal().getName());

    	ceb = this.contentVO.validate();
    	ceb.throwIfNotEmpty();
    			
    	newContentVO = ContentControllerProxy.getController().acCreate(this.getInfoGluePrincipal(), parentContentId, contentTypeDefinitionId, repositoryId, contentVO);
		//newContentVO = ContentController.create(parentContentId, contentTypeDefinitionId, repositoryId, contentVO);
    	
    	return "success";
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
		
		if(ContentControllerProxy.getController().getIsContentProtected(parentContentId) && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.Create", parentContentId.toString()))
			ceb.add(new AccessConstraintException("Content.contentId", "1002"));

		ceb.throwIfNotEmpty();
		
		return "input";
    }
    */
    
	public String[] getAllowedContentTypeDefinitionId()
	{
		return allowedContentTypeDefinitionId;
	}

	public void setAllowedContentTypeDefinitionId(String[] strings)
	{
		allowedContentTypeDefinitionId = strings;
	}

	public java.lang.Integer getContentVersionId()
	{
		return this.contentVersionVO.getContentVersionId();
	}

	public void setContentVersionId(java.lang.Integer contentVersionId)
	{
		this.contentVersionVO.setContentVersionId(contentVersionId);
	}
	public List getAttributes()
	{
		return attributes;
	}

	public List getAvailableLanguages()
	{
		return availableLanguages;
	}

	public ContentTypeDefinitionVO getContentTypeDefinitionVO()
	{
		return contentTypeDefinitionVO;
	}

	public ContentVersionVO getContentVersionVO()
	{
		return contentVersionVO;
	}

	public ContentVO getContentVO()
	{
		return contentVO;
	}

	public Integer getCurrentEditorId()
	{
		return currentEditorId;
	}

	public Integer getLanguageId()
	{
		return languageId;
	}

	public ContentVO getNewContentVO()
	{
		return newContentVO;
	}

	public String getTextAreaId()
	{
		return textAreaId;
	}

	public void setAttributes(List list)
	{
		attributes = list;
	}

	/**
	 * This method returns the attributes in the content type definition for generation.
	 */
	
	public List getContentTypeAttributes()
	{   		
		return this.attributes;
	}
	
	public void setAvailableLanguages(List list)
	{
		availableLanguages = list;
	}

	public void setContentTypeDefinitionVO(ContentTypeDefinitionVO definitionVO)
	{
		contentTypeDefinitionVO = definitionVO;
	}

	public void setContentVersionVO(ContentVersionVO versionVO)
	{
		contentVersionVO = versionVO;
	}

	public void setContentVO(ContentVO contentVO)
	{
		this.contentVO = contentVO;
	}

	public void setCurrentEditorId(Integer integer)
	{
		currentEditorId = integer;
	}

	public void setLanguageId(Integer integer)
	{
		languageId = integer;
	}

	public void setNewContentVO(ContentVO contentVO)
	{
		newContentVO = contentVO;
	}

	public void setTextAreaId(String string)
	{
		textAreaId = string;
	}

}
