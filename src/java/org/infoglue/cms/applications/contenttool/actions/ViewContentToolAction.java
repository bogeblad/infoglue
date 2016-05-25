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

import org.infoglue.cms.applications.cmstool.actions.ViewCMSAbstractToolAction;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * This class implements the action class for the framed page in the content tool.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewContentToolAction extends ViewCMSAbstractToolAction
{
	private static final long serialVersionUID = 1L;
	
	private Integer contentId = null;
	private Integer languageId = null;

    public String doExecute() throws Exception
    {
		logUserActionInfo(getClass(), "doExecute");
        return "success";
    }
    
    public Integer getRepositoryId()
    {
    	return getContentRepositoryId();
    }

    public void setRepositoryId(Integer repositoryId)
    {
		if(repositoryId != null)
		{
	   		getHttpSession().setAttribute("contentRepositoryId", repositoryId);
	   		if(CmsPropertyHandler.getUseGlobalRepositoryChange())
		   		getHttpSession().setAttribute("structureRepositoryId", repositoryId);
		}
    }

	public Integer getContentId() 
	{
		return contentId;
	}

	public void setContentId(Integer contentId) 
	{
		this.contentId = contentId;
	}

	public Integer getLanguageId() 
	{
		return languageId;
	}

	public void setLanguageId(Integer languageId) 
	{
		this.languageId = languageId;
	}

}
