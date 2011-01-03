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

package org.infoglue.cms.services;

import java.util.HashMap;
import java.util.List;

import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;

/**
 * This is the service which fetches contents from this content-system. 
 * That is - it fetches it from the internal content-repository. 
 */

public class CoreContentService implements BaseService
{
	/**
	 * As this is a local service for now we just use the ContentController to get us the contents
	 * matching the criterias. It could be interessting in the future to have distributed sources as well
	 * and then this class would act as the adaptor and use SOAP or another tech to query the source.
	 * We must allways convert the result to a list of ContentVO after all.  
	 */
	
	public List selectMatchingEntities(HashMap argumentHashMap) throws Exception
	{
		return ContentController.getContentController().getContentVOList(argumentHashMap);		
	}

	/**
	 * As this is a local service for now we just use the ContentController to get us the contents
	 * matching the criterias. It could be interessting in the future to have distributed sources as well
	 * and then this class would act as the adaptor and use SOAP or another tech to query the source.
	 * We must allways convert the result to a list of ContentVO after all.  
	 */
	
	public List selectMatchingEntities(HashMap argumentHashMap, Database db) throws Exception
	{
		return ContentController.getContentController().getContentVOList(argumentHashMap, db);		
	}
}