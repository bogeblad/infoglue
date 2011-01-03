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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * This action represents the create content step in the wizards.
 */

public class CreateContentWizardInputContentAction extends InfoGlueAbstractAction
{
	private List contentTypeDefinitionVOList = new ArrayList();
	private String returnAddress;
	private ContentVO contentVO;
	private Integer contentTypeDefinitionId;
	private ConstraintExceptionBuffer ceb;

	private String[] allowedContentTypeIds	 = null;


	public CreateContentWizardInputContentAction()
	{
		this(new ContentVO());
	}
	
	public CreateContentWizardInputContentAction(ContentVO contentVO)
	{
		this.contentVO = contentVO;
		this.ceb = new ConstraintExceptionBuffer();			
	}	

	private void initialiaze() throws Exception
	{
		if(allowedContentTypeIds == null)
		{
			this.contentTypeDefinitionVOList = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
		}
		else
		{
			for(int i=0; i < allowedContentTypeIds.length; i++)
			{
				String allowedContentTypeDefinitionIdString = allowedContentTypeIds[i];
				this.contentTypeDefinitionVOList.add(ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(new Integer(allowedContentTypeDefinitionIdString)));
			}
		}		
	}
	
	/**
	 * This method presents the user with the initial input screen for creating a content.
	 * 
	 * @return
	 * @throws Exception
	 */
	 
	public String doInput() throws Exception
	{
		initialiaze();
		return "input";
	}

	/**
	 * This method validates the input and handles any deviations.
	 * 
	 * @return
	 * @throws Exception
	 */
	 
	public String doExecute() throws Exception
	{
		this.contentVO.setCreatorName(this.getInfoGluePrincipal().getName());

		ceb = this.contentVO.validate();
	
		if(!ceb.isEmpty())
			initialiaze();
	
		ceb.throwIfNotEmpty();
		
		return "success";
	}

	/**
	 * This method fetches the list of ContentTypeDefinitions
	 */
	
	public List getContentTypeDefinitions() throws Exception
	{
		return this.contentTypeDefinitionVOList;
	}      

	public java.lang.String getName()
	{
		return this.contentVO.getName();
	}

	public void setName(String name)
	{
		this.contentVO.setName(name);
	}

	public String getPublishDateTime()
	{    		
		return new VisualFormatter().formatDate(this.contentVO.getPublishDateTime(), "yyyy-MM-dd HH:mm");
	}

	public void setPublishDateTime(String publishDateTime)
	{
		this.contentVO.setPublishDateTime(new VisualFormatter().parseDate(publishDateTime, "yyyy-MM-dd HH:mm"));
	}
        
	public String getExpireDateTime()
	{
		return new VisualFormatter().formatDate(this.contentVO.getExpireDateTime(), "yyyy-MM-dd HH:mm");
	}

	public void setExpireDateTime(String expireDateTime)
	{
		this.contentVO.setExpireDateTime(new VisualFormatter().parseDate(expireDateTime, "yyyy-MM-dd HH:mm"));
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

	public void setIsBranch(Boolean isBranch)
	{
		this.contentVO.setIsBranch(isBranch);
	}
            
	public Integer getContentTypeDefinitionId()
	{
		return this.contentTypeDefinitionId;
	}

	public void setContentTypeDefinitionId(Integer contentTypeDefinitionId)
	{
		this.contentTypeDefinitionId = contentTypeDefinitionId;
	}
	
	public String getReturnAddress()
	{
		return returnAddress;
	}

	public void setReturnAddress(String string)
	{
		returnAddress = string;
	}

    public String getAllowedContentTypeIdsAsUrlEncodedString() throws Exception
    {
        StringBuffer sb = new StringBuffer();
        
        for(int i=0; i<allowedContentTypeIds.length; i++)
        {
            if(i > 0)
                sb.append("&");
            
            sb.append("allowedContentTypeIds=" + URLEncoder.encode(allowedContentTypeIds[i], "UTF-8"));
        }

        return sb.toString();
    }

    public String[] getAllowedContentTypeIds()
    {
        return allowedContentTypeIds;
    }
    
    public void setAllowedContentTypeIds(String[] allowedContentTypeIds)
    {
        this.allowedContentTypeIds = allowedContentTypeIds;
    }

}
