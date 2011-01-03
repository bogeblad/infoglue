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

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;


/**
 * This action represents a base class all steps in the wizard uses.
 */

public abstract class CreateContentWizardAbstractAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(CreateContentWizardAbstractAction.class.getName());

	/**
	 * This methods returns a new or the stored InfoBean for the wizard. 
	 * @return
	 */
	
	protected CreateContentWizardInfoBean getCreateContentWizardInfoBean()
	{
		CreateContentWizardInfoBean createContentWizardInfoBean = (CreateContentWizardInfoBean)this.getHttpSession().getAttribute("CreateContentWizardInfoBean");
		if(createContentWizardInfoBean == null)
		{
			createContentWizardInfoBean = new CreateContentWizardInfoBean();
			this.getHttpSession().setAttribute("CreateContentWizardInfoBean", createContentWizardInfoBean);
		}

		return createContentWizardInfoBean;
	}

	/**
	 * This methods invalidates the wizard bean so a new wizard can be started. 
	 */
	
	protected void invalidateCreateContentWizardInfoBean()
	{
		this.getHttpSession().removeAttribute("CreateContentWizardInfoBean");
	}

	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetUrl(Integer digitalAssetId) throws Exception
	{
		String imageHref = null;
		try
		{
       		imageHref = DigitalAssetController.getDigitalAssetUrl(digitalAssetId);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the digitalAsset: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}
	
	
	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetThumbnailUrl(Integer digitalAssetId) throws Exception
	{
		String imageHref = null;
		try
		{
       		imageHref = DigitalAssetController.getDigitalAssetThumbnailUrl(digitalAssetId);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the thumbnail: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}

}
