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
import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;


public class ChangeVersionLanguageAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 2617481307791186845L;

	private Integer repositoryId;
	private Integer contentId;
	private Integer contentVersionId;
	private Integer languageId;
	
	private String returnAddress;
   	private String userSessionKey;
    private String originalAddress;

		    
	private List languageVOList = null;

	public String doInput() throws Exception
	{      		
		this.languageVOList = LanguageController.getController().getLanguageVOList(repositoryId);
		
		return INPUT;
	}

	public String doInputV3() throws Exception
	{      		
		this.languageVOList = LanguageController.getController().getLanguageVOList(repositoryId);
		
		return INPUT + "V3";
	}

    public String doExecute() throws Exception
    {      		
    	ContentVersionController.getContentVersionController().changeVersionLanguage(contentVersionId, languageId);
    	
       	return SUCCESS;
    }

    public String doV3() throws Exception
    {      		
    	ContentVersionController.getContentVersionController().changeVersionLanguage(contentVersionId, languageId);
    	
		if(this.returnAddress != null && !this.returnAddress.equals(""))
	    {
	        String arguments 	= "userSessionKey=" + userSessionKey;
	        String messageUrl 	= returnAddress + (returnAddress.indexOf("?") > -1 ? "&" : "?") + arguments;
	        
	        this.getResponse().sendRedirect(messageUrl);
	        return NONE;
	    }
	    else
	    {
	    	return SUCCESS;
	    }
    }

	public Integer getRepositoryId()
	{
		return repositoryId;
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public Integer getContentId()
	{
		return contentId;
	}

	public void setContentId(Integer contentId)
	{
		this.contentId = contentId;
	}

	public Integer getContentVersionId()
	{
		return contentVersionId;
	}

	public void setContentVersionId(Integer contentVersionId)
	{
		this.contentVersionId = contentVersionId;
	}

	public Integer getLanguageId()
	{
		return languageId;
	}

	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
	}

	public List getLanguageVOList()
	{
		return languageVOList;
	}
     
	public String getReturnAddress() 
	{
		return returnAddress;
	}

	public void setReturnAddress(String returnAddress) 
	{
		this.returnAddress = returnAddress;
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

}
