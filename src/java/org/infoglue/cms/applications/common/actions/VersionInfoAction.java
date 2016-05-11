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

package org.infoglue.cms.applications.common.actions;

import org.apache.log4j.Logger;
import org.infoglue.cms.util.CmsPropertyHandler;

import webwork.action.Action;


/** 
 * This class contains methods to show system/version information.
 */

public class VersionInfoAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(VersionInfoAction.class.getName());

	private static final long serialVersionUID = 1L;
		
	public String getAboutHTML()
	{
		return CmsPropertyHandler.getAboutHTML(this.getLocale());
	}

	protected String doExecute() throws Exception
    {
		logUserActionInfo(getClass(), "doExecute");
		return Action.SUCCESS;
    }

}