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
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.LabelController;
import org.infoglue.cms.controllers.kernel.impl.simple.PortletAssetController;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.FileUploadHelper;

import webwork.action.ActionContext;

/**
 * This class handles all actions towards the labels directory
 * 
 * @author Mattias Bogeblad
 */

public class ViewLabelsAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;

    private final static Logger logger = Logger.getLogger(ViewLabelsAction.class.getName());

	private List<Locale> translations = new ArrayList<Locale>();
	private String translation = null;
	
	public String doExecute() throws Exception
    {
    	this.translations = LabelController.getController(getLocale()).getAvailableTranslations();
    	
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

		File newFile = new File(CmsPropertyHandler.getContextRootPath() + File.separator + "translations" + File.separator + file.getName().substring(file.getName().indexOf("Presentation")));

		file.renameTo(newFile);

		// Create Digital Asset for label
		logger.info("Creating Digital Asset for label");
		DigitalAssetVO newAsset = new DigitalAssetVO();
		newAsset.setAssetContentType("text/infoglue-translation");
		newAsset.setAssetKey("translation");
		newAsset.setAssetFileName(newFile.getName());
		newAsset.setAssetFilePath(newFile.getPath());
		newAsset.setAssetFileSize(new Integer(new Long(newFile.length()).intValue()));

		// Check existance of presentation string and remove old ones
		List assets = LabelController.getDigitalAssetByName(newFile.getName());
		if (assets != null && assets.size() > 0)
		{
			logger.info("Removing old instance of " + newFile.getName());
			for (Iterator it = assets.iterator(); it.hasNext();)
			{
				DigitalAsset oldAsset = (DigitalAsset) it.next();
				LabelController.delete(oldAsset.getId());
			}
		}

		logger.info("Storing Digital Asset (portlet) " + newFile.getName());
		InputStream is = new FileInputStream(newFile);
		DigitalAsset digitalAsset = LabelController.create(newAsset, is);
		is.close();
		logger.debug("Digital Asset stored as id = " + digitalAsset.getId());
		
    	return doExecute();
    }

    public String doDelete() throws Exception
    {
		File file = new File(CmsPropertyHandler.getContextRootPath() + File.separator + "translations" + File.separator + translation);
		
		// Check existance of presentation string and remove old ones
		List assets = LabelController.getDigitalAssetByName(file.getName());
		if (assets != null && assets.size() > 0)
		{
			logger.info("Removing old instance of " + file.getName());
			for (Iterator it = assets.iterator(); it.hasNext();)
			{
				DigitalAsset oldAsset = (DigitalAsset) it.next();
				LabelController.delete(oldAsset.getId());
			}
		}

		System.out.println("file:" + file + ":" + file.exists());
		if(file.exists())
			file.delete();

		return doExecute();
    }

    public List<Locale> getTranslations()
	{
		return translations;
	}

    public void setTranslation(String translation)
    {
    	this.translation = translation;
    }
    
}
