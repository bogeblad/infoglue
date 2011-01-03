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

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.RolePropertiesController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserPropertiesController;
import org.infoglue.cms.entities.management.UserPropertiesVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
  * This is the action-class for UpdateUserPropertiesAction
  * 
  * @author Mattias Bogeblad
  */

public class UpdateUserPropertiesAction extends InfoGlueAbstractAction 
{
	private UserPropertiesVO userPropertiesVO;
	private Integer languageId;
	private Integer contentTypeDefinitionId;
	private Integer currentEditorId;
	private String attributeName;
	private String returnAddress;

	private ConstraintExceptionBuffer ceb;
	
	public UpdateUserPropertiesAction()
	{
		userPropertiesVO = new UserPropertiesVO();
		this.ceb = new ConstraintExceptionBuffer();	
	}
		
	public String doExecute() throws Exception
	{
		ceb.throwIfNotEmpty();
		UserPropertiesController.getController().update(this.languageId, this.contentTypeDefinitionId, this.userPropertiesVO);
		
		this.getResponse().sendRedirect(returnAddress);
	    
	    return NONE;
	}

	public String doSaveAndExit() throws Exception
	{
	    UserPropertiesController.getController().update(this.languageId, this.contentTypeDefinitionId, this.userPropertiesVO);
						 
		return "saveAndExit";
	}

	public String doSaveAndExitStandalone() throws Exception
	{
	    UserPropertiesController.getController().update(this.languageId, this.contentTypeDefinitionId, this.userPropertiesVO);
						 
		return "saveAndExitStandalone";
	}

	public String doV3() throws Exception
	{
	    ceb.throwIfNotEmpty();
	    UserPropertiesController.getController().update(this.languageId, this.contentTypeDefinitionId, this.userPropertiesVO);
		
		this.getResponse().sendRedirect(returnAddress);
	    
	    return NONE;
	}

	public String doSaveAndExitV3() throws Exception
	{
		UserPropertiesController.getController().update(this.languageId, this.contentTypeDefinitionId, this.userPropertiesVO);
						 
		return "saveAndExitV3";
	}

	public String doSaveAndExitStandaloneV3() throws Exception
	{
		UserPropertiesController.getController().update(this.languageId, this.contentTypeDefinitionId, this.userPropertiesVO);
						 
		return "saveAndExitStandaloneV3";
	}

	public void setEntityId(Integer userPropertiesId)
	{
		this.userPropertiesVO.setUserPropertiesId(userPropertiesId);
	}

	public java.lang.Integer getEntityId()
	{
		return this.userPropertiesVO.getUserPropertiesId();
	}
	
	public Integer getContentTypeDefinitionId()
	{
		return contentTypeDefinitionId;
	}

	public void setContentTypeDefinitionId(Integer contentTypeDefinitionId)
	{
		this.contentTypeDefinitionId = contentTypeDefinitionId;
	}

	public String getUserName()
	{
		return this.userPropertiesVO.getUserName();
	}

	public void setUserName(String userName)
	{
		this.userPropertiesVO.setUserName(userName);
	}

	public String getOwnerEntityId()
	{
	    return this.userPropertiesVO.getUserName();
	}

	public void setOwnerEntityId(String ownerEntityId)
	{
	    this.userPropertiesVO.setUserName(ownerEntityId);
	}

	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
	}

	public java.lang.Integer getLanguageId()
	{
		return this.languageId;
	}
        
	public java.lang.String getValue()
	{
		return this.userPropertiesVO.getValue();
	}
        
	public void setValue(java.lang.String value)
	{
		this.userPropertiesVO.setValue(value);
	}
    
	public Integer getCurrentEditorId() 
	{
		return currentEditorId;
	}

	public void setCurrentEditorId(Integer integer) 
	{
		currentEditorId = integer;
	}

	public String getAttributeName()
	{
		return this.attributeName;
	}

	public void setAttributeName(String attributeName)
	{
		this.attributeName = attributeName;
	}

    public String getReturnAddress()
    {
        return returnAddress;
    }
    
    public void setReturnAddress(String returnAddress)
    {
        this.returnAddress = returnAddress;
    }

}
