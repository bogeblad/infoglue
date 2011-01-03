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

package org.infoglue.cms.applications.cmstool.actions;

import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.contenttool.actions.CreateContentAndAssetFromUploadAction;
import org.infoglue.cms.applications.databeans.InfoglueTool;
import org.infoglue.cms.services.AdminToolsService;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * This class implements the action class for the base fram for the entire tool.
 * 
 * @author Mattias Bogeblad
 */

public class AdminAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = -2904286525405758091L;
	
	public String doExecute() throws Exception
    {
		Object o = Class.forName("org.infoglue.cms.applications.contenttool.actions.CreateContentAndAssetFromUploadAction").newInstance();
		System.out.println("o:" + o);
		System.out.println("o:" + ((CreateContentAndAssetFromUploadAction)o).doInput());

		String preferredGUI = CmsPropertyHandler.getDefaultGUI(getUserName());
		if(preferredGUI.equalsIgnoreCase("classic"))
			return "successClassic";
		else
			return "success";
    }

	public String doEmbla() throws Exception
    {
		String preferredGUI = CmsPropertyHandler.getDefaultGUI(getUserName());
		if(preferredGUI.equalsIgnoreCase("classic"))
			return "successClassic";
		else
			return "successEmbla";
    }
	
	public String doResetGUI() throws Exception
    {
		this.getHttpSession().removeAttribute("repositoryId");
		setLanguageCode(CmsPropertyHandler.getPreferredLanguageCode(getUserName()));
		
		return "successReset";
    }
	
}
