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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.LabelController;
import org.infoglue.cms.controllers.kernel.impl.simple.ThemeController;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.Chat;
import org.infoglue.cms.entities.management.Message;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.CmsSessionContextListener;
import org.infoglue.cms.util.FileUploadHelper;

import webwork.action.ActionContext;

/**
 * This class handles all actions towards the themes directory
 * 
 * @author Mattias Bogeblad
 */

public class ViewThemesAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;

    private final static Logger logger = Logger.getLogger(ViewThemesAction.class.getName());

	private List<String> themes = new ArrayList<String>();
	private String theme = null;
	
	public String doExecute() throws Exception
    {
		this.themes = ThemeController.getController().getAvailableThemes();
		
    	return "success";
    }

    public String doInput() throws Exception
    {
    	return "input";
    }

    public String doAdd() throws Exception
    {
		File file = FileUploadHelper.getUploadedFile(ActionContext.getContext().getMultiPartRequest());
		if(file == null || !file.exists())
			throw new SystemException("The file upload must have gone bad as no file reached this action.");

		File newFile = new File(CmsPropertyHandler.getContextRootPath() + File.separator + "css" + File.separator + "skins" + File.separator + file.getName());

		file.renameTo(newFile);
		
		FileHelper.unzipFile(newFile, CmsPropertyHandler.getContextRootPath() + File.separator + "css" + File.separator + "skins");
		
		// Create Digital Asset for label
		logger.info("Creating Digital Asset for themes");
		DigitalAssetVO newAsset = new DigitalAssetVO();
		newAsset.setAssetContentType("zip/infoglue-theme");
		newAsset.setAssetKey("theme");
		newAsset.setAssetFileName(newFile.getName());
		newAsset.setAssetFilePath(newFile.getPath());
		newAsset.setAssetFileSize(new Integer(new Long(newFile.length()).intValue()));

		// Check existance of presentation string and remove old ones
		List assets = ThemeController.getDigitalAssetByName(newFile.getName());
		if (assets != null && assets.size() > 0)
		{
			logger.info("Removing old instance of " + newFile.getName());
			for (Iterator it = assets.iterator(); it.hasNext();)
			{
				DigitalAsset oldAsset = (DigitalAsset) it.next();
				ThemeController.delete(oldAsset.getId());
			}
		}

		logger.info("Storing Digital Asset (Theme) " + newFile.getName());
		InputStream is = new FileInputStream(newFile);
		DigitalAsset digitalAsset = ThemeController.create(newAsset, is);
		is.close();
		logger.debug("Digital Asset stored as id = " + digitalAsset.getId());

		newFile.delete();
		
    	return doExecute();
    }

    public String doDelete() throws Exception
    {
		File file = new File(CmsPropertyHandler.getContextRootPath() + File.separator + "css" + File.separator + "skins" + File.separator + theme);

		System.out.println("file:" + file + ":" + file.exists());
		
		// Check existance of presentation string and remove old ones
		List assets = ThemeController.getDigitalAssetByName(file.getName());
		if (assets != null && assets.size() > 0)
		{
			logger.info("Removing old instance of " + file.getName());
			for (Iterator it = assets.iterator(); it.hasNext();)
			{
				DigitalAsset oldAsset = (DigitalAsset) it.next();
				ThemeController.delete(oldAsset.getId());
			}
		}
		
		if(file.exists())
			FileHelper.deleteDirectory(file);

		return doExecute();
    }

    public List<String> getThemes()
	{
		return themes;
	}

    public void setTheme(String theme)
    {
    	this.theme = theme;
    }
}
