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

import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;


/**
  * This is the action-class for UpdateContentTypeDefinition
  * 
  * @author Mattias Bogeblad
  */
public class UpdateContentTypeDefinitionAction extends ViewContentTypeDefinitionAction //WebworkAbstractAction
{
	
	private ContentTypeDefinitionVO contentTypeDefinitionVO;
	private ConstraintExceptionBuffer ceb;
	
	public UpdateContentTypeDefinitionAction()
	{
		this(new ContentTypeDefinitionVO());
	}
	
	public UpdateContentTypeDefinitionAction(ContentTypeDefinitionVO contentTypeDefinitionVO)
	{
		this.contentTypeDefinitionVO = contentTypeDefinitionVO;
		this.ceb = new ConstraintExceptionBuffer();	
	}

       	
	public String doExecute() throws Exception
    {
		super.initialize(getContentTypeDefinitionId());

    	ceb.add(this.contentTypeDefinitionVO.validate());
    	ceb.throwIfNotEmpty();		
    	
		ContentTypeDefinitionController.getController().update(contentTypeDefinitionVO.getParentId(), contentTypeDefinitionVO);
				
		return "success";
	}

	public String doSaveAndExit() throws Exception
    {
		doExecute();
						
		return "saveAndExit";
	}
	
	public void setContentTypeDefinitionId(Integer contentTypeDefinitionId) throws Exception
	{
		this.contentTypeDefinitionVO.setContentTypeDefinitionId(contentTypeDefinitionId);	
	}

    public java.lang.Integer getContentTypeDefinitionId()
    {
        return this.contentTypeDefinitionVO.getContentTypeDefinitionId();
    }
        
    public java.lang.String getName()
    {    
    	return this.contentTypeDefinitionVO.getName();
    }
        
    public void setName(java.lang.String name)
    {
       	this.contentTypeDefinitionVO.setName(name);
    }

    public String getSchemaValue()
    {
        return this.contentTypeDefinitionVO.getSchemaValue();
    }
        
    public void setSchemaValue(String schemaValue)
    {
      	this.contentTypeDefinitionVO.setSchemaValue(schemaValue);
    }
    
	public Integer getType()
	{
		return this.contentTypeDefinitionVO.getType();
	}

	public void setType(Integer type)
	{
		this.contentTypeDefinitionVO.setType(type);
	}
	
	public void setDetailPageResolverClass(String detailPageResolverClass)
	{
		this.contentTypeDefinitionVO.setDetailPageResolverClass(detailPageResolverClass);
	}

	public void setDetailPageResolverData(String detailPageResolverData)
	{
		this.contentTypeDefinitionVO.setDetailPageResolverData(detailPageResolverData);
	}

	public void setParentContentTypeDefinitionId(Integer parentContentTypeDefinitionId)
	{
		this.contentTypeDefinitionVO.setParentId(parentContentTypeDefinitionId);
	}

}
