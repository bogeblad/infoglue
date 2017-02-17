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

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;

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
	private Integer assetId;
	
	protected String doExecute() throws Exception 
	{
		String assetUrl = null;
		
		if (contentId != null && assetKey != null && languageId != null)
		{
			try
			{
				// Only create url to asset if it belongs to the latest active content version, otherwise it would be possible to access unpublished assets.
				if (isAssetAvailableInCurrentMode(contentId, languageId, assetKey))
				{
					Integer stateId = getCurrentOperatingMode();
					assetUrl = DigitalAssetController.getDigitalAssetUrlInState(contentId, languageId, assetKey, true, stateId);
				}
				else
				{
					logger.info("Asset not available in the latest active content version. AssetId: " + assetId + ", contentId:" + contentId + " (" + languageId + "/" + assetKey + ")");
				}
			}
			catch(Exception e)
			{
				logger.info("Could not download asset on contentId:" + contentId + " (" + languageId + "/" + assetKey + ")", e);
			}
		}
		else if (assetId != null)
		{
			try 
			{
				// Only create url to asset if it belongs to the latest active content version, otherwise it would be possible to access unpublished assets.
				if (isAssetAvailableInCurrentMode(assetId))
				{
					assetUrl = DigitalAssetController.getDigitalAssetUrl(assetId, false);
				}
				else
				{
					logger.info("Asset not available in the latest active content version. AssetId: " + assetId);
				}
			}
			catch(Exception e)
			{
				logger.info("Could not download asset on assetId:" + assetId, e);
			}
		}

		if (assetUrl != null)
		{
			this.getResponse().sendRedirect(assetUrl);
		} 
		else
		{
			logger.info("Could not find asset since parameters were not set correctly.");
			logger.debug("contentId: " + contentId + ", languageId: " + languageId + ", assetKey: " + assetKey + ", assetId: " + assetId);
			this.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		
		return NONE;
	}
	
	
	/**
	 * Return true if the asset identified by contentId, languageId and assetKey is available in
	 * the current operating mode.
	 */
	boolean isAssetAvailableInCurrentMode(int contentId, int languageId, String assetKey)
	{
		boolean assetAvailable = false;
		try 
		{
			int operatingMode = getCurrentOperatingMode();
			return DigitalAssetController.getController().isAssetAvailableInState(contentId, languageId, assetKey, operatingMode);
		}
		catch (SystemException e) 
		{
			logger.error("Could not check asset availability for contentId: " + contentId + " (" + languageId + "/" + assetKey + ")", e);
		}
		return assetAvailable;
	}

	/**
	 * Return true if the asset identified by assetId is available in
	 * the current operating mode.
	 */
	boolean isAssetAvailableInCurrentMode(int assetId)
	{
		boolean assetAvailable = false;
		try 
		{
			int operatingMode = getCurrentOperatingMode();
			return DigitalAssetController.getController().isAssetAvailableInState(assetId, operatingMode);
		}
		catch (SystemException e) 
		{
			logger.error("Could not check asset availability for asset id " + assetId, e);
		}
		return assetAvailable;
	}

	/**
	 * Returns the current operating mode of the Infoglue webapp
	 */
	protected Integer getCurrentOperatingMode() {
		return new Integer(CmsPropertyHandler.getOperatingMode());
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

	public void setAssetId(Integer assetId) 
	{
		this.assetId = assetId;
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
