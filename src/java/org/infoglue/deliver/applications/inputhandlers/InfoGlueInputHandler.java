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
 
 
package org.infoglue.deliver.applications.inputhandlers;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.deliver.applications.databeans.DatabaseWrapper;


/**
 * This interface is what all input handlers should follow. The input handler interface is the 
 * entry point for customizable data handling from users outside. The handlers could mail, store the data in
 * a database or interact with the user in other ways.
 */

public interface InfoGlueInputHandler
{
	/**
	 * This is the method that is invoked.
	 */
	
	public void processInput(DatabaseWrapper databaseWrapper, Integer siteNodeId, Integer languageId, Integer contentId, Integer formContentId, HashMap parameters, HttpServletRequest request, InfoGluePrincipal infogluePrincipal) throws Exception;
	
	
}
