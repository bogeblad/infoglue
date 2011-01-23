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

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.util.ConstraintExceptionBuffer;


public class CreateContentTypeDefinitionAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(CreateContentTypeDefinitionAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private ContentTypeDefinitionVO contentTypeDefinitionVO;
	private ConstraintExceptionBuffer ceb;
    private String name;
    private String description;
	
	public CreateContentTypeDefinitionAction()
	{
		this(new ContentTypeDefinitionVO());
	}
	
	public CreateContentTypeDefinitionAction(ContentTypeDefinitionVO contentTypeDefinitionVO)
	{
		this.contentTypeDefinitionVO = contentTypeDefinitionVO;
		
		String schemaValue = "";
		try
		{
			schemaValue = FileHelper.getStreamAsString(this.getClass().getResourceAsStream("/org/infoglue/cms/applications/defaultContentTypeDefinition.xml"));
		}
		catch(Exception e)
		{
			logger.error("The system could not find the default content type definition:" + e.getMessage(), e);
		}
		
		this.contentTypeDefinitionVO.setSchemaValue(schemaValue);
		this.ceb = new ConstraintExceptionBuffer();
			
	}	
    
    public Integer getContentTypeDefinitionId()
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
      
/*
    public String getSchemaValue()
    {
    	return this.contentTypeDefinitionVO.getSchemaValue();
    }
        
    public void setSchemaValue(String schemaValue)
    {
       	this.contentTypeDefinitionVO.setSchemaValue(schemaValue);
    }
*/


    public String doExecute() throws Exception
    {
		ceb.add( this.contentTypeDefinitionVO.validate());
    	ceb.throwIfNotEmpty();				
    	this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().create(this.contentTypeDefinitionVO);
		
        return "success";
    }
        
    public String doInput() throws Exception
    {
    	return "input";
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

	public List<ContentTypeDefinitionVO> getContentTypeDefinitions() throws Exception
	{
		return ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList(ContentTypeDefinitionVO.CONTENT);
	}

}
