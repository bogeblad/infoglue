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

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;

/**
* This action downloads an asset from the system.
* 
* @author Mattias Bogeblad
*/

public class DownloadAssetAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(DownloadAssetAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private Integer contentId;
	private Integer languageId;
	private String assetKey;
	
	protected String doExecute() throws Exception 
	{
		String assetUrl = "";
		
		try
		{
			assetUrl = DigitalAssetController.getDigitalAssetUrl(contentId, languageId, assetKey, true);
			this.getResponse().sendRedirect(assetUrl);
		}
		catch(Exception e)
		{
			logger.warn("Could not download asset on contentId:" + contentId + " (" + languageId + "/" + assetKey + ")");
		}
		
		return NONE;
	}

	public String getAssetKey() 
	{
		return assetKey;
	}
	
	public void setAssetKey(String assetKey) 
	{
		this.assetKey = assetKey;
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
